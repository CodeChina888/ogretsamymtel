package turnbasedsrv.fightObj;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import game.msg.Define.DTurnbasedObject;
import game.msg.Define.EPosType;
import game.msg.Define.ETurnbasedObjectType;
import turnbasedsrv.buff.Buff;
import turnbasedsrv.skill.Skill;
import turnbasedsrv.stage.FightStageObject;

public class FightMonsterObject extends FightObject {
	public boolean isBoss = false;
	public boolean isShowBossInfo = false;

	public FightMonsterObject(FightStageObject stageObj) {
		super(stageObj);
	}

	@Override
	public boolean isMonster() {
		return true;
	}

	@Override
	public boolean isBoss() {
		return isBoss;
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
		msg.setType(ETurnbasedObjectType.FightMonster);
		msg.setId(idFight);
		msg.setSn(sn);
		msg.setPos(EPosType.valueOf(pos));

		// 属性
		msg.setProp(prop.createMsg(this));

		// 技能
		if (specialSkill != null) {
			msg.addSkillList(specialSkill.createMsg());
		}
		for (Skill skill : commonSkillList) {
			msg.addSkillList(skill.createMsg());
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
