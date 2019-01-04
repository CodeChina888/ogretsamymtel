package game.worldsrv.enumType;

import core.entity.EntityBase;
import game.worldsrv.entity.RankFairyland;
import game.worldsrv.entity.RankGuild;
import game.worldsrv.entity.RankInstance;
import game.worldsrv.entity.RankLevel;
import game.worldsrv.entity.RankSumCombat;
import game.worldsrv.entity.RankTower;
import game.worldsrv.entity.RankVip;
import game.worldsrv.param.ParamManager;

public enum RankType {
	// 等级排行
	RankLevel(RankLevel.class),
	// VIP排行
	RankVip(RankVip.class),
	// 爬塔排行
	RankTower(RankTower.class),
	// 公会排行
	RankGuild(RankGuild.class),
	// 总战力排行
	RankSumCombat(RankSumCombat.class),
	// 副本排行
	RankInstance(RankInstance.class),	
	// 洞天福地排行
	RankFairyland(RankFairyland.class),
	;

	private RankType(Class<? extends EntityBase> clazz) {
		
	}

	private int size = 100; // 排行榜大小

	public int getSize() {
		String enumName = this.name();
		if (enumName.equals(RankLevel.name())) {
			size = ParamManager.rankTopShowNum[RankIntType.Level.value()];
		} else if (enumName.equals(RankGuild.name())) {
			size = ParamManager.rankTopShowNum[RankIntType.Guild.value()];
		} else if (enumName.equals(RankInstance.name())) {
			size = ParamManager.rankTopShowNum[RankIntType.Instance.value()];
		} else if (enumName.equals(RankTower.name())) {
			size = ParamManager.rankTopShowNum[RankIntType.Tower.value()];
		} else if (enumName.equals(RankSumCombat.name())) {
			size = ParamManager.rankTopShowNum[RankIntType.SumCombat.value()];
		} 
		return size;
	}
	
}
