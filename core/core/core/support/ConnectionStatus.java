package core.support;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

/**
 * 玩家连接状态
 */
public class ConnectionStatus implements ISerilizable {
	// 状态代码
	public static final int STATUS_LOGIN = 0; // 登陆中
	public static final int STATUS_GATE = 1; // 选择角色
	public static final int STATUS_PLAYING = 2; // 游戏中
	public static final int STATUS_LOSTED = 3; // 已断开连接

	public int status = STATUS_LOGIN; // 默认状态为登陆中
	public int serverId; //所在服务器Id
	public long humanId; // 玩家ID
	public String account = ""; // 玩家账号
	public String accountId = ""; // 玩家账号ID
	public String name = "";	// 玩家昵称
	public String clientIP = "";//玩家客户端IP
	public String stageNodeId = ""; // 玩家登陆后被分配到的场景线程的nodeId(StagePort.NodeId=world0)
	public String stagePortId = ""; // 玩家登陆后被分配到的场景线程的portId(StagePort.PortId=stage0~4)
	public String channel = "local"; // 渠道ID
	public String appendInfo = ""; //附加信息
	
	// public long stageId; //玩家登陆后被分配到的场景线程的ID(StagePort.ID)

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(status);
		stream.write(serverId);
		stream.write(humanId);
		stream.write(account);
		stream.write(accountId);
		stream.write(name);
		stream.write(clientIP);
		stream.write(stageNodeId);
		stream.write(stagePortId);
		stream.write(channel);
		stream.write(appendInfo);
	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		this.status = stream.read();
		this.serverId = stream.read();
		this.humanId = stream.read();
		this.account = stream.read();
		this.accountId = stream.read();
		this.name = stream.read();
		this.clientIP = stream.read();
		this.stageNodeId = stream.read();
		this.stagePortId = stream.read();
		this.channel = stream.read();
		this.appendInfo = stream.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("status", status)
				.append("serverId", serverId)
				.append("humanId", humanId)
				.append("account", account)
				.append("accountId", accountId)
				.append("name", name)
				.append("stageNodeId", stageNodeId)
				.append("stagePortId", stagePortId)
				.append("channel", channel)
				.append("appendInfo", appendInfo)
				.toString();
	}
}
