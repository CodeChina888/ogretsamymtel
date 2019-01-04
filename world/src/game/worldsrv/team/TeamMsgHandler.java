package game.worldsrv.team;

import game.msg.MsgTeam.CSFindTeam;
import game.msg.MsgTeam.CSTeamApplyJoin;
import game.msg.MsgTeam.CSTeamCreate;
import game.msg.MsgTeam.CSTeamEnterRep;
import game.msg.MsgTeam.CSTeamInviteAll;
import game.msg.MsgTeam.CSTeamInviteOne;
import game.msg.MsgTeam.CSTeamJoin;
import game.msg.MsgTeam.CSTeamKickOut;
import game.msg.MsgTeam.CSTeamLeave;
import game.msg.MsgTeam.CSTeamMatch;
import game.msg.MsgTeam.CSTeamMatchCancel;
import game.msg.MsgTeam.CSTeamRepInfo;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import core.support.observer.MsgReceiver;

/**
 * 组队
 */
public class TeamMsgHandler {

	/**
	 * 创建指定副本的队伍
	 * @param param
	 */
	@MsgReceiver(CSTeamCreate.class)
	public void _msg_CSTeamCreate(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTeamCreate msg = param.getMsg();
		TeamManager.inst()._msg_CSTeamCreate(humanObj, msg.getActInstSn());
	}

	/**
	 * 快速加入指定副本的队伍，如无队伍存在则自己创建一个队伍
	 * @param param
	 */
	@MsgReceiver(CSTeamJoin.class)
	public void _msg_CSTeamJoin(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTeamJoin msg = param.getMsg();
		TeamManager.inst()._msg_CSTeamJoin(humanObj, msg.getActInstSn());
	}

	/**
	 * 主动离队
	 * @param param
	 */
	@MsgReceiver(CSTeamLeave.class)
	public void _msg_CSTeamLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TeamManager.inst()._msg_CSTeamLeave(humanObj);
	}

	/**
	 * 队长踢人（队长功能）
	 * @param param
	 */
	@MsgReceiver(CSTeamKickOut.class)
	public void _msg_CSTeamKickOut(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTeamKickOut msg = param.getMsg();
		TeamManager.inst()._msg_CSTeamKickOut(humanObj, msg.getId());
	}

	/**
	 * 好友邀请（队长功能）
	 * @param param
	 */
	@MsgReceiver(CSTeamInviteOne.class)
	public void _msg_CSTeamInviteOne(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTeamInviteOne msg = param.getMsg();
		TeamManager.inst()._msg_CSTeamInviteOne(humanObj, msg.getId());
	}

	/**
	 * 全服邀请（队长功能）
	 * @param param
	 */
	@MsgReceiver(CSTeamInviteAll.class)
	public void _msg_CSTeamInviteAll(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTeamInviteAll msg = param.getMsg();
		TeamManager.inst()._msg_CSTeamInviteAll(humanObj, msg.getContent());
	}

	/**
	 * 申请入队
	 * @param param
	 */
	@MsgReceiver(CSTeamApplyJoin.class)
	public void _msg_CSTeamApplyJoin(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTeamApplyJoin msg = param.getMsg();
		TeamManager.inst()._msg_CSTeamApplyJoin(humanObj, msg.getTeamId(), msg.getActInstSn());
	}

	/**
	 * 进入副本
	 * @param param
	 */
	@MsgReceiver(CSTeamEnterRep.class)
	public void _msg_CSTeamEnterRep(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TeamManager.inst()._msg_CSTeamEnterRep(humanObj);
	}

	/**
	 * 请求活动副本信息
	 * @param param
	 */
	@MsgReceiver(CSTeamRepInfo.class)
	public void _msg_CSTeamRepInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TeamManager.inst()._msg_CSTeamRepInfo(humanObj);
	}
	
	/**
	 * 开始匹配
	 * @param param
	 */
	@MsgReceiver(CSTeamMatch.class)
	public void _msg_CSTeamMatch(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TeamManager.inst()._msg_CSTeamMatch(humanObj);
	}

	/**
	 * 取消匹配
	 * @param param
	 */
	@MsgReceiver(CSTeamMatchCancel.class)
	public void _msg_CSTeamMatchCancel(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TeamManager.inst()._msg_CSTeamMatchCancel(humanObj);
	}

	/**
	 * 搜索队伍
	 * @param param
	 */
	@MsgReceiver(CSFindTeam.class)
	public void _msg_CSFindTeam(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSFindTeam msg = param.getMsg();
		TeamManager.inst()._msg_CSFindTeam(humanObj, msg.getTeamId());
	}

}
