package game.worldsrv.activity.types;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.param.ParamManager;
import game.msg.Define.DActivityZoneItem;
import game.worldsrv.activity.ActivityObject;
import game.worldsrv.activity.ActivityParamObject;
import game.worldsrv.activity.ActivityZoneItemObject;

public class ActivityTypeBase implements IActivityType {

	private static final int type = ActivityTypeDefine.Activity_TYPE_0; // 类型
	private static final ActivityTypeBase instance = new ActivityTypeBase();
	protected static final int HOUR_ZERO = ParamManager.dailyHourReset; // 每日充值时间点

	/**
	 * 构造函数
	 */
	protected ActivityTypeBase() {
	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static ActivityTypeBase getInstance() {
		return ActivityTypeBase.instance;
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	public int getType() {
		return ActivityTypeBase.type;
	}

	/**
	 * 解析操作参数
	 * 
	 * @param paramStr
	 * @return
	 */
	public List<ActivityZoneItemObject> initOperateParam(ActivityObject activity,String paramStr) {
		List<ActivityZoneItemObject> zoneItems = new ArrayList<>();
		return zoneItems;
	}

	/**
	 * 获取给客户端的参数
	 * 
	 * @param activity
	 * @return
	 */
	public Param getShowParam(ActivityObject activity, HumanObject humanObj,
			List<DActivityZoneItem> zoneList) {
		return null;
	}

	/**
	 * 监听事件触发
	 * 
	 * @param event
	 * @param param
	 */
	public boolean onTrigger(int event, ActivityObject activity, Param param) {

		return false;
	}

	/**
	 * 处理客户端的执行请求
	 * 
	 * @param activity
	 * @return
	 */
	@Override
	public boolean commitActivity(ActivityObject activity,
			HumanObject humanObj, List<ActivityParamObject> paramList) {

		return false;
	}

	/**
	 * 创建玩家活动数据,创建并提交数据库，请先将数据处理完成后再提交
	 * 
	 * @param activity
	 * @param humanObj
	 * @param sn
	 * @param numValue
	 * @param strValue
	 * @return
	 */
	public ActivityHumanData createHumanActivityData(ActivityObject activity,
			HumanObject humanObj, int sn, long numValue, String strValue) {
		Map<Integer, ActivityHumanData> dataList = humanObj.activityDatas.get(activity.id);
		if (dataList == null) {
			dataList = new HashMap<>();
			humanObj.activityDatas.put(activity.id, dataList);
		}
		ActivityHumanData dbData = new ActivityHumanData();
		dbData.setId(Port.applyId());
		dbData.setHumanId(humanObj.id);
		dbData.setActivityId(activity.id);
		dbData.setSn(sn);
		dbData.setNumValue(numValue);
		dbData.setStrValue(strValue);
		dbData.persist();// 提交数据库
		dataList.put(sn, dbData);// 加入活动数据列表中
		return dbData;
	}

	/**
	 * 单条提交数据
	 */
	public void commitData(ActivityObject activity, HumanObject humanObj,
			String jsonStr, int persistSn) {
		List<Integer> snList = new ArrayList<>();// 索引列表
		Map<Integer, Long> numValues = new HashMap<>();// 数值参数表
		Map<Integer, String> strValues = new HashMap<>();

		snList.add(persistSn);
		numValues.put(persistSn, Port.getTime());
		strValues.put(persistSn, jsonStr);
		commitHumanActivityData(activity, humanObj, snList, numValues,
				strValues);
	}

	/**
	 * 提交玩家活动数据变更，请先将数据处理后再调用
	 * 
	 * @param activity
	 * @param humanObj
	 * @param snList
	 * @param numValues
	 * @param strValues
	 */
	public void commitHumanActivityData(ActivityObject activity,
			HumanObject humanObj, List<Integer> snList,
			Map<Integer, Long> numValues, Map<Integer, String> strValues) {
		
		Map<Integer, ActivityHumanData> activityData = humanObj.activityDatas.get(activity.id);
		if (activityData == null) {// 活动数据列表为空，新增所有数据
			for (int sn : snList) {
				Long lvalue = numValues.get(sn);
				long nv = lvalue == null ? 0 : lvalue.longValue();
				String strv = strValues.get(sn);
				if (strv == null) {
					strv = "";
				}
				createHumanActivityData(activity, humanObj, sn, nv, strv);
			}
		} else {// 修改数据，新增不存在数据
			for (int sn : snList) {
				Long lvalue = numValues.get(sn);
				long nv = lvalue == null ? 0 : lvalue.longValue();
				String strv = strValues.get(sn);
				ActivityHumanData data = activityData.get(sn);
				if (data != null) {
					data.setNumValue(nv);
					if (strv != null) {
						data.setStrValue(strv);
					}
					data.update();
				} else {
					createHumanActivityData(activity, humanObj, sn, nv, strv);
				}
			}
		}
	}

	/**
	 * 提交玩家活动数据变更，请先将数据处理后再调用
	 * 
	 * @param activity
	 * @param humanObj
	 * @param snList
	 * @param numValues
	 */
	public void commitHumanActivityData(ActivityObject activity,
			HumanObject humanObj, List<Integer> snList,
			Map<Integer, Long> numValues) {
		Map<Integer, String> strValues = new HashMap<>();
		commitHumanActivityData(activity, humanObj, snList, numValues,
				strValues);
	}

	/**
	 * 获取当天时间值，转为类似20160101形式整数
	 * 
	 * @return
	 */
	public static int getNowDateValue() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return year * 10000 + month * 100 + day;
	}

	/**
	 * 获取本月首日时间值，转为类似20160101形式整数
	 * 
	 * @return
	 */
	public static int getNowMonthFirstValue() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始
		return year * 10000 + month * 100 + 1;
	}

