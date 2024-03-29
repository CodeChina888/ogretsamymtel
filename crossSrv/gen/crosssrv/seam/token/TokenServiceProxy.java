package crosssrv.seam.token;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import core.support.ConnectionStatus;

@GofGenFile
public final class TokenServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int TokenService_checkGateNum_int = 1;
		public static final int TokenService_connCheck_long = 2;
		public static final int TokenService_connClosed_long = 3;
		public static final int TokenService_msgHandler_long_ConnectionStatus_bytes = 4;
		public static final int TokenService_setLoginMaxOnline_int = 5;
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
	private TokenServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		TokenService serv = (TokenService)service;
		switch (methodKey) {
			case EnumCall.TokenService_checkGateNum_int: {
				return (GofFunction1<Integer>)serv::checkGateNum;
			}
			case EnumCall.TokenService_connCheck_long: {
				return (GofFunction1<Long>)serv::connCheck;
			}
			case EnumCall.TokenService_connClosed_long: {
				return (GofFunction1<Long>)serv::connClosed;
			}
			case EnumCall.TokenService_msgHandler_long_ConnectionStatus_bytes: {
				return (GofFunction3<Long, ConnectionStatus, byte[]>)serv::msgHandler;
			}
			case EnumCall.TokenService_setLoginMaxOnline_int: {
				return (GofFunction1<Integer>)serv::setLoginMaxOnline;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static TokenServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static TokenServiceProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param localPort
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static TokenServiceProxy createInstance(String node, String port, Object id) {
		TokenServiceProxy inst = new TokenServiceProxy();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
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
	
	public void checkGateNum(int status) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TokenService_checkGateNum_int, "TokenServiceProxy.checkGateNum", new Object[] {status});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void connCheck(long connId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TokenService_connCheck_long, "TokenServiceProxy.connCheck", new Object[] {connId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void connClosed(long connId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TokenService_connClosed_long, "TokenServiceProxy.connClosed", new Object[] {connId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void msgHandler(long connId, ConnectionStatus status, byte... msgbuf) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TokenService_msgHandler_long_ConnectionStatus_bytes, "TokenServiceProxy.msgHandler", new Object[] {connId, status, msgbuf});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setLoginMaxOnline(int maxOnline) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TokenService_setLoginMaxOnline_int, "TokenServiceProxy.setLoginMaxOnline", new Object[] {maxOnline});
		if(immutableOnce) immutableOnce = false;
	}
}
