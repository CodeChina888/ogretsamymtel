package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;
import game.worldsrv.entity.EntityUnit;
/**
 * 伙伴宝物
 * @author songy
 *
 */
@Entity(entityName = "Cimelia", tableName = "cimelia")
public enum EntityCimelia {
	@Column(type = long.class, comment = "所属伙伴ID", index = true)
	PartnerId,
	@Column(type = int.class, comment = "伙伴的经验", defaults = "0")
	Exp,
	@Column(type = int.class, comment = "伙伴的星级", defaults = "0")
	Star,
	@Column(type = int.class, comment = "进阶等级", defaults = "0")
	AdvLevel,
	@Column(type = int.class, comment = "等级", defaults = "1")
	Level,
	@Column(type = int.class, comment = "sn", defaults = "0")
	Sn,
	@Column(type = int.class, comment = "品质", defaults = "0")
	Quality,
}
