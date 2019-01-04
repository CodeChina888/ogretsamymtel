package game.worldsrv.item;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.Define.DItem;
import game.msg.MsgItem.CSCompoundItemMsg;
import game.msg.MsgItem.CSItemUse;
import game.msg.MsgItem.CSItemsBagSell;
import game.msg.MsgItem.CSSelectPackageItem;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.enumType.LogSysModType;

public class ItemBagMsgHandler {
	
	/**
	 * 出售--多个物品
	 */
	@MsgReceiver(CSItemsBagSell.class)
	public void _msg_CSItemsBagSell(MsgParam param) {
		CSItemsBagSell msg = param.getMsg();
		HumanObject humanObject = param.getHumanObject();
		List<DItem> listPro = msg.getProList();

		ItemBagManager.inst()._msg_CSItemsBagSell(humanObject, listPro);
	}

	/**
	 * 使用物品
	 * @param param
	 */
	@MsgReceiver(CSItemUse.class)
	public void _msg_CSItemUse(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSItemUse msg = param.getMsg();

		long itemId = msg.getId();
		int num = msg.getNum();

		ItemBagManager.inst().itemUse(humanObj, itemId, num, LogSysModType.BagItemUse);
	}

	/**
	 * 合成道具
	 */
	@MsgReceiver(CSCompoundItemMsg.class)
	public void _msg_CSCompoundItemMsg(MsgParam param) {
		CSCompoundItemMsg msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		ItemBagManager.inst()._msg_CSCompoundItemMsg(humanObj, msg.getCompoundItemSn(), msg.getCompoundCount());
	}
	
	/**
	 * 选择礼包道具中的物品
	 */
	@MsgReceiver(CSSelectPackageItem.class)
	public void _msg_CSSelectPackageItem(MsgParam param) {
		CSSelectPackageItem msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int num = msg.getNum();
		int packageSn = msg.getPackageSn();
		int index = msg.getIndex();
		ItemBagManager.inst()._msg_CSSelectPackageItem(humanObj, num, packageSn, index);
	}

}
