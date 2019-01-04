package game.worldsrv.immortalCave;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.Define.ETeamType;
import game.msg.Define.ETokenType;
import game.msg.MsgCave.CSCaveBuyToken;
import game.msg.MsgCave.CSCaveCDTimeAdd;
import game.msg.MsgCave.CSCaveDefense;
import game.msg.MsgCave.CSCaveEnemy;
import game.msg.MsgCave.CSCaveEnemyInfo;
import game.msg.MsgCave.CSCaveFightEnd;
import game.msg.MsgCave.CSCaveFightLeave;
import game.msg.MsgCave.CSCaveGiveUp;
import game.msg.MsgCave.CSCaveGuildMemberInfo;
import game.msg.MsgCave.CSCaveInfo;
import game.msg.MsgCave.CSCaveMoneyInfo;
import game.msg.MsgCave.CSCaveOccupyInfo;
import game.msg.MsgCave.CSGetFreeCave;
import game.msg.MsgCave.CSMyCaveInfo;
import game.msg.MsgCave.CSOccupyBattle;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * 仙域
 * @author songy
 *
 */
public class CaveMsgHandler {

	/*仙域列表信息*/
	@MsgReceiver(CSCaveInfo.class)
	public void onCSCaveInfo(MsgParam param) {
		CSCaveInfo msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int type = msg.getCaveType().getNumber();
		List<Integer> pageList = msg.getPageListList();
		CaveManager.inst().getCSCaveInfo(humanObj,type,pageList);
	}
	
	/*获取被占领情况 */
	@MsgReceiver(CSCaveOccupyInfo.class)
	public void onCSCaveOccupyInfo(MsgParam param) {
		CSCaveOccupyInfo msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int type = msg.getCaveType().getNumber();
		int page = msg.getPage();
		int index = msg.getIndex();
		long humanID = msg.getHumanId();//占领者ID
		CaveManager.inst().CSCaveOccupyInfo(humanObj,type,page,index,humanID);
		
	}
	
	/*占领仙府,发起挑战*/
	@MsgReceiver(CSOccupyBattle.class)
	public void _msg_CSCompeteFight(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSOccupyBattle msg = param.getMsg();
		int type = msg.getCaveType().getNumber();
		int page = msg.getPage();
		int index = msg.getIndex();
		ETokenType etype = msg.getType();
		// 进入挑战
		CaveManager.inst().occupyBattleFight(humanObj, type, page,index,etype);
	}
	
	/**
	 * 请求离开仙府(逃跑)
	 * @param param
	 */
	@MsgReceiver(CSCaveFightLeave.class)
	public void _msg_CSCompeteLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CaveManager.inst()._msg_CSCaveFightLeave(humanObj);
	}
	
	
	/**
	 * 仙府战斗结束
	 * @param param
	 */
	@MsgReceiver(CSCaveFightEnd.class)
	public void _msg_CSCaveFightEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;
		if(null == finishMsg) {
			return;
		}
		CSCaveFightEnd msg = param.getMsg();
		int type = msg.getCaveType().getNumber();
		int page = msg.getPage();
		int index = msg.getIndex();
		boolean isWin = (finishMsg.getWinTeam() == ETeamType.Team1);//成功还是失败
		ETokenType battleType = msg.getType();
		CaveManager.inst()._msg_CSCaveEnd(humanObj,battleType,isWin,type , page,index);
	}
	
	/**
	 * 请求购买次数
	 */
	@MsgReceiver(CSCaveMoneyInfo.class)
	public void onCSCaveMoneyInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CaveManager.inst().sendCaveMoneyInfo(humanObj);
	}
	
	 /**
	  * 购买开采令/强夺令
	  */
	@MsgReceiver(CSCaveBuyToken.class)
	public void onCSCaveBuyToken(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSCaveBuyToken msg = param.getMsg();
		ETokenType type = msg.getType();
		CaveManager.inst().buyToken(humanObj,type);
		
	}
	/*我的洞府信息*/
	@MsgReceiver(CSMyCaveInfo.class)
	public void onCSMyCaveInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CaveManager.inst().getMyCaveInfo(humanObj);
	}
	
	/*放弃仙府*/
	@MsgReceiver(CSCaveGiveUp.class)
	public void onCSCaveGiveUp(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSCaveGiveUp msg = param.getMsg();
		int type = msg.getCaveType().getNumber();
		int page = msg.getPage();
		int index = msg.getIndex();
		CaveManager.inst().giveUp(humanObj,type,page,index);
	}
	/*延长时间*/
	@MsgReceiver(CSCaveCDTimeAdd.class)
	public void onCSCaveCDTimeAdd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSCaveCDTimeAdd msg = param.getMsg();
		int type = msg.getCaveType().getNumber();
		int page = msg.getPage();
		int index = msg.getIndex();
		CaveManager.inst().addTime(humanObj,type,page,index);
	}
	
	/*请求星级仙府列表*/
	@MsgReceiver(CSGetFreeCave.class)
	public void onCSGetFreeCave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGetFreeCave msg = param.getMsg();
		int type = msg.getCaveType().getNumber();
		int sn = msg.getSn();
		CaveManager.inst().getFreeCave(humanObj,type,sn);
	}
	
	/*请求防守记录*/
	@MsgReceiver(CSCaveDefense.class)
	public void onCSCaveDefense(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CaveManager.inst().getCaveLogMsg(humanObj);
	}
	
	@MsgReceiver(CSCaveEnemyInfo.class)
	public void onCSCaveEnemyInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSCaveEnemyInfo msg = param.getMsg();
		CaveManager.inst().getEnemyInfo(humanObj,msg.getEnemyHumanID());
	}
	
	/*请求仇人[从防守记录中获取仇人信息]*/
	@MsgReceiver(CSCaveEnemy.class)
	public void onCSCaveEnemy(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSCaveEnemy msg = param.getMsg();
		CaveManager.inst().getEnemyMsg(humanObj);
	}
	
	
	//获取我的盟友的仙府信息
	@MsgReceiver(CSCaveGuildMemberInfo.class)
	public void onCSCaveGuildMemberInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CaveManager.inst().getGuildMemberInfo(humanObj);
	}
	
	/*秘法研究*/
	
}
