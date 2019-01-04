package game.worldsrv.team;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DMemberInfo;
import game.msg.Define.DTeamInfo;
import game.msg.Define.DMemberInfo;
import game.msg.Define.DTeamInfo;
import game.msg.MsgTeam.SCMemberInfo;
import game.msg.MsgTeam.SCTeamInfo;
import game.msg.MsgTeam.SCTeamMemberInfo;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.entity.Human;
import game.worldsrv.support.Log;

public class TeamData implements ISerilizable {
	public int teamId = 0; // 队伍Id
	public int actInstSn = 0; // 活动副本Sn
	public long leaderId = 0; // 队长的玩家Id

	private int groupId = 1; // 小组Id：1即一队小组，2即二队小组
	private int numMax = 0; // 副本进入最大人数
	private int numMin = 0; // 副本进入最小人数
	private boolean isMatch = false; // 是否已开始匹配
	private boolean isStart = false; // 是否已开始副本
	private long timeOpen = 0; // 开启时间（时间戳）
	private long timeClose = 0; // 关闭时间（时间戳）

	// 队员信息列表，按先后加入顺序排
	private List<TeamMember> listMemberInfo = new ArrayList<>();

	public TeamData() {
	}

	public TeamData(int teamId, Human human, ConfInstActConfig conf) {
		this.teamId = teamId;
		this.actInstSn = conf.sn;
		this.leaderId = human.getId();

		this.numMax = conf.numMax;
		this.numMin = conf.numMin;
		this.isStart = false;
		this.timeOpen = 0;
		this.timeClose = 0;

		TeamMember member = new TeamMember(human);
		this.add(member);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("teamId", teamId)
				.append("actInstSn", actInstSn).append("leaderId", leaderId).append("numMax", numMax)
				.append("numMin", numMin).append("isStart", isStart).append("isMatch", isMatch).toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(groupId);
		out.write(teamId);
		out.write(actInstSn);
		out.write(leaderId);
		out.write(numMax);
		out.write(numMin);
		out.write(isStart);
		out.write(isMatch);
		out.write(listMemberInfo);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		groupId = in.read();
		teamId = in.read();
		actInstSn = in.read();
		leaderId = in.read();
		numMax = in.read();
		numMin = in.read();
		isStart = in.read();
		isMatch = in.read();
		listMemberInfo = in.read();
	}

	/**
	 * 创建队伍简要信息
	 * @return
	 */
	public DTeamInfo createDTeamInfo() {
		DTeamInfo.Builder dTeamInfo = DTeamInfo.newBuilder();
		TeamMember leader = getLeaderInfo();
		int teamSize = getTeamSize();
		if (leader != null && teamSize > 0) {
			dTeamInfo.setTeamId(teamId);
			dTeamInfo.setActInstSn(actInstSn);
			dTeamInfo.setLeaderId(leaderId);
			dTeamInfo.setLeaderName(leader.name);
			dTeamInfo.setLeaderLevel(leader.level);
			dTeamInfo.setTeamNum(teamSize);
		}
		return dTeamInfo.build();
	}

	/**
	 * 创建队员信息
	 * @return
	 */
	public List<DMemberInfo> createDMemberInfo() {
		List<DMemberInfo> list = new ArrayList<DMemberInfo>();
		// 队员信息列表
		for (TeamMember member : listMemberInfo) {
			DMemberInfo.Builder dMemberInfo = DMemberInfo.newBuilder();
			dMemberInfo.setId(member.id);
			dMemberInfo.setName(member.name);
			dMemberInfo.setLevel(member.level);
			dMemberInfo.setCombat(member.combat);
			dMemberInfo.setProfession(member.profession);
			dMemberInfo.setSex(member.sex);
			dMemberInfo.setModelSn(member.modelSn);
			dMemberInfo.setIsOnline(member.isOnline);
			list.add(dMemberInfo.build());
		}
		return list;
	}

	/**
	 * 创建队伍详细信息的消息包
	 * @return
	 */
	public SCTeamMemberInfo createMsg() {
		SCTeamMemberInfo.Builder msg = SCTeamMemberInfo.newBuilder();
		msg.setTeamInfo(createDTeamInfo());// 队伍信息
		msg.addAllMemberInfo(createDMemberInfo());// 队员信息
		return msg.build();
	}

	/**
	 * 创建队伍简要信息的消息包
	 * @return
	 */
	public SCTeamInfo createTeamInfoMsg() {
		SCTeamInfo.Builder msg = SCTeamInfo.newBuilder();
		msg.setTeamInfo(createDTeamInfo());// 队伍信息
		return msg.build();
	}

	/**
	 * 创建队员信息的消息包
	 * @return
	 */
	public SCMemberInfo createMemberInfoMsg() {
		SCMemberInfo.Builder msg = SCMemberInfo.newBuilder();
		msg.addAllMemberInfo(createDMemberInfo());// 队员信息
		return msg.build();
	}

	/**
	 * 加入一个队员
	 * @param member
	 */
	public boolean add(TeamMember member) {
		if (listMemberInfo.size() >= numMax)
			return false;// 队伍已满

		if (listMemberInfo.isEmpty()) {// 第一个加入的设为队长
			leaderId = member.id;
		}
		// 检查是否重复加入了，防止出现一人占多坑
		boolean canAdd = true;
		Iterator<TeamMember> it = listMemberInfo.iterator();
		while (it.hasNext()) {
			TeamMember mem = it.next();
			if (mem != null && mem.id == member.id) {
				Log.human.error("===team.add：teamId={},name={},id={} 重复入队了", this.teamId, mem.name, mem.id);
				canAdd = false;
				break;
			}
		}
		if (canAdd) {
			listMemberInfo.add(member);
		}
		return true;
	}

