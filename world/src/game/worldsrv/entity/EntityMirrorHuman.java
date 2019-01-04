package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

/**
 * 玩家镜像数据
 */
@Entity(entityName = "MirrorHuman", isSuper = true, superEntity = EntityUnit.class)
public enum EntityMirrorHuman {
	// 上阵伙伴信息
	@Column(type = String.class, length = 256, comment = "伙伴阵容(id列表) ", defaults = "")
	PartnerLineup,
	@Column(type = int.class, comment = "伙伴站位(0-W型；1-M型)")
	PartnerStance,
	
	// 上阵爆点信息
	@Column(type = String.class, length = 128, comment = "上阵爆点技能sn,lv,power,value", defaults = "")
	InstallGodsJSON,

	// 称号信息
	@Column(type = int.class, comment = "称号sn", defaults = "0")
	TitleSn,
	@Column(type = boolean.class, comment = "是否显示称号")
	TitleShow,
	;
}