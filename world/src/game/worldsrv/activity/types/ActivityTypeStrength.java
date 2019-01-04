package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.msg.Define.DItem;
import game.msg.Define.EMoneyType;
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
/**
 * 整点体力
 * @author songy
 *
 */
public class ActivityTypeStrength extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_4;	//类型
	private static final ActivityTypeStrength instance = new ActivityTypeStrength();
	//这个状态是特殊的
	private static final Long    NotTimeYet = 1L;     // 时间未到
	private static final Long    CanGet= 2L;            //可以领取
	private static final Long    AlreadyGet= 3L;         //已经领取
	private static final Long  	OutOfTime = 4L;          //超时未领取

	/**
	 * 构造函数
	 */
	private ActivityTypeStrength(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityTypeStrength.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityTypeStrength.type;
	}
	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		
		//字符串转为json对象
		JSONObject json = Utils.toJSONObject(paramStr);
		if(json == null){
			return zoneItems;
		}
		JSONArray awards = json.getJSONArray("awards");//列表
		long price = json.getLong("fundPrice");//补领价格
		for(int i=0;i<awards.size();i++){
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");//编号
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			String fundName = rewardJson.getString("fundName");//名字
			paramObj.strParams.add(fundName);
			long rankTime = rewardJson.getLong("ranktime");//开始时间小时
			paramObj.numParams.add(rankTime);
			long tlvTime = rewardJson.getLong("tlvtime");//结束时间小时
			paramObj.numParams.add(tlvTime);
			paramObj.numParams.add(price);
			
			int confRewardSn = rewardJson.getIntValue("rewardId");//奖励表对应的sn
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
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		
		//记录的数据
		Map<Integer,Long> paramList = new HashMap<>();
		if(activityData != null){
			for(Map.Entry<Integer, ActivityHumanData> entry:activityData.entrySet()){
				paramList.put(entry.getKey(), entry.getValue().getNumValue());
			}			
		}
		
		//不是今日的话，重置领取状态
		if(paramList.size()>0){
			Long day = paramList.get(0);
			if(day != null && day < getNowDateValue(HOUR_ZERO)){
				paramList.clear();
			}
		}
		
		boolean showPoint = false;
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		Calendar cal = Calendar.getInstance();	
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		for(Map.Entry<Integer, List<ActivityZoneItemObject>> entry:activity.zoneItems.entrySet()){
			ActivityZoneItemObject zoneItem = entry.getValue().get(0);
			if(zoneItem!=null){
				DActivityParam.Builder dp = DActivityParam.newBuilder();
				ActivityParamObject zoneParam = zoneItem.getParams();
				dp.addNumParam(entry.getKey());			
				for(DItem di:zoneParam.itemParams){//奖励物品
					dp.addItems(di);
				}
				String name = zoneParam.strParams.get(0);
				dp.addStrParam(name);
				Long value = paramList.get(entry.getKey());
				Long bh = zoneParam.numParams.get(0);//开始小时
				Long eh = zoneParam.numParams.get(1);//结束小时
				Long price = zoneParam.numParams.get(2);//补领价格
				//状态设置
				Long status =NotTimeYet;
				if(value!=null && value.longValue() > 0){//是否领取过了
					status = AlreadyGet;
				}
				else{
					if( bh != null && bh <= hour && hour < eh){
						//正常领取时间范围内
						status = CanGet;
						showPoint = true;
					}else if( bh != null && bh < hour && hour >= eh){
						status = OutOfTime;
					}
				}
				dp.addNumParam(status);
				
				
				if(bh !=null){
					//时间转成时间戳发给客户端
					dp.addNumParam(Utils.getTimestampTodayAssign(bh.intValue(), 0, 0));
					dp.addNumParam(Utils.getTimestampTodayAssign(eh.intValue(), 0, 0));
					dp.addNumParam(price);
				}
				else{
					dp.addNumParam(0);
					dp.addNumParam(1);	
					dp.addNumParam(2);					
				}
				dz.addActivityParams(dp.build());
			}	
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
		
		//来自客户端的参数
		ActivityParamObject params = paramList.get(0);
		if(params==null){
			return false;
		}
		long zone = 0;//第几个奖励
		boolean isCompensate = false;//是否是补领
		if(params != null){
			if(params.numParams.size()>0){
				zone = params.numParams.get(0);
			}
			if(params.numParams.size()>1){
				isCompensate = params.numParams.get(1)==1;
			}
		}
		
		//玩家身上的记录数据
		Map<Integer, ActivityHumanData> activityData= humanObj.activityDatas.get(activity.id);
		Map<Integer,Long> paramLists = new HashMap<>();
		if(activityData != null){
			for(Map.Entry<Integer, ActivityHumanData> entry:activityData.entrySet()){
				paramLists.put(entry.getKey(), entry.getValue().getNumValue());
			}			
		}
		//如果上次领取时间不是今日，领取状态清空  
		if(paramLists.size()>0){
			Long day = paramLists.get(0);
			if(day != null && day < getNowDateValue(HOUR_ZERO)){
				paramLists.clear();
			}
		}
		
		Long value = paramLists.get(Utils.intValue(zone));
		if(value!=null && value.longValue()>0){
			return false;//已经领取过了
		}
		
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get((int)zone);
		if(zoneItems==null){
			return false;
		}
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		if(zoneItem == null){
			return false;
		}
		ActivityParamObject zoneParam = zoneItem.getParams();
		Long bh = zoneParam.numParams.get(0);//开始的小时
		Long eh = zoneParam.numParams.get(1);//结束的小时
		Long price = zoneParam.numParams.get(2);//补领价格
		Calendar cal = Calendar.getInstance();	
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if(hour<HOUR_ZERO){
			hour = 24 + hour;
		}
		if(bh>hour){
			return false;//未到领取时间
		}
		if(hour >= eh){//已经过了领取时间
			if(!isCompensate){
				return false;//只能补领
			}
			// 扣元宝
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, price.intValue(), LogSysModType.ActValueGet)) {
				return false;
			}
		}
		Long day = paramLists.get(0);//记录的日期数值
		int nowDataValue = getNowDateValue(HOUR_ZERO);//当前的日期数值
		
		//数据提交数据库
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		snList.add(0);
		numValues.put(0, (long)nowDataValue);
		if(day==null || day < nowDataValue){//不是今天的，重置其他数据，并设置标志
			for(int i=1; i<= activity.zoneItems.size();++i){
				if(i==zone){
					snList.add(i);
					numValues.put(i, 1L);					
				}
				else{
					snList.add(i);
					numValues.put(i, 0L);
				}
			}
		}
		else{//是今天的，设置标志
			snList.add((int)zone);
			numValues.put((int)zone, 1L);
		}
		commitHumanActivityData(activity,humanObj,snList,numValues);
		
		//奖励
		List<ProduceVo> proList = new ArrayList<>();
		for(DItem item:zoneParam.itemParams){
			ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
			proList.add(pro);
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActValueGet);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return true;
	}
	
	/**
	 * 监听事件触发
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param) {
		if(activity.zoneItems.size()<1) {
			return false;
		}
		switch (event) {
			case EventKey.HumanLoginFinishFirstToday:
				return true;
			case EventKey.HumanLoginFinish:
				return true;
			case EventKey.ResetDailyHour: 
				int hour = param.get("hour");
				for(List<ActivityZoneItemObject> zoneItemList:activity.zoneItems.values()) {
					ActivityZoneItemObject zoneItem = zoneItemList.get(0);
					if(zoneItem != null) {
						ActivityParamObject zoneParam = zoneItem.getParams();
						long bh = zoneParam.numParams.get(0);//开始小时
						long eh = zoneParam.numParams.get(0);//开始小时
						if (bh == hour || eh == hour) {
							return true;
							
						}
					}
				}
				return false;
			default:
				break;
		}
		return false;
	}
}
