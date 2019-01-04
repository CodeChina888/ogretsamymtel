package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "Quest", tableName = "quest")
public enum EntityQuest {
	@Column(type = String.class, length = 32, comment = "玩家名字")
	Name,
	@Column(type = boolean.class, comment = "七天奖励是否补发了", defaults = "false")
	SevenQuestOverReissue,
	@Column(type = String.class, length = 2048, comment = "普通任务", defaults = "[]")
	NormalJSON, 
	@Column(type = String.class, length = 5120, comment = "每日任务", defaults = "[]")
	DailyJSON,
	@Column(type = String.class, length = 5120, comment = "开服七天任务", defaults = "[]")
	SevenJSON,  
	;
}