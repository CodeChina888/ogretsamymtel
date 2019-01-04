package game.worldsrv.stage;

import game.worldsrv.support.Vector2D;
import game.turnbasedsrv.combat.CombatManager;
import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.SwitchState;

import java.util.Map;

import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.ConnectionStatus;
import core.support.SysException;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;

@DistrClass(importClass = {HumanObject.class, Vector2D.class, Map.class, 
		Message.class, ConnectionStatus.class, GeneratedMessage.class})
public class StageObjectService extends Service {
	protected final StageObject stageObj;

	/**
	 * 初始化数据
	 * @return
	 */
	protected void init() {

	}

	/**
	 * 启动服务
	 */
	public void startupLocal() {
		this.startup();
		init();
	}

	/**
	 * 构造函数
	 * @param stageObj
	 */
	public StageObjectService(StageObject stageObj, Port port) {
		super(port);
		this.stageObj = stageObj;
	}

	@Override
	public Object getId() {
		return stageObj.stageId;
	}

	@Override
	public void pulseOverride() {
		stageObj.pulse();
	}

	public StageObject getStageObj() {
		return stageObj;
	}
	
	@DistrMethod
	public void clearError(long curr, long interval) {
		if(curr - this.stageObj.createTime  > interval) {
			if(this.stageObj.getHumanObjs().isEmpty()) {
				this.stageObj.destory();
			}
		}
	}

	@DistrMethod
	public void login(long humanId, CallPoint connPoint, ConnectionStatus connStatus, long stageId, Vector2D stagePos) {
		stageObj.login(humanId, connPoint, connStatus, stageId, stagePos);
	}

	@DistrMethod
	public void register(HumanObject humanObj) {
		//humanObj.stageRegister(stageObj);// 将玩家注入进地图
		//Port.getCurrent().returns("posNow", humanObj.posNow, "stageSn", stageObj.stageSn);
		
		Human human = humanObj.getHuman();
		
		//如果玩家已经在了，就直接返回信息
		if (stageObj.getHumanObj(humanObj.id) != null) {
			int count = 0;
			if (stageObj.getHumanObjs() != null) {
				count = stageObj.getHumanObjs().entrySet().size();
			}
			Port.getCurrent().returns("success", true, "posNow", humanObj.posNow, "dirNow", humanObj.dirNow, 
					"stageSn", stageObj.stageSn, "count", count);
			return;
		}
		
		try {
			//将玩家注入进地图
			humanObj.stageRegister(stageObj);
			humanObj.switchState = SwitchState.InStage;
			//返回信息
			int count = 0;
			if (stageObj.getHumanObjs() != null) {
				count = stageObj.getHumanObjs().entrySet().size();
			}
			Port.getCurrent().returns("success", true, "posNow", humanObj.posNow, "dirNow", humanObj.dirNow, 
					"stageSn", stageObj.stageSn, "count", count);
			
		} catch(Exception e) {
			Port.getCurrent().returns("success", false);
			Log.stageCommon.error("注册地图场景发生错误，玩家名字 {}，场景SN {}", human.getName(), stageObj.mapSn);
			
			throw new SysException(e);
		}
	}

	/**
	 * 每日零/五时重置玩家数据
	 */
	// @ScheduleMethod({DataResetService.CRON_DAY_ZERO,
	// DataResetService.CRON_DAY_FIVE})
	// public void schdDayReset() {
	// //当前时间
	// long timeNow = Port.getTime();
	// long timeZero = Utils.getTimeBeginOfToday(timeNow);
	//
	// //遍历本地图下的玩家
	// Collection<HumanObject> humans = stageObj.getHumanObjs().values();
	// for(HumanObject ho : humans) {
	// Human h = ho.getHuman();
	//
	// //上次最后登录时间
	// long timeLast = h.getTimeLogin();
	//
	// //更新玩家登录时间
	// h.setTimeLogin(timeNow);
	//
	// //防止出现切换地图多次更新
	// if(timeLast < timeZero && timeZero <= timeNow) {
	// Event.fire(EventKey.HUMAN_RESET_ZERO, "humanObj", ho, "timeLoginLast",
	// timeLast);
	// }
	//
	// //发送本日五时首次登录事件
	// long timeFive = timeZero + 5 * Time.HOUR;
	// if(timeLast < timeFive && timeFive <= timeNow) {
	// Event.fire(EventKey.HUMAN_RESET_FIVE, "humanObj", ho, "timeLoginLast",
	// timeLast);
	// }
	// }
	// }

	/**
	 * 每个service预留空方法
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {
		
	}
	
	/**
	 * 转发新手战斗消息
	 * @param stageId
	 */
	@DistrMethod
	public void dispatchCombatMsg(long stageId, long connId, CallPoint connPoint, int msgId, GeneratedMessage msg) {
		CombatManager.inst().dispatchCombatMsg(stageObj,stageId,connId,connPoint,msgId,msg);
	}
}
