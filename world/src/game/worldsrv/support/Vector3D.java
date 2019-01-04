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
import game.worldsrv.support.Vector3D;

import game.msg.Define.DVector3;

/**
 * 坐标
 */
public class Vector3D implements ISerilizable {
	public double x = 0; // X坐标
	public double y = 0; // Y坐标
	public double z = 0; // Z坐标

	public Vector3D() {
	}

	/**
	 * 构造函数
	 * @param x
	 * @param y
	 */
	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * 将消息位置信息转化本坐标
	 */
	public Vector3D(DVector3 vector3) {
		this(vector3.getX(), vector3.getY(), vector3.getZ());
	}

	/**
	 * 将消息位置信息转化本坐标
	 * @param vs
	 * @return
	 */
	public static List<Vector3D> parseFrom(List<DVector3> vs) {
		List<Vector3D> result = new ArrayList<>();
		for (DVector3 v : vs) {
			result.add(new Vector3D(v));
		}

		return result;
	}

	/**
	 * 将消息位置信息转化本坐标
	 * @param vs
	 * @return
	 */
	public static List<DVector3> toMsgs(List<Vector3D> vs) {
		List<DVector3> result = new ArrayList<>();
		for (Vector3D v : vs) {
			result.add(v.toMsg());
		}

		return result;
	}

	/**
	 * 转化为消息类型
	 * @return
	 */
	public DVector3 toMsg() {
		DVector3.Builder msg = DVector3.newBuilder();
		msg.setX((float) x);
		msg.setY((float) y);
		msg.setZ((float) z);

		return msg.build();
	}

	/**
	 * 转换为三维float型数组
	 */
	public float[] toFloat3() {
		return new float[]{(float) x, (float) y, (float) z};
	}

	public Vector2D toVector2D() {
		return new Vector2D(x, y);
	}

	/**
	 * 导航网格的 xyz 转到 detour 的格式
	 * @return
	 */
	public float[] toDetourFloat3() {
		return new float[]{(float) y, (float) z, (float) x};
	}

	public Vector3D toServFromDetour() {
		return new Vector3D(z, x, y);
	}

	/**
	 * 设置坐标值
	 * @param vector
	 */
	public void set(Vector3D vector) {
		this.x = vector.x;
		this.y = vector.y;
		this.z = vector.z;
	}

	public Vector3D sub(Vector3D vector) {
		this.x -= vector.x;
		this.y -= vector.y;
		this.z -= vector.z;
		return this;
	}

	public Vector3D sum(Vector3D vector) {
		this.x += vector.x;
		this.y += vector.y;
		this.z += vector.z;
		return this;
	}

	public Vector3D div(double a) {
		Vector3D result = new Vector3D();
		result.x = this.x / a;
		result.y = this.y / a;
		result.z = this.z / a;
		return result;
	}

	/**
	 * 获取易读的坐标字符串
	 * @return
	 */
	public String getPosStr() {
		return new StringBuilder("(").append(x).append(",").append(y).append(",").append(z).append(")").toString();
	}

	/**
	 * 两点之间的距离
	 * @param pos
	 * @return
	 */
	public double distance(Vector3D pos) {
		double t1x = this.x;
		double t1y = this.y;
		double t2x = pos.x;
		double t2y = pos.y;

		return Math.sqrt(Math.pow((t1x - t2x), 2) + Math.pow((t1y - t2y), 2));
	}

	/**
	 * 两点之间的距离参考值，不开根号
	 * @param pos
	 * @return
	 */
	public double distanceFar(Vector3D pos) {
		double t1x = this.x;
		double t1y = this.y;
		double t2x = pos.x;
		double t2y = pos.y;

		return Math.pow((t1x - t2x), 2) + Math.pow((t1y - t2y), 2);
	}

	/**
	 * 两点之间的距离,带高度
	 * @param pos
	 * @return
	 */
	public double distance3D(Vector3D pos) {
		double t1x = this.x;
		double t1y = this.y;
		double t1z = this.z;
		double t2x = pos.x;
		double t2y = pos.y;
		double t2z = pos.z;

		return Math.sqrt(Math.pow((t1x - t2x), 2) + Math.pow((t1y - t2y), 2) + Math.pow((t1z - t2z), 2));
	}

	public double Dot(Vector3D vector) {
		return this.x * vector.x + this.y * vector.y + this.z * vector.z;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(x);
		out.write(y);
		out.write(z);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		x = in.read();
		y = in.read();
		z = in.read();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof Vector3D)) {
			return false;
		}

		Vector3D castOther = (Vector3D) other;
		return new EqualsBuilder().append(this.x, castOther.x).append(this.y, castOther.y).append(this.z, castOther.z)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(x).append(y).append(z).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("x=", String.format("%.2f", x))
				.append("y=", String.format("%.2f", y)).append("z=", String.format("%.2f", z)).toString();
		// return new
		// StringBuilder().append("[").append(x).append(",").append(y).append(",").append(z).append("]")
		// .toString();
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
		if (this.x == 0 && this.y == 0 && this.z == 0) {
			ret = true;
		}
		return ret;
	}

	/**
	 * 是否相等的点
	 * @return
	 */
	public boolean isEqual(Vector3D pos) {
		boolean ret = false;
		if (this.x == pos.x && this.y == pos.y && this.z == pos.z) {
			ret = true;
		}
		return ret;
	}

}
