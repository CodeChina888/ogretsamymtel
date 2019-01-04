package crosssrv.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Config;
import core.support.Distr;
import core.support.RandomMT19937;
import core.support.TickTimer;
import core.support.Time;
import core.support.Utils;
import crosssrv.character.CombatantObject;
import crosssrv.character.CombatantObjectService;
import crosssrv.combatant.CombatantGlobalInfo;
import crosssrv.combatant.CombatantManager;
import crosssrv.entity.FightRecord;
import crosssrv.groupFight.GroupFightServiceProxy;
import crosssrv.seam.CrossPort;
import crosssrv.singleFight.SingleFightServiceProxy;
import crosssrv.support.Log;
import game.msg.Define.DTurnbasedBuff;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.DTurnbasedTeamObjs;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EStanceType;
import game.msg.Define.ETeamType;
import game.msg.Define.EWorldObjectType;
import game.msg.MsgCross.SCEnemyDisconnect;
import game.msg.MsgTurnbasedFight.SCTurnbasedActionStart;
import game.msg.MsgTurnbasedFight.SCTurnbasedAutoFight;
import game.msg.MsgTurnbasedFight.SCTurnbasedBuff;
import game.msg.MsgTurnbasedFight.SCTurnbasedCastSkill;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.msg.MsgTurnbasedFight.SCTurnbasedHumanSelSkill;
import game.msg.MsgTurnbasedFight.SCTurnbasedObjectEnter;
import game.msg.MsgTurnbasedFight.SCTurnbasedObjectLeave;
import game.msg.MsgTurnbasedFight.SCTurnbasedRageSkillWaitList;
import game.msg.MsgTurnbasedFight.SCTurnbasedRoundChange;
import game.msg.MsgTurnbasedFight.SCTurnbasedRoundEnd;
import game.msg.MsgTurnbasedFight.SCTurnbasedSpeed;
import game.msg.MsgTurnbasedFight.SCTurnbasedStageStep;
import game.msg.MsgTurnbasedFight.SCTurnbasedStopFight;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfParam;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfPropAdd;
import game.worldsrv.config.ConfSkill;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.entity.PartnerMirror;
import game.worldsrv.entity.Unit;
import game.worldsrv.fightParam.ResultParam;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.support.D;
import game.worldsrv.support.PropCalc;
import turnbasedsrv.buff.BuffTriggerData;
import turnbasedsrv.enumType.SkillType;
import turnbasedsrv.enumType.StageOpType;
import turnbasedsrv.fightObj.FightGeneralObject;
import turnbasedsrv.fightObj.FightHumanObject;
import turnbasedsrv.fightObj.FightMonsterObject;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.param.FightParamBase;
import turnbasedsrv.param.FightParamBoolean;
import turnbasedsrv.param.FightParamFighterList;
import turnbasedsrv.param.FightParamInitPropMonsterConf;
import turnbasedsrv.param.FightParamInt;
import turnbasedsrv.param.FightParamMonsterInfo;
import turnbasedsrv.param.FightParamPlot;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.skill.Skill;
import turnbasedsrv.skill.SkillCastData;
import turnbasedsrv.skill.SkillTargetData;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.stageEvent.EventFighterStageShow;
import turnbasedsrv.stageEvent.EventPlot;
import turnbasedsrv.support.JsonKey;
import turnbasedsrv.value.ValueBase;
import turnbasedsrv.value.ValueDouble;
import turnbasedsrv.value.ValueFactory;

/**
 * @author landy
 */
public abstract class CrossStageObject extends FightStageObject {
	protected List<Long> combatantIds = new ArrayList<>();
	protected Map<Long, CombatantObject> combatantObjs = new HashMap<>();// 该地图内所有竞技者
	protected Map<Long, Boolean> combatantReadyMap = new HashMap<>();
	private CrossPort port;

	public boolean isClient; // 是否前端战斗

	public String name; // 地图名称

	public int fightType;// 战斗类型
	private TickTimer waitDestoryTimer; // 等待地图销毁定时器
	private static final long WAIT_DESTORY_TIME = 10 * Time.SEC;// 删除地图时间
	private static final long WAIT_CHECK_TIME = 1 * Time.MIN;// 等待客户端响应时间
	
	protected ConfPropAdd team1_ConfPropAdd = null; // 队伍1，战力压制配表PorpAdd
	protected ConfPropAdd team2_ConfPropAdd = null; // 队伍2，战力压制配表PorpAdd
	

	/** 等待玩家定时器 **/
	private TickTimer ttWaitCheck;
	/** 等待玩家出手定时器 **/
	private TickTimer ttWaitCastSkill;

	/** 是否是录制录像战斗 **/
	public boolean isRecordFight;

	/** 录像数据 **/
	public FightRecord fightRecord;
	/** 录像记录:自动战斗次数 **/
	private int fightRecordAutoTimes = 0;
	/** 录像记录:施放技能次数 **/
	private int fightRecordCastTimes = 0;
	/** 录像记录:自动战斗变更数据 **/
	public Map<ETeamType, Boolean> fightRecordAutoFightMap;
	/** 录像记录:施放技能变更数据 **/
	public List<Integer> fightRecordSpecialOrderList;
	/** 录像:操作数据 **/
	public JSONObject fightRecordOperate;
	/** 是否是单人参与的战斗 **/
	private boolean isSingleFight = true;

	public CrossStageObject(CrossPort port, long stageId, int stageSn, int mapSn, int fightType,
			boolean isSingleFight) {
		super(port, stageId, stageSn, mapSn);
		this.port = port;
		this.isClient = true;
		this.name = "";
		this.fightType = fightType;
		this.isRecordFight = false;
		this.isSingleFight = isSingleFight;
	}

	public CrossStageObject(FightRecord fightRecord, CrossPort port, long stageId, int stageSn, int mapSn) {
		super(port, stageId, stageSn, mapSn);
		this.port = port;
		this.isClient = true;
		this.name = "";
		this.fightType = fightRecord.getFightType();
		this.needRecord = false;
		this.isReplay = true;
		this.fightRecord = fightRecord;
		this.isRecordFight = false;
		this.isSingleFight = true;

		initFightReplay();
	}
	
	/**
	 * 建立对应的service,stageObject的子类需要自己的service的时候，可以覆盖此方法
	 * 
	 * @param port
	 * @return
	 */
	public CrossStageObjectService createService(Port port) {
		// 设置服务接口
		return new CrossStageObjectService(this, port);
	}

	/**
	 * 增加特殊技能列表
	 * 
	 * @param pos
	 */
	@Override
	public void addSpecialList(int pos) {
		this.specialOrderList.add(pos);
		if (this.needRecord) {
			this.fightRecordSpecialOrderList.add(pos);
		}
	}

	/**
	 * 地图初始化
	 */
	public void startup() {
		// 创建服务此stageobject的service
		CrossPort port = getPort();
		CrossStageObjectService serv = createService(port);
		serv.startupLocal();

		// 设置服务接口
		port.addService(serv);

		// 将地图进行全局注册
		String nodeId = Config.getCrossPartDefaultNodeId();
		String portId = Config.getCrossPartDefaultPortId();
		CrossStageGlobalServiceProxy prx = CrossStageGlobalServiceProxy.newInstance(nodeId, portId,
				D.CROSS_SERV_STAGE_GLOBAL);
		prx.infoRegister(stageId, mapSn, name, Distr.getNodeId(port.getId()), port.getId());
	}

	/**
	 * 快速结算
	 * 
	 * @param combatantObj
	 */
	public ResultParam startupQuick() {
		this.isQuickFight = true;
		this.isClient = false;
		this.isRecordFight = true;
		int count = 1;
		while (!this.isEnd) {
			pulseStageStep();
			count = count + 1;
			if (count > 1000000) {
				break;
			}
		}

		ResultParam param = getQuickFightInfo();
		if (this.fightRecord != null) {
			param.recordId = this.fightRecord.getId();
		}
		return param;
	}

