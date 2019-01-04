package crosssrv.seam;

import core.Port;
import core.support.SeamServiceBase;
import crosssrv.character.CombatantObjectServiceProxy;
import crosssrv.seam.token.TokenServiceProxy;

/**
 * 继承父类 CORE会通过接口的返回值进行通信
 * 
 * @author GaoZhangCheng
 */
public class SeamService extends SeamServiceBase {

	public SeamService(Port port) {
		super(port);
	}

	/**
	 * 登陆阶段接收消息函数
	 */
	@Override
	public int methodAccountMsg() {
		return TokenServiceProxy.EnumCall.TokenService_msgHandler_long_ConnectionStatus_bytes;
	}

	/**
	 * 游戏阶段接收消息函数
	 */
	@Override
	public int methodWorldMsg() {
		return CombatantObjectServiceProxy.EnumCall.CombatantObjectService_msgHandler_long_bytes;
	}

	/**
	 * 登陆阶段连接中断消息函数
	 */
	@Override
	public int methodAccountLost() {
		return TokenServiceProxy.EnumCall.TokenService_connClosed_long;
	}

	/**
	 * 游戏阶段连接中断消息函数
	 */
	@Override
	public int methodWorldLost() {
		return CombatantObjectServiceProxy.EnumCall.CombatantObjectService_connClosed_long;
	}

	/**
	 * 登陆阶段连接验证消息函数
	 */
	@Override
	public int methodAccountCheck() {
		return TokenServiceProxy.EnumCall.TokenService_connCheck_long;
	}

	/**
	 * 游戏阶段连接验证消息函数
	 */
	@Override
	public int methodWorldCheck() {
		return CombatantObjectServiceProxy.EnumCall.CombatantObjectService_connCheck_long;
	}
}
