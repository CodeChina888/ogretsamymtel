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
import core.Port;
import core.interfaces.ISerilizable;
import game.msg.Define.DGuildMemberInfo;
import game.msg.Define.EGuildImmoType;
import game.worldsrv.entity.Human;
import game.worldsrv.support.Utils;
/**
 * 工会信息
 * @author songy
 *
 */
public class GuildData implements ISerilizable {
	
	private static final String idKey = "id";				// 会员ID
	private static final String nameKey = "na";			// 会员名字
	private static final String levelKey = "lv";			// 当前等级
	private static final String contributeKey = "cb";	// 帮会贡献
	private static final String timeLogoutKey = "tl";	// 离线时间
	private static final String combatKey = "co";		// 战斗力
	private static final String onlineStateKey = "os";	// 用户在线状态 1 在线， 0 离线
	private static final String postKey = "po";			// 职位 0
	private static final String snKey = "sn";				// sn
	private static final String typeKey = "ty";			// type
	private static final String aptitudeKey = "ap";			// 资质

	public long id = 0; // 会员ID
	public String name = ""; // 会员名字
	public int level = 1; // 当前等级
	public long contribute = 0; // 帮会贡献
	public long timeLogout = 0; // 离线时间
	public int combat = 0; // 战斗力
	public int onlineState = 0; // 用户在线状态 1 在线， 0 离线
	public int post = 0; // 职位  0 会员，1 会长， 2 副会长
	public int sn = 0;	//主角sn
	public int type = 0;//建设类型，0未建设，1初级建设，2中级建设，3高级建设
	public int aptitude = 0;//资质

	public GuildData() {
	}

	// 格式={ id:1,名字:2,等级:3,职业:4,帮会贡献:5,离线时间:6,战力:7,状态:8,职位:9}
	public GuildData(JSONObject jo) {
		this.id = jo.getLongValue(GuildData.idKey);
		this.name = jo.getString(GuildData.nameKey);
		this.level = jo.getIntValue(GuildData.levelKey);
		this.contribute = jo.getLongValue(GuildData.contributeKey);
		this.timeLogout = jo.getLongValue(GuildData.timeLogoutKey);
		this.combat = jo.getIntValue(GuildData.combatKey);
		this.onlineState = jo.getIntValue(GuildData.onlineStateKey);
		this.post = jo.getIntValue(GuildData.postKey);
		this.sn = jo.getIntValue(GuildData.snKey);
		this.type = jo.getIntValue(GuildData.typeKey);
		this.aptitude = jo.getIntValue(aptitudeKey);
	}
	
	public GuildData(Human human, int onlineStatus, int post, long timeLogout) {
		this.id = human.getId();// 会员ID
		this.name = human.getName();// 名字
		this.level = human.getLevel();// 等级
		this.contribute = human.getContribute();// 帮会贡献
		if (onlineStatus == 0) {// 下线
			this.timeLogout = timeLogout;// 离线时间
		} else {
			this.timeLogout = human.getTimeLogout(); // 离线时间
		}
		this.combat = human.getSumCombat();// 战力
		this.onlineState = onlineStatus;// 用户在线状态 1 在线 0 离线
		this.post = post; // 职位 0 普通会员， 1 会长，2 副会长
		this.sn = human.getSn();// 主角sn
		this.type = 0;
		this.aptitude = human.getAptitude();
	}

