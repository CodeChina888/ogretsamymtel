package crosssrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Combatant", tableName = "combatant")
public enum EntityCombatant {
	/* 基础信息 */
	@Column(type = long.class, comment = "玩家ID", index = true)
	humanId,
}