package game.worldsrv.instLootMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.protobuf.Message.Builder;

import core.Port;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;
import game.msg.Define.DMemberInfo;
import game.msg.Define.DProduce;
import game.msg.Define.ECostGoldType;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ELootMapGMTestType;
import game.msg.Define.ELootMapType;
import game.msg.Define.EModeType;
import game.msg.Define.EMoneyType;
import game.msg.Define.ETeamType;
import game.msg.MsgInstLootMap.SCDailyLootMapRevival;
import game.msg.MsgInstLootMap.SCLootMapPkEnd;
import game.msg.MsgInstLootMap.SCLootMapPkFight;
import game.msg.MsgInstLootMap.SCLootMapReadyEnter;
import game.msg.MsgInstLootMap.SCPVPLootMapSignUp;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.combat.types.CombatObjectPKHuman;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfCostGold;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfLootMap;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.fightParam.PKHumanParam;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.instLootMap.Room.InstLootMapSignUpRoom;
import game.worldsrv.instLootMap.Stage.StageObjectLootMap;
import game.worldsrv.instLootMap.Stage.StageObjectLootMapMultiple;
import game.worldsrv.instLootMap.Stage.StageObjectLootMapSingle;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.pk.PKHumanInfo;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamManager;

public class InstLootMapManager  extends ManagerBase {
	
	public static InstLootMapManager inst() {
		return inst(InstLootMapManager.class);
	}
	
	@Listener(EventKey.HumanLogin)
	private void _lisenter_HumanLogin(Param params){
		_lisenter_HumanLogout(params);
	}
	
	/**
	 * 玩家登出 互踢在这个事件之前 在关闭玩家连接之前 已经实现
	 * @param params
	 */
	@Listener(EventKey.HumanLogout)
	private void _lisenter_HumanLogout(Param params){
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		if (humanObj == null) {
			Log.lootMap.error("===_listener_StageHumanEnter humanObj is null");
			return;
		}
		// 不确定是否有报名 直接走service移除
		InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
		prx.humanSignOut(humanObj.id);
		//在单人抢夺本中  进行结算
		if(humanObj.stageObj instanceof StageObjectLootMapSingle){
			StageObjectLootMapSingle stageObj = (StageObjectLootMapSingle)humanObj.stageObj;
			stageObj.singleGameSettlement(humanObj.id);
		}
	}
	
