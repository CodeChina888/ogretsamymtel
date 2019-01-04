package game.worldsrv.humanSkill;

import game.msg.Define.DProduce;
import game.msg.Define.DSkill;
import game.msg.Define.DSkillGroup;
import game.msg.Define.DSkillRune;
import game.msg.Define.DSkillTrain;
import game.msg.Define.EInformType;
import game.msg.Define.ELevelAward;
import game.msg.Define.EModeType;
import game.msg.Define.EMoneyType;
import game.msg.MsgSkill.SCGodsUnlockByItem;
import game.msg.MsgSkill.SCSelectSkillGods;
import game.msg.MsgSkill.SCSkillGodsInfo;
import game.msg.MsgSkill.SCSkillGodsLvUp;
import game.msg.MsgSkill.SCSkillGodsStarUp;
import game.msg.MsgSkill.SCSkillGodsUnlock;
import game.msg.MsgSkill.SCSkillInfo;
import game.msg.MsgSkill.SCSkillInstall;
import game.msg.MsgSkill.SCSkillLvUp;
import game.msg.MsgSkill.SCSkillResetTrain;
import game.msg.MsgSkill.SCSkillRunePractice;
import game.msg.MsgSkill.SCSkillRuneUnlock;
import game.msg.MsgSkill.SCSkillSaveTrain;
import game.msg.MsgSkill.SCSkillStageUp;
import game.msg.MsgSkill.SCSkillTrain;
import game.msg.MsgSkill.SCSkillTrainMutiple;
import game.msg.MsgSkill.SCSkillTrainUnlock;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfLevelAward;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfSkill;
import game.worldsrv.config.ConfSkillAdvanced;
import game.worldsrv.config.ConfSkillGods;
import game.worldsrv.config.ConfSkillGodsLvUp;
import game.worldsrv.config.ConfSkillGodsStar;
import game.worldsrv.config.ConfSkillLvUp;
import game.worldsrv.config.ConfSkillRune;
import game.worldsrv.config.ConfSkillTrain;
import game.worldsrv.config.ConfSkillTrainRule;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanSkill;
import game.worldsrv.entity.MirrorHuman;
import game.worldsrv.enumType.FightPropName;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.inform.InformManager;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.ItemTypeKey;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.GlobalConfVal;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalc;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

/**
 * 
 * @author Neak
 * 2017.2.7 主角技能逻辑处理
 */
public class HumanSkillManager extends ManagerBase{
	
	// 培养抽奖槽位
	private static final int TRAIN_SLOTCOUNT = 3;
	
	public static HumanSkillManager inst() {
		return inst(HumanSkillManager.class);
	}
	
	/**
	 * 创角，初始化玩家技能
	 */
	@Listener(EventKey.HumanCreate)
	public void initHumanSkill(Param param) {
		Human human = param.get("human");
		
		// 玩家技能相关生成数据库条目
		HumanSkillRecord skillRecord = new HumanSkillRecord();
		HumanSkill skill = skillRecord.getHumanSkill();
		skill.setId(human.getId());
		// 创角时，判断解锁的技能
		List<Integer> skillTagList = processUnlockSkill(human, skillRecord, null);
		
		// 当前解锁槽位
		int unlockSlotCount = getUnlockSkillSlot(human);
		List<Integer> skillGroup = skillRecord.getSkillGroup();
		// 根据创角技能设置上阵技能
		for (Integer skillTag : skillTagList) {
			// 解锁槽位大于当前上阵技能数
			if(unlockSlotCount > skillGroup.size()) {
				// 自动上阵
				skillGroup.add(skillTag);
			}
		}
		// 设置当前上阵技能
		skillRecord.setSkillGroup(skillGroup);
		skill.persist();
		
//		UnitPropPlus unitPropPlus = param.get("unitPropPlus");
//		if (unitPropPlus == null) {
//			return;
//		}
//		// 设置被动技能属性加成
//		List<Integer> skills = Utils.strToIntList(human.getSkillAllSn());
//		PropCalc passivityPropCalc = abandon_calc_passivitySkillProp(skills);
//		unitPropPlus.setPassivitySkill(passivityPropCalc.toJSONStr());
	}
	
	/**
	 * 玩家其它数据加载开始：加载玩家的技能信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.skill.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(HumanSkill.tableName);
		dbPrx.get(humanObj.getHumanId());  // 一条
//		dbPrx.findBy(false, HumanSkill.K.HumanId, humanObj.id); // 多条
		dbPrx.listenResult(this::_result_loadHumanSkill, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	/**
	 * 根据代理接口处理返回的值，去加载技能数据
	 * @param results
	 * @param context
	 */
	private void _result_loadHumanSkill(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.skill.error("===_result_loadHumanPartner humanObj is null");
			return;
		}
		Record record = results.get();
		if (record == null) {
			Log.skill.error("===_result_loadHumanPartner records=null");
		} else {
			// 加载数据
			HumanSkill gen = new HumanSkill(record);
			humanObj.humanSkillRecord.init(gen);
			
			//同步全局信息
			this.updateSkillInfo(humanObj);
			
			// 下发玩家技能信息
			_send_SCSkillInfo(humanObj);
		}
		
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	
	/**
	 * 下发所有玩家技能信息，技能布阵情况，爆点信息
	 */
	private void _send_SCSkillInfo(HumanObject humanObj){
		// 技能信息
		SCSkillInfo.Builder skillMsg = SCSkillInfo.newBuilder();
		List<SkillJSON> skillJSONList = humanObj.humanSkillRecord.getSkillList();
		for (SkillJSON skillJSON : skillJSONList) {
			skillMsg.addSkillInfoSet(skillJSON.createDSkillInfo());
		}
		
		List<SkillTrainJSON> skillTrainList = humanObj.humanSkillRecord.getSkillTrainList();
		for (SkillTrainJSON skillTrainJSON : skillTrainList) {
			skillMsg.addSkillTrain(skillTrainJSON.createDSkill());
		}
		
		// 获得培养倍率
		int[] trainInfo = this.getTrainMutipleInfo(humanObj.humanSkillRecord.getTrainMutiple());
		// 消耗万份比倍率
		int costMutiple = trainInfo[0];
		// 获得倍率
		float gainMutiple = trainInfo[1] / Utils.F10000;
		
		// 获取培养抽奖结果
		List<DProduce> trainList = humanObj.humanSkillRecord.getSkillTrain();
		if (!trainList.isEmpty()) {
			DSkillTrain.Builder trainMsg = skillMsg.getTrainBuilder();
			trainMsg.addAllItemList(trainList);
			DProduce extraDp = this.getTrainExtra(trainList, gainMutiple);
			if (extraDp.getSn() != 0) {
				trainMsg.setExtraItem(extraDp);
			}
		}
		skillMsg.setMutiple(costMutiple);
		humanObj.sendMsg(skillMsg);
		
		// 上阵技能信息
		SCSkillInstall.Builder installMsg = SCSkillInstall.newBuilder();
		DSkillGroup.Builder skillGroup = installMsg.getSkillGroupBuilder();
		skillGroup.addAllSkillSet(humanObj.humanSkillRecord.getSkillGroup());
		humanObj.sendMsg(installMsg);
		
		// 爆点信息
		SCSkillGodsInfo.Builder godsMsg = SCSkillGodsInfo.newBuilder();
		List<SkillGodsJSON> skillGodsList = humanObj.humanSkillRecord.getSkillGodsList();
		for (SkillGodsJSON gods : skillGodsList) {
			godsMsg.addSkillGodsSet(gods.createDSkillGods());
		}
		godsMsg.setGodsSnInBattle(humanObj.humanSkillRecord.getInstallGods());
		humanObj.sendMsg(godsMsg);
	}
	
	/**
	 * 监听玩家升级，判断是否有技能可以解锁
	 */
	@Listener(EventKey.HumanLvUp)
	public void _listener_HumanLvUp(Param params) {
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		processUnlock(humanObj);
	}
	/**
	 * 监听玩家副本过关，判断是否有技能可以解锁
	 */
	@Listener(EventKey.InstFirstPass)
	public void _listener_PassInst(Param params) {
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		processUnlock(humanObj);
	}
	/**
	 * 监听玩家VIP升级，判断是否有技能可以解锁
	 */
	@Listener(EventKey.VipLvChange)
	public void _listener_VipLvUp(Param params) {
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		processUnlock(humanObj);
	}
	
	/**
	 * 获取技能json串中的技能数据
	 * @param skillJSON
	 * @return
	 */
	public List<SkillData> getSkillDataList(String skillJSON) {
		List<SkillData> list = new ArrayList<>();
		Map<Integer, Integer> map = Utils.jsonToMapIntInt(skillJSON);
		for(Entry<Integer, Integer> e : map.entrySet()){
			Integer sn = e.getKey();
			Integer lv = e.getValue();
			SkillData data = new SkillData(sn, lv);
			list.add(data);
		}
		return list;
	}
	
	
	/**
	 * 处理技能上阵
	 * @param humanObj 玩家对象
	 * @param skillGroup 上阵技能列表
	 */
	public void _msg_CSSkillInstalll(HumanObject humanObj, DSkillGroup skillGroup) {
		if (skillGroup.getSkillSetCount() == 0) {
			Log.skill.error("===_msg_CSSkillInstalll 没有上阵技能===");
			humanObj.sendSysMsg(290201); //错误码
			return;
		}
		
		List<Integer> tmpList = new ArrayList<>();
		
		//没有上阵技能的槽位
		int emptySlot = 0;  
		for (int i = 0; i < skillGroup.getSkillSetCount(); i++) {
			int tag = skillGroup.getSkillSet(i);
			// sn为0，则为空槽位
			if(tag == 0){
				emptySlot += 1;
			}
			SkillJSON sj = HumanSkillManager.inst().getSkillJSON(humanObj, tag);
			// 技能不存在
			if (sj == null){
				tmpList.add(0);
				continue;
			}
			tmpList.add(tag);
		}
		
		// 解锁的槽位数量
		int unlockSlot = getUnlockSkillSlot(humanObj.getHuman()); 
		
		// 最大槽位 - 空槽位 = 当前上阵的技能数
		if ((ParamManager.skillMaxInstall - emptySlot) > unlockSlot){          //上阵的技能数 > 解锁的技能槽位，则证明上阵的技能多余解锁槽位技能
			Log.skill.debug("===_msg_CSSkillInstalll 上阵技能超过解锁技能槽数量 ===");
			humanObj.sendSysMsg(290202); //错误码
			return;
		}
		
		// 设置当前上阵技能
		humanObj.humanSkillRecord.setSkillGroup(tmpList);
		
		SCSkillInstall.Builder msg = SCSkillInstall.newBuilder();
		msg.setSkillGroup(skillGroup);
		humanObj.sendMsg(msg);
		
		// 同步全局信息
		this.updateSkillList(humanObj);
	}
	
