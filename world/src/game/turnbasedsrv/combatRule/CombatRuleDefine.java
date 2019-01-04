package game.turnbasedsrv.combatRule;

public class CombatRuleDefine {
	/** 回合流程 **/
	public static final String RoundPVE = "RoundPVE";
	/** 回合流程 **/
	public static final String RoundPVP = "RoundPVP";
	
	/** 胜负结算： 副本(默认胜利得1星,其他依条件判定，失败完全不得星) **/
	public static final String ResultInstance = "ResultInstance";
	/** 胜负结算： 副本剧情(默认胜利3星) **/
	public static final String ResultInstancePlot = "ResultInstancePlot";
	/** 胜负结算： 每日副本(默认胜利) **/
	public static final String ResultDailyInst = "ResultDailyInst";
	/** 胜负结算：竞技场 **/
	public static final String ResultCompete = "ResultCompete";
	/** 胜负结算： 普通 **/
	public static final String ResultCommon = "ResultCommon";
	/** 胜负结算： 总是胜利 **/
	public static final String ResultWin = "ResultWin";
	/** 胜负结算：爬塔(条件判定：任一条件不满足，则失败)*/
	public static final String ResultTower = "ResultTower";
	/** 胜负结算：资源本(条件判定：团灭才失败)*/
	public static final String ResultInstRes = "ResultInstRes";

	/** 得星：param回合内完成 **/
	public static final String StarRoundLimit = "StarRoundLimit";
	/** 得星：我方死亡人数<=param **/
	public static final String StarFriendDieLimit = "StarFriendDieLimit";
	/** 得星：我方主角未死亡 **/
	public static final String StarFriendHumanDie = "StarFriendHumanDie";
	/** 得星：我方剩余生命万分比>=param **/
	public static final String StarFriendLifePct = "StarFriendLifePct";
	
	/** 失败：设置最大回合数，未设置则默认最大回合20 **/
	public static final String RoundMax = "RoundMax";
	/** 失败：param回合内未完成 **/
	public static final String RoundLimit = "RoundLimit";
	/** 失败：我方死亡人数>param **/
	public static final String FriendDieLimit = "FriendDieLimit";
	/** 失败：我方主角死亡 **/
	public static final String FriendHumanDie = "FriendHumanDie";
	/** 失败：我方剩余生命万分比<param **/
	public static final String FriendLifePct = "FriendLifePct";
	
	/** 敌方生命万分比（默认为100%，用于每日副本进度） **/
	public static final String EnemyLifePct = "EnemyLifePct";
	
	/** 回合触发怪物 **/
	public static final String MonsterEnter = "MonsterEnter";
	/** 剧情 **/
	public static final String Plot = "Plot";
	/** 主角强化 **/
	public static final String HumanEnhance = "HumanEnhance";

}
