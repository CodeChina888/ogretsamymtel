package crosssrv.groupFight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

/**
 * pvp房间信息
 * 
 * @author lxf
 *
 */
public class GroupFightStageInfo implements ISerilizable {
	/** 玩家id **/
	public List<Long> humanIds = new ArrayList<>();
	/** 服务器ID **/
	public String serverNodeId;
	/** 服务器ID **/
	public String serverPortId;
	/** 队伍Id **/
	public List<Long> teamIds = new ArrayList<>();
	/** 战场类型 **/
	public int type;
	/** 场景Id **/
	public long stageId;
	/** 战场配置sn **/
	public int stageSn;
	/** 战场地图sn **/
	public int mapSn;
	/** 节点分支Id **/
	public int partId;
	/** 创建时间 **/
	public long startTime;

	public GroupFightStageInfo() {

	}

	/**
	 * 构造函数
	 * 
	 * @param humanIds
	 * @param teamIds
	 * @param type
	 * @param serverNodeId
	 * @param serverPortId
	 * @param stageId
	 * @param sn
	 * @param partId
	 * @param startTime
	 */
	public GroupFightStageInfo(List<Long> humanIds, List<Long> teamIds, int type, String serverNodeId,
			String serverPortId, long stageId, int stageSn, int mapSn, int partId, long startTime) {
		this.humanIds = humanIds;
		this.teamIds = teamIds;
		this.type = type;
		this.serverNodeId = serverNodeId;
		this.serverPortId = serverPortId;
		this.stageId = stageId;
		this.stageSn = stageSn;
		this.mapSn = mapSn;
		this.partId = partId;
		this.startTime = startTime;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(humanIds);
		out.write(teamIds);
		out.write(type);
		out.write(stageId);
		out.write(stageSn);
		out.write(mapSn);
		out.write(serverNodeId);
		out.write(serverPortId);
		out.write(partId);
		out.write(startTime);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		humanIds = in.read();
		teamIds = in.read();
		type = in.read();
		stageId = in.read();
		stageSn = in.read();
		mapSn = in.read();
		serverNodeId = in.read();
		serverPortId = in.read();
		partId = in.read();
		startTime = in.read();
	}
}
