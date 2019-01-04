package game.turnbasedsrv.param;

import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.skill.SkillCastData;

public class TriggerParam {
	public CombatObject combatObj;
	public FightObject fightObj;
	public FightObject targetObj;
	public SkillCastData skillData;

	public TriggerParam() {

	}

	public TriggerParam(CombatObject stageObj) {
		this.combatObj = stageObj;
	}

	public TriggerParam(FightObject fightObj, FightObject targetObj) {
		this.fightObj = fightObj;
		this.targetObj = targetObj;
	}

	public TriggerParam(SkillCastData skillData, FightObject fightObj) {
		this.skillData = skillData;
		this.fightObj = fightObj;
	}

	public TriggerParam(SkillCastData skillData, FightObject fightObj, FightObject targetObj) {
		this.skillData = skillData;
		this.fightObj = fightObj;
		this.targetObj = targetObj;
	}

}
