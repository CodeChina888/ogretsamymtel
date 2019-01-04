package core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import core.Node;
import core.RemoteNode;
import core.support.TickTimer;
import core.support.Time;
import core.support.log.LogCore;

/**
 * 远程连接状态守护进程
 */
public class RemoteNodeDaemon extends Thread {
	// 异常连接主动清理时间
	private final long INTERVAL_HANDLER = 10 * Time.MIN;
	// 主动连接关闭后 重新连接时间
	private final long INTERVA_RECONN = 5 * Time.SEC;

	// 连接检查计时器
	private final TickTimer checkTimer = new TickTimer(1 * Time.MIN);
	// 重接检查计时器
	private final TickTimer reconnTimer = new TickTimer(3 * Time.SEC);

	// 待重新连接信息
	private final List<DATA> reconn = new ArrayList<>();

	// 被守护Node
	private final Node node;
	// 日志
	private final Logger log = LogCore.remote;

	/**
	 * 构造函数
	 * @param node
	 */
	public RemoteNodeDaemon(Node node) {
		this.node = node;
	}

	@Override
	public void run() {
		// 运行中就不断轮询
		while (true) {
			try {
				// 检查连接状态并继续处理
				handler();
				// 重新连接
				reconn();

				Thread.sleep(Time.SEC);
			} catch (Throwable e) {
				// 不做任何处理 仅仅抛出异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行
				LogCore.core.error("", e);
			}
		}
	}

	/**
	 * 检查连接状态并继续处理
	 */
	private void handler() {
		// 当前时间
		long now = System.currentTimeMillis();
		// 未到检查时间
		if (!checkTimer.isPeriod(now))
			return;

		// 寻找异常连接
		List<RemoteNode> rs = new ArrayList<>();
		for (RemoteNode r : node.getRemoteNodeAll()) {
			if (now - r.getPongTime() < INTERVAL_HANDLER)
				continue;

			// 记录需要处理的异常连接
			rs.add(r);
		}

		// 处理异常连接
		for (RemoteNode r : rs) {
			log.warn("[{}]删除长期断开的远程Node连接：remote={}", r.getLocalId(), r);
			// 清理异常连接
			try {
				node.delRemoteNode(r.getRemoteId());
			} finally {
				// 如果是主动连接 5秒后重新建立连接
				if (r.isMain()) {
					reconn.add(new DATA(now + INTERVA_RECONN, r.getRemoteId(), r.getRemoteAddr()));
				}
			}
		}
	}

	/**
	 * 重新连接
	 */
	private void reconn() {
		// 当前时间
		long now = System.currentTimeMillis();
		// 未到检查时间
		if (!reconnTimer.isPeriod(now)) {
			return;
		}

		for (Iterator<DATA> it = reconn.iterator(); it.hasNext();) {
			DATA d = it.next();
			if (d.time > now) {
				continue;
			}
			log.info("====重新连接 reconn() d.remoteId={},d.remoteAddr={}", d.remoteId, d.remoteAddr);//sjh
			// 重新连接
			it.remove();
			node.addRemoteNode(d.remoteId, d.remoteAddr);
		}
	}

	/**
	 * 缓存等待重新建立的连接
	 */
	private static class DATA {
		// 重连时间
		public final long time;
		// 远程ID
		public final String remoteId;
		// 远程地址
		public final String remoteAddr;

		/**
		 * 构造函数
		 * @param time
		 * @param remoteId
		 * @param remoteAddr
		 */
		public DATA(long time, String remoteId, String remoteAddr) {
			this.time = time;
			this.remoteId = remoteId;
			this.remoteAddr = remoteAddr;
		}
	}
}
