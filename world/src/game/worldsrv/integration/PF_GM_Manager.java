package game.worldsrv.integration;

import game.msg.Define.EInformType;
import game.msg.Define.EMoneyType;
import game.seam.account.AccountService;
import game.support.ClassScanProcess;
import game.support.DataReloadManager;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.config.ConfItem;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.ItemBody;
import game.worldsrv.entity.Partner;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.inform.InformManager;
import game.worldsrv.mail.FillMailServiceProxy;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.AssetsTxtFix;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.RecordTransient;
import core.db.DBKey;
import core.dbsrv.DB;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

public class PF_GM_Manager extends ManagerBase {

	public static final String Split = ",";//英文逗号
	private static final String SplitChinese = "，";//中文逗号
    private static final String __break = "__break";
	/**
	 * 一些限制
	 */
	public static final int All_Maill_Gold = 30000;  //群发邮件元宝上限
    public static final int All_Maill_BindGold = 30000;//群发邮件绑定元宝上限
    public static final int Maill_Gold = 100000;  //个人邮件元宝上限
    public static final int Maill_BindGold = 100000;//个人邮件绑定元宝上限
    public static final int CHARGE_Gs = 3240;//gs人民币上限
    
    
    public static final String namesKey = "names";// 要发送给的人名称集合（names:玩家名称或玩家Id）
    public static final String EventKeyKey = "eventKey";//操作id
    public static final String successKey = "success";//返回结果 true,false
	public static final String reasonKey = "reason";//返回结果内容
    
    /** 公告相关 **/
    public static final String sendNoticeKey = "sendNotice";//发送公告接口值
    public static final String addNoitfyKey = "addNoitfy";//添加公告接口值
    public static final String NoticeKey = "1|";//表示公告只播放一次
    public static final String NoticeThreeKey = "3|";//表示公告播放三次
    public static final String NoticeIdKey = "id";//公告id
    public static final String NoticeTitleKey = "title";//公告标题
    public static final String NoticeContentKey = "content";//公告内容
    public static final String NoticeTypeKey = "type";//公告type: 0登陆公告，1聊天区公告，2世界推送公告
    public static final String NoticeTimestampKey = "timestamp";//公告生效时间戳
    public static final String NoticeIntervalTimeKey = "split";//公告循环间隔
    public static final String NoticeCountKey = "count";//公告循环次数
    
    /** 邮件相关 **/
    public static final String sendMailKey = "sendMail";// 发送邮件接口值
    public static final String sendMailItemSnKey = "sn";// 要发送给的物品sn称集合
    public static final String sendMailItemNumKey = "num";// 要发送给的物品数量称集合
    public static final String sendMailTitle = "title";// 邮件的标题
    public static final String sendMailDetail = "detail";// 邮件的内容
    public static final String sendMailStartTime = "startTime";// 邮件开始时间
    public static final String sendMailEndTime = "endTime";//邮件结束时间
    
    public static final String sendFillMaillKey = "sendFillMail";//发送补偿邮件
    
    /** 禁言/封号 **/
    public static final String sealKey = "seal";//禁言/封号接口值
    public static final String unSealKey = "unSeal";//解除禁言/封号接口值
    public static final String sealEndDateStr = "endDateStr"; //结束时间
    
    
    /** vip等级调整 **/
    public static final String setVIPKey = "setVIP";//vip等级设置接口值
    public static final String vipKey ="vip";//vip等级集合
    
    /** 添加白名单 **/
    public static final String addWhileListKey = "addWhileList";//添加白名单
    public static final String setWhileListKey = "setWhileListStatus";//开启或关闭白名单
    public static final String whileListStatusKey = "status";//白名单状态，0：开启，1：关闭
    public static final String getWhileListKey = "getWhileList";//获取白名单
    public static final String deleteWhileListKey = "deleteWhileList";//获取白名单
    public static final String test = "test";//获取白名单
    
    /** 设置gm **/
    public static final String gmOpen = "gmOpen";// 开启gm权限
    public static final String gmStop = "gmStop";// 关闭gm权限
    
	public static PF_GM_Manager inst() {
		return inst(PF_GM_Manager.class);
	}
	
	/**
	 * web平台：执行GM命令
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { "inputCommand" })
	public void inputCommand(Param param) {
		JSONObject jo = param.get();
		String command = jo.getString("command");
		Log.game.info("PF_GM_Manager.inputCommand jo={}", jo);

        String r = handleInputCommand(command);
        if (r != __break) {
            Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey, true, reasonKey, r);
        }
	}

	private String handleInputCommand(String command) {
        String[] order = command.split(" ");
        if (order.length <= 0) {
            return "empty command";
        }
        switch (order[0]) {
            case "reloadres":
                DataReloadManager.inst().reloadConf();
                break;
            case "reloadclass":
                if (order.length > 1) {
                    ClassScanProcess.getInstance().reloadClass(order[1]);
                } else {
                    ClassScanProcess.getInstance().checkToReload();
                }
                break;
            case gmOpen:
                gmSet(order[1], true);
                break;
            case gmStop:
                gmSet(order[1], false);
                break;
            case "queryOnlineNum":
                PF_MONITOR_Manager.inst().queryOnlineNum(new JSONObject());
                return __break;
            case "setMaxOnline":
                setMaxOnline(Integer.valueOf(order[1]));
                return __break;
            default:
                return "unknow command";
        }
        return "ok";
    }
	
	/** -------------sendMail start-------------- */
	/**
	 * 给玩家发送邮件 注意:一个发送完成后，发送下一个 names:玩家名称或玩家Id
	 * 
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { sendMailKey })
	public void sendMail(Param param) {
		JSONObject jo = param.get();
		// 要发送给的人名称集合
		String names = jo.getString(namesKey);
		String itemSn = jo.getString(sendMailItemSnKey);
		String itemNum = jo.getString(sendMailItemNumKey);
		
		if(itemSn.indexOf(SplitChinese) >= 0 || itemNum.indexOf(SplitChinese) >= 0){
			//分隔符不能是中文逗号
			Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "分隔符不能是中文逗号");
			return;
		}
		String [] ids = itemSn.split(Split);
		String [] ns = itemNum.split(Split);
		
		if(!StringUtils.isEmpty(itemSn) || !StringUtils.isEmpty(itemNum)){
			if(ids.length != ns.length){
				//类型和数量不匹配
				Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "资源类型和数量个数不匹配");
				return;
			}
			if(ids.length > 0 || ns.length > 0){
				if(!Utils.isDigits(ids) || !Utils.isDigits(ns)){
					//附件格式不正确
					Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "附件格式不正确");
					return;
				}
			}
		}
		sendMail(names,jo);
	}
	
	/**
	 * 给指定人员发送邮件
	 * @param names
	 * @param jo
	 */
	public void sendMail(String names, JSONObject jo){
		String[] nameArray = names.split(Split);
		List<String> nameList = new LinkedList<>();
		try {
			for (String name : nameArray) {
				if (name.isEmpty()) {
					continue;
				}
				name = name.trim();
				if (name.length() >= 18) {
					// 如果length>=18,检查id的合法性
					Utils.longValue(name);
				}
				nameList.add(name);
			}
		} catch (Exception e) {
			Port.getCurrent().returns(successKey, false, reasonKey, "格式不正确");
			return;
		}

		jo.remove(namesKey);

		// 添加的物品list
		int itemSn[] = null;
		int itemNum[] = null;
		String sn = jo.getString(sendMailItemSnKey);
		String num = jo.getString(sendMailItemNumKey);
		if (sn != null && !"".equals(sn) && !"null".equals(sn)) {
			String[] sns = sn.split(Split);
			String[] nums = num.split(Split);

			itemSn = new int[sns.length];
			itemNum = new int[nums.length];

			for (int i = 0; i < sns.length; i++) {
				itemSn[i] = Utils.intValue(sns[i]);
				itemNum[i] = Utils.intValue(nums[i]);
				if(itemSn[i] == EMoneyType.gold_VALUE){
					if(itemNum[i] > Maill_BindGold){
						Port.getCurrent().returns(successKey, false, reasonKey, "绑定元宝超出上限");
						return ;
					}
				}else if(itemSn[i] == EMoneyType.gold_VALUE){
					if(itemNum[i] > Maill_Gold){
						Port.getCurrent().returns(successKey, false, reasonKey, "元宝超出上限");
						return ;
					}
				}
			}
		}

		jo.put("pid", Port.getCurrent().createReturnAsync());
		// 查询的列（id）
		List<String> coList = new ArrayList<>();
		coList.add(Human.K.id);

		send(jo, nameList, coList, itemSn, itemNum, "");
	}

