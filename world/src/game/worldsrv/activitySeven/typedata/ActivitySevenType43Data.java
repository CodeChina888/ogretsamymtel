
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 元宝商店购买{0}次
 * @author sys
 *
 */
public class ActivitySevenType43Data extends ActivitySevenTypeData {

  private static ActivitySevenType43Data instance = new ActivitySevenType43Data();
  
  public static ActivitySevenType43Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType43Data() {
    super(ActivitySevenTypeKey.Type_43);
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
