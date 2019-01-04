package game.worldsrv.entity;

import core.gen.entity.Column;

import core.gen.entity.Entity;
/**
 * @author Neak
 * 符文装备系统
 */
@Entity(entityName = "Rune", tableName = "rune")
public enum EntityRune {
	@Column(type = long.class, comment = "主角id")
	humanId,
	@Column(type = int.class, comment = "Sn")
	Sn, 
	@Column(type = int.class, comment = "等级")
	Level,
	@Column(type = int.class, comment = "经验")
	Exp,
	@Column(type = long.class, comment = "装备对象的Id，>0伙伴，-1主角，0没有被装备", defaults = "0")
	belongUnitId,
}
