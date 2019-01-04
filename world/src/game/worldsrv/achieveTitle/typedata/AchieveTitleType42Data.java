package game.worldsrv.achieveTitle.typedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import game.msg.Define.EAchieveTitleStatus;
import game.worldsrv.achieveTitle.AchieveTitleManager;
import game.worldsrv.achieveTitle.AchieveTitleTypeKey;
import game.worldsrv.achieveTitle.achieveTitleVO.AchieveTitleVO;
import game.worldsrv.achieveTitle.achieveTitleVO.TitleVO;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchieveTitle;

/**
 * 封印之地伤害达到前{0}
 */
public class AchieveTitleType42Data extends AchieveTitleTypeData {

	private static AchieveTitleType42Data instance = new AchieveTitleType42Data();

	public static AchieveTitleType42Data getInstance() {
		return instance;
	}

	private AchieveTitleType42Data() {
		super(AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_42);
	}

	@Override
	public AchieveTitleVO init(long humanId, ConfAchieveTitle conf, Map<Integer, AchieveTitleVO> map) {
		// 初始化任务VO类
		AchieveTitleVO atVO = super.init(humanId, conf, map);
		return atVO;
	}
	
	/**
	 * 封印之地伤害排行达到前n名，并检查是否称号是否完成
	 * @param humanObj
	 * @param atVO 某个类型的称号成就
	 * @param progress 本次名次
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
		// 之前的进度
		int oldProgress = 0;
		
		// 该类型的所有称号成就list
		List<TitleVO> titleList = atVO.titleList;
		for (TitleVO to : titleList) {
			titleSn = to.sn;
			if (isFinished(humanObj, titleSn)) {
				continue;
			}
			
			// 之前的进度
			oldProgress = to.progress; 
			// 如果进度最好名次比本次进度名次靠前，则不更新
			if (oldProgress <= progress && progress != 0 && oldProgress != 0) {
				continue;
			}
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
	
	/**
	 * 如果进度达到，直接完成 该类型特殊处理
	 * 封印之地伤害排名前n
	 */
	@Override
	protected boolean checkProgressStatus(TitleVO to) {
		// 对应每个titleSn的配置表
		ConfAchieveTitle conf = null;
		conf = ConfAchieveTitle.get(to.sn);
		if (conf == null) {
			return false;
		}
		// 参数:进度条件
		int param = conf.param[0];
		
		if (to.progress > 0 && to.progress <= param && to.status == EAchieveTitleStatus.AchieveTitleDoing_VALUE) {
			to.setStatus(EAchieveTitleStatus.AchieveTitleFinished_VALUE);
			// 设置领取时间
			to.setGainTime(Port.getTime());
			// 设置到期时间为默认永久
			if (conf.limitTime == 0) {
				to.setLimitTime(conf.limitTime);
			} else {
				to.setLimitTime(Port.getTime() + conf.limitTime);
			}
			return true;
		}
		return false;
	}
}
