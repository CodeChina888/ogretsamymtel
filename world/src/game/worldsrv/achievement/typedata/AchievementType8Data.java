package game.worldsrv.achievement.typedata;

import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchievement;
import game.worldsrv.entity.Achievement;
import game.worldsrv.achievement.AchievementTypeKey;

/**
 * 主角达到{0}级
 */
public class AchievementType8Data extends AbstractAchievementTypeData {

	private static AchievementType8Data instance = new AchievementType8Data();

	public static AchievementType8Data getInstance() {
		return instance;
	}

	private AchievementType8Data() {
		super(AchievementTypeKey.ACHIEVEMENT_TYPE_8);
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
	 * 设置任务进度 玩家等级
	 */
	@Override
	public boolean doProgress(HumanObject humanObj, Achievement achieve, int progress) {
		if (isFinished(achieve)) {
			return false;
		}
		
		// 当前等级为进度
		achieve.setProgress(progress);
		// 检查任务完成状态
		checkProgressStatus(achieve);
		return true;
	}

}
