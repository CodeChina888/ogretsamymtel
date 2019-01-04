package game.worldsrv.instLootMap.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.support.Param;
import game.msg.Define;
import game.msg.Define.DMemberInfo;
import game.msg.Define.ELootMapKillHornerType;
import game.msg.Define.ELootMapTimeMode;
import game.msg.Define.ELootMapType;
import game.msg.Define.EMoneyType;
import game.msg.MsgInstLootMap.SCLootMapGameTime;
import game.msg.MsgInstLootMap.SCLootMapHumanRevival;
import game.msg.MsgInstLootMap.SCLootMapKill;
import game.msg.MsgInstLootMap.SCLootMapMultipleEnd;
import game.msg.MsgInstLootMap.SCLootMapPkState;
import game.msg.MsgInstLootMap.SCLootMapProtectState;
import game.msg.MsgInstLootMap.SCLootMapScoreChange;
import game.msg.MsgInstLootMap.SCLootMapScoreRank;
import game.msg.MsgInstLootMap.SCLootMapScoreReward;
import game.msg.MsgInstLootMap.SCLootMapTimeMod;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfLootMapKillHonor;
import game.worldsrv.config.ConfLootMapRankReward;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.enumType.RankType;
import game.worldsrv.instLootMap.InstLootMapManager;
import game.worldsrv.instLootMap.Game.InstLootMapHumanData;
import game.worldsrv.param.ParamManager;
import game.worldsrv.pk.PKHumanInfo;
import game.worldsrv.rank.RankData;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.stage.StagePort;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

public class StageObjectLootMapMultiple extends StageObjectLootMap{
	
	class ModeTimer{
		ELootMapTimeMode mode;
		int triggerSecond;
	}
	
	public Long roomId; // 房间内容
	ArrayList<ModeTimer> timerArray = new ArrayList<>(); // 时间模式
	private Map<Long,InstLootMapHumanData> outHumanMap = new HashMap<>(); // 中途离开的玩家map
	private List<Long> scoreRank = new ArrayList<>();
	private long firstKillerId = 0L; // 第一滴血玩家
	
	public StageObjectLootMapMultiple(StagePort port, long stageId, int stageSn, int mapSn, int fightType, 
			int humanNumber, int lvType, int lootMapSn)  {
		super(port, stageId, stageSn, mapSn, fightType, humanNumber, lvType, lootMapSn);
		
		//格子数量
		int[] size =  ParamManager.lootMapMultipleSize;
		this.lootMapWidth = size[0];
		this.lootMapHeight = size[1];
		mapInit(lootMapSn);
		
		lootMapType = ELootMapType.LootMapMultip;
		for(int i = 0; i < ParamManager.lootMapTimeModeTime.length;i++){ // 遍历生成
			ModeTimer t = new ModeTimer();
			t.mode = ELootMapTimeMode.valueOf(ParamManager.lootMapTimeModeType[i]); // 时间type
			t.triggerSecond = ParamManager.lootMapTimeModeTime[i];
			timerArray.add(t);
		}
	}
	
	@Override
	public void destory() {
		super.destory();
		
		timerArray.clear();
		outHumanMap.clear();
	}
	
	@Override
	public void startGame(){
		super.startGame();
		startProtected();
	}
	
	/**
	 * 游戏开始 设置保护时间
	 */
	private void startProtected(){
		for(InstLootMapHumanData data:getAllHumanData()){
			data.protectedTime = ParamManager.lootMapStartPortectedTime;
			sendProtectState(data.humanId,true);
		}
	}
	
	@Override
	protected InstLootMapHumanData GetHumanData(long humanId){
		InstLootMapHumanData data  = super.GetHumanData(humanId);
		if(data == null){
			if(outHumanMap.containsKey(humanId)){
				data = outHumanMap.get(humanId);
			}
		}
		return data;
	}
	
	/**
	 * 获取pk玩家信息
	 * @return
	 */
	public PKHumanInfo getPKHumanInfo(long humanId) {
		InstLootMapHumanData data = GetHumanData(humanId);
		if (data == null) {
			return null;
		}
		return data.getPKHumanInfo();
	}
	
