
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 洞天福地参与{0}次
 * @author sys
 *
 */
public class ActivitySevenType53Data extends ActivitySevenTypeData {

  private static ActivitySevenType53Data instance = new ActivitySevenType53Data();
  
  public static ActivitySevenType53Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType53Data() {
    super(ActivitySevenTypeKey.Type_53);
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
