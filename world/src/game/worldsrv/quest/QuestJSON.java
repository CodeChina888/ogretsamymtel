package game.worldsrv.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import game.msg.Define.DQuestDaily;
import game.msg.Define.EQuestDailyStatus;
import game.worldsrv.config.ConfQuestDaily;
import game.worldsrv.support.Utils;

/**
 * 任务JSON数据的处理类
 */
public class QuestJSON {//implements ISerilizable {
	private static final String snKey = "sn";// 该key为任务sn
	private static final String npKey = "np";// 该key为任务进度
	private static final String stKey = "st";// 该key为任务状态
	
	public int sn;
	public int nowProgress; // 当前进度
	public int status; // 状态EQuestDailyStatus：0是进行中，1是完成，2是已领奖，3是条件不足（级别限制，时间未到等等）
	
	public QuestJSON() {
	}

	public QuestJSON(int sn, int nowProgress, int status) {
		this.sn = sn;
		this.nowProgress = nowProgress;
		this.status = status;
	}

	public QuestJSON(JSONObject jo) {
		this.sn = jo.getIntValue(snKey);
		this.nowProgress = jo.getIntValue(npKey);
		this.status = jo.getIntValue(stKey);
	}
	
//	@Override
//	public void writeTo(OutputStream out) throws IOException {
//		out.write(sn);
//		out.write(nowProgress);
//		out.write(status);
//	}
//
//	@Override
//	public void readFrom(InputStream in) throws IOException {
//		sn = in.read();
//		nowProgress = in.read();
//		status = in.read();
//	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(snKey, sn)
				.append(npKey, nowProgress).append(stKey, status)
				.toString();
	}
	
	/**
	 * 增加任务数据
	 */
	public static String add(String questJSON, QuestJSON quest) {
		String ret = questJSON;
		JSONArray ja = Utils.toJSONArray(questJSON);
		JSONObject jo = new JSONObject();
		jo.put(QuestJSON.snKey, quest.sn);
		jo.put(QuestJSON.npKey, quest.nowProgress);
		jo.put(QuestJSON.stKey, quest.status);
		ja.add(jo);
		ret = ja.toJSONString();
		return ret;
	}
	
	/**
	 * 删除任务数据
	 */
	public static String delete(String questJSON, int snQuest) {
		String ret = questJSON;
		JSONArray ja = Utils.toJSONArray(questJSON);
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if (jo.getIntValue(QuestJSON.snKey) == snQuest) {
				ja.remove(jo);
				ret = ja.toJSONString();
				break;
			}
		}
		return ret;
	}
	
	/**
	 * 修改任务数据
	 */
	public static String modify(String questJSON, QuestJSON quest) {
		String ret = questJSON;
		JSONArray ja = Utils.toJSONArray(questJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if (jo.getIntValue(QuestJSON.snKey) == quest.sn) {
				jo.replace(QuestJSON.npKey, quest.nowProgress);
				jo.replace(QuestJSON.stKey, quest.status);
				ret = ja.toJSONString();
				break;
			}
		}
		return ret;
	}
	/**
	 * 修改任务进度
	 */
	public static String modify(String questJSON, int snQuest, int nowProgress,int statusType) {
		String ret = questJSON;
		JSONArray ja = Utils.toJSONArray(questJSON);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if (jo.getIntValue(QuestJSON.snKey) == snQuest) {
				jo.replace(QuestJSON.npKey, nowProgress);
				jo.replace(QuestJSON.stKey, statusType);
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
	public static String mapToJSON(Map<Integer, QuestJSON> map) {
		JSONArray ja = new JSONArray();
		for (QuestJSON questJSON : map.values()) {
			JSONObject jo = new JSONObject();
			jo.put(snKey, questJSON.sn);
			jo.put(npKey, questJSON.nowProgress);
			jo.put(stKey, questJSON.status);
			ja.add(jo);
		}
		return ja.toJSONString();
	}
		
	/**
	 * 将StringJson 转为Map key 任务类型（QuestShowCatg中的sn）
	 * @param json
	 * @return 
	 */
//	public static Map<Integer,QuestJSON> mapTypeToList(String json){
//		Map<Integer,QuestJSON> maps = new HashMap<Integer, QuestJSON>();
//		List<QuestJSON> result = jsonToList(json);
//		for (QuestJSON quest : result) {
//			ConfQuest conf = ConfQuest.get(quest.sn);
//			if (conf == null) {
//				continue;
//			}
//			if (!maps.containsKey(conf.showType)) {
//				maps.put(conf.showType, quest);	
//			}
//		}
//		return maps;
//	}
	
	/**
	 * 把Json转换为List
	 * @param json
	 * @return
	 */
	public static List<QuestJSON> jsonToList(String json) {
		List<QuestJSON> result = new ArrayList<QuestJSON>();
		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return result;
		}
		for (int i = 0; i < ja.size(); i++) {
			QuestJSON vo = new QuestJSON(ja.getJSONObject(i));
			result.add(vo);
		}
		return result;
	}
	
	/**
	 * 将List转换为Json
	 * @param questDailyList
	 * @return
	 */
	public static String listToJson(List<QuestJSON> questDailyList) {
		return listToJson(questDailyList, true);
	}

	/**
	 * 将List转换为Json
	 * @param questDailyList
	 * @param saveReward 是否保存领奖过的任务，默认true保存，false不保存
	 * @return
	 */
	public static String listToJson(List<QuestJSON> questDailyList, boolean saveReward) {
		JSONArray ja = new JSONArray();
		for (QuestJSON vo : questDailyList) {
			if (!saveReward) {// 不保存领奖过的任务
				if (vo.status == EQuestDailyStatus.Rewarded_VALUE)
					continue;
			}
			JSONObject jo = new JSONObject();
			jo.put(snKey, vo.sn);
			jo.put(npKey, vo.nowProgress);
			jo.put(stKey, vo.status);
			ja.add(jo);
		}
		return ja.toJSONString();
	}
	
	/**
	 * 创建DQuestDaily
	 */
	public DQuestDaily createDQuestDaily() {
		DQuestDaily.Builder msg = DQuestDaily.newBuilder();
		msg.setSn(sn);
		@SuppressWarnings("unused")
		int targetProgress = 0;
		ConfQuestDaily conf = ConfQuestDaily.get(sn);
		if (conf != null) {
			targetProgress = conf.param;
		}
		msg.setNowProgress(nowProgress);
		msg.setEQuestDailyStatus(EQuestDailyStatus.valueOf(status));
		return msg.build();
	}

}
