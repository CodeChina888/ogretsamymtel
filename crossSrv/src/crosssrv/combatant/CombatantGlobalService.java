package crosssrv.combatant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.Message;

import core.CallPoint;
import core.Port;
import core.Service;
import core.connsrv.ConnectionProxy;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Distr;
import core.support.TickTimer;
import core.support.Time;
import crosssrv.character.CombatantObjectServiceProxy;
import crosssrv.seam.CrossPort;
import crosssrv.seam.token.TokenService;
import game.msg.MsgCross.SCCombatantKick;
import game.worldsrv.support.D;

@DistrClass(importClass = { CombatantGlobalInfo.class, Map.class, List.class, Message.class, Set.class })
public class CombatantGlobalService extends Service {
	private TickTimer msgPulseTimer = new TickTimer(500); // 控制广播发送频率
	private TickTimer scheduleTimer = new TickTimer(10000); // 调度的处理
	private Map<Integer, Long> shceduleMap = new LinkedHashMap<Integer, Long>();

	private TickTimer logTimer = new TickTimer(300000); // 写日志时间
	private int COUNT100Per10SEC = 500;

	// 玩家状态信息
	private Map<Long, CombatantGlobalInfo> datas = new HashMap<>();

	// 向token同步服务器人数满员状态
	public static int TOKENTIME = 10; // 检查超时时间
	private TickTimer tokenOnlineFullTimer = new TickTimer(TOKENTIME * Time.SEC);

	TokenService tokenServ;
	
	@Override
	public Object getId() {
		return D.SERV_COMBATANT_GLOBAL;
	}

	/**
	 * 每个service预留空方法
	 * 
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {

	}

	@Override
	public void pulseOverride() {

		long now = Port.getTime();

		// 延迟处理统一发送
		if (msgPulseTimer.isPeriod(now)) {
			// sendInfomAll();
		}

		if (scheduleTimer.isPeriod(now)) {
			onSchdule();
		}

		// // 写日志
		// if (logTimer.isPeriod(now)) {
		// writeOnlineLog();
		// }

		// 向token同步服务器人数满员状态
		pulseTokenOnlineFull(now);

	}

	/**
	 * 向token同步服务器人数满员状态
	 * 
	 * @param now
	 */
	private void pulseTokenOnlineFull(long now) {
		if (!tokenOnlineFullTimer.isPeriod(now)) {
			return;
		}

		// 当前人数
		int num = datas.size();
		// Log.temp.info("pulseAccountOnlineFull:num============{}",num);
		// 同步到token服务器
		tokenServ.setCombatantOnlineFull(num);
	}

	public CombatantGlobalService(CrossPort port) {
		super(port);
		init(port);
	}

	protected void init(Port port) {
		tokenServ = (TokenService) port.getService(Distr.SERV_GATE);
	}

	/**
	 * 获取玩家的全局信息
	 * 
	 * @param humanId
	 */
	public CombatantGlobalInfo getCombatantGlobalInfo(long humanId) {
		return datas.get(humanId);
	}

	/**
	 * 获取玩家的全局信息
	 * 
	 * @param humanIds
	 */
	@DistrMethod
	public void getInfo(long humanId) {
		port.returns(datas.get(humanId));
	}

	/**
	 * 获取多个玩家的全局信息
	 * 
	 * @param humanIds
	 */
	@DistrMethod
	public void getInfos(List<Long> humanIds) {
		List<CombatantGlobalInfo> infos = getHumanInfos(humanIds);
		port.returns(infos);
	}

	/**
	 * 获取多个队伍的玩家的全局信息
	 * 
	 * @param unitId
	 */
	@DistrMethod
	public void getInfosByMap(Map<Object, List<Long>> humanIdsMap) {
		Map<Object, List<CombatantGlobalInfo>> result = new LinkedHashMap<Object, List<CombatantGlobalInfo>>();
		for (Map.Entry<Object, List<Long>> entry : humanIdsMap.entrySet()) {
			Object key = entry.getKey();
			List<Long> ids = entry.getValue();
			List<CombatantGlobalInfo> infos = getHumanInfos(ids);
			result.put(key, infos);
		}
		port.returns(result);
	}

