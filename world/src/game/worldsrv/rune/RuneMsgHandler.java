package game.worldsrv.rune;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.MsgRune.CSRuneExchange;
import game.msg.MsgRune.CSRuneSummon;
import game.msg.MsgRune.CSRuneTakeOff;
import game.msg.MsgRune.CSRuneTakeOffOneKey;
import game.msg.MsgRune.CSRuneUpgrade;
import game.msg.MsgRune.CSRuneWear;
import game.msg.MsgRune.CSRuneWearOneKey;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * @author Neak
 * 符文系统（命格，纹石）
 * 主角，伙伴通用
 */
public class RuneMsgHandler {
	
	/**
	 * 符文召唤
	 */
	@MsgReceiver(CSRuneSummon.class)
	public void _msg_CSRuneSummon(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRuneSummon msg = param.getMsg();
		int summonSn = msg.getRuneSummonSn();
		RuneManager.inst()._msg_CSRuneSummon(humanObj, summonSn);
	}
	
	/**
	 * 符文合成升级
	 */
	@MsgReceiver(CSRuneUpgrade.class)
	public void _msg_CSRuneUpgrade(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRuneUpgrade msg = param.getMsg();
		// 养成的符文
		long upgradeRuneId = msg.getUpgradeRuneId();
		// 被用于合成消耗的符文
		List<Long> consumeRuneIds = msg.getConsumeRuneIdsList();
		RuneManager.inst()._msg_CSRuneUpgrade(humanObj, upgradeRuneId, consumeRuneIds);
	}
	
	/**
	 * 符文穿戴
	 */
	@MsgReceiver(CSRuneWear.class)
	public void _msg_CSRuneWear(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRuneWear msg = param.getMsg();
		// 佩带者id
		long unitId = msg.getUnitId(); 
		long runeId = msg.getRuneId();
		int slotIndex = msg.getSlotIndex();
		RuneManager.inst()._msg_CSRuneWear(humanObj, unitId, runeId, slotIndex);
	}
	
	/**
	 * 符文脱下
	 */
	@MsgReceiver(CSRuneTakeOff.class)
	public void _msg_CSRuneTakeOff(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRuneTakeOff msg = param.getMsg();
		// 佩带者id
		long unitId = msg.getUnitId(); 
		int slotIndex = msg.getSlotIndex();
		RuneManager.inst()._msg_CSRuneTakeOff(humanObj, unitId, slotIndex);
	}
	
	/**
	 * 符文兑换
	 */
	@MsgReceiver(CSRuneExchange.class)
	public void _msg_CSRuneExchange(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRuneExchange msg = param.getMsg();
		int itemSn = msg.getItemSn();
		RuneManager.inst()._msg_CSRuneExchange(humanObj, itemSn);
	}
	
	/**
	 * 符文一键穿戴
	 */
	@MsgReceiver(CSRuneWearOneKey.class)
	public void _msg_CSRuneWearOneKey(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRuneWearOneKey msg = param.getMsg();
		long unitId = msg.getUnitId();
		List<Long> runeIds = msg.getRuneIdsList();
		RuneManager.inst()._msg_CSRuneWearOneKey(humanObj, unitId, runeIds);
	}
	
	/**
	 * 符文一键脱下
	 */
	@MsgReceiver(CSRuneTakeOffOneKey.class)
	public void _msg_CSRuneTakeOffOneKey(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRuneTakeOffOneKey msg = param.getMsg();
		long unitId = msg.getUnitId();
		RuneManager.inst()._msg_CSRuneTakeOffOneKey(humanObj, unitId);
	}
	
}
