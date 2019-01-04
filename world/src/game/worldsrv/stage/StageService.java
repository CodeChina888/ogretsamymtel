package game.worldsrv.stage;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Distr;
import core.support.Param;
import game.msg.Define.ECrossFightType;
import game.worldsrv.fightrecord.RecordInfo;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.instLootMap.Stage.StageObjectLootMapMultiple;
import game.worldsrv.instLootMap.Stage.StageObjectLootMapSingle;
import game.worldsrv.stage.types.*;
import game.worldsrv.support.D;
import game.worldsrv.team.instance.StageObjectInstMoba;
import game.worldsrv.team.instance.StageObjectInstPVE;
import game.worldsrv.team.instance.StageObjectInstPVP;

@DistrClass(importClass = {HumanGlobalInfo.class, RecordInfo.class, Param.class})
public class StageService extends Service {

	public StageService(StagePort port) {
		super(port);
	}

	@Override
	public Object getId() {
		return D.SERV_STAGE_DEFAULT;
	}

	/**
	 * 销毁地图
	 * @param stageId
	 */
	@DistrMethod
	public void destroy(long stageId) {
		StageObject stage = ((StagePort) port).getStageObject(stageId);
		if (stage != null) {
			stage.destory();
		}
	}
	
	/**
	 * 创建一般地图，可多人在同张地图活动
	 * @param mapSn
	 */
	@DistrMethod
	public void createStageCommon(long stageID, int mapSn, int lineNum) {
		//long stageID = Port.applyId();

		int stageSn = 0;// 非副本地图传0
		StageObject stage = new StageObject((StagePort) port, stageID, stageSn, mapSn);
		stage.startup();
		stage.lineNum = lineNum;
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建PVE副本地图
	 * @param stageSn 副本关卡sn
	 * @param mapSn 地图sn
	 */
	@DistrMethod
	public void createStageInstPVE(int stageSn, int mapSn, boolean isMonsterAddProp) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectInstPVE((StagePort) port, stageID, stageSn, mapSn, isMonsterAddProp);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}

	/**
	 * 创建PVP副本地图
	 * @param stageSn 副本关卡sn
	 * @param mapSn 地图sn
	 */
	@DistrMethod
	public void createStageInstPVP(int stageSn, int mapSn, boolean isMonsterAddProp) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectInstPVP((StagePort) port, stageID, stageSn, mapSn, isMonsterAddProp);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}

	/**
	 * 创建PVP副本地图：Moba
	 * @param stageSn 副本关卡sn
	 * @param mapSn 地图sn
	 */
	@DistrMethod
	public void createStageInstMoba(int stageSn, int mapSn, boolean isMonsterAddProp) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectInstMoba((StagePort) port, stageID, stageSn, mapSn, isMonsterAddProp);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建单人副本地图
	 * @param stageSn 副本关卡sn
	 * @param mapSn 地图sn
	 */
	@DistrMethod
	public void createStageInstance(int stageSn, int mapSn, int fightType) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectInstance((StagePort) port, stageID, stageSn, mapSn, fightType);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建单人资源本
	 * @param stageSn 副本关卡sn
	 * @param mapSn 地图sn
	 */
	@DistrMethod
	public void createStageInstRes(int stageSn, int mapSn, int fightType, int instResSn) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectInstRes((StagePort) port, stageID, stageSn, mapSn, fightType, instResSn);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建世界boss副本
	 * @param stageSn 副本关卡sn
	 * @param mapSn 地图sn
	 */
	@DistrMethod
	public void createStageInstBoss(int stageSn, int mapSn, int fightType) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectInstWorldBoss((StagePort) port, stageID, stageSn, mapSn, fightType);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建竞技场地图-开始战斗
	 */
	@DistrMethod
	public void createStageCompete(int stageSn, int mapSn, int fightType, Param param) {
		long stageID = Port.applyId();
		StageObject stage = new StageObjectCompete((StagePort) port, stageID, stageSn, mapSn, fightType, param);
		stage.startup();
		String stageportId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(stageportId), "portId", stageportId);
	}

	/**
	 * 创建仙府地图-开始战斗
	 */
	@DistrMethod
	public void createStageCave(int stageSn, int mapSn, int fightType, Param param) {
		long stageID = Port.applyId();
		StageObject stage = new StageObjectCave((StagePort) port, stageID, stageSn, mapSn, fightType, param);
		stage.startup();
		String stageportId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(stageportId), "portId", stageportId);
	}

	/**
     * 创建公会副本
     */
    @DistrMethod
    public void createStageGuildInst(int stageSn, int mapSn, int fightType,	Param param) {
        long stageID = Port.applyId();

        StageObject stage = new StageObjectGuildInst((StagePort) port, stageID, stageSn, mapSn, fightType, param);
        stage.startup();

        String portId = stage.getPort().getId();
        port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
    }

	/**
	 * 创建爬塔战斗地图
	 */
	@DistrMethod
	public void createStageTower(int stageSn, int mapSn, int fightType,	int fightLayer, int selDiff, Param param) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectTower((StagePort) port, stageID, stageSn, mapSn, fightType, fightLayer, selDiff, param);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建抢夺本pve副本地图
	 * @param stageSn 副本关卡sn
	 * @param mapSn 地图sn
	 */
	@DistrMethod
	public void createStageInstLootMap(int stageSn, int mapSn, int fightType, int humanNumber, int lvType, int lootMapSn) {
		long stageID = Port.applyId();
		StageObject stage = null;
		switch(fightType) {
			case ECrossFightType.FIGHT_LOOTMAP_SINGLE_VALUE: {// 单人
				stage = new StageObjectLootMapSingle((StagePort) port, stageID, stageSn, mapSn, fightType, 
						humanNumber, lvType, lootMapSn);
			} break;
			case ECrossFightType.FIGHT_LOOTMAP_MULTIPLE_VALUE: {// 多人
				stage = new StageObjectLootMapMultiple((StagePort) port, stageID, stageSn, mapSn, fightType, 
						humanNumber, lvType, lootMapSn);
			} break;
		}
		stage.startup();
		
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建切磋真人玩家副本
	 */
	@DistrMethod
	public void createStagePKHuman(int stageSn, int mapSn, int fightType, Param param) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectPKHuman((StagePort) port, stageID, stageSn, mapSn, fightType, param);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建切磋镜像玩家副本
	 */
	@DistrMethod
	public void createStagePKMirror(int stageSn, int mapSn, int fightType, Param param) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectPKMirror((StagePort) port, stageID, stageSn, mapSn, fightType, param);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建录像副本
	 * @param stageSn
	 * @param mapSn
	 */
	@DistrMethod
	public void createStageReplay(RecordInfo record, int stageSn, int mapSn) {
		long stageID = Port.applyId();

		StageObject stage = new StageObjectReplay((StagePort) port, stageID, stageSn, mapSn, record);
		stage.startup();

		String portId = stage.getPort().getId();
		port.returns("stageId", stage.stageId, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
}
