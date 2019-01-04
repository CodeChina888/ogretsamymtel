
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 获得{0}个{1}品质的命格
 * @author sys
 *
 */
public class ActivitySevenType56Data extends ActivitySevenTypeData{
  private static ActivitySevenType56Data instance = new ActivitySevenType56Data();
  
  public static ActivitySevenType56Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType56Data() {
    super(ActivitySevenTypeKey.Type_56);
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
