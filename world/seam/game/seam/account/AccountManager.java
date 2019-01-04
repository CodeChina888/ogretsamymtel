package game.seam.account;

import game.msg.Define;
import game.msg.Define.ELoginType;
import game.msg.MsgAccount.CSAccountBind;
import game.msg.MsgAccount.CSLogin;
import game.msg.MsgAccount.SCAccountBind;
import game.msg.MsgAccount.SCAccountReconnectResult;
import game.msg.MsgAccount.SCLoginResult;
import game.msg.MsgCommon.SCHumanKick;
import game.msg.MsgIds;
import game.msg.MsgInform;
import game.msg.MsgLogin;
import game.msg.MsgLogin.SCCharacterCreateName;
import game.msg.MsgLogin.SCCharacterCreateResult;
import game.msg.MsgLogin.SCCharacterDeleteResult;
import game.platform.DistrPF;
import game.platform.login.LoginServiceProxy;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.entity.Human;
import game.worldsrv.human.HumanApplyServiceProxy;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.name.NameServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.support.AssetsTxtFix;
import game.worldsrv.support.C;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.SensitiveWordFilter;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSONArray;

import core.CallPoint;
import core.Chunk;
import core.Port;
import core.Record;
import core.connsrv.ConnectionProxy;
import core.dbsrv.DB;
import core.support.Config;
import core.support.ConnectionStatus;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.RandomUtils;
import core.support.observer.Listener;

public class AccountManager extends ManagerBase {

	/**
	 * 获取实例
	 * @return
	 */
	public static AccountManager inst() {
		return inst(AccountManager.class);
	}
		
	@Listener(EventKey.HumanLoginFinish)
	public void _listener_HumanLoginFinish(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===_listener_HumanLoginFinish humanObj is null");
			return;
		}
		Human human = humanObj.getHuman();
		human.setSessionKey((long) humanObj.connPoint.servId);
		// 这里做同步操作 避免登陆根据sessionKey查询时需要刷新写缓存
		human.update(true);
	}

	/**
	 * 玩家登陆 在此处验证玩家的身份 根据当前逻辑此处只传递用户名即可 永远验证通过 以后根据业务需求再修改
	 * 如果账号之前已登录，那么就踢掉之前的玩家连接，本次登录也算失败。
	 */
	public void _msg_CSLogin(CallPoint connPoint, ConnectionStatus connStatus, CSLogin msg) {
		Log.game.info("login--------debug--------connPoint:"+connPoint+",connStatus:"+connStatus+", msg:"+msg);
		// 先验证版本号（该值必须和客户端登录时提交的验证版本号一致才允许登录）
		String version = msg.getVersion();// 最新版本号，验证通过才允许进入服务器
		if (ParamManager.openCheckVersion) {
			// 开启检查版本号，如不一致则不让登录
			long newlyVersion = Utils.getVersionNum(version);
			if (newlyVersion < ParamManager.newlyVersion) {// 小于版本，返回提示
				// 获取连接代理
				ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
				// 发送登录请求结果
				SCLoginResult.Builder reply = SCLoginResult.newBuilder();
				reply.setResultCode(6);// 6本地版本号与最新服务器不一致，导致您无法登陆服务器！
				prx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply));
				return;
			}
		}
		
		ELoginType loginType = msg.getLoginType();// 登录类型
		String channel = msg.getChannel(); // 渠道
		// 平台账号登录需要的参数
		String account = msg.getAccount();// 账号，即用户ID
		String password = msg.getPassword();// 密码，即访问口令（提供给服务器验证用的）
		String timeStamp = msg.getTimestamp();
		String token = msg.getToken();
		int serverId = msg.getServerId(); //服务器ID
		Log.game.info("===登陆  serverId check：uid={} token={}  serverId={}", account, password,serverId);
		
		if (ParamManager.whileListStatus) {// 如果启动白名单，只有白名单里的才能进
			if(!AssetsTxtFix.AccountWhiteListSet.contains(account)){
				sendSCLoginResult(connPoint, 100);// 100账号验证失败，请重新登陆
				Log.game.error("===登陆失败：白名单验证失败！account={}", account);
				return;
			}
		}
		// 数据分析需要的参数
		String devMAC = msg.getDeviceMAC();//设备MAC地址
		long devIMEI = msg.getDeviceIMEI();//设备IMEI号
		int devType = msg.getDeviceType().getNumber();//设备类型
		
		// 连接一个随机的验证服务
		String portLogin = D.PORT_PLATFORM_LOGIN_PREFIX + new Random().nextInt(D.PORT_WORLD_STARTUP_PLATFORM_LOGIN);
		LoginServiceProxy loginServ = LoginServiceProxy.newInstance(DistrPF.NODE_ID, portLogin, DistrPF.SERV_LOGIN);
		//懒猫sdk password就是signature
		loginServ.check(loginType.getNumber(), account, password,token,timeStamp,channel);
		//七政平台sdk		
//		loginServ.check(account, channel, serverId, password);
		loginServ.listenResult(this::_result_msg_CSLogin, "connPoint", connPoint, "connStatus", connStatus,
				"account", account, "loginType", loginType, "channel", channel, "devMAC", devMAC, "devIMEI", devIMEI, "devType", devType);
		// 发送SCMsgFill
