package game.worldsrv.activity.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.support.Param;
import core.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.msg.Define.DItem;
import game.msg.Define.DMoney;
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

public class ActivityType8 extends ActivityTypeBase {
	private static final int type = ActivityTypeDefine.Activity_TYPE_8; // 类型
	private static final ActivityType8 instance = new ActivityType8();

	/**
	 * 构造函数
	 */
	private ActivityType8() {
	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static ActivityTypeBase getInstance() {
		return ActivityType8.instance;
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	public int getType() {
		return ActivityType8.type;
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
		JSONObject json = Utils.toJSONObject(paramStr);
		if (json == null) {
			return zoneItems;
		}
		JSONArray awards = json.getJSONArray("awards");
		long price = json.getLongValue("fundPrice");
		long ceof = json.getLongValue("double");
		long days = json.getLongValue("days");

		ActivityZoneItemObject zoneItemObj0 = new ActivityZoneItemObject(
				0);
		ActivityParamObject paramObj0 = new ActivityParamObject();
		paramObj0.numParams.add(price);
		paramObj0.numParams.add(ceof);
		paramObj0.numParams.add(days);

		zoneItemObj0.addParam(paramObj0);
		zoneItems.add(zoneItemObj0);

		for (int i = 0; i < awards.size(); i++) {
			JSONObject rewardJson = awards.getJSONObject(i);
			int aid = rewardJson.getIntValue("aid");
			ActivityZoneItemObject zoneItemObj = new ActivityZoneItemObject(
					aid);
			ActivityParamObject paramObj = new ActivityParamObject();

			JSONArray exp = rewardJson.getJSONArray("experience");
			int itemSn = exp.getIntValue(0);
			int itemCount = exp.getIntValue(1);
			DItem.Builder ditem = DItem.newBuilder();
			ditem.setItemSn(itemSn);
			ditem.setNum(itemCount);
			paramObj.itemParams.add(ditem.build());

			JSONArray diamond = rewardJson.getJSONArray("diamond");
			itemSn = diamond.getIntValue(0);
			itemCount = diamond.getIntValue(1);
			ditem = DItem.newBuilder();
			ditem.setItemSn(itemSn);
			ditem.setNum(itemCount);
			paramObj.itemParams.add(ditem.build());

			JSONArray prestige = rewardJson.getJSONArray("prestige");
			itemSn = prestige.getIntValue(0);
			itemCount = prestige.getIntValue(1);
			ditem = DItem.newBuilder();
			ditem.setItemSn(itemSn);
			ditem.setNum(itemCount);
			paramObj.itemParams.add(ditem.build());

			zoneItemObj.addParam(paramObj);
			zoneItems.add(zoneItemObj);
		}
		return zoneItems;
	}

	/**
	 * 获取给客户端的参数
	 * 
	 * @param activity
	 * @return
	 */
	@Override
	public Param getShowParam(ActivityObject activity, HumanObject humanObj,
			List<DActivityZoneItem> zoneList) {
		if (activity.zoneItems.size() < 1) {
			return null;
		}
		Map<Integer, Long> paramList = getHumanActivityDataList(activity,
				humanObj);
		boolean showPoint = false;
		DActivityZoneItem.Builder dz = DActivityZoneItem.newBuilder();
		dz.setZone(1);
		// 获取不在线天数
		Long offday = paramList.get(1);
		if (offday == null || offday == 0l) {
			return null;
		}
		List<ActivityZoneItemObject> zoneItemList0 = activity.zoneItems
				.get(0);
		ActivityZoneItemObject zoneItem0 = zoneItemList0.get(0);
		ActivityParamObject zoneParam0 = zoneItem0.getParams();
		long price = zoneParam0.numParams.get(0);// 价格
		long ceof = zoneParam0.numParams.get(1);// 加成
		long days = zoneParam0.numParams.get(2);// 周期
		if (offday > days) {
			offday = days;
		}
		List<ActivityZoneItemObject> zoneItemList = activity.zoneItems
				.get(offday.intValue());
		ActivityZoneItemObject zoneItem = zoneItemList.get(0);
		ActivityParamObject zoneParam = zoneItem.params.get(0);
		boolean notshow = true;
		// 信息
		for (int i = 0; i < 3; ++i) {
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			dp.addNumParam(i + 1);
			DItem item = zoneParam.itemParams.get(i);
			dp.addItems(item);// 物品
			// 免费是否领取
			Long value = paramList.get(i + 301);
			if (offday > 0 && value != null && value.longValue() > 0) {
				dp.addNumParam(1);
			} else {
				dp.addNumParam(0);
			}
			// 付费是否领取
			Long value1 = paramList.get(i + 401);
			if (offday > 0 && value1 != null && value1.longValue() > 0) {
				dp.addNumParam(1);
			} else {
				dp.addNumParam(0);
			}
			long freeNum = 0;
			// 倍数
			dp.addNumParam(ceof);
			// 免费数量
			if (offday < 1) {
				dp.addNumParam(0);
			} else {
				dp.addNumParam(item.getNum());
				if (offday > 0 && (value == null || value.longValue() <= 0)) {// 有，未领取
					showPoint = true;
					notshow = false;
					freeNum = item.getNum();
				}
			}
			// 付费数量
			if (offday < 1) {
				dp.addNumParam(0);
			} else {
				dp.addNumParam(item.getNum() * ceof / 100 + freeNum);
				if (offday > 0 && (value == null || value.longValue() <= 0)) {// 有，未领取
					showPoint = true;
					notshow = false;
				}
			}
			// 付费价格
			dp.addNumParam(price);
			dz.addActivityParams(dp.build());
		}
		zoneList.add(dz.build());
		Param param = new Param();
		param.put("showPoint", showPoint);
		param.put("getNoShow", notshow);
		return param;
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
		ActivityParamObject params = paramList.get(0);
		if (params == null) {
			return false;
		}
		long rewardindex = 0;
		boolean bdiamond = false;
		if (params != null) {
			if (params.numParams.size() > 0) {
				rewardindex = params.numParams.get(0);
			}
			if (params.numParams.size() > 1) {
				bdiamond = params.numParams.get(1) == 1L;
			}
		}
		Map<Integer, Long> paramLists = getHumanActivityDataList(activity, humanObj);
		// if(paramLists.size()>0){
		// Long day = paramLists.get(0);
		// if(day != null && day < getNowDateValue()){
		// paramLists.clear();
		// }
		// }
		long offday = paramLists.get(1);
		if (offday < 1) {
			return false;
		}
		List<ActivityZoneItemObject> zoneItemList0 = activity.zoneItems
				.get(0);
		ActivityZoneItemObject zoneItem0 = zoneItemList0.get(0);
		ActivityParamObject zoneParam0 = zoneItem0.getParams();
		long price = zoneParam0.numParams.get(0);
		long ceof = zoneParam0.numParams.get(1);
		long days = zoneParam0.numParams.get(2);
		if (offday > days) {
			offday = days;
		}
		List<ActivityZoneItemObject> zoneItemList = activity.zoneItems
				.get((int)offday);
		if (zoneItemList == null) {
			zoneItemList = activity.zoneItems.get(1);
		}
		ActivityZoneItemObject zoneItem = zoneItemList.get(0);
		ActivityParamObject zoneParam = null;
		DItem item = null;
		if (zoneItem != null) {
			zoneParam = zoneItem.params.get(0);
			if (zoneParam != null) {
				item = zoneParam.itemParams.get((int) rewardindex - 1);
			}
		}

		if (bdiamond) {// 付费，领取时同时领取免费的
			Long value = paramLists.get((int) rewardindex + 300);
			Long value1 = paramLists.get((int) rewardindex + 400);
			if (value1 != null && value1.longValue() > 0) {
				return false;
			}
			
			// 扣元宝
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, (int) price, LogSysModType.ActExpRecover)) {
				return false;
			}

			// 数据提交数据库
			List<Integer> snList = new ArrayList<>();// 索引列表
			Map<Integer, Long> numValues = new HashMap<>();// 数值参数表
			if (offday > 0 && (value == null || value < 1L)) {// 免费一起给
				snList.add((int) rewardindex + 300);
				numValues.put((int) rewardindex + 300, 1L);
			}
			snList.add((int) rewardindex + 400);
			numValues.put((int) rewardindex + 400, 1L);
			commitHumanActivityData(activity, humanObj, snList, numValues);

			// 奖励
			List<ProduceVo> proList = new ArrayList<>();
			ProduceVo pro = new ProduceVo(item.getItemSn(), (int) (item.getNum()
					* ceof / 100));
			proList.add(pro);
			if (offday > 0 && (value == null || value < 1L)) {// 免费一起标记
				pro = new ProduceVo(item.getItemSn(), item.getNum());
				proList.add(pro);
			}
			ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActExpRecover);
			ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
			return true;
		} else {
			Long value = paramLists.get((int) rewardindex + 300);
			if (value != null && value.longValue() > 0) {
				return false;
			}

			// 数据提交数据库
			List<Integer> snList = new ArrayList<>();// 索引列表
			Map<Integer, Long> numValues = new HashMap<>();// 数值参数表
			snList.add((int) rewardindex + 300);
			numValues.put((int) rewardindex + 300, 1L);
			commitHumanActivityData(activity, humanObj, snList, numValues);

			// 奖励
			List<ProduceVo> proList = new ArrayList<>();
			ProduceVo pro = new ProduceVo(item.getItemSn(), item.getNum());
			proList.add(pro);
			ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActExpRecover);
			ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
			return true;
		}
	}

	/**
	 * 获取玩家的活动数据
	 * 
	 * @param activity
	 * @param humanObj
	 * @return
	 */
	private Map<Integer, Long> getHumanActivityDataList(
			ActivityObject activity, HumanObject humanObj) {
		Map<Integer, Long> paramList = new HashMap<>();
		Map<Integer, ActivityHumanData> activityData = humanObj.activityDatas
				.get(activity.id);
		if (activityData != null) {
			for (Map.Entry<Integer, ActivityHumanData> entry : activityData
					.entrySet()) {
				paramList.put(entry.getKey(), entry.getValue().getNumValue());
			}
		}
		return paramList;
	}

	/**
	 * 监听事件触发
	 * 
	 * @param event
	 * @param param
	 */
	@Override
	public boolean onTrigger(int event, ActivityObject activity, Param param) {
		if (activity.zoneItems.size() < 1) {
			return false;
		}
		switch (event) {
		case EventKey.HumanLoginFinishFirstToday:
			HumanObject humanObj = param.get("humanObj");
			long loginTime = humanObj.getHuman().getTimeLogin();// 本次登入时间
			long logouTime = humanObj.getHuman().getTimeLogout();// 最后一次登出时间
			int day = Utils.getDaysBetween(loginTime, logouTime);
			if (logouTime != 0 && loginTime > logouTime && day > 1) {//离线超过1天
				// 如果玩家存在着时间
				List<Integer> snList = new ArrayList<>();// 索引列表
				Map<Integer, Long> numValues = new HashMap<>();// 数值参数表
				snList.add(1);
				numValues.put(1, day - 1l);// 可以领取几天
				for (int i = 0; i < 3; ++i) {
					// 清空领取标志
					snList.add(i + 301);
					numValues.put(i + 301, 0L);
					snList.add(i + 401);
					numValues.put(i + 401, 0L);
				}
				commitHumanActivityData(activity, humanObj, snList, numValues);
				return true;
			}
			break;
		}
		return false;
	}
}
