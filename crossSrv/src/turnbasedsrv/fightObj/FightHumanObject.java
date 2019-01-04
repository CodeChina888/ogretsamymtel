package turnbasedsrv.fightObj;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.DTurnbasedObject;
import game.msg.Define.EPosType;
import game.msg.Define.ETurnbasedObjectType;
import turnbasedsrv.buff.Buff;
import turnbasedsrv.skill.Skill;
import turnbasedsrv.stage.FightStageObject;

public class FightHumanObject extends FightObject {

	public FightHumanObject(FightStageObject stageObj) {
		super(stageObj);
	}

	@Override
	public boolean isHuman() {
		return true;
	}

	/**
	 * 转为文本显示
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("super", super.toString()).toString();
	}

	@Override
	public DTurnbasedObject.Builder createMsg() {
		DTurnbasedObject.Builder msg = DTurnbasedObject.newBuilder();
		msg.setType(ETurnbasedObjectType.FightHuman);
		msg.setId(idFight);
		msg.setSn(sn);
		msg.setPos(EPosType.valueOf(pos));

		// 属性
		msg.setProp(prop.createMsg(this));

		// 技能
		for (Skill skill : commonSkillList) {
			msg.addSkillList(skill.createMsg());
		}
		if (specialSkill != null) {
			msg.addSkillList(specialSkill.createMsg());
		}
		for (Skill skill : passiveSkillList) {
			msg.addSkillList(skill.createMsg());
		}
		for (Buff buff : this.buffManager.getAllBuff()) {
			msg.addBuffList(buff.creageMsg());
		}

		return msg;
	}

}
