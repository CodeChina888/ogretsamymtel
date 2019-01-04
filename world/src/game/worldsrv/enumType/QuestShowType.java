package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.worldsrv.enumType.QuestShowType;

public enum QuestShowType {
	MainLine(1),	// 主线任务
	BranchLine(2),	// 支线任务
	LvUp(3), 		// 等级提升
	GenUp(4),	 	// X个X阶武将
	ActConsume(5),	// 累计消耗体力
	CombatTotal(6),	// 总战力
	PhasesUp(7), 	// 段位提升
	OfficialUp(8), 	// 加官进爵
	TreasureGet(9),	// 宝物收集
	GenRecruit(10), // 武将招募：猛将聚首
	CombatTeam(11),	// 队伍战力
	
	;
	
	private int value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, Integer> mapEnums = new HashMap<>();

	static {
		for (QuestShowType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private QuestShowType(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}

	/**
	 * 是否存在指定的枚举名
	 */
	public static boolean containsKey(String name) {
		return mapEnums.containsKey(name);
	}
	/**
	 * 是否存在指定的枚举值
	 */
	public static boolean containsValue(int value) {
		return mapEnums.containsValue(value);
	}
	
	/**
	 * 获取指定枚举名的枚举
	 */
	public static QuestShowType get(String name) {
		QuestShowType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static QuestShowType getByValue(int value) {
		QuestShowType type = null;
		if (containsValue(value)) {
			for (QuestShowType t : values()) {
				if (t.value() == value) {
					type = t;
					break;
				}
			}
		}
		return type;
	}
	
	/**
	 * 获取枚举名列表
	 */
	public static List<String> toList() {
		List<String> result = new ArrayList<>();
		for (QuestShowType k : QuestShowType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
