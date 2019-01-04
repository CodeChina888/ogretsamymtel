package game.turnbasedsrv.value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.support.Utils;
import game.worldsrv.config.ConfProperty;

public class ValueFactory {

	private static final ValueDouble zeroValueDouble = new ValueDouble("0");
	private static final ValueFloat zeroValueFloat = new ValueFloat("0");
	private static final ValueInt zeroValueInt = new ValueInt("0");
	private static final ValueLong zeroValueLong = new ValueLong("0");
	private static final ValueBoolean zeroValueBoolean = new ValueBoolean("false");
	private static final ValueBooleanFalse zeroValueBooleanFalse = new ValueBooleanFalse("flase");
	private static final ValueString zeroValueString = new ValueString("");
	private static final ValueIntSet zeroValueIntList = new ValueIntSet("");

	/**
	 * 获取指定属性的零值副本
	 * 
	 * @return
	 */
	public static ValueBase getZeroValueByName(String propName) {
		ValueBase ret = null;
		ConfProperty confProp = ConfProperty.get(propName);
		if (null != confProp) {
			ret = getZeroValueByType(confProp.propValueType);
		}
		return ret;
	}

	public static ValueBase getZeroValueByType(String valueType) {
		ValueBase ret = null;
		switch (valueType) {
		case ValueDefine.ValueDouble: {
			ret = zeroValueDouble.getCopy();
		}
			break;
		case ValueDefine.ValueFloat: {
			ret = zeroValueFloat.getCopy();
		}
			break;
		case ValueDefine.ValueInt: {
			ret = zeroValueInt.getCopy();
		}
			break;
		case ValueDefine.ValueLong: {
			ret = zeroValueLong.getCopy();
		}
			break;
		case ValueDefine.ValueBoolean: {
			ret = zeroValueBoolean.getCopy();
		}
			break;
		case ValueDefine.ValueBooleanFalse: {
			ret = zeroValueBooleanFalse.getCopy();
		}
			break;
		case ValueDefine.ValueString: {
			ret = zeroValueString.getCopy();
		}
			break;
		case ValueDefine.ValueIntSet: {
			ret = zeroValueIntList.getCopy();
		}
			break;
		default:
			break;
		}
		return ret;
	}

	/**
	 * 获取指定属性名的值
	 * 
	 * @return
	 */
	public static ValueBase getFightValueByName(String propName, String value) {
		ValueBase ret = null;
		ConfProperty confProp = ConfProperty.get(propName);
		if (null != confProp) {
			ret = getFightValueByType(confProp.propValueType, value);
		}
		return ret;
	}

	/**
	 * 获取指定类型的值
	 * 
	 * @return
	 */
	public static ValueBase getFightValueByType(String valueType, String value) {
		switch (valueType) {
		case ValueDefine.ValueDouble:
			return new ValueDouble(value);
		case ValueDefine.ValueFloat:
			return new ValueFloat(value);
		case ValueDefine.ValueInt:
			return new ValueInt(value);
		case ValueDefine.ValueLong:
			return new ValueLong(value);
		case ValueDefine.ValueBoolean:
			return new ValueBoolean(value);
		case ValueDefine.ValueBooleanFalse:
			return new ValueBooleanFalse(value);
		case ValueDefine.ValueString:
			return new ValueString(value);
		case ValueDefine.ValueIntSet:
			return new ValueIntSet(value);
		default:
			return null;
		}
	}

	/**
	 * 获取数值
	 * 
	 * @param valueType
	 * @param value
	 * @param mapValues
	 * @return
	 */
	public static ValueBase getFightValue(String valueType, String value, Map<Long, ValueBase> mapValues) {
		switch (valueType) {
		case ValueDefine.ValueDouble:
			return new ValueDouble(value, mapValues);
		case ValueDefine.ValueFloat:
			return new ValueFloat(value, mapValues);
		case ValueDefine.ValueInt:
			return new ValueInt(value, mapValues);
		case ValueDefine.ValueLong:
			return new ValueLong(value, mapValues);
		case ValueDefine.ValueBoolean:
			return new ValueBoolean(value, mapValues);
		case ValueDefine.ValueBooleanFalse:
			return new ValueBooleanFalse(value, mapValues);
		case ValueDefine.ValueString:
			return new ValueString(value, mapValues);
		case ValueDefine.ValueIntSet:
			return new ValueIntSet(value, mapValues);
		default:
			return null;
		}
	}

	/**
	 * 获取数值
	 * 
	 * @param value
	 * @return
	 */
	public static ValueBase getFightValue(String value) {
		// 填充实体数据
		List<List<String>> allList = ValueBase.stringToList(value);
		Map<Long, ValueBase> mapValues = new HashMap<>();
		for (int i = 0; i < allList.size(); i++) {
			List<String> list = allList.get(i);
			long id = Utils.longValue(list.get(0));
			String type = list.get(1);
			String param = list.get(2);
			ValueBase v = ValueFactory.getFightValue(type, param, mapValues);
			mapValues.put(id, v);
		}
		return mapValues.get(0L);
	}

	/**
	 * 根据参数类型获取数值
	 * 
	 * @param value
	 * @return
	 */
	public static ValueBase getFightValueByParam(double value) {
		return new ValueDouble(value);
	}

	/**
	 * 根据参数类型获取数值
	 * 
	 * @param value
	 * @return
	 */
	public static ValueBase getFightValueByParam(float value) {
		return new ValueFloat(value);
	}

	/**
	 * 根据参数类型获取数值
	 * 
	 * @param value
	 * @return
	 */
	public static ValueBase getFightValueByParam(int value) {
		return new ValueInt(value);
	}

	/**
	 * 根据参数类型获取数值
	 * 
	 * @param value
	 * @return
	 */
	public static ValueBase getFightValueByParam(long value) {
		return new ValueLong(value);
	}

	/**
	 * 根据参数类型获取数值
	 * 
	 * @param value
	 * @return
	 */
	public static ValueBase getFightValueByParam(boolean value) {
		return new ValueBoolean(value);
	}

	/**
	 * 根据参数类型获取数值
	 * 
	 * @param value
	 * @return
	 */
	public static ValueBase getFightValueByParam(String value) {
		return new ValueString(value);
	}
}
