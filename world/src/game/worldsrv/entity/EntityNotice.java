package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Notice", tableName = "notice")
public enum EntityNotice {
	
    @Column(type = int.class, comment = "公告id")
	NoticeId, 
	@Column(type = String.class, length = 64, comment = "公告标题")
	Title, 
	@Column(type = String.class, length = 512, comment = "公告内容")
    Content, 
    @Column(type = int.class, comment = "公告type")
    Type, 
	@Column(type = long.class, comment = "公告生效时间戳")
	Timestamp, 
	@Column(type = long.class, comment = "公告循环间隔")
    IntervalTime,
    @Column(type = long.class, comment = "公告结束时间戳")
	TimesEnd,
    @Column(type = int.class, comment = "公告循环次数")
    Count,
    @Column(type = int.class, comment = "公告是否过期，0未过期，1过期")
    Outmoded,
	@Column(type=String.class, comment="平台的标识")
	EventKey,
	
	;

}