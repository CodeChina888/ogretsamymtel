package game.worldsrv.activity.types;

import core.support.Param;
import core.support.Utils;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EMoneyType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityLevelPack;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.EventKey;

import java.util.*;

/**
 * 等级礼包
 */
public class ActivityLevelPack extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_3;	//类型
	private static final ActivityLevelPack instance = new ActivityLevelPack();

	/**
	 * 条件参数映射
	 */
	private static int MINT_LV = 0;


	/**
	 * 构造函数
	 */
	private ActivityLevelPack(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityLevelPack.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityLevelPack.type;
	}



	/**
	 * 解析操作参数
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();

		Collection<ConfActivityLevelPack> conf_Collection = ConfActivityLevelPack.findAll();
		for(ConfActivityLevelPack conf:conf_Collection){
			{
				if(conf == null){
					Log.table.error("initOperateParam ConfActivityLevelPack error");
					continue;

				}
				int aid = conf.sn;
				ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
				ActivityParamObject paramObj = new ActivityParamObject();

				//从配置表取出参数
				Long minTLv = Utils.longValue(conf.minTLv);
				int confRewardSn = conf.rewardSn;//奖励表对应的sn
				//设置参数只能用静态变量
				paramObj.numParams.add(1L);
				paramObj.numParams.add(MINT_LV,minTLv);
				
				paramObj.setItemByRewardSn(confRewardSn);



				zoneItemObj.addParam(paramObj);
				zoneItems.add(zoneItemObj);
			}
		}
		return zoneItems;
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
//		if(paramList.size()>0){
//			Long day = paramList.get(0);
//			if(day != null && day < getActivityBeginDateValue(activity)){
//				paramList.clear();
//			}
//		}
		return paramList;
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
		//记录的数据
		boolean showPoint = false;
		Map<Integer,Long> paramList = getHumanActivityDataList(activity,humanObj);
		
		DActivityZoneItem.Builder dActivityZoneItem = DActivityZoneItem.newBuilder();
		dActivityZoneItem.setZone(1);
		for(Map.Entry<Integer, List<ActivityZoneItemObject>> entry:activity.zoneItems.entrySet()){
			ActivityZoneItemObject zoneItem = entry.getValue().get(0);
			if(zoneItem != null){
				DActivityParam.Builder dActivityParam = DActivityParam.newBuilder();
				dActivityParam.addNumParam(entry.getKey());//编号
				ActivityParamObject zoneParam = zoneItem.getParams();
				for(DItem di:zoneParam.itemParams){
					dActivityParam.addItems(di);//奖励物品
				}
				Long value = paramList.get(entry.getKey());
				if(value != null && value.longValue() > 0){
					dActivityParam.addNumParam(1);//已经领取
				}else{
					dActivityParam.addNumParam(0);//未领取
				}
				Long type = zoneParam.numParams.get(0);//要求类型
			
				Long	Num = zoneParam.numParams.get(0);//要求数据
			
				if(type != null){
					dActivityParam.addNumParam(type);
					dActivityParam.addNumParam(Num);
					switch (type.intValue()) {
					case 1:
						if((value == null || value.intValue() < 1) && humanObj.getHuman().getLevel() >= Num.intValue()){
							showPoint = true;
						}
						break;
					default:
						break;
					}
				}else{
					dActivityParam.addNumParam(1);
					dActivityParam.addNumParam(0);	
					if(value == null || value.intValue() < 1){
						showPoint = true;
					}
				}
				dActivityZoneItem.addActivityParams(dActivityParam.build());
			}
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
		//客户端参数
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
		
		//记录的数据
		Map<Integer, ActivityHumanData> activityData = humanObj.activityDatas.get(activity.id);
		Map<Integer,Long> paramLists = getHumanActivityDataList(activity,humanObj);
		
		Long value = paramLists.get((int)zone);
		if(value!=null && value.longValue()>0){//已经领取过了
			return false;
		}
		
		List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get((int)zone);
		if(zoneItems==null){
			return false;
		}
		ActivityZoneItemObject zoneItem = zoneItems.get(0);
		if(zoneItem == null){
			return false;
		}
		ActivityParamObject zoneParam = zoneItem.params.get(0);
		long num = zoneParam.numParams.get(MINT_LV);//需求数值
		if(humanObj.getHuman().getLevel() < num){
			return false;
		}

		//数据提交数据库
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		if(activityData == null){
			snList.add(0);
			numValues.put(0, (long)getActivityBeginDateValue(activity));
		}
		snList.add( (int)zone);
		numValues.put( (int)zone, 1L);
		commitHumanActivityData(activity,humanObj,snList,numValues);
				
		//奖励
		List<ProduceVo> proList = new ArrayList<>();
		for(DItem item:zoneParam.itemParams){
			ProduceVo pro = new ProduceVo(item.getItemSn(),item.getNum());
			proList.add(pro);
		}
		ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActLvUpGift);
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
		if(activity.zoneItems.size() < 1){
			return false;
		}
		switch (event) {
			case EventKey.HumanLoginFinishFirstToday:
			case EventKey.HumanLoginFinish:
				return true;
			case EventKey.HumanLvUp:
				HumanObject humanObj = param.get("humanObj");
				int lvOld = param.get("lvOld");
				Map<Integer,Long> paramList = getHumanActivityDataList(activity,humanObj);
				
				int lvCur = humanObj.getHuman().getLevel();
				//判断是否已经全部领取完了
				for(List<ActivityZoneItemObject> zoneItemList:activity.zoneItems.values()){
					ActivityZoneItemObject zoneItem = zoneItemList.get(0);
					if(zoneItem!=null){
						ActivityParamObject zoneParam = zoneItem.getParams();
						Long value = paramList.get(zoneItem.aid);
						if(value==null || value.longValue() < 1){
							Long type = zoneParam.numParams.get(0);//要求类型
							Long num = zoneParam.numParams.get(1);//要求数据
							if(type !=null){
								if(type == 1L && num.intValue() >= lvOld && num.intValue() <= lvCur ){
									return true;
								}
							}
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
