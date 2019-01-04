package crosssrv.combatant;

import java.io.IOException;

import core.CallPoint;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.ETeamType;
import game.worldsrv.character.HumanMirrorObject;

public class CombatantGlobalInfo implements ISerilizable {
	public long id; // ID
	public String nodeId; // Node名称
	public String portId; // Port名称
	public long stageId; // 所在地图
	public String stageNodeId;		//跨服房间所在Node名称
	public String stagePortId;		//跨服房间所在Port名称
	public int crossPartId;			//所处跨服通讯节点
	
	public ETeamType team; // 所属战队
	public int order; // 序号
	public int stageSn; // 战场配置sn
	public int mapSn; // 战场地图sn
	public long teamId; // 队伍ID
	public CallPoint connPoint = new CallPoint();// 玩家连接ID
	public String token; // 验证串
	public boolean isLogined = false; // 是否登录状态
	public long shceduleTime; // 调度任务时间
	public HumanMirrorObject humanMirrorObj;

	public CombatantGlobalInfo() {

	}

	public CombatantGlobalInfo(long id, String nodeId, String portId, ETeamType team, int order, int stageSn, int mapSn,
			long teamId, String token, HumanMirrorObject humanMirror, long stageId, 
			String stageNodeId, String stagePortId, int crossPartId) {
		this.id = id;
		this.nodeId = nodeId;
		this.portId = portId;
		this.stageId = stageId;
		this.stageNodeId = stageNodeId;
		this.stagePortId = stagePortId;
		this.crossPartId = crossPartId;
		
		this.team = team;
		this.order = order;
		this.stageSn = stageSn;
		this.mapSn = mapSn;
		this.teamId = teamId;
		this.token = token;
		this.humanMirrorObj = humanMirror;
		
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(nodeId);
		out.write(portId);
		out.write(stageId);
		out.write(stageNodeId);
		out.write(stagePortId);
		out.write(crossPartId);
		
		out.write(connPoint);
		out.write(teamId);
		out.write(token);
		out.write(shceduleTime);
		out.write(team);
		out.write(order);
		out.write(stageSn);
		out.write(mapSn);
		out.write(humanMirrorObj);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		nodeId = in.read();
		portId = in.read();
		stageId = in.read();
		stageNodeId = in.read();
		stagePortId = in.read();
		crossPartId = in.read();
		
		connPoint = in.read();
		teamId = in.read();
		token = in.read();
		shceduleTime = in.read();
		team = in.read();
		order = in.read();
		stageSn = in.read();
		mapSn = in.read();
		humanMirrorObj = in.read();
	}
}
