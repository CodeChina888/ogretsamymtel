package core.connsrv;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import core.Chunk;
import core.support.ConnectionStatus;
import java.util.List;

@GofGenFile
public final class ConnectionProxy extends ProxyBase {
	public final class EnumCall{
		public static final int Connection_close = 1;
		public static final int Connection_getIpAddress = 2;
		public static final int Connection_getMsgBuf = 3;
		public static final int Connection_initMsgBuf_CallPoint = 4;
		public static final int Connection_sendMsg_int_Chunk = 5;
		public static final int Connection_sendMsg_List_List = 6;
		public static final int Connection_sendMsgBuf = 7;
		public static final int Connection_setStatus_int = 8;
		public static final int Connection_updateStatus_String_String_long = 9;
		public static final int Connection_updateStatus_ConnectionStatus = 10;
	}
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private ConnectionProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		Connection serv = (Connection)service;
		switch (methodKey) {
			case EnumCall.Connection_close: {
				return (GofFunction0)serv::close;
			}
			case EnumCall.Connection_getIpAddress: {
				return (GofFunction0)serv::getIpAddress;
			}
			case EnumCall.Connection_getMsgBuf: {
				return (GofFunction0)serv::getMsgBuf;
			}
			case EnumCall.Connection_initMsgBuf_CallPoint: {
				return (GofFunction1<CallPoint>)serv::initMsgBuf;
			}
			case EnumCall.Connection_sendMsg_int_Chunk: {
				return (GofFunction2<Integer, Chunk>)serv::sendMsg;
			}
			case EnumCall.Connection_sendMsg_List_List: {
				return (GofFunction2<List, List>)serv::sendMsg;
			}
			case EnumCall.Connection_sendMsgBuf: {
				return (GofFunction0)serv::sendMsgBuf;
			}
			case EnumCall.Connection_setStatus_int: {
				return (GofFunction1<Integer>)serv::setStatus;
			}
			case EnumCall.Connection_updateStatus_String_String_long: {
				return (GofFunction3<String, String, Long>)serv::updateStatus;
			}
			case EnumCall.Connection_updateStatus_ConnectionStatus: {
				return (GofFunction1<ConnectionStatus>)serv::updateStatus;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ConnectionProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ConnectionProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static ConnectionProxy createInstance(String node, String port, Object id) {
		ConnectionProxy inst = new ConnectionProxy();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Param context) {
		context.put("_callerInfo", callerInfo);
		localPort.listenResult(method, context);
	}
	
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Param context) {
		context.put("_callerInfo", callerInfo);
		localPort.listenResult(method, context);
	}
	
	
	/**
	 * 等待返回值
	 */
	public Param waitForResult() {
		return localPort.waitForResult();
	}
	
	/**
	 * 设置后预先提醒框架下一次RPC调用参数是不可变的，可进行通信优化。<br/>
	 * 同Node间通信将不在进行Call对象克隆，可极大提高性能。<br/>
	 * 但设置后由于没进行克隆操作，接发双方都可对同一对象进行操作，可能会引起错误。<br/>
	 * 
	 * *由于有危险性，并且大多数时候RPC成本不高，建议只有业务中频繁调用或参数克隆成本较高时才使用本函数。<br/>
	 * *当接发双方仅有一方会对通信参数进行处理时，哪怕参数中有可变类型，也可以调用本函数进行优化。<br/>
	 * *当接发双方Node不相同时，本参数无效，双方处理不同对象；<br/>
	 *  当接发双方Node相同时，双方处理相同对象，这种差异逻辑会对分布式应用带来隐患，要小心使用。 <br/>
	 */
	public void immutableOnce() {
		this.immutableOnce = true;
	}
	
	public void close() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_close, "ConnectionProxy.close", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getIpAddress() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.Connection_getIpAddress, "ConnectionProxy.getIpAddress", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getMsgBuf() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_getMsgBuf, "ConnectionProxy.getMsgBuf", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void initMsgBuf(CallPoint connPoint) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_initMsgBuf_CallPoint, "ConnectionProxy.initMsgBuf", new Object[] {connPoint});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendMsg(int msgId, Chunk msgbuf) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_sendMsg_int_Chunk, "ConnectionProxy.sendMsg", new Object[] {msgId, msgbuf});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void sendMsg(List idList, List chunkList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_sendMsg_List_List, "ConnectionProxy.sendMsg", new Object[] {idList, chunkList});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendMsgBuf() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_sendMsgBuf, "ConnectionProxy.sendMsgBuf", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setStatus(int status) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_setStatus_int, "ConnectionProxy.setStatus", new Object[] {status});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateStatus(String node, String port, long stage) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_updateStatus_String_String_long, "ConnectionProxy.updateStatus", new Object[] {node, port, stage});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateStatus(ConnectionStatus status) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.Connection_updateStatus_ConnectionStatus, "ConnectionProxy.updateStatus", new Object[] {status});
		if(immutableOnce) immutableOnce = false;
	}
}
