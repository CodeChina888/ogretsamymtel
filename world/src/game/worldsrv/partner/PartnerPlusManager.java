package game.worldsrv.partner;

import game.msg.Define.EMoneyType;
import game.msg.MsgPartner;
import game.msg.MsgPartner.SCCimeliaAddLevel;
import game.msg.MsgPartner.SCPartnerAddLevel;
import game.msg.MsgPartner.SCPartnerAddStar;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfCimeliaConstitutions;
import game.worldsrv.config.ConfCimeliaStarUp;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfPartnerConstitutions;
import game.worldsrv.config.ConfPartnerFate;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfPartnerRecruit;
import game.worldsrv.config.ConfPartnerStarUp;
import game.worldsrv.config.ConfServantAddProp;
import game.worldsrv.config.ConfSkill;
import game.worldsrv.entity.Cimelia;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Partner;
import game.worldsrv.enumType.ItemParamType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.RankType;
import game.worldsrv.human.HumanManager;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemTypeKey;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.rank.RankManager;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;


public class PartnerPlusManager extends ManagerBase {
	
	public static final int ADVANCED_LEVEL_MAX = 11;
	//最高星级
	public static final int STAR_LEVEL_MAX = 5;
	public static final int FRIENDSHIP_LEVEL_MAX = 7;
	public static final int HotohoriTemp =100;//星宿技能
	
	/**
	 * 获取实例
	 * @return
	 */
	public static PartnerPlusManager inst() {
		return inst(PartnerPlusManager.class);
	}
	
	
	/**
	 * 自动使用一个道具: 类型是武将卡。
	 * @param param
	 */
	
	@Listener(value = EventKey.ItemUse, subInt = ItemTypeKey.partnerChip)
	public void _listener_ItemUse(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ItemUse humanObj is null");
			return;
		}
		
