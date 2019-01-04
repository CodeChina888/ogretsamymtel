package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="FriendObject", tableName="friendobject")
public enum EntityFriendObject {
	@Column(type = int.class, comment = "玩家ID")
	humanId,
	@Column(type = int.class, comment = "角色Sn")
	Sn,
	@Column(type=String.class, comment="玩家姓名")
	Name,
	@Column(type = int.class, comment = "头像")
	HeadSn,
	@Column(type = int.class, comment = "坐骑")
	MountSn,
	@Column(type = int.class, comment = "时装")
	ModelSn,
	@Column(type = int.class, comment = "等级")
	Lv,
	@Column(type = int.class, comment = "战斗力")
	Combat,
	@Column(type = int.class, comment = "Vip等级")
	VipLv,
	@Column(type = int.class, comment = "申请人数")
	ApplyNum,
	@Column(type = int.class, comment = "好友人数")
	FriendNum,
	@Column(type = boolean.class, comment = "是否在线")
	Line,
}
