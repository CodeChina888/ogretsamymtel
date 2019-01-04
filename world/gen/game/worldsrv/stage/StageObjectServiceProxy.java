package game.worldsrv.stage;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import game.worldsrv.character.HumanObject;
import game.worldsrv.support.Vector2D;
import java.util.Map;
import com.google.protobuf.Message;
import core.support.ConnectionStatus;
import com.google.protobuf.GeneratedMessage;

@GofGenFile
public final class StageObjectServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int StageObjectService_clearError_long_long = 1;
		public static final int StageObjectService_dispatchCombatMsg_long_long_CallPoint_int_GeneratedMessage = 2;
		public static final int StageObjectService_login_long_CallPoint_ConnectionStatus_long_Vector2D = 3;
		public static final int StageObjectService_register_HumanObject = 4;
		public static final int StageObjectService_update_Objects = 5;
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
	private StageObjectServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		StageObjectService serv = (StageObjectService)service;
		switch (methodKey) {
			case EnumCall.StageObjectService_clearError_long_long: {
				return (GofFunction2<Long, Long>)serv::clearError;
			}
			case EnumCall.StageObjectService_dispatchCombatMsg_long_long_CallPoint_int_GeneratedMessage: {
				return (GofFunction5<Long, Long, CallPoint, Integer, GeneratedMessage>)serv::dispatchCombatMsg;
			}
			case EnumCall.StageObjectService_login_long_CallPoint_ConnectionStatus_long_Vector2D: {
				return (GofFunction5<Long, CallPoint, ConnectionStatus, Long, Vector2D>)serv::login;
			}
			case EnumCall.StageObjectService_register_HumanObject: {
				return (GofFunction1<HumanObject>)serv::register;
			}
			case EnumCall.StageObjectService_update_Objects: {
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
	public static StageObjectServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static StageObjectServiceProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static StageObjectServiceProxy createInstance(String node, String port, Object id) {
		StageObjectServiceProxy inst = new StageObjectServiceProxy();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param method
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param method
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
	
	public void clearError(long curr, long interval) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageObjectService_clearError_long_long, "StageObjectServiceProxy.clearError", new Object[] {curr, interval});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void dispatchCombatMsg(long stageId, long connId, CallPoint connPoint, int msgId, GeneratedMessage msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageObjectService_dispatchCombatMsg_long_long_CallPoint_int_GeneratedMessage, "StageObjectServiceProxy.dispatchCombatMsg", new Object[] {stageId, connId, connPoint, msgId, msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void login(long humanId, CallPoint connPoint, ConnectionStatus connStatus, long stageId, Vector2D stagePos) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageObjectService_login_long_CallPoint_ConnectionStatus_long_Vector2D, "StageObjectServiceProxy.login", new Object[] {humanId, connPoint, connStatus, stageId, stagePos});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void register(HumanObject humanObj) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageObjectService_register_HumanObject, "StageObjectServiceProxy.register", new Object[] {humanObj});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageObjectService_update_Objects, "StageObjectServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
}
