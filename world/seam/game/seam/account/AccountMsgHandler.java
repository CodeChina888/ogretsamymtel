package game.seam.account;

import game.msg.Define.ECrossFightType;
import game.msg.MsgAccount.CSAccountBind;
import game.msg.MsgAccount.CSAccountReconnect;
import game.msg.MsgAccount.CSLogin;
import game.msg.MsgFight.CSNewbieFight;
import game.msg.MsgLogin.CSCharacterCreate;
import game.msg.MsgLogin.CSCharacterCreateName;
import game.msg.MsgLogin.CSCharacterDelete;
import game.msg.MsgLogin.CSCharacterLogin;
import game.msg.MsgLogin.CSQueryCharacters;
import game.seam.msg.MsgParamAccount;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Utils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.google.protobuf.GeneratedMessage;

import core.CallPoint;
import core.Port;
import core.support.Config;
import core.support.ConnectionStatus;
import core.support.Distr;
import core.support.observer.MsgReceiver;

public class AccountMsgHandler {
	// 下属监听消息
	public static final Set<Class<? extends GeneratedMessage>> methods = new HashSet<>();
	
	static {
		// 寻找本类监听的消息
		Method[] mths = AccountMsgHandler.class.getMethods();
		for (Method m : mths) {
			// 不是监听函数的忽略
			if (!m.isAnnotationPresent(MsgReceiver.class)) {
				continue;
			}

			// 记录
			MsgReceiver ann = m.getAnnotation(MsgReceiver.class);
			methods.add(ann.value()[0]);
		}
	}

	/**
	 * 玩家登陆请求
	 * @param param
	 */
	@MsgReceiver(CSLogin.class)
	public void _msg_CSLogin(MsgParamAccount param) {
		CSLogin msg = param.getMsg();
		CallPoint connPoint = param.getConnPoint();
		ConnectionStatus connStatus = param.getConnStatus();
		//connStatus.channel = msg.getChannel();
		//connStatus.serverId = msg.getServerId();
		int serverId = msg.getServerId();
		if (!ParamManager.isServerIdValid(serverId)) {
			AccountManager.inst().sendSCLoginResult(connPoint, 100);
			return;
		}
		connStatus.serverId = serverId;
		// 登录验证
		AccountManager.inst()._msg_CSLogin(connPoint, connStatus, msg);
		
	}
	
	/**
	 * 绑定账号
	 * @param param
	 */
	@MsgReceiver(CSAccountBind.class)
	public void _msg_CSAccountBind(MsgParamAccount param) {
		CSAccountBind msg = param.getMsg();
		AccountService serv = param.getService();
		long connId = param.getConnId();
		AccountObject accObj = serv.datas.get(connId);
		// 登录验证
		AccountManager.inst()._msg_CSAccountBind(accObj, msg);
	}

	/**
	 * 查询角色列表的请求
	 * @param param
	 */
	@MsgReceiver(CSQueryCharacters.class)
	public void _msg_CSQueryCharacters(MsgParamAccount param) {
		Long connId = param.getConnId();
		AccountService serv = param.getService();
		AccountObject obj = serv.datas.get(connId);
		if(null == obj){//白名单开启,不在白名单中会使新建立角色时为空
			return;
		}
		AccountManager.inst()._msg_CSQueryCharacters(obj, obj.connStatus.accountId, obj.getServerId());
	}

	/**
	 * 创角时请求获取随机名字
	 * @param param
	 */
	@MsgReceiver(CSCharacterCreateName.class)
	public void _msg_CSCharacterCreateName(MsgParamAccount param) {
		CSCharacterCreateName msg = param.getMsg();
		AccountService serv = param.getService();
		long connId = param.getConnId();
		AccountObject accObj = serv.datas.get(connId);
		if (accObj == null) { // maybe offline
		    return;
        }

		AccountManager.inst()._msg_CSCharacterCreateName(accObj, msg.getProfession(), msg.getSex());
	}

