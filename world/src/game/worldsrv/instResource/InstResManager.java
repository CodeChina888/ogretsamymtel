package game.worldsrv.instResource;

import java.util.ArrayList;
import java.util.List;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define.DInstEnd;
import game.msg.Define.DProduce;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EMoneyType;
import game.msg.Define.ETeamType;
import game.msg.MsgInstance.SCInstResAuto;
import game.msg.MsgInstance.SCInstResEnd;
import game.msg.MsgInstance.SCLoadInstRes;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfInstRes;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.InstRes;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.stage.types.StageObjectInstRes;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamManager;

/**
 * 资源本
 * @author Neak
 *
 */
public class InstResManager extends ManagerBase {
	
	public static InstResManager inst() {
		return inst(InstResManager.class);
	}
	
	/**
	 * 创角，初始化玩家资源本信息
	 */
	@Listener(EventKey.HumanCreate)
	public void initInstRes(Param param) {
		Human human = param.get("human");
		// 初始化玩家资源本信息
		InstRes instRes = new InstRes();
		instRes.setId(human.getId());
		instRes.persist();
	}
	
	/**
	 * 玩家其它数据加载开始：加载玩家的副本信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(InstRes.tableName);
		dbPrx.get(humanObj.getHumanId());
		dbPrx.listenResult(this::_result_loadHumanInstRes, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanInstRes(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanInst humanObj is null");
			return;
		}
		Record record = results.get();
		if (record == null) {
			Log.tower.info("===_result_loadHumanInstRes records=null");
		} else {
			// 加载数据
			InstRes instRes = new InstRes(record);
			InstResRecord instResRecord = new InstResRecord();
			instResRecord.init(instRes);
			humanObj.instResRecord = instResRecord;
			
			// 每日重置副本记录信息
			if(humanObj.isDailyFirstLogin){
				resetDaily(humanObj);
			} else {
				_send_SCLoadInstRes(humanObj);// 下发所有副本章节信息
			}
		}
		
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	
	/**
	 * 发送玩家的资源本信息
	 * @param humanObj
	 */
	private void _send_SCLoadInstRes(HumanObject humanObj) {
		InstResRecord record = humanObj.instResRecord;
		SCLoadInstRes.Builder msg = SCLoadInstRes.newBuilder();
		msg.addAllResTypeInfos(record.createDInstResTypeInfoList()); // 资源本类型信息
		msg.addAllInstResInfos(record.createDInstResInfoList());  // 资源本对应的挑战信息
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 每日重置
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDaily(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		long curTime = Port.getTime();
		int hour = Utils.getHourOfDay(curTime);
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置
			resetDaily(humanObj);  //  资源本重置挑战记录
		}
	}
	/**
	 * 资源本重置挑战记录
	 * @param humanObj
	 */
	private void resetDaily(HumanObject humanObj) {
		// 重置资源本信息
		humanObj.instResRecord.resetResInfo();
		_send_SCLoadInstRes(humanObj);
	}
	
