package game.worldsrv.enumType;
/**
 * Item表 Param字段 类型 <100:货币类型 | 后面跟着是值,
 * @author Administrator
 *
 */
public enum ItemParamType {
	
	//GiftBag(100),//礼包类型   100|Produce表的sn
	//DropBag(110),//掉落表礼包类型   110|DropInfos表的sn
	GeneralCard(101),//武将卡类型 101|武将sn
	GeneralRuneEnchant(102),//武将装备附魔上限等级  102|上限等级
	GeneralExp(103),//武将经验 103|武将经验值
	
	;
	
	private int value;
	
	private ItemParamType(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
	
}
