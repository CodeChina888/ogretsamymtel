
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 仙玉商店购买{0}次
 * @author sys
 *
 */
public class ActivitySevenType46Data extends ActivitySevenTypeData {

  private static ActivitySevenType46Data instance = new ActivitySevenType46Data();
  
  public static ActivitySevenType46Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType46Data() {
    super(ActivitySevenTypeKey.Type_46);
  }

  @Override
  public boolean doProgress(HumanObject humanObj, int type, int progress) {
    ActivitySeven act = humanObj.humanActivitySeven.get(type);
    if (act == null) {
      return false;
    }
    act.setActIng(act.getActIng() + progress);//累计
    return this.checkProgressStatus(humanObj, act);
  }
  
  public boolean disposeCommit(HumanObject humanObj, int actId){
    return this.actDisposeCommit(humanObj, actId);
  } 

}
