package crosssrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "FightRecord", tableName = "fight_record")
public enum EntityFightRecord {
	@Column(type = int.class, comment = "战斗类型")
	fightType, @Column(type = String.class, length = 32, comment = "左方战斗者名", index = true)
	leftName, @Column(type = int.class, comment = "左方战斗者sn")
	leftSn, @Column(type = int.class, comment = "左方战斗者品质")
	leftAptitude, @Column(type = int.class, comment = "左方战斗者战力")
	leftCombat, @Column(type = String.class, length = 32, comment = "右方战斗者名", index = true)
	rightName, @Column(type = int.class, comment = "右方战斗者sn")
	rightSn, @Column(type = int.class, comment = "右方战斗者品质")
	rightAptitude, @Column(type = int.class, comment = "右方战斗者战力")
	rightCombat, @Column(type = int.class, comment = "stageSn")
	stageSn, @Column(type = int.class, comment = "mapSn")
	mapSn, @Column(type = long.class, comment = "随机数种子")
	randSeed,

	@Column(type = String.class, length = Column.TEXT_MIN_SIZE, comment = "战斗左方数据")
	leftInfo, @Column(type = String.class, length = Column.TEXT_MIN_SIZE, comment = "战斗右方数据")
	rightInfo, @Column(type = String.class, length = Column.TEXT_MIN_SIZE, comment = "玩家操作数据")
	operateInfo, @Column(type = String.class, length = Column.TEXT_MIN_SIZE, comment = "战斗结果数据")
	finishInfo, @Column(type = String.class, length = Column.TEXT_MIN_SIZE, comment = "额外数据")
	extraInfo,

}