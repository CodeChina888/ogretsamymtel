package game.turnbasedsrv.skill;

import java.util.ArrayList;
import java.util.List;

import game.turnbasedsrv.buff.BuffTriggerData;
import game.turnbasedsrv.effectAction.EffectActionManager;
import game.turnbasedsrv.effectAction.EffectActionType;
import game.turnbasedsrv.effectCondition.EffectCondManager;
import game.turnbasedsrv.effectCondition.EffectCondType;
import game.turnbasedsrv.effectTarget.EffectTargetManager;
import game.turnbasedsrv.effectTarget.EffectTargetType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.param.ProcessParam;
import game.turnbasedsrv.param.SourceParam;
import game.turnbasedsrv.prop.Prop;
import game.turnbasedsrv.prop.PropManager;
import game.turnbasedsrv.trigger.Trigger;
import game.turnbasedsrv.value.ValueBase;
import game.turnbasedsrv.value.ValueFactory;
import game.worldsrv.config.ConfSkillEffect;
import game.worldsrv.support.Log;

public class SkillEffect {
	/**
	 * 执行主动技能效果
	 */
	public static boolean doSkillEffect(SkillCastData data, List<Integer> effects) {
		if (effects == null || effects.size() < 1) {
			return false;
		}

		List<FightObject> targetList = new ArrayList<>();
		targetList.add(data.creator);

		return doEffect(new SourceParam(data.creator, data.creator.combatObj, data), new ProcessParam(targetList), effects);
	}

	/**
	 * 执行被动技能效果
	 */
	public static boolean doSkillPassiveEffect(Trigger trigger, SkillCastData data, List<Integer> effects) {
		if (effects == null || effects.size() < 1) {
			return false;
		}

		List<FightObject> targetList = new ArrayList<>();
		FightObject target = trigger.triggerParam.targetObj;
		if (target != null) {
			targetList.add(target);
		} else {
			targetList.add(data.creator);
		}

		return doEffect(new SourceParam(data.creator, data.creator.combatObj, data),
				new ProcessParam(target, targetList), effects);
	}

	/**
	 * 执行主动buff效果
	 */
	public static void doBuffEffect(BuffTriggerData data, List<Integer> effects) {
		if (effects == null || effects.size() < 1) {
			return;
		}

		List<FightObject> targetList = new ArrayList<>();
		targetList.add(data.buff.owner);

		doEffect(new SourceParam(data.buff.owner, data.buff.owner.combatObj, data), new ProcessParam(targetList),
				effects);
	}

	/**
	 * 执行被动buff效果
	 */
	public static boolean doBuffPassiveEffect(Trigger trigger, BuffTriggerData data, List<Integer> effects) {
		if (effects == null || effects.size() < 1) {
			return false;
		}

		List<FightObject> targetList = new ArrayList<>();
		FightObject target = trigger.triggerParam.targetObj;
		if (target != null) {
			targetList.add(target);
		}

		return doEffect(new SourceParam(data.buff.owner, data.buff.owner.combatObj, data),
				new ProcessParam(target, targetList), effects);
	}

	/**
	 * 执行效果
	 * 
	 * @param source
	 *            始源数据（效果执行前准备的）
	 * @param process
	 *            过程数据（效果执行过程中加入的）
	 * @param effects
	 *            效果列表
	 */
	private static boolean doEffect(SourceParam source, ProcessParam process, List<Integer> effects) {
		boolean isTrigger = false;
		boolean ok = true;
		for (int sn : effects) {
			ConfSkillEffect confEffect = ConfSkillEffect.get(sn);
			if (confEffect == null) {
				Log.fight.error("FightSkillEffect执行出错，sn={}", sn);
				return isTrigger;
			}
			process.SkillEffectSN = sn;
			
			// 条件继承
			if (confEffect.conditionInherit) {
				if (!ok) {
					continue;
				}
			} else if (confEffect.conditionType != null && !confEffect.conditionType.isEmpty()) {
				// 检查条件是否满足
				ok = checkCondition(confEffect, source, process);
				if (!ok) {
					continue;
				}
			} else {
				ok = true;
			}

			isTrigger = true;
			// 设置属性
			if (confEffect.prop == null && confEffect.propValue != null
					&& confEffect.prop.length == confEffect.propValue.length) {
				Prop prop = getSkillProp(confEffect, source, process);
				process.skillProp = prop;
			} else {
				process.skillProp = null;
			}

			// 目标继承
			if (!confEffect.targetInherit) {
				List<FightObject> targetList = new ArrayList<>();
				process.targetObjList = targetList;
				process.targetObj = null;
			}

			// 获取目标对象
			if (confEffect.targetType != null) {
				getTarget(confEffect, source, process);
			}

			// 执行行为逻辑
			if (confEffect.actionType != null) {
				doAction(confEffect, source, process);
			}			
		}
		return isTrigger;
	}

