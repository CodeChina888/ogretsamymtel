package game.turnbasedsrv.buff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.effectAction.EffectActionManager;
import game.turnbasedsrv.enumType.AtkType;
import game.turnbasedsrv.enumType.SkillType;
import game.turnbasedsrv.enumType.TriggerPoint;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.param.ProcessParam;
import game.turnbasedsrv.param.SourceParam;
import game.turnbasedsrv.prop.Prop;
import game.turnbasedsrv.prop.PropManager;
import game.turnbasedsrv.skill.Skill;
import game.turnbasedsrv.skill.SkillCastData;
import game.turnbasedsrv.skill.SkillTargetData;
import game.turnbasedsrv.support.GlobalConfVal;
import game.turnbasedsrv.trigger.TriggerListen;
import game.turnbasedsrv.value.ValueBase;
import game.turnbasedsrv.value.ValueFactory;
import game.turnbasedsrv.value.ValueIntSet;
import game.worldsrv.config.ConfSkillBuff;
import game.worldsrv.enumType.FightPropName;
import game.worldsrv.support.Utils;

public class BuffCollection {
	/** 拥有者 **/
	FightObject owner;
	/** 当前的监听队列 **/
	Map<TriggerPoint, List<TriggerListen>> mapListenList = new HashMap<>();
	/** 新一轮的监听队列 **/
	Map<TriggerPoint, List<TriggerListen>> queueMapListenList = new HashMap<>();
	/** 正在执行的监听队列 **/
	List<TriggerListen> excuteListenList = new ArrayList<>();
	/** buff列表 **/
	List<Buff> buffList = new ArrayList<>();
	/** 失效，被驱散净化的buff列表 **/
	List<Buff> invalidBuffList = new ArrayList<>();

	/**
	 * 构造函数
	 * 
	 * @param owner
	 */
	public BuffCollection(FightObject owner) {
		this.owner = owner;
	}

