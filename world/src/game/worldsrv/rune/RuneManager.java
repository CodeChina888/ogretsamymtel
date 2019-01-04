package game.worldsrv.rune;

import game.msg.Define.EInformType;
import game.msg.MsgRune.SCLoadRuneInfo;
import game.msg.MsgRune.SCRuneCreate;
import game.msg.MsgRune.SCRuneExchange;
import game.msg.MsgRune.SCRuneSummon;
import game.msg.MsgRune.SCRuneTakeOff;
import game.msg.MsgRune.SCRuneTakeOffOneKey;
import game.msg.MsgRune.SCRuneUpgrade;
import game.msg.MsgRune.SCRuneWear;
import game.msg.MsgRune.SCRuneWearOneKey;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.character.UnitObject;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfRune;
import game.worldsrv.config.ConfRuneSummon;
import game.worldsrv.config.ConfRunesSuit;
import game.worldsrv.drop.DropBag;
import game.worldsrv.drop.DropManager;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Rune;
import game.worldsrv.entity.Unit;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.inform.InformManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.ItemTypeKey;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

/**
 * 符文系统
 */
public class RuneManager extends ManagerBase {

	public static RuneManager inst() {
		return inst(RuneManager.class);
	}
	
	/**
	 * 登陆时候:玩家其它数据加载开始：加载玩家的武将信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Rune.tableName);
		dbPrx.findBy(false, Rune.K.humanId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanRune, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanRune(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanRune humanObj is null");
			return;
		}
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
		
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanInst records=null");
		} else {
			// 加载数据
			for (Record rec : records) {
				Rune rune = new Rune(rec);
				humanObj.runeRecord.addRune(rune);
			}
			this._send_SCLoadRuneInfo(humanObj);// 下发所有符文信息
		}
	}
	
	/**
	 * 下发所有符文信息
	 * @param humanObj
	 */
	private void _send_SCLoadRuneInfo(HumanObject humanObj) {
		// 下发所有符文信息
		SCLoadRuneInfo.Builder msg = SCLoadRuneInfo.newBuilder();
		msg.addAllWearList(humanObj.runeRecord.getWearDRuneList());
		msg.addAllNoWearList(humanObj.runeRecord.getNoWearDRuneList());
		msg.setRuneSummonSn(humanObj.getHumanExtInfo().getRuneSummonSn());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 符文召唤
	 * @param humanObj
	 * @param summonSn 本次招募sn -> ConfRuneSummon.sn 
	 */
	public void _msg_CSRuneSummon(HumanObject humanObj, int summonSn) {
		RuneRecord record = humanObj.runeRecord;
		if ( this.isBagLimit(record.getRuneBagNum()) ) {
			Log.game.error("=== 符文背包已满 ===");
			humanObj.sendSysMsg(170101);
			return;
		}
		// 获取符文召唤对应的
		int sn = humanObj.getHumanExtInfo().getRuneSummonSn();
		if (summonSn != sn) {
			Log.game.error("=== 符文召唤失败，与上次召唤类型不匹配 sn:{} ,curSn:{}===", summonSn, sn);
			humanObj.sendSysMsg(171101);
			return;
		}
		
		ConfRuneSummon conf = ConfRuneSummon.get(sn);
		if (conf == null) {
			Log.game.error("=== 符文召唤记录sn异常，重置为0 ===");
			sn = 0;
			conf = ConfRuneSummon.get(sn);
		}
		// 判断消耗
		if (!RewardHelper.checkAndConsume(humanObj, conf.costType, conf.costNum, LogSysModType.RuneSummon)) {
			return;
		}
		
		// 本次招募获得的符文
		List<Rune> runeList = new ArrayList<>();
		// 本次招募获得的道具
		ItemChange itemChange = new ItemChange();
		// 获取所有的召唤次数
		List<Integer> summonCounts = Utils.strToIntList(humanObj.getHumanExtInfo().getRuneSummonCount());
		// 招募一次的处理
		this._summonRuneOnce(humanObj, conf, runeList, itemChange, summonCounts);
		humanObj.getHumanExtInfo().setRuneSummonCount(Utils.ListIntegerToStr(summonCounts));
		
		SCRuneSummon.Builder msg = SCRuneSummon.newBuilder();
		msg.addAllRune(RuneRecord.createDRuneList(runeList));
		msg.addAllProduce(itemChange.getProduce());
		msg.setNextSn(humanObj.getHumanExtInfo().getRuneSummonSn());
		humanObj.sendMsg(msg);
		
		int num = humanObj.cultureTimes.getRune() +1 ;
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", num , "type",ActivitySevenTypeKey.Type_55);
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0 , "type",ActivitySevenTypeKey.Type_56);
		
	}
	
