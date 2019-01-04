package game.seam.account;

import core.CallPoint;
import core.support.ConnectionStatus;
import game.msg.Define.ELoginType;
import game.seam.account.AccountService;

public class AccountObject {
	protected AccountService serv; // 当前所属线程
	protected ConnectionStatus connStatus; // 当前连接状态
	protected CallPoint connPoint; // 连接点
	protected long id; // 主键ID等于连接ID
	protected ELoginType loginType;// 登录类型
	protected String devMAC; // 设备MAC地址
	protected long devIMEI; // 设备IMEI号
	protected int devType; // 设备类型

	protected long humanId; // 玩家ID
	protected String accountBind = ""; // 玩家账号绑定游客
	
	public AccountObject(AccountService serv, ConnectionStatus connStatus, CallPoint connPoint,  
			ELoginType loginType, String devMAC, long devIMEI, int devType) {
		this.serv = serv;
		this.connStatus = connStatus;
		this.connPoint = connPoint;
		this.id = (long) connPoint.servId;
		this.loginType = loginType;
		this.devMAC = devMAC;
		this.devIMEI = devIMEI;
		this.devType = devType;
	}

	public long getId() {
		return id;
	}
	
	public long getHumanId() {
		return humanId;
	}
	
	public int getServerId() {
		return connStatus.serverId;
	}
	
	public String getChannel() {
		return connStatus.channel;
	}
	
	public String getAccount() {
		return connStatus.account;
	}
	
	public String getAccountId() {
		return connStatus.accountId;
	}
	
	public String getClientIP() {
		return connStatus.clientIP;
	}
	
	public ELoginType getLoginType() {
		return loginType;
	}
	
	public String getAccountBind() {
		return accountBind;
	}

	public String getDevMAC() {
		return devMAC;
	}

	public long getDevIMEI() {
		return devIMEI;
	}

	public int getDevType() {
		return devType;
	}
	
}