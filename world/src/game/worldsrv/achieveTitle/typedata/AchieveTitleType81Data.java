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
 * 累计参加{0}次洞天福地
 */
public class AchieveTitleType81Data extends AchieveTitleTypeData {

	private static AchieveTitleType81Data instance = new AchieveTitleType81Data();

	public static AchieveTitleType81Data getInstance() {
		return instance;
	}

	private AchieveTitleType81Data() {
		super(AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_81);
	}

	@Override
	public AchieveTitleVO init(long humanId, ConfAchieveTitle conf, Map<Integer, AchieveTitleVO> map) {
		// 初始化任务VO类
		AchieveTitleVO atVO = super.init(humanId, conf, map);
		return atVO;
	}

	/**
	 * 累计参加n次洞天福地，并检查是否称号是否完成
	 * @param humanObj
	 * @param atVO 某个类型的称号成就
	 * @param progress 进度+1
	 * @return Map<" Integer = Update_Title_DEF/Gain_Title_DEF, List<"AchieveTitle.sn"> ">
	 */
	@Override
	public Map<Integer, List<TitleVO>> doProgressAndCheckGain(HumanObject humanObj, AchieveTitleVO atVO, int progress) {
				// 返回的Map
		Map<Integer, List<TitleVO>> retMap = new HashMap<>();
		
		// 获得的新的称号list<TitleVO>
		List<TitleVO> gainTitleList = null;
		// 需要更新的称号list<TitleVO>
		List<TitleVO> updateTitleList = null;
		
		// 称号sn
		int titleSn = 0;
		
		// 该类型的所有称号成就list
		List<TitleVO> titleList = atVO.titleList;
		for (TitleVO to : titleList) {
			titleSn = to.sn;
			if (isFinished(humanObj, titleSn)) {
				continue;
			}
			
			// 次数累加
			to.setProgress(to.progress + progress);
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
