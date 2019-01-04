package crosssrv.singleFight;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

/**
 * pvp房间信息
 * 
 * @author lxf
 *
 */
public class SingleFightStageInfo implements ISerilizable {
	/** 玩家id **/
	public Long humanId;
	/** 服务器ID **/
	public String nodeId;
	/** 服务器ID **/
	public String portId;
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

	public SingleFightStageInfo() {

	}

	/**
	 * 构造函数
	 * 
	 * @param humanIds
	 * @param type
	 * @param serverNodeId
	 * @param serverPortId
	 * @param stageId
	 * @param sn
	 * @param partId
	 * @param startTime
	 */
	public SingleFightStageInfo(long humanId, int type, String serverNodeId, String serverPortId, long stageId,
			int stageSn, int mapSn, int partId, long startTime) {
		this.humanId = humanId;
		this.type = type;
		this.nodeId = serverNodeId;
		this.portId = serverPortId;
		this.stageId = stageId;
		this.stageSn = stageSn;
		this.mapSn = mapSn;
		this.partId = partId;
		this.startTime = startTime;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(humanId);
		out.write(type);
		out.write(stageId);
		out.write(stageSn);
		out.write(mapSn);
		out.write(nodeId);
		out.write(portId);
		out.write(partId);
		out.write(startTime);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		humanId = in.read();
		type = in.read();
		stageId = in.read();
		stageSn = in.read();
		mapSn = in.read();
		nodeId = in.read();
		portId = in.read();
		partId = in.read();
		startTime = in.read();
	}
}
