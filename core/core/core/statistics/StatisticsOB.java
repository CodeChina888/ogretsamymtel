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
public class StatisticsOB {
	// 是否记录过信息
	private static boolean logged = false;
	// 接收
	private static Map<String, DATA> event = new ConcurrentHashMap<>();
	private static Map<String, DATA> msg = new ConcurrentHashMap<>();

	/**
	 * 统计数据
	 */
	private static class DATA {
		// OB关键字
		public final String key;
		// 抛出次数
		public final AtomicLong countCast = new AtomicLong();
		// 接收次数
		public final AtomicLong countCatch = new AtomicLong();
		// 耗时
		public final AtomicLong nanoTime = new AtomicLong();
		// 最高耗时
		public long maxTime = 0L;

		public DATA(String key) {
			this.key = key;
			// 设定已记录过信息
			logged = true;
		}
	}

	/**
	 * 统计Event事件消耗
	 */
	public static void event(String key, long countCatch, long consum) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = event.computeIfAbsent(key, k -> new DATA(k));
		d.countCast.incrementAndGet();
		d.countCatch.addAndGet(countCatch);
		d.nanoTime.addAndGet(consum);
		d.maxTime = consum > d.maxTime ? consum : d.maxTime;
	}

	/**
	 * 统计Msg事件消耗
	 */
	public static void msg(String key, long consum) {
		if (!Config.STATISTICS_ENABLE)
			return;

		DATA d = msg.computeIfAbsent(key, k -> new DATA(k));
		d.countCast.incrementAndGet();
		d.nanoTime.addAndGet(consum);
		d.maxTime = consum > d.maxTime ? consum : d.maxTime;
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
		String rst = "\n========================\n" + "==  游戏运行统计 - OB类事件  ==\n" + "========================\n"
				+ "Event总耗时{}, 高占比列表:\n" + "耗时\t抛次数\t接次数\t均耗时\t耗时%\t最大耗时\t关键字\n" + "{}\n" + "Msg总耗时{}, 高占比列表:\n"
				+ "耗时\t抛次数\t均耗时\t耗时%\t最大耗时\t关键字\n" + "{}";

		// 1 接收消息数据
		Collection<DATA> es = event.values();

		// 1.1 Event总耗时
		long esTimeTotal = es.stream().mapToLong(d -> d.nanoTime.get()).sum();

		// 1.2 Event耗时TOP10
		StringBuilder esTime = new StringBuilder();
		es.stream()
				.sorted((a, b) -> Long.compare(b.nanoTime.get() - b.maxTime, a.nanoTime.get() - a.maxTime))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							esTime.append(Utils.formatNanoTime(d.nanoTime.get())).append("\t").append(d.countCast).append("\t")
									.append(d.countCatch).append("\t")
									.append(Utils.formatNanoTime((d.nanoTime.get() - d.maxTime) / d.countCast.get()))
									.append("\t").append(String.format("%.2f", 100.0 * d.nanoTime.get() / esTimeTotal))
									.append("%").append("\t").append(Utils.formatNanoTime(d.maxTime)).append("\t").append(d.key)
									.append("\n");
						});

		// 2 接收消息数据
		Collection<DATA> ms = msg.values();

		// 2.1 MSG总耗时
		long msTimeTotal = ms.stream().mapToLong(d -> d.nanoTime.get()).sum();

		// 2.2 MSG耗时TOP10
		StringBuilder msTime = new StringBuilder();
		ms.stream()
				.sorted((a, b) -> Long.compare(b.nanoTime.get() - b.maxTime, a.nanoTime.get() - a.maxTime))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							msTime.append(Utils.formatNanoTime(d.nanoTime.get())).append("\t").append(d.countCast).append("\t")
									.append(Utils.formatNanoTime((d.nanoTime.get() - d.maxTime) / d.countCast.get()))
									.append("\t").append(String.format("%.2f", 100.0 * d.nanoTime.get() / msTimeTotal))
									.append("%").append("\t").append(Utils.formatNanoTime(d.maxTime)).append("\t").append(d.key)
									.append("\n");
						});

		// 3 输出结果
		rst = Utils.createStr(rst, Utils.formatNanoTime(esTimeTotal), esTime.toString(), Utils.formatNanoTime(msTimeTotal),
				msTime.toString());

		LogCore.statis.info(rst);
	}
}
