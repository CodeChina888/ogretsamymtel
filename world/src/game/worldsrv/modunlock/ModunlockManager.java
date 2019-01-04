package game.worldsrv.modunlock;

import java.util.ArrayList;
import java.util.List;

import core.support.ManagerBase;
import game.msg.Define.EModUnlockType;
import game.msg.Define.EModeType;
import game.msg.MsgCommon.SCModUnlock;
import game.msg.MsgCommon.SCModUnlockView;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfModUnlock;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

/**
 * 功能解锁功能
 * @author
 */
public class ModunlockManager extends ManagerBase {

	/**
	 * 获取实例
	 * @return
	 */
	public static ModunlockManager inst() {
		return inst(ModunlockManager.class);
	}
	
	/**
	 * 触发功能模块解锁
	 * @param humanObj 
	 * @param unlockType 触发解锁的类型（等级，副本，任务）
	 * @param value 对应类型的参数
	 */
	public void triggerModUnlock(HumanObject humanObj, int unlockType, int value) {
		List<ConfModUnlock> listConfMod = ConfModUnlock.findBy(ConfModUnlock.K.type, unlockType, ConfModUnlock.K.value, value);
		if (listConfMod == null || listConfMod.isEmpty())
			return;
		
		Human human = humanObj.getHuman();
		List<Integer> snList = new ArrayList<Integer>();
		List<Integer> modUnlockList = Utils.strToIntList(human.getModUnlock());
		for (ConfModUnlock confMod : listConfMod) {
			if (!ModunlockManager.inst().isUnlock(confMod.sn, humanObj)) {// 未解锁，则解锁
				modUnlockList.add(confMod.sn);
				snList.add(confMod.sn);
			}
		}
		if (!snList.isEmpty()) {
			human.setModUnlock(Utils.ListIntegerToStr(modUnlockList));// 保存数据
			// 下发通知客户端
			ModunlockManager.inst().sendSCModUnlockMsg(humanObj, snList);
		}
	}
	
	/**
	 * 判断是否能解锁
	 * @return true已解锁，false未解锁
	 */
	public boolean isUnlock(EModeType modeType, HumanObject humanObj) {
		return isUnlock(modeType.getNumber(), humanObj);
	}
	/**
	 * 判断是否能解锁
	 * @return true已解锁，false未解锁
	 */
	public boolean isUnlock(int sn, HumanObject humanObj) {
		boolean unlock = false;
		ConfModUnlock conf = ConfModUnlock.get(sn);
		if (conf == null) {
			Log.table.error("===isOpenLock ConfModUnlock配表错误，sn={}", sn);
			return unlock;
		}
		Human human = humanObj.getHuman();
		int type = 0;
		int value = 0;
		for (int i = 0; i < conf.type.length ; i++) {
			type = conf.type[i];
			value = conf.value[i];
			// 无解锁条件
			if (type == EModUnlockType.ModUnlockeNone_VALUE) {
				unlock = true;
				break;
			// 解锁条件：玩家等级
			} else if (type == EModUnlockType.ModUnlockHumanLevel_VALUE) {
				if (value <= human.getLevel()) {
					unlock = true;
					break;
				}
			// 解锁条件：通过副本
			} else if (type == EModUnlockType.ModUnlockInstance_VALUE) {
				if (humanObj != null && InstanceManager.inst().isInstPass(humanObj, value)) {
					unlock = true;
					break;
				}
				
			// 解锁条件：vip解锁
			} else if (type == EModUnlockType.ModUnlockVip_VALUE) {
				if (value <= human.getVipLevel()) {
					unlock = true;
					break;
				}
			
			// 解锁条件：成就任务进度
			} else if (type == EModUnlockType.ModUnlockQuest_VALUE) {
				List<Integer> modUnlock = Utils.strToIntList(human.getModUnlock());
				if (modUnlock.contains(sn)) {
					unlock = true;
					break;
				}
			}
		}
		return unlock;
	}
	/**
	 * 判断是否能解锁
	 */
	public boolean isUnlock(int sn, Human human) {
		boolean unlock = false;// true已解锁，false未解锁
		ConfModUnlock conf = ConfModUnlock.get(sn);
		if (conf == null) {
			Log.table.error("===isOpenLock ConfModUnlock配表错误，sn={}", sn);
			return unlock;
		}
		int type = 0;
		int value = 0;
		for (int i = 0; i <= conf.type.length ; i++) {
			type = conf.type[i];
			value = conf.value[i];
			// 无解锁条件
			if (type == EModUnlockType.ModUnlockeNone_VALUE) {
				unlock = true;
				break;
			// 解锁条件：玩家等级
			} else if (type == EModUnlockType.ModUnlockHumanLevel_VALUE) {
				if (value <= human.getLevel()) {
					unlock = true;
					break;
				}
			// 解锁条件：通过副本
			} else if (type == EModUnlockType.ModUnlockInstance_VALUE) {
				
			// 解锁条件：vip解锁
			} else if (type == EModUnlockType.ModUnlockVip_VALUE) {
				if (value <= human.getVipLevel()) {
					unlock = true;
					break;
				}
				
			// 解锁条件：成就任务进度
			} else if (type == EModUnlockType.ModUnlockQuest_VALUE) {
				List<Integer> modUnlock = Utils.strToIntList(human.getModUnlock());
				if (modUnlock.contains(sn)) {
					unlock = true;
					break;
				}
			}
		}
		return unlock;
	}

	/**
	 * 发送激活的功能模块sn
	 * @param humanObj
	 * @param snList
	 */
	public void sendSCModUnlockMsg(HumanObject humanObj, List<Integer> snList) {
		SCModUnlock.Builder msg = SCModUnlock.newBuilder();
		msg.addAllSnList(snList);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 领取功能开放奖励
	 * @param humanObj
	 * @param modeType
	 */
	public void _msg_CSModUnlockView(HumanObject humanObj, EModeType modeType) {
		if(!isUnlock(modeType, humanObj)) {
			Log.game.debug("=== 功能未开放，不能领取奖励 ===");
			humanObj.sendSysMsg(115401);
			return;
		}
		
		ConfModUnlock conf = ConfModUnlock.get(modeType.getNumber());
		if (conf == null) {
			Log.game.debug("=== 功能不存在，无法领取 ===");
			humanObj.sendSysMsg(115402);
			return;
		}
		
		String modStr = humanObj.getHuman().getModUnlockReward();
		List<Integer> modList = Utils.strToIntList(modStr);
		int curType = modeType.getNumber();
		if (modList.contains(modeType.getNumber())) {
			Log.game.debug("=== 功能奖励已经领取，无法重复领取 ===");
			humanObj.sendSysMsg(115403);
			return;
		}
		modList.add(curType);
		humanObj.getHuman().setModUnlockReward(Utils.ListIntegerToStr(modList));
		
		// 添加奖励
		ItemChange itemChange = ProduceManager.inst().getAndGiveProduce(humanObj, conf.rewardSn, 1, LogSysModType.ModUnlockReward);
		
		SCModUnlockView.Builder msg = SCModUnlockView.newBuilder();
		msg.setModeType(modeType);
		msg.addAllProduceList(itemChange.getProduce());
		humanObj.sendMsg(msg);
	}

}