	/**
	 * 发送pk状态
	 * @param triggerHumanId
	 * @param targetHumanId
	 * @param isPk
	 */
	public void sendPkStste(long triggerHumanId, long targetHumanId, boolean isPk){
		// 设置两方的谁是触发方，及设置次数
		this.setPKTrigger(triggerHumanId, targetHumanId);
		
		//发起pk的人状态是保护时候 先取消保护状态
		InstLootMapHumanData human = GetHumanData(triggerHumanId);
		if(human.isProtecting){
			sendProtectState(triggerHumanId,false);
		}
		
		setPkState(triggerHumanId,isPk);
		setPkState(targetHumanId,isPk);
		
		//发送其他玩家PK状态
		SCLootMapPkState.Builder pkStateBuilder = SCLootMapPkState.newBuilder();
		pkStateBuilder.addHumanIdList(triggerHumanId);
		pkStateBuilder.addHumanIdList(targetHumanId);
		pkStateBuilder.setIsPk(isPk);
		InstLootMapManager.inst().sendAllHuman(pkStateBuilder,this);
	}
	
	/**
	 * 设置发起挑战的记录
	 * @param triggerHumanId 发起挑战的玩家id
	 * @param targetHumanId 被挑战的玩家id
	 */
	private void setPKTrigger(long triggerHumanId,long targetHumanId){
		InstLootMapHumanData triggerHuman = GetHumanData(triggerHumanId);
		InstLootMapHumanData targetHuman = GetHumanData(targetHumanId);
		
		triggerHuman.isPKTrigger = true;
		targetHuman.isPKTrigger = false;
		
		//次数 -- 
		triggerHuman.activeRobNum--;
		targetHuman.passiveRobNum--;
	}
	
