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
import game.worldsrv.fightParam.InstanceParam;
import game.worldsrv.support.C;
import turnbasedsrv.fightObj.FightMonsterObject;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.support.JsonKey;

public class CrossStageObjectInstance extends CrossStageObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者
	private InstanceParam instanceParam;// 额外数据
	
	public CrossStageObjectInstance(HumanMirrorObject humanMirrorObj, CrossPort port, long stageId, int stageSn, int mapSn,
			int fightType) {
		super(port, stageId, stageSn, mapSn, fightType, true);
		
		Long combatantId = humanMirrorObj.humanMirror.getId();
		this.combatantIds.add(combatantId);
		this.humanMirrorObj = humanMirrorObj;
		// 提取额外数据
		instanceParam = Utils.getParamValue(humanMirrorObj.exParam, HumanMirrorObject.InstanceParam, null);
		if (null == instanceParam) {
			Log.fight.error("没有额外数据！error in InstanceParam is null");
		}
		// 设置自动战斗
		this.autoFightMap.put(ETeamType.Team2, true);
	}

	public CrossStageObjectInstance(FightRecord fightRecord, CrossPort port, long stageId, int stageSn, int mapSn) {
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
	 * 获取我方战力
	 */
	@Override
	protected int getTeam1Combat() {
		int combat = humanMirrorObj.calc_TotalCombat();
		return combat;
	}
	/**
	 * 获取副本对方推荐战力
	 */
	@Override
	protected int getTeam2Combat() {
		int combat = 0;
		if (confInstStage != null) {
			// 副本推荐战力
			combat = confInstStage.power;
		}
		return combat;
	}

	/**
	 * 判断登录的玩家是否达到要求
	 */
	@Override
	public void initFightObjOverride() {
		// 计算战力压制
		calc_combatSuppress();
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
		msg.setWinTeam(winTeam);
		if (winTeam == ETeamType.Team1) {
			msg.addStar(1);
			boolean star2 = Utils.getParamValue(exParam, JsonKey.Star2, false);
			boolean star3 = Utils.getParamValue(exParam, JsonKey.Star3, false);
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
	 * @param msg
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
