package game.turnbasedsrv.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.DTurnbasedObjectSkill;
import game.turnbasedsrv.combatEvent.EventCastSkill;
import game.turnbasedsrv.enumType.AtkType;
import game.turnbasedsrv.enumType.SkillType;
import game.turnbasedsrv.enumType.TriggerPoint;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.param.TriggerParam;
import game.turnbasedsrv.prop.Prop;
import game.turnbasedsrv.prop.PropManager;
import game.turnbasedsrv.trigger.Trigger;
import game.turnbasedsrv.trigger.TriggerListen;
import game.worldsrv.config.ConfSkill;
import game.worldsrv.config.ConfSkillEffect;
import game.worldsrv.support.Log;

public class Skill {
	/** 技能sn **/
	public int sn;
	/** 技能等级 **/
	public int lv;
	/** 技能配表 **/
	public ConfSkill confSkill;
	/** 技能养成加成威力 **/
	public int power;
	/** 技能养成加成固定值 **/
	public int value;
	/** 监听列表 **/
	public List<TriggerListen> listenList = new ArrayList<>();
	/** 效果分组 **/
	public Map<Integer, List<Integer>> effectMap = new HashMap<>();
	/** 技能拥有者 **/
	public FightObject owner;
	/** 被动触发次数(大于-1为有次数) **/
	public Map<Integer, Integer> triggerTimesMap;

	/**
	 * 获取实例
	 * @param sn 技能sn （confSkill.sn）
	 * @param lv 等级
	 * @param power 养成加成威力
	 * @param value 养成加成固定值
	 * @return
	 */
	public static Skill newInstance(int sn, int lv, int power, int value, FightObject owner) {
	    if (sn == 0) {
	        return null;
        }
		ConfSkill confSkill = ConfSkill.get(sn);
		if (confSkill == null) {
			Log.fight.error("confSkill配表错误，no find sn ={}", sn);
			return null;
		}
		return new Skill(sn, lv, power, value, confSkill, owner);
	}

	/** 构造函数 **/
	private Skill(int sn, int lv, int power, int value, ConfSkill confSkill, FightObject owner) {
		this.sn = sn;
		this.lv = lv;
		this.power = power;
		this.value = value;
		this.confSkill = confSkill;
		this.owner = owner;
		if (confSkill.effects != null && confSkill.effects.length > 0) {
			for (int i = 0; i < confSkill.effects.length; i++) {
				int effectSn = confSkill.effects[i];
				ConfSkillEffect confEffect = ConfSkillEffect.get(effectSn);
				if (confEffect == null) {
					Log.fight.error("confSkill配表错误，no effect sn ={}", effectSn);
					continue;
				}
				int type = confEffect.triggerType;
				List<Integer> list = this.effectMap.get(type);
				if (list == null) {
					list = new ArrayList<>();
					this.effectMap.put(type, list);
				}
				list.add(effectSn);
			}
		}
	}

	/**
	 * 是否是被动
	 * 
	 * @return
	 */
	public boolean isPassiveSkill() {
		if (this.confSkill == null) {
			return false;
		}
		return !this.confSkill.active;
	}

	/**
	 * 执行技能的被动部分
	 */
	public void startPassive() {
		if (this.effectMap.isEmpty()) {
			return;
		}

		// 设置被动监听
		for (Map.Entry<Integer, List<Integer>> entry : this.effectMap.entrySet()) {
			int type = entry.getKey();
			if (type == 0) {
				continue;
			}
			TriggerPoint triggerPoint = TriggerPoint.getByValue(type);
			if (triggerPoint == null) {
				Log.fight.error("ConfSkillEffect配表错误，no triggerType sn ={}", entry.getValue());
				continue;
			}
			TriggerListen listen = new TriggerListen(triggerPoint, this.owner.idFight, this::passiveTrigger);
			this.owner.combatObj.triggerManager.addListen(listen);
			this.listenList.add(listen);
		}

		// 立即触发
		List<Integer> list = this.effectMap.get(TriggerPoint.PassiveStart.value());
		if (list != null) {
			SkillCastData data = new SkillCastData(this, this.owner);
			TriggerParam param = new TriggerParam(this.owner, this.owner);
			Trigger trigger = new Trigger(TriggerPoint.PassiveStart, param);
			SkillEffect.doSkillPassiveEffect(trigger, data, list);
		}
	}
	
	/**
	 * 死亡触发被动技能
	 */
	public void deathPassive() {
		// 死亡立即触发
		List<Integer> list = this.effectMap.get(TriggerPoint.PassiveDeath.value());
		if (list != null) {
			TriggerParam param = new TriggerParam(this.owner, this.owner);
			Trigger trigger = new Trigger(TriggerPoint.PassiveDeath, param);
			passiveTrigger(trigger);
		}
	}

