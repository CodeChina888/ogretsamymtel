package game.worldsrv.activity.types;

import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.msg.Define.EDrawType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityAccumulatedCost;
import game.worldsrv.config.ConfActivitySeekImmortal;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
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

/**
 * 寻仙有礼
 * @author Administrator
 *
 */
public class ActivitySeekImmortal extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_30;	//类型
	private static final ActivitySeekImmortal instance = new ActivitySeekImmortal();//实例	
	
	private static final String jsonKey_Draw = "Draw";
	private static final String jsonKey_AlreadyGet = "AlreadyGet";
	
	//持久化 sn =1
	private static final Integer persistSn = 1;
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivitySeekImmortal.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivitySeekImmortal.type;
	}

	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		Collection<ConfActivitySeekImmortal> confAll = ConfActivitySeekImmortal.findAll();
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		for(ConfActivitySeekImmortal conf:confAll){
			int aid = conf.sn;
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			long needTopUp = conf.needNum;
			
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
		long gold = jo.getLongValue(jsonKey_Draw);
		List <Long> alreadyGetAid = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
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
			long status = EAwardType.AwardNot_VALUE;
			//如果没领取过
			if(!alreadyGetAid.contains(aid)){
				if(num <= gold){
					status = EAwardType.Awarding_VALUE;
					showPoint = true;
				}
			}else{
				status = EAwardType.Awarded_VALUE;
			}
			
			dp.addNumParam(aid);//活动编号
			dp.addNumParam(status);
			dp.addNumParam(num);
			
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
		long rechargeGold = jo.getLongValue(jsonKey_Draw);
		
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get((int)aid);
		if(zoneItems == null){
			return false;
		}
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		ActivityParamObject zoneParam = zoneItem.params.get(0);
		Long numParam = zoneParam.numParams.get(0);//改项所需金额
		if(rechargeGold < numParam){
			Log.activity.info("高级探访次数不足，无法领取");
			return false;
		}
		List<Long> alreadyGetAid_List = Utils.strToLongList(jo.getString(jsonKey_AlreadyGet));
		if(alreadyGetAid_List.contains(aid) ){
			Log.activity.info("已经领取过该奖励");
			return false;
		}
		alreadyGetAid_List.add(aid);
		jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(alreadyGetAid_List));
		humanData.setStrValue(jo.toJSONString());
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
		case EventKey.DrawCard:
			int type = param.getInt("type");
			if(type != EDrawType.ByGold_VALUE){
				return false;
			}
			HumanObject humanObj = param.get("humanObj");
			int gold = param.get("num");
			Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
			JSONObject jo = new JSONObject();
			ActivityHumanData data = activityHumanDataMap.get(persistSn);
			jo = Utils.toJSONObject(data.getStrValue());
			if(jo.size() <= 0){
				jo.put(jsonKey_Draw, gold);
				jo.put(jsonKey_AlreadyGet, Utils.ListLongToStr(new ArrayList<Long>()));
			}else{
				int num = jo.getInteger(jsonKey_Draw);
				jo.put(jsonKey_Draw, gold + num);
			}
			data.setStrValue(jo.toJSONString());
			return true;
		default:
			break;
		}
		
		return false;
		
	}
}