	/**
	 * 获取快速战斗的结果数据，子类扩展此类
	 * 
	 * @return
	 */
	public ResultParam getQuickFightInfo() {
		ResultParam param = new ResultParam();
		return param;
	}

	public CrossPort getPort() {
		return port;
	}

	/**
	 * 销毁地图
	 */
	public void destory() {
		CrossPort port = getPort();
		// 删除服务接口
		port.delServiceBySafe(stageId);

		// 将地图从全局信息中删除
		StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
		prx.infoCancel(stageId);
	}

	/**
	 * 一定时间后销毁地图
	 */
	public void waitDestory() {
	}

	@Override
	public void pulse() {
		if (ttWaitCastSkill != null) {
			if (ttWaitCastSkill.isPeriod(getTime())) {
				stopCastSkill();// 玩家手动释放技能，停止计时器，解除暂停状态
				// 玩家随机释放技能
				int pos = this.lastOrderList.get(this.lastOrderList.size()-1);
				FightObject fightObj = this.getObjByFightPos(pos);
				if (fightObj.isHuman()) {
					if (fightObj.canCastCommonSkill()) {
						fightObj.castCommonSkill();
					}
				}
			}
		}
		if (ttWaitCheck != null) {
			if (ttWaitCheck.isPeriod(getTime())) {
				ttWaitCheck = null;
			}
		}
		if (waitDestoryTimer != null) {
			if (waitDestoryTimer.isPeriod(getTime())) {
				waitDestoryTimer = null;
				this.destory();
				return;
			}
			super.finallyPulse();
			return;
		}
		// 间隔不到就返回
		super.pulse();
		super.finallyPulse();
	}

	/**
	 * 添加竞技者单元
	 * 
	 * @param obj
	 */
	public void addCombatantObj(CombatantObject obj) {
		CrossPort port = getPort();
		/* 设置地图与活动单元的关系 */
		obj.stageObj = this;
		combatantObjs.put(obj.id, obj);

		// 设置访问玩家服务接口
		CombatantObjectService combatantObjServ = new CombatantObjectService(obj, port);
		combatantObjServ.startup();
		port.addService(combatantObjServ);
		// 日志
		if (Log.fight.isDebugEnabled()) {
			Log.fight.debug("将活动单元注册至地图中：stage={},obj={}", this, obj);
		}
	}

	/**
	 * 删除竞技者单元
	 * 
	 * @param obj
	 */
	public void delCombatantObj(CombatantObject obj) {
		CrossPort port = getPort();
		/* 解除地图与活动单元的关系 */
		obj.stageObj = null;

		combatantObjs.remove(obj.id);

		// 删除访问玩家服务接口
		// 只有当接口中的CombatantObject与要删除的是同一内存对象时才进行删除
		// 否则由于清理的延迟操作，在切换地图等情况下会造成：
		// 标记为清理 -> 注册至地图 -> 进行真实清理（造成了service丢失的情况）
		CombatantObjectService serv = port.getService(obj.id);
		if (serv != null && serv.getCombatantObj() == obj) {
			port.delService(obj.id);
		} else {
			// Log.temp.info("删除CombatantObjectService错误:{},{}", obj.id, serv);
		}

		// 日志
		if (Log.fight.isDebugEnabled()) {
			Log.fight.debug("将活动单元从地图中删除：stage={},obj={}", this, obj);
		}
	}

	/**
	 * 获取竞技者列表
	 * 
	 * @return
	 */
	public Collection<CombatantObject> getCombatants() {
		return combatantObjs.values();
	}

	/**
	 * 获取竞技者映射表
	 * 
	 * @return
	 */
	public Map<Long, CombatantObject> getCombatantObjs() {
		return combatantObjs;
	}

	/**
	 * 获取竞技者对象
	 * 
	 * @param id
	 * @return
	 */
	public CombatantObject getCombatantObj(long id) {
		return combatantObjs.get(id);
	}

	/**
	 * 竞技者进入地图
	 * 
	 * @param info
	 */
	public void login(CombatantGlobalInfo info) {
		CombatantObject combatantObj = new CombatantObject();
		combatantObj.id = info.id;
		combatantObj.connPoint = info.connPoint;
		combatantObj.stageObj = this;
		combatantObj.humanMirrorObj = info.humanMirrorObj;
		combatantObj.team = info.team;
		CombatantManager.inst().login(combatantObj);
		if (this.step > 0) {
			this.sendStepInfoMsg();
		}
	}

	/**
	 * 有人离开(掉线处理)
	 * @param id
	 */
	public void leaveStage(long id) {
		CombatantObject combatantObjDisconnect = getCombatantObj(id);// 掉线者
		List<Long> disconnectList = new ArrayList<>();
		disconnectList.add(id);
		for (CombatantObject combatantObj : getCombatants()) {
			if (combatantObjDisconnect == combatantObj)
				continue;
			SCEnemyDisconnect.Builder msg = SCEnemyDisconnect.newBuilder();
			msg.addAllPlayerId(disconnectList);
			combatantObj.sendMsg(msg.build());
		}
		// 删除掉线者
		delCombatantObj(combatantObjDisconnect);
	}

	public int getStageSn() {
		return stageSn;
	}

	/**
	 * 修改时间倍率
	 * 
	 * @param speed
	 */
	public void onChangeFightSpeed(float speed) {
		if (speed < 1) {
			speed = 1;
		}
		if (speed > 5) {
			speed = 5;
		}
		this.speedTimes = speed;
		for (CombatantObject obj : combatantObjs.values()) {
			SCTurnbasedSpeed.Builder msg = SCTurnbasedSpeed.newBuilder();
			msg.setSpeed(speed);
			obj.sendMsg(msg.build());
		}
	}

	/**
	 * 修改是否自动战斗
	 * 
	 * @param combatantObj
	 * @param auto
	 */
	public void onChangeAutoFight(CombatantObject combatantObj, boolean auto) {
		if (this.isReplay) {
			return;
		}
		this.autoFightMap.put(combatantObj.team, auto);

		if (this.needRecord) {
			// 录像记录自动战斗
			this.fightRecordAutoFightMap.put(combatantObj.team, auto);
		}

		SCTurnbasedAutoFight.Builder msg = SCTurnbasedAutoFight.newBuilder();
		msg.setAuto(auto);
		combatantObj.sendMsg(msg.build());
	}

	/**
	 * 玩家开始战斗
	 * 
	 * @param combatantObj
	 */
	public void onCombatantStartFight(CombatantObject combatantObj) {
		combatantReadyMap.put(combatantObj.id, true);
	}

	/**
	 * 玩家跳过战斗
	 * 
	 * @param combatantObj
	 */
	public void onCombatantQuickFight(CombatantObject combatantObj) {
		if (!this.canQuickFight) {
			return;
		}
		if (isQuickFight) {
			return;
		}
		// 战斗未开始
		if (this.step < 1 || this.step == 1 && this.round < 1) {
			return;
		}
		// 是否录像
		if (this.isReplay) {
			finishFight();
			return;
		}

		this.autoFightMap.put(combatantObj.team, true);
		this.isQuickFight = true;
		int count = 1;
		while (!this.isEnd) {
			pulseStageStep();
			count = count + 1;
			if (count > 1000000) {
				break;
			}
		}
	}

	/**
	 * 等待回合结束确认
	 */
	public void onCombatantRoundEnd(CombatantObject combatantObj, int round) {
		if (this.round != round) {
			return;
		}
		combatantReadyMap.put(combatantObj.id, true);
	}
	
