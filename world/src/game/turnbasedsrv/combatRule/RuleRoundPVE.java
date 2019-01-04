package game.turnbasedsrv.combatRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.DTurnbasedBuff;
import game.msg.Define.EPosType;
import game.msg.Define.ETeamType;
import game.turnbasedsrv.buff.Buff;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.AtkType;
import game.turnbasedsrv.enumType.CombatOpType;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.param.FightParamBase;
import game.turnbasedsrv.prop.PropManager;
import game.worldsrv.param.ParamManager;

public class RuleRoundPVE extends CombatRuleBase {

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public RuleRoundPVE(int id, String value) {
		super(id);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return CombatRuleDefine.RoundPVE;
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
	public void init(CombatObject stageObj) {
		// 增加检测场景结束
		stageObj.addStageFinishCheck(this);
		// 增加主角施放技能操作
		stageObj.addStageOpAction(CombatOpType.HumanCommmonSkill, this); // 主角释放技能操作
		
		// 增加检测场景阶段结束
		stageObj.addStepFinishCheck(CombatStepType.CombatWaitFighter, this);// 等待战斗者进入场景
		stageObj.addStepFinishCheck(CombatStepType.CombatStart, this);// 场景开始
		stageObj.addStepFinishCheck(CombatStepType.StepStart, this);// 场景阶段开始
		stageObj.addStepFinishCheck(CombatStepType.RoundStart, this);// 回合开始
		stageObj.addStepFinishCheck(CombatStepType.RoundOrderStart, this);// 回合普攻前
		// TODO 
//		stageObj.addStepFinishCheck(CombatStepType.RoundOrderWaitEnd, this);// 回合普攻前
		stageObj.addStepFinishCheck(CombatStepType.RoundOrderEnd, this);// 回合普攻后
		stageObj.addStepFinishCheck(CombatStepType.RoundWaitEnd, this);// 等待回合结束
		stageObj.addStepFinishCheck(CombatStepType.RoundEnd, this);// 回合结束
		stageObj.addStepFinishCheck(CombatStepType.StepEnd, this);// 场景阶段结束
		// 增加场景阶段前置行为
		stageObj.addStepBeforeAction(CombatStepType.StepStart, this);// 场景阶段开始
		stageObj.addStepBeforeAction(CombatStepType.RoundStart, this);// 回合开始
		stageObj.addStepBeforeAction(CombatStepType.RoundOrderEnd, this);// 回合普攻后
		// 增加场景阶段行为
		stageObj.addStepAction(CombatStepType.CombatStart, this);// 场景开始
		stageObj.addStepAction(CombatStepType.RoundOrderStart, this);// 回合普攻前
		stageObj.addStepAction(CombatStepType.RoundWaitEnd, this);// 等待回合结束
	}

	/**
	 * 执行场景阶段前置行为
	 * 
	 * @param stageObj
	 */
	@Override
	public void doStepActionBefore(CombatObject stageObj) {
		switch (stageObj.stageStep) {
		case StepStart:
			stageObj.step = stageObj.step + 1;
			stageObj.round = 0;
			stageObj.nowOrderIsSpecial = false;
			stageObj.nowOrderPosition = 0;
			stageObj.doStageOp(CombatOpType.InitFightObj, null);
			stageObj.doStageOp(CombatOpType.ActionInitFightObj, null);
			stageObj.doStageOp(CombatOpType.SendStepInfoMsg, null);
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
			// 出手结束时，是否有需要移除的buff
			buffRemoveList = new ArrayList<>();
			// 遍历所有对象
			for (FightObject fightObj : stageObj.getFightObjs().values()) {
				// 添加失效，净化/驱散的buff信息
				List<Buff> invalidBuffList = fightObj.buffManager.getAllInvalidBuff();
				for (Buff buff : invalidBuffList) {
					buffRemoveList.add(buff.creageMsg());
				}
			}
			// 获取出手对象
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
					if (buff.round == 0) {
						fightObj.buffManager.removeBuff(buff.fightId, true);
						buffRemoveList.add(buff.creageMsg());
					}
				}
			}
			// 跳过战斗，不发消息
			if (!stageObj.isQuickFight) {
				// 每次出手发送buff信息变化列表
				stageObj.sendRoundOrderEndMsg(buffRemoveList);
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
	public void doStepAction(CombatObject stageObj) {
		switch (stageObj.stageStep) {
		case CombatStart:
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
		case RoundOrderWaitEnd:
			stageObj.sendWaitHumanCheckRoundOrderEnd();
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
	private boolean castSpecialSkill(CombatObject stageObj) {
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
	private boolean castCommonSkill(CombatObject stageObj) {
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
			Integer pos = stageObj.orderList.get(0);
			FightObject fightObj = stageObj.getObjByFightPos(pos);
			
			// 主角出手 且 不是自动战斗 且 可以出手
			if (fightObj.isHuman() && !stageObj.autoFightMap.get(fightObj.team) && fightObj.canCastCommonSkill()) {
				// 设置战斗暂停
				stageObj.stopFight(true, ParamManager.PVEHumanCastTime, fightObj.idFight);
			} else {
				pos = stageObj.orderList.remove(0);
				stageObj.lastOrderList.add(pos);
				stageObj.nowOrderIsSpecial = false;
				stageObj.nowOrderPosition = pos;
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

	private void autoFight(CombatObject stageObj) {
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
							if(PropManager.inst().getHpPct(obj1) <= CombatObject.CurePct) {
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
	public boolean checkStepFinish(CombatObject stageObj) {
		// 判断怪物是否全死光了
		if (stageObj.getTeamCount(ETeamType.Team2) == 0) {
			if (stageObj.step > 0 && stageObj.round > 0) {
				return true;
			}
		}

		switch (stageObj.stageStep) {
		case CombatWaitFighter:
			if (stageObj.checkHumanLoginOk()) {
				return true;
			}
			break;
		case CombatStart:
			return true;
		case StepStart:
			if (stageObj.step == 1) {
				if (stageObj.checkHumanReadyOk()) {
					return true;
				}
				return false;
			}
			return true;
		case StepEnd:
			return true;
		case CombatEnd:
			return true;
		case RoundStart:
			return true;
		case RoundOrderStart:
			return true;
		case RoundOrderWaitEnd:
			if (stageObj.checkHumanRoundEndOk()) {
				return true;
			}
			return false;
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
	public void setNextStep(CombatObject stageObj, CombatStepType nowStep) {
		// 判断怪物是否全死光了
		if (stageObj.getTeamCount(ETeamType.Team2) == 0) {
			if (stageObj.step > 0 && stageObj.round > 0) {
				stageObj.isStop = true;
				stageObj.initNextScene();
				stageObj.setStageStep(CombatStepType.StepStart);
				return;
			}
		}
		// 判断阶段是否结束
		if (!checkStepFinish(stageObj)) {
			return;
		}
		switch (stageObj.stageStep) {
		case CombatWaitFighter:
			stageObj.setStageStep(CombatStepType.CombatStart);
			return;
		case CombatStart:
			stageObj.setStageStep(CombatStepType.StepStart);
			return;
		case StepStart:
			stageObj.setStageStep(CombatStepType.RoundStart);
			return;
		case StepEnd:
			stageObj.setStageStep(CombatStepType.StepStart);
			return;
		case RoundStart:
			stageObj.setStageStep(CombatStepType.RoundOrderStart);
			return;
		case RoundOrderStart:
			// TODO 每次出手都同步需要开启，后续跟客户端对接
//			// 跳过战斗
//			if (stageObj.isQuickFight) {
//				stageObj.setStageStep(CombatStepType.RoundOrderEnd);
//				return;
//			}
//			// 等待上一个对象出手结束
//			stageObj.setStageStep(CombatStepType.RoundOrderWaitEnd);
			
			stageObj.setStageStep(CombatStepType.RoundOrderEnd);
			return;
		case RoundOrderWaitEnd:
			// 等待上一个对象出手结束后，结束该出手回合
			stageObj.setStageStep(CombatStepType.RoundOrderEnd);
			return;
		case RoundOrderEnd:
			// 是否还有需要出手的单位
			autoFight(stageObj);
			// 是否要继续释放特殊技能
			if (continueSpecialOrder(stageObj)) {
				stageObj.setStageStep(CombatStepType.RoundOrderStart);
				return;
			}
			// 是否要继续释放普通技能
			if (continueCommonOrder(stageObj)) {
				stageObj.setStageStep(CombatStepType.RoundOrderStart);
				return;
			}
			// 跳过战斗
			if (stageObj.isQuickFight) {
				stageObj.setStageStep(CombatStepType.RoundEnd);
				return;
			}
			stageObj.setStageStep(CombatStepType.RoundWaitEnd);
			return;
		case RoundWaitEnd:
			stageObj.setStageStep(CombatStepType.RoundEnd);
			return;
		case RoundEnd:
			stageObj.setStageStep(CombatStepType.RoundStart);
			return;
		default:
			break;
		}
	}
	
	/**
	 * 处理操作，获取结果
	 * @param type
	 * @param param
	 */
	@Override
	public FightParamBase doStageOp(CombatObject stageObj, CombatOpType type, FightParamBase param){
		switch(type){
		case HumanCommmonSkill: {
			// 处理主角处理技能
			Integer pos = stageObj.orderList.remove(0);
			stageObj.lastOrderList.add(pos);
			stageObj.nowOrderIsSpecial = false;
			stageObj.nowOrderPosition = pos;
			
			// 玩家手动释放技能，停止计时器，解除暂停状态
			stageObj.stopCastSkill();
			FightObject fightObj = stageObj.getObjByFightPos(pos);
			if (fightObj.canCastCommonSkill()) {
				fightObj.castCommonSkill();
			}
		}
			break;
		case WaitHumanOp:
			
			break;
		default:
			break;
		}
		return null;
	}

	/**
	 * 是否要继续释放特殊技能
	 * 
	 * @param stageObj
	 * @return
	 */
	private boolean continueSpecialOrder(CombatObject stageObj) {
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
	private boolean continueCommonOrder(CombatObject stageObj) {
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
	public boolean checkStageFinish(CombatObject stageObj) {
		if (stageObj.stageStep == CombatStepType.CombatWaitFighter || stageObj.stageStep == CombatStepType.CombatStart) {
			return false;
		}
		if (stageObj.stageStep == CombatStepType.RoundEnd && stageObj.round >= stageObj.roundMax) {
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
