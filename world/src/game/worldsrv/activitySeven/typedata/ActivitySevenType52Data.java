
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 炼妖塔赛季积分达到{0}
 * @author sys
 *
 */
public class ActivitySevenType52Data extends ActivitySevenTypeData {

  private static ActivitySevenType52Data instance = new ActivitySevenType52Data();
  
  public static ActivitySevenType52Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType52Data() {
    super(ActivitySevenTypeKey.Type_52);
  }

  @Override
  public boolean doProgress(HumanObject humanObj, int type, int progress) {
    ActivitySeven act = humanObj.humanActivitySeven.get(type);
    if (act == null) {
      return false;
    }
    act.setActIng(progress);//累计
    return this.checkProgressStatus(humanObj, act);
  }
  
  public boolean disposeCommit(HumanObject humanObj, int actId){
    return this.actDisposeCommit(humanObj, actId);
  } 

}
