
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * {0}个命格最低升至{1}级
 * @author sys
 *
 */
public class ActivitySevenType57Data extends ActivitySevenTypeData{
  private static ActivitySevenType57Data instance = new ActivitySevenType57Data();
  
  public static ActivitySevenType57Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType57Data() {
    super(ActivitySevenTypeKey.Type_57);
  }

  @Override
  public boolean doProgress(HumanObject humanObj, int type, int progress) {
    ActivitySeven act = humanObj.humanActivitySeven.get(type);
    if (act == null) {
      return false;
    }
//    act.setActIng(progress);
    return this.checkProgressStatus(humanObj, act);
  }
  
  public boolean disposeCommit(HumanObject humanObj, int actId){
    return this.actDisposeCommit(humanObj, actId);
  }
}
