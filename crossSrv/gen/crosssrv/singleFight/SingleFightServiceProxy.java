package crosssrv.singleFight;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import java.util.List;
import game.worldsrv.character.HumanMirrorObject;
import com.google.protobuf.Message;

@GofGenFile
public final class SingleFightServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int SingleFightService_SF001_match_HumanMirrorObject_int_String_String_int_int = 1;
		public static final int SingleFightService_SF002_leave_long = 2;
		public static final int SingleFightService_SF003_StageFightFinish_long_List_Message_int_boolean = 3;
		public static final int SingleFightService_SF004_StageLeaveFinish_long_List_long = 4;
		public static final int SingleFightService_SF005_ReplayRecord_long_String_String_long = 5;
		public static final int SingleFightService_SF006_LoadRecord_long = 6;
		public static final int SingleFightService_SF007_quickFight_HumanMirrorObject_int_String_String_int_int = 7;
		public static final int SingleFightService_SF008_update_Objects = 8;
		public static final int SingleFightService_SF009_NewbieFight_long_long_int_String_String_int_int = 9;
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
	private SingleFightServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		SingleFightService serv = (SingleFightService)service;
		switch (methodKey) {
			case EnumCall.SingleFightService_SF001_match_HumanMirrorObject_int_String_String_int_int: {
				return (GofFunction6<HumanMirrorObject, Integer, String, String, Integer, Integer>)serv::SF001_match;
			}
			case EnumCall.SingleFightService_SF002_leave_long: {
				return (GofFunction1<Long>)serv::SF002_leave;
			}
			case EnumCall.SingleFightService_SF003_StageFightFinish_long_List_Message_int_boolean: {
				return (GofFunction5<Long, List, Message, Integer, Boolean>)serv::SF003_StageFightFinish;
			}
			case EnumCall.SingleFightService_SF004_StageLeaveFinish_long_List_long: {
				return (GofFunction3<Long, List, Long>)serv::SF004_StageLeaveFinish;
			}
			case EnumCall.SingleFightService_SF005_ReplayRecord_long_String_String_long: {
				return (GofFunction4<Long, String, String, Long>)serv::SF005_ReplayRecord;
			}
			case EnumCall.SingleFightService_SF006_LoadRecord_long: {
				return (GofFunction1<Long>)serv::SF006_LoadRecord;
			}
			case EnumCall.SingleFightService_SF007_quickFight_HumanMirrorObject_int_String_String_int_int: {
				return (GofFunction6<HumanMirrorObject, Integer, String, String, Integer, Integer>)serv::SF007_quickFight;
			}
			case EnumCall.SingleFightService_SF008_update_Objects: {
				return (GofFunction1<Object[]>)serv::SF008_update;
			}
			case EnumCall.SingleFightService_SF009_NewbieFight_long_long_int_String_String_int_int: {
				return (GofFunction7<Long, Long, Integer, String, String, Integer, Integer>)serv::SF009_NewbieFight;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static SingleFightServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static SingleFightServiceProxy newInstance(String node, String port, Object id) {
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
	private static SingleFightServiceProxy createInstance(String node, String port, Object id) {
		SingleFightServiceProxy inst = new SingleFightServiceProxy();
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
	
	public void SF001_match(HumanMirrorObject humanMirrorObj, int type, String nodeIdWorld, String portIdWorld, int stageSn, int mapSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF001_match_HumanMirrorObject_int_String_String_int_int, "SingleFightServiceProxy.SF001_match", new Object[] {humanMirrorObj, type, nodeIdWorld, portIdWorld, stageSn, mapSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void SF002_leave(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF002_leave_long, "SingleFightServiceProxy.SF002_leave", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void SF003_StageFightFinish(long stageId, List combatants, Message msg, int winTeam, boolean isAlwaysWin) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF003_StageFightFinish_long_List_Message_int_boolean, "SingleFightServiceProxy.SF003_StageFightFinish", new Object[] {stageId, combatants, msg, winTeam, isAlwaysWin});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void SF004_StageLeaveFinish(long stageId, List combatants, long combatantId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF004_StageLeaveFinish_long_List_long, "SingleFightServiceProxy.SF004_StageLeaveFinish", new Object[] {stageId, combatants, combatantId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void SF005_ReplayRecord(long humanId, String serverNodeId, String serverPortId, long recordId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF005_ReplayRecord_long_String_String_long, "SingleFightServiceProxy.SF005_ReplayRecord", new Object[] {humanId, serverNodeId, serverPortId, recordId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void SF006_LoadRecord(long recordId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF006_LoadRecord_long, "SingleFightServiceProxy.SF006_LoadRecord", new Object[] {recordId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void SF007_quickFight(HumanMirrorObject humanMirrorObj, int type, String serverNodeId, String serverPortId, int stageSn, int mapSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF007_quickFight_HumanMirrorObject_int_String_String_int_int, "SingleFightServiceProxy.SF007_quickFight", new Object[] {humanMirrorObj, type, serverNodeId, serverPortId, stageSn, mapSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void SF008_update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF008_update_Objects, "SingleFightServiceProxy.SF008_update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void SF009_NewbieFight(long connId, long humanId, int fightType, String serverNodeId, String serverPortId, int stageSn, int mapSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.SingleFightService_SF009_NewbieFight_long_long_int_String_String_int_int, "SingleFightServiceProxy.SF009_NewbieFight", new Object[] {connId, humanId, fightType, serverNodeId, serverPortId, stageSn, mapSn});
		if(immutableOnce) immutableOnce = false;
	}
}
