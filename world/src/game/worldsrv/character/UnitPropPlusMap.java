package game.worldsrv.character;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.UnitPropPlus;
import game.worldsrv.support.Log;

public class UnitPropPlusMap implements ISerilizable {
	// 保存数据库记录
	private UnitPropPlus unitPropPlus = new UnitPropPlus();
	// 保存所有属性信息Map<属性字段名, JSON字符串转换的map数据> Map<String, Object>
	public Map<String, String> dataMap = new HashMap<>();
	
	public UnitPropPlusMap() {
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(unitPropPlus);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		unitPropPlus = in.read();
		parse(unitPropPlus);// 解析数据库记录到内存数据
	}
	
	/**
	 * 初始化 在读取数据库以后调用
	 */
	@Deprecated
	public void init(UnitPropPlus unitPropPlus) {
		this.unitPropPlus = unitPropPlus;
		parse(this.unitPropPlus);// 解析数据库记录到内存数据
	}
	
	/**
	 * 解析数据库记录到内存数据
	 */
	@Deprecated
	private void parse(UnitPropPlus unitPropPlus) {
		dataMap.clear();
		for (EntityUnitPropPlus type : EntityUnitPropPlus.values()) {
			switch(type) {
				case Name : {// 名字，非属性字段不处理
				}	break;
				case Type : {// 类型，非属性字段不处理
				}	break;
				case Base : {
					dataMap.put(type.name(), unitPropPlus.getBase());
				}	break;
				case Level : {
					dataMap.put(type.name(), unitPropPlus.getLevel());
				}	break;
				case ItemEquip : {
					dataMap.put(type.name(), unitPropPlus.getItemEquip());
				}	break;
				case EquipRefine : {
					dataMap.put(type.name(), unitPropPlus.getEquipRefine());
				}	break;
				case Rune : {
					dataMap.put(type.name(), unitPropPlus.getRune());
				}	break;
				case Skill : {
					dataMap.put(type.name(), unitPropPlus.getSkill());
				}	break;
				case SkillStage:{
					dataMap.put(type.name(),unitPropPlus.getSkillStage());
				}	break;
				case SkillTrain : {
					dataMap.put(type.name(), unitPropPlus.getSkillTrain());
				}	break;
				case PassivitySkill : {
					dataMap.put(type.name(), unitPropPlus.getPassivitySkill());
				}	break;
				case SkillGodsLv : {
					dataMap.put(type.name(), unitPropPlus.getSkillGodsLv());
				}	break;
				case SkillGodsStar : {
					dataMap.put(type.name(), unitPropPlus.getSkillGodsStar());
				}	break;
				case Title : {
					dataMap.put(type.name(), unitPropPlus.getTitle());
				}	break;
				case Fashion : {
					dataMap.put(type.name(), unitPropPlus.getFashion());
				}	break;
				case FashionHenshin:{
					dataMap.put(type.name(),unitPropPlus.getFashionHenshin());
				}	break;
				case Advance : {
					dataMap.put(type.name(), unitPropPlus.getAdvance());
				}	break;
				case Star : {
					dataMap.put(type.name(), unitPropPlus.getStar());
				}	break;
				case Practice : {
					dataMap.put(type.name(), unitPropPlus.getPractice());
				}	break;
				case Fate: {
					dataMap.put(type.name(), unitPropPlus.getFate());
				}	break;
				case Servant:{
					dataMap.put(type.name(),unitPropPlus.getServant());
				}	break;
				case PartnerSkil:{
					dataMap.put(type.name(),unitPropPlus.getPartnerSkil());
				}	break;
				case CimeliaBase:{
					dataMap.put(type.name(),unitPropPlus.getCimeliaBase());
				}	break;
				case CimeliaLv:{
					dataMap.put(type.name(),unitPropPlus.getCimeliaLv());
				}	break;
				case CimeliaAdv:{
					dataMap.put(type.name(),unitPropPlus.getCimeliaAdv());
				}	break;
				case CimeliaStar:{
					dataMap.put(type.name(),unitPropPlus.getCimeliaStar());
				}	break;
                case GuildSkill:{
                    dataMap.put(type.name(),unitPropPlus.getGuildSkill());
                }	break;

				default : {
					Log.human.error("===EntityUnitPropPlus no case type={}", type.name());
				}	break;
			}
		}
	}
	
