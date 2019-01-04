package game.worldsrv.team;

import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.team.instance.TeamInstanceManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.scheduler.ScheduleTask;
import core.support.Param;
import core.support.SysException;
import game.msg.Define.EInformType;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.entity.Human;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.inform.InformManager;
import game.worldsrv.team.TeamData;
import game.worldsrv.team.TeamMember;

/**
 * 队伍全局服务
 */
@DistrClass(servId = D.SERV_TEAM, importClass = {Human.class, TeamData.class})
public class TeamService extends GameServiceBase {
	// 可分配的队伍ID[10000,99999]，初始分配5位数的队伍ID，如不够用了则再分配6位数的队伍ID
	private final int minTeamId5 = 10000;// 初始5位数的队伍ID
	private final int maxTeamId5 = 99999;// 初始5位数的队伍ID
	private final int minTeamId6 = 100000;// 初始6位数的队伍ID
	private final int maxTeamId6 = 999999;// 初始6位数的队伍ID
	private int maxTeamId = 0;// 记录当前分配的最大队伍ID
	private LinkedList<Integer> listTeamId = new LinkedList<>();
	// <副本SN, <队伍ID, 队伍信息>>
	private Map<Integer, HashMap<Integer, TeamData>> mapTeamInfo = new HashMap<Integer, HashMap<Integer, TeamData>>();

	private final int actInstSn1101 = 1101;// 1v1竞技的活动副本sn
	private final int actInstSn1103 = 1103;// 3v3竞技的活动副本sn
	private final int actInstSn1105 = 1105;// 5v5竞技的活动副本sn
	private final int actInstSn1205 = 1205;// 5v5团战的活动副本sn
	private final int actInstSn1303 = 1303;// 3v3Moba的活动副本sn

	public TeamService(GamePort port) {
		super(port);

		addTeamId(minTeamId5, maxTeamId5);// 初始分配5位数的队伍ID
	}
	
	@Override
	protected void init() {
		checkTime();
	}
	
	/**
	 * 检查活动的开启/关闭时间
	 */
	private void checkTime() {
		for(ConfInstActConfig config : ConfInstActConfig.findAll()){
			if (null == config.openWeekDay || null == config.openHour || null == config.openMinute 
					|| null == config.totalMinute) {
				continue;
			}
			if (noTimeLimit(config)) {
				continue;
			}
			// 活动开始的schedule
			for (int i = 0; i < config.openHour.length; i++) {
				int minute = 0;
				if (i < config.openMinute.length) {
					minute = config.openMinute[i];
				}
				String startCron = Utils.getQuartzCron(Utils.getQuartzDayOffWeek(config.openWeekDay), 
						config.openHour[i], minute, 1);
				checkSchedule(startCron, config);
			}
		}
		
	}
	/**
	 * 是否有时间限制
	 * @param config
	 * @return
	 */
	private boolean noTimeLimit(ConfInstActConfig config) {
		return (config.openWeekDay.length == 1 && config.openWeekDay[0] == 0) && 
				(config.openHour.length == 1 && config.openHour[0] == 0) && 
				(config.openMinute.length == 1 && config.openMinute[0] == 0) && 
				(config.totalMinute.length == 1 && config.totalMinute[0] == 24 * 60);
	}
	/**
	 * 检查活动执行时间
	 * @param cron
	 */
	private void checkSchedule(String cron, ConfInstActConfig conf) {
		scheduleCron(new ScheduleTask() {
			@Override
			public void execute() {
				try {
					// 走马灯，连续提示3次
					scheduleRepeatForTotalCount(new ScheduleTask() {
						@Override
						public void execute() {
							//999活动副本 {} 开启了！
							//String info = InformManager.inst().getSysMsg(999, conf.name);
							// 通知
							//InformManager.inst().sendNotify(EInformType.SystemInform, info, 3);
						}
					}, 0, 1000, 3);
						
				} catch (Exception e) {
					throw new SysException(e, "service初始化schedule错误，servicId:{}", getParam().get("servId"));
				}
			}
		}, cron);
	}
	
//	/**
//	 * 定时计数 队伍连续30秒都是满员的队伍状态且 队伍状态不是开战状态 （即不是RUN而是DEFAULT状态）踢出队长，并顺位擢升新队长
//	 */
//	@ScheduleMethod("0/1 * * * * ?")
//	public void sheduleMethod() {
//		
//	}
	
