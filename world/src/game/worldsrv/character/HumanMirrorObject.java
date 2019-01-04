package game.worldsrv.character;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import core.support.Param;
import game.msg.Define.EPosType;
import game.worldsrv.compete.CompeteHumanObj;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.entity.CaveHuman;
import game.worldsrv.entity.CavePartner;
import game.worldsrv.entity.CompeteHuman;
import game.worldsrv.entity.CompetePartner;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.entity.MirrorHuman;
import game.worldsrv.entity.MirrorPartner;
import game.worldsrv.entity.PartnerMirror;
import game.worldsrv.entity.TowerHuman;
import game.worldsrv.entity.TowerPartner;
import game.worldsrv.entity.Unit;
import game.worldsrv.enumType.ProfessionType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanManager;
import game.worldsrv.human.HumanPlusManager;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.immortalCave.CaveHumanObj;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.tower.TowerHumanObj;

/**
 * 人物镜像对象
 */
public class HumanMirrorObject implements ISerilizable {
	public final static String mapLineUpId = "mapLineUpId"; // 上阵阵容id
	
	public final static String InstanceParam = "InstanceParam"; // 副本的额外参数KEY
	public final static String PassParam = "PassParam"; // 是否通关副本的额外参数KEY
	public final static String CompeteParam = "CompeteParam"; // 竞技场的额外参数KEY
	public final static String TowerParam = "TowerParam"; // 爬塔的额外参数KEY
	public final static String WorldBossParam = "WorldBossParam"; // 世界boss的额外参数KEY
	public final static String GuildParam = "GuildParam"; // Guild的额外参数KEY
	public final static String InvadeParam = "InvadeParam"; // Invade的额外参数KEY
	public final static String CaveParam = "CaveParam"; // Cave的额外参数KEY
	public final static String PKHumanParam = "PKHumanParam"; // pvp的额外参数KEY
    public final static String GuildInstParam = "GuildInstParam"; // 公会副本的额外参数KEY
	// 玩家镜像数据
	public HumanMirror humanMirror;
	// 玩家拥有的伙伴<伙伴id, 伙伴对象>
	public Map<Long, PartnerMirror> partnerMirrorMap = new HashMap<>();
	// 附加参数
	public Param exParam = new Param();
	// 查看玩家信息
	//public SCQueryInfo.Builder msgQueryInfo = SCQueryInfo.newBuilder();
	
	/**
	 * 默认构造函数（implements ISerilizable必须写）
	 */
	public HumanMirrorObject() {
		
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(humanMirror);
		out.write(partnerMirrorMap);
		out.write(exParam);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		humanMirror = in.read();
		partnerMirrorMap.clear();
		partnerMirrorMap.putAll(in.<Map<Long, PartnerMirror>> read());
		exParam = in.read();
	}
	
	/**
	 * 使用模板
	 * @param humanObj
	 */
	public void initMod(HumanObject humanObj) {
		// 保存玩家场景位置记录
		HumanManager.inst().saveStageHistory(humanObj);
		humanMirror = new HumanMirror();
		Human human = humanObj.getHuman();
		long humanId = human.getId();
		humanMirror.setId(humanId);
		humanMirror.setSn(human.getSn());
		
		// 称号信息
		humanMirror.setTitleSn(human.getTitleSn());
		humanMirror.setTitleShow(human.isTitleShow());
		// 主角模板属性设置
		this.initHumanModInfo(human.getSn());
		
		humanMirror.setModelSn(human.getModelSn());// 模型sn
		humanMirror.setDefaultModelSn(human.getDefaultModelSn()); // 默认模型（时装改变）
		humanMirror.setSkillGroupSn(human.getSkillGroupSn());// 技能组sn
		humanMirror.setSkillAllSn(human.getSkillAllSn());// 所有技能sn
		
		// 主角技能模板相关
		HumanSkillManager.inst().setHumanMirrorModSkill(humanObj, humanMirror);
		// 上阵伙伴阵容和站位
		humanMirror.setPartnerLineup(humanObj.extInfo.getPartnerLineup());
		humanMirror.setPartnerStance(humanObj.extInfo.getPartnerStance());
		// 上阵伙伴属性
		List<Long> lineupList = Utils.strToLongList(humanObj.extInfo.getPartnerLineup());
		for (Long partnerId : lineupList) {
			if(partnerId <= 0) {// 过滤：0空位置，-1主角
				continue;
			}
			PartnerObject partnerObj = humanObj.partnerMap.get(partnerId);
			if(null == partnerObj) {
				Log.game.info("伙伴不存在！partnerId={},humanId={}", partnerId, humanId); 
				continue;
			}
			PartnerMirror pm = new PartnerMirror();
			// 将伙伴信息复制给镜像
			HumanPlusManager.inst().copyPartnerObToMirrorPartner(humanId, partnerObj, pm);
			partnerMirrorMap.put(pm.getId(), pm);
		}
	}
	
