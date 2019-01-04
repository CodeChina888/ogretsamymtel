package game.turnbasedsrv.param;

import game.turnbasedsrv.fightObj.FightObject;
import game.worldsrv.config.ConfPartnerProperty;

public class FightParamInitPropMonsterConf extends FightParamBase {
	public FightObject fightObj;
	public ConfPartnerProperty confMonster;
	public int lv;
	
	public FightParamInitPropMonsterConf(FightObject fightObj, ConfPartnerProperty confMonster, int lv) {
		this.fightObj = fightObj;
		this.confMonster = confMonster;
		this.lv = lv;
	}
}