	/**
	 * 战斗暂停，玩家点击了暂停按钮
	 */
	public void onCombatantStopFight(CombatantObject combatantObj, boolean isStop) {
		if (!this.canStop) {
			return;
		}
		if (this.isStop == isStop) {
			return;
		}
		this.isStop = isStop;
		SCTurnbasedStopFight.Builder msg = SCTurnbasedStopFight.newBuilder();
		msg.setStop(isStop);
		combatantObj.sendMsg(msg.build());
	}
	
	/**
	 * 退出战斗
	 */
	public void onCombatantLeaveFight(CombatantObject combatantObj) {
		// 结束战斗
		finishFight();
		
		// 由客户端发就行了
//		if (!this.isReplay) {
//			String nodeId = Config.getCrossPartDefaultNodeId(0);
//			String portId = Config.getCrossPartDefaultPortId(0);
//			if (this.isSingleFight) {
//				SingleFightServiceProxy prx = SingleFightServiceProxy.newInstance(nodeId, portId,
//						D.CROSS_SERV_SINGLE_FIGHT);
//				prx.SF004_StageLeaveFinish(stageId, combatantIds, combatantObj.id);
//			} else {
//				GroupFightServiceProxy prx = GroupFightServiceProxy.newInstance(nodeId, portId,
//						D.CROSS_SERV_GROUP_FIGHT);
//				prx.GF006_StageLeaveFinish(stageId, combatantIds, combatantObj.id);
//			}
//		}
	}
	
	public void onMsgActionEnd(CombatantObject combatantObj, int select) {
		if(this.stageEvent instanceof EventPlot) {
			EventPlot event = (EventPlot)this.stageEvent;
			event.setFinish(this, select);
		}
	}
	
	public void onMsgMonterChangeEnd(CombatantObject combatantObj) {
		if(this.stageEvent instanceof EventFighterStageShow) {
			EventFighterStageShow event = (EventFighterStageShow)this.stageEvent;
			event.setFinish(this);
		}
	}
	
	/**
	 * 玩家施放技能
	 * 
	 * @param combatantObj
	 * @param casterId
	 * @param sn
	 */
	public void onCombatantCastSkill(CombatantObject combatantObj, int casterId, int sn) {
		if (this.isReplay) {
			return;
		}
		FightObject fightObj = this.getFightObjs().get(casterId);
		if (fightObj == null) {
			return;
		}
		// 战队判断：施法者是否属于玩家所在战队
		if (combatantObj.team != fightObj.team) {
			return;
		}
		ConfSkill confSkill = ConfSkill.get(sn);
		if (null == confSkill) {
			return;
		}
		// 怒气判断
		int rage = PropManager.inst().getCurRage(fightObj);
		if (rage < confSkill.fireClearRage) {
			Log.fight.error("怒气不足：{} 施放技能={},", fightObj.name, confSkill.sn);
			return;// 怒气不足
		}
		// 技能判断
		int fightPos = fightObj.getFightPos();

		if (confSkill.type == SkillType.Special.value()) {
			// 释放大招
			if (specialOrderList.contains(fightPos)) {
				return;// 已经在等待列表中
			}
			if (lastSpecialOrderList.contains(fightPos)) {
				return;// 本回合已经释放过了
			}

			specialOrderList.add(fightPos);
			this.sendSpecialOrderList(fightObj.idFight);
			if (this.needRecord) {
				// 录像记录技能操作
				fightRecordSpecialOrderList.add(fightPos);
			}
			
			stopCastSkill();// 玩家手动释放技能，停止计时器，解除暂停状态
		} else if (confSkill.type >= SkillType.Common.value()) {
			// 释放技能
			for (int i = 0; i < fightObj.commonSkillList.size(); i++) {
				if (fightObj.commonSkillList.get(i).sn == sn) {
					fightObj.indexOfCommonSkill = i;
					stopCastSkill();// 玩家手动释放技能，停止计时器，解除暂停状态
					if (fightObj.canCastCommonSkill()) {
						fightObj.castCommonSkill();
					}
					break;
				}
			}
		}
	}
	
	/**
	 * 玩家手动释放技能，停止计时器，解除暂停状态
	 */
	private void stopCastSkill() {
		if (ttWaitCastSkill != null) {
			ttWaitCastSkill.stop();
			ttWaitCastSkill = null;
		}
		this.isStop = false;// 解除暂停状态
	}

