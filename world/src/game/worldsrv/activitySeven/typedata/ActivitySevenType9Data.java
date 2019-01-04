package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
import game.worldsrv.support.Log;

/**
 * 竞技场
 * @author qizhen
 *
 */
public class ActivitySevenType9Data extends ActivitySevenTypeData{
	private static ActivitySevenType9Data instance = new ActivitySevenType9Data();
	
	public static ActivitySevenType9Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType9Data() {
		super(ActivitySevenTypeKey.Type_9);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			Log.game.info("humanObj.humanActivitySeven == null type={}",type);
			return false;
		}
		// 保留最大的进度
//		if (act.getActIng() >= progress) {
//			return false;
//		}
		act.setActIng(progress);
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}	
}
