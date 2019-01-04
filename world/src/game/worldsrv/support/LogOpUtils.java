package game.worldsrv.support;

import com.pwrd.op.LogOp;
import com.pwrd.op.LogOpChannel;

import core.Port;
import core.support.Utils;
import game.msg.Define.EDeviceType;
import game.msg.MsgCommon.SCLogOp;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;

public class LogOpUtils {

	private static String trim(String str) {
		if (str != null) {
			return str.replace("|", "");
		}
		return null;
	}
	
	/**
	 * 发送给客户端日志信息
	 */
	private static void sendLogOpMsg(HumanObject humanObj, String logStr) {
		SCLogOp.Builder msg = SCLogOp.newBuilder();
		msg.setLog(logStr);
		humanObj.sendMsg(msg);
	}
	/**
	 * 发送消费日志
	 * @param humanObj 
	 * @param logType 日志类型
	 * @param resType 资源/道具sn 
	 * @param costNum 消耗数量
	 */
	public static void sendCostLog(HumanObject humanObj, LogSysModType logType, int resType, long costNum) {
		Human human = humanObj.getHuman();
		// 服务端日志
		LogCost(human, logType, resType, costNum);
		
		// 客户端日志
		String logStr = "COST" + "|" + 
						human.getId() + "|" +
						Port.getTime() +"|" +
						trim(Utils.formatTime(Port.getTime(), String.valueOf(Port.getTime()))) + "|" +
						trim(LogSysModType.getName(logType)) + "|" +
						trim(LogSysModType.getResName(resType)) + "|" +
						costNum + "|" +
						human.getAccountId() + "|" +
						trim(human.getName()) + "|" +
						human.getLevel() + "|" +
						human.getServerId();
		sendLogOpMsg(humanObj, logStr);
	}
	
	/**
	 * 发送玩家在线信息 （每隔5min发一次）
	 * @param humanObj
	 * @return
	 */
	public static void sendOnlineLog(HumanObject humanObj){
		String logStr = "ONLINE" + "|" +
						humanObj.getHumanId() + "|" +
						Port.getTime() + "|" +
						trim(Utils.formatTime(Port.getTime(), "yyyy-MM-dd")) + "|" +
						humanObj.getHuman().getServerId();
		sendLogOpMsg(humanObj, logStr);
	}

	/**
	 * 发送主角升级日志
	 * @param humanObj
	 * @return
	 */
	public static void sendUpgradeLog(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 服务端日志
		LogUpgrade(human);
		
		// 客户端日志
		String logStr = "UPGRADE" + "|" +
						humanObj.getHumanId() + "|" +
						trim(human.getName()) + "|" +
						human.getLevel() + "|" +
						Port.getTime() + "|" +
						trim(Utils.formatTime(Port.getTime(), "yyyy-MM-dd")) + "|" +
						human.getAccountId() + "|" +
						human.getServerId();
		sendLogOpMsg(humanObj, logStr);
	}
	
	/**
	 * 每日任务日志
	 * @param humanObj
	 * @param questSn 任务id
	 * @param state 状态类型 0-领取，1-完成，2-放弃
	 */
	public static void sendQuestLog(HumanObject humanObj, int questSn, int state) {
		Human human = humanObj.getHuman();
		// 服务端日志
		LogQuest(human, questSn, state);
		
		// 客户端日志
		String logStr = "QUEST" + "|" +
						human.getId() + "|" +
						human.getAccountId() + "|" +
						trim(human.getName()) + "|" +
						human.getLevel() + "|" +
						questSn + "|" +
						state + "|" +
						Port.getTime() + "|" +
						trim(Utils.formatTime(Port.getTime(), "yyyy-MM-dd")) + "|" +
						human.getServerId();
		sendLogOpMsg(humanObj, logStr);
	}
	
	/**
	 * 用户登录日志
	 * @return
	 */
	public static void sendLoginLog(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 服务端日志
		LogOpUtils.LogLogin(humanObj);
		
		// 客户端日志
		String logStr = "LOGIN" + "|" +
						humanObj.getHumanId() + "|" +
						trim(String.valueOf(Port.getTime())) + "|" +
						trim(Utils.formatTime(Port.getTime(), "yyyy-MM-dd")) + "|" +
						human.getAccountId() + "|" +
						trim(human.getName()) + "|" +
						human.getLevel() + "|" +
						human.getServerId() + "|" +
						human.getChannel();
		sendLogOpMsg(humanObj, logStr);
	}

	
	//*******************************************************
	// 服务端OP日志记录
	//*******************************************************
	/**
	 * 用户消费资源日志：玩家，操作类型, 资源/道具sn ， 数量
	 * @param human
	 * @param logType 操作类型
	 * @param resSn 资源/道具sn
	 * @param costNum 数量
	 */
	private static void LogCost(Human human ,LogSysModType logType ,int resSn ,long costNum ){
		//添加消费日志:用户ID,消费时间戳,日期串,消耗来源,"道具名",花费数量,账号ID,角色名,等级
		LogOp.log(LogOpChannel.COST,
				human.getId(),
				Port.getTime(),
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd"),
				trim(LogSysModType.getName(logType)),
				trim(LogSysModType.getResName(resSn)),
				costNum,
				human.getAccountId(),
				trim(human.getName()),
				human.getLevel(),
				human.getServerId()
				);
	}
	
