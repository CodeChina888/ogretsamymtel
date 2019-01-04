package game.worldsrv.activitySeven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Config;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.Utils;
import core.support.observer.Listener;
import game.msg.Define.DIntegralBox;
import game.msg.Define.DNoviceActivity;
import game.msg.Define.DProduce;
import game.msg.Define.EAwardType;
import game.msg.Define.EMoneyType;
import game.msg.Define.EShopType;
import game.msg.MsgActivity.SCActivityIntegral;
import game.msg.MsgActivitySeven.SCCommitNoviceActivity;
import game.msg.MsgActivitySeven.SCOpenNoviceActivity;
import game.msg.MsgActivitySeven.SCTypeNoviceActivity;
import game.worldsrv.achieveTitle.AchieveTitleTypeKey;
import game.worldsrv.achievement.AchievementTypeKey;
import game.worldsrv.activitySeven.typedata.ActivitySevenTypeDataFactory;
import game.worldsrv.activitySeven.typedata.IActivitySevenTypeData;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivitySeven;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.Achievement;
import game.worldsrv.entity.ActivitySeven;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemBodyManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class ActivitySevenManager extends ManagerBase {
	public static final int Status_Ing = 1; // 进行中（默认进行中）
	public static final int Status_CanGet = 2; // 可以领取
	public static final int Status_YetGet = 3; // 已经领取

	private static final String GENERAL = "1";
	private static final String HARD = "2";
	private static final int PURPLE = 3;
	private static final int ORANGE = 4;

	public static ActivitySevenManager inst() {
		return inst(ActivitySevenManager.class);
	}
	
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		long humanId = humanObj.id;
		DB db = DB.newInstance(ActivitySeven.tableName);
		db.findBy(false, ActivitySeven.K.humanID, humanId);
		db.listenResult(this::_result_loadActivitySeven, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadActivitySeven(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<Record> records = results.get();
		for (Record record : records) {
			if (record == null) {
				continue;
			}
			ActivitySeven data = new ActivitySeven(record);
			humanObj.humanActivitySeven.put(data.getType(), data);
		}
		if(humanObj.humanActivitySeven==null || humanObj.humanActivitySeven.isEmpty()){
			Log.game.info("+++++++++++++++++++七日活动数据异常");
		}
		
		_listener_loginFinish(humanObj);
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	
	/**
	 * 初始化7天活动
	 */
	@Listener(EventKey.HumanCreate)
	public void initActivitySeven(Param param) {
		Human human = param.get("human");
		createNewAct(human);
	}
	/**
	 * 解锁7天活动
	 */
	public void createNewAct(Human human) {
		// 超过创角7天就不存7天数据了
		int getData = getCreateToNowDay(human.getTimeCreate());
		if (getData > 7) {
			return;
		}
		Map<Integer,ActivitySeven> newServiceAct = new HashMap<>();
		for (ConfActivitySeven conf : ConfActivitySeven.findAll()) {
			ActivitySeven data = newServiceAct.get(conf.type);
			if (data == null) {
				data = new ActivitySeven();
				data.setId(Port.applyId());
				data.setHumanID(human.getId());
				data.setType(conf.type);
				data.persist();
			}
			// 存入ID
			List<Integer> list = Utils.strToIntList(data.getActId());
			list.add(conf.sn);
			data.setActId(Utils.ListIntegerToStr(list));
			list.clear();
			// 存入进度
			list = Utils.strToIntList(data.getActStatus());
			int status = Status_Ing;
			//每日福利，只要登录了就是可领取状态
			if (conf.type == ActivitySevenTypeKey.Type_1) {
				status = Status_CanGet;
			}
			list.add(status);
			data.setActStatus(Utils.ListIntegerToStr(list));
			list.clear();
			// 加入进度
			list = Utils.strToIntList(data.getActProgress());
			list.add(0);
			data.setActProgress(Utils.ListIntegerToStr(list));
			newServiceAct.put(conf.type, data);
		}
		
	}

	public void _listener_loginFinish(HumanObject humanObj){
		long createTime = humanObj.getHuman().getTimeCreate();
		int createDay = getCreateToNowDay(createTime);
		if (createDay > 7) {// 超过7天就不下发信息
			return ;
		}
		SCOpenNoviceActivity.Builder msg = SCOpenNoviceActivity.newBuilder();
		msg.setCreateTime(createTime);
		msg.setCreateDay(createDay);
		for (ActivitySeven act : humanObj.humanActivitySeven.values()) {
			msg.addAllDna(getDNoviceAct(act, createTime));
		}
		humanObj.sendMsg(msg);
	}
	
	@Listener(EventKey.HumanLoginFinishFirstToday)
	public void _listener_loginFirstToday(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		long createTime = humanObj.getHuman().getTimeCreate();
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", getCreateToNowDay(createTime) , "type",
				ActivitySevenTypeKey.Type_1);
	}
	
	@Listener(EventKey.PayNotify)
	public void _listener_PayNotify(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		long gold = param.get("gold");
		Human human = humanObj.getHuman();
		/*修改玩家每日充值信息*/
		long myGold = human.getTodayChargeGold();
		human.setTodayChargeGold(myGold+gold);
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", (int)human.getTodayChargeGold() , "type",
				ActivitySevenTypeKey.Type_5);
	}
	
	/**
	 * 每个整点执行一次
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
			// 每日重置
			long createTime = humanObj.getHuman().getTimeCreate();
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", getCreateToNowDay(createTime) , "type",
					ActivitySevenTypeKey.Type_1);
		}
	}
	
	/**
	 * 装备精炼
	 */
	@Listener(EventKey.EquipRefine)
	public void onEventHumanEquipRefine(Param param) {
		HumanObject humanObj = param.get("humanObj");
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", humanObj.cultureTimes.getRefine(), "type",
				ActivitySevenTypeKey.Type_41);
		
	}
	
	
	
	/**
	 * 技能修炼
	 */
	@Listener(EventKey.HumanSkillTrainSave)
	public void onEventHumanSkillTrainSave(Param param) {
		HumanObject humanObj = param.get("humanObj");
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0, "type",
				ActivitySevenTypeKey.Type_38);
		
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress",humanObj.cultureTimes.getAvatarNum(), "type",
				ActivitySevenTypeKey.Type_39);
	}
	
	
	
