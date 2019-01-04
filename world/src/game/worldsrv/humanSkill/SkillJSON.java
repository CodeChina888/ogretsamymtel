package game.worldsrv.humanSkill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DSkill;
import game.msg.Define.DSkillInfo;
import game.msg.Define.DSkillRune;
import game.worldsrv.support.Utils;

/**
 * 
 * @author Neak
 */
public class SkillJSON implements ISerilizable{
	private static final String tagKey = "tg";		// 该key为技能标识tag
	private static final String lvKey = "lv";		// 该key为技能lv
	private static final String stKey = "st";	// 该key为技能stage
	private static final String runeKey = "ru";   // 该key为runeKey
	
	public int tag;
	public int lv; 	  // 技能等级
	public int stage; // 技能阶段
	// 该技能拥有的符文
	private HashMap<Integer, SkillRuneJSON> skillRuneMap = new HashMap<>();
	
	public SkillJSON() {
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(tag);
		out.write(lv);
		out.write(stage);
		out.write(skillRuneMap);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		skillRuneMap.clear();
		
		tag = in.read();
		lv = in.read();
		stage = in.read();
		skillRuneMap.putAll(in.<Map<Integer, SkillRuneJSON>> read());
	}
	
	/**
	 * 生成新的技能
	 * @param tag
	 * @param lv
	 */
	public SkillJSON(int tag, int lv) {
		this.tag = tag;
		this.lv = lv;
		this.stage = 0;
	}
	
	/**
	 * 解析json转成技能对象
	 * @param jo
	 */
	public SkillJSON(JSONObject jo) {
		this.tag = jo.getIntValue(tagKey);
		this.lv = jo.getIntValue(lvKey);
		this.stage = jo.getIntValue(stKey);
		String runeStr = jo.getString(runeKey);
		JSONArray ja = Utils.toJSONArray(runeStr);
		if(ja.isEmpty()){
			return;
		}
		for (int i = 0; i < ja.size(); i++) {
			SkillRuneJSON sr = new SkillRuneJSON(ja.getJSONObject(i));
			skillRuneMap.put(sr.sn, sr);
		}		
	}
	
	/**
	 * 技能等级设置
	 * @param lv
	 */
	public void setLv(int lv){
		this.lv = lv;
	}
	
	/**
	 * 技能等级增加
	 * @param lv
	 */
	public void addLv(int lv){
		this.lv += lv;
	}
	
	/**
	 * 设置技能阶级
	 * @param stage 技能阶级
	 */
	public void setStage(int stage) {
		this.stage = stage;
	}
	/**
	 * 技能阶级增加
	 * @param stage 技能阶级
	 */
	public void addStage(int stage) {
		this.stage += stage;
	}
	
	/**
	 * 获取技能符文
	 * @return
	 */
	public List<SkillRuneJSON> getSkillRuneList(){
		List<SkillRuneJSON> list =  new ArrayList<>(skillRuneMap.values());
		return list;
	}
	/**
	 * 获取技能符文
	 * @param runeSn 符文sn
	 * @return
	 */
	public SkillRuneJSON getSkillRune(int runeSn){
		return skillRuneMap.get(runeSn);
	}
	/**
	 * 新增激活的技能符文
	 * @param sn
	 * @param lv
	 */
	public void addRune(int sn, int lv){
		SkillRuneJSON rune = new SkillRuneJSON(sn, lv);
		skillRuneMap.put(sn, rune);
	}
	
	/**
	 * 技能符文等级修改
	 * @param sn
	 * @param lv
	 */
	public void setRuneLv(int sn, int lv){
		SkillRuneJSON rune = skillRuneMap.get(sn);
		rune.setLv(lv);
	}
	
	
	/**
	 * 设置数据进技能详细（包含符文）协议里
	 * @return DSkillInfo 技能完整包
	 */
	public DSkillInfo createDSkillInfo() {
		DSkillInfo.Builder msg = DSkillInfo.newBuilder();
		DSkill.Builder skill = msg.getSkillBuilder();
		skill.setSkillTag(tag);
		skill.setLv(lv);
		skill.setStage(stage);
		for (SkillRuneJSON runeJSON : skillRuneMap.values()) {
			DSkillRune rune = runeJSON.createDSkillRune(tag);
			msg.addSkillRune(rune);
		}
		return msg.build();
	}
	
