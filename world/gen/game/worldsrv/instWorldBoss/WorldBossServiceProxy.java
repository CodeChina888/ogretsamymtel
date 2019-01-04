package game.worldsrv.instWorldBoss;
                    
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
import game.worldsrv.entity.Human;
import game.worldsrv.entity.WorldBoss;
import java.util.List;

@GofGenFile
public final class WorldBossServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int WorldBossService_getBossInfo_int = 1;
		public static final int WorldBossService_getHarmRankList_int_long = 2;
		public static final int WorldBossService_getOtherHuman_long_int = 3;
		public static final int WorldBossService_getUponRecordNames_int = 4;
		public static final int WorldBossService_humanEnter_Human_int = 5;
		public static final int WorldBossService_humanLeave_long_int = 6;
		public static final int WorldBossService_isOpen = 7;
		public static final int WorldBossService_reduceHp_Human_int_long_List = 8;
	}
	private static final String SERV_ID = "worldsrv.instWorldBoss.WorldBossService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private WorldBossServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		WorldBossService serv = (WorldBossService)service;
		switch (methodKey) {
			case EnumCall.WorldBossService_getBossInfo_int: {
				return (GofFunction1<Integer>)serv::getBossInfo;
			}
			case EnumCall.WorldBossService_getHarmRankList_int_long: {
				return (GofFunction2<Integer, Long>)serv::getHarmRankList;
			}
			case EnumCall.WorldBossService_getOtherHuman_long_int: {
				return (GofFunction2<Long, Integer>)serv::getOtherHuman;
			}
			case EnumCall.WorldBossService_getUponRecordNames_int: {
				return (GofFunction1<Integer>)serv::getUponRecordNames;
			}
			case EnumCall.WorldBossService_humanEnter_Human_int: {
				return (GofFunction2<Human, Integer>)serv::humanEnter;
			}
			case EnumCall.WorldBossService_humanLeave_long_int: {
				return (GofFunction2<Long, Integer>)serv::humanLeave;
			}
			case EnumCall.WorldBossService_isOpen: {
				return (GofFunction0)serv::isOpen;
			}
			case EnumCall.WorldBossService_reduceHp_Human_int_long_List: {
				return (GofFunction4<Human, Integer, Long, List>)serv::reduceHp;
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
	public static WorldBossServiceProxy newInstance() {
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
	private static WorldBossServiceProxy createInstance(String node, String port, Object id) {
		WorldBossServiceProxy inst = new WorldBossServiceProxy();
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
	
	public void getBossInfo(int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.WorldBossService_getBossInfo_int, "WorldBossServiceProxy.getBossInfo", new Object[] {actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getHarmRankList(int actInstSn, long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.WorldBossService_getHarmRankList_int_long, "WorldBossServiceProxy.getHarmRankList", new Object[] {actInstSn, humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getOtherHuman(long humanId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.WorldBossService_getOtherHuman_long_int, "WorldBossServiceProxy.getOtherHuman", new Object[] {humanId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getUponRecordNames(int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.WorldBossService_getUponRecordNames_int, "WorldBossServiceProxy.getUponRecordNames", new Object[] {actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void humanEnter(Human human, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.WorldBossService_humanEnter_Human_int, "WorldBossServiceProxy.humanEnter", new Object[] {human, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void humanLeave(long humanId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.WorldBossService_humanLeave_long_int, "WorldBossServiceProxy.humanLeave", new Object[] {humanId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isOpen() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.WorldBossService_isOpen, "WorldBossServiceProxy.isOpen", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void reduceHp(Human human, int actInstSn, long harmTotal, List harmList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.WorldBossService_reduceHp_Human_int_long_List, "WorldBossServiceProxy.reduceHp", new Object[] {human, actInstSn, harmTotal, harmList});
		if(immutableOnce) immutableOnce = false;
	}
}
