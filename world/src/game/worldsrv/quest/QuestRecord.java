package game.worldsrv.quest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.EQuestDailyStatus;
import game.worldsrv.config.ConfQuestDaily;
import game.worldsrv.entity.Quest;
import game.worldsrv.enumType.QuestDailyType;
import game.worldsrv.support.Utils;

/**
 * 玩家任务记录的增删改查等操作处理：包括普通任务，每日任务，开服七天任务
 */
public class QuestRecord implements ISerilizable {
	// 保存数据库记录
	private Quest quest = new Quest();
	// 保存所有普通任务Map<任务sn, QuestJSON>
	private Map<Integer, QuestJSON> normalMap = new HashMap<>();
	// 保存所有每日任务Map<任务sn, QuestJSON>
	private Map<Integer, QuestJSON> dailyMap = new HashMap<>();
	
	public QuestRecord() {
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(quest);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		quest = in.read();
		parse(quest);// 解析数据库记录到内存数据
	}
	
	@Override
	public String toString() {
		String ret = "";
		ret = quest.toString();
		return ret;
	}
	
	public void init(Quest quest) {
		this.quest = quest;// 保存数据库记录
		parse(this.quest);// 解析数据库记录到内存数据
	}
	
	/**
	 * 解析数据库记录到内存数据
	 */
	private void parse(Quest quest) {
		// 解析普通任务数据到Map
		normalMap.clear();
		normalMap = jsonToMap(quest.getNormalJSON());
		// 解析每日任务数据到Map
		dailyMap.clear();
		dailyMap = jsonToMap(quest.getDailyJSON());
	}
	
	/**
	 * 解析JSON数据到Map
	 */
	private static Map<Integer, QuestJSON> jsonToMap(String json) {
		Map<Integer, QuestJSON> ret = new HashMap<Integer, QuestJSON>();
		JSONArray ja = Utils.toJSONArray(json);
		if(ja.isEmpty()){
			return ret;
		}
		for (int i = 0; i < ja.size(); i++) {
			QuestJSON q = new QuestJSON(ja.getJSONObject(i));
			ret.put(q.sn, q);
		}
		return ret;
	}
	
	//**以下为普通任务的增删改查处理*******************************************************************
	/**
	 * 增加一条普通任务数据
	 */
	public boolean addNormal(QuestJSON questJSON) {
		boolean isChange = false;
		if (!normalMap.containsKey(questJSON.sn)) {
			normalMap.put(questJSON.sn, questJSON);
			isChange = true;
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setNormalJSON(QuestJSON.mapToJSON(normalMap));
		}
		return isChange;
	}
	/**
	 * 增加多条普通任务数据
	 */
	public boolean addNormal(List<QuestJSON> questJSONList) {
		boolean isChange = false;
		for (QuestJSON q : questJSONList) {
			if (!normalMap.containsKey(q.sn)) {
				normalMap.put(q.sn, q);
				isChange = true;
			}
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setNormalJSON(QuestJSON.mapToJSON(normalMap));
		}
		return isChange;
	}
	
	/**
	 * 删除一条普通任务数据
	 */
	public boolean deleteNormal(int snQuest) {
		boolean isChange = false;
		QuestJSON q = normalMap.get(snQuest);
		if (q != null) {
			q = null;
			normalMap.remove(snQuest);
			isChange = true;
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setNormalJSON(QuestJSON.mapToJSON(normalMap));
		}
		return isChange;
	}
	/**
	 * 删除多条普通任务数据
	 */
	public boolean deleteNormal(List<Integer> snQuestList) {
		boolean isChange = false;
		for (Integer snQuest : snQuestList) {
			if (snQuest == null)
				continue;
			QuestJSON q = normalMap.get(snQuest);
			if (q != null) {
				q = null;
				normalMap.remove(snQuest);
				isChange = true;
			}
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setNormalJSON(QuestJSON.mapToJSON(normalMap));
		}
		return isChange;
	}
	
	/**
	 * 修改一条普通任务数据
	 */
	public boolean modifyNormal(QuestJSON questJSON) {
		boolean isChange = false;
		QuestJSON q = normalMap.get(questJSON.sn);
		if (q != null) {
			// 移除已领取状态的数据
			if (questJSON.status == EQuestDailyStatus.Rewarded_VALUE) {
				normalMap.remove(questJSON.sn);
			} else {
				q.nowProgress = questJSON.nowProgress;
				q.status = questJSON.status;
			}
			isChange = true;
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setNormalJSON(QuestJSON.mapToJSON(normalMap));
		}
		return isChange;
	}
	/**
	 * 修改多条普通任务数据
	 */
	public boolean modifyNormal(List<QuestJSON> questJSONList) {
		boolean isChange = false;
		for (QuestJSON questJSON : questJSONList) {
			QuestJSON q = normalMap.get(questJSON.sn);
			if (q != null) {
				// 移除已领取状态的数据
				if (questJSON.status == EQuestDailyStatus.Rewarded_VALUE) {
					normalMap.remove(questJSON.sn);
				} else {
					q.nowProgress = questJSON.nowProgress;
					q.status = questJSON.status;
				}
				isChange = true;
			}
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setNormalJSON(QuestJSON.mapToJSON(normalMap));
		}
		return isChange;
	}
	
