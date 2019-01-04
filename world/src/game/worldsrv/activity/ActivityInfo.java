package game.worldsrv.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.support.Utils;
import game.msg.Define.ERankType;
import game.worldsrv.character.HumanObject;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.activity.types.ActivityImmortalDiscount;
import game.worldsrv.activity.types.ActivityServerCompetition;
import game.worldsrv.activity.types.ActivityTypeDefine;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.support.RandomUtil;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.enumType.LogSysModType;

/**
 * 本服内存活动信息（不持久化）
 */
public class ActivityInfo{
	private static int SAVE_SIZE = 4;
	private static Map<Integer, List<ActivityInfo2>> activityMap = new HashMap<>();
	private static boolean Activity12 = false;// 限时资源产出加成
	private static Map<Integer, Integer> chapterAdd = new HashMap<Integer, Integer>();// 限时资源产出加成
	private static boolean Activity17 = false;	//是否开始打折
	private static boolean Activity22 = false;	//是否开始限时打折
	private static boolean Activity15 = false;	//限时掉落
	private static Map<Integer,List<Integer>> act15Item = new HashMap<>();//道具
	private static Map<Integer,List<Integer>> act15Num = new HashMap<>();
	private static Map<Integer,List<Integer>> act15Weight = new HashMap<>();
	public static ActivityObject act31 = null;


	public static List<ActivityInfo2> getDB(int actId){
		List<ActivityInfo2> info2 = new ArrayList<>();
		if (activityMap.containsKey(actId)) {
			info2.addAll(activityMap.get(actId));
		}
		return info2;
	}
	
	// 保存至内存只有打开整个界面才会下发所有的
	public static void saveActivity(int id,String name,List<ProduceVo> dList){
		List<ActivityInfo2> dataList = activityMap.get(id);
		if (dataList == null) {
			dataList = new ArrayList<>();
		}
		ActivityInfo2 info = new ActivityInfo2(dList, name);
		dataList.add(0, info);
		activityMap.put(id, dataList);
		if (dataList.size() >= SAVE_SIZE) {
			dataList.remove(SAVE_SIZE - 1);
		}
	}
	public static void act12ToOpen(ActivityObject activity){
		Activity12 = true;
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry : activity.zoneItems.entrySet()) {
			ActivityZoneItemObject object = entry.getValue().get(0);
			Long add = object.params.get(0).numParams.get(0);
			chapterAdd.put(entry.getKey(), add.intValue());
		}
	}
	public static void act12ToClose(){
		Activity12 = false;
	}
	public static boolean act12Get(){
		return Activity12;
	}
	public static int act12GetAdd(int chapteId){
		int add = 0;
		if (Activity12 && chapterAdd.containsKey(chapteId)) {
			add = chapterAdd.get(chapteId);
		}
		return add;
	}
	public static void act17ToOpen(){
		Activity17 = true;
	}
	public static void act17ToClose(){
		Activity17 = false;
	}

	public static boolean act17Get(){
		return Activity17;
	}
	

	public static void act22ToOpen(){
		Activity22 = true;
	}
	public static void act22ToClose(){
		Activity22 = false;
	}
	
	public static void act15ToOpen(ActivityObject activity){
		Activity15 = true;
		for (Map.Entry<Integer, List<ActivityZoneItemObject>> entry:activity.zoneItems.entrySet()) {
			if (entry.getKey() < 1000) {
				continue;
			}
			int chapteId = entry.getKey() - 1000;
			ActivityParamObject obj = entry.getValue().get(0).params.get(0);
			String item = obj.strParams.get(0);
			String weightStr = obj.strParams.get(1);
			Long min = obj.numParams.get(0);
			Long max = obj.numParams.get(1);
			List<Integer> items = Utils.strToIntList(item);
			List<Integer> num = new ArrayList<>();
			List<Integer> weights = Utils.strToIntList(weightStr);
			if (items.size() != weights.size()) {
				continue;
			}
			num.add(min.intValue());
			num.add(max.intValue());
			act15Item.put(chapteId, items);
			act15Num.put(chapteId, num);
			act15Weight.put(chapteId, weights);
		}
	}
	public static boolean act15Get(){
		return Activity15;
	}
	public static void act15ToClose(){
		Activity15 = false;
	}	
	/**
	 * 活动初始化的时候将某些活动存在服务器内存中
	 * @param activity
	 */
	public static void initToAddInfo(ActivityObject activity,boolean isInit){
		switch (activity.type) {
		case ActivityTypeDefine.Activity_TYPE_12:
			if (!activity.isValid()) {
				act12ToClose();
			}else{
				act12ToOpen(activity);
			}
			break;
		case ActivityTypeDefine.Activity_TYPE_15:
			if (!activity.isValid()) {
				act15ToClose();
			}else{
				act15ToOpen(activity);
			}
			break;
		case ActivityTypeDefine.Activity_TYPE_17:
			if (!activity.isValid()) {
				act17ToClose();
			}else{
				act17ToOpen();
			}
			break;
		case ActivityTypeDefine.Activity_TYPE_22:
			if (!activity.isValid()) {
				act22ToClose();
			}else{
				act22ToOpen();
			}
			break;
		case ActivityTypeDefine.Activity_TYPE_27:
			if(!activity.isValid() && !isInit){
				
			}
			break;
		case ActivityTypeDefine.Activity_TYPE_31:
			act31 = activity;
			break;
		default:
			break;
		}
	}
	
	public static int getAct31Discount(){
		int discount = 100;
		ActivityZoneItemObject zoneItem = act31.zoneItems.get(1).get(0);
		if(zoneItem == null){
			return discount;
		}
		ActivityParamObject paramObj = zoneItem.params.get(0);
		if(paramObj == null){
			return discount;
		}
		return paramObj.numParams.get(0).intValue();
	}
	public static ItemChange act15Loser(HumanObject humanObj, int chapter, LogSysModType key){
		ItemChange itemChange = new ItemChange();
		if (!Activity15) {
			return itemChange;
		}
		// 该章节不参与活动
		List<Integer> weightsList = act15Weight.get(chapter);
		if (weightsList == null) {
			return itemChange;
		}
		// 
		int index = RandomUtil.getRandomIndexByRate(weightsList);
		List<Integer> itemList = act15Item.get(chapter);
		if (itemList == null || itemList.size() < index + 1) {
			return itemChange;
		}
		int item = itemList.get(index);
		List<Integer> numList = act15Num.get(chapter);
		if (numList.size() != 2) {
			return itemChange;
		}
		int num = RandomUtil.randomIntValue(numList.get(0), numList.get(1));
		itemChange = RewardHelper.reward(humanObj, item, num, key);
		return itemChange;
	}

	/**
	 * 活动截止操作
	 * @param activity
	 */
	public static void updatePlanActivity(ActivityObject activity){
		switch (activity.type) {
		case ActivityTypeDefine.Activity_TYPE_27:
			RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
			proxy.actServerCompetitionPlanAbort();
			break;
		}
	}
}
