package game.turnbasedsrv.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.RandomMT19937;
import core.support.TickTimer;
import core.support.Time;
import game.msg.Define.DTurnbasedBuff;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.DTurnbasedTeamObjs;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EStanceType;
import game.msg.Define.ETeamType;
import game.msg.Define.EWorldObjectType;
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
import game.msg.MsgTurnbasedFight.SCTurnbasedRoundOrderEnd;
import game.msg.MsgTurnbasedFight.SCTurnbasedSpeed;
import game.msg.MsgTurnbasedFight.SCTurnbasedStageStep;
import game.msg.MsgTurnbasedFight.SCTurnbasedStopFight;
import game.turnbasedsrv.buff.BuffTriggerData;
import game.turnbasedsrv.combatEvent.CombatEventBase;
import game.turnbasedsrv.combatEvent.EventFighterStageShow;
import game.turnbasedsrv.combatEvent.EventPlot;
import game.turnbasedsrv.combatEvent.EventWaitHumanOp;
import game.turnbasedsrv.combatRule.CombatRuleBase;
import game.turnbasedsrv.combatRule.CombatRuleFactory;
import game.turnbasedsrv.enumType.CombatOpType;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.enumType.SkillType;
import game.turnbasedsrv.enumType.TriggerPoint;
import game.turnbasedsrv.fightObj.FightGeneralObject;
import game.turnbasedsrv.fightObj.FightHumanObject;
import game.turnbasedsrv.fightObj.FightMonsterObject;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.param.FightParamBase;
import game.turnbasedsrv.param.FightParamBoolean;
import game.turnbasedsrv.param.FightParamFighterList;
import game.turnbasedsrv.param.FightParamInitPropMonsterConf;
import game.turnbasedsrv.param.FightParamInt;
import game.turnbasedsrv.param.FightParamMonsterInfo;
import game.turnbasedsrv.param.FightParamPlot;
import game.turnbasedsrv.param.TriggerParam;
import game.turnbasedsrv.prop.PropManager;
import game.turnbasedsrv.skill.Skill;
import game.turnbasedsrv.skill.SkillCastData;
import game.turnbasedsrv.support.JsonKey;
import game.turnbasedsrv.trigger.Trigger;
import game.turnbasedsrv.trigger.TriggerCollection;
import game.turnbasedsrv.trigger.TriggerListen;
import game.turnbasedsrv.value.ValueBase;
import game.turnbasedsrv.value.ValueDouble;
import game.turnbasedsrv.value.ValueFactory;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfMap;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfPropAdd;
import game.worldsrv.config.ConfSceneEvent;
import game.worldsrv.config.ConfSkill;
import game.worldsrv.entity.FightRecord;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.entity.PartnerMirror;
import game.worldsrv.entity.Unit;
import game.worldsrv.fightParam.ResultParam;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalc;
import game.worldsrv.support.Utils;

public abstract class CombatObject {
	private static final int FlameTime = 33;// 帧时间
	public static final int FightPosAdd = 100;// 战场位置偏移值
	public static final int RageMax = 150;// 最大怒气值
	public static final int RageCast = 100;// 施法所需怒气
	public static final int RoundMax = 20;// 最大回合数，>=max则进入结算
	public static final int CurePct = 8000;// 治疗的百分比

	/** 地图配置 **/
	public StageObject mapStageObj;
	/** 地图配置SN（即confMap.sn） **/
	public int mapSn = 0;
	/** 玩法配置sn **/
	public int stageSn = 0;
	/** 固定随机数系统 **/
	public RandomMT19937 randUtils = null;
	/** 触发器 **/
	public TriggerCollection triggerManager;
	/** 战斗阶段 **/
	public CombatStepType stageStep;
	/** 战斗事件 **/
	public CombatEventBase stageEvent;
	/** 战斗回合数 **/
	public int round = 0;
	/** 战斗最大回合数 **/
	public int roundMax = RoundMax;
	/** 是否自动战斗 **/
	public Map<ETeamType, Boolean> autoFightMap = new HashMap<>();
	
	/** 加速倍率 **/
	public float speedTimes = 1;
	/** 优先出手的阵营 **/
	public ETeamType priorTeam = ETeamType.Team1;
	/** 出手顺序,小于100为左边，右边为战位加100 **/
	public List<Integer> orderList = new ArrayList<>();
	/** 已出手顺序,小于100为左边，右边为战位加100 **/
	public List<Integer> lastOrderList = new ArrayList<>();
	/** 怒气出手列表,小于100为左边，右边为战位加100 **/
	public List<Integer> specialOrderList = new ArrayList<>();
	/** 怒气已出手列表,小于100为左边，右边为战位加100 **/
	public List<Integer> lastSpecialOrderList = new ArrayList<>();
	/** 胜利战队 **/
	public ETeamType winTeam = ETeamType.Team0;
	/** 是否全是胜利 **/
	public boolean isAlwaysWin = false;
	/** 当前出手主动技的战斗者位置 **/
	public int nowOrderPosition = 0;
	/** 当前出手的主动技能是否是怒气技能 **/
	public boolean nowOrderIsSpecial = false;
	/** 是否跳过战斗 **/
	public boolean isQuickFight = false;
	/** 特殊参数：用于具体副本地图记录战斗过程中的数据，如副本星数等 **/
	public Param exParam = new Param();
	/** 是否是录像回放 **/
	public boolean isReplay = false;
	/** 是否需要录像 **/
	public boolean needRecord = false;
	
	/** 随机数种子 **/
	protected long randSeed = 100;
	/** 是否结束 **/
	protected boolean isEnd = false;
	/** 是否是暂停状态 **/
	public boolean isStop = false;
	/** 是否可以跳过战斗 **/
	protected boolean canQuickFight = false;
	/** 是否可以暂停 **/
	protected boolean canStop = true;
	/** 显示boss详情的怪物id **/
	protected int bossInfoId = -1;
	
	/** 所属地图配置 **/
	private ConfMap confMap;
	/** 玩法配置 **/
	protected ConfInstStage confInstStage;
	/** 场景配置 **/
	protected ConfInstMonster confInstMonster;
	/** 所属port **/
	private Port port;
	/** 副本创建时间 **/
	private long createTime;
	/** 这个场景当前的时间 **/
	private long currTime;
	/** 这个场景上个节点的时间 **/
	private long currLastTime;
	/** 距离上个帧结束的时间 **/
	private int deltaTime;
	/** 该地图内所有战斗单位 **/
	private Map<Integer, FightObject> fightObjs = new HashMap<>();
	/** 战斗单位站位 <fightPos, obj> **/
	private Map<Integer, FightObject> posFightObjs = new HashMap<>();
	/** 场景阶段行为 **/
	private Map<CombatStepType, List<CombatRuleBase>> stepActionMap = new HashMap<>();
	/** 场景阶段前置行为 **/
	private Map<CombatStepType, List<CombatRuleBase>> stepBeforeActionMap = new HashMap<>();
	/** 场景阶段结束检测 **/
	private Map<CombatStepType, List<CombatRuleBase>> stepFinishCheckMap = new HashMap<>();	
	/** 特殊操作相关的处理 **/
	private Map<CombatOpType,List<CombatRuleBase>> opActionMap = new HashMap<>();
	/** 场景结束检测 **/
	private List<CombatRuleBase> stageFinishCheckList = new ArrayList<>();
	/** 事件列表 **/
	private List<CombatEventBase> eventList = new ArrayList<>();
	/** 监听列表 **/
	private List<TriggerListen> stepListenList = new ArrayList<>();
	/** 全局监听列表 **/
	private List<TriggerListen> listenList = new ArrayList<>();
	/** 战斗对象ID **/
	private int fightId = 0;
	/** 暂停时间 **/
	private long stopTime = 0;
	
