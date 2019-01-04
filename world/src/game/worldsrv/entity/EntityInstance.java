package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 副本章节信息记录
 */
@Entity(entityName = "Instance", tableName = "instance")
public enum EntityInstance {
	@Column(type = long.class, comment = "所属玩家ID", index = true)
	HumanId,
	@Column(type = String.class, length = 32, comment = "所属玩家名字")
	HumanName, 
	@Column(type = int.class, comment = "章节SN")
	ChapterSn,
	@Column(type = int.class, comment = "章节总星数", defaults = "0")
	StarAll, 
	@Column(type = String.class, length = 128, comment = "章节宝箱领取状态{star:EAwardType}(0不能领，1可领取，2已领取)", defaults = "{}")
	BoxJSON,
	@Column(type = String.class, length = 1024, comment = "下属副本通关星数{instSn:star}", defaults = "{}")
	StarJSON,
	@Column(type = String.class, length = 1024, comment = "下属副本已挑战次数{instSn:num}", defaults = "{}")
	FightJSON,
	@Column(type = String.class, length = 1024, comment = "下属副本已重置次数{instSn:num}", defaults = "{}")
	ResetJSON,
	@Column(type = int.class, comment = "是否是已删除的玩家，0 未删除，1 已删除", defaults = "0")
	DeleteRole, 
	;
}
