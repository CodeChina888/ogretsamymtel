package game.worldsrv.stage.types;

import game.turnbasedsrv.combat.types.CombatObjectInstance;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;

/**
 * 普通单人副本场景
 */
public class StageObjectInstance extends StageObject {
	
	public long milliSecEnd = 0;// 副本结束时间戳(毫秒)

	public StageObjectInstance(StagePort port, long stageId, int stageSn, int mapSn, int fightType) {
		super(port, stageId, stageSn, mapSn);

		this.combatObj = new CombatObjectInstance(port, this, stageSn, mapSn, fightType);
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
