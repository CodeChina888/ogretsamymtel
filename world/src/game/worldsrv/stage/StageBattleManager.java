package game.worldsrv.stage;

import game.worldsrv.character.UnitObject;
import game.worldsrv.character.WorldObject;
import game.worldsrv.support.Vector2D;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;

import java.util.ArrayList;
import java.util.List;

import core.support.ManagerBase;
import core.support.RandomUtils;

public class StageBattleManager extends ManagerBase {
	public float GridWidth = 1f;
	public float GridHeight = 1f;
	public int GridColumns = 5;

	public static StageBattleManager inst() {
		return inst(StageBattleManager.class);
	}

	public int calMarchIndex(UnitObject mons, int[] index) {
		int result = -1;
		if (mons != null) {
			int row = mons.getUnit().getProfession() - 1;
			result = index[row] + row * GridColumns;
			index[row]++;
		}

		return result;
	}

	/**
	 * 网格计算武将的实际坐标
	 * @param basePos
	 * @param dir
	 * @param index
	 * @param rotate
	 * @return
	 */
	public Vector2D calPos(Vector2D basePos, Vector2D dir, int index, boolean rotate) {
		return calPos(basePos, dir, index, rotate, 2);
	}

	public Vector2D calPos(Vector2D basePos, Vector2D dir, int index, boolean rotate, float factor) {
		int col = index % GridColumns;
		int row = index / GridColumns;

		Vector2D position = basePos.sum(dir.mul(row * GridHeight * factor * -1));
		Vector2D dirRot90 = new Vector2D(dir.y, -dir.x);
		position = position.sum(dirRot90.mul((double) (((Math.ceil((double) col / 2) * (col % 2 - 0.5) * (rotate
				? 2
				: -2)) * GridWidth + GridWidth * (row % 2) * (rotate ? -0.5f : 0.5f)) * factor)));

		return position;
	}

	/**
	 * 根据圆心和起始半径，在指定角度的扇行范围内随机一个点
	 * @param pos
	 * @return//角度
	 */
	public Vector2D randomPosInAngle(Vector2D pos, double radiusMin, double radiusMax, int angle) {
		if (radiusMin >= radiusMax) {
			return pos;
			// throw new SysException("在圆环内随机点出错，小圆半径大于大圆半径");
		}

		// 半径 TODO 需要改进
		double r = radiusMin + RandomUtils.nextDouble() * (radiusMax - radiusMin);
		// 计算位置
		return new Vector2D(pos.x + r * Math.sin(angle), pos.y + r * Math.cos(angle));
	}

	/**
	 * 根据圆心和起始半径，在圆环内随机一个点
	 * @param pos
	 * @return
	 */
	public Vector2D randomPosInCircle(Vector2D pos, double radiusMin, double radiusMax) {
		if (radiusMin >= radiusMax) {
			return pos;
			// throw new SysException("在圆环内随机点出错，小圆半径大于大圆半径");
		}

		// 随机位置
		// 角度
		int angle = RandomUtils.nextInt(360);
		// 半径 TODO 需要改进
		double r = radiusMin + RandomUtils.nextDouble() * (radiusMax - radiusMin);
		// 计算位置
		return new Vector2D(pos.x + r * Math.sin(angle), pos.y + r * Math.cos(angle));
	}

	public Vector2D randomPosInCircle(StageObject stageObj, Vector2D pos, double radiusMin, double radiusMax) {
		if (radiusMin >= radiusMax) {
			return pos;
			// throw new SysException("在圆环内随机点出错，小圆半径大于大圆半径");
		}

		// 随机位置
		// 角度
		int angle = stageObj.randUtils.nextInt(360);
		// 半径 TODO 需要改进
		double r = radiusMin + stageObj.randUtils.nextDouble() * (radiusMax - radiusMin);
		// 计算位置
		return new Vector2D(pos.x + r * Math.sin(angle), pos.y + r * Math.cos(angle));
	}

	public List<UnitObject> getUnitObjFarest(List<UnitObject> tars, UnitObject unitObjAtk, boolean excludeTeamBundle) {
		UnitObject resultObj = tars.get(0);
		double dis = -1;
		double tempDis;
		for (UnitObject unitObject : tars) {
			tempDis = unitObjAtk.posNow.distance(unitObject.posNow);
			if (tempDis > dis) {
				// 删除 不包含自己的时候的自己 不包含敌人时候的敌人
				if ((excludeTeamBundle && unitObject.teamBundleID == unitObjAtk.teamBundleID)
						|| (!excludeTeamBundle && unitObject.teamBundleID != unitObjAtk.teamBundleID)) {
					continue;
				}
				dis = tempDis;
				resultObj = unitObject;
			}
		}
		tars.clear();
		tars.add(resultObj);

		return tars;
	}

