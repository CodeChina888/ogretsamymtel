package core.db;

import static core.db.QueueStatusKey.S_EXEC;
import static core.db.QueueStatusKey.S_NONE;
import static core.db.QueueStatusKey.S_QUERY;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import core.db.QueueStatusKey;
import core.db.QueueTypeKey;
import core.db.QueueUnit;
import core.db.VerTables;
import core.statistics.StatisticsDB;
import core.support.log.LogCore;

/**
 * 数据库表SQL执行队列
 */
public class QueueTables {
	// 待执行队列<表名, 执行队列>
	private Map<String, LinkedList<QueueUnit>> datas = new ConcurrentHashMap<>();
	// 各表当前的状态<表名, 执行/查询/无>
	private Map<String, QueueStatusKey> status = new ConcurrentHashMap<>();

	// 已执行命令数量
	private long numSQL;
	// 状态阻塞命令数量
	private long numBlock;

	/**
	 * 获取数据表名称集合
	 * @return
	 */
	public Set<String> getTableNames() {
		return datas.keySet();
	}

	/**
	 * 获取一个可执行的SQL语句 当没有可执行的语句后 返回null
	 * @param tableName
	 * @return
	 */
	public QueueUnit pop(String tableName) {
		// 可执行队列
		LinkedList<QueueUnit> ds = getUnits(tableName);
		if (ds.isEmpty())
			return null;

		// 待执行语句
		QueueUnit unit = ds.get(0);

		// 当前状态
		QueueStatusKey stLast = getStatus(tableName);
		QueueStatusKey stUnit = unit.getStatus();

		// 返回值
		QueueUnit rst = null;

		// 可执行的情况
		// 当前执行与之前状态相同 or 无状态 可执行
		if (stLast == stUnit || stUnit == S_NONE || stLast == S_NONE) {
			rst = unit;
			// 需要转换状态时 可执行
		} else if (stUnit == S_EXEC && VerTables.isDone(tableName)) {
			rst = unit;
			// 需要转换状态时 可查询
		} else if (stUnit == S_QUERY && VerTables.isDone(tableName)) {
			rst = unit;
		} else {
			// 记录阻塞SQL数量
			numBlock++;
			// 阻塞日志
			if (LogCore.db.isInfoEnabled()) {
				LogCore.db.info("等待切换执行状态：之前状态={}, 执行状态={}, 版本信息={}, 语句={}, 参数={}", stLast, stUnit,
						VerTables.versionDigest(tableName), unit.getSql(), Arrays.toString(unit.getParams()));
			}
		}

		// 命中可执行时 删除对应任务
		if (rst != null) {
			ds.remove(0);

			// 设置执行中的版本号
			VerTables.flush(tableName, unit.getId(), rst.getVersion());
		}

		// 设置执行状态
		if (rst != null && stUnit != stLast && stUnit != S_NONE) {
			status.put(tableName, stUnit);
		}

		// 记录执行SQL数量
		numSQL++;

		return rst;
	}

	/**
	 * 增加新的执行队列
	 */
	public long put(String tableName, QueueUnit unit) {
		// 版本号
		long version = 0;

		// 有状态操作 需要刷新版本号
		if (unit.getStatus() != QueueStatusKey.S_NONE) {
			version = VerTables.increment(tableName);
			unit.version(version);
		}

		// 记录执行队列
		List<QueueUnit> list = getUnits(tableName);
		list.add(unit);

		// 统计运行数据
		if (QueueTypeKey.T_QUERY == unit.getType() || QueueTypeKey.T_COUNT == unit.getType()) {
			StatisticsDB.query(tableName);
		}

		return version;
	}

	/**
	 * 获取SQL执行队列
	 * @param tableName
	 * @return
	 */
	private LinkedList<QueueUnit> getUnits(String tableName) {
		return datas.computeIfAbsent(tableName, k -> new LinkedList<>());
	}

	/**
	 * 获取执行状态
	 * @param tableName
	 * @return
	 */
	private QueueStatusKey getStatus(String tableName) {
		return status.computeIfAbsent(tableName, k -> QueueStatusKey.S_NONE);
	}

	public long getNumSQL() {
		return numSQL;
	}

	public long getNumBlock() {
		return numBlock;
	}
}