	/**
	 * 人物对象转换为人物镜像对象
	 * @param humanObj
	 */
	public HumanMirrorObject(HumanObject humanObj) {
		// 保存玩家场景位置记录
		HumanManager.inst().saveStageHistory(humanObj);
				
		humanMirror = new HumanMirror();
		Human human = humanObj.getHuman();
		long humanId = human.getId();
		humanMirror.setId(humanId);
		//humanMirror.setIsRobot(false);
		// 主角属性
		UnitManager.inst().copyUnit(humanObj.getUnit(), humanMirror);
		// 称号信息
		humanMirror.setTitleSn(human.getTitleSn());
		humanMirror.setTitleShow(human.isTitleShow());
		// 技能相关
		HumanSkillManager.inst().setHumanMirrorSkill(humanObj, humanMirror);
		// 上阵伙伴阵容和站位
		humanMirror.setPartnerLineup(humanObj.extInfo.getPartnerLineup());
		humanMirror.setPartnerStance(humanObj.extInfo.getPartnerStance());
		// 上阵伙伴属性
		List<Long> lineupList = Utils.strToLongList(humanObj.extInfo.getPartnerLineup());
		for (Long partnerId : lineupList) {
			if(partnerId <= 0) {// 过滤：0空位置，-1主角
				continue;
			}
			PartnerObject partnerObj = humanObj.partnerMap.get(partnerId);
			if(null == partnerObj) {
				Log.game.info("伙伴不存在！partnerId={},humanId={}", partnerId, humanId); 
				continue;
			}
			PartnerMirror pm = new PartnerMirror();
			// 将伙伴信息复制给镜像
			HumanPlusManager.inst().copyPartnerObToMirrorPartner(humanId, partnerObj, pm);
			partnerMirrorMap.put(pm.getId(), pm);
			
			// FIXME ..............测试战斗攻击超叼 
			if (humanObj.daddyFight[0] == 1) {
				int daddyAtk = humanObj.daddyFight[1] != 0 ? humanObj.daddyFight[1] : 10000;
				int daddyHp = humanObj.daddyFight[2] != 0 ? humanObj.daddyFight[2] : 100000;
				pm.setAtk(daddyAtk);
				pm.setHpMax(daddyHp);
			}
		}
		// FIXME ..............测试战斗攻击超叼 
		if (humanObj.daddyFight[0] == 1) {
			int daddyAtk = humanObj.daddyFight[1] != 0 ? humanObj.daddyFight[1] : 10000;
			int daddyHp = humanObj.daddyFight[2] != 0 ? humanObj.daddyFight[2] : 100000;
			humanMirror.setAtk(daddyAtk);
			humanMirror.setHpMax(daddyHp);
		}
	}
	
