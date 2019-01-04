
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 解锁护法位置{0}个
 * @author sys
 *
 */
public class ActivitySevenType47Data extends ActivitySevenTypeData {

  private static ActivitySevenType47Data instance = new ActivitySevenType47Data();
  
  public static ActivitySevenType47Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType47Data() {
    super(ActivitySevenTypeKey.Type_47);
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
