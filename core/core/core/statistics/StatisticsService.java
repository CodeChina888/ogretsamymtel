package core.statistics;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.statistics.StatisticsDB;
import core.statistics.StatisticsMSG;
import core.statistics.StatisticsOB;
import core.statistics.StatisticsRPC;
import core.support.Config;
import core.support.Distr;
import core.support.TickTimer;
import core.support.Time;

/**
 * 执行信息统计 生产环境中不建议开启，会影响系统性能。
 */
@DistrClass(servId = Distr.SERV_STATISTICS)
public class StatisticsService extends Service {
	// 打印统计数据
	private TickTimer timer = new TickTimer(Config.STATISTICS_RESULT_TIME * Time.SEC);

	public StatisticsService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return Distr.SERV_DEFAULT;
	}

	@Override
	public void pulseOverride() {
		long now = port.getTimeCurrent();

		// 打印统计信息
		if (timer.isPeriod(now)) {
			StatisticsMSG.showResult();
			StatisticsOB.showResult();
			StatisticsDB.showResult();
			StatisticsRPC.showResult();
		}
	}
}