	/**
	 * 角色创建请求
	 * @param param
	 */
	@MsgReceiver(CSCharacterCreate.class)
	public void _msg_CSCharacterCreate(MsgParamAccount param) {
		CSCharacterCreate msg = param.getMsg();
		AccountService serv = param.getService();
		long connId = param.getConnId();
		AccountObject accObj = serv.datas.get(connId);
		String name = msg.getName();

		if (name == null || name.isEmpty()) {
			// 50名字不能为空
			AccountManager.inst().sendMsgSCCharacterCreateResult(accObj, 0, -1, 50);
			return;
		}
		AccountManager.inst()._msg_CSCharacterCreate(accObj, name, msg.getProfession());
	}

	/**
	 * 角色删除请求
	 * @param param
	 */
	@MsgReceiver(CSCharacterDelete.class)
	public void _msg_CSCharacterDelete(MsgParamAccount param) {
		CSCharacterDelete msg = param.getMsg();
		AccountService serv = param.getService();
		long connId = param.getConnId();
		AccountObject accObj = serv.datas.get(connId);

		AccountManager.inst()._msg_CSCharacterDelete(accObj, msg.getId());
	}

	/**
	 * 角色进入游戏请求
	 * @param param
	 */
	@MsgReceiver(CSCharacterLogin.class)
	public void _msg_CSCharacterLogin(MsgParamAccount param) {
		CSCharacterLogin msg = param.getMsg();
		long humanId = msg.getHumanId();
		// 获取AccountObject
		Long connId = param.getConnId();
		AccountService serv = param.getService();
		AccountObject obj = serv.datas.get(connId);
		// 登陆游戏
		AccountManager.inst()._msg_CSCharacterLogin(obj, humanId, false);
	}

	/**
	 * 断线重连
	 * @param param
	 */
	@MsgReceiver(CSAccountReconnect.class)
	public void _msg_CSAccountReconnect(MsgParamAccount param) {
		CSAccountReconnect msg = param.getMsg();
		CallPoint connPoint = param.getConnPoint();
		Long connId = param.getConnId();

		AccountManager.inst()._msg_CSAccountReconnect(connId, connPoint, msg.getName());
	}

	/******************************************************************
	 * 新手战斗相关
	 ******************************************************************/
	/**
	 * 新手战斗
	 * @param param
	 */
	@MsgReceiver(CSNewbieFight.class)
	public void _msg_CSNewbieFight(MsgParamAccount param) {
		//CallPoint connPoint = param.getConnPoint();
		Long connId = param.getConnId();
		AccountService serv = param.getService();
		AccountObject accObj = serv.datas.get(connId);		
		if(accObj.humanId != 0){//已经战过了
			return;
		}
		if(accObj.connStatus.status!=ConnectionStatus.STATUS_GATE){//登录状态中
			return;
		}
		long id = Port.applyId();
		accObj.humanId = id;
		
		Port port = Port.getCurrent();
		int crossServerIndex = 0;
		if (Config.CROSS_SERVER_NUM > 1) {
			crossServerIndex = Utils.randomBetween(1, Config.CROSS_SERVER_NUM) - 1;
		}
		int crossServerId = Config.crossSrvIDMap.get(crossServerIndex);
		String crossNodeId = Config.getCrossDefaultNodeId(crossServerId);
		CallPoint toPoint = new CallPoint(crossNodeId,
				Distr.CROSS_PORT_DEFAULT, D.CROSS_SERV_SINGLE_FIGHT);
		
		int stageSn = ParamManager.newbieStageSn;
		int mapSn = ParamManager.newbieMapSn;
		// SF009_NewbieFight
		port.call(toPoint,9,new Object[] {
			connId,
			id,
			ECrossFightType.FIGHT_NEWBIE_VALUE,
			Config.getGameWorldPartDefaultNodeId(Distr.NODE_DEFAULT),
			Distr.PORT_DEFAULT, stageSn, mapSn });
	}
	
}
