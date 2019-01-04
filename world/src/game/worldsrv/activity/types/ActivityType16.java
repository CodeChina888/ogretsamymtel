package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.msg.Define.DItem;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DProduce;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ActivityType16 extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_16;	//类型
	private static final ActivityType16 instance = new ActivityType16();//实例	
	
	private ActivityType16() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType16.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType16.type;
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
		JSONArray awards = json.getJSONArray("awards");
		for(int i=0;i<awards.size();i++){
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			JSONArray items = rewardJson.getJSONArray("items");
			for(int j=0;j<items.size();j++){
				JSONArray item = items.getJSONArray(j);
				int itemSn = item.getIntValue(0);
				int itemCount = item.getIntValue(1);
				DItem.Builder ditem = DItem.newBuilder();
				ditem.setItemSn(itemSn);
				ditem.setNum(itemCount);
				paramObj.itemParams.add(ditem.build());
			}
			zoneItemObj.addParam(paramObj);	
			zoneItems.add(zoneItemObj);
		}
		return zoneItems;
	}
	/**
	 * 获取给客户端的参数
	 * 备注：返回两个DActivityParam
	 * 第一个是昨天选的如果昨天没有选addNumParam返回0
	 * 
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		if(activity.zoneItems.size()<1){
			return null;
		}
		Map<Integer, ActivityHumanData> activityData= activityData(humanObj, activity);
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		int sid = getDayBetween(activity.beginTime, Port.getTime(), HOUR_ZERO) + 1;
		// 昨天预定的(如果玩家昨天没有预定的话返回0，如果有的话返回正常的SID)
		ActivityHumanData yetData = activityData.get(sid - 1);
		boolean showPoint = false;
		DActivityParam.Builder dp = DActivityParam.newBuilder();
		if (yetData == null) {
			dp.addNumParam(0l);//[0]
		}else{
			List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(yetData.getSn());
			if (zoneItems == null) {
				dp.addNumParam(0);//[0]
			}else{
				dp.addNumParam(yetData.getSn());
				Long index = yetData.getNumValue();
				dp.addNumParam(index);//[1]
				DItem ditem= zoneItems.get(0).getParams().itemParams.get(index.intValue());
				dp.addItems(ditem);
				int canGet = 0;
				if (!yetData.getStrValue().equals("")) {
					canGet = 1;
				}
				dp.addNumParam(canGet);//[2]
				if (canGet == 0) {
					showPoint = true;
				}
			}
		}
		dz.addActivityParams(dp.build());
		// 今天可以领取的
		Param param = new Param();
		DActivityParam.Builder dp2 = DActivityParam.newBuilder();
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(sid);
		if(zoneItems != null){
			ActivityHumanData nowData = activityData.get(sid);
			dp2.addNumParam(sid);
			List<DItem> itemList = zoneItems.get(0).params.get(0).itemParams;
			dp2.addAllItems(itemList);
			if (nowData != null) {
				dp2.addNumParam(nowData.getNumValue());
			}else{
				dp2.addNumParam(-1);
				showPoint = true;
			}
		}else{
			dp2.addNumParam(0l);
		}
		dz.addActivityParams(dp2.build());
		zoneList.add(dz.build());
		param.put("showPoint", showPoint);
		return param;
		
	}
	public Map<Integer, ActivityHumanData> activityData(HumanObject humanObj,ActivityObject activity){
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		if (activityData == null) {
			activityData = new HashMap<>();
		}
		ActivityHumanData data = activityData.get(0);
		if (data != null) {
			Long time = Utils.longValue(data.getStrValue());
			if (time < activity.beginTime) {
				activityData = new HashMap<>();
				humanObj.activityDatas.get(activity.id).clear();
			}
		}
		return activityData;
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
		long zone = 0L;
		if(params != null){
			if(params.numParams.size()>0){
				zone = params.numParams.get(0);
			}
		}
		Map<Integer, ActivityHumanData> activityData= activityData(humanObj, activity);
		// 如果zone为0l 选择
		if (zone == 0L) {
			Long index = params.numParams.get(1);
			int sid = getDayBetween(activity.beginTime, Port.getTime(), HOUR_ZERO) + 1;
			// 判断是不是二次选择
			ActivityHumanData data = activityData.get(sid);
			if(data != null){
				return false;
			}
			
			// 存储
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			snList.add(sid);
			numValues.put(sid, index);
			commitHumanActivityData(activity,humanObj,snList,numValues);
		}else{// 领取
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, -1);
			int sid = getDayBetween(activity.beginTime, c.getTimeInMillis(), HOUR_ZERO) + 1;
			
			ActivityHumanData data = activityData.get(sid);
			// 数据异常或者 已经领取过了
			if (data == null || (!data.getStrValue().endsWith("") && Integer.valueOf(data.getStrValue()) == 1)) {
				return false;
			}
			Long index = data.getNumValue();
			if (data.getStrValue().equals("1")) {
				return false;
			}
			
			// 获取这个活动的Aid配置
			List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(sid);
			if(zoneItems==null){
				return false;
			}
			ActivityZoneItemObject zoneItem = zoneItems.get(0);
			if(zoneItem == null){ 
				return false;
			}
			DItem item = zoneItem.getParams().itemParams.get(index.intValue());
			ItemChange itemChange = RewardHelper.reward(humanObj, item.getItemSn(),item.getNum(), LogSysModType.ActHalfPrice);
			ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
			// 存储
			Map<Integer, String> strValues = new HashMap<Integer, String>();
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			strValues.put(sid, 1+"");
			snList.add(sid);
			numValues.put(sid, index);
			strValues.put(0, Port.getTime()+"");
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
		}
		return true;
	}
	
	/**
	 * 监听事件触发
	 * 	每日首次登录，0点为刷新时间。
			可以选择明日要的礼物时。客户端实现
			有可领取的奖励未领取时。客户端实现
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		switch (event) {
		case EventKey.HumanLoginFinishFirstToday:
		case EventKey.ResetDailyHour: {
			if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
				return true;
		}	break;
		default:
			break;
			}
		return false;
	}
	public String getStringToListDProduce(List<DProduce> list){
		if(list.size() == 0){
			return "";
		}
		String result = "";
		for(DProduce dp : list){
			result += (dp.getSn()+ ","+dp.getNum()+","); 
		}
		return result.substring(0, result.length() - 1);
	}
	
}
