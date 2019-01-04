package crosssrv.stage;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import crosssrv.combatant.CombatantGlobalInfo;
import java.util.List;

@GofGenFile
public final class CrossStageGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int CrossStageGlobalService_destroy_long = 1;
		public static final int CrossStageGlobalService_infoCancel_long = 2;
		public static final int CrossStageGlobalService_infoRegister_long_int_String_String_String = 3;
		public static final int CrossStageGlobalService_login_CombatantGlobalInfo = 4;
		public static final int CrossStageGlobalService_setEnd_long = 5;
		public static final int CrossStageGlobalService_stageHumanNumAdd_long = 6;
		public static final int CrossStageGlobalService_stageHumanNumReduce_long = 7;
		public static final int CrossStageGlobalService_update_Objects = 8;
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
	private CrossStageGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		CrossStageGlobalService serv = (CrossStageGlobalService)service;
		switch (methodKey) {
			case EnumCall.CrossStageGlobalService_destroy_long: {
				return (GofFunction1<Long>)serv::destroy;
			}
			case EnumCall.CrossStageGlobalService_infoCancel_long: {
				return (GofFunction1<Long>)serv::infoCancel;
			}
			case EnumCall.CrossStageGlobalService_infoRegister_long_int_String_String_String: {
				return (GofFunction5<Long, Integer, String, String, String>)serv::infoRegister;
			}
			case EnumCall.CrossStageGlobalService_login_CombatantGlobalInfo: {
				return (GofFunction1<CombatantGlobalInfo>)serv::login;
			}
			case EnumCall.CrossStageGlobalService_setEnd_long: {
				return (GofFunction1<Long>)serv::setEnd;
			}
			case EnumCall.CrossStageGlobalService_stageHumanNumAdd_long: {
				return (GofFunction1<Long>)serv::stageHumanNumAdd;
			}
			case EnumCall.CrossStageGlobalService_stageHumanNumReduce_long: {
				return (GofFunction1<Long>)serv::stageHumanNumReduce;
			}
			case EnumCall.CrossStageGlobalService_update_Objects: {
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
	public static CrossStageGlobalServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static CrossStageGlobalServiceProxy newInstance(String node, String port, Object id) {
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
	private static CrossStageGlobalServiceProxy createInstance(String node, String port, Object id) {
		CrossStageGlobalServiceProxy inst = new CrossStageGlobalServiceProxy();
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
	
	public void destroy(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageGlobalService_destroy_long, "CrossStageGlobalServiceProxy.destroy", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void infoCancel(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageGlobalService_infoCancel_long, "CrossStageGlobalServiceProxy.infoCancel", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void infoRegister(long stageId, int mapSn, String stageName, String nodeId, String portId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageGlobalService_infoRegister_long_int_String_String_String, "CrossStageGlobalServiceProxy.infoRegister", new Object[] {stageId, mapSn, stageName, nodeId, portId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void login(CombatantGlobalInfo info) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageGlobalService_login_CombatantGlobalInfo, "CrossStageGlobalServiceProxy.login", new Object[] {info});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setEnd(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageGlobalService_setEnd_long, "CrossStageGlobalServiceProxy.setEnd", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void stageHumanNumAdd(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageGlobalService_stageHumanNumAdd_long, "CrossStageGlobalServiceProxy.stageHumanNumAdd", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void stageHumanNumReduce(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageGlobalService_stageHumanNumReduce_long, "CrossStageGlobalServiceProxy.stageHumanNumReduce", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageGlobalService_update_Objects, "CrossStageGlobalServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
}
