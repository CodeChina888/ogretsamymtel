package game.worldsrv.enumType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扩展属性类型枚举，一些有别于propType的字段，各个模块可能都要用到。
 */
public enum PropExtType {
	MpCur("MpCur"), 					// 加或扣 当前魔法值，例如：填1000则加1000魔，填-1000则扣1000魔
	HpCur("HpCur"), 					// 加或扣 当前生命值，例如：填1000则加1000血，填-1000则扣1000血
	HpCurPct("HpCurPct"), 				// 加或扣 当前生命万分比，例如：填1000则 hpCur += (1000/10000)*hpMax
	HpMaxPct("HpMaxPct"), 				// 增加最大生命万分比，例如：填1000则 hpMax += (1000/10000)*hpMax
	AtkPhyPct("AtkPhyPct"), 			// 物理攻击万分比，例如：填1000则 atkPhy += (1000/10000)*atkPhy
	DefPhyPct("DefPhyPct"), 			// 物理防御万分比，例如：填1000则 defPhy += (1000/10000)*defPhy
	AtkMagPct("AtkMagPct"), 			// 法术攻击万分比，例如：填1000则 atkMag += (1000/10000)*atkMag
	DefMagPct("DefMagPct"), 			// 法术防御万分比，例如：填1000则 defMag += (1000/10000)*defMag
	//CritPct("CritPct"), 				// 暴击万分比，例如：填1000则 crit += (1000/10000)*crit
	//AntiCritPct("AntiCritPct"), 		// 坚韧万分比，例如：填1000则 antiCrit += (1000/10000)*antiCrit
	;

	private String value;
	// 存放所有枚举信息，<枚举名，枚举值>
	private static final Map<String, String> mapEnums = new HashMap<>();

	static {
		for (PropExtType type : values()) {
			mapEnums.put(type.name(), type.value());
		}
	}

	private PropExtType(String value) {
		this.value = value;
	}
	
	public String value() {
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
	public static boolean containsValue(String value) {
		return mapEnums.containsValue(value);
	}
	
	/**
	 * 获取指定枚举名的枚举
	 */
	public static PropExtType get(String name) {
		PropExtType type = null;
		if (containsKey(name))
			type = valueOf(name);
		return type;
	}
	/**
	 * 获取指定枚举值的枚举
	 */
	public static PropExtType getByValue(String value) {
		PropExtType type = null;
		if (containsValue(value)) {
			for (PropExtType t : values()) {
				if (t.value().equals(value)) {
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
		for (PropExtType k : PropExtType.values()) {
			result.add(k.name());
		}
		return result;
	}
}
