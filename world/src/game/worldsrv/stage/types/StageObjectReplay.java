package game.worldsrv.stage.types;

import game.worldsrv.fightrecord.RecordInfo;
import game.worldsrv.stage.StageObject;
import game.worldsrv.stage.StagePort;
import game.worldsrv.stage.StageRandomUtils;
import game.worldsrv.support.Utils;

/**
 * 普通单人副本场景
 */
public class StageObjectReplay extends StageObject {
	RecordInfo record;
	
	public StageObjectReplay(StagePort port, long stageId, int stageSn, int mapSn, RecordInfo record) {
		super(port, stageId, stageSn, mapSn);

		this.randUtils = new StageRandomUtils(Utils.I100);
		
		this.record = record;
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
		if (this.isDestroy)
			return;
		
		// 删除副本地图
		super.destory();
		this.isDestroy = true;
	}

}
