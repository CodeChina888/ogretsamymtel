package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 每日单笔充值{0}元宝
 * @author qizhen
 *
 */
public class ActivitySevenType5Data extends ActivitySevenTypeData{
	private static ActivitySevenType5Data instance = new ActivitySevenType5Data();
	
	public static ActivitySevenType5Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType5Data() {
		super(ActivitySevenTypeKey.Type_5);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		ActivitySeven act = humanObj.humanActivitySeven.get(type);
		if (act == null) {
			return false;
		}
		act.setActIng(progress);//累计充值
		return this.checkProgressStatus(humanObj, act);
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}	
}
