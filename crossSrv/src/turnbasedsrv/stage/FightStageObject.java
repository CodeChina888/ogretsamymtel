package turnbasedsrv.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.Port;
import core.support.Param;
import core.support.RandomMT19937;
import crosssrv.support.Log;
import game.msg.Define.DTurnbasedBuff;
import game.msg.Define.ETeamType;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfMap;
import game.worldsrv.config.ConfSceneEvent;
import turnbasedsrv.buff.BuffTriggerData;
import turnbasedsrv.enumType.StageOpType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.enumType.TriggerPoint;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.param.FightParamBase;
import turnbasedsrv.param.TriggerParam;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.skill.SkillCastData;
import turnbasedsrv.stageCond.StageCondBase;
import turnbasedsrv.stageCond.StageCondFactory;
import turnbasedsrv.stageEvent.StageEventBase;
import turnbasedsrv.trigger.Trigger;
import turnbasedsrv.trigger.TriggerCollection;
import turnbasedsrv.trigger.TriggerListen;

public abstract class FightStageObject {
	private static final int FlameTime = 33;// 帧时间
	public static final int FightPosAdd = 100;// 战场位置偏移值
	public static final int RoundMax = 20;// 最大回合数，>=max则进入结算
	public static final int CurePct = 8000;// 治疗的百分比

	/** 地图配置SN（即confMap.sn） **/
	public int mapSn = 0;
	/** 玩法配置sn **/
	public int stageSn = 0;
	/** 地图实际ID（普通地图存的是mapSn，副本地图存的是分配的唯一ID） **/
	public long stageId;
	/** 固定随机数系统 **/
	public RandomMT19937 randUtils = null;
	/** 随机数种子 **/
	public long randSeed = 100;
	
	/** 触发器 **/
	public TriggerCollection triggerManager;
	/** 战斗阶段 **/
	public StageStep stageStep;
	/** 战斗事件 **/
	public StageEventBase stageEvent;
	/** 战斗回合数 **/
	public int round = 0;
	/** 战斗最大回合数 **/
	public int roundMax = RoundMax;
	/** 是否自动战斗 **/
	public Map<ETeamType, Boolean> autoFightMap = new HashMap<>();
	/** 加速倍率 **/
	public float speedTimes = 1;
	/** 优先出手的战队 **/
	public ETeamType priorTeam = ETeamType.Team1;
	/** 出手顺序 **/
	public List<Integer> orderList = new ArrayList<>();
	/** 已出手顺序 **/
	public List<Integer> lastOrderList = new ArrayList<>();
	/** 怒气出手列表 **/
	public List<Integer> specialOrderList = new ArrayList<>();
	/** 怒气已出手列表 **/
	public List<Integer> lastSpecialOrderList = new ArrayList<>();
	/** 胜利所属战队 **/
	public ETeamType winTeam = ETeamType.Team0;
	/** 是否全是胜利 **/
	public boolean isAlwaysWin = false;
	/** 当前出手主动技的战斗者位置 **/
	public int nowOrderPosition = 0;
	/** 当前出手的主动技能是否是特殊技能 **/
	public boolean nowOrderIsSpecial = false;
	
	/** 特殊参数：用于具体副本地图记录战斗过程中的数据，如副本星数等 **/
	public Param exParam = new Param();
	/** 是否是录像回放 **/
	public boolean isReplay = false;
	/** 是否需要录像 **/
	public boolean needRecord = false;
	/** 是否结束 **/
	public boolean isEnd = false;
	/** 是否是暂停状态 **/
	public boolean isStop = false;
	/** 是否可以暂停 **/
	public boolean canStop = true;
	/** 是否跳过战斗 **/
	public boolean isQuickFight = false;
	/** 是否可以跳过战斗 **/
	public boolean canQuickFight = false;
	/** 显示boss详情的怪物id **/
	public int bossInfoId = -1;
	
