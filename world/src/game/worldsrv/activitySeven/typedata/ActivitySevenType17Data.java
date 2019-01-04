package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 装备进阶
 * @author qizhen
 *
 */
public class ActivitySevenType17Data extends ActivitySevenTypeData{
	private static ActivitySevenType17Data instance = new ActivitySevenType17Data();
	
	public static ActivitySevenType17Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType17Data() {
		super(ActivitySevenTypeKey.Type_17);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
		// 保留最大的进度
		/*if (act.getActIng() >= progress) {
			return false;
		}
		act.setActIng(progress);*/
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}
}
