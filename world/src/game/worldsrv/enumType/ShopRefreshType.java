package game.worldsrv.enumType;

/**
 * 
 * @author Neak
 * ConfShopControl.funcId
 */
public enum ShopRefreshType {
	DailyReset_Just(0),  // 只有跨天刷新
	MTReset_Just(1),     // 只能手动刷新
	All_Reset(2),        // 支持手动，自动，跨天刷新
	Un_Reset(3),		 // 无法刷新
	;
	
	private int value;
	
	private ShopRefreshType(int value){
		this.value = value;
	}
	
	public int value() {
		return value;
	}
	
	/**
	 * 根据int类型的type，获取Key
	 * @return
	 */
	public static ShopRefreshType getByValue(int value) {
		for (ShopRefreshType k : values()) {
			if (k.value == value)
				return k;
		}
		return null;
	}
}
