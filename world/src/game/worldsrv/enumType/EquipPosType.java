package game.worldsrv.enumType;

/**
 * 主公装备部位类型
 * @author shenjh
 */
public enum EquipPosType {
	Helmet(1),   		// 装备-头盔	
	Armors(2),   		// 装备-护甲
	Leggings(3), 		// 装备-护腿
	Boots(4), 			// 装备-靴子
	Cuffs(5), 			// 装备-护腕
	Weapon(6), 			// 装备-武器
	Ring(7), 			// 装备-戒指
	Necklace(8), 		// 装备-项链
	
	;
	
	private int value;
	
	private EquipPosType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
