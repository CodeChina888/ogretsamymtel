package turnbasedsrv.value;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;

public class ValueIntSet extends ValueBase {
	public Set<Integer> value = new HashSet<>();

	/**
	 * 获取值的类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return ValueDefine.ValueIntSet;
	}

	/**
	 * 构造函数
	 */
	public ValueIntSet() {

	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public ValueIntSet(String value) {
		List<Integer> list = Utils.strToIntList(value);
		this.value.addAll(list);
	}

	/**
	 * 构造函数
	 * 
	 * @param value
	 * @param mapValue
	 */
	public ValueIntSet(String value, Map<Long, ValueBase> mapValue) {
		List<Integer> list = Utils.strToIntList(value);
		this.value.addAll(list);
	}

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	@Override
	public void setValue(String value) {
		List<Integer> list = Utils.strToIntList(value);
		this.value.addAll(list);
	}

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	@Override
	public void setValue(ValueBase value) {
		if (value instanceof ValueIntSet) {
			this.value.clear();
			ValueIntSet setValue = (ValueIntSet) value;
			this.value.addAll(setValue.value);
		}
	}

	/**
	 * 获取值拷贝
	 * 
	 * @return
	 */
	@Override
	public ValueBase getCopy() {
		ValueIntSet otherValue = new ValueIntSet();
		otherValue.value.addAll(this.value);
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
		if (other instanceof ValueIntSet) {
			ValueIntSet addValue = (ValueIntSet)other;
			this.value.addAll(addValue.value);
		}
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