	public void send(JSONObject jo, List<String> nameList, List<String> coList,
			int[] itemSn, int[] itemNum, String reason) {
		if (nameList.isEmpty()) {
			boolean flag = false;
			if ("".equals(reason)) {
				reason = "操作成功";
				flag = true;
			}
			// 循环出口
			Port.getCurrent().returnsAsync(jo.getLongValue("pid"), successKey,
					flag, reasonKey, reason);
			return;
		}
		String name = nameList.remove(0);
		// 如果是ID
		String whereSql = Utils.createStr(" where name = '{}'", name);
		if (name.length() >= 18) {
			whereSql = Utils.createStr(" where id = '{}'", name);
		}
		DB db = DB.newInstance(Human.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, coList);
		db.listenResult(this::_result_sendMail, "jo", jo, "nameList",
				nameList, "coList", coList, "itemSn", itemSn, "itemNum",
				itemNum, reasonKey, reason, "name", name);
	}

	private void _result_sendMail(Param results, Param context) {
		List<RecordTransient> reclist = results.get();
		JSONObject jo = Utils.getParamValue(context, "jo", null);
		List<String> nameList = Utils.getParamValue(context, "nameList", null);
		List<String> coList = Utils.getParamValue(context, "coList", null);
		if(null == reclist || null == nameList || null == coList){
			Log.game.error("===PF_GM_Manager._result_sendMail jo={}, nameList={}, coList={}", jo, nameList, coList);
			return;
		}
		
		int len = 0;
		// 物品
		int[] itemSn = Utils.getParamValue(context, "itemSn", null);
		int[] itemNum = Utils.getParamValue(context, "itemNum", null);
		
//		if(null == itemSn || null == itemNum){
//			Log.game.error("PF_GM_Manager._result_sendMail itemSn={}, itemNum={}", itemSn, itemNum);
//			return;
//		}
		
		if (itemSn != null) {
			len += itemSn.length;
		}

		int[] targetItem = new int[len];
		int[] targetNum = new int[len];

		int i = 0;
		if (itemSn != null) {
			int index = 0;
			for (; i < len; i++) {
				targetItem[i] = itemSn[index];
				targetNum[i] = itemNum[index];
				index++;
			}
		}

		String reason = context.getString(reasonKey);
		if (reclist.isEmpty()) {
			reason += "玩家 " + Utils.getParamValue(context, "name", "") + " 不存在<br/>";
		} else {
			long receiverId = reclist.get(0).get("id");
			List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
			itemProduce.addAll(ProduceManager.inst().produceItem(targetItem, targetNum));
			// 发送一个玩家的邮件
			MailManager.inst().sendSysMail(receiverId, jo.getString("title"), jo.getString("detail"), itemProduce);
		}
		// 发送其他玩家的邮件
		send(jo, nameList, coList, itemSn, itemNum, reason);// 继续执行
	}

	/** -------------sendMail end-------------- */
	
	/**
	 * 发送补偿邮件
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { sendFillMaillKey })
	public void sendFillMail(Param param) {
		JSONObject jo = param.get();
		String itemSn = jo.getString(sendMailItemSnKey);
		String itemNum = jo.getString(sendMailItemNumKey);
		if(itemSn.indexOf(SplitChinese) >= 0 || itemNum.indexOf(SplitChinese) >= 0){
			//分隔符不能是中文逗号
			Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "分隔符不能是中文逗号");
			return;
		}
		if(!StringUtils.isEmpty(itemSn) && !StringUtils.isEmpty(itemNum)){
			String [] ids = itemSn.split(Split);
			String [] ns = itemNum.split(Split);
			
			if(ids.length != ns.length){
				//类型和数量不匹配
				Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "资源类型和数量个数不匹配");
				return;
			}
			
			if(ids.length > 0 || ns.length > 0){
				if(!Utils.isDigits(ids) || !Utils.isDigits(ns)){
					//附件格式不正确
					Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "附件格式不正确");
					return;
				}
			}
			if(!isMoneyLimit(ids, ns)){
				//元宝或绑定元宝超出上限
				Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "元宝或绑定元宝超出上限");
				return;
			}
		}
		String title = jo.getString(sendMailTitle);
		String content = jo.getString(sendMailDetail);
		Long startTime = jo.getLongValue(sendMailStartTime);
		Long endTime = jo.getLongValue(sendMailEndTime);
		String eventKey = jo.getString(EventKeyKey);
		//全服补发邮件
		FillMailServiceProxy prx = FillMailServiceProxy.newInstance();
		prx.sendMail(itemSn,itemNum,title,content,startTime,endTime,eventKey);
		
		Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,true, reasonKey, "发送成功！");
	}
	
	
	
	/**
	 * 判断群发邮件货币上限
	 * @return
	 */
	public boolean isMoneyLimit(String [] ids, String [] ns){
		int[] itemIds = new int[]{};
		int[] nums = new int[]{};
		
		if(null == ids || null == ns || ids.length != ns.length){
			//类型和数量不匹配
			return false;
		}
		int len = ids.length;
		itemIds = new int[len];
		nums = new int[len];
		for(int i=0; i<len; i++){
			itemIds[i] = Utils.intValue(ids[i]);
			nums[i] = Utils.intValue(ns[i]);
			if(itemIds[i] == EMoneyType.gold_VALUE){
				if(nums[i] > All_Maill_BindGold){
					return false;
				}
			}else if(itemIds[i] == EMoneyType.gold_VALUE){
				if(nums[i] > All_Maill_Gold){
					return false;
				}
			}
		}
		return true;
	}
	
	
	
