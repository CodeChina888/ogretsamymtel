package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Mail", tableName = "mail")
public enum EntityMail {
	@Column(type = long.class, comment = "接受者")
	Receiver,
	@Column(type=long.class, comment="发送者")
	Sender,
	@Column(type = String.class, length = 32, comment = "发送者名字")
	SenderName,
	@Column(type = String.class, length = 64, comment = "标题")
	Title, 
	@Column(type = String.class, length = 512, comment = "内容")
	Content, 
	@Column(type = long.class, comment = "接收时间戳")
	AcceptTimestamp, 
	@Column(type = long.class, comment = "删除时间戳")
	DeleteTimestamp,
	@Column(type = boolean.class, comment = "是否已读")
	Read, 
	@Column(type = boolean.class, comment = "是否已经领取")
	Pickup, 
	@Column(type = String.class, length = 1024, comment = "物品列表目前支持90个不同道具", defaults = "{}")
	ItemJSON, ;
	
}