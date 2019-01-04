package game.worldsrv.enumType;

public enum QuestCompetFrame {

	Auto(2),
	;
	private QuestCompetFrame(int type){
		this.type = type;
	}
	private int type;

	public int getType() {
		return type;
	}
}
