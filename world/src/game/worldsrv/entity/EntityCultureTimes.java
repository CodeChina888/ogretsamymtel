package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 培养次数表
 * @author songy
 *
 */
@Entity(entityName="CultureTimes", tableName="culture_times")
public enum EntityCultureTimes {
	/* 每日签到 */
	@Column(type=long.class, comment="人物id", defaults = "0")
	HumanId,
	@Column(type=int.class, comment="神通修炼次数", defaults = "0")
	AvatarNum,
	@Column(type=int.class, comment="备精炼数", defaults = "0")
	Refine,
	@Column(type=int.class, comment="元宝商店购买数", defaults = "0")
	GoldShop,
	@Column(type=int.class, comment="将魂商店购买数", defaults = "0")
	SoulShop,
	@Column(type=int.class, comment="竞技场商店购买数", defaults = "0")
	CompeteShop,
	@Column(type=int.class, comment="仙玉商店购买数", defaults = "0")
	JadeShop,
	@Column(type=int.class, comment="解锁护法位置数", defaults = "0")
	ServantLock,
	@Column(type=int.class, comment="元宝抽卡数", defaults = "0")
	GoldDraw,
	@Column(type=int.class, comment="参与封印之地数", defaults = "0")
	JoinWorldBoss,
	@Column(type=int.class, comment="封印之地元宝复活", defaults = "0")
	WorldBossGoldResurrection,
	@Column(type=int.class, comment="参与洞天福地", defaults = "0")
	JoinLootMap,
	@Column(type=int.class, comment="竞技场挑战", defaults = "0")
	CompeteChallenge,
	@Column(type=int.class, comment="命格占卜", defaults = "0")
	Rune,

	@Column(type=int.class, comment="爬塔积分", defaults = "0")
	TowerScore,
	@Column(type=int.class, comment="法宝升级", defaults = "0")
	CimeliaLvUp,
	
}