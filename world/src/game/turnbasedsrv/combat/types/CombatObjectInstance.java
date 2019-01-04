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
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightMonsterObject;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.PropManager;
import game.turnbasedsrv.support.JsonKey;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.FightRecord;
import game.worldsrv.fightParam.InstanceParam;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.C;

public class CombatObjectInstance extends CombatObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者镜像
	private InstanceParam instanceParam;// 额外数据
	
	public CombatObjectInstance(Port port, StageObject mapStageObj,	int stageSn, int mapSn, 
			int fightType) {
		super(port, mapStageObj, stageSn, mapSn, fightType);
		
		this.autoFightMap.put(ETeamType.Team2, true);
	}

	public CombatObjectInstance(FightRecord fightRecord, Port port, StageObject mapStageObj, 
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
	 * 判断登录的玩家是否达到要求
	 */
	@Override
	public void initFightObjOverride() {
		if (fightType == ECrossFightType.FIGHT_INSTANCE_VALUE) {
			for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
				if(null == humanObj.humanMirrorObj) {
					// 镜像数据
					int star = InstanceManager.inst().getInstStar(humanObj, stageSn);
					instanceParam = new InstanceParam(star);
					humanMirrorObj = new HumanMirrorObject(humanObj);
					humanMirrorObj.exParam = new Param(HumanMirrorObject.InstanceParam, instanceParam);
					humanObj.humanMirrorObj = humanMirrorObj;
				}
			}
		}
		
		// 转换数据
		humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);
		// 转换怪物对象
		sceneMonsterToFightObj();
		
		// 计算战力压制
		calcCombatSuppress();
		// 战力高的先出手
		int combat1 = getTeamCombat(ETeamType.Team1);
		if(confInstStage != null && confInstStage.power > combat1) {
			this.priorTeam = ETeamType.Team2;
		} else {
			this.priorTeam = ETeamType.Team1;
		}
		
		// 设置可以跳过战斗
		if (instanceParam != null && instanceParam.star > 0) {
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
		if (winTeam == ETeamType.Team1) {
			boolean star1 = Utils.getParamValue(exParam, JsonKey.Star1, false);
			boolean star2 = Utils.getParamValue(exParam, JsonKey.Star2, false);
			boolean star3 = Utils.getParamValue(exParam, JsonKey.Star3, false);
			if (star1) {
				msg.addStar(1);
			} else {
				msg.addStar(0);
			}
			if (star2) {
				msg.addStar(1);
			} else {
				msg.addStar(0);
			}
			if (star3) {
				msg.addStar(1);
			} else {
				msg.addStar(0);
			}
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
		if (winTeam == ETeamType.Team1) {
			json.put(JsonKey.Star1, 1);
			boolean star2 = Utils.getParamValue(exParam, JsonKey.Star2, false);
			boolean star3 = Utils.getParamValue(exParam, JsonKey.Star3, false);
			if (star2) {
				json.put(JsonKey.Star2, 1);
			} else {
				json.put(JsonKey.Star2, 0);
			}
			if (star3) {
				json.put(JsonKey.Star3, 1);
			} else {
				json.put(JsonKey.Star3, 0);
			}
		} else {
			json.put(JsonKey.Star1, 1);
			json.put(JsonKey.Star2, 0);
			json.put(JsonKey.Star3, 0);
		}
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
		int s1 = 0;
		int s2 = 0;
		int s3 = 0;
		if (json.containsKey(JsonKey.Star1)) {
			s1 = json.getIntValue(JsonKey.Star1);
		}
		if (json.containsKey(JsonKey.Star2)) {
			s2 = json.getIntValue(JsonKey.Star2);
		}
		if (json.containsKey(JsonKey.Star3)) {
			s3 = json.getIntValue(JsonKey.Star3);
		}
		msg.addStar(s1);
		msg.addStar(s2);
		msg.addStar(s3);
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
