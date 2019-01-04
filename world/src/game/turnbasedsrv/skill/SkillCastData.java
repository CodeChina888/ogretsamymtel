package game.turnbasedsrv.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.msg.Define.DTurnbasedSkillTarget;
import game.turnbasedsrv.buff.Buff;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.Prop;

/**
 * 技能执行时总数据
 * 
 * @author landy
 *
 */
public class SkillCastData {
	public Prop prop = new Prop();
	/** 技能 **/
	public Skill skill;
	/** 施法者 **/
	public FightObject creator;
	/** 产生的buff列表 <技能分段,列表> **/
	public Map<Integer, List<Buff>> buffMap = new HashMap<>();
	/** 目标列表 <技能分段,列表> **/
	public Map<Integer, List<FightObject>> targetMap = new HashMap<>();
	/** 目标数据列表<技能分段,列表> **/
	public Map<Integer, List<SkillTargetData>> targetDataMap = new HashMap<>();
	/** 施法者消耗数据<技能分段,列表> **/
	public SkillTargetData casterData;
	/** 技能击杀对象列表 */
	public int killCount = 0;
	/** 技能分段 **/
	public int step = 1;
	/** 总伤害 **/
	public int totalDamage = 0;
	
	/**
	 * 构造函数
	 * 
	 */
	public SkillCastData(Skill skill, FightObject fightObj) {
		this.skill = skill;
		this.creator = fightObj;
	}

	public boolean isTargetDodge(FightObject target) {
		List<SkillTargetData> list = targetDataMap.get(this.step);
		if (list == null) {
			return false;
		}
		for (SkillTargetData data : list) {
			if (data.target == target) {
				return data.isDodge();
			}
		}
		return false;
	}

	/**
	 * 技能产生的所有buff
	 * 
	 * @return
	 */
	public List<Buff> getAllBuff() {
		List<Buff> list = new ArrayList<>();
		for (List<Buff> buffList : buffMap.values()) {
			list.addAll(buffList);
		}
		return list;
	}

	/**
	 * 技能影响的所有目标
	 * 
	 * @return
	 */
	public List<FightObject> getAllTarget() {
		List<FightObject> list = new ArrayList<>();
		for (List<FightObject> targetList : targetMap.values()) {
			list.addAll(targetList);
		}
		return list;
	}

	/**
	 * 技能所有的目标数据
	 * 
	 * @return
	 */
	public List<SkillTargetData> getAllTargetData() {
		List<SkillTargetData> list = new ArrayList<>();
		for (List<SkillTargetData> targetDataList : targetDataMap.values()) {
			list.addAll(targetDataList);
		}
		return list;
	}

	/**
	 * 获取某技能分段buff列表
	 * 
	 * @param step
	 * @return
	 */
	public List<Buff> getBuffList(int step) {
		return buffMap.get(step);
	}

	/**
	 * 获取某技能分段目标列表
	 * 
	 * @param step
	 * @return
	 */
	public List<FightObject> getTargetList(int step) {
		return targetMap.get(step);
	}

	/**
	 * 获取某技能分段目标数据
	 * 
	 * @param step
	 * @return
	 */
	public List<SkillTargetData> getTargetDataList(int step) {
		return targetDataMap.get(step);
	}

	/**
	 * 获取当前技能分段buff列表
	 * 
	 * @return
	 */
	public List<Buff> getBuffList() {
		return buffMap.get(this.step);
	}

	/**
	 * 获取当前技能分段目标列表
	 * 
	 * @return
	 */
	public List<FightObject> getTargetList() {
		return targetMap.get(this.step);
	}

	/**
	 * 获取当前技能分段目标数据列表
	 * 
	 * @return
	 */
	public List<SkillTargetData> getTargetDataList() {
		return targetDataMap.get(this.step);
	}
	
	/**
	 * 获取技能击杀的人数
	 * @return
	 */
	public int getKillCount() {
		return killCount;
	}

	/**
	 * 记录新增buff
	 * 
	 * @param buff
	 */
	public void addBuff(Buff buff) {
		List<Buff> list = buffMap.get(this.step);
		if (list == null) {
			list = new ArrayList<>();
			buffMap.put(this.step, list);
		}
		list.add(buff);
	}

