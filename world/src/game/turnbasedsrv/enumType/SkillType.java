package game.turnbasedsrv.enumType;

public enum SkillType {
	Special(0), // 特殊技能：大招，爆点
	Common(1), 	// 普攻：无表现效果的
	Rage(2), 	// 怒气技能：有表现效果的
	Joint(3), 	// 合击技能：合击对象存在情况下替换怒气技能
	
	;

	private int value;

	private SkillType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
