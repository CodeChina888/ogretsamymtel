package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 全服补偿邮件
 * @author 
 *
 */
@Entity(entityName="FillMail", tableName="fill_mail")
public enum EntityFillMail {
	@Column(type=String.class, comment="标题")
	Title,	
	@Column(type=String.class, comment="内容", length=2048)
	Content,	
	@Column(type=int.class, comment="类型，0全服，1分服，2创觉时间")
	Type,
	@Column(type=int.class, comment="邮件属于哪分服，只有类型是分服时才有用")
	ServerId,
	@Column(type=long.class, comment="GM发送时间")
	SendTime,
	@Column(type=long.class, comment="可领取时间")
	StartTime,	
	@Column(type=long.class, comment="有效期截止时间戳")
	EndTime,
	@Column(type=long.class, comment="系统发送给玩家的时间", defaults="0")
	SysSendTime,
	@Column(type = String.class, length = 1024, comment = "物品列表", defaults = "{}")
	ItemJSON, 
	@Column(type=String.class, comment="平台的标识")
	EventKey,
	;
}