package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.worldsrv.enumType.QuestType;

public enum QuestType {
	TalkNPC(1),				// 对话NPC
	HumanLvUp(2),			// 人物升级
	RepPass(3), 			// 副本通关
	ItemUse(4), 			// 物品使用
	ItemCollect(5), 		// 物品收集
	EquipPutOn(6),			// 装备穿戴
	EquipIntensify(7),		// 装备强化
	ShopBuy(8),				// 商店购买
	HoroscopeDraw(9),		// 占星抽奖
	ActValueConsume(10),	// 体力累计消耗
	CombatTotal(11),		// 总战力达到X
	GenSkillUp(12),			// 武将技能升级
	GenRecruit(13),			// 武将招募
	GenStarUp(14),			// 武将升星
	GenAdvUp(15),			// 武将进阶(X个X阶)
	EnterUI(16),			// 前往界面
	CompeteFight(17),		// 竞技场挑战
	OfficialUp(19),			// 爵位升级
	MWActivate(20),			// 法宝激活(X个X级)
	// 以下为未实现的
	BattleStepUp(18),		// 群雄争霸段位提升
	
	;
	
	private int value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, Integer> mapEnums = new HashMap<>();

	static {
		for (QuestType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private QuestType(int value) {
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
	public static QuestType get(String name) {
		QuestType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static QuestType getByValue(int value) {
		QuestType type = null;
		if (containsValue(value)) {
			for (QuestType t : values()) {
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
		for (QuestType k : QuestType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
