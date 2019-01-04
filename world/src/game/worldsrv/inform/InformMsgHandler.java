package game.worldsrv.inform;

import core.Port;
import core.support.Sys;
import core.support.observer.MsgReceiver;
import game.msg.Define.EInformType;
import game.msg.MsgInform.CSInformChat;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.gm.GMManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Utils;

public class InformMsgHandler {
	// Logger logInform = Log.inform;
	// Logger logChat = Log.chat;

	/**
	 * 聊天
	 * @param param
	 */
	@MsgReceiver(CSInformChat.class)
	public void _msg_CSInformChat(MsgParam param) {
		CSInformChat msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		
		EInformType type = msg.getType();
		if (type != EInformType.PrivateInform) {// 私聊不需要禁言
			// 禁言
			if(Port.getTime() <= humanObj.getHuman().getSilenceEndTime()) {
				//162由于非法操作，你的账号已经被禁言，{}后才能聊天。
				// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
				String info = Utils.createStr("{}|{}|{}", ParamManager.sysMsgMark, 162, 
						Utils.formatTime(humanObj.getHuman().getSilenceEndTime(), "yyyy-MM-dd HH:mm:ss"));
				InformManager.inst().user(humanObj.id, info);
				return;
			}
		}
		
		String content = msg.getContent();
		if(!msg.getIsVoice()) {// 语音不需要屏蔽字
			int length = ParamManager.chatLengthMax;
			// 设置发言长度，不超过40
			if (content.length() > length) {
				// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
				String info = Utils.createStr("{}|{}", ParamManager.sysMsgMark, 164);
				InformManager.inst().user(humanObj.id, info);
				return;
			}
		}
		String contentGM = content.toLowerCase();
		// GM命令
		if (contentGM.length() > 2 && contentGM.substring(0, 1).equals("-")) {
			String gm = contentGM.substring(1);
			GMManager.inst().gmCommand(param, gm);
			return;
		}
		if (Sys.isWin()  && contentGM.length() > 4 && contentGM.substring(0, 4).equals("read")) {
			String gm = contentGM.substring(4);
			GMManager.inst().readFile(param, gm);
			return;
		}
		
		
		// 聊天
		long targetKey = msg.getTargetKey();
		String toHumanName = msg.getReceiveHumanName();
		InformManager.inst().sendSCInformMsg(humanObj, type, content, targetKey,toHumanName);
		
	}
}