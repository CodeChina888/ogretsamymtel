package game.worldsrv.stage;

import game.worldsrv.character.WorldObject;
import game.worldsrv.config.ConfMap;
import game.worldsrv.support.Vector2D;
import game.turnbasedsrv.combat.CombatObject;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.HumanObjectService;
import game.worldsrv.character.MonsterObject;
import game.worldsrv.human.HumanManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.team.TeamData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitObject;

import org.apache.commons.lang3.exception.ExceptionUtils;

import core.CallPoint;
import core.Port;
import core.support.ConnectionStatus;
import core.support.Distr;
import core.support.TickTimer;
import core.support.Time;
import game.worldsrv.stage.StageCell;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.stage.StageObjectService;
import game.worldsrv.stage.StagePort;

/**
 * 普通场景：如主城等common类型的地图
 */
public class StageObject {
	public static long DESTROY_TIME = 100 * Time.SEC;//10 * Time.SEC;//sjh test
	
	public int mapSn = 0; // 地图配置SN（即confMap.sn）
	public int stageSn = 0; // 副本关卡SN（即confRepStage.sn，副本地图才有）
	public long stageId; // 地图实际ID（普通地图存的是mapSn，副本地图存的是分配的唯一ID）
	public ConfMap confMap; // 所属地图配置
	public double width = 9999D; // 地图的宽
	public double height = 9999D; // 地图的高
	
	public long createTime;//副本创建时间
	private long currTime; // 这个场景当前的时间， 当进行瞬间战斗计算的时候 这个时间会很神奇
	private long currLastTime; // 这个场景当前的时间， 当进行瞬间战斗计算的时候 这个时间会很神奇
	private int deltaTime; // 每帧时间差
	public StageRandomUtils randUtils = null; // 固定随机数系统
	
	public boolean isStart = false; // 是否已开启，只要有人进入就标识为开启
	public boolean isDestroy = false; // 是否正在摧毁副本
	public boolean isPass = false; // 是否已经通过副本
	
	public double cellWidth = 16; // 单元格宽16
	public double cellHeight = 9; // 单元格高9
	int w = 1; // 单元格总宽
	int h = 1; // 单元格总高
	
	public long LineId; // 地图分线的原始ID //如果是分线地图那么就是 SN 如果不是分线地图就是ID
	public int lineNum = 1; // 地图分线号

	public String mapName; // 地图名称
	
	// 用途：场景内只有单场战斗逻辑 （所有单人玩法战斗）
	public CombatObject combatObj = null;
	
	// 用途：场景内有多场战斗逻辑（多人抢夺本）
	public Map<Long, CombatObject> combatObjMap = new HashMap<>();
	// 记录单场景多场战斗，玩家对应的逻辑id <玩家id，战场id>
	public Map<Long, Long> humanCombatObjMap = new HashMap<>();

	private StagePort port; // 所属Port
	private StageCell[][] cells = null; // 该地图内所有地图块

	private Map<Long, WorldObject> worldObjs = new HashMap<>(); //该地图内所有单位
	private Map<Long, UnitObject> unitObjs = new HashMap<Long, UnitObject>();// 该地图内所有攻击单位
	private Map<Integer, List<HumanObject>> mapTeamHumanObj = new HashMap<>(); // 该地图内的各个队伍的人员，key=teamId
	private Map<Long, HumanMirrorObject> humanMirrorObjs = new HashMap<>(); // 该地图内所有镜像玩家，key=humanId
	private Map<Long, HumanObject> humanObjs = new HashMap<>(); // 该地图内所有玩家，key=humanId
	private Map<Long, Integer> humanRevive = new HashMap<>(); // 该地图内所有玩家的可复活次数，key=humanId
	private Map<Long, MonsterObject> monsterObjs = new HashMap<>(); // 该地图内所有怪物
	private Map<Long, PartnerObject> genObj = new HashMap<>();// 该地图内所有武将

	private TickTimer msgPulseTimer; // 控制广播发送频率

	//随机ID，在stageObject范围内，不持久化的对象，用于取ID，替代Port.applyId()方法
	private AtomicLong idAuto = new AtomicLong(1l);


