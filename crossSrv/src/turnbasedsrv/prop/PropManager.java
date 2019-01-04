package turnbasedsrv.prop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.support.ManagerBase;
import core.support.Utils;
import crosssrv.support.Log;
import game.worldsrv.enumType.FightPropName;
import turnbasedsrv.buff.Buff;
import turnbasedsrv.enumType.AtkType;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.param.ProcessParam;
import turnbasedsrv.param.SourceParam;
import turnbasedsrv.skill.Skill;
import turnbasedsrv.skill.SkillCastData;
import turnbasedsrv.stage.FightStageObject;
import turnbasedsrv.support.GlobalConfVal;
import turnbasedsrv.trigger.Trigger;
import turnbasedsrv.value.ValueBase;
import turnbasedsrv.value.ValueFactory;
import turnbasedsrv.value.ValueInt;

/**
 * 战斗属性管理类
 */
public class PropManager extends ManagerBase {

	public final static String HpPct = "HpPct";// 血量万分比值

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static PropManager inst() {
		return inst(PropManager.class);
	}

	/**
	 * 获取指定属性的最终值，该值是包含BUFF等加成的值
	 * 
	 * @param propName
	 * @param fightObj
	 * @return
	 */
	public ValueBase getFinalPropValue(String propName, FightObject fightObj) {
		// 特殊处理HpPct即血量万分比值
		if (propName.equals(PropManager.HpPct)) {
			return new ValueInt(getHpPct(fightObj));
		}

		// 处理指定属性值
		ValueBase propValue = GlobalConfVal.getPropValueBase(propName);
		if (propValue == null) {
			Log.fight.error("===error propName={}", propName);
			return null;
		}
		FightPropName type = FightPropName.get(propName);
		switch (type) {
		case HpCur: {// 当前生命值
			propValue.addInt(getCurHp(fightObj));
		}
			break;
		case HpMax: {// 最大生命值
			propValue.addInt(getMaxHp(fightObj));
		}
			break;
		case RageCur: {// 当前怒气值
			propValue.addInt(getCurRage(fightObj));
		}
			break;
		case RageMax: {// 最大怒气值
			propValue.addInt(getMaxRage(fightObj));
		}
			break;
		case Atk: {// 基础攻击值
			propValue.addInt(getAtk(fightObj));
		}
			break;
		case AtkPhy: {// 物理攻击值
			propValue.addInt(getAtkPhy(fightObj));
		}
			break;
		case AtkMag: {// 法术攻击值
			propValue.addInt(getAtkMag(fightObj));
		}
			break;
		case Def: {// 基础防御值
			propValue.addInt(getDef(fightObj));
		}
			break;
		case DefPhy: {// 物理防御值
			propValue.addInt(getDefPhy(fightObj));
		}
			break;
		case DefMag: {// 法术防御值
			propValue.addInt(getDefMag(fightObj));
		}
			break;
		default: {// 其他值
			propValue = getOnePropValue(propName, fightObj);
		}
		}
		return propValue;
	}

	/**
	 * 获取指定属性的累加值，该值是包含BUFF加成的值（包括配表默认值计算在内）
	 * 
	 * @param propName
	 * @param props
	 * @return
	 */
	private ValueBase getOnePropValue(String propName, FightObject fightObj) {
		ValueBase propValue = GlobalConfVal.getPropValueBase(propName);
		if (propValue == null) {
			Log.fight.error("===error propName={}", propName);
			return null;
		}

		List<Prop> propList = new ArrayList<>();
		propList.add(fightObj.prop);
		for (Buff buff : fightObj.buffManager.getBuffList()) {
			propList.add(buff.prop);
		}

		for (Prop prop : propList) {
			// 获取指定属性的基础值，该值是不包含BUFF等加成的值
			ValueBase valueZero = ValueFactory.getZeroValueByName(propName);
			ValueBase value = prop.getOnePropValue(propName);
			if (!valueZero.equals(value)) {
				// 累加BUFF加成的值
				propValue.add(value);
			}
		}
		return propValue;
	}

