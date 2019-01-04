package game.worldsrv.compete;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.entity.CompeteHuman;
import game.worldsrv.entity.CompetePartner;
import game.worldsrv.human.HumanPlusManager;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.support.Log;

public class CompeteHumanObj implements ISerilizable {
	public boolean isFight = false;
	public CompeteHuman cpHuman;
	// 伙伴信息<partnerId, CompetePartner>
	public Map<Long, CompetePartner> partnerMap = new HashMap<>();

	public CompeteHumanObj() {
		
	}
	
	/**
	 * 人物进入竞技场转换CompeteHumanObj
	 * @param humanObj
	 */
	public CompeteHumanObj(HumanObject humanObj) {
		cpHuman = new CompeteHuman();
		long humanId = humanObj.getHumanId();
		cpHuman.setId(humanId);
		cpHuman.setIsRobot(false);
		// 主角属性
		UnitManager.inst().copyUnit(humanObj.getUnit(), cpHuman);
		// 称号信息
		cpHuman.setTitleSn(humanObj.getHuman().getTitleSn());
		cpHuman.setTitleShow(humanObj.getHuman().isTitleShow());
		// 上阵技能和爆点
		HumanSkillManager.inst().setHumanMirrorSkill(humanObj, cpHuman);
		// 上阵伙伴阵容和站位
		cpHuman.setPartnerLineup(humanObj.extInfo.getPartnerLineup());
		cpHuman.setPartnerStance(humanObj.extInfo.getPartnerStance());
		// 上阵伙伴属性
		List<Long> lineup = PartnerManager.inst().getPartnerLineUp(humanObj);
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
			CompetePartner pm = new CompetePartner();
			// 将伙伴信息复制给镜像
			HumanPlusManager.inst().copyPartnerObToMirrorPartner(humanId, partnerObj, pm);
			partnerMap.put(pm.getId(), pm);
		}
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(isFight);
		out.write(cpHuman);
		out.write(partnerMap);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		isFight = in.read();
		cpHuman = in.read();
		partnerMap.clear();
		partnerMap.putAll(in.<Map<Long,CompetePartner>> read());
	}
	
	@Override
	public String toString() {
		return "CompeteHumanObj [isFight=" + isFight + ", cpHuman=" + cpHuman.getName() + ", partner=" + partnerMap + "]";
	}
		
	public int getSumCombat(){
		int humanCombat = cpHuman.getCombat();
		for(Entry<Long ,CompetePartner>entry:partnerMap.entrySet()){
			CompetePartner p = entry.getValue();
			humanCombat +=p.getCombat();
		}
		return humanCombat;
	}
}
