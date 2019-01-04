package game.worldsrv.entity;

import core.gen.entity.Column;
import core.gen.entity.Entity;

@Entity(entityName = "GodsWarInfo", tableName = "godswar_info")
public enum EntityGodsWarInfo {
	/*众神之战*/	
	@Column(type = int.class,comment = "众神之战重置次数",defaults = "0")
	GWarReset,
	@Column(type = String.class, length = 16, comment = "通关次数，通关等级。结构：1,1",defaults = "0,0")
	GWarPass,
	@Column(type = int.class,comment = "敌方战怒气万分比",defaults = "0")
	GWarEnterMp,	
	@Column(type = int.class,comment = "我方战怒气万分比",defaults = "0")
	GWarMyMp,
	@Column(type = int.class,comment = "进度，关卡和宝箱一起",defaults = "1")
	GWarProgress,
	@Column(type = String.class,length = 128,comment = "对方阵容血量万分比",defaults = "")
	GWarEnterHp,
	@Column(type = String.class, length = 512, comment = "众神之战关卡对手id,结构：int position,long id 1:12L",defaults = "")
	GWarEnermys,	
	@Column(type = String.class, length = 512, comment = "记录上一次对手,结构：int position,long id 1:12L",defaults = "")
	GWarEneRecord,
	@Column(type = String.class, length = 512, comment = "众神之战死亡将领阵容,结构：10001,10002,10003",defaults = "")
	GWarDieList,
	@Column(type = String.class, length = 512, comment = "众神之站对手战斗力列表",defaults = "")
	GWarCombatList,	
	
	@Column(type = int.class,comment = "可以扫荡到第几关",defaults = "0")
	GWarAutoPro,
	@Column(type = boolean.class, comment = "是否已扫荡", defaults = "false")
	GWarIsAuto,
	;
}
