package game.worldsrv.tower;

import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import game.msg.Define.DTowerRewardBox;
import game.msg.Define.ETowerCardState;
import game.msg.Define.ETowerDifficulty;
import game.worldsrv.support.Utils;

/**
 * @author Neak
 * @see ：奖励宝箱实例
 */
public class TowerBoxJSON {
	public static final int TOWRR_CARD_NUM = 3;
	
	private static final String layerKey = "l"; // key：layer 所在层数
	private static final String diffKey = "df"; // key：diff 难度
	private static final String dropKey = "d"; // key:Reward 宝箱sn
	private static final String cardKey = "c"; // Key：card 翻牌情况
	private static final String drop1Key = "d1"; // key:costReward 宝箱sn	
	private static final String card1Key = "c1"; // Key：costCard 翻牌情况
	
	public int layer = 0; // 宝箱所在层
	public int diff = 0; // 宝箱所属难度
	public int dropId = 0; // 宝箱sn
	public int[] cardStates = new int[TOWRR_CARD_NUM]; // 宝箱翻牌情况(0没翻，1翻牌)
	
	public int dropId1 = 0; // 宝箱sn
	public int[] cardStates1 = new int[TOWRR_CARD_NUM]; // 宝箱翻牌情况(0没翻，1翻牌)
	
	public TowerBoxJSON() {}
	
	public TowerBoxJSON(int l, int d) {
		layer = l;
		dropId = d;
	}
	
	public TowerBoxJSON(int l, int dif, int dId0, int dId1) {
		layer = l;
		diff = dif;
		dropId = dId0;
		dropId1 = dId1;
	}
	
	
	public TowerBoxJSON(JSONObject jo) {
		layer = jo.getIntValue(layerKey);
		diff = jo.getIntValue(diffKey);
		dropId = jo.getIntValue(dropKey);
		String stateStr = jo.getString(cardKey);
		cardStates = Utils.strToIntArray(stateStr);
		
		dropId1 = jo.getIntValue(drop1Key);
		String stateStr1 = jo.getString(card1Key);
		cardStates1 = Utils.strToIntArray(stateStr1);
	}
	
	/**
	 * 获取翻牌情况字符串
	 */
	public String getCardStates() {
		String ret = cardStates[0] + "," + cardStates[1] + "," + cardStates[2];
		return ret;
	}
	/**
	 * 获取元宝翻牌情况字符串
	 */
	public String getCardStates1() {
		String ret = cardStates1[0] + "," + cardStates1[1] + "," + cardStates1[2];
		return ret;
	}
	
	/**
	 * 判断是否重复翻牌
	 * @param isCost 是否是元宝翻牌
	 */
	public boolean isRepeatOpen(int index, boolean isCost) {
		int[] states = cardStates;
		if (isCost) {
			states = cardStates1;
		}
		// 翻的卡牌index溢出，或者该张牌已经被翻开
		if (index + 1 > states.length || states[index] == ETowerCardState.TowerCardOpen_VALUE) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获得该宝箱被翻的卡牌数量
	 */
	public int getOpenCount(boolean isCost) {
		int[] states = cardStates;
		if (isCost) {
			states = cardStates1;
		}
		int count = 0;
		for (int cardState : states) {
			if (cardState == ETowerCardState.TowerCardOpen_VALUE) {
				count ++;
			}
		}
		return count;
	}
	
	/**
	 * 根据
	 * @param openIndex
	 */
	public void setCardOpen(int openIndex, boolean isCost) {
		if (isCost) {
			cardStates1[openIndex] = ETowerCardState.TowerCardOpen_VALUE;
		} else {
			cardStates[openIndex] = ETowerCardState.TowerCardOpen_VALUE;
		}
	}
	
	/**
	 * 复制到协议DTowerRewardBox 
	 */
	public DTowerRewardBox createDTowerRewardBox() {
		DTowerRewardBox.Builder msg = DTowerRewardBox.newBuilder();
		msg.setTowerLayer(layer);
		msg.setDiff(ETowerDifficulty.valueOf(diff));
		msg.setRewardBoxSn(dropId);
		msg.addAllCardStateList(Utils.intToIntegerList(cardStates));
		msg.setRewardBoxSn1(dropId1);
		msg.addAllCardStateList1(Utils.intToIntegerList(cardStates1));
		return msg.build();
	}
	
	//////////////////////////////////////////////
	/**
	 * 该层对象转JSON
	 */
	public String toJSONString() {
		JSONObject jo = new JSONObject();
		jo.put(layerKey, layer); // 层级
		jo.put(diffKey, diff);
		jo.put(dropKey, dropId); // 宝箱id
		jo.put(cardKey, getCardStates()); // 翻牌情况
		jo.put(drop1Key, dropId1); 
		jo.put(card1Key, getCardStates1()); // 翻牌情况
		return jo.toJSONString();
	}
	
	/**
	 * 增加一个宝箱的数据
	 * @param tbJSON
	 * @param box
	 * @return 玩家匹配层数的json
	 */
	public static String add(String tbJSON, TowerBoxJSON box) {
		String ret = tbJSON;
		JSONArray ja = Utils.toJSONArray(tbJSON);
		JSONObject jo = new JSONObject();
		jo.put(layerKey, box.layer); // 层级
		jo.put(diffKey, box.diff); // 难度
		jo.put(dropKey, box.dropId); // dropId
		jo.put(cardKey, box.getCardStates()); // 普通宝箱翻牌情况
		jo.put(drop1Key, box.dropId1); // 消费翻牌dropId
		jo.put(card1Key, box.getCardStates1()); // 消费翻牌情况
		ja.add(jo);
		ret = ja.toJSONString();
		return ret;
	}
	
	/**
	 * 修改一个宝箱的数据
	 * @param tbJSON
	 * @param box
	 * @return 玩家匹配层数的json
	 */
	public static String modify(String tbJSON, TowerBoxJSON box) {
		String ret = tbJSON;
		JSONArray ja = Utils.toJSONArray(tbJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if (jo.getIntValue(layerKey) == box.layer) {
				// 不用replace，而是用clear重新put，是为了以后拓展新字段更便捷，replace无法替换没有的新增字段
				jo.clear();
				jo.put(layerKey, box.layer);
				jo.put(diffKey, box.diff);
				jo.put(dropKey, box.dropId);
				jo.put(cardKey, box.getCardStates());
				jo.put(drop1Key, box.dropId1);
				jo.put(card1Key, box.getCardStates1());
				ret = ja.toJSONString();
				break;
			}
		}
		return ret;
	}
	
	/**
	 * Map转换为JSON
	 * @param map
	 * @return
	 */
	public static String mapToJSON(Map<Integer, TowerBoxJSON> map) {
		JSONArray ja = new JSONArray();
		for (TowerBoxJSON tbJSON : map.values()) {
			JSONObject jo = new JSONObject();
			jo.put(layerKey, tbJSON.layer);
			jo.put(diffKey, tbJSON.diff);
			jo.put(dropKey, tbJSON.dropId);
			jo.put(cardKey, tbJSON.getCardStates());
			jo.put(drop1Key, tbJSON.dropId1);
			jo.put(card1Key, tbJSON.getCardStates1());
			ja.add(jo);
		}
		return ja.toJSONString();
	}
}
