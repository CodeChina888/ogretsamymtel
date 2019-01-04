
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 竞技场商店购买{0}次 - 竞技场商店累积购买次数

 * @author sys
 *
 */
public class ActivitySevenType45Data extends ActivitySevenTypeData {

  private static ActivitySevenType45Data instance = new ActivitySevenType45Data();
  
  public static ActivitySevenType45Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType45Data() {
    super(ActivitySevenTypeKey.Type_45);
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
