package turnbasedsrv.effectAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.support.ManagerBase;
import core.support.Utils;
import crosssrv.support.Log;
import game.worldsrv.config.ConfSkillEffect;
import game.worldsrv.enumType.FightPropName;
import turnbasedsrv.buff.Buff;
import turnbasedsrv.buff.BuffTriggerData;
import turnbasedsrv.enumType.AtkType;
import turnbasedsrv.enumType.SkillType;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.param.ProcessParam;
import turnbasedsrv.param.SourceParam;
import turnbasedsrv.prop.Prop;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.skill.SkillCastData;
import turnbasedsrv.skill.SkillTargetData;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.trigger.Trigger;
import turnbasedsrv.value.ValueBase;

public class EffectActionManager extends ManagerBase {

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static EffectActionManager inst() {
		return inst(EffectActionManager.class);
	}

	/**
	 * 执行行为逻辑，字段说明： actionType为逻辑类型 actionParam1为参数1 actionParam2为参数2
	 */
	public void doEffectAction(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		EffectActionType type = EffectActionType.get(confEffect.actionType);
		switch (type) {
		case OperateDirect: { // 对目标进行直接血量运算（正值时为最大血量百分比，负值时为当前血量百分比）
			doOperateDirect(confEffect, source, process);
		}
			break;
		case OperateHurt: { // 对目标进行攻防运算
			doOperateHurt(confEffect, source, process);
		}
			break;
		case OperateCure: { // 对目标进行治疗运算
			doOperateCure(confEffect, source, process);
		}
			break;
		case OperateBuffCure: { // 对目标进行Buff治疗运算
			doOperateBuffCure(confEffect, source, process);
		}
		case OperatePoison: { // 对目标进行中毒运算
			doOperatePoison(confEffect, source, process);
		}
			break;
		case OperateBlood: { // 对目标进行流血运算
			doOperateBlood(confEffect, source, process);
		}
			break;
		case OperateBurn: { // 对目标进行灼烧运算
			doOperateBurn(confEffect, source, process);
		}
			break;
		case OperateKill: { // 对目标直接击杀，无视任何状态（包括无敌）
			doOperateKill(confEffect, source, process);
		}
			break;

		case BuffAdd: { // 给目标增加buff
			doBuffAdd(confEffect, source, process);
		}
			break;
		case BuffRemoveBySn: { // 给目标移除指定sn的buff
			doBuffRemoveBySn(confEffect, source, process);
		}
			break;
		case BuffRemoveByType: { // 给目标移除指定类型的buff
			doBuffRemoveByType(confEffect, source, process);
		}
			break;
		case BuffRemoveByGroup: { // 给目标移除指定组的buff
			doBuffRemoveByGroup(confEffect, source, process);
		}
			break;

		case RageAdd: { // 给目标加怒
			doRageAdd(confEffect, source, process);
		}
			break;

		case TriggerSetTimes: { // 设置触发次数，次数数据不存在时设置数据（-1），存在时减少1，当次数为0时不再触发
			doTriggerSetTimes(confEffect, source, process);
		}
			break;

		default: {
			Log.fight.error("未处理的逻辑类型：actionType={},sn={}", confEffect.actionType, confEffect.sn);
		}
		}
	}

	/**
	 * 获取对象的属性拷贝
	 */
	private Prop getPropCopy(FightObject fightObj, Prop skillProp) {
		Prop copyProp = null;
		if (skillProp != null) {
			// 附带了技能属性
			List<Prop> propList = new ArrayList<>();
			propList.add(skillProp);
			copyProp = PropManager.inst().getPropCopy(fightObj, propList);
		} else {
			copyProp = PropManager.inst().getPropCopy(fightObj, null);
		}
		return copyProp;
	}

