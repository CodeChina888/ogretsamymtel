package game.worldsrv.character;
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import core.gen.GofGenFile;
import game.worldsrv.entity.Mail;
import game.worldsrv.team.TeamData;
import java.util.List;
import game.worldsrv.entity.FillMail;
import game.worldsrv.entity.FriendObject;
import game.worldsrv.instWorldBoss.WBData;
import game.worldsrv.instLootMap.Room.InstLootMapSignUpHuman;
import game.msg.Define.ELootMapType;
import com.google.protobuf.Message;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.fightrecord.RecordInfo;
import game.msg.Define.ETeamType;
import game.msg.Define.ECrossFightType;
import game.worldsrv.entity.Castellan;
import game.worldsrv.guild.GuildInstData;

@GofGenFile
public final class HumanObjectServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int HumanObjectService_LootMapEnter_int_int = 1;
		public static final int HumanObjectService_acceptFillMail_FillMail = 2;
		public static final int HumanObjectService_changeConnPoint_CallPoint = 3;
		public static final int HumanObjectService_connCheck_long = 4;
		public static final int HumanObjectService_connClosed_long = 5;
		public static final int HumanObjectService_dispostHumanFriend_FriendObject_int_long = 6;
		public static final int HumanObjectService_doHumanInCurrentThread_String_String_Param_Param = 7;
		public static final int HumanObjectService_getHuman = 8;
		public static final int HumanObjectService_guildCDRUp_boolean = 9;
		public static final int HumanObjectService_guildJoin_boolean_long_int_String_int = 10;
		public static final int HumanObjectService_guildLeave_boolean_int = 11;
		public static final int HumanObjectService_kickClosed_long = 12;
		public static final int HumanObjectService_leave = 13;
		public static final int HumanObjectService_lootMapCostIntoItem_int_int = 14;
		public static final int HumanObjectService_lootMapEnter_long_ELootMapType = 15;
		public static final int HumanObjectService_lootMapIntoSignUpRoom_InstLootMapSignUpHuman = 16;
		public static final int HumanObjectService_lootMapLeaveSignUpRoom_long = 17;
		public static final int HumanObjectService_lootMapSignUpRoomTimeOut = 18;
		public static final int HumanObjectService_mailAccept_Mail = 19;
		public static final int HumanObjectService_msgHandler_long_bytes = 20;
		public static final int HumanObjectService_noticeMainCity_Castellan = 21;
		public static final int HumanObjectService_onSchedule_int_long = 22;
		public static final int HumanObjectService_pay_String = 23;
		public static final int HumanObjectService_pvpFinishFight_long_int_int_Message = 24;
		public static final int HumanObjectService_pvpLeaveFight_long_int = 25;
		public static final int HumanObjectService_pvpMatchCancelOK = 26;
		public static final int HumanObjectService_pvpMatchResult_long_Message_HumanMirrorObject_String = 27;
		public static final int HumanObjectService_pvpMatchResult_long_int_String_String_int_int_HumanMirrorObject_String_long_int_int = 28;
		public static final int HumanObjectService_quickFightResult_int_int_HumanMirrorObject_Param = 29;
		public static final int HumanObjectService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo = 30;
		public static final int HumanObjectService_sealAccount_int_long = 31;
		public static final int HumanObjectService_sendFightInfo_ETeamType_ECrossFightType_int_long = 32;
		public static final int HumanObjectService_sendSCLootMapReadyEnter_int = 33;
		public static final int HumanObjectService_sendSCWorldBossEnd_WBData = 34;
		public static final int HumanObjectService_setGm_int = 35;
		public static final int HumanObjectService_setSilenceEndTime = 36;
		public static final int HumanObjectService_setVipLv_int = 37;
		public static final int HumanObjectService_silence_long = 38;
		public static final int HumanObjectService_syncGuildInstData_GuildInstData = 39;
		public static final int HumanObjectService_teamApplyJoin_int_TeamData = 40;
		public static final int HumanObjectService_teamEnterInst_int_int_long_int = 41;
		public static final int HumanObjectService_teamEnterPVP_int_int_long_int_int_List_List = 42;
		public static final int HumanObjectService_teamInfoUpdate_TeamData = 43;
		public static final int HumanObjectService_teamKickOut_boolean = 44;
		public static final int HumanObjectService_teamLeave_boolean = 45;
		public static final int HumanObjectService_teamMatch_int = 46;
		public static final int HumanObjectService_teamMatchCancel_int = 47;
		public static final int HumanObjectService_teamMemberInfoUpdate_TeamData = 48;
		public static final int HumanObjectService_update_Objects = 49;
		public static final int HumanObjectService_updateFriendHumanObject_long_FriendObject = 50;
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
	private HumanObjectServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		HumanObjectService serv = (HumanObjectService)service;
		switch (methodKey) {
			case EnumCall.HumanObjectService_LootMapEnter_int_int: {
				return (GofFunction2<Integer, Integer>)serv::LootMapEnter;
			}
			case EnumCall.HumanObjectService_acceptFillMail_FillMail: {
				return (GofFunction1<FillMail>)serv::acceptFillMail;
			}
			case EnumCall.HumanObjectService_changeConnPoint_CallPoint: {
				return (GofFunction1<CallPoint>)serv::changeConnPoint;
			}
			case EnumCall.HumanObjectService_connCheck_long: {
				return (GofFunction1<Long>)serv::connCheck;
			}
			case EnumCall.HumanObjectService_connClosed_long: {
				return (GofFunction1<Long>)serv::connClosed;
			}
			case EnumCall.HumanObjectService_dispostHumanFriend_FriendObject_int_long: {
				return (GofFunction3<FriendObject, Integer, Long>)serv::dispostHumanFriend;
			}
			case EnumCall.HumanObjectService_doHumanInCurrentThread_String_String_Param_Param: {
				return (GofFunction4<String, String, Param, Param>)serv::doHumanInCurrentThread;
			}
			case EnumCall.HumanObjectService_getHuman: {
				return (GofFunction0)serv::getHuman;
			}
			case EnumCall.HumanObjectService_guildCDRUp_boolean: {
				return (GofFunction1<Boolean>)serv::guildCDRUp;
			}
			case EnumCall.HumanObjectService_guildJoin_boolean_long_int_String_int: {
				return (GofFunction5<Boolean, Long, Integer, String, Integer>)serv::guildJoin;
			}
			case EnumCall.HumanObjectService_guildLeave_boolean_int: {
				return (GofFunction2<Boolean, Integer>)serv::guildLeave;
			}
			case EnumCall.HumanObjectService_kickClosed_long: {
				return (GofFunction1<Long>)serv::kickClosed;
			}
			case EnumCall.HumanObjectService_leave: {
				return (GofFunction0)serv::leave;
			}
			case EnumCall.HumanObjectService_lootMapCostIntoItem_int_int: {
				return (GofFunction2<Integer, Integer>)serv::lootMapCostIntoItem;
			}
			case EnumCall.HumanObjectService_lootMapEnter_long_ELootMapType: {
				return (GofFunction2<Long, ELootMapType>)serv::lootMapEnter;
			}
			case EnumCall.HumanObjectService_lootMapIntoSignUpRoom_InstLootMapSignUpHuman: {
				return (GofFunction1<InstLootMapSignUpHuman>)serv::lootMapIntoSignUpRoom;
			}
			case EnumCall.HumanObjectService_lootMapLeaveSignUpRoom_long: {
				return (GofFunction1<Long>)serv::lootMapLeaveSignUpRoom;
			}
			case EnumCall.HumanObjectService_lootMapSignUpRoomTimeOut: {
				return (GofFunction0)serv::lootMapSignUpRoomTimeOut;
			}
			case EnumCall.HumanObjectService_mailAccept_Mail: {
				return (GofFunction1<Mail>)serv::mailAccept;
			}
			case EnumCall.HumanObjectService_msgHandler_long_bytes: {
				return (GofFunction2<Long, byte[]>)serv::msgHandler;
			}
			case EnumCall.HumanObjectService_noticeMainCity_Castellan: {
				return (GofFunction1<Castellan>)serv::noticeMainCity;
			}
			case EnumCall.HumanObjectService_onSchedule_int_long: {
				return (GofFunction2<Integer, Long>)serv::onSchedule;
			}
			case EnumCall.HumanObjectService_pay_String: {
				return (GofFunction1<String>)serv::pay;
			}
			case EnumCall.HumanObjectService_pvpFinishFight_long_int_int_Message: {
				return (GofFunction4<Long, Integer, Integer, Message>)serv::pvpFinishFight;
			}
			case EnumCall.HumanObjectService_pvpLeaveFight_long_int: {
				return (GofFunction2<Long, Integer>)serv::pvpLeaveFight;
			}
			case EnumCall.HumanObjectService_pvpMatchCancelOK: {
				return (GofFunction0)serv::pvpMatchCancelOK;
			}
			case EnumCall.HumanObjectService_pvpMatchResult_long_Message_HumanMirrorObject_String: {
				return (GofFunction4<Long, Message, HumanMirrorObject, String>)serv::pvpMatchResult;
			}
			case EnumCall.HumanObjectService_pvpMatchResult_long_int_String_String_int_int_HumanMirrorObject_String_long_int_int: {
				return (GofFunction11<Long, Integer, String, String, Integer, Integer, HumanMirrorObject, String, Long, Integer, Integer>)serv::pvpMatchResult;
			}
			case EnumCall.HumanObjectService_quickFightResult_int_int_HumanMirrorObject_Param: {
				return (GofFunction4<Integer, Integer, HumanMirrorObject, Param>)serv::quickFightResult;
			}
			case EnumCall.HumanObjectService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo: {
				return (GofFunction10<Long, Integer, Integer, String, String, Integer, Long, Integer, Integer, RecordInfo>)serv::replayRecordResult;
			}
			case EnumCall.HumanObjectService_sealAccount_int_long: {
				return (GofFunction2<Integer, Long>)serv::sealAccount;
			}
			case EnumCall.HumanObjectService_sendFightInfo_ETeamType_ECrossFightType_int_long: {
				return (GofFunction4<ETeamType, ECrossFightType, Integer, Long>)serv::sendFightInfo;
			}
			case EnumCall.HumanObjectService_sendSCLootMapReadyEnter_int: {
				return (GofFunction1<Integer>)serv::sendSCLootMapReadyEnter;
			}
			case EnumCall.HumanObjectService_sendSCWorldBossEnd_WBData: {
				return (GofFunction1<WBData>)serv::sendSCWorldBossEnd;
			}
			case EnumCall.HumanObjectService_setGm_int: {
				return (GofFunction1<Integer>)serv::setGm;
			}
			case EnumCall.HumanObjectService_setSilenceEndTime: {
				return (GofFunction0)serv::setSilenceEndTime;
			}
			case EnumCall.HumanObjectService_setVipLv_int: {
				return (GofFunction1<Integer>)serv::setVipLv;
			}
			case EnumCall.HumanObjectService_silence_long: {
				return (GofFunction1<Long>)serv::silence;
			}
			case EnumCall.HumanObjectService_syncGuildInstData_GuildInstData: {
				return (GofFunction1<GuildInstData>)serv::syncGuildInstData;
			}
			case EnumCall.HumanObjectService_teamApplyJoin_int_TeamData: {
				return (GofFunction2<Integer, TeamData>)serv::teamApplyJoin;
			}
			case EnumCall.HumanObjectService_teamEnterInst_int_int_long_int: {
				return (GofFunction4<Integer, Integer, Long, Integer>)serv::teamEnterInst;
			}
			case EnumCall.HumanObjectService_teamEnterPVP_int_int_long_int_int_List_List: {
				return (GofFunction7<Integer, Integer, Long, Integer, Integer, List, List>)serv::teamEnterPVP;
			}
			case EnumCall.HumanObjectService_teamInfoUpdate_TeamData: {
				return (GofFunction1<TeamData>)serv::teamInfoUpdate;
			}
			case EnumCall.HumanObjectService_teamKickOut_boolean: {
				return (GofFunction1<Boolean>)serv::teamKickOut;
			}
			case EnumCall.HumanObjectService_teamLeave_boolean: {
				return (GofFunction1<Boolean>)serv::teamLeave;
			}
			case EnumCall.HumanObjectService_teamMatch_int: {
				return (GofFunction1<Integer>)serv::teamMatch;
			}
			case EnumCall.HumanObjectService_teamMatchCancel_int: {
				return (GofFunction1<Integer>)serv::teamMatchCancel;
			}
			case EnumCall.HumanObjectService_teamMemberInfoUpdate_TeamData: {
				return (GofFunction1<TeamData>)serv::teamMemberInfoUpdate;
			}
			case EnumCall.HumanObjectService_update_Objects: {
				return (GofFunction1<Object[]>)serv::update;
			}
			case EnumCall.HumanObjectService_updateFriendHumanObject_long_FriendObject: {
				return (GofFunction2<Long, FriendObject>)serv::updateFriendHumanObject;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static HumanObjectServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static HumanObjectServiceProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static HumanObjectServiceProxy createInstance(String node, String port, Object id) {
		HumanObjectServiceProxy inst = new HumanObjectServiceProxy();
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
	
	public void LootMapEnter(int mapSn, int sn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_LootMapEnter_int_int, "HumanObjectServiceProxy.LootMapEnter", new Object[] {mapSn, sn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void acceptFillMail(FillMail fillMail) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_acceptFillMail_FillMail, "HumanObjectServiceProxy.acceptFillMail", new Object[] {fillMail});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void changeConnPoint(CallPoint point) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_changeConnPoint_CallPoint, "HumanObjectServiceProxy.changeConnPoint", new Object[] {point});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void connCheck(long connId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_connCheck_long, "HumanObjectServiceProxy.connCheck", new Object[] {connId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void connClosed(long connId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_connClosed_long, "HumanObjectServiceProxy.connClosed", new Object[] {connId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void dispostHumanFriend(FriendObject object, int type, long newTime) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_dispostHumanFriend_FriendObject_int_long, "HumanObjectServiceProxy.dispostHumanFriend", new Object[] {object, type, newTime});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void doHumanInCurrentThread(String className, String methodName, Param results, Param context) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_doHumanInCurrentThread_String_String_Param_Param, "HumanObjectServiceProxy.doHumanInCurrentThread", new Object[] {className, methodName, results, context});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getHuman() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_getHuman, "HumanObjectServiceProxy.getHuman", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void guildCDRUp(boolean isCDR) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_guildCDRUp_boolean, "HumanObjectServiceProxy.guildCDRUp", new Object[] {isCDR});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void guildJoin(boolean result, long guildId, int guildLv, String guildName, int isApply) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_guildJoin_boolean_long_int_String_int, "HumanObjectServiceProxy.guildJoin", new Object[] {result, guildId, guildLv, guildName, isApply});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void guildLeave(boolean result, int isKickOut) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_guildLeave_boolean_int, "HumanObjectServiceProxy.guildLeave", new Object[] {result, isKickOut});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void kickClosed(long connId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_kickClosed_long, "HumanObjectServiceProxy.kickClosed", new Object[] {connId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void leave() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_leave, "HumanObjectServiceProxy.leave", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void lootMapCostIntoItem(int itemSn, int itemNum) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_lootMapCostIntoItem_int_int, "HumanObjectServiceProxy.lootMapCostIntoItem", new Object[] {itemSn, itemNum});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void lootMapEnter(long stageId, ELootMapType mapType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_lootMapEnter_long_ELootMapType, "HumanObjectServiceProxy.lootMapEnter", new Object[] {stageId, mapType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void lootMapIntoSignUpRoom(InstLootMapSignUpHuman otherHuman) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_lootMapIntoSignUpRoom_InstLootMapSignUpHuman, "HumanObjectServiceProxy.lootMapIntoSignUpRoom", new Object[] {otherHuman});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void lootMapLeaveSignUpRoom(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_lootMapLeaveSignUpRoom_long, "HumanObjectServiceProxy.lootMapLeaveSignUpRoom", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void lootMapSignUpRoomTimeOut() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_lootMapSignUpRoomTimeOut, "HumanObjectServiceProxy.lootMapSignUpRoomTimeOut", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void mailAccept(Mail mail) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_mailAccept_Mail, "HumanObjectServiceProxy.mailAccept", new Object[] {mail});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void msgHandler(long connId, byte... chunk) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_msgHandler_long_bytes, "HumanObjectServiceProxy.msgHandler", new Object[] {connId, chunk});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void noticeMainCity(Castellan castellan) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_noticeMainCity_Castellan, "HumanObjectServiceProxy.noticeMainCity", new Object[] {castellan});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void onSchedule(int key, long timeLast) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_onSchedule_int_long, "HumanObjectServiceProxy.onSchedule", new Object[] {key, timeLast});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pay(String param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_pay_String, "HumanObjectServiceProxy.pay", new Object[] {param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpFinishFight(long humanId, int type, int win, Message msg) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_pvpFinishFight_long_int_int_Message, "HumanObjectServiceProxy.pvpFinishFight", new Object[] {humanId, type, win, msg});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpLeaveFight(long humanId, int type) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_pvpLeaveFight_long_int, "HumanObjectServiceProxy.pvpLeaveFight", new Object[] {humanId, type});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpMatchCancelOK() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_pvpMatchCancelOK, "HumanObjectServiceProxy.pvpMatchCancelOK", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpMatchResult(long humanId, Message msg, HumanMirrorObject enemy, String nodeId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_pvpMatchResult_long_Message_HumanMirrorObject_String, "HumanObjectServiceProxy.pvpMatchResult", new Object[] {humanId, msg, enemy, nodeId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void pvpMatchResult(long humanId, int type, String token, String crossIp, int crossPort, int teamCamp, HumanMirrorObject enemy, String nodeId, long stageId, int stageSn, int mapSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_pvpMatchResult_long_int_String_String_int_int_HumanMirrorObject_String_long_int_int, "HumanObjectServiceProxy.pvpMatchResult", new Object[] {humanId, type, token, crossIp, crossPort, teamCamp, enemy, nodeId, stageId, stageSn, mapSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void quickFightResult(int result, int type, HumanMirrorObject humanMirrorObj, Param param) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_quickFightResult_int_int_HumanMirrorObject_Param, "HumanObjectServiceProxy.quickFightResult", new Object[] {result, type, humanMirrorObj, param});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void replayRecordResult(long humanId, int result, int fightType, String token, String crossIp, int crossPort, long stageId, int stageSn, int mapSn, RecordInfo record) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_replayRecordResult_long_int_int_String_String_int_long_int_int_RecordInfo, "HumanObjectServiceProxy.replayRecordResult", new Object[] {humanId, result, fightType, token, crossIp, crossPort, stageId, stageSn, mapSn, record});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sealAccount(int type, long timeEnd) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_sealAccount_int_long, "HumanObjectServiceProxy.sealAccount", new Object[] {type, timeEnd});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendFightInfo(ETeamType teamType, ECrossFightType fightType, int mapSn, long stageId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_sendFightInfo_ETeamType_ECrossFightType_int_long, "HumanObjectServiceProxy.sendFightInfo", new Object[] {teamType, fightType, mapSn, stageId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendSCLootMapReadyEnter(int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_sendSCLootMapReadyEnter_int, "HumanObjectServiceProxy.sendSCLootMapReadyEnter", new Object[] {actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void sendSCWorldBossEnd(WBData wbData) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_sendSCWorldBossEnd_WBData, "HumanObjectServiceProxy.sendSCWorldBossEnd", new Object[] {wbData});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setGm(int value) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_setGm_int, "HumanObjectServiceProxy.setGm", new Object[] {value});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setSilenceEndTime() {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_setSilenceEndTime, "HumanObjectServiceProxy.setSilenceEndTime", new Object[] {});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setVipLv(int vipLv) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_setVipLv_int, "HumanObjectServiceProxy.setVipLv", new Object[] {vipLv});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void silence(long keepTime) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_silence_long, "HumanObjectServiceProxy.silence", new Object[] {keepTime});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void syncGuildInstData(GuildInstData inst) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_syncGuildInstData_GuildInstData, "HumanObjectServiceProxy.syncGuildInstData", new Object[] {inst});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamApplyJoin(int result, TeamData team) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamApplyJoin_int_TeamData, "HumanObjectServiceProxy.teamApplyJoin", new Object[] {result, team});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamEnterInst(int actInstSn, int mapSn, long stageId, int index) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamEnterInst_int_int_long_int, "HumanObjectServiceProxy.teamEnterInst", new Object[] {actInstSn, mapSn, stageId, index});
		if(immutableOnce) immutableOnce = false;
	}
	@SuppressWarnings("rawtypes")
	public void teamEnterPVP(int actInstSn, int mapSn, long stageId, int group, int index, List listDMemInfoTeam1, List listDMemInfoTeam2) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamEnterPVP_int_int_long_int_int_List_List, "HumanObjectServiceProxy.teamEnterPVP", new Object[] {actInstSn, mapSn, stageId, group, index, listDMemInfoTeam1, listDMemInfoTeam2});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamInfoUpdate(TeamData team) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamInfoUpdate_TeamData, "HumanObjectServiceProxy.teamInfoUpdate", new Object[] {team});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamKickOut(boolean result) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamKickOut_boolean, "HumanObjectServiceProxy.teamKickOut", new Object[] {result});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamLeave(boolean result) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamLeave_boolean, "HumanObjectServiceProxy.teamLeave", new Object[] {result});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamMatch(int result) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamMatch_int, "HumanObjectServiceProxy.teamMatch", new Object[] {result});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamMatchCancel(int result) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamMatchCancel_int, "HumanObjectServiceProxy.teamMatchCancel", new Object[] {result});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamMemberInfoUpdate(TeamData team) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_teamMemberInfoUpdate_TeamData, "HumanObjectServiceProxy.teamMemberInfoUpdate", new Object[] {team});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_update_Objects, "HumanObjectServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateFriendHumanObject(long humanId, FriendObject object) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.HumanObjectService_updateFriendHumanObject_long_FriendObject, "HumanObjectServiceProxy.updateFriendHumanObject", new Object[] {humanId, object});
		if(immutableOnce) immutableOnce = false;
	}
}
