package core.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import core.db.VerNumber;
import core.dbsrv.DBLineService;
import core.support.Distr;
import core.support.SysException;
import core.support.log.LogCore;

public class VerTables {
	// 各执行队列版本信息<表名, <执行队列PortID, 执行队列版本>>
	private static Map<String, Map<String, VerNumber>> numbers = new ConcurrentHashMap<>();
	// 总分配服务当前执行队列版本<表名, 执行队列版本>
	private static Map<String, AtomicLong> versions = new ConcurrentHashMap<>();

	/**
	 * 初始化各执行版本号
	 * @param tables 数据库表集合
	 */
	public static void init(Collection<String> tables) {
		for (String t : tables) {
			// 各执行队列版本号
			Map<String, VerNumber> m = new ConcurrentHashMap<String, VerNumber>();
			for (int i = 0; i < Distr.PORT_STARTUP_NUM_DB_LINE; i++) {
				String p = Distr.PORT_DB_LINE_PREFIX + i;
				m.put(p, new VerNumber(t, p));
			}

			// 初始化表版本号
			versions.put(t, new AtomicLong());

			// 设置表与执行ID的对应
			numbers.put(t, m);
		}
	}

	/**
	 * 执行类操作 分配版本号提升
	 * @param tableName
	 * @param id
	 */
	public static void flush(String tableName, long id, long version) {
		if (version <= 0)
			return;

		// 执行队列PortId
		String linePortId = DBLineService.linePortId(tableName, id);

		Map<String, VerNumber> ns = numbers.get(tableName);
		if (ns == null) {
			throw new SysException("发现业务层在操作一张数据库中不存在表，请确认{}表的存在。", tableName);
		}

		// 设置操作队列版本号
		VerNumber vn = ns.get(linePortId);
		if (version > vn.flush) {
			vn.flush = version;
		} else {
			LogCore.db.error("设置刷新版本号时发现错误版本号，请检查：当前刷新版本号={}，新刷新版本号={}", vn.flush, version, new Throwable());
		}
	}

	/**
	 * 操作类操作 完成版本号提升
	 * @param tableName
	 * @param linePortId
	 * @param version
	 */
	public static void finish(String tableName, String linePortId, long version) {
		if (version <= 0)
			return;

		Map<String, VerNumber> ns = numbers.get(tableName);
		if (ns == null) {
			throw new SysException("发现业务层在操作一张数据库中不存在表，请确认{}表的存在。", tableName);
		}

		// 设置完成版本号 不允许版本回滚
		VerNumber vn = ns.get(linePortId);
		if (version > vn.finish) {
			vn.finish = version;
		} else {
			LogCore.db.error("设置完成版本号时发现错误版本号，请检查：当前完成版本号={}，新完成版本号={}", vn.finish, version);
		}
	}

	/**
	 * 增加执行版本号
	 * @param tableName
	 * @return
	 */
	public static long increment(String tableName) {
		AtomicLong vs = versions.get(tableName);
		if (vs == null) {
			throw new SysException("发现业务层在操作一张数据库中不存在表，请确认{}表的存在。", tableName);
		}

		return vs.incrementAndGet();
	}

	/**
	 * 任务都已完毕
	 * @return
	 */
	public static boolean isDone(String tableName) {
		// 只要有一个队列未准备好，就无法切换操作模式
		Collection<VerNumber> ls = numbers.get(tableName).values();
		for (VerNumber l : ls) {
			if (!l.isDone()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 获取版本信息摘要
	 * @return
	 */
	public static String versionDigest(String tableName) {
		StringBuilder rst = new StringBuilder();

		// 记录各队列版本号
		for (Entry<String, VerNumber> e : numbers.get(tableName).entrySet()) {
			String id = e.getKey();
			VerNumber ver = e.getValue();

			rst.append("[").append(id).append("=").append(ver.finish).append("/").append(ver.flush).append("]");
		}

		return rst.toString();
	}

	/**
	 * 获取未完成的队列版本信息
	 * @param linePortId
	 * @return
	 */
	public static List<VerNumber> getUndone(String linePortId) {
		return numbers.values().stream().flatMap(v -> v.values().stream()).filter(v -> !v.isDone())
				.filter(v -> v.linePortId.equals(linePortId)).collect(Collectors.toList());
	}
}