	/**
	 * 删除补偿邮件
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { "unSendFillMail" })
	public void deleteFillMail(Param param) {
		JSONObject jo = param.get();
		String eventKey = jo.getString("mailKey");
		long pid = Port.getCurrent().createReturnAsync();
		FillMailServiceProxy prx = FillMailServiceProxy.newInstance();
		prx.deleteMail(eventKey);
		prx.listenResult(this::_result_deleteFillMail, "pid", pid);
	}
	
	public void _result_deleteFillMail(Param results, Param context){
		boolean result = Utils.getParamValue(results, successKey, false);
		String reason = Utils.getParamValue(results, reasonKey, "");
		long pid = Utils.getParamValue(context, "pid", 0L);
		Port.getCurrent().returnsAsync(pid, successKey, result, reasonKey, reason);
	}
	
	
	
	/**
	 * 设置最大在线玩家(针对登录排队)
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { "setMaxOnline" })
	public void setMaxOnline(Param param) {
		JSONObject jo = param.get();
        int maxOnlineNum = jo.getIntValue("maxOnlineNum");
		setMaxOnline(maxOnlineNum);
	}
	
	/**
	 * 设置最大在线人数(针对登录排队)
	 */
	public void setMaxOnline(int maxOnlineNum) {
		AccountService serv = Port.getCurrent().getService(Distr.SERV_GATE);
		if(serv != null){
			serv.setLoginMaxOnline(maxOnlineNum);
			Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,true, reasonKey, "发送成功！");
		}else{
			Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "发送失败！");
		}
		
	}
	
	
	

	/** -------------sendGiftCode start-------------- */
	/**
	 * 给玩家发送邮件 注意:一个发送完成后，发送下一个 names:玩家名称或玩家Id
	 * 
	 * @param param
	 *//*
	@Listener(value = EventKey.GM, subStr = { "sendGiftCode" })
	public void sendGiftCode(Param param) {
		JSONObject jo = param.get();
		// 要发送给的人名称集合
		String names = jo.getString("names");
		String[] nameArray = names.split(SPLIT);
		List<String> nameList = new LinkedList<>();
		try {
			for (String name : nameArray) {
				if (name.isEmpty()) {
					continue;
				}
				if (name.length() >= 18) {
					// 如果length>=18,检查id的合法性
					Utils.longValue(name);
				}
				nameList.add(name);
			}
		} catch (Exception e) {
			Port.getCurrent().returns(successKey, false, reasonKey, "格式不正确");
			return;
		}

		jo.remove("names");

		// 添加的物品list
		String code = jo.getString("code");
		ConfGiftCode confCode = ConfGiftCode.get(code);
		if (confCode == null) {
			Port.getCurrent().returns(successKey, false, reasonKey, "礼包码不存在");
			return;
		}

		jo.put("pid", Port.getCurrent().createReturnAsync());
		// 查询的列（id）
		List<String> coList = new ArrayList<>();
		coList.add(Human.K.id);

		sendGiftCode(jo, nameList, coList, code, "");
	}

	public void sendGiftCode(JSONObject jo, List<String> nameList,
			List<String> coList, String code, String reason) {
		if (nameList.isEmpty()) {
			boolean flag = false;
			if ("".equals(reason)) {
				reason = "操作成功";
				flag = true;
			}
			// 循环出口
			Port.getCurrent().returnsAsync(jo.getLongValue("pid"), successKey,
					flag, reasonKey, reason);
			return;
		}
		String name = nameList.remove(0);
		// 如果是ID
		String whereSql = Utils.createStr(" where name = '{}'", name);
		if (name.length() >= 18) {
			whereSql = Utils.createStr(" where id = '{}'", name);
		}
		DB db = DB.newInstance(Human.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, coList);
		db.listenResult(this::_result_sendGiftCode, "jo", jo, "nameList",
				nameList, "coList", coList, "code", code, reasonKey, reason,
				"name", name);
	}

	private void _result_sendGiftCode(Param results, Param context) {
		List<RecordTransient> list = results.get();
		JSONObject jo = context.get("jo");
		List<String> nameList = context.get("nameList");
		List<String> coList = context.get("coList");

		String code = context.get("code");

		String reason = context.getString(reasonKey);
		long receiverId;
		if (list.size() > 0) {
			receiverId = list.get(0).get("id");
		} else {
			reason += "玩家 " + context.getString("name") + " 不存在<br/>";
			sendGiftCode(jo, nameList, coList, code, reason);
			return;
		}

		ConfGiftCode confCode = ConfGiftCode.get(code);
		if (confCode != null) {

			// 发送一个玩家的邮件
			MailManager.inst().sendMail(receiverId, MailManager.SYS_SENDER,
					confCode.title, confCode.content, confCode.itemId,
					confCode.itemNum, true);
		}

		// 发送其他玩家的邮件
		sendGiftCode(jo, nameList, coList, code, reason);
	}*/

	/** -------------sendGiftCode end-------------- */

	
	/**-------------------公告-----------------**/
	/**
	 * 走马灯（发公告）
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { sendNoticeKey })
	public void sendNotice(Param param) {
		JSONObject jo = param.get();
		String notice = jo.getString(NoticeContentKey);
		int count = jo.getIntValue(NoticeCountKey);//公告循环次数
		int type = jo.getIntValue(NoticeTypeKey);//公告type
		// TODO 后续补充间隔时间相关逻辑
		int intervalTime = jo.getIntValue(NoticeIntervalTimeKey);//公告循环间隔
		long pid = Port.getCurrent().createReturnAsync();

		if ("".equals(notice) || "null".equals(notice)) {
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "通告不能为空。");
			return;
		}
		// 发送公告
		boolean ret = InformManager.inst().sendNotify(EInformType.valueOf(type), notice, count, intervalTime);
//		boolean ret = InformManager.inst().sendNotify(EInformType.valueOf(type), notice, count);
		if(ret){
			Port.getCurrent().returnsAsync(pid, successKey, true, reasonKey, "发送通告成功。");
		} else {
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "发送通告失败。");
		}
	}
	
	/**
	 * 添加公告（目前还是及时公告）
	 * @param param
	 * @throws InterruptedException 
	 */
	/*
	@Listener(value = EventKey.GM, subStr = { addNoitfyKey })
	public void addNoitfy(Param param){
		JSONObject jo = param.get();
		String id = jo.getString(NoticeIdKey);//公告id
	    String title = jo.getString(NoticeTitleKey);//公告标题
	    String content = jo.getString(NoticeContentKey);//公告内容
	    int type = jo.getIntValue(NoticeTypeKey);//公告type
	    long timestamp = jo.getLongValue(NoticeTimestampKey);//公告生效时间戳
	    long intervalTime = jo.getLongValue(NoticeIntervalTimeKey);//公告循环间隔
	    long timeEnd = Port.getTime();
	    int count = jo.getIntValue(NoticeCountKey);//公告循环次数
	    String eventKey = jo.getString(EventKeyKey);//操作id
	    
	    long pid = Port.getCurrent().createReturnAsync();
	    
	    content = title +content;//标题+内容组成新的内容 
	    
		if ("".equals(content) || "null".equals(content)) {
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "通告不能为空。");
			return;
		}
		int channel = Utils.intValue(type);
		NoticeServiceProxy pxy = NoticeServiceProxy.newInstance();
		pxy.addNotice(title, content, channel, timestamp, timeEnd, intervalTime, eventKey);
		
		Log.notice.info("添加公告成功。pid={},id={},title={},content={},type={},timesTamp={},intervalTime={},"
				+ "count={},eventKey={}" , pid, id, title, content, type, timestamp, intervalTime, count, eventKey);
		
		Port.getCurrent().returnsAsync(pid, successKey, true, reasonKey, "添加通告成功。");
	}*/
	
	
	/**
	 * 解除禁言，封号
	 * 
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { unSealKey })
	public void unSeal(Param param) {
		JSONObject jo = param.get();
		// 要发送给的人名称集合
		String names = jo.getString(namesKey);
		
		String[] nameArray = names.split(Split);
		List<String> nameList = new LinkedList<>();
		try {
			for (String name : nameArray) {
				if (name.isEmpty()) {
					continue;
				}
				if (name.length() >= 18) {
					// 如果length>=18,检查id的合法性
					Utils.longValue(name);
				}
				nameList.add(name);
			}
		} catch (Exception e) {
			Port.getCurrent().returns(successKey, false, reasonKey, "格式不正确");
			return;
		}

		jo.remove(namesKey);
		jo.put(sealEndDateStr, 0);
		jo.put("pid", Port.getCurrent().createReturnAsync());

		List<String> coList = new ArrayList<>();
		coList.add(Human.K.id);

		doSeal(jo, nameList, coList, "");
	}
	/**
	 * 禁言，封号
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { sealKey })
	public void seal(Param param) {
		JSONObject jo = param.get();
		// 要发送给的人名称集合
		String names = jo.getString(namesKey);
		
		String[] nameArray = names.split(Split);
		List<String> nameList = new LinkedList<>();
		try {
			for (String name : nameArray) {
				if (name.isEmpty()) {
					continue;
				}
				if (name.length() >= 18) {
					// 如果length>=18,检查id的合法性
					Utils.longValue(name);
				}
				nameList.add(name);
			}
		} catch (Exception e) {
			Port.getCurrent().returns(successKey, false, reasonKey, "格式不正确");
			return;
		}

		jo.remove(namesKey);

		jo.put("pid", Port.getCurrent().createReturnAsync());

		List<String> coList = new ArrayList<>();
		coList.add(Human.K.id);

		doSeal(jo, nameList, coList, "");
	}

	/**
	 * 禁言
	 * 
	 * @param jo
	 * @param nameList
	 * @param reason
	 */
	public void doSeal(JSONObject jo, List<String> nameList, List<String> coList, String reason) {
		if (nameList.isEmpty()) {
			boolean flag = false;
			if ("".equals(reason)) {
				reason = "操作成功";
				flag = true;
			}
			// 循环出口
			Port.getCurrent().returnsAsync(jo.getLongValue("pid"), successKey,
					flag, reasonKey, reason);
			return;
		}

		String name = nameList.remove(0);
		// 如果是ID
		String whereSql = Utils.createStr(" where name = '{}'", name);
		if (name.length() >= 18) {
			whereSql = Utils.createStr(" where id = '{}'", name);
		}

		DB db = DB.newInstance(Human.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, coList);
		db.listenResult(this::_result_seal, "jo", jo, "nameList", nameList,
				"coList", coList, reasonKey, reason, "name", name);
	}
	private void _result_seal(Param results, Param context) {
		List<RecordTransient> reclist = results.get();
		JSONObject jo = Utils.getParamValue(context,"jo", null);
		List<String> nameList = Utils.getParamValue(context,"nameList",null);
		List<String> coList = Utils.getParamValue(context,"coList", null);
		String reason = context.getString(reasonKey);
		if (null == reclist || null == jo || null == nameList || null == coList) {
			Log.game.error("PF_GM_Manager._result_seal jo={},nameList={},coList={}", jo, nameList, coList);
			return;
		}

		if (reclist.isEmpty()) {// 查无记录
			reason += "玩家 " + Utils.getParamValue(context, "name", "") + " 不存在<br/>";
		} else {// 查到记录
			long receiverId = reclist.get(0).get("id");
			int type = jo.getIntValue("type");
			long endTime = jo.getLong(sealEndDateStr);
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.sealAccount(receiverId, type, endTime);
		}
		// 继续执行
		doSeal(jo, nameList, coList, reason);
	}
	
	/**
	 * 查看玩家信息
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { "humanInfo" })
	public void findHumanInfo(Param param) {
		JSONObject jo = param.get();
		String name = jo.getString("name");
		String whereSql = Utils.createStr(" where name = '{}'", name);
		if(name.length() >= 18){
			try{
				whereSql = Utils.createStr(" where id = '{}'", Utils.longValue(name));
			}catch(Exception e){
				Port.getCurrent().returns(successKey,false, reasonKey, "格式不正确");
				return;
			}
		}
		List<String> coList= new ArrayList<>();
		coList.add(Human.K.Name);
		coList.add(Human.K.AccountId);
		coList.add(Human.K.id);
		coList.add(Human.K.Level);
		coList.add(Human.K.VipLevel);
		coList.add(Human.K.ChargeGold);
		coList.add(Human.K.Coin);
		coList.add(Human.K.Gold);
		coList.add(Human.K.Act);
		coList.add(Human.K.TimeLogin);
		coList.add(Human.K.TimeLogout);
		DB db = DB.newInstance(Human.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, coList);
		db.listenResult(this::_result_findHumanInfo, "pid", Port.getCurrent().createReturnAsync());
		
	}
	private void _result_findHumanInfo(Param results, Param context){
		long pid = context.get("pid");
		List<RecordTransient> list = results.get();
		if(list.isEmpty()){
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "没有该玩家");
			return;
		}
		Map<String, Object> humanMap = new HashMap<>();
		RecordTransient rt = list.get(0);
		humanMap.put(Human.K.Name, rt.get(Human.K.Name));
		humanMap.put(Human.K.AccountId, rt.get(Human.K.AccountId));
		humanMap.put(Human.K.id, rt.get(Human.K.id));
		humanMap.put(Human.K.Level, rt.get(Human.K.Level));
		humanMap.put(Human.K.VipLevel, rt.get(Human.K.VipLevel));
		humanMap.put(Human.K.ChargeGold, rt.get(Human.K.ChargeGold));
		humanMap.put(Human.K.Coin, rt.get(Human.K.Coin));
		humanMap.put(Human.K.Gold, rt.get(Human.K.Gold));
		humanMap.put(Human.K.Act, rt.get(Human.K.Act));
		humanMap.put(Human.K.TimeLogin, rt.get(Human.K.TimeLogin));
		humanMap.put(Human.K.TimeLogout, rt.get(Human.K.TimeLogout));
		
		String whereSql = Utils.createStr(" where {} = {}", Partner.K.HumanId, rt.get(Human.K.id));
		List<String> coList= new ArrayList<>();
		coList.add(Partner.K.Name);
		coList.add(Partner.K.Star);
		DB db = DB.newInstance(Partner.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, coList);
		db.listenResult(this::_result_findHumanInfo1, "pid", pid, "map", humanMap, "id", rt.get(Human.K.id));
	}
	private void _result_findHumanInfo1(Param results, Param context){
		long pid = Utils.getParamValue(context, "pid", 0L);
		long id = Utils.getParamValue(context, "id", 0L);
		Map<String, Object> map =  Utils.getParamValue(context, "map", null);
		List<RecordTransient> list = results.get();
		if(id <= 0 || null == map){
			Log.game.error("PF_GM_Manager._result_findHumanInfo1 id={}, map={}", id, map);
			return;
		}
		
		List<Map<String, Object>> generalList = new ArrayList<>();
		for(RecordTransient rt : list){
			Map<String, Object> geMap = new HashMap<String, Object>();
			geMap.put(Partner.K.Name, rt.get(Partner.K.Name));
			geMap.put(Partner.K.Star, rt.get(Partner.K.Star));
			generalList.add(geMap);
		}
		
		map.put("generalList", generalList);
		
		String whereSql = Utils.createStr(" where {} = {}", ItemBody.K.OwnerId, id);//身上的物品
		List<String> coList= new ArrayList<>();
		coList.add(ItemBody.K.id);
		coList.add(ItemBody.K.Sn);
		coList.add(ItemBody.K.Num);
		DB db = DB.newInstance(ItemBody.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, coList);
		db.listenResult(this::_result_findHumanInfo2, "pid", pid, "map", map, "id", id);
	}
	private void _result_findHumanInfo2(Param results, Param context){
		Map<String, Object> map = Utils.getParamValue(context, "map", null);
		long pid = Utils.getParamValue(context, "pid", 0L);
		long id = Utils.getParamValue(context, "id", 0L);
		
		if(null == map || id <= 0){
			Log.game.error("PF_GM_Manager._result_findHumanInfo2 map={}, id={}", map, id);
			return;
		}
		
		List<RecordTransient> list = results.get();
		List<Map<String, Object>> itemList = new ArrayList<>();

		for(RecordTransient rt : list){
			Map<String, Object> itemMap = new HashMap<>();
			itemMap.put(ItemBody.K.id, rt.get(ItemBody.K.id));
			itemMap.put(ItemBody.K.Sn, rt.get(ItemBody.K.Sn));
			itemMap.put(ItemBody.K.Num, rt.get(ItemBody.K.Num));
			ConfItem conf = ConfItem.get(rt.get(ItemBody.K.Sn));
			if(conf == null){
				continue;
			}
			itemMap.put("name", conf.name);
			itemList.add(itemMap);
		}
		map.put("itemList", itemList);
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(id);
		prx.listenResult(this::_result_findHumanInfo3, "pid", pid, "map", map); 
	}
	
	private void _result_findHumanInfo3(Param results, Param context){
		HumanGlobalInfo info = results.get();
		Map<String, Object> map = Utils.getParamValue(context, "map", null);
		long pid = Utils.getParamValue(context, "pid", 0L);
		if(null == map){
			Log.game.error("PF_GM_Manager._result_findHumanInfo3 map={}", map);
			return;
		}
		if(info == null){
			map.put("status", "离线");
		}else{
			map.put("status", "在线");
		}
		Port.getCurrent().returnsAsync(pid, successKey, true, "param", Utils.toJSONString(map));
//		Log.temp.info("{}", Utils.toJSONString(map));
	}
	
	/**
	 * 设置vip等级
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { setVIPKey })
	public void humanVipLvUp(Param param) {
		JSONObject jo = param.get();
		String names = jo.getString(namesKey);
		String vipLvs = jo.getString(vipKey);
		if(names.indexOf(SplitChinese) >= 0 || vipLvs.indexOf(SplitChinese) >= 0){
			//分隔符不能是中文逗号
			Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "分隔符不能是中文逗号");
			return;
		}
		String [] nameArray = names.split(Split);
		String [] vipLvArray = vipLvs.split(Split);
		
		if(!StringUtils.isEmpty(names) && !StringUtils.isEmpty(vipLvs)){
			if(nameArray.length != vipLvArray.length){
				//类型和数量不匹配
				Port.getCurrent().returnsAsync(Port.getCurrent().createReturnAsync(), successKey,false, reasonKey, "名字个数和vip个数不匹配");
			}
			humanVipLvUp(nameArray,vipLvArray, jo);
		}
	}
	
	public void humanVipLvUp(String[] nameArray, String[] vipLvArray, JSONObject jo){
		List<String> nameList = new LinkedList<>();
		List<Integer> vipLvList = new LinkedList<>();
		try {
			for (String name : nameArray) {
				if (name.isEmpty()) {
					continue;
				}
				if (name.length() >= 18) {
					// 如果length>=18,检查id的合法性
					Utils.longValue(name);
				}
				nameList.add(name);
			}
			for (int i = 0; i < vipLvArray.length; i++) {
				int vipLv = Utils.intValue(vipLvArray[i]);
				vipLvList.add(vipLv);
			}
			if(vipLvList.size() != nameList.size()){//
				Port.getCurrent().returns(successKey, false, reasonKey, "格式不正确");
				return;
			}
		} catch (Exception e) {
			Port.getCurrent().returns(successKey, false, reasonKey, "格式不正确");
			return;
		}

		jo.remove(namesKey);
		jo.remove(vipKey);

		jo.put("pid", Port.getCurrent().createReturnAsync());
		// 查询的列（id）
		List<String> coList = new ArrayList<>();
		coList.add(Human.K.id);

		update(jo, nameList,vipLvList, coList, "");
	}
	
	private void update(JSONObject jo, List<String> nameList, List<Integer> vipLvList, List<String> coList, String reason){
		if (nameList.isEmpty()) {
			boolean flag = false;
			if ("".equals(reason)) {
				reason = "操作成功";
				flag = true;
			}
			// 循环出口
			Port.getCurrent().returnsAsync(jo.getLongValue("pid"), successKey, flag, reasonKey, reason);
			return;
		}
		String name = nameList.remove(0);
		int viplv = vipLvList.remove(0);
		
		// 如果是ID
		String whereSql = Utils.createStr(" where `{}` =?", Human.K.Name);
		if (name.length() >= 18) {
			whereSql = Utils.createStr(" where `{}` =?", Human.K.id);
		}
		DB db = DB.newInstance(Human.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, coList, name);
		db.listenResult(this::_result_humanVipLvUp, "jo", jo, "nameList", nameList,"vipLvList",vipLvList
				, "coList", coList, reasonKey, reason, "name", name, "viplv", viplv);
	}
	
	private void _result_humanVipLvUp(Param results, Param context) {
		List<RecordTransient> list = results.get();
		JSONObject jo = Utils.getParamValue(context, "jo", null);
		List<String> nameList = Utils.getParamValue(context, "nameList", null);
		List<Integer> vipLvList =  Utils.getParamValue(context, "vipLvList", null);
		List<String> coList =  Utils.getParamValue(context, "coList", null);
		String reason = context.getString(reasonKey);
		int viplv = Utils.getParamValue(context, "viplv", -1);
		if(null == jo || null == nameList || null == vipLvList || null == coList || viplv < 0){
			Log.game.error("PF_GM_Manager._result_humanVipLvUp jo={}, nameList={}, vipLvList={}, coList={}, viplv={}",
					jo, nameList, vipLvList, coList, viplv);
			return;
		}
		
		if (list.isEmpty()) {
			reason += "玩家 " + context.getString("name") + " 不存在<br/>";
		}else{
			long receiverId = list.get(0).get("id");
			setHumanVipLv(receiverId, viplv);//设置玩家的vip等级
		}
		update(jo, nameList, vipLvList, coList, reason);
	}
	
	public void setHumanVipLv(long humanId, int viplv){
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_remindHumanTo, "humanId", humanId, "viplv", viplv);
	}
	private void _result_remindHumanTo(Param results, Param context) {
		long humanId = Utils.getParamValue(context, "humanId", -1L);
		int viplv = Utils.getParamValue(context, "viplv", -1);
		if (humanId <= 0 || viplv < 0) {
			Log.game.error("===_result_remindHumanTo humanId={}, vip={}", humanId, viplv);
			return;
		}
		HumanGlobalInfo humanInfo = results.get();

		// 若玩家不在线，则直接修改数据库
		if (null == humanInfo) {
			String whereSql = Utils.createStr(" update `{}` set `{}`=? where id =?", Human.tableName, Human.K.VipLevel);
			DB db = DB.newInstance(Human.tableName); 
			db.sql(false, false, whereSql, viplv, humanId);
		} else {
			//在线玩家设置vip等级并通知玩家改变
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(humanInfo.nodeId, humanInfo.portId,humanInfo.id);
			prx.setVipLv(viplv);
		}
	}
	/** --------设置白名单-------- **/
	
	@Listener(value = EventKey.GM, subStr = { addWhileListKey })
	public void addWhileList(Param param){
		JSONObject jo = param.get();
//		String ip = jo.getString("ip");//id
		String account = jo.getString("account");//帐号
		if(account.indexOf(SplitChinese) >= 0 || account.indexOf(SplitChinese) >= 0){
			//分隔符不能是中文逗号
			Port.getCurrent().returns(successKey,false, reasonKey, "分隔符不能是中文逗号");
			return;
		}
		String [] accountArray = account.split(Split);
//		AccountChargeList.accountList.add(ip);
		boolean ret = false;
		for (int i = 0; i < accountArray.length; i++) {
			String value = accountArray[i];
			if(null == value || value.isEmpty()){
				Port.getCurrent().returns(successKey, false, reasonKey, "白名单添加失败");
				continue;
			}
			if(AssetsTxtFix.AccountWhiteListSet.contains(value)){//如果已经存在
				Port.getCurrent().returns(successKey, false, reasonKey, "添加失败,已经存在");
				continue;
			}
			AssetsTxtFix.AccountWhiteListSet.add(value);
			ret = true;
		}
		if(ret){
			AssetsTxtFix.writeToAccountWhiteList();//重新写配置
		}
		
		Port.getCurrent().returns(successKey, ret, reasonKey, "白名单添加");
	}
	
	
	
	/**
	 * 开启或关闭白名单
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { setWhileListKey })
	public void setWhileListKey(Param param){
		JSONObject jo = param.get();
		boolean status = jo.getBoolean(whileListStatusKey);//状态0:开启，1：关闭
		String eventKey = jo.getString(EventKeyKey);//操作id
		ParamManager.whileListStatus = status;
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("status", status);
		Port.getCurrent().returns(successKey, true, reasonKey, "白名单设置成功","param",  Utils.mapToJSON(map));
	}
	/**
	 * 获取白名单
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { getWhileListKey })
	public void getWhileList(Param param) {
		String result = "";
		for (String value : AssetsTxtFix.AccountWhiteListSet) {
			if(null == value || value.isEmpty())
				continue;
			if(result.isEmpty()){
				result += value;
				continue;
			}
			result = result + "," + value;
		}
		Map<String,Object> map = new HashMap<String, Object>();
		map.put(whileListStatusKey, ParamManager.whileListStatus);
		Port.getCurrent().returns(successKey, true, reasonKey, result,"param", Utils.mapToJSON(map));
	}
	
	@Listener(value = EventKey.GM, subStr = { test })
	public void test(Param param) {
		Port.getCurrent().returns(successKey, true);
	}

	/**
	 * 删除白名单
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { deleteWhileListKey })
	public void deleteWhileList(Param param){
		JSONObject jo = param.get();
		String ip = jo.getString("ip");
		String account = jo.getString("account");
		boolean ret = false;
		if(AssetsTxtFix.AccountWhiteListSet.contains(ip)){
			AssetsTxtFix.AccountWhiteListSet.remove(ip);
			ret = true;
		}
		if(AssetsTxtFix.AccountWhiteListSet.contains(account)){
			AssetsTxtFix.AccountWhiteListSet.remove(account);
			ret = true;
		}
		
		if(ret){
			AssetsTxtFix.writeToAccountWhiteList();//重新写配置
		}
		
		Port.getCurrent().returns(successKey, ret, reasonKey, "删除白名单");
		
	}
	
	//=====================gm权限设置开始==================
	
	/**
	 * 开启gm权限
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { gmOpen })
	public void gmOpen(Param param) {
        JSONObject jo = param.get();
        String names = jo.getString(namesKey);
		gmSet(names, true);
	}
	
	/**
	 * 关闭gm权限
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { gmStop })
	public void gmStop(Param param) {
        JSONObject jo = param.get();
        String names = jo.getString(namesKey);
		gmSet(names, false);
	}
	
	/**
	 * 设置gm权限开关 
	 * @param isOpen
	 */
	private void gmSet(String names, boolean isOpen) {
		// 要发送给的人名称集合

		String[] nameArray = names.split(Split);
		List<String> nameList = new LinkedList<>();
		try {
			for (String name : nameArray) {
				if (name.isEmpty()) {
					continue;
				}
				if (name.length() >= 18) {
					// 如果length>=18,检查id的合法性
					Utils.longValue(name);
				}
				nameList.add(name);
			}
		} catch (Exception e) {
			Port.getCurrent().returns(successKey, false, reasonKey, "格式不正确");
			return;
		}

        JSONObject jo = new JSONObject();
		jo.put("pid", Port.getCurrent().createReturnAsync());

		List<String> coList = new ArrayList<>();
		coList.add(Human.K.id);
		doGmSet(jo, nameList, coList, "", isOpen);
	}
	
	/**
	 * gm设置
	 * @param jo
	 * @param nameList
	 * @param reason
	 */
	public void doGmSet(JSONObject jo, List<String> nameList, List<String> coList, String reason, boolean isOpen) {
		if (nameList.isEmpty()) {
			boolean flag = false;
			if ("".equals(reason)) {
				reason = "操作成功";
				flag = true;
			}
			// 循环出口
			Port.getCurrent().returnsAsync(jo.getLongValue("pid"), successKey,
					flag, reasonKey, reason);
			return;
		}

		String name = nameList.remove(0);
		// 如果是ID
		String whereSql = Utils.createStr(" where name = '{}'", name);
		if (name.length() >= 18) {
			whereSql = Utils.createStr(" where id = '{}'", name);
		}

		DB db = DB.newInstance(Human.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, coList);
		db.listenResult(this::_result_doGmSet, "jo", jo, "nameList", nameList,
				"coList", coList, reasonKey, reason, "name", name, "isOpen", isOpen);
	}
	private void _result_doGmSet(Param results, Param context) {
		List<RecordTransient> list = results.get();
		JSONObject jo = Utils.getParamValue(context, "jo", null);
		List<String> nameList = Utils.getParamValue(context, "nameList", null);
		List<String> coList = Utils.getParamValue(context, "coList", null);
		boolean isOpen = Utils.getParamValue(context, "isOpen", false);
		String reason = Utils.getParamValue(context, reasonKey, "");
		if(null == jo || null == nameList || null == coList){
			Log.game.error("PF_GM_Manager._result_doGmSet jo={}, nameList={}, coList={}", jo, nameList, coList);
			return;
		}
		if (list.isEmpty()) {
			reason += "玩家 " + Utils.getParamValue(context,"name","") + " 不存在<br/>";
		} else {
			long receiverId = list.get(0).get("id");
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.setGm(receiverId, isOpen);
		}
		doGmSet(jo, nameList, coList, reason, isOpen);//继续执行
	}
	
	//======================gm权限设置结束======================
	
	
	/**
	 * 开启运营活动
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { "openOperateActivity" })
	public void openOperateActivity(Param param) {
		JSONObject jo = param.get();
		Integer timeType = jo.getInteger("timeType");  //活动时间类型（1。开服时间算 2 .自然时间）
//		long startTime = jo.getLongValue("startTime");  //开始时间
//		long endTime = jo.getLongValue("endTime");    //结束时间
		Integer activityKey = jo.getInteger("activityKey");  //活动类型
		Integer multiple = jo.getInteger("multiple");   //奖励倍数
//		Integer activityId = jo.getInteger("activityId");  //活动id
		if(multiple == null || activityKey == null || timeType == null){
			Port.getCurrent().returns(successKey, false, reasonKey, "活动配置错误");
			return;
		}
//		long pid = Port.getCurrent().createReturnAsync();
//		ActivityGlobalServiceProxy prx = ActivityGlobalServiceProxy.newInstance();
//		prx.newOperateActivity(jo.toString());
//		prx.listenResult(this::_result_openOperateActivity, "pid", pid);
//		Log.temp.info("activityId{}",activityId);
//		Port.getCurrent().returns(successKey, true, "activityId", activityId, reasonKey, "活动配置成功");
	}
	public void _result_openOperateActivity(Param results, Param context){
		boolean result = Utils.getParamValue(results, "result", false);
		String reason = results.get(reasonKey);
		long pid = Utils.getParamValue(context, "pid", 0L);
		Port.getCurrent().returnsAsync(pid, successKey, result, reasonKey, reason);
	}
	/**
	 * 关闭运营活动
	 * @param param
	 */
	@Listener(value = EventKey.GM, subStr = { "closeOperateActivity" })
	public void closeOperateActivity(Param param) {
//		JSONObject jo = param.get();
//		String eventKey = jo.getString("activityKey");  //eventKey
//		long pid = Port.getCurrent().createReturnAsync();
//		ActivityGlobalServiceProxy prx = ActivityGlobalServiceProxy.newInstance();
//		prx.closeOperateActivity(eventKey);
//		prx.listenResult(this::_result_openOperateActivity, "pid", pid);
	}
	public void _result_closeOperateActivity(Param results, Param context){
		boolean result = Utils.getParamValue(results, "result", false);
		String reason = Utils.getParamValue(results, reasonKey, "");
		long pid = Utils.getParamValue(context,"pid", 0L);
		Port.getCurrent().returnsAsync(pid, successKey, result, reasonKey, reason);
	}
