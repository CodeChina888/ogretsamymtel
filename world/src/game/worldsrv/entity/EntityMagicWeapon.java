package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "MagicWeapon", tableName = "magicWeapon")
public enum EntityMagicWeapon {
	
	@Column(type = String.class, length = 32, comment = "人物名字")
	HumanName, 
	@Column(type = String.class, length = 1024, comment = "法宝信息[{“sn”:法宝sn,“lv”:法宝等级,“exp”:法宝经验},{...}]")
	MWJSON,
	;
}
