package crosssrv.seam.msg;

import com.google.protobuf.GeneratedMessage;

import core.CallPoint;
import core.support.ConnectionStatus;
import core.support.observer.MsgParamBase;
import crosssrv.seam.token.TokenService;

/**
 * 登陆阶段中的客户端消息都通过这个类封装
 */
public class MsgParamToken extends MsgParamBase {
	private CallPoint connPoint; // 发送消息的连接点
	private TokenService service; // 所属服务
	private long connId; // 连接ID
	private ConnectionStatus connStatus; // 连接状态

	public MsgParamToken(GeneratedMessage msg) {
		super(msg);
	}

	public CallPoint getConnPoint() {
		return connPoint;
	}

	public void setConnPoint(CallPoint connPoint) {
		this.connPoint = connPoint;
	}

	public TokenService getService() {
		return service;
	}

	public void setService(TokenService service) {
		this.service = service;
	}

	public long getConnId() {
		return connId;
	}

	public void setConnId(long connId) {
		this.connId = connId;
	}

	public ConnectionStatus getConnStatus() {
		return connStatus;
	}

	public void setConnStatus(ConnectionStatus connStatus) {
		this.connStatus = connStatus;
	}
}