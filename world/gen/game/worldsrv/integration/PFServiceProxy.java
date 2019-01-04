package game.worldsrv.integration;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;

@GofGenFile
public final class PFServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int PFService_PF1_payNotice_String = 1;
		public static final int PFService_PF2_gmNotice_String = 2;
		public static final int PFService_PF3_monitorBanNotice_String = 3;
		public static final int PFService_PF4_monitorQueryNotice_String = 4;
		public static final int PFService_PF5_monitorQueryOnlineNum_String = 5;
		public static final int PFService_PF6_monitorQueryLvRank_String = 6;
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
	private PFServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		PFService serv = (PFService)service;
		switch (methodKey) {
			case EnumCall.PFService_PF1_payNotice_String: {
				return (GofFunction1<String>)serv::PF1_payNotice;
			}
			case EnumCall.PFService_PF2_gmNotice_String: {
				return (GofFunction1<String>)serv::PF2_gmNotice;
			}
			case EnumCall.PFService_PF3_monitorBanNotice_String: {
				return (GofFunction1<String>)serv::PF3_monitorBanNotice;
			}
			case EnumCall.PFService_PF4_monitorQueryNotice_String: {
				return (GofFunction1<String>)serv::PF4_monitorQueryNotice;
			}
			case EnumCall.PFService_PF5_monitorQueryOnlineNum_String: {
				return (GofFunction1<String>)serv::PF5_monitorQueryOnlineNum;
			}
			case EnumCall.PFService_PF6_monitorQueryLvRank_String: {
				return (GofFunction1<String>)serv::PF6_monitorQueryLvRank;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static PFServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static PFServiceProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static PFServiceProxy createInstance(String node, String port, Object id) {
		PFServiceProxy inst = new PFServiceProxy();
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
	
	public void PF1_payNotice(String msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PFService_PF1_payNotice_String, "PFServiceProxy.PF1_payNotice", new Object[] {msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void PF2_gmNotice(String msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PFService_PF2_gmNotice_String, "PFServiceProxy.PF2_gmNotice", new Object[] {msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void PF3_monitorBanNotice(String msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PFService_PF3_monitorBanNotice_String, "PFServiceProxy.PF3_monitorBanNotice", new Object[] {msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void PF4_monitorQueryNotice(String msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PFService_PF4_monitorQueryNotice_String, "PFServiceProxy.PF4_monitorQueryNotice", new Object[] {msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void PF5_monitorQueryOnlineNum(String msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PFService_PF5_monitorQueryOnlineNum_String, "PFServiceProxy.PF5_monitorQueryOnlineNum", new Object[] {msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void PF6_monitorQueryLvRank(String msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.PFService_PF6_monitorQueryLvRank_String, "PFServiceProxy.PF6_monitorQueryLvRank", new Object[] {msg});
		if(immutableOnce) immutableOnce = false;
	}
}
