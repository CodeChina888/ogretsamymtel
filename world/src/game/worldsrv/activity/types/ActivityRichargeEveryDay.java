package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfRewards;
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
/**
 *每日单笔充值
 * @author qizheng
 *
 */
public class ActivityRichargeEveryDay extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_21;	//类型
	private static final ActivityRichargeEveryDay instance = new ActivityRichargeEveryDay();	//实例
	
	private ActivityRichargeEveryDay() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityRichargeEveryDay.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityRichargeEveryDay.type;
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
			long needTopUp = rewardJson.getLongValue("needTopUp");//需要购买
			paramObj.numParams.add(needTopUp);
			long count = rewardJson.getLongValue("count");//可领取次数
			paramObj.numParams.add(count);
			
			/*JSONArray items = rewardJson.getJSONArray("items");// 给的物品
			for(int j=0;j<items.size();j++){
				JSONArray item = items.getJSONArray(j);
				int itemSn = item.getIntValue(0);
				int itemCount = item.getIntValue(1);
				DItem.Builder ditem = DItem.newBuilder();
				ditem.setItemSn(itemSn);
				ditem.setNum(itemCount);
				paramObj.itemParams.add(ditem.build());
			}*/
			
			int confRewardSn = rewardJson.getIntValue("rewardId");//奖励表对应的sn
			ConfRewards confRewards = ConfRewards.get(confRewardSn);
			for (int j = 0; j < confRewards.itemSn.length; j++) {
				int itemSn = confRewards.itemSn[j];
				int itemCount = confRewards.itemNum[j];
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
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		if(activity.zoneItems.size()<1){
			return null;
		}
		boolean showPoint = false;//显示小红点
		Map<Integer,Long> paramList = getHumanActivityDataList(activity,humanObj);
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		Long gold = paramList.get(0);// 玩家已经充值了多少
		if (gold == null) {
			gold = 0l;
		}
		DActivityParam.Builder dps = DActivityParam.newBuilder();
		dps.addNumParam(gold);
		dz.addActivityParams(dps.build());
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry : activity.zoneItems.entrySet()) {
			ActivityZoneItemObject zoneItem = entry.getValue().get(0);
			if(zoneItem == null){
				continue;
			}
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			dp.addNumParam(entry.getKey());//编号
			ActivityParamObject zoneParam = zoneItem.getParams();
			for(DItem di:zoneParam.itemParams){
				dp.addItems(di);//奖励物品
			}
			Long value = paramList.get(entry.getKey());
			if(value != null && value > 0){
				dp.addNumParam(1);//已经领取
			}else{ 
				dp.addNumParam(0);//未领取
			}
			Long num = zoneParam.numParams.get(0);//需要充值的元宝数
			if(num != null){
				dp.addNumParam(num);
			}else{
				dp.addNumParam(-1);
			}
			Long count = zoneParam.numParams.get(1);//可领取的次数
			if(num != null){
				dp.addNumParam(count);
			}else{
				dp.addNumParam(0);
			}
			Long getCount = paramList.get(entry.getKey()+100);//已领取次数
			if (getCount != null){
				dp.addNumParam(getCount);
			}else{
				dp.addNumParam(0);
			}
			
			// 未领取且充值的金额大于配置
			if (dp.getNumParam(1) == 0 && gold != null && gold >= num) {
				showPoint = true;
			}
			dz.addActivityParams(dp.build());
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
		long newTime = Port.getTime();
		if (activity.beginTime > newTime  || newTime > activity.endTime) {
			return false;
		}
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
		
		//记录在玩家身上的数据
		Map<Integer,Long> paramLists = getHumanActivityDataList(activity,humanObj);
		if (zone <= 0) {
			return false;
		}
		//已领过
		Long value = paramLists.get((int)zone);
		if(value != null && value> 0){
			return false;
		}
		// 充值都没充值过
		Long gold = paramLists.get(0);
		if (gold == null) {
			return false;
		}
		// 获取这个活动的Aid配置
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get((int)zone);
		if(zoneItems==null){
			return false;
		}
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		if(zoneItem == null){
			return false;
		}
		//没达到配置的需求
		ActivityParamObject zoneParam = zoneItem.params.get(0);
		Long numParam = zoneParam.numParams.get(0);
		
		if (numParam == null || gold < numParam) {
			return false;
		}		

		Long count = paramLists.get((int)zone+100);
		if (count == null) {
			count = 0L;
		}
		Long countParam = zoneParam.numParams.get(1);
		if (countParam == null || count >= countParam) {//领奖次数超过配置
			return false;
		}
		
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		snList.add((int)zone);// 设置档位
		numValues.put((int)zone, 1L);// 

		snList.add((int)zone+100);// 领取次数
		numValues.put((int)zone+100, count + 1);//
		
		commitHumanActivityData(activity,humanObj,snList,numValues);
		//奖励
		List<ProduceVo> proList = new ArrayList<>();
		for(DItem item:zoneParam.itemParams){
			ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
			proList.add(pro);
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActForeverGrowUpFund);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return true;
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
				Long time = Utils.longValue(data.getStrValue());
				//int nowDateValue = getNowDateValue(HOUR_ZERO);
				if (activity.beginTime > time || !isSameDay(Port.getTime(),time,HOUR_ZERO)) {
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
	 * 监听事件触发
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		switch (event) {
		case EventKey.HumanLoginFinishFirstToday:
		case EventKey.HumanLoginFinish:
			return true;
		case EventKey.ResetDailyHour: 
			if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
				return true;
			break;
		case EventKey.PayNotify:
			HumanObject humanObj = param.get("humanObj");
			long gold = param.get("gold");
			// 获取活动内 玩家充值的金额
			Map<Integer,Long> paramList = getHumanActivityDataList(activity,humanObj);
			Long value = paramList.get(0);// 获取充值金额
			if (value != null && gold <= value) {
				gold = value;
			}			
			
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			Map<Integer,String> strValues = new HashMap<>();
			for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry : activity.zoneItems.entrySet()) {
				ActivityZoneItemObject zoneItem = entry.getValue().get(0);
				ActivityParamObject zoneParam = zoneItem.getParams();
				Long numParam = zoneParam.numParams.get(0);//需要的充值额度
				Long countParam = zoneParam.numParams.get(1);//可以领取的次数
				int zone  = entry.getKey();
				Long isGet = paramList.get(zone);
				Long count = paramList.get(zone+100);//已领取次数
				if(countParam > 1 && isGet != null && isGet > 0){//如果配置的可以领取的次数是多次，且当时状态为已领取
					
					if(gold > numParam && count != null && count < countParam){
						snList.add((int)zone);// 设置档位
						numValues.put((int)zone, 0L);//重新设置为可领状态
					}
				}
			}			
			
			snList.add(0);
			numValues.put(0, gold);
			//int nowDateValue = getNowDateValue(HOUR_ZERO); //当天时间
			//strValues.put(0, nowDateValue + "");
			strValues.put(0, Port.getTime() + "");
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
			return true;
//			break;
		default:
			break;
		}
		return false;
	}
}
