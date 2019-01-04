package game.worldsrv.enumType;

/**
 * VK职业类型type
 */
public enum ProfessionType {
	Tank(1), 			// 坦克（防御）
	Nanny(2), 			// 奶妈（恢复）
	Dps(3),				// 输出（输出）
	Ctrl(4),			// 控制（控制）
	;
	
	private int value;

	private ProfessionType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
