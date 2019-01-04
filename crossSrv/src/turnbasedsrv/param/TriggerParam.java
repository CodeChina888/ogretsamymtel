package turnbasedsrv.param;

import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.skill.SkillCastData;
import turnbasedsrv.stage.FightStageObject;

public class TriggerParam {
	public FightStageObject stageObj;
	public FightObject fightObj;
	public FightObject targetObj;
	public SkillCastData skillData;

	public TriggerParam() {

	}

	public TriggerParam(FightStageObject stageObj) {
		this.stageObj = stageObj;
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
