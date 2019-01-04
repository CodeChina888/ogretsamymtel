package game.turnbasedsrv.combat.types;

import java.util.List;

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
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.combatRule.CombatRuleBase;
import game.turnbasedsrv.combatRule.CombatRuleFactory;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.PropManager;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfSceneEvent;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.fightParam.ResultParam;
import game.worldsrv.fightParam.TowerParam;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;

public class CombatObjectTower extends CombatObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者镜像
	private TowerParam towerParam;// 额外数据
	private Param param; 

	public CombatObjectTower(Port port, StageObject mapStageObj, int stageSn, int mapSn, int fightType, Param param) {
		super(port, mapStageObj, stageSn, mapSn, fightType);
		
		this.autoFightMap.put(ETeamType.Team1, true);
		this.autoFightMap.put(ETeamType.Team2, true);
		// 提取额外数据
		this.param = param;
		this.towerParam = Utils.getParamValue(this.param, HumanMirrorObject.TowerParam, null);
		if (null == towerParam) {
			Log.fight.error("没有额外数据！error in CompeteParam is null");
		}
		// 初始化额外的场景事件
		this.initExtraSceneEvent();
		
//		// 初始化战斗
//		this.initFightRecord();
	}

	/**
	 * 初始化爬塔场景事件
	 */
	private void initExtraSceneEvent() {
		if (null == towerParam) {
			return;
		}
		// 初始化结算条件
		List<Integer> conditions = towerParam.conditions;
		if (conditions == null) {
			return;
		}
		// 初始化所有场景事件
		for (int condSn : conditions) {
			ConfSceneEvent confEvent = ConfSceneEvent.get(condSn);
			if (confEvent == null) {
				Log.table.error("confEvent配表错误，no find sn={}", condSn);
				continue;
			}
			CombatRuleBase action = CombatRuleFactory.getStageAction(condSn, confEvent.type, confEvent.param);
			if (action != null) {
				action.init(this);
			}
		}
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
		if (fightType == ECrossFightType.FIGHT_TOWER_VALUE) {
			for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
				if(null == humanObj.humanMirrorObj) {
					// 镜像数据
					humanMirrorObj = new HumanMirrorObject(humanObj);
					humanMirrorObj.exParam = this.param;
					humanObj.humanMirrorObj = humanMirrorObj;
				}
			}
		}
		
//		humanMirrorObj.humanMirror.setHpMax(300);
		// 转换数据
		humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);
		humanMirrorObjToFightObj(towerParam.fightHumanMirrorObj, ETeamType.Team2);
		
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
		if (winTeam == ETeamType.Team1) {
			msg.addStar(1);
			msg.addStar(1);
			msg.addStar(1);
		} else {
			msg.addStar(0);
			msg.addStar(0);
			msg.addStar(0);
		}
		msg.setFightType(ECrossFightType.valueOf(fightType));
		for (FightObject obj : this.getFightObjs().values()) {
			DTurnbasedFinishObject.Builder objMsg = DTurnbasedFinishObject.newBuilder();
			objMsg.setId(obj.idFight);
			objMsg.setHpCur(PropManager.inst().getCurHp(obj));
			objMsg.setRageCur(PropManager.inst().getCurRage(obj));
			msg.addObjList(objMsg.build());
		}
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
		
		HumanMirrorObject fightHumanMirrorObj = towerParam.fightHumanMirrorObj;
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

		canQuickFight = true;
	}

	/**
	 * 录像
	 */
	@Override
	public void setRecordCampInfo() {
		HumanMirrorObject fightHumanMirrorObj = towerParam.fightHumanMirrorObj;
		if (fightHumanMirrorObj != null) {
			JSONObject json2 = fightHumanMirrorObj.toFightJson();
			this.fightRecord.setRightInfo(json2.toJSONString());
		}

		JSONObject json = this.humanMirrorObj.toFightJson();
		this.fightRecord.setLeftInfo(json.toJSONString());
	}
}
