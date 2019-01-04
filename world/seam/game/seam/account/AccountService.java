package game.seam.account;

import game.msg.Define.ECrossFightType;
import game.msg.MsgAccount.SCAccountLoginQueue;
import game.msg.MsgAccount.SCLoginResult;
import game.msg.MsgFight.SCNewbieFight;
import game.msg.MsgIds;
import game.seam.msg.AccountExtendMsgHandler;
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
import game.seam.account.AccountObject;
import game.worldsrv.keyActivate.KeyActivateServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;

@DistrClass(importClass = {ConnectionStatus.class})
public class AccountService extends Service {
	// 消息处理类
	private AccountExtendMsgHandler msgHandler = MsgHandler.getInstance(AccountExtendMsgHandler.class);

	// 存储选人阶段的信息：key=MsgParamAccount.connId
	public Map<Long, AccountObject> datas = new HashMap<>();
	
	//申请进入列表
	public List<Long> loginApply = new LinkedList<>();
	
	//最大在线人数(在线的+选角色界面的玩家)
	private int loginMaxOnline;
	
	//选角色界面的玩家数量(在Connection修改玩家状态的时候维护)
	private int loginGateNum;
		
	//服务器人数是否已满
	public boolean humanOnlineFull = false;
	
	//服务器是否已满  <渠道Id,是否已满>
	public Map<String, Boolean> serverFullMap = new HashMap<String, Boolean>(); 
	
	//服务器是否可以进行注册
	public boolean isServerFull = false;
	
	//本秒允许玩家登陆数量
	public int loginedNumPerSec = 0;
	
	//本次提示玩家间隔期允许登陆玩家数量（仅作显示记录，不参与任何逻辑）
	public int loginedNumPerTips = 0;
	
	/**
	 * 清理已经不存在的请求，清理操作时间间隔
	 */
	private TickTimer loginClearTimer =  new TickTimer(1 * Time.SEC);
	
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
	 * @param port
	 */
	public AccountService(Port port) {
		super(port);
		loginMaxOnline = ParamManager.loginMaxOnline;
	}
	
	@Override
	public Object getId() {
		return Distr.SERV_GATE;
	}
	
	@Override
	public void pulseOverride() {
		super.pulseOverride();
		
		//当前时间
		long now = Port.getTime();
		
		//检查需要开始登陆加载的角色
		if(loginTimer.isPeriod(now)) {
			loginedNumPerSec = 0;
			loginQueue();
		}
		
		//对登陆队列中的玩家进行提示
		plusCharLoginQueueTips(now);
		//登陆队列数据维护清理
		plusCharLoginClear(now);
	}

	public Port getPort() {
		return port;
	}
	
	/**
	 * 申请玩家ID
	 * @return
	 */
	public long applyHumanId() {
		return port.applySeqHumanId();
	}
	