	/**
	 * 获取当天月份
	 * 
	 * @return
	 */
	public static int getNowMonth() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始
	}

	/**
	 * 返回活动开始时间值，转为类似20160101形式整数
	 * 
	 * @param activity
	 * @return
	 */
	public static int getActivityBeginDateValue(ActivityObject activity) {
		Calendar cal = Calendar.getInstance();
		if (activity.beginTime > 0) {
			cal.setTimeInMillis(activity.beginTime);
		}
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return year * 10000 + month * 100 + day;
	}

	/**
	 * 获取当天时间值，转为类似20160101形式整数，hour为日期重置点
	 * 
	 * @return
	 */
	public static int getNowDateValue(int hour) {
		Calendar cal = Calendar.getInstance();
		int nowhour = cal.get(Calendar.HOUR_OF_DAY);
		if (nowhour < hour) {
			cal.setTimeInMillis(cal.getTimeInMillis() - hour * 3600000);
		}
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return year * 10000 + month * 100 + day;
	}

	/**
	 * 获取当天时间值，转为类似20160101形式整数，hour为日期重置点
	 * 
	 * @return
	 */
	public static int getNowDateToHourValue(int hour) {
		Calendar cal = Calendar.getInstance();
		int nowhour = cal.get(Calendar.HOUR_OF_DAY);
		if (nowhour < hour) {
			cal.setTimeInMillis(cal.getTimeInMillis() - hour * 3600000);
		}
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return year * 1000000 + month * 10000 + day * 100 + hour;
	}

	public static int getTimeDataValue(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return year * 10000 + month * 100 + day;
	}

	/**
	 * 获取本月首日时间值，转为类似20160101形式整数，hour为日期重置点
	 * 
	 * @param hour
	 * @return
	 */
	public static int getNowMonthFirstValue(int hour) {
		Calendar cal = Calendar.getInstance();
		int nowhour = cal.get(Calendar.HOUR_OF_DAY);
		if (nowhour < hour) {
			cal.setTimeInMillis(cal.getTimeInMillis() - hour * 3600000);
		}
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始
		return year * 10000 + month * 100 + 1;
	}

	/**
	 * 获取当天月份，hour为日期重置点
	 * 
	 * @param hour
	 * @return
	 */
	public static int getNowMonth(int hour) {
		Calendar cal = Calendar.getInstance();
		int nowhour = cal.get(Calendar.HOUR_OF_DAY);
		if (nowhour < hour) {
			cal.setTimeInMillis(cal.getTimeInMillis() - hour * 3600000);
		}
		// MONTH从0开始, 真实月份是从1开始
		return cal.get(Calendar.MONTH) + 1;
	}

	/**
	 * 判断日期是否同一天，hour为日期重置点
	 * 
	 * @param time1
	 * @param time2
	 * @param hour
	 * @return
	 */
	public static boolean isSameDay(long time1, long time2, int hour) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(time1);
		int nowhour = cal1.get(Calendar.HOUR_OF_DAY);
		if (nowhour < hour) {
			cal1.setTimeInMillis(cal1.getTimeInMillis() - hour * 3600000);
		}
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(time2);
		nowhour = cal2.get(Calendar.HOUR_OF_DAY);
		if (nowhour < hour) {
			cal2.setTimeInMillis(cal2.getTimeInMillis() - hour * 3600000);
		}
		return Utils.isSameDay(cal1.getTimeInMillis(), cal2.getTimeInMillis());
	}

	public static int getDayBetween(long time1, long time2, int hour) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(time1);
		int nowhour = cal1.get(Calendar.HOUR_OF_DAY);
		if (nowhour < hour) {
			cal1.setTimeInMillis(cal1.getTimeInMillis() - hour * 3600000);
		}
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(time2);
		nowhour = cal2.get(Calendar.HOUR_OF_DAY);
		if (nowhour < hour) {
			cal2.setTimeInMillis(cal2.getTimeInMillis() - hour * 3600000);
		}
		return Utils.getDaysBetween(cal1.getTimeInMillis(),
				cal2.getTimeInMillis());
	}

	/**
	 * 返回活动开始时间值，转为类似20160101形式整数，hour为日期重置点，实际计算时，不到hour时间点算未开始
	 * 
	 * @param activity
	 * @param hour
	 * @return
	 */
	public static int getActivityBeginDateValue(ActivityObject activity,
			int hour) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(activity.beginTime);

		// int nowhour = cal.get(Calendar.HOUR_OF_DAY);
		// if(nowhour<hour){
		// cal.setTimeInMillis(cal.getTimeInMillis()-hour*3600000);
		// }
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;// MONTH从0开始, 真实月份是从1开始
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return year * 10000 + month * 100 + day;
	}

	/**
	 * 时间字符串转数值，如果是数值，直接转数值
	 * 支持整数数值,字符串格式:"yyyy-MM-dd","yyyy-MM-dd HH:mm","yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param strTime
	 * @return
	 */
	public static long stringTimeToLong(String strTime) {
		long time = 0;
		if (StringUtils.isNotEmpty(strTime) && NumberUtils.isNumber(strTime)) {
			time = Long.parseLong(strTime);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
	 * 周期判定
	 * 
	 * @param activity
	 * @return
	 */
	public boolean isPeriod(long nowtime, ActivityObject activity) {
		return false;
	}
	
	protected long getParamAid(List<ActivityParamObject> paramList) {
		ActivityParamObject params = paramList.get(0);
		if(params==null){
			return 0;
		}
		long aid = 0;
		if(params != null){
			if(params.numParams.size()>0){
				aid = params.numParams.get(0);
			}
		}
		return aid;
	}
	
	protected ActivityHumanData getHumanActivityData(HumanObject humanObj,ActivityObject activity, int persistSn) {
		Map<Integer, ActivityHumanData> activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		if (activityHumanDataMap == null) {
			List<Integer> snList = new ArrayList<>();//索引列表
			Map<Integer,Long> numValues = new HashMap<>();//数值参数表
			Map<Integer,String> strValues = new HashMap<>();
			
			snList.add(persistSn);
			strValues.put(persistSn, new JSONObject().toJSONString());
			commitHumanActivityData(activity,humanObj,snList,numValues,strValues);
			
			activityHumanDataMap = humanObj.activityDatas.get(activity.id);
		}
		return activityHumanDataMap.get(persistSn);
	}
}
