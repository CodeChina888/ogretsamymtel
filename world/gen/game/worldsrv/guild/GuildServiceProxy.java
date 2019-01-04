package game.worldsrv.guild;
                    
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
import game.worldsrv.entity.Guild;
import game.worldsrv.guild.GuildData;
import java.util.List;
import game.msg.Define.EGuildInstResetType;

@GofGenFile
public final class GuildServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int GuildService_ReduceHp_long_int_List_String = 1;
		public static final int GuildService_add_String = 2;
		public static final int GuildService_addGuildLiveness_Guild_int = 3;
		public static final int GuildService_addGuildTotalContribute_Guild_int = 4;
		public static final int GuildService_autoAddGuild_long_int = 5;
		public static final int GuildService_cancleJoin_long_long = 6;
		public static final int GuildService_changeChairmanName_Human_long_String = 7;
		public static final int GuildService_changeGuildName_String_String = 8;
		public static final int GuildService_changeGuildSet_long_long_int_int_int = 9;
		public static final int GuildService_checkChapterReward_long_int = 10;
		public static final int GuildService_checkStageReward_long_int_int = 11;
		public static final int GuildService_createGuild_Human_String_String_int = 12;
		public static final int GuildService_getChallengeGuildInstData_long_int = 13;
		public static final int GuildService_getGuildImmo_long = 14;
		public static final int GuildService_getGuildInfo_long_Human = 15;
		public static final int GuildService_getGuildInstData_long = 16;
		public static final int GuildService_getGuildLevel_long = 17;
		public static final int GuildService_getGuildName_long = 18;
		public static final int GuildService_getMyApplyList_long = 19;
		public static final int GuildService_getNameGuild_String = 20;
		public static final int GuildService_getNameSimilarGuild_String = 21;
		public static final int GuildService_guildImmo_Guild_long_long_int_int_int = 22;
		public static final int GuildService_guildJoin_GuildData_long = 23;
		public static final int GuildService_guildLeave_Human_Guild = 24;
		public static final int GuildService_isCanApply_Human_long = 25;
		public static final int GuildService_isCanGuild_long_String = 26;
		public static final int GuildService_isCanGuildJoin_GuildData_long = 27;
		public static final int GuildService_isCanRename_long_long_String = 28;
		public static final int GuildService_isRepeatGuildName_String = 29;
		public static final int GuildService_isUnion_long_long = 30;
		public static final int GuildService_kickOutGuild_Guild_long = 31;
		public static final int GuildService_mapGuildCDR_Human = 32;
		public static final int GuildService_removeApplyInfo_long_long = 33;
		public static final int GuildService_removeApplyInfo_Guild = 34;
		public static final int GuildService_removeGuild_long = 35;
		public static final int GuildService_rename_long_String = 36;
		public static final int GuildService_replyApply_long_long_long = 37;
		public static final int GuildService_seeApplyInfo_long = 38;
		public static final int GuildService_seeGuildMemberInfo_long = 39;
		public static final int GuildService_selectAllGuildInfo_long_int = 40;
		public static final int GuildService_setGuildInstResetType_long_long_EGuildInstResetType = 41;
		public static final int GuildService_stageRewardInfo_long_int = 42;
		public static final int GuildService_syncStageReward_long_String_int_int_int_int_boolean = 43;
		public static final int GuildService_updatHumanGlobalServiceGuildName_long_long = 44;
		public static final int GuildService_update_Objects = 45;
		public static final int GuildService_updateDeclare_long_long_String = 46;
		public static final int GuildService_updateGuildHuman_long_Human_int_long = 47;
		public static final int GuildService_updateGuildInfo_Guild = 48;
		public static final int GuildService_updateGuildPostMember_Guild_GuildData_boolean = 49;
		public static final int GuildService_updateIcon_long_long_int = 50;
		public static final int GuildService_updateNotice_long_long_String = 51;
	}
	private static final String SERV_ID = "worldsrv.guild.GuildService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private GuildServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getMethodFunction(Service service, int methodKey) {
		GuildService serv = (GuildService)service;
		switch (methodKey) {
			case EnumCall.GuildService_ReduceHp_long_int_List_String: {
				return (GofFunction4<Long, Integer, List, String>)serv::ReduceHp;
			}
			case EnumCall.GuildService_add_String: {
				return (GofFunction1<String>)serv::add;
			}
			case EnumCall.GuildService_addGuildLiveness_Guild_int: {
				return (GofFunction2<Guild, Integer>)serv::addGuildLiveness;
			}
			case EnumCall.GuildService_addGuildTotalContribute_Guild_int: {
				return (GofFunction2<Guild, Integer>)serv::addGuildTotalContribute;
			}
			case EnumCall.GuildService_autoAddGuild_long_int: {
				return (GofFunction2<Long, Integer>)serv::autoAddGuild;
			}
			case EnumCall.GuildService_cancleJoin_long_long: {
				return (GofFunction2<Long, Long>)serv::cancleJoin;
			}
			case EnumCall.GuildService_changeChairmanName_Human_long_String: {
				return (GofFunction3<Human, Long, String>)serv::changeChairmanName;
			}
			case EnumCall.GuildService_changeGuildName_String_String: {
				return (GofFunction2<String, String>)serv::changeGuildName;
			}
			case EnumCall.GuildService_changeGuildSet_long_long_int_int_int: {
				return (GofFunction5<Long, Long, Integer, Integer, Integer>)serv::changeGuildSet;
			}
			case EnumCall.GuildService_checkChapterReward_long_int: {
				return (GofFunction2<Long, Integer>)serv::checkChapterReward;
			}
			case EnumCall.GuildService_checkStageReward_long_int_int: {
				return (GofFunction3<Long, Integer, Integer>)serv::checkStageReward;
			}
			case EnumCall.GuildService_createGuild_Human_String_String_int: {
				return (GofFunction4<Human, String, String, Integer>)serv::createGuild;
			}
			case EnumCall.GuildService_getChallengeGuildInstData_long_int: {
				return (GofFunction2<Long, Integer>)serv::getChallengeGuildInstData;
			}
			case EnumCall.GuildService_getGuildImmo_long: {
				return (GofFunction1<Long>)serv::getGuildImmo;
			}
			case EnumCall.GuildService_getGuildInfo_long_Human: {
				return (GofFunction2<Long, Human>)serv::getGuildInfo;
			}
			case EnumCall.GuildService_getGuildInstData_long: {
				return (GofFunction1<Long>)serv::getGuildInstData;
			}
			case EnumCall.GuildService_getGuildLevel_long: {
				return (GofFunction1<Long>)serv::getGuildLevel;
			}
			case EnumCall.GuildService_getGuildName_long: {
				return (GofFunction1<Long>)serv::getGuildName;
			}
			case EnumCall.GuildService_getMyApplyList_long: {
				return (GofFunction1<Long>)serv::getMyApplyList;
			}
			case EnumCall.GuildService_getNameGuild_String: {
				return (GofFunction1<String>)serv::getNameGuild;
			}
			case EnumCall.GuildService_getNameSimilarGuild_String: {
				return (GofFunction1<String>)serv::getNameSimilarGuild;
			}
			case EnumCall.GuildService_guildImmo_Guild_long_long_int_int_int: {
				return (GofFunction6<Guild, Long, Long, Integer, Integer, Integer>)serv::guildImmo;
			}
			case EnumCall.GuildService_guildJoin_GuildData_long: {
				return (GofFunction2<GuildData, Long>)serv::guildJoin;
			}
			case EnumCall.GuildService_guildLeave_Human_Guild: {
				return (GofFunction2<Human, Guild>)serv::guildLeave;
			}
			case EnumCall.GuildService_isCanApply_Human_long: {
				return (GofFunction2<Human, Long>)serv::isCanApply;
			}
			case EnumCall.GuildService_isCanGuild_long_String: {
				return (GofFunction2<Long, String>)serv::isCanGuild;
			}
			case EnumCall.GuildService_isCanGuildJoin_GuildData_long: {
				return (GofFunction2<GuildData, Long>)serv::isCanGuildJoin;
			}
			case EnumCall.GuildService_isCanRename_long_long_String: {
				return (GofFunction3<Long, Long, String>)serv::isCanRename;
			}
			case EnumCall.GuildService_isRepeatGuildName_String: {
				return (GofFunction1<String>)serv::isRepeatGuildName;
			}
			case EnumCall.GuildService_isUnion_long_long: {
				return (GofFunction2<Long, Long>)serv::isUnion;
			}
			case EnumCall.GuildService_kickOutGuild_Guild_long: {
				return (GofFunction2<Guild, Long>)serv::kickOutGuild;
			}
			case EnumCall.GuildService_mapGuildCDR_Human: {
				return (GofFunction1<Human>)serv::mapGuildCDR;
			}
			case EnumCall.GuildService_removeApplyInfo_long_long: {
				return (GofFunction2<Long, Long>)serv::removeApplyInfo;
			}
			case EnumCall.GuildService_removeApplyInfo_Guild: {
				return (GofFunction1<Guild>)serv::removeApplyInfo;
			}
			case EnumCall.GuildService_removeGuild_long: {
				return (GofFunction1<Long>)serv::removeGuild;
			}
			case EnumCall.GuildService_rename_long_String: {
				return (GofFunction2<Long, String>)serv::rename;
			}
			case EnumCall.GuildService_replyApply_long_long_long: {
				return (GofFunction3<Long, Long, Long>)serv::replyApply;
			}
			case EnumCall.GuildService_seeApplyInfo_long: {
				return (GofFunction1<Long>)serv::seeApplyInfo;
			}
			case EnumCall.GuildService_seeGuildMemberInfo_long: {
				return (GofFunction1<Long>)serv::seeGuildMemberInfo;
			}
			case EnumCall.GuildService_selectAllGuildInfo_long_int: {
				return (GofFunction2<Long, Integer>)serv::selectAllGuildInfo;
			}
			case EnumCall.GuildService_setGuildInstResetType_long_long_EGuildInstResetType: {
				return (GofFunction3<Long, Long, EGuildInstResetType>)serv::setGuildInstResetType;
			}
			case EnumCall.GuildService_stageRewardInfo_long_int: {
				return (GofFunction2<Long, Integer>)serv::stageRewardInfo;
			}
			case EnumCall.GuildService_syncStageReward_long_String_int_int_int_int_boolean: {
				return (GofFunction7<Long, String, Integer, Integer, Integer, Integer, Boolean>)serv::syncStageReward;
			}
			case EnumCall.GuildService_updatHumanGlobalServiceGuildName_long_long: {
				return (GofFunction2<Long, Long>)serv::updatHumanGlobalServiceGuildName;
			}
			case EnumCall.GuildService_update_Objects: {
				return (GofFunction1<Object[]>)serv::update;
			}
			case EnumCall.GuildService_updateDeclare_long_long_String: {
				return (GofFunction3<Long, Long, String>)serv::updateDeclare;
			}
			case EnumCall.GuildService_updateGuildHuman_long_Human_int_long: {
				return (GofFunction4<Long, Human, Integer, Long>)serv::updateGuildHuman;
			}
			case EnumCall.GuildService_updateGuildInfo_Guild: {
				return (GofFunction1<Guild>)serv::updateGuildInfo;
			}
			case EnumCall.GuildService_updateGuildPostMember_Guild_GuildData_boolean: {
				return (GofFunction3<Guild, GuildData, Boolean>)serv::updateGuildPostMember;
			}
			case EnumCall.GuildService_updateIcon_long_long_int: {
				return (GofFunction3<Long, Long, Integer>)serv::updateIcon;
			}
			case EnumCall.GuildService_updateNotice_long_long_String: {
				return (GofFunction3<Long, Long, String>)serv::updateNotice;
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
	public static GuildServiceProxy newInstance() {
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
	private static GuildServiceProxy createInstance(String node, String port, Object id) {
		GuildServiceProxy inst = new GuildServiceProxy();
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
	public void ReduceHp(long guildId, int stage, List harmList, String playerName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_ReduceHp_long_int_List_String, "GuildServiceProxy.ReduceHp", new Object[] {guildId, stage, harmList, playerName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void add(String name) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_add_String, "GuildServiceProxy.add", new Object[] {name});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void addGuildLiveness(Guild guild, int active) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_addGuildLiveness_Guild_int, "GuildServiceProxy.addGuildLiveness", new Object[] {guild, active});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void addGuildTotalContribute(Guild guild, int contribute) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_addGuildTotalContribute_Guild_int, "GuildServiceProxy.addGuildTotalContribute", new Object[] {guild, contribute});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void autoAddGuild(long humanId, int level) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_autoAddGuild_long_int, "GuildServiceProxy.autoAddGuild", new Object[] {humanId, level});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void cancleJoin(long humanId, long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_cancleJoin_long_long, "GuildServiceProxy.cancleJoin", new Object[] {humanId, guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void changeChairmanName(Human human, long guildId, String newName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_changeChairmanName_Human_long_String, "GuildServiceProxy.changeChairmanName", new Object[] {human, guildId, newName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void changeGuildName(String oldName, String newName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_changeGuildName_String_String, "GuildServiceProxy.changeGuildName", new Object[] {oldName, newName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void changeGuildSet(long humanId, long guildId, int QQ, int isApply, int initiationMinLevel) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_changeGuildSet_long_long_int_int_int, "GuildServiceProxy.changeGuildSet", new Object[] {humanId, guildId, QQ, isApply, initiationMinLevel});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void checkChapterReward(long guildId, int chapter) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_checkChapterReward_long_int, "GuildServiceProxy.checkChapterReward", new Object[] {guildId, chapter});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void checkStageReward(long guildId, int stage, int slot) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_checkStageReward_long_int_int, "GuildServiceProxy.checkStageReward", new Object[] {guildId, stage, slot});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void createGuild(Human human, String guildName, String content, int icon) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_createGuild_Human_String_String_int, "GuildServiceProxy.createGuild", new Object[] {human, guildName, content, icon});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getChallengeGuildInstData(long guildId, int stage) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getChallengeGuildInstData_long_int, "GuildServiceProxy.getChallengeGuildInstData", new Object[] {guildId, stage});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getGuildImmo(long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getGuildImmo_long, "GuildServiceProxy.getGuildImmo", new Object[] {guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getGuildInfo(long guildId, Human human) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getGuildInfo_long_Human, "GuildServiceProxy.getGuildInfo", new Object[] {guildId, human});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getGuildInstData(long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getGuildInstData_long, "GuildServiceProxy.getGuildInstData", new Object[] {guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getGuildLevel(long id) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getGuildLevel_long, "GuildServiceProxy.getGuildLevel", new Object[] {id});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getGuildName(long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getGuildName_long, "GuildServiceProxy.getGuildName", new Object[] {guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getMyApplyList(long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getMyApplyList_long, "GuildServiceProxy.getMyApplyList", new Object[] {humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getNameGuild(String name) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getNameGuild_String, "GuildServiceProxy.getNameGuild", new Object[] {name});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void getNameSimilarGuild(String name) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_getNameSimilarGuild_String, "GuildServiceProxy.getNameSimilarGuild", new Object[] {name});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void guildImmo(Guild guild, long guildContribute, long humanId, int type, int contributePlan, int contributeErect) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_guildImmo_Guild_long_long_int_int_int, "GuildServiceProxy.guildImmo", new Object[] {guild, guildContribute, humanId, type, contributePlan, contributeErect});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void guildJoin(GuildData bean, long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_guildJoin_GuildData_long, "GuildServiceProxy.guildJoin", new Object[] {bean, guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void guildLeave(Human human, Guild guild) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_guildLeave_Human_Guild, "GuildServiceProxy.guildLeave", new Object[] {human, guild});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isCanApply(Human human, long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_isCanApply_Human_long, "GuildServiceProxy.isCanApply", new Object[] {human, guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isCanGuild(long humanId, String guildName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_isCanGuild_long_String, "GuildServiceProxy.isCanGuild", new Object[] {humanId, guildName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isCanGuildJoin(GuildData bean, long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_isCanGuildJoin_GuildData_long, "GuildServiceProxy.isCanGuildJoin", new Object[] {bean, guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isCanRename(long guildId, long humanId, String guildName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_isCanRename_long_long_String, "GuildServiceProxy.isCanRename", new Object[] {guildId, humanId, guildName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isRepeatGuildName(String newName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_isRepeatGuildName_String, "GuildServiceProxy.isRepeatGuildName", new Object[] {newName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void isUnion(long guildId, long humanid2) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_isUnion_long_long, "GuildServiceProxy.isUnion", new Object[] {guildId, humanid2});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void kickOutGuild(Guild guild, long humanId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_kickOutGuild_Guild_long, "GuildServiceProxy.kickOutGuild", new Object[] {guild, humanId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void mapGuildCDR(Human human) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_mapGuildCDR_Human, "GuildServiceProxy.mapGuildCDR", new Object[] {human});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void removeApplyInfo(long guildId, long replyId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_removeApplyInfo_long_long, "GuildServiceProxy.removeApplyInfo", new Object[] {guildId, replyId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void removeApplyInfo(Guild guild) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_removeApplyInfo_Guild, "GuildServiceProxy.removeApplyInfo", new Object[] {guild});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void removeGuild(long id) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_removeGuild_long, "GuildServiceProxy.removeGuild", new Object[] {id});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void rename(long guildId, String guildName) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_rename_long_String, "GuildServiceProxy.rename", new Object[] {guildId, guildName});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void replyApply(long humanId, long guildId, long replyId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_replyApply_long_long_long, "GuildServiceProxy.replyApply", new Object[] {humanId, guildId, replyId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void seeApplyInfo(long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_seeApplyInfo_long, "GuildServiceProxy.seeApplyInfo", new Object[] {guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void seeGuildMemberInfo(long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_seeGuildMemberInfo_long, "GuildServiceProxy.seeGuildMemberInfo", new Object[] {guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void selectAllGuildInfo(long humanId, int page) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_selectAllGuildInfo_long_int, "GuildServiceProxy.selectAllGuildInfo", new Object[] {humanId, page});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void setGuildInstResetType(long playerId, long guildId, EGuildInstResetType resetType) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_setGuildInstResetType_long_long_EGuildInstResetType, "GuildServiceProxy.setGuildInstResetType", new Object[] {playerId, guildId, resetType});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void stageRewardInfo(long guildId, int stage) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_stageRewardInfo_long_int, "GuildServiceProxy.stageRewardInfo", new Object[] {guildId, stage});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void syncStageReward(long guildId, String playerName, int stage, int slot, int itemSn, int itemNum, boolean ok) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_syncStageReward_long_String_int_int_int_int_boolean, "GuildServiceProxy.syncStageReward", new Object[] {guildId, playerName, stage, slot, itemSn, itemNum, ok});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updatHumanGlobalServiceGuildName(long humanId, long guildId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_updatHumanGlobalServiceGuildName_long_long, "GuildServiceProxy.updatHumanGlobalServiceGuildName", new Object[] {humanId, guildId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void update(Object... objs) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_update_Objects, "GuildServiceProxy.update", new Object[] {objs});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateDeclare(long guildId, long humanId, String declare) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_updateDeclare_long_long_String, "GuildServiceProxy.updateDeclare", new Object[] {guildId, humanId, declare});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateGuildHuman(long guildId, Human human, int onlineStatus, long time) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_updateGuildHuman_long_Human_int_long, "GuildServiceProxy.updateGuildHuman", new Object[] {guildId, human, onlineStatus, time});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateGuildInfo(Guild guild) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_updateGuildInfo_Guild, "GuildServiceProxy.updateGuildInfo", new Object[] {guild});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateGuildPostMember(Guild guild, GuildData bean, boolean isCDR) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_updateGuildPostMember_Guild_GuildData_boolean, "GuildServiceProxy.updateGuildPostMember", new Object[] {guild, bean, isCDR});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateIcon(long guildId, long humanId, int icon) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_updateIcon_long_long_int, "GuildServiceProxy.updateIcon", new Object[] {guildId, humanId, icon});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void updateNotice(long guildId, long humanId, String notice) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.GuildService_updateNotice_long_long_String, "GuildServiceProxy.updateNotice", new Object[] {guildId, humanId, notice});
		if(immutableOnce) immutableOnce = false;
	}
}
