package game.worldsrv.raffle;

import core.support.ManagerBase;
import game.msg.Define.EModeType;
import game.msg.MsgRaffle.SCLeaveLuckTurntable;
import game.msg.MsgRaffle.SCLuckTurntable;
import game.msg.MsgRaffle.SCOpenLuckTurntable;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfLuckTurntable;
import game.worldsrv.config.ConfLuckTurntableWeight;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.raffle.raffleMode.LuckTurntableMod;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

/**
 * @author Neak
 * 玩家所有抽奖处理
 */
public class RaffleManager extends ManagerBase{
	/**
	 * 获取实例
	 * @return
	 */
	public static RaffleManager inst() {
		return inst(RaffleManager.class);
	}

	/**
	 * 发送对应玩法的转盘
	 * 通知客户端打开转盘界面
	 * @param modeType 玩法类型
	 * @param lv 转盘级别（洞天福地层级..等）
	 */
	public void send_openTurntable(HumanObject humanObj, EModeType modeType, int lv) {
		LuckTurntableMod turntable = humanObj.raffleInfo.luckTurntable;
		turntable.modeType = modeType;
		turntable.lv = lv;
		turntable.sn = this.getLuckTurnTableSn(modeType, lv);
		
		SCOpenLuckTurntable.Builder msg = SCOpenLuckTurntable.newBuilder();
		msg.setModeType(modeType);
		msg.setLv(lv);
		msg.setSn(turntable.sn);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 摇奖转盘
	 * @param humanObj
	 */
	public void _msg_CSLuckTurntable(HumanObject humanObj) {
		// 获得转盘信息
		LuckTurntableMod turntable = humanObj.raffleInfo.luckTurntable;
		int sn = turntable.sn;

		ConfLuckTurntable conf = ConfLuckTurntable.get(sn);
		if(conf == null) {
			Log.table.debug("=== 幸运转盘异常 ===");
			humanObj.sendSysMsg(380101);
			return;
		}
		
		// vip等级
		int vipLv = humanObj.getHuman().getVipLevel();
		// 获得抽奖次数
		int limitCount = conf.limitCount[vipLv];
		// 当前次数
		int count = turntable.count;
		if (count >= limitCount) {
			Log.table.debug("=== 超过抽奖次数，请提升特权 ===");
			humanObj.sendSysMsg(380102);
			return;
		}
		
		// 判断消耗
		if (!RewardHelper.checkAndConsume(humanObj, conf.costItem, conf.costNum[count], LogSysModType.RaffleLuckTurntable)) {
			return;
		}
		// 如果当前信息中的权重列表为空，则从配置中获得当前权重列表
		if (turntable.weights.isEmpty()) {
			turntable.weights = Utils.intToIntegerList(conf.weights);
		}
		// 
		int index = Utils.getRandRange(turntable.weights);
		
		int itemSn = conf.itemList[index];
		int itemNum = conf.itemNumList[index];
		RewardHelper.reward(humanObj, itemSn, itemNum, LogSysModType.RaffleLuckTurntable);
		
		turntable.count++;
		turntable.weights.set(index, 0);
		
		SCLuckTurntable.Builder msg = SCLuckTurntable.newBuilder();
		msg.setCount(turntable.count);
		msg.setResultIndex(index);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 离开转盘
	 * @param humanObj
	 */
	public void _msg_CSLeaveLuckTurntable(HumanObject humanObj) {
		// 获得转盘信息
		LuckTurntableMod turntable = humanObj.raffleInfo.luckTurntable;
		turntable.reset();
		
		SCLeaveLuckTurntable.Builder msg = SCLeaveLuckTurntable.newBuilder();
		humanObj.sendMsg(msg);
	}
	
	
	
	
	///////////////////////////////////////
	/**
	 * 根据玩法类型和等级，权重随机出当前等级的转盘sn
	 * @param modeType
	 * @param lv
	 * @return
	 */
	private int getLuckTurnTableSn(EModeType modeType, int lv) {
		// 获取配置表
		ConfLuckTurntableWeight conf = ConfLuckTurntableWeight.get(ConfigKeyFormula.getLuckTurntableWeightSn(modeType.getNumber(), lv));
		if(conf == null) {
			Log.table.error("=== 幸运转盘权重表异常 ===");
			return 0;
		}
		if (conf.weight.length != conf.luckTurnTtablSn.length) {
			Log.table.error("=== 幸运转盘异常，权重和sn长度不一样 ===");
			return 0;
		}
		// 权重出index
		int index = Utils.getRandRange(conf.weight);
		return conf.luckTurnTtablSn[index];
	}
	
}
