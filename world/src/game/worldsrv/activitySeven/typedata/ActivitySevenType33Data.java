package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

public class ActivitySevenType33Data extends ActivitySevenTypeData {

	private static ActivitySevenType33Data instance = new ActivitySevenType33Data();
	
	public static ActivitySevenType33Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType33Data() {
		super(ActivitySevenTypeKey.Type_33);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
		// 保留最大的进度
//		if (act.getActIng() >= progress) {
//			return false;
//		}
//		act.setActIng(progress);
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}	

}
