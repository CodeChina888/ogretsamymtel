
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 装备精炼{0}次
 * @author sys
 *
 */
public class ActivitySevenType41Data extends ActivitySevenTypeData {

  private static ActivitySevenType41Data instance = new ActivitySevenType41Data();
  
  public static ActivitySevenType41Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType41Data() {
    super(ActivitySevenTypeKey.Type_41);
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
