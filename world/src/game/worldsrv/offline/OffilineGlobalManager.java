package game.worldsrv.offline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.protobuf.format.JsonFormat;

import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define.DEquip;
import game.msg.Define.DPartnerBriefInfo;
import game.msg.Define.DServantBriefInfo;
import game.msg.MsgFriend.SCQueryCharacter;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanSimple;
import game.worldsrv.entity.ItemBody;
import game.worldsrv.entity.Partner;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanManager;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.humanSkill.SkillGodsJSON;
import game.worldsrv.humanSkill.SkillJSON;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.Item;
import game.worldsrv.item.ItemPack;
import game.worldsrv.param.ParamManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

/**
 * 离线数据管理器
 * 
 * @author songy
 *
 */
public class OffilineGlobalManager extends ManagerBase {


	public static OffilineGlobalManager inst() {
		return inst(OffilineGlobalManager.class);
	}

	
	public void updateInfo(HumanGlobalInfo globalInfo) {
		long humanId = globalInfo.id;
		OffilineGlobalServiceProxy prx = OffilineGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_updateInfo,"globalInfo",globalInfo);
	}
	
	private void _result_updateInfo(Param results, Param context) {
		HumanSimple humanSimple = results.get("humanSimple");
		HumanGlobalInfo globalInfo = context.get("globalInfo");
		if(humanSimple == null) {
			humanSimple = new HumanSimple();
			humanSimple.setId(globalInfo.id);
		}
		
		
	}
	/**
	 * 每分钟同步一次数据
	 * @param param
	 */
	@Listener(EventKey.EVERY_MIN)
	public void updateHumanSimple(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		long humanId = humanObj.getHumanId();
		
		SCQueryCharacter msg = getMyOffilineMsg(humanObj);
		OffilineGlobalServiceProxy prx = OffilineGlobalServiceProxy.newInstance();
		prx.updatOffilineInfo(humanId, msg);
	}
	
	
	public SCQueryCharacter getMyOffilineMsg(HumanObject humanObj){
		SCQueryCharacter.Builder msg = SCQueryCharacter.newBuilder();
		if (humanObj == null) {
			Log.game.error("=== humanObj is null ===");
			return msg.build();
		}
		Human human = humanObj.getHuman();
		msg.setHumanId(humanObj.id);
		msg.setHumanDigit(humanObj.getHuman().getHumanDigit());
		msg.setLevel(human.getLevel());
		msg.setName(human.getName());
		msg.setModelSn(human.getModelSn());
		msg.setTitleSn(human.getTitleSn());
		msg.setCombat(human.getSumCombat());//这里同步的是总战斗力
		msg.setVip(human.getVipLevel());
		msg.setProfession(human.getProfession());
//		HumanSkillRecord humanSkillRecord = info.humanSkillRecord;
//		msg.addAllSkill(humanSkillRecord.getSkillGroup());
//		msg.setGods(humanSkillRecord.getInstallGods());
		int instStart = InstanceManager.inst().getAllStarCount(humanObj);
		// 玩法数据
		msg.setInstStar(instStart);
		
		// 设置技能相关
		// 上阵技能
		List<SkillJSON> skillList = HumanSkillManager.inst().getInstallSkillJSON(humanObj);
		for (SkillJSON skillJSON : skillList) {
			msg.addSkillList(skillJSON.createDSkill());
		}
		// 爆点信息
		List<SkillGodsJSON> skillGodsList = humanObj.humanSkillRecord.getSkillGodsList();
		for (SkillGodsJSON skillGodsJSON : skillGodsList) {
			msg.addSkillGodsList(skillGodsJSON.createDSkillGods());
		}
		// 上阵爆点sn
		msg.setSkillGodsSn(humanObj.humanSkillRecord.getInstallGods());
		
		// 设置装备相关
		// 装备信息
		ItemPack itemBodyPack = humanObj.itemBody;// 背包信息
		for (Item item : itemBodyPack.findAll()) {
			msg.addEquipList(item.getDEquip());
		}
		
		
		// 设置伙伴信息
		List<PartnerObject> partnerList = PartnerManager.inst().getPartnerList(humanObj);
		if(partnerList != null){
			for (PartnerObject po : partnerList) {
				Partner partner  = po.getPartner();
				DPartnerBriefInfo.Builder dpi = DPartnerBriefInfo.newBuilder();
				dpi.setStar(partner.getStar());
				dpi.setLevel(partner.getLevel());
				dpi.setId(partner.getId());
				dpi.setSn(partner.getSn());
				dpi.setAdvanceLevel(partner.getAdvLevel());
				//设置护法信息
				Map<Long, Partner> servantMap = po.getServantMap();
				if (null != servantMap) {
					List<Long> slist  = po.getServantList();
					for(Long id : slist){
						Partner p = servantMap.get(id);
						if (null != p) {
							DServantBriefInfo.Builder dsinfo = DServantBriefInfo.newBuilder();
							dsinfo.setSn(p.getSn());
							dsinfo.setLevel(p.getLevel());
							dsinfo.setStarts(p.getStar());
							dsinfo.setAdvance(p.getAdvLevel());
							dpi.addServant(dsinfo);
						}
					}
					msg.addInfo(dpi);
				}
			}
		}
		int competeRank = human.getCompeteRank();
		msg.setCompeteRank(competeRank==ParamManager.competeRankMax?0:competeRank);//0是未上榜
		return msg.build();
	}
	
}
