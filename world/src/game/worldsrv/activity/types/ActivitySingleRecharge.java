package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivitySingleRecharge;
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
/**
 * 单笔充值
 * @author qizheng
 *
 */
public class ActivitySingleRecharge extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_19;	//类型
	private static final ActivitySingleRecharge instance = new ActivitySingleRecharge();//实例	
	
	public static final String RECHARGE = "recharge";
	public static final String COUNT = "count";
	public static final String GETNUM = "getNum";
	private ActivitySingleRecharge() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivitySingleRecharge.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivitySingleRecharge.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		Collection<ConfActivitySingleRecharge>  confList = ConfActivitySingleRecharge.findBy("activityId",activity.id);
		for(ConfActivitySingleRecharge conf:confList){

			int aid = conf.sn;
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			Long needTopUp = new Long(conf.needTopUp);//所需升级
			Long count = new Long(conf.count);//次数
			int confRewardSn = conf.rewardSn;//奖励表对应的sn
			ConfRewards confRewards = ConfRewards.get(confRewardSn);
			List<DItem> ditemList = new ArrayList<>();
			for (int j = 0; j < confRewards.itemSn.length; j++) {
				int itemSn = confRewards.itemSn[j];
				int itemCount = confRewards.itemNum[j];
				DItem.Builder ditem = DItem.newBuilder();
				ditem.setItemSn(itemSn);
				ditem.setNum(itemCount);
				ditemList.add(ditem.build());
			}
			
			//参数拼接
			paramObj.numParams.add(needTopUp);
			paramObj.numParams.add(count);
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
		
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		
		boolean showPoint = false;//显示小红点
		//加入一个特殊的
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		DActivityParam.Builder dps = DActivityParam.newBuilder();
		dps.addNumParam(0);
		dz.addActivityParams(dps.build());
	
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry : activity.zoneItems.entrySet()) {
			//条件数据
			int aid = entry.getKey();
			List<ActivityZoneItemObject> paraList = entry.getValue();
			long needToUp = paraList.get(0).getParams().numParams.get(0);
			long count = paraList.get(0).params.get(0).numParams.get(1);//配置表中的次数
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			long buttonStatus = EAwardType.AwardNot_VALUE;
			long getNum = 0L;
			//玩家自身数据,看状态
			if(activityHumanDataMap != null && activityHumanDataMap.size()>0){
				ActivityHumanData human_data = activityHumanDataMap.get(0);
				JSONObject jo = Utils.toJSONObject(human_data.getStrValue());
				JSONObject jodata = jo.getJSONObject(String.valueOf(needToUp));
				
				if(jodata != null){
					getNum = jodata.getLongValue(GETNUM);//已领取次数
					long rechargeCount = jodata.getLongValue(COUNT);//充值次数
					//限定次数 大于 领取次数  领取次数 > 充值次数
					if(count > getNum && rechargeCount >getNum){
						buttonStatus = EAwardType.Awarding_VALUE;
						showPoint = true;
					}else{
						buttonStatus = EAwardType.Awarded_VALUE;
					}
				}
			}
			
			dp.addNumParam(aid);
			dp.addNumParam(buttonStatus);
			dp.addNumParam(needToUp);
			dp.addNumParam(count);
			dp.addNumParam(getNum);
			for(DItem di:paraList.get(0).params.get(0).itemParams){
				dp.addItems(di);//奖励物品
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
		//要领取奖励的活动编号
		int aid = params.numParams.get(0).intValue();
		ConfActivitySingleRecharge conf = ConfActivitySingleRecharge.get(aid);
		if(conf == null){
			Log.table.error("ConfActivitySingleRecharge error , sn={}",aid);
			return false;
		}
		int needToUp = conf.needTopUp;//所需金额
		//记录在玩家身上的数据
		Map<Integer, ActivityHumanData> activityData = humanObj.activityDatas.get(activity.id);
		if(activityData ==null || activityData.size() <= 0){
			Log.activity.info("玩家活动数据错误, humanId={},activityId={}",humanObj.getHumanId(),activity.id);
			return false;
		}
		ActivityHumanData data = activityData.get(0);
		//下面两个json一定会有数据，如果没有则说明不存在相应的充值，直接返回false
		JSONObject json = Utils.toJSONObject(data.getStrValue());
		if(json == null){
			Log.activity.info("json == null 玩家活动数据错误, humanId={},activityId={}",humanObj.getHumanId(),activity.id);
			return false;
		}
		JSONObject jsondata = json.getJSONObject(String.valueOf(needToUp));
		if(jsondata == null){
			Log.activity.info("jsondata == null 玩家活动数据错误, humanId={},activityId={}",humanObj.getHumanId(),activity.id);
			return false;
		}
		//修改json 
		int count = jsondata.getIntValue(COUNT);
		int getNum = jsondata.getIntValue(GETNUM);
		//判断是否可以领取
		if(conf.count > getNum && count > getNum){
			jsondata.put(GETNUM, getNum+1);		
		}else{
			Log.activity.info("已经达到次数上限 或次数不足  humanId={},activityId={} ,aid={},rechargeCount={}",humanObj.getHumanId(),activity.id,aid,count);
			return false;
		}
		
		
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		Map<Integer,String> strValues = new HashMap<>();//字符参数表
		snList.add(0);
		numValues.put(0, activity.beginTime);
		strValues.put(0, json.toJSONString());
		commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
		
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(aid);
		//奖励
		List<ProduceVo> proList = new ArrayList<>();
		for(DItem item:zoneItems.get(0).params.get(0).itemParams){
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
		case EventKey.ResetDailyHour: {
			if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
				return true;
		}	break;
		case EventKey.PayNotify:
			HumanObject humanObj = param.get("humanObj");
			long gold = param.get("gold");//玩家充值的金额
			/*
			 * "10":{"recharge":"10","count":"1","getNum":"0"}
			 * 充值10元 1次 领取 0次
			 */
			//获取玩家数据
			Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
			JSONObject jo = new JSONObject();
			if(activityHumanDataMap != null && activityHumanDataMap.size() >0){
				ActivityHumanData data = activityHumanDataMap.get(0);
				jo = Utils.toJSONObject(data.getStrValue());
			}
			
			JSONObject data = jo.getJSONObject(String.valueOf(gold));
			if(data == null || data.isEmpty()){
				data= new JSONObject();
				data.put(RECHARGE, gold);
				data.put(COUNT, 1);
				data.put(GETNUM, 0);
			}else{
				int count = data.getIntValue(COUNT);
				count++;
				data.put("count", count);
			}
			jo.put(String.valueOf(gold), data);
			
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			Map<Integer,String> strValues = new HashMap<>();
			snList.add(0);
			numValues.put(0, activity.beginTime);
			strValues.put(0, jo.toJSONString());
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
			return true;
		default:
			break;
		}
		return false;
	}
}
