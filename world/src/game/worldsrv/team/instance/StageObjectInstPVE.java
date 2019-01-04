package game.worldsrv.team.instance;

import java.util.ArrayList;
import java.util.List;

import core.support.Time;
import game.msg.Define.DInstEnd;
import game.msg.Define.DPVEHarm;
import game.msg.Define.EWorldObjectType;
import game.msg.MsgInstance.SCInstEnd;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.MonsterObject;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.TeamBundleType;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.team.TeamServiceProxy;
import game.worldsrv.team.instance.StageObjectInstTeam;
import game.worldsrv.team.instance.TeamInstanceManager;

/**
 * PVE副本：生存，塔防，BOSS战
 * @author shenjh
 */
public class StageObjectInstPVE extends StageObjectInstTeam {

	public StageObjectInstPVE(StagePort port, long stageId, int stageSn, int mapSn, boolean isMonsterAddProp) {
		super(port, stageId, stageSn, mapSn, isMonsterAddProp);

	}

	@Override
	public void pulse() {
		super.pulse();// 最后再调用通用的心跳处理

		if (this.endType < 0) {// 检查副本是否该结束了
			checkInstEnd();
		}

		checkInstDestory();// 检查副本是否该销毁了
	}

	/**
	 * 检查副本是否该结束了
	 */
	private void checkInstEnd() {
		long timeCur = this.getTime();// 当前时间戳
		// 副本开始5秒后才执行判断胜负
		if (timeCur - this.milliSecStart > (long) (5 * Time.SEC)) {
//			if (this.confInstStage.failWhenBossDied > 0 || this.confInstStage.winWhenBossDied > 0
//					|| this.confInstStage.winAfterSection > 0) {
//				// 有配置打死BOSS失败或胜利或清完波数胜利
//				if (this.confInstStage.winAfterSection > 0) {
//					// 清完指定波数的所有怪后胜利
//					if (madeIndexCur >= this.confInstStage.winAfterSection && this.isMonsterAllDie()) {
//						this.endType = 1; // 1胜利
//						this.milliSecEnd = timeCur; // 记录副本结束时间戳
//					}
//				}
//				if (this.confInstStage.winWhenBossDied > 0) {
//					// 指定BOSS死亡胜利
//					if (this.isBossDie(this.confInstStage.winWhenBossDied)) {
//						this.endType = 1; // 1胜利
//						this.milliSecEnd = timeCur; // 记录副本结束时间戳
//					}
//				} else if (this.confInstStage.failWhenBossDied > 0) {
//					// 指定BOSS死亡失败
//					if (this.isBossDie(this.confInstStage.failWhenBossDied)) {
//						this.endType = 0; // 0失败
//						this.milliSecEnd = timeCur; // 记录副本结束时间戳
//					}
//				}
//			} else {
				if (this.confInstActConfig.reviveType > 0 && this.confInstStage.limitSec > 0) {
					// 可复活的活动副本要打到时间到才算结束
					this.milliSecEnd = this.milliSecStart + this.confInstStage.limitSec * Time.SEC;// 记录副本结束时间戳
				} else {
					// 不可复活的活动副本只要怪物死光了就算结束了
					if (this.getMonsterObjs().isEmpty()) {// 怪物死光了
						this.endType = 1; // 1胜利
						this.milliSecEnd = timeCur; // 记录副本结束时间戳
					}
				}
//			}
		}
	}

	/**
	 * 检查副本是否该销毁了
	 */
	private void checkInstDestory() {
		long timeCur = this.getTime();// 当前时间戳
		// 限时副本，按副本结束时间戳，延时几秒后关闭
		long timeInterval = timeCur - this.milliSecEnd;
		if (!isSendInstanceEnd) {// 还未通知玩家结束副本，检查是否胜利通关或失败超时
			// 判断是否时间到了该结束或销毁副本
			if (timeInterval >= 0) {// 时间到了，通知副本结束
				sendInstEnd();
			}
		} else {// 已经通知玩家结束副本，延时清离玩家以及销毁副本
			if (timeInterval > DESTROY_TIME) {// 该销毁副本了，有人还在则踢出去，没人就销毁副本
				// 副本开启后，如果一定时间内副本没人则销毁副本
				if (this.getHumanObjs().isEmpty()) {
					// Log.human.debug("===副本没人了");
					this.destory();
				} else {
					// Log.human.debug("===延时几秒后请离副本");
					this.milliSecEnd = timeCur;// 再过一定时间后继续检查是否没人了，没人则销毁副本
					for (HumanObject humanObj : this.getHumanObjs().values()) {
						TeamInstanceManager.inst()._msg_CSInstanceLeave(humanObj);
					}
				}
			}
		}
	}

