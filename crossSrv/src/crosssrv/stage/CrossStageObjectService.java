package crosssrv.stage;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Distr;
import core.support.SysException;
import core.support.Time;
import crosssrv.character.CombatantObject;
import crosssrv.combatant.CombatantGlobalInfo;

@DistrClass(importClass = { CombatantObject.class, CombatantGlobalInfo.class })
public class CrossStageObjectService extends Service {
	protected final CrossStageObject stageObj;

	/**
	 * 初始化数据
	 * 
	 * @return
	 */
	protected void init() {

	}

	/**
	 * 启动服务
	 */
	public void startupLocal() {
		this.startup();
		init();
	}

	/**
	 * 构造函数
	 * 
	 * @param stageObj
	 */
	public CrossStageObjectService(CrossStageObject stageObj, Port port) {
		super(port);
		this.stageObj = stageObj;
	}

	@Override
	public Object getId() {
		return stageObj.stageId;
	}

	/**
	 * 每个service预留空方法
	 * 
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {

	}

	@Override
	public void pulseOverride() {
		stageObj.pulse();
	}

	public CrossStageObject getStageObj() {
		return stageObj;
	}

	@DistrMethod
	public void clearError(long curr) {
		if (curr - this.stageObj.getCreateTime() > 10 * Time.MIN) {
			if (this.stageObj.getCombatantObjs().isEmpty()) {
				this.stageObj.destory();
			}
		}
	}

	@DistrMethod
	public void login(CombatantGlobalInfo info) {
		stageObj.login(info);
		String portId = stageObj.getPort().getId();
		port.returns("stageId", stageObj.stageId, "node", Distr.getNodeId(portId), "port", portId);
	}

	/**
	 * 将玩家注册进地图
	 * 
	 * @param combatantObj
	 * @param isRevive
	 * @param stageTargetId
	 */
	@DistrMethod
	public void register(CombatantObject combatantObj, boolean isRevive, long stageTargetId) {
		// 如果玩家已经在了，就直接返回信息
		if (stageObj.getCombatantObj(combatantObj.id) != null) {
			// int count = 0;
			// if (stageObj.getCombatantObjs() != null) {
			// count = stageObj.getCombatantObjs().entrySet().size();
			// }
			// Port.getCurrent().returns("success", true, "posNow",
			// combatantObj.posNow, "dirNow", combatantObj.dirNow, "stageSn",
			// stageObj.getStageSnSn(),"counter", count);
			return;
		}

		try {
			// 将玩家注入进地图
			// combatantObj.stageRegister(stageObj);

			// 返回信息
			// int count = 0;
			// if (stageObj.getCombatantObjs() != null) {
			// count = stageObj.getCombatantObjs().entrySet().size();
			// }
			// Port.getCurrent().returns("success", true, "posNow",
			// combatantObj.posNow, "dirNow", combatantObj.dirNow, "stageSn",
			// stageSn, "counter", count);

		} catch (Exception e) {
			Port.getCurrent().returns("success", false);
			// Log.stageCommon.error("注册地图场景发生错误，玩家名字 {}，场景SN {}",
			// human.getName(), stageObj.sn);

			throw new SysException(e);
		}
	}

}