//	/**
//	 * 商店购买
//	 */
//	@Listener(value = EventKey.ActShopBuy)
//	public void onHumanActShopBuy(Param param) {
//		HumanObject humanObj = param.get("humanObj");
//		if (humanObj == null) {
//			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
//			return;
//		}
//		int sn = param.getInt("shopSn");
////		EShopType
//		switch (EShopType.valueOf(sn)) {
//		case EShopType.ShopGold_VALUE:
//			
//			break;
//
//		default:
//			break;
//		}
//		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", humanObj.getHuman().getLevel() , "type",
//				ActivitySevenTypeKey.Type_6);
//	}
	
	
	/**
	 *事件监听: 充值成功
	 */
	@Listener(value = EventKey.PayNotifyHttps)
	public void onEventChargeGold(Param param) {
		// 玩家关于这个的数据
		HumanObject humanObj = param.get("humanObj");
		// 判断今天的有没有充值过了
		long gold = param.get("gold");
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", (int)gold , "type",
				ActivitySevenTypeKey.Type_5);
	}
	/**
	 * 玩家升级更新，成员表等级信息
	 * @param param
	 */
	@Listener(EventKey.HumanLvUp)
	public void onHumanUpgrade(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", humanObj.getHuman().getLevel() , "type",
				ActivitySevenTypeKey.Type_6);
	}
	/**
	 * 技能升级
	 * 
	 * @param param
	 */
	@Listener(EventKey.HumanSkillUp)
	public void _listener_HumanSkillUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		int type = ActivitySevenTypeKey.Type_37;
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0, "type",	type);
	}
	/**
	 * 装备强化
	 * 
	 * @param param
	 */
	@Listener(EventKey.EquipIntensify)
	public void _listener_EquipIntensify(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		//int progress = ItemBodyManager.inst().getEquipNumByReinforceLv(humanObj, curParamByLv);
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0, "type",
				ActivitySevenTypeKey.Type_4);
	}
	
	/**
	 * 装备进阶
	 * 
	 * @param param
	 */
	@Listener(EventKey.EquipAdvanced)
	public void _listener_EquipAdvanced(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		//int progress = ItemBodyManager.inst().getEquipNumByAdvancedLv(humanObj, curParamByLv);
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0 , "type",
				ActivitySevenTypeKey.Type_17);
	}
	/**
	 * 伙伴招募
	 * 
	 * @param param
	 */
	@Listener(EventKey.PartnerUnlocked)
	public void _listener_PartnerUnlocked(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		int partnerSn = Utils.getParamValue(param, "sn", null);
		ConfPartnerProperty confPartnerProperty = ConfPartnerProperty.get(partnerSn);
		if (confPartnerProperty == null) {
			Log.game.error("===_配置表PartnerProperty错误,sn={}", partnerSn);
			return;
		}
		if (PURPLE == confPartnerProperty.quality) {
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 1 , "type",ActivitySevenTypeKey.Type_30);
		}else if (ORANGE == confPartnerProperty.quality) {
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 1 , "type",ActivitySevenTypeKey.Type_31);
		}
	}
	
	/**
	 * 伙伴突破
	 * 
	 * @param param
	 */
	@Listener(EventKey.PartnerAdvanced)
	public void _listener_PartnerAdvanced(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		//int progress = PartnerManager.inst().hasNumAdvanced(humanObj, curParamByLv);
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0 , "type",
				ActivitySevenTypeKey.Type_32);
	}

	/**
	 * 伙伴升星
	 * 
	 * @param param
	 */
	@Listener(EventKey.PartnerStartUp)
	public void _listener_PartnerStartUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		//int progress = PartnerManager.inst().hasNumStart(humanObj, curParamByLv);
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0 , "type",
				ActivitySevenTypeKey.Type_33);
	}
	/**
	 * 神兽升级
	 * 
	 * @param param
	 */
	@Listener(EventKey.GodsLvUp)
	public void _listener_GodsLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		//Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_16);
		//int target = achievement.getTarget();
		//int progress = humanObj.humanSkillRecord.getAmountByGodsLv(target);
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0 , "type",
				ActivitySevenTypeKey.Type_15);
	}
	
	/**
	 * 通关副本
	 * 
	 * @param param
	 */
	@Listener(EventKey.InstAnyPass)
	public void _listener_InstAnyPass(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}		

		int stageSn = Utils.getParamValue(param, "stageSn", 0);
		// 获得副本配置
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={} ", stageSn);
			return;
		}
		String stageSnStr = String.valueOf(stageSn);
		String type = stageSnStr.substring(0, 1);
		if (GENERAL.equals(type)) {// 普通副本
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", stageSn , "type",
				ActivitySevenTypeKey.Type_3);
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0 , "type",
					ActivitySevenTypeKey.Type_34);
		} else if (HARD.equals(type)) {// 精英副本
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", stageSn , "type",
				ActivitySevenTypeKey.Type_36);
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0 , "type",
				ActivitySevenTypeKey.Type_35);
		}
	}
	/**
	 * 竞技场达到最高名次
	 * 
	 * @param param
	 */
	@Listener(EventKey.CompeteRankHighest)
	public void _listener_CompeteRankHighest(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		int rank = Utils.getParamValue(param, "rank", 0);
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", rank, "type",
				ActivitySevenTypeKey.Type_9);
	}
	
	/**
	 * 炼药塔通关
	 */
	
	@Listener(EventKey.TowerPass)
	public void _listener_TowerPass(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		// 校验参考值：难度 target = 难度*1000+层数 (参考成就任务的数值填发)
		int diff = Utils.getParamValue(param, "selDiff", 1);//难度
		int layer = Utils.getParamValue(param, "layer", 1);//层数
		int num = diff*Utils.I1000 + layer;
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", num, "type",ActivitySevenTypeKey.Type_51);
	}
	
	/**
	 *战力
	 * @param param
	 */
	/*@Listener(EventKey.HumanCombatChange)
	public void updateActFight(Param param){
		HumanObject humanObj = param.get("humanObj");
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", humanObj.getHuman().getCombat() , "type",
				ActivitySevenTypeKey.Type_7);
	}*/
	/**
	 * 更新玩家7天开服活动
	 */
	@Listener(EventKey.UpdateActivitySeven)
	public void _listener_UpdateSeviceActivity(Param param) {
		HumanObject humanObj = param.get("humanObj");
		long createTime = humanObj.getHuman().getTimeCreate();
		// 超过创角7天就不存7天数据了
		if (getCreateToNowDay(createTime) > 7) {
			return;
		}
		// 判断是否有任务变化
		boolean change = false;
		// 获取任务变量，次数，任务类型
		int progress = param.getInt("progress");
		int type = param.getInt("type");
		IActivitySevenTypeData data = ActivitySevenTypeDataFactory.getTypeData(type);
		change = data.doProgress(humanObj, type, progress);
		if (change) {
			sendTypeNoviceActivity(humanObj, type);
		}
	}
	
	/**
	 * 下发该类型的活动
	 * @param humanObj
	 * @param type
	 */
	public void sendTypeNoviceActivity(HumanObject humanObj,int type){
		SCTypeNoviceActivity.Builder msg = SCTypeNoviceActivity.newBuilder();
		msg.setType(type);
		msg.addAllDna(getDNoviceAct(humanObj.humanActivitySeven.get(type), humanObj.getHuman().getTimeCreate()));
		humanObj.sendMsg(msg);
	}
	//或许未开启就不下发信息了
	public List<DNoviceActivity> getDNoviceAct(ActivitySeven act, long createTime) {
		List<DNoviceActivity> msgList = new ArrayList<>();
		List<Integer> idList = Utils.strToIntList(act.getActId());
		List<Integer> statusList = Utils.strToIntList(act.getActStatus());
		List<Integer> pgList = Utils.strToIntList(act.getActProgress());
		for (int i = 0; i < idList.size(); i++) {
			int id = idList.get(i);
			ConfActivitySeven conf = ConfActivitySeven.get(id);
			if (conf == null) {
				continue;
			}
//			//活动未到开启时间就不下发了吧
//			if (getCreateToNowDay(createTime) < conf.day) {
//				continue;
//			}
			DNoviceActivity.Builder dna = DNoviceActivity.newBuilder();
			//如果是每日充值,过期的不下发 临时措施
			if(conf.type == 5 ) {
				if( getCreateToNowDay(createTime) > conf.day) {
					continue;
				}
				
			}
			dna.setSn(conf.sn);
			dna.setStatus(statusList.get(i));
			dna.setPropgress(pgList.get(i));
//			System.out.println(conf.id + " "+ statusList.get(i)+" "+pgList.get(i));
			msgList.add(dna.build());
		}
		return msgList;
	}
	/**
	 * 创角时间至今几天 --改成开服至今几天
	 * @param createTime
	 * @return
	 */
	public int getCreateToNowDay(long createTime){
		long newTime = Port.getTime();
		long startTime = Config.SERVER_STARTDATE; // 获得开服时间 
		Date date = new Date(startTime);
		createTime = date.getTime();
		int getData = Utils.getDaysBetween(createTime, newTime);
		return getData + 1;
	}
	/**
	 * 提交活动ID
	 * 
	 * @param humanObj
	 * @param actId
	 */
	public void commitServiceActivity(HumanObject humanObj, int actId) {
		ConfActivitySeven conf = ConfActivitySeven.get(actId);
		if (conf == null) {
			return;
		}
		if (!conf.open || conf.needLv > humanObj.getHuman().getLevel()) {
			//Inform.sendSysInform(humanObj.id, 17);
			return;
		}
		int getCreateDay = getCreateToNowDay(humanObj.getHuman().getTimeCreate());
		// 时间未到 不能提交
		if (conf.day > getCreateDay) {
			return;
		}
		if (!RewardHelper.canConsume(humanObj, EMoneyType.gold_VALUE,conf.needMoney)) {
			//TODO 发公告
			//Inform.sendSysInform(humanObj.id, 6);
			return;
		}
		IActivitySevenTypeData data = ActivitySevenTypeDataFactory.getTypeData(conf.type);
		if (!data.disposeCommit(humanObj, actId)) {
			//TODO 发公告
//			Inform.sendSysInform(humanObj.id, 108);
//			humanObj.sendSysMsg();
			return;
		}
		
		if (conf.needMoney > 0) {
			if(!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE,conf.needMoney, LogSysModType.KFSeven)) {
				return;
			}
			
		}
		ItemChange itemChange = null;
		if(conf.type == ActivitySevenTypeKey.Type_2){
			itemChange = RewardHelper.reward(humanObj, Utils.intValue(conf.param[0]), Utils.intValue(conf.param[1]), LogSysModType.KFSeven);
		}else{
			ConfRewards confRewards = ConfRewards.get(conf.reward);
			itemChange = RewardHelper.reward(humanObj, confRewards.itemSn, confRewards.itemNum, LogSysModType.KFSeven);
			//ItemChange itemChange = RewardHelper.reward(humanObj, conf.rewardList, conf.rewardCountList, LogSysModType.KFSeven);
		}
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		//领取成功增加积分
		
		int integral = conf.integral;
		
		addIntegral(humanObj,integral);
		
		
		// 发送这个提交成功
		SCCommitNoviceActivity.Builder msg = SCCommitNoviceActivity.newBuilder();
		msg.setSn(actId);
		humanObj.sendMsg(msg);
		sendSCActivityIntegralMsg(humanObj,null);
	}
	
	/**
	 * 增加活动积分
	 * @param humanObj
	 * @param integral
	 */
	public void addIntegral(HumanObject humanObj,int integral) {
		Human human = humanObj.getHuman();
		int old_integral = human.getActivityIntegral();
		human.setActivityIntegral(old_integral+integral);
	}
	/**
	 * 领取活动积分宝箱
	 * @param humanObj
	 * @param sn
	 */
	public void onCSActivityIntegral(HumanObject humanObj, int sn) {
		//判断积分是否达到
		ConfActivitySeven conf = ConfActivitySeven.get(sn);
		if(conf == null ) {
			Log.table.error("can't find sn ={}",sn);
		}
		Human human = humanObj.getHuman();
		int order = Utils.intValue(conf.param[0]);
		if(human.getActivityIntegral() < order) {
			//积分不足
			return;
		}
		//判断是否领取过
		List<Integer> getList = Utils.strToIntList(human.getIntegralBoxList());
		if(getList == null) {
			getList = new ArrayList<>();
		}
		if(getList.contains(sn)) {
			//已经领取过了
			return;
		}
		//设置已经领取
		getList.add(sn);
		human.setIntegralBoxList(Utils.intListToStr(getList));
		//领取奖励,发放奖品
		ConfRewards rewordId = ConfRewards.get(conf.reward);
		if (rewordId == null) {
			Log.table.error("===Rewards配置表错误 ,no find sn={}", sn);
			return;
		}
		int[] itemSn = rewordId.itemSn;
		int[] itemNum = rewordId.itemNum;

		ItemChange itemChange =  RewardHelper.reward(humanObj, itemSn, itemNum, LogSysModType.Activity);
		sendSCActivityIntegralMsg(humanObj,itemChange.getProduce());
		
	}
	
	public static final int IntegralBox = 99;
	
	
	
	/**
	 * 登录加载所有数据后，下发七日活动宝箱数据
	 */
	@Listener(EventKey.HumanDataLoadAllFinish)
	public void _listener_HumanDataLoadAllFinish(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadAllFinish humanObj is null");
			return;
		}
		//下发七日活动宝箱数据
		ActivitySevenManager.inst().sendSCActivityIntegralMsg(humanObj, null);
	}
	
	/**
	 * 发送宝箱状态
	 * @param humanObj
	 */
	public void sendSCActivityIntegralMsg(HumanObject humanObj,List<DProduce> items) {
		//发送宝箱领取状况
		SCActivityIntegral.Builder msg = SCActivityIntegral.newBuilder();
		
		Human human = humanObj.getHuman();
		List<Integer> getList = Utils.strToIntList(human.getIntegralBoxList());
		//积分
		int integral = human.getActivityIntegral();
		
		//TODO 后期修改
		List<ConfActivitySeven> confAll = new ArrayList<>();
		confAll.add(ConfActivitySeven.get(13101));
		confAll.add(ConfActivitySeven.get(13102));
		confAll.add(ConfActivitySeven.get(13103));
		confAll.add(ConfActivitySeven.get(13104));
		
		for(ConfActivitySeven conf : confAll) {
			DIntegralBox.Builder dinfo = DIntegralBox.newBuilder();
			dinfo.setSn(conf.sn);
			//判断状态
			int order = Utils.intValue(conf.param[0]);
			EAwardType status = EAwardType.AwardNot;
			if(getList.contains(conf.sn)) {
				status = EAwardType.Awarded;
			}
			if(!getList.contains(conf.sn) && integral >= order) {
				status = EAwardType.Awarding;
			}
			dinfo.setType(status);
			dinfo.setRewardId(conf.reward);
			msg.addBox(dinfo);
			msg.setScore(integral);
		}
		if(items!= null) {
			msg.addAllDitemList(items);
		}
		humanObj.sendMsg(msg);
		
	}
		
}