	/**
	 * 增加队伍ID
	 * @param start
	 * @param end
	 */
	private void addTeamId(int start, int end) {
		for (int i = start; i <= end; i++) {
			listTeamId.add(i);
		}
		maxTeamId = end;// 记录当前分配的最大队伍ID
	}

	/**
	 * 申请队伍ID
	 */
	private int popTeamId() {
		int ret = 0;
		if (listTeamId.isEmpty()) {
			if (maxTeamId == maxTeamId5) {// 初始分配5位数的队伍ID，如不够用了则再分配6位数的队伍ID
				Log.human.info("===5位数的队伍ID不够用了，已有9万个队伍？");
				addTeamId(minTeamId6, maxTeamId6);// 增加队伍ID[100000,999999]
			} else if (maxTeamId == maxTeamId6) {
				Log.human.error("===6位数的队伍ID不够用了，已有90万个队伍？不可能吧。。。");
			} else {
				Log.human.error("===队伍ID不够用了，逻辑有问题，是否忘记分配队伍ID？");
			}
		}
		if (!listTeamId.isEmpty()) {
			ret = listTeamId.poll();
		}
		return ret;
	}

	/**
	 * 回收队伍ID
	 */
	private void pushTeamId(int id) {
		listTeamId.add(id);// 插到后面去，如要插前面用push
	}
	
	/**
	 * 每隔5秒执行一次
	 */
	@ScheduleMethod(Utils.cron_Second_Five)
	public void _cron_Second_Five() {
		// 匹配1v1竞技
		timeToMatch1101();
	}

	/**
	 * 每隔7秒执行一次
	 */
	@ScheduleMethod(Utils.cron_Second_Seven)
	public void _cron_Second_Seven() {
		// 匹配3v3竞技，3v3Moba
		timeToMatch1103();
		timeToMatch1303();
	}

	/**
	 * 每隔9秒执行一次
	 */
	@ScheduleMethod(Utils.cron_Second_Nine)
	public void _cron_Second_Nine() {
		// 匹配5v5竞技，5v5团战
		timeToMatch1105();
		timeToMatch1205();
	}

	/**
	 * 每隔5秒执行一次匹配1v1竞技
	 */
	private void timeToMatch1101() {
		List<TeamData> listTeamMatch = null;
		// 1v1竞技
		if (mapTeamInfo.containsKey(actInstSn1101)) {
			listTeamMatch = sortTeamCombat(mapTeamInfo.get(actInstSn1101));
		}
		timeToMatch(listTeamMatch);
	}

	/**
	 * 每隔7秒执行一次匹配3v3竞技
	 */
	private void timeToMatch1103() {
		List<TeamData> listTeamMatch = null;
		// 3v3竞技
		if (mapTeamInfo.containsKey(actInstSn1103)) {
			listTeamMatch = sortTeamCombat(mapTeamInfo.get(actInstSn1103));
		}
		timeToMatch(listTeamMatch);
	}

	/**
	 * 每隔9秒执行一次匹配5v5竞技
	 */
	private void timeToMatch1105() {
		List<TeamData> listTeamMatch = null;
		// 5v5竞技
		if (mapTeamInfo.containsKey(actInstSn1105)) {
			listTeamMatch = sortTeamCombat(mapTeamInfo.get(actInstSn1105));
		}
		timeToMatch(listTeamMatch);
	}

	/**
	 * 每隔9秒执行一次匹配5v5团战
	 */
	private void timeToMatch1205() {
		List<TeamData> listTeamMatch = null;
		// 5v5团战
		if (mapTeamInfo.containsKey(actInstSn1205)) {
			listTeamMatch = sortTeamCombat(mapTeamInfo.get(actInstSn1205));
		}
		timeToMatch(listTeamMatch);
	}

	/**
	 * 每隔7秒执行一次匹配3v3Moba
	 */
	private void timeToMatch1303() {
		List<TeamData> listTeamMatch = null;
		// 3v3Moba
		if (mapTeamInfo.containsKey(actInstSn1303)) {
			listTeamMatch = sortTeamCombat(mapTeamInfo.get(actInstSn1303));
		}
		timeToMatch(listTeamMatch);
	}

