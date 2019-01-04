package game.worldsrv.tower;
                    
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
import game.worldsrv.tower.TowerRecord;

@GofGenFile
public final class TowerServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int TowerService_changeLayerCount_int = 1;
		public static final int TowerService_firstLoadTowerData = 2;
		public static final int TowerService_getLayerInfo = 3;
		public static final int TowerService_getSeasonEndTime = 4;
		public static final int TowerService_getTowerPartner_long = 5;
		public static final int TowerService_gmLoadTowerData = 6;
		public static final int TowerService_matchTowerHuman_TowerRecord = 7;
		public static final int TowerService_prevent_method = 8;
		public static final int TowerService_processFirstPass_int_int = 9;
	}
	private static final String SERV_ID = "worldsrv.tower.TowerService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private TowerServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		TowerService serv = (TowerService)service;
		switch (methodKey) {
			case EnumCall.TowerService_changeLayerCount_int: {
				return (GofFunction1<Integer>)serv::changeLayerCount;
			}
			case EnumCall.TowerService_firstLoadTowerData: {
				return (GofFunction0)serv::firstLoadTowerData;
			}
			case EnumCall.TowerService_getLayerInfo: {
				return (GofFunction0)serv::getLayerInfo;
			}
			case EnumCall.TowerService_getSeasonEndTime: {
				return (GofFunction0)serv::getSeasonEndTime;
			}
			case EnumCall.TowerService_getTowerPartner_long: {
				return (GofFunction1<Long>)serv::getTowerPartner;
			}
			case EnumCall.TowerService_gmLoadTowerData: {
				return (GofFunction0)serv::gmLoadTowerData;
			}
			case EnumCall.TowerService_matchTowerHuman_TowerRecord: {
				return (GofFunction1<TowerRecord>)serv::matchTowerHuman;
			}
			case EnumCall.TowerService_prevent_method: {
				return (GofFunction0)serv::prevent_method;
			}
			case EnumCall.TowerService_processFirstPass_int_int: {
				return (GofFunction2<Integer, Integer>)serv::processFirstPass;
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
	public static TowerServiceProxy newInstance() {
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
	private static TowerServiceProxy createInstance(String node, String port, Object id) {
		TowerServiceProxy inst = new TowerServiceProxy();
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
	
	public void changeLayerCount(int willFightLayer) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_changeLayerCount_int, "TowerServiceProxy.changeLayerCount", new Object[] {willFightLayer});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void firstLoadTowerData() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_firstLoadTowerData, "TowerServiceProxy.firstLoadTowerData", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getLayerInfo() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_getLayerInfo, "TowerServiceProxy.getLayerInfo", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getSeasonEndTime() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_getSeasonEndTime, "TowerServiceProxy.getSeasonEndTime", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getTowerPartner(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_getTowerPartner_long, "TowerServiceProxy.getTowerPartner", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void gmLoadTowerData() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_gmLoadTowerData, "TowerServiceProxy.gmLoadTowerData", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void matchTowerHuman(TowerRecord towerRecord) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_matchTowerHuman_TowerRecord, "TowerServiceProxy.matchTowerHuman", new Object[] {towerRecord});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void prevent_method() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_prevent_method, "TowerServiceProxy.prevent_method", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void processFirstPass(int layer, int diff) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TowerService_processFirstPass_int_int, "TowerServiceProxy.processFirstPass", new Object[] {layer, diff});
		if(immutableOnce) immutableOnce = false;
	}
}
