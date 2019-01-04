package game.worldsrv.achievement.typedata;

import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchievement;
import game.worldsrv.entity.Achievement;
import game.worldsrv.achievement.AchievementTypeKey;

/**
 * 完成{0}次世界boss最后一击
 */
public class AchievementType14Data extends AbstractAchievementTypeData {

	private static AchievementType14Data instance = new AchievementType14Data();

	public static AchievementType14Data getInstance() {
		return instance;
	}

	private AchievementType14Data() {
		super(AchievementTypeKey.ACHIEVEMENT_TYPE_14);
	}

	@Override
	public Achievement init(long humanId, ConfAchievement conf) {
		// 初始化任务VO类
		Achievement achieve = super.init(humanId, conf);
		return achieve;
	}

	@Override
	public boolean onCheck(HumanObject humanObj, Achievement achieve) {
		return checkProgressStatus(achieve);
	}
	
	/**
	 * 设置任务进度   该类型特殊处理
	 */
	@Override
	public boolean doProgress(HumanObject humanObj, Achievement achieve,
			int progress) {

		if (isFinished(achieve)) {
			return false;
		}
		//累计
		achieve.setProgress(achieve.getProgress() + progress);
		// 检查任务完成状态
		checkProgressStatus(achieve);
		return true;
	}
}