	/**
	 * 人物全局信息转换为人物镜像对象
	 * @param hgInfo
	 */
	public HumanMirrorObject(HumanGlobalInfo hgInfo) {
		humanMirror = new HumanMirror();
		Unit unit = hgInfo.unit;
		long humanId = hgInfo.id;
		humanMirror.setId(humanId);
		//humanMirror.setIsRobot(false);
		// 主角属性
		UnitManager.inst().copyUnit(unit, humanMirror);
		// 称号信息
		//humanMirror.setTitleSn(human.getTitleSn());
		//humanMirror.setTitleShow(human.isTitleShow());
		// 技能相关
		HumanSkillManager.inst().setHumanMirrorSkill(hgInfo, humanMirror);
		// 上阵伙伴阵容和站位
		humanMirror.setPartnerLineup(hgInfo.lineup);
		humanMirror.setPartnerStance(hgInfo.stance);
		// 上阵伙伴属性
		for (PartnerObject partnerObj : hgInfo.partnerObjList) {
			if(null == partnerObj) {
				Log.game.info("伙伴不存在！humanId={}", humanId); 
				continue;
			}
			PartnerMirror pm = new PartnerMirror();
			// 将伙伴信息复制给镜像
			HumanPlusManager.inst().copyPartnerObToMirrorPartner(humanId, partnerObj, pm);
			partnerMirrorMap.put(pm.getId(), pm);
		}
	}
	
	/**
	 * 竞技场人物对象转换为人物镜像对象
	 */
	public HumanMirrorObject(CompeteHumanObj cpHumanObj) {
		CompeteHuman cpHuman = cpHumanObj.cpHuman;
		
		// 爬塔数据复制到任务镜像对象
		this.copyToHumanMirror(cpHuman);
		
		// 上阵伙伴属性
		List<Long> lineup = Utils.strToLongList(cpHuman.getPartnerLineup());
		for (Long partnerId : lineup) {
			if(partnerId <= 0) {// 过滤：0空位置，-1主角
				continue;
			}
			CompetePartner cpPartner = cpHumanObj.partnerMap.get(partnerId);
			if(null == cpPartner) {
				Log.game.info("伙伴不存在！partnerId={},humanId={}", partnerId, cpHuman.getId()); 
				continue;
			}
			// 爬塔伙伴数据复制
			this.copyToPartnerMirror(cpPartner);
		}
	}

	/**
	 * 竞技场人物对象转换为人物镜像对象
	 */
	public HumanMirrorObject(CaveHumanObj caveHumanObj) {
		CaveHuman cpHuman = caveHumanObj.caveHuman;
		
		//复制到镜像对象
		this.copyToHumanMirror(cpHuman);
		
		// 上阵伙伴属性
		List<Long> lineup = Utils.strToLongList(cpHuman.getPartnerLineup());
		for (Long partnerId : lineup) {
			if(partnerId <= 0) {// 过滤：0空位置，-1主角
				continue;
			}
			CavePartner cPartner = caveHumanObj.partnerMap.get(partnerId);
			if(null == cPartner) {
				Log.game.info("伙伴不存在！partnerId={},humanId={}", partnerId, cpHuman.getId()); 
				continue;
			}
			// 爬塔伙伴数据复制
			this.copyToPartnerMirror(cPartner);
		}
	}
	
	/**
	 * 爬塔人物对象转换为人物镜像对象
	 * @param combat 爬塔匹配时的战力
	 */
	public HumanMirrorObject(TowerHumanObj towerHumanObj, int combat) {
		TowerHuman towerHuman = towerHumanObj.getHuman();
		// 爬塔对象的战力
		double oriCombat = (double)towerHumanObj.getCombat();
		// 战力系数
		double coefficient = (double)combat / oriCombat;
		
		// 爬塔数据复制到任务镜像对象
		this.copyToHumanMirror(towerHuman, coefficient);
		
		// 上阵伙伴属性
		List<Long> lineup = Utils.strToLongList(towerHuman.getPartnerLineup());
		for (Long partnerId : lineup) {
			if(partnerId <= 0) {// 过滤：0空位置，-1主角
				continue;
			}
			TowerPartner towerPartner = towerHumanObj.getPartnerMap().get(partnerId);
			if(null == towerPartner) {
				Log.game.info("伙伴不存在！partnerId={},humanId={}", partnerId, towerHuman.getId()); 
				continue;
			}
			// 爬塔伙伴数据复制
			this.copyToPartnerMirror(towerPartner, coefficient);
		}
	}
	
