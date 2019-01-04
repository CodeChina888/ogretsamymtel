package core.db;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import core.Chunk;
import core.Record;
import core.interfaces.IRecord;
import core.support.BufferPool;

public class FlushingRecords {
	// 数据 <ID, <刷新版本号, 实际数据>>
	private Map<Long, LinkedHashMap<Long, Record>> records = new HashMap<>();

	/**
	 * 新增
	 * @param id
	 * @param record
	 */
	public void put(long id, long version, Record record) {
		LinkedHashMap<Long, Record> rs = records.get(id);
		if (rs == null) {
			rs = new LinkedHashMap<>();
			records.put(id, rs);
		}

		// 增加数据
		rs.put(version, record);
	}

	/**
	 * 清除
	 * @param id
	 * @return
	 */
	public Record remove(long id, long version) {
		Map<Long, Record> rs = records.get(id);
		if (rs == null)
			return null;

		// 删除
		Record result = rs.remove(version);

		// 清理空包
		if (rs.isEmpty()) {
			records.remove(id);
		}

		return result;
	}

	/**
	 * 获取
	 * @param id
	 * @return
	 */
	public Record get(long id, long version) {
		Map<Long, Record> rs = records.get(id);
		if (rs == null)
			return null;

		// 返回值
		return rs.get(version);
	}

	/**
	 * 对给定的结果进行未入库数据修正
	 * @param record
	 */
	public void pathUpdate(IRecord record) {
		if (record == null)
			return;

		// 主键
		long id = record.get("id");
		// 未入库修改
		LinkedHashMap<Long, Record> paths = records.get(id);
		if (paths == null)
			return;

		for (Record r : paths.values()) {
			// 恢复数据
			Chunk path = r.pathUpdateGen();
			try {
				record.patchUpdate(path, false);
			} finally {
				// 回收缓冲buff
				BufferPool.deallocate(path.buffer);
			}
		}
	}

	/**
	 * 获取缓存的ID列表
	 * @return
	 */
	public Set<Long> getIds() {
		return records.keySet();
	}
}
