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
public class StatisticsRPC {
	// 是否记录过信息
	private static boolean logged = false;
	// PRC调用处理数据
	private static Map<String, DATA> rpc = new ConcurrentHashMap<>();
	// PRC回调处理数据
	private static Map<String, DATA> result = new ConcurrentHashMap<>();

	/**
	 * 统计数据
	 */
	private static class DATA {
		// 方法关键字
		public final String key;
		// 被调用次数
		public final AtomicLong count = new AtomicLong();
		// 总执行时间(纳秒)
		public final AtomicLong nanoTime = new AtomicLong();
		// 最高耗时
		public long maxTime = 0L;

		/**
		 * 构造函数
		 * @param key
		 */
		public DATA(String key) {
			this.key = key;
			// 设定已记录过信息
			logged = true;
		}
	}

	/**
	 * 统计PRC调用信息
	 */
	public static void rpc(String key, long nanoTime) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = rpc.computeIfAbsent(key, k -> new DATA(k));
		d.count.incrementAndGet();
		d.nanoTime.addAndGet(nanoTime);
		d.maxTime = nanoTime > d.maxTime ? nanoTime : d.maxTime;
	}

	/**
	 * 统计PRC回调信息
	 */
	public static void rst(String key, long nanoTime) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = result.computeIfAbsent(key, k -> new DATA(k));
		d.count.incrementAndGet();
		d.nanoTime.addAndGet(nanoTime);
		d.maxTime = nanoTime > d.maxTime ? nanoTime : d.maxTime;
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
		String rst = "\n========================\n" + "==  游戏运行统计 - RPC调用  ==\n" + "========================\n"
				+ "主动调用总耗时{}, 高占比列表:\n" + "总耗时\t次数\t均耗时\t耗时%\t最大耗时\t关键字\n" + "{}\n" + "请求回调总耗时{}, 高占比列表:\n"
				+ "总耗时\t次数\t均耗时\t耗时%\t最大耗时\t关键字\n" + "{}";

		// 1 主动调用数据
		Collection<DATA> rs = rpc.values();

		// 1.1 RPC调用耗时
		long rpcTimeTotal = rs.stream().mapToLong(d -> d.nanoTime.get() - d.maxTime).sum();

		// 1.2 RPC
		StringBuilder rpcTime = new StringBuilder();
		rs.stream()
				.sorted((a, b) -> Long.compare(b.nanoTime.get() - b.maxTime, a.nanoTime.get() - a.maxTime))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							rpcTime.append(Utils.formatNanoTime(d.nanoTime.get())).append("\t").append(d.count.get())
									.append("\t").append(Utils.formatNanoTime((d.nanoTime.get() - d.maxTime) / d.count.get()))
									.append("\t")
									.append(String.format("%.2f", 100.0 * d.nanoTime.get() / rpcTimeTotal)).append("%")
									.append("\t").append(Utils.formatNanoTime(d.maxTime)).append("\t").append(d.key).append("\n");
						});

		// 2 RST回调数据
		Collection<DATA> ls = result.values();

		// 2.1 RST回调耗时
		long lsTimeTotal = ls.stream().mapToLong(d -> d.nanoTime.get()).sum();

		// 2.2 RST
		StringBuilder lsTime = new StringBuilder();
		ls.stream()
				.sorted((a, b) -> Long.compare(b.nanoTime.get(), a.nanoTime.get()))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							lsTime.append(Utils.formatNanoTime(d.nanoTime.get())).append("\t").append(d.count.get()).append("\t")
									.append(Utils.formatNanoTime((d.nanoTime.get() - d.maxTime) / d.count.get())).append("\t")
									.append(String.format("%.2f", 100.0 * d.nanoTime.get() / lsTimeTotal)).append("%")
									.append("\t").append(Utils.formatNanoTime(d.maxTime)).append("\t").append(d.key).append("\n");
						});

		// 3 输出结果
		rst = Utils.createStr(rst, Utils.formatNanoTime(rpcTimeTotal), rpcTime.toString(), Utils.formatNanoTime(lsTimeTotal),
				lsTime.toString());

		LogCore.statis.info(rst);
	}
}
