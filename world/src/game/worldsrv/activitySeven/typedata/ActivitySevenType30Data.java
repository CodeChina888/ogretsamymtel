package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

public class ActivitySevenType30Data extends ActivitySevenTypeData {

	private static ActivitySevenType30Data instance = new ActivitySevenType30Data();
	
	public static ActivitySevenType30Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType30Data() {
		super(ActivitySevenTypeKey.Type_30);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
		act.setActIng(act.getActIng() + progress);//累计
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}	

}
