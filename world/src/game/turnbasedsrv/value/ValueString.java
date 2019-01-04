package game.turnbasedsrv.value;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
public class ValueString extends ValueBase {
	public String value;

	/**
	 * 获取值的类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return ValueDefine.ValueString;
	}

	/**
	 * 构造函数
	 */
	public ValueString() {

	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public ValueString(String value) {
		this.value = value;
	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 * @param mapValue
	 */
	public ValueString(String value, Map<Long, ValueBase> mapValue) {
		this.value = value;
	}

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	@Override
	public void setValue(ValueBase value) {
		if (value.isStringValue()) {
			this.value = value.getStringValue();
		}
	}

	/**
	 * 获取值拷贝
	 * 
	 * @return
	 */
	@Override
	public ValueBase getCopy() {
		ValueString otherValue = new ValueString();
		otherValue.value = this.value;
		return otherValue;
	}

	/**
	 * 值相加
	 * 
	 * @return
	 */
	@Override
	public ValueBase add(ValueBase other) {

		if (other.isStringValue()) {
			this.value = this.value + other.getStringValue();
		}
		return this;
	}

	/**
	 * 是否是字符串
	 * 
	 * @return
	 */
	@Override
	public boolean isStringValue() {
		return true;
	}

	/**
	 * 取得字符串
	 * 
	 * @return
	 */
	public String getStringValue() {
		return this.value;
	}

	/**
	 * 值是否相等
	 * 
	 * @return
	 */
	@Override
	public boolean equals(ValueBase other) {
		if (other.isStringValue()) {
			return this.value.equals(other.getStringValue());
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
