package game.worldsrv.character;

import game.msg.Define.DProp;
import game.worldsrv.achieveTitle.AchieveTitleManager;
import game.worldsrv.config.ConfCimelia;
import game.worldsrv.config.ConfEquipAdvanced;
import game.worldsrv.config.ConfFashion;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfPartnerRecruit;
import game.worldsrv.config.ConfPropertyGrow;
import game.worldsrv.entity.Cimelia;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.entity.Partner;
import game.worldsrv.entity.Unit;
import game.worldsrv.enumType.FightPropName;
import game.worldsrv.fashion.FashionManager;
import game.worldsrv.guild.GuildManager;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.item.ItemBodyManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.partner.ObjectType;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.partner.PartnerPlusManager;
import game.worldsrv.rune.RuneManager;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalc;
import game.worldsrv.support.PropCalcBase;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.support.ManagerBase;

public class UnitManager extends ManagerBase {

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static UnitManager inst() {
		return inst(UnitManager.class);
	}

	/**
	 * 获取unit当前血量
	 */
	public int getHpCur(UnitObject unitObj) {
		return unitObj.getUnit().getHpCur();
	}

	/**
	 * 设置unit当前血量
	 */
	public void setHpCur(UnitObject unitObj, int hpCur) {
		unitObj.getUnit().setHpCur(Math.max(0, hpCur));
	}

	/**
	 * 获取unit最大血量
	 */
	public int getHpMax(UnitObject unitObj) {
		return unitObj.getUnit().getHpMax();
	}

	/**
	 * 设置unit最大血量
	 */
	public void setHpMax(UnitObject unitObj, int hpMax) {
		unitObj.getUnit().setHpMax(Math.max(0, hpMax));
	}

	/**
	 * 初始化玩家属性，上阵伙伴与护法属性
	 * @param humanObject
	 */
	public void humanPropsInit(HumanObject humanObject) {
		HumanExtInfo exinfo = humanObject.getHumanExtInfo();
		List<Long> idLists = Utils.strToLongList(exinfo.getPartnerLineup());
		HashSet<Long> needInitServents = new HashSet<>();
		for (PartnerObject partnerObject : humanObject.partnerMap.values()) {
			if (partnerObject.getPartnerId()!=-1 && idLists.indexOf(partnerObject.getPartnerId())>-1) {
				needInitServents.addAll(partnerObject.getServantList());
			}
		}
		//初始化护法属性
		for (long serventId : needInitServents) {
			UnitObject unitObject = humanObject.getUnitControl(serventId);
			if (unitObject!=null && unitObject.isPartnerObj()) {
				initPartnerProps((PartnerObject) unitObject);
			}
		}
		//初始化伙伴属性
		for (long partnerId : idLists) {
			UnitObject unitObject = humanObject.getUnitControl(partnerId);
			if (unitObject!=null && unitObject.isPartnerObj()) {
				initPartnerProps((PartnerObject) unitObject);
			}
		}
		//初始化玩家属性
		initHumanProps(humanObject);
		//计算玩家总战力
		PartnerPlusManager.inst().sumCombat(humanObject);
	}
	
	/**
	 * 计算玩家个人属性
	 * @param humanObject
	 */
	private void initHumanProps(HumanObject humanObject) {
		PropCalcCommon propCalcCommon = new PropCalcCommon();
		Map<String, PropCalcCommon> props = new HashMap<>();
		for (EntityUnitPropPlus type : EntityUnitPropPlus.values()) {
			PropCalcCommon prop = calHumanProps(humanObject, type);
			if (!prop.getDatas().isEmpty()) {
				props.put(type.name(), prop);
				propCalcCommon.add(prop);
			}
		}
		humanObject.propsSum = propCalcCommon;
		humanObject.props = props;
		Unit unit = humanObject.getUnit();
		setUnitProp(humanObject.propsSum, unit);
		int combat = getCombat(humanObject);
		unit.setCombat(combat);
		Log.human.info("玩家：{}，战力为：{}， 初始化属性为：{}，各属性为：{}", humanObject, combat, humanObject.propsSum, humanObject.props);
	}
	
	/**
	 * 计算伙伴或护法属性
	 * @param partnerObject
	 */
	public void initPartnerProps(PartnerObject partnerObject) {
		PropCalcCommon propCalcCommon = new PropCalcCommon();
		Map<String, PropCalcCommon> props = partnerObject.props;
		props.clear();
		for (EntityUnitPropPlus type : EntityUnitPropPlus.values()) {
			PropCalcCommon prop = calPartnerProps(partnerObject, type);
			if (!prop.getDatas().isEmpty()) {
				props.put(type.name(), prop);
				propCalcCommon.add(prop);
			}
		}
		partnerObject.propsSum = propCalcCommon;
		Unit unit = partnerObject.getUnit();
		setUnitProp(partnerObject.propsSum, unit);
		int combat = getCombat(partnerObject);
		unit.setCombat(combat);
		Log.human.info("Partner：{}，战力为：{}，初始化属性为：{}，各属性为：{}", partnerObject, combat, partnerObject.propsSum, partnerObject.props);
	}
	
	private PropCalcCommon calProps(UnitObject unitObject, EntityUnitPropPlus type) {
		if (unitObject instanceof HumanObject) {
			return calHumanProps((HumanObject)unitObject, type);
		} else if (unitObject instanceof PartnerObject) {
			return calPartnerProps((PartnerObject)unitObject, type);
		} else {
			return new PropCalcCommon();
		}
	}
	
	private PropCalcCommon calHumanProps(HumanObject humanObject, EntityUnitPropPlus type) {
		PropCalcCommon propCalc = null;
		Human human = humanObject.getHuman();
		switch (type) {
		case Base:
			//基础属性
			ConfPartnerProperty conf = ConfPartnerProperty.get(humanObject.getHuman().getSn());
			propCalc = new PropCalcCommon();
			propCalc.add(conf.propName, conf.propValue);
			return propCalc;
		case Level:
			return UnitManager.inst().getLevelProp(human.getSn(), human.getLevel(), human.getAptitude(),ObjectType.Human);
		case ItemEquip:
			//装备属性
			return ItemBodyManager.inst().calc_equipPropWithoutRefine(humanObject);
		case EquipRefine:
			//装备精炼属性
			return ItemBodyManager.inst().calc_equipPropWithRefine(humanObject);
		case Rune:
			//命格套装属性
			return RuneManager.inst().calc_unitRuneProps(humanObject, -1);
		case Skill:
			//技能升级加成属性
			return HumanSkillManager.inst().calc_humanSkillLvUpProps(humanObject);
		case SkillStage:
			//技能进阶加成属性
			return HumanSkillManager.inst().calc_humanSkillAdvUpProps(humanObject);
		case SkillTrain:
			//技能培养加成的属性
			return HumanSkillManager.inst().getSkillTrainProp(humanObject);
		case PassivitySkill:break;
		case SkillGodsLv:
			//所有神兽等级相关加成的总属性
			return HumanSkillManager.inst().getSkillGodsLvProp(humanObject);
		case SkillGodsStar:
			//所有神兽星级相关加成的属性
			return HumanSkillManager.inst().getSkillGodsStarProp(humanObject);
		case Title:
			//称号的总属性（包含已经佩戴的）
			return AchieveTitleManager.inst()._process_titleTotalProps(humanObject);
		case Fashion:
			//时装属性
			return FashionManager.inst().calc_fashionProp(humanObject);
		case FashionHenshin:
			//变身属性
			return FashionManager.inst().calc_fashionHenshinProp(humanObject);
		case GuildSkill:
			//主角帮会技能
			return GuildManager.inst().calcGuildSkillProps(humanObject);
		default:
			break;
		}
		return new PropCalcCommon();
	}
	
	private PropCalcCommon calPartnerProps(PartnerObject partnerObject, EntityUnitPropPlus type) {
		PropCalcCommon propCalc = null;
		HumanObject humanObject = partnerObject.getHumanObj();
		Partner partner = partnerObject.getPartner();
		switch (type) {
		case Base: {
			//基础属性
			ConfPartnerProperty conf = ConfPartnerProperty.get(partner.getSn());
			propCalc = new PropCalcCommon();
			propCalc.add(conf.propName, conf.propValue);
			return propCalc;
		}
		case Level: {
			//等级属性
			return UnitManager.inst().getLevelProp(partner.getSn(), partner.getLevel(), partner.getAptitude(),ObjectType.Human);
		}
		case Rune:
			//命格套装属性
			return RuneManager.inst().calc_unitRuneProps(partnerObject.getHumanObj(), partnerObject.getPartnerId());
		case Advance:
			//计算伙伴升阶相关加成属性
			return PartnerPlusManager.inst().calc_partnerAdvProps(partnerObject);
		case Star:
			//计算伙伴星级相关加成属性（星级属性依赖基础，等级，升阶属性。初始化顺序需要保证）
			return PartnerPlusManager.inst().calc_partnerStarProps(partnerObject);
		case Fate:
			//计算伙伴缘分加成属性
			return PartnerPlusManager.inst().calc_partnerFateProps(partnerObject);
		case Servant:
			//护法属性
			return PartnerPlusManager.inst().calc_addPropByServents(humanObject, partnerObject);
		//PartnerSkil
		case CimeliaBase: {
			//法宝基础属性
			propCalc = new PropCalcCommon();
			ConfPartnerRecruit confPartnerReruit = ConfPartnerRecruit.get(partnerObject.getSn());
			if (confPartnerReruit == null)
				return propCalc;
			if (confPartnerReruit.cimelia == 0)
				return propCalc;
			ConfCimelia confCimelia = ConfCimelia.get(confPartnerReruit.cimelia);
			if (confCimelia == null)
				return propCalc;
			propCalc.add(confCimelia.propName, confCimelia.propValue);
			return propCalc;
		}
		case CimeliaLv: {
			//法宝等级属性
			propCalc = new PropCalcCommon();
			Cimelia cimelia = partnerObject.getCimeLia();
			if (cimelia == null)
				return propCalc;
			return UnitManager.inst().getLevelProp(cimelia.getSn(), cimelia.getLevel(), cimelia.getQuality(), ObjectType.Cimelia);
		}
		case CimeliaAdv: {
			//法宝升阶属性
			return PartnerPlusManager.inst().calc_cimeliaAdvProps(partnerObject);
		}
		case CimeliaStar:
			//法宝升星属性
			return PartnerPlusManager.inst().calc_cimeliaStarProps(partnerObject);
		case SkillGodsLv:
			//所有神兽等级相关加成的总属性
			return HumanSkillManager.inst().getSkillGodsLvProp(partnerObject.getHumanObj());
		case SkillGodsStar:
			//主角神兽星级相关加成的属性
			return HumanSkillManager.inst().getSkillGodsStarProp(humanObject);
		case Title:
			//主角称号的总属性（包含已经佩戴的）
			return AchieveTitleManager.inst()._process_titleTotalProps(humanObject);
//		case GuildSkill://TODO
//			//主角帮会技能
//			return GuildManager.inst().calcGuildSkillProps(humanObject);
		default:
			break;
		}
		return new PropCalcCommon();
	}
	
	public void propsChange(UnitObject unitObject, EntityUnitPropPlus type) {
		HumanObject humanObj = unitObject.getHumanObj();
		if (humanObj == null)
			return;
		EntityUnitPropPlus[] types = new EntityUnitPropPlus[]{type};
		if (unitObject.isPartnerObj()) {
			if (type == EntityUnitPropPlus.Base) {
				types = new EntityUnitPropPlus[]{EntityUnitPropPlus.Base,EntityUnitPropPlus.Star,EntityUnitPropPlus.Fate};
			}
			else if (type == EntityUnitPropPlus.Level) {
				types = new EntityUnitPropPlus[]{EntityUnitPropPlus.Level,EntityUnitPropPlus.Star,EntityUnitPropPlus.Fate};
			}
			else if (type == EntityUnitPropPlus.Advance) {
				types = new EntityUnitPropPlus[]{EntityUnitPropPlus.Advance,EntityUnitPropPlus.Star};
			}
			else if (type == EntityUnitPropPlus.CimeliaBase) {
				types = new EntityUnitPropPlus[]{EntityUnitPropPlus.CimeliaBase,EntityUnitPropPlus.CimeliaStar};
			}
			else if (type == EntityUnitPropPlus.CimeliaLv) {
				types = new EntityUnitPropPlus[]{EntityUnitPropPlus.CimeliaLv,EntityUnitPropPlus.CimeliaStar};
			}
			else if (type == EntityUnitPropPlus.CimeliaAdv) {
				types = new EntityUnitPropPlus[]{EntityUnitPropPlus.CimeliaAdv,EntityUnitPropPlus.CimeliaStar};
			}
		} else if (unitObject.isHumanObj()) {
			if (type==EntityUnitPropPlus.SkillGodsLv || type==EntityUnitPropPlus.SkillGodsStar || type==EntityUnitPropPlus.Title) {
				HumanExtInfo exinfo = humanObj.getHumanExtInfo();
				List<Long> idLists = Utils.strToLongList(exinfo.getPartnerLineup());
				for (long partnerId : idLists) {
					UnitObject partnerObject = humanObj.getUnitControl(partnerId);
					if (partnerObject!=null && partnerObject.isPartnerObj()) {
						propsChange(partnerObject, types);
					}
				}
			}
		}
		Unit unit = unitObject.getUnit();
		int combatBefore = unit.getCombat();
		propsChange(unitObject, types); //属性变化
		int combat = unit.getCombat();
		boolean flag = combat!=combatBefore; //战力变化
		boolean isHuman,isPartner;
		isHuman=isPartner=false;
		if (unitObject.isHumanObj()) {
			isHuman = true;
			if (flag) {
				Event.fire(EventKey.HumanCombatChange, "humanObj", humanObj, "combat", combat);
			}
			// 同步全局信息
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.updateProp(humanObj.getHumanId(), unit);
		} else if (unitObject.isPartnerObj()) {
			// 武将战力改变的时候
			PartnerObject partnerObject = (PartnerObject) unitObject;
			List<PartnerObject> partnerList = PartnerManager.inst().getPartnerList(humanObj);
			PartnerObject serventObject = null;
			PartnerObject parentsObject = null;
			for (PartnerObject po : partnerList) {
				if (po.getPartnerId() == unit.getId()) {
					isPartner = true;
					break;
				}
				List<Long> serventIdList = po.getServantList();
				if (serventIdList!=null && serventIdList.indexOf(unit.getId())>-1) {
					serventObject = partnerObject;
					parentsObject = po;
					break;
				}
			}
			if (serventObject != null) {
				//护法属性变化影响伙伴属性
				for (EntityUnitPropPlus type1 : types) {
					if (serventPropsChangeAssPartner(type1)) {
						propsChange(parentsObject, EntityUnitPropPlus.Servant);
						return;
					}
				}
			}
		}
		if (flag && (isHuman||isPartner)) {
			//计算玩家总战力是否变化
			PartnerPlusManager.inst().sumCombat(humanObj);
		}
	}
	
