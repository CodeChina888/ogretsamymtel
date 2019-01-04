package core.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.Record;
import core.db.CachedRecords;

/**
 * 数据表级缓存
 */
public class CachedTables {
	// <表名, 缓存数据>
	private Map<String, CachedRecords> datas = new HashMap<String, CachedRecords>();

	/**
	 * 获取数据表的缓存数据
	 * @param tableName
	 * @return
	 */
	public CachedRecords getOrCreate(String tableName) {
		CachedRecords result = datas.get(tableName);

		if (result == null) {
			result = new CachedRecords();
			datas.put(tableName, result);
		}

		return result;
	}

	/**
	 * 获取缓存的所有表名
	 * @return
	 */
	public Set<String> getTableNames() {
		return datas.keySet();
	}

	/**
	 * 删除表缓存
	 * @return
	 */
	public boolean remove(String tableName) {
		return datas.isEmpty();
	}

	/**
	 * 获取缓存的所有表名
	 * @return
	 */
	public Collection<CachedRecords> values() {
		return datas.values();
	}

	/**
	 * 数据量是否为空
	 * @return
	 */
	public boolean isEmpty() {
		return datas.isEmpty();
	}

	/**
	 * 数据数量
	 * @return
	 */
	public int size() {
		return datas.size();
	}

	/**
	 * 删除数据表的缓存数据
	 * @param tableName
	 * @param id
	 * @return
	 */
	public Record removeRecord(String tableName, long id) {
		CachedRecords result = datas.get(tableName);
		if (result == null)
			return null;

		return result.remove(id);
	}
}
