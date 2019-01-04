package core.support.idAllot;

import java.util.LinkedList;

import core.Port;
import core.PortPulseQueue;
import core.gen.callback.DistrCallback;
import core.gen.proxy.DistrMethod;
import core.support.Config;
import core.support.Distr;
import core.support.Param;
import core.support.idAllot.IdAllotServiceProxy;
import core.support.log.LogCore;

/**
 * 可分配ID池 此类并非线程安全的
 */
@SuppressWarnings("deprecation")
public abstract class IdAllotPoolBase {
	private int warnNum;// 剩余警戒值，默认5000 //当ID池数量小于警戒值之后 就立即申请的ID
	private int applyNum;// 每次申请数量，默认10000
	private long platformDigit;// 运营商标识，即平台ID(最大可设置为920)
	private long serverDigit;// 游戏区标识，即服务器ID(最大可设置为9999)，10的13次方的游戏区编号
	private long serverDigit5;// 游戏区标识，即服务器ID(最大可设置为9999)，10的5次方的游戏区编号

	// 19位数据库ID = (2位平台ID + 4位服务器ID + 13位可分配ID池)，是根据bigint最大值
	// (9223372036854775807)分配的
	// humanId范围[100001-999999]，非humanId范围(999999, 13个9]
	
	private boolean notApplyHumanId = false;// 是否已无法分配玩家ID
	// 可分配玩家ID池
	private final LinkedList<Long> idHumans = new LinkedList<>();
	
	// 可分配ID池
	private final LinkedList<Long> ids = new LinkedList<>();
	
	// 是否在ID分配申请中
	private boolean applying = false;
	
	// 所属Port
	private Port port;

	/**
	 * 构造函数 立即申请一些ID备用
	 */
	public IdAllotPoolBase(Port port, int platformDigit, int serverDigit) {
		// modify by shenjh,缩小ID的增长速度，从10000, 5000 改为 2000, 1000
		this(port, platformDigit, serverDigit, 10000, 5000);
	}

	/**
	 * 构造函数 立即申请一些ID备用
	 */
	public IdAllotPoolBase(Port port, int platformDigit, int serverDigit, int applyNum, int warnNum) {
		// 记录所属Port
		this.port = port;
		this.platformDigit = platformDigit * (long) Math.pow(10, 17);// 10, 17
		this.serverDigit = serverDigit * (long) Math.pow(10, 13);// 10, 13
		this.serverDigit5 = serverDigit * (long) Math.pow(10, 5);// 10, 5
		this.applyNum = applyNum;
		this.warnNum = warnNum;
		
		// 初始化ID分配池
		port.addQueue(new PortPulseQueue() {
			public void execute(Port port) {
				// Port启动时先同步方式申请一次
				applySyn();
				if (port.getId().equals(Distr.PORT_DEFAULT)) {
					// PORT_DEFAULT线程挂了AccountService会申请玩家ID
					applyHumanSyn();
				}
			}
		});
	}

	/**
	 * 申请ID
	 * @return
	 */
	public long applyId() {
		// 没有可分配的ID了
		if (ids.isEmpty()) {
			// 记录日志
			LogCore.core.error("[ID分配]出现了问题：可分配ID池空了，" + "这种情况下会出现线程阻塞等待的情况：申请数量={}, 警戒值={}, port={}", applyNum,
					warnNum, port, new Throwable());

			// 正常来说 当ID池数量小于警戒值之后就会自动申请ID
			// 此处无ID可分配也许是申请结果还木有返回
			// 单木有办法 此处阻塞线程进行同步申请
			applySyn();
		}

		// 小于警戒值 申请新ID
		if (ids.size() < warnNum && !applying) {
			applyAyn();
		}

		// 获取可分配的ID
		long id = ids.pop();
		// 加上运营商和服务器标识
		long result = platformDigit + serverDigit + id;

		return result;
	}
	
