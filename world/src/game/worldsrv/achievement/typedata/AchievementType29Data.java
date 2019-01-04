package game.worldsrv.achievement.typedata;

import java.util.List;

import game.msg.Define.EAchievementStatus;
import game.worldsrv.achievement.AchievementTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchievement;
import game.worldsrv.config.ConfAchievementType;
import game.worldsrv.entity.Achievement;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Utils;
import game.worldsrv.tower.TowerManager;

/**
 * 炼妖塔通关{0}难度第{1}关
 */
public class AchievementType29Data extends AbstractAchievementTypeData {

	private static AchievementType29Data instance = new AchievementType29Data();

	public static AchievementType29Data getInstance() {
		return instance;
	}

	private AchievementType29Data() {
		super(AchievementTypeKey.ACHIEVEMENT_TYPE_29);
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
	 * 炼妖塔通关{0}难度第{1}关
	 */
	@Override
	public boolean doProgress(HumanObject humanObj, Achievement achieve, int progress) {

		if (isFinished(achieve)) {
			return false;
		}
		// 副本特殊处理：progress为当前的记录难度层数，achieve.getTarget为目标难度层数
		// 当前进度sn和目标sn不等则return
		if (progress != achieve.getTarget())
			return false;
		
		achieve.setProgress(progress);
		// 检查任务完成状态
		return checkProgressStatus(achieve);
	}

	/**
	 * 成就任务升档
	 * 炼妖塔通关{0}难度第{1}关
	 */
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
		int newTarget = confAT.param[0];
		if (TowerManager.inst().getAlreadyFight(humanObj, newTarget)) {
			achieve.setProgress(newTarget);
		}
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
