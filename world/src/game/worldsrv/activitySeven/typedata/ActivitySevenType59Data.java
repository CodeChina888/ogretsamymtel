
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;

/**
 * {0}个法宝突破至{1}阶 - 指定个数法宝全部最低突破至指定阶数

 * @author sys
 *
 */
public class ActivitySevenType59Data extends ActivitySevenTypeData{
  private static ActivitySevenType59Data instance = new ActivitySevenType59Data();
  
  public static ActivitySevenType59Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType59Data() {
    super(ActivitySevenTypeKey.Type_59);
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
