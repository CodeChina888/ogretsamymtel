package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;
import game.worldsrv.entity.EntityUnit;

@Entity(entityName = "Partner", tableName = "partner", superEntity = EntityUnit.class)
public enum EntityPartner {
	@Column(type = long.class, comment = "所属的人物ID", index = true)
	HumanId,
	@Column(type = int.class, comment = "伙伴的经验", defaults = "0")
	Exp,
	@Column(type = int.class, comment = "伙伴的星级", defaults = "0")
	Star,
	@Column(type = int.class, comment = "进阶等级", defaults = "0")
	AdvLevel,
	@Column(type = String.class, comment = "激活的羁绊缘分sn", defaults = "")
	RelationActive,
	
	@Column(type = String.class, comment = "护法的ID列表", defaults = "[-1,-1,-1]")
	ServantList,
	@Column(type = String.class, comment = "护法的SN列表", defaults = "[-1,-1,-1]")
	ServantSnList,
	@Column(type = boolean.class, comment = "是否是护法", defaults = "false")
	isServant,
	@Column(type = String.class, comment = "伙伴技能", defaults = "[]")
	PartnerSkill,
//	@Column(type = int.class, comment = "资质")
//	talent,
	
}
