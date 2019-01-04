package core.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.Chunk;
import core.Record;
import core.interfaces.IRecord;
import core.support.BufferPool;

public class CachedRecords {
	private Map<Long, Record> records = new HashMap<>(); // 数据 <ID, 实际数据>

	/**
	 * 新增
	 * @param id
	 * @param record
	 */
	public void put(long id, Record record) {
		records.put(id, record);
	}

	/**
	 * 清除
	 * @param id
	 * @return
	 */
	public Record remove(long id) {
		return records.remove(id);
	}

	/**
	 * 获取
	 * @param id
	 * @return
	 */
	public Record get(long id) {
		return records.get(id);
	}

	/**
	 * 获取缓存的ID列表
	 * @return
	 */
	public Set<Long> getIds() {
		return records.keySet();
	}

	/**
	 * 获取缓存的数据集合
	 * @return
	 */
	public Collection<Record> values() {
		return records.values();
	}

	/**
	 * 对给定的结果进行未入库数据修正
	 */
	public void pathUpdate(IRecord record) {
		if (record == null)
			return;

		// 主键
		long id = record.get("id");
		// 未入库修改
		Record r = records.get(id);
		if (r != null) {
			Chunk path = r.pathUpdateGen();
			record.patchUpdate(path, false);
			// 回收缓冲buff
			BufferPool.deallocate(path.buffer);
		}
	}

	/**
	 * 数据量是否为空
	 * @return
	 */
	public boolean isEmpty() {
		return records.isEmpty();
	}
}
