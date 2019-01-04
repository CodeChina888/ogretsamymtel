package game.worldsrv.enumType;

import game.msg.Define.ELootMapEventType;
import game.msg.Define.ELootMapLevelLimitType;

public enum LootMapType {

	//抢夺本玩家等级区间
	minLevelLimitType(1),
	maxLevelLimitType(10),
	
	//互斥范围3*3
	mutuallyExclusiveX(3),
	mutuallyExclusiveY(3),
	;
	
	private LootMapType(int value){
		this.value = value;
	}
	private int value;
	

	public int value() {
		return value;
	}
	
	/**
	 * 根据int类型的type，获取Key
	 * @return
	 */
	public static LootMapType getByValue(int value) {
		for (LootMapType k : values()) {
			if (k.value == value)
				return k;
		}
		return null;
	}
	
	public static boolean isLevelEvent(ELootMapEventType eventTpe){
		switch(eventTpe){
			case LootMapEventMonster:
			case LootMapEventHaloMonster:
			case LootMapEventResource:
			case LootMapEventBuff:
			case LootMapEventSkill:{
				return true;
			}
			default:
				return false;
		}
	}
	
	public static boolean isRewardEvent(ELootMapEventType eventTpe){
		switch(eventTpe){
			case LootMapEventMonster:
			case LootMapEventHaloMonster:
			case LootMapEventResource:{
				return true;
			}
			default:
				return false;
		}
	}
	
	public static boolean isHumanStop(ELootMapEventType eventTpe){
		switch(eventTpe){
		case LootMapEventBuff:
		case LootMapEventResource:
		case LootMapEventRandomDoor:
		case LootMapEventEggDoor:
		case LootMapEventNextDoor:
		{
			return false;
		}
		default:
			return true;
	}
	}
	
	public static ELootMapLevelLimitType getLevelLimitType(int lv){
		for(int i = minLevelLimitType.value;i <= maxLevelLimitType.value;i++){
			int maxLevel = i*10;
			int minLevel = maxLevel-10+1;
			if(lv >= minLevel && lv <= maxLevel){
				return ELootMapLevelLimitType.valueOf(i);
			}
		}
		return ELootMapLevelLimitType.valueOf(1);
	}
	
	
}

