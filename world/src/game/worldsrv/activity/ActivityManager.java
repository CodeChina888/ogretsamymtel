package game.worldsrv.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Chunk;
import core.Port;
import core.Record;
import core.connsrv.ConnectionProxy;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.MsgIds;
import game.msg.Define.DActivity;
import game.msg.Define.DActivityParam;
import game.msg.Define.DSevenLoginAwardType;
import game.msg.Define.EAwardType;
import game.msg.Define.EInformType;
import game.msg.MsgActivity.SCActivityCommitReturn;
import game.msg.MsgActivity.SCActivityInfo;
import game.msg.MsgActivitySeven.SCSevenLogin;
import game.worldsrv.activity.types.ActivityType5;
import game.worldsrv.activity.types.ActivityTypeDefine;
import game.worldsrv.activity.types.ActivityTypeManager;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfActivity;
import game.worldsrv.config.ConfSevenLogin;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.PayLog;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.inform.InformManager;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class ActivityManager  extends ManagerBase{
	public ActivityTypeManager typeManager = new ActivityTypeManager();
	/** 单次查询的数据条目 **/
	public static final int pageNum = 1000;
	
	/**
	 * 获取实例
	 */
	public static ActivityManager inst() {
		return inst(ActivityManager.class);
	}
	
	/**
	 * 加载玩家数据
	 */
	@Listener(value = EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}		
		
		DB db = DB.newInstance(ActivityHumanData.tableName);
		db.findBy(false, ActivityHumanData.K.HumanId, humanObj.getHumanId());
		db.listenResult(this::_result_loadHumanActivity,  "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanActivity(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");		
		//结果
		List<Record> records = results.get();
		for(Record re:records) {
			ActivityHumanData new_data = new ActivityHumanData(re);
			Map<Integer, ActivityHumanData> dataList = humanObj.activityDatas.get(new_data.getActivityId());
			if(dataList == null){
				dataList = new HashMap<>();
				//将数据按活动ID分类放到玩家活动数据管理中.
				humanObj.activityDatas.put(new_data.getActivityId(), dataList);
			}
			ActivityHumanData old_data = dataList.get(new_data.getSn());
			if(humanObj.activityDatas.containsKey(new_data.getActivityId()) && old_data != null &&new_data.getSn() == old_data.getSn() ) {
				if(new_data.getActivityId() == 29) {
					JSONObject jo = Utils.toJSONObject(new_data.getStrValue());//new
					Map<Integer,Integer> back = Utils.jsonToMapIntInt(jo.getString("Back"));
					JSONObject jo1 = Utils.toJSONObject(old_data.getStrValue());//old
					Map<Integer,Integer> back1 = Utils.jsonToMapIntInt(jo1.getString("Back"));
					for(Entry<Integer, Integer> entry : back1.entrySet()) {
						Integer gets = back.get(entry.getKey());
						if(gets != null) {
							back.put(entry.getKey(), gets + entry.getValue());
						}else {
							back.put(entry.getKey(), entry.getValue());
						}
					}
					jo.put("Back", Utils.mapIntIntToJSON(back));
					new_data.setStrValue(jo.toJSONString());
				}
				if(new_data.getActivityId() == 32) {
					if(new_data.getStrValue().length() < 3) {
						continue;
					}
					JSONObject jo = Utils.toJSONObject(new_data.getStrValue());//new
//					Map<Integer,Integer> day = Utils.jsonToMapIntInt(jo.getString("day"));
					JSONObject jo1 = Utils.toJSONObject(old_data.getStrValue());//old
//					Map<Integer,Integer> day1 = Utils.jsonToMapIntInt(jo1.getString("day"));
//					for(Entry<Integer, Integer> entry : day.entrySet()) {
//						if(day1.containsKey(entry.getKey())) {
//							day1.put(entry.getKey(), day.get(entry.getKey())+day1.get(entry.getKey()));
//						}else {
//							day1.put(entry.getKey(), entry.getValue());
//						}
//					}
//					jo.put("day", Utils.mapIntIntToJSON(day1));
					
					Map<Integer,Integer> pay = Utils.jsonToMapIntInt(jo.getString("pay"));
					Map<Integer,Integer> pay1 = Utils.jsonToMapIntInt(jo1.getString("pay"));
					pay.putAll(pay1);
					jo.put("pay", Utils.mapIntIntToJSON(pay));
					
					
					Map<Integer,Integer> day1 = new HashMap<>();
					day1.put(3,pay.size());
					jo.put("day", Utils.mapIntIntToJSON(day1));
					List<Integer> get = Utils.strToIntList((jo.getString("get")));
					List<Integer> get1 = Utils.strToIntList((jo1.getString("get")));
					get.addAll(get1);
					jo.put("get", Utils.ListIntegerToStr(get));
					new_data.setStrValue(jo.toJSONString());
				}
				if(new_data.getActivityId() == 30 ) {
					//寻仙有礼
					JSONObject jo = Utils.toJSONObject(new_data.getStrValue());//new
					JSONObject jo1 = Utils.toJSONObject(old_data.getStrValue());//old
					int oldDraw = 0;
					int newDraw = 0;
					if(jo.containsKey("Draw")) {
						newDraw = jo.getInteger("Draw");
					}
					if(jo1.containsKey("Draw")) {
						oldDraw = jo1.getInteger("Draw");
					}
					jo.put("Draw", oldDraw + newDraw);
					List<Integer> get = Utils.strToIntList((jo.getString("AlreadyGet")));
					List<Integer> get1 = Utils.strToIntList((jo1.getString("AlreadyGet")));
					get.addAll(get1);
					java.util.HashSet<Integer> hashSet = new java.util.HashSet<Integer>(get);  
					get.clear();  
					get.addAll(hashSet); 
			        
					jo.put("AlreadyGet", Utils.ListIntegerToStr(get));
					new_data.setStrValue(jo.toJSONString());
				}
				if(new_data.getActivityId() == 31) {
					int oldDraw = 0;
					int newDraw = 0;
					JSONObject jo = Utils.toJSONObject(new_data.getStrValue());//new
					JSONObject jo1 = Utils.toJSONObject(old_data.getStrValue());//old
					String key = "Num";
					if(jo.containsKey(key)) {
						newDraw = jo.getInteger(key);
					}
					if(jo1.containsKey(key)) {
						oldDraw = jo1.getInteger(key);
					}
					int num = newDraw+oldDraw>=2?2:newDraw+oldDraw;
					jo.put(key, num);
					new_data.setStrValue(jo.toJSONString());
				}
				if(new_data.getActivityId() == 28) {
					String enjoyKey = "Enjoy";
					JSONObject jo = Utils.toJSONObject(new_data.getStrValue());//new
					JSONObject jo1 = Utils.toJSONObject(old_data.getStrValue());//old
					if(!jo.containsKey(enjoyKey)) {
						continue;
					}
					List<Integer> get = Utils.strToIntList((jo.getString("AlreadyGet")));
					List<Integer> get1 = Utils.strToIntList((jo1.getString("AlreadyGet")));
					get.addAll(get1);
					java.util.HashSet<Integer> hashSet = new java.util.HashSet<Integer>(get);  
					get.clear();  
					get.addAll(hashSet); 
					jo.put("AlreadyGet", Utils.ListIntegerToStr(get));
					new_data.setStrValue(jo.toJSONString());
				}
				old_data.remove();
			}
			dataList.put(new_data.getSn(), new_data);
		}
		
		// FIXME 活动，后续打开，未完全测试，暂时不敢开启
		// 今日首次登录
//		if (humanObj.isDailyFirstLogin) {
//			ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
//			prx.triggerDailyHour(ParamManager.dailyHourReset);
//			// 每日重置
//			context.put("hour", ParamManager.dailyHourReset);// EventKey.ResetDailyHour需要插入hour
//			prx.getAllActivityInfo();
//			prx.listenResult(this::_result_onEventTrigger, "eventParam", context, "event", EventKey.HumanLoginFinishFirstToday);
//			return;
//		} 
		//获取活动数据
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", context, "event", EventKey.HumanLoginFinish);	
	}
	
	
	/**
	 * 玩家上线通知活动内容
	 * @param humanObj
	 */
	/*@Listener(value = EventKey.HumanLoginFinishFirstToday)
	public void _listener_HumanLoginFinishFirstToday(Param param) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.HumanLoginFinishFirstToday);
	}*/
	
	/**
	 * 每个整点执行一次
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		Log.game.info("==_listener_ResetDailyHour ");
		int hour = Utils.getHourOfDay(Port.getTime());
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.triggerDailyHour(hour);
		
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置
			humanObj.getHuman().setDailyOnlineTime(0L);// 重置今日在线时间
			param.put("hour", hour);// EventKey.ResetDailyHour需要插入hour
			prx.getAllActivityInfo();
			prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.ResetDailyHour);
		}
	}
	
	
	/**
	 * 每分钟执行一次，用于在线时间
	 */
	@Listener(EventKey.EVERY_MIN)
	public void _listener_EVERY_MIN(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getOnlineActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.EVERY_MIN);	
	}
	
	/**
	 * 玩家升级通知
	 */
	@Listener(value = EventKey.HumanLvUp)
	public void _listener_HumanLvUp(Param param) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.HumanLvUp);	
	}
	/**
	 * 玩家充值通知
	 */
	@Listener(value = EventKey.PayNotify)
	public void _listener_PayNotify(Param param) {
		int sn = param.get("sn");
		HumanObject humanObj = param.get("humanObj");
		if(sn == 2 || sn == 1){//月卡或者和季卡
			humanObj.sendSysMsg(81);//充值成功，记得每天上线来领取元宝奖励哦！
		}
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.PayNotify);	
	}
	/**
	 * 玩家vip等级变化
	 */
	@Listener(value = EventKey.VipLvChange)
	public void _listener_VipLvChange(Param param) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.VipLvChange);
	}
	/**
	 * 玩家消费元宝
	 */
	@Listener(value = EventKey.ActConsumeGold)
	public void _listener_ActConsumeGold(Param param) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.ActConsumeGold);	
	}
	//5点触发触发
	@Listener(value = EventKey.ResetDailyHour)
	public void _listener_ResetDailyHourResetToFrist(Param param) {
		if (Utils.getHourOfDay(Port.getTime()) == ParamManager.dailyHourReset){
			ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
			prx.getAllActivityInfo();//获取活动数据
			prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.ActResetDailyHour);	
		}
	}
	
	/**
	 * 玩家抽卡寻仙
	 */
	@Listener(value = EventKey.DrawCard)
	public void _listener_ActDrawCard(Param param) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.DrawCard);	
	}
	
	/**
	 * 玩家抽卡寻仙
	 */
	@Listener(value = EventKey.DrawCard_GoldTen)
	public void _listener_ActDrawCard_GoldTen(Param param) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.DrawCard_GoldTen);	
	}
	
	/**
	 * 今日首次登陸
	 */
	@Listener(value = EventKey.HumanLoginFinishFirstToday)
	public void _listener_ActLoginFristToday(Param param) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.HumanLoginFinishFirstToday);	
	}
	/**
	 * 首次登陸
	 */
	@Listener(value = EventKey.HumanFirstLogin)
	public void _listener_ActHumanFirstLogin(Param param) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllActivityInfo();//获取活动数据
		prx.listenResult(this::_result_onEventTrigger, "eventParam", param, "event", EventKey.HumanFirstLogin);	
	}
	/**
	 * 触发活动事件
	 */
	private void _result_onEventTrigger(Param results, Param context){
		// 事件类型
		int event = context.get("event");
		List<ActivityObject> activityList = results.get("activityList");
		Map<Integer, ActivityEffectObject> effects = results.get("effects");
		// 事件参数（玩家对象，时间等）
		Param eventParam = context.get("eventParam");
		onEventTrigger(event, activityList, effects, eventParam);
		
		// 如果是登录加载
		if (event == EventKey.HumanLoginFinish) {
			// 玩家数据加载完成一个
			Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", eventParam.get("humanObj"));
		}
	}
	/**
	 * 活动事件触发处理
	 * @param event
	 * @param activityList
	 * @param eventParam
	 */
	private void onEventTrigger(int event, List<ActivityObject> activityList, Map<Integer, ActivityEffectObject> effects, Param eventParam) {
		List<ActivityObject> list = new ArrayList<>();
		for(ActivityObject activity:activityList) {
			if(typeManager.onTrigger(event, activity, eventParam)) {
				list.add(activity);
			}
		}
		if(list.size() > 0) {
			HumanObject humanObj = eventParam.get("humanObj");
			List<Integer> ids = new ArrayList<>();
			sendActivityMessageToHumanObj(false, list, effects, humanObj, ids);
		}		
	}
	/**
	 * 发送消息
	 * @param isAll
	 * @param list
	 * @param effects
	 * @param humanObj
	 * @param ids
	 */
	private void sendActivityMessageToHumanObj(boolean isAll, List<ActivityObject> list, 
		Map<Integer, ActivityEffectObject> effects, HumanObject humanObj, List<Integer> ids) {
		SCActivityInfo.Builder msg = SCActivityInfo.newBuilder();
		msg.setIsAll(isAll);
		
		List<Integer> removeIds = new ArrayList<>();
		for(ActivityObject activityObj:list){
			//如果完成全部则不下发
			if(activityObj.type == ActivityTypeDefine.Activity_TYPE_5) {
				boolean isCompete = ActivityType5.isComplete(humanObj, activityObj);
				if(isCompete) {
					continue;
				}
			}
			
			try {
				//调用不同类型的活动显示数据生成--登陆下发如果没有下发从这里查看原因
				DActivity dActivity = activityObj.createMsg(humanObj, typeManager);
				if(dActivity != null){
					msg.addActivity(dActivity);
				}
			} catch (Throwable e) {
				String str=e.toString();
		        StackTraceElement[] stackElements = e.getStackTrace();
		        if (stackElements != null) {            
		            for (int i = 0; i < stackElements.length; i++) {
		            	str = str + "\n	at " + stackElements[i].getClassName();
		            	str = str + "." + stackElements[i].getMethodName();
		            	str = str + "(" + stackElements[i].getFileName();
		            	str = str + ":" + stackElements[i].getLineNumber()+")";
		            }
		        }
				Log.game.error("activity {} type {} createMsg failed: \n{}", activityObj.id, activityObj.type, str);
			}
			//请求的活动ID已经在显示列表,则从未激活列表中删除
			if(ids.size() > 0) {
				int index = ids.indexOf(activityObj.id); 
				if(index != -1){ 
					//Integer id = ids.remove(index);
					removeIds.add(activityObj.id);
				}
			}
		}
		List<Integer>newIds = new ArrayList<>();
		for(Integer id:ids){
			if(!removeIds.contains(id)){
				newIds.add(id);
			}	
		}

		for(Map.Entry<Integer, ActivityEffectObject> effect:effects.entrySet()){
			msg.addActivityEffect(effect.getValue().createMsg());
		}
		for(int id:newIds){
			msg.addUnactivatedIds(id);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 获取玩家所有活动列表,由客户端请求返回
	 */
	public void getAllShowActivityInfo(HumanObject humanObj) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getAllShowActivityInfo(); //获取玩家所有活动列表
		prx.listenResult(this::_result_getAllShowActivityInfo, "humanObj", humanObj);
	}
	private void _result_getAllShowActivityInfo(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<ActivityObject> activityList = results.get("activityList");
		Map<Integer, ActivityEffectObject> effects = results.get("effects");
		// 发送消息
		List<Integer> ids = new ArrayList<>();
		sendActivityMessageToHumanObj(true, activityList, effects, humanObj, ids);
	}
	
	/**
	 * 获取玩家指定活动列表,由客户端请求返回
	 * @param humanObj
	 * @param ids
	 */
	public void getShowActivityInfo(HumanObject humanObj, List<Integer> ids) {
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		prx.getShowActivityInfo(ids);
		prx.listenResult(this::_result_getShowActivityInfo, "humanObj", humanObj, "ids", ids);		
	}
	private void _result_getShowActivityInfo(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<Integer> ids = context.get("ids");
		List<ActivityObject> activityList = results.get("activityList");
		Map<Integer, ActivityEffectObject> effects = results.get("effects");
		// 发送消息
		sendActivityMessageToHumanObj(false, activityList, effects, humanObj, ids);
	}
	
	/**
	 * 执行活动操作
	 */
	public void commitActivity(HumanObject humanObj, int id, List<DActivityParam> activityParamsList) {		
		//加锁
//		if(!humanObj.cdLocks.lock(humanObj, HumanOperateCDManager.Oper_Type_Activity)) {
//			return;
//		}
		
		ActivityServiceProxy prx = ActivityServiceProxy.newInstance();
		List<ActivityParamObject> paramList = new ArrayList<>(); //参数对象list
		//客户端传过来的操作参数(activityParamsList)转化
		for(DActivityParam p:activityParamsList) {
			ActivityParamObject pObj = new ActivityParamObject(p);
			paramList.add(pObj);
		}
		prx.getActivityInfo(id);//根据活动ID 远程获取对应的活动数据 (activityService 316)
		prx.listenResult(this::_result_commitActivity, "humanObj", humanObj, "paramList", paramList);	
	}
	private void _result_commitActivity(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<ActivityParamObject> paramList = context.get("paramList");
		
		ActivityObject activity = results.get("activity");
		//Map<Integer, ActivityEffectObject> effects = results.get("effects");
		
		//调用活动执行处理,根据参数不同,会有不同的处理
		boolean success = false;
		if(activity != null){
			success = typeManager.commitActivity(activity, humanObj, paramList);
			//需要充值的活动类型
			List<Integer> noticeList = new ArrayList<>();
			noticeList.add(ActivityTypeDefine.Activity_TYPE_19);
			noticeList.add(ActivityTypeDefine.Activity_TYPE_21);
			noticeList.add(ActivityTypeDefine.Activity_TYPE_11);
			noticeList.add(ActivityTypeDefine.Activity_TYPE_13);
			noticeList.add(ActivityTypeDefine.Activity_TYPE_5);
			if(success && noticeList.contains(activity.type)){
				ConfActivity conf = ConfActivity.get(activity.id);
				if(conf != null) {
					// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
					String content = Utils.createStr("{}|{}|{}|{}", ParamManager.sysMsgMark, 999001, 
							humanObj.getHuman().getName(), conf.sn);
					InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
				}
				
			}
		}
		
		//给客户端发一个处理返回的活动内容更新消息(应客户端要求,无论成功与否都发送)
		SCActivityCommitReturn.Builder msg = SCActivityCommitReturn.newBuilder();
		msg.setSuccess(success);
		if(activity != null){
			DActivity dActivity = activity.createMsg(humanObj, typeManager, false);
			if(dActivity != null){
				msg.setActivity(dActivity);
			}
		}
		if(activity.type == ActivityTypeDefine.Activity_TYPE_13 && !success){
			Log.game.info("许愿失败");
			return;
		}
//		if(activity.type == ActivityTypeDefine.Activity_TYPE_18 && !success){
//			Log.game.info("领取在线礼包失败");
//			return;
//		}
		humanObj.sendMsg(msg);
		
		//解锁
//		humanObj.cdLocks.unlock(humanObj, HumanOperateCDManager.Oper_Type_Activity);
	}
	/**
	 * 获取总共需要多少元宝才能达到要求
	 * @return
	 */
	public int getNeedGold(int activityType,int actaid){
		int gold = 0;
		int sn = activityType;
		ConfActivity conf = ConfActivity.get(sn);
		if(conf == null ){
			Log.table.info("ActivtityManager.getNeedGold error sn ={}",sn);
			return gold;
		}
		String jsonStr  = conf.Json;
		JSONObject json = Utils.toJSONObject(jsonStr);
		if(json == null){
			return gold;
		}
		JSONArray awards = json.getJSONArray("awards");
		if (awards == null) {
			return gold;
		}
		for(int i=0;i<awards.size();i++){
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");
			if(actaid== aid){
				int needTopUp = rewardJson.getIntValue("needTopUp");//需要购买
				gold+=needTopUp;
				break;
			}
		}
		return gold;
	}
	
	/**
	 * 获取活动期间玩家充值的金额List
	 * @param activityObj
	 * @param humanObj
	 * @return
	 */
	public static List<Long> getChargeInActivity(ActivityObject activityObj,HumanObject humanObj){
		List<Long> payList = new ArrayList<>();
		List<PayLog> payLogs = humanObj.payLogs;
		for (PayLog payLog : payLogs) {
			String timeStr =payLog.getTime();
			long payTimeStamp = Utils.formatTimeToLong(timeStr);
			if(payTimeStamp >= activityObj.beginTime && payTimeStamp <= activityObj.endTime){
				String money = payLog.getActualPrice();//单位是分
				payList.add(Utils.longValue(money));
			}
		}
		return payList;
	}
	
	
	/**
	 * 登录，下发爬塔今天是否挑战过
	 */
	@Listener(EventKey.HumanDataLoadAllFinish)
	public void _listener_HumanDataLoadOther2(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		sendSCSevenLoginMsg(humanObj);
	}
	
	/**
	 * 根据登陆LoginNum发送SCSevenLogin
	 */
	public void sendSCSevenLoginMsg(HumanObject humanObj) {
		ConnectionProxy prxConn = ConnectionProxy.newInstance(humanObj.connPoint);// 连接代理
		SCSevenLogin.Builder msg = getSCSevenLoginMsg(humanObj);
		prxConn.sendMsg(MsgIds.SCSevenLogin, new Chunk(msg));
		
	}

	public SCSevenLogin.Builder getSCSevenLoginMsg(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		SCSevenLogin.Builder msg = SCSevenLogin.newBuilder();
		String times = Utils.formatTime(human.getGetloginAwardTime(),"yyyyMMdd");
		msg.setLoginDayNum(Utils.intValue(times));
		
		long now = Port.getTime();
		
		int day = human.getLoginNumAwardStatus();
		
		Collection<ConfSevenLogin>  confAll = ConfSevenLogin.findAll();

		for(ConfSevenLogin conf:confAll) {
			DSevenLoginAwardType.Builder dinfo = DSevenLoginAwardType.newBuilder();
			dinfo.setSn(conf.sn);
			if (conf.sn <= day) {
				dinfo.setLoginDaytype(EAwardType.Awarded);
			} else if (conf.sn == day+1) {
				if (canGetSevenLogin(human, now)) {
					dinfo.setLoginDaytype(EAwardType.Awarding);
				} else {
					dinfo.setLoginDaytype(EAwardType.AwardNot);
				}
			} else {
				dinfo.setLoginDaytype(EAwardType.AwardNot);
			}
			msg.addCondition(dinfo);
		}
		return msg;
				
	}
	
	private boolean canGetSevenLogin(Human human, long now) {
		long lastGetTime = human.getGetloginAwardTime();
		if(lastGetTime !=0 && Utils.isSameDay(lastGetTime, now)){
			return false;
		}
		return true;
	}
	
	public void getSevenLoginAward(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		int day = human.getLoginNumAwardStatus();
		int sn = day+1;

		ConfSevenLogin conf = ConfSevenLogin.get(sn);
		if(conf == null) {
			Log.activity.info("getSevenLoginAward not conf {}", sn);
			return;
		}
		
		//判断是否跨天,跨天才可以领取
		long now = Port.getTime();
		if (!canGetSevenLogin(human, now)) {
			Log.activity.info("getSevenLoginAward cd中");
			return;
		}
		
		//记录领取时间
		human.setLoginNumAwardStatus(sn);
		human.setGetloginAwardTime(now);
		RewardHelper.reward(humanObj, conf.itemSn,conf.itemNum, LogSysModType.Activity);
		
		SCSevenLogin.Builder msg = getSCSevenLoginMsg(humanObj);
		String times = Utils.formatTime(now,"yyyyMMdd");
		msg.setLoginDayNum(Utils.intValue(times));
		DSevenLoginAwardType.Builder dinfo = DSevenLoginAwardType.newBuilder();
		dinfo.setSn(conf.sn);
		for (int i = 0; i < conf.itemNum.length; i++) {
			ProduceVo p = new ProduceVo(conf.itemSn[i],conf.itemNum[i]);
			msg.addItemList(p.toDProduce());
		}
		msg.addCondition(dinfo);
		
		msg.setIsSuccess(true);
		humanObj.sendMsg(msg);
	}
}
