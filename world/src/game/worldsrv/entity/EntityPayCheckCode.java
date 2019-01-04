package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 充值校验码
 */
@Entity(entityName = "PayCheckCode", tableName="pay_check_code")
public enum EntityPayCheckCode {
	@Column(type=String.class, comment="校验码", index=true)
	CheckCode,
	@Column(type=long.class, comment="生成时间")
	Time,
	@Column(type=long.class, comment="玩家ID")
	HumanId,
	;
}