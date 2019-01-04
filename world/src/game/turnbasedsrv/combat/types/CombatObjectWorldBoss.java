package game.turnbasedsrv.combat.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EPosType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfWorldBoss;
import game.worldsrv.entity.FightRecord;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.fightParam.ResultParam;
import game.worldsrv.fightParam.WorldBossParam;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.PropManager;
import game.turnbasedsrv.support.JsonKey;

public class CombatObjectWorldBoss extends CombatObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者镜像
	private WorldBossParam wbParam;// 额外数据
	private Param param;

	public CombatObjectWorldBoss(Port port, StageObject mapStageObj, int stageSn, int mapSn, 
			int fightType, Param param) {
		super(port, mapStageObj, stageSn, mapSn, fightType);
		
		this.autoFightMap.put(ETeamType.Team2, true);
		// 提取额外数据
		this.param = param;
		this.wbParam = Utils.getParamValue(this.param, HumanMirrorObject.WorldBossParam, null);
		if (null == this.wbParam) {
			Log.fight.error("没有额外数据！error in WorldBossParam is null");
		}
	}

	public CombatObjectWorldBoss(FightRecord fightRecord, Port port, StageObject mapStageObj, 
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
		if (fightType == ECrossFightType.FIGHT_WORLD_BOSS_VALUE) {
			for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
				// 镜像数据
				humanMirrorObj = new HumanMirrorObject(humanObj);
				humanMirrorObj.exParam = this.param;
				humanObj.humanMirrorObj = humanMirrorObj;
			}
		}
		
		// 转换数据
		humanMirrorObjToFightObj(this.humanMirrorObj, ETeamType.Team1);
		sceneMonsterToFightObj();
		
		// 改变数据
		for(FightObject obj : this.getFightObjs().values()) {
			if (null == obj) {
				continue;
			}
			if(obj.team == ETeamType.Team1) {
				// 设置玩家加成攻击万分比
				PropManager.inst().addAtkPct(obj, wbParam.addAtkPct);
			} else if(obj.team == ETeamType.Team2) {
				// 设置怪物hp
				int pos = obj.getFightPos();
				PropManager.inst().resetHpMaxHp(obj, wbParam.monsterHpCur.get(pos), wbParam.monsterHpMax.get(pos));
			}
		}
		
		// 玩家先出手
		this.priorTeam = ETeamType.Team1;
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
		
		ConfWorldBoss confWorldBoss = ConfWorldBoss.get(wbParam.bossInstSn);
		if (null == confWorldBoss) {
			Log.table.error("ConfWorldBoss no find sn={}", wbParam.bossInstSn);
			return msg.build();
		}
		ConfInstMonster confInstMonster = ConfInstMonster.get(confWorldBoss.armyId);
		if (null == confInstMonster) {
			Log.table.error("ConfInstMonster no find sn={}", confWorldBoss.armyId);
			return msg.build();
		}
		
		int len = confInstMonster.monsterIds.length;
		List<Integer> harmList = new ArrayList<>();// 对每个位置造成的伤害
		int harmTotal = 0;// 总伤害
		for (int pos = 0; pos < len; pos++) {
			if (confInstMonster.monsterIds[pos] > 0) {
				FightObject obj = this.getObjByPos(pos, ETeamType.Team2);
				if (null == obj) {
					// 死亡
					int harm = wbParam.monsterHpCur.get(pos);
					harmList.add(harm);
					harmTotal += harm;
				} else {
					// 没死，累计伤害
					int curHp = PropManager.inst().getCurHp(obj);
					int harm = wbParam.monsterHpCur.get(pos) - curHp;
					harmList.add(harm);
					harmTotal += harm;
					// 记录对象信息
					DTurnbasedFinishObject.Builder objMsg = DTurnbasedFinishObject.newBuilder();
					objMsg.setId(obj.idFight);
					objMsg.setHpCur(curHp);
					objMsg.setRageCur(PropManager.inst().getCurRage(obj));
					msg.addObjList(objMsg.build());
				}
			} else {
				harmList.add(0);
			}
		}
		
		// 对每个位置造成的伤害
		msg.addAllHarm(harmList);
		// 输出总伤害
		msg.addParam64(harmTotal);
		// 活动副本SN
		msg.addParam32(this.wbParam.actInstSn);
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

		for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
			FightObject obj = this.getObjByPos(pos, ETeamType.Team2);
			if (obj == null) {
				param.hpLeft.add(0);
			} else {
				param.hpLeft.add(PropManager.inst().getCurHp(obj));
			}
		}
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
		String rightName = "worldBoss";
		int rightCombat = 0;
		int rightSn = 1;
		int rightAptitude = 1;
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
		// 生成怪物,0hp的不生成
		List<Integer> excludeList = new ArrayList<>();
		List<Integer> hpList = new ArrayList<>();
		List<Integer> maxHpList = new ArrayList<>();
		for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
			String key = JsonKey.HpCur + pos;
			int hp = json2.getIntValue(key);
			if (hp <= 0) {
				// 已经死亡的怪物
				excludeList.add(pos);
			}
			hpList.add(hp);

			String key2 = JsonKey.HpMax + pos;
			int maxHp = json2.getIntValue(key2);
			maxHpList.add(maxHp);
		}

		sceneMonsterToFightObj();

		// 设置怪物hp
		for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
			FightObject obj = this.getObjByPos(pos, ETeamType.Team2);
			if (obj == null) {
				continue;
			}
			int hp = hpList.get(pos);
			if (hp > 0) {
				int maxHp = maxHpList.get(pos);
				PropManager.inst().resetHpMaxHp(obj, hp, maxHp);
			}
		}

		canQuickFight = true;
	}

	/**
	 * 录像
	 */
	@Override
	public void setRecordCampInfo() {
		// 设置右边数据
		JSONObject json2 = new JSONObject();
		for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
			String key = JsonKey.HpCur + pos;
			int hp = wbParam.monsterHpCur.get(pos);
			json2.put(key, hp);
			String key2 = JsonKey.HpMax + pos;
			int maxHp = wbParam.monsterHpMax.get(pos);
			json2.put(key2, maxHp);
		}
		this.fightRecord.setRightInfo(json2.toJSONString());
		// 设置左边数据
		JSONObject json = this.humanMirrorObj.toFightJson();
		this.fightRecord.setLeftInfo(json.toJSONString());
	}
}
