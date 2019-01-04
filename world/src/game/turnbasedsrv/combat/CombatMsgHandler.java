package game.turnbasedsrv.combat;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.google.protobuf.GeneratedMessage;

import core.CallPoint;
import core.support.observer.MsgParamBase;
import core.support.observer.MsgReceiver;
import game.msg.MsgIds;
import game.msg.MsgStage.CSStageEnter;
import game.msg.MsgTurnbasedFight.CSTurnbasedActionEnd;
import game.msg.MsgTurnbasedFight.CSTurnbasedAutoFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedCastSkill;
import game.msg.MsgTurnbasedFight.CSTurnbasedLeaveFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedMonsterChangeEnd;
import game.msg.MsgTurnbasedFight.CSTurnbasedQuickFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedRoundEnd;
import game.msg.MsgTurnbasedFight.CSTurnbasedSpeed;
import game.msg.MsgTurnbasedFight.CSTurnbasedStartFight;
import game.msg.MsgTurnbasedFight.CSTurnbasedStopFight;
import game.seam.account.AccountObject;
import game.seam.account.AccountService;
import game.seam.msg.MsgParam;
import game.seam.msg.MsgParamAccount;
import game.worldsrv.character.HumanObject;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.stage.StageManager;

public class CombatMsgHandler {
	// 下属监听消息
	public static final Set<Class<? extends GeneratedMessage>> methods = new HashSet<>();
	
	static {
		// 寻找本类监听的消息
		Method[] mths = CombatMsgHandler.class.getMethods();
		for (Method m : mths) {
			// 不是监听函数的忽略
			if (!m.isAnnotationPresent(MsgReceiver.class)) {
				continue;
			}

			// 记录
			MsgReceiver ann = m.getAnnotation(MsgReceiver.class);
			methods.add(ann.value()[0]);
		}
	}
	/**
	 * 转到正确的战斗地图
	 * @param param
	 * @return
	 */
	private boolean dispatchCombatMsg(int msgId,MsgParamBase param){
		if(param instanceof MsgParamAccount){//新手战斗未有角色
			MsgParamAccount accountMsg = (MsgParamAccount)param;
			CallPoint connPoint = accountMsg.getConnPoint();
			Long connId = accountMsg.getConnId();
			AccountService serv = accountMsg.getService();
			AccountObject accObj = serv.datas.get(connId);		
			if(accObj == null){
				return true;
			}
			if(accObj.getHumanId() == 0L){//未进入战斗
				return true;
			}
			GeneratedMessage msg = param.getMsg();
			StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
			proxy.dispatchCombatMsg(accObj.getHumanId(), connId, connPoint, msgId, msg);
			return true;
		}
		if(!(param instanceof MsgParam)){
			return true;
		}
		return false;
	}

	/**
	 * 玩家准备好 进入地图
	 * @param param
	 */
	@MsgReceiver(CSStageEnter.class)
	public void _msg_CSStageEnter(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSStageEnter,param)){
			return;
		}
		MsgParam msgParam  = (MsgParam)param;
		HumanObject humanObj = msgParam.getHumanObject();
		StageManager.inst()._msg_CSStageEnter(humanObj);
	}
	/**
	 * 修改变速倍率
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedSpeed.class)
	public void onCSTurnbasedSpeed(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedSpeed,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedSpeed((MsgParam)param);
	}

	/**
	 * 修改自动战斗
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedAutoFight.class)
	public void onCSTurnbasedAutoFight(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedAutoFight,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedAutoFight((MsgParam)param);
	}

	/**
	 * 开始战斗
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedStartFight.class)
	public void onCSTurnbasedStartFight(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedStartFight,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedStartFight((MsgParam)param);
	}

	/**
	 * 跳过战斗
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedQuickFight.class)
	public void onCSTurnbasedQuickFight(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedQuickFight,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedQuickFight((MsgParam)param);
	}

	/**
	 * 使用技能
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedCastSkill.class)
	public void onCSTurnbasedCastSkill(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedCastSkill,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedCastSkill((MsgParam)param);
	}

	/**
	 * 回合结束
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedRoundEnd.class)
	public void onCSTurnbasedRoundEnd(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedRoundEnd,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedRoundEnd((MsgParam)param);
	}

	/**
	 * 暂停
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedStopFight.class)
	public void onCSTurnbasedStopFight(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedStopFight,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedStopFight((MsgParam)param);
	}
	
	/**
	 * 离开
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedLeaveFight.class)
	public void onCSTurnbasedLeaveFight(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedLeaveFight,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedLeaveFight((MsgParam)param);
	}

	/**
	 * 剧情结束
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedActionEnd.class)
	public void onCSTurnbasedActionEnd(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedActionEnd,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedActionEnd((MsgParam)param);
	}
	/**
	 * 战斗者出入场表现结束
	 * 
	 * @param param
	 */
	@MsgReceiver(CSTurnbasedMonsterChangeEnd.class)
	public void onCSTurnbasedMonsterChangeEnd(MsgParamBase param) {
		if(dispatchCombatMsg(MsgIds.CSTurnbasedMonsterChangeEnd,param)){
			return;
		}
		CombatManager.inst().onCSTurnbasedMonsterChangeEnd((MsgParam)param);
	}
}