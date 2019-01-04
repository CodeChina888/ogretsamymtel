package game.turnbasedsrv.fightObj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.PortPulseQueue;
import core.interfaces.ISerilizable;
import game.msg.Define.DTurnbasedObject;
import game.msg.Define.EStanceType;
import game.msg.Define.ETeamType;
import game.turnbasedsrv.buff.BuffCollection;
import game.turnbasedsrv.combat.CombatObject;
import game.turnbasedsrv.enumType.SkillType;
import game.turnbasedsrv.enumType.TriggerPoint;
import game.turnbasedsrv.param.TriggerParam;
import game.turnbasedsrv.prop.Prop;
import game.turnbasedsrv.prop.PropManager;
import game.turnbasedsrv.skill.Skill;
import game.turnbasedsrv.trigger.Trigger;
import game.turnbasedsrv.trigger.TriggerListen;
import game.worldsrv.config.ConfSkill;
import game.worldsrv.support.Log;

public abstract class FightObject implements ISerilizable {
	/** 所属地图 **/
	public CombatObject combatObj;
	/** 属性 **/
	public Prop prop = new Prop();

	/** 战斗中的临时id **/
	public int idFight;
	/** 名字 **/
	public String name = "";
	/** 模型sn **/
	public int modelSn;
	/** 配置表sn **/
	public int sn;

	/** 是否在地图上显示 **/
	protected boolean inWorld = false;

	public abstract DTurnbasedObject.Builder createMsg();

	/** 制造这个对象的源头 **/
	public FightObject fireObj;
	/** 创建的时间 **/
	private long tmCreate;

	public int pos;// 队中位置
	public ETeamType team;// 所属战队
	public EStanceType stance;// 队伍站位
	public int combat = 0;// 战斗力
	
	/** 监听列表 **/
	public List<TriggerListen> listenList = new ArrayList<>();
	/** 被动技能列表 **/
	public List<Skill> passiveSkillList = new ArrayList<>();
	/** 普通技能列表 **/
	public List<Skill> commonSkillList = new ArrayList<>();
	public int indexOfCommonSkill = -1;// 选择的普通技能下标，默认0即第一个,-1即未指定则随机选择
	/** 怒气技能 **/
	public Skill specialSkill;
	/** buff管理器 **/
	public BuffCollection buffManager;
	/** 是否死亡 **/
	public boolean isDead = false;
	/** 性别：1男，2女 **/
	public int sex = 0;
	/** 所属阵营 **/
	public int camp = 0;

	/**
	 * 构造函数
	 * 
	 * @param stageObj
	 */
	public FightObject(CombatObject stageObj) {
		this.combatObj = stageObj;
		this.buffManager = new BuffCollection(this);
	}

	/**
	 * 构造函数
	 */
	public FightObject() {
		this.buffManager = new BuffCollection(this);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(idFight);
		out.write(name);
		out.write(modelSn);
		out.write(sn);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		idFight = in.read();
		name = in.read();
		modelSn = in.read();
		sn = in.read();
	}

	/**
	 * 是否死亡
	 * 
	 * @return
	 */
	public boolean isDie() {
		return isDead;
	}
	
	/**
	 * 死亡过程处理亡语被动
	 */
	public void deathPassive() {
		for (Skill skill : passiveSkillList) {
			skill.deathPassive();
		}
	}

	/**
	 * 死亡过程处理
	 */
	public void Deading(FightObject killer) {
		// 已经死亡
		if (isDead) {
			return;
		}
		// 死亡立即触发被动
		deathPassive();
		
		stageLeave();

		TriggerParam param = new TriggerParam(this, this);
		Trigger trigger = new Trigger(TriggerPoint.Dead, param);
		this.combatObj.triggerManager.addTrigger(trigger);

		TriggerParam param1 = new TriggerParam(killer, this);
		Trigger trigger1 = new Trigger(TriggerPoint.Kill, param1);
		this.combatObj.triggerManager.addTrigger(trigger1);
		
		isDead = true;
	}
	
