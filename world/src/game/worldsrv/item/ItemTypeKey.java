package game.worldsrv.item;

public class ItemTypeKey {
	public static final int equip 				= 200;// 装备
	public static final int equipArmors 		= 201;// 装备-衣服
	public static final int equipLeggings 		= 202;// 装备-裤子
	public static final int equipBoots 			= 203;// 装备-靴子
	public static final int equipCuffs	 		= 204;// 装备-护腕
	public static final int equipRing 			= 205;// 装备-饰品
	public static final int equipWeapon 		= 206;// 装备-武器
	
	public static final int fashion 			= 210;// 时装
	
	public static final int rune 		        = 310;// 命格
	public static final int fashionHenshin 		= 312;// 变身时装卡
	
	public static final int useForAttributes 	= 100;// 使用获得属性-如：钱袋，体力丹等
	public static final int useForDropInfo 		= 110;// 类型为110的礼包，使用后执行DropInfos表
	public static final int summonScoreExchange = 114;// 招募积分兑换道具
	public static final int towerScore 			= 115;// 爬塔积分
	public static final int partner				= 120;// 完整的伙伴类型
	
	//public static final int useForItem 			= 0x04010000;// 使用获得道具-礼包，宝箱
	//public static final int useForCard			= 0x04020000;// 获得自动使用-武将卡
	public static final int partnerChip 		= 401;// 伙伴碎片
	public static final int GodsChip			= 402;// 神兽碎片

	
	public static final int fashionWeapon 		= 0x08000100;// 时装-武器
	public static final int fashionClothes 		= 0x08000200;// 时装-服装*

	/**
	 * 道具是否属于该大类的类型
	 * @param itemType 需要比较鉴定的道具类型
	 * @return
	 */
//	public static boolean isSameType(int baseType, int itemType) {
//		return (baseType & itemType) == baseType;
//	}
	public static boolean isSameType(int baseType, int itemType) {
		return itemType == baseType;
	}

	/**
	 * 返回类型的前16位 OX1200
	 * @param type
	 * @return
	 */
	/*public static int getSubType(int type) {
		return type & 0Xffff0000;
	}*/

	/**
	 * 返回类型的前24位 OX1230（例如：装备-武器-子类）
	 * @param type
	 * @return
	 */
	/*public static int getSubType3(int type) {
		return type & 0Xffffff00;
	}*/

}