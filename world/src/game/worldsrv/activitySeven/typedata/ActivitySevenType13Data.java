package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 每日福利
 * @author qizhen
 *
 */
public class ActivitySevenType13Data extends ActivitySevenTypeData{
	private static ActivitySevenType13Data instance = new ActivitySevenType13Data();
	
	public static ActivitySevenType13Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType13Data() {
		super(ActivitySevenTypeKey.Type_1);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
		act.setActIng(progress);
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}
}

