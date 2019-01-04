package game.worldsrv.humanSkill;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DSkillRune;

/**
 * 
 * @author Neak
 */
public class SkillRuneJSON implements ISerilizable{
	public static final String snKey = "sn";		// 该key为技能符文sn
	public static final String lvKey = "lv";		// 该key为技能符文lv
	
	public int sn;
	public int lv; 	  // 技能等级
	
	
	
	public SkillRuneJSON() {
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(lv);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		lv = in.read();
	}

	/**
	 * 生成新的技能符文
	 * @param sn
	 * @param lv
	 */
	public SkillRuneJSON(int sn, int lv) {
		this.sn = sn;
		this.lv = lv;
	}
	
	/**
	 * 解析json转成技能符文对象
	 * @param jo
	 */
	public SkillRuneJSON(JSONObject jo) {
		this.sn = jo.getIntValue(snKey);
		this.lv = jo.getIntValue(lvKey);
	}
	
	/**
	 * 增加技能数据
	 * @return 玩家技能符文json
	 */
	public JSONObject toJson() {
		JSONObject jo = new JSONObject();
		jo.put(SkillRuneJSON.snKey, sn);
		jo.put(SkillRuneJSON.lvKey, lv);
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
	 * 设置符文信息进协议包
	 * @return DSkillRune符文信息
	 */
	public DSkillRune createDSkillRune(int skillSn){
		DSkillRune.Builder srBd = DSkillRune.newBuilder();
		srBd.setSkillSn(skillSn);
		srBd.setRuneSn(sn);
		srBd.setRuneLv(lv);
		return srBd.build();
	}

}
