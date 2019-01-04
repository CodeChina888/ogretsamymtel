package game.worldsrv.instLootMap;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.Define.DVector2;
import game.msg.Define.ETeamType;
import game.msg.MsgInstLootMap.CSDailyLootMapRevival;
import game.msg.MsgInstLootMap.CSLeavePVPLootMapSignUp;
import game.msg.MsgInstLootMap.CSLootMapAttackMonster;
import game.msg.MsgInstLootMap.CSLootMapBackMap;
import game.msg.MsgInstLootMap.CSLootMapEnter;
import game.msg.MsgInstLootMap.CSLootMapEnterDoor;
import game.msg.MsgInstLootMap.CSLootMapGMTest;
import game.msg.MsgInstLootMap.CSLootMapGameEnter;
import game.msg.MsgInstLootMap.CSLootMapGameTime;
import game.msg.MsgInstLootMap.CSLootMapMove;
import game.msg.MsgInstLootMap.CSLootMapOpenFloor;
import game.msg.MsgInstLootMap.CSLootMapOut;
import game.msg.MsgInstLootMap.CSLootMapPkFight;
import game.msg.MsgInstLootMap.CSLootMapPkLeave;
import game.msg.MsgInstLootMap.CSLootMapPkEnd;
import game.msg.MsgInstLootMap.CSLootMapPlayMove;
import game.msg.MsgInstLootMap.CSLootMapReadyEnterDoor;
import game.msg.MsgInstLootMap.CSLootMapSingleEnd;
import game.msg.MsgInstLootMap.CSLootMapUseSkill;
import game.msg.MsgInstLootMap.CSPVELootMapSignUp;
import game.msg.MsgInstLootMap.CSPVPLootMapSignUp;
import game.msg.MsgInstLootMap.CSTriggerEvent;
import game.msg.MsgPk.CSPKHumanEnd;
import game.msg.MsgPk.CSPKHumanLeave;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.pk.PKManager;

public class InstLootMapMsgHandler {
	
	/**
	 * 抢夺本pvp报名
	 */
	@MsgReceiver(CSPVPLootMapSignUp.class)
	public void _msg_CSPVPLootMapSignUp(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSPVPLootMapSignUp msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSPVPLootMapSignUp(humanObj,msg.getActInstSn());
	}
	
	/**
	 * 抢夺本离开pvp报名
	 */
	@MsgReceiver(CSLeavePVPLootMapSignUp.class)
	public void _msg_CSLeavePVPLootMapSignUp(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		InstLootMapManager.inst()._msg_CSLeavePVPLootMapSignUp(humanObj);
	}
	
	/**
	 * 抢夺本pve报名
	 */
	@MsgReceiver(CSPVELootMapSignUp.class)
	public void _msg_CSPVELootMapSignUp(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSPVELootMapSignUp msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSPVELootMapSignUp(humanObj,msg.getActInstSn());
	}
	
	/**
	 * 准备进入游戏
	 * @param param
	 */
	@MsgReceiver(CSLootMapEnter.class)
	public void _msg_CSLootMapEnter(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSLootMapEnter msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapEnter(humanObj,msg.getMapType(),msg.getActInstSn()); 
	}
	
	/**
	 * 发起PK
	 */
	@MsgReceiver(CSLootMapPkFight.class)
	public void _msg_CSLootMapPkFight(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSLootMapPkFight msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapPkFight(humanObj, msg.getBeFightId());
	}
	
	/**
	 * 离开PK
	 */
	@MsgReceiver(CSLootMapPkLeave.class)
	public void _msg_CSLootMapPkLeave(MsgParam param) {
//		HumanObject humanObj = param.getHumanObject();
//		InstLootMapManager.inst()._msg_CSLootMapPkLeave(humanObj);
	}
	
