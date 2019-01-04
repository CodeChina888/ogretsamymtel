package turnbasedsrv.value;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;

public class ValueDouble extends ValueBase {
	private double value;

	/**
	 * 获取值的类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return ValueDefine.ValueDouble;
	}

	/**
	 * 构造函数
	 */
	public ValueDouble() {

	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public ValueDouble(String value) {
		this.value = Utils.doubleValue(value);
	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public ValueDouble(double value) {
		this.value = value;
	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 * @param mapValue
	 */
	public ValueDouble(String value, Map<Long, ValueBase> mapValue) {
		this.value = Utils.doubleValue(value);
	}

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	@Override
	public void setValue(String value) {
		this.value = Utils.doubleValue(value);
	}

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	@Override
	public void setValue(ValueBase value) {
		if (value.isNumberValue()) {
			this.value = value.getNumberValue();
		}
	}

	/**
	 * 获取值拷贝
	 * 
	 * @return
	 */
	@Override
	public ValueBase getCopy() {
		ValueDouble propValue = new ValueDouble();
		propValue.value = this.value;
		return propValue;
	}

	/**
	 * 值加上一个int固定值
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase addInt(int value) {
		this.value += value;
		return this;
	}

	/**
	 * 值相加
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase add(ValueBase other) {
		if (other.isNumberValue()) {
			this.value = this.value + other.getNumberValue();
		}
		return this;
	}

	/**
	 * 值相减
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase sub(ValueBase other) {
		if (other.isNumberValue()) {
			this.value = this.value - other.getNumberValue();
		}
		return this;
	}

	/**
	 * 值相乘
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase multiply(ValueBase other) {
		if (other.isNumberValue()) {
			this.value = this.value * other.getNumberValue();
		}
		return this;
	}

	/**
	 * 值相除
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase divide(ValueBase other) {
		if (other.isNumberValue()) {
			this.value = this.value / other.getNumberValue();
		}
		return this;
	}

	/**
	 * 值幂
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase power(ValueBase other) {
		if (other.isNumberValue()) {
			this.value = Math.pow(this.value, other.getNumberValue());
		}
		return this;
	}

	/**
	 * 值模
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase modle(ValueBase other) {
		if (other.isNumberValue()) {
			this.value = Math.floorMod((long) this.value, (long) other.getNumberValue());
		}
		return this;
	}

	/**
	 * 值下整
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase floor() {
		this.value = Math.floor(this.value);
		return this;
	}

	/**
	 * 值上整
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase ceil() {
		this.value = Math.ceil(this.value);
		return this;
	}

	/**
	 * 值绝对值
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase abs() {
		this.value = Math.abs(this.value);
		return this;
	}

	/**
	 * 值开方
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase sqrt() {
		this.value = Math.sqrt(this.value);
		return this;
	}

	/**
	 * 值是否相等
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public boolean equals(ValueBase other) {
		return isNumberValueEquals(other);
	}

	/**
	 * 是否是数值
	 * 
	 * @return
	 */
	@Override
	public boolean isNumberValue() {
		return true;
	}

	/**
	 * 取得数值
	 * 
	 * @return
	 * @return
	 */
	@Override
	public double getNumberValue() {
		return this.value;
	}

	/**
	 * 转为文本显示
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("value", value).toString();
	}
}