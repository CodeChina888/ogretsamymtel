package crosssrv.singleFight;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import core.support.Utils;
import game.worldsrv.character.HumanMirrorObject;

/**
 * 单人战斗的玩家信息
 */
public class SingleFightHuman implements ISerilizable {
	public String nodeIdWorld;// 记录玩家在原服务器的nodeId
	public String portIdWorld;// 记录玩家在原服务器的portId
	public HumanMirrorObject humanMirrorObj;// 角色镜像数据
	public int type;// 战场类型
	public String token;// 登录验证串
	public boolean online;// 是否在线

	public long stageId;// 分配到的场景Id
	public int stageSn;// 分配到的场景sn
	public int mapSn;// 分配到的地图sn

	public SingleFightHuman() {

	}

	/**
	 * 构造函数
	 * 
	 * @param humanId
	 * @param humanMirrorObj
	 * @param type
	 * @param serverNodeId
	 * @param serverPortId
	 */
	public SingleFightHuman(HumanMirrorObject obj, int type, String nodeIdWorld, String portIdWorld) {
		this.nodeIdWorld = nodeIdWorld;
		this.portIdWorld = portIdWorld;
		this.humanMirrorObj = obj;
		this.type = type;

		this.token = String.valueOf(Utils.randomBetween(100000000, 999999999));
		this.online = true;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(nodeIdWorld);
		out.write(portIdWorld);
		out.write(humanMirrorObj);
		out.write(type);
		out.write(token);
		out.write(online);

		out.write(stageId);
		out.write(stageSn);
		out.write(mapSn);

	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		nodeIdWorld = in.read();
		portIdWorld = in.read();
		humanMirrorObj = in.read();
		type = in.read();
		token = in.read();
		online = in.read();

		stageId = in.read();
		stageSn = in.read();
		mapSn = in.read();

	}

	public long getHumanId() {
		return humanMirrorObj.getHumanMirror().getId();
	}

	public String getHumanName() {
		return humanMirrorObj.getHumanMirror().getName();
	}

	public int getHumanLv() {
		return humanMirrorObj.getHumanMirror().getLevel();
	}

}
