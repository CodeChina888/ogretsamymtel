package crosssrv.stage;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import crosssrv.character.CombatantObject;
import java.util.List;
import crosssrv.entity.FightRecord;
import game.worldsrv.character.HumanMirrorObject;

@GofGenFile
public final class CrossStageServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int CrossStageService_createStageByFightRecord_FightRecord_long = 1;
		public static final int CrossStageService_createStageCommon_HumanMirrorObject_long_int_int_int = 2;
		public static final int CrossStageService_createStageCompete_HumanMirrorObject_long_int_int_int = 3;
		public static final int CrossStageService_createStageDailyRep_HumanMirrorObject_long_int_int_int = 4;
		public static final int CrossStageService_createStageInstance_HumanMirrorObject_long_int_int_int = 5;
		public static final int CrossStageService_createStageNewbie_long_long_int_int_int = 6;
		public static final int CrossStageService_createStageTower_HumanMirrorObject_long_int_int_int = 7;
		public static final int CrossStageService_createStageWorldBoss_HumanMirrorObject_long_int_int_int = 8;
		public static final int CrossStageService_destroy_long = 9;
		public static final int CrossStageService_getRecordFriendBoss_HumanMirrorObject_long_int_int_int = 10;
		public static final int CrossStageService_getRecordGuildRep_HumanMirrorObject_long_int_int_int = 11;
		public static final int CrossStageService_leaveStage_long_long = 12;
		public static final int CrossStageService_waitDestroy_long = 13;
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
	private CrossStageServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		CrossStageService serv = (CrossStageService)service;
		switch (methodKey) {
			case EnumCall.CrossStageService_createStageByFightRecord_FightRecord_long: {
				return (GofFunction2<FightRecord, Long>)serv::createStageByFightRecord;
			}
			case EnumCall.CrossStageService_createStageCommon_HumanMirrorObject_long_int_int_int: {
				return (GofFunction5<HumanMirrorObject, Long, Integer, Integer, Integer>)serv::createStageCommon;
			}
			case EnumCall.CrossStageService_createStageCompete_HumanMirrorObject_long_int_int_int: {
				return (GofFunction5<HumanMirrorObject, Long, Integer, Integer, Integer>)serv::createStageCompete;
			}
			case EnumCall.CrossStageService_createStageDailyRep_HumanMirrorObject_long_int_int_int: {
				return (GofFunction5<HumanMirrorObject, Long, Integer, Integer, Integer>)serv::createStageDailyRep;
			}
			case EnumCall.CrossStageService_createStageInstance_HumanMirrorObject_long_int_int_int: {
				return (GofFunction5<HumanMirrorObject, Long, Integer, Integer, Integer>)serv::createStageInstance;
			}
			case EnumCall.CrossStageService_createStageNewbie_long_long_int_int_int: {
				return (GofFunction5<Long, Long, Integer, Integer, Integer>)serv::createStageNewbie;
			}
			case EnumCall.CrossStageService_createStageTower_HumanMirrorObject_long_int_int_int: {
				return (GofFunction5<HumanMirrorObject, Long, Integer, Integer, Integer>)serv::createStageTower;
			}
			case EnumCall.CrossStageService_createStageWorldBoss_HumanMirrorObject_long_int_int_int: {
				return (GofFunction5<HumanMirrorObject, Long, Integer, Integer, Integer>)serv::createStageWorldBoss;
			}
			case EnumCall.CrossStageService_destroy_long: {
				return (GofFunction1<Long>)serv::destroy;
			}
			case EnumCall.CrossStageService_getRecordFriendBoss_HumanMirrorObject_long_int_int_int: {
				return (GofFunction5<HumanMirrorObject, Long, Integer, Integer, Integer>)serv::getRecordFriendBoss;
			}
			case EnumCall.CrossStageService_getRecordGuildRep_HumanMirrorObject_long_int_int_int: {
				return (GofFunction5<HumanMirrorObject, Long, Integer, Integer, Integer>)serv::getRecordGuildRep;
			}
			case EnumCall.CrossStageService_leaveStage_long_long: {
				return (GofFunction2<Long, Long>)serv::leaveStage;
			}
			case EnumCall.CrossStageService_waitDestroy_long: {
				return (GofFunction1<Long>)serv::waitDestroy;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static CrossStageServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static CrossStageServiceProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param localPort
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static CrossStageServiceProxy createInstance(String node, String port, Object id) {
		CrossStageServiceProxy inst = new CrossStageServiceProxy();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
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
	
	public void createStageByFightRecord(FightRecord fightRecord, long stageID) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_createStageByFightRecord_FightRecord_long, "CrossStageServiceProxy.createStageByFightRecord", new Object[] {fightRecord, stageID});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageCommon(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_createStageCommon_HumanMirrorObject_long_int_int_int, "CrossStageServiceProxy.createStageCommon", new Object[] {humanMirrorObj, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageCompete(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_createStageCompete_HumanMirrorObject_long_int_int_int, "CrossStageServiceProxy.createStageCompete", new Object[] {humanMirrorObj, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageDailyRep(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_createStageDailyRep_HumanMirrorObject_long_int_int_int, "CrossStageServiceProxy.createStageDailyRep", new Object[] {humanMirrorObj, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageInstance(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_createStageInstance_HumanMirrorObject_long_int_int_int, "CrossStageServiceProxy.createStageInstance", new Object[] {humanMirrorObj, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageNewbie(long combatantId, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_createStageNewbie_long_long_int_int_int, "CrossStageServiceProxy.createStageNewbie", new Object[] {combatantId, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageTower(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_createStageTower_HumanMirrorObject_long_int_int_int, "CrossStageServiceProxy.createStageTower", new Object[] {humanMirrorObj, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageWorldBoss(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_createStageWorldBoss_HumanMirrorObject_long_int_int_int, "CrossStageServiceProxy.createStageWorldBoss", new Object[] {humanMirrorObj, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void destroy(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_destroy_long, "CrossStageServiceProxy.destroy", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getRecordFriendBoss(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_getRecordFriendBoss_HumanMirrorObject_long_int_int_int, "CrossStageServiceProxy.getRecordFriendBoss", new Object[] {humanMirrorObj, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getRecordGuildRep(HumanMirrorObject humanMirrorObj, long stageID, int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_getRecordGuildRep_HumanMirrorObject_long_int_int_int, "CrossStageServiceProxy.getRecordGuildRep", new Object[] {humanMirrorObj, stageID, stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void leaveStage(long stageId, long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_leaveStage_long_long, "CrossStageServiceProxy.leaveStage", new Object[] {stageId, humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void waitDestroy(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.CrossStageService_waitDestroy_long, "CrossStageServiceProxy.waitDestroy", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
}
