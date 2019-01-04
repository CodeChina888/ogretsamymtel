package game.worldsrv.human;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.ManagerBase;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfCimeliaConstitutions;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfPartnerConstitutions;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfPartnerStarUp;
import game.worldsrv.entity.CaveHuman;
import game.worldsrv.entity.CavePartner;
import game.worldsrv.entity.Cimelia;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.entity.MirrorHuman;
import game.worldsrv.entity.MirrorPartner;
import game.worldsrv.entity.Partner;
import game.worldsrv.entity.PartnerMirror;
import game.worldsrv.immortalCave.CaveHumanObj;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.partner.PartnerPlusManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

public class HumanPlusManager extends ManagerBase {

	public static HumanPlusManager inst() {
		return inst(HumanPlusManager.class);
	}

	
	
	
	public CaveHumanObj getCaveHumanObject(int instMonsterSn) {
		CaveHumanObj mHumanObject = new CaveHumanObj();
		Map<Long,CavePartner> map = new HashMap<>();
		CaveHuman cpHuman = new CaveHuman();
		// 构建军团数据
		ConfInstMonster confInst = ConfInstMonster.get(instMonsterSn);
		if (null == confInst) {
			Log.human.info("error confInst can't find confInst sn = {}",instMonsterSn);
			return mHumanObject;
		}

		long humanId = cpHuman.getId();
		for (int partnerSn : confInst.monsterIds) {
			ConfPartnerProperty confPartner = ConfPartnerProperty.get(partnerSn);
			if (confPartner == null) {
				// 阵型
				String lineupStr = cpHuman.getPartnerLineup();
				List<Long> pidlist = Utils.strToLongList(lineupStr);
				pidlist.add(0L);
				cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));
				continue;
			}
			long partnerId = Port.applyId();
			if (confPartner.roleType == 0) {
				// 主角
				setMirrorHumanProp(cpHuman, partnerSn);
				cpHuman.setPartnerStance(confInst.lineup);

				String lineupStr = cpHuman.getPartnerLineup();
				List<Long> pidlist = Utils.strToLongList(lineupStr);
				pidlist.add(-1L);
				cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));
				continue;
			}
			// 阵型
			String lineupStr = cpHuman.getPartnerLineup();
			List<Long> pidlist = Utils.strToLongList(lineupStr);
			pidlist.add(partnerId);
			cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));

			CavePartner cpartner = new CavePartner();
			map.put(partnerId,cpartner);
			setMirrorPartner(cpartner, humanId, partnerId, partnerSn);
		}
		
		
		mHumanObject.caveHuman = cpHuman;
		mHumanObject.partnerMap = map;
		
		return mHumanObject;
	}
	
	/**
	 * 
	 * @param instMonsterSn 根据配置表配置镜像数据
	 * @return
	 */
	public HumanMirrorObject getMirrorObject(int instMonsterSn) {
		HumanMirrorObject mHumanObject = new HumanMirrorObject();
		Map<Long,PartnerMirror> map = new HashMap<>();
		HumanMirror cpHuman = new HumanMirror();
		// 构建军团数据
		ConfInstMonster confInst = ConfInstMonster.get(instMonsterSn);
		if (null == confInst) {
			Log.human.info("error confInst can't find confInst sn = {}",instMonsterSn);
			return mHumanObject;
		}

		long humanId = cpHuman.getId();
		for (int partnerSn : confInst.monsterIds) {
			ConfPartnerProperty confPartner = ConfPartnerProperty.get(partnerSn);
			if (confPartner == null) {
				// 阵型
				String lineupStr = cpHuman.getPartnerLineup();
				List<Long> pidlist = Utils.strToLongList(lineupStr);
				pidlist.add(0L);
				cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));
				continue;
			}
			long partnerId = Port.applyId();
			if (confPartner.roleType == 0) {
				// 主角
				setMirrorHumanProp(cpHuman, partnerSn);
				cpHuman.setPartnerStance(confInst.lineup);

				String lineupStr = cpHuman.getPartnerLineup();
				List<Long> pidlist = Utils.strToLongList(lineupStr);
				pidlist.add(-1L);
				cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));
				continue;
			}
			// 阵型
			String lineupStr = cpHuman.getPartnerLineup();
			List<Long> pidlist = Utils.strToLongList(lineupStr);
			pidlist.add(partnerId);
			cpHuman.setPartnerLineup(Utils.ListLongToStr(pidlist));

			PartnerMirror cpartner = new PartnerMirror();
			this.setMirrorPartner(cpartner, humanId, partnerId, partnerSn);
			map.put(partnerId,cpartner);
		}
		
		
		mHumanObject.humanMirror = cpHuman;
		mHumanObject.partnerMirrorMap = map;
		
		return mHumanObject;
	}

	/**
	 * 设置MirrorHuman的prop属性 (机器人用) 爬塔/仙府/竞技场
	 * @return
	 */
	public void setMirrorHumanProp(MirrorHuman mirror, int snProperty) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(snProperty);
		if (null == conf) {
			Log.table.error("===ConfPartnerProperty no find sn={}", snProperty);
			return;
		}
		
		List<String> installs = new ArrayList<>();
		int[] info = null;
		for (int skillSn : conf.skill) {
			info = new int[]{skillSn, mirror.getLevel(), 0, 0};
			installs.add(Utils.arrayIntToStr(info));
		}
		// 根据属性配表设置上阵技能
		mirror.setInstallSkillJSON(Utils.ListStrToStrSplit(installs));
		// 爆点技能 
		int randomIndex = Utils.randomBetween(0, conf.godsSkill.length);
		int[] godsInfo = new int[]{conf.godsSkill[randomIndex], mirror.getLevel(), 0, 0};
		mirror.setInstallGodsJSON(Utils.arrayIntToStr(godsInfo));

		// 根据属性配表设置属性信息
		UnitManager.inst().initProperty(mirror, snProperty);
	}
	
	/**
	 * 制作一个镜像数据
	 */
	public void setMirrorPartner(MirrorPartner mirrorPartner, long humanId, long partnerId, int snProperty) {
		mirrorPartner.setHumanId(humanId);
		mirrorPartner.setId(partnerId);

		// 根据属性配表设置属性信息
		UnitManager.inst().initProperty(mirrorPartner, snProperty);

		ConfPartnerProperty conf = ConfPartnerProperty.get(snProperty);
		if (null == conf) {
			Log.table.error("===ConfPartnerProperty no find sn={}", snProperty);
		} else {
			// 根据属性配表设置上阵技能
			setPartnerSkill(mirrorPartner);
		}
	}
	
	/**
	 * 设置伙伴镜像养成信息
	 * @param humanId 玩家id
	 * @param partnerObj 信息来源
	 * @param mirrorPartner 镜像对象
	 */
	public void copyPartnerObToMirrorPartner(long humanId, PartnerObject partnerObj, MirrorPartner mirrorPartner) {
		// 伙伴信息
		Partner partner = partnerObj.getPartner();
		mirrorPartner.setHumanId(humanId);// 归属玩家ID
		mirrorPartner.setId(partner.getId());// 伙伴ID
		mirrorPartner.setSn(partner.getSn());
		mirrorPartner.setExp(partner.getExp());// 伙伴的经验
		mirrorPartner.setStar(partner.getStar());// 伙伴的星级
		mirrorPartner.setAdvLevel(partner.getAdvLevel());// 进阶品质的等级
		mirrorPartner.setRelationActive(partner.getRelationActive());// 激活的羁绊
		// 法宝信息
		Cimelia cimelia = partnerObj.getCimeLia();
		if (cimelia != null) {
			mirrorPartner.setCimeliaSn(cimelia.getSn());
			mirrorPartner.setCimeliaStar(cimelia.getStar());
			mirrorPartner.setCimeliaAdvLevel(cimelia.getAdvLevel());
		}
		// 上阵技能
		HumanPlusManager.inst().setPartnerSkill(mirrorPartner);
		// 伙伴属性
		UnitManager.inst().copyUnit(partnerObj.getUnit(), mirrorPartner);
	}
	
	/**
	 * 获取伙伴技能，被动技能
	 * @param mirrorPartner
	 * @return
	 */
	private void setPartnerSkill(MirrorPartner mirrorPartner) {
		// 伙伴信息
		int partnerSn = mirrorPartner.getSn();
		int adv = mirrorPartner.getAdvLevel();
		int star = mirrorPartner.getStar();
		
		// 伙伴突破表
		ConfPartnerConstitutions confCons = PartnerManager.inst().getConfPartnerConstitution(partnerSn, adv);
		// 法宝信息
		int cimeliaSn = mirrorPartner.getCimeliaSn();
		int cimeliaAdv = mirrorPartner.getCimeliaAdvLevel();
		// 法宝突破表
		ConfCimeliaConstitutions confCimelia = PartnerManager.inst().getConfCimeliaConstitution(cimeliaSn, cimeliaAdv);
		int[] skillAry = null;
		String passiveSnStr = "";
		// 等级
		int lv = 1;
		if (confCons != null) {
			if (confCimelia != null) {
				// 优先使用法宝养成的技能
				skillAry = confCimelia.skill; 
			} else {
				// 法宝数据不存在使用原始技能相关
				skillAry = confCons.skill;
			}
			// 被动只有伙伴突破会有
			passiveSnStr = Utils.arrayIntToStr(confCons.addSkillId); 
		} else {
			// 基础角色属性表
			ConfPartnerProperty confPP = ConfPartnerProperty.get(partnerSn);
			if (confPP != null) {
				skillAry = confPP.skill;
				List<Integer> passiveSns = new ArrayList<>();
				if(confPP.passiveSkills != null) {
					for (int sn : confPP.passiveSkills) {
						if (sn != 0) {
							passiveSns .add(sn);
						}
					}
				}
				if(confPP.talent != null) {
					for (int sn : confPP.talent) {
						if (sn != 0) {
							passiveSns.add(sn);
						}
					}
				}
				
				passiveSnStr = Utils.intListToStr(passiveSns);
			}
		}
		// 伙伴升星表
		ConfPartnerStarUp confStar = PartnerManager.inst().getConfPartnerStarUp(partnerSn, star);
		
		// 威力
		int power = 0;
		// 固定加成
		int value = 0;
		List<String> installs = new ArrayList<>();
		int[] info = null;
		// 遍历所有技能
		for (int skillSn : skillAry) {
			// 伙伴升星加成的对应技能的威力和固定值
			if (confCons != null) {
				int[] consPower = PartnerPlusManager.inst().getDevelopSkillPowerAndValue(skillSn, 
						confCons.skillType, confCons.power, confCons.value);
				power += consPower[0];
				value += consPower[1];
			}
			// 伙伴法宝加成技能的威力和固定值
			if (confCimelia != null) {
				int[] cimeliaPower = PartnerPlusManager.inst().getDevelopSkillPowerAndValue(skillSn, 
						confCimelia.skillType, confCimelia.power, confCimelia.value);
				power += cimeliaPower[0];
				value += cimeliaPower[1];
			}
			// 伙伴升星加成的对应技能的威力和固定值
			if (confStar != null) {
				int[] starPower = PartnerPlusManager.inst().getDevelopSkillPowerAndValue(skillSn, 
						confStar.skillType, confStar.power, confStar.value);
				power += starPower[0];
				value += starPower[1];
			}
			info = new int[]{skillSn, lv, power, value};
			installs.add(Utils.arrayIntToStr(info));
		}
		
		// 主动技能
		mirrorPartner.setInstallSkillJSON(Utils.ListStrToStrSplit(installs));
		// 被动技能
		mirrorPartner.setPassiveSkill(passiveSnStr);
	}
	
	/**
	 * 拷贝伙伴镜像技能相关
	 * @param sourceMirror 信息来源方
	 * @param targetMirror 
	 */
	public void setPartnerSkill(MirrorPartner sourceMirror, MirrorPartner targetMirror) {
		// 上阵技能
		targetMirror.setInstallSkillJSON(sourceMirror.getInstallSkillJSON());
		// 被动技能
		targetMirror.setPassiveSkill(sourceMirror.getPassiveSkill());
	}
}
