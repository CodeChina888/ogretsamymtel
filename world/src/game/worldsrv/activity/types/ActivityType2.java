package game.worldsrv.activity.types;

import java.util.ArrayList;
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
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ActivityType2 extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_2;	//7日签到
	private static final ActivityType2 instance = new ActivityType2();
	
	/**
	 * 构造函数
	 */
	private ActivityType2(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType2.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType2.type;
	}
	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		
		//字串转为json对象
		JSONObject json = Utils.toJSONObject(paramStr);
		if(json == null){
			return zoneItems;
		}
		
		
		JSONArray awards = json.getJSONArray("awards");//列表
		Map<Integer,Map<Integer,DItem>> itemTable = new HashMap<>();
		for(int i=0;i<awards.size();i++){
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");//第几个奖励
			JSONArray items = rewardJson.getJSONArray("items");//奖励物品
			for(int j=0;j<items.size();j++){//每个奖励是一个周期
				JSONArray item = items.getJSONArray(j);
				int itemSn = item.getIntValue(0);
				int itemCount = item.getIntValue(1);
				DItem.Builder ditem = DItem.newBuilder();
				ditem.setItemSn(itemSn);
				ditem.setNum(itemCount);
				Map<Integer,DItem> itemList = itemTable.get(j);
				if(itemList == null){
					itemList = new HashMap<>();
					itemTable.put(j, itemList);
				}
				itemList.put(aid-1, ditem.build());
			}
		}
		//记录
		for(int i=0;i<itemTable.size();i++){
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(i+1);
			ActivityParamObject paramObj = new ActivityParamObject();
			Map<Integer,DItem> itemList = itemTable.get(i);
			for(int j=0;j<itemList.size();j++){
				paramObj.itemParams.add(itemList.get(j));
			}
			zoneItemObj.addParam(paramObj);			
			zoneItems.add(zoneItemObj);
		}
		return zoneItems;
	}
	/**
	 * 根据顺序取得列表
	 * @return
	 */
	private ActivityZoneItemObject getConfActivityObjectList(ActivityObject activity, int signCycle){
		if(signCycle == 0){
			signCycle = 1;
		}		
		List<ActivityZoneItemObject> zoneItemList = activity.zoneItems.get((int)Math.min(activity.zoneItems.size(),signCycle));
		if(zoneItemList == null){
			return null;
		}
		return zoneItemList.get(0);
	}
	/**
	 * 获取给客户端的参数
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		if(activity.zoneItems.size()<1){
			return null;
		}
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		long signInDay = 0;//最后一次签到日期
		long signInTimes = 0;//签到次数
		long signCycle = 1;//当前第几组
		
		//记录的数据
		if(activityData != null){
			ActivityHumanData obj = activityData.get(0);
			if(obj!=null){
				signInDay = obj.getNumValue();
			}
			obj = activityData.get(1);
			if(obj!=null){
				signInTimes = obj.getNumValue();
			}
			obj = activityData.get(2);
			if(obj!=null){
				signCycle = obj.getNumValue();
			}
		}
		//应加入重置的处理
//		if(signInDay < getActivityBeginDateValue(activity)){
//			signInDay = 0;
//			signInTimes = 0;
//			signCycle = 1;
//		}
		// 取得活动参数组列表
		ActivityZoneItemObject zoneItem = getConfActivityObjectList(activity, (int)signCycle);
		if(zoneItem == null){
			return null;
		}
		if(zoneItem.params.size() < 1){
			return null;
		}
		boolean showPoint = false;
		int value = getNowDateValue(HOUR_ZERO);
		if(zoneItem.params.get(0).itemParams.size() <= signInTimes && value > signInDay){
			//本组签到完毕，换下一组
			signCycle = Math.min(activity.zoneItems.size(), signCycle + 1);
			zoneItem = getConfActivityObjectList(activity, (int)signCycle);
			if(zoneItem.params.size() < 1){
				return null;
			}
			signInTimes = 0;
		}
		
		
		DActivityZoneItem.Builder dActivityZoneItem = DActivityZoneItem.newBuilder();
		dActivityZoneItem.setZone(1);
		DActivityParam.Builder dActivityParam = DActivityParam.newBuilder();
		//本组奖励列表
		for(DItem dItem:zoneItem.params.get(0).itemParams){
			dActivityParam.addItems(dItem);
		}
		dActivityParam.addNumParam(signInTimes);//领取次数
		if(value == signInDay){//今日是否已经领取完毕
			dActivityParam.addNumParam(1);
		}
		else{
			dActivityParam.addNumParam(0);
			showPoint = true;
		}
		dActivityZoneItem.addActivityParams(dActivityParam.build());
		zoneList.add(dActivityZoneItem.build());
		
		Param param = new Param();
		param.put("showPoint", showPoint);
		return param;
	}
	/**
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		long signInDay = 0;//签到的最后日期
		long signInTimes = 0;//签到的次数
		long signCycle = 1;//当前第几组
		
		//记录的数据
		if(activityHumanDataMap != null){
			ActivityHumanData obj = activityHumanDataMap.get(0);
			if(obj!=null){
				signInDay = obj.getNumValue();
			}
			obj = activityHumanDataMap.get(1);
			if(obj!=null){
				signInTimes = obj.getNumValue();
			}
			obj = activityHumanDataMap.get(2);
			if(obj!=null){
				signCycle = obj.getNumValue();
			}
		}
		int nowDateValue = getNowDateValue(HOUR_ZERO); //当天时间
		if(nowDateValue == signInDay){//今日已经领取
			return false;
		}
		//应加入重置的处理
//		if(signInDay < getActivityBeginDateValue(activity)){
//			signInDay = 0;
//			signInTimes = 0;
//			signCycle = 1;
//		}
		// 获取活动参数数组
		ActivityZoneItemObject zoneItem = getConfActivityObjectList(activity, (int)signCycle);
		if(zoneItem == null){
			return false;
		}
		if(zoneItem.params.size() < 1){
			return false;
		}
		if(zoneItem.getParams().itemParams.size() <= signInTimes && nowDateValue > signInDay){
			//签到完毕，换下一组
			signCycle = Math.min(activity.zoneItems.size(), signCycle + 1);
			zoneItem = getConfActivityObjectList(activity, (int)signCycle);
			if(zoneItem.params.size()<1){
				return false;
			}
			signInTimes = 0L;
		}
		signInTimes = signInTimes + 1;
		
		//数据提交数据库
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		snList.add(0);
		numValues.put(0, (long)nowDateValue);
		snList.add(1);
		numValues.put(1, signInTimes);
		snList.add(2);
		numValues.put(2, signCycle);
		commitHumanActivityData(activity,humanObj,snList,numValues);//提交玩家活动数据
			
		//奖励
		DItem item = zoneItem.params.get(0).itemParams.get((int)signInTimes-1);
		ItemChange itemChange = RewardHelper.reward(humanObj, item.getItemSn(),item.getNum(), LogSysModType.ActSevenDaysSignIn);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return true;
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
