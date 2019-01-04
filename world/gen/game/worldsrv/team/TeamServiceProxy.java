package game.worldsrv.team;
                    
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
import game.worldsrv.team.TeamData;

@GofGenFile
public final class TeamServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int TeamService_teamApplyJoin_Human_int_int = 1;
		public static final int TeamService_teamCreate_Human_int = 2;
		public static final int TeamService_teamEndRep_int = 3;
		public static final int TeamService_teamEnterRep_int_int = 4;
		public static final int TeamService_teamFind_Human_int = 5;
		public static final int TeamService_teamJoin_Human_int = 6;
		public static final int TeamService_teamJoinRecord_Human = 7;
		public static final int TeamService_teamKickOut_long_int_int = 8;
		public static final int TeamService_teamLeave_long_int_int = 9;
		public static final int TeamService_teamMatch_int_int = 10;
		public static final int TeamService_teamMatchCancel_int_int = 11;
	}
	private static final String SERV_ID = "worldsrv.team.TeamService";
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private TeamServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		TeamService serv = (TeamService)service;
		switch (methodKey) {
			case EnumCall.TeamService_teamApplyJoin_Human_int_int: {
				return (GofFunction3<Human, Integer, Integer>)serv::teamApplyJoin;
			}
			case EnumCall.TeamService_teamCreate_Human_int: {
				return (GofFunction2<Human, Integer>)serv::teamCreate;
			}
			case EnumCall.TeamService_teamEndRep_int: {
				return (GofFunction1<Integer>)serv::teamEndRep;
			}
			case EnumCall.TeamService_teamEnterRep_int_int: {
				return (GofFunction2<Integer, Integer>)serv::teamEnterRep;
			}
			case EnumCall.TeamService_teamFind_Human_int: {
				return (GofFunction2<Human, Integer>)serv::teamFind;
			}
			case EnumCall.TeamService_teamJoin_Human_int: {
				return (GofFunction2<Human, Integer>)serv::teamJoin;
			}
			case EnumCall.TeamService_teamJoinRecord_Human: {
				return (GofFunction1<Human>)serv::teamJoinRecord;
			}
			case EnumCall.TeamService_teamKickOut_long_int_int: {
				return (GofFunction3<Long, Integer, Integer>)serv::teamKickOut;
			}
			case EnumCall.TeamService_teamLeave_long_int_int: {
				return (GofFunction3<Long, Integer, Integer>)serv::teamLeave;
			}
			case EnumCall.TeamService_teamMatch_int_int: {
				return (GofFunction2<Integer, Integer>)serv::teamMatch;
			}
			case EnumCall.TeamService_teamMatchCancel_int_int: {
				return (GofFunction2<Integer, Integer>)serv::teamMatchCancel;
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
	public static TeamServiceProxy newInstance() {
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
	private static TeamServiceProxy createInstance(String node, String port, Object id) {
		TeamServiceProxy inst = new TeamServiceProxy();
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
	
	public void teamApplyJoin(Human human, int teamId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamApplyJoin_Human_int_int, "TeamServiceProxy.teamApplyJoin", new Object[] {human, teamId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamCreate(Human human, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamCreate_Human_int, "TeamServiceProxy.teamCreate", new Object[] {human, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamEndRep(int teamId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamEndRep_int, "TeamServiceProxy.teamEndRep", new Object[] {teamId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamEnterRep(int teamId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamEnterRep_int_int, "TeamServiceProxy.teamEnterRep", new Object[] {teamId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamFind(Human human, int teamId) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamFind_Human_int, "TeamServiceProxy.teamFind", new Object[] {human, teamId});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamJoin(Human human, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamJoin_Human_int, "TeamServiceProxy.teamJoin", new Object[] {human, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamJoinRecord(Human human) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamJoinRecord_Human, "TeamServiceProxy.teamJoinRecord", new Object[] {human});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamKickOut(long humanIdKickout, int teamId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamKickOut_long_int_int, "TeamServiceProxy.teamKickOut", new Object[] {humanIdKickout, teamId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamLeave(long humanIdLeave, int teamId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamLeave_long_int_int, "TeamServiceProxy.teamLeave", new Object[] {humanIdLeave, teamId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamMatch(int teamId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamMatch_int_int, "TeamServiceProxy.teamMatch", new Object[] {teamId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
	
	public void teamMatchCancel(int teamId, int actInstSn) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(immutableOnce, remote, EnumCall.TeamService_teamMatchCancel_int_int, "TeamServiceProxy.teamMatchCancel", new Object[] {teamId, actInstSn});
		if(immutableOnce) immutableOnce = false;
	}
}
