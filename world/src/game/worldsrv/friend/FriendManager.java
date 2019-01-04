package game.worldsrv.friend;

import game.msg.Define.DFriendInfo;
import game.msg.Define.DPartnerBriefInfo;
import game.msg.Define.DServantBriefInfo;
import game.msg.Define.EFriendType;
import game.msg.Define.EMoneyType;
import game.msg.MsgFriend.SCAcceptFriend;
import game.msg.MsgFriend.SCFriendInfo;
import game.msg.MsgFriend.SCFriendList;
import game.msg.MsgFriend.SCGiveFriendAc;
import game.msg.MsgFriend.SCQueryCharacter;
import game.msg.MsgFriend.SCReceFriendAc;
import game.msg.MsgFriend.SCRecommendFriend;
import game.msg.MsgFriend.SCRefuseFriend;
import game.msg.MsgFriend.SCRemoveBlackList;
import game.msg.MsgFriend.SCRemoveFriend;
import game.msg.MsgFriend.SCRequestFriend;
import game.msg.MsgFriend.SCSearchFriend;
import game.msg.MsgFriend.SCToBlackList;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.entity.Friend;
import game.worldsrv.entity.FriendObject;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.ItemBody;
import game.worldsrv.entity.Partner;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.humanSkill.SkillGodsJSON;
import game.worldsrv.humanSkill.SkillJSON;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.offline.OffilineGlobalServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.Port;
import core.Record;
import core.RecordTransient;
import core.db.DBKey;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;


/**
 * 好友管理器
 */
public class FriendManager extends ManagerBase{
	
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static FriendManager inst() {
		return inst(FriendManager.class);
	}
	
	/**
	 * 读取玩家的好友信息
	 * @param param
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB db = DB.newInstance(Friend.tableName);
		db.findBy(false, Friend.K.HumanId, humanObj.id);
		db.listenResult(this::_result_loadFriendData, "humanObj", humanObj);
		// 玩家数据加载开始一个
//		Event.fire(EventKey.HumanDataLoadBeginOne, "humanObj", humanObj);
	}
	private void _result_loadFriendData(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<Record> records = results.get();
		for (Record record : records) {
			if (record == null) {
				continue;
			}
			Friend friend = new Friend(record);
			humanObj.friendInfo.addInfoMap(friend);
		}
		if (humanObj.isDailyFirstLogin) {
			humanObj.extInfo.setFriendGiveTimes(0);
			humanObj.extInfo.setFriendReceiveTimes(0);
		}
		
		FriendServiceProxy proxy = FriendServiceProxy.newInstance();
		proxy.loginFinishOnline(humanObj.getHuman(), 
				humanObj.friendInfo.friendMap.keySet(), humanObj.friendInfo.applyMap.keySet());
		proxy.listenResult(this::_listen_LoadFriendObjs, context);
		// 玩家数据加载完成一个
//		Event.fire(EventKey.HumanDataLoadFinishOne, "humanObj", humanObj);
	}
	
	private void _listen_LoadFriendObjs(Param param, Param context) {
		HumanObject humanObj = context.get("humanObj");
		onSCFriendList(humanObj);
	}
	
	/******************************************玩家处理************************************/
	// 玩家这辈子首次登陆，将对象持久化到服务端
	@Listener(value = EventKey.HumanFirstLogin)
	public void friendLogin(Param param){
		HumanObject humanObj = param.get("humanObj");
		FriendServiceProxy.newInstance().createFriend(humanObj.getHuman());
	}
	
	// 玩家离线的时候 将 好友数和申请数同步到玩家身上
	@Listener(value = EventKey.HumanLogout)
	public void onHumanLogout(Param param) {
		HumanObject humanObj = param.get("humanObj");
		FriendInfo info = humanObj.friendInfo;
		FriendServiceProxy.newInstance().synApplyFriend(humanObj.getHumanId(),info.applyMap.size(),info.friendMap.size(),false);
	}
	
