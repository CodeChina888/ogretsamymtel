package game.worldsrv.team.instance;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.support.Time;

import game.msg.Define.DPVPKill;
import game.msg.MsgInstance.SCInstEnd;
import game.worldsrv.character.HumanObject;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Utils;

import game.worldsrv.team.TeamServiceProxy;
import game.worldsrv.team.instance.StageObjectInstTeam;
import game.worldsrv.team.instance.TeamInstanceManager;

/**
 * PVP副本：1V1，3V3，5V5
 * @author shenjh
 */
public class StageObjectInstPVP extends StageObjectInstTeam {

	private int winTeamId = 0;// 赢的队伍ID：0代表平局，>0代表赢的队伍ID

	public StageObjectInstPVP(StagePort port, long stageId, int stageSn, int mapSn, boolean isMonsterAddProp) {
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
		if (timeCur - this.milliSecStart > 5 * Time.SEC) {
			if (this.confInstActConfig.reviveType > 0 && this.confInstStage.limitSec > 0) {
				// 可复活的活动副本要打到时间到才算结束
				this.milliSecEnd = this.milliSecStart + this.confInstStage.limitSec * Time.SEC;// 记录副本结束时间戳
			} else {
				// 不可复活的活动副本只要有一队死光了就算结束了
				if (this.isPass()) {// 副本结束
					this.milliSecEnd = timeCur; // 记录副本结束时间戳
				}
			}
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

		Map<Integer, List<HumanObject>> mapTeamHumanObj = this.getTeamHumanObj();
		for (Entry<Integer, List<HumanObject>> entry : mapTeamHumanObj.entrySet()) {
			int teamId = Utils.intValue(entry.getKey());
			List<HumanObject> listHumanObj = entry.getValue();
			// 通知队伍结束副本
			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamEndRep(teamId);

			int endType = 0;// 结束类型：0失败，1胜利，2平局（PVE或PVP才下发）
			if (winTeamId == teamId) {
				endType = 1;// 胜利
			} else if (winTeamId == 0) {// 平局的话，需要根据复活模式进一步确认输赢
				int winTeamId = getWinTeamId();
				if (winTeamId == 0)
					endType = 2;// 平局
				else if (winTeamId == teamId)
					endType = 1;// 胜利
				else
					endType = 0;// 失败
			}
			// Log.human.debug("===sendInstanceEnd teamId={},endType={}",
			// teamId, endType);

			List<DPVPKill> listDPVPKill = getDPVPKill(listHumanObj);// 获取本队的PVP结算

			for (HumanObject humanObj : listHumanObj) {
				humanObj.setTeamStartClose();// 设置为结束副本
				if (humanObj.isInCloseDelay)
					continue;// 处于断线延迟状态的玩家不下发副本结束，也无奖励，即认为是死人

				SCInstEnd.Builder msg = SCInstEnd.newBuilder();
				// 下发活动副本配表奖励
				ProduceManager.inst().giveActInstAward(humanObj, msg, endType, confInstActConfig);
				//msg.addAllDPVPKill(listDPVPKill);
				// 下发消息
				humanObj.sendMsg(msg);
			}
		}
		// 记录已发送过结束副本
		isSendInstanceEnd = true;
	}

	/**
	 * 是否该队伍的人全死光了
	 */
	private boolean isTeamAllDie(List<HumanObject> listHumanObj) {
		boolean dieAll = true;// 这队死光了没？
		for (HumanObject humanObj : listHumanObj) {
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家不参与判断，即认为是死人

			if (!humanObj.isDie()) {
				dieAll = false;
				break;
			}
		}
		return dieAll;
	}

	/**
	 * 是否副本结束
	 */
	private boolean isPass() {
		boolean isPass = true;
		// 只有一个队 或 其中一队死光了，就算结束
		Map<Integer, List<HumanObject>> mapTeamHumanObj = this.getTeamHumanObj();
		if (mapTeamHumanObj.size() == 2) {// 2个队伍
			int[] ids = new int[2];
			boolean[] die = new boolean[2];
			int index = 0;
			for (Entry<Integer, List<HumanObject>> entry : mapTeamHumanObj.entrySet()) {
				ids[index] = Utils.intValue(entry.getKey());
				die[index] = isTeamAllDie(entry.getValue());
				index++;
			}
			if (die[0] && !die[1]) {// 0队全死光且1队有活口
				winTeamId = ids[1];
			} else if (!die[0] && die[1]) {// 0队有活口且1队全死光
				winTeamId = ids[0];
			} else if (die[0] && die[1]) {// 0队全死光且1队全死光
				winTeamId = 0;
			} else {// 还没打完
				isPass = false;
			}
		} else if (mapTeamHumanObj.size() == 1) {// 1个队伍，说明对手掉线或没进来
			for (Entry<Integer, List<HumanObject>> entry : mapTeamHumanObj.entrySet()) {
				winTeamId = Utils.intValue(entry.getKey());
				break;
			}
		}
		return isPass;
	}

	/**
	 * 获取赢的队伍ID：根据复活模式不同，输赢判断不同 可复活的模式：输赢判断依据=击杀数量 不可复活的模式：输赢判断依据=活口个数和剩余血量百分比
	 * @return
	 */
	private int getWinTeamId() {
		int winTeamId = 0;// 0表示平局
		Map<Integer, List<HumanObject>> mapTeamHumanObj = this.getTeamHumanObj();
		if (this.confInstActConfig.reviveType > 0) {
			// 可复活的模式：输赢判断依据=击杀数量
			if (mapTeamHumanObj.size() == 2) {// 2个队伍
				int[] ids = new int[2];
				int[] killNum = new int[2];// 击杀个数
				int index = 0;
				for (Entry<Integer, List<HumanObject>> entry : mapTeamHumanObj.entrySet()) {
					ids[index] = Utils.intValue(entry.getKey());
					killNum[index] = 0;
					for (HumanObject humanObj : entry.getValue()) {
						if (humanObj.isInCloseDelay) {
							continue;// 处于断线延迟状态的玩家不参与判断，即认为是死人
						}
						killNum[index] += humanObj.dPVPKill.getKill();
					}
					index++;
				}
				for (int i = 0; i < 2; i++) {
					if (killNum[0] > killNum[1])
						winTeamId = ids[0];// 击杀多的胜
					else if (killNum[0] < killNum[1])
						winTeamId = ids[1];// 击杀多的胜
					else
						// 击杀一样，则平局
						winTeamId = 0;
				}
			}
		} else {
			// 不可复活的模式：输赢判断依据=活口个数和剩余血量百分比
			if (mapTeamHumanObj.size() == 2) {// 2个队伍
				int[] ids = new int[2];
				int[] aliveNum = new int[2];// 活口个数
				int[] leftHpPercent = new int[2];// 剩余血量百分比
				int index = 0;
				for (Entry<Integer, List<HumanObject>> entry : mapTeamHumanObj.entrySet()) {
					ids[index] = Utils.intValue(entry.getKey());
					aliveNum[index] = 0;
					leftHpPercent[index] = 0;
					for (HumanObject humanObj : entry.getValue()) {
						if (humanObj.isInCloseDelay) {
							continue;// 处于断线延迟状态的玩家不参与判断，即认为是死人
						}
						aliveNum[index]++;
						leftHpPercent[index] += (int) (humanObj.getHuman().getHpCur() * Utils.D100 / humanObj
								.getHuman().getHpMax());
					}
					index++;
				}
				for (int i = 0; i < 2; i++) {
					if (aliveNum[0] > aliveNum[1])
						winTeamId = ids[0];// 活口多的胜
					else if (aliveNum[0] < aliveNum[1])
						winTeamId = ids[1];// 活口多的胜
					else if (leftHpPercent[0] > leftHpPercent[1])
						winTeamId = ids[0];// 活口一样，剩余血量百分比多的胜
					else if (leftHpPercent[0] < leftHpPercent[1])
						winTeamId = ids[1];// 活口一样，剩余血量百分比多的胜
					else
						// 活口一样，剩余血量百分比一样，则平局
						winTeamId = 0;
				}
			}
		}
		return winTeamId;
	}

}
