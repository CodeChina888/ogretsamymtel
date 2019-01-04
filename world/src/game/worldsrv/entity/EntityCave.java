package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Cave", tableName = "cave")
public enum EntityCave {

	@Column(type = long.class, comment = "占领者ID", defaults = "0")
	HumanID,
	@Column(type = int.class, comment = "战斗力", defaults = "0")
	Combat,
	@Column(type = int.class, comment = "页数", defaults = "0")
	Page,
	@Column(type = int.class, comment = "索引", defaults = "0")
	Index,
	@Column(type = int.class, comment = "类型", defaults = "0")
	Type,
	@Column(type = int.class, comment = "洞穴sn", defaults = "0")
	Sn,
	@Column(type = String.class, comment = "人物名", defaults = "")
	name,
	@Column(type = boolean.class, comment = "是否被占领",defaults="false")
	isOwn,
	@Column(type = long.class, comment = "占领时间时间戳")
	ownTime,
	@Column(type = long.class, comment = "预期占领到某个时间")
	ExpOwnTime,
	@Column(type = int.class, comment = "延时档次，对应Cave表cdTime下标", defaults = "0")
	DelayCount,
	@Column(type = int.class, comment = "被抢夺金币数", defaults = "0")
	BeRobNum,
	@Column(type = int.class, comment = "被抢次数", defaults = "0")
	RobCount,
	@Column(type = String.class, comment = "抢夺的人物id列表", defaults = "[]")
	RobHumanList,
//	@Column(type = long.class, comment = "工会id", defaults = "0")
//	GuiLdId,
}