	/**
	 * 对目标进行直接血量运算（正值时为最大血量万分比，负值时为当前血量万分比）
	 */
	private void doOperateDirect(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行行为逻辑
			operateDirect(confEffect, source, process);
		}
	}

	/**
	 * 对目标进行直接血量运算（正值时为最大血量万分比，负值时为当前血量万分比）
	 */
	private void operateDirect(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		// 判断是否闪避
		if (isTargetDodge(source, process, targetObj)) {
			return;
		}

		// 获取配表值
		int valuePct = Utils.intValue(confEffect.actionParam1);
		int value = Utils.intValue(confEffect.actionParam2);
		// 负值为当前血量万分比，正值为最大血量万分比
		if (valuePct < 0) {
			int hp = PropManager.inst().getCurHp(targetObj);
			value = (int) ((double)value + hp * valuePct / Utils.D10000);
		} else {
			int maxHp = PropManager.inst().getMaxHp(targetObj);
			value = (int) ((double)value + maxHp * valuePct / Utils.D10000);
		}

		SkillCastData skillData = getSkillCastData(source, process);
		Prop attackerProp = PropManager.inst().getPropCopy(skillData.creator, null);
		Prop targetProp = PropManager.inst().getPropCopy(targetObj, null);
		double damageCeof = (attackerProp.getOnePropValue(FightPropName.DamCeof.value()).getNumberValue()
				- targetProp.getOnePropValue(FightPropName.DamRedCeof.value()).getNumberValue() + Utils.D10000)
				/ Utils.D10000;

		value *= damageCeof;
		if (value < 0) {
			// 无敌
			if (PropManager.inst().isInvincible(targetObj)) {
				// 免疫
				addTargetDataState(source, process, targetObj, SkillTargetData.StateImmune, true);
				return;
			}
		}
		int addHp = PropManager.inst().addHp(targetObj, value);
		addTargetDataDamage(source, process, targetObj, addHp);
	}

	/**
	 * 对目标进行攻防运算
	 */
	private void doOperateHurt(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("===error in fightObj is null");
			return;
		}

		// 获取对象的属性拷贝
		Prop skillProp = process.skillProp;
		Prop copyProp = getPropCopy(fightObj, skillProp);
		// 获取技能数据
		SkillCastData skillData = getSkillCastData(source, process);
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行拷贝玩家属性
			Prop attackerProp = copyProp.getCheckTargetPropCopy(fightObj, obj, skillData);
			process.attackerProp = attackerProp;
			Prop targetProp = PropManager.inst().getCheckAttackerPropCopy(obj, fightObj, skillData);
			process.targetProp = targetProp;
			// 执行行为逻辑
			operateHurt(confEffect, source, process);
		}
	}

	/**
	 * 对目标进行攻防运算
	 */
	private void operateHurt(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject target = process.targetObj;
		if (target == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		// 判断是否闪避
		if (isTargetDodge(source, process, target)) {
			return;
		}

		// 获取配表值
		int valuePct = Utils.intValue(confEffect.actionParam1);
		int value = Utils.intValue(confEffect.actionParam2);
		// 处理特殊情况：免疫，闪避等
		SkillCastData skillData = getSkillCastData(source, process);
		Prop attackerProp = process.attackerProp;
		Prop targetProp = process.targetProp;
		// 计算免疫
		if (targetProp.isInvincible() || targetProp.isSkillImmune(skillData.skill.confSkill.atkType)) {
			// 免疫
			addTargetDataState(source, process, target, SkillTargetData.StateImmune);
			return;
		}
		// 计算命中万分比(最低2000)
		ValueBase hit = attackerProp.getOnePropValue(FightPropName.Hit.value());
		ValueBase dodge = targetProp.getOnePropValue(FightPropName.Dodge.value());
		double hitValue = hit.getNumberValue() - dodge.getNumberValue();
		if (hitValue < 2000) {
			hitValue = 2000;
		}
		if (hitValue < Utils.D10000 && stageObj.randUtils.nextInt(Utils.I10000) > hitValue) {
			// 闪避
			addTargetDataState(source, process, target, SkillTargetData.StateDodge);
			return;
		}

		// 计算伤害值
		double damage = 0;
		double damageAdd = 0;
		double damageAddEx = 0;
		int atk = attackerProp.getAtk(skillData.skill.confSkill.atkType);
		int def = targetProp.getDef(skillData.skill.confSkill.atkType);
		int pene = attackerProp.getPene(skillData.skill.confSkill.atkType);
		double baseDamage = (double)atk - Math.max(0, def * (Utils.D10000 - pene) / Utils.D10000);
		if (valuePct > 0) {
			ValueBase damAdd = attackerProp.getOnePropValue(FightPropName.DamAdd.value());
			ValueBase damAddEx = attackerProp.getOnePropValue(FightPropName.DamAddEx.value());
			ValueBase damRed = targetProp.getOnePropValue(FightPropName.DamRed.value());

			damageAdd = damageAdd + damAdd.getNumberValue() - damRed.getNumberValue();
			damageAddEx = damageAddEx + damAddEx.getNumberValue();
			if (skillData.skill.confSkill.atkType == AtkType.Mag.value()) {
				ValueBase damMagAdd = attackerProp.getOnePropValue(FightPropName.DamMagAdd.value());
				ValueBase damMagRed = targetProp.getOnePropValue(FightPropName.DamMagRed.value());
				damageAdd = damageAdd + damMagAdd.getNumberValue() - damMagRed.getNumberValue();
			} else {
				ValueBase damPhyAdd = attackerProp.getOnePropValue(FightPropName.DamPhyAdd.value());
				ValueBase damPhyRed = targetProp.getOnePropValue(FightPropName.DamPhyRed.value());
				damageAdd = damageAdd + damPhyAdd.getNumberValue() - damPhyRed.getNumberValue();
			}
			if (skillData.skill.confSkill.type == SkillType.Common.value()) {
				ValueBase damComAdd = attackerProp.getOnePropValue(FightPropName.DamComAdd.value());
				ValueBase damComRed = targetProp.getOnePropValue(FightPropName.DamComRed.value());
				damageAdd = damageAdd + damComAdd.getNumberValue() - damComRed.getNumberValue();
			} else {
				ValueBase damRageAdd = attackerProp.getOnePropValue(FightPropName.DamRageAdd.value());
				ValueBase damRageRed = targetProp.getOnePropValue(FightPropName.DamRageRed.value());
				damageAdd = damageAdd + damRageAdd.getNumberValue() - damRageRed.getNumberValue();
			}
			int rand = stageObj.randUtils.nextInt(1000) + 9500;// 95%-105%间浮动
			damage = baseDamage * rand / Utils.D10000 * valuePct / Utils.D10000 * (Utils.D10000 + damageAdd)
					/ Utils.D10000;
		}

		ValueBase damRedEx = targetProp.getOnePropValue(FightPropName.DamRedEx.value());
		if (skillData.skill.confSkill.atkType == AtkType.Mag.value()) {
			ValueBase damMagAddEx = attackerProp.getOnePropValue(FightPropName.DamMagAddEx.value());
			ValueBase damMagRedEx = targetProp.getOnePropValue(FightPropName.DamMagRedEx.value());
			damageAddEx = damageAddEx + damMagAddEx.getNumberValue() - damMagRedEx.getNumberValue()
					- damRedEx.getNumberValue();
		} else {
			ValueBase damPhyAddEx = attackerProp.getOnePropValue(FightPropName.DamPhyAddEx.value());
			ValueBase damPhyRedEx = targetProp.getOnePropValue(FightPropName.DamPhyRedEx.value());
			damageAddEx = damageAddEx + damPhyAddEx.getNumberValue() - damPhyRedEx.getNumberValue()
					- damRedEx.getNumberValue();
		}
		damage = damage + value + damageAddEx;
		// 最低伤害=攻击*0.15
		double minDamage = atk * 0.15;

		// 伤害倍数
		double damageCeof = (attackerProp.getOnePropValue(FightPropName.DamCeof.value()).getNumberValue()
				- targetProp.getOnePropValue(FightPropName.DamRedCeof.value()).getNumberValue() + Utils.D10000)
				/ Utils.D10000;
		damage = damage * damageCeof;
		minDamage = minDamage * damageCeof;
		// 判定最低伤害值
		if (damage < minDamage) {
			finalDamage(source, process, skillData, target, attackerProp, targetProp, (int) Math.floor(minDamage),
					null);
			return;
		}
		// 判定格挡
		ValueBase block = targetProp.getOnePropValue(FightPropName.Block.value());
		ValueBase antiBlock = attackerProp.getOnePropValue(FightPropName.AntiBlock.value());
		double blockValue = block.getNumberValue() - antiBlock.getNumberValue();
		if (blockValue > 0 && stageObj.randUtils.nextInt(Utils.I10000) < blockValue) {
			// 格挡即0.5倍伤害
			List<String> list = new ArrayList<>();
			list.add(SkillTargetData.StateBlock);
			finalDamage(source, process, skillData, target, attackerProp, targetProp, (int) Math.floor(damage * 0.5),
					list);
			return;
		}
		// 判定暴击
		ValueBase crit = attackerProp.getOnePropValue(FightPropName.Crit.value());
		ValueBase antiCrit = targetProp.getOnePropValue(FightPropName.AntiCrit.value());
		double critValue = crit.getNumberValue() - antiCrit.getNumberValue();
		if (critValue > 0 && stageObj.randUtils.nextInt(Utils.I10000) < critValue) {
			// 暴击
			ValueBase critAdd = attackerProp.getOnePropValue(FightPropName.CritAdd.value());
			ValueBase antiCritAdd = targetProp.getOnePropValue(FightPropName.AntiCritAdd.value());
			double critPctMin = Utils.D10000 * 1.5;// 最低暴伤1.5倍
			double critTimes = critPctMin + critAdd.getNumberValue() - antiCritAdd.getNumberValue();
			if (critTimes < critPctMin) {
				critTimes = critPctMin;
			}
			List<String> list = new ArrayList<>();
			list.add(SkillTargetData.StateCrit);
			finalDamage(source, process, skillData, target, attackerProp, targetProp,
					(int) Math.floor(damage * critTimes / Utils.D10000), list);
			return;
		}
		// 正常情况下的最终伤害
		finalDamage(source, process, skillData, target, attackerProp, targetProp, (int) Math.floor(damage), null);
	}

	/**
	 * 最终伤害计算
	 * 
	 * @param source
	 * @param process
	 * @param skillData
	 * @param target
	 * @param targetProp
	 * @param hurt
	 * @param stateList
	 */
	private void finalDamage(SourceParam source, ProcessParam process, SkillCastData skillData, FightObject target,
			Prop attackerProp, Prop targetProp, int hurt, List<String> stateList) {
		// 护盾计算
		int leftHurt = target.buffManager.hurtShield(source, process, hurt);
		if (hurt - leftHurt > 0) {
			// 吸收值
			addTargetDataValue(source, process, target, SkillTargetData.ValueShield, hurt - leftHurt);
		}
		// 反伤计算
		ValueBase damBack = targetProp.getOnePropValue(FightPropName.DamBack.value());
		if (damBack != null && damBack.isNumberValue() && damBack.getNumberValue() > 0) {
			int hurtBack = -(int) (damBack.getNumberValue() * leftHurt / Utils.D10000);
			FightObject fightObj = skillData.creator;
			int addHp = PropManager.inst().addHp(fightObj, hurtBack);
			addTargetDataDamage(source, process, fightObj, addHp, true);
		}
		// 吸血计算
		ValueBase bloodSuck = attackerProp.getOnePropValue(FightPropName.BloodSuck.value());
		ValueBase bloodSucked = targetProp.getOnePropValue(FightPropName.BloodSucked.value());
		double suck = 0;
		if (bloodSuck != null && bloodSuck.isNumberValue() && bloodSuck.getNumberValue() > 0) {
			suck = suck + bloodSuck.getNumberValue();
		}
		if (bloodSucked != null && bloodSucked.isNumberValue() && bloodSucked.getNumberValue() > 0) {
			suck = suck + bloodSucked.getNumberValue();
		}
		if (suck > 0) {
			int suckValue = (int) (suck * leftHurt / Utils.D10000);
			FightObject fightObj = skillData.creator;
			int addHp = PropManager.inst().addHp(fightObj, suckValue);
			addTargetDataDamage(source, process, fightObj, addHp, true);
		}
		// 最终伤害
		int addHp = PropManager.inst().addHp(target, -leftHurt);
		addTargetDataDamage(source, process, target, addHp, stateList);
	}
	
	/**
	 * 对目标进行治疗运算
	 */
	private void doOperateBuffCure(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		SkillCastData skillData = getSkillCastData(source, process);
		FightObject fightObj = skillData.creator;
		if (fightObj == null) {
			Log.fight.error("===error in fightObj is null");
			return;
		}
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			Prop targetProp = PropManager.inst().getCheckAttackerPropCopy(obj, fightObj, skillData);
			process.targetProp = targetProp;
			// 执行行为逻辑
			operateBuffCure(confEffect, source, process);
		}
	}

	/**
	 * 对目标进行治疗运算
	 */
	private void operateBuffCure(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}

		// 获取配表值
		int valuePct = Utils.intValue(confEffect.actionParam1);
		// int value = Utils.intValue(confEffect.actionParam2);
		// 治疗值=治疗技能百分比*（100%+治疗率+被治疗者的被治疗率）*施法者攻击力+施法者治疗值+被治疗者的被治疗值
		SkillCastData skillData = getSkillCastData(source, process);
		BuffTriggerData buffData = source.buffData;
		if (buffData == null) {
			Log.fight.error("===非buff调用，buff治疗运算只能是buff调用！");
			return;
		}
		Prop attackerProp = buffData.buff.fireProp;
		Prop targetProp = process.targetProp;
		if (targetProp == null) {
			Log.fight.error("===缺少targetProp");
			return;
		}

		int atk = attackerProp.getAtk(skillData.skill.confSkill.atkType);
		ValueBase cureAdd = attackerProp.getOnePropValue(FightPropName.CureAdd.value());
		ValueBase cureAddEx = attackerProp.getOnePropValue(FightPropName.CureAddEx.value());
		ValueBase healAdd = targetProp.getOnePropValue(FightPropName.HealAdd.value());
		ValueBase healAddEx = targetProp.getOnePropValue(FightPropName.HealAddEx.value());
		int cureValue = (int) (valuePct / Utils.D10000
				* (Utils.D10000 + cureAdd.getNumberValue() + healAdd.getNumberValue()) / Utils.D10000 * atk
				+ cureAddEx.getNumberValue() + healAddEx.getNumberValue());
		if (cureValue < 0) {
			cureValue = 0;
		}
		// 伤害倍数
		double damageCeof = (attackerProp.getOnePropValue(FightPropName.DamCeof.value()).getNumberValue()
				+ Utils.D10000) / Utils.D10000;
		cureValue = (int) (cureValue * damageCeof);

		int addHp = PropManager.inst().addHp(targetObj, cureValue);
		addTargetDataDamage(source, process, targetObj, addHp);
	}

	/**
	 * 对目标进行治疗运算
	 */
	private void doOperateCure(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("===error in fightObj is null");
			return;
		}

		// 获取对象的属性拷贝
		Prop skillProp = process.skillProp;
		Prop copyProp = getPropCopy(fightObj, skillProp);
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行拷贝玩家属性
			SkillCastData skillData = getSkillCastData(source, process);
			Prop attackerProp = copyProp.getCheckTargetPropCopy(fightObj, obj, skillData);
			process.attackerProp = attackerProp;
			Prop targetProp = PropManager.inst().getCheckAttackerPropCopy(obj, fightObj, skillData);
			process.targetProp = targetProp;
			// 执行行为逻辑
			operateCure(confEffect, source, process);
		}
	}

	/**
	 * 对目标进行治疗运算
	 */
	private void operateCure(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}

		// 获取配表值
		int valuePct = Utils.intValue(confEffect.actionParam1);
		// int value = Utils.intValue(confEffect.actionParam2);
		// 治疗值=治疗技能百分比*（100%+治疗率+被治疗者的被治疗率）*施法者攻击力+施法者治疗值+被治疗者的被治疗值
		SkillCastData skillData = getSkillCastData(source, process);
		Prop attackerProp = process.attackerProp;
		if (attackerProp == null) {
			Log.fight.error("===缺少attackerProp");
			return;
		}
		Prop targetProp = process.targetProp;
		if (targetProp == null) {
			Log.fight.error("===缺少targetProp");
			return;
		}

		int atk = attackerProp.getAtk(skillData.skill.confSkill.atkType);
		ValueBase cureAdd = attackerProp.getOnePropValue(FightPropName.CureAdd.value());
		ValueBase cureAddEx = attackerProp.getOnePropValue(FightPropName.CureAddEx.value());
		ValueBase healAdd = targetProp.getOnePropValue(FightPropName.HealAdd.value());
		ValueBase healAddEx = targetProp.getOnePropValue(FightPropName.HealAddEx.value());
		int cureValue = (int) (valuePct / Utils.D10000
				* (Utils.D10000 + cureAdd.getNumberValue() + healAdd.getNumberValue()) / Utils.D10000 * atk
				+ cureAddEx.getNumberValue() + healAddEx.getNumberValue());
		if (cureValue < 0) {
			cureValue = 0;
		}
		// 伤害倍数
		double damageCeof = (attackerProp.getOnePropValue(FightPropName.DamCeof.value()).getNumberValue() + Utils.D10000)
				/ Utils.D10000;
		cureValue = (int) (cureValue * damageCeof);

		int addHp = PropManager.inst().addHp(targetObj, cureValue);
		addTargetDataDamage(source, process, targetObj, addHp);
	}

	/**
	 * 对目标进行中毒运算
	 */
	private void doOperatePoison(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		SkillCastData skillData = getSkillCastData(source, process);
		FightObject fightObj = skillData.creator;
		if (fightObj == null) {
			Log.fight.error("===error in fightObj is null");
			return;
		}
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			Prop targetProp = PropManager.inst().getCheckAttackerPropCopy(obj, fightObj, skillData);
			process.targetProp = targetProp;
			// 执行行为逻辑
			operatePoison(confEffect, source, process);
		}
	}

	/**
	 * 对目标进行中毒运算
	 */
	private void operatePoison(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		// 判断是否无敌
		if (PropManager.inst().isInvincible(targetObj)) {
			return;
		}

		// 获取配表值
		int valuePct = Utils.intValue(confEffect.actionParam1);
		// int value = Utils.intValue(confEffect.actionParam2);
		// 伤害值=技能百分比*（100%+毒伤率-目标抗毒率）*施法者攻击力+施法者毒伤值-目标抗毒值
		SkillCastData skillData = getSkillCastData(source, process);
		BuffTriggerData buffData = source.buffData;
		if (buffData == null) {
			Log.fight.error("===非buff调用，中毒运算只能是buff调用！");
			return;
		}

		Prop attackerProp = buffData.buff.fireProp;
		Prop targetProp = process.targetProp;
		if (targetProp == null) {
			Log.fight.error("===缺少targetProp");
			return;
		}
		int atk = attackerProp.getAtk(skillData.skill.confSkill.atkType);
		ValueBase poisonAdd = attackerProp.getOnePropValue(FightPropName.PoisonAdd.value());
		ValueBase poisonAddEx = attackerProp.getOnePropValue(FightPropName.PoisonAddEx.value());
		ValueBase antiPoisonAdd = targetProp.getOnePropValue(FightPropName.AntiPoisonAdd.value());
		ValueBase antiPoisonAddEx = targetProp.getOnePropValue(FightPropName.AntiPoisonAddEx.value());
		int poisonValue = (int) (valuePct / Utils.D10000
				* (Utils.D10000 + poisonAdd.getNumberValue() + antiPoisonAdd.getNumberValue()) / Utils.D10000 * atk
				+ poisonAddEx.getNumberValue() + antiPoisonAddEx.getNumberValue()) * buffData.buff.times;
		if (poisonValue < 0) {
			poisonValue = 0;
		} else {
			poisonValue = -poisonValue;
		}
		double damageCeof = (attackerProp.getOnePropValue(FightPropName.DamCeof.value()).getNumberValue()
				- targetProp.getOnePropValue(FightPropName.DamRedCeof.value()).getNumberValue() + Utils.D10000)
				/ Utils.D10000;
		poisonValue *= damageCeof;
		int addHp = PropManager.inst().addHp(targetObj, poisonValue);
		addTargetDataDamage(source, process, targetObj, addHp);
	}

	/**
	 * 对目标进行流血运算
	 */
	private void doOperateBlood(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		SkillCastData skillData = getSkillCastData(source, process);
		FightObject fightObj = skillData.creator;
		if (fightObj == null) {
			Log.fight.error("===error in fightObj is null");
			return;
		}
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			Prop targetProp = PropManager.inst().getCheckAttackerPropCopy(obj, fightObj, skillData);
			process.targetProp = targetProp;
			// 执行行为逻辑
			operateBlood(confEffect, source, process);
		}
	}

	/**
	 * 对目标进行流血运算
	 */
	private void operateBlood(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		// 判断是否无敌
		if (PropManager.inst().isInvincible(targetObj)) {
			return;
		}

		// 获取配表值
		int valuePct = Utils.intValue(confEffect.actionParam1);
		// int value = Utils.intValue(confEffect.actionParam2);
		// 伤害值=技能百分比*（100%+毒伤率-目标抗毒率）*施法者攻击力+施法者毒伤值-目标抗毒值
		SkillCastData skillData = getSkillCastData(source, process);
		BuffTriggerData buffData = source.buffData;
		if (buffData == null) {
			Log.fight.error("===非buff调用，流血运算只能是buff调用！");
			return;
		}

		Prop attackerProp = buffData.buff.fireProp;
		Prop targetProp = process.targetProp;
		if (targetProp == null) {
			Log.fight.error("===缺少targetProp");
			return;
		}
		int atk = attackerProp.getAtk(skillData.skill.confSkill.atkType);
		ValueBase bloodAdd = attackerProp.getOnePropValue(FightPropName.BloodAdd.value());
		ValueBase bloodAddEx = attackerProp.getOnePropValue(FightPropName.BloodAddEx.value());
		ValueBase antiBloodAdd = targetProp.getOnePropValue(FightPropName.AntiBloodAdd.value());
		ValueBase antiBloodAddEx = targetProp.getOnePropValue(FightPropName.AntiBloodAddEx.value());
		int bloodValue = (int) (valuePct / Utils.D10000
				* (Utils.D10000 + bloodAdd.getNumberValue() + antiBloodAdd.getNumberValue()) / Utils.D10000 * atk
				+ bloodAddEx.getNumberValue() + antiBloodAddEx.getNumberValue()) * buffData.buff.times;
		if (bloodValue < 0) {
			bloodValue = 0;
		} else {
			bloodValue = -bloodValue;
		}
		double damageCeof = (attackerProp.getOnePropValue(FightPropName.DamCeof.value()).getNumberValue()
				- targetProp.getOnePropValue(FightPropName.DamRedCeof.value()).getNumberValue() + Utils.D10000)
				/ Utils.D10000;
		bloodValue *= damageCeof;
		int addHp = PropManager.inst().addHp(targetObj, bloodValue);
		addTargetDataDamage(source, process, targetObj, addHp);
	}

	/**
	 * 对目标进行灼烧运算
	 */
	private void doOperateBurn(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		SkillCastData skillData = getSkillCastData(source, process);
		FightObject fightObj = skillData.creator;
		if (fightObj == null) {
			Log.fight.error("===error in fightObj is null");
			return;
		}
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			Prop targetProp = PropManager.inst().getCheckAttackerPropCopy(obj, fightObj, skillData);
			process.targetProp = targetProp;
			// 执行行为逻辑
			operateBurn(confEffect, source, process);
		}
	}

	/**
	 * 对目标进行灼烧运算
	 */
	private void operateBurn(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		// 判断是否无敌
		if (PropManager.inst().isInvincible(targetObj)) {
			return;
		}

		// 获取配表值
		int valuePct = Utils.intValue(confEffect.actionParam1);
		// int value = Utils.intValue(confEffect.actionParam2);
		// 伤害值=技能百分比*（100%+毒伤率-目标抗毒率）*施法者攻击力+施法者毒伤值-目标抗毒值
		SkillCastData skillData = getSkillCastData(source, process);
		BuffTriggerData buffData = source.buffData;
		if (buffData == null) {
			Log.fight.error("===非buff调用，灼烧运算只能是buff调用！");
			return;
		}

		Prop attackerProp = buffData.buff.fireProp;
		Prop targetProp = process.targetProp;
		if (targetProp == null) {
			Log.fight.error("===缺少targetProp");
			return;
		}
		int atk = attackerProp.getAtk(skillData.skill.confSkill.atkType);
		ValueBase burnAdd = attackerProp.getOnePropValue(FightPropName.BurnAdd.value());
		ValueBase burnAddEx = attackerProp.getOnePropValue(FightPropName.BurnAddEx.value());
		ValueBase antiBurnAdd = targetProp.getOnePropValue(FightPropName.AntiBurnAdd.value());
		ValueBase antiBurnAddEx = targetProp.getOnePropValue(FightPropName.AntiBurnAddEx.value());
		int burnValue = (int) (valuePct / Utils.D10000
				* (Utils.D10000 + burnAdd.getNumberValue() + antiBurnAdd.getNumberValue()) / Utils.D10000 * atk
				+ burnAddEx.getNumberValue() + antiBurnAddEx.getNumberValue()) * buffData.buff.times;
		if (burnValue < 0) {
			burnValue = 0;
		} else {
			burnValue = -burnValue;
		}
		double damageCeof = (attackerProp.getOnePropValue(FightPropName.DamCeof.value()).getNumberValue()
				- targetProp.getOnePropValue(FightPropName.DamRedCeof.value()).getNumberValue() + Utils.D10000)
				/ Utils.D10000;
		burnValue *= damageCeof;
		int addHp = PropManager.inst().addHp(targetObj, burnValue);
		addTargetDataDamage(source, process, targetObj, addHp);
	}

	/**
	 * 对目标直接击杀，无视任何状态（包括无敌）
	 */
	private void doOperateKill(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行行为逻辑
			operateKill(confEffect, source, process);
		}
	}

	/**
	 * 对目标直接击杀，无视任何状态（包括无敌）
	 */
	private void operateKill(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		int hp = -PropManager.inst().getCurHp(targetObj);
		int addHp = PropManager.inst().addHp(targetObj, hp);
		addTargetDataDamage(source, process, targetObj, addHp);
	}

	/**
	 * 给目标增加buff
	 */
	private void doBuffAdd(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		
		FightObject fightObj = source.fightObj;
		if (fightObj == null) {
			Log.fight.error("===error in fightObj is null");
			return;
		}
		// 获取对象的属性拷贝
		Prop skillProp = process.skillProp;
		Prop copyProp = getPropCopy(fightObj, skillProp);
		// 获取技能数据
		SkillCastData skillData = getSkillCastData(source, process);
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行拷贝玩家属性
			Prop attackerProp = copyProp.getCheckTargetPropCopy(fightObj, obj, skillData);
			process.attackerProp = attackerProp;
			Prop targetProp = PropManager.inst().getCheckAttackerPropCopy(obj, fightObj, skillData);
			process.targetProp = targetProp;
			// 执行行为逻辑
			buffAdd(confEffect, source, process);
		}
	}

	/**
	 * 给目标增加buff
	 */
	private void buffAdd(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		FightObject creator = getCreator(source, process);
		if (creator == null) {
			Log.fight.error("===error in creator is null");
			return;
		}
		// 判断是否闪避
		if (isTargetDodge(source, process, targetObj)) {
			return;
		}

		// 获取配表值
		int buffSn = Utils.intValue(confEffect.actionParam1);
		int probability = Utils.intValue(confEffect.actionParam2);
		// 机率判定
		if (probability > 0 && probability < Utils.I10000) {
			if (stageObj.randUtils.nextInt(Utils.I10000) >= probability) {
				return;
			}
		}
		// 增加buff
		Buff buff = targetObj.buffManager.addBuff(source, process, buffSn, 1, creator);
		if (buff != null) {
			List<Buff> list = new ArrayList<>();
			list.add(buff);
			addTargetDataBuffList(source, process, targetObj, list);
		}
	}

	/**
	 * 给目标移除指定sn的buff
	 */
	private void doBuffRemoveBySn(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行行为逻辑
			buffRemoveBySn(confEffect, source, process);
		}
	}

	/**
	 * 给目标移除指定sn的buff
	 */
	private void buffRemoveBySn(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		FightObject creator = getCreator(source, process);
		if (creator == null) {
			Log.fight.error("===error in creator is null");
			return;
		}
		// 判断是否闪避
		if (isTargetDodge(source, process, targetObj)) {
			return;
		}

		// 获取配表值
		List<Integer> buffSnList = Utils.strToIntList(confEffect.actionParam1);
		int probability = Utils.intValue(confEffect.actionParam2);
		// 机率判定
		if (probability > 0 && probability < Utils.I10000) {
			if (stageObj.randUtils.nextInt(Utils.I10000) >= probability) {
				return;
			}
		}
		// 移除buff
		List<Buff> buffList = targetObj.buffManager.getBuffBySn(buffSnList);
		for (Buff buff : buffList) {
			targetObj.buffManager.removeBuff(buff, false);
		}
	}

	/**
	 * 给目标移除指定类型的buff
	 */
	private void doBuffRemoveByType(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行行为逻辑
			buffRemoveByType(confEffect, source, process);
		}
	}

	/**
	 * 给目标移除指定类型的buff
	 */
	private void buffRemoveByType(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		FightObject creator = getCreator(source, process);
		if (creator == null) {
			Log.fight.error("===error in creator is null");
			return;
		}
		// 判断是否闪避
		if (isTargetDodge(source, process, targetObj)) {
			return;
		}

		// 获取配表值
		List<Integer> buffTypeList = Utils.strToIntList(confEffect.actionParam1);
		int probability = Utils.intValue(confEffect.actionParam2);
		// 机率判定
		if (probability > 0 && probability < Utils.I10000) {
			if (stageObj.randUtils.nextInt(Utils.I10000) >= probability) {
				return;
			}
		}
		// 移除buff
		List<Buff> buffList = targetObj.buffManager.getBuffByType(buffTypeList);
		for (Buff buff : buffList) {
			targetObj.buffManager.removeBuff(buff, false);
		}
	}

	/**
	 * 给目标移除指定组的buff
	 */
	private void doBuffRemoveByGroup(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行行为逻辑
			buffRemoveByGroup(confEffect, source, process);
		}
	}

	/**
	 * 给目标移除指定组的buff
	 */
	private void buffRemoveByGroup(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		FightObject creator = getCreator(source, process);
		if (creator == null) {
			Log.fight.error("===error in creator is null");
			return;
		}
		// 判断是否闪避
		if (isTargetDodge(source, process, targetObj)) {
			return;
		}

		// 获取配表值
		List<Integer> buffGroupList = Utils.strToIntList(confEffect.actionParam1);
		int probability = Utils.intValue(confEffect.actionParam2);
		// 机率判定
		if (probability > 0 && probability < Utils.I10000) {
			if (stageObj.randUtils.nextInt(Utils.I10000) >= probability) {
				return;
			}
		}
		// 移除buff
		List<Buff> buffList = targetObj.buffManager.getBuffByGroup(buffGroupList);
		for (Buff buff : buffList) {
			targetObj.buffManager.removeBuff(buff, false);
		}
	}

	/**
	 * 给目标加怒
	 */
	private void doRageAdd(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行行为逻辑
			rageAdd(confEffect, source, process);
		}
	}

	/**
	 * 给目标加怒
	 */
	private void rageAdd(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		FightObject targetObj = process.targetObj;
		if (targetObj == null) {
			Log.fight.error("===error in targetObj is null");
			return;
		}
		// 判断是否闪避
		if (isTargetDodge(source, process, targetObj)) {
			return;
		}

		// 获取配表值
		int rage = Utils.intValue(confEffect.actionParam1);
		int probability = Utils.intValue(confEffect.actionParam2);
		// 机率判定
		if (probability > 0 && probability < Utils.I10000) {
			if (stageObj.randUtils.nextInt(Utils.I10000) >= probability) {
				return;
			}
		}
		// 增加怒气
		int rageChange = PropManager.inst().addRage(targetObj, rage);
		addTargetDataValue(source, process, targetObj, SkillTargetData.ValueShowRage, rageChange);
	}

	/**
	 * 设置触发次数，次数数据不存在时设置数据（-1），存在时减少1，当次数为0时不再触发
	 */
	private void doTriggerSetTimes(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		List<FightObject> targetList = process.targetObjList;
		if (targetList == null) {
			return;
		}
		// 对每个目标执行行为逻辑
		for (FightObject obj : targetList) {
			process.targetObj = obj;
			// 执行行为逻辑
			triggerSetTimes(confEffect, source, process);
		}
	}

	/**
	 * 设置触发次数，次数数据不存在时设置数据（-1），存在时减少1，当次数为0时不再触发
	 */
	private void triggerSetTimes(ConfSkillEffect confEffect, SourceParam source, ProcessParam process) {
		FightStageObject stageObj = source.stageObj;
		if (stageObj == null) {
			Log.fight.error("===error in stageObj is null");
			return;
		}
		Trigger trigger = stageObj.triggerManager.nowTrigger;
		if (trigger == null) {
			Log.fight.error("===error in trigger is null");
			return;
		}
		SkillCastData skillData = source.skillData;
		if (skillData == null) {
			Log.fight.error("===error in skillData is null");
			return;
		}

		// 获取配表值
		int times = Utils.intValue(confEffect.actionParam1);
		// int value = Utils.intValue(confEffect.actionParam2);
		if (times <= 0) {
			Log.fight.error("===times error,actionParam1={},sn={}", confEffect.actionParam1, confEffect.sn);
			return;
		}

		int triggerKey = trigger.triggerPoint.value();
		Integer oldTimes = skillData.skill.triggerTimesMap.get(triggerKey);
		if (oldTimes == null) {
			times = times - 1;
			skillData.skill.triggerTimesMap.put(triggerKey, times);
		} else {
			times = oldTimes.intValue() - 1;
			skillData.skill.triggerTimesMap.put(triggerKey, times);
		}
	}

	/**
	 * 获取技能或buff的创建者
	 */
	private FightObject getCreator(SourceParam source, ProcessParam process) {
		FightObject ret = null;
		BuffTriggerData buffData = source.buffData;
		if (buffData != null) {
			// 此次操作是处理buff
			ret = buffData.buff.fireObj;
		} else {
			// 此次操作是处理技能
			SkillCastData skillData = source.skillData;
			if (skillData != null) {
				ret = skillData.creator;
			}
		}
		return ret;
	}

	/**
	 * 获取技能数据
	 */
	public SkillCastData getSkillCastData(SourceParam source, ProcessParam process) {
		SkillCastData skillData = null;
		BuffTriggerData buffData = source.buffData;
		if (buffData != null) {
			// 此次操作是处理buff
			skillData = buffData.buff.skillData;
		} else {
			// 此次操作是处理技能
			skillData = source.skillData;
		}
		return skillData;
	}

	/**
	 * 判断是否闪避
	 */
	public boolean isTargetDodge(SourceParam source, ProcessParam process, FightObject target) {
		boolean ret = false;
		// buff作用效果是必中的，技能才能被闪避
		BuffTriggerData buffData = source.buffData;
		if (buffData != null) {
			// 此次操作是处理buff
			ret = false;
		} else {
			// 此次操作是处理技能，判断技能是否被闪避
			SkillCastData skillData = source.skillData;
			if (skillData != null) {
				ret = skillData.isTargetDodge(target);
			}
		}
		return ret;
	}

	/**
	 * 增加目标数据
	 * 
	 * @param source
	 * @param process
	 * @param target
	 * @param damage
	 * @param rage
	 * @param buffList
	 * @param stateList
	 * @param isExtra
	 */
	private void addTargetData(SourceParam source, ProcessParam process, FightObject target,
			Map<String, Integer> valueMap, List<Buff> buffList, List<String> stateList, boolean isExtra) {
		SkillCastData skillData = source.skillData;
		BuffTriggerData buffData = source.buffData;
		if (buffData != null) {
			// 此次操作是处理buff
			int damage = 0;
			if (valueMap != null && valueMap.containsKey(SkillTargetData.ValueDamage)) {
				damage = valueMap.get(SkillTargetData.ValueDamage);
			}
			int rage = 0;
			if (valueMap != null && valueMap.containsKey(SkillTargetData.ValueRage)) {
				rage = valueMap.get(SkillTargetData.ValueRage);
			}
			buffData.addTargetData(target, buffList, damage, rage);
		} else if (skillData != null) {
			// 此次操作是处理技能
			SkillTargetData targetData = new SkillTargetData(target, valueMap, buffList, stateList);
			if (isExtra) {
				skillData.addTargetDataExtra(targetData);
			} else {
				skillData.addTargetData(targetData);
			}
		}

		if (valueMap != null && valueMap.containsKey(SkillTargetData.ValueDamage)) {
			int damage = valueMap.get(SkillTargetData.ValueDamage);
			if (damage < 0) {
				// 死亡
				if (PropManager.inst().getCurHp(target) <= 0) {
					FightObject killer = null;
					if (buffData != null) {
						if (target == buffData.buff.skillData.creator) {
							killer = process.targetObj;
						} else {
							killer = buffData.buff.skillData.creator;
						}
					} else if (skillData != null) {
						if (target == skillData.creator) {
							killer = process.targetObj;
						} else {
							killer = skillData.creator;
						}
					}
					if (killer == null) {
						killer = target;
					}
					target.Deading(target);
				}
			}
		}
		Log.fight.info("目标数据:targetPos:{},hp:{},skillSn:{},valueMap:{},state:{},buffList:{},isBuff:{},stage:{}",
				target.getFightPos(), PropManager.inst().getCurHp(target),
				buffData == null ? skillData.skill.sn : buffData.buff.sn, valueMap, stateList, buffList,
				buffData == null ? false : true, target.stageObj.stageId);
	}

	/**
	 * 增加目标伤害值
	 * 
	 * @param source
	 * @param process
	 * @param target
	 * @param damage
	 * @param isExtra
	 */
	private void addTargetDataDamage(SourceParam source, ProcessParam process, FightObject target, int damage,
			boolean isExtra) {
		Map<String, Integer> valueMap = new HashMap<>();
		valueMap.put(SkillTargetData.ValueDamage, damage);
		addTargetData(source, process, target, valueMap, null, null, isExtra);
	}

	private void addTargetDataDamage(SourceParam source, ProcessParam process, FightObject target, int damage) {
		addTargetDataDamage(source, process, target, damage, false);
	}

	private void addTargetDataDamage(SourceParam source, ProcessParam process, FightObject target, int damage,
			List<String> stateList) {
		addTargetDataDamage(source, process, target, damage, stateList, false);
	}

	private void addTargetDataDamage(SourceParam source, ProcessParam process, FightObject target, int damage,
			List<String> stateList, boolean isExtra) {
		Map<String, Integer> valueMap = new HashMap<>();
		valueMap.put(SkillTargetData.ValueDamage, damage);
		addTargetData(source, process, target, valueMap, null, stateList, isExtra);
	}

	/**
	 * 增加目标固定值：怒气值，显示怒气值，吸收值等
	 */
	public void addTargetDataValue(SourceParam source, ProcessParam process, FightObject target, String type, int value,
			boolean isExtra) {
		Map<String, Integer> valueMap = new HashMap<>();
		valueMap.put(type, value);
		addTargetData(source, process, target, valueMap, null, null, isExtra);
	}

	public void addTargetDataValue(SourceParam source, ProcessParam process, FightObject target, String type,
			int value) {
		addTargetDataValue(source, process, target, type, value, false);
	}

	/**
	 * 增加目标固定状态：暴击，格挡，闪避，免疫等
	 */
	public void addTargetDataState(SourceParam source, ProcessParam process, FightObject target, List<String> stateList,
			boolean isExtra) {
		addTargetData(source, process, target, null, null, stateList, isExtra);
	}

	public void addTargetDataState(SourceParam source, ProcessParam process, FightObject target,
			List<String> stateList) {
		addTargetDataState(source, process, target, stateList, false);
	}

	public void addTargetDataState(SourceParam source, ProcessParam process, FightObject target, String state) {
		List<String> stateList = new ArrayList<>();
		stateList.add(state);
		addTargetDataState(source, process, target, stateList, false);
	}

	public void addTargetDataState(SourceParam source, ProcessParam process, FightObject target, String state,
			boolean isExtra) {
		List<String> stateList = new ArrayList<>();
		stateList.add(state);
		addTargetDataState(source, process, target, stateList, isExtra);
	}

	/**
	 * 增加目标buff数据
	 * 
	 * @param source
	 * @param process
	 * @param target
	 * @param buffList
	 * @param isExtra
	 */
	private void addTargetDataBuffList(SourceParam source, ProcessParam process, FightObject target,
			List<Buff> buffList, boolean isExtra) {
		addTargetData(source, process, target, null, buffList, null, isExtra);
	}

	private void addTargetDataBuffList(SourceParam source, ProcessParam process, FightObject target,
			List<Buff> buffList) {
		addTargetDataBuffList(source, process, target, buffList, false);
	}

}
