package game.worldsrv.enumType;

public enum RankIntType {
	Level(0), // 等级排行 
	Combat(1), // 战力排行
	Guild(2), // 公会排行
	Compete(3), // 竞技场排行
	SumCombat(4), // 总战力排行
	Instance(5), // 副本排行
	Fairyland(6) ,// 洞天福地
	Tower(7), // 爬塔排行
	
	//Vip(8), // VIP排行
	
	;

	private int value;
	
	private RankIntType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
