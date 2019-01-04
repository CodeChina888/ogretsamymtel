package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 框架自带（大领主）
 */
@Entity(entityName = "ShopExchange", tableName = "shopexchange")
public enum EntityShopExchange {
	/* 商城系统相关 */
	@Column(type = String.class, length = 128, comment = "限购的玩家已购情况")
	ShopLimitJSON,

	/* 兑换商店相关 */
	// 付费刷新次数
	@Column(type = String.class, length = 128, comment = "兑换商店刷新次数格式  试炼，竞技，公会,众神{1,2,1，1}",defaults = "0,0,0,0")
	ShopExResetTimes,
	@Column(type = String.class, length = 32, comment = "每日定点刷新商店记录{0,0,0}",defaults = "")
	RefreshRecord,
	
	// 试炼商店相关
	@Column(type = String.class, length = 512, comment = "试炼商店限购物品已购买次数", defaults = "")
	TrialShopBuyNum,
	// 竞技场商店相关
	@Column(type = String.class, length = 512, comment = "竞技场商店限购物品已购买次数", defaults = "")
	CompeteShopBuyNum, 
	// 公会商店相关
	@Column(type = String.class, length = 512, comment = "公会商店限购物品已购买次数", defaults = "")
	GuildShopBuyNum,
	// 众神之战商店相关
	@Column(type = String.class, length = 512, comment = "众神之战商店限购物品已购买次数", defaults = "")
	GWarShopBuyNum,
	
	
	// 神秘商店相关
	@Column(type = int.class, comment = "付费刷新次数")
	ShopMysRefreshTimes, 
	@Column(type = String.class, length = 128, comment = "玩家刷新后得到的商品列表", defaults = "")
	ShopMysRefreshJSON,
	// 魂点商店相关
	@Column(type = int.class, comment = "付费刷新次数")
	ShopSERefreshTimes, 
	@Column(type = String.class, length = 128, comment = "玩家刷新后得到的商品列表", defaults = "")
	ShopSERefreshJSON,

	;
}