	/**
	 * 获取战斗者属性拷贝
	 * 
	 * @param fightObj
	 * @return
	 */
	public Prop getPropCopy(FightObject fightObj, List<Prop> addPropList) {
		Prop copyProp = new Prop();
		// 获取属性的非0默认值
		for (Map.Entry<String, ValueBase> entry : GlobalConfVal.get_propDefaultValueMap().entrySet()) {
			ValueBase valueDefault = entry.getValue();
			copyProp.setPropValue(entry.getKey(), valueDefault.getCopy());
		}

		// 获取其它属性值
		List<Prop> propList = new ArrayList<>();
		// 加入对象属性
		propList.add(fightObj.prop);
		// 加入buff属性
		for (Buff buff : fightObj.buffManager.getBuffList()) {
			propList.add(buff.prop);
		}
		// 加入额外属性
		if (addPropList != null) {
			for (Prop prop : addPropList) {
				propList.add(prop);
			}
		}
		for (Prop prop : propList) {
			for (Entry<String, ValueBase> entry : prop.getAllPropValue().entrySet()) {
				String propName = entry.getKey();
				ValueBase value = entry.getValue();
				copyProp.addPropValue(propName, value);
			}
		}
		return copyProp;
	}

	/**
	 * 获取战斗者对攻击者属性拷贝
	 * 
	 * @param attacker
	 * @return
	 */
	public Prop getCheckAttackerPropCopy(FightObject fightObj, FightObject attacker, SkillCastData skillData) {
		Prop copyProp = getPropCopy(fightObj, null);
		Prop prop = new Prop();
		for (Buff buff : fightObj.buffManager.getAllBuff()) {
			buff.checkAttackerProp(attacker, prop, skillData);
		}
		for (Skill skill : fightObj.passiveSkillList) {
			skill.checkAttackerProp(attacker, prop, skillData);
		}
		for (Map.Entry<String, ValueBase> entry : prop.getAllPropValue().entrySet()) {
			String propName = entry.getKey();
			ValueBase value = entry.getValue().getCopy();
			copyProp.addPropValue(propName, value);
		}
		return copyProp;
	}

	/**
	 * 获取血量万分比值
	 * 
	 * @param fightObj
	 * @return
	 */
	public int getHpPct(FightObject fightObj) {
		int hp = getCurHp(fightObj);
		int maxHp = getMaxHp(fightObj);
		return (int) (10000.0d * hp / maxHp);
	}

	/**
	 * 获取当前血量值
	 * 
	 * @param fightObj
	 * @return
	 */
	public int getCurHp(FightObject fightObj) {
		return (int) fightObj.prop.getOnePropValue(FightPropName.HpCur.value()).getNumberValue();
	}

	/**
	 * 获取最大血量值
	 * 
	 * @param fightObj
	 * @return
	 */
	public int getMaxHp(FightObject fightObj) {
		ValueBase hpMax = getOnePropValue(FightPropName.HpMax.value(), fightObj);
		ValueBase hpMaxPct = getOnePropValue(FightPropName.HpMaxPct.value(), fightObj);
		ValueBase hpMaxEx = getOnePropValue(FightPropName.HpMaxEx.value(), fightObj);
		return (int) (hpMax.getNumberValue() * (10000 + hpMaxPct.getNumberValue()) / 10000 + hpMaxEx.getNumberValue());
	}

	/**
	 * 获取当前怒气值
	 * 
	 * @param fightObj
	 * @return
	 */
	public int getCurRage(FightObject fightObj) {
		return (int) fightObj.prop.getOnePropValue(FightPropName.RageCur.value()).getNumberValue();
	}

	/**
	 * 获取最大怒气值
	 * 
	 * @param fightObj
	 * @return
	 */
	public int getMaxRage(FightObject fightObj) {
		ValueBase rageMax = getOnePropValue(FightPropName.RageMax.value(), fightObj);
		return (int) rageMax.getNumberValue();
	}

	/**
	 * 是否满怒气
	 * 
	 * @param fightObj
	 * @return
	 */
	public boolean isFullRage(FightObject fightObj) {
		int rageCur = getCurRage(fightObj);
		int rageMax = getMaxRage(fightObj);
		return (rageCur >= rageMax) ? true : false;
	}

	/**
	 * 获取基础攻击值
	 * 
	 * @param atkType
	 * @return
	 */
	public int getAtk(FightObject fightObj) {
		int atkPhy = getAtkPhy(fightObj);
		int atkMag = getAtkMag(fightObj);
		return Math.max(atkPhy, atkMag);
	}

