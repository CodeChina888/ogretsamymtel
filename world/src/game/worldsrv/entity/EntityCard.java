package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Card", tableName = "card")
public enum EntityCard {
	@Column(type = long.class, comment = "所属的人物ID")
	HumanId,
	/**
	 * 这个id是snid
	 */
	@Column(type = boolean.class, comment = "是否首次免费招募令抽卡", defaults = "true")
	FreeForCardFirst_BySummonToken,
	@Column(type = boolean.class, comment = "是否首次免费元宝抽卡", defaults = "true")
	FreeForCardFirst_ByGold,
	@Column(type = boolean.class, comment = "是否首次花费招募令抽卡", defaults = "true")
	UserSummonTokenFirst,
	@Column(type = boolean.class, comment = "是否首次花费元宝抽卡", defaults = "true")
	UserGoldFirst,
	@Column(type = long.class, comment = "下次免费招募令抽卡时间", defaults = "0")
	DrawCardFreeTimeBySummonToken,
	@Column(type = long.class, comment = "下次免费元宝抽卡时间", defaults = "0")
	DrawCardFreeTimeByGold,  
	@Column(type = int.class, comment = "今日元宝抽卡次数", defaults = "0")
	TodayDrawByGold, 
	@Column(type = int.class, comment = "今日招募令抽卡次数",defaults = "0")
	TodayDrawBySummonToken,
	@Column(type = int.class, comment = "元宝总抽卡数量",defaults = "0")
	totleNumByGold,
	@Column(type = int.class, comment = "招募令总抽卡数量",defaults = "0")
	totleNumBySummonToken,
	@Column(type = int.class, comment = "今日免费招募令抽卡次数",defaults = "0")
	dailyFreeSummonToken,
	@Column(type = int.class, comment = "兑换第几轮次", defaults = "1")
	ExchangeRound,
	@Column(type = String.class, length = 64, comment = "本轮次每一阶领取状态,0未兑换，1已经兑换", defaults = "0,0,0")
	ExchangeState,
}
