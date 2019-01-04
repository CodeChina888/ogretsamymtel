package turnbasedsrv.stageCond;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.DTurnbasedBuff;
import game.msg.Define.EPosType;
import game.msg.Define.ETeamType;
import game.worldsrv.param.ParamManager;
import turnbasedsrv.buff.Buff;
import turnbasedsrv.enumType.AtkType;
import turnbasedsrv.enumType.StageOpType;
import turnbasedsrv.enumType.StageStep;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.stage.FightStageObject;

public class CondRoundPVE extends StageCondBase {

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondRoundPVE(int id, String value) {
		super(id);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return StageCondDefine.RoundPVE;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param stageObj
	 */
	@Override
	public void init(FightStageObject stageObj) {
		// 增加检测场景结束
		stageObj.addStageFinishCheck(this);
		// 增加检测场景阶段结束
		stageObj.addStepFinishCheck(StageStep.StageWaitFighter, this);// 等待战斗者进入场景
		stageObj.addStepFinishCheck(StageStep.StageStart, this);// 场景开始
		stageObj.addStepFinishCheck(StageStep.StageStepStart, this);// 场景阶段开始
		stageObj.addStepFinishCheck(StageStep.RoundStart, this);// 回合开始
		stageObj.addStepFinishCheck(StageStep.RoundOrderStart, this);// 回合普攻前
		stageObj.addStepFinishCheck(StageStep.RoundOrderEnd, this);// 回合普攻后
		stageObj.addStepFinishCheck(StageStep.RoundWaitEnd, this);// 等待回合结束
		stageObj.addStepFinishCheck(StageStep.RoundEnd, this);// 回合结束
		stageObj.addStepFinishCheck(StageStep.StageStepEnd, this);// 场景阶段结束
		// 增加场景阶段前置行为
		stageObj.addStepBeforeAction(StageStep.StageStepStart, this);// 场景阶段开始
		stageObj.addStepBeforeAction(StageStep.RoundStart, this);// 回合开始
		stageObj.addStepBeforeAction(StageStep.RoundOrderEnd, this);// 回合普攻后
		// 增加场景阶段行为
		stageObj.addStepAction(StageStep.StageStart, this);// 场景开始
		stageObj.addStepAction(StageStep.RoundOrderStart, this);// 回合普攻前
		stageObj.addStepAction(StageStep.RoundWaitEnd, this);// 等待回合结束
	}

	/**
	 * 执行场景阶段前置行为
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepActionBefore(FightStageObject stageObj) {
		switch (stageObj.stageStep) {
		case StageStepStart:
			stageObj.step = stageObj.step + 1;
			stageObj.round = 0;
			stageObj.nowOrderIsSpecial = false;
			stageObj.nowOrderPosition = 0;
			stageObj.doStageOp(StageOpType.InitFightObj, null);
			stageObj.doStageOp(StageOpType.ActionInitFightObj, null);
			stageObj.doStageOp(StageOpType.SendStepInfoMsg, null);
			return;
		case RoundStart:
			stageObj.round = stageObj.round + 1;
			stageObj.nowOrderIsSpecial = false;
			stageObj.nowOrderPosition = 0;

			// 技能出手顺序
			stageObj.orderList.clear();
			stageObj.lastOrderList.clear();
			stageObj.lastSpecialOrderList.clear();

			if (stageObj.priorTeam == ETeamType.Team1) {
				int[] orders = { 100, 0, 101, 1, 102, 2, 103, 3, 104, 4, 105, 5, 106, 6, 107, 7, 108, 8, 109, 9 };
				for (int i = 0; i < orders.length; i++) {
					stageObj.orderList.add(orders[i]);
				}
			} else {
				int[] orders = { 0, 100, 1, 101, 2, 102, 3, 103, 4, 104, 5, 105, 6, 106, 7, 107, 8, 108, 9, 109 };
				for (int i = 0; i < orders.length; i++) {
					stageObj.orderList.add(orders[i]);
				}
			}

			// 回合开始时，是否有需要移除的buff
			List<DTurnbasedBuff> buffRemoveList = new ArrayList<>();
			for (FightObject obj : stageObj.getFightObjs().values()) {
				List<Buff> buffList = obj.buffManager.getAllBuff();
				for (Buff buff : buffList) {
					if (buff.round == 0) {
						obj.buffManager.removeBuff(buff.fightId, true);
						buffRemoveList.add(buff.creageMsg());
					}
				}
			}
			// 跳过战斗，不发消息
			if (!stageObj.isQuickFight) {
				stageObj.sendRoundChangeInfoMsg(buffRemoveList);
			}
			return;
		case RoundOrderEnd:
			if (stageObj.nowOrderIsSpecial) {
				break;
			}
			FightObject fightObj = stageObj.getObjByFightPos(stageObj.nowOrderPosition);
			if (fightObj == null) {
				break;
			}
			if (fightObj.isDie()) {
				break;
			}
			List<Buff> buffList = fightObj.buffManager.getAllBuff();
			for (Buff buff : buffList) {
				if (fightObj.isDie()) {
					break;
				}
				if (!fightObj.buffManager.hasBuff(buff.fightId)) {
					continue;
				}
				if (buff.isOrderCheckBuff() && buff.round > 0) {
					buff.round--;
					buff.orderTrigger();
				} else if (buff.isRoundCheckBuff() && buff.round > 0) {
					buff.round--;
				}
			}
			return;
		default:
			break;
		}
	}

	/**
	 * 执行场景阶段行为
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepAction(FightStageObject stageObj) {
		switch (stageObj.stageStep) {
		case StageStart:
			break;
		case RoundStart:
			break;
		case RoundOrderStart: {
			// 是否还有需要出手的单位
			autoFight(stageObj);

			// 释放怒气技
			boolean isCast = castSpecialSkill(stageObj);
			if (!isCast) {
				// 释放普通技
				castCommonSkill(stageObj);
			}
		}
			break;
		case RoundOrderEnd:
			break;
		case RoundWaitEnd:
			stageObj.sendWaitHumanCheckRoundEnd();
			break;
		case RoundEnd:
			break;
		default:
			break;
		}
	}

	/**
	 * 释放怒气技
	 */
	private boolean castSpecialSkill(FightStageObject stageObj) {
		boolean isCast = false;
		// 排除无效的
		Iterator<Integer> ite = stageObj.specialOrderList.iterator();
		while (ite.hasNext()) {
			int pos = ite.next();
			if (stageObj.getObjByFightPos(pos) == null) {
				ite.remove();
			}
		}

		// 按顺序出手
		if (stageObj.specialOrderList.size() > 0) {
			Integer pos = stageObj.specialOrderList.remove(0);
			stageObj.lastSpecialOrderList.add(pos);
			stageObj.nowOrderIsSpecial = true;
			stageObj.nowOrderPosition = pos;
			FightObject fightObj = stageObj.getObjByFightPos(pos);
			fightObj.castSpecialSkill();
			isCast = true;
		}
		return isCast;
	}

	/**
	 * 释放普通技
	 */
	private boolean castCommonSkill(FightStageObject stageObj) {
		boolean isCast = false;
		// 排除无效的
		Iterator<Integer> ite = stageObj.orderList.iterator();
		while (ite.hasNext()) {
			int pos = ite.next();
			if (stageObj.getObjByFightPos(pos) == null) {
				ite.remove();
			}
		}

		// 按顺序出手
		if (stageObj.orderList.size() > 0) {
			Integer pos = stageObj.orderList.remove(0);
			stageObj.lastOrderList.add(pos);
			stageObj.nowOrderIsSpecial = false;
			stageObj.nowOrderPosition = pos;
			
			FightObject fightObj = stageObj.getObjByFightPos(pos);
			// 玩家且非自动战斗，则等待玩家选取技能
			if (fightObj.isHuman()) {
				boolean isAuto = stageObj.autoFightMap.get(fightObj.team);
				if (!isAuto) {
					stageObj.stopFight(true, ParamManager.PVEHumanCastTime, fightObj.idFight);
				}
			}
			// 非暂停状态则自动施法
			if (!stageObj.isStop) {
				if (fightObj.canCastCommonSkill()) {
					fightObj.castCommonSkill();
					isCast = true;
				}
			}
		}
		return isCast;
	}

	private void autoFight(FightStageObject stageObj) {
		stageObj.checkAutoFightReplay();
		if (stageObj.isReplay) {
			stageObj.checkCastSkillReplay();
			return;
		}

		List<ETeamType> list = new ArrayList<>();
		if (stageObj.priorTeam == ETeamType.Team1) {
			list.add(ETeamType.Team1);
			list.add(ETeamType.Team2);
		} else {
			list.add(ETeamType.Team2);
			list.add(ETeamType.Team1);
		}
		
		List<Integer> sendList = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			ETeamType team = list.get(i);
			Boolean isAuto = stageObj.autoFightMap.get(team);
			if (isAuto == null || isAuto == false) {
				continue;
			}
			for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
				FightObject obj = stageObj.getObjByPos(pos, team);
				if (obj == null) {
					continue;
				}
				// 是否可以施放特殊技能
				if (!obj.canCastSpecialSkill()) {
					continue;
				}
				// 技能判断
				int fightPos = obj.getFightPos();
				// 已经在等待列表中
				if (stageObj.specialOrderList.contains(fightPos)) {
					continue;
				}
				// 本回合已经释放过了
				if (stageObj.lastSpecialOrderList.contains(fightPos)) {
					continue;
				}
				// 治疗技能特殊处理
				if(obj.specialSkill.confSkill != null 
					&& obj.specialSkill.confSkill.atkType == AtkType.Cure.value()) {
					boolean needCure = false;
					for (FightObject obj1 : stageObj.getFightObjs().values()) {
						if (obj1.team == obj.team) {
							if(PropManager.inst().getHpPct(obj1) <= FightStageObject.CurePct) {
								needCure = true;
								break;
							}
						}
					}
					if(!needCure){
						continue;
					}
				}
				stageObj.addSpecialList(fightPos);
				sendList.add(obj.idFight);
			}
		}