	/**
	 * 同步申请可分配ID
	 */
	private void applySyn() {
		// 申请可分配ID
		Param results;
		if(Config.isCrossSrv) {
			CrossIdAllotServiceProxy prx = CrossIdAllotServiceProxy.newInstance();
			prx.apply(applyNum * 10);
			results = prx.waitForResult();
		} else {
			IdAllotServiceProxy prx = IdAllotServiceProxy.newInstance();
			prx.apply(applyNum * 10);
			results = prx.waitForResult();
		}

		// 分配到的ID范围
		long begin = results.get("begin");
		long end = results.get("end");

		// 加入可分配ID池中
		putIds(begin, end);

		// 日志
		LogCore.core.info("[ID分配]同步方式向服务端申请可分配ID范围：ids.size={}, begin={}, end={}, portId={}", 
				ids.size(), begin, end, port.getId());
	}
	/**
	 * 增加可分配ID
	 * @param begin
	 * @param end
	 */
	private void putIds(long begin, long end) {
		// 加入可分配ID池中
		for (long i = begin; i <= end; i++) {
			ids.add(i);
		}
	}

	/**
	 * 异步申请可分配ID
	 */
	private void applyAyn() {
		// 设置申请状态
		applying = true;

		IdAllotServiceProxy prx = IdAllotServiceProxy.newInstance();
		prx.apply(applyNum);
		prx.listenResult(this::_result_applyId);

		// 日志
		LogCore.core.info("[ID分配]向服务端申请新的可分配ID：applyNum={}, portId={}", applyNum, port.getId());
	}
	/**
	 * 处理申请ID请求的返回值
	 */
	@DistrMethod
	private void _result_applyId(Param results, Param context) {
		// 设置申请状态
		applying = false;

		// 分配到的ID范围
		long begin = results.get("begin");
		long end = results.get("end");

		// 加入可分配ID池中
		putIds(begin, end);

		// 日志
		LogCore.core.info("[ID分配]服务端返回可分配ID范围：ids.size={}, begin={}, end={}, portId={}", 
				ids.size(), begin, end, port.getId());
	}

	/**
	 * 获取10的5次方的游戏区编号
	 * @return
	 */
	public long getGameArea_pow5() {
		return this.serverDigit5;
	}
	
	//////////////////////////////////////////////////////////////////////////
	/**
	 * 同步申请玩家ID
	 * @return
	 */
	public long applyHumanId() {
		if (notApplyHumanId)
			return 0;// 已无法分配玩家ID，则返回0
		
		// 没有可分配的玩家ID了
		if (idHumans.isEmpty()) {
			// 记录日志
			//LogCore.core.error("[玩家ID分配]出现了问题：可分配玩家ID池空了，" + "这种情况下会出现线程阻塞等待的情况：port={}", 
			//		port, new Throwable());

			// 正常来说 当ID池数量小于警戒值之后就会自动申请ID
			// 此处无ID可分配也许是申请结果还木有返回
			// 单木有办法 此处阻塞线程进行同步申请
			applyHumanSyn();
		}
		
		// 获取可分配的玩家ID
		long id = idHumans.pop();
		if (id == 0) {// 无法分配了
			notApplyHumanId = true;// 是否已无法分配玩家ID
			return 0;// 已无法分配玩家ID，则返回0
		} else {
			// 加上运营商和服务器标识
			long result = platformDigit + serverDigit + id;
			// 更新最大玩家ID到数据库
			IdAllotServiceProxy prx = IdAllotServiceProxy.newInstance();
			prx.updateHumanId(id);
			return result;
		}
	}
	
	/**
	 * 同步申请可分配玩家ID
	 */
	public void applyHumanSyn() {
		// 申请可分配玩家ID
		IdAllotServiceProxy prx = IdAllotServiceProxy.newInstance();
		prx.applyHumanId();
		Param results = prx.waitForResult();

		// 分配到的玩家ID范围
		long begin = results.get("begin");
		long end = results.get("end");

		if (idHumans.isEmpty()) {
			// 加入可分配玩家ID池中
			for (long i = begin; i <= end; i++) {
				idHumans.add(i);
			}
		}
		
		// 日志
		LogCore.core.info("[ID分配]同步方式向服务端申请可分配玩家ID范围：idHumans.size={}, begin={}, end={}, portId={}", 
				idHumans.size(), begin, end, port.getId());
	}
	
}
