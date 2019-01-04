package crosssrv.groupFight;

/**
 * 多人战斗状态枚举
 */
public enum GroupFightStatus {
	Match(0), // 0匹配状态
	Fight(1), // 1战斗状态
	End(2), // 2结束状态
	;

	private int value;

	private GroupFightStatus(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