	/** 战斗阶段当前数 **/
	public int step = 0;
	public int stepAll = 0;
	/** scene列表 **/
	private List<Integer> sceneList = new ArrayList<>();
	
	// 保存场景中每个队伍的玩家镜像数据，供数据查询用
	public Map<ETeamType, HumanMirrorObject> teamHumanMirMap = new HashMap<>();
	// 玩家准备状态列表
	protected Map<Long, Boolean> humanReadyMap = new HashMap<>();
	// 参与战斗人数，要和玩家准备状态列表个数一致才开始战斗
	private int fightNum = 0;
	public int fightType;// 战斗类型
	
//	private TickTimer waitDestoryTimer; // 等待地图销毁定时器
//	private static final long WAIT_DESTORY_TIME = 10 * Time.SEC;// 删除地图时间
	private static final long WAIT_CHECK_TIME = 1 * Time.MIN;// 等待客户端响应时间

	/** 等待玩家定时器 **/
	private TickTimer waitCheckTimer;
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
	
	
	protected ConfPropAdd team1_ConfPropAdd = null; // 队伍1，战力压制配表PorpAdd
	protected ConfPropAdd team2_ConfPropAdd = null; // 队伍2，战力压制配表PorpAdd
	
	
	/**
	 * 构造函数
	 * 
	 * @param port
	 * @param repSn
	 * @param mapSn
	 */
	public CombatObject(Port port, StageObject mapStageObj, int repSn, int mapSn) {
		this.triggerManager = new TriggerCollection(this);
		this.port = port;
		this.stageSn = repSn;
		this.confInstStage = ConfInstStage.get(repSn);
		if (confInstStage == null) {
			Log.fight.error("ConfRepStage配表错误，no find sn ={}", repSn);
		}
		this.mapSn = mapSn;
		this.confMap = ConfMap.get(this.mapSn);
		if (confMap == null) {
			Log.fight.error("ConfMap配表错误，no find sn ={}", mapSn);
		}
		this.confInstStage = ConfInstStage.get(stageSn);
		if (this.confInstStage == null) {
			Log.fight.error("ConfInstStage配表错误，no find sn={}", stageSn);
		} else {
			if (this.confInstStage.monsterSN != null) {
				for (int sn : this.confInstStage.monsterSN) {
					ConfInstMonster conf = ConfInstMonster.get(sn);
					if (conf == null) {
						Log.fight.error("ConfInstMonster配表错误，no find sn={},stageSn={}", sn, stageSn);
						break;
					}
					stepAll++;
					sceneList.add(sn);
					if (null == this.confInstMonster) {
						this.confInstMonster = conf;
					}
				}
			}
		}

		this.mapStageObj = mapStageObj;
		createTime = Port.getTime();
		currTime = Port.getTime();
		currLastTime = currTime;

		init();
	}

	public CombatObject(Port port, StageObject mapStageObj, int stageSn, int mapSn, int fightType) {
		this(port, mapStageObj, stageSn, mapSn);
		this.fightType = fightType;
		this.isRecordFight = false;
		this.fightNum = 1;
	}
	
	public CombatObject(Port port, StageObject mapStageObj, int stageSn, int mapSn, int fightType,
			int fightNum) {
		this(port, mapStageObj, stageSn, mapSn);
		this.fightType = fightType;
		this.isRecordFight = false;
		this.fightNum = fightNum;
	}

	public CombatObject(FightRecord fightRecord, Port port, StageObject mapStageObj, int stageSn, int mapSn) {
		this(port, mapStageObj, stageSn, mapSn);
		this.fightType = fightRecord.getFightType();
		this.needRecord = false;
		this.isReplay = true;
		this.fightRecord = fightRecord;
		this.isRecordFight = false;
		this.fightNum = 1;

		initFightReplay();
	}
	
	/** 是否是最后一个场景 **/
	public boolean isLastScene(){
		return this.step < this.sceneList.size();
	}
	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
	
	/**
	 * 是否战斗结束
	 * @return
	 */
	public boolean isEnd(){
		return isEnd;
	}

	/**
	 * 获取port
	 * 
	 * @return
	 */
	public Port getPort() {
		return port;
	}
	
	/**
	 * 获取战斗分配ID
	 * 
	 * @return
	 */
	public int getFightId() {
		if (fightId == Integer.MAX_VALUE) {
			fightId = 0;
		}
		fightId++;
		return fightId;
	}

	/**
	 * 设置随机数种子
	 */
	public void setRandUtils() {
		this.randSeed = (long) (Math.random() * 65536);
		Log.fight.info("fight RandSeed = " + randSeed);
		this.randUtils = new RandomMT19937(this.randSeed);
	}

	/**
	 * 初始化
	 */
	public void init() {
		setRandUtils();
		initNextScene();
		this.stageStep = CombatStepType.CombatInit;
		this.setStageStep(CombatStepType.CombatWaitFighter);
	}
	
	public void initNextScene(){
		if(this.sceneList.size()>this.step){
			this.confInstMonster = ConfInstMonster.get(this.sceneList.get(this.step));
		}
		else{
			this.confInstMonster = null;
		}
		stepActionMap.clear();
		stepBeforeActionMap.clear();
		stepFinishCheckMap.clear();
		opActionMap.clear();
		stageFinishCheckList.clear();
		
		if(this.confInstMonster==null){
			Log.fight.error("confScene配表错误，no find next Scene,stageSn={},step={}",this.stageSn,this.step);
			return;
		}
		if (this.confInstMonster.eventIDs == null) {
			Log.table.error("ConfScene配表错误 eventIDs=null，sn={}", confInstMonster.sn);
		} else {
			// 初始化所有场景事件
			for (int id : this.confInstMonster.eventIDs) {
				ConfSceneEvent confEvent = ConfSceneEvent.get(id);
				if (confEvent == null) {
					Log.table.error("confEvent配表错误，no find sn={},stageSn={}", id,this.stageSn);
					continue;
				}
				CombatRuleBase action = CombatRuleFactory.getStageAction(id, confEvent.type, confEvent.param);
				if (null == action) {
					Log.fight.error("===no find CombatRule,id={},confEvent={}", id, confEvent);
				} else {
					action.init(this);
				}
			}			
		}
	}
	public void removeAction(CombatRuleBase action){
		List<CombatStepType> keyList = new ArrayList<>();
		keyList.addAll(stepActionMap.keySet());
		for(CombatStepType key:keyList){
			List<CombatRuleBase> actionList = stepActionMap.get(key);
			if (actionList != null && actionList.contains(action)) {
				actionList.remove(action);
				if(actionList.isEmpty()){
					stepActionMap.remove(key);
				}
			}
		}
		
		keyList.clear();
		keyList.addAll(stepBeforeActionMap.keySet());
		for(CombatStepType key:keyList){
			List<CombatRuleBase> actionList = stepBeforeActionMap.get(key);
			if (actionList != null && actionList.contains(action)) {
				actionList.remove(action);
				if(actionList.isEmpty()){
					stepBeforeActionMap.remove(key);
				}
			}
		}
		
		keyList.clear();
		keyList.addAll(stepFinishCheckMap.keySet());
		for(CombatStepType key:keyList){
			List<CombatRuleBase> actionList = stepFinishCheckMap.get(key);
			if (actionList != null && actionList.contains(action)) {
				actionList.remove(action);
				if(actionList.isEmpty()){
					stepFinishCheckMap.remove(key);
				}
			}
		}
		

		List<CombatOpType> opKeyList = new ArrayList<>();
		opKeyList.addAll(opActionMap.keySet());
		for(CombatOpType key:opKeyList){
			List<CombatRuleBase> actionList = opActionMap.get(key);
			if (actionList != null && actionList.contains(action)) {
				actionList.remove(action);
				if(actionList.isEmpty()){
					opActionMap.remove(key);
				}
			}
		}
		
		if(stageFinishCheckList.contains(action)){
			stageFinishCheckList.remove(action);
		}
	}
	/**
	 * 增加场景阶段行为
	 * 
	 * @param step
	 * @param action
	 */
	public void addStepAction(CombatStepType step, CombatRuleBase action) {
		List<CombatRuleBase> actionList = stepActionMap.get(step);
		if (actionList == null) {
			actionList = new ArrayList<>();
			stepActionMap.put(step, actionList);
		}
		actionList.add(action);
	}