	/**
	 * 注意，这只是同意申请时把申请信息改成公会成员临时信息，玩家登陆的时候会更新
	 * @param bean
	 */
	public GuildData(GuildApplyData bean) {
		this.id = bean.id;// 会员ID
		this.name = bean.name;// 名字
		this.level = bean.level;// 等级
		this.contribute = 0;// 帮会贡献
		this.timeLogout = bean.time;// 离线时间
		this.combat = bean.combat;// 战力
		this.onlineState = 0;// 用户在线状态 1 在线 0 离线
		this.post = 0; // 职位 0 普通会员， 1 会长，2 副会长
		this.sn = bean.sn;// 主角sn
		this.type = 0;
		this.aptitude = bean.aptitude;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(idKey, id).append(nameKey, name)
				.append(levelKey, level).append(contributeKey, contribute).append(timeLogoutKey, timeLogout).append(combatKey, combat)
				.append(onlineStateKey, onlineState).append(postKey, post).append(snKey, sn).append(typeKey, type)
				.append(aptitudeKey, aptitude).toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(name);
		out.write(level);
		out.write(contribute);
		out.write(timeLogout);
		out.write(combat);
		out.write(onlineState);
		out.write(post);
		out.write(sn);
		out.write(type);		
		out.write(aptitude);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		name = in.read();
		level = in.read();
		contribute = in.read();
		timeLogout = in.read();
		combat = in.read();
		onlineState = in.read();
		post = in.read();
		sn = in.read();
		type = in.read();
		aptitude = in.read();
	}

	/**
	 * 将List转换为Json
	 * @return
	 */
	public static String listToJson(List<GuildData> GuildList) {
		JSONArray ja = new JSONArray();

		for (GuildData bean : GuildList) {
			if (bean == null) {
				continue;
			}
			ja.add(getJSONObject(bean));
		}

		return ja.toJSONString();
	}

	/**
	 * 根据id获取此玩家的公会信息
	 * @param json
	 * @param humanId
	 * @return
	 */
	public static GuildData get(String json, long humanId) {
		GuildData result = null;
		if (json == null || json.equals("")) {
			return result;
		}
		JSONArray ja = Utils.toJSONArray(json);
		if (ja.isEmpty()) {
			return result;
		}
		
		for (int i = 0; i < ja.size(); i++) {
			GuildData vo = new GuildData(ja.getJSONObject(i));
			if (vo.id == humanId) {
				return vo;
			}
		}
		return result;
	}

	/**
	 * 删除会员
	 * @param json
	 * @param humanId
	 * @return
	 */
	public static String delete(String json, long humanId) {
		if (json == null || json.equals("")) {
			return "";
		}
		JSONArray ja = Utils.toJSONArray(json);
		if (ja.isEmpty()) {
			return "";
		}

		for (int i = 0; i < ja.size(); i++) {
			GuildData vo = new GuildData(ja.getJSONObject(i));
			if (vo.id == humanId) {
				ja.remove(ja.getJSONObject(i));
				return ja.toJSONString();
			}
		}
		return "";
	}

	/**
	 * guildData转换为JSONObject
	 */
	public static JSONObject getJSONObject(GuildData bean) {
		JSONObject jo = new JSONObject();
		if (bean != null) {
			jo.put(idKey, bean.id);
			jo.put(nameKey, bean.name);
			jo.put(levelKey, bean.level);
			jo.put(contributeKey, bean.contribute);
			jo.put(timeLogoutKey, bean.timeLogout);
			jo.put(combatKey, bean.combat);
			jo.put(onlineStateKey, bean.onlineState);
			jo.put(postKey, bean.post);
			jo.put(snKey, bean.sn);
			jo.put(typeKey, bean.type);			
			jo.put(aptitudeKey, bean.aptitude);
		}
		return jo;
	}
	
	/**
	 * 修改会员
	 * @return
	 */
	public static String modify(String guildHumanJSON, GuildData guildData){
		String ret = guildHumanJSON;
		JSONArray ja = Utils.toJSONArray(guildHumanJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if(jo.getLongValue(idKey) == guildData.id){
				jo.replace(nameKey, guildData.name);
				jo.replace(levelKey, guildData.level);
				jo.replace(contributeKey, guildData.contribute);
				jo.replace(timeLogoutKey, guildData.timeLogout);
				jo.replace(combatKey, guildData.combat);
				jo.replace(onlineStateKey, guildData.onlineState);
				jo.replace(postKey, guildData.post);
				jo.replace(snKey, guildData.sn);
				jo.replace(typeKey, guildData.type);				
				jo.replace(aptitudeKey, guildData.aptitude);
				return ja.toJSONString();//记住找到后一定要跳出循环
			}
		}
		return ret;
	}
	
