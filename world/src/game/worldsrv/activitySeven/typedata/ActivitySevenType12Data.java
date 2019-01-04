package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 高级研制
 * @author qizhen
 *
 */
public class ActivitySevenType12Data extends ActivitySevenTypeData{
	private static ActivitySevenType12Data instance = new ActivitySevenType12Data();
	
	public static ActivitySevenType12Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType12Data() {
		super(ActivitySevenTypeKey.Type_12);
	}

	/**
	 * 是否可以进行
	 */
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
	/**
	 * 是否达成
	 */
	public boolean disposeCommit(HumanObject humanObj, int actId){
		return this.actDisposeCommit(humanObj, actId);
	}
	
}
