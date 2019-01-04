package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityAccumulatedRecharge;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.EventKey;
import io.netty.handler.codec.json.JsonObjectDecoder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 累计充值
 * @author qizheng
 *
 */
public class ActivityAccumulatedRecharge extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_10;	//类型
	private static final ActivityAccumulatedRecharge instance = new ActivityAccumulatedRecharge();//实例	
	
	private static final String jsonKey_Gold = "Gold";
	private static final String jsonKey_AlreadyGet = "AlreadyGet";
	private ActivityAccumulatedRecharge() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityAccumulatedRecharge.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityAccumulatedRecharge.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		
		Collection<ConfActivityAccumulatedRecharge> confAll = ConfActivityAccumulatedRecharge.findBy("activityId",activity.id);
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		for(ConfActivityAccumulatedRecharge conf:confAll){

			int aid =	conf.sn;
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
		
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		
		/**
		 * {"gold":"10","alreadyGet"}
		 */
		long gold = 0;//活动期间充值金额
		List <Long> alreadyGetAid = new ArrayList<>();//已经领取的活动编号
		if(activityHumanDataMap != null ){
			ActivityHumanData data = activityHumanDataMap.get(0);
			JSONObject jo = Utils.toJSONObject(data.getStrValue());
			gold = jo.getLongValue(jsonKey_Gold);
			alreadyGetAid = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
		}
		//加入一个特殊的
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		DActivityParam.Builder dps = DActivityParam.newBuilder();
		dps.addNumParam(gold);
		dz.addActivityParams(dps.build());
		
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry : activity.zoneItems.entrySet()) {
			ActivityZoneItemObject zoneItem = entry.getValue().get(0);
			if(zoneItem == null){
				continue;
			}
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			long aid = entry.getKey();
			
			ActivityParamObject zoneParam = zoneItem.getParams();
			for(DItem di:zoneParam.itemParams){
				dp.addItems(di);//奖励物品
			}
			
			Long num = zoneParam.numParams.get(0);//需求购买的砖石
			if(num == null){
				num = -1L;
			}
			//还需要购买多少钻石
			long needToRecharge = 0;
			long status = EAwardType.AwardNot_VALUE;
			if(gold < num){
				needToRecharge = num - gold;
			}else{
				//如果没领取过
				if(!alreadyGetAid.contains(aid)){
					status = EAwardType.Awarding_VALUE;
					showPoint = true;
				}else{
					status = EAwardType.Awarded_VALUE;
				}
			}
			
			dp.addNumParam(aid);//活动编号
			dp.addNumParam(status);
			dp.addNumParam(num);
			dp.addNumParam(needToRecharge);
			
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
		long aid = 0;
		if(params != null){
			if(params.numParams.size()>0){
				aid = params.numParams.get(0);
			}
		}
		
		//记录在玩家身上的数据
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		if(activityHumanDataMap == null || activityHumanDataMap.size() <= 0){
			Log.activity.info("activityHumanDataMap error ,humanId ={} , activityId = {}",humanObj.getHumanId(),activity.id);
			return false;
		}
		ActivityHumanData humanData = activityHumanDataMap.get(0);
		//查看条件是否达成
		
		JSONObject jo = Utils.toJSONObject(humanData.getStrValue());
		long rechargeGold = jo.getLongValue(jsonKey_Gold);
		
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get((int)aid);
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		ActivityParamObject zoneParam = zoneItem.params.get(0);
		Long numParam = zoneParam.numParams.get(0);//改项所需金额
		if(rechargeGold < numParam){
			Log.activity.info("未达到充值金额");
			return false;
		}
		List<Long> alreadyGetAid_List = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
		if(alreadyGetAid_List.contains(aid) ){
			Log.activity.info("已经领取过该奖励");
			return false;
		}
		alreadyGetAid_List.add(aid);
		jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(alreadyGetAid_List));
		
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		Map<Integer,String> strValues = new HashMap<>();
		
		snList.add(0);
		numValues.put(0, Port.getTime());
		strValues.put(0,jo.toJSONString());
		commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
		
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
		case EventKey.ResetDailyHour: {
			if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
				return true;
		}	break;
		case EventKey.PayNotify:
			HumanObject humanObj = param.get("humanObj");
			long gold = param.get("gold");
			// 获取活动内 玩家充值的金额
			
			Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
			JSONObject jo = new JSONObject();
			if(activityHumanDataMap != null && activityHumanDataMap.size() >0){
				ActivityHumanData data = activityHumanDataMap.get(0);
				jo = Utils.toJSONObject(data.getStrValue());
			}
			if(jo.size() <= 0){
				//初始化json
				jo.put(jsonKey_Gold, gold);
				jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(new ArrayList<Long>()));
			}else{
				int num = jo.getInteger(jsonKey_Gold);
				jo.put(jsonKey_Gold, gold+num);
			}
			
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			Map<Integer,String> strValues = new HashMap<>();
			
			snList.add(0);
			numValues.put(0, Port.getTime());
			strValues.put(0,jo.toJSONString());
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
			return true;
		default:
			break;
		}
		return false;
	}
}
