package game.worldsrv.enumType;

/**
 * ActivityCatalog.Sn
 */
public enum ActivityType {
	LevelGift(1),// 等级礼包---<成长计划>
	OnlineGift(2),// 在线奖励
	
	DailySignIn(3),// 每日签到
	DailyPay(5),// 每日充值
	DailyGoldConsume(6),// 每日消费
	DailyBuyActValue(11),// 每日购买体力
	DailyPassRepHard(12),// 每日征战精英副本
	DailyCoinConsume(13),// 每日金币消费
	
	ActInvestment(4),// 投资返利
	ActConsume(7),// 活动消费
	ActCompete(8),// 竞技有礼
	ActGoldExchange(9),// 点金手
	ActDailyPayGift(10),// 每日充值获取超值大礼包
	
	GrowUpFund(14),// 成长基金
	ByStagesReturn(15),// 分期返还
	HalfPrice(16),// 半价购买
	MonthCard(17),// 月卡
	;
	
	private int value;
	
	private ActivityType(int value){
		this.value = value;
	}
	
	public int value() {
		return value;
	}
	
	/**
	 * 根据int类型的type，获取Key
	 * @return
	 */
	public static ActivityType getByValue(int value) {
		for (ActivityType k : values()) {
			if (k.value == value)
				return k;
		}
		return null;
	}
}
