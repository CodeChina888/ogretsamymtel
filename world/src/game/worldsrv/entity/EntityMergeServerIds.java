package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="MergeServerIds",tableName="merge_server_ids")
public enum EntityMergeServerIds {
	@Column(type=String.class, comment="已合并服务器id列表", length=6000)
	serverIds,;
}