	public List<UnitObject> getUnitObjNearest(List<UnitObject> tars, UnitObject unitObjAtk, boolean excludeTeamBundle) {
		UnitObject resultObj = tars.get(0);
		double dis = 9999;
		double tempDis;
		for (UnitObject unitObject : tars) {
			tempDis = unitObjAtk.posNow.distance(unitObject.posNow);
			if (tempDis < dis) {
				// 删除 不包含自己的时候的自己 不包含敌人时候的敌人
				if ((excludeTeamBundle && unitObject.teamBundleID == unitObjAtk.teamBundleID)
						|| (!excludeTeamBundle && unitObject.teamBundleID != unitObjAtk.teamBundleID)) {
					continue;
				}
				dis = tempDis;
				resultObj = unitObject;
			}
		}
		tars.clear();
		tars.add(resultObj);

		return tars;
	}

	/**
	 * 获取以pos位置为起点，target为终点的线段，往线段两边扩展width/2，并且从起始点为长度length的矩形区域内的unitObj
	 * @param stageObj
	 * @param pos
	 * @param width
	 * @return
	 */
	public List<UnitObject> getUnitObjsInRectangle(StageObject stageObj, Vector2D pos, Vector2D target, double height,
			double width) {
		List<UnitObject> result = new ArrayList<>();
		if (stageObj == null)
			return result;

		double c = pos.distance(target);

		// 利用余弦定理计算夹角a的余弦值
		double cosA = (double) (target.x - pos.x) / c;
		double sinA = (double) (target.y - pos.y) / c;

		for (UnitObject uo : stageObj.getUnitObjs().values()) {
			float collisionRadius = 0f;
//			if (uo.confModel != null)
//				collisionRadius = uo.confModel.collisionRadius;

			// 判断距离是否符合要求
			Vector2D p = uo.posNow;
			// p绕pos点旋转-A角度到新坐标
			// 如果P点绕另一点P0（x0,y0）旋转β到点P1，旋转后位置计算公式如下：
			// dx = x-x0;
			// dy = y-y0;
			// x1=cos(β)*dx-sin(β)*dy+x0;
			// y1=cos(β)*dy+sin(β)*dx+y0;
			double cosB = cosA;
			double sinB = -sinA;
			double dx = p.x - pos.x;
			double dy = p.y - pos.y;
			double x1 = cosB * dx - sinB * dy + pos.x;
			double y1 = cosB * dy + sinB * dx + pos.y;

			// if(x1 >= pos.x && x1 <= (pos.x + width) &&
			// y1 >= (pos.y - height / 2) && y1 <= (pos.y + height / 2)) {
			if (x1 >= pos.x && x1 <= (pos.x + width + collisionRadius) && y1 <= (pos.y + height / 2 + collisionRadius)
					&& y1 >= pos.y - height / 2 - collisionRadius) {
				result.add(uo);
			}
		}
		return result;
	}

	/**
	 * 获取以pos为圆心，半径为radius的圆形区域内的UnitObject
	 * @param stageObj
	 * @param pos
	 * @param radius
	 * @return
	 */
	public List<UnitObject> getUnitObjsInCircle(StageObject stageObj, Vector2D pos, double radius) {
		List<UnitObject> result = new ArrayList<>();
		if (stageObj == null)
			return result;// 如果已经调用_delWorldObj，则场景已经置空了

		for (UnitObject uo : stageObj.getUnitObjs().values()) {
			float collisionRadius = 0f;
//			if (uo.confModel != null)
//				collisionRadius = uo.confModel.collisionRadius;

			// 判断距离是否符合要求
			Vector2D p = uo.posNow;
			if (pos.distance(p) < (radius + collisionRadius)) {
				result.add(uo);
			}
		}

		return result;
	}

	public List<UnitObject> getUnitObjsInCircleProfession(StageObject stageObj, int profession, Vector2D pos,
			double radius) {
		List<UnitObject> result = new ArrayList<>();
		if (stageObj == null)
			return result;

		int pro = 0;
		if ((profession & 1) > 0) {
			pro = 1;
		} else if ((profession & 2) > 0) {
			pro = 2;
		} else if ((profession & 4) > 0) {
			pro = 3;
		}
		for (UnitObject uo : stageObj.getUnitObjs().values()) {
			// 判断距离是否符合要求
			Vector2D p = uo.posNow;
			if (pos.distance(p) < radius) {
				if (uo.profession == pro) {
					result.add(uo);
				}
			}
		}

		return result;
	}

