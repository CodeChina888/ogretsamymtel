package game.worldsrv.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.Message.Builder;

import core.Node;
import core.Port;
import core.PortPulseQueue;
import core.scheduler.ScheduleTask;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.SysException;
import core.support.observer.Listener;
import game.msg.Define.DStage;
import game.msg.Define.DStageObject;
import game.msg.Define.EMapType;
import game.msg.MsgStage.SCStageEnterEnd;
import game.msg.MsgStage.SCStageEnterResult;
import game.msg.MsgStage.SCStageObjectAppear;
import game.msg.MsgStage.SCStageObjectDisappear;
import game.msg.MsgStage.SCStagePullTo;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitObject;
import game.worldsrv.character.WorldObject;
import game.worldsrv.config.ConfMap;
import game.worldsrv.config.ConfMapPosition;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.AppearType;
import game.worldsrv.enumType.DisappearType;
import game.worldsrv.enumType.SwitchState;
import game.worldsrv.human.HumanManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.MathUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.Vector3D;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.support.pathFinding.HeightFinding;
import game.worldsrv.support.pathFinding.PathFinding;

public class StageManager extends ManagerBase {

	public static final AtomicInteger portIdAuto = new AtomicInteger();//地图port分配值
	
//	public static int MaxDummy = 20;// 最大假人数量
//	public List<Long> listHumanIds = new ArrayList<Long>();// 保存所有假人的Id
//	public List<Long> listReserveIds = new ArrayList<Long>();// 保存备用假人的Id
//	public Map<Long, Human> mapDBHuman = new HashMap<Long, Human>();// 从数据库里查询过的玩家数据，key=humanId

	/**
	 * 获取实例
	 * @return
	 */
	public static StageManager inst() {
		return inst(StageManager.class);
	}
	
	/**
	 * 随机分配到场景port
	 * @return
	 */
	public String getStagePortId() {
		int id = portIdAuto.incrementAndGet();
		if (id < 0) {// 负数则重置为0
			id = 0;
			portIdAuto.set(id);
		}
		if(D.PORT_STAGE_STARTUP_NUM > 3)
			return D.PORT_STAGE_PREFIX + ((id % (D.PORT_STAGE_STARTUP_NUM - 2)) + 2);
		else
			return D.PORT_STAGE_PREFIX + id % D.PORT_STAGE_STARTUP_NUM;
	}

