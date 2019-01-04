package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "CompeteHistory", tableName = "compete_history")
public enum EntityCompeteHistory {
	@Column(type = long.class, comment = "比赛时间", index = true)
	CreatedAt,
	@Column(type = long.class, comment = "挑战者id", index = true)
	HumanIdFight, 
	@Column(type = String.class, length = 32, comment = "挑战者姓名")
	HumanNameFight, 
	@Column(type = int.class, comment = "挑战者等级")
	HumanNameLevel, 
	@Column(type = long.class, comment = "被挑战者id", index = true)
	HumanIdBeFight, 
	@Column(type = String.class, length = 32, comment = "被挑战者姓名")
	HumanNameBeFight, 
	@Column(type = int.class, comment = "被挑战者等级")
	HumanNameBeLevel, 
	@Column(type = boolean.class, comment = "是否获胜")
	Win,
	@Column(type = int.class, comment = "挑战玩家当前新的排名, 如果排名不变则为0，否则为当前新的排名")
	Rank,
	@Column(type = int.class, comment = "被挑战玩家当前新的排名, 如果排名不变则为0，否则为当前新的排名")
	BeRank,
	@Column(type = boolean.class, comment = "角色排名是否改变")
	Chage,
	@Column(type = int.class, comment = "挑战者头像")
	FightHeadSn,
	@Column(type = int.class, comment = "被挑战者头像")
	BeFightHeadSn,
	;
}