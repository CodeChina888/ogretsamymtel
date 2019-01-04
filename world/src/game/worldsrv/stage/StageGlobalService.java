package game.worldsrv.stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.CallPoint;
import core.Chunk;
import core.Port;
import core.Service;
import core.connsrv.ConnectionProxy;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.ConnectionStatus;
import core.support.Distr;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.config.ConfMap;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.entity.Human;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.support.Vector2D;
import game.msg.Define.DStage;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EMapType;
import game.msg.MsgStage.SCStageSwitch;
import game.msg.MsgIds;
import game.worldsrv.character.HumanObject;
import game.worldsrv.human.HumanManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.MathUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.stage.StageGlobalInfo;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObjectServiceProxy;
import game.worldsrv.stage.StageServiceProxy;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

@DistrClass(servId = D.SERV_STAGE_GLOBAL, importClass = {HumanObject.class, List.class, 
		Message.class, Vector2D.class, ConnectionStatus.class, GeneratedMessage.class})
public class StageGlobalService extends Service {

	// 地图信息集合：key=stageId
	private final Map<Long, StageGlobalInfo> mapStageInfos = new HashMap<Long, StageGlobalInfo>();

	// 分线系统：key=mapSn, value=<stageId, StageGlobalInfo>对应的所有分线地图
	private final Map<Integer, Map<Long, StageGlobalInfo>> stageMulLines = new HashMap<Integer, Map<Long, StageGlobalInfo>>();
		
	// 计时器-每隔5分钟打印在线人数
	private TickTimer ttOnlineLog = new TickTimer(5 * Time.MIN);
	// 计时器-每隔10分钟清理错误的副本场景
	private TickTimer ttClearErrorInstStage = new TickTimer(10 * Time.MIN);
	
	// 心跳上一针的时间
//	private long lastPulseTime = 0;
		
