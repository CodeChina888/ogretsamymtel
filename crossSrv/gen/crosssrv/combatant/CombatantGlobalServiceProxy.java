package crosssrv.combatant;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import crosssrv.combatant.CombatantGlobalInfo;
import java.util.Map;
import java.util.List;
import com.google.protobuf.Message;
import java.util.Set;

@GofGenFile
public final class CombatantGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int CombatantGlobalService_ChangeConnPoint_Long_CallPoint = 1;
		public static final int CombatantGlobalService_cancel_long = 2;
		public static final int CombatantGlobalService_fireEventToAll_List_int = 3;
		public static final int CombatantGlobalService_getInfo_List = 4;
		public static final int CombatantGlobalService_getInfo_long = 5;
		public static final int CombatantGlobalService_getInfos_List = 6;
		public static final int CombatantGlobalService_getInfosByMap_Map = 7;
		public static final int CombatantGlobalService_kick_long_String = 8;
		public static final int CombatantGlobalService_register_List = 9;
		public static final int CombatantGlobalService_register_CombatantGlobalInfo = 10;
		public static final int CombatantGlobalService_sendMsg_long_Message = 11;
		public static final int CombatantGlobalService_sendMsgTo_List_Message = 12;
		public static final int CombatantGlobalService_sendMsgToAll_List_Message = 13;
		public static final int CombatantGlobalService_sendMsgToStage_Set_Message = 14;
		public static final int CombatantGlobalService_update_Objects = 15;
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
	private CombatantGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		CombatantGlobalService serv = (CombatantGlobalService)service;
		switch (methodKey) {
			case EnumCall.CombatantGlobalService_ChangeConnPoint_Long_CallPoint: {
				return (GofFunction2<Long, CallPoint>)serv::ChangeConnPoint;
			}
			case EnumCall.CombatantGlobalService_cancel_long: {
				return (GofFunction1<Long>)serv::cancel;
			}
			case EnumCall.CombatantGlobalService_fireEventToAll_List_int: {
				return (GofFunction2<List, Integer>)serv::fireEventToAll;
			}
			case EnumCall.CombatantGlobalService_getInfo_List: {
				return (GofFunction1<List>)serv::getInfo;
			}
			case EnumCall.CombatantGlobalService_getInfo_long: {
				return (GofFunction1<Long>)serv::getInfo;
			}
			case EnumCall.CombatantGlobalService_getInfos_List: {
				return (GofFunction1<List>)serv::getInfos;
			}
			case EnumCall.CombatantGlobalService_getInfosByMap_Map: {
				return (GofFunction1<Map>)serv::getInfosByMap;
			}
			case EnumCall.CombatantGlobalService_kick_long_String: {
				return (GofFunction2<Long, String>)serv::kick;
			}
			case EnumCall.CombatantGlobalService_register_List: {
				return (GofFunction1<List>)serv::register;
			}
			case EnumCall.CombatantGlobalService_register_CombatantGlobalInfo: {
				return (GofFunction1<CombatantGlobalInfo>)serv::register;
			}
			case EnumCall.CombatantGlobalService_sendMsg_long_Message: {
				return (GofFunction2<Long, Message>)serv::sendMsg;
			}
			case EnumCall.CombatantGlobalService_sendMsgTo_List_Message: {
				return (GofFunction2<List, Message>)serv::sendMsgTo;
			}
			case EnumCall.CombatantGlobalService_sendMsgToAll_List_Message: {
				return (GofFunction2<List, Message>)serv::sendMsgToAll;
			}
			case EnumCall.CombatantGlobalService_sendMsgToStage_Set_Message: {
				return (GofFunction2<Set, Message>)serv::sendMsgToStage;
			}
			case EnumCall.CombatantGlobalService_update_Objects: {
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
	public static CombatantGlobalServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static CombatantGlobalServiceProxy newInstance(String node, String port, Object id) {
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
	private static CombatantGlobalServiceProxy createInstance(String node, String port, Object id) {
		CombatantGlobalServiceProxy inst = new CombatantGlobalServiceProxy();
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
	
	public void ChangeConnPoint(Long id, CallPoint point) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_ChangeConnPoint_Long_CallPoint, "CombatantGlobalServiceProxy.ChangeConnPoint", new Object[] {id, point});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void cancel(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_cancel_long, "CombatantGlobalServiceProxy.cancel", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void fireEventToAll(List excludeIds, int key) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CombatantGlobalService_fireEventToAll_List_int, "CombatantGlobalServiceProxy.fireEventToAll", new Object[] {excludeIds, key});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getInfo(List humansIds) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_getInfo_List, "CombatantGlobalServiceProxy.getInfo", new Object[] {humansIds});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getInfo(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_getInfo_long, "CombatantGlobalServiceProxy.getInfo", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getInfos(List humanIds) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_getInfos_List, "CombatantGlobalServiceProxy.getInfos", new Object[] {humanIds});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getInfosByMap(Map humanIdsMap) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_getInfosByMap_Map, "CombatantGlobalServiceProxy.getInfosByMap", new Object[] {humanIdsMap});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void kick(long humanId, String reason) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_kick_long_String, "CombatantGlobalServiceProxy.kick", new Object[] {humanId, reason});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void register(List infos) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_register_List, "CombatantGlobalServiceProxy.register", new Object[] {infos});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void register(CombatantGlobalInfo info) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_register_CombatantGlobalInfo, "CombatantGlobalServiceProxy.register", new Object[] {info});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendMsg(long humanId, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CombatantGlobalService_sendMsg_long_Message, "CombatantGlobalServiceProxy.sendMsg", new Object[] {humanId, msg});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void sendMsgTo(List humanIds, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CombatantGlobalService_sendMsgTo_List_Message, "CombatantGlobalServiceProxy.sendMsgTo", new Object[] {humanIds, msg});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void sendMsgToAll(List excludeIds, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CombatantGlobalService_sendMsgToAll_List_Message, "CombatantGlobalServiceProxy.sendMsgToAll", new Object[] {excludeIds, msg});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void sendMsgToStage(Set stageIds, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_sendMsgToStage_Set_Message, "CombatantGlobalServiceProxy.sendMsgToStage", new Object[] {stageIds, msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CombatantGlobalService_update_Objects, "CombatantGlobalServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
}
