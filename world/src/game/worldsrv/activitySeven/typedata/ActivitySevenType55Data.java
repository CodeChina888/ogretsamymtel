
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 命格占卜{0}次
 * @author sys
 *
 */
public class ActivitySevenType55Data extends ActivitySevenTypeData {

  private static ActivitySevenType55Data instance = new ActivitySevenType55Data();
  
  public static ActivitySevenType55Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType55Data() {
    super(ActivitySevenTypeKey.Type_55);
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
