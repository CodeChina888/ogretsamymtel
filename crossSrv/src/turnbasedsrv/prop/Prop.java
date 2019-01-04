package turnbasedsrv.prop;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.DTurnbasedObjectProp;
import game.worldsrv.enumType.FightPropName;
import turnbasedsrv.buff.Buff;
import turnbasedsrv.enumType.AtkType;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.skill.Skill;
import turnbasedsrv.skill.SkillCastData;
import turnbasedsrv.support.GlobalConfVal;
import turnbasedsrv.value.ValueBase;
import turnbasedsrv.value.ValueFactory;

/**
 * 战斗中属性处理类
 */
public class Prop {

	// 属性值表<属性名propName, 属性值FightValueBase>
	private Map<String, ValueBase> mapPropValue = new HashMap<>();

	/**
	 * 构造函数
	 */
	public Prop() {

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
	 * 生成消息
	 * 
	 * @return
	 */
	public DTurnbasedObjectProp createMsg(FightObject fightObj) {
		DTurnbasedObjectProp.Builder msg = DTurnbasedObjectProp.newBuilder();
		msg.setHpCur(PropManager.inst().getCurHp(fightObj));
		msg.setHpMax(PropManager.inst().getMaxHp(fightObj));
		msg.setRageCur(PropManager.inst().getCurRage(fightObj));
		msg.setRageMax(PropManager.inst().getMaxRage(fightObj));
		return msg.build();
	}

	/**
	 * 返回所有属性值
	 * 
	 * @return
	 */
	public Map<String, ValueBase> getAllPropValue() {
		return mapPropValue;
	}

	/**
	 * 获取指定属性的基础值，该值是不包含BUFF加成的值(不存在的属性，返回该属性的零值，方便计算)
	 * 
	 * @param propName
	 * @return
	 */
	public ValueBase getOnePropValue(String propName) {
		ValueBase value = mapPropValue.get(propName);
		if (null == value) {
			// 不存在的属性，返回该属性的零值，方便计算
			value = ValueFactory.getZeroValueByName(propName);
			if (value != null) {
				mapPropValue.put(propName, value);
			}
			return value;
		} else {
			return value;
		}
	}

	/**
	 * 设值
	 * 
	 * @param propName
	 * @param value
	 */
	public void setPropValue(String propName, ValueBase value) {
		if (GlobalConfVal.isPropValid(propName)) {
			if (this.mapPropValue.containsKey(propName)) {
				this.mapPropValue.remove(propName);
			}
			this.mapPropValue.put(propName, value);
		}
	}

	/**
	 * 加上指定值
	 * 
	 * @param propName
	 * @param value
	 */
	public void addPropValue(String propName, ValueBase value) {
		ValueBase propValue = this.mapPropValue.get(propName);
		if (propValue != null) {// 存在，则相加
			propValue.add(value);
		} else {// 不存在，则设置值
			setPropValue(propName, value.getCopy());
		}
	}

	/**
	 * 乘以指定值
	 * 
	 * @param propName
	 * @param value
	 */
	public void multiplyPropValue(String propName, ValueBase value) {
		ValueBase propValue = this.mapPropValue.get(propName);
		if (propValue != null) {// 存在，则相乘
			propValue.multiply(value);
		}
	}

	/**
	 * 清空指定属性的值
	 */
	public void clearOnePropValue(String propName) {
		if (this.mapPropValue.containsKey(propName)) {
			this.mapPropValue.remove(propName);
		}
	}

	/**
	 * 清空所有属性值
	 */
	public void clearAllPropValue() {
		mapPropValue.clear();
	}

	/**
	 * 获取战斗者对目标属性拷贝
	 * 
	 * @param target
	 * @return
	 */
	public Prop getCheckTargetPropCopy(FightObject fightObj, FightObject target, SkillCastData skillData) {
		Prop copyProp = new Prop();
		for (Entry<String, ValueBase> entry : mapPropValue.entrySet()) {
			String propName = entry.getKey();
			ValueBase value = entry.getValue().getCopy();
			copyProp.mapPropValue.put(propName, value);
		}
		Prop prop = new Prop();
		for (Buff buff : fightObj.buffManager.getAllBuff()) {
			buff.checkTargetProp(target, prop, skillData);
		}
		for (Skill skill : fightObj.passiveSkillList) {
			skill.checkTargetProp(target, prop, skillData);
		}
		if (skillData != null && !skillData.skill.isPassiveSkill()) {
			skillData.skill.checkTargetProp(target, prop, skillData);
		}
		for (Entry<String, ValueBase> entry : prop.mapPropValue.entrySet()) {
			String propName = entry.getKey();
			ValueBase value = entry.getValue().getCopy();
			copyProp.addPropValue(propName, value);
		}
		return copyProp;
	}

	/**
	 * 获取物理攻击力
	 * 
	 * @return
	 */
	public int getAtkPhy() {
		ValueBase atk = getOnePropValue(FightPropName.Atk.value());
		ValueBase atkPct = getOnePropValue(FightPropName.AtkPct.value());
		ValueBase atkEx = getOnePropValue(FightPropName.AtkEx.value());
		ValueBase atkPhy = getOnePropValue(FightPropName.AtkPhy.value());
		ValueBase atkPhyPct = getOnePropValue(FightPropName.AtkPhyPct.value());
		ValueBase atkPhyEx = getOnePropValue(FightPropName.AtkPhyEx.value());
		return (int) ((atk.getNumberValue() + atkPhy.getNumberValue())
				* (10000 + atkPct.getNumberValue() + atkPhyPct.getNumberValue()) / 10000 + atkEx.getNumberValue()
				+ atkPhyEx.getNumberValue());
	}

	/**
	 * 获取法术攻击力
	 * 
	 * @return
	 */
	public int getAtkMag() {
		ValueBase atk = getOnePropValue(FightPropName.Atk.value());
		ValueBase atkPct = getOnePropValue(FightPropName.AtkPct.value());
		ValueBase atkEx = getOnePropValue(FightPropName.AtkEx.value());
		ValueBase atkMag = getOnePropValue(FightPropName.AtkMag.value());
		ValueBase atkMagPct = getOnePropValue(FightPropName.AtkMagPct.value());
		ValueBase atkMagEx = getOnePropValue(FightPropName.AtkMagEx.value());
		return (int) ((atk.getNumberValue() + atkMag.getNumberValue())
				* (10000 + atkPct.getNumberValue() + atkMagPct.getNumberValue()) / 10000 + atkEx.getNumberValue()
				+ atkMagEx.getNumberValue());
	}

	/**
	 * 获取攻击力
	 * 
	 * @param atkType
	 * @return
	 */
	public int getAtk(int atkType) {
		if (atkType == AtkType.Mag.value()) {
			return getAtkMag();
		}
		return getAtkPhy();
	}

	/**
	 * 获取无视物理防御值
	 * 
	 * @return
	 */
	public int getPenePhy() {
		ValueBase pene = getOnePropValue(FightPropName.Pene.value());
		ValueBase penePhy = getOnePropValue(FightPropName.PenePhy.value());
		return (int) (pene.getNumberValue() + penePhy.getNumberValue());
	}

	/**
	 * 获取无视法术防御值
	 * 
	 * @return
	 */
	public int getPeneMag() {
		ValueBase pene = getOnePropValue(FightPropName.Pene.value());
		ValueBase peneMag = getOnePropValue(FightPropName.PeneMag.value());
		return (int) (pene.getNumberValue() + peneMag.getNumberValue());
	}

	/**
	 * 获取无视防御值
	 * 
	 * @param atkType
	 * @return
	 */
	public int getPene(int atkType) {
		if (atkType == AtkType.Mag.value()) {
			return getPeneMag();
		}
		return getPenePhy();
	}

	/**
	 * 获取物理防御力
	 * 
	 * @return
	 */
	public int getDefPhy() {
		ValueBase def = getOnePropValue(FightPropName.Def.value());
		ValueBase defPct = getOnePropValue(FightPropName.DefPct.value());
		ValueBase defEx = getOnePropValue(FightPropName.DefEx.value());
		ValueBase defPhy = getOnePropValue(FightPropName.DefPhy.value());
		ValueBase defPhyPct = getOnePropValue(FightPropName.DefPhyPct.value());
		ValueBase defPhyEx = getOnePropValue(FightPropName.DefPhyEx.value());
		return (int) ((def.getNumberValue() + defPhy.getNumberValue())
				* (10000 + defPct.getNumberValue() + defPhyPct.getNumberValue()) / 10000 + defEx.getNumberValue()
				+ defPhyEx.getNumberValue());
	}

	/**
	 * 获取法术防御力
	 * 
	 * @return
	 */
	public int getDefMag() {
		ValueBase def = getOnePropValue(FightPropName.Def.value());
		ValueBase defPct = getOnePropValue(FightPropName.DefPct.value());
		ValueBase defEx = getOnePropValue(FightPropName.DefEx.value());
		ValueBase defMag = getOnePropValue(FightPropName.DefMag.value());
		ValueBase defMagPct = getOnePropValue(FightPropName.DefMagPct.value());
		ValueBase defMagEx = getOnePropValue(FightPropName.DefMagEx.value());
		return (int) ((def.getNumberValue() + defMag.getNumberValue())
				* (10000 + defPct.getNumberValue() + defMagPct.getNumberValue()) / 10000 + defEx.getNumberValue()
				+ defMagEx.getNumberValue());
	}

	/**
	 * 获取防御力
	 * 
	 * @param atkType
	 * @return
	 */
	public int getDef(int atkType) {
		if (atkType == AtkType.Mag.value()) {
			return getDefMag();
		}
		return getDefPhy();
	}

	/**
	 * 是否无敌
	 * 
	 * @return
	 */
	public boolean isInvincible() {
		ValueBase invincible = getOnePropValue(FightPropName.Invincible.value());
		if (invincible != null && invincible.isBooleanValue() && invincible.getBooleanValue()) {
			return true;
		}
		return false;
	}

	/**
	 * 是否技能免疫
	 * 
	 * @param atkType
	 * @return
	 */
	public boolean isSkillImmune(int atkType) {
		if (atkType == AtkType.Mag.value()) {
			ValueBase immune = getOnePropValue(FightPropName.ImmuneMag.value());
			if (immune != null && immune.isBooleanValue() && immune.getBooleanValue()) {
				return true;
			}
			return false;
		}
		ValueBase immune = getOnePropValue(FightPropName.ImmunePhy.value());
		if (immune != null && immune.isBooleanValue() && immune.getBooleanValue()) {
			return true;
		}
		return false;
	}

}
