package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="PayLog", tableName="pay_log")
public enum EntityPayLog {
	@Column(type=String.class, comment="角色ID", index=true)
	roleId,
	@Column(type=String.class, comment="订单号", index=true)
	orderId,
	@Column(type=String.class, comment="sn")
	propId,
	@Column(type=String.class, comment="渠道ID")
	channelId,
	@Column(type=String.class, comment="服务器ID")
	serverId,
	@Column(type=String.class, comment="不认识")
	serviceId,
	@Column(type=String.class, comment="订单金额(分)")
	chargePrice,
	@Column(type=String.class, comment="实际支付金额(分)")
	actualPrice,
	@Column(type=String.class, comment="业务ID")
	payChannelId,
	@Column(type=String.class, comment="游戏用户ID")
	userId,
	@Column(type=String.class, comment="计费点ID")
	chargeUnitId,
	@Column(type=String.class, comment="货币类型")
	currencyType,
	@Column(type=String.class, comment="语言ID")
	localeId,
	@Column(type=String.class, comment="机型组ID")
	deviceGroupId,
	@Column(type=String.class, comment="签名")
	sign,
	@Column(type=String.class, comment="处理状态")
	status,
	@Column(type=String.class, comment="处理时间")
	time,
	;
}
