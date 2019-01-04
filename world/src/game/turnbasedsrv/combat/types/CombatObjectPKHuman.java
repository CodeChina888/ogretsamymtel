package game.turnbasedsrv.combat.types;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.msg.Define.DTurnbasedFinishObject;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.CombatStepType;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.PropManager;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.fightParam.PKHumanParam;
import game.worldsrv.pk.PKHumanInfo;
import game.worldsrv.stage.StageObject;

public class CombatObjectPKHuman extends CombatObject {
	private HumanMirrorObject humanMirrorObj1;// 队伍1玩家镜像数据（主动攻击者）
	private HumanMirrorObject humanMirrorObj2;// 队伍2玩家镜像数据（被攻击者）
	private PKHumanParam pkHumanParam;// 额外数据
	private Param param; 
	
	private Set<Long> combatFightObjIdSet = new HashSet<>(); 
	
	public CombatObjectPKHuman(Port port, StageObject mapStageObj, int stageSn, int mapSn, int fightType, Param param) {
		super(port, mapStageObj, stageSn, mapSn, fightType, 2);
		
		this.autoFightMap.put(ETeamType.Team1, false);
		this.autoFightMap.put(ETeamType.Team2, false);
		
		// 提取额外数据
		this.param = param;
		if (param == null) {
			return;
		}
		this.pkHumanParam = Utils.getParamValue(this.param, HumanMirrorObject.PKHumanParam, null);
		
		// 加入战斗对象
		combatFightObjIdSet.add(pkHumanParam.team1Human.humanId);
		combatFightObjIdSet.add(pkHumanParam.team2Human.humanId);
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
		PKHumanInfo pkHumanInfo1 = this.pkHumanParam.team1Human;
		HumanObject humanObj1 = mapStageObj.getHumanObjs().get(pkHumanInfo1.humanId);  
		humanMirrorObj1 = new HumanMirrorObject();
		// 初始化模板
		humanMirrorObj1.initMod(humanObj1);
		humanObj1.humanMirrorObj = humanMirrorObj1;
		// 转换数据
		humanMirrorObjToFightObj(humanMirrorObj1, ETeamType.Team1);
		
		PKHumanInfo pkHumanInfo2 = this.pkHumanParam.team2Human;
		HumanObject humanObj2 = mapStageObj.getHumanObjs().get(pkHumanInfo2.humanId);  
		humanMirrorObj2 = new HumanMirrorObject();
		// 初始化模板
		humanMirrorObj2.initMod(humanObj2);
		humanObj2.humanMirrorObj = humanMirrorObj2;
		// 转换数据
		humanMirrorObjToFightObj(humanMirrorObj2, ETeamType.Team2);
		
		// 计算战力压制
		calcCombatSuppress();
		
		// 属性值 * 系数
		multiplyProp(ETeamType.Team1, pkHumanInfo1.propName, pkHumanInfo1.multiply);
		// 属性值 * 系数
		multiplyProp(ETeamType.Team2, pkHumanInfo2.propName, pkHumanInfo2.multiply);

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
	 * 判断登录的玩家是否达到要求，子类重写
	 * 双方是否就绪
	 * @return
	 */
	@Override
	public boolean checkHumanLoginOkOverride() {
		if (mapStageObj.getHumanObjs().size() < 1) {
			return false;
		}
		// 双方就绪
		int readyCount = 0;
		for (HumanObject humanObj : mapStageObj.getHumanObjs().values()) {
			// 不是本场战斗玩家，则continue
			if (!isCombatFightObj(humanObj.getHumanId())) {
				continue;
			}
			// 玩家就绪则++
			if (humanObj.isClientStageReady) {
				readyCount++;
			}
		}
		// 两个都就绪
		return readyCount == 2;
	}
	
	/**
	 * 判断该玩家是否在该场战斗中，子类重写
	 * 适用于同一地图有多场战斗的情况
	 * @return 是否为该战斗的对象
	 */
	public boolean isCombatFightObj(long humanId) {
		return combatFightObjIdSet.contains(humanId);
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
		if (fightType == ECrossFightType.FIGHT_LOOTMAP_MULTIPLE_VALUE) {
			// 多人抢夺本结算设置返回值
			long winHumanId = 0;//胜利方id
			long loseHumanId = 0;//失败方id
			long triggerId = humanMirrorObj1.getHumanId();//发起者id
			if (winTeam == ETeamType.Team1) {
				winHumanId = humanMirrorObj1.getHumanId();
				loseHumanId = humanMirrorObj2.getHumanId();
			} else if (winTeam == ETeamType.Team2) {
				winHumanId = humanMirrorObj2.getHumanId();
				loseHumanId = humanMirrorObj1.getHumanId();
			}
			msg.addParam64(winHumanId);
			msg.addParam64(loseHumanId);
			msg.addParam64(triggerId);
		}
		return msg.build();
	}
	
}
