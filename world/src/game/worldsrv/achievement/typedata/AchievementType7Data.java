package game.worldsrv.achievement.typedata;

import game.msg.Define.EAchievementStatus;
import game.worldsrv.achievement.AchievementTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchievement;
import game.worldsrv.entity.Achievement;

/**
 * 竞技场达到{0}名
 */
public class AchievementType7Data extends AbstractAchievementTypeData {

	private static AchievementType7Data instance = new AchievementType7Data();

	public static AchievementType7Data getInstance() {
		return instance;
	}

	private AchievementType7Data() {
		super(AchievementTypeKey.ACHIEVEMENT_TYPE_7);
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
	 * 设置任务进度 该类型特殊处理
	 * 竞技场达到{0}名
	 */
	@Override
	public boolean doProgress(HumanObject humanObj, Achievement achieve, int progress) {

		if (isFinished(achieve)) {
			return false;
		}
		int lastProgress = achieve.getProgress();
		
		// 竞技场进度特殊处理:进度为当前名次，如果名次低于成就进度名次，则不更新
		// 且当前进度等于0
		if (progress <= achieve.getProgress() || achieve.getProgress() == 0) {
			achieve.setProgress(progress);
		}
		
		// 检查任务完成状态
		checkProgressStatus(achieve);
		return lastProgress != progress;
	}

	/**
	 * 如果进度达到，直接完成 该类型特殊处理
	 * 竞技场达到{0}名
	 *
	 */
	@Override
	protected boolean checkProgressStatus(Achievement achieve) {
		int lastStatus = achieve.getStatus();
		
		ConfAchievement conf = ConfAchievement.get(achieve.getAchieveSn());
		if (conf == null) {
			return false;
		}
//		int achieveLv = achieve.getAchieveLv();
//		// 成就任务 目标进度参数 (param == achieve.getTarget())
//		int param = Utils.strToIntArraySplit(conf.param[achieveLv])[0];

		// 正常的是小到大增长，这里是排名，由大到小
		if (achieve.getProgress() > 0 && achieve.getProgress() <= achieve.getTarget() && lastStatus != EAchievementStatus.AchievementFinished_VALUE) { 
			achieve.setStatus(EAchievementStatus.AchievementCompleted_VALUE);
		}
		// 反回是否有变化
		return EAchievementStatus.AchievementCompleted_VALUE == achieve.getStatus(); 
	}
}
