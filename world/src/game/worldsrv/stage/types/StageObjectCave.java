package game.worldsrv.stage.types;

import core.support.Param;
import game.turnbasedsrv.combat.types.CombatObjectCave;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;

/**
 * 离线仙府场景
 */
public class StageObjectCave extends StageObject {
		
	public StageObjectCave(StagePort port, long stageId, int stageSn, int mapSn, int fightType, Param param) {
		super(port, stageId, stageSn, mapSn);

		this.combatObj = new CombatObjectCave(port, this, stageSn, mapSn, fightType, param);
	}
	
	@Override
	public void pulse() {
		super.pulse();

		long curr = this.getTime();
		// 单人副本，按副本创建时间戳，延时几秒后检查是否没人，是则关闭
		if (curr - this.createTime > DESTROY_TIME) {
			// 副本开启后，如果一定时间内副本没人则销毁副本
			if (this.getHumanObjs().isEmpty()) {
				this.destory();
			}
		}
	}
	
	@Override
	public void destory() {
		super.destory();
	}
}