	private void propsChange(UnitObject unitObject, EntityUnitPropPlus... types) {
		if (types.length == 0)
			return;
		for (EntityUnitPropPlus type : types) {
			PropCalcCommon propCalcBefore = unitObject.props.get(type.name());
			propCalcBefore = propCalcBefore==null? new PropCalcCommon():propCalcBefore;
			PropCalcCommon propCalcAfter = calProps(unitObject, type);
			PropCalcBase<String> propCalcChanges = new PropCalcCommon();
			Set<String> keys = new HashSet<>();
			keys.addAll(propCalcBefore.getDatas().keySet());
			keys.addAll(propCalcAfter.getDatas().keySet());
			for (String key : keys) {
				Double val = propCalcAfter.getDatas().get(key);
				val = val==null?0:val;
				Double before = propCalcBefore.getDatas().get(key);
				before = before==null?0:before;
				if (val != before) {
					double changed = val-before;
					propCalcChanges.add(key, changed);
				}
			}
			if (propCalcChanges.getDatas().isEmpty())
				continue;
			unitObject.propsSum.add(propCalcChanges);
			if (propCalcAfter.getDatas().isEmpty()) {
				unitObject.props.remove(type.name());
			} else {
				unitObject.props.put(type.name(), propCalcAfter);
			}
		}
		Unit unit = unitObject.getUnit();
		setUnitProp(unitObject.propsSum, unit);
		int combatBefore = unit.getCombat();
		int combat = getCombat(unitObject);
		unit.setCombat(combat);
		Log.human.info("===propCalc : unitObj={}, combat={}, unit.combat={}, unit.props={}, unit.propsdis={}", unitObject, combatBefore, unit.getCombat(), unitObject.propsSum, unitObject.props);
	}

	/**
	 * @param type
	 * @return true 护法的属性变化影响伙伴属性
	 */
	public static boolean serventPropsChangeAssPartner(EntityUnitPropPlus type) {
		return type==EntityUnitPropPlus.Base || type==EntityUnitPropPlus.Level || type==EntityUnitPropPlus.Advance || type==EntityUnitPropPlus.Star ||
				type==EntityUnitPropPlus.CimeliaBase || type==EntityUnitPropPlus.CimeliaLv || type==EntityUnitPropPlus.CimeliaAdv || type==EntityUnitPropPlus.CimeliaStar;
	}
	
	/**
	 * 初始化基础信息，属性
	 * @param unit 即伙伴单元
	 * @param snProperty 即PartnerProperty.sn
	 */
	public void initProperty(Unit unit, int snProperty) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(snProperty);
		if (null == conf) {
			Log.table.error("===ConfPartnerProperty no find sn={}", snProperty);
			return;
		}

		// 设置配表模型及等级信息
		unit.setSn(conf.sn);
		unit.setModelSn(conf.modId);
		unit.setLevel(conf.lvl);
		unit.setAptitude(conf.aptitude);

