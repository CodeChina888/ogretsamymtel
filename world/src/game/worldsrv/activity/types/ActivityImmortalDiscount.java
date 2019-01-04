package game.worldsrv.activity.types;

import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.EDrawOperation;
import game.msg.Define.EDrawType;
import game.worldsrv.activity.ActivityInfo;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityImmortalDiscount;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;

/**
 * 折扣寻仙
 * @author Administrator
 *
 */
public class ActivityImmortalDiscount extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_31;	//类型
	private static final ActivityImmortalDiscount instance = new ActivityImmortalDiscount();	//实例
	
	private static final String jsonKey_PlanNum = "Num";//领取状况
	//持久化 sn =1
	private static final Integer persistSn = 1;
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityImmortalDiscount.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityImmortalDiscount.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(persistSn);
		ActivityParamObject paramObj = new ActivityParamObject();
		paramObj.numParams.add((long) ConfActivityImmortalDiscount.get(1).param);//折扣率
		paramObj.numParams.add((long) ConfActivityImmortalDiscount.get(2).param);//活动次数上限
		zoneItemObj.addParam(paramObj);
		zoneItems.add(zoneItemObj);
		return zoneItems;
	}
	
	/***
	 * 是否已经达到上限了
	 * @param humanObj
	 * @return
	 */
	public static boolean isUpLimit(HumanObject humanObj){
		ActivityObject activity = ActivityInfo.act31;
		if(activity == null){
			return false;
		}
		if(!activity.isValid()){
			return false;
		}
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		if(activityHumanDataMap == null) {
			return true;
		}
		ActivityHumanData data = activityHumanDataMap.get(persistSn);
		if(data == null) {
			return true;
		}
		JSONObject jo = Utils.toJSONObject(data.getStrValue());
		Integer planNum = jo.getInteger(jsonKey_PlanNum);
		//活动已经重新开始了 防止旧数据未请除
		if(data.getNumValue() < activity.beginTime && activity.beginTime != 0l){
			planNum = 0;
		}
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(persistSn);
		if(zoneItems == null){
			return false;
		}
		List<Long> numParams = zoneItems.get(0).params.get(0).numParams;
		if(planNum != null && planNum >= numParams.get(1)){
			return false;
		}
		return true;
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
		
		Map<Integer, ActivityHumanData> activityHumanDataMap = getHumanActivityDataList(humanObj, activity);
		ActivityHumanData data = activityHumanDataMap.get(persistSn);
		JSONObject jo = Utils.toJSONObject(data.getStrValue());
		Integer planNum = jo.getInteger(jsonKey_PlanNum);
		if(planNum == null) {
			planNum = 0;
		}
		//加入一个特殊的
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(persistSn);
		if(zoneItems == null){
			return null;
		}
		List<Long> numParams = zoneItems.get(0).params.get(0).numParams;
		DActivityParam.Builder dps = DActivityParam.newBuilder();
		dps.addAllNumParam(numParams);//折扣 活动上限
		dps.addNumParam(planNum);
		dz.addActivityParams(dps.build());
		zoneList.add(dz.build());	
		Param param = new Param();
		param.put("showPoint", true);
		return param;
	}
	/**
	 * 获取玩家的活动数据
	 * @param activity
	 * @param humanObj
	 * @return
	 */
	private Map<Integer, ActivityHumanData> getHumanActivityDataList(HumanObject humanObj,ActivityObject activity) {
		if(!humanObj.activityDatas.containsKey(activity.id)){
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			Map<Integer,String> strValues = new HashMap<>();
			
			snList.add(persistSn);
			numValues.put(persistSn, Port.getTime());
			JSONObject json = new JSONObject();
			strValues.put(persistSn, json.toJSONString());
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
		}
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		ActivityHumanData  data = activityHumanDataMap.get(persistSn);
		if(data.getNumValue() < activity.beginTime && activity.beginTime != 0l){
			JSONObject json = new JSONObject();
			data.setStrValue(json.toJSONString());
		}
		data.setNumValue(Port.getTime());
		return activityHumanDataMap;
	}
	/**
	 * 监听事件触发
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		switch (event) {
		case EventKey.HumanLoginFinishFirstToday:
		case EventKey.HumanLoginFinish:
		case EventKey.ActResetDailyHour:
			return true;
		case EventKey.DrawCard_GoldTen:
			boolean isCostOther = param.getBoolean("isCostOther");
			if(isCostOther){//若扣除代币 不进入进度计算
				return false;
			}
			HumanObject humanObj = param.get("humanObj");
			
			Map<Integer, ActivityHumanData> activityHumanDataMap = getHumanActivityDataList(humanObj, activity);
			ActivityHumanData data = activityHumanDataMap.get(persistSn);
			JSONObject jo = Utils.toJSONObject(data.getStrValue());
			List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(persistSn);
			if(zoneItems == null){
				return false;
			}
			List<Long> numParams = zoneItems.get(0).params.get(0).numParams;
			Integer num = jo.getInteger(jsonKey_PlanNum);
			if(num == null){
				num = 0;
			}
			if(num >= numParams.get(1)){
				return false;
			}
			if(jo.size() <= 0){
				jo.put(jsonKey_PlanNum, 1);
			}else{
				jo.put(jsonKey_PlanNum, 1 + num);
			}
			data.setStrValue(jo.toJSONString());
			return true;
		default:
			break;
		}
		return false;
	}
}
