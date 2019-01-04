package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.EAwardType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityLoginWelfare;
import game.worldsrv.config.ConfActivityWeekWelfare;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;


/**
 * 每日福利
 * 
 * 1.登陆福利 [只能领取当前vip等级的，例如vip6只能领取vip6的礼包]
 * 
 * 2.每周福利 [vip6可以领取vip6以下的所有礼包] {""}
 */
public class ActivityLoginWelfare extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_23; // 类型
	private static final ActivityLoginWelfare instance = new ActivityLoginWelfare();

	/**
	 * 条件参数映射
	 */
	private static int MINT_LV = 0;
	private static int REWARDSN = 1;
	/**
	 * 每周活动的AID都+100
	 */
	private static int WEEK_AID_START = 100;

	/**
	 * 构造函数
	 */
	private ActivityLoginWelfare() {
	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static ActivityTypeBase getInstance() {
		return ActivityLoginWelfare.instance;
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	public int getType() {
		return ActivityLoginWelfare.type;
	}
	
	
	
	
	/**
	 * 解析操作参数
	 * 
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr) {
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		// 初始化登陆福利
		Collection<ConfActivityLoginWelfare> conf_Collection = ConfActivityLoginWelfare.findAll();
		for (ConfActivityLoginWelfare conf : conf_Collection) {
			{
				if (conf == null) {
					Log.table.error("initOperateParam ConfActivityLevelPack error");
					continue;

				}
				int aid = conf.sn;
				ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid);
				ActivityParamObject paramObj = new ActivityParamObject();

				// 从配置表取出参数
				Long minTLv = Utils.longValue(conf.minTLv);
				int confRewardSn = conf.rewardSn;// 奖励表对应的sn
				// 设置参数只能用静态变量
				paramObj.numParams.add(MINT_LV, minTLv);
				paramObj.setItemByRewardSn(confRewardSn);

				zoneItemObj.addParam(paramObj);
				zoneItems.add(zoneItemObj);
			}
		}

		// 初始化 每周福利
		Collection<ConfActivityWeekWelfare> conf_week = ConfActivityWeekWelfare.findAll();
		for (ConfActivityWeekWelfare conf : conf_week) {
			{
				if (conf == null) {
					Log.table.error("initOperateParam ConfActivityLevelPack error");
					continue;

				}
				int aid = conf.sn;
				ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(aid + WEEK_AID_START);
				ActivityParamObject paramObj = new ActivityParamObject();

				// 从配置表取出参数
				Long minTLv = Utils.longValue(conf.minTLv);
				Long costItemSn = Utils.longValue(conf.costItemSn);
				Long costItemNum = Utils.longValue(conf.costItemNum);
				Long getNum = Utils.longValue(conf.getnum);
				// 设置参数只能用静态变量
				paramObj.numParams.add(minTLv);
				paramObj.numParams.add(costItemSn);
				paramObj.numParams.add(costItemNum);
				paramObj.numParams.add(getNum);
				
				paramObj.setItemByRewardSn(conf.rewardSn);
				zoneItemObj.addParam(paramObj);
				zoneItems.add(zoneItemObj);
			}
		}

		return zoneItems;
	}

	/**
	 * 初始化玩家该活动数据
	 */
	public void initData(ActivityObject activity,HumanObject humanObj){
		JSONObject json = new JSONObject();
		Collection <ConfActivityLoginWelfare> conf_collection = ConfActivityLoginWelfare.findAll();
		for(ConfActivityLoginWelfare conf : conf_collection){
			if(humanObj.getHuman().getVipLevel() >= conf.minTLv) {
				json.put(String.valueOf(conf.sn), EAwardType.Awarding_VALUE);
			}else {
				json.put(String.valueOf(conf.sn), EAwardType.AwardNot_VALUE);
			}
		}
		
		JSONObject week_json = new JSONObject();/*{"aid":"num"}*/
		Collection <ConfActivityWeekWelfare> conf_week_collection = ConfActivityWeekWelfare.findAll();
		for(ConfActivityWeekWelfare conf : conf_week_collection){
			week_json.put(String.valueOf(conf.sn+WEEK_AID_START), EAwardType.AwardNot_VALUE);
		}
		
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		Map<Integer,String> strValues = new HashMap<>();
		snList.add(Login_Key);
		snList.add(WEEK_AID_START);
		
		numValues.put(Login_Key, 0L);
		numValues.put(WEEK_AID_START, 0L);
		
		strValues.put(Login_Key,json.toJSONString());
		strValues.put(WEEK_AID_START,week_json.toJSONString());
		commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
		
		
		Log.activity.info("[init data finish humanId={}",humanObj.getHumanId());
	}
	
	

	/**
	 * 获取给客户端的参数
	 * 
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList) {
		if (activity.zoneItems.size() < 1) {
			return null;
		}

		DActivityZoneItem.Builder login_dActivityZoneItem = DActivityZoneItem.newBuilder();
		DActivityZoneItem.Builder week_dActivityZoneItem = DActivityZoneItem.newBuilder();

		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		
		if (activityHumanDataMap == null) {
			initData(activity, humanObj);
			//重新获取一下
			activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		}
		int humanVip = humanObj.getHuman().getVipLevel();
		
		//重置每天状态
		resetStatus(activityHumanDataMap,humanVip);
		
		JSONObject login_status = Utils.toJSONObject(activityHumanDataMap.get(Login_Key).getStrValue());
		JSONObject week_status = Utils.toJSONObject(activityHumanDataMap.get(WEEK_AID_START).getStrValue());
		
		Map<Integer, List<ActivityZoneItemObject>> zoneItems = activity.zoneItems;// 条件数据
		int cangetNum = 0;//可领取的标识
		for (List<ActivityZoneItemObject> list : zoneItems.values()) {

			for (ActivityZoneItemObject aco : list) {
				ActivityParamObject params = aco.getParams();
				int aid = aco.aid;
				
				int login_welfare_status = login_status.getIntValue(String.valueOf(aid));
				Long login_welfare_vip = 0L;
				List<DItem> login_welfare_item = new ArrayList<>();
				
				int week_welfare_status = EAwardType.AwardNot_VALUE;
				Long week_vip = 0L;
				List<DItem> week_welfare_item = new ArrayList<>();
				Long cost_itemSn = 0L;
				Long cost_itemNum = 0L;
				Long getNum = 0L;
				// 登陆福利部分
				if (aid < 100) {
					login_welfare_vip = params.numParams.get(0);
					login_welfare_item = params.itemParams;
					// 登陆福利部分
					DActivityParam.Builder login_dActivityParam = DActivityParam.newBuilder();
					login_dActivityParam.addAllItems(login_welfare_item);
					login_dActivityParam.addNumParam(aid);//aid
					login_dActivityParam.addNumParam(login_welfare_vip);
					login_dActivityParam.addNumParam(login_welfare_status);
					if(humanVip == login_welfare_vip && login_welfare_status == EAwardType.Awarding_VALUE){
						cangetNum++;
					}
					login_dActivityZoneItem.addActivityParams(login_dActivityParam.build());
				} else {
					// 周末奖励部分
					week_welfare_item = params.itemParams;
					week_vip = params.numParams.get(0);
					cost_itemSn = params.numParams.get(1);
					cost_itemNum = params.numParams.get(2);
					getNum = params.numParams.get(3);//已领取次数
					Long buyNum = week_status.getLongValue(String.valueOf(aid));
					// 每周福利部分
					DActivityParam.Builder week_dActivityParam = DActivityParam.newBuilder();
					week_dActivityParam.addAllItems(week_welfare_item);// 物品
					week_dActivityParam.addNumParam(aid);//aid
					week_dActivityParam.addNumParam(week_vip);// vip等级
					week_dActivityParam.addNumParam(cost_itemSn);// 花费物品
					week_dActivityParam.addNumParam(cost_itemNum);// 数量
					
					// 剩余可领取次数
					long levenum = getNum-buyNum;
					if(humanVip >= week_vip.intValue() && levenum > 0 ){
						week_welfare_status = EAwardType.Awarding_VALUE;
					}
					if(humanVip >= week_vip.intValue() && levenum == 0 ){
						week_welfare_status = EAwardType.Awarded_VALUE;
					}
					
					//红点显示
					if(week_vip >= humanVip && week_welfare_status == EAwardType.Awarding_VALUE){
						cangetNum++;
					}

					
					
					week_dActivityParam.addNumParam(week_welfare_status);// 状态
					week_dActivityParam.addNumParam(levenum);
					week_dActivityZoneItem.addActivityParams(week_dActivityParam.build());
				}

			}
		}

		zoneList.add(login_dActivityZoneItem.build());
		zoneList.add(week_dActivityZoneItem.build());
		Param param = new Param();
		// 记录的数据
		boolean showPoint = cangetNum > 0?true:false;
		param.put("showPoint", showPoint);
		return param;
	}

	
	private Integer Login_Key = 1;
	private Integer Wekk_Key = 100;
	
	/**
	 * 处理客户端的执行请求
	 * 
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList) {
		// 客户端参数
		ActivityParamObject params = paramList.get(0);
		if (params == null) {
			return false;
		}
		Long aid = params.numParams.get(0);
		// 记录的数据
		Map<Integer, ActivityHumanData> activityData = humanObj.activityDatas.get(activity.id);
		if(activityData == null){
			initData(activity, humanObj);
		}
		if(aid<100){
			
			ActivityHumanData humandata = activityData.get(Login_Key);
			JSONObject jo = Utils.toJSONObject(humandata.getStrValue());
			List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(aid.intValue());
			ActivityZoneItemObject zoneItem = zoneItems.get(0);
			ActivityParamObject zoneParam = zoneItem.params.get(0);
			Long viplv = zoneParam.numParams.get(0);//改项所需VIP等级
			
			
			//可以领取,判断vip等级
			if(humanObj.getHuman().getVipLevel() >= viplv){
				//判断之前是否领取
				String isGet = jo.getString(String.valueOf(aid));
				Integer getStatus = Utils.intValue(isGet) ;
				
				if (getStatus != EAwardType.Awarding_VALUE ){
					//不是可领取状态则返回
					Log.activity.info("");
					return false;
				} 
				//发奖励
				ConfActivityLoginWelfare conf = ConfActivityLoginWelfare.get(aid.intValue());
				if(conf == null){
					Log.table.error("conf error sn ={}",aid);
					return false;
				}
				
				ConfRewards confReward = ConfRewards.get(conf.rewardSn);
				if (confReward != null) {
					RewardHelper.reward(humanObj, confReward.itemSn, confReward.itemNum, LogSysModType.PKMirrorWin);
				}
				jo.put(String.valueOf(aid), EAwardType.Awarded_VALUE);
				humandata.setStrValue(jo.toJSONString());
				humandata.setNumValue(Port.getTime());
		
			}
		}
		else{
			//每周福利
			ActivityHumanData humandata = activityData.get(Wekk_Key);
			JSONObject jo = Utils.toJSONObject(humandata.getStrValue());
			List<ActivityZoneItemObject> zoneItems = activity.zoneItems.get(aid.intValue());
			ActivityZoneItemObject zoneItem = zoneItems.get(0);
			ActivityParamObject zoneParam = zoneItem.params.get(0);
			Long viplv = zoneParam.numParams.get(0);//改项所需VIP等级
			Long cost_itemSn = zoneParam.numParams.get(1);
			Long cost_itemNum = zoneParam.numParams.get(2);
			Long maxNum = zoneParam.numParams.get(3);
			//可以领取,判断vip等级
			if(humanObj.getHuman().getVipLevel() >= viplv){
				//之前领取次数
				int getNum = jo.getIntValue(String.valueOf(aid));
				//领取次数上限
				if(maxNum > getNum){
					//设置次数
					jo.put(String.valueOf(aid),getNum+1);
					humandata.setStrValue(jo.toJSONString());
					//发奖励
					ConfActivityWeekWelfare conf = ConfActivityWeekWelfare.get(aid.intValue()-Wekk_Key);
					if(conf == null){
						Log.table.error("conf error sn ={}",aid);
						return false;
					}
					
					ConfRewards confReward = ConfRewards.get(conf.rewardSn);
					RewardHelper.checkAndConsume(humanObj, cost_itemSn.intValue(), cost_itemNum.intValue(),LogSysModType.Activity);
					if (confReward != null) {
						RewardHelper.reward(humanObj, confReward.itemSn, confReward.itemNum, LogSysModType.PKMirrorWin);
					}
					humandata.setStrValue(jo.toJSONString());
					humandata.setNumValue(Port.getTime());
				}
		
			}
		}
		return true;
	}

	
	
	/**
	 * 监听事件触发
	 * 
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param) {
		if(activity.zoneItems.size() < 1){
			return false;
		}
		HumanObject humanObj = param.get("humanObj");
		if (null == humanObj) {
//			Log.human.error("===onTrigger error in humanObj=null, event={}, param={}", event, param.toString());
			return false;
		}
		
		switch (event) {
			case EventKey.HumanLoginFinishFirstToday:
			case EventKey.HumanLoginFinish:
				return true;
			case EventKey.VipLvChange:
//			case EventKey.ResetDailyHour:
				int viplv = humanObj.getHuman().getVipLevel();
				//取出aid
				Collection <ConfActivityLoginWelfare> conf_c = ConfActivityLoginWelfare.findAll();
				int aid = 0;
				for(ConfActivityLoginWelfare conf:conf_c){
					if(conf.minTLv == viplv){
						aid = conf.sn;
					}
				}
				if(aid == 0){
					Log.activity.info("error aid=0");
					return false;
				}
				//获取玩家数据
				Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
				//登陆福利部分将不可领取的部分更改为可领取 TODO
	//			if(activityHumanDataMap == null){
	//				Log.activity.error("activityHumanDataMap is null");
	//				return false;
	//			}
				ActivityHumanData login_data = activityHumanDataMap.get(Login_Key);
				if(login_data == null){
					Log.activity.error("login_data is null");
					return false;
				}
				JSONObject json = Utils.toJSONObject(login_data.getStrValue());
				String aidStr = String.valueOf(aid);
				int oldType = json.getInteger(aidStr);
				if(oldType == EAwardType.AwardNot_VALUE){
					json.put(aidStr, EAwardType.Awarding_VALUE);
				}
				login_data.setStrValue(json.toJSONString());
				//每周福利
				ActivityHumanData week_data = activityHumanDataMap.get(Wekk_Key);
				return true;
		}
		return false;
	}
	
	/**
	 * 重置
	 * @param activityHumanDataMap
	 * @param vip
	 */
	private void resetStatus(Map<Integer, ActivityHumanData> activityHumanDataMap,int vip) {
		Log.activity.debug("开始重置");
		ActivityHumanData ldata =activityHumanDataMap.get(Login_Key);
		if (ldata == null)
			return;
		ActivityHumanData wdata =activityHumanDataMap.get(WEEK_AID_START);
		Long loginTime = ldata.getNumValue();
		Long weekTime =wdata.getNumValue();
		
		//隔天
		if(Utils.getDaysBetween(loginTime, Port.getTime()) >= 1&& loginTime!=0){
			String jsonStr = resetLoginJson(vip).toJSONString();
			ldata.setStrValue(jsonStr);
		}
		//不是同一周
		if(!Utils.isSameWeekChina(weekTime, Port.getTime()) && weekTime != 0){
			String jsonStr = resetWeekJson(vip).toJSONString();
			wdata.setStrValue(jsonStr);
		}
	}

	
	
	
	
	/**
	 * 基于VIP等级初始化每天礼包li
	 * @param viplv
	 * @return
	 */
	private JSONObject resetLoginJson(int viplv){
		JSONObject json = new JSONObject();
		Collection <ConfActivityLoginWelfare> conf_collection = ConfActivityLoginWelfare.findAll();
		for(ConfActivityLoginWelfare conf : conf_collection){
			if(conf.minTLv == viplv){
				json.put(String.valueOf(conf.sn), EAwardType.Awarding_VALUE);
			}else {
				json.put(String.valueOf(conf.sn), EAwardType.AwardNot_VALUE);
			}
		}
		return json;
	}
	
	/**
	 * 基于VIP等级初始化每周福利json
	 */
	private JSONObject resetWeekJson(int viplv){
		JSONObject week_json = new JSONObject();/*{"aid":"num"}*/
		Collection <ConfActivityWeekWelfare> conf_week_collection = ConfActivityWeekWelfare.findAll();
		for(ConfActivityWeekWelfare conf : conf_week_collection){
				week_json.put(String.valueOf(conf.sn+WEEK_AID_START), 0);
		}
		return week_json;
	}
}