	/**
	 * 时间到了，执行匹配，进入PVP副本
	 * @param listTeamMatch
	 */
	private void timeToMatch(List<TeamData> listTeamMatch) {
		if (listTeamMatch == null)
			return;

		Iterator<TeamData> iter = listTeamMatch.iterator();
		while (iter.hasNext()) {
			TeamData team1 = iter.next();
			TeamData team2 = null;
			if (iter.hasNext()) {
				iter.remove();
				team2 = iter.next();
				iter.remove();
			}
			if (team2 != null) {// 有匹配到对手
				team1.matchClose();// 结束匹配
				team2.matchClose();// 结束匹配
				team1.startOpen();// 开始副本
				team2.startOpen();// 开始副本
				team1.setGroupId(1);// 设为小组1
				team2.setGroupId(2);// 设为小组2
				TeamInstanceManager.inst().enterPVP(team1, team2);// 进入PVP副本
			}
		}
	}

	/**
	 * 升序排序：队伍总战力
	 */
	private List<TeamData> sortTeamCombat(Map<Integer, TeamData> mapTeam) {
		List<TeamData> listTeamMatch = new LinkedList<TeamData>();// 满员的队伍列表
		List<TeamData> listTeamMatchMerge = new LinkedList<TeamData>();// 未满员的队伍列表

		for (TeamData team : mapTeam.values()) {
			if (team.isStart())
				continue;// 已开始副本的队伍就略过
			if (!team.isMatch())
				continue;// 未开始匹配的队伍就略过

			if (team.isFull()) {// 人满的队伍
				listTeamMatch.add(team);
			} else {// 未满的队伍尝试合并队伍
				boolean ret = false;
				Iterator<TeamData> it = listTeamMatchMerge.iterator();
				while (it.hasNext()) {
					TeamData tt = it.next();
					if (tt.isFull()) {// sjhtest
						Log.human.error("===mergeOtherTeam处理中发现listTeamMatchMerge中的队伍满员了？没删除掉？");
						continue;
					}

					if (tt.mergeOtherTeam(team)) {// 并入成功了，删除队伍并且回收队伍ID
						ret = true;
						if (tt.isFull()) {// 并入后满员了，则加入到满员队列
							listTeamMatch.add(tt);
							it.remove();
						}
						// 查找队员：通知队员信息变化
						List<Long> humanIdList = tt.getAllId();
						List<Long> humanIdOtherList = team.getAllId();
						HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
						prx.getInfoList(humanIdList);
						prx.listenResult(this::_result_teamMerge, "result", 0, "humanIdOtherList", humanIdOtherList,
								"team", tt);

						removeTeam(team);// 删除队伍并且回收队伍ID
						break;
					}
				}
				if (!ret) {// 未找到合适的队伍并入
					listTeamMatchMerge.add(team);
				}
			}
		}

		Collections.sort(listTeamMatch, new Comparator<TeamData>() {
			@Override
			public int compare(TeamData t1, TeamData t2) {
				int ret = 0;
				if (t1 != null && t2 != null) {
					if (t1.getTeamCombat() < t2.getTeamCombat())
						ret = -1;
					else if (t1.getTeamCombat() > t2.getTeamCombat())
						ret = 1;
				}
				return ret;
			}
		});
		return listTeamMatch;
	}