		// 初始化属性，计算战力
		initUnitProp(unit, conf.propName, conf.propValue);
	}
	
	/**
	 * 初始化属性，计算战力
	 * @param unit
	 */
	public void initUnitProp(Unit unit, String[] propName, int[] propValue) {
		// 设置属性
		setUnitProp(unit, propName, propValue);

		// 计算战力
		int combat = UnitManager.inst().calcCombatProp(unit);
		unit.setCombat(combat);
	}
	/**
	 * 设置属性
	 */
	public void setUnitProp(Unit unit, String[] propName, int[] propValue) {
		PropCalc propCalc = new PropCalc(Utils.toJSONString(propName, propValue));
		setUnitProp(propCalc, unit);
	}

	private void setUnitProp(PropCalc propCalc, Unit unit) {
		
		for (FightPropName type : FightPropName.values()) {
			switch (type) {
			case HpCur: {
			}
				break;
			case HpMax: {
				unit.setHpMax(propCalc.getInt(FightPropName.HpMax));
			}
				break;
			case HpMaxPct: {
				unit.setHpMaxPct(propCalc.getInt(FightPropName.HpMaxPct));
			}
				break;
			case HpMaxEx: {
				unit.setHpMaxEx(propCalc.getInt(FightPropName.HpMaxEx));
			}
				break;
			case RageCur: {
				unit.setRageCur(propCalc.getInt(FightPropName.RageCur));
			}
				break;
			case RageMax: {
				unit.setRageMax(propCalc.getInt(FightPropName.RageMax));
			}
				break;
			case Atk: {
				unit.setAtk(propCalc.getInt(FightPropName.Atk));
			}
				break;
			case AtkPct: {
				unit.setAtkPct(propCalc.getInt(FightPropName.AtkPct));
			}
				break;
			case AtkEx: {
				unit.setAtkEx(propCalc.getInt(FightPropName.AtkEx));
			}
				break;
			case AtkPhy: {
				unit.setAtkPhy(propCalc.getInt(FightPropName.AtkPhy));
			}
				break;
			case AtkPhyPct: {
				unit.setAtkPhyPct(propCalc.getInt(FightPropName.AtkPhyPct));
			}
				break;
			case AtkPhyEx: {
				unit.setAtkPhyEx(propCalc.getInt(FightPropName.AtkPhyEx));
			}
				break;
			case AtkMag: {
				unit.setAtkMag(propCalc.getInt(FightPropName.AtkMag));
			}
				break;
			case AtkMagPct: {
				unit.setAtkMagPct(propCalc.getInt(FightPropName.AtkMagPct));
			}
				break;
			case AtkMagEx: {
				unit.setAtkMagEx(propCalc.getInt(FightPropName.AtkMagEx));
			}
				break;
			case Def: {
				unit.setDef(propCalc.getInt(FightPropName.Def));
			}
				break;
			case DefPct: {
				unit.setDefPct(propCalc.getInt(FightPropName.DefPct));
			}
				break;
			case DefEx: {
				unit.setDefEx(propCalc.getInt(FightPropName.DefEx));
			}
				break;
			case DefPhy: {
				unit.setDefPhy(propCalc.getInt(FightPropName.DefPhy));
			}
				break;
			case DefPhyPct: {
				unit.setDefPhyPct(propCalc.getInt(FightPropName.DefPhyPct));
			}
				break;
			case DefPhyEx: {
				unit.setDefPhyEx(propCalc.getInt(FightPropName.DefPhyEx));
			}
				break;
			case DefMag: {
				unit.setDefMag(propCalc.getInt(FightPropName.DefMag));
			}
				break;
			case DefMagPct: {
				unit.setDefMagPct(propCalc.getInt(FightPropName.DefMagPct));
			}
				break;
			case DefMagEx: {
				unit.setDefMagEx(propCalc.getInt(FightPropName.DefMagEx));
			}
				break;
			case Hit: {
				unit.setHit(propCalc.getInt(FightPropName.Hit));
			}
				break;
			case Dodge: {
				unit.setDodge(propCalc.getInt(FightPropName.Dodge));
			}
				break;
			case Crit: {
				unit.setCrit(propCalc.getInt(FightPropName.Crit));
			}
				break;
			case AntiCrit: {
				unit.setAntiCrit(propCalc.getInt(FightPropName.AntiCrit));
			}
				break;
			case CritAdd: {
				unit.setCritAdd(propCalc.getInt(FightPropName.CritAdd));
			}
				break;
			case AntiCritAdd: {
				unit.setAntiCritAdd(propCalc.getInt(FightPropName.AntiCritAdd));
			}
				break;
			case Pene: {
				unit.setPene(propCalc.getInt(FightPropName.Pene));
			}
				break;
			case PenePhy: {
				unit.setPenePhy(propCalc.getInt(FightPropName.PenePhy));
			}
				break;
			case PeneMag: {
				unit.setPeneMag(propCalc.getInt(FightPropName.PeneMag));
			}
				break;
			case Block: {
				unit.setBlock(propCalc.getInt(FightPropName.Block));
			}
				break;
			case AntiBlock: {
				unit.setAntiBlock(propCalc.getInt(FightPropName.AntiBlock));
			}
				break;
			case BloodSuck: {
				unit.setBloodSuck(propCalc.getInt(FightPropName.BloodSuck));
			}
				break;
			case BloodSucked: {
				// unit.setBloodSucked(propCalc.getInt(FightProp.BloodSucked));
			}
				break;
			case Control: {
				unit.setControl(propCalc.getInt(FightPropName.Control));
			}
				break;
			case AntiControl: {
				unit.setAntiControl(propCalc.getInt(FightPropName.AntiControl));
			}
				break;
				
			case DamAdd: {
				unit.setDamAdd(propCalc.getInt(FightPropName.DamAdd));
			}
				break;
			case DamAddEx: {
				unit.setDamAddEx(propCalc.getInt(FightPropName.DamAddEx));
			}
				break;
			case DamRed: {
				unit.setDamRed(propCalc.getInt(FightPropName.DamRed));
			}
				break;
			case DamRedEx: {
				unit.setDamRedEx(propCalc.getInt(FightPropName.DamRedEx));
			}
				break;
			case DamPhyAdd: {
				unit.setDamPhyAdd(propCalc.getInt(FightPropName.DamPhyAdd));
			}
				break;
			case DamPhyAddEx: {
				unit.setDamPhyAddEx(propCalc.getInt(FightPropName.DamPhyAddEx));
			}
				break;
			case DamPhyRed: {
				unit.setDamPhyRed(propCalc.getInt(FightPropName.DamPhyRed));
			}
				break;
			case DamPhyRedEx: {
				unit.setDamPhyRedEx(propCalc.getInt(FightPropName.DamPhyRedEx));
			}
				break;
			case DamMagAdd: {
				unit.setDamMagAdd(propCalc.getInt(FightPropName.DamMagAdd));
			}
				break;
			case DamMagAddEx: {
				unit.setDamMagAddEx(propCalc.getInt(FightPropName.DamMagAddEx));
			}
				break;
			case DamMagRed: {
				unit.setDamMagRed(propCalc.getInt(FightPropName.DamMagRed));
			}
				break;
			case DamMagRedEx: {
				unit.setDamMagRedEx(propCalc.getInt(FightPropName.DamMagRedEx));
			}
				break;
			case DamComAdd: {
				unit.setDamComAdd(propCalc.getInt(FightPropName.DamComAdd));
			}
				break;
			case DamComRed: {
				unit.setDamComRed(propCalc.getInt(FightPropName.DamComRed));
			}
				break;
			case DamRageAdd: {
				unit.setDamRageAdd(propCalc.getInt(FightPropName.DamRageAdd));
			}
				break;
			case DamRageRed: {
				unit.setDamRageRed(propCalc.getInt(FightPropName.DamRageRed));
			}
				break;

			case CureAdd: {
				unit.setCureAdd(propCalc.getInt(FightPropName.CureAdd));
			}
				break;
			case CureAddEx: {
				unit.setCureAddEx(propCalc.getInt(FightPropName.CureAddEx));
			}
				break;
			case HealAdd: {
				unit.setHealAdd(propCalc.getInt(FightPropName.HealAdd));
			}
				break;
			case HealAddEx: {
				unit.setHealAddEx(propCalc.getInt(FightPropName.HealAddEx));
			}
				break;
			case Shield: {
				unit.setShield(propCalc.getInt(FightPropName.Shield));
			}
				break;
			case ShieldPhy: {
				unit.setShieldPhy(propCalc.getInt(FightPropName.ShieldPhy));
			}
				break;
			case ShieldMag: {
				unit.setShieldMag(propCalc.getInt(FightPropName.ShieldMag));
			}
				break;
			case DamBack: {
				unit.setDamBack(propCalc.getInt(FightPropName.DamBack));
			}
				break;
			case PoisonAdd: {
				unit.setPoisonAdd(propCalc.getInt(FightPropName.PoisonAdd));
			}
				break;
			case PoisonAddEx: {
				unit.setPoisonAddEx(propCalc.getInt(FightPropName.PoisonAddEx));
			}
				break;
			case AntiPoisonAdd: {
				unit.setAntiPoisonAdd(propCalc.getInt(FightPropName.AntiPoisonAdd));
			}
				break;
			case AntiPoisonAddEx: {
				unit.setAntiPoisonAddEx(propCalc.getInt(FightPropName.AntiPoisonAddEx));
			}
				break;
			case BurnAdd: {
				unit.setBurnAdd(propCalc.getInt(FightPropName.BurnAdd));
			}
				break;
			case BurnAddEx: {
				unit.setBurnAddEx(propCalc.getInt(FightPropName.BurnAddEx));
			}
				break;
			case AntiBurnAdd: {
				unit.setAntiBurnAdd(propCalc.getInt(FightPropName.AntiBurnAdd));
			}
				break;
			case AntiBurnAddEx: {
				unit.setAntiBurnAddEx(propCalc.getInt(FightPropName.AntiBurnAddEx));
			}
				break;
			case BloodAdd: {
				unit.setBloodAdd(propCalc.getInt(FightPropName.BloodAdd));
			}
				break;
			case BloodAddEx: {
				unit.setBloodAddEx(propCalc.getInt(FightPropName.BloodAddEx));
			}
				break;
			case AntiBloodAdd: {
				unit.setAntiBloodAdd(propCalc.getInt(FightPropName.AntiBloodAdd));
			}
				break;
			case AntiBloodAddEx: {
				unit.setAntiBloodAddEx(propCalc.getInt(FightPropName.AntiBloodAddEx));
			}
				break;
			case Stun: {
				unit.setStun(propCalc.getInt(FightPropName.Stun));
			}
				break;
			case Chaos: {
				unit.setChaos(propCalc.getInt(FightPropName.Chaos));
			}
				break;
			case BanHeal: {
				unit.setBanHeal(propCalc.getInt(FightPropName.BanHeal));
			}
				break;
			case Paralytic: {
				unit.setParalytic(propCalc.getInt(FightPropName.Paralytic));
			}
				break;
			case BanRage: {
				unit.setBanRage(propCalc.getInt(FightPropName.BanRage));
			}
				break;
			case Silent: {
				unit.setSilent(propCalc.getInt(FightPropName.Silent));
			}
				break;
			case Immortal: {
				unit.setImmortal(propCalc.getInt(FightPropName.Immortal));
			}
				break;
			case ImmunePhy: {
				unit.setImmunePhy(propCalc.getInt(FightPropName.ImmunePhy));
			}
				break;
			case ImmuneMag: {
				unit.setImmuneMag(propCalc.getInt(FightPropName.ImmuneMag));
			}
				break;
			case Invincible: {
				unit.setInvincible(propCalc.getInt(FightPropName.Invincible));
			}
				break;
			case CertainlyHit: {
				unit.setCertainlyHit(propCalc.getInt(FightPropName.CertainlyHit));
			}
				break;
			case CertainlyControl: {
				unit.setCertainlyControl(propCalc.getInt(FightPropName.CertainlyControl));
			}
				break;
			case Weak: {
				unit.setWeak(propCalc.getInt(FightPropName.Weak));
			}
				break;
			default: {
				if (!type.isFightProp()) {
					Log.human.error("===FightProp no case type={}", type.name());
				}
			}
				break;
			}
		}
	}

	/**
	 * 计算人物属性
	 * 
	 * @propPlus 玩家当前属性
	 * @unit 角色对象
	 */
	public void setUnitProp(PropCalcCommon propPlus, Unit unit) {
		// 记录属性计算前的血/魔
		int hpMaxOld = unit.getHpMax();

		// 最大生命附加
		int HpMaxEx = propPlus.getInt(FightPropName.HpMaxEx);
		// 最大生命
		int hpMax = propPlus.getInt(FightPropName.HpMax);
		// 最大生命万分比
		int HpMaxPct = propPlus.getInt(FightPropName.HpMaxPct);
		hpMax = (int) (hpMax * (1 + HpMaxPct / Utils.D10000) + HpMaxEx);

		// 当前怒气
		int RageCur = propPlus.getInt(FightPropName.RageCur);
		// 最大怒气
		int RageMax = propPlus.getInt(FightPropName.RageMax);

		// 攻击万分比
		int AtkPct = propPlus.getInt(FightPropName.AtkPct);
		// 攻击力附加
		int AtkEx = propPlus.getInt(FightPropName.AtkEx);
		// 攻击力
		int atk = propPlus.getInt(FightPropName.Atk);
		atk = (int) (atk * (1 + AtkPct / Utils.D10000) + AtkEx);

		// 物理攻击万分比
		int AtkPhyPct = propPlus.getInt(FightPropName.AtkPhyPct);
		// 物理攻击附加
		int AtkPhyEx = propPlus.getInt(FightPropName.AtkPhyEx);
		// 物理攻击
		int atkPhy = propPlus.getInt(FightPropName.AtkPhy);
		atkPhy = (int) (atkPhy * (1 + AtkPhyPct / Utils.D10000) + AtkPhyEx);

		// 法术攻击万分比
		int AtkMagPct = propPlus.getInt(FightPropName.AtkMagPct);
		// 法术攻击附加
		int AtkMagEx = propPlus.getInt(FightPropName.AtkMagEx);
		// 法术攻击
		int atkMag = propPlus.getInt(FightPropName.AtkMag);
		atkMag = (int) (atkMag * (1 + AtkMagPct / Utils.D10000) + AtkMagEx);

		// 防御万分比
		int DefPct = propPlus.getInt(FightPropName.DefPct);
		// 防御附加
		int DefEx = propPlus.getInt(FightPropName.DefEx);
		// 防御
		int Def = propPlus.getInt(FightPropName.Def);

		// 物理防御万分比
		int DefPhyPct = propPlus.getInt(FightPropName.DefPhyPct);
		// 物理防御附加
		int DefPhyEx = propPlus.getInt(FightPropName.DefPhyEx);
		// 物理防御=物理防御+防御 + 附加
		int defPhy = propPlus.getInt(FightPropName.DefPhy) + Def;
		// 最终物理防御=物理防御*所有万分比加成的和(物理防御万分比+防御万分比)
		defPhy = (int) (defPhy * (1 + (DefPhyPct + DefPct) / Utils.D10000) + DefPhyEx);

		// 法术防御万分比=法术防御*所有万分比加成的和(法术防御万分比+防御万分比)
		int DefMagPct = propPlus.getInt(FightPropName.DefMagPct);
		// 法术防御附加
		int DefMagEx = propPlus.getInt(FightPropName.DefMagEx);
		int defMag = propPlus.getInt(FightPropName.DefMag) + Def;
		defMag = (int) (defMag * (1 + (DefMagPct + DefPct) / Utils.D10000) + DefMagEx);

		// 命中
		int hit = propPlus.getInt(FightPropName.Hit);
		// 闪避
		int dodge = propPlus.getInt(FightPropName.Dodge);
		// 暴击
		int crit = propPlus.getInt(FightPropName.Crit);
		// 坚韧
		int antiCrit = propPlus.getInt(FightPropName.AntiCrit);
		// 必杀
		int critAdd = propPlus.getInt(FightPropName.CritAdd);
		// 守护
		int antiCritAdd = propPlus.getInt(FightPropName.AntiCritAdd);
		// 防御穿透
		int Pene = propPlus.getInt(FightPropName.Pene);
		// 物理穿透
		int penePhy = propPlus.getInt(FightPropName.PenePhy);
		// 法术穿透
		int peneMag = propPlus.getInt(FightPropName.PeneMag);
		// 格挡
		int block = propPlus.getInt(FightPropName.Block);
		// 破击
		int antiBlock = propPlus.getInt(FightPropName.AntiBlock);
		// 吸血
		int bloodSuck = propPlus.getInt(FightPropName.BloodSuck);
		// 被吸血
		int bloodSucked = propPlus.getInt(FightPropName.BloodSucked);
		// 控制
		int control = propPlus.getInt(FightPropName.Control);
		// 抵抗控制
		int antiControl = propPlus.getInt(FightPropName.AntiControl);
		
		// 最终增伤率
		int DamAdd = propPlus.getInt(FightPropName.DamAdd);
		// 最终增伤附加
		int DamAddEx = propPlus.getInt(FightPropName.DamAddEx);
		// 最终减伤率
		int DamRed = propPlus.getInt(FightPropName.DamRed);
		// 最终减伤附加
		int DamRedEx = propPlus.getInt(FightPropName.DamRedEx);
		// 最终物理增伤率
		int DamPhyAdd = propPlus.getInt(FightPropName.DamPhyAdd);
		// 最终物理增伤附加 = 最终物理增伤附加 + 最终增伤附加
		int DamPhyAddEx = propPlus.getInt(FightPropName.DamPhyAddEx) + DamAddEx;
		// 最终物理减伤率
		int DamPhyRed = propPlus.getInt(FightPropName.DamPhyRed);
		// 最终物理减伤附加 = 最终物理减伤附加 + 最终减伤附加
		int DamPhyRedEx = propPlus.getInt(FightPropName.DamPhyRedEx) + DamRedEx;
		// 最终法术增伤率
		int DamMagAdd = propPlus.getInt(FightPropName.DamMagAdd);
		// 最终法术增伤附加 = 最终法术增伤附加 + 最终增伤附加
		int DamMagAddEx = propPlus.getInt(FightPropName.DamMagAddEx) + DamAddEx;
		// 最终法术减伤率
		int DamMagRed = propPlus.getInt(FightPropName.DamMagRed);
		// 最终法术减伤附加 = 最终法术减伤附加 + 最终减伤附加
		int DamMagRedEx = propPlus.getInt(FightPropName.DamMagRedEx) + DamRedEx;
		// 普攻增伤率
		int DamComAdd = propPlus.getInt(FightPropName.DamComAdd);
		// 普攻减伤率
		int DamComRed = propPlus.getInt(FightPropName.DamComRed);
		// 怒攻增伤率
		int DamRageAdd = propPlus.getInt(FightPropName.DamRageAdd);
		// 怒攻减伤率
		int DamRageRed = propPlus.getInt(FightPropName.DamRageRed);
		// 治疗率
		int CureAdd = propPlus.getInt(FightPropName.CureAdd);
		// 治疗量
		int CureAddEx = propPlus.getInt(FightPropName.CureAddEx);
		// 被治疗率
		int HealAdd = propPlus.getInt(FightPropName.HealAdd);
		// 被治疗量
		int HealAddEx = propPlus.getInt(FightPropName.HealAddEx);
		// 护盾
		int Shield = propPlus.getInt(FightPropName.Shield);
		// 物理护盾
		int ShieldPhy = propPlus.getInt(FightPropName.ShieldPhy);
		// 法术护盾
		int ShieldMag = propPlus.getInt(FightPropName.ShieldMag);
		// 反伤率
		int DamBack = propPlus.getInt(FightPropName.DamBack);
		// 中毒伤害率
		int PoisonAdd = propPlus.getInt(FightPropName.PoisonAdd);
		// 中毒伤害附加
		int PoisonAddEx = propPlus.getInt(FightPropName.PoisonAddEx);
		// 中毒伤害减免率
		int AntiPoisonAdd = propPlus.getInt(FightPropName.AntiPoisonAdd);
		// 中毒伤害减免附加
		int AntiPoisonAddEx = propPlus.getInt(FightPropName.AntiPoisonAddEx);
		// 灼烧伤害率
		int BurnAdd = propPlus.getInt(FightPropName.BurnAdd);
		// 灼烧伤害附加
		int BurnAddEx = propPlus.getInt(FightPropName.BurnAddEx);
		// 灼烧伤害减免率
		int AntiBurnAdd = propPlus.getInt(FightPropName.AntiBurnAdd);
		// 灼烧伤害减免附加
		int AntiBurnAddEx = propPlus.getInt(FightPropName.AntiBurnAddEx);
		// 流血伤害率
		int BloodAdd = propPlus.getInt(FightPropName.BloodAdd);
		// 流血伤害附加
		int BloodAddEx = propPlus.getInt(FightPropName.BloodAddEx);
		// 流血伤害减免率
		int AntiBloodAdd = propPlus.getInt(FightPropName.AntiBloodAdd);
		// 流血伤害减免附加
		int AntiBloodAddEx = propPlus.getInt(FightPropName.AntiBloodAddEx);
		// 定身
		int Stun = propPlus.getInt(FightPropName.Stun);
		// 混乱
		int Chaos = propPlus.getInt(FightPropName.Chaos);
		// 禁疗
		int BanHeal = propPlus.getInt(FightPropName.BanHeal);
		// 麻痹
		int Paralytic = propPlus.getInt(FightPropName.Paralytic);
		// 封怒
		int BanRage = propPlus.getInt(FightPropName.BanRage);
		// 封技
		int Silent = propPlus.getInt(FightPropName.Silent);
		// 不死
		int Immortal = propPlus.getInt(FightPropName.Immortal);
		// 物理免疫
		int ImmunePhy = propPlus.getInt(FightPropName.ImmunePhy);
		// 法术免疫
		int ImmuneMag = propPlus.getInt(FightPropName.ImmuneMag);
		// 无敌
		int Invincible = propPlus.getInt(FightPropName.Invincible);
		// 必中
		int certainlyHit = propPlus.getInt(FightPropName.CertainlyHit);
		// 必控
		int certainlyControl = propPlus.getInt(FightPropName.CertainlyControl);
		// 虚弱
		int weak = propPlus.getInt(FightPropName.Weak);

		// 设置新属性值
		unit.setHpCur(hpMax);
		unit.setHpMax(hpMax);
		unit.setHpMaxPct(HpMaxPct);
		unit.setHpMaxEx(HpMaxEx);
		unit.setRageCur(RageCur);
		unit.setRageMax(RageMax);
		unit.setAtk(atk);
		unit.setAtkPct(AtkPct);
		unit.setAtkEx(AtkEx);
		unit.setAtkPhy(atkPhy);
		unit.setAtkPhyPct(AtkPhyPct);
		unit.setAtkPhyEx(AtkPhyEx);
		unit.setAtkMag(atkMag);
		unit.setAtkMagPct(AtkMagPct);
		unit.setAtkMagEx(AtkMagEx);
		unit.setDef(Def);
		unit.setDefPct(DefPct);
		unit.setDefEx(DefEx);
		unit.setDefPhy(defPhy);
		unit.setDefPhyPct(DefPhyPct);
		unit.setDefPhyEx(DefPhyEx);
		unit.setDefMag(defMag);
		unit.setDefMagPct(DefMagPct);
		unit.setDefMagEx(DefMagEx);
		unit.setHit(hit);
		unit.setDodge(dodge);
		unit.setCrit(crit);
		unit.setAntiCrit(antiCrit);
		unit.setCritAdd(critAdd);
		unit.setAntiCritAdd(antiCritAdd);
		unit.setPene(Pene);
		unit.setPenePhy(penePhy);
		unit.setPeneMag(peneMag);
		unit.setBlock(block);
		unit.setAntiBlock(antiBlock);
		unit.setBloodSuck(bloodSuck);
		unit.setBloodSucked(bloodSucked);
		unit.setControl(control);
		unit.setAntiControl(antiControl);
		
		unit.setDamAdd(DamAdd);
		unit.setDamAddEx(DamAddEx);
		unit.setDamRed(DamRed);
		unit.setDamRedEx(DamRedEx);
		unit.setDamPhyAdd(DamPhyAdd);
		unit.setDamPhyAddEx(DamPhyAddEx);
		unit.setDamPhyRed(DamPhyRed);
		unit.setDamPhyRedEx(DamPhyRedEx);
		unit.setDamMagAdd(DamMagAdd);
		unit.setDamMagAddEx(DamMagAddEx);
		unit.setDamMagRed(DamMagRed);
		unit.setDamMagRedEx(DamMagRedEx);
		unit.setDamComAdd(DamComAdd);
		unit.setDamComRed(DamComRed);
		unit.setDamRageAdd(DamRageAdd);
		unit.setDamRageRed(DamRageRed);
		unit.setCureAdd(CureAdd);
		unit.setCureAddEx(CureAddEx);
		unit.setHealAdd(HealAdd);
		unit.setHealAddEx(HealAddEx);
		unit.setShield(Shield);
		unit.setShieldPhy(ShieldPhy);
		unit.setShieldMag(ShieldMag);
		unit.setDamBack(DamBack);
		unit.setPoisonAdd(PoisonAdd);
		unit.setPoisonAddEx(PoisonAddEx);
		unit.setAntiPoisonAdd(AntiPoisonAdd);
		unit.setAntiPoisonAddEx(AntiPoisonAddEx);
		unit.setBurnAdd(BurnAdd);
		unit.setBurnAddEx(BurnAddEx);
		unit.setAntiBurnAdd(AntiBurnAdd);
		unit.setAntiBurnAddEx(AntiBurnAddEx);
		unit.setBloodAdd(BloodAdd);
		unit.setBloodAddEx(BloodAddEx);
		unit.setAntiBloodAdd(AntiBloodAdd);
		unit.setAntiBloodAddEx(AntiBloodAddEx);
		unit.setStun(Stun);
		unit.setChaos(Chaos);
		unit.setBanHeal(BanHeal);
		unit.setParalytic(Paralytic);
		unit.setBanRage(BanRage);
		unit.setSilent(Silent);
		unit.setImmortal(Immortal);
		unit.setImmunePhy(ImmunePhy);
		unit.setImmuneMag(ImmuneMag);
		unit.setInvincible(Invincible);
		unit.setCertainlyHit(certainlyHit);
		unit.setCertainlyControl(certainlyControl);
		unit.setWeak(weak);

		// 计算当前的HP和MP
		double hpNew = unit.getHpCur() * (1.0 * unit.getHpMax() / hpMaxOld);
		unit.setHpCur((int) hpNew);
	}

	/**
	 * 计算战斗力 = 属性1*属性1战力参数 + 属性2*属性2战力参数 + 属性n*属性n战力参数
	 * 
	 * @return
	 */
	public int getCombat(UnitObject unitObj) {
		int combatAll = 0;
		if (unitObj.isHumanObj()) { // 人物战斗力的处理
			// 如果或者当前unit的Human
			// HumanObject humanObj = unitObj.getHumanObj();
			// 现在使用的计算战力方式
			int combatSkill = 0;// calcCombatSkill(unitObj.getUnit());
			int combatProp = calcCombatProp(unitObj.getUnit());

			combatAll = combatSkill + combatProp;
			// humanObj.getHuman().setCombat(combatAll);

			// System.out.println("人物技能战力：" + combatSkill);
			// System.out.println("人物属性战力：" + combatProp);
			// System.out.println("最终战力：" + combatAll);
		} else if (unitObj.isMonsterObj()) {
			// 现在使用的计算战力方式
			int combatSkill = 0;// calcCombatSkill(unitObj.getUnit());
			int combatProp = calcCombatProp(unitObj.getUnit());
			combatAll = combatSkill + combatProp;
		} else if (unitObj.isPartnerObj()) {// 武将战力 暂时的
			int combatSkill = 0;// calcCombatSkill(unitObj.getUnit()); //技能战力
			int combatProp = calcCombatProp(unitObj.getUnit());
			combatAll = combatSkill + combatProp;
		}
		return combatAll;
	}

	/**
	 * 属性战斗力模块 = int（属性1*属性1战力参数 + 属性2*属性2战力参数 + 属性n*属性n战力参数）
	 */
	public int calcCombatProp(Unit unit) {
		// 战力 = 属性值 * 对应参数
		double combatProp = 0;
		if (unit == null) {
			return (int) combatProp;
		}

		combatProp += unit.getHpMax() * ParamManager.combatHpMax;         	// 战斗力-最大怒气系数(Float型）  
		combatProp += unit.getRageMax() * ParamManager.combatRageMax;     	// 战斗力-最大怒气系数(Float型）          
		combatProp += unit.getAtk() * ParamManager.combatAtk;				// 战斗力-攻击系数(Float型）
		combatProp += unit.getDefPhy() * ParamManager.combatDefPhy;      	// 战斗力-物防系数(Float型）              
		combatProp += unit.getDefMag() * ParamManager.combatDefMag;      	// 战斗力-法防系数(Float型）              
		combatProp += unit.getHit() * ParamManager.combatHit;         		// 战斗力-命中系数(Float型）            
		combatProp += unit.getDodge() * ParamManager.combatDodge;       	// 战斗力-闪避系数(Float型）              
		combatProp += unit.getCrit() * ParamManager.combatCrit;        		// 战斗力-暴击系数(Float型）            
		combatProp += unit.getAntiCrit() * ParamManager.combatAntiCrit;    	// 战斗力-坚韧系数(Float型）                
		combatProp += unit.getBlock() * ParamManager.combatBlock;       	// 战斗力-格挡系数(Float型）              
		combatProp += unit.getAntiBlock() * ParamManager.combatAntiBlock;   // 战斗力-破挡系数(Float型）  
		combatProp += unit.getDamAdd() * ParamManager.combatDamAdd;      	// 战斗力-伤害加深(Float型） 
		combatProp += unit.getDamAddEx() * ParamManager.combatDamAddEx;    	// 战斗力-最终增伤(Float型）
		combatProp += unit.getDamRed() * ParamManager.combatDamRed;      	// 战斗力-伤害减免(Float型）              
		combatProp += unit.getDamRedEx() * ParamManager.combatDamRedEx;    	// 战斗力-最终减伤(Float型）                
		combatProp += unit.getPenePhy() * ParamManager.combatPenePhy;     	// 战斗力-物理穿透(Float型）                
		combatProp += unit.getPeneMag() * ParamManager.combatPeneMag;     	// 战斗力-法术穿透(Float型）                
		combatProp += unit.getCureAdd() * ParamManager.combatCureAdd;     	// 战斗力-治疗强度(Float型）                
		combatProp += unit.getCureAddEx() * ParamManager.combatCureAddEx;   // 战斗力-最终治疗(Float型）                  
		combatProp += unit.getHealAdd() * ParamManager.combatHealAdd;     	// 战斗力-受疗效果(Float型）                
		combatProp += unit.getCritAdd() * ParamManager.combatCritAdd;     	// 战斗力-致命一击(Float型）                
		combatProp += unit.getAntiCritAdd() * ParamManager.combatAntiCritAdd; // 战斗力-致命抵抗(Float型）

		return (int) Math.ceil(combatProp);// 向上取整
	}

	/**
	 * 获取指定属性sn的主角或伙伴在指定等级下的等级加成属性
	 * @param sn 对应PartnerProperty.sn
	 * @param level 等级(必须>1，即>1才有加成)
	 * @param aptitude kk资质(必须>=1，即>=1才有加成)
	 * @return
	 */
	public PropCalcCommon getLevelProp(int sn, int level, int aptitude, ObjectType objectType) {
		PropCalcCommon prop = new PropCalcCommon();
		if (level > 1 && aptitude >= 1) {// 取(level-1)级的属性加成
			int growpSn = 0;

			switch (objectType) {
			case Human:
			case Partner:
				ConfPartnerProperty conf = ConfPartnerProperty.get(sn);
				if (conf != null) {
					growpSn = conf.snPropertyGrow;
				}
				break;
			case Cimelia:
				ConfCimelia confCimelia = ConfCimelia.get(sn);
				if (confCimelia != null) {
					growpSn = confCimelia.snPropertyGrow;
				}
				break;
			default:
				break;
			}

			ConfPropertyGrow confGrow = ConfPropertyGrow.get(growpSn);
			if (confGrow != null) {
				// 成长属性=成长值*资质/2*(lv-1)
				prop.add(confGrow.propName, confGrow.propValue);
				// 伙伴和主角才计算资质相关
				if (objectType != ObjectType.Cimelia) {
					prop.multiply(confGrow.propName, aptitude/2);
				}
				prop.multiply(confGrow.propName, level - 1);
			}
		}
		return prop;

	}

	/**
	 * 获取初始化时装备的属性
	 * @return
	 */
	public PropCalc getEquipProp() {
		PropCalc prop = new PropCalc();
		int[] initHumanEquipSnArr = ParamManager.initHumanEquip;
		ConfItem confItem = null;
		ConfEquipAdvanced confEquip = null;
		for (int itemSn : initHumanEquipSnArr) {
			confItem = ConfItem.get(itemSn);
			int equipSn = ConfigKeyFormula.getEquipAdvancedSn(confItem.itemType, 1);
			confEquip = ConfEquipAdvanced.get(equipSn);

			prop.add(confEquip.properties, confEquip.value);
		}
		return prop;
	}

	/**
	 * 获取初始化时时装的属性
	 */
	public PropCalc getFashionProp(ConfFashion confFashion) {
		PropCalc prop = new PropCalc();
		prop.add(confFashion.attrType, confFashion.attrValue);
		return prop;
	}

	public Object getPropTypeValue(String name, Unit unit) {
		Object result = null;
		FightPropName type = FightPropName.get(name);
		if (type == null) {
			Log.game.error("===getPropTypeValue: no find type={}", name);
			return result;
		}

		switch (type) {
		case HpCur: {
		}
			break;
		case HpMax: {
			result = unit.getHpMax();
		}
			break;
		case HpMaxPct: {
			result = unit.getHpMaxPct();
		}
			break;
		case HpMaxEx: {
			result = unit.getHpMaxEx();
		}
			break;
		case RageCur: {
			result = unit.getRageCur();
		}
			break;
		case RageMax: {
			result = unit.getRageMax();
		}
			break;
		case Atk: {
			result = unit.getAtk();
		}
			break;
		case AtkPct: {
			result = unit.getAtkPct();
		}
			break;
		case AtkEx: {
			result = unit.getAtkEx();
		}
			break;
		case AtkPhy: {
			result = unit.getAtkPhy();
		}
			break;
		case AtkPhyPct: {
			result = unit.getAtkPhyPct();
		}
			break;
		case AtkPhyEx: {
			result = unit.getAtkPhyEx();
		}
			break;
		case AtkMag: {
			result = unit.getAtkMag();
		}
			break;
		case AtkMagPct: {
			result = unit.getAtkMagPct();
		}
			break;
		case AtkMagEx: {
			result = unit.getAtkMagEx();
		}
			break;
		case Def: {
			result = unit.getDef();
		}
			break;
		case DefPct: {
			result = unit.getDefPct();
		}
			break;
		case DefEx: {
			result = unit.getDefEx();
		}
			break;
		case DefPhy: {
			result = unit.getDefPhy();
		}
			break;
		case DefPhyPct: {
			result = unit.getDefPhyPct();
		}
			break;
		case DefPhyEx: {
			result = unit.getDefPhyEx();
		}
			break;
		case DefMag: {
			result = unit.getDefMag();
		}
			break;
		case DefMagPct: {
			result = unit.getDefMagPct();
		}
			break;
		case DefMagEx: {
			result = unit.getDefMagEx();
		}
			break;
		case Hit: {
			result = unit.getHit();
		}
			break;
		case Dodge: {
			result = unit.getDodge();
		}
			break;
		case Crit: {
			result = unit.getCrit();
		}
			break;
		case AntiCrit: {
			result = unit.getAntiCrit();
		}
			break;
		case CritAdd: {
			result = unit.getCritAdd();
		}
			break;
		case AntiCritAdd: {
			result = unit.getAntiCritAdd();
		}
			break;
		case Pene: {
			result = unit.getPene();
		}
			break;
		case PenePhy: {
			result = unit.getPenePhy();
		}
			break;
		case PeneMag: {
			result = unit.getPeneMag();
		}
			break;
		case Block: {
			result = unit.getBlock();
		}
			break;
		case AntiBlock: {
			result = unit.getAntiBlock();
		}
			break;
		case BloodSuck: {
			result = unit.getBloodSuck();
		}
			break;
		case BloodSucked: {
			result = unit.getBloodSucked();
		}
			break;
		case Control: {
			result = unit.getControl();
		}
			break;
		case AntiControl: {
			result = unit.getAntiControl();
		}
			break;
			
		case DamAdd: {
			result = unit.getDamAdd();
		}
			break;
		case DamAddEx: {
			result = unit.getDamAddEx();
		}
			break;
		case DamRed: {
			result = unit.getDamRed();
		}
			break;
		case DamRedEx: {
			result = unit.getDamRedEx();
		}
			break;
		case DamPhyAdd: {
			result = unit.getDamPhyAdd();
		}
			break;
		case DamPhyAddEx: {
			result = unit.getDamPhyAddEx();
		}
			break;
		case DamPhyRed: {
			result = unit.getDamPhyRed();
		}
			break;
		case DamPhyRedEx: {
			result = unit.getDamPhyRedEx();
		}
			break;
		case DamMagAdd: {
			result = unit.getDamMagAdd();
		}
			break;
		case DamMagAddEx: {
			result = unit.getDamMagAddEx();
		}
			break;
		case DamMagRed: {
			result = unit.getDamMagRed();
		}
			break;
		case DamMagRedEx: {
			result = unit.getDamMagRedEx();
		}
			break;
		case DamComAdd: {
			result = unit.getDamComAdd();
		}
			break;
		case DamComRed: {
			result = unit.getDamComRed();
		}
			break;
		case DamRageAdd: {
			result = unit.getDamRageAdd();
		}
			break;
		case DamRageRed: {
			result = unit.getDamRageRed();
		}
			break;

		case CureAdd: {
			result = unit.getCureAdd();
		}
			break;
		case CureAddEx: {
			result = unit.getCureAddEx();
		}
			break;
		case HealAdd: {
			result = unit.getHealAdd();
		}
			break;
		case HealAddEx: {
			result = unit.getHealAddEx();
		}
			break;
		case Shield: {
			result = unit.getShield();
		}
			break;
		case ShieldPhy: {
			result = unit.getShieldPhy();
		}
			break;
		case ShieldMag: {
			result = unit.getShieldMag();
		}
			break;
		case DamBack: {
			result = unit.getDamBack();
		}
			break;
		case PoisonAdd: {
			result = unit.getPoisonAdd();
		}
			break;
		case PoisonAddEx: {
			result = unit.getPoisonAddEx();
		}
			break;
		case AntiPoisonAdd: {
			result = unit.getAntiPoisonAdd();
		}
			break;
		case AntiPoisonAddEx: {
			result = unit.getAntiPoisonAddEx();
		}
			break;
		case BurnAdd: {
			result = unit.getBurnAdd();
		}
			break;
		case BurnAddEx: {
			result = unit.getBurnAddEx();
		}
			break;
		case AntiBurnAdd: {
			result = unit.getAntiBurnAdd();
		}
			break;
		case AntiBurnAddEx: {
			result = unit.getAntiBurnAddEx();
		}
			break;
		case BloodAdd: {
			result = unit.getBloodAdd();
		}
			break;
		case BloodAddEx: {
			result = unit.getBloodAddEx();
		}
			break;
		case AntiBloodAdd: {
			result = unit.getAntiBloodAdd();
		}
			break;
		case AntiBloodAddEx: {
			result = unit.getAntiBloodAddEx();
		}
			break;
		case Stun: {
			result = unit.getStun();
		}
			break;
		case Chaos: {
			result = unit.getChaos();
		}
			break;
		case BanHeal: {
			result = unit.getBanHeal();
		}
			break;
		case Paralytic: {
			result = unit.getParalytic();
		}
			break;
		case BanRage: {
			result = unit.getBanRage();
		}
			break;
		case Silent: {
			result = unit.getSilent();
		}
			break;
		case Immortal: {
			result = unit.getImmortal();
		}
			break;
		case ImmunePhy: {
			result = unit.getImmunePhy();
		}
			break;
		case ImmuneMag: {
			result = unit.getImmuneMag();
		}
			break;
		case Invincible: {
			result = unit.getInvincible();
		}
			break;
		case CertainlyHit: {
			result = unit.getCertainlyHit();
		}
			break;
		case CertainlyControl: {
			result = unit.getCertainlyControl();
		}
			break;
		case Weak: {
			result = unit.getWeak();
		}
			break;

		default: {
			if (type.isFightProp()) {
				Log.human.error("===属性错误,属性是战斗专用属性：getPropTypeValue FightProp.java key={}", type.value());
			} else {
				Log.human.error("===属性找不到：getPropTypeValue FightProp.java no find key={}", type.value());
			}
		}
			break;
		}
		return result;
	}

	public void setPropKeyValue(FightPropName key, Double value, Unit unit) {
		// Log.game.debug("UnitManager.setPropKeyValue FightProp={},
		// value={}, unit={}",key, value, unit);
		switch (key) {
		case HpCur: {
		}
			break;
		case HpMax: {
			unit.setHpMax(value.intValue());
		}
			break;
		case HpMaxPct: {
			unit.setHpMaxPct(value.intValue());
		}
			break;
		case HpMaxEx: {
			unit.setHpMaxEx(value.intValue());
		}
			break;
		case RageCur: {
			unit.setRageCur(value.intValue());
		}
			break;
		case RageMax: {
			unit.setRageMax(value.intValue());
		}
			break;
		case Atk: {
			unit.setAtk(value.intValue());
		}
			break;
		case AtkPct: {
			unit.setAtkPct(value.intValue());
		}
			break;
		case AtkEx: {
			unit.setAtkEx(value.intValue());
		}
			break;
		case AtkPhy: {
			unit.setAtkPhy(value.intValue());
		}
			break;
		case AtkPhyPct: {
			unit.setAtkPhyPct(value.intValue());
		}
			break;
		case AtkPhyEx: {
			unit.setAtkPhyEx(value.intValue());
		}
			break;
		case AtkMag: {
			unit.setAtkMag(value.intValue());
		}
			break;
		case AtkMagPct: {
			unit.setAtkMagPct(value.intValue());
		}
			break;
		case AtkMagEx: {
			unit.setAtkMagEx(value.intValue());
		}
			break;
		case Def: {
			unit.setDef(value.intValue());
		}
			break;
		case DefPct: {
			unit.setDefPct(value.intValue());
		}
			break;
		case DefEx: {
			unit.setDefEx(value.intValue());
		}
			break;
		case DefPhy: {
			unit.setDefPhy(value.intValue());
		}
			break;
		case DefPhyPct: {
			unit.setDefPhyPct(value.intValue());
		}
			break;
		case DefPhyEx: {
			unit.setDefPhyEx(value.intValue());
		}
			break;
		case DefMag: {
			unit.setDefMag(value.intValue());
		}
			break;
		case DefMagPct: {
			unit.setDefMagPct(value.intValue());
		}
			break;
		case DefMagEx: {
			unit.setDefMagEx(value.intValue());
		}
			break;
		case Hit: {
			unit.setHit(value.intValue());
		}
			break;
		case Dodge: {
			unit.setDodge(value.intValue());
		}
			break;
		case Crit: {
			unit.setCrit(value.intValue());
		}
			break;
		case AntiCrit: {
			unit.setAntiCrit(value.intValue());
		}
			break;
		case CritAdd: {
			unit.setCritAdd(value.intValue());
		}
			break;
		case AntiCritAdd: {
			unit.setAntiCritAdd(value.intValue());
		}
			break;
		case Pene: {
			unit.setPene(value.intValue());
		}
			break;
		case PenePhy: {
			unit.setPenePhy(value.intValue());
		}
			break;
		case PeneMag: {
			unit.setPeneMag(value.intValue());
		}
			break;
		case Block: {
			unit.setBlock(value.intValue());
		}
			break;
		case AntiBlock: {
			unit.setAntiBlock(value.intValue());
		}
			break;
		case BloodSuck: {
			unit.setBloodSuck(value.intValue());
		}
			break;
		case BloodSucked: {
			unit.setBloodSucked(value.intValue());
		}
			break;
		case Control: {
			unit.setControl(value.intValue());
		}
			break;
		case AntiControl: {
			unit.setAntiControl(value.intValue());
		}
			break;
			
		case DamAdd: {
			unit.setDamAdd(value.intValue());
		}
			break;
		case DamAddEx: {
			unit.setDamAddEx(value.intValue());
		}
			break;
		case DamRed: {
			unit.setDamRed(value.intValue());
		}
			break;
		case DamRedEx: {
			unit.setDamRedEx(value.intValue());
		}
			break;
		case DamPhyAdd: {
			unit.setDamPhyAdd(value.intValue());
		}
			break;
		case DamPhyAddEx: {
			unit.setDamPhyAddEx(value.intValue());
		}
			break;
		case DamPhyRed: {
			unit.setDamPhyRed(value.intValue());
		}
			break;
		case DamPhyRedEx: {
			unit.setDamPhyRedEx(value.intValue());
		}
			break;
		case DamMagAdd: {
			unit.setDamMagAdd(value.intValue());
		}
			break;
		case DamMagAddEx: {
			unit.setDamMagAddEx(value.intValue());
		}
			break;
		case DamMagRed: {
			unit.setDamMagRed(value.intValue());
		}
			break;
		case DamMagRedEx: {
			unit.setDamMagRedEx(value.intValue());
		}
			break;
		case DamComAdd: {
			unit.setDamComAdd(value.intValue());
		}
			break;
		case DamComRed: {
			unit.setDamComRed(value.intValue());
		}
			break;
		case DamRageAdd: {
			unit.setDamRageAdd(value.intValue());
		}
			break;
		case DamRageRed: {
			unit.setDamRageRed(value.intValue());
		}
			break;

		case CureAdd: {
			unit.setCureAdd(value.intValue());
		}
			break;
		case CureAddEx: {
			unit.setCureAddEx(value.intValue());
		}
			break;
		case HealAdd: {
			unit.setHealAdd(value.intValue());
		}
			break;
		case HealAddEx: {
			unit.setHealAddEx(value.intValue());
		}
			break;
		case Shield: {
			unit.setShield(value.intValue());
		}
			break;
		case ShieldPhy: {
			unit.setShieldPhy(value.intValue());
		}
			break;
		case ShieldMag: {
			unit.setShieldMag(value.intValue());
		}
			break;
		case DamBack: {
			unit.setDamBack(value.intValue());
		}
			break;
		case PoisonAdd: {
			unit.setPoisonAdd(value.intValue());
		}
			break;
		case PoisonAddEx: {
			unit.setPoisonAddEx(value.intValue());
		}
			break;
		case AntiPoisonAdd: {
			unit.setAntiPoisonAdd(value.intValue());
		}
			break;
		case AntiPoisonAddEx: {
			unit.setAntiPoisonAddEx(value.intValue());
		}
			break;
		case BurnAdd: {
			unit.setBurnAdd(value.intValue());
		}
			break;
		case BurnAddEx: {
			unit.setBurnAddEx(value.intValue());
		}
			break;
		case AntiBurnAdd: {
			unit.setAntiBurnAdd(value.intValue());
		}
			break;
		case AntiBurnAddEx: {
			unit.setAntiBurnAddEx(value.intValue());
		}
			break;
		case BloodAdd: {
			unit.setBloodAdd(value.intValue());
		}
			break;
		case BloodAddEx: {
			unit.setBloodAddEx(value.intValue());
		}
			break;
		case AntiBloodAdd: {
			unit.setAntiBloodAdd(value.intValue());
		}
			break;
		case AntiBloodAddEx: {
			unit.setAntiBloodAddEx(value.intValue());
		}
			break;
		case Stun: {
			unit.setStun(value.intValue());
		}
			break;
		case Chaos: {
			unit.setChaos(value.intValue());
		}
			break;
		case BanHeal: {
			unit.setBanHeal(value.intValue());
		}
			break;
		case Paralytic: {
			unit.setParalytic(value.intValue());
		}
			break;
		case BanRage: {
			unit.setBanRage(value.intValue());
		}
			break;
		case Silent: {
			unit.setSilent(value.intValue());
		}
			break;
		case Immortal: {
			unit.setImmortal(value.intValue());
		}
			break;
		case ImmunePhy: {
			unit.setImmunePhy(value.intValue());
		}
			break;
		case ImmuneMag: {
			unit.setImmuneMag(value.intValue());
		}
			break;
		case Invincible: {
			unit.setInvincible(value.intValue());
		}
			break;
		case CertainlyHit: {
			unit.setCertainlyHit(value.intValue());
		}
			break;
		case CertainlyControl: {
			unit.setCertainlyControl(value.intValue());
		}
			break;
		case Weak: {
			unit.setWeak(value.intValue());
		}
			break;
		default: {
			if (!key.isFightProp()) {
				Log.human.error("===属性找不到：setPropKeyValue no find key={}", key.value());
			}
		}
			break;
		}
	}

	/**
	 * 获取Unit的属性信息
	 * 
	 * @return
	 */
	public void setDProp(DProp.Builder dProp, String name, Object value) {
		FightPropName type = FightPropName.get(name);
		if (type == null) {
			Log.game.error("===FightProp no find type={}", name);
			return;
		}

		switch (type) {
		case HpCur: {
		}
			break;
		case HpMax: {
			dProp.setHpMax(Utils.intValue(value));
		}
			break;
		case HpMaxPct: {
			dProp.setHpMaxPct(Utils.intValue(value));
		}
			break;
		case HpMaxEx: {
			dProp.setHpMaxEx(Utils.intValue(value));
		}
			break;
		case RageCur: {
			dProp.setRageCur(Utils.intValue(value));
		}
			break;
		case RageMax: {
			dProp.setRageMax(Utils.intValue(value));
		}
			break;
		case Atk: {
			dProp.setAtk(Utils.intValue(value));
		}
			break;
		case AtkPct: {
			dProp.setAtkPct(Utils.intValue(value));
		}
			break;
		case AtkEx: {
			dProp.setAtkEx(Utils.intValue(value));
		}
			break;
		case AtkPhy: {
			dProp.setAtkPhy(Utils.intValue(value));
		}
			break;
		case AtkPhyPct: {
			dProp.setAtkPhyPct(Utils.intValue(value));
		}
			break;
		case AtkPhyEx: {
			dProp.setAtkPhyEx(Utils.intValue(value));
		}
			break;
		case AtkMag: {
			dProp.setAtkMag(Utils.intValue(value));
		}
			break;
		case AtkMagPct: {
			dProp.setAtkMagPct(Utils.intValue(value));
		}
			break;
		case AtkMagEx: {
			dProp.setAtkMagEx(Utils.intValue(value));
		}
			break;
		case Def: {
			dProp.setDef(Utils.intValue(value));
		}
			break;
		case DefPct: {
			dProp.setDefPct(Utils.intValue(value));
		}
			break;
		case DefEx: {
			dProp.setDefEx(Utils.intValue(value));
		}
			break;
		case DefPhy: {
			dProp.setDefPhy(Utils.intValue(value));
		}
			break;
		case DefPhyPct: {
			dProp.setDefPhyPct(Utils.intValue(value));
		}
			break;
		case DefPhyEx: {
			dProp.setDefPhyEx(Utils.intValue(value));
		}
			break;
		case DefMag: {
			dProp.setDefMag(Utils.intValue(value));
		}
			break;
		case DefMagPct: {
			dProp.setDefMagPct(Utils.intValue(value));
		}
			break;
		case DefMagEx: {
			dProp.setDefMagEx(Utils.intValue(value));
		}
			break;
		case Hit: {
			dProp.setHit(Utils.intValue(value));
		}
			break;
		case Dodge: {
			dProp.setDodge(Utils.intValue(value));
		}
			break;
		case Crit: {
			dProp.setCrit(Utils.intValue(value));
		}
			break;
		case AntiCrit: {
			dProp.setAntiCrit(Utils.intValue(value));
		}
			break;
		case CritAdd: {
			dProp.setCritAdd(Utils.intValue(value));
		}
			break;
		case AntiCritAdd: {
			dProp.setAntiCritAdd(Utils.intValue(value));
		}
			break;
		case Pene: {
			dProp.setPene(Utils.intValue(value));
		}
			break;
		case PenePhy: {
			dProp.setPenePhy(Utils.intValue(value));
		}
			break;
		case PeneMag: {
			dProp.setPeneMag(Utils.intValue(value));
		}
			break;
		case Block: {
			dProp.setBlock(Utils.intValue(value));
		}
			break;
		case AntiBlock: {
			dProp.setAntiBlock(Utils.intValue(value));
		}
			break;
		case BloodSuck: {
			dProp.setBloodSuck(Utils.intValue(value));
		}
			break;
		case BloodSucked: {
			// dProp.setBloodSucked(Utils.intValue(value));
		}
			break;
		case Control: {
			dProp.setControl(Utils.intValue(value));
		}
			break;
		case AntiControl: {
			 dProp.setAntiControl(Utils.intValue(value));
		}
			break;
			
		case DamAdd: {
			dProp.setDamAdd(Utils.intValue(value));
		}
			break;
		case DamAddEx: {
			dProp.setDamAddEx(Utils.intValue(value));
		}
			break;
		case DamRed: {
			dProp.setDamRed(Utils.intValue(value));
		}
			break;
		case DamRedEx: {
			dProp.setDamRedEx(Utils.intValue(value));
		}
			break;
		case DamPhyAdd: {
			dProp.setDamPhyAdd(Utils.intValue(value));
		}
			break;
		case DamPhyAddEx: {
			dProp.setDamPhyAddEx(Utils.intValue(value));
		}
			break;
		case DamPhyRed: {
			dProp.setDamPhyRed(Utils.intValue(value));
		}
			break;
		case DamPhyRedEx: {
			dProp.setDamPhyRedEx(Utils.intValue(value));
		}
			break;
		case DamMagAdd: {
			dProp.setDamMagAdd(Utils.intValue(value));
		}
			break;
		case DamMagAddEx: {
			dProp.setDamMagAddEx(Utils.intValue(value));
		}
			break;
		case DamMagRed: {
			dProp.setDamMagRed(Utils.intValue(value));
		}
			break;
		case DamMagRedEx: {
			dProp.setDamMagRedEx(Utils.intValue(value));
		}
			break;
		case DamComAdd: {
			dProp.setDamComAdd(Utils.intValue(value));
		}
			break;
		case DamComRed: {
			dProp.setDamComRed(Utils.intValue(value));
		}
			break;
		case DamRageAdd: {
			dProp.setDamRageAdd(Utils.intValue(value));
		}
			break;
		case DamRageRed: {
			dProp.setDamRageRed(Utils.intValue(value));
		}
			break;

		case CureAdd: {
			dProp.setCureAdd(Utils.intValue(value));
		}
			break;
		case CureAddEx: {
			dProp.setCureAddEx(Utils.intValue(value));
		}
			break;
		case HealAdd: {
			dProp.setHealAdd(Utils.intValue(value));
		}
			break;
		case HealAddEx: {
			dProp.setHealAddEx(Utils.intValue(value));
		}
			break;
		case Shield: {
			dProp.setShield(Utils.intValue(value));
		}
			break;
		case ShieldPhy: {
			dProp.setShieldPhy(Utils.intValue(value));
		}
			break;
		case ShieldMag: {
			dProp.setShieldMag(Utils.intValue(value));
		}
			break;
		case DamBack: {
			dProp.setDamBack(Utils.intValue(value));
		}
			break;
		case PoisonAdd: {
			dProp.setPoisonAdd(Utils.intValue(value));
		}
			break;
		case PoisonAddEx: {
			dProp.setPoisonAddEx(Utils.intValue(value));
		}
			break;
		case AntiPoisonAdd: {
			dProp.setAntiPoisonAdd(Utils.intValue(value));
		}
			break;
		case AntiPoisonAddEx: {
			dProp.setAntiPoisonAddEx(Utils.intValue(value));
		}
			break;
		case BurnAdd: {
			dProp.setBurnAdd(Utils.intValue(value));
		}
			break;
		case BurnAddEx: {
			dProp.setBurnAddEx(Utils.intValue(value));
		}
			break;
		case AntiBurnAdd: {
			dProp.setAntiBurnAdd(Utils.intValue(value));
		}
			break;
		case AntiBurnAddEx: {
			dProp.setAntiBurnAddEx(Utils.intValue(value));
		}
			break;
		case BloodAdd: {
			dProp.setBloodAdd(Utils.intValue(value));
		}
			break;
		case BloodAddEx: {
			dProp.setBloodAddEx(Utils.intValue(value));
		}
			break;
		case AntiBloodAdd: {
			dProp.setAntiBloodAdd(Utils.intValue(value));
		}
			break;
		case AntiBloodAddEx: {
			dProp.setAntiBloodAddEx(Utils.intValue(value));
		}
			break;
		case Stun: {
			dProp.setStun(Utils.intValue(value));
		}
			break;
		case Chaos: {
			dProp.setChaos(Utils.intValue(value));
		}
			break;
		case BanHeal: {
			dProp.setBanHeal(Utils.intValue(value));
		}
			break;
		case Paralytic: {
			dProp.setParalytic(Utils.intValue(value));
		}
			break;
		case BanRage: {
			dProp.setBanRage(Utils.intValue(value));
		}
			break;
		case Silent: {
			dProp.setSilent(Utils.intValue(value));
		}
			break;
		case Immortal: {
			dProp.setImmortal(Utils.intValue(value));
		}
			break;
		case ImmunePhy: {
			dProp.setImmunePhy(Utils.intValue(value));
		}
			break;
		case ImmuneMag: {
			dProp.setImmuneMag(Utils.intValue(value));
		}
			break;
		case Invincible: {
			dProp.setInvincible(Utils.intValue(value));
		}
			break;
		case CertainlyHit: {
			dProp.setCertainlyHit(Utils.intValue(value));
		}
			break;
		case CertainlyControl: {
			dProp.setCertainlyControl(Utils.intValue(value));
		}
			break;
		case Weak: {
			dProp.setWeak(Utils.intValue(value));
		}
			break;
		default: {
			if (!type.isFightProp()) {
				Log.human.error("===FightProp no case type={}", type.name());
			}
		}
			break;
		}
	}

	/**
	 * 获取Unit的属性信息
	 * 
	 * @param unit
	 * @return
	 */
	public DProp getDProp(Unit unit) {
		DProp.Builder dProp = DProp.newBuilder();
		if (unit == null) {
			return dProp.build();
		}
		for (FightPropName type : FightPropName.values()) {
			switch (type) {
			case HpCur: {
			}
				break;
			case HpMax: {
				dProp.setHpMax(unit.getHpMax());
			}
				break;
			case HpMaxPct: {
				dProp.setHpMaxPct(unit.getHpMaxPct());
			}
				break;
			case HpMaxEx: {
				dProp.setHpMaxEx(unit.getHpMaxEx());
			}
				break;
			case RageCur: {
				dProp.setRageCur(unit.getRageCur());
			}
				break;
			case RageMax: {
				dProp.setRageMax(unit.getRageMax());
			}
				break;
			case Atk: {
				dProp.setAtk(unit.getAtk());
			}
				break;
			case AtkPct: {
				dProp.setAtkPct(unit.getAtkPct());
			}
				break;
			case AtkEx: {
				dProp.setAtkEx(unit.getAtkEx());
			}
				break;
			case AtkPhy: {
				dProp.setAtkPhy(unit.getAtkPhy());
			}
				break;
			case AtkPhyPct: {
				dProp.setAtkPhyPct(unit.getAtkPhyPct());
			}
				break;
			case AtkPhyEx: {
				dProp.setAtkPhyEx(unit.getAtkPhyEx());
			}
				break;
			case AtkMag: {
				dProp.setAtkMag(unit.getAtkMag());
			}
				break;
			case AtkMagPct: {
				dProp.setAtkMagPct(unit.getAtkMagPct());
			}
				break;
			case AtkMagEx: {
				dProp.setAtkMagEx(unit.getAtkMagEx());
			}
				break;
			case Def: {
				dProp.setDef(unit.getDef());
			}
				break;
			case DefPct: {
				dProp.setDefPct(unit.getDefPct());
			}
				break;
			case DefEx: {
				dProp.setDefEx(unit.getDefEx());
			}
				break;
			case DefPhy: {
				dProp.setDefPhy(unit.getDefPhy());
			}
				break;
			case DefPhyPct: {
				dProp.setDefPhyPct(unit.getDefPhyPct());
			}
				break;
			case DefPhyEx: {
				dProp.setDefPhyEx(unit.getDefPhyEx());
			}
				break;
			case DefMag: {
				dProp.setDefMag(unit.getDefMag());
			}
				break;
			case DefMagPct: {
				dProp.setDefMagPct(unit.getDefMagPct());
			}
				break;
			case DefMagEx: {
				dProp.setDefMagEx(unit.getDefMagEx());
			}
				break;
			case Hit: {
				dProp.setHit(unit.getHit());
			}
				break;
			case Dodge: {
				dProp.setDodge(unit.getDodge());
			}
				break;
			case Crit: {
				dProp.setCrit(unit.getCrit());
			}
				break;
			case AntiCrit: {
				dProp.setAntiCrit(unit.getAntiCrit());
			}
				break;
			case CritAdd: {
				dProp.setCritAdd(unit.getCritAdd());
			}
				break;
			case AntiCritAdd: {
				dProp.setAntiCritAdd(unit.getAntiCritAdd());
			}
				break;
			case Pene: {
				dProp.setPene(unit.getPene());
			}
				break;
			case PenePhy: {
				dProp.setPenePhy(unit.getPenePhy());
			}
				break;
			case PeneMag: {
				dProp.setPeneMag(unit.getPeneMag());
			}
				break;
			case Block: {
				dProp.setBlock(unit.getBlock());
			}
				break;
			case AntiBlock: {
				dProp.setAntiBlock(unit.getAntiBlock());
			}
				break;
			case BloodSuck: {
				dProp.setBloodSuck(unit.getBloodSuck());
			}
				break;
			case BloodSucked: {
				// dProp.setBloodSucked(unit.getBloodSucked());
			}
				break;
			case Control: {
				dProp.setControl(unit.getControl());
			}
				break;
			case AntiControl: {
				dProp.setAntiControl(unit.getAntiControl());
			}
				break;
				
			case DamAdd: {
				dProp.setDamAdd(unit.getDamAdd());
			}
				break;
			case DamAddEx: {
				dProp.setDamAddEx(unit.getDamAddEx());
			}
				break;
			case DamRed: {
				dProp.setDamRed(unit.getDamRed());
			}
				break;
			case DamRedEx: {
				dProp.setDamRedEx(unit.getDamRedEx());
			}
				break;
			case DamPhyAdd: {
				dProp.setDamPhyAdd(unit.getDamPhyAdd());
			}
				break;
			case DamPhyAddEx: {
				dProp.setDamPhyAddEx(unit.getDamPhyAddEx());
			}
				break;
			case DamPhyRed: {
				dProp.setDamPhyRed(unit.getDamPhyRed());
			}
				break;
			case DamPhyRedEx: {
				dProp.setDamPhyRedEx(unit.getDamPhyRedEx());
			}
				break;
			case DamMagAdd: {
				dProp.setDamMagAdd(unit.getDamMagAdd());
			}
				break;
			case DamMagAddEx: {
				dProp.setDamMagAddEx(unit.getDamMagAddEx());
			}
				break;
			case DamMagRed: {
				dProp.setDamMagRed(unit.getDamMagRed());
			}
				break;
			case DamMagRedEx: {
				dProp.setDamMagRedEx(unit.getDamMagRedEx());
			}
				break;
			case DamComAdd: {
				dProp.setDamComAdd(unit.getDamComAdd());
			}
				break;
			case DamComRed: {
				dProp.setDamComRed(unit.getDamComRed());
			}
				break;
			case DamRageAdd: {
				dProp.setDamRageAdd(unit.getDamRageAdd());
			}
				break;
			case DamRageRed: {
				dProp.setDamRageRed(unit.getDamRageRed());
			}
				break;
			case CureAdd: {
				dProp.setCureAdd(unit.getCureAdd());
			}
				break;
			case CureAddEx: {
				dProp.setCureAddEx(unit.getCureAddEx());
			}
				break;
			case HealAdd: {
				dProp.setHealAdd(unit.getHealAdd());
			}
				break;
			case HealAddEx: {
				dProp.setHealAddEx(unit.getHealAddEx());
			}
				break;
			case Shield: {
				dProp.setShield(unit.getShield());
			}
				break;
			case ShieldPhy: {
				dProp.setShieldPhy(unit.getShieldPhy());
			}
				break;
			case ShieldMag: {
				dProp.setShieldMag(unit.getShieldMag());
			}
				break;
			case DamBack: {
				dProp.setDamBack(unit.getDamBack());
			}
				break;
			case PoisonAdd: {
				dProp.setPoisonAdd(unit.getPoisonAdd());
			}
				break;
			case PoisonAddEx: {
				dProp.setPoisonAddEx(unit.getPoisonAddEx());
			}
				break;
			case AntiPoisonAdd: {
				dProp.setAntiPoisonAdd(unit.getAntiPoisonAdd());
			}
				break;
			case AntiPoisonAddEx: {
				dProp.setAntiPoisonAddEx(unit.getAntiPoisonAddEx());
			}
				break;
			case BurnAdd: {
				dProp.setBurnAdd(unit.getBurnAdd());
			}
				break;
			case BurnAddEx: {
				dProp.setBurnAddEx(unit.getBurnAddEx());
			}
				break;
			case AntiBurnAdd: {
				dProp.setAntiBurnAdd(unit.getAntiBurnAdd());
			}
				break;
			case AntiBurnAddEx: {
				dProp.setAntiBurnAddEx(unit.getAntiBurnAddEx());
			}
				break;
			case BloodAdd: {
				dProp.setBloodAdd(unit.getBloodAdd());
			}
				break;
			case BloodAddEx: {
				dProp.setBloodAddEx(unit.getBloodAddEx());
			}
				break;
			case AntiBloodAdd: {
				dProp.setAntiBloodAdd(unit.getAntiBloodAdd());
			}
				break;
			case AntiBloodAddEx: {
				dProp.setAntiBloodAddEx(unit.getAntiBloodAddEx());
			}
				break;
			case Stun: {
				dProp.setStun(unit.getStun());
			}
				break;
			case Chaos: {
				dProp.setChaos(unit.getChaos());
			}
				break;
			case BanHeal: {
				dProp.setBanHeal(unit.getBanHeal());
			}
				break;
			case Paralytic: {
				dProp.setParalytic(unit.getParalytic());
			}
				break;
			case BanRage: {
				dProp.setBanRage(unit.getBanRage());
			}
				break;
			case Silent: {
				dProp.setSilent(unit.getSilent());
			}
				break;
			case Immortal: {
				dProp.setImmortal(unit.getImmortal());
			}
				break;
			case ImmunePhy: {
				dProp.setImmunePhy(unit.getImmunePhy());
			}
				break;
			case ImmuneMag: {
				dProp.setImmuneMag(unit.getImmuneMag());
			}
				break;
			case Invincible: {
				dProp.setInvincible(unit.getInvincible());
			}
				break;
			case CertainlyHit: {
				dProp.setCertainlyHit(unit.getCertainlyHit());
			}
				break;
			case CertainlyControl: {
				dProp.setCertainlyControl(unit.getCertainlyControl());
			}
				break;
			case Weak: {
				dProp.setWeak(unit.getWeak());
			}
				break;
			default: {
				if (!type.isFightProp()) {
					Log.human.error("===FightProp no case type={}", type.name());
				}
			}
				break;
			}
		}
		return dProp.build();
	}

	/**
	 * 获取DProp协议
	 * 
	 * @return
	 */
	public DProp getDProp(String[] propName, int[] propValue) {
		DProp.Builder dProp = DProp.newBuilder();

		PropCalc propCalc = new PropCalc(Utils.toJSONString(propName, propValue));
		for (FightPropName type : FightPropName.values()) {
			switch (type) {
			case HpCur: {
			}
				break;
			case HpMax: {
				dProp.setHpMax(propCalc.getInt(FightPropName.HpMax));
			}
				break;
			case HpMaxPct: {
				dProp.setHpMaxPct(propCalc.getInt(FightPropName.HpMaxPct));
			}
				break;
			case HpMaxEx: {
				dProp.setHpMaxEx(propCalc.getInt(FightPropName.HpMaxEx));
			}
				break;
			case RageCur: {
				dProp.setRageCur(propCalc.getInt(FightPropName.RageCur));
			}
				break;
			case RageMax: {
				dProp.setRageMax(propCalc.getInt(FightPropName.RageMax));
			}
				break;
			case Atk: {
				dProp.setAtk(propCalc.getInt(FightPropName.Atk));
			}
				break;
			case AtkPct: {
				dProp.setAtkPct(propCalc.getInt(FightPropName.AtkPct));
			}
				break;
			case AtkEx: {
				dProp.setAtkEx(propCalc.getInt(FightPropName.AtkEx));
			}
				break;
			case AtkPhy: {
				dProp.setAtkPhy(propCalc.getInt(FightPropName.AtkPhy));
			}
				break;
			case AtkPhyPct: {
				dProp.setAtkPhyPct(propCalc.getInt(FightPropName.AtkPhyPct));
			}
				break;
			case AtkPhyEx: {
				dProp.setAtkPhyEx(propCalc.getInt(FightPropName.AtkPhyEx));
			}
				break;
			case AtkMag: {
				dProp.setAtkMag(propCalc.getInt(FightPropName.AtkMag));
			}
				break;
			case AtkMagPct: {
				dProp.setAtkMagPct(propCalc.getInt(FightPropName.AtkMagPct));
			}
				break;
			case AtkMagEx: {
				dProp.setAtkMagEx(propCalc.getInt(FightPropName.AtkMagEx));
			}
				break;
			case Def: {
				dProp.setDef(propCalc.getInt(FightPropName.Def));
			}
				break;
			case DefPct: {
				dProp.setDefPct(propCalc.getInt(FightPropName.DefPct));
			}
				break;
			case DefEx: {
				dProp.setDefEx(propCalc.getInt(FightPropName.DefEx));
			}
				break;
			case DefPhy: {
				dProp.setDefPhy(propCalc.getInt(FightPropName.DefPhy));
			}
				break;
			case DefPhyPct: {
				dProp.setDefPhyPct(propCalc.getInt(FightPropName.DefPhyPct));
			}
				break;
			case DefPhyEx: {
				dProp.setDefPhyEx(propCalc.getInt(FightPropName.DefPhyEx));
			}
				break;
			case DefMag: {
				dProp.setDefMag(propCalc.getInt(FightPropName.DefMag));
			}
				break;
			case DefMagPct: {
				dProp.setDefMagPct(propCalc.getInt(FightPropName.DefMagPct));
			}
				break;
			case DefMagEx: {
				dProp.setDefMagEx(propCalc.getInt(FightPropName.DefMagEx));
			}
				break;
			case Hit: {
				dProp.setHit(propCalc.getInt(FightPropName.Hit));
			}
				break;
			case Dodge: {
				dProp.setDodge(propCalc.getInt(FightPropName.Dodge));
			}
				break;
			case Crit: {
				dProp.setCrit(propCalc.getInt(FightPropName.Crit));
			}
				break;
			case AntiCrit: {
				dProp.setAntiCrit(propCalc.getInt(FightPropName.AntiCrit));
			}
				break;
			case CritAdd: {
				dProp.setCritAdd(propCalc.getInt(FightPropName.CritAdd));
			}
				break;
			case AntiCritAdd: {
				dProp.setAntiCritAdd(propCalc.getInt(FightPropName.AntiCritAdd));
			}
				break;
			case Pene: {
				dProp.setPene(propCalc.getInt(FightPropName.Pene));
			}
				break;
			case PenePhy: {
				dProp.setPenePhy(propCalc.getInt(FightPropName.PenePhy));
			}
				break;
			case PeneMag: {
				dProp.setPeneMag(propCalc.getInt(FightPropName.PeneMag));
			}
				break;
			case Block: {
				dProp.setBlock(propCalc.getInt(FightPropName.Block));
			}
				break;
			case AntiBlock: {
				dProp.setAntiBlock(propCalc.getInt(FightPropName.AntiBlock));
			}
				break;
			case BloodSuck: {
				dProp.setBloodSuck(propCalc.getInt(FightPropName.BloodSuck));
			}
				break;
			case BloodSucked: {
				// dProp.setBloodSucked(propCalc.getInt(FightPropName.BloodSucked));
			}
				break;
			case Control: {
				dProp.setControl(propCalc.getInt(FightPropName.Control));
			}
				break;
			case AntiControl: {
				 dProp.setAntiControl(propCalc.getInt(FightPropName.AntiControl));
			}
				break;
				
			case DamAdd: {
				dProp.setDamAdd(propCalc.getInt(FightPropName.DamAdd));
			}
				break;
			case DamAddEx: {
				dProp.setDamAddEx(propCalc.getInt(FightPropName.DamAddEx));
			}
				break;
			case DamRed: {
				dProp.setDamRed(propCalc.getInt(FightPropName.DamRed));
			}
				break;
			case DamRedEx: {
				dProp.setDamRedEx(propCalc.getInt(FightPropName.DamRedEx));
			}
				break;
			case DamPhyAdd: {
				dProp.setDamPhyAdd(propCalc.getInt(FightPropName.DamPhyAdd));
			}
				break;
			case DamPhyAddEx: {
				dProp.setDamPhyAddEx(propCalc.getInt(FightPropName.DamPhyAddEx));
			}
				break;
			case DamPhyRed: {
				dProp.setDamPhyRed(propCalc.getInt(FightPropName.DamPhyRed));
			}
				break;
			case DamPhyRedEx: {
				dProp.setDamPhyRedEx(propCalc.getInt(FightPropName.DamPhyRedEx));
			}
				break;
			case DamMagAdd: {
				dProp.setDamMagAdd(propCalc.getInt(FightPropName.DamMagAdd));
			}
				break;
			case DamMagAddEx: {
				dProp.setDamMagAddEx(propCalc.getInt(FightPropName.DamMagAddEx));
			}
				break;
			case DamMagRed: {
				dProp.setDamMagRed(propCalc.getInt(FightPropName.DamMagRed));
			}
				break;
			case DamMagRedEx: {
				dProp.setDamMagRedEx(propCalc.getInt(FightPropName.DamMagRedEx));
			}
				break;
			case DamComAdd: {
				dProp.setDamComAdd(propCalc.getInt(FightPropName.DamComAdd));
			}
				break;
			case DamComRed: {
				dProp.setDamComRed(propCalc.getInt(FightPropName.DamComRed));
			}
				break;
			case DamRageAdd: {
				dProp.setDamRageAdd(propCalc.getInt(FightPropName.DamRageAdd));
			}
				break;
			case DamRageRed: {
				dProp.setDamRageRed(propCalc.getInt(FightPropName.DamRageRed));
			}
				break;

			case CureAdd: {
				dProp.setCureAdd(propCalc.getInt(FightPropName.CureAdd));
			}
				break;
			case CureAddEx: {
				dProp.setCureAddEx(propCalc.getInt(FightPropName.CureAddEx));
			}
				break;
			case HealAdd: {
				dProp.setHealAdd(propCalc.getInt(FightPropName.HealAdd));
			}
				break;
			case HealAddEx: {
				dProp.setHealAddEx(propCalc.getInt(FightPropName.HealAddEx));
			}
				break;
			case Shield: {
				dProp.setShield(propCalc.getInt(FightPropName.Shield));
			}
				break;
			case ShieldPhy: {
				dProp.setShieldPhy(propCalc.getInt(FightPropName.ShieldPhy));
			}
				break;
			case ShieldMag: {
				dProp.setShieldMag(propCalc.getInt(FightPropName.ShieldMag));
			}
				break;
			case DamBack: {
				dProp.setDamBack(propCalc.getInt(FightPropName.DamBack));
			}
				break;
			case PoisonAdd: {
				dProp.setPoisonAdd(propCalc.getInt(FightPropName.PoisonAdd));
			}
				break;
			case PoisonAddEx: {
				dProp.setPoisonAddEx(propCalc.getInt(FightPropName.PoisonAddEx));
			}
				break;
			case AntiPoisonAdd: {
				dProp.setAntiPoisonAdd(propCalc.getInt(FightPropName.AntiPoisonAdd));
			}
				break;
			case AntiPoisonAddEx: {
				dProp.setAntiPoisonAddEx(propCalc.getInt(FightPropName.AntiPoisonAddEx));
			}
				break;
			case BurnAdd: {
				dProp.setBurnAdd(propCalc.getInt(FightPropName.BurnAdd));
			}
				break;
			case BurnAddEx: {
				dProp.setBurnAddEx(propCalc.getInt(FightPropName.BurnAddEx));
			}
				break;
			case AntiBurnAdd: {
				dProp.setAntiBurnAdd(propCalc.getInt(FightPropName.AntiBurnAdd));
			}
				break;
			case AntiBurnAddEx: {
				dProp.setAntiBurnAddEx(propCalc.getInt(FightPropName.AntiBurnAddEx));
			}
				break;
			case BloodAdd: {
				dProp.setBloodAdd(propCalc.getInt(FightPropName.BloodAdd));
			}
				break;
			case BloodAddEx: {
				dProp.setBloodAddEx(propCalc.getInt(FightPropName.BloodAddEx));
			}
				break;
			case AntiBloodAdd: {
				dProp.setAntiBloodAdd(propCalc.getInt(FightPropName.AntiBloodAdd));
			}
				break;
			case AntiBloodAddEx: {
				dProp.setAntiBloodAddEx(propCalc.getInt(FightPropName.AntiBloodAddEx));
			}
				break;
			case Stun: {
				dProp.setStun(propCalc.getInt(FightPropName.Stun));
			}
				break;
			case Chaos: {
				dProp.setChaos(propCalc.getInt(FightPropName.Chaos));
			}
				break;
			case BanHeal: {
				dProp.setBanHeal(propCalc.getInt(FightPropName.BanHeal));
			}
				break;
			case Paralytic: {
				dProp.setParalytic(propCalc.getInt(FightPropName.Paralytic));
			}
				break;
			case BanRage: {
				dProp.setBanRage(propCalc.getInt(FightPropName.BanRage));
			}
				break;
			case Silent: {
				dProp.setSilent(propCalc.getInt(FightPropName.Silent));
			}
				break;
			case Immortal: {
				dProp.setImmortal(propCalc.getInt(FightPropName.Immortal));
			}
				break;
			case ImmunePhy: {
				dProp.setImmunePhy(propCalc.getInt(FightPropName.ImmunePhy));
			}
				break;
			case ImmuneMag: {
				dProp.setImmuneMag(propCalc.getInt(FightPropName.ImmuneMag));
			}
				break;
			case Invincible: {
				dProp.setInvincible(propCalc.getInt(FightPropName.Invincible));
			}
				break;
			case CertainlyHit: {
				dProp.setCertainlyHit(propCalc.getInt(FightPropName.CertainlyHit));
			}
				break;
			case CertainlyControl: {
				dProp.setCertainlyControl(propCalc.getInt(FightPropName.CertainlyControl));
			}
				break;
			case Weak: {
				dProp.setWeak(propCalc.getInt(FightPropName.Weak));
			}
				break;
			default: {
				if (!type.isFightProp()) {
					Log.human.error("===FightProp no case type={}", type.name());
				}
			}
				break;
			}
		}
		return dProp.build();
	}

	/**
	 * 获取unit的所有属性，即FightProp定义的所有属性
	 * 
	 * @param unit
	 * @return PropCalc={"hpMax":999,...}
	 */
	public PropCalc getPropCalc(Unit unit) {
		PropCalc prop = new PropCalc();
		for (FightPropName type : FightPropName.values()) {
			switch (type) {
			case HpCur: {
			}
				break;
			case HpMax: {
				prop.put(type, unit.getHpMax());
			}
				break;
			case HpMaxPct: {
				prop.put(type, unit.getHpMaxPct());
			}
				break;
			case HpMaxEx: {
				prop.put(type, unit.getHpMaxEx());
			}
				break;
			case RageCur: {
				prop.put(type, unit.getRageCur());
			}
				break;
			case RageMax: {
				prop.put(type, unit.getRageMax());
			}
				break;
			case Atk: {
				prop.put(type, unit.getAtk());
			}
				break;
			case AtkPct: {
				prop.put(type, unit.getAtkPct());
			}
				break;
			case AtkEx: {
				prop.put(type, unit.getAtkEx());
			}
				break;
			case AtkPhy: {
				prop.put(type, unit.getAtkPhy());
			}
				break;
			case AtkPhyPct: {
				prop.put(type, unit.getAtkPhyPct());
			}
				break;
			case AtkPhyEx: {
				prop.put(type, unit.getAtkPhyEx());
			}
				break;
			case AtkMag: {
				prop.put(type, unit.getAtkMag());
			}
				break;
			case AtkMagPct: {
				prop.put(type, unit.getAtkMagPct());
			}
				break;
			case AtkMagEx: {
				prop.put(type, unit.getAtkMagEx());
			}
				break;
			case Def: {
				prop.put(type, unit.getDef());
			}
				break;
			case DefPct: {
				prop.put(type, unit.getDefPct());
			}
				break;
			case DefEx: {
				prop.put(type, unit.getDefEx());
			}
				break;
			case DefPhy: {
				prop.put(type, unit.getDefPhy());
			}
				break;
			case DefPhyPct: {
				prop.put(type, unit.getDefPhyPct());
			}
				break;
			case DefPhyEx: {
				prop.put(type, unit.getDefPhyEx());
			}
				break;
			case DefMag: {
				prop.put(type, unit.getDefMag());
			}
				break;
			case DefMagPct: {
				prop.put(type, unit.getDefMagPct());
			}
				break;
			case DefMagEx: {
				prop.put(type, unit.getDefMagEx());
			}
				break;
			case Hit: {
				prop.put(type, unit.getHit());
			}
				break;
			case Dodge: {
				prop.put(type, unit.getDodge());
			}
				break;
			case Crit: {
				prop.put(type, unit.getCrit());
			}
				break;
			case AntiCrit: {
				prop.put(type, unit.getAntiCrit());
			}
				break;
			case CritAdd: {
				prop.put(type, unit.getCritAdd());
			}
				break;
			case AntiCritAdd: {
				prop.put(type, unit.getAntiCritAdd());
			}
				break;
			case Pene: {
				prop.put(type, unit.getPene());
			}
				break;
			case PenePhy: {
				prop.put(type, unit.getPenePhy());
			}
				break;
			case PeneMag: {
				prop.put(type, unit.getPeneMag());
			}
				break;
			case Block: {
				prop.put(type, unit.getBlock());
			}
				break;
			case AntiBlock: {
				prop.put(type, unit.getAntiBlock());
			}
				break;
			case BloodSuck: {
				prop.put(type, unit.getBloodSuck());
			}
				break;
			case BloodSucked: {
				// prop.put(type, unit.getBloodSucked());
			}
				break;
			case Control: {
				prop.put(type, unit.getControl());
			}
				break;
			case AntiControl: {
				prop.put(type, unit.getAntiControl());
			}
				break;
				
			case DamAdd: {
				prop.put(type, unit.getDamAdd());
			}
				break;
			case DamAddEx: {
				prop.put(type, unit.getDamAddEx());
			}
				break;
			case DamRed: {
				prop.put(type, unit.getDamRed());
			}
				break;
			case DamRedEx: {
				prop.put(type, unit.getDamRedEx());
			}
				break;
			case DamPhyAdd: {
				prop.put(type, unit.getDamPhyAdd());
			}
				break;
			case DamPhyAddEx: {
				prop.put(type, unit.getDamPhyAddEx());
			}
				break;
			case DamPhyRed: {
				prop.put(type, unit.getDamPhyRed());
			}
				break;
			case DamPhyRedEx: {
				prop.put(type, unit.getDamPhyRedEx());
			}
				break;
			case DamMagAdd: {
				prop.put(type, unit.getDamMagAdd());
			}
				break;
			case DamMagAddEx: {
				prop.put(type, unit.getDamMagAddEx());
			}
				break;
			case DamMagRed: {
				prop.put(type, unit.getDamMagRed());
			}
				break;
			case DamMagRedEx: {
				prop.put(type, unit.getDamMagRedEx());
			}
				break;
			case DamComAdd: {
				prop.put(type, unit.getDamComAdd());
			}
				break;
			case DamComRed: {
				prop.put(type, unit.getDamComRed());
			}
				break;
			case DamRageAdd: {
				prop.put(type, unit.getDamRageAdd());
			}
				break;
			case DamRageRed: {
				prop.put(type, unit.getDamRageRed());
			}
				break;

			case CureAdd: {
				prop.put(type, unit.getCureAdd());
			}
				break;
			case CureAddEx: {
				prop.put(type, unit.getCureAddEx());
			}
				break;
			case HealAdd: {
				prop.put(type, unit.getHealAdd());
			}
				break;
			case HealAddEx: {
				prop.put(type, unit.getHealAddEx());
			}
				break;
			case Shield: {
				prop.put(type, unit.getShield());
			}
				break;
			case ShieldPhy: {
				prop.put(type, unit.getShieldPhy());
			}
				break;
			case ShieldMag: {
				prop.put(type, unit.getShieldMag());
			}
				break;
			case DamBack: {
				prop.put(type, unit.getDamBack());
			}
				break;
			case PoisonAdd: {
				prop.put(type, unit.getPoisonAdd());
			}
				break;
			case PoisonAddEx: {
				prop.put(type, unit.getPoisonAddEx());
			}
				break;
			case AntiPoisonAdd: {
				prop.put(type, unit.getAntiPoisonAdd());
			}
				break;
			case AntiPoisonAddEx: {
				prop.put(type, unit.getAntiPoisonAddEx());
			}
				break;
			case BurnAdd: {
				prop.put(type, unit.getBurnAdd());
			}
				break;
			case BurnAddEx: {
				prop.put(type, unit.getBurnAddEx());
			}
				break;
			case AntiBurnAdd: {
				prop.put(type, unit.getAntiBurnAdd());
			}
				break;
			case AntiBurnAddEx: {
				prop.put(type, unit.getAntiBurnAddEx());
			}
				break;
			case BloodAdd: {
				prop.put(type, unit.getBloodAdd());
			}
				break;
			case BloodAddEx: {
				prop.put(type, unit.getBloodAddEx());
			}
				break;
			case AntiBloodAdd: {
				prop.put(type, unit.getAntiBloodAdd());
			}
				break;
			case AntiBloodAddEx: {
				prop.put(type, unit.getAntiBloodAddEx());
			}
				break;
			case Stun: {
				prop.put(type, unit.getStun());
			}
				break;
			case Chaos: {
				prop.put(type, unit.getChaos());
			}
				break;
			case BanHeal: {
				prop.put(type, unit.getBanHeal());
			}
				break;
			case Paralytic: {
				prop.put(type, unit.getParalytic());
			}
				break;
			case BanRage: {
				prop.put(type, unit.getBanRage());
			}
				break;
			case Silent: {
				prop.put(type, unit.getSilent());
			}
				break;
			case Immortal: {
				prop.put(type, unit.getImmortal());
			}
				break;
			case ImmunePhy: {
				prop.put(type, unit.getImmunePhy());
			}
				break;
			case ImmuneMag: {
				prop.put(type, unit.getImmuneMag());
			}
				break;
			case Invincible: {
				prop.put(type, unit.getInvincible());
			}
				break;
			case CertainlyHit: {
				prop.put(type, unit.getCertainlyHit());
			}
				break;
			case CertainlyControl: {
				prop.put(type, unit.getCertainlyControl());
			}
				break;
			case Weak: {
				prop.put(type, unit.getWeak());
			}
				break;
			default: {
				if (!type.isFightProp()) {
					Log.human.error("===FightProp no case type={}", type.name());
				}
			}
				break;
			}
		}
		return prop;
	}

	public String getPropJSON(Unit unit) {
		PropCalc propCalc = getPropCalc(unit);
		return propCalc.toJSONStr();
	}

	/**
	 * 把一个unit赋值给另一个unit
	 * 
	 * @param unitFrom
	 *            源复制体
	 * @param unitTo
	 *            复制目标
	 */
	public void copyUnit(Unit unitFrom, Unit unitTo) {
		unitTo.setName(unitFrom.getName());// 名称
		unitTo.setProfession(unitFrom.getProfession());// 职业
		unitTo.setSex(unitFrom.getSex());// 性别
		unitTo.setLevel(unitFrom.getLevel());// 等级
		unitTo.setCombat(unitFrom.getCombat());// 战斗力

		unitTo.setSn(unitFrom.getSn());// 配表sn
		unitTo.setModelSn(unitFrom.getModelSn());// 模型sn
		unitTo.setDefaultModelSn(unitFrom.getDefaultModelSn()); // 默认模型（时装改变）
		unitTo.setSkillGroupSn(unitFrom.getSkillGroupSn());// 技能组sn
		unitTo.setSkillAllSn(unitFrom.getSkillAllSn());// 所有技能sn

		unitTo.setSpeed(unitFrom.getSpeed());// 移动速度
		// unitTo.setAtkSpeed(unitFrom.getAtkSpeed());// 攻击速度

		unitTo.setAptitude(unitFrom.getAptitude());// 资质

		// 设置属性
		for (FightPropName type : FightPropName.values()) {
			switch (type) {
			case HpCur: {
			}
				break;
			case HpMax: {
				unitTo.setHpMax(unitFrom.getHpMax());
			}
				break;
			case HpMaxPct: {
				unitTo.setHpMaxPct(unitFrom.getHpMaxPct());
			}
				break;
			case HpMaxEx: {
				unitTo.setHpMaxEx(unitFrom.getHpMaxEx());
			}
				break;
			case RageCur: {
				unitTo.setRageCur(unitFrom.getRageCur());
			}
				break;
			case RageMax: {
				unitTo.setRageMax(unitFrom.getRageMax());
			}
				break;
			case Atk: {
				unitTo.setAtk(unitFrom.getAtk());
			}
				break;
			case AtkPct: {
				unitTo.setAtkPct(unitFrom.getAtkPct());
			}
				break;
			case AtkEx: {
				unitTo.setAtkEx(unitFrom.getAtkEx());
			}
				break;
			case AtkPhy: {
				unitTo.setAtkPhy(unitFrom.getAtkPhy());
			}
				break;
			case AtkPhyPct: {
				unitTo.setAtkPhyPct(unitFrom.getAtkPhyPct());
			}
				break;
			case AtkPhyEx: {
				unitTo.setAtkPhyEx(unitFrom.getAtkPhyEx());
			}
				break;
			case AtkMag: {
				unitTo.setAtkMag(unitFrom.getAtkMag());
			}
				break;
			case AtkMagPct: {
				unitTo.setAtkMagPct(unitFrom.getAtkMagPct());
			}
				break;
			case AtkMagEx: {
				unitTo.setAtkMagEx(unitFrom.getAtkMagEx());
			}
				break;
			case Def: {
				unitTo.setDef(unitFrom.getDef());
			}
				break;
			case DefPct: {
				unitTo.setDefPct(unitFrom.getDefPct());
			}
				break;
			case DefEx: {
				unitTo.setDefEx(unitFrom.getDefEx());
			}
				break;
			case DefPhy: {
				unitTo.setDefPhy(unitFrom.getDefPhy());
			}
				break;
			case DefPhyPct: {
				unitTo.setDefPhyPct(unitFrom.getDefPhyPct());
			}
				break;
			case DefPhyEx: {
				unitTo.setDefPhyEx(unitFrom.getDefPhyEx());
			}
				break;
			case DefMag: {
				unitTo.setDefMag(unitFrom.getDefMag());
			}
				break;
			case DefMagPct: {
				unitTo.setDefMagPct(unitFrom.getDefMagPct());
			}
				break;
			case DefMagEx: {
				unitTo.setDefMagEx(unitFrom.getDefMagEx());
			}
				break;
			case Hit: {
				unitTo.setHit(unitFrom.getHit());
			}
				break;
			case Dodge: {
				unitTo.setDodge(unitFrom.getDodge());
			}
				break;
			case Crit: {
				unitTo.setCrit(unitFrom.getCrit());
			}
				break;
			case AntiCrit: {
				unitTo.setAntiCrit(unitFrom.getAntiCrit());
			}
				break;
			case CritAdd: {
				unitTo.setCritAdd(unitFrom.getCritAdd());
			}
				break;
			case AntiCritAdd: {
				unitTo.setAntiCritAdd(unitFrom.getAntiCritAdd());
			}
				break;
			case Pene: {
				unitTo.setPene(unitFrom.getPene());
			}
				break;
			case PenePhy: {
				unitTo.setPenePhy(unitFrom.getPenePhy());
			}
				break;
			case PeneMag: {
				unitTo.setPeneMag(unitFrom.getPeneMag());
			}
				break;
			case Block: {
				unitTo.setBlock(unitFrom.getBlock());
			}
				break;
			case AntiBlock: {
				unitTo.setAntiBlock(unitFrom.getAntiBlock());
			}
				break;
			case BloodSuck: {
				unitTo.setBloodSuck(unitFrom.getBloodSuck());
			}
				break;
			case BloodSucked: {
				// unitTo.setBloodSucked(unitFrom.getBloodSucked());
			}
				break;
			case Control: {
				unitTo.setControl(unitFrom.getControl());
			}
				break;
			case AntiControl: {
				unitTo.setAntiControl(unitFrom.getAntiControl());
			}
				break;
				
			case DamAdd: {
				unitTo.setDamAdd(unitFrom.getDamAdd());
			}
				break;
			case DamAddEx: {
				unitTo.setDamAddEx(unitFrom.getDamAddEx());
			}
				break;
			case DamRed: {
				unitTo.setDamRed(unitFrom.getDamRed());
			}
				break;
			case DamRedEx: {
				unitTo.setDamRedEx(unitFrom.getDamRedEx());
			}
				break;
			case DamPhyAdd: {
				unitTo.setDamPhyAdd(unitFrom.getDamPhyAdd());
			}
				break;
			case DamPhyAddEx: {
				unitTo.setDamPhyAddEx(unitFrom.getDamPhyAddEx());
			}
				break;
			case DamPhyRed: {
				unitTo.setDamPhyRed(unitFrom.getDamPhyRed());
			}
				break;
			case DamPhyRedEx: {
				unitTo.setDamPhyRedEx(unitFrom.getDamPhyRedEx());
			}
				break;
			case DamMagAdd: {
				unitTo.setDamMagAdd(unitFrom.getDamMagAdd());
			}
				break;
			case DamMagAddEx: {
				unitTo.setDamMagAddEx(unitFrom.getDamMagAddEx());
			}
				break;
			case DamMagRed: {
				unitTo.setDamMagRed(unitFrom.getDamMagRed());
			}
				break;
			case DamMagRedEx: {
				unitTo.setDamMagRedEx(unitFrom.getDamMagRedEx());
			}
				break;
			case DamComAdd: {
				unitTo.setDamComAdd(unitFrom.getDamComAdd());
			}
				break;
			case DamComRed: {
				unitTo.setDamComRed(unitFrom.getDamComRed());
			}
				break;
			case DamRageAdd: {
				unitTo.setDamRageAdd(unitFrom.getDamRageAdd());
			}
				break;
			case DamRageRed: {
				unitTo.setDamRageRed(unitFrom.getDamRageRed());
			}
				break;

			case CureAdd: {
				unitTo.setCureAdd(unitFrom.getCureAdd());
			}
				break;
			case CureAddEx: {
				unitTo.setCureAddEx(unitFrom.getCureAddEx());
			}
				break;
			case HealAdd: {
				unitTo.setHealAdd(unitFrom.getHealAdd());
			}
				break;
			case HealAddEx: {
				unitTo.setHealAddEx(unitFrom.getHealAddEx());
			}
				break;
			case Shield: {
				unitTo.setShield(unitFrom.getShield());
			}
				break;
			case ShieldPhy: {
				unitTo.setShieldPhy(unitFrom.getShieldPhy());
			}
				break;
			case ShieldMag: {
				unitTo.setShieldMag(unitFrom.getShieldMag());
			}
				break;
			case DamBack: {
				unitTo.setDamBack(unitFrom.getDamBack());
			}
				break;
			case PoisonAdd: {
				unitTo.setPoisonAdd(unitFrom.getPoisonAdd());
			}
				break;
			case PoisonAddEx: {
				unitTo.setPoisonAddEx(unitFrom.getPoisonAddEx());
			}
				break;
			case AntiPoisonAdd: {
				unitTo.setAntiPoisonAdd(unitFrom.getAntiPoisonAdd());
			}
				break;
			case AntiPoisonAddEx: {
				unitTo.setAntiPoisonAddEx(unitFrom.getAntiPoisonAddEx());
			}
				break;
			case BurnAdd: {
				unitTo.setBurnAdd(unitFrom.getBurnAdd());
			}
				break;
			case BurnAddEx: {
				unitTo.setBurnAddEx(unitFrom.getBurnAddEx());
			}
				break;
			case AntiBurnAdd: {
				unitTo.setAntiBurnAdd(unitFrom.getAntiBurnAdd());
			}
				break;
			case AntiBurnAddEx: {
				unitTo.setAntiBurnAddEx(unitFrom.getAntiBurnAddEx());
			}
				break;
			case BloodAdd: {
				unitTo.setBloodAdd(unitFrom.getBloodAdd());
			}
				break;
			case BloodAddEx: {
				unitTo.setBloodAddEx(unitFrom.getBloodAddEx());
			}
				break;
			case AntiBloodAdd: {
				unitTo.setAntiBloodAdd(unitFrom.getAntiBloodAdd());
			}
				break;
			case AntiBloodAddEx: {
				unitTo.setAntiBloodAddEx(unitFrom.getAntiBloodAddEx());
			}
				break;
			case Stun: {
				unitTo.setStun(unitFrom.getStun());
			}
				break;
			case Chaos: {
				unitTo.setChaos(unitFrom.getChaos());
			}
				break;
			case BanHeal: {
				unitTo.setBanHeal(unitFrom.getBanHeal());
			}
				break;
			case Paralytic: {
				unitTo.setParalytic(unitFrom.getParalytic());
			}
				break;
			case BanRage: {
				unitTo.setBanRage(unitFrom.getBanRage());
			}
				break;
			case Silent: {
				unitTo.setSilent(unitFrom.getSilent());
			}
				break;
			case Immortal: {
				unitTo.setImmortal(unitFrom.getImmortal());
			}
				break;
			case ImmunePhy: {
				unitTo.setImmunePhy(unitFrom.getImmunePhy());
			}
				break;
			case ImmuneMag: {
				unitTo.setImmuneMag(unitFrom.getImmuneMag());
			}
				break;
			case Invincible: {
				unitTo.setInvincible(unitFrom.getInvincible());
			}
				break;
			case CertainlyHit: {
				unitTo.setCertainlyHit(unitFrom.getCertainlyHit());
			}
				break;
			case CertainlyControl: {
				unitTo.setCertainlyControl(unitFrom.getCertainlyControl());
			}
				break;
			case Weak: {
				unitTo.setWeak(unitFrom.getWeak());
			}
				break;
			default: {
				if (!type.isFightProp()) {
					Log.human.error("===FightProp no case type={}", type.name());
				}
			}
				break;
			}
		}
	}
	
	/**
	 * 单位基础属性 * 系数
	 * @param unit 单位
	 * @param coefficient 系数
	 */
	public void basePropMultiply(Unit unit, double coefficient) {
		if (coefficient == 1) {
			return;
		}
		PropCalc propCalc = getPropCalc(unit);
		propCalc.multiply(FightPropName.HpMax, coefficient);
		propCalc.multiply(FightPropName.Atk, coefficient);
		propCalc.multiply(FightPropName.Def, coefficient);
		propCalc.multiply(FightPropName.DefPhy, coefficient);
		propCalc.multiply(FightPropName.DefMag, coefficient);
		this.setUnitProp(propCalc, unit);
	}

}