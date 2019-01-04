package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "HumanSkill", tableName = "human_skill")
public enum EntityHumanSkill {
	
	@Column(type = String.class, length = 64, comment = "上阵的技能：{s1:0,s2:0...}")
	InstallSkill,
	// String json = "[{"tag": "310","lv": "1","stage": "1","rune": [{"tag": "1","lv": "1"}, {"s": "2","lv": "1"}]}]";
	@Column(type = String.class, length = 2048, comment = "技能信息json[{tag:310,lv:1,st:1,ru:[{tag:1,lv:1},{tag:2,lv:1}]}]")
	SkillInfo,
	@Column(type = int.class, comment = "培养倍率(万分比)", defaults = "10000")
	TrainMutiple,
	@Column(type = int.class, comment = "可用的免费培养抽奖次数", defaults = "0")
	FreeTrainNum,
	@Column(type = String.class, length = 128, comment = "培养抽奖结果数组（对应各个槽位产出的道具sn）", defaults = "")
	SkillTrainResult,
	@Column(type = int.class,  comment = "上阵的爆点sn")
	InstallGods,
	@Column(type = String.class, length = 1024, comment = "爆点技能json[{tag:1,lv:1},{tag:2,lv:1}]")
	SkillGods,
	@Column(type = String.class, length = 1024, comment = "神通技能json[{type:1,st:1},{type:2,st:1}]")
	SkillTrain,
	;
}
