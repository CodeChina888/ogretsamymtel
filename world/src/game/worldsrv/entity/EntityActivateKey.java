package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="ActivateKey", tableName="activate_key")
public enum EntityActivateKey {
	@Column(type=String.class, comment="激活码")
	ActivateKey,
	@Column(type=String.class, comment="使用者的账号")
	Account,
}