package game.worldsrv.character;

import game.msg.MsgCommon.SCStageObjectInfoChange;
import game.msg.MsgStage.SCStageMove;
import game.msg.MsgStage.SCStageMoveStop;
import game.msg.MsgStage.SCStageObjectDisappear;
import game.worldsrv.config.ConfRoleModel;
import game.worldsrv.entity.Buff;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Unit;
import game.worldsrv.enumType.DisappearType;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Running;
import game.worldsrv.support.Utils;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.Vector3D;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.PortPulseQueue;
import core.support.Param;

/**
 * 角色基类 包含移动、战斗等
 */
public abstract class UnitObject extends WorldObject {
	public UnitDataPersistance dataPers = new UnitDataPersistance();
	public PropCalcCommon propsSum = new PropCalcCommon(); //总属性
	public Map<String, PropCalcCommon> props = new HashMap<>(); //功能块属性缓存 key:EntityUnitPropPlus.name
	public ConfRoleModel confModel = null; // 模型相关配置
	public Running running = new Running(this); // 玩家移动信息

	public boolean canMove = true; // 是否可以移动
	public boolean canAttack = true; // 是否可以普攻
	public boolean canCastSkill = true; // 是否可以施法
	
	public boolean castSkilling = false; // 是否在施法中
	public boolean charming = false; // 是否被魅惑中
	
	public boolean pause = false; //暂停，（目前状态，一个特殊的字段,比如副本结束的话设定暂停就不再索敌,也不再攻击）
	
	// 保存出生位置
	public Vector2D posBegin = new Vector2D();// 出生位置
	// 保存出生朝向
	public Vector2D dirBegin = new Vector2D();// 出生朝向

	// 从属关系
	public HumanObject parentObject = null;
	public long teamBundleID = -1; // 敌我标志：-1敌方，0中立，1友方
	public int killCount = 0;// 连杀数，死亡时重置为0

	public List<UnitObject> beAttacked = new ArrayList<>(); // 被攻击者列表

	public int order; // 站位顺序
	public int index = 0; // 当前波数的位置索引
	public int matrixIndex; // 阵型站位索引
	public int entryIndex; // 战斗群 按order的排序
	public int profession; // 职业
	
	
	public UnitObject(StageObject stageObj) {
		super(stageObj);
	}

	@Override
	public void startup() {
		// 一些初始化工作
		posNow.x = posBegin.x;
		posNow.y = posBegin.y;

		dirNow.x = dirBegin.x;
		dirNow.y = dirBegin.y;

		if (getUnit() != null) {
			profession = getUnit().getProfession();// 职业
			// skillTempInfo.rageValue = getUnit().getRage();//怒气
		}
	}

	public boolean isHumanObj() {
		return this instanceof HumanObject;
	}

	public boolean isMonsterObj() {
		return this instanceof MonsterObject;
	}

	public boolean isPartnerObj() {
		return this instanceof PartnerObject;
	}

	/**
	 * 获取归属主人Id
	 */
	public long getHumanId() {
		long id = -1;
		if (isHumanObj()) {
			id = this.id;
		} else if (isPartnerObj() && this.parentObject != null) {
			id = parentObject.id;
		}
		return id;
	}

	/**
	 * 获取归属主人
	 */
	public HumanObject getHumanObj() {
		if (isHumanObj()) {
			return (HumanObject) this;
		} else if (isPartnerObj() && this.parentObject != null) {
			// 武将，返回归属主人
			return (HumanObject) parentObject;
		}
		return null;
	}

	@Override
	public void pulse(int deltaTime) {
		super.pulse(deltaTime);

		// 单元移动了
		pulseMove(tmCurr);
	}

	/**
	 * 地图单元移动
	 * @param timeCurr 当前时间
	 */
	public void pulseMove(long timeCurr) {
		if (!isInWorld())
			return;
		if (!running.isRunning())
			return;
		// 单元移动了
		running._pulse(timeCurr);
	}
		
	/**
	 * 强制单元停止
	 */
	public void stop() {
		if (!running.isRunning())
			return;

		// if(this.isHumanObj()) {
		// Log.stageMove.info("stop角色{}id{}到达最终目标{}，停止移动。", name, id, posNow);
		// } else if(this.isGeneralObj()) {
		// Log.stageMove.info("stop武将{}id{}到达最终目标{}，停止移动。", name, id, posNow);
		// }
		// else {
		// Log.stageMove.info("stop怪物{}id{}到达最终目标{}，停止移动。", name, id, posNow);
		// }

		// 强制同步移动
		// this.running._pulse(this.getTime(), true);//lock by
		// sjh,不修正坐标，以免导致位置不同步，影响技能释放范围判断

		// 停止移动
		running._stop();

		// 广播移动停止消息
		// if(this.stageObj.confMap.sendMove) {//当前地图是允许广播移动的话才广播
		SCStageMoveStop.Builder msgStop = SCStageMoveStop.newBuilder();
		msgStop.setObjId(id);
		msgStop.setPosEnd(posNow.toMsg());
		msgStop.setDirEnd(dirNow.toMsg());
		StageManager.inst().sendMsgToArea(msgStop, stageObj, posNow);

	}

