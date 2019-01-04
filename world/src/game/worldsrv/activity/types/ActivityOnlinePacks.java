package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.Param;
import core.support.Time;
import core.support.Utils;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.msg.MsgActivity.SCHumanOnLineTimeMsg;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityOnlinePack;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.EventKey;
/**
 * 在线礼包
 * @author sys
 *
 */
public class ActivityOnlinePacks extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_18;	//类型
	private static final ActivityOnlinePacks instance = new ActivityOnlinePacks();
	
	/**
	 * 构造函数
	 */
	private ActivityOnlinePacks(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityOnlinePacks.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityOnlinePacks.type;
	}
	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		
		Collection<ConfActivityOnlinePack>  conf_Collection = ConfActivityOnlinePack.findAll();
		for(ConfActivityOnlinePack conf:conf_Collection){
			int aid =conf.sn;//编号,做为ActivityZoneItemObject里面的zone
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject itemParamObj = new ActivityParamObject();
			itemParamObj.numParams.add(Utils.longValue(aid));//aid
			itemParamObj.numParams.add(Utils.longValue(conf.openDay));//开服后第几天
			itemParamObj.numParams.add(Utils.longValue(conf.minTime));//在线达标时间
			itemParamObj.numParams.add(Utils.longValue(conf.rewardSn));//奖励ID
			itemParamObj.numParams.add(0L);//未领取
			zoneItemObj.addParam(itemParamObj);	
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
		if(activity.zoneItems.size() < 1){
			return null;
		}
		//记录的数据
		boolean showPoint = false;

		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		long signInDay = 0;//领奖日期
		String situation = "";//领取情况
		Map<Long, Integer> getMap = null;
		int nowDateValue = getNowDateValue(HOUR_ZERO);
		
		
		//记录的数据
		if(activityHumanDataMap != null){
			ActivityHumanData activityHumanData = activityHumanDataMap.get(0);
			if(activityHumanData != null){
				signInDay = activityHumanData.getNumValue();
				situation = activityHumanData.getStrValue();
				getMap = Utils.jsonToMapLongInt(situation);
			}
		}
		//跨天不返回上一天数据
		if(signInDay != nowDateValue){
			getMap = null;
		}
		int dayNum = getDayBetween(humanObj.getHuman().getTimeCreate(), Port.getTime(), HOUR_ZERO) + 1;//今天是开服第几天
		DActivityZoneItem.Builder dActivityZoneItem = DActivityZoneItem.newBuilder();
		Map<Integer,List<ActivityZoneItemObject>> zoneItems = activity.zoneItems;//条件数据
		int canGet = 0;
		//如果领取情况为空，则下发初始数据
		for(List<ActivityZoneItemObject> list:zoneItems.values()){
			
			for(ActivityZoneItemObject aco:list){
				ActivityParamObject params = aco.getParams();
				int dayLimit = params.numParams.get(1).intValue();//天数限制 conf.openDay
				int rewardSn = params.numParams.get(3).intValue();//奖励ID
				
				if(dayLimit != dayNum){
					continue;
				}
				DActivityParam.Builder dActivityParam = DActivityParam.newBuilder();
				long paramOnline = params.numParams.get(2);
				dActivityParam.addNumParam(paramOnline);//在线时段
				dActivityParam.addNumParam(aco.aid);
				long botton_status = EAwardType.AwardNot_VALUE;
				//查询数据
				if(humanObj.getHuman().getDailyOnlineTime() >= paramOnline*Time.MIN){
					//是否已经领取
					if(getMap != null && getMap.get(paramOnline) !=null && getMap.get(paramOnline) == EAwardType.Awarded_VALUE){
						botton_status = EAwardType.Awarded_VALUE;
					}else{
						botton_status = EAwardType.Awarding_VALUE;
						canGet++;
					}
				}
				dActivityParam.addNumParam(botton_status);
				dActivityParam.addNumParam(activity.beginTime);//活动开始时间
				dActivityParam.addNumParam(0L);//活动开始时间
				ConfRewards confRewards = ConfRewards.get(rewardSn);
				int len = confRewards.itemSn.length;
				for (int k = 0; k < len; k++) {
					int itemSn = confRewards.itemSn[k];
					int itemCount = confRewards.itemNum[k];
					DItem.Builder ditem = DItem.newBuilder();
					ditem.setItemSn(itemSn);
					ditem.setNum(itemCount);
					dActivityParam.addItems(ditem.build());
				}
				dActivityZoneItem.addActivityParams(dActivityParam.build());
			}
		}
		
		zoneList.add(dActivityZoneItem.build());		
		
		Param param = new Param();
		showPoint = canGet > 0?true:false;
		param.put("showPoint", showPoint);
		
		//同步在线时间
		SCHumanOnLineTimeMsg.Builder msg = SCHumanOnLineTimeMsg.newBuilder();
		long time = humanObj.getHuman().getDailyOnlineTime()/Time.MIN;//分钟
		msg.setOnlineTime(time);
		humanObj.sendMsg(msg);
		
		return param;
	}
	/**
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> activityParamObjectList){
		//客户端参数
		ActivityParamObject activityParamObject = activityParamObjectList.get(0);
		if(activityParamObject == null){
			return false;
		}
		long aid = 0;//领取的是哪个时段的在线奖励
		if(activityParamObject != null){
			if(activityParamObject.numParams.size() > 0){
				aid = activityParamObject.numParams.get(0);
			}
		}	
		
		int nowDateValue = getNowDateValue(HOUR_ZERO); //当天时间
		long signInDay = 0;//领奖日期v
		//创角之后多少天
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		List<ActivityZoneItemObject> activityZoneItemList = activity.zoneItems.get((int)aid);
		ActivityZoneItemObject zoneItem = activityZoneItemList.get(0);
		Map<Long, Integer> getMap = null;
		if(activityHumanDataMap != null){
			ActivityHumanData activityHumanData = activityHumanDataMap.get(0);
			signInDay = activityHumanData.getNumValue();
			getMap = Utils.jsonToMapLongInt(activityHumanData.getStrValue());//各在线时段的奖励领取情况{"5":0,"10":0,"30":0}
		}
		ActivityParamObject getActivityParamObject = zoneItem.params.get(0);//哪个时段
		long dailyOnlineTimeSecond = humanObj.getHuman().getDailyOnlineTime()/Time.MIN;//今日在线分钟
		long confMinTime = getActivityParamObject.numParams.get(2);
		//在线时间未达到
		if(confMinTime - dailyOnlineTimeSecond  >= 120000 ){
			Log.game.info("在线时间未达到");
			return false;
		}
		// 如果是后一天的就覆盖前一天的数据，只记录一天的在线礼包数据
		if(getMap == null || signInDay != nowDateValue){
			getMap = new HashMap<>();
			getMap.put(getActivityParamObject.numParams.get(2), 0);
		}
		
		//数据提交数据库和内存中数据
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		Map<Integer,String> strValues = new HashMap<>();//字符参数表
		snList.add(0);
		numValues.put(0, (long)nowDateValue);
		getMap.put(confMinTime, EAwardType.Awarded_VALUE);
		strValues.put(0, Utils.mapLongIntToJSON(getMap));
		commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
		
		//奖励
//		List<ProduceVo> proList = new ArrayList<>();
//		for(DItem dItem:getActivityParamObject.itemParams){
//			ProduceVo pro = new ProduceVo(dItem.getItemSn(),dItem.getNum());
//			proList.add(pro);
//		}
		
		int confSn = getActivityParamObject.numParams.get(3).intValue();
		ConfRewards conf = ConfRewards.get(confSn);
		if(conf == null){
			Log.table.error("ConfRewards error sn = {}",confSn);
			return false;
		}
		RewardHelper.reward(humanObj, conf.itemSn,conf.itemNum, LogSysModType.ActOnlineGift);
		
		return true;
	}
	
	/**
	 * 监听事件触发
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		HumanObject humanObj = param.get("humanObj");
		if (null == humanObj) {
			Log.human.info("===onTrigger error in humanObj=null, event={}, param={}", event, param.toString());
			return false;
		}
		
		int sid = getDayBetween(humanObj.getHuman().getTimeCreate(), Port.getTime(), HOUR_ZERO) + 1;
		if(activity.zoneItems.size() < sid){
			return false;
		}
		if(activity.zoneItems.size() < 1){
			return false;
		}
		switch (event) {
			case EventKey.HumanLoginFinishFirstToday:
			case EventKey.HumanLoginFinish:
				return true;
			// FIXME 活动，后续打开，未完全测试，暂时不敢开启————黄彬
			// 今日首次登录
			case EventKey.ResetDailyHour:
				if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset){
					return true;
				}
				break;
			case EventKey.EVERY_MIN://每分钟都推送状态
				return true;
			default:
				break;
		}
		return false;
	}
}
