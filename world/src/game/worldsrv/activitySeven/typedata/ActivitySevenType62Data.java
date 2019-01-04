
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 灵域之争占领{0}星仙域{1}次  - 灵域之争玩法占领指定星级仙府达到累积指定次数

 * @author sys
 *
 */
public class ActivitySevenType62Data extends ActivitySevenTypeData{
  private static ActivitySevenType62Data instance = new ActivitySevenType62Data();
  
  public static ActivitySevenType62Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType62Data() {
    super(ActivitySevenTypeKey.Type_62);
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
