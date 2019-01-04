package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "ItemBase", isSuper = true)
public enum EntityItem {
	@Column(type = long.class, comment = "物主ID", index = true)
	OwnerId,
	@Column(type = String.class, length = 32, comment = "物主名字")
	OwnerName,
	@Column(type = int.class, comment = "道具SN")
	Sn,
	@Column(type = int.class, comment = "道具类型")
	Type,
	@Column(type = int.class, comment = "数量", defaults = "1")
	Num,
	
	@Column(type = int.class, comment = "物品在哪个容器：1身上，2背包，3仓库")
	Container, 
	@Column(type = int.class, comment = "物品在容器中的位置，从1开始", defaults = "1")
	Position,

	@Column(type = int.class, comment = "绑定状态，已无效")
	Bind,
	@Column(type = long.class, comment = "使用时限, 0无寿命限制，>0 结束寿命时间")
	Life,

	@Column(type = int.class, comment = "强化等级", defaults = "1")
	ReinforceLv,
	@Column(type = int.class, comment = "进阶等级", defaults = "1")
	AdvancedLv,
	@Column(type = String.class, length = 32, comment = "槽位段位数组" , defaults = "")
	RefineSlotLv,
	@Column(type = String.class, length = 32, comment = "精炼未操作的结果记录" , defaults = "")
	RefineRecordLv,
	@Column(type = int.class, comment = "装备精炼等级", defaults = "0")
	RefineLv,
	
	@Column(type = int.class, comment = "时装状态", defaults = "0")
	FashionStatus,
	;
}
