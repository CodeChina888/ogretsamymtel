package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;
/**
 * @author Neak
 * 时装信息
 */
@Entity(entityName="Fashion", tableName="fashion")
public enum EntityFashion{
	@Column(type=String.class, length=512, comment="已获得的时装sn",  defaults="")
	fashionSn,
	@Column(type=String.class, length=2048, comment="对应时装sn的到期时间，-1为永久",  defaults="")
	fashionLimitTime,
	@Column(type=String.class, length=512, comment="已经获得的变装卡snList(1,2,3)",  defaults="")
	HenshinSn,
	@Column(type=String.class, length=2048, comment="对应变装sn的到期时间，-1为永久",  defaults="")
	HenshinLimitTime,
}
