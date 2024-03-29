package game.worldsrv.mail;
                    
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
import game.worldsrv.produce.ProduceVo;

@GofGenFile
public final class FillMailServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int FillMailService_deleteMail_String = 1;
		public static final int FillMailService_loginCheck_long_String_int_long = 2;
		public static final int FillMailService_sendMail_String_String_String_String_long_long_String = 3;
	}
	private static final String SERV_ID = "worldsrv.mail.FillMailService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private FillMailServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		FillMailService serv = (FillMailService)service;
		switch (methodKey) {
			case EnumCall.FillMailService_deleteMail_String: {
				return (GofFunction1<String>)serv::deleteMail;
			}
			case EnumCall.FillMailService_loginCheck_long_String_int_long: {
				return (GofFunction4<Long, String, Integer, Long>)serv::loginCheck;
			}
			case EnumCall.FillMailService_sendMail_String_String_String_String_long_long_String: {
				return (GofFunction7<String, String, String, String, Long, Long, String>)serv::sendMail;
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
	public static FillMailServiceProxy newInstance() {
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
	private static FillMailServiceProxy createInstance(String node, String port, Object id) {
		FillMailServiceProxy inst = new FillMailServiceProxy();
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
	
	public void deleteMail(String eventKey) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FillMailService_deleteMail_String, "FillMailServiceProxy.deleteMail", new Object[] {eventKey});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void loginCheck(long humanId, String fillMailJson, int serverId, long timeCreate) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FillMailService_loginCheck_long_String_int_long, "FillMailServiceProxy.loginCheck", new Object[] {humanId, fillMailJson, serverId, timeCreate});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendMail(String itemSn, String itemNum, String title, String content, long startTime, long endTime, String eventKey) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FillMailService_sendMail_String_String_String_String_long_long_String, "FillMailServiceProxy.sendMail", new Object[] {itemSn, itemNum, title, content, startTime, endTime, eventKey});
		if(immutableOnce) immutableOnce = false;
	}
}