	/**
	 * 是否在场景中
	 * 
	 * @return
	 */
	public boolean isInWorld() {
		return inWorld;
	}

	/**
	 * 激活被动
	 */
	public void startup() {
		for (int i = 0; i < this.passiveSkillList.size(); i++) {
			Skill skill = this.passiveSkillList.get(i);
			skill.startPassive();
		}
	}

	/**
	 * 进入地图
	 * 
	 * @param stageObj
	 */
	public void stageRegister(CombatObject stageObj) {
		// 在地图中添加活动单元
		stageObj._addFightObj(this);

		this.tmCreate = stageObj.getTime();
	}

	/**
	 * 获取创建时间
	 * 
	 * @return
	 */
	public long getCreateTime() {
		return this.tmCreate;
	}

	/**
	 * 在地图显示：正常出现
	 */
	public void stageShow() {
		// 已在地图中的 忽略
		if (inWorld) {
			Log.fight.warn("使活动单元进入地图时发现inWorld状态为true：data={}", this);
			return;
		}

		// 设置状态为在地图中
		inWorld = true;

		// 调试日志
		if (Log.fight.isDebugEnabled()) {
			Log.fight.debug("地图单位进入地图: mapSn={}, repSn={}, objId={}, objSn={}, objName={}", combatObj.mapSn,
					combatObj.stageSn, idFight, sn, name);
		}

		// 通知其他玩家 有地图单元进入视野
		// StageManager.inst().sendMsgToArea(createMsgAppear(AppearType.Appear),
		// stageObj, posNow);
	}

	/**
	 * 在地图显示：复活
	 */
	public void stageShowRevive() {
		// 已在地图中的 忽略
		// if (inWorld) {
		// Log.stageCommon.warn("使活动单元进入地图时发现inWorld状态为true：data={}", this);
		// return;
		// }

		// 设置状态为在地图中
		inWorld = true;

		// 日志
		if (Log.fight.isInfoEnabled()) {
			Log.fight.info("地图单位进入地图: objId={}, objName={}", idFight, name);
		}

		// 通知其他玩家 有地图单元进入视野
		// StageManager.inst().sendMsgToArea(createMsgAppear(AppearType.Revive),
		// stageObj, posNow);
	}

