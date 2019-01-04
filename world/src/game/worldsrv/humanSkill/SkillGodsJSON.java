package game.worldsrv.humanSkill;

import java.io.IOException;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DSkillGods;
import game.worldsrv.config.ConfSkillGodsStar;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Utils;

/**
 * 
 * @author Neak
 */
public class SkillGodsJSON implements ISerilizable{
	public static final String tagKey = "tg";		// 该key为爆点tag
	public static final String lvKey = "lv";		// 该key为爆点lv
	public static final String expKey = "exp";      // 该key为爆点exp
	public static final String starKey = "st";		// 该key为爆点星级
	public static final String adKey = "ad";        // 该key为爆点附加技能sn
	
	public int tag;
	public int lv = 1; 	  // 爆点等级
	public int exp = 0;	  // 爆点当前经验（不是总经验）
	public int star = 0;  // 爆点当前星级
	public int ad = 0;    // 爆点附加技能
	
	public SkillGodsJSON(){
		
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(tag);
		out.write(lv);
		out.write(exp);
		out.write(star);
		out.write(ad);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		tag = in.read();
		lv = in.read();
		exp = in.read();
		star = in.read();
		ad = in.read();
	}
	
	/**
	 * 生成新的技能符文
	 * @param tag
	 * @param lv
	 * @param ad 附加技能
	 */
	public SkillGodsJSON(int tag, int lv, int ad) {
		this.tag = tag;
		this.lv = lv;
		this.exp = 0;
		this.star = 0;
		this.ad = ad;
	}
	
	/**
	 * 解析json转成技能符文对象
	 * @param jo
	 */
	public SkillGodsJSON(JSONObject jo) {
		this.tag = jo.getIntValue(tagKey);
		this.lv = jo.getIntValue(lvKey);
		this.exp = jo.getIntValue(expKey);
		this.star = jo.getIntValue(starKey);
		this.ad = jo.getIntValue(adKey);
	}
	
	/**
	 * 增加技能数据
	 * @return 玩家技能符文json
	 */
	public JSONObject toJson() {
		JSONObject jo = new JSONObject();
		jo.put(tagKey, tag);
		jo.put(lvKey, lv);
		jo.put(expKey, exp);
		jo.put(starKey, star);
		jo.put(adKey, ad);
		return jo;
	}
	
	/**
	 * 设置等级
	 * @param lv
	 */
	public void setLv(int lv){
		this.lv = lv;
	}
	/**
	 * 爆点升级
	 */
	public void addLv() {
		this.lv += 1;
	}
	
	/**
	 * 设置经验
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}
	
	/**
	 * 附加技能设置
	 */
	public void setAdd(int addSn){
		ad = addSn;
	}
	
	/**
	 * 设置爆点星级
	 */
	public void setStar(int star) {
		this.star = star;
	}
	
	/**
	 * 获得该爆点当前的技能sn
	 * @return
	 */
	public int getGodsSkillSn() {
		ConfSkillGodsStar conf = ConfSkillGodsStar.get(ConfigKeyFormula.getSkillGodsStarSn(tag, star));
		if (conf == null) {
			return 0;
		}
		return conf.skillSn;
	}
	
	/**
	 * 设置符文信息进协议包
	 * @return DSkillRune符文信息
	 */
	public DSkillGods createDSkillGods(){
		DSkillGods.Builder srBd = DSkillGods.newBuilder();
		srBd.setGodsTag(tag);
		srBd.setGodsLv(lv);
		srBd.setExp(exp);
		srBd.setStar(star);
		srBd.setAdditionSkillSn(ad);
		return srBd.build();
	}
	
	/**
	 * 修改爆点数据
	 * @param gods
	 */
	public static String modify(String godsJSON, SkillGodsJSON gods){
		String ret = godsJSON;
		JSONArray ja = Utils.toJSONArray(godsJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if (jo.getIntValue(tagKey) == gods.tag) {
				// 不用replace，而是用clear重新put，是为了以后拓展新字段更便捷，replace无法替换没有的新增字段
				jo.clear();
				jo.put(tagKey, gods.tag);
				jo.put(lvKey, gods.lv);
				jo.put(expKey, gods.exp);
				jo.put(starKey, gods.star);
				jo.put(adKey, gods.ad);
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
	public static String mapToJSON(Map<Integer, SkillGodsJSON> map) {
		JSONArray ja = new JSONArray();
		for (SkillGodsJSON godsJSON : map.values()) {
			JSONObject jo = new JSONObject();
			jo.put(tagKey, godsJSON.tag);
			jo.put(lvKey, godsJSON.lv);
			jo.put(expKey, godsJSON.exp);
			jo.put(starKey, godsJSON.star);
			jo.put(adKey, godsJSON.ad);
			ja.add(jo);
		}
		return ja.toJSONString();
	}
	
	/**
	 * 上阵爆点技能JSON
	 */
	public static String installGodsJSON(SkillGodsJSON godsJSON) {
		if (godsJSON == null) {
			return "";
		}
		JSONObject jo = new JSONObject();
		jo.put(tagKey, godsJSON.tag);
		jo.put(lvKey, godsJSON.lv);
		jo.put(expKey, godsJSON.exp);
		jo.put(starKey, godsJSON.star);
		jo.put(adKey, godsJSON.ad);
		return jo.toJSONString();
	}
}
