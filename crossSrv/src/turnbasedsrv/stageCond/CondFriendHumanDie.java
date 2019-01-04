package turnbasedsrv.stageCond;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.support.Utils;
import game.msg.Define.ETeamType;
import turnbasedsrv.stage.FightStageObject;

public class CondFriendHumanDie extends StageCondBase {
	private boolean param;// 参数：是否死亡

	/**
	 * 构造函数
	 * 
	 * @param value
	 */
	public CondFriendHumanDie(int id, String value) {
		super(id);
		param = Utils.booleanValue(value);
	}

	/**
	 * 获取类型
	 * 
	 * @return
	 */
	@Override
	public String getType() {
		return StageCondDefine.FriendHumanDie;
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("param", param).toString();
	}

	/**
	 * 初始化
	 * 
	 * @param stageObj
	 */
	@Override
	public void init(FightStageObject stageObj) {
		// 增加地图结束检测行为配置
		stageObj.addStageFinishCheck(this);
	}
	
	/**
	 * 检测场景是否结束
	 * 
	 * @param stageObj
	 * @return
	 */
	@Override
	public boolean checkStageFinish(FightStageObject stageObj) {
		boolean isDie = stageObj.isDieTeamHuman(ETeamType.Team1);
		if (isDie == param) {
			return true;
		} else {
			return false;
		}
	}

}
