package game.worldsrv.human;

import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import game.worldsrv.character.HumanObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * 玩家操作冷却管理
 */
public class HumanOperateCDManager implements ISerilizable {
	public Map<Integer, Long> cdMap = new HashMap<>();
	public static  long LOCK_COMMON_INTERVAL = 10000;	  		// 预设锁间隔
	// 锁类型
	public static final int Oper_Type_Activity = 0x0001;		// 活动领奖
	public static final int Oper_Type_GuildCreate = 0x0002;		// 公会创建
	public static final int Oper_Type_GuildPray = 0x0003;		// 公会祈福
	
	public HumanOperateCDManager() {
		
	}
	
	/**
	 * 加锁
	 * @param humanObj
	 * @param event
	 * @return true 可以继续操作 false 正在操作冷却中，后续代码不再执行
	 */
	public boolean lock(HumanObject humanObj, int event){
		long lastTick = 0;
		if(cdMap.containsKey(event)){
			lastTick = cdMap.get(event);
		}
		long curTick = Port.getTime();
		//冷却时间未结束
		if(lastTick!=0 && curTick < lastTick + LOCK_COMMON_INTERVAL){
			humanObj.sendSysMsg(1011);// 操作过于频繁，请稍后再试
			return false;
		}
		cdMap.put(event, curTick);
		return true;
	}
	/**
	 * 解锁，事件操作结束，重置冷却
	 * @param humanObj
	 * @param event
	 */
	public void unlock(HumanObject humanObj, int event){
		if(!cdMap.containsKey(event)) return;
		cdMap.put(event, 0L);
		return;
	}
	
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(cdMap);
	}
	@Override
	public void readFrom(InputStream in) throws IOException { 
		cdMap = in.read();
	}
}
