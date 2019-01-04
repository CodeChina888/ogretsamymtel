package game.worldsrv.guild;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Utils;

public class GuildImmoLog implements ISerilizable {
	private static final String snKey = "sn";				// sn
	private static final String nameKey = "na";			// 名字
	private static final String timeKey = "ti";			// 时间(献祭/挑战/日常)
	private static final String typeKey = "ty";			// 消耗类型
	private static final String dareSnKey = "ds";		// 挑战sn
	private static final String handleKey = "hk";		// 1进入公会，2退出公会，3任命会长，4任命副会长，5踢出公会
	private static final String logTypeKey = "lt";		// 1献祭，2挑战，3日常
	private static final String humanIdKey = "id";		// humanId玩家ID
	private static final String aptitudeKey = "ap";		// 资质
	
	public int sn = 0; 		// sn
	public String name = ""; 	// 名字
	public long time = 0;	 	// 献祭时间
	public int type = 0;		// 消耗类型
	public int dareSn = 0;		// 挑战sn
	public int handle = 0;		// 1进入公会，2退出公会，3任命会长，4任命副会长
	public int logType = 0; 	// 1献祭，2挑战，3日常
	public long humanId = 0;	// 玩家id
	public int aptitude = 0;	// 资质
	
	public GuildImmoLog() {
	}

	// 格式={ sn:1,名字:2,时间:3,类型:4}
	public GuildImmoLog(JSONObject jo) {
		this.sn = jo.getIntValue(GuildImmoLog.snKey);
		this.name = jo.getString(GuildImmoLog.nameKey);
		this.time = jo.getLongValue(GuildImmoLog.timeKey);
		this.type = jo.getIntValue(GuildImmoLog.typeKey);
		this.dareSn = jo.getIntValue(GuildImmoLog.dareSnKey);
		this.handle = jo.getIntValue(GuildImmoLog.handleKey);
		this.logType = jo.getIntValue(GuildImmoLog.logTypeKey);
		this.humanId = jo.getLongValue(GuildImmoLog.humanIdKey);
		this.aptitude = jo.getIntValue(GuildImmoLog.aptitudeKey);
	}
	
	/**
	 * 公会日志
	 * @param name 名字
	 * @param time 时间
	 * @param logType 1献祭，2挑战，3日常
	 * @param value 对应类型的参数
	 */
	public GuildImmoLog(String name, long time, int logType, int value, long humanId, int aptitude) {
		this.name = name;
		this.time = time;
		this.logType = logType;
		this.aptitude = aptitude;
		this.humanId = humanId;
		// 1献祭，2挑战，3日常
		switch (logType) {
			case 1:// 公会建设日志（献祭）
				this.type = value;
				break;
			case 2:// 挑战日志
				this.dareSn = value;
				break;
			case 3:// 日常操作日志
				this.handle = value;
				break;
		default:
			break;
		}
	}


	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(GuildImmoLog.snKey, sn)
				.append(nameKey, name).append(timeKey, time).append(typeKey, type).append(dareSnKey, dareSn)
				.append(handleKey, handle).append(logTypeKey, logType).append(humanIdKey, humanId)
				.append(aptitudeKey, aptitude).toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(name);
		out.write(time);
		out.write(type);
		out.write(dareSn);
		out.write(handle);
		out.write(logType);
		out.write(humanId);
		out.write(aptitude);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		name = in.read();
		time = in.read();
		type = in.read();
		dareSn = in.read();
		handle = in.read();
		logType = in.read();
		humanId = in.read();
		aptitude = in.read();
	}
	
	/**
	 * 公会献祭记录String转List<GuildImmoLog>
	 * @param json
	 * @return
	 */
	public static List<GuildImmoLog> getAll(String json){
		List<GuildImmoLog> listDGuildImmo = new ArrayList<>();
		JSONArray ja = Utils.toJSONArray(json);
		if(null == ja || ja.isEmpty()){
			return listDGuildImmo;
		}
		for (int i = 0; i < ja.size(); i++) {
			GuildImmoLog bean = new GuildImmoLog(ja.getJSONObject(i));
			listDGuildImmo.add(bean);
		}
		return listDGuildImmo;
	}

	/**
	 * 
	 * @param guildHumanJSON
	 * @param bean
	 * @return
	 */
	public static String add(String guildHumanJSON, GuildImmoLog bean){
		String ret = guildHumanJSON;
		JSONArray ja = Utils.toJSONArray(guildHumanJSON);
		if(ja.isEmpty()){
			return ret;
		}
		int size = ja.size();
		for (int i = 0; i < size; i++) {
			JSONObject jo = ja.getJSONObject(i);
			
			if(jo.getLongValue(GuildImmoLog.snKey) == bean.sn){
				jo.replace(nameKey, bean.name);
				jo.replace(timeKey, bean.time);
				jo.replace(typeKey, bean.type);
				jo.replace(dareSnKey, bean.dareSn);
				jo.replace(handleKey, bean.handle);
				jo.replace(logTypeKey, bean.logType);
				jo.replace(humanIdKey, bean.humanId);
				jo.replace(aptitudeKey, bean.aptitude);
				return ja.toJSONString();//记住找到后一定要跳出循环
			}
		}
		return ret;
	}
	
	/**
	 * List转换为JSON
	 * @return
	 */
	public static String listToJSON(List<GuildImmoLog> list) {
		int removeNum = 0;//要删除的个数
		int size = list.size();//记录个数
		if(size > ParamManager.guildImmoLogMax){//如果记录的数据条数大于最大条数
			removeNum = size - ParamManager.guildImmoLogMax;
		}
		JSONArray ja = new JSONArray();
		Iterator<GuildImmoLog> it = list.iterator();
		int i = 0;//记录删除了几条
		while (it.hasNext()) {
			GuildImmoLog log  = it.next();
			if(removeNum > 0 && i < removeNum){//拥有要删除个数且未删除干净
				it.remove();
				i++;
				continue;
			}
			if(null != log){
				JSONObject jo = new JSONObject();
				jo.put(snKey, log.sn);
				jo.put(nameKey, log.name);
				jo.put(timeKey, log.time);
				jo.put(typeKey, log.type);
				jo.put(dareSnKey, log.dareSn);
				jo.put(handleKey, log.handle);
				jo.put(logTypeKey, log.logType);
				jo.put(humanIdKey, log.humanId);
				jo.put(aptitudeKey, log.aptitude);
				ja.add(jo);
			}
		}
		return ja.toJSONString();
	}
}
