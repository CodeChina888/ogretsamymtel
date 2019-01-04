package game.worldsrv.enumType;

public enum PayChargeType {
	NormalPay(1), // 普通充值
	MonthPay(2), // 月卡充值
	SeasonPay(3), // 季卡充值
	;
	
	private int value;
	
	private PayChargeType(int value){
		this.value = value;
	}
	
	public int value() {
		return value;
	}
	
	/**
	 * 根据int类型的type，获取Key
	 * @return
	 */
	public static PayChargeType getByValue(int value) {
		for (PayChargeType k : values()) {
			if (k.value == value)
				return k;
		}
		return null;
	}
}
