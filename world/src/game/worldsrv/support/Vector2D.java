package game.worldsrv.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.support.Vector2D;

import game.msg.Define.DVector2;

/**
 * 坐标
 */
public class Vector2D implements ISerilizable {
	public double x = 0; // 横坐标
	public double y = 0; // 纵坐标

	public Vector2D() {
	}

	/**
	 * 构造函数
	 * @param x
	 * @param y
	 */
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 将消息位置信息转化本坐标
	 * @param vector2
	 */
	public Vector2D(DVector2 vector2) {
		this(vector2.getX(), vector2.getY());
	}

	/**
	 * 将消息位置信息转化本坐标
	 * @param vs
	 * @return
	 */
	public static List<Vector2D> parseFrom(List<DVector2> vs) {
		List<Vector2D> result = new ArrayList<>();
		for (DVector2 v : vs) {
			result.add(new Vector2D(v));
		}

		return result;
	}

	/**
	 * 将消息位置信息转化本坐标
	 * @param vs
	 * @return
	 */
	public static List<DVector2> toMsgs(List<Vector2D> vs) {
		List<DVector2> result = new ArrayList<>();
		for (Vector2D v : vs) {
			result.add(v.toMsg());
		}

		return result;
	}

	/**
	 * 转化为消息类型
	 * @return
	 */
	public DVector2 toMsg() {
		DVector2.Builder msg = DVector2.newBuilder();
		msg.setX((float) x);
		msg.setY((float) y);

		return msg.build();
	}

	/**
	 * 转换为三维float型数组
	 */
	public float[] toFloat3() {
		return new float[]{(float) x, 0, (float) y};
	}

	/**
	 * 导航网格的
	 * @return
	 */
	public float[] toDetourFloat3() {
		return new float[]{(float) y, 0, (float) x};
	}

	/**
	 * 设置坐标值
	 */
	public void set(Vector2D vector) {
		this.x = vector.x;
		this.y = vector.y;
	}

	/**
	 * 设置坐标值
	 */
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2D sub(Vector2D vector) {
		Vector2D result = new Vector2D();
		result.x = this.x - vector.x;
		result.y = this.y - vector.y;
		return result;
	}

	public Vector2D sum(Vector2D vector) {
		Vector2D result = new Vector2D();
		result.x = this.x + vector.x;
		result.y = this.y + vector.y;
		return result;
	}

	public Vector2D mul(Vector2D vector) {
		Vector2D result = new Vector2D();
		result.x = this.x * vector.x;
		result.y = this.y * vector.y;
		return result;
	}

	public Vector2D mul(double a) {
		Vector2D result = new Vector2D();
		result.x = this.x * a;
		result.y = this.y * a;
		return result;
	}

	public Vector2D div(double a) {
		Vector2D result = new Vector2D();
		result.x = this.x / a;
		result.y = this.y / a;
		return result;
	}

	public Vector2D normalize() {
		Vector2D result = new Vector2D();
		double dis = Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
		result.x = this.x / dis;
		result.y = this.y / dis;
		return result;
	}

	public double Dot(Vector2D vector) {
		return this.x * vector.x + this.y * vector.y;
	}

	/**
	 * 获取易读的坐标字符串
	 * @return
	 */
	public String getPosStr() {
		return new StringBuilder("(").append(x).append(",").append(y).append(")").toString();
	}

	/**
	 * 两点之间的距离
	 * @param pos
	 * @return
	 */
	public double distance(Vector2D pos) {
		if (pos == null)
			return 0;

		double t1x = this.x;
		double t1y = this.y;
		double t2x = pos.x;
		double t2y = pos.y;

		return Math.sqrt(Math.pow((t1x - t2x), 2) + Math.pow((t1y - t2y), 2));
	}

	/**
	 * 从start 指向 end的方向 从org点移动 DIS的距离
	 * @param start
	 * @param end
	 * @param dis
	 * @return
	 */
	public static Vector2D lookAtDis(Vector2D start, Vector2D end, Vector2D org, double dis) {
		Vector2D result = new Vector2D();

		if (end.equals(start)) {
			return start;
		}
		double diffX = end.x - start.x;
		double diffY = end.y - start.y;

		// 实际距离
		double diffTrue = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

		// 起始至目标的Sin,Cos值
		double tempSin = diffY / diffTrue;
		double tempCos = diffX / diffTrue;

		double dX = tempCos * dis;
		double dY = tempSin * dis;

		result.x = org.x + dX;
		result.y = org.y + dY;

		return result;
	}

	/**
	 * 从start 指向 end的方向 从org点移动 DIS的距离 包含攻击，移动距离要减去相对距离
	 * @param start
	 * @param end
	 * @return
	 */
	public static Vector2D lookAtAttDis(Vector2D start, Vector2D end, Vector2D org, double attDis) {
		Vector2D result = new Vector2D();

		if (end.equals(start)) {
			return start;
		}
		double diffX = end.x - start.x;
		double diffY = end.y - start.y;

		// 实际距离
		double diffTrue = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));

		// 移动距离不能超过攻击移动距离
		double fixAttDis = attDis - diffTrue;
		fixAttDis = fixAttDis < 0 ? 0D : fixAttDis;

		// 起始至目标的Sin,Cos值
		double tempSin = diffY / diffTrue;
		double tempCos = diffX / diffTrue;

		double dX = tempCos * fixAttDis;
		double dY = tempSin * fixAttDis;

		result.x = org.x + dX;
		result.y = org.y + dY;

		return result;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(x);
		out.write(y);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		x = in.read();
		y = in.read();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof Vector2D)) {
			return false;
		}

		Vector2D castOther = (Vector2D) other;
		return new EqualsBuilder().append(this.x, castOther.x).append(this.y, castOther.y).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(x).append(y).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("x=", String.format("%.2f", x))
				.append("y=", String.format("%.2f", y)).toString();
		// return new
		// StringBuilder().append("[").append(x).append(",").append(y).append("]").toString();
	}

	/**
	 * 是否错误坐标点（x或y是负值则属于错误坐标点）
	 * @return
	 */
	public boolean isWrongPos() {
		boolean ret = false;
		if (this.x < 0 || this.y < 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 是否零点
	 * @return
	 */
	public boolean isZero() {
		boolean ret = false;
		if (this.x == 0 && this.y == 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 是否相等的点
	 */
	public boolean isEqual(Vector2D pos) {
		boolean ret = false;
		if (this.x == pos.x && this.y == pos.y) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 获取两个点连线的中点
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Vector2D getMiddlePos(Vector2D p1, Vector2D p2) {
		return new Vector2D((p1.x + p2.x) / 2.0, (p1.y + p2.y) / 2.0);
	}

}
