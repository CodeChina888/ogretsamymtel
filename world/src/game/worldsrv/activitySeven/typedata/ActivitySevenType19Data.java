package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 协力作战次数
 * @author qizhen
 *
 */
public class ActivitySevenType19Data extends ActivitySevenTypeData{
	private static ActivitySevenType19Data instance = new ActivitySevenType19Data();
	
	public static ActivitySevenType19Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType19Data() {
		super(ActivitySevenTypeKey.Type_19);
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
		// 保留最大的进度
		act.setActIng(progress);
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}
}
