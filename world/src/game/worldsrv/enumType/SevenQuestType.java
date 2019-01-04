package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.worldsrv.enumType.SevenQuestType;

public enum SevenQuestType {
	HumanLvUp(1), 			// 人物升级
	RepPass(2),				// 副本通关
	Login(3), 				// 登陆赠送
	SignIn(4),				// 签到领奖
	EquipIntensify(5),		// 装备强化(X件X级)
	EquipStarUp(6),			// 装备升星(X件X阶X星)
	GenRecruit(7), 			// 武将招募
	GenAdvUp(8),			// 武将进阶(X个X阶)
	CompeteFight(9), 		// 竞技场挑战
	CombatTotal(10),		// 总战力达到X
	PayRMBTotal(11), 		// 充值累计达X
	HalfBuy(12), 			// 半价抢购
	
	;

	private int value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, Integer> mapEnums = new HashMap<>();

	static {
		for (SevenQuestType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private SevenQuestType(int value) {
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
	public static SevenQuestType get(String name) {
		SevenQuestType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static SevenQuestType getByValue(int value) {
		SevenQuestType type = null;
		if (containsValue(value)) {
			for (SevenQuestType t : values()) {
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
		for (SevenQuestType k : SevenQuestType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
