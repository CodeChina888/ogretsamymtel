package game.worldsrv.immortalCave;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import core.support.Utils;
import game.msg.Define.DItem;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.entity.Cave;
import game.worldsrv.entity.CaveHuman;
import game.worldsrv.entity.CaveLog;
import game.worldsrv.entity.CavePartner;
import game.worldsrv.entity.Partner;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.support.Log;

public class CaveObject implements ISerilizable{
	
	private long caveId = 0;
	public  Cave cave = new Cave();
	public CaveHumanObj Mirrorhuman = new CaveHumanObj();
	/**
	*日志
	*/
	private List<CaveLog> cave_logs = new ArrayList<>();
	/**
	*占领者Id
	*/
	private long humanId = 0;
	
	public CaveObject(Cave cave, List<CaveLog> cave_logs, long humanId) {
		super();
		this.cave = cave;
		this.cave_logs = cave_logs;
		this.humanId = humanId;
	}
	
	/**
	 * 仙府id = 页数*1000 + sn
	 */
	public CaveObject() {

	}
	
	public CaveObject(HumanObject humanObj) {
		
		//设置战斗对象
		CaveHuman caveHuman = new CaveHuman();
		long humanId = humanObj.getHumanId();
		caveHuman.setId(humanId);
		cave.setIsOwn(true);
		// 主角属性
		UnitManager.inst().copyUnit(humanObj.getUnit(), caveHuman);
		// 称号信息
		caveHuman.setTitleSn(humanObj.getHuman().getTitleSn());
		caveHuman.setTitleShow(humanObj.getHuman().isTitleShow());
		// 设置技能相关
		HumanSkillManager.inst().setHumanMirrorSkill(humanObj, caveHuman);
		// 上阵伙伴阵容和站位
		List<Long> lineUps = PartnerManager.inst().getPartnerLineUp(humanObj);
		caveHuman.setPartnerLineup(Utils.ListLongToStr(lineUps));
		caveHuman.setPartnerStance(humanObj.extInfo.getPartnerStance());
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
			Partner partner = partnerObj.getPartner();
			CavePartner pm = new CavePartner();
			pm.setId(partner.getId());// 伙伴ID
			pm.setHumanId(humanObj.getHumanId());// 归属玩家ID
			pm.setExp(partner.getExp());// 伙伴的经验
			pm.setStar(partner.getStar());// 伙伴的星级
			pm.setAdvLevel(partner.getAdvLevel());// 进阶品质的等级
			pm.setRelationActive(partner.getRelationActive());// 激活的羁绊
			// 伙伴属性
			UnitManager.inst().copyUnit(partnerObj.getUnit(), pm);
			Mirrorhuman.getPartnerMap().put(pm.getId(), pm);
		}
		Mirrorhuman.setCaveHuman(caveHuman);
	}
	public long getCaveId() {
		return caveId;
	}

	public void setCaveId(long caveId) {
		this.caveId = caveId;
	}



	public Cave getCave() {
		return cave;
	}

	public void setCave(Cave cave) {
		this.cave = cave;
	}



	public List<CaveLog> getCave_logs() {
		return cave_logs;
	}

	public void setCave_logs(List<CaveLog> cave_logs) {
		this.cave_logs = cave_logs;
	}

	public long getHumanId() {
		return humanId;
	}

	public void setHumanId(long humanId) {
		this.humanId = humanId;
	}

	public boolean isOwn() {
		return cave.isIsOwn();
	}


	
	
	public CaveHumanObj getMirrorhuman() {
		return Mirrorhuman;
	}

	public void setMirrorhuman(CaveHumanObj mirrorhuman) {
		Mirrorhuman = mirrorhuman;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(caveId);
		out.write(cave);
		out.write(Mirrorhuman);
		out.write(cave_logs);
		out.write(humanId);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		caveId = in.read();
		cave = in.read();
		Mirrorhuman = in.read();
		cave_logs = in.read();
		humanId = in.read();
	}
	/**
	 * 
	 */
	public List<DItem> Checkout(){
		return null;
	}
	
	public String getOwnName() {
		return this.Mirrorhuman.getCaveHuman().getName();
	}

	public long getGuildId() {
		// TODO Auto-generated method stub
		CaveHumanObj caveHObj =  this.Mirrorhuman;
		if(caveHObj == null) {
			return 0;
		}
		CaveHuman ch = caveHObj.caveHuman;
		if(ch == null) {
			return 0;
		}
		return this.Mirrorhuman.caveHuman.getGuiLdId();
	}
	
}
