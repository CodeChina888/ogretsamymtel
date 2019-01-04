package turnbasedsrv.value;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;

public class ValueBooleanFalse extends ValueBase {
	public boolean value;

	/**
	 * 获取值的类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return ValueDefine.ValueBooleanFalse;
	}

	/**
	 * 构造函数
	 */
	public ValueBooleanFalse() {

	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public ValueBooleanFalse(String value) {
		this.value = Utils.booleanValue(value);
	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public ValueBooleanFalse(boolean value) {
		this.value = value;
	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 * @param mapValue
	 */
	public ValueBooleanFalse(String value, Map<Long, ValueBase> mapValue) {
		this.value = Utils.booleanValue(value);
	}

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	@Override
	public void setValue(String value) {
		this.value = Utils.booleanValue(value);
	}

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	@Override
	public void setValue(ValueBase value) {
		if (value.isBooleanValue()) {
			this.value = value.getBooleanValue();
		}
	}

	/**
	 * 获取值拷贝
	 * 
	 * @return
	 */
	@Override
	public ValueBase getCopy() {
		ValueBooleanFalse otherValue = new ValueBooleanFalse();
		otherValue.value = this.value;
		return otherValue;
	}

	/**
	 * 值相加
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public ValueBase add(ValueBase other) {
		if (!this.value) {
			return this;
		}
		if (other.isBooleanValue() && !other.getBooleanValue()) {
			this.value = false;
		}
		return this;
	}

	/**
	 * 是否是布尔值
	 * 
	 * @return
	 */
	@Override
	public boolean isBooleanValue() {
		return true;
	}

	/**
	 * 取得布尔值
	 * 
	 * @return
	 */
	public boolean getBooleanValue() {
		return this.value;
	}

	/**
	 * 值是否相等
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public boolean equals(ValueBase other) {
		if (other.isBooleanValue()) {
			return this.value == other.getBooleanValue();
		}
		return false;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("value", value).toString();
	}
}
