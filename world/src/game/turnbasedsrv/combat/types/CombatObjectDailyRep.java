package game.turnbasedsrv.combat.types;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Utils;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.entity.FightRecord;
import game.worldsrv.fightParam.PassParam;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.C;
import game.worldsrv.support.Log;
import game.turnbasedsrv.fightObj.FightMonsterObject;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.PropManager;
import game.turnbasedsrv.support.JsonKey;

public class CombatObjectDailyRep extends CombatObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者镜像
	private PassParam passParam;// 额外数据
	
	public CombatObjectDailyRep(Port port, StageObject mapStageObj, int stageSn, int mapSn, int fightType) {
		super(port, mapStageObj, stageSn, mapSn, fightType);
		
		// 提取额外数据
		passParam = Utils.getParamValue(humanMirrorObj.exParam, HumanMirrorObject.PassParam, null);
		if (null == passParam) {
			Log.fight.error("没有额外数据！error in PassParam is null");
		}
		// 设置自动战斗
		this.autoFightMap.put(ETeamType.Team2, true);
	}

	public CombatObjectDailyRep(FightRecord fightRecord, Port port, StageObject mapStageObj, 
			int stageSn, int mapSn) {
		super(fightRecord, port, mapStageObj, stageSn, mapSn);
		
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
		// 转换数据
		humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);
		// 转换怪物对象
		sceneMonsterToFightObj();
		
		// 战力高的先出手
		int combat1 = getTeamCombat(ETeamType.Team1);
		if(confInstStage != null && confInstStage.power > combat1) {
			this.priorTeam = ETeamType.Team2;
		} else {
			this.priorTeam = ETeamType.Team1;
		}
		
		// 设置可以跳过战斗
		if (passParam != null && passParam.isPass) {
			canQuickFight = true;
		} else {
			canQuickFight = false;
			for (FightObject obj : this.getFightObjs().values()) {
				if (obj.isMonster()) {
					FightMonsterObject monster = (FightMonsterObject) obj;
					if (monster.isShowBossInfo) {
						bossInfoId = monster.idFight;
						break;
					}
				}
			}
		}
		
		// XXX 测试开启跳过
		if(C.GAME_SERVER_ID == 0 && C.DEBUG_ENABLE) {
			canQuickFight = true;
		}
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
		msg.addStar(Utils.getParamValue(exParam, JsonKey.InstPercent, Utils.I100));

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

	/**********************************************************************************************
	 * 录像
	 ***********************************************************************************************/
	/**
	 * 获取结果数据
	 * 
	 * @return
	 */
	@Override
	public JSONObject getFinishRecordJson() {
		JSONObject json = new JSONObject();
		json.put(JsonKey.InstPercent, Utils.getParamValue(exParam, JsonKey.InstPercent, Utils.I100));
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
		int percent = Utils.I100;
		if (json.containsKey(JsonKey.InstPercent)) {
			percent = json.getIntValue(JsonKey.InstPercent);
		}
		msg.addStar(percent);
		return msg;
	}

	/**
	 * 子类扩展这个函数
	 */
	@Override
	public void initFightObjFromRecord() {
		super.initFightObjFromRecord();
		canQuickFight = true;
	}
}