	/**
	 * 增加场景阶段前置行为
	 * 
	 * @param step
	 * @param action
	 */
	public void addStepBeforeAction(CombatStepType step, CombatRuleBase action) {
		List<CombatRuleBase> actionList = stepBeforeActionMap.get(step);
		if (actionList == null) {
			actionList = new ArrayList<>();
			stepBeforeActionMap.put(step, actionList);
		}
		actionList.add(action);
	}

	/**
	 * 增加场景阶段结束检测
	 * 
	 * @param step
	 * @param action
	 */
	public void addStepFinishCheck(CombatStepType step, CombatRuleBase action) {
		List<CombatRuleBase> actionList = stepFinishCheckMap.get(step);
		if (actionList == null) {
			actionList = new ArrayList<>();
			stepFinishCheckMap.put(step, actionList);
		}
		actionList.add(action);
	}

	/**
	 * 增加特殊操作处理
	 * 
	 * @param op
	 * @param action
	 */
	public void addStageOpAction(CombatOpType op, CombatRuleBase action) {
		List<CombatRuleBase> actionList = opActionMap.get(op);
		if (actionList == null) {
			actionList = new ArrayList<>();
			opActionMap.put(op, actionList);
		}
		actionList.add(action);
	}


	/**
	 * 删除特殊操作处理
	 * 
	 * @param op
	 * @param action
	 */
	public void removeStageOpAction(CombatOpType op, CombatRuleBase action) {
		List<CombatRuleBase> actionList = opActionMap.get(op);
		if (actionList != null && actionList.contains(action)) {
			actionList.remove(action);
		}
	}
	/**
	 * 增加场景结束检测
	 * 
	 * @param action
	 */
	public void addStageFinishCheck(CombatRuleBase action) {
		stageFinishCheckList.add(action);
	}

	/**
	 * 增加阶段监听
	 * 
	 * @param listen
	 * @return
	 */
	public boolean addStepListen(TriggerListen listen) {
		if (stepListenList.contains(listen)) {
			return false;
		}
		if (!this.triggerManager.addListen(listen)) {
			return false;
		}
		stepListenList.add(listen);
		return true;
	}
	public void delStepListen(TriggerListen listen) {
		if (stepListenList.contains(listen)) {
			stepListenList.remove(listen);
		}
		this.triggerManager.delListen(listen);
	}

	/**
	 * 增加全局监听
	 * 
	 * @param listen
	 * @return
	 */
	public boolean addListen(TriggerListen listen) {
		if (listenList.contains(listen)) {
			return false;
		}
		if (!this.triggerManager.addListen(listen)) {
			return false;
		}
		listenList.add(listen);
		return true;
	}

	public void delListen(TriggerListen listen) {
		if (listenList.contains(listen)) {
			listenList.remove(listen);
		}
		this.triggerManager.delListen(listen);
	}

	public boolean addTrigger(Trigger trigger) {
		return this.triggerManager.addTrigger(trigger);
	}

	/**
	 * 改变战斗阶段
	 * 
	 * @param stageStep
	 */
	public void setStageStep(CombatStepType stageStep) {
		this.stageStep = stageStep;

		// 执行场景阶段前置行为
		List<CombatRuleBase> beforeList = stepBeforeActionMap.get(this.stageStep);
		if (beforeList != null) {
			List<CombatRuleBase> excuteList = new ArrayList<>();
			excuteList.addAll(beforeList);
			for (int i = 0; i < excuteList.size(); i++) {
				CombatRuleBase action = excuteList.get(i);
				if(action.isValid){
					action.doStepActionBefore(this);
				}
			}
		}
		// 检测场景阶段触发器
		checkStepTrigger(stageStep);

		// 执行场景阶段行为
		List<CombatRuleBase> actionList = stepActionMap.get(this.stageStep);
		if (actionList != null) {
			List<CombatRuleBase> excuteList = new ArrayList<>();
			excuteList.addAll(actionList);
			for (int i = 0; i < excuteList.size(); i++) {
				CombatRuleBase action = excuteList.get(i);
				if(action.isValid){
					action.doStepAction(this);
				}
			}
		}
		// 是否场景结束
		if (this.stageStep == CombatStepType.CombatEnd) {
			this.finishFight();
		}
	}

	/**
	 * 检测场景阶段触发器
	 * 
	 * @param stageStep
	 */
	private void checkStepTrigger(CombatStepType stageStep) {
		// 根据场景阶段设置触发阶段
		TriggerPoint triggerPoint = null;
		switch (stageStep) {
		case CombatStart:
			triggerPoint = TriggerPoint.CombatStart;
			break;
		case StepStart:
			triggerPoint = TriggerPoint.StepStart;
			break;
		case StepEnd:
			triggerPoint = TriggerPoint.StepEnd;
			break;
		case RoundStart:
			triggerPoint = TriggerPoint.RoundStart;
			break;
		case RoundOrderStart:
			triggerPoint = TriggerPoint.RoundOrderStart;
			break;
		case RoundOrderEnd:
			triggerPoint = TriggerPoint.RoundOrderEnd;
			break;
		case RoundEnd:
			triggerPoint = TriggerPoint.RoundEnd;
			break;
		default:
			break;
		}
		if (triggerPoint != null) {
			TriggerParam param = new TriggerParam(this);
			Trigger trigger = new Trigger(triggerPoint, param);
			// 增加触发器
			this.triggerManager.addTrigger(trigger);
			// 执行触发器
		}
		this.triggerManager.doTrigger();
	}

	/**
	 * 正常心跳
	 */
	public void pulse() {
		if (ttWaitCastSkill != null) {
			if (ttWaitCastSkill.isPeriod(getTime())) {
				if(this.stageEvent instanceof EventWaitHumanOp){
					EventWaitHumanOp event = (EventWaitHumanOp)this.stageEvent;
					event.setFinish(this);
				}
				this.doStageOp(CombatOpType.HumanCommmonSkill, null);
//				stopCastSkill();// 玩家手动释放技能，停止计时器，解除暂停状态
//				// 玩家随机释放技能
//				Integer pos = this.orderList.get(0);
//				FightObject fightObj = this.getObjByFightPos(pos);
//				if (fightObj.isHuman()) {
//					if (fightObj.canCastCommonSkill()) {
//						fightObj.castCommonSkill();
//					}
//				}
			}
		}
		if (waitCheckTimer != null) {
			if (waitCheckTimer.isPeriod(Port.getTime())) {
				waitCheckTimer = null;
			}
		}
//XXX 不可删除地图
//		if (waitDestoryTimer != null) {
//			if (waitDestoryTimer.isPeriod(Port.getTime())) {
//				waitDestoryTimer = null;
//				this.mapStageObj.destory();
//				return;
//			}
//			return;
//		}
		if (isEnd) {
			return;
		}
		if (isStop) {
			pulseStop();
			return;
		}
		// 执行各单位本次心跳中的事务
		this.currTime = Port.getTime();
		this.deltaTime += (int) (currTime - currLastTime);
		this.currLastTime = this.currTime;
		if (this.deltaTime >= CombatObject.FlameTime) {
			pulseStageEvent(this.deltaTime);
			pulseStageStep();
			this.deltaTime = 0;
		}
	}