	/**
	 * 通知副本结束
	 */
	private void sendInstEnd() {
		if (isSendInstanceEnd)
			return;

		List<HumanObject> listHumanObj = new ArrayList<>();
		listHumanObj.addAll(this.getHumanObjs().values());

		int endType = this.endType;// 结束类型：0失败，1胜利，2平局（PVE或PVP才下发）
		if (endType < 0) {// 未通关，需判断限时类型：0失败，1胜利
//			if (this.confInstStage.tmLimitType == 0)
//				endType = 0;// 时间到了认为通关失败
//			else {
//				if (isHumanAllDie(listHumanObj))
//					endType = 0;// 时间到了但人全死了，则失败
//				else
//					endType = 1;// 时间到了认为通关胜利
//			}
		}

		List<DPVEHarm> listDPVEHarm = getDPVEHarm(listHumanObj);// 获取本队的PVE结算
		int teamId = 0;
		for (HumanObject humanObj : listHumanObj) {
			humanObj.setTeamStartClose();// 设置为结束副本
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家不下发副本结束，也无奖励，即认为是死人

			if (humanObj.getTeam() != null)
				teamId = humanObj.getTeam().teamId;
			else
				Log.human.error("===PVE副本中的人缺少队伍信息？humanObj.team is null");

			SCInstEnd.Builder msg = SCInstEnd.newBuilder();
			// 给予副本区域掉落奖励
			//List<DTriggerDrop> listDTriggerDrop = new ArrayList<>();// 副本区域掉落物品记录
			//if (endType == 1) {// 1胜利，才触发副本区域掉落
			//	listDTriggerDrop = InstanceManager.inst().getTriggerDrop(stageSn, Utils.I10000, false);
			//}
			int second = (int) ((this.milliSecEnd - this.milliSecStart) / Time.SEC);
			//DInstEnd.Builder dInstEnd = InstanceManager.inst().giveRepReward(LogSysModType.ActInst, humanObj,  
			//		listDTriggerDrop, stageSn, second, false);
			//msg.addInstEnd(dInstEnd);// 副本结束奖励
			// 下发活动副本配表奖励
			ProduceManager.inst().giveActInstAward(humanObj, msg, endType, confInstActConfig);
			//msg.addAllDPVEHarm(listDPVEHarm);
			humanObj.sendMsg(msg);
		}

		// 记录已发送过结束副本
		isSendInstanceEnd = true;
		if (teamId > 0) {
			// 通知队伍结束副本
			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamEndRep(teamId);
		}
	}

	/**
	 * 是否所有玩家都死了
	 */
	private boolean isHumanAllDie(List<HumanObject> listHumanObj) {
		boolean ret = true;
		for (HumanObject humanObj : listHumanObj) {
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家不下发副本结束，也无奖励，即认为是死人
			if (!humanObj.isDie()) {
				ret = false;// 只要有1人没死就算没全死
				break;
			}
		}
		return ret;
	}

	/**
	 * 是否所有敌方怪物都死了
	 */
	private boolean isMonsterAllDie() {
		boolean ret = false;
		int num = 0;// 计算敌方怪物数量
		for (MonsterObject monObj : this.getMonsterObjs().values()) {
			if (null == monObj.confProperty)
				continue;
			
			if (monObj.confProperty.roleType >= EWorldObjectType.Monster_VALUE && 
					monObj.confProperty.roleType <= EWorldObjectType.MonsterBoss_VALUE)
				num++;
		}
		if (num == 0)
			ret = true;
		return ret;
	}

	/**
	 * 是否指定BOSS怪物已死
	 */
	private boolean isBossDie(int monsterSn) {
		boolean ret = true;
		for (MonsterObject monObj : this.getMonsterObjs().values()) {
			if (monObj.confProperty != null && monObj.confProperty.sn == monsterSn) {
				ret = false;
				break;
			}
		}
		return ret;
	}

}
