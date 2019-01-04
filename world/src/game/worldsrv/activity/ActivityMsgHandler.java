package game.worldsrv.activity;

import java.util.List;

import core.support.observer.MsgReceiver;
import game.seam.msg.MsgParam;
import game.worldsrv.activity.ActivityManager;
import game.worldsrv.character.HumanObject;
import game.msg.Define.DActivityParam;
import game.msg.MsgActivity.CSGetActivityInfo;
import game.msg.MsgActivitySeven.CSGetSevenLoginAward;
import game.msg.MsgActivity.CSActivityCommit;

public class ActivityMsgHandler {
	/**
	 * 获取活动列表
	 * @param param
	 */
	@MsgReceiver(CSGetActivityInfo.class)
	public void onCSActivityInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();

		CSGetActivityInfo msg = param.getMsg();
		boolean isAll = msg.getIsAll();
		if(isAll) {// 是否获取全部活动列表
			ActivityManager.inst().getAllShowActivityInfo(humanObj);
		} else {// 根据ID列表获取活动数据
			List<Integer> ids = msg.getIdList();
			ActivityManager.inst().getShowActivityInfo(humanObj, ids);
		}
	}
	
	/**
	 * 执行活动操作
	 * @param param
	 */
	@MsgReceiver(CSActivityCommit.class)
	public void onCSActivityExcuteZoneItem(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();

		CSActivityCommit msg = param.getMsg();
		int id = msg.getId();
		List<DActivityParam> activityParamsList = msg.getActivityParamsList();//获取协议操作参数
		// 根据活动id执行活动操作
		ActivityManager.inst().commitActivity(humanObj, id, activityParamsList);
	}
	
	/**
	 * 单另出来的七日登陆
	 */
	@MsgReceiver(CSGetSevenLoginAward.class)
	public void onCSGetSevenLoginAward(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();

		CSGetSevenLoginAward msg = param.getMsg();
		ActivityManager.inst().getSevenLoginAward(humanObj);
	}
}
