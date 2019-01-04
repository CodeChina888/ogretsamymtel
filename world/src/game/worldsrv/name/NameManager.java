package game.worldsrv.name;

import org.apache.commons.lang3.StringUtils;

import core.support.ManagerBase;
import core.support.Param;
import core.support.RandomUtils;
import game.msg.Define.EMoneyType;
import game.msg.MsgCommon.SCStageObjectInfoChange;
import game.msg.MsgName.SCChangeNameRandomResult;
import game.msg.MsgName.SCChangeNameResult;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageManager;
import game.worldsrv.support.AssetsTxtFix;
import game.worldsrv.support.Log;
import game.worldsrv.support.SensitiveWordFilter;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class NameManager extends ManagerBase {

	/**
	 * o 获取实例
	 * @return
	 */
	public static NameManager inst() {
		// 加载敏感词库
		SensitiveWordFilter filterSW = SensitiveWordFilter.getInstance();
		Log.human.info("===加载敏感词库：allSize={}", filterSW.getSizeOfSensitiveWord());

		return inst(NameManager.class);
	}

	/**
	 * 改名字， 任务里改名字不花费，其余的要花费
	 * @param humanObj
	 * @param name
	 */
	public void _msg_CSChangeName(HumanObject humanObj, String name) {
		// 名字不能为空
		if (StringUtils.isEmpty(name)) {
			// 发送文字提示消息 名字不能为空
			humanObj.sendSysMsg(50);
			return;
		}
		int length = ParamManager.maxHumanNameLength;
		if (name.length() > length || name.length() < 2) {
			// 发送文字提示消息 名字长度不能超过{}8 或小于2
			humanObj.sendSysMsg(51, "B", length, "B1", 2);
			return;
		}

		// 检查是否存在非法的特殊字符
		if (!AssetsTxtFix.checkContent(name, length)) {
			humanObj.sendSysMsg(22);// 输入的文本中存在非法字符！请重新输入！
			return;
		}
				
		// 检查名字是否有屏蔽字，如果有屏蔽字返回
		String fix = SensitiveWordFilter.getInstance().getSensitiveWord(name.toLowerCase());
		if (fix != null) {
			// 返回消息
			SCChangeNameResult.Builder msg = SCChangeNameResult.newBuilder();
			msg.setResult(false);
			msg.setShield(fix);
			humanObj.sendMsg(msg);
			return;
		}

		// 判断名字是否重复
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.isRepeatName(name);
		prx.listenResult(this::_result_getHumanNameRepeat, "humanObj", humanObj, "name", name);
	}

	public void _result_getHumanNameRepeat(Param results, Param context) {
		// 上下文环境
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		String name = Utils.getParamValue(context, "name", "");
		int costGold = ParamManager.changeNameCostGold;  // 改成读取costGold表
		if (humanObj == null || name.isEmpty() || costGold < 0) {
			Log.game.error("===_result_getHumanNameRepeat humanObj={}, name={}, costGold={}", humanObj, name, costGold);
			return;
		}
		boolean repeat = Utils.getParamValue(results, "repeat", true);
		// 查询结果
		if (repeat) {
			// 发送文字提示消息 名字重复
			humanObj.sendSysMsg(2);
			return;
		}

		// 扣钱
		if (costGold > 0) {
			// 扣元宝
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGold, LogSysModType.HumanRename)) {
				return;
			}
		}
		// 修改名字
		Human human = humanObj.getHuman();
		String oldName = human.getName();
		human.setName(name);
		int renameNum = human.getRenameNum() + 1;// 每次改名都记录改名几次
		human.setRenameNum(renameNum);

		// 同步名字服务
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.changeName(oldName, name);
		// 派发事件
		Event.fire(EventKey.HumanNameChange, "humanObj", humanObj);

		// 返回消息
		SCChangeNameResult.Builder msg = SCChangeNameResult.newBuilder();
		msg.setResult(true);
		msg.setRenameNum(renameNum);
		msg.setName(name);
		humanObj.sendMsg(msg);
		// 给附近玩家发送变化信息
		SCStageObjectInfoChange.Builder msgPutOn = SCStageObjectInfoChange.newBuilder();
		msgPutOn.setObj(humanObj.createMsg());
		StageManager.inst().sendMsgToArea(msgPutOn, humanObj.stageObj, humanObj.posNow);
	}

	/**
	 * 随机获取名字
	 * @return
	 */
	public void _msg_CSChangeNameRandom(HumanObject humanObj) {
		//boolean isFemale = HumanManager.inst().isHumanFemale(humanObj.getHuman().getModelSn());
		boolean isFemale = (RandomUtils.nextInt(2) == 1 ? true : false);// 随机性别
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.getRandomName(isFemale);
		prx.listenResult(this::_result_randomNameRepeat, "humanObj", humanObj);
	}

	public void _result_randomNameRepeat(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		String name = Utils.getParamValue(results, "randomName", "");
		if (humanObj == null || name.isEmpty()) {
			Log.game.error("===_result_randomNameRepeat humanObj={}, name={}", humanObj, name);
			return;
		}
		SCChangeNameRandomResult.Builder msg = SCChangeNameRandomResult.newBuilder();
		msg.setName(name);
		humanObj.sendMsg(msg);
	}

	/**
	 * 改名字，只允许一次 改完名后不能再改名
	 * @param humanObj
	 * @param name
	 */
	public void changeNameOnce(HumanObject humanObj, String name) {
		Human human = humanObj.getHuman();
		boolean ret = human.isQuestNameChangePassed();
		if (ret) {// true 改过名了 false 未改过名
			humanObj.sendSysMsg(230102);// 你已经改过名了
			return;
		}
		// 名字不能为空
		if (StringUtils.isEmpty(name)) {
			// 发送文字提示消息 名字不能为空
			humanObj.sendSysMsg(50);
			return;
		}
		int length = ParamManager.maxHumanNameLength;
		if (name.length() > length || name.length() < 2) {
			// 发送文字提示消息 名字长度不能超过{}8 或小于2
			humanObj.sendSysMsg(51, "B", length, "B1", 2);
			return;
		}
		
		// 是否存在非法的特殊字符
		if (!AssetsTxtFix.checkContent(name, length)) {
			humanObj.sendSysMsg(22);// 输入的文本中存在非法字符！请重新输入！
			return;
		}

		// 检查名字是否有屏蔽字，如果有屏蔽字返回
		// String fix = NameFix.shield(name);
		String fix = SensitiveWordFilter.getInstance().getSensitiveWord(name.toLowerCase());
		if (fix != null) {
			// 返回消息
			SCChangeNameResult.Builder msg = SCChangeNameResult.newBuilder();
			msg.setResult(false);
			msg.setShield(fix);
			humanObj.sendMsg(msg);
			return;
		}

		// 判断名字是否重复
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.isRepeatName(name);
		prx.listenResult(this::_result_getHumanNameRepeatOne, "humanObj", humanObj, "name", name);
	}

	public void _result_getHumanNameRepeatOne(Param results, Param context) {
		// 上下文环境
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		String name = Utils.getParamValue(context, "name", "");
		if (humanObj == null || name.isEmpty()) {
			Log.game.error("===_result_getHumanNameRepeatOne humanObj={}, name={}", humanObj, name);
			return;
		}
		boolean repeat = Utils.getParamValue(results, "repeat", true);
		// 查询结果
		if (repeat) {
			// 发送文字提示消息 名字重复
			humanObj.sendSysMsg(2);
			return;
		}

		// 修改名字
		Human human = humanObj.getHuman();
		String oldName = human.getName();
		human.setName(name);

		// 改名成功
		humanObj.getHuman().setQuestNameChangePassed(true);

		// 同步名字服务
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.changeName(oldName, name);
		// 派发事件
		Event.fire(EventKey.HumanNameChange, "humanObj", humanObj);

		// 返回消息
		SCChangeNameResult.Builder msg = SCChangeNameResult.newBuilder();
		msg.setResult(true);
		msg.setName(name);
		humanObj.sendMsg(msg);
	}
	
}