	/**
	 * 监听玩家进入抢夺本事件
	 * @param params
	 */
	@Listener(EventKey.StageHumanEnter)
	public void _lisenter_LootMapEnter(Param params){
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		if (humanObj == null) {
			Log.lootMap.error("===_listener_StageHumanEnter humanObj is null");
			return;
		}
		//如果这个stage不是抢夺本内容 返回
		if (! (humanObj.stageObj instanceof StageObjectLootMap) ) {
			return;
		}
		//取得stage
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		//设置进入地图
 		if(stageObj.intoMap(humanObj) && stageObj.isMapFull()){
			stageObj.startGame();
		}
 		
 		//发布七日成就事件
 		int num = humanObj.cultureTimes.getJoinLootMap() +1 ;
 		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", num , 
 				"type", ActivitySevenTypeKey.Type_53);
	}
	
	/**
	 * 该时间段是否参加过
	 * @param humanObj
	 * @param actInstSn
	 * @return
	 */
	private boolean isJoinOnce(HumanObject humanObj, int actInstSn) {
		HumanExtInfo exInfo = humanObj.getHumanExtInfo();
		long lastJoinTime = exInfo.getLootMapMultipleJoinTime();
		long curTime = Port.getTime();
		if(Utils.isSameDay(lastJoinTime,curTime) == false){ // 不是同一天
			return false;
		}
		
		ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);//此处配置表肯定正确
		for(int i = 0;i < conf.openHour.length;i++){
			
			int hour = conf.openHour[i]; // 开启小时
			int minute = conf.openMinute[i]; // 开启分钟
			int totalMinute = conf.totalMinute[i]; // 该次开启的持续时间
			
			long openMinTime = Utils.getTimestampTodayAssign(hour,minute,0); // 获取开启时间
			long openMaxTime = openMinTime + totalMinute*Time.MIN; // 结束时间
	
			if(lastJoinTime >= openMinTime && lastJoinTime <= openMaxTime){ // 最后参与的时间在这个时间段里面
				return curTime >= openMinTime && curTime <= openMaxTime; // 现在时间也在这个时间段内
			}
		}
		
		return false;
	}
	
	/**
	 * 抢夺本pvp报名
	 */
	public void _msg_CSPVPLootMapSignUp(HumanObject humanObj,int actInstSn){
		
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeLootMultiple_VALUE, humanObj)) { // 还没解锁
			//Log.lootMap.debug(" ======= _msg_CSPVPLootMapSignUp isUnlock humanId = {}",humanObj.id);
			humanObj.sendSysMsg(560201);
			return;
		}
		
		if(humanObj.isInLootMapSignUp()){ // 已经在队列中 重新进行报名
			//Log.lootMap.debug(" ======= _msg_CSPVPLootMapSignUp humanObj.isInLootMapSignUp");
			resetSignUp(humanObj,actInstSn); // 重新报名
			return;
		}
		
		if(TeamManager.inst().canJoinActInst(humanObj,actInstSn) == false){ // 判断是否能够参加活动;
			ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);
			if(conf == null) return; // 获取活动配置
			ConfInstStage confInstStage = ConfInstStage.get(conf.instSn); // 获取Inst配置
			if(confInstStage == null) return; // 配置为空返回
			if(confInstStage.costItemSN!= EModeType.ModeLootMultiple_VALUE) return; // 不是多人密钥
			int costItemNumber = confInstStage.costItemNumber; // 取得消耗
			boolean ret = RewardHelper.canConsume(humanObj, confInstStage.costItemSN,costItemNumber);
			if(ret == false){
				humanObj.sendSysMsg(560202);
			}
			return;//这个肯定返回
		}
		
		if(isJoinOnce(humanObj, actInstSn)){ // 已经参加过一次
			int count = humanObj.getHumanExtInfo().getLootMapMultipleJoinCount();
			if (count >= ParamManager.lootMapMultipleJoinCount) {// 已达次数限制
				humanObj.sendSysMsg(560203);
				return;
			}
		} else {// 未参加过该时间段的活动，重置参加次数记录
			humanObj.getHumanExtInfo().setLootMapMultipleJoinCount(0);
		}
		
		InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance(); // 进入报名房间
		prx.intoSignUpRoom(humanObj.getHuman(),actInstSn);
		prx.listenResult(this::_result_intoSignUpRoom, "humanObj", humanObj,"actInstSn",actInstSn);
	}
	
	//intoSignUpRoom
	private void _result_intoSignUpRoom(Param results, Param context){
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null); // 操作的humanObj
		int actInstSn = Utils.getParamValue(context, "actInstSn", null); // 活动id
		if(humanObj == null){
			return;
		}
		InstLootMapSignUpRoom room = Utils.getParamValue(results, "room", null); // 返回房间
		ArrayList<HumanGlobalInfo> hgList = Utils.getParamValue(results, "hgList", null); // 返回的玩家组
		
		SCPVPLootMapSignUp.Builder msg = SCPVPLootMapSignUp.newBuilder(); //当参加成功
		for(int i = 0;i < hgList.size();i++){
			msg.addHumanList(getDMemberInfoBuilder(hgList.get(i)));// 玩家转为 DMemberInfo.Builder
		}
		msg.setCountdownTime(room.getCountdownTime());
		humanObj.sendMsg(msg);
		humanObj.setLootMapSignUpRoomId(room.roomId); // 设置房间 -> 离线可以做处理
		
		if(room.isFull()){ //当房间满了 准备开始 关闭报名房间 开启游戏房间
			SCLootMapReadyEnter.Builder builder = SCLootMapReadyEnter.newBuilder(); // 回执客户端 准备进入游戏 客户端发送 CSLootMapEnter
			builder.setMapType(ELootMapType.LootMapMultip);
			builder.setActInstSn(actInstSn);
			humanObj.sendMsg(builder);
		}
	}

	/**
	 * 将HumanGlobalInfo转成DMemberInfo.Builder
	 * @param hgi
	 * @return
	 */
	private DMemberInfo.Builder getDMemberInfoBuilder(HumanGlobalInfo hgi){
		DMemberInfo.Builder builder = DMemberInfo.newBuilder();
		builder.setId(hgi.id);
		builder.setName(hgi.name);
		builder.setCombat(hgi.combat);
		builder.setProfession(hgi.profession);
		builder.setSex(hgi.sex);
		builder.setModelSn(hgi.defaultModelSn);
		builder.setIsOnline(true);
		return builder;
	}
	
	/**
	 * 抢夺本pvp离开队列
	 */
	public void _msg_CSLeavePVPLootMapSignUp(HumanObject humanObj){
		InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
		prx.leaveSignUpRoom(humanObj.getHuman());
		prx.listenResult(this::_result_leaveSignUpRoom, "humanObj", humanObj);
	}
	
	//leaveSignUpRoom
	private void _result_leaveSignUpRoom(Param results, Param context){
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		humanObj.clearLootMapSignUp();
	}
	
	/**
	 * 当已经存在报名队列时候重新报名
	 * @param humanObj
	 */
	private void resetSignUp(HumanObject humanObj,int actInstSn){
		InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
		prx.leaveSignUpRoom(humanObj.getHuman());
		prx.listenResult(this::_result_resetSignUp, "humanObj", humanObj,"actInstSn",actInstSn);
	}
	
	private void _result_resetSignUp(Param results, Param context){
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		humanObj.clearLootMapSignUp();
		int actInstSn = Utils.getParamValue(context, "actInstSn", 0);
		_msg_CSPVPLootMapSignUp(humanObj,actInstSn);
	}
	
	/**
	 * 抢夺本pve报名
	 */
	public void _msg_CSPVELootMapSignUp(HumanObject humanObj,int actInstSn){
		
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeLootMapSingle_VALUE, humanObj)) {
			Log.lootMap.debug(" ======= _msg_CSPVELootMapSignUp isUnlock humanId = {}",humanObj.id);
			humanObj.sendSysMsg(560701);
			return;
		}
		
		//判断是否允许参加
		if(TeamManager.inst().canJoinActInst(humanObj,actInstSn) == false){
			Log.lootMap.debug("    ========   _msg_CSPVELootMapSignUp TeamManager.inst().canJoinActInst == false");
			humanObj.sendSysMsg(560702);
			return;
		}
		
		// 回执客户端 准备进入游戏 客户端发送 CSLootMapEnter
		SCLootMapReadyEnter.Builder builder = SCLootMapReadyEnter.newBuilder();
		builder.setMapType(ELootMapType.LootMapSingle);
		builder.setActInstSn(actInstSn);
		humanObj.sendMsg(builder);
	}
	
	/**
	 * 请求进入抢夺本
	 * @param humanObj
	 * @param mapType
	 */
	public void _msg_CSLootMapEnter(HumanObject humanObj, ELootMapType mapType, int actInstSn) {
		switch(mapType) {
			case LootMapSingle: {
				InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
				prx.enterSingle(humanObj.getHuman(), actInstSn);
			} break;
			case LootMapMultip: {
				InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
				prx.enterMultip(humanObj.id, actInstSn);
			} break;
			default:
				break;
		}
	}
	
	/**
	 * 触发pk
	 * @param humanObj 发起者
	 */
	public void _msg_CSLootMapPkFight(HumanObject humanObj, long fightHumanId) {
		long stageId = 0;
		StageObjectLootMapMultiple stageObj = null;
		if (humanObj.stageObj instanceof StageObjectLootMapMultiple) {
			stageObj = (StageObjectLootMapMultiple) (humanObj.stageObj);
			if (stageObj.fightType != ECrossFightType.FIGHT_LOOTMAP_MULTIPLE_VALUE) {
				Log.fight.error("===error in fightType={}", stageObj.fightType);
				return;
			}
			stageId = stageObj.stageId;
		} else {
			Log.fight.error("===玩家所在地图不对!");
			return;
		}
		// 判断发起方是否满足挑战
		stageObj.isPkErr(humanObj.getHumanId(), true);
		// 判断被挑战方是否满足挑战
		stageObj.isPkErr(fightHumanId, false);
				
		// 判断副本配置是否合法
		int stageSn = ParamManager.pvpMapSn; 
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.stageCommon.error("===进入发起切磋错误： ConfInstStage.sn={}", stageSn);
			return;
		}
		// 被挑战方的humanObj
		HumanObject targetHumanObj = humanObj.stageObj.getHumanObjs().get(fightHumanId);
		if (targetHumanObj == null) {
			Log.stageCommon.error("===进入发起切磋错误： fightHumanId={}不在场景中", fightHumanId);
			return;
		}
		// 双方pk数据
		PKHumanInfo pkHumanInfo1 = stageObj.getPKHumanInfo(humanObj.getHumanId());
		PKHumanInfo pkHumanInfo2 = stageObj.getPKHumanInfo(targetHumanObj.getHumanId());
		if (pkHumanInfo1 == null || pkHumanInfo2 == null) {
			Log.stageCommon.error("=== 敌方数据不存在 ===");
			return;
		}
		// 设置双方挑战信息/状态修改
		stageObj.sendPkStste(humanObj.getHumanId(), fightHumanId, true);
		
		int mapSn = confInstStage.mapSN;
		// 返回战斗请求结果给自己，通知客户端倒计时后进入PK场景
		SCLootMapPkFight.Builder msgFight = SCLootMapPkFight.newBuilder();
		msgFight.setResultCode(0);
		msgFight.setTriggerHumanId(humanObj.getHumanId());
		msgFight.setTargetHumanId(fightHumanId);
		humanObj.sendMsg(msgFight);
		// 返回战斗请求结果给对方，通知客户端倒计时后进入PK场景
		targetHumanObj.sendMsg(msgFight);
		// 如果不在同一场景里，则要用这个
