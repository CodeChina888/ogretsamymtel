package crosssrv.stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Distr;
import crosssrv.character.CombatantObject;
import crosssrv.entity.FightRecord;
import crosssrv.seam.CrossPort;
import crosssrv.stage.types.CrossStageObjectCommon;
import crosssrv.stage.types.CrossStageObjectCompete;
import crosssrv.stage.types.CrossStageObjectDailyRep;
import crosssrv.stage.types.CrossStageObjectFriendBoss;
import crosssrv.stage.types.CrossStageObjectGuildRep;
import crosssrv.stage.types.CrossStageObjectInstance;
import crosssrv.stage.types.CrossStageObjectNewbie;
import crosssrv.stage.types.CrossStageObjectTower;
import crosssrv.stage.types.CrossStageObjectWorldBoss;
import game.msg.Define.ECrossFightType;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.fightParam.ResultParam;
import game.worldsrv.support.D;

@DistrClass(importClass = { CombatantObject.class, List.class, FightRecord.class, HumanMirrorObject.class })
public class CrossStageService extends Service {

	@SuppressWarnings("unused")
	private Map<Integer, CrossStageObject> originalMap = new HashMap<Integer, CrossStageObject>();

	public CrossStageService(CrossPort port) {
		super(port);
	}

	@Override
	public Object getId() {
		return D.CROSS_SERV_STAGE_DEFAULT;
	}

	/**
	 * 创建副本地图
	 * 
	 * @param combatantId
	 * @param stageID
	 * @param stageSn
	 * @param fightType
	 */
	@DistrMethod
	public void createStageInstance(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		CrossStageObject stage = new CrossStageObjectInstance(humanMirrorObj, (CrossPort) port, stageID, stageSn, 
				mapSn, fightType);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}

	/**
	 * 创建副本地图
	 * 
	 * @param combatantIdList
	 * @param stageID
	 * @param stageSn
	 * @param fightType
	 */
	@DistrMethod
	public void createStageDailyRep(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		CrossStageObject stage = new CrossStageObjectDailyRep(humanMirrorObj, (CrossPort) port, stageID, stageSn, 
				mapSn, fightType);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}

	/**
	 * 创建普通地图
	 * 
	 * @param combatantIdList
	 * @param stageID
	 * @param stageSn
	 * @param fightType
	 */
	@DistrMethod
	public void createStageCommon(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		CrossStageObject stage = new CrossStageObjectCommon(humanMirrorObj, (CrossPort) port, stageID, stageSn, 
				mapSn, fightType);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}

