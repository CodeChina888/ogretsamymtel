package turnbasedsrv.effectCondition;

import java.util.List;

import core.support.ManagerBase;
import core.support.Utils;
import crosssrv.support.Log;
import game.worldsrv.config.ConfSkillEffect;
import turnbasedsrv.buff.Buff;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.param.ProcessParam;
import turnbasedsrv.param.SourceParam;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.trigger.Trigger;
import turnbasedsrv.value.ValueBase;

public class EffectCondManager extends ManagerBase {

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static EffectCondManager inst() {
		return inst(EffectCondManager.class);
	}

	/**
	 * 检查条件判断，字段说明： conditionType为条件类型， conditionOP为条件运算符， conditionParam1为比较右值，
	 * conditionParam2为额外参数
	 */
	public boolean checkEffectCond(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		boolean ret = false;
		int valueLeft = 0;// 比较左值
		int valueRight = 0;// 比较右值
		// 取比较左值
		EffectCondType type = EffectCondType.get(confEffect.conditionType);
		if (type == null) {
			Log.fight.error("未定义的条件类型：conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		} else {// 取类型值
			valueLeft = getCondValue(type, confEffect, source, process);
		}
		// 取比较右值
		EffectCondType type2 = EffectCondType.get(confEffect.conditionParam1);
		if (type2 == null) {// 不是类型，则是数值
			valueRight = Utils.intValue(confEffect.conditionParam1);
		} else {// 取类型值
			valueRight = getCondValue(type2, confEffect, source, process);
		}
		// 根据运算符比较左右值
		String conditionOP = confEffect.conditionOP;
		conditionOP.trim();
		switch (conditionOP) {
		case "<": { // 小于
			if (valueLeft < valueRight) {
				ret = true;
			}
		}
			break;
		case "<=": { // 小于等于
			if (valueLeft <= valueRight) {
				ret = true;
			}
		}
			break;
		case ">": { // 大于
			if (valueLeft > valueRight) {
				ret = true;
			}
		}
			break;
		case ">=": { // 大于等于
			if (valueLeft >= valueRight) {
				ret = true;
			}
		}
			break;
		case "==": { // 等于
			if (valueLeft == valueRight) {
				ret = true;
			}
		}
			break;
		case "!=": { // 不等于
			if (valueLeft != valueRight) {
				ret = true;
			}
		}
			break;
		}
		return ret;
	}

	private int getCondValue(EffectCondType type, ConfSkillEffect confEffect, SourceParam source,
			ProcessParam process) {
		int ret = 0;
		switch (type) {
		case Round: { // 当前回合数
			ret = getRound(confEffect, source, process);
		}
			break;
		case Probability: { // 万分比几率
			ret = getProbability(confEffect, source, process);
		}
			break;
		case CountFriend: { // 友方人数
			ret = getCountFriend(confEffect, source, process);
		}
			break;
		case CountEnemy: { // 敌方人数
			ret = getCountEnemy(confEffect, source, process);
		}
			break;

		case SelfProp: { // 自身某属性值
			ret = getSelfProp(false, confEffect, source, process);
		}
			break;
		case TargetProp: { // 目标某属性值
			ret = getTargetProp(false, confEffect, source, process);
		}
			break;
		case TriggerSelfProp: { // 触发者某属性值
			ret = getTriggerSelfProp(false, confEffect, source, process);
		}
			break;
		case TriggerTargetProp: { // 触发目标某属性值
			ret = getTriggerTargetProp(false, confEffect, source, process);
		}
			break;

		case SelfPropPct: { // 自身某属性万分比值
			ret = getSelfProp(true, confEffect, source, process);
		}
			break;
		case TargetPropPct: { // 目标某属性万分比值
			ret = getTargetProp(true, confEffect, source, process);
		}
			break;
		case TriggerSelfPropPct: { // 触发者某属性万分比值
			ret = getTriggerSelfProp(true, confEffect, source, process);
		}
			break;
		case TriggerTargetPropPct: { // 触发目标某属性万分比值
			ret = getTriggerTargetProp(true, confEffect, source, process);
		}
			break;

		case SelfCamp: { // 自身阵营
			ret = getSelfCamp(confEffect, source, process);
		}
			break;
		case TargetCamp: { // 目标阵营
			ret = getTargetCamp(confEffect, source, process);
		}
			break;
		case TriggerSelfCamp: { // 获取触发者阵营
			ret = getTriggerSelfCamp(confEffect, source, process);
		}
			break;
		case TriggerTargetCamp: { // 获取触发目标阵营
			ret = getTriggerTargetCamp(confEffect, source, process);
		}
			break;

		case TargetIsFriend: { // 目标是友方
			ret = getTargetIsFriend(confEffect, source, process);
		}
			break;
		case TriggerTargetIsFriend: { // 触发目标是友方
			ret = getTriggerTargetIsFriend(confEffect, source, process);
		}
			break;
		case TriggerSelfIsFriend: { // 触发者是友方
			ret = getTriggerSelfIsFriend(confEffect, source, process);
		}
			break;

		case SelfHasBuffType: { // 自身含有某类型buff
			ret = getSelfHasBuffType(confEffect, source, process);
		}
			break;
		case TargetHasBuffType: { // 目标含有某类型buff
			ret = getTargetHasBuffType(confEffect, source, process);
		}
			break;
		case TriggerSelfHasBuffType: { // 触发者含有某类型buff
			ret = getTriggerSelfHasBuffType(confEffect, source, process);
		}
			break;
		case TriggerTargetHasBuffType: { // 触发目标含有某类型buff
			ret = getTriggerTargetHasBuffType(confEffect, source, process);
		}
			break;

		default: {
			Log.fight.error("未处理的条件类型：conditionType={},sn={}", type.value(), confEffect.sn);
		}
		}
		return ret;
	}

	private int getRound(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		ret = stageObj.round;
		return ret;
	}

	private int getProbability(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		// int maxValue = Utils.I100;// 默认百分比几率
		// if(confEffect.conditionParam2 != null) {
		// int value = Utils.intValue(confEffect.conditionParam2);
		// if(value < Utils.I100) {// 最小支持百分比
		// maxValue = Utils.I100;
		// Log.fight.error("错误参数:conditionType={},conditionParam2={},sn={}",
		// confEffect.conditionType, confEffect.conditionParam2, confEffect.sn);
		// } else if (value > Utils.I10000) {// 最大支持万分比
		// maxValue = Utils.I10000;
		// Log.fight.error("错误参数:conditionType={},conditionParam2={},sn={}",
		// confEffect.conditionType, confEffect.conditionParam2, confEffect.sn);
		// } else {// 范围[100,10000]
		// maxValue = value;
		// }
		// }
		ret = stageObj.randUtils.nextInt(Utils.I10000);
		return ret;
	}

	private int getCountFriend(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		for (FightObject targetObj : stageObj.getFightObjs().values()) {
			if (fightObj.team == targetObj.team) {
				ret++;
			}
		}
		return ret;
	}

	private int getCountEnemy(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		for (FightObject targetObj : stageObj.getFightObjs().values()) {
			if (fightObj.team != targetObj.team) {
				ret++;
			}
		}
		return ret;
	}

	private int getSelfProp(boolean isPct, ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		ValueBase propValue = PropManager.inst().getFinalPropValue(confEffect.conditionParam2, fightObj);
		if (isPct) {
			ret = Utils.intValue(propValue.getNumberValue() / Utils.D10000);
		} else {
			ret = Utils.intValue(propValue.getNumberValue());
		}
		return ret;
	}

	private int getTargetProp(boolean isPct, ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		ValueBase propValue = PropManager.inst().getFinalPropValue(confEffect.conditionParam2, targetObj);
		if (isPct) {
			ret = Utils.intValue(propValue.getNumberValue() / Utils.D10000);
		} else {
			ret = Utils.intValue(propValue.getNumberValue());
		}
		return ret;
	}

	private int getTriggerSelfProp(boolean isPct, ConfSkillEffect confEffect, SourceParam source,
			ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject fightObj = trigger.triggerParam.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		ValueBase propValue = PropManager.inst().getFinalPropValue(confEffect.conditionParam2, fightObj);
		if (isPct) {
			ret = Utils.intValue(propValue.getNumberValue() / Utils.D10000);
		} else {
			ret = Utils.intValue(propValue.getNumberValue());
		}
		return ret;
	}

	private int getTriggerTargetProp(boolean isPct, ConfSkillEffect confEffect, SourceParam source,
			ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject targetObj = trigger.triggerParam.targetObj;
		if (targetObj == null) {
			Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		ValueBase propValue = PropManager.inst().getFinalPropValue(confEffect.conditionParam2, targetObj);
		if (isPct) {
			ret = Utils.intValue(propValue.getNumberValue() / Utils.D10000);
		} else {
			ret = Utils.intValue(propValue.getNumberValue());
		}
		return ret;
	}

	private int getSelfCamp(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		ret = fightObj.camp;
		return ret;
	}

	private int getTargetCamp(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			// 没有目标，则检查目标列表有没有
			List<FightObject> targetList = process.targetObjList;
			if (targetList != null && !targetList.isEmpty()) {
				targetObj = targetList.get(0);
			}
			if (targetObj == null) {
				Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
				return ret;
			}
		}
		ret = targetObj.camp;
		return ret;
	}

	private int getTriggerSelfCamp(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject fightObj = trigger.triggerParam.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		ret = fightObj.camp;
		return ret;
	}

	private int getTriggerTargetCamp(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject targetObj = trigger.triggerParam.targetObj;
		if (targetObj == null) {
			Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		ret = targetObj.camp;
		return ret;
	}

	private int getTargetIsFriend(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		if (fightObj.team == targetObj.team) {
			ret = 1;
		}
		return ret;
	}

	private int getTriggerTargetIsFriend(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject targetObj = trigger.triggerParam.targetObj;
		if (targetObj == null) {
			Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		if (fightObj.team == targetObj.team) {
			ret = 1;
		}
		return ret;
	}

	private int getTriggerSelfIsFriend(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		if (fightObj.team == targetObj.team) {
			ret = 1;
		}
		return ret;
	}

	private int getSelfHasBuffType(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		if (confEffect.conditionParam2 == null) {
			Log.fight.error("conditionParam2 is null！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		List<Integer> buffTypeList = Utils.strToIntList(confEffect.conditionParam2);
		List<Buff> buffList = fightObj.buffManager.getBuffByType(buffTypeList);
		if (!buffList.isEmpty()) {
			ret = 1;
		}
		return ret;
	}

	private int getTargetHasBuffType(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		if (confEffect.conditionParam2 == null) {
			Log.fight.error("conditionParam2 is null！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		List<Integer> buffTypeList = Utils.strToIntList(confEffect.conditionParam2);
		List<Buff> buffList = targetObj.buffManager.getBuffByType(buffTypeList);
		if (!buffList.isEmpty()) {
			ret = 1;
		}
		return ret;
	}

	private int getTriggerSelfHasBuffType(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject fightObj = trigger.triggerParam.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		if (confEffect.conditionParam2 == null) {
			Log.fight.error("conditionParam2 is null！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		List<Integer> buffTypeList = Utils.strToIntList(confEffect.conditionParam2);
		List<Buff> buffList = fightObj.buffManager.getBuffByType(buffTypeList);
		if (!buffList.isEmpty()) {
			ret = 1;
		}
		return ret;
	}

	private int getTriggerTargetHasBuffType(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		int ret = 0;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		FightObject targetObj = trigger.triggerParam.targetObj;
		if (targetObj == null) {
			Log.fight.error("no find targetObj！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		if (confEffect.conditionParam2 == null) {
			Log.fight.error("conditionParam2 is null！conditionType={},sn={}", confEffect.conditionType, confEffect.sn);
			return ret;
		}
		List<Integer> buffTypeList = Utils.strToIntList(confEffect.conditionParam2);
		List<Buff> buffList = targetObj.buffManager.getBuffByType(buffTypeList);
		if (!buffList.isEmpty()) {
			ret = 1;
		}
		return ret;
	}
}