//	
//	
//	
///**-------------内部账号设置-------------- */
//	
//	@Listener(value = EventKey.GM, subStr = { "addWelfare" })
//	public void gs(Param param) {
//		JSONObject jo = param.get();
//		long humanId = jo.getLongValue("humanId");
//		int type = jo.getIntValue("type");
//		int value = jo.getIntValue("value");
//		
//		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
//		prx.getInfo(humanId);
//		prx.listenResult(this::_result_gs1,"pid", Port.getCurrent().createReturnAsync(), "type", type, "humanId", humanId, "value", value);
//	}
//	
//	private void _result_gs1(Param results, Param context){
//		long humanId = context.getLong("humanId");
//		int type = context.getInt("type");
//		int value = context.getInt("value");
//		HumanGlobalInfo info = results.get();
//		if(info != null){
//			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
//			prx.gm_Gs(type, value);
//			prx.listenResult(this::_result_gs3, context);
//		}else{
//			List<String> columns = new ArrayList<>();
//			columns.add(Human.K.id);
//			columns.add(Human.K.fuli);
//			columns.add(Human.K.gs);
//			String whereSql = Utils.createStr(" where {}={}", Human.K.id, humanId);
//			DB db = DB.newInstance(Human.tableName);
//			db.findByQuery(false, whereSql, DBKey.COLUMN, columns);
//			db.listenResult(this::_result_gs2, context);
//		}
//	}
//	
//	private void _result_gs2(Param results, Param context){
//		long pid = context.getLong("pid");
//		
//		List<RecordTransient> list = results.get();
//		if(list.isEmpty()){
//			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "玩家不存在");
//			return;
//		}
//		int isGs = 0;
//		int isFuli = 0;
//		for(RecordTransient record : list){
//			isFuli = record.get(Human.K.fuli);
//			isGs = record.get(Human.K.gs);
//			
//		}
//		
//		long humanId = context.getLong("humanId");
//		int type = context.getInt("type");
//		int value = context.getInt("value");
//		if(type == 0){  //福利号
//			if(value == 0 && isFuli == 0){
//				Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "该玩家不是福利号");
//				return ;
//			}
//			if(value == 1 && isFuli == 1){
//				Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "该玩家已经是福利号");
//				return ;
//			}
//		}else if(type == 1){  //gs
//			if(value == 0 && isGs == 0){
//				Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "该玩家不是gs");
//				 return ;
//			}
//			if(value == 1 && isGs == 1){
//				Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "该玩家已经是gs");
//				 return ;
//			}
//		}else{
//			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "配置错误");
//			return ;
//		}
//		DB db = DB.newInstance(Human.tableName);
//		if(type == 0){
//			db.sql(false, true, Utils.createStr("update {} set {}={} where id={}", Human.tableName, Human.K.fuli, value, humanId));
//		}else{
//			if(value == 1){
//				db.sql(false, true, Utils.createStr("update {} set {}={} where id={}", Human.tableName, Human.K.gs, value, humanId));
//			}else{
//				db.sql(false, true, Utils.createStr("update {} set {}={},{}={} where id={}", Human.tableName, Human.K.gs, value,Human.K.gsRmb, 0, humanId));
//			}
//		}
////		Map<String, Integer> paramMap = new HashMap<String, Integer>();
////		paramMap.put("type", type);
////		paramMap.put("value", value);
////		Pocket.add(humanId, PocketLineKey.GM_GS, Utils.toJSONString(paramMap));
//		Port.getCurrent().returnsAsync(pid, successKey, true);
//	}
//	private void _result_gs3(Param results, Param context){
//		long pid = context.getLong("pid");
//		boolean result = results.get(successKey);
//		String reason = results.get(reasonKey);
//		if(result){
//			Port.getCurrent().returnsAsync(pid, successKey, true);
//		}else{
//			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, reason);
//		}
//	}
//	
//	/**
//	 * gs号设置代办
//	 * @param param
//	 */
//	@Listener(value = EventKey.POCKET_LINE_HANDLE_ONE, subStr = PocketLineEventSubKey.HUMAN_POCKET_LINE_HANDLE_GM_GS)
//	public void pocketLine_gs(Param param){
//		HumanObject humanObj = param.get("humanObj");
//		PocketLine p = param.get("pocketLine");
//		// 待办参数
//		@SuppressWarnings("unchecked")
//		Map<String, Integer> resMap = Utils.toJSONObject(p.getParam(),Map.class);
//		int type = resMap.get("type");
//		int value = resMap.get("value");
//		if(type == 0){
//			humanObj.getHuman().setFuli(value == 1?true:false);
//			return;
//		}else if(type == 1){
//			if(value == 0){
//				humanObj.getHuman().setGs(false);
//				humanObj.getHuman().setGsRmb(0);
//			}else{
//				humanObj.getHuman().setGs(true);
//			}
//		}
//	}
	/**-------------补发充值-------------- *//*
	@Listener(value = EventKey.GM, subStr = { "fixPayment" })
	public void charge(Param param) {
		JSONObject jo = param.get();
		int sn = jo.getIntValue("sn");
		ConfPayCharge conf = ConfPayCharge.get(sn);
		ConfPayMonthCard confs = ConfPayMonthCard.get(sn);
		if(Config.GAME_PLATFORM_NAME.equals("tw")){
		}else if(conf == null && confs == null){
			Port.getCurrent().returns(successKey, false, reasonKey, "没有该SN");
			return;
		}
		//要发送给的人list
		String names = jo.getString("names");
		String[] nameArray = names.split(SPLIT);
		List<String> nameList = new LinkedList<>();
		try{
			for(String name : nameArray){
				if(name.isEmpty()){
					continue;
				}
				if(name.length() >= 18){
					Utils.longValue(name);
				}
				nameList.add(name);
			}
		}catch(Exception e){
			Port.getCurrent().returns(successKey,false, reasonKey, "格式不正确");
			return;
		}
		jo.remove("names");
		
		jo.put("pid", Port.getCurrent().createReturnAsync());
		//查询的列（id）
		List<String> coList= new ArrayList<>();
		coList.add(Human.K.id);
		
		String reason = "";
		
		charge(jo, nameList, coList, reason);
	}*/
	
	public void charge(JSONObject jo, List<String> nameList, List<String> coList, String reason){
		if(nameList.isEmpty()){
			boolean flag = false;
			if(reason.length()==0){
				reason = "操作成功";
				flag = true;
			}
			//循环出口
			Port.getCurrent().returnsAsync(jo.getLongValue("pid"), successKey, flag, reasonKey, reason);
			return;
		}
		String name = nameList.remove(0);
		
		String whereSql = Utils.createStr(" where name = '{}'", name);
		if(name.length() >= 18){
			whereSql = Utils.createStr(" where id = '{}'", name);
		}
		DB prx = DB.newInstance(Human.tableName);
		prx.findByQuery(false, whereSql, DBKey.COLUMN, coList);
		prx.listenResult(this::_result_charge1, "jo", jo, "nameList", nameList, "coList", coList, reasonKey, reason, "name", name);
	}
	private void _result_charge1(Param results, Param context){
		List<RecordTransient> list = results.get();
		JSONObject jo = context.get("jo");
		List<String> nameList = context.get("nameList");
		List<String> coList = context.get("coList");
		String reason = context.getString(reasonKey);
		long id;
		if(list.size() > 0){
			id = list.get(0).get("id");
		}else{
			reason += "玩家 " + context.getString("name") + " 不存在<br/>";
			charge(jo, nameList, coList, reason);
			return;
		}
		//充值
		pay(id, jo.getIntValue("sn"));
		
		
		//发送其他玩家充值
		charge(jo, nameList, coList, reason);
	}
	private void pay(long humanId, int sn) {
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_pay, "sn", sn, "humanId", humanId);
		
	}
	private void _result_pay(Param results, Param context){/*
		long humanId = context.get("humanId");
		int sn = context.get("sn");
		HumanGlobalInfo info = results.get();
		if(info != null){
			//在线
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, humanId);
			prx.gm_pay(sn);
		}else{
			
			if(Config.GAME_PLATFORM_NAME.equals("tw")){
				return;
			}
			//代办
			Pocket.add(humanId, PocketLineKey.GM_CHARGE, String.valueOf(sn));
		}*/
	}
	/**
	 * 充值补发代办
	 * @param param
	 *//*
	@Listener(value = EventKey.POCKET_LINE_HANDLE_ONE, subStr = PocketLineEventSubKey.HUMAN_POCKET_LINE_HANDLE_GM_CHARGE)
	public void pocketLine_pay(Param param){
		HumanObject humanObj = param.get("humanObj");
		PocketLine p = param.get("pocketLine");
		int sn = Integer.parseInt(p.getParam());
		ConfPayCharge chargeConf= ConfPayCharge.get(sn);
		if(chargeConf == null) {
			return;
		}
		int num = 1;
		JSONObject json = Utils.toJSONObject(humanObj.getHuman().getPayCharge());
		if(json.containsKey(String.valueOf(sn))){
			num = json.getIntValue(String.valueOf(sn)) + 1;
		}
		json.put(String.valueOf(sn), num);
		humanObj.getHuman().setPayCharge(json.toJSONString());
	}*/
	
