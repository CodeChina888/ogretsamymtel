package game.worldsrv.enumType;

/**
 * 敌我标识类型枚举
 * @author shenjh
 */
public enum TeamBundleType {
	Enemy(-1), // 敌方
	Neutrality(0), // 中立
	FriendOne(1), // 友方（队伍1）
	FriendTwo(2), // 友方（队伍2）

	;

	private int value;
	
	private TeamBundleType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