	/**
	 * 获取普通任务数据，查无数据则返回null
	 */
	public QuestJSON getNormalBy(int snQuest) {
		return normalMap.get(snQuest);
	}
	/**
	 * 获取所有指定类型的普通任务数据
	 */
//	public List<QuestJSON> getNormalList(QuestType type) {
//		List<QuestJSON> list = new ArrayList<>();
//		for (QuestJSON q : normalMap.values()) {
//			ConfQuest conf = ConfQuest.get(q.sn);
//			if (conf != null && conf.type == type.value()) {
//				list.add(q);
//			}
//		}
//		return list;
//	}
	
	/**
	 * 获取所有的普通任务数据
	 */
	public List<QuestJSON> getNormalList() {
		List<QuestJSON> list = new ArrayList<>();
		list.addAll(normalMap.values());
		return list;
	}
	
	/**
	 * 是否已拥有指定的普通任务
	 */
	public boolean containsNormal(int snQuest) {
		return normalMap.containsKey(snQuest);
	}
	
	/**
	 * 清空普通任务数据
	 */
	public void clearNormal() {
		if (!normalMap.isEmpty()) {
			normalMap.clear();
			// 数据改变则保存到数据库
			quest.setNormalJSON(QuestJSON.mapToJSON(normalMap));
		}
	}
	
	//**以下为每日任务的增删改查处理*******************************************************************
	/**
	 * 增加一条每日任务数据
	 */
	public boolean addDaily(QuestJSON questJSON) {
		boolean isChange = false;
		if (!dailyMap.containsKey(questJSON.sn)) {
			dailyMap.put(questJSON.sn, questJSON);
			isChange = true;
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setDailyJSON(QuestJSON.mapToJSON(dailyMap));
		}
		return isChange;
	}
	/**
	 * 增加多条每日任务数据
	 */
	public boolean addDaily(List<QuestJSON> questJSONList) {
		boolean isChange = false;
		for (QuestJSON q : questJSONList) {
			if (!dailyMap.containsKey(q.sn)) {
				dailyMap.put(q.sn, q);
				isChange = true;
			}
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setDailyJSON(QuestJSON.mapToJSON(dailyMap));
		}
		return isChange;
	}
	
	/**
	 * 删除一条每日任务数据
	 */
	public boolean deleteDaily(int snQuest) {
		boolean isChange = false;
		QuestJSON q = dailyMap.get(snQuest);
		if (q != null) {
			q = null;
			dailyMap.remove(snQuest);
			isChange = true;
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setDailyJSON(QuestJSON.mapToJSON(dailyMap));
		}
		return isChange;
	}
	/**
	 * 删除多条每日任务数据
	 */
	public boolean deleteDaily(List<Integer> snQuestList) {
		boolean isChange = false;
		for (Integer snQuest : snQuestList) {
			if (snQuest == null)
				continue;
			QuestJSON q = dailyMap.get(snQuest);
			if (q != null) {
				q = null;
				dailyMap.remove(snQuest);
				isChange = true;
			}
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setDailyJSON(QuestJSON.mapToJSON(dailyMap));
		}
		return isChange;
	}
	
	/**
	 * 修改一条每日任务数据
	 */
	public boolean modifyDaily(QuestJSON questJSON) {
		boolean isChange = false;
		QuestJSON q = dailyMap.get(questJSON.sn);
		if (q != null) {
			// 移除已领取状态的数据
			if (questJSON.status != EQuestDailyStatus.Rewarded_VALUE) {
				q.nowProgress = questJSON.nowProgress;
				q.status = questJSON.status;
			}
			isChange = true;
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setDailyJSON(QuestJSON.mapToJSON(dailyMap));
		}
		return isChange;
	}
	/**
	 * 修改多条每日任务数据
	 */
	public boolean modifyDaily(List<QuestJSON> questJSONList) {
		boolean isChange = false;
		for (QuestJSON questJSON : questJSONList) {
			QuestJSON q = dailyMap.get(questJSON.sn);
			if (q != null) {
				// 移除已领取状态的数据
				if (questJSON.status == EQuestDailyStatus.Rewarded_VALUE) {
					dailyMap.remove(questJSON.sn);
				} else {
					q.nowProgress = questJSON.nowProgress;
					q.status = questJSON.status;
				}
				isChange = true;
			}
		}
		if (isChange) {// 数据改变则保存到数据库
			quest.setDailyJSON(QuestJSON.mapToJSON(dailyMap));
		}
		return isChange;
	}
	/**
	 * 获取每日任务数据，查无数据则返回null
	 */
	public QuestJSON getDailyBy(int snQuest) {
		return dailyMap.get(snQuest);
	}
	/**
	 * 获取每日任务数据，查无数据则返回null
	 */
	public QuestJSON getDailyBy(QuestDailyType type) {
		QuestJSON questJSON = null;
		for (QuestJSON q : dailyMap.values()) {
			ConfQuestDaily conf = ConfQuestDaily.get(q.sn);
			if (conf != null && conf.type == type.value()) {
				questJSON = q;
				break;// 同类型的每日任务，只会存在一个
			}
		}
		return questJSON;
	}
	
	/**
	 * 获取所有的每日任务数据
	 */
	public List<QuestJSON> getDailyList() {
		List<QuestJSON> list = new ArrayList<>();
		list.addAll(dailyMap.values());
		return list;
	}
	
	/**
	 * 是否已拥有指定的每日任务
	 */
	public boolean containsDaily(int snQuest) {
		return dailyMap.containsKey(snQuest);
	}
	
	/**
	 * 清空每日任务数据
	 */
	public void clearDaily() {
		if (!dailyMap.isEmpty()) {
			dailyMap.clear();
			// 数据改变则保存到数据库
			quest.setDailyJSON(QuestJSON.mapToJSON(dailyMap));
		}
	}
	
}
