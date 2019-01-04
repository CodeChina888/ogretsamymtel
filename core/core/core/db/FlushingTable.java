package core.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.db.FlushingRecords;

/**
 * 写入缓存 刷新中的 临时缓存 好吧 我承认名称有点绕
 */
public class FlushingTable {
	// <表名, 缓存数据>
	private Map<String, FlushingRecords> datas = new HashMap<String, FlushingRecords>();

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
