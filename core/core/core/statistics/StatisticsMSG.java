package core.statistics;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import core.support.Config;
import core.support.Utils;
import core.support.log.LogCore;
import io.netty.buffer.ByteBuf;

/**
 * 执行信息统计 生产环境中不建议开启，会影响系统性能。
 */
public class StatisticsMSG {
	// 是否记录过信息
	private static boolean logged = false;

	// 接收
	private static Map<Integer, DATA> recevie = new ConcurrentHashMap<>();
	private static Map<Integer, DATA> send = new ConcurrentHashMap<>();

	/**
	 * 统计数据
	 */
	private static class DATA {
		public final Integer msgId;
		public final AtomicLong count = new AtomicLong();
		public final AtomicLong size = new AtomicLong();
		public final AtomicLong sizeMax = new AtomicLong();
		public final AtomicLong sizeMin = new AtomicLong();

		public DATA(Integer msgId) {
			this.msgId = msgId;
			// 设定已记录过信息
			logged = true;
		}

		/**
		 * 消息均值
		 * @return
		 */
		public long sizeAvg() {
			long c = count.get();
			long s = size.get();

			if (c == 0)
				return 0;
			else
				return s / c;
		}
	}

	/**
	 * 接收消息
	 * @param bytes
	 */
	public static void recevice(byte[] bytes) {
		if (!Config.STATISTICS_ENABLE)
			return;

		// 消息长度
		int len = bytes.length;
		// 消息ID
		int msgId = Utils.bytesToInt(bytes, 4);

		// 统计
		DATA d = recevie.computeIfAbsent(msgId, id -> new DATA(id));
		d.count.incrementAndGet();
		d.size.addAndGet(len);
		d.sizeMax.updateAndGet(v -> Math.max(v, len));
		d.sizeMin.updateAndGet(v -> v == 0 ? len : Math.min(v, len));
	}