	/**
	 * 创建对象
	 * @param port
	 * @param stageId 场景ID
	 * @param stageSn 关卡sn（非副本地图传0即可，副本地图传关卡sn）
	 * @param mapSn 地图sn
	 */
	public StageObject(StagePort port, long stageId, int stageSn, int mapSn) {
		this.confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn ={}", mapSn);
		}

		this.mapSn = mapSn;
		this.stageSn = stageSn;
		this.stageId = stageId;

		createTime = Port.getTime();
		currTime = Port.getTime();
		currLastTime = currTime;

		this.randUtils = new StageRandomUtils(Utils.I100);
		
		this.port = port;

		// 判断是否是需要分线的普通地图，分线地图返回stageId，非分线返回mapSn
		if (confMap.humanMaxNum > 0) {
			this.LineId = mapSn;
		} else {
			this.LineId = stageId;
		}
		
		this.mapName = confMap.name;
		this.msgPulseTimer = new TickTimer(33 * 3);// 消息发送间隔，每3帧发送一次
	}
	
	/**
	 * 开启场景副本，有人进入就开启
	 */
	public void start() {
		if (!this.isStart) {
			this.isStart = true;
		}
	}
	private void pulseWorldObjs(int deltaTime) {
		for (WorldObject wo : worldObjs.values()) {
			try {
				if (wo == null) {
					if (Log.stageCommon.isInfoEnabled()) {
						Log.stageCommon.info("执行地图心跳操作时发现下属WorldObj为空 worldObjs={}", worldObjs);
					}
					continue;
				}
				wo.pulse(deltaTime);
			} catch (Exception e) {
				// 特意吞掉异常 避免由于某个对象出错 造成全部操作都无法执行的问题
				// Log.stageCommon.error(ExceptionUtils.getStackTrace(e));
				e.printStackTrace();
			}
		}
	}
	public void pulse() {
		if (!isStart) {// 还未开启
			finallyPulse();
			return;
		}
		
		// 执行各单位本次心跳中的事务
		long curr = Port.getTime();
		this.currTime = curr;
		this.deltaTime += (int) (currTime - currLastTime);
		this.currLastTime = this.currTime;
		if (this.deltaTime >= 33) {
			pulseWorldObjs(this.deltaTime);
			this.deltaTime = 0;
		}
		// 执行战斗逻辑心跳
		if(combatObj != null) {
			combatObj.pulse();
		}
		
		// 执行场景多场战斗逻辑的心跳
		for (CombatObject combatObject : combatObjMap.values()) {
			combatObject.pulse();
		}
		
//		// 间隔不到就返回
//		if(!msgPulseTimer.isPeriod(Port.getTime())) 
//			return;
//		
//		// lock by sjh,不用这种方式发送广播消息了
//		long curr = Port.getTime();
//		// 执行各单位本次心跳中的事务
//		if (msgPulseTimer.isPeriod(curr)) {
//			// 心跳所有stageCell中的消息
//			for (int i = 0; i < h; i++) {
//				for (int j = 0; j < w; j++) {
//					cells[i][j].sendMsg();
//				}
//			}
//		}
	}
	private void finallyPulseWorldObjs(int deltaTime) {
		for(WorldObject wo : worldObjs.values()) {
			try {
				wo.finallyPulse(deltaTime);
			}catch (Exception e) {
				//特意吞掉异常 避免由于某个对象出错 造成全部操作都无法执行的问题
				Log.stageCommon.error("mapSn={}中{}帧频出现异常{}", this.mapSn, wo, ExceptionUtils.getStackTrace(e));
			}
		}
	}
	/**
	 * 返回当前时间
	 * @return
	 */
	public long getTime() {
		return currTime;
	}
	/**
	 * 无论如何都要执行的心跳处理，例如：场景未开启时必须执行该心跳，不必执行正常心跳
	 */
	public void finallyPulse() {
		long curr = Port.getTime();
		this.currTime = curr;
		this.deltaTime += (int)(currTime - currLastTime);
		this.currLastTime = this.currTime;
		if(this.deltaTime >= 33) {
			finallyPulseWorldObjs(deltaTime);
			this.deltaTime = 0;
		}
	}
	/**
	 * 地图初始化
	 */
	public void startup() {
		// 创建服务此stageobject的service
		StageObjectService serv = createService(port);
		serv.startupLocal();

		// 设置服务接口
		port.addService(serv);

		// 将地图进行全局注册
		StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
		prx.infoRegister(stageId, stageSn, mapSn, mapName, Distr.getNodeId(port.getId()), port.getId());

		// 将地图进行单元格分割，不读取表配置了，一个地图一个格子
		cellWidth = width;
		cellHeight = height;
		w = (int) (Math.ceil(width / cellWidth));
		h = (int) (Math.ceil(height / cellHeight));
//		Log.stageCommon.debug("===地图单元格：mapSn={},mapName={},w={},h={},width={},cellWidth={},height={},cellHeight={}",
//				 mapSn, mapName, w, h, width, cellWidth, height, cellHeight);
		cells = new StageCell[h][w];
		for (int j = 0; j < w; j++) {
			for (int i = 0; i < h; i++) {
				StageCell cell = new StageCell(mapSn, i, j);
				cells[i][j] = cell;
			}
		}

		createMonster();
	}
	/**
	 * 建立对应的service,stageObject的子类需要自己的 service的时候，可以覆盖此方法
	 */
	public StageObjectService createService(Port port) {
		// 设置服务接口
		return new StageObjectService(this, port);
	}
	public void createMonster() {
		// 生成怪物
		/*
		 * List<ConfMonsterBirth> confs =
		 * ConfMonsterBirth.findBy(ConfMonsterBirth.K.mapSn, stageSn); for
		 * (ConfMonsterBirth confMonsterBirth : confs) { //如果怪物群组为0
		 * if(confMonsterBirth.mapPointGroup == 0) {
		 * StageManager.inst().createObjByConf(this, confMonsterBirth); } }
		 */
	}

	/**
	 * 销毁地图
	 */
	public void destory() {
		if (this.isDestroy)
			return;
		
		// 删除服务接口
		port.delServiceBySafe(stageId);

		// 将地图从全局信息中删除
		StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
		prx.infoCancel(stageId);
		
		mapTeamHumanObj.clear();
		humanObjs.clear();
		humanRevive.clear();
		monsterObjs.clear();
		genObj.clear();
		
		this.isDestroy = true;
	}

	// public void stopStageObjTime(UnitObject unitObj, long time) {
	// //如果以前已经暂停了 那么现在当前时间等于暂停时间
	// if(this.stopTimeEnd > 0) {
	// time += this.currTime - this.stopTimeStart;
	// } else {
	// if(targetSpecialObjs.size() > 0)
	// targetSpecialObjs.clear();
	// }
	// targetSpecialObjs.put(unitObj.id, unitObj);
	//
	// this.stopTimeStart = this.currTime;
	// this.stopTimeEnd = this.currTime + time;
	// }
	// private void recoverStageObjTime() {
	// targetSpecialObjs.clear();
	// this.stopTimeStart = -1;
	// this.stopTimeEnd = -1;
	// }
	//
	// private void pulseStop(long curr) {
	// if(isStopping()) {
	// if(this.stopTimeEnd <= curr) {
	// recoverStageObjTime();
	// }
	// }
	// }
	//
	// public boolean isStopping() {
	// return (this.stopTimeEnd >= 0);
	// }
	
	public StagePort getPort() {
		return port;
	}

	public void login(long humanId, CallPoint connPoint, ConnectionStatus connStatus, long stageId, Vector2D stagePos) {
		// 玩家数据
		HumanObject humanObj = new HumanObject();
		humanObj.id = humanId;
		humanObj.connPoint = connPoint;
		humanObj.connStatus = connStatus;
		humanObj.stageObj = this;
		humanObj.posNow = stagePos;
		humanObj.teamBundleID = humanId;

		// 玩家登录游戏
		HumanManager.inst().loadHumanAllData(humanObj);
	}

	// public UnitObject getUnitObj(long unitId) {
	// UnitObject unitObj = getHumanObj(unitId);
	// if(unitObj == null) {
	// unitObj = getMonsterObj(unitId);
	// }
	// if(unitObj == null) {
	// unitObj = getGeneralObj(unitId);
	// }
	// return unitObj;
	// }
	//
	//
	// public Map<Long, UnitObject> getUnitObjs() {
	// Map<Long, UnitObject> units = new HashMap<>();
	//
	// units.putAll(humanObjs);
	// units.putAll(monsterObjs);
	// units.putAll(generalObjs);
	// return units;
	// }
	
	public UnitObject getUnitObj(long unitId) {
		return unitObjs.get(unitId);
	}

	public Map<Long, UnitObject> getUnitObjs() {
		return unitObjs;
	}

	public WorldObject getWorldObj(long objId) {
		return worldObjs.get(objId);
	}

	public Map<Long, WorldObject> getWorldObjs() {
		return worldObjs;
	}
	
	/**
	 * 获取玩家可复活次数
	 */
	public int getReviveCount(long humanId) {
		return humanRevive.get(humanId);
	}

	/**
	 * 设置玩家可复活次数
	 */
	public void setReviveCount(long humanId, int reviveCount) {
		humanRevive.put(humanId, reviveCount);
	}

	/**
	 * 获取玩家对象
	 * @param humanId
	 * @return
	 */
	public HumanObject getHumanObj(long humanId) {
		return humanObjs.get(humanId);
	}
	
	/**
	 * 获取玩家镜像对象
	 * @param humanId
	 * @return
	 */
	public HumanMirrorObject getHumanMirrorObj(long humanId) {
		return humanMirrorObjs.get(humanId);
	}
	
	/**
	 * 获取怪物对象
	 * @return
	 */
	public MonsterObject getMonsterObj(long monsterId) {
		return monsterObjs.get(monsterId);
	}

	public PartnerObject getGeneralObj(long genId){
		return genObj.get(genId);
	}
	/**
	 * 获取副本里的所有玩家的平均等级
	 * @return
	 */
	public int getLevelAVG() {
		int lv = 0;
		int num = 0;
		for (HumanObject humanObj : this.getHumanObjs().values()) {
			lv += humanObj.getHuman().getLevel();
			num++;
		}
		if (num != 0) {
			lv = lv / num;
		} else {
			Log.game.error("===getLevelAVG num=0");
		}
		if (lv < 1)
			lv = 1;
		return lv;
	}

	/**
	 * 获取场景里的队伍信息
	 * @return
	 */
	public Map<Integer, List<HumanObject>> getTeamHumanObj() {
		return mapTeamHumanObj;
	}

	public Map<Long, HumanObject> getHumanObjs() {
		return humanObjs;
	}
	
	public Map<Long, HumanMirrorObject> getHumanMirrorObjs() {
		return humanMirrorObjs;
	}
	
	public Map<Long, MonsterObject> getMonsterObjs() {
		return monsterObjs;
	}
	
	public Map<Long, PartnerObject> getGeneralObjs(){
		return genObj;
	}
	
	public int getMonsterNum() {
		return monsterObjs.size();
	}
	
	/**
	 * 添加地图单元 不要直接调用本函数 请使用WorldObject.stageRegister()
	 * @param obj
	 */
	public boolean _addWorldObj(WorldObject obj) {
		worldObjs.put(obj.id, obj);
		if (obj instanceof UnitObject) {
			unitObjs.put(obj.id, (UnitObject) obj);
		}
		/* 设置地图与活动单元的关系 */
		obj.stageObj = this;

		if (obj instanceof HumanObject) {// 记录玩家
			HumanObject ho = (HumanObject) obj;
			humanObjs.put(obj.id, ho);
			TeamData team = ho.getTeam();
			if (team != null) {// 有所属队伍信息，需要加入队伍容器供处理
				int teamId = team.teamId;
				List<HumanObject> listHumanObj = new ArrayList<>();
				if (mapTeamHumanObj.containsKey(teamId)) {
					listHumanObj = mapTeamHumanObj.get(teamId);
				}
				listHumanObj.add(ho);
				mapTeamHumanObj.put(teamId, listHumanObj);
			}

			if (stageSn > 0) {// 副本地图的话，重置复活次数
//				int reviveCount = 0;// 副本可复活次数
//				// 获得副本配置
//				ConfInstStage ConfInstStage = ConfInstStage.get(stageSn);
//				if (ConfInstStage != null) {
//					reviveCount = ConfInstStage.reviveCount;
//				} else {
//					Log.table.error("ConfInstStage配表错误，no find sn ={}", stageSn);
//				}
//				humanRevive.put(obj.id, reviveCount);
			}

			// 设置访问玩家服务接口
			HumanObjectService humanObjServ = new HumanObjectService(ho, port);
			humanObjServ.startup();
			port.addService(humanObjServ);
//		} else if (obj instanceof HumanMirrorObject) {// 记录玩家镜像
//			humanMirrorObjs.put(obj.id, (HumanMirrorObject) obj);
		} else if (obj instanceof MonsterObject) {// 记录怪物
			monsterObjs.put(obj.id, (MonsterObject) obj);
		} else if(obj instanceof PartnerObject){// 记录武将
			genObj.put(obj.id, (PartnerObject)obj);
		}

		/* 设置地图格与活动单元的关系 */
		StageCell cell = getCell(obj.posNow);
		if (null == cell) {
			return false;
		}
		obj.stageCell = cell;// getCell(obj.posNow);
		cell.addWorldObject(obj);

		// 日志
		if (Log.stageCommon.isDebugEnabled()) {
			// Log.stageCommon.debug("将活动单元注册至地图中：stage={}, obj={}", this, obj);
		}
		return true;
	}

	/**
	 * 删除地图单元 不要擅自调用这个接口 移除地图单元建议调用WorldObject.stageLeave()接口
	 * @param obj
	 */
	public void _delWorldObj(WorldObject obj) {
		worldObjs.remove(obj.id);
		if (obj instanceof UnitObject) {
			unitObjs.remove(obj.id);
		}
		/* 解除地图与活动单元的关系 */
		obj.stageObj = null;

		if (obj instanceof HumanObject) {// 删除玩家
			humanObjs.remove(obj.id);
			humanRevive.remove(obj.id);

			// 删除访问玩家服务接口
			// 只有当接口中的HumanObject与要删除的是同一内存对象时才进行删除
			// 否则由于清理的延迟操作，在切换地图等情况下会造成：
			// 标记为清理 -> 注册至地图 -> 进行真实清理（造成了service丢失的情况）
			HumanObjectService serv = port.getService(obj.id);
			if (serv != null && serv.getHumanObj() == obj) {
				port.delService(obj.id);
				
			}
//		} else if (obj instanceof HumanMirrorObject) {// 删除玩家镜像
//			humanMirrorObjs.remove(obj.id);
		} else if (obj instanceof MonsterObject) {// 删除怪物
			monsterObjs.remove(obj.id);
		} else if(obj instanceof PartnerObject){// 删除武将
			genObj.remove(obj.id);
		}

		/* 解除地图格与活动单元的关系 */
		if (null != obj.stageCell) {
			obj.stageCell.delWorldObject(obj);
			obj.stageCell = null;
		}

		// 日志
		if (Log.stageCommon.isDebugEnabled()) {
			// Log.stageCommon.debug("将活动单元从地图中删除：stage={}, obj={}", this, obj);
		}
	}

	public StageCell getCell(Vector2D pos) {
		StageCell cell = null;
		if (pos.isWrongPos()) {
//			Log.stageCommon.error("===StageObject.getCell has wrong pos={},mapSn={}", pos, mapSn);
		} else {
			int j = 0;//(int) Math.floor(pos.x / cellWidth);
			int i = 0;//(int) Math.floor(pos.y / cellHeight);
			cell = getCell(i, j);
			if (cell == null) {// 发现错误坐标
				Log.stageCommon.error("无法通过坐标获取到对应的地图Cell：mapSn={}, pos={}, starckTrace={}", mapSn, pos,
						ExceptionUtils.getStackTrace(new Throwable()));
			}
		}
		return cell;
	}

	public StageCell getCell(int i, int j) {
		if (cells == null) {
			Log.stageCommon.error("===getCell() error in 地图初始化startup还没完成就调用cells，问题很严重赶快去查！");
			return null;
		}
		if (i < 0 || j < 0 || i >= cells.length || j >= cells[0].length) {
			return null;
		}
		return cells[i][j];
	}

	public long getAutoId(){
		if(idAuto.get() == Long.MAX_VALUE)
			idAuto.set(0l);
		return idAuto.incrementAndGet();
	}
	
	/**
	 * 根据玩家id获得在场景的战斗对象
	 * @param humanId
	 * @return
	 */
	public CombatObject getCombatObj(long humanId) {
		if (combatObj != null) {
			return combatObj;
		}
		// 单场景多战斗：玩家对应的战斗id
		Long combatObjId = humanCombatObjMap.get(humanId);
		if (combatObjId != null) {
			return combatObjMap.get(combatObjId);
		}
		return null;
	}
	
}
