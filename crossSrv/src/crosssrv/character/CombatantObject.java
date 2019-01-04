package crosssrv.character;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

import core.CallPoint;
import core.Chunk;
import core.InputStream;
import core.OutputStream;
import core.Port;
import core.PortPulseQueue;
import core.connsrv.ConnectionProxy;
import core.interfaces.ISerilizable;
import core.support.Time;
import crosssrv.seam.CrossPort;
import crosssrv.stage.CrossStageObject;
import game.msg.Define.ETeamType;
import game.msg.MsgIds;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class CombatantObject implements ISerilizable {
	public CrossStageObject stageObj; // 所属地图
	public HumanMirrorObject humanMirrorObj; // 玩家角色信息
	public long id; // 玩家ID

	protected boolean inWorld = false; // 是否在地图上显示 本属性不应该被Distr同步
	// 客户端地图状态已准备完毕
	public boolean isClientStageReady;
	// 链接关闭时间
	protected long closeTime = 0l;

	// 下线延时
	public static int CLOSE_DELAY = 10;
	// 连接点信息
	public CallPoint connPoint = new CallPoint();

	// 所属战队
	public ETeamType team;

	/**
	 * ISerilizable类型必需的构造函数
	 */
	public CombatantObject() {

	}

	/**
	 * 连接断开时清理
	 */
	public void clearCloseStatus() {
	}

	/**
	 * 获取所在房间port
	 * 
	 * @return
	 */
	public CrossPort getPort() {
		return stageObj.getPort();
	}

	/**
	 * 离开房间
	 */
	public final void stageLeave() {
		// 设置状态
		inWorld = false;

		// 将具体删除操作排入队列 在心跳的最后在进行删除
		// 因为本心跳中可能还有后续操作需要本对象的实例
		if (stageObj == null) {
			return;
		}
		stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "combatantObj", this) {
			public void execute(Port port) {
				CrossStageObject stageObj = param.get("stageObj");
				CombatantObject combatantObj = param.get("combatantObj");

				stageObj.delCombatantObj(combatantObj);

				// 发送消息 通知客户端
			}
		});
	}

	/**
	 * 序列化写入
	 */
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(connPoint);
		out.write(id);
		out.write(humanMirrorObj);
	}

	/**
	 * 序列化读取
	 */
	@Override
	public void readFrom(InputStream in) throws IOException {
		connPoint = in.read();
		id = in.read();
		humanMirrorObj = in.read();

	}

	/**
	 * 玩家下线时进行清理
	 * 
	 * @param humanMirrorObj
	 */
	public void connCloseClear() {
		// //发布退出事件
		Event.fireEx(EventKey.CombatantLogout, stageObj.mapSn, "humanObj", this);

		// 清理
		// CombatantGlobalServiceProxy hgsprx =
		// CombatantGlobalServiceProxy.newInstance();
		// hgsprx.cancel(id);
	}

	public void connDelayCloseClear() {
		// 处理延迟关于 启动关闭后XX 秒才会彻底清除玩家数据
		closeTime = Port.getTime() + CLOSE_DELAY * Time.SEC;
	}

	/**
	 * 获取镜像数据
	 * 
	 * @return
	 */
	public HumanMirrorObject getHumanMirrorObj() {
		return humanMirrorObj;
	}

	/**
	 * 发送消息至玩家
	 * 
	 * @param builder
	 */
	public void sendMsg(Builder builder) {
		if (builder == null)
			return;

		// 发送单条消息
		sendMsg(builder.build());
	}

	/**
	 * 发送消息至玩家，发送多条消息
	 * 
	 * @param builder
	 */
	public void sendMsg(List<Builder> builders) {
		if (builders == null || builders.size() <= 0)
			return;

		List<Integer> idList = new ArrayList<Integer>();
		List<Chunk> chunkList = new ArrayList<Chunk>();
		for (Builder builder : builders) {
			Message msg = builder.build();
			idList.add(MsgIds.getIdByClass(msg.getClass()));
			chunkList.add(new Chunk(msg));
		}
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(idList, chunkList);
	}

	/**
	 * 发送多条消息至玩家
	 * 
	 * @param idList
	 * @param chunkList
	 */
	public void sendMsg(List<Integer> idList, List<Chunk> chunkList) {
		if (idList == null || chunkList == null || idList.size() <= 0)
			return;

		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(idList, chunkList);
	}

	/**
	 * 发送消息至玩家
	 * 
	 * @param builder
	 */
	public void sendMsg(Message msg) {
		if (msg == null)
			return;

		// 玩家连接信息
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(MsgIds.getIdByClass(msg.getClass()), new Chunk(msg));
	}

	/**
	 * 玩家进入跨服房间
	 * 
	 * @param stageObj
	 */
	public void stageRegister(CrossStageObject stageObj) {
		stageObj.addCombatantObj(this);
	}

}
