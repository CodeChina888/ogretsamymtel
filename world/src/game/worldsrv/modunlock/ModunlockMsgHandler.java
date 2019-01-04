package game.worldsrv.modunlock;

import core.support.observer.MsgReceiver;
import game.msg.Define.EModeType;
import game.msg.MsgCommon.CSModUnlockView;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

public class ModunlockMsgHandler {
	
	@MsgReceiver(CSModUnlockView.class) 
	public void _msg_CSModUnlockView(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSModUnlockView msg = param.getMsg();
		EModeType modeType = msg.getModeType();
		ModunlockManager.inst()._msg_CSModUnlockView(humanObj, modeType);
	}
}
