package turnbasedsrv.param;

import game.msg.Define.EStanceType;

public class FightParamMonsterInfo extends FightParamBase {
	public int sn;
	public EStanceType stance;// 队伍站位
	public int pos;// 队中位置
	
	public FightParamMonsterInfo(int sn, EStanceType stance, int pos) {
		this.sn = sn;
		this.stance = stance;
		this.pos = pos;
	}
}
