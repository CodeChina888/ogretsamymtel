package game.worldsrv.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;
import game.msg.Define.DItem;
import game.msg.Define.DProduce;
import game.msg.Define.EDrawType;
import game.msg.Define.EMoneyType;
import game.msg.Define.EQuestDailyStatus;
import game.msg.Define.EShopType;
import game.msg.MsgQuest.SCCommitQuestDaily;
import game.msg.MsgQuest.SCGetLivenessRewardResult;
import game.msg.MsgQuest.SCLivenessInfoChange;
import game.msg.MsgQuest.SCQuestDailyInfo;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfQuestDaily;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Quest;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.QuestDailyType;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.LogOpUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

/**
 * 每日任务的管理器
 */
/**
 * @author Administrator
 *
 */
public class QuestDailyManager extends ManagerBase {
	
	private static final int _99 = 99;//活跃度类型
	private static final int L_100000 = 100000;
	private static final int INSTNORMAL = 1;
	private static final int INSTHARD = 2;
	private static final int L_1000 = 1000;

	/**
	 * 获取实例
	 * @return
	 */
	public static QuestDailyManager inst() {
		return inst(QuestDailyManager.class);
	}
	/**
	 * 玩家其它数据加载开始：加载玩家的任务信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		
		DB dbPrx = DB.newInstance(Quest.tableName);
		dbPrx.get(humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanQuest, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanQuest(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanQuest humanObj is null");
			return;
		}
		
		Record record = results.get();
		if (record == null) {
			Log.game.error("===_result_loadHumanQuest record is null");
		} else {
			// 加载数据
			Quest quest = new Quest(record);
			humanObj.questRecord.init(quest);
			
			if (humanObj.isDailyFirstLogin) {
				// 每日重置
				resetDailyQuest(humanObj);// 重置每日任务
			} else {
				_send_SCQuestDailyInfo(humanObj);
				_send_SCLivenessInfoChange(humanObj);
			}
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}	
	
	
	/**
	 * 初始化每日任务
	 */
	@Listener(EventKey.HumanCreate)
	public void initQuestDaily(Param param){
		Human human = param.get("human");
		Map<Integer, QuestJSON> dailyMap = new HashMap<>();
		Quest quest = new Quest();
		quest.setId(human.getId());
		quest.setName(human.getName());
		// 重新接受每日任务
		for (ConfQuestDaily confDaily : ConfQuestDaily.findAll()) {
			if (confDaily.needLv > human.getLevel()) {// 等级不足
				continue;
			}
			int status = EQuestDailyStatus.Doing_VALUE;// 接受任务
			// 新建每日任务数据
			QuestJSON q = new QuestJSON(confDaily.sn, 0, status);
			dailyMap.put(q.sn, q);
		}
		quest.setDailyJSON(QuestJSON.mapToJSON(dailyMap));
		quest.persist();
		
		
//		humanObj.questRecord.init(quest);
//		_send_SCQuestDailyInfo(humanObj);
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
			resetDailyQuest(humanObj);// 重置每日任务
		}

	}
 
	/**
	 * 用户升级事件监听
	 */
	@Listener(EventKey.HumanLvUp)
	public void _listener_HumanLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanLvUp humanObj is null");
			return;
		}
		List<QuestJSON> updateList = new ArrayList<>();
		for (ConfQuestDaily confDaily : ConfQuestDaily.findAll()) {
			if (confDaily.needLv > humanObj.getHuman().getLevel()) {// 等级不足
				continue;
			}
			if (humanObj.questRecord.containsDaily(confDaily.sn))
				continue;
			// 新建每日任务数据
			QuestJSON q = new QuestJSON(confDaily.sn, 0, EQuestDailyStatus.Doing_VALUE);
			updateList.add(q);
		}
		// 更新数据到人物的任务列表
		humanObj.questRecord.addDaily(updateList);
		_send_SCQuestDailyInfo(humanObj, updateList);// 下发每日任务信息
	}
	/**
	 * 重置每日任务
	 * @param humanObj
	 */
	public void resetDailyQuest(HumanObject humanObj) {
		if (humanObj.questRecord == null)
			return;
		// 清空玩家每日任务记录，清空活跃度
		humanObj.questRecord.clearDaily();
		humanObj.getHuman().setDailyQuestLiveness(0);		// 每日活跃度，清零
		humanObj.getHuman().setDailyLivenessReward("");		// 每日活跃度奖品，清空
		//isMonDayToClearWeekLiveness(humanObj);// 每周任务活跃度，需每周清
		// 重新接受每日任务
		List<QuestJSON> allList = new ArrayList<>();
		for (ConfQuestDaily confDaily : ConfQuestDaily.findAll()) {
			if (confDaily.needLv > humanObj.getHuman().getLevel()) {// 等级不足
				continue;
			}
			int status = EQuestDailyStatus.Doing_VALUE;// 接受任务
			// 特性情况：Vip奖励
//			if (confDaily.type == QuestDailyType.VIPAward.value()) {
//				if (confDaily.progress <= humanObj.getHuman().getVipLevel()) {
//					status = EQuestDailyStatus.Completed_VALUE;
//				}
//			}
			// 新建每日任务数据
			QuestJSON q = new QuestJSON(confDaily.sn, 0, status);
			allList.add(q);
		}
		// 更新数据到人物的任务列表
		humanObj.questRecord.addDaily(allList);
		_send_SCQuestDailyInfo(humanObj, allList);// 下发每日任务信息
		_send_SCLivenessInfoChange(humanObj);// 下发活跃度变化
	}

	
	
	/**
	 *每日任务1,2：通关任意副本 
	 */
	@Listener(EventKey.InstAnyPass)
	public void _listener_RepPassAny(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_RepNormalPass humanObj is null");
			return;
		}
		int num = Utils.getParamValue(param, "num", 1);
		int stageSn = Utils.getParamValue(param, "stageSn", 1);
		int instType =  stageSn/L_100000;
		
		if(instType == INSTNORMAL){
			addAndSave(humanObj, QuestDailyType.RepPassNormal,num);
		} else if(instType == INSTHARD){
			addAndSave(humanObj, QuestDailyType.RepPassHard,num);
		}
	}
	/**
	 *每日任务3：竞技场挑战 
	 */
	@Listener(EventKey.CompeteStart)
	public void _listener_CompeteStart(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_CompeteStart humanObj is null");
			return;
		}
		
		addAndSave(humanObj, QuestDailyType.CompeteFight,1);
	}
	/**
	 *每日任务4：技能升級
	 */
	@Listener(EventKey.HumanSkillUp)
	public void _listener_HumanSkillUp(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanSkillUp humanObj is null");
			return;
		}
		int num = Utils.getParamValue(param, "num", 1);
		addAndSave(humanObj, QuestDailyType.SkillUp,num);
	}
	/**
	 *每日任务5：购买体力
	 */
	@Listener(EventKey.DailyActBuy)
	public void _listener_DailyActBuy(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_DailyActBuy humanObj is null");
			return;
		}
		int num = Utils.getParamValue(param, "num", 1);
		addAndSave(humanObj, QuestDailyType.ActValueBuy,num);
	}
	
	/**
	 *每日任务6：购买铜币
	 */
	@Listener(EventKey.DailyCoinBuy)
	public void _listener_DailyCoinBuy(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_DailyCoinBuy humanObj is null");
			return;
		}
		int num = Utils.getParamValue(param, "num", 1);
		addAndSave(humanObj, QuestDailyType.CoinBuy,num);
	}
	/**
	 *每日任务7：使用元宝
	 */
	@Listener(EventKey.HumanMoneyReduce)
	public void _listener_HumanMoneyReduce(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		int type = Utils.getParamValue(param, "type", 0);
		if (EMoneyType.gold_VALUE == type) {
			addAndSave(humanObj, QuestDailyType.GoldConsume, 1);
		}
	}

	
	/**
	 *每日任务8-11: 商店购买
	 */
	@Listener(EventKey.ActShopBuy)
	public void _listener_ActShopBuy(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("_listener_ActShopBuy humanObj is null");
			return;
		}
		int num = Utils.getParamValue(param, "num", 1);
		int shopType = Utils.getParamValue(param, "shopType", 1);
		switch (shopType) {
		case EShopType.ShopGold_VALUE:
			addAndSave(humanObj, QuestDailyType.GoldShopBuy,num);
			break;
		case EShopType.ShopArena_VALUE:
			addAndSave(humanObj, QuestDailyType.CompeteShopBuy,num);
			break;
		case EShopType.ShopGeneral_VALUE:
			addAndSave(humanObj, QuestDailyType.GeneralShopBuy,num);
			break;
		case EShopType.ShopTower_VALUE:
			addAndSave(humanObj, QuestDailyType.TowerShopBuy,num);
			break;
		}
	}
	
	/**
	 *每日任务12: 挑战爬塔
	 */
	@Listener(EventKey.TowerEnter)
	public void _listener_TowerEnter(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		addAndSave(humanObj, QuestDailyType.TowerFight,1);
	}
	
	/**
	 *每日任务13: 参与世界boss
	 */
	@Listener(EventKey.ActInstWorldBossEnter)
	public void _listener_WorldBoss(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		addAndSave(humanObj, QuestDailyType.WorldBoss, 1);
	}
	
	/**
	 * 每日任务14：参与洞天福地
	 */
	@Listener(EventKey.LootMapEnter)
	public void _listener_JoinLootMap(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		addAndSave(humanObj, QuestDailyType.LootMap, 1);
	}
	
	/**
	 *每日任务15,16: 伙伴招募
	 */
	@Listener(EventKey.DrawCard)
	public void _listener_DrawCard(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		int type = Utils.getParamValue(param, "type", 0);
		int num = Utils.getParamValue(param, "num", 1);
		switch (type) {
		case EDrawType.BySummonToken_VALUE:
			addAndSave(humanObj, QuestDailyType.RecruitGeneral,num);
			break;
		case EDrawType.ByGold_VALUE:
			addAndSave(humanObj, QuestDailyType.RecruitAdvanced,num);
			break;

		}
	}
	
	/**
	 * 每日任务17：神兽升级培养
	 * @param param
	 */
	@Listener(EventKey.GodsLvPractice)
	public void _listener_GodsLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		addAndSave(humanObj, QuestDailyType.GodsLvUp, 1);
	}
	
	/**
	 * 每日任务18：技能修炼
	 * @param param
	 */
	@Listener(EventKey.HumanSkillTrain)
	public void _listener_HumanSkillTrain(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		addAndSave(humanObj, QuestDailyType.SkillTrain, 1);
	}
	
	/**
	 * 每日任务19：装备精炼
	 * @param param
	 */
	@Listener(EventKey.EquipRefine)
	public void _listener_EquipRefine(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		addAndSave(humanObj, QuestDailyType.EquipRefine, 1);
	}
	
	/**
	 * 每日任务20：玩家切磋
	 * @param param
	 */
	@Listener(EventKey.PKMirrorFightNum)
	public void _listener_MainCityFightNum(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		addAndSave(humanObj, QuestDailyType.MainCityFight, 1);
	}
	
	/**
	 * 提交每日任务，即完成任务领取奖励
	 */
	public void _msg_CSCommitQuestDaily(HumanObject humanObj, int sn) {
		QuestJSON questJSON = humanObj.questRecord.getDailyBy(sn);
		if (questJSON == null)
			return;
		ConfQuestDaily conf = ConfQuestDaily.get(questJSON.sn);
		if (conf == null)
			return;
		
		// 是否为完成状态
		if (questJSON.status == EQuestDailyStatus.Completed_VALUE) {
			// 完成并下发奖励
			finishAndGiveReward(humanObj, questJSON, conf);
		}
	}
	
	/**
	 * 完成并下发奖励
	 */
	private void finishAndGiveReward(HumanObject humanObj, QuestJSON questJSON, ConfQuestDaily conf) {
		if (conf == null)
			return;
		int rewardSn = conf.reward;
		ConfRewards confRewards = ConfRewards.get(rewardSn);
		if(confRewards == null){
			Log.table.error("===Rewards配置表错误 ,no find sn={}",rewardSn);
			return;
		}
		int itemSn = 0; 
		int itemNum = 0;
		int[] numAry = new int[confRewards.itemSn.length];
		for (int i = 0; i < confRewards.itemSn.length; i++) {
			itemSn = confRewards.itemSn[i];
			itemNum = confRewards.itemNum[i]; 
			// 经验 = 当前等级 * 经验系数
			if (itemSn == EMoneyType.exp_VALUE) {
				itemNum = humanObj.getHuman().getLevel() * itemNum;
			}
			numAry[i] = itemNum;
		}
		//  下发奖励
		ItemChange itemChange = RewardHelper.reward(humanObj,confRewards.itemSn, numAry, LogSysModType.QuestDaily);
		
		// 记录已领奖状态
		questJSON.status = EQuestDailyStatus.Rewarded_VALUE;
		// 更新数据到人物的任务列表
		humanObj.questRecord.modifyDaily(questJSON);
		_send_SCCommitQuestDaily(humanObj, questJSON, itemChange.getProduce());// 下发每日任务信息
//		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
//		itemProduce.addAll(ProduceManager.inst().produceItem(conf.produceType, conf.produceNum));
//		ProduceManager.inst().giveProduceItem(humanObj, itemProduce, LogSysModType.QuestDaily);
		
		// 处理活跃度
		Human human = humanObj.getHuman();
		// 增加每日任务活跃度
		human.setDailyQuestLiveness(human.getDailyQuestLiveness() + conf.activeValue);
		// 增加每周活跃度
//		human.setWeeklyQuestLiveness(human.getWeeklyQuestLiveness() + conf.liveness);
		if(conf.activeValue > 0){
			_send_SCLivenessInfoChange(humanObj);//发送活跃度变化
		}
		checkLivenessInfoState(humanObj);
		
		// 发送任务状态为领取
		LogOpUtils.sendQuestLog(humanObj, questJSON.sn, 0);
	}
	

	
	/**
	 * 判断修改活跃度
	 */
	private void checkLivenessInfoState(HumanObject humanObj) {
		int dailyQuestLiveness = humanObj.getHuman().getDailyQuestLiveness();
		List<QuestJSON> allList = new ArrayList<>();
		List<ConfQuestDaily> list = ConfQuestDaily.findBy("type",_99);
		if(list.isEmpty()){
			Log.table.error("=== 配置表QuestDaily错误type={}",_99);
		}
		for (ConfQuestDaily confQuestDaily : list) {
			if (confQuestDaily == null)
				continue;
			QuestJSON dailyBy = humanObj.questRecord.getDailyBy(confQuestDaily.sn);
			//满足条件且是正在任务中的任务
			if (dailyBy.status != EQuestDailyStatus.Rewarded_VALUE && dailyQuestLiveness >= confQuestDaily.param) {
				dailyBy.status = EQuestDailyStatus.Completed_VALUE;
			}
			// 更新数据到人物的任务列表
			humanObj.questRecord.modifyDaily(dailyBy);
			allList.add(dailyBy);
		}
		_send_SCQuestDailyInfo(humanObj, allList);// 下发下发活跃度任务
		
	}
	private void _send_SCCommitQuestDaily(HumanObject humanObj, QuestJSON questJSON, List<DProduce> produces) {
		SCCommitQuestDaily.Builder msg = SCCommitQuestDaily.newBuilder();
		msg.setQuest(questJSON.createDQuestDaily());
		msg.addAllDItem(createDItems(produces));
		humanObj.sendMsg(msg);
	}
	/**
	 * 增加进度并保存数据，指定类型的进度+1
	 */
	private void addAndSave(HumanObject humanObj, QuestDailyType type) {
		addAndSave(humanObj, type, 1);
	}	

	/**
	 * 增加进度并保存数据，指定类型的进度+n
	 */
	private void addAndSave(HumanObject humanObj, QuestDailyType type, int progress) {
		QuestJSON questJSON = humanObj.questRecord.getDailyBy(type);
		if (questJSON == null || questJSON.status == EQuestDailyStatus.Completed_VALUE || 
				questJSON.status == EQuestDailyStatus.Rewarded_VALUE || questJSON.status == EQuestDailyStatus.Discontented_VALUE)
			return;
		ConfQuestDaily conf = ConfQuestDaily.get(questJSON.sn);
		if (conf == null)
			return;
		
		// 增加进度
		questJSON.nowProgress += progress;
		//满足条件且是正在任务中的任务
		if (questJSON.nowProgress >= conf.param) {
			questJSON.status = EQuestDailyStatus.Completed_VALUE;
			// 发送任务状态为完成
			LogOpUtils.sendQuestLog(humanObj, questJSON.sn, 1);
		}
		// 更新数据到人物的任务列表
		humanObj.questRecord.modifyDaily(questJSON);
		_send_SCQuestDailyInfo(humanObj, questJSON);// 下发每日任务信息
	}
	
	/**
	 * 下发所有每日任务信息
	 */
	public void _send_SCQuestDailyInfo(HumanObject humanObj) {
		SCQuestDailyInfo.Builder msg = SCQuestDailyInfo.newBuilder();
		List<QuestJSON> questJSONList = humanObj.questRecord.getDailyList();
		for (QuestJSON questJSON : questJSONList) {
			msg.addQuest(questJSON.createDQuestDaily());
		}
		humanObj.sendMsg(msg);
	}
	/**
	 * 下发单条每日任务信息
	 */
	private void _send_SCQuestDailyInfo(HumanObject humanObj, QuestJSON questJSON) {
		SCQuestDailyInfo.Builder msg = SCQuestDailyInfo.newBuilder();
		msg.addQuest(questJSON.createDQuestDaily());
		humanObj.sendMsg(msg);
	}
	/**
	 * 下发多条每日任务信息
	 */
	private void _send_SCQuestDailyInfo(HumanObject humanObj, List<QuestJSON> questJSONList) {
		SCQuestDailyInfo.Builder msg = SCQuestDailyInfo.newBuilder();
		for (QuestJSON questJSON : questJSONList) {
			msg.addQuest(questJSON.createDQuestDaily());
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 发送活跃度变化
	 */
	public void _send_SCLivenessInfoChange(HumanObject humanObj){
		SCLivenessInfoChange.Builder msg = SCLivenessInfoChange.newBuilder();
		msg.setDailyQuestLiveness(humanObj.getHuman().getDailyQuestLiveness());// 每日任务活跃度，需每日清0
		msg.setWeeklyQuestLiveness(humanObj.getHuman().getWeeklyQuestLiveness());// 每周任务活跃度，需每周清
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 星期一4点清掉周活跃度
	 */
	private void isMonDayToClearWeekLiveness(HumanObject humanObj) {
		//注意固定点重置
		long timeBack = Utils.getOffByTimes(Port.getTime(), -ParamManager.dailyHourReset);
		long lastLogout =  Utils.getOffByTimes(humanObj.getHuman().getTimeLogout(), -ParamManager.dailyHourReset);
		int dayOfWeek = Utils.getDayOfWeek(timeBack);
		boolean isSameWeek = Utils.isSameWeek(lastLogout -Time.DAY,timeBack -Time.DAY);//java以周日为周的第一天，此处需转换
		if (dayOfWeek == 1 || !isSameWeek) {
			humanObj.getHuman().setWeeklyQuestLiveness(0);
			humanObj.getHuman().setWeeklyLivenessReward("");
		}
	}
	
	/**
	 * 领取活跃度奖励
	 */
	public void _msg_CSGetLivenessReward(HumanObject humanObj, int sn) {
		ConfQuestDaily conf = ConfQuestDaily.get(sn);
		if (conf == null) {
			Log.table.error("===QuestDaily配表错误，no find sn={}", sn);
			return;
		}

		QuestJSON dailyJson = humanObj.questRecord.getDailyBy(sn);
		if(dailyJson == null){
			humanObj.sendSysMsg(201401);
			return;
		}
		// 是否为完成状态
		if (dailyJson.status != EQuestDailyStatus.Completed_VALUE) {
			return;
		}

		// 记录已领奖状态
		dailyJson.status = EQuestDailyStatus.Rewarded_VALUE;
		// 更新数据到人物的任务列表
		humanObj.questRecord.modifyDaily(dailyJson);
		
		int rewardSn = conf.reward;
		ConfRewards confRewards = ConfRewards.get(rewardSn);
		if(confRewards == null){
			Log.table.error("===Rewards配置表错误 ,no find sn={}",rewardSn);
			return;
		}
		int itemSn = 0; 
		int itemNum = 0;
		int[] numAry = new int[confRewards.itemSn.length];
		for (int i = 0; i < confRewards.itemSn.length; i++) {
			itemSn = confRewards.itemSn[i];
			itemNum = confRewards.itemNum[i]; 
			// 经验 = 当前等级 * 经验系数
			if (itemSn == EMoneyType.exp_VALUE) {
				itemNum = humanObj.getHuman().getLevel() * itemNum;
			}
			numAry[i] = itemNum;
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, confRewards.itemSn, numAry, LogSysModType.QuestDailyLiveness);
		
		SCGetLivenessRewardResult.Builder msg = SCGetLivenessRewardResult.newBuilder();
		msg.setSn(sn);
		msg.setResult(true);
		msg.addAllDItem(createDItems(itemChange.getProduce()));
		humanObj.sendMsg(msg);
		
		
//		Human human = humanObj.getHuman();
//		String strReward = human.getDailyLivenessReward();
		// sn》20 是周活跃的
//		if (sn >= 20) {
//			strReward = human.getWeeklyLivenessReward();
//		}
//		List<Integer> rewardlist = Utils.strToIntList(strReward);
//		boolean isGet = false;
//		if (rewardlist != null && !rewardlist.isEmpty()) {
//			if (rewardlist.contains(sn)) {
//				isGet = true;
//			}
//		}
//
//		if (!isGet) {// 还未领取，则给予奖励，记录领取过了
//			if (sn >= 20) {
//				if (human.getWeeklyQuestLiveness() < conf.liveness) {
//					return;// 周活跃度不足
//				}
//			} else {
//				if (human.getDailyQuestLiveness() < conf.liveness) {
//					return;// 日活跃度不足
//				}
//			}
//			List<ProduceVo> itemProduce = ProduceManager.inst().produceItem(conf.produceSn);
//			// 判断是否可以给
//			ReasonResult rr = ProduceManager.inst().canGiveProduceItem(humanObj, itemProduce);
//			if (!rr.success) {
//				return;
//			}
//			// 实际给物品
//			ProduceManager.inst().giveProduceItem(humanObj, itemProduce,
//					LogSysModType.QuestDailyLiveness);
//			// 记录领取过了
//			rewardlist.add(sn);
//			strReward = Utils.intListToStr(rewardlist);
//			if (sn >= 20) {
//				human.setWeeklyLivenessReward(strReward);// 周活跃度奖励已领取记录，需每周清空
//			} else {
//				human.setDailyLivenessReward(strReward);// 活跃度奖励已领取记录，需每日清空
//			}
//			// 返回结果
//			SCGetLivenessRewardResult.Builder msg = SCGetLivenessRewardResult.newBuilder();
//			msg.setSn(sn);
//			msg.setResult(true);
//			humanObj.sendMsg(msg);
//		}
	}

	public List<DItem> createDItems(ConfRewards confRewards) {
		List<DItem> list = new ArrayList<>();
		int[] itemSn = confRewards.itemSn;
		int[] itemNum = confRewards.itemNum;
		for (int j = 0; j < itemSn.length; j++) {
			DItem.Builder db = DItem.newBuilder();
			db.setItemSn(itemSn[j]);
			db.setNum(itemNum[j]);
			list.add(db.build());
		}
		return list;
	}
	
	public List<DItem> createDItems(List<DProduce> produces) {
		List<DItem> list = new ArrayList<>();
		for (DProduce pro : produces) {
			DItem.Builder db = DItem.newBuilder();
			db.setItemSn(pro.getSn());
			db.setNum(pro.getNum());
			list.add(db.build());
		}
		return list;
	}
}