	/**
	 * 被动触发效果
	 * 
	 */
	public void passiveTrigger(Trigger trigger) {
		if (!canTrigger()) {
			return;
		}
		TriggerPoint triggerType = trigger.triggerPoint;
		// 触发次数
		if (triggerTimesMap != null) {
			Integer times = triggerTimesMap.get(triggerType.value());
			if (times != null && times.intValue() == 0) {
				return;
			}
		}

		List<Integer> list = this.effectMap.get(triggerType.value());
		if (list == null) {
			return;
		}
		SkillCastData data = new SkillCastData(this, this.owner);
		boolean isTrigger = SkillEffect.doSkillPassiveEffect(trigger, data, list);
		if (!isTrigger) {
			return;
		}
		// 跳过战斗，不触发等待事件
		if (this.owner.combatObj.isQuickFight) {
			return;
		}
		Log.fight.info("触发被动技能：fightObjPos={},sn={},stageSn={}", this.owner.getFightPos(), this.sn,
				this.owner.combatObj.stageSn);
		this.owner.combatObj.sendCastSkillInfoMsg(data);
	}

	/**
	 * 是否可以触发
	 * 
	 * @return
	 */
	public boolean canTrigger() {
		if (this.owner.isDie()) {
			return false;
		}
		return true;
	}

