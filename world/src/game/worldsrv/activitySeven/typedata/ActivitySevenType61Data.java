
package game.worldsrv.activitySeven.typedata;

import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivitySeven;
/**
 * 灵域之争累积占领{0}小时 - 灵域之争玩法任意仙域累积占领指定小时（2个仙府分开计算）

 * @author sys
 *
 */
public class ActivitySevenType61Data extends ActivitySevenTypeData {

  private static ActivitySevenType61Data instance = new ActivitySevenType61Data();
  
  public static ActivitySevenType61Data getInstance(){
    return instance;
  }
  
  protected ActivitySevenType61Data() {
    super(ActivitySevenTypeKey.Type_61);
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