	private List<CombatantGlobalInfo> getHumanInfos(List<Long> ids) {
		List<CombatantGlobalInfo> infos = new ArrayList<CombatantGlobalInfo>();
		for (Long hId : ids) {
			if (hId == null) {
				continue;
			}
			if (datas.containsKey(hId)) {
				infos.add(datas.get(hId));
			}
		}
		return infos;
	}

	/**
	 * @param humanIds
	 */
	@DistrMethod
	public void getInfo(List<Long> humansIds) {
		List<CombatantGlobalInfo> humansInfo = new ArrayList<CombatantGlobalInfo>();
		// 优化
		for (Long id : humansIds) {
			CombatantGlobalInfo tmp = datas.get(id);
			if (tmp != null)
				humansInfo.add(tmp);
		}
		port.returns(humansInfo);
	}

	/**
	 * 注册玩家全局信息
	 * 
	 * @param info
	 */
	@DistrMethod
	public void register(CombatantGlobalInfo info) {
		datas.put(info.id, info);
		port.returns("");
	}

	/**
	 * 注册玩家全局信息
	 * 
	 * @param info
	 */
	@DistrMethod
	public void register(List<CombatantGlobalInfo> infos) {
		for (CombatantGlobalInfo info : infos) {
			datas.put(info.id, info);
		}
		port.returns("");
	}

	@DistrMethod
	public void ChangeConnPoint(Long id, CallPoint point) {
		CombatantGlobalInfo info = datas.get(id);
		if (info != null) {
			info.connPoint = point;
		}
	}

	/**
	 * 玩家是否已登录
	 * 
	 * @param humanId
	 * @return
	 */
	public boolean isLogined(Long humanId) {
		boolean logined = false;
		for (CombatantGlobalInfo h : datas.values()) {
			if (h.id == humanId) {
				logined = true;
				break;
			}
		}
		return logined;
	}

	/**
	 * 踢出玩家
	 * 
	 * @param humanId
	 * @param reason
	 */
	@DistrMethod
	public void kick(long humanId, String reason) {
		kickHuman(humanId, reason);
	}

	/**
	 * 踢人下线
	 * 
	 * @param connPoint
	 * @param msg
	 */
	private void kickHuman(long humanId, String reason) {
		// 发送消息 通知玩家被踢
		SCCombatantKick.Builder msg = SCCombatantKick.newBuilder();
		msg.setReason(reason);
		sendMsg(humanId, msg.build());

		// 玩家连接信息
		CombatantGlobalInfo info = datas.get(humanId);

		if (info == null)
			return;
		
		// 断开连接
		ConnectionProxy prx = ConnectionProxy.newInstance(info.connPoint);
		prx.close();
		
		// //直接清除玩家的一些信息 因为如果玩家连接
		CombatantObjectServiceProxy prxSource = CombatantObjectServiceProxy.newInstance(info.stageNodeId, info.stagePortId,
				humanId);
		prxSource.kickClosed();
	}

	/**
	 * 发送消息至玩家
	 * 
	 * @param builder
	 */
	@DistrMethod(argsImmutable = true)
	public void sendMsg(long humanId, Message msg) {
		// 玩家连接信息
		CombatantGlobalInfo info = datas.get(humanId);

		if (info == null)
			return;
		CombatantGlobalManager.inst().sendMsg(info.connPoint, msg);
	}

	/**
	 * 发送消息至全服玩家，有要排除的则设置excludeIds
	 * 
	 * @param excludeIds
	 * @param msg
	 */
	@DistrMethod(argsImmutable = true)
	public void sendMsgToAll(List<Long> excludeIds, Message msg) {
		if (excludeIds == null) {
			excludeIds = new ArrayList<>();
		}
		// 给所有玩家发送消息
		for (CombatantGlobalInfo info : datas.values()) {
			// 排除不需要发送的玩家id
			if (excludeIds.contains(info.id)) {
				continue;
			}
			CombatantGlobalManager.inst().sendMsg(info.connPoint, msg);
		}
	}

