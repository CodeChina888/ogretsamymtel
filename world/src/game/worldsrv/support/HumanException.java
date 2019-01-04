package game.worldsrv.support;

import game.worldsrv.support.Utils;
import game.worldsrv.inform.InformManager;

/**
 * 当程序出现可预知错误，并希望用户看到出错信息的时候抛出此异常
 */
public class HumanException extends RuntimeException {
	private static final long serialVersionUID = 1;

	public HumanException(long humanId, String msg, Object... params) {
		super(Utils.createStr(msg, params));

		InformManager.inst().ErrorInform(humanId, Utils.createStr(msg, params));
	}

	public HumanException(Throwable e, long humanId, String msg, Object... params) {
		super(Utils.createStr(msg, params), e);

		InformManager.inst().ErrorInform(humanId, Utils.createStr(msg, params));
	}
}