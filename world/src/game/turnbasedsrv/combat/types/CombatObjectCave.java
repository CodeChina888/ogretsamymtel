package game.turnbasedsrv.combat.types;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.FightRecord;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.fightParam.CaveParam;
import game.worldsrv.fightParam.ResultParam;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.PropManager;

public class CombatObjectCave extends CombatObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者镜像
	private CaveParam caveParam;// 额外数据
	private Param param;
	
	public CombatObjectCave(Port port, StageObject mapStageObj, int stageSn, int mapSn, 
			int fightType, Param param) {
		super(port, mapStageObj, stageSn, mapSn, fightType);
		
		this.autoFightMap.put(ETeamType.Team2, true);
		// 提取额外数据
		this.param = param;
		this.caveParam = Utils.getParamValue(this.param, HumanMirrorObject.CaveParam, null);
		if (null == this.caveParam) {
			Log.fight.error("没有额外数据！error in CompeteParam is null");
		}
	}

	public CombatObjectCave(FightRecord fightRecord, Port port, StageObject mapStageObj, 
			int stageSn, int mapSn) {
		super(fightRecord, port, mapStageObj, stageSn, mapSn);
		
		this.autoFightMap.put(ETeamType.Team1, true);
		this.autoFightMap.put(ETeamType.Team2, true);
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
	
	/**
	 * 玩家镜像转为战斗对象
	 */
	@Override
	public void initFightObjOverride() {
		if (fightType == ECrossFightType.FIGHT_COMPETE_VALUE || fightType == ECrossFightType.FIGHT_INST_CAVE_VALUE) {
			for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
				if(null == humanObj.humanMirrorObj) {
					// 镜像数据
					humanMirrorObj = new HumanMirrorObject(humanObj);
					humanMirrorObj.exParam = this.param;
					humanObj.humanMirrorObj = humanMirrorObj;
				}
			}
		}
		// 转换数据
		humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);
		humanMirrorObjToFightObj(caveParam.fightHumanMirrorObj, ETeamType.Team2);
		
		// 计算战力压制
		calcCombatSuppress();
		// 战力高的先出手
		int combat1 = getTeamCombat(ETeamType.Team1);
		int combat2 = getTeamCombat(ETeamType.Team2);
		if(combat2 > combat1) {
			this.priorTeam = ETeamType.Team2;
		} else {
			this.priorTeam = ETeamType.Team1;
		}
		
		// 设置可以跳过战斗
		canQuickFight = true;
	}
	
	/**
	 * 获取结果消息
	 * 
	 * @return
	 */
	@Override
	public SCTurnbasedFinish getFinishMsg() {
		SCTurnbasedFinish.Builder msg = SCTurnbasedFinish.newBuilder();
		if (this.stageStep == CombatStepType.CombatEnd) {
			msg.setIsCombatEnd(true);
		} else {
			msg.setIsCombatEnd(false);
		}
		msg.setWinTeam(winTeam);
		msg.setFightType(ECrossFightType.valueOf(fightType));
		for (FightObject obj : this.getFightObjs().values()) {
			DTurnbasedFinishObject.Builder objMsg = DTurnbasedFinishObject.newBuilder();
			objMsg.setId(obj.idFight);
			objMsg.setHpCur(PropManager.inst().getCurHp(obj));
			objMsg.setRageCur(PropManager.inst().getCurRage(obj));
			msg.addObjList(objMsg.build());
		}
		// 设置战斗对手ID
		msg.addParam64(caveParam.fightHumanMirrorObj.humanMirror.getId());
		// 设置战斗记录ID
		//msg.addParam64(0);//(this.fightRecord.getId());
		return msg.build();
	}

	/**
	 * 获取快速战斗的结果数据，子类扩展此类
	 * 
	 * @return
	 */
	@Override
	public ResultParam getQuickFightInfo() {
		ResultParam param = new ResultParam();
		param.isWin = (winTeam == ETeamType.Team1);
		return param;
	}
	
	/**********************************************************************************************
	 * 录像
	 ***********************************************************************************************/
	/**
	 * 录像：战斗者名
	 */
	@Override
	public void initFightRecordFightName() {
		HumanMirror humanMirror = this.humanMirrorObj.getHumanMirror();
		String leftName = humanMirror.getName();
		int leftCombat = humanMirror.getCombat();
		int leftSn = humanMirror.getSn();
		int leftAptitude = humanMirror.getAptitude();
		String rightName = "CompeteDefender";
		int rightCombat = 0;
		int rightSn = 1;
		int rightAptitude = 1;
		
		HumanMirrorObject fightHumanMirrorObj = caveParam.fightHumanMirrorObj;
		if (fightHumanMirrorObj != null) {
			HumanMirror enemyMirror = fightHumanMirrorObj.getHumanMirror();
			rightName = enemyMirror.getName();
			rightCombat = enemyMirror.getCombat();
			rightSn = enemyMirror.getSn();
			rightAptitude = enemyMirror.getAptitude();
		}
		this.fightRecord.setLeftName(leftName);
		this.fightRecord.setLeftCombat(leftCombat);
		this.fightRecord.setLeftSn(leftSn);
		this.fightRecord.setLeftAptitude(leftAptitude);
		this.fightRecord.setRightName(rightName);
		this.fightRecord.setRightCombat(rightCombat);
		this.fightRecord.setRightSn(rightSn);
		this.fightRecord.setRightAptitude(rightAptitude);
	}

	/**
	 * 获取结果数据
	 * 
	 * @return
	 */
	@Override
	public JSONObject getFinishRecordJson() {
		JSONObject json = new JSONObject();
		return json;
	}

	/**
	 * 设置录像结果战斗数据
	 * 
	 * @param json
	 */
	@Override
	public SCTurnbasedFinish.Builder getFinishRecordFinishMsg(JSONObject json) {
		SCTurnbasedFinish.Builder msg = SCTurnbasedFinish.newBuilder();
		return msg;
	}

	/**
	 * 子类扩展这个函数
	 */
	@Override
	public void initFightObjFromRecord() {
		JSONObject json = Utils.toJSONObject(this.fightRecord.getLeftInfo());
		HumanMirrorObject humanMirrorObj = new HumanMirrorObject(json);
		humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);

		JSONObject json2 = Utils.toJSONObject(this.fightRecord.getRightInfo());
		HumanMirrorObject humanMirrorObj2 = new HumanMirrorObject(json2);
		humanMirrorObjToFightObj(humanMirrorObj2, ETeamType.Team2);
		
		// 战力高的先出手
		if(caveParam.fightHumanMirrorObj.getHumanMirror().getCombat() > humanMirrorObj.getHumanMirror().getCombat()) {
			this.priorTeam = ETeamType.Team2;
		} else {
			this.priorTeam = ETeamType.Team1;
		}
		// 设置可以跳过战斗
		canQuickFight = true;
	}

	/**
	 * 录像
	 */
	@Override
	public void setRecordCampInfo() {
		HumanMirrorObject fightHumanMirrorObj = caveParam.fightHumanMirrorObj;
		if (fightHumanMirrorObj != null) {
			JSONObject json2 = fightHumanMirrorObj.toFightJson();
			this.fightRecord.setRightInfo(json2.toJSONString());
		}

		JSONObject json = this.humanMirrorObj.toFightJson();
		this.fightRecord.setLeftInfo(json.toJSONString());
	}
}
