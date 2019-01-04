package game.worldsrv.guild;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DGuildApplyHumanInfo;
import game.worldsrv.entity.Human;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Utils;
/**
 * 工会申请信息
 * @author songy
 *
 */
public class GuildApplyData implements ISerilizable {
	
	private static final String idKey = "id";				// 会员ID
	private static final String nameKey = "name";			// 会员名字
	private static final String levelKey = "lv";			// 当前等级
	private static final String combatKey = "combat";		// 战力
	private static final String timeKey = "time";			// 申请时间
	private static final String snKey = "sn";				// 主角sn
	private static final String aptitudeKey = "aptitude";		// 资质

	public long id = 0; 		// 会员ID
	public String name = ""; 	// 会员名字
	public int level = 1; 		// 当前等级
	public int combat = 0; 	// 战力
	public long time = 0;		// 申请时间
	public int sn = 0;			// 主角sn
	public int aptitude = 0;

	public GuildApplyData() {

	}

	// 格式={ id:1,名字:2,等级:3,职业:4,战力:5}
	public GuildApplyData(JSONObject jo) {
		this.id = jo.getLongValue(GuildApplyData.idKey);
		this.name = jo.getString(GuildApplyData.nameKey);
		this.level = jo.getIntValue(GuildApplyData.levelKey);
		this.combat = jo.getIntValue(GuildApplyData.combatKey);
		this.time = jo.getLongValue(GuildApplyData.timeKey);
		this.sn = jo.getIntValue(snKey);
		this.aptitude = jo.getIntValue(aptitudeKey);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(idKey, id)
				.append(nameKey, name).append(levelKey, level).append(combatKey, combat)
				.append(timeKey, time).append(snKey, sn).append(aptitudeKey, aptitude).toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(name);
		out.write(level);
		out.write(combat);
		out.write(time);
		out.write(sn);
		out.write(aptitude);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		name = in.read();
		level = in.read();
		combat = in.read();
		time = in.read();
		sn = in.read();
		aptitude = in.read();
	}

	/**
	 * 将List转换为Json
	 * @return
	 */
	public static String listToJson(List<GuildApplyData> ApplyList) {
		JSONArray ja = new JSONArray();

		for (GuildApplyData bean : ApplyList) {
			if (bean == null) {
				continue;
			}
			ja.add(getJSONObject(bean));
		}

		return ja.toJSONString();
	}

	public static JSONObject getJSONObject(GuildApplyData bean) {
		JSONObject jo = new JSONObject();
		jo.put(idKey, bean.id);
		jo.put(nameKey, bean.name);
		jo.put(levelKey, bean.level);
		jo.put(combatKey, bean.combat);
		jo.put(timeKey, bean.time);
		jo.put(snKey, bean.sn);
		jo.put(aptitudeKey, bean.aptitude);
		return jo;
	}

	/*
	 * 组装一条信息成为JSONObject
	 */
	public static JSONObject humanToJSONObject(Human human, long time) {

		JSONObject jo = new JSONObject();
		jo.put(idKey, human.getId()); // 会员ID
		jo.put(nameKey, human.getName()); // 名字
		jo.put(levelKey, human.getLevel()); // 等级
		jo.put(combatKey, human.getCombat()); // 战力
		jo.put(timeKey, time);//申请时间
		jo.put(snKey, human.getSn());//主角sn
		jo.put(aptitudeKey, human.getAptitude());
		return jo;
	}

	/**
	 * 把Json转换为List
	 * @param json
	 * @return
	 */
	public static List<GuildApplyData> jsonToList(String json) {
		List<GuildApplyData> result = new ArrayList<GuildApplyData>();
		if (json == null || json.equals("")) {
			return result;
		}

		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return result;
		}
		for (int i = 0; i < ja.size(); i++) {
			GuildApplyData vo = new GuildApplyData(ja.getJSONObject(i));
			result.add(vo);
		}
		return result;
	}

	/**
	 * 根据humanId 获取信息
	 * @param json
	 * @param humanId
	 * @return
	 */
	public static GuildApplyData get(String json, long humanId) {
		GuildApplyData result = null;
		if (json == null || json.equals("")) {
			return result;
		}
		JSONArray ja = Utils.toJSONArray(json);
		if (ja.isEmpty()) {
			return result;
		}

		for (int i = 0; i < ja.size(); i++) {
			GuildApplyData vo = new GuildApplyData(ja.getJSONObject(i));
			if (vo.id == humanId) {
				return vo;
			}
		}
		return result;
	}

	/**
	 * 删除具体一个玩家申请入会信息
	 * @param json
	 * @param humanId
	 * @return
	 */
	public static String delete(String json, long humanId) {
		JSONArray ja = Utils.toJSONArray(json);
		if (ja == null || ja.isEmpty()) {
			return "";
		}
		for (int i = 0; i < ja.size(); i++) {
			GuildApplyData vo = new GuildApplyData(ja.getJSONObject(i));
			if (vo.id == humanId) {
				ja.remove(ja.getJSONObject(i));
				return ja.toJSONString();
			}
		}
		return "";
	}

	public static String deleteBy(String json, long time) {
		JSONArray ja = Utils.toJSONArray(json);
		if (ja == null || ja.isEmpty()) {
			return "";
		}
		int guildApplyDay = ParamManager.applyLimitTime;
		for (int i = 0; i < ja.size(); i++) {
			GuildApplyData vo = new GuildApplyData(ja.getJSONObject(i));
			int day = Utils.getDaysBetween(vo.time, time);
			if (day >= guildApplyDay) {
				ja.remove(ja.getJSONObject(i));
			}
		}
		return ja.toJSONString();
	}

	/**
	 * 申请人信息
	 * @return
	 */
	public DGuildApplyHumanInfo.Builder createMsg() {
		DGuildApplyHumanInfo.Builder dApplyInfo = DGuildApplyHumanInfo.newBuilder();
		dApplyInfo.setHumanId(id);
		dApplyInfo.setHumanName(name);
		dApplyInfo.setHumanLevel(level);
		dApplyInfo.setHumanCombat(combat);
		dApplyInfo.setHumanSn(sn);
		dApplyInfo.setAptitude(aptitude);
		dApplyInfo.setTimeLogout(time);
		return dApplyInfo;
	}

}
