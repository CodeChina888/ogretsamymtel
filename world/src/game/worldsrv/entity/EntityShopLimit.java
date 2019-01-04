package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 框架自带（大领主）
 */
@Entity(entityName = "ShopLimit", tableName = "shoplimit")
public enum EntityShopLimit {
	@Column(type = int.class, comment = "限购商品的商品sn")
	ShopSn,
	@Column(type = int.class, comment = "目前已经售出数量")
	NumSell, ;
}