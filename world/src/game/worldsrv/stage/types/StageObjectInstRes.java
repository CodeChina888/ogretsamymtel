package game.worldsrv.stage.types;

import game.turnbasedsrv.combat.types.CombatObjectInstRes;
import game.worldsrv.config.ConfInstRes;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;

public class StageObjectInstRes extends StageObject {
	
	public ConfInstStage confInstStage = null;// 副本关卡配表数据
	public ConfInstRes confInstRes = null;// 资源本配表数据

	public StageObjectInstRes(StagePort port, long stageId, int instSn, int mapSn, int fightType, int instResSn) {
		super(port, stageId, instSn, mapSn);

		confInstStage = ConfInstStage.get(instSn);
		if (confInstStage == null) {
			Log.table.error("ConfRepStage配表错误，no find sn={}", instSn);
			return;
		}
		confInstRes = ConfInstRes.get(instResSn);
		if (confInstRes == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={}", instResSn);
			return;
		}
		
		this.combatObj = new CombatObjectInstRes(port, this, stageSn, mapSn, fightType);
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

