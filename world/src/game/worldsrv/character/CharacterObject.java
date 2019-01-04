package game.worldsrv.character;

import java.util.Comparator;

import game.worldsrv.character.UnitManager;
import game.msg.Define.DProp;
import game.msg.Define.DUnit;
import game.worldsrv.stage.StageCell;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;

import game.worldsrv.character.CharacterObject;
import game.worldsrv.character.UnitObject;

/**
 * 处理玩家 武将 怪物的公共属性 方法。 但是不包括战斗， 使得战斗可以独立
 * @author rattler
 */
public abstract class CharacterObject extends UnitObject implements Comparator<CharacterObject> {
	public CharacterObject(StageObject stageObj) {
		super(stageObj);
	}

	public StagePort getPort() {
		return stageObj.getPort();
	}

	@Override
	public void pulseMove(long timeCurr) {
		super.pulseMove(timeCurr);

		StageCell cellBegin = stageCell;
		StageCell cellEnd = stageObj.getCell(posNow);
		if (cellEnd == null) {
			return;
		}

		stageCell = cellEnd;
		// 判断玩家有没有跨地图格了
		if (cellBegin != null && !cellEnd.isEquals(cellBegin)) { // 跨地图格了
			StageManager.inst().cellChanged(cellBegin, cellEnd, this);
		}
	}

	public DUnit.Builder createDUnit() {
		// 基本信息
		DUnit.Builder dUnit = DUnit.newBuilder();
		dUnit.setId(id);								// 设置ID
		DProp dProp = UnitManager.inst().getDProp(getUnit());
		dUnit.setProp(dProp);							// 设置属性信息
		dUnit.setName(getUnit().getName());				// 昵称
		dUnit.setProfession(getUnit().getProfession()); // 职业
		dUnit.setLevel(getUnit().getLevel());			// 等级
		dUnit.setModelSn(getUnit().getModelSn());		// 模型sn
		dUnit.setSn(getUnit().getSn());					// 配置sn
		return dUnit;
	}

	@Override
	public int compare(CharacterObject u1, CharacterObject u2) {
		if (u1 == null || u2 == null)
			return 0;
		if (u1.profession < u2.profession)
			return -1;
		else if (u1.profession > u2.profession)
			return 1;
		else
			return u1.order - u2.order;
	}

}
