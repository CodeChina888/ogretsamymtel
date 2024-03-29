package game.worldsrv.compete;
                    
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
import game.worldsrv.compete.CompeteHumanObj;

@GofGenFile
public final class CompeteServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int CompeteService_challenge_long = 1;
		public static final int CompeteService_competeOpen_CompeteHumanObj = 2;
		public static final int CompeteService_getCompeteCastellan = 3;
		public static final int CompeteService_getCompeteRank = 4;
		public static final int CompeteService_getHumanRank_long = 5;
		public static final int CompeteService_getTopRank_long = 6;
		public static final int CompeteService_getTowerMirror = 7;
		public static final int CompeteService_rename_long_String = 8;
		public static final int CompeteService_swapRank_long_long_boolean_long = 9;
	}
	private static final String SERV_ID = "worldsrv.compete.CompeteService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private CompeteServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		CompeteService serv = (CompeteService)service;
		switch (methodKey) {
			case EnumCall.CompeteService_challenge_long: {
				return (GofFunction1<Long>)serv::challenge;
			}
			case EnumCall.CompeteService_competeOpen_CompeteHumanObj: {
				return (GofFunction1<CompeteHumanObj>)serv::competeOpen;
			}
			case EnumCall.CompeteService_getCompeteCastellan: {
				return (GofFunction0)serv::getCompeteCastellan;
			}
			case EnumCall.CompeteService_getCompeteRank: {
				return (GofFunction0)serv::getCompeteRank;
			}
			case EnumCall.CompeteService_getHumanRank_long: {
				return (GofFunction1<Long>)serv::getHumanRank;
			}
			case EnumCall.CompeteService_getTopRank_long: {
				return (GofFunction1<Long>)serv::getTopRank;
			}
			case EnumCall.CompeteService_getTowerMirror: {
				return (GofFunction0)serv::getTowerMirror;
			}
			case EnumCall.CompeteService_rename_long_String: {
				return (GofFunction2<Long, String>)serv::rename;
			}
			case EnumCall.CompeteService_swapRank_long_long_boolean_long: {
				return (GofFunction4<Long, Long, Boolean, Long>)serv::swapRank;
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
	public static CompeteServiceProxy newInstance() {
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
	private static CompeteServiceProxy createInstance(String node, String port, Object id) {
		CompeteServiceProxy inst = new CompeteServiceProxy();
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
	
	public void challenge(long fightId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CompeteService_challenge_long, "CompeteServiceProxy.challenge", new Object[] {fightId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void competeOpen(CompeteHumanObj newCpObj) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CompeteService_competeOpen_CompeteHumanObj, "CompeteServiceProxy.competeOpen", new Object[] {newCpObj});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getCompeteCastellan() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CompeteService_getCompeteCastellan, "CompeteServiceProxy.getCompeteCastellan", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getCompeteRank() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CompeteService_getCompeteRank, "CompeteServiceProxy.getCompeteRank", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getHumanRank(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CompeteService_getHumanRank_long, "CompeteServiceProxy.getHumanRank", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getTopRank(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CompeteService_getTopRank_long, "CompeteServiceProxy.getTopRank", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getTowerMirror() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CompeteService_getTowerMirror, "CompeteServiceProxy.getTowerMirror", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void rename(long humanId, String name) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CompeteService_rename_long_String, "CompeteServiceProxy.rename", new Object[] {humanId, name});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void swapRank(long idAtk, long idDef, boolean isWin, long recordId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.CompeteService_swapRank_long_long_boolean_long, "CompeteServiceProxy.swapRank", new Object[] {idAtk, idDef, isWin, recordId});
		if(immutableOnce) immutableOnce = false;
	}
}
