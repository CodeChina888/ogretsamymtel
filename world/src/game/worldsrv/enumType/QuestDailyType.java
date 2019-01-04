package game.worldsrv.enumType;

import game.worldsrv.enumType.QuestDailyType;

public enum QuestDailyType {
	RepPassNormal(1), 		// 普通副本通关
	RepPassHard(2), 		// 精英副本通关
	CompeteFight(3), 		// 竞技场挑战
	SkillUp(4),				// 技能升级
	ActValueBuy(5), 		// 购买体力
	CoinBuy(6),				// 购买铜币
	GoldConsume(7),			// 使用元宝
	GoldShopBuy(8),			// 元宝商店购买
	CompeteShopBuy(9),		// 威望商店购买
	GeneralShopBuy(10),		// 将魂商店购买
	TowerShopBuy(11),   	// 远征商店购买
	TowerFight(12),     	// 挑战爬塔
	WorldBoss(13),         	// 参与世界boss
	LootMap(14),      		// 参与洞天福地
	RecruitGeneral(15), 	// 普通伙伴招募
	RecruitAdvanced(16),	// 高级伙伴招募
	GodsLvUp(17),			// 爆点培养（升级）
	SkillTrain(18),     	// 技能修炼
	EquipRefine(19),    	// 装备精炼
	MainCityFight(20),      // 主城切磋
	
	
	// 以下为未实现的
	;

	private int value;
		
	private QuestDailyType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
	
}
