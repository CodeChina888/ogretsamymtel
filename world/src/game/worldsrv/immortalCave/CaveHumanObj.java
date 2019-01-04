package game.worldsrv.immortalCave;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.entity.CaveHuman;
import game.worldsrv.entity.CavePartner;
import game.worldsrv.human.HumanPlusManager;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.support.Log;

public class CaveHumanObj implements ISerilizable {
	public boolean isFight = false;
	public CaveHuman caveHuman;
	// 伙伴信息<partnerId, CompetePartner>
	public Map<Long, CavePartner> partnerMap = new HashMap<>();
	public CaveHumanObj() {
		
	}
	
	/**
	 * 人物进入竞技场转换CompeteHumanObj
	 * @param humanObj
	 */
	public CaveHumanObj(HumanObject humanObj) {
		caveHuman = new CaveHuman();
		long humanId = humanObj.getHumanId();
		caveHuman.setId(humanId);
		caveHuman.setIsRobot(false);
		// 主角属性
		UnitManager.inst().copyUnit(humanObj.getUnit(), caveHuman);
		// 称号信息
		caveHuman.setTitleSn(humanObj.getHuman().getTitleSn());
		caveHuman.setTitleShow(humanObj.getHuman().isTitleShow());
		// 技能相关
		HumanSkillManager.inst().setHumanMirrorSkill(humanObj, caveHuman);
		
		// 上阵伙伴阵容和站位
		caveHuman.setPartnerLineup(humanObj.extInfo.getPartnerLineup());
		caveHuman.setPartnerStance(humanObj.extInfo.getPartnerStance());
		// 上阵伙伴属性
		List<Long> lineup = PartnerManager.inst().getPartnerLineUp(humanObj);
		//工会相关
		caveHuman.setGuiLdId(humanObj.getHuman().getGuildId());
		caveHuman.setGuiLdName(humanObj.getHuman().getGuildName());
		for (Long partnerId : lineup) {
			if(partnerId == 0 || partnerId == -1) {
				// 0为主角，-1为空位置，均需过滤掉
				continue;
			}
			PartnerObject partnerObj = humanObj.partnerMap.get(partnerId);
			if(null == partnerObj) {
				Log.game.info("伙伴不存在！partnerId={},humanId={}", partnerId, humanObj.getHumanId()); 
				continue;
			}
			CavePartner pm = new CavePartner();
			// 将伙伴信息复制给镜像
			HumanPlusManager.inst().copyPartnerObToMirrorPartner(humanId, partnerObj, pm);
			partnerMap.put(pm.getId(), pm);
		}
	}
	
	
	public CaveHuman getCaveHuman() {
		return caveHuman;
	}

	public void setCaveHuman(CaveHuman caveHuman) {
		this.caveHuman = caveHuman;
	}

	public Map<Long, CavePartner> getPartnerMap() {
		return partnerMap;
	}

	public void setPartnerMap(Map<Long, CavePartner> partnerMap) {
		this.partnerMap = partnerMap;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(isFight);
		out.write(caveHuman);
		out.write(partnerMap);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		isFight = in.read();
		caveHuman = in.read();
		partnerMap.clear();
		partnerMap.putAll(in.<Map<Long,CavePartner>> read());
	}
	
	@Override
	public String toString() {
		return "CompeteHumanObj [isFight=" + isFight + ", cpHuman=" + caveHuman.getName() + ", partner=" + partnerMap + "]";
	}
	
	
}