	/**
	 * 技能升级
	 * @param humanObj
	 * @param skillList
	 * @param bOneKey  是否一键升级
	 */
	public void _msg_CSSkillLvUp(HumanObject humanObj, List<DSkill> skillList, boolean bOneKey) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillLvUp, humanObj)) {
			Log.skill.error("===_msg_CSSkillUpgrade 升级技能未开放 ===");
			humanObj.sendSysMsg(290401);
			return;
		}	
//		int minLv = humanObj.humanSkillRecord.getSkillMinLv();
//		// 升级不加属性时注释
//		// 获取升级前当前解锁技能最低等级对应的技能奖励配置表
//		ConfLevelAward oldConf = getConfLevCorrelationBySkillLvSum(minLv); 
		
		// 升级次数
		int lvUpCount = 0;
		
		// 升级需要的总金额
		int totalCost = 0;
		// 升级成功的技能sn列表
		List<Integer> successSnList = new ArrayList<>();
		// 验证客户端升级结果
		for (DSkill dSkill : skillList) {
			int skillTag = dSkill.getSkillTag();
			SkillJSON skill = humanObj.humanSkillRecord.getSkillJSON(skillTag);
			if (skill == null) {
				Log.skill.error("===_msg_CSSkillUpgrade 技能{}对象为空 ===", skillTag);
				continue;
			}
			
			// 将要升级到的等级
			int willLv = dSkill.getLv();
			if (willLv <= skill.lv) {
				Log.skill.debug("===_msg_CSSkillUpgrade 无法降级curLv:{}, willLv:{}, tag:{}===", skill.lv, willLv, skillTag);
				continue;
			}
			
			// 将要升级到的技能等级配置表
			ConfSkillLvUp confWillLv = ConfSkillLvUp.get(ConfigKeyFormula.getSkillLvUpSn(skillTag, willLv));
			if (confWillLv == null) {
				if(!bOneKey){
					Log.skill.error("===_msg_CSSkillUpgrade 技能已经升级到最高等级 ===", skillTag);
					humanObj.sendSysMsg(290403);
				}
				continue;
			}
			// 主角等级不满足升级等级
			if (humanObj.getHuman().getLevel() < confWillLv.unLockLv) {
				if(!bOneKey){
					Log.skill.error("===_msg_CSSkillUpgrade 玩家等级不满足技能升级条件 ===", skillTag);
					humanObj.sendSysMsg(290404);
				}
				continue;
			}
			// 技能阶级不满足升级等级
//			if (skill.stage < confWillLv.skillstageQm) {
//				if(!bOneKey){
//					Log.skill.error("===_msg_CSSkillUpgrade 技能阶数不满足技能升级条件 ===", skillTag);
//					humanObj.sendSysMsg(290405);
//				}
//				continue;
//			}
			
			// 未升级前的等级
			int curLv = skill.lv;
			// 升级需要的金钱
			int needCoin = processSkillLvUpNeedCoin(skillTag, curLv, willLv);
			// 扣铜币
			if(!bOneKey) {
				if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.coin_VALUE, needCoin, LogSysModType.SkillUpgrade)) {
					break;
				}
			}
			
			// 升级总次数
			lvUpCount += (willLv - curLv);
			
			// 金币总额
			totalCost += needCoin;
			// 设置技能等级
			skill.setLv(willLv);
			// 添加成功的skillSn
			successSnList.add(skillTag);
			
			if (!bOneKey) {
				// 修改一个技能
				humanObj.humanSkillRecord.modifySkill(skill);
			}
		}
		
		// 如果消费数量为0，则证明没有技能被升级
		if (totalCost == 0) {
			if (bOneKey) {
				Log.skill.debug("===_msg_CSSkillUpgrade 没有可以升级的技能 ===");
				humanObj.sendSysMsg(290406);
			}
			return;
		}

		if (bOneKey) {
			// 修改全部技能
			humanObj.humanSkillRecord.modifyAllSkill();
			// 扣铜币
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.coin_VALUE, totalCost, LogSysModType.SkillOneKeyUpgrade)) {
				return;
			}
		}
		
