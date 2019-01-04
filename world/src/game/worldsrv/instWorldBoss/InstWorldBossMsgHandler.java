package game.worldsrv.instWorldBoss;

import java.util.List;

import core.support.Utils;
import core.support.observer.MsgReceiver;
import game.msg.Define.ETeamType;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.msg.MsgWorldBoss.CSWorldBossEnter;
import game.msg.MsgWorldBoss.CSWorldBossLeave;
import game.msg.MsgWorldBoss.CSWorldBossEnterFight;
import game.msg.MsgWorldBoss.CSWorldBossLeaveFight;
import game.msg.MsgWorldBoss.CSWorldBossInstSn;
import game.msg.MsgWorldBoss.CSWorldBossInfo;
import game.msg.MsgWorldBoss.CSWorldBossRank;
import game.msg.MsgWorldBoss.CSWorldBossRankFinal;
import game.msg.MsgWorldBoss.CSWorldBossHarm;
import game.msg.MsgWorldBoss.CSWorldBossOtherHuman;
import game.msg.MsgWorldBoss.CSWorldBossRevive;
import game.msg.MsgWorldBoss.CSWorldBossUponTop;
import game.msg.MsgWorldBoss.CSWorldBossReborn;
import game.msg.MsgWorldBoss.CSWorldBossInspireCDClean;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.compete.CompeteManager;

/**
 * 单人活动副本
 * @author Administrator
 *
 */
public class InstWorldBossMsgHandler {
	
	/**
	 * 打开世界boss界面，获得上次伤害排行前三和击杀者昵称
	 */
	@MsgReceiver(CSWorldBossUponTop.class)
	public void _msg_CSWorldBossUponTop(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossUponTop msg = param.getMsg();
		int actInstSn = msg.getActInstSn();
		InstWorldBossManager.inst()._msg_CSWorldBossUponTop(humanObj, actInstSn);
	}
	
	/**
	 * 进入世界BOSS地图
	 * @param param
	 */
	@MsgReceiver(CSWorldBossEnter.class)
	public void _msg_CSWorldBossEnter(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossEnter msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossEnter(humanObj, msg.getActInstSn());
	}
	
	/**
	 * 离开世界BOSS地图
	 * @param param
	 */
	@MsgReceiver(CSWorldBossLeave.class)
	public void _msg_CSWorldBossLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		InstWorldBossManager.inst()._msg_CSWorldBossLeave(humanObj);
	}
	
	/**
	 * 进入世界BOSS战斗
	 * @param param
	 */
	@MsgReceiver(CSWorldBossEnterFight.class)
	public void _msg_CSWorldBossEnterFight(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossEnterFight msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossEnterFight(humanObj, msg.getActInstSn(), false);
	}
	
	/**
	 * 离开世界BOSS战斗
	 * @param param
	 */
	@MsgReceiver(CSWorldBossLeaveFight.class)
	public void _msg_CSWorldBossLeaveFight(MsgParam param) {
//		HumanObject humanObj = param.getHumanObject();
//		CSWorldBossLeaveFight msg = param.getMsg();
//		InstWorldBossManager.inst()._msg_CSWorldBossLeaveFight(humanObj, msg.getActInstSn());
	}
	
	/**
	 * 请求世界BOSS副本SN
	 * @param param
	 */
	@MsgReceiver(CSWorldBossInstSn.class)
	public void _msg_CSWorldBossInstSn(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		InstWorldBossManager.inst()._msg_CSWorldBossInstSn(humanObj);
	}
	
	/**
	 * 请求世界BOSS信息
	 * @param param
	 */
	@MsgReceiver(CSWorldBossInfo.class)
	public void _msg_CSWorldBossInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossInfo msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossInfo(humanObj, msg.getActInstSn());
	}
	
	/**
	 * 请求世界BOSS伤害排行前几名
	 * @param param
	 */
	@MsgReceiver(CSWorldBossRank.class)
	public void _msg_CSWorldBossRank(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossRank msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossRank(humanObj, msg.getActInstSn());
	}
	
	/**
	 * 请求最终世界BOSS伤害排行前几名
	 * @param param
	 */
	@MsgReceiver(CSWorldBossRankFinal.class)
	public void _msg_CSWorldBossRankFinal(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossRankFinal msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossRankFinal(humanObj, msg.getActInstSn());
	}
	
	/**
	 * 结算一次世界BOSS伤害
	 * @param param
	 */
	@MsgReceiver(CSWorldBossHarm.class)
	public void _msg_CSWorldBossHarm(MsgParam param) {
		//HumanObject humanObj = param.getHumanObject();
		//CSWorldBossHarm msg = param.getMsg();
		//InstWorldBossManager.inst()._msg_CSWorldBossHarm(humanObj, msg.getActInstSn(), msg.getHarmSelf());
		
		HumanObject humanObj = param.getHumanObject();
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;
		if(null == finishMsg) {
			return;
		}
		
		// 对每个位置造成的伤害
		List<Integer> harmList = finishMsg.getHarmList();
		long harmTotal = finishMsg.getParam64(0);// 输出总伤害
		int actInstSn = finishMsg.getParam32(0);// 活动副本SN
		boolean isWin = (finishMsg.getWinTeam() == ETeamType.Team1);//成功还是失败
		InstWorldBossManager.inst()._msg_CSWorldBossHarm(humanObj, actInstSn, harmTotal, harmList);
	}
	
	/**
	 * 请求世界BOSS立即复活
	 * @param param
	 */
	@MsgReceiver(CSWorldBossRevive.class)
	public void _msg_CSWorldBossRevive(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossRevive msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossRevive(humanObj, msg.getActInstSn());
	}
	
	/**
	 * 请求世界BOSS涅槃重生
	 * @param param
	 */
	@MsgReceiver(CSWorldBossReborn.class)
	public void _msg_CSWorldBossReborn(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossReborn msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossReborn(humanObj, msg.getActInstSn());
	}
	
	/**
	 * 请求世界BOSS清除鼓舞CD
	 * @param param
	 */
	@MsgReceiver(CSWorldBossInspireCDClean.class)
	public void _msg_CSWorldBossInspireCDClean(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossInspireCDClean msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossInspireCDClean(humanObj, msg.getActInstSn());
	}
	
	/**
	 * 请求世界BOSS其他玩家信息
	 * @param param
	 */
	@MsgReceiver(CSWorldBossOtherHuman.class)
	public void _msg_CSWorldBossOtherHuman(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSWorldBossOtherHuman msg = param.getMsg();
		InstWorldBossManager.inst()._msg_CSWorldBossOtherHuman(humanObj, msg.getActInstSn());
	}
}
