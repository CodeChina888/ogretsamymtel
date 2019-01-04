package game.seam.msg;

import java.io.IOException;

import core.CallPoint;
import core.support.ConnectionStatus;
import core.support.MsgHandler;
import core.support.Param;
import core.support.log.LogCore;
import core.support.observer.MsgSender;
import game.seam.msg.MsgParamAccount;
import game.turnbasedsrv.combat.CombatMsgHandler;
import game.msg.MsgIds;
import game.msg.MsgStage.CSStageEnter;
import game.seam.account.AccountMsgHandler;
import game.seam.account.AccountService;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

public class AccountExtendMsgHandler extends MsgHandler {
	private AccountExtendMsgHandler() {
	}

	@Override
	protected void fire(GeneratedMessage msg, Param param) {
		// 忽略不是本阶段要关心的协议
		if (!AccountMsgHandler.methods.contains(msg.getClass()) && !CombatMsgHandler.methods.contains(msg.getClass())) {
			return;
		}

		// 拼写参数
		MsgParamAccount mp = new MsgParamAccount(msg);
		mp.setConnPoint(param.<CallPoint> get("connPoint"));
		mp.setService(param.<AccountService> get("serv"));
		mp.setConnId(param.<Long> get("connId"));
		mp.setConnStatus(param.<ConnectionStatus> get("connStatus"));

		// 输出消息日志
//		if(LogCore.conn.isDebugEnabled()) {
//			LogCore.msg.debug("====客户端请求消息：connId={}, msgClass={}, msgString={}",
//					param.<Long>get("connId"), msg.getClass(), msg.toString());
//		}

		MsgSender.fire(mp);
	}

	@Override
	protected GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException {
		return MsgIds.parseFrom(type, s);
	}

}
