package crosssrv.stage;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import crosssrv.character.CombatantObject;
import crosssrv.combatant.CombatantGlobalInfo;

@GofGenFile
public final class CrossStageObjectServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int CrossStageObjectService_clearError_long = 1;
		public static final int CrossStageObjectService_login_CombatantGlobalInfo = 2;
		public static final int CrossStageObjectService_register_CombatantObject_boolean_long = 3;
		public static final int CrossStageObjectService_update_Objects = 4;
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
	private CrossStageObjectServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		CrossStageObjectService serv = (CrossStageObjectService)service;
		switch (methodKey) {
			case EnumCall.CrossStageObjectService_clearError_long: {
				return (GofFunction1<Long>)serv::clearError;
			}
			case EnumCall.CrossStageObjectService_login_CombatantGlobalInfo: {
				return (GofFunction1<CombatantGlobalInfo>)serv::login;
			}
			case EnumCall.CrossStageObjectService_register_CombatantObject_boolean_long: {
				return (GofFunction3<CombatantObject, Boolean, Long>)serv::register;
			}
			case EnumCall.CrossStageObjectService_update_Objects: {
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
	public static CrossStageObjectServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static CrossStageObjectServiceProxy newInstance(String node, String port, Object id) {
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
	private static CrossStageObjectServiceProxy createInstance(String node, String port, Object id) {
		CrossStageObjectServiceProxy inst = new CrossStageObjectServiceProxy();
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
	
	public void clearError(long curr) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageObjectService_clearError_long, "CrossStageObjectServiceProxy.clearError", new Object[] {curr});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void login(CombatantGlobalInfo info) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageObjectService_login_CombatantGlobalInfo, "CrossStageObjectServiceProxy.login", new Object[] {info});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void register(CombatantObject combatantObj, boolean isRevive, long stageTargetId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageObjectService_register_CombatantObject_boolean_long, "CrossStageObjectServiceProxy.register", new Object[] {combatantObj, isRevive, stageTargetId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageObjectService_update_Objects, "CrossStageObjectServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
}
