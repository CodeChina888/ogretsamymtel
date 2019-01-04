package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "CompeteHuman", tableName = "compete_human" , superEntity = EntityMirrorHuman.class)
public enum EntityCompeteHuman {
	@Column(type = boolean.class, comment = "是否机器人")
	IsRobot,
	
	// 战斗力及排名等
	@Column(type = int.class, comment = "排名")
	Rank,
	@Column(type = int.class, comment = "历史最高排名")
	RankTop, 
	@Column(type = int.class, comment = "每日最高排名")
	RankDaily, 
	@Column(type = long.class, comment = "每日最高排名奖励发放时间")
	TimeRankAwardDaily, 
	@Column(type = long.class, comment = "玩家同步时间")
	TimeSync,
	@Column(type = boolean.class, comment = "上次战斗结果")
	LastBattleIsWin,
	@Column(type = int.class, comment = "连续胜利次数")
	SerialWinNums,
	@Column(type = boolean.class, comment = "是否是第一次进入竞技场")
	FirstIn,
	@Column(type = boolean.class, comment = "是否被攻击导致排名下降")
	Drop,
}
