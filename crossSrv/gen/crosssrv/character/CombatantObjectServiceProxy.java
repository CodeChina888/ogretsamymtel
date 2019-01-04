package crosssrv.character;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;

@GofGenFile
public final class CombatantObjectServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int CombatantObjectService_ChangeConnPoint_CallPoint = 1;
		public static final int CombatantObjectService_connCheck_long = 2;
		public static final int CombatantObjectService_connClosed_long = 3;
		public static final int CombatantObjectService_fireEventToHuman_int = 4;
		public static final int CombatantObjectService_kickClosed = 5;
		public static final int CombatantObjectService_leave_long = 6;
		public static final int CombatantObjectService_msgHandler_long_bytes = 7;
		public static final int CombatantObjectService_onSchedule_int_long = 8;
		public static final int CombatantObjectService_update_Objects = 9;
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
	private CombatantObjectServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		CombatantObjectService serv = (CombatantObjectService)service;
		switch (methodKey) {
			case EnumCall.CombatantObjectService_ChangeConnPoint_CallPoint: {
				return (GofFunction1<CallPoint>)serv::ChangeConnPoint;
			}
			case EnumCall.CombatantObjectService_connCheck_long: {
				return (GofFunction1<Long>)serv::connCheck;
			}
			case EnumCall.CombatantObjectService_connClosed_long: {
				return (GofFunction1<Long>)serv::connClosed;
			}
			case EnumCall.CombatantObjectService_fireEventToHuman_int: {
				return (GofFunction1<Integer>)serv::fireEventToHuman;
			}
			case EnumCall.CombatantObjectService_kickClosed: {
				return (GofFunction0)serv::kickClosed;
			}
			case EnumCall.CombatantObjectService_leave_long: {
				return (GofFunction1<Long>)serv::leave;
			}
			case EnumCall.CombatantObjectService_msgHandler_long_bytes: {
				return (GofFunction2<Long, byte[]>)serv::msgHandler;
			}
			case EnumCall.CombatantObjectService_onSchedule_int_long: {
				return (GofFunction2<Integer, Long>)serv::onSchedule;
			}
			case EnumCall.CombatantObjectService_update_Objects: {
				return (GofFunction1<Object[]>)serv::update;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static CombatantObjectServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static CombatantObjectServiceProxy newInstance(String node, String port, Object id) {
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
	private static CombatantObjectServiceProxy createInstance(String node, String port, Object id) {
		CombatantObjectServiceProxy inst = new CombatantObjectServiceProxy();
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
	
	public void ChangeConnPoint(CallPoint point) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_ChangeConnPoint_CallPoint, "CombatantObjectServiceProxy.ChangeConnPoint", new Object[] {point});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void connCheck(long connId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_connCheck_long, "CombatantObjectServiceProxy.connCheck", new Object[] {connId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void connClosed(long connId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_connClosed_long, "CombatantObjectServiceProxy.connClosed", new Object[] {connId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void fireEventToHuman(int key) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_fireEventToHuman_int, "CombatantObjectServiceProxy.fireEventToHuman", new Object[] {key});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void kickClosed() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_kickClosed, "CombatantObjectServiceProxy.kickClosed", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void leave(long stageTargetId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_leave_long, "CombatantObjectServiceProxy.leave", new Object[] {stageTargetId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void msgHandler(long connId, byte... chunk) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_msgHandler_long_bytes, "CombatantObjectServiceProxy.msgHandler", new Object[] {connId, chunk});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void onSchedule(int key, long timeLast) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_onSchedule_int_long, "CombatantObjectServiceProxy.onSchedule", new Object[] {key, timeLast});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantObjectService_update_Objects, "CombatantObjectServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
}
