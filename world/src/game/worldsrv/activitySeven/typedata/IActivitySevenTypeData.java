package game.worldsrv.activitySeven.typedata;

import game.worldsrv.character.HumanObject;

public interface IActivitySevenTypeData {
	
	public boolean doProgress(HumanObject humanObj, int type, int progress);
	/**
	 * 提交处理
	 * @param humanObj
	 * @param actId
	 * @return
	 */
	public boolean disposeCommit(HumanObject humanObj, int actId);
}
