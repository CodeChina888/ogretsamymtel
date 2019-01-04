package crosssrv.stage.types;

import java.util.ArrayList;
import java.util.List;

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
import game.msg.Define.EPosType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.fightParam.GuildParam;
import game.worldsrv.fightParam.ResultParam;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.prop.PropManager;
import turnbasedsrv.support.JsonKey;

public class CrossStageObjectGuildRep extends CrossStageObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者
	private GuildParam guildParam;// 额外数据

	public CrossStageObjectGuildRep(HumanMirrorObject humanMirrorObj, CrossPort port, long stageId, int stageSn,
			int mapSn, int fightType) {
		super(port, stageId, stageSn, mapSn, fightType, true);
		
		this.autoFightMap.put(ETeamType.Team1, true);
		this.autoFightMap.put(ETeamType.Team2, true);
		this.humanMirrorObj = humanMirrorObj;
		// 提取额外数据
		this.guildParam = Utils.getParamValue(humanMirrorObj.exParam, HumanMirrorObject.GuildParam, null);
		if (null == guildParam) {
			Log.fight.error("没有额外数据！error in guildParam is null");
		}
		// 初始化战斗
		this.initFightRecord();
	}

	public CrossStageObjectGuildRep(FightRecord fightRecord, CrossPort port, long stageId, int stageSn, int mapSn) {
		super(fightRecord, port, stageId, stageSn, mapSn);
		
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
		// 生成怪物,0hp的不生成
		List<Integer> excludeList = new ArrayList<>();
		List<Integer> hpList = new ArrayList<>();
		List<Integer> maxHpList = new ArrayList<>();
		for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
			int hp = guildParam.hpCur.get(pos);
			if (hp <= 0) {
				// 已经死亡的怪物
				excludeList.add(pos);
			}
			hpList.add(hp);

			int hpMax = guildParam.hpMax.get(pos);
			maxHpList.add(hpMax);
		}

		sceneMonsterToFightObj(excludeList, 0);

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

		humanMirrorObjToFightObj(this.humanMirrorObj, ETeamType.Team1);

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
		String rightName = "guildRep";
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

		sceneMonsterToFightObj(excludeList, 0);

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
		JSONObject json2 = new JSONObject();
		for (int pos = EPosType.Pos0_VALUE; pos < EPosType.PosMax_VALUE; pos++) {
			String key = JsonKey.HpCur + pos;
			int hp = guildParam.hpCur.get(pos);
			json2.put(key, hp);
			String key2 = JsonKey.HpMax + pos;
			int maxHp = guildParam.hpMax.get(pos);
			json2.put(key2, maxHp);
		}
		this.fightRecord.setRightInfo(json2.toJSONString());

		JSONObject json = this.humanMirrorObj.toFightJson();
		this.fightRecord.setLeftInfo(json.toJSONString());
	}
}
