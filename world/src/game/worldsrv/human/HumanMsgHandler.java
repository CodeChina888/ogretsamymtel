package game.worldsrv.human;

import java.util.Arrays;
import java.util.List;

import core.support.Param;
import core.support.observer.MsgReceiver;
import game.msg.Define.ELoginType;
import game.msg.MsgAccount.CSAccountBindInGame;
import game.msg.MsgActivity.CSActivityLvPackage;
import game.msg.MsgActivity.CSActivitySign;
import game.msg.MsgCommon.CSDailyActBuy;
import game.msg.MsgCommon.CSDailyCoinBuy;
import game.msg.MsgCommon.CSHumanInfo;
import game.msg.MsgCommon.SCHumanInfo;
import game.msg.MsgFight.CSReplayLeave;
import game.msg.MsgGuide.CSChangeGuideStatus;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.compete.CompeteServiceProxy;
import game.worldsrv.entity.Human;
import game.worldsrv.stage.types.StageObjectReplay;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

public class HumanMsgHandler {
	
	/**
	 * 绑定账号
	 */
	@MsgReceiver(CSAccountBindInGame.class)
	public void _msg_CSAccountBindInGame(MsgParam param) {
		CSAccountBindInGame msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		// 登录验证
		ELoginType loginType = msg.getLoginType();// 登录类型
		// 平台账号登录需要的参数
		String account = msg.getAccount();// 账号，即用户ID
		String password = msg.getPassword();// 密码，即访问口令（提供给服务器验证用的）
		HumanManager.inst().bindAccount(humanObj, loginType, account, password);
	}
	
	@MsgReceiver(CSHumanInfo.class)
	public void _msg_CSHumanInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompeteServiceProxy pro = CompeteServiceProxy.newInstance();
		pro.getTopRank(humanObj.id);
		pro.listenResult(this::_result_msg_CSHumanInfo, "humanObj", humanObj);
	}

	public void _result_msg_CSHumanInfo(Param result, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_msg_CSHumanInfo humanObj is null");
			return;
		}
		Human human = humanObj.getHuman();

		// 获得竞技场最高排名
		int rank = result.get();
		// 武将收集最多数量
		int genCount = 0;
		// String[] generalRequired = human.getAllGeneral().split(",");
		// if(generalRequired!= null && generalRequired.length > 0) {
		// genCount = generalRequired.length;
		// }
		SCHumanInfo.Builder msg = SCHumanInfo.newBuilder();
		msg.setCombat(human.getCombat());
		msg.setGeneralColCount(genCount);
		msg.setCompeteTopRank(rank);
		humanObj.sendMsg(msg);
	}

	/* 技能设置变动 */
	/*
	 * @MsgReceiver(CSSkillSet.class) public void onCSSkillSet(MsgParam param) {
	 * CSSkillSet msg = param.getMsg(); String skillSet = ""; List<Integer>
	 * listSkillSet1 = msg.getSkillSet1List(); HumanObject humanObj =
	 * param.getHumanObject(); Human human = humanObj.getHuman(); List<Integer>
	 * skills = Utils.strToIntList(human.getSkillSet()); int[] useSkill = new
	 * int[skills.size()]; //正在使用的技能 for(Integer id : listSkillSet1) { if(id ==
	 * null) { continue; } for (int i = 0; i < skills.size(); i++) {
	 * if(skills.get(i).equals(id) && skills.get(i) != 0){//获得没有改变的技能
	 * useSkill[i] = skills.get(i); break; } } if(skillSet.isEmpty()){ skillSet
	 * = String.valueOf(id); continue; } skillSet += ("," + id);//保存现在的技能 }
	 * boolean res = true; //下发卸下技能消息 for (int i = 0; i < skills.size(); i++) {
	 * if(useSkill[i] == 0 && skills.get(i) != 0 && useSkill[i] != skills.get(i)
	 * && res){//原有技能和正在用的技能对比 humanObj.sendSysMsg(62);//技能卸下了 只提示一次 res =
	 * false; break; } } //保存技能变化 human.setSkillSet(skillSet); }
	 */


	/**
	 * 技能升级
	 * @param param
	 */
//	@MsgReceiver(CSGeneralSkillUpdate.class)
//	public void _msg_CSGeneralSkillUpdate(MsgParam param) {
//		/*
//		 * 技能升级功能已经改了 这套没用 CSGeneralSkillUpdate msg = param.getMsg(); // 主角技能升级
//		 * HumanManager.inst()._msg_CSGeneralSkillUpdate(param.getHumanObject(),
//		 * msg.getSkillSn());
//		 */
//	}

	/**
	 * 客户端提交新手引导信息
	 * @param param
	 */
	@MsgReceiver(CSChangeGuideStatus.class)
	public void _msg_ChangeGuideStatus(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSChangeGuideStatus msg = param.getMsg();
		List<Integer> guideList = Arrays.asList(msg.getOrderIndex(), msg.getCurFunctionId(), msg.getFuncIndex());
		humanObj.getHuman().setGuideIds(Utils.intListToStr(guideList));
	}
	
	/**
	 * 用户每日签到 FIXME 需要根据模块调整代码块位置
	 * @param param
	 */
	@MsgReceiver(CSActivitySign.class)
	public void _msg_CSActivitySign(MsgParam param){
		HumanManager.inst()._msg_CSActivitySign(param.getHumanObject());
	}
	
	/**
	 * 等级礼包
	 * @param param
	 */
	@MsgReceiver(CSActivityLvPackage.class)
	public void _msg_CSActivityLvPackage(MsgParam param){
		CSActivityLvPackage msg = param.getMsg();
		int lvPackage = msg.getLv();
		HumanManager.inst()._msg_CSActivityLvPackage(param.getHumanObject(),lvPackage);
	}
		
	/**
	 * 玩家每日购买体力
	 * @param param
	 */
	@MsgReceiver(CSDailyActBuy.class)
	public void _msg_CSDailyActBuy(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		HumanManager.inst()._msg_CSDailyActBuy(humanObj);
	}
	/**
	 * 玩家每日购买铜币
	 * @param param
	 */
	@MsgReceiver(CSDailyCoinBuy.class)
	public void _msg_CSDailyCoinBuy(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSDailyCoinBuy msg = param.getMsg();
		HumanManager.inst()._msg_CSDailyCoinBuy(humanObj, msg.getNum());
	}
	
	/**
	 * 离开录像副本
	 * @param param
	 */
	@MsgReceiver(CSReplayLeave.class)
	public void _msg_CSReplayLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		if (humanObj.stageObj instanceof StageObjectReplay) {
			HumanManager.inst()._msg_CSReplayLeave(humanObj);
		} else {
			Log.human.error("===离开副本出错：不是录像副本！mapSn={},stageSn={}", humanObj.stageObj.mapSn,
					humanObj.stageObj.stageSn);
		}
	}
}
