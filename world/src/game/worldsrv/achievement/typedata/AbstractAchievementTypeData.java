package game.worldsrv.achievement.typedata;

import java.util.List;

import core.Port;
import game.msg.Define.EAchievementStatus;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchievement;
import game.worldsrv.config.ConfAchievementType;
import game.worldsrv.entity.Achievement;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

/**
 * 任务抽象类型对象
 * 
 * @author Aivs.Gao
 */
public abstract class AbstractAchievementTypeData implements IAchievementTypeData {

	protected final int type;

	protected AbstractAchievementTypeData(int type) {
		this.type = type;
	}

	/**
	 * 初始化任务VO类，所有任务初始化都先执行它
	 * 
	 */
	public Achievement init(long humanId, ConfAchievement conf) {
		/* 初始化 */
		Achievement achieve = new Achievement();
		achieve.setId(Port.applyId());
		achieve.setHumanId(humanId);
		achieve.setAchieveSn(conf.sn);
		achieve.setAchieveType(conf.type);
		achieve.setAchieveLv(0);// 一开始成就等级为0
		achieve.setUniqueSn(conf.achievementSn[0]); // 成就类型唯一id

		ConfAchievementType confAT = ConfAchievementType.get(achieve.getUniqueSn());
		// 如果是条件是数组
		// 成就的判断条件读第二位
		if (confAT.param.length >= 2) {
			achieve.setTarget(confAT.param[1]);
		} else {
			achieve.setTarget(confAT.param[0]);
		}
		achieve.setProgress(0);
		achieve.setStatus(EAchievementStatus.AchievementDoing_VALUE);
		achieve.setUpdateTime(Port.getTime());

		// 设置奖励
		List<ProduceVo> resList = ProduceManager.inst().produceItem(confAT.reward);
		String json = ProduceManager.inst().produceToJson(resList);
		achieve.setAwardsJson(json);

		// 持久化
		achieve.persist();

		// 初始化完立即检查成就任务，以防sb策划 完成条件配0来给玩家送奖励
		checkProgressStatus(achieve);
		return achieve;
	}

	/**
	 * 设置任务进度 部分类型需重载此函数
	 * 
	 * @return 是否状态有变化
	 */
	public boolean doProgress(HumanObject humanObj, Achievement achieve, int progress) {

		if (isFinished(achieve)) {
			return false;
		}
		achieve.setProgress(progress);
		// 检查任务完成状态
		return checkProgressStatus(achieve);
	}

	/**
	 * 成就任务升档
	 */
	public boolean lvUp(HumanObject humanObj, Achievement achieve, ConfAchievement confAchieve) {
//		// 已经达到满级成就
//		if (achieve.getAchieveLv() >= confAchieve.param.length - 1) {
//			return false;
//		}
		
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
		achieve.setTarget(confAT.param[0]);
		// achieve.setProgress(0);//策划说进度继续累计
		achieve.setStatus(EAchievementStatus.AchievementDoing_VALUE);
		// 设置奖励
		List<ProduceVo> resList = ProduceManager.inst().produceItem(confAT.reward);
		String json = ProduceManager.inst().produceToJson(resList);
		achieve.setAwardsJson(json);

		// 升档后继续检查成就完成状态
		checkProgressStatus(achieve);
		return true;
	}

	/**
	 * 如果进度达到，直接完成 部分类型需重载此函数
	 * 
	 *
	 */
	protected boolean checkProgressStatus(Achievement achieve) {
		int lastStatus = achieve.getStatus();

		ConfAchievement conf = ConfAchievement.get(achieve.getAchieveSn());
		if (conf == null) {
			return false;
		}
		@SuppressWarnings("unused")
		int achieveLv = achieve.getAchieveLv();
		// 成就任务唯一表
		ConfAchievementType confAT = ConfAchievementType.get(achieve.getUniqueSn());
		// 成就任务 目标进度参数
		if(confAT == null){
			Log.table.error("checkProgressStatus  ConfAchievementType errorsn={}",achieve.getUniqueSn());
			return false;
		}
		int param = confAT.param[0];

		// 进度满足，且该任务状态不是完结状态
		if (achieve.getProgress() >= param && lastStatus != EAchievementStatus.AchievementFinished_VALUE) {
			achieve.setStatus(EAchievementStatus.AchievementCompleted_VALUE);
		}
		// 反回是否有变化
		return EAchievementStatus.AchievementCompleted_VALUE == achieve.getStatus();
	}

	/**
	 * 任务是否完结
	 * 
	 * @param achieve
	 * @return
	 */
	protected boolean isFinished(Achievement achieve) {

		if (achieve.getStatus() == EAchievementStatus.AchievementFinished_VALUE) {
			return true;
		}
		return false;
	}

}