	/**
	 * 创建代理类
	 * @return
	 */
	public static AccountServiceProxy createProxy() {
		return AccountServiceProxy.newInstance(Config.getGameWorldPartDefaultNodeId(), 
				Config.getGameWorldPartDefaultPortId(), Distr.SERV_GATE);
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
	
	/**
	 * 同步服务器玩家数已满状态
	 */
	@DistrMethod
	public void setHumanOnlineFull(int onlineNum) {
		int totalNum = onlineNum + loginGateNum;
		//Log.temp.info("总人数totalNum={}",totalNum);
		this.humanOnlineFull = totalNum >= loginMaxOnline ? true : false;
	}
	
	/**
	 * 修改最大在线人数
	 * @param maxOnline
	 */
	@DistrMethod
	public void setLoginMaxOnline(int maxOnline){
		loginMaxOnline = maxOnline;
	}
	
	/**
	 * 同步更新选角色界面的玩家数量
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
	
	//----登录排队-开始----
	/**
	 * 增加登陆申请
	 * @param connId
	 */
	public void loginApplyAdd(Long connId) {
		// 加入登录队列
		loginApply.add(connId);
		//Log.temp.info("connId={}",connId);
		Log.game.info("connId:"+connId+"----login---debug----queueNum:"+ParamManager.loginMaxQueue);
		if(humanOnlineFull) {
			Log.game.info("====已达最大在线人数！humanOnlineFull,numOnline={}", loginMaxOnline);
		} else {
			loginQueue();// 立即触发一次登录检查
		}
		
		// 立即登录没有成功，提示玩家等待
		if(loginApply.contains(connId)) {
			//当前时间
			long now = port.getTimeCurrent();
			plusCharLoginQueueTipsOne(now, connId, loginApply.size(), loginApply.size());
		}
	}
	
	/**
	 * 检查需要开始登陆加载的角色
	 */
	private void loginQueue() {
		int sizeLoginApply = loginApply.size();
		if(sizeLoginApply >= loginMaxOnline*20/100) {
			Log.game.info("排队人数={}, 最大允许在线人数={}", sizeLoginApply, loginMaxOnline);
		}
		// 服务器达到最大在线人数, 不再继续登陆新玩家
		if(humanOnlineFull) {
            Log.game.info("online full loginMaxOnline={}", loginMaxOnline);
			return;
		}
		
		//Log.temp.info("排队人数={}",loginApply.size());
		//每秒登录人数检测
		int queueNum = ParamManager.loginMaxQueue;
		while(!loginApply.isEmpty() && loginedNumPerSec < queueNum) {
			//Log.temp.info("loginedNumPerSec={}",loginedNumPerSec);
			//维护登陆队列状态
			Long connId = loginApply.remove(0);
			
			AccountObject obj = datas.get(connId);
			if(null == obj) 
				continue;
			
			// 记录本次允许登陆人数
			loginedNumPerSec++;
			loginedNumPerTips++;
			
			// 获取连接代理
			ConnectionProxy prx = ConnectionProxy.newInstance(obj.connPoint);
			
			// 更新链接状态
			obj.connStatus.status = ConnectionStatus.STATUS_GATE;
			prx.updateStatus(obj.connStatus);
			checkGateNum(obj.connStatus.status);
			
			//发送登录请求结果
			SCLoginResult.Builder reply = SCLoginResult.newBuilder();
			reply.setResultCode(0);// 返回结果编号：0成功，非0失败（即sysMsg中的sn）
			reply.setKeyActivate(false);
			reply.setAccountId(obj.connStatus.accountId);
			if(isServerFull){
				reply.setIsServerFull(true);
			}else{
				reply.setIsServerFull(serverFullMap.get(obj.connStatus.channel));
			}
			
			// 是否显示Gm
			if(ParamManager.openGM){
				reply.setShowGm(1);
			}else{
				reply.setShowGm(0);
			}
			
			// 是否联系客服开放
//			reply.setShowService(ConfGlobalUtils.getValue(ConfGlobalKey.是否显示客户按钮));
			if(ParamManager.openShowService){
				reply.setShowService(1);
			}else{
				reply.setShowService(0);
			}
			
			// 是否礼包激活码开放
//			reply.setShowGiftCode(ConfGlobalUtils.getValue(ConfGlobalKey.是否显示礼包码按钮));
			if(ParamManager.openGiftKey){
				reply.setShowGiftCode(1);
			}else{
				reply.setShowGiftCode(0);
			}
			
			// 是否激活码开放
			if(ParamManager.openActiveKey){
				KeyActivateServiceProxy prox = KeyActivateServiceProxy.newInstance();
				prox.isActivate(obj.connStatus.accountId);
				prox.listenResult(this::_result_isActivate, "obj", obj, "reply", reply);
			}else{
				prx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply));
//				if (Log.game.isDebugEnabled()) {
//					Log.game.debug("===登陆成功：account={}", obj.connStatus.accountId);
//				}
				Log.game.debug("===登陆成功：account={}", obj.connStatus.accountId);
			}
			
		}
	}
	/**
	 * 查看玩家是否激活
	 * @param results
	 * @param context
	 */
	private void _result_isActivate(Param results, Param context){
		AccountObject obj = context.get("obj");
		SCLoginResult.Builder reply = context.get("reply");
		boolean result = results.get("result");
		reply.setKeyActivate(!result);
		ConnectionProxy prx = ConnectionProxy.newInstance(obj.connPoint);
		prx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply));
		if (Log.game.isDebugEnabled()) {
			Log.game.debug("===登陆成功：account={}", obj.connStatus.accountId);
		}
	}
	
	/**
	 * 对登陆队列中的玩家进行提示(有多少人在排队，需要等多长时间)
	 */
	private void plusCharLoginQueueTipsOne(long now, Long connId, int num, int totalNum) {
		//登陆信息
		AccountObject obj = datas.get(connId);
		if(null == obj) 
			return;
		
		//剩余时间
		int sec;
		if(humanOnlineFull) {
			sec = (num + 1) * ParamManager.loginTimeFull;
		} else {
			sec = num / ParamManager.loginMaxQueue + 1;
		}
		
		//提示客户端
		SCAccountLoginQueue.Builder msgResult = SCAccountLoginQueue.newBuilder();
		msgResult.setNum(num);
		msgResult.setSec(sec);
		msgResult.setFull(humanOnlineFull);
		msgResult.setTotal(totalNum);
		
		ConnectionProxy prxConn = ConnectionProxy.newInstance(obj.connPoint);
		prxConn.sendMsg(MsgIds.SCAccountLoginQueue, new Chunk(msgResult));
	}
	
	/**
	 * 对登陆队列中的玩家进行提示
	 */
	private void plusCharLoginQueueTips(long now) {
		if(!loginTipsTimer.isPeriod(now))
			return;
		
		loginTipsTimer.start(ParamManager.loginIntervalTips * Time.SEC);
		
		//每隔ConfParamKey.LOGIN_INTERVAL_TIPS秒，提示排队队列中的所有玩家，现在的排队状况
		for(int i = 0; i < loginApply.size(); i++) {
			Long connId = loginApply.get(i);
			//排队人数
			int num = i + 1;

			//提示队列中的玩家
			plusCharLoginQueueTipsOne(now, connId, num, loginApply.size());
		}
		
		//如果有排队 那么输出排队信息
		if(!loginApply.isEmpty()) {
			Log.game.info("当前排队中：申请登陆人数={}，正在登陆人数={}", loginApply.size(), loginedNumPerTips);
			loginedNumPerTips = 0;
		}
	}
	
	/**
	 * 登陆队列数据维护清理
	 * @param now
	 */
	private void plusCharLoginClear(long now) {
		if(!loginClearTimer.isPeriod(now)) return;
		loginClearTimer.start(ParamManager.loginIntervalClear * Time.SEC);
		
		//清理已经不存在的请求
		for(Iterator<Long> iter = loginApply.iterator(); iter.hasNext();) {
			Long connId = iter.next();
			if(!datas.containsKey(connId)) {
				iter.remove();
			}
		}
	}
	
	//----登录排队-结束----
	
	/**
	 * 每个service预留空方法
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {
		
	}
	
	/**
	 * 新手战斗
	 */
	@DistrMethod
	public void newbieFightResult(long connId, long humanId, int fightType, int result, String token, 
			String crossIp, int crossPort, long stageId, int stageSn, int mapSn) {
		//登陆信息
		AccountObject obj = datas.get(connId);
		if(null == obj){
			return;
		}
		if(result!=0){
			SCNewbieFight.Builder msg = SCNewbieFight.newBuilder();			
			msg.setFightType(ECrossFightType.valueOf(fightType));
			msg.setAreaSwitchKey(String.valueOf(mapSn));
			msg.setResult(result);
			msg.setInstSn(stageSn);
			msg.setToken(token);
			msg.setIp(crossIp);
			msg.setPort(crossPort);
			msg.setTempId(humanId);
			
			ConnectionProxy prxConn = ConnectionProxy.newInstance(obj.connPoint);
			prxConn.sendMsg(MsgIds.SCNewbieFight, new Chunk(msg));
			return;
		}
		SCNewbieFight.Builder msg = SCNewbieFight.newBuilder();
		msg.setFightType(ECrossFightType.valueOf(fightType));
		msg.setAreaSwitchKey(String.valueOf(mapSn));
		msg.setResult(result);
		msg.setInstSn(stageSn);
		msg.setToken(token);
		msg.setIp(crossIp);
		msg.setPort(crossPort);
		msg.setTempId(humanId);
		
		ConnectionProxy prxConn = ConnectionProxy.newInstance(obj.connPoint);
		prxConn.sendMsg(MsgIds.SCNewbieFight, new Chunk(msg));
	}
}