//		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
//		SCMsgFill.Builder reply = SCMsgFill.newBuilder();
//		prx.sendMsg(MsgIds.SCMsgFill, new Chunk(reply));
	}
	private void _result_msg_CSLogin(Param results, Param context) {
		// 登录验证结果（0成功；非0失败）
		boolean result = Utils.getParamValue(results, "result", false);
		String accountId = Utils.getParamValue(results, "accountId", "");
		String uid = Utils.getParamValue(results, "uid", "");
		String token = Utils.getParamValue(results, "token", "");
		
		Log.game.info("===_result_msg_CSLogin result={},accountId={},uid={},token={}", result, accountId, uid, token);
		if (!result) {// 平台服登录验证失败
			CallPoint connPoint = Utils.getParamValue(context, "connPoint", null);
			if (null == connPoint) {
				Log.game.error("===_result_msg_CSLogin connPoint is null");
				return;
			}
			
			sendSCLoginResult(connPoint, 100);// 100账号验证失败，请重新登录
			Log.game.info("===登陆失败：平台服登陆验证失败！uid={},token={}", uid, token);
			return;
		}
		
		context.put("accountId", accountId);// 验证返回的账号ID
		// 先检验玩家是否已登录
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.isLogined(accountId);
		prx.listenResult(this::_result_login2, context);
	}
	private void _result_login2(Param results, Param context) {
		CallPoint connPoint = Utils.getParamValue(context, "connPoint", null);
		ConnectionStatus connStatus = Utils.getParamValue(context, "connStatus", null);
		String account = Utils.getParamValue(context, "account", "");
		String accountId = Utils.getParamValue(context, "accountId", "");
		ELoginType loginType = Utils.getParamValue(context, "loginType", null);
		String channel = Utils.getParamValue(context, "channel", null);// 渠道
		String devMAC = Utils.getParamValue(context, "devMAC", "");//设备MAC地址
		long devIMEI = Utils.getParamValue(context, "devIMEI", 0L);//设备IMEI号
		int devType = Utils.getParamValue(context, "devType", 0);//设备类型
		
		if (null == connPoint || null == connStatus || accountId.isEmpty() || 
				null == loginType) {
			Log.game.error("===_result_login2 connPoint={},connStatus={},accountId={},loginType={}", 
					connPoint, connStatus, accountId, loginType);
			return;
		}
		
		// 环境 这里应该只有account会调用此函数
		AccountService serv = Port.getCurrent().getService(Distr.SERV_GATE);

		// 检查下之前是否有登陆中的玩家 有就踢了
		// 这里仅踢登陆中的 后面会踢在游戏中的玩家
		AccountObject accOld = getByAccount(serv, accountId);
		boolean isReconnect = false;
		CallPoint oldConn = null;
		if (accOld != null && (oldConn=accOld.connPoint)!=null) {
			if (connPoint.servId.equals(oldConn.servId)) {
				isReconnect = true;
				Log.game.info("登陆重连！account={},conn={}", accountId, connPoint);
			} else {
				ConnectionProxy connPrx = ConnectionProxy.newInstance(oldConn);
				// 提示另一个玩家 被踢
				SCHumanKick.Builder kickMsg = SCHumanKick.newBuilder();
				kickMsg.setReason(101);// 101您的账号在另一地点登陆，您被迫下线。wgz
				// 发消息
				connPrx.sendMsg(MsgIds.SCHumanKick, new Chunk(kickMsg));
				
				// 断开另一个玩家的连接
				connPrx.close();
				Log.game.info("===登陆检测到已有登陆中的玩家，断开登陆中的玩家连接！account={}", accountId);
			}
		}
		
		// FIXME HumanGlobalService.isLogined 并没有判断全服是否已满员 
		serv.isServerFull = Utils.getParamValue(results, "isServerFull", false);
		// FIXME HumanGlobalService.isLogined 并没有判断渠道是否已满员
		boolean isFull = Utils.getParamValue(results, "isFull", false);
		Boolean isServerFull = serv.serverFullMap.get(connStatus.channel);
		if(isServerFull == null || isServerFull != isFull){
			serv.serverFullMap.put(connStatus.channel, isFull);
		}
		// 黄彬添加多渠道时
		String channelStr = String.valueOf(channel);
		Boolean isChannelFull = serv.serverFullMap.get(channelStr);
		if (isChannelFull == null) {
			serv.serverFullMap.put(channelStr, false);
		}
		

		// 如果账号之前已登录 则踢出 本次登录也失败
		boolean logined = Utils.getParamValue(results, "logined", false);
		long loginedHumanId = Utils.getParamValue(results, "humanId", -1L);
		if (loginedHumanId < 0) {
			Log.game.error("===_result_login2 loginedHumanId = {}", loginedHumanId);
			return;
		}
		if (logined && !isReconnect) {
			Log.game.info("===登陆检测到已有玩家存在，踢出存在的玩家！account={}, loginedHumanId={}", accountId, loginedHumanId);
			// 踢出玩家
			HumanGlobalServiceProxy hgPrx = HumanGlobalServiceProxy.newInstance();
			hgPrx.kick(loginedHumanId, 101);// 101您的账号在另一地点登陆，您被迫下线
			
			/*
			 * //add by sjh,为什么本地登录要失败呢，暂时屏蔽掉 //消息 SCLoginResult.Builder reply =
			 * SCLoginResult.newBuilder(); reply.setResultCode(-1);//0成功，非0失败
			 * reply
			 * .setResultReason("账号已在其他终端登录，请您再次确认登录。");//102账号已在其他终端登录，请您再次确认登录
			 * //返回登录失败 ConnectionProxy connPrx =
			 * ConnectionProxy.newInstance(connPoint);
			 * connPrx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply)); return;
			 */
		}
		
		//登录角色信息检查
		loginCheck(account, accountId, connPoint, connStatus, loginType, channel, devMAC, devIMEI, devType);
	}
	
	/**
	 * 查询玩家角色信息
	 */
	public void loginCheck(String account, String accountId, CallPoint connPoint, ConnectionStatus connStatus, 
			ELoginType loginType, String channel, String devMAC, long devIMEI, int devType) {
		DB db = DB.newInstance(Human.tableName);
		db.getBy(false, Human.K.AccountId, accountId, Human.K.ServerId, connStatus.serverId);
		db.listenResult(this::_result_login_check, "connPoint", connPoint, "connStatus", connStatus, 
				"account", account, "accountId", accountId, 
				"loginType", loginType, "channel", channel, "devMAC", devMAC, "devIMEI", devIMEI, "devType", devType);
	}
	private void _result_login_check(Param results, Param context) {
		CallPoint connPoint = context.get("connPoint");
		ConnectionStatus connStatus = context.get("connStatus");
		String account = context.get("account");
		String accountId = context.get("accountId");
		ELoginType loginType = Utils.getParamValue(context, "loginType", null);
		String channel = Utils.getParamValue(context, "channel", null);// 渠道
		String devMAC = Utils.getParamValue(context, "devMAC", "");//设备MAC地址
		long devIMEI = Utils.getParamValue(context, "devIMEI", 0L);//设备IMEI号
		int devType = Utils.getParamValue(context, "devType", 0);//设备类型
		
		Log.game.info(context+"----login---debug----266");
		Port port = Port.getCurrent();
		AccountService serv = port.getService(Distr.SERV_GATE);
		SCLoginResult.Builder builder = SCLoginResult.newBuilder();
		
		//玩家信息
		Record r = results.get();
		Human human = new Human(r);
		
		if(r != null && System.currentTimeMillis() < human.getSealEndTime()){
			//封号检查
			ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
			builder.setResultCode(-1);
			prx.sendMsg(MsgIds.SCLoginResult, new Chunk(builder));
			return;
		}

		if (serv.humanOnlineFull) {
            Log.game.info("====已达最大在线人数！humanOnlineFull");
            ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);

            MsgInform.SCSysMsg.Builder msg = MsgInform.SCSysMsg.newBuilder();
            msg.setSysMsgId(20);
            prx.sendMsg(MsgIds.SCLoginResult, new Chunk(msg));
            return;
        }
		//注册玩家消息
		AccountObject obj = new AccountObject(serv, connStatus, connPoint, loginType, devMAC, devIMEI, devType);
		obj.connStatus.account = account;
		obj.connStatus.accountId = accountId;
		obj.connStatus.channel = channel;
		serv.datas.put(obj.getId(), obj);
		
		//登录排队
		serv.loginApplyAdd(obj.getId());
	}
	
	/**
	 * 发送登录请求返回结果
	 * @param connPoint 连接点
	 * @param resultCode 返回结果编号：0成功，非0失败（即sysMsg中的sn）
	 */
	public void sendSCLoginResult(CallPoint connPoint, int resultCode) {
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
		SCLoginResult.Builder reply = SCLoginResult.newBuilder();
		reply.setResultCode(resultCode);
		prx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply));
	}
	
	/**
	 * 发送绑定账号返回结果
	 * @param connPoint 连接点
	 * @param resultCode 返回结果编号：0成功，非0失败（即sysMsg中的sn）
	 */
	private void sendSCAccountBind(CallPoint connPoint, int resultCode) {
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
		SCAccountBind.Builder reply = SCAccountBind.newBuilder();
		reply.setResultCode(resultCode);
		prx.sendMsg(MsgIds.SCAccountBind, new Chunk(reply));
	}
	
	/**
	 * 绑定账号
	 */
	public void _msg_CSAccountBind(AccountObject accObj, CSAccountBind msg) {
		String accountOld = accObj.connStatus.accountId;
		if (accountOld.isEmpty()) {
			Log.game.error("===_msg_CSAccountBind accountOld.isEmpty()");
			return;
		}
		
		ELoginType loginType = msg.getLoginType();// 登录类型
		CallPoint connPoint = accObj.connPoint;
		// 不能绑定到电脑或游客登录类型
		if (loginType == ELoginType.PC) {
			sendSCAccountBind(connPoint, 11302);// 11302不允许绑定为游客
			return;
		}
		// 平台账号登录需要的参数
		String account = msg.getAccount();// 账号，即用户ID
		String password = msg.getPassword();// 密码，即访问口令（提供给服务器验证用的）
		ConnectionStatus connStatus = accObj.connStatus;
		// 连接一个随机的验证服务
		String portLogin = D.PORT_PLATFORM_LOGIN_PREFIX + new Random().nextInt(D.PORT_WORLD_STARTUP_PLATFORM_LOGIN);
		LoginServiceProxy loginServ = LoginServiceProxy.newInstance(DistrPF.NODE_ID, portLogin, DistrPF.SERV_LOGIN);
		loginServ.check(loginType.getNumber(), account, password,"",Port.getTime()+"");
		loginServ.listenResult(this::_result_msg_CSAccountBind, "connPoint", connPoint, "connStatus", connStatus,
				"loginType", loginType, "account", account, "accObj", accObj);
	}
	private void _result_msg_CSAccountBind(Param results, Param context) {
		String account = Utils.getParamValue(context, "account", "");
		if (account.isEmpty()) {
			Log.game.error("===_result_msg_CSAccountBind account.isEmpty()");
			return;
		}
		CallPoint connPoint = Utils.getParamValue(context, "connPoint", null);
		if (null == connPoint) {
			Log.game.error("===_result_msg_CSAccountBind connPoint is null");
			return;
		}
		// 登录验证结果
		int resultCode = results.getInt();// 0成功，非0错误码
		if (resultCode != 0) {// 平台服登录验证失败
			sendSCAccountBind(connPoint, 100);// 100账号验证失败，请重新登录
			Log.game.info("===登陆失败：平台服登陆验证失败！account={}", account);
			return;
		}
		// 查询数据account是否已经绑定
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.countBy(false, Human.K.AccountId, account);
		dbPrx.listenResult(this::_result_isAccountExist, context);
	}
	/**
	 * 查询是否有多个帐号
	 * @param results
	 * @param context
	 */
	private void _result_isAccountExist(Param results, Param context){
		CallPoint connPoint = Utils.getParamValue(context, "connPoint", null);
		if (null == connPoint) {
			Log.game.error("===_result_isAccountExist connPoint is null");
			return;
		}
		int count = results.get();
		if(count > 0){
			sendSCAccountBind(connPoint, 11303);// 11303该帐号已经被其他人绑定，请用其他帐号
			return;
		}
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		if (null == accObj) {
			Log.game.error("===_result_isAccountExist accObj is null");
			return;
		}
		String account = Utils.getParamValue(context, "account", "");
		if (account.isEmpty()) {
			Log.game.error("===_result_isAccountExist account.isEmpty()");
			return;
		}
		ELoginType loginType = Utils.getParamValue(context, "loginType", null);
		if (null == loginType) {
			Log.game.error("===_result_isAccountExist loginType is null");
			return;
		}
		// 玩家账号绑定游客，并更改登录类型
		accObj.loginType = loginType;
		accObj.accountBind = account;
		sendSCAccountBind(connPoint, 0);// 0成功 返回成功
	}

	/**
	 * 查询角色列表的请求
	 */
	public void _msg_CSQueryCharacters(AccountObject accObj, String account, int serverId) {
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.findBy(false, Human.K.AccountId, account, Human.K.ServerId, serverId);
		dbPrx.listenResult(this::_result_msg_CSQueryCharacters, "accObj", accObj);
	}
	private void _result_msg_CSQueryCharacters(Param results, Param context) {
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		if (null == accObj) {
			Log.game.error("===_result_msg_CSQueryCharacters accObj is null");
			return;
		}
		MsgLogin.SCQueryCharactersResult.Builder builder = MsgLogin.SCQueryCharactersResult.newBuilder();

		List<Record> records = results.get();
		if (null == records) {
			Log.game.error("===_result_msg_CSQueryCharacters records is null");
			return;
		}
		for (Record r : records) {
			// 玩家信息
			Human human = new Human(r);
			// 删除过的角色跳过
			if (1 == human.getDeleteRole()) {
				continue;
			}
			Define.DCharacter.Builder info = Define.DCharacter.newBuilder();
			info.setId(human.getId());
			info.setLevel(human.getLevel());
			info.setName(human.getName());
			info.setProfession(human.getProfession());
			info.setSex(human.getSex());
			info.setFashionShow(human.isFashionShow());// 是否显示时装
			info.setFashionWeaponSn(human.getFashionWeaponSn());// 时装武器
			info.setFashionClothesSn(human.getFashionSn());// 时装衣服
			builder.addCharacters(info);
		}

		ConnectionProxy prx = ConnectionProxy.newInstance(accObj.connPoint);
		prx.sendMsg(MsgIds.SCQueryCharactersResult, new Chunk(builder));
	}

	/**
	 * 玩家创角 不发名字 由服务端随机获取名字
	 * @param accObj
	 * @param profession
	 * @param sex
	 */
	public void _msg_CSCharacterCreate(AccountObject accObj, int profession, int sex) {
		//boolean isFemale = (sex == 1 ? true : false);// 0男，1女
		boolean isFemale = (RandomUtils.nextInt(2) == 1 ? true : false);// 随机性别
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.getRandomName(isFemale);
		prx.listenResult(this::_result_msg_CSCharacterCreate, "accObj", accObj, "profession", profession, "sex", sex);
	}
	/**
	 * 获取随机名字后执行创角
	 * @param results
	 * @param context
	 */
	private void _result_msg_CSCharacterCreate(Param results, Param context) {
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		String name = Utils.getParamValue(results, "randomName", "");
		int profession = Utils.getParamValue(context, "profession", -1);
		int sex = Utils.getParamValue(context, "sex", -1);
		if (null == accObj || name.isEmpty() || profession < 0 || sex < 0) {
			Log.game.error("===_result_randomNameRepeat accObj={},name={},profession={},sex={}", 
					accObj, name, profession, sex);
			return;
		}
		// 查询验证是否能创建角色
		HumanApplyServiceProxy prx = HumanApplyServiceProxy.newInstance();
		prx.applyCreateHuman(accObj.connStatus.serverId, accObj.connStatus.accountId, name);// name.toLowerCase()
		prx.listenResult(this::_result_applyCreateHuman, "accObj", accObj, "name", name, "profession", profession,"sex", sex);
	}
	/**
	 * 查询验证是否能创建角色返回
	 * @param results
	 * @param context
	 */
	private void _result_applyCreateHuman(Param results, Param context) {
		// 返回值
		boolean succeed = Utils.getParamValue(results, "result", false);
		int reason = Utils.getParamValue(results, "reason", 0);

		// 上下文
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		String name = Utils.getParamValue(context, "name", "");// 名字
		int profession = Utils.getParamValue(context, "profession", -1);

		if (null == accObj || name.isEmpty() || profession < 0)  {
			Log.game.error("===_result_applyCreateHuman accObj={},name={},profession={},sex={}", 
					accObj, name, profession );
			return;
		}
		// 验证是否可以创建角色
		if (!succeed) {
			sendMsgSCCharacterCreateResult(accObj, 0, -1, reason);// 返回创角失败
			return;
		}

		// 验证通过 正式创建角色
		AccountService serv = Port.getCurrent().getService(Distr.SERV_GATE);
		long humanId = serv.applyHumanId();//Port.applyHumanId();
		if (humanId == 0) {
			Log.human.error("===创建角色失败，无法分配角色ID：humanId=0");
			reason = 100501;// 100501创建角色失败，本服已到达最大创建角色数，请选择其他区服！
			sendMsgSCCharacterCreateResult(accObj, 0, -1, reason);// 返回创角失败
			return;
		}
		Human human = HumanManager.inst().create(humanId, accObj, name, profession);
		human.update(true);
				
		sendMsgSCCharacterCreateResult(accObj, humanId, 0, 0);// 0成功，非0失败
		
		// 名字库加入新名字
		NameServiceProxy prxName = NameServiceProxy.newInstance();
		prxName.addNewName(name);
	}

	/**
	 * 创角时根据性别和职业获取随机名字
	 * @param accObj
	 * @param profession
	 * @param sex
	 */
	public void _msg_CSCharacterCreateName(AccountObject accObj, int profession, int sex) {
		//boolean isFemale = (sex == 1 ? true : false);// 0男，1女
		boolean isFemale = sex == 0?true:false;
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.getRandomName(isFemale);
		prx.listenResult(this::_result_msg_CSCharacterCreateName, "accObj", accObj, "profession", profession, "sex",
				sex);
	}

	/**
	 * 返回创角时获取名字结果
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSCharacterCreateName(Param results, Param context) {
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		String name = Utils.getParamValue(results, "randomName", "");
		int profession = Utils.getParamValue(context, "profession", -1);
		int sex = Utils.getParamValue(context, "sex", -1);
		if (null == accObj || name.isEmpty() || profession < 0 || sex < 0) {
			Log.game.error("===_result_randomNameRepeat accObj={},name={},profession={},sex={}", 
					accObj, name, profession, sex);
			return;
		}
		sendMsgSCCharacterCreateName(accObj, name);
	}
	/**
	 * 返回创角时获取的名字
	 * @param accObj
	 * @param name
	 */
	public void sendMsgSCCharacterCreateName(AccountObject accObj, String name) {
		SCCharacterCreateName.Builder msgSender = SCCharacterCreateName.newBuilder();
		msgSender.setName(name);
		// 返回消息
		ConnectionProxy prx = ConnectionProxy.newInstance(accObj.connPoint);
		prx.sendMsg(MsgIds.SCCharacterCreateName, new Chunk(msgSender));
	}

	/**
	 * 处理创建角色
	 * @param accObj
	 */
	public void _msg_CSCharacterCreate(AccountObject accObj, String name, int profession) {
		if (name.isEmpty() || profession < 0) {// 角色名空或职业不对或性别不对
			// 146创建失败！请检查角色名，职业及性别是否正确！
			sendMsgSCCharacterCreateResult(accObj, 0, -1, 146);
			return;
		}
		
		int length = ParamManager.maxHumanNameLength;
		if (name.length() > length || name.length() < 2) {
			// 发送文字提示消息 名字长度不能超过{}8 或小于2
//			humanObj.sendSysMsg(51, "B", length, "B1", 2);
			sendMsgSCCharacterCreateResult(accObj, 0, -1, 146);
			return;
		}

		// 检查是否存在非法的特殊字符
		if (!AssetsTxtFix.checkContent(name, length)) {
//			humanObj.sendSysMsg(22);// 输入的文本中存在非法字符！请重新输入！
			sendMsgSCCharacterCreateResult(accObj, 0, -1, 146);
			return;
		}
				
		// 检查名字是否有屏蔽字，如果有屏蔽字返回
		String fix = SensitiveWordFilter.getInstance().getSensitiveWord(name.toLowerCase());
		if (fix != null) {
			// 返回消息
//			SCChangeNameResult.Builder msg = SCChangeNameResult.newBuilder();
//			msg.setResult(false);
//			msg.setShield(fix);
//			humanObj.sendMsg(msg);
			sendMsgSCCharacterCreateResult(accObj, 0, -1, 146);
			return;
		}
		
		
		
		// 判断名字是否重复
		NameServiceProxy prxName = NameServiceProxy.newInstance();
		prxName.isRepeatName(name);
		prxName.listenResult(this::_result_CSCharacterCreate, "accObj", accObj, "name", name, "profession", profession);
	}
	/**
	 * 判断名字是否重复返回
	 * @param results
	 * @param context
	 */
	private void _result_CSCharacterCreate(Param results, Param context) {
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		String name = Utils.getParamValue(context, "name", "");
		int profession = Utils.getParamValue(context, "profession", -1);
		boolean repeat = Utils.getParamValue(results, "repeat", true);
		if (null == accObj || name.isEmpty() || profession < 0) {
			Log.game.error("===_result_CSCharacterCreate accObj={},name={},profession={},sex={}", 
					accObj, name, profession);
			return;
		}
		// 查询结果
		if (repeat) {
			sendMsgSCCharacterCreateResult(accObj, 0, -1, 2);// 2昵称已存在
		} else {
			// 查询验证是否能创建角色
			HumanApplyServiceProxy prx = HumanApplyServiceProxy.newInstance();
			prx.applyCreateHuman(accObj.connStatus.serverId, accObj.connStatus.accountId, name);// name.toLowerCase()
			prx.listenResult(this::_result_applyCreateHuman, "accObj", accObj, "name", name, "profession", profession);
		}
	}

	/**
	 * 返回创角结果
	 * @param humanId 分配的玩家ID
	 * @param resultCode 执行结果：0成功，非0失败
	 * @param resultReason 执行结果原因：sysmsg中的sn
	 */
	public void sendMsgSCCharacterCreateResult(AccountObject accObj, long humanId, int resultCode, int resultReason) {
		SCCharacterCreateResult.Builder msgSender = SCCharacterCreateResult.newBuilder();
		msgSender.setResultCode(resultCode);// 执行结果：0成功，非0失败
		if (resultReason > 0)
			msgSender.setResultReason(resultReason);// 执行结果原因：sysmsg中的sn
		if (humanId > 0)
			msgSender.setHumanId(humanId);

		// 返回消息
		ConnectionProxy prx = ConnectionProxy.newInstance(accObj.connPoint);
		prx.sendMsg(MsgIds.SCCharacterCreateResult, new Chunk(msgSender));
	}

	/**
	 * 处理删除角色
	 * @param accObj
	 */
	public void _msg_CSCharacterDelete(AccountObject accObj, long humanId) {
		// 旧角色存在则删除
		HumanApplyServiceProxy prx = HumanApplyServiceProxy.newInstance();
		prx.applyDeleteHuman(C.GAME_SERVER_ID, accObj.connStatus.accountId, humanId);
		prx.listenResult(this::_result_applyDeleteHuman, "accObj", accObj, "humanId", humanId);
	}

	/**
	 * 角色删除请求结果
	 * @param results
	 * @param context
	 */
	public void _result_applyDeleteHuman(Param results, Param context) {
		// 返回值
		boolean result = Utils.getParamValue(results, "result", false);
		// String reason = results.get("reason");

		// 上下文
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		long humanId = Utils.getParamValue(context, "humanId", -1L);
		if (null == accObj || humanId <= 0) {
			Log.game.error("===_result_applyDeleteHuman accObj={},humanId={}", accObj, humanId);
			return;
		}

		// 创建返回消息
		SCCharacterDeleteResult.Builder msgSender = SCCharacterDeleteResult.newBuilder();
		msgSender.setResultCode(result ? 0 : -1);
		// msgSender.setResultReason(reason);

		// 返回消息
		ConnectionProxy prx = ConnectionProxy.newInstance(accObj.connPoint);
		prx.sendMsg(MsgIds.SCCharacterDeleteResult, new Chunk(msgSender));

		if (result) {// 删除角色成功后删除相关信息
//			RankManager.inst().deleteHumanRankInfo(humanId);// 删除与角色相关的排行信息
			// InstanceManager.inst().deleteAllInstanceInfo(humanId);//
			// 删除与角色相关的副本信息
			// InstanceManager.inst().deleteAllInstanceInfo(humanId);//
			// 与角色相关的副本信息标志为删除
		}
	}

	/**
	 * 角色登陆
	 * @param accObj
	 * @param humanId
	 * @param reconnect false=正常登陆 true=断线重连
	 */
	public void _msg_CSCharacterLogin(AccountObject accObj, long humanId, boolean reconnect) {
		accObj.humanId = humanId;
		// 获取玩家的地图ID
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.getBy(false, Human.K.id, humanId, Human.K.AccountId, accObj.connStatus.accountId);
		dbPrx.listenResult(this::_result_msg_CSCharacterLogin, "accObj", accObj, "reconnect", reconnect);
	}
	private void _result_msg_CSCharacterLogin(Param results, Param context) {
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		if (null == accObj) {
			Log.game.error("===_result_msg_CSCharacterLogin accObj is null");
			return;
		}
		boolean reconnect = Utils.getParamValue(context, "reconnect", false);
		Record r = results.get();
		if (null == r) {
			Log.human.error("===_result_msg_CSCharacterLogin record is null, id={}, humanId={}, reconnect={}",
					accObj.id, accObj.humanId, Utils.booleanToStr(reconnect));
			return;
		}
		
		Human human = new Human(r);
		if (!accObj.accountBind.isEmpty()) {// 玩家账号绑定游客
			human.setAccountId(accObj.accountBind);
		}
		
		if(Port.getTime() < human.getSealEndTime()){//该玩家被封号了
			sendSCCharacterLoginResult(accObj,163);//发送登录返回结果 你被封号了
			return;
		}
		
		// Log.temp.info("人物已经登录：连接ID={}, HumanID={}", accObj.getId(),
		// accObj.humanId);

		// //获取并处理地图历史路径
		// 数据格式：[[100010000000022097,101,19.6,30.9,"rep"],[1,1,65.5,7.3,"common"]]
		String stageHistory = human.getStageHistory();
		List<List<?>> stageIdsHistory = new ArrayList<>();
		JSONArray ja = Utils.toJSONArray(stageHistory);
		if(ja.isEmpty()){
			Log.game.error("===_result_msg_CSCharacterLogin human.getStageHistory() is null");
			return;
		}
		for (Object obj : ja) {
			JSONArray jaTemp = Utils.toJSONArray(obj.toString());
			if(jaTemp.isEmpty()){
				Log.game.error("===_result_msg_CSCharacterLogin obj is null");
				return;
			}
			List<?> tmp = Utils.ofList(jaTemp.getLongValue(0), jaTemp.getIntValue(1), 
					jaTemp.getDoubleValue(2), jaTemp.getDoubleValue(3));
			stageIdsHistory.add(tmp);
		}

		int firstStory = human.getFirstStory();// 创角后的第一个新手剧情
		// 根据地图历史路径获取可登陆地图
		StageGlobalServiceProxy prx1 = StageGlobalServiceProxy.newInstance();
		prx1.login(accObj.humanId, accObj.connPoint, accObj.connStatus, stageIdsHistory, firstStory);
		prx1.listenResult(this::_result_characterLogin2, "accObj", accObj, "reconnect", reconnect);
	}

	public void _result_characterLogin2(boolean timeout, Param results, Param context) {
		AccountObject accObj = Utils.getParamValue(context, "accObj", null);
		if (null == accObj) {
			Log.game.error("===_result_characterLogin2 accObj is null");
			return;
		}
		boolean reconnect = Utils.getParamValue(context, "reconnect", false);
		if (timeout) {// 30秒超时
			Log.human.error("===登陆失败：超时了，返回登陆失败！");
			sendSCCharacterLoginResult(accObj,-1);//发送登录返回结果
			return;
		}

		// 应答客户端
		// if(!reconnect) {
		sendSCCharacterLoginResult(accObj,0);//发送登录返回结果
		// }

		// 更新连接状态
		String node = Utils.getParamValue(results, "nodeId", "");
		String port = Utils.getParamValue(results, "portId", "");
		if (node.isEmpty() || port.isEmpty()) {
			Log.game.error("===_result_characterLogin2 node={}, port ={}", node, port);
			return;
		}

		ConnectionStatus status = accObj.connStatus;
		status.humanId = accObj.humanId;
		status.stageNodeId = node;
		status.stagePortId = port;
		status.status = ConnectionStatus.STATUS_PLAYING;

		ConnectionProxy prxConn = ConnectionProxy.newInstance(accObj.connPoint);// 连接代理
		prxConn.updateStatus(status);
		accObj.serv.checkGateNum(status.status);
		// 清理当前的缓存数据
		accObj.serv.datas.remove(accObj.getId());
	}

	/**
	 * 发送登录返回结果
	 * @param accObj
	 * @param result 0成功，非0失败
	 */
	public void sendSCCharacterLoginResult(AccountObject accObj, int result){
		ConnectionProxy prxConn = ConnectionProxy.newInstance(accObj.connPoint);// 连接代理
		MsgLogin.SCCharacterLoginResult.Builder msgResult = MsgLogin.SCCharacterLoginResult.newBuilder();
		msgResult.setResultCode(result);// 0成功，非0失败
		msgResult.setServerTime(Port.getTime());// 服务器时间
		msgResult.setServerStartDate(Config.SERVER_STARTDATE);// 开服时间
		prxConn.sendMsg(MsgIds.SCCharacterLoginResult, new Chunk(msgResult));
	}
	
	/**
	 * 在登陆信息中 查找同account的数据
	 * @param serv
	 * @param account
	 * @return
	 */
	public AccountObject getByAccount(AccountService serv, String account) {
		// 遍历寻找登陆中的玩家信息 看有没有同account的
		for (AccountObject o : serv.datas.values()) {
			if (account.equals(o.connStatus.accountId)) {
				return o;
			}
		}

		return null;
	}

	/**
	 * 断线重连
	 * @param account
	 * @param connPoint
	 */
	public void _msg_CSAccountReconnect(Long connId, CallPoint connPoint, String account) {
		// 从humanGlobal 中获得人物的连接
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.isLogined(account);
		prx.listenResult(this::_result_msg_CSAccountReconnect, "connPoint", connPoint);

		// //查询要恢复的数据
		// DBServiceProxy db = DBServiceProxy.newInstance();
		// db.getBy(false, Human.tableName, Human.K.sessionKey,
		// msg.getSessionKey());
		// db.listenResult(this,
		// AccountMsgHandlerCallback._result_onCSAccountReconnect, "msgParam",
		// param);
	}
	private void _result_msg_CSAccountReconnect(Param results, Param context) {
		CallPoint connPointNew = Utils.getParamValue(context, "connPoint", null);
		HumanGlobalInfo hgInfo = Utils.getParamValue(results, "humanInfo", null);
		if (null == connPointNew || null == hgInfo) {
			SCAccountReconnectResult.Builder sendMsg = SCAccountReconnectResult.newBuilder();
			// 不能重新连接 直接走断线
			sendMsg.setResultCode(19);// 0成功，非0失败
			// 发送恢复状态消息
			ConnectionProxy prx = ConnectionProxy.newInstance(connPointNew);
			prx.sendMsg(MsgIds.SCAccountReconnectResult, new Chunk(sendMsg));
			
			Log.game.info("===_result_msg_CSAccountReconnect CallPoint={},HumanGlobalInfo={}",
					connPointNew, hgInfo);
			return;
		}
		boolean logined = Utils.getParamValue(results, "logined", false);
		if (!logined) {
			SCAccountReconnectResult.Builder sendMsg = SCAccountReconnectResult.newBuilder();
			// 不能重新连接 直接走断线
			sendMsg.setResultCode(19);// 0成功，非0失败
			// 发送恢复状态消息
			ConnectionProxy prx = ConnectionProxy.newInstance(connPointNew);
			prx.sendMsg(MsgIds.SCAccountReconnectResult, new Chunk(sendMsg));
			return;
		}

		// 打开新的发送新的人物连接 并更新conn 的状态
		ConnectionProxy connPrxNew = ConnectionProxy.newInstance(connPointNew);
		ConnectionStatus status = new ConnectionStatus();
		status.account = hgInfo.account;
		status.accountId = hgInfo.accountId;
		status.humanId = hgInfo.id;
		status.stageNodeId = hgInfo.nodeId;
		status.stagePortId = hgInfo.portId;
		status.channel = hgInfo.channel;
		status.serverId = hgInfo.serverId;
		status.status = ConnectionStatus.STATUS_PLAYING;
		connPrxNew.updateStatus(status);
		
		AccountService serv = Port.getCurrent().getService(Distr.SERV_GATE);
		serv.checkGateNum(status.status);
		
		// copy 旧的连接 缓冲过来
		connPrxNew.initMsgBuf(hgInfo.connPoint);

		// 关闭原来的人物连接
		ConnectionProxy connPrxOld = ConnectionProxy.newInstance(hgInfo.connPoint);
		connPrxOld.close();
		Log.game.info("===关闭原来的人物连接：{},connPrxOld={},new={}", Port.getTime(), hgInfo.connPoint.toString(),
				connPointNew.toString());

		// 更新humanObject的conn
		HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hgInfo.nodeId, hgInfo.portId,
				hgInfo.id);
		prxHumanObj.changeConnPoint(connPointNew);
		// Log.temp.info("connPointNew {} {} ", connPointNew.servId,
		// Port.getTime());
		// 更新humanGlobal对应的HumanObject 的conn
		HumanGlobalServiceProxy prxHumanGol = HumanGlobalServiceProxy.newInstance();
		prxHumanGol.changeConnPoint(hgInfo.id, connPointNew);

		// 发送缓冲数据
		connPrxNew.sendMsgBuf();
		// 返回恢复状态
		SCAccountReconnectResult.Builder sendMsg = SCAccountReconnectResult.newBuilder();
		sendMsg.setResultCode(0);// 0成功，非0失败
		// 发送恢复状态消息
		connPrxNew.sendMsg(MsgIds.SCAccountReconnectResult, new Chunk(sendMsg));

	}

	/**
	 * 断线重连 返回值处理
	 * @param results
	 * @param context
	 */
	// public void _result_onCSAccountReconnect(Param results, Param context) {
	// MsgParamAccount msgParam = Utils.getParamValue(context, "msgParam",
	// null);
	// if(msgParam==null){
	// Log.game.error("===_result_onCSAccountReconnect msgParam=null");
	// return;
	// }
	//
	// //参数
	// Long connId = msgParam.getConnId();
	// AccountService serv = msgParam.getService();
	// ConnectionStatus status = msgParam.getConnStatus();
	// CallPoint connPoint = msgParam.getConnPoint();
	//
	// //返回结果
	// Record r = results.get();
	// if(r==null){
	// Log.game.error("===_result_onCSAccountReconnect Record=null");
	// return;
	// }
	//
	// //可以恢复？
	// boolean recover = true;
	//
	// //无法通过SessionKey恢复
	// if(null == r) recover = false;
	//
	// //恢复玩家数据
	// Human human = new Human(r);
	//
	// //在尝试看下有没有登陆中的同account数据
	// if(recover) {
	// //检查下如果此account正在登陆中 那么就断线重连失败
	// //如果玩家已登陆到游戏中，那么sessionKey会改变，所以不用考虑已在游戏中的玩家
	// AccountObject accOld = AccountManager.inst().getByAccount(serv,
	// human.getAccount());
	//
	// if(accOld != null) recover = false;
	// }
	//
	// //返回恢复状态
	// SCAccountReconnectResult.Builder sendMsg =
	// SCAccountReconnectResult.newBuilder();
	//
	// //如果找不到恢复数据 则恢复失败 否则就当做成功
	// if(recover) sendMsg.setResultCode(0);
	// else sendMsg.setResultCode(-1);
	//
	// //发送恢复状态消息
	// ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
	// prx.sendMsg(MsgIds.SCAccountReconnectResult, new Chunk(sendMsg));
	//
	// //如果不可恢复 那么流程到此结束
	// if(!recover) {
	// return;
	// }
	//
	// //取出会用到的恢复数据
	// long humanId = human.getId();
	// String account = human.getAccount();
	//
	// //重建账户数据
	// AccountObject obj = new AccountObject(connId, serv, status, connPoint);
	// obj.humanId = humanId;
	// obj.status.humanId = humanId;
	// obj.status.account = account;
	// obj.status.status = ConnectionStatus.STATUS_GATE;
	//
	// serv.datas.put(obj.getId(), obj);
	//
	// //玩家登陆
	// AccountManager.inst().characterLogin(obj, obj.humanId, true);
	// }

}