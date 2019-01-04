
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 将魂商店购买{0}次
 * @author sys
 *
 */
public class ActivitySevenType44Data extends ActivitySevenTypeData {

  private static ActivitySevenType44Data instance = new ActivitySevenType44Data();
  
  public static ActivitySevenType44Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType44Data() {
    super(ActivitySevenTypeKey.Type_44);
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
