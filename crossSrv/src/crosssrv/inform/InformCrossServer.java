package crosssrv.inform;

import com.google.protobuf.Message;

import core.CallPoint;
import core.Node;
import core.RemoteNode;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Distr;
import crosssrv.seam.CrossPort;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.support.D;

@DistrClass(importClass = { Message.class })
public class InformCrossServer extends Service {

	/**
	 * 构造函数
	 * 
	 * @param port
	 */
	public InformCrossServer(CrossPort port) {
		super(port);
	}

	@Override
	public Object getId() {
		return D.CROSS_SERV_INFORM;
	}

	/**
	 * 弹幕
	 * 
	 * @param msgBuild
	 */
	@DistrMethod
	public void informCross01_Chat(int serverId, String name, String content, int icon, int aptitude) {
		Node curNode = port.getNode();
		for (RemoteNode r : curNode.getRemoteNodeAll()) {
			if (r.getRemoteId().endsWith(Distr.NODE_DEFAULT)) {
				CallPoint toPoint = new CallPoint(r.getRemoteId(), Distr.getPortId(D.SERV_HUMAN_GLOBAL),
						D.SERV_HUMAN_GLOBAL);
				port.call(false, toPoint,
						HumanGlobalServiceProxy.EnumCall.HumanGlobalService_sendCrossInform_int_String_String_int_int,
						"HumanGlobalService_sendCrossInform", new Object[] { serverId, name, content, icon, aptitude });
			}
		}
	}

	/**
	 * 每个service预留空方法
	 * 
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {

	}
}
