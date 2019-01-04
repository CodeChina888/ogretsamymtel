package crosssrv.groupFight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import core.CallPoint;
import core.Node;
import core.Port;
import core.RemoteNode;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Config;
import core.support.Distr;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import core.support.Utils;
import crosssrv.combatant.CombatantGlobalInfo;
import crosssrv.combatant.CombatantGlobalServiceProxy;
import crosssrv.seam.CrossPort;
import crosssrv.stage.CrossStageServiceProxy;
import crosssrv.support.Log;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.support.D;

/**
 * 多人战斗服务类
 * 
 * @author landy
 * @note 外部调用的方法需要加类似前缀GF001_
 */
@DistrClass(importClass = { List.class, HumanMirrorObject.class, Message.class, })
public class GroupFightService extends Service {
	/**
	 * 战斗超时时间
	 */
	private static long FIGHT_TIMEOUT_TIME = 240 * Time.SEC;

	/**
	 * 匹配超时时间
	 */
	private static long TIMEOUT_TIME = 10 * Time.SEC;

	/**
	 * 成员信息 <玩家Id,玩家信息>
	 */
	private Map<Long, GroupFightHuman> members = new HashMap<>();

	/**
	 * 排队队列 <type,玩家ID>
	 */
	public Map<Integer, List<Long>> queueMap = new HashMap<>();

	/**
	 * 地图信息
	 */
	public Map<Long, GroupFightStageInfo> stageInfos = new HashMap<>();

	/**
	 * 队伍信息 teamId,Team
	 */
	private Map<Long, GroupFightTeam> teams = new HashMap<>();

	/**
	 * 多类型队伍信息 type,teamId
	 */
	private Map<Integer, ArrayList<Long>> typeTeamIds = new HashMap<>();

	/**
	 * 匹配时间间隔
	 */
	private TickTimer queueTimer = new TickTimer(1 * Time.SEC);

	/**
	 * 录像表
	 */
	// private Map<Long,FightRecord> mapFightRecords = new HashMap<>();

	/**
	 * 构造函数
	 * 
	 * @param port
	 */
	public GroupFightService(CrossPort port) {
		super(port);
	}

	@Override
	public Object getId() {
		return D.CROSS_SERV_GROUP_FIGHT;
	}

	/**
	 * 获取类型对战最小人数
	 * 
	 * @param type
	 * @return
	 */
	private static int getFightMin(int type) {
		switch (type) {
		default:
			break;
		}
		return 1;
	}

	/**
	 * 获取类型对战最大人数
	 * 
	 * @param type
	 * @return
	 */
	private static int getFightMax(int type) {
		switch (type) {
		default:
			break;
		}
		return 1;
	}