		if (!sendList.isEmpty()) {
			stageObj.sendSpecialOrderList(sendList);
		}
		stageObj.checkCastSkillReplay();
	}

	/**
	 * 检测场景阶段是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean checkStepFinish(FightStageObject stageObj) {
		// 判断怪物是否全死光了
		if (stageObj.getTeamCount(ETeamType.Team2) == 0) {
			if (stageObj.step > 0 && stageObj.round > 0) {
				return true;
			}
		}

		switch (stageObj.stageStep) {
		case StageWaitFighter:
			if (stageObj.checkHumanLoginOk()) {
				return true;
			}
			break;
		case StageStart:
			return true;
		case StageStepStart:
			if (stageObj.step == 1) {
				if (stageObj.checkHumanReadyOk()) {
					return true;
				}
				return false;
			}
			return true;
		case StageStepEnd:
			return true;
		case StageEnd:
			return true;
		case RoundStart:
			return true;
		case RoundOrderStart:
			return true;
		case RoundOrderEnd:
			return true;
		case RoundWaitEnd:
			if (stageObj.checkHumanRoundEndOk()) {
				return true;
			}
			return false;
		case RoundEnd:
			return true;
		default:
			break;

		}
		return false;
	}

	/**
	 * 设置下一个场景阶段
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public void setNextStep(FightStageObject stageObj, StageStep nowStep) {
		// 判断怪物是否全死光了
		if (stageObj.getTeamCount(ETeamType.Team2) == 0) {
			if (stageObj.step > 0 && stageObj.round > 0) {
				stageObj.isStop = true;
				stageObj.initNextScene();
				stageObj.setStageStep(StageStep.StageStepStart);
				return;
			}
		}
		// 判断阶段是否结束
		if (!checkStepFinish(stageObj)) {
			return;
		}
		switch (stageObj.stageStep) {
		case StageWaitFighter:
			stageObj.setStageStep(StageStep.StageStart);
			return;
		case StageStart:
			stageObj.setStageStep(StageStep.StageStepStart);
			return;
		case StageStepStart:
			stageObj.setStageStep(StageStep.RoundStart);
			return;
		case StageStepEnd:
			stageObj.setStageStep(StageStep.StageStepStart);
			return;
		case RoundStart:
			stageObj.setStageStep(StageStep.RoundOrderStart);
			return;
		case RoundOrderStart:
			stageObj.setStageStep(StageStep.RoundOrderEnd);
			return;
		case RoundOrderEnd:
			// 是否还有需要出手的单位
			autoFight(stageObj);
			// 是否要继续释放特殊技能
			if (continueSpecialOrder(stageObj)) {
				stageObj.setStageStep(StageStep.RoundOrderStart);
				return;
			}
			// 是否要继续释放普通技能
			if (continueCommonOrder(stageObj)) {
				stageObj.setStageStep(StageStep.RoundOrderStart);
				return;
			}
			// 跳过战斗
			if (stageObj.isQuickFight) {
				stageObj.setStageStep(StageStep.RoundEnd);
				return;
			}
			stageObj.setStageStep(StageStep.RoundWaitEnd);
			return;
		case RoundWaitEnd:
			stageObj.setStageStep(StageStep.RoundEnd);
			return;
		case RoundEnd:
			stageObj.setStageStep(StageStep.RoundStart);
			return;
		default:
			break;
		}
	}

	/**
	 * 是否要继续释放特殊技能
	 * 
	 * @param stageObj
	 * @return
	 */
	private boolean continueSpecialOrder(FightStageObject stageObj) {
		boolean isContinue = false;
		// 排除无效的
		Iterator<Integer> ite = stageObj.specialOrderList.iterator();
		while (ite.hasNext()) {
			int pos = ite.next();
			if (stageObj.getObjByFightPos(pos) == null) {
				ite.remove();
			}
		}
		if (stageObj.specialOrderList.size() > 0) {
			// 是否可以施放
			for (int i = 0; i < stageObj.specialOrderList.size(); i++) {
				int pos = stageObj.specialOrderList.get(i);
				FightObject fightObj = stageObj.getObjByFightPos(pos);
				if (fightObj.canCastSpecialSkill()) {
					isContinue = true;
				}
			}
		}
		return isContinue;
	}

	/**
	 * 是否要继续释放普通技能
	 * 
	 * @param stageObj
	 * @return
	 */
	private boolean continueCommonOrder(FightStageObject stageObj) {
		boolean isContinue = false;
		// 排除无效的
		Iterator<Integer> ite2 = stageObj.orderList.iterator();
		while (ite2.hasNext()) {
			int pos = ite2.next();
			if (stageObj.getObjByFightPos(pos) == null) {
				ite2.remove();
			}
		}
		if (stageObj.orderList.size() > 0) {
			isContinue = true;
		}
		return isContinue;
	}

	/**
	 * 检测场景是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean checkStageFinish(FightStageObject stageObj) {
		if (stageObj.stageStep == StageStep.StageWaitFighter || stageObj.stageStep == StageStep.StageStart) {
			return false;
		}
		if (stageObj.stageStep == StageStep.RoundEnd && stageObj.round >= stageObj.roundMax) {
			return true;
		}
		// 判断挑战方是否全死光了
		if (stageObj.getTeamCount(ETeamType.Team1) == 0) {
			return true;
		}
		// 判断怪物是否全死光了
		if (stageObj.getTeamCount(ETeamType.Team2) == 0) {
			if (stageObj.step > 0 && stageObj.round > 0 && !stageObj.isLastScene()) {
				return true;
			}
		}
		return false;
	}

}