	private void pulseStop() {
		this.currTime = Port.getTime();
		this.deltaTime += (int) (currTime - currLastTime);
		this.currLastTime = this.currTime;
		if (this.deltaTime >= CombatObject.FlameTime) {
			stopTime = stopTime + deltaTime;
			this.deltaTime = 0;
		}
	}

	protected void pulseStageEvent(int deltaTime) {
		int leftTime = (int) (deltaTime * this.speedTimes);
		if (this.stageEvent != null) {
			CombatEventBase event = this.stageEvent;
			leftTime = this.stageEvent.pulse(this, leftTime);
			while (this.stageEvent != null && event != this.stageEvent && leftTime > 0) {
				event = this.stageEvent;
				leftTime = this.stageEvent.pulse(this, leftTime);
			}
		}
	}

	protected void pulseStageStep() {
		// 执行触发器
		if (this.triggerManager.hasTrigger()) {
			this.triggerManager.doTrigger();
		}
		// 跳过战斗，不处理事件
		if (isQuickFight) {
			if (this.stageEvent != null) {
				this.stageEvent.setDefaultFinish(this);
				return;
			}
		}
		else{
			if (this.stageEvent != null) {
				return;
			}
		}
		// 检测场景是否结束
		for (int i = 0; i < stageFinishCheckList.size(); i++) {
			CombatRuleBase action = stageFinishCheckList.get(i);
			if(action.isValid){
				if (action.checkStageFinish(this)) {
					this.setStageStep(CombatStepType.CombatEnd);
					return;
				}
			}
		}
		// 检测场景阶段是否结束
		List<CombatRuleBase> actionList = stepFinishCheckMap.get(this.stageStep);
		if (actionList != null) {
			for (int i = 0; i < actionList.size(); i++) {
				CombatRuleBase action = actionList.get(i);
				if(action.isValid){
					if (!action.checkStepFinish(this)) {
						return;
					}
				}
			}
		}
		// 检测是否切换至下个场景阶段
		List<CombatRuleBase> checkActionList = stepFinishCheckMap.get(this.stageStep);
		if (checkActionList != null) {
			CombatStepType oldStep = this.stageStep;
			for (int i = 0; i < checkActionList.size(); i++) {
				CombatRuleBase action = actionList.get(i);
				if(action.isValid){
					action.setNextStep(this, oldStep);
					if (this.stageStep != oldStep) {
						break;
					}
				}
			}
		}
	}
	
	public void removeStageEvent(CombatEventBase stageEvent) {
		if (this.stageEvent == stageEvent) {
			if (this.eventList.size() > 0) {
				this.stageEvent = this.eventList.remove(0);
			} else {
				this.stageEvent = null;
			}
		} else if (this.eventList.contains(stageEvent)) {
			this.eventList.remove(stageEvent);
		}
	}

	public void addStageEvent(CombatEventBase stageEvent) {
		if (this.stageEvent == null) {
			this.stageEvent = stageEvent;
		} else {
			this.eventList.add(stageEvent);
		}
	}

	/**
	 * 是否存在指定模型sn的对象
	 * @return
	 */
	public boolean isExistRelation(int[] modelSns, ETeamType team) {
		int num = 0;
		for (int modelSn : modelSns) {
			for (FightObject obj : fightObjs.values()) {
				// 同一个队伍 且 模型符合
				if (obj.team == team && obj.modelSn == modelSn) {
					num++;
					break;
				}
			}
		}
		if (modelSns.length == num) {
			return true;
		} else {
			return false;
		}
	}
	
	public FightObject getFightObj(int objId) {
		return fightObjs.get(objId);
	}

	public Map<Integer, FightObject> getFightObjs() {
		return fightObjs;
	}

	public int getFightObjCount() {
		return fightObjs.size();
	}
	
	/**
	 * 获取玩家镜像对象
	 * 
	 * @return
	 */
	public HumanMirrorObject getHumanMirrorObject(long humanId) {
		HumanMirrorObject objMir = null;
		for(HumanMirrorObject obj : this.teamHumanMirMap.values()) {
			if (obj != null && obj.getHumanId() != humanId) {
				objMir = obj;
				break;
			}
		}
		return objMir;
	}
	
	/**
	 * 获取指定战队的战力
	 * 
	 * @param team
	 * @return
	 */
	public int getTeamCombat(ETeamType team) {
		int combat = this.confInstStage.power;
		if (combat != 0 && team == ETeamType.Team2) {
			return combat;
		}
		combat = 0;
		for(FightObject obj : this.getFightObjs().values()) {
			if (obj != null && obj.team == team) {
				combat += obj.combat;
			}
		}
		return combat;
	}

