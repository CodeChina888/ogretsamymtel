package crosssrv.seam.token;

import core.CallPoint;
import core.Chunk;
import core.Port;
import core.connsrv.ConnectionProxy;
import core.support.ConnectionStatus;
import core.support.Distr;
import core.support.ManagerBase;
import crosssrv.combatant.CombatantGlobalInfo;
import crosssrv.combatant.CombatantGlobalService;
import game.msg.MsgCross.SCCombatantKick;
import game.msg.MsgCross.SCTokenLoginResult;
import game.msg.MsgIds;
import game.worldsrv.support.D;

public class TokenManager extends ManagerBase {

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static TokenManager inst() {
		return inst(TokenManager.class);
	}

	/**
	 * 玩家登陆 在此处验证玩家的身份 根据当前逻辑此处只传递用户名即可 永远验证通过 以后根据业务需求再修改
	 * 如果账号之前已登录，那么就踢掉之前的玩家连接，本次登录也算失败。
	 * 
	 */
	public void login(CallPoint connPoint, ConnectionStatus connStatus, String token) {
		Port port = Port.getCurrent();
		// 先检验玩家是否已登录
		CombatantGlobalService serv = port.getService(D.SERV_COMBATANT_GLOBAL);
		CombatantGlobalInfo cgInfo = serv.getCombatantGlobalInfo(connStatus.humanId);
		if (cgInfo == null || cgInfo.isLogined) {
			// 如果账号之前已登录 则踢出 本次登录也失败
			serv.kick(connStatus.humanId, "other login!");
			// 发送登录请求结果
			ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
			SCTokenLoginResult.Builder reply = SCTokenLoginResult.newBuilder();
			reply.setResultCode(-1);
			reply.setIsServerFull(false);
			prx.sendMsg(game.msg.MsgIds.SCTokenLoginResult, new Chunk(reply));
			return;
		}

		// 验证帐号
		TokenService tokenServ = port.getService(Distr.SERV_GATE);

		// 检查下之前是否有登陆中的玩家 有就踢了
		// 这里仅踢登陆中的 后面会踢在游戏中的玩家
		TokenObject tokenOld = getByHumanId(tokenServ, connStatus.humanId);
		if (tokenOld != null) {
			ConnectionProxy connPrx = ConnectionProxy.newInstance(tokenOld.connPoint);

			// 提示另一个玩家 被踢
			SCCombatantKick.Builder kickMsg = SCCombatantKick.newBuilder();
			kickMsg.setReason("other login!");

			// 发消息
			connPrx.sendMsg(MsgIds.SCCombatantKick, new Chunk(kickMsg));

			// 断开另一个玩家的连接
			connPrx.close();
		}

		// 如果账号之前已登录 则踢出 本次登录也失败
		if (!serv.checkToken(connStatus.humanId, token)) {
			serv.kick(connStatus.humanId, "other login!");
			// 发送登录请求结果
			ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
			SCTokenLoginResult.Builder reply = SCTokenLoginResult.newBuilder();
			reply.setResultCode(-1);
			reply.setIsServerFull(false);
			prx.sendMsg(game.msg.MsgIds.SCTokenLoginResult, new Chunk(reply));
			return;
		}
		// 注册玩家消息
		long connId = (long) connPoint.servId;
		TokenObject obj = new TokenObject(connId, tokenServ, connStatus, connPoint, cgInfo);
		tokenServ.datas.put(obj.getId(), obj);
		// 登录排队
		tokenServ.loginApplyAdd(connId);
	}

	/**
	 * 在登陆信息中 查找同account的数据
	 * 
	 * @param serv
	 * @param humanId
	 * @return
	 */
	public TokenObject getByHumanId(TokenService serv, long humanId) {
		// 遍历寻找登陆中的玩家信息 看有没有同id的
		for (TokenObject o : serv.datas.values()) {
			if (humanId == o.humanId) {
				return o;
			}
		}
		return null;
	}
}