	// TODO  玩家信息更改
	@Listener({EventKey.VipLvChange,EventKey.HumanLvUp,EventKey.HumanNameChange,EventKey.HumanTotalCombatChange})
	//	,EventKey.DRIVER_SHOW_CHANGER})
	public void humanChange(Param param){
		HumanObject humanObj = param.get("humanObj");
		if (humanObj == null) {
			return;
		}
		Map<Long,Friend> friendMap = humanObj.friendInfo.friendMap;
		List<Long> ids = new ArrayList<>(friendMap.keySet());
		int combat = HumanManager.inst().getLineUpCombat(humanObj);
		FriendServiceProxy.newInstance().changeObject(humanObj.getHuman(),ids,combat);
	}
	
	/************************************获取好友***********************************************/
	public void onSCFriendList(HumanObject humanObj){
		FriendInfo info = humanObj.friendInfo;
		Set<Long> FriendId = new HashSet<>();
		FriendId.addAll(info.getAllMap().keySet());
		FriendServiceProxy prx = FriendServiceProxy.newInstance();
		prx.getListFriend(FriendId);
		prx.listenResult(this::_result_getFriendAll, "humanObj",humanObj);
	}
	private void _result_getFriendAll(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		Map<Long,FriendObject> infoMap = results.get("recordMap");
		SCFriendList.Builder msg = SCFriendList.newBuilder();
		// 获取玩家信息
		Map<Long,Friend> friendMap = humanObj.friendInfo.getAllMap();
		for (Friend f : friendMap.values()) {
			FriendObject friendObj = infoMap.get(f.getFId());
			if (friendObj == null) {
				continue;
			}
			DFriendInfo.Builder dMsg = getDFriendInfo(friendObj, f);
			switch (f.getType()) {
			case FriendType.Apply:
				msg.addApplyList(dMsg);
				break;
			case FriendType.Friend:
				msg.addFriendList(dMsg);
				break;
			case FriendType.Black:
				msg.addBlackList(dMsg);
				break;
			default:
				break;
			}
		}
		msg.setYetGetNum(humanObj.extInfo.getFriendReceiveTimes());
		msg.setYetGiveNum(humanObj.extInfo.getFriendGiveTimes());
		humanObj.sendMsg(msg);
	}
	/**
	 * 每日重置已领取次数跟已赠送次数
	 * @param param
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			humanObj.extInfo.setFriendGiveTimes(0);
			humanObj.extInfo.setFriendReceiveTimes(0);
		}
	}
	
	public DFriendInfo.Builder getDFriendInfo(FriendObject friendObj,Friend friend){
		DFriendInfo.Builder msg = createMsg(friendObj);
		int actStage = 0;
		// 获得领取情况
		List<Long> getList = Utils.strToLongList(friend.getGet());
		if (getList.size() == 2) {
			// 获得今天数据
			if (Utils.getDaysBetween(Port.getTime(), getList.get(0)) == 0) {
				actStage = getList.get(1).intValue();
			}
		}
		//判断是否是已经赠送了体力
		if (Utils.getDaysBetween(Port.getTime(), friend.getGive()) == 0) {
			actStage = actStage + 10;
		}
		msg.setActStage(actStage);
		return msg;
	}
	
	private DFriendInfo.Builder createMsg(FriendObject record){
		DFriendInfo.Builder msg = DFriendInfo.newBuilder();
		msg.setHumanId(record.getId());
		msg.setName(record.getName());
		msg.setLevel(record.getLv());
		msg.setCombat(record.getCombat());
		msg.setOnline(record.isLine());
		msg.setVip(record.getVipLv());
		msg.setHeadSn(record.getHeadSn());
		msg.setMountSn(record.getMountSn());
		msg.setModelSn(record.getModelSn());
		return msg;
	}
	
	private DFriendInfo.Builder createMsg(HumanGlobalInfo r){
		DFriendInfo.Builder msg = DFriendInfo.newBuilder();
		msg.setHumanId(r.id);
		msg.setName(r.name);
		msg.setLevel(r.level);
		msg.setCombat(r.combat);
		msg.setOnline(true);
		msg.setVip(r.vipLv);
		msg.setHeadSn(r.headSn);
		msg.setMountSn(r.mountSn);
		msg.setModelSn(r.defaultModelSn);
		return msg;
	}
	
	private DFriendInfo.Builder createMsg(RecordTransient record){
		DFriendInfo.Builder msg = DFriendInfo.newBuilder();
		msg.setHumanId(record.get(Human.K.id));
		msg.setName(record.get(Human.K.Name));
		msg.setLevel(record.get(Human.K.Level));
		msg.setCombat(record.get(Human.K.Combat));
		msg.setOnline(true);
		msg.setVip(record.get(Human.K.VipLevel));
		msg.setHeadSn(record.get(Human.K.HeadSn));
		msg.setMountSn(record.get(Human.K.MountSn));
		msg.setModelSn(record.get(Human.K.ModelSn));
		return msg;
	}
	/***********************************推荐搜索好友*************************************************/
	/**
	 * 查询好友 模糊查找
	 * 
	 * @param humanObj
	 */
	public void onCSSearchFriend(HumanObject humanObj, String name) {
		// 要查询的列
		List<String> listCol = new ArrayList<>();
		listCol.add(Human.K.Name);
		listCol.add(Human.K.Level);
		listCol.add(Human.K.Combat);
		listCol.add(Human.K.id);
		listCol.add(Human.K.ModelSn);
		listCol.add(Human.K.VipLevel);
		listCol.add(Human.K.HeadSn);
		listCol.add(Human.K.MountSn); 
		listCol.add(Human.K.ModelSn); 
		
		String whereSql = Utils.createStr(" where name like'%{}%' or showId like '%{}%'", name, name);
		if(name.length()<6){
			whereSql = Utils.createStr(" where name like'%{}%'", name);
		}
		DB db = DB.newInstance(Human.tableName);
		db.findByQuery(false, whereSql, DBKey.COLUMN, listCol);
		db.listenResult(this::_result_searchFriend, "humanObj", humanObj);
	}

