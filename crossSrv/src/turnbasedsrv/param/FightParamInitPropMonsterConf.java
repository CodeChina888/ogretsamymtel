package turnbasedsrv.param;

import game.worldsrv.config.ConfPartnerProperty;
import turnbasedsrv.fightObj.FightObject;

public class FightParamInitPropMonsterConf extends FightParamBase {
	public FightObject fightObj;
	public ConfPartnerProperty confMonster;
	public int lv;
	
	public FightParamInitPropMonsterConf(FightObject fightObj, ConfPartnerProperty conf, int lv) {
		this.fightObj = fightObj;
		this.confMonster = conf;
		this.lv = lv;
	}
}
