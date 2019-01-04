package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * @author Neak
 * @see :玩家爬塔数据
 */
@Entity(entityName = "TowerGlobal", tableName = "tower_global")
public enum EntityTowerGlobal {
	@Column(type = String.class, length = 256, comment = "各层通过人数记录层数：1,2,3,4,5,6...max+1", defaults = "")
	layerPass,
	@Column(type = String.class, length = 256, comment = "爬塔每层首次通过状态0|0|0,0|0|0(0未被通过，1通过，|区分难度)", defaults = "")
	firstPassState,
	@Column(type = long.class, comment = "赛季结束时间")
	SeasonEndTime,
	;
}
