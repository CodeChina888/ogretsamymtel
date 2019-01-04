package game.worldsrv.enumType;

/**
 * 邮件类型
 */
public enum MailType {
	MaintainNotice(1),		// 维护公告
	CompeteAward(2),		// 竞技场奖励
	RankCombat(3),			// 战力排行奖励
	RankLevel(4),			// 等级排行奖励
	RankTower(5), 			// 爬塔奖励
	BagFull(6),				// 背包已满补发邮件
	ActOpenSeven(7),		// 开服七天奖励
	GuildJoin(8),			// 入会消息
	GuildKickout(9),		// 被踢出公会
	GuildLeaderOffline(10),	// 会长离线通知
	GuildNewLeader(11),		// 更换会长
	GuildRemove(12),		// 解散公会
	WorldBossAward(13),		// 世界BOSS奖励
	GiftCodeExchange(14),	// 礼包编码兑换
	Pay(15),//
	PayGS(16),	//
	; 

	private int value;
	
	private MailType(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}

}
