package game.worldsrv.stage;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 地图全局信息 记录Node,Port等信息，地图间通信等情况下会用到。
 */
public class StageGlobalInfo {
	public final long stageId; // 场景ID
	public final int stageSn; // 副本SN(0即非副本)
	public final int mapSn; // 地图SN
	public final String mapName; // 地图名称
	public final String nodeId; // Node名称
	public final String portId; // Port名称
	public int lineNum = 0; // 分线的编号，编号从0开始（0表示主线，1表示第1条分线即第2条记录）
	public int humanNum = 0; // 地图中玩家个数

	/**
	 * 构造函数
	 * @param nodeId
	 * @param portId
	 */
	public StageGlobalInfo(long stageId, int stageSn, int mapSn, String mapName, String nodeId, String portId) {
		super();
		this.stageId = stageId;
		this.stageSn = stageSn;
		this.mapSn = mapSn;
		this.mapName = mapName;
		this.nodeId = nodeId;
		this.portId = portId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("stageId", stageId)
				.append("stageSn", stageSn).append("mapSn", mapSn).append("mapName", mapName).append("nodeId", nodeId)
				.append("portId", portId).append("lineNum", lineNum).append("humanNum", humanNum).toString();
	}
}
