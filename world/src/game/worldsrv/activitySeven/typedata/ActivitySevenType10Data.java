package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 远征
 * @author qizhen
 *
 */
public class ActivitySevenType10Data extends ActivitySevenTypeData{
	private static ActivitySevenType10Data instance = new ActivitySevenType10Data();
	
	public static ActivitySevenType10Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType10Data() {
		super(ActivitySevenTypeKey.Type_10);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
		// 保留最大的进度
		if (act.getActIng() >= progress) {
			return false;
		}
		act.setActIng(progress);
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}
	
}

