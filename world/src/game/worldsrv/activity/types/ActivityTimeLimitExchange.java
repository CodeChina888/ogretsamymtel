package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.msg.Define.DActivityParam;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivityTimeLimitExchange;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.EventKey;

// 限时兑换
public class ActivityTimeLimitExchange extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_34;	//类型
	private static final ActivityTimeLimitExchange instance = new ActivityTimeLimitExchange();//实例	
	private static final Integer persistSn = 1;
	
	private ActivityTimeLimitExchange() {
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static ActivityTypeBase getInstance(){
		return ActivityTimeLimitExchange.instance;
	}
	
	/**
	 * 获取类型
	 * @return
	 */
	public int getType(){
		return ActivityTimeLimitExchange.type;
	}
	
	/**
	 * 解析操作参数，即表
	 * @param paramStr
	 * @return
	 */
	@Override
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr){
		return  new ArrayList<>();
	}
	
	private int getExchangedTimes(JSONObject jo, int aid) {
		int times = 0;
		Integer timesObj = jo.getInteger(String.valueOf(aid));
		if (timesObj != null) {
			times = timesObj;
		}
		return times;
	}
	/**
	 * 获取给客户端的参数（不给下红点，客户端自行判断显示）
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj, List<DActivityZoneItem> zoneList){
		boolean showPoint = false;//显示小红点
		
		ActivityHumanData humanData = getHumanActivityData(humanObj, activity, persistSn);
		
		// 获取兑换次数
		JSONObject jo = Utils.toJSONObject(humanData.getStrValue());

		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		
		ConfActivityTimeLimitExchange[] confs = ConfActivityTimeLimitExchange.findArray();
		for (int i=0; i<confs.length; ++i) {
			ConfActivityTimeLimitExchange conf = confs[i];

			DActivityParam.Builder dp = DActivityParam.newBuilder();
			dp.addNumParam(conf.sn);// 编号
			
			int times = getExchangedTimes(jo, conf.sn);
			times = conf.exchangenum - times;
			if (times < 0) times = 0;
			dp.addNumParam(times);// 剩余兑换次数
			
			dz.addActivityParams(dp.build());
			
			if (times > 0) {
				showPoint = true;
			}
		}		
		zoneList.add(dz.build());	
		Param param = new Param();
		param.put("showPoint", showPoint);
		return param;
	}
	
	/**
	 * 处理客户端的执行请求(要求客户端发送要兑换的ID)
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity, HumanObject humanObj, List<ActivityParamObject> paramList){
		long aid = getParamAid(paramList);
		if (aid <= 0) {
			return false;
		}
		
		// conf
		ConfActivityTimeLimitExchange conf = ConfActivityTimeLimitExchange.get((int)aid);
		if (conf == null) {
			Log.activity.info("Can not found ConfActivityTimeLimitExchange:{}", aid);
			return false;
		}
		
		ActivityHumanData humanData = getHumanActivityData(humanObj, activity, persistSn);
		
		// 判断兑换次数
		JSONObject jo = Utils.toJSONObject(humanData.getStrValue());
		int times = getExchangedTimes(jo, (int)aid);
		if (times >= conf.exchangenum) {
			return false;
		}
		
		// get奖励配置
		ConfRewards confRewards = ConfRewards.get(conf.rewardSn);
		if (confRewards == null) {
			Log.activity.info("Can not found ConfRewards:{}", conf.rewardSn);
			return false;
		}
				
		// 消耗判断
		if (!RewardHelper.checkAndConsume(humanObj, conf.exchangeItemSn, conf.exchangeItemNum, LogSysModType.ActTimeLimitExchange)) {
			return false;
		}
		
		// 累加次数
		jo.put(String.valueOf(aid), times+1);
		commitData(activity, humanObj, jo.toJSONString(), persistSn);
		
		// 给与奖励
		ItemChange itemChange = RewardHelper.reward(humanObj, confRewards.itemSn, confRewards.itemNum, LogSysModType.ActTimeLimitExchange);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return true;
	}
	
	/**
	 * 监听事件触发（不触发）
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
