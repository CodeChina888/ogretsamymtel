package game.worldsrv.tower;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DTowerLayerEnemy;
import game.msg.Define.DTowerRewardBox;
import game.msg.Define.ETowerDifficulty;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfTower;
import game.worldsrv.entity.Tower;
import game.worldsrv.support.Utils;
import game.worldsrv.tower.TowerService.TowerMatchType;

public class TowerRecord implements ISerilizable{
	// 主角爬塔
	private Tower tower = new Tower();
	
	// 曾经挑战过的层数(层数和难度组合数
	private List<Integer> alreadyFightList = new ArrayList<>();
	
	// 爬塔领取的宝箱<层数，层对应的宝箱信息>
	private Map<Integer, TowerBoxJSON> boxMap = new HashMap<>();
	
	// 爬塔难度1数据 <层数，层对应的信息>
	private Map<Integer, TowerLayerJSON> diff1TowerMap = new HashMap<>();
	// 爬塔难度2数据 <层数，层对应的信息>
	private Map<Integer, TowerLayerJSON> diff2TowerMap = new HashMap<>();
	// 爬塔难度3数据 <层数，层对应的信息>
	private Map<Integer, TowerLayerJSON> diff3TowerMap = new HashMap<>();
	

	// 当前是否在挑战
	public boolean isFighting = false;

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(tower);
		out.write(isFighting);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		tower = in.read();
		parse(tower);
		isFighting = in.read();
	}
	
	public TowerRecord() {}
	
	/**
	 * 登录时，初始化玩家的爬塔信息
	 */
	public void init(Tower tower) {
		this.tower = tower;// 保存数据库记录
		parse(this.tower);// 解析数据库记录到内存数据
	}
	
	/**
	 * 解析数据库数据到内存
	 * @param tower
	 */
	private void parse(Tower tower) {
		// 解析曾经打过的层数
		alreadyFightList.clear();
		alreadyFightList = Utils.strToIntList(tower.getAlreadyFight());
		
		// 解析宝箱数据
		boxMap.clear();
		jsonToBoxMap(tower.getRewardBox(), boxMap);
		
		// 解析爬塔信息数据到Map
		diff1TowerMap.clear();
		diff2TowerMap.clear();
		diff3TowerMap.clear();
		jsonToDiffMap(tower.getDiffcultyLv1(), diff1TowerMap);
		jsonToDiffMap(tower.getDiffcultyLv2(), diff2TowerMap);
		jsonToDiffMap(tower.getDiffcultyLv3(), diff3TowerMap);
	}
	
	/**
	 * 获取爬塔数据
	 */
	public Tower getTower() {
		return tower;
	}
	
	/**
	 * 判断是否挑战成功过
	 * @return true 通关过
	 */
	 public boolean getAlreadyFight(int recordLayer) {
		 return alreadyFightList.contains(recordLayer);
	 }
	
	/**
	 * 根据层数，获取奖励宝箱
	 * @param layer 层数
	 * @return
	 */
	public TowerBoxJSON getTowerBoxJSON(int layer) {
		return boxMap.get(layer);
	}
	
	/**
	 * 根据层数和难度，获得该层的匹配信息
	 * @param layer 层数
	 * @param diff 难度
	 * @return
	 */
	public TowerLayerJSON getTowerLayerJSON(int layer, int diff) {
		switch (diff) {
		case ETowerDifficulty.TowerDiffLv1_VALUE:
			return diff1TowerMap.get(layer);
		case ETowerDifficulty.TowerDiffLv2_VALUE:
			return diff2TowerMap.get(layer);
		case ETowerDifficulty.TowerDiffLv3_VALUE:
			return diff3TowerMap.get(layer);
		}
		return null;
	}
	
	/**
	 * 根据层数，复制匹配信息到协议中
	 * @param layer 层数
	 * @return DTowerLayerEnemy
	 */
	public DTowerLayerEnemy createDTowerLayerEnemy(int layer) {
		TowerLayerJSON tl1 = diff1TowerMap.get(layer);
		TowerLayerJSON tl2 = diff2TowerMap.get(layer);
		TowerLayerJSON tl3 = diff3TowerMap.get(layer);
		DTowerLayerEnemy.Builder msg = DTowerLayerEnemy.newBuilder();
		// 通关或者异常，则写死一个类型发送给客户端，不发送匹配数据
		if (tl1 == null || tl2 == null || tl3 == null) {
			msg.setMatchLayerType(TowerMatchType.MatchArmy.value());
			return msg.build();
		}
		long humanId = tl1.humanId;
		// 如果id在怪物军团表中有的话，则类型为怪物
		if (ConfInstMonster.containsKey((int)humanId)) {
			// 类型：怪物
			msg.setMatchLayerType(TowerMatchType.MatchArmy.value());
		} else {
			// 类型：玩家镜像
			msg.setMatchLayerType(TowerMatchType.MatchHuman.value());
		}
		msg.addLayerEnemyList(tl1.createDTowerMatchEnemy());
		msg.addLayerEnemyList(tl2.createDTowerMatchEnemy());
		msg.addLayerEnemyList(tl3.createDTowerMatchEnemy());
		return msg.build();
	}
	
	/**
	 * 复制宝箱数据到协议中
	 */
	public List<DTowerRewardBox> createDTowerRewardBoxList() {
		List<DTowerRewardBox> list = new ArrayList<>();
		for(TowerBoxJSON boxJSON : boxMap.values()) {
			list.add(boxJSON.createDTowerRewardBox());
		}
		return list;
	}

	/**
	 * 新增一层爬塔数据
	 * @param diff
	 * @return
	 */
	public void putData(TowerLayerJSON tlJSON, int diff) {
		switch (diff) {
		case ETowerDifficulty.TowerDiffLv1_VALUE:
			diff1TowerMap.put(tlJSON.layer, tlJSON);
			break;
		case ETowerDifficulty.TowerDiffLv2_VALUE:
			diff2TowerMap.put(tlJSON.layer, tlJSON);
			break;
		case ETowerDifficulty.TowerDiffLv3_VALUE:
			diff3TowerMap.put(tlJSON.layer, tlJSON);
			break;
		}
	}
	
	/**
	 * 修改某一层的过关条件
	 * @param layer
	 */
	public void resetCondition(int layer) {
		ConfTower confTower = ConfTower.get(layer);
		// 权重得出条件索引
		int cdIndex = Utils.getRandRange(confTower.conditionWeight);
		TowerLayerJSON tlJSON = null;
		for (int i = ETowerDifficulty.TowerDiffLv1_VALUE; i <= ETowerDifficulty.TowerDiffLv3_VALUE; i++) {
			tlJSON = this.getTowerLayerJSON(layer, i);
			tlJSON.setCondition(confTower, cdIndex);
		}
		// 修改所有层数的匹配数据
		this.modifyAllLayer();
	}
	
	//////////////////////////////////////////////////////
	// 修改持久化数据
	/**
	 * 修改所有难度的所有匹配数据
	 */
	public void modifyAllLayer(){
		tower.setDiffcultyLv1(TowerLayerJSON.mapToJSON(diff1TowerMap));
		tower.setDiffcultyLv2(TowerLayerJSON.mapToJSON(diff2TowerMap));
		tower.setDiffcultyLv3(TowerLayerJSON.mapToJSON(diff3TowerMap));
	}
	
	/**
	 * 新增一个宝箱数据
	 */
	public void addRewardBox(TowerBoxJSON boxJSON) {
		if (!boxMap.containsKey(boxJSON.layer)) {
			boxMap.put(boxJSON.layer, boxJSON);
			// 数据改变则保存到数据库
			tower.setRewardBox(TowerBoxJSON.mapToJSON(boxMap));
			
			// 这个方法也可以
//			String rewardBox = tower.getRewardBox();
//			tower.setRewardBox(TowerBoxJSON.add(rewardBox, boxJSON));
		}
	}
	
	/**
	 * 修改一个宝箱数据	
	 */
	public void modifyRewardBox(TowerBoxJSON boxJSON){
		String rewardBox = tower.getRewardBox();
		tower.setRewardBox(TowerBoxJSON.modify(rewardBox, boxJSON));
	}
	
	/**
	 * 新增一层挑战的数据
	 * @param recordLayer layer*1000+diff
	 */
	public void addAlreadyFight(int recordLayer) {
		// 已经记录，则不再记录
		if (alreadyFightList.contains(recordLayer)) {
			return;
		}
		alreadyFightList.add(recordLayer);
		tower.setAlreadyFight(Utils.ListIntegerToStr(alreadyFightList));
	}
	///////////////////////////////////////////////////////
	
	/**
	 * 解析JSON数据到boxMap
	 */
	public static void jsonToBoxMap(String json, Map<Integer, TowerBoxJSON> boxMap) {
		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return;
		}
		for (int i = 0; i < ja.size(); i++) {
			TowerBoxJSON tlJSON = new TowerBoxJSON(ja.getJSONObject(i));
			boxMap.put(tlJSON.layer, tlJSON);                   
		}
	}
	
	/**
	 * 解析JSON数据到diff?TowerMap
	 */
	public static void jsonToDiffMap(String json, Map<Integer, TowerLayerJSON> diffMap) {
		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return;
		}
		for (int i = 0; i < ja.size(); i++) {
			TowerLayerJSON tlJSON = new TowerLayerJSON(ja.getJSONObject(i));
			diffMap.put(tlJSON.layer, tlJSON);                   
		}
	}

}
