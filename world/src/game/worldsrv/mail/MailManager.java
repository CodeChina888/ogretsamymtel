package game.worldsrv.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Record;
import core.RecordTransient;
import core.db.DBKey;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;
import game.msg.Define.DMail;
import game.msg.Define.DMailItem;
import game.msg.Define.EMailType;
import game.msg.MsgMail.SCMailList;
import game.msg.MsgMail.SCMailNewRemind;
import game.msg.MsgMail.SCPickupItemMailResult;
import game.msg.MsgMail.SCReadMail;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.entity.FillMail;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Mail;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.rune.RuneManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class MailManager extends ManagerBase {

	// public static String COMPETE_TITLE = "竞技场奖励";
	public static long DEL_TIMESTAMP = Time.DAY * 3; // 最长邮件保存时间
	private static final int countPerFind = 1000; // 每次查询1000
	public static final long SYS_SENDER = 1;      //系统发送
	public static final String SYS_SENDER_NAME = "系统";      //系统发送
	public static final long PICKEUP_ALL_MAIL = -1;		//领取所有邮件的标记
	/**
	 * 获取实例
	 * @return
	 */
	public static MailManager inst() {
		return inst(MailManager.class);
	}

	/**
	 * 玩家其它数据加载开始：加载玩家的邮件信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Mail.tableName);
		dbPrx.findBy(false, Mail.K.Receiver,humanObj.getHumanId());
		dbPrx.listenResult(this::_result_loadHumanMail, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanMail(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanMail humanObj is null");
			return;
		}
		
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanMail records=null");
		} else {
			humanObj.mailList.clear();// 先清掉，4点时候会清一下数据库中过期的邮件。
			for (Record record : records) {
				Mail mail = new Mail(record);
				humanObj.mailList.add(mail);
			}
			// 玩家上线时自动下发数据：邮件相关的数据
			_msg_CSOpenMailList(humanObj);
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	
	/**
	 * 登录成功
	 * @param param
	 */
	@Listener(EventKey.HumanLoginFinish)
	public void onHumanLoginFinish(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		FillMailServiceProxy prx = FillMailServiceProxy.newInstance();
		prx.loginCheck(humanObj.id, human.getFillMailJson(), human.getServerId(), human.getTimeCreate());
		prx.listenResult(this::_result_login_fillMail, "humanObj", humanObj);
	}
	private void _result_login_fillMail(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		List<FillMail> mailList = results.get("mailList");
		String mailJson = results.get("mailJson");
		
		if(null != mailList && !mailList.isEmpty()){
			for(FillMail mail : mailList){
				List<ProduceVo> itemProduce = ProduceManager.inst().jsonToProduceList(mail.getItemJSON());
				MailManager.inst().sendSysMail(humanObj.id, mail.getTitle(), mail.getContent(), itemProduce);
			}
		}
		
		humanObj.getHuman().setFillMailJson(mailJson);
	}
	
	/**
	 * 打开邮箱 发送所有邮件
	 * @param humanObj
	 */
	public void _msg_CSOpenMailList(HumanObject humanObj) {
		List<Mail> mailList = removePastMail(humanObj.mailList);
		if (mailList.isEmpty()) {
			return;
		}
		Collections.sort(mailList, (m1, m2) -> (int) (m1.getAcceptTimestamp() - m2.getAcceptTimestamp()));
		// 构建返回信息
		SCMailList.Builder msg = SCMailList.newBuilder();
		for (Mail mail:mailList) {
			if(mail.isRead() && mail.isPickup()) continue; // 已读与领取 不发送
			if(mail.isRead() && Utils.isEmptyJSONString(mail.getItemJSON())) continue; // 已读与没有物品 不发送
			msg.addMails(this.builtDmailMsg(mail));
		}
		humanObj.sendMsg(msg);
	}
	/**
	 * 删除过期邮件
	 * @param mailList
	 * @return
	 */
	private List<Mail> removePastMail(List<Mail> mailList){
		Iterator<Mail> it = mailList.iterator();
		// 清除玩家已经过期的邮件
		long time =  Port.getTime();
		while(it.hasNext()){
			Mail mail = it.next();
			if (mail.getDeleteTimestamp() <= time) {
				it.remove();
			}
		}
		return mailList;
	}
	
	public void sendMail(long humanId, long sender, String senderName, 
			String title, String content, List<ProduceVo> itemProduce) {
		// 如果标题为空就不发邮件
		if (title == null || title.isEmpty()) {
			return;
		}
		// 持久化邮件
		Mail mail = new Mail();
		mail.setId(Port.applyId());
		mail.setReceiver(humanId);//接受者
		mail.setSender(sender);//发送者
		mail.setSenderName(senderName);//发送者名字
		mail.setTitle(title);
		mail.setContent(content);
		mail.setAcceptTimestamp(Port.getTime());
		mail.setDeleteTimestamp(mail.getAcceptTimestamp() + DEL_TIMESTAMP);
		mail.setRead(false);
		mail.setPickup(false);
		// 判断物品是否为空
		if (itemProduce != null)
			mail.setItemJSON(ProduceManager.inst().produceToJson(itemProduce));
		mail.persist();
		// 通知给玩家
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_remindHumanTo, "mail", mail, "humanId", humanId);
	}
	
	/**
	 * 系统发送邮件，给某一个玩家，携带物品
	 * @param title
	 * @param content
	 * @param itemProduce
	 */
	public void sendSysMail(long humanId, String title, String content, List<ProduceVo> itemProduce) { 
		this.sendSysMail(humanId, title, content, itemProduce, DEL_TIMESTAMP);
	}
	/**
	 * 系统发送邮件，给某一个玩家，携带物品
	 * @param title
	 * @param content
	 * @param itemProduce
	 * @param duringTime 保存时间
	 */
	public void sendSysMail(long humanId, String title, String content, List<ProduceVo> itemProduce, long duringTime) {
		// 如果标题为空就不发邮件
		if (title == null || title.isEmpty()) {
			return;
		}
		// 持久化邮件
		Mail mail = new Mail();
		mail.setId(Port.applyId());
		mail.setReceiver(humanId);//接受者
		mail.setSender(MailManager.SYS_SENDER);//发送者
		mail.setSenderName(MailManager.SYS_SENDER_NAME);//发送者名字
		mail.setTitle(title);
		mail.setContent(content);
		mail.setAcceptTimestamp(Port.getTime());
		mail.setDeleteTimestamp(mail.getAcceptTimestamp() + duringTime);
		mail.setRead(false);
		mail.setPickup(false);
		// 判断物品是否为空
		if (itemProduce != null)
			mail.setItemJSON(ProduceManager.inst().produceToJson(itemProduce));
		mail.persist();
		// 通知给玩家
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_remindHumanTo, "mail", mail, "humanId", humanId);
	}
	
	/**
	 * 存储邮件
	 * @param humanId
	 * @param title
	 * @param content
	 * @param itemProduce
	 * @param duringTime
	 */
	public void sendSysMailNoNotify(long humanId, String title, String content, List<ProduceVo> itemProduce, long duringTime) {
		// 如果标题为空就不发邮件
		if (title == null || title.isEmpty()) {
			return;
		}
		// 持久化邮件
		Mail mail = new Mail();
		mail.setId(Port.applyId());
		mail.setReceiver(humanId);//接受者
		mail.setSender(MailManager.SYS_SENDER);//发送者
		mail.setSenderName(MailManager.SYS_SENDER_NAME);//发送者名字
		mail.setTitle(title);
		mail.setContent(content);
		mail.setAcceptTimestamp(Port.getTime());
		mail.setDeleteTimestamp(mail.getAcceptTimestamp() + duringTime);
		mail.setRead(false);
		mail.setPickup(false);
		// 判断物品是否为空
		if (itemProduce != null)
			mail.setItemJSON(ProduceManager.inst().produceToJson(itemProduce));
		mail.persist();
	}
	
	private void _result_remindHumanTo(Param results, Param context) {
		long humanId = Utils.getParamValue(context, "humanId", -1L);
		Mail mail = Utils.getParamValue(context, "mail", null);
		if (humanId <= 0 || mail == null) {
			Log.game.error("===_result_remindHumanTo humanId={}, mail={}", humanId, mail);
			return;
		}
		HumanGlobalInfo humanToInfo = results.get();
		if (humanToInfo != null) {
			// 接收者接收邮件
			HumanObjectServiceProxy humanPrx = HumanObjectServiceProxy.newInstance(humanToInfo.nodeId,
					humanToInfo.portId, humanId);
			humanPrx.mailAccept(mail);
		}
	}
	
	/**
	 * 系统发送邮件，给某个玩家，不带物品
	 * @param title
	 */
	public void _msg_CSSendMail(long humanId, String title, String detail) {
		sendSysMail(humanId, title, detail, null);
	}

	/**
	 * 给一封邮件设置已读
	 * @param humanObj
	 * @param mailId
	 */
	public void _msg_CSReadMail(HumanObject humanObj, long mailId) {
		Boolean state = false;
		for (Mail mail : humanObj.mailList) {
			if (mail.getId() == mailId){
				mail.setRead(true);
				state = true;
				break;
			}
		}
		//发送消息
		SCReadMail.Builder msg = SCReadMail.newBuilder();
		msg.setId(mailId);
		msg.setSuccess(state);
		humanObj.sendMsg(msg);
	}

	/**
	 * 在线接收邮件后续操作
	 * @param humanObj
	 */
	public void addNew(HumanObject humanObj, Mail mail) {
		// 放到玩家身上第一个
		if (humanObj.mailList.size() > 1) {
			humanObj.mailList.add(0, mail);// 是 add,不是set
		} else {
			humanObj.mailList.add(mail);
		}
		// 给前端发消息
		SCMailNewRemind.Builder msg = SCMailNewRemind.newBuilder();
		msg.setMail(this.builtDmailMsg(mail));
		humanObj.sendMsg(msg);

		// 如果超过100封做删除处理
		int numTotal = humanObj.mailList.size();
		int numMax = ParamManager.mailMax;
		if (numTotal > numMax) {
			// 遍历后面的
			List<Mail> toRemove = new ArrayList<Mail>();
			
			for (int i = numTotal-1; i >= numMax; i--) {//注意:这里要倒着删除
				toRemove.add(humanObj.mailList.get(i));
				humanObj.mailList.remove(i);
			}
			// 删除
			for (Mail delMail : toRemove) {
				if(null != delMail)
					delMail.remove();
			}
		}
	}

	/**
	 * 获取邮件中的物品
	 * @param humanObj
	 * @param mailId
	 */
	public void _msg_CSPickupMailItem(HumanObject humanObj, long mailId) {
		SCPickupItemMailResult.Builder resultMsg = SCPickupItemMailResult.newBuilder();
		if (mailId == PICKEUP_ALL_MAIL){
			for (Mail mail : humanObj.mailList) {
				String content = mail.getContent();
				content = content.substring(1, content.length() - 1);
				// 获取类型
				int[] contents = Utils.strToIntArraySplit(content);
				// 分解物品
				List<ProduceVo> itemList = ProduceManager.inst().jsonToProduceList(mail.getItemJSON());
				// 符文类型
				if (contents[0] == EMailType.MailItemToRune_VALUE) {
					// 暂时没有符文相关的邮件了
					if (!RuneManager.inst().process_ItemToNewRune(humanObj, itemList)) {
						continue;
					}
				} else {
					// 判断物品是否可以添加到背包里
					boolean ret = ProduceManager.inst().canGiveProduceItem(humanObj, itemList);
					if (!ret) {
						continue;
					}
					// 添加到玩家的背包里
					ProduceManager.inst().giveProduceItem(humanObj, itemList, LogSysModType.MailPickup);
				}
				
				mail.remove();// 清掉数据库中的数据
				resultMsg.addId(mail.getId());
			}
			humanObj.mailList.clear();
		}
		else{
			Mail mail = null;
			// 查找当前的邮件
			for (Mail m : humanObj.mailList) {
				// 未过期
				if (m.getId() == mailId && m.getDeleteTimestamp() > Port.getTime()) {
					mail = m;
					break;
				}
			}
			
			// 没找到
			if (mail == null) {
				// 发送文字提示消息 邮件不存在
				humanObj.sendSysMsg(310101);
				return;
			}
			
			String content = mail.getContent();
			content = content.substring(1, content.length() - 1);
			// 获取类型
			int[] contents = Utils.strToIntArraySplit(content);
			// 分解物品
			List<ProduceVo> itemList = ProduceManager.inst().jsonToProduceList(mail.getItemJSON());
			// 符文类型
			if (contents[0] == EMailType.MailItemToRune_VALUE) {
				if (!RuneManager.inst().process_ItemToNewRune(humanObj, itemList)) {
					return;
				}
			} else {
				// 判断物品是否可以添加到背包里
				boolean ret = ProduceManager.inst().canGiveProduceItem(humanObj, itemList);
				if (!ret) {
					return;
				}
		
				// 添加到玩家的背包里
				ProduceManager.inst().giveProduceItem(humanObj, itemList, LogSysModType.MailPickup);
			}
	
			// 设置邮件状态 直接删除
			humanObj.mailList.remove(mail);
			mail.remove();// 清掉数据库中的数据
			resultMsg.addId(mail.getId());
		}
		
		// 推送给前端新的列表 构建返回信息
//		SCMailList.Builder msg = SCMailList.newBuilder();
//		for (Mail m : humanObj.mailList) {
//			msg.addMails(this.builtDmailMsg(m));
//		}

//		humanObj.sendMsg(msg);

		resultMsg.setResult(true);
		humanObj.sendMsg(resultMsg);

	}

	/**
	 * 构建Dmail信息
	 * @param mail
	 * @return
	 */
	public DMail.Builder builtDmailMsg(Mail mail) {
		DMail.Builder msg = DMail.newBuilder();
		msg.setId(mail.getId());
		msg.setReceiver(mail.getReceiver());
		msg.setSender(mail.getSender());
		msg.setSenderName(mail.getSenderName());
		msg.setTitle(mail.getTitle());
		msg.setContent(mail.getContent());
		msg.setAcceptTimestamp(mail.getAcceptTimestamp());
		msg.setDeleteTimestamp(mail.getDeleteTimestamp());
		msg.setRead(mail.isRead());
		msg.setPickup(mail.isPickup());
		Map<Integer, Integer> itemsMap = Utils.jsonToMapIntInt(mail.getItemJSON());
		for (Map.Entry<Integer, Integer> entry : itemsMap.entrySet()) {
			DMailItem.Builder dMailItem = DMailItem.newBuilder();
			dMailItem.setItemSn(entry.getKey());
			dMailItem.setItemNum(entry.getValue());
			msg.addItems(dMailItem);
		}
		return msg;
	}

	/**
	 * 给所有玩家发送系统邮件
	 * @param produces
	 */
	public void sendMailAll(String... produces) {
		// 要发送的消息以及物品
		List<ProduceVo> list = new ArrayList<ProduceVo>();
		if (produces.length > 2) {
			for (int i = 3; i < produces.length; i += 2) {
				ProduceVo pr = new ProduceVo(Utils.intValue(produces[i]), Utils.intValue(produces[i + 1]));
				list.add(pr);
			}
		}
		// 所有用户
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.countBy(false);
		Param result = dbPrx.waitForResult();
		int numAll = result.get();
		int loopCount = numAll / countPerFind;
		for (int i = 0; i <= loopCount; i++) {
			String sql = Utils.createStr(" limit {},{}", i * countPerFind,countPerFind);
			List<String> colums = new ArrayList<String>();
			colums.add("id");
			dbPrx.findByQuery(false, sql, DBKey.COLUMN,colums);
			dbPrx.listenResult(this::_result_sendMailAll, "list", list, "titile", produces[1], "detail", produces[2]);
		}
	}
	
	public void sendMailAll(String title,String detail,String[] itemSn,String[] itemNum){
		List<ProduceVo> list = new ArrayList<ProduceVo>();
		for (int i = 0; i < itemSn.length; i ++) {
			list.add(new ProduceVo(Utils.intValue(itemSn[i]), Utils.intValue(itemNum[i])));
		}
		// 所有用户
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.countBy(false);
		Param result = dbPrx.waitForResult();
		int numAll = result.get();
		int loopCount = numAll / countPerFind;
		for (int i = 0; i <= loopCount; i++) {
			String sql = Utils.createStr(" limit {},{}", i * countPerFind,countPerFind);
			List<String> colums = new ArrayList<String>();
			colums.add("id");
			dbPrx.findByQuery(false, sql, DBKey.COLUMN,colums);
			dbPrx.listenResult(this::_result_sendMailAll, "list", list, "titile", title, "detail", detail);
		}
	}
	
	private void _result_sendMailAll(Param results, Param context) {
		List<RecordTransient> records = results.get();
		List<ProduceVo> list = Utils.getParamValue(context, "list", null);
		String title = Utils.getParamValue(context, "titile", "");
		String detail = Utils.getParamValue(context, "detail", "");
		// 逐个发送
		for (RecordTransient r : records) {
			if (r != null) {
				sendSysMail(r.get("id"), title, detail, list);
			}
		}
	}

	/**
	 * 开服七天已完成但未领取的奖励发送给用户
	 * @param humanId
	 * @param itemProduce
	 */
	public void sendSysMailActivityOpenSeven(long humanId, List<ProduceVo> itemProduce) {
		// 特殊邮件内容：{MailTemplate.sn}
//		String detail = "{" + EMailType.ActOpenSeven.value() + "}";
//		sendSysMail(humanId, ParamManager.mailMark, detail, itemProduce);
	}
	
}
