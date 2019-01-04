package game.worldsrv.character;

import game.msg.Define.DStageMonster;
import game.msg.Define.DStageObject;
import game.msg.Define.EWorldObjectType;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfRoleModel;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Monster;
import game.worldsrv.monster.MonsterManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.support.Param;

/**
 * 怪物
 */
public class MonsterObject extends CharacterObject {

	public ConfPartnerProperty confProperty; // 属性配置
	
	/**
	 * 怪物初始化
	 * @param stageObj
	 * @param order 站位顺序
	 * @param sn 怪物sn
	 * @param index
	 * @param isMonsterAddProp
	 * @param level 指定怪物等级
	 */
	public MonsterObject(StageObject stageObj, int order, int sn, int index, 
			boolean isMonsterAddProp, int level) {
		super(stageObj);

		// 初始化怪物数据
		Monster monster = MonsterManager.inst().initMonster(sn, level);
		if (monster != null) {
			// 如场景里的小怪可以用stageObj.getAutoId()代替Port.applyId()
			monster.setId(stageObj.getAutoId());
		} else {
			Log.table.error("===MonsterObject.initMonster error in monsterSn={},lv={}", sn, level);
			return;
		}
		
		this.dataPers.unit = monster;
		this.id = monster.getId();
		this.sn = sn;
		this.order = order;
		this.index = index;
		this.confProperty = ConfPartnerProperty.get(sn);
		this.confModel = ConfRoleModel.get(this.confProperty.modId);
		this.modelSn = this.confModel.sn;
		this.name = confProperty.name;
//		this.teamBundleID = confProperty.teamBundleID;
		if (level <= 0) {// 没有指定怪物等级，就用配表的等级
			level = confProperty.lvl;
		}
		
		// ***属性设置
		// 设置基础属性：移动和攻击速度
		this.props.put(EntityUnitPropPlus.Base.name(), new PropCalcCommon());
		// 设置等级属性
		PropCalcCommon levelPropCalc = new PropCalcCommon();
		this.props.put(EntityUnitPropPlus.Level.name(), levelPropCalc);
		levelPropCalc.add(confProperty.propName, confProperty.propValue);
		if (isMonsterAddProp) {// 当人物战力小于推荐战力时，增加怪物属性
			// 属性加成
			Map<String, Integer> mapPropAdd = new HashMap<String, Integer>();
			Map<String, Integer> mapPropAddBoss = new HashMap<String, Integer>();
//			for (ConfPropAdd cp : ConfPropAdd.findAll()) {
//				mapPropAdd.put(cp.sn, cp.addPercent);
//				mapPropAddBoss.put(cp.sn, cp.addPercentBoss);
//			}
	
			for (Map.Entry<String, Integer> entry : mapPropAdd.entrySet()) {// 根据属性JSON赋值
				String key = entry.getKey();
				// 属性加成
				Double value = levelPropCalc.getValue(key);
				if (value == null)
					continue;
				if (this.confProperty.roleType == EWorldObjectType.MonsterBoss_VALUE) {
					// 加强BOSS属性
					value = value * (1 + mapPropAddBoss.get(key) / Utils.F100);
				} else {// 加强普通怪物属性
					value = value * (1 + mapPropAdd.get(key) / Utils.F100);
				}
				levelPropCalc.put(key, value);
			}
		}
		PropCalcCommon sum = new PropCalcCommon();
		for (PropCalcCommon propCal : this.props.values()) {
			sum.add(propCal);
		}
		this.propsSum = sum;
		UnitManager.inst().setUnitProp(sum, this.getUnit());
		monster.setHpCur(monster.getHpMax());
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
	}

	@Override
	public void startup() {
		// 确定是否new成功，基础数据为空说明配置出错，不允许启动
		if (this.getUnit() == null)
			return;

		super.startup();

		// 进入场景
		if (stageObj != null) {
			this.stageEnter(stageObj);
		}
	}

	/**
	 * 怪物死亡，根据怪物配置来确定怪物是否要删除
	 */
	@Override
	public void die(UnitObject killer, Param params) {
		Event.fireEx(EventKey.MonsterBeKilledBefore, stageObj.mapSn, "killer", killer, "dead", this);
		super.die(killer, params);
		Event.fireEx(EventKey.MonsterBeKilled, stageObj.mapSn, "killer", killer, "dead", this);
	}

	/**
	 * 将玩家注册到地图中，但暂不显示玩家
	 * @param stageObj
	 */
	@Override
	public boolean stageRegister(StageObject stageObj) {
		if (stageObj.getCell(posNow) == null) {
			Log.human.error("===怪物注册到地图失败：mapSn={},posNow={},name={},modelSn={}", stageObj.mapSn, posNow.toString(),
					this.name, this.modelSn);
			return false;
		}

		// 调用父类实现
		return super.stageRegister(stageObj);
	}

	/**
	 * 在地图显示：正常出现
	 */
	@Override
	public void stageShow() {
		super.stageShow();

		// 抛出怪物出生事件
		// Event.fire(EventKey.MONSTER_BORN, "monsterObj", this);
	}

	/**
	 * 在地图显示：复活
	 */
	@Override
	public void stageShowRevive() {
		super.stageShowRevive();
	}

	@Override
	public void pulse(int tmDelta) {
		super.pulse(tmDelta);

		// sjh,调试怪物位置的代码
		// if(tmTemp == 0){
		// posTemp = this.posNow;
		// }
		// tmTemp += deltaTime;
		// if(tmTemp > 2000) {
		// tmTemp = 0;
		// if(posTemp.distance(this.posNow) > 0.1) {
		// posTemp = this.posNow;
		// Log.monster.info("===怪物位置：id={},name={},posNow={}", this.id,
		// this.name, this.posNow);
		// }
		// }
	}

	@Override
	public DStageObject.Builder createMsg() {
		// monster特有信息
		DStageMonster.Builder dMonster = DStageMonster.newBuilder();
		dMonster.addAllPosEnd(running.getRunPathMsg());
		dMonster.setHpCur(getUnit().getHpCur());
		dMonster.setHpMax(getUnit().getHpMax());
//		if (confModel != null) {
//			dMonster.setCollisionRadius((int) (confModel.collisionRadius));
//		}
		dMonster.setTeamBundleID(teamBundleID);
		dMonster.setIndex(index);
		dMonster.setCanAttack(canAttack);
		dMonster.setCanCastSkill(canCastSkill);
		dMonster.setCanMove(canMove);
		dMonster.setPropJson(propsSum.toJSONStr());
		dMonster.setCombat(getUnit().getCombat());
		dMonster.setSn(getUnit().getSn());
		dMonster.setSkillGroupSn(getUnit().getSkillGroupSn());

		//List<DSkill> skills = SkillManager.inst().getDSkillList(getUnit());
		//dMonster.addAllSkill(skills);

		// 共同信息
		DStageObject.Builder dObj = DStageObject.newBuilder();
		dObj.setPos(posNow.toMsg());// 位置
		dObj.setDir(dirNow.toMsg());// 朝向
		dObj.setObjId(id);// WordldObjectId
		dObj.setModelSn(modelSn);// 模型Sn
		dObj.setName(name);// 昵称
		dObj.setType(EWorldObjectType.Monster);// 对象类识别码，参见EWorldObjectType
		dObj.setMonster(dMonster);// 怪物对象

		return dObj;
	}

}