	/**
	 * 是否可以施法
	 * 
	 * @param skill
	 * @return
	 */
	public boolean canCastSkill(Skill skill) {
		// 定身，不可普攻，不可技能
		ValueBase valueStun = PropManager.inst().getStun(this.owner);
		if (valueStun.isBooleanValue() && valueStun.getBooleanValue()) {
			return false;
		}

		if (skill.confSkill.type == SkillType.Common.value()) {
			// 麻痹，不可普攻
			ValueBase valueParalytic = PropManager.inst().getParalytic(this.owner);
			if (valueParalytic.isBooleanValue() && valueParalytic.getBooleanValue()) {
				return false;
			}
		} else if (skill.confSkill.type == SkillType.Special.value() 
					|| skill.confSkill.type == SkillType.Rage.value() 
					|| skill.confSkill.type == SkillType.Joint.value()) {
			// 封技，不可爆点/怒攻/合击
			ValueBase valueSilent = PropManager.inst().getSilent(this.owner);
			if (valueSilent.isBooleanValue() && valueSilent.getBooleanValue()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否可以加血
	 * 
	 * @return
	 */
	public boolean canAddHp() {
		ValueBase value = PropManager.inst().getBanHeal(this.owner);
		if (value.isBooleanValue() && value.getBooleanValue()) {
			return false;
		}
		return true;
	}

	/**
	 * 是否可以加怒气
	 * 
	 * @return
	 */
	public boolean canAddRage() {
		ValueBase value = PropManager.inst().getBanRage(this.owner);
		if (value.isBooleanValue() && value.getBooleanValue()) {
			return false;
		}
		return true;
	}

	/**
	 * 增加buff
	 * 
	 * @param source
	 * @param process
	 * @param buffId
	 * @param buffLevel
	 * @param creator
	 * @return
	 */
	public Buff addBuff(SourceParam source, ProcessParam process, int buffId, int buffLevel, FightObject creator) {
		ConfSkillBuff confBuff = ConfSkillBuff.get(buffId);
		if (confBuff == null) {
			return null;
		}
		Prop attackerProp = process.attackerProp;
		Prop targetProp = process.targetProp;
		if (attackerProp == null || targetProp == null) {
			return null;
		}
		// 免疫
		ValueBase valueBase = targetProp.getOnePropValue(FightPropName.ImmuneBuffGroup.value());
		if(valueBase instanceof ValueIntSet){
			ValueIntSet setValue = (ValueIntSet)valueBase;
			if(GlobalConfVal.isBuffBelongToGroupCollection(confBuff.type, setValue.value)){
				EffectActionManager.inst().addTargetDataState(source, process, this.owner, SkillTargetData.StateImmune);
				return null;//免疫
			}
		}
		// 叠加类型
		int type = confBuff.type;
		Buff sameTypeBuff = null;
		for (Buff buff : buffList) {
			if (buff.confBuff.type == type) {
				sameTypeBuff = buff;
				break;
			}
		}
		if (sameTypeBuff != null) {
			switch (confBuff.multiType) {
			case 0:// 不可叠加，高级别覆盖低级别
				if (confBuff.priority <= sameTypeBuff.confBuff.priority) {
					return null;
				}
				this.removeBuff(sameTypeBuff, true);
				break;
			case 1:// 回合叠加
				int buffRound = Math.min(sameTypeBuff.round + confBuff.round, confBuff.multiMax);
				if (buffRound < 1) {
					buffRound = 1;
				}
				sameTypeBuff.round = buffRound;
				return sameTypeBuff;
			case 2:// 层数叠加
				int buffTimes = Math.min(sameTypeBuff.times + 1, confBuff.multiMax);
				if (buffTimes < 1) {
					buffTimes = 1;
				}				
				sameTypeBuff.times = buffTimes;
				// 回合数重置
				if(sameTypeBuff.round<confBuff.round){
					sameTypeBuff.round = confBuff.round;
				}
				// 重算基础属性相关
				sameTypeBuff.buffAddPropTimes();
				return sameTypeBuff;
			}
		}

		// 增加buff
		SkillCastData skillData = EffectActionManager.inst().getSkillCastData(source, process);
		CombatObject stageObj = source.combatObj;
		int fightId = stageObj.getFightId();
		Buff buff = new Buff(this.owner, buffId, fightId, creator, attackerProp, targetProp, skillData);
		this.buffList.add(buff);
		buff.start();
		return buff;
	}

	/**
	 * 护盾处理
	 * 
	 * @param source
	 * @param process
	 * @param hurt
	 * @return
	 */
	public int hurtShield(SourceParam source, ProcessParam process, int hurt) {
		if (buffList.isEmpty()) {
			return hurt;
		}
		Prop targetProp = process.targetProp;
		SkillCastData skillData = EffectActionManager.inst().getSkillCastData(source, process);
		int damage = hurt;
		String shieldPropName;
		// 防御
		int def = targetProp.getDef(skillData.skill.confSkill.atkType); 
		if (skillData.skill.confSkill.atkType == AtkType.Mag.value()) {
			shieldPropName = FightPropName.ShieldMag.value();
		} else {
			shieldPropName = FightPropName.ShieldPhy.value();
		}
		List<Buff> removeList = new ArrayList<>();
		for (int i = 0; i < buffList.size(); i++) {
			Buff buff = buffList.get(i);
			ValueBase value = buff.prop.getOnePropValue(FightPropName.Shield.value());
			int shield = (int) value.getNumberValue();
			boolean hasShield = false;
			if (shield > 0) {
				hasShield = true;
				float useShield = def * shield / Utils.F10000;
				useShield = Math.min(damage, useShield);
				damage = damage - (int)useShield;
				int factShield = (int) (useShield * Utils.I10000 / def);
				shield = shield - factShield;
				ValueBase newValue = value.getCopy();
				ValueBase leftValue = ValueFactory.getFightValueByParam(shield);
				newValue.setValue(leftValue);
				buff.prop.setPropValue(FightPropName.Shield.value(), newValue);
				if (shield <= 0) {
					removeList.add(buff);
				}
			}
			if (damage > 0) {
				ValueBase valueShieldType = buff.prop.getOnePropValue(shieldPropName);
				int shieldType = (int) valueShieldType.getNumberValue();
				if (shieldType > 0) {
					hasShield = true;
					int useShield = Math.min(damage, shieldType);
					damage = damage - useShield;
					shieldType = shieldType - useShield;
					ValueBase newValue = value.getCopy();
					ValueBase leftValue = ValueFactory.getFightValueByParam(shieldType);
					newValue.setValue(leftValue);
					buff.prop.setPropValue(shieldPropName, newValue);
					if (shieldType <= 0) {
						removeList.add(buff);
					}
				}
			} else {
				if (hasShield && shield <= 0) {
					removeList.add(buff);
				}
			}
			if (damage <= 0) {
				break;
			}
		}
		// 添加失效buff
		excuteInvalidBuff(removeList, false);
		return damage;
	}

	/**
	 * 是否存在buff
	 * 
	 * @param fightId
	 * @return
	 */
	public boolean hasBuff(long fightId) {
		for (Buff buff : buffList) {
			if (buff.fightId == fightId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 根据buff group获取buff
	 * 
	 * @return
	 */
	public List<Buff> getBuffByGroup(List<Integer> groups) {
		Set<Integer> types = GlobalConfVal.getBuffGroupTypes(groups);
		List<Buff> result = new ArrayList<>();
		for (Buff buff : buffList) {
			if (types.contains(buff.confBuff.type)) {
				result.add(buff);
			}
		}
		return result;
	}
	/**
	 * 根据类型获取buff
	 * 
	 * @param types
	 * @return
	 */
	public List<Buff> getBuffByType(List<Integer> types) {
		List<Buff> result = new ArrayList<>();
		for (Buff buff : buffList) {
			if (types.contains(buff.confBuff.type)) {
				result.add(buff);
			}
		}
		return result;
	}

	/**
	 * 根据sn获取buff
	 * 
	 * @param snList
	 * @return
	 */
	public List<Buff> getBuffBySn(List<Integer> snList) {
		List<Buff> result = new ArrayList<>();
		for (Buff buff : buffList) {
			if (snList.contains(buff.sn)) {
				result.add(buff);
			}
		}
		return result;
	}

	/**
	 * 移除buff
	 * 
	 * @param fightId
	 * @param bForce
	 */
	public void removeBuff(long fightId, boolean bForce) {
		for (Buff buff : buffList) {
			if (buff.fightId == fightId) {
				buffList.remove(buff);
				return;
			}
		}
	}

	/**
	 * 移除buff
	 * 
	 * @param bForce
	 */
	public void removeBuff(Buff buff, boolean bForce) {
		if (buffList.contains(buff)) {
			buffList.remove(buff);
		}
	}

	/**
	 * 被销毁时的处理
	 */
	public void killed() {
		buffList.clear();
	}

	/**
	 * 返回所有buff列表，
	 * 
	 * @return newlist
	 */
	public List<Buff> getAllBuff() {
		List<Buff> result = new ArrayList<>();
		result.addAll(buffList);
		return result;
	}

	/**
	 * 返回所有buff列表
	 * 
	 * @return bufflist
	 */
	public List<Buff> getBuffList() {
		return buffList;
	}
	
	/**
	 * 处理失效，驱散，净化的buff
	 * @param bForce
	 */
	public void excuteInvalidBuff(List<Buff> invalidBuffs, boolean bForce) {
		// 添加至列表中，出手下发给客户端
		invalidBuffList.addAll(invalidBuffs);
		for (Buff buff : invalidBuffs) {
			removeBuff(buff, bForce);
		}
	}
	
	/**
	 * 返回驱散净化和失效的buff
	 * @return newList
	 */
	public List<Buff> getAllInvalidBuff() {
		List<Buff> result = new ArrayList<>();
		result.addAll(invalidBuffList);
		// 清空列表
		invalidBuffList.clear();
		return result;
	}
}
