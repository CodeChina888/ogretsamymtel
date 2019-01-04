package game.worldsrv.activity;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Config;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import game.worldsrv.character.HumanObject;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfActivity;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.msg.MsgActivity.SCActivityNeedUpdate;
import game.worldsrv.activity.types.ActivityServerCompetition;
import game.worldsrv.activity.types.ActivityTypeManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.entity.ActServerCompetition;
import game.worldsrv.entity.Activity;
import game.worldsrv.entity.ActivityData;
import game.worldsrv.entity.ActivityGlobal;

/**
 * 活动
 */
@DistrClass(
	servId = D.SERV_ACTIVITY,
	importClass = {List.class, HumanObject.class}
)
public class ActivityService extends GameServiceBase {
	private TickTimer activityPulseTimer = new TickTimer(3000); //控制活动时间频率
	public Map<Integer, ActivityObject> activitys = new HashMap<>();//活动列表 初始化时添加数据
	private Map<Integer, Map<Integer,ActivityEffectObject>> effects = new HashMap<>();//活动效果(typeid,activityid,value)
	public ActivityTypeManager typeManager = new ActivityTypeManager();//活动处理管理器
	
	public static final int pageNum = 1000;//单次查询的数据条目
	
	//活动全局信息[全民基金使用]
	private static ActivityGlobal activityGlobal = null;
	private static final long Key_ActivityGlobal = 1; // 活动全局数据表的唯一key值
	
	/**
	 * 构造函数
	 * @param port
	 */
	public ActivityService(GamePort port) {
		super(port);
	}
	
