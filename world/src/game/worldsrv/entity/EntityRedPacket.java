package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;
/**
 * 红包
 * @author songy
 *
 */
@Entity(entityName = "RedPacket", tableName = "red_packet")
public enum EntityRedPacket {
	@Column(type = int.class, comment = "红包Sn")
	Sn,
	@Column(type = long.class, comment = "发红包的人")
	HumanId,
	@Column(type = int.class, comment = "红包个数")
	Nums,
	@Column(type = long.class, comment = "开始时间戳")
	BeginTime,
	@Column(type = long.class, comment = "结束时间戳")
	EndTime,
	/**
	 * [{'humanid':'1','name':'张三','getItem':{'itemSn':1,'itemNum':2}}.....]
	 */
	@Column(type = String.class, comment = "获取的情况",length=4096)
	Gainsituation,
	@Column(type = String.class, comment = "红包中剩余物品个数列表")
	SurplusDItems,
	@Column(type = int.class, comment = "红包中物品Sn")
	ItemSn,
}
