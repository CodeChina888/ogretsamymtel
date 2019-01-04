package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.msg.Define.DItem;
import game.msg.Define.ESignType;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityRegister;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.EventKey;

public class ActivityType1 extends ActivityTypeBase{
	private static final int type = ActivityTypeDefine.Activity_TYPE_1;	//类型
	private static final ActivityType1 instance = new ActivityType1();//实例	
	
	/**
	 * 构造函数
	 */
	private ActivityType1(){
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityType1.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityType1.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		return zoneItems;
	}
	
	private boolean hasSigned(HumanObject humanObject) {
		Human human = humanObject.getHuman();
		return human.getDailySignFlag() == ESignType.signed_VALUE;
	}
	
	private boolean hasNextSignItem(int signGroup, int signIndex) {
		if (signIndex == 30) {
			signGroup = signGroup + 1;
			signIndex = 1;
		} else {
			signIndex = signIndex + 1;
		}
		int sn = (signGroup + 1) * 100 + signIndex;
		ConfActivityRegister conf = ConfActivityRegister.get(sn);
		return conf != null;
	}
	/**
	 * 获取给客户端的参数
	 * 参数中zone=1时，取params.items为奖励组;params.numParam[0]为签到天数,params.numParam[1]为补签天数,params.numParam[2]==0时为本日未签到
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		int signGroup = 0;// 签到分组
		int signIndex = 0; // 签到次数
		//记录的玩家活动数据
		Map<Integer, ActivityHumanData> activityHumanDataMap= humanObj.activityDatas.get(activity.id);
		if(activityHumanDataMap != null){
			ActivityHumanData data = activityHumanDataMap.get(0);
			if(data != null){
				signGroup = (int)data.getNumValue();
			}
			data = activityHumanDataMap.get(1);
			if(data != null){
				signIndex = (int)data.getNumValue();
			}
		}
		if (signIndex == 30) {
			signGroup += 1;
			signIndex = 0;
		}
		int beginSn = (signGroup + 1) * 100 + 1;
		int endSn = beginSn + 30;
		List<ConfActivityRegister> arList = new ArrayList<>();
		for (int i=beginSn; i<endSn; ++i) {
			ConfActivityRegister ar = ConfActivityRegister.get(i);
			if (ar != null) {
				arList.add(ar);
			}
		}
		boolean signed = true;
		if (arList.size() > 0) {
			signed = hasSigned(humanObj);;
		}
		//签到的基本信息
		DActivityZoneItem.Builder dActivityZoneItem = DActivityZoneItem.newBuilder();
		dActivityZoneItem.setZone(0);
		DActivityParam.Builder dActivityParam = DActivityParam.newBuilder();	
		dActivityParam.addNumParam(signIndex);//签到次数	
		dActivityParam.addNumParam(0);//补签次数（无用）
		dActivityParam.addNumParam(signed ? 1 : 0);//今日是否已经签到到	
		dActivityParam.addNumParam(0);//补签金钱（无用）
		dActivityZoneItem.addActivityParams(dActivityParam);
		zoneList.add(dActivityZoneItem.build());
		
		boolean showPoint = !signed;
		
		//签到的奖励信息////////////////////////////////////////
		dActivityZoneItem = DActivityZoneItem.newBuilder();
		dActivityZoneItem.setZone(1);
		for(ConfActivityRegister ar:arList){
			dActivityParam = DActivityParam.newBuilder();	
			DItem.Builder dItem = DItem.newBuilder();
			ConfRewards confRewards = ConfRewards.get(ar.rewardSn);
			if(confRewards != null){
				for (int i = 0; i < confRewards.itemSn.length; i++) {
					dItem.setItemSn(confRewards.itemSn[i]);
					dItem.setNum(confRewards.itemNum[i]);
					dActivityParam.addItems(dItem);
				}
			}
			int vip = ar.vipGrade;
			int times = ar.multiple;
			if(vip==0){
				times = 1;
			}
			dActivityParam.addNumParam(vip);
			dActivityParam.addNumParam(times);
			dActivityZoneItem.addActivityParams(dActivityParam);
		}		
		zoneList.add(dActivityZoneItem.build());
		showPoint=true;
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
		int signGroup = 0;// 签到分组
		int signIndex = 0; // 签到次数
		//记录的玩家活动数据
		Map<Integer, ActivityHumanData> activityHumanDataMap= humanObj.activityDatas.get(activity.id);
		if(activityHumanDataMap != null){
			ActivityHumanData data = activityHumanDataMap.get(0);
			if(data != null){
				signGroup = (int)data.getNumValue();
			}
			data = activityHumanDataMap.get(1);
			if(data != null){
				signIndex = (int)data.getNumValue();
			}
		}
		
		// 判断是否签到
		if (hasSigned(humanObj)) {
			humanObj.sendSysMsg(480701);// 您今天已经签到过！
			return false;
		}
		
		if (signIndex == 30) {
			signGroup = signGroup + 1;
			signIndex = 1;
		} else {
			signIndex = signIndex + 1;
		}

		int sn = (signGroup + 1) * 100 + signIndex;
		ConfActivityRegister conf = ConfActivityRegister.get(sn);
		if (conf == null) {
			Log.table.error("===SignDaily配置表错误 ,no find sn={}", sn);
			return false;
		}
		ConfRewards confRewards = ConfRewards.get(conf.rewardSn);
		if (confRewards == null) {
			Log.table.error("===Rewards配置表错误 ,no find sn={}", sn);
			return false;
		}

		humanObj.getHuman().setDailySignFlag(ESignType.signed_VALUE);
		
		//数据提交数据库
		List<Integer> snList = new ArrayList<>();//索引列表
		Map<Integer,Long> numValues = new HashMap<>();//数值参数表
		snList.add(0);
		numValues.put(0, (long)signGroup);
		snList.add(1);
		numValues.put(1, (long)signIndex);
		commitHumanActivityData(activity,humanObj,snList,numValues);		
				
		int len = confRewards.itemSn.length;
		int[] itemCountArr = new int[len];
		for (int i = 0; i < len; i++) {
			if(conf.vipGrade > 0 && humanObj.getHuman().getVipLevel() >= conf.vipGrade){
				itemCountArr[i] = confRewards.itemNum[i] * conf.multiple;//vip多倍;
			}else{
				itemCountArr[i] = confRewards.itemNum[i];
			}
		}
		
		ItemChange itemChange = RewardHelper.reward(humanObj, confRewards.itemSn,itemCountArr, LogSysModType.ActDailySignIn);
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
			case EventKey.ResetDailyHour:
				if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset){
					return true;
				}
				break;
			default:
				break;
		}
		return false;
	}
}
