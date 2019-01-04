package game.worldsrv.stage.types;

import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfWorldBoss;
import game.worldsrv.instWorldBoss.WorldBossServiceProxy;
import game.worldsrv.character.WorldObject;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;

/**
 * 世界boss副本场景
 */
public class StageObjectInstWorldBoss extends StageObject {
	public ConfWorldBoss confWorldBoss = null;// 世界boss配表数据
	public ConfInstActConfig confInstActConfig = null;// 活动副本配表数据
	public int fightType = 0;
	
	public StageObjectInstWorldBoss(StagePort port, long stageId, int stageSn, int mapSn, int fightType) {
		super(port, stageId, stageSn, mapSn);

		confWorldBoss = ConfWorldBoss.get(stageSn);
		if (confWorldBoss == null) {
			Log.table.error("confWorldBoss配表错误，no find sn={}", stageSn);
			return;
		}
		confInstActConfig = ConfInstActConfig.get(confWorldBoss.actInstSn);
		if (confInstActConfig == null) {
			Log.table.error("ConfInstActConfig配表错误，no find sn={}", confWorldBoss.actInstSn);
			return;
		}
		this.fightType = fightType;
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

	@Override
	public boolean _addWorldObj(WorldObject obj) {
		super._addWorldObj(obj);
		
		if (obj instanceof HumanObject) {// 记录玩家进入
			WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
			prx.humanEnter(((HumanObject) obj).getHuman(), confInstActConfig.sn);
		}
		return true;
	}
	@Override
	public void _delWorldObj(WorldObject obj) {
		super._delWorldObj(obj);
		
		if (obj instanceof HumanObject) {// 记录玩家离开
			WorldBossServiceProxy prx = WorldBossServiceProxy.newInstance();
			prx.humanLeave(obj.id, confInstActConfig.sn);
		}
	}
	
}
