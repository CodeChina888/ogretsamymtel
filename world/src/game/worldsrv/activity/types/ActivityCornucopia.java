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
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.msg.Define.DItem;
import game.msg.Define.EMoneyType;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityInfo;
import game.worldsrv.activity.ActivityInfo2;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 聚宝盆
 * @author qizheng
 *
 */
public class ActivityCornucopia extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_13;	//类型
	
	private static final ActivityCornucopia instance = new ActivityCornucopia();//实例	
	
	
	private static final int VIP_MAX_STATUS = -1;//已经达到vip最大的状态
	
	private static final int ERROR_STATUS = -2;//异常状态
	
	private ActivityCornucopia() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityCornucopia.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityCornucopia.type;
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
		
		Long getItemId = json.getLong("itemId");
		ActivityZoneItemObject zoneItemObj0 = new ActivityZoneItemObject(0);
		ActivityParamObject paramObj0 = new ActivityParamObject();
		paramObj0.numParams.add(getItemId);
		zoneItemObj0.addParam(paramObj0);	
		zoneItems.add(zoneItemObj0);
		
		
		JSONArray awards = json.getJSONArray("awards");
		for(int i=0;i<awards.size();i++){
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
			ActivityParamObject paramObj = new ActivityParamObject();
			Long need = rewardJson.getLong("need");
			paramObj.numParams.add(need);
			Long vip = rewardJson.getLong("vip");
			paramObj.numParams.add(vip);
			Long min = rewardJson.getLong("min");
			paramObj.numParams.add(min);
			Long max = rewardJson.getLong("max");
			paramObj.numParams.add(max);			
			
			DItem.Builder ditem = DItem.newBuilder();
			ditem.setItemSn(EMoneyType.gold_VALUE);
			ditem.setNum(game.worldsrv.support.Utils.getIntRandom(min.intValue(),max.intValue()));
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
		if(activity.zoneItems.size()<1){
			return null;
		}
		Map<Integer,Long> paramList = getHumanActivityDataList(activity,humanObj);
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		int sid = 1;// 获取到第几档
		if (paramList.containsKey(0)) {
			sid = toValue(paramList, 0).intValue();
		}
		
		DActivityParam.Builder dp = DActivityParam.newBuilder();
		if (!activity.zoneItems.containsKey(sid)) {
			dp.addNumParam(-1);//编号
			sid = sid - 1;
		}else{
			dp.addNumParam(sid);//编号
		}
		ActivityZoneItemObject zoneItem = activity.zoneItems.get(sid).get(0);
		if(zoneItem == null){
			return null;
		}
		ActivityParamObject zoneParam = zoneItem.getParams();
		for(DItem di:zoneParam.itemParams){
			dp.addItems(di);//奖励物品
		}
		Long num = zoneParam.numParams.get(0);//需要的元宝数
		if(num != null){
			dp.addNumParam(num);
		}else{
			dp.addNumParam(-1);
		}
		dp.addNumParam(zoneParam.numParams.get(1));//VIP
		dp.addNumParam(zoneParam.numParams.get(2));//最少可获得
		dp.addNumParam(zoneParam.numParams.get(3));//最多可获得
		
		
		List<ActivityZoneItemObject> zoneItems0 = activity.zoneItems.get(0);
		ActivityZoneItemObject object0 = zoneItems0.get(0);
		int itemId = object0.params.get(0).numParams.get(0).intValue();
		dp.addNumParam(itemId);
		//dp.addNumParam(toValue(paramList, sid));// 獲得數量
		int itemNum = 0;
		for(DItem ditem:zoneParam.itemParams){
			itemNum += ditem.getNum();
		}
		dp.addNumParam(itemNum);// 獲得數量
		dp.addNumParam(toValue(paramList, sid + 100));// 是否已經領取了		
		
		int topLevelSid = maxPrayNum(activity, humanObj.getHuman().getVipLevel());//当前vip可以获得的最大许愿次数
		long nextAddVip = nextPrayVip(activity, humanObj.getHuman().getVipLevel());
		//判断是否是最大的vip数,如果是则nextAddVip =0
		if(nextAddVip == VIP_MAX_STATUS){
			nextAddVip = 0;
		}
		//计算剩余次数
		int alreadyUse = 0;
		for(Long status:paramList.values()){
			if(status == 1){
				alreadyUse++;
			}
		}
		dp.addNumParam(topLevelSid - alreadyUse);//剩余数量
		dp.addNumParam(nextAddVip);
		
		dz.addActivityParams(dp.build());
		
		//
		DActivityZoneItem.Builder dz2 = DActivityZoneItem.newBuilder();
		List<ActivityInfo2> dataList = ActivityInfo.getDB(activity.id);
		for (int i = 0; i < dataList.size(); i++) {
			ActivityInfo2 info = dataList.get(i);
			DActivityParam.Builder dp2 = DActivityParam.newBuilder();
			dp2.addNumParam(info.dList.get(0).sn);//道具sn编号
			dp2.addNumParam(info.dList.get(0).num);//道具数量
			dp2.addStrParam(info.name);//玩家名称
			dz2.addActivityParams(dp2);
		}
		
		zoneList.add(dz.build());	
		zoneList.add(dz2.build());
		Param param = new Param();
		boolean showPoint = false;
		if(humanObj.getHuman().getGold() >= num && topLevelSid - (sid-1) > 0 ){//金币足够且有剩余次数
			showPoint = true;
		}
		param.put("showPoint", showPoint);
		return param;
	}
	public Long toValue(Map<Integer,Long> paramList,int id){
		Long toValue = paramList.get(id);
		
		//处理数据库还没数据时的情况
		if (id > 100 && toValue == null) {
			toValue = 1l;
		}
		if (toValue == null) {
			toValue = 0l;
		}
		return toValue;
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
	 * 处理客户端的执行请求
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		ActivityParamObject params = paramList.get(0);
		if(params == null){
			return false;
		}
		Long zone = 0l;
		if(params != null){
			if(params.numParams.size()>0){
				zone = params.numParams.get(0);
			}
		}
		int zoneInt = zone.intValue();
		Map<Integer,Long> paramLists = getHumanActivityDataList(activity,humanObj);
		// 存储
		Map<Integer, String> strValues = new HashMap<Integer, String>();
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		
		
		if (zoneInt > 100) {// 領取獎勵
			// 判断是否满足领取奖励
			if (toValue(paramLists, zoneInt) == 1l) {
				return false;
			}
			Map<Integer, ActivityHumanData> activityData = humanObj.activityDatas.get(activity.id);
			if (activityData == null) {
				return false;
			}
			
			String produceStr = activityData.get(zoneInt % 100).getStrValue();
			List<ProduceVo> list = getPro(Utils.strToIntList(produceStr));
			
			snList.add(0);
			snList.add(zoneInt);
			snList.add(zoneInt % 100 + 1);
			
			numValues.put(0, zoneInt % 100 + 1l);
			numValues.put(zoneInt, 1L);
			numValues.put(zoneInt % 100 + 1, 0l);
			
			commitHumanActivityData(activity,humanObj,snList,numValues);
			ItemChange itemChange = RewardHelper.reward(humanObj, list, LogSysModType.ActWishing);
			ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
			ActivityInfo.saveActivity(activity.id,humanObj.name,list);// 存入数据
		}else{
//			if (toValue(paramLists, zoneInt) != 0l) {
//				return false;
//			}
			if(toValue(paramLists, 0) != 0l &&  toValue(paramLists, 0) != zone) {
				return false;
			}
			if (paramLists.containsKey(100 + zoneInt)) {
				return false;
			}
			// 获取这个活动的Aid配置
			List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(zoneInt);
			if(zoneItems==null){
				return false;
			}
			ActivityZoneItemObject zoneItem = zoneItems.get(0);
			if(zoneItem == null){
				return false;
			}
			List<ActivityZoneItemObject> zoneItems0 = activity.zoneItems.get(0);
			if (zoneItems0 == null) {
				return false;
			}
			ActivityZoneItemObject object0 = zoneItems0.get(0);
			int itemId = object0.params.get(0).numParams.get(0).intValue();
			//没达到配置的需求
			ActivityParamObject zoneParam = zoneItem.params.get(0);
			Long vipParam = zoneParam.numParams.get(1);
			if (humanObj.getHuman().getVipLevel() < vipParam) {
				return false;
			}
			
			Long numParam = zoneParam.numParams.get(0);
			// 扣元宝
			if (!RewardHelper.checkAndConsume(humanObj, itemId, numParam.intValue(), LogSysModType.ActWishing)) {
				return false;
			}
			
			//奖励
			int[] items = new int[zoneParam.itemParams.size()];
			int[] nums = new int[zoneParam.itemParams.size()];
			//int[] items = new int[1];
			//int[] nums = new int[1];
			
			int i = 0;
			for(DItem ditem:zoneParam.itemParams){
				items[i] = ditem.getItemSn();
				nums[i] = ditem.getNum();
				i++;
			}
			//items[0] = EMoneyType.gold_VALUE;
			//Long min = zoneParam.numParams.get(2);
			//Long max = zoneParam.numParams.get(2);
			//nums[0] = game.worldsrv.support.Utils.getIntRandom(min.intValue(),max.intValue());
			
			
			List<ProduceVo> addNewMerge = RewardHelper.addNewMerge(items, nums);
			
			snList.add(0);// 设置档位
			snList.add(zoneInt);
			snList.add(zoneInt + 100);
			
			int num = 0;
			for(ProduceVo produceVo:addNewMerge){
				num = num + produceVo.num;
			}
			
			int rewardZoneInt = zoneInt + 100;
			numValues.put(0,zone);// 
			numValues.put(zoneInt, num + 0l);//獲得數量
			numValues.put(rewardZoneInt, 0l);//是否已领取
			
			String dpString = getStringToListDProduce(addNewMerge);
			strValues.put(0, Port.getTime()+"");
			strValues.put(zoneInt, dpString);			
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
			
			return getRewards(rewardZoneInt, activity, humanObj);
		}
		
		return true;
	}
	private boolean getRewards(int zoneInt, ActivityObject activity, HumanObject humanObj){
		Map<Integer,Long> paramLists = getHumanActivityDataList(activity,humanObj);
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		// 判断是否满足领取奖励
		if (toValue(paramLists, zoneInt) == 1l) {
			return false;
		}
		Map<Integer, ActivityHumanData> activityData = humanObj.activityDatas.get(activity.id);
		if (activityData == null) {
			return false;
		}
		
		String produceStr = activityData.get(zoneInt % 100).getStrValue();
		List<ProduceVo> list = getPro(Utils.strToIntList(produceStr));
		
		snList.add(0);
		snList.add(zoneInt);
		snList.add(zoneInt % 100 + 1);
		
		numValues.put(0, zoneInt % 100 + 1l);
		numValues.put(zoneInt, 1L);
		numValues.put(zoneInt % 100 + 1, 0l);
		
		commitHumanActivityData(activity,humanObj,snList,numValues);
		ItemChange itemChange = RewardHelper.reward(humanObj, list, LogSysModType.ActWishing);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		ActivityInfo.saveActivity(activity.id,humanObj.name,list);// 存入数据

		return true;
	}
	
	/**
	 * 监听事件触发
	 * 	每日首次登录，0点为刷新时间。
			有足够的钻石许愿时  客户端自行处理
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param){
		switch (event) {
		case EventKey.HumanLoginFinishFirstToday:
		case EventKey.HumanLoginFinish:
			return true;
		case EventKey.VipLvChange:
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
	public String getStringToListDProduce(List<ProduceVo> list){
		if(list.size() == 0){
			return "";
		}
		
		String result = "";
		for(ProduceVo vo : list){
			result += (vo.sn+ ","+vo.num+","); 
		}
		return result.substring(0, result.length() - 1);
	}
	public List<ProduceVo> getPro(List<Integer> list){
		List<ProduceVo> voList = new ArrayList<>();
		for (int i = 0; i < list.size()/2; i++) {
			ProduceVo vo = new ProduceVo(list.get(i * 2), list.get(i*2 + 1));
			voList.add(vo);
		}
		return voList;
	}
	

	/**
	 * 下一次能摇奖的VIP等级
	 * @param activity
	 * @param viplv 玩家vip等级
	 * @return 正常返回vip等级 达到最大则返回-1(VIP_MAX_STATUS) 异常返回-2
	 */
	private int nextPrayVip(ActivityObject activity,int viplv){
		int nextVip = ERROR_STATUS;
		boolean isMax = false;

		Map<Integer,List<ActivityZoneItemObject>> map = activity.zoneItems;
		
		Tohere:
		for(List<ActivityZoneItemObject> acList:map.values()){
			for(ActivityZoneItemObject ao:acList){
				List<ActivityParamObject> params = ao.params;
				for(ActivityParamObject param:params){
					if(param.numParams !=null && param.numParams.size() < 4){//小于4的是冗余数据
						continue;
					}
					Long vipneed = param.numParams.get(1);
					if(viplv < vipneed){
						nextVip = vipneed.intValue();
						break Tohere;
					}else{
						continue;
					}
				}
			}
		}
		
		if(nextVip == ERROR_STATUS){
			nextVip = VIP_MAX_STATUS;
		}
		return nextVip;
	} 
	
	
	/**根据配计算出该vip等级能获取的最大的次数*/
	private int maxPrayNum(ActivityObject activity,int viplv){
		int num = 0;
		Map<Integer,List<ActivityZoneItemObject>> map = activity.zoneItems;
		for(List<ActivityZoneItemObject> acList:map.values()){
			for(ActivityZoneItemObject ao:acList){
				List<ActivityParamObject> params = ao.params;
				for(ActivityParamObject param:params){
					if(param.numParams !=null && param.numParams.size() < 4){//小于4的是冗余数据
						continue;
					}
					long vipneed = param.numParams.get(1);
					if(viplv >= vipneed){
						num++;
					}
				}
			}
		}
		return num;
	} 
}