	public void _result_searchFriend(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");

		SCSearchFriend.Builder msg = SCSearchFriend.newBuilder();
		// 查询失败，返回错误提示
		List<RecordTransient> list = results.get();
		if (list == null || list.size() == 0) {
			// Inform.sendInform(humanObj.id, 2017);
		}else{
			for (RecordTransient record : list) {
				msg.addInfos(createMsg(record));
			}
		}
		humanObj.sendMsg(msg);// 应答客户端
	}
	//推荐好友
	public void onCSRecommendFriend(HumanObject humanObj){
		FriendInfo info = humanObj.friendInfo;
		// 获取所有的好友 申请过的 就不推荐了
		List<Long> getList = new ArrayList<>(info.getFriendAndBlack().keySet());
		getList.add(humanObj.id);
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getRecommendNewUsers(humanObj.getHumanId(), getList);
		prx.listenResult(this::_result_recommendFriend, "humanObj",humanObj);
	}
	private void _result_recommendFriend(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		List<HumanGlobalInfo> recommendList = results.get("userList");
		//System.out.println("好友推荐数量："+recommendList.size());
		SCRecommendFriend.Builder msg = SCRecommendFriend.newBuilder();
		for (HumanGlobalInfo r : recommendList) {
			msg.addInfos(createMsg(r));
		}
		humanObj.sendMsg(msg);
	}
	/*************************************好友申请*******************************************/
	/**
	 * 发送好友请求
	 * @param humanObj
	 * @param toHumanIds
	 */
	public void requestFriend(HumanObject humanObj, List<Long> toHumanIds) {
		FriendInfo info = humanObj.friendInfo;
		SCRequestFriend.Builder msg = SCRequestFriend.newBuilder();
		if (info.friendMap.size() >= ParamManager.friendsUpperLimit) {
			msg.setResult(false);
			humanObj.sendSysMsg(340301); //好友列表已经达到最大上限
			return;
		}
		for (long toHumanId : toHumanIds) {
			// 是否等于自己
			if (toHumanId == humanObj.getHumanId()) {
				// Inform.user(humanObj.id, Inform.提示操作, "申请人不能是自己");
				msg.setResult(false);
				humanObj.sendSysMsg(340302); //申请人不能是自己
				return;
			}
			// 好友名单中
			if(info.friendMap.containsKey(toHumanId)){
				msg.setResult(false);
				humanObj.sendSysMsg(340303); //该玩家已经是您的好友
				return;
			}
		}
		FriendServiceProxy prx = FriendServiceProxy.newInstance();
		prx.getListFriend(new HashSet<>(toHumanIds));
		prx.listenResult(this::_result_requestFriend, "humanObj",humanObj);
	}
	public void _result_requestFriend(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		Map<Long,FriendObject> recordMap = results.get("recordMap");//好友对象map
		boolean isResult = false;
//		ConfGlobalParam confFriendMax = ConfGlobalParam.get(24001);// 我的好友上限数量
//		ConfGlobalParam confApplyMax = ConfGlobalParam.get(24002);// 申请列表上限数量
		for (FriendObject record : recordMap.values()) {
			// 黑名单中存在玩家 要先删掉黑名单中的玩家
			if (humanObj.friendInfo.blackMap.containsKey(record.getId())) {
//				disposeFriend(FriendType.RemoveBlack, humanObj, record.getId(), Port.getTime());
				onCSRemoveFriend(humanObj, record.getId(), false);
			}
			if (record.getApplyNum() >= ParamManager.friendsApplyLimit) {
				humanObj.sendSysMsg(340401); //该玩家的申请数达到上限
				continue;
			}else if (record.getFriendNum() >= ParamManager.friendsUpperLimit) {//该玩家的好友列表达到上限
				humanObj.sendSysMsg(340402);//该玩家的好友列表达到上限
				continue;
			}else{
				isResult = true;
				HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
				prx.disposeHumanFriend(humanObj.id, FriendType.Apply, Port.getTime(), record.getId());
			}
		}
		SCRequestFriend.Builder msg = SCRequestFriend.newBuilder();
		msg.addAllHumanIds(recordMap.keySet());
		msg.setResult(isResult);
		humanObj.sendMsg(msg);
		return;
	}
	/**********************************拒绝与同意***********************************/
	/**
	 * 拒绝成为好友
	 * @param humanObj
	 * @param humanIds
	 */
	public void onCSRefuseFriend(HumanObject humanObj, List<Long> humanIds){
		FriendInfo info = humanObj.friendInfo;
		if (humanIds.isEmpty()) {
			humanIds.addAll(info.getApply().keySet());
		}
		SCRefuseFriend.Builder msg = SCRefuseFriend.newBuilder();
		long time = Port.getTime();
		for (long humanId : humanIds) {
// TODO			Inform.sendInform(humanId, 2012, humanObj.name);
			// 判断时 如果存在体力关系 先放在删除
			disposeFriend(FriendType.Refuse, humanObj, humanId, time);
			// 直接默认完成
			msg.addHumanIds(humanId);
		}
		msg.setResult(true);
		humanObj.sendMsg(msg);
	}
	
