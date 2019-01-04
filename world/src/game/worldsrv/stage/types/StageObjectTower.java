package game.worldsrv.stage.types;

import core.support.Param;
import core.support.Time;
import game.turnbasedsrv.combat.types.CombatObjectCompete;
import game.turnbasedsrv.combat.types.CombatObjectTower;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;

/**
 * 
 * @author Neak
 */
public class StageObjectTower extends StageObject{
	public ConfInstStage confInstStage = null;// 副本关卡配表数据
	public long milliSecStart = 0;// 副本开始时间戳(毫秒)
	public long milliSecEnd = 0;// 副本结束时间戳(毫秒)
	
	// 挑战的层数
	public int fightLayer = 0;
	// 选择的难度
	public int selDiff = 0;
	
	// 是否经过了跨天
	public boolean isSpanDay = false;

	public StageObjectTower(StagePort port, long stageId, int stageSn, int mapSn, int fightType, int fightLayer, int selDiff, Param param) {
		super(port, stageId, stageSn, mapSn);
		this.fightLayer = fightLayer;
		this.selDiff = selDiff;

		confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={}", stageSn);
			return;
		}
		
		this.combatObj = new CombatObjectTower(port, this, stageSn, mapSn, fightType, param);
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
	
	// 设置跨天
	public void setIsSpanDay() {
		isSpanDay = true;
	}
	
}
