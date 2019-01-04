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
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 限时折扣
 * @author qizheng
 *
 */
public class ActivityType22 extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_22;	//类型
	private static final ActivityType22 instance = new ActivityType22();//实例	
	
	private ActivityType22() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType22.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType22.type;
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
		/*String str = json.getString("client");
		ActivityZoneItemObject zoneItemObj0 = new ActivityZoneItemObject(0);
		ActivityParamObject paramObj0 = new ActivityParamObject();
		paramObj0.strParams.add(str);
		zoneItemObj0.addParam(paramObj0);	
		zoneItems.add(zoneItemObj0);
		return zoneItems;*/		
		

		JSONArray awards = json.getJSONArray("awards");
		for(int i=0;i<awards.size();i++){
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			//"itemSn":402001,"moneySn":2,"moneyCount":50,"shopNum":10,"disCount":2.5,"maxCount":5,"vipLevel":0,"level":0
//			params.numParam[0] = 物品道具SN
//			params.numParam[1] = 购买该商品消耗的货币类型
//			params.numParam[2]== 购买物品对应消耗的货币数量
//			params.numParam[3]== 一次购买获得物品的数量
//			params.numParam[4]== 商品价格的折扣
//			params.numParam[5]== 商品可购买的最大次数
//			params.numParam[6]== 0为不限制，1以上为VIP等级
//			params.numParam[7]== 0为不限制，1以上为主角等级
			long moneySn = rewardJson.getLongValue("moneySn");//购买该商品消耗的货币类型
			paramObj.numParams.add(moneySn);
			long moneyCount = rewardJson.getLongValue("moneyCount");//购买物品对应消耗的货币数量
			paramObj.numParams.add(moneyCount);
			
//			long shopNum = rewardJson.getLongValue("shopNum");//一次购买获得物品的数量
//			paramObj.numParams.add(shopNum);
			
			long disCount = rewardJson.getLongValue("disCount");//商品价格的折扣
			paramObj.numParams.add(disCount);
			long maxCount = rewardJson.getLongValue("maxCount");//商品可购买的最大次数
			paramObj.numParams.add(maxCount);
			long vipLevel = rewardJson.getLongValue("vipLevel");//0为不限制，1以上为VIP等级
			paramObj.numParams.add(vipLevel);
			long level = rewardJson.getLongValue("level");//0为不限制，1以上为主角等级
			paramObj.numParams.add(level);
			
			int itemSn = rewardJson.getIntValue("itemSn");
			int itemCount = rewardJson.getIntValue("shopNum");
			DItem.Builder ditem = DItem.newBuilder();
			ditem.setItemSn(itemSn);
			ditem.setNum(itemCount);
			paramObj.itemParams.add(ditem.build());
			
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
		/*DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		List<ActivityZoneItemObject> list = activity.zoneItems.get(0);
		String str = list.get(0).params.get(0).strParams.get(0);
		DActivityParam.Builder dp = DActivityParam.newBuilder();
		dp.addStrParam(str);//编号
		dz.addActivityParams(dp.build());
		zoneList.add(dz.build());
		Param param = new Param();
		param.put("showPoint", false);
		return param;*/
		
		if(activity.zoneItems.size()<1){
			return null;
		}
		boolean showPoint = false;//显示小红点
		Map<Integer,Long> paramLists = getHumanActivityDataList(activity,humanObj);
		DActivityZoneItem.Builder dActivityZoneItem = DActivityZoneItem.newBuilder();
		dActivityZoneItem.setZone(0);
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry:activity.zoneItems.entrySet()) {
			ActivityZoneItemObject zoneItem = entry.getValue().get(0);
			if(zoneItem == null){
				continue;
			}
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			dp.addNumParam(entry.getKey());//编号
			
			ActivityParamObject zoneParam = zoneItem.getParams();

			Long moneySn = zoneParam.numParams.get(0);
			Long moneyCount = zoneParam.numParams.get(1);
			Long disCount = zoneParam.numParams.get(2);
			Long maxCount = zoneParam.numParams.get(3);//可购买的最大次数
			Long vipLevel = zoneParam.numParams.get(4);
			Long level = zoneParam.numParams.get(5);	
			

			Long buyCount = paramLists.get((int)entry.getKey());//已购买次数
			if(buyCount == null){
				buyCount = 0L;
			}
			//Long maxCount = zoneParam.numParams.get(3);//可购买的最大次数
			dp.addNumParam(maxCount - buyCount);//可购买的剩余次数
			for(DItem item:zoneParam.itemParams){
				dp.addNumParam(item.getItemSn());//物品道具SN
				dp.addNumParam(item.getNum());//一次购买获得物品的数量
			}
			dp.addNumParam(moneySn);
			dp.addNumParam(moneyCount);
			dp.addNumParam(disCount);
			dp.addNumParam(maxCount);
			dp.addNumParam(vipLevel);
			dp.addNumParam(level);
			
			// 未领取且消费的金额大于配置
			if (maxCount > buyCount && humanObj.getHuman().getVipLevel() >= vipLevel && humanObj.getHuman().getLevel() >= level) {
				showPoint = true;
			}
			dActivityZoneItem.addActivityParams(dp.build());
		}		
		zoneList.add(dActivityZoneItem.build());	
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
		long zone = 0;//aid
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
		// 消费
		/*Long gold = paramLists.get(0);
		if (gold == null) {
			return false;
		}*/
		// 获取这个活动的Aid配置
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get((int)zone);
		if(zoneItems==null){
			return false;
		}
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		if(zoneItem == null){
			return false;
		}
		//读取配置的需求
		//"moneySn":2,"moneyCount":50,"disCount":2.5,"maxCount":5,"vipLevel":0,"level":0
		ActivityParamObject zoneParam = zoneItem.getParams();
		Long moneySn = zoneParam.numParams.get(0);
		Long moneyCount = zoneParam.numParams.get(1);
		//Long disCount = zoneParam.numParams.get(2);
		Long maxCount = zoneParam.numParams.get(3);//可购买的最大次数
		Long vipLevel = zoneParam.numParams.get(4);
		Long level = zoneParam.numParams.get(5);

		Long buyCount = paramLists.get((int)zone);//已购买次数
		if(buyCount != null && buyCount >= maxCount){
			return false;
		}
		if (humanObj.getHuman().getVipLevel() < vipLevel) {
			return false;
		}
		if (humanObj.getHuman().getLevel() < level) {
			return false;
		}
		// 扣元宝
		if (!RewardHelper.checkAndConsume(humanObj, moneySn.intValue(), moneyCount.intValue(), LogSysModType.ActDisCount)) {
			return false;
		}
		if(buyCount == null){
			buyCount = 0L;
		}
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		snList.add((int)zone);// 设置档位
		numValues.put((int)zone, buyCount+1);// 
		commitHumanActivityData(activity,humanObj,snList,numValues);
		//奖励
		List<ProduceVo> proList = new ArrayList<>();
		for(DItem item:zoneParam.itemParams){
			ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
			proList.add(pro);
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActDisCount);
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
		Map<Integer, ActivityHumanData> activityData = humanObj.activityDatas.get(activity.id);
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
		case EventKey.HumanLvUp:
		case EventKey.PayNotify:
			return true;
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
