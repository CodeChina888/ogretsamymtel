package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="GiftActivate", tableName="gift_activate")
public enum EntityGiftActivate {
	@Column(type=String.class, comment="已经使用过的礼包码")
	GiftKey,
}
