package game.worldsrv.tower;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import game.worldsrv.character.UnitManager;
import game.worldsrv.compete.CompeteHumanObj;
import game.worldsrv.entity.CompeteHuman;
import game.worldsrv.entity.CompetePartner;
import game.worldsrv.entity.TowerHuman;
import game.worldsrv.entity.TowerPartner;
import game.worldsrv.human.HumanPlusManager;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.support.Utils;

/**
 * @author Neak
 * @see :爬塔 用于匹配的敌方玩家对象
 */
public class TowerHumanObj implements ISerilizable {

	// 爬塔玩家对象
	private TowerHuman towerHuman;
	// 玩家拥有的伙伴<伙伴id, 伙伴对象>
	private Map<Long, TowerPartner> partnerMap = new HashMap<>();
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(towerHuman);
		out.write(partnerMap);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		towerHuman = in.read();
		partnerMap.clear();
		partnerMap.putAll(in.<Map<Long,TowerPartner>> read());
	}
	
	public TowerHuman getHuman() {
		return towerHuman;
	}
	public void setHuman(TowerHuman human) {
		this.towerHuman = human;
	}

	public Map<Long, TowerPartner> getPartnerMap() {
		return partnerMap;
	}
	public void setPartnerMap(Map<Long, TowerPartner> partnerMap) {
		this.partnerMap = partnerMap;
	}
	
	/**
	 * 获取爬塔玩家总战力
	 */
	public int getCombat() {
		int combat = towerHuman.getCombat();
		List<Long> partnerLineup =  Utils.strToLongList(towerHuman.getPartnerLineup());
		for (TowerPartner tp : partnerMap.values()) {
			// 只有在阵容里的伙伴才要计算战力
			if (partnerLineup.contains(tp.getId())) {
				combat += tp.getCombat();
			}
		}
		return combat;
	}
	
	/**
	 * 复制新数据，数据存在直接更新
	 */
	public void updateFromCompeteHumanObj(CompeteHumanObj cpHumanObj) {
		// 赋值新的主角数据
		copyToTowerHuman(cpHumanObj.cpHuman);
		
		// 伙伴id，竞技场镜像玩家的上阵伙伴
		// 持久化爬塔伙伴镜像数据
		for (CompetePartner cpPartner : cpHumanObj.partnerMap.values()) {
			// 查询该伙伴是否在之前的数据中存在
			TowerPartner thPartner = partnerMap.get(cpPartner.getId());
			
			if (thPartner != null) {
				// 伙伴镜像数据存在则更新数据
				copyToTowerPartner(cpPartner, thPartner);
			} else {
				// 伙伴镜像数据不存在则插入新数据
				persistTowerPartner(cpPartner);	
			}
		}
	}

	/**
	 * 持久化爬塔镜像数据
	 * @param cpHumanObj
	 */
	public void persistFromCompeteHumanObj(CompeteHumanObj cpHumanObj) {
		// 持久化爬塔玩家镜像数据
		persistTowerHuman(cpHumanObj.cpHuman);
		
		// 伙伴id，竞技场镜像玩家的上阵伙伴
		// 持久化爬塔伙伴镜像数据
		for (CompetePartner cpPartner : cpHumanObj.partnerMap.values()) {
			persistTowerPartner(cpPartner);	
		}
	}
	/**
	 * 持久化玩家数据
	 */
	private  void persistTowerHuman(CompeteHuman cpHuman) {
		towerHuman = new TowerHuman();
		// 数据拷贝
		copyToTowerHuman(cpHuman);
		towerHuman.persist();
	}
	/**
	 * 持久化伙伴数据
	 */
	private void persistTowerPartner(CompetePartner cpPartner) {
		TowerPartner thPartner = new TowerPartner();
		copyToTowerPartner(cpPartner, thPartner);
		thPartner.persist();
		partnerMap.put(thPartner.getId(), thPartner);
	}
	
	/**
	 * 设置新的主角数据
	 */
	private void copyToTowerHuman(CompeteHuman cpHuman) {
		// 基础信息/属性
		UnitManager.inst().copyUnit(cpHuman, towerHuman);
		
		// 设置爬塔玩家相关数据
		towerHuman.setId(cpHuman.getId());
		towerHuman.setIsRobot(cpHuman.isIsRobot());
		towerHuman.setTitleSn(cpHuman.getTitleSn());
		towerHuman.setTitleShow(cpHuman.isTitleShow());
		towerHuman.setPartnerLineup(cpHuman.getPartnerLineup());
		towerHuman.setPartnerStance(cpHuman.getPartnerStance());
		// 技能相关
		HumanSkillManager.inst().setHumanMirrorSkill(cpHuman, towerHuman);
		
		// 记录时间
		towerHuman.setRecordTime(Port.getTime());
	}
	/**
	 * 设置新的伙伴数据
	 * @param cpPartner 竞技场镜像，数据源
	 * @param thPartner 爬塔镜像，设置目标
	 */
	private void copyToTowerPartner(CompetePartner cpPartner, TowerPartner thPartner) {
		// 基础信息/属性
		UnitManager.inst().copyUnit(cpPartner, thPartner);
		
		// 设置爬塔伙伴相关数据
		thPartner.setId(cpPartner.getId());
		thPartner.setHumanId(cpPartner.getHumanId());
		thPartner.setExp(cpPartner.getExp());
		thPartner.setStar(cpPartner.getStar());
		thPartner.setAdvLevel(cpPartner.getAdvLevel());
		thPartner.setRelationActive(cpPartner.getRelationActive());
		// 设置技能相关
		HumanPlusManager.inst().setPartnerSkill(cpPartner, thPartner);
		
		// 记录时间
		thPartner.setRecordTime(towerHuman.getRecordTime());
	}
}
