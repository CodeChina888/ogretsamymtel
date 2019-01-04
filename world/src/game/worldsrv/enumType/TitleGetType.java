package game.worldsrv.enumType;

public enum TitleGetType {
	Vip(1), // 达到指定vip
	Compete(2), // 竞技场排名
	Level(3), // 角色的等级

	;	

	private int value;
	
	private TitleGetType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
