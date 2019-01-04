
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 竞技场挑战{0}次 - 竞技场累积挑战指定次数

 * @author sys
 *
 */
public class ActivitySevenType54Data extends ActivitySevenTypeData {

  private static ActivitySevenType54Data instance = new ActivitySevenType54Data();
  
  public static ActivitySevenType54Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType54Data() {
    super(ActivitySevenTypeKey.Type_54);
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
