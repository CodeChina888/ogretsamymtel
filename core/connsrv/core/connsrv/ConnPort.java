package core.connsrv;

import java.util.Iterator;

import core.Port;
import core.connsrv.Connection;
import core.connsrv.netty.ServerHandler;

public class ConnPort extends Port {
	public ConnPort(String name) {
		super(name);
	}

	public ConnPort(String name, int interval) {
		super(name, interval);
	}
	
	/**
	 * 添加新连接服务
	 * @param connection
	 */
	public void openConnection(Connection connection) {
		addService(connection);
	}

	/**
	 * 安全删除连接服务
	 * @param connection
	 */
	public void closeConnection(Connection connection) {
		delServiceBySafe(connection.getId());
	}

	@Override
	public void pulseOverride() {
		super.pulseOverride();

		// 接收数据
		for (Iterator<Connection> iter = ServerHandler.conns.iterator(); iter.hasNext();) {
			iter.next().handleInput();
		}
	}
}
