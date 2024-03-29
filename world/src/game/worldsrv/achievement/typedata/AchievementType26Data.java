package game.worldsrv.achievement.typedata;

import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchievement;
import game.worldsrv.config.ConfAchievementType;
import game.worldsrv.entity.Achievement;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Utils;

import java.util.List;

import game.msg.Define.EAchievementStatus;
import game.worldsrv.achievement.AchievementTypeKey;

/**
 * 成就任务类型29 主角{0]个技能达到{1}阶
 */
public class AchievementType26Data extends AbstractAchievementTypeData {

	private static AchievementType26Data instance = new AchievementType26Data();

	public static AchievementType26Data getInstance() {
		return instance;
	}

	private AchievementType26Data() {
		super(AchievementTypeKey.ACHIEVEMENT_TYPE_26);
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
	 */
	@Override
	public boolean doProgress(HumanObject humanObj, Achievement achieve, int progress) {

		if (isFinished(achieve)) {
			return false;
		}
		int lastProgress = achieve.getProgress();
		if (progress != achieve.getProgress())
			achieve.setProgress(progress);
		// 检查任务完成状态
		checkProgressStatus(achieve);
		return lastProgress != progress;
	}

	/**
	 * 成就任务升档 多线任务
	 * 主角{0]个技能达到{1}阶
	 */
	@Override
	public boolean lvUp(HumanObject humanObj, Achievement achieve, ConfAchievement confAchieve) {
		List<Integer> achievementUniqueSns = Utils.intToIntegerList(confAchieve.achievementSn);
		int curIndex = achievementUniqueSns.indexOf(achieve.getUniqueSn());
		// 已经达到满级成就
		if (curIndex >= achievementUniqueSns.size() - 1) {
			return false;
		}

		// 更新数据
		achieve.setAchieveLv(achieve.getAchieveLv() + 1);
		// 更新当前的唯一sn
		achieve.setUniqueSn(confAchieve.achievementSn[curIndex + 1]);
		
		// 成就任务唯一表
		ConfAchievementType confAT = ConfAchievementType.get(achieve.getUniqueSn());
		// 判断条件
		int newTarget = confAT.param[1];
		int newProgress = humanObj.humanSkillRecord.getAmountBySkillStage(newTarget);
		achieve.setProgress(newProgress);
		achieve.setTarget(newTarget);
		achieve.setStatus(EAchievementStatus.AchievementDoing_VALUE);
		// 设置奖励
		List<ProduceVo> resList = ProduceManager.inst().produceItem(confAT.reward);
		String json = ProduceManager.inst().produceToJson(resList);
		achieve.setAwardsJson(json);

		// 升档后继续检查成就完成状态
		checkProgressStatus(achieve);
		return true;
	}

}
