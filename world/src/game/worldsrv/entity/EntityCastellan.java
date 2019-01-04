package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;
import game.worldsrv.entity.EntityUnit;

@Entity(entityName = "Castellan", tableName = "castellan")
public enum EntityCastellan {
	
	@Column(type = long.class, comment = "所属的人物ID")
	HumanId,
	@Column(type = String.class, comment = "人物姓名", defaults = "")
	Name,
	@Column(type = String.class, comment = "城主宣言", defaults = "")
	Declaration,
	@Column(type = int.class, comment = "已购买次数", defaults = "0")
	HasBuyNum,
	@Column(type = int.class, comment = "城主类型,与ECastellanType对应", defaults = "0")
	type,
	@Column(type = int.class, comment = "城主ModelSn", defaults = "0")
	ModelSn,
	@Column(type = boolean.class, comment = "是否通知客户端", defaults = "false")
	isNotice,
//	@Column(type = boolean.class, comment = "是否开启加成buff", defaults = "false")
//	isNotice,
	
}