	/**
	 * 判断pk是否正确
	 * @param humanId
	 * @param isTrigger
	 * @return
	 */
	public boolean isPkErr(long humanId, boolean isTrigger){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null){ // 玩家是否为空
			Log.lootMap.error(" ========= isPkError human is null humanId = {}  [isTrigger] = {}",humanId,isTrigger);
			return true;
		}
		if(human.isPking){ // 是否在pk
			HumanObject humanObj = getHumanObj(humanId);
			if(humanObj!=null){
				int msgId = isTrigger?568001:568002; //正在pk,对方正在pk
				humanObj.sendSysMsg(msgId);
			}
			return true;
		}
		if(isTrigger){ // 是否是发起者
			if(human.activeRobNum <= 0){ // 主动挑战次数是否不足
				HumanObject humanObj = getHumanObj(humanId);
				if(humanObj!=null){
					humanObj.sendSysMsg(568003);
				}
				return true;
			}
			if(human.getHp() < ParamManager.lootMapBattleHpCost){ // 发起挑战消耗是否不足
				HumanObject humanObj = getHumanObj(humanId);
				if(humanObj!=null){
					humanObj.sendSysMsg(568005);
				}
				return true;
			}
		}else{
			if(human.isProtecting){ // 是否保护状态
				HumanObject humanObj = getHumanObj(humanId);
				if(humanObj!=null){
					humanObj.sendSysMsg(568006);
				}
				return true ;
			}
			if(human.getHp() == 0){ // 被挑战者是否死亡
				HumanObject humanObj = getHumanObj(humanId);
				if(humanObj!=null){
					humanObj.sendSysMsg(568007);
				}
				return true;
			}
			if(human.passiveRobNum <= 0){ // 被挑战次数是否不足
				HumanObject humanObj = getHumanObj(humanId);
				if(humanObj!=null){
					humanObj.sendSysMsg(568004);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取该房间玩家ID
	 * @return
	 */
	public Long[] getRoomAllHumanId(){
		return (Long[])humanMap.keySet().toArray();
	}
	
	@Override
	protected void on30Sec(){
		super.on30Sec();
		if(!isStartGame){return;}
		sendGameTime();
	}
		
	@Override
	protected void onOneSecond() {
		super.onOneSecond();
		
		if(!isStartGame) 
			return;
		//房间的时间到了
		if(gameStartTime == ParamManager.lootMapMultipleTime){
			activityTimeUp();
			return;
		}
		//死亡玩家经过1s
		foreachHumanOneSecond();
		//时间模式是否有改变
		timeModeOneSecond();
	}
	
	/**
	 * 遍历玩家的操作 保护时间 死亡时间
	 */
	private void foreachHumanOneSecond(){
		for(InstLootMapHumanData human : getAllHumanData()){
			
			if(isHumanDead(human.humanId)){// 死亡时间判断
				if(++human.deadTime == ParamManager.lootMapWaitRevivalTime){
					humanRevival(human.humanId);
				}
			}
			
			if(human.isProtecting){//保护时间判断
				if(--human.protectedTime <= 0){
					sendProtectState(human.humanId,false);
				}
			}
		}
	}
		
	/**
	 * 玩家复活
	 * @param humanId
	 * @return
	 */
	public void humanRevival(long humanId){
		if(isHumanDead(humanId)){
			InstLootMapHumanData human = GetHumanData(humanId);
			//玩家复活
			human.revival();
			//发送消息
			SCLootMapHumanRevival.Builder builder = SCLootMapHumanRevival.newBuilder();
			builder.setHp(human.getHp());
			builder.setHumanId(humanId);
			builder.setAttack(human.getAttack());
			InstLootMapManager.inst().sendAllHuman(builder,this);
		}
	}
	
	/**
	 * 时间模式 经过了1S
	 */
	private void timeModeOneSecond(){
		if(timerArray.size() == 0) return;
		for(int i = 0;i < timerArray.size();i++){
			ModeTimer t = timerArray.get(i);
			if(t.triggerSecond <= gameStartTime){
				timeMode = t.mode;
				
				SCLootMapTimeMod.Builder builder = SCLootMapTimeMod.newBuilder();
				builder.setModetype(t.mode);
				InstLootMapManager.inst().sendAllHuman(builder,this); // 发送协议
				timerArray.remove(i);
				break;
			}
		}
	}
	
	/**
	 * 进入游戏 请求数据
	 */
	@Override
	public void sendGameEnterLevelData(long humanId){
		super.sendGameEnterLevelData(humanId);
	}
	
	/**
	 * 每30秒进行更新
	 */
	private void sendGameTime(){
		InstLootMapManager.inst().sendAllHuman(getGameTimeBuilder(), this);		
	}
		
	/**
	 * 向单个玩家发送现在时间
	 */
	public void sendGameTime(long humanId){
		InstLootMapManager.inst().sendHuman(getGameTimeBuilder(),getHumanObj(humanId));
	}
	
	/**
	 * 获取当前剩余时间
	 * @return
	 */
	private SCLootMapGameTime.Builder getGameTimeBuilder(){
		SCLootMapGameTime.Builder builder = SCLootMapGameTime.newBuilder();
		builder.setGameTime(ParamManager.lootMapMultipleTime - gameStartTime);
		return builder;
	}
	
	/**
	 * 获取战后奖励
	 */
	public Map<Integer, Integer> getResultItem(long winHumanId, long loseHumanId) {
		//InstLootMapHumanData winHuman = GetHumanData(winHumanId);
		InstLootMapHumanData loseHuman = GetHumanData(loseHumanId);
		//获取失败者积分
		int score = getFailScore(loseHuman.getOneScore(),loseHuman.killNumber);
		Map<Integer, Integer> bag = new HashMap<>();
		bag.put(EMoneyType.lootScore_VALUE, score);
		return bag;
	}
	
	/**
	 * 设置pk状态
	 * @param humanId
	 * @param isPk
	 */
	private void setPkState(long humanId,boolean isPk){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null) 
			return;
		human.isPking = isPk;
	}
		
	/**
	 * 设置保护状态
	 * @param humanId
	 * @param isProtect
	 */
	private void setProtectState(long humanId,boolean isProtect){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null) return;
		human.isProtecting = isProtect;
		if(isProtect == false){
			human.protectedTime = 0;
		}
	}
	
	private void sendProtectState(long humanId,boolean isProtect){
		setProtectState(humanId,isProtect);
		SCLootMapProtectState.Builder protectKillBuilder = SCLootMapProtectState.newBuilder();
		protectKillBuilder.setHumanId(humanId);
		protectKillBuilder.setIsProtect(isProtect);
		InstLootMapManager.inst().sendAllHuman(protectKillBuilder,this);
	}
	
	/**
	 * PK结束
	 * @param killerId
	 * @param beKillId
	 */
	public void battleEnd(long killerId, long beKillId) {
		//当活动结算了积分不进行变化
		if(!isStartGame){
			return;
		}
		//获取杀人者
		InstLootMapHumanData killerHuman = GetHumanData(killerId);
		if(killerHuman == null){
			Log.lootMap.error(" ======== pkEnd killerHuman is null killerId = {}",killerId);
			return;
		}
		//获取被杀者
		InstLootMapHumanData beKillHuman = GetHumanData(beKillId);
		if(beKillHuman == null){
			Log.lootMap.error(" ======== pkEnd beKillHuman is null beKillId = {}",killerHuman);
			return;
		}
		//广播内容
		pkEndSettlementScore(killerHuman, beKillHuman); // 积分结算 
	}
	
	/**
	 * 由战斗返回抢夺本
	 * @param humanId
	 * @param isWin
	 */
	public void backMap(long humanId,boolean isWin){
		InstLootMapHumanData humanData = GetHumanData(humanId); // 获取human
		if(humanData == null) {
			Log.lootMap.error("   ========   backMap human = null , humanId = {}",humanId);
			return;
		}
		//战后血量改变
		int hpChange = isWin ? ParamManager.lootMapBattleWinHpChange: ParamManager.lootMapBattleFailHpChange;
		if(hpChange!=0){//血量不为0则进行变化
			humanData.setHp(humanData.getHp()+hpChange);
			sendHumanHp(humanId);
		}
		//判断是否pk发起者
		if(humanData.isPKTrigger){
			humanData.setHp(humanData.getHp() - ParamManager.lootMapBattleHpCost);
			sendHumanHp(humanData.humanId);
			humanData.isPKTrigger = false;
		}
		//设置pk状态
		setPkState(humanId,false); 
		//分发pk
		SCLootMapPkState.Builder pkStateBuilder = SCLootMapPkState.newBuilder();
		pkStateBuilder.addHumanIdList(humanId);
		pkStateBuilder.setIsPk(false);
		InstLootMapManager.inst().sendAllHuman(pkStateBuilder,this);
		//当结算后不死亡 则进行保护状态
		if(humanData.getHp() != 0){
			humanData.protectedTime = isWin ? ParamManager.lootMapBattleWintPortectedTime :ParamManager.lootMapBattleFailtPortectedTime;
			sendProtectState(humanData.humanId,true);
		}
	}
	
	/**
	 * 战斗结束 结算积分
	 */
	private void pkEndSettlementScore(InstLootMapHumanData killerHuman,InstLootMapHumanData beKillHuman){
		// 获取失败损失积分
		int failScore = 0;
		if(killerHuman.isPKTrigger){ // 进攻方成功才能抢夺
			Map<Integer,Integer> battleBag = getResultItem(killerHuman.humanId,beKillHuman.humanId);
			if(battleBag.containsKey(EMoneyType.lootScore_VALUE)){
				failScore = battleBag.get(EMoneyType.lootScore_VALUE);
			}
		}
		
		killerHuman.killNumber ++; // 杀人数++
		
		int notifiyHornerSn = 0; // 需要进行广播的荣誉sn
		int rewardScore = 0; // 荣誉奖励
		ArrayList<ConfLootMapKillHonor> hornerConfList = new ArrayList<>(); // 荣誉数组 取得最高显示
		
		// 1血荣誉判断
		if(firstKillerId == 0){ 
			firstKillerId = killerHuman.humanId; // 首杀id
			int sn = ConfigKeyFormula.getLootMapKillHonorSn(ELootMapKillHornerType.LootMapKillHornerFirst_VALUE, 0);
			rewardScore += getKillHonorScore(sn,hornerConfList); // 添加到list,获取score
		}
		
		// 获取终结荣誉
		if(beKillHuman.killNumber > 0){ // 当对方身上已经有人头
			// 获取配置表sn
			int sn = ConfigKeyFormula.getLootMapKillHonorSn(ELootMapKillHornerType.LootMapKillHornerStopKiller_VALUE, beKillHuman.killNumber); 
			rewardScore += getKillHonorScore(sn,hornerConfList); // 添加到list,获取score

			if(beKillHuman.killNumber != 0){ // 重置对方连杀记录
				beKillHuman.killNumber = 0;
			}
		}
		
		// 获取多杀荣誉 
		// 获取配置表sn
		int sn = ConfigKeyFormula.getLootMapKillHonorSn(ELootMapKillHornerType.LootMapKillHornerKill_VALUE, killerHuman.killNumber); 
		rewardScore += getKillHonorScore(sn,hornerConfList); // 添加到list,获取score
		
		ConfLootMapKillHonor tempConf = null; // 临时对象
		if(hornerConfList.size() == 1){
			tempConf = hornerConfList.get(0);
		}else{
			for(int i = 1;i < hornerConfList.size(); i++){ // 取得最高荣誉
				ConfLootMapKillHonor c = hornerConfList.get(i);
				if(c.sort > tempConf.sort){
					tempConf = c;
				}
			}
		}
		
		//设置多杀荣誉通知
		if(tempConf!=null){
			notifiyHornerSn = tempConf.sn;	
		}
		
		int winScore = getWinScore(failScore,rewardScore);
		
		if(winScore!=0){
			setOneScore(killerHuman.humanId, killerHuman.getOneScore() + winScore, true);
		}
		
		if(failScore!=0){
			setOneScore(beKillHuman.humanId, beKillHuman.getOneScore() - failScore, true);
		}
		
		SCLootMapKill.Builder killBuilder = SCLootMapKill.newBuilder();
		killBuilder.setKillerInfo(killerHuman.getDMemberInfo());
		killBuilder.setBeKillerInfo(beKillHuman.getDMemberInfo());
		killBuilder.setHornorSn(notifiyHornerSn);
		killBuilder.setScore(winScore);
		killBuilder.setRewardScore(rewardScore);
		killBuilder.setKillNumber(killerHuman.killNumber);
		InstLootMapManager.inst().sendAllHuman(killBuilder,this);
	}
	
	/**
	 * 根据配置表sn获取积分 以及添加到list
	 * @param confSn
	 * @param hornerConfList
	 * @return
	 */
	private int getKillHonorScore(int confSn,ArrayList<ConfLootMapKillHonor> hornerConfList){
		ConfLootMapKillHonor conf = ConfLootMapKillHonor.get(confSn); // 获取配置表
		if(conf == null){
			Log.lootMap.error(" ======== getKillHonorScore ConfLootMapKillHonor.get is null sn = {} ",confSn);
			return 0;
		}
		if(hornerConfList!=null && conf.isNotice == 1){
			hornerConfList.add(conf); // 添加记录vv
		}
		return conf.reward;
	}
	
	/**
	 * 获取失败积分
	 * @param score
	 * @param killCount
	 * @return
	 */
	//被抢夺玩家损失=被抢夺者当前积分*抢夺比例/1000*(1000+被抢夺玩家当前人头数*人头参数)/1000，向下取整
	private int getFailScore(int score,int killCount){
		int resoult = (int)Math.floor(score * ParamManager.lootMapLootScale / 1000f * (1000 + killCount * ParamManager.lootMapKillScale) / 1000);
		if(timeMode == ELootMapTimeMode.LootMapTimeModeDoubleAll || 
				timeMode == ELootMapTimeMode.LootMapTimeModeDoublePvp){
			resoult *= 2;
		}
		return resoult;
	}
	
	/**
	 * 获取胜利积分
	 * @param lostScore
	 * @param reward
	 * @return
	 */
	//抢夺玩家收益=被抢夺玩家损失+抢夺玩家当前人头数对应的赏金收益+【首杀闪金（判定是否首杀）】
	private int getWinScore(int lostScore,int reward){
		if(timeMode == ELootMapTimeMode.LootMapTimeModeDoubleAll || 
				timeMode == ELootMapTimeMode.LootMapTimeModeDoublePvp){
			reward *= 2;
		}
		return lostScore + reward; // lostScore 已经翻倍过了
	}

	
	/**
	 * 获取积分奖励
	 * @param humanId
	 * @param rewardSn
	 */
	private void humanGetScoreReward(long humanId,int rewardSn){
		List<Define.DItem> list = getRewardItemList(rewardSn);
		if(list == null){
			return;
		}
		getReward(humanId,rewardSn); // 玩家获取
		SCLootMapScoreReward.Builder builder = SCLootMapScoreReward.newBuilder();
		builder.addAllItemList(list);
		InstLootMapManager.inst().sendHuman(builder,getHumanObj(humanId));
	}
	
	@Override
	protected void setOneScore(long humanId,int score){
		setOneScore(humanId, score, false);
	}
	
	private void setOneScore(long humanId, int score, boolean isBattleEnd) {
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human == null) return;
		int rewardSn = human.setOneScore(score);
		if(rewardSn!=0){
			human.setOneScoreReceive();
			humanGetScoreReward(humanId, rewardSn);
		}
		sendUpdateScore(humanId, isBattleEnd);
	}
	
