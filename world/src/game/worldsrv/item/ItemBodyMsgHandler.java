package game.worldsrv.item;

import core.support.observer.MsgReceiver;
import game.msg.MsgItem.CSEquipRefineAbandonSlotUp;
import game.msg.MsgItem.CSEquipRefineSaveSlotUp;
import game.msg.MsgItem.CSEquipRefineSlotUp;
import game.msg.MsgItem.CSEquipRefineUp;
import game.msg.MsgItem.CSReinforceAllEquip2Msg;
import game.msg.MsgItem.CSReinforceEquipMsg;
import game.msg.MsgItem.CSUpEquipMsg;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 装备操作借口
 * @author GZC-WORK
 */
public class ItemBodyMsgHandler {

	

	/**
	 * 穿装备
	 * @param param
	 */
	public void _msg_CSItemEquipPutOn(MsgParam param) {
	}
	
	/**
	 * 脱装备
	 * @param param
	 */
	public void _msg_CSItemEquipTakeOff(MsgParam param) {
	}

	/**
	 * 装备合成
	 * @param param
	 */
	public void _msg_CSEquipCompose(MsgParam param) {
	}

	/**
	 * 强化装备--金币强化 一次强化
	 */
	@MsgReceiver(CSReinforceEquipMsg.class)
	public void _msg_CSReinforceEquipMsg(MsgParam param) {
		CSReinforceEquipMsg msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		ItemBodyManager.inst()._msg_CSReinforceEquipMsg(humanObj, msg.getItemID());
	}
	/**
	 * 强化装备--一键强化
	 */
	@MsgReceiver(CSReinforceAllEquip2Msg.class)
	public void _msg_CSReinforceAllEquip2Msg(MsgParam param) {
		CSReinforceAllEquip2Msg msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		ItemBodyManager.inst()._msg_CSReinforceAllEquipMsg(humanObj, msg);
	}
	/**
	 * 装备进阶
	 */
	@MsgReceiver(CSUpEquipMsg.class)
	public void _msg_CSUpEquipMsg(MsgParam param) {
		CSUpEquipMsg msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		//FIXME 判断进阶功能是否开启 true 未开启 false 开启
//		if (!ModunlockManager.inst().isUnlock(ModType.EquipEvolution.value(), humanObj.getHuman())) {
//			// 等级未达到功能开放等级
//			humanObj.sendSysMsg(66);
//			return;
//		}
		long itemId = msg.getItemID();
		ItemBodyManager.inst()._msg_CSAdvancedEquip(humanObj,itemId);
	}	
	
	/**
	 * 精炼
	 * @param param
	 */
	@MsgReceiver(CSEquipRefineSlotUp.class)
	public void _msg_CSEquipRefineSlotUp(MsgParam param) {
		CSEquipRefineSlotUp msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		long itemId = msg.getItemID();
		boolean isPerfect = msg.getIsPerfect();
		ItemBodyManager.inst()._msg_CSEquipRefineSlotUp(humanObj, itemId, isPerfect);
	}

	/**
	 * 保存精炼结果
	 * @param param
	 */
	@MsgReceiver(CSEquipRefineSaveSlotUp.class)
	public void _msg_CSEquipRefineSaveSlotUp(MsgParam param) {
		CSEquipRefineSaveSlotUp msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		long itemId = msg.getItemID();
		ItemBodyManager.inst()._msg_CSEquipRefineSaveSlotUp(humanObj, itemId);
	}

	/**
	 * 放弃精炼结果
	 * @param param
	 */
	@MsgReceiver(CSEquipRefineAbandonSlotUp.class)
	public void _msg_CSEquipRefineAbandonSlotUp(MsgParam param) {
		CSEquipRefineAbandonSlotUp msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		long itemId = msg.getItemID();
		ItemBodyManager.inst()._msg_CSEquipRefineAbandonSlotUp(humanObj, itemId);
	}

	/**
	 * 提升精炼品质
	 * @param param
	 */
	@MsgReceiver(CSEquipRefineUp.class)
	public void _msg_CSEquipRefineUp(MsgParam param) {
		CSEquipRefineUp msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		long itemId = msg.getItemID();
		ItemBodyManager.inst()._msg_CSEquipRefineUp(humanObj, itemId);
	}
		
}