	/**
	 * 创建新手地图
	 * 
	 * @param combatantIdList
	 * @param stageID
	 * @param stageSn
	 * @param fightType
	 */
	@DistrMethod
	public void createStageNewbie(long combatantId, long stageID, int stageSn, int mapSn, int fightType) {
		CrossStageObject stage = new CrossStageObjectNewbie(combatantId, (CrossPort) port, stageID, stageSn, 
				mapSn, fightType);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建竞技场地图
	 * 
	 * @param humanMirrorObj
	 * @param stageID
	 * @param stageSn
	 * @param mapSn
	 * @param fightType
	 */
	@DistrMethod
	public void createStageCompete(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn,
			int fightType) {
		CrossStageObject stage = new CrossStageObjectCompete(humanMirrorObj, (CrossPort) port, stageID, stageSn, 
				mapSn, fightType);
		stage.startup();
		
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建世界boss地图
	 * 
	 * @param humanMirrorObj
	 * @param stageID
	 * @param stageSn
	 * @param mapSn
	 * @param fightType
	 */
	@DistrMethod
	public void createStageWorldBoss(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn,
			int fightType) {
		CrossStageObject stage = new CrossStageObjectWorldBoss(humanMirrorObj, (CrossPort) port, stageID, stageSn,
				mapSn, fightType);
		stage.startup();
		
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建爬塔地图
	 * 
	 * @param humanMirrorObj
	 * @param stageID
	 * @param stageSn
	 * @param mapSn
	 * @param fightType
	 */
	@DistrMethod
	public void createStageTower(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		CrossStageObject stage = new CrossStageObjectTower(humanMirrorObj, (CrossPort) port, stageID, stageSn, mapSn, fightType);
		stage.startup();
		
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建强敌地图
	 * 
	 * @param humanMirrorObj
	 * @param stageID
	 * @param stageSn
	 * @param mapSn
	 * @param fightType
	 */
	@DistrMethod
	public void getRecordFriendBoss(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn,
			int fightType) {
		CrossStageObject stage = new CrossStageObjectFriendBoss(humanMirrorObj, (CrossPort) port, stageID, stageSn,
				mapSn, fightType);
		ResultParam resultParam = stage.startupQuick();
		port.returns(resultParam);
	}
	
	/**
	 * 创建公会副本地图
	 * 
	 * @param humanMirrorObj
	 * @param stageID
	 * @param stageSn
	 * @param mapSn
	 * @param fightType
	 */
	@DistrMethod
	public void getRecordGuildRep(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn,
			int fightType) {
		CrossStageObject stage = new CrossStageObjectGuildRep(humanMirrorObj, (CrossPort) port, stageID, stageSn, mapSn,
				fightType);
		ResultParam resultParam = stage.startupQuick();
		port.returns(resultParam);
	}

	/**
	 * 销毁地图
	 * 
	 * @param gameId
	 */
	@DistrMethod
	public void destroy(long stageId) {
		CrossStageObject stage = ((CrossPort) port).getStageObject(stageId);
		if (stage != null) {
			stage.destory();
		}
	}

	/**
	 * 设定时间销毁地图
	 * 
	 * @param gameId
	 */
	@DistrMethod
	public void waitDestroy(long stageId) {
		CrossStageObject stage = ((CrossPort) port).getStageObject(stageId);
		if (stage != null) {
			stage.waitDestory();
		}
	}

	/**
	 * 有人离开战斗
	 * 
	 * @param stageId
	 * @param humanId
	 */
	@DistrMethod
	public void leaveStage(long stageId, long humanId) {
		CrossStageObject stage = ((CrossPort) port).getStageObject(stageId);
		if (stage != null) {
			stage.leaveStage(humanId);
		}
	}

	public long getAutoId() {
		return Port.applyId();
	}

	@DistrMethod
	public void createStageByFightRecord(FightRecord fightRecord, long stageID) {
		int fightType = fightRecord.getFightType();
		int stageSn = fightRecord.getStageSn();
		int mapSn = fightRecord.getMapSn();
		switch (fightType) {
		case ECrossFightType.FIGHT_INSTANCE_VALUE: {
			CrossStageObject stage = new CrossStageObjectInstance(fightRecord, (CrossPort) port, stageID, stageSn,
					mapSn);
			stage.startup();

			String portId = stage.getPort().getId();
			port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
		}
			break;
		case ECrossFightType.FIGHT_DAILYREP_VALUE: {
			CrossStageObject stage = new CrossStageObjectDailyRep(fightRecord, (CrossPort) port, stageID, stageSn,
					mapSn);
			stage.startup();

			String portId = stage.getPort().getId();
			port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
		}
			break;
		case ECrossFightType.FIGHT_FRIEND_BOSS_VALUE: {
			CrossStageObject stage = new CrossStageObjectFriendBoss(fightRecord, (CrossPort) port, stageID, stageSn,
					mapSn);
			stage.startup();

			String portId = stage.getPort().getId();
			port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
		}
			break;
		case ECrossFightType.FIGHT_WORLD_BOSS_VALUE: {
			CrossStageObject stage = new CrossStageObjectWorldBoss(fightRecord, (CrossPort) port, stageID, stageSn,
					mapSn);
			stage.startup();

			String portId = stage.getPort().getId();
			port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
		}
			break;
		case ECrossFightType.FIGHT_GUILD_REP_VALUE: {
			CrossStageObject stage = new CrossStageObjectGuildRep(fightRecord, (CrossPort) port, stageID, stageSn,
					mapSn);
			stage.startup();

			String portId = stage.getPort().getId();
			port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
		}
			break;
		case ECrossFightType.FIGHT_COMMON_VALUE: 
		case ECrossFightType.FIGHT_COMPETE_VALUE:  
		case ECrossFightType.FIGHT_TOWER_VALUE: {
			CrossStageObject stage = new CrossStageObjectCommon(fightRecord, (CrossPort) port, stageID, stageSn, mapSn);
			stage.startup();

			String portId = stage.getPort().getId();
			port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
		}
			break;
		}
	}
}