	/**
	 * 进入资源本挑战
	 * @param humanObj
	 * @param instResSn 资源本sn（ConfInstRes.sn）
	 */
	public void _msg_CSInstResEnter(HumanObject humanObj, int instResSn) {
		// 判断副本配置是否合法
		ConfInstRes confInstRes = ConfInstRes.get(instResSn);
		if (null == confInstRes) {
			Log.table.info("===进入资源本错误：humanId={}, ConfInstRes.sn={}", humanObj.id, instResSn);
			humanObj.sendSysMsg(36);
			return;
		}
		// 判断副本配置是否合法
		ConfInstStage confInstStage = ConfInstStage.get(confInstRes.instSn);
		if (null == confInstStage) {
			Log.table.info("===进入资源本错误：humanId={}, ConfInstStage.sn={}", humanObj.id, confInstRes.instSn);
			humanObj.sendSysMsg(36);
			return;
		}
		// 存在前置关卡，并且前置关卡未挑战过，则无法进入当前关卡
		if (confInstRes.preSn != 0 && humanObj.instResRecord.getStar(confInstRes.preSn) <= 0) {
			Log.table.info("===请先通过上一关卡：humanId={}, ConfInstStage.sn={}", humanObj.id, confInstRes.instSn);
			humanObj.sendSysMsg(224205);
			return;
		}
		
		// 副本所在的地图sn
		int mapSn = confInstStage.mapSN;
		
		// 判断体力
		if (humanObj.getHuman().getAct() < confInstStage.needManual) {
			Log.stageCommon.info("=== 体力不足 act={}, ConfInstRes.sn={} ===", humanObj.getHuman().getAct(), confInstRes.instSn);
			humanObj.sendSysMsg(224201);
			return;
		}
		
		// 判断挑战等级是否满足
		if (humanObj.getHuman().getLevel() < confInstRes.lvEnter) {
			Log.stageCommon.info("=== 等级不满足 lv={}, ConfInstRes.sn={} ===", humanObj.getHuman().getLevel(), confInstRes.instSn);
			humanObj.sendSysMsg(224202);
			return;
		}
		
		// 判断今天是否在可以挑战的时间中
		int todayOfWeek = this.getTodayOfWeek();
		List<Integer> openWeekDay = Utils.intToIntegerList(confInstRes.openWeekDay);
		if (!openWeekDay.contains(todayOfWeek)) {
			Log.stageCommon.info("=== 开放日不满足  weekDay={}, ConfInstRes.sn={} ===", todayOfWeek, confInstRes.instSn);
			humanObj.sendSysMsg(224203);
			return;
		}
		
		// 获取剩余挑战次数
		int fightNumRemain = humanObj.instResRecord.getFightNumRemain(confInstRes.resType);
		if (fightNumRemain <= 0) {
			Log.stageCommon.info("=== 挑战次数不足  lv={}, ConfInstRes.sn={} ===", confInstRes.instSn);
			humanObj.sendSysMsg(224204);
			return;
		}
		
		// 创建副本
		this.create(humanObj, confInstRes.instSn, mapSn, instResSn);
	}
	private void create(HumanObject humanObj, int stageSn, int mapSn, int instResSn) {
		humanObj.setCreateRepTime();//进入异步前要先设置，避免重复操作
		
		// 创建副本
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageInstRes(stageSn, mapSn, ECrossFightType.FIGHT_INST_RES_VALUE, instResSn);
		prx.listenResult(this::_result_create, "humanObj", humanObj, "mapSn", mapSn);
		// 派发进入副本事件
		Event.fire(EventKey.InstEnter, "humanObj", humanObj, "stageSn", stageSn);
	}
	private void _result_create(Param results, Param context) {
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		int mapSn = Utils.getParamValue(context, "mapSn", -1);
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if (stageId < 0 || mapSn < 0 || null == humanObj) {
			Log.game.error("===创建失败：_result_create stageId={}, mapSn={}, humanObj={}", 
					stageId, mapSn, humanObj);
			return;
		}
		if (!humanObj.checkHumanSwitchState(results, context, true)) {
			return;
		}
		
		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_INST_RES, mapSn, stageId);
	}

	/**
	 * 资源本逃跑
	 * @param humanObj
	 */
	public void _msg_CSInstResLeave(HumanObject humanObj) {
		if (!(humanObj.stageObj instanceof StageObjectInstRes)) {
			return;
		}
		StageObjectInstRes stageObj = (StageObjectInstRes) humanObj.stageObj;
		if (stageObj == null) {
			return;
		}
		// 离开单人副本则清除组队ID
		if (humanObj.getTeamId() > 0) {
			TeamManager.inst()._msg_CSTeamLeave(humanObj);
		}
		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
	}

	/**
	 * 资源本正常结算
	 * @param humanObj
	 * @param isWin
	 * @param damageRatio 万分比值
	 */
	public void _msg_CSInstResEnd(HumanObject humanObj, boolean isWin, int damageRatio) {
		// 不再资源本挑战中
		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectInstRes)) {
			Log.tower.error(" === 结算失败，不再爬塔挑战中 ===");
			return;
		}
		// 玩家所在资源本场景
		StageObjectInstRes stageObj = (StageObjectInstRes) (humanObj.stageObj);
		// 关卡配置
		ConfInstStage confInstStage = stageObj.confInstStage;
		// 资源本配置
		ConfInstRes confInstRes = stageObj.confInstRes;
		
		// 应答消息
		SCInstResEnd.Builder msg = SCInstResEnd.newBuilder();
		msg.setInstResSn(confInstRes.sn);
		DInstEnd.Builder dInstEnd = null;
		if (!isWin) {
			dInstEnd = DInstEnd.newBuilder();
			dInstEnd.setInstSn(confInstStage.sn); // 对应副本场景sn
			msg.setIsWin(isWin);
			humanObj.sendMsg(msg);
			return;
		}
		
		// 成功通关副本，则扣除体力，给予奖励
		int costActValue = confInstStage.needManual;
		if (costActValue > 0) {
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.act_VALUE, costActValue, LogSysModType.InstRes)) {
				return;
			}
		}
		
		InstResRecord instResRecord = humanObj.instResRecord;
