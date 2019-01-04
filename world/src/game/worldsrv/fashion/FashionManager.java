package game.worldsrv.fashion;

import game.msg.Define.DFashionHenshin;
import game.msg.Define.EContainerType;
import game.msg.Define.EFashionHenshinType;
import game.msg.Define.EInformType;
import game.msg.Define.EModeType;
import game.msg.MsgFashion.SCFashionBuyHenshin;
import game.msg.MsgFashion.SCFashionHenshinTimeOut;
import game.msg.MsgFashion.SCFashionHenshinWear;
import game.msg.MsgFashion.SCFashionTimeOut;
import game.msg.MsgFashion.SCFashionUnlock;
import game.msg.MsgFashion.SCFashionWear;
import game.msg.MsgFashion.SCItemUseFashionHenshin;
import game.msg.MsgFashion.SCLoginFashionHenshin;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfFashion;
import game.worldsrv.config.ConfFashionHenshin;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfRoleModel;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Fashion;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.ItemPackType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.inform.InformManager;
import game.worldsrv.item.ItemManager;
import game.worldsrv.item.ItemPack;
import game.worldsrv.item.ItemTypeKey;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.List;
import java.util.Map;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;

public class FashionManager extends ItemManager {
	
	public static final int Type_Fashion = 0;
	public static final int Type_Henshin = 1;
	
	public static final int QM_HUMANLV = 1;
	public static final int QM_VIPLV = 2;
	public static final int QM_PARTNERHAVE = 3;
	
	@Override
	public ItemPack getPack(HumanObject humanObj) {
		return humanObj.itemBag;
	}

	@Override
	protected ItemPackType getPackType() {
		return ItemPackType.Bag;
	}

	@Override
	protected EContainerType getContainerType() {
		return EContainerType.Bag;
	}
	
	public static FashionManager inst() {
		return inst(FashionManager.class);
	}
	
	/**
	 * 创角，初始化玩家时装相关的信息
	 */
	@Listener(EventKey.HumanCreate)
	public void initFashionInfo(Param param) {
		Human human = param.get("human");
		// 初始化玩家时装信息
		Fashion fh = new Fashion();
		fh.setId(human.getId());
		
		// 新手套装
		ConfFashion confFashion = ConfFashion.get(ConfigKeyFormula.getFashionSn(human.getSn(), 1));
		if (confFashion == null) {
			Log.table.error("ConfFashions配表错误，no find sn={}", 0);
			return;
		}
		// 设置第一套
		fh.setFashionSn(String.valueOf(confFashion.sn));
		fh.setFashionLimitTime("-1");
		fh.persist();
		
		// 当前穿着的时装
		human.setFashionSn(confFashion.sn);
		// 默认对应时装的模型sn
		human.setDefaultModelSn(confFashion.modelSn);
		// 当前使用模型
		human.setModelSn(confFashion.modelSn);
		human.setHeadSn(confFashion.modelSn);
	}
	
	/**
	 * 玩家其它数据加载开始：加载玩家的变装信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Fashion.tableName);
		dbPrx.get(humanObj.getHumanId());
		dbPrx.listenResult(this::_result_loadHumanFashion, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanFashion(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanItemBag humanObj is null");
			return;
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
		
		Record record = results.get();
		if (record == null) {
			Log.game.debug("===没有解锁的时装变装===");
			return;
		} else {
			// 加载数据
			Fashion f = new Fashion(record);
			humanObj.fashionRecord.init(f);
			_send_SCLoginFashion(humanObj);
		}
	}
	
	/**
	 * 登录下发玩家变装信息
	 * @param humanObj
	 */
	private void _send_SCLoginFashion(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		FashionRecord fr = humanObj.fashionRecord;
		
		// 下发登录
		SCLoginFashionHenshin.Builder msg = SCLoginFashionHenshin.newBuilder();
		// 时装
		msg.addAllDFashionList(fr.createDFashionList(human.getFashionSn()));
		// 变装
		msg.addAllDFashionHenshinList(fr.createDHenshinList(human.getHenshinSn()));
		msg.setModelSn(humanObj.getHuman().getModelSn());
		humanObj.sendMsg(msg);
		
		// 判断时装过期
		this.judge_FashionTimeOut(humanObj);
		// 开启时装变身规则
		this.openFashionTickTimer(humanObj);
		
		// 判断变装过期
		this.judge_HenshinTimeOut(humanObj);
		// 登录开启玩家限时变身规则
		this.openHenshinTickTimer(humanObj); 
	}
	
