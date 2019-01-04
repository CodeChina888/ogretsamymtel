package game.worldsrv.entity;


import core.gen.entity.Column;
import core.gen.entity.Entity;


@Entity(entityName="Shop", tableName="shop")
public enum EntityShop {
	@Column(type=long.class, comment="玩家id", index = true)
	HumanId,
	@Column(type=int.class, comment="商店类型 配置表sn")
	Type,
	@Column(type=String.class, length=1024, comment="商品列表[{sn:1,in:1,nm:1}...]", defaults = "{}")
	ItemsJSON,
	@Column(type=int.class, comment="购买次数（暂时无用）",defaults = "0")
	BuyCount,
	@Column(type=int.class, comment="当前可用的免费刷新次数",defaults = "0")
	FreeRefCount,
	@Column(type=long.class, comment="最后恢复免费刷新次数的时间戳",defaults = "0")
	LastReplyTime,
	@Column(type=int.class, comment="今日手动刷新次数",defaults = "0")
	DailyMTRefCount,
	@Column(type=long.class, comment="下一次整点自动刷新的时间",defaults = "0")
	NextAutoRefTime,
	;
}