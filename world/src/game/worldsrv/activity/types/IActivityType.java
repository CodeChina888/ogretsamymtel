package game.worldsrv.activity.types;

import java.util.List;

import core.support.Param;
import game.worldsrv.character.HumanObject;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;

public interface IActivityType {
	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr);
	
	/**
	 * 获取给客户端的参数
	 * @param activity
	 * @return
	 */
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList);
	
	/**
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList);
		
	/**
	 * 监听事件触发(需要在ActivityManager中增加human监听,或是在ActivityService中增加全局监听)
	 * @param event
	 * @param param
	 */
	public boolean onTrigger(int event, ActivityObject activity, Param param);
	
	/**
	 * 周期判定
	 * @param nowtime
	 * @param activity
	 * @return
	 */
	public boolean isPeriod(long nowtime, ActivityObject activity);
}
