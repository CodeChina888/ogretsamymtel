package crosssrv.seam.msg;

import java.io.IOException;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

import core.CallPoint;
import core.support.ConnectionStatus;
import core.support.MsgHandler;
import core.support.Param;
import core.support.observer.MsgSender;
import crosssrv.seam.token.TokenMsgHandler;
import crosssrv.seam.token.TokenService;
import game.msg.MsgIds;

public class TokenExtendMsgHandler extends MsgHandler {
	private TokenExtendMsgHandler() {
	}

	@Override
	protected void fire(GeneratedMessage msg, Param param) {
		// 忽略不是本阶段要关心的协议
		if (!TokenMsgHandler.methodFilter(msg.getClass())) {
			return;
		}

		// 拼写参数
		MsgParamToken mp = new MsgParamToken(msg);
		mp.setConnPoint(param.<CallPoint>get("connPoint"));
		mp.setService(param.<TokenService>get("serv"));
		mp.setConnId(param.<Long>get("connId"));
		mp.setConnStatus(param.<ConnectionStatus>get("connStatus"));

		// 输出当登陆人物
		// if(LogCore.conn.isDebugEnabled()) {
		// LogCore.conn.debug("msg={}, connId={}, msgStr={}", msg.getClass(),
		// param.<Long>get("connId"), msg.toString());
		// }

		MsgSender.fire(mp);
	}

	@Override
	protected GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException {
		return MsgIds.parseFrom(type, s);
	}

}
