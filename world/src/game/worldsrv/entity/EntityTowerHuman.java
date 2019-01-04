package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * @author Neak
 * @see :爬塔可以匹配的玩家数据
 */
@Entity(entityName = "TowerHuman", tableName = "tower_human" , superEntity = EntityMirrorHuman.class)
public enum EntityTowerHuman {
	@Column(type = boolean.class, comment = "是否机器人")
	IsRobot,
	
	@Column(type = long.class, comment = "记录玩家插入时间")
	RecordTime,
}