	/**
	 * 执行技能
	 */
	public void cast() {
		List<Integer> list = new ArrayList<>();
		for(List<Integer> ls : this.effectMap.values()) {
			list.addAll(ls);
		}

		SkillCastData data = new SkillCastData(this, this.owner);
		// 怒气判断
		if (confSkill.fireClearRage > 0) {
			int rage = PropManager.inst().getCurRage(this.owner);
			if (rage < confSkill.fireClearRage) {
				Log.fight.error("怒气不足：{} 施放技能={},", this.owner.name, confSkill.sn);
				return;// 怒气不足
			} else {
				// 自身扣除怒气
				int rageChange = PropManager.inst().addRage(this.owner, -confSkill.fireClearRage);
				SkillTargetData targetData = new SkillTargetData(this.owner, SkillTargetData.ValueRage, rageChange);
				data.addCasterData(targetData);
			}
		}
		// 是否自身增加怒气
		if (confSkill.fireAddRage > 0) {
			int rageChange = PropManager.inst().addRage(this.owner, confSkill.fireAddRage);
			SkillTargetData targetData = new SkillTargetData(this.owner, SkillTargetData.ValueRage, rageChange);
			data.addCasterData(targetData);
		}

		// 执行主动技能效果
		// 是否命中目标增加怒气
		boolean isTrigger = SkillEffect.doSkillEffect(data, list);
		if (isTrigger && confSkill.targetAddRage > 0) {
			for (FightObject target : data.getAllTarget()) {
				int rageChange = PropManager.inst().addRage(target, confSkill.targetAddRage);
				SkillTargetData targetData = new SkillTargetData(target, SkillTargetData.ValueRage, rageChange);
				data.addTargetData(targetData);
			}
		}
		
		// 设置触发器：攻击后，普攻后，怒攻后
		// 攻击后
		TriggerParam paramAfterAttack = new TriggerParam(data, this.owner);
		Trigger triggerAfterAttack = new Trigger(TriggerPoint.AfterAttack, paramAfterAttack);
		this.owner.combatObj.triggerManager.addTrigger(triggerAfterAttack);
		// 普攻后，怒攻后
		if (this.confSkill.type == SkillType.Common.value()) {// 普攻
			TriggerParam param = new TriggerParam(data, this.owner);
			Trigger trigger = new Trigger(TriggerPoint.AfterNormalAtk, param);
			this.owner.combatObj.triggerManager.addTrigger(trigger);
		} else {// 怒攻
			TriggerParam param = new TriggerParam(data, this.owner);
			Trigger trigger = new Trigger(TriggerPoint.AfterSuperAtk, param);
			this.owner.combatObj.triggerManager.addTrigger(trigger);
		}

		// 目标触发器：被攻击后，被普攻后，被怒攻后，被治疗后，治疗后
		List<FightObject> allTargetList = data.getAllTarget();
		for (FightObject target : allTargetList) {
			// 被攻击后（受到任何攻击包含治疗）
			TriggerParam paramAfterAttacked = new TriggerParam(data, target, this.owner);
			Trigger triggerAfterAttacked = new Trigger(TriggerPoint.AfterAttacked, paramAfterAttacked);
			this.owner.combatObj.triggerManager.addTrigger(triggerAfterAttacked);
			
			// 被普攻后，被怒攻后
			if (this.confSkill.type == SkillType.Common.value()) {// 普攻
				TriggerParam param = new TriggerParam(data, target, this.owner);
				Trigger trigger = new Trigger(TriggerPoint.AfterNormalAtked, param);
				this.owner.combatObj.triggerManager.addTrigger(trigger);
			} else {// 怒攻
				TriggerParam param = new TriggerParam(data, target, this.owner);
				Trigger trigger = new Trigger(TriggerPoint.AfterSuperAtked, param);
				this.owner.combatObj.triggerManager.addTrigger(trigger);
			}
			// 被治疗后，治疗后
			if (this.confSkill.atkType == AtkType.Cure.value()) {
				TriggerParam param = new TriggerParam(data, target, this.owner);
				Trigger trigger = new Trigger(TriggerPoint.AfterCured, param);
				this.owner.combatObj.triggerManager.addTrigger(trigger);
				TriggerParam param1 = new TriggerParam(data, this.owner, target);
				Trigger trigger1 = new Trigger(TriggerPoint.AfterCure, param1);
				this.owner.combatObj.triggerManager.addTrigger(trigger1);
			} else {
				// 伤害类型攻击后
				TriggerParam param = new TriggerParam(data, this.owner);
				Trigger trigger = new Trigger(TriggerPoint.AfterDamageAttack, param);
				this.owner.combatObj.triggerManager.addTrigger(trigger);
				// 被伤类型害攻击后
				TriggerParam param1 = new TriggerParam(data, target, this.owner);
				Trigger trigger1 = new Trigger(TriggerPoint.AfterDamageAttacked, param1);
				this.owner.combatObj.triggerManager.addTrigger(trigger1);
			}
		}

		List<SkillTargetData> targetDataList = data.getAllTargetData();
		List<FightObject> targetList = new ArrayList<>();
		for (SkillTargetData targetData : targetDataList) {
			if (targetList.contains(targetData.target)) {
				continue;
			}
			int damage = targetData.getDamage();
			if (damage <= 0) {
				continue;
			}
			targetList.add(targetData.target);
			TriggerParam param = new TriggerParam(data, targetData.target, this.owner);
			Trigger trigger = new Trigger(TriggerPoint.DecreaseHp, param);
			this.owner.combatObj.triggerManager.addTrigger(trigger);
		}
		targetList.clear();
		for (SkillTargetData targetData : targetDataList) {
			if (targetList.contains(targetData.target)) {
				continue;
			}
			int damage = targetData.getDamage();
			if (damage >= 0) {
				continue;
			}
			targetList.add(targetData.target);
			TriggerParam param = new TriggerParam(data, targetData.target, this.owner);
			Trigger trigger = new Trigger(TriggerPoint.InCreaseHp, param);
			this.owner.combatObj.triggerManager.addTrigger(trigger);
		}
		targetList.clear();
		List<FightObject> dodgeTargetList = new ArrayList<>();
		List<FightObject> blockTargetList = new ArrayList<>();
		List<FightObject> critTargetList = new ArrayList<>();
		for (SkillTargetData targetData : targetDataList) {
			if (!dodgeTargetList.contains(targetData.target) && targetData.isDodge()) {
				dodgeTargetList.add(targetData.target);
			}
			if (!blockTargetList.contains(targetData.target) && targetData.isBlock()) {
				blockTargetList.add(targetData.target);
			}
			if (!critTargetList.contains(targetData.target) && targetData.isCrit()) {
				critTargetList.add(targetData.target);
			}
		}
		for (FightObject target : allTargetList) {
			if (dodgeTargetList.contains(target)) {
				TriggerParam param = new TriggerParam(data, target, this.owner);
				Trigger trigger = new Trigger(TriggerPoint.Dodge, param);
				this.owner.combatObj.triggerManager.addTrigger(trigger);
				TriggerParam param1 = new TriggerParam(data, this.owner, target);
				Trigger trigger1 = new Trigger(TriggerPoint.Dodged, param1);
				this.owner.combatObj.triggerManager.addTrigger(trigger1);
			} else {
				TriggerParam param = new TriggerParam(data, target, this.owner);
				Trigger trigger = new Trigger(TriggerPoint.Hited, param);
				this.owner.combatObj.triggerManager.addTrigger(trigger);
				TriggerParam param1 = new TriggerParam(data, this.owner, target);
				Trigger trigger1 = new Trigger(TriggerPoint.Hit, param1);
				this.owner.combatObj.triggerManager.addTrigger(trigger1);
			}
			if (blockTargetList.contains(target)) {
				TriggerParam param = new TriggerParam(data, target, this.owner);
				Trigger trigger = new Trigger(TriggerPoint.Block, param);
				this.owner.combatObj.triggerManager.addTrigger(trigger);
				TriggerParam param1 = new TriggerParam(data, this.owner, target);
				Trigger trigger1 = new Trigger(TriggerPoint.Blocked, param1);
				this.owner.combatObj.triggerManager.addTrigger(trigger1);
			}
			if (critTargetList.contains(target)) {
				TriggerParam param = new TriggerParam(data, target, this.owner);
				Trigger trigger = new Trigger(TriggerPoint.Crited, param);
				this.owner.combatObj.triggerManager.addTrigger(trigger);
				TriggerParam param1 = new TriggerParam(data, this.owner, target);
				Trigger trigger1 = new Trigger(TriggerPoint.Crit, param1);
				this.owner.combatObj.triggerManager.addTrigger(trigger1);
			}
		}

		// 跳过战斗，不触发等待事件
		if (this.owner.combatObj.isQuickFight) {
			return;
		}
		if (confSkill.stiffTime > 0) {
			// long lastTime = (long)
			// (confSkill.stiffTime/(double)this.owner.combatObj.speedTimes);
			long lastTime = (long) (confSkill.stiffTime);
			this.owner.combatObj.addStageEvent(new EventCastSkill(lastTime));
		}
		Log.fight.debug("使用主动技能：fightObjPos={},sn={},stageSn={}", this.owner.getFightPos(), this.sn,
				this.owner.combatObj.stageSn);
		this.owner.combatObj.sendCastSkillInfoMsg(data);
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("sn", sn).toString();
	}

