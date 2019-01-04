package game.worldsrv.fashion;

import core.support.observer.MsgReceiver;
import game.msg.MsgFashion.CSFashionBuyHenshin;
import game.msg.MsgFashion.CSFashionHenshinOpen;
import game.msg.MsgFashion.CSFashionHenshinWear;
import game.msg.MsgFashion.CSFashionOpen;
import game.msg.MsgFashion.CSFashionUnlock;
import game.msg.MsgFashion.CSFashionWear;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

public class FashionMsgHandler {	
	/**
	 * 打开套装界面
	 */
	@MsgReceiver(CSFashionOpen.class)
	public void _msg_CSFashionOpen(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		FashionManager.inst()._msg_CSFashionOpen(humanObj);
	}
	
	/**
	 * 解锁套装
	 */
	@MsgReceiver(CSFashionUnlock.class)
	public void _msg_CSFashionUnlock(MsgParam param) {
		CSFashionUnlock msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int fashionSn = msg.getFashionSn();
		FashionManager.inst()._msg_CSFashionUnlock(humanObj, fashionSn);
	}
	/**
	 * 穿套装
	 */
	@MsgReceiver(CSFashionWear.class)
	public void _msg_CSFashionWear(MsgParam param) {
		CSFashionWear msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int fashionSn = msg.getFashionSn();
		FashionManager.inst()._msg_CSFashionWear(humanObj, fashionSn);
	}
	
	
	/**
	 * 使用变身卡（走ItemBagMsgHandler._msg_ItemUse）
	 */
	 
	/**
	 * 购买变身身装
	 */
	@MsgReceiver(CSFashionBuyHenshin.class)
	public void _msg_CSFashionBuyHenshin(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSFashionBuyHenshin msg = param.getMsg();
		int fashionSn = msg.getFashionSn();
		FashionManager.inst()._msg_CSFashionBuyHenshin(humanObj, fashionSn);
	}
	
	/**
	 * 打开变身装界面，处理过期变装(预留，暂时不用和客户端对接)
	 */
	@MsgReceiver(CSFashionHenshinOpen.class)
	public void _msg_CSFashionHenshinOpen(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		FashionManager.inst()._msg_CSFashionHenshinOpen(humanObj);
	}
	
	/**
	 * 穿已经解锁的变身装（预留，暂时不用和客户端对接）
	 */
	@MsgReceiver(CSFashionHenshinWear.class)
	public void _msg_CSFashionHenshinWear(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSFashionHenshinWear msg = param.getMsg();
		int fashionSn = msg.getFashionSn();
		FashionManager.inst()._msg_CSFashionHenshinWear(humanObj, fashionSn);
	}
	
		
}
