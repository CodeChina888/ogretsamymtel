package game.worldsrv.instLootMap.Game.Event;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import game.msg.Define.DLootMapEvent;
import game.msg.Define.ELootMapEventType;
import game.msg.Define.ELootMapType;
import game.worldsrv.config.ConfLootMapEvent;
import game.worldsrv.config.ConfLootMapLevelObject;
import game.worldsrv.enumType.LootMapType;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Log;

public class InstLootMapEventLevel extends InstLootMapEvent{
	
	public int levelObjConfSn;
	protected int eggProbability; // 触发概率
	protected int eggDoorEventSn;
	
	public int[] rewardItemSnList;
	public int[] rewardItemNumList;
	
	public InstLootMapEventLevel(int eventSn,int mapLevel,int humanLevel,ELootMapType mapType){
		super(eventSn, mapLevel, humanLevel,mapType);
	}
	
	@Override
	public void init(int eventSn,int mapLevel,int humanLevel){
		super.init(eventSn,mapLevel,humanLevel);
		//获取
		ConfLootMapEvent eventConf = ConfLootMapEvent.get(eventSn);
		if(eventConf == null){
			Log.lootMap.error("===InstLootMapEventLevel ConfLootMapEvent.get is null, eventSn = {}",eventSn);
			return;
		}
		//是否需要层级跟等级变化
		if(LootMapType.isLevelEvent(InstLootMapEvent.getEventType(eventSn))){
			//公式获取sn
			this.levelObjConfSn = ConfigKeyFormula.getLootMapLevelObjectConfSn(eventSn,mapLevel,humanLevel);
			//取得配置
			ConfLootMapLevelObject levelObjConf = ConfLootMapLevelObject.get(levelObjConfSn);
			if(levelObjConf == null){
				
				Log.lootMap.error("===InstLootMapEventLevel ConfLootMapLevelObject.get is null, levelObjConfSn = {} eventSn = {}",levelObjConfSn,eventSn);
				return;
			}
			onInit(levelObjConf);//ConfLootMapLevelObject
		}
	}

	
	
	//详细数据
	protected void onInit(ConfLootMapLevelObject conf){
		if(conf==null){
			return;
		}
		
		this.eggProbability = conf.eggProbability;
		this.eggDoorEventSn = conf.eggEventSn;
		
		//是否会有奖励内容
		if(LootMapType.isRewardEvent(getEventType())){
			rewardItemSnList = conf.rewardItemSn;
			rewardItemNumList = conf.rewardItemNumber;
		}
	}
	
	/**
	 * 返回 0 不触发
	 * @return
	 */
	//当物件消失时候判断是否触发彩蛋关
	public boolean isTriggerEggDoor(){
		if(eggDoorEventSn == 0 || eggProbability == 0){
			return false;
		}
		//多人本不触发
		if(mapType == ELootMapType.LootMapMultip){
			return false;
		}
		//如果不是可以触发的对象<理论上不随强度变化而变化的对象不是触发对象>
		if(LootMapType.isLevelEvent(getEventType())==false){
			return false;
		}
		//菜单关卡为0不触发随机
		return Math.random()*1000 <= eggProbability;
	}
	
	public int getEggDoorEventSn(){
		return eggDoorEventSn;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		out.write(levelObjConfSn);
		out.write(eggProbability);
		out.write(eggDoorEventSn);
		out.write(rewardItemSnList);
		out.write(rewardItemNumList);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		levelObjConfSn = in.read();
		eggProbability = in.read();
		eggDoorEventSn = in.read();
		rewardItemSnList = in.read();
		rewardItemNumList = in.read();
	}
	
	@Override
	protected void initDLootMapEvent(DLootMapEvent.Builder eventMsg){
		super.initDLootMapEvent(eventMsg);
		eventMsg.setEventObjSn(levelObjConfSn);
	}
}
