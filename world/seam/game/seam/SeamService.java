package game.seam;

import core.Port;
import game.seam.account.AccountServiceProxy;
import game.worldsrv.character.HumanObjectServiceProxy;
import core.support.SeamServiceBase;

/*
 * import java.lang.reflect.Method; import
 * org.apache.commons.lang3.reflect.MethodUtils; import
 * core.support.ConnectionStatus; import
 * game.worldsrv.support.Utils; import
 * game.worldsrv.character.HumanObjectService; import
 * game.seam.account.AccountService;
 */

/**
 * 继承父类 CORE会通过接口的返回值进行通信
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
		return AccountServiceProxy.EnumCall.AccountService_msgHandler_long_ConnectionStatus_bytes;
		// return _methodAccountMsg;
	}

	// private String _methodAccountMsg =
	// Utils.createMethodKey(MethodUtils.getAccessibleMethod(AccountService.class,
	// "msgHandler", long.class, ConnectionStatus.class, byte[].class));

	/**
	 * 游戏阶段接收消息函数
	 */
	@Override
	public int methodWorldMsg() {
		return HumanObjectServiceProxy.EnumCall.HumanObjectService_msgHandler_long_bytes;
		// return _methodWorldMsg;
	}

	// private String _methodWorldMsg =
	// Utils.createMethodKey(MethodUtils.getAccessibleMethod(HumanObjectService.class,
	// "msgHandler", long.class, byte[].class));

	/**
	 * 登陆阶段连接中断消息函数
	 */
	@Override
	public int methodAccountLost() {
		return AccountServiceProxy.EnumCall.AccountService_connClosed_long;
		// return _methodAccountLost;
	}

	// private String _methodAccountLost =
	// Utils.createMethodKey(MethodUtils.getAccessibleMethod(AccountService.class,
	// "connClosed", long.class));

	/**
	 * 游戏阶段连接中断消息函数
	 */
	@Override
	public int methodWorldLost() {
		return HumanObjectServiceProxy.EnumCall.HumanObjectService_connClosed_long;
		// return _methodWorldLost;
	}

	// private String _methodWorldLost =
	// Utils.createMethodKey(MethodUtils.getAccessibleMethod(HumanObjectService.class,
	// "connClosed", long.class));

	/**
	 * 登陆阶段连接验证消息函数
	 */
	@Override
	public int methodAccountCheck() {
		return AccountServiceProxy.EnumCall.AccountService_connCheck_long;
		// return _methodAccountCheck;
	}

	// private String _methodAccountCheck =
	// Utils.createMethodKey(MethodUtils.getAccessibleMethod(AccountService.class,
	// "connCheck", long.class));

	/**
	 * 游戏阶段连接验证消息函数
	 */
	@Override
	public int methodWorldCheck() {
		return HumanObjectServiceProxy.EnumCall.HumanObjectService_connCheck_long;
		// return _methodWorldCheck;
	}
	// private String _methodWorldCheck =
	// Utils.createMethodKey(MethodUtils.getAccessibleMethod(HumanObjectService.class,
	// "connCheck", long.class));
}
