package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName="Achievement", tableName="achievement")
public enum EntityAchievement{
	@Column(type = long.class, comment = "玩家id", index=true)
	HumanId,
	@Column(type=int.class, comment="成就SN（Achievement.sn）")
	AchieveSn,
	@Column(type=int.class, comment="成就类型")
	AchieveType,
	@Column(type=int.class, comment="成就等级")
	AchieveLv,
	@Column(type=int.class, comment="当前成就类型唯一sn(AchievementType.sn)")
	UniqueSn,
	@Column(type=int.class, comment="当前目标(判断条件：eg1:5个伙伴50，则该值为50)； eg2:通过副本本1，则该值为1； eg3:累计话费1w金币，则该值为1w")
	Target,
	@Column(type=int.class, comment="当前进度(进度条件：eg1:5个伙伴50，则达成值为5)； eg2:通过副本1，则该值为对应目标值； eg3:累计话费1w金币，则该值为累计花费值")
	Progress,
	@Column(type=int.class, comment="状态")
	Status,
	@Column(type=long.class, comment="最后更新时间")
	UpdateTime,
	@Column(type=String.class, comment="奖励", length=1024, defaults="[]")
	AwardsJson,
}
