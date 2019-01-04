package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.Param;
import core.support.Time;
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
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ActivityType7 extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_7;	//类型
	private static final ActivityType7 instance = new ActivityType7();
	
	/**
	 * 构造函数
	 */
	private ActivityType7(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType7.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType7.type;
	}
	/**
	 * 解析操作参数
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
			String fundName = rewardJson.getString("fundName");
			paramObj.strParams.add(fundName);
//			String rankTime = rewardJson.getString("ranktime");
//			paramObj.numParams.add(stringTimeToLong(rankTime));
//			String tlvTime = rewardJson.getString("tlvtime");
//			paramObj.numParams.add(stringTimeToLong(tlvTime));
			
			int rankTime = rewardJson.getIntValue("ranktime");
			paramObj.numParams.add((long)rankTime);
			int tlvTime = rewardJson.getIntValue("tlvtime");
			paramObj.numParams.add((long)tlvTime);
			
			
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
	 * 获取玩家的活动数据
	 * @param activity
	 * @param humanObj
	 * @return
	 */
	private Map<Integer,Long> getHumanActivityDataList(ActivityObject activity, HumanObject humanObj) {
		Map<Integer,Long> paramList = new HashMap<>();
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		if(activityData != null){
			//清除数据
			ActivityHumanData data = activityData.get(0);
			if (data != null) {
				long time = Utils.longValue(data.getStrValue());
				if (activity.beginTime > time) {
					humanObj.activityDatas.get(activity.id).clear();
					return paramList;
				}
			}
			for(Map.Entry<Integer, ActivityHumanData> entry:activityData.entrySet()){
				paramList.put(entry.getKey(), entry.getValue().getNumValue());
			}			
		}
		return paramList;
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
		Map<Integer,Long> paramList= getHumanActivityDataList(activity, humanObj);
		boolean showPoint = false;
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		for(Map.Entry<Integer, List<ActivityZoneItemObject>> entry:activity.zoneItems.entrySet()){
			ActivityZoneItemObject zoneItem = entry.getValue().get(0);
			if(zoneItem!=null){
				DActivityParam.Builder dp = DActivityParam.newBuilder();
				dp.addNumParam(entry.getKey());
				ActivityParamObject zoneParam = zoneItem.getParams();
				for(DItem di:zoneParam.itemParams){
					dp.addItems(di);
				}
				Long value = paramList.get(entry.getKey());
				if(value!=null && value.longValue() > 0){
					dp.addNumParam(1);
				}
				else{
					dp.addNumParam(0);
				}
				//转换子项 开始、结束天数
				int beginTime = zoneParam.numParams.get(0).intValue();   //子项开始天数  0点
				int endTime = zoneParam.numParams.get(1).intValue();      //子项结束天数  23点59分结束
				Long bt = activity.beginTime +  (beginTime-1)*Time.DAY;   
				Long et = activity.beginTime +  endTime*Time.DAY  - 1*Time.SEC;  //提前一秒 是当天
		
				Calendar cal = Calendar.getInstance();
				long nowtime = cal.getTimeInMillis();
				if(nowtime < bt.longValue()){
					dp.addNumParam(bt.longValue()-nowtime);
				}
				else{
					dp.addNumParam(0);
					if(nowtime < et.longValue() && (value == null || value.longValue()<1)){
						showPoint = true;
					}
				}
				dp.addNumParam(bt.longValue());
				dp.addNumParam(et.longValue());
				String fundName = zoneParam.strParams.get(0);
				dp.addStrParam(fundName);
				dz.addActivityParams(dp.build());
			}	
		}	
		zoneList.add(dz.build());	
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
		ActivityParamObject params = paramList.get(0);
		if(params==null){
			return false;
		}
		long zone = 0;
		if(params != null){
			if(params.numParams.size()>0){
				zone = params.numParams.get(0);
			}
		}
		Map<Integer,Long> paramLists = getHumanActivityDataList(activity, humanObj);
		Long value = paramLists.get((int)zone);
		if(value!=null && value > 0){
			return false;
		}
		
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get((int)zone);
		if(zoneItems==null){
			return false;
		}
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		if(zoneItem == null){
			return false;
		}
		ActivityParamObject zoneParam = zoneItem.getParams();
		//转换子项 开始、结束天数
		int beginTime = zoneParam.numParams.get(0).intValue();   //子项开始天数  0点
		int endTime = zoneParam.numParams.get(1).intValue();      //子项结束天数  23点59分结束
		Long l_bt = activity.beginTime + (beginTime - 1)*Time.DAY;   
		Long l_et = activity.beginTime +  endTime*Time.DAY - 1*Time.SEC;  //提前一秒 是当天
		
		int bt = getTimeDataValue(l_bt);
		int et = getTimeDataValue(l_et);
		int nt = getTimeDataValue(Port.getTime());
		if (nt < bt || nt > et) {
			return false;
		}
		//数据提交数据库
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		Map<Integer,String> str = new HashMap<>();
		snList.add(0);
		str.put(0, Port.getTime()+"");
		snList.add( (int)zone);
		numValues.put( (int)zone, 1L);
		
		commitHumanActivityData(activity,humanObj,snList,numValues,str);
		
		//奖励
		List<ProduceVo> proList = new ArrayList<>();
		for(DItem item:zoneParam.itemParams){
			ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
			proList.add(pro);
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActLoginSpecial);
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
		if(activity.zoneItems.size()<1){
			return false;
		}
		switch (event) {
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
