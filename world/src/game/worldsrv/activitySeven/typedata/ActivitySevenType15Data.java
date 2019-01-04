package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 神器升级
 * @author qizhen
 *
 */
public class ActivitySevenType15Data extends ActivitySevenTypeData{
	private static ActivitySevenType15Data instance = new ActivitySevenType15Data();
	
	public static ActivitySevenType15Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType15Data() {
		super(ActivitySevenTypeKey.Type_15);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
//		act.setActIng(progress);
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}
}