	/**
	 * 获得日志
	 * @param human
	 * @param logType 获得来源
	 * @param resSn 获得资源类型 资源/道具sn
	 * @param gainNum 获得数量
	 */
	public static void LogGain(Human human ,LogSysModType logType ,int resSn ,long gainNum){
		// 自动恢复体力不记录
		if (logType == LogSysModType.ActValueRecovery) {
			return;
		}
		//用户ID，时间戳，日期串，物品标识，物品名字（“金币”游戏货币，“钻石”充值货币），物品个数，来源，账号ID，角色名，等级
		LogOp.log(LogOpChannel.GAIN,
				human.getId(),
				Port.getTime(),
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd"),
				resSn,
				trim(LogSysModType.getResName(resSn)),
				gainNum,
				trim(LogSysModType.getName(logType)),
				human.getAccountId(),
				trim(human.getName()),
				human.getLevel(),
				human.getServerId()
				);
	 }
	 
	/**
	 * 用户登录日志
	 * @param humanObj
	 */
	private static void LogLogin(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 添加登陆日志:用户ID, 登陆时间戳, IP地址, 日期串, 账号ID, 角色名, 等级
		LogOp.log(LogOpChannel.LOGIN, 
				human.getId(), 
				Port.getTime(), 
				humanObj.connStatus.clientIP,	// ip地址
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd"),
				human.getAccountId(),
				trim(human.getName()),
				human.getLevel(),
				human.getServerId()
				); 
	}

	/**
	 * 用户下线日志
	 * @param humanObj
	 */
	public static void LogLogout(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 用户ID，登陆时间戳，账号ID，角色名，等级，日期串，在线时长（秒），停留任务，场景地图，停留坐标，状态1，状态2，状态3
		LogOp.log(LogOpChannel.LOGOUT, 
				human.getId(), 
				Port.getTime(), 
				human.getAccountId(), 
				trim(human.getName()), 
				human.getLevel(), 
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd"), 
				human.getTimeSecOnline(), 
				"", 
				humanObj.name, 
				humanObj.posNow, 
				"", 
				"", 
				"离线",
				human.getServerId()
				);
	}
	
	/**
	 * 服务器在线信息
	 * @param registerCount 注册人数
	 * @param onlineCount 在线人数
	 */
	public static void LogOnline(int registerCount, int onlineCount) {
		// 注册人数，在线人数，统计时的时间戳，日期串
		LogOp.log(LogOpChannel.ONLINE, 
				registerCount, 
				onlineCount, 
				Port.getTime(), 
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd")
				);
	} 
	
	
	/**
	 * 用户注册日志
	 * @param human
	 */	
	public static void LogRegister(Human human, String clientIP) {
		int logChannel = 1;
//		if (human.getDevType() == EDeviceType.Ios_VALUE) {
//			logChannel = 1;
//		} else 
		if (human.getDevType() == EDeviceType.Android_VALUE) {
			logChannel = 2;
		}
		// 用户ID，角色名，时间戳，IP地址，日期串，赠送的钻石，账号id，渠道id(要和后台的渠道id对应起来)，新手卡号，玩家ID(可选，玩家可以在游戏中看到，方便反馈给GM查询)
		LogOp.log(LogOpChannel.REGISTER,
				human.getId(),
				trim(human.getName()),
				Port.getTime(),
				clientIP,
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd"),
				human.getGold(),
				human.getAccountId(),
				human.getChannel(),
				"",
				human.getHumanDigit(),
				human.getServerId()
				);
	}
	
	/**
	 * 用户充值日志
	 * @param human
	 * @param costRMB 充值RMB
	 * @param gold 获得钻石
	 */
	public static void LogRecharge(Human human, int costRMB, long gold) {
		// 用户ID，时间戳，日期串，充值RMB，钻石数量，帐号ID，角色名，等级，vip等级
		LogOp.log(LogOpChannel.RECHARGE, 
				human.getId(), 
				Port.getTime(), 
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd"), 
				costRMB,
				gold, 
				human.getAccountId(),
				trim(human.getName()),
				human.getLevel(),
				human.getVipLevel(),
				human.getServerId(),
				""
				);
	}
	
	/**
	 * 用户升级日志 
	 */
	private static void LogUpgrade(Human human) {
		// 用户ID，角色名，等级，时间戳，日期串，账号id
		LogOp.log(LogOpChannel.UPGRADE, 
				human.getId(), 
				trim(human.getName()), 
				human.getLevel(), 
				Port.getTime(),
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd"), 
				human.getAccountId(),
				human.getServerId()
				);
	}
	
	/**
	 * 任务成就
	 */
	private static void LogQuest(Human human, int questSn, int state) {
		// 用户ID，角色名，帐号ID，等级，任务id，完成类型，0-领取，1-完成，2-放弃，时间戳，日期串
		LogOp.log(LogOpChannel.QUEST, 
				human.getId(),
				trim(human.getName()),
				human.getAccountId(),
				human.getLevel(),
				questSn,
				state,
				Port.getTime(),
				Utils.formatTime(Port.getTime(), "yyyy-MM-dd"),
				human.getServerId()				
				);
	}

	 
}
