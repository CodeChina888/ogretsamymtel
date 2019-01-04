package game.worldsrv.maincity;

import core.support.observer.MsgReceiver;
import game.msg.Define.ECastellanType;
import game.msg.Define.EWinksSendType;
import game.msg.MsgCastellan.CSSendWinks;
import game.msg.MsgCastellan.CS_BuyMasterPackageMsg;
import game.msg.MsgCastellan.CS_RobRedPacketMsg;
import game.msg.MsgPk.CSPKMirrorEnd;
import game.msg.MsgPk.CSPKMirrorFight;
import game.msg.MsgPk.CSPKMirrorLeave;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

public class MainCityMsgHandler {
	
	/**
	 * 城主购买商品
	 */
	@MsgReceiver(CS_BuyMasterPackageMsg.class)
	public void onCS_BuyMasterPackageMsg(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CS_BuyMasterPackageMsg  msg = param.getMsg();
		int buySn = msg.getSn();
		int num = msg.getBuyNum();
		ECastellanType type = msg.getType();
		MainCityManager.inst().CS_BuyMasterPackageMsg(type, humanObj, buySn, num);
	}
	/**
	 * 抢红包
	 */
	@MsgReceiver(CS_RobRedPacketMsg.class)
	public void onCS_RobRedPacketMsg(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CS_RobRedPacketMsg  msg =param.getMsg();
		long id = msg.getId();
		MainCityManager.inst().robRedPacketMsg(humanObj,id);
	}
	
	/**
	 * 发送魔法表情
	 */
	@MsgReceiver(CSSendWinks.class)
	public void _msg_CSSendWinks(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSendWinks msg = param.getMsg();
		long receiverId = msg.getReceiverId();
		String receiverName = msg.getRecieverName();
		int winksSn = msg.getWinksSn();
		EWinksSendType sendType = msg.getSendType();
		MainCityManager.inst()._msg_CSSendWinks(humanObj, receiverId, receiverName, winksSn, sendType);
	}
	
}
