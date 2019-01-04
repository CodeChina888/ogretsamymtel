package game.platform.enumType;

public enum CallMethodType {
	// 必须与game.worldsrv.integration.PFServiceProxy的EnumCall的value一样
	PAY(1), // 处理支付通知
	GM_CMD(2), // GM命令分发入口
	CSSC_BANS(3), // 封号禁言接口
	GM_QUERY_ROLE(4), // 查询角色
	GM_COUNT_ALL(5),  // 查询在线人数
	GM_QUERY_TOPLEVEL(6);//查询等级排行榜
	;
	
	private int value;
	
	private CallMethodType(int value) {
		this.value = value;
	}
		
	public int value() {
		return value;
	}	
}
