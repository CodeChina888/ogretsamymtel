package game.worldsrv.maincity;
                    
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
import java.util.List;
import java.util.Map;
import game.worldsrv.entity.Castellan;
import game.msg.Define.ECastellanType;

@GofGenFile
public final class MaincityServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int MaincityService_CastellanMap_ECastellanType_Castellan = 1;
		public static final int MaincityService_addRedPacket_long_int = 2;
		public static final int MaincityService_addWorldBossCastellan_Castellan = 3;
		public static final int MaincityService_cleanRedPacket = 4;
		public static final int MaincityService_getCastellanMap = 5;
		public static final int MaincityService_getCastellanType_Buy_ECastellanType_int = 6;
		public static final int MaincityService_getRedPacketMap = 7;
		public static final int MaincityService_robRedPacket_long_String_long = 8;
		public static final int MaincityService_updateDuke = 9;
	}
	private static final String SERV_ID = "worldsrv.maincity.MaincityService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private MaincityServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		MaincityService serv = (MaincityService)service;
		switch (methodKey) {
			case EnumCall.MaincityService_CastellanMap_ECastellanType_Castellan: {
				return (GofFunction2<ECastellanType, Castellan>)serv::CastellanMap;
			}
			case EnumCall.MaincityService_addRedPacket_long_int: {
				return (GofFunction2<Long, Integer>)serv::addRedPacket;
			}
			case EnumCall.MaincityService_addWorldBossCastellan_Castellan: {
				return (GofFunction1<Castellan>)serv::addWorldBossCastellan;
			}
			case EnumCall.MaincityService_cleanRedPacket: {
				return (GofFunction0)serv::cleanRedPacket;
			}
			case EnumCall.MaincityService_getCastellanMap: {
				return (GofFunction0)serv::getCastellanMap;
			}
			case EnumCall.MaincityService_getCastellanType_Buy_ECastellanType_int: {
				return (GofFunction2<ECastellanType, Integer>)serv::getCastellanType_Buy;
			}
			case EnumCall.MaincityService_getRedPacketMap: {
				return (GofFunction0)serv::getRedPacketMap;
			}
			case EnumCall.MaincityService_robRedPacket_long_String_long: {
				return (GofFunction3<Long, String, Long>)serv::robRedPacket;
			}
			case EnumCall.MaincityService_updateDuke: {
				return (GofFunction0)serv::updateDuke;
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
	public static MaincityServiceProxy newInstance() {
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
	private static MaincityServiceProxy createInstance(String node, String port, Object id) {
		MaincityServiceProxy inst = new MaincityServiceProxy();
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
	
	public void CastellanMap(ECastellanType type, Castellan castellan) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_CastellanMap_ECastellanType_Castellan, "MaincityServiceProxy.CastellanMap", new Object[] {type, castellan});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void addRedPacket(long humanId, int packSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_addRedPacket_long_int, "MaincityServiceProxy.addRedPacket", new Object[] {humanId, packSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void addWorldBossCastellan(Castellan castellan) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_addWorldBossCastellan_Castellan, "MaincityServiceProxy.addWorldBossCastellan", new Object[] {castellan});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void cleanRedPacket() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_cleanRedPacket, "MaincityServiceProxy.cleanRedPacket", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getCastellanMap() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_getCastellanMap, "MaincityServiceProxy.getCastellanMap", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getCastellanType_Buy(ECastellanType type, int sn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_getCastellanType_Buy_ECastellanType_int, "MaincityServiceProxy.getCastellanType_Buy", new Object[] {type, sn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getRedPacketMap() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_getRedPacketMap, "MaincityServiceProxy.getRedPacketMap", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void robRedPacket(long humanId, String humanName, long packId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_robRedPacket_long_String_long, "MaincityServiceProxy.robRedPacket", new Object[] {humanId, humanName, packId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateDuke() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.MaincityService_updateDuke, "MaincityServiceProxy.updateDuke", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
}
