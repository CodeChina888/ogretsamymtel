package game.worldsrv.support;

import java.util.ArrayList;
import java.util.List;

import core.support.TickTimer;
import core.support.Time;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.Vector3D;
import game.worldsrv.config.ConfMap;

import game.worldsrv.character.UnitObject;
import game.msg.Define.DVector3;
import game.worldsrv.character.HumanObject;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.pathFinding.HeightFinding;
import game.worldsrv.support.pathFinding.PathFinding;

/**
 * 地图单元移动类
 */
public class Running {
	public static final int INTERVAL = 125; // 两次改变移动最少间隔
	public static final int TIME_ERROR_MAX = 200; // 起点最大误差时间

	private UnitObject obj; // 移动所属对象
	private boolean running; // 是否在移动中，切记不可直接赋值，否则不同步
	private long runTimeBegin; // 移动开始时间
	public long runTimePulse; // 最后修改位置的时间
	private final Vector3D runPosBegin = new Vector3D(); // 移动开始坐标
	private final Vector3D runPosEnd = new Vector3D(); // 移动结束坐标
	private final List<Vector3D> runPaths = new ArrayList<>(); // 移动接下来目标

	private double speed; // 移动速度

	// 为了便于运算的暂存值
	public double runTempSin; // 起始至目标的Sin值
	public double runTempCos; // 起始至目标的Cos值

	// 计时器 每100毫秒计算一次
	private TickTimer timerUpdate = new TickTimer();

	/**
	 * 构造函数
	 * @param worldObject
	 */
	public Running(UnitObject worldObject) {
		this.obj = worldObject;
	}

	/**
	 * 移动 仅供WorldObj及子类的对应函数move调用 其余业务逻辑可使用xxObj.move()来改变移动状态 而不要直接调用本函数
	 * @param from
	 * @param to
	 */
	public void _move(Vector3D from, List<Vector3D> to, double speed) {
		if (to.isEmpty()) {
			Log.stageMove.error("===error in Running._move, to.isEmpty()");
			return;
		}

		// 首个目标点
		Vector3D posEndFirst = to.remove(0);

		// 设置移动过程中的临时变量
		runTimeBegin = obj.getTime();

		runPaths.clear();
		runPaths.addAll(to);

		// 设置为移动状态
		running = true;

		// 记录速度
		this.speed = speed;

		// 计算朝向
		// Vector2D toPos = new Vector2D(from.x, from.y);
		// obj.dirNow = toPos.sub(obj.posNow);
		// 当前位置
		obj.posNow.x = from.x;
		obj.posNow.y = from.y;

		// 设置下一段路径
		setNextPath(from, posEndFirst);

		// if(StageManager.inst().stageCollisionDetect(obj,
		// MathUtils.getDir(obj.posNow, runPosEnd.toVector2D()))) {
		// obj.stop();
		// return;
		// }

		// 为了便于运算的暂存值
		initTempValue();

		// 重置移动的timer
		timerUpdate.start(obj.getTime(), Utils.L100);
	}

	/**
	 * 停止移动 仅供WorldObj及子类的对应函数stop调用 其余业务逻辑可使用xxObj.stop()来改变移动状态 而不要直接调用本函数
	 */
	public void _stop() {
		if (!running)
			return;

		running = false;
		runPosEnd.x = runPosBegin.x;
		runPosEnd.y = runPosBegin.y;
		runPaths.clear();

		// if(obj.isHumanObj()) {
		// Log.fight.info("_stop {}", ExceptionUtils.getStackTrace(new
		// Throwable()));
		// }
	}

	public void _pulse(long curr) {
		_pulse(curr, false);
	}

