package game.worldsrv.stage;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.fightrecord.RecordInfo;
import core.support.Param;

@GofGenFile
public final class StageServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int StageService_createStageCave_int_int_int_Param = 1;
		public static final int StageService_createStageCommon_long_int_int = 2;
		public static final int StageService_createStageCompete_int_int_int_Param = 3;
		public static final int StageService_createStageGuildInst_int_int_int_Param = 4;
		public static final int StageService_createStageInstBoss_int_int_int = 5;
		public static final int StageService_createStageInstLootMap_int_int_int_int_int_int = 6;
		public static final int StageService_createStageInstMoba_int_int_boolean = 7;
		public static final int StageService_createStageInstPVE_int_int_boolean = 8;
		public static final int StageService_createStageInstPVP_int_int_boolean = 9;
		public static final int StageService_createStageInstRes_int_int_int_int = 10;
		public static final int StageService_createStageInstance_int_int_int = 11;
		public static final int StageService_createStagePKHuman_int_int_int_Param = 12;
		public static final int StageService_createStagePKMirror_int_int_int_Param = 13;
		public static final int StageService_createStageReplay_RecordInfo_int_int = 14;
		public static final int StageService_createStageTower_int_int_int_int_int_Param = 15;
		public static final int StageService_destroy_long = 16;
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
	private StageServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		StageService serv = (StageService)service;
		switch (methodKey) {
			case EnumCall.StageService_createStageCave_int_int_int_Param: {
				return (GofFunction4<Integer, Integer, Integer, Param>)serv::createStageCave;
			}
			case EnumCall.StageService_createStageCommon_long_int_int: {
				return (GofFunction3<Long, Integer, Integer>)serv::createStageCommon;
			}
			case EnumCall.StageService_createStageCompete_int_int_int_Param: {
				return (GofFunction4<Integer, Integer, Integer, Param>)serv::createStageCompete;
			}
			case EnumCall.StageService_createStageGuildInst_int_int_int_Param: {
				return (GofFunction4<Integer, Integer, Integer, Param>)serv::createStageGuildInst;
			}
			case EnumCall.StageService_createStageInstBoss_int_int_int: {
				return (GofFunction3<Integer, Integer, Integer>)serv::createStageInstBoss;
			}
			case EnumCall.StageService_createStageInstLootMap_int_int_int_int_int_int: {
				return (GofFunction6<Integer, Integer, Integer, Integer, Integer, Integer>)serv::createStageInstLootMap;
			}
			case EnumCall.StageService_createStageInstMoba_int_int_boolean: {
				return (GofFunction3<Integer, Integer, Boolean>)serv::createStageInstMoba;
			}
			case EnumCall.StageService_createStageInstPVE_int_int_boolean: {
				return (GofFunction3<Integer, Integer, Boolean>)serv::createStageInstPVE;
			}
			case EnumCall.StageService_createStageInstPVP_int_int_boolean: {
				return (GofFunction3<Integer, Integer, Boolean>)serv::createStageInstPVP;
			}
			case EnumCall.StageService_createStageInstRes_int_int_int_int: {
				return (GofFunction4<Integer, Integer, Integer, Integer>)serv::createStageInstRes;
			}
			case EnumCall.StageService_createStageInstance_int_int_int: {
				return (GofFunction3<Integer, Integer, Integer>)serv::createStageInstance;
			}
			case EnumCall.StageService_createStagePKHuman_int_int_int_Param: {
				return (GofFunction4<Integer, Integer, Integer, Param>)serv::createStagePKHuman;
			}
			case EnumCall.StageService_createStagePKMirror_int_int_int_Param: {
				return (GofFunction4<Integer, Integer, Integer, Param>)serv::createStagePKMirror;
			}
			case EnumCall.StageService_createStageReplay_RecordInfo_int_int: {
				return (GofFunction3<RecordInfo, Integer, Integer>)serv::createStageReplay;
			}
			case EnumCall.StageService_createStageTower_int_int_int_int_int_Param: {
				return (GofFunction6<Integer, Integer, Integer, Integer, Integer, Param>)serv::createStageTower;
			}
			case EnumCall.StageService_destroy_long: {
				return (GofFunction1<Long>)serv::destroy;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static StageServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static StageServiceProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static StageServiceProxy createInstance(String node, String port, Object id) {
		StageServiceProxy inst = new StageServiceProxy();
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
	
	public void createStageCave(int stageSn, int mapSn, int fightType, Param param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageCave_int_int_int_Param, "StageServiceProxy.createStageCave", new Object[] {stageSn, mapSn, fightType, param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageCommon(long stageID, int mapSn, int lineNum) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageCommon_long_int_int, "StageServiceProxy.createStageCommon", new Object[] {stageID, mapSn, lineNum});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageCompete(int stageSn, int mapSn, int fightType, Param param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageCompete_int_int_int_Param, "StageServiceProxy.createStageCompete", new Object[] {stageSn, mapSn, fightType, param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageGuildInst(int stageSn, int mapSn, int fightType, Param param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageGuildInst_int_int_int_Param, "StageServiceProxy.createStageGuildInst", new Object[] {stageSn, mapSn, fightType, param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageInstBoss(int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageInstBoss_int_int_int, "StageServiceProxy.createStageInstBoss", new Object[] {stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageInstLootMap(int stageSn, int mapSn, int fightType, int humanNumber, int lvType, int lootMapSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageInstLootMap_int_int_int_int_int_int, "StageServiceProxy.createStageInstLootMap", new Object[] {stageSn, mapSn, fightType, humanNumber, lvType, lootMapSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageInstMoba(int stageSn, int mapSn, boolean isMonsterAddProp) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageInstMoba_int_int_boolean, "StageServiceProxy.createStageInstMoba", new Object[] {stageSn, mapSn, isMonsterAddProp});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageInstPVE(int stageSn, int mapSn, boolean isMonsterAddProp) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageInstPVE_int_int_boolean, "StageServiceProxy.createStageInstPVE", new Object[] {stageSn, mapSn, isMonsterAddProp});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageInstPVP(int stageSn, int mapSn, boolean isMonsterAddProp) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageInstPVP_int_int_boolean, "StageServiceProxy.createStageInstPVP", new Object[] {stageSn, mapSn, isMonsterAddProp});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageInstRes(int stageSn, int mapSn, int fightType, int instResSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageInstRes_int_int_int_int, "StageServiceProxy.createStageInstRes", new Object[] {stageSn, mapSn, fightType, instResSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageInstance(int stageSn, int mapSn, int fightType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageInstance_int_int_int, "StageServiceProxy.createStageInstance", new Object[] {stageSn, mapSn, fightType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStagePKHuman(int stageSn, int mapSn, int fightType, Param param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStagePKHuman_int_int_int_Param, "StageServiceProxy.createStagePKHuman", new Object[] {stageSn, mapSn, fightType, param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStagePKMirror(int stageSn, int mapSn, int fightType, Param param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStagePKMirror_int_int_int_Param, "StageServiceProxy.createStagePKMirror", new Object[] {stageSn, mapSn, fightType, param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageReplay(RecordInfo record, int stageSn, int mapSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageReplay_RecordInfo_int_int, "StageServiceProxy.createStageReplay", new Object[] {record, stageSn, mapSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createStageTower(int stageSn, int mapSn, int fightType, int fightLayer, int selDiff, Param param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_createStageTower_int_int_int_int_int_Param, "StageServiceProxy.createStageTower", new Object[] {stageSn, mapSn, fightType, fightLayer, selDiff, param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void destroy(long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.StageService_destroy_long, "StageServiceProxy.destroy", new Object[] {stageId});
		if(immutableOnce) immutableOnce = false;
	}
}
