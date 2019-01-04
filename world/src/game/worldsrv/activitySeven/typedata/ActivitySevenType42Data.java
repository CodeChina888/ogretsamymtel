
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * {0}只神兽渡劫至{1}星 - 指定个数灵兽全部最低渡劫至指定星级

 * @author sys
 *
 */
public class ActivitySevenType42Data extends ActivitySevenTypeData{
  private static ActivitySevenType42Data instance = new ActivitySevenType42Data();
  
  public static ActivitySevenType42Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType42Data() {
    super(ActivitySevenTypeKey.Type_42);
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
