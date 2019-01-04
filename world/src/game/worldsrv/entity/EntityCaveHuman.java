package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "CaveHuman", tableName = "cave_human" , superEntity = EntityMirrorHuman.class)
public enum EntityCaveHuman {
	@Column(type = boolean.class, comment = "是否机器人")
	IsRobot,
	@Column(type = long.class, comment = "工会id", defaults = "0")
	GuiLdId,
	@Column(type = String.class, comment = "工会名", defaults = "")
	GuiLdName,
}
