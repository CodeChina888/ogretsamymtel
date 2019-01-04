package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.List;

import core.support.Param;
import game.worldsrv.character.HumanObject;
import game.worldsrv.csplatform.CSPlatformManager;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
/**
 * 各渠道的礼包兑换
 * @author qizheng
 *
 */
public class ActivityType9 extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_9;	//类型
	private static final ActivityType9 instance = new ActivityType9();
	
	/**
	 * 构造函数
	 */
	private ActivityType9(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType9.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType9.type;
	}
	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		return zoneItems;
	}
	
	/**
	 * 获取给客户端的参数
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){		
		Param param = new Param();
		param.put("showPoint", false);
		param.put("getNoShow", false);
		return param;
	}
	/**
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		ActivityParamObject params = paramList.get(0);
		if(params==null){
			return false;
		}
		String giftcode = params.strParams.get(0);
		if(giftcode==null||giftcode.length()<1){
			return false;
		}
		CSPlatformManager.inst().checkGiftCode(humanObj, giftcode);
		return true;
	}
	
	/**
	 * 监听事件触发
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		return false;
	}
}