	/**
	 * 更新积分 进行发送
	 */
	private void sendUpdateScore(long humanId, boolean isBattleEnd){
		InstLootMapHumanData humanData = GetHumanData(humanId);
		if(humanData == null){
			Log.lootMap.error("   ========   sendUpdateScore human = null, humanId = {}",humanId);
			return;
		}
		sendSortRank(); // 更新排名变化 进行广播
		//积分变换 进行推送
		SCLootMapScoreChange.Builder builder = SCLootMapScoreChange.newBuilder();
		builder.setScore(humanData.getOneScore());
		builder.setIsBattleEnd(isBattleEnd);
		InstLootMapManager.inst().sendHuman(builder,getHumanObj(humanId));
	}

	@Override
	public void humanOut(long humanId){
		super.humanOut(humanId);
		
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human==null){
			return;
		}
		
		saveScore(humanId);
		saveScore2Rank(humanId);
		
		outHumanMap.put(humanId, human);
	}
	
	/**
	 * 进行积分记录
	 * @param humanId
	 */
	private void saveScore(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		if(human==null){
			return;
		}
		//数据修改
		HumanObject humanObj = getHumanObj(humanId);
		if(humanObj == null) return;
		HumanExtInfo extInfo = humanObj.getHumanExtInfo(); // 获取exInfo
		if(extInfo == null) return;
		int exInfoTodayScore = extInfo.getLootMapMultipleTodayScore(); // 获取今日最高
		int todayScore = human.getTodayTop1Score();
		if(exInfoTodayScore < todayScore){ // 记录分数小于现在分数
			extInfo.setLootMapMultipleTodayScore(todayScore); // 设置今日最高分数
			long exInfoScore = extInfo.getLootMapMultipleScore(); // 获取总分数
			exInfoScore -= exInfoTodayScore; // 扣去旧分数
			exInfoScore += todayScore; // 加上今日最高
			extInfo.setLootMapMultipleScore(exInfoScore); // 保存分数
		}
	}
	
	private void saveScore2Rank(long humanId){
		InstLootMapHumanData human = GetHumanData(humanId);
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.addNew(human.getRankData(),RankType.RankFairyland);
	}
	
	@Override
	protected void gameSettlement(){
		if(!isStartGame) return;
		super.gameSettlement();
		for(InstLootMapHumanData humanData : outHumanMap.values()){
			settlement2Mail(humanData);
		}
	}
	
	/**
	 * 结算
	 * 增加排名奖励到玩家身上
	 */
	@Override
	protected void onSettlement2Mail(InstLootMapHumanData human){
		//把积分排名奖励增加到玩家身上
		for(ConfLootMapRankReward conf : ConfLootMapRankReward.findAll()){
			int min = conf.min;
			int max = conf.max;
			if(human.oneRank >= min && human.oneRank <= max){
				ConfRewards rewardConf = ConfRewards.get(conf.rewardSn);
				if(rewardConf != null){ // 找得到该配置表
					int[] items = rewardConf.itemSn; // 获取道具sn
					int[] num = rewardConf.itemNum; // 数量
					for(int i = 0; i < items.length;i++){ // 增加到map
						human.addItem(items[i],num[i]);
					}
				}
				break;
			}
		}
	}
	
	/**
	 * 活动结束
	 * 积分写入排行榜
	 */
	@Override
	protected void activityTimeUp(){
		super.activityTimeUp();
		Set<RankData> rankSet = new HashSet<>();
		for(InstLootMapHumanData data : getAllHumanData()){
			saveScore(data.humanId);
			rankSet.add(data.getRankData());
		}
		if(rankSet.size() > 0){
			RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
			proxy.addNews(rankSet,RankType.RankFairyland,true);
			proxy.listenResult(this::_result_updateRankByLootMap);
		}
	}
	
	/**
	 * 玩家加入抢夺本 查询排行榜里面积分
	 */
	@Override
	protected void humanIntoMap(InstLootMapHumanData human){
		super.humanIntoMap(human);
		scoreRank.add(human.humanId);
		int oneRank= scoreRank.size();
		human.oneRank = oneRank;
		sendUpdateScoreRank(human.humanId,oneRank);
	}
	
	/**
	 * 排序
	 */
	private void sortRank(){
		scoreRank.sort((id1,id2) -> GetHumanData(id2).getOneScore() - GetHumanData(id1).getOneScore());
	}
	
	/**
	 * 进行排序且发送
	 */
	private void sendSortRank(){
		sortRank();
		for(int i = 0;i < scoreRank.size();i++){
			Long humanId = scoreRank.get(i);
			int rank = i+1;
			InstLootMapHumanData human = GetHumanData(humanId);
			if(human.oneRank != rank){
				human.oneRank = rank;
				sendUpdateScoreRank(humanId,rank);
			}
		}
	}
	
	/**
	 * 更新单场排名
	 * @param humanId
	 * @param rank
	 */
	private void sendUpdateScoreRank(long humanId,int rank){
		SCLootMapScoreRank.Builder builder = SCLootMapScoreRank.newBuilder();
		builder.setHumanId(humanId);
		builder.setOneRank(rank);
		InstLootMapManager.inst().sendAllHuman(builder, this);
	}

	/**
	 * 结算查询
	 * @param results
	 * @param context
	 */
	private void _result_updateRankByLootMap(Param results, Param context){
		List<Long> humanList = new ArrayList<>();
		for(InstLootMapHumanData data : getAllHumanData()){
			humanList.add(data.humanId);
		}
		for(InstLootMapHumanData data : outHumanMap.values()){
			humanList.add(data.humanId);
		}
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.getRankByLootMap(humanList);
		proxy.listenResult(this::_result_updateRankByLootMap2);
	}
	
	private void _result_updateRankByLootMap2(Param results, Param context){
		Map<Long,Integer> rankMap = Utils.getParamValue(results, "rank",null);// 查询排名 key = humanId, value = rank
		if(rankMap == null){
			Log.lootMap.error("   ========   _result_getRankData rankSet == null");
			return;
		}
		
		//当场最高
		long curTop1HumanId = getOneScoreRank1HumanId();
		InstLootMapHumanData curTop1Human =	GetHumanData(curTop1HumanId);
		DMemberInfo.Builder firstDMemberInfo = curTop1Human.getDMemberInfo();
		
		for(Long humanId : rankMap.keySet()){
			InstLootMapHumanData human = GetHumanData(humanId);
			if(human == null) continue;
			HumanObject humanObj = getHumanObj(humanId);
			if(humanObj == null) continue;
			SCLootMapMultipleEnd.Builder builder = SCLootMapMultipleEnd.newBuilder();
			builder.setFirstHuman(firstDMemberInfo);
			builder.setFirstHumanScore(curTop1Human.getOneScore());
			builder.setRank(human.oneRank);
			builder.setScore(human.getOneScore());
			builder.setTodayTop1Score(human.getTodayTop1Score());
			builder.setAllScore(human.getRankDataScore());
			builder.setAddScore(human.getAddScore());
			InstLootMapManager.inst().sendHuman(builder,humanObj);
		}
	}
	
	/**
	 * 获取本场积分最高玩家
	 * @return
	 */
	private long getOneScoreRank1HumanId(){
		for(InstLootMapHumanData data : getAllHumanData()){
			if(data.oneRank == 1){
				return data.humanId;
			}
		}
		for(InstLootMapHumanData data : outHumanMap.values()){
			if(data.oneRank == 1){
				return data.humanId;
			}
		}
		return -1;
	}
	
}