	/**
	 * 计算全队的战力
	 */
	public int calc_TotalCombat() {
		int combat = humanMirror.getCombat();
		// 伙伴战力
		long[] idPartners = Utils.strToLongArray(humanMirror.getPartnerLineup());
		for (int pos = 0; pos < idPartners.length; pos++) {
			long partnerId = idPartners[pos];
			if (partnerId <= 0) {
				continue;
			}
			PartnerMirror partnerMir = partnerMirrorMap.get(partnerId);
			if (null == partnerMir) {
				continue;
			}
			combat += partnerMir.getCombat();
		}
		return combat;
	}
	
	/**
	 * 获取人物名字
	 */
	public String getHumanName() {
		return humanMirror.getName();
	}
	
	/**
	 * 获取人物id
	 */
	public long getHumanId() {
		return humanMirror.getId();
	}
	
	/**
	 * 获取人物镜像
	 */
	public HumanMirror getHumanMirror() {
		return humanMirror;
	}
	/**
	 * 获取伙伴镜像
	 * @return
	 */
	public Map<Long, PartnerMirror> getPartnerMirror() {
		return partnerMirrorMap;
	}
	/**
	 * 获取主角在队伍中的位置（PartnerLineup里的-1所在位置即是主角位置，位置从0开始）
	 * @return
	 */
	public int getHumanPos() {
		int pos = 0;
		long[] lineup = Utils.strToLongArray(humanMirror.getPartnerLineup());
		for(int i = 0; i < lineup.length; i++) {
			if(-1 == lineup[i]) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	/**
	 * 竞技场/爬塔伙伴数据复制到镜像
	 */
	private void copyToHumanMirror(MirrorHuman mirrorHuman) {
		copyToHumanMirror(mirrorHuman, 1);
	}
	/**
	 * 竞技场/爬塔玩家数据复制到镜像
	 * @param mirrorHuman 竞技场/爬塔角色镜像基类
	 */
	private void copyToHumanMirror(MirrorHuman mirrorHuman, double coefficient) {
		humanMirror = new HumanMirror();
		long humanId = mirrorHuman.getId();
		humanMirror.setId(humanId);
		//humanMirror.setIsRobot(false);
		// 主角属性
		UnitManager.inst().copyUnit(mirrorHuman, humanMirror);
		// 属性 * 系数
		UnitManager.inst().basePropMultiply(humanMirror, coefficient);
		
		// 称号信息
		humanMirror.setTitleSn(mirrorHuman.getTitleSn());
		humanMirror.setTitleShow(mirrorHuman.isTitleShow());
		// 技能相关
		HumanSkillManager.inst().setHumanMirrorSkill(mirrorHuman, humanMirror);
		
		// 上阵伙伴阵容和站位
		humanMirror.setPartnerLineup(mirrorHuman.getPartnerLineup());
		humanMirror.setPartnerStance(mirrorHuman.getPartnerStance());
	}
	
	/**
	 * 竞技场/爬塔伙伴数据复制到镜像
	 * @param mirrorPartner 竞技场/爬塔伙伴镜像基类
	 */
	private void copyToPartnerMirror(MirrorPartner mirrorPartner) {
		copyToPartnerMirror(mirrorPartner, 1);
	}
	/**
	 * 竞技场/爬塔伙伴数据复制到镜像
	 * @param mirrorPartner 竞技场/爬塔伙伴镜像基类
	 */
	private void copyToPartnerMirror(MirrorPartner mirrorPartner, double coefficient) {
		PartnerMirror pm = new PartnerMirror();
		pm.setId(mirrorPartner.getId());// 伙伴ID
		pm.setHumanId(mirrorPartner.getHumanId());// 归属玩家ID
		pm.setExp(mirrorPartner.getExp());// 伙伴的经验
		pm.setStar(mirrorPartner.getStar());// 伙伴的星级
		pm.setAdvLevel(mirrorPartner.getAdvLevel());// 进阶品质的等级
		pm.setRelationActive(mirrorPartner.getRelationActive());// 激活的羁绊
		// 设置技能相关
		HumanPlusManager.inst().setPartnerSkill(mirrorPartner, pm);
		
		// 伙伴属性
		UnitManager.inst().copyUnit(mirrorPartner, pm);
		// 属性 * 系数
		UnitManager.inst().basePropMultiply(pm, coefficient);
		partnerMirrorMap.put(pm.getId(), pm);
	}
	
	/**
	 * 主角模板
	 */
	private boolean initHumanModInfo(int partnerPropertySn) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(partnerPropertySn);
		if (conf == null) {
			return false;
		}
		int modSn = 0;
		// 获取主角模板
		int[] templateHuman = ParamManager.lootMapPvpRoleCombatData;
		if (ProfessionType.Tank.value() == conf.type) {
			// 主角战士模板ID
			modSn = templateHuman[0]; 
		} else if (ProfessionType.Nanny.value() == conf.type) {
			// 主角法师模板ID
			modSn = templateHuman[1]; 
		} else if (ProfessionType.Dps.value() == conf.type) {
			// 主角刺客模板ID
			modSn = templateHuman[2]; 
		} 
		
		// 模板配置
		conf = ConfPartnerProperty.get(modSn);
		if (conf == null) {
			return false;
		}
		// 主角模板属性
		UnitManager.inst().initUnitProp(humanMirror, conf.propName, conf.propValue);
		humanMirror.setLevel(conf.lvl);
		return true;
	}
	/**
	 * 伙伴模板
	 */
	private boolean initPartnerModInfo(PartnerMirror pm, int partnerPropertySn) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(partnerPropertySn);
		if (conf == null) {
			return false;
		}
		int modSn = 0;
		// 获取伙伴模板
		int[] templateHuman = ParamManager.lootMapPvpPartnerCombatData;;
		if (ProfessionType.Tank.value() == conf.type) {
			// 防御模板ID
			modSn = templateHuman[0]; 
		} else if (ProfessionType.Nanny.value() == conf.type) {
			// 辅助模板ID
			modSn = templateHuman[1]; 
		} else if (ProfessionType.Dps.value() == conf.type) {
			// 控制模板ID
			modSn = templateHuman[2]; 
		} else if (ProfessionType.Ctrl.value() == conf.type){
			// 控制模板ID
			modSn = templateHuman[2]; 
		}
		
		// 模板配置
		conf = ConfPartnerProperty.get(modSn);
		if (conf == null) {
			return false;
		}
		// 主角模板属性
		UnitManager.inst().initUnitProp(pm, conf.propName, conf.propValue);
		pm.setLevel(conf.lvl);
		return true;
	}
	
