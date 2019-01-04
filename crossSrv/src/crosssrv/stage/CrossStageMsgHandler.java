package crosssrv.stage;

import core.support.observer.MsgReceiver;
import crosssrv.character.CombatantObject;
import crosssrv.seam.msg.MsgParam;
import game.msg.MsgCross.CSCrossStageEnter;
import game.msg.MsgTurnbasedFight.CSTurnbasedActionEnd;
import game.msg.MsgTurnbasedFight.CSTurnbasedAutoFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedCastSkill;
import game.msg.MsgTurnbasedFight.CSTurnbasedLeaveFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedMonsterChangeEnd;
import game.msg.MsgTurnbasedFight.CSTurnbasedQuickFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedRoundEnd;
import game.msg.MsgTurnbasedFight.CSTurnbasedSpeed;
import game.msg.MsgTurnbasedFight.CSTurnbasedStartFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedStopFight;

public class CrossStageMsgHandler {

	/**
	 * 玩家准备好 进入地图
	 * 
	 * @param param
	 */
	@MsgReceiver(CSCrossStageEnter.class)
	public void onCSCrossStageEnter(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		CrossStageManager.inst().onCombatantStageEnter(combatantObj);
	}

	/**
	 * 修改变速倍率
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedSpeed.class)
	public void onCSTurnbasedSpeed(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		CSTurnbasedSpeed msg = param.getMsg();
		float speed = msg.getSpeed();
		combatantObj.stageObj.onChangeFightSpeed(speed);
	}

	/**
	 * 修改自动战斗
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedAutoFight.class)
	public void onCSTurnbasedAutoFight(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		CSTurnbasedAutoFight msg = param.getMsg();
		boolean auto = msg.getAuto();
		combatantObj.stageObj.onChangeAutoFight(combatantObj, auto);
	}

	/**
	 * 开始战斗
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedStartFight.class)
	public void onCSTurnbasedStartFight(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		combatantObj.stageObj.onCombatantStartFight(combatantObj);
	}

	/**
	 * 跳过战斗
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedQuickFight.class)
	public void onCSTurnbasedQuickFight(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		combatantObj.stageObj.onCombatantQuickFight(combatantObj);
	}

	/**
	 * 使用技能
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedCastSkill.class)
	public void onCSTurnbasedCastSkill(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		CSTurnbasedCastSkill msg = param.getMsg();
		int sn = msg.getSn();
		int casterId = msg.getCasterId();
		combatantObj.stageObj.onCombatantCastSkill(combatantObj, casterId, sn);
	}

	/**
	 * 回合结束
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedRoundEnd.class)
	public void onCSTurnbasedRoundEnd(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		CSTurnbasedRoundEnd msg = param.getMsg();
		int round = msg.getRound();
		combatantObj.stageObj.onCombatantRoundEnd(combatantObj, round);
	}

	/**
	 * 暂停
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedStopFight.class)
	public void onCSTurnbasedStopFight(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		CSTurnbasedStopFight msg = param.getMsg();
		boolean isStop = msg.getStop();
		combatantObj.stageObj.onCombatantStopFight(combatantObj, isStop);
	}

	/**
	 * 退出战斗
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedLeaveFight.class)
	public void onCSTurnbasedLeaveFight(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		combatantObj.stageObj.onCombatantLeaveFight(combatantObj);
	}
	
	/**
	 * 剧情结束
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedActionEnd.class)
	public void onCSTurnbasedActionEnd(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		CSTurnbasedActionEnd msg = param.getMsg();
		int select = msg.getSelect();
		combatantObj.stageObj.onMsgActionEnd(combatantObj,select);
	}
	/**
	 * 战斗者出入场表现结束
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedMonsterChangeEnd.class)
	public void onCSTurnbasedMonsterChangeEnd(MsgParam param) {
		CombatantObject combatantObj = param.getCombatantObject();
		combatantObj.stageObj.onMsgMonterChangeEnd(combatantObj);
	}
}