	/**
	 * 时间字符串转数值，如果是数值，直接转数值
	 * 支持整数数值,字符串格式:"yyyy-MM-dd","yyyy-MM-dd HH:mm","yyyy-MM-dd HH:mm:ss"
	 * @param strTime
	 * @return
	 */
	private long stringTimeToLong(String strTime){
		long time = 0;
		if(StringUtils.isNotEmpty(strTime) && NumberUtils.isNumber(strTime)){
			time = Long.parseLong(strTime);
		}
		else{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				Date d = sdf.parse(strTime);
				time = d.getTime();
			} catch (Exception e) {
				sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				try {
					Date d = sdf.parse(strTime);
					time = d.getTime();
				} catch (Exception e1) {
					sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						Date d = sdf.parse(strTime);
						time = d.getTime();
					} catch (Exception e2) {
						time = 0;
					}
				}
			}
		}
		return time;
	}
	
	/**
	 * 初始化函数
	 */
	@Override
	protected void init() {		
		//初始化活动数据
		DB db = DB.newInstance(Activity.tableName);
		db.countAll(false);
		Param paramCount = db.waitForResult();
		int count = paramCount.get();
		if (count > 0) {			
			int page = count / pageNum;
			for (int i = 0; i <= page; i++) {
				db.findBy(false, i * pageNum, pageNum);
				Param params = db.waitForResult();
				List<Record> records = params.get();
				for (Record record : records) {					
					Activity data = new Activity(record);
					int id = data.getActivityId();
					//查询活动对象是否存在,不存在则创建一个活动对象
					ActivityObject activity = activitys.get(id);
					if(activity == null){
						activity = new ActivityObject(id);
						activitys.put(id, activity);
					}
					//设置活动的状态数据
					activity.setStatus(data);
				}
			}
		}
		
		//初始化活动数据
		db = DB.newInstance(ActivityData.tableName);
		db.countAll(false);
		paramCount = db.waitForResult();
		count = paramCount.get();
		if (count > 0) {			
			int page = count / pageNum;
			for (int i = 0; i <= page; i++) {
				db.findBy(false, i * pageNum, pageNum);
				Param params = db.waitForResult();
				List<Record> records = params.get();
				for (Record record : records) {					
					ActivityData activityData = new ActivityData(record);
					int id = activityData.getActivityId();
					//查询活动对象是否存在,若不存在则创建一个活动对象
					ActivityObject activity = activitys.get(id);
					if(activity==null){
						activity = new ActivityObject(id);
						activitys.put(id, activity);
					}
					//加入活动数据
					activity.addActivityData(activityData);
				}
			}
		}
		List<ConfActivity> confActivityList = new ArrayList<>(ConfActivity.findAll());
		for(ConfActivity confActivity:confActivityList){
			ActivityObject activityObj = activitys.get(confActivity.sn);
			long startTime = stringTimeToLong(confActivity.beginTime);
			long endTime = stringTimeToLong(confActivity.endTime);
			long previewTime = stringTimeToLong(confActivity.previewTime) ;
			long planTime = stringTimeToLong(confActivity.planTime);
			if(activityObj == null){
				if (confActivity.mode == 2) {
					startTime = Config.SERVER_STARTDATE; // 获得开服时间 
					int delayTime = Utils.intValue(confActivity.beginTime);// 延迟天数
					if (delayTime > 0) {	//开服第二天后开始
						startTime = Utils.getTimeBeginOfToday(startTime);
					}
					startTime += delayTime * Time.DAY;// 开服时间要加上延迟的时间
					Date date = new Date(startTime);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date); 
					calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + Utils.intValue(confActivity.endTime));  //如果是类型2
			        long time = calendar.getTimeInMillis();
					endTime = Utils.getTimeBeginOfToday(time)-Time.SEC; //获取本日0点的时间戳
					if(Utils.intValue(confActivity.planTime)!= 0){
						calendar.setTime(date);
						calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + Utils.intValue(confActivity.planTime));  //如果是类型2
						time = calendar.getTimeInMillis();
						planTime = Utils.getTimeBeginOfToday(time); //获取本日0点的时间戳
					}
				}
				activityObj = new ActivityObject(confActivity.sn, 1, confActivity.type,
						previewTime , startTime, endTime ,confActivity.notice,confActivity.mode,confActivity.index,planTime, 
						confActivity.serviceTime);
				activitys.put(confActivity.sn, activityObj);
			}else{
				activityObj.initInfo(1, confActivity.type,previewTime
						, startTime, endTime,confActivity.notice,confActivity.mode,confActivity.index,planTime,confActivity.serviceTime);
			}

			typeManager.initOperateParam(activityObj, confActivity.Json);
			
		}
		
		for(ActivityObject activity:activitys.values()){
			//调用活动操作参数初始化
			activity.setInitDataOk(true);
			activity.checkTimeStep(Port.getTime());
			//最后再初始化活动开关状态
			ActivityInfo.initToAddInfo(activity,true);
		}
		
		
 		Log.game.info("Activity_Load_Compete");
 		//加载全民基金模块
 		loadActivityGlobal();
 		  
	}
	
	
	private void loadActivityGlobal() {
		DB dbPrx = DB.newInstance(ActivityGlobal.tableName);
		// 获得数量
		dbPrx.get(Key_ActivityGlobal);
		// 同步等待结果
		Param result = dbPrx.waitForResult();
		Record record = result.get();
		// 没有查询到数据 ，则插入一条新的数据
		activityGlobal = new ActivityGlobal();
		if (record == null) {
			activityGlobal.setId(Key_ActivityGlobal);
			// 设置投资理财默认参购买人数
			activityGlobal.setFundBuyCount(0);
			activityGlobal.persist();
			return;
		}
		// 设置数据
		activityGlobal.setRecord(record);
	}
	
	
	
	@Override
	public void pulseOverride() {		
		long nowtime = Port.getTime();
		if (activityPulseTimer.isPeriod(nowtime)){
			List<Integer> list = new ArrayList<>();
			List<Integer> plan = new ArrayList<>();
			for(ActivityObject activity:activitys.values()){
				boolean isAdd = false;
				if(activity.checkTimeStep(nowtime)){
					isAdd = true;
				}
				if(typeManager.isPeriod(nowtime, activity)){
					isAdd = true;
				}
				if(isAdd){
					list.add(activity.id);
				}
				if(activity.checkPlanStep()){
					plan.add(activity.id);
				}
			}
			if(list.size()>0){
				HumanGlobalServiceProxy pr = HumanGlobalServiceProxy.newInstance();
				SCActivityNeedUpdate.Builder msg = SCActivityNeedUpdate.newBuilder();
				msg.addAllId(list);
				pr.sendMsgToAll(new ArrayList<>(), msg.build());
				updateActivity(list);
			}
			if(plan.size() > 0){
				updatePlanActivity(plan);
			}
		}
	}
	
	/***
	 * 更新全局活动状态
	 * @param list
	 */
	private void updateActivity(List<Integer> list){
		for (int id : list) {
			ActivityObject activity = activitys.get(id);
			if (activity != null) {
				ActivityInfo.initToAddInfo(activity,false);
			}
		}
	}
	/**
	 * 更新全局活动截止
	 * @param list
	 */
	private void updatePlanActivity(List<Integer> list){
		for(int id : list){
			ActivityObject activity = activitys.get(id);
			if (activity != null) {
				ActivityInfo.updatePlanActivity(activity);
			}
		}
	}
	/**
	 * 获取玩家活动列表
	 */
	@DistrMethod
	public void getAllActivityInfo()	{
		List<ActivityObject> activityList = new ArrayList<>();		

		for(Map.Entry<Integer, ActivityObject> entry:activitys.entrySet()){
			if(entry.getValue().isValid()){
				activityList.add(entry.getValue());
			}else {
				Log.activity.debug("活动无效,不下发,activity ={}",entry.getValue().id);
			}
		}
		Map<Integer, ActivityEffectObject> activityEffects = getActivityEffects();
		port.returns("activityList",activityList, "effects",activityEffects);
	}
	
	@DistrMethod
	public void getOnlineActivityInfo(){
		List<ActivityObject> activityList = new ArrayList<>();		

		for(Map.Entry<Integer, ActivityObject> entry:activitys.entrySet()){
			//特殊处理，如果不是在线礼包id不返回
			if(entry.getValue().id != 2){
				continue;
			}
			if(entry.getValue().isValid()){
				activityList.add(entry.getValue());
			}
		}
		Map<Integer, ActivityEffectObject> activityEffects = getActivityEffects();
		port.returns("activityList",activityList, "effects",activityEffects);
	}
	
	/**
	 * 获取玩家活动列表
	 */
	@DistrMethod
	public void getAllShowActivityInfo()	{
		List<ActivityObject> activityList = new ArrayList<>();		

		for(Map.Entry<Integer, ActivityObject> entry:activitys.entrySet()){
			if(entry.getValue().isShowValid()){ // 活动显示状态
				activityList.add(entry.getValue());
			}
		}
		Map<Integer, ActivityEffectObject> activityEffects = getActivityEffects();
		port.returns("activityList",activityList, "effects",activityEffects);
	}
	
	/**
	 * 获取指定活动列表
	 * @param ids
	 */
	@DistrMethod
	public void getActivityInfo(List<Integer> ids){
		List<ActivityObject> activityList = new ArrayList<>();

		for(int id:ids){
			ActivityObject activity = activitys.get(id);
			if(activity.isValid()){ // 活动状态
				activityList.add(activity);
			}
		}

		Map<Integer, ActivityEffectObject> activityEffects = getActivityEffects();
		port.returns("activityList",activityList, "effects",activityEffects);
	}
	
	/**
	 * 获取指定活动显示列表
	 * @param ids
	 */
	@DistrMethod
	public void getShowActivityInfo(List<Integer> ids){
		List<ActivityObject> activityList = new ArrayList<>();
		for(int id:ids){
			ActivityObject activity = activitys.get(id);
			if(activity.isShowValid()){ // 活动显示状态
				activityList.add(activity);
			}
		}
		Map<Integer, ActivityEffectObject> activityEffects = getActivityEffects();
		port.returns("activityList",activityList, "effects",activityEffects);
	}
		
	/**
	 * 通知所有玩家活动数据变动
	 * @param ids
	 */
	@DistrMethod
	public void sendActivityUpdateInfo(List<Integer> ids){
		SCActivityNeedUpdate.Builder msg = SCActivityNeedUpdate.newBuilder();
		Log.activity.error("===== sendActivityUpdateInfo msg");
		msg.addAllId(ids);
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendMsgToAll(new ArrayList<>(), msg.build());
	}
	
	
	/**
	 * 指定活动列表
	 * @param id
	 */
	@DistrMethod
	public void getActivityInfo(int id){
		ActivityObject activity = activitys.get(id);
		Map<Integer, ActivityEffectObject> activityEffects = getActivityEffects(); //获取活动效果列表
		port.returns("activity",activity, "effects",activityEffects);
	}
	/**
	 * 增加活动效果，同一活动同一效果只能有一个
	 * @param activityId
	 * @param effectType
	 * @param param
	 */
	public void addEffect(int activityId, int effectType, ActivityEffectObject param){
		Map<Integer,ActivityEffectObject> activityEffect = effects.get(effectType);
		if(activityEffect == null){
			activityEffect = new HashMap<>();
			activityEffect.put(activityId, param);
			effects.put(effectType, activityEffect);
		}
		else{
			activityEffect.put(activityId, param);
		}
	}
	

	/**
	 * 删除活动效果，同一活动同一效果只能有一个
	 * @param activityId
	 * @param effectType
	 */
	public void removeEffect(long activityId, int effectType){
		Map<Integer,ActivityEffectObject> activityEffect = effects.get(effectType);
		if(activityEffect == null){
			return;
		}
		else{
			activityEffect.remove(activityId);
			if(activityEffect.size()<1){
				effects.remove(effectType);
			}
		}
	}
	
	/**
	 * 获取活动效果列表，将活动所有效果统计之后的参数，同一活动效果的参数设定必须一致，否则以第一个为准，若同类型，则执行类型加法方法
	 */
	public Map<Integer, ActivityEffectObject> getActivityEffects(){
		Map<Integer, ActivityEffectObject> result = new HashMap<>();
		for(Map.Entry<Integer, Map<Integer,ActivityEffectObject>> entry : effects.entrySet()){
			ActivityEffectObject param = null;
			for(Map.Entry<Integer, ActivityEffectObject> activityEntry : entry.getValue().entrySet()){
				ActivityEffectObject paramValue = activityEntry.getValue();
				if(param == null){
					param = paramValue.getClone();
				}
				else{
					if(paramValue.type == param.type){
						param.add(paramValue);
					}
				}
			}
			result.put(entry.getKey(),param);
		}
		return result;
	}
	
	@DistrMethod
	public void triggerDailyHour(int hour) {
		Param eventParam = new Param();
		eventParam.put("hour", hour);
		onEventTrigger(EventKey.ResetDailyHour, eventParam);
	}
	
	
	/**
	 * 活动事件触发处理
	 * @param event
	 * @param eventParam
	 */
	private void onEventTrigger(int event, Param eventParam){
		List<Integer> list = new ArrayList<>();
		for(ActivityObject activity:activitys.values()){
			if(activity.isValid()){
				// 触发：事件名称，活动对象，时间
				if(typeManager.onTrigger(event, activity, eventParam)){
					//Log.activity.error("actid={}",activity.id);
					list.add(activity.id);
				}
			}
		}
		if(list.size()>0){
			HumanGlobalServiceProxy pr = HumanGlobalServiceProxy.newInstance();
			SCActivityNeedUpdate.Builder msg = SCActivityNeedUpdate.newBuilder();
			msg.addAllId(list);
			pr.sendMsgToAll(new ArrayList<>(), msg.build());		
		}
	}
	
	/**
	 * 重置活动
	 * @param activity
	 */
	public void resetActivity(ConfActivity activity){
		ActivityObject oldactivity = activitys.get(activity.sn);
		if (oldactivity != null) {
			removeActivityEffect(oldactivity); 
		}
		ActivityObject Activity = activitys.get(activity.sn);
		if(Activity == null) {
			Activity = new ActivityObject(activity.sn, 1, activity.type,
					stringTimeToLong(activity.previewTime), stringTimeToLong(activity.beginTime), stringTimeToLong(activity.endTime),activity.notice,activity.mode,activity.index,stringTimeToLong(activity.planTime),
					activity.serviceTime);
			activitys.put(activity.sn, Activity);
		} else {
			Activity.initInfo(1, activity.type, 
					stringTimeToLong(activity.previewTime), stringTimeToLong(activity.beginTime), stringTimeToLong(activity.endTime),activity.notice,activity.mode,activity.index,stringTimeToLong(activity.planTime),
					activity.serviceTime);
		}
		//调用活动操作参数初始化
		typeManager.initOperateParam(Activity, activity.Json);
	}
	
	/**
	 * 移除活动效果
	 * @param activity
	 */
	private void removeActivityEffect(ActivityObject activity){
		java.util.Iterator<Map.Entry<Integer, Map<Integer, ActivityEffectObject>>> itr = effects.entrySet().iterator();
		while(itr.hasNext()){
			Map.Entry<Integer, Map<Integer, ActivityEffectObject>> entry = itr.next();
			ActivityEffectObject  effect = entry.getValue().get(activity.id);
			if(effect!=null){
				entry.getValue().remove(activity.id);
				if(entry.getValue().size()<1){
					itr.remove();
				}
			}
		}
	}
	
	/**
	 * 增加活动效果
	 * @param activity
	 * @param effect
	 */
	public void addActivityEffect(ActivityObject activity, ActivityEffectObject effect){
		Map<Integer, ActivityEffectObject> effectList = effects.get(effect.type);
		if(effectList==null){
			effectList = new HashMap<>();
			effects.put(effect.type, effectList);			
		}
		effectList.put(activity.id, effect);
	}
	
	@DistrMethod
	public void getaAtivityGlobal(){
		port.returns("activityGlobal",activityGlobal);
	}
	
	//修改setFundBuyCount
	@DistrMethod
	public void setAtivityGlobalNum(int i){
		activityGlobal.setFundBuyCount(i);
	}
	

}