//		minLv = humanObj.humanSkillRecord.getSkillMinLv();
//		// 获取当前解锁技能最低等级对应的技能奖励配置表
//		ConfLevelAward curConf = getConfLevCorrelationBySkillLvSum(minLv);
//		// 属性变化(升级不加属性)
//		if ( (oldConf == null && curConf != null) || (oldConf != null && curConf != null && oldConf.sn != curConf.sn) ) {
//		}
		
		// 技能升级，属性重新计算
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.Skill);
		
		// 发送技能升级
		Event.fire(EventKey.HumanSkillUp, "humanObj", humanObj, "num" ,lvUpCount);
		
		SCSkillLvUp.Builder msg = SCSkillLvUp.newBuilder();
		for (Integer skillTag : successSnList) {
			SkillJSON skill = humanObj.humanSkillRecord.getSkillJSON(skillTag);
			msg.addSkillSet(skill.createDSkill());
		} 
		humanObj.sendMsg(msg);
		
		// 同步全局信息
		this.updateSkillList(humanObj);
	}
	
	/**
	 * 主角技能升阶
	 * @param humanObj
	 * @param skillTag
	 */
	public void _msg_CSSkillStageUp(HumanObject humanObj, int skillTag) {
		HumanSkillRecord skillRecord = humanObj.humanSkillRecord;
		// 升阶的技能对象
		SkillJSON sj = skillRecord.getSkillJSON(skillTag);
		if (sj == null) {
			Log.skill.error("=== 进阶失败,技能不存在 skillTag:{}", skillTag);
			humanObj.sendSysMsg(290501);
			return;
		}
		// 当前阶数
		int nextSt = sj.stage + 1;
		ConfSkillAdvanced confNext = ConfSkillAdvanced.get(ConfigKeyFormula.getSkillAdvancedSn(skillTag, nextSt));
		if (confNext == null) {
			Log.skill.error("=== 进阶失败, 已满阶 skillTag:{}, nextSt:{}", skillTag, nextSt);
			humanObj.sendSysMsg(290502);
			return;
		}
		
		ConfSkillAdvanced confCur = ConfSkillAdvanced.get(ConfigKeyFormula.getSkillAdvancedSn(skillTag, sj.stage));
		if (confCur == null) {
			Log.skill.error("=== 进阶失败, 已满阶 skillTag:{}, nextSt:{}", skillTag, sj.stage);
			humanObj.sendSysMsg(290502);
			return;
		}
		if (sj.lv < confNext.skillLvQm) {
			Log.skill.debug("=== 进阶失败,技能等级不足 skillTag:{}, curLv:{}", skillTag, sj.lv);
			humanObj.sendSysMsg(290503);
			return;
		}
		// 消耗判断
		int[] cost = Utils.appendInt(confCur.costItem, EMoneyType.coin_VALUE);
		int[] costNum = Utils.appendInt(confCur.itemNumber, confCur.useCost);
		if (!RewardHelper.checkAndConsume(humanObj, cost, costNum, LogSysModType.skillStageUp)) {
			return;
		}
		// 设置阶级
		sj.stage = nextSt;
		skillRecord.modifySkill(sj);
		
		// 计算技能进阶加成属性
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.SkillStage);
		
		SCSkillStageUp.Builder msg = SCSkillStageUp.newBuilder();
		msg.setDSkill(sj.createDSkill());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 主角技能培养倍率
	 * @param humanObj
	 * @param mutiple 倍率（万分比）
	 */
	public void _msg_CSSkillTrainMutiple(HumanObject humanObj, int mutiple) {
		int[] info = null;
		boolean bRight = false;
		int lv = 0;
		int vip = 0;
		for (String rate : ParamManager.skillTrainCostRate) {
			info = Utils.strToIntArraySplit(rate);
			if (mutiple != info[0]) {
				continue;
			}
			if (info.length < 3) {
				break;
			}
			lv = info[2];
			vip = info[3];
			if (humanObj.getHuman().getLevel() < lv && humanObj.getHuman().getVipLevel() < vip) {
				Log.skill.error("=== VIP或等级不满足，无法更换倍率 ===");
				humanObj.sendSysMsg(292102);
				return;
			}
					
			bRight = true;
			break;
		}
		if (!bRight) {
			Log.skill.error("=== 配置错误，无法更换该倍率 ===");
			humanObj.sendSysMsg(292103);
			return;
		}
		
		// 设置倍率
		humanObj.humanSkillRecord.setTrainMutiple(mutiple);
		SCSkillTrainMutiple.Builder msg = SCSkillTrainMutiple.newBuilder();
		msg.setMutiple(mutiple);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 主角技能培养
	 * @param humanObj
	 * @param bOneKey 是否一键培养
	 */
	public void _msg_CSSkillTrain(HumanObject humanObj, boolean bOneKey) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillTrain, humanObj)) {
			Log.skill.error("===_msg_CSSkillUpgrade 技能培养未开放 ===");
			humanObj.sendSysMsg(292101);
			return;
		}
		// 获得培养倍率
		int[] trainInfo = this.getTrainMutipleInfo(humanObj.humanSkillRecord.getTrainMutiple());
		float costMutiple = trainInfo[0] / Utils.F10000;
		float gainMutiple = trainInfo[1] / Utils.F10000;
		
		// 所有神通
		int[] trainTypes = ParamManager.skillTrainType;
		// 获取未解锁，满级的技能培养对象对应的道具
		List<Integer> rmItems = this.getTrainFilterItemSnList(humanObj);
		if (rmItems.size() == trainTypes.length) {
			humanObj.sendSysMsg(294304);
			return;
		}
		
		// 获得消耗道具/数量
		int itemSn = ParamManager.skillTrainCost[0];
		int itemNum = (int) (ParamManager.skillTrainCost[1] * costMutiple);
		if(!RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.SkillTrain)) {
			return;
		}
		
		// 抽出的道具
		List<DProduce> trainList = new ArrayList<>();
		// 抽奖三次的结果
		for (int i = 1; i <= TRAIN_SLOTCOUNT; i++) {
			trainList.add(getOnceTrainResult(humanObj, gainMutiple, i, rmItems));
		}
		// 保存抽奖结果
		humanObj.humanSkillRecord.setSkillTrain(trainList);
		
		// 获得额外加成的DProduce
		DProduce extraDp = this.getTrainExtra(trainList, gainMutiple);
		
		SCSkillTrain.Builder msg = SCSkillTrain.newBuilder();
		msg.setIsOnekey(bOneKey);
		DSkillTrain.Builder stMsg = msg.getTrainListBuilder();
		stMsg.addAllItemList(trainList);
		// 额外奖励
		if (extraDp.getSn() != 0) {
			stMsg.setExtraItem(extraDp);
		}
		humanObj.sendMsg(msg);
		
		// 技能修炼事件
		Event.fire(EventKey.HumanSkillTrain, "humanObj", humanObj);
	}
	
	/**
	 * 重置技能培养
	 * @param humanObj
	 */
	public void _msg_CSSkillResetTrain(HumanObject humanObj, int resetIndex) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillTrain, humanObj)) {
			Log.skill.error("===_msg_CSSkillUpgrade 技能培养未开放 ===");
			humanObj.sendSysMsg(292101);
			return;
		}
		if (resetIndex >= TRAIN_SLOTCOUNT || resetIndex < 0) {
			Log.skill.error("===_msg_CSSkillResetTrain 重置槽位异常 ===");
			humanObj.sendSysMsg(292501);
			return;
		}
		
		// 获取抽奖结果
		List<DProduce> trainList = humanObj.humanSkillRecord.getSkillTrain();
		if (trainList.isEmpty()) {
			Log.skill.error("===_msg_CSSkillResetTrain 无抽奖结果，无法重置抽奖 ===");
			humanObj.sendSysMsg(292502);
			return;
		}
		// 获得培养倍率
		int[] trainInfo = this.getTrainMutipleInfo(humanObj.humanSkillRecord.getTrainMutiple());
		float costMutiple = trainInfo[0] / Utils.F10000;
		float gainMutiple = trainInfo[1] / Utils.F10000;
		
		// 获得消耗道具/数量
		int itemSn = ParamManager.skillTrainReset[0];
		int itemNum = (int) (ParamManager.skillTrainReset[1] * costMutiple);
		if(!RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.SkillTrainReset)) {
			return;
		}
		
		// 获取未解锁，满级的技能培养对象对应的道具
		List<Integer> rmItems = this.getTrainFilterItemSnList(humanObj);
		// 重置
		DProduce dp = this.getOnceTrainResult(humanObj, gainMutiple, resetIndex+1, rmItems);
		trainList.set(resetIndex, dp);
		// 保存抽奖结果
		humanObj.humanSkillRecord.setSkillTrain(trainList);
		
		// 获得额外加成的DProduce
		DProduce extraDp = this.getTrainExtra(trainList, gainMutiple);
		
		SCSkillResetTrain.Builder msg = SCSkillResetTrain.newBuilder();
		msg.setResetIndex(resetIndex);
		DSkillTrain.Builder stMsg = msg.getTrainListBuilder();
		stMsg.addAllItemList(trainList);
		// 额外奖励
		if (extraDp.getSn() != 0) {
			stMsg.setExtraItem(extraDp);
		}
		humanObj.sendMsg(msg);
		
		// 技能修炼事件
		Event.fire(EventKey.HumanSkillTrain, "humanObj", humanObj);
	}

	/**
	 * 保存使用培养结果
	 * @param humanObj
	 */
	public void _msg_CSSkillSaveTrain(HumanObject humanObj) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillTrain, humanObj)) {
			Log.skill.error("===_msg_CSSkillUpgrade 技能培养未开放 ===");
			humanObj.sendSysMsg(292101);
			return;
		}
		
		// 获取抽奖结果
		List<DProduce> trainList = humanObj.humanSkillRecord.getSkillTrain();
		if (trainList.isEmpty()) {
			Log.skill.error("===_msg_CSSkillResetTrain 无抽奖结果，无法保存 ===");
			humanObj.sendSysMsg(292701);
			return;
		}
		
		// 需要发送给客户端的skill信息
		HashSet<Integer> trainTypeSet = new HashSet<>();
		// 遍历
		for (DProduce train : trainList) {
			// 神通类型
			int trainType = train.getSn() % Utils.I1000;
			// 加成经验
			int addStageExp = train.getNum();
			// 保存经验成功
			if (this.processSkillSaveTrain(humanObj, trainType, addStageExp) ){
				trainTypeSet.add(trainType);
			}
		}
		
		// 获得培养倍率
		int[] trainInfo = this.getTrainMutipleInfo(humanObj.humanSkillRecord.getTrainMutiple());
		float gainMutiple = trainInfo[1] / Utils.F10000;
		
		// 额外加成经验
		DProduce extraDp = this.getTrainExtra(trainList, gainMutiple);
		if (extraDp.getSn() != 0) {
			int extraTrainType = extraDp.getSn() % Utils.I1000;
			int extraAddExp = extraDp.getNum();
			this.processSkillSaveTrain(humanObj, extraTrainType, extraAddExp);
		}
		
		// 清除抽奖结果
		humanObj.humanSkillRecord.clearSkillTrain();
		
		// 保存培养结果
		humanObj.humanSkillRecord.modifyAllTrain();
		
		// 重新计算技能培养加成的属性
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.SkillTrain);

		
		// 是否需要同步全局
		SCSkillSaveTrain.Builder msg = SCSkillSaveTrain.newBuilder();
		SkillTrainJSON sk = null;
		for (Integer trainType : trainTypeSet) {
			sk = humanObj.humanSkillRecord.getSkillTrainJSON(trainType);
			if (sk == null) {
				continue;
			}
			msg.addSkillList(sk.createDSkill());
		}
		humanObj.sendMsg(msg);
		
		//培养次数+1
		humanObj.cultureTimes.setAvatarNum(humanObj.cultureTimes.getAvatarNum()+1);

		// 保存技能修炼
		Event.fire(EventKey.HumanSkillTrainSave, "humanObj", humanObj);
		
		// 同步全局神通信息
		this.updateSkillTrainList(humanObj);
	}

	/**
	 * 技能符文激活
	 * @param humanObj
	 * @param skillTag 技能sn
	 * @param runeSn 符文sn
	 */
	public void _msg_CSSkillRuneUnlock(HumanObject humanObj, int skillTag, int runeSn) {
		// 符文激活是否解锁
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillRune, humanObj)) {
			Log.skill.error("===_msg_CSSkillRuneUnlock 符文激活未开放 ===");
			humanObj.sendSysMsg(291201);
			return;
		}
		
		SkillJSON skill = humanObj.humanSkillRecord.getSkillJSON(skillTag);
		if (skill == null) {
			Log.skill.error("===_msg_CSSkillRuneUnlock 无法激活未解锁技能的符文");
			humanObj.sendSysMsg(291202);
			return;
		}
		
		SkillRuneJSON runeJSON = skill.getSkillRune(runeSn);
		if (runeJSON != null && runeJSON.lv != 0) {
			Log.skill.error("===_msg_CSSkillRuneUnlock 符文无法重复激活");
			humanObj.sendSysMsg(291203);
			return;
		}
		// 获取是否可以激活的配置表
		ConfSkillRune confSkillRune0 = ConfSkillRune.get(ConfigKeyFormula.getSkillRuneSn(skillTag, runeSn, 0));
		if (confSkillRune0 == null) {
			Log.table.error("===_msg_CSSkillRuneUnlock skillSn:{}, runeSn:{}, lv:0", skillTag, runeSn);
			return;
		}
		
		// 技能，符文激活需要的玩家等级
		int lvQm = confSkillRune0.QmLev;
		int humanLv = humanObj.getHuman().getLevel();
		if (humanLv < lvQm ) {
			// 不满足激活条件
			Log.skill.error("===_msg_CSSkillRuneUnlock 激活符文条件未达成");
			humanObj.sendSysMsg(291204);
			return;
		}
		
		// 扣道具
		if (!RewardHelper.checkAndConsume(humanObj, confSkillRune0.needItem, confSkillRune0.itemNum, LogSysModType.SkillRuneUnlock)) {
			return;
		}
				
		// 激活一个新的符文
		skill.addRune(runeSn, 1);
		// 更新技能数据库详情
		humanObj.humanSkillRecord.modifySkill(skill);
		
		SCSkillRuneUnlock.Builder msg = SCSkillRuneUnlock.newBuilder();
		DSkillRune.Builder skRuneBd = msg.getRuneInfoBuilder();
		skRuneBd.setSkillSn(skillTag);
		skRuneBd.setRuneSn(runeSn);
		skRuneBd.setRuneLv(1);
		humanObj.sendMsg(msg);
		
		if (isInstallSkill(humanObj, skillTag)) {
			// 同步全局信息
			this.updateSkillList(humanObj);
		}
	}
	
	/**
	 * 技能符文洗练（被废弃）
	 * @param humanObj
	 * @param skillTag
	 * @param runeSn
	 */
	public void _msg_CSSkillRunePractice(HumanObject humanObj, int skillTag, int runeSn) {
		// 符文激活相关
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillRune, humanObj)) {
			Log.skill.error("===_msg_CSSkillRunePractice 符文洗练未开放 ===");
			humanObj.sendSysMsg(294301);
			return;
		}
		
		SkillJSON skill = humanObj.humanSkillRecord.getSkillJSON(skillTag);
		if (skill == null) {
			Log.skill.error("===_msg_CSSkillRunePractice 无法激活未解锁技能的符文");
			humanObj.sendSysMsg(294302);
			return;
		}
		
		SkillRuneJSON runeJSON = skill.getSkillRune(runeSn);
		if (runeJSON == null || runeJSON.lv == 0) {
			Log.skill.error("===_msg_CSSkillRunePractice 符文未激活，无法洗练");
			humanObj.sendSysMsg(294303);
			return;
		}
		// 当前符文等级
		int curLv = runeJSON.lv;
		ConfSkillRune nextConf = ConfSkillRune.get(ConfigKeyFormula.getSkillRuneSn(skillTag, runeSn, curLv + 1));
		if (nextConf == null) {
			Log.skill.error("===_msg_CSSkillRunePractice 符文已达最大等级");
			humanObj.sendSysMsg(294304);
			return;
		}
		
		ConfSkillRune conf = ConfSkillRune.get(ConfigKeyFormula.getSkillRuneSn(skillTag, runeSn, curLv));
		if (conf == null) {
			Log.table.error("===_msg_CSSkillRunePractice 技能符文配置异常");
			return;
		}
		
		int lvQm = conf.QmLev;
		int humanLv = humanObj.getHuman().getLevel();
		if (humanLv < lvQm ) {
			// 不满足激活条件
			Log.skill.error("===_msg_CSSkillRuneUnlock 符文洗练条件未达成");
			humanObj.sendSysMsg(294305);
			return;
		}
		
		// 扣道具
		if (!RewardHelper.checkAndConsume(humanObj, conf.needItem, conf.itemNum, LogSysModType.SkillRunePractice)) {
			return;
		}
		
		// 权重出洗练结果
		int practiceLv = Utils.getRandRange(conf.addWeight);
		ConfSkillRune practiceConf = ConfSkillRune.get(ConfigKeyFormula.getSkillRuneSn(skillTag, runeSn, practiceLv));
		if (practiceConf == null) {
			Log.table.error("===_msg_CSSkillRunePractice 技能符文修炼等级异常");
			return;
		}
		
		// 设置符文等级，如果为0级，则意味着修炼爆了重置
		runeJSON.setLv(practiceLv);
		// 数据持久化修改
		humanObj.humanSkillRecord.modifySkill(skill);
		
		SCSkillRunePractice.Builder msg = SCSkillRunePractice.newBuilder();
		DSkillRune.Builder runeBd = msg.getRuneInfoBuilder();
		runeBd.setSkillSn(skillTag);
		runeBd.setRuneSn(runeSn);
		runeBd.setRuneLv(practiceLv);
		humanObj.sendMsg(msg);
		
		if (isInstallSkill(humanObj, skillTag)) {
			// 同步全局信息
			this.updateSkillList(humanObj);
		}
	}
	
	/**
	 * 爆点培养升级
	 * @param humanObj
	 * @param godsTag
	 */
	public void _msg_CSSkillGodsLvUp(HumanObject humanObj, int godsTag) {
		// 爆点升级相关
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillGods, humanObj)) {
			Log.skill.error("===_msg_CSSkillGodsLvUp 爆点未开放 ===");
			humanObj.sendSysMsg(295101);
			return;
		}
		
		SkillGodsJSON gods = humanObj.humanSkillRecord.getSkillGodsJSON(godsTag);
		if (gods == null) {
			Log.skill.error("===_msg_CSSkillGodsLvUp 爆点未激活===");
			humanObj.sendSysMsg(295102);
			return;
		}
		// 当前等级的配置表
		ConfSkillGodsLvUp conf = ConfSkillGodsLvUp.get(gods.lv);
		if (gods.lv == humanObj.getHuman().getLevel() || conf.upSoulValue == 0) {
			Log.skill.error("===_msg_CSSkillGodsLvUp 爆点已达当前最大等级===");
			humanObj.sendSysMsg(295103);
			return;
		}
		
		// 扣道具
		if (!RewardHelper.checkAndConsume(humanObj, conf.costMoney, conf.costNum, LogSysModType.SkillGodsLvUp)) {
			return;
		}
		
		// 默认倍率为1
		int critValue = 10000;
		// 是否暴击
		if(Utils.isRandRangeInner(conf.chance)) {
			// 暴击倍率 = 配置倍率
			critValue = conf.critValue;
		}
		// 随机获得灵气值 
		int randomSoul = Utils.randomBetween(conf.soulValue[0], conf.soulValue[1]);
		// 暴击后的结果值
		int soulValue = randomSoul * critValue / Utils.I10000;
		
		// 是否升级
		boolean isUp = false;
		
		// 添加经验
		int curExp = gods.exp + soulValue;
		// 如果经验超过升级经验
		if (curExp >= conf.upSoulValue) {
			// 下一级配置
			ConfSkillGodsLvUp confNextLv = ConfSkillGodsLvUp.get(gods.lv + 1);
			List<Integer> godsTags = Utils.intToIntegerList(confNextLv.godsSn);
			// index：爆点对应的数组角标
			int index = godsTags.indexOf(godsTag);
			if (index < 0) {
				Log.table.error("===_msg_CSGodsUnlockByItem 技能爆点升级配置表错误，godsSn:{}===", godsTag);
				return;
			}
			// 不满足升级条件，则经验加到满
			if ( !processCanUnlock(humanObj.getHuman(), humanObj, confNextLv.lvQm[index], confNextLv.vipLvQm[index], confNextLv.instQm[index]) ){
				// 上一级配置
				ConfSkillGodsLvUp confUponLv = ConfSkillGodsLvUp.get(gods.lv - 1);
				if (confUponLv == null) {
					Log.table.error("===_msg_CSGodsUnlockByItem 技能爆点升级配置表错误，godsSn:{}===", godsTag);
					return;
				}
				// 当前等级的经验上限 - 上一级经验上限 - 1 = 当前级的最大经验
				gods.setExp(conf.upSoulValue - confUponLv.upSoulValue - 1);
			} else {
				// 升级爆点
				gods.addLv();
				// 设置经验
				gods.setExp(curExp - conf.upSoulValue);
				
				isUp = true;
			}
		} else {
			gods.setExp(curExp);
		}
		
		// 更新爆点数据
		humanObj.humanSkillRecord.modifyGods(gods);
		// 升级了
		if(isUp) {
			// 计算爆点升级加成属性
			this.calc_humanSkillGodsLvProp(humanObj);
			
			// 同步全局信息
			this.updateSkillGodsList(humanObj);
			
			// 发送升级事件
			Event.fire(EventKey.GodsLvUp, "humanObj", humanObj);
		}
		// 发送升级培养事件
		Event.fire(EventKey.GodsLvPractice, "humanObj", humanObj);
		
		SCSkillGodsLvUp.Builder msg = SCSkillGodsLvUp.newBuilder();
		msg.setSkillGods(gods.createDSkillGods());
		// 单次培养获得的灵气值
		msg.setSoulValue(soulValue);
		// 本次暴击的数值
		msg.setCritValue(critValue);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 爆点升星（渡劫）
	 * @param humanObj
	 * @param godsTag
	 */
	public void _msg_CSSkillGodsStarUp(HumanObject humanObj, int godsTag) {
		// 爆点升星开放状态
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeGodsStar, humanObj)) {
			Log.skill.error("===_msg_CSSkillGodsLvUp 爆点升星未开放 ===");
			humanObj.sendSysMsg(295301);
			return;
		}
		
		SkillGodsJSON gods = humanObj.humanSkillRecord.getSkillGodsJSON(godsTag);
		if (gods == null) {
			Log.skill.error("===_msg_CSSkillGodsLvUp 爆点未激活===");
			humanObj.sendSysMsg(295302);
			return;
		}
		// 当前星级的配置表
		ConfSkillGodsStar conf = ConfSkillGodsStar.get(ConfigKeyFormula.getSkillGodsStarSn(godsTag, gods.star));
		if (conf == null) {
			Log.table.error("===_msg_CSSkillGodsLvUp 爆点升星配置表错误===");
			return;
		}
		// 满级
		if (conf.isMax == 1) {
			Log.skill.error("===_msg_CSSkillGodsLvUp 爆点已达当前最大星级===");
			humanObj.sendSysMsg(295303);
			return;
		}
		// 当前星级的配置表
		ConfSkillGodsStar confNext = ConfSkillGodsStar.get(ConfigKeyFormula.getSkillGodsStarSn(godsTag, gods.star + 1));
		if (confNext == null) {
			Log.table.error("===_msg_CSSkillGodsLvUp 爆点升星配置表错误 1===");
			return;
		}
		// 主角等级不足，无法升星
		if (humanObj.getHuman().getLevel() < confNext.lvQm) {
			Log.skill.error("===_msg_CSSkillGodsLvUp 主角等级不足，无法升星===");
			humanObj.sendSysMsg(295304);
			return;
		}
		// 货币判断，如果不足直接return
		if (!RewardHelper.canConsume(humanObj, conf.costMoney, conf.costMoneyNum)) {
			return;
		}
		// 碎片的index
		int chipSnIndex = -1;
		for (int i = 0; i < conf.costItem.length; i++) {
			ConfItem confItem = ConfItem.get(conf.costItem[i]);
			if (confItem.itemType == ItemTypeKey.GodsChip) {
				chipSnIndex = i;
				break;
			}
		}
		// 碎片sn，数量
		int chipItemSn = 0;
		int needChipNum = 0;
		int chipNum = 0;
		int universalNum = 0;
		if (chipSnIndex != -1) {
			// 碎片sn，数量
			chipItemSn = conf.costItem[chipSnIndex];
			needChipNum = conf.costItemNum[chipSnIndex];
			// 实际需要消耗的碎片数量
			chipNum = needChipNum;
			// 本身碎片不足
			if (!RewardHelper.canConsume(humanObj, chipItemSn, needChipNum)) {
				// 当前拥有的神兽碎片
				chipNum = RewardHelper.countBySn(humanObj, chipItemSn);
				// 需要万能碎片的数量
				if (ParamManager.universalGodConversion.isEmpty()) {
					humanObj.sendSysMsg(28);
					return;
				}
				universalNum = ParamManager.universalGodConversion.get(godsTag - 1) * (needChipNum - chipNum); 
				// 万能碎片判断
				if (!RewardHelper.canConsume(humanObj, ParamManager.universalGodItem, universalNum)) {
					humanObj.sendSysMsg(28);
					return;
				}
			}
		}
		int[] itemSns = Utils.concatAll_Int(conf.costMoney, conf.costItem); // 资源加上道具
		itemSns = Utils.appendInt(itemSns, ParamManager.universalGodItem);  // 加上万能碎片
		int[] itemNums = new int[conf.costItemNum.length];
		for (int i = 0; i < conf.costItemNum.length; i++) {
			if (chipSnIndex == i) {
				itemNums[i] = chipNum;
			} else {
				itemNums[i] = conf.costItemNum[i];
			}
		}
		itemNums = Utils.concatAll_Int(conf.costMoneyNum, itemNums); // 资源花费，和道具花费数量组合
		itemNums = Utils.appendInt(itemNums, universalNum);			 // 加上万能碎片数量
		// 检查消耗
		if (!RewardHelper.checkAndConsume(humanObj, itemSns, itemNums, LogSysModType.SkillGodsStarUp)) {
			return;
		}
		
		// 升星
		gods.setStar(gods.star + 1);
		// 更新爆点数据
		humanObj.humanSkillRecord.modifyGods(gods);
		
		// 计算星级增加的属性
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.SkillGodsStar);

		SCSkillGodsStarUp.Builder msg = SCSkillGodsStarUp.newBuilder();
		msg.setSkillGods(gods.createDSkillGods());
		humanObj.sendMsg(msg);
		
		//跑马灯——神兽升星，名称需要修改
		if (confNext.star != conf.star) {
			String content = Utils.createStr("{}|{}|{}|{}|{}", ParamManager.sysMsgMark, 999014, 
						humanObj.getHuman().getName(), godsTag , gods.star);
			InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
		}
		
		// 同步全局信息
		this.updateSkillGodsList(humanObj);
		
		// 爆点升星事件
		Event.fire(EventKey.GodsStarUp, "humanObj", humanObj);
	}

	/**
	 * 选择上阵爆点
	 * @param humanObj
	 * @param godsTag
	 */
	public void _msg_CSSelectSkillGods(HumanObject humanObj, int godsTag) {
		// 爆点升级相关
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillGods, humanObj)) {
			Log.skill.error("===_msg_CSSelectGodsSkill 爆点功能未开放 ===");
			humanObj.sendSysMsg(295300);
			return;
		}
		SkillGodsJSON gods = humanObj.humanSkillRecord.getSkillGodsJSON(godsTag);
		if (gods == null) {
			Log.skill.error("===_msg_CSSelectGodsSkill 爆点未激活===");
			humanObj.sendSysMsg(295302);
			return;
		}
		
		// 设置爆点上阵
		humanObj.humanSkillRecord.setInstallGods(godsTag);

		SCSelectSkillGods.Builder msg = SCSelectSkillGods.newBuilder();
		msg.setGodsTag(godsTag);
		humanObj.sendMsg(msg);
		
		// 同步全局信息
		this.updateSkillGodsSn(humanObj);
	}	

	/**
	 * 道具解锁爆点
	 * @param humanObj
	 * @param godsTag
	 */
	public void _msg_CSGodsUnlockByItem(HumanObject humanObj, int godsTag) {
		// 爆点升级相关
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeSkillGods, humanObj)) {
			Log.skill.error("===_msg_CSGodsUnlockByItem 爆点激活未开放 ===");
			humanObj.sendSysMsg(295701);
			return;
		} 
		SkillGodsJSON gods = humanObj.humanSkillRecord.getSkillGodsJSON(godsTag);
		if (gods != null) {
			Log.skill.error("===_msg_CSGodsUnlockByItem 爆点已激活===");
			humanObj.sendSysMsg(295702);
			return;
		}
		
		// 判断爆点是否解锁
		ConfSkillGodsLvUp confGodsLvUp = ConfSkillGodsLvUp.get(0);
		if (confGodsLvUp == null) {
			Log.table.error("===_msg_CSGodsUnlockByItem 技能配置表错误，godsSn:{}===", godsTag);
			return;
		}
		
		List<Integer> godsTags = Utils.intToIntegerList(confGodsLvUp.godsSn);
		// index：爆点对应的数组角标
		int index = godsTags.indexOf(godsTag);
		if (index < 0) {
			Log.table.error("===_msg_CSGodsUnlockByItem 技能配置表错误，godsSn:{}===", godsTag);
			return;
		}
		// 条件不满足
		if ( !processCanUnlock(humanObj.getHuman(), humanObj, confGodsLvUp.lvQm[index], confGodsLvUp.vipLvQm[index], confGodsLvUp.instQm[index]) ){
			Log.skill.error("===_msg_CSGodsUnlockByItem 爆点激活条件不满足 ===");
			humanObj.sendSysMsg(295703);
			return;
		}
		
		// 道具
		String[] unlockItemStr = Utils.strToStrArray(confGodsLvUp.unlockItem);
		int[] itemAry = Utils.strToIntArraySplit(unlockItemStr[index]);
		
		// 道具数量
		String[] itemNumStr = Utils.strToStrArray(confGodsLvUp.unlockItemNum);
		int[] numAry = Utils.strToIntArraySplit(itemNumStr[index]);
				
		// 扣道具
		if (!RewardHelper.checkAndConsume(humanObj, itemAry, numAry, LogSysModType.SkillGodsUnlock)) {
			return;
		}
		
		// 新增一个爆点
		gods = new SkillGodsJSON(godsTag, 1, 0);
		// 插入内存并更新缓存
		humanObj.humanSkillRecord.addGods(gods);
		// 如果当前没有上阵爆点，则设置解锁爆点为上阵爆点
		if (humanObj.humanSkillRecord.getInstallGods() == 0) {
			humanObj.humanSkillRecord.setInstallGods(godsTag);
		}
		
		// 计算爆点升级加成属性
		this.calc_humanSkillGodsLvProp(humanObj);
		
		SCGodsUnlockByItem.Builder msg = SCGodsUnlockByItem.newBuilder();
		msg.setSkillGods(gods.createDSkillGods());
		msg.setGodsSnInBattle(humanObj.humanSkillRecord.getInstallGods());
		humanObj.sendMsg(msg);
		
		// 爆点解锁
		Event.fire(EventKey.GodsUnlock , "humanObj", humanObj, "unlockNum", 1);
		
		// 同步全局信息
		this.updateSkillGodsList(humanObj);
	}
	
	/**
	 * 处理技能升级需要的金钱
	 * @param skillSn 升级的技能sn
	 * @param curLv 升级前的等级
	 * @param willLv 升级后的等级
	 * @return 到升级后需要的金钱
	 */
	private int processSkillLvUpNeedCoin(int skillSn, int curLv, int willLv) {
		ConfSkillLvUp conf = null;
		int totalCost = 0;
		for (int lv = curLv; lv < willLv; ++lv) {
			conf = ConfSkillLvUp.get(ConfigKeyFormula.getSkillLvUpSn(skillSn, lv));
			totalCost += conf.useCost;
		}
		return totalCost;
	}
	
	/**
	 * 升级，副本解锁，VIP升级时
	 * 判断技能解锁，并且推送给客户端解锁情况
	 */
	private void processUnlock(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		HumanSkillRecord skillRecord = humanObj.humanSkillRecord;
		
		// 解锁的技能sn列表
		List<Integer> skillTagList = processUnlockSkill(human, skillRecord, humanObj);
		if (skillTagList.size() != 0) {
			// 是否需要同步全局
			boolean isUpdateGlobal = false;
//			SCSkillUnlock.Builder msg = SCSkillUnlock.newBuilder();  // 技能解锁暂时不推送给客户端，由客户端自行处理
			for (Integer skillTag : skillTagList) {
				SkillJSON skill = humanObj.humanSkillRecord.getSkillJSON(skillTag);
				if (skill == null) {
					continue;
				}
				if (isInstallSkill(humanObj, skillTag)) {
					isUpdateGlobal = true;
				}
//				msg.addSkillInfoSet(skill.createDSkillInfo());
			}
//			humanObj.sendMsg(msg);
			if (isUpdateGlobal) {
				// 同步全局信息
				this.updateSkillList(humanObj);
			}
		}
		
		// 解锁的爆点sn列表
		List<Integer> godsTagList = processUnlockGods(human, skillRecord, humanObj);
		// 爆点解锁需要服务端推送给客户端
		if (godsTagList.size() != 0) {
			if (godsTagList.size() != 0) {
				SCSkillGodsUnlock.Builder msg = SCSkillGodsUnlock.newBuilder();
				for (Integer godsTag : godsTagList) {
					SkillGodsJSON gods = humanObj.humanSkillRecord.getSkillGodsJSON(godsTag);
					if (gods == null) {
						continue;
					}
					msg.addSkillGods(gods.createDSkillGods());
				}
				msg.setGodsSnInBattle(humanObj.humanSkillRecord.getInstallGods());
				humanObj.sendMsg(msg);
			}
			// 爆点解锁
			Event.fire(EventKey.GodsUnlock , "humanObj", humanObj, "unlockNum", godsTagList.size());
			
			// 计算爆点升级加成属性
			this.calc_humanSkillGodsLvProp(humanObj);
			
			// 同步全局信息
			this.updateSkillGodsList(humanObj);
		}
		
		// 判断是否有技能开放升阶
		List<Integer> trainTypes = this.processUnlockTrain(human, skillRecord, humanObj);
		// 推送阶级变化的技能信息
		if (trainTypes.size() != 0) {
			SCSkillTrainUnlock.Builder msg = SCSkillTrainUnlock.newBuilder();
			for (Integer trainType : trainTypes) {
				SkillTrainJSON st = humanObj.humanSkillRecord.getSkillTrainJSON(trainType);
				if (st == null) {
					continue;
				}
				msg.addTrainList(st.createDSkill());
			}
			humanObj.sendMsg(msg);
		}
	}
	
	/**
	 * 判断技能是否解锁
	 * @param human
	 * @param skillRecord 技能的记录
	 * @param humanObj 玩家对象（创角流程为null）
	 * @return 解锁的技能列表
	 */
	private List<Integer> processUnlockSkill(Human human, HumanSkillRecord skillRecord, HumanObject humanObj) {
		List<Integer> skillList = new ArrayList<>();
		int heroSn = human.getSn();
		ConfSkillLvUp confSkLvUp = null;
		ConfPartnerProperty confHero = ConfPartnerProperty.get(heroSn);
		int[] skillSns = confHero.skill;
		for (int skillSn : skillSns) {
			int skillTag = ConfigKeyFormula.getSkillTagBySn(skillSn);
			
			// 技能已经解锁
			if (skillRecord.getSkillJSON(skillTag) != null) {
				continue;
			}
			confSkLvUp = ConfSkillLvUp.get(ConfigKeyFormula.getSkillLvUpSn(skillTag, 0));
			// 该技能不存在，或者不满足解锁条件
			if (confSkLvUp == null) {
				continue;
			}
			// 需要解锁道具
			if (confSkLvUp.unlockItem[0] != 0) {
				continue;
			}
			// 条件不满足
			if ( !processCanUnlock(human, humanObj, confSkLvUp.unLockLv, confSkLvUp.vipLev, confSkLvUp.chapterLevId)) {
				continue;
			}
			
			// 新增技能
			SkillJSON skill = new SkillJSON(skillTag, 1);
			// 新增到内存，并修改数据库
			skillRecord.addSkill(skill);
			
			skillList.add(skill.tag);
		}
		return skillList;
	}
	/**
	 * 判断爆点是否解锁
	 * @param human
	 * @param skillRecord 技能的记录
	 * @param humanObj 玩家对象（创角流程为null）
	 * @return 解锁的爆点列表
	 */
	private List<Integer> processUnlockGods(Human human, HumanSkillRecord skillRecord, HumanObject humanObj) {
		List<Integer> godsList = new ArrayList<>();
		// 判断爆点是否解锁
		ConfSkillGodsLvUp confGodsLvUp = ConfSkillGodsLvUp.get(0);
		List<Integer> godsTags = Utils.intToIntegerList(confGodsLvUp.godsSn);
		int index = 0;
		for (int godsTag : godsTags) {
			// 爆点已经解锁
			if (skillRecord.getSkillGodsJSON(godsTag) != null) {
				continue;
			}
			// index：爆点对应的数组角标
			index = godsTags.indexOf(godsTag);
			// 该技能不存在
			if (index < 0) {
				continue;
			}
			// 解锁需要道具
			String[] unlockItemStr = Utils.strToStrArray(confGodsLvUp.unlockItem);
			int[] unlockItemAry = Utils.strToIntArraySplit(unlockItemStr[index]);
			// 需要解锁道具
			if (unlockItemAry[0] != 0) {
				continue;
			}
			// 条件不满足
			if ( !processCanUnlock(human, humanObj, confGodsLvUp.lvQm[index], confGodsLvUp.vipLvQm[index], confGodsLvUp.instQm[index]) ){
				continue;
			}
			
			// 新增爆点
			SkillGodsJSON gods = new SkillGodsJSON(godsTag, 1, 0);
			// 插入内存并更新缓存
			skillRecord.addGods(gods);
			// 如果当前没有上阵爆点，则设置解锁爆点为上阵爆点
			if (skillRecord.getInstallGods() == 0) {
				skillRecord.setInstallGods(godsTag);
			}
			godsList.add(gods.tag);
		}
		return godsList;
	}
	
	/**
	 * 判断神通是否解锁
	 * @param human
	 * @param skillRecord 技能的记录
	 * @param humanObj 玩家对象（创角流程为null）
	 * @return 解锁的神通列表
	 */
	private List<Integer> processUnlockTrain(Human human, HumanSkillRecord skillRecord, HumanObject humanObj) {
		List<Integer> trainList = new ArrayList<>();
		// 判断是否有神通解锁
		ConfSkillTrain confSkTrain = null;
		int[] trainTypes = ParamManager.skillTrainType;
		for (int trainType : trainTypes) {
			// 神通已经解锁
			if (skillRecord.getSkillTrainJSON(trainType) != null) {
				continue;
			}
			confSkTrain = ConfSkillTrain.get(ConfigKeyFormula.getSkillTrainSn(trainType, 0));
			if(confSkTrain == null) {
				continue;
			}
			// 修炼解锁
			if (!processCanUnlock(human, humanObj, confSkTrain.lvQm, confSkTrain.vipQm, confSkTrain.instQm)) {
				continue;
			}
			// 新增爆点
			SkillTrainJSON train = new SkillTrainJSON(trainType);
			// 插入内存并更新缓存
			skillRecord.addTrain(train);
			
			trainList.add(trainType);
		}
		return trainList;
	}
	
	/**
	 * 判断是否可以解锁技能
	 * @param human 玩家实体
	 * @param humanObj 创角流程这个为空
	 * @param lvQm 玩家等级限制
	 * @param vipQm vip等级限制
	 * @param instQm 副本限制
	 * @return true可以解锁，false不可以解锁
	 */
	private boolean processCanUnlock(Human human, HumanObject humanObj, int lvQm, int vipQm, int instQm) {
		// 条件满足
		if ( 	// 没有条件 或者 等级超过条件则满足
				( lvQm == 0 || human.getLevel() >= lvQm ) &&  
				// 没有条件 或者 VIP超过条件则满足
				( vipQm == 0 || human.getVipLevel() >= vipQm ) && 
				// 没有条件 或者 （在玩家对象不为空的情况下，通过了条件副本则满足）
				( instQm == 0 || (humanObj != null && InstanceManager.inst().isInstPass(humanObj, instQm)) ) 
		   ) {
			return true;
		}
		return false;
	}
	
	/**
	 * 处理技能神通升级加经验
	 * @param humanObj 
	 * @param trainType 神通标识（Param.SkillTrainType）
	 */
	private boolean processSkillSaveTrain(HumanObject humanObj, int trainType, int addStageExp) {
		HumanSkillRecord skillRecord = humanObj.humanSkillRecord;
		// 获取神通
		SkillTrainJSON tj = skillRecord.getSkillTrainJSON(trainType);
		if (tj == null) {
			return false;
		}
		// 玩家等级
		int humanLv = humanObj.getHuman().getLevel();
		
		// 获取配置表
		ConfSkillTrain confTrain = ConfSkillTrain.get(ConfigKeyFormula.getSkillTrainSn(trainType, tj.stage));
		// 原经验
		int oldExp = tj.exp;
		// 添加后的经验
		int curExp = oldExp + addStageExp;
		// 当前等级升到下一等级需要的经验
		int upExp = 0;
		// 升级前的星级
		int oldTrainStar = tj.stage / 10;
		
		while (curExp >= confTrain.upExp) {
			upExp = confTrain.upExp;
			// 当前满级，则经验为升到下一级经验
			if (confTrain.isMax == 1) {
				curExp = upExp;
				break;
			} else {
				// 下一级配置
				confTrain = ConfSkillTrain.get(ConfigKeyFormula.getSkillTrainSn(trainType, tj.stage + 1));
				// 如果不满足升到下一级的等级，设置为满经验
				if (confTrain != null && humanLv < confTrain.lvQm) {
					curExp = upExp;
					break;
				} else {
					// 正常升阶，经验正常添加
					tj.addStage(1);
					curExp -= upExp;
				}
			}
		}
		int curTrainStar = tj.stage / 10;
		if (oldTrainStar != curTrainStar) {
			// 神通升星成功
			// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
			String content = Utils.createStr("{}|{}|{}|{}|{}", ParamManager.sysMsgMark, 999020, 
					humanObj.getHuman().getName(), confTrain.sn, curTrainStar);
			InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
		}
		
		// 设置当前经验
		tj.setExp(curExp);
		
		return true;
	}
	
	/**
	 * 获取需要过滤掉的修炼技能 （注：Filter = 过滤器）
	 * 未解锁，满级的技能培养对象对应的道具
	 */
	private List<Integer> getTrainFilterItemSnList(HumanObject humanObj) {
		// 角色当前等级
		int humanLv = humanObj.getHuman().getLevel();
		
		ConfSkillTrain conf = null;
		// 要移除的index
		List<Integer> rmItems = new ArrayList<>();
		SkillTrainJSON trainJSON = null;
		
		// 角色所有的神通类型
		int[] trainTypes = ParamManager.skillTrainType;
		// 获取未解锁的技能, 获取满级的技能
		for (int trainType : trainTypes) {
			trainJSON = humanObj.humanSkillRecord.getSkillTrainJSON(trainType);
			// 未解锁技能
			if (trainJSON == null) {
				rmItems.add(ConfigKeyFormula.getSkillTrainItemSn(trainType));
				continue;
			}
			// 当前配置
			conf = ConfSkillTrain.get(ConfigKeyFormula.getSkillTrainSn(trainType, trainJSON.stage));
			// 当前等级满经验
			int curStageExp = conf.upExp;
			// 下一阶配置为空，则为满阶
			conf = ConfSkillTrain.get(ConfigKeyFormula.getSkillTrainSn(trainType, trainJSON.stage+1));
			if (conf == null || (humanLv < conf.lvQm && trainJSON.exp >= curStageExp)) {
				rmItems.add(ConfigKeyFormula.getSkillTrainItemSn(trainType));
			}
		}
		return rmItems;
	}
	
	/**
	 * 抽取某个槽位的抽奖结果
	 * @param humanObj
	 * @param mutiple 培养倍率
	 * @param slotIndex 抽奖槽位
	 * @param rmItems 要移除的技能对应的产出经验道具sn
	 * @return
	 */
	private DProduce getOnceTrainResult(HumanObject humanObj, float mutiple, int slotIndex, List<Integer> rmItems) {
		ConfSkillTrainRule conf = ConfSkillTrainRule.get(slotIndex);
		if (conf == null) {
			return null;
		}
		List<Integer> dropItemSns = Utils.intToIntegerList(conf.dropType);
		List<Integer> weights = Utils.intToIntegerList(conf.weight);
		List<String> itemNums = Utils.strToStringList(conf.experienceNum);
		int index = -1;
		for (Integer rmItemSn : rmItems) {
			index = dropItemSns.indexOf(rmItemSn);
			if (index < 0) {
				continue;
			}
			// 从掉落数组和权重数组中移除
			dropItemSns.remove(index);
			weights.remove(index);
			itemNums.remove(index);
		}
		// 权重掉落的index为
		int dropIndex = Utils.getRandRange(weights);
		// 根据index获得sn
		int dropItemSn = dropItemSns.get(dropIndex);
		int[] numAry = Utils.strToIntArraySplit(itemNums.get(dropIndex));
		// 随机获得经验值 
		int randomNum = Utils.randomBetween(numAry[0], numAry[1]);
		// 随机值 * 倍率
		randomNum = (int) (randomNum * mutiple);
		
		DProduce.Builder produce = DProduce.newBuilder();
		produce.setSn(dropItemSn);
		produce.setNum(randomNum);
		return produce.build();
	}
	
	/**
	 * 处理额外奖励
	 */
	private DProduce getTrainExtra(List<DProduce> trainList, float mutiple) {
		// itemSn, 数量
		Map<Integer, Integer> tmpMap = new HashMap<>();
		for (DProduce dProduce : trainList) {
			// 将结果sn放进map中
			Integer count = tmpMap.get(dProduce.getSn());
			if (count == null) {
				count = 0;
			}
			count += 1;
			tmpMap.put(dProduce.getSn(), count);
		}
		DProduce.Builder dp = DProduce.newBuilder();
		dp.setSn(0);
		for (Integer itemSn : tmpMap.keySet()) {
			int count = tmpMap.get(itemSn);
			if (count == 2) {
				dp.setSn(itemSn);
				dp.setNum((int)(ParamManager.skillTrainDouble * mutiple));
			} else if (count == 3) {
				dp.setSn(itemSn);
				dp.setNum((int)(ParamManager.skillTrainTriple * mutiple));
			}
		}
		return dp.build();
	}
	
	/**
	 * 解锁的技能槽位数
	 * @param human
	 * @return
	 */
	private int getUnlockSkillSlot(Human human){
		int unlockSlot = 0; //解锁的槽位数量
		int[] openSlotAry = ParamManager.skillSlotUnlock; 
		for (int i = 0; i < ParamManager.skillMaxInstall; i++) {
			if (human.getLevel() >= openSlotAry[i]){
				unlockSlot += 1;
			}
		}
		return unlockSlot;
	}
	
	/**
	 * 技能升级属性相关配置表
	 */
	public ConfLevelAward getConfLevCorrelationBySkillLvSum(int needLv){
		Map<Integer , Map<Integer, List<ConfLevelAward>>> levelAwardMap = GlobalConfVal.getLevelAwardMap();
		if (levelAwardMap == null) {
			return null;
		}
		// 获取类型 style:1
		Map<Integer, List<ConfLevelAward>> styleMap = levelAwardMap.get(1);
		if (styleMap == null) {
			return null;
		}
		// 获取条件 condition:2
		List<ConfLevelAward> confList = styleMap.get(ELevelAward.LvAwardNeedLv_VALUE);
		for (ConfLevelAward conf : confList) {
			if (conf.parameter1.length <= 1) {
				return conf;
			}
			if (needLv >= conf.parameter1[0] && needLv < conf.parameter1[1]) {
				return conf;
			}
		}
		return null;
	}
	
	/**
	 * 获取玩家技能
	 * @param skillTag 技能标识
	 */
	public SkillJSON getSkillJSON(HumanObject humanObj, int skillTag) {
		return humanObj.humanSkillRecord.getSkillJSON(skillTag);
	}
	
	/**
	 * 获取玩家的技能当前指向sn
	 */
	public int getSkillSn(int skillTag, int stage) {
		int skillSn = ConfigKeyFormula.getSkillAdvancedSn(skillTag, stage);
		ConfSkill conf = ConfSkill.get(skillSn);
		if (conf == null) {
			return 0;
		}
		return skillSn;
	}
	
	/**
	 * 获取神兽身上的天赋技能
	 * @param godsTag
	 * @param star
	 * @return 需要判空
	 */
	public int[] getSkillGodsTalent(int godsTag, int star) {
		ConfSkillGodsStar conf = ConfSkillGodsStar.get(ConfigKeyFormula.getSkillGodsStarSn(godsTag, star));
		if (conf == null) {
			return null;
		}
		return conf.talent;
	}
	
	/**
	 * 获取神兽对应的技能sn
	 */
	public int getSkillGodsSkillSn(int godsTag, int star) {
		ConfSkillGodsStar conf = ConfSkillGodsStar.get(ConfigKeyFormula.getSkillGodsStarSn(godsTag, star));
		if (conf == null) {
			return -1;
		}
		return conf.skillSn;
	}
	
	/**
	 * 处理培养倍率信息
	 * @param mutiple
	 * @return info info[0]消耗倍率，info[1]产出倍率
	 */
	private int[] getTrainMutipleInfo(int mutiple) {
		int[] info = {10000, 10000, 0, 0};
		for (String rate : ParamManager.skillTrainCostRate) {
			info = Utils.strToIntArraySplit(rate);
			if (mutiple != info[0]) {
				continue;
			}
			return info;
		}
		return info;
	}
	
	/**
	 * 判断该技能是否上阵
	 */
	public boolean isInstallSkill(HumanObject humanObj, int skillTag) {
		List<Integer> skillTagList = humanObj.humanSkillRecord.getSkillGroup();
		return skillTagList.contains(skillTag);
	}
	
	/**
	 * 获得上阵技能SkillJSON
	 */
	public List<SkillJSON> getInstallSkillJSON (HumanObject humanObj) {
		List<Integer> skillTagList = humanObj.humanSkillRecord.getSkillGroup();
		List<SkillJSON> skillList = new ArrayList<>();
		for (Integer skillTag : skillTagList) {
			SkillJSON skillJSON = this.getSkillJSON(humanObj, skillTag);
			if (skillJSON != null) {
				skillList.add(skillJSON);
			}
		}
		return skillList;
	}
	/**
	 * 获得所有的爆点技能详细
	 */
	public List<SkillGodsJSON> getSkillGodsList(HumanObject humanObj) {
		return humanObj.humanSkillRecord.getSkillGodsList();
	}
	
	/**
	 * 获得上阵爆点
	 */
	public SkillGodsJSON getInstallSkillGodsJSON(HumanObject humanObj) {
		return humanObj.humanSkillRecord.getInstallSkillGodsJSON();
	}
	
	/**
	 * 获取神通的被动sn
	 */
	private List<Integer> getSkillTrainPassiveSn(int humanSn, int trainType, int stage) {
		int sn = ConfigKeyFormula.getSkillTrainSn(trainType, stage);
		ConfSkillTrain conf = ConfSkillTrain.get(sn);
		if (conf == null) {
			return null;
		}
		if (conf.passiveSkillsUnlock[0] == 0) {
			return null;
		}
		String[] passiveSkillStr = Utils.strToStrArray(conf.passiveSkill);
		List<Integer> passiveSkill = Utils.strToIntList(passiveSkillStr[humanSn - 1]);
		return passiveSkill;
	}
	
	/**
	 * 同步全局技能信息
	 */
	private void updateSkillInfo(HumanObject humanObj) {
		List<SkillJSON> skillList = getInstallSkillJSON(humanObj);
		List<SkillGodsJSON> godsList = humanObj.humanSkillRecord.getSkillGodsList();
		int skillGodsSn = humanObj.humanSkillRecord.getInstallGods();
		List<SkillTrainJSON> trainList = humanObj.humanSkillRecord.getSkillTrainList();
		// 同步全局信息
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateSkillInfo(humanObj.getHumanId(), skillList, godsList, skillGodsSn, trainList);
	}
	
	/**
	 * 同步全局上阵技能信息
	 */
	private void updateSkillList(HumanObject humanObj) {
		List<SkillJSON> skillList = getInstallSkillJSON(humanObj);
		// 同步全局信息
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateSkillList(humanObj.getHumanId(), skillList);
	}
	
	/**
	 * 同步全局爆点技能信息
	 */
	private void updateSkillGodsList(HumanObject humanObj) {
		List<SkillGodsJSON> godsList = humanObj.humanSkillRecord.getSkillGodsList();
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateSkillGodsList(humanObj.getHumanId(), godsList);
	}
	
	/**
	 * 同步全局上阵的爆点sn
	 */
	private void updateSkillGodsSn(HumanObject humanObj) {
		int skillGodsSn = humanObj.humanSkillRecord.getInstallGods();
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateSkillGodsSn(humanObj.getHumanId(), skillGodsSn);
	}

	/**
	 * 同步全局技能神通信息
	 */
	private void updateSkillTrainList(HumanObject humanObj) {
		List<SkillTrainJSON> trainList = humanObj.humanSkillRecord.getSkillTrainList();
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateSkillTrainList(humanObj.getHumanId(), trainList);
	}
	
	//////////////////////////////////////////////////
	// 战斗需要相关
	//////////////////////////////////////////////////
	/**
	 * 设置玩家镜像的技能信息
	 * @param humanObj 来源方：玩家对象
	 * @param humanMirror
	 */
	public void setHumanMirrorSkill(HumanObject humanObj, MirrorHuman humanMirror) {
		// 上阵技能
		List<SkillJSON> skillList = this.getInstallSkillJSON(humanObj);
		humanMirror.setInstallSkillJSON(this.getInstallSkillInfos(skillList, false));
		// 上阵爆点
		SkillGodsJSON godsJSON = this.getInstallSkillGodsJSON(humanObj);
		humanMirror.setInstallGodsJSON(this.getInstallGodsInfo(godsJSON, false));
		//设置被动技能
		this.setPassiveSkillStr(humanObj.getHuman().getSn(), humanMirror, 
									humanObj.humanSkillRecord.getSkillTrainList());
	}
	/**
	 * 设置玩家镜像的技能信息
	 * @param hgInfo 来源方：全局对象
	 * @param humanMirror
	 */
	public void setHumanMirrorSkill(HumanGlobalInfo hgInfo, MirrorHuman humanMirror) {
		// 上阵技能
		List<SkillJSON> skillList = hgInfo.skillList;
		humanMirror.setInstallSkillJSON(this.getInstallSkillInfos(skillList, false));
		// 上阵爆点
		SkillGodsJSON godsJSON = hgInfo.getInstallGodsJSON();
		humanMirror.setInstallGodsJSON(this.getInstallGodsInfo(godsJSON, false));
		//设置被动技能
		this.setPassiveSkillStr(hgInfo.sn, humanMirror, 
									hgInfo.skillTrainList);
	}
	/**
	 * 设置玩家镜像的技能信息
	 * @param sourceMirror 来源方：镜像
	 * @param targetMirror 
	 */
	public void setHumanMirrorSkill(MirrorHuman sourceMirror, MirrorHuman targetMirror) {
		targetMirror.setInstallSkillJSON(sourceMirror.getInstallSkillJSON());
		targetMirror.setInstallGodsJSON(sourceMirror.getInstallGodsJSON());
		targetMirror.setPassiveSkill(sourceMirror.getPassiveSkill());
	}

	/**
	 * 设置玩家镜像的技能信息
	 * @param humanObj 来源方：玩家对象
	 */
	public void setHumanMirrorModSkill(HumanObject humanObj, MirrorHuman humanMirror) {
		// 上阵技能
		List<SkillJSON> skillList = this.getInstallSkillJSON(humanObj);
		humanMirror.setInstallSkillJSON(this.getInstallSkillInfos(skillList, true));
		// 上阵爆点
		SkillGodsJSON godsJSON = this.getInstallSkillGodsJSON(humanObj);
		humanMirror.setInstallGodsJSON(this.getInstallGodsInfo(godsJSON, true));
		//设置被动技能
		this.setPassiveSkillStr(humanObj.getHuman().getSn(), humanMirror, 
									humanObj.humanSkillRecord.getSkillTrainList());
	}
	
	/**
	 * 获取玩家所有被动技能
	 * @param skillTrains 技能修炼详细
	 */
	private void setPassiveSkillStr(int humanSn, MirrorHuman humanMirror, List<SkillTrainJSON> skillTrains) {
		List<Integer> passiveSnList = new ArrayList<>();
		// 技能神通 相关被动
		for (SkillTrainJSON trainJSON : skillTrains) {
			if(trainJSON != null) {
				List<Integer> sns = HumanSkillManager.inst().getSkillTrainPassiveSn(humanSn, trainJSON.type, trainJSON.stage);
				if (sns != null) {
					passiveSnList.addAll(sns);
				}
			}
		}
		// 后续模块被动...
		
		// 设置镜像被动信息
		humanMirror.setPassiveSkill(Utils.ListIntegerToStr(passiveSnList));
	}
	
	
	/**
	 * 获取上阵技能信息"sn,lv,power,value|..." 如："1010000,1,100,10|..."
	 * @param skillList 上阵技能
	 * @param useMod 是否使用模板 true使用，false正常
	 * @return
	 */
	public String getInstallSkillInfos(List<SkillJSON> skillList, boolean useMod) {
		List<String> installs = new ArrayList<>();
		for (SkillJSON sJSON : skillList) {
			if(sJSON != null) {
				installs.add(this.getInstallSkillInfo(sJSON, useMod));
			}
		}
		return Utils.ListStrToStrSplit(installs);
	}
	
	/**
	 * 获取技能所需要保存的信息
	 * @param sj "sn,lv,power,value" （技能sn，技能等级，技能威力，技能威力固定加成）
	 * @param useMod 是否使用模板 true使用，false正常
	 */
	public String getInstallSkillInfo(SkillJSON sj, boolean useMod) {
		if (sj == null) {
			return "";
		}
		int sn = HumanSkillManager.inst().getSkillSn(sj.tag, sj.stage);
		int[] info = null;
		// 使用模板
		if (useMod) {
			info = new int[]{sn, ParamManager.lootMapPvpRoleSkillLv, 0, 0};
			return Utils.arrayIntToStr(info);
		}
		// 正常流程
		// 升级养成相关
		ConfSkillLvUp confSkillLvUp = ConfSkillLvUp.get(ConfigKeyFormula.getSkillLvUpSn(sj.tag, sj.lv));
		// 升阶养成相关
		ConfSkillAdvanced confSkillAdv = ConfSkillAdvanced.get(ConfigKeyFormula.getSkillAdvancedSn(sj.tag, sj.stage)); 
		int power = 0;
		int value = 0;
		if (confSkillLvUp != null) {
			power += confSkillLvUp.power;
			value += confSkillLvUp.value;
		}
		if (confSkillAdv != null) {
			power += confSkillAdv.power;
		}
		info = new int[]{sn, sj.lv, power, value};
		return Utils.arrayIntToStr(info);
	}
	
	/**
	 * 获取爆点所需要保存的信息
	 * @param gj "sn,lv,power,value" （技能sn，技能等级，技能威力，技能威力固定加成）
	 * @param useMod 是否使用模板 true使用，false正常
	 */
	public String getInstallGodsInfo(SkillGodsJSON gj, boolean useMod) {
		if (gj == null) {
			return "";
		}
		int sn = HumanSkillManager.inst().getSkillGodsSkillSn(gj.tag, gj.star);
		int[] info = null;
		// 使用模板
		if (useMod) {
			info = new int[]{sn, ParamManager.lootMapPvpGodSkillLv, 0, 0};
			return Utils.arrayIntToStr(info);
		}
		// 正常流程
		// 升级养成相关
		ConfSkillLvUp confSkillLvUp = ConfSkillLvUp.get(ConfigKeyFormula.getSkillLvUpSn(gj.tag, gj.lv));
		// 升星养成相关
		ConfSkillGodsStar confSkillStar = ConfSkillGodsStar.get(ConfigKeyFormula.getSkillGodsStarSn(gj.tag, gj.star)); 
		int power = 0;
		int value = 0;
		if (confSkillLvUp != null) {
			power += confSkillLvUp.power;
			value += confSkillLvUp.value;
		}
		if (confSkillStar != null) {
			power += confSkillStar.power;
		}
		info = new int[]{sn, gj.lv, power, value};
		return Utils.arrayIntToStr(info);
	}
	
	//////////////////////////////////////////////////
	// 任务相关
	//////////////////////////////////////////////////
	/**
	 * 满足等级的技能数量
	 * @param lv 等级参数
	 * @return 满足的数量
	 */
	public int getAmountBySkillLv(HumanObject humanObj, int lv) {
		return humanObj.humanSkillRecord.getAmountBySkillLv(lv);
	}
	
	/**
	 * 满足阶段的技能数量
	 * @param stage
	 * @return 满足的数量
	 */
	public int getAmountBySkillStage(HumanObject humanObj, int stage) {
		return humanObj.humanSkillRecord.getAmountBySkillStage(stage);
	}
	
	/**
	 * 满足等级的爆点数量
	 * @param lv 等级参数
	 * @return 满足的数量
	 */
	public int getAmountByGodsLv(HumanObject humanObj, int lv) {
		return humanObj.humanSkillRecord.getAmountByGodsLv(lv);
	}
	
	/**
	 * 满足星级的爆点数量
	 * @param star 星级参数
	 * @return 满足的数量
	 */
	public int getAmountByGodsStar(HumanObject humanObj, int star) {
		return humanObj.humanSkillRecord.getAmountByGodsStar(star);
	}
	
	/**
	 * 获取满足重数的技能修炼
	 * @param stage
	 * @return 满足的数量
	 */
	public int getAmountByTrainStage(HumanObject humanObj, int stage) {
		return humanObj.humanSkillRecord.getAmountByTrainStage(stage);
	}
	
	
	// -----------------------------------------
	// 主角养成属性计算
	// -----------------------------------------
	/**
	 * 计算技能升级加成属性
	 * @param humanObj
	 */
	public PropCalcCommon calc_humanSkillLvUpProps(HumanObject humanObj) {
		int minLv = humanObj.humanSkillRecord.getSkillMinLv();
		// 获取当前解锁技能最低等级对应的技能奖励配置表
		ConfLevelAward curConf = HumanSkillManager.inst().getConfLevCorrelationBySkillLvSum(minLv);
		if (curConf == null) {
			return new PropCalcCommon();
		}
		
		PropCalcCommon propCalc = new PropCalcCommon();
		// 技能等级加成
		List<SkillJSON> skillJSONList = humanObj.humanSkillRecord.getSkillList();
		for (SkillJSON sj : skillJSONList) {
			// 将要升级到的技能等级配置表
			ConfSkillLvUp confSLU = ConfSkillLvUp.get(ConfigKeyFormula.getSkillLvUpSn(sj.tag, sj.lv));
			if (confSLU == null) {
				continue;
			}
			propCalc.add(confSLU.attribute, confSLU.attributeValue);
		}
		// 技能大师加成
		for (int i = 0; i < curConf.attribute.length; i++) {
			propCalc.add(curConf.attribute[i], curConf.attributeValue[i]);
		}
		return propCalc;
	}
	
	/**
	 * @param humanObj
	 * @return 技能等阶属性
	 */
	public PropCalcCommon calc_humanSkillAdvUpProps(HumanObject humanObj) {
		PropCalcCommon propCalc = new PropCalcCommon();
		ConfSkillAdvanced confSkillAdvanced = null;
		for (SkillJSON skill : humanObj.humanSkillRecord.getSkillList()) {
			confSkillAdvanced = ConfSkillAdvanced.get(ConfigKeyFormula.getSkillAdvancedSn(skill.tag, skill.stage));
			if (confSkillAdvanced == null) {
				continue;
			}
			propCalc.add(confSkillAdvanced.attr, confSkillAdvanced.attrValue);	
		}
		return propCalc;
	}
	
	/**
	 * 计算爆点升级加成
	 */
	public void calc_humanSkillGodsLvProp(HumanObject humanObj) {
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.SkillGodsLv);
	}
	
	/**
	 * 获取玩家技能培养加成的属性
	 */
	public PropCalcCommon getSkillTrainProp(HumanObject humanObj) {
		PropCalcCommon prop = new PropCalcCommon();
		ConfSkillTrain conf = null;
		for (SkillTrainJSON stj : humanObj.humanSkillRecord.getSkillTrainList()) {
			if (stj.stage <= 0) {
				continue;
			}
			conf = ConfSkillTrain.get(ConfigKeyFormula.getSkillTrainSn(stj.type, stj.stage));
			if (conf == null) {
				continue;
			}
			String[] attr = conf.attr;
			int[] attrValue = conf.attrValue;
			prop.add(attr, attrValue);
		}
		return prop;
	}
	
	/**
	 * 获取所有神兽等级相关加成的总属性
	 */
	public PropCalcCommon getSkillGodsLvProp(HumanObject humanObj) {
		HumanSkillRecord skillRecord = humanObj.humanSkillRecord;
		List<SkillGodsJSON> godsJSONs = skillRecord.getSkillGodsList();
		
		// 爆点升级配置表
		ConfSkillGodsLvUp conf = ConfSkillGodsLvUp.get(0);
		List<Integer> godsTags = Utils.intToIntegerList(conf.godsSn);
		// 爆点所对应数组index
		int index = -1;
		PropCalcCommon prop = new PropCalcCommon();
		for (SkillGodsJSON skillGodsJSON : godsJSONs) {
			// 该爆点当前等级配置表
			conf = ConfSkillGodsLvUp.get(skillGodsJSON.lv);
			if (conf == null) {
				continue;
			}
			index = godsTags.indexOf(skillGodsJSON.tag);
			if (index < 0) {
				continue;
			}
			
			// 增加的属性
			String[] attrStr = conf.attr;
			// 增加的属性值
			String[] attrValues = Utils.strToStrArray(conf.attrValue);
			int[] attrValue = Utils.strToIntArraySplit(attrValues[index]);
			prop.add(attrStr, attrValue);
		}
		return prop;
	}
	
	/**
	 * 获取所有神兽星级相关加成的属性
	 */
	public PropCalcCommon getSkillGodsStarProp(HumanObject humanObj) {
		HumanSkillRecord skillRecord = humanObj.humanSkillRecord;
		List<SkillGodsJSON> godsJSONs = skillRecord.getSkillGodsList();
		
		// 爆点升星配置表
		ConfSkillGodsStar conf = null;
		PropCalcCommon prop = new PropCalcCommon();
		for (SkillGodsJSON skillGodsJSON : godsJSONs) {
			conf = ConfSkillGodsStar.get(ConfigKeyFormula.getSkillGodsStarSn(skillGodsJSON.tag, skillGodsJSON.star));
			if (conf == null) {
				continue;
			}
			prop.add(conf.attr, conf.attrValue);
		}
		return prop;
	} 

	//////////////////////////////////////
	////// —————— 废弃方法 —————— //////////
	//////////////////////////////////////
	/**
	 * 玩家被动技能加成属性
	 * @param skills
	 * @return
	 */
	public PropCalc abandon_calc_passivitySkillProp(List<Integer> skills) {
		// 被动技能属性加成
		PropCalc passivitySkillPlus = new PropCalc();
		if (skills.isEmpty()) {
			return passivitySkillPlus;
		}
		// 属性加成
		Map<String, Float> mapPropAdd = new HashMap<String, Float>();

		for (int i = 0; i < skills.size(); i++) {
			int skillSn = skills.get(i);
			ConfSkill confSkill = ConfSkill.get(skillSn);
			if (confSkill == null) {
				Log.table.error("ConfSkill 配表错误，no find sn={}", skillSn);
				continue;
			}
		}

		for (String key : mapPropAdd.keySet()) {
			passivitySkillPlus.add(FightPropName.valueOf(key), mapPropAdd.get(key));
		}
		return passivitySkillPlus;
	}
	
	/**
	 * 获取爆点配置表
	 * @param godsSn 爆点sn
	 * @param godsLv 爆点等级
	 * @return
	 */
	public ConfSkillGods abandon_getConfSkillGods(int godsSn, int godsLv) {
		Map<Integer, List<ConfSkillGods>> skillGodsMap = GlobalConfVal.getSkillGodsMap();
		if (skillGodsMap == null) {
			return null;
		}
		List<ConfSkillGods> confList = skillGodsMap.get(godsSn);
		for (ConfSkillGods conf : confList) {
			if (godsLv >= conf.skillLev[0] && godsLv <= conf.skillLev[1]) {
				return conf;
			}
		}
		return null;
	}

}
