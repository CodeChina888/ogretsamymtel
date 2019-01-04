package crosssrv.groupFight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import game.msg.Define.ETeamType;

/**
 * 多人战斗的队伍信息
 */
public class GroupFightTeam implements ISerilizable {
	public long teamId;// 队伍Id
	public long enemyTeamId;// 敌对队伍Id
	public Set<Long> humanIds = new HashSet<>();// 队员id集合

	public long stageId;// 场景Id
	public int stageSn;// 场景Sn
	public int mapSn;// 地图Sn
	public int type;// pvp类型
	public int fightMin;// 最少战斗人数
	public int fightMax;// 最大战斗人数
	public long startTime;// 达到最小开战人数的时间
	public ETeamType team = ETeamType.Team1;// 所属战队
	public GroupFightStatus state = GroupFightStatus.Match;// 多人战斗状态

	public GroupFightTeam(long teamId, Long humanId, int type, int stageSn, int mapSn, int fightMin, int fightMax) {
		this.teamId = teamId;
		this.humanIds.add(humanId);
		this.type = type;
		this.stageSn = stageSn;
		this.mapSn = mapSn;
		this.fightMin = fightMin;
		this.fightMax = fightMax;
		this.startTime = Port.getTime();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(teamId);
		out.write(enemyTeamId);
		out.write(humanIds);

		out.write(stageId);
		out.write(stageSn);
		out.write(mapSn);
		out.write(type);
		out.write(fightMin);
		out.write(fightMax);
		out.write(startTime);
		out.write(team);
		out.write(state);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		teamId = in.read();
		enemyTeamId = in.read();
		humanIds = in.read();

		stageId = in.read();
		stageSn = in.read();
		mapSn = in.read();
		type = in.read();
		fightMin = in.read();
		fightMax = in.read();
		startTime = in.read();
		team = in.read();
		state = in.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("humanIds", humanIds).toString();
	}

	/**
	 * 队伍是否已满
	 */
	public boolean isFull() {
		return humanIds.size() >= fightMax;
	}

	/**
	 * 是否未开战且符合最低开战人数
	 * 
	 * @return
	 */
	public boolean canStart() {
		return enemyTeamId == 0 && humanIds.size() >= fightMin;
	}

	/**
	 * 是否在战斗状态
	 * 
	 * @return
	 */
	public boolean isInFight() {
		return state == GroupFightStatus.Fight;
	}

	/**
	 * 获取成员ID列表
	 * 
	 * @return
	 */
	public List<Long> getIds() {
		List<Long> ids = new ArrayList<>();
		for (long id : humanIds) {
			ids.add(id);
		}
		return ids;
	}

}
