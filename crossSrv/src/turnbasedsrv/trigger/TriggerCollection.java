package turnbasedsrv.trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import crosssrv.support.Log;
import turnbasedsrv.enumType.TriggerPoint;
import turnbasedsrv.fightObj.FightObject;
import turnbasedsrv.stage.FightStageObject;

public class TriggerCollection {
	/** 触发器队列 **/
	List<Trigger> triggerList = new ArrayList<>();
	/** 新一轮的触发器队列 **/
	List<Trigger> queueTriggerList = new ArrayList<>();
	/** 当前的监听队列 **/
	Map<TriggerPoint, List<TriggerListen>> mapListenList = new HashMap<>();
	/** 新一轮的监听队列 **/
	Map<TriggerPoint, List<TriggerListen>> queueMapListenList = new HashMap<>();
	/** 正在执行的监听队列 **/
	List<TriggerListen> excuteListenList = new ArrayList<>();
	/** 正在触发的触发器 **/
	public Trigger nowTrigger = null;
	/** 地图 **/
	public FightStageObject stageObj;

	/**
	 * 构造函数
	 */
	public TriggerCollection(FightStageObject stageObj) {
		this.stageObj = stageObj;
	}

	/**
	 * 是否有触发器等待触发
	 * 
	 * @return
	 */
	public boolean hasTrigger() {
		return queueTriggerList.size() > 0;
	}

	/**
	 * 增加触发器
	 * 
	 * @param trigger
	 * @return
	 */
	public boolean addTrigger(Trigger trigger) {
		if (queueTriggerList.contains(trigger)) {
			return false;
		}
		queueTriggerList.add(trigger);
		return true;
	}

	/**
	 * 执行触发器
	 */
	public void doTrigger() {
		int trigger_count = 0;
		while (queueTriggerList.size() > 0) {
			trigger_count = trigger_count + 1;
			excute();
			if (trigger_count > 1000) {
				Log.fight.error("触发器循环次数超1000次");
			}
		}
	}

	/**
	 * 执行触发
	 */
	private void excute() {
		// 移动触发器队列
		triggerList.addAll(queueTriggerList);
		queueTriggerList.clear();

		// 移动监听队列
		for (Map.Entry<TriggerPoint, List<TriggerListen>> entry : queueMapListenList.entrySet()) {
			TriggerPoint tp = entry.getKey();
			List<TriggerListen> listenList = mapListenList.get(tp);
			if (listenList == null) {
				mapListenList.put(tp, entry.getValue());
			} else {
				for (TriggerListen listen : entry.getValue()) {
					if (!listenList.contains(listen)) {
						listenList.add(listen);
					}
				}
			}
		}
		queueMapListenList.clear();

		// 执行触发器
		for (int i = 0; i < triggerList.size(); i++) {
			Trigger trigger = triggerList.get(i);
			this.nowTrigger = trigger;
			List<TriggerListen> listenList = mapListenList.get(trigger.triggerPoint);
			if (listenList == null) {
				continue;
			}
			excuteListenList.clear();
			if (trigger.triggerPoint.isCheckFightObj()) {
				FightObject fightObj = trigger.triggerParam.fightObj;
				if (fightObj == null || fightObj.isDie()) {
					continue;
				}
				for (int j = 0; j < listenList.size(); j++) {
					TriggerListen listen = listenList.get(j);
					if (listen.idFight != fightObj.idFight) {
						continue;
					}
					excuteListenList.add(listen);
				}
			} else {
				excuteListenList.addAll(listenList);
			}
			while (!excuteListenList.isEmpty()) {
				TriggerListen listen = excuteListenList.get(0);
				excuteListenList.remove(0);
				listen.excuteTrigger(trigger);
			}
		}
		this.nowTrigger = null;
		triggerList.clear();
	}

	/**
	 * 增加监听
	 * 
	 * @param listen
	 * @return
	 */
	public boolean addListen(TriggerListen listen) {
		List<TriggerListen> listenList = queueMapListenList.get(listen.triggerPoint);
		if (listenList != null && listenList.contains(listen)) {
			Log.fight.debug("增加重复的监听:{}", listen.triggerPoint.name());
			return false;
		}
		List<TriggerListen> listenList1 = mapListenList.get(listen.triggerPoint);
		if (listenList1 != null && listenList1.contains(listen)) {
			Log.fight.debug("增加重复的监听:{}", listen.triggerPoint.name());
			return false;
		}
		if (listenList == null) {
			listenList = new ArrayList<>();
			listenList.add(listen);
			queueMapListenList.put(listen.triggerPoint, listenList);
		} else {
			listenList.add(listen);
		}
		return true;
	}

	/**
	 * 删除监听
	 * 
	 * @param listen
	 * @return
	 */
	public void delListen(TriggerListen listen) {
		List<TriggerListen> listenList = queueMapListenList.get(listen.triggerPoint);
		if (listenList != null && listenList.contains(listen)) {
			listenList.remove(listen);
		}
		List<TriggerListen> listenList1 = mapListenList.get(listen.triggerPoint);
		if (listenList1 != null && listenList1.contains(listen)) {
			listenList1.remove(listen);
		}
		if (excuteListenList.contains(listen)) {
			excuteListenList.remove(listen);
		}
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}
}
