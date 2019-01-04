package game.worldsrv.instLootMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import core.Port;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Distr;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import game.msg.Define.ECrossFightType;
import game.msg.Define.ELootMapLevelLimitType;
import game.msg.Define.ELootMapType;
//import game.msg.MsgInstLootMap.SCLootMapReadyEnter;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfLootMap;
import game.worldsrv.config.ConfMap;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LootMapType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.instLootMap.Room.InstLootMapSignUpHuman;
import game.worldsrv.instLootMap.Room.InstLootMapSignUpRoom;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
//import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

@DistrClass(servId = D.SERV_LOOTMAP, importClass = {Human.class})
public class InstLootMapService extends GameServiceBase {
	//当前报名房间id
	private Long signUpRoomID = 0L;
	//最多空闲的报名房间
	private int maxFreeSignUpRoom = 50;
	//空闲报名房间
	private Stack<InstLootMapSignUpRoom> freeSignUpRoomStack = new Stack<InstLootMapSignUpRoom>();
	//报名房间 roomId,room
	private Map<Long,InstLootMapSignUpRoom> signUpRoomMap = new HashMap<>();
	//human 存放玩家所在的room的id key = humanId, value = roomId
	private Map<Long,Long> huamanSignUpMap = new HashMap<>();
	//huamanId,roomId
	private Map<Long,Long> huamanGameMap = new HashMap<>();
	
	public InstLootMapService(GamePort port) {
		super(port);
	}

	TickTimer oneSecondTimer = new TickTimer();
	
	@Override
	protected void init() {
		
	}
	
	@Override
	public void pulseOverride() {
		if(isNeedOneSecond() == false){
			if(oneSecondTimer.isStarted()){
				oneSecondTimer.stop();
			}
			return; // 如果不需要计时 则return
		}
		
		if(oneSecondTimer.isStarted() == false){
			oneSecondTimer.start(Time.SEC);
		}
		
		if(oneSecondTimer.isPeriod(Port.getTime())){
			signUpRoomOneSecond();
		}
	}
	
	/**
	 * 是否需要service进行计时
	 * @return
	 */
	private boolean isNeedOneSecond(){
		return huamanSignUpMap.size() != 0;
	}
	
