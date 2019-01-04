package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 伙伴镜像数据
 */
@Entity(entityName = "MirrorPartner", isSuper = true, superEntity = EntityUnit.class)
public enum EntityMirrorPartner {
	@Column(type = long.class, comment = "所属的人物ID", index = true)
	HumanId,
	@Column(type = int.class, comment = "伙伴的经验", defaults = "0")
	Exp,
	@Column(type = int.class, comment = "伙伴的星级", defaults = "0")
	Star,
	@Column(type = int.class, comment = "进阶品质的等级", defaults = "0")
	AdvLevel,
	@Column(type = int.class, comment = "进阶品质的等级", defaults = "0")
	cimeliaSn,	
	@Column(type = int.class, comment = "伙伴所属法宝的星级", defaults = "0")
	cimeliaStar,
	@Column(type = int.class, comment = "伙伴所属法宝进阶品质的等级", defaults = "0")
	cimeliaAdvLevel,
	@Column(type = String.class, comment = "激活的羁绊缘分sn", defaults = "[]")
	RelationActive, 
}
