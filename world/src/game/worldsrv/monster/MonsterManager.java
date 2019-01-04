package game.worldsrv.monster;

import game.worldsrv.character.MonsterObject;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfRoleModel;
import game.worldsrv.entity.Monster;
import game.worldsrv.stage.StageBattleManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.Vector2D;
import core.support.SysException;

public class MonsterManager extends core.support.ManagerBase {

	/**
	 * 获取实例
	 * @return
	 */
	public static MonsterManager inst() {
		return inst(MonsterManager.class);
	}
	
	public Monster initMonster(int sn) {
		return initMonster(sn, 0);
	}
	/**
	 * 初始化怪物数据
	 * @param sn 怪物sn
	 * @param level 指定怪物等级，<=0则用表格配的等级
	 */
	public Monster initMonster(int sn, int level) {
		ConfPartnerProperty confProperty = ConfPartnerProperty.get(sn);
		if (confProperty == null) {
			Log.table.error("===ConfPartnerProperty 配表错误，no find sn={}", sn);
			return null;
		}
		ConfRoleModel confModel = ConfRoleModel.get(confProperty.modId);
		if (confModel == null) {
			Log.table.error("===ConfRoleModel配表错误，no find sn={}", confProperty.modId);
			return null;
		}

		Monster monster = new Monster();
		// 不再生成ID了，外部需要自己生成ID，
		// 如场景里的小怪可以用stageObj.getAutoId()代替Port.applyId()
		// 如世界BOSS的要保存数据到数据库的，必须要用Port.applyId()
		//monster.setId(Port.applyId());
		monster.setSn(sn);
		monster.setModelSn(confProperty.modId);
		monster.setName(confProperty.name);
		monster.setProfession(confProperty.type);
		
		// 初始化技能
		monster.setSkillAllSn(Utils.arrayIntToStr(confProperty.skill));
		//monster.setSkillGroupSn(0);

		// 属性设置
		if (level <= 0) {// 没有指定怪物等级，就用配表的等级
			level = confProperty.lvl;
		}
		monster.setLevel(level);
		return monster;
	}

	public void create(StageObject stageObj, int order, int monsterSn, Vector2D pos, Vector2D dir, double radius) {
		create(stageObj, order, monsterSn, pos, dir, radius, 1, 1, false, 0, 0);
	}
	public void create(StageObject stageObj, int order, int monsterSn, Vector2D pos, Vector2D dir, double radius,
			int num, int index, boolean isMonsterAddProp) {
		create(stageObj, order, monsterSn, pos, dir, radius, num, index, isMonsterAddProp, 0, 0);
	}
	public void create(StageObject stageObj, int order, int monsterSn, Vector2D pos, Vector2D dir, double radius,
			int num, int index, boolean isMonsterAddProp, int lvAVG) {
		create(stageObj, order, monsterSn, pos, dir, radius, num, index, isMonsterAddProp, lvAVG, 0);
	}
	/**
	 * 新的生成怪物：在指定场景中以指定位置和朝向等信息生成副本怪物
	 * @param stageObj
	 * @param order 站位顺序
	 * @param monsterSn 怪物SN(param1)
	 * @param pos 位置
	 * @param dir 朝向
	 * @param radius 半径
	 * @param num 数量
	 * @param index 当前波数的位置索引
	 * @param isMonsterAddProp 是否增加怪物属性
	 * @param lvAVG 指定怪物平均等级，<=0即不指定
	 * @param hpCur 指定怪物当前血量，<=0即不指定
	 */
	public void create(StageObject stageObj, int order, int monsterSn, Vector2D pos, Vector2D dir, double radius,
			int num, int index, boolean isMonsterAddProp, int lvAVG, int hpCur) {
		try {
			if (pos.isWrongPos()) {
				Log.game.error("===MonsterManager.create has wrong pos={},monsterSn={}", pos, monsterSn);
				return;
			}
			for (int i = 0; i < num; i++) {
				ConfPartnerProperty conf = ConfPartnerProperty.get(monsterSn);
				if (conf == null) {
					Log.table.error("ConfPartnerProperty配表错误，no find sn={}", monsterSn);
					continue;
				}
				if (stageObj.getCell(pos) == null) {// 无法找到地图单元格，认为是错误的摆怪点，不创建此怪物
					Log.game.error("===创建怪物的出生点无法获取到对应的地图格子cell：monsterSn={}, pos={}", monsterSn, pos.toString());
					continue;
				}
				
				MonsterObject monsterObj = new MonsterObject(stageObj, order, monsterSn, index, 
						isMonsterAddProp, lvAVG);
				if (hpCur > 0) {// 有指定怪物当前血量
					monsterObj.getUnit().setHpCur(hpCur);
				}
				Vector2D posRand = pos;
				if (num > 0) {
					posRand = StageBattleManager.inst().randomPosInCircle(pos, 0, radius);
					if (stageObj.getCell(posRand) == null) {
						posRand = pos;
					}
				}
				monsterObj.posBegin = posRand;
				monsterObj.dirBegin = dir;
				monsterObj.startup();
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

}
