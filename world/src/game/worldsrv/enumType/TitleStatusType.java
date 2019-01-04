package game.worldsrv.enumType;

public enum TitleStatusType {
	PUTON(1), // 已穿戴
	TAKEOFF(2), // 已激活但未穿戴
	NONACTIVE(3), // 未激活
	;

	private int value;
	
	private TitleStatusType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
