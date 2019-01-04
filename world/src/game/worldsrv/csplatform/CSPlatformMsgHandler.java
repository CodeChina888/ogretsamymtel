package game.worldsrv.csplatform;

import core.support.observer.MsgReceiver;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.msg.MsgPlatform.CSCheckGiftCode;

public class CSPlatformMsgHandler {
	/**
	 * 提交礼包激活码
	 * @param param
	 */
	@MsgReceiver(CSCheckGiftCode.class)
	public void onCSCheckGiftCode(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSCheckGiftCode msg = param.getMsg();
		String giftcode = msg.getCode();
		
		CSPlatformManager.inst().checkGiftCode(humanObj, giftcode);		
	}
}