	/**
	 * 获取技能属性效果
	 */
	private static Prop getSkillProp(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		Prop prop = new Prop();
		// 默认属性系数为一倍
		ValueBase times = ValueFactory.getFightValueByParam((int) 1);
		if (confEffect.propCoef != null) {
			// 设置为配表的属性系数
			ValueBase value = PropManager.inst().getPropCoefValue(confEffect.propCoef, source, process);
			times.setValue(value);
		}
		for (int i = 0; i < confEffect.prop.length; i++) {
			String propName = confEffect.prop[i];
			String propValue = confEffect.propValue[i];
			ValueBase value = prop.getOnePropValue(propName);
			if (value != null) {
				ValueBase newValue = value.getCopy();
				newValue.setValue(propValue);
				newValue.multiply(times);
				prop.setPropValue(propName, newValue);
			}
		}
		return prop;
	}

	/**
	 * 获取目标对象
	 */
	private static void getTarget(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		if (EffectTargetType.containsKey(confEffect.targetType)) {
			EffectTargetManager.inst().getEffectTarget(confEffect, source, process);
		}
	}

	/**
	 * 执行行为逻辑
	 */
	private static void doAction(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		if (EffectActionType.containsKey(confEffect.actionType)) {
			EffectActionManager.inst().doEffectAction(confEffect, source, process);
		}
	}

	/**
	 * 检查条件是否满足
	 */
	private static boolean checkCondition(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		boolean ret = false;
		if (EffectCondType.containsKey(confEffect.conditionType)) {
			ret = EffectCondManager.inst().checkEffectCond(confEffect, source, process);
		}
		return ret;
	}

	/**
	 * 执行被动技能属性效果
	 */
	public static void doPassiveSkillProp(SkillCastData data, List<Integer> effects, FightObject target, Prop prop) {
		if (effects == null || effects.size() < 1) {
			return;
		}

		List<FightObject> targetList = new ArrayList<>();
		targetList.add(target);

		doPropAction(new SourceParam(data.creator, data.creator.combatObj, data), new ProcessParam(target, targetList),
				effects, prop);
	}

	/**
	 * 执行buff属性效果
	 */
	public static void doBuffProp(BuffTriggerData data, List<Integer> effects, FightObject target, Prop prop) {
		if (effects == null || effects.size() < 1) {
			return;
		}

		List<FightObject> targetList = new ArrayList<>();
		targetList.add(target);

		doPropAction(new SourceParam(data.buff.owner, data.buff.owner.combatObj, data),
				new ProcessParam(target, targetList), effects, prop);
	}

	/**
	 * 执行对目标属性效果
	 * 
	 * @param source
	 * @param process
	 * @param effects
	 */
	private static void doPropAction(SourceParam source, ProcessParam process, List<Integer> effects, Prop prop) {
		for (int sn : effects) {
			ConfSkillEffect confEffect = ConfSkillEffect.get(sn);
			if (confEffect == null) {
				Log.fight.error("FightSkillEffect执行出错，sn={}", sn);
				return;
			}
			process.SkillEffectSN = sn;

			// 条件
			if (confEffect.conditionType != null && !confEffect.conditionType.isEmpty()) {
				// 检查条件是否满足
				boolean ok = checkCondition(confEffect, source, process);
				if (!ok) {
					continue;
				}
			}
			// 设置属性
			if (confEffect.prop != null && confEffect.propValue != null
					&& confEffect.prop.length == confEffect.propValue.length) {
				// 默认属性系数为一倍
				ValueBase times = ValueFactory.getFightValueByParam((int) 1);
				if (confEffect.propCoef != null && !confEffect.propCoef.isEmpty()) {
					ValueBase value = PropManager.inst().getPropCoefValue(confEffect.propCoef, source, process);
					times.setValue(value);
				}
				for (int i = 0; i < confEffect.prop.length; i++) {
					String propName = confEffect.prop[i];
					String propValue = confEffect.propValue[i];
					ValueBase value = prop.getOnePropValue(propName);
					if (value != null) {
						ValueBase newValue = value.getCopy();
						newValue.setValue(propValue);
						newValue.multiply(times);
						prop.addPropValue(propName, newValue);
					}
				}
			}
		}
	}

}
