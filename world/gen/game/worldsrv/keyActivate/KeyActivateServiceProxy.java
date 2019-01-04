package game.worldsrv.keyActivate;
                    
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

@GofGenFile
public final class KeyActivateServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int KeyActivateService_activateGift_String_String = 1;
		public static final int KeyActivateService_giftCodeActivate_String_String_String = 2;
		public static final int KeyActivateService_isActivate_String = 3;
		public static final int KeyActivateService_payBackGet_String_String_String = 4;
		public static final int KeyActivateService_useKey_String_String_String = 5;
	}
	private static final String SERV_ID = "worldsrv.keyActivate.KeyActivateService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private KeyActivateServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		KeyActivateService serv = (KeyActivateService)service;
		switch (methodKey) {
			case EnumCall.KeyActivateService_activateGift_String_String: {
				return (GofFunction2<String, String>)serv::activateGift;
			}
			case EnumCall.KeyActivateService_giftCodeActivate_String_String_String: {
				return (GofFunction3<String, String, String>)serv::giftCodeActivate;
			}
			case EnumCall.KeyActivateService_isActivate_String: {
				return (GofFunction1<String>)serv::isActivate;
			}
			case EnumCall.KeyActivateService_payBackGet_String_String_String: {
				return (GofFunction3<String, String, String>)serv::payBackGet;
			}
			case EnumCall.KeyActivateService_useKey_String_String_String: {
				return (GofFunction3<String, String, String>)serv::useKey;
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
	public static KeyActivateServiceProxy newInstance() {
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
	private static KeyActivateServiceProxy createInstance(String node, String port, Object id) {
		KeyActivateServiceProxy inst = new KeyActivateServiceProxy();
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
	
	public void activateGift(String key, String channel) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.KeyActivateService_activateGift_String_String, "KeyActivateServiceProxy.activateGift", new Object[] {key, channel});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void giftCodeActivate(String humanId, String server, String giftCode) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.KeyActivateService_giftCodeActivate_String_String_String, "KeyActivateServiceProxy.giftCodeActivate", new Object[] {humanId, server, giftCode});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isActivate(String account) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.KeyActivateService_isActivate_String, "KeyActivateServiceProxy.isActivate", new Object[] {account});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void payBackGet(String humanId, String server, String account) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.KeyActivateService_payBackGet_String_String_String, "KeyActivateServiceProxy.payBackGet", new Object[] {humanId, server, account});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void useKey(String account, String key, String severId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.KeyActivateService_useKey_String_String_String, "KeyActivateServiceProxy.useKey", new Object[] {account, key, severId});
		if(immutableOnce) immutableOnce = false;
	}
}
