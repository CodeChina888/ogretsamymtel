package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Buff", tableName = "buff")
public enum EntityBuff {
	@Column(type = int.class, comment = "BuffSn")
	Sn, 
	@Column(type = int.class, comment = "类型")
	Type, 
	@Column(type = boolean.class, comment = "是否作用过")
	Affected, 
	@Column(type = long.class, comment = "下次更新时间")
	TimePulse, 
	@Column(type = long.class, comment = "在线存在时间（在线累加）")
	TimeExistOnline, 
	@Column(type = long.class, comment = "结束时间戳")
	TimeEnd, 
	@Column(type = long.class, comment = "Buff作用对象Id", index = true)
	IdAffect, 
	@Column(type = long.class, comment = "施放者Id")
	IdFire, 
	@Column(type = String.class, length = 128, comment = "该buff初始对属性的影响", defaults = "{}")
	PropPlusDefaultJSON, 
	@Column(type = String.class, length = 128, comment = "该buff对属性的影响", defaults = "{}")
	PropPlusJSON, ;

}