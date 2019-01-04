package game.worldsrv.achievement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define.DAchievement;
import game.msg.Define.DProduce;
import game.msg.Define.EAchievementStatus;
import game.msg.Define.EDrawType;
import game.msg.Define.EMoneyType;
import game.msg.MsgQuest.SCAchievementInfo;
import game.msg.MsgQuest.SCCommitAchievement;
import game.worldsrv.achievement.typedata.AchievementTypeDataFactory;
import game.worldsrv.achievement.typedata.IAchievementTypeData;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchievement;
import game.worldsrv.config.ConfAchievementType;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.Achievement;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.ItemBodyManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.quest.QuestDailyManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.LogOpUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.tower.TowerManager;

public class AchievementManager extends ManagerBase {
	private static final String GENERAL = "1";
	private static final String HARD = "2";
	private static final int ORANGE = 4;
	private static final int PURPLE = 3;
	private static final int RED = 5;

	/**
	 * 获取实例
	 *
	 * @return
	 */
	public static AchievementManager inst() {
		return inst(AchievementManager.class);
	}

	/**
	 * 初始化全部成就任务
	 */
	@Listener(EventKey.HumanCreate)
	public void initAchievement(Param param) {
		Human human = param.get("human");
		// 取出所有的配置
		List<ConfAchievement> confList = new ArrayList<>();
		confList.addAll(ConfAchievement.findAll());
		
		// 挨个遍历配置，并且初始化
		for (ConfAchievement conf : confList) {
			// 去掉暂未开放的成就
			// if (conf.param.length == 1 && conf.param[0] == 0)
			// continue;
			// 等级不满足的任务不开放 创角时等级为
			if (conf.needLv > 1)
				continue;

			// 初始化所有的任务
			IAchievementTypeData typeData = AchievementTypeDataFactory.getTypeData(conf.type);

			// 成就任务，初始化时需要区别设置状态
			typeData.init(human.getId(), conf);
//			Achievement achieve = typeData.init(human.getId(), conf);
//			humanObj.achievements.add(achieve); // 加入在线内存管理
		}
	}