	/***
	 * 同意加为好友
	 * @param humanObj
	 * @param humanIds
	 */
	public void onCSAcceptFriend(HumanObject humanObj, List<Long> humanIds){
		if (humanIds.isEmpty()) {
			humanIds.addAll(humanObj.friendInfo.getApply().keySet());
		}
		FriendServiceProxy prx = FriendServiceProxy.newInstance();
		prx.getListFriend(new HashSet<>(humanIds));
		prx.listenResult(this::_result_FriendAccept,"humanObj",humanObj);
	}
	
	private void _result_FriendAccept(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		Map<Long,FriendObject> recordMap = results.get("recordMap");
		long time = Port.getTime();
		SCAcceptFriend.Builder msg = SCAcceptFriend.newBuilder();
		//ConfGlobalParam confFriendMax = ConfGlobalParam.get(24001);// 我的好友上限数量
		boolean result = false;
		for (FriendObject object : recordMap.values()) {
			if (humanObj.friendInfo.friendMap.size() >= ParamManager.friendsUpperLimit) {
				humanObj.sendSysMsg(340304);//您的好友数已达上限
				break;
			}
			if (object.getFriendNum() >= ParamManager.friendsUpperLimit) {
				humanObj.sendSysMsg(340305);//对方的好友数已达上限
				continue;
			}
			Friend friend = disposeFriend(FriendType.Accept, humanObj, object, time);
			if (friend != null) {//添加好友
				result = true;
				msg.addHumanIds(object.getId());
				HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
				prx.disposeHumanFriend(humanObj.id, FriendType.Friend, time, object.getId());
			}
		}
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}
	/**********************************删除与拉黑与删黑********************************/
	/**
	 * 删除好友
	 * @param humanObj
	 * @param humanId
	 */
	public void onCSRemoveFriend(HumanObject humanObj, long humanId,boolean isRemove){
		long time = Port.getTime();
		// 默认移除黑名单
		int type = FriendType.RemoveBlack;
		if (isRemove) {//移除好友
			type = FriendType.Remove;
		}else{
			//判断黑名单中是否有
			if (!humanObj.friendInfo.blackMap.containsKey(humanId)) {
				humanObj.sendSysMsg(341901);//该玩家不在黑名单中
				return;
			}
		}
		disposeFriend(type, humanObj, humanId, time);
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.disposeHumanFriend(humanObj.id, type, time, humanId);
		if (!isRemove) {// 删除黑名单
			SCRemoveBlackList.Builder msg = SCRemoveBlackList.newBuilder();
			msg.setResult(true);
			msg.setHumanId(humanId);
			humanObj.sendMsg(msg);
		}else{// 删除
			SCRemoveFriend.Builder msg = SCRemoveFriend.newBuilder();
			msg.setHumanId(humanId);
			msg.setResult(true);
			humanObj.sendMsg(msg);
		}
	}
	/**
	 * 拉黑好友
	 * @param humanObj
	 * @param humanId
	 */
	public void onCSToBlackList(HumanObject humanObj, long humanId){
		if (humanObj.friendInfo.blackMap.containsKey(humanId)) {
			humanObj.sendSysMsg(341701);//该玩家已经在黑名单中
			return;
		}
		long time = Port.getTime();

		//黑名单上限
		if (humanObj.friendInfo.blackMap.size() >= ParamManager.friendsBlackUpperLimit) {
			humanObj.sendSysMsg(341702); //黑名单已达上限
			return;
		}
		// 对方处理被拉黑 直接删除这个好友
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.disposeHumanFriend(humanId,FriendType.Black, time,humanObj.id);
		prx.disposeHumanFriend(humanObj.id, FriendType.Blacked, time, humanId);
		SCToBlackList.Builder msg = SCToBlackList.newBuilder();
		msg.setResult(true);
		msg.setHumanId(humanId);
		humanObj.sendMsg(msg);
	}
	
