package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * @author fzhg
 * @see :全服活动数据
 */
@Entity(entityName = "ActivityGlobal", tableName = "activity_global")
public enum EntityActivityGlobal {
	@Column(type = int.class, comment = "全服购买次数", defaults = "0")
	FundBuyCount,
	;
}
