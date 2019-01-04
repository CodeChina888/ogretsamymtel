package game.turnbasedsrv.buff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.DTurnbasedBuff;
import game.turnbasedsrv.enumType.AtkType;
import game.turnbasedsrv.enumType.SkillType;
import game.turnbasedsrv.enumType.TriggerPoint;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.Prop;
import game.turnbasedsrv.skill.SkillCastData;
import game.turnbasedsrv.skill.SkillEffect;
import game.turnbasedsrv.trigger.Trigger;
import game.turnbasedsrv.trigger.TriggerListen;
import game.turnbasedsrv.value.ValueBase;
import game.turnbasedsrv.value.ValueDefine;
import game.turnbasedsrv.value.ValueFactory;
import game.worldsrv.config.ConfSkillBuff;
import game.worldsrv.config.ConfSkillEffect;
import game.worldsrv.enumType.FightPropName;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

public class Buff {
	/** 配置sn **/
	public int sn;
	/** 配置 **/
	ConfSkillBuff confBuff;
	/** 战斗分配的ID **/
	public int fightId;
	/** 修改的属性值 **/
	public Prop prop = new Prop();
	/** 创建者的瞬时属性 **/
	public Prop fireProp;
	/** 目标的瞬时属性 **/
	public Prop targetProp;
	/** 创建者 **/
	public FightObject fireObj;
	/** 创建的技能数据 **/
	public SkillCastData skillData;
	/** 叠加层数 **/
	public int times;
	/** 目标 **/
	public FightObject owner;
	/** 监听列表 **/
	public List<TriggerListen> listenList = new ArrayList<>();
	/** 触发效果分组<triggerType, effectList> **/
	public Map<Integer, List<Integer>> effectMap = new HashMap<>();

	/** 持续回合数，小于0为永久 **/
	public int round = 1;

	/** 是否回合开始时减少回合数 **/
	public boolean isRoundCheck = false;
	/** 是否出手后触发效果 **/
	public boolean isOrderCheck = false;

	/**
	 * 构造函数
	 * @param owner
	 * @param sn
	 * @param fightId
	 * @param fireObj
	 * @param fireProp
	 */
	public Buff(FightObject owner, int sn, int fightId, FightObject fireObj, Prop fireProp, Prop targetProp,
			SkillCastData skillData) {
		this.owner = owner;
		this.sn = sn;
		this.confBuff = ConfSkillBuff.get(sn);
		if (confBuff == null) {
			Log.logTableError(ConfSkillBuff.class,"no find sn ={}", sn);
		}
		this.fightId = fightId;
		this.fireObj = fireObj;
		this.fireProp = fireProp;
		this.targetProp = targetProp;
		this.skillData = skillData;
		this.round = this.confBuff.round;
		this.times = 1;
		init();
	}

	/**
	 * 是否回合开始时减少回合数
	 * 
	 * @return
	 */
	public boolean isRoundCheckBuff() {
		return this.isRoundCheck;
	}

	/**
	 * 是否出手后触发效果
	 * 
	 * @return
	 */
	public boolean isOrderCheckBuff() {
		return this.isOrderCheck;
	}

