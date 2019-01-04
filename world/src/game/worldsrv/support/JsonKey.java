package game.worldsrv.support;

/**
 * 定义用于传递的参数变量
 */
public final class JsonKey {
	// 战斗对象参数
	public final static String FightPos = "FP";		// 位置
	public final static String FightSn = "SN";		// sn
	public final static String FightSex = "SX";		// 性别
	public final static String FightProp = "PP";	// 属性
	public final static String FightSkill = "SK";	// 技能
	public final static String FightId = "ID";		// id
	public final static String FightGen = "G";		// 武将标识
	public final static String FightCombat = "FC";	// 战力
		
	// 活动用的参数(活动这块参数部份不是单单代码里的参数，还关联表格中参数，请谨慎修改)
	public final static String SignTime = "ST";		// 签到时间
	public final static String SignIndex = "SI";	// 签到索引
	public final static String RewardState = "RS";	// 奖励状态
	public final static String Gold = "GD";			// 签到元宝
	public final static String HoroCount = "HC";	// 招纳次数
	public final static String Process = "p";		// 进度
	public final static String ShowKey = "s";		// 显示进度
	
	public final static String Vip = "VIP";			// vip级别要求
	public final static String CostGold = "CG";		// 购买元宝需求
	public final static String BuyState = "BS";		// 购买状态
	
	public final static String FundCount = "FC";	// 基金次数
	public final static String DayTime = "DT";		// 日签到时间
	public final static String WeekTime = "WT";		// 周签到时间
	public final static String DayReward = "DR";	// 日领奖状态
	public final static String WeekReward = "WR";	// 周领奖状态
	
}