	/////////////////////////////////////////////////////////
	////// 套装相关
	/////////////////////////////////////////////////////////
	
	/**
	 * 打开套装
	 * @param humanObj
	 */
	public void _msg_CSFashionOpen(HumanObject humanObj) {
		// 打开变身装，判断过期
		this.judge_FashionTimeOut(humanObj);
	}
	
	/**
	 * 解锁套装
	 * @param humanObj
	 */
	public void _msg_CSFashionUnlock(HumanObject humanObj, int fashionSn) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeEquipFashion, humanObj)) {
			Log.game.error("===_msg_CSFashionUnlock 时装未开放 ===");
			humanObj.sendSysMsg(23);
			return;
		}
		
		FashionRecord fr = humanObj.fashionRecord;
		Map<Integer, Long> fashionMap = fr.getFashionMap();
		if (fashionMap.containsKey(fashionSn)) {
			Log.human.error("=== 无法重复解锁套装  fashionSn:{}===", fashionSn);
			humanObj.sendSysMsg(140301);
			return;
		}
		// 时装配置表
		ConfFashion conf = ConfFashion.get(fashionSn);
		if (conf == null) {
			Log.human.error("=== 无法解锁，配置不存在  fashionSn:{}===", fashionSn);
			humanObj.sendSysMsg(140302);
			return;
		}
		
		int[] cost = Utils.appendInt(conf.cost, conf.costMoney);
		int[] costNum = Utils.appendInt(conf.costNum, conf.costMoneyNum);
		// 检测道具是否足够
		if (!RewardHelper.checkAndConsume(humanObj, cost, costNum, LogSysModType.FashionUnlock)) {
			return;
		}
		
		long limitTime = -1;
		if (conf.time > 0) {
			limitTime = Port.getTime() + conf.time * Time.MIN;
		}
		// 获得套装
		fr.addFashion(conf.sn, limitTime);
		
		// 解锁套装属性加成
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.Fashion);
		
		SCFashionUnlock.Builder msg = SCFashionUnlock.newBuilder();
		msg.setFashionSn(fashionSn);
		humanObj.sendMsg(msg);
		
		// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
		String content = Utils.createStr("{}|{}|{}|{}", ParamManager.sysMsgMark, 999022, 
				humanObj.getHuman().getName(), conf.sn);
		InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
	}
	
	/**
	 * 穿套装
	 * @param humanObj
	 */
	public void _msg_CSFashionWear(HumanObject humanObj, int fashionSn) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeEquipFashion, humanObj)) {
			Log.game.error("===_msg_CSFashionWear 时装未开放 ===");
			humanObj.sendSysMsg(23);
			return;
		}
		
		FashionRecord fr = humanObj.fashionRecord;
		Map<Integer, Long> fashionMap = fr.getFashionMap();
		Long limitTime = fashionMap.get(fashionSn);
		if (limitTime == null) {
			Log.human.error("=== 该套装未解锁，无法穿戴  fashionSn:{}===", fashionSn);
			humanObj.sendSysMsg(140501);
			return;
		}
		long curTime = Port.getTime();
		if (limitTime != -1 && curTime >= limitTime) {
			Log.human.error("=== 套装过期，无法穿戴  fashionSn:{}===", fashionSn);
			humanObj.sendSysMsg(140502);
			return;
		}
		
		ConfFashion conf = ConfFashion.get(fashionSn);
		if (conf == null) {
			Log.human.error("=== 配置异常，无法穿戴  fashionSn:{}===", fashionSn);
			humanObj.sendSysMsg(140503);
			return;
		}
		
		// 当前穿着的时装
		humanObj.getHuman().setFashionSn(conf.sn);
		// 默认对应时装的模型sn
		humanObj.getHuman().setDefaultModelSn(conf.modelSn);
		// 当前使用模型
		humanObj.getHuman().setModelSn(this.getModelSn(humanObj.getHuman()));
		
		SCFashionWear.Builder msg = SCFashionWear.newBuilder();
		DFashionHenshin.Builder dmsg = msg.getDFashionHenshinBuilder();
		dmsg.setFashionSn(conf.sn);
		dmsg.setState(EFashionHenshinType.fashionHenshinEquiped);
		dmsg.setLimitTime(limitTime);
		msg.setModelSn(humanObj.getHuman().getModelSn());
		humanObj.sendMsg(msg);
	}
	
	/////////////////////////////////////////////////////////
	////// 变身相关
	/////////////////////////////////////////////////////////
	/**
	 * 打开变身装界面，处理过期变装(预留，暂时不用和客户端对接)
	 * @param humanObj
	 */
	public void _msg_CSFashionHenshinOpen(HumanObject humanObj) {
		// 打开变身装，判断过期
		this.judge_HenshinTimeOut(humanObj);		
	}
	
	/**
	 * 使用道具：类型是变身卡
	 * @param param
	 */
	@Listener(value = EventKey.ItemUse, subInt = ItemTypeKey.fashionHenshin)
	public void _listener_ItemUseFashionHenshin(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ItemUse humanObj is null");
			return;
		}
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeEquipSoul, humanObj)) {
			Log.game.error("===_listener_ItemUseFashionHenshin 变装未开放 ===");
			humanObj.sendSysMsg(23);
			return;
		}
		
		// 道具配置
		ConfItem confItem = Utils.getParamValue(param, "confItem", null);
		if(null == confItem){
			Log.game.error("===_listener_ItemUse conf=null");
			return;
		}
		
		int[] confparam = Utils.strToIntArraySplit(confItem.param[0]);
		// 对应的时装sn
		int fashionSn = Utils.intValue(confparam[0]);
		
		ConfFashionHenshin conf = ConfFashionHenshin.get(fashionSn);
		if (conf == null) {
			Log.game.error("=== 变装失败，配置表不存在，fashionSn = {}", fashionSn);
			humanObj.sendSysMsg(142301);
			return;
		}
		
		if (!RewardHelper.checkAndConsume(humanObj, conf.item, 1, LogSysModType.FashionHenshinUseItem)) {
			Log.game.error("=== 变装失败，道具不足， = {}", conf.item);
			humanObj.sendSysMsg(142302);
			return;
		}
		// 持续时间
		int durationMin = conf.time;
		long limitTime = Port.getTime() + durationMin * Time.MIN;
		
		FashionRecord fr = humanObj.fashionRecord;
		fr.addHenshin(conf.sn, limitTime);
		
		Human human = humanObj.getHuman();
		human.setHenshinSn(conf.sn);
		// 设置当前模型
		human.setModelSn(this.getModelSn(human));
		// 重新开启变身计时器
		this.openHenshinTickTimer(humanObj);
		
		// 计算变身属性
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.FashionHenshin);

		
		SCItemUseFashionHenshin.Builder msg = SCItemUseFashionHenshin.newBuilder();
		DFashionHenshin.Builder dmsg = msg.getDFashionHenshinBuilder();
		dmsg.setFashionSn(conf.sn);
		dmsg.setState(EFashionHenshinType.fashionHenshinEquiped);
		dmsg.setLimitTime(limitTime);
		msg.setModelSn(human.getModelSn());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 购买一张变身卡
	 * @param humanObj
	 * @param fashionSn
	 */
	public void _msg_CSFashionBuyHenshin(HumanObject humanObj, int fashionSn) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeEquipSoul, humanObj)) {
			Log.game.debug("===_msg_CSFashionBuyHenshin 变装未开放 ===");
			humanObj.sendSysMsg(23);
			return;
		}
		
		ConfFashionHenshin conf = ConfFashionHenshin.get(fashionSn);
		if (conf == null) {
			Log.game.debug("=== 购买失败，配置表不存在，fashionSn = {}", fashionSn);
			humanObj.sendSysMsg(142401);
			return;
		}
		
		int getType = conf.getType;
		int param1 = conf.param1;
