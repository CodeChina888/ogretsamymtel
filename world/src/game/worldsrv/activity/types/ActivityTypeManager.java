package game.worldsrv.activity.types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.Param;
import game.worldsrv.character.HumanObject;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.EventKey;

public class ActivityTypeManager {

	Map<Integer,ActivityTypeBase>operateTypes = new HashMap<>();//条件处理管理器
	
	/**
	 * 构造函数
	 */
	public ActivityTypeManager(){
		this.init();
	}
	
	/**
	 * 初始化
	 */
	private void init(){
		operateTypes.put(ActivityTypeBase.getInstance().getType(), ActivityTypeBase.getInstance());
		operateTypes.put(ActivityType1.getInstance().getType(), ActivityType1.getInstance());
		operateTypes.put(ActivityType2.getInstance().getType(), ActivityType2.getInstance());
		operateTypes.put(ActivityLevelPack.getInstance().getType(), ActivityLevelPack.getInstance());
		operateTypes.put(ActivityTypeStrength.getInstance().getType(), ActivityTypeStrength.getInstance());
		operateTypes.put(ActivityType5.getInstance().getType(), ActivityType5.getInstance());
		operateTypes.put(ActivityType6.getInstance().getType(), ActivityType6.getInstance());
		operateTypes.put(ActivityType7.getInstance().getType(), ActivityType7.getInstance());
		operateTypes.put(ActivityType8.getInstance().getType(), ActivityType8.getInstance());
		operateTypes.put(ActivityType9.getInstance().getType(), ActivityType9.getInstance());
		operateTypes.put(ActivityAccumulatedRecharge.getInstance().getType(), ActivityAccumulatedRecharge.getInstance());
		operateTypes.put(ActivityAccumulatedCost.getInstance().getType(), ActivityAccumulatedCost.getInstance());
		operateTypes.put(ActivityType12.getInstance().getType(), ActivityType12.getInstance());
		operateTypes.put(ActivityCornucopia.getInstance().getType(), ActivityCornucopia.getInstance());
		operateTypes.put(ActivityType14.getInstance().getType(), ActivityType14.getInstance());
		operateTypes.put(ActivityType15.getInstance().getType(), ActivityType15.getInstance());
		operateTypes.put(ActivityType16.getInstance().getType(), ActivityType16.getInstance());
		operateTypes.put(ActivityType17.getInstance().getType(), ActivityType17.getInstance());
		operateTypes.put(ActivityOnlinePacks.getInstance().getType(), ActivityOnlinePacks.getInstance());
		operateTypes.put(ActivitySingleRecharge.getInstance().getType(), ActivitySingleRecharge.getInstance());
		operateTypes.put(ActivitySingleRechargeEveryday.getInstance().getType(), ActivitySingleRechargeEveryday.getInstance());
		operateTypes.put(ActivityType22.getInstance().getType(), ActivityType22.getInstance());
		operateTypes.put(ActivityLoginWelfare.getInstance().getType(), ActivityLoginWelfare.getInstance());
		operateTypes.put(ActivityTreasure.getInstance().getType(), ActivityTreasure.getInstance());
		operateTypes.put(ActivitySeekImmortal.getInstance().getType(), ActivitySeekImmortal.getInstance());
		operateTypes.put(ActivityLoginNumSendGold.getInstance().getType(), ActivityLoginNumSendGold.getInstance());
		operateTypes.put(ActivitySevenDaysEnjoy.getInstance().getType(), ActivitySevenDaysEnjoy.getInstance());
		operateTypes.put(ActivityServerGiftBag.getInstance().getType(), ActivityServerGiftBag.getInstance());
		operateTypes.put(ActivityPayWelfare.getInstance().getType(), ActivityPayWelfare.getInstance());
		operateTypes.put(ActivityImmortalDiscount.getInstance().getType(), ActivityImmortalDiscount.getInstance());
		operateTypes.put(ActivityServerCompetition.getInstance().getType(), ActivityServerCompetition.getInstance());
		operateTypes.put(ActivityLoginSum.getInstance().getType(), ActivityLoginSum.getInstance());
		operateTypes.put(ActivityTimeLimitExchange.getInstance().getType(), ActivityTimeLimitExchange.getInstance());
	}
	
	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	public void initOperateParam(ActivityObject activity, String paramStr){
		ActivityTypeBase at = operateTypes.get(activity.type);
		if(at == null){
			return;
		}
		try{
			activity.zoneItems.clear();
			List<ActivityZoneItemObject> zoneItems = at.initOperateParam(activity,paramStr);
			activity.addAllZoneItems(zoneItems);
			activity.setInitZoneItemOk(true);
			//ActivityInfo.initToAddInfo(activity);
		}catch (Exception e){
			String str=e.toString();
	        StackTraceElement[] stackElements = e.getStackTrace();
	        if (stackElements != null) {            
	            for (int i = 0; i < stackElements.length; i++) {
	            	str = str + "\n	at " + stackElements[i].getClassName();
	            	str = str + "." + stackElements[i].getMethodName();
	            	str = str + "(" + stackElements[i].getFileName();
	            	str = str + ":" + stackElements[i].getLineNumber()+")";
	            }
	        }
			Log.game.error("activity_type {} initOperateParam: \n{}", activity.type, str);
		}		
	}
	
	/**
	 * 获取给客户端的参数
	 * @param activity
	 * @return
	 */
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		ActivityTypeBase at = operateTypes.get(activity.type);
		if(at==null){
			return null;
		}
		if (!activity.isShowValid()) {
			return null;
		}
		return at.getShowParam(activity, humanObj, zoneList);
	}
	
	/**
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		ActivityTypeBase at = operateTypes.get(activity.type);
		if(at==null){
			return false;
		}
		
		if (!activity.isValid()) {
			return false;
		}
		
		return at.commitActivity(activity, humanObj, paramList);
	}
	
	/**
	 * 事件触发
	 * @param activity
	 * @param param
	 */
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		ActivityTypeBase at = operateTypes.get(activity.type);
		if(at == null){
			return false;
		}
		if (!activity.isValid()) {
			return false;
		}
		if(activity.planTime != 0 && activity.planTime < Port.getTime()){
			if(event == EventKey.HumanLoginFinish){
				return at.onTrigger(event, activity, param);
			}else{
				return false;
			}
		}
 		return at.onTrigger(event, activity, param);
	}

	/**
	 * 周期判断
	 * @param nowtime
	 * @param activity
	 * @return
	 */
	public boolean isPeriod(long nowtime, ActivityObject activity) {
		ActivityTypeBase at = operateTypes.get(activity.type);
		if(at==null){
			return false;
		}
		return at.isPeriod(nowtime, activity);
	}
}
