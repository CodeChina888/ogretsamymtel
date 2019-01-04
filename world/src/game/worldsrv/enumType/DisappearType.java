package game.worldsrv.enumType;

/**
 * 创建物件消失类型枚举
 * @author shenjh
 */
public enum DisappearType {
	Disappear(1), // 1正常消失
	Die(2), // 2死亡

	;
	
	private int value;
	
	private DisappearType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
	
}
