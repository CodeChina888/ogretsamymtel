package game.worldsrv.support;
import game.msg.Define.ELootMapType;

/**
 * @author Neak
 * 配置表key组合公式
 */
public class ConfigKeyFormula {
	// 技能相关
	/**
	 * 获得技能tag
	 * @param skillSn
	 * @return
	 */
	public static int getSkillTagBySn(int skillSn) {
		return skillSn / 100000;
	}
	
	/**
	 * 获取技能升级配置sn
	 * @param skillTag
	 * @param skillLv
	 * @return
	 */
	public static int getSkillLvUpSn(int skillTag , int skillLv) {
		return skillTag * 100000 + skillLv;
	}
	/**
	 * 获取技能升级配置sn
	 * @param skillSn 技能sn（skill.sn）
	 * @param skillLv 技能等级
	 * @return
	 */
	public static int getSkillLvUpSnBySkillSn(int skillSn, int skillLv) {
		return getSkillTagBySn(skillSn) * 100000 + skillLv;
	}
	
	/**
	 * 获取技能符文配置sn
	 * @param skillTag
	 * @param runeSn
	 * @param runeLv
	 * @return
	 */
	public static Integer getSkillRuneSn(int skillTag, int runeSn, int runeLv) {
		return skillTag * 1000 + runeSn * 100 + runeLv;
	}
	
	/**
	 * 获取技能阶段（修炼）配置sn
	 * @param skillTag 技能标识
	 * @param stageLv 技能阶级
	 * @return
	 */
	public static Integer getSkillAdvancedSn(int skillTag, int stageLv) {
		return skillTag * 100000 + stageLv * 1000;
	}
	
	/**
	 * 获取神通配置sn
	 * @param trainType 神通类型
	 * @param stage 神通阶级
	 * @return
	 */
	public static Integer getSkillTrainSn(int trainType, int stage) {
		return trainType * Utils.I1000 + stage;
	}
	
	/**
	 * 获取技能标识对应的升阶经验道具sn
	 */
	public static Integer getSkillTrainItemSn(int skillTag) {
		return 135 * 1000 + skillTag;  
	}
	
	/**
	 * 获取技能爆点升星配置sn
	 */
	public static Integer getSkillGodsStarSn(int godsTag, int star) {
		// 星级
		int starStage = star / 5;
		// 每个星级里的阶段
		int starLv = star % 5; 
		return godsTag * 100000 + starStage * 1000 + starLv;
	}
	
	// 抢夺本相关
	/**
	 * 获取ConfLootMapSn
	 * @param level 层数
	 * @param group 组
	 * @param mapType 地图类型
	 * @return
	 */
	public static int getLootMapSn(int level,int group,ELootMapType mapType){
		return level*1000+group*10+mapType.getNumber();
	}
	
	/**
	 * 根据参数获取 LevelObjectConfSn
	 * @param eventSn
	 * @param mapLevel
	 * @param humanLevel
	 * @return
	 */
	public static int getLootMapLevelObjectConfSn(int eventSn,int mapLevel,int humanLevel){
		return eventSn*10000+mapLevel*100+humanLevel;
	}
	/**
	 * 获取抢夺本荣誉配置
	 * @param hornerType
	 * @param param1
	 * @return
	 */
	public static int getLootMapKillHonorSn(int hornerType,int param1){
		return hornerType*1000+param1;
	}
	
	
	//---------------------------
	// 装备相关
	//---------------------------
	/**
	 * 获取装备进阶表EquipAdvanced.sn
	 * @param itemType 道具类型
 	 * @param advancedLv 装备进阶等级
	 * @return
	 */
	public static int getEquipAdvancedSn(int itemType, int advancedLv) {
		return itemType * Utils.I1000 + advancedLv;
	}
	
	/**
	 * 获取装备强化表EquipReinforce.sn
	 * @param itemType
	 * @param reinforceLv
	 * @return
	 */
	public static int getEquipReinforceSn(int itemType, int reinforceLv) {
		return itemType * Utils.I1000 + reinforceLv;
	}
	
	/**
	 * 获取装备精炼表EquipReinforce.sn
	 * @param itemType
	 * @param refineLv
	 * @return
	 */
	public static int getEquipRefineSn(int itemType, int refineLv) {
		return itemType * Utils.I1000 + refineLv;
	}
	
	/**
	 * 获取时装表Fashion.sn
	 * @param partnerPropertySn 角色sn
	 * @param seriesType 套装系列
	 * @return
	 */
	public static int getFashionSn(int partnerPropertySn, int seriesType) {
		return partnerPropertySn * Utils.I1000 + seriesType;
	}
	
	/**
	 * 成就任务唯一sn
	 * @param achiSn 成就sn ConfAchievement.sn
 	 * @param achiLv 成就任务链等级
	 */
	public static int getAchievementLvSn(int achiSn, int achiLv) {
		return achiSn * Utils.I100 + achiLv;
	}
	
	/**
	 * 幸运转盘sn
	 * @param modeType
	 * @param lv
	 * @return
	 */
	public static Integer getLuckTurntableWeightSn(int modeType, int lv) {
		return modeType * Utils.I10000 + lv;
	}
	
	//---------------------------
	// 伙伴相关
	//---------------------------
	/**
	 * 获取伙伴突破表sn
	 * @param partnerSn 伙伴sn
	 * @param advLv 突破阶级
	 * @return
	 */
	public static Integer getPartnerConsititutionsSn(int partnerSn, int advLv) {
		return partnerSn * Utils.I100 + advLv;
	}
	
	/**
	 * 获取伙伴升星表sn
	 * @param partnerSn sn
	 * @param star 星级 
	 * @return
	 */
	public static Integer getPartnerStarSn(int partnerSn, int star) {
		return partnerSn * Utils.I100 + star;
	}
	/**
	 * 获取法宝升星表sn
	 * @param cimelia sn
	 * @param star 星级
	 * @return
	 */
	public static Integer getCimeliaStarSn(int cimelia, int star) {
		return cimelia * Utils.I100 + star;
	}
	
	/**
	 * 获取法宝突破表sn
	 * @param partnerSn 伙伴sn
	 * @param advLv 突破阶级
	 * @return
	 */
	public static Integer getCimeliaConsititutionsSn(int partnerSn, int advLv) {
		return partnerSn * Utils.I100 + advLv;
	}


	/**
	 * 伙伴随从配置表sn
	 * @param humanLv 
	 * @param quality 伙伴资质
	 */
	public static Integer getPartnerServantSn(int humanLv, int quality) {
		return humanLv * Utils.I10000 + quality;
	}
	/**
	 * 招募积分兑换表sn
	 */
	public static Integer getCardExchangeSn(int roundTag, int index) {
		return roundTag * 10 + index;
	}
	

}