	/**
	 *房间经过1s
	 */
	private void signUpRoomOneSecond(){
		if(signUpRoomMap.size() > 0){
			for(InstLootMapSignUpRoom room : signUpRoomMap.values()){
				room.oneSecond();
			}
		}
	}

	
	@DistrMethod
	public void intoSignUpRoom(Human human,int actSn){
		InstLootMapSignUpRoom room = null;
		if(huamanSignUpMap.containsKey(human.getId())){//判断是否已经在房间
			Log.lootMap.error("抢夺本pvp房间报名失败 ：已经存在报名房间 humanId = {}",human.getId());
			return;
		}else {//未在房间
			room = getSignUpRoom(human);
			if(room == null){//没有房间则获取一个空房间
				room = getFreeSignUpRoom(human);
				room.actInstSn = actSn;
			}
			if(!room.addPlayer(human.getId())){ //添加玩家进房间记录
				return;
			}
			huamanSignUpMap.put(human.getId(),room.roomId);
			long pid = port.createReturnAsync();// 创建一个异步返回
			//对房间中的其他玩家进行通知
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.getInfoList(room.playerArray);
			prx.listenResult(this::_result_intoSignUpRoom,"pid",pid,"human",human,"room",room);
		}
	}

	
	private void _result_intoSignUpRoom(Param results, Param context){
		long pid = Utils.getParamValue(context, "pid", -1L);
		Human human = Utils.getParamValue(context, "human",null);
		InstLootMapSignUpRoom room = Utils.getParamValue(context, "room", null);
		//获取本次进入房间的玩家id
		//查询结果
		ArrayList<HumanGlobalInfo> hgList = results.get();
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				if (hg.id != human.getId()) { //通知其他玩家有玩家加入
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,hg.id);
					InstLootMapSignUpHuman humanData = new InstLootMapSignUpHuman(human);
					prxHumanObj.lootMapIntoSignUpRoom(humanData);
				}
			}
		}
		port.returnsAsync(pid,"result",true,"hgList",hgList,"room",room);
	}
	
	//获取一个未满的房间
	//符合该玩家条件的免费房间 例如等级符合
	//可以返回null 当返回null getFreeSignUpRoom 创建一个房间
	InstLootMapSignUpRoom getSignUpRoom(Human human){
		InstLootMapSignUpRoom room = null;
		Set<Long> keys = signUpRoomMap.keySet();
		for (Long key : keys) {  
			InstLootMapSignUpRoom tempRoom = signUpRoomMap.get(key);
			//判断该房间是否符合
			//FIXME 现阶段都符合
			if(tempRoom.isFull()==false){
				room = tempRoom;
				break;
			}
		}
		return room;
	}
	
	//获取一个空房间
	//符合该玩家条件的免费房间 例如等级符合
	InstLootMapSignUpRoom getFreeSignUpRoom(Human human){
		InstLootMapSignUpRoom room = null;
		if(freeSignUpRoomStack.isEmpty() == false){
			room = freeSignUpRoomStack.pop();
			room.openRoom();
			room.setLevel(human.getLevel());
		}else{
			room = new InstLootMapSignUpRoom();
		}
		
		//FIXME 等级限制等等 可能需要修改限制条件
		
		//添加房间记录
		++signUpRoomID;
		room.roomId = signUpRoomID;
		signUpRoomMap.put(signUpRoomID,room);
		return room;
	}
	
	//离开房间
	@DistrMethod
	public void leaveSignUpRoom(Human human){
		long humanId = human.getId();
		leaveSignUpRoom(humanId);
	}
	
	/**
	 * 移除humanId
	 * @param humanId
	 */
	private void leaveSignUpRoom(long humanId){
		InstLootMapSignUpRoom room = null;
		if(huamanSignUpMap.containsKey(humanId) == false){//判断是否已经在房间
			Log.table.error("   ========   leaveSignUpRoom huamanSignUpMap.containsKey == false , humanId = {}",humanId);
			port.returns(); // 已经没在报名房间了
			return;
		}
		//获取房间id
		Long roomId = huamanSignUpMap.get(humanId);
		if(signUpRoomMap.containsKey(roomId)==false){//判断是否有改房间存在
			Log.table.error("   ========   leaveSignUpRoom signUpRoomMap.containsKey == false , roomId = {}",roomId);
			port.returns(); //已经不存在这个房间了
			return;
		}
		room = signUpRoomMap.get(roomId); // 获取房间
		if(room.rmvPlayer(humanId) == false){ // 移除玩家
			return;
		}
		huamanSignUpMap.remove(humanId); // 从map中移除
		if(room.isEmpty()){//房间为空 关闭房间
			closeSignUpRoom(room);
		}else{//房间不为空 通知其他人
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.getInfoList(room.playerArray);
			prx.listenResult(this::_result_leaveSignUpRoom,"humanId",humanId);
		}
		port.returns();
	}
	
	//通知房间其他人退出
	private void _result_leaveSignUpRoom(Param results, Param context){
		long humanId = Utils.getParamValue(context, "humanId", null);
		//查询结果
		ArrayList<HumanGlobalInfo> hgList = results.get();
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				//通知其他玩家有玩家退出
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,hg.id);
				prxHumanObj.lootMapLeaveSignUpRoom(humanId);
			}
		}
	}

	/**
	 * 房间超时
	 * @param roomId
	 */
	@DistrMethod
	public void signUpRoomTimeOut(long roomId){
		if(signUpRoomMap.containsKey(roomId)){
			InstLootMapSignUpRoom room = signUpRoomMap.get(roomId);
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.getInfoList(room.playerArray);
			prx.listenResult(this::_result_signUpRoomTimeOut,"roomId",roomId);
		}
	}

	private void _result_signUpRoomTimeOut(Param results, Param context){
		ArrayList<HumanGlobalInfo> hgList = results.get();
		long roomId = Utils.getParamValue(context, "roomId",-1L);
		InstLootMapSignUpRoom room = signUpRoomMap.get(roomId);
		if(room == null) return;
		boolean isNeedSendMsg = room.isFull(); // 是否满人
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				//通知其他玩家有玩家退出
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,hg.id);
				prxHumanObj.lootMapSignUpRoomTimeOut();
				if(isNeedSendMsg){ // 满人发送开启协议
					isNeedSendMsg = false;
					int actInstSn = room.actInstSn;
					if(actInstSn!=-1){
						prxHumanObj.sendSCLootMapReadyEnter(actInstSn);
					}
				}
			}
		}
	}
	
	//关闭报名房间
	private void closeSignUpRoom(InstLootMapSignUpRoom room){
		//删除玩家关联
		if(room.isEmpty() == false){
			for(Long id : room.playerArray){
				if(huamanSignUpMap.containsKey(id)){
					huamanSignUpMap.remove(id);
				}
			}
		}
		//关闭房间
		room.closeRoom();
		//移除房间id关联
		signUpRoomMap.remove(room.roomId);
		//数量允许 存入stack
		if(freeSignUpRoomStack.size() < maxFreeSignUpRoom){
			freeSignUpRoomStack.push(room);
		}
	}
	
	@DistrMethod
	public void rmvGameStage(long stageId){
		if(huamanGameMap.size()!=0){
			for(Long humanId : huamanGameMap.keySet()){
				if(huamanGameMap.get(humanId) == stageId){
					huamanGameMap.remove(humanId);
				}
			}
		}
	}
	
	/**
	 * 进入单人本
	 * @param human 玩家
	 * @param actInstSn 活动sn
	 */
	@DistrMethod
	public void enterSingle(Human human, int actInstSn) {
		if(huamanGameMap.containsKey(human.getId()) == false) {
			ConfInstActConfig actConf = ConfInstActConfig.get(actInstSn); //已经有了活动id
			int stageSn = actConf.instSn; //获取副本sn
			int lootMapSN = InstLootMapManager.inst().getRandomLootMapConfSn(ELootMapType.LootMapSingle_VALUE,1); //获取随机组
			//int lootMapSN = ConfigKeyFormula.getLootMapSn(1,group,ELootMapType.LootMapSingle); //获取地图sn
			ConfLootMap lootMapconf = ConfLootMap.get(lootMapSN); //获取sn对应的配置项目
			int mapSn = lootMapconf.mapSn; //获取mapSn
			ELootMapLevelLimitType lvType = LootMapType.getLevelLimitType(human.getLevel()); // lvType
			ConfInstStage confInstStage = ConfInstStage.get(stageSn);
			if (confInstStage == null) {
				Log.lootMap.error("创建抢夺本副本失败：createStageInstLootMap() no find stageSn={}", stageSn);
				return;// 副本关卡Sn不存在
			}
			
			// 地图配置信息
			ConfMap confMap = ConfMap.get(mapSn);
			if (confMap == null) {
				Log.lootMap.error("创建抢夺本副本失败：createStageInstLootMap() no find mapSn={}", mapSn);
				return;// 地图Sn不存在
			}
			
			String portId = StageManager.inst().getStagePortId();
			StageServiceProxy proxy = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
			proxy.createStageInstLootMap(stageSn, mapSn, ECrossFightType.FIGHT_LOOTMAP_SINGLE_VALUE, 
					1, lvType.getNumber(), lootMapSN);
			proxy.listenResult(this::_result_createLootMapStageInst, 
					"actInstSn", actInstSn,
					"mapSn", mapSn,
					"mapType", ELootMapType.LootMapSingle,
					"humanId", human.getId()
			);
		}
	}
	
	/**
	 * 进入多人本
	 * @param humanId 玩家id
	 * @param actInstSn 活动sn
	 */
	@DistrMethod
	public void enterMultip(long humanId, int actInstSn) {
		if(huamanSignUpMap.containsKey(humanId) == false) {
			Log.lootMap.error("pvp报名房间开启游戏失败 ：找不到玩家所在报名房间 humanId = {}",humanId);
			return;
		}
		long signUpRoomId = huamanSignUpMap.get(humanId); // 获取报名房间id
		if(signUpRoomMap.containsKey(signUpRoomId) == false) {
			Log.lootMap.error("pvp报名房间开启游戏失败 ：找不到报名房间 signUpRoomId = {}",signUpRoomId);
			return;
		} else {
			InstLootMapSignUpRoom room = signUpRoomMap.get(signUpRoomId); //取得房间
			if(room.isFull()==false){
				Log.lootMap.error("pvp报名房间开启游戏失败 ：房间没满 signUpRoomId = {}",signUpRoomId);
				return;
			}
			ConfInstActConfig actConf = ConfInstActConfig.get(actInstSn); //已经有了活动id
			int stageSn = actConf.instSn; //获取副本sn
			int lootMapSn = InstLootMapManager.inst().getRandomLootMapConfSn(ELootMapType.LootMapMultip_VALUE,1); //获取随机组
			//int lootMapSn = ConfigKeyFormula.getLootMapSn(1,group,ELootMapType.LootMapMultip); //获取地图sn
			ConfLootMap lootMapconf = ConfLootMap.get(lootMapSn); //获取sn对应的配置项目
			int mapSn = lootMapconf.mapSn; //获取mapSn
			ELootMapLevelLimitType lvType = LootMapType.getLevelLimitType(room.getLevel()); // lvType
			
			ConfInstStage confInstStage = ConfInstStage.get(stageSn);
			if (confInstStage == null) {
				Log.lootMap.error("创建抢夺本副本失败：createStageInstLootMap() no find stageSn={}", stageSn);
				return;// 副本关卡Sn不存在
			}
			
			// 地图配置信息
			ConfMap confMap = ConfMap.get(mapSn);
			if (confMap == null) {
				Log.lootMap.error("创建抢夺本副本失败：createStageInstLootMap() no find mapSn={}", mapSn);
				return;// 地图Sn不存在
			}
			
			String portId = StageManager.inst().getStagePortId();
			StageServiceProxy proxy = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
			proxy.createStageInstLootMap(stageSn, mapSn, ECrossFightType.FIGHT_LOOTMAP_MULTIPLE_VALUE, 
					room.getFullSize(), lvType.getNumber(), lootMapSn);
			proxy.listenResult(this::_result_createLootMapStageInst, 
					"actInstSn", actInstSn,
					"mapSn", mapSn,
					"mapType",ELootMapType.LootMapMultip,
					"signUpRoomId",signUpRoomId
			);
		}
	}
	
	private void _result_createLootMapStageInst(Param results, Param context){
		//通过 StageService createStageInstLootMap
		//stage已经start
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		ELootMapType mapType = Utils.getParamValue(context, "mapType", ELootMapType.LootMapNull);
		//存储玩家内容与stageId的关系
		List<Long> humanArry = new ArrayList<>();
		
		int actInstSn = Utils.getParamValue(context,"actInstSn",0);
		if(actInstSn == 0){
			Log.lootMap.error(" ========= _result_createLootMapStageInst actRepSn == 0");
			return;
		}
		
		ConfInstActConfig actConf = ConfInstActConfig.get(actInstSn);
		if(actConf == null){
			Log.lootMap.error(" ========= _result_createLootMapStageInst actConf == null");
			return;
		}
		ConfInstStage confInstStage = ConfInstStage.get(actConf.instSn);
		if(confInstStage == null){
			Log.lootMap.error(" ========= _result_createLootMapStageInst confInstStage == null");
			return;
		}
		
		int costItemSn = confInstStage.costItemSN;
		int costItemNum = confInstStage.costItemNumber;
		
		switch(mapType) {
			case LootMapSingle: {// 单人
				long humanId = Utils.getParamValue(context,"humanId",-1L);
				//插入查询列表
				humanArry.add(humanId);
				//设置进入最多人数
			} break;
			case LootMapMultip: {// 多人
				//报名房间
				long signUpRoomId = Utils.getParamValue(context,"signUpRoomId",0L);
				if(signUpRoomId != 0) {
					InstLootMapSignUpRoom room = signUpRoomMap.get(signUpRoomId);
					for(long humanId : room.playerArray) {
						//插入查询列表
						humanArry.add(humanId);
					}
					closeSignUpRoom(room);
				}
			} break;
		default:
			break;
		}
		
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfoList(humanArry);
		prx.listenResult(this::_result_createLootMapStageInst2,
				"stageId", stageId,
				"costItemSn", costItemSn,
				"costItemNum", costItemNum,
				"mapType", mapType);
	}
	
	/**
	 * 查询玩家结果
	 * @param results
	 * @param context
	 */
	private void _result_createLootMapStageInst2(Param results, Param context){
		long stageId = Utils.getParamValue(context, "stageId", -1L);
		int costItemSn = Utils.getParamValue(context, "costItemSn", -1);
		int costItemNum = Utils.getParamValue(context, "costItemNum", -1);
		ELootMapType mapType = Utils.getParamValue(context, "mapType", ELootMapType.LootMapNull);
		
		ArrayList<HumanGlobalInfo> hgList = results.get();
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				if(costItemSn == -1 || costItemSn == 0) {
					// 不用扣消耗道具
					prxLootMapEnter(hg, stageId, mapType);
					huamanGameMap.put(hg.id, stageId);
				} else {
					// 需要扣消耗道具
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,hg.id);
					prxHumanObj.lootMapCostIntoItem(costItemSn, costItemNum);
					prxHumanObj.listenResult(this::_result_createLootMapStageInst3,
							"stageId", stageId,
							"hg", hg,
							"mapType", mapType);
				}
			}
		}
	}
	
	/**
	 * 扣除道具是否成功 -> 成功切换
	 * @param results
	 * @param context
	 */
	private void _result_createLootMapStageInst3(Param results, Param context){
		long stageId = Utils.getParamValue(context, "stageId", -1L);
		boolean result = Utils.getParamValue(results,"result",false);
		ELootMapType mapType = Utils.getParamValue(context, "mapType", ELootMapType.LootMapNull);
		
		if(result == false){
			return;
		}
		HumanGlobalInfo hg = Utils.getParamValue(context, "hg",null);
		if(hg != null){
			prxLootMapEnter(hg, stageId, mapType);
			huamanGameMap.put(hg.id,stageId);
		}
	}
	
	/**
	 * 进入stage
	 * @param hg
	 * @param stageId
	 */
	private void prxLootMapEnter(HumanGlobalInfo hg, long stageId, ELootMapType mapType) {
		HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId,hg.id);
		prxHumanObj.lootMapEnter(stageId, mapType);
	}
	
	/**
	 * 玩家退出
	 * @param huamanId
	 */
	@DistrMethod
	public void humanOut(long huamanId){
		if(huamanGameMap.containsKey(huamanId)){
			huamanGameMap.remove(huamanId);
		}
	}
	
	/**
	 * 玩家掉线
	 * @param humanId
	 */
	@DistrMethod
	public void humanSignOut(long humanId){
		humanSignOutSignUpRoom(humanId);
	}
	
	/**
	 * 登出房间
	 */
	private void humanSignOutSignUpRoom(long humanId){
		if(huamanSignUpMap.containsKey(humanId)){
			long roomId = huamanSignUpMap.get(humanId);
			if(signUpRoomMap.containsKey(roomId)){
				leaveSignUpRoom(humanId);
			}
		}
		
	}
	
	/**
	 * 热更方法
	 * @param param
	 * @param str
	 */
	@DistrMethod()
	public void resrveFunction(int param,String str){
		
	}
}
