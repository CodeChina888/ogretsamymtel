package crosssrv.seam.token;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.CallPoint;
import core.Chunk;
import core.Port;
import core.Service;
import core.connsrv.ConnectionProxy;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Config;
import core.support.ConnectionStatus;
import core.support.Distr;
import core.support.MsgHandler;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import crosssrv.seam.msg.TokenExtendMsgHandler;
import crosssrv.stage.CrossStageGlobalServiceProxy;
import crosssrv.support.Log;
import game.msg.MsgCross.SCTokenLoginQueue;
import game.msg.MsgCross.SCTokenLoginResult;
import game.msg.MsgIds;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;

@DistrClass(importClass = { ConnectionStatus.class })
public class TokenService extends Service {
	// 消息处理类
	private TokenExtendMsgHandler msgHandler = MsgHandler.getInstance(TokenExtendMsgHandler.class);

	// 存储选人阶段的信息
	public Map<Long, TokenObject> datas = new HashMap<>();

	// 申请进入列表
	public List<Long> loginApply = new LinkedList<>();

	// 最大在线人数(在线的+选角色界面的玩家)
	private int loginMaxOnline;

	// 选角色界面的玩家数量(在Connection修改玩家状态的时候维护)
	private int loginGateNum;

	// 服务器人数是否已满
	public boolean combatantOnlineFull = false;

	// 本秒允许玩家登陆数量
	public int loginedNumPerSec = 0;

	// 本次提示玩家间隔期允许登陆玩家数量（仅作显示记录，不参与任何逻辑）
	public int loginedNumPerTips = 0;

	/**
	 * 清理已经不存在的请求，清理操作时间间隔
	 */
	private TickTimer loginClearTimer = new TickTimer(1 * Time.SEC);

	/**
	 * 给登陆队列中角色的提示间隔(秒)
	 */
	private TickTimer loginTipsTimer = new TickTimer(1 * Time.SEC);

	/**
	 * 每批次登录的时间间隔
	 */
	private TickTimer loginTimer = new TickTimer(1 * Time.SEC);

	/**
	 * 构造函数
	 * 
	 * @param port
	 */
	public TokenService(Port port) {
		super(port);
		loginMaxOnline = ParamManager.loginMaxOnline;
	}

	@DistrMethod
	public void msgHandler(long connId, ConnectionStatus status, byte[] msgbuf) {
		CallPoint connPoint = new CallPoint();
		connPoint.nodeId = port.getCallFromNodeId();
		connPoint.portId = port.getCallFromPortId();
		connPoint.servId = connId;

		msgHandler.handle(msgbuf, "connPoint", connPoint, "serv", this, "connId", connId, "connStatus", status);
	}

	@DistrMethod
	public void connClosed(long connId) {
		datas.remove(connId);
		checkGateNum(ConnectionStatus.STATUS_LOSTED);
	}

	@DistrMethod
	public void connCheck(long connId) {
		port.returns(datas.containsKey(connId));
	}

	@Override
	public Object getId() {
		return Distr.SERV_GATE;
	}

	@Override
	public void pulseOverride() {
		super.pulseOverride();

		// 当前时间
		long now = Port.getTime();

		// 检查需要开始登陆加载的角色
		if (loginTimer.isPeriod(now)) {
			loginedNumPerSec = 0;
			loginQueue();
		}

		// 对登陆队列中的玩家进行提示
		plusCharLoginQueueTips(now);
		// 登陆队列数据维护清理
		plusCharLoginClear(now);
	}

	public Port getPort() {
		return port;
	}

	/**
	 * 创建代理类
	 * 
	 * @param nodeId
	 * @param portId
	 * @return
	 */
	public static TokenServiceProxy createProxy(String nodeId, String portId) {
		return TokenServiceProxy.newInstance(nodeId, portId, Distr.SERV_GATE);
	}

	// ----登录排队-开始----
	/**
	 * 增加登陆申请
	 * 
	 * @param connId
	 */
	public void loginApplyAdd(Long connId) {
		// 加入登录队列
		loginApply.add(connId);
		// Log.temp.info("connId={}",connId);
		// 立即触发一次登录检查
		loginQueue();

		// 立即登录没有成功，提示玩家等待
		if (loginApply.contains(connId)) {
			// 当前时间
			long now = port.getTimeCurrent();
			plusCharLoginQueueTipsOne(now, connId, loginApply.size());
		}
	}

	/**
	 * 检查需要开始登陆加载的角色
	 */
	private void loginQueue() {
		// 服务器达到最大在线人数, 不再继续登陆新玩家

		if (combatantOnlineFull)
			return;
		// Log.temp.info("排队人数={}",loginApply.size());
		// 每秒登录人数检测
		int queueNum = ParamManager.loginMaxQueue;
		while (!loginApply.isEmpty() && loginedNumPerSec < queueNum) {
			// Log.temp.info("loginedNumPerSec={}",loginedNumPerSec);
			// 维护登陆队列状态
			Long connId = loginApply.remove(0);

			TokenObject obj = datas.get(connId);
			if (obj == null)
				continue;

			// 记录本次允许登陆人数
			loginedNumPerSec++;
			loginedNumPerTips++;
			// 获取连接代理
			ConnectionProxy prx = ConnectionProxy.newInstance(obj.connPoint);

			// 更新链接状态
			obj.status.status = ConnectionStatus.STATUS_GATE;
			obj.info.connPoint = obj.connPoint;
			prx.updateStatus(obj.status);
			checkGateNum(obj.status.status);

			String nodeId = Config.getCrossPartDefaultNodeId();
			String portId = Config.getCrossPartDefaultPortId();
			CrossStageGlobalServiceProxy prx1 = CrossStageGlobalServiceProxy.newInstance(nodeId, portId,
					D.CROSS_SERV_STAGE_GLOBAL);
			prx1.login(obj.info);
			prx1.listenResult(this::_result_login, "tokenObj", obj);
		}
	}

