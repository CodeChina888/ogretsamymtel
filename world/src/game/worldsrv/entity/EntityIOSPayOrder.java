package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="IOSPayOrder", tableName="ios_pay_order")
public enum EntityIOSPayOrder {
	@Column(type = long.class, comment = "玩家id", index=true)
	HumanId,
	@Column(type=int.class, comment="商品SN")
	ProductSn,
	@Column(type=String.class, comment="订单号")
	Order,
	@Column(type=String.class, comment="订单收据MD5")
	ReceiptMd5,
	@Column(type=String.class, comment="订单原始收据", length=4096)
	OriginReceipt,
	@Column(type=String.class, comment="订单校验后回执", length=4096)
	CheckReceipt,
	@Column(type=int.class, comment="处理状态")
	Status,
	@Column(type=long.class, comment="处理时间")
	Time,
}
