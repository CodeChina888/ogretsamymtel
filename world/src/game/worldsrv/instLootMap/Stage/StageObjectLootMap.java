package game.worldsrv.instLootMap.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import game.worldsrv.support.Vector2D;
import game.msg.Define.DItem;
import game.msg.Define.DVector2;
import game.msg.Define.ELootMapBuffTarger;
import game.msg.Define.ELootMapEventType;
import game.msg.Define.ELootMapLevelLimitType;
import game.msg.Define.ELootMapTimeMode;
import game.msg.Define.ELootMapType;
import game.msg.Define.EMailType;
import game.msg.Define.EMoneyType;
import game.msg.MsgInstLootMap.SCHumanEnter;
import game.msg.MsgInstLootMap.SCLootMapAddBuff;
import game.msg.MsgInstLootMap.SCLootMapAttackMonster;
import game.msg.MsgInstLootMap.SCLootMapEventDisenable;
import game.msg.MsgInstLootMap.SCLootMapEventEnable;
import game.msg.MsgInstLootMap.SCLootMapGameEnterLevel;
import game.msg.MsgInstLootMap.SCLootMapGetSkill;
import game.msg.MsgInstLootMap.SCLootMapHumanAttack;
import game.msg.MsgInstLootMap.SCLootMapHumanHP;
import game.msg.MsgInstLootMap.SCLootMapMonsterAttack;
import game.msg.MsgInstLootMap.SCLootMapMonsterHP;
import game.msg.MsgInstLootMap.SCLootMapMove;
import game.msg.MsgInstLootMap.SCLootMapOpenFloor;
import game.msg.MsgInstLootMap.SCLootMapOut;
import game.msg.MsgInstLootMap.SCLootMapPlayMove;
import game.msg.MsgInstLootMap.SCLootMapReadyEnterDoor;
import game.msg.MsgInstLootMap.SCLootMapRmvBuff;
import game.msg.MsgInstLootMap.SCLootMapSetPos;
import game.msg.MsgInstLootMap.SCLootMapTriggerEvent;
import game.msg.MsgInstLootMap.SCLootMapUseSkill;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfBuffVk;
import game.worldsrv.config.ConfLootMap;
import game.worldsrv.config.ConfLootMapLayout;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.enumType.LootMapType;
import game.worldsrv.enumType.SwitchState;
import game.worldsrv.instLootMap.InstLootMapManager;
import game.worldsrv.instLootMap.Game.InstLootMapBuff;
import game.worldsrv.instLootMap.Game.InstLootMapHumanData;
import game.worldsrv.instLootMap.Game.InstLootMapPoint;
import game.worldsrv.instLootMap.Game.InstLootMapSkill;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEvent;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEventBuff;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEventDoor;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEventHaloMonster;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEventLevel;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEventMonster;
import game.worldsrv.instLootMap.Game.Event.InstLootMapEventSkill;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.stage.StagePort;
import game.worldsrv.stage.types.StageObjectPKHuman;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class StageObjectLootMap extends StageObjectPKHuman {
	protected int objId = 0; // 事件id 刷新出来 ++
	protected ELootMapLevelLimitType levelType; // 等级区间
	protected int mapLevel; // 地图层数
	public int intoMapMaxNumber = 0; // 该地图开启的时候的人数
	public ELootMapType lootMapType; // 副本类型
	public boolean isStartGame = false; // 是否开启了游戏
	protected int lootMapWidth; // 地图尺寸
	protected int lootMapHeight;
	public int lootMapSn; // 该地图sn
	int[][] mapPosInfo = null; // 地图信息 阻碍为sn
	int[][] mapPosInfoEx = null; // 互斥地图内容<阻挡与传送门>
	
	protected boolean isMask = false; // 是否有遮罩
	protected int[][] floorInfo = null; // 地砖信息 1 为打开
	
	Set<InstLootMapEvent> refreshSet = new HashSet<InstLootMapEvent>(); // 等待刷新set
	public Map<Integer,InstLootMapEvent> eventMap = new HashMap<>(); // 已经刷新内容
	public Map<Integer,InstLootMapEventDoor> randomDoorMap = new HashMap<>(); // 传送门
	public Map<Integer,InstLootMapEventMonster> monsterMap = new HashMap<>(); // 怪物
	public Map<Long,InstLootMapHumanData> humanMap = new HashMap<>(); // 玩家map
	protected Vector2D vector2DZero = new Vector2D(0,0); // 0坐标
	protected ELootMapTimeMode timeMode = ELootMapTimeMode.LootMapTimeModeNormal; // 时间模式
	private TickTimer oneSecondTimer = new TickTimer(); //更新时间
	private TickTimer one30SecTimer = new TickTimer(); //每一个分钟
	private int gameCreateTime = 0; // 游戏create的时间
	protected int gameStartTime = 0; // 游戏玩家载入之后的时间
	
	List<Vector2D> humanPosList = new ArrayList<Vector2D>(); // 人物占位
	
	private boolean isCloseGame = false;
	
	public StageObjectLootMap(StagePort port, long stageId, int stageSn, int mapSn, int fightType, 
			int humanNumber, int lvType, int lootMapSn) {
		super(port, stageId, stageSn, mapSn, fightType, null);
		
		levelType = ELootMapLevelLimitType.valueOf(lvType);
		intoMapMaxNumber = humanNumber;
		mapLevel = 1;
		isCloseGame = false;
	}
	
	/**
	 * 生成抢夺本数据
	 * 该方法在子类对象的构造中被调用
	 * @param mapSn
	 */
	protected void mapInit(int mapSn){
		//生成二维数组
		mapPosInfo = new int[lootMapWidth][lootMapHeight];
		mapPosInfoEx = new int[lootMapWidth][lootMapHeight];		
		//设置lootMapSn
		this.lootMapSn = mapSn;
	}
	
	/**
	 * 正常心跳
	 */
	@Override
	public void pulse() {
		super.pulse(); // 父类心跳
		
		if(this.isDestroy) 
			return;
		
		boolean isOneSecond = false; // 是否经过1S
		if (oneSecondTimer.isPeriod(getTime())){ // 1S
			// 安全处理2：当安全处理1 经过10s还没destory()则手动执行
			if(isCloseGame && ++gameCreateTime >= ParamManager.lootMapRoomTime + 10){
				destory(); // 强制关闭
				return;
			}
			if(isCloseGame) return;
			// 安全处理1： 时间大于超时时间 强制切换stage
			if(++gameCreateTime == ParamManager.lootMapRoomTime){
				forciblyCloseLootMapState();
				return;
			}
			// 5s还没开始 则start
 			if(!isStartGame && gameCreateTime == 5){
				startGame();
			}
			isOneSecond = true; // 经过了1s
		}
		gamePulse(isOneSecond);
	}

	
	/**
	 * 游戏开始的心跳
	 */
	private void gamePulse(boolean isOneSecond){
		if(isOneSecond){
			pulseOneSconed();
		}
	}
	
	/**
	 * 游戏开始 1s
	 */
	private void pulseOneSconed(){
		gameStartTime++;
		onOneSecond();
		if(one30SecTimer.isPeriod(getTime())){
			pulse30Sec();
		}
	}
	
	/**
	 * 游戏开始1min
	 */
	private void pulse30Sec(){
		on30Sec();
	}

	/**
	 * 子类覆盖
	 */
	protected void on30Sec(){}
	
	/**
	 * 一秒回调
	 */
	protected void onOneSecond(){
		oneSecondRefresh();
	}
	
	/**
	 * 时间到了进行结算
	 */
	protected void activityTimeUp(){
		//单人情况：
		//1.单人没有时间限制 但是还是有超时时间 超时时间到了
		//
		
		//多人情况：
		//1.房间时间到了
		gameSettlement();
		isStartGame = false;
	}
	
	/**
	 * 子类覆盖 是否需要进行刷新
	 * @return
	 */
	protected boolean isRefresh(){
		//单人本情况 ：彩蛋关时间到 不再走刷新
		return true;
	}
	
	private void oneSecondRefresh(){
		if(!isStartGame) return;
		if(!isRefresh()) return;
		//set内容为空
		if(refreshSet.size()==0){
			return;
		}
		//迭代器检测
		Iterator<InstLootMapEvent> it = refreshSet.iterator();
		ArrayList<InstLootMapEvent> reArray = null;// 刷新数组
		while(it.hasNext()){
			InstLootMapEvent insEvent =  it.next();//取出对象
			if(insEvent == null){
				continue;
			}
			
			if(isErrRefershPos(insEvent.getPosX(),insEvent.getPosY())){
				continue;
			}
			
			if(getTime() >= insEvent.refershTime){// 判断是否到了刷新时间
				if(reArray == null){
					reArray = new ArrayList<InstLootMapEvent>();
				}
				reArray.add(insEvent);
				it.remove();// 删除该对象
			}
		}
		
		if(reArray!=null && reArray.size() > 0 ){
			notifyLootMapEventEnable(reArray);// 刷新 加入刷新内容 通知
		}
	}
	
	/**
	 * 是否符合会刷新规则
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isErrRefershPos(int x,int y){
		if(isErrorPoint(x, y)) return true;
		
		for(InstLootMapEvent insEvent : eventMap.values()){
			if(insEvent.getPosX() == x && insEvent.getPosY() == y){
				return true;
			}
		}
		
		for(InstLootMapHumanData human : getAllHumanData()){
			if(human.getPosX() == x && human.getPosY() == y){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 生成内容信息
	 */
	@Override
	public void createMonster() {
		createMap();
		oneSecondTimer.start(getTime(), Time.SEC);
		one30SecTimer.start(getTime(), Time.SEC * 30);
	}
	
	public void createMap(){
		ConfLootMap confLootMap = ConfLootMap.get(lootMapSn);
		if(confLootMap == null){
			Log.lootMap.error(" ======== ConfLootMap.get is null sn = {}",lootMapSn);
			return;
		}
		mapSn = confLootMap.mapSn;
		isMask = confLootMap.generateMask == 1; //地砖内容 
		if(isMask){
			floorInfo = new int[lootMapWidth][lootMapHeight];	
		}else{
			floorInfo = null;
		}
		
		//获取layout数组
		int[] layoutSn = confLootMap.layoutSn;
		ArrayList<InstLootMapEvent> eventArray = new ArrayList<InstLootMapEvent>();
		for(int i = 0;i<layoutSn.length;i++){
			int sn = layoutSn[i];
			ConfLootMapLayout confLayout = ConfLootMapLayout.get(sn);
			if(confLayout == null){
				continue;
			}
			int number = confLayout.number;
			// 刷新n个
			for(int j = 0;j < number;j++){
				if(InstLootMapEvent.isLayoutRefersh(confLayout.refreshOdds)){ // 判断是否有刷新
					InstLootMapEvent instEvent = getInstLootMapEvent(confLayout);
					if(instEvent == null){
						Log.lootMap.error("创建内容失败 ：instEvent == null eventSn = {}, ",confLayout.eventSn);
						continue;
					}
					instEvent.layoutSn = confLayout.sn;
					eventArray.add(instEvent);
				}
			}
		}
		//设置layuot定点坐标
		initFixPosEvent(eventArray);
		//设置玩家到点位置上
		initHumanStop();
		//设置剩下的坐标(随机)
		initRandomPosEvent(eventArray);
		//清除内容
		eventArray.clear();
		//设置完毕
		createComplete();
	}
	
	/**
	 * 创建完毕 释放无用内容
	 */
	private void createComplete(){
		mapPosInfoEx = null;
		if(isMask == false){
			//查找所有怪物 增加攻击力
			for(InstLootMapEventMonster monster :monsterMap.values()){
				if(monster.getEventType() == ELootMapEventType.LootMapEventHaloMonster){
					int buffSn = monster.getBuffSn();
					for(InstLootMapEventMonster addBuffMonster : monsterMap.values()){
						addBuffMonster.addBuff(buffSn, 1);
					}
				}
			}
		}
	}
	
	
	/**
	 * 设置事件id
	 * @param instEvent
	 */
	protected void setInstEventObjId(InstLootMapEvent instEvent){
		instEvent.id = ++objId;
	}
	
	/**
	 * 根据配置表内容 生成对象
	 * @param layoutConf
	 * @return
	 */
	protected InstLootMapEvent getInstLootMapEvent(ConfLootMapLayout layoutConf){
		
		int confLootMapEventSn = layoutConf.eventSn; // 配置表sn
		//ConfLootMapEvent conf = ConfLootMapEvent.get(confLootMapEventSn); // 获取event基础配置
		ELootMapEventType eventType = InstLootMapEvent.getEventType(confLootMapEventSn);
		
		if(eventType == null){
			Log.lootMap.error("创建内容失败 ：ConfLootMapLayout.eventSn to ELootMapEventType == null eventSn = {}, ",layoutConf.eventSn);
			return null;
		}
		
		InstLootMapEvent instEvent = null;
		
		switch(eventType){
			case LootMapEventMonster:{
				instEvent = new InstLootMapEventMonster(confLootMapEventSn,this.mapLevel,this.levelType.getNumber(),lootMapType);
			}break;
			case LootMapEventHaloMonster:{
				instEvent = new InstLootMapEventHaloMonster(confLootMapEventSn,this.mapLevel,this.levelType.getNumber(),lootMapType);
			}break;
			case LootMapEventResource:{
				instEvent = new InstLootMapEventLevel(confLootMapEventSn,this.mapLevel,this.levelType.getNumber(),lootMapType);
			}break;
			case LootMapEventBuff:{
				instEvent = new InstLootMapEventBuff(confLootMapEventSn,this.mapLevel,this.levelType.getNumber(),lootMapType);
			}break;
			case LootMapEventSkill:{
				instEvent = new InstLootMapEventSkill(confLootMapEventSn,this.mapLevel,this.levelType.getNumber(),lootMapType);
			}break;
			case LootMapEventStop:{
				instEvent = new InstLootMapEvent(confLootMapEventSn,this.mapLevel,this.levelType.getNumber(),lootMapType);
			}break;
			case LootMapEventNextDoor:
			case LootMapEventEggDoor:
			case LootMapEventRandomDoor:{
				instEvent = new InstLootMapEventDoor(confLootMapEventSn,lootMapType);
			}break;
			default:
				return null;
				//break;
		}
		instEvent.init(layoutConf);
		return instEvent;
	}
	
	/**
	 * 根据layout配置 找到定点的事件
	 */
	private void initFixPosEvent(ArrayList<InstLootMapEvent> arry){
		//遍历layoutSn
		for(int i = 0;i < arry.size();i++){
			InstLootMapEvent instEvent = arry.get(i);
			int layoutSn = instEvent.layoutSn;
			//取得配表
			ConfLootMapLayout confLayout = ConfLootMapLayout.get(layoutSn);
			if(confLayout==null){
				continue;
			}
			//配表不为空 判断坐标是否有效
			if(confLayout.enablePos==1){
				
				if(confLayout.posX >= lootMapWidth){
					Log.lootMap.debug(" confLayout.posX >= lootMapWidth");
					continue;
				}
				
				if(confLayout.posY >= lootMapHeight){
					Log.lootMap.debug(" confLayout.posY >= lootMapHeight");
					continue;
				}
				
				//判断是否包含这个key
				instEvent.setPosX(confLayout.posX);
				instEvent.setPosY(confLayout.posY);
				
				//只有怪物会互斥
				createMutally(instEvent.getPosX(),instEvent.getPosY(),instEvent.eventSn);
				//创建这个事件 -> 加入管理
				createIntoMap(instEvent,confLayout.firstDelalTime);
			}
		}
	}
	
	/**
	 * 随机生成位置
	 */
	private void initRandomPosEvent(ArrayList<InstLootMapEvent> arry){
		for(int i = 0;i < arry.size();i++){
			InstLootMapEvent instEvent = arry.get(i);
			int layoutSn = instEvent.layoutSn;
			//取得配表
			ConfLootMapLayout confLayout = ConfLootMapLayout.get(layoutSn);
			if(confLayout==null){
				continue;
			}
			//配表不为空 判断坐标是否有效
			if(confLayout.enablePos==0){
				int[] pos = getMapFreePoint(instEvent.eventSn);
				if(pos == null){
					continue;
				}
				//设置坐标
				instEvent.setPosX(pos[0]);
				instEvent.setPosY(pos[1]);
				createMutally(pos[0],pos[1],instEvent.eventSn);
				//创建这个事件 -> 加入管理
				createIntoMap(instEvent,confLayout.firstDelalTime);
			}
		}
	}
	
	/**
	 * 设置玩家互斥位置
	 */
	private void initHumanStop(){
		humanPosList.clear();
		InstLootMapEventDoor door = getNextDoor();
		int stop = ParamManager.lootMapStartPos; // 范围
		if(door != null){
			//这个范围不能有内容
			int [] range = InstLootMapPoint.getRangePoint(door.getPosX(), door.getPosY(), stop, stop,lootMapWidth,lootMapHeight); // 取得门的范围
			int [][] humanPosInfo = new int[lootMapWidth][lootMapHeight];
			for(int i =0;i < humanPosInfo.length;i++){
				for(int j = 0; j < humanPosInfo[i].length;j++){
					if(InstLootMapPoint.isInRange(i,j,range)){ // 遍历设置阻碍
						humanPosInfo[i][j] = 1;
					}
				}
			}
			for(int i =0;i < mapPosInfo.length;i++){
				for(int j = 0; j < mapPosInfo[i].length;j++){
					if(mapPosInfo[i][j] == 1){
						humanPosInfo[i][j] = 1;
					}
				}
			}
			
			List<Vector2D> tempList = new ArrayList<Vector2D>();
			for(int i =0;i < humanPosInfo.length;i++){
				for(int j = 0; j < humanPosInfo[i].length;j++){
					if(humanPosInfo[i][j] == 0){
						Vector2D v2 = new Vector2D(i,j);
						tempList.add(v2);
					}
				}
			}
			
			int count = 0;
			while(true){
				int tempSize = tempList.size();
				if(tempSize == 0){
					break;
				}
				int randomIndex = (int)(Utils.randomBetween(0, tempSize));
				Vector2D v2 = tempList.get(randomIndex);
				tempList.remove(randomIndex);
				humanPosList.add(v2);
				if(++count == intoMapMaxNumber){
					break;
				}
			}
			tempList.clear();
		}
	}
	
	/**
	 * 创建占位
	 * @param x
	 * @param y
	 */
	private void createMutally(int x,int y,int sn){
		
		ELootMapEventType eventType = InstLootMapEvent.getEventType(sn);
		
		switch(eventType){
			case LootMapEventNextDoor:
			case LootMapEventStop:{
				if(mapPosInfoEx != null){
					mutuallyExclusMapPlacehold(x,y);
				}
			}break;
			default:{
				if(mapPosInfo!=null){
					mapPosInfo[x][y] = 1;
				}
			}break;
		}
	}
	
	/**
	 * 设置完坐标 判断是否有生成
	 * @param instEvent
	 */
	private void createIntoMap(InstLootMapEvent instEvent,int firstDelalTime){
		if(firstDelalTime!=0){
			addSetNextRefersh(instEvent,firstDelalTime); //需要等待刷新
		}else{ // firstDelalTime!=0 
			refershObject(instEvent);// 刷新成功
		}
	}
	
	/**
	 * 刷新 添加管理
	 * @param instEvent
	 */
	private void refershObject(InstLootMapEvent instEvent){
		instEvent.refersh();//扣去刷新次数
		setInstEventObjId(instEvent);//设置事件id
		addEventToMap(instEvent);
		switch(instEvent.getEventType()){//取出传送门 直接出现
			case LootMapEventNextDoor:
			case LootMapEventRandomDoor:{
				if(isErrorPoint(instEvent.getPosX(),instEvent.getPosY()) == false){
					openFloor(instEvent.getPosX(),instEvent.getPosY()); //开放这个点
				}
			}break;
			default:break;
		}
	}
	
	/**
	 * 添加管理
	 * @param instEvent
	 */
	private void addEventToMap(InstLootMapEvent instEvent){
		
		//占地图位置
				
		mapPlacehold(instEvent.id,instEvent.eventSn,instEvent.getPosX(),instEvent.getPosY());
		switch(instEvent.getEventType()){
			case LootMapEventMonster:
			case LootMapEventHaloMonster:{
				monsterMap.put(instEvent.id,(InstLootMapEventMonster)instEvent);
			}break;
			case LootMapEventRandomDoor:{
				randomDoorMap.put(instEvent.id,(InstLootMapEventDoor)instEvent);
			}break;
		default:
			break;
		}
		eventMap.put(instEvent.id, instEvent);
	}
	
	protected void rmvEventToMap(int eventId){
		if(eventMap.containsKey(eventId)==false){ // 没有包含这个key
			Log.lootMap.error(" ========= rmvEventToMap eventMap.containsKey == false eventId = {}",eventId);
			return;
		}
		InstLootMapEvent instEvent = eventMap.get(eventId);//取出这个id 进行移除
		eventMap.remove(eventId); // 从map移除
		if(instEvent == null){ // 当事件 == null 
			Log.lootMap.error(" ========= rmvEventToMap instEvent == null eventId = {}",eventId);
			return;
		}
		mapPosInfo[instEvent.getPosX()][instEvent.getPosY()] = 0; // 占位设置 = 0
		
		switch(instEvent.getEventType()){
			case LootMapEventMonster:
			case LootMapEventHaloMonster:{ // 从怪物map中移除
				if(monsterMap.containsKey(instEvent.id)){
					monsterMap.remove(instEvent.id);
				}
				tryCreateEggDoor((InstLootMapEventMonster)instEvent); // 生成彩蛋关卡
			}break;
			default:
				break;
		}
		
		if(instEvent.isCreateCountZero()==false){ // 当还存在创建次数
			addSetNextRefersh(instEvent,instEvent.againRefreshTime);
		}	
	}
	
	/**
	 * 创建彩蛋关卡
	 * @param monster
	 */
	private void tryCreateEggDoor(InstLootMapEventMonster monster){
		if(isHasEggDoor()) return;
		if(monster != null){
			if(monster.isTriggerEggDoor()){
				InstLootMapEventDoor door = new InstLootMapEventDoor(monster.getEggDoorEventSn(),lootMapType);
				door.setPosX(monster.getPosX());
				door.setPosY(monster.getPosY());
				notifyLootMapEventEnable(door);
			}
		}
	}
	
	private boolean isHasEggDoor(){
		for(InstLootMapEvent mapEvent : getAllEvent()){
			if(mapEvent.getEventType() == ELootMapEventType.LootMapEventEggDoor){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 刷新 添加管理 并通知
	 */
	private void notifyLootMapEventEnable(ArrayList<InstLootMapEvent> array){
		
		//遍历事件对象
		for(int i = 0;i < array.size();i++){
			
			InstLootMapEvent instEvent = array.get(i);
			refershObject(instEvent);
			
			SCLootMapEventEnable.Builder msg = SCLootMapEventEnable.newBuilder();
			ELootMapEventType enableType = instEvent.getEventType();
			switch(enableType){
				case LootMapEventMonster:
				case LootMapEventHaloMonster:{
					InstLootMapEventMonster enableMonster = (InstLootMapEventMonster)instEvent;
					for(InstLootMapEventMonster monster : monsterMap.values()){
						if(monster.id == enableMonster.id) continue; // 等于自身 不增加
						if(isHasFloor(monster.getPosX(),monster.getPosY())) continue; // 没有打开
						if(monster.getEventType() == ELootMapEventType.LootMapEventHaloMonster){
							int buffSn = monster.getBuffSn();
							enableMonster.addBuff(buffSn,1);
						}
					}
				}
				default:break;
			}
			msg.setDEvent(instEvent.getDLootMapEvent());
			InstLootMapManager.inst().sendAllHuman(msg,this);//发送给所有玩家 先添加事件
			
			if(enableType == ELootMapEventType.LootMapEventHaloMonster){ // 将刷新列表中的光环怪触发内容
				if(isHasFloor(instEvent.getPosX(),instEvent.getPosY()) == false){
					sendHaloMonsterBuff(instEvent.id,true);
				}
			}
		}

	}
	
	
	/**
	 * 一个事件
	 * @param mapEvent
	 */
	private void notifyLootMapEventEnable(InstLootMapEvent mapEvent){
		SCLootMapEventEnable.Builder msg = SCLootMapEventEnable.newBuilder();
		refershObject(mapEvent);
		msg.setDEvent(mapEvent.getDLootMapEvent());
		InstLootMapManager.inst().sendAllHuman(msg,this);
	}
	
	
	/**
	 * 移除事件
	 */
	protected void notifyLootMapEventDisenable(int eventId){
		InstLootMapEvent instEvent = null;
		if(eventMap.containsKey(eventId)){
			instEvent = eventMap.get(eventId);
		}else{
			return;
		}
		
		if(instEvent!=null){
			SCLootMapEventDisenable.Builder msg = SCLootMapEventDisenable.newBuilder();
			msg.setEventId(eventId);
			InstLootMapManager.inst().sendAllHuman(msg,this);
		}
	}

	/**
	 * 加入set中 等待队列刷新
	 * @param instEvent
	 * @param needTime
	 */
	private void addSetNextRefersh(InstLootMapEvent instEvent,int needTime){
		nextRefersh(instEvent,needTime);// 设置下次刷新时间
		refreshSet.add(instEvent); // 添加到set中 等待刷新
	}
	
	/**
	 * 事件 等待时间 needTime / 1000
	 * @param instEvent
	 * @param needTime
	 */
	private void nextRefersh(InstLootMapEvent instEvent,int needTime){
		instEvent.refershTime = this.getTime()+needTime;
	}
	
	private int[] getMapFreePoint(int eventSn){
		
		ArrayList<int[]> pointArray = new ArrayList<int[]>();
		ELootMapEventType eventType =  InstLootMapEvent.getEventType(eventSn);
		
		int[][] posInfo = null;
		
		switch(eventType){//当是互斥的时候 从互斥里面找
			case LootMapEventStop:
			case LootMapEventNextDoor:{
				posInfo = mapPosInfoEx;
			}break;
			default:{ // 从mapInfo找
				posInfo = mapPosInfo;
			}break;
		}
		
		for(int i = 0; i < posInfo.length ;i++){
			for(int j = 0; j < posInfo[i].length ;j++){
				if(posInfo[i][j] == 0){ // 玩家位置过滤
					boolean isContinue = false;
					for(int k =0;k < humanPosList.size();k++){
						Vector2D v = humanPosList.get(k);
						if(v.x == i && v.y == j){
							isContinue = true;
							break;
						}
					}
					if(isContinue){
						continue;
					}
					pointArray.add(new int[]{i,j});
				}
			}
		}
		
		if(pointArray.size() == 0){
			return null;
		}
		
		int index = (int)(Math.random()*(pointArray.size()-1));
		return pointArray.get(index);
	}
	
	/**
	 * 地图占位
	 * 0 0 -> 指向 1,1
	 * @param x
	 * @param y
	 */
	private void mapPlacehold(int eventId,int eventSn,int x,int y){
		if(isErrorPoint(x, y)) return;
		mapPosInfo[x][y] = eventId;
	}
	
	/**
	 * 判断点是否正确
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isErrorPoint(int x,int y){
		if(x < 0 || y < 0 || x >= lootMapWidth || y >= lootMapHeight){
			Log.lootMap.debug("   =========   isErrorPoint !! x = {} y = {}",x,y);
			return true;
		}
		return false;
	}
	
	/**
	 * 是否是事件
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isEventPos(int x,int y){
		if(isErrorPoint(x,y)) return false;
		return mapPosInfo[x][y] != 0;
	}
	

	
	/**
	 * 该点是否是玩家阻碍
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isHumanMapStopPos(int x,int y){
		if(isErrorPoint(x,y)) return true;
		if(isHasFloor(x,y)) return true; // 地砖 
		int eventId = mapPosInfo[x][y];
		if(eventId == 0) return false;
		if(eventMap.containsKey(eventId) == false){
			mapPosInfo[x][y] = 0; // 修正
			return false;
		}
		InstLootMapEvent mapEvent = eventMap.get(eventId);
		if(mapEvent == null) return false;
		return LootMapType.isHumanStop(mapEvent.getEventType());
	}
	
	/**
	 * 互斥地图占位
	 * @param x
	 * @param y
	 */
	private void mutuallyExclusMapPlacehold(int x,int y){
		//取得互斥范围值
		int[] points = InstLootMapPoint.getRangePoint(
				x, y, 
				LootMapType.mutuallyExclusiveX.value(), 
				LootMapType.mutuallyExclusiveY.value(),
				lootMapWidth,lootMapHeight);
		
		//设置互斥范围内为占位
		for(int i = points[0];i <= points[2];i++){
			for(int j = points[1];j <= points[3];j++){
				mapPosInfoEx[i][j] = 1;
			}
		}
	}

	/**
	 * 获取传送门位置
	 * @return
	 */
	InstLootMapEventDoor getNextDoor(){
		for(InstLootMapEvent mapEvent : getAllEvent()){
			if(mapEvent.getEventType() == ELootMapEventType.LootMapEventNextDoor){
				return (InstLootMapEventDoor)mapEvent;
			}
		}
		return null;
	}
	
	/**
	 * 获取一个玩家可用坐标
	 * @return
	 */
	private Vector2D getHuamnFreePos(){

		if(humanPosList.size() > 0){
			Vector2D v2 = humanPosList.get(0);
			humanPosList.remove(0);
			return v2;
		}else{
			Log.lootMap.debug("   ========   getHuamnFreePos err humanPosList size = 0");
		}
		
		return null;
	}
	
	public SCLootMapGameEnterLevel.Builder getGameEnterLevelData(){
		SCLootMapGameEnterLevel.Builder builder = SCLootMapGameEnterLevel.newBuilder();
		builder.setMapSn(mapSn);
		builder.setLootMapSn(lootMapSn);
		builder.setMapType(lootMapType);
		builder.setLevel(mapLevel);
		if(isMask && floorInfo != null){
			for(int i = 0;i < floorInfo.length;i++){
				for(int j = 0;j < floorInfo[i].length;j++){
					if(isHasFloor(i,j)){
						DVector2.Builder v2 = DVector2.newBuilder();
						v2.setX(i);
						v2.setY(j);
						builder.addFloorList(v2);
					}
				}
			}
		}
		for(InstLootMapHumanData data:getAllHumanData()){ //添加玩家
			builder.addHuamanList(data.getDLootMapHuman());
		}
		for(InstLootMapEvent data:getAllEvent()){ // 添加事件
			builder.addEventSnList(data.getDLootMapEvent());
		}
		return builder;
	}
	
	/**
	 * 发送进入数据
	 * 不同子类需要自己覆盖且调用它本方法
	 * @param humanId
	 */
	public void sendGameEnterLevelData(long humanId){
		InstLootMapManager.inst().sendHuman(getGameEnterLevelData(), getHumanObj(humanId));
	}
	
	/**
	 * 设置坐标
	 */
	protected void humanIntoMap(InstLootMapHumanData human){
		Vector2D pos = getHuamnFreePos();
		if(pos == null) return;
		int x = (int)pos.x;
		int y = (int)pos.y;
		//设置坐标
		setHumanPos(human.humanId,x,y);
		// 设置自动打开地砖
		openFloor(x,y);
	}
	
	public boolean intoMap(HumanObject humanData){
		if(humanMap.containsKey(humanData.id)){
			return false;
		}
		//在map增加
		InstLootMapHumanData human = newHuman(humanData);
		human.stage = this;
		humanMap.put(humanData.id,human);
		humanIntoMap(human);
		
		switch(lootMapType){
			case LootMapSingle:
			case LootMapMultip:{
				Event.fire(EventKey.LootMapEnter, "humanObj", humanData,"lootMapType",lootMapType);
			}break;
			default:break;
		}
		
		//通知其他人玩家进入
		SCHumanEnter.Builder humanEnterBuilder = SCHumanEnter.newBuilder();
		humanEnterBuilder.setHuman(human.getDLootMapHuman());
		InstLootMapManager.inst().sendAllHuman(humanEnterBuilder, this);
		
		return true;
	}

	protected boolean outMap(long humanId){
		if(humanMap.containsKey(humanId) == false){
			return false;
		}
		humanMap.remove(humanId); // 移除
		return true;
	}
	
	/**
	 * 获取房间中的一个玩家
	 * @param humanId
	 * @return
	 */
	protected InstLootMapHumanData GetHumanData(long humanId) {
		if(humanMap.containsKey(humanId)){
			return humanMap.get(humanId);
		}
		return null;
	}
	
	public Collection<InstLootMapHumanData> getAllHumanData(){
		return humanMap.values();
	}
	
	/**
	 * 房间是否满人
	 * @return
	 */
	public boolean isMapFull(){
		return intoMapMaxNumber == getHumanObjs().size();
	}
	
//	/**
//	 * 玩家移动检测
//	 * @param humanId
//	 * @param posList
//	 */
//	public void humanReadyMove(long humanId,List<DVector2> posList){
//		
//		InstLootMapHumanData data = GetHumanData(humanId);
//		
//		if(data == null){
//			Log.lootMap.error("===humanReadyMove human is null id = {}",humanId);
//			return;
//		}
//		
//		//第一个点是否合法 这个坐标为客户端玩家当前位置
//		DVector2 firstPoint = posList.get(0);
//		
//		if(data.isCorrectRangePoint(firstPoint.getX(),firstPoint.getY()) == false){
//			Log.lootMap.debug("===humanReadyMove isCorrectRangePoint == false, humanId = {} humanX = {} huamnY = {} posX = {} posY = {}",
//					humanId,data.getPosX(),data.getPosY(),firstPoint.getX(),firstPoint.getY());			
//			return;
//		}
//		
//
//		//取出现在坐标
//		int posX = (int)firstPoint.getX();
//		int posY = (int)firstPoint.getY();
//		
//		List<DVector2> movePath = new ArrayList<DVector2>();
//		movePath.add(firstPoint);
//		
//		//玩家第一个点抛弃掉
//		for(int i = 1;i < posList.size();i++){
//			DVector2 v = posList.get(i); // 获取其中一个点
//			int pointX = (int)v.getX();
//			int pointY = (int)v.getY();
//			if(isHumanMapStopPos(pointX,pointY)){return;} //该点是玩家阻碍
//			if(InstLootMapPoint.isAroundPoint(pointX, pointY, posX, posY) == false){return;}//超出范围的点
//			posX = pointX; // 正常 检测下一个
//			posY = pointY; // 正常 检测下一个
//			movePath.add(v);
//		}
//		
//		SCLootMapReadyMove.Builder builder = SCLootMapReadyMove.newBuilder();
//		builder.setHumanId(humanId);
//		builder.addAllPos(movePath);
//		InstLootMapManager.inst().sendAllHuman(builder, this);
//	}
	
	/**
	 * 玩家播放跑动作
	 * @param isPlay
	 */ 
	public void humanPlayMove(long humanId,boolean isPlay){
		InstLootMapHumanData data = GetHumanData(humanId);
		if(data == null){
			Log.lootMap.error("===humanReadyMove human is null id = {}",humanId);
			return;
		}
		SCLootMapPlayMove.Builder bulider = SCLootMapPlayMove.newBuilder();
		bulider.setIsPlay(isPlay);
		bulider.setHumanId(humanId);
		InstLootMapManager.inst().sendAllHuman(bulider,humanId,this);
	}
	
	
	/**
	 * 玩家移动
	 * @return
	 */
	public void humanMove(long humanId,int x,int y){

		InstLootMapHumanData data = GetHumanData(humanId);
		if(data == null){ //记录数据空
			return;
		}
		
		if(InstLootMapPoint.isAroundPoint(data.getPosX(),data.getPosY(),x,y)==false){ //记录位置不合法
			Log.lootMap.debug("===humanMove pos is isAroundPoint is Err ,  curx = {} , cury = {}, x = {} , y = {}",data.getPosX(),data.getPosY(),x,y);
			return;
		}
		
		if(isHumanMapStopPos(x, y)){ //判断位置是否合法
			Log.lootMap.debug("===humanMove pos is humanStop humanId = {} , x = {} , y = {}",humanId,x,y);
			return;
		}
		
		setHumanPos(humanId,x,y); // 设置坐标
		
		DVector2.Builder v2 = DVector2.newBuilder();
		v2.setX(x);
		v2.setY(y);
		SCLootMapMove.Builder builder = SCLootMapMove.newBuilder();
		builder.setPos(v2);	
		builder.setHumanId(humanId);
		InstLootMapManager.inst().sendAllHuman(builder,humanId,this);
	}
	

	
	/**
	 * 设置玩家坐标
	 * @param humanId
	 * @param x
	 * @param y
	 */
	protected void setHumanPos(long humanId,int x,int y){
		InstLootMapHumanData data = GetHumanData(humanId);
		if(data != null){
			data.setPosX(x);
			data.setPosY(y);
		}
		HumanObject humanObj = getHumanObj(humanId);
		humanObj.posNow.x = x;
		humanObj.posNow.y = y;
	}
	
	/**
	 * 获取一个事件
	 * @param eventId
	 * @return
	 */
	public InstLootMapEvent getEvent(int eventId){
		if(eventMap.containsKey(eventId)){
			return eventMap.get(eventId);
		}
		return null;
	}
	
	/**
	 * 获取一个玩家 HumanGlobalInfo
	 * @param humanData
	 * @return
	 */
	protected InstLootMapHumanData newHuman(HumanObject humanData) {
		//TODO 这里可能已经在里面了 - 走back
		return new InstLootMapHumanData(humanData.getHuman(),humanData.getHumanExtInfo(),lootMapType);
	}
	
	/**
	 * 获取所有数据
	 * @return
	 */
	protected Collection<InstLootMapEvent> getAllEvent(){
		return eventMap.values();
	}
	
	/**
	 * 开启游戏
	 */
	public void startGame(){
		isStartGame = true;
		gameStartTime = 0;
	}
	
	/**
	 * 是否存在地砖
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isHasFloor(int x,int y){
		return isMask ?  floorInfo[x][y] == 0 : false;
	}
	
	/**
	 * 打开一个地砖
	 * @param x
	 * @param y
	 */
	public void openFloor(long humanId,int x,int y){
		if(isErrorPoint(x, y)) return;
		if(isHasFloor(x,y)==false){
			Log.lootMap.debug("   =========   isFloor is false x = {} y = {}",x,y);
			return;
		}
		if(isOpenFloorErr(x,y)){
			Log.lootMap.debug("   =========   isOpenFloorErr x = {} y = {}",x,y);
			return;
		}
		//从floor里面移除
		openFloor(x,y);
		
		//广播
		SCLootMapOpenFloor.Builder msg = SCLootMapOpenFloor.newBuilder();
		DVector2.Builder v2 = DVector2.newBuilder();
		v2.setX(x);
		v2.setY(y);
		msg.setPos(v2);
		InstLootMapManager.inst().sendAllHuman(msg,this);
		
		//打开触发
		openFloorEnable(x,y);
	}
	
	/**
	 * 判断这个点是否能打开
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isOpenFloorErr(int x,int y){
		int minX = x - 1;
		int minY = y - 1;
		int maxX = x + 1;
		int maxY = y + 1;
		for(int i = minX;i <= maxX;i++){
			for(int j = minY ; j <= maxY;j++){
				if(i == x && j == y) continue; // 这个点与打开的点一致
				if(isErrorPoint(i, j)) continue;
				if(isHasFloor(i,j)) continue; // 这个点还没打开
				if(isEventPos(i,j) == false) continue;// 这个点不是事件
				int eventId = mapPosInfo[i][j]; // 取得事件id
				if(eventMap.containsKey(eventId) == false) continue; // 存在事件id 不包含事件 跳过
				InstLootMapEvent mapEvent = eventMap.get(eventId); // 取得事件
				switch(mapEvent.getEventType()){ // 判断是否怪物内容
					case LootMapEventMonster:
					case LootMapEventHaloMonster:
						return true;
					default:
						continue;
				}
			}
		}
		return false;
	}
	
	/**
	 * 打开 -> 数据记录
	 * @param x
	 * @param y
	 */
	private void openFloor(int x,int y){
		if(isMask){
			floorInfo[x][y] = 1; // 设置1 为开启
		}
	}
	
	/**
	 * 打开触发内容
	 * @param x
	 * @param y
	 */
	private void openFloorEnable(int x,int y){
		//触发buff
		for(InstLootMapEvent instEvent: eventMap.values()){
			if(instEvent.isOnePoint(x, y)){
				//开启发现光环怪物
				if(instEvent.getEventType() == ELootMapEventType.LootMapEventHaloMonster){
					sendHaloMonsterBuff(instEvent.id,true);
				}
				break;
			}
		}
	}
	
	/**
	 * 怪物buff 控制
	 * @param eventId
	 */
	private void haloMonsterBuff(int eventId,int buffSn,boolean isAdd){
		for(InstLootMapEventMonster m: monsterMap.values()){
			if(isAdd){
				m.addBuff(buffSn,1);	
			}else{
				if(m.id == eventId) continue;
				m.rmvBuff(buffSn,1);
			}
		}
	}
	
	/**
	 * 添加与发送怪物buff
	 * @param eventId
	 * @param isAdd
	 */
	protected void sendHaloMonsterBuff(int eventId,boolean isAdd){
		if(eventMap.containsKey(eventId) ==false) return;
		InstLootMapEventBuff instEvent = (InstLootMapEventBuff) eventMap.get(eventId);
		if(instEvent == null) return;
		int buffSn = instEvent.getBuffSn();
		if(buffSn == 0) return;
		if(instEvent.buffTarget != ELootMapBuffTarger.LootMapBufMonster) return; // 不是针对怪物 不获取与发送
		haloMonsterBuff(eventId,buffSn,isAdd); // 添加或删除
		if(isAdd){
			sendAddBuff(0, buffSn);
		}else{
			List<Integer> buffSnList = new ArrayList<Integer>();
			buffSnList.add(buffSn);
			sendRmvBuff(0, buffSnList);
		}
		
		if(InstLootMapBuff.isMonsterAttack(buffSn)){
			SCLootMapMonsterAttack.Builder attackBuilder = SCLootMapMonsterAttack.newBuilder();
			for(InstLootMapEventMonster m: monsterMap.values()){
				attackBuilder.addEventIdList(m.id);
				attackBuilder.addAttack(m.getAttack());
			}
			InstLootMapManager.inst().sendAllHuman(attackBuilder, this);
		}
	}
	
	/**
	 * 判断改事件是否存在
	 * @param eventId
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isTriggerEvent(int eventId,int x,int y){
		if(eventMap.containsKey(eventId)){
			InstLootMapEvent insEvent = eventMap.get(eventId);
			//获取不能触发的对象
			switch(insEvent.getEventType()){
				case LootMapEventStop:{
					return false;
				}
				default:break;
			}
			if(insEvent.isOnePoint(x,y)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * 触发事件
	 */
	public void triggerEvent(long humanId,int eventId){
		InstLootMapEvent baseEvent = null;
		//判断在map里面是否存在
		if(eventMap.containsKey(eventId)){
			baseEvent = eventMap.get(eventId);
		}

		if(baseEvent == null){ //判断该事件是否为空
			Log.lootMap.error(" === triggerEvent === baseEvent is null , eventId = {}",eventId);
			return;
		}
		

		notifyLootMapEventDisenable(baseEvent.id);
		ELootMapEventType eventType = baseEvent.getEventType();
		//判断事件类型
		switch(eventType){
			case LootMapEventMonster:{
				InstLootMapEventMonster instEvent = (InstLootMapEventMonster)baseEvent; // 取得事件
				
				if(instEvent.getHP() != 0){
					Log.lootMap.debug(" === triggerEvent === monster hp != 0, eventId = {}",eventId);
					return;
				}
				
				sendGetReward(humanId,instEvent.id); // 获取奖励添加到玩家信息并发送
				sendGetBuff(humanId,instEvent.id); // 触发buff 并发送
			}break;
			case LootMapEventHaloMonster:{//触发奖励,可能触发buff
				InstLootMapEventHaloMonster instEvent = (InstLootMapEventHaloMonster)baseEvent;
				//triggerRmvEvent(instEvent.id);
				
				if(instEvent.getHP() != 0){
					Log.lootMap.debug(" === triggerEvent === LootMapEventHaloMonster hp != 0, eventId = {}",eventId);
					return;
				}
				
				sendGetReward(humanId,instEvent.id); // 获取奖励添加到玩家信息并发送
				sendHaloMonsterBuff(eventId, false); // 移除buff
			}break;
			case LootMapEventResource:{//直接触发奖励内容
				InstLootMapEventLevel instEvent = (InstLootMapEventLevel)baseEvent;
				//triggerRmvEvent(instEvent.id);
				sendGetReward(humanId,instEvent.id); // 触发奖励
			}break;
			case LootMapEventBuff:{//触发buff不触发奖励
				InstLootMapEventBuff buf = (InstLootMapEventBuff)baseEvent;
				//triggerRmvEvent(buf.id);
				sendGetBuff(humanId,buf.id); // 触发buff 并发送
			}break;
			case LootMapEventSkill:{// 获取技能
				if(isHumanHasSkill(humanId)){ //获取玩家判断是否有skill
					return;
				}
				InstLootMapEventSkill instEvent = (InstLootMapEventSkill)baseEvent;
				//triggerRmvEvent(instEvent.id);
				getSkill(humanId,instEvent);
				SCLootMapGetSkill.Builder skillBuilder = SCLootMapGetSkill.newBuilder(); //通知玩家
				skillBuilder.setHumanId(humanId);
				skillBuilder.setSkill(instEvent.getSkill().getDLootMapSkill());
				DVector2.Builder v2 = DVector2.newBuilder();
				v2.setX(instEvent.getPosX());
				v2.setY(instEvent.getPosY());
				skillBuilder.setPos(v2);
				InstLootMapManager.inst().sendAllHuman(skillBuilder,this); 
			}break;
		default:
			break;
		}
		
		rmvEventToMap(baseEvent.id);
	}
	
//	/**
//	 * 移除事件
//	 * @param eventId
//	 */
//	private void triggerRmvEvent(int eventId){
//		notifyLootMapEventDisenable(eventId);
//		rmvEventToMap(eventId);
//	}
	
	/**
	 * 添加与发送
	 * @param humanId
	 * @param eventId
	 */
	protected void sendGetReward(long humanId,int eventId){
		if(eventMap.containsKey(eventId) == false) return;
		InstLootMapEventLevel mapEvent = (InstLootMapEventLevel)eventMap.get(eventId);
		if(mapEvent == null) return;
		
		int[] itemSnList = mapEvent.rewardItemSnList;
		int[] itemNumList = mapEvent.rewardItemNumList;
				
		if(itemSnList == null || itemNumList == null){
			Log.lootMap.debug("===sendGetReward item is null humanId = {}",humanId);
			return;
		}
		
		getReward(humanId,itemSnList,itemNumList); // 添加到玩家
		
		sendReward(humanId,eventId,itemSnList,itemNumList); // 发送消息
	}
	
	/**
	 * 触发奖励 添加到玩家身上
	 * @param humanId
	 * @return 返回值 true 存在 false 无奖励
	 */
	protected void getReward(long humanId,int[] itemSnList,int[] itemNumList){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null){
			Log.lootMap.error("===getReward InstLootMapHumanData is null humanId = {}",humanId);
			return;
		}

		int scoreScale = getScoreScale(); // 获取积分基数
		for(int i = 0;i < itemSnList.length;i++){ // 遍历添加到玩家信息里面
			int itemId = itemSnList[i];
			int itemNum = itemNumList[i];
			if(itemId == EMoneyType.lootScore_VALUE){
				itemNum *= scoreScale;
				setOneScore(humanId, human.getOneScore() + itemNum);
				continue;
			}
			human.addItem(itemId,itemNum); // 添加到玩家信息
		}
		

	}
	
	protected void setOneScore(long humanId,int score){}
	
	
	/**
	 * 发送奖励
	 * @param humanId
	 * @param eventId
	 * @param dItemBuilderList
	 */
	private void sendReward(long humanId,int eventId,List<DItem> dItemBuilderList){
		SCLootMapTriggerEvent.Builder builder = SCLootMapTriggerEvent.newBuilder();
		builder.addAllItemList(dItemBuilderList);
		builder.setEventId(eventId);
		InstLootMapEvent mapEvent = eventMap.get(eventId);
		
		DVector2.Builder v =  DVector2.newBuilder();
		v.setX(mapEvent.getPosX());
		v.setY(mapEvent.getPosY());
		builder.setPos(v);
		
		InstLootMapManager.inst().sendHuman(builder, getHumanObj(humanId));
	}
	
	/**
	 * 发送奖励 发送给玩家
	 * @param humanId
	 * @param eventId
	 */
	protected void sendReward(long humanId,int eventId,int[] itemSnList,int[] itemNumList){
		List<DItem> itemList = getRewardItemList(itemSnList,itemNumList);
		if(itemList == null) return;
		sendReward(humanId,eventId,itemList);
	}
	
	/**
	 * 触发奖励 添加到玩家身上
	 * @param humanId
	 * @param rewardSn
	 * @return 返回值 true 存在 false 无奖励
	 */
	protected void getReward(long humanId,int rewardSn){
		if(rewardSn == 0){
			return; // 无奖励
		}
		ConfRewards conf = ConfRewards.get(rewardSn);
		if(conf == null){
			Log.lootMap.error("===ConfRewards no find sn={}", rewardSn);
			return;
		}
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null){
			Log.lootMap.error("===getReward InstLootMapHumanData is null humanId = {}",humanId);
			return;
		}
		
		getReward(humanId, conf.itemSn, conf.itemNum);
	}
	
//	/**
//	 * 发送奖励 发送给玩家
//	 * @param humanId
//	 * @param eventId
//	 * @param rewardSn
//	 */
//	protected void sendReward(long humanId,int eventId,int rewardSn){
//		List<DItem> itemList = getRewardItemList(rewardSn);
//		if(itemList == null) return;
//		sendReward(humanId,eventId,itemList);
//	}
	
	/**
	 * 当该item是积分的时候 判断现在时间节点是否得积分基数
	 * @return
	 */
	private int getScoreScale(){
		return (timeMode == ELootMapTimeMode.LootMapTimeModeDoubleAll ||
				timeMode == ELootMapTimeMode.LootMapTimeModeDoubleGet ||
				timeMode  == ELootMapTimeMode.LootMapTimeModeDoublePve)? 
								2:1;
	}
	
	/**
	 * 通过rewardSn获取一个List<DItem>
	 * @param rewardSn
	 * @return
	 */
	protected List<DItem> getRewardItemList(int rewardSn){
		if(rewardSn == 0){
			return null; // 无奖励
		}
		ConfRewards conf = ConfRewards.get(rewardSn);
		if(conf == null){
			Log.lootMap.error("===getReward ConfProduce is null rewardSn = {}",rewardSn);
			return null;
		}
		return getRewardItemList(conf.itemSn,conf.itemNum);
	}
	
	/**
	 * 获取奖励
	 * @param itemSnList
	 * @param itemNumList
	 * @return
	 */
	protected List<DItem> getRewardItemList(int[] itemSnList,int[] itemNumList){
		int scoreScale = getScoreScale(); // 获取积分基数
		List<DItem> list = new ArrayList<DItem>();
		for(int i = 0;i < itemSnList.length;i++){
			int itemId = itemSnList[i];
			int itemNum = itemNumList[i];
			if(itemId == EMoneyType.lootScore_VALUE){
				itemNum *= scoreScale;
			}
			DItem.Builder itemBuilder = DItem.newBuilder();
			itemBuilder.setItemId(0); // 还没得到 -> 设置为0
			itemBuilder.setItemSn(itemId);
			itemBuilder.setNum(itemNum);
			list.add(itemBuilder.build());
		}
		return list;
	}
	

	/**
	 * 获取buff并发送
	 * @param humanId
	 * @param eventId
	 */
	protected void sendGetBuff(long humanId,int eventId){
		if(eventMap.containsKey(eventId) ==false) return;
		InstLootMapEventBuff instEvent = (InstLootMapEventBuff) eventMap.get(eventId);
		if(instEvent == null) return;
		int buffSn = instEvent.getBuffSn();
		if(buffSn == 0) return;
		if(instEvent.buffTarget != ELootMapBuffTarger.LootMapBufTrigger) return; // 不是针对触发者 不获取与发送
		getBuff(humanId,buffSn);
		sendAddBuff(humanId,buffSn);
		
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null){
			Log.lootMap.error("   ======== getBuff human is null ,humanId = {}",humanId);
			return;
		}
		
		if(InstLootMapBuff.isAttack(buffSn)){ //判断是攻击Buff
			sendHumanAttack(humanId);
		}else if(InstLootMapBuff.isHp(buffSn)){ //判断是回血
			sendHumanHp(humanId);
		}
	}
	
	protected void sendHumanAttack(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null) return;
		SCLootMapHumanAttack.Builder attackBuilder = SCLootMapHumanAttack.newBuilder();
		attackBuilder.setHumanId(humanId);
		attackBuilder.setAttack(human.getAttack());
		InstLootMapManager.inst().sendAllHuman(attackBuilder, this);
	}
	
	protected void sendHumanHp(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null) return;
		SCLootMapHumanHP.Builder hpBuilder = SCLootMapHumanHP.newBuilder();
		hpBuilder.setHumanId(humanId);
		hpBuilder.setHp(human.getHp());
		InstLootMapManager.inst().sendAllHuman(hpBuilder, this);
	}
	
	/**
	 * 添加buff
	 * @param humanId
	 */
	private void getBuff(long humanId,int buffSn){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null){
			Log.lootMap.error("   ======== getBuff human is null ,humanId = {}",humanId);
			return;
		}
		//取得配置
		ConfBuffVk conf = ConfBuffVk.get(buffSn);
		if(conf == null){
			Log.lootMap.error("   ======== getBuff ConfBuffVk.get is null ,buffSn = {}",buffSn);
			return;
		}
		human.addBuff(buffSn,conf.duration);
	}
	
	/**
	 * 添加buff 当 humanId == 0 则是怪物
	 * @param humanId
	 * @param buffSn
	 */
	protected void sendAddBuff(long humanId,int buffSn){
		SCLootMapAddBuff.Builder builder = SCLootMapAddBuff.newBuilder();
		builder.setHumanId(humanId);
		builder.setBuffSn(buffSn);
		InstLootMapManager.inst().sendAllHuman(builder, this);
	}
	
	/**
	 * 发送移除buff 当humanid == 0 怪物
	 * @param humanId
	 * @param buffSnList
	 */
	protected void sendRmvBuff(long humanId,List<Integer> buffSnList){
		SCLootMapRmvBuff.Builder builder = SCLootMapRmvBuff.newBuilder();
		builder.setHumanId(humanId);
		for(int i = 0; i < buffSnList.size();i++){
			builder.addBuffSnList(buffSnList.get(i));
		}
		InstLootMapManager.inst().sendAllHuman(builder, this);
	}
	
	/**
	 * 判断是否有技能
	 * @param humanId
	 * @return
	 */
	private boolean isHumanHasSkill(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null){
			Log.lootMap.error("getSkill human is null humanId = {}, ",humanId);
			return false;
		}
		return human.getSkill() != null;
	}
	/**
	 * 获取技能sn
	 * @param humanId
	 */
	private void getSkill(long humanId,InstLootMapEventSkill skillEvent){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null){
			Log.lootMap.error("getSkill human is null humanId = {}, ",humanId);
			return;
		}
		human.setSkill(skillEvent.getSkill());
	}
	
	/**
	 * 攻击怪物行为
	 * @param humanId
	 * @param evetnId
	 * @param x
	 * @param y
	 * @return
	 */
	public void attackMonster(long humanId,int evetnId,int x,int y){
		//判断事件是否存在
		if(eventMap.containsKey(evetnId) == false){
			return;
		}
		InstLootMapEventMonster monster = (InstLootMapEventMonster)eventMap.get(evetnId);
		if(monster == null){
			Log.lootMap.debug(" ======== attackMonster monster == null evetnId = {}",evetnId);
			return;
		}
		//位置是否正确
		if(monster.isOnePoint(x, y)==false){
			Log.lootMap.debug(" ======== attackMonster monster pos err attX = {} attY = {} eventX = {} eventY = {}",
					x,y,monster.getPosX(),monster.getPosY());
			return;
		}
		
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human.getHp() <= 0){
			Log.lootMap.debug(" ======== attackMonster human hp <= 0 humanId = {}",evetnId); 
			return;
		}
		if(monster.getHP() <= 0){
			Log.lootMap.debug(" ======== attackMonster monster hp <= 0 evenId = {}",humanId);
			return;
		}
		
		int attack = human.getAttack();
		
		//判断范围是否合法
		if(!human.isCorrectRangePoint(monster.getPosX(),monster.getPosY())){
			Log.lootMap.debug(" ======== attackMonster isCorrectRangePoint == false , evenId = {}",humanId);
			return;
		}
		
		monster.attackHuman(attack);
		human.attackMonster(monster.getAttack());
		
		if(monster.getHP() <= 0){
			human.mapBattle();
		}
		
		//攻击行为回执
		SCLootMapAttackMonster.Builder builder = SCLootMapAttackMonster.newBuilder();
		builder.setEventId(evetnId);
		DVector2.Builder pos = DVector2.newBuilder();
		pos.setX(x);
		pos.setY(y);
		builder.setPos(pos);
		builder.setHumanId(humanId);
		InstLootMapManager.inst().sendAllHuman(builder,this);
		
		//怪物血量变更通知
		SCLootMapMonsterHP.Builder monsterBuilder = SCLootMapMonsterHP.newBuilder();
		monsterBuilder.addEventIdList(monster.id);
		monsterBuilder.addHpList(monster.getHP());
		monsterBuilder.setHumanId(humanId);
		InstLootMapManager.inst().sendAllHuman(monsterBuilder,this);
		
		//玩家血量变更通知
		sendHumanHp(humanId);
		
		//玩家攻击变更通知
		if(attack != human.getAttack()){
			sendHumanAttack(humanId);
		}
	}
	
	/**
	 * 是否死亡
	 * @param humanId
	 * @return
	 */
	public boolean isHumanDead(long humanId){
		if(GetHumanData(humanId)==null){
			return false;
		}else{
			return GetHumanData(humanId).getHp() == 0;
		}
	}
	
	
	
	/**
	 * 使用技能
	 * @param humanId
	 */
	public void humanUseSkill(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null){
			Log.lootMap.error(" ======== humanUseSkill human == null humanId = {} ",humanId);
			return;
		}
		
		InstLootMapSkill skill = human.getSkill();
		if(skill == null){
			Log.lootMap.debug(" ======== humanUseSkill human.getSkill is null humanId = {} ",humanId);
			return;
		}
		
		//移除技能
		human.setSkill(null);
		
		//发送通知
		SCLootMapUseSkill.Builder builder = SCLootMapUseSkill.newBuilder();
		builder.setHumanId(humanId);
		builder.setSkill(skill.getDLootMapSkill());
		InstLootMapManager.inst().sendAllHuman(builder, this);
		
		//取得攻击
		int attack = skill.getAttack();
		
		//判断怪物是否为空
		if(monsterMap.size() > 0){
			int[] range = InstLootMapPoint.getRangePoint(
					human.getPosX(), human.getPosY(), 
					skill.getWidth(), skill.getHeight(),
					lootMapWidth,lootMapHeight);
			
			//被打伤的存入
			ArrayList<InstLootMapEventMonster> monsterList = new ArrayList<InstLootMapEventMonster>();
			//判断是否显示 且 在范围内
			for(InstLootMapEventMonster m : monsterMap.values()){
				int posX = m.getPosX();
				int posY = m.getPosY();
				if(InstLootMapPoint.isInRange(posX,posY,range)){
					if(isHasFloor(posX,posY)){
						continue;
					}
					//添加到列表
					monsterList.add(m);
				}
			}
			
			//遍历伤害
			if(monsterList.size() > 0){
				SCLootMapMonsterHP.Builder monsterBuilder = SCLootMapMonsterHP.newBuilder();
				monsterBuilder.setHumanId(humanId);
				//遍历扣血
				for(int i = 0;i< monsterList.size();i++){
					InstLootMapEventMonster m = monsterList.get(i);
					//设置血量
					m.setHp(m.getHP() - attack);
					//设置内容
					monsterBuilder.addEventIdList(m.id);
					monsterBuilder.addHpList(m.getHP());
				}
				//分发事件
				InstLootMapManager.inst().sendAllHuman(monsterBuilder, this);
			}
		}
	}
	
	
	/**
	 * 多人本玩法 Override 该方法 存储离开玩法的人
	 * @param humanId
	 * 被 StageObjectLootMapMultiple @Override
	 */
	public void humanOut(long humanId) {
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human==null) {
			Log.lootMap.error(" ========= humanOut human is null humanId = {}",humanId);
			return;
		}
		
		HumanObject humanObj = getHumanObj(humanId);
		//退出地图
		humanObj.switchState = SwitchState.WaitGlobal;
		StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
		prx.quitToCommon(humanObj, humanObj.stageObj.confMap.sn, "callBack", true);
		prx.listenResult(this::_result_humanOut, "humanObj", humanObj);
	}
	private void _result_humanOut(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		outMap(humanObj.id);
		InstLootMapManager.inst().humanOut(humanObj.id); //从service中移除
		SCLootMapOut.Builder builder = SCLootMapOut.newBuilder();
		builder.setHumanId(humanObj.id);
		InstLootMapManager.inst().sendAllHuman(builder,this); // 广播给其他人
		//已经是结算之后的内容
		closeLootMapStage();
	}
	
	protected void closeLootMapStage(){
		if(isStartGame == false && humanMap.size() == 0){
			destory(); // 销毁地图
		}
	}
	
	/**
	 * 到达了强制关闭时间 -> 强制进行关闭
	 * 多人本已经结算过了
	 * 单人本先进行结算
	 */
	public void forciblyCloseLootMapState(){
		if(isCloseGame) return;
		isCloseGame = true;
		gameSettlement();
		if(isStartGame) isStartGame = false;
		if(humanMap.size()!=0){
			for(Long humanId : humanMap.keySet()){
				humanOut(humanId);
			}
		}
	}
		
	@Override
	public void destory() {
		super.destory();
		
		refreshSet.clear();
		eventMap.clear();
		randomDoorMap.clear();
		monsterMap.clear();
		humanMap.clear();
		humanPosList.clear();
		
		if(oneSecondTimer.isStarted()){
			oneSecondTimer.stop();
		}
		if(one30SecTimer.isStarted()){
			one30SecTimer.stop();
		}
	}
	
	/**
	 * 活动结算
	 */
	protected void gameSettlement(){
		if(!isStartGame) return; // 已经结算过
		isStartGame = false;
		for(InstLootMapHumanData humanData : humanMap.values()){
			settlement2Mail(humanData);
		}
	}
	
	/**
	 * 奖励分发给玩家
	 * @param human
	 */
	protected void settlement2Mail(InstLootMapHumanData human){
		onSettlement2Mail(human);
		sendHumanItme2Mail(human);
	}
	
	/**
	 * 奖励分发给玩家 可能需要添加物品
	 * 子类覆盖
	 * @param human
	 */
	protected void onSettlement2Mail(InstLootMapHumanData human){}
	
	/**
	 * 将玩家身上物品通过邮件进行发送
	 * @param human
	 */
	private void sendHumanItme2Mail(InstLootMapHumanData human){
		List<ProduceVo> itemProduce = human.getAllItemProduceVo();
		sendMail2Human(human.humanId,itemProduce);
	}
	
	/**
	 * 发送邮件给玩家
	 * @param humanId
	 * @param itemProduce
	 */
	private void sendMail2Human(long humanId,List<ProduceVo> itemProduce){
// 		特殊邮件内容：{MailTemplate.sn|rank}
		String detail = "{" + EMailType.MailLooMapAward_VALUE +"}";
		MailManager.inst().sendSysMail(humanId, ParamManager.mailMark,detail, itemProduce);
	}
	
	/**
	 * 准备进入
	 * @param humanId
	 */
	public void readyEnterDoor(long humanId,int eventId){
		if(eventMap.containsKey(eventId) == false){
			Log.lootMap.debug(" ======== readyEnterNextLevel eventMap.containsKey == false ,eventId = {}",eventId);
			return;
		}
		InstLootMapEvent mapEvent = eventMap.get(eventId);
		if(mapEvent == null){
			Log.lootMap.error(" ======== readyEnterNextLevel mapEvent == null ,eventId = {}",eventId);
			return;
		}
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human==null){
			Log.lootMap.error(" ========= readyEnterNextLevel human is null humanId = {}",humanId);
			return;
		}
		
		if(human.isCorrectRangePoint(mapEvent.getPosX(),mapEvent.getPosY()) == false){
			Log.lootMap.debug(" ========= readyEnterNextLevel isCorrectRangePoint is eventX = {} eventY = {}  humanX = {} humanY = {}",
					mapEvent.getPosX(),mapEvent.getPosY(), human.getPosX(), human.getPosY());
			return;
		}
		
		DVector2.Builder v2 = DVector2.newBuilder();
		v2.setX(mapEvent.getPosX());
		v2.setY(mapEvent.getPosY());
		
		SCLootMapReadyEnterDoor.Builder bulider = SCLootMapReadyEnterDoor.newBuilder();
		bulider.setPos(v2);
		bulider.setHumanId(humanId);
		bulider.setEventId(eventId);
		InstLootMapManager.inst().sendAllHuman(bulider,humanId,this);
	}
	

	/**
	 * 进入下一层
	 * @param humanId
	 */
	public void enterDoor(long humanId,int eventId,int x,int y){
		if(eventMap.containsKey(eventId) == false){
			Log.lootMap.debug(" ======== enterDoor eventMap.containsKey == false ,eventId = {}",eventId);
			return;
		}
		InstLootMapEvent mapEvent = eventMap.get(eventId);
		if(mapEvent == null){
			Log.lootMap.error(" ======== enterDoor mapEvent == null ,eventId = {}",eventId);
			return;
		}
		switch(mapEvent.getEventType()){
			case LootMapEventNextDoor:{
				enterNextLevelDoor(humanId);
			}break;
			case LootMapEventEggDoor:{
				enterEggDoor(humanId);
			}break;
			case LootMapEventRandomDoor:{
				enterRandomDoor(humanId,eventId);
			}break;
			default:{
				Log.lootMap.debug(" ======== enterDoor mapEvent.getEventType not door ,eventId = {} eventType = {}",eventId,mapEvent.getEventType());
			}break;
		}
	}
	
	/**
	 * 进入下一层 子类覆盖
	 * @param humanId
	 */
	protected void enterNextLevelDoor(long humanId){
		
	}
	
	/**
	 * 进入菜单关 子类覆盖
	 * @param humanId
	 */
	protected void enterEggDoor(long humanId){
		
	}
	
	/**
	 * 进入随机传送门
	 * @param humanId
	 * @param eventId
	 */
	protected void enterRandomDoor(long humanId,int eventId){
		if(randomDoorMap.containsKey(eventId) == false){
			return;
		}
		int randomValue = (int)(Math.random()*(randomDoorMap.size() -1)); // 取出随机内容
		int tempValue = 0; // 随机索引
		for(InstLootMapEventDoor door : randomDoorMap.values()){ // 遍历
			if(door.id == eventId) continue; // 与当前相等
			if(tempValue++ == randomValue){ // 如果是这个索引则设置坐标
				setHumanPos(humanId,door.getPosX(),door.getPosY());
				//InstLootMapHumanData human = GetHumanData(humanId);
				
				SCLootMapSetPos.Builder builder = SCLootMapSetPos.newBuilder();
				builder.setHumanId(humanId);
				DVector2.Builder v2 = DVector2.newBuilder();
				v2.setX(door.getPosX());
				v2.setY(door.getPosY());
				builder.setPos(v2);
				InstLootMapManager.inst().sendAllHuman(builder,this);
				break;
			}
		}
	}
	
	/**
	 * 热更补充方法
	 * @param param
	 * @param str
	 */
	public void resrveFunction(int param,String str){
		
	}
}
