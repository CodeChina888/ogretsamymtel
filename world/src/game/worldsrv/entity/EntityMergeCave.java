package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="MergeCave", tableName="merge_cave")
public enum EntityMergeCave {
	@Column(type=String.class, comment="仙域json字符串", length=2000)
	CaveJsonStr,
	@Column(type=int.class, comment="处理标记：0-未处理 1-已处理")
	Flag,;
}
