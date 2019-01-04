package game.platform.http;

public enum RequestKey {	
	
	LOGIN_CHECK(HttpServerHandler.LOGIN_CHECK, 1),		//登陆检查
	PAY_NOTIFY(HttpServerHandler.PAY_NOTIFY, 2),				//充值通知
	IOS_PAY_NOTIFY(HttpServerHandler.IOS_PAY_NOTIFY, 2),				//iOS充值通知
	CSSC(HttpServerHandler.CSSC, 2),						//CSSC平台	  【GM平台转发】
	
	GM_CMD(HttpServerHandler.GM_CMD, 1),					//GM平台命令  【GM平台接口】
	QUERY_ROLE(HttpServerHandler.QUERY_ROLE, 2),			//玩家查询	  【GM平台接口】
	COUNT_ALL(HttpServerHandler.COUNT_ALL, 2),				//查询在线人数【GM平台接口】
	QUERY_TOPLEVEL(HttpServerHandler.QUERY_TOPLEVEL,2),
	;
	
	/**
	 * 构造函数
	 * @param action
	 */
	RequestKey(String action, int parseType) {
		this.action = action;
		this.parseType = parseType;
	}
	
	private String action;	//对应的处理URL
	private int parseType;	//解析类型 1是requestParam， 2是requestBody

	public String getAction() {
		return action;
	}
	public int getParseType() {
		return parseType;
	}

	/**
	 * 通过Key来获取
	 * @param action
	 */
	public static RequestKey getKeyByAction(String action) {
		RequestKey key = null;
		for(RequestKey k : values()) {
			if(k.getAction().equals(action)) {
				key = k;
				break;
			}
		}
		
		return key;
	}
}