	/**
	 * 获取物理攻击值
	 * 
	 * @return
	 */
	public int getAtkPhy(FightObject fightObj) {
		ValueBase atk = getOnePropValue(FightPropName.Atk.value(), fightObj);
		ValueBase atkPct = getOnePropValue(FightPropName.AtkPct.value(), fightObj);
		ValueBase atkEx = getOnePropValue(FightPropName.AtkEx.value(), fightObj);
		ValueBase atkPhy = getOnePropValue(FightPropName.AtkPhy.value(), fightObj);
		ValueBase atkPhyPct = getOnePropValue(FightPropName.AtkPhyPct.value(), fightObj);
		ValueBase atkPhyEx = getOnePropValue(FightPropName.AtkPhyEx.value(), fightObj);
		return (int) ((atk.getNumberValue() + atkPhy.getNumberValue())
				* (10000 + atkPct.getNumberValue() + atkPhyPct.getNumberValue()) / 10000 + atkEx.getNumberValue()
				+ atkPhyEx.getNumberValue());
	}

	/**
	 * 获取法术攻击值
	 * 
	 * @return
	 */
	public int getAtkMag(FightObject fightObj) {
		ValueBase atk = getOnePropValue(FightPropName.Atk.value(), fightObj);
		ValueBase atkPct = getOnePropValue(FightPropName.AtkPct.value(), fightObj);
		ValueBase atkEx = getOnePropValue(FightPropName.AtkEx.value(), fightObj);
		ValueBase atkMag = getOnePropValue(FightPropName.AtkMag.value(), fightObj);
		ValueBase atkMagPct = getOnePropValue(FightPropName.AtkMagPct.value(), fightObj);
		ValueBase atkMagEx = getOnePropValue(FightPropName.AtkMagEx.value(), fightObj);
		return (int) ((atk.getNumberValue() + atkMag.getNumberValue())
				* (10000 + atkPct.getNumberValue() + atkMagPct.getNumberValue()) / 10000 + atkEx.getNumberValue()
				+ atkMagEx.getNumberValue());
	}

	/**
	 * 获取基础防御值
	 * 
	 * @param atkType
	 * @return
	 */
	public int getDef(FightObject fightObj) {
		int defPhy = getDefMag(fightObj);
		int defMag = getDefPhy(fightObj);
		return Math.max(defPhy, defMag);
	}

	/**
	 * 获取物理防御值
	 * 
	 * @return
	 */
	public int getDefPhy(FightObject fightObj) {
		ValueBase def = getOnePropValue(FightPropName.Def.value(), fightObj);
		ValueBase defPct = getOnePropValue(FightPropName.DefPct.value(), fightObj);
		ValueBase defEx = getOnePropValue(FightPropName.DefEx.value(), fightObj);
		ValueBase defPhy = getOnePropValue(FightPropName.DefPhy.value(), fightObj);
		ValueBase defPhyPct = getOnePropValue(FightPropName.DefPhyPct.value(), fightObj);
		ValueBase defPhyEx = getOnePropValue(FightPropName.DefPhyEx.value(), fightObj);
		return (int) ((def.getNumberValue() + defPhy.getNumberValue())
				* (10000 + defPct.getNumberValue() + defPhyPct.getNumberValue()) / 10000 + defEx.getNumberValue()
				+ defPhyEx.getNumberValue());
	}

	/**
	 * 获取法术防御值
	 * 
	 * @return
	 */
	public int getDefMag(FightObject fightObj) {
		ValueBase def = getOnePropValue(FightPropName.Def.value(), fightObj);
		ValueBase defPct = getOnePropValue(FightPropName.DefPct.value(), fightObj);
		ValueBase defEx = getOnePropValue(FightPropName.DefEx.value(), fightObj);
		ValueBase defMag = getOnePropValue(FightPropName.DefMag.value(), fightObj);
		ValueBase defMagPct = getOnePropValue(FightPropName.DefMagPct.value(), fightObj);
		ValueBase defMagEx = getOnePropValue(FightPropName.DefMagEx.value(), fightObj);
		return (int) ((def.getNumberValue() + defMag.getNumberValue())
				* (10000 + defPct.getNumberValue() + defMagPct.getNumberValue()) / 10000 + defEx.getNumberValue()
				+ defMagEx.getNumberValue());
	}

