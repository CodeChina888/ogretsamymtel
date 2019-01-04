package game.worldsrv.tower;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.Define.ETeamType;
import game.msg.MsgTower.CSTowerBuyLife;
import game.msg.MsgTower.CSTowerEnd;
import game.msg.MsgTower.CSTowerEnter;
import game.msg.MsgTower.CSTowerGoAhead;
import game.msg.MsgTower.CSTowerLayerCount;
import game.msg.MsgTower.CSTowerLeave;
import game.msg.MsgTower.CSTowerModUnlock;
import game.msg.MsgTower.CSTowerMultipleAward;
import game.msg.MsgTower.CSTowerOpenCard;
import game.msg.MsgTower.CSTowerOpenRewardBox;
import game.msg.MsgTower.CSTowerResetConditon;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 
 * @author Neak
 *
 */
public class TowerMsgHandler {
	
	
	/**
	 * 初始化爬塔数据 
	 */
	@MsgReceiver(CSTowerModUnlock.class)
	public void _msg_CSTowerModUnlock(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst()._msg_CSTowerModUnlock(humanObj);
	}
	
	/**
	 * 爬塔挑战 
	 */
	@MsgReceiver(CSTowerEnter.class)
	public void _msg_CSTowerEnter(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTowerEnter msg = param.getMsg();
		// 要挑战的层数
		int fightLayer = msg.getFightLayer();
		// 选择的难度
		int selDiff = msg.getSelDifficulty();
		TowerManager.inst()._msg_CSTowerEnter(humanObj, fightLayer, selDiff);
	}
	
	/**
	 * 爬塔逃跑 
	 */
	@MsgReceiver(CSTowerLeave.class)
	public void _msg_CSTowerLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst()._msg_CSTowerLeave(humanObj);
	}
	
	/**
	 * 爬塔正常结算
	 */
	@MsgReceiver(CSTowerEnd.class)
	public void _msg_CSCSTowerEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;		
		if(null == finishMsg) {
			return;
		}
		List<Integer> stars = finishMsg.getStarList(); //通关获得星星数
		// 成功还是失败：胜利是队伍2则失败
		boolean isFail = (finishMsg.getWinTeam() == ETeamType.Team2);
		
		TowerManager.inst()._msg_CSTowerEnd(humanObj, stars, isFail);
	}
	
	/**
	 * 进入下一层
	 */
	@MsgReceiver(CSTowerGoAhead.class)
	public void _msg_CSTowerGoAhead(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTowerGoAhead msg = param.getMsg();
		// 当前层级
		int stayLayer = msg.getStayLayer();
		TowerManager.inst()._msg_CSTowerGoAhead(humanObj, stayLayer);
	}

	/**
	 * 开启爬塔宝箱
	 */
	@MsgReceiver(CSTowerOpenRewardBox.class)
	public void _msg_CSTowerOpenRewardBox(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTowerOpenRewardBox msg = param.getMsg();
		// 开启的宝箱所在层数
		int boxLayer = msg.getBoxLayer();
		TowerManager.inst()._msg_CSTowerOpenRewardBox(humanObj, boxLayer);
	}
	
	/**
	 * 宝箱翻牌 
	 */
	@MsgReceiver(CSTowerOpenCard.class)
	public void _msg_CSTowerOpenCard(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTowerOpenCard msg = param.getMsg();
		// 开启的宝箱所在层数
		int boxLayer = msg.getBoxLayer();
		// 翻开卡牌的位置
		int openIndex = msg.getOpenIndex();
		// 是否元宝翻牌
		boolean isCost = msg.getIsCost();
		TowerManager.inst()._msg_CSTowerOpenCard(humanObj, boxLayer, openIndex, isCost);
	}
	
	/**
	 * 购买爬塔生命
	 */
	@MsgReceiver(CSTowerBuyLife.class)
	public void _msg_CSTowerBuyLife(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst()._msg_CSTowerBuyLife(humanObj);
	}
	
	/**
	 * 购买结算多倍奖励
	 */
	@MsgReceiver(CSTowerMultipleAward.class)
	public void _msg_CSTowerMultipleAward(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst()._msg_CSTowerMultipleAward(humanObj);
	}
	
	/**
	 * 查看每一层的人数
	 */
	@MsgReceiver(CSTowerLayerCount.class)
	public void _msg_CSTowerLayerCount(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst()._msg_CSTowerLayerCount(humanObj);
	}
	
	/**
	 * 重置爬塔过关条件
	 */
	@MsgReceiver(CSTowerResetConditon.class) 
	public void _msg_CSTowerResetConditon(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst()._msg_CSTowerResetConditon(humanObj);
	}
	
}
