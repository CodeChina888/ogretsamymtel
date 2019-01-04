package game.turnbasedsrv.param;

import game.turnbasedsrv.buff.BuffTriggerData;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.skill.SkillCastData;

public class SourceParam {
	public FightObject fightObj;
	public CombatObject combatObj;
	public SkillCastData skillData;
	public BuffTriggerData buffData;

	public SourceParam() {

	}

	public SourceParam(FightObject fightObj, CombatObject stageObj) {
		this.fightObj = fightObj;
		this.combatObj = stageObj;
	}

	public SourceParam(FightObject fightObj, CombatObject stageObj, SkillCastData skillData) {
		this.fightObj = fightObj;
		this.combatObj = stageObj;
		this.skillData = skillData;
	}

	public SourceParam(FightObject fightObj, CombatObject stageObj, BuffTriggerData buffData) {
		this.fightObj = fightObj;
		this.combatObj = stageObj;
		this.buffData = buffData;
	}

}