	/**
	 * 获取对象状态值： Stun("Stun"), //定身 Chaos("Chaos"), //混乱 Paralytic("Paralytic"),
	 * //麻痹 BanHeal("BanHeal"), //禁疗 BanRage("BanRage"), //封怒 Silent("Silent"),
	 * //封技 Immortal("Immortal"), //不死 ImmunePhy("ImmunePhy"), //物理免疫
	 * ImmuneMag("ImmuneMag"), //法术免疫 Invincible("Invincible"), //无敌
	 * 
	 * @param fightObj
	 * @return
	 */
	public ValueBase getStun(FightObject fightObj) {
		return getOnePropValue(FightPropName.Stun.value(), fightObj);
	}

	public ValueBase getChaos(FightObject fightObj) {
		return getOnePropValue(FightPropName.Chaos.value(), fightObj);
	}

	public ValueBase getParalytic(FightObject fightObj) {
		return getOnePropValue(FightPropName.Paralytic.value(), fightObj);
	}

	public ValueBase getBanHeal(FightObject fightObj) {
		return getOnePropValue(FightPropName.BanHeal.value(), fightObj);
	}

	public ValueBase getBanRage(FightObject fightObj) {
		return getOnePropValue(FightPropName.BanRage.value(), fightObj);
	}

	public ValueBase getSilent(FightObject fightObj) {
		return getOnePropValue(FightPropName.Silent.value(), fightObj);
	}

	public ValueBase getImmortal(FightObject fightObj) {
		return getOnePropValue(FightPropName.Immortal.value(), fightObj);
	}

	public ValueBase getImmunePhy(FightObject fightObj) {
		return getOnePropValue(FightPropName.ImmunePhy.value(), fightObj);
	}

	public ValueBase getImmuneMag(FightObject fightObj) {
		return getOnePropValue(FightPropName.ImmuneMag.value(), fightObj);
	}

	public ValueBase getInvincible(FightObject fightObj) {
		return getOnePropValue(FightPropName.Invincible.value(), fightObj);
	}

	/**
	 * 根据攻击类型获取对应防御值
	 * 
	 * @param atkType
	 * @return
	 */
	public int getDef(FightObject fightObj, int atkType) {
		if (atkType == AtkType.Mag.value()) {
			return getDefMag(fightObj);
		}
		return getDefPhy(fightObj);
	}

	/**
	 * 回血，回复最大血量的万分比值
	 * 
	 * @param fightObj
	 * @return
	 */
	public void restoreMaxHp(FightObject fightObj) {
		int maxHp = getMaxHp(fightObj);
		ValueBase newValue = ValueFactory.getFightValueByParam(maxHp);
		fightObj.prop.setPropValue(FightPropName.HpCur.value(), newValue);
	}

	/**
	 * 重置血量
	 * 
	 * @param fightObj
	 * @return
	 */
	public void resetHpMaxHp(FightObject fightObj, int curHp, int maxHp) {
		// 设置值
		ValueBase valueHpMax = ValueFactory.getFightValueByName(FightPropName.HpMax.value(), String.valueOf(maxHp));
		fightObj.prop.setPropValue(FightPropName.HpMax.value(), valueHpMax);

		ValueBase valueHpCur = ValueFactory.getFightValueByName(FightPropName.HpCur.value(), String.valueOf(curHp));
		fightObj.prop.setPropValue(FightPropName.HpCur.value(), valueHpCur);

		// 清空值
		fightObj.prop.clearOnePropValue(FightPropName.HpMaxPct.value());
		fightObj.prop.clearOnePropValue(FightPropName.HpMaxEx.value());
	}

	/**
	 * 加血，考虑封疗
	 * 
	 * @param fightObj
	 * @param value
	 * @return
	 */
	public int addHp(FightObject fightObj, int value) {
		if (fightObj.isDie()) {
			return 0;
		}
		// 封疗
		if (value > 0 && !fightObj.buffManager.canAddHp()) {
			return 0;
		}
		ValueBase nowHp = fightObj.prop.getOnePropValue(FightPropName.HpCur.value());
		int oldHp = (int) nowHp.getNumberValue();
		int hp = oldHp + value;
		if (hp < 0) {
			// 不死
			ValueBase immortal = getOnePropValue(FightPropName.Immortal.value(), fightObj);
			if (immortal != null && immortal.isBooleanValue() && immortal.getBooleanValue()) {
				ValueBase newValue = ValueFactory.getFightValueByParam((int) 1);
				fightObj.prop.setPropValue(FightPropName.HpCur.value(), newValue);
				return -oldHp + 1;
			}

			ValueBase newValue = ValueFactory.getFightValueByParam((int) 0);
			fightObj.prop.setPropValue(FightPropName.HpCur.value(), newValue);
			return value;
		}
		int maxHp = getMaxHp(fightObj);
		if (hp > maxHp) {
			ValueBase newValue = ValueFactory.getFightValueByParam(maxHp);
			fightObj.prop.setPropValue(FightPropName.HpCur.value(), newValue);
			return value;
		}
		ValueBase newValue = ValueFactory.getFightValueByParam(hp);
		fightObj.prop.setPropValue(FightPropName.HpCur.value(), newValue);
		return value;
	}