	/**
	 * 记录新增加buff列表
	 * 
	 * @param buffList
	 */
	public void addBuffList(List<Buff> buffList) {
		List<Buff> list = buffMap.get(this.step);
		if (list == null) {
			list = new ArrayList<>();
			buffMap.put(this.step, list);
		}
		list.addAll(buffList);
	}

	/**
	 * 增加施法者消耗数据
	 */
	public void addCasterData(SkillTargetData casterData) {
		this.casterData = casterData;
	}
	
	/**
	 * 增加目标数据,同目标的数据合并
	 * 
	 * @param targetData
	 */
	public void addTargetData(SkillTargetData targetData) {
		if (targetData.valueMap != null) {
			Integer damage = targetData.valueMap.get(SkillTargetData.ValueDamage);
			if (damage != null && damage.intValue() < 0) {
				this.totalDamage = this.totalDamage + damage.intValue();
			}
		}
		addTarget(targetData);
		List<SkillTargetData> list = targetDataMap.get(this.step);
		if (list == null) {
			list = new ArrayList<>();
			targetDataMap.put(this.step, list);
			list.add(targetData.getCopy());
			return;
		}
		for (SkillTargetData data : list) {
			if (data.isSameTarget(targetData)) {
				data.dataAdd(targetData);
				return;
			}
		}
		list.add(targetData.getCopy());
	}

	/**
	 * 增加目标
	 * 
	 * @param targetData
	 */
	public void addTarget(SkillTargetData targetData) {
		if (targetData.getRage() > 0) {
			return;
		}
		List<FightObject> targetList = targetMap.get(this.step);
		if (targetList == null) {
			targetList = new ArrayList<>();
			targetMap.put(this.step, targetList);
		}
		if (!targetList.contains(targetData.target)) {
			targetList.add(targetData.target);
		}
	}

	/**
	 * 增加目标数据,同目标的数据合并
	 * 
	 * @param targetData
	 */
	public void addTargetDataExtra(SkillTargetData targetData) {
		if (targetData.valueMap != null) {
			Integer damage = targetData.valueMap.get(SkillTargetData.ValueDamage);
			if (damage != null && damage.intValue() < 0) {
				this.totalDamage = this.totalDamage + damage.intValue();
			}
		}
		addTarget(targetData);
		List<SkillTargetData> list = targetDataMap.get(this.step);
		if (list == null) {
			list = new ArrayList<>();
			targetDataMap.put(this.step, list);
			list.add(targetData.getCopy());
			return;
		}
		list.add(targetData.getCopy());
	}

	/**
	 * 增加目标数据,同目标的数据合并
	 * 
	 * @param targetDataList
	 */
	public void addTagetData(List<SkillTargetData> targetDataList) {
		List<SkillTargetData> list = targetDataMap.get(this.step);
		if (list == null) {
			list = new ArrayList<>();
			targetDataMap.put(this.step, list);
		}
		for (SkillTargetData targetData : targetDataList) {
			if (targetData.valueMap != null) {
				Integer damage = targetData.valueMap.get(SkillTargetData.ValueDamage);
				if (damage != null && damage.intValue() < 0) {
					this.totalDamage = this.totalDamage + damage.intValue();
				}
			}
			addTarget(targetData);
			boolean isAdd = false;
			for (SkillTargetData data : list) {
				if (data.isSameTarget(targetData)) {
					data.dataAdd(targetData);
					isAdd = true;
					break;
				}
			}
			if (!isAdd) {
				list.add(targetData.getCopy());
			}
		}
	}

	/**
	 * 目标数据生成消息
	 * 
	 * @return
	 */
	public List<DTurnbasedSkillTarget> createTargetDataMsg() {
		List<DTurnbasedSkillTarget> dList = new ArrayList<>();
		for (int i = 1; i <= step; i++) {
			List<SkillTargetData> list = targetDataMap.get(i);
			if (list == null) {
				continue;
			}
			for (SkillTargetData data : list) {
				dList.add(data.createTargetDataMsg());
			}
		}
		return dList;
	}
	
	/**
	 * 击杀数量增加
	 */
	public void addKillCount() {
		killCount ++;
	}

}
