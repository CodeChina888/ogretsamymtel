package game.worldsrv.enumType;

public enum HumanScopeType {
	ALL("所有"), 
	HUMAN("玩家"), 
	STAGE("地图"), 
	COUNTRY("国家"), 
	GUILD("公会"), 
	TEAM("组队"), 
	TEAMSETUP("发起组队"),
	;
	
	private String value;
	
	private HumanScopeType(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
	
}
