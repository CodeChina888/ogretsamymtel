package game.worldsrv.stage;

import core.support.observer.MsgReceiver;
import game.msg.MsgStage.CSStageEnter;
import game.msg.MsgStage.CSStageMove;
import game.msg.MsgStage.CSStageMoveStop;
import game.msg.MsgStage.CSStageSwitch;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.Vector3D;
import game.worldsrv.stage.StageManager;

public class StageMsgHandler {

//	/**
//	 * 玩家准备好 进入地图
//	 * @param param
//	 */
//	@MsgReceiver(CSStageEnter.class)
//	public void _msg_CSStageEnter(MsgParam param) {
//		HumanObject humanObj = param.getHumanObject();
//		StageManager.inst()._msg_CSStageEnter(humanObj);
//	}

	/**
	 * 切换地图
	 * @param param
	 */
	@MsgReceiver(CSStageSwitch.class)
	public void _msg_CSStageSwitch(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSStageSwitch msg = param.getMsg();
		String key = msg.getAreaSwitchKey();
		int index = key.indexOf(':');
		int mapSn = 0;
		long stageId = 0;
		if(index == -1) {
			mapSn = Utils.intValue(key);
		} else {
			mapSn = Utils.intValue(key.substring(0,index));
			stageId = Utils.longValue(key.substring(index+1));
		}
		StageManager.inst()._msg_CSStageSwitch(humanObj, mapSn, stageId);
	}
	
	/**
	 * 玩家移动
	 * @param param
	 */
	@MsgReceiver(CSStageMove.class)
	public void _msg_CSStageMove(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSStageMove msg = param.getMsg();
		long sendId = msg.getObjId();

		// 获得指定角色
		if (humanObj.stageObj == null) {
			return;
		}
		UnitObject unitObj = humanObj.stageObj.getUnitObj(sendId);
		// if(unitObj.teamBundleID != humanObj.teamBundleID) {
		// Log.common.info("onCSStageMove unitObj = null");
		// return;
		// }
		if (unitObj == null) {
			return;
		}

		if (unitObj.isHumanObj()) {
			// Log.common.info("onCSStageMove {} {} {}",
			// unitObj.posNow,msg.getPosBegin(), msg.getPosEndList());
		}
		Vector3D dirClient = new Vector3D();
		if (msg.getDir() != null) {
			dirClient = new Vector3D(msg.getDir());
		}
		unitObj.move(new Vector3D(msg.getPosBegin()), Vector3D.parseFrom(msg.getPosEndList()), dirClient);

		//Event.fire(EventKey.StageRepStart, humanObj);
	}

	/**
	 * 玩家移动停止
	 * @param param
	 * @return
	 */
	@MsgReceiver(CSStageMoveStop.class)
	public void _msg_CSStageMoveStop(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSStageMoveStop msg = param.getMsg();
		long sendId = msg.getSendId();
		Vector2D posEnd = new Vector2D(msg.getPosEnd());
		Vector2D dirEnd = new Vector2D(msg.getDirEnd());

		if (humanObj.stageObj == null) {
			return;
		}

		UnitObject unitObj = humanObj.stageObj.getUnitObj(sendId);
		// if(unitObj.teamBundleID != humanObj.teamBundleID) {
		// Log.common.info("CSStageMoveStop unitObj = null");
		// return;
		// }
		// if(!unitObj.isHumanObj()) {
		// Log.common.info("CSStageMoveStop !isHumanObj");
		// }
		if (unitObj == null) {
			return;
		}

		if (!(posEnd.x == 0 && posEnd.y == 0)) {
			// 允许误差范围：移动速度的1.5倍
			double offDistance = Double.MAX_VALUE;// (unitObj.getUnit().getSpeed()
													// / 100f) * 1.5;// * 0.2
			if (unitObj.posNow.distance(posEnd) < offDistance) {
				// 误差范围内，信任客户端位置
				unitObj.posNow = posEnd;
				unitObj.dirNow = dirEnd;
				// if(unitObj.isHumanObj()) {//sjh调试日志
				// Log.common.info("===误差范围内：CSStageMoveStop unitObj={},posNow={},posEnd={}, offDistance<{}",
				// unitObj.name, unitObj.posNow.toString(), posEnd.toString(),
				// offDistance);
				// }
			} else {
				// unitObj.posNow = posEnd;//lock by
				// shenjh,超过误差范围，认为玩家开挂了，不能信任客户端位置
				if (unitObj.isHumanObj()) {// sjh调试日志
					Log.stageMove.error("===超过误差范围：CSStageMoveStop unitObj={},posNow={},posEnd={}, offDistance>={}",
							unitObj.name, unitObj.posNow.toString(), posEnd.toString(), offDistance);
				}
			}
		} else {
			Log.stageMove.error("===跑到坐标0点去了？CSStageMoveStop unitObj={},posEnd={}", unitObj.name, posEnd);// sjh调试日志
		}

		unitObj.stop();// 停止
	}

//	/**
//	 * 请求删除假人，自动下发增加假人
//	 * @param param
//	 */
//	@MsgReceiver(CSDelDummy.class)
//	public void _msg_CSDelDummy(MsgParam param) {
//		HumanObject humanObj = param.getHumanObject();
//		CSDelDummy msg = param.getMsg();
//		long humanId = msg.getId();
//		StageManager.inst()._msg_CSDelDummy(humanObj, humanId);
//	}

}