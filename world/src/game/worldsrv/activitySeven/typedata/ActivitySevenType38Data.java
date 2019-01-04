
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * {0}个技能修炼至{1}重 - 指定个数技能全部最低修炼至指定等级

 * @author sys
 *
 */
public class ActivitySevenType38Data extends ActivitySevenTypeData{
  private static ActivitySevenType38Data instance = new ActivitySevenType38Data();
  
  public static ActivitySevenType38Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType38Data() {
    super(ActivitySevenTypeKey.Type_38);
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
