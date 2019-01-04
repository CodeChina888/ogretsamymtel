package turnbasedsrv.value;

import java.util.ArrayList;
import java.util.List;

public abstract class ValueBase {

	/**
	 * 获取值的类型
	 * 
	 * @return
	 */
	public abstract String getType();

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	public abstract String toString();

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	public abstract void setValue(String value);

	/**
	 * 初始化值
	 * 
	 * @param value
	 */
	public abstract void setValue(ValueBase value);

	/**
	 * 获取值拷贝
	 * 
	 * @return
	 */
	public abstract ValueBase getCopy();

	/**
	 * 是否相等
	 * 
	 * @param value
	 * @return
	 */
	public abstract boolean equals(ValueBase prop);

	/**
	 * 值加上一个int固定值
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase addInt(int value) {
		return this;
	}

	/**
	 * 值相加
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase add(ValueBase value) {
		return this;
	}

	/**
	 * 值相减
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase sub(ValueBase value) {
		return this;
	}

	/**
	 * 值相乘
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase multiply(ValueBase value) {
		return this;
	}

	/**
	 * 值相除
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase divide(ValueBase value) {
		return this;
	}

	/**
	 * 值幂
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase power(ValueBase value) {
		return this;
	}

	/**
	 * 值模
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase modle(ValueBase value) {
		return this;
	}

	/**
	 * 值下整
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase floor() {
		return this;
	}

	/**
	 * 值上整
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase ceil() {
		return this;
	}

	/**
	 * 值绝对值
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase abs() {
		return this;
	}

	/**
	 * 值开方
	 * 
	 * @param value
	 * @return
	 */
	public ValueBase sqrt() {
		return this;
	}

	/**
	 * 是否是布尔值
	 * 
	 * @return
	 */
	public boolean isBooleanValue() {
		return false;
	}

	/**
	 * 取得布尔值
	 * 
	 * @return
	 */
	public boolean getBooleanValue() {
		return false;
	}

	/**
	 * 是否是字符串
	 * 
	 * @return
	 */
	public boolean isStringValue() {
		return false;
	}

	/**
	 * 取得字符串
	 * 
	 * @return
	 */
	public String getStringValue() {
		return "";
	}

	/**
	 * 是否是数值
	 * 
	 * @return
	 */
	public boolean isNumberValue() {
		return false;
	}

	/**
	 * 取得数值
	 * 
	 * @return
	 */
	public double getNumberValue() {
		return 0;
	}

	/**
	 * 比较两个数值是否相等,精度为0.01
	 * 
	 * @param value1
	 * @param value2
	 * @return
	 */
	public static boolean isNumberValueEquals(ValueBase value1, ValueBase value2) {
		if (!value1.isNumberValue() || !value2.isNumberValue()) {
			return false;
		}
		double v1 = value1.getNumberValue();
		double v2 = value2.getNumberValue();
		if (Math.abs(v1 - v2) <= 0.01d) {
			return true;
		}
		return false;
	}

	/**
	 * 比较两个数值是否相等,精度为0.01
	 * 
	 * @param value
	 * @return
	 */
	public boolean isNumberValueEquals(ValueBase value) {
		if (!isNumberValue() || !value.isNumberValue()) {
			return false;
		}
		double v1 = getNumberValue();
		double v2 = value.getNumberValue();
		if (Math.abs(v1 - v2) <= 0.01d) {
			return true;
		}
		return false;
	}

	/**
	 * 比较两个数值是否大小,精度为0.01,小于-1,等于0,大于1
	 * 
	 * @param value
	 * @return
	 */
	public int compareTo(ValueBase value) {
		if (!isNumberValue() || !value.isNumberValue()) {
			return -1;
		}
		double v1 = getNumberValue();
		double v2 = value.getNumberValue();
		double diff = v1 - v2;
		if (Math.abs(v1 - v2) <= 0.01d) {
			return 0;
		}
		if (diff < 0.0d) {
			return -1;
		}
		return 1;
	}

	/**
	 * 将 {long,int,string},{long,int,string},...格式的字符串转成List<List<String>>
	 * 
	 * @param str
	 * @return
	 */
	public static List<List<String>> stringToList(String str) {
		if (str == null || str.isEmpty()) {
			return new ArrayList<>();
		}
		List<List<String>> result = new ArrayList<>();
		int pos1 = str.indexOf('{');
		int pos2 = 0;
		while (pos1 >= 0) {
			List<String> list = new ArrayList<>();
			pos2 = str.indexOf(',', pos1 + 1);
			list.add(str.substring(pos1 + 1, pos2));
			pos1 = str.indexOf(',', pos2 + 1);
			list.add(str.substring(pos2 + 1, pos1));
			pos2 = str.indexOf('"', pos1 + 1);
			pos1 = str.indexOf('"', pos2 + 1);
			list.add(str.substring(pos2 + 1, pos1));
			pos1 = str.indexOf('{', pos1 + 1);
			result.add(list);
		}
		return result;
	}

}
