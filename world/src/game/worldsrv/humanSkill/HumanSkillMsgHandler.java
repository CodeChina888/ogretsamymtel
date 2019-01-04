package game.worldsrv.humanSkill;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.msg.Define.DSkill;
import game.msg.Define.DSkillGroup;
import game.msg.MsgSkill.CSGodsUnlockByItem;
import game.msg.MsgSkill.CSSelectSkillGods;
import game.msg.MsgSkill.CSSkillGodsLvUp;
import game.msg.MsgSkill.CSSkillGodsStarUp;
import game.msg.MsgSkill.CSSkillInstall;
import game.msg.MsgSkill.CSSkillLvUp;
import game.msg.MsgSkill.CSSkillResetTrain;
import game.msg.MsgSkill.CSSkillRunePractice;
import game.msg.MsgSkill.CSSkillRuneUnlock;
import game.msg.MsgSkill.CSSkillSaveTrain;
import game.msg.MsgSkill.CSSkillStageUp;
import game.msg.MsgSkill.CSSkillTrain;
import game.msg.MsgSkill.CSSkillTrainCheck;
import game.msg.MsgSkill.CSSkillTrainMutiple;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

/**
 * @author Neak
 * 2017.2.7 主角技能协议处理模块
 */
public class HumanSkillMsgHandler {
	/**
	 * 上阵技能变动
	 */
	@MsgReceiver(CSSkillInstall.class)
	public void _msg_CSSkillInstalll(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSkillInstall msg = param.getMsg();
		// 上阵技能
		DSkillGroup skillGroup = msg.getSkillGroup();
		HumanSkillManager.inst()._msg_CSSkillInstalll(humanObj, skillGroup);
	}
	
	/**
	 * 主角技能升级
	 */
	@MsgReceiver(CSSkillLvUp.class)
	public void _msg_CSSkillLvUp(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSSkillLvUp msg = param.getMsg();
		boolean bOneKey = msg.getBOneKey();
		List<DSkill> skillSnList = msg.getSkillSetList();
		HumanSkillManager.inst()._msg_CSSkillLvUp(humanObj, skillSnList, bOneKey);
	}
	
	/**
	 * 主角技能升阶
	 */
	@MsgReceiver(CSSkillStageUp.class)
	public void _msg_CSSkillStageUp(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSkillStageUp msg = param.getMsg();
		int skillTag = msg.getSkillTag();
		HumanSkillManager.inst()._msg_CSSkillStageUp(humanObj, skillTag);
	}
	
	/**
	 * 主角技能培养倍率
	 */
	@MsgReceiver(CSSkillTrainMutiple.class) 
	public void _msg_CSSkillTrainMutiple(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSkillTrainMutiple msg = param.getMsg();
		int mutiple = msg.getMutiple();
		HumanSkillManager.inst()._msg_CSSkillTrainMutiple(humanObj, mutiple);
	}
	
	/**
	 * 主角技能培养
	 */
	@MsgReceiver(CSSkillTrain.class)
	public void _msg_CSSkillTrain(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSSkillTrain msg = param.getMsg();
		boolean bOneKey = msg.getIsOnekey();
		HumanSkillManager.inst()._msg_CSSkillTrain(humanObj, bOneKey);
	}
	
	/**
	 * 重置技能培养
	 */
	@MsgReceiver(CSSkillResetTrain.class)
	public void _msg_CSSkillResetTrain(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSSkillResetTrain msg = param.getMsg();
		int resetIndex = msg.getResetIndex();
		HumanSkillManager.inst()._msg_CSSkillResetTrain(humanObj, resetIndex);
	}
	
	/**
	 * 保存技能培养结果
	 */
	@MsgReceiver(CSSkillSaveTrain.class)
	public void _msg_CSSkillSaveTrain(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		@SuppressWarnings("unused")
		CSSkillSaveTrain msg = param.getMsg();
		HumanSkillManager.inst()._msg_CSSkillSaveTrain(humanObj);
	}
	
	/**
	 * 检查是否有未处理的培养
	 */
	@MsgReceiver(CSSkillTrainCheck.class)
	public void _msg_CSSkillTrainCheck(MsgParam param){
//		HumanObject humanObj = param.getHumanObject();
//		CSSkillTrainCheck msg = param.getMsg();
//		HumanSkillManager.inst()._msg_CSSkillTrainCheck(humanObj);
	}
	
	/**
	 * 主角技能符文激活
	 */
	@MsgReceiver(CSSkillRuneUnlock.class)
	public void _msg_CSSkillRuneUnlock(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSSkillRuneUnlock msg = param.getMsg();
		int skillSn = msg.getSkillTag();
		int runeSn = msg.getRuneSn();
		HumanSkillManager.inst()._msg_CSSkillRuneUnlock(humanObj, skillSn, runeSn);
	}
	
	/**
	 * 主角技能符文洗练
	 */
	@MsgReceiver(CSSkillRunePractice.class)
	public void _msg_CSSkillRunePractice(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSSkillRunePractice msg = param.getMsg();
		int skillSn = msg.getSkillTag();
		int runeSn = msg.getRuneSn();
		HumanSkillManager.inst()._msg_CSSkillRunePractice(humanObj, skillSn, runeSn);
	}
	
	/**
	 * 主角爆点上阵
	 */
	@MsgReceiver(CSSkillGodsLvUp.class)
	public void _msg_CSSkillGodsLvUp(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSSkillGodsLvUp msg = param.getMsg();
		int godsSn = msg.getGodsTag();
		HumanSkillManager.inst()._msg_CSSkillGodsLvUp(humanObj, godsSn);
	}
	
	/**
	 * 主角爆点升星
	 */
	@MsgReceiver(CSSkillGodsStarUp.class)
	public void _msg_CSSkillGodsStarUp(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSkillGodsStarUp msg = param.getMsg();
		int godsSn = msg.getGodsTag();
		HumanSkillManager.inst()._msg_CSSkillGodsStarUp(humanObj, godsSn);
	}
	
	
	/**
	 * 主角爆点上阵
	 */
	@MsgReceiver(CSSelectSkillGods.class)
	public void _msg_CSSelectSkillGods(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSSelectSkillGods msg = param.getMsg();
		int godsSn = msg.getGodsTag();
		HumanSkillManager.inst()._msg_CSSelectSkillGods(humanObj, godsSn);
	}
	
	/**
	 * 主角爆点通过资源解锁
	 */
	@MsgReceiver(CSGodsUnlockByItem.class)
	public void _msg_CSGodsUnlockByItem(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSGodsUnlockByItem msg = param.getMsg();
		int godsSn = msg.getGodsTag();
		HumanSkillManager.inst()._msg_CSGodsUnlockByItem(humanObj, godsSn);
	}
	
}
