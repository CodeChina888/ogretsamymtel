package game.worldsrv.human;
                    
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
import game.worldsrv.human.HumanGlobalInfo;
import game.msg.Define.EInformType;
import java.util.List;
import com.google.protobuf.Message;
import game.worldsrv.entity.FillMail;
import game.worldsrv.entity.FriendObject;
import game.worldsrv.entity.ItemBody;
import game.worldsrv.entity.Unit;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.fightrecord.RecordInfo;
import game.msg.Define.ETeamType;
import game.msg.Define.ECrossFightType;
import game.worldsrv.guild.GuildInstData;

@GofGenFile
public final class HumanGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int HumanGlobalService_FriendChange_long_FriendObject = 1;
		public static final int HumanGlobalService_cancel_long = 2;
		public static final int HumanGlobalService_changeConnPoint_Long_CallPoint = 3;
		public static final int HumanGlobalService_disposeHumanFriend_long_int_long_long = 4;
		public static final int HumanGlobalService_doHumanInCurrentThread_long_String_String_Param_Param = 5;
		public static final int HumanGlobalService_getInfo_long = 6;
		public static final int HumanGlobalService_getInfoList_List = 7;
		public static final int HumanGlobalService_getInfoMap_List = 8;
		public static final int HumanGlobalService_getRecommendNewUsers_long_List = 9;
		public static final int HumanGlobalService_isLogined_String = 10;
		public static final int HumanGlobalService_kick_long_int = 11;
		public static final int HumanGlobalService_pvpFinishFight_long_int_int_Message = 12;
		public static final int HumanGlobalService_pvpLeaveFight_long_int = 13;
		public static final int HumanGlobalService_pvpMatchCancelOK_long = 14;
		public static final int HumanGlobalService_pvpMatchResult_long_int_String_String_int_int_HumanMirrorObject_String_long_int_int = 15;
		public static final int HumanGlobalService_pvpMatchTimeOut_long = 16;
		public static final int HumanGlobalService_queryOnlineNum = 17;
		public static final int HumanGlobalService_quickFightResult_int_int_HumanMirrorObject_Param = 18;
		public static final int HumanGlobalService_register_HumanGlobalInfo = 19;
		public static final int HumanGlobalService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo = 20;
		public static final int HumanGlobalService_sealAccount_long_int_long = 21;
		public static final int HumanGlobalService_sendCrossInform_int_String_String_int_int = 22;
		public static final int HumanGlobalService_sendFightInfo_long_ETeamType_ECrossFightType_int_long = 23;
		public static final int HumanGlobalService_sendFillMail_FillMail = 24;
		public static final int HumanGlobalService_sendInform_EInformType_List_String_Long_int_int = 25;
		public static final int HumanGlobalService_sendMailOnline_Strings = 26;
		public static final int HumanGlobalService_sendMsg_long_Message = 27;
		public static final int HumanGlobalService_sendMsgTo_List_Message = 28;
		public static final int HumanGlobalService_sendMsgToAll_List_Message = 29;
		public static final int HumanGlobalService_setGm_long_boolean = 30;
		public static final int HumanGlobalService_setSilenceEndTime_long = 31;
		public static final int HumanGlobalService_silence_long_int_long = 32;
		public static final int HumanGlobalService_stageIdModify_long_long_String_String_String = 33;
		public static final int HumanGlobalService_syncGuildInstData_long_GuildInstData = 34;
		public static final int HumanGlobalService_syncInfoTime_long = 35;
		public static final int HumanGlobalService_updatGuildName_Long_String = 36;
		public static final int HumanGlobalService_update_Objects = 37;
		public static final int HumanGlobalService_updateCompeteRank_long_int = 38;
		public static final int HumanGlobalService_updateEquip_long_List = 39;
		public static final int HumanGlobalService_updateEquip_long_ItemBody = 40;
		public static final int HumanGlobalService_updateHumanInfo_long_String_int_int_int_long_int_int_int_int = 41;
		public static final int HumanGlobalService_updateInstStar_long_int = 42;
		public static final int HumanGlobalService_updatePartnerInfo_long_String_int_List = 43;
		public static final int HumanGlobalService_updateProp_long_Unit = 44;
		public static final int HumanGlobalService_updateSkillGodsList_long_List = 45;
		public static final int HumanGlobalService_updateSkillGodsSn_long_int = 46;
		public static final int HumanGlobalService_updateSkillInfo_long_List_List_int_List = 47;
		public static final int HumanGlobalService_updateSkillList_long_List = 48;
		public static final int HumanGlobalService_updateSkillTrainList_long_List = 49;
		public static final int HumanGlobalService_updateVipLv_long_int = 50;
	}
	private static final String SERV_ID = "worldsrv.human.HumanGlobalService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private HumanGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		HumanGlobalService serv = (HumanGlobalService)service;
		switch (methodKey) {
			case EnumCall.HumanGlobalService_FriendChange_long_FriendObject: {
				return (GofFunction2<Long, FriendObject>)serv::FriendChange;
			}
			case EnumCall.HumanGlobalService_cancel_long: {
				return (GofFunction1<Long>)serv::cancel;
			}
			case EnumCall.HumanGlobalService_changeConnPoint_Long_CallPoint: {
				return (GofFunction2<Long, CallPoint>)serv::changeConnPoint;
			}
			case EnumCall.HumanGlobalService_disposeHumanFriend_long_int_long_long: {
				return (GofFunction4<Long, Integer, Long, Long>)serv::disposeHumanFriend;
			}
			case EnumCall.HumanGlobalService_doHumanInCurrentThread_long_String_String_Param_Param: {
				return (GofFunction5<Long, String, String, Param, Param>)serv::doHumanInCurrentThread;
			}
			case EnumCall.HumanGlobalService_getInfo_long: {
				return (GofFunction1<Long>)serv::getInfo;
			}
			case EnumCall.HumanGlobalService_getInfoList_List: {
				return (GofFunction1<List>)serv::getInfoList;
			}
			case EnumCall.HumanGlobalService_getInfoMap_List: {
				return (GofFunction1<List>)serv::getInfoMap;
			}
			case EnumCall.HumanGlobalService_getRecommendNewUsers_long_List: {
				return (GofFunction2<Long, List>)serv::getRecommendNewUsers;
			}
			case EnumCall.HumanGlobalService_isLogined_String: {
				return (GofFunction1<String>)serv::isLogined;
			}
			case EnumCall.HumanGlobalService_kick_long_int: {
				return (GofFunction2<Long, Integer>)serv::kick;
			}
			case EnumCall.HumanGlobalService_pvpFinishFight_long_int_int_Message: {
				return (GofFunction4<Long, Integer, Integer, Message>)serv::pvpFinishFight;
			}
			case EnumCall.HumanGlobalService_pvpLeaveFight_long_int: {
				return (GofFunction2<Long, Integer>)serv::pvpLeaveFight;
			}
			case EnumCall.HumanGlobalService_pvpMatchCancelOK_long: {
				return (GofFunction1<Long>)serv::pvpMatchCancelOK;
			}
			case EnumCall.HumanGlobalService_pvpMatchResult_long_int_String_String_int_int_HumanMirrorObject_String_long_int_int: {
				return (GofFunction11<Long, Integer, String, String, Integer, Integer, HumanMirrorObject, String, Long, Integer, Integer>)serv::pvpMatchResult;
			}
			case EnumCall.HumanGlobalService_pvpMatchTimeOut_long: {
				return (GofFunction1<Long>)serv::pvpMatchTimeOut;
			}
			case EnumCall.HumanGlobalService_queryOnlineNum: {
				return (GofFunction0)serv::queryOnlineNum;
			}
			case EnumCall.HumanGlobalService_quickFightResult_int_int_HumanMirrorObject_Param: {
				return (GofFunction4<Integer, Integer, HumanMirrorObject, Param>)serv::quickFightResult;
			}
			case EnumCall.HumanGlobalService_register_HumanGlobalInfo: {
				return (GofFunction1<HumanGlobalInfo>)serv::register;
			}
			case EnumCall.HumanGlobalService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo: {
				return (GofFunction10<Long, Integer, Integer, String, String, Integer, Long, Integer, Integer, RecordInfo>)serv::replayRecordResult;
			}
			case EnumCall.HumanGlobalService_sealAccount_long_int_long: {
				return (GofFunction3<Long, Integer, Long>)serv::sealAccount;
			}
			case EnumCall.HumanGlobalService_sendCrossInform_int_String_String_int_int: {
				return (GofFunction5<Integer, String, String, Integer, Integer>)serv::sendCrossInform;
			}
			case EnumCall.HumanGlobalService_sendFightInfo_long_ETeamType_ECrossFightType_int_long: {
				return (GofFunction5<Long, ETeamType, ECrossFightType, Integer, Long>)serv::sendFightInfo;
			}
			case EnumCall.HumanGlobalService_sendFillMail_FillMail: {
				return (GofFunction1<FillMail>)serv::sendFillMail;
			}
			case EnumCall.HumanGlobalService_sendInform_EInformType_List_String_Long_int_int: {
				return (GofFunction6<EInformType, List, String, Long, Integer, Integer>)serv::sendInform;
			}
			case EnumCall.HumanGlobalService_sendMailOnline_Strings: {
				return (GofFunction1<String[]>)serv::sendMailOnline;
			}
			case EnumCall.HumanGlobalService_sendMsg_long_Message: {
				return (GofFunction2<Long, Message>)serv::sendMsg;
			}
			case EnumCall.HumanGlobalService_sendMsgTo_List_Message: {
				return (GofFunction2<List, Message>)serv::sendMsgTo;
			}
			case EnumCall.HumanGlobalService_sendMsgToAll_List_Message: {
				return (GofFunction2<List, Message>)serv::sendMsgToAll;
			}
			case EnumCall.HumanGlobalService_setGm_long_boolean: {
				return (GofFunction2<Long, Boolean>)serv::setGm;
			}
			case EnumCall.HumanGlobalService_setSilenceEndTime_long: {
				return (GofFunction1<Long>)serv::setSilenceEndTime;
			}
			case EnumCall.HumanGlobalService_silence_long_int_long: {
				return (GofFunction3<Long, Integer, Long>)serv::silence;
			}
			case EnumCall.HumanGlobalService_stageIdModify_long_long_String_String_String: {
				return (GofFunction5<Long, Long, String, String, String>)serv::stageIdModify;
			}
			case EnumCall.HumanGlobalService_syncGuildInstData_long_GuildInstData: {
				return (GofFunction2<Long, GuildInstData>)serv::syncGuildInstData;
			}
			case EnumCall.HumanGlobalService_syncInfoTime_long: {
				return (GofFunction1<Long>)serv::syncInfoTime;
			}
			case EnumCall.HumanGlobalService_updatGuildName_Long_String: {
				return (GofFunction2<Long, String>)serv::updatGuildName;
			}
			case EnumCall.HumanGlobalService_update_Objects: {
				return (GofFunction1<Object[]>)serv::update;
			}
			case EnumCall.HumanGlobalService_updateCompeteRank_long_int: {
				return (GofFunction2<Long, Integer>)serv::updateCompeteRank;
			}
			case EnumCall.HumanGlobalService_updateEquip_long_List: {
				return (GofFunction2<Long, List>)serv::updateEquip;
			}
			case EnumCall.HumanGlobalService_updateEquip_long_ItemBody: {
				return (GofFunction2<Long, ItemBody>)serv::updateEquip;
			}
			case EnumCall.HumanGlobalService_updateHumanInfo_long_String_int_int_int_long_int_int_int_int: {
				return (GofFunction10<Long, String, Integer, Integer, Integer, Long, Integer, Integer, Integer, Integer>)serv::updateHumanInfo;
			}
			case EnumCall.HumanGlobalService_updateInstStar_long_int: {
				return (GofFunction2<Long, Integer>)serv::updateInstStar;
			}
			case EnumCall.HumanGlobalService_updatePartnerInfo_long_String_int_List: {
				return (GofFunction4<Long, String, Integer, List>)serv::updatePartnerInfo;
			}
			case EnumCall.HumanGlobalService_updateProp_long_Unit: {
				return (GofFunction2<Long, Unit>)serv::updateProp;
			}
			case EnumCall.HumanGlobalService_updateSkillGodsList_long_List: {
				return (GofFunction2<Long, List>)serv::updateSkillGodsList;
			}
			case EnumCall.HumanGlobalService_updateSkillGodsSn_long_int: {
				return (GofFunction2<Long, Integer>)serv::updateSkillGodsSn;
			}
			case EnumCall.HumanGlobalService_updateSkillInfo_long_List_List_int_List: {
				return (GofFunction5<Long, List, List, Integer, List>)serv::updateSkillInfo;
			}
			case EnumCall.HumanGlobalService_updateSkillList_long_List: {
				return (GofFunction2<Long, List>)serv::updateSkillList;
			}
			case EnumCall.HumanGlobalService_updateSkillTrainList_long_List: {
				return (GofFunction2<Long, List>)serv::updateSkillTrainList;
			}
			case EnumCall.HumanGlobalService_updateVipLv_long_int: {
				return (GofFunction2<Long, Integer>)serv::updateVipLv;
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
	public static HumanGlobalServiceProxy newInstance() {
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
	private static HumanGlobalServiceProxy createInstance(String node, String port, Object id) {
		HumanGlobalServiceProxy inst = new HumanGlobalServiceProxy();
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
	
	public void FriendChange(long humanId, FriendObject object) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_FriendChange_long_FriendObject, "HumanGlobalServiceProxy.FriendChange", new Object[] {humanId, object});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void cancel(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_cancel_long, "HumanGlobalServiceProxy.cancel", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void changeConnPoint(Long id, CallPoint point) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_changeConnPoint_Long_CallPoint, "HumanGlobalServiceProxy.changeConnPoint", new Object[] {id, point});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void disposeHumanFriend(long humanA, int type, long time, long humanB) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_disposeHumanFriend_long_int_long_long, "HumanGlobalServiceProxy.disposeHumanFriend", new Object[] {humanA, type, time, humanB});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void doHumanInCurrentThread(long humanId, String className, String methodName, Param results, Param context) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_doHumanInCurrentThread_long_String_String_Param_Param, "HumanGlobalServiceProxy.doHumanInCurrentThread", new Object[] {humanId, className, methodName, results, context});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getInfo(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_getInfo_long, "HumanGlobalServiceProxy.getInfo", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getInfoList(List humanIdList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_getInfoList_List, "HumanGlobalServiceProxy.getInfoList", new Object[] {humanIdList});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getInfoMap(List humanIdList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_getInfoMap_List, "HumanGlobalServiceProxy.getInfoMap", new Object[] {humanIdList});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void getRecommendNewUsers(long humanId, List humanIds) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_getRecommendNewUsers_long_List, "HumanGlobalServiceProxy.getRecommendNewUsers", new Object[] {humanId, humanIds});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isLogined(String account) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_isLogined_String, "HumanGlobalServiceProxy.isLogined", new Object[] {account});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void kick(long id, int reason) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_kick_long_int, "HumanGlobalServiceProxy.kick", new Object[] {id, reason});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpFinishFight(long humanId, int type, int win, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_pvpFinishFight_long_int_int_Message, "HumanGlobalServiceProxy.pvpFinishFight", new Object[] {humanId, type, win, msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpLeaveFight(long humanId, int type) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_pvpLeaveFight_long_int, "HumanGlobalServiceProxy.pvpLeaveFight", new Object[] {humanId, type});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpMatchCancelOK(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_pvpMatchCancelOK_long, "HumanGlobalServiceProxy.pvpMatchCancelOK", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpMatchResult(long humanId, int type, String token, String crossIp, int crossPort, int teamCamp, HumanMirrorObject enemy, String nodeId, long stageId, int stageSn, int mapSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_pvpMatchResult_long_int_String_String_int_int_HumanMirrorObject_String_long_int_int, "HumanGlobalServiceProxy.pvpMatchResult", new Object[] {humanId, type, token, crossIp, crossPort, teamCamp, enemy, nodeId, stageId, stageSn, mapSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpMatchTimeOut(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_pvpMatchTimeOut_long, "HumanGlobalServiceProxy.pvpMatchTimeOut", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void queryOnlineNum() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_queryOnlineNum, "HumanGlobalServiceProxy.queryOnlineNum", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void quickFightResult(int result, int type, HumanMirrorObject humanMirrorObj, Param param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_quickFightResult_int_int_HumanMirrorObject_Param, "HumanGlobalServiceProxy.quickFightResult", new Object[] {result, type, humanMirrorObj, param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void register(HumanGlobalInfo status) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_register_HumanGlobalInfo, "HumanGlobalServiceProxy.register", new Object[] {status});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void replayRecordResult(long humanId, int result, int fightType, String token, String crossIp, int crossPort, long stageId, int stageSn, int mapSn, RecordInfo record) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo, "HumanGlobalServiceProxy.replayRecordResult", new Object[] {humanId, result, fightType, token, crossIp, crossPort, stageId, stageSn, mapSn, record});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sealAccount(long humanId, int type, long endTime) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_sealAccount_long_int_long, "HumanGlobalServiceProxy.sealAccount", new Object[] {humanId, type, endTime});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendCrossInform(int serverId, String name, String content, int icon, int aptitude) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_sendCrossInform_int_String_String_int_int, "HumanGlobalServiceProxy.sendCrossInform", new Object[] {serverId, name, content, icon, aptitude});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendFightInfo(long humanId, ETeamType teamType, ECrossFightType fightType, int mapSn, long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_sendFightInfo_long_ETeamType_ECrossFightType_int_long, "HumanGlobalServiceProxy.sendFightInfo", new Object[] {humanId, teamType, fightType, mapSn, stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendFillMail(FillMail fillMail) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_sendFillMail_FillMail, "HumanGlobalServiceProxy.sendFillMail", new Object[] {fillMail});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void sendInform(EInformType type, List keys, String content, Long sendHumanId, int num, int interval) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_sendInform_EInformType_List_String_Long_int_int, "HumanGlobalServiceProxy.sendInform", new Object[] {type, keys, content, sendHumanId, num, interval});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendMailOnline(String... produces) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_sendMailOnline_Strings, "HumanGlobalServiceProxy.sendMailOnline", new Object[] {produces});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendMsg(long humanId, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.HumanGlobalService_sendMsg_long_Message, "HumanGlobalServiceProxy.sendMsg", new Object[] {humanId, msg});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void sendMsgTo(List humanIds, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.HumanGlobalService_sendMsgTo_List_Message, "HumanGlobalServiceProxy.sendMsgTo", new Object[] {humanIds, msg});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void sendMsgToAll(List excludeIds, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(true, remote, EnumCall.HumanGlobalService_sendMsgToAll_List_Message, "HumanGlobalServiceProxy.sendMsgToAll", new Object[] {excludeIds, msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setGm(long humanId, boolean isOpen) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_setGm_long_boolean, "HumanGlobalServiceProxy.setGm", new Object[] {humanId, isOpen});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setSilenceEndTime(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_setSilenceEndTime_long, "HumanGlobalServiceProxy.setSilenceEndTime", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void silence(long humanId, int campType, long keepTime) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_silence_long_int_long, "HumanGlobalServiceProxy.silence", new Object[] {humanId, campType, keepTime});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void stageIdModify(long humanId, long stageIdNew, String stageName, String nodeId, String portId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_stageIdModify_long_long_String_String_String, "HumanGlobalServiceProxy.stageIdModify", new Object[] {humanId, stageIdNew, stageName, nodeId, portId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void syncGuildInstData(long humanId, GuildInstData inst) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_syncGuildInstData_long_GuildInstData, "HumanGlobalServiceProxy.syncGuildInstData", new Object[] {humanId, inst});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void syncInfoTime(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_syncInfoTime_long, "HumanGlobalServiceProxy.syncInfoTime", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updatGuildName(Long id, String name) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updatGuildName_Long_String, "HumanGlobalServiceProxy.updatGuildName", new Object[] {id, name});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_update_Objects, "HumanGlobalServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateCompeteRank(long humanId, int competeRank) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateCompeteRank_long_int, "HumanGlobalServiceProxy.updateCompeteRank", new Object[] {humanId, competeRank});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void updateEquip(long humanId, List equipList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateEquip_long_List, "HumanGlobalServiceProxy.updateEquip", new Object[] {humanId, equipList});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateEquip(long humanId, ItemBody equip) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateEquip_long_ItemBody, "HumanGlobalServiceProxy.updateEquip", new Object[] {humanId, equip});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateHumanInfo(long humanId, String name, int level, int vipLv, int combat, long unionId, int modelSn, int defaultModelSn, int titleSn, int partnerStance) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateHumanInfo_long_String_int_int_int_long_int_int_int_int, "HumanGlobalServiceProxy.updateHumanInfo", new Object[] {humanId, name, level, vipLv, combat, unionId, modelSn, defaultModelSn, titleSn, partnerStance});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateInstStar(long humanId, int instStar) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateInstStar_long_int, "HumanGlobalServiceProxy.updateInstStar", new Object[] {humanId, instStar});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void updatePartnerInfo(long humanId, String lineup, int stance, List partnerList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updatePartnerInfo_long_String_int_List, "HumanGlobalServiceProxy.updatePartnerInfo", new Object[] {humanId, lineup, stance, partnerList});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateProp(long humanId, Unit unit) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateProp_long_Unit, "HumanGlobalServiceProxy.updateProp", new Object[] {humanId, unit});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void updateSkillGodsList(long humanId, List skillGodsList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateSkillGodsList_long_List, "HumanGlobalServiceProxy.updateSkillGodsList", new Object[] {humanId, skillGodsList});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateSkillGodsSn(long humanId, int installGods) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateSkillGodsSn_long_int, "HumanGlobalServiceProxy.updateSkillGodsSn", new Object[] {humanId, installGods});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void updateSkillInfo(long humanId, List skillList, List skillGodsList, int installGods, List skillTrainList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateSkillInfo_long_List_List_int_List, "HumanGlobalServiceProxy.updateSkillInfo", new Object[] {humanId, skillList, skillGodsList, installGods, skillTrainList});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void updateSkillList(long humanId, List skillList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateSkillList_long_List, "HumanGlobalServiceProxy.updateSkillList", new Object[] {humanId, skillList});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void updateSkillTrainList(long humanId, List skillTrainList) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateSkillTrainList_long_List, "HumanGlobalServiceProxy.updateSkillTrainList", new Object[] {humanId, skillTrainList});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateVipLv(long humanId, int vipLv) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanGlobalService_updateVipLv_long_int, "HumanGlobalServiceProxy.updateVipLv", new Object[] {humanId, vipLv});
		if(immutableOnce) immutableOnce = false;
	}
}
