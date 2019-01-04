package game.worldsrv.enumType;

public enum GrowPlanType {
	Achieve(1), // 达成条件
	Discontent(2), // 条件不足
	Reward(3), // 已领奖
	Past(4),// 已过期
	
	; 

	private int value;
	
	private GrowPlanType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
	
}