	/**
	 * 玩家其它数据加载开始：加载玩家的成就信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Achievement.tableName);
		dbPrx.findBy(false, Achievement.K.HumanId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanAchievement, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}

	private void _result_loadHumanAchievement(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanAchievement humanObj is null");
			return;
		}
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanAchievement records=null");
		} else {
			// 加载数据
			for (Record record : records) {
				Achievement achievement = new Achievement(record);
				humanObj.achievements.add(achievement);
			}
			sendMsg(humanObj, humanObj.achievements);
		}

		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}

	/**
	 * 通关副本 1 2 3 4 5 6
	 * @param param
	 */
	@Listener(EventKey.InstAnyPass)
	public void _listener_InstAnyPass(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		// 任务1普通本章节全三星
		Achievement achievement1 = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_1);
		int target1 = achievement1.getTarget();
		// 任务2精英本章节全三星
		Achievement achievement2 = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_2);
		int target2 = achievement2.getTarget();

		int stageSn = Utils.getParamValue(param, "stageSn", 0);
		String stageSnStr = String.valueOf(stageSn);
		String type = stageSnStr.substring(0, 1);
		if (GENERAL.equals(type)) {// 普通副本
			achieveUpdate(humanObj, stageSn, AchievementTypeKey.ACHIEVEMENT_TYPE_3);
			if (InstanceManager.inst().isInstPassPerfect(humanObj, stageSn)) {// 是否三星通关
				achieveUpdate(humanObj, stageSn, AchievementTypeKey.ACHIEVEMENT_TYPE_5);
				if (InstanceManager.inst().isInstChapPassPerfect(humanObj, target1)) { // 章节全三星
					achieveUpdate(humanObj, target1, AchievementTypeKey.ACHIEVEMENT_TYPE_1);
				}
			}
		} else if (HARD.equals(type)) {// 精英副本
			achieveUpdate(humanObj, stageSn, AchievementTypeKey.ACHIEVEMENT_TYPE_4);
			if (InstanceManager.inst().isInstPassPerfect(humanObj, stageSn)) {// 是否三星通关
				achieveUpdate(humanObj, stageSn, AchievementTypeKey.ACHIEVEMENT_TYPE_6);
				if (InstanceManager.inst().isInstChapPassPerfect(humanObj, target2)) { // 章节全三星
					achieveUpdate(humanObj, target2, AchievementTypeKey.ACHIEVEMENT_TYPE_2);
				}
			}
		}
	}

	/**
	 * 竞技场达到最高名次 7
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
		achieveUpdate(humanObj, rank, AchievementTypeKey.ACHIEVEMENT_TYPE_7);
	}

	/**
	 * 主角升级 8
	 * @param param
	 */
	@Listener(EventKey.HumanLvUp)
	public void _listener_HumanLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		int level = humanObj.getHuman().getLevel();
		achieveUpdate(humanObj, level, AchievementTypeKey.ACHIEVEMENT_TYPE_8);
	}

	/**
	 * 技能升级 9
	 * @param param
	 */
	@Listener(EventKey.HumanSkillUp)
	public void _listener_HumanSkillUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_9);
		int target = achievement.getTarget();
		int progress = humanObj.humanSkillRecord.getAmountBySkillLv(target);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_9);
	}

	/**
	 * 伙伴招募 10 11 12
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
		// 图鉴解锁的品质
		if (PURPLE == confPartnerProperty.quality) {
			achieveUpdate(humanObj, 1, AchievementTypeKey.ACHIEVEMENT_TYPE_10);
		} else if (ORANGE == confPartnerProperty.quality) {
			achieveUpdate(humanObj, 1, AchievementTypeKey.ACHIEVEMENT_TYPE_11);
		} else if (RED == confPartnerProperty.quality) {
			achieveUpdate(humanObj, 1, AchievementTypeKey.ACHIEVEMENT_TYPE_12);
		}
	}

	/**
	 * 参与世界boss 13
	 */
	@Listener(EventKey.ActInstWorldBossEnter)
	public void _listener_JoinWorldBoss(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		AchievementManager.inst().achieveUpdate(humanObj, 1, AchievementTypeKey.ACHIEVEMENT_TYPE_13);
	}

	/**
	 * 击杀世界boss 14
	 */
	@Listener(EventKey.ActInstWorldBossKiller)
	public void _listener_KillWorldBoss(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		AchievementManager.inst().achieveUpdate(humanObj, 1, AchievementTypeKey.ACHIEVEMENT_TYPE_14);
	}

	/**
	 * 神兽解锁15
	 */
	@Listener(EventKey.GodsUnlock)
	public void _listener_GodsUnlock(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		int unlockNum = Utils.getParamValue(param, "unlockNum", 1);
		achieveUpdate(humanObj, unlockNum, AchievementTypeKey.ACHIEVEMENT_TYPE_15);
	}

	/**
	 * 神兽升级16
	 * @param param
	 */
	@Listener(EventKey.GodsLvUp)
	public void _listener_GodsLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_16);
		int target = achievement.getTarget();
		int progress = humanObj.humanSkillRecord.getAmountByGodsLv(target);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_16);
	}

	/**
	 * 伙伴升级17
	 * @param param
	 */
	@Listener(EventKey.PartnerLvUp)
	public void _listener_PartnerLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_17);
		int target = achievement.getTarget();
		int progress = PartnerManager.inst().hasNumInLevel(humanObj, target);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_17);
	}

	/**
	 * 伙伴突破18
	 * @param param
	 */
	@Listener(EventKey.PartnerAdvanced)
	public void _listener_PartnerAdvanced(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_18);
		int achieveSn = achievement.getAchieveSn();
		int uniqueSn = achievement.getUniqueSn();
		int curParam = getCurParamByUniqueSn(achieveSn, uniqueSn);
		if (curParam < 0) {
			return;
		}
		// 特殊类型,达到条件的个数
		int progress = PartnerManager.inst().hasNumAdvanced(humanObj, curParam);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_18);
	}

	/**
	 * 伙伴升星19
	 * @param param
	 */
	@Listener(EventKey.PartnerStartUp)
	public void _listener_PartnerStartUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_19);
		int achieveSn = achievement.getAchieveSn();
		int uniqueSn = achievement.getUniqueSn();
		int curParam = getCurParamByUniqueSn(achieveSn, uniqueSn);
		if (curParam < 0) {
			return;
		}
		// 特殊类型,达到条件的个数
		int progress = PartnerManager.inst().hasNumStart(humanObj, curParam);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_19);
	}

	/**
	 * 装备进阶20
	 * @param param
	 */
	@Listener(EventKey.EquipAdvanced)
	public void _listener_EquipAdvanced(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_20);
		int achieveSn = achievement.getAchieveSn();
		int uniqueSn = achievement.getUniqueSn();
		int curParam = getCurParamByUniqueSn(achieveSn, uniqueSn);
		if (curParam < 0) {
			return;
		}
		// 特殊类型,达到条件的个数
		int progress = ItemBodyManager.inst().getEquipNumByAdvancedLv(humanObj, curParam);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_20);
	}

	/**
	 * 装备强化21
	 * @param param
	 */
	@Listener(EventKey.EquipIntensify)
	public void _listener_EquipIntensify(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_21);
		int achieveSn = achievement.getAchieveSn();
		int uniqueSn = achievement.getUniqueSn();
		int curParam = getCurParamByUniqueSn(achieveSn, uniqueSn);
		if (curParam < 0) {
			return;
		}
		// 特殊类型,达到条件的个数
		int progress = ItemBodyManager.inst().getEquipNumByReinforceLv(humanObj, curParam);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_21);
	}

	/**
	 * 抽卡22 23
	 * @param param
	 */
	@Listener(EventKey.DrawCard)
	public void _listener_DrawCard(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		int type = Utils.getParamValue(param, "type", 0);
		int num = Utils.getParamValue(param, "num", 1);
		switch (type) {
		case EDrawType.BySummonToken_VALUE:
			achieveUpdate(humanObj, num, AchievementTypeKey.ACHIEVEMENT_TYPE_22);
			break;
		case EDrawType.ByGold_VALUE:
			achieveUpdate(humanObj, num, AchievementTypeKey.ACHIEVEMENT_TYPE_23);
			break;

		}
	}

	/**
	 * 消耗货币24 25
	 * @param param
	 */
	@Listener(EventKey.HumanMoneyReduce)
	public void _listener_HumanMoneyReduce(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		long num = Utils.getParamValue(param, "num", 0l);
		int type = Utils.getParamValue(param, "type", 0);
		switch (type) {
		case EMoneyType.gold_VALUE:
			achieveUpdate(humanObj, (int) num, AchievementTypeKey.ACHIEVEMENT_TYPE_24);
			break;
		case EMoneyType.coin_VALUE:
			achieveUpdate(humanObj, (int) num, AchievementTypeKey.ACHIEVEMENT_TYPE_25);
			break;
		}
	}

	/**
	 * 技能修炼 26
	 * @param param
	 */
	@Listener(EventKey.HumanSkillTrainSave)
	public void _listener_HumanSkillTrain(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_26);
		int target = achievement.getTarget();
		int progress = humanObj.humanSkillRecord.getAmountBySkillStage(target);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_26);
	}

	/**
	 * 装备精炼 27
	 * @param param
	 */
	@Listener(EventKey.EquipRefineUp)
	public void _listener_EquipRefineUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_27);
		int achieveSn = achievement.getAchieveSn();
		int uniqueSn = achievement.getUniqueSn();
		int curParam = getCurParamByUniqueSn(achieveSn, uniqueSn);
		if (curParam < 0) {
			return;
		}
		// 特殊类型,达到条件的个数
		int progress = ItemBodyManager.inst().getEquipNumByRefineLv(humanObj, curParam);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_27);
	}

	/**
	 * 爆点升星 28
	 * @param param
	 */
	@Listener(EventKey.GodsStarUp)
	public void _listener_GodsStarUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_28);
		int target = achievement.getTarget();
		int progress = humanObj.humanSkillRecord.getAmountByGodsStar(target);
		achieveUpdate(humanObj, progress, AchievementTypeKey.ACHIEVEMENT_TYPE_28);
	}

	/**
	 * 炼妖塔通关 29
	 */
	@Listener(EventKey.TowerPass)
	public void _listener_TowerPass(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		Achievement achievement = GetAchieveByType(humanObj, AchievementTypeKey.ACHIEVEMENT_TYPE_29);
		// 特殊类型，需要过关的难度层数
		int target = achievement.getTarget();
		// 判断是否过关
		if (TowerManager.inst().getAlreadyFight(humanObj, target)) {
			// 过关了则更新进度
			achieveUpdate(humanObj, target, AchievementTypeKey.ACHIEVEMENT_TYPE_29);
		}
	}


	/**
	 * 提交任务
	 * @param humanObj
	 */
	public void _msg_CSCommitAchievement(HumanObject humanObj, int achieveSn) {
		// 没有这个成就的配置
		ConfAchievement confAchieve = ConfAchievement.get(achieveSn);
		if (confAchieve == null) {
			Log.table.error("===ConfAchievement no find sn={}", achieveSn);
			return;
		}

		List<Achievement> achieveList = humanObj.achievements;
		// 目标任务
		Achievement achieve = null;
		// 遍历所有的任务，找到目标任务
		for (Achievement tempOne : achieveList) {
			if (tempOne.getAchieveSn() == achieveSn) {
				achieve = tempOne;
				break;
			}
		}

		// 成就没有找到
		if (achieve == null) {
			humanObj.sendSysMsg(205501);// 成就不存在！
			return;
		}

		// 如果成就已经完结
		if (achieve.getStatus() == EAchievementStatus.AchievementFinished_VALUE) {
			humanObj.sendSysMsg(205502);// 成就已完结
			return;
		}

		// 找到成就的配置
		IAchievementTypeData achieveData = AchievementTypeDataFactory.getTypeData(confAchieve.type);
		if (!achieveData.onCheck(humanObj, achieve)) {
			humanObj.sendSysMsg(205503);// 成就未完成！
			return;
		}
		// 成就唯一sn
		int achieveUniqueSn = achieve.getUniqueSn();
		ConfAchievementType confAchiType = ConfAchievementType.get(achieveUniqueSn);
		if (confAchiType == null) {
			Log.table.error("=== AchievementType配置表错误 ,no find sn={} ===", achieveUniqueSn);
			return;
		}
		int rewardSn = confAchiType.reward;
		ConfRewards confRewards = ConfRewards.get(rewardSn);
		if (confRewards == null) {
			Log.table.error("=== Rewards配置表错误 ,no find sn={} ===", rewardSn);
			return;
		}
		// 完成了，发奖励，通知前端
		ItemChange itemChange = RewardHelper.reward(humanObj, confRewards.itemSn, confRewards.itemNum, LogSysModType.Achievement);
		// 更新任务状态
		achieve.setUpdateTime(Port.getTime());
		// 发送给前端完成信息
		SCCommitAchievement.Builder msg = SCCommitAchievement.newBuilder();
		msg.setSn(achieveSn);
		msg.setResult(true);
		msg.addAllDItem(QuestDailyManager.inst().createDItems(itemChange.getProduce()));
		humanObj.sendMsg(msg);

		// 发送成就任务状态为领取
		LogOpUtils.sendQuestLog(humanObj, achieveUniqueSn, 0);

		// 切换到下一档任务或者全部完成后关闭
		if (false == achieveData.lvUp(humanObj, achieve, confAchieve)) {
			achieve.setStatus(EAchievementStatus.AchievementFinished_VALUE);
		}
		// 刷新成就列表
		this.sendMsg(humanObj, achieveList);
		// 成就完成和当前成就对应的param
		Event.fire(EventKey.AchievementComplete, "humanObj", humanObj, "sn", achieveSn, "param", achieveUniqueSn);
	}

	/**
	 * 向前端发送一个任务信息
	 *
	 * @param humanObj
	 */
	public void sendMsg(HumanObject humanObj, Achievement achieve) {
		SCAchievementInfo.Builder msg = SCAchievementInfo.newBuilder();
		DAchievement dAchievement = this.getDAchievement(achieve);
		msg.addInfos(dAchievement);

		// 发送消息
		humanObj.sendMsg(msg);
	}

	/**
	 * 向前端发送多个成就信息
	 * @param humanObj
	 */
	public void sendMsg(HumanObject humanObj, Collection<Achievement> achieveList) {
		SCAchievementInfo.Builder msg = SCAchievementInfo.newBuilder();
		for (Achievement achieve : achieveList) {
			msg.addInfos(this.getDAchievement(achieve));
		}
		// 发送消息
		humanObj.sendMsg(msg);
	}

	/**
	 * 构建所有的DAchievement结构给客户端
	 * @param humanObj
	 * @return
	 */
	public List<DAchievement> getQuestMsg(HumanObject humanObj) {
		List<DAchievement> result = new ArrayList<>();
		for (Achievement achieve : humanObj.achievements) {
			result.add(this.getDAchievement(achieve));
		}
		return result;
	}

	/**
	 * 构建单个DAchievement
	 *
	 * @return
	 */
	private DAchievement getDAchievement(Achievement achieve) {
		// 根据字段构建结构体
		DAchievement.Builder msg = DAchievement.newBuilder();
		msg.setSn(achieve.getAchieveSn());
		msg.setTargetProgress(achieve.getTarget());
		msg.setNowProgress(achieve.getProgress());
		msg.setLv(achieve.getAchieveLv());
		msg.setUniqueSn(achieve.getUniqueSn());
		msg.setStatus(EAchievementStatus.valueOf(achieve.getStatus()));
		msg.setUpdateTime(achieve.getUpdateTime());
		msg.setType(achieve.getAchieveType());
		return msg.build();
	}

	/**
	 * 更新成就完成度，判断是否是达到任务条件自动完成的类型
	 * @param param
	 */
	/*
	public void achieveUpdate(HumanObject humanObj, int progress, int type) {
		// 判断是否有任务变化
		boolean change = false;
		// // 发送给客户端的列表
		// List<Achievement> changeList = new ArrayList<>();

		// 取出任务，挨个遍历，匹配类型
		List<Achievement> achieveList = humanObj.achievements;
		for (Achievement temp : achieveList) {
			// 任务类型不对，跳过
			if (temp.getAchieveType() != type)
				continue;

			// 处理任务进度
			IAchievementTypeData typeData = AchievementTypeDataFactory.getTypeData(type);
			boolean changeTemp = typeData.doProgress(humanObj, temp, progress);

			// 加入到发送列表
			if (changeTemp) {
				change = true;
			}
		}

		// 如果有变化，给客户端全发
		if (change) {
			this.sendMsg(humanObj, achieveList);
		}
	}
	*/
	
	/**
	 *  更新成就完成度，判断是否是达到任务条件自动完成的类型
	 * @param humanObj
	 * @param progress
	 * @param type
	 */
	public void achieveUpdate(HumanObject humanObj, int progress, int type) {
		// 判断是否有任务变化
		boolean change = false;
		// // 发送给客户端的列表
		// List<Achievement> changeList = new ArrayList<>();

		// 取出任务，挨个遍历，匹配类型
		List<Achievement> achieveList = humanObj.achievements;
		Achievement achievement = null;
		for (Achievement achieve : achieveList) {
			// 任务类型不对，跳过
			if (achieve.getAchieveType() != type)
				continue;
			achievement = achieve;
			// 处理任务进度
			IAchievementTypeData typeData = AchievementTypeDataFactory.getTypeData(type);
			boolean changeTemp = typeData.doProgress(humanObj, achieve, progress);

			// 加入到发送列表
			if (changeTemp) {
				change = true;
			}
		}

		// 如果有变化，给客户端全发
		if (change) {
			this.sendMsg(humanObj, achievement);
		}
	}

	/**
	 * 玩家升级的时候，检查新增成就
	 * @param param
	 */
	@Listener(EventKey.HumanLvUp)
	public void onHumanLevelUp(Param param) {
		HumanObject humanObj = param.get("humanObj");
		int level = humanObj.getHuman().getLevel();
		// 取出所有的配置
		List<ConfAchievement> confList = new ArrayList<>();
		confList.addAll(ConfAchievement.findAll());
		boolean change = false;
		// 挨个遍历配置，并且初始化
		for (ConfAchievement conf : confList) {
			// 去掉暂未开放的成就
			if (conf.achievementSn.length == 1 && conf.achievementSn[0] == 0)
				continue;
			// 等级不满足的任务不开放
			if (conf.needLv > level)
				continue;
			// 已经存在的成就不处理
			Achievement achieve = GetAchieveBySn(humanObj, conf.sn);
			if (achieve != null)
				continue;
			// 初始化新开放的任务
			IAchievementTypeData typeData = AchievementTypeDataFactory.getTypeData(conf.type);
			achieve = typeData.init(humanObj.getHuman().getId(), conf); // 持久化
			humanObj.achievements.add(achieve); // 加入在线内存管理
			change = true;
		}
		// 有变化，给客户端全发
		if (change) {
			this.sendMsg(humanObj, humanObj.achievements);
		}
	}

	/**
	 * 查找指定Sn的成就
	 *
	 * @param humanObj
	 * @param achieveId
	 */
	public Achievement GetAchieveBySn(HumanObject humanObj, int achieveId) {
		List<Achievement> achieveList = humanObj.achievements;
		// 遍历所有的任务，找到目标任务
		for (Achievement achieve : achieveList) {
			if (achieve.getAchieveSn() == achieveId) {
				return achieve;
			}
		}
		return null;
	}

	/**
	 * 查找指定type的成就
	 * @param humanObj
	 */
	public Achievement GetAchieveByType(HumanObject humanObj, int type) {
		List<Achievement> achieveList = humanObj.achievements;
		// 遍历所有的任务，找到目标任务
		for (Achievement achieve : achieveList) {
			if (achieve.getAchieveType() == type) {
				return achieve;
			}
		}
		return null;
	}

	/**
	 * 获取当前任务的参数
	 * @param sn
	 * @param uniqueSn
	 * @return
	 */
	public int getCurParamByUniqueSn(int sn, int uniqueSn) {
		ConfAchievementType conf = ConfAchievementType.get(uniqueSn);
		if (conf == null) {
			Log.table.error("=== 配置表Achievement错误,sn={}", sn);
			return -1;
		}
		int[] param = conf.param;
		return param[1];
	}
}
