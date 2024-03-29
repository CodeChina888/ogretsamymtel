package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.List;

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
import game.worldsrv.config.ConfActivityAccumulateLogin;
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

// 累积登陆
public class ActivityLoginSum extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_33;	//类型
	private static final ActivityLoginSum instance = new ActivityLoginSum();//实例	
	
	//持久化 sn =1
	private static final Integer persistSn = 1;
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityLoginSum.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityLoginSum.type;
	}

	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		return new ArrayList<>();
	}
	
	private boolean hasGet(JSONObject jo, int aid) {
		int times = 0;
		Integer timesObj = jo.getInteger(String.valueOf(aid));
		if (timesObj != null) {
			times = timesObj;
		}
		return times > 0;
	}
	
	/**
	 * 获取给客户端的参数
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		boolean showPoint = false;//显示小红点
		
		// 获取存储数据
		ActivityHumanData data = getHumanActivityData(humanObj, activity, persistSn);
		JSONObject jo = Utils.toJSONObject(data.getStrValue());

		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		
		// 设置天数
		DActivityParam.Builder dps = DActivityParam.newBuilder();
		
		int loginNum = humanObj.getHuman().getLoginNum();
		dps.addNumParam(loginNum);
		dz.addActivityParams(dps.build());
		
		// 设置条目
		ConfActivityAccumulateLogin[] confs = ConfActivityAccumulateLogin.findArray();
		for (int i=0; i<confs.length; ++i) {
			ConfActivityAccumulateLogin conf = confs[i];
	
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			int aid = conf.sn;
			int num = conf.AccumulateLogin;

			long status = EAwardType.AwardNot_VALUE;
			if(!hasGet(jo, aid)){
				if(num <= loginNum){
					status = EAwardType.Awarding_VALUE;
					showPoint = true;
				}
			}else{
				status = EAwardType.Awarded_VALUE;
			}
			
			dp.addNumParam(aid);//活动编号
			dp.addNumParam(status);//1可领取，0不可领取，2已领取
			dp.addNumParam(num);//需要登陆天数

			// 奖品
			int confRewardSn = conf.rewardSn;
			ConfRewards confRewards = ConfRewards.get(confRewardSn);
			if (confRewards != null) {
				for (int j = 0; j < confRewards.itemSn.length; j++) {
					int itemSn = confRewards.itemSn[j];
					int itemCount = confRewards.itemNum[j];
					DItem.Builder ditem = DItem.newBuilder();
					ditem.setItemSn(itemSn); // 物品模板id
					ditem.setNum(itemCount); // 物品数量
					dp.addItems(ditem.build());
				}
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
		long aid = getParamAid(paramList);
		if (aid <= 0) {
			return false;
		}
		// 该活动配置
		ConfActivityAccumulateLogin conf = ConfActivityAccumulateLogin.get((int)aid);
		if (conf == null) {
			Log.activity.info("Can not found ConfActivityAccumulateLogin:{}", aid);
			return false;
		}

		// get奖励配置
		ConfRewards confRewards = ConfRewards.get(conf.rewardSn);
		if (confRewards == null) {
			Log.activity.info("Can not found ConfRewards:{}", conf.rewardSn);
			return false;
		}
		
		// 登陆累积天数判断
		Human human = humanObj.getHuman();
		if(human.getLoginNum() < conf.AccumulateLogin){
			Log.activity.info("登录天数不足，无法领取奖励: need:{} real:{}", conf.AccumulateLogin, human.getLoginNum());
			return false;
		}
		
		// 是否已经领取
		ActivityHumanData humanData = getHumanActivityData(humanObj, activity, persistSn);
		JSONObject jo = Utils.toJSONObject(humanData.getStrValue());
		if(hasGet(jo, (int)aid)){
			Log.activity.info("已经领取过该奖励");
			return false;
		}
		
		// 设置领取
		jo.put(String.valueOf(aid), 1);
		commitData(activity, humanObj, jo.toJSONString(), persistSn);
		
		// 给与奖励
		ItemChange itemChange = RewardHelper.reward(humanObj, confRewards.itemSn, confRewards.itemNum, LogSysModType.ActLoginNumSendGold);
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
		case EventKey.HumanLoginFinish:
//		case EventKey.HumanLoginFinishFirstToday:
		case EventKey.HumanFirstLogin:
			return true;
		case EventKey.ActResetDailyHour:
			if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset)
				return true;
			break;
		default:
			break;
		}
		return false;
	}
}
