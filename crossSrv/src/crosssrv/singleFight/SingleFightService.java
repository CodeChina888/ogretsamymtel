package crosssrv.singleFight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import core.CallPoint;
import core.Node;
import core.Port;
import core.Record;
import core.RemoteNode;
import core.Service;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Config;
import core.support.Distr;
import core.support.Param;
import core.support.Time;
import core.support.Utils;
import crosssrv.combatant.CombatantGlobalInfo;
import crosssrv.combatant.CombatantGlobalServiceProxy;
import crosssrv.entity.FightRecord;
import crosssrv.seam.CrossPort;
import crosssrv.stage.CrossStageServiceProxy;
import crosssrv.support.Log;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.seam.account.AccountServiceProxy;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.fightParam.ResultParam;
import game.worldsrv.fightrecord.RecordInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.support.D;

/**
 * 单人战斗服务类
 * 
 * @author landy
 * @note 外部调用的方法需要加类似前缀SF001_且必须按序增加
 */
@DistrClass(importClass = { List.class, HumanMirrorObject.class, Message.class, })
public class SingleFightService extends Service {
	/**
	 * 战斗超时时间
	 */
	private static long FIGHT_TIMEOUT_TIME = 240 * Time.SEC;

	/**
	 * 成员信息 <玩家Id,玩家信息>
	 */
	private Map<Long, SingleFightHuman> members = new HashMap<>();

	/**
	 * 地图信息
	 */
	public Map<Long, SingleFightStageInfo> stageInfos = new HashMap<>();

	/**
	 * 录像表
	 */
	private Map<Long, FightRecord> mapFightRecords = new HashMap<>();

	/**
	 * 构造函数
	 * 
	 * @param port
	 */
	public SingleFightService(CrossPort port) {
		super(port);
	}

	@Override
	public Object getId() {
		return D.CROSS_SERV_SINGLE_FIGHT;
	}

	/**
	 * 子类重写的心跳
	 */
	public void pulseOverride() {

	}

	/**
	 * 队伍匹配
	 * 
	 * @param humanMirrorObj
	 * @param type
	 * @param nodeIdWorld
	 * @param portIdWorld
	 * @param stageSn
	 * @param mapSn
	 */
	@DistrMethod
	public void SF001_match(HumanMirrorObject humanMirrorObj, int type, String nodeIdWorld, String portIdWorld,
			int stageSn, int mapSn) {
		long humanId = humanMirrorObj.getHumanMirror().getId();
		SingleFightHuman member = members.get(humanId);
		if (member != null) {
			SingleFightStageInfo stageInfo = stageInfos.get(member.stageId);
			if (stageInfo != null) {
				// TODO 目前不做断线重连
				// if (Port.getTime() - stageInfo.startTime >
				// FIGHT_TIMEOUT_TIME) {
				CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(stageInfo.nodeId,
						stageInfo.portId, D.CROSS_SERV_STAGE_DEFAULT);
				prx.destroy(stageInfo.stageId);
				removeMember(humanId);
				SF001_match(humanMirrorObj, type, nodeIdWorld, portIdWorld, stageSn, mapSn);
				return;
				// }
				// matchResult(member);
				// return;
			}
			removeMember(humanId);
			SF001_match(humanMirrorObj, type, nodeIdWorld, portIdWorld, stageSn, mapSn);
			return;
		}

		// 成员信息
		member = new SingleFightHuman(humanMirrorObj, type, nodeIdWorld, portIdWorld);
		members.put(humanId, member);
		// 地图配置信息
		long stageId = Port.applyId();
		Log.cross.info("玩家 {} ({})  pve战斗 type = {} stageSn={},stageId={}", member.getHumanName(), humanId, type,
				stageSn, stageId);

		// 选择分支
		int partId = selectCrossPart();
		String nodeId = Config.getCrossPartDefaultNodeId(partId);
		String portId = D.CROSS_STAGE_PORT_PREFIX + Utils.randomBetween(0, D.CROSS_PORT_STARTUP_NUM-1);
		// 创建房间地图
		createStage(member, nodeId, portId, stageSn, mapSn, stageId, partId);
	}
	
