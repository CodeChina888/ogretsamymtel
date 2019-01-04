package game.worldsrv.drop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfDropCounts;
import game.worldsrv.entity.DropInfo;
import game.worldsrv.entity.Human;
import game.worldsrv.support.GlobalConfVal;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;


public class DropManager extends ManagerBase  {

	//最大计数掉落的类型
	public static int MAX_COUNTTYPE = 100;
	
	 
	/**
	 * 获取实例
	 * @return
	 */
	public static DropManager inst() {
		return inst(DropManager.class);
	}
	/**
	 * 创角初始化
	 */
	@Listener(EventKey.HumanCreate)
	public void initDropData(Param param){
		Human human = param.get("human");
		DropInfo dropInfo = new DropInfo();
		dropInfo.setId(Port.applyId());
		dropInfo.setHumanId(human.getId());
		//设置初始化掉落信息 {"88":0,"89":0,"90":0,"91":0,.....}
		
		JSONObject countTypeJson = new JSONObject();
		for (int i = 0; i < MAX_COUNTTYPE; i++) {
			countTypeJson.put(String.valueOf(i), 0);
			//初始化humanObj
//			humanObj.dropCountMap.put(i, 0);
		}
		
		dropInfo.setCountType(countTypeJson.toJSONString());
		dropInfo.persist();
	
//		humanObj.dropInfo = dropInfo;
	}
	
	/**
	 * 加载数据
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_StageHumanEnter(Param params) {
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_StageHumanEnter humanObj is null");
			return;
		}
		
		//加载信息到humanObj
		DB dbPrx = DB.newInstance(DropInfo.tableName);
		dbPrx.getBy(false, DropInfo.K.HumanId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanDropInfo, "humanObj", humanObj);
		
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanDropInfo(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanPartner humanObj is null");
			return;
		}
		
		Record record = results.get();
 		if (record == null) {
 			Log.game.error("===_result_loadHumanDropInfo record is null");
		} else {
			// 加载数据到humanObj
			DropInfo drop = new DropInfo(record);
			JSONObject jsonObj = Utils.toJSONObject(drop.getCountType());
			for (int i = 0; i < MAX_COUNTTYPE; i++) {
				Integer value = jsonObj.getInteger(String.valueOf(i));
				humanObj.dropCountMap.put(i, value);
				humanObj.dropInfo = drop;
			}
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	/**
	 * 
	 * @param humanObj
	 * @param dropId
	 * @return
	 */
	public DropBag getItem(HumanObject humanObj,int dropId){
		//掉落分组，掉落道具 ，第一次概率过滤
		//读取配置表的物品列表
		List<DropItemVO> drop_from_conf = GlobalConfVal.getChanceMap().get(Integer.valueOf(dropId));
		if(drop_from_conf == null){
			Log.table.info("掉落表错误： dropID={}",dropId);
			return null;
		}
		//重新组织配表
		Map<Integer,List<DropItemVO>> rebuild_map = new HashMap<>();
		
		//第一次的掉落表
		List<DropItemVO> drop = new ArrayList<>();
		
		int groupId = 0;
		for (DropItemVO div : drop_from_conf) {
			
			if(div.getGroupId() != groupId){
				//groupId不同
				groupId = div.getGroupId();
				List<DropItemVO> l = new ArrayList<>();
				l.add(div);
				rebuild_map.put(groupId, l);
			}else{
				//重复
				rebuild_map.get(groupId).add(div);
			}
		}
		
		for (Map.Entry<Integer, List<DropItemVO>> entry : rebuild_map.entrySet()) {
			List<DropItemVO> list = entry.getValue();
			if(list.size() <= 1){
				//只有单个掉落的
				if(list.get(0).isGet()){
					drop.add(list.get(0));
				}
				
			}else if(list.size() >= 2){
				//多个掉落
				if(list.get(0).isGet()){
					int index = getRandRange(list);
					drop.add(list.get(index));
				}
			}
		}
		//第三次筛选，判断掉落计数是否掉落该物品
		DropBag dr = getDropBag(humanObj, drop, dropId);
		return dr;
	}
	
