package game.worldsrv.activity;
                    
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
import game.worldsrv.character.HumanObject;

@GofGenFile
public final class ActivityServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ActivityService_getActivityInfo_int = 1;
		public static final int ActivityService_getActivityInfo_List = 2;
		public static final int ActivityService_getAllActivityInfo = 3;
		public static final int ActivityService_getAllShowActivityInfo = 4;
		public static final int ActivityService_getOnlineActivityInfo = 5;
		public static final int ActivityService_getShowActivityInfo_List = 6;
		public static final int ActivityService_getaAtivityGlobal = 7;
		public static final int ActivityService_sendActivityUpdateInfo_List = 8;
		public static final int ActivityService_setAtivityGlobalNum_int = 9;
		public static final int ActivityService_triggerDailyHour_int = 10;
	}
	private static final String SERV_ID = "worldsrv.activity.ActivityService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private ActivityServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		ActivityService serv = (ActivityService)service;
		switch (methodKey) {
			case EnumCall.ActivityService_getActivityInfo_int: {
				return (GofFunction1<Integer>)serv::getActivityInfo;
			}
			case EnumCall.ActivityService_getActivityInfo_List: {
				return (GofFunction1<List>)serv::getActivityInfo;
			}
			case EnumCall.ActivityService_getAllActivityInfo: {
				return (GofFunction0)serv::getAllActivityInfo;
			}
			case EnumCall.ActivityService_getAllShowActivityInfo: {
				return (GofFunction0)serv::getAllShowActivityInfo;
			}
			case EnumCall.ActivityService_getOnlineActivityInfo: {
				return (GofFunction0)serv::getOnlineActivityInfo;
			}
			case EnumCall.ActivityService_getShowActivityInfo_List: {
				return (GofFunction1<List>)serv::getShowActivityInfo;
			}
			case EnumCall.ActivityService_getaAtivityGlobal: {
				return (GofFunction0)serv::getaAtivityGlobal;
			}
			case EnumCall.ActivityService_sendActivityUpdateInfo_List: {
				return (GofFunction1<List>)serv::sendActivityUpdateInfo;
			}
			case EnumCall.ActivityService_setAtivityGlobalNum_int: {
				return (GofFunction1<Integer>)serv::setAtivityGlobalNum;
			}
			case EnumCall.ActivityService_triggerDailyHour_int: {
				return (GofFunction1<Integer>)serv::triggerDailyHour;
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
	public static ActivityServiceProxy newInstance() {
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
	private static ActivityServiceProxy createInstance(String node, String port, Object id) {
		ActivityServiceProxy inst = new ActivityServiceProxy();
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
	
	public void getActivityInfo(int id) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_getActivityInfo_int, "ActivityServiceProxy.getActivityInfo", new Object[] {id});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getActivityInfo(List ids) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_getActivityInfo_List, "ActivityServiceProxy.getActivityInfo", new Object[] {ids});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getAllActivityInfo() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_getAllActivityInfo, "ActivityServiceProxy.getAllActivityInfo", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getAllShowActivityInfo() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_getAllShowActivityInfo, "ActivityServiceProxy.getAllShowActivityInfo", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getOnlineActivityInfo() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_getOnlineActivityInfo, "ActivityServiceProxy.getOnlineActivityInfo", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getShowActivityInfo(List ids) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_getShowActivityInfo_List, "ActivityServiceProxy.getShowActivityInfo", new Object[] {ids});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getaAtivityGlobal() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_getaAtivityGlobal, "ActivityServiceProxy.getaAtivityGlobal", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void sendActivityUpdateInfo(List ids) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_sendActivityUpdateInfo_List, "ActivityServiceProxy.sendActivityUpdateInfo", new Object[] {ids});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setAtivityGlobalNum(int i) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_setAtivityGlobalNum_int, "ActivityServiceProxy.setAtivityGlobalNum", new Object[] {i});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void triggerDailyHour(int hour) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.ActivityService_triggerDailyHour_int, "ActivityServiceProxy.triggerDailyHour", new Object[] {hour});
		if(immutableOnce) immutableOnce = false;
	}
}