//		HumanGlobalServiceProxy proxy = HumanGlobalServiceProxy.newInstance();
//		proxy.sendMsg(fightHumanId, msgFight.build());
		
		// 本场PVP的参数信息
		PKHumanParam pkHumanParam = new PKHumanParam(pkHumanInfo1, pkHumanInfo2);
		Param param = new Param(HumanMirrorObject.PKHumanParam, pkHumanParam);
		
		CombatObject combatObj = new CombatObjectPKHuman(humanObj.stageObj.getPort(), humanObj.stageObj, 
				stageSn, mapSn, ECrossFightType.FIGHT_LOOTMAP_MULTIPLE_VALUE, param);
		humanObj.stageObj.combatObjMap.put(humanObj.getHumanId(), combatObj);
		// 记录两个玩家对应的战斗id
		humanObj.stageObj.humanCombatObjMap.put(humanObj.getHumanId(), humanObj.getHumanId());
		humanObj.stageObj.humanCombatObjMap.put(targetHumanObj.getHumanId(), humanObj.getHumanId());
		
		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_LOOTMAP_MULTIPLE, mapSn, stageId);
		// 发送给对手
		targetHumanObj.sendFightInfo(ETeamType.Team2, ECrossFightType.FIGHT_LOOTMAP_MULTIPLE, mapSn, stageId);
		// 如果不在同一场景里，则要用这个