	/**
	 * 子类重写的心跳
	 */
	public void pulseOverride() {
		// 当前时间
		long now = Port.getTime();

		if (queueTimer.isPeriod(now)) {
			// 匹配
			matchTeam();

			// 超时
			Iterator<Map.Entry<Long, GroupFightTeam>> ite = teams.entrySet().iterator();
			while (ite.hasNext()) {
				Map.Entry<Long, GroupFightTeam> entry = ite.next();
				GroupFightTeam team = entry.getValue();
				if (!team.isInFight() && team.startTime + TIMEOUT_TIME < now) {
					// 从成员中删除玩家
					for (long humanId : team.humanIds) {
						// 通知玩家
						GroupFightHuman human = members.get(humanId);

						CallPoint toPoint = new CallPoint(human.nodeIdWorld, Distr.getPortId(D.SERV_HUMAN_GLOBAL),
								D.SERV_HUMAN_GLOBAL);
						port.call(false, toPoint,
								HumanGlobalServiceProxy.EnumCall.HumanGlobalService_pvpMatchTimeOut_long,
								"HumanGlobalService_pvpMatchTimeOut", new Object[] { humanId });
						Log.cross.info("玩家 {} ({}) 超时离开竞技场匹配队列 ", human.getHumanName(), humanId);
						members.remove(humanId);
					}
					// 删除队伍
					typeTeamIds.get(team.type).remove(team.teamId);
					ite.remove();
				}
			}
		}

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
	public void GF001_match(HumanMirrorObject humanMirrorObj, int type, String nodeIdWorld, String portIdWorld,
			int stageSn, int mapSn) {
		long humanId = humanMirrorObj.getHumanMirror().getId();
		GroupFightHuman member = members.get(humanId);
		if (member != null) {
			Log.cross.info("玩家 {} ({})  重复进入竞技场匹配队列 type = {}", member.getHumanName(), humanId, member.type);
			// 判断是否超时
			GroupFightTeam team = teams.get(member.teamId);
			if (team != null) {
				GroupFightStageInfo stageInfo = stageInfos.get(member.stageId);
				if (stageInfo != null) {
					// TODO 目前不做断线重连
					// if (Port.getTime() - stageInfo.startTime >
					// FIGHT_TIMEOUT_TIME) {
					// 超时
					// GF003_CombatEnd(humanId,
					// CompetitionManager.RESULT_COMPET_DRAW);
					removeMember(humanId);
					GF001_match(humanMirrorObj, type, nodeIdWorld, portIdWorld, stageSn, mapSn);
					return;
					// }
					// matchResult(member);
					// return;
				}
			}
			matchResult(member);
			return;
		}

		// 成员信息
		member = new GroupFightHuman(humanMirrorObj, type, nodeIdWorld, portIdWorld, 0);
		members.put(humanId, member);
		Log.cross.info("玩家 {} ({})  进入竞技场匹配队列  type = {}", member.getHumanName(), humanId, type);
		// 这里需要做一次寻找
		GroupFightTeam team = joinTeam(member, type, stageSn, mapSn);

		if (!matchTeam(team)) {
			List<Long> queue = queueMap.get(type);
			if (queue == null) {
				queue = new LinkedList<>();
				queueMap.put(type, queue);
			}
			if (!queue.contains(humanId)) {
				// 未找到时才加入排队队列
				queue.add(humanId);
			}
		}
	}

	/**
	 * 通知匹配信息
	 * 
	 * @param human
	 */
	private void matchResult(GroupFightHuman human) {
		GroupFightTeam team = teams.get(human.teamId);
		if (team == null) {
			return;
		}
		long humanId = human.getHumanId();
		GroupFightStageInfo stageInfo = stageInfos.get(human.stageId);
		if (stageInfo == null) {
			return;
		}
		GroupFightTeam enemyTeam = teams.get(team.enemyTeamId);
		if (enemyTeam == null) {
			return;
		}

		Iterator<Long> it = enemyTeam.humanIds.iterator();
		if (it.hasNext()) {
			long enemyId = it.next();
			GroupFightHuman enemy = members.get(enemyId);

			CallPoint toPoint = new CallPoint(human.nodeIdWorld, Distr.getPortId(D.SERV_HUMAN_GLOBAL),
					D.SERV_HUMAN_GLOBAL);
			port.call(false, toPoint,
					HumanGlobalServiceProxy.EnumCall.HumanGlobalService_pvpMatchResult_long_int_String_String_int_int_HumanMirrorObject_String_long_int_int,
					"HumanGlobalService_pvpMatchResult",
					new Object[] { humanId, human.type, human.token, Config.getCrossMainNodeConnIp(stageInfo.partId),
							Config.getCrossMainNodeConnPort(stageInfo.partId), team.team.getNumber(),
							enemy.humanMirrorObj, Config.getCrossPartDefaultNodeId(), human.stageId, human.stageSn,
							human.mapSn });
			return;

		}
	}
	// /**
	// * 通知玩家战斗结束信息
	// * @param human
	// */
	// private void combatResult(PvpMatchHuman human, boolean isWin){
	//
	// }

	/**
	 * 取消排队
	 * 
	 * @param humanId
	 */
	@DistrMethod
	public void GF002_cancleMatch(long humanId) {
		Log.cross.info("humanId {} 主动请求离开竞技场匹配队列 ", humanId);
		GroupFightHuman member = members.get(humanId);
		// 玩家未参加PVP
		if (member == null) {
			port.returns("result", 0);
			return;
		}
		// 玩家正在战斗中
		if (member.state == GroupFightStatus.Fight) {
			Log.cross.info("玩家 {} ({}) 在战斗中取消竞技场匹配队列 失败 ", member.getHumanName(), humanId);
			port.returns("result", 1);
			return;
		}
		// String serverNodeId = member.serverNodeId;

		// 玩家正在排队中，移除
		removeMember(humanId);
		// //通知移除
		// CallPoint toPoint = new CallPoint(serverNodeId,
		// Distr.getPortId(D.SERV_HUMAN_GLOBAL), D.SERV_HUMAN_GLOBAL);
		// port.call(toPoint,
		// HumanGlobalServiceProxy.EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_PVPMATCHCANCELOK_LONG,
		// new Object[]{ humanId});
		port.returns("result", 0);
	}

	/**
	 * 战斗结束
	 * 
	 * @param humanId
	 * @param isWin
	 */
	@DistrMethod
	public void GF003_CombatEnd(long humanId, int isWin) {
		Log.cross.info("HumanId = {} 战斗结束 提交 ", humanId);
		GroupFightHuman human = members.get(humanId);
		if (human == null) {
			return;
		}

		GroupFightTeam team = teams.get(human.teamId);
		if (team == null) {
			return;
		}
		GroupFightTeam enemyTeam = teams.get(team.enemyTeamId);
		if (enemyTeam != null) {
			// 从成员中删除玩家
			for (long id : enemyTeam.humanIds) {
				members.remove(id);
				// //通知玩家
				// PvpMatchHuman member = members.get(id);
				// combatResult(member, (enemyTeam.camp == winCamp));
			}
			typeTeamIds.get(enemyTeam.type).remove(enemyTeam.teamId);
			teams.remove(enemyTeam.teamId);
		}
		Log.cross.info("玩家 {} ({}) 战斗结束 提交 清理对战副本 ", human.getHumanName(), humanId);
		// int winCamp = human.camp;
		long stageId = team.stageId;
		// if(isWin == CompetitionManager.RESULT_COMPET_WIN || isWin ==
		// CompetitionManager.RESULT_COMPET_PERFECT ){
		// winCamp = human.camp;
		// }else if(isWin == CompetitionManager.RESULT_COMPET_DRAW){
		// //平局 不处理
		// winCamp = 0;
		// }
		// else{
		// winCamp = enemyTeam.camp;
		// }

		// 从成员中删除玩家
		for (long id : team.humanIds) {
			members.remove(id);
			// //通知玩家
			// PvpMatchHuman member = members.get(id);
			// combatResult(member, (team.camp == winCamp));
		}
		typeTeamIds.get(team.type).remove(team.teamId);
		teams.remove(team.teamId);

		// 删除地图
		GroupFightStageInfo stageInfo = stageInfos.get(stageId);
		if (stageInfo != null) {
			CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(stageInfo.serverNodeId,
					stageInfo.serverPortId, D.CROSS_SERV_STAGE_DEFAULT);
			prx.waitDestroy(stageId);
			// prx.destroy(stageId);
			stageInfos.remove(stageId);
		}
	}

	/**
	 * 玩家主动离开战斗
	 * 
	 * @param humanId
	 */
	@DistrMethod
	public void GF004_Leave(long humanId) {
		GroupFightHuman member = members.get(humanId);
		// 玩家未参加PVP
		if (member == null) {

			port.returns("result", 0);
			return;
		}
		// 玩家正在战斗中
		if (member.state == GroupFightStatus.Fight) {
			// PvpMatchTeam team = teams.get(member.teamId);
			// if(team == null){
			// Log.cross.error("！！！PVP 玩家在战斗状态离开，却找不到队伍");
			// //从成员中删除
			// members.remove(humanId);
			// }
			// PvpMatchTeam enemyTeam = teams.get(team.enemyTeamId);
			// if(enemyTeam == null){
			// Log.cross.error("！！！玩家在战斗状态离开，却找不到对手队伍");
			// team.enemyTeamId = 0;
			// }
			// 通知房间其他玩家，有人掉线
			long stageId = member.stageId;
			GroupFightStageInfo stageInfo = stageInfos.get(stageId);
			if (stageInfo != null) {
				// CrossStageServiceProxy prx =
				// CrossStageServiceProxy.newInstance(stageInfo.serverNodeId,
				// stageInfo.serverPortId, D.CROSS_SERV_STAGE_DEFAULT);
				// prx.leaveStage(stageId, humanId);
			}
		}
		// 从队伍中移除
		removeMember(humanId);
		port.returns("result", 1);
	}

	/**
	 * 离开队伍/取消排队
	 * 
	 * @param humanId
	 */
	private void removeMember(long humanId) {
		GroupFightHuman member = members.get(humanId);
		if (member == null) {
			return;
		}
		if (member.teamId > 0) {
			GroupFightTeam team = teams.get(member.teamId);
			if (team != null) {
				// 从队伍中删除
				team.humanIds.remove(humanId);

				if (team.humanIds.size() == 0) {
					// 如果队伍没有人了，删除队伍
					typeTeamIds.get(team.type).remove(team.teamId);
					teams.remove(team.teamId);
					// 删除队伍的敌对关系
					if (team.enemyTeamId > 0) {
						GroupFightTeam e_team = teams.get(team.enemyTeamId);
						if (e_team != null) {
							e_team.enemyTeamId = 0;
						}
					}
				}
			}
		}
		// 排队中
		if (member.state == GroupFightStatus.Match) {
			List<Long> queue = queueMap.get(member.type);
			if (queue != null) {
				queue.remove(humanId);
			}
		}
		// 从成员中删除
		Log.cross.info("玩家 {} ({}) 离开竞技场匹配队列 ", member.getHumanName(), humanId);
		members.remove(humanId);

	}

	/**
	 * 查找队伍或建立一个新的队伍
	 * 
	 * @param member
	 * @param type
	 * @param stageSn
	 * @param mapSn
	 * @return
	 */
	public GroupFightTeam joinTeam(GroupFightHuman member, int type, int stageSn, int mapSn) {
		long humanId = member.getHumanId();
		// 查找指定类型
		ArrayList<Long> typeIds = typeTeamIds.get(type);
		if (typeIds == null) {
			typeIds = new ArrayList<>();
			typeTeamIds.put(type, typeIds);
		}

		// 在未开战的队伍中寻找
		for (Long teamId : typeIds) {
			GroupFightTeam team = teams.get(teamId);
			if (team == null)
				continue;
			if (team.isFull())
				continue;
			if (team.state == GroupFightStatus.Match) {
				member.teamId = team.teamId;
				member.stageId = team.stageId;
				member.online = true;
				team.humanIds.add(humanId);
				return team;
			}
		}

		// 新建一个队伍
		GroupFightTeam team = new GroupFightTeam(Port.applyId(), humanId, type, stageSn, mapSn, getFightMin(type),
				getFightMax(type));
		teams.put(team.teamId, team);
		typeIds.add(team.teamId);
		member.teamId = team.teamId;
		return team;
	}

	/**
	 * 所有的队伍做一次敌对匹配 XXX 根据需要重写
	 */
	public void matchTeam() {
		for (GroupFightTeam b_team : teams.values()) {
			matchTeam(b_team);
		}
	}

	/**
	 * 某个队伍做一次敌对匹配
	 */
	public boolean matchTeam(GroupFightTeam team) {
		if (team == null) {
			return false;
		}
		// 战斗中
		if (team.isInFight()) {
			return true;
		}
		// 未满足开始匹配条件
		if (!team.canStart()) {
			return false;
		}

		// 查找指定类型
		ArrayList<Long> typeIds = typeTeamIds.get(team.type);
		if (typeIds == null) {
			typeIds = new ArrayList<>();
			typeTeamIds.put(team.type, typeIds);
		}
		// 匹配
		for (Long teamId : typeIds) {
			GroupFightTeam e_team = teams.get(teamId);
			if (e_team.isInFight() || e_team.enemyTeamId > 0 || team.teamId == e_team.teamId || !e_team.canStart()) {
				continue;
			}
			setEnemyTeam(team, e_team);
			return true;
		}
		return false;
	}

	/**
	 * 匹配成功，设置敌对阵营
	 * 
	 * @param team1
	 * @param team2
	 */
	public void setEnemyTeam(GroupFightTeam team1, GroupFightTeam team2) {
		team1.enemyTeamId = team2.teamId;
		team2.enemyTeamId = team1.teamId;
		team1.state = GroupFightStatus.Fight;
		team2.state = GroupFightStatus.Fight;
		team1.team = ETeamType.Team1;
		team2.team = ETeamType.Team2;

		String logInfo = "pvp配对成功 type = " + team1.type + "[ ";
		int order = 0;
		for (long humanId : team1.humanIds) {
			GroupFightHuman human = members.get(humanId);
			human.team = team1.team;
			human.order = order;
			order++;
			logInfo = logInfo + human.getHumanName() + "(" + humanId + ")  ";
		}
		logInfo = logInfo + "] VS [";
		order = 0;
		for (long humanId : team2.humanIds) {
			GroupFightHuman human = members.get(humanId);
			human.team = team2.team;
			human.order = order;
			order++;
			logInfo = logInfo + human.getHumanName() + "(" + humanId + ")  ";
		}
		logInfo = logInfo + "]";
		Log.cross.info(logInfo);

		// 地图配置信息
		long stageId = Port.applyId();
		int mapSn = team1.mapSn;
		int repSn = team1.stageSn;
		// 选择分支
		int partId = selectCrossPart();
		String nodeId = Config.getCrossPartDefaultNodeId(partId);
		String portId = Config.getCrossPartDefaultPortId(partId);
		// 创建房间地图
		createStage(team1, null, nodeId, portId, repSn, mapSn, stageId, partId);
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
	private void createStage(GroupFightTeam team1, GroupFightTeam team2, String nodeId, String portId, int stageSn,
			int mapSn, long stageId, int partId) {
		switch (team1.type) {
		// case ECrossFightType.FIGHT_INSTANCE_VALUE:
		// {
		// CrossStageServiceProxy prx =
		// CrossStageServiceProxy.newInstance(nodeId, portId,
		// D.CROSS_SERV_STAGE_DEFAULT);
		// prx.createStageInstance(team1.getIds(),
		// stageId,stageSn,mapSn,team1.type);
		// prx.listenResult(this::_result_createStagePve, "team1",
		// team1.teamId,"stageSn",stageSn,
		// "mapSn",mapSn,"stageId",stageId,"nodeId",nodeId,"portId",portId,"partId",partId);
		// return;
		// }
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
	private void _result_createStagePvp(boolean timeout, Param results, Param context) {
		if (timeout) {
			// 连接不上时，重新选择分支
			long stageId = context.get("stageId");
			long teamid1 = context.get("team1");
			long teamid2 = context.get("team2");
			GroupFightTeam team1 = teams.get(teamid1);
			GroupFightTeam team2 = teams.get(teamid2);
			if (team1 == null || team2 == null) {
				return;
			}
			int mapSn = context.get("mapSn");
			int stageSn = context.get("stageSn");
			int partId = 0;
			String nodeId = Config.getCrossPartDefaultNodeId();
			String portId = Config.getCrossPartDefaultPortId();
			createStage(team1, team2, nodeId, portId, stageSn, mapSn, stageId, partId);
			return;
		}
		// 创建完毕，用户切换地图
		long stageId = context.get("stageId");
		long teamid1 = context.get("team1");
		long teamid2 = context.get("team2");
		GroupFightTeam team1 = teams.get(teamid1);
		GroupFightTeam team2 = teams.get(teamid2);
		if (team1 == null || team2 == null) {
			return;
		}
		int mapSn = context.get("mapSn");
		int stageSn = context.get("stageSn");
		// 配置信息
		String nodeId = context.get("nodeId");
		String portId = context.get("portId");
		int partId = context.get("partId");
		List<CombatantGlobalInfo> infos = new ArrayList<>();
		List<Long> humanIds = new ArrayList<>();
		List<Long> teamIds = new ArrayList<>();
		teamIds.add(team1.teamId);
		teamIds.add(team2.teamId);
		for (long humanId : team1.humanIds) {
			GroupFightHuman human = members.get(humanId);
			CombatantGlobalInfo info = new CombatantGlobalInfo(humanId, human.nodeIdWorld, human.portIdWorld,
					team1.team, human.order, human.stageSn, human.mapSn, team1.teamId, human.token,
					human.humanMirrorObj, stageId, nodeId, portId, partId);
			infos.add(info);
			humanIds.add(humanId);
		}
		for (long humanId : team2.humanIds) {
			GroupFightHuman human = members.get(humanId);
			CombatantGlobalInfo info = new CombatantGlobalInfo(humanId, human.nodeIdWorld, human.portIdWorld,
					team2.team, human.order, human.stageSn, human.mapSn, team2.teamId, human.token,
					human.humanMirrorObj, stageId, nodeId, portId, partId);
			infos.add(info);
			humanIds.add(humanId);
		}
		GroupFightStageInfo stageInfo = new GroupFightStageInfo(humanIds, teamIds, team1.type, nodeId, portId, stageId,
				stageSn, mapSn, partId, Port.getTime());
		this.stageInfos.put(stageId, stageInfo);
		// 传递玩家信息
		CombatantGlobalServiceProxy prx = CombatantGlobalServiceProxy.newInstance(nodeId, Config.getCrossPartDefaultPortId(partId),
				D.SERV_COMBATANT_GLOBAL);
		prx.register(infos);
		prx.listenResult(this::_result_transmitDataPvp, "team1", team1.teamId, "team2", team2.teamId, "stageSn",
				stageSn, "mapSn", mapSn, "stageId", stageId);
	}

	/**
	 * 传递玩家信息完毕回调
	 * 
	 * @param results
	 * @param context
	 */
	public void _result_transmitDataPvp(Param results, Param context) {
		// 创建完毕，用户切换地图
		long stageId = context.get("stageId");
		long teamid1 = context.get("team1");
		long teamid2 = context.get("team2");
		GroupFightTeam team1 = teams.get(teamid1);
		GroupFightTeam team2 = teams.get(teamid2);
		if (team1 == null || team2 == null) {
			return;
		}
		int mapSn = context.get("mapSn");
		int stageSn = context.get("stageSn");

		team1.stageId = stageId;
		team2.stageId = stageId;

		// 通知进入战场
		int index = 0;
		for (Long humanId : team1.humanIds) {
			GroupFightHuman member = members.get(humanId);
			member.stageId = stageId;
			member.stageSn = stageSn;
			member.mapSn = mapSn;
			member.order = index;
			member.team = ETeamType.Team1;
			matchResult(member);
			// 排队中移除
			if (member.state == GroupFightStatus.Match) {
				List<Long> queue = queueMap.get(member.type);
				if (queue != null) {
					queue.remove(humanId);
				}
			}
			member.state = GroupFightStatus.Fight;
			index++;
		}

		// 通知进入战场
		index = 0;
		for (Long humanId : team2.humanIds) {
			GroupFightHuman member = members.get(humanId);
			member.stageId = stageId;
			member.stageSn = stageSn;
			member.mapSn = mapSn;
			member.order = index;
			member.team = ETeamType.Team2;
			matchResult(member);
			// 排队中移除
			if (member.state == GroupFightStatus.Match) {
				List<Long> queue = queueMap.get(member.type);
				if (queue != null) {
					queue.remove(humanId);
				}
			}
			member.state = GroupFightStatus.Fight;
			index++;
		}
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
	public void GF005_StageFightFinish(long stageId, List<Long> combatants, Message msg, int winTeam,
			boolean isAlwaysWin) {
		for (long humanId : combatants) {
			GroupFightHuman member = members.get(humanId);
			if (member.stageId != stageId) {
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
	public void GF006_StageLeaveFinish(long stageId, List<Long> combatants, long combatantId) {
		for (long humanId : combatants) {
			GroupFightHuman member = members.get(humanId);
			if (member.stageId != stageId) {
				continue;
			}

			CallPoint toPoint = new CallPoint(member.nodeIdWorld, Distr.getPortId(D.SERV_HUMAN_GLOBAL),
					D.SERV_HUMAN_GLOBAL);
			port.call(false, toPoint, HumanGlobalServiceProxy.EnumCall.HumanGlobalService_pvpLeaveFight_long_int,
					"HumanGlobalService_pvpLeaveFight", new Object[] { humanId, member.type });
			return;
		}
	}

	/**
	 * 每个service预留空方法
	 * 
	 * @param objs
	 */
	@DistrMethod
	public void GF007_update(Object... objs) {

	}
}