	/**
	 * 加怒，考虑封怒
	 * 
	 * @param fightObj
	 * @param value
	 * @return
	 */
	public int addRage(FightObject fightObj, int value) {
		// 封怒
		if (value > 0 && !fightObj.buffManager.canAddRage()) {
			return 0;
		}
		ValueBase nowRage = fightObj.prop.getOnePropValue(FightPropName.RageCur.value());
		int oldRage = (int) nowRage.getNumberValue();
		int rage = oldRage + value;
		if (rage < 0) {
			ValueBase newValue = ValueFactory.getFightValueByParam((int) 0);
			fightObj.prop.setPropValue(FightPropName.RageCur.value(), newValue);
			return -oldRage;
		}
		int maxRage = (int) getOnePropValue(FightPropName.RageMax.value(), fightObj).getNumberValue();
		if (rage > maxRage) {
			ValueBase newValue = ValueFactory.getFightValueByParam(maxRage);
			fightObj.prop.setPropValue(FightPropName.RageCur.value(), newValue);
			return maxRage - oldRage;
		}
		ValueBase newValue = ValueFactory.getFightValueByParam(rage);
		fightObj.prop.setPropValue(FightPropName.RageCur.value(), newValue);
		return value;
	}

	/**
	 * 是否无敌
	 * 
	 * @param fightObj
	 * @return
	 */
	public boolean isInvincible(FightObject fightObj) {
		ValueBase invincible = getOnePropValue(FightPropName.Invincible.value(), fightObj);
		if (invincible != null && invincible.isBooleanValue() && invincible.getBooleanValue()) {
			return true;
		}
		return false;
	}

	/**
	 * 获取属性系数值
	 * 
	 * @param propCoef
	 *            属性系数
	 * @return
	 */
	public ValueBase getPropCoefValue(String propCoef, SourceParam source, ProcessParam process) {
		ValueBase value = new ValueInt(0);
		String[] temps = Utils.strToStrArray(propCoef);
		if (null == temps || temps.length != 2) {
			Log.fight.error("===error in propCoef={}", propCoef);
			return value;
		}

		PropCoefType type = PropCoefType.get(temps[0]);
		int valuePct = Utils.intValue(temps[1]);
		switch (type) {
		case SelfLoseHpPctCoef: {
			// 获取自身减少的生命万分比倍数
			FightObject fightObj = source.fightObj;
			int pct = PropManager.inst().getHpPct(fightObj);
			int coef = (Utils.I10000 - pct) / valuePct;
			value.addInt(coef);
		}
			break;
		case TargetLoseHpPctCoef: {
			// 获取目标减少的生命万分比倍数
			FightObject target = process.targetObj;
			int pct = PropManager.inst().getHpPct(target);
			int coef = (Utils.I10000 - pct) / valuePct;
			value.addInt(coef);
		}
			break;
		case TriggerTargetLoseHpPctCoef: {
			// 获取触发目标减少的生命万分比倍数
			FightStageObject stageObj = source.stageObj;
			Trigger trigger = stageObj.triggerManager.nowTrigger;
			FightObject target = trigger.triggerParam.targetObj;
			int pct = PropManager.inst().getHpPct(target);
			int coef = (Utils.I10000 - pct) / valuePct;
			value.addInt(coef);
		}
			break;
		case TriggerSelfLoseHpPctCoef: {
			// 获取触发者减少的生命万分比倍数
			FightStageObject stageObj = source.stageObj;
			Trigger trigger = stageObj.triggerManager.nowTrigger;
			FightObject fightObj = trigger.triggerParam.fightObj;
			int pct = PropManager.inst().getHpPct(fightObj);
			int coef = (Utils.I10000 - pct) / valuePct;
			value.addInt(coef);
		}
			break;
		}
		return value;
	}
}
