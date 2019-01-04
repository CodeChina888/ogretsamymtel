package crosssrv.seam.msg;

import java.io.IOException;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

import core.statistics.StatisticsOB;
import core.support.Config;
import core.support.MsgHandler;
import core.support.Param;
import core.support.observer.MsgSender;
import crosssrv.character.CombatantObject;
import game.msg.MsgIds;

public class CombatantExtendMsgHandler extends MsgHandler {

	private CombatantExtendMsgHandler() {
	}

	@Override
	protected void fire(GeneratedMessage msg, Param param) {

		// 如果用户正在切换地图中，则不接受任何请求
		CombatantObject combatantObj = param.get("combatantObj");

		MsgParam mp = new MsgParam(msg);
		mp.setCombatantObject(combatantObj);

		// 执行消息，并且执行统计
		long start = Config.STATISTICS_ENABLE ? System.nanoTime() : 0;
		MsgSender.fire(mp);
		if (start > 0)
			StatisticsOB.msg(msg.getClass().getName(), System.nanoTime() - start);

	}

	@Override
	protected GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException {
		return MsgIds.parseFrom(type, s);
	}

}