package crosssrv.common;

import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import crosssrv.seam.CrossPort;
import game.worldsrv.support.D;

@DistrClass(servId = D.SERV_DATA_RESET)
public class CrossDataResetService extends CrossServiceBase {
	// 每日0时重置
	public static final String CRON_DAY_ZERO = "1 0 0 * * ?";
	// 每日凌晨3点重置
	public static final String CRON_DAY_3ST = "1 0 3 * * ?";
	// 每日5时重置
	public static final String CRON_DAY_FIVE = "1 0 5 * * ?";
	// 每日12时重置
	public static final String CRON_DAY_12ST = "1 0 12 * * ?";
	// 每日18时重置
	public static final String CRON_DAY_18ST = "1 0 18 * * ?";
	// 每日21时重置
	public static final String CRON_DAY_21ST = "1 0 21 * * ?";

	// 每周零时重置
	public static final String CRON_WEEK_ZERO = "1 0 0 ? * MON";
	// 每周五时重置
	public static final String CRON_WEEK_FIVE = "1 0 5 ? * MON";

	// 每小时执行一次
	public static final String CRON_DAY_HOUR = "1 0 0/1 * * ?";
	// 每周一三五零时重置
	public static final String CRON_WEEK_135 = "1 0 0 ? * MON,WED,FRI";

	// 每月一日六点整
	public static final String CRON_MONTH_6H = "1 0 6 1 * ?";

	// 每分钟校验一次
	public static final String CRON_MIN_1ST = "0 */1 * * * ?";

	public CrossDataResetService(CrossPort port) {
		super(port);
	}

	@Override
	protected void init() {

	}

	/**
	 * 每个service预留空方法
	 * 
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {

	}
}