	/**
	 * 设置指定属性字段名的数据
	 */
	@Deprecated
	public void set(EntityUnitPropPlus type, String strJSON) {
		if (strJSON == null || strJSON.isEmpty()) {
			strJSON = "{}";
		}
		switch(type) {
			case Name : {// 名字，非属性字段不处理
			}	break;
			case Type : {// 类型，非属性字段不处理
			}	break;
			case Base : {
				unitPropPlus.setBase(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Level : {
				unitPropPlus.setLevel(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case ItemEquip : {
				unitPropPlus.setItemEquip(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case EquipRefine : {
				unitPropPlus.setEquipRefine(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Rune : {
				unitPropPlus.setRune(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Skill : {
				unitPropPlus.setSkill(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case SkillStage:{
				unitPropPlus.setSkillStage(strJSON);
				dataMap.put(type.name(), strJSON);
			} 	break;
			case SkillTrain : {
				unitPropPlus.setSkillTrain(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case PassivitySkill : {
				unitPropPlus.setPassivitySkill(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case SkillGodsLv : {
				unitPropPlus.setSkillGodsLv(strJSON);
				dataMap.put(type.name(), strJSON);
			}   break;
			case SkillGodsStar : {
				unitPropPlus.setSkillGodsStar(strJSON);
				dataMap.put(type.name(), strJSON);
			}   break;
			case Title : {
				unitPropPlus.setTitle(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Fashion : {
				unitPropPlus.setFashion(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case FashionHenshin : {
				unitPropPlus.setFashionHenshin(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Advance : {
				unitPropPlus.setAdvance(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Star : {
				unitPropPlus.setStar(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Practice : {
				unitPropPlus.setPractice(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Fate: {
				unitPropPlus.setFate(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case Servant:{
				unitPropPlus.setServant(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case PartnerSkil:{
				unitPropPlus.setPartnerSkil(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case CimeliaBase:{
				unitPropPlus.setCimeliaBase(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case CimeliaLv:{
				unitPropPlus.setCimeliaLv(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case CimeliaAdv:{
				unitPropPlus.setCimeliaAdv(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
			case CimeliaStar:{
				unitPropPlus.setCimeliaStar(strJSON);
				dataMap.put(type.name(), strJSON);
			}	break;
            case GuildSkill:{
                unitPropPlus.setGuildSkill(strJSON);
                dataMap.put(type.name(), strJSON);
            }	break;
			default : {
				Log.human.error("===EntityUnitPropPlus no case type={}", type.name());
			}	break;
		}
	}
	
	/**
	 * 获取指定属性字段名的JSON数据
	 */
	@Deprecated
	public String get(EntityUnitPropPlus type) {
		String strJSON = "{}";
		switch(type) {
			case Name : {// 名字，非属性字段不处理
			}	break;
			case Type : {// 类型，非属性字段不处理
			}	break;
			case Base : {
				strJSON = unitPropPlus.getBase();
			}	break;
			case Level : {
				strJSON = unitPropPlus.getLevel();
			}	break;
			case ItemEquip : {
				strJSON = unitPropPlus.getItemEquip();
			}	break;
			case EquipRefine : {
				strJSON = unitPropPlus.getEquipRefine();
			}	break;
			case Rune : {
				strJSON = unitPropPlus.getRune();
			}	break;
			case Skill : {
				strJSON = unitPropPlus.getSkill();
			}	break;
			case SkillStage:{
				strJSON = unitPropPlus.getSkillStage();
			}	break;
			case SkillTrain : {
				strJSON = unitPropPlus.getSkillTrain();
			}	break;
			case PassivitySkill : {
				strJSON = unitPropPlus.getPassivitySkill();
			}	break;
			case SkillGodsLv : {
				strJSON = unitPropPlus.getSkillGodsLv();
			}	break;
			case SkillGodsStar : {
				strJSON = unitPropPlus.getSkillGodsStar();
			}	break;
			case Title : {
				strJSON = unitPropPlus.getTitle();
			}	break;
			case Fashion : {
				strJSON = unitPropPlus.getFashion();
			}	break;
			case FashionHenshin: {
				strJSON = unitPropPlus.getFashionHenshin();
			}	break;
			case Advance : {
				strJSON = unitPropPlus.getAdvance();
			}	break;
			case Star : {
				strJSON = unitPropPlus.getStar();
			}	break;
			case Practice : {
				strJSON = unitPropPlus.getPractice();
			}	break;
			case Fate:{
				strJSON = unitPropPlus.getFate();
			}	break;
			case Servant:{
				strJSON = unitPropPlus.getServant();
			}	break;
			case PartnerSkil:{
				strJSON = unitPropPlus.getPartnerSkil();
			}	break;
			case CimeliaBase:{
				strJSON = unitPropPlus.getCimeliaBase();
			}	break;
			case CimeliaLv:{
				strJSON = unitPropPlus.getCimeliaLv();
			}	break;
			case CimeliaAdv:{
				strJSON = unitPropPlus.getCimeliaAdv();
			}	break;
			case CimeliaStar:{
				strJSON = unitPropPlus.getCimeliaStar();
			}	break;
            case GuildSkill:{
                strJSON = unitPropPlus.getGuildSkill();
            }	break;
			default : {
				Log.human.error("===EntityUnitPropPlus no case type={}", type.name());
			}	break;
		}
		return strJSON;
	}
	
	/**
	 * 获取指定属性字段名的Map数据
	 */
	public Map<String, Double> getFrom(EntityUnitPropPlus type) {
		PropCalcCommon propComm = new PropCalcCommon(dataMap.get(type.name()));
		return propComm.getDatas();
	}
	
}