	public void _result_login(Param results, Param context) {
		TokenObject tokenObj = context.get("tokenObj");

		// 连接代理
		ConnectionProxy prxConn = ConnectionProxy.newInstance(tokenObj.connPoint);

		// 更新连接状态
		String node = results.get("node");
		String port = results.get("port");

		ConnectionStatus status = tokenObj.status;
		status.humanId = tokenObj.humanId;
		status.stageNodeId = node;
		status.stagePortId = port;
		status.status = ConnectionStatus.STATUS_PLAYING;

		prxConn.updateStatus(status);
		tokenObj.serv.checkGateNum(status.status);
		// 清理当前的缓存数据
		tokenObj.serv.datas.remove(tokenObj.getId());

		// 发送登录请求结果
		ConnectionProxy prx = ConnectionProxy.newInstance(tokenObj.connPoint);
		SCTokenLoginResult.Builder reply = SCTokenLoginResult.newBuilder();
		prx.sendMsg(MsgIds.SCTokenLoginResult, new Chunk(reply));
	}

	/**
	 * 登陆队列数据维护清理
	 * 
	 * @param now
	 */
	private void plusCharLoginClear(long now) {
		if (!loginClearTimer.isPeriod(now))
			return;
		loginClearTimer.start(ParamManager.loginIntervalClear * Time.SEC);

		// 清理已经不存在的请求
		for (Iterator<Long> iter = loginApply.iterator(); iter.hasNext();) {
			Long connId = iter.next();
			if (!datas.containsKey(connId)) {
				iter.remove();
			}
		}
	}

	/**
	 * 对登陆队列中的玩家进行提示
	 */
	private void plusCharLoginQueueTips(long now) {
		if (!loginTipsTimer.isPeriod(now))
			return;
		loginTipsTimer.start(ParamManager.loginIntervalTips * Time.SEC);

		// 每隔ConfParamKey.LOGIN_INTERVAL_TIPS秒，提示排队队列中的所有玩家，现在的排队状况
		for (int i = 0; i < loginApply.size(); i++) {
			Long connId = loginApply.get(i);
			// 排队人数
			int num = i + 1;

			// 提示队列中的玩家
			plusCharLoginQueueTipsOne(now, connId, num);
		}

		// 如果有排队 那么输出排队信息
		if (!loginApply.isEmpty()) {
			Log.cross.info("当前排队中：申请登陆人数={}，正在登陆人数={}", loginApply.size(), loginedNumPerTips);
			loginedNumPerTips = 0;
		}
	}

	/**
	 * 对登陆队列中的玩家进行提示(有多少人在排队，需要等多长时间)
	 */
	private void plusCharLoginQueueTipsOne(long now, Long connId, int num) {
		// 登陆信息
		TokenObject obj = datas.get(connId);
		if (obj == null)
			return;

		// 剩余时间
		int sec;
		if (combatantOnlineFull) {
			sec = (num + 1) * ParamManager.loginTimeFull;
		} else {
			sec = num / ParamManager.loginMaxQueue + 1;
		}

		// 提示客户端
		SCTokenLoginQueue.Builder msgResult = SCTokenLoginQueue.newBuilder();
		msgResult.setNum(num);
		msgResult.setSec(sec);
		msgResult.setFull(combatantOnlineFull);

		ConnectionProxy prxConn = ConnectionProxy.newInstance(obj.connPoint);
		prxConn.sendMsg(MsgIds.SCTokenLoginQueue, new Chunk(msgResult));
	}

	// ----登录排队-结束----

	/**
	 * 同步更新选角色界面的玩家数量
	 * 
	 * @param status
	 */
	@DistrMethod
	public void checkGateNum(int status) {
		if (status == ConnectionStatus.STATUS_GATE) {
			loginGateNum++;
		} else {
			loginGateNum--;
			if (loginGateNum < 0) {
				loginGateNum = 0;
			}
		}
	}

	/**
	 * 修改最大在线人数
	 * 
	 * @param maxOnline
	 */
	@DistrMethod
	public void setLoginMaxOnline(int maxOnline) {
		loginMaxOnline = maxOnline;
	}

	/**
	 * 同步服务器玩家数已满状态
	 * 
	 * @param full
	 */
	public void setCombatantOnlineFull(int onlineNum) {
		int totalNum = onlineNum + loginGateNum;
		// Log.fight.info("总人数totalNum={},max={}", totalNum, loginMaxOnline);
		this.combatantOnlineFull = totalNum >= loginMaxOnline ? true : false;
	}
}