	/**
	 * 每心跳移动处理
	 */
	public void _pulse(long curr, boolean force) {
		if (!isRunning())
			return;

		// 判断间隔时间
		if (!force && !timerUpdate.isPeriod(curr))
			return;

		// 经过时间
		long timeDiff = curr - runTimeBegin;

		/* 更新当前坐标 */
		// 移动距离
		double posMoveMax = (timeDiff * speed) / Time.SEC;
		// 移动距离对应的横纵偏移量
		double diffX = runTempCos * posMoveMax;
		double diffY = runTempSin * posMoveMax;

		// 实际移动距离
		double trueX = runPosBegin.x + diffX;
		double trueY = runPosBegin.y + diffY;

		// 计算拐点超时的时间
		long overDiffTime = (int) ((trueX - runPosEnd.x) * Time.SEC / speed);

		// 验证一下 不能超过目标点
		// 理论上不会出现这种情况 检查一下放心
		if (runPosEnd.x >= runPosBegin.x && runPosEnd.x < trueX)
			trueX = runPosEnd.x;
		else if (runPosEnd.x < runPosBegin.x && runPosEnd.x > trueX)
			trueX = runPosEnd.x;
		if (runPosEnd.y >= runPosBegin.y && runPosEnd.y < trueY)
			trueY = runPosEnd.y;
		else if (runPosEnd.y < runPosBegin.y && runPosEnd.y > trueY)
			trueY = runPosEnd.y;

		Vector3D trueVector = new Vector3D(trueX, trueY, runPosEnd.z);

		// if(StageManager.inst().stageCollisionDetect(obj,
		// MathUtils.getDir(obj.posNow, runPosEnd.toVector2D()))) {
		// obj.stop();
		// return;
		// }

		// 计算朝向
		Vector2D toPos = new Vector2D(trueX, trueY);
		obj.dirNow = toPos.sub(obj.posNow);
		// 设置对象的当前坐标
		obj.posNow.x = trueX;
		obj.posNow.y = trueY;

		runTimePulse = curr;

		// if(obj.isHumanObj()) {
		// Log.stageMove.info("{}移动至{}:{}。", obj.name ,obj.posNow.x,
		// obj.posNow.y);
		// }

		/* 判断是否达到目标点 */
		// 未达当前目标点
		if (!runPosEnd.equals(trueVector))
			return;

		// 没有后续目标点
		if (runPaths.isEmpty()) {
			obj.stop();// add by shenjh,停止移动

			// 记录日志
			// if(Log.stageMove.isDebugEnabled()) {
			// if(obj.isHumanObj()) {
			// Log.stageMove.debug("角色{}到达最终目标{}，停止移动。", obj.name,
			// trueVector.getPosStr());
			// } else if(obj.isGeneralObj()) {
			// Log.stageMove.debug("武将{}id{}到达最终目标{}，停止移动。", obj.name,
			// obj.id,trueVector.getPosStr());
			// }
			// else {
			// Log.stageMove.debug("怪物{}id{}到达最终目标{}，停止移动。", obj.name,
			// obj.id,trueVector.getPosStr());
			// }
			// }
		} else {
			// 设置下一段路径
			setNextPath(runPosEnd, runPaths.remove(0));
			runTimeBegin = curr - overDiffTime;

			// 为了便于运算的暂存值
			initTempValue();

			// 记录日志
			if (Log.stageMove.isDebugEnabled() && obj instanceof HumanObject) {
				Log.stageMove.debug("角色{}到达当前目标{}，下次目标{}，接下来的目标为{}。", obj.name, runPosBegin.getPosStr(),
						runPosEnd.getPosStr(), runPaths);
			}
		}
	}
	
	/**
	 * 获取当前的移动路径
	 * @return
	 */
	public List<Vector3D> getRunPath() {
		List<Vector3D> result = new ArrayList<>();
		result.add(runPosEnd);
		result.addAll(runPaths);

		return result;
	}

	/**
	 * 获取当前的移动路径消息
	 * @return
	 */
	public List<DVector3> getRunPathMsg() {
		return Vector3D.toMsgs(getRunPath());
	}

	/**
	 * 初始化暂存值 优化路径计算过程
	 */
	private void initTempValue() {
		// 修正终点坐标
		if (runPosEnd.x <= 0)
			runPosEnd.x = 1;
		if (runPosEnd.x >= obj.stageObj.width)
			runPosEnd.x = obj.stageObj.width - 1;
		if (runPosEnd.y <= 0)
			runPosEnd.y = 1;
		if (runPosEnd.y >= obj.stageObj.height)
			runPosEnd.y = obj.stageObj.height - 1;

		if (runPosEnd.x == runPosBegin.x && runPosEnd.y == runPosBegin.y) {
			_stop();
			return;
		}

		// 起始至目标横纵偏移量
		double diffX = runPosEnd.x - runPosBegin.x;
		double diffY = runPosEnd.y - runPosBegin.y;

		// 实际距离
		double diffTrue = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
		if (diffTrue == 0) {
			runTempSin = 0;
			runTempCos = 0;
			return;
		}

		// 起始至目标的Sin,Cos值
		runTempSin = diffY / diffTrue;
		runTempCos = diffX / diffTrue;
	}