	/** 所属地图配置 **/
	public ConfMap confMap = null;
	/** 玩法配置 **/
	public ConfInstStage confInstStage = null;
	/** 场景怪物配置 **/
	public ConfInstMonster confInstMonster = null;
	/** 所属port **/
	public Port port;
	/** 副本创建时间 **/
	public long createTime;
	/** 这个场景当前的时间 **/
	private long currTime;
	/** 这个场景上个节点的时间 **/
	private long currLastTime;
	/** 距离上个帧结束的时间 **/
	private int deltaTime;

	/** 该地图内所有战斗单位 **/
	protected Map<Integer, FightObject> fightObjs = new HashMap<>();
	/** 战斗单位站位 <fightPos, obj> **/
	private Map<Integer, FightObject> posFightObjs = new HashMap<>();
	
	/** 场景阶段行为 **/
	private Map<StageStep, List<StageCondBase>> stepActionMap = new HashMap<>();
	/** 场景阶段前置行为 **/
	private Map<StageStep, List<StageCondBase>> stepBeforeActionMap = new HashMap<>();
	/** 场景阶段结束检测 **/
	private Map<StageStep, List<StageCondBase>> stepFinishCheckMap = new HashMap<>();
	/** 特殊操作相关的处理 **/
	private Map<StageOpType, List<StageCondBase>> opActionMap = new HashMap<>();
	/** 场景结束检测 **/
	private List<StageCondBase> stageFinishCheckList = new ArrayList<>();

	/** 事件列表 **/
	public List<StageEventBase> eventList = new ArrayList<>();
	/** 监听列表 **/
	public List<TriggerListen> stepListenList = new ArrayList<>();
	/** 全局监听列表 **/
	public List<TriggerListen> listenList = new ArrayList<>();
	
	/** 战斗对象ID **/
	private int fightId = 0;
	/** 暂停时间 **/
	private long stopTime = 0;
	
	/** 战斗阶段当前数 **/
	public int step = 0;
	public int stepAll = 0;
	/** scene列表 **/
	public List<Integer> sceneList = new ArrayList<>();
	
	/**
	 * 构造函数
	 * 
	 * @param port
	 * @param stageId
	 * @param repSn
	 * @param mapSn
	 */
	public FightStageObject(Port port, long stageId, int stageSn, int mapSn) {
		this.triggerManager = new TriggerCollection(this);
		this.port = port;
		this.stageId = stageId;
		this.stageSn = stageSn;
		this.mapSn = mapSn;
		this.confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.fight.error("ConfMap配表错误，no find sn={}", mapSn);
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

		createTime = Port.getTime();
		currTime = Port.getTime();
		currLastTime = currTime;

		init();
	}
	
