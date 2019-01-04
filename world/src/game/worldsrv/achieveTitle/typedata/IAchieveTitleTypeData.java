package game.worldsrv.achieveTitle.typedata;

import java.util.List;
import java.util.Map;

import game.worldsrv.achieveTitle.achieveTitleVO.AchieveTitleVO;
import game.worldsrv.achieveTitle.achieveTitleVO.TitleVO;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchieveTitle;
import game.worldsrv.entity.AchieveTitle;

/**
 * 成就任务类型数据
 * @author Aivs.Gao
 */
public interface IAchieveTitleTypeData {
	
	/**
	 * 初始化一个Quest
	 * @param humanId
	 * @param conf
	 * @return
	 */
	public AchieveTitleVO init(long humanId, ConfAchieveTitle conf, Map<Integer, AchieveTitleVO> map);
	
	/**
	 * 进度更改，并且检查是否完成称号
	 * @param humanObj
	 * @param atVO
	 * @param progress 目标进度值
	 * @return 返回可以需要更新或者获得的称号
	 */
	public Map<Integer, List<TitleVO>> doProgressAndCheckGain(HumanObject humanObj, AchieveTitleVO atVO, int progress);
	
	/**
	 * 进度更改，并且检查是否完成称号
	 * @param humanObj
	 * @param atVO 该称号类型的VO
	 * @param progress 目标进度值
	 * @param target 校验进度的目标参考值
	 * @return 返回可以需要更新或者获得的称号
	 */
	public Map<Integer, List<TitleVO>> doProgressAndCheckGain(HumanObject humanObj, AchieveTitleVO atVO, int progress, int target);
	
	/**
	 * 检查任务是否完成
	 * @param humanObj
	 * @return
	 */
	public boolean onCheck(HumanObject humanObj, AchieveTitle achieve);
}
