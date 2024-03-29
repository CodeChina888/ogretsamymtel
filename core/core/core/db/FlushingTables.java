package core.db;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import core.db.FlushingRecords;

/**
 * 刷新中的写入缓存
 */
public class FlushingTables {
	// 刷新中的数据<表名, 缓存数据>
	private Map<String, FlushingRecords> datas = new ConcurrentHashMap<String, FlushingRecords>();

	/**
	 * 获取数据表的缓存数据
	 * @param tableName
	 * @return
	 */
	public FlushingRecords getOrCreate(String tableName) {
		FlushingRecords result = datas.get(tableName);

		if (result == null) {
			result = new FlushingRecords();
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
}
