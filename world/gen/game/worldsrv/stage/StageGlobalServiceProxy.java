package game.worldsrv.stage;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Distr;
import core.support.log.LogCore;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import game.worldsrv.character.HumanObject;
import java.util.List;
import com.google.protobuf.Message;
import game.worldsrv.support.Vector2D;
import core.support.ConnectionStatus;
import com.google.protobuf.GeneratedMessage;

@GofGenFile
public final class StageGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int StageGlobalService_applyStageBySn_long = 1;
		public static final int StageGlobalService_destroy_long = 2;
		public static final int StageGlobalService_dispatchCombatMsg_long_long_CallPoint_int_GeneratedMessage = 3;
		public static final int StageGlobalService_infoCancel_long = 4;
		public static final int StageGlobalService_infoRegister_long_int_int_String_String_String = 5;
		public static final int StageGlobalService_login_long_CallPoint_ConnectionStatus_List_int = 6;
		public static final int StageGlobalService_quitToCommon_HumanObject_int_Objects = 7;
		public static final int StageGlobalService_stageHumanNumAdd_long = 8;
		public static final int StageGlobalService_stageHumanNumReduce_long = 9;
		public static final int StageGlobalService_switchToStage_HumanObject_long_Vector2D_Vector2D = 10;
		public static final int StageGlobalService_update_Objects = 11;
	}
	private static final String SERV_ID = "stage.StageGlobalService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private StageGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		StageGlobalService serv = (StageGlobalService)service;
		switch (methodKey) {
			case EnumCall.StageGlobalService_applyStageBySn_long: {
				return (GofFunction1<Long>)serv::applyStageBySn;
			}
			case EnumCall.StageGlobalService_destroy_long: {
				return (GofFunction1<Long>)serv::destroy;
			}
			case EnumCall.StageGlobalService_dispatchCombatMsg_long_long_CallPoint_int_GeneratedMessage: {
				return (GofFunction5<Long, Long, CallPoint, Integer, GeneratedMessage>)serv::dispatchCombatMsg;
			}
			case EnumCall.StageGlobalService_infoCancel_long: {
				return (GofFunction1<Long>)serv::infoCancel;
			}
			case EnumCall.StageGlobalService_infoRegister_long_int_int_String_String_String: {
				return (GofFunction6<Long, Integer, Integer, String, String, String>)serv::infoRegister;
			}
			case EnumCall.StageGlobalService_login_long_CallPoint_ConnectionStatus_List_int: {
				return (GofFunction5<Long, CallPoint, ConnectionStatus, List, Integer>)serv::login;
			}
			case EnumCall.StageGlobalService_quitToCommon_HumanObject_int_Objects: {
				return (GofFunction3<HumanObject, Integer, Object[]>)serv::quitToCommon;
			}
			case EnumCall.StageGlobalService_stageHumanNumAdd_long: {
				return (GofFunction1<Long>)serv::stageHumanNumAdd;
			}
			case EnumCall.StageGlobalService_stageHumanNumReduce_long: {
				return (GofFunction1<Long>)serv::stageHumanNumReduce;
			}
			case EnumCall.StageGlobalService_switchToStage_HumanObject_long_Vector2D_Vector2D: {
				return (GofFunction4<HumanObject, Long, Vector2D, Vector2D>)serv::switchToStage;
			}
			case EnumCall.StageGlobalService_update_Objects: {
				return (GofFunction1<Object[]>)serv::update;
			}
			default: break;
		}
		return null;
	}
	
	/**
	 * 获取实例
	 * 大多数情况下可用此函数获取
	 * @return
	 */
	public static StageGlobalServiceProxy newInstance() {
		String portId = Distr.getPortId(SERV_ID);
		if(portId == null) {
			LogCore.remote.error("通过servId未能找到查找上级Port: servId={}", SERV_ID);
			return null;
		}
		
		String nodeId = Distr.getNodeId(portId);
		if(nodeId == null) {
			LogCore.remote.error("通过portId未能找到查找上级Node: portId={}", portId);
			return null;
		}
		
		return createInstance(nodeId, portId, SERV_ID);
	}
	
	
	/**
	 * 创建实例
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static StageGlobalServiceProxy createInstance(String node, String port, Object id) {
		StageGlobalServiceProxy inst = new StageGlobalServiceProxy();
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
	
	public void applyStageBySn(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_applyStageBySn_long, "StageGlobalServiceProxy.applyStageBySn", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void destroy(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_destroy_long, "StageGlobalServiceProxy.destroy", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void dispatchCombatMsg(long stageId, long connId, CallPoint connPoint, int msgId, GeneratedMessage msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_dispatchCombatMsg_long_long_CallPoint_int_GeneratedMessage, "StageGlobalServiceProxy.dispatchCombatMsg", new Object[] {stageId, connId, connPoint, msgId, msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void infoCancel(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_infoCancel_long, "StageGlobalServiceProxy.infoCancel", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void infoRegister(long stageId, int stageSn, int mapSn, String stageName, String nodeId, String portId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_infoRegister_long_int_int_String_String_String, "StageGlobalServiceProxy.infoRegister", new Object[] {stageId, stageSn, mapSn, stageName, nodeId, portId});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void login(long humanId, CallPoint connPoint, ConnectionStatus connStatus, List lastStageIds, int firstStory) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_login_long_CallPoint_ConnectionStatus_List_int, "StageGlobalServiceProxy.login", new Object[] {humanId, connPoint, connStatus, lastStageIds, firstStory});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void quitToCommon(HumanObject humanObj, int nowSn, Object... params) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.StageGlobalService_quitToCommon_HumanObject_int_Objects, "StageGlobalServiceProxy.quitToCommon", new Object[] {humanObj, nowSn, params});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void stageHumanNumAdd(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_stageHumanNumAdd_long, "StageGlobalServiceProxy.stageHumanNumAdd", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void stageHumanNumReduce(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_stageHumanNumReduce_long, "StageGlobalServiceProxy.stageHumanNumReduce", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void switchToStage(HumanObject humanObj, long stageId, Vector2D pos, Vector2D dir) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_switchToStage_HumanObject_long_Vector2D_Vector2D, "StageGlobalServiceProxy.switchToStage", new Object[] {humanObj, stageId, pos, dir});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageGlobalService_update_Objects, "StageGlobalServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
}
