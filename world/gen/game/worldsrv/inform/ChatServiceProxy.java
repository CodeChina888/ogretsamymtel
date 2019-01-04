package game.worldsrv.inform;
                    
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
import java.util.Map;

@GofGenFile
public final class ChatServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ChatService_GMNotice_long_int_int_String = 1;
		public static final int ChatService_addChat_Map_int_int = 2;
		public static final int ChatService_onLogworldChat = 3;
		public static final int ChatService_uploadChat = 4;
	}
	private static final String SERV_ID = "worldsrv.inform.ChatService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private ChatServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		ChatService serv = (ChatService)service;
		switch (methodKey) {
			case EnumCall.ChatService_GMNotice_long_int_int_String: {
				return (GofFunction4<Long, Integer, Integer, String>)serv::GMNotice;
			}
			case EnumCall.ChatService_addChat_Map_int_int: {
				return (GofFunction3<Map, Integer, Integer>)serv::addChat;
			}
			case EnumCall.ChatService_onLogworldChat: {
				return (GofFunction0)serv::onLogworldChat;
			}
			case EnumCall.ChatService_uploadChat: {
				return (GofFunction0)serv::uploadChat;
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
	public static ChatServiceProxy newInstance() {
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
	private static ChatServiceProxy createInstance(String node, String port, Object id) {
		ChatServiceProxy inst = new ChatServiceProxy();
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
	
	public void GMNotice(long time, int split, int count, String content) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ChatService_GMNotice_long_int_int_String, "ChatServiceProxy.GMNotice", new Object[] {time, split, count, content});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void addChat(Map chat, int level, int vip) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ChatService_addChat_Map_int_int, "ChatServiceProxy.addChat", new Object[] {chat, level, vip});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void onLogworldChat() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ChatService_onLogworldChat, "ChatServiceProxy.onLogworldChat", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void uploadChat() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ChatService_uploadChat, "ChatServiceProxy.uploadChat", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
}