//		int param2 = conf.param2;
		if(getType == QM_HUMANLV) {
			if (humanObj.getHuman().getLevel() < param1) {
				Log.game.debug("=== 变装失败，玩家等级不足，fashionSn = {}", fashionSn);
				humanObj.sendSysMsg(142402);
				return;
			}
		} else if (getType == QM_VIPLV) {
			if (humanObj.getHuman().getLevel() < param1) {
				Log.game.debug("=== 变装失败，特权等级不足，fashionSn = {}", fashionSn);
				humanObj.sendSysMsg(142403);
				return;
			}
		} else if (getType == QM_PARTNERHAVE) {
			if (!PartnerManager.inst().partnerActivityHand(humanObj, param1)) {
				Log.game.debug("=== 变装失败，伙伴不曾拥有过，fashionSn = {}", fashionSn);
				humanObj.sendSysMsg(142404);
				return;
			}
		}
		
		if (!RewardHelper.checkAndConsume(humanObj, conf.money, conf.moneyNum, LogSysModType.FashionHenshinBuyNew)) {
			return;
		}
		
		// 添加道具
		RewardHelper.reward(humanObj, conf.item, 1, LogSysModType.FashionHenshinBuyNew);
		
		SCFashionBuyHenshin.Builder msg = SCFashionBuyHenshin.newBuilder();
		msg.setFashionSn(conf.sn);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 穿已经解锁的变身装（预留，暂时不用和客户端对接）
	 * @param humanObj
	 * @param fashionSn
	 */
	public void _msg_CSFashionHenshinWear(HumanObject humanObj, int fashionSn) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeEquipSoul, humanObj)) {
			Log.game.error("===_msg_CSFashionHenshinWear 变装未开放 ===");
			humanObj.sendSysMsg(23);
			return;
		}
		
		FashionRecord fr = humanObj.fashionRecord;
		// 变装信息
		Map<Integer, Long> henshinMap = fr.getHenshinMap();
		
		if (!henshinMap.containsKey(fashionSn)) {
			Log.game.error("=== 变身卡未解锁 ===");
			humanObj.sendSysMsg(142701);
			return;
		}
		
		ConfFashionHenshin conf = ConfFashionHenshin.get(fashionSn);
		if (conf == null) {
			Log.game.error("=== 变装失败，配置表不存在，fashionSn = {}", fashionSn);
			humanObj.sendSysMsg(142702);
			return;
		}
		
		// 重新开启变身计时器
		this.openHenshinTickTimer(humanObj);
		
		Human human = humanObj.getHuman();
		human.setHenshinSn(conf.sn);
		// 设置当前模型
		human.setModelSn(this.getModelSn(human));
		
		// 计算变身属性
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.FashionHenshin);
		
		SCFashionHenshinWear.Builder msg = SCFashionHenshinWear.newBuilder();
		msg.setFashionSn(fashionSn);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 开启时装限时规则
	 * @param humanObj
	 */
	public void openFashionTickTimer(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		long time = Port.getTime();// 当前时间
		FashionRecord fr = humanObj.fashionRecord;
		// 时装信息
		Map<Integer, Long> fashionMap = fr.getFashionMap();
				
		// 当前穿戴的sn
		int curSn = human.getFashionSn();
		
		Long limitTime = fashionMap.get(curSn);
		// 不存在该时装或时装为永久
		if (limitTime == null || limitTime == -1) {
			humanObj.ttFashion.stop();
			return;
		}
		// 过期了
		if (time >= limitTime) {
			humanObj.ttFashion.stop();
			// 判断过期并且处理
			this.judge_FashionTimeOut(humanObj);
			return;
		}
		// 间隔时间
		long inveral = limitTime - time;
		humanObj.ttFashion.stop();// 先关闭后开启计时器
		humanObj.ttFashion.start(inveral);// 开启计时器
	}
	
	/**
	 * 开启变身限时规则
	 * @param humanObj
	 */
	public void openHenshinTickTimer(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		long time = Port.getTime();// 当前时间
		FashionRecord fr = humanObj.fashionRecord;
		// 变装信息
		Map<Integer, Long> henshinMap = fr.getHenshinMap();
				
		// 当前穿戴的sn
		int curSn = human.getHenshinSn();
		
		Long limitTime = henshinMap.get(curSn);
		// 不存在该变身
		if (limitTime == null) {
			humanObj.ttFashionHenshin.stop();
			return;
		}
		// 过期了
		if (time >= limitTime) {
			humanObj.ttFashionHenshin.stop();
			// 判断过期并且处理
			this.judge_HenshinTimeOut(humanObj);
			return;
		}
		// 间隔时间
		long inveral = limitTime - time;
		humanObj.ttFashionHenshin.stop();// 先关闭后开启计时器
		humanObj.ttFashionHenshin.start(inveral);// 开启计时器
	}
	
	/**
	 * 判断时装是否过期 
	 * @param humanObj
	 */
	private void judge_FashionTimeOut(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		
		FashionRecord fr = humanObj.fashionRecord;
		// 时装信息
		Map<Integer, Long> fashionMap = fr.getFashionMap();
		
		if (fashionMap.size() == 0) {
			humanObj.ttFashion.stop();
			return;
		}
		// 取得过期的sn
		List<Integer> timeOutSns = fr.getTimeOutSns(Type_Fashion);
		// 没有过期的
		if (timeOutSns == null || timeOutSns.isEmpty()) {
			return;
		}
		
		// 当前穿戴变身装已经过期
		if (timeOutSns.contains(human.getFashionSn())) {
			// 新手套装
			ConfFashion confFashion = ConfFashion.get(ConfigKeyFormula.getFashionSn(human.getSn(), 1));
			human.setFashionSn(confFashion.sn);
			// 设置当前modelSn
			human.setModelSn(this.getModelSn(human));
		}
		// 清空变身属性
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.Fashion);

		// 下发过期
		SCFashionTimeOut.Builder msg = SCFashionTimeOut.newBuilder();
		msg.addAllFashionSn(timeOutSns);
		msg.setModelSn(humanObj.getHuman().getModelSn());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 判断变装是否过期 
	 * @param humanObj
	 */
	private void judge_HenshinTimeOut(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		
		FashionRecord fr = humanObj.fashionRecord;
		// 变装信息
		Map<Integer, Long> henshinMap = fr.getHenshinMap();
		
		if (henshinMap.size() == 0) {
			humanObj.ttFashionHenshin.stop();
			return;
		}
		// 记录过期的sn
		List<Integer> timeOutSns = fr.getTimeOutSns(Type_Henshin);
		// 没有过期的
		if (timeOutSns == null || timeOutSns.isEmpty()) {
			return;
		}
		
		// 当前穿戴变身装已经过期
		if (timeOutSns.contains(human.getHenshinSn())) {
			// 玩家变装sn置0
			human.setHenshinSn(0);
			// 设置当前modelSn
			human.setModelSn(this.getModelSn(human));
			
			// 清空变身属性
			UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.FashionHenshin);
		}
		
		// 下发过期
		SCFashionHenshinTimeOut.Builder msg = SCFashionHenshinTimeOut.newBuilder();
		msg.addAllFashionSn(timeOutSns);
		msg.setModelSn(humanObj.getHuman().getModelSn());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 获取玩家当前模型
	 */
	private int getModelSn(Human human) {
		int defaultModelSn = human.getDefaultModelSn();
		int fashionSn = human.getHenshinSn();
		ConfFashionHenshin confFashion = ConfFashionHenshin.get(fashionSn);
		if (confFashion == null) {
			return defaultModelSn;
		}
		// 变装模型sn
		int henshinSn = confFashion.modleSn;
		
		// 当前角色模型配置
		ConfRoleModel confRMM = ConfRoleModel.get(defaultModelSn);
		// 变装模型配置
		ConfRoleModel confRMF = ConfRoleModel.get(henshinSn);
		if (confRMM == null || confRMF == null) {
			return defaultModelSn;
		}
		int sortM = confRMM.sort;
		int sortF = confRMF.sort;
		if (sortM > sortF) {
			return defaultModelSn;
		} else if (sortM == sortF) {
			return (defaultModelSn > henshinSn) ? defaultModelSn : henshinSn;
		}
		return henshinSn;
	}
	
	/**
	 * @param humanObj
	 * @return 玩家时装属性
	 */
	public PropCalcCommon calc_fashionProp(HumanObject humanObj) {
		long time = Port.getTime();// 当前时间
		FashionRecord fr = humanObj.fashionRecord;
		PropCalcCommon prop = new PropCalcCommon();
		Map<Integer, Long> henshinMap = fr.getFashionMap();
		for (Map.Entry<Integer, Long> entry : henshinMap.entrySet()) {
			// 时装配置表
			int fashionSn = entry.getKey();
			ConfFashion conf = ConfFashion.get(fashionSn);
			if (conf == null) {
				Log.human.error("=== 无法解锁，配置不存在  fashionSn:{}===", fashionSn);
				continue;
			}
			long limitTime = entry.getValue();
			// 不是永久，当前时间没到过期时间，则continue
			if (limitTime != -1 && time > limitTime) {
				continue;
			}
			prop.add(conf.attrType, conf.attrValue);
		}
		return prop;
	}
	
	/**
	 * 计算件时装属性
	 * @param humanObj
	 * @param fashionSns 时装sns
	 * @param isUnlock true：获得 ，false：过期失去
	 */
	public PropCalcCommon calc_fashionHenshinProp(HumanObject humanObj) {
		long time = Port.getTime();// 当前时间
		FashionRecord fr = humanObj.fashionRecord;
				
		PropCalcCommon prop = new PropCalcCommon();
		// 当前穿戴的sn
		int curSn = humanObj.getHuman().getHenshinSn();
		if (curSn == 0)
			return prop;
		// 变装信息
		Map<Integer, Long> henshinMap = fr.getHenshinMap();
		Long limitTime = henshinMap.get(curSn);
		// 不存在该变身
		if (limitTime == null) {
			return prop;
		}
		// 过期了
		if (time >= limitTime) {
			return prop;
		}
		ConfFashionHenshin conf = ConfFashionHenshin.get(curSn);
		// 计算属性
		prop.add(conf.attrType, conf.attrValue);
		return prop;
	}
}
