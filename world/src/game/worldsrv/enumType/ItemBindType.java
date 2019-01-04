package game.worldsrv.enumType;

public enum ItemBindType {
	NotBind(0), 	// 不绑定
	Bind(1), 		// 绑定
	
	; 
	
	private int value;
	
	private ItemBindType(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
	
}
