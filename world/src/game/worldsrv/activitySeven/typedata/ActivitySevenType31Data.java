package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

public class ActivitySevenType31Data extends ActivitySevenTypeData {

	private static ActivitySevenType31Data instance = new ActivitySevenType31Data();
	
	public static ActivitySevenType31Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType31Data() {
		super(ActivitySevenTypeKey.Type_31);
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