	/**
	 * 地图单元移动
	 * @param posFrom 起始点
	 * @param paths 路径点
	 * @param dirClient 客户端的朝向
	 */
	public void move(Vector3D posFrom, List<Vector3D> paths, Vector3D dirClient) {
		if (!isInWorld())
			return;// 不在地图中
		if (paths.isEmpty())
			return;// 路径点为空
		if (getUnit().getSpeed() <= 0) {
			Log.stageMove.error("===error in 不会移动的object开始移动：id={},name={},posFrom={},posTo[0]={}", id, this.name,
					posFrom.toString(), paths.get(0).toString());
			return;// 不会移动，没有配置移动速度
		}
		if (!canMove)
			return;// 处于不可移动状态，受到晕眩等控制

		// 移动过于频繁，忽略这次消息
		// if (fromClient && !running.isTimeExpired()) {
		// return;
		// }
		// if(posFrom.z == 0) {
		// posFrom.z = 0;
		// }
		// 修正起点
		// if (isHumanObj()) {
		// posFrom.set(running.correctPosFrom(posFrom));
		// }

		// 修正所有点，如果连续两个点相同，则移出后一个
		Vector3D pos = new Vector3D();
		pos.set(posFrom);
		Iterator<Vector3D> it = paths.iterator();
		while (it.hasNext()) {
			Vector3D posNext = it.next();
			if (pos.distance(posNext) < 0.01) {
				it.remove();
				continue;
			}
			pos.set(posNext);
		}
		if (paths.isEmpty())
			return;// 路径点为空

		if (this.isHumanObj()) {
			Event.fire(EventKey.HumanMoveStartBefore, "humanObj", this);
			Event.fire(EventKey.HumanActionBefore, "humanObj", this);
		}
		
		// 移动
		running._move(posFrom, paths, 1.0D * getUnit().getSpeed());

		// if(this.isHumanObj()) {
		// Log.temp.info("玩家地图单元({})id{}开始移动，起始位置{}，接下来的目标为{}。", this.name,
		// this.id,
		// posFrom.getPosStr(), running.getRunPathMsg().get(0));
		// } else if(this.isGeneralObj()) {
		// Log.temp.info("武将地图单元({})id{}开始移动，起始位置{}，接下来的目标为{}。", this.name,
		// this.id,
		// posFrom.getPosStr(), running.getRunPathMsg().get(0));
		// }
		// else {
		// Log.temp.info("怪物地图单元({})id{}开始移动，起始位置{}，接下来的目标为{}。", this.name,
		// this.id,
		// posFrom.getPosStr(), running.getRunPathMsg().get(0));
		// }

		// 广播移动消息
		// if(this.stageObj.confMap.sendMove) {//当前地图是允许广播移动的话才广播
		SCStageMove.Builder move = SCStageMove.newBuilder();
		move.setObjId(id);
		move.setPosBegin(posFrom.toMsg());
		move.addAllPosEnd(running.getRunPathMsg());
		if (dirClient != null) {
			move.setDir(dirClient.toMsg());// 客户端请求移动，则用客户端的朝向
		} else {
			move.setDir(running.getDirV3D().toMsg());// 非客户端请求移动，则用服务端的朝向
		}
//		System.out.println(posFrom.getPosStr());
		StageManager.inst().sendMsgToArea(move, stageObj, posNow);

		// 抛出开始移动的事件
//		Event.fire(EventKey.UnitMoveStart, "unitObj", this);
//		Event.fire(EventKey.UnitAction, "unitObj", this);
//		if (this.isHumanObj()) {
//			Event.fire(EventKey.HumanMoveStart, "humanObj", this);
//			Event.fire(EventKey.HumanAction, "humanObj", this);
//		} else {
//			Event.fire(EventKey.MonsterMoveStart, "monsterObj", this);
//			Event.fire(EventKey.MonsterAction, "monsterObj", this);
//		}
		// 记录日志
		if (this.isHumanObj()) {
			if (Log.stageMove.isDebugEnabled()) {
				Log.stageMove.debug("角色({} {})开始移动，起始位置{}，接下来的目标为{}。",
						this.name, this.id,posFrom.getPosStr(),
						running.getRunPathMsg());
			}
		 } else {
			 if (Log.stageMove.isDebugEnabled()) {
				 Log.stageMove.debug("地图单元({})开始移动，起始位置{}，接下来的目标为{}。", this.name,
						 posFrom.getPosStr(), running.getRunPathMsg());
			 }
		 }

	}