	/**
	 * 设置下一段路径，如果下一个目标点不能直接到达，那么取起点和终点直线上从起点能到达的最远的点
	 * @param posBegin
	 * @param posEnd
	 */
	public void setNextPath(Vector3D posBegin, Vector3D posEnd) {
		Vector3D endReal = new Vector3D();
		endReal.set(posEnd);
		// if(obj instanceof HumanObject) {
		// endReal = PathFinding.raycast(obj.stageObj.mapSn, posBegin, posEnd,
		// obj.stageObj.pathFindingFlag);
		// //if(!endReal.equals(posEnd)) {
		// // Log.temp.info("两个路点之间不可以直接到达，进行坐标修正！起点为{}，终点为{}，修正后终点为{}",
		// posBegin, posEnd, endReal);
		// //}
		//
		// if(!posBegin.equals(posEnd) && endReal.equals(posBegin)) {
		// //Log.temp.info("射线找到人物{}的终点跟起点相同，起点{}，目标点{}", obj.name, posBegin,
		// posEnd);
		// _stop();
		// }
		// }
		runPosBegin.set(posBegin);
		runPosEnd.set(endReal);

		return;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isTimeExpired() {
		long curr = obj.getTime();
		// 如果更改移动的时间过短（能容忍最低时间间隔为125ms）
		// Log.stageMove.info("curr - runTimePulse: {}", curr - runTimePulse);
		if (running && curr - runTimePulse <= INTERVAL)
			return false;

		return true;
	}
	
	public void release() {
		this.obj = null;
	}
	
	public Vector3D getMoveEnd(){
		return this.runPosEnd;
	}

	/**
	 * 修正移动起始点
	 * @param from
	 * @return
	 */
	public Vector3D correctPosFrom(Vector3D from) {
		StageObject stageObj = obj.stageObj;
		ConfMap confMap = ConfMap.get(stageObj.mapSn);
		if (confMap == null) {
			Log.stageMove.error("Running.correctPosFrom : no finded mapSn={}", stageObj.mapSn);
			return from;
		}

		Vector3D result = new Vector3D();
		Vector3D posNow3D = StageManager.getHeight(stageObj.mapSn, obj.posNow);
		if (posNow3D.z == HeightFinding.NO_HEIGHT_INFO) {// 查无高度信息
			posNow3D.z = from.z;
		}

		// 如果起点在阻挡内，或者起点跟当前点之间走不通，则将起点强制设置为当前点
		if (PathFinding.isPosInBlock(stageObj.mapSn, from)) {// ||
																// !PathFinding.canReach(stageObj.mapSn,
																// posNow3D,
																// from,
																// stageObj.pathFindingFlag))
																// {
			result.set(posNow3D);
			if (Log.stageMove.isDebugEnabled()) {
				Log.stageMove.debug("Running.correctPosFrom : 起点={}，修正后终点={}", from, result);
			}
			return result;
		}

		// 没在阻挡中，误差在一定范围内，那修正到新给定的点
		double distanceMax = distanceErrorMax();
		if (from.distance(posNow3D) <= distanceMax) {
			result.set(from);
			return result;
		}
		// 没在阻挡中，且误差超过范围，那就在允许范围内尽量靠近给定的起点
		double diffX = from.x - obj.posNow.x;
		double diffY = from.y - obj.posNow.y;

		// 实际距离
		double diffTrue = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
		if (diffTrue == 0) {
			result.set(from);
			return result;
		}

		double fixX = diffX * distanceMax / diffTrue;
		double fixY = diffY * distanceMax / diffTrue;

		result.x = from.x - fixX;
		result.y = from.y - fixY;
		result.z = from.z;

		return result;
	}

	public double distanceErrorMax() {
		double posMoveMax = (TIME_ERROR_MAX * speed) / Time.SEC;
		return posMoveMax;
		// 移动距离对应的横纵偏移量
		// double diffX = runTempCos * posMoveMax;
		// double diffY = runTempSin * posMoveMax;
		//
		// return Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
	}

	/**
	 * 获取移动所属对象的移动朝向
	 */
	public Vector2D getDirV2D() {
		return obj.dirNow;
	}
	/**
	 * 获取移动所属对象的移动朝向
	 */
	public Vector3D getDirV3D() {
		return new Vector3D(obj.dirNow.x, obj.dirNow.y, 0);
	}

}
