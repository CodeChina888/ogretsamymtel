package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 协力作战次数
 * @author qizhen
 *
 */
public class ActivitySevenType18Data extends ActivitySevenTypeData{
	private static ActivitySevenType18Data instance = new ActivitySevenType18Data();
	
	public static ActivitySevenType18Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType18Data() {
		super(ActivitySevenTypeKey.Type_18);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
		// 保留最大的进度
		act.setActIng(act.getActIng() + 1);
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}
}
