package game.worldsrv.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.entity.FillMail;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.integration.PF_GM_Manager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.D;
import game.worldsrv.support.Utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 全服补偿邮件服务
 * @author 
 *
 */
@DistrClass(servId = D.SERV_FILL_MAIL, importClass = {List.class,ProduceVo.class} )
public class FillMailService extends GameServiceBase {
	
	/**
	 * 当前所有的补偿邮件
	 */
	Map<Long, FillMail> mails = new HashMap<>();
	
	/** 单次查询的数据条目 **/
	public static final int pageNum = 1000;
	
	public FillMailService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		//初始化补偿邮件
		DB db = DB.newInstance(FillMail.tableName);
		db.countAll(false);
		Param paramCount = db.waitForResult();
		int count = paramCount.get();
		if (count > 0) {
			
			int page = count / pageNum;
			for (int i = 0; i <= page; i++) {

				db.findBy(false, i * pageNum, pageNum);
				Param params = db.waitForResult();
				List<Record> records = params.get();
				for (Record record : records) {
					
					FillMail fillMail = new FillMail(record);
					mails.put(fillMail.getId(), fillMail);
					//System.out.println("补发邮件："+fillMail);
				}
			}
		}
	}
	
	/**
	 * 全服发送补偿邮件
	 */
	@DistrMethod
	public void sendMail(String itemSn,String itemNum,String title,String content,long startTime,long endTime,String eventKey){
		
		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
		if(null != itemSn && null != itemNum && !itemSn.isEmpty() && !itemNum.isEmpty()){
			String [] ids = itemSn.split(PF_GM_Manager.Split);
			String [] ns = itemNum.split(PF_GM_Manager.Split);
			for (int i = 0; i < ids.length; i ++) {
				itemProduce.add(new ProduceVo(Utils.intValue(ids[i]), Utils.intValue(ns[i])));
			}
		}
		
		sendMail(title, content, itemProduce, startTime, endTime, eventKey);
	}
	
	/**
	 * 全服发送补偿邮件
	 */
	private void sendMail(String title, String content, List<ProduceVo> itemProduce, long startTime, long endTime, String eventKey) {
		
		//持久化邮件
		FillMail mail = new FillMail();
		mail.setId(Port.applyId());
		mail.setTitle(title);
		mail.setContent(content);
		mail.setSendTime(Port.getTime());
		mail.setStartTime(startTime);
		mail.setEventKey(eventKey);
		
		if(null != itemProduce && !itemProduce.isEmpty()){			
		    mail.setEndTime(endTime);
		    mail.setItemJSON(ProduceManager.inst().produceToJson(itemProduce));
		}else{
			mail.setEndTime(endTime);
		}
		
		mail.persist();
		
		mails.put(mail.getId(), mail);
		
		if(mail.getStartTime() > Port.getTime()){
			//未到邮件发送时间,返回
			return;
		}
		mail.setSysSendTime(Port.getTime());
		
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendFillMail(mail);
		
	}
	
	/**
	 * 删除邮件
	 * @param eventKey
	 */
	@DistrMethod
	public void deleteMail(String eventKey){
		boolean success = false;
		String reason = "邮件不存在或者已过期";
		Iterator<FillMail> itrs = mails.values().iterator();
		while(itrs.hasNext()){
			FillMail mail = itrs.next();
			if(mail.getEventKey().equals(eventKey)){
				mail.remove();
				itrs.remove();
				success = true;
				reason = "";
			}
		}
		port.returns("success", success, "reason", reason);
	}
	
	/**
	 * 登录检查补偿邮件
	 * @param humanId
	 * @param fillMailJson
	 */
	@DistrMethod
	public void loginCheck(long humanId, String fillMailJson,int serverId,long timeCreate){
		List<FillMail> results = new ArrayList<>();
		JSONArray joArray = Utils.toJSONArray(fillMailJson);
		if(null == mails || mails.isEmpty()){
			port.returns("mailList", results, "mailJson", joArray.toJSONString());
			return;
		}
		Iterator<FillMail> itrs = mails.values().iterator();
		while(itrs.hasNext()){
			FillMail mail = itrs.next();
			long time = Port.getTime();
			int type = mail.getType();
			boolean ret = true;//是否清除过期并已经领取的邮件记录
			if(1 == type){//分服
				if(serverId != mail.getServerId())
					continue;
			}else if(2 == type){//创觉时间
				time = timeCreate;
				ret = false;//因为创角色时间不会变，所以领取记录不能删除
			}
			
			if(mail.getStartTime() > time){
				//未到发送时间,不发送
				continue;
			}
			if(mail.getEndTime() <= time){
				if(ret && !joArray.isEmpty()){
					joArray.remove(mail.getId());//跳过过期的邮件，且维护玩家的领取记录
				}
				continue;
			}
			
			if(joArray.contains(mail.getId())){
				//跳过已经领取的
				continue;
			}
			
			//领取记录
			joArray.add(mail.getId());
			results.add(mail);
		}
		
		checkMailJson(joArray);
		
		port.returns("mailList", results, "mailJson", joArray.toJSONString());
	}
	
	
	/**
	 * 检查并删除过期的邮件ID
	 * @param joArray
	 */
	public void checkMailJson(JSONArray joArray){
		if(joArray.isEmpty()){
			return;
		}
		int size = joArray.size();
		
		//查找过期的邮件Id
		List<Long> removeIds = new ArrayList<>();
		for(int i=0; i<size; i++){
			long mailId = (long)joArray.get(i);
			if(mails.get(mailId) == null){
				removeIds.add(mailId);
			}
		}
		
		//删除过期的邮件Id
		for(int i=0; i<removeIds.size(); i++){
			joArray.remove(removeIds.get(i));
		}
	}
	
	/**
	 * 定时删除过期的补偿邮件 每2分钟一次
	 */
	@ScheduleMethod("0 0/2 * * * ?")
	public void deleteTimeoutMail() {	
		Iterator<FillMail> itrs = mails.values().iterator();
		while(itrs.hasNext()){
			FillMail mail = itrs.next();
			if(mail.getEndTime() <= Port.getTime()){
				mail.remove();
				itrs.remove();
			}
		}
	}
	
	/**
	 * 每1分钟检查一次可发送的邮件(每发送100个人，等待1s)
	 */
	@ScheduleMethod("0 0/1 * * * ?")
	public void checkAndSend() {
		Iterator<FillMail> itrs = mails.values().iterator();
		while(itrs.hasNext()){
			FillMail fillMail = itrs.next();
			if(Port.getTime() > fillMail.getStartTime() && Port.getTime() < fillMail.getEndTime()){
				if(fillMail.getSysSendTime() == 0){
					HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
					prx.sendFillMail(fillMail);
					fillMail.setSysSendTime(Port.getTime());
				}
			}
		}
	}
}