//		int starNew = InstanceManager.inst().countStars(stars);
		
		// 增加挑战次数
		instResRecord.addFightNum(confInstRes.resType, confInstRes.difficulty, 1);
		
		// 根据伤害，获得对应的index
		int awardIndex = this.getDamageIndex(confInstRes, damageRatio);
		// 结算奖励
		msg.addAllProduces(giveInstResReward(humanObj, confInstStage, confInstRes, awardIndex));
		
		int star = awardIndex;
		if (damageRatio == Utils.I10000) {
			star = 3; // 完美通过为3星
		}else if (damageRatio < Utils.I10000 && star != 0) {
			star = 2; // 不是一档伤害则，且不是完美则为2星
		} else if(star == 0) {
			star = 1; // 过关默认1星
		}
		// 设置星数
		msg.setStar(star);
		instResRecord.setInstResStar(confInstRes.sn, star);
		
		// 设置资源本类型信息
		msg.setResTypeInfo(instResRecord.createDInstResTypeInfo(confInstRes.resType));
		
		msg.setDamageRatio(damageRatio);
		msg.setIsWin(isWin);
		
		humanObj.sendMsg(msg);
		
		// 发布通过资源本事件
		Event.fire(EventKey.InstResPass, "humanObj", humanObj, "num", 1, "instSn", confInstRes.sn);
	}
	
	/**
	 * 资源本扫荡
	 * @param humanObj
	 * @param instResSn
	 */
	public void _msg_CSInstResAuto(HumanObject humanObj, int instResSn) {
		// 判断副本配置是否合法
		ConfInstRes confInstRes = ConfInstRes.get(instResSn);
		if (null == confInstRes) {
			Log.table.info("===扫荡资源本错误：humanId={}, ConfInstRes.sn={}", humanObj.id, instResSn);
			return;
		}
		// 判断副本配置是否合法
		ConfInstStage confInstStage = ConfInstStage.get(confInstRes.instSn);
		if (null == confInstStage) {
			Log.table.info("===扫荡资源本错误：humanId={}, ConfInstStage.sn={}", humanObj.id, confInstRes.instSn);
			return;
		}
		
		// 判断挑战等级是否满足
		if (humanObj.getHuman().getLevel() < confInstRes.lvEnter) {
			Log.stageCommon.info("=== 等级不满足 lv={}, ConfInstRes.sn={} ===", humanObj.getHuman().getLevel(), confInstRes.instSn);
			humanObj.sendSysMsg(224202);
			return;
		}
		
		// 判断今天是否在可以挑战的时间中
		int todayOfWeek = this.getTodayOfWeek();
		List<Integer> openWeekDay = Utils.intToIntegerList(confInstRes.openWeekDay);
		if (!openWeekDay.contains(todayOfWeek)) {
			Log.stageCommon.info("=== 开放日不满足  weekDay={}, ConfInstRes.sn={} ===", todayOfWeek, confInstRes.instSn);
			humanObj.sendSysMsg(224203);
			return;
		}
		
		InstResRecord instResRecord = humanObj.instResRecord;
		// 判断是否满足三星扫荡需求
		int star = instResRecord.getStar(instResSn);
		if (star < 3) {
			Log.stageCommon.info("=== 星数不满足  star={}, ConfInstRes.sn={} ===", star, confInstRes.instSn);
			humanObj.sendSysMsg(220901);
			return;
		}
		
		// 获取剩余挑战次数
		int fightNumRemain = humanObj.instResRecord.getFightNumRemain(confInstRes.resType);
		if (fightNumRemain <= 0) {
			Log.stageCommon.info("=== 扫荡次数不足  lv={}, ConfInstRes.sn={} ===", confInstRes.instSn);
			humanObj.sendSysMsg(224204);
			return;
		}
		
		// 扫荡副本通关处理
		instResAuto(humanObj, confInstStage, confInstRes, fightNumRemain);
	}
	/**
	 * 扫荡通关副本：修改副本进度及给副本奖励相关
	 * @param num 扫荡次数
	 */
	private void instResAuto(HumanObject humanObj, ConfInstStage confInstStage, ConfInstRes confInstRes, int num) {
		InstResRecord instResRecord = humanObj.instResRecord;
		
		LogSysModType logType = LogSysModType.InstResAuto;
		int instResSn = confInstRes.sn;
		int resType = confInstRes.resType;
		
		// 扣除体力
		int costActValue = confInstStage.needManual;
		if (costActValue > 0) {
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.act_VALUE, costActValue * num, logType)) {
				return; // 体力不足，则return
			}
		}
		
		// 修改副本已挑战次数
		instResRecord.addFightNum(resType, confInstRes.difficulty, num);
		
		SCInstResAuto.Builder msg = SCInstResAuto.newBuilder();
		msg.setResTypeInfo(instResRecord.createDInstResTypeInfo(confInstRes.resType));
		// 对应的伤害百分比：等于星级
		int awardIndex = this.getDamageIndex(confInstRes, Utils.I10000);
		if (awardIndex == -1) {
			awardIndex = 0;
		}
		// 资源本掉落
		for (int index = 1; index <= num; index++) {
			// 扫荡的伤害默认是最大的
			msg.addAllProduces(giveInstResReward(humanObj, confInstStage, confInstRes, awardIndex));
		}
		// 返回消息
		humanObj.sendMsg(msg);
		
		// 发布扫荡资源本消息
		Event.fire(EventKey.InstResPass, "humanObj", humanObj, "num", num, "instSn", instResSn);			
		Event.fire(EventKey.InstResAutoPass, "humanObj", humanObj, "num", num, "instSn", instResSn);
	}
	
	/**
	 * 根据伤害比值，获得奖励的index
	 * @param ratio 造成伤害万分比
	 */
	private int getDamageIndex(ConfInstRes confInstRes, int ratio) {
		int[] damageRationAry = confInstRes.damageRatio;
		for (int i = 0; i < damageRationAry.length - 1; i++) {
			// 超过下限，小于等于上限，则在这个区间内
			if (ratio > damageRationAry[i] && ratio <= damageRationAry[i + 1]) {
				return i;
			}
		}
		return 0;
	}
	
	/**
	 * 资源本奖励
	 */
	private List<DProduce> giveInstResReward(HumanObject humanObj, ConfInstStage confInstStage, ConfInstRes confInstRes, int awardIndex) {
		List<DProduce> list = new ArrayList<>();
		if (awardIndex >= confInstRes.itemNum.length) {
			awardIndex = confInstRes.itemNum.length - 1;
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, confInstRes.itemSn, confInstRes.itemNum[awardIndex], LogSysModType.InstResDrop);
		list = itemChange.getProduce();
		return list;
	}
	
	/**
	 * 是否是挑战日
	 * @return
	 */
	private int getTodayOfWeek() {
		int hour = Utils.getHourOfDay();
		// 获得今天是本周的星期几
		int todayOfWeek = Utils.getDayOfWeek(Port.getTime());
		// 当前小时小于跨天，则时间 当前日 -1
		if (hour < ParamManager.dailyHourReset) {
			todayOfWeek -= 1;
			if (todayOfWeek == 0) {
				todayOfWeek = 7;
			}
		}
		return todayOfWeek;
	}
}
