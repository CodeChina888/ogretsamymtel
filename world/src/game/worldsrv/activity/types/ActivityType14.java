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
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ActivityType14 extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_14;	//类型
	private static final ActivityType14 instance = new ActivityType14();//实例	
	
	private ActivityType14() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType14.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType14.type;
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
		if (awards == null) {
			return zoneItems;
		}
		for(int i=0;i<awards.size();i++){
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			JSONArray items = rewardJson.getJSONArray("items");// 给的物品
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
	public Map<Integer, ActivityHumanData> activityData(HumanObject humanObj,ActivityObject activity){
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		if (activityData != null) {
			// 判断活动期限
			ActivityHumanData get0Data = activityData.get(0);
			if (get0Data != null) {
				Integer time = Utils.intValue(get0Data.getStrValue());
				Integer beginTime = getTimeDataValue(activity.beginTime);
				if (beginTime > time) {
					humanObj.activityDatas.get(activity.id).clear();
					activityData = new HashMap<>();
				}
			}
		}else{
			activityData = new HashMap<>();
		}
		return activityData;
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
		boolean showPoint = false;//显示小红点
		Map<Integer, ActivityHumanData> activityData= activityData(humanObj, activity);
		////////////////////////////
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		int sn = 0;
		if (activityData.containsKey(0)) {
			sn=(int)activityData.get(0).getNumValue();
			// 如果是当天已经充值了就不解锁第二天，某天如果充值中断之能从原先的充值天数进行接壤
			int payDay = Integer.valueOf(activityData.get(0).getStrValue());
			if (payDay != getNowDateValue(HOUR_ZERO)) {
				sn = sn + 1;
			}
		}else{
			sn = 1;
		}
		DActivityParam.Builder dp2 = DActivityParam.newBuilder();
		dp2.addNumParam(sn);//编号
		dz.addActivityParams(dp2);
		
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry:activity.zoneItems.entrySet()) {
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
			ActivityHumanData data = activityData.get(entry.getKey());
			if (data == null) {
				dp.addNumParam(-1);
				dp.addNumParam(0);
			}else{
				dp.addNumParam(data.getNumValue());
				if (!data.getStrValue().equals("")) {
					dp.addNumParam(Integer.valueOf(data.getStrValue()));
 				}else{
					dp.addNumParam(0);
				}
				if (data.getNumValue() != 1l) {
					showPoint = true;
				}
			}
			dz.addActivityParams(dp);
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
		Long zone = 0L;// 获得SID
		if(params != null){
			if(params.numParams.size() > 0){
				zone = params.numParams.get(0);
			}
		}
		if (zone == 0L) {
			return false;
		}
		// 配置
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(zone.intValue());
		if(zoneItems == null){
			return false;
		}
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		if(zoneItem == null){
			return false;
		}
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		ActivityHumanData data = activityData.get(zone.intValue());
		if (data == null || data.getNumValue() == 1l) {// 未充值或者已经领取过了
			return false;
		}
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		snList.add(zone.intValue());
		numValues.put(zone.intValue(), 1l);
		commitHumanActivityData(activity,humanObj,snList,numValues);
		List<ProduceVo> proList = new ArrayList<>();
		ActivityParamObject zoneParam = zoneItem.params.get(0);
		for(DItem item:zoneParam.itemParams){
			ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
			proList.add(pro);
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActDailyPayGift);
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
			case EventKey.ResetDailyHour: {
				if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
					return true;
			}	break;
			case EventKey.PayNotify:
			// 玩家关于这个的数据
			HumanObject humanObj = param.get("humanObj");
			// 判断今天的有没有充值过了
			long gold = param.get("gold");
			int nowDataValue = getNowDateValue(HOUR_ZERO);
			Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			Map<Integer,String> strValues = new HashMap<>();
			int sn = 1;
			Long status = 0l;
			if (activityData != null) {
				ActivityHumanData data = activityData.get(0);
				if (data != null) {
					int payDay = Integer.valueOf(data.getStrValue());
					sn = (int)data.getNumValue();
					if (payDay == nowDataValue) {
						ActivityHumanData data1 = activityData.get(sn);
						if (data1!=null) {
							status = data1.getNumValue();
							gold += Integer.valueOf(data1.getStrValue());
						}
					}else{
						sn = (int) (data.getNumValue() + 1);
					}
				}
			}
			snList.add(0);
			snList.add(sn);
			numValues.put(0, (long)sn);
			numValues.put(sn, status);
			strValues.put(0, nowDataValue+"");
			strValues.put(sn, gold+"");
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
			return true;
		default:
			break;
		}
		return false;
	}
	/**
	 * 备注：每天充值的时候都会重新处理，若是二次充值会在对应的SID进行修改金额
	 * Map<ActivityHumanData> data = humanObj.activityDatas.get(14);
	 * data1 = data.get(0);
	 * data1.sn = 0;
	 * data1.numParam = sid;// 对应的sid
	 * data1.strParam = nowData;// 对应充值的时间
	 * 
	 * 
	 * data2 = data.get(sid);
	 * data2.sn = sid;
	 * data2.numParam = stautc;// 当前的状态，1是已经领取了0未领取
	 * data2.strParam = gold;//充值的钱
	 * 
	 */
}
