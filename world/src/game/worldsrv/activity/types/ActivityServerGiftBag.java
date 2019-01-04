package game.worldsrv.activity.types;

import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityLoginNumSendGold;
import game.worldsrv.config.ConfActivityPeopleWelfare;
import game.worldsrv.config.ConfActivityServerGiftBag;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
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

/***
 * 新服红包
 * @author Administrator
 *
 */
public class ActivityServerGiftBag extends ActivityTypeBase {

	private static final int type = ActivityTypeDefine.Activity_TYPE_29; // 类型
	private static final ActivityServerGiftBag instance = new ActivityServerGiftBag();

	private static final String jsonKey_Back = "Back";//返还数量
	private static final String jsonKey_AlreadyGet = "Get";//领取状况
	
	//持久化 sn =1
	private static final Integer persistSn = 1;
	/**
	 * 构造函数
	 */
	private ActivityServerGiftBag() {
	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static ActivityTypeBase getInstance() {
		return ActivityServerGiftBag.instance;
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	public int getType() {
		return ActivityServerGiftBag.type;
	}

	/**
	 * 解析操作参数，即表
	 * 
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr) {
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(persistSn);
		ActivityParamObject paramObj = new ActivityParamObject();
		paramObj.numParams.add((long) ConfActivityServerGiftBag.get(1).param);//返还率
		paramObj.numParams.add((long) ConfActivityServerGiftBag.get(2).param);//每日返回上限
		paramObj.numParams.add((long) ConfActivityServerGiftBag.get(3).param);//每轮天数
		paramObj.numParams.add((long) ConfActivityServerGiftBag.get(4).param);//返回的货币类型
		zoneItemObj.addParam(paramObj);
		zoneItems.add(zoneItemObj);
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
		Map<Integer, ActivityHumanData> activityHumanDataMap = getHumanActivityDataList(humanObj, activity);
		ActivityHumanData data = activityHumanDataMap.get(persistSn);
		JSONObject jo = Utils.toJSONObject(data.getStrValue());
		List<Long> getDays = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
		Map<Integer,Integer> back = Utils.jsonToMapIntInt(jo.getString(jsonKey_Back));
		//加入一个特殊的
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(persistSn);
		if(zoneItems == null){
			return null;
		}
		DActivityParam.Builder dps = DActivityParam.newBuilder();
		List<Long> numParams = zoneItems.get(0).params.get(0).numParams;
		dps.addAllNumParam(numParams);//返还率 每日上限 每轮天数 货币类型
		dz.addActivityParams(dps.build());
		int ringDay = numParams.get(2).intValue();
		//活动进行了几天 - 1
		int days = getDayBetween(activity.beginTime, Port.getTime(), HOUR_ZERO);
		int ring = days / ringDay; //第几轮
		int day = (days % ringDay) + 1;	//这一轮的第几天
		//活动到截止时间
		int dayTime = getDayBetween(activity.beginTime, activity.planTime,HOUR_ZERO) + 1;
		int rewardNum = 0;
		for (int i = 1; i <= dayTime; i++) {
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			dp.addNumParam(i);
			Integer toDayBack = back.get(i);
			if (toDayBack == null) {
				dp.addNumParam(0);
			} else {
				dp.addNumParam(toDayBack);
			}
			// 还需要购买多少钻石
			long status = EAwardType.AwardNot_VALUE;
			if (getDays.contains((long)i)) {
				status = EAwardType.Awarded_VALUE;
			} else {
				if (day == 1) {
					if (((i - 1) / ringDay) == ring - 1 && ring != 0 && toDayBack != null) {
						status = EAwardType.Awarding_VALUE;
						rewardNum++;
					}
				}
			}
//			System.out.println(i +" 状态： "+ status+"钱： "+back.get(i));
			dp.addNumParam(status);
			dz.addActivityParams(dp);
		}
		zoneList.add(dz.build());
		Param param = new Param();
		showPoint = rewardNum>0 ? true:false;
		param.put("showPoint", showPoint);
		return param;
	}
	/**
	 * 获取玩家的活动数据
	 * @param activity
	 * @param humanObj
	 * @return
	 */
	private Map<Integer, ActivityHumanData> getHumanActivityDataList(HumanObject humanObj,ActivityObject activity) {
		/*
		 * 初始化活动
		 */
		if(!humanObj.activityDatas.containsKey(activity.id)){
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			Map<Integer,String> strValues = new HashMap<>();
			
			snList.add(persistSn);
			numValues.put(persistSn, Port.getTime());
			strValues.put(persistSn, new JSONObject().toJSONString());
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
		}
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		ActivityHumanData  data = activityHumanDataMap.get(persistSn);
		if(data.getNumValue() < activity.beginTime && activity.beginTime != 0l){
			JSONObject json = new JSONObject();
			data.setStrValue(json.toJSONString());
		}
		return activityHumanDataMap;
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
		long aid = 0;
		if(params != null){
			if(params.numParams.size()>0){
				aid = params.numParams.get(0);
			}
		}
		//记录在玩家身上的数据
		Map<Integer, ActivityHumanData> activityHumanDataMap = getHumanActivityDataList(humanObj, activity);
		if(activityHumanDataMap == null || activityHumanDataMap.size() <= 0){
			Log.activity.info("activityHumanDataMap error ,humanId ={} , activityId = {}",humanObj.getHumanId(),activity.id);
			return false;
		}
		ActivityHumanData humanData = activityHumanDataMap.get(persistSn);
		JSONObject jo = Utils.toJSONObject(humanData.getStrValue());
		Map<Integer,Integer> back = Utils.jsonToMapIntInt(jo.getString(jsonKey_Back));
		List<Long> getDays = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
		
		Integer value = back.get((int)aid);
		if(value == null || getDays.contains(aid)){
			return false;
		}
		
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(persistSn);
		List<Long> numParams = zoneItems.get(0).params.get(0).numParams;
		int ringDay = numParams.get(2).intValue();
		//活动进行了几天 - 1
		int days = getDayBetween(activity.beginTime, Port.getTime(), HOUR_ZERO);
		int ring = days / ringDay; //第几轮
		int day = (days % ringDay) + 1;	//这一轮的第几天
		if(day != 1 || ring == 0){
			return false;
		}
		
		getDays.add(aid);
		jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(getDays));
		humanData.setStrValue(jo.toJSONString());
		
		ItemChange itemChange = RewardHelper.reward(humanObj, numParams.get(3).intValue(), value, LogSysModType.ActServerGiftBag);
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
		case EventKey.HumanLoginFinishFirstToday:
		case EventKey.HumanLoginFinish:
		case EventKey.ActResetDailyHour:
			return true;
		case EventKey.ActConsumeGold:
			List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(persistSn);
			if(zoneItems == null){
				return false;
			}
			HumanObject humanObj = param.get("humanObj");
			long gold = param.get("num");//充值金额
			
			Map<Integer, ActivityHumanData> activityHumanDataMap = getHumanActivityDataList(humanObj, activity);
			ActivityHumanData data = activityHumanDataMap.get(persistSn);
			JSONObject jo = Utils.toJSONObject(data.getStrValue());
			Map<Integer,Integer> back = Utils.jsonToMapIntInt(jo.getString(jsonKey_Back));
			int days = getDayBetween(activity.beginTime, Port.getTime(),HOUR_ZERO)  + 1;
			List<Long> numParams = zoneItems.get(0).params.get(0).numParams;
			int backNum = (int) (gold * numParams.get(0) / 100);
			if(back.containsKey(days)){
				backNum = backNum + back.get(days);
			}
			int dayLimit = numParams.get(1).intValue();
			if(dayLimit <= backNum){
				backNum = dayLimit;
			}
			back.put(days, backNum);
			jo.put(jsonKey_Back, Utils.mapIntIntToJSON(back));
			data.setStrValue(jo.toJSONString());
			data.setNumValue(Port.getTime());
			return true;
		default:
			break;
		}
		return false;
		
	}
}