	/**
	 * 结算PK
	 */
	@MsgReceiver(CSLootMapPkEnd.class)
	public void _msg_CSLootMapPkEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		SCTurnbasedFinish finishMsg = humanObj.crossFightFinishMsg;
		if(null == finishMsg) {
			return;
		}
		long winHumanId = finishMsg.getParam64(0);//胜利方id
		long loseHumanId = finishMsg.getParam64(1);//失败方id
		long triggerId = finishMsg.getParam64(2);//发起者id
		InstLootMapManager.inst()._msg_CSLootMapPkEnd(humanObj, winHumanId, loseHumanId, triggerId);
	}
	
	/**
	 * 正式进入请求数据
	 * @param param
	 */
	@MsgReceiver(CSLootMapGameEnter.class)
	public void _msg_CSLootMapGameEnter(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		//CSLootMapGameEnter msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapGameEnter(humanObj); 
	}
	
	/**
	 * 玩家是否播放跑动作
	 * @param param
	 */
	@MsgReceiver(CSLootMapPlayMove.class)
	public void _msg_CSLootMapPlayMove(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSLootMapPlayMove msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapPlayMove(humanObj,msg.getIsPlay());
	}
	
	
	/**
	 * 玩家移动
	 */
	@MsgReceiver(CSLootMapMove.class)
	public void _msg_CSLootMapMove(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSLootMapMove msg = param.getMsg();
		DVector2 v2 = msg.getPos();
		InstLootMapManager.inst()._msg_CSLootMapMove(humanObj,(int)v2.getX(),(int)v2.getY());
	}
	
	/**
	 * 触发事件
	 * @param param
	 */
	@MsgReceiver(CSTriggerEvent.class)
	public void _msg_CSTriggerEvent(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTriggerEvent msg = param.getMsg();
		DVector2 v2 = msg.getPos();
		InstLootMapManager.inst()._msg_CSTriggerEvent(humanObj,msg.getEventId(), (int)v2.getX(),(int)v2.getY());
	}
	
	/**
	 * 打开一个地砖
	 * @param param
	 */
	@MsgReceiver(CSLootMapOpenFloor.class)
	public void _msg_CSOpenFloor(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSLootMapOpenFloor msg = param.getMsg();
		DVector2 v2 = msg.getPos();
		InstLootMapManager.inst()._msg_CSOpenFloor(humanObj,(int)v2.getX(),(int)v2.getY());
	}
	
	/**
	 * 玩家攻击怪物
	 * @param param
	 */
	@MsgReceiver(CSLootMapAttackMonster.class)
	public void _msg_CSLootMapAttackMonster(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSLootMapAttackMonster msg = param.getMsg();
		DVector2 v2 = msg.getPos();
		InstLootMapManager.inst()._msg_CSLootMapAttackMonster(humanObj,msg.getEventId(), (int)v2.getX(),(int)v2.getY());
	}
	
	/**
	 * 使用技能
	 * @param param
	 */
	@MsgReceiver(CSLootMapUseSkill.class)
	public void _msg_CSLootMapUseSkill(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		InstLootMapManager.inst()._msg_CSLootMapUseSkill(humanObj);
	}
	
	/**
	 * 玩家复活
	 * @param param
	 */
	@MsgReceiver(CSDailyLootMapRevival.class)
	public void _msg_CSLootMapHumanRevival(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		InstLootMapManager.inst()._msg_CSDailyLootMapRevival(humanObj);
	}
	
	/**
	 * 退出 切换Stage
	 * @param param
	 */
	@MsgReceiver(CSLootMapOut.class)
	public  void _msg_CSLootMapOut(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		InstLootMapManager.inst()._msg_CSLootMapOut(humanObj);
	}
	
	/**
	 * 准备进入下一层
	 * @param param
	 */
	@MsgReceiver(CSLootMapReadyEnterDoor.class)
	public  void _msg_CSLootMapReadyEnterDoor(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSLootMapReadyEnterDoor msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapReadyEnterDoor(humanObj,msg.getEventId());
	}
	
	/**
	 * 确定进入下一层
	 * @param param
	 */
	@MsgReceiver(CSLootMapEnterDoor.class)
	public void _msg_CSLootMapEnterDoor(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSLootMapEnterDoor msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapEnterDoor(humanObj,msg.getEventId(),(int)msg.getPos().getX(),(int)msg.getPos().getY());
	}
	
	/**
	 * 请求单人结束
	 * @param param
	 */
	@MsgReceiver(CSLootMapSingleEnd.class)
	public void _msg_CSLootMapSingleEnd(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		//CSLootMapSingleEnd msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapSingleEnd(humanObj);
	}
	
	/**
	 * 多人玩法主动请求剩余时间
	 * @param param
	 */
	@MsgReceiver(CSLootMapGameTime.class)
	public void _msg_CSLootMapGameTime(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		//CSLootMapSingleEnd msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapGameTime(humanObj);
	}
	
	@MsgReceiver(CSLootMapBackMap.class)
	public void _msg_CSLootMapBackMap(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSLootMapBackMap msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapBackMap(humanObj,msg.getIsWin());
	}
	
	@MsgReceiver(CSLootMapGMTest.class)
	public void _msg_CSLootMapGMTest(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSLootMapGMTest msg = param.getMsg();
		InstLootMapManager.inst()._msg_CSLootMapGMTest(humanObj,msg.getGmType());
	}
}
