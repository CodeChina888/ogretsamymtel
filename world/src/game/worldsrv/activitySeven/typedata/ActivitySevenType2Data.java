package game.worldsrv.activitySeven.typedata;

import java.util.List;

import core.support.Utils;
import game.worldsrv.activitySeven.ActivitySevenManager;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 半价出售
 * @author qizhen
 *
 */
public class ActivitySevenType2Data extends ActivitySevenTypeData{
	private static ActivitySevenType2Data instance = new ActivitySevenType2Data();
	
	public static ActivitySevenType2Data getInstance(){
		return instance;
	}
	
	protected ActivitySevenType2Data() {
		super(ActivitySevenTypeKey.Type_2);
	}

	@Override
	public boolean doProgress(HumanObject humanObj, int type, int progress) {
		return false;
	}
	
	public boolean disposeCommit(HumanObject humanObj, int actId){
		ActivitySeven act = humanObj.humanActivitySeven.get(ActivitySevenTypeKey.Type_2);
		List<Integer> idList = Utils.strToIntList(act.getActId());
		List<Integer> statusList = Utils.strToIntList(act.getActStatus());
		boolean result = false;
		for (int i = 0; i < idList.size(); i++) {
			if (idList.get(i) == actId) {
				int status = statusList.get(i);
				if (status == ActivitySevenManager.Status_YetGet) {
					return false;
				}
				statusList.set(i, ActivitySevenManager.Status_YetGet);
				result = true;
			}
		}
		if (!result) {
			return false;
		}else{
			act.setActStatus(Utils.ListIntegerToStr(statusList));
		}
		return true;
	}
}