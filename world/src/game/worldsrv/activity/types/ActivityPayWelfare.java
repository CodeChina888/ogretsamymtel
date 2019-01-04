package game.worldsrv.activity.types;

import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.msg.Define.EMoneyType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityPayWelfare;
import game.worldsrv.config.ConfActivityPeopleWelfare;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;
//充值福利
public class ActivityPayWelfare extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_32; // 类型
	private static final ActivityPayWelfare instance = new ActivityPayWelfare();

	private static final String jsonKey_TotalDay = "day";//累计天数 {"day":"{\"1\":384}"} Map{key(sn),value(day)}
	private static final String jsonKey_TotalTodayPay = "pay";//今日充值 {"day":"{\"20171111\":384}"}
	private static final String jsonKey_AlreadyGet = "get";//领取状况{"AlreadyGet":"1,2,3"}
	
	/**
	 * 
	 */
	private static final Integer persistSn = 1;
	private static final Integer clientSn = 0;
	
	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static ActivityTypeBase getInstance() {
		return ActivityPayWelfare.instance;
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	public int getType() {
		return ActivityPayWelfare.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * 
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr) {
		Collection<ConfActivityPayWelfare> confAll = ConfActivityPayWelfare.findAll();
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		for (ConfActivityPayWelfare conf : confAll) {
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(conf.sn);
			ActivityParamObject paramObj = new ActivityParamObject();
			paramObj.numParams.add((long) conf.type);
			paramObj.numParams.add((long) conf.needNum);
			paramObj.numParams.add((long) conf.needDay);
			paramObj.numParams.add((long) conf.formerPrice);
			paramObj.numParams.add((long) conf.nowPrice);
			paramObj.numParams.add((long) conf.needReset);
			int confRewardSn = conf.rewardSn;
			ConfRewards confRewards = ConfRewards.get(confRewardSn);
			if(confRewards != null){
				List<DItem> itemList = new ArrayList<>();
				for (int j = 0; j < confRewards.itemSn.length; j++) {
					int itemSn = confRewards.itemSn[j];
					int itemCount = confRewards.itemNum[j];
					DItem.Builder ditem = DItem.newBuilder();
					ditem.setItemSn(itemSn);
					ditem.setNum(itemCount);
					itemList.add(ditem.build());
				}
				paramObj.itemParams.addAll(itemList);
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
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		DActivityParam.Builder dps = DActivityParam.newBuilder();
		
		Map<Integer, ActivityHumanData> activityHumanDataMap = getHumanActivityDataList(humanObj, activity);
		ActivityHumanData data = activityHumanDataMap.get(persistSn);
		JSONObject jo = Utils.toJSONObject(data.getStrValue());
		Map<Integer,Integer> dayTotalPay = Utils.jsonToMapIntInt(jo.getString(jsonKey_TotalTodayPay));
		int totalPay = 0;//今日充值累计金额
		int day = getNowDateValue(ParamManager.dailyHourReset);
		if(dayTotalPay.containsKey(day)){
			totalPay = dayTotalPay.get(day);
		}
		//客户端抬头值 ActivityPayWelfare.needNum
		int clientValue = 0;
		List<ActivityZoneItemObject> clientList = activity.zoneItems.get(clientSn);
		if(clientList != null){
			clientValue = clientList.get(0).params.get(0).numParams.get(1).intValue();
		}
		dps.addNumParam(totalPay);
		dps.addNumParam(clientValue);
		dz.addActivityParams(dps.build());
		
		List<Long> alreadyGetAid = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
		Map<Integer,Integer> totalDayGet = Utils.jsonToMapIntInt(jo.getString(jsonKey_TotalDay));
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry : activity.zoneItems.entrySet()) {
			if(entry.getKey() == 0){
				continue;
			}
			ActivityZoneItemObject zoneItem = entry.getValue().get(0);
			if(zoneItem == null){
				continue;
			}
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			Integer aid = entry.getKey();
			
			ActivityParamObject zoneParam = zoneItem.getParams();
			for(DItem di:zoneParam.itemParams){
				dp.addItems(di);//奖励物品
			}
			dp.addNumParam(aid);
			dp.addNumParam(zoneParam.numParams.get(0));//类型;
			long needTotalPay = zoneParam.numParams.get(1);
			dp.addNumParam(needTotalPay);//需要充值;
			Integer plan = totalDayGet.get(aid);
			if(plan == null){
				plan = 0;
			}
			dp.addNumParam(plan);
			long needTotalDays = zoneParam.numParams.get(2);
			dp.addNumParam(needTotalDays);//需要连续充值天数;
			dp.addNumParam(zoneParam.numParams.get(3));//原价
			dp.addNumParam(zoneParam.numParams.get(4));//现价
			long status = EAwardType.AwardNot_VALUE;
			if(!alreadyGetAid.contains((long)aid)){
				if(needTotalDays == 0){
					if(needTotalPay <= totalPay){
						status = EAwardType.Awarding_VALUE;
						showPoint = true;
					}
				} else {
					if(needTotalDays <= plan){
						status = EAwardType.Awarding_VALUE;
						showPoint = true;
					}
				}
			}else{
				status = EAwardType.Awarded_VALUE;
			}
			dp.addNumParam(status);
			dz.addActivityParams(dp.build());
		}
		zoneList.add(dz.build());	
		Param param = new Param();
		param.put("showPoint", showPoint);
		return param;
	}
	
	/**
	 * 处理客户端的执行请求 
	 * 备注：已是否需要累计充值天数为主 分为 不需要充值天数 和 需要天数
	 * 需要天数在充值触发时进行处理 如果满足在增加天数
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		//来自客户端的参数
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
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get((int)zone);
		if(zoneItems==null){
			return false;
		}
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		if(zoneItem == null){
			return false;
		}
		ActivityParamObject zoneParam = zoneItem.getParams();
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		ActivityHumanData data = activityHumanDataMap.get(persistSn);
		JSONObject jo = Utils.toJSONObject(data.getStrValue());
		List<Long> already_Get = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
		if(already_Get.contains(zone)){
			return false;
		}
		
		//需要充值天数
		long needTotalDays = zoneParam.numParams.get(2);
		if(needTotalDays != 0){
			Map<Integer,Integer> dayTotalDay = Utils.jsonToMapIntInt(jo.getString(jsonKey_TotalDay));
			Integer totalDay = dayTotalDay.get((int)zone);
			if(totalDay == null || totalDay < needTotalDays){
				return false;
			}
		}else{
			Map<Integer,Integer> dayTotalPay = Utils.jsonToMapIntInt(jo.getString(jsonKey_TotalTodayPay));
			long needTotalPay = zoneParam.numParams.get(1);
			int totalPay = 0;
			int day = getNowDateValue(ParamManager.dailyHourReset); 
			if(dayTotalPay.containsKey(day)){
				totalPay = dayTotalPay.get(day);
			}
			if(needTotalPay > totalPay){
				return false;
			}
			long type = zoneParam.numParams.get(0);
			if(type == 2){//需要购买
				Long nowPrice = zoneParam.numParams.get(4);
				// 扣元宝
				if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, nowPrice.intValue(), LogSysModType.ActPayWelfare)) {
					return false;
				}
			} 
		}
		already_Get.add(zone);
		jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(already_Get));
		data.setStrValue(jo.toJSONString());
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
		ActivityHumanData data = activityHumanDataMap.get(persistSn);
		boolean isUpdateToDay = true;
		if(data.getNumValue() < activity.beginTime && activity.beginTime != 0l){
			JSONObject json = new JSONObject();
			data.setStrValue(json.toJSONString());
			isUpdateToDay = false;
		}
		if(isUpdateToDay){
			//重置处理 
			JSONObject jo = Utils.toJSONObject(data.getStrValue());
			List<Long> already_Get = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
			Map<Integer,Integer> dayTotalDay = Utils.jsonToMapIntInt(jo.getString(jsonKey_TotalDay));
			Map<Integer,Integer> dayTotalDayPay = Utils.jsonToMapIntInt(jo.getString(jsonKey_TotalTodayPay));
			
			int totalPay = 0;//今日充值累计金额
			int day = getNowDateValue(ParamManager.dailyHourReset);
			if(dayTotalDayPay.containsKey(day)){
				totalPay = dayTotalDayPay.get(day);
			}
			boolean reset = false;
			for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry : activity.zoneItems.entrySet()) {
				ActivityZoneItemObject zoneItem = entry.getValue().get(0);
				if(zoneItem == null){
					continue;
				}
				ActivityParamObject zoneParam = zoneItem.getParams();
				long needReset = zoneParam.numParams.get(5);
				if(needReset != 1){//不需要清除
					continue;
				}
				if(!already_Get.contains(entry.getKey().longValue())){
					continue;
				}
				//如果是同一天完成的任务 不进行重置进度
				if(totalPay != 0){
					continue;
				}
				reset = true;
				already_Get.remove(entry.getKey().longValue());
				if(dayTotalDay.containsKey(entry.getKey().intValue())){
					dayTotalDay.put(entry.getKey(), 0);
				}
			}
			if(reset){
				jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(already_Get));
				jo.put(jsonKey_TotalDay, Utils.mapIntIntToJSON(dayTotalDay));
				data.setStrValue(jo.toJSONString());
			}
		}
		return activityHumanDataMap;
	}
	
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		if(activity.zoneItems.size() < 1){
			return false;
		}
		switch (event) {
			case EventKey.HumanLoginFinishFirstToday:
			case EventKey.HumanLoginFinish:
			case EventKey.ActResetDailyHour:
				return true;
			case EventKey.PayNotify:
				HumanObject humanObj = param.get("humanObj");
				Map<Integer, ActivityHumanData> activityHumanDataMap = getHumanActivityDataList(humanObj, activity);
				ActivityHumanData humanData = activityHumanDataMap.get(persistSn);
				JSONObject jo = Utils.toJSONObject(humanData.getStrValue());
				Map<Integer,Integer> dayTotalPay = Utils.jsonToMapIntInt(jo.getString(jsonKey_TotalTodayPay));
				Map<Integer,Integer> dayTotalDay = Utils.jsonToMapIntInt(jo.getString(jsonKey_TotalDay));
				int oldTotalPay = 0;//今日充值累计金额
				int day = getNowDateValue(ParamManager.dailyHourReset); 
				if(dayTotalPay.containsKey(day)){
					oldTotalPay = dayTotalPay.get(day);
				}
				long gold = param.get("gold");
				long totalPay = oldTotalPay + gold;
				dayTotalPay.put(day, (int) totalPay);
				jo.put(jsonKey_TotalTodayPay, Utils.mapIntIntToJSON(dayTotalPay));
				
				for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry : activity.zoneItems.entrySet()) {
					ActivityZoneItemObject zoneItem = entry.getValue().get(0);
					if(zoneItem == null){
						continue;
					}
					ActivityParamObject zoneParam = zoneItem.getParams();
					long needTotalDays = zoneParam.numParams.get(2);
					if(needTotalDays <= 0){//天数进度
						continue;
					}
					long needTotalPay = zoneParam.numParams.get(1);
					if(oldTotalPay < needTotalPay && totalPay >= needTotalPay){
						Integer totalDay = dayTotalDay.get(entry.getKey());
						if(totalDay == null){
							totalDay = 0;
						}
						dayTotalDay.put(entry.getKey(), totalDay + 1);
					}
				}
				
				jo.put(jsonKey_TotalDay, Utils.mapIntIntToJSON(dayTotalDay));
				humanData.setStrValue(jo.toJSONString());
				humanData.setNumValue(Port.getTime());
				return true;
		}
		return false;
	}
}