	/**
	 * 符文合成升级
	 * @param humanObj
	 * @param upgradeRuneId 养成升级的符文id
	 * @param consumeRuneIds 被合成消耗的符文id
	 */
	public void _msg_CSRuneUpgrade(HumanObject humanObj, long upgradeRuneId, List<Long> consumeRuneIds) {
		if (consumeRuneIds.contains(upgradeRuneId)) {
			humanObj.sendSysMsg(103);
			return ;
		}
		// 符文记录信息
		RuneRecord record = humanObj.runeRecord;
		// 养成符文
		Rune upRune = record.getRune(upgradeRuneId); 
		if (upRune == null) {
			Log.game.error("=== 该符文不存在，无法养成 ===");
			humanObj.sendSysMsg(171301);
			return;
		}
		ConfRune confRune = ConfRune.get(upRune.getSn());
		
		// 被合成消耗的纹石总经验
		int addExp = record.getExpByRuneIdList(consumeRuneIds);
		
		// 升级前等级
		int lvOld = upRune.getLevel();
		// 加成前经验
		int expOld = upRune.getExp();
		// 加成后经验
		int expNew = expOld + addExp;
		// 下一级需要的经验
		ConfLevelExp confLvExp = ConfLevelExp.get(lvOld + 1);
		int expUpgrade = getLevelUpExp(confLvExp, confRune.qualityId);
		
		int lvNew = lvOld;// 记录原来的等级
		while (expNew >= expUpgrade) {// 如果达到升级条件
			lvNew++;// 升级
			confLvExp = ConfLevelExp.get(lvNew);
			if (null == confLvExp) {
				Log.table.error("ConfLevelExp配表错误，no find sn={} ", lvNew);
				lvNew--;
				break;
			}
			if (getLevelUpExp(confLvExp, confRune.qualityId) == 0) {
				//达到符文等级上限;
				lvNew--;
				break;
			}	
			// 下一等级需要经验
			confLvExp = ConfLevelExp.get(lvNew + 1);
			if (null == confLvExp) {
				Log.table.error("===ConfLevelExp配表错误，no find sn={} == " , lvNew + 1);
				break;
			}
			expUpgrade = getLevelUpExp(confLvExp, confRune.qualityId);
		}
		
		// 穿戴符文的对象id
		long unitId = upRune.getBelongUnitId();
		upRune.setExp(expNew);
		if (lvOld != lvNew) {
			upRune.setLevel(lvNew);
			this.calc_unitRuneProp(humanObj, unitId);
		}
		// 穿戴对象
		UnitObject unitObj = this.getUnitObjByUnitId(humanObj, unitId);
		if (unitObj != null) {
			Unit unit = unitObj.getUnit();
			// 当前穿戴的符文id
			List<Long> wearRuneIds = Utils.strToLongList(unit.getRuneInfo());
			for (int i = 0; i < wearRuneIds.size(); i++) {
				if (consumeRuneIds.contains(wearRuneIds.get(i))) {
					wearRuneIds.set(i, 0l);
				}
			}
			unitObj.getUnit().setRuneInfo(Utils.ListLongToStr(wearRuneIds));
		}
		// 移除被合成的符文
		record.removeRune(consumeRuneIds);
		
		SCRuneUpgrade.Builder msg = SCRuneUpgrade.newBuilder();
		msg.setRune(RuneRecord.createDRune(upRune));
		msg.addAllConsumeRuneIds(consumeRuneIds);
		humanObj.sendMsg(msg);
		
		
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0 , "type",ActivitySevenTypeKey.Type_57);
	}
	
	/**
	 * 符文穿戴
	 * @param humanObj
	 * @param unitId 穿戴的对象id
	 * @param willRuneId 穿戴的符文id
	 * @param slotIndex 穿戴的槽位
	 */
	public void _msg_CSRuneWear(HumanObject humanObj, long unitId, long willRuneId, int slotIndex) {
		RuneRecord record = humanObj.runeRecord;
		// 对应槽位的解锁等级
		int[] slotUnlockLvs = ParamManager.divinationLatticeUnlockLvArr;
		if (slotUnlockLvs[slotIndex] > humanObj.getHuman().getLevel()) {
			Log.game.error("=== 无法穿戴，槽位未解锁 slot:{}===", slotIndex);
			humanObj.sendSysMsg(171501);
			return;
		}
		
		// 穿戴对象
		UnitObject unitObj = this.getUnitObjByUnitId(humanObj, unitId);
		if (unitObj == null) {
			Log.game.error("=== 无法穿戴，该对象不存在 partnerId:{}===", unitId);
			humanObj.sendSysMsg(171502);
			return;
		}
		Unit unit = unitObj.getUnit();
		// 当前穿戴的符文id
		List<Long> wearRuneIds = Utils.strToLongList(unit.getRuneInfo());
		// 当前操作的槽位比记录的槽位多，则补充到当前操作槽位长度
		if (wearRuneIds.size() - 1 < slotIndex) {
			for (int i = wearRuneIds.size(); i <= slotIndex; i++) {
				wearRuneIds.add(0l);
			}
		}
		// 获得被替换槽位的RuneId
		long replaceRuneId = wearRuneIds.get(slotIndex);
		
		// 判断符文是否可以穿戴
		int errorCode = this._canWearRune(record, wearRuneIds, willRuneId, replaceRuneId);
		if (errorCode != 0) {
			humanObj.sendSysMsg(errorCode);
			return;
		}

		if (replaceRuneId > 0) {
			// 脱下符文
			record.takeOffRune(replaceRuneId);
		}
		// 新戴符文
		record.wearRune(willRuneId, unitId);
		wearRuneIds.set(slotIndex, willRuneId);
		// 更新数据
		unit.setRuneInfo(Utils.ListLongToStr(wearRuneIds));
		
		// 计算这个对象符文加成的属性
		this.calc_unitRuneProp(humanObj, unitId);
		
		SCRuneWear.Builder msg = SCRuneWear.newBuilder();
		msg.setUnitId(unitId);
		msg.addAllWearRuneIds(wearRuneIds);
		msg.setWearSuccessId(willRuneId);
		if (replaceRuneId > 0) {
			msg.setReplaceRuneId(replaceRuneId);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 符文脱下
	 * @param humanObj
	 * @param slotIndex 脱下符文的槽位
	 */
	public void _msg_CSRuneTakeOff(HumanObject humanObj, long unitId, int slotIndex) {
		RuneRecord record = humanObj.runeRecord;
		// 实际大小是否满了
		if ( this.isFactLimit(record.getRuneBagNum()) ) {
			Log.game.error("=== 符文背包已满 ===");
			humanObj.sendSysMsg(170101);
			return;
		}
		
		// 对应槽位的解锁等级
		int[] slotUnlockLvs = ParamManager.divinationLatticeUnlockLvArr;
		if (slotUnlockLvs[slotIndex] > humanObj.getHuman().getLevel()) {
			Log.game.error("=== 无法操作，该槽位未解锁  slot:{}===", slotIndex);
			humanObj.sendSysMsg(171701);
			return;
		}
		// 穿戴对象
		UnitObject unitObj = this.getUnitObjByUnitId(humanObj, unitId);
		if (unitObj == null) {
			Log.game.error("=== 没有该对象，无法脱下 partnerId:{}===", unitId);
			humanObj.sendSysMsg(171702);
			return;
		}
		Unit unit = unitObj.getUnit();
		// 当前穿戴的符文id
		List<Long> wearRuneIds = Utils.strToLongList(unit.getRuneInfo());
		// 卸下的符文
		long takeOffRuneId = wearRuneIds.get(slotIndex);
		if (takeOffRuneId <= 0) {
			Log.game.error("=== 该槽位上没有符文，无法脱下  slot:{}===", slotIndex);
			humanObj.sendSysMsg(171703);
			return;
		}
		// 卸下符文
		record.takeOffRune(takeOffRuneId);
		wearRuneIds.set(slotIndex, 0l);
		// 更新数据
		unit.setRuneInfo(Utils.ListLongToStr(wearRuneIds));
		
		// 计算这个对象符文加成的属性
		this.calc_unitRuneProp(humanObj, unitId);
		
		SCRuneTakeOff.Builder msg = SCRuneTakeOff.newBuilder();
		msg.setUnitId(unitId);
		msg.setTakeOffRuneId(takeOffRuneId);
		msg.addAllWearRuneIds(wearRuneIds);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 兑换符文
	 * @param humanObj
	 */
	public void _msg_CSRuneExchange(HumanObject humanObj, int runeId){
		RuneRecord record = humanObj.runeRecord;
		if ( this.isBagLimit(record.getRuneBagNum()) ) {
			Log.game.error("=== 符文背包已满 ===");
			humanObj.sendSysMsg(170101);
			return;
		}
		ConfRune confRune = ConfRune.get(runeId);
		if (confRune == null) {
			Log.game.error("=== 兑换失败，不存在该套装id  suitSn:{}===", runeId);
			humanObj.sendSysMsg(171901);
			return;
		}
		
		if (humanObj.getHuman().getLevel() < confRune.exchangeLevel) {
			Log.game.error("=== 兑换失败，等级不足 ===");
			humanObj.sendSysMsg(171903);
			return;
		}
//		DropBag dropBag = DropManager.inst().getItem(humanObj, confRune.dropInfoId);
//		if (dropBag.getItemSn().length <= 0) {
//			Log.game.error("=== 掉落失败 dropInfoId:{} ===", confRune.dropInfoId);
//			return;
//		}
		int exchangeSn = confRune.exchangeSn;
		if (exchangeSn <= 0)
			return;
		ConfItem confItem = ConfItem.get(exchangeSn);
		if (confItem == null)
			return;
		if (confItem.itemType != ItemTypeKey.rune)
			return;
		// 判断货币是否充足
		if (!RewardHelper.checkAndConsume(humanObj, confRune.itemId, confRune.number, LogSysModType.RuneExchange)) {
			return;
		}
		Log.game.info(humanObj+"兑换了命运占卜："+confItem.name);
//		dropRune(humanObj, dropBag, runeList, itemChange, LogSysModType.RuneExchange);
		// 判断是否命格类型
		Rune rune = record.createNewRune(Utils.intValue(confItem.param[0]), humanObj.getHumanId());
		// 发送跑马灯
		sendRuneInform(humanObj, confRune);
		SCRuneExchange.Builder msg = SCRuneExchange.newBuilder();
		msg.setRune(RuneRecord.createDRune(rune));
		humanObj.sendMsg(msg);
	}
	
	
	/**
	 * 符文一键穿戴
	 * @param humanObj
	 * @param unitId
	 * @param runeIds 穿戴的符文idList
	 */
	public void _msg_CSRuneWearOneKey(HumanObject humanObj, long unitId, List<Long> runeIds) {
		RuneRecord record = humanObj.runeRecord;
		
		// 穿戴对象
		UnitObject unitObj = this.getUnitObjByUnitId(humanObj, unitId);
		if (unitObj == null) {
			Log.game.error("=== 无法穿戴，该对象不存在 partnerId:{}===", unitId);
			humanObj.sendSysMsg(171502);
			return;
		}
		// 对应槽位的解锁等级
		int[] slotUnlockLvs = ParamManager.divinationLatticeUnlockLvArr;
		if (runeIds.size() > slotUnlockLvs.length) {
			Log.game.error("=== 一键穿戴失败，穿戴符文数量过多 num:{}===", runeIds.size());
			humanObj.sendSysMsg(172101);
			return;
		}
		
		Unit unit = unitObj.getUnit();
		
		// 当前穿戴的符文id
		List<Long> wearRuneIds = Utils.strToLongList(unit.getRuneInfo());
		// 当前操作的槽位比记录的槽位多，则补充到当前操作槽位长度
		if (wearRuneIds.size() <= runeIds.size()) {
			for (int i = wearRuneIds.size(); i <= runeIds.size(); i++) {
				wearRuneIds.add(0l);
			}
		}
		// 条件不满足穿戴的符文id
		List<Long> wearFailList = new ArrayList<>();
		long willRuneId = 0;
		for (int i = 0; i < runeIds.size(); i++) {
			willRuneId = runeIds.get(i);
			// 这个槽位还没解锁
			if (slotUnlockLvs[i] > humanObj.getHuman().getLevel()) {
				wearFailList.add(willRuneId);
				continue;
			}
			
			// 判断该符文是否能穿戴
			int errorCode = this._canWearRune(record, wearRuneIds, willRuneId, 0);
			// 不能穿戴
			if (errorCode != 0) {
				wearFailList.add(willRuneId);
			}
		}
		// 没有可以穿戴的符文了
		if (runeIds.size() == wearFailList.size()) {
			Log.game.error("=== 一键穿戴失败，没有符合条件的符文 ids:{}===", Utils.ListLongToStr(runeIds));
			humanObj.sendSysMsg(172102);
			return;
		}
		
		// 穿戴成功的符文idList
		List<Long> wearSuccessIds = new ArrayList<>();
		// 一键穿戴的list index
		int index = 0;
		for (int i = 0; i < wearRuneIds.size(); i ++) {
			long runeId = wearRuneIds.get(i);
			// 不是空的槽位，不能穿戴
			if (runeId != 0) {
				continue;
			}
			willRuneId = runeIds.get(index);
			// 不满足条件的
			if (wearFailList.contains(willRuneId)) {
				continue;
			}
			// 新戴符文
			record.wearRune(willRuneId, unitId);
			wearRuneIds.set(i, willRuneId);
			wearSuccessIds.add(willRuneId);
			index++;
			// 没有可以被穿戴的符文了
			if (index >= runeIds.size()) {
				break;
			}
		}
		// 更新数据
		unit.setRuneInfo(Utils.ListLongToStr(wearRuneIds));
		
		// 计算这个对象符文加成的属性
		this.calc_unitRuneProp(humanObj, unitId);
		
		SCRuneWearOneKey.Builder msg = SCRuneWearOneKey.newBuilder();
		msg.setUnitId(unitId);
		msg.addAllWearRuneIds(wearRuneIds);
		msg.addAllWearSuccessIds(wearSuccessIds);
		humanObj.sendMsg(msg);
	}

	/**
	 * 符文一键脱下
	 * @param humanObj
	 * @param unitId
	 */
	public void _msg_CSRuneTakeOffOneKey(HumanObject humanObj, long unitId) {
		// 超过实际大小
		if ( this.isFactLimit(humanObj.runeRecord.getRuneBagNum()) ) {
			Log.game.error("=== 符文背包已满 ===");
			humanObj.sendSysMsg(170101);
			return;
		}
		// 穿戴对象
		UnitObject unitObj = this.getUnitObjByUnitId(humanObj, unitId);
		if (unitObj == null) {
			Log.game.error("=== 没有该对象，无法脱下 partnerId:{}===", unitId);
			humanObj.sendSysMsg(171702);
			return;
		}
		// 卸下所有符文
		int errorCode = this.process_TakeOffAllRune(humanObj, unitObj);
		UnitManager.inst().propsChange(unitObj, EntityUnitPropPlus.Rune);
		if (errorCode != 0) {
			Log.game.error("=== 卸下所有符文失败 errorCode:{}===", errorCode);
			humanObj.sendSysMsg(errorCode);
		}
	}
	
	/**
	 * 处理卸下所有符文(一键卸下，伙伴阵容变化时调用)
	 * @param humanObj 
	 * @return errorCode 错误码
	 */
	public int process_TakeOffAllRune(HumanObject humanObj, UnitObject unitObj) {
		long unitId = unitObj.getUnit().getId();
		// 是主角的，则设置为-1（约定规则）
		if (unitId == humanObj.getHumanId()) {
			unitId = -1;
		}
		RuneRecord record = humanObj.runeRecord;
		Unit unit = unitObj.getUnit();
		// 当前穿戴的符文id
		List<Long> wearRuneIds = Utils.strToLongList(unit.getRuneInfo());
		// 空槽位个数
		int emptyCount = 0;
		// 卸下符文
		long takeOffRuneId = 0;
		for (int i = 0 ; i < wearRuneIds.size() ; i++) {
			takeOffRuneId = wearRuneIds.get(i);
			if (takeOffRuneId == 0) {
				emptyCount ++;
				continue;
			}
			record.takeOffRune(takeOffRuneId);
			wearRuneIds.set(i, 0l);
		}
		if (emptyCount == wearRuneIds.size()) {
			// 没有可以卸下的符文
			return 172301;
		}
		// 更新数据
		unit.setRuneInfo(Utils.ListLongToStr(wearRuneIds));
		
		SCRuneTakeOffOneKey.Builder msg = SCRuneTakeOffOneKey.newBuilder();
		msg.setUnitId(unitId);
		humanObj.sendMsg(msg);
		
		return 0;
	}
	
	/**
	 * 交换阵位伙伴的携带符文
	 * @param humanObj
	 * @param downObj 下阵的伙伴
	 * @param upObj 将要替换该位置的伙伴
	 */
	public void process_SwapWearRune(HumanObject humanObj, UnitObject downObj, UnitObject upObj) {
		// 下阵的伙伴
		Unit downUnit = downObj.getUnit();
		// 需要移接的符文list
		List<Long> swapRuneIds = Utils.strToLongList(downUnit.getRuneInfo());
		// 脱下之前阵位上伙伴的符文
		this.process_TakeOffAllRune(humanObj, downObj);
		
		RuneRecord record = humanObj.runeRecord;
		// 将要替换该位置的伙伴
		Unit upUnit = upObj.getUnit();
		// 替换位置的伙伴身上的符文
		List<Long> wearRuneIds = Utils.strToLongList(upUnit.getRuneInfo());
		for (Long wearRuneId : wearRuneIds) {
			// 如果该伙伴有携带符文，则不交换两个伙伴的符文
			if (wearRuneId != 0) {
				return;
			}
		}
		
		// 一键穿戴的list index
		for (int i = 0; i < swapRuneIds.size(); i ++) {
			long runeId = swapRuneIds.get(i);
			// 新戴符文
			record.wearRune(runeId, upUnit.getId());
			swapRuneIds.set(i, runeId);
		}
		// 更新上阵伙伴符文数据
		upUnit.setRuneInfo(Utils.ListLongToStr(swapRuneIds));
		
		SCRuneWearOneKey.Builder msg = SCRuneWearOneKey.newBuilder();
		msg.setUnitId(upUnit.getId());
		msg.addAllWearRuneIds(swapRuneIds);
		msg.addAllWearSuccessIds(swapRuneIds);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 获得新符文
	 * @param humanObj
	 */
	public boolean process_ItemToNewRune(HumanObject humanObj, List<ProduceVo> list) {
		RuneRecord record = humanObj.runeRecord;
		if ( this.isFactLimit(record.getRuneBagNum()) ) {
			// 当超过上限后，获得的新符文全都直接默认移除；原来是false，邮件不会被删除
			return true; 
		}
		List<Rune> runeList = new ArrayList<>();
		ConfItem confItem = null;
		ConfRune confRune = null;
		for (ProduceVo produce : list) {
			confItem = ConfItem.get(produce.sn);
			if (confItem == null) {
				Log.game.error("=== 符文奖励异常，不给予获得===");
				continue;
			}
			int runeSn = Utils.intValue(confItem.param[0]);
			
			confRune = ConfRune.get(runeSn);
			if (confRune == null) {
				Log.game.error("=== 符文不存在，不给予获得===");
				continue;
			}
			for (int i = 0; i < produce.num; i++) {
				Rune rune = record.createNewRune(runeSn, humanObj.getHumanId());
				runeList.add(rune);
				// 发送跑马灯
				sendRuneInform(humanObj, confRune);
			}
			
		}
		
		SCRuneCreate.Builder msg = SCRuneCreate.newBuilder();
		msg.addAllRuneList(RuneRecord.createDRuneList(runeList));
		humanObj.sendMsg(msg);
		return true;
	}
	
	/**
	 * 当前符文背包是否满了（控制兑换和召唤的产出）
	 * @return
	 */
	public boolean isBagLimit(int noWearNum) {
		return noWearNum >= ParamManager.divinationBagNum;
	}
	
	/**
	 * 当前符文实际总大小是否满了（控制卸下和外部获得）
	 * @return
	 */
	public boolean isFactLimit(int noWearNum) {
		return noWearNum >= ParamManager.divinationBagActualNum;
	}
	
	/**
	 * 召唤一次纹石
	 * @param humanObj
	 * @param conf
	 * @param runeList
	 * @param itemChange
	 * @param summonCounts
	 */
	private void _summonRuneOnce(HumanObject humanObj, ConfRuneSummon conf, List<Rune> runeList, ItemChange itemChange, List<Integer> summonCounts) {
		int sn = conf.sn;
		Integer summonCount = summonCounts.get(sn);
		// 随机到的品质权重
		int[] weights = conf.weight;
		// 本次产出对应的dropSn
		int[] dropSns = conf.dropSns;
		// 掉落表sn
		int dropIndex = this.getDropIndexByLevel(conf.level, humanObj.getHuman().getLevel());
		if (summonCount == null || summonCount == 0) {
			// 该类型首次召唤
			if (summonCount == null) {
				summonCount = 0;
			}
			weights = conf.firstWeight;
			dropSns = conf.firstDropSns; 
			dropIndex = 0;
		} else if (conf.goals != 0 && summonCount % conf.goals == 0) {
			// 该类型达成轮次
			dropSns = conf.goalsDropSns;
		}
		// 下次对应召唤的sn
		int nextSn = Utils.getRandRange(weights);
		// 记录数据，下次的招募sn
		humanObj.getHumanExtInfo().setRuneSummonSn(nextSn);
		summonCounts.set(sn, ++summonCount);
		
		int dropSn = dropSns[dropIndex];
		DropBag dropBag = DropManager.inst().getItem(humanObj, dropSn);
		//设置循环最大上限
		int loopCount = 0;
		int max = ParamManager.dropCountRepeatNum;
		while (loopCount < max) {
			if(dropBag != null && dropBag.getItemSn().length > 0){
				break;
			}
			dropBag = DropManager.inst().getItem(humanObj, dropSn);
			loopCount++;
		}
		
		dropRune(humanObj, dropBag, runeList, itemChange, LogSysModType.RuneSummon);
	}
	
	/**
	 * 判断符文是否可以穿戴
	 * @param record 符文记录管理
	 * @param wearRuneIds 未被操作前穿戴的符文
	 * @param willRuneId 将要被穿戴的符文
	 * @param replaceRuneId 将要被替换的符文（没有的话填0）
	 * @return errorCode 0:可以穿 ；171503:要被穿戴的符文已经被穿 ；171504:属性不兼容当前穿戴的符文
	 */
	private int _canWearRune(RuneRecord record, List<Long> wearRuneIds, long willRuneId, long replaceRuneId) {
		Rune willRune = record.getRune(willRuneId);
		if (willRune == null) {
			Log.game.error("=== 无法穿戴，171301 willRuneId:{}===", willRuneId);
			return 171503;
		}
		if (willRune.getBelongUnitId() != 0) {
			Log.game.error("=== 无法穿戴，该符文已被穿戴 runeSn:{}，belongId:{}===", willRune.getSn(), willRune.getBelongUnitId());
			return 171503;
		}
		// 将要穿戴的符文配置
		ConfRune willConf = ConfRune.get(willRune.getSn());
		// 将要穿戴的符文所包含的属性
		List<String> willAttrType = Utils.arrayStrToListStr(willConf.attrType);
		
		Rune rune = null;
		ConfRune conf = null;
		// 判断属性是否可以兼容
		for (Long runeId : wearRuneIds) {
			rune = record.getRune(runeId);
			if (rune == null) {
				continue;
			}
			// 被替换的符文不用做属性兼容比对
			if (replaceRuneId == runeId) {
				continue;
			}
			conf = ConfRune.get(rune.getSn());
			// 遍历已经穿戴的符文属性
			for (String attrType : conf.attrType) {
				// 将要穿戴的符文属性中，拥有已经穿戴的符文属性，则return
				if (willAttrType.contains(attrType)) {
					Log.game.error("=== 无法兼容已穿戴符文属性 willRuneSn:{}，runeSn:{}===", willRune.getSn(), conf.sn);
					return 171504;
				}
			}
		}
		return 0;
	}
	
	/**
	 * 获取角色对象UnitObj
	 */
	private UnitObject getUnitObjByUnitId(HumanObject humanObj, long unitId) {
		// 穿戴对象
		UnitObject unitObj = null;
		// 主角
		if (unitId == -1) {
			unitObj = humanObj;
		} else {
			PartnerObject partnerObj = humanObj.partnerMap.get(unitId);
			if (partnerObj == null) {
				return null;
			}
			unitObj = partnerObj;
		}
		return unitObj;
	}
	
	/**
	 * 根据当前等级返回玩家当前在哪个等级区间
	 * @param levels
	 * @param curLv
	 * @return
	 */
	private int getDropIndexByLevel(int[] levels, int curLv){
		int index = 0;
		for (int i = levels.length-1; i >= 0; i--) {
			if(curLv >= levels[i]){
				index = i;
				break;
			}
		}
		return index;
	}
	
	/**
	 * 依据品质返回经验列表
	 * @param expConf 配表记录
	 * @param quality 品质
	 * @return
	 */
	private int getLevelUpExp(ConfLevelExp expConf, int quality){
		switch (quality) {
			case 1:
				return expConf.rune1Exp;
			case 2:
				return expConf.rune2Exp;
			case 3:
				return expConf.rune3Exp;
			case 4:
				return expConf.rune4Exp;
			case 5:
				return expConf.rune5Exp;
		}
		return 0;
	}
	
	

	/////////////////////////////////////
	// 任务相关
	////////////////////////////////////
	/**
	 * 满足品质的符文数量
	 * @param humanObj
	 * @param quality
	 * @return
	 */
	public int getAmountByQuality(HumanObject humanObj, int quality) {
		return humanObj.runeRecord.getAmountByQuality(humanObj, quality);
	}
	
	/**
	 * 满足等级的符文数量 
	 * @param lv
	 * @return
	 */
	public int getAmountByLv(HumanObject humanObj, int lv) {
		return humanObj.runeRecord.getAmountByLv(humanObj, lv);
	}


	/**
	 * 获得符文公告
	 */
	public void sendRuneInform(HumanObject humanObj, ConfRune confRune) {
		if (confRune.noticeId != 0) {
			// 召唤高级符文
			// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
			String content = Utils.createStr("{}|{}|{}|{}", ParamManager.sysMsgMark, confRune.noticeId, humanObj.getHuman().getName(), confRune.sn);
			InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
		}
	}
	
	/**
	 * 计算符文属性
	 * @param humanObj
	 * @param unitId 穿戴的对象id
	 */
	private void calc_unitRuneProp(HumanObject humanObj, long unitId) {
		UnitObject unitObj = humanObj.getUnitControl(unitId);
		if (unitObj == null)
			return;
		UnitManager.inst().propsChange(unitObj, EntityUnitPropPlus.Rune);
	}
	
	public PropCalcCommon calc_unitRuneProps(HumanObject humanObj, long unitId) {
		// 穿戴对象
		UnitObject unitObj = this.getUnitObjByUnitId(humanObj, unitId);
		if (unitObj == null) {
			return new PropCalcCommon();
		}
		HashMap<Integer, Integer> suitCount = new HashMap<>();
		
		// 穿戴的符文id
		List<Long> wearRuneIds = Utils.strToLongList(unitObj.getUnit().getRuneInfo());
		// 属性计算
		PropCalcCommon runePropCalc = new PropCalcCommon();
		Rune rune = null;
		ConfRune conf = null;
		for (Long runeId : wearRuneIds) {
			rune = humanObj.runeRecord.getRune(runeId);
			if (rune == null) {
				continue;
			}
			conf = ConfRune.get(rune.getSn());
			if (conf == null) {
				continue;
			}
			int lv = rune.getLevel() - 1;
			int[] attrValue = new int[conf.attrValue.length];
			for (int i = 0 ; i < attrValue.length ; i++) {
				// 属性加成值 = 基础加成 + 等级成长加成
				attrValue[i] = conf.attrValue[i] + conf.attrGrow[i] * lv;
			}
			runePropCalc.add(conf.attrType, attrValue );
			// 套装计数
			int suitId = conf.suitId;
			if (suitId > 0) {
				Integer c = suitCount.get(suitId);
				if (c == null) {
					suitCount.put(suitId, 1);
				} else {
					suitCount.put(suitId, c+1);
				}
			}
		}
		if (!suitCount.isEmpty()) {
			for (Map.Entry<Integer, Integer> e : suitCount.entrySet()) {
				int suitId = e.getKey();
				int count = e.getValue();
				ConfRunesSuit confSuit = ConfRunesSuit.get(suitId);
				if (confSuit == null) {
					continue;
				}
				int[] limit = confSuit.limit;
				if (limit.length >= 2 && count >= limit[1]) {
					runePropCalc.add(confSuit.attrType2, confSuit.attrValue2);
//					continue;
				}
				if (limit.length >= 1 && count >= limit[0]) {
					runePropCalc.add(confSuit.attrType1, confSuit.attrValue1);
//					continue;
				}
			}
		}
		return runePropCalc;
	}
	
	private void dropRune(HumanObject humanObj, DropBag dropBag, List<Rune> runeList, ItemChange itemChange, LogSysModType log) {
		// 符文记录
		RuneRecord record = humanObj.runeRecord;
		int[] items = dropBag.getItemSn();
		int[] nums = dropBag.getItemNum();
		ConfItem confItem = null;
		ConfRune confRune = null;
		for (int i = 0; i < items.length; i++) {
			int itemSn = items[i];
			int num = nums[i];
			confItem = ConfItem.get(itemSn);
			// 判断是否命格类型
			if(confItem.itemType == ItemTypeKey.rune){
				Rune rune = record.createNewRune(Utils.intValue(confItem.param[0]), humanObj.getHumanId());
				runeList.add(rune);
				confRune = ConfRune.get(rune.getSn());
				// 发送跑马灯
				sendRuneInform(humanObj, confRune);
			} else {// 掉落命格碎片
				itemChange.merge(RewardHelper.reward(humanObj, itemSn, num, log));
			}			
		}
	}
}
