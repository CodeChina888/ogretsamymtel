package game.worldsrv.friend;
                    
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
import game.worldsrv.character.HumanObject;
import java.util.List;
import java.util.Set;
import game.worldsrv.entity.Human;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.friend.FriendInfo;

@GofGenFile
public final class FriendServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int FriendService_changeObject_Human_List_int = 1;
		public static final int FriendService_createFriend_Human = 2;
		public static final int FriendService_disposeHumanFriend_HumanGlobalInfo_long_long_int_long = 3;
		public static final int FriendService_getFriendRecord_long = 4;
		public static final int FriendService_getListFriend_Set = 5;
		public static final int FriendService_loginFinishOnline_Human_Set_Set = 6;
		public static final int FriendService_recommendFriend_List = 7;
		public static final int FriendService_searchFriend_String = 8;
		public static final int FriendService_synApplyFriend_long_int_int_boolean = 9;
	}
	private static final String SERV_ID = "worldsrv.friend.FriendService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private FriendServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		FriendService serv = (FriendService)service;
		switch (methodKey) {
			case EnumCall.FriendService_changeObject_Human_List_int: {
				return (GofFunction3<Human, List, Integer>)serv::changeObject;
			}
			case EnumCall.FriendService_createFriend_Human: {
				return (GofFunction1<Human>)serv::createFriend;
			}
			case EnumCall.FriendService_disposeHumanFriend_HumanGlobalInfo_long_long_int_long: {
				return (GofFunction5<HumanGlobalInfo, Long, Long, Integer, Long>)serv::disposeHumanFriend;
			}
			case EnumCall.FriendService_getFriendRecord_long: {
				return (GofFunction1<Long>)serv::getFriendRecord;
			}
			case EnumCall.FriendService_getListFriend_Set: {
				return (GofFunction1<Set>)serv::getListFriend;
			}
			case EnumCall.FriendService_loginFinishOnline_Human_Set_Set: {
				return (GofFunction3<Human, Set, Set>)serv::loginFinishOnline;
			}
			case EnumCall.FriendService_recommendFriend_List: {
				return (GofFunction1<List>)serv::recommendFriend;
			}
			case EnumCall.FriendService_searchFriend_String: {
				return (GofFunction1<String>)serv::searchFriend;
			}
			case EnumCall.FriendService_synApplyFriend_long_int_int_boolean: {
				return (GofFunction4<Long, Integer, Integer, Boolean>)serv::synApplyFriend;
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
	public static FriendServiceProxy newInstance() {
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
	private static FriendServiceProxy createInstance(String node, String port, Object id) {
		FriendServiceProxy inst = new FriendServiceProxy();
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
	@SuppressWarnings("rawtypes")
	public void changeObject(Human human, List ids, int combat) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_changeObject_Human_List_int, "FriendServiceProxy.changeObject", new Object[] {human, ids, combat});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createFriend(Human human) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_createFriend_Human, "FriendServiceProxy.createFriend", new Object[] {human});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void disposeHumanFriend(HumanGlobalInfo info, long myId, long humanId, int type, long time) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_disposeHumanFriend_HumanGlobalInfo_long_long_int_long, "FriendServiceProxy.disposeHumanFriend", new Object[] {info, myId, humanId, type, time});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getFriendRecord(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_getFriendRecord_long, "FriendServiceProxy.getFriendRecord", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getListFriend(Set humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_getListFriend_Set, "FriendServiceProxy.getListFriend", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void loginFinishOnline(Human human, Set friendSize, Set applySize) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_loginFinishOnline_Human_Set_Set, "FriendServiceProxy.loginFinishOnline", new Object[] {human, friendSize, applySize});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void recommendFriend(List humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_recommendFriend_List, "FriendServiceProxy.recommendFriend", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void searchFriend(String name) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_searchFriend_String, "FriendServiceProxy.searchFriend", new Object[] {name});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void synApplyFriend(long id, int applySize, int friendSize, boolean isLine) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.FriendService_synApplyFriend_long_int_int_boolean, "FriendServiceProxy.synApplyFriend", new Object[] {id, applySize, friendSize, isLine});
		if(immutableOnce) immutableOnce = false;
	}
}
