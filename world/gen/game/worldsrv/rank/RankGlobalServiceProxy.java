package game.worldsrv.rank;
                    
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
import game.worldsrv.enumType.RankType;
import core.entity.EntityBase;
import java.util.List;
import game.worldsrv.rank.RankData;
import java.util.Set;
import game.msg.Define.ERankType;

@GofGenFile
public final class RankGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int RankGlobalService_actServerCompetitionPlanAbort = 1;
		public static final int RankGlobalService_addNew_RankData_RankType = 2;
		public static final int RankGlobalService_addNews_Set_RankType_boolean = 3;
		public static final int RankGlobalService_clearRank_RankType = 4;
		public static final int RankGlobalService_getActCombatList = 5;
		public static final int RankGlobalService_getCastellan = 6;
		public static final int RankGlobalService_getLvRank_int = 7;
		public static final int RankGlobalService_getRank_ERankType = 8;
		public static final int RankGlobalService_getRankByLootMap_List = 9;
		public static final int RankGlobalService_updateAllRankData_RankData = 10;
	}
	private static final String SERV_ID = "worldsrv.rank.RankGlobalService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private RankGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		RankGlobalService serv = (RankGlobalService)service;
		switch (methodKey) {
			case EnumCall.RankGlobalService_actServerCompetitionPlanAbort: {
				return (GofFunction0)serv::actServerCompetitionPlanAbort;
			}
			case EnumCall.RankGlobalService_addNew_RankData_RankType: {
				return (GofFunction2<RankData, RankType>)serv::addNew;
			}
			case EnumCall.RankGlobalService_addNews_Set_RankType_boolean: {
				return (GofFunction3<Set, RankType, Boolean>)serv::addNews;
			}
			case EnumCall.RankGlobalService_clearRank_RankType: {
				return (GofFunction1<RankType>)serv::clearRank;
			}
			case EnumCall.RankGlobalService_getActCombatList: {
				return (GofFunction0)serv::getActCombatList;
			}
			case EnumCall.RankGlobalService_getCastellan: {
				return (GofFunction0)serv::getCastellan;
			}
			case EnumCall.RankGlobalService_getLvRank_int: {
				return (GofFunction1<Integer>)serv::getLvRank;
			}
			case EnumCall.RankGlobalService_getRank_ERankType: {
				return (GofFunction1<ERankType>)serv::getRank;
			}
			case EnumCall.RankGlobalService_getRankByLootMap_List: {
				return (GofFunction1<List>)serv::getRankByLootMap;
			}
			case EnumCall.RankGlobalService_updateAllRankData_RankData: {
				return (GofFunction1<RankData>)serv::updateAllRankData;
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
	public static RankGlobalServiceProxy newInstance() {
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
	private static RankGlobalServiceProxy createInstance(String node, String port, Object id) {
		RankGlobalServiceProxy inst = new RankGlobalServiceProxy();
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
	
	public void actServerCompetitionPlanAbort() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_actServerCompetitionPlanAbort, "RankGlobalServiceProxy.actServerCompetitionPlanAbort", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void addNew(RankData data, RankType type) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_addNew_RankData_RankType, "RankGlobalServiceProxy.addNew", new Object[] {data, type});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void addNews(Set data, RankType type, boolean isNeedUpdate) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_addNews_Set_RankType_boolean, "RankGlobalServiceProxy.addNews", new Object[] {data, type, isNeedUpdate});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void clearRank(RankType type) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_clearRank_RankType, "RankGlobalServiceProxy.clearRank", new Object[] {type});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getActCombatList() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_getActCombatList, "RankGlobalServiceProxy.getActCombatList", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getCastellan() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_getCastellan, "RankGlobalServiceProxy.getCastellan", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getLvRank(int topCount) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_getLvRank_int, "RankGlobalServiceProxy.getLvRank", new Object[] {topCount});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getRank(ERankType rankType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_getRank_ERankType, "RankGlobalServiceProxy.getRank", new Object[] {rankType});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getRankByLootMap(List humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_getRankByLootMap_List, "RankGlobalServiceProxy.getRankByLootMap", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateAllRankData(RankData data) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.RankGlobalService_updateAllRankData_RankData, "RankGlobalServiceProxy.updateAllRankData", new Object[] {data});
		if(immutableOnce) immutableOnce = false;
	}
}