		ConfItem conf = Utils.getParamValue(param, "confItem", null);
		if(null == conf){
			Log.game.error("===_listener_ItemUse conf=null");
			return;
		}
		int num = Utils.getParamValue(param, "num", 0);
		int[] confparam = Utils.strToIntArraySplit(conf.param[0]);
		if(confparam.length < 2){//避免超出数组范围
			Log.game.error("===_listener_ItemUse confItem.param.length < 2, sn={}", conf.sn);
			return;
		}
		int type = Utils.intValue(confparam[0]);// ItemParamType类型 
//		int value = Utils.intValue(confparam[1]);// 值或Produce表的sn
		if(type == ItemParamType.GeneralCard.value()){// 武将卡类型
			for (int i = 0; i < num; i++) {
//				userCard(humanObj, value);//直接使用武将卡，如果没有就招募，如果有就自动分解
			}
		}else{
			Log.game.error("===_listener_ItemUse confItem.param.type={}, confItem.sn={}, num = {}", type, conf.sn, num);
		}
		ItemBagManager.inst().remove(humanObj, conf.sn, num);
	}
	
	
	/**
	 * 是否可以分解成碎片：
	 * 如果可以返回被分解的武将sn
	 * 如果不能分解返回-1
	 * @return
	 */
	public int canResolveGeneral(HumanObject humanObj,int itemSn){
		int sn = -1;
		return sn;
	}
	
	/**
	 * 获取武将符文进阶个数
	 * @param humanObj
	 * @param advLv 指定阶数
	 * @return
	 */
	public int getGenAdvLvSize(HumanObject humanObj, int advLv) {
		int size = 0;
		return size;
	}


	/**
	 * 伙伴升星 
	 * @param humanObj
	 * @param generalId
	 */
	public void _msg_CSPartnerAddStar(HumanObject humanObj, long generalId) {
		//返回消息给前端
		PartnerObject partnerObj = (PartnerObject) humanObj.partnerMap.get(generalId);
		if(null == partnerObj){
			Log.game.error("===GeneralPlusManager.starLevelup generalObj=null, id={}", generalId);
			return;
		}
		if(null == partnerObj.parentObject) {
			Log.human.error("===GeneralPlusManager.starLevelup genObj.parentObject=null");
			partnerObj.parentObject = humanObj;
		}
		
		Partner partner = partnerObj.getPartner();
		// 下一星级sn
		int nextSn = ConfigKeyFormula.getPartnerStarSn(partner.getSn(), partner.getStar()+1);
		ConfPartnerStarUp nextConf = ConfPartnerStarUp.get(nextSn);//判断是否有下一星
		if(null == nextConf){
			humanObj.sendSysMsg(190507);//发送文字提示消息  武将星级已满
			return;
		}
		// 当前星级sn
		int curSn = ConfigKeyFormula.getPartnerStarSn(partner.getSn(), partner.getStar());
		ConfPartnerStarUp conf = ConfPartnerStarUp.get(curSn);
		if(null == conf){
			Log.table.error("===ConfGeneralStar配表错误，no find sn={}", curSn);
//			humanObj.sendMsg(msg);
			return;
		}
		
		// 货币判断，如果不足直接return
		if (!RewardHelper.canConsume(humanObj,  EMoneyType.coin_VALUE, conf.upStarCostMoney)) {
			return;
		}
		// 伙伴碎片的index
		int chipSnIndex = -1;
		for (int i = 0; i < conf.upStarCost.length; i++) {
			ConfItem confItem = ConfItem.get(conf.upStarCost[i]);
			if (confItem.itemType == ItemTypeKey.partnerChip) {
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
			chipItemSn = conf.upStarCost[chipSnIndex];
			needChipNum = conf.upStarCostNumb[chipSnIndex];
			// 实际需要消耗的碎片数量
			chipNum = needChipNum;
			// 本身碎片不足
			if (!RewardHelper.canConsume(humanObj, chipItemSn, needChipNum)) {
				// 当前拥有的可消耗碎片数量
				chipNum = RewardHelper.countBySn(humanObj, chipItemSn);
				// 资质对应的兑换比例
				int conversionIndex = ParamManager.universalPartnerList.indexOf(partner.getAptitude());
				// 需要万能碎片的数量
				if (ParamManager.universalPartnerConversion.isEmpty() || conversionIndex < 0) {
					humanObj.sendSysMsg(28);
					return;
				}
				// 万能碎片数量 = 比例 * 需要补足的碎片数
				universalNum = ParamManager.universalPartnerConversion.get(conversionIndex) * (needChipNum - chipNum); 
				// 万能碎片判断
				if (!RewardHelper.canConsume(humanObj, ParamManager.universalPartnerItem, universalNum)) {
					humanObj.sendSysMsg(28);
					return;
				}
			}
		}
		int[] tmpSns = {ParamManager.universalPartnerItem, EMoneyType.coin_VALUE}; // 万能碎片sn，金币sn
		int[] itemSns = Utils.concatAll_Int(conf.upStarCost, tmpSns); // 默认道具，万能碎片sn，金币sn
		int[] itemNums = new int[conf.upStarCostNumb.length];
		for (int i = 0; i < conf.upStarCostNumb.length; i++) {
			if (chipSnIndex == i) {
				itemNums[i] = chipNum;
			} else {
				itemNums[i] = conf.upStarCostNumb[i];
			}
		}
		int[] tmpNums = {universalNum, conf.upStarCostMoney}; // 万能碎片数量，金币数量
		itemNums = Utils.concatAll_Int(itemNums, tmpNums); // 默认道具，万能碎片数量，金币数量
		// 检查消耗
		if (!RewardHelper.checkAndConsume(humanObj, itemSns, itemNums, LogSysModType.PartnerAddStar)) {
			return;
		}

		int curStar = partner.getStar() + 1;
		partner.setStar(curStar);//保存
		
		// 重新计算属性战力
		UnitManager.inst().propsChange(partnerObj, EntityUnitPropPlus.Star);
		
		//通知客户端
		SCPartnerAddStar.Builder msg   = SCPartnerAddStar.newBuilder();
		msg.setResult(true);
		msg.setPartnerId(generalId);
		msg.setCurStar(curStar);
		humanObj.sendMsg(msg);
		
		//发布事件
		Event.fire(EventKey.PartnerStartUp, "humanObj", humanObj);
		
	}

	/**
	 * 法宝升星
	 * @param humanObj
	 * @param partnerId
	 */
	public void _msg_CSCimeliaAddStar(HumanObject humanObj, long partnerId) {
		//返回消息给前端
		PartnerObject partnerObj = (PartnerObject) humanObj.partnerMap.get(partnerId);
		if(null == partnerObj){
			Log.game.error("===GeneralPlusManager.starLevelup generalObj=null, id={}", partnerId);
			return;
		}
		if(null == partnerObj.parentObject) {
			Log.human.error("===GeneralPlusManager.starLevelup genObj.parentObject=null");
			partnerObj.parentObject = humanObj;
		}

		Cimelia cimelia = partnerObj.getCimeLia();
		// 下一星级sn
		int nextSn = ConfigKeyFormula.getCimeliaStarSn(cimelia.getSn(), cimelia.getStar()+1);
		ConfCimeliaStarUp nextConf = ConfCimeliaStarUp.get(nextSn);//判断是否有下一星
		if(null == nextConf){
			humanObj.sendSysMsg(190507);//发送文字提示消息  武将星级已满
			return;
		}
		// 当前星级sn
		int curSn = ConfigKeyFormula.getCimeliaStarSn(cimelia.getSn(), cimelia.getStar());
		ConfCimeliaStarUp conf = ConfCimeliaStarUp.get(curSn);
		if(null == conf){
			Log.table.error("===ConfCimelia配表错误，no find sn={}", curSn);
//			humanObj.sendMsg(msg);
			return;
		}
		if (partnerObj.getPartner().getLevel() < conf.rolelevQm) {
			// 伙伴等级不足,不能升星
			humanObj.sendSysMsg(360105);// 英雄的等級未達到要求
			return;
		}

		// 道具够不够
		int[] itemSn = Utils.appendInt(conf.upStarCost, EMoneyType.coin_VALUE);
		int[] itemNum = Utils.appendInt(conf.upStarCostNumb, conf.upStarCostMoney);
		if(itemSn.length != itemNum.length){
			Log.table.error("ConfGeneralStar配表错误，costItem.length != costNum.length");
			return;
		}
		// 扣道具
		if (!RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.PartnerAddStar)) {
			return;
		}
		
		int curStar = cimelia.getStar() + 1;
		cimelia.setStar(curStar);

		// 法宝星级加成属性
		UnitManager.inst().propsChange(partnerObj, EntityUnitPropPlus.CimeliaStar);
		
		//通知客户端
		MsgPartner.SCCimeliaAddStar.Builder msg   = MsgPartner.SCCimeliaAddStar.newBuilder();
		msg.setResult(true);
		msg.setPartnerId(partnerId);
		msg.setCurStar(curStar);
		humanObj.sendMsg(msg);

		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress",0, "type",ActivitySevenTypeKey.Type_60);
	}

	
	/**
	 * 添加法宝经验
	 * @param humanObj
	 * @param partnerObj
	 * @param addExp
	 * @param log
	 * @return
	 */
	public void cimeliaExpAdd(HumanObject humanObj, PartnerObject partnerObj, long addExp, LogSysModType log){
		if(partnerObj == null){
			Log.game.error("===GeneralPlusManager.expAdd generalObj=null, humanObj.id={}",humanObj.getHumanId());
			return;
		}
		if(null == partnerObj.parentObject) {
			Log.human.error("===GeneralPlusManager.expAdd genObj.parentObject=null");
			partnerObj.parentObject = humanObj;
		}
		
		Cimelia cimelia = partnerObj.getCimeLia();
		int cimeliaLevel = cimelia.getLevel();
		int humanLevel = humanObj.getHuman().getLevel();
		if(cimeliaLevel >= humanLevel) {//是否可以升级
			humanObj.sendSysMsg(360108);//法宝等级不能超过伙伴等级
			Log.partner.debug("法宝等级不能超过伙伴等级");
			return;
		}
		// 是否有升级
		boolean isLevelUp = false;
		long exp = cimelia.getExp() + addExp;
		int costExp = cimeliaLvUpNeedExp(humanObj, partnerObj);//计算出升级所花费的经验
		while (exp >= costExp) {//判断是否满足升级条件
			cimeliaLevel++;//升级
			if(cimeliaLevel > humanLevel){//判断升级后是否大于主角等级
				cimeliaLevel--;//等级回掉
				exp = costExp-1;				;//经验设置为满格
				humanObj.sendSysMsg(360107);//武将等级不能超过主角等级
				break;
			}
			//真的升级了
			exp -= costExp;//升级后要扣除相应的经验
			ConfLevelExp nextconf = ConfLevelExp.get(cimeliaLevel-1);
			ConfLevelExp nowconf = ConfLevelExp.get(cimeliaLevel);
			int partnerCoin = nowconf.partnerCoin;
			int last_partnerCoin = nextconf.partnerCoin;
			int needCoin =Math.abs(last_partnerCoin -partnerCoin);
			// 扣金币
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.coin_VALUE, needCoin, LogSysModType.PartnerAddLevel)) {
				break;
			}
			
			cimelia.setLevel(cimeliaLevel);//升级
			
			isLevelUp = true;
			
			//根据新的等级获取新的升级所需数据
			costExp = cimeliaLvUpNeedExp(humanObj, partnerObj);
		}
		if(exp > Integer.MAX_VALUE){//避免超出数值范围
			exp = Integer.MAX_VALUE;
		}
		int partner_exp = (int)exp;
		cimelia.setExp(partner_exp);// 保存经验
		cimelia.update();
		
		if (isLevelUp) {
			// 重新计算伙伴属性战力
			UnitManager.inst().propsChange(partnerObj, EntityUnitPropPlus.CimeliaLv);
		}
		
		//通知客户端
		SCCimeliaAddLevel.Builder msg  = SCCimeliaAddLevel.newBuilder();
		msg.setSuccess(true);
		msg.setPartnerId(cimelia.getPartnerId());
		msg.setCimeliaLv(cimelia.getLevel());
		Log.partner.debug("Cimelia_exp:"+partner_exp);
		msg.setExp(partner_exp);
		humanObj.sendMsg(msg);
		
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0, "type",
				ActivitySevenTypeKey.Type_58);
	}
	
	
	/**
	 * 获取该法宝升级所需经验
	 */
	public int cimeliaLvUpNeedExp(HumanObject humanObj,PartnerObject partnerObj){
		Cimelia cimelia = partnerObj.getCimeLia();
		int partnerSn = cimelia.getSn();
		long partnerId = cimelia.getId();
		//判断是否拥有伙伴
		boolean ishas = PartnerManager.inst().isExistPartnerId(humanObj, partnerId);
		if(!ishas){
			Log.game.info("未拥有该伙伴 sn={},partnerID={}",partnerSn,partnerId);
			return 0;
		}
		int nowLevel = cimelia.getLevel();
		//判断是否可以升级
		int lvMax = ConfLevelExp.findAll().size();
		if(nowLevel>=lvMax){
			// 已经满级了
			humanObj.sendSysMsg(190504);
			return 0;
		}
		
		int quality = cimelia.getQuality();
		ConfLevelExp nextconf = ConfLevelExp.get(nowLevel);
		ConfLevelExp nowconf = ConfLevelExp.get(nowLevel+1);
		int exp1 = getLevelupExpByQuality_Cimelia(nextconf,quality);
		int exp2 =getLevelupExpByQuality_Cimelia(nowconf,quality);
		int needExp = Math.abs( exp1-exp2);
		return needExp;
	}
	
	

	/**
	 * 添加武将经验
	 * @param humanObj
	 * @param partnerObj
	 * @param addExp
	 * @param log
	 * @return
	 */
	public void partnerExpAdd(HumanObject humanObj, PartnerObject partnerObj, long addExp, LogSysModType log){
		if(partnerObj == null){
			Log.game.error("===GeneralPlusManager.expAdd generalObj=null, humanObj.id={}",humanObj.getHumanId());
			return;
		}
		if(null == partnerObj.parentObject) {
			Log.human.error("===GeneralPlusManager.expAdd genObj.parentObject=null");
			partnerObj.parentObject = humanObj;
		}
		
		Partner partner = partnerObj.getPartner();
		int partnerLevel = partner.getLevel();
		int humanLevel = humanObj.getHuman().getLevel();
		if(partnerLevel >= humanLevel) {//是否可以升级
			humanObj.sendSysMsg(360107);//武将等级不能超过主角等级
			return;
		}
		// 是否有升级
		boolean isLevelUp = false;
		long exp = partner.getExp() + addExp;
		int costExp = partnerLvUpNeedExp(humanObj, partner);//计算出升级所花费的经验
		while (exp >= costExp) {//判断是否满足升级条件
			partnerLevel++;//升级
			if(partnerLevel > humanLevel){//判断升级后是否大于主角等级CHROM
				partnerLevel--;//等级回掉
				exp = costExp-1;				;//经验设置为满格
				humanObj.sendSysMsg(360107);//武将等级不能超过主角等级
				break;
			}
			//真的升级了
			exp -= costExp;//升级后要扣除相应的经验
			ConfLevelExp nextconf = ConfLevelExp.get(partnerLevel-1);
			ConfLevelExp nowconf = ConfLevelExp.get(partnerLevel);
			int partnerCoin = nowconf.partnerCoin;
			int last_partnerCoin = nextconf.partnerCoin;
			int needCoin =Math.abs(last_partnerCoin -partnerCoin);
			// 扣金币
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.coin_VALUE, needCoin, LogSysModType.PartnerAddLevel)) {
				break;
			}
			
			partner.setLevel(partnerLevel);//升级
			
			isLevelUp = true;
			
			//发布事件
			Event.fire(EventKey.PartnerLvUp,"humanObj",humanObj);
					
			//根据新的等级获取新的升级所需数据
			costExp = partnerLvUpNeedExp(humanObj, partner);
		}
		if(exp > Integer.MAX_VALUE){//避免超出数值范围
			exp = Integer.MAX_VALUE;
		}
		int partner_exp = (int)exp;
		partner.setExp(partner_exp);// 保存经验
		partner.update();
		
		if (isLevelUp) {
			// 重新计算属性战力
			UnitManager.inst().propsChange(partnerObj, EntityUnitPropPlus.Level);
		}
		
		//通知客户端
		SCPartnerAddLevel.Builder msg  = SCPartnerAddLevel.newBuilder();
		msg.setResult(true);
		msg.setPartnerId(partner.getId());
		msg.setPartnerLv(partner.getLevel());
		System.out.println("partner_exp:"+partner_exp);
		msg.setExp(partner_exp);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 获取该武将升级所需经验
	 * 
	 */
	public int partnerLvUpNeedExp(HumanObject humanObj,Partner partner){
		int partnerSn = partner.getSn();
		long partnerId = partner.getId();
		//判断是否拥有伙伴
		boolean ishas = PartnerManager.inst().isExistPartnerId(humanObj, partnerId);
		if(!ishas){
			Log.game.info("未拥有该伙伴 sn={},partnerID={}",partnerSn,partnerId);
			return 0;
		}
		int nowLevel = partner.getLevel();
		//判断是否可以升级
		int lvMax = ConfLevelExp.findAll().size();
		if(nowLevel>=lvMax){
			humanObj.sendSysMsg(190504);
			return 0;
		}
		
		ConfPartnerProperty conf = ConfPartnerProperty.get(partnerSn);
		int quality = conf.quality;
		ConfLevelExp nextconf = ConfLevelExp.get(nowLevel);
		ConfLevelExp nowconf = ConfLevelExp.get(nowLevel+1);
		int needExp = Math.abs( getLevelupExpByQuality(nextconf,quality)-getLevelupExpByQuality(nowconf,quality));
		return needExp;
	}
	
	/**
	 * 依据伙伴品质返回经验列表
	 * @param expConf 配表记录
	 * @param quality 品质
	 * @return
	 */
	public int  getLevelupExpByQuality(ConfLevelExp expConf,int quality){
		int exp =0;
		switch (quality) {
			case 1:				exp = expConf.partner1Exp;				break;
			case 2:				exp = expConf.partner2Exp;				break;
			case 3:				exp = expConf.partner3Exp;				break;
			case 4:				exp = expConf.partner4Exp;				break;
			case 5:				exp = expConf.partner5Exp;				break;
			case 6:				exp = expConf.partner6Exp;				break;
			default:				break;
		}
		return exp;
	}
	
	/**
	 * 依据法宝品质返回经验列表
	 * @param expConf 配表记录
	 * @param quality 品质
	 * @return
	 */
	public int  getLevelupExpByQuality_Cimelia(ConfLevelExp expConf,int quality){
		int exp =0;
		switch (quality) {
			case 1:				exp = expConf.cimelia1Exp;				break;
			case 2:				exp = expConf.cimelia2Exp;				break;
			case 3:				exp = expConf.cimelia3Exp;				break;
			case 4:				exp = expConf.cimelia4Exp;				break;
			case 5:				exp = expConf.cimelia5Exp;				break;
			case 6:				exp = expConf.cimelia6Exp;				break;
			default:				break;
		}
		return exp;
	}
	
	/**
	 * 获取当前玩家的最强阵容
	 * @param humanObj
	 */
	public void mostLineup(HumanObject humanObj){
		
	}
		
	/**
	 * 人物总战力
	 * @param humanObj
	 */
	public void sumCombat(HumanObject humanObj){
		Human human = humanObj.getHuman();
		int sumCombatBefore = human.getSumCombat();
		int lineSumCombat = HumanManager.inst().getLineUpCombat(humanObj);
//		int sumCombat = human.getCombat() + lineSumCombat; //+human.getGeneralSumCombat();
		human.setSumCombat(lineSumCombat);
		if (sumCombatBefore != lineSumCombat) {
			// 如果不是登录推送，则发送战力变化通知
			Event.fire(EventKey.HumanTotalCombatChange, "humanObj", humanObj);			
			// 人物需要进行战斗力排行
			RankManager.inst().addNewRank(humanObj, RankType.RankSumCombat, true);
		}
		Log.human.info("玩家：{}，总战力为：{}，之前总战力为：{}", humanObj, lineSumCombat, sumCombatBefore);
	}
	
	/**
	 * 用户战力发生变化
	 * @param param
	 */
	@Listener(EventKey.HumanCombatChange)
	public void _listener_HumanCombatChange(Param param) {
	}
				
	/**
	 * 获取伙伴品质
	 * @param partnerSn
	 * @return
	 */
	public int getQuality(int partnerSn){
		ConfPartnerProperty conf = ConfPartnerProperty.get(partnerSn);
		if(conf==null){
			Log.table.error("==getQuality  没有对应的资质 sn={}",partnerSn);
		}
		return conf.quality;
	}
	
	/**
	 * @param partnerObj
	 * @return 伙伴缘分属性
	 */
	public PropCalcCommon calc_partnerFateProps(PartnerObject partnerObj) {
		PropCalcCommon propCalc = new PropCalcCommon();
		HumanObject humanObject = partnerObj.getHumanObj();
		if (humanObject == null)
			return propCalc;
		PropCalcCommon baseProps = partnerObj.props.get(EntityUnitPropPlus.Base.name());
		PropCalcCommon levelProps = partnerObj.props.get(EntityUnitPropPlus.Level.name());
		List<Integer> activedPartners = Utils.strToIntList(humanObject.getHumanExtInfo().getActivityHand());
		ConfPartnerRecruit confRecruit = ConfPartnerRecruit.get(partnerObj.getSn());
		if (confRecruit == null)
			return propCalc;
		PropCalcCommon baseAndLevel = new PropCalcCommon();
		if (baseProps != null) {
			baseAndLevel.add(baseProps);
		}
		if (levelProps != null) {
			baseAndLevel.add(levelProps);
		}
		int partnerSn = partnerObj.getSn();
		int[] fates = confRecruit.fateAll;
		for (int fateSn : fates) {
			ConfPartnerFate confFate = ConfPartnerFate.get(fateSn);
			if (confFate == null)
				continue;
			int[] pair = confFate.heroFate;
			if (pair.length != 2)
				continue;
			if (pair[0] != partnerSn)
				continue;
			int oPartnerSn = pair[1];
			if (!activedPartners.contains(oPartnerSn))
				continue;
			PropCalcCommon multi = baseAndLevel.multiply(confFate.AttrPct, confFate.ValPct); 
			propCalc.add(multi); // 百分百属性
			propCalc.add(confFate.Attr, confFate.Val); // 固定属性
		}
		return propCalc;
	}
	
	/**
	 * 计算伙伴升阶相关加成属性
	 * @param partnerObj
	 */
	public PropCalcCommon calc_partnerAdvProps(PartnerObject partnerObj) {
		Partner partner = partnerObj.getPartner();
		if (partner == null) {
			return new PropCalcCommon();
		}
		ConfPartnerConstitutions conf = PartnerManager.inst().getConfPartnerConstitution(partner.getSn(), partner.getAdvLevel());
		if (conf == null) {
			return new PropCalcCommon();
		}
		// 突破影响的属性变化
		JSONObject jo = new JSONObject();
		int length = conf.advancedAttribute.length;
		for (int i = 0; i < length; i++) {
			jo.put(String.valueOf(conf.advancedAttribute[i]), String.valueOf(conf.addHeroGrowRate[i]));
		}
		PropCalcCommon propCalc = new PropCalcCommon();
		propCalc.add(conf.advancedAttribute, conf.addHeroGrowRate);
		return propCalc;
	}
	
	/**
	 * 计算伙伴星级相关加成属性
	 * @param partnerObj 伙伴
	 */
	public PropCalcCommon calc_partnerStarProps(PartnerObject partnerObj) {
		PropCalcCommon propCalc = new PropCalcCommon();
		Partner partner = partnerObj.getPartner();
		if (partner == null) {
			return propCalc;
		}
		// 获取伙伴当前星级配表sn
		int starSn = ConfigKeyFormula.getPartnerStarSn(partner.getSn(), partner.getStar());
		ConfPartnerStarUp confStarUp = ConfPartnerStarUp.get(starSn);
		if (confStarUp == null) {
			return propCalc;
		}
		// 伙伴各模块加成属性
		// 当前基础加成的属性
		PropCalcCommon baseMap = partnerObj.props.get(EntityUnitPropPlus.Base.name());
		// 当前等级加成的属性
		PropCalcCommon levelMap = partnerObj.props.get(EntityUnitPropPlus.Level.name());
		// 当前突破加成的属性
		PropCalcCommon advanceMap = partnerObj.props.get(EntityUnitPropPlus.Advance.name());
		
		String[] upStarAddAttr1 = confStarUp.upStarAddAttr1; // 百分比加成属性类型
		int[] upStarAddAttrNumb1 = confStarUp.upStarAddAttrNumb1; // 百分比加成值 
		int length = upStarAddAttr1.length;
		int curAttrValue = 0;
		// 需要比例加成的属性
		double effectAttr = 0;
		for (int i = 0; i < length; i++) {
			effectAttr = 0;
			// 需要加成的 基础属性
			if (baseMap!=null && baseMap.getValue(upStarAddAttr1[i]) != null) {
				effectAttr += baseMap.getValue(upStarAddAttr1[i]);
			}
			// 需要加成的 等级属性
			if (levelMap!=null && levelMap.getValue(upStarAddAttr1[i]) != null) {
				effectAttr += levelMap.getValue(upStarAddAttr1[i]);
			}
			// 需要加成的 突破属性
			if (advanceMap!=null && advanceMap.getValue(upStarAddAttr1[i]) != null) {
				effectAttr += advanceMap.getValue(upStarAddAttr1[i]);
			}
			// 加成属性 = 需要比例加成的属性 * 比例
			curAttrValue = (int) (effectAttr * upStarAddAttrNumb1[i] / Utils.I10000);
			propCalc.add(upStarAddAttr1[i], curAttrValue);
		}
		// 升星固定加成部分
		propCalc.add(confStarUp.upStarAddAttr, confStarUp.upStarAddAttrNumb);
		return propCalc;
	}
	
	/**
	 * @param partnerObj
	 * @return
	 */
	public PropCalcCommon calc_cimeliaAdvProps(PartnerObject partnerObj) {
		Cimelia cimelia = partnerObj.getCimeLia();
		if (cimelia == null) {
			return new PropCalcCommon();
		}
		ConfCimeliaConstitutions conf = PartnerManager.inst().getConfCimeliaConstitution(cimelia.getSn(), cimelia.getAdvLevel());
		PropCalcCommon propCalc = new PropCalcCommon();
		propCalc.add(conf.advancedAttribute, conf.addHeroGrowRate);
		return propCalc;
	}
	
	/**
	 * 计算法宝星级相关加成属性
	 * @param partnerObj 伙伴
	 */
	public PropCalcCommon calc_cimeliaStarProps(PartnerObject partnerObj) {
		PropCalcCommon propCalc = new PropCalcCommon();
		Cimelia cimelia = partnerObj.getCimeLia();
		if (cimelia == null) {
			return propCalc;
		}
		
		// 获取法宝当前星级配表sn
		int starSn = ConfigKeyFormula.getCimeliaStarSn(cimelia.getSn(), cimelia.getStar());
		ConfCimeliaStarUp confStarUp = ConfCimeliaStarUp.get(starSn);
		if (confStarUp == null) {
			return propCalc;
		}
		
		// 根据比例，加成不包含升星属性的加成值
		// 当前基础加成的属性
		PropCalcCommon baseMap = partnerObj.props.get(EntityUnitPropPlus.CimeliaBase.name());
		// 当前等级加成的属性
		PropCalcCommon levelMap = partnerObj.props.get(EntityUnitPropPlus.CimeliaLv.name());
		// 当前突破加成的属性
		PropCalcCommon advanceMap = partnerObj.props.get(EntityUnitPropPlus.CimeliaAdv.name());
		
		String[] upStarAddAttr1 = confStarUp.upStarAddAttr1; // 百分比加成属性类型
		int[] upStarAddAttrNumb1 = confStarUp.upStarAddAttrNumb1; // 百分比加成值 
		int length = upStarAddAttr1.length;
		int curAttrValue = 0;
		// 需要比例加成的属性
		double effectAttr = 0;
		for (int i = 0; i < length; i++) {
			effectAttr = 0;
			// 需要加成的 基础属性
			if (baseMap!=null && baseMap.getValue(upStarAddAttr1[i])!=null) {
				effectAttr += baseMap.getValue(upStarAddAttr1[i]);
			}
			// 需要加成的 等级属性
			if (levelMap!=null && levelMap.getValue(upStarAddAttr1[i])!=null) {
				effectAttr += levelMap.getValue(upStarAddAttr1[i]);
			}
			// 需要加成的 突破属性
			if (advanceMap!=null && advanceMap.getValue(upStarAddAttr1[i])!=null) {
				effectAttr += advanceMap.getValue(upStarAddAttr1[i]);
			}
			// 加成属性 = 需要比例加成的属性 * 比例
			curAttrValue = (int) (effectAttr * upStarAddAttrNumb1[i] / Utils.I10000);
			propCalc.add(upStarAddAttr1[i], curAttrValue);
		}
		// 升星固定加成部分
		propCalc.add(confStarUp.upStarAddAttr, confStarUp.upStarAddAttrNumb);
		return propCalc;
	}
	
	/**
	 * @param humanObj
	 * @param pto
	 * @return 护法加成属性
	 */
	public PropCalcCommon calc_addPropByServents(HumanObject humanObj, PartnerObject pto) {
		// 当前的随从sn列表
		List<Long> serventIdList = pto.getServantList();
		// 要加成的属性
		PropCalcCommon propCalc = new PropCalcCommon();
		for (Long servantId : serventIdList) {
			PartnerObject servartPO = humanObj.partnerMap.get(servantId);
			if (servartPO == null) {
				continue;
			}
			int servantSn = servartPO.getSn();
			ConfPartnerProperty confProp = ConfPartnerProperty.get(servantSn);
			if (confProp == null) {
				Log.table.info("没有找到对应伙伴资质 func ==addServant partnerSn = {}", servantSn);
				continue;
			}
			// 品质
			int quality = confProp.quality;
			// 星级
			int star = servartPO.getPartner().getStar();
			// 职业
			int profession = confProp.type;
			// 各个模块对应sn
			ConfServantAddProp conf_star_pro = ConfServantAddProp.get(star);
			ConfServantAddProp conf_profession_pro = ConfServantAddProp.get(profession + Utils.I100);
			ConfServantAddProp conf_quality_pro = ConfServantAddProp.get(quality + Utils.I1000);
			PropCalcCommon propCalcCommon = getServentPros(servartPO);
			if (conf_star_pro != null ) {
				PropCalcCommon multi = propCalcCommon.multiply(conf_star_pro.propName, conf_star_pro.propValue);
				propCalc.add(multi);
			}
			if( conf_profession_pro != null ) {
				PropCalcCommon multi = propCalcCommon.multiply(conf_profession_pro.propName, conf_profession_pro.propValue);
				propCalc.add(multi);
			}
			if(conf_quality_pro != null) {
				PropCalcCommon multi = propCalcCommon.multiply(conf_quality_pro.propName, conf_quality_pro.propValue);
				propCalc.add(multi);
			}
		}
		return propCalc;
	}

	private PropCalcCommon getServentPros(PartnerObject partnerObject) {
		PropCalcCommon propCalcCommon = new PropCalcCommon();
		for (Map.Entry<String, PropCalcCommon> entry : partnerObject.props.entrySet()) {
			String type = entry.getKey();
			if (UnitManager.serventPropsChangeAssPartner(EntityUnitPropPlus.valueOf(type))) {
				propCalcCommon.add(entry.getValue());
			}
		}
		return propCalcCommon;
	}
	
	/**
	 * 根据ServantAddProp配置表和护法的属性给伙伴属性加成
	 */
