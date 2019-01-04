package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "WorldBoss", tableName = "world_boss")
public enum EntityWorldBoss {
	@Column(type = int.class, comment = "所属活动副本sn", defaults = "0")
	ActInstSn,
	@Column(type = int.class, comment = "世界Boss的sn", defaults = "0")
	BossInstSn,
	@Column(type = int.class, comment = "世界Boss所在地图sn", defaults = "0")
	BossMapSn,
	@Column(type = int.class, comment = "世界Boss所在位置", defaults = "0")
	BossPos,
	@Column(type = String.class, length = 128, comment = "怪物sn：1,2,3,4,5,6")
	monsterSN,
	@Column(type = String.class, length = 128, comment = "怪物等级：1,2,3,4,5,6")
	monsterLv,
	@Column(type = String.class, length = 1024, comment = "怪物最大hp：11,22,33,44,55,66")
	monsterHpMax,
	@Column(type = String.class, length = 1024, comment = "怪物剩余hp：11,22,33,44,55,66")
	monsterHpCur,
	@Column(type = long.class, comment = "怪物最大血量之和", defaults = "0")
	HpMax,
	@Column(type = long.class, comment = "怪物当前血量之和", defaults = "0")
	HpCur,
	@Column(type = long.class, comment = "上次开启时间戳", defaults = "0")
	TimeLastStart,
	@Column(type = long.class, comment = "上次击杀时间戳", defaults = "0")
	TimeLastKill,
	@Column(type = long.class, comment = "上次击杀玩家ID")
	KillerId,
	@Column(type = String.class, length = 32, comment = "上次击杀玩家名字")
	KillerName, 
	@Column(type = String.class, length = 256, comment = "上次挑战排行前N的玩家昵称")
	RankTopName, 
	;
}