package game.worldsrv.stage.types;

import core.support.Time;
import game.turnbasedsrv.combat.types.CombatObjectNewbie;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;

public class StageObjectNewbie extends StageObject {
	public static long LIMIT_TIME = 20 * Time.MIN;

	public StageObjectNewbie(StagePort port, long stageId, int stageSn, int mapSn, int fightType) {
		super(port, stageId, stageSn, mapSn);
		this.combatObj = new CombatObjectNewbie(port, this, stageSn, mapSn, fightType);
	}

	@Override
	public void pulse() {
		super.pulse();

		long curr = this.getTime();

		// 如果一定时间内副本没人 删除
		if (curr - this.createTime > DESTROY_TIME) {
			//战斗结束，新手地图不保留
			if(this.combatObj.isEnd()){
				this.destory();
				return;
			}
			// 没人了直接删除地图
			if (this.getHumanObjs().isEmpty()) {
				this.destory();
				return;
			}
		}
		//如果一定时间内没结束，则强制结束
		if (curr - this.createTime > LIMIT_TIME) {
			this.destory();
			return;
		}
	}

	@Override
	public void destory() {
		if (this.isDestroy)
			return;
		
		// 删除副本地图
		super.destory();
		this.isDestroy = true;
	}
}