	/**
	 * 游戏启动时 创建主地图
	 * @param params
	 * @throws Exception
	 */
	@Listener(EventKey.GameStartUpBefore)
	public void _listener_GameStartUpBefore(Param params) {
		try {
			Node node = params.get("node");
			String parentNodeId = node.getPartId();

			// 初始化地图
			for (int i = 0; i < D.PORT_STAGE_STARTUP_NUM; ++i) {
				// 拼PortId
				String portId = D.PORT_STAGE_PREFIX + i;
				
				//验证启动Node
				String nodeId = Distr.getPartNodeId(portId);
				if(!parentNodeId.equals(nodeId)) {
					continue;
				}

				// 创建地图Port
				StagePort portStage = new StagePort(portId);
				portStage.startup(node);

				// 默认服务
				StageService stageServ = new StageService(portStage);
				stageServ.startup();
				portStage.addService(stageServ);

				// 加载下属主地图
				List<ConfMap> stages = ConfMap.findBy(ConfMap.K.type, EMapType.common.name());
				for (ConfMap s : stages) {
					portStage.createCommonSafe(s.sn);
				}

			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 创建普通地图
	 * @param mapSn
	 */
	public StageObject createCommon(int mapSn) {
		StagePort port = (StagePort) Port.getCurrent();

		StageObject stage = new StageObject(port, mapSn, 0, mapSn);
		stage.startup();

		return stage;
	}

	/**
	 * 摧毁地图
	 */
	public void destory(long stageId) {
		StagePort port = (StagePort) Port.getCurrent();
		StageObject stage = port.getStageObject(stageId);
		if (stage != null) {
			stage.destory();
		}
	}

	/**
	 * 发送消息至玩家
	 */
	public void sendMsgToHumans(Builder builder, Collection<HumanObject> collection) {
		for (HumanObject humanObj : collection) {
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家
			humanObj.sendMsg(builder);
		}
	}

	/**
	 * 广播给场景内的所有玩家
	 */
	public void sendMsgToArea(Builder builder, StageObject stageObj) {
		sendMsgToArea(builder, stageObj, null);
	}
	/**
	 * 广播给场景内指定位置的周围玩家
	 */
	public void sendMsgToArea(Builder builder, StageObject stageObj, Vector2D pos) {
		if (null == pos) {// 全场景广播
			sendMsgToHumans(builder, stageObj.getHumanObjs().values());
		} else {// 只广播给周围玩家
			sendMsgToHumans(builder, getHumanObjsInArea(stageObj, pos));
		}
		
		// 获取九宫格
//		StageCell cell = stageObj.getCell(pos);
//		if (cell == null)
//			return;
//
//		List<StageCell> cells = getCellsInArea(stageObj, cell);
//
//		Message msg = builder.build();
//		Chunk ch = new Chunk(msg);
//		int id = MsgIds.getIdByClass(msg.getClass());
//		for (StageCell c : cells) {
//			c.idList.add(id);
//			c.chunkList.add(ch);
//		}
	}

	/**
	 * 获取某张地图中以某个地图格为中心的九宫格
	 * @return
	 */
	public List<StageCell> getCellsInArea(StageObject stageObj, StageCell cell) {
		List<StageCell> result = new ArrayList<>();

		int i = cell.i;
		int j = cell.j;

		int[] is = {i - 1, i, i + 1};
		int[] js = {j - 1, j, j + 1};
		for (int y : js) {
			for (int x : is) {
				StageCell temp = stageObj.getCell(x, y);
				if (temp == null)
					continue;
				result.add(temp);
			}
		}
		return result;
	}

	/**
	 * 获得stageCell中的玩家
	 * @param stageObj
	 * @param pos
	 * @return
	 */
	public List<HumanObject> getHumanObjsInArea(StageObject stageObj, Vector2D pos) {
		List<HumanObject> result = new ArrayList<>();
		if (stageObj == null) {
			return result;
		}
		// 获取九宫格
		StageCell cell = stageObj.getCell(pos);
		if (cell == null)
			return result;

		List<StageCell> cells = getCellsInArea(stageObj, cell);

		// 在九宫格中获取玩家
		for (StageCell c : cells) {
			for (HumanObject ho : c.getHumans().values()) {
				//if (ho.isInWorld()) {
					result.add(ho);
				//}
			}
		}

		return result;
	}

	/**
	 * 地图单元跨地图格，不只是人，还可能是怪物等
	 * @param cellBegin
	 * @param cellEnd
	 */
	public void cellChanged(StageCell cellBegin, StageCell cellEnd, WorldObject obj) {
		// 不是同一张地图内
		if (!cellBegin.isInSameStage(cellEnd)) {
			throw new SysException("严重错误！地图单元跨不同地图的地图格");
		}
		// 没跨格
		if (cellBegin.isEquals(cellEnd))
			return;

		StageObject stageObj = obj.stageObj;
		if (Log.stageMove.isDebugEnabled()) {
			Log.stageMove.debug("地图单元跨地图格子了，objId={}，cellBegin={}，cellEnd={}", obj.id, cellBegin.i + "," + cellBegin.j,
					cellEnd.i + "," + cellEnd.j);
		}
		// 把obj从旧的区域移走，加入到新的区域
		cellBegin.delWorldObject(obj);
		cellEnd.addWorldObject(obj);

		// 通知旧的区域有地图单位离开
		List<StageCell> cellsLeave = getCellsChangedLeave(stageObj, cellBegin, cellEnd);
		SCStageObjectDisappear.Builder msgObjDisappear = obj.createMsgDisappear(DisappearType.Disappear);
		for (StageCell cell : cellsLeave) {
			for (HumanObject humanObj : cell.getHumanObjects().values()) {
				if (humanObj.id == obj.id)
					continue;
				// 发送消息
				humanObj.sendMsg(msgObjDisappear);
				if (Log.stageMove.isDebugEnabled()) {
					Log.stageMove.debug("移动单元{}从{}的视野中消失", obj.id, humanObj.name);
				}
			}
		}

		// 通知新的区域有物体进入
		SCStageObjectAppear.Builder msgObjAppear = obj.createMsgAppear(AppearType.Appear);

		List<StageCell> cellsNew = this.getCellChangedEnter(stageObj, cellBegin, cellEnd);
		for (StageCell cell : cellsNew) {
			for (HumanObject humanObj : cell.getHumanObjects().values()) {
				if (humanObj.id == obj.id)
					continue;
				// 发送消息
				humanObj.sendMsg(msgObjAppear);
				//if (Log.stageMove.isDebugEnabled()) {
					Log.stageMove.info("移动单元{}名字为{}出现在{}的视野中", obj.id, humanObj.name);
				//}
			}
		}

		// 给玩家发送周围信息
		if (obj instanceof HumanObject) {
			HumanObject humanObj = (HumanObject) obj;
			// 离开格子中的东西
			for (StageCell cell : cellsLeave) {
				for (WorldObject o : cell.getWorldObjects().values()) {
					humanObj.sendMsg(o.createMsgDisappear(DisappearType.Disappear));
				}
			}
			// 新增格子中的东西
			for (StageCell cell : cellsNew) {
				for (WorldObject wo : cell.getWorldObjects().values()) {
					if (wo.id == obj.id)
						continue;
					if (!wo.isInWorld())
						continue;
					humanObj.sendMsg(wo.createMsgAppear(AppearType.Appear));
				}
			}
		}

	}

	/**
	 * 获取进入新区域后，新九宫格对比旧九宫格的增加区域。
	 * @param cellBegin
	 * @param cellEnd
	 * @return
	 */
	private List<StageCell> getCellChangedEnter(StageObject stageObj, StageCell cellBegin, StageCell cellEnd) {
		// 新旧区域
		List<StageCell> begin = getCellsInArea(stageObj, cellBegin);
		List<StageCell> end = getCellsInArea(stageObj, cellEnd);

		// 取出新增部分
		List<StageCell> result = new ArrayList<>();
		for (StageCell c : end) {
			if (begin.contains(c))
				continue;

			result.add(c);
		}

		return result;
	}

	/**
	 * 获取进入新区域后，新九宫格对比旧九宫格减少的区域。
	 * @param cellBegin
	 * @param cellEnd
	 * @return
	 */
	private List<StageCell> getCellsChangedLeave(StageObject stageObj, StageCell cellBegin, StageCell cellEnd) {
		// 新旧区域
		List<StageCell> begin = getCellsInArea(stageObj, cellBegin);
		List<StageCell> end = getCellsInArea(stageObj, cellEnd);

		// 取出减少部分
		List<StageCell> result = new ArrayList<>();
		for (StageCell c : begin) {
			if (end.contains(c))
				continue;

			result.add(c);
		}

		return result;
	}

	/**
	 * 通过坐标来获取区九宫格域内所有单元
	 * @param stageObj
	 * @param pos
	 * @return
	 */
	public List<WorldObject> getWorldObjsInArea(StageObject stageObj, Vector2D pos) {
		List<WorldObject> result = new ArrayList<>();
		// 获取九宫格
		StageCell cell = stageObj.getCell(pos);
		if (cell == null)
			return result;

		List<StageCell> cells = this.getCellsInArea(stageObj, cell);

		// 在九宫格中获取地图单元
		for (StageCell c : cells) {
			for (WorldObject wo : c.getWorldObjects().values()) {
				result.add(wo);
			}
		}

		return result;
	}

	/**
	 * 将玩家拉到当前地图上的某个位置
	 * @param humanObj
	 * @param pos 位置
	 * @param dir 朝向
	 */
	public void pullTo(HumanObject humanObj, Vector2D pos, Vector2D dir) {
		// 原地点发送消失消息
		humanObj.stageHide();

		humanObj.posNow = pos;
		humanObj.dirNow = dir;

		// 通知前端刷新到当前点
		SCStagePullTo.Builder msgPull = SCStagePullTo.newBuilder();
		msgPull.setPos(humanObj.posNow.toMsg());
		humanObj.sendMsg(msgPull.build());

		// lock by shenjh,屏蔽下发该消息
		// SCStageEnterResult.Builder msgER = SCStageEnterResult.newBuilder();
		// for(WorldObject o : getWorldObjsInArea(humanObj.stageObj,
		// humanObj.posNow)) {
		// if(!o.isInWorld())
		// continue;
		// if(o.equals(humanObj))
		// continue;
		//
		// msgER.addObj(o.createMsg());
		// }
		// humanObj.sendMsg(msgER);

		// 从现在的地点出现
		humanObj.stageShow();
	}

	/**
	 * 将玩家复活到当前地图上的某个位置
	 * @param humanObj
	 * @param pos 位置
	 * @param dir 朝向
	 */
	public void pullToRevive(HumanObject humanObj, Vector2D pos, Vector2D dir) {
		// 原地点发送消失消息
		humanObj.stageHide();

		humanObj.posNow = pos;
		humanObj.dirNow = dir;

		// 通知前端刷新到当前点
		SCStagePullTo.Builder msgPull = SCStagePullTo.newBuilder();
		msgPull.setPos(humanObj.posNow.toMsg());
		humanObj.sendMsg(msgPull.build());

		// lock by shenjh,屏蔽下发该消息
		// SCStageEnterResult.Builder msgER = SCStageEnterResult.newBuilder();
		// for(WorldObject o : getWorldObjsInArea(humanObj.stageObj,
		// humanObj.posNow)) {
		// if(!o.isInWorld())
		// continue;
		// if(o.equals(humanObj))
		// continue;
		//
		// msgER.addObj(o.createMsg());
		// }
		// humanObj.sendMsg(msgER);

		// 从现在的地点出现
		humanObj.stageShowRevive();
	}
	
	/**
	 * 退出副本 活动 等地图到进入前的主地图
	 * @param humanObj
	 * @param params 可以不用填
	 */
	public void quitToCommon(final HumanObject humanObj, final Object... params) {
		// 正在切地图状态中
		if (humanObj.isStageSwitching)
			return;

		long humanId = humanObj.id;
//		Param param = new Param(params);
//		param.put("oldGameType", humanObj.stageObj.confMap.gameType);
//		long delay = Utils.getParamValue(param, "delay", 100L);
		
		//设置玩家状态为正在切换地图中
		humanObj.setStageSwitching(true);
		
		Port port = Port.getCurrent();
		// 切换地图加200ms延迟，并且改变用户状态设置为切换地图中
		port.getService(D.SERV_STAGE_DEFAULT).scheduleOnce(new ScheduleTask() {
			@Override
			public void execute() {
				// 如果已经退出，就返回
				if (humanObj == null || humanObj.stageObj == null) {
					Log.stageCommon.info("===Human already exit : humanId={}", humanId);
					return;
				}
				
				// 切换地图
				humanObj.quitToCommon(humanObj.stageObj.confMap.sn, params);
			}
		}, 100L);
	}

	/**
	 * 切换地图,尽量用此处方法，这里会加延迟切换并且改变玩家状态
	 * @param humanObj
	 * @param stageId 场景ID（普通地图代表mapSn，副本地图代表分配的唯一场景ID）
	 * @param pos 切换地图后的出现位置，为null的话出现在默认位置
	 * @param dir 朝向
	 */
	public void switchTo(final HumanObject humanObj, final long stageId, final Vector2D pos, final Vector2D dir) {
		// 正在切地图状态中
		if (humanObj.isStageSwitching())
			return;
		
		// 保存玩家场景位置记录
		HumanManager.inst().saveStageHistory(humanObj);
		//设置客户端标记
		humanObj.isClientStageReady = false;
		
		//设置玩家状态为正在切换地图中
		humanObj.setStageSwitching(true);

		Port port = Port.getCurrent();
		// 切换地图加200ms延迟，并且改变用户状态设置为切换地图中
		port.getService(D.SERV_STAGE_DEFAULT).scheduleOnce(new ScheduleTask() {
			@Override
			public void execute() {
				// 切换地图
				humanObj.switchState = SwitchState.WaitGlobal;
				StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
				prx.switchToStage(humanObj, stageId, pos, dir);
			}
		}, 200);
	}
	
	/**
	 * 获取玩家所在地图场景信息
	 * @param humanObj
	 * @return
	 */
	public DStage getDStage(HumanObject humanObj) {
		try {
			DStage.Builder dStage = DStage.newBuilder();
			dStage.setPosNow(new Vector2D(humanObj.posNow.x, humanObj.posNow.y).toMsg());
			dStage.setDirNow(MathUtils.getDir(humanObj.posNow, StageManager.inst().getHumanDir(humanObj.stageObj.mapSn)).toMsg());
			dStage.setStageId(humanObj.stageObj.stageId);
			dStage.setMapSn(humanObj.stageObj.mapSn);
			dStage.setInstSn(humanObj.stageObj.stageSn);
			dStage.setLineNum(humanObj.stageObj.lineNum);
			return dStage.build();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 切换地图,尽量用此处方法，这里会加延迟切换并且改变玩家状态
	 * @param humanObj
	 */
	public void _msg_CSStageSwitch(final HumanObject humanObj, final int mapSn, long stageId) {
		ConfMap confMapTo = ConfMap.get(mapSn);
		if (confMapTo == null) {
			Log.stageCommon.info("StageMsgHandler.onCSStageSwitch : error goto mapSn={}", mapSn);
			// 发送文字提示消息 切换地图错误！
			humanObj.sendSysMsg(36);
			return;
		}

		// 出生点及朝向
		Vector2D pos = StageManager.inst().getHumanPos(mapSn);
		Vector2D dir = MathUtils.getDir(pos, StageManager.inst().getHumanDir(mapSn));
		StageObject stageObj = humanObj.stageObj;
		if (stageObj.mapSn == mapSn) {// 同个地图内传送
			// 将玩家拉到当前地图上的某个位置
			StageManager.inst().pullTo(humanObj, pos, dir);

		} else if (confMapTo.type.equals(EMapType.common.name())) {
			// 只能切换到普通地图，副本地图要用进入副本命令"/goRep stageSn"
//			if (stageObj.confMap.type.equals(EMapType.rep.name())) {
//				// 从副本地图切换到普通地图，需要获取进入副本前保存的坐标
//				pos = HumanManager.inst().stageHistoryCommon(humanObj);
//			}
			StageManager.inst().switchTo(humanObj, mapSn, pos, dir);
		} else {
			StageManager.inst().switchTo(humanObj, stageId, pos, dir);
		}
	}
	
	/**
	 * 进入场景最后一步：客户端发送进入场景
	 * @param humanObj
	 */
	public void _msg_CSStageEnter(HumanObject humanObj) {
		StageObject stageObj = humanObj.stageObj;
		stageObj.start();// 开启场景副本，有人进入就开启

		Event.fireEx(EventKey.StageHumanEnterBefore, stageObj.mapSn, "humanObj", humanObj);

		// 通知本人 本区域的地图单元信息
		SCStageEnterResult.Builder msgER = SCStageEnterResult.newBuilder();
		List<WorldObject> list = StageManager.inst().getWorldObjsInArea(humanObj.stageObj, humanObj.posNow);
		for (WorldObject o : list) {
			if (!o.isInWorld())
				continue;
			if (o.equals(humanObj))
				continue;
			if (o.id == humanObj.id) {
				Log.temp.info("==SCStageObjectAppear o.id={}, humanObj.id={}", o.id, humanObj.id);
				continue;
			}

			DStageObject.Builder msgObj = o.createMsg();
			msgER.addObj(msgObj);
			//System.out.println("_msg_CSStageEnter name="+msgObj.getName());
		}
		humanObj.sendMsg(msgER);

		// //死亡的时候不发stageEnter
		// if(humanObj.isDie()) {
		// Log.human.error("===死亡还进入场景？？？onCSStageEnter humanObj isDie");
		// }
						
		humanObj.stageShow();

		// 玩家登录到地图中的事件（切换地图是不会触发）
		if (humanObj.loginStageState == 1) {
			Event.fire(EventKey.HumanLoginFinish, "humanObj", humanObj);
			humanObj.loginStageState = 0;
		} else if (humanObj.loginStageState == 2) {
			
			Event.fire(EventKey.HumanLoginFinishFirstToday, "humanObj", humanObj);
			Event.fire(EventKey.HumanLoginFinish, "humanObj", humanObj);
			humanObj.loginStageState = 0;
		}

		// 发送进入地图事件
		Event.fireEx(EventKey.StageHumanEnter, stageObj.mapSn, "humanObj", humanObj);

		// 发送场景单元完成的消息
		stageObj.getPort().addQueue(new PortPulseQueue("humanObj", humanObj) {
			@Override
			public void execute(Port port) {
				HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
				if(null == humanObj){
					Log.game.error("_msg_CSStageEnter humanObj is null");
					return;
				}
				SCStageEnterEnd.Builder msg = SCStageEnterEnd.newBuilder();
				if (null != stageObj.randUtils) {
					for (int seek : stageObj.randUtils.seek) {
						msg.addSeek(seek);// 随机种子
					}
				}
				StageManager.inst().sendMsgToArea(msg, humanObj.stageObj, humanObj.posNow);
				
			}
		});
	}

	/**
	 * 全角度碰撞 判断obj 在地图中可能会发生所有碰撞角度
	 * @param obj
	 * @param dir
	 * @param angleArr
	 * @return
	 */
	public boolean stageCollisionDetectAll(UnitObject obj, Vector2D dir, List<Double> angleArr) {
		boolean result = false;
		if (obj.stageObj == null)
			return result;

		if (angleArr != null) {
			angleArr.clear();
		}
		for (UnitObject unitObj : obj.stageObj.getUnitObjs().values()) {
			if (!unitObj.isDie() && unitObj.isInWorld()) {
				if (unitObj.id == obj.id) {
					continue;
				}
				if (MathUtils.collisionDetectAll(obj.posNow, dir, unitObj.posNow, 0 
						/*obj.confModel.collisionRadius + unitObj.confModel.collisionRadius*/, angleArr)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 全角度碰撞 判断obj 是否产生碰撞
	 * @param obj
	 * @param dir
	 * @return
	 */
	public boolean stageCollisionDetect(UnitObject obj, Vector2D dir) {
		boolean result = false;
		if (obj.stageObj == null)
			return result;

		for (UnitObject unitObj : obj.stageObj.getUnitObjs().values()) {
			if (!unitObj.isDie() && unitObj.isInWorld()) {
				if (unitObj.id == obj.id) {
					continue;
				}
				if (MathUtils.collisionDetect(obj.posNow, dir, unitObj.posNow, 0
						/*obj.confModel.collisionRadius	+ unitObj.confModel.collisionRadius*/, null)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 获得坐标的Z
	 * @param stageSn
	 * @param vec
	 * @return
	 */
	public static boolean getHeight(int stageSn, Vector3D vec) {
		// boolean result = PathFinding.posHeight(stageSn, vec);
		boolean result = HeightFinding.posHeight(stageSn, vec);
		return result;
	}

	/**
	 * 获得坐标的Z
	 * @param stageSn
	 * @param vec
	 * @return
	 */
	public static double getHeightY(int stageSn, Vector2D vec) {
		// Vector3D result = PathFinding.posHeight(stageSn, vec);
		Vector3D result = HeightFinding.posHeight(stageSn, vec);
		return result.y;
	}

	/**
	 * 获得坐标的Z
	 * @param stageSn
	 * @return
	 */
	public static Vector3D getHeight(int stageSn, Vector2D pos) {
		// Vector3D result = PathFinding.posHeight(stageSn, pos);
		Vector3D result = HeightFinding.posHeight(stageSn, pos);
		if (result.z == HeightFinding.NO_HEIGHT_INFO) {
			// Log.human.debug("===PathFinding.posHeight stageSn={},pos={}",
			// stageSn, pos);
			// result = PathFinding.posHeight(stageSn, pos);//lock by
			// sjh,在linux下有时候出错，就导致服务器挂了，很危险所以屏蔽掉
		}
		return result;
	}

	/**
	 * 如果下一个目标点不能直接到达，那么取起点和终点直线上从起点能到达的最远的点
	 * @param mapSn
	 * @return
	 */
	public static Vector2D getRaycastDis(int mapSn, Vector2D posBegin, Vector2D posEnd, int flag) {
		Vector3D endReal = PathFinding.raycast(mapSn, getHeight(mapSn, posBegin), getHeight(mapSn, posEnd), flag);
		return endReal.toVector2D();
	}

	/**
	 * 获取我方出生点（默认获取第一个点）
	 * @param mapSn 地图SN
	 * @return
	 */
	public Vector2D getHumanPos(int mapSn) {
		return getHumanPos(mapSn, 1);
	}

	/**
	 * 获取我方出生点
	 * @param mapSn 地图SN
	 * @param index 第几个，从1开始
	 * @return
	 */
	public Vector2D getHumanPos(int mapSn, int index) {
		Vector2D v2DPos = new Vector2D();
		double radius = 0;
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn={}", mapSn);
		} else {
            if (confMap.posID > 0) {
                ConfMapPosition confMapPos = ConfMapPosition.get(confMap.posID);
                if (confMapPos == null || confMapPos.humanPos == null) {
                    Log.table.error("ConfMapPosition配表错误，no find sn={} or humanPos==null", confMap.posID);
                } else if (index <= 0) {
                    Log.table.error("ConfMapPosition 配表有误，sn={} humanPos.length={}, index={}", confMapPos.sn,
                            confMapPos.humanPos.length, index);
                } else {
                    if (index > confMapPos.humanPos.length) {
                        index = confMapPos.humanPos.length;// 没配那么多个，那就用最后一个
                    }
                    String[] strPos = Utils.strToStrArraySplit(confMapPos.humanPos[index - 1]);
                    if (strPos != null && strPos.length >= 3) {
                        v2DPos.x = Utils.doubleValue(strPos[0]);
                        v2DPos.y = Utils.doubleValue(strPos[2]);
                    }
                    radius = confMapPos.humanRadius;
                }
            }
		}

		if (radius > 0)
			return StageBattleManager.inst().randomPosInCircle(v2DPos, 0, radius);
		else
			return v2DPos;
	}

	/**
	 * 获取我方朝向（默认获取第一个点）
	 * @param mapSn 地图SN
	 * @return
	 */
	public Vector2D getHumanDir(int mapSn) {
		return getHumanDir(mapSn, 1);
	}

	/**
	 * 获取我方朝向
	 * @param mapSn 地图SN
	 * @param index 第几个
	 * @return
	 */
	public Vector2D getHumanDir(int mapSn, int index) {
		Vector2D v2DPos = new Vector2D();
		if (index <= 0) {
			Log.table.error("error in getHumanDir(),index={}", index);
			return v2DPos;
		}

		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn ={}", mapSn);
		} else {
		    if (confMap.posID > 0) {
                ConfMapPosition confMapPos = ConfMapPosition.get(confMap.posID);
                if (confMapPos == null) {
                    Log.table.error("ConfMapPosition配表错误，no find sn={}", confMap.posID);
                } else if (confMapPos.humanDir == null) {
                    Log.table.error("ConfMapPosition配表错误，sn={},humanDir==null", confMap.posID);
                } else {
                    if (index > confMapPos.humanDir.length) {
                        index = confMapPos.humanDir.length;// 没配那么多个，那就用最后一个
                    }
                    String[] strPos = Utils.strToStrArraySplit(confMapPos.humanDir[index - 1]);
                    if (strPos != null && strPos.length >= 3) {
                        v2DPos.x = Utils.doubleValue(strPos[0]);
                        v2DPos.y = Utils.doubleValue(strPos[2]);
                    }
                }
            }
		}
		return v2DPos;
	}

	/**
	 * 获取我方的宠物出生点
	 * @param mapSn 地图SN
	 * @param index 第几只宠物，从1开始
	 */
	public Vector2D getHumanPetPos(int mapSn, int index) {
		Vector2D v2DPos = new Vector2D();
		double radius = 0;
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn={}", mapSn);
		} else {
			ConfMapPosition confMapPos = ConfMapPosition.get(confMap.posID);
			if (confMapPos == null || confMapPos.humanPetPos == null) {
				Log.table.error("ConfMapPosition配表错误，no find sn={} or humanPetPos==null", confMap.posID);
			} else if (index <= 0) {
				Log.table.error("ConfMapPosition 配表有误，sn={} humanPetPos.length={}, index={}", confMapPos.sn,
						confMapPos.humanPetPos.length, index);
			} else {
				if (index > confMapPos.humanPetPos.length) {
					index = confMapPos.humanPetPos.length;// 没配那么多个，那就用最后一个
				}
				String[] strPetPos = Utils.strToStrArraySplit(confMapPos.humanPetPos[index - 1]);
				if (strPetPos != null && strPetPos.length >= 3) {
					v2DPos.x = Utils.doubleValue(strPetPos[0]);
					v2DPos.y = Utils.doubleValue(strPetPos[2]);
				}
				radius = confMapPos.humanRadius;
			}
		}

		if (radius > 0)
			return StageBattleManager.inst().randomPosInCircle(v2DPos, 0, radius);
		else
			return v2DPos;
	}

	/**
	 * 获取敌方的宠物出生点（竞技场或PVP地图会用到）
	 * @param mapSn 地图SN
	 * @param index 第几只宠物，从1开始
	 */
	public Vector2D getEnemyPetPos(int mapSn, int index) {
		Vector2D v2DPos = new Vector2D();
		double radius = 0;
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn={}", mapSn);
		} else {
			ConfMapPosition confMapPos = ConfMapPosition.get(confMap.posID);
			if (confMapPos == null || confMapPos.enemyPetPos == null) {
				Log.table.error("ConfMapPosition配表错误，no find sn={} or enemyPetPos==null", confMap.posID);
			} else if (index <= 0) {
				Log.table.error("ConfMapPosition 配表有误，sn={} enemyPetPos.length={}, index={}", confMapPos.sn,
						confMapPos.enemyPetPos.length, index);
			} else {
				if (index > confMapPos.enemyPetPos.length) {
					index = confMapPos.enemyPetPos.length;// 没配那么多个，那就用最后一个
				}
				String[] strPetPos = Utils.strToStrArraySplit(confMapPos.enemyPetPos[index - 1]);
				if (strPetPos != null && strPetPos.length >= 3) {
					v2DPos.x = Utils.doubleValue(strPetPos[0]);
					v2DPos.y = Utils.doubleValue(strPetPos[2]);
				}
				radius = confMapPos.enemyRadius;
			}
		}

		if (radius > 0)
			return StageBattleManager.inst().randomPosInCircle(v2DPos, 0, radius);
		else
			return v2DPos;
	}

	/**
	 * 获取敌方出生点（默认获取第一个点）
	 * @param mapSn 地图SN
	 * @return
	 */
	public Vector2D getEnemyPos(int mapSn) {
		return getEnemyPos(mapSn, 1);
	}

	/**
	 * 获取敌方出生点
	 * @param mapSn 地图SN
	 * @param index 第几个，从1开始
	 * @return
	 */
	public Vector2D getEnemyPos(int mapSn, int index) {
		Vector2D v2DPos = new Vector2D();
		double radius = 0;
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn ={}", mapSn);
		} else {
            if (confMap.posID > 0) {
                ConfMapPosition confMapPos = ConfMapPosition.get(confMap.posID);
                if (confMapPos == null) {
                    Log.table.error("ConfMapPosition配表错误，no find sn={}", confMap.posID);
                } else if (confMapPos.enemyPos != null) { // 敌方位置可不填的
                    if (index <= 0) {
                        Log.table.error("ConfMapPosition 配表有误，sn={} enemyPos.length={}, index={}", confMapPos.sn,
                                confMapPos.enemyPos.length, index);
                    } else {
                        if (index > confMapPos.enemyPos.length) {
                            index = confMapPos.enemyPos.length;// 没配那么多个，那就用最后一个
                        }
                        String[] strPos = Utils.strToStrArraySplit(confMapPos.enemyPos[index - 1]);
                        if (strPos != null && strPos.length >= 3) {
                            v2DPos.x = Utils.doubleValue(strPos[0]);
                            v2DPos.y = Utils.doubleValue(strPos[2]);
                            radius = confMapPos.enemyRadius;
                        }
                    }
                }
            }
		}

		if (radius > 0)
			return StageBattleManager.inst().randomPosInCircle(v2DPos, 0, radius);
		else
			return v2DPos;
	}

	/**
	 * 获取敌方朝向（默认获取第一个点）
	 * @param mapSn 地图SN
	 * @return
	 */
	public Vector2D getEnemyDir(int mapSn) {
		return getEnemyDir(mapSn, 1);
	}

	/**
	 * 获取敌方朝向
	 * @param mapSn 地图SN
	 * @param index 第几个
	 * @return
	 */
	public Vector2D getEnemyDir(int mapSn, int index) {
		Vector2D v2DPos = new Vector2D();
		if (index <= 0) {
			Log.table.error("error in getEnemyDir(),index={}", index);
			return v2DPos;
		}

		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn ={}", mapSn);
		} else {
			ConfMapPosition confMapPos = ConfMapPosition.get(confMap.posID);
			if (confMapPos == null) {
				Log.table.error("ConfMapPosition配表错误，no find sn={}", confMap.posID);
			} else if (confMapPos.enemyDir == null) {
				Log.table.error("ConfMapPosition配表错误，sn={},enemyDir==null", confMap.posID);
			} else {
				if (index > confMapPos.enemyDir.length) {
					index = confMapPos.enemyDir.length;// 没配那么多个，那就用最后一个
				}
				String[] strPos = Utils.strToStrArraySplit(confMapPos.enemyDir[index - 1]);
				if (strPos != null && strPos.length >= 3) {
					v2DPos.x = Utils.doubleValue(strPos[0]);
					v2DPos.y = Utils.doubleValue(strPos[2]);
				}
			}
		}
		return v2DPos;
	}

	//主城假人///////////////////////////////////////////////////////////////////////////////
//	/**
//	 * 下发所有的假人信息
//	 */
//	public void sendAllDummy(HumanObject humanObj) {
//		// LinkedList<RankCombat> listRankCombat =
//		// RankManager.inst().getRankCombat();
//		// LinkedList<RankLevel> listRankLevel =
//		// RankManager.inst().getRankLevel();
//		// LinkedList<RankVip> listRankVip = RankManager.inst().getRankVip();
//		//
//		// int numSingle = (MaxDummy + 1) / 3;
//		// // 加入战力最高几个
//		// int num = 0;
//		// for (RankCombat rankCombat : listRankCombat) {
//		// if (rankCombat.getId() == humanObj.getHuman().getId()) {
//		// continue;
//		// }
//		// if (num < numSingle) {
//		// if (!listHumanIds.contains(rankCombat.getId())) {
//		// listHumanIds.add(rankCombat.getId());
//		// num++;
//		// }
//		// } else if (!listReserveIds.contains(rankCombat.getId())) {//
//		// 其他人保存到备用列表里
//		// listReserveIds.add(rankCombat.getId());
//		// }
//		// }
//		// // 加入等级最高几个
//		// num = 0;
//		// for (RankLevel rankLevel : listRankLevel) {
//		// if (rankLevel.getId() == humanObj.getHuman().getId()) {
//		// continue;
//		// }
//		// if (num < numSingle) {
//		// if (!listHumanIds.contains(rankLevel.getId())) {
//		// listHumanIds.add(rankLevel.getId());
//		// num++;
//		// }
//		// } else if (!listReserveIds.contains(rankLevel.getId())) {//
//		// 其他人保存到备用列表里
//		// listReserveIds.add(rankLevel.getId());
//		// }
//		// }
//		// // 加入VIP最高几个
//		// num = 0;
//		// for (RankVip rankVip : listRankVip) {
//		// if (rankVip.getId() == humanObj.getHuman().getId()) {
//		// continue;
//		// }
//		// if (num < numSingle && listHumanIds.size() < MaxDummy) {
//		// if (!listHumanIds.contains(rankVip.getId())) {
//		// listHumanIds.add(rankVip.getId());
//		// num++;
//		// }
//		// } else if (!listReserveIds.contains(rankVip.getId())) {// 其他人保存到备用列表里
//		// listReserveIds.add(rankVip.getId());
//		// }
//		// }
//
//		sendAddDummy(humanObj, listHumanIds);// 下发增加的假人信息
//	}
//
//	/**
//	 * 下发增加的假人信息
//	 */
//	public void sendAddDummy(HumanObject humanObj, List<Long> listIds) {
//		List<Long> listSearchIds = new ArrayList<Long>();// 需要查询的玩家信息
//		List<Human> listHumans = new ArrayList<Human>();// 已查询过的玩家信息
//		for (Long id : listIds) {
//			if (mapDBHuman.containsKey(id)) {// 已查询过的
//				listHumans.add(mapDBHuman.get(id));
//			} else {// 没有查询过，需要进行查询数据库
//				listSearchIds.add(id);
//			}
//		}
//
//		// 查询玩家数据
//		String whereSql = "WHERE ";
//		int num = 0;
//		for (Long id : listSearchIds) {
//			whereSql += "`id=`" + id;
//			num++;
//			if (num < listSearchIds.size()) {// 非最后一个要加" or "
//				whereSql += " or ";
//			}
//		}
//		// 查询的列
//		/*
//		 * List<String> listColumn = new ArrayList<String>();
//		 * listColumn.add("Name");
//		 */
//		// 开始查询
//		DB dbPrx = DB.newInstance(Human.tableName);
//		dbPrx.findByQuery(false, whereSql);
//		dbPrx.listenResult(this::_result_queryHumanList, "humanObj", humanObj, "listHumans", listHumans);
//	}
//	private void _result_queryHumanList(Param results, Param context) {
//		List<Record> listRecord = results.get();
//		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
//		List<Human> listHumans = Utils.getParamValue(context, "listHumans", null);
//		if (listRecord == null || humanObj == null || listHumans == null) {
//			Log.game.error("===_result_queryHumanList listRecord={}, humanObj={}, listHumans={}", listRecord, humanObj,
//					listHumans);
//			return;
//		}
//
//		SCAllDummy.Builder msg = SCAllDummy.newBuilder();
//		for (Record r : listRecord) {
//			Human human = new Human(r);
//			long humanId = human.getId();
//			if (!mapDBHuman.containsKey(humanId)) {
//				mapDBHuman.put(humanId, human);
//			}
//			msg.addDummy(createDDummy(human));
//		}
//		// 加入已查询过的
//		for (Human human : listHumans) {
//			if (human != null) {
//				msg.addDummy(createDDummy(human));
//			}
//		}
//		humanObj.sendMsg(msg);
//	}
//
//	private DDummy createDDummy(Human human) {
//		DDummy.Builder dDummy = DDummy.newBuilder();
//		if (human != null) {
//			dDummy.setHumanId(human.getId());
//			dDummy.setModelSn(human.getModelSn());
//			dDummy.setName(human.getName());
//			dDummy.setSex(human.getSex());
//			dDummy.setProfession(human.getProfession());
//			dDummy.setFashionShow(human.isFashionShow());
//			dDummy.setFashionWeaponSn(human.getFashionWeaponSn());
//			dDummy.setFashionClothesSn(human.getFashionClothesSn());
//			dDummy.setEquipWeaponSn(human.getEquipWeaponSn());
//			dDummy.setEquipWeaponStrthLv(human.getEquipWeaponStrthLv());
//			dDummy.setEquipClothesSn(human.getEquipClothesSn());
//			dDummy.setEquipClothesStrthLv(human.getEquipClothesStrthLv());
//		}
//		return dDummy.build();
//	}
//
//	/**
//	 * 下发假人下线
//	 */
//	public void sendOfflineDummy(long humanId) {
//		// SCOfflineDummy.Builder msg = SCOfflineDummy.newBuilder();
//	}
//
//	/**
//	 * 请求删除假人，自动下发增加假人
//	 */
//	public void _msg_CSDelDummy(HumanObject humanObj, long humanId) {
//		if (listHumanIds.contains(humanId)) {// 假人消失，加入备用列表里
//			listHumanIds.remove(humanId);
//			if (!listReserveIds.contains(humanId)) {
//				listReserveIds.add(humanId);
//			}
//		}
//
//		List<Long> listIds = new ArrayList<Long>();
//		if (!listReserveIds.isEmpty()) {// 随机加入1到多个
//			int randMax = MaxDummy - listHumanIds.size();
//			if (randMax > 0) {
//				int rand = RandomUtils.nextInt(randMax);// 加入rand个假人
//				for (int i = 0; i < rand && !listReserveIds.isEmpty(); i++) {
//					listIds.add(listReserveIds.get(0));
//					listReserveIds.remove(0);
//				}
//			}
//		} else {// 备用列表无数据，怎么办？
//		}
//
//		sendAddDummy(humanObj, listIds);// 下发增加的假人信息
//	}

}
