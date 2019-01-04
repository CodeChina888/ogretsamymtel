package game.worldsrv.achieveTitle.typedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.worldsrv.achieveTitle.AchieveTitleManager;
import game.worldsrv.achieveTitle.AchieveTitleTypeKey;
import game.worldsrv.achieveTitle.achieveTitleVO.AchieveTitleVO;
import game.worldsrv.achieveTitle.achieveTitleVO.TitleVO;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchieveTitle;

/**
 * 九层妖塔通过{0}难度第{1}层
 */
public class AchieveTitleType51Data extends AchieveTitleTypeData {

	private static AchieveTitleType51Data instance = new AchieveTitleType51Data();

	public static AchieveTitleType51Data getInstance() {
		return instance;
	}

	private AchieveTitleType51Data() {
		super(AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_51);
	}
	
	@Override
	public AchieveTitleVO init(long humanId, ConfAchieveTitle conf, Map<Integer, AchieveTitleVO> map) {
		// 初始化任务VO类
		AchieveTitleVO atVO = super.init(humanId, conf, map);
		return atVO;
	}

	/**
	 *  九层妖塔通过x难度第y层（进度值为y，校验参考值为x），并检查是否称号是否完成
	 * @param humanObj
	 * @param atVO 某个类型的称号成就
	 * @param progress 最新的进度 层数
	 * @param target 校验参考值 挑战难度
	 * @return Map<" Integer = Update_Title_DEF/Gain_Title_DEF, List<"AchieveTitle.sn"> ">
	 */
	@Override
	public Map<Integer, List<TitleVO>> doProgressAndCheckGain(HumanObject humanObj, AchieveTitleVO atVO, int progress, int target) {
				// 返回的Map
		Map<Integer, List<TitleVO>> retMap = new HashMap<>();
		
		// 获得的新的称号list<TitleVO>
		List<TitleVO> gainTitleList = null;
		// 需要更新的称号list<TitleVO>
		List<TitleVO> updateTitleList = null;
		
		// 称号sn
		int titleSn = 0;
		// 配置表
		ConfAchieveTitle conf = null;
		// 目标参考值
		int targetParam = 0;
		// 之前的进度
		int oldProgress = 0;
		
		// 该类型的所有称号成就list
		List<TitleVO> titleList = atVO.titleList;
		for (TitleVO to : titleList) {
			titleSn = to.sn;
			conf = ConfAchieveTitle.get(to.sn);
			if (conf == null) {
				continue;
			}
			// 目标参考值不同，不更新
			targetParam = conf.param[1];
			if (target != targetParam) {
				continue;
			}
			
			if (isFinished(humanObj, titleSn)) {
				continue;
			}
			
			oldProgress = to.progress;
			// 如果历史挑战层数比本次高，不更新
			if (oldProgress >= progress) {
				continue;
			}
			// 设置新的进度
			to.setProgress(progress);
			if (this.checkProgressStatus(to)) {
				gainTitleList = retMap.get(AchieveTitleManager.GAIN_TITLE_DEF);
				if (gainTitleList == null) {
					gainTitleList = new ArrayList<>();
					retMap.put(AchieveTitleManager.GAIN_TITLE_DEF, gainTitleList);
				}
				gainTitleList.add(to);
			} else {
				updateTitleList = retMap.get(AchieveTitleManager.UPDATE_TITLE_DEF);
				if (updateTitleList == null) {
					updateTitleList = new ArrayList<>();
					retMap.put(AchieveTitleManager.UPDATE_TITLE_DEF, updateTitleList);
				}
				updateTitleList.add(to);
			}
		}
		
		if (updateTitleList != null && !updateTitleList.isEmpty()) {
			atVO.modifyProgress();
		}
		if (gainTitleList != null && !gainTitleList.isEmpty()) {
			atVO.modifyStatusInfo();
		}
		
		// 返回可以需要更新或者获得的称号
		return retMap;
	}
}
