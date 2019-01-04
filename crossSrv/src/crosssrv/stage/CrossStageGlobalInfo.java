package crosssrv.stage;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 地图全局信息 记录Node,Port等信息，地图间通信等情况下会用到。
 */
public class CrossStageGlobalInfo {
	public final long stageId; // 地图真实ID
	public final int mapSn; // 地图配置SN
	public final String mapName; // 地图名称
	public final String nodeId; // Node名称
	public final String portId; // Port名称
	public int lineNum = 0; // 分线的编号，编号从0开始（0表示主线，1表示第1条分线即第2条记录）
	public int humanNum = 0; // 地图中玩家个数
	public boolean isEnd = false;// 是否结束

	/**
	 * 构造函数
	 * 
	 * @param id
	 * @param sn
	 * @param name
	 * @param nodeId
	 * @param portId
	 */
	public CrossStageGlobalInfo(long id, int sn, String name, String nodeId, String portId) {
		this.stageId = id;
		this.mapSn = sn;
		this.mapName = name;
		this.nodeId = nodeId;
		this.portId = portId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("stageId", stageId)
				.append("mapSn", mapSn).append("mapName", mapName).append("nodeId", nodeId).append("portId", portId)
				.append("lineNum", lineNum).append("humanNum", humanNum).toString();
	}
}
