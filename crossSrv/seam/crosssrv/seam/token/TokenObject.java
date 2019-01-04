package crosssrv.seam.token;

import java.io.IOException;

import core.CallPoint;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import core.support.ConnectionStatus;
import crosssrv.combatant.CombatantGlobalInfo;

public class TokenObject implements ISerilizable {
	public ConnectionStatus status; // 当前连接状态
	public TokenService serv; // 当前所属线程
	public long id; // 主键ID等于连接ID
	public long humanId; // 玩家ID
	public CombatantGlobalInfo info; //
	public CallPoint connPoint; // 连接点

	public TokenObject(long id, TokenService serv, ConnectionStatus status, CallPoint connPoint,
			CombatantGlobalInfo info) {
		this.id = id;
		this.humanId = status.humanId;
		this.serv = serv;
		this.status = status;
		this.connPoint = connPoint;
		this.info = info;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(status);
		out.write(id);
		out.write(humanId);
		out.write(connPoint);
		out.write(info);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		status = in.read();
		id = in.read();
		humanId = in.read();
		connPoint = in.read();
		info = in.read();
	}

	public long getId() {
		return id;
	}
}