	public void die(UnitObject uoKill, Param params) {
		// 将具体删除操作排入队列 在心跳的最后在进行删除
		// 因为本心跳中可能还有后续操作需要本对象的实例
		if (stageObj == null || uoKill == null)
			return;

		Unit unit = getUnit();
		unit.setHpCur(0);

		// 设置状态
		// inWorld = false;

		// 停止移动
		stop();

		Event.fireEx(EventKey.UnitBeKilled, stageObj.mapSn, "uoKill", uoKill, "uoDie", this, "skillSn",
				params.get("skillSn"));

		// 设置状态
		this.inWorld = false;
		Log.stageCommon.info("===sjh测试死亡消失 die() name={},inWorld={}", this.name,inWorld);
		this.killCount = 0;// 死亡者的连杀数重置为0
		uoKill.killCount += 1;// 杀人者的连杀数+1

		// 通知其他玩家 有地图单元离开视野
		StageManager.inst().sendMsgToArea(this.createMsgDie(uoKill, params), stageObj, this.posNow);

		if (this.isMonsterObj()) {// 怪物死亡从场景里删除，人物和武将的死亡不从场景里删除，因为可以复活。
			stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "unitObj", this) {
				public void execute(Port port) {
					StageObject stageObj = param.get("stageObj");
					UnitObject unitObj = param.get("unitObj");
					if (stageObj != null && unitObj != null) {
						stageObj._delWorldObj(unitObj);
					}
				}
			});
		}
	}
		
	public boolean isDie() {
		boolean ret = true;
		if (this.isInWorld()) {// 在地图中且HPCur>0才算活的；不在地图中，可能还没出生也认为是死的
			if (getUnit().getHpCur() > 0)
				ret = false;
		}
		return ret;
	}

	public Unit getUnit() {
		return dataPers.unit;
	}

	public Map<Integer, Buff> getBuffs() {
		return dataPers.buffs;
	}

	@Deprecated
	public PropCalcCommon getPropPlus() {
		PropCalcCommon propComm = new PropCalcCommon();
		UnitPropPlusMap unitPropPlus = dataPers.unitPropPlus;
		// 遍历加成属性来累加数据
		for (EntityUnitPropPlus type : EntityUnitPropPlus.values()) {
			if (type.name().equals("Type") || type.name().equals("Name")) {
				continue;// 类型和名字不属于属性 需要排除
			}
			propComm.add(unitPropPlus.getFrom(type));
		}
		return propComm;
	}
	
	public String getSkills() {
		return dataPers.unit.getSkillAllSn();
	}

	// public String getInborns() {
	// return dataPers.unit.getInborn();
	// }

	public double nextDouble() {
		if (this.stageObj == null || this.stageObj.randUtils == null) {
			Log.game.info("Error nextDouble （stageObj == null OR stageObj.randUtils == null）");
			return 0;
		}

		return this.stageObj.randUtils.nextDouble();
	}

	public int nextInt(int range) {
		if (this.stageObj == null || this.stageObj.randUtils == null) {
			Log.game.info("Error nextInt （stageObj == null OR stageObj.randUtils == null）");
			return 0;
		}
		return this.stageObj.randUtils.nextInt(range);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		out.write(teamBundleID);
		out.write(order);
		out.write(propsSum);
		out.write(props);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		teamBundleID = in.read();
		order = in.read();
		propsSum = in.read();
		props = in.read();
	}

	public SCStageObjectInfoChange.Builder createMsgUpdate() {
		SCStageObjectInfoChange.Builder infoChange = SCStageObjectInfoChange.newBuilder();
		infoChange.setObj(createMsg());

		return infoChange;
	}

	public SCStageObjectDisappear.Builder createMsgDie(UnitObject killer, Param params) {
		Param param = new Param(params);

		long killerId = 0;
		String killerName = "";
		int killCount = 0;// 连杀数
		int skillSn = Utils.getParamValue(param, "skillSn", 0);
		if (killer != null) {
			killerId = killer.id;
			killerName = killer.name;
			if (killer instanceof HumanObject) {
				killCount = ((HumanObject) killer).killCount;
			}
		}

		// 通知其他玩家 有地图单元离开视野
		SCStageObjectDisappear.Builder msg = SCStageObjectDisappear.newBuilder();
		msg.setObjId(id);
		msg.setType(DisappearType.Die.value());// 死亡
		msg.setKillerName(killerName);
		msg.setKillerId(killerId);
		msg.setSkillSn(skillSn);
		msg.setKillCount(killCount);// 连杀数

		return msg;
	}

}