//	private void addPropByServent(PartnerObject servartPO, ConfServantAddProp conf, PropCalcCommon propCalc) {
//		PropCalcCommon propPlus = new PropCalcCommon();
//		UnitPropPlusMap unitPropPlus = servartPO.dataPers.unitPropPlus;
//		// 遍历加成属性来累加数据
//		for (EntityUnitPropPlus type : EntityUnitPropPlus.values()) {
//			if (type.name().equals("Type") || type.name().equals("Name")) {
//				continue;// 类型和名字不属于属性 需要排除
//			}
//			propPlus.add(unitPropPlus.getFrom(type));
//		}
//		// 根据配表加属性
//		// 当前称号加成的属性
//		Map<String, Double> titleAttrMap = unitPropPlus.getFrom(EntityUnitPropPlus.Title);
//		// 当前爆点星级加成的属性
//		Map<String, Double> starAttrMap = unitPropPlus.getFrom(EntityUnitPropPlus.SkillGodsStar);
//		// 当前爆点等级加成的属性
//		Map<String, Double> levelAttrMap = unitPropPlus.getFrom(EntityUnitPropPlus.SkillGodsLv);
//
//		double curAttrValue = 0;
//		// 不需要计算的属性值
//		double unCalcAttr = 0;
//
//		int length = conf.propName.length;
//		for (int i = 0; i < length; i++) {
//			unCalcAttr = 0;
//			// 增加的属性类型
//			String attrType = conf.propName[i];
//			if (titleAttrMap.get(conf.propName[i]) != null) {
//				unCalcAttr += titleAttrMap.get(attrType); // 称号属性不被计算
//			}
//			if (starAttrMap.get(attrType) != null) {
//				unCalcAttr += starAttrMap.get(attrType); // 爆点星级加成不被计算
//			}
//			if (levelAttrMap.get(attrType) != null) {
//				unCalcAttr += levelAttrMap.get(attrType); // 爆点等级加成不被计算
//			}
//
//			// 伙伴其他属性总值 = 伙伴当前该属性总值 - 称号加成的值 - 爆点星级加成的值 - 爆点等级加成值
//			curAttrValue = propPlus.getInt(attrType) - unCalcAttr;
//			curAttrValue = curAttrValue * conf.propValue[i] / Utils.I10000;
//			propCalc.add(attrType, curAttrValue);
//		}
//	}
	
	/**
	 * 获取伙伴养成加成的技能威力
	 * @param skillSn 技能sn
	 * @param skillTypeAry 技能类型数组
	 * @param powerAry 威力数组
	 * @param valueAry 固定加成数组
	 * @return int[0]power，int[1]value
	 */
	public int[] getDevelopSkillPowerAndValue(int skillSn, int[] skillTypeAry, int[] powerAry, int[] valueAry) {
		int[] retAry = new int[2];
		if (skillTypeAry == null) {
			return retAry;
		}
		ConfSkill confSkill = ConfSkill.get(skillSn);
		if (confSkill == null) {
			return retAry;
		}
		for (int i = 0; i < skillTypeAry.length; i++) {
			if (skillTypeAry[i] == confSkill.type) {
				if (powerAry != null) {
					retAry[0] = powerAry[i];
				}
				if (valueAry != null) {
					retAry[1] = valueAry[i];
				}
				break;
			}
		}
		return retAry;
	}

}
