
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * {0}件装备精练至{1}星 - 指定件数装备全部最低精炼至指定星级

 * @author sys
 *
 */
public class ActivitySevenType40Data extends ActivitySevenTypeData{
  private static ActivitySevenType40Data instance = new ActivitySevenType40Data();
  
  public static ActivitySevenType40Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType40Data() {
    super(ActivitySevenTypeKey.Type_40);
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
