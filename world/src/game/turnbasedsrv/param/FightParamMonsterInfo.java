package game.turnbasedsrv.param;

import game.msg.Define.EStanceType;

public class FightParamMonsterInfo extends FightParamBase {
	public int sn;
	public int level;
	public int pos;// 队中位置
	public EStanceType stance;// 队伍站位
	
	public FightParamMonsterInfo(int sn, int level, int pos, EStanceType stance) {
		this.sn = sn;
		this.level = level;
		this.pos = pos;
		this.stance = stance;
	}
}
