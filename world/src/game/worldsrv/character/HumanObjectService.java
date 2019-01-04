package game.worldsrv.character;

import game.msg.Define.DMemberInfo;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EFriendType;
import game.msg.Define.ELootMapType;
import game.msg.Define.EMailType;
import game.msg.Define.ETeamType;
import game.msg.MsgCross.SCCrossFightInfo;
import game.msg.MsgFriend.SCFriendInfo;
import game.msg.MsgGuild.SCGuildJoinResult;
import game.msg.MsgGuild.SCGuildLeaveResult;
import game.msg.MsgInstLootMap.SCLootMapIntoSignUpRoom;
import game.msg.MsgInstLootMap.SCLootMapLeaveSignUpRoom;
import game.msg.MsgInstLootMap.SCLootMapReadyEnter;
import game.msg.MsgInstLootMap.SCLootMapSingUpRoomTimeOut;
import game.msg.MsgTeam.SCTeamApplyJoin;
import game.msg.MsgTeam.SCTeamKickOut;
import game.msg.MsgTeam.SCTeamLeave;
import game.msg.MsgTeam.SCTeamMatch;
import game.msg.MsgTeam.SCTeamMatchCancel;
import game.msg.MsgTeam.SCTeamVSTeam;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.seam.msg.HumanExtendMsgHandler;
import game.worldsrv.config.ConfPayCharge;
import game.worldsrv.entity.Castellan;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.FillMail;
import game.worldsrv.entity.Friend;
import game.worldsrv.entity.FriendObject;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.entity.Mail;
import game.worldsrv.entity.PayLog;
import game.worldsrv.enumType.SwitchState;
import game.worldsrv.enumType.TeamBundleType;
import game.worldsrv.fightrecord.RecordInfo;
import game.worldsrv.friend.FriendConstants;
import game.worldsrv.friend.FriendManager;
import game.worldsrv.guild.GuildInstData;
import game.worldsrv.guild.GuildInstManager;
import game.worldsrv.guild.GuildManager;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.instLootMap.InstLootMapManager;
import game.worldsrv.instLootMap.Room.InstLootMapSignUpHuman;
import game.worldsrv.instWorldBoss.InstWorldBossManager;
import game.worldsrv.instWorldBoss.WBData;
import game.worldsrv.integration.PF_PAY_Manager;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.payment.PaymentManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;
import game.worldsrv.support.MathUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamData;
import game.worldsrv.team.TeamManager;

import java.lang.reflect.Method;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Message;

import core.CallPoint;
import core.Port;
import core.Service;
import core.connsrv.ConnectionProxy;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.ManagerBase;
import core.support.MsgHandler;
import core.support.Param;

@DistrClass(importClass = {Mail.class, TeamData.class, List.class, FillMail.class,
		FriendObject.class, WBData.class, InstLootMapSignUpHuman.class, ELootMapType.class, 
		Message.class, HumanMirrorObject.class, RecordInfo.class, ETeamType.class, ECrossFightType.class ,Castellan.class,GuildInstData.class})
public class HumanObjectService extends Service {
	private HumanExtendMsgHandler msgHandler = MsgHandler.getInstance(HumanExtendMsgHandler.class);

	// 对应的玩家对象
	private final HumanObject humanObj;

	/**
	 * 构造函数
	 * @param humanObj
	 */
	public HumanObjectService(HumanObject humanObj, Port port) {
		super(port);
		this.humanObj = humanObj;
	}
	
	@Override
	public Object getId() {
		return humanObj.id;
	}

	/**
	 * 获取所属Port
	 * @return
	 */
	public StagePort getPort() {
		return humanObj.getPort();
	}

	/**
	 * 离开地图
	 */
	@DistrMethod
	public void leave() {
		humanObj.stageLeave();
	}

