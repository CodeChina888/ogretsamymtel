package turnbasedsrv.param;

import turnbasedsrv.buff.BuffTriggerData;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.skill.SkillCastData;
import turnbasedsrv.stage.FightStageObject;

public class SourceParam {
	public FightObject fightObj;
	public FightStageObject stageObj;
	public SkillCastData skillData;
	public BuffTriggerData buffData;

	public SourceParam() {

	}

	public SourceParam(FightObject fightObj, FightStageObject stageObj) {
		this.fightObj = fightObj;
		this.stageObj = stageObj;
	}

	public SourceParam(FightObject fightObj, FightStageObject stageObj, SkillCastData skillData) {
		this.fightObj = fightObj;
		this.stageObj = stageObj;
		this.skillData = skillData;
	}

	public SourceParam(FightObject fightObj, FightStageObject stageObj, BuffTriggerData buffData) {
		this.fightObj = fightObj;
		this.stageObj = stageObj;
		this.buffData = buffData;
	}

}
