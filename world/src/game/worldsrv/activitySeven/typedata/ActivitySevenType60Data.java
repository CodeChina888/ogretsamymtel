
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * {0}个法宝升至{1}星
 * @author sys
 *
 */
public class ActivitySevenType60Data extends ActivitySevenTypeData{
  private static ActivitySevenType60Data instance = new ActivitySevenType60Data();
  
  public static ActivitySevenType60Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType60Data() {
    super(ActivitySevenTypeKey.Type_60);
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
