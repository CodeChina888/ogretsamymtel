package crosssrv.stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Param;
import crosssrv.combatant.CombatantGlobalInfo;
import crosssrv.support.Log;
import game.worldsrv.support.D;

@DistrClass(importClass = { CombatantGlobalInfo.class, List.class })
public class CrossStageGlobalService extends Service {

	/** 地图信息集合<stageId,CrossStageGlobalInfo> **/
	private final Map<Long, CrossStageGlobalInfo> infos = new HashMap<Long, CrossStageGlobalInfo>();

	public CrossStageGlobalService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return D.CROSS_SERV_STAGE_GLOBAL;
	}

	/**
	 * 每个service预留空方法
	 * 
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {

	}

	/**
	 * 注册地图信息
	 * 
	 * @param stageId
	 * @param mapSn
	 */
	@DistrMethod
	public void infoRegister(long stageId, int mapSn, String stageName, String nodeId, String portId) {
		if (infos.containsKey(stageId))
			return;

		// 创建信息对象并缓存
		CrossStageGlobalInfo info = new CrossStageGlobalInfo(stageId, mapSn, stageName, nodeId, portId);
		infos.put(stageId, info);

		// 记录创建日志
		if (Log.fight.isInfoEnabled()) {
			Log.fight.info("创建地图：stageName={}, stageId={}, stageSn={}", stageName, stageId, mapSn);
		}
	}

	/**
	 * 设置副本结束
	 * 
	 * @param stageId
	 */
	@DistrMethod
	public void setEnd(long stageId) {
		CrossStageGlobalInfo info = infos.get(stageId);
		if (info != null) {
			info.isEnd = true;
		}
	}

	/**
	 * 注销地图信息
	 * 
	 * @param stageId
	 */
	@DistrMethod
	public void infoCancel(long stageId) {
		CrossStageGlobalInfo information = infos.remove(stageId);

		if (information == null) {
			Log.cross.warn("销毁地图时发现地图已不存在：stageId={}", stageId);
			return;
		}
	}

	/**
	 * 销毁地图
	 * 
	 * @param stageId
	 */
	@DistrMethod
	public void destroy(long stageId) {
		CrossStageGlobalInfo info = infos.get(stageId);
		if (info == null) {
			return;
		}
		CrossStageServiceProxy prx = CrossStageServiceProxy.newInstance(info.nodeId, info.portId,
				D.CROSS_SERV_STAGE_DEFAULT);
		prx.destroy(stageId);
	}

	/**
	 * 玩家进入房间地图
	 * 
	 * @param info
	 */
	@DistrMethod
	public void login(CombatantGlobalInfo info) {
		Long pid = port.createReturnAsync();
		CrossStageGlobalInfo information = this.infos.get(info.stageId);
		CrossStageObjectServiceProxy prx = CrossStageObjectServiceProxy.newInstance(information.nodeId,
				information.portId, information.stageId);
		prx.login(info);
		prx.listenResult(this::_result_login1, "pid", pid);
	}

	public void _result_login1(boolean timeout, Param results, Param context) {
		Long pid = context.get("pid");
		if (timeout) {
			port.returnsAsync(pid, "code", -2000);
			return;
		}

		port.returnsAsync(pid, results.toArray());
	}

	/**
	 * 地图玩家数量增加
	 * 
	 * @param stageId
	 */
	@DistrMethod
	public void stageHumanNumAdd(long stageId) {
		stageHumanNumChange(stageId, true);
	}

	/**
	 * 地图玩家数量减少
	 * 
	 * @param stageId
	 */
	@DistrMethod
	public void stageHumanNumReduce(long stageId) {
		stageHumanNumChange(stageId, false);
	}

	/**
	 * 地图玩家数量变动
	 * 
	 * @param stageId
	 * @param add
	 */
	private void stageHumanNumChange(long stageId, boolean add) {
		CrossStageGlobalInfo info = this.infos.get(stageId);

		if (info == null)
			return;

		// System.out.println("--------" +(info.humanNum + "") + (add?1:-1) +
		// "----");

		// 地图人数变动
		if (add) {
			info.humanNum = Math.max(0, info.humanNum + 1);
		} else {
			info.humanNum = Math.max(0, info.humanNum - 1);
		}
	}

	/**
	 * 在心跳里打印场景信息，并且调用清理
	 */
	@Override
	public void pulseOverride() {
	}

}