	/**
	 * 构造函数(仅战斗使用)
	 * @param json 战斗json
	 */
	public HumanMirrorObject(JSONObject json){
		humanMirror = new HumanMirror();
//		if(json.containsKey("pos")){
//			humanMirror.setHumanPos(json.getIntValue("pos"));
//		}
		if(json.containsKey("sn")){
			humanMirror.setSn(json.getIntValue("sn"));
		}
		if(json.containsKey("sex")){
			humanMirror.setSex(json.getIntValue("sex"));
		}
//		if(json.containsKey("prop")){
//			humanMirror.setPropJSON(json.getString("prop"));
//		}
		if(json.containsKey("SkillJSON")){
			humanMirror.setInstallSkillJSON(json.getString("SkillJSON"));
		}
		if(json.containsKey("GodsJSON")){
			humanMirror.setInstallGodsJSON(json.getString("GodsJSON"));
		}
		if(json.containsKey("PassiveSn")){
			humanMirror.setInstallGodsJSON(json.getString("PassiveSn"));
		}
		//----------------------武将--------------------		
		for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
			
		}
	}
		
	/**
	 * 获取战斗json(仅战斗使用)
	 * @return
	 */
	public JSONObject toFightJson(){
		JSONObject json = new JSONObject();
		//json.put("pos", this.getHumanPos());
		json.put("sn", this.humanMirror.getSn());
		json.put("sex", this.humanMirror.getSex());
		//json.put("prop", this.humanMirror.getPropJSON());
		json.put("SkillJSON", this.humanMirror.getInstallSkillJSON());
		json.put("GodsJSON", this.humanMirror.getInstallGodsJSON());
		//----------------------武将--------------------		
		for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
			
		}
		return json;
	}
	
}
