package game.worldsrv.tower;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import game.msg.Define.DTowerMatchEnemy;
import game.worldsrv.config.ConfSceneEvent;
import game.worldsrv.config.ConfTower;
import game.worldsrv.support.Utils;

/**
 * @author Neak
 * @see :爬塔匹配参数
 */
public class TowerLayerJSON {
	private static final String layerKey = "l";	// 该key为层数 
	private static final String idKey = "id"; // 该key为humanId
	private static final String cbKey = "c"; // 该key为战斗力（combat）
	private static final String cdKey = "cd"; // 该key为条件（condition）
	
	public int layer;
	public long humanId;
	public int combat;
	public List<Integer> conditions = new ArrayList<>();
	
	public TowerLayerJSON() {}
	
	public TowerLayerJSON(int l, int id, int cb, List<Integer> cds) {
		layer = l;
		humanId = id;
		combat = cb;
		conditions = cds;
	}

	public TowerLayerJSON(JSONObject jo) {
		this.layer = jo.getIntValue(layerKey);
		this.humanId = jo.getLongValue(idKey);
		this.combat = jo.getIntValue(cbKey);
		String cdStr = jo.getString(cdKey); 
		this.conditions = Utils.strToIntList(cdStr);
	}
	
	/**
	 * 设置条件
	 * @param confTower
	 * @param cdIndex
	 */
	public void setCondition(ConfTower confTower, int cdIndex) {
		// 该层匹配的胜利条件
		if (cdIndex >= 0) {
			List<Integer> conditions = new ArrayList<>();
			String[] strAry = Utils.strToStrArray(confTower.condition);
			// 条件sn
			int[] condSnAry = Utils.strToIntArraySplit(strAry[cdIndex]);
			ConfSceneEvent confSceneEvent = null;
			for (int sn : condSnAry) {
				confSceneEvent = ConfSceneEvent.get(sn);
				if (confSceneEvent == null) {
					continue;
				}
				conditions.add(sn);
			}
			this.conditions = conditions;
		}
	}
	
	/**
	 * 获取过关条件字符串
	 */
	public String getCondition() {
		String ret = Utils.intListToStr(conditions);
		return ret;
	}
	
	/**
	 * 复制到协议DTowerMatchEnemy
	 */
	public DTowerMatchEnemy createDTowerMatchEnemy() {
		DTowerMatchEnemy.Builder msg = DTowerMatchEnemy.newBuilder();
		msg.setLayer(layer);
		msg.setHumanId(humanId);
		msg.setCombat(combat);
		msg.addAllConditionSns(conditions);
		return msg.build();
	}
	
	/**
	 * 该层对象转JSON
	 */
	public String toJSONString() {
		JSONObject jo = new JSONObject();
		jo.put(layerKey, layer); // 层级
		jo.put(idKey, humanId); // 玩家id
		jo.put(cbKey, combat); // 战斗力
		jo.put(cdKey, getCondition()); // 条件
		return jo.toJSONString();
	}
	
	/**
	 * 增加一层的数据
	 * @param tlJSON
	 * @param tm
	 * @return 玩家匹配层数的json
	 */
	public static String add(String tlJSON, TowerLayerJSON tm) {
		String ret = tlJSON;
		JSONArray ja = Utils.toJSONArray(tlJSON);
		JSONObject jo = new JSONObject();
		jo.put(layerKey, tm.layer); // 层级
		jo.put(idKey, tm.humanId); // 玩家id
		jo.put(cbKey, tm.combat); // 战斗力
		jo.put(cdKey, tm.getCondition()); // 条件
		ja.add(jo);
		ret = ja.toJSONString();
		return ret;
	}
	
	/**
	 * 修改一层的数据
	 * @param tlJSON
	 * @param tm
	 * @return 玩家匹配层数的json
	 */
	public static String modify(String tlJSON, TowerLayerJSON tm) {
		String ret = tlJSON;
		JSONArray ja = Utils.toJSONArray(tlJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if (jo.getIntValue(layerKey) == tm.layer) {
				// 不用replace，而是用clear重新put，是为了以后拓展新字段更便捷，replace无法替换没有的新增字段
				jo.clear();
				jo.put(layerKey, tm.layer);
				jo.put(idKey, tm.humanId);
				jo.put(cbKey, tm.combat);
				jo.put(cdKey, tm.getCondition());
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
	public static String mapToJSON(Map<Integer, TowerLayerJSON> map) {
		JSONArray ja = new JSONArray();
		for (TowerLayerJSON tlJSON : map.values()) {
			JSONObject jo = new JSONObject();
			jo.put(layerKey, tlJSON.layer);
			jo.put(idKey, tlJSON.humanId);
			jo.put(cbKey, tlJSON.combat);
			jo.put(cdKey, tlJSON.getCondition());
			ja.add(jo);
		}
		return ja.toJSONString();
	}
}
