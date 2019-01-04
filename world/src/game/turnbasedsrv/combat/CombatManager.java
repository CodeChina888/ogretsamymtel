package game.turnbasedsrv.combat;

import com.google.protobuf.GeneratedMessage;

import core.CallPoint;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define.ETeamType;
import game.msg.MsgIds;
import game.msg.MsgTurnbasedFight.CSTurnbasedActionEnd;
import game.msg.MsgTurnbasedFight.CSTurnbasedAutoFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedCastSkill;
import game.msg.MsgTurnbasedFight.CSTurnbasedRoundEnd;
import game.msg.MsgTurnbasedFight.CSTurnbasedSpeed;
import game.msg.MsgTurnbasedFight.CSTurnbasedStopFight;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.types.StageObjectNewbie;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.observer.EventKey;

public class CombatManager extends ManagerBase {
	/**
	 * 获取实例
	 * @return
	 */
	public static CombatManager inst() {
		return inst(CombatManager.class);
	}
	/**
	 * 同步镜像品质
	 * @param param
	 */
	@Listener(EventKey.StageHumanShow)
	public void _listener_StageHumanShow(Param param) {
		
	}
	
	public void dispatchCombatMsg(StageObject stageObj, long stageId,long connId,CallPoint connPoint, int msgId,GeneratedMessage msg){
		if(!(stageObj instanceof StageObjectNewbie)){
			return;
		}
		HumanObject humanObj = null;
		for(HumanObject obj:stageObj.getHumanObjs().values()){
			humanObj = obj;
		}
		if(msgId == MsgIds.CSStageEnter){
			StageObjectNewbie stageNewbie = (StageObjectNewbie)stageObj;
			//创建humanObj
			HumanObject obj = new HumanObject();
			obj.id = connId;
			obj.connPoint = connPoint;
			obj.stageObj = stageNewbie;
			obj.posNow = new Vector2D();
			obj.teamBundleID = connId;
			obj.isClientStageReady = true;
			obj.fightTeam = ETeamType.Team1;
			stageNewbie.getHumanObjs().put(obj.id, obj);
			humanObj = obj;
			stageObj.start();
		}
		if(humanObj == null){
			return;
		}
		switch(msgId){
		case MsgIds.CSTurnbasedSpeed:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedSpeed(msgParam);
		}
		break;
		case MsgIds.CSTurnbasedAutoFight:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedSpeed(msgParam);
		}
		break;
		case MsgIds.CSTurnbasedStartFight:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedStartFight(msgParam);
		}
		break;
		case MsgIds.CSTurnbasedQuickFight:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedQuickFight(msgParam);
		}
		break;
		case MsgIds.CSTurnbasedCastSkill:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedCastSkill(msgParam);
		}
		break;
		case MsgIds.CSTurnbasedRoundEnd:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedRoundEnd(msgParam);
		}
		break;
		case MsgIds.CSTurnbasedStopFight:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedStopFight(msgParam);
		}
		break;
		case MsgIds.CSTurnbasedActionEnd:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedActionEnd(msgParam);
		}
		break;
		case MsgIds.CSTurnbasedMonsterChangeEnd:
		{
			MsgParam msgParam = new MsgParam(msg,humanObj);
			onCSTurnbasedMonsterChangeEnd(msgParam);
		}
		break;
		}
	}
	
	/**
	 * 修改变速倍率
	 */
	public void onCSTurnbasedSpeed(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CSTurnbasedSpeed msg = msgParam.getMsg();
		float speed = msg.getSpeed();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onChangeFightSpeed(speed);
		}
	}

	/**
	 * 修改自动战斗
	 * @param msgParam
	 */
	public void onCSTurnbasedAutoFight(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CSTurnbasedAutoFight msg = msgParam.getMsg();
		boolean auto = msg.getAuto();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onChangeAutoFight(humanObj, auto);
		}
	}

	/**
	 * 开始战斗
	 */
	public void onCSTurnbasedStartFight(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onCombatantStartFight(humanObj);
		}
	}

	/**
	 * 跳过战斗
	 */
	public void onCSTurnbasedQuickFight(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onCombatantQuickFight(humanObj);
		}
	}

	/**
	 * 使用技能
	 */
	public void onCSTurnbasedCastSkill(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CSTurnbasedCastSkill msg = msgParam.getMsg();
		int sn = msg.getSn();
		int casterId = msg.getCasterId();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onCombatantCastSkill(humanObj, casterId, sn);
		}
	}

	/**
	 * 回合结束
	 */
	public void onCSTurnbasedRoundEnd(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CSTurnbasedRoundEnd msg = msgParam.getMsg();
		int round = msg.getRound();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onCombatantRoundEnd(humanObj, round);
		}
	}

	/**
	 * 暂停
	 */
	public void onCSTurnbasedStopFight(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CSTurnbasedStopFight msg = msgParam.getMsg();
		boolean isStop = msg.getStop();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onCombatantStopFight(humanObj, isStop);
		}
	}
	
	/**
	 * 退出战斗
	 * 
	 */
	public void onCSTurnbasedLeaveFight(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		//CSTurnbasedLeaveFight msg = msgParam.getMsg();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onCombatantLeaveFight(humanObj);
		} 
	}

	/**
	 * 剧情结束
	 */
	public void onCSTurnbasedActionEnd(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CSTurnbasedActionEnd msg = msgParam.getMsg();
		int select = msg.getSelect();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onMsgActionEnd(humanObj,select);
		}
	}
	/**
	 * 战斗者出入场表现结束
	 */
	public void onCSTurnbasedMonsterChangeEnd(MsgParam msgParam) {
		HumanObject humanObj = msgParam.getHumanObject();
		CombatObject combatObj = humanObj.stageObj.getCombatObj(humanObj.getHumanId());
		if(combatObj != null){
			combatObj.onMsgMonterChangeEnd(humanObj);
		}
	}
}