	/**
	 * 发送消息
	 */
	public static void send(ByteBuf buf) {
		if (!Config.STATISTICS_ENABLE)
			return;

		// 消息长度
		int len = buf.readInt();
		// 消息ID
		int msgId = buf.readInt();

		// 重置读取位置
		buf.resetReaderIndex();

		// 统计
		DATA d = send.computeIfAbsent(msgId, id -> new DATA(id));
		d.count.incrementAndGet();
		d.size.addAndGet(len);
		d.sizeMax.updateAndGet(v -> Math.max(v, len));
		d.sizeMin.updateAndGet(v -> v == 0 ? len : Math.min(v, len));
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
		String rst = "\n========================\n" + "==  游戏运行统计 - 消息协议  ==\n" + "========================\n"
				+ "收到总消息数量{}次, 高占比列表:\n" + "次数\t总大小\t均大小\t数量%\t消息ID\n" + "{}\n" + "收到总消息大小{}, 高占比列表:\n"
				+ "次数\t总大小\t均大小\t大小%\t消息ID\n" + "{}\n" + "收到消息均大小{}, 高占比列表:\n" + "次数\t总大小\t均大小\t最大\t最小\t消息ID\n"
				+ "{}\n" + "发送总消息数量{}次, 高占比列表:\n" + "次数\t总大小\t均大小\t数量%\t消息ID\n" + "{}\n" + "发送总消息大小{}, 高占比列表:\n"
				+ "次数\t总大小\t均大小\t大小%\t消息ID\n" + "{}\n" + "发送消息均大小{}, 高占比列表:\n" + "次数\t总大小\t均大小\t最大\t最小\t消息ID\n" + "{}";

		// 1 接收消息数据
		Collection<DATA> recVal = recevie.values();

		// 1.1 消息汇总
		long recCountTotal = recVal.stream().mapToLong(d -> d.count.get()).sum();
		long recSizeTotal = recVal.stream().mapToLong(d -> d.size.get()).sum();
		long recSizeAvg = (long) recVal.stream().mapToLong(DATA::sizeAvg).average().orElse(0);

		// 1.2 数量高占比列表
		StringBuilder recCount = new StringBuilder();
		recVal.stream()
				.sorted((a, b) -> Long.compare(b.count.get(), a.count.get()))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							recCount.append(d.count).append("\t").append(Utils.formatByteSize(d.size.get()))
									.append("\t").append(Utils.formatByteSize(d.sizeAvg())).append("\t")
									.append(String.format("%.2f", 100.0 * d.count.get() / recCountTotal)).append("%")
									.append("\t").append(d.msgId).append("\n");
						});

		// 1.3 大小高占比列表
		StringBuilder recSize = new StringBuilder();
		recVal.stream()
				.sorted((a, b) -> Long.compare(b.size.get(), a.size.get()))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							recSize.append(d.count).append("\t").append(Utils.formatByteSize(d.size.get()))
									.append("\t").append(Utils.formatByteSize(d.sizeAvg())).append("\t")
									.append(String.format("%.2f", 100.0 * d.size.get() / recSizeTotal)).append("%")
									.append("\t").append(d.msgId).append("\n");
						});

		// 1.4 均大小高占比列表
		StringBuilder recAvgSize = new StringBuilder();
		recVal.stream()
				.sorted((a, b) -> Long.compare(b.sizeMax.get(), a.sizeMax.get()))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							recAvgSize.append(d.count).append("\t").append(Utils.formatByteSize(d.size.get()))
									.append("\t").append(Utils.formatByteSize(d.sizeAvg())).append("\t")
									.append(Utils.formatByteSize(d.sizeMax.get())).append("\t")
									.append(Utils.formatByteSize(d.sizeMin.get())).append("\t").append(d.msgId)
									.append("\n");
						});

		// 2 接收消息数据
		Collection<DATA> sndVal = send.values();

		// 2.1 消息汇总
		long sndCountTotal = sndVal.stream().mapToLong(d -> d.count.get()).sum();
		long sndSizeTotal = sndVal.stream().mapToLong(d -> d.size.get()).sum();
		long sndSizeAvg = (long) sndVal.stream().mapToLong(DATA::sizeAvg).average().orElse(0);

		// 2.2 数量高占比列表
		StringBuilder sndCount = new StringBuilder();
		sndVal.stream()
				.sorted((a, b) -> Long.compare(b.count.get(), a.count.get()))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							sndCount.append(d.count).append("\t").append(Utils.formatByteSize(d.size.get()))
									.append("\t").append(Utils.formatByteSize(d.sizeAvg())).append("\t")
									.append(String.format("%.2f", 100.0 * d.count.get() / sndCountTotal)).append("%")
									.append("\t").append(d.msgId).append("\n");
						});

		// 2.3 大小高占比列表
		StringBuilder sndSize = new StringBuilder();
		sndVal.stream()
				.sorted((a, b) -> Long.compare(b.size.get(), a.size.get()))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							sndSize.append(d.count).append("\t").append(Utils.formatByteSize(d.size.get()))
									.append("\t").append(Utils.formatByteSize(d.sizeAvg())).append("\t")
									.append(String.format("%.2f", 100.0 * d.size.get() / sndSizeTotal)).append("%")
									.append("\t").append(d.msgId).append("\n");
						});

		// 2.4 均大小高占比列表
		StringBuilder sndAvgSize = new StringBuilder();
		sndVal.stream()
				.sorted((a, b) -> Long.compare(b.sizeMax.get(), a.sizeMax.get()))
				.limit(Config.STATISTICS_TOP_NUM)
				.forEach(
						d -> {
							sndAvgSize.append(d.count).append("\t").append(Utils.formatByteSize(d.size.get()))
									.append("\t").append(Utils.formatByteSize(d.sizeAvg())).append("\t")
									.append(Utils.formatByteSize(d.sizeMax.get())).append("\t")
									.append(Utils.formatByteSize(d.sizeMin.get())).append("\t").append(d.msgId)
									.append("\n");
						});

		// 3 输出结果
		rst = Utils.createStr(rst, recCountTotal, recCount.toString(), Utils.formatByteSize(recSizeTotal),
				recSize.toString(), Utils.formatByteSize(recSizeAvg), recAvgSize.toString(), sndCountTotal,
				sndCount.toString(), Utils.formatByteSize(sndSizeTotal), sndSize.toString(),
				Utils.formatByteSize(sndSizeAvg), sndAvgSize.toString());

		LogCore.statis.info(rst);
	}
}