//		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
//		prx.sendFightInfo(fightHumanId, ETeamType.Team2, ECrossFightType.FIGHT_LOOTMAP_MULTIPLE, mapSn, stageId);
	}
	
	/**
	 * 离开PK
	 * @param humanObj
	 */
	public void _msg_CSLootMapPkLeave(HumanObject humanObj) {
//		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectLootMapMultiple)) {
//			Log.game.error(" ===离开失败，不在多人抢夺本中 ===");
//			return;
//		}
//		
//		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
	}

	/**
	 * 结算PK
	 * @param humanObj
	 */
	public void _msg_CSLootMapPkEnd(HumanObject humanObj, long winHumanId, long loseHumanId, long triggerId) {
		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectLootMapMultiple)) {
			Log.game.error(" ===结算失败，不在多人抢夺本中 ===");
			return;
		}
		
		StageObjectLootMapMultiple stageObj = (StageObjectLootMapMultiple) humanObj.stageObj;
		if (humanObj.getHumanId() == triggerId) {
			// 广播PK结束
			stageObj.battleEnd(winHumanId, loseHumanId);
		}
		
		// 返回地图操作（处理结算扣血）
		stageObj.backMap(humanObj.getHumanId(), winHumanId == humanObj.getHumanId());
		
		// 获取结算的道具
		Map<Integer, Integer> produceMap = null;
		if(winHumanId == triggerId){
			produceMap = stageObj.getResultItem(winHumanId, loseHumanId);
		}
		
		SCLootMapPkEnd.Builder msg = SCLootMapPkEnd.newBuilder();
		msg.setWinHumanId(winHumanId);
		msg.setLoseHumanId(loseHumanId);
		msg.setTriggerId(triggerId);
		if (produceMap != null) {
			for (Entry<Integer, Integer> entry : produceMap.entrySet()) {
				DProduce.Builder produce = msg.addProduceListBuilder();
				produce.setSn(entry.getKey());
				produce.setNum(entry.getValue());
			}
		}
		// 广播给战场中的玩家
		StageManager.inst().sendMsgToHumans(msg, stageObj.getHumanObjs().values());
	}
	
	/**
	 * 载入完毕 请求数据
	 * @param humanObj
	 */
	public void _msg_CSLootMapGameEnter(HumanObject humanObj){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		sendGameData(humanObj);
	}
	
	private void sendGameData(HumanObject humanObj){
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		if(stageObj == null) 
			return;
		
		stageObj.sendGameEnterLevelData(humanObj.id);
	}
	
	/**
	 * 玩家是否播放动作
	 * @param humanObj
	 * @param isPlayMove
	 */
	public void _msg_CSLootMapPlayMove(HumanObject humanObj,boolean isPlayMove){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		stageObj.humanPlayMove(humanObj.id,isPlayMove);
	}
	/**
	 * 玩家移动 x,y 
	 */
	public void _msg_CSLootMapMove(HumanObject humanObj,int x,int y){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		stageObj.humanMove(humanObj.id,x,y);
	}
	
	/**
	 * 打开一个地砖
	 * @param humanObj
	 * @param x
	 * @param y
	 */
	public void _msg_CSOpenFloor(HumanObject humanObj,int x,int y){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		stageObj.openFloor(humanObj.getHumanId(),x,y);
	}
	
	/**
	 * 触发事件
	 * @param humanObj
	 * @param eventId
	 * @param x
	 * @param y
	 */
	public void _msg_CSTriggerEvent(HumanObject humanObj,int eventId,int x,int y){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		//通过stage判断是否是可以触发奖励
		if(stageObj.isTriggerEvent(eventId, x, y)){
			stageObj.triggerEvent(humanObj.id, eventId);
		}else{
			Log.lootMap.debug("   ========   _msg_CSTriggerEvent isTriggerEvent false,huamanId = {}",humanObj.id);
		}
	}
	
	//获取一个地图
	public int getRandomLootMapConfSn(int mapType,int level){
		List<Integer> snArray = new ArrayList<Integer>();
		List<Integer> weightArray = new ArrayList<Integer>();
		for(ConfLootMap conf : ConfLootMap.findAll()){
			if(conf.type == mapType && conf.level == level){
				snArray.add(conf.sn);
				weightArray.add(conf.mapWeight);
			}
		}
		
		if(snArray.size() == 0){
			Log.lootMap.error("===getLootMapGroup size == 0");
			return 0;
		}
		
		int ramdomIndex = Utils.getRandRange(weightArray);
		if(ramdomIndex <= 0 && ramdomIndex >= weightArray.size()){
			Log.lootMap.error("===getLootMapGroup ramdomIndex is err");
			return 0;
		}
		
		return  snArray.get(ramdomIndex);
	}
	
	
	/**
	 * 玩家攻击行为
	 * @param humanObj
	 * @param evetId
	 * @param x
	 * @param y
	 */
	public void _msg_CSLootMapAttackMonster(HumanObject humanObj,int evetId,int x,int y){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		stageObj.attackMonster(humanObj.getHumanId(),evetId,x,y);
	}
	
	/**
	 * 玩家复活
	 * @param humanObj
	 */
	public void _msg_CSDailyLootMapRevival(HumanObject humanObj){
		if (!(humanObj.stageObj instanceof StageObjectLootMapMultiple)) {
			return;
		}
		StageObjectLootMapMultiple stageObj = (StageObjectLootMapMultiple)humanObj.stageObj;
		if(stageObj.isHumanDead(humanObj.getHumanId())){
			int num = 0; // 本次购买需要的gold
			Human human = humanObj.getHuman(); // 取得玩家
			int numBuyed = human.getDailyLootMapRevivalBuyNum(); //取得已经购买次数
			++numBuyed;
			ConfCostGold confCostGold = ConfCostGold.get(ECostGoldType.lootMapReviveCost_VALUE); // 取得购买配置表
			int[] countArr = confCostGold.count; // 购买次数组
			int[] costGoldNumArr = confCostGold.costGoldNum; // 购买花费组
			
			for(int i = 0;i < countArr.length;i++){ // 取得该次的number
				int count = countArr[i];
				num = costGoldNumArr[i];
				if(count == numBuyed){
					break;
				}
			}
			
			if(RewardHelper.checkAndConsume(humanObj,EMoneyType.gold_VALUE,num,LogSysModType.LootMapRevival)){
				stageObj.humanRevival(humanObj.getHumanId()); // 玩家复活
				human.setDailyLootMapRevivalBuyNum(numBuyed); // 设置购买次数
				SCDailyLootMapRevival.Builder builder = SCDailyLootMapRevival.newBuilder(); // 回执 玩家复活
				builder.setNumBuyed(numBuyed);
				sendHuman(builder, humanObj);
			}
		}else{
			Log.lootMap.debug("===_msg_CSLootMapHumanRevival humanObj is not dead");
		}
	}
	
	/**
	 * 玩家使用技能
	 * @param humanObj
	 */
	public void _msg_CSLootMapUseSkill(HumanObject humanObj){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		stageObj.humanUseSkill(humanObj.getHumanId());
	}
	
	/**
	 * 退出玩法
	 * @param humanObj
	 */
	public void _msg_CSLootMapOut(HumanObject humanObj){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		stageObj.humanOut(humanObj.id);
	}
	
	/**
	 * 退出玩法
	 * @param humanId
	 */
	public void humanOut(long humanId){
		InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
		prx.humanOut(humanId);
	}
	
	/**
	 * 关闭一个房间
	 * @param stageId
	 */
	public void closeGame(long stageId){
		InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
		prx.rmvGameStage(stageId);
	}

	/**
	 * 准备进入下一层
	 * @param humanObj
	 */
	public void _msg_CSLootMapReadyEnterDoor(HumanObject humanObj,int eventId){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		stageObj.readyEnterDoor(humanObj.id,eventId);
	}
	
	/**
	 * 进入下一层
	 * @param humanObj
	 */
	public void _msg_CSLootMapEnterDoor(HumanObject humanObj,int eventId,int x,int y){
		if (!(process_leaveStageToCutConn(humanObj))) {
			return;
		}
		StageObjectLootMap stageObj = (StageObjectLootMap)humanObj.stageObj;
		stageObj.enterDoor(humanObj.id,eventId,x,y);
	}
	
	/**
	 * 请求单人结束
	 * @param humanObj
	 */
	public void _msg_CSLootMapSingleEnd(HumanObject humanObj){
		if (!(humanObj.stageObj instanceof StageObjectLootMapSingle)) {
			return;
		}
		StageObjectLootMapSingle stageObj = (StageObjectLootMapSingle)humanObj.stageObj;
		stageObj.singleGameSettlement(humanObj.id);
	}
	
	/**
	 * 进入游戏 请求时间 倒计时
	 * @param humanObj
	 */
	public void _msg_CSLootMapGameTime(HumanObject humanObj){
		if (!(humanObj.stageObj instanceof StageObjectLootMapMultiple)) {
			return;
		}
		StageObjectLootMapMultiple stageObj = (StageObjectLootMapMultiple)humanObj.stageObj;
		stageObj.sendGameTime(humanObj.id);
	}
	
	
	// 是否扣除成功
	public boolean consumeActivityCostItem(HumanObject humanObj,int costItemSn,int costNum){
		if(RewardHelper.checkAndConsume(humanObj, costItemSn,costNum, LogSysModType.Inst)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 退出战斗
	 * @param humanObj
	 */
	public void _msg_CSLootMapBackMap(HumanObject humanObj,boolean isWin){
		if (humanObj.stageObj instanceof StageObjectLootMapMultiple) { //如果这个stage不是抢夺本内容
			StageObjectLootMapMultiple stageObj = (StageObjectLootMapMultiple)humanObj.stageObj;
			stageObj.backMap(humanObj.getHumanId(), isWin);
		}
	}
	
	/**
	 * 抢夺本测试
	 * @param humanObj
	 */
	public void _msg_CSLootMapGMTest(HumanObject humanObj,ELootMapGMTestType type){
//		switch(type){
//			case LootMapGMTest_protected:{
//				SCLootMapProtectState.Builder builder = SCLootMapProtectState.newBuilder();
//				builder.setHumanId(humanObj.getHumanId());
//				builder.setIsProtect(true);
//				InstLootMapManager.inst().sendAllHuman(builder, humanObj.stageObj, new Vector2D(0,0));
//			}break;
//			case LootMapGMTest_unProtected:{
//				SCLootMapProtectState.Builder builder = SCLootMapProtectState.newBuilder();
//				builder.setHumanId(humanObj.getHumanId());
//				builder.setIsProtect(false);
//				InstLootMapManager.inst().sendAllHuman(builder, humanObj.stageObj, new Vector2D(0,0));
//			}break;
//			case LootMapGMTest_pk:{
//				SCLootMapPkState.Builder builder = SCLootMapPkState.newBuilder();
//				builder.addHumanIdList(humanObj.getHumanId());
//				builder.setIsPk(true);
//				InstLootMapManager.inst().sendAllHuman(builder, humanObj.stageObj, new Vector2D(0,0));
//			}break;
//			case LootMapGMTest_unpk:{
//				SCLootMapPkState.Builder builder = SCLootMapPkState.newBuilder();
//				builder.addHumanIdList(humanObj.getHumanId());
//				builder.setIsPk(false);
//				InstLootMapManager.inst().sendAllHuman(builder, humanObj.stageObj, new Vector2D(0,0));
//			}break;
//			case LootMapGmTest_trigetPk:
//			case LootMapGmTest_targetPk:{
//				SCLootMapPk.Builder builder = SCLootMapPk.newBuilder();
//				builder.setTargetHumanId(humanObj.getHumanId());
//				builder.setTriggerHumanId(humanObj.getHumanId());
//				InstLootMapManager.inst().SendHuman(builder, humanObj);
//			}break;
//			case LootMapGmTest_pkRunAway:{
//				SCLootMapCanclePk.Builder builder = SCLootMapCanclePk.newBuilder();
//				builder.setHumanId(humanObj.getHumanId());
//				InstLootMapManager.inst().SendHuman(builder, humanObj);
//			}break;
//			case LootMapGmTest_killNotific:{
//				
//				SCLootMapKill.Builder builder = SCLootMapKill.newBuilder();
//				DMemberInfo.Builder momberInfo = DMemberInfo.newBuilder();
//				momberInfo.setId(humanObj.getHumanId());
//				momberInfo.setName(humanObj.getHuman().getName());
//				momberInfo.setModelSn(humanObj.getHuman().getModelSn());
//				builder.setBeKillerInfo(momberInfo);
//				builder.setKillerInfo(momberInfo);
//				
//				Collection<ConfLootMapKillHonor> cc = ConfLootMapKillHonor.findAll();
//				int len = cc.size();
//				int randomIndex = ((int)(Math.random()*len));
//				int tempIndex = 0;
//				int confSn = 0;
//				for(ConfLootMapKillHonor conf : cc){
//					if(tempIndex == randomIndex){
//						confSn = conf.sn;
//						break;
//					}
//					tempIndex++;
//				}
//				builder.setHornorSn(confSn);
//				builder.setScore(100);
//				if(((int)(Math.random()*100))%2 ==0){
//					builder.setRewardScore(99);
//				}else{
//					builder.setRewardScore(0);
//				}
//				builder.setKillNumber((int)(Math.random()*20));
//				InstLootMapManager.inst().sendAllHuman(builder, humanObj.stageObj, new Vector2D(0,0));
//			}break;
//			default:
//				break;
//		}
	}
	
	
	/**
	 * 发送给全图玩家消息 排除这个humanId的玩家
	 * @param builder
	 */
	public void sendAllHuman(Builder builder,Long humanId,StageObject stageObj){
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			if(humanId == humanObj.id){
				continue;
			}
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家
			if (!(process_leaveStageToCutConn(humanObj))) { //如果这个stage不是抢夺本内容
				continue;
			}
			humanObj.sendMsg(builder);
		}
	}
	

	/**
	 * 发送给全图玩家消息
	 * @param builder
	 */
	public void sendAllHuman(Builder builder,StageObject stageObj){
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家
			if (!(process_leaveStageToCutConn(humanObj))) { //如果这个stage不是抢夺本内容
				continue;
			}
			humanObj.sendMsg(builder);
		}
	}
	
	/**
	 * 发送给单人消息
	 * @param humanObj
	 * @param builder
	 */
	public void sendHuman(Builder builder, HumanObject humanObj) {
		if (humanObj == null) {
			Log.lootMap.error("===SendHuman humanObj is null");
			return;
		}
		if (!(process_leaveStageToCutConn(humanObj))) { //如果这个stage不是抢夺本内容 返回
			return;
		}
		humanObj.sendMsg(builder);
	}
	
	/**
	 * 不在抢夺本中，则强行踢下线
	 */
	public boolean process_leaveStageToCutConn(HumanObject humanObj) {
		if (humanObj.stageObj instanceof StageObjectLootMap) {
			return true;
		}
		// 踢出玩家
		HumanGlobalServiceProxy hgPrx = HumanGlobalServiceProxy.newInstance();
		hgPrx.kick(humanObj.getHumanId(), 19); // 19	重连失败，请重新登录！
		return false;
	}
	
}