	/************************************体力领取与赠送*****************************************/
	/***
	 * 体力赠送
	 * @param humanObj
	 * @param toHumanIds
	 */
	public void onGiveFriendAc(HumanObject humanObj, List<Long> toHumanIds) {
		if (toHumanIds == null || toHumanIds.isEmpty()) {
			toHumanIds = new ArrayList<>(humanObj.friendInfo.getCanGiveAcId());
		}
		long newTime = Port.getTime();
		SCGiveFriendAc.Builder msg = SCGiveFriendAc.newBuilder();
		boolean result = false;
		for (long humanId : toHumanIds) {
			if (humanId == humanObj.getHumanId()) {
				humanObj.sendSysMsg(342201);//不能赠送体力给自己
				continue;
			}
			int friendGiveTimes = humanObj.extInfo.getFriendGiveTimes();
			if(friendGiveTimes > ParamManager.friendsGiveTimes){
				humanObj.sendSysMsg(342202);//今天可以赠送的次数已达上限
				break;
			}
			// 赠送体力 对方走被赠送 将状态修改成 可领取
			Friend Friend = disposeFriend(FriendType.Give, humanObj, humanId, newTime);
			if (Friend != null) {
				result = true;
				int newFriendGiveTimes = friendGiveTimes + 1;
				humanObj.extInfo.setFriendGiveTimes(newFriendGiveTimes);
				msg.addHumanIds(humanId);
				msg.setYetGiveNum(newFriendGiveTimes);
				HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
				prx.disposeHumanFriend(humanObj.id, FriendType.Gived, newTime, humanId);
			}
		}
//		if (result) {
//			Inform.sendInform(humanObj.id, 2022);
//		}else{
// 			Inform.sendInform(humanObj.id, 2021);
//		}
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 领取玩家体力
	 */
	public void onGetFriendAc(HumanObject humanObj, List<Long> toHumanIds){
//		ConfGlobalParam confPer = ConfGlobalParam.get(24004);//每次增加体力
//		ConfGlobalParam confDailyMax = ConfGlobalParam.get(24005);//每日最大领取次数配置
//		if (confPer == null || confDailyMax == null) {
//			return;
//		}
		
		int friendsReceiveTimes = ParamManager.friendsReceiveTimes;
		long newTime = Port.getTime();
		// 一键领取获取可以一键领取的好友ID
		if (toHumanIds == null || toHumanIds.isEmpty()) {
			toHumanIds = new ArrayList<>(humanObj.friendInfo.getCanGetAcId());
		}
		List<Long> getAcToHumanIds = new ArrayList<>();
		// 获取可以领取的次数
		int yetReceiveTimes = humanObj.extInfo.getFriendReceiveTimes();
		int canGetNum = friendsReceiveTimes - yetReceiveTimes;
		if (canGetNum <= 0) {
			humanObj.sendSysMsg(342401);//超过每天领取次数
			return;
		}
		for (long humanId : toHumanIds) {
			if (canGetNum <= 0) {
				break;
			}
			// 不能领取返回null
			Friend Friend = disposeFriend(FriendType.Get, humanObj, humanId, newTime);
			if (Friend != null) {
				getAcToHumanIds.add(humanId);
				humanObj.extInfo.setFriendReceiveTimes(yetReceiveTimes + 1);
			}else{
//				Inform.sendInform(humanObj.id, 2018);	
				continue;
			}
			canGetNum --;
		}
		if (getAcToHumanIds.size() > 0) {
			RewardHelper.reward(humanObj, EMoneyType.act_VALUE, ParamManager.friendsGiveNumber, LogSysModType.FriendSendAct);
			SCReceFriendAc.Builder msg = SCReceFriendAc.newBuilder();
			msg.setYetGetNum(humanObj.extInfo.getFriendReceiveTimes());
			msg.addAllHumanIds(getAcToHumanIds);
			msg.setResult(true);
			humanObj.sendMsg(msg);
		}
	}
	
	public Friend disposeFriend(int type, HumanObject humanObj, long humanId, long time){
		return disposeFriend(type,humanObj,null,humanId,time);
	}
	
	/**
	 * 处理好友操作
	 * @param type
	 * @param humanObj 好友humanObj
	 * @param object 自己对应FriendObject
	 * @param time
	 * @return
	 */
	public Friend disposeFriend(int type, HumanObject humanObj, FriendObject object,long time){
		return disposeFriend(type, humanObj, object, 0l, time);
	}
	
	public static Friend disposeFriend(int type, HumanObject humanObj, FriendObject object,long humanId, long time){
		if (humanId == 0l && object != null) {
			humanId = object.getId();
		}
		
		if (humanId == 0l) {
			return null;
		}
		FriendInfo info = humanObj.friendInfo;
		Map<Long, Friend> maps = new HashMap<>(info.getAllMap());
		maps.putAll(info.removeMap);
		//从玩家所有的信息中 获取该ID的信息
		Friend friend = maps.get(humanId);
		//拒绝成为好友或者删除 
		if (friend == null && type != FriendType.Refuse && type != FriendType.Remove) {
			friend = new Friend();
			friend.setId(Port.applyId());
			friend.setHumanId(humanObj.id);
			friend.setFId(humanId);
			friend.persist();
		}
		if (friend == null)
			return friend;
		int oldType = friend.getType();
		/************************************************************/
		//申请成为好友
		if (type == FriendType.Apply) {
			// 如果已经是的话 就不更新数据
			if (oldType != FriendType.Apply) {
				friend.setType(FriendType.Apply);
				friend.setTime(time);
				info.applyMap.put(friend.getFId(), friend);
			}
			if (oldType == FriendType.Black) {
				sendMsgToC(humanObj,friend, object, FriendConstants.OPTION_DELETE, EFriendType.Black);
			}
			sendMsgToC(humanObj,friend, object, FriendConstants.OPTION_ADD, EFriendType.Apply);
		}
		// 好友删除、黑名单删除、拒绝
		else if (friend != null && (type == FriendType.Refuse || type == FriendType.Remove 
				|| type == FriendType.RemoveBlack || type == FriendType.Blacked)) {
			boolean isToRemove = false;//是否暂时留在删除中
			if (Utils.getDaysBetween(friend.getGive(), Port.getTime()) > 0) {//如果赠送的不是今天
				List<Long> getList = Utils.strToLongList(friend.getGet());
				if (getList.size() == 2) {
					int getTime = Utils.getDaysBetween(getList.get(0), Port.getTime());
					//如果今天是已经领取了这个好友的体力，那么就暂时移到删除
					if (getTime == 0) {
						isToRemove = true;
					}
				}
			}else{
				isToRemove = true;
			}
			if (type == FriendType.Refuse) {
				info.applyMap.remove(friend.getFId());
			}else if(type == FriendType.Remove || type == FriendType.Blacked){
				if (info.friendMap.containsKey(friend.getFId())) {
					info.friendMap.remove(friend.getFId());
					sendMsgToC(humanObj, friend, object, FriendConstants.OPTION_DELETE, EFriendType.Friend);
				}
			}else{
				info.blackMap.remove(friend.getFId());
				sendMsgToC(humanObj, friend, object, FriendConstants.OPTION_DELETE, EFriendType.Black);
			}
			if (isToRemove) {// 转移到删除中
				friend.setType(FriendType.Remove);
				info.removeMap.put(friend.getFId(), friend);
			}else{
				friend.remove();
			}
		}else if (type == FriendType.Accept || type == FriendType.Friend) {// 同意
			if (friend.getType() != FriendType.Friend) {
				friend.setType(FriendType.Friend);
				friend.setTime(time);
				info.friendMap.put(friend.getFId(), friend);
				info.applyMap.remove(friend.getFId());
				sendMsgToC(humanObj,friend, object, FriendConstants.OPTION_ADD, EFriendType.Friend);
				if (oldType == FriendType.Black) {
					sendMsgToC(humanObj,friend, object, FriendConstants.OPTION_DELETE, EFriendType.Black);
				}
			}
		}else if(type == FriendType.Black){//拉黑
			if (oldType != FriendType.Black) {
				friend.setType(FriendType.Black);
				friend.setTime(time);
				if (info.friendMap.containsKey(friend.getFId())) {
					info.friendMap.remove(friend.getFId());
				}
				info.blackMap.put(friend.getFId(),friend);
				sendMsgToC(humanObj, friend,object, FriendConstants.OPTION_DELETE, EFriendType.Friend);
				sendMsgToC(humanObj, friend,object, FriendConstants.OPTION_ADD, EFriendType.Black);
			}
		}
		/*********************************************************************/
		if (type == FriendType.Give) {
			// 设置赠送时间
			if (Utils.getDaysBetween(time, friend.getGive()) != 0) {
				friend.setGive(time);
			}
		}else if(type == FriendType.Get){
			List<Long> getTime = Utils.strToLongList(friend.getGet());
			// 对方有赠送以及 赠送时间为今天
			if (getTime != null && getTime.size() == 2) {
				if (getTime.get(1) == 1 && Utils.getDaysBetween(time, getTime.get(0)) == 0) {
					getTime.set(1, 2L);
					friend.setGet(Utils.ListLongToStr(getTime));
					info.getAcSize ++;
				}else{
					return null;
				}
			}else{
				return null;
			}
		}else if(type == FriendType.Gived){
			List<Long> list = Utils.strToLongList(friend.getGet());
			if (list.size() == 2) {
				if (list.get(1) == 1) { // 可领取，不必再处理
					return null;
				}
				if (Utils.getDaysBetween(time, list.get(0)) == 0) {
					return null;
				}
			}
			list.clear();
			list.add(time);
			list.add(1l);//可领取
			friend.setGet(Utils.ListLongToStr(list));
			sendMsgToC(humanObj,friend, object, FriendConstants.OPTION_UPDATE, EFriendType.Friend);
		}
		//System.out.println("操作完"+humanObj.name+"-"+type+"好友数："+info.friendMap.size()+" 申请数："+info.applyMap.size());
		FriendServiceProxy prx = FriendServiceProxy.newInstance();
		prx.synApplyFriend(humanObj.getHumanId(), info.applyMap.size(),info.friendMap.size(),true);
		return friend;
	}
	
	public static void sendMsgToC(HumanObject humanObj,Friend friend,FriendObject object,int option,EFriendType type){
		if (friend == null || object == null) {
			return;
		}
		SCFriendInfo.Builder msg1 = SCFriendInfo.newBuilder();
		msg1.setOption(option);
		msg1.setType(type);
		msg1.setInfo(FriendManager.inst().getDFriendInfo(object, friend));
		humanObj.sendMsg(msg1);
	}
	public void _msg_CSFindByDigit(HumanObject humanObj, long humanId) {
		// TODO Auto-generated method stub
		onCSSearchFriend(humanObj, humanId + "");
	}
	public void createFriendList(long id, String name) {
		// TODO Auto-generated method stub
		
	}
	public void updateFriendInfo(HumanObject humanObject) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 查看好友信息
	 * @param queryId
	 */
	public void queryCharacters(HumanObject humanObj,long queryId) {
		if(humanObj.getHumanId() == queryId ) {
			humanObj.sendSysMsg(342403);//无法查看自己
			return; 
		}
		
		OffilineGlobalServiceProxy prxs = OffilineGlobalServiceProxy.newInstance();
		prxs.getInfo(queryId);
		prxs.listenResult(this::_result_queryCharacters2, "humanObj", humanObj);
//		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
//		prx.getInfo(queryId);
//		prx.listenResult(this::_result_queryCharacters, "humanObj", humanObj);
	}
	public void _result_queryCharacters2(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		if (humanObj == null) {
			Log.game.error("=== humanObj is null ===");
			return;
		}
		SCQueryCharacter msg = results.get("msg");
		if(msg.getHumanId() != 0) {
			humanObj.sendMsg(msg);
		}else {
			humanObj.sendSysMsg(342402);//该玩家无法查看 竞技场机器人数据
		}
	}
	public void _result_queryCharacters(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		HumanGlobalInfo info = results.get();
		if (info == null) {
			Log.game.error("=== 玩家已经离线，无法查询 ===");
			humanObj.sendSysMsg(120);
			return;
		}
		SCQueryCharacter.Builder msg = SCQueryCharacter.newBuilder();
		msg.setHumanId(info.id);
		msg.setHumanDigit(info.digit);
		msg.setLevel(info.level);
		msg.setName(info.name);
		msg.setModelSn(info.modelSn);
		msg.setTitleSn(info.titleSn);
		msg.setCombat(info.combat);
		msg.setVip(info.vipLv);
		msg.setProfession(info.profession);
//		HumanSkillRecord humanSkillRecord = info.humanSkillRecord;
//		msg.addAllSkill(humanSkillRecord.getSkillGroup());
//		msg.setGods(humanSkillRecord.getInstallGods());
		
		// 玩法数据
		msg.setInstStar(info.instStar);
		
		// 设置技能相关
		// 上阵技能
		List<SkillJSON> skillList = info.skillList;
		for (SkillJSON skillJSON : skillList) {
			msg.addSkillList(skillJSON.createDSkill());
		}
		// 爆点信息
		List<SkillGodsJSON> skillGodsList = info.skillGodsList;
		for (SkillGodsJSON skillGodsJSON : skillGodsList) {
			msg.addSkillGodsList(skillGodsJSON.createDSkillGods());
		}
		// 上阵爆点sn
		msg.setSkillGodsSn(info.installGods);
		
		// 设置装备相关
		// 装备信息
		List<ItemBody> equipList = info.equipList;
		for (ItemBody equip : equipList) {
			msg.addEquipList(equip.getDEquip());
		}
		
		// 设置伙伴信息
		List<PartnerObject> partnerList = info.partnerObjList;
		if(partnerList != null){
			for (PartnerObject po : partnerList) {
				Partner partner  = po.getPartner();
				DPartnerBriefInfo.Builder dpi = DPartnerBriefInfo.newBuilder();
				dpi.setStar(partner.getStar());
				dpi.setLevel(partner.getLevel());
				dpi.setId(partner.getId());
				dpi.setSn(partner.getSn());
				dpi.setAdvanceLevel(partner.getAdvLevel());
				//设置护法信息
				Map<Long, Partner> servantMap = po.getServantMap();
				if (null != servantMap) {
					List<Long> slist  = po.getServantList();
					for(Long id : slist){
						Partner p = servantMap.get(id);
						if (null != p) {
							DServantBriefInfo.Builder dsinfo = DServantBriefInfo.newBuilder();
							dsinfo.setSn(p.getSn());
							dsinfo.setLevel(p.getLevel());
							dsinfo.setStarts(p.getStar());
							dsinfo.setAdvance(p.getAdvLevel());
							dpi.addServant(dsinfo);
						}
					}
					msg.addInfo(dpi);
				}
			}
		}
		msg.setCompeteRank(info.competeRank==ParamManager.competeRankMax?0:info.competeRank);//0是未上榜
		humanObj.sendMsg(msg);
	}
	
	
}
