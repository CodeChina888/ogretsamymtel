package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.msg.Define.ETreesureType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityTreasure;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

/**
 *寻宝
 *				 今日领取次数   背包中的物品
 * |上次领取时间|{"count":12,"itemList":[{"num":1,"sn":4},{"num":1,"sn":2},{"num":1,"sn":1},{"num":1,"sn":1},{"num":1,"sn":2},{"num":1,"sn":4},{"num":1,"sn":4}]}
 * @author qizheng
 *
 */
public class ActivityTreasure extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_25;	//类型
	private static final ActivityTreasure instance = new ActivityTreasure();	//实例


	private ActivityTreasure() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityTreasure.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityTreasure.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		Collection<ConfActivityTreasure>  confList = ConfActivityTreasure.findAll();
		for(ConfActivityTreasure conf:confList){

			int aid = conf.sn;
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			//数量上限
			paramObj.numParams.add((long)conf.limit);
			//cd时间
			paramObj.numParams.add(conf.cdTime);
			//物品列表
			List<DItem> ditemList = new ArrayList<>();
			for (int j = 0; j < conf.itemSn.length; j++) {
				int itemSn = conf.itemSn[j];
				int itemCount = conf.itemNum[j];
				DItem.Builder ditem = DItem.newBuilder();
				ditem.setItemSn(itemSn);
				ditem.setNum(itemCount);
				ditemList.add(ditem.build());
			}
			
			//参数拼接
			paramObj.itemParams.addAll(ditemList);
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
		//今日创角天数
		int createDay = getFromCreate(humanObj);
		//获取对应的条件
		//这里没有判断空
		if(activity.zoneItems.get(createDay) == null) {
			return null;
		}
		ActivityZoneItemObject conditionList = activity.zoneItems.get(createDay).get(0);

		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		

		long limitMax = conditionList.params.get(0).numParams.get(0);//条件中能获取的最大数量
		long cdTime = conditionList.params.get(0).numParams.get(1)*1000;//cd时间

		long alreadyGet = 0;
		List<DItem> ditems = conditionList.params.get(0).itemParams;
		boolean canGETAward = false;
		//临时背包的物品
		String pakageList = "";
		long lastTime = 0L;
		boolean isInterDay = false;
		
		JSONObject jo = new JSONObject();
		JSONArray itemList = new JSONArray();
		if(activityHumanDataMap != null ){
			ActivityHumanData human_data = activityHumanDataMap.get(0);
			
			//领取过了
			lastTime = human_data.getNumValue();
			pakageList = human_data.getStrValue();
			isInterDay = isInterDay(lastTime);
			JSONObject json = Utils.toJSONObject( human_data.getStrValue());
			jo = Utils.toJSONObject(pakageList);
			itemList = Utils.toJSONArray(jo.getString("itemList"));
			//重置已领取次数
			if(isInterDay) {
				json.put("count", 0);
			}
			alreadyGet = json.getIntValue("count");
			human_data.setStrValue(json.toJSONString());
		}
		//如果物品集齐,并且跨天则可以领取,切物品列表不为空
		
		if(isInterDay && itemList.size()!=0){
			//物品已经集满，可以领取
			canGETAward = true;
		}
		boolean showPoint = false;//显示小红点
		
		//加入一个特殊的
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		DActivityParam.Builder dp = DActivityParam.newBuilder();
		long leaveNum = 0;//剩余可领取次数 今日登陆时长/cdTime
		leaveNum = humanObj.getHuman().getDailyOnlineTime()/cdTime;
		if(leaveNum >= limitMax) {
			leaveNum = limitMax;
		}
		dp.addNumParam(leaveNum);//剩余可领取次数 
		dp.addNumParam(limitMax);//最大领取次数
		dp.addNumParam(alreadyGet);//已领取次数
		dp.addNumParam(0);//倒计时
		dp.addNumParam(canGETAward == true?EAwardType.Awarding_VALUE:EAwardType.AwardNot_VALUE);
		dp.addNumParam(cdTime/1000);//今日CD

		
		
		if(itemList != null && itemList.size()>0){
			int [] sn_arr = new int[itemList.size()];
			int [] num_arr = new int[itemList.size()];
			for(int i =0;i<itemList.size();i++){
				JSONObject itemJson = (JSONObject) itemList.get(i);
				int sn = itemJson.getIntValue("sn");
				int num = itemJson.getIntValue("num");
				sn_arr[i] = sn;
				num_arr[i] = num;
			}
			dp.addStrParam(Utils.arrayIntToStr(sn_arr));
			dp.addStrParam(Utils.arrayIntToStr(num_arr));
		}else {
			dp.addStrParam("");
			dp.addStrParam("");
		}
		Log.activity.debug("data error");
		
		for(DItem di:conditionList.params.get(0).itemParams){
			dp.addItems(di);//奖励物品
		}
		dz.addActivityParams(dp.build());
//		if(limitMax - alreadyGet>0 || limitMax - alreadyGet != 10) {
//			showPoint = true;
//		}
		if(leaveNum - alreadyGet >0) {
			showPoint = true;
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
		ActivityParamObject params = paramList.get(0);
		if(params==null){
			return false;
		}

		//今日创角天数
		int createDay = getFromCreate(humanObj);
		//获取对应的条件
		ActivityZoneItemObject conditionList = activity.zoneItems.get(createDay).get(0);


		//如果是领取背包中的物品
		int type = params.numParams.get(0).intValue();

		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		ActivityHumanData human_data = null;
		if(activityHumanDataMap != null) {
			human_data = activityHumanDataMap.get(0);
		}else {
			human_data = new ActivityHumanData();
			human_data.setId(Port.applyId());
			human_data.setHumanId(humanObj.getHumanId());
			human_data.setNumValue(0L);
			human_data.setActivityId(activity.id);
			human_data.setSn(0);
			JSONObject jo = new JSONObject();
			jo.put("count", 0);
			jo.put("itemList", new JSONArray());
			human_data.setStrValue(jo.toJSONString());
			
			activityHumanDataMap = new HashMap<>();
			activityHumanDataMap.put(0, human_data);
			humanObj.activityDatas.put(activity.id, activityHumanDataMap);
		}
		
		
		JSONObject json = Utils.toJSONObject( human_data.getStrValue());
		
		if(type == ETreesureType.getAwards_VALUE){
			Log.activity.debug("正在领取背包中的物品");
			long lastTime = human_data.getNumValue();
			//上次领取时间是否是今天凌晨五点之前
			boolean isInterDay = isInterDay(lastTime);
			if(!isInterDay){
				Log.activity.info("时间还未跨天。。。。");
				return false;
			}
			//校验背包中物品是否满了
			List<Integer> itemSnList = Utils.strToIntList(human_data.getStrValue());
			//数量满足
			if(json.getString("itemList").length() > 2) {
				JSONArray jarr = json.getJSONArray("itemList");
				int [] itemSn = new int[jarr.size()];
				int [] itemNum = new int[jarr.size()];
				
				for(int i =0;i<jarr.size();i++) {
					JSONObject itemJson = (JSONObject) jarr.get(i);
					itemSn[i] = itemJson.getIntValue("sn");
					itemNum[i] = itemJson.getIntValue("num");
				}
				RewardHelper.reward(humanObj, itemSn,itemNum,LogSysModType.Activity);
				//清空背包
				json.put("itemList", "");
				human_data.setStrValue(json.toJSONString());
				return true;
			}else {
				return false;
			}
			
		}else{
			Log.activity.debug("正在获取单个物品");
			long lastTime = human_data.getNumValue();
			//上次领取时间是否是今天凌晨五点之前
			boolean isInterDay = isInterDay(lastTime);
			//如果背包有东西而且跨天了
			if(json.getString("itemList").length() > 2 && json.getJSONArray("itemList").size() > 0 && isInterDay) {
				humanObj.sendSysMsg(520400);//请先领取昨日背包中的物品
				return false;
			}
			
			int limitMax  = conditionList.params.get(0).numParams.get(0).intValue();//条件中能获取的最大数量
			long cdTime = conditionList.params.get(0).numParams.get(1)*1000;
			List<DItem> ditems = conditionList.params.get(0).itemParams;
			//要领取物品sn
			int index = params.numParams.get(1).intValue();//物品的索引
			DItem _ditem = ditems.get(index);
			//条件列表中是否包含该物品
			//RewardHelper.reward(humanObj, _ditem.getItemSn(), _ditem.getNum(),LogSysModType.Activity);
			//今日次数是否达到上限
			
			int count = json.getIntValue("count");
			//如果背包中由东西，且隔天了，要先领取
			
			
			//TODO 记得开放
			if(count >=limitMax){
				Log.activity.debug("已达到次数上限");
				return false;
			}
//			if(itemSnList.size() >= limitMax) {
//				Log.activity.debug("已达到次数上限");
//				return false;
//			}
			
			//CD时间是否达到
//			long lastTime = human_data.getNumValue();
//			if(lastTime!=0 && Port.getTime() - lastTime < cdTime){
//				Log.activity.debug("cd时间还未到达");
//				return false;
//			}
			
			//领取放入临时背包
			JSONArray jarr = new JSONArray();
			if(!json.getString("itemList").equals("")) {
				jarr = json.getJSONArray("itemList");
			}
			JSONObject jo = new JSONObject();
			jo.put("sn",_ditem.getItemSn());
			jo.put("num",_ditem.getNum());
			jarr.add(jo);
			json.put("itemList", jarr);

			//记录本次领取时间
			human_data.setNumValue(Port.getTime());
			//add count
			json.put("count",count+1);
			human_data.setStrValue(json.toJSONString());
			if(!human_data.isOldRecord()) {
				human_data.persist();
			}
			return true;
		}

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
		switch(event){
			case EventKey.HumanLoginFinishFirstToday:
			case EventKey.HumanLoginFinish:
				return true;
			case EventKey.ResetDailyHour: {
				if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
					return true;
			}	break;
		}
		return false;
	}

	/**
	 * 获取玩家创角之后的天数
	 */
	private int getFromCreate(HumanObject humanObject){
		long createTime = humanObject.getHuman().getTimeCreate();
		int between = Utils.getDaysBetween(createTime,Port.getTime());
		return between;
	}

	/**
	 * 是否是凌晨五点以前的时间
	 */
	public  boolean isInterDay(long lastTime){
		//如果lastTime和今天是同一天，则当前时间要大于凌晨五点，且上次领取事件要小于凌晨五点
		long now = Port.getTime();
		if(Utils.isSameDay(lastTime,now)) {
			long hour_5_timestamp = Utils.getTimestampTodayAssign(5,0, 0);//今日五点时间戳
			boolean condition1 = now > hour_5_timestamp;
			boolean condition2 = hour_5_timestamp > lastTime;
			return condition1 && condition2 ;
		}
		//如果不是同一天
		if(lastTime <= 0){
			Log.activity.debug("isInterDay's lastime can not be 0");
			return false;
		}
		long times = Utils.getTimeHourOfToday(5);
		return times > lastTime && now > times;
	}
}