	/**
	 * 获取指定战队的在场人数
	 * 
	 * @param team
	 * @return
	 */
	public int getTeamCount(ETeamType team) {
		int count = 0;
		for (FightObject obj : fightObjs.values()) {
			if (obj.team == team) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 指定战队的玩家对象是否死亡
	 * 
	 * @param team
	 * @return
	 */
	public boolean isDieTeamHuman(ETeamType team) {
		boolean ret = true;
		for (FightObject obj : fightObjs.values()) {
			if (obj.team == team && obj.isHuman()) {
				ret = false;
				break;
			}
		}
		return ret;
	}
	
	/**
	 * 获取指定战队的所有对象的最大血量总和
	 * 
	 * @param team
	 * @return
	 */
	public long getTeamTotalMaxHp(ETeamType team) {
		long total = 0;
		for (FightObject obj : fightObjs.values()) {
			if (obj.team == team) {
				total += PropManager.inst().getMaxHp(obj);
			}
		}
		return total;
	}
	
	/**
	 * 获取指定战队的所有对象的当前血量总和
	 * 
	 * @param team
	 * @return
	 */
	public long getTeamTotalCurHp(ETeamType team) {
		long total = 0;
		for (FightObject obj : fightObjs.values()) {
			if (obj.team == team) {
				total += PropManager.inst().getCurHp(obj);
			}
		}
		return total;
	}

	/**
	 * 获得指定战场位置的对象
	 * 
	 * @param fightPos
	 *            战场位置
	 * @return
	 */
	public FightObject getObjByFightPos(int fightPos) {
		return posFightObjs.get(fightPos);
	}

	/**
	 * 获得指定位置的对象
	 * 
	 * @param pos
	 *            队伍位置
	 * @param team
	 *            所在战队
	 * @return
	 */
	public FightObject getObjByPos(int pos, ETeamType team) {
		// 1号战队即左下方，队中位置：0-9，战场位置：100-109
		// 2号战队即右上方，队中位置：0-9，战场位置：0-9
		if (team == ETeamType.Team1) {
			return posFightObjs.get(pos + CombatObject.FightPosAdd);
		} else {
			return posFightObjs.get(pos);
		}
	}

	public void _addFightObj(FightObject obj) {
		fightObjs.put(obj.idFight, obj);
		int pos = obj.getFightPos();
		posFightObjs.put(pos, obj);
	}

	public void _delFightObj(FightObject obj) {
		fightObjs.remove(obj.idFight);
		int pos = obj.getFightPos();
		posFightObjs.remove(pos);
	}

	/**
	 * 返回当前时间
	 * 
	 * @return
	 */
	public long getTime() {
		return currTime;
	}

	/**
	 * 返回副本创建时间
	 * 
	 * @return
	 */
	public long getCreateTime() {
		return createTime;
	}

	/**
	 * 获取结果
	 * @param type
	 * @param param
	 */
	public FightParamBase doStageOp(CombatOpType type, FightParamBase param){
		if(opActionMap.containsKey(type)){
			List<CombatRuleBase> list = opActionMap.get(type);
			List<CombatRuleBase> excuteList = new ArrayList<>();
			excuteList.addAll(list);
			for(CombatRuleBase action:excuteList){
				if(action.isValid){
					FightParamBase result = action.doStageOp(this,type, param);
					if(result!=null){
						return result;
					}
				}
			}
		}
		
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
			for (FightObject fightObj : fightObjs.values()) {
				// FIXME ..激活被动 调整到这里 
				if (fightObj != null) {
					fightObj.startup();
				}
			}
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
		default:
			break;
		}
		return null;
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
	public boolean checkStageOp(CombatOpType type, FightParamBase param){
		switch(type){
		default:
			break;
		}
		return false;
	}
	
	//XXX ---------------------------------
	/**
	 * 增加怒气技能列表
	 * @param pos
	 */
	public void addSpecialList(int pos){
		this.specialOrderList.add(pos);	
		if(this.needRecord){
			this.fightRecordSpecialOrderList.add(pos);
		}
	}

	/**
	 * 快速结算
	 * 
	 */
	public ResultParam startupQuick() {
		this.isQuickFight = true;
		this.isRecordFight = true;
		int count = 1;
		while (!this.isEnd) {
			pulseStageEvent(1);
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

	/**
	 * 一定时间后销毁地图
	 */
	public void waitDestory() {
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
		for (HumanObject obj : this.mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
			SCTurnbasedSpeed.Builder msg = SCTurnbasedSpeed.newBuilder();
			msg.setSpeed(speed);
			obj.sendMsg(msg.build());
		}
	}

	/**
	 * 修改是否自动战斗
	 * 
	 * @param humanObj
	 * @param auto
	 */
	public void onChangeAutoFight(HumanObject humanObj, boolean auto) {
		if (this.isReplay) {
			return;
		}
		this.autoFightMap.put(humanObj.fightTeam, auto);

		if (this.needRecord) {
			// 录像记录自动战斗
			this.fightRecordAutoFightMap.put(humanObj.fightTeam, auto);
		}

		SCTurnbasedAutoFight.Builder msg = SCTurnbasedAutoFight.newBuilder();
		msg.setAuto(auto);
		humanObj.sendMsg(msg.build());
		
		// 如果自动战斗时，有主角出手事件
		if(auto && this.stageEvent instanceof EventWaitHumanOp){
			EventWaitHumanOp event = (EventWaitHumanOp)this.stageEvent;
			event.setFinish(this);
			
			// 处理主角自动释放技能操作
			this.doStageOp(CombatOpType.HumanCommmonSkill, null);
		}
	}

	/**
	 * 玩家开始战斗
	 * 
	 * @param humanObj
	 */
	public void onCombatantStartFight(HumanObject humanObj) {
		humanReadyMap.put(humanObj.id, true);
	}

	/**
	 * 玩家跳过战斗
	 * 
	 * @param humanObj
	 */
	public void onCombatantQuickFight(HumanObject humanObj) {
		if (!this.canQuickFight) {
			return;
		}
		if (isQuickFight) {
			return;
		}
		if (isQuickFight) {
			if(this.isEnd){
				finishFight();
			}
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

		long oldTime = System.nanoTime();
		this.autoFightMap.put(humanObj.fightTeam, true);
		// 将暂停设置为false(如果主角第一个出手，则暂停状态为true，将会出现我方不出手，对方胜利的bug)
		this.isStop = false; 
		this.isQuickFight = true;
		int count = 1;
		while (!this.isEnd) {
			pulseStageEvent(1);
			pulseStageStep();
			count = count + 1;
			if (count > 1000000) {
				break;
			}
		}
		long lastTime = System.nanoTime() - oldTime;
		Log.logInfo(Log.fight, "战斗{}跳过，用时{}ns,count={}",fightType, lastTime,count);
	}

	/*
	 * 等待回合结束确认
	 */
	public void onCombatantRoundEnd(HumanObject humanObj, int round) {
		if (this.round != round) {
			return;
		}
		humanReadyMap.put(humanObj.id, true);
	}

	/*
	 * 战斗暂停
	 */
	public void onCombatantStopFight(HumanObject humanObj, boolean isStop) {
		if (!this.canStop) {
			return;
		}
		if (this.isStop == isStop) {
			return;
		}
		this.isStop = isStop;
		SCTurnbasedStopFight.Builder msg = SCTurnbasedStopFight.newBuilder();
		msg.setStop(isStop);
		humanObj.sendMsg(msg.build());
	}

	/**
	 * 退出战斗
	 */
	public void onCombatantLeaveFight(HumanObject humanObj) {
		// 根据主动离开的玩家所在队伍，判定输赢
		if (humanObj.fightTeam == ETeamType.Team1) {
			this.winTeam = ETeamType.Team2;
		} else if (humanObj.fightTeam == ETeamType.Team2) {
			this.winTeam = ETeamType.Team1;
		}
		// 结束战斗
		finishFight();
	}
	
	public void onMsgActionEnd(HumanObject humanObj, int select) {
		if(this.stageEvent instanceof EventPlot){
			EventPlot event = (EventPlot)this.stageEvent;
			event.setFinish(this, select);
		}
	}
	public void onMsgMonterChangeEnd(HumanObject humanObj) {
		if(this.stageEvent instanceof EventFighterStageShow){
			EventFighterStageShow event = (EventFighterStageShow)this.stageEvent;
			event.setFinish(this);
		}
	}
	/**
	 * 玩家施放技能
	 * 
	 * @param humanObj
	 * @param casterId
	 * @param sn
	 */
	public void onCombatantCastSkill(HumanObject humanObj, int casterId, int sn) {
		if (this.isReplay) {
			return;
		}
		FightObject fightObj = this.getFightObjs().get(casterId);
		if (fightObj == null) {
			return;
		}
		// 战队判断：施法者是否属于玩家所在战队
		if (humanObj.fightTeam != fightObj.team) {
			return;
		}
		ConfSkill confSkill = ConfSkill.get(sn);
		if (null == confSkill) {
			return;
		}
		// 怒气判断
		int rage = PropManager.inst().getCurRage(fightObj);
		if (rage < confSkill.fireClearRage) {
			Log.fight.debug("怒气不足：{} 施放技能={},", fightObj.name, confSkill.sn);
			return;// 怒气不足
		}
		
		if(this.stageEvent instanceof EventWaitHumanOp){
			EventWaitHumanOp event = (EventWaitHumanOp)this.stageEvent;
			event.setFinish(this);
		}
		
		int fightPos = fightObj.getFightPos();
		// 技能类型判断
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
			if (lastOrderList.contains(fightPos)) {
				return;// 本回合已经释放过了
			}
			// 释放技能
			for (int i = 0; i < fightObj.commonSkillList.size(); i++) {
				if (fightObj.commonSkillList.get(i).sn == sn) {
					fightObj.indexOfCommonSkill = i;
					
					// 处理主角释放技能操作
					this.doStageOp(CombatOpType.HumanCommmonSkill, null);
					
//					if (fightObj.canCastCommonSkill()) {
//						fightObj.castCommonSkill();
//					}
					break;
				}
			}
		}
	}

	/**
	 * 玩家手动释放技能，停止计时器，解除暂停状态
	 */
	public void stopCastSkill() {
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
		if (this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		for (HumanObject obj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
			if (obj.isStageSwitching())
				continue;
			
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
					if (fightObj.isMonster() && fightObj.combatObj.confInstMonster != null
							&& team2.getSnInstMonster() != fightObj.combatObj.confInstMonster.sn) {
						team2.setSnInstMonster(fightObj.combatObj.confInstMonster.sn);
					}
				}
			}
			msg.setTeam1(team1);
			msg.setTeam2(team2);
			msg.setSelfTeam(obj.fightTeam);
			msg.setPriorTeam(priorTeam);
			msg.setBossId(bossInfoId);
			msg.setCanQuickFight(canQuickFight);
			int multiple = 0;
			FightParamBase param = this.doStageOp(CombatOpType.GetStepMultipe, null);
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
	public final void sendRoundChangeInfoMsg(List<DTurnbasedBuff> buffRemoveList) {
		if(this.isQuickFight){//跳过战斗不发送中间消息
			return;
		}
		for (HumanObject obj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
			SCTurnbasedRoundChange.Builder msg = SCTurnbasedRoundChange.newBuilder();
			msg.setRound(round);
			msg.setMaxRound(roundMax);
			msg.addAllBuff(buffRemoveList);
			obj.sendMsg(msg.build());
		}
	}

	/**
	 * 发送技能施法消息
	 * @param skillData
	 */
	public final void sendCastSkillInfoMsg(SkillCastData skillData) {
		if(this.isQuickFight){//跳过战斗不发送中间消息
			return;
		}
		for (HumanObject obj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
			SCTurnbasedCastSkill.Builder msg = SCTurnbasedCastSkill.newBuilder();
			msg.setCasterId(skillData.creator.idFight);
			msg.setSn(skillData.skill.sn);
			msg.addAllTargetList(skillData.createTargetDataMsg());//技能目标列表的战斗数据变化
			if (skillData.casterData != null) {
				msg.setCaster(skillData.casterData.createTargetDataMsg());//施法者的战斗数据变化
			}
			obj.sendMsg(msg.build());
		}
	}
	
	/**
	 * 出手结束
	 * @param buffRemoveList 移除的buff
	 */
	public final void sendRoundOrderEndMsg(List<DTurnbasedBuff> buffRemoveList) {
		if(this.isQuickFight){//跳过战斗不发送中间消息
			return;
		}
		if (buffRemoveList.isEmpty()) {
			return;
		}
		for (HumanObject obj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
			SCTurnbasedRoundOrderEnd.Builder msg = SCTurnbasedRoundOrderEnd.newBuilder();
			msg.addAllBuff(buffRemoveList);
			obj.sendMsg(msg.build());
		}
	}
	
	/**
	 * 发送buff生效消息
	 * 
	 * @param buffData
	 * @param isRemove
	 */
	public final void sendBuffTriggerInfoMsg(BuffTriggerData buffData, boolean isRemove) {
		if(this.isQuickFight) {//跳过战斗不发送中间消息
			return;
		}
		if (buffData == null) {// || buffData.damage == 0 && buffData.rage == 0) {
			return;
		}
		for (HumanObject obj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
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
	 * 发送结束包给玩家
	 */
	public void sendFinishMsgToCambatants() {
		if (this.isReplay) {
			for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
				if (!isCombatFightObj(humanObj.getHumanId())) {
					continue;
				}
				humanObj.sendMsg(this.getFinishMsgFromRecord());
			}
			return;
		}
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			humanObj.sendMsg(this.getFinishMsg());
		}
	}

	/**
	 * 获取结果消息
	 * 
	 * @return
	 */
	public abstract SCTurnbasedFinish getFinishMsg();

	/**
	 * 战斗结束消息
	 */
	public final void finishFight() {
		isEnd = true;
		if(!this.isRecordFight){
			if (this.isReplay||this.fightType == ECrossFightType.FIGHT_NEWBIE_VALUE) {
				this.sendFinishMsgToCambatants();
			}
			else{
				sendFinishFightMsg(getFinishMsg());
			}
		}
		if (this.needRecord) {
			setFinishRecordInfo();
		}
//		XXX 不可删除地图
//		waitDestoryTimer = new TickTimer(WAIT_DESTORY_TIME);
	}

	/**
	 * 战斗结束,发送结果给游戏服
	 */
	private void sendFinishFightMsg(SCTurnbasedFinish msg) {
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()){
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			humanObj.crossFightFinishMsg = getFinishMsg();
			humanObj.crossFightTeamId = humanObj.fightTeam.getNumber();
			humanObj.sendMsg(msg);
		}
	}

	/**
	 * 战斗暂停，等待玩家选择技能
	 */
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
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()){
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			// 通知客户端轮到主角选择技能
			SCTurnbasedHumanSelSkill.Builder msgSel = SCTurnbasedHumanSelSkill.newBuilder();
			msgSel.setFighterId(idFight);
			msgSel.setTimeout(timeOut);
			humanObj.sendMsg(msgSel.build());
		}
		
		ttWaitCastSkill = new TickTimer(tmStop * Time.SEC);
		
		// 添加等待玩家出手事件
		this.addStageEvent(new EventWaitHumanOp());
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
	protected void calcCombatSuppress() {
		// 需要压制的玩法类型
//		List<Integer> suppressType = ParamManager.combatCapabilityRepressio;
//		if (!suppressType.contains(fightType)) {
//			return;
//		}
		// 是否需要战力压制
		boolean isRepressio = this.confInstStage.repression;
		if (!isRepressio) {
			return;
		}
		
		// 我方总战力
		int team1Combat = getTeamCombat(ETeamType.Team1);
		// 对方总战力
		int team2Combat = getTeamCombat(ETeamType.Team2);

		// 压制系数(万分比)
		int ratio = 0;
		// 战力差
		int distance = team1Combat - team2Combat;
		if (distance > 0) {
			ratio = Math.abs(distance * Utils.I10000 / team2Combat); // 队伍1战力高
		} else {
			ratio = Math.abs(distance * Utils.I10000 / team1Combat); // 队伍2战力高
		}
		int curSn = -1;
		ConfPropAdd[] confs = ConfPropAdd.findArray();
		ConfPropAdd conf = null;
		for (int i = confs.length - 1; i >= 0 ; i--) {
			conf = confs[i];
			if (conf == null) {
				continue;
			}
			if(curSn == -1){
				curSn = conf.sn;
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
		
		calcCombatSuppressProp();
	}
	/**
	 * 战力压制影响的属性
	 */
	protected void calcCombatSuppressProp() {
		ConfPropAdd confPropAdd = null;
		for (FightObject fightObj : fightObjs.values()) {
			// 获取属性压制配置
			confPropAdd = fightObj.team == ETeamType.Team1 ? team1_ConfPropAdd : team2_ConfPropAdd;
			if (confPropAdd == null) {
				continue;
			}
			String propName = ""; // 属性名
			ValueBase attrValue = null; // 属性值
			ValueDouble addValue = null; // 加成值
			for (int i = 0; i < confPropAdd.attr.length; i++) {
				propName = confPropAdd.attr[i];
				attrValue = fightObj.prop.getOnePropValue(propName);
				// 加成比例
				addValue = new ValueDouble(confPropAdd.attrAdd[i] / Utils.D10000);
				// 加成值 = 比例 x 属性值
				addValue.multiply(attrValue); 
				fightObj.prop.addPropValue(propName, addValue);
			}
			// 当前血量同步
			PropManager.inst().restoreMaxHp(fightObj);
		}
	}
	
	
	/**
	 * 玩家镜像转为战斗对象
	 */
	public void humanMirrorObjToFightObj() {
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if(null == humanObj.humanMirrorObj) {
				return;
			}
			humanMirrorObjToFightObj(humanObj.humanMirrorObj, humanObj.fightTeam);
		}
	}
	/**
	 * 玩家镜像转为战斗对象
	 * @param intParam
	 */
	private void humanMirrorObjToFightObj(FightParamInt intParam) {
		ETeamType team = ETeamType.valueOf(intParam.value);
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if(humanObj.fightTeam == team){
				if(null == humanObj.humanMirrorObj) {
					return;
				}
				HumanMirror humanMir = humanObj.humanMirrorObj.getHumanMirror();
				EStanceType stance = EStanceType.valueOf(humanMir.getPartnerStance());
				if (null == stance) {
					Log.table.error("error in stance={},humanId={}", humanMir.getPartnerStance(), humanMir.getId());
					return;
				}
				int humanPos = humanObj.humanMirrorObj.getHumanPos();
				FightHumanObject fightHumanObj = new FightHumanObject(this);
				// 初始化主角战斗对象
				initHumanObj(fightHumanObj, humanMir, humanPos, team, stance);
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
					Log.logError(Log.fight,"属性错误，错误的初始属性：stage={},isHuman={},sn={},propName={}", 
							this, obj.isHuman(), obj.sn, key);
					continue;
				}
				ValueBase newValue = propValue.getCopy();
				newValue.setValue(ValueFactory.getFightValueByParam(value));
				obj.prop.addPropValue(key, newValue);
			}
		}
	}

	public void setFightObjProp(FightObject obj, Unit unit) {
		String jsonStr = UnitManager.inst().getPropJSON(unit);
		setFightObjProp(obj, jsonStr);
	}
	
	/**
	 * 队伍属性 * 系数
	 * @param teamType 队伍
	 * @param propName 属性
	 * @param mutiplyValue 系数值
	 */
	public void multiplyProp(ETeamType teamType, String[] propName, double[] mutiplyValue) {
		for (FightObject fightObj : fightObjs.values()) {
			if (fightObj.team != teamType) {
				continue;
			}
			for (int i = 0; i < propName.length; i++) {
				fightObj.prop.multiplyPropValue(propName[i], new ValueDouble(mutiplyValue[i]));
			}
		}
	}
	
	/**
	 * 设置主角或伙伴技能
	 * @param obj 角色
	 * @param installSkillJSON 主动技能 "sn,lv,power,value|sn,lv,power,value...."(,是1维|是2维；)(配表|是1维,是2维)
	 * @param installGodsJSON 爆点技能
	 * @param passiveSnStr 被动技能
	 */
	public void setFightObjSkill(FightObject obj, String installSkillJSON, String installGodsJSON, String passiveSnStr) {
		String[] skillStrAry = Utils.strToStrArraySplit(installSkillJSON);
		int[] skillAry = null;
		if (skillStrAry != null) { 
			for (String skillStr : skillStrAry) {
				skillAry = Utils.strToIntArray(skillStr);
				int sn = skillAry[0];
				int lv = skillAry[1];
				int power = skillAry[2];
				int value = skillAry[3];
				Skill normalSkill = Skill.newInstance(sn, lv, power, value, obj);
				if (normalSkill != null) {
					obj.commonSkillList.add(normalSkill);
				}
			}
		}
		
		String[] godsStrAry = Utils.strToStrArraySplit(installGodsJSON);
		if (godsStrAry != null) { 
			for (String godsStr : godsStrAry) {
				skillAry = Utils.strToIntArray(godsStr);
				int sn = skillAry[0];
				int lv = skillAry[1];
				int power = skillAry[2];
				int value = skillAry[3];
				obj.specialSkill = Skill.newInstance(sn, lv, power, value, obj);
			}
		}
		
		List<Integer> passiveSns = Utils.strToIntList(passiveSnStr);
		for (int sn : passiveSns) {
			Skill passiveSkill = Skill.newInstance(sn, 1, 0, 0, obj);
			if (passiveSkill != null) {
				obj.passiveSkillList.add(passiveSkill);
			}
		}
	}
	
	/**
	 * 设置怪物配表技能
	 * 
	 * @param obj
	 */
	public void setMonsterObjSkill(FightObject obj, ConfPartnerProperty conf) {
		int[] skillSns = conf.skill;
		if (null == skillSns) {
			return;
		}
		ConfSkill confSkill = null;
		int lv = 1;// 配表技能默认1级
		for (int sn : skillSns) {
			confSkill = ConfSkill.get(sn);
			if (null == confSkill) {
				Log.table.error("ConfSkill 配表错误，no find sn={}", sn);
				continue;
			}
			if (confSkill.active) {
				// 主动技能，包括：多个普通技能，单个特殊技能
				if (confSkill.type == SkillType.Special.value()) {
					obj.specialSkill = Skill.newInstance(sn, lv, 0, 0, obj);
				} else if (confSkill.type >= SkillType.Common.value()) {
					Skill normalSkill = Skill.newInstance(sn, lv, 0, 0, obj);
					if (normalSkill != null) {
						obj.commonSkillList.add(normalSkill);
					}
				}
			}
		}
		
		int[] passiveSns = null;
		if (conf.passiveSkills != null) {
			passiveSns = conf.passiveSkills;
		}
		if (conf.talent != null) {
			passiveSns = Utils.concatAll_Int(passiveSns, conf.talent);
		}
		if (passiveSns == null) {
			return;
		}
		for (int sn : passiveSns) {
			confSkill = ConfSkill.get(sn);
			if (null == confSkill) {
				Log.table.error("ConfSkill 配表错误，no find sn={}", sn);
				continue;
			}
			if (!confSkill.active) {
				// 被动技能
				Skill passiveSkill = Skill.newInstance(sn, lv, 0, 0, obj);
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
				Log.fight.error("玩家id={}的阵容里查无伙伴partnerId={}", humanMir.getId(), partnerId);
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
		
		// 保存场景中每个队伍的玩家镜像数据，供数据查询用
		teamHumanMirMap.put(team, humanMirrorObj);
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
		
		humanObj.humanId = hMir.getId();// 玩家ID
		humanObj.idFight = getFightId();// 战斗中分配的临时ID
		humanObj.sn = hMir.getSn();// 配表sn即PartnerProperty.sn
		humanObj.modelSn = hMir.getModelSn();// 模型sn
		humanObj.sex = hMir.getSex();// 性别：1男，2女
		humanObj.pos = pos;// 队中位置
		humanObj.team = team;// 所属战队
		humanObj.stance = stance;// 队伍站位
		humanObj.combat = hMir.getCombat();// 战斗力
		
		// 属性
		setFightObjProp(humanObj, hMir);
		// 当前血量同步
		PropManager.inst().restoreMaxHp(humanObj);
		// 技能
		setFightObjSkill(humanObj, hMir.getInstallSkillJSON(), hMir.getInstallGodsJSON(), hMir.getPassiveSkill());
		// 入场
		humanObj.stageRegister(this);
		// FIXME ..激活被动 调整到CombatObject.doStageOp中 
//		humanObj.startup();
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
		partnerObj.combat = pMir.getCombat();// 战斗力

		// 属性
		setFightObjProp(partnerObj, pMir);
		// 当前血量同步
		PropManager.inst().restoreMaxHp(partnerObj);
		// 技能
		setFightObjSkill(partnerObj, pMir.getInstallSkillJSON(), null, pMir.getPassiveSkill());
		// 入场
		partnerObj.stageRegister(this);
		// FIXME ..激活被动 调整到CombatObject.doStageOp中
//		partnerObj.startup();
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
		//monsterObj.combat = conf.combat// 战斗力
		
		// boss详情
		if (conf.roleType == EWorldObjectType.MonsterBoss_VALUE) {
			monsterObj.isShowBossInfo = true;
		}
		// 属性
		PartnerMirror mirror = new PartnerMirror();
		UnitManager.inst().setUnitProp(mirror, conf.propName, conf.propValue);
		setFightObjProp(monsterObj, mirror);
		// 战力计算
		monsterObj.combat = UnitManager.inst().calcCombatProp(mirror);
		
		// 当前血量同步
		PropManager.inst().restoreMaxHp(monsterObj);
		// 技能
		setMonsterObjSkill(monsterObj, conf);
		// 入场
		monsterObj.stageRegister(this);
		// FIXME ..激活被动 调整到CombatObject.doStageOp中 
//		monsterObj.startup();
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
	public final boolean checkHumanLoginOk() {
		if (this.isRecordFight) {
			return true;
		}
		// 录像回放
		if (this.isReplay) {
			if (mapStageObj.getHumanObjs().size() < 1) {
				return false;
			}
			for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
				if (!isCombatFightObj(humanObj.getHumanId())) {
					continue;
				}
				if (!humanObj.isClientStageReady) {
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
		if (mapStageObj.getHumanObjs().size() < 1) {
			return false;
		}
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if (!humanObj.isClientStageReady) {
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
	public final boolean checkHumanReadyOk() {
		if (this.isRecordFight) {
			return true;
		}
		if (this.isReplay) {
			if (humanReadyMap.size() == fightNum) {
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
		if (humanReadyMap.size() == fightNum) {
			return true;
		}
		return false;
	}

	/**
	 * 等待玩家确认出手结束
	 */
	public final void sendWaitHumanCheckRoundOrderEnd() {
		if (this.isQuickFight) {
			return;
		}
		humanReadyMap.clear();
		waitCheckTimer = new TickTimer(WAIT_CHECK_TIME);
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			SCTurnbasedRoundOrderEnd.Builder msg = SCTurnbasedRoundOrderEnd.newBuilder();
			humanObj.sendMsg(msg.build());
		}
	}
	
	/**
	 * 等待玩家确认回合结束
	 */
	public final void sendWaitHumanCheckRoundEnd() {
		if (this.isQuickFight) {
			return;
		}
		humanReadyMap.clear();
		waitCheckTimer = new TickTimer(WAIT_CHECK_TIME);
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			SCTurnbasedRoundEnd.Builder msg = SCTurnbasedRoundEnd.newBuilder();
			msg.setRound(this.round);
			humanObj.sendMsg(msg.build());
		}
	}

	/**
	 * 发送怒气技能等待队列
	 */
	public void sendSpecialOrderList(List<Integer> idFightList) {
		if (this.isQuickFight) {
			return;
		}
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			SCTurnbasedRageSkillWaitList.Builder msg = SCTurnbasedRageSkillWaitList.newBuilder();
			msg.addAllFighterId(idFightList);
			humanObj.sendMsg(msg.build());
		}
	}
	/**
	 * 发送怒气技能等待队列
	 */
	public void sendSpecialOrderRemoveList(List<Integer> idFightList) {
		if (this.isQuickFight) {
			return;
		}
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			SCTurnbasedRageSkillWaitList.Builder msg = SCTurnbasedRageSkillWaitList.newBuilder();
			msg.addAllRemoveId(idFightList);
			humanObj.sendMsg(msg.build());
		}
	}
	/**
	 * 发送怒气技能等待队列
	 */
	public void sendSpecialOrderList(int idFight) {
		if (this.isQuickFight) {
			return;
		}
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			SCTurnbasedRageSkillWaitList.Builder msg = SCTurnbasedRageSkillWaitList.newBuilder();
			msg.addFighterId(idFight);
			humanObj.sendMsg(msg.build());
		}
	}

	/**
	 * 判断玩家是否回合播放结束就绪
	 * 
	 * @return
	 */
	public final boolean checkHumanRoundEndOk() {
		if (this.isQuickFight) {
			return true;
		}
		if (waitCheckTimer == null) {
			return true;
		}
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			if (humanReadyMap.get(humanObj.id) == null) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 判断该玩家是否在该场战斗中，子类扩展此函数
	 * 适用于同一地图有多场战斗的情况
	 * @return 是否为该战斗的对象
	 */
	public boolean isCombatFightObj(long humanId) {
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
		this.fightRecord.setId(StagePort.applyId());
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
		for (HumanObject obj : this.mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
			HumanMirrorObject humanMirrorObj = obj.humanMirrorObj;
			if (null == humanMirrorObj) {
				continue;
			}
			HumanMirror humanMirror = humanMirrorObj.getHumanMirror();
			if (obj.fightTeam == ETeamType.Team1) {
				leftName = humanMirror.getName();
				leftCombat = humanMirror.getCombat();
				leftSn = humanMirror.getSn();
				leftAptitude = humanMirror.getAptitude();
			} else if (obj.fightTeam == ETeamType.Team2) {
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
			int hpCur = 0;
			int rageCur = 0;
			if (js.containsKey(JsonKey.Team)) {
				team = ETeamType.valueOf(js.getIntValue(JsonKey.Team));
			}
			if (js.containsKey(JsonKey.Pos)) {
				pos = js.getIntValue(JsonKey.Pos);
			}
			if (js.containsKey(JsonKey.HpCur)) {
				hpCur = js.getIntValue(JsonKey.HpCur);
			}
			if (js.containsKey(JsonKey.RageCur)) {
				rageCur = js.getIntValue(JsonKey.RageCur);
			}

			FightObject obj = this.getObjByPos(pos, team);
			if (obj == null) {
				continue;
			}
			DTurnbasedFinishObject.Builder objMsg = DTurnbasedFinishObject.newBuilder();
			objMsg.setId(obj.idFight);
			objMsg.setHpCur(hpCur);
			objMsg.setRageCur(rageCur);
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
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			JSONObject json = humanObj.humanMirrorObj.toFightJson();
			this.fightRecord.setLeftInfo(json.toJSONString());
			break;
		}
	}

	/**
	 * 录像/回放:自动战斗处理
	 */
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
		//录像记录
		if(this.needRecord){
			if(this.fightRecordSpecialOrderList.isEmpty()){
				return;
			}
			String key = JsonKey.SkillNum + this.fightRecordCastTimes;
			JSONArray castJson = new JSONArray();
			for(int i=0;i<this.fightRecordSpecialOrderList.size();i++){
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
	private void sendPlotMsg(FightParamPlot plotParam){
		if(this.isQuickFight){//跳过战斗不发送中间消息
			return;
		}
		for (HumanObject obj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
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
		if(this.isQuickFight){//跳过战斗不发送中间消息
			return;
		}	
		for (HumanObject obj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
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
		if(this.isQuickFight){//跳过战斗不发送中间消息
			return;
		}
		for (HumanObject obj : mapStageObj.getHumanObjs().values()) {
			if (!isCombatFightObj(obj.getHumanId())) {
				continue;
			}
			SCTurnbasedObjectLeave.Builder msg = SCTurnbasedObjectLeave.newBuilder();
			for(FightObject fighter:listParam.fighterList){
				msg.addFightObjList(fighter.createMsg());
			}
			obj.sendMsg(msg.build());
		}		
	}
}
