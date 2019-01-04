package turnbasedsrv.buff;

import java.util.ArrayList;
import java.util.List;

import turnbasedsrv.fightObj.FightObject;

/**
 * buff执行时总数据
 * 
 * @author landy
 *
 */
public class BuffTriggerData {
	/** buff **/
	public Buff buff;
	/** 目标列表 **/
	List<FightObject> targetList = new ArrayList<>();
	/** 伤害值 **/
	public int damage;
	/** 怒气值 **/
	public int rage;
	/** buff列表 **/
	public List<Buff> buffList;

	/**
	 * 构造函数
	 * 
	 * @param sn
	 */
	public BuffTriggerData(Buff buff) {
		this.buff = buff;
	}

	/**
	 * 技能影响的所有目标
	 * 
	 * @return
	 */
	public List<FightObject> getAllTarget() {
		return targetList;
	}

	/**
	 * 记录新增目标
	 * 
	 * @param target
	 */
	public void addTarget(FightObject target) {
		if (target == null) {
			return;
		}
		if (targetList.contains(target)) {
			return;
		}
		targetList.add(target);
	}

	/**
	 * 增加目标数据,同目标的数据合并
	 * 
	 * @param targetData
	 */
	public void addTargetData(FightObject fightObj, List<Buff> buffList, int damage, int rage) {
		if (fightObj != this.buff.owner) {
			return;
		}
		this.damage = this.damage + damage;
		this.rage = this.rage + rage;
		if (buffList != null) {
			if (this.buffList == null) {
				this.buffList = new ArrayList<>();
			}
			for (Buff buff : buffList) {
				if (!this.buffList.contains(buff)) {
					this.buffList.add(buff);
				}
			}
		}
	}

}
