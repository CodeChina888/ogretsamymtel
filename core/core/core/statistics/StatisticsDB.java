package core.statistics;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import core.support.Config;
import core.support.Utils;
import core.support.log.LogCore;

/**
 * 执行信息统计 生产环境中不建议开启，会影响系统性能。
 */
public class StatisticsDB {
	// 是否记录过信息
	private static boolean logged = false;
	// <表名, 数据>
	private static Map<String, DATA> data = new ConcurrentHashMap<>();

	/**
	 * 统计数据
	 */
	private static class DATA {
		// 表名
		public final String table;
		// 新增次数
		public final AtomicLong insertCount = new AtomicLong();
		// 删除次数
		public final AtomicLong deleteCount = new AtomicLong();
		// 查询次数
		public final AtomicLong queryCount = new AtomicLong();
		// 更新次数
		public final AtomicLong updateCount = new AtomicLong();
		// 玩家自定义SQL次数
		public final AtomicLong sqlCount = new AtomicLong();

		// 更新写入缓存次数
		public final AtomicLong patchCount = new AtomicLong();
		// 更新写入缓存大小
		public final AtomicLong patchSize = new AtomicLong();

		/**
		 * 构造函数
		 * @param table
		 */
		public DATA(String table) {
			this.table = table;
			// 设定已记录过信息
			logged = true;
		}

		/**
		 * 操作数量 包括增删改查等操作的总和
		 * @return
		 */
		public long exeCount() {
			return insertCount.get() + deleteCount.get() + queryCount.get() + updateCount.get() + sqlCount.get();
		}
	}

	/**
	 * 统计更新写入缓存事件
	 */
	public static void patch(String tableName, int length) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = data.computeIfAbsent(tableName, k -> new DATA(k));
		d.patchCount.incrementAndGet();
		d.patchSize.addAndGet(length);
	}

	/**
	 * 统计新增事件
	 */
	public static void insert(String tableName) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = data.computeIfAbsent(tableName, k -> new DATA(k));
		d.insertCount.incrementAndGet();
	}

	/**
	 * 统计删除事件
	 */
	public static void delete(String tableName) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = data.computeIfAbsent(tableName, k -> new DATA(k));
		d.deleteCount.incrementAndGet();
	}

	/**
	 * 统计更新事件
	 */
	public static void update(String tableName) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = data.computeIfAbsent(tableName, k -> new DATA(k));
		d.updateCount.incrementAndGet();
	}

	/**
	 * 统计查询事件
	 */
	public static void query(String tableName) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = data.computeIfAbsent(tableName, k -> new DATA(k));
		d.queryCount.incrementAndGet();
	}

	/**
	 * 统计自定义SQL事件
	 */
	public static void sql(String tableName) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = data.computeIfAbsent(tableName, k -> new DATA(k));
		d.sqlCount.incrementAndGet();
	}

	/**
	 * 生成统计结果
	 * @return
	 */
	public static void showResult() {
		if (!Config.STATISTICS_ENABLE)
			return;
		if (!logged)
			return;

		// 返回信息模板
		String rst = "\n========================\n" + "==  游戏运行统计 - 数据库执行  ==\n" + "========================\n"
				+ "写入缓存总数{}次, 高占比列表:\n" + "次数\t总大小\t均大小\t数量%\t表名\n" + "{}\n" + "写入缓存总大小{}, 高占比列表:\n"
				+ "总大小\t次数\t均大小\t大小%\t表名\n" + "{}\n" + "表总操作{}次, 高占比列表:\n" + "总次\t新增\t修改\t删除\t查询\t自定\t表名\n" + "{}";

		// 数据
		Collection<DATA> ds = data.values();

		// 1.1 写入缓存总数
		long patchCountTotal = ds.stream().mapToLong(d -> d.patchCount.get()).sum();

		// 1.2 写入缓存数量TOP10
		StringBuilder patchCount = new StringBuilder();
		ds.stream()
				.sorted((a, b) -> Long.compare(b.patchCount.get(), a.patchCount.get()))
				.filter(d -> d.patchCount.get() > 0)
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							patchCount.append(d.patchCount.get()).append("\t")
									.append(Utils.formatByteSize(d.patchSize.get())).append("\t")
									.append(Utils.formatByteSize(d.patchSize.get() / d.patchCount.get())).append("\t")
									.append(String.format("%.2f", 100.0 * d.patchCount.get() / patchCountTotal))
									.append("%").append("\t").append(d.table).append("\n");
						});

		// 2.1 写入缓存总数
		long patchSizeTotal = ds.stream().mapToLong(d -> d.patchSize.get()).sum();

		// 2.2 写入缓存数量TOP10
		StringBuilder patchSize = new StringBuilder();
		ds.stream()
				.sorted((a, b) -> Long.compare(b.patchSize.get(), a.patchSize.get()))
				.filter(d -> d.patchCount.get() > 0)
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							patchSize.append(Utils.formatByteSize(d.patchSize.get())).append("\t")
									.append(d.patchCount.get()).append("\t")
									.append(Utils.formatByteSize(d.patchSize.get() / d.patchCount.get())).append("\t")
									.append(String.format("%.2f", 100.0 * d.patchSize.get() / patchSizeTotal))
									.append("%").append("\t").append(d.table).append("\n");
						});

		// 3.1 操作总数
		long exeTotal = ds.stream().mapToLong(d -> d.exeCount()).sum();

		// 3.2 操作数量TOP20
		StringBuilder exeCount = new StringBuilder();
		ds.stream()
				.sorted((a, b) -> Long.compare(b.exeCount(), a.exeCount()))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							exeCount.append(d.exeCount()).append("\t").append(d.insertCount).append("\t")
									.append(d.updateCount).append("\t").append(d.deleteCount).append("\t")
									.append(d.queryCount).append("\t").append(d.sqlCount).append("\t").append(d.table)
									.append("\n");
						});

		// 4 输出结果
		rst = Utils.createStr(rst, patchCountTotal, patchCount.toString(), Utils.formatByteSize(patchSizeTotal),
				patchSize.toString(), exeTotal, exeCount);

		LogCore.statis.info(rst);
	}
}
