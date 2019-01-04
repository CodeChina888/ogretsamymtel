package game.worldsrv.achievement.typedata;

import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfAchievement;
import game.worldsrv.entity.Achievement;

/**
 * 成就任务类型数据
 * 
 * @author Aivs.Gao
 *
 */
public interface IAchievementTypeData {

	/**
	 * 初始化一个Quest
	 * @param humanId
	 * @param conf
	 * @return
	 */
	public Achievement init(long humanId, ConfAchievement conf);
	
	/**
	 * 推进任务
	 * @param humanObj
	 * @return
	 */
	public boolean doProgress(HumanObject humanObj, Achievement achieve, int progress);
	
	/**
	 * 成就任务升档
	 * @param humanObj
	 * @param achieve
	 * @param confAchieve
	 * @return 是否能升档
	 */
	public boolean lvUp(HumanObject humanObj,  Achievement achieve,  ConfAchievement confAchieve);
	
	/**
	 * 检查任务是否完成
	 * @param humanObj
	 * @return
	 */
	public boolean onCheck(HumanObject humanObj, Achievement achieve);
}