	/**
	 * 进入地图
	 * 
	 * @param stageObj
	 */
	public void stageEnter(CombatObject stageObj) {
		// if(stageObj.getCell(posNow) == null) {
		// return;
		// }
		// 加入地图并显示
		stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "fightObj", this) {
			@Override
			public void execute(Port port) {
				FightObject fightObj = param.get("fightObj");
				CombatObject stageObj = param.get("stageObj");
				if (fightObj != null) {
					if (stageObj != null) {
						fightObj.stageRegister(stageObj);
					}
					fightObj.stageShow();
				}
			}
		});
	}

	public final void stageLeave() {
		// buff监听取消
		buffManager.killed();
		// 技能监听取消
		for (int i = 0; i < this.passiveSkillList.size(); i++) {
			Skill skill = this.passiveSkillList.get(i);
			skill.killed();
		}
		// 自身监听取消
		for (TriggerListen listen : this.listenList) {
			this.combatObj.triggerManager.delListen(listen);
		}
		listenList.clear();
		// 设置状态
		inWorld = false;		

		// 将具体删除操作排入队列 在心跳的最后在进行删除
		// 因为本心跳中可能还有后续操作需要本对象的实例
		if (combatObj == null) {
			return;
		}
		combatObj._delFightObj(this);
	}

	/**
	 * 从地图隐藏
	 */
	public void stageHide() {
		// 设置状态
		inWorld = false;

		// 通知其他玩家 有地图单元离开视野
		// StageManager.inst().sendMsgToArea(createMsgDisappear(DisappearType.Disappear),
		// stageObj, posNow);
	}

	/**
	 * 是玩家角色
	 * 
	 * @return
	 */
	public boolean isHuman() {
		return false;
	}

	/**
	 * 是怪物角色
	 * 
	 * @return
	 */
	public boolean isMonster() {
		return false;
	}

	/**
	 * 是武将角色
	 * 
	 * @return
	 */
	public boolean isGeneral() {
		return false;
	}

	/**
	 * 是boss
	 * 
	 * @return
	 */
	public boolean isBoss() {
		return false;
	}

	/**
	 * 获取战场位置
	 * 
	 * @return
	 */
	public int getFightPos() {
		// 1号战队即左下方，队中位置：0-9，战场位置：100-109
		// 2号战队即右上方，队中位置：0-9，战场位置：0-9
		if (team == ETeamType.Team1) {
			return pos + CombatObject.FightPosAdd;
		} else {
			return pos;
		}
	}

	/**
	 * 转为文本显示
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", idFight).toString();
	}

	/**
	 * 施放普通技能
	 */
	public void castCommonSkill() {
		if (commonSkillList == null) {
			return;
		}

		commonSkillList.get(indexOfCommonSkill).cast();

		indexOfCommonSkill = -1;// 重置选取技能索引
	}

	/**
	 * 施放怒气技能
	 */
	public void castSpecialSkill() {
		if (specialSkill == null) {
			return;
		}
		specialSkill.cast();
	}

	/**
	 * 是否可以施放怒气技能
	 * 
	 * @return
	 */
	public boolean canCastSpecialSkill() {
		if (specialSkill == null) {
			return false;
		}
		// 怒气判断
		if (specialSkill.confSkill.fireClearRage > 0) {
			int rage = PropManager.inst().getCurRage(this);
			if (rage < specialSkill.confSkill.fireClearRage) {
				return false;// 怒气不足
			}
		}
		// 检查是否处于buff控制状态中
		return buffManager.canCastSkill(specialSkill);
	}

	/**
	 * 是否可以施放普通技能
	 * 
	 * @return
	 */
	public boolean canCastCommonSkill() {
		if (this.commonSkillList == null) {
			return false;
		}
		
		if (indexOfCommonSkill < 0) {
			int rage = PropManager.inst().getCurRage(this);
			// 未指定，则选取一个能释放的技能，优先顺序：合击技能>怒气技能>普攻
			for (int type = SkillType.Joint.value(); type >= SkillType.Common.value(); type--) {
				for (int i = 0; i < commonSkillList.size(); i++) {
					Skill skill = commonSkillList.get(i);
					ConfSkill conf = ConfSkill.get(skill.sn);
					if (conf != null && conf.type == type && rage >= conf.fireClearRage) {
						// 检查合击技能条件
						if (type == SkillType.Joint.value()) {
							if (!this.combatObj.isExistRelation(conf.relationModelSN, this.team))
								continue;
						}
						// 检查是否有控制buff状态
						if (this.buffManager.canCastSkill(skill)) {
							indexOfCommonSkill = i;
							// 主角自动出手，不是最后一个技能选择
							if (this.isHuman() && i != commonSkillList.size()-1) {
								// 50%的概率是该技能
								if (combatObj.randUtils.nextInt(2) == 0) {
									continue;
								}
							}
							return true;
						}
					}
				}
			}
			return false;
		} else if (indexOfCommonSkill < commonSkillList.size()) {
			return this.buffManager.canCastSkill(commonSkillList.get(indexOfCommonSkill));
		}
		return false;
	}

	/**
	 * 增加监听事件
	 * 
	 * @param listen
	 * @return
	 */
	public boolean addListen(TriggerListen listen) {
		if (listenList.contains(listen)) {
			return false;
		}
		if (!combatObj.triggerManager.addListen(listen)) {
			return false;
		}
		listenList.add(listen);
		return true;
	}
}
