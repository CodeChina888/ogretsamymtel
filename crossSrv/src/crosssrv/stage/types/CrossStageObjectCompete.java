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
import game.worldsrv.entity.HumanMirror;
import game.worldsrv.fightParam.CompeteParam;
import game.worldsrv.fightParam.ResultParam;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.prop.PropManager;

public class CrossStageObjectCompete extends CrossStageObject {
	private HumanMirrorObject humanMirrorObj;// 挑战者
	private CompeteParam competeParam;// 额外数据

	public CrossStageObjectCompete(HumanMirrorObject humanMirrorObj, CrossPort port, long stageId, int stageSn,
			int mapSn, int fightType) {
		super(port, stageId, stageSn, mapSn, fightType, true);
		
		Long combatantId = humanMirrorObj.humanMirror.getId();
		this.combatantIds.add(combatantId);
		this.humanMirrorObj = humanMirrorObj;
		// 提取额外数据
		competeParam = Utils.getParamValue(humanMirrorObj.exParam, HumanMirrorObject.CompeteParam, null);
		if (null == competeParam) {
			Log.fight.error("没有额外数据！error in CompeteParam is null");
		}
		// 设置自动战斗
		this.autoFightMap.put(ETeamType.Team1, true);
		this.autoFightMap.put(ETeamType.Team2, true);
		// 初始化战斗
		this.initFightRecord();
	}

	public CrossStageObjectCompete(FightRecord fightRecord, CrossPort port, long stageId, int stageSn, int mapSn) {
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
	 * 获取我方战力
	 */
	@Override
	protected int getTeam1Combat() {
		int combat = humanMirrorObj.calc_TotalCombat();
		return combat;
	}
	/**
	 * 获取竞技场对方战力
	 */
	@Override
	protected int getTeam2Combat() {
		if (null == competeParam || competeParam.fightHumanMirrorObj == null) {
			return 0;
		}
		// 敌方战力
		int combat = competeParam.fightHumanMirrorObj.calc_TotalCombat();
		return combat;
	}

	/**
	 * 玩家镜像转为战斗对象
	 */
	@Override
	public void initFightObjOverride() {
		// 计算战力压制
		calc_combatSuppress();
		
		// 转换数据
		humanMirrorObjToFightObj(humanMirrorObj, ETeamType.Team1);
		humanMirrorObjToFightObj(competeParam.fightHumanMirrorObj, ETeamType.Team2);
		
		// 战力高的先出手
		if(competeParam.fightHumanMirrorObj.getHumanMirror().getCombat() > humanMirrorObj.getHumanMirror().getCombat()) {
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
		msg.addParam64(competeParam.fightHumanMirrorObj.humanMirror.getId());
		// 设置战斗记录ID
		msg.addParam64(this.fightRecord.getId());
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
		
		HumanMirrorObject fightHumanMirrorObj = competeParam.fightHumanMirrorObj;
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
		HumanMirrorObject humanMirrorObj2 = new HumanMirrorObject(json2);
		humanMirrorObjToFightObj(humanMirrorObj2, ETeamType.Team2);
		
		// 战力高的先出手
		if(competeParam.fightHumanMirrorObj.getHumanMirror().getCombat() > humanMirrorObj.getHumanMirror().getCombat()) {
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
		HumanMirrorObject fightHumanMirrorObj = competeParam.fightHumanMirrorObj;
		if (fightHumanMirrorObj != null) {
			JSONObject json2 = fightHumanMirrorObj.toFightJson();
			this.fightRecord.setRightInfo(json2.toJSONString());
		}

		JSONObject json = this.humanMirrorObj.toFightJson();
		this.fightRecord.setLeftInfo(json.toJSONString());
	}
}