	/** 是否是最后一个场景 **/
	public boolean isLastScene() {
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
	 * 获取port
	 * 
	 * @return
	 */
	public Port getPort() {
		return port;
	}

	/**
	 * 增加特殊技能列表
	 * 
	 * @param pos
	 */
	public void addSpecialList(int pos) {
		this.specialOrderList.add(pos);
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
		this.randUtils = new RandomMT19937(this.randSeed);
	}

	/**
	 * 初始化
	 */
	public void init() {
		setRandUtils();// 设置随机数种子
		initNextScene();// 初始化下个场景
		this.stageStep = StageStep.StageInit;
		this.setStageStep(StageStep.StageWaitFighter);
	}
	
	/**
	 * 初始化下个场景
	 */
	public void initNextScene() {
		stepActionMap.clear();
		stepBeforeActionMap.clear();
		stepFinishCheckMap.clear();
		opActionMap.clear();
		stageFinishCheckList.clear();
		
		if(this.sceneList.size() > this.step) {
			this.confInstMonster = ConfInstMonster.get(this.sceneList.get(this.step));
		} else {
			this.confInstMonster = null;
		}
		if(this.confInstMonster == null) {
			Log.fight.error("confInstMonster配表错误，no find next Scene,stageSn={},step={}",this.stageSn,this.step);
		} else {
			if (this.confInstMonster.eventIDs == null) {
				Log.table.error("confInstMonster配表错误 eventIDs=null，sn={}", confInstMonster.sn);
			} else {
				// 初始化所有场景事件
				for (int id : this.confInstMonster.eventIDs) {
					ConfSceneEvent confEvent = ConfSceneEvent.get(id);
					if (confEvent == null) {
						Log.table.error("confEvent配表错误，no find sn={},stageSn={}", id, this.stageSn);
						continue;
					}
					StageCondBase action = StageCondFactory.getStageAction(id, confEvent.type, confEvent.param);
					action.init(this);
				}			
			}
		}
	}
	
	public void removeAction(StageCondBase action) {
		List<StageStep> keyList = new ArrayList<>();
		keyList.addAll(stepActionMap.keySet());
		for(StageStep key : keyList) {
			List<StageCondBase> actionList = stepActionMap.get(key);
			if (actionList != null && actionList.contains(action)) {
				actionList.remove(action);
				if(actionList.isEmpty()) {
					stepActionMap.remove(key);
				}
			}
		}
		
		keyList.clear();
		keyList.addAll(stepBeforeActionMap.keySet());
		for(StageStep key : keyList) {
			List<StageCondBase> actionList = stepBeforeActionMap.get(key);
			if (actionList != null && actionList.contains(action)) {
				actionList.remove(action);
				if(actionList.isEmpty()) {
					stepBeforeActionMap.remove(key);
				}
			}
		}
		
		keyList.clear();
		keyList.addAll(stepFinishCheckMap.keySet());
		for(StageStep key : keyList) {
			List<StageCondBase> actionList = stepFinishCheckMap.get(key);
			if (actionList != null && actionList.contains(action)) {
				actionList.remove(action);
				if(actionList.isEmpty()) {
					stepFinishCheckMap.remove(key);
				}
			}
		}
		

		List<StageOpType> opKeyList = new ArrayList<>();
		opKeyList.addAll(opActionMap.keySet());
		for(StageOpType key:opKeyList) {
			List<StageCondBase> actionList = opActionMap.get(key);
			if (actionList != null && actionList.contains(action)) {
				actionList.remove(action);
				if(actionList.isEmpty()) {
					opActionMap.remove(key);
				}
			}
		}
		
		if(stageFinishCheckList.contains(action)) {
			stageFinishCheckList.remove(action);
		}
	}

	/**
	 * 增加场景阶段行为
	 * 
	 * @param step
	 * @param action
	 */
	public void addStepAction(StageStep step, StageCondBase action) {
		List<StageCondBase> actionList = stepActionMap.get(step);
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
	public void addStepBeforeAction(StageStep step, StageCondBase action) {
		List<StageCondBase> actionList = stepBeforeActionMap.get(step);
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
	public void addStepFinishCheck(StageStep step, StageCondBase action) {
		List<StageCondBase> actionList = stepFinishCheckMap.get(step);
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
	public void addStageOpAction(StageOpType op, StageCondBase action) {
		List<StageCondBase> actionList = opActionMap.get(op);
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
	public void removeStageOpAction(StageOpType op, StageCondBase action) {
		List<StageCondBase> actionList = opActionMap.get(op);
		if (actionList != null && actionList.contains(action)) {
			actionList.remove(action);
		}
	}
	
	/**
	 * 增加场景结束检测
	 * 
	 * @param action
	 */
	public void addStageFinishCheck(StageCondBase action) {
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
	 * 设置场景阶段
	 * 
	 * @param stageStep
	 */
	public void setStageStep(StageStep stageStep) {
		this.stageStep = stageStep;

		// 执行场景阶段前置行为
		List<StageCondBase> beforeList = stepBeforeActionMap.get(this.stageStep);
		if (beforeList != null) {
			for (int i = 0; i < beforeList.size(); i++) {
				beforeList.get(i).doStepActionBefore(this);
			}
		}
		// 检测场景阶段触发器
		checkStepTrigger(stageStep);

		// 执行场景阶段行为
		List<StageCondBase> actionList = stepActionMap.get(this.stageStep);
		if (actionList != null) {
			for (int i = 0; i < actionList.size(); i++) {
				actionList.get(i).doStepAction(this);
			}
		}
		// 是否场景结束
		if (this.stageStep == StageStep.StageEnd) {
			this.finishFight();
		}
	}

	/**
	 * 检测场景阶段触发器
	 * 
	 * @param stageStep
	 */
	private void checkStepTrigger(StageStep stageStep) {
		// 根据场景阶段设置触发阶段
		TriggerPoint triggerPoint = null;
		switch (stageStep) {
		case StageStart:
			triggerPoint = TriggerPoint.StageStart;
			break;
		case StageStepStart:
			triggerPoint = TriggerPoint.StageStepStart;
			break;
		case StageStepEnd:
			triggerPoint = TriggerPoint.StageStepEnd;
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
			this.triggerManager.doTrigger();
		}
	}

	/**
	 * 无论如何都要执行的心跳处理，例如：场景未开启时必须执行该心跳，不必执行正常心跳
	 */
	public void finallyPulse() {
		if (isStop) {
			pulseStop();
			return;
		}
		long curr = Port.getTime();
		this.currTime = curr;
		this.deltaTime += (int) (currTime - currLastTime);
		this.currLastTime = this.currTime;
		if (this.deltaTime >= FightStageObject.FlameTime) {
			pulseStageEvent(this.deltaTime);
			this.deltaTime = 0;
		}
	}

	/**
	 * 正常心跳
	 */
	public void pulse() {
		if (isEnd) {
			return;
		}
		if (isStop) {
			pulseStop();
			return;
		}
		// 执行各单位本次心跳中的事务
		long curr = Port.getTime();
		this.currTime = curr;
		this.deltaTime += (int) (currTime - currLastTime);
		this.currLastTime = this.currTime;
		if (this.deltaTime >= FightStageObject.FlameTime) {
			pulseStageEvent(this.deltaTime);
			pulseStageStep();
			this.deltaTime = 0;
		}
	}

	private void pulseStop() {
		long curr = Port.getTime();
		this.currTime = curr;
		this.deltaTime += (int) (currTime - currLastTime);
		this.currLastTime = this.currTime;
		if (this.deltaTime >= FightStageObject.FlameTime) {
			stopTime = stopTime + deltaTime;
			this.deltaTime = 0;
		}
	}

	protected void pulseStageEvent(int deltaTime) {
		int leftTime = (int) (deltaTime * this.speedTimes);
		if (this.stageEvent != null) {
			StageEventBase event = this.stageEvent;
			leftTime = this.stageEvent.pulse(this, leftTime);
			while (this.stageEvent != null && event != this.stageEvent && leftTime > 0) {
				event = this.stageEvent;
				leftTime = this.stageEvent.pulse(this, leftTime);
			}
		}
	}

	public void removeStageEvent(StageEventBase stageEvent) {
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

	public void addStageEvent(StageEventBase stageEvent) {
		if (this.stageEvent == null) {
			this.stageEvent = stageEvent;
		} else {
			this.eventList.add(stageEvent);
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
		} else {
			if (this.stageEvent != null ) {
				return;
			}
		}
		// 检测场景是否结束
		for (int i = 0; i < stageFinishCheckList.size(); i++) {
			if (stageFinishCheckList.get(i).checkStageFinish(this)) {
				this.setStageStep(StageStep.StageEnd);
				return;
			}
		}
		// 检测场景阶段是否结束
		List<StageCondBase> actionList = stepFinishCheckMap.get(this.stageStep);
		if (actionList != null) {
			for (int i = 0; i < actionList.size(); i++) {
				if (!actionList.get(i).checkStepFinish(this)) {
					return;
				}
			}
		}
		// 检测是否切换至下个场景阶段
		List<StageCondBase> checkActionList = stepFinishCheckMap.get(this.stageStep);
		if (checkActionList != null) {
			StageStep oldStep = this.stageStep;
			for (int i = 0; i < checkActionList.size(); i++) {
				checkActionList.get(i).setNextStep(this, oldStep);
				if (this.stageStep != oldStep) {
					break;
				}
			}
		}
	}
	
	/**
	 * 是否存在指定模型sn的对象
	 * @return
	 */
	public boolean isExistRelation(int[] modelSns) {
		int num = 0;
		for (int modelSn : modelSns) {
			for (FightObject obj : fightObjs.values()) {
				if (obj.modelSn == modelSn) {
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
	 * @param fightPos战场位置
	 * @return
	 */
	public FightObject getObjByFightPos(int fightPos) {
		return posFightObjs.get(fightPos);
	}

	/**
	 * 获得指定位置的对象
	 * 
	 * @param pos队伍位置
	 * @param team所在战队
	 * @return
	 */
	public FightObject getObjByPos(int pos, ETeamType team) {
		// 1号战队即左下方，队中位置：0-9，战场位置：100-109
		// 2号战队即右上方，队中位置：0-9，战场位置：0-9
		if (team == ETeamType.Team1) {
			return posFightObjs.get(pos + FightStageObject.FightPosAdd);
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
	 * 发送回合数变更消息
	 */
	public abstract void sendRoundChangeInfoMsg(List<DTurnbasedBuff> buffRemoveList);

	/**
	 * 发送技能施法消息
	 * 
	 * @param skillData
	 */
	public abstract void sendCastSkillInfoMsg(SkillCastData skillData);

	/**
	 * 发送buff生效消息
	 * 
	 * @param buffData
	 * @param isRemove
	 */
	public abstract void sendBuffTriggerInfoMsg(BuffTriggerData buffData, boolean isRemove);

	/**
	 * 战斗暂停
	 */
	public abstract void stopFight(boolean isStop, int tmStop, int idFight);

	/**
	 * 战斗结束
	 */
	public abstract void finishFight();

	/**
	 * 等待玩家确认回合结束
	 */
	public abstract void sendWaitHumanCheckRoundEnd();

	/**
	 * 发送特殊技能等待队列
	 */
	public abstract void sendSpecialOrderList(List<Integer> idFightList);

	/**
	 * 发送特殊技能等待队列
	 */
	public abstract void sendSpecialOrderList(int idFight);

	/**
	 * 判断登录的玩家是否达到要求
	 * 
	 * @return
	 */
	public abstract boolean checkHumanLoginOk();

	/**
	 * 判断玩家是否准备就绪
	 * 
	 * @return
	 */
	public abstract boolean checkHumanReadyOk();

	/**
	 * 判断玩家是否回合播放结束就绪
	 * 
	 * @return
	 */
	public abstract boolean checkHumanRoundEndOk();

	/**
	 * 录像回放时自动战斗处理
	 */
	public abstract void checkAutoFightReplay();

	/**
	 * 录像回放时施放技能处理
	 */
	public abstract void checkCastSkillReplay();
	
	/**
	 * 获取结果
	 * @param type
	 * @param param
	 */
	public FightParamBase doStageOp(StageOpType type, FightParamBase param) {
		if(opActionMap.containsKey(type)) {
			List<StageCondBase> list = opActionMap.get(type);
			List<StageCondBase> excuteList = new ArrayList<>();
			excuteList.addAll(list);
			for(StageCondBase action : excuteList) {
				if(action.isValid) {
					FightParamBase result = action.doStageOp(this, type, param);
					if(result != null) {
						return result;
					}
				}
			}			
		}
		switch(type) {
		}
		return null;
	}
	
	/**
	 * 判定操作
	 * @param type
	 * @param param
	 */
	public boolean checkStageOp(StageOpType type, FightParamBase param) {
		switch(type) {
		
		}
		return false;
	}
}
