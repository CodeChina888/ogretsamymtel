package game.platform.enumType;

public enum RequestParseType {
	//解析类型 1是requestParam， 2是requestBody
	RequestParam(1),
	RequestBody(2),
	
	;
	
	private int value;
	
	private RequestParseType(int value) {
		this.value = value;
	}
		
	public int value() {
		return value;
	}
	
	/**
	 * 根据int类型的type，获取Key
	 * @return
	 */
	public static RequestParseType getByValue(int value) {
		for (RequestParseType k : values()) {
			if (k.value == value)
				return k;
		}
		return null;
	}
}
