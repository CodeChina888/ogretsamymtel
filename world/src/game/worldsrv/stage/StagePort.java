package game.worldsrv.stage;

import game.seam.id.IdAllotPool;

import core.Port;
import core.PortPulseQueue;
import core.support.idAllot.IdAllotPoolBase;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StageObjectService;

public class StagePort extends Port {

	public StagePort(String name) {
		super(name);
	}

	static {
		init();
	}

	/**
	 * 初始化地图
	 */
	public static void init() {

	}

	/**
	 * 在Port中安全创建普通地图
	 * @param mapSn
	 */
	public void createCommonSafe(int mapSn) {
		this.addQueue(new PortPulseQueue(mapSn) {
			@Override
			public void execute(Port port) {
				StageManager.inst().createCommon(param.getInt());
			}
		});
	}

	/**
	 * 获取地图对象
	 * @param id
	 * @return
	 */
	public StageObject getStageObject(long id) {
		StageObjectService serv = getService(id);
		if (serv == null)
			return null;

		return serv.getStageObj();
	}

	@Override
	protected IdAllotPoolBase initIdAllotPool() {
		return new IdAllotPool(this);
	}

}