	/**
	 * 初始化
	 */
	private void init() {
		if (this.confBuff == null) {
			return;
		}
		//免疫
		if(this.confBuff.immuneGroup!=null && this.confBuff.immuneGroup.length>0){
			String value = Utils.arrayIntToStr(this.confBuff.immuneGroup);
			ValueBase immuneValue = ValueFactory.getFightValueByType(ValueDefine.ValueIntSet, value);
			this.prop.setPropValue(FightPropName.ImmuneBuffGroup.value(), immuneValue);
		}
		//效果
		if (this.confBuff.triggerEffects == null || this.confBuff.triggerEffects.length < 1) {
			// 没有触发效果,且回合数大于0
			if (this.confBuff.round > 0) {
				this.isRoundCheck = true;
			}
			return;
		}
		for (int i = 0; i < this.confBuff.triggerEffects.length; i++) {
			int effectSn = this.confBuff.triggerEffects[i];
			ConfSkillEffect confEffect = ConfSkillEffect.get(effectSn);
			if (confEffect == null) {
				Log.logTableError(ConfSkillEffect.class,"no effect sn ={}", effectSn);
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
		if (this.effectMap.get(0) != null) {
			this.isOrderCheck = true;
		} else {
			this.isRoundCheck = true;
		}
	}

	/**
	 * 转为字符串显示
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("sn", sn).toString();
	}

	/**
	 * 启动
	 */
	public void start() {
		// buff加成属性
		buffAddPropTimes();
		
		// 设置被动监听
		for (Map.Entry<Integer, List<Integer>> entry : this.effectMap.entrySet()) {
			int type = entry.getKey();
			if (type <= 0) {
				continue;
			}
			TriggerPoint triggerPoint = TriggerPoint.getByValue(type);
			if (triggerPoint == null) {
				Log.logTableError(ConfSkillEffect.class,"no triggerType sn ={}", entry.getValue());
				continue;
			}
			if (triggerPoint.isDirectTrigger()) {
				continue;
			}
			TriggerListen listen = new TriggerListen(triggerPoint, this.owner.idFight, this::passiveTrigger);
			this.owner.combatObj.triggerManager.addListen(listen);
			this.listenList.add(listen);
		}
		
		// TODO 立即触发
//		List<Integer> list = this.effectMap.get(TriggerPoint.StartBuff.value());
//		if (list != null) {
//			passiveTrigger(new Trigger(TriggerPoint.StartBuff, new TriggerParam(this.owner, this.owner)));
//			this.round--;
//		}
	}
	
	/**
	 * 设置buff加成的属性
	 */
	public void buffAddPropTimes() {
		if (this.confBuff != null && this.confBuff.prop != null && this.confBuff.propValue != null
				&& this.confBuff.prop.length == this.confBuff.propValue.length) {
			// 设置属性
			for (int i = 0; i < this.confBuff.prop.length; i++) {
				String propName = this.confBuff.prop[i];
				String propValue = this.confBuff.propValue[i];
				ValueBase value = this.prop.getOnePropValue(propName);
				if (value != null) {
					ValueBase newValue = value.getCopy();
					ValueBase timesValue = ValueFactory.getFightValueByParam(this.times);
					newValue.setValue(propValue);
					newValue.multiply(timesValue);
					this.prop.setPropValue(propName, newValue);
				}
			}
		}
	}
	

	/**
	 * 被动触发效果
	 */
	public void passiveTrigger(Trigger trigger) {
		TriggerPoint triggerType = trigger.triggerPoint;
		List<Integer> list = this.effectMap.get(triggerType.value());
		if (list == null) {
			return;
		}
		BuffTriggerData data = new BuffTriggerData(this);
		boolean isTrigger = SkillEffect.doBuffPassiveEffect(trigger, data, list);
		if (!isTrigger) {
			return;
		}
		// 跳过战斗，不发消息
		if (owner.combatObj.isQuickFight) {
			return;
		}
		Log.logTest(Log.fight,"触发buff被动：fightObjPos={},sn={}", this.owner.getFightPos(), this.sn);
		owner.combatObj.sendBuffTriggerInfoMsg(data, false);
	}

	/**
	 * 被移除时的处理
	 */
	public void removed() {
		for (TriggerListen listen : this.listenList) {
			this.owner.combatObj.triggerManager.delListen(listen);
		}
		this.listenList.clear();
	}

	/**
	 * 被销毁的处理
	 */
	public void killed() {
		for (TriggerListen listen : this.listenList) {
			this.owner.combatObj.triggerManager.delListen(listen);
		}
		this.listenList.clear();
	}

	/**
	 * 出手触发
	 */
	public void orderTrigger() {
		List<Integer> list = this.effectMap.get(0);
		if (list == null) {
			return;
		}
		BuffTriggerData data = new BuffTriggerData(this);
		SkillEffect.doBuffEffect(data, list);
		// 跳过战斗，不发消息
		if (owner.combatObj.isQuickFight) {
			return;
		}
		Log.logTest(Log.fight,"触发buff：fightObjPos={},sn={}", this.owner.getFightPos(), this.sn);
		owner.combatObj.sendBuffTriggerInfoMsg(data, false);
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
			BuffTriggerData data = new BuffTriggerData(this);
			SkillEffect.doBuffProp(data, list, target, prop);
		}
		if (skillData == null) {
			return;
		}

		if (skillData.skill.confSkill.type == SkillType.Common.value()) {
			list = this.effectMap.get(TriggerPoint.ImNormalAtkToTargetProp.value());
			if (list != null) {
				BuffTriggerData data = new BuffTriggerData(this);
				SkillEffect.doBuffProp(data, list, target, prop);
			}
		} else {
			list = this.effectMap.get(TriggerPoint.ImSuperAtkToTargetProp.value());
			if (list != null) {
				BuffTriggerData data = new BuffTriggerData(this);
				SkillEffect.doBuffProp(data, list, target, prop);
			}
		}
		if (skillData.skill.confSkill.atkType == AtkType.Cure.value()) {
			list = this.effectMap.get(TriggerPoint.ImCureTargetProp.value());
			if (list != null) {
				BuffTriggerData data = new BuffTriggerData(this);
				SkillEffect.doBuffProp(data, list, target, prop);
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
		if (skillData == null) {
			return;
		}
		List<Integer> list = this.effectMap.get(TriggerPoint.ImFightProp.value());
		if (list != null) {
			BuffTriggerData data = new BuffTriggerData(this);
			SkillEffect.doBuffProp(data, list, attacker, prop);
		}

		if (skillData.skill.confSkill.type == SkillType.Common.value()) {
			list = this.effectMap.get(TriggerPoint.ImNormalAtkToFightProp.value());
			if (list != null) {
				BuffTriggerData data = new BuffTriggerData(this);
				SkillEffect.doBuffProp(data, list, attacker, prop);
			}
		} else {
			list = this.effectMap.get(TriggerPoint.ImSuperAtkToFightProp.value());
			if (list != null) {
				BuffTriggerData data = new BuffTriggerData(this);
				SkillEffect.doBuffProp(data, list, attacker, prop);
			}
		}
		if (skillData.skill.confSkill.atkType == AtkType.Cure.value()) {
			list = this.effectMap.get(TriggerPoint.ImCureFightProp.value());
			if (list != null) {
				BuffTriggerData data = new BuffTriggerData(this);
				SkillEffect.doBuffProp(data, list, attacker, prop);
			}
		}
	}

	/**
	 * buff消息
	 * 
	 * @return
	 */
	public DTurnbasedBuff creageMsg() {
		DTurnbasedBuff.Builder msg = DTurnbasedBuff.newBuilder();
		msg.setId(fightId);
		msg.setSn(sn);
		msg.setOwnerId(owner.idFight);
		msg.setRoundLeft(round);
		msg.setTriggleSkillSn(skillData.skill.sn);// 触发buff的技能sn
		return msg.build();
	}
}
