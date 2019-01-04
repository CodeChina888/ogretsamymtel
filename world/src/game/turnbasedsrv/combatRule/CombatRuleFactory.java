package game.turnbasedsrv.combatRule;


public class CombatRuleFactory {
	/**
	 * 获取条件判断类
	 */
	public static CombatRuleBase getStageAction(int id,String type, String param) {
		switch (type) {
		case CombatRuleDefine.RoundPVE:
			return new RuleRoundPVE(id, param);
		case CombatRuleDefine.RoundPVP:
			return new RuleRoundPVP(id, param);
		
		case CombatRuleDefine.ResultInstance:
			return new RuleResultInstance(id, param);
		case CombatRuleDefine.ResultInstancePlot:
			return new RuleResultInstancePlot(id, param);
		case CombatRuleDefine.ResultDailyInst:
			return new RuleResultDailyInst(id, param);
		case CombatRuleDefine.ResultCompete:
			return new RuleResultCompete(id, param);
		case CombatRuleDefine.ResultCommon:
			return new RuleResultCommon(id, param);
		case CombatRuleDefine.ResultWin:
			return new RuleResultWin(id, param);
		case CombatRuleDefine.ResultTower:
			return new RuleResultTower(id, param);
		case CombatRuleDefine.ResultInstRes:
			return new RuleResultInstRes(id, param);
		
		case CombatRuleDefine.StarRoundLimit:
			return new RuleStarRoundLimit(id, param);
		case CombatRuleDefine.StarFriendDieLimit:
			return new RuleStarFriendDieLimit(id, param);
		case CombatRuleDefine.StarFriendHumanDie:
			return new RuleStarFriendHumanDie(id, param);
		case CombatRuleDefine.StarFriendLifePct:
			return new RuleStarFriendLifePct(id, param);
		
		case CombatRuleDefine.RoundMax:
			return new RuleRoundMax(id, param);
		case CombatRuleDefine.RoundLimit:
			return new RuleRoundLimit(id, param);
		case CombatRuleDefine.FriendDieLimit:
			return new RuleFriendDieLimit(id, param);
		case CombatRuleDefine.FriendHumanDie:
			return new RuleFriendHumanDie(id, param);
		case CombatRuleDefine.FriendLifePct:
			return new RuleFriendLifePct(id, param);
		
		case CombatRuleDefine.EnemyLifePct:
			return new RuleEnemyLifePct(id, param);
			
		case CombatRuleDefine.MonsterEnter:
			return new RuleMonsterEnter(id, param);
		case CombatRuleDefine.Plot:
			return new RulePlot(id, param);
		case CombatRuleDefine.HumanEnhance:
			return new RuleHumanEnhance(id, param);
		}
		return null;
	}
}
