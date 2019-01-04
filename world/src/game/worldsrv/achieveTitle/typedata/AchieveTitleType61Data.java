package game.worldsrv.achieveTitle.typedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import game.msg.Define.EAchieveTitleStatus;
import game.msg.Define.ECastellanType;
import game.worldsrv.achieveTitle.AchieveTitleManager;
import game.worldsrv.achieveTitle.AchieveTitleTypeKey;
import game.worldsrv.achieveTitle.achieveTitleVO.AchieveTitleVO;
import game.worldsrv.achieveTitle.achieveTitleVO.TitleVO;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchieveTitle;
import game.worldsrv.config.ConfMainCityShow;
import game.worldsrv.support.Utils;

/**
 * 成为[某某玩法]城主
 */
public class AchieveTitleType61Data extends AchieveTitleTypeData {

	private static AchieveTitleType61Data instance = new AchieveTitleType61Data();

	public static AchieveTitleType61Data getInstance() {
		return instance;
	}

	private AchieveTitleType61Data() {
		super(AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_61);
	}

	@Override
	public AchieveTitleVO init(long humanId, ConfAchieveTitle conf, Map<Integer, AchieveTitleVO> map) {
		// 初始化任务VO类
		AchieveTitleVO atVO = super.init(humanId, conf, map);
		return atVO;
	}
	
	/**
	 * 设置玩家玩法为进度，并检查是否称号是否完成
	 * @param humanObj
	 * @param atVO 某个类型的称号成就
	 * @param progress 城主类型
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
		// 配置表
		ConfAchieveTitle conf = null;
		// 目标参考值
		int targetParam = 0;
		
		// 该类型的所有称号成就list
		List<TitleVO> titleList = atVO.titleList;
		for (TitleVO to : titleList) {
			titleSn = to.sn;
			conf = ConfAchieveTitle.get(to.sn);
			if (conf == null) {
				continue;
			}
			// 当前类型≠参考值的类型，不更新
			targetParam = conf.param[0];
			if (progress != targetParam) {
				continue;
			}
			if (isFinished(humanObj, titleSn)) {
				continue;
			}
			
			// 设置进度为城主类型
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
	 * 判断进度，如果完成直接变成已领取状态
	 * @param to TitilVO每个称号的详细信息
	 * @return true状态修改为已获得
	 */
	protected boolean checkProgressStatus(TitleVO to) {
		// 对应每个titleSn的配置表
		ConfAchieveTitle conf = ConfAchieveTitle.get(to.sn);
		if (conf == null) {
			return false;
		}
		// 参数:进度条件
		int param = conf.param[0];
		// 当前进度（代表类型） == 进度条件类型时，说明称号解锁
		if (to.progress == param && to.status == EAchieveTitleStatus.AchieveTitleDoing_VALUE) {
			to.setStatus(EAchieveTitleStatus.AchieveTitleFinished_VALUE);
			// 根据城主类型获得配置
			ConfMainCityShow confMainCity = ConfMainCityShow.get(param);
			// 90000 = 9:00:00
			int changeTime = 90000; 
			if (confMainCity != null) {
				changeTime = confMainCity.changeTime;
			}
			// 设置领取时间
			to.setGainTime(Port.getTime());
			
			if (param == ECastellanType.WorldBossDuke_VALUE) {
				// 当前小时 如果当前是19点，则  = 19 * 10000 = 190000 = 19:00:00 
				changeTime = Utils.getHourOfDay() * 10000;
				// 设置世界boss到期时间 = 当前小时时间 + 有效期
				to.setLimitTime(Utils.getTimeOfToday(changeTime) + conf.limitTime * Utils.L1000);
			} else {
				// 设置其他到期时间 = 更换时间 + 有效期 
				to.setLimitTime(Utils.getTimeOfToday(changeTime) + conf.limitTime * Utils.L1000);				
			}
			
			return true;
		}
		
		return false;
	}
}
