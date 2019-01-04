package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityPeopleWelfare;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityGlobal;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.msg.Define.EMoneyType;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityServiceProxy;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 成长基金
 * @author songy
 *
 */
public class ActivityType5 extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_5;	//类型
	private static final ActivityType5 instance = new ActivityType5();
	
	
	private static final int fundSn = 1;	//基金相关的存在这
	private static final int welfareSn = 2; //全民福利存在这
	
	private static final String jsonKey_AlreadyGet = "AlreadyGet";
	
	
	
	
	

	
//	public static int getActivityGlobalCount(){
//		return activityGlobal.getFundBuyCount();
//	}
	/**
	 * 构造函数
	 */
	private ActivityType5(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType5.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType5.type;
	}
	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		// 字符串转为json
		JSONObject json = Utils.toJSONObject(paramStr);
		if (json == null) {
			return zoneItems;
		}
		// 基本信息
		ActivityZoneItemObject zio = new ActivityZoneItemObject(0);
		ActivityParamObject activityParamObject = new ActivityParamObject();
		JSONArray awards = json.getJSONArray("awards");// 列表
		long fundPrice = json.getLong("fundPrice");// 购买价格
		activityParamObject.numParams.add(fundPrice);
		long vipLimit = json.getLong("vipLimit");// 购买需要的VIP级别
		activityParamObject.numParams.add(vipLimit);
		long total = json.getLong("total");// 最后返值的总数
		activityParamObject.numParams.add(total);
		zio.addParam(activityParamObject);
		zoneItems.add(zio);

		// 奖励 信息
		for (int i = 0; i < awards.size(); i++) {
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");// 编号
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			long minTLv = rewardJson.getLong("minTLv");// 最低领取级别
			paramObj.numParams.add(minTLv);

			int confRewardSn = rewardJson.getIntValue("rewardId");// 奖励表对应的sn
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
		
		Collection<ConfActivityPeopleWelfare> confAll = ConfActivityPeopleWelfare.findAll();
		for (ConfActivityPeopleWelfare conf : confAll) {
			int aid = conf.sn;
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			long needTopUp = conf.needTopUp;

			int confRewardSn = conf.rewardSn;
			ConfRewards confRewards = ConfRewards.get(confRewardSn);
			List<DItem> itemList = new ArrayList<>();
			for (int j = 0; j < confRewards.itemSn.length; j++) {
				int itemSn = confRewards.itemSn[j];
				int itemCount = confRewards.itemNum[j];
				DItem.Builder ditem = DItem.newBuilder();
				ditem.setItemSn(itemSn);
				ditem.setNum(itemCount);
				itemList.add(ditem.build());
			}
			paramObj.numParams.add(needTopUp);
			paramObj.itemParams.addAll(itemList);
			zoneItemObj.addParam(paramObj);
			zoneItems.add(zoneItemObj);
		}
		return zoneItems;
	}
	/**
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getaAtivityGlobal();
		Param result = prx.waitForResult();
		ActivityGlobal activityGlobal = result.get("activityGlobal");
		if(activityGlobal == null){
			Log.activity.error("没有 activityGlobal 数据");
			return false;
		}
		
		//来自客户端的参数
		ActivityParamObject params = paramList.get(0);
		if(params==null){
			return false;
		}
		long zone = 0;//等于0为购买基金，小于0为一键领取，大于0为领取对应的奖励
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
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		if(zone < 100){//购买与领取基金奖励
			if(zone != 0){ // 领取基金奖励
				if(activityHumanDataMap == null || !activityHumanDataMap.containsKey(fundSn)){
					return false;
				}
				ActivityHumanData humanData = activityHumanDataMap.get(fundSn);
				JSONObject jo = Utils.toJSONObject(humanData.getStrValue());
				List<Long> alreadyGetAid_List = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
				if(alreadyGetAid_List.contains(zone) ){
					Log.activity.info("已经领取过该奖励");
					return false;
				}
				alreadyGetAid_List.add(zone);
				jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(alreadyGetAid_List));
				
				humanData.setStrValue(jo.toJSONString());
				//奖励
				List<ProduceVo> proList = new ArrayList<>();
				ActivityParamObject zoneParam = zoneItem.getParams();
				for(DItem item:zoneParam.itemParams){
					ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
					proList.add(pro);
				}
				ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActForeverGrowUpFund);
				ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
			}else{// 购买基金
				//默认有fundSn就算购买过了
				if(activityHumanDataMap != null && activityHumanDataMap.containsKey(fundSn)){//已经购买过了
					return false;
				}
				ActivityParamObject zoneParam = zoneItem.params.get(0);
				Long diamond = zoneParam.numParams.get(0);
				Long vip = zoneParam.numParams.get(1);
				if(humanObj.getHuman().getVipLevel() < vip.intValue()){//VIP级别 
					return false;
				}
				// 扣元宝
				if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, diamond.intValue(), LogSysModType.ActForeverGrowUpFund)) {
					return false;
				}
				
				//数据提交数据库
				List<Integer> snList = new ArrayList<>();//索引列表
				Map<Integer,Long> numValues = new HashMap<>();//数值参数表
				Map<Integer,String> strValues = new HashMap<>();//数值参数表
				snList.add(fundSn);
				numValues.put(fundSn, (long)Port.getTime());
				JSONObject json = new JSONObject();
				strValues.put(fundSn, json.toJSONString());
				
				commitHumanActivityData(activity,humanObj,snList,numValues);
				
				prx.setAtivityGlobalNum(activityGlobal.getFundBuyCount() + 1);
				return true;
			}
		} else {//领取全名福利
			JSONObject jo = new JSONObject();
			List<Long> alreadyGetAid_List =  new ArrayList<Long>();
			if(activityHumanDataMap != null){
				ActivityHumanData humanData = activityHumanDataMap.get(welfareSn);
				if(humanData != null){
					jo = Utils.toJSONObject(humanData.getStrValue());
					alreadyGetAid_List = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
					if(alreadyGetAid_List.contains(zone)){
						return false;
					}
				}
			}
			
			ActivityParamObject zoneParam = zoneItem.params.get(0);
			Long numParam = zoneParam.numParams.get(0);//改项所需金额
			if(activityGlobal.getFundBuyCount() < numParam){
				Log.activity.info("购买人数不足，无法领取奖励");
				return false;
			}
			alreadyGetAid_List.add(zone);
			jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(alreadyGetAid_List));
			//数据提交数据库
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			Map<Integer,String> strValues = new HashMap<>();//数值参数表
			snList.add(welfareSn);
			numValues.put(welfareSn, (long)Port.getTime());
			strValues.put(welfareSn, jo.toJSONString());
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
			//奖励
			List<ProduceVo> proList = new ArrayList<>();
			for(DItem item : zoneParam.itemParams){
				ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
				proList.add(pro);
			}
			ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActLoginNumSendGold);
			ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
			return true;
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
		if(activity.zoneItems.size() < 1){
			return null;
		}
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getaAtivityGlobal();
		Param result = prx.waitForResult();//TODO:
		ActivityGlobal activityGlobal = result.get("activityGlobal");
		
		//记录在玩家身上的数据
		boolean showPoint = false;
		boolean buy = false;
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		ActivityHumanData fundData = null;
		ActivityHumanData welfareData = null;
		List<Long> fundGetAid_List =  new ArrayList<Long>();
		List<Long> welfareGetAid_List =  new ArrayList<Long>();
		if(activityHumanDataMap != null ){
			JSONObject jo = new JSONObject();
			if(activityHumanDataMap.containsKey(fundSn)){
				fundData = activityHumanDataMap.get(fundSn);
				jo = Utils.toJSONObject(fundData.getStrValue());
				fundGetAid_List = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
			}
			if(activityHumanDataMap.containsKey(welfareSn)){
				welfareData = activityHumanDataMap.get(welfareSn);
				jo = Utils.toJSONObject(welfareData.getStrValue());
				welfareGetAid_List = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
			}
		}
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(0);
		DActivityZoneItem.Builder dz0 = DActivityZoneItem.newBuilder();
		dz0.setZone(1);
		DActivityZoneItem.Builder dz1 = DActivityZoneItem.newBuilder();
		dz1.setZone(2);
		for(Map.Entry<Integer, List<ActivityZoneItemObject>> entry:activity.zoneItems.entrySet()){
			ActivityZoneItemObject zoneItem = entry.getValue().get(0);
			if(zoneItem == null) continue;
			if(entry.getKey() == 0){//购买状态
				DActivityParam.Builder dp = DActivityParam.newBuilder();
				ActivityParamObject zoneParam = zoneItem.getParams();			
				if(fundData != null){
					dp.addNumParam(1);
					buy = true;
				} else {
					dp.addNumParam(0);
				}
				Long diamond = zoneParam.numParams.get(0);//需求的购买钻石
				Long vip = zoneParam.numParams.get(1);//需求的VIP级别要求
				long total = zoneParam.numParams.get(2);//最终返值
				if(vip != null){
					dp.addNumParam(vip);
					dp.addNumParam(diamond);
				}
				else{
					dp.addNumParam(0);
					dp.addNumParam(0);
				}
				dp.addNumParam(total);
//				if(activityGlobal == null){
//					loadActivityGlobal();
//				}
				dp.addNumParam(activityGlobal.getFundBuyCount());//有多少人购买
				System.out.println("有多少人购买"+activityGlobal.getFundBuyCount());
				dz.addActivityParams(dp.build());
				continue;
			}
			if(entry.getKey() < 100){// 基金领取 状况
				DActivityParam.Builder dp = DActivityParam.newBuilder();
				
				dp.addNumParam(entry.getKey());
				ActivityParamObject zoneParam = zoneItem.params.get(0);
				for(DItem di:zoneParam.itemParams){
					dp.addItems(di);
				}
				Long lv = zoneParam.numParams.get(0);//领取的级别要求
				long status = 0;//未领取
				if(fundGetAid_List.contains(entry.getKey().longValue())){
					status = 1;//已领取
				}else{
					if(lv!=null && humanObj.getHuman().getLevel()>=lv.intValue()){
						showPoint = true;
					}
				}
				dp.addNumParam(status);
				if(lv !=null){
					dp.addNumParam(lv);
				}
				else{
					dp.addNumParam(0);				
				}
				dz0.addActivityParams(dp.build());
			}else{
				DActivityParam.Builder dp = DActivityParam.newBuilder();
				dp.addNumParam(entry.getKey());
				ActivityParamObject zoneParam = zoneItem.params.get(0);
				for(DItem di:zoneParam.itemParams){
					dp.addItems(di);
				}
				long status = EAwardType.AwardNot_VALUE;
				Long num = zoneParam.numParams.get(0);//需求购买人数
				if(welfareGetAid_List.contains(entry.getKey().longValue())){
					status = EAwardType.Awarded_VALUE;
				}else{
					if(num!=null && activityGlobal != null && activityGlobal.getFundBuyCount()>=num){
						status = EAwardType.Awarding_VALUE;
						showPoint = true;
					}
				}
				dp.addNumParam(status);
				if(num !=null){
					dp.addNumParam(num);
				}
				else{
					dp.addNumParam(0);				
				}
				dz1.addActivityParams(dp.build());
			}
		}
		zoneList.add(dz.build());
		zoneList.add(dz0.build());
		zoneList.add(dz1.build());
		Param param = new Param();
		param.put("showPoint", buy && showPoint);
		return param;
	}
	public static boolean isComplete(HumanObject humanObj,ActivityObject activity) {
		//总进度
		int totle = activity.zoneItems.size() - 1;
		Map<Integer, ActivityHumanData> activityHumanDataMap= humanObj.activityDatas.get(activity.id);
		if(activityHumanDataMap == null) {
			return false;
		}
		if(!activityHumanDataMap.containsKey(fundSn) || !activityHumanDataMap.containsKey(welfareSn)){
			return false;
		}
		JSONObject jo = Utils.toJSONObject(activityHumanDataMap.get(fundSn).getStrValue());
		int fundSize = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet)).size();
		jo = Utils.toJSONObject(activityHumanDataMap.get(welfareSn).getStrValue());
		int welfareSize =  Utils.strToLongList(jo.getString(jsonKey_AlreadyGet)).size();
		if(fundSize + welfareSize >= totle) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		if(activity.zoneItems.size() < 1){
			return false;
		}
		switch (event) {
			case EventKey.HumanLoginFinishFirstToday:
			case EventKey.HumanLoginFinish:
				return true;
			case EventKey.ResetDailyHour:
				if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset){
					return true;
				}
				break;
			case EventKey.HumanLvUp:
				HumanObject humanObj = param.get("humanObj");
				int lvOld = param.get("lvOld");
				Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
				if(activityHumanDataMap == null || !activityHumanDataMap.containsKey(fundSn)){
					return false;
				}
				for(int i = 1; i<activity.zoneItems.size(); i++){
					if(activity.zoneItems == null || activity.zoneItems.get(i) == null) {
						continue;
					}
					ActivityZoneItemObject zoneItem = activity.zoneItems.get(i).get(0);
					ActivityParamObject zoneParam = zoneItem.getParams();
					Long lv = zoneParam.numParams.get(0);//领取的级别要求
					if(lv != null && lvOld <= lv && lv <= humanObj.getHuman().getLevel()){
						return true;
					}
				}
				return false;
			default:
				break;
		}
		return false;
	}
}