	/**
	 * 生成消息
	 * 
	 * @return
	 */
	public DTurnbasedObjectSkill createMsg() {
		DTurnbasedObjectSkill.Builder msg = DTurnbasedObjectSkill.newBuilder();
		msg.setSn(sn);
		msg.setLv(lv);//FIXME SJH1027
		return msg.build();
	}

	/**
	 * 对目标属性
	 * 
	 * @param target
	 * @param prop
	 */
	public void checkTargetProp(FightObject target, Prop prop, SkillCastData skillData) {
		List<Integer> list = this.effectMap.get(TriggerPoint.ImTargetProp.value());
		if (list != null) {
			SkillCastData data = new SkillCastData(this, this.owner);
			SkillEffect.doPassiveSkillProp(data, list, target, prop);
		}
		if (skillData == null) {
			return;
		}

		if (skillData.skill.confSkill.type == SkillType.Common.value()) {
			list = this.effectMap.get(TriggerPoint.ImNormalAtkToTargetProp.value());
			if (list != null) {
				SkillCastData data = new SkillCastData(this, this.owner);
				SkillEffect.doPassiveSkillProp(data, list, target, prop);
			}
		} else {
			list = this.effectMap.get(TriggerPoint.ImSuperAtkToTargetProp.value());
			if (list != null) {
				SkillCastData data = new SkillCastData(this, this.owner);
				SkillEffect.doPassiveSkillProp(data, list, target, prop);
			}
		}
		if (skillData.skill.confSkill.atkType == AtkType.Cure.value()) {
			list = this.effectMap.get(TriggerPoint.ImCureTargetProp.value());
			if (list != null) {
				SkillCastData data = new SkillCastData(this, this.owner);
				SkillEffect.doPassiveSkillProp(data, list, target, prop);
			}
		}
	}

	/**
	 * 对攻击者属性
	 * 
	 * @param attacker
	 * @param prop
	 */
	public void checkAttackerProp(FightObject attacker, Prop prop, SkillCastData skillData) {
		// 对攻击者属性影响
		List<Integer> list = this.effectMap.get(TriggerPoint.ImFightProp.value());
		if (list != null) {
			SkillCastData data = new SkillCastData(this, this.owner);
			SkillEffect.doPassiveSkillProp(data, list, attacker, prop);
		}
		if (skillData == null) {
			return;
		}

		if (skillData.skill.confSkill.type == SkillType.Common.value()) {
			// 普攻对攻击者属性影响
			list = this.effectMap.get(TriggerPoint.ImNormalAtkToFightProp.value());
			if (list != null) {
				SkillCastData data = new SkillCastData(this, this.owner);
				SkillEffect.doPassiveSkillProp(data, list, attacker, prop);
			}
		} else {
			// 怒攻普攻对攻击者属性影响
			list = this.effectMap.get(TriggerPoint.ImSuperAtkToFightProp.value());
			if (list != null) {
				SkillCastData data = new SkillCastData(this, this.owner);
				SkillEffect.doPassiveSkillProp(data, list, attacker, prop);
			}
		}
		// 治疗对攻击者属性影响
		if (skillData.skill.confSkill.atkType == AtkType.Cure.value()) {
			list = this.effectMap.get(TriggerPoint.ImCureFightProp.value());
			if (list != null) {
				SkillCastData data = new SkillCastData(this, this.owner);
				SkillEffect.doPassiveSkillProp(data, list, attacker, prop);
			}
		}
	}

	/**
	 * 销毁处理
	 */
	public void killed() {
		for (TriggerListen listen : this.listenList) {
			this.owner.combatObj.triggerManager.delListen(listen);
		}
		this.listenList.clear();
	}

}