	/**
	 * 获取计数掉落物品
	 */
//	public DropBag getItem(HumanObject humanObj,int dropId){
//		//掉落分组，掉落道具 ，第一次概率过滤
//		//读取配置表的物品列表
//		List<DropItemVO> drop_from_conf = GlobalConfVal.getChanceMap().get(Integer.valueOf(dropId));
//		//第一次的掉落表
//		List<DropItemVO> drop = new ArrayList<>();
//		
//		
//		for (DropItemVO div : drop_from_conf) {
//			if(div.isGet()){
//				drop.add(div);
//			}
//		}
//		if(drop.size()==5){
//			System.out.println("终于掉了五个");
//		}
//		//如果没有掉落物品
//		if(drop.size() <= 0){
//			return new DropBag();
//		}
//		//第二次权重筛选,筛选权重相同的物品
//		drop = selectWeight(drop);
//		//第三次筛选，判断掉落计数是否掉落该物品
//		DropBag dr =  getDropBag( humanObj,drop, dropId);
//		return dr;
//	}
	/**
	 * 比较计数计数掉落数值，返回掉落物品包
	 * @param humanObj
	 * @param drop 第二步中掉落的物品List
	 * @param dropId
	 * @return
	 */
	public DropBag getDropBag(HumanObject humanObj,List<DropItemVO> drop,int dropId){
		
		Map<Integer,Integer> dropCountMap = humanObj.dropCountMap;
		//返还物品的背包
		DropBag dropBag = new DropBag();
		for (DropItemVO div : drop) {
			if(div == null){
				continue;
			}
			int need = getDropCount(div,dropId);
			if(div.presionCountType <= 0){
				//无计数类型
				dropBag.add(div);
				continue;
			}else if(dropCountMap.get(div.presionCountType) < need){
				//掉落计数不够，不放进背包
				//增加计数类型
				int presionCountType =dropCountMap.get(div.presionCountType);
				presionCountType++;
				//修改缓存数据
				humanObj.dropCountMap.put(div.presionCountType, presionCountType);
				//提交数据库
				DropInfo _drops = humanObj.dropInfo;
				_drops.setCountType(MapIntToString(humanObj.dropCountMap));
				continue;
			}else if(dropCountMap.get(div.presionCountType) >= need){
				//符合计数，不删除物品,重置计数
				DropInfo _drops = humanObj.dropInfo;
				humanObj.dropCountMap.put(div.presionCountType, 0);
				_drops.setCountType(MapIntToString(humanObj.dropCountMap));
				dropBag.add(div);
				continue;
			}else{
				Log.item.info("物品异常");
			}
		}
		return dropBag;
	}
	/**
	 * 依据 dropId 和 DropItemVO返回他的掉落计数
	 * @return
	 */
	public int getDropCount(DropItemVO drop,long dropId){
		int countType = drop.presionCountType;
		if(countType == 0){
			return -1;
		}
		ConfDropCounts dropCount = ConfDropCounts.get(countType);
		if(dropCount==null){
			Log.table.info(" ==getDropCount 配置表错误，错误countType {}",countType);
			return 0;
		}
		return dropCount.resetCount;
	}
	
	
	/**
	 * 依据DropItemVO 权重，去除重复
	 */
	public List<DropItemVO> selectWeight(List<DropItemVO> list){
		//最终物品列表
		List<DropItemVO> dropList  = new ArrayList<>();
		//权重列表
//		List<Integer> weightList = new ArrayList<>();
		//权重列表(物品)
		List<DropItemVO> weightList_vo = new ArrayList<>();
		int original = -1;
		
		DropItemVO lastDiv = null;//可能权重相同
		int f = 0;
		int f2 = 0;
		for (DropItemVO div : list) {
			if (div.getSn() == 31467){
				f = 1;
			}
			if (div.getSn() == 31461){
				f2 = 1;
			}
			if (div.getGroupId() != original) {
				//权重不同，下一个
				dropList.add(div);
				original = div.getGroupId() ;
				lastDiv = div;
				continue;
			}
			//权重重复,要加上前一个
			if (lastDiv!=null){
				weightList_vo.add(lastDiv);
				dropList.remove(lastDiv);
				lastDiv=null;
			}
			weightList_vo.add(div);
		}

		if(weightList_vo.size() < 2 ){
			//没有需要筛选的权重
			return dropList;
		}
		
		//依据权重计算下标
		int flag = getRandRange(weightList_vo);
		dropList.add(weightList_vo.get(flag));
		return dropList;
	}
	
	/**
	 * 根据权重取值，返回随机到的元素下标
	 * 
	 * @param rates
	 * @return
	 */
	private static  int getRandRange(List<DropItemVO> rates) {
		Integer baseRate = 0 ;
		List<Integer> l = new ArrayList<>();
		for (DropItemVO div : rates) {
			baseRate += div.weigh;
			l.add(div.weigh);
		}
		int r = (int) (Math.random() * baseRate);
		int c = 0;
		if (rates != null && !rates.isEmpty()) {
			for (int i = 0; i < rates.size(); i ++) {
				c += rates.get(i).weigh;
				if (r < c) {
					//System.out.println("fuck:"+l+" i:"+i);
					return i;
				}
			}
		}
		return -1;
	}
	
//	public static void main(String[] args) {
//		DropItemVO div= new DropItemVO();
//		div.setWeigh(1);
//		DropItemVO div2= new DropItemVO();
//		div2.setWeigh(1);
//		List<DropItemVO> rates = new ArrayList<>();
//		rates.add(div);
//		rates.add(div2);
//		int sum =0;
//		for (int i = 0; i <1000; i++) {
//			int x =getRandRange(rates);
//			if(x==1){
//				sum++;
//			}
//		}
//		System.out.println(sum);
//		
//	}
	/**
	 * human身上的dropCountMap转json格式
	 */
	public static String MapIntToString(Map<Integer,Integer> map){
		JSONObject json = new JSONObject();
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			json.put(String.valueOf(entry.getKey()), entry.getValue());
		}
		return json.toString();
	}

	
}
