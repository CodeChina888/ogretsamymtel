package game.worldsrv.support;

import game.worldsrv.support.Vector2D;

import java.util.List;

import game.worldsrv.support.MathUtils;

public class MathUtils {

	public static double EPSILON = 1.0e-6;
	public static double COLLISION_ANGLE = 42.5;

	/**
	 * 计算 从起始点 到目标点是否产生碰撞
	 * @param originPos 起始点
	 * @param originDir 起始方向
	 * @param targetPos 目标点
	 * @param radius 碰撞体积
	 * @param angleArr
	 * @return
	 */
	public static boolean collisionDetect(Vector2D originPos, Vector2D originDir, Vector2D targetPos, double radius,
			List<Double> angleArr) {
		boolean result = false;
		// 如果 AB 距离小于距离 并且角度
		double dis = originPos.distance(targetPos);
		if (dis < radius) {
			double angle = MathUtils.getRotateAngle(targetPos.x - originPos.x, targetPos.y - originPos.y, originDir.x,
					originDir.y);
			// Log.temp.info("碰撞产生angle{}:", angle);
			if (angle < COLLISION_ANGLE && angle >= 0) {
				// Log.temp.info("碰撞产生angle{}:", angle);
				if (angleArr != null) {
					angleArr.add(angle);
				}
				return true;
			} else if (angle > (360 - COLLISION_ANGLE) && angle <= 360) {
				// Log.temp.info("碰撞产生angle{}:", angle);
				if (angleArr != null) {
					angleArr.add(angle);
				}
				return true;
			}
		}

		return result;
	}

	/**
	 * 计算 从起始点 到目标点是否产生碰撞
	 * @param originPos 起始点
	 * @param originDir 起始方向
	 * @param targetPos 目标点
	 * @param radius 碰撞体积
	 * @param angleArr 返回起点到目标的角度
	 * @return
	 */
	public static boolean collisionDetectAll(Vector2D originPos, Vector2D originDir, Vector2D targetPos, double radius,
			List<Double> angleArr) {
		boolean result = false;
		// 如果 AB 距离小于距离 并且角度
		double dis = originPos.distance(targetPos);
		if (dis < radius) {
			double angle = MathUtils.getRotateAngle(targetPos.x - originPos.x, targetPos.y - originPos.y, originDir.x,
					originDir.y);

			if (angle < COLLISION_ANGLE && angle >= 0) {
				result = true;
			} else if (angle > (360 - COLLISION_ANGLE) && angle <= 360) {
				result = true;
			}

			if (angleArr != null) {
				if (angle > 180) {
					angle = angle - 360;
				}
				angleArr.add(angle);
			}
		}

		return result;
	}

	public static Vector2D getDir(Vector2D ori, Vector2D tar) {
		return new Vector2D(tar.x - ori.x, tar.y - ori.y);
	}

	/**
	 * 获取向量p2到p1的逆时针旋转角，其中向量p1表示为x1，y1；向量p2表示为x2，y2
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double getRotateAngle(double x1, double y1, double x2, double y2) {
		double dist, dot, degree, angle;
		if (x1 == x2 && y1 == y2) {
			return 0;
		}

		// normalize，标准化
		dist = Math.sqrt(x1 * x1 + y1 * y1);
		x1 /= dist;
		y1 /= dist;

		dist = Math.sqrt(x2 * x2 + y2 * y2);
		x2 /= dist;
		y2 /= dist;

		// dot product 向量点积
		dot = x1 * x2 + y1 * y2;
		if (Math.abs(dot - 1.0) <= EPSILON) {
			angle = 0.0;
		} else if (Math.abs(dot + 10) <= EPSILON) {
			angle = Math.PI;
		} else {
			double cross;
			angle = Math.acos(dot);

			// cross product 向量x乘
			cross = x1 * y2 - x2 * y1;

			// vector p2 is clockwise from vector p1
			// with respect to the origin (0.0)
			if (cross > 0) {
				angle = 2 * Math.PI - angle;
			}
		}

		degree = angle * 180.0 / Math.PI;
		return degree;
	}
}
