package game.worldsrv.character;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import core.InputStream;
import core.OutputStream;
import core.Port;
import core.PortPulseQueue;
import core.interfaces.ISerilizable;

import game.worldsrv.support.Vector2D;
import game.msg.Define.DStageObject;
import game.msg.MsgStage.SCStageObjectAppear;
import game.msg.MsgStage.SCStageObjectDisappear;
import game.worldsrv.enumType.AppearType;
import game.worldsrv.enumType.DisappearType;
import game.worldsrv.stage.StageCell;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;

/**
 * 地图单元基类
 */
public abstract class WorldObject implements ISerilizable {
	public StageObject stageObj; // 所属地图
	public StageCell stageCell; // 所属地图格

	public long id; // ID
	public String name = ""; // name
	public int modelSn; // 模型Sn
	public int sn; // 配置表sn
	public Vector2D posNow = new Vector2D(); // 坐标
	public Vector2D dirNow = new Vector2D(); // 朝向

	protected boolean inWorld = false; // 是否在地图上显示 本属性不应该被Distr同步

	public abstract DStageObject.Builder createMsg();

	public WorldObject fireObj; // 制造这个对象的源头
	private long tmCreate; // 产生时候的时间
	protected long tmCurr; // 当前的时候 使用timeCrete + pulse 的时间
	protected int tmDelta; // 增量时间

	public WorldObject(StageObject stageObj) {
		this.stageObj = stageObj;
	}

	public WorldObject() {
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(name);
		out.write(modelSn);
		out.write(sn);
		out.write(posNow);
		out.write(dirNow);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		name = in.read();
		modelSn = in.read();
		sn = in.read();
		posNow = in.read();
		dirNow = in.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", id).append("name", name)
				.toString();
	}

	public boolean isInWorld() {
		return inWorld;
	}

	public void pulse(int deltaTime) {
		tmCurr += deltaTime;
		this.tmDelta = deltaTime;
	}

	public void finallyPulse(int deltaTime){
		tmCurr += deltaTime;
		this.tmDelta = deltaTime;
	}
	
	public long getTime() {
		return tmCurr;
	}

	public long geDeltaTime() {
		return tmDelta;
	}

	public void startup() {
	}

	public boolean stageRegister(StageObject stageObj) {
		// 在地图中添加活动单元
		boolean ret = stageObj._addWorldObj(this);

		this.tmCreate = stageObj.getTime();
		this.tmCurr = this.tmCreate;
		return ret;
	}

	/**
	 * 在地图显示：正常出现
	 */
	public void stageShow() {
		// 已在地图中的 忽略
		if (inWorld) {
			Log.stageCommon.warn("使活动单元进入地图时发现inWorld状态为true：data={}", this);
			return;
		}

		// 设置状态为在地图中
		inWorld = true;

		// 调试日志
		if (Log.stageCommon.isDebugEnabled()) {
			Log.stageCommon.debug("地图单位进入地图: mapSn={}, stageId={}, objId={}, objSn={}, objName={}",
					stageObj.mapSn, stageObj.stageId, id, sn, name);
		}

		// 通知其他玩家 有地图单元进入视野
		StageManager.inst().sendMsgToArea(createMsgAppear(AppearType.Appear), stageObj, posNow);
	}

	/**
	 * 在地图显示：复活
	 */
	public void stageShowRevive() {
		// 已在地图中的 忽略
		// if (inWorld) {
		// Log.stageCommon.warn("使活动单元进入地图时发现inWorld状态为true：data={}", this);
		// return;
		// }

		// 设置状态为在地图中
		inWorld = true;

		// 日志
		if (Log.stageCommon.isInfoEnabled()) {
			Log.stageCommon.info("地图单位进入地图: stageId={}, objId={}, objName={}", stageObj.stageId, id, name);
		}

		// 通知其他玩家 有地图单元进入视野
		StageManager.inst().sendMsgToArea(createMsgAppear(AppearType.Revive), stageObj, posNow);
	}

	/**
	 * 进入地图
	 * @param stageObj
	 */
	public void stageEnter(StageObject stageObj) {
		// if(stageObj.getCell(posNow) == null) {
		// return;
		// }
		// 加入地图并显示
		stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "worldObj", this) {
			@Override
			public void execute(Port port) {
				WorldObject worldObj = param.get("worldObj");
				StageObject stageObj = param.get("stageObj");
				if (worldObj != null) {
					if (stageObj != null) {
						worldObj.stageRegister(stageObj);
					}
					worldObj.stageShow();
				}
			}
		});
	}

	public final void stageLeave() {
		// 设置状态
		inWorld = false;
		Log.stageCommon.info("===测试离开场景消失 stageLeave() name={},inWorld={}", this.name,inWorld);

		// 将具体删除操作排入队列 在心跳的最后在进行删除
		// 因为本心跳中可能还有后续操作需要本对象的实例
		if (stageObj == null) {
			return;
		}
		Log.stageCommon.info("===测试离开场景消失 stageLeave() mapSn={}, stageSn={}", stageObj.mapSn, stageObj.stageSn);
		stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "worldObj", this) {
			public void execute(Port port) {
				WorldObject worldObj = param.get("worldObj");
				StageObject stageObj = param.get("stageObj");
				if (worldObj != null && stageObj != null) {
					// 通知其他玩家 有地图单元离开视野
					StageManager.inst().sendMsgToArea(worldObj.createMsgDisappear(DisappearType.Disappear), stageObj, worldObj.posNow);
					stageObj._delWorldObj(worldObj);
				}
			}
		});
	}

	/**
	 * 从地图隐藏
	 */
	public void stageHide() {
		// 设置状态
		inWorld = false;
		if (Log.stageCommon.isDebugEnabled()) {
			Log.stageCommon.debug("===sjh测试 从地图隐藏消失 stageHide() name={},inWorld={}", this.name,inWorld);
		}

		// 通知其他玩家 有地图单元离开视野
		StageManager.inst().sendMsgToArea(createMsgDisappear(DisappearType.Disappear), stageObj, posNow);
	}

	/**
	 * 创建地图单元进入视野消息，表示自己出现在了别人视野
	 */
	public SCStageObjectAppear.Builder createMsgAppear(AppearType enumType) {
		SCStageObjectAppear.Builder msgAppear = SCStageObjectAppear.newBuilder();
		msgAppear.setObjAppear(createMsg());
		msgAppear.setType(enumType.value());
		System.out.println("===SCStageObjectAppear type="+enumType.value() + ",name="+this.name);
		return msgAppear;
	}

	/**
	 * 创建地图单元离开视野消息
	 */
	public SCStageObjectDisappear.Builder createMsgDisappear(DisappearType enumType) {
		SCStageObjectDisappear.Builder msgDisappear = SCStageObjectDisappear.newBuilder();
		msgDisappear.setObjId(id);
		msgDisappear.setType(enumType.value());
		// System.out.println("===SCStageObjectDisappear type="+type +
		// ",name="+this.name);
		return msgDisappear;
	}

}