	private void _result_teamMerge(Param results, Param context) {
		int result = Utils.getParamValue(context, "result", 0);
		List<Long> humanIdOtherList = Utils.getParamValue(context, "humanIdOtherList", null);
		if (humanIdOtherList == null) {
			Log.game.error("===_result_teamMerge humanIdOtherList=null");
			return;
		}
		TeamData team = Utils.getParamValue(context, "team", null);
		if (team == null) {
			Log.game.error("===_result_teamMerge team=null");
			return;
		}
		List<HumanGlobalInfo> hgList = results.get();
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				if (humanIdOtherList.contains(hg.id)) {
					// 加入的人通知队伍信息，并且通知加入返回结果消息
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,
							hg.id);
					prxHumanObj.teamApplyJoin(result, team);
				} else {
					// 其他队员，通知队员信息改变
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,
							hg.id);
					prxHumanObj.teamMemberInfoUpdate(team);
				}
			}
		}
	}

	/**
	 * 创建指定副本的队伍
	 */
	@DistrMethod
	public void teamCreate(Human human, int actInstSn) {
		teamCreateFrom(human, actInstSn);
	}

	private void teamCreateFrom(Human human, int actInstSn) {
		TeamData team = null;
		ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);
		if (conf != null) {
			HashMap<Integer, TeamData> mapTeam = new HashMap<Integer, TeamData>();
			if (mapTeamInfo.containsKey(actInstSn)) {// 存在，则加入该活动副本组队信息
				mapTeam = mapTeamInfo.get(actInstSn);
			} else {// 不存在，则新建该活动副本组队信息
				mapTeamInfo.put(actInstSn, mapTeam);
			}

			int teamId = human.getTeamId();
			if (teamId > 0) {// 判断之前记录的队伍是否还存在
				team = mapTeam.get(teamId);
				if (team != null) {// 之前记录的队伍还存在，则退出重新创建
					teamLeaveRecord(human, team);// 退出之前记录的队伍
					team = null;
				}
			}
			if (team == null) {// 创建新的队伍
				teamId = popTeamId();
				team = new TeamData(teamId, human, conf);
				mapTeam.put(teamId, team);
			}
		}
		// 监听返回
		port.returns("team", team);
	}

	/**
	 * 快速加入指定副本的队伍，如无队伍存在则自己创建一个队伍
	 */
	@DistrMethod
	public void teamJoin(Human human, int actInstSn) {
		TeamData team = null;
		if (human.getTeamId() > 0) {// 判断之前记录的队伍是否还存在
			teamLeaveRecord(human, null);// 退出之前记录的队伍
		}

		boolean isJoin = false;
		if (mapTeamInfo.containsKey(actInstSn)) {// 存在，则查找是否有空缺的队伍可加入
			Map<Integer, TeamData> mapTeam = mapTeamInfo.get(actInstSn);
			for (TeamData teamFind : mapTeam.values()) {
				if (!teamFind.isFull() && !teamFind.isStart()) {// 队伍未满员且还没开启活动副本
					if (teamFind.add(new TeamMember(human))) {// 加入成功
						sendTeamJoinSucc(human, teamFind);// 通知成功加入队伍
						isJoin = true;
						team = teamFind;
						break;
					}
				}
			}
		}
		if (!isJoin) {// 没找到空缺的队伍，则创建一个新的队伍
			teamCreateFrom(human, actInstSn);
		} else {
			// 监听返回
			port.returns("team", team);
		}
	}

	/**
	 * 申请入队
	 */
	@DistrMethod
	public void teamApplyJoin(Human human, int teamId, int actInstSn) {
		int result = 0;// 0成功（成功才返回队伍信息）；>0失败（参见SysMsg.xlsx：521401已有队伍，521402队伍不存在，521403已开始副本，521404人数已满）
		TeamData team = findTeam(teamId, actInstSn);
		if (team != null) {// 队伍存在
			if (team.isStart()) {
				result = 521403;// 3已开始副本
			} else if (team.isFull()) {
				result = 521404;// 4人数已满
			} else {
				result = 0;// 0成功
				if (team.add(new TeamMember(human))) {// 加入成功
					sendTeamJoinSucc(human, team);// 通知成功加入队伍
				}
			}
		} else {
			result = 521402;// 2队伍不存在
		}
		// 监听返回
		port.returns("result", result);
	}
	
	/**
	 * 搜索指定队伍（知道队伍Id）
	 * @param teamId 队伍Id
	 */
	private TeamData findTeam(int teamId) {
		TeamData team = null;
		if (teamId > 0) {
			for (Map<Integer, TeamData> mapTeam : mapTeamInfo.values()) {
				if (mapTeam != null && mapTeam.containsKey(teamId)) {
					team = mapTeam.get(teamId);
					break;
				}
			}
		}
		return team;
	}

	/**
	 * 搜索指定队伍（知道队伍Id及活动副本Sn）
	 * @param teamId 队伍Id
	 * @param actInstSn 活动副本Sn
	 */
	private TeamData findTeam(int teamId, int actInstSn) {
		TeamData team = null;
		if (teamId > 0 && actInstSn > 0) {
			if (mapTeamInfo.containsKey(actInstSn)) {
				Map<Integer, TeamData> mapTeam = mapTeamInfo.get(actInstSn);
				if (mapTeam != null && mapTeam.containsKey(teamId)) {
					team = mapTeam.get(teamId);
				}
			}
		}
		return team;
	}

	/**
	 * 通知成功加入队伍
	 */
	private void sendTeamJoinSucc(Human human, TeamData team) {
		int result = 0;// 0成功
		// human.setTeamId(team.teamId);
		List<Long> humanIdList = team.getAllId();
		// 查找队员：通知队员信息变化
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfoList(humanIdList);
		prx.listenResult(this::_result_teamJoinSucc, "result", result, "humanIdApplyJoin", human.getId(), "team", team);
	}

	private void _result_teamJoinSucc(Param results, Param context) {
		int result = Utils.getParamValue(context, "result", 0);// 0成功
		long humanIdApplyJoin = Utils.getParamValue(context, "humanIdApplyJoin", -1L);
		TeamData team = Utils.getParamValue(context, "team", null);
		List<HumanGlobalInfo> hgList = results.get();
		if (humanIdApplyJoin < 0 || team == null || hgList == null) {
			Log.game.error("===_result_teamApplyJoin humanIdApplyJoin={}, team={}, hgList={}", humanIdApplyJoin, team,
					hgList);
			return;
		}
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				if (hg.id == humanIdApplyJoin) {
					// 加入的人通知队伍信息，并且通知加入返回结果消息
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,
							hg.id);
					prxHumanObj.teamApplyJoin(result, team);
				} else {
					// 其他队员，通知队员信息改变
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,
							hg.id);
					prxHumanObj.teamMemberInfoUpdate(team);
				}
			}
		}
	}

	/**
	 * 加入之前记录的队伍
	 */
	@DistrMethod
	public void teamJoinRecord(Human human) {
		int teamId = human.getTeamId();
		TeamData team = findTeam(teamId);
		if (team != null) {// 之前记录的队伍还存在，则加入该队伍
			if (team.isStart()) {// 已开始副本，通知玩家是否传送到之前记录的队伍的副本
				//
			} else {// 未开始副本，则判断队伍是否已满，如有空位就加入
				if (!team.isFull()) {
					if (team.add(new TeamMember(human))) {// 加入成功
						sendTeamJoinSucc(human, team);// 通知成功加入队伍
					}
				}
			}
		} else {// 之前记录的队伍不存在，则清除
			human.setTeamId(0);
		}
	}

	/**
	 * 退出之前记录的队伍
	 */
	private void teamLeaveRecord(Human human, TeamData team) {
		int teamId = human.getTeamId();
		if (teamId > 0) {// 判断之前记录的队伍是否还存在
			if (team == null) {
				team = findTeam(teamId);// 外面没找team，那就这里找吧
			}
			if (team != null) {// 之前记录的队伍还存在，则退出
				List<Long> humanIdList = team.getAllId();
				long humanIdLeave = human.getId();
				if (team.delete(humanIdLeave)) {
					sendTeamLeaveSucc(humanIdLeave, team, humanIdList);// 通知成功退出队伍
				}
			}
			human.setTeamId(0);
		}
	}

	/**
	 * 通知成功退出队伍
	 */
	private void sendTeamLeaveSucc(long humanIdLeave, TeamData team, List<Long> humanIdList) {
		if (team == null || humanIdList == null)
			return;

		boolean isChangeLeader = false;
		if (team.isEmpty()) {// 队伍无人了，删除队伍并且回收队伍ID
			removeTeam(team);// 删除队伍并且回收队伍ID
		} else {// 队伍还有人，且队长自己离队了则转移队长
			if (team.leaderId == humanIdLeave) {
				team.leaderId = team.getFirstMemberId();
				isChangeLeader = true;
			}
		}
		// 查找队员：通知队员信息变化
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfoList(humanIdList);
		prx.listenResult(this::_result_teamLeave, "result", true, "humanIdLeave", humanIdLeave, "team", team,
				"isChangeLeader", isChangeLeader);
	}

	private void _result_teamLeave(Param results, Param context) {
		boolean result = Utils.getParamValue(context, "result", true);
		long humanIdLeave = Utils.getParamValue(context, "humanIdLeave", -1L);
		TeamData team = Utils.getParamValue(context, "team", null);
		boolean isChangeLeader = Utils.getParamValue(context, "isChangeLeader", false);
		List<HumanGlobalInfo> hgList = results.get();
		if (humanIdLeave < 0 || team == null || hgList == null) {
			Log.game.error("===_result_teamLeave  humanIdLeave={}, team={}, hgList={}", humanIdLeave, team, hgList);
			return;
		}
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				if (hg.id == humanIdLeave) {
					// 离队的人清除队伍信息，并且通知离队消息
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,
							hg.id);
					prxHumanObj.teamLeave(result);
				} else {
					// 其他队员，通知队员信息改变
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,
							hg.id);
					if (isChangeLeader)
						prxHumanObj.teamInfoUpdate(team);
					else
						prxHumanObj.teamMemberInfoUpdate(team);
				}
			}
		}
	}

	/**
	 * 主动离队
	 */
	@DistrMethod
	public void teamLeave(long humanIdLeave, int teamId, int actInstSn) {
		TeamData team = null;
		if (actInstSn > 0) {
			team = findTeam(teamId, actInstSn);
		} else {// 不知道是哪个活动副本，只知道队伍ID
			team = findTeam(teamId);
		}

		if (team != null) {// 找到队伍了
			List<Long> humanIdList = team.getAllId();
			if (team.delete(humanIdLeave)) {
				sendTeamLeaveSucc(humanIdLeave, team, humanIdList);// 通知成功退出队伍
			}
		}
	}

	/**
	 * 队长踢人（队长功能）
	 */
	@DistrMethod
	public void teamKickOut(long humanIdKickout, int teamId, int actInstSn) {
		boolean result = false;
		TeamData team = findTeam(teamId, actInstSn);
		if (team != null) {
			List<Long> humanIdList = team.getAllId();
			if (team.delete(humanIdKickout)) {
				result = true;
				// 查找队员：通知队员信息变化
				HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
				prx.getInfoList(humanIdList);
				prx.listenResult(this::_result_teamKickOut, "result", true, "humanIdKickout", humanIdKickout, "team",
						team);
			}
		}
		// 监听返回
		port.returns("result", result);
	}

	private void _result_teamKickOut(Param results, Param context) {
		boolean result = Utils.getParamValue(context, "result", false);
		long humanIdKickout = Utils.getParamValue(context, "humanIdKickout", -1L);
		TeamData team = Utils.getParamValue(context, "team", null);
		List<HumanGlobalInfo> hgList = results.get();
		if (humanIdKickout < 0 || team == null || hgList == null) {
			Log.game.error("===_result_teamKickOut humanIdKickout={}, team={}, hgList={}", humanIdKickout, team, hgList);
			return;
		}
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				if (hg.id == humanIdKickout) {
					// 被踢的人清除队伍信息，并且通知被踢消息
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,
							hg.id);
					prxHumanObj.teamKickOut(result);
				} else {
					// 其他队员，通知队员信息改变
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,
							hg.id);
					prxHumanObj.teamMemberInfoUpdate(team);
				}
			}
		}
	}

	/**
	 * 进入副本
	 */
	@DistrMethod
	public void teamEnterRep(int teamId, int actInstSn) {
		int result = 0;// 0成功
		TeamData team = findTeam(teamId, actInstSn);
		if (team != null) {// 找到队伍
			if (team.isClose()) {
				result = 521602;// 521602活动副本已关闭
			} else if (team.isStart()) {
				result = 521603;// 521603活动副本已开启
			} else {
				team.startOpen();// 开始副本
				ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);
				if (conf != null) {
					// 进入PVE副本
					TeamInstanceManager.inst().enterPVE(team, conf.instSn);
				}
			}
		} else {
			result = -1;// 查无队伍
		}
		// 监听返回
		port.returns("result", result);
	}

	/**
	 * 开始匹配
	 */
	@DistrMethod
	public void teamMatch(int teamId, int actInstSn) {
		int result = 0;// 0成功
		TeamData team = findTeam(teamId, actInstSn);
		if (team != null) {// 找到队伍
			if (team.isClose()) {
				result = 525302;// 525302活动副本已关闭
			} else if (team.isStart()) {
				result = 525303;// 525303活动副本已开启
			} else {
				team.matchOpen();
				// 通知队员队伍正在匹配中...
				List<Long> humanIdList = team.getAllId();
				// 查找队员：通知队员信息变化
				HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
				prx.getInfoList(humanIdList);
				prx.listenResult(this::_result_teamMatch, "result", 0);
			}
		} else {
			result = -1;// 查无队伍
		}
		// 监听返回
		port.returns("result", result);
	}

	/**
	 * 通知队员队伍正在匹配中...
	 * @param results
	 * @param context
	 */
	private void _result_teamMatch(Param results, Param context) {
		int result = Utils.getParamValue(context, "result", 0);
		List<HumanGlobalInfo> hgList = results.get();
		if (hgList == null) {
			Log.game.error("===_result_teamMatch hgList=null");
			return;
		}
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId, hg.id);
				prxHumanObj.teamMatch(result);
			}
		}
	}

	/**
	 * 删除队伍并且回收队伍ID
	 * @return
	 */
	private void removeTeam(TeamData team) {
		if (team != null) {
			int teamId = team.teamId;
			Map<Integer, TeamData> mapTeam = mapTeamInfo.get(team.actInstSn);
			if (mapTeam != null && mapTeam.containsKey(teamId)) {
				team = null;
				mapTeam.remove(teamId);
				pushTeamId(teamId);// 回收队伍ID
			}
		} else {
			Log.human.error("===删除队伍并且回收队伍ID出错了：team is null");
		}
	}

	/**
	 * 取消匹配
	 */
	@DistrMethod
	public void teamMatchCancel(int teamId, int actInstSn) {
		int result = 0;// 0成功
		TeamData team = findTeam(teamId, actInstSn);
		if (team == null) {
			result = 525802;// 525802队伍不存在
		} else {// 找到队伍
			if (team.isStart()) {
				result = 525803;// 525803活动副本已开启
			} else {
				team.matchClose();
				// 通知队员队伍取消匹配
				List<Long> humanIdList = team.getAllId();
				// 查找队员：通知队员信息变化
				HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
				prx.getInfoList(humanIdList);
				prx.listenResult(this::_result_teamMatchCancel, "result", 0);
			}
		}
		// 监听返回
		port.returns("result", result);
	}

	/**
	 * 通知队员队伍取消匹配
	 * @param results
	 * @param context
	 */
	private void _result_teamMatchCancel(Param results, Param context) {
		int result = Utils.getParamValue(context, "result", 0);
		List<HumanGlobalInfo> hgList = results.get();
		if (hgList == null) {
			Log.game.error("===_result_teamMatchCancel hgList=null");
			return;
		}
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId, hg.id);
				prxHumanObj.teamMatchCancel(result);
			}
		}
	}

	/**
	 * 搜索队伍
	 */
	@DistrMethod
	public void teamFind(Human human, int teamId) {
		int result = 0;// 0成功（成功即加入队伍）；>0失败（参见SysMsg.xlsx：525601你已有队伍，525602队伍不存在，525603队伍正在战斗中，525604队伍人数已满）
		TeamData team = findTeam(teamId);
		if (team == null) {
			result = 525602;// 队伍不存在
		} else {
			if (team.isStart() || team.isMatch()) {
				result = 525603;// 队伍正在战斗中
			} else if (team.isFull()) {
				result = 525604;// 队伍人数已满
			} else {// 加入该队伍
				result = 0;// 0成功
				if (team.add(new TeamMember(human))) {// 加入成功
					sendTeamJoinSucc(human, team);// 通知成功加入队伍
				}
			}
		}
		// 监听返回
		port.returns("result", result);
	}

	/**
	 * 队伍结束副本
	 */
	@DistrMethod
	public void teamEndRep(int teamId) {
		TeamData team = findTeam(teamId);
		if (team != null) {
			team.startClose();
		}
	}

}