	/**
	 * 设置数据至技能协议
	 */
	public DSkill createDSkill() {
		DSkill.Builder msg = DSkill.newBuilder();
		msg.setSkillTag(tag);
		msg.setLv(lv);
		msg.setStage(stage);
		return msg.build();
	}
	
	
	/**
	 * 增加技能数据
	 * @param skillJSON
	 * @param skill
	 * @return 玩家技能json
	 */
	public static String add(String skillJSON, SkillJSON skill) {
		String ret = skillJSON;
		JSONArray ja = Utils.toJSONArray(skillJSON);
		JSONObject jo = new JSONObject();
		jo.put(SkillJSON.tagKey, skill.tag);
		jo.put(SkillJSON.lvKey, skill.lv);
		jo.put(SkillJSON.stKey, skill.stage);
		jo.put(SkillJSON.runeKey, "");
		ja.add(jo);
		ret = ja.toJSONString();
		return ret;
	}
	
	/**
	 * 修改技能数据
	 * @return 玩家技能json
	 */
	public static String modify(String skillJSON, SkillJSON skill) {
		String ret = skillJSON;
		JSONArray ja = Utils.toJSONArray(skillJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if (jo.getIntValue(SkillJSON.tagKey) == skill.tag) {
				// 不用replace，而是用clear重新put，是为了以后拓展新字段更便捷，replace无法替换没有的新增字段
				jo.clear();
				jo.put(SkillJSON.tagKey, skill.tag);
				jo.put(SkillJSON.lvKey, skill.lv);
				jo.put(SkillJSON.stKey, skill.stage);
				JSONArray runeJa = new JSONArray();
				for (SkillRuneJSON runeJSON : skill.skillRuneMap.values()) {
					JSONObject runeJo = runeJSON.toJson();
					runeJa.add(runeJo);
				}
				jo.put(SkillJSON.runeKey, runeJa.toJSONString());
				
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
	public static String mapToJSON(Map<Integer, SkillJSON> map) {
		JSONArray ja = new JSONArray();
		for (SkillJSON skillJSON : map.values()) {
			JSONObject jo = new JSONObject();
			jo.put(tagKey, skillJSON.tag);
			jo.put(lvKey, skillJSON.lv);
			jo.put(stKey, skillJSON.stage);
			JSONArray runeJa = new JSONArray();
			for (SkillRuneJSON runeJSON : skillJSON.skillRuneMap.values()) {
				JSONObject runeJo = runeJSON.toJson();
				runeJa.add(runeJo);
			}
			ja.add(jo);
			jo.put(runeKey, runeJa.toJSONString());
		}
		return ja.toJSONString();
	}
	
	/**
	 * 获得上阵技能的JSON
	 */
	public static String installSkillJSON(Map<Integer, SkillJSON> map, List<Integer> skillGroup) {
		JSONArray ja = new JSONArray();
		for (SkillJSON skillJSON : map.values()) {
			if (!skillGroup.contains(skillJSON.tag)) {
				continue;
			}
			JSONObject jo = new JSONObject();
			jo.put(tagKey, skillJSON.tag);
			jo.put(lvKey, skillJSON.lv);
			jo.put(stKey, skillJSON.stage);
			JSONArray runeJa = new JSONArray();
			for (SkillRuneJSON runeJSON : skillJSON.skillRuneMap.values()) {
				JSONObject runeJo = runeJSON.toJson();
				runeJa.add(runeJo);
			}
			ja.add(jo);
			jo.put(runeKey, runeJa.toJSONString());
		}
		return ja.toJSONString();
	}
}
