package game.turnbasedsrv.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.msg.Define.DTurnbasedBuff;
import game.msg.Define.DTurnbasedSkillTarget;
import game.turnbasedsrv.buff.Buff;
import game.turnbasedsrv.fightObj.FightObject;
import game.turnbasedsrv.prop.PropManager;

/**
 * 技能执行时目标数据
 * 
 * @author landy
 *
 */
public class SkillTargetData {
	/** 暴击 **/
	public static final String StateCrit = "StateCrit";
	/** 格挡 **/
	public static final String StateBlock = "StateBlock";
	/** 闪避 **/
	public static final String StateDodge = "StateDodge";
	/** 免疫 **/
	public static final String StateImmune = "StateImmune";

	/** 伤害值 **/
	public static final String ValueDamage = "ValueDamage";
	/** 怒气值 **/
	public static final String ValueRage = "ValueRage";
	/** 显示怒气值 **/
	public static final String ValueShowRage = "ValueShowRage";
	/** 吸收值 **/
	public static final String ValueShield = "ValueShield";

	/** 目标 **/
	public FightObject target;
	/** buff列表 **/
	public List<Buff> buffList;
	/** 状态列表 **/
	List<String> stateList;
	/** 数值列表 **/
	Map<String, Integer> valueMap;

	public SkillTargetData() {

	}

	public SkillTargetData(FightObject target, String key, int value) {
		this.target = target;
		this.valueMap = new HashMap<>();
		this.valueMap.put(key, value);
	}
	
	public SkillTargetData(FightObject target, Map<String, Integer> valueMap, List<Buff> buffList,
			List<String> stateList) {
		this.target = target;
		this.valueMap = valueMap;
		this.buffList = buffList;
		this.stateList = stateList;
	}

	/**
	 * 是否是同一个目标
	 * 
	 * @param data
	 * @return
	 */
	public boolean isSameTarget(SkillTargetData data) {
		if (this == data) {
			return true;
		}
		if (this.target == data.target) {
			return true;
		}
		return false;
	}

	/**
	 * 是否闪避
	 * 
	 * @return
	 */
	public boolean isDodge() {
		if (stateList == null) {
			return false;
		}
		return stateList.contains(StateDodge);
	}

	/**
	 * 是否格挡
	 * 
	 * @return
	 */
	public boolean isBlock() {
		if (stateList == null) {
			return false;
		}
		return stateList.contains(StateBlock);
	}

	/**
	 * 是否暴击
	 * 
	 * @return
	 */
	public boolean isCrit() {
		if (stateList == null) {
			return false;
		}
		return stateList.contains(StateCrit);
	}

	/**
	 * 数据合并
	 * 
	 * @param data
	 */
	public void dataAdd(SkillTargetData data) {
		if (this == data) {
			return;
		}
		if (this.target != data.target) {
			return;
		}
		if (data.valueMap != null) {
			if (this.valueMap == null) {
				this.valueMap = new HashMap<>();
			}
			for (Map.Entry<String, Integer> entry : data.valueMap.entrySet()) {
				String key = entry.getKey();
				int value = entry.getValue();
				if (this.valueMap.get(key) != null) {
					value = value + this.valueMap.get(key);
				}
				this.valueMap.put(key, value);
			}
		}
		if (data.buffList != null) {
			if (buffList == null) {
				buffList = new ArrayList<>();
			}
			for (Buff buff : data.buffList) {
				if (!this.buffList.contains(buff)) {
					this.buffList.add(buff);
				}
			}
		}
		if (data.stateList != null) {
			if (stateList == null) {
				stateList = new ArrayList<>();
			}
			for (String state : data.stateList) {
				if (!this.stateList.contains(state)) {
					this.stateList.add(state);
				}
			}
		}
	}

	/**
	 * 获取一个拷贝数据
	 * 
	 * @return
	 */
	public SkillTargetData getCopy() {
		SkillTargetData data = new SkillTargetData();
		data.target = this.target;
		if (this.valueMap != null) {
			data.valueMap = new HashMap<>();
			for (Map.Entry<String, Integer> entry : this.valueMap.entrySet()) {
				data.valueMap.put(entry.getKey(), entry.getValue());
			}
		}
		if (this.buffList != null) {
			data.buffList = new ArrayList<>();
			data.buffList.addAll(this.buffList);
		}
		if (this.stateList != null) {
			data.stateList = new ArrayList<>();
			data.stateList.addAll(this.stateList);
		}
		return data;
	}

	/**
	 * 目标数据生成消息
	 * 
	 * @return
	 */
	public DTurnbasedSkillTarget createTargetDataMsg() {
		DTurnbasedSkillTarget.Builder msg = DTurnbasedSkillTarget.newBuilder();
		msg.setId(target.idFight);
		if (valueMap != null) {
			for (Map.Entry<String, Integer> entry : this.valueMap.entrySet()) {
				String key = entry.getKey();
				int value = entry.getValue();
				switch (key) {
				case ValueDamage:
					msg.setDamage(value);
					break;
				case ValueRage:
					msg.setRage(value);
					break;
				case ValueShowRage:
					msg.setShowRage(value);
					break;
				case ValueShield:
					msg.setShield(value);
					break;
				}
			}
		}
		if (stateList != null) {
			for (String state : stateList) {
				switch (state) {
				case StateCrit:
					msg.setIsCrit(true);
					break;
				case StateBlock:
					msg.setIsBlock(true);
					break;
				case StateDodge:
					msg.setIsDodge(true);
					break;
				case StateImmune:
					msg.setIsImmune(true);
					break;
				}
			}
		}
		if (buffList != null) {
			for (Buff buff : buffList) {
				DTurnbasedBuff.Builder buffMsg = DTurnbasedBuff.newBuilder();
				buffMsg.setId(buff.fightId);
				buffMsg.setSn(buff.sn);
				buffMsg.setRoundLeft(buff.round);
				msg.addBuffList(buffMsg.build());
			}
		}
		// 当前目标最大血量
		msg.setHpMax(PropManager.inst().getMaxHp(target));
		return msg.build();
	}

	/**
	 * 获取怒气值
	 * 
	 * @return
	 */
	public int getRage() {
		if (valueMap == null) {
			return 0;
		}
		Integer rage = valueMap.get(ValueRage);
		if (rage == null) {
			return 0;
		}
		return rage.intValue();
	}

	/**
	 * 获取伤害值
	 * 
	 * @return
	 */
	public int getDamage() {
		if (valueMap == null) {
			return 0;
		}
		Integer damage = valueMap.get(ValueDamage);
		if (damage == null) {
			return 0;
		}
		return damage.intValue();
	}

	/**
	 * 获取显示怒气值
	 * 
	 * @return
	 */
	public int getShowRage() {
		if (valueMap == null) {
			return 0;
		}
		Integer showRage = valueMap.get(ValueShowRage);
		if (showRage == null) {
			return 0;
		}
		return showRage.intValue();
	}

	/**
	 * 获取护盾值
	 * 
	 * @return
	 */
	public int getShield() {
		if (valueMap == null) {
			return 0;
		}
		Integer shield = valueMap.get(ValueShield);
		if (shield == null) {
			return 0;
		}
		return shield.intValue();
	}

}
