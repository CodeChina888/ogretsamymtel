package game.worldsrv.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * 通用属性累加，可指定KEY，值为Double
 */
public abstract class PropCalcBase<K>{
	protected final Map<K, Double> datas = new HashMap<>();
	
	protected abstract K toKey(String key);
	
	public PropCalcBase() {
		
	}

	public PropCalcBase(String json) {
		if (StringUtils.isBlank(json)) {
			return;
		}
		
		JSONObject jo = Utils.toJSONObject(json);
		for (Entry<String, Object> entry : jo.entrySet()) {
			K k = this.toKey(entry.getKey());
			Object v = entry.getValue();
			if (null != k && null != v) {
				datas.put(k, Utils.doubleValue(v));
			}
		}
	}
	
	@Override
	public String toString() {
		return toJSONStr();
	}
	
	/**
	 * 转换为可储存的文本格式
	 * @return
	 */
	public String toJSONStr() {
		// 去除所有可以忽略的选择
		Iterator<Map.Entry<K, Double>> it = datas.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<K, Double> entry = it.next();
			if (canDiscard(entry.getValue())) {
				it.remove();
			}
		}
		return Utils.toJSONString(datas);
	}
	
	/**
	 * 是否可抛弃的值
	 * @param value
	 * @return
	 */
	protected boolean canDiscard(Double value) {
		if (value != null) {
			double temp = (double)value;
			if (temp < 1e-06 && temp > -1e-06) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 合并属性
	 * @param plus
	 * @return
	 */
	public PropCalcBase<K> merge(PropCalcBase<K> plus) {
		Map<K, Double> datasNew = plus.datas;
		for (Entry<K, Double> entry : datasNew.entrySet()) {
			K k = entry.getKey();
			Double v = entry.getValue();
			add(k, v);// 累加属性
		}
		return this;
	}
	
	/**
	 * 累加属性：存在累加，不存在新加
	 */
	public PropCalcBase<K> add(K key, Object value) {
		Double v = getValue(key);
		if (null != v) {// key存在才累加值
			if (null != value) {
				datas.replace(key, v + Utils.doubleValue(value));// 存在累加
			}
		} else {
			datas.put(key, Utils.doubleValue(value));// 不存在新加
		}
		return this;
	}
	/**
	 * 累加属性
	 */
	public PropCalcBase<K> add(PropCalcBase<K> plus) {
		Map<K, Double> datasNew = plus.datas;
		for (Entry<K, Double> entry : datasNew.entrySet()) {
			K k = entry.getKey();
			Double v = entry.getValue();
			// 累加属性
			add(k, v);
		}
		return this;
	}
	/**
	 * 累加属性
	 */
	public PropCalcBase<K> add(String json) {
		if (StringUtils.isBlank(json))
			return this;
		
		JSONObject jo = Utils.toJSONObject(json);
		if (jo != null) {
			for (Entry<String, Object> entry : jo.entrySet()) {
				K k = this.toKey(entry.getKey());
				Object v = entry.getValue();
				if (null != k && null != v) {
					add(k, v);
				}
			}
		}
		return this;
	}
	/**
	 * 累加属性
	 */
	public PropCalcBase<K> add(Map<String, Double> map) {
		if (map == null || map.isEmpty())
			return this;
		
		for (Entry<String, Double> entry : map.entrySet()) {
			K k = this.toKey(entry.getKey());
			if (k != null) {
				add(k, entry.getValue());
			}
		}
		return this;
	}
	/**
	 * 累加属性
	 */
	public PropCalcBase<K> add(String[] propKey, int[] propValue) {
		if (propKey != null && propValue != null && propKey.length == propValue.length) {
			for (int i = 0; i < propKey.length; i++) {
				K k = this.toKey(propKey[i]);
				if (k != null) {
					add(k, propValue[i]);
				}
			}
		}
		return this;
	}
	
	/**
	 * 减去属性
	 */
	public PropCalcBase<K> subtract(K key, Object value) {
		Double v = getValue(key);
		if (null != v) {// key存在则减去
			if (null != value) {
				datas.replace(key, v - Utils.doubleValue(value)); // 存在则减去
			}
		} 
		return this;
	}
	/**
	 * 减去属性
	 */
	public PropCalcBase<K> subtract(K key, Double value) {
		Double v = getValue(key);
		if (null != v && null != value) {// key存在则减去
			datas.replace(key, v - value);
		}
		return this;
	}
	/**
	 * 减去属性
	 */
	public PropCalcBase<K> subtract(PropCalcBase<K> plus) {
		Map<K, Double> datasNew = plus.datas;
		for (Entry<K, Double> entry : datasNew.entrySet()) {
			K k = entry.getKey();
			Double v = entry.getValue();
			// 累加属性
			subtract(k, v);
		}
		return this;
	}
	/**
	 * 减去属性
	 */
	public PropCalcBase<K> subtract(String[] propKey, int[] propValue) {
		if (propKey != null && propValue != null && propKey.length == propValue.length) {
			for (int i = 0; i < propKey.length; i++) {
				K k = this.toKey(propKey[i]);
				if (k != null) {
					subtract(k, propValue[i]);
				}
			}
		}
		return this;
	}
	
	/**
	 * 乘以属性
	 */
	public PropCalcBase<K> multiply(K key, double value) {
		Double v = getValue(key);
		if (null != v) {
			datas.replace(key, v * value);
		}
		return this;
	}
	/**
	 * 乘以属性
	 */
	public PropCalcBase<K> multiply(String[] propKey, float[] propValue) {
		if (null != propKey && null != propValue && propKey.length == propValue.length) {
			for (int i = 0; i < propKey.length; i++) {
				K k = this.toKey(propKey[i]);
				if (null != k) {
					multiply(k, propValue[i]);
				}
			}
		}
		return this;
	}
	
	/**
	 * 乘以属性
	 */
	public PropCalcBase<K> multiply(String[] keys, int value) {
		if (null != keys) {
			for (int i = 0; i < keys.length; i++) {
				K k = this.toKey(keys[i]);
				if (null != k) {
					multiply(k, value);
				}
			}
		}
		return this;
	}
	
	/**
	 * 移除某个属性
	 */
	public PropCalcBase<K> remove(K key) {
		datas.remove(key);
		return this;
	}
	/**
	 * 移除所有数据
	 */
	public PropCalcBase<K> removeAll() {
		datas.clear();
		return this;
	}

	/**
	 * 添加属性
	 */
	public PropCalcBase<K> put(K key, Object value) {
		datas.put(key, Utils.doubleValue(value));
		return this;
	}

	/**
	 * 获取指定key的值
	 */
	public Double getValue(K key) {
		Double v = null;
		if (datas.containsKey(key))
			v = datas.get(key);
		return v;
	}
	
	/**
	 * 获取int型数值
	 */
	public int getInt(K key) {
		Double v = datas.get(key);
		if (null == v)
			return 0;
		else {
			if (v > Integer.MAX_VALUE) {// 比最大值大，则返回最大值
				Log.human.error("===PropCalcBase.getInt({})={} > Integer.MAX_VALUE", key, v.longValue());
				return Integer.MAX_VALUE;
			} else {
				return v.intValue();
			}
		}
	}
	
	/**
	 * 获取long型数值
	 */
	public long getLong(K key) {
		Double v = datas.get(key);
		if (null == v)
			return 0L;
		else
			return v.longValue();
	}

	/**
	 * 获取float型数值
	 */
	public float getFloat(K key) {
		Double v = datas.get(key);
		if (null == v)
			return 0.0F;
		else
			return v.floatValue();
	}
	
	/**
	 * 获取double型数值
	 */
	public double getDouble(K key) {
		Double v = datas.get(key);
		if (null == v)
			return 0.0D;
		else
			return v.doubleValue();
	}

	/**
	 * 获取当前键值的Map对象
	 * @return
	 */
	public Map<K, Double> getDatas() {
		return datas;
	}
	
}
