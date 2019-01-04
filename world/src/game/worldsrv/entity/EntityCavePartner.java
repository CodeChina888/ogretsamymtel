package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 竞技场玩家的伙伴镜像数据
 */
@Entity(entityName = "CavePartner", tableName = "cave_partner", superEntity = EntityMirrorPartner.class)
public enum EntityCavePartner {
	@Column(type = long.class, comment = "洞穴id")
	CaveId,
	@Column(type = long.class, comment = "伙伴id")
	PartnerId,
	
}
