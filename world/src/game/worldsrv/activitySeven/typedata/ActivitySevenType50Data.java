
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 封印之地元宝复活{0}次
 * @author sys
 *
 */
public class ActivitySevenType50Data extends ActivitySevenTypeData {

  private static ActivitySevenType50Data instance = new ActivitySevenType50Data();
  
  public static ActivitySevenType50Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType50Data() {
    super(ActivitySevenTypeKey.Type_50);
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
