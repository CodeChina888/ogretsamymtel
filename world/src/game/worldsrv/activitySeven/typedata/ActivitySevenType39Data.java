
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 技能修炼{0}次
 * @author sys
 *
 */
public class ActivitySevenType39Data extends ActivitySevenTypeData {

  private static ActivitySevenType39Data instance = new ActivitySevenType39Data();
  
  public static ActivitySevenType39Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType39Data() {
    super(ActivitySevenTypeKey.Type_39);
  }

  @Override
  public boolean doProgress(HumanObject humanObj, int type, int progress) {
    ActivitySeven act = humanObj.humanActivitySeven.get(type);
    if (act == null) {
      return false;
    }
    act.setActIng(act.getActIng());//累计
    return this.checkProgressStatus(humanObj, act);
  }
  
  public boolean disposeCommit(HumanObject humanObj, int actId){
    return this.actDisposeCommit(humanObj, actId);
  } 

}
