package crosssrv.stage.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONObject;

import crosssrv.entity.FightRecord;
import crosssrv.seam.CrossPort;
import crosssrv.stage.CrossStageObject;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EPosType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.worldsrv.support.C;
import turnbasedsrv.fightObj.FightMonsterObject;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.prop.PropManager;

public class CrossStageObjectNewbie extends CrossStageObject {
	public CrossStageObjectNewbie(long combatantId, CrossPort port, long stageId, int stageSn, int mapSn,
			int fightType) {
		super(port, stageId, stageSn, mapSn, fightType, true);
		
		this.combatantIds.add(combatantId);
		this.autoFightMap.put(ETeamType.Team2, true);
	}

	public CrossStageObjectNewbie(FightRecord fightRecord, CrossPort port, long stageId, int stageSn, int mapSn) {
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
		sceneMonsterToFightObj();

//		List<FightObject> fightObjList = new ArrayList<>();
//		fightObjList.addAll(fightObjs.values());
//		for (FightObject obj : fightObjList) {
//			if (obj.isMonster()) {
//				FightMonsterObject monster = (FightMonsterObject) obj;
//				if (monster.pos >= EPosType.PosMax_VALUE) {
//					this._delFightObj(monster);
//					monster.pos = monster.pos - EPosType.PosMax_VALUE;
//					monster.team = ETeamType.Team1;
//					this._addFightObj(monster);
//				}
//			}
//		}

		if (canQuickFight == false) {
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