	/**
	 * 踢出一个队员
	 * @param id 玩家ID
	 */
	public boolean delete(long id) {
		boolean ret = false;
		Iterator<TeamMember> it = listMemberInfo.iterator();
		while (it.hasNext()) {
			TeamMember mem = it.next();
			if (mem != null && mem.id == id) {
				it.remove();
				ret = true;
				break;
			}
		}
		return ret;
	}

	/**
	 * 获取队伍人数
	 */
	public int getTeamSize() {
		int ret = 0;
		if (listMemberInfo != null)
			ret = listMemberInfo.size();
		return ret;
	}

	/**
	 * 获取队伍空位置数
	 */
	public int getTeamEmptySize() {
		int ret = 0;
		if (listMemberInfo != null)
			ret = numMax - listMemberInfo.size();
		return ret;
	}

	/**
	 * 是否空队伍
	 * @return
	 */
	public boolean isEmpty() {
		boolean ret = true;
		if (listMemberInfo != null)
			ret = listMemberInfo.isEmpty();
		return ret;
	}

	/**
	 * 是否满员
	 * @return
	 */
	public boolean isFull() {
		boolean ret = false;
		if (listMemberInfo != null && listMemberInfo.size() >= numMax)
			ret = true;
		return ret;
	}

	/**
	 * 获取小组Id
	 * @return
	 */
	public int getGroupId() {
		return this.groupId;
	}

	/**
	 * 设置小组Id
	 * @param groupId
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	/**
	 * 是否已关闭副本
	 * @return
	 */
	public boolean isClose() {
		// 判断时间
		return false;
	}

	/**
	 * 是否已开始副本
	 * @return
	 */
	public boolean isStart() {
		return isStart;
	}

	/**
	 * 开始副本
	 * @return
	 */
	public void startOpen() {
		if (!isStart)
			isStart = true;
	}

	/**
	 * 结束副本
	 * @return
	 */
	public void startClose() {
		if (isStart)
			isStart = false;
	}

	/**
	 * 是否已开始匹配
	 * @return
	 */
	public boolean isMatch() {
		return isMatch;
	}

	/**
	 * 开始匹配
	 * @return
	 */
	public void matchOpen() {
		if (!isMatch)
			isMatch = true;
	}

	/**
	 * 结束匹配
	 * @return
	 */
	public void matchClose() {
		if (isMatch)
			isMatch = false;
	}

	/**
	 * 是否能进入副本
	 * @return
	 */
	public boolean canEnter() {
		boolean ret = false;
		if (listMemberInfo != null && listMemberInfo.size() >= numMin)
			ret = true;
		return ret;
	}

	/**
	 * 获取队长信息
	 * @return
	 */
	public TeamMember getLeaderInfo() {
		TeamMember leader = null;
		if (!listMemberInfo.isEmpty()) {// 第一个就是队长
			leader = listMemberInfo.get(0);
		}
		return leader;
	}

	/**
	 * 获取指定队员信息
	 * @return
	 */
	public TeamMember getMemberInfo(long id) {
		TeamMember mem = null;
		for (TeamMember member : listMemberInfo) {
			if (member.id == id) {
				mem = member;
				break;
			}
		}
		return mem;
	}

	/**
	 * 判断是否是队员
	 * @return
	 */
	public boolean isTeamMember(long id) {
		boolean ret = false;
		TeamMember mem = getMemberInfo(id);
		if (mem != null)
			ret = true;
		return ret;
	}

	/**
	 * 判断是否是队长
	 * @return
	 */
	public boolean isTeamLeader(long id) {
		boolean ret = false;
		if (id == leaderId)
			ret = true;
		return ret;
	}

	/**
	 * 返回所有队员的玩家ID列表
	 * @return
	 */
	public List<Long> getAllId() {
		ArrayList<Long> result = new ArrayList<>();
		for (TeamMember member : listMemberInfo) {
			result.add(member.id);
		}
		return result;
	}

	/**
	 * 返回第一个队员的玩家ID（非队长）
	 * @return
	 */
	public Long getFirstMemberId() {
		Long result = 0L;
		for (TeamMember member : listMemberInfo) {
			if (member.id != this.leaderId) {// 不是队长
				result = member.id;
				break;
			}
		}
		return result;
	}

	/**
	 * 获取队伍总战力
	 * @return
	 */
	public int getTeamCombat() {
		int ret = 0;
		for (TeamMember member : listMemberInfo) {
			if (ret > Integer.MAX_VALUE - member.combat)
				ret = Integer.MAX_VALUE;
			else
				ret += member.combat;
		}
		return ret;
	}

	/**
	 * 并入未满队伍
	 * @param team
	 * @return
	 */
	public boolean mergeOtherTeam(TeamData team) {
		boolean ret = false;
		if (team != null) {
			if (this.getTeamEmptySize() >= team.getTeamSize()) {// 空位置数>=队伍人数
				for (TeamMember member : team.listMemberInfo) {
					this.add(member);
				}
				ret = true;
			}
		}
		return ret;
	}

}
