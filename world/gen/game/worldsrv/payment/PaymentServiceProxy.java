package game.worldsrv.payment;
                    
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
import game.worldsrv.entity.IOSPayOrder;

@GofGenFile
public final class PaymentServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int PaymentService_addChargePresent_long_String_int = 1;
		public static final int PaymentService_addIOSPayOrder_long_int_String_String = 2;
		public static final int PaymentService_generatePresentTw_long_String_long = 3;
		public static final int PaymentService_payNotifyHttps_String = 4;
		public static final int PaymentService_takeChargePresent_int_long = 5;
	}
	private static final String SERV_ID = "worldsrv.payment.PaymentService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private PaymentServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		PaymentService serv = (PaymentService)service;
		switch (methodKey) {
			case EnumCall.PaymentService_addChargePresent_long_String_int: {
				return (GofFunction3<Long, String, Integer>)serv::addChargePresent;
			}
			case EnumCall.PaymentService_addIOSPayOrder_long_int_String_String: {
				return (GofFunction4<Long, Integer, String, String>)serv::addIOSPayOrder;
			}
			case EnumCall.PaymentService_generatePresentTw_long_String_long: {
				return (GofFunction3<Long, String, Long>)serv::generatePresentTw;
			}
			case EnumCall.PaymentService_payNotifyHttps_String: {
				return (GofFunction1<String>)serv::payNotifyHttps;
			}
			case EnumCall.PaymentService_takeChargePresent_int_long: {
				return (GofFunction2<Integer, Long>)serv::takeChargePresent;
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
	public static PaymentServiceProxy newInstance() {
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
	private static PaymentServiceProxy createInstance(String node, String port, Object id) {
		PaymentServiceProxy inst = new PaymentServiceProxy();
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
	
	public void addChargePresent(long humanId, String humanName, int sn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PaymentService_addChargePresent_long_String_int, "PaymentServiceProxy.addChargePresent", new Object[] {humanId, humanName, sn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void addIOSPayOrder(long humanId, int sn, String order, String receiptData) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PaymentService_addIOSPayOrder_long_int_String_String, "PaymentServiceProxy.addIOSPayOrder", new Object[] {humanId, sn, order, receiptData});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void generatePresentTw(long humanId, String humanName, long gold) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PaymentService_generatePresentTw_long_String_long, "PaymentServiceProxy.generatePresentTw", new Object[] {humanId, humanName, gold});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void payNotifyHttps(String msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PaymentService_payNotifyHttps_String, "PaymentServiceProxy.payNotifyHttps", new Object[] {msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void takeChargePresent(int id, long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PaymentService_takeChargePresent_int_long, "PaymentServiceProxy.takeChargePresent", new Object[] {id, humanId});
		if(immutableOnce) immutableOnce = false;
	}
}
