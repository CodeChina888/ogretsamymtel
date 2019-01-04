package game.worldsrv.stage.types;

import core.support.Param;
import core.support.Time;
import game.msg.Define.ECrossFightType;
import game.turnbasedsrv.combat.types.CombatObjectPKHuman;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;

/**
 * 切磋真人玩家
 */
public class StageObjectPKHuman extends StageObject {
	public long milliSecStart = 0;// 副本开始时间戳(毫秒)
	public long milliSecEnd = 0;// 副本结束时间戳(毫秒)
	public ConfInstStage confInstStage = null;// 副本关卡配表数据
	public int fightType = 0;
	
	public StageObjectPKHuman(StagePort port, long stageId, int stageSn, int mapSn, int fightType, Param param) {
		super(port, stageId, stageSn, mapSn);
		
		confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={}", stageSn);
			return;
		}
		
		this.fightType = fightType;
		if (fightType == ECrossFightType.FIGHT_PK_HUMAN_VALUE) {
			// 切磋真人玩家是真的切换地图，所以在这里创建
			this.combatObj = new CombatObjectPKHuman(port, this, stageSn, mapSn, fightType, param);
		}
	}
	
	@Override
	public void pulse() {
		super.pulse();
		
		// 记录副本开始时间和结束时间
		if (this.milliSecStart == 0 && this.milliSecEnd == 0) {
			this.milliSecStart = this.getTime();// 记录副本开始时间戳
			if (confInstStage.limitSec <= 0)// 0即不限时
				this.milliSecEnd = Long.MAX_VALUE;
			else
				this.milliSecEnd = this.milliSecStart + confInstStage.limitSec * Time.SEC;
		}

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
