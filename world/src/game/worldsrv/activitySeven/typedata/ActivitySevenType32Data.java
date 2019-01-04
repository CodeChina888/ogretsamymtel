package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

public class ActivitySevenType32Data extends ActivitySevenTypeData {

	private static ActivitySevenType32Data instance = new ActivitySevenType32Data();
	
	public static ActivitySevenType32Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType32Data() {
		super(ActivitySevenTypeKey.Type_32);
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
