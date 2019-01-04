package crosssrv.seam.token;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.google.protobuf.GeneratedMessage;

import core.CallPoint;
import core.support.Config;
import core.support.ConnectionStatus;
import core.support.observer.MsgReceiver;
import crosssrv.seam.msg.MsgParamToken;
import crosssrv.support.Log;
import game.msg.MsgCross.CSTokenLogin;

public class TokenMsgHandler {

	// 下属监听消息
	private static final Set<Class<? extends GeneratedMessage>> methods = new HashSet<>();

	static {
		// 寻找本类监听的消息
		Method[] mths = TokenMsgHandler.class.getMethods();
		for (Method m : mths) {
			// 不是监听函数的忽略
			if (!m.isAnnotationPresent(MsgReceiver.class)) {
				continue;
			}

			// 记录
			MsgReceiver ann = m.getAnnotation(MsgReceiver.class);
			methods.add(ann.value()[0]);
		}
	}

	public static boolean methodFilter(Class<? extends GeneratedMessage> clazz) {
		return methods.contains(clazz);
	}

	/**
	 * 玩家登陆请求
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTokenLogin.class)
	public void onCSLogin(MsgParamToken param) {

		CSTokenLogin msg = param.getMsg();
		CallPoint connPoint = param.getConnPoint();
		ConnectionStatus connStatus = param.getConnStatus();
		connStatus.humanId = msg.getHumanId();

		Log.cross.info("tmpServId={}", connStatus.serverId);
		if (connStatus.serverId == 0) {
			connStatus.serverId = Config.CROSS_SERVER_ID;
		}
		// 登录
		TokenManager.inst().login(connPoint, connStatus, msg.getToken());
	}

}
