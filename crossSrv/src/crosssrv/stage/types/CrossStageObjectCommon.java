package crosssrv.stage.types;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONObject;

import core.support.Utils;
import crosssrv.entity.FightRecord;
import crosssrv.seam.CrossPort;
import crosssrv.stage.CrossStageObject;
import crosssrv.support.Log;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.fightParam.PassParam;
import game.worldsrv.support.C;
import turnbasedsrv.fightObj.FightMonsterObject;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.prop.PropManager;

public class CrossStageObjectCommon extends CrossStageObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者
	private PassParam passParam;// 额外数据
	
	public CrossStageObjectCommon(HumanMirrorObject humanMirrorObj, CrossPort port, long stageId, int stageSn, int mapSn,
			int fightType) {
		super(port, stageId, stageSn, mapSn, fightType, true);
		
		Long combatantId = humanMirrorObj.humanMirror.getId();
		this.combatantIds.add(combatantId);
		this.humanMirrorObj = humanMirrorObj;
		// 提取额外数据
		passParam = Utils.getParamValue(humanMirrorObj.exParam, HumanMirrorObject.PassParam, null);
		if (null == passParam) {
			Log.fight.error("没有额外数据！error in PassParam is null");
		}
		// 设置自动战斗
		this.autoFightMap.put(ETeamType.Team2, true);
	}

	public CrossStageObjectCommon(FightRecord fightRecord, CrossPort port, long stageId, int stageSn, int mapSn) {
		super(fightRecord, port, stageId, stageSn, mapSn);
		
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
		// 转换数据
		humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);
		// 转换怪物对象
		sceneMonsterToFightObj();
		
		// 战力高的先出手
		if(confInstStage != null && confInstStage.power > humanMirrorObj.getHumanMirror().getCombat()) {
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
		msg.setWinTeam(winTeam);
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
		return json;
	}

	/**
	 * 设置录像结果战斗数据
	 * 
	 * @param json
	 * @param msg
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
		super.initFightObjFromRecord();
		canQuickFight = true;
	}
}