//	/**-------------内部充值-------------- */
//	@Listener(value = EventKey.GM, subStr = { "innerPay" })
//	public void chargeGs(Param param) {
//		JSONObject jo = param.get();
//		int money = jo.getIntValue("money");
//		long humanId = jo.getLongValue("humanId");
//		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
//		prx.getInfo(humanId);
//		prx.listenResult(this::_result_chargeGs,"pid", Port.getCurrent().createReturnAsync(), "money", money, "humanId", humanId);
//	}
//	
//	private void _result_chargeGs(Param results, Param context){
//		long humanId = context.getLong("humanId");
////		long pid = context.getLong("pid");
//		int money = context.getInt("money");
//		HumanGlobalInfo info = results.get();
//		if(info != null){
//			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
//			prx.chargeGs(money);
//			prx.listenResult(this::_result_chargeGs2, context);
////			Port.getCurrent().returnsAsync(pid, successKey, true);
//		}else{
//			List<String> columns = new ArrayList<>();
//			columns.add(Human.K.id);
//			columns.add(Human.K.gs);
//			String whereSql = Utils.createStr(" where {}={}", Human.K.id, humanId);
//			DB db = DB.newInstance(Human.tableName);
//			db.findByQuery(false, whereSql, DBKey.COLUMN, columns);
//			db.listenResult(this::_result_chargeGs1, context);
//		}
//	}
//	
//	private void _result_chargeGs1(Param results, Param context){
//		long pid = context.getLong("pid");
//		int money = context.getInt("money");
//		List<RecordTransient> list = results.get();
//		if(list.isEmpty()){
//			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "玩家不存在");
//			return;
//		}
//		int isGs = 0;
//		for(RecordTransient record : list){
//			isGs = record.get(Human.K.gs);
//		}
//		if(isGs == 0){
//			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "该玩家不是gs");
//			return;
//		}
//		
//		long humanId = context.getLong("humanId");
//		Pocket.add(humanId, PocketLineKey.Charge_Gs, String.valueOf(money));
//		Port.getCurrent().returnsAsync(pid, successKey, true);
//	}
//	private void _result_chargeGs2(Param results, Param context){
//		long pid = context.getLong("pid");
//		boolean result = results.get(successKey);
//		String reason = results.get(reasonKey);
//		if(result){
//			Port.getCurrent().returnsAsync(pid, successKey, true);
//		}else{
//			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, reason);
//		}
//	}
//	/**
//	 * gs充值代办
//	 * @param param
//	 */
//	@Listener(value = EventKey.POCKET_LINE_HANDLE_ONE, subStr = PocketLineEventSubKey.HUMAN_POCKET_LINE_HANDLE_GS_CHARGE)
//	public void pocketLine_Gs_pay(Param param){
//		HumanObject humanObj = param.get("humanObj");
//		PocketLine p = param.get("pocketLine");
//		int money = Integer.parseInt(p.getParam()) * 648 + humanObj.getHuman().getGsRmb();
//		if(money > CHARGE_Gs){
//			money = CHARGE_Gs;
//		}
//		if(humanObj.getHuman().isGs()){
//			humanObj.getHuman().setGsRmb(money);
//		}else{
//			humanObj.getHuman().setGsRmb(0);
//		}
//	}
	
}
