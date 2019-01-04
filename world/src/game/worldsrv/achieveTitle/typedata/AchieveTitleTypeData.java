package game.worldsrv.achieveTitle.typedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import game.msg.Define.EAchieveTitleStatus;
import game.worldsrv.achieveTitle.AchieveTitleManager;
import game.worldsrv.achieveTitle.achieveTitleVO.AchieveTitleVO;
import game.worldsrv.achieveTitle.achieveTitleVO.TitleVO;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchieveTitle;
import game.worldsrv.entity.AchieveTitle;

/**
 * 任务抽象类型对象
 * @author Neak
 */
public abstract class AchieveTitleTypeData implements IAchieveTitleTypeData {
	
	protected final int type;

	protected AchieveTitleTypeData(int type) {
		this.type = type;
	}

	/**
	 * 初始化任务VO类，所有任务初始化都先执行它
	 */
	public AchieveTitleVO init(long humanId, ConfAchieveTitle conf, Map<Integer, AchieveTitleVO> map) {
		AchieveTitleVO atVO = map.get(conf.type);
		if (atVO == null) {
			atVO = new AchieveTitleVO(humanId, conf.type);
		}
		// 新增title
		TitleVO to = atVO.createTitleVO(conf.sn);
		
		// 初始化完立即检查成就任务，以防sb策划 完成条件配0来给玩家送奖励
		checkProgressStatus(to);
		
		return atVO;
	}

	/**
	 * 设置任务进度，并且检查是否完成称号
	 * @param humanObj
	 * @param atVO 某个类型的称号成就
	 * @param progress 最新的进度
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
			if (oldProgress == progress) {
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
	
	/**
	 * 进度更改，并且检查是否完成称号
	 * @param humanObj
	 * @param atVO 该称号类型的VO
	 * @param progress 目标进度值
	 * @param target 校验进度的目标参考值
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
		// 之前的进度
		int oldProgress = 0;
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
			// 目标参考值不同，不进行处理
			targetParam = conf.param[1];
			if (target != targetParam) {
				continue;
			}
			
			if (isFinished(humanObj, titleSn)) {
				continue;
			}
			// 之前的进度
			oldProgress = to.progress;
			if (oldProgress == progress) {
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
		
		if (to.progress >= param && to.status == EAchieveTitleStatus.AchieveTitleDoing_VALUE) {
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

	/**
	 * 该称号成就是否完结
	 * @return true：已经完结 或者 数据异常
	 */
	protected boolean isFinished(HumanObject humanObj, int titleSn) {
		// 取的对应sn的配置
		ConfAchieveTitle conf = ConfAchieveTitle.get(titleSn);
		if (conf == null) {
			return true;
		}
		// 取的对应类型的数据
		AchieveTitleVO atVO = humanObj.achieveTitleMap.get(conf.type);
		if (atVO == null) {
			return true;
		}
		// 获取该titleSn的TitleVO
		TitleVO to = atVO.getTitleVO(titleSn);
		if (to == null) {
			return true;
		}
		// 如果状态不是进行中，则已完结
		if (to.status != EAchieveTitleStatus.AchieveTitleDoing_VALUE) {
			return true;
		}
		return false;
	}
	
	/**
	 * 检查称号状态
	 */
	@Override
	public boolean onCheck(HumanObject humanObj, AchieveTitle achieve) {
		return false;
	}
}
