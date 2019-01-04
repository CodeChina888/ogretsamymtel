package crosssrv.seam.id;

import core.Port;
import core.support.Config;
import core.support.idAllot.IdAllotPoolBase;
import game.worldsrv.support.C;

/**
 * 可分配ID池 此类并非线程安全的
 */
public class CrossIdAllotPool extends IdAllotPoolBase {

	public CrossIdAllotPool(Port port) {
		super(port, C.GAME_PLATFORM_ID, Config.CROSS_SERVER_ID, 500, 250);// 20倍，相当于10000，5000
	}

	public CrossIdAllotPool(Port port, int applyNum, int warnNum) {
		super(port, C.GAME_PLATFORM_ID, Config.CROSS_SERVER_ID, applyNum, warnNum);
	}
}
