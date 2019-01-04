package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 装备强化
 * @author qizhen
 *
 */
public class ActivitySevenType4Data extends ActivitySevenTypeData{
	private static ActivitySevenType4Data instance = new ActivitySevenType4Data();
	
	public static ActivitySevenType4Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType4Data() {
		super(ActivitySevenTypeKey.Type_4);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}
	
}