	/**
	 * 发送战斗每波信息
	 */
	private void sendStepInfoMsg() {
		if(this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		for (CombatantObject obj : combatantObjs.values()) {
			SCTurnbasedStageStep.Builder msg = SCTurnbasedStageStep.newBuilder();
			msg.setStep(step);
			msg.setStepAll(stepAll);
			
			DTurnbasedTeamObjs.Builder team1 = DTurnbasedTeamObjs.newBuilder();
			team1.setTeam(ETeamType.Team1);
			DTurnbasedTeamObjs.Builder team2 = DTurnbasedTeamObjs.newBuilder();
			team2.setTeam(ETeamType.Team2);
			for (FightObject fightObj : this.getFightObjs().values()) {
				if (fightObj.team == ETeamType.Team1) {
					team1.addFightObjList(fightObj.createMsg());
					team1.setStance(fightObj.stance);
				} else if (fightObj.team == ETeamType.Team2) {
					team2.addFightObjList(fightObj.createMsg());
					team2.setStance(fightObj.stance);
					// 怪物只会在2队，附带个怪物表InstMonster.sn
					if (fightObj.isMonster() && fightObj.stageObj.confInstMonster != null
							&& team2.getSnInstMonster() != fightObj.stageObj.confInstMonster.sn) {
						team2.setSnInstMonster(fightObj.stageObj.confInstMonster.sn);
					}
				}
			}
			msg.setTeam1(team1);
			msg.setTeam2(team2);
			msg.setSelfTeam(obj.team);
			msg.setPriorTeam(priorTeam);
			msg.setBossId(bossInfoId);
			msg.setCanQuickFight(canQuickFight);
			int multiple = 0;
			FightParamBase param = this.doStageOp(StageOpType.GetStepMultipe, null);
			if(param instanceof FightParamInt){
				multiple = ((FightParamInt)param).value;
			}
			msg.setMultiple(multiple);
			obj.sendMsg(msg.build());
		}
	}

	/**
	 * 发送回合数变更消息
	 */
	@Override
	public final void sendRoundChangeInfoMsg(List<DTurnbasedBuff> buffRemoveList) {
		if(this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		for (CombatantObject obj : combatantObjs.values()) {
			SCTurnbasedRoundChange.Builder msg = SCTurnbasedRoundChange.newBuilder();
			msg.setRound(round);
			msg.setMaxRound(roundMax);
			if (buffRemoveList != null && !buffRemoveList.isEmpty()) {
				msg.addAllBuff(buffRemoveList);
			}
			obj.sendMsg(msg.build());
		}
	}

	/**
	 * 发送技能施法消息
	 * 
	 * @param skillData
	 */
	@Override
	public final void sendCastSkillInfoMsg(SkillCastData skillData) {
		if(this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		for (CombatantObject obj : combatantObjs.values()) {
			SCTurnbasedCastSkill.Builder msg = SCTurnbasedCastSkill.newBuilder();
			msg.setCasterId(skillData.creator.idFight);
			msg.setSn(skillData.skill.sn);
			msg.addAllTargetList(skillData.createTargetDataMsg());//技能目标列表的战斗数据变化
			msg.setCaster(skillData.casterData.createTargetDataMsg());//施法者的战斗数据变化
			obj.sendMsg(msg.build());
		}
	}

	/**
	 * 发送buff生效消息
	 * 
	 * @param buffData
	 * @param isRemove
	 */
	@Override
	public final void sendBuffTriggerInfoMsg(BuffTriggerData buffData, boolean isRemove) {
		if(this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		if (buffData == null || buffData.damage == 0 && buffData.rage == 0) {
			return;
		}
		for (CombatantObject obj : combatantObjs.values()) {
			SCTurnbasedBuff.Builder msg = SCTurnbasedBuff.newBuilder();
			msg.setCasterId(buffData.buff.owner.idFight);
			msg.setBuff(buffData.buff.creageMsg());
			msg.setIsRemove(isRemove);
			msg.setDamage((int) buffData.damage);
			msg.setRage((int) buffData.rage);
			obj.sendMsg(msg.build());
		}
	}
	
	/**
	 * 获取结果消息
	 * 
	 * @return
	 */
	public abstract SCTurnbasedFinish getFinishMsg();

	/**
	 * 战斗结束
	 */
	@Override
	public final void finishFight() {
		if (isEnd) {
			return;
		}
		
		isEnd = true;
		if(!this.isRecordFight) {
			if (this.isReplay || this.fightType == ECrossFightType.FIGHT_NEWBIE_VALUE) {
				// 发送战斗结束给玩家，包括：录像回放结束或新手战斗结束
				this.sendFinishMsgToCambatants();
			} else {
				// 发送战斗结束给游戏服，再由游戏服转发给玩家
				sendFinishFightMsg(getFinishMsg());
			}
		}
		if (this.needRecord) {
			setFinishRecordInfo();
		}
		waitDestoryTimer = new TickTimer(WAIT_DESTORY_TIME);
	}
	/**
	 * 发送战斗结束给玩家，包括：录像回放结束或新手战斗结束
	 */
	private void sendFinishMsgToCambatants() {
		if (this.isReplay) {
			for (CombatantObject combatantObj : combatantObjs.values()) {
				combatantObj.sendMsg(this.getFinishMsgFromRecord());
			}
		} else {
			for (CombatantObject combatantObj : combatantObjs.values()) {
				combatantObj.sendMsg(this.getFinishMsg());
			}
		}
	}
	/**
	 * 正常战斗结束,发送结果给游戏服
	 */
	private void sendFinishFightMsg(SCTurnbasedFinish msg) {
		String nodeId = Config.getCrossPartDefaultNodeId(0);
		String portId = Config.getCrossPartDefaultPortId(0);
		if (this.isSingleFight) {
			SingleFightServiceProxy prx = SingleFightServiceProxy.newInstance(nodeId, portId,
					D.CROSS_SERV_SINGLE_FIGHT);
			prx.SF003_StageFightFinish(stageId, combatantIds, msg, winTeam.getNumber(), isAlwaysWin);
		} else {
			GroupFightServiceProxy prx = GroupFightServiceProxy.newInstance(nodeId, portId, 
					D.CROSS_SERV_GROUP_FIGHT);
			prx.GF005_StageFightFinish(stageId, combatantIds, msg, winTeam.getNumber(), isAlwaysWin);
		}
	}
	
	/**
	 * 战斗暂停，等待玩家选择技能
	 */
	@Override
	public final void stopFight(boolean isStop, int tmStop, int idFight) {
		if (!this.canStop) {
			return;
		}
		if (tmStop <= 0) {
			return;
		}
		if (this.isStop == isStop) {
			return;
		}
		this.isStop = isStop;
		
		long timeOut = this.getTime() + tmStop * Time.SEC;// 等待玩家选取技能
		for (CombatantObject combatantObj : combatantObjs.values()) {
			// 通知客户端轮到主角选择技能
			SCTurnbasedHumanSelSkill.Builder msgSel = SCTurnbasedHumanSelSkill.newBuilder();
			msgSel.setFighterId(idFight);
			msgSel.setTimeout(timeOut);
			combatantObj.sendMsg(msgSel.build());
		}
		
		ttWaitCastSkill = new TickTimer(tmStop * Time.SEC);
	}

	/**
	 * 初始化战斗对你
	 */
	private void initFightObj() {
		if (this.step == 1) {
			if (this.isReplay) {
				initFightObjFromRecord();
			} else {
				initFightObjOverride();
			}
			if (this.needRecord) {
				setRecordCampInfo();
			}
		} else {
			initFightObjNewStepOverride();
		}
	}

	/**
	 * 子类扩展这个函数
	 */
	public void initFightObjOverride() {
		humanMirrorObjToFightObj();
		sceneMonsterToFightObj();
	}

	/**
	 * 子类扩展这个函数
	 */
	public void initFightObjNewStepOverride() {
		sceneMonsterToFightObj();
	}
	
	/**
	 * 计算战力压制
	 */
	protected void calc_combatSuppress() {
		// 需要压制的玩法类型
		List<Integer> suppressType = Utils.strToIntList(ConfParam.get("combatCapabilityRepressio").value);
		if (!suppressType.contains(fightType)) {
			return;
		}
		// 我方总战力
		int team1Combat = getTeam1Combat();
		// 对方总战力
		int team2Combat = getTeam2Combat();

		// 压制系数(万分比)
		int ratio = 0;
		// 战力差
		int distance = team1Combat - team2Combat;
		if (distance < 0) {
			ratio = Math.abs(distance * Utils.I10000 / team2Combat); // 队伍2战力高
		} else {
			ratio = Math.abs(distance * Utils.I10000 / team1Combat); // 队伍1战力高
		}
		int curSn = -1;
		for (ConfPropAdd conf : ConfPropAdd.findAll()) {
			if(curSn == -1 && conf.sn >= ratio){
				curSn = conf.sn;
				continue;
			}
			
			if(curSn > conf.sn && conf.sn >= ratio) {
				curSn = conf.sn;
			}
		}
		
		if (distance < 0) {
			team2_ConfPropAdd = ConfPropAdd.get(curSn);
		} else {
			team1_ConfPropAdd = ConfPropAdd.get(curSn);
		}
		
	}
	/**
	 * 子类扩展获取我方战力
	 */
	protected int getTeam1Combat() {
		return 0;
	}
	/**
	 * 子类扩展获取对方战力
	 */
	protected int getTeam2Combat() {
		return 0;
	}

	/**
	 * 玩家镜像转为战斗对象
	 */
	public void humanMirrorObjToFightObj() {
		for (CombatantObject combatant : getCombatants()) {
			humanMirrorObjToFightObj(combatant.humanMirrorObj, combatant.team);
		}
	}
	
	/**
	 * 玩家镜像转为战斗对象
	 * @param intParam
	 */
	private void humanMirrorObjToFightObj(FightParamInt intParam) {
		ETeamType team = ETeamType.valueOf(intParam.value);
		for (CombatantObject combatant : getCombatants()) {
			if(combatant.team == team) {
				if(combatant.humanMirrorObj == null) {
					return;
				}
				HumanMirror humanMir = combatant.humanMirrorObj.getHumanMirror();
				EStanceType stance = EStanceType.valueOf(humanMir.getPartnerStance());
				if (null == stance) {
					Log.table.error("error in stance={},humanId={}", humanMir.getPartnerStance(), humanMir.getId());
					return;
				}
				int humanPos = combatant.humanMirrorObj.getHumanPos();
				FightHumanObject humanObj = new FightHumanObject(this);
				// 初始化主角战斗对象
				initHumanObj(humanObj, humanMir, humanPos, team, stance);
			}
		}
	}

	/**
	 * 设置战斗者属性
	 * 
	 * @param obj
	 * @param jsonStr
	 */
	public void setFightObjProp(FightObject obj, String jsonStr) {
		JSONObject arrAllPropJSON = Utils.toJSONObject(jsonStr);
		if (!arrAllPropJSON.isEmpty()) {
			for (String key : arrAllPropJSON.keySet()) {
				int value = arrAllPropJSON.getIntValue(key);
				ValueBase propValue = obj.prop.getOnePropValue(key);
				if (null == propValue) {
					Log.logError(Log.fight, "属性错误，错误的初始属性：stage={},isHuman={},sn={},propName={}", 
							this, obj.isHuman(), obj.sn, key);
					continue;
				}
				ValueBase newValue = propValue.getCopy();
				newValue.setValue(ValueFactory.getFightValueByParam(value));
				obj.prop.addPropValue(key, newValue);
			}
		}
		
		// 获取属性压制配置
		ConfPropAdd confPropAdd = obj.team == ETeamType.Team1 ? team1_ConfPropAdd : team2_ConfPropAdd;
		if (confPropAdd == null) {
			return;
		}
		String propName = ""; // 属性名
		ValueBase attrValue = null; // 属性值
		ValueDouble addValue = null; // 加成值
		for (int i = 0; i < confPropAdd.attr.length; i++) {
			propName = confPropAdd.attr[i];
			attrValue = obj.prop.getOnePropValue(propName);
			// 加成比例
			addValue = new ValueDouble(confPropAdd.attrAdd[i] / Utils.D10000);
			// 加成值 = 比例 x 属性值
			addValue.multiply(attrValue); 
			obj.prop.addPropValue(propName, addValue);
		}
	}

	public void setFightObjProp(FightObject obj, Unit unit) {
		String jsonStr = UnitManager.inst().getPropJSON(unit);
		setFightObjProp(obj, jsonStr);
	}

	/**
	 * 设置主角或伙伴技能
	 * 
	 * @param obj
	 * @param snList
	 */
	public void setFightObjSkill(FightObject obj, String installSkillJSON, String installGodsJSON) {
		Map<Integer, Integer> mapSkills = Utils.jsonToMapIntInt(installSkillJSON);
		for (Entry<Integer, Integer> e : mapSkills.entrySet()) {
			Integer sn = e.getKey();
			Integer lv = e.getValue();
			Skill normalSkill = Skill.newInstance(sn, lv, obj);
			if (normalSkill != null) {
				obj.commonSkillList.add(normalSkill);
			}
		}
		Map<Integer, Integer> mapGods = Utils.jsonToMapIntInt(installGodsJSON);
		for (Entry<Integer, Integer> e : mapGods.entrySet()) {
			Integer sn = e.getKey();
			Integer lv = e.getValue();
			obj.specialSkill = Skill.newInstance(sn, lv, obj);
		}
	}

	/**
	 * 设置怪物配表技能
	 * 
	 * @param obj
	 * @param snAll
	 */
	public void setMonsterObjSkill(FightObject obj, int[] snAll) {
		if (null == snAll) {
			return;
		}
		int lv = 1;// 配表技能默认1级
		for (int sn : snAll) {
			ConfSkill confSkill = ConfSkill.get(sn);
			if (null == confSkill) {
				Log.table.error("ConfSkill 配表错误，no find sn={}", sn);
				continue;
			}
			if (confSkill.active) {
				// 主动技能，包括：多个普通技能，单个特殊技能
				if (confSkill.type == SkillType.Special.value()) {
					obj.specialSkill = Skill.newInstance(sn, lv, obj);
				} else if (confSkill.type >= SkillType.Common.value()) {
					Skill normalSkill = Skill.newInstance(sn, lv, obj);
					if (normalSkill != null) {
						obj.commonSkillList.add(normalSkill);
					}
				}
				continue;
			} else {
				// 被动技能
				Skill passiveSkill = Skill.newInstance(sn, lv, obj);
				if (passiveSkill != null) {
					obj.passiveSkillList.add(passiveSkill);
				}
			}
		}
	}

	/**
	 * 角色镜像转为战斗者
	 * 
	 * @param humanMirrorObj
	 * @param team
	 */
	public void humanMirrorObjToFightObj(HumanMirrorObject humanMirrorObj, ETeamType team) {
		humanMirrorObjToFightObj(humanMirrorObj, team, null);
	}

	public void humanMirrorObjToFightObj(HumanMirrorObject humanMirrorObj, ETeamType team, List<Integer> excludeList) {
		if (null == humanMirrorObj) {
			Log.fight.error("humanMirrorObjToFightObj error in humanMirrorObj is null");
			return;
		}
		
		HumanMirror humanMir = humanMirrorObj.getHumanMirror();
		EStanceType stance = EStanceType.valueOf(humanMir.getPartnerStance());
		if (null == stance) {
			Log.table.error("error in stance={},humanId={}", humanMir.getPartnerStance(), humanMir.getId());
			return;
		}
		int humanPos = humanMirrorObj.getHumanPos();
		// 创建战斗玩家数据
		if (excludeList == null || !excludeList.contains(humanPos)) {
			int snHuman = humanMir.getSn();// 配表sn即PartnerProperty.sn
			ConfPartnerProperty conf = ConfPartnerProperty.get(snHuman);
			if (null == conf) {
				Log.logTableError(ConfPartnerProperty.class,"玩家配置错误，缺少sn={},stage={}", snHuman, this);
				return;
			}
			FightHumanObject humanObj = new FightHumanObject(this);
			// 初始化主角战斗对象
			initHumanObj(humanObj, humanMir, humanPos, team, stance);
		}

		// 创建战斗伙伴数据
		long[] idPartners = Utils.strToLongArray(humanMir.getPartnerLineup());
		Map<Long, PartnerMirror> partnerMap = humanMirrorObj.getPartnerMirror();
		for (int pos = 0; pos < idPartners.length; pos++) {
			if (excludeList != null && excludeList.contains(pos)) {
				continue;
			}
			long partnerId = idPartners[pos];
			if (partnerId <= 0) {// 过滤：0空位置，-1主角
				continue;
			}
			PartnerMirror partnerMir = partnerMap.get(partnerId);
			if (null == partnerMir) {
				Log.cross.error("玩家id={}的阵容里查无伙伴partnerId={}", humanMir.getId(), partnerId);
				continue;
			}
			int snPartner = humanMir.getSn();// 配表sn即PartnerProperty.sn
			ConfPartnerProperty conf = ConfPartnerProperty.get(snPartner);
			if (null == conf) {
				Log.logTableError(ConfPartnerProperty.class,"玩家配置错误，缺少sn={},stage={}", snPartner, this);
				continue;
			}
			FightGeneralObject genObj = new FightGeneralObject(this);
			// 初始化伙伴战斗对象
			initPartnerObj(genObj, partnerMir, pos, team, stance);
		}
	}

	/**
	 * 初始化主角战斗对象
	 * 
	 * @param humanObj
	 * @param hMir
	 * @param pos
	 * @param team
	 */
	private void initHumanObj(FightHumanObject humanObj, HumanMirror hMir, int pos, ETeamType team,
			EStanceType stance) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(hMir.getSn());
		if (conf == null) {
			Log.logTableError(ConfPartnerProperty.class,"主角配置错误，缺少sn={},stage={}", hMir.getSn(), this);
			return;
		}
		
		humanObj.idFight = getFightId();// 战斗中分配的临时ID
		humanObj.sn = hMir.getSn();// 配表sn即PartnerProperty.sn
		humanObj.modelSn = hMir.getModelSn();// 模型sn
		humanObj.sex = hMir.getSex();// 性别：1男，2女
		humanObj.pos = pos;// 队中位置
		humanObj.team = team;// 所属战队
		humanObj.stance = stance;// 队伍站位
		
		// 属性
		setFightObjProp(humanObj, hMir);
		// 当前血量同步
		PropManager.inst().restoreMaxHp(humanObj);
		// 技能
		setFightObjSkill(humanObj, hMir.getInstallSkillJSON(), hMir.getInstallGodsJSON());
		// 入场
		humanObj.stageRegister(this);
		// 激活被动
		humanObj.startup();
	}

	/**
	 * 初始化伙伴战斗对象
	 * 
	 * @param partnerObj
	 * @param pMir
	 * @param pos
	 * @param team
	 */
	private void initPartnerObj(FightGeneralObject partnerObj, PartnerMirror pMir, int pos, ETeamType team,
			EStanceType stance) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(pMir.getSn());
		if (conf == null) {
			Log.logTableError(ConfPartnerProperty.class,"伙伴配置错误，缺少sn={},stage={}", pMir.getSn(), this);
			return;
		}
		
		partnerObj.idFight = getFightId();// 战斗中分配的临时ID
		partnerObj.sn = pMir.getSn();// 配表sn即PartnerProperty.sn
		partnerObj.modelSn = pMir.getModelSn();// 模型sn
		partnerObj.sex = pMir.getSex();// 性别：1男，2女
		partnerObj.pos = pos;// 队中位置
		partnerObj.team = team;// 所属战队
		partnerObj.stance = stance;// 队伍站位

		// 属性
		setFightObjProp(partnerObj, pMir);
		// 当前血量同步
		PropManager.inst().restoreMaxHp(partnerObj);
		// 技能
		setFightObjSkill(partnerObj, pMir.getInstallSkillJSON(), null);
		// 入场
		partnerObj.stageRegister(this);
		// 激活被动
		partnerObj.startup();
	}

	/**
	 * 初始化怪物对象
	 * 
	 * @param monsterObj
	 * @param conf
	 *            配表ConfPartnerProperty
	 * @param pos
	 *            队中位置
	 * @param stance
	 *            队伍站位
	 */
	private void initMonsterObj(FightMonsterObject monsterObj, ConfPartnerProperty conf, int pos, EStanceType stance) {
		monsterObj.idFight = getFightId();// 战斗中分配的临时ID
		monsterObj.sn = conf.sn;// 配表sn即PartnerProperty.sn
		monsterObj.modelSn = conf.modId;// 模型sn
		monsterObj.sex = conf.sex;// 性别：1男，2女
		monsterObj.camp = conf.camp;// 所属阵营
		monsterObj.pos = pos;// 队中位置
		monsterObj.team = ETeamType.Team2;// 所属战队
		monsterObj.stance = stance;// 队伍站位

		// boss详情
		if (conf.roleType == EWorldObjectType.MonsterBoss_VALUE) {
			monsterObj.isShowBossInfo = true;
		}
		// 属性
		PropCalc propCalc = new PropCalc(Utils.toJSONString(conf.propName, conf.propValue));
		setFightObjProp(monsterObj, propCalc.toJSONStr());
		// 当前血量同步
		PropManager.inst().restoreMaxHp(monsterObj);
		// 技能
		setMonsterObjSkill(monsterObj, conf.skill);
		// 入场
		monsterObj.stageRegister(this);
		// 激活被动
		monsterObj.startup();
	}
	
	/**
	 * 创建怪物
	 * @param infoParam
	 */
	private void createMonsterObj(FightParamMonsterInfo infoParam) {		
		// 获取怪物配表
		ConfPartnerProperty conf = ConfPartnerProperty.get(infoParam.sn);
		if (conf == null) {
			Log.logTableError(ConfPartnerProperty.class,"怪物配置错误，缺少sn={},stage={}", infoParam.sn, this);
			return;
		}
		
		FightObject fightObj = this.getObjByFightPos(infoParam.pos);
		if(fightObj != null) {//移除旧战斗者
			fightObj.stageLeave();
		}
		
		FightMonsterObject monsterObj = new FightMonsterObject(this);
		// 初始化怪物对象
		initMonsterObj(monsterObj, conf, infoParam.pos, infoParam.stance);
		return;
	}

	public void sceneMonsterToFightObj() {
		sceneMonsterToFightObj(null, 0);
	}

	public void sceneMonsterToFightObj(List<Integer> excludeList, int setLv) {
		if (null == confInstMonster) {
			Log.fight.error("confInstMonster配表错误，confInstMonster is null");
			return;
		}
		if (null == confInstMonster.monsterIds) {
			Log.table.error("战斗场景怪物配置数据(ID)为空,stageSn={}", this.stageSn);
			return;
		}

		for (int pos = 0; pos < this.confInstMonster.monsterIds.length; pos++) {
			// 检查是否需要排除
			if (excludeList != null && excludeList.contains(pos)) {
				continue;
			}
			EStanceType stance = EStanceType.valueOf(confInstMonster.lineup);
			if (null == stance) {
				Log.table.error("confInstMonster error in stance={},sn={}", confInstMonster.lineup, confInstMonster.sn);
				continue;
			}
			// 获取怪物配表
			int snMonster = this.confInstMonster.monsterIds[pos];
			if (snMonster == 0) {// 过滤：sn=0即空位置
				continue;
			}
			ConfPartnerProperty conf = ConfPartnerProperty.get(snMonster);
			if (null == conf) {
				Log.logTableError(ConfPartnerProperty.class,"怪物配置错误，缺少sn={},stage={}", snMonster, this);
				continue;
			}

			FightMonsterObject monsterObj = new FightMonsterObject(this);
			// 初始化怪物对象
			initMonsterObj(monsterObj, conf, pos, stance);
		}

	}

	/**
	 * 判断登录的玩家是否达到要求
	 * 
	 * @return
	 */
	@Override
	public final boolean checkHumanLoginOk() {
		if (this.isRecordFight) {
			return true;
		}
		if (this.isReplay) {
			if (combatantObjs.size() < 1) {
				return false;
			}
			for (CombatantObject combatantObj : combatantObjs.values()) {
				if (!combatantObj.isClientStageReady) {
					return false;
				}
			}
			return true;
		}
		return checkHumanLoginOkOverride();
	}

	/**
	 * 判断登录的玩家是否达到要求,子类扩展此函数
	 * 
	 * @return
	 */
	public boolean checkHumanLoginOkOverride() {
		if (combatantObjs.size() < 1) {
			return false;
		}
		for (CombatantObject combatantObj : combatantObjs.values()) {
			if (!combatantObj.isClientStageReady) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断玩家是否准备就绪
	 * 
	 * @return
	 */
	@Override
	public final boolean checkHumanReadyOk() {
		if (this.isRecordFight) {
			return true;
		}
		if (this.isReplay) {
			if (combatantReadyMap.size() > 0) {
				return true;
			}
			return false;
		}
		return checkHumanReadyOkOverride();
	}

	/**
	 * 判断玩家是否准备就绪,子类扩展此函数
	 * 
	 * @return
	 */
	public boolean checkHumanReadyOkOverride() {
		if (combatantReadyMap.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 等待玩家确认回合结束
	 */
	@Override
	public final void sendWaitHumanCheckRoundEnd() {
		if (this.isQuickFight) {
			return;
		}
		combatantReadyMap.clear();
		ttWaitCheck = new TickTimer(WAIT_CHECK_TIME);
		for (CombatantObject combatantObj : combatantObjs.values()) {
			SCTurnbasedRoundEnd.Builder msg = SCTurnbasedRoundEnd.newBuilder();
			msg.setRound(this.round);
			combatantObj.sendMsg(msg.build());
		}
	}

	/**
	 * 发送特殊技能等待队列
	 */
	@Override
	public void sendSpecialOrderList(List<Integer> idFightList) {
		if (this.isQuickFight) {
			return;
		}
		for (CombatantObject combatantObj : combatantObjs.values()) {
			SCTurnbasedRageSkillWaitList.Builder msg = SCTurnbasedRageSkillWaitList.newBuilder();
			msg.addAllFighterId(idFightList);
			combatantObj.sendMsg(msg.build());
		}
	}

	/**
	 * 发送特殊技能等待队列
	 */
	@Override
	public void sendSpecialOrderList(int idFight) {
		if (this.isQuickFight) {
			return;
		}
		for (CombatantObject combatantObj : combatantObjs.values()) {
			SCTurnbasedRageSkillWaitList.Builder msg = SCTurnbasedRageSkillWaitList.newBuilder();
			msg.addFighterId(idFight);
			combatantObj.sendMsg(msg.build());
		}
	}

	/**
	 * 判断玩家是否回合播放结束就绪
	 * 
	 * @return
	 */
	@Override
	public final boolean checkHumanRoundEndOk() {
		if (this.isQuickFight) {
			return true;
		}
		if (ttWaitCheck == null) {
			return true;
		}
		for (CombatantObject combatantObj : combatantObjs.values()) {
			if (combatantReadyMap.get(combatantObj.id) == null) {
				return false;
			}
		}
		return true;
	}

	/**********************************************************************************************
	 * 录像
	 ***********************************************************************************************/
	/**
	 * 录像:初始化录像记录数据
	 */
	public void initFightRecord() {
		if (this.isReplay) {
			return;
		}
		this.needRecord = true;
		this.fightRecord = new FightRecord();
		this.fightRecord.setId(this.stageId);
		this.fightRecord.setRandSeed(this.randSeed);
		this.fightRecord.setFightType(this.fightType);
		this.fightRecord.setStageSn(this.stageSn);
		this.fightRecord.setMapSn(this.mapSn);

		this.fightRecordAutoFightMap = new HashMap<>();
		this.fightRecordSpecialOrderList = new ArrayList<>();
		this.fightRecordOperate = new JSONObject();
		initFightRecordFightName();
	}

	/**
	 * 录像：战斗者名
	 */
	public void initFightRecordFightName() {
		String leftName = "attacker";
		int leftCombat = 0;
		int leftSn = 1;
		int leftAptitude = 1;
		String rightName = "defender";
		int rightCombat = 0;
		int rightSn = 1;
		int rightAptitude = 1;
		for (CombatantObject obj : this.combatantObjs.values()) {
			HumanMirrorObject humanMirrorObj = obj.getHumanMirrorObj();
			if (humanMirrorObj == null) {
				continue;
			}
			HumanMirror humanMirror = humanMirrorObj.getHumanMirror();
			if (obj.team == ETeamType.Team1) {
				leftName = humanMirror.getName();
				leftCombat = humanMirror.getCombat();
				leftSn = humanMirror.getSn();
				leftAptitude = humanMirror.getAptitude();
			} else if (obj.team == ETeamType.Team2) {
				rightName = humanMirror.getName();
				rightCombat = humanMirror.getCombat();
				rightSn = humanMirror.getSn();
				rightAptitude = humanMirror.getAptitude();
			}
		}
		this.fightRecord.setLeftName(leftName);
		this.fightRecord.setLeftCombat(leftCombat);
		this.fightRecord.setLeftSn(leftSn);
		this.fightRecord.setLeftAptitude(leftAptitude);
		this.fightRecord.setRightName(rightName);
		this.fightRecord.setRightCombat(rightCombat);
		this.fightRecord.setRightSn(rightSn);
		this.fightRecord.setRightAptitude(rightAptitude);

	}

	/**
	 * 回放:初始化录像回放数据
	 */
	public void initFightReplay() {
		// 从录像中获取随机种子
		this.randSeed = this.fightRecord.getRandSeed();
		this.randUtils = new RandomMT19937(this.randSeed);

		this.fightRecordOperate = Utils.toJSONObject(this.fightRecord.getOperateInfo());
	}

	/**
	 * 录像:保存结果数据
	 */
	public void setFinishRecordInfo() {
		JSONObject json = getFinishRecordJson();
		json.put(JsonKey.Win, winTeam.getNumber());
		int index = 0;
		for (FightObject obj : this.getFightObjs().values()) {
			JSONObject js = new JSONObject();
			js.put(JsonKey.Pos, obj.pos);
			js.put(JsonKey.Team, obj.team.getNumber());
			js.put(JsonKey.HpCur, PropManager.inst().getCurHp(obj));
			js.put(JsonKey.RageCur, PropManager.inst().getCurRage(obj));
			json.put(String.valueOf(index), js);
			index++;
		}
		json.put(JsonKey.FightNum, index);
		this.fightRecord.setFinishInfo(json.toJSONString());
		this.fightRecord.setOperateInfo(this.fightRecordOperate.toJSONString());
		this.fightRecord.setExtraInfo(this.getFightRecordExtInfo().toJSONString());
		this.fightRecord.persist();
	}

	/**
	 * 录像:获取录像附加信息
	 * 
	 * @return
	 */
	public JSONObject getFightRecordExtInfo() {
		return new JSONObject();
	}

	/**
	 * 录像:获取结果数据
	 * 
	 * @return
	 */
	public JSONObject getFinishRecordJson() {
		return new JSONObject();
	}

	/**
	 * 回放:获取结果消息
	 * 
	 * @return
	 */
	public SCTurnbasedFinish getFinishMsgFromRecord() {
		JSONObject json = Utils.toJSONObject(this.fightRecord.getFinishInfo());
		int win = ETeamType.Team1_VALUE;
		if (json.containsKey(JsonKey.Win)) {
			win = json.getIntValue(JsonKey.Win);
		}
		SCTurnbasedFinish.Builder msg = getFinishRecordFinishMsg(json);
		msg.setWinTeam(ETeamType.valueOf(win));
		msg.setFightType(ECrossFightType.valueOf(fightType));
		int fighterNum = 0;
		if (json.containsKey(JsonKey.FightNum)) {
			fighterNum = json.getIntValue(JsonKey.FightNum);
		}
		for (int i = 0; i < fighterNum; i++) {
			JSONObject js = json.getJSONObject(String.valueOf(i));
			if (js == null) {
				break;
			}
			ETeamType team = ETeamType.Team1;
			int pos = 0;
			int hp = 0;
			int rage = 0;
			if (js.containsKey(JsonKey.Team)) {
				team = ETeamType.valueOf(js.getIntValue(JsonKey.Team));
			}
			if (js.containsKey(JsonKey.Pos)) {
				pos = js.getIntValue(JsonKey.Pos);
			}
			if (js.containsKey(JsonKey.HpCur)) {
				hp = js.getIntValue(JsonKey.HpCur);
			}
			if (js.containsKey(JsonKey.RageCur)) {
				rage = js.getIntValue(JsonKey.RageCur);
			}

			FightObject obj = this.getObjByPos(pos, team);
			if (obj == null) {
				continue;
			}
			DTurnbasedFinishObject.Builder objMsg = DTurnbasedFinishObject.newBuilder();
			objMsg.setId(obj.idFight);
			objMsg.setHpCur(hp);
			objMsg.setRageCur(rage);
			msg.addObjList(objMsg.build());
		}
		return msg.build();
	}

	/**
	 * 回放:设置录像结果战斗数据
	 * 
	 * @param json
	 * @return
	 */
	public SCTurnbasedFinish.Builder getFinishRecordFinishMsg(JSONObject json) {
		SCTurnbasedFinish.Builder msg = SCTurnbasedFinish.newBuilder();
		msg.addStar(1);
		msg.addStar(1);
		msg.addStar(1);
		return msg;
	}

	/**
	 * 回放:初始化战斗者信息，子类扩展这个函数
	 */
	public void initFightObjFromRecord() {
		JSONObject json = Utils.toJSONObject(this.fightRecord.getLeftInfo());
		HumanMirrorObject humanMirrorObj = new HumanMirrorObject(json);
		humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);
		sceneMonsterToFightObj();
	}

	/**
	 * 录像，保存战斗者信息
	 */
	public void setRecordCampInfo() {
		for (CombatantObject combatant : getCombatants()) {
			JSONObject json = combatant.humanMirrorObj.toFightJson();
			this.fightRecord.setLeftInfo(json.toJSONString());
			break;
		}
	}

	/**
	 * 录像/回放:自动战斗处理
	 */
	@Override
	public void checkAutoFightReplay() {
		this.fightRecordAutoTimes = this.fightRecordAutoTimes + 1;
		// 录像操作回放
		if (this.isReplay) {
			String key = JsonKey.AutoNum + this.fightRecordAutoTimes;
			JSONArray autoJson = this.fightRecordOperate.getJSONArray(key);
			if (autoJson == null) {
				return;
			}
			for (int i = 0; i < autoJson.size(); i++) {
				JSONObject js = autoJson.getJSONObject(i);
				int team = js.getIntValue(JsonKey.Team);
				boolean auto = js.getBooleanValue(JsonKey.AutoNum);
				this.autoFightMap.put(ETeamType.valueOf(team), auto);
			}
			return;
		}
		// 录像记录
		if (this.needRecord) {
			if (this.fightRecordAutoFightMap.isEmpty()) {
				return;
			}
			String key = JsonKey.AutoNum + this.fightRecordAutoTimes;
			JSONArray autoJson = new JSONArray();
			for (Map.Entry<ETeamType, Boolean> entry : this.fightRecordAutoFightMap.entrySet()) {
				JSONObject js = new JSONObject();
				js.put(JsonKey.Team, entry.getKey().getNumber());
				js.put(JsonKey.AutoNum, entry.getValue());
				autoJson.add(js);
			}
			this.fightRecordAutoFightMap.clear();
			this.fightRecordOperate.put(key, autoJson);
		}
	}

	/**
	 * 录像/回放:施放技能处理
	 */
	@Override
	public void checkCastSkillReplay() {
		this.fightRecordCastTimes = this.fightRecordCastTimes + 1;
		if (this.isReplay) {
			String key = JsonKey.SkillNum + this.fightRecordCastTimes;
			JSONArray castJson = this.fightRecordOperate.getJSONArray(key);
			if (castJson == null) {
				return;
			}
			List<Integer> list = new ArrayList<>();
			for (int i = 0; i < castJson.size(); i++) {
				JSONObject js = castJson.getJSONObject(i);
				int pos = js.getIntValue(JsonKey.RagePos);
				this.specialOrderList.add(pos);
				FightObject obj = this.getObjByFightPos(pos);
				if (obj != null) {
					list.add(obj.idFight);
				}
			}
			if (!list.isEmpty()) {
				this.sendSpecialOrderList(list);
			}
			return;
		}
		// 录像记录
		if (this.needRecord) {
			if (this.fightRecordSpecialOrderList.isEmpty()) {
				return;
			}
			String key = JsonKey.SkillNum + this.fightRecordCastTimes;
			JSONArray castJson = new JSONArray();
			for (int i = 0; i < this.fightRecordSpecialOrderList.size(); i++) {
				JSONObject js = new JSONObject();
				js.put(JsonKey.RagePos, this.fightRecordSpecialOrderList.get(i));
				castJson.add(js);
			}
			this.fightRecordSpecialOrderList.clear();
			this.fightRecordOperate.put(key, castJson);
		}
	}
	
	/**
	 * 发送剧情消息
	 * @param plotParam
	 */
	private void sendPlotMsg(FightParamPlot plotParam) {
		if(this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		for (CombatantObject obj : combatantObjs.values()) {
			SCTurnbasedActionStart.Builder msg = SCTurnbasedActionStart.newBuilder();
			msg.setActionId(plotParam.plot);
			obj.sendMsg(msg.build());
		}
	}


	/**
	 * 发送战斗者入场消息
	 * @param listParam
	 */
	private void sendFighterEnterMsg(FightParamFighterList listParam) {
		if(this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		for (CombatantObject obj : combatantObjs.values()) {
			SCTurnbasedObjectEnter.Builder msg = SCTurnbasedObjectEnter.newBuilder();
			for(FightObject fighter:listParam.fighterList){
				msg.addFightObjList(fighter.createMsg());
			}
			obj.sendMsg(msg.build());
		}		
	}

	/**
	 * 发送战斗者离场消息
	 * @param listParam
	 */
	private void sendFighterLeaveMsg(FightParamFighterList listParam) {
		if(this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		for (CombatantObject obj : combatantObjs.values()) {
			SCTurnbasedObjectLeave.Builder msg = SCTurnbasedObjectLeave.newBuilder();
			for(FightObject fighter:listParam.fighterList){
				msg.addFightObjList(fighter.createMsg());
			}
			obj.sendMsg(msg.build());
		}		
	}
	/**
	 * 执行操作
	 * @param type
	 * @param param
	 */
	@Override
	public FightParamBase doStageOp(StageOpType type, FightParamBase param) {
		switch(type){
		case CreateMonter:
		{
			if(param instanceof FightParamMonsterInfo){
				FightParamMonsterInfo infoParam = (FightParamMonsterInfo)param;
				createMonsterObj(infoParam);
				return null;
			}
		}
		break;
		case SendPlotMsg:
		{
			if(param instanceof FightParamPlot){
				FightParamPlot plotParam = (FightParamPlot)param;
				sendPlotMsg(plotParam);
				return null;
			}
		}
		break;
		case SendFighterEnter:
		{
			if(param instanceof FightParamFighterList){
				FightParamFighterList listParam = (FightParamFighterList)param;				
				sendFighterEnterMsg(listParam);
				return null;
			}
		}
		break;
		case SendFighterLeave:
		{
			if(param instanceof FightParamFighterList){
				FightParamFighterList listParam = (FightParamFighterList)param;
				sendFighterLeaveMsg(listParam);
				return null;
			}
		}
		break;
		case InitFightObj:
		{
			initFightObj();
		}
		return null;
		case SendStepInfoMsg:
		{
			sendStepInfoMsg();
		}
		return null;
		case CreateHumanObj:
		{
			if(param instanceof FightParamInt){
				FightParamInt intParam = (FightParamInt)param;
				humanMirrorObjToFightObj(intParam);
				return null;
			}
		}
		return null;
		case InitPropMonsterConf:
		{
			if(param instanceof FightParamInitPropMonsterConf){
				FightParamInitPropMonsterConf initParam = (FightParamInitPropMonsterConf)param;
				return initPropFromMonsterConf(initParam);
			}
		}
		return null;
		}
		return super.doStageOp(type, param);
	}
	
	/**
	 * 初始化怪物属性
	 * @param param
	 * @return
	 */
	private FightParamBase initPropFromMonsterConf(FightParamInitPropMonsterConf param) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(param.confMonster.sn);
		if (conf == null) {
			Log.logTableError(ConfPartnerProperty.class,"怪物配置错误，缺少sn={},stage={}", param.confMonster.sn, this);
			return new FightParamBoolean(false);
		}
		// 属性
		PropCalc propCalc = new PropCalc(Utils.toJSONString(conf.propName, conf.propValue));
		setFightObjProp(param.fightObj, propCalc.toJSONStr());
		return new FightParamBoolean(true);
	}

	/**
	 * 判定操作
	 * @param type
	 * @param param
	 */
	@Override
	public boolean checkStageOp(StageOpType type, FightParamBase param){
		switch(type){
		}
		return super.checkStageOp(type, param);
	}
}
