package game.worldsrv.instLootMap.Stage;

import java.util.List;

import game.msg.Define.DItem;
import game.msg.Define.ELootMapEventType;
import game.msg.Define.ELootMapMissionType;
import game.msg.Define.ELootMapType;
import game.msg.Define.EModeType;
import game.msg.MsgInstLootMap.SCLootMapMission;
import game.msg.MsgInstLootMap.SCLootMapMissionComplete;
import game.msg.MsgInstLootMap.SCLootMapSingleEnd;
import game.worldsrv.config.ConfLootMap;
import game.worldsrv.instLootMap.InstLootMapManager;
import game.worldsrv.instLootMap.Game.InstLootMapHumanData;
import game.worldsrv.instLootMap.Game.InstLootMapMission;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEvent;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEventLevel;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEventMonster;
import game.worldsrv.param.ParamManager;
import game.worldsrv.raffle.RaffleManager;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.Log;

public class StageObjectLootMapSingle extends StageObjectLootMap{

	//任务系统
	InstLootMapMission mission = new InstLootMapMission();
	private boolean rmvLock = false;
	private int eggLevelCurTime = 0;
	
	public StageObjectLootMapSingle(StagePort port, long stageId, int stageSn, int mapSn, int fightType, 
			int humanNumber, int lvType, int lootMapSn)  {
		super(port, stageId, stageSn, mapSn, fightType, humanNumber, lvType, lootMapSn);
		
		lootMapType = ELootMapType.LootMapSingle;
		int[] size = ParamManager.lootMapSingleSize;
		this.lootMapWidth = size[0];
		this.lootMapHeight = size[1];
		mapInit(lootMapSn);
	}
	
	/**
	 * 对象发送任务
	 */
	@Override
	public void sendGameEnterLevelData(long humanId){
		super.sendGameEnterLevelData(humanId);
		sendUpdateMission(humanId);
	}
	
	@Override
	public void createMap() {
		super.createMap();
		mission.init(ConfLootMap.get(lootMapSn));
	}

	@Override
	public void openFloor(long humanId,int x,int y){
		super.openFloor(humanId,x,y);
		if(mission.isHasMission()==false){	//判断是否有任务
			return;
		}
		if(mission.type == ELootMapMissionType.LootMapMissionClearMask){ //判断是否为清除地砖
			if(isClearFoor()){ // 判断是否被清空
				mission.count = 1;
				completeMission(humanId); // 获取奖励
			}
		}
	}
	
	/**
	 * 判断是否是清空了遮罩
	 * @return
	 */
	private boolean isClearFoor(){
		if(floorInfo== null){
			Log.lootMap.error("   ========   isClearFoor floorInfo is null");
			return false;
		}
		for(int i = 0; i < floorInfo.length ;i++){ 
			for(int j = 0; j < floorInfo[i].length;j++){
				if(floorInfo[i][j] == 0){ //当有存在没打开的 == 0
					return false;
				}
			}
		}
		return true; //否则被清空
	}
	
	//触发任务
	@Override
	public void triggerEvent(long humanId,int eventId){
		if(rmvLock){
			return;
		}
		
		if(eventMap.containsKey(eventId) == false){
			return;
		}
			
		rmvLock = true;	
		//判断是否有任务 且 是事件任务
		if(mission.isHasMission() && mission.type == ELootMapMissionType.LootMapMissionEvent){
			if(mission.count < mission.completeCount){ // 未完成
				InstLootMapEvent instEvent = eventMap.get(eventId);
				if(instEvent instanceof InstLootMapEventLevel){
					InstLootMapEventLevel instLevelEvent = (InstLootMapEventLevel)instEvent;
					if(instLevelEvent.eventSn == mission.eventSn){ // 该事件为任务要求事件
						if(++mission.count >= mission.completeCount){
							completeMission(humanId);
						}else{
							sendUpdateMission(humanId);
						}
					}
				}
			}
		}else if(mission.isHasMission() && mission.type == ELootMapMissionType.LootMapMissionClearMonster){
			// 单人本的刷新次数为1 暂时不考虑刷新情况
			InstLootMapEvent instEvent = eventMap.get(eventId);
			if(instEvent instanceof InstLootMapEventMonster){ // 是怪物
				if(monsterMap.size() == 1){ // 只有一只
					mission.count = 1;
					completeMission(humanId); // 领取奖励
				}
			}
		}
		
		super.triggerEvent(humanId,eventId); // 调用父类方法
		rmvLock = false;
	}
	
