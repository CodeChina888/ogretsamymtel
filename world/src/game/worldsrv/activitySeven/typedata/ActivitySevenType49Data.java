
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 参与封印之地{0}次
 * @author sys
 *
 */
public class ActivitySevenType49Data extends ActivitySevenTypeData {

  private static ActivitySevenType49Data instance = new ActivitySevenType49Data();
  
  public static ActivitySevenType49Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType49Data() {
    super(ActivitySevenTypeKey.Type_49);
  }

  @Override
  public boolean doProgress(HumanObject humanObj, int type, int progress) {
    ActivitySeven act = humanObj.humanActivitySeven.get(type);
    if (act == null) {
      return false;
    }
    act.setActIng(act.getActIng() + progress);//累计
    return this.checkProgressStatus(humanObj, act);
  }
  
  public boolean disposeCommit(HumanObject humanObj, int actId){
    return this.actDisposeCommit(humanObj, actId);
  } 

}