	/**
	 * 选择分支
	 * 
	 * @return
	 */
	private int selectCrossPart() {
		Map<Integer, String> connIpPortMap = Config.crossSrvConnMap.get(Config.CROSS_SERVER_INDEX);
		List<Integer> partNodeList = new ArrayList<>();
		partNodeList.add(0);
		Node curNode = port.getNode();
		List<String> remoteList = new ArrayList<>();
		for (RemoteNode r : curNode.getRemoteNodeAll()) {
			remoteList.add(r.getRemoteId());
		}
		Iterator<Integer> it = connIpPortMap.keySet().iterator();
		while (it.hasNext()) {
			int partId = it.next();
			if (remoteList.contains(Config.getCrossPartDefaultNodeId(partId))) {
				partNodeList.add(partId);
			}
		}
		int partId = 0;
		if (partNodeList.size() > 1) {
			int partIndex = Utils.randomBetween(1, partNodeList.size()) - 1;
			partId = partNodeList.get(partIndex);
		}
		return partId;
	}

	/**
	 * 创建地图
	 * 
	 * @param team1
	 * @param team2
	 * @param nodeId
	 * @param portId
	 * @param stageSn
	 * @param mapSn
	 * @param stageId
	 * @param partId
	 */
	private void createStage(SingleFightHuman human, String nodeId, String portId, int stageSn, int mapSn, long stageId,
			int partId) {
		long humanId = human.getHumanId();
		switch (human.type) {
		case ECrossFightType.FIGHT_INSTANCE_VALUE: {
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.createStageInstance(human.humanMirrorObj, stageId, stageSn, mapSn, human.type);
			prx.listenResult(this::_result_createStagePve, "humanId", humanId, "stageSn", stageSn, "mapSn", mapSn,
					"stageId", stageId, "nodeId", nodeId, "portId", portId, "partId", partId);
			return;
		}
		case ECrossFightType.FIGHT_DAILYREP_VALUE: {
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.createStageDailyRep(human.humanMirrorObj, stageId, stageSn, mapSn, human.type);
			prx.listenResult(this::_result_createStagePve, "humanId", humanId, "stageSn", stageSn, "mapSn", mapSn,
					"stageId", stageId, "nodeId", nodeId, "portId", portId, "partId", partId);
			return;
		}
		case ECrossFightType.FIGHT_COMMON_VALUE: {
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.createStageCommon(human.humanMirrorObj, stageId, stageSn, mapSn, human.type);
			prx.listenResult(this::_result_createStagePve, "humanId", humanId, "stageSn", stageSn, "mapSn", mapSn,
					"stageId", stageId, "nodeId", nodeId, "portId", portId, "partId", partId);
			return;
		}
		case ECrossFightType.FIGHT_COMPETE_VALUE:
		case ECrossFightType.FIGHT_PK_MIRROR_VALUE:{ 
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.createStageCompete(human.humanMirrorObj, stageId, stageSn, mapSn, human.type);
			prx.listenResult(this::_result_createStagePve, "humanId", humanId, "stageSn", stageSn, "mapSn", mapSn,
					"stageId", stageId, "nodeId", nodeId, "portId", portId, "partId", partId);
			return;
		}
		case ECrossFightType.FIGHT_WORLD_BOSS_VALUE: { 
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.createStageWorldBoss(human.humanMirrorObj, stageId, stageSn, mapSn, human.type);
			prx.listenResult(this::_result_createStagePve, "humanId", humanId, "stageSn", stageSn, "mapSn", mapSn,
					"stageId", stageId, "nodeId", nodeId, "portId", portId, "partId", partId);
			return;
		}
		case ECrossFightType.FIGHT_TOWER_VALUE: {
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.createStageTower(human.humanMirrorObj, stageId, stageSn, mapSn, human.type);
			prx.listenResult(this::_result_createStagePve, "humanId", humanId, "stageSn", stageSn, "mapSn", mapSn,
					"stageId", stageId, "nodeId", nodeId, "portId", portId, "partId", partId);
			return;
		}
		default:
			break;
		}
	}

