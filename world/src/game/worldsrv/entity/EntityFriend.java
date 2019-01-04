package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="Friend", tableName="friend")
public enum EntityFriend {
	@Column(type = long.class, comment = "玩家id")
	HumanId,
	@Column(type = long.class, comment = "好友id")
	FId,
	@Column(type=int.class, comment="类别：0,申请,1为好友,2黑名单,3,删除好友")
	Type,
	@Column(type=long.class, comment="赠送好友体力时间",defaults = "0")
	Give,
	@Column(type=String.class, comment="领取好友体力时间,1，可领取，2已经领取", length=256)
	Get,
	@Column(type=long.class, comment="申请时间|好友时间|黑。。。",defaults = "0")
	Time,
}