	@DistrMethod
	public void changeConnPoint(CallPoint point) {
		humanObj.connPoint = point;
		humanObj.ttDeferClose.stop();
		humanObj.stageShow();// add by shenjh,显示断线重连玩家
		humanObj.isInCloseDelay = false;// add by shenjh,断线延迟状态
		//
		Log.human.debug("===sjh断线重连：连接成功 name={},teamIdRecord={}", humanObj.getHuman().getName(),
				humanObj.getTeamIdRecord());
		if (humanObj.getTeamIdRecord() <= 0)
			teamLeave(true);// add by shenjh,断线重连玩家下发通知离队消息
	}

	/**
	 * 接受并转发通信消息
	 * @param chunk
	 */
	@DistrMethod
	public void msgHandler(long connId, byte[] chunk) {
		// 忽略错误连接ID的请求
		long humanConnId = (long) humanObj.connPoint.servId;
		if (humanConnId != connId) {
			// 将发送错误连接的请求连接关了
			CallPoint connPoint = new CallPoint();
			connPoint.nodeId = port.getCallFromNodeId();
			connPoint.portId = port.getCallFromPortId();
			connPoint.servId = connId;

			ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
			Log.human.debug("msgHandler close");
			prx.close();
			return;
		}

		msgHandler.handle(chunk, "humanObj", humanObj);
	}
	
	/**
	 * 连接关闭
	 * @param connId
	 */
	@DistrMethod
	public void connClosed(long connId) {
		// 忽略错误连接ID的请求
		long humanConnId = (long) humanObj.connPoint.servId;
		if (humanConnId != connId) {
			Log.human.error("HumanObjectService.connClosed : humanConnId={}, connId={}, time={}", humanConnId, connId,
					Port.getTime());
			return;
		}
		if (Log.human.isDebugEnabled()) {
			Log.human.debug("HumanObjectService.connClosed : connId={}, time={}", connId, Port.getTime());
		}
		// 延时T人
		humanObj.connDelayCloseClear();
	}

	@DistrMethod
	public void kickClosed(long connId) {
		// 忽略错误连接ID的请求
		long humanConnId = (long) humanObj.connPoint.servId;
		if (humanConnId != connId) {
			Log.human.error("HumanObjectService.kickClosed : humanConnId={}, connId={}, time={}", humanConnId, connId,
					Port.getTime());
			return;
		}
		if (Log.human.isDebugEnabled()) {
			Log.human.debug("HumanObjectService.kickClosed : connId={}, time={}", connId, Port.getTime());
		}
		// 直接T人
		humanObj.connCloseClear();
	}

	/**
	 * 连接存活验证
	 * @param connId
	 */
	@DistrMethod
	public void connCheck(long connId) {
		port.returns(true);
	}

	/**
	 * 调度事件的处理，来自于humanGlobalSerivce
	 * @param key
	 */
	@DistrMethod
	public void onSchedule(int key, long timeLast) {
        Log.game.info("-------------- onSchedule {} {}", key, timeLast);
		HumanManager.inst().onScheduleEvent(humanObj, key, timeLast);
	}

	/**
	 * 接收邮件
	 */
	@DistrMethod
	public void mailAccept(Mail mail) {
		MailManager.inst().addNew(humanObj, mail);
	}

	@DistrMethod
	public void getHuman() {
		getPort().returns(humanObj.getHuman());
	}

	public HumanObject getHumanObj() {
		return humanObj;
	}

	/**
	 * 更新队伍信息
	 */
	@DistrMethod
	public void teamInfoUpdate(TeamData team) {
		humanObj.setTeam(team);
		// 通知队伍详细信息
		humanObj.sendMsg(team.createMsg());
	}

	/**
	 * 更新队员信息
	 */
	@DistrMethod
	public void teamMemberInfoUpdate(TeamData team) {
		humanObj.setTeam(team);
		// 通知队员信息改变
		humanObj.sendMsg(team.createMemberInfoMsg());
	}