	/**
	 * 修改会员
	 * @return
	 */
	public static String modifyType(String guildHumanJSON){
		String ret = guildHumanJSON;
		JSONArray ja = Utils.toJSONArray(guildHumanJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			jo.replace(GuildData.typeKey, 0);
		}
		return ja.toJSONString();
	}
	
	/**
	 * 修改会员信息
	 * @return
	 */
	public static String modify(String guildHumanJSON, Human human, int onlineStatus, long time){
		String ret = guildHumanJSON;
		JSONArray ja = Utils.toJSONArray(guildHumanJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if(jo.getLongValue(GuildData.idKey) == human.getId()){
				jo.replace(nameKey, human.getName());
				jo.replace(levelKey, human.getLevel());
				jo.replace(contributeKey, human.getContribute());
				if (onlineStatus == 0) {// 下线
					jo.replace(timeLogoutKey, time);// 离线时间
				} else {
					jo.replace(timeLogoutKey, human.getTimeLogout());// 离线时间
				}
				jo.replace(combatKey, human.getSumCombat());
				jo.replace(onlineStateKey, onlineStatus);
				jo.replace(snKey, human.getSn());
				jo.replace(aptitudeKey, human.getAptitude());
//				jo.replace(GuildData.typeKey, type);			
				
				jo.replace(aptitudeKey, human.getAptitude());
				return ja.toJSONString();//记住找到后一定要跳出循环
			}
		}
		return ret;
	}
	
	/**
	 * 计算军团的总战斗力
	 * @param guildHumanJSON
	 * @return
	 */
	public static long countGuildCombat(String guildHumanJSON){
		long combat = 0;
		JSONArray ja = Utils.toJSONArray(guildHumanJSON);
		if(ja.isEmpty()){
			return combat;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			combat += jo.getLongValue(GuildData.combatKey);
		}
		return combat;
	}

	/**
	 * 把Json转换为List
	 * @param json
	 * @return
	 */
	public static List<GuildData> jsonToList(String json) {
		List<GuildData> result = new ArrayList<GuildData>();
		if (json == null || json.equals("")) {
			return result;
		}

		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return result;
		}
		for (int i = 0; i < ja.size(); i++) {
			GuildData vo = new GuildData(ja.getJSONObject(i));
			result.add(vo);
		}
		return result;
	}
	
	/**
	 * 获取公会总人数
	 * @param json
	 * @return
	 */
	public static int getGuildHumanNum(String json) {
		int size = 0;
		if (json == null || json.equals("")) {
			return size;
		}
		JSONArray ja = Utils.toJSONArray(json);
		return ja.size();
	}
	
	/**
	 * 启动服务器时重置所有公会玩家为在线状态
	 * 处理因为关服等等玩家不是正常退出游戏时没有处理公会在线状态
	 * @param json
	 * @return
	 */
	public static String initGuildHumanOnlineStatus(String json) {
		if (json == null || json.equals("")) {
			return json;
		}
		JSONArray ja = Utils.toJSONArray(json);
		if(null == ja || ja.isEmpty()){
			return json;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if(1 == jo.getIntValue(GuildData.onlineStateKey)){
				jo.replace(onlineStateKey, 0);// 设置为离线状态
				jo.replace(timeLogoutKey, Port.getTime());// 离线时间
			}
		}
		return ja.toJSONString();
		
	}
	
	/**
	 * 公会会员信息
	 * @return
	 */
	public DGuildMemberInfo.Builder createMsg() {
		DGuildMemberInfo.Builder dmsg = DGuildMemberInfo.newBuilder();
		dmsg.setMemberId(id);
		dmsg.setMemberName(name);
		dmsg.setMemberLevel(level);
		dmsg.setMemberSn(sn);
		dmsg.setMemberContribute(contribute);
		dmsg.setMemberTimeLogout(timeLogout);
		dmsg.setMemberCombat(combat);
		dmsg.setOnlineStatus(onlineState);
		dmsg.setPost(post);
		dmsg.setType(EGuildImmoType.valueOf(type));
		dmsg.setAptitude(aptitude);
		return dmsg;
	}

	
}
