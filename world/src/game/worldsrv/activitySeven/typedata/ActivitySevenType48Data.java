
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 元宝抽卡{0}次
 * @author sys
 *
 */
public class ActivitySevenType48Data extends ActivitySevenTypeData {

  private static ActivitySevenType48Data instance = new ActivitySevenType48Data();
  
  public static ActivitySevenType48Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType48Data() {
    super(ActivitySevenTypeKey.Type_48);
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
