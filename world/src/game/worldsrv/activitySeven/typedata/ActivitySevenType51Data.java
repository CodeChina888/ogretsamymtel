
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * 炼妖塔通关{0}难度第{1}关 - 完成指定难度和指定层数的炼妖塔战斗并胜利

 * @author sys
 *
 */
public class ActivitySevenType51Data extends ActivitySevenTypeData{
  private static ActivitySevenType51Data instance = new ActivitySevenType51Data();
  
  public static ActivitySevenType51Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType51Data() {
    super(ActivitySevenTypeKey.Type_51);
  }

  @Override
  public boolean doProgress(HumanObject humanObj, int type, int progress) {
    ActivitySeven act = humanObj.humanActivitySeven.get(type);
    if (act == null) {
      return false;
    }
    act.setActIng(progress);
    return this.checkProgressStatus(humanObj, act);
  }
  
  public boolean disposeCommit(HumanObject humanObj, int actId){
    return this.actDisposeCommit(humanObj, actId);
  }
}
