package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "CaveLog", tableName = "caveLog")
public enum EntityCaveLog {

	@Column(type = long.class, comment = "仙府ID", defaults = "1")
	CaveId,
	@Column(type = String.class, comment = "仙府日志", defaults = "1")
	Log,
	@Column(type = long.class, comment = "挑战者ID", defaults = "1")
	HumanId,
	@Column(type = long.class, comment = "被挑战者ID", defaults = "1")
	beChallegeHumanId,
	@Column(type = boolean.class, comment = "被挑战者是否胜利", defaults = "false")
	isWin,
	@Column(type = String.class, comment = "挑战者玩家姓名", defaults = "1")
	HumanName,
	@Column(type = int.class, comment = "挑战者玩家战斗力", defaults = "1")
	Combat,
	@Column(type = int.class, comment = "战斗类型", defaults = "1")
	BattleType,
	@Column(type = long.class, comment = "发生时间", defaults = "1")
	Time,
	@Column(type = int.class, comment = "被抢夺铜币数量", defaults = "0")
	RobCoin,
	@Column(type = long.class, comment = "占领时间", defaults = "1")
	OwnTime,
	@Column(type = int.class, comment = "页数", defaults = "0")
	Page,
	@Column(type = int.class, comment = "索引", defaults = "0")
	Index,
	@Column(type = int.class, comment = "类型", defaults = "0")
	Type,
}