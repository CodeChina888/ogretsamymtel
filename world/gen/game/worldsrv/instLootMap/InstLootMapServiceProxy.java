package game.worldsrv.instLootMap;
                    
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
import game.worldsrv.entity.Human;

@GofGenFile
public final class InstLootMapServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int InstLootMapService_enterMultip_long_int = 1;
		public static final int InstLootMapService_enterSingle_Human_int = 2;
		public static final int InstLootMapService_humanOut_long = 3;
		public static final int InstLootMapService_humanSignOut_long = 4;
		public static final int InstLootMapService_intoSignUpRoom_Human_int = 5;
		public static final int InstLootMapService_leaveSignUpRoom_Human = 6;
		public static final int InstLootMapService_resrveFunction_int_String = 7;
		public static final int InstLootMapService_rmvGameStage_long = 8;
		public static final int InstLootMapService_signUpRoomTimeOut_long = 9;
	}
	private static final String SERV_ID = "worldsrv.instLootMap.InstLootMapService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private InstLootMapServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		InstLootMapService serv = (InstLootMapService)service;
		switch (methodKey) {
			case EnumCall.InstLootMapService_enterMultip_long_int: {
				return (GofFunction2<Long, Integer>)serv::enterMultip;
			}
			case EnumCall.InstLootMapService_enterSingle_Human_int: {
				return (GofFunction2<Human, Integer>)serv::enterSingle;
			}
			case EnumCall.InstLootMapService_humanOut_long: {
				return (GofFunction1<Long>)serv::humanOut;
			}
			case EnumCall.InstLootMapService_humanSignOut_long: {
				return (GofFunction1<Long>)serv::humanSignOut;
			}
			case EnumCall.InstLootMapService_intoSignUpRoom_Human_int: {
				return (GofFunction2<Human, Integer>)serv::intoSignUpRoom;
			}
			case EnumCall.InstLootMapService_leaveSignUpRoom_Human: {
				return (GofFunction1<Human>)serv::leaveSignUpRoom;
			}
			case EnumCall.InstLootMapService_resrveFunction_int_String: {
				return (GofFunction2<Integer, String>)serv::resrveFunction;
			}
			case EnumCall.InstLootMapService_rmvGameStage_long: {
				return (GofFunction1<Long>)serv::rmvGameStage;
			}
			case EnumCall.InstLootMapService_signUpRoomTimeOut_long: {
				return (GofFunction1<Long>)serv::signUpRoomTimeOut;
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
	public static InstLootMapServiceProxy newInstance() {
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
	private static InstLootMapServiceProxy createInstance(String node, String port, Object id) {
		InstLootMapServiceProxy inst = new InstLootMapServiceProxy();
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
	
	public void enterMultip(long humanId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_enterMultip_long_int, "InstLootMapServiceProxy.enterMultip", new Object[] {humanId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void enterSingle(Human human, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_enterSingle_Human_int, "InstLootMapServiceProxy.enterSingle", new Object[] {human, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void humanOut(long huamanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_humanOut_long, "InstLootMapServiceProxy.humanOut", new Object[] {huamanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void humanSignOut(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_humanSignOut_long, "InstLootMapServiceProxy.humanSignOut", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void intoSignUpRoom(Human human, int actSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_intoSignUpRoom_Human_int, "InstLootMapServiceProxy.intoSignUpRoom", new Object[] {human, actSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void leaveSignUpRoom(Human human) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_leaveSignUpRoom_Human, "InstLootMapServiceProxy.leaveSignUpRoom", new Object[] {human});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void resrveFunction(int param, String str) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_resrveFunction_int_String, "InstLootMapServiceProxy.resrveFunction", new Object[] {param, str});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void rmvGameStage(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_rmvGameStage_long, "InstLootMapServiceProxy.rmvGameStage", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void signUpRoomTimeOut(long roomId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.InstLootMapService_signUpRoomTimeOut_long, "InstLootMapServiceProxy.signUpRoomTimeOut", new Object[] {roomId});
		if(immutableOnce) immutableOnce = false;
	}
}
