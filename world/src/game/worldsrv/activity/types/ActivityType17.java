package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.List;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.param.ParamManager;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONObject;
/**
 * 限时折扣
 * @author qizheng
 *
 */
public class ActivityType17 extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_17;	//类型
	private static final ActivityType17 instance = new ActivityType17();//实例	
	
	private ActivityType17() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType17.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType17.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		JSONObject json = Utils.toJSONObject(paramStr);
		if(json == null){
			return zoneItems;
		}
		String str = json.getString("client");
		ActivityZoneItemObject zoneItemObj0 = new ActivityZoneItemObject(0);
		ActivityParamObject paramObj0 = new ActivityParamObject();
		paramObj0.strParams.add(str);
		zoneItemObj0.addParam(paramObj0);	
		zoneItems.add(zoneItemObj0);
		return zoneItems;
	}
	
	/**
	 * 获取给客户端的参数
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		List<ActivityZoneItemObject> list = activity.zoneItems.get(0);
		String str = list.get(0).getParams().strParams.get(0);
		DActivityParam.Builder dp = DActivityParam.newBuilder();
		dp.addStrParam(str);//编号
		dz.addActivityParams(dp.build());
		zoneList.add(dz.build());
		Param param = new Param();
		param.put("showPoint", false);
		return param;
	}
	/**
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		return false;
		
	}
	/**
	 * 监听事件触发
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		switch (event) {
		/*case EventKey.HumanLoginFinishFirstToday:
		case EventKey.HumanLoginFinish:
			return true;*/
		case EventKey.ResetDailyHour: {
			if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
				return true;
		}	break;
		default:
			break;
			}
		return false;
	}
}
