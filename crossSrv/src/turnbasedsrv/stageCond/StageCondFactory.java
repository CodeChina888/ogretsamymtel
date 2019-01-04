package turnbasedsrv.stageCond;

public class StageCondFactory {
	/**
	 * 获取条件判断类
	 */
	public static StageCondBase getStageAction(int id, String type, String param) {
		switch (type) {
		case StageCondDefine.RoundPVE:
			return new CondRoundPVE(id, param);
//		case StageCondDefine.RoundPVP:
//			return new CondRoundPVP(id, param);
		
		case StageCondDefine.ResultInstance:
			return new CondResultInstance(id, param);
		case StageCondDefine.ResultInstancePlot:
			return new CondResultInstancePlot(id, param);
		case StageCondDefine.ResultDailyInst:
			return new CondResultDailyInst(id, param);
		case StageCondDefine.ResultCompete:
			return new CondResultCompete(id, param);
		case StageCondDefine.ResultCommon:
			return new CondResultCommon(id, param);
		case StageCondDefine.ResultWin:
			return new CondResultWin(id, param);
		case StageCondDefine.ResultTower:
			return new CondResultTower(id, param);
		
		case StageCondDefine.StarRoundLimit:
			return new CondStarRoundLimit(id, param);
		case StageCondDefine.StarFriendDieLimit:
			return new CondStarFriendDieLimit(id, param);
		case StageCondDefine.StarFriendHumanDie:
			return new CondStarFriendHumanDie(id, param);
		case StageCondDefine.StarFriendLifePct:
			return new CondStarFriendLifePct(id, param);
		
		case StageCondDefine.RoundMax:
			return new CondRoundMax(id, param);
		case StageCondDefine.RoundLimit:
			return new CondRoundLimit(id, param);
		case StageCondDefine.FriendDieLimit:
			return new CondFriendDieLimit(id, param);
		case StageCondDefine.FriendHumanDie:
			return new CondFriendHumanDie(id, param);
		case StageCondDefine.FriendLifePct:
			return new CondFriendLifePct(id, param);
		
		case StageCondDefine.EnemyLifePct:
			return new CondEnemyLifePct(id, param);
			
		case StageCondDefine.MonsterEnter:
			return new CondMonsterEnter(id, param);
		case StageCondDefine.Plot:
			return new CondPlot(id, param);
		case StageCondDefine.HumanEnhance:
			return new CondHumanEnhance(id, param);
		}
		return null;
	}
}
