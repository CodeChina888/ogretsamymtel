
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * {0}个法宝升至{1}级
 * @author sys
 *
 */
public class ActivitySevenType58Data extends ActivitySevenTypeData{
  private static ActivitySevenType58Data instance = new ActivitySevenType58Data();
  
  public static ActivitySevenType58Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType58Data() {
    super(ActivitySevenTypeKey.Type_58);
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