	/**
	 * 任务完成
	 */
	private void completeMission(long humanId){
		int[] rewardSn = mission.rewardSn;
		int[] rewardNum = mission.rewardNum;
		getReward(humanId,rewardSn,rewardNum);
		
		sendUpdateMission(humanId);
		
		SCLootMapMissionComplete.Builder comBuilder = SCLootMapMissionComplete.newBuilder();
		comBuilder.addAllItemList(getRewardItemList(rewardSn,rewardNum));
		InstLootMapManager.inst().sendHuman(comBuilder,getHumanObj(humanId)); // 发送奖励
	}
	
	/**
	 * 更新任务
	 */
	private void sendUpdateMission(long humanId){
		SCLootMapMission.Builder misBuilder = SCLootMapMission.newBuilder();
		misBuilder.setMissionSn(mission.missionSn);
		misBuilder.setState(mission.getStateType());
		misBuilder.setProgress(mission.count);
		InstLootMapManager.inst().sendHuman(misBuilder,getHumanObj(humanId)); 	// 发送完成
	}
	
	
	
	@Override
	protected void onOneSecond(){
		super.onOneSecond();
		if(lootMapType == ELootMapType.LootMapEgg){
			onEggLevelOneSecond();
		}
	}
	
	private void onEggLevelOneSecond(){
		//当时间到了 移除所有 道具
		if(++eggLevelCurTime == ParamManager.lootMapEggLevelTime){
			
			refreshSet.clear();
			monsterMap.clear();
			randomDoorMap.clear();//清除刷新内容
			
			for(InstLootMapEvent mapEvent : getAllEvent()){
				if(mapEvent.getEventType() ==ELootMapEventType.LootMapEventNextDoor) continue;
				notifyLootMapEventDisenable(mapEvent.id);
				eventMap.remove(mapEvent);
			}
		}
	}
	
	@Override
	public void enterNextLevelDoor(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human==null){
			Log.lootMap.error(" ========= enterNextLevel human is null humanId = {}",humanId);
			return;
		}
		mapLevel++; // 层级 ++
		lootMapType = ELootMapType.LootMapSingle;
		enterSingleDoor(humanId);
		
//		RaffleManager.inst().send_openTurntable(getHumanObj(humanId),EModeType.ModeInstNormal,1);
		RaffleManager.inst().send_openTurntable(getHumanObj(humanId),EModeType.ModeLootMapSingle,getRaffTurntableLv());
	}
	
	private int getRaffTurntableLv(){
		return levelType.getNumber()*100+(mapLevel-1);
	}
	
	@Override
	protected void enterEggDoor(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human==null){
			Log.lootMap.error(" ========= enterNextLevel human is null humanId = {}",humanId);
			return;
		}
		lootMapType = ELootMapType.LootMapEgg;
		eggLevelCurTime = 0;
		enterSingleDoor(humanId);
	}
	
	/**
	 * 准备分发内容
	 * @param humanId
	 */
	private void enterSingleDoor(long humanId){
		int nextLootMapSn = InstLootMapManager.inst().getRandomLootMapConfSn(lootMapType.getNumber(),mapLevel); //获取随机组
		//int nextLootMapSn = ConfigKeyFormula.getLootMapSn(mapLevel,group,lootMapType); //获取地图sn
		//初始化sn
		mapInit(nextLootMapSn);
		//清除容器内容
		refreshSet.clear();
		eventMap.clear();
		monsterMap.clear();
		randomDoorMap.clear();
		//创建地图
		createMap();
		//传入玩家
		//intoMapNumber = 0;
		for(InstLootMapHumanData data : getAllHumanData()){
			humanIntoMap(data);
		}
		
		sendGameEnterLevelData(humanId);
	}
	
	@Override
	protected void activityTimeUp(){
		super.activityTimeUp();
		for(InstLootMapHumanData human : getAllHumanData()){
			sendSignelEnd(human.humanId); // 进行结算
		}
	}
	
	/**
	 * 单人结算接口 由客户端发起
	 */
	public void singleGameSettlement(long humanId){
		gameSettlement();
		sendSignelEnd(humanId);
	}
	
	private void sendSignelEnd(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human==null){
			Log.lootMap.error(" ========= readyEnterNextLevel human is null humanId = {}",humanId);
			return;
		}
		List<DItem.Builder> itemList = human.getAllItem();
		SCLootMapSingleEnd.Builder builder = SCLootMapSingleEnd.newBuilder();
		for(int i = 0; i < itemList.size();i++){
			builder.addItemList(itemList.get(i));
		}
		InstLootMapManager.inst().sendHuman(builder,getHumanObj(humanId));
	}
	
	/**
	 * 彩蛋关时间到了 不需要进行刷新
	 */
	@Override
	protected boolean isRefresh(){
		if(lootMapType == ELootMapType.LootMapEgg && eggLevelCurTime >= ParamManager.lootMapEggLevelTime){
			return false;
		}
		return true;
	}
}

