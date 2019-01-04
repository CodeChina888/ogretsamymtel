package game.seam.id;

import game.worldsrv.support.C;

import core.Port;
import core.support.idAllot.IdAllotPoolBase;

/**
 * 可分配ID池 此类并非线程安全的
 */
public class IdAllotPool extends IdAllotPoolBase {

	public IdAllotPool(Port port) {
		super(port, C.GAME_PLATFORM_ID, C.GAME_SERVER_ID);
	}

	public IdAllotPool(Port port, int applyNum, int warnNum) {
		super(port, C.GAME_PLATFORM_ID, C.GAME_SERVER_ID, applyNum, warnNum);
	}
}