	/**
	 * 房间地图创建完毕回调
	 * 
	 * @param timeout
	 * @param results
	 * @param context
	 */
	private void _result_createStagePve(boolean timeout, Param results, Param context) {
		if (timeout) {
			// 连接不上时，重新选择分支
			long stageId = context.get("stageId");
			long humanId = context.get("humanId");
			SingleFightHuman member = members.get(humanId);
			int mapSn = context.get("mapSn");
			int stageSn = context.get("stageSn");
			int partId = 0;
			String nodeId = Config.getCrossPartDefaultNodeId();
			String portId = Config.getCrossPartDefaultPortId();
			createStage(member, nodeId, portId, stageSn, mapSn, stageId, partId);
			return;
		}
		// 创建完毕，用户切换地图
		long stageId = context.get("stageId");
		long humanId = context.get("humanId");
		SingleFightHuman human = members.get(humanId);
		if (human == null) {
			Log.logObjectNull(Log.fight, "human");
			return;
		}
		int mapSn = context.get("mapSn");
		int stageSn = context.get("stageSn");
		// 配置信息
		String nodeId = context.get("nodeId");
		String portId = context.get("portId");
		int partId = context.get("partId");
		List<CombatantGlobalInfo> infos = new ArrayList<>();
		CombatantGlobalInfo info = new CombatantGlobalInfo(humanId, human.nodeIdWorld, human.portIdWorld,
				ETeamType.Team1, 0, human.stageSn, human.mapSn, -1, human.token, human.humanMirrorObj, 
				stageId, nodeId, portId, partId);
		infos.add(info);
		SingleFightStageInfo stageInfo = new SingleFightStageInfo(humanId, human.type, nodeId, portId, 
				stageId, stageSn, mapSn, partId, Port.getTime());
		this.stageInfos.put(stageId, stageInfo);
		// 传递玩家信息
		CombatantGlobalServiceProxy prx = CombatantGlobalServiceProxy.newInstance(nodeId, 
				Config.getCrossPartDefaultPortId(partId), D.SERV_COMBATANT_GLOBAL);
		prx.register(infos);
		prx.listenResult(this::_result_transmitDataPve, "humanId", humanId, "stageSn", stageSn, "mapSn", mapSn,
				"stageId", stageId);
	}

	/**
	 * 传递玩家信息完毕回调
	 * 
	 * @param results
	 * @param context
	 */
	private void _result_transmitDataPve(Param results, Param context) {
		// 创建完毕，用户切换地图
		long stageId = context.get("stageId");
		long humanId = context.get("humanId");
		SingleFightHuman member = members.get(humanId);
		int stageSn = context.get("stageSn");
		int mapSn = context.get("mapSn");

		// 通知进入战场
		member.stageId = stageId;
		member.stageSn = stageSn;
		member.mapSn = mapSn;
		matchResult(member);
	}
	
	/**
	 * 通知匹配信息
	 * 
	 * @param human
	 */
	private void matchResult(SingleFightHuman human) {
		SingleFightStageInfo stageInfo = stageInfos.get(human.stageId);
		if (stageInfo == null) {
			return;
		}
		// pve
		CallPoint toPoint = new CallPoint(human.nodeIdWorld, Distr.getPortId(D.SERV_HUMAN_GLOBAL), D.SERV_HUMAN_GLOBAL);
		port.call(false, toPoint,
				HumanGlobalServiceProxy.EnumCall.HumanGlobalService_pvpMatchResult_long_int_String_String_int_int_HumanMirrorObject_String_long_int_int,
				"HumanGlobalService_pvpMatchResult",
				new Object[] { human.getHumanId(), human.type, human.token,
						Config.getCrossMainNodeConnIp(stageInfo.partId),
						Config.getCrossMainNodeConnPort(stageInfo.partId), 1, (HumanMirrorObject) null,
						Config.getCrossPartDefaultNodeId(), human.stageId, human.stageSn, human.mapSn });
		return;
	}

