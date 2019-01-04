package game.seam.msg;

import java.io.IOException;

import core.statistics.StatisticsOB;
import core.support.Config;
import core.support.MsgHandler;
import core.support.Param;
import core.support.observer.MsgSender;
import game.seam.msg.MsgParam;
import game.msg.MsgStage.CSStageDirection;
import game.msg.MsgStage.CSStageEnter;
import game.msg.MsgStage.CSStageMove;
import game.msg.MsgStage.CSStageMoveStop;
import game.msg.MsgIds;
import game.worldsrv.character.HumanObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

public class HumanExtendMsgHandler extends MsgHandler {
	private HumanExtendMsgHandler() {
	}

	@Override
	protected void fire(GeneratedMessage msg, Param param) {
		// 如果用户正在切换地图中，则不接受任何请求
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if(humanObj == null){
			Log.game.error("HumanExtendMsgHandler.fire humanObj == null");
			return;
		}
		// 切换过地图后可以认为OK了
		if(msg instanceof CSStageEnter){
			humanObj.setStageSwitching(false);
		}
		
		if(humanObj.isStageSwitching()) {
			Log.logGameTest("玩家在切地图中收到消息，human={},msgid={}", humanObj, msg.getClass().getName());
			return;
		}

		MsgParam mp = new MsgParam(msg, humanObj);

		// 排除移动消息
		if (!(msg instanceof CSStageMove) && !(msg instanceof CSStageMoveStop) && !(msg instanceof CSStageDirection) // 转向广播（群发）
		) {
			//启动消息补偿定时器。 如果服务器没有回SC消息 自己补一个
			humanObj.ttMsgFill.start(200);// 启动消息补偿定时器

			// 输出消息日志
			// if(LogCore.conn.isDebugEnabled()) {
			// LogCore.msg.debug("====客户端请求消息：msgClass={}, msgString={}",
			// msg.getClass(), msg.toString());
			// }
		}

		// 执行消息，并且执行统计
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;
		MsgSender.fire(mp);
		if(start > 0)
			StatisticsOB.msg(msg.getClass().getName(), System.nanoTime()-start);
	}

	@Override
	protected GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException {
		return MsgIds.parseFrom(type, s);
	}
}