	public StageGlobalService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return D.SERV_STAGE_GLOBAL;
	}
	
	/**
	 * 在心跳里打印场景信息，并且调用清理
	 */
	@Override
	public void pulseOverride() {
		long now = Port.getTime();
		
		// 打印各个地图的分线以及对应的人数
		pulseStageLog(now);
		
		// 每隔10分钟清理错误的副本场景
		clearErrorInstStage(now);
		
//		this.lastPulseTime = now;
	}
	
	/**
	 * 打印场景信息
	 * @param now
	 */
	private void pulseStageLog(Long now) {
		if(!ttOnlineLog.isPeriod(now)) {
			return;
		}
		
		int countAll = 0;
		Log.stageCommon.info("-----------------打印场景信息pulseStageLog-----------------");
		for (Map<Long, StageGlobalInfo> mulLines : stageMulLines.values()) {
			for (StageGlobalInfo info : mulLines.values()) {
				countAll += info.humanNum;
				Log.stageCommon.info("pulseStageLog Stage Human count line mapSn={},humanNum={},stageId={}",
						info.mapSn, info.humanNum, info.stageId);
			}
		}
		
		Log.stageCommon.info("-----------------pulseStageLog countAllHuman={}-----------------", countAll);
		Log.stageCommon.info("-----------------pulseStageLog countAllStage={}-----------------", mapStageInfos.size());
	}
	
	/**
	 * 每隔10分钟清理错误的副本场景
	 * @param now
	 */
	private void clearErrorInstStage(long now) {
		if(!ttClearErrorInstStage.isPeriod(now)) {
			return;
		}
		
		long interval = ttClearErrorInstStage.getInterval();
		Map<Integer, Integer> infoHumanCount = new HashMap<Integer, Integer>();
		for (StageGlobalInfo info : mapStageInfos.values()) {
			if(infoHumanCount.containsKey(info.mapSn)) {
				infoHumanCount.put(info.mapSn, infoHumanCount.get(info.mapSn) + info.humanNum);
			} else {
				infoHumanCount.put(info.mapSn, info.humanNum);
			}
			
			// 如果场景人数为0 并且是副本 并且建立时间大于30分钟清除 
			if(0 == info.humanNum) {
				ConfMap conf = ConfMap.get(info.mapSn);
				// 不是主城都清理
				if(null != conf && !conf.type.equals(EMapType.common.name())) {
					StageObjectServiceProxy prxTarget = StageObjectServiceProxy.newInstance(info.nodeId, info.portId, info.stageId);
					prxTarget.clearError(now, interval);
				}
			}
		}
	}

	/**
	 * 注册地图信息
	 * @param stageId 地图ID
	 * @param mapSn 地图SN
	 */
	@DistrMethod
	public void infoRegister(long stageId, int stageSn, int mapSn, String stageName, String nodeId, String portId) {
		if(mapStageInfos.containsKey(stageId))
			return;
		
		// 创建信息对象并缓存
		StageGlobalInfo stageInfo = new StageGlobalInfo(stageId, stageSn, mapSn, stageName, nodeId, portId);
		mapStageInfos.put(stageId, stageInfo);

		// 添加分线信息
		ConfMap confMap = ConfMap.get(stageInfo.mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn={}", stageInfo.mapSn);
			return;
		}
		// 判断是否是需要分线的普通地图
		if (confMap.humanMaxNum > 0 && confMap.type.equals(EMapType.common.name())) {
			Map<Long, StageGlobalInfo> mulLines = stageMulLines.get(mapSn);
			if (mulLines == null) {
				mulLines = new HashMap<Long, StageGlobalInfo>();
				stageMulLines.put(stageInfo.mapSn, mulLines);
			}
			stageInfo.lineNum = mulLines.size();// 分线的编号，编号从0开始（0表示主线，1表示第1条分线即第2条记录）
			mulLines.put(stageInfo.stageId, stageInfo);
			
			// 记录创建日志
			Log.stageCommon.info("创建分线的普通地图：stageId={}, mapSn={}, lineNum={}", stageId, mapSn, stageInfo.lineNum);
		} else {
			// 记录创建日志
			Log.stageCommon.info("创建副本地图：stageId={}, mapSn={}", stageId, mapSn);
		}

		Event.fireEx(EventKey.StageRegister, mapSn, "stageId", stageId, "mapSn", mapSn, "stageName", stageName,
				"nodeId", nodeId, "portId", portId);
	}

	/**
	 * 注销地图信息
	 * @param stageId
	 */
	@DistrMethod
	public void infoCancel(long stageId) {
		StageGlobalInfo information = mapStageInfos.remove(stageId);

		if (information == null) {
			Log.stageCommon.warn("销毁地图时发现地图已不存在：stageId={}", stageId);
			return;
		}

		// 添加分线信息
		ConfMap confMap = ConfMap.get(information.mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn={}", information.mapSn);
			return;
		}
		// 判断是否是需要分线的普通地图
		if (confMap.humanMaxNum > 0 && confMap.type.equals(EMapType.common.name())) {
			Map<Long, StageGlobalInfo> mulLines = stageMulLines.get(information.mapSn);
			if (mulLines != null) {
				mulLines.remove(stageId);
			}
		}

		Event.fireEx(EventKey.StageCancel, information.mapSn, "stageId", stageId, "mapSn", information.mapSn,
				"stageName", confMap.name, "nodeId", port.getCallFromNodeId(), "portId", port.getCallFromPortId());
	}

	@DistrMethod
	public void destroy(long stageId) {
		StageGlobalInfo info = mapStageInfos.get(stageId);
		if (info == null) {
			return;
		}

		StageServiceProxy prx = StageServiceProxy.newInstance(info.nodeId, info.portId, D.SERV_STAGE_DEFAULT);
		prx.destroy(stageId);
	}

	@DistrMethod
	public void login(long humanId, CallPoint connPoint, ConnectionStatus connStatus, List<List<?>> lastStageIds, int firstStory) {
		Long pid = port.createReturnAsync();
//		int mapSnNewbie = 0;// 新手剧情副本地图SN
//		int stageSnNewbie = 0;// 新手剧情副本关卡SN
//
//		if (firstStory == 0 && ParamManager.newbieMapSn > 0 && ParamManager.newbieStageSn > 0) {
//			// 第一次创建角色后，进入的开场副本场景
//			mapSnNewbie = ParamManager.newbieMapSn;
//			stageSnNewbie = ParamManager.newbieStageSn;
//			// 检查配置信息
//			ConfMap confMapFirst = ConfMap.get(mapSnNewbie);
//			if (confMapFirst == null) {// 地图Sn不存在
//				Log.table.error("创建新手剧情副本失败：login() no find mapSnNewbie={}", mapSnNewbie);
//				mapSnNewbie = 0;
//			}
//			ConfInstStage confInstStage = ConfInstStage.get(stageSnNewbie);
//			if (confInstStage == null) {// 副本关卡Sn不存在
//				Log.table.error("创建新手剧情副本失败：login() no find stageSnNewbie={}", stageSnNewbie);
//				stageSnNewbie = 0;
//			}
//		}
//		if (mapSnNewbie > 0 && stageSnNewbie > 0) {
//			// 第一次创建角色后，进入的开场副本场景
//			//地图配置信息
//			String portId = StageManager.inst().getStagePortId();
//			StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
//			prx.createStageInstance(stageSnNewbie, mapSnNewbie, ECrossFightType.FIGHT_NEWBIE_VALUE);
//			prx.listenResult(this::_result_create, "pid", pid);
//			
//			//StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
//			//prx.createInstance(stageSnNewbie, mapSnNewbie, false);
//			//prx.listenResult(this::_result_login, "pid", pid, "humanId", humanId, "connPoint", connPoint);
//		} else {
			long stageId = 0;
			if (!lastStageIds.isEmpty() && !lastStageIds.get(0).isEmpty()) {
				stageId = Utils.longValue(lastStageIds.get(0).get(0));
			}

			StageGlobalInfo stageInfo = null;
			// 只处理common的
			// 数据格式：[[100010000000022097,101,19.63238,30.99205,"rep"],[1,1,65.5440673828125,7.353435516357422,"common"]]
			for (List<?> list : lastStageIds) {
				stageInfo = this.mapStageInfos.get(list.get(0));
				ConfMap confMap = ConfMap.get((Integer) (list.get(1)));
				if (confMap != null && confMap.type.equals(EMapType.common.name())) {
					// && stageInfo != null) {
					if (stageInfo != null) {
						stageId = stageInfo.stageId;
					} else {
						stageId = confMap.sn;
					}
					break;
				}
			}
			
			// 如果为空，就取默认出生地图
			if(null == stageInfo) {
				stageId = ParamManager.firstMapSn;
			}

			StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
			proxy.applyStageBySn(stageId);
			proxy.listenResult(this::_result_login, "pid", pid, "humanId", humanId, 
					"connPoint", connPoint, "connStatus", connStatus);
//		}
	}

	public void _result_login(boolean timeout, Param results, Param context) {
		Long pid = Utils.getParamValue(context, "pid", -1L);
		if (pid < 0) {
			Log.game.error("===_result_login pid={}", pid);
			return;
		}
		if (timeout) {// 30秒超时
			port.returnsAsync(pid, "code", -2000);
			return;
		}

		long humanId = Utils.getParamValue(context, "humanId", -1L);
		CallPoint connPoint = Utils.getParamValue(context, "connPoint", null);
		ConnectionStatus connStatus = Utils.getParamValue(context, "connStatus", null);
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		if (humanId < 0 || connPoint == null || stageId < 0) {
			Log.game.error("===_result_login stageId={},humanId={},connPoint={}", stageId, humanId, connPoint.toString());
			return;
		}

		StageGlobalInfo stageInfo = this.mapStageInfos.get(stageId);
		if (stageInfo != null) {
			StageObjectServiceProxy prx = StageObjectServiceProxy.newInstance(stageInfo.nodeId, stageInfo.portId,
					stageInfo.stageId);
			prx.login(humanId, connPoint, connStatus, stageInfo.stageId, new Vector2D(-1f, -1f));
			prx.listenResult(this::_result_login1, "pid", pid);
		}
	}

	public void _result_login1(boolean timeout, Param results, Param context) {
		Long pid = Utils.getParamValue(context, "pid", -1L);
		if (pid < 0) {
			Log.game.error("===_result_login1 pid={}", pid);
			return;
		}
		if (timeout) {// 30秒超时
			port.returnsAsync(pid, "code", -2000);
			return;
		}

		port.returnsAsync(pid, results.toArray());
	}

	@DistrMethod
	public void applyStageBySn(long stageId) {
		long result = -1;// 返回的地图真实ID
		int mapSn = 0;
		// Log.stageMove.debug("===applyStageBySn(stageId={})", stageId);
		StageGlobalInfo info = mapStageInfos.get(stageId);
		if (info == null) {
			Log.stageCommon.error("出现了无法处理的stageId={}", stageId);
			port.returns("stageId", result);
			return;
		} else {
			mapSn = info.mapSn;
		}
		// 地图配置信息
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			port.returns("stageId", result);
			Log.table.error("ConfMap配表错误，no find sn={}", mapSn);
			return;
		}

		Map<Long, StageGlobalInfo> mulLines = null;
		// 判断是否是需要分线的地图
		if (confMap.humanMaxNum == 0) {// 无需分线的地图
			result = stageId;
		} else {//如果是分线地图，就获取分线信息，如果满了，就设置标记，在后面生成一个
			// Log.stageMove.debug("===applyStageBySn: mulLines mapSn={}",
			// mapSn);
			mulLines = stageMulLines.get(mapSn);
			if (mulLines == null || confMap.humanMaxNum == 1) {
				result = -1;
			} else {
				if(info.humanNum < confMap.humanMaxNum) {
					 result = info.stageId;
				} else {
					for (StageGlobalInfo stageGlobalInfo : mulLines.values()) {
						if (stageGlobalInfo.humanNum < confMap.humanMaxNum) {
							result = stageGlobalInfo.stageId;
							break;
						}
					}
				}
			}
			// Log.stageMove.debug("===applyStageBySn: mulLines mapSn={}, result={}",
			// mapSn, result);
		}

		// 如果需要分线 并且是一般地图
		if (result < 0 && confMap.type.equals(EMapType.common.name())) {
			// Log.stageMove.debug("===applyStageBySn: mulLines common mapSn={}",
			// mapSn);
			// 创建新地图
			// 获得地图分线数量
			int lineNum = 1;
			if (mulLines != null) {
				lineNum = mulLines.size() + 1;
			}

			long stageID = Port.applyId();
			Long pid = port.createReturnAsync();
			String portId = StageManager.inst().getStagePortId();
			StageServiceProxy proxy = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
			proxy.createStageCommon(stageID, confMap.sn, lineNum);
			proxy.listenResult(this::_result_applyStageBySn, "pid", pid, "mapSn", confMap.sn);
			
			// 提前注册
			infoRegister(stageID, info.stageSn, confMap.sn, confMap.name, Distr.getNodeId(portId), portId);
		} else {
			port.returns("stageId", result);
		}

		Log.stageCommon.info("申请的stageId={}", stageId);
	}

	private void _result_applyStageBySn(boolean timeout, Param results, Param context) {
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		String nodeId = Utils.getParamValue(results, "nodeId", "");
		String portId = Utils.getParamValue(results, "portId", "");

		Long pid = Utils.getParamValue(context, "pid", -1L);
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		if (stageId < 0 || nodeId.isEmpty() || portId.isEmpty() || pid < 0 || mapSn == 0) {
			Log.game.error("===_result_applyStageBySn stageId<0, nodeId={}, portId={}, pid={}, mapSn={}", stageId,
					nodeId, portId, pid, mapSn);
			return;
		}
		if (timeout) {// 30秒超时
			port.returnsAsync(pid, "code", -1000, "reason", "等待返回请求超时");
			return;
		}

//		ConfMap confMap = ConfMap.get(mapSn);
//		if (confMap == null) {
//			Log.table.error("ConfMap配表错误，no find sn={}", mapSn);
//			return;
//		}
//		infoRegister(stageId, mapSn, confMap.name, nodeId, portId);
		// Log.stageMove.debug("===_result_applyStageBySn: mapSn={},stageId={}",
		// mapSn, stageId);
		port.returnsAsync(pid, "stageId", stageId);
	}

	/**
	 * 地图玩家数量增加
	 * @param stageId
	 */
	@DistrMethod
	public void stageHumanNumAdd(long stageId) {
		stageHumanNumChange(stageId, true);
	}

	/**
	 * 地图玩家数量减少
	 * @param stageId
	 */
	@DistrMethod
	public void stageHumanNumReduce(long stageId) {
		stageHumanNumChange(stageId, false);
	}

	/**
	 * 地图玩家数量变动
	 * @param stageId
	 * @param add
	 */
	private void stageHumanNumChange(long stageId, boolean add) {
		StageGlobalInfo info = this.mapStageInfos.get(stageId);
		if (info == null)
			return;

		// 地图人数变动
		if (add) {// 增加
			info.humanNum = Math.max(0, info.humanNum + 1);
		} else {// 减少
			info.humanNum = Math.max(0, info.humanNum - 1);
			
			// 地图没人则删除地图
			if(info.humanNum <= 0) {
				removeEmptyMap(info);
			}
		}

		if (Log.stageCommon.isDebugEnabled()) {
			ConfMap confMap = ConfMap.get(info.mapSn);
			if (confMap != null && confMap.type.equals(EMapType.common.name())) {
				Log.stageCommon.debug("===stageHumanNumChange mapSn={},num={}", info.mapSn, info.humanNum);
			}
		}
	}
	private void removeEmptyMap(StageGlobalInfo info) {
		Map<Long, StageGlobalInfo> mulLines = stageMulLines.get(info.mapSn);
		if(mulLines != null) {
			// 不处理主地图，只处理分线地图
			if(info.humanNum <= 0 && info.lineNum > 0 && info.stageId != info.mapSn) {
				ConfMap conf = ConfMap.get(info.mapSn);
				if(null != conf && conf.humanMaxNum > 0) {
					// 将地图从全局信息中删除
					infoCancel(info.stageId);
					
					StageServiceProxy proxy = StageServiceProxy.newInstance(info.nodeId, info.portId, D.SERV_STAGE_DEFAULT);
					proxy.destroy(info.stageId);
				}
			}
		}
	}

	@DistrMethod
	public void switchToStage(HumanObject humanObj, long stageId, Vector2D pos, Vector2D dir) {
		Long pid = port.createReturnAsync();
		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		proxy.applyStageBySn(stageId);
		proxy.listenResult(this::_result_switchToStage1, "pid", pid, "humanObj", humanObj, "pos", pos, "dir", dir);
	}
	private void _result_switchToStage1(Param results, Param context) {
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		Vector2D pos = Utils.getParamValue(context, "pos", null);
		Vector2D dir = Utils.getParamValue(context, "dir", null);
		if (stageId < 0 || humanObj == null || pos == null) {
			Log.game.error("===_result_switchToStage1 humanObj={}, posAppear={}, stageId={}", humanObj, pos, stageId);
			return;
		}

		// 根据配置重设出生点
		StageGlobalInfo stageGlobal = this.mapStageInfos.get(stageId);
		if (stageGlobal == null) {
			Log.stageCommon.error("===切换地图出错：StageGlobalService._result_switchToStage1 no finded stageId={}", stageId);
			//return;
			
			// 如果地图不存在那么修正到玩家出生
			stageGlobal = this.mapStageInfos.get(ParamManager.firstMapSn);
			pos = StageManager.inst().getHumanPos(ParamManager.firstMapSn);
			dir = StageManager.inst().getHumanDir(ParamManager.firstMapSn);
		}

		// 常规地图位置
		ConfMap confMap = ConfMap.get(stageGlobal.mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn={}", stageGlobal.mapSn);
			return;
		}
		// 玩家上一张地图坐标
		Vector2D posOld = new Vector2D(humanObj.posNow.x, humanObj.posNow.y);

		// 玩家新地图的坐标
		humanObj.posNow = pos;
		humanObj.dirNow = dir;
		// Log.human.debug("===switchToStage1 posNow={},name={},mapSn={}",
		// humanObj.posNow, humanObj.name, stageGlobal.mapSn);

		/* 离开原地图 */
		// 从原地图清离,原地图玩家数-1
		long stageIdNow = humanObj.getStageStageId();
		StageGlobalInfo infoSource = this.mapStageInfos.get(stageIdNow);
		if (infoSource == null) {
			Log.stageCommon.error("===离开原地图失败：_result_switchToStage1 error in no find stageIdNow={},humanId={}",
					stageIdNow, humanObj.id);
		} else {
			HumanGlobalServiceProxy prxHumanService = HumanGlobalServiceProxy.newInstance();
			prxHumanService.getInfo(humanObj.id);
			prxHumanService.listenResult(this::_result_switchToStage2, "stageId", stageId, "humanObj", humanObj, "posOld", posOld);
		}
	}
	private void _result_switchToStage2(Param results, Param context) {
		HumanGlobalInfo info = results.get();
		
		HumanObject humanObj = context.get("humanObj");
		long stageId = context.get("stageId");
		Vector2D posOld = context.get("posOld");
		if(info == null){
			// FIXME 需要排查，信息不存在于humanGlobalinfo里
			StageGlobalInfo infoTarget = this.mapStageInfos.get(stageId);

			// 更新玩家全局信息
			Human human = humanObj.getHuman();
			HumanGlobalServiceProxy prxHumanStatus = HumanGlobalServiceProxy.newInstance();
			prxHumanStatus.stageIdModify(human.getId(), infoTarget.stageId, infoTarget.mapName, infoTarget.nodeId, infoTarget.portId);

			// 注册玩家至新地图，新地图玩家数+1(业务逻辑只能写在此处前，防止玩家数据提前串行化，到时修改失效)
			StageObjectServiceProxy prxTarget = StageObjectServiceProxy.newInstance(infoTarget.nodeId, infoTarget.portId, infoTarget.stageId);
			prxTarget.register(humanObj);
			prxTarget.listenResult(this::_result_switchToStage, "infoTarget", infoTarget, "humanObj", humanObj);
			
			// 切完地图强制同步当前地图数量
			stageHumanNumChange(stageId, true);
			return;
		}
		HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
		
		prxHumanObj.leave();
		humanObj.setSwitchFrom(humanObj.getStageMapSn());
		
		long stageIdNow = humanObj.getStageStageId();
		StageGlobalServiceProxy prx =StageGlobalServiceProxy.newInstance();  
		prx.stageHumanNumReduce(stageIdNow);

		/* 业务逻辑部分 */
		// 更新玩家地图路径信息
		StageGlobalInfo stageGlobal = this.mapStageInfos.get(stageId);
		ConfMap confMap = ConfMap.get(stageGlobal.mapSn);
		
		HumanManager.inst().recordStage(humanObj, stageId, stageGlobal.mapSn, confMap.type, posOld);
		// if(conf.humanMaxNum > 0) {//lock by sjh,玩家分线数量>0，认为是非副本地图？
		// HumanManager.inst().recordStage(humanObj, temp.stageSn, temp.stageSn,
		// conf.type, posOld);
		// } else {
		// HumanManager.inst().recordStage(humanObj, stageTargetId,
		// temp.stageSn, conf.type, posOld);
		// }

		/* 进入新地图 */
		StageGlobalInfo infoTarget = this.mapStageInfos.get(stageId);

		// 更新玩家全局信息
		Human human = humanObj.getHuman();
		HumanGlobalServiceProxy prxHumanStatus = HumanGlobalServiceProxy.newInstance();
		prxHumanStatus.stageIdModify(human.getId(), infoTarget.stageId, infoTarget.mapName, infoTarget.nodeId, infoTarget.portId);

		// 注册玩家至新地图，新地图玩家数+1(业务逻辑只能写在此处前，防止玩家数据提前串行化，到时修改失效)
		StageObjectServiceProxy prxTarget = StageObjectServiceProxy.newInstance(infoTarget.nodeId, infoTarget.portId, infoTarget.stageId);
		prxTarget.register(humanObj);
		prxTarget.listenResult(this::_result_switchToStage, "infoTarget", infoTarget, "humanObj", humanObj);
		
		// 切完地图强制同步当前地图数量
		stageHumanNumChange(stageId, true);
	}
	private void _result_switchToStage(boolean timeout, Param results, Param context) {
		StageGlobalInfo infoTarget = Utils.getParamValue(context, "infoTarget", null);
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		int stageSn = Utils.getParamValue(results, "stageSn", 0);
		int count = Utils.getParamValue(results, "count", 0);	//当前玩家数量
		boolean success = Utils.getParamValue(results, "success", false);
		if(!success) {
			Log.stageCommon.error("====切换场景出现错误，返回主城");
			// 离开副本
			humanObj.quitToCommon(infoTarget.mapSn);
			return;
		}
		if (infoTarget == null || humanObj == null || stageSn < 0) {
			Log.game.error("===切换地图错误：_result_switchToStage humanObj={},infoTarget={},pos={},dir={},stageSn={}", humanObj, infoTarget, stageSn);
			return;
		}
		
		// 打日志
		//Log.stageCommon.error("强制同步地图在线玩家数量，地图{}，实际在线量{}，记录在线量{}", infoTarget.sn, count, infoTarget.humanNum);
		
		// 强制同步数量
		if (count > 0)
			infoTarget.humanNum = count;

		// Log.human.debug("===SCStageSwitch posNow={},name={},mapSn={}",
		// posNow, humanObj.name, infoTarget.mapSn);
		/* 消息处理 */
		// 返回地图切换消息
		SCStageSwitch.Builder msg = SCStageSwitch.newBuilder();
		DStage.Builder dStage = DStage.newBuilder();
		dStage.setPosNow(humanObj.posNow.toMsg());
		dStage.setDirNow(humanObj.dirNow.toMsg());
		dStage.setStageId(infoTarget.stageId);
		dStage.setMapSn(infoTarget.mapSn);
		dStage.setInstSn(infoTarget.stageSn);
		dStage.setLineNum(infoTarget.lineNum);
		msg.setStage(dStage);

		// 玩家连接
		CallPoint connPoint = humanObj.connPoint;
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(MsgIds.getIdByClass(msg.build().getClass()), new Chunk(msg));

		// 更新连接服务器的玩家信息
		prx.updateStatus(infoTarget.nodeId, infoTarget.portId, infoTarget.stageId);
	}
		
	private void _result_create(boolean timeout, Param results, Param context) {
		Long pid = Utils.getParamValue(context, "pid", -1L);
		if (pid < 0) {
			Log.game.error("===_result_createInstance pid={}", pid);
			return;
		}
		if (timeout) {// 30秒超时
			port.returnsAsync(pid, "code", -1000, "reason", "等待返回请求超时，创建副本场景失败！");
			return;
		}

		port.returnsAsync(pid, results.toArray());
	}
	
	/**
	 * 退出副本 活动 等地图到进入前的主地图
	 * @param humanObj
	 * @param params
	 */
	@DistrMethod(argsImmutable=true)
	public void quitToCommon(HumanObject humanObj, int nowSn, Object... params) {
		Param param = new Param(params);
		// 是否需要返回
		boolean callBack = Utils.getParamValue(param, "callBack", false);

		int mapSn = HumanManager.inst().stageHistoryCommonSn(humanObj);
		if (mapSn > 0) {
			// 出生点及朝向
			Vector2D pos = HumanManager.inst().stageHistoryCommon(humanObj);
			Vector2D dir = MathUtils.getDir(pos, StageManager.inst().getHumanDir(mapSn));
			//切回场景
			StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
			prx.switchToStage(humanObj, mapSn, pos, dir);
		} else {
			Log.stageCommon.info("StageGlobalService.quitToCommon : error goto mapSn={} from nowSn={}", mapSn, nowSn);
		}

		if (callBack) {
			port.returns();
		}
	}
	
	/**
	 * 每个service预留空方法
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs){
		
	}
	
	/**
	 * 转发新手战斗消息
	 * @param stageId
	 */
	@DistrMethod
	public void dispatchCombatMsg(long stageId, long connId, CallPoint connPoint, int msgId, GeneratedMessage msg) {
		StageGlobalInfo info = mapStageInfos.get(stageId);
		if (info == null) {
			return;
		}

		StageObjectServiceProxy prx = StageObjectServiceProxy.newInstance(info.nodeId, info.portId, info.stageId);
		prx.dispatchCombatMsg(stageId, connId, connPoint, msgId, msg);
	}
}