	/**
	 * 发送给指定的玩家
	 * 
	 * @param humanIds
	 * @param msg
	 */
	@DistrMethod(argsImmutable = true)
	public void sendMsgTo(List<Long> humanIds, Message msg) {
		if (humanIds == null || humanIds.size() == 0) {
			return;
		}
		// 给所有玩家发送消息
		for (CombatantGlobalInfo info : datas.values()) {
			// 排除不需要发送的玩家id
			if (humanIds.contains(info.id)) {
				CombatantGlobalManager.inst().sendMsg(info.connPoint, msg);
			}
		}
	}

	/**
	 * 给指定地图的所有玩家发送消息
	 * 
	 * @param stageIds
	 * @param msg
	 */
	@DistrMethod
	public void sendMsgToStage(Set<Long> stageIds, Message msg) {
		// 遍历所有玩家发送消息
		for (CombatantGlobalInfo info : datas.values()) {
			if (stageIds.contains(info.stageId)) {
				CombatantGlobalManager.inst().sendMsg(info.connPoint, msg);
			}
		}
	}

	/**
	 * 向全服玩家发送简要事件通知，有要排除的则设置excludeIds
	 * 
	 * @param excludeIds
	 * @param key
	 */
	@DistrMethod(argsImmutable = true)
	public void fireEventToAll(List<Long> excludeIds, int key) {
		if (excludeIds == null) {
			excludeIds = new ArrayList<>();
		}
		// 给所有玩家发送消息
		for (CombatantGlobalInfo info : datas.values()) {
			if (info == null)
				continue;
			// 排除不需要发送的玩家id
			if (excludeIds.contains(info.id)) {
				continue;
			}
			CombatantObjectServiceProxy prx = CombatantObjectServiceProxy.newInstance(info.stageNodeId, info.stagePortId,
					info.id);
			prx.fireEventToHuman(key);

		}
	}

	/**
	 * 清除玩家全局信息
	 * 
	 * @param num
	 */
	@DistrMethod
	public void cancel(long humanId) {
		datas.remove(humanId);
	}

	/**
	 * 调度事件处理
	 */
	public void onSchdule() {
		if (shceduleMap.size() <= 0) {
			return;
		}

		int i = 0;
		for (CombatantGlobalInfo obj : datas.values()) {
			boolean sendSchd = false;
			for (Entry<Integer, Long> entry : shceduleMap.entrySet()) {
				if (obj.shceduleTime < entry.getValue()) {
					// 发送到玩家
					CombatantObjectServiceProxy proxy = CombatantObjectServiceProxy.newInstance(obj.stageNodeId, obj.stagePortId,
							obj.id);
					proxy.onSchedule(entry.getKey(), entry.getValue());
					sendSchd = true;
				}
			}

			obj.shceduleTime = Port.getTime();

			if (sendSchd) {
				i++;
				// 超过数量返回
				if (i > COUNT100Per10SEC) {
					return;
				}
			}

		}
		if (i == 0) {
			shceduleMap.clear();
		}
	}

	/**
	 * 统计注册人数和在线人数，写日志
	 */
	// public void writeOnlineLog() {
	// // 获得数量
	// DB db = DB.newInstance(Combatant.tableName);
	// db.countAll(false);
	// db.listenResult(this::_result_writeOnlineLog);
	// }
	//
	// public void _result_writeOnlineLog(Param results, Param context) {
	// int registerCount = results.get();
	// int onlineCount = datas.size();
	// }

	/**
	 * 检测登录验证串
	 * 
	 * @param humanId
	 * @param token
	 * @return
	 */
	public boolean checkToken(long humanId, String token) {
		CombatantGlobalInfo info = datas.get(humanId);
		if (info != null) {
			if (StringUtils.equals(token, info.token)) {
				return true;
			}
		}
		return false;
	}

}