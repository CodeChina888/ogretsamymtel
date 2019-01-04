package game.worldsrv.immortalCave;
                    
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
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Partner;
import game.worldsrv.immortalCave.CaveObject;
import game.worldsrv.character.HumanMirrorObject;

@GofGenFile
public final class CaveServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int CaveService_OwnCave_long_HumanMirrorObject = 1;
		public static final int CaveService_addTime_long_int_int_int = 2;
		public static final int CaveService_canOccupy_long_int = 3;
		public static final int CaveService_canRob_long_int_int_int = 4;
		public static final int CaveService_challenge_int_int_int = 5;
		public static final int CaveService_checkAccount_long = 6;
		public static final int CaveService_getBaseMsg_int_int_int_long = 7;
		public static final int CaveService_getCaveInfo_long_int_int_int = 8;
		public static final int CaveService_getCaveLog_long = 9;
		public static final int CaveService_getCaveMemberInfo_List = 10;
		public static final int CaveService_getFreeCave_int_int = 11;
		public static final int CaveService_getInfo_long_int_List = 12;
		public static final int CaveService_getMyCaveInfo_long = 13;
		public static final int CaveService_giveUp_long_int_int_int = 14;
		public static final int CaveService_gmtest_getCaveInfo_int_int_int = 15;
		public static final int CaveService_occupy_battle_end_CaveHumanObj_int_int_int_boolean = 16;
		public static final int CaveService_rob_battle_end_CaveHumanObj_int_int_int_boolean = 17;
	}
	private static final String SERV_ID = "worldsrv.immortalCave.CaveService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private CaveServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		CaveService serv = (CaveService)service;
		switch (methodKey) {
			case EnumCall.CaveService_OwnCave_long_HumanMirrorObject: {
				return (GofFunction2<Long, HumanMirrorObject>)serv::OwnCave;
			}
			case EnumCall.CaveService_addTime_long_int_int_int: {
				return (GofFunction4<Long, Integer, Integer, Integer>)serv::addTime;
			}
			case EnumCall.CaveService_canOccupy_long_int: {
				return (GofFunction2<Long, Integer>)serv::canOccupy;
			}
			case EnumCall.CaveService_canRob_long_int_int_int: {
				return (GofFunction4<Long, Integer, Integer, Integer>)serv::canRob;
			}
			case EnumCall.CaveService_challenge_int_int_int: {
				return (GofFunction3<Integer, Integer, Integer>)serv::challenge;
			}
			case EnumCall.CaveService_checkAccount_long: {
				return (GofFunction1<Long>)serv::checkAccount;
			}
			case EnumCall.CaveService_getBaseMsg_int_int_int_long: {
				return (GofFunction4<Integer, Integer, Integer, Long>)serv::getBaseMsg;
			}
			case EnumCall.CaveService_getCaveInfo_long_int_int_int: {
				return (GofFunction4<Long, Integer, Integer, Integer>)serv::getCaveInfo;
			}
			case EnumCall.CaveService_getCaveLog_long: {
				return (GofFunction1<Long>)serv::getCaveLog;
			}
			case EnumCall.CaveService_getCaveMemberInfo_List: {
				return (GofFunction1<List>)serv::getCaveMemberInfo;
			}
			case EnumCall.CaveService_getFreeCave_int_int: {
				return (GofFunction2<Integer, Integer>)serv::getFreeCave;
			}
			case EnumCall.CaveService_getInfo_long_int_List: {
				return (GofFunction3<Long, Integer, List>)serv::getInfo;
			}
			case EnumCall.CaveService_getMyCaveInfo_long: {
				return (GofFunction1<Long>)serv::getMyCaveInfo;
			}
			case EnumCall.CaveService_giveUp_long_int_int_int: {
				return (GofFunction4<Long, Integer, Integer, Integer>)serv::giveUp;
			}
			case EnumCall.CaveService_gmtest_getCaveInfo_int_int_int: {
				return (GofFunction3<Integer, Integer, Integer>)serv::gmtest_getCaveInfo;
			}
			case EnumCall.CaveService_occupy_battle_end_CaveHumanObj_int_int_int_boolean: {
				return (GofFunction5<CaveHumanObj, Integer, Integer, Integer, Boolean>)serv::occupy_battle_end;
			}
			case EnumCall.CaveService_rob_battle_end_CaveHumanObj_int_int_int_boolean: {
				return (GofFunction5<CaveHumanObj, Integer, Integer, Integer, Boolean>)serv::rob_battle_end;
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
	public static CaveServiceProxy newInstance() {
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
	private static CaveServiceProxy createInstance(String node, String port, Object id) {
		CaveServiceProxy inst = new CaveServiceProxy();
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
	
	public void OwnCave(long caveId, HumanMirrorObject humanMirrorObject) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_OwnCave_long_HumanMirrorObject, "CaveServiceProxy.OwnCave", new Object[] {caveId, humanMirrorObject});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void addTime(long humanId, int type, int page, int index) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_addTime_long_int_int_int, "CaveServiceProxy.addTime", new Object[] {humanId, type, page, index});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void canOccupy(long humanId, int type) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_canOccupy_long_int, "CaveServiceProxy.canOccupy", new Object[] {humanId, type});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void canRob(long humanId, int type, int pages, int index) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_canRob_long_int_int_int, "CaveServiceProxy.canRob", new Object[] {humanId, type, pages, index});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void challenge(int type, int page, int index) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_challenge_int_int_int, "CaveServiceProxy.challenge", new Object[] {type, page, index});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void checkAccount(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_checkAccount_long, "CaveServiceProxy.checkAccount", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getBaseMsg(int type, int page, int index, long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_getBaseMsg_int_int_int_long, "CaveServiceProxy.getBaseMsg", new Object[] {type, page, index, humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getCaveInfo(long humanId, int type, int page, int index) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_getCaveInfo_long_int_int_int, "CaveServiceProxy.getCaveInfo", new Object[] {humanId, type, page, index});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getCaveLog(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_getCaveLog_long, "CaveServiceProxy.getCaveLog", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getCaveMemberInfo(List humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_getCaveMemberInfo_List, "CaveServiceProxy.getCaveMemberInfo", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getFreeCave(int sn, int type) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_getFreeCave_int_int, "CaveServiceProxy.getFreeCave", new Object[] {sn, type});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getInfo(long humanId, int type, List pageList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_getInfo_long_int_List, "CaveServiceProxy.getInfo", new Object[] {humanId, type, pageList});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getMyCaveInfo(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_getMyCaveInfo_long, "CaveServiceProxy.getMyCaveInfo", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void giveUp(long humanId, int type, int page, int index) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_giveUp_long_int_int_int, "CaveServiceProxy.giveUp", new Object[] {humanId, type, page, index});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void gmtest_getCaveInfo(int type, int page, int index) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_gmtest_getCaveInfo_int_int_int, "CaveServiceProxy.gmtest_getCaveInfo", new Object[] {type, page, index});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void occupy_battle_end(CaveHumanObj caveHumanObj, int type, int page, int index, boolean isWin) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_occupy_battle_end_CaveHumanObj_int_int_int_boolean, "CaveServiceProxy.occupy_battle_end", new Object[] {caveHumanObj, type, page, index, isWin});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void rob_battle_end(CaveHumanObj caveHumanObj, int type, int page, int index, boolean isWin) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CaveService_rob_battle_end_CaveHumanObj_int_int_int_boolean, "CaveServiceProxy.rob_battle_end", new Object[] {caveHumanObj, type, page, index, isWin});
		if(immutableOnce) immutableOnce = false;
	}
}
