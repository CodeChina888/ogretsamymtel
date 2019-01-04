package game.worldsrv.humanSkill;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DSkill;
import game.worldsrv.support.Utils;

/**
 * 
 * @author Neak
 */
public class SkillTrainJSON implements ISerilizable{
	private static final String typeKey = "type";		// 该key为神通标识tag
	private static final String stKey = "st";	// 该key为神通stage
	private static final String expKey = "exp";	// 该key为神通stage经验
	
	public int type;
	public int stage; // 神通阶段
	public int exp; // 神通阶段经验
	
	public SkillTrainJSON() {
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(type);
		out.write(stage);
		out.write(exp);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		type = in.read();
		stage = in.read();
		exp = in.read();
	}
	
	/**
	 * 生成新的神通
	 * @param tag
	 */
	public SkillTrainJSON(int tag) {
		this.type = tag;
		this.stage = 0;
		this.exp = 0;
	}
	
	/**
	 * 解析json转成神通对象
	 * @param jo
	 */
	public SkillTrainJSON(JSONObject jo) {
		this.type = jo.getIntValue(typeKey);
		this.stage = jo.getIntValue(stKey);
		this.exp = jo.getIntValue(expKey);
	}
	
	/**
	 * 设置神通阶级
	 * @param stage 神通阶级
	 */
	public void setStage(int stage) {
		this.stage = stage;
	}
	/**
	 * 神通阶级增加
	 * @param stage 神通阶级
	 */
	public void addStage(int stage) {
		this.stage += stage;
	}
	/**
	 * 设置神通升阶经验
	 * @param exp 神通升阶经验
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}
	
	/**
	 * 设置数据至神通协议
	 */
	public DSkill createDSkill() {
		DSkill.Builder msg = DSkill.newBuilder();
		msg.setSkillTag(type);
		msg.setStage(stage);
		msg.setStageExp(exp);
		return msg.build();
	}
	
	/**
	 * 增加神通数据
	 * @param skillJSON
	 * @param skill
	 * @return 玩家神通json
	 */
	public static String add(String skillJSON, SkillTrainJSON skill) {
		String ret = skillJSON;
		JSONArray ja = Utils.toJSONArray(skillJSON);
		JSONObject jo = new JSONObject();
		jo.put(SkillTrainJSON.typeKey, skill.type);
		jo.put(SkillTrainJSON.stKey, skill.stage);
		jo.put(SkillTrainJSON.expKey, skill.exp);
		ja.add(jo);
		ret = ja.toJSONString();
		return ret;
	}
	
	/**
	 * 修改神通数据
	 * @return 玩家神通json
	 */
	public static String modify(String skillJSON, SkillTrainJSON skill) {
		String ret = skillJSON;
		JSONArray ja = Utils.toJSONArray(skillJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if (jo.getIntValue(SkillTrainJSON.typeKey) == skill.type) {
				// 不用replace，而是用clear重新put，是为了以后拓展新字段更便捷，replace无法替换没有的新增字段
				jo.clear();
				jo.put(SkillTrainJSON.typeKey, skill.type);
				jo.put(SkillTrainJSON.stKey, skill.stage);
				jo.put(SkillTrainJSON.expKey, skill.exp);
				
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
	public static String mapToJSON(Map<Integer, SkillTrainJSON> map) {
		JSONArray ja = new JSONArray();
		for (SkillTrainJSON skillJSON : map.values()) {
			JSONObject jo = new JSONObject();
			jo.put(typeKey, skillJSON.type);
			jo.put(stKey, skillJSON.stage);
			jo.put(SkillTrainJSON.expKey, skillJSON.exp);
			ja.add(jo);
		}
		return ja.toJSONString();
	}
	
	/**
	 * 获得上阵神通的JSON
	 */
	public static String installSkillJSON(Map<Integer, SkillTrainJSON> map, List<Integer> skillGroup) {
		JSONArray ja = new JSONArray();
		for (SkillTrainJSON skillJSON : map.values()) {
			if (!skillGroup.contains(skillJSON.type)) {
				continue;
			}
			JSONObject jo = new JSONObject();
			jo.put(typeKey, skillJSON.type);
			jo.put(stKey, skillJSON.stage);
			jo.put(SkillTrainJSON.expKey, skillJSON.exp);
			ja.add(jo);
		}
		return ja.toJSONString();
	}
}
