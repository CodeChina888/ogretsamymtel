package game.worldsrv.enumType;

/**
 * 创建物件出现类型枚举
 * @author shenjh
 */
public enum AppearType {
	Appear(1), // 1正常出现
	Revive(2), // 2复活

	;

	private int value;
	
	private AppearType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
	
}
