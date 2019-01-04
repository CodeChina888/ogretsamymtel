package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 爬塔玩家的伙伴镜像数据
 */
@Entity(entityName = "TowerPartner", tableName = "tower_partner" , superEntity = EntityMirrorPartner.class)
public enum EntityTowerPartner {
	@Column(type = long.class, comment = "记录伙伴插入时间")
	RecordTime,
	;
}
