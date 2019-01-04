package game.worldsrv.enumType;

/**
 * 攻击类型枚举
 */
public enum AtkType {
	Mag(1), 			// 1法攻
	Phy(2), 			// 2物攻
	;
	
	private int value;

	private AtkType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
