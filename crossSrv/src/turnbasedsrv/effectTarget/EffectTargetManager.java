package turnbasedsrv.effectTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.support.ManagerBase;
import core.support.RandomMT19937;
import core.support.Utils;
import crosssrv.support.Log;
import game.msg.Define.EStanceType;
import game.worldsrv.config.ConfSkillEffect;
import turnbasedsrv.buff.BuffTriggerData;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.param.ProcessParam;
import turnbasedsrv.param.SourceParam;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.skill.SkillCastData;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.trigger.Trigger;
import turnbasedsrv.value.ValueBase;

public class EffectTargetManager extends ManagerBase {

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static EffectTargetManager inst() {
		return inst(EffectTargetManager.class);
	}

	/**
	 * 获取目标对象，字段说明： targetType为目标类型
	 * targetParam1为数量或指定类型(All全体，One单人，Col列，Row排，FrontRow前排，BackRow后排)
	 * targetParam2为额外参数
	 */
	public void getEffectTarget(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetType type = EffectTargetType.get(confEffect.targetType);
		switch (type) {
		case FriendRand: { // 取随机数量的友方
			ret = getFriendRand(confEffect, source, process);
		}
			break;
		case FriendRandOther: { // 取随机数量的友方，优先他人，数量不足时再补自己
			ret = getFriendRandOther(confEffect, source, process);
		}
			break;
		case EnemyRand: { // 取随机数量的敌方
			ret = getEnemyRand(confEffect, source, process);
		}
			break;

		case EnemyPropDESC: { // 取某属性从高到低的敌方数量
			ret = getEnemyPropDESC(confEffect, source, process);
		}
			break;
		case EnemyPropASC: { // 取某属性从低到高的敌方数量
			ret = getEnemyPropASC(confEffect, source, process);
		}
			break;
		case FriendPropDESC: { // 取某属性从高到低的友方数量
			ret = getFriendPropDESC(confEffect, source, process);
		}
			break;
		case FriendPropASC: { // 取某属性从低到高的友方数量
			ret = getFriendPropASC(confEffect, source, process);
		}
			break;

		case EnemyPropHighest: { // 取某属性最高的敌方(单人，所在列，所在排)
			ret = getEnemyPropHighest(confEffect, source, process);
		}
			break;
		case EnemyPropLowest: { // 取某属性最低的敌方(单人，所在列，所在排)
			ret = getEnemyPropLowest(confEffect, source, process);
		}
			break;
		case FriendPropHighest: { // 取某属性最高的友方(单人，所在列，所在排)
			ret = getFriendPropHighest(confEffect, source, process);
		}
			break;
		case FriendPropLowest: { // 取某属性最低的友方(单人，所在列，所在排)
			ret = getFriendPropLowest(confEffect, source, process);
		}
			break;

		case Caster: { // 取施法者(单人，所在列，所在排)
			ret = getCaster(confEffect, source, process);
		}
			break;
		case BuffOwner: { // 取buff拥有者(单人，所在列，所在排)
			ret = getBuffOwner(confEffect, source, process);
		}
			break;

		case Enemy: { // 取敌方(全体，单人，所在列，所在排，前排，后排)
			ret = getEnemy(confEffect, source, process);
		}
			break;
		case Friend: { // 取友方(全体，单人，所在列，所在排，前排，后排)
			ret = getFriend(confEffect, source, process);
		}
			break;

		case TriggerSelf: { // 触发点触发者(单人，所在列，所在排)
			ret = getTriggerSelf(confEffect, source, process);
		}
			break;
		case TriggerTarget: { // 触发点触发目标(单人，所在列，所在排)
			ret = getTriggerTarget(confEffect, source, process);
		}
			break;

		default: {
			Log.fight.error("未处理的目标类型：targetType={},sn={}", confEffect.conditionType, confEffect.sn);
		}
		}

		// 把符合条件的目标对象塞入process，以便后续处理获取
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			targetList = new ArrayList<>();
			process.targetObjList = targetList;
		}
		for (FightObject obj : ret) {
			if (!targetList.contains(obj)) {
				targetList.add(obj);
			}
		}
	}

	private List<FightObject> getFriendRand(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		int count = Utils.intValue(confEffect.targetParam1);// 数量
		if (count < 1) {
			Log.fight.error("数量不可<1：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj!");
			return ret;
		}
		// 取出所有友方后再过滤
		List<FightObject> list = getFriendObjList(source, process);
		// 取随机数量的对象
		ret = getRandObj(stageObj.randUtils, count, list);
		return ret;
	}

	private List<FightObject> getFriendRandOther(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		int count = Utils.intValue(confEffect.targetParam1);// 数量
		if (count < 1) {
			Log.fight.error("数量不可<1：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj!");
			return ret;
		}
		// 取出所有友方后再过滤
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj!");
			return ret;
		}
		List<FightObject> list = new ArrayList<>();
		// 他人优先
		for (FightObject obj : stageObj.getFightObjs().values()) {
			if (obj != fightObj && obj.team == fightObj.team) {
				list.add(obj);
			}
		}
		// 数量不足加上自己
		if (list.size() < count) {
			list.add(fightObj);
		}
		if (list.size() == count) {
			ret.addAll(list);
			return ret;
		}
		// 取随机数量的对象
		ret = getRandObj(stageObj.randUtils, count, list);
		return ret;
	}

	private List<FightObject> getEnemyRand(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		int count = Utils.intValue(confEffect.targetParam1);// 数量
		if (count < 1) {
			Log.fight.error("数量不可<1：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj!");
			return ret;
		}
		// 取出所有敌方后再过滤
		List<FightObject> list = getEnemyObjList(source, process);
		// 取随机数量的对象
		ret = getRandObj(stageObj.randUtils, count, list);
		return ret;
	}

	private List<FightObject> getEnemyPropDESC(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		int count = Utils.intValue(confEffect.targetParam1);// 数量
		if (count < 1) {
			Log.fight.error("数量不可<1：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		// 取出所有敌方后再过滤
		List<FightObject> list = getEnemyObjList(source, process);
		// 按指定属性从高到低排序
		descPropValue(list, confEffect.targetParam2);
		// 取指定数量的目标
		for (int i = 0; i < count && i < list.size(); i++) {
			ret.add(list.get(i));
		}
		return ret;
	}

	private List<FightObject> getEnemyPropASC(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		int count = Utils.intValue(confEffect.targetParam1);// 数量
		if (count < 1) {
			Log.fight.error("数量不可<1：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		// 取出所有敌方后再过滤
		List<FightObject> list = getEnemyObjList(source, process);
		// 按指定属性从低到高排序
		ascPropValue(list, confEffect.targetParam2);
		// 取指定数量的目标
		for (int i = 0; i < count && i < list.size(); i++) {
			ret.add(list.get(i));
		}
		return ret;
	}

	private List<FightObject> getFriendPropDESC(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		int count = Utils.intValue(confEffect.targetParam1);// 数量
		if (count < 1) {
			Log.fight.error("数量不可<1：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		// 取出所有友方后再过滤
		List<FightObject> list = getFriendObjList(source, process);
		// 按指定属性从高到低排序
		descPropValue(list, confEffect.targetParam2);
		// 取指定数量的目标
		for (int i = 0; i < count && i < list.size(); i++) {
			ret.add(list.get(i));
		}
		return ret;
	}

	private List<FightObject> getFriendPropASC(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		int count = Utils.intValue(confEffect.targetParam1);// 数量
		if (count < 1) {
			Log.fight.error("数量不可<1：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		// 取出所有友方后再过滤
		List<FightObject> list = getFriendObjList(source, process);
		// 按指定属性从低到高排序
		ascPropValue(list, confEffect.targetParam2);
		// 取指定数量的目标
		for (int i = 0; i < count && i < list.size(); i++) {
			ret.add(list.get(i));
		}
		return ret;
	}

	private List<FightObject> getEnemyPropHighest(ConfSkillEffect confEffect, SourceParam source,
			ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (null == fightObj) {
			Log.fight.error("no find fightObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		// 取出所有敌方后再过滤
		List<FightObject> list = getEnemyObjList(source, process);
		// 按指定属性从高到低排序
		descPropValue(list, confEffect.targetParam2);
		// 取符合指定规则的目标
		ret = getByRule(rule, list, fightObj);
		return ret;
	}

	private List<FightObject> getEnemyPropLowest(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (null == fightObj) {
			Log.fight.error("no find fightObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		// 取出所有敌方后再过滤
		List<FightObject> list = getEnemyObjList(source, process);
		// 按指定属性从低到高排序
		ascPropValue(list, confEffect.targetParam2);
		// 取符合指定规则的目标
		ret = getByRule(rule, list, fightObj);
		return ret;
	}

	private List<FightObject> getFriendPropHighest(ConfSkillEffect confEffect, SourceParam source,
			ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (null == fightObj) {
			Log.fight.error("no find fightObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		// 取出所有友方后再过滤
		List<FightObject> list = getFriendObjList(source, process);
		// 按指定属性从高到低排序
		descPropValue(list, confEffect.targetParam2);
		// 取符合指定规则的目标
		ret = getByRule(rule, list, fightObj);
		return ret;
	}

	private List<FightObject> getFriendPropLowest(ConfSkillEffect confEffect, SourceParam source,
			ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (null == fightObj) {
			Log.fight.error("no find fightObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		// 取出所有友方后再过滤
		List<FightObject> list = getFriendObjList(source, process);
		// 按指定属性从低到高排序
		ascPropValue(list, confEffect.targetParam2);
		// 取符合指定规则的目标
		ret = getByRule(rule, list, fightObj);
		return ret;
	}

	private List<FightObject> getCaster(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject casterObj = getCasterObj(source, process);
		if (null == casterObj) {
			Log.fight.error("no find casterObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		if (rule == EffectTargetRule.One) {// 单人
			ret.add(casterObj);
		} else {// 非单人
			FightStageObject stageObj = source.stageObj;
			List<FightObject> list = getFriendObjList(stageObj, casterObj);
			// 取符合指定规则的目标
			ret = getByRule(rule, list, casterObj);
		}
		return ret;
	}

	private List<FightObject> getBuffOwner(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject buffOwnerObj = getBuffOwnerObj(source, process);
		if (null == buffOwnerObj) {
			Log.fight.error("no find buffOwnerObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		if (rule == EffectTargetRule.One) {// 单人
			ret.add(buffOwnerObj);
		} else {// 非单人
			FightStageObject stageObj = source.stageObj;
			List<FightObject> list = getFriendObjList(stageObj, buffOwnerObj);
			// 取符合指定规则的目标
			ret = getByRule(rule, list, buffOwnerObj);
		}
		return ret;
	}

	private List<FightObject> getTriggerSelf(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject triggerSelfObj = getTriggerSelfObj(source, process);
		if (null == triggerSelfObj) {
			Log.fight.error("no find triggerSelfObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		if (rule == EffectTargetRule.One) {// 单人
			ret.add(triggerSelfObj);
		} else {// 非单人
			FightStageObject stageObj = source.stageObj;
			List<FightObject> list = getFriendObjList(stageObj, triggerSelfObj);
			// 取符合指定规则的目标
			ret = getByRule(rule, list, triggerSelfObj);
		}
		return ret;
	}

	private List<FightObject> getTriggerTarget(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject triggerTargetObj = getTriggerTargetObj(source, process);
		if (null == triggerTargetObj) {
			Log.fight.error("no find triggerTargetObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		if (rule == EffectTargetRule.One) {// 单人
			ret.add(triggerTargetObj);
		} else {// 非单人
			FightStageObject stageObj = source.stageObj;
			List<FightObject> list = getFriendObjList(stageObj, triggerTargetObj);
			// 取符合指定规则的目标
			ret = getByRule(rule, list, triggerTargetObj);
		}
		return ret;
	}

	private List<FightObject> getEnemy(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (null == fightObj) {
			Log.fight.error("no find fightObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		// 取出所有敌方后再过滤
		List<FightObject> list = getEnemyObjList(source, process);
		// 取符合指定规则的目标
		ret = getByRule(rule, list, fightObj);
		return ret;
	}

	private List<FightObject> getFriend(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		EffectTargetRule rule = EffectTargetRule.get(confEffect.targetParam1);
		if (null == rule) {
			Log.fight.error("取目标规则错误：targetParam1={},sn={}", confEffect.targetParam1, confEffect.sn);
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (null == fightObj) {
			Log.fight.error("no find fightObj!targetType={},sn={}", confEffect.targetType, confEffect.sn);
			return ret;
		}
		// 取出所有友方后再过滤
		List<FightObject> list = getFriendObjList(source, process);
		// 取符合指定规则的目标
		ret = getByRule(rule, list, fightObj);
		return ret;
	}

	/**
	 * 获取指定对象的敌方列表
	 */
	private List<FightObject> getEnemyObjList(SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj!");
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj!");
			return ret;
		}
		ret = getEnemyObjList(stageObj, fightObj);
		return ret;
	}

	private List<FightObject> getEnemyObjList(FightStageObject stageObj, FightObject fightObj) {
		List<FightObject> ret = new ArrayList<>();
		int count = stageObj.getFightObjCount();
		if (count <= 0) {
			return ret;
		}
		for (FightObject obj : stageObj.getFightObjs().values()) {
			if (obj != fightObj && obj.team != fightObj.team) {
				ret.add(obj);
				count--;
				if (count <= 0) {
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * 获取指定对象的友方列表
	 */
	private List<FightObject> getFriendObjList(SourceParam source, ProcessParam process) {
		List<FightObject> ret = new ArrayList<>();
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj!");
			return ret;
		}
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("no find fightObj!");
			return ret;
		}
		ret = getFriendObjList(stageObj, fightObj);
		return ret;
	}

	private List<FightObject> getFriendObjList(FightStageObject stageObj, FightObject fightObj) {
		List<FightObject> ret = new ArrayList<>();
		if (null == stageObj || null == fightObj) {
			return ret;
		}
		int count = stageObj.getFightObjCount();
		if (count <= 0) {
			return ret;
		}
		// 自己肯定是友方，优先加入
		ret.add(fightObj);
		count--;
		if (count <= 0) {
			return ret;
		}
		for (FightObject obj : stageObj.getFightObjs().values()) {
			if (obj != fightObj && obj.team == fightObj.team) {
				ret.add(obj);
				count--;
				if (count <= 0) {
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * 取施法者对象
	 */
	private FightObject getCasterObj(SourceParam source, ProcessParam process) {
		FightObject ret = null;
		SkillCastData skillData = source.skillData;
		if (skillData == null) {
			Log.fight.error("no find skillData!");
			return ret;
		}
		ret = skillData.creator;
		if (ret == null) {
			Log.fight.error("no find skillData.creator!");
		}
		return ret;
	}

	/**
	 * 取buff拥有者对象
	 */
	private FightObject getBuffOwnerObj(SourceParam source, ProcessParam process) {
		FightObject ret = null;
		BuffTriggerData buffData = source.buffData;
		if (buffData == null) {
			Log.fight.error("no find buffData!");
			return ret;
		}
		ret = buffData.buff.owner;
		if (ret == null) {
			Log.fight.error("no find buffData.buff.owner!");
		}
		return ret;
	}

	/**
	 * 取触发点触发者
	 */
	private FightObject getTriggerSelfObj(SourceParam source, ProcessParam process) {
		FightObject ret = null;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj!");
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger!");
			return ret;
		}
		ret = trigger.triggerParam.fightObj;
		if (ret == null) {
			Log.fight.error("no find triggerFightObj!");
		}
		return ret;
	}

	/**
	 * 取触发点触发目标
	 */
	private FightObject getTriggerTargetObj(SourceParam source, ProcessParam process) {
		FightObject ret = null;
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("no find stageObj!");
			return ret;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("no find trigger!");
			return ret;
		}
		ret = trigger.triggerParam.targetObj;
		if (ret == null) {
			Log.fight.error("no find triggerTargetObj!");
		}
		return ret;
	}

	/**
	 * 升序排序，即从低到高
	 */
	private void ascPropValue(List<FightObject> list, String propName) {
		Collections.sort(list, new Comparator<FightObject>() {
			@Override
			public int compare(FightObject u1, FightObject u2) {
				int ret = 0;
				if (u1 != null && u2 != null) {
					ValueBase v1 = PropManager.inst().getFinalPropValue(propName, u1);
					ValueBase v2 = PropManager.inst().getFinalPropValue(propName, u2);
					if (v1 != null && v2 != null && v1.isNumberValue() && v2.isNumberValue()) {
						if (v1.getNumberValue() < v2.getNumberValue()) {
							ret = -1;
						} else if (v1.getNumberValue() > v2.getNumberValue()) {
							ret = 1;
						}
					}
				}
				return ret;
			}
		});
	}

	/**
	 * 降序排序，即从高到低
	 */
	private void descPropValue(List<FightObject> list, String propName) {
		Collections.sort(list, new Comparator<FightObject>() {
			@Override
			public int compare(FightObject u1, FightObject u2) {
				int ret = 0;
				if (u1 != null && u2 != null) {
					ValueBase v1 = PropManager.inst().getFinalPropValue(propName, u1);
					ValueBase v2 = PropManager.inst().getFinalPropValue(propName, u2);
					if (v1 != null && v2 != null && v1.isNumberValue() && v2.isNumberValue()) {
						if (v1.getNumberValue() > v2.getNumberValue()) {
							ret = -1;
						} else if (v1.getNumberValue() < v2.getNumberValue()) {
							ret = 1;
						}
					}
				}
				return ret;
			}
		});
	}

	/**
	 * 取随机数量的对象
	 */
	public List<FightObject> getRandObj(RandomMT19937 randUtils, int count, List<FightObject> list) {
		List<FightObject> ret = new ArrayList<>();
		if (count >= list.size()) {
			ret.addAll(list);
		} else if (count > 0) {// count在范围内随机：[0,list.size()-1]
			List<Integer> randList = getRandListInRange(randUtils, count, list.size() - 1);
			for (int i : randList) {
				ret.add(list.get(i));
			}
		}
		return ret;
	}

	/**
	 * 取不重复的随机数列表（范围[max,min]）
	 */
	public List<Integer> getRandListInRange(RandomMT19937 randUtils, int count, int max) {
		return getRandListInRange(randUtils, count, max, 0);
	}

	public List<Integer> getRandListInRange(RandomMT19937 randUtils, int count, int max, int min) {
		List<Integer> randList = new ArrayList<>();
		// 非正常范围取值
		if (min > max) {
			return randList;
		} else if (min == max) {
			randList.add(min);
			return randList;
		}
		// 正常范围取值：[max,min]
		int loopMax = 100;
		for (int i = 0; i < count && loopMax > 0;) {
			loopMax--;
			int rand = min + randUtils.nextInt(1 + max - min);
			if (!randList.contains(rand)) {
				// 取到一个不重复的
				i++;
				randList.add(rand);
			}
		}
		// System.out.println("循环次数："+(100-loopMax));
		return randList;
	}

	/**
	 * 取出符合规则的对象列表
	 * 
	 * @param rule
	 *            过滤规则
	 * @param list
	 *            可筛选的对象列表
	 * @param fightObj
	 *            施法者
	 */
	private List<FightObject> getByRule(EffectTargetRule rule, List<FightObject> list, FightObject fightObj) {
		List<FightObject> ret = new ArrayList<>();
		if (list == null || list.isEmpty()) {
			return ret;
		}

		int posStartFrontRow = 0;// 前排的起始位置
		int posEndFrontRow = 4;// 前排的结束位置
		int lenOneRow = posEndFrontRow - posStartFrontRow + 1;// 一排几个位置

		// 根据类型过滤
		switch (rule) {
		case All: { // 全体
			ret.addAll(list);
		}
			break;
		case One: { // 单人
			ret.add(list.get(0));
		}
			break;
		case RandomOne: { // 随机单人
			int rand = fightObj.stageObj.randUtils.nextInt(list.size());
			ret.add(list.get(rand));
		}
			break;
		case Others: { // 除自己外的其他人
			for (FightObject obj : list) {
				if (obj.idFight != fightObj.idFight) {
					ret.add(obj);
				}
			}
		}
			break;
		case Col: { // 所在列
			FightObject obj = list.get(0);
			ret.add(obj);
			int pos = obj.pos + lenOneRow;
			if (obj.pos >= lenOneRow) {
				pos = obj.pos - lenOneRow;
			}
			FightObject objFind = getFightObj(list, pos);
			if (objFind != null) {
				ret.add(objFind);
			}
		}
			break;
		case Row: { // 所在排
			FightObject obj = list.get(0);
			ret.add(obj);
			int posStart = posStartFrontRow;
			int posEnd = posEndFrontRow;
			if (obj.pos >= lenOneRow) {
				// 后排的起始和结束
				posStart = lenOneRow;
				posEnd = 2 * lenOneRow - 1;
			}
			List<FightObject> objFind = getFightObjList(list, posStart, posEnd);
			if (!objFind.isEmpty()) {
				ret.addAll(objFind);
			}
		}
			break;
		case FrontRow: { // 前排
			List<FightObject> objFind = getFightObjList(list, posStartFrontRow, posEndFrontRow);
			if (!objFind.isEmpty()) {
				ret.addAll(objFind);
			} else { // 前排没人则继续找后排
				objFind = getFightObjList(list, lenOneRow, 2 * lenOneRow - 1);
				if (!objFind.isEmpty()) {
					ret.addAll(objFind);
				}
			}
		}
			break;
		case BackRow: { // 后排
			List<FightObject> objFind = getFightObjList(list, lenOneRow, 2 * lenOneRow - 1);
			if (!objFind.isEmpty()) {
				ret.addAll(objFind);
			} else { // 后排没人则继续找前排
				objFind = getFightObjList(list, posStartFrontRow, posEndFrontRow);
				if (!objFind.isEmpty()) {
					ret.addAll(objFind);
				}
			}
		}
			break;
		case FrontRowOne: { // 前排单人
			if (null == fightObj) {
				// 未指定施法者，则取第一个
				ret.add(list.get(0));
			} else {
				// 有指定施法者，则取对面位置或最靠近的对面位置
				int[] posFind = findPosInFrontRow(fightObj.pos);// 搜索对面顺序
				for (int pos : posFind) {
					FightObject objFind = getFightObj(list, pos);
					if (objFind != null) {
						ret.add(objFind);
						break;// 只找一个
					}
				}
			}
		}
			break;
		case BackRowOne: { // 后排单人
			if (null == fightObj) {
				// 未指定施法者，则取第一个
				ret.add(list.get(0));
			} else {
				// 有指定施法者，则取对面位置或最靠近的对面位置
				int[] posFind = findPosInBackRow(fightObj.pos);// 搜索对面顺序
				for (int pos : posFind) {
					FightObject objFind = getFightObj(list, pos);
					if (objFind != null) {
						ret.add(objFind);
						break;// 只找一个
					}
				}
			}
		}
			break;
		case Col2: { // 随机2列
			int[] posFind = findPosFromCol(2, fightObj.stageObj.randUtils);
			List<FightObject> objFind = getFightObjList(list, posFind);
			if (!objFind.isEmpty()) {
				ret.addAll(objFind);
			}
		}
			break;
		case Triangle: { // 三角范围
			FightObject obj = list.get(0);
			List<FightObject> objFind = findPosInTriangle(list, fightObj.pos, obj.stance);
			if (!objFind.isEmpty()) {
				ret.addAll(objFind);
			}
		}
			break;
		default:
			Log.fight.error("未处理的规则类型：getByRule no find rule={}", rule.value());
		}
		return ret;
	}

	/**
	 * 选取指定位置范围的对象列表
	 * 
	 * @param list
	 * @param posStart
	 * @param posEnd
	 * @return
	 */
	private List<FightObject> getFightObjList(List<FightObject> list, int posStart, int posEnd) {
		List<FightObject> ret = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			FightObject other = list.get(i);
			if (other.pos >= posStart && other.pos <= posEnd) {
				ret.add(other);
			}
		}
		return ret;
	}

	/**
	 * 选取指定多个位置的对象列表
	 * 
	 * @param list
	 * @param posFind
	 * @return
	 */
	private List<FightObject> getFightObjList(List<FightObject> list, int[] posFind) {
		List<FightObject> ret = new ArrayList<>();
		for (int pos : posFind) {
			FightObject objFind = getFightObj(list, pos);
			if (objFind != null) {
				ret.add(objFind);
			}
		}
		return ret;
	}

	/**
	 * 选取指定单个位置的对象
	 * 
	 * @param list
	 * @param pos
	 * @return
	 */
	private FightObject getFightObj(List<FightObject> list, int pos) {
		for (int i = 0; i < list.size(); i++) {
			FightObject other = list.get(i);
			if (other.pos == pos) {
				return other;
			}
		}
		return null;
	}

	/**
	 * 从所有列中随机出几列的序号
	 * 
	 * @param countCol列数
	 * @return
	 */
	private int[] findPosFromCol(int countCol, RandomMT19937 randUtils) {
		// 5列里随机取几列
		if (countCol > 5 || countCol < 1) {
			return null;
		}
		int[] order = null;
		List<Integer> randList = getRandListInRange(randUtils, countCol, 5, 1);
		List<Integer> posFind = new ArrayList<>();
		for (int i : randList) {
			switch (i) {
			case 1: {// 取第1列位置：0,5
				posFind.add(0);
				posFind.add(5);
			}
				break;
			case 2: {// 取第2列位置：1,6
				posFind.add(1);
				posFind.add(6);
			}
				break;
			case 3: {// 取第3列位置：2,7
				posFind.add(2);
				posFind.add(7);
			}
				break;
			case 4: {// 取第4列位置：3,8
				posFind.add(3);
				posFind.add(8);
			}
				break;
			case 5: {// 取第5列位置：4,9
				posFind.add(4);
				posFind.add(9);
			}
				break;
			}
		}
		if (!posFind.isEmpty()) {
			order = new int[posFind.size()];
			for (int i = 0; i < posFind.size(); i++) {
				order[i] = posFind.get(i);
			}
		}
		return order;
	}

	/**
	 * 搜索位置（从对面的前排开始）
	 * 
	 * @param posAtk施法者位置
	 * @return
	 */
	private int[] findPosInFrontRow(int posAtk) {
		int[] order = null;
		if (posAtk == 0 || posAtk == 5) {
			// 0,5号位置，搜索对面顺序：0,1,2,3,4,5,6,7,8,9
			order = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		} else if (posAtk == 1 || posAtk == 6) {
			// 1,6号位置，搜索对面顺序：1,0,2,3,4,6,5,7,8,9
			order = new int[] { 1, 0, 2, 3, 4, 6, 5, 7, 8, 9 };
		} else if (posAtk == 2 || posAtk == 7) {
			// 2,7号位置，搜索对面顺序：2,1,3,0,4,7,6,8,5,9
			order = new int[] { 2, 1, 3, 0, 4, 7, 6, 8, 5, 9 };
		} else if (posAtk == 3 || posAtk == 8) {
			// 3,8号位置，搜索对面顺序：3,2,4,1,0,8,7,9,6,5
			order = new int[] { 3, 2, 4, 1, 0, 8, 7, 9, 6, 5 };
		} else if (posAtk == 4 || posAtk == 9) {
			// 4,9号位置，搜索对面顺序：4,3,2,1,0,9,8,7,6,5
			order = new int[] { 4, 3, 2, 1, 0, 9, 8, 7, 6, 5 };
		}
		return order;
	}

	/**
	 * 搜索位置（从对面的后排开始）
	 * 
	 * @param posAtk施法者位置
	 * @return
	 */
	private int[] findPosInBackRow(int posAtk) {
		int[] order = null;
		if (posAtk == 0 || posAtk == 5) {
			// 0,5号位置，搜索对面顺序：5,6,7,8,9,0,1,2,3,4
			order = new int[] { 5, 6, 7, 8, 9, 0, 1, 2, 3, 4 };
		} else if (posAtk == 1 || posAtk == 6) {
			// 1,6号位置，搜索对面顺序：6,5,7,8,9,1,0,2,3,4
			order = new int[] { 6, 5, 7, 8, 9, 1, 0, 2, 3, 4 };
		} else if (posAtk == 2 || posAtk == 7) {
			// 2,7号位置，搜索对面顺序：7,6,8,5,9,2,1,3,0,4
			order = new int[] { 7, 6, 8, 5, 9, 2, 1, 3, 0, 4 };
		} else if (posAtk == 3 || posAtk == 8) {
			// 3,8号位置，搜索对面顺序：8,7,9,6,5,3,2,4,1,0
			order = new int[] { 8, 7, 9, 6, 5, 3, 2, 4, 1, 0 };
		} else if (posAtk == 4 || posAtk == 9) {
			// 4,9号位置，搜索对面顺序：9,8,7,6,5,4,3,2,1,0
			order = new int[] { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 };
		}
		return order;
	}

	/**
	 * 搜索三角范围的位置（根据对面的站位W或M决定）
	 * 
	 * @param posAtk施法者位置
	 * @param stance对面的站位
	 * @return
	 */
	private List<FightObject> findPosInTriangle(List<FightObject> list, int posAtk, EStanceType stance) {
		List<FightObject> ret = new ArrayList<>();
		if (posAtk == 0 || posAtk == 5 || posAtk == 1 || posAtk == 6) {
			// 0,5,1,6号位置
			if (stance == EStanceType.StanceW) {
				// 对面站位W型则搜索对面顺序：0,6,2＞6,2,8＞2,8,4
				List<FightObject> objFind1 = getFightObjList(list, new int[] { 0, 6, 2 });
				List<FightObject> objFind2 = getFightObjList(list, new int[] { 6, 2, 8 });
				List<FightObject> objFind3 = getFightObjList(list, new int[] { 2, 8, 4 });
				List<FightObject> objFind = getTriangleObjs(objFind1, objFind2, objFind3);
				ret.addAll(objFind);
			}
			if (stance == EStanceType.StanceM) {
				// 对面站位M型则搜索对面顺序：5,1,7＞1,7,3＞7,3,9
				List<FightObject> objFind1 = getFightObjList(list, new int[] { 5, 1, 7 });
				List<FightObject> objFind2 = getFightObjList(list, new int[] { 1, 7, 3 });
				List<FightObject> objFind3 = getFightObjList(list, new int[] { 7, 3, 9 });
				List<FightObject> objFind = getTriangleObjs(objFind1, objFind2, objFind3);
				ret.addAll(objFind);
			}
		} else if (posAtk == 2 || posAtk == 7) {
			// 2,7号位置
			if (stance == EStanceType.StanceW) {
				// 对面站位W型则搜索对面顺序：6,2,8＞0,6,2＞2,8,4
				List<FightObject> objFind1 = getFightObjList(list, new int[] { 6, 2, 8 });
				List<FightObject> objFind2 = getFightObjList(list, new int[] { 0, 6, 2 });
				List<FightObject> objFind3 = getFightObjList(list, new int[] { 2, 8, 4 });
				List<FightObject> objFind = getTriangleObjs(objFind1, objFind2, objFind3);
				ret.addAll(objFind);
			}
			if (stance == EStanceType.StanceM) {
				// 对面站位M型则搜索对面顺序：1,7,3＞5,1,7＞7,3,9
				List<FightObject> objFind1 = getFightObjList(list, new int[] { 1, 7, 3 });
				List<FightObject> objFind2 = getFightObjList(list, new int[] { 5, 1, 7 });
				List<FightObject> objFind3 = getFightObjList(list, new int[] { 7, 3, 9 });
				List<FightObject> objFind = getTriangleObjs(objFind1, objFind2, objFind3);
				ret.addAll(objFind);
			}
		} else if (posAtk == 3 || posAtk == 8 || posAtk == 4 || posAtk == 9) {
			// 3,8,4,9号位置
			if (stance == EStanceType.StanceW) {
				// 对面站位W型则搜索对面顺序：2,8,4＞6,2,8＞0,6,2
				List<FightObject> objFind1 = getFightObjList(list, new int[] { 2, 8, 4 });
				List<FightObject> objFind2 = getFightObjList(list, new int[] { 6, 2, 8 });
				List<FightObject> objFind3 = getFightObjList(list, new int[] { 0, 6, 2 });
				List<FightObject> objFind = getTriangleObjs(objFind1, objFind2, objFind3);
				ret.addAll(objFind);
			}
			if (stance == EStanceType.StanceM) {
				// 对面站位M型则搜索对面顺序：7,3,9＞1,7,3＞5,1,7
				List<FightObject> objFind1 = getFightObjList(list, new int[] { 7, 3, 9 });
				List<FightObject> objFind2 = getFightObjList(list, new int[] { 1, 7, 3 });
				List<FightObject> objFind3 = getFightObjList(list, new int[] { 5, 1, 7 });
				List<FightObject> objFind = getTriangleObjs(objFind1, objFind2, objFind3);
				ret.addAll(objFind);
			}
		}
		return ret;
	}
	/**
	 * 获取三角范围内最合适的对象列表
	 * @param s1 第一个三角范围的人
	 * @param s2 第二个三角范围的人
	 * @param s3 第三个三角范围的人
	 * @return
	 */
	private List<FightObject> getTriangleObjs(List<FightObject> objFind1, List<FightObject> objFind2, List<FightObject> objFind3) {
		// 优先3人，再优先2人，再优先1人
		List<FightObject> ret = new ArrayList<>();
		int s1 = objFind1.size();
		int s2 = objFind2.size();
		int s3 = objFind3.size();
		// 优先3人
		int index = getTriangleIndex(3, s1, s2, s3);
		if (index < 0) {
			// 再优先2人
			index = getTriangleIndex(2, s1, s2, s3);
			if (index < 0) {
				// 再优先1人
				index = getTriangleIndex(1, s1, s2, s3);
				if (index < 0) {
					index = 1;
				}
			}
		}
		// 根据索引取最合适对象
		if (1 == index) {
			ret.addAll(objFind1);
		} else if (2 == index) {
			ret.addAll(objFind2);
		} else if (3 == index) {
			ret.addAll(objFind3);
		}
		return ret;
	}
	/**
	 * 获取三角范围内符合指定人数的索引
	 */
	private int getTriangleIndex(int sMax, int s1, int s2, int s3) {
		int index = -1;
		if (s1 == sMax) {
			index = 1;
		} else {
			if (s2 == sMax) {
				index = 2;
			} else if (s3 == sMax) {
				index = 3;
			}
		}
		return index;
	}
}
