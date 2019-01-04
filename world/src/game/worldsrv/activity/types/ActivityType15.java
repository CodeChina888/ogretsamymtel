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

public class ActivityType15 extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_15;	//类型
	private static final ActivityType15 instance = new ActivityType15();//实例	
	
	private ActivityType15() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType15.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType15.type;
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
			Long excNum = rewardJson.getLong("excNum");
			paramObj.numParams.add(excNum);
			JSONArray needItems = rewardJson.getJSONArray("needItems");// 需要的物品
			paramObj.strParams.add(needItems.toJSONString());// 因为结构中并没有需要的item类型，所以存入字符串
			JSONArray getItems = rewardJson.getJSONArray("getItems");// 给的物品
			for(int j=0;j<getItems.size();j++){
				JSONArray item = getItems.getJSONArray(j);
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
		JSONArray loser = json.getJSONArray("loser");
		for(int i=0;i<loser.size();i++){
			JSONObject rewardJson = loser.getJSONObject(i);
			int chapteId = rewardJson.getIntValue("chapteId");
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(chapteId + 1000);
			ActivityParamObject paramObj = new ActivityParamObject();
			String item = rewardJson.getString("item");
			paramObj.strParams.add(item);
			String weights = rewardJson.getString("weights");
			paramObj.strParams.add(weights);
			int min = rewardJson.getIntValue("min");
			paramObj.numParams.add((long)min);
			int max = rewardJson.getIntValue("max");
			paramObj.numParams.add((long)max);
			zoneItemObj.addParam(paramObj);	
			zoneItems.add(zoneItemObj);
		}
		return zoneItems;
	}
	
	public List<ProduceVo> getProduceVo(String json){
		JSONArray needItems = Utils.toJSONArray(json);
		List<ProduceVo> list = new ArrayList<>();
		for (int i = 0; i < needItems.size(); i++) {
			JSONArray item = needItems.getJSONArray(i);
			int itemSn = item.getIntValue(0);
			int itemCount = item.getIntValue(1);
			list.add(new ProduceVo(itemSn, itemCount));
		}
		return list;
	}
	
	/**
	 * 获取给客户端的参数（不给下红点，客户端自行判断显示）
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
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry:activity.zoneItems.entrySet()) {
			if (entry.getKey() > 1000) {
				continue;
			}
			
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
			if(value!=null){
				dp.addNumParam(value);//已经兑换了几次
			}else{
				dp.addNumParam(0);//未兑换
			}
			dp.addNumParam(zoneParam.numParams.get(0));
			String needItem = zoneParam.strParams.get(0);//需求的无物品
			dp.addStrParam(needItem);//方案1：将道具字符串全部发给客户端，让客户端自行处理
			// 方案2：后端将数据处理一下发给客户端
			dz.addActivityParams(dp.build());
		}		
		zoneList.add(dz.build());	
		Param param = new Param();
		param.put("showPoint", showPoint);
		return param;
	}
	
	/**
	 * 获取玩家的活动数据
	 * @param activity
	 * @param humanObj
	 * @return
	 */
	private Map<Integer,Long> getHumanActivityDataList(ActivityObject activity, HumanObject humanObj) {
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		Map<Integer,Long> paramList = new HashMap<>();
		if(activityData != null){
			for(Map.Entry<Integer, ActivityHumanData> entry:activityData.entrySet()){
				paramList.put(entry.getKey(), entry.getValue().getNumValue());
			}			
		}
		//应加入重置的处理
		if(paramList.size()>0){
			Long day = paramList.get(0);
			if(day != null && day.intValue() < getActivityBeginDateValue(activity)){
				paramList.clear();
				humanObj.activityDatas.get(activity.id).clear();
			}
		}
		return paramList;
	}
	/**
	 * 处理客户端的执行请求(要求客户端发送要兑换的ID)
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
		Long value = paramLists.get((int)zone);
		if (value == null) {
			value = 0l;
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
		//没达到配置的需求(可兑换次数)
		ActivityParamObject zoneParam = zoneItem.params.get(0);
		Long canExc = zoneParam.numParams.get(0);
		Long yetExc = paramLists.get((int)zone);
		if (yetExc != null && yetExc >= canExc) {// 兑换已达上限
			return false;
		}
		if (yetExc == null) {
			yetExc = 0L;
		}
		String needItem = zoneParam.strParams.get(0);//需求购买的砖石
		List<ProduceVo> getProduceVo = getProduceVo(needItem);
		// 扣元宝
		if (!RewardHelper.checkAndConsume(humanObj, getProduceVo, LogSysModType.ActItemExchange)) {
			return false;
		}
		
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		snList.add((int)zone);
		snList.add(0);
		numValues.put(0, (long)getNowDateValue());
		numValues.put((int)zone, yetExc + 1);
		commitHumanActivityData(activity,humanObj,snList,numValues);
		//奖励
		List<ProduceVo> proList = new ArrayList<>();
		for(DItem item:zoneParam.itemParams){
			ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
			proList.add(pro);
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActItemExchange);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return false;
		
	}
	
	/**
	 * 监听事件触发（不触发）
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
}
