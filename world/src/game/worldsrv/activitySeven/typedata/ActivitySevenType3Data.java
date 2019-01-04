package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 通关副本
 * @author qizhen
 *
 */
public class ActivitySevenType3Data extends ActivitySevenTypeData{
	private static ActivitySevenType3Data instance = new ActivitySevenType3Data();
	
	public static ActivitySevenType3Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType3Data() {
		super(ActivitySevenTypeKey.Type_3);
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
