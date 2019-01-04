package game.worldsrv.support;

import java.util.Properties;

/**
 * 节点配置
 */
public class D {
	// 配置文件名称
	private static final String DISTR_NAME = "distr.properties";

	// 前缀
	public static final String NODE_WORLD_PREFIX = "world"; // 游戏服务器Node前缀

	public static final String PORT_PLATFORM_LOGIN_PREFIX = "login"; // 平台登陆业务Port前缀

	public static final String PORT_GAME_PREFIX = "game"; // 游戏业务Port前缀
	public static final String PORT_STAGE_PREFIX = "stage"; // 游戏地图Port前缀

	
	public static final String SERV_STAGE_DEFAULT = "stageDefault"; // 地图默认服务
	
	// Service
	public static final String SERV_DATA_RESET = "common.DataResetService"; // 每日数据重置
	public static final String SERV_WORLD_PF = "integration.PFService";// 对平台服务器提供服务
	public static final String SERV_GIFT = "gift.GiftServer";			// 对平台服务器提供服务
	
	public static final String SERV_STAGE_GLOBAL = "stage.StageGlobalService"; // 全局地图信息服务
	
	public static final String SERV_NAME = "worldsrv.name.NameService"; // 名字服务
	public static final String SERV_TOWER = "worldsrv.tower.TowerService"; // 爬塔
	public static final String SERV_COMPETE = "worldsrv.compete.CompeteService"; // 个人竞技
	public static final String SERV_HUMAN_GLOBAL = "worldsrv.human.HumanGlobalService"; // 全局玩家信息服务
	public static final String SERV_HUMAN_APPLY = "worldsrv.human.HumanApplyService"; // 玩家申请服务
	public static final String SERV_PAY = "worldsrv.payment.PaymentService"; //充值服务
	public static final String SERV_KEYACTIVATE = "worldsrv.keyActivate.KeyActivateService"; //激活码服务
	
	public static final String SERV_ACTIVITY = "worldsrv.activity.ActivityService"; // 活动
	public static final String SERV_FRIEND = "worldsrv.friend.FriendService"; // 好友服务
	public static final String SERV_CHAT = "worldsrv.inform.ChatService"; //聊天服务
	public static final String SERV_RANK = "worldsrv.rank.RankGlobalService"; // 排行服务
	public static final String SERV_GUILD = "worldsrv.guild.GuildService"; // 工会服务
	public static final String SERV_CAVE = "worldsrv.immortalCave.CaveService"; // 仙域服务
	public static final String SERV_MAIL = "worldsrv.mail.MailService"; // 邮件服务
	public static final String SERV_FILL_MAIL = "worldsrv.mail.FillMailService";	//全服补偿邮件服务
	public static final String SERV_NOTICE = "worldsrv.notice.NoticeService";	// 全服公告服务
	public static final String SERV_OFFILNE = "worldsrv.offline.OffilineGlobalService";	// 全服离线数据服务
	
	public static final String SERV_MAINCITY ="worldsrv.maincity.MaincityService";//城主服务
	
	public static final String SERV_WORLDBOSS = "worldsrv.instWorldBoss.WorldBossService"; // 世界boss服务
	public static final String SERV_LOOTMAP = "worldsrv.instLootMap.InstLootMapService";// 抢夺本
	
	public static final String SERV_TEAM = "worldsrv.team.TeamService"; // 队伍服务
	
	// 其他配置
	public static final int NODE_WORLD_STARTUP_NUM; // 游戏服务实例数
	public static final int PORT_WORLD_STARTUP_PLATFORM_LOGIN; // 游戏服务实例数
	public static final int PORT_STAGE_STARTUP_NUM; // 地图Port服务实例数
	public static final int PORT_GAME_STARTUP_NUM; // 游戏业务Port服务实例数
	
	
	//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String CROSS_NODE_PREFIX = "cross";						//跨服
	public static final String CROSS_SERV_STAGE_GLOBAL = "crossStageGlobal";	//全局地图信息服务
	public static final String CROSS_SERV_STAGE_DEFAULT = "crossStageDefault";	//地图默认服务
	public static final String CROSS_SERV_GROUP_FIGHT = "crossGroupFight";		//多人进入的战斗
	public static final String CROSS_SERV_SINGLE_FIGHT = "crossSingleFight";	//单人进入的战斗
	public static final String CROSS_SERV_INFORM = "crossInform";				//跨服聊天
	public static final String SERV_COMBATANT_GLOBAL = "combatantGlobal";		//跨服全局玩家信息服务
	public static final String CROSS_PORT_PREFIX = "crossGame";					//跨服游戏业务Port前缀
	public static final int CROSS_PORT_STARTUP_NUM;								//跨服游戏业务Port服务实例数
	public static final String CROSS_STAGE_PORT_PREFIX = "crossStagePort";		//跨服场景业务Port前缀
	public static final int CROSS_STAGE_PORT_NUM;								//跨服场景业务Port服务实例数
	//跨服配置///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	static {
		Properties prop = Utils.readProperties(DISTR_NAME); // 加载

		NODE_WORLD_STARTUP_NUM = Integer.valueOf(prop.getProperty("node.startup.num.world"));
		PORT_WORLD_STARTUP_PLATFORM_LOGIN = Integer.valueOf(prop.getProperty("port.startup.num.platform.login"));
		PORT_STAGE_STARTUP_NUM = Integer.valueOf(prop.getProperty("port.startup.num.stage"));
		PORT_GAME_STARTUP_NUM = Integer.valueOf(prop.getProperty("port.startup.num.game"));
		
		if (prop.containsKey("port.startup.num.crossGame")) {
			CROSS_PORT_STARTUP_NUM = Integer.parseInt(prop.getProperty("port.startup.num.crossGame"));
		} else {
			CROSS_PORT_STARTUP_NUM = 1;
		}
		if (prop.containsKey("port.startup.num.crossStagePort")) {
			CROSS_STAGE_PORT_NUM = Integer.parseInt(prop.getProperty("port.startup.num.crossStagePort"));
		} else {
			CROSS_STAGE_PORT_NUM = 1;
		}
	}
}