	/**
	 * 申请入队
	 */
	@DistrMethod
	public void teamApplyJoin(int result, TeamData team) {
		if (team == null)
			return;

		humanObj.setTeam(team);
		humanObj.teamBundleID = TeamBundleType.FriendOne.value();// 友方（队伍1）

		// 加入的人通知加入返回结果消息
		SCTeamApplyJoin.Builder msg = SCTeamApplyJoin.newBuilder();
		msg.setResult(result);// 0成功（成功才返回队伍信息）；>0失败（参见SysMsg.xlsx）
		if (result == 0) {
			msg.setTeamInfo(team.createDTeamInfo());
			msg.addAllMemberInfo(team.createDMemberInfo());
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 主动离队
	 */
	@DistrMethod
	public void teamLeave(boolean result) {
		if (result) {
			humanObj.setTeam(null);
			humanObj.teamBundleID = humanObj.getHumanId();
		}
		// 被踢的人通知离队消息
		SCTeamLeave.Builder msg = SCTeamLeave.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}

	/**
	 * 被踢出队伍
	 */
	@DistrMethod
	public void teamKickOut(boolean result) {
		if (result) {
			humanObj.setTeam(null);
			humanObj.teamBundleID = humanObj.getHumanId();
		}
		// 被踢的人通知被踢消息
		SCTeamKickOut.Builder msg = SCTeamKickOut.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}

	/**
	 * 进入PVE副本
	 */
	@DistrMethod
	public void teamEnterInst(int actInstSn, int mapSn, long stageId, int index) {
		humanObj.setTeamStartOpen();// 设置为开启副本
		humanObj.teamBundleID = TeamBundleType.FriendOne.value();// 友方（队伍1）
		// Log.human.info("===进入PVE副本teamEnterInst：teamBundleID={},humanId={}",
		// humanObj.teamBundleID, humanObj.id);
		
		// 活动副本完成次数自增1
		TeamManager.inst().addActInstFinishNum(humanObj, actInstSn);
		
		Event.fire(EventKey.ActInstTeamPVEEnter, "humanObj", humanObj);// 发布事件：TEAM_PVE

		// 出生点及朝向
		Vector2D pos = StageManager.inst().getHumanPos(mapSn, index);
		Vector2D dir = MathUtils.getDir(pos, StageManager.inst().getHumanDir(mapSn, index));
		StageManager.inst().switchTo(humanObj, stageId, pos, dir);
	}

	/**
	 * 进入PVP副本
	 */
	@DistrMethod
	public void teamEnterPVP(int actInstSn, int mapSn, long stageId, int group, int index,
			List<DMemberInfo> listDMemInfoTeam1, List<DMemberInfo> listDMemInfoTeam2) {
		humanObj.setTeamStartOpen();// 设置为开启副本
		humanObj.teamBundleID = group;// 组队下的敌我标识置为队伍所在分组
		// Log.human.info("===进入PVP副本teamEnterPVP：teamBundleID={},humanId={},group={}",
		// humanObj.teamBundleID,
		// humanObj.id, group);
		
		// 活动副本完成次数自增1
		TeamManager.inst().addActInstFinishNum(humanObj, actInstSn);
				
		// 下发队伍对战成员信息
		SCTeamVSTeam.Builder msg = SCTeamVSTeam.newBuilder();
		msg.addAllMemberOne(listDMemInfoTeam1);
		msg.addAllMemberTwo(listDMemInfoTeam2);
		humanObj.sendMsg(msg);

		Event.fire(EventKey.ActInstTeamPVPEnter, "humanObj", humanObj);// 发布事件：TEAM_PVP

		// 出生点及朝向
		Vector2D pos = new Vector2D();
		Vector2D dir = new Vector2D();
		if (group == 1) {// 第1组队伍位置读取我方出生点
			pos = StageManager.inst().getHumanPos(mapSn, index);
			dir = MathUtils.getDir(pos, StageManager.inst().getHumanDir(mapSn, index));
		} else if (group == 2) {// 第2组队伍位置读取敌方出生点
			pos = StageManager.inst().getEnemyPos(mapSn, index);
			dir = MathUtils.getDir(pos, StageManager.inst().getEnemyDir(mapSn, index));
			if (pos.isZero()) {// 没配数据，为了避免错误，还是传到我方出生点得了
				pos = StageManager.inst().getHumanPos(mapSn, index);
				Log.human.error("===teamEnterPVP posAppear.isZero,group={},name={},mapSn={},index={}", group,
						humanObj.name, mapSn, index);// sjh
			}
		}
		// Log.human.info("===teamEnterPVP posAppear={},group={},name={},mapSn={},index={}",
		// posAppear, group, humanObj.name, mapSn, index);
		StageManager.inst().switchTo(humanObj, stageId, pos, dir);
	}

	/**
	 * 通知队员队伍正在匹配中...
	 */
	@DistrMethod
	public void teamMatch(int result) {
		humanObj.setTeamMatchOpen();// 设置为匹配中
		SCTeamMatch.Builder msg = SCTeamMatch.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}

	/**
	 * 通知队员队伍取消匹配
	 */
	@DistrMethod
	public void teamMatchCancel(int result) {
		humanObj.setTeamMatchClose();// 设置为匹配结束
		SCTeamMatchCancel.Builder msg = SCTeamMatchCancel.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 发送广播世界BOSS活动结束
	 */
	@DistrMethod
	public void sendSCWorldBossEnd(WBData wbData) {
		InstWorldBossManager.inst().sendSCWorldBossEnd(humanObj, wbData);
	}
	
	/**
	 * 在线玩家gm权限设置 
	 * @param value 0：无权限，1：gm权限
	 */
	@DistrMethod
	public void setGm(int value){
		humanObj.getHuman().setGMPrivilege(value);
	}
	
	/**
	 * 禁言/封号 
	 * @param type 1:禁言   2:封号
	 * @param timeEnd
	 */
	@DistrMethod
	public void sealAccount(int type, long timeEnd){
		if(type == 1){
			humanObj.getHuman().setSilenceEndTime(timeEnd);
		}else{
			humanObj.getHuman().setSealEndTime(timeEnd);
			
			if(System.currentTimeMillis() < timeEnd){
				humanObj.sendSysMsg(163);//你被封号了
				HumanGlobalServiceProxy hprx = HumanGlobalServiceProxy.newInstance();
				hprx.kick(humanObj.id, 163);//踢下线
			}
		}
	}
	
	/**
	 * 禁言
	 * @param keepTime 持续时间（单位：毫秒）
	 */
	@DistrMethod
	public void silence(long keepTime){
		
		humanObj.silence(keepTime);
	}
	
	/**
	 * 给玩家禁言
	 * @param
	 */
	@DistrMethod
	public void setSilenceEndTime() {
//		long curTime = Port.getTime();
//		long endTime = humanObj.getHuman().getSilenceEndTime();
//		int hour = ConfGlobalUtils.getValue(ConfGlobalKey.系统自动禁言时间);
//		if(endTime > curTime) {
//			endTime += hour * Time.HOUR;
//		} else {
//			endTime = curTime + hour * Time.HOUR;
//		}
//		humanObj.getHuman().setSilenceEndTime(endTime);
	}
	
	/**
	 * 设置vip等级
	 * @param
	 */
	@DistrMethod
	public void setVipLv(int vipLv){
		
		humanObj.setVipLv(vipLv);
	}
	
	/**
	 * 接收补偿邮件
	 * @param fillMail
	 */
	@DistrMethod
	public void acceptFillMail(FillMail fillMail){
		// 分解物品
		List<ProduceVo> itemList = ProduceManager.inst().jsonToProduceList(fillMail.getItemJSON());
		MailManager.inst().sendSysMail(humanObj.id, fillMail.getTitle(), fillMail.getContent(), itemList);
		
		//领取记录
		JSONArray joArray = Utils.toJSONArray(humanObj.getHuman().getFillMailJson());
		joArray.add(fillMail.getId());
		humanObj.getHuman().setFillMailJson(joArray.toJSONString());
		
	}
	
	/**
	 * 充值
	 * @return
	 */
	@DistrMethod
	public void pay(String param){

		JSONObject jo = Utils.toJSONObject(param);
//		int sn = 0;
//		String[] propId = jo.getString("sn").split("_");
//		if(propId.length > 1){
//			sn = Utils.intValue(propId[1]);
//		} else {
//			sn = Utils.intValue(propId[0]);
//		}
//		if (humanObj.payLogs){
//			port.returns(false);
//			return; 
//		}
//		Boolean k = false;
//		for (PayLog paylog : humanObj.payLogs) {
//			String x = jo.getString("orderId");
//			String y = paylog.getOrderId();
//			
//			if (paylog.getOrderId() == jo.getString("orderId")){
//				k = true;
//				break;
//				
//			}
//		}
//		if (k) {
////			port.returns(false);
//			return; 
//		}
		Boolean result = false;
		String productId = jo.getString("productId");
		String amount = jo.getString("amount");
		String orderId = jo.getString(PayLog.K.orderId);

		if(amount == null) {
			Log.charge.error("error  amount is null");
			port.returns(result);
			return;
		}
		ConfPayCharge cpc = ConfPayCharge.getBy("androidProductId", productId); 
		if(cpc == null) {
			Log.table.error("can't find  ConfPayCharge androidProductId={},orderId={}",productId,orderId);
			port.returns(result);
			return;
		}
		int amountMoney = Utils.intValue(amount)/100;
		if(amountMoney != cpc.rmb) {
			Log.charge.error("错误的充值金额 amount={} productId={},orderId={}",amount,productId,orderId);
			port.returns(result);
			return;
		}
		//		String productId = jo.getString("productId");
		int sn = cpc.sn;
		Log.game.info("payInfo productId={},sn={},orderId={}",productId,sn,orderId);
		Long re = PaymentManager.inst().onPay(humanObj, sn);
		
		if (re != null){
//			port.returns(false);
			result = true;
		}
		
		PayLog log = new PayLog();
		log.setId(Port.applyId());
		log.setActualPrice(jo.getString(PayLog.K.actualPrice));
		log.setChannelId(jo.getString("paymentType"));
		log.setChargePrice(jo.getString(PayLog.K.chargePrice));
		log.setChargeUnitId(jo.getString(PayLog.K.chargeUnitId));
		log.setCurrencyType(jo.getString(PayLog.K.currencyType));
		log.setDeviceGroupId(jo.getString(PayLog.K.deviceGroupId));
		log.setLocaleId(jo.getString(PayLog.K.localeId));
		log.setOrderId(jo.getString(PayLog.K.orderId));
		log.setUserId(jo.getString(PayLog.K.userId));
		log.setServiceId(jo.getString(PayLog.K.serviceId));
		log.setServerId(jo.getString(PayLog.K.serverId));
		log.setRoleId(jo.getString(PayLog.K.roleId));
		log.setPayChannelId(jo.getString(PayLog.K.payChannelId));
		log.setSign(jo.getString(PayLog.K.sign));
		log.setPropId(jo.getString(PayLog.K.propId));
		log.setDeviceGroupId(jo.getString(PayLog.K.deviceGroupId));
		log.setTime(Utils.formatTime(Port.getTime(), "yyyy-MM-dd HH:mm:ss"));
		log.setStatus("充值==");
		humanObj.payLogs.add(log);
		
//			PaymentManager.inst().creatPayLog(humanObj, sn, "充值");
		PF_PAY_Manager.inst().recordPayLog(humanObj.id, jo, "充值");
		
		port.returns(result);

	}
	
	
	/**
	 * 进入抢夺本报名房间
	 */
	@DistrMethod
	public void lootMapIntoSignUpRoom(InstLootMapSignUpHuman otherHuman){
		SCLootMapIntoSignUpRoom.Builder  msg = otherHuman.getSignUpRoomBuilder();
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 离开抢夺本报名房间
	 */
	@DistrMethod
	public void lootMapLeaveSignUpRoom(long humanId){
		SCLootMapLeaveSignUpRoom.Builder builder = SCLootMapLeaveSignUpRoom.newBuilder();
		builder.setHumanId(humanId);
		humanObj.sendMsg(builder);
		humanObj.clearLootMapSignUp(); // 清除报名内容消息
	}

	/**
	 * 房间超时
	 */
	@DistrMethod
	public void lootMapSignUpRoomTimeOut(){
		SCLootMapSingUpRoomTimeOut.Builder builder = SCLootMapSingUpRoomTimeOut.newBuilder();
		humanObj.sendMsg(builder);
	}
	
	@DistrMethod
	public void lootMapCostIntoItem(int itemSn,int itemNum){
		boolean costResult = InstLootMapManager.inst().consumeActivityCostItem(humanObj, itemSn, itemNum);
		port.returns("result",costResult);
	}
	
	@DistrMethod
	public void sendSCLootMapReadyEnter(int actInstSn){
		SCLootMapReadyEnter.Builder builder = SCLootMapReadyEnter.newBuilder(); // 回执客户端 准备进入游戏 客户端发送 CSLootMapEnter
		builder.setMapType(ELootMapType.LootMapMultip);
		builder.setActInstSn(actInstSn);
		humanObj.sendMsg(builder);
	}
	
	/**
	 * 进入抢夺本地图
	 */
	@DistrMethod
	public void lootMapEnter(long stageId, ELootMapType mapType) {
		//玩家列表
		StageManager.inst().switchTo(humanObj, stageId, new Vector2D(0,0), new Vector2D(0,0));
		 // 清除报名内容消息
		humanObj.clearLootMapSignUp();
		// 设置参与时间点
		if(mapType == ELootMapType.LootMapMultip) {
			HumanExtInfo exInfo = humanObj.getHumanExtInfo();
			exInfo.setLootMapMultipleJoinTime(Port.getTime());
			int count = exInfo.getLootMapMultipleJoinCount();
			exInfo.setLootMapMultipleJoinCount(count + 1);
		}
	}
	
	/**
	 * 
	 * @param mapSn
	 * @param sn
	 */
	@DistrMethod
	public void LootMapEnter(int mapSn,int sn){
		//玩家列表
		StageManager.inst().switchTo(humanObj, mapSn, new Vector2D(0,0), new Vector2D(0,0));
	}
	
	/**************好友*******************/
	/**
	 * 处理好友信息
	 * @param object 对应自己的FriendObject
	 * @param type 请求类型
	 * @param newTime 
	 */
	
	@DistrMethod
	public void dispostHumanFriend(FriendObject object,int type,long newTime){
		FriendManager.inst().disposeFriend(type, humanObj, object, Port.getTime());
	}
	
	
	/***
	 * 更新好友信息信息
	 * @param humanId
	 * @param object
	 */
	@DistrMethod
	public void updateFriendHumanObject(long humanId, FriendObject object){
		// 查询对方是不是有这个好友
		Friend record = humanObj.friendInfo.friendMap.get(object.getId());
		if (record==null) {
			return;
		}
		// 通知客户端
		SCFriendInfo.Builder msg = SCFriendInfo.newBuilder();
		msg.setOption(FriendConstants.OPTION_UPDATE);
		msg.setType(EFriendType.Friend);
		msg.setInfo(FriendManager.inst().getDFriendInfo(object, record));
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 每隔5分钟执行一次
	 */
	@ScheduleMethod(Utils.cron_Min_Five)
	public void _cron_Min_Five() {
		// 发送在线信息
		
	}
	
	/**
	 * 调用玩家实际线程处理
	 */
	@DistrMethod
	public void doHumanInCurrentThread(String className, String methodName, Param results, Param context) {
		if(humanObj.switchState != SwitchState.InStage) {
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance(); 
			prx.doHumanInCurrentThread(humanObj.id, className, methodName, results, context);
			return;
		}
		try {
           Class<?> clazz = Class.forName(className);
           Class<?>[] argsClass = {Param.class, Param.class};
           context.put(HumanObject.paramKey, this.humanObj);
           Object[] args={results, context};
           Method method = clazz.getMethod(methodName, argsClass);
           if(method != null) {
        	   ManagerBase manager = ManagerBase.inst(className);
        	   if(manager != null) {
        		   method.invoke(manager, args);
        	   }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
	}
	
	/**
	 * 每个service预留空方法
	 */
	@DistrMethod
	public void update(Object... objs) {
		
	}
	
	/**
	 * 记录并发送战斗信息
	 * @param teamType
	 * @param fightType
	 * @param mapSn
	 * @param stageId
	 */
	@DistrMethod
	public void sendFightInfo(ETeamType teamType, ECrossFightType fightType, int mapSn, long stageId) {
		humanObj.sendFightInfo(teamType, fightType, mapSn, stageId);
	}
	
	/**************************************************************************************************************
	 * 跨服消息
	 **************************************************************************************************************/
	/**
	 * 竞技PVP取消成功
	 */
	@DistrMethod
	public void pvpMatchCancelOK(){
//		CompetitionManager.inst().resetPVPState(humanObj);
//		//主动取消匹配
//		SCCompetitionRandomMatchFail.Builder msg = SCCompetitionRandomMatchFail.newBuilder();
//		msg.setCode(1);
//		humanObj.sendMsg(msg);
	}
	
	@DistrMethod
	public void pvpMatchResult(long humanId, Message msg, HumanMirrorObject enemy, String nodeId){		
		humanObj.crossFightEnemy = enemy;
		humanObj.crossFightNodeId = nodeId;
		humanObj.sendMsg(msg);
	}
	
	// 跨服使用，暂时无用
	@DistrMethod
	public void pvpMatchResult(long humanId, int type, String token, String crossIp, int crossPort, 
			int teamCamp, HumanMirrorObject enemy, String nodeId, long stageId,int stageSn,int mapSn) {
		SCCrossFightInfo.Builder msg = SCCrossFightInfo.newBuilder();
		msg.setAreaSwitchKey(humanObj.crossFightSwitchKey);
		msg.setMapSn(mapSn);
		msg.setFightType(ECrossFightType.valueOf(type));
		msg.setToken(token);
		msg.setIp(crossIp);
		msg.setPort(crossPort);
		msg.setTeam(ETeamType.valueOf(teamCamp));
		humanObj.sendMsg(msg.build());
	}
	@DistrMethod
	public void pvpFinishFight(long humanId, int type, int win, Message msg) {
		// 记录跨服战斗结束消息
		humanObj.crossFightFinishMsg = (SCTurnbasedFinish)msg;
		
		// 转发结束消息给玩家
		//humanObj.sendMsg(humanObj.crossPveFightFinishMsg);
		
		// 不转发，只通知客户端战斗结束消息，客户端处理剧情完毕后，由客户端发起end再处理结算返回
		SCTurnbasedFinish.Builder msgFinish = SCTurnbasedFinish.newBuilder();
		humanObj.sendMsg(msgFinish);
	}
	
	@DistrMethod
	public void pvpLeaveFight(long humanId, int type) {
		// 由客户端发就行了
//		switch(type) {
//		case ECrossFightType.FIGHT_INSTANCE_VALUE: {
//			InstanceManager.inst()._msg_CSInstLeave(humanObj);
//		}	break;
//		case ECrossFightType.FIGHT_COMPETE_VALUE: {
//			CompeteManager.inst()._msg_CSCompeteLeave(humanObj);
//		}	break;
//		case ECrossFightType.FIGHT_WORLD_BOSS_VALUE: {
//			InstWorldBossManager.inst()._msg_CSWorldBossLeave(humanObj);
//		}	break;
//		case ECrossFightType.FIGHT_TOWER_VALUE: {
//			TowerManager.inst()._msg_CSTowerLeave(humanObj);
//		}	break;
//		}
	}
	
	@DistrMethod
	public void replayRecordResult(long humanId, int result, int fightType, String token, 
			String crossIp, int crossPort, long stageId, int stageSn, int mapSn, RecordInfo record) {
		HumanManager.inst().createReplayStage(humanObj, result, fightType, token, 
				crossIp, crossPort, stageId, stageSn, mapSn, record);
	}

	@DistrMethod
	public void quickFightResult(int result,int type, HumanMirrorObject humanMirrorObj, Param param) {
		switch(type){
		case ECrossFightType.FIGHT_COMPETE_VALUE: {
			//竞技场快速战斗结果
			//boolean isWin = Utils.getParamValue(param, "isWin", false);
			//long recordId = Utils.getParamValue(param, "recordId", 0L);			
			//CompeteManager.inst().endCompete(result,humanObj, humanMirrorObj, isWin, recordId);
		}	break;
		}
	}
	//====================================公会相关===========================================
	/**
	 * 通知在线玩家入会成功
	 * @param result
	 */
	@DistrMethod
	public void guildJoin(boolean result, long guildId, int guildLv, String guildName, int isApply) {

//		if(humanObj.switchState != SwitchState.InStage){
//			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance(); 
//			prx.doHumanInCurrentThreadGuildJoin(humanObj.id, result, guildId, guildLv, guildName, isApply);
//			return;
//		}
		//移除申请列表
		humanObj.humanGlobalInfo.guildId = guildId;
		humanObj.humanGlobalInfo.guildName= guildName;//设置所属公会姓名
		humanObj.getHuman().setGuildId(guildId);//设置所属公会id
		humanObj.getHuman().setGuildName(guildName);
		humanObj.getHuman().setGuildLevel(guildLv);//设置所属公会等级
		HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
		pxy.updatGuildName(humanObj.id, guildName);
		// 目前加入公会没有属性加成
//		UnitManager.inst().propCalc(humanObj);// 重新计算属性及战力
        GuildManager.inst().addGuildSkillProp(humanObj);
		// 加入的人通知加入返回结果消息
		SCGuildJoinResult.Builder msg = SCGuildJoinResult.newBuilder();
		msg.setResult(result);// true成功；>false失败
		if (result) {
			msg.setResult(result);
			msg.setGuildId(guildId);
			msg.setIsApply(isApply);
		}
		humanObj.sendMsg(msg);
		port.returns("results", true);
	}
	/**
	 * 通知在线玩家退出公会了
	 */
	@DistrMethod
	public void guildLeave(boolean result, int isKickOut) {
//		if(humanObj.switchState != SwitchState.InStage){
//			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance(); 
//			prx.doHumanInCurrentThreadGuildLeave(humanObj.id, result, isKickOut);
//			return;
//		}else{
			Human human = humanObj.getHuman();
			human.setGuildId(0);//设置所属公会 
			human.setGuildLevel(0);//设置所属公会等级
			human.setGuildLeaveTime(Port.getTime());
			human.setGuildName("");
			// 目前加入公会没有属性加成
			UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.GuildSkill);
			Log.human.debug("guildLevel(); name={}, guildBeLong={}, unionId={}, guildContribute={}",human.getName(), human.getGuildId(), humanObj.humanGlobalInfo.guildId, human.getContribute());

			SCGuildLeaveResult.Builder msg = SCGuildLeaveResult.newBuilder();
			msg.setResult(result);
			if (result) {
				msg.setResult(result);
				msg.setPertainGuild(0);
				msg.setIsKickOut(isKickOut);
			}
			humanObj.sendMsg(msg);
			port.returns("results", true);
//		}
	}
	
	
	@DistrMethod
	public void guildCDRUp(boolean isCDR){
		
	}
	
	@DistrMethod
	public void noticeMainCity(Castellan castellan){
		// 根据isNotice来提示
		if (castellan.isIsNotice()) {
			return;
		}
		castellan.setIsNotice(true);
		
		String detail = "{" + EMailType.MailMaster_VALUE + "|" + castellan.getType() + "}";
		
		MailManager.inst().sendSysMail(castellan.getHumanId(), ParamManager.mailMark, detail,null);
		
		// 发送通知，成为城主
		Event.fire(EventKey.MainCityCastellan, "humanObj", humanObj, "castellanType", castellan.getType());
		
	}

    @DistrMethod
    public void syncGuildInstData(GuildInstData inst) {
	    GuildInstManager.inst().syncGuildInstData(humanObj, inst);
    }
}