	/**
	 * 玩家主动离开战斗
	 * 
	 * @param humanId
	 */
	@DistrMethod
	public void SF002_leave(long humanId) {
		SingleFightHuman member = members.get(humanId);
		long stageId = member.stageId;
		SingleFightStageInfo stageInfo = stageInfos.get(stageId);
		if (stageInfo != null) {
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(stageInfo.nodeId,
					stageInfo.portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.leaveStage(stageId, humanId);
		}
		// 从队伍中移除
		removeMember(humanId);
	}

	/**
	 * 离开队伍/取消排队
	 * 
	 * @param humanId
	 */
	private void removeMember(long humanId) {
		SingleFightHuman member = members.get(humanId);
		if (member == null) {
			return;
		}
		// 从成员中删除
		Log.cross.info("玩家 {} ({}) 离开PVE队列 ", member.getHumanName(), humanId);
		members.remove(humanId);
	}
	
	/**
	 * 战斗结束
	 * 
	 * @param stageId
	 * @param combatants
	 * @param msg
	 * @param winTeam
	 * @param isAlwaysWin
	 */
	@DistrMethod
	public void SF003_StageFightFinish(long stageId, List<Long> combatants, Message msg, int winTeam,
			boolean isAlwaysWin) {
		for (long humanId : combatants) {
			SingleFightHuman member = members.get(humanId);
			if (null == member || member.stageId != stageId) {
				continue;
			}
			this.members.remove(humanId);

			SCTurnbasedFinish.Builder finishMsg = SCTurnbasedFinish.newBuilder((SCTurnbasedFinish) msg);
			ETeamType win = ETeamType.valueOf(winTeam);
			if (!isAlwaysWin) {
				finishMsg.setWinTeam(win);
			}
			CallPoint toPoint = new CallPoint(member.nodeIdWorld, Distr.getPortId(D.SERV_HUMAN_GLOBAL),
					D.SERV_HUMAN_GLOBAL);
			port.call(false, toPoint,
					HumanGlobalServiceProxy.EnumCall.HumanGlobalService_pvpFinishFight_long_int_int_Message,
					"HumanGlobalService_pvpFinishFight",
					new Object[] { humanId, member.type, winTeam, finishMsg.build() });
			return;
		}
	}

	/**
	 * 退出战斗
	 * 
	 * @param stageId
	 * @param combatants
	 */
	@DistrMethod
	public void SF004_StageLeaveFinish(long stageId, List<Long> combatants, long combatantId) {
		// 由客户端发就行了
//		for (long humanId : combatants) {
//			SingleFightHuman member = members.get(humanId);
//			if (null == member || member.stageId != stageId) {
//				continue;
//			}
//
//			CallPoint toPoint = new CallPoint(member.nodeIdWorld, Distr.getPortId(D.SERV_HUMAN_GLOBAL),
//					D.SERV_HUMAN_GLOBAL);
//			port.call(false, toPoint, HumanGlobalServiceProxy.EnumCall.HumanGlobalService_pvpLeaveFight_long_int,
//					"HumanGlobalService_pvpLeaveFight", new Object[] { humanId, member.type });
//			return;
//		}
	}

	/******************************************************************************************
	 * 快速战斗相关
	 ******************************************************************************************/
	@DistrMethod
	public void SF007_quickFight(HumanMirrorObject humanMirrorObj, int type, String serverNodeId, String serverPortId,
			int stageSn, int mapSn) {
		// 地图配置信息
		long stageId = Port.applyId();
		// 选择分支
		int partId = selectCrossPart();
		quickfight(humanMirrorObj, type, serverNodeId, serverPortId, stageSn, mapSn, partId, stageId);
	}

	private void quickfight(HumanMirrorObject humanMirrorObj, int type, String serverNodeId, String serverPortId,
			int stageSn, int mapSn, int partId, long stageId) {
		String nodeId = Config.getCrossPartDefaultNodeId(partId);
		String portId = D.CROSS_STAGE_PORT_PREFIX + Utils.randomBetween(0, D.CROSS_PORT_STARTUP_NUM-1);
		// 创建房间地图
		CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
		switch (type) {
		case ECrossFightType.FIGHT_FRIEND_BOSS_VALUE:
			prx.getRecordFriendBoss(humanMirrorObj, stageId, stageSn, mapSn, type);
			break;
		case ECrossFightType.FIGHT_GUILD_REP_VALUE:
			prx.getRecordGuildRep(humanMirrorObj, stageId, stageSn, mapSn, type);
			break;
		default:

		}
		prx.listenResult(this::_result_quickFight, "serverPortId", serverPortId, "serverNodeId", serverNodeId,
				"humanMirrorObj", humanMirrorObj, "type", type, "stageId", stageId, "stageSn", stageSn, "mapSn", mapSn,
				"nodeId", nodeId, "portId", portId, "partId", partId);
	}

	private void _result_quickFight(boolean timeout, Param results, Param context) {
		if (timeout) {
			int partId = context.get("partId");
			// 连接不上时，重新选择分支
			HumanMirrorObject humanMirrorObj = context.get("humanMirrorObj");
			int type = context.get("type");
			long stageId = context.get("stageId");
			String serverNodeId = context.get("serverNodeId");
			String serverPortId = context.get("serverPortId");
			int stageSn = context.get("stageSn");
			int mapSn = context.get("mapSn");
			if (partId == 0) {
				CallPoint toPoint = new CallPoint(serverNodeId, Distr.getPortId(D.SERV_HUMAN_GLOBAL),
						D.SERV_HUMAN_GLOBAL);
				port.call(false, toPoint,
						HumanGlobalServiceProxy.EnumCall.HumanGlobalService_quickFightResult_int_int_HumanMirrorObject_Param,
						"HumanGlobalService_quickFightResult",
						new Object[] { 1, type, humanMirrorObj, new ResultParam() });
				return;
			}
			quickfight(humanMirrorObj, type, serverNodeId, serverPortId, stageSn, mapSn, partId, stageId);
			return;
		}
		HumanMirrorObject humanMirrorObj = context.get("humanMirrorObj");
		String serverNodeId = context.get("serverNodeId");
		int type = context.get("type");
		ResultParam resultParam = results.get();
		CallPoint toPoint = new CallPoint(serverNodeId, Distr.getPortId(D.SERV_HUMAN_GLOBAL), D.SERV_HUMAN_GLOBAL);
		port.call(false, toPoint,
				HumanGlobalServiceProxy.EnumCall.HumanGlobalService_quickFightResult_int_int_HumanMirrorObject_Param,
				"HumanGlobalService_quickFightResult", new Object[] { 0, type, humanMirrorObj, resultParam });
	}

	/******************************************************************************************
	 * 录像相关
	 ******************************************************************************************/
	/**
	 * 查看录像
	 * 
	 * @param humanId
	 * @param serverNodeId
	 * @param serverPortId
	 * @param recordId
	 */
	@DistrMethod
	public void SF005_ReplayRecord(long humanId, String serverNodeId, String serverPortId, long recordId) {
		FightRecord fightRecord = this.mapFightRecords.get(recordId);
		if (fightRecord == null) {
			String nodeId = Config.getCrossPartDefaultNodeId();
			String portId = Config.getCrossPartDefaultPortId();
			// 加载，异步返回
			SingleFightServiceProxy prx = SingleFightServiceProxy.newInstance(nodeId, portId,
					D.CROSS_SERV_SINGLE_FIGHT);
			prx.SF006_LoadRecord(recordId);
			prx.listenResult(this::_result_replayRecordLoad, "serverPortId", serverPortId, "serverNodeId", serverNodeId,
					"recordId", recordId, "humanId", humanId);
			return;
		}
		createFightRecordStage(recordId, humanId, serverNodeId, serverPortId);
	}

	private void _result_replayRecordLoad(Param results, Param context) {
		long recordId = context.get("recordId");
		long humanId = context.get("humanId");
		String serverNodeId = context.get("serverNodeId");
		String serverPortId = context.get("serverPortId");
		createFightRecordStage(recordId, humanId, serverNodeId, serverPortId);
	}

	private void createFightRecordStage(long recordId, long humanId, String serverNodeId, String serverPortId) {
		FightRecord fightRecord = this.mapFightRecords.get(recordId);
		if (fightRecord == null) {
			CallPoint toPoint = new CallPoint(serverNodeId, Distr.getPortId(D.SERV_HUMAN_GLOBAL), D.SERV_HUMAN_GLOBAL);
			RecordInfo record = new RecordInfo();
			record.id = recordId;
			port.call(false, toPoint,
					HumanGlobalServiceProxy.EnumCall.HumanGlobalService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo,
					"HumanGlobalService_replayRecordResult", new Object[] { humanId, 1, 0, "",
							Config.getCrossMainNodeConnIp(0), Config.getCrossMainNodeConnPort(0), 0, 0, 0, record });
			return;
		}
		// 地图配置信息
		long stageId = Port.applyId();
		// 选择分支
		int partId = selectCrossPart();
		String nodeId = Config.getCrossPartDefaultNodeId(partId);
		String portId = D.CROSS_STAGE_PORT_PREFIX + Utils.randomBetween(0, D.CROSS_PORT_STARTUP_NUM-1);
		// 创建房间地图
		CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
		prx.createStageByFightRecord(fightRecord, stageId);
		prx.listenResult(this::_result_createStageFightRecord, "serverPortId", serverPortId, "serverNodeId",
				serverNodeId, "humanId", humanId, "recordId", recordId, "stageId", stageId, "nodeId", nodeId, "portId",
				portId, "partId", partId);

	}

	private void _result_createStageFightRecord(boolean timeout, Param results, Param context) {
		if (timeout) {
			int partId = context.get("partId");
			// 连接不上时，重新选择分支
			long stageId = context.get("stageId");
			long recordId = context.get("recordId");
			long humanId = context.get("humanId");
			String serverNodeId = context.get("serverNodeId");
			String serverPortId = context.get("serverPortId");
			if (partId == 0) {
				CallPoint toPoint = new CallPoint(serverNodeId, Distr.getPortId(D.SERV_HUMAN_GLOBAL),
						D.SERV_HUMAN_GLOBAL);
				RecordInfo record = new RecordInfo();
				record.id = recordId;
				port.call(false, toPoint,
						HumanGlobalServiceProxy.EnumCall.HumanGlobalService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo,
						"HumanGlobalService_replayRecordResult",
						new Object[] { humanId, 2, 0, "", Config.getCrossMainNodeConnIp(partId),
								Config.getCrossMainNodeConnPort(partId), 0, 0, 0, record });
				return;
			}
			partId = 0;
			FightRecord fightRecord = this.mapFightRecords.get(recordId);
			String nodeId = Config.getCrossPartDefaultNodeId();
			String portId = D.CROSS_STAGE_PORT_PREFIX + Utils.randomBetween(0, D.CROSS_PORT_STARTUP_NUM-1);
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.createStageByFightRecord(fightRecord, stageId);
			prx.listenResult(this::_result_createStageFightRecord, "serverPortId", serverPortId, "serverNodeId",
					serverNodeId, "humanId", humanId, "recordId", recordId, "stageId", stageId, "nodeId", nodeId,
					"portId", portId, "partId", partId);
			return;
		}
		// 创建完毕，用户切换地图
		long stageId = context.get("stageId");
		long humanId = context.get("humanId");
		long recordId = context.get("recordId");
		// 配置信息
		String nodeId = context.get("nodeId");
		String portId = context.get("portId");
		int partId = context.get("partId");
		String serverNodeId = context.get("serverNodeId");
		String serverPortId = context.get("serverPortId");
		FightRecord fightRecord = this.mapFightRecords.get(recordId);
		int stageSn = fightRecord.getStageSn();
		int mapSn = fightRecord.getMapSn();
		String token = "token";
		CombatantGlobalInfo info = new CombatantGlobalInfo(humanId, serverNodeId, serverPortId, ETeamType.Team1, 1,
				stageSn, mapSn, -1, token, null, stageId, nodeId, portId, partId);
		// 传递玩家信息
		CombatantGlobalServiceProxy prx = CombatantGlobalServiceProxy.newInstance(nodeId, 
				Config.getCrossPartDefaultPortId(partId), D.SERV_COMBATANT_GLOBAL);
		prx.register(info);
		prx.listenResult(this::_result_transmitDataRecord, "token", token, "serverPortId", serverPortId, "serverNodeId",
				serverNodeId, "humanId", humanId, "recordId", recordId, "stageId", stageId, "nodeId", nodeId, "portId",
				portId, "partId", partId);
	}

	private void _result_transmitDataRecord(Param results, Param context) {
		long humanId = context.get("humanId");
		String serverNodeId = context.get("serverNodeId");
		String token = context.get("token");
		int partId = context.get("partId");
		long stageId = context.get("stageId");
		long recordId = context.get("recordId");
		FightRecord fightRecord = this.mapFightRecords.get(recordId);
		int fightType = fightRecord.getFightType();
		int stageSn = fightRecord.getStageSn();
		int mapSn = fightRecord.getMapSn();
		RecordInfo recordInfo = getRecordInfo(fightRecord);
		CallPoint toPoint = new CallPoint(serverNodeId, Distr.getPortId(D.SERV_HUMAN_GLOBAL), D.SERV_HUMAN_GLOBAL);
		port.call(false, toPoint,
				HumanGlobalServiceProxy.EnumCall.HumanGlobalService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo,
				"HumanGlobalService_replayRecordResult",
				new Object[] { humanId, 0, fightType, token, Config.getCrossMainNodeConnIp(partId),
						Config.getCrossMainNodeConnPort(partId), stageId, stageSn, mapSn, recordInfo });
	}

	public RecordInfo getRecordInfo(FightRecord fightRecord) {
		RecordInfo info = new RecordInfo();
		info.id = fightRecord.getId();
		info.leftName = fightRecord.getLeftName();
		info.leftSn = fightRecord.getLeftSn();
		info.leftCombat = fightRecord.getLeftCombat();
		info.leftAptitude = fightRecord.getLeftAptitude();
		info.rightName = fightRecord.getRightName();
		info.rightSn = fightRecord.getRightSn();
		info.rightCombat = fightRecord.getRightCombat();
		info.rightAptitude = fightRecord.getRightAptitude();
		return info;
	}

	/**
	 * 加载录像
	 */
	@DistrMethod
	public void SF006_LoadRecord(long recordId) {
		// 加载，异步返回
		long pid = port.createReturnAsync();// 创建一个异步返回
		DB dbPrx = DB.newInstance(FightRecord.tableName);
		dbPrx.get(recordId);
		dbPrx.listenResult(this::_result_loadRecord, "pid", pid, "recordId", recordId);
	}

	private void _result_loadRecord(Param results, Param context) {
		long pid = context.get("pid");
		Record record = results.get();
		if (record != null) {
			FightRecord fightRecord = new FightRecord(record);
			this.mapFightRecords.put(fightRecord.getId(), fightRecord);
		}
		port.returnsAsync(pid);
	}

	/**
	 * 每个service预留空方法
	 * 
	 * @param objs
	 */
	@DistrMethod
	public void SF008_update(Object... objs) {

	}

	/******************************************************************************************
	 * 新手战斗相关
	 ******************************************************************************************/
	/**
	 * 查看录像
	 * 
	 * @param humanId
	 * @param serverNodeId
	 * @param serverPortId
	 * @param recordId
	 */
	@DistrMethod
	public void SF009_NewbieFight(long connId, long humanId, int fightType, String serverNodeId, String serverPortId,
			int stageSn, int mapSn) {
		// 地图配置信息
		long stageId = Port.applyId();
		// 选择分支
		int partId = selectCrossPart();
		String nodeId = Config.getCrossPartDefaultNodeId(partId);
		String portId = D.CROSS_STAGE_PORT_PREFIX + Utils.randomBetween(0, D.CROSS_PORT_STARTUP_NUM-1);
		// 创建房间地图
		CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
		prx.createStageNewbie(humanId, stageId, stageSn, mapSn, fightType);
		prx.listenResult(this::_result_createStageNewbieFight, "serverPortId", serverPortId, "serverNodeId",
				serverNodeId, "humanId", humanId, "connId", connId, "fightType", fightType, "stageId", stageId,
				"stageSn", stageSn, "mapSn", mapSn, "nodeId", nodeId, "portId", portId, "partId", partId);

	}

	private void _result_createStageNewbieFight(boolean timeout, Param results, Param context) {
		if (timeout) {
			int partId = context.get("partId");
			int stageSn = context.get("stageSn");
			int mapSn = context.get("mapSn");
			int fightType = context.get("fightType");
			// 连接不上时，重新选择分支
			long stageId = context.get("stageId");
			long humanId = context.get("humanId");
			long connId = context.get("connId");
			String serverNodeId = context.get("serverNodeId");
			String serverPortId = context.get("serverPortId");
			if (partId == 0) {
				CallPoint toPoint = new CallPoint(serverNodeId, Distr.getPortId(Distr.SERV_GATE), Distr.SERV_GATE);
				port.call(false, toPoint,
						AccountServiceProxy.EnumCall.AccountService_newbieFightResult_long_long_int_int_String_String_int_long_int_int,
						"AccountService_newbieFightResult",
						new Object[] { connId, humanId, 2, fightType, "", Config.getCrossMainNodeConnIp(partId),
								Config.getCrossMainNodeConnPort(partId), stageId, stageSn, mapSn });
				return;
			}
			partId = 0;
			String nodeId = Config.getCrossPartDefaultNodeId();
			String portId = D.CROSS_STAGE_PORT_PREFIX + Utils.randomBetween(0, D.CROSS_PORT_STARTUP_NUM-1);
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(nodeId, portId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.createStageNewbie(humanId, stageId, stageSn, mapSn, fightType);
			prx.listenResult(this::_result_createStageFightRecord, "serverPortId", serverPortId, "serverNodeId",
					serverNodeId, "humanId", humanId, "connId", connId, "fightType", fightType, "stageId", stageId,
					"stageSn", stageSn, "mapSn", mapSn, "nodeId", nodeId, "portId", portId, "partId", partId);
			return;
		}
		// 创建完毕，用户切换地图
		long stageId = context.get("stageId");
		long humanId = context.get("humanId");
		long connId = context.get("connId");
		int stageSn = context.get("stageSn");
		int mapSn = context.get("mapSn");
		int fightType = context.get("fightType");
		// 配置信息
		String nodeId = context.get("nodeId");
		String portId = context.get("portId");
		int partId = context.get("partId");
		String serverNodeId = context.get("serverNodeId");
		String serverPortId = context.get("serverPortId");
		String token = "token";
		CombatantGlobalInfo info = new CombatantGlobalInfo(humanId, serverNodeId, serverPortId, ETeamType.Team1, 1,
				stageSn, mapSn, -1, token, null, stageId, nodeId, portId, partId);
		// 传递玩家信息
		CombatantGlobalServiceProxy prx = CombatantGlobalServiceProxy.newInstance(nodeId, 
				Config.getCrossPartDefaultPortId(partId), D.SERV_COMBATANT_GLOBAL);
		prx.register(info);
		prx.listenResult(this::_result_transmitDataNewbie, "fightType", fightType, "token", token, "serverPortId",
				serverPortId, "serverNodeId", serverNodeId, "humanId", humanId, "connId", connId, "stageId", stageId,
				"stageSn", stageSn, "mapSn", mapSn, "nodeId", nodeId, "portId", portId, "partId", partId);
	}

	private void _result_transmitDataNewbie(Param results, Param context) {
		long humanId = context.get("humanId");
		long connId = context.get("connId");
		int stageSn = context.get("stageSn");
		int mapSn = context.get("mapSn");
		int fightType = context.get("fightType");
		String serverNodeId = context.get("serverNodeId");
		String serverPortId = context.get("serverPortId");
		String token = context.get("token");
		int partId = context.get("partId");
		long stageId = context.get("stageId");
		CallPoint toPoint = new CallPoint(serverNodeId, serverPortId, Distr.SERV_GATE);
		port.call(false, toPoint,
				AccountServiceProxy.EnumCall.AccountService_newbieFightResult_long_long_int_int_String_String_int_long_int_int,
				"AccountService_newbieFightResult",
				new Object[] { connId, humanId, fightType, 0, token, Config.getCrossMainNodeConnIp(partId),
						Config.getCrossMainNodeConnPort(partId), stageId, stageSn, mapSn });
	}

}