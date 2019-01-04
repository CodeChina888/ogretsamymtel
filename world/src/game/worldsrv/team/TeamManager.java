package game.worldsrv.team;

import game.msg.Define.DFinishNum;
import game.msg.Define.EActInstType;
import game.msg.MsgTeam.SCFindTeamResult;
import game.msg.MsgTeam.SCTeamApplyJoin;
import game.msg.MsgTeam.SCTeamEnterRep;
import game.msg.MsgTeam.SCTeamInviteOne;
import game.msg.MsgTeam.SCTeamKickOut;
import game.msg.MsgTeam.SCTeamMatch;
import game.msg.MsgTeam.SCTeamMatchCancel;
import game.msg.MsgTeam.SCTeamRepInfo;
import game.msg.MsgIds;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.inform.InformManager;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Port;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;

/**
 * 组队
 */
public class TeamManager extends ManagerBase {
	
	/**
	 * 获取实例
	 * @return
	 */
	public static TeamManager inst() {
		return inst(TeamManager.class);
	}
	
	/**
	 * 玩家登录时判断是否之前的队伍还存在
	 * @param param
	 */
	@Listener(EventKey.HumanLogin)
	public void _listener_HumanLogin(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("_listener_HumanLogin humanObj is null");
			return;
		}

		int teamIdRecord = humanObj.getTeamIdRecord();
		if (teamIdRecord > 0) {
			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamJoinRecord(humanObj.getHuman());
		}
	}
	
	/**
	 * 掉线离队
	 */
	@Listener(EventKey.HumanLogout)
	public void _listener_HumanLogout(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("_listener_HumanLogout humanObj is null");
			return;
		}
		// humanObj.setTeamIdRecord(0);// 清除记录的队伍Id
		_msg_CSTeamLeave(humanObj);
	}
	
	/**
	 * 是否可以加入活动副本
	 */
	public boolean canJoinActInst(HumanObject humanObj, int actInstSn) {
		return canJoinActInst(humanObj, actInstSn, 1, false,false);
	}
	
	/**
	 * 是否可以加入活动副本
	 */
	public boolean canJoinActInst(HumanObject humanObj, int actInstSn, int numJoin, boolean isAuto ,boolean isConsumeCostItem) {
		Human human = humanObj.getHuman();
		ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);
		if (conf == null) {
			humanObj.sendSysMsg(520401);// 活动副本配置找不到
			Log.table.error("ConfInstActConfig配表错误，no find sn={}", actInstSn);
			return false;
		}
		if (conf.openHour == null || conf.openMinute == null || conf.totalMinute == null) {
			humanObj.sendSysMsg(520402);// 活动副本配置的开启时间错误！
			return false;
		}
		if (conf.openWeekDay == null || conf.openWeekDay.length <= 0 || conf.openWeekDay.length > 7) {
			humanObj.sendSysMsg(520402);// 活动副本配置的开启时间和关闭时间不匹配
			return false;
		}

		// **判断开启时间
		long tmNow = Port.getTime();// 当前时间
		int dayOfWeek = Utils.getDayOfWeek(tmNow);// 星期几
		boolean isOpen = false;// 是否开启
		int[] openWeekDay = conf.openWeekDay;
		if (0 == openWeekDay[0]) {// 判断星期几：0即每天，1-7即星期一到星期日
			isOpen = true;
		} else {
			for (int i = 0; i < openWeekDay.length; i++) {
				if (openWeekDay[i] == dayOfWeek) {
					isOpen = true;
					break;
				}
			}
		}
		if (isOpen) {// 判断小时数
			isOpen = false;
			long tmZero = Utils.getTimeBeginOfToday(tmNow);// 当天0点时间
			for (int i = 0; i < conf.openHour.length; i++) {
				long tmOpen = tmZero + conf.openHour[i] * Time.HOUR + conf.openMinute[i] * Time.MIN;
				long tmClose = tmOpen + conf.totalMinute[i] * Time.MIN;
				if (tmNow >= tmOpen && tmNow < tmClose) {
					isOpen = true;
					break;
				}
			}
		}
		if (!isOpen) {
			humanObj.sendSysMsg(520403);// 活动副本不在开启时间内
			return false;
		}

		// **判断是否低于进入等级
		if (isUnderLvEnter(human.getLevel(), conf)) {
			humanObj.sendSysMsg(520404);// 活动副本的进入等级不足
			return false;
		}
		
		// **判断是否解锁了关卡
		if (!isUnlockActInst(human.getInstActStoryLastPass(), conf)) {
			humanObj.sendSysMsg(520405);// 活动副本未解锁，请先通关前面的关卡！
			return false;
		}
		
		// **判断是否解锁了关卡
		if (!isUnlockActInst(human.getInstActMHXKWarLastPass(), conf, isAuto)) {
			humanObj.sendSysMsg(520407);// 活动副本未解锁，请先通关前面的关卡！
			return false;
		}

		// **判断完成次数
		if (getRestDayJoinNum(human.getDailyInstFinishNum(), conf) < numJoin) {
			humanObj.sendSysMsg(520406);// 活动副本的剩余次数不足
			return false;
		}
		
		ConfInstStage confInstStage = ConfInstStage.get(conf.instSn);
		if(null != confInstStage) {
			// 判断进入花费道具
			if (confInstStage.costItemSN > 0) {
				int costItemNumber = confInstStage.costItemNumber;
				if(isConsumeCostItem){
					boolean ret = RewardHelper.checkAndConsume(humanObj, confInstStage.costItemSN,costItemNumber, LogSysModType.Inst);
					if (!ret)
						return false;
				}else{
					boolean ret = RewardHelper.canConsume(humanObj, confInstStage.costItemSN,costItemNumber);
					if (!ret)
						return false;
				}
				
			}
			// 判断体力限制
			if (confInstStage.needManual > 0) {
				if (human.getAct() < (confInstStage.needManual) * numJoin) {
					// 发送文字提示消息 活力不足！
					humanObj.sendSysMsg(7);
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 是否低于进入等级
	 * @return
	 */
	private boolean isUnderLvEnter(int lvHuman, ConfInstActConfig conf) {
		boolean isUnder = true;
		if (lvHuman >= conf.lvEnter) {
			isUnder = false;
		}
		return isUnder;
	}
	
	/**
	 * 是否解锁了关卡
	 * @return
	 */
	private boolean isUnlockActInst(String actInstStoryLastPass, ConfInstActConfig conf) {
		boolean isUnlock = true;// 已解锁
		return isUnlock;
	}
	/**
	 * 获取是否解锁扫荡
	 * @param actInstStoryLastPass
	 * @return
	 */
	public boolean isUnlockActInst(String actInstStoryLastPass, ConfInstActConfig conf,boolean isAuto){
		boolean isUnlock = true;// 已解锁
		return isUnlock;
	}
	
	/**
	 * 成功通关活动副本，修改完成次数及最后通关记录
	 * @param humanObj
	 * @param stageSn 关卡sn
	 * @param isAuto true即扫荡,false手动
	 * @param num =1即手动通关;>1即扫荡次数
	 */
	public void passActInst(HumanObject humanObj, int stageSn, boolean isAuto, int num) {
		ConfInstActConfig conf = ConfInstActConfig.getBy("stageSn", stageSn);
		if (conf == null) {
			Log.human.error("===ConfInstActConfig no find stageSn={}", stageSn);
			return;
		}
		
		// 活动副本完成次数增加n
		TeamManager.inst().addActInstFinishNum(humanObj, conf.sn, num);
	}

	/**
	 * 获取剩余参与次数
	 * @param actInstFinishNum
	 * @return
	 */
	private int getRestDayJoinNum(String actInstFinishNum, ConfInstActConfig conf) {
		int num = 0;// 剩余参与次数
		if (conf == null) 
			return num;
		// 世界boss的参与次数无限制
		if (conf.type == EActInstType.WorldBoss_VALUE)
			return 1;
		
		int actInstSn = conf.sn;
		Map<Integer, Integer> mapRepFinish = Utils.jsonToMapIntInt(actInstFinishNum);
		
		// 其它类型的记录格式："110001":单个完成次数
		num = conf.dayJoinNum;
		if (mapRepFinish.containsKey(actInstSn)) {
			num -= mapRepFinish.get(actInstSn);
		}
		return num;
	}

	/**
	 * 是否已无奖励次数
	 * @param actInstFinishNum
	 * @return
	 */
	public boolean isNoneDayAwardNum(String actInstFinishNum, ConfInstActConfig conf) {
		if (conf == null) 
			return true;
		
		int actInstSn = conf.sn;
		boolean isNone = false;
		Map<Integer, Integer> mapRepFinish = Utils.jsonToMapIntInt(actInstFinishNum);
	
		// 其它类型的记录格式："110001":单个完成次数
		if (mapRepFinish.containsKey(actInstSn) && mapRepFinish.get(actInstSn) >= conf.dayAwardNum) {
			isNone = true;
		}
		return isNone;
	}
	
	/**
	 * 活动副本完成次数自增1
	 * @param humanObj
	 * @param actInstSn
	 */
	public void addActInstFinishNum(HumanObject humanObj, int actInstSn) {
		addActInstFinishNum(humanObj, actInstSn, 1);
	}
	/**
	 * 活动副本完成次数增加n
	 * @param humanObj
	 * @param actInstSn
	 * @param numAdd 增加次数
	 */
	public void addActInstFinishNum(HumanObject humanObj, int actInstSn, int numAdd) {
		ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);
		if (conf == null) 
			return;
		
		Human human = humanObj.getHuman();
		// 其它类型的记录格式："110001":单个完成次数
		// 单个完成次数自增1
		human.setDailyInstFinishNum(Utils.plusJSONValue(human.getDailyInstFinishNum(), actInstSn, numAdd));
	}
	
	/**
	 * 请求活动副本信息
	 */
	public void _msg_CSTeamRepInfo(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		SCTeamRepInfo.Builder msg = SCTeamRepInfo.newBuilder();
		msg.addAllDFinishNum(getActInstFinishNum(human));
		humanObj.sendMsg(msg);
	}
	/**
	 * 获取活动副本完成情况列表
	 * @return
	 */
	public List<DFinishNum> getActInstFinishNum(Human human) {
		List<DFinishNum> list = new ArrayList<>();
		//活动副本完成情况<actInstSn, num>
		Map<Integer, Integer> mapRepFinish = Utils.jsonToMapIntInt(human.getDailyInstFinishNum());
		for (Entry<Integer, Integer> entry : mapRepFinish.entrySet()) {
			Integer actInstSn = entry.getKey();
			Integer num = entry.getValue();
			if (actInstSn != null && num != null) {
				DFinishNum.Builder dFinishNum = DFinishNum.newBuilder();
				dFinishNum.setActInstSn(actInstSn);
				dFinishNum.setNum(num);
				list.add(dFinishNum.build());
			}
		}
		return list;
	}

	/**
	 * 创建指定副本的队伍
	 */
	public void _msg_CSTeamCreate(HumanObject humanObj, int actInstSn) {
		ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);
		if (conf == null) {
			Log.table.error("ConfActConfig配表错误，no find sn={}", actInstSn);
			return;
		}
		Human human = humanObj.getHuman();
		// if(human.getTeamId() > 0)
		// return;//已有队伍

		if (canJoinActInst(humanObj, actInstSn)) {
			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamCreate(human, actInstSn);
			prx.listenResult(this::_result_teamCreate, "humanObj", humanObj);
		}
	}

	private void _result_teamCreate(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		TeamData team = Utils.getParamValue(results, "team", null);
		if (humanObj == null || team == null) {
			Log.game.error("===_result_teamCreate huamnObj={}, team={}", humanObj, team);
			return;
		}
		humanObj.setTeam(team);
		humanObj.sendMsg(team.createMsg());
	}

	/**
	 * 快速加入指定副本的队伍，如无队伍存在则自己创建一个队伍
	 */
	public void _msg_CSTeamJoin(HumanObject humanObj, int actInstSn) {
		ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);
		if (conf == null) {
			Log.table.error("ConfActConfig配表错误，no find sn={}", actInstSn);
			return;
		}

		if (canJoinActInst(humanObj, actInstSn)) {
			Human human = humanObj.getHuman();
			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamJoin(human, actInstSn);
			prx.listenResult(this::_result_teamJoin, "humanObj", humanObj);
		}
	}

	private void _result_teamJoin(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		TeamData team = Utils.getParamValue(results, "team", null);
		if (humanObj == null || team == null) {
			Log.game.error("===_result_teamJoin huamnObj={}, team={}", humanObj, team);
			return;
		}
		if (team != null) {
			humanObj.setTeam(team);
			humanObj.sendMsg(team.createMsg());
		}
	}

	/**
	 * 主动离队
	 */
	public void _msg_CSTeamLeave(HumanObject humanObj) {
		TeamData team = humanObj.getTeam();
		int teamId = humanObj.getTeamId();
		if (teamId > 0 && team != null) {
			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamLeave(humanObj.id, teamId, team.actInstSn);
		}
	}

	/**
	 * 队长踢人（队长功能）
	 */
	public void _msg_CSTeamKickOut(HumanObject humanObj, long humanIdKickout) {
		TeamData team = humanObj.getTeam();
		int teamId = humanObj.getTeamId();
		if (teamId > 0 && team != null) {
			if (humanObj.id != team.leaderId) {
				return;// 队长才能踢人
			}
			if (humanObj.id == humanIdKickout) {
				return;// 不能踢自己
			}

			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamKickOut(humanIdKickout, teamId, team.actInstSn);
			prx.listenResult(this::_result_teamKickOut, "humanObj", humanObj);
		}
	}

	private void _result_teamKickOut(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		boolean result = Utils.getParamValue(results, "result", false);
		// 被踢的人通知被踢消息
		SCTeamKickOut.Builder msg = SCTeamKickOut.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}

	/**
	 * 好友邀请（队长功能）
	 */
	public void _msg_CSTeamInviteOne(HumanObject humanObj, long humanIdInvite) {
		TeamData team = humanObj.getTeam();
		int teamId = humanObj.getTeamId();
		if (teamId > 0 && team != null) {
			if (humanObj.id != team.leaderId) {
				return;// 队长才能邀请好友
			}
			if (humanObj.id == humanIdInvite) {
				return;// 不能邀请自己
			}

			// 查找被邀请的人
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.getInfo(humanIdInvite);
			prx.listenResult(this::_result_teamInvite, "team", team);
		}
	}

	private void _result_teamInvite(Param results, Param context) {
		TeamData team = Utils.getParamValue(context, "team", null);
		if (team == null) {
			Log.game.error("===_result_teamJoin team=null");
			return;
		}
		HumanGlobalInfo hg = results.get();
		if (hg != null) {// 在线
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			SCTeamInviteOne.Builder msg = SCTeamInviteOne.newBuilder();
			msg.setTeamInfo(team.createDTeamInfo());
			prx.sendMsg(hg.id, msg.build());
		}
	}

	/**
	 * 全服邀请（队长功能）
	 */
	public void _msg_CSTeamInviteAll(HumanObject humanObj, String content) {
		TeamData team = humanObj.getTeam();
		int teamId = humanObj.getTeamId();
		if (teamId > 0 && team != null) {
			if (humanObj.id != team.leaderId) {
				return;// 队长才能邀请好友
			}

			content = "{" + MsgIds.CSTeamInviteAll + "|" + teamId + "|" + team.actInstSn + "}" + content;
			InformManager.inst().all(content);
		}
	}

	/**
	 * 申请入队
	 */
	public void _msg_CSTeamApplyJoin(HumanObject humanObj, int teamId, int actInstSn) {
		if (humanObj.getTeamId() > 0) {
			_send_SCTeamApplyJoin(humanObj, 521401);// 521401已有队伍
		} else {
			if (canJoinActInst(humanObj, actInstSn)) {
				TeamServiceProxy prx = TeamServiceProxy.newInstance();
				prx.teamApplyJoin(humanObj.getHuman(), teamId, actInstSn);
				prx.listenResult(this::_result_teamApplyJoin, "humanObj", humanObj);
			}
		}
	}

	private void _result_teamApplyJoin(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_teamApplyJoin humanObj is null");
			return;
		}
		int result = Utils.getParamValue(results, "result", 0);// 0成功（成功才返回队伍信息）；>0失败
		_send_SCTeamApplyJoin(humanObj, result);
	}

	/**
	 * 发送申请入队失败结果
	 */
	private void _send_SCTeamApplyJoin(HumanObject humanObj, int result) {
		if (result > 0) {// >0失败（参见SysMsg.xlsx：521401已有队伍，521402队伍不存在，521403已开始副本，521404人数已满）
			SCTeamApplyJoin.Builder msg = SCTeamApplyJoin.newBuilder();
			msg.setResult(result);
			humanObj.sendMsg(msg);
		}
	}

	/**
	 * 进入副本
	 */
	public void _msg_CSTeamEnterRep(HumanObject humanObj) {
		TeamData team = humanObj.getTeam();
		int teamId = humanObj.getTeamId();
		if (teamId > 0 && team != null) {
			if (humanObj.id != team.leaderId) {
				_send_SCTeamEnterRep(humanObj, 521601);// 521601队长才能开始副本
				return;
			}

			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamEnterRep(teamId, team.actInstSn);
			prx.listenResult(this::_result_teamEnterRep, "humanObj", humanObj);
		}
	}

	private void _result_teamEnterRep(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_teamEnterRep humanObj is null");
			return;
		}
		int result = Utils.getParamValue(results, "result", 0);
		_send_SCTeamEnterRep(humanObj, result);
	}

	/**
	 * 发送进入副本结果
	 */
	private void _send_SCTeamEnterRep(HumanObject humanObj, int result) {
		if (result > 0) {// >0失败（参见SysMsg.xlsx：521601队长才能开始副本，521602活动副本已关闭，521603活动副本已开启）
			SCTeamEnterRep.Builder msg = SCTeamEnterRep.newBuilder();
			msg.setResult(result);
			humanObj.sendMsg(msg);
		}
	}

	/**
	 * 开始匹配
	 */
	public void _msg_CSTeamMatch(HumanObject humanObj) {
		TeamData team = humanObj.getTeam();
		int teamId = humanObj.getTeamId();
		if (teamId > 0 && team != null) {
			if (humanObj.id != team.leaderId) {
				_send_SCTeamMatch(humanObj, 525301);// 队长才能开始匹配
				return;
			}

			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamMatch(teamId, team.actInstSn);
			prx.listenResult(this::_result_teamMatch, "humanObj", humanObj);
		}
	}

	private void _result_teamMatch(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_teamEnterRep humanObj is null");
			return;
		}
		int result = Utils.getParamValue(results, "result", 0);
		_send_SCTeamMatch(humanObj, result);
	}

	/**
	 * 发送开始匹配结果
	 */
	private void _send_SCTeamMatch(HumanObject humanObj, int result) {
		// 0成功（成功即进入匹配状态）；>0失败（参见SysMsg.xlsx：525301队长才能开始匹配，525302活动副本已关闭，525303活动副本已开启）
		SCTeamMatch.Builder msg = SCTeamMatch.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}

	/**
	 * 取消匹配
	 */
	public void _msg_CSTeamMatchCancel(HumanObject humanObj) {
		TeamData team = humanObj.getTeam();
		int teamId = humanObj.getTeamId();
		if (teamId > 0 && team != null) {
			if (humanObj.id != team.leaderId) {
				_send_SCTeamMatchCancel(humanObj, 525801);// 队长才能取消匹配
				return;
			}

			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamMatchCancel(teamId, team.actInstSn);
			prx.listenResult(this::_result_teamMatchCancel, "humanObj", humanObj);
		}
	}

	private void _result_teamMatchCancel(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		int result = Utils.getParamValue(results, "result", 0);
		if (humanObj == null) {
			Log.game.error("===_result_teamMatchCancel humanObj is null");
			return;
		}
		_send_SCTeamMatchCancel(humanObj, result);
	}

	/**
	 * 发送取消匹配结果
	 */
	private void _send_SCTeamMatchCancel(HumanObject humanObj, int result) {
		// 0成功（成功即取消匹配状态）；>0失败（参见SysMsg.xlsx：525801队长才能取消匹配，525802队伍不存在）
		SCTeamMatchCancel.Builder msg = SCTeamMatchCancel.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}

	/**
	 * 搜索队伍
	 */
	public void _msg_CSFindTeam(HumanObject humanObj, int teamId) {
		if (humanObj.getTeamId() > 0) {
			_send_SCFindTeamResult(humanObj, 525601);// 你已有队伍！
		} else {
			TeamServiceProxy prx = TeamServiceProxy.newInstance();
			prx.teamFind(humanObj.getHuman(), teamId);
			prx.listenResult(this::_result_teamFind, "humanObj", humanObj);
		}
	}

	private void _result_teamFind(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		int result = Utils.getParamValue(results, "result", 0);
		if (humanObj == null) {
			Log.game.error("===_result_teamMatchCancel humanObj is null");
			return;
		}
		_send_SCFindTeamResult(humanObj, result);
	}

	/**
	 * 发送搜索队伍结果
	 */
	private void _send_SCFindTeamResult(HumanObject humanObj, int result) {
		// 0成功（成功即加入队伍）；>0失败（参见SysMsg.xlsx：525601你已有队伍，525602队伍不存在，525603队伍正在战斗中，525604队伍人数已满）
		SCFindTeamResult.Builder msg = SCFindTeamResult.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
	}

}