	/**
	 * 范围内HP 最少的
	 * @param stageObj
	 * @param exclude
	 * @param pos
	 * @param radius
	 * @return
	 */
	public UnitObject getUnitObjsInCircleHpLeast(StageObject stageObj, List<UnitObject> exclude, Vector2D pos,
			double radius) {
		int hpMax = 9999999;
		UnitObject hpLeastObj = null;
		if (stageObj == null)
			return hpLeastObj;

		for (UnitObject uo : stageObj.getUnitObjs().values()) {
			// 判断距离是否符合要求
			Vector2D p = uo.posNow;
			if (pos.distance(p) < radius) {
				if (uo.getUnit().getHpCur() < hpMax && !exclude.contains(uo)) {
					hpLeastObj = uo;
					hpMax = uo.getUnit().getHpCur();
				}
			}
		}

		return hpLeastObj;
	}

	/**
	 * 获取某张地图上以center为圆心，朝向target，且半径为radius，角度为angle的扇形区域内的UnitObject，
	 * isFront用于选择是正面还是背面，true正面
	 * @param target
	 * @param radius
	 * @param angle
	 * @param isFront
	 * @return
	 */
	public List<UnitObject> getUnitObjsInSector(UnitObject unitObjAtk, Vector2D target, double radius, int angle,
			boolean isFront) {
		StageObject stageObj = unitObjAtk.stageObj;
		Vector2D center = unitObjAtk.posNow;
		// 如果是取背面，把目标点变为中心点的对称点
		if (!isFront) {
			double x = 2 * target.x - center.x;
			double y = 2 * target.y - center.y;
			target = new Vector2D(x, y);
		}

		List<UnitObject> result = new ArrayList<>();
		for (WorldObject wo : stageObj.getWorldObjs().values()) {
			if (!(wo instanceof UnitObject))
				continue;

			UnitObject unitObj = (UnitObject) wo;
			// Log.skill.info("===获取范围内目标：wo.name={},wo.pos={}, center={},nameAtk={}",wo.name,wo.posNow.toString(),
			// center.toString(),unitObjAtk.name);
			// 如果该UnitObject在扇形区域内
			if (isPosInSector(unitObj, center, target, radius, angle)) {
				result.add(unitObj);

				if (Log.skill.isDebugEnabled()) {// 调试日志
					// if(wo instanceof MonsterObject)
					// Log.skill.debug("===范围内的目标：wo.name={},wo.pos={}, center={},nameAtk={}",
					// wo.name,
					// wo.posNow.toString(), center.toString(),
					// unitObjAtk.name);
				}
			} else {
				if (Log.skill.isDebugEnabled()) {// 调试日志
					// if(wo instanceof MonsterObject)
					// Log.skill
					// .debug("===不在范围内的目标：wo.name={},wo.pos={}, center={}, target.pos={}, radius={}, angle={}, nameAtk={}",
					// wo.name, wo.posNow.toString(), center.toString(),
					// target.toString(), radius, angle,
					// unitObjAtk.name);
				}
			}
		}
		// Log.human.info("===获取范围内目标：name={},result.size={}",unitObjAtk.name,result.size());//sjh
		return result;
	}

	/**
	 * 判断点pos是否在以center为圆心，朝向target，且半径为radius，角度为angle的扇形区域内 余弦定理
	 * @param center
	 * @param target
	 * @param radius
	 * @param angle
	 * @return
	 */
	public boolean isPosInSector(UnitObject uo, Vector2D center, Vector2D target, double radius, int angle) {
		Vector2D pos = uo.posNow;
		float collisionRadius = 0f;
//		if (uo.confModel != null)
//			collisionRadius = uo.confModel.collisionRadius;

		// 如果点在圆外，直接false
		if (pos.distance(center) > (radius + collisionRadius)) {
			return false;
		}

		// 线段长
		double a = target.distance(pos);// 敌人到技能点的距离
		double b = pos.distance(center);// 你与敌人的距离
		double c = center.distance(target);// 你到技能点的距离
		// 利用余弦定理计算夹角a的余弦值
		double cosA = (b * b + c * c - a * a) / (2 * b * c);
		// 计算direct到line之间的角度
		double an = Math.acos(cosA);
		// 减掉碰撞半径夹角
		double tan = Math.asin(collisionRadius / b);// 计算direct到line之间的角度
		an -= tan;
		//

		an = an * 180 / Math.PI;
		// 用角度判断
		if ((int) an <= angle / 2) {
			return true;
		}
		return false;
	}

}
