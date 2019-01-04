package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 技能升级
 * @author qizhen
 *
 */
public class ActivitySevenType37Data extends ActivitySevenTypeData {

	private static ActivitySevenType37Data instance = new ActivitySevenType37Data();
	
	public static ActivitySevenType37Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType37Data() {
		super(ActivitySevenTypeKey.Type_37);
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
