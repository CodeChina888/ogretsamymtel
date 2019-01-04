package game.worldsrv.character;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.worldsrv.guild.GuildInstManager;
import org.apache.commons.lang3.time.StopWatch;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

import core.CallPoint;
import core.Chunk;
import core.InputStream;
import core.OutputStream;
import core.Port;
import core.connsrv.ConnectionProxy;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.support.ConnectionStatus;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import game.msg.Define.DPVEHarm;
import game.msg.Define.DPVPKill;
import game.msg.Define.DSkillGroup;
import game.msg.Define.DStageHuman;
import game.msg.Define.DStageObject;
import game.msg.Define.DVector3;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EMapType;
import game.msg.Define.EMoneyType;
import game.msg.Define.ETeamType;
import game.msg.Define.EWorldObjectType;
import game.msg.MsgAccount.SCMsgFill;
import game.msg.MsgCross.SCCrossFightInfo;
import game.msg.MsgIds;
import game.msg.MsgInform;
import game.msg.MsgTurnbasedFight.SCTurnbasedFinish;
import game.worldsrv.achieveTitle.achieveTitleVO.AchieveTitleVO;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfMap;
import game.worldsrv.entity.Achievement;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.entity.ActivitySeven;
import game.worldsrv.entity.Backlog;
import game.worldsrv.entity.Card;
import game.worldsrv.entity.CultureTimes;
import game.worldsrv.entity.DropInfo;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.entity.Instance;
import game.worldsrv.entity.Mail;
import game.worldsrv.entity.PayLog;
import game.worldsrv.entity.Shop;
import game.worldsrv.entity.ShopExchange;
import game.worldsrv.enumType.ItemPackType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.SwitchState;
import game.worldsrv.fashion.FashionManager;
import game.worldsrv.fashion.FashionRecord;
import game.worldsrv.friend.FriendInfo;
import game.worldsrv.friend.FriendManager;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.human.HumanOperateCDManager;
import game.worldsrv.humanSkill.HumanSkillRecord;
import game.worldsrv.instLootMap.InstLootMapManager;
import game.worldsrv.instLootMap.InstLootMapServiceProxy;
import game.worldsrv.instLootMap.Stage.StageObjectLootMapMultiple;
import game.worldsrv.instLootMap.Stage.StageObjectLootMapSingle;
import game.worldsrv.instResource.InstResRecord;
import game.worldsrv.item.Item;
import game.worldsrv.item.ItemPack;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.quest.QuestRecord;
import game.worldsrv.raffle.RaffleInfo;
import game.worldsrv.rune.RuneRecord;
import game.worldsrv.stage.StageGlobalServiceProxy;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageObject;
import game.worldsrv.support.Log;
import game.worldsrv.support.LogOpUtils;
import game.worldsrv.support.MathUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.Vector2D;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamData;
import game.worldsrv.team.TeamManager;
import game.worldsrv.tower.TowerRecord;

/**
 * 角色
 */
@DistrClass
public class HumanObject extends CharacterObject {
	public static final String paramKey = "humanObj";
	
	// GM战斗自定义数值
	public int[] daddyFight = new int[3]; 
	
	// 充值校验码生成用
	public String lastPayCheckCode = "";
	public long createPayCheckCodeTime = 0;
	public List<PayLog> payLogs = new ArrayList<>();	//充值记录
	
	public boolean isDailyFirstLogin = false;// 是否每日首次登录，是则需执行凌晨四点重置
	public boolean isInCloseDelay = false;// 是否处于断线延迟状态
	public HumanGlobalInfo humanGlobalInfo = new HumanGlobalInfo();// 记录玩家全局信息
	public CallPoint connPoint = new CallPoint(); // 玩家连接点信息
	public ConnectionStatus connStatus = new ConnectionStatus();// 玩家连接状态
	
	public long loadingPID = 0; // 正在加载玩家数据时的请求ID
	public int loadingNum = 0; // 正在加载玩家数据时的计数器 当等于0时代表加载完毕
	public int loadingGenNum = 0; // 正在加载武将数据时的计数器 当等于0时代表加载完毕
	
	public boolean isGetHumanPos = true;// 是否取我方位置：true取我方位置，false取敌方位置
	public Vector2D birthPos = new Vector2D();// 用于记录进入地图时的出生点
	public Vector2D birthDir = new Vector2D();// 用于记录进入地图时的朝向
	public int reviveBuffSn = 0;// 记录复活BuffSn
	public int loginStageState;// 玩家登陆状态判断 临时属性 0=无状态 1=登陆中 2=今日首次登陆中
	
	// 人物持久化信息
	protected TickTimer ttDeferClose = new TickTimer(); // 延迟关闭定时器
	private TickTimer ttOnline = new TickTimer(Time.MIN);// 在线时间改变计时器
	private TickTimer ttSync = new TickTimer(Time.SEC * 30);// 同步时间检测定时器
	private TickTimer ttSwitching = null;// 切换地图定时器
	public TickTimer ttMsgFill = new TickTimer(); // 如果CS消息没有返回那么补充SC消失
	
	//time:开启倒计时的时间点 portTime - 4,表示4分钟前就开启了倒计时，再过两分钟就可以增加体力
	public TickTimer ttActValue = new TickTimer(); // 体力恢复定时器
	public TickTimer ttGenSkillPoint = new TickTimer(); // 恢复武将技能点计时器
	public TickTimer ttGenSPNext = new TickTimer();// 一次的武将恢复技能点
	public TickTimer ttNowAward = new TickTimer(); // 恢复在线奖励倒计时计时器
	public TickTimer ttLifeEquipMin = new TickTimer(); // 身上限时装备最小结束时间计时器，过期装备属性不计算人物身上
	public TickTimer ttFriendList = new TickTimer(); // 每隔N分钟更新一下角色好友的状态，并写入数据库
	public TickTimer ttWorldBossReviveCD = new TickTimer();// 世界boss复活CD计时器
	public TickTimer ttWorldBossInspireCD = new TickTimer();// 世界boss鼓舞CD计时器
	public TickTimer ttFashion = new TickTimer(); // 时装过期判断计时器
	public TickTimer ttFashionHenshin = new TickTimer();// 变装过期判断计时器
	
	public boolean isClientStageReady; // 客户端地图状态已准备完毕
	public boolean isStageSwitching = false; // 正在切换地图中
	private int switchFrom;// 切换地图时的来源地图sn。用于判断buff的条件
	public SwitchState switchState;// 切图状态

	// 切换场景需要携带的的信息，需要手动写入到 writeTo和readFrom两个方法中
	public Map<Long, PartnerObject> partnerMap = new HashMap<>(); // 保存所有武将信息
		
	// 世界频道的发言CD
	public long worldInformCD; // 最后一次发言的时间戳

	// 待办事项列表key=BacklogType.name()
	public Map<String, List<Backlog>> backlogMap = new HashMap<>();
	
	// 邮件列表
	public List<Mail> mailList = new ArrayList<>();

	// 好友信息
	public FriendInfo friendInfo = new FriendInfo();

	//活动数据Map<活动id,Map<aid,ActivityHumanData>>
	public Map<Integer, Map<Integer, ActivityHumanData>> activityDatas = new HashMap<>();
	//开服活动
	public Map<Integer, ActivitySeven> humanActivitySeven = new HashMap<>();
	
	// 任务信息
	public QuestRecord questRecord = new QuestRecord();                      		//任务
	
	//商店信息
	public ShopExchange shopExchange;
	
	// 副本信息，<key=ChapterSn章节SN, value=Instance副本章节记录>
	public Map<Integer, Instance> instancesMap = new HashMap<>();
	// 资源本信息
	public InstResRecord instResRecord = null;
	
	// 组队信息
	private TeamData teamData = null;
	public DPVPKill.Builder dPVPKill = DPVPKill.newBuilder();// PVP结算击杀
	public DPVEHarm.Builder dPVEHarm = DPVEHarm.newBuilder();// PVE结算伤害
	
	// 技能信息
	public HumanSkillRecord humanSkillRecord = new HumanSkillRecord();
	// 成就任务
	public List<Achievement> achievements = new ArrayList<>();
	// 称号成就< Type , 称号类型对象>
	public Map<Integer, AchieveTitleVO> achieveTitleMap = new HashMap<>();
	
	// 角色的扩展信息数据 
	public HumanExtInfo extInfo;
	// 操作锁
	public HumanOperateCDManager cdLocks = new HumanOperateCDManager();
	
	// 爬塔信息
	public TowerRecord towerRecord = null;

	// 仙盟副本战斗中
	public boolean isGuildInstFighting = false;
	
	// 远征信息
	//public GodsWarInfo godswarInfo;
	//整点体力
	public int [] activity = new int[]{0,0,0};
	//抽卡信息
	public Card cardInfo;
	//掉落计数 <计数掉落类型:数量>
	public Map<Integer,Integer> dropCountMap = new HashMap<>();
	public DropInfo dropInfo;
	//个人限时充值信息
	public Map<Integer,ActivityHumanData> limitrechargeMap = new HashMap<>();
	// 抽奖信息
	public RaffleInfo raffleInfo = new RaffleInfo();
	
	//商店信息
	public Map<Integer, Shop> shopMap = new HashMap<Integer, Shop>();
	
	// 背包物品
	public ItemPack itemBag = new ItemPack(ItemPackType.Bag, new ArrayList<Item>());
	// 身上装备
	public ItemPack itemBody = new ItemPack(ItemPackType.Body, new ArrayList<Item>());
	
	// 玩家时装信息
	public FashionRecord fashionRecord = new FashionRecord();
	// 玩家符文信息
	public RuneRecord runeRecord = new RuneRecord();
	
	private Long lootMapSingUpRoomId = -1L; // 报名房间
	//培养次数统计信息
	public CultureTimes cultureTimes = new CultureTimes();
	
	/** 跨服战斗相关 **/
	/** 战斗预切入地图sn **/
	public int crossFightMapSn;
	/** 战斗切入地图key **/
	public String crossFightSwitchKey;
	/** pvp战斗对方数据 **/
	public HumanMirrorObject crossFightEnemy;
	/** 战斗node **/
	public String crossFightNodeId;
	/** cross服编号 **/
	public int crossServerIndex;
	/** 战斗结束包 **/
	public SCTurnbasedFinish crossFightFinishMsg;
	/** 战斗者编队 **/
	public int crossFightTeamId;
	
	/** 战斗队伍 **/
	public ETeamType fightTeam = ETeamType.Team1;
	public HumanMirrorObject humanMirrorObj = null;

	//记录创建所有副本时间
	public long createRepTime = 0;
	public void setCreateRepTime() {
		createRepTime = Port.getTime();
	}
	public boolean isInCreateRepTime() {
		if(Port.getTime() - createRepTime > 5 * Time.SEC) {
			return false;
		}
		return true;
	}
		
	/**
	 * 构造函数
	 */
	public HumanObject() {
		super(null);
		this.switchState = SwitchState.InStage;
	}


	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		// GM战斗自定义数值
		out.write(daddyFight);
		
		// 充值校验码生成用
		out.write(lastPayCheckCode);
		out.write(createPayCheckCodeTime);
		out.write(payLogs);
				
		out.write(isDailyFirstLogin);// 是否每日首次登录
		out.write(isInCloseDelay);// 断线延迟状态
		out.write(dataPers);// 基础数据
		out.write(connPoint);// 连接信息
		out.write(humanGlobalInfo);// 玩家全局信息
		// 进入地图时的出生点和朝向信息
		out.write(birthPos);
		out.write(birthDir);
		// 定时器--进入副本需要传入，否则定时器将关闭
		out.write(ttOnline);
		out.write(ttSync);
		out.write(ttSwitching);
		out.write(ttActValue);
		out.write(ttGenSkillPoint);
		out.write(ttGenSPNext);
		out.write(ttNowAward);
		out.write(ttFashion);
		out.write(ttFashionHenshin);
		out.write(ttLifeEquipMin);
		out.write(ttFriendList);
		out.write(ttWorldBossReviveCD);
		out.write(ttWorldBossInspireCD);
				
		// 主角背包物品及身上装备
		out.write(itemBody);
		out.write(itemBag);
		
		// 变装信息
		out.write(fashionRecord);
		// 符文信息
		out.write(runeRecord);
				
		// 武将
		out.write(partnerMap);
		out.write(cardInfo);
		// 其它
		out.write(switchFrom);
		out.write(switchState);
		out.write(fightTeam);
		
		out.write(worldInformCD);// 最后一次发言时间
		out.write(mailList);// 邮件
		out.write(friendInfo);// 好友
		out.write(activityDatas);// 活动
		out.write(questRecord);// 任务
		out.write(humanSkillRecord);// 技能
		out.write(shopExchange);//商店
		// 副本信息
		out.write(instancesMap);
		out.write(teamData);// 组队
		// 资源本
		out.write(instResRecord);
		// 爬塔
		out.write(towerRecord);
        out.write(isGuildInstFighting);

		out.write(achievements);// 成就
		out.write(achieveTitleMap); //称号成就
		out.write(extInfo);// 玩家扩展信息
		out.write(cdLocks);// 操作锁
		out.write(activity);//整点体力
		out.write(shopMap);//商店
//		out.write(cpHuman);//竞技场
		out.write(dropCountMap);
		out.write(limitrechargeMap);
		out.write(dropInfo);
		
		out.write(raffleInfo);// 抽奖信息
		
		//活动数据
		out.write(humanActivitySeven);
		//培养次数信息
		out.write(cultureTimes);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		// GM战斗自定义数值
		daddyFight = in.read();
		
		// 充值校验码生成用
		lastPayCheckCode = in.read();
		createPayCheckCodeTime = in.read();
		payLogs = in.read();
				
		isDailyFirstLogin = in.read();// 是否每日首次登录
		isInCloseDelay = in.read();// 断线延迟状态
		dataPers = in.read();// 基础数据
		connPoint = in.read();// 连接信息
		humanGlobalInfo = in.read();// 玩家全局信息
		// 进入地图时的出生点和朝向信息
		birthPos = in.read();
		birthDir = in.read();
		// 定时器
		ttOnline = in.read();
		ttSync = in.read();
		ttSwitching = in.read();
		ttActValue = in.read();
		ttGenSkillPoint = in.read();
		ttGenSPNext = in.read();
		ttNowAward = in.read();
		ttFashion = in.read();
		ttFashionHenshin = in.read();
		ttLifeEquipMin = in.read();
		ttFriendList = in.read();
		ttWorldBossReviveCD = in.read();
		ttWorldBossInspireCD = in.read();
				
		// 主角背包物品及身上装备
		itemBody = in.read();
		itemBag = in.read();
		
		// 变装信息
		fashionRecord = in.read();
		// 符文信息
		runeRecord = in.read();
		
		// 武将
		partnerMap.clear();
		partnerMap.putAll(in.<Map<Long, PartnerObject>> read());
		cardInfo = in.read();
		
		// 其它
		switchFrom = in.read();
		switchState = in.read();
		fightTeam = in.read();
		
		worldInformCD = in.read();// 最后一次发言时间
		mailList = in.read();// 邮件
		friendInfo = in.read();// 好友
		activityDatas = in.read();// 活动
		questRecord = in.read();// 任务
		humanSkillRecord = in.read(); //技能
		shopExchange = in.read();//商店
		// 副本信息
		instancesMap.clear();
		instancesMap.putAll(in.<Map<Integer, Instance>> read());
		teamData = in.read();// 组队
		// 资源本
		instResRecord = in.read();
		// 爬塔
		towerRecord = in.read();
		isGuildInstFighting = in.read();
		
		// 成就
		achievements.clear();
		achievements = in.read();
		// 称号成就
		achieveTitleMap.clear();
		achieveTitleMap.putAll(in.<Map<Integer, AchieveTitleVO>> read());
		// 玩家扩展信息
		extInfo = in.read();
		// 操作锁
		cdLocks = in.read();
		// 活动
		activity = in.read();
		// 商店
		shopMap.clear();
		shopMap.putAll(in.<Map<Integer, Shop>> read());
		//竞技场
//		cpHuman = in.read();
		dropCountMap.clear();
		dropCountMap.putAll(in.<Map<Integer,Integer>>read());

		limitrechargeMap.clear();
		limitrechargeMap.putAll(in.<Map<Integer,ActivityHumanData>>read());
		dropInfo = in.read();
		
		raffleInfo = in.read();
		
		//七日活动
		humanActivitySeven.clear();
		humanActivitySeven.putAll(in.<Map<Integer, ActivitySeven>>read());
		
		//培养次数信息
		cultureTimes = in.read();
		// 重新关联主仆关系
		for (PartnerObject po : this.partnerMap.values()) {
			// 属性变化
			HumanObject humanObj = po.parentObject;
			if(humanObj == null){
				po.parentObject = this;
			}
		}
}

	@Override
	public void startup() {
		super.startup();
	}

	@Override
	public void die(UnitObject killer, Param params) {
		super.die(killer, params);
	}
	
	/**
	 * 将玩家注册到地图中，但暂不显示玩家
	 * @param stageObj
	 */
	@Override
	public boolean stageRegister(StageObject stageObj) {
		// 断线重连等情况下，会出现注册玩家前，可能会残留之前的数据
		// 这里调用数据的原坐标，能避免坐标差距，造成客户端人物位置改变。
		HumanObject humanObjOld = stageObj.getHumanObj(id);
		if (humanObjOld != null) {
			posNow.set(humanObjOld.posNow);
			dirNow.set(humanObjOld.dirNow);
			posBegin.set(humanObjOld.posBegin);
			dirBegin.set(humanObjOld.dirBegin);
		}
		if (stageObj.getCell(posNow) == null) {
			Log.human.error("===玩家注册到地图失败：mapSn={},posNow={},name={}", stageObj.mapSn, posNow.toString(), this.name);
			return false;
		}

		// 注册到地图前，把出生点和朝向保存起来供复活时调用
		int mapSn = stageObj.mapSn;
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap != null && !confMap.type.equals(EMapType.common.name())) {
			// 非主城地图的话，取出生点为进入地图场景的当前点
			this.birthPos = this.posNow;// 位置
			this.birthDir = this.dirNow;// 朝向
			Vector2D humanPos = StageManager.inst().getHumanPos(mapSn);// 我方位置
			Vector2D enemyPos = StageManager.inst().getEnemyPos(mapSn);// 敌方位置
			if (enemyPos.isZero()) {// 没配数据，为了避免错误，还是传到我方出生点得了
				isGetHumanPos = true;
				this.birthPos = humanPos;
				this.birthDir = MathUtils.getDir(this.birthPos, StageManager.inst().getHumanDir(mapSn));
			} else {// 有配敌方位置，那判断下是否取我方位置还是敌方位置
				if (this.posNow.distance(humanPos) < 10.0) {// 取我方位置
					isGetHumanPos = true;
					this.birthPos = humanPos;
					this.birthDir = MathUtils.getDir(this.birthPos, StageManager.inst().getHumanDir(mapSn));
				} else {// 取敌方位置
					isGetHumanPos = false;
					this.birthPos = enemyPos;
					this.birthDir = MathUtils.getDir(this.birthPos, StageManager.inst().getEnemyDir(mapSn));
				}
			}

			// Log.human.info("===复活点复活：human.id={}, name={}, pos={}, dir={}",
			// this.id, this.name, this.birthPos,
			// this.faceTo);
		}

		// 调用父类实现
		return super.stageRegister(stageObj);
	}

	/**
	 * 在地图显示：正常出现
	 */
	@Override
	public void stageShow() {
		super.stageShow();

		isStageSwitching = false;// 切换地图完毕，才开始接受消息
		// 发送进入地图事件
		Event.fireEx(EventKey.StageHumanShow, this.stageObj.mapSn, "humanObj", this);
		
		//callLineupGeneral(true);// 召唤上阵武将
	}

	/**
	 * 在地图显示：复活
	 */
	@Override
	public void stageShowRevive() {
		super.stageShowRevive();

		//callLineupGeneral(false);// 召唤上阵武将
	}	

	public void pulseConnection() {
		if (ttDeferClose.isOnce(tmCurr)) {
			connCloseClear();
		}
	}
	
	@Override
	public void finallyPulse(int deltaTime) {
		super.finallyPulse(deltaTime);
		// 处理connection
		pulseConnection();
	}
	
	@Override
	public void pulse(int tmDelta) {
		// FIXME 性能记录
		StopWatch sw = new StopWatch();
		sw.start();
		
		// 先执行通用操作
		super.pulse(tmDelta);
		
		if (ttSwitching != null && ttSwitching.isOnce(getTime())) {
			setStageSwitching(false);
		}
		
		// 处理connection
		pulseConnection();

//		if (ttMsgFill.isOnce(tmCurr)) {
//			sendScFillMsg();
//		}
		
		// 体力恢复处理
		if (ttActValue.isPeriod(tmCurr)) {
			recoveryAct();
		}
		
		// friendListTimer 定时器
		if (ttFriendList.isPeriod(tmCurr)) {
			FriendManager.inst().updateFriendInfo(this);
		}

		GuildInstManager.inst().humanTick(this);
		//登录时距离下一点武将技能点回复时间
//		if(ttGenSPNext.isOnce(tmCurr)){
//			if(!ttGenSkillPoint.isStarted()){
//				ttGenSkillPoint.start(ParamManager.generalSkillPointRecover * Time.SEC);//开启
//			}
//			// 当前拥有的技能点
//			int skillPoint = getHuman().getGenSkillPoint();
//			skillPoint++;
//			// 最大技能点
//			int generalSkillPointMax = ParamManager.generalSkillPointMax;
//			if (skillPoint >= generalSkillPointMax) {
//				skillPoint = generalSkillPointMax;
//				ttGenSkillPoint.stop();// 关闭计时器
//				getHuman().setGenSkillPointAllTime(0);// 恢复满的总时间
//			}
//			getHuman().setGenSkillPoint(skillPoint);//保存技能点
//			ttGenSPNext.stop();//关闭一次的
//			PartnerPlusManager.inst().sendSCGeneralSkillPoint(this);//发送技能点信息
//		}
		
		// 武将技能点回复
//		if (ttGenSkillPoint.isPeriod(tmCurr)) {
//			// 当前拥有的技能点
//			int skillPoint = getHuman().getGenSkillPoint();
//			skillPoint++;
//			// 最大技能点
//			int generalSkillPointMax = ParamManager.generalSkillPointMax;
//			if (skillPoint >= generalSkillPointMax) {
//				skillPoint = generalSkillPointMax;
//				ttGenSkillPoint.stop();// 关闭计时器
//				getHuman().setGenSkillPointAllTime(0);// 恢复满的总时间
//			}
//			getHuman().setGenSkillPoint(skillPoint);//保存技能点
//			PartnerPlusManager.inst().sendSCGeneralSkillPoint(this);//发送技能点信息
//		}
		
		//　世界boss中的复活CD计时器
		if(ttWorldBossReviveCD.isPeriod(tmCurr)){
			ttWorldBossReviveCD.stop();
		}
		//　世界boss中的鼓舞CD计时器
		if(ttWorldBossInspireCD.isPeriod(tmCurr)){
			ttWorldBossInspireCD.stop();
		}
		
		// 限时时装计时器
		if(ttFashion.isPeriod(tmCurr)){
			FashionManager.inst().openFashionTickTimer(this);
		}
		
		// 限时变身计时器
		if(ttFashionHenshin.isPeriod(tmCurr)) {
			FashionManager.inst().openHenshinTickTimer(this);
		}

		// 限时武器计时器
		if (ttLifeEquipMin.isPeriod(tmCurr)) {
			HumanManager.inst().openHumanLifeEquip(this);
		}

		// 客户端是否已完成玩家的加载并登陆到地图前 不做任何操作
		if (!isClientStageReady) {
			return;
		}
		// 累加玩家在线时间
		onlineTimePlus(this);

		// 检查更新玩家全局信息
		updateHumanGlobalInfo(getHuman());
		//同步全局离线数据
		
		// 30秒同步一次，用于检查异常玩家踢下线，或者服务器很卡时踢人下线
		if(ttSync.isPeriod(getTime())) {
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.syncInfoTime(id);
		}
		
		sw.stop();
		if (sw.getTime() > Time.SEC) {
			Log.human.info("HumanObject.pulse() use time={}", sw.getTime());
		}
	}
	
	/**
	 * 玩家在线时间累积
	 * @param humanObject
	 */
	private void onlineTimePlus(HumanObject humanObject) {
		if (!ttOnline.isPeriod(Port.getTime())) {
			return;
		}
		Human human = humanObject.getHuman();
		// 本次累计增加毫秒数
		long msPlus = ttOnline.getInterval();
		// 累计在线总时长： 秒
		human.setTimeSecOnline((int) (human.getTimeSecOnline() + msPlus  / Time.SEC));
		// 累计在线总时长： 毫秒
		human.setTimeOnLine(human.getTimeOnLine() + msPlus);
		// 今日累计在线时长： 毫秒
		human.setDailyOnlineTime(human.getDailyOnlineTime() + msPlus);
	}

	/**
	 * 获取玩家信息，玩家进入地图时能够被其他玩家看到的基础信息
	 */
	@Override
	public DStageObject.Builder createMsg() {
		Human human = getHuman();
		// 移动中的目标路径
		List<DVector3> runPath = running.getRunPathMsg();

		// 玩家信息单元
		DStageHuman.Builder h = DStageHuman.newBuilder();
		h.addAllPosEnd(runPath);				// 目标路径坐标
		h.setLevel(human.getLevel());			// 等级
		h.setSex(human.getSex());				// 性别
		h.setProfession(human.getProfession());	// 职业
		h.setHpCur(human.getHpCur());			// 血量
		h.setHpMax(human.getHpMax());			// 最大血量
		h.setMpCur(0);			// 魔量
		h.setMpMax(0);			// 最大魔量

//		h.setEvil(false);						// 罪恶,红名
		h.setTitleSn(human.getTitleSn()); 		// 称号sn
//		h.setTeamId(0);							// 队伍Id
//		h.setUnionId(0);						// 联盟Id
//		h.setCountryId(0);						// 国家Id

		h.setFashionShow(human.isFashionShow());			// 是否显示时装
		h.setFashionWeaponSn(human.getFashionWeaponSn());	// 时装武器
		h.setFashionClothesSn(human.getFashionSn());	// 时装衣服
//		h.setEquipWeaponSn(human.getEquipWeaponSn());		// 身上武器
//		h.setEquipClothesSn(human.getEquipClothesSn());		// 身上衣服
		
		//h.setPvpMode(human.getPvpMode());// pvp模式
		//h.setInFighting(human.isInFighting());// 是否在战斗状态
		// h.addAllSkill(0);//技能
		// List<DSkill> skills = SkillManager.inst().getSkills(this);
		// h.addAllSkill(skills);//技能

		List<DSkillGroup> skillsList = new ArrayList<>();
		DSkillGroup.Builder dskill = DSkillGroup.newBuilder();
		List<Integer> skills = Utils.strToIntList(this.getHuman().getSkillSet1());
		dskill.addAllSkillSet(skills);
		skillsList.add(dskill.build());
		h.addAllSkillGroup(skillsList);// 技能

//		h.setCombat(human.getCombat());// 战力
//		h.setCanMove(canMove);// 是否可以移动
//		h.setPropJson(getPropPlus().toJSONStr());// 所有属性的JSON数据
//		h.setSn(human.getSn());// 配置SN
//		h.setName(human.getName());// 人物姓名
		

		DStageObject.Builder objInfo = DStageObject.newBuilder();
		objInfo.setPos(posNow.toMsg());// 坐标
		objInfo.setDir(dirNow.toMsg());// 方向
		objInfo.setObjId(id);// WordldObjectId
		objInfo.setModelSn(human.getModelSn());// 模型Sn
		objInfo.setName(name);// 昵称
		objInfo.setType(EWorldObjectType.Human);// 对象类识别码
		objInfo.setHuman(h);

		return objInfo;
	}
	
	public Human getHuman() {
		return (Human) dataPers.unit;
	}
	
	/**
	 * 检查更新玩家全局信息之：名字，等级，VIP等级，战斗力,所属公会id
	 */
	private void updateHumanGlobalInfo(Human human) {
		// 只检查几个需要的信息，用于其他地方查询玩家全局信息
		boolean isUpdate = false;
		String name = human.getName();
		int level = human.getLevel();
		int vipLv = human.getVipLevel();
		int combat = HumanManager.inst().getLineUpCombat(this);
		long belongGuild = human.getGuildId();
		int modelsn = human.getModelSn();
		int defaultModelSn = human.getDefaultModelSn();
		int titleSn = human.getTitleSn();
		int partnerStance = this.extInfo.getPartnerStance();
		if (this.humanGlobalInfo != null) {
			if (!this.humanGlobalInfo.name.equals(name)) {
				isUpdate = true;
			} else if (this.humanGlobalInfo.level != level) {
				isUpdate = true;
			} else if (this.humanGlobalInfo.vipLv != vipLv) {
				isUpdate = true;
			} else if (this.humanGlobalInfo.combat != combat) {
				isUpdate = true;
			} else if (this.humanGlobalInfo.guildId != belongGuild) {
				isUpdate = true;
			} else if (this.humanGlobalInfo.modelSn != modelsn) {
				isUpdate = true;
			} else if (this.humanGlobalInfo.defaultModelSn != defaultModelSn) {
				isUpdate = true;
			} else if (this.humanGlobalInfo.titleSn != titleSn) {
				isUpdate = true;
			} else if (this.humanGlobalInfo.partnerStance != partnerStance) {
				isUpdate = true;
			} 
		}
		if (isUpdate) {
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.updateHumanInfo(this.id, name, level, vipLv, combat, belongGuild, modelsn, defaultModelSn, titleSn, partnerStance);
			prx.listenResult(this::_result_updateHumanInfo, "humanObj", this);
		}
	}
	private void _result_updateHumanInfo(Param results, Param context){
		HumanGlobalInfo humanGlobalInfo = Utils.getParamValue(results, "humanGlobalInfo", null);
		HumanObject humanObj = Utils.getParamValue(context, "humanObj",null);
		if(null != humanObj && null != humanGlobalInfo) {
			humanObj.humanGlobalInfo = humanGlobalInfo;
		}
	}
	
	/**
	 * 在线时间领取奖励,累计
	 */
	public boolean isNowAwardTimerStart() {
		boolean isStart = false;
			if (ttNowAward.isStarted()) {
				isStart = true;
			}
			return isStart;
	}

	/**
	 * 发送通用系统消息至玩家
	 * @param sysMsgId 提示Id
	 * @param params 提示参数(支持A,A1,A2多个A为KEY)，格式为："A",0.0f,"A1",1.1f,"A2",2.2f,"B",1
	 * 				KEY=A为Float类型的数值
	 * 				KEY=B为Integer类型的数值
	 *            	KEY=C为Integer类型的道具SN 
	 *            	KEY=D为Integer类型的技能ID
	 *            	KEY=E为Integer类型的NPCID 
	 *            	KEY=F为Integer类型的装备SN
	 *            	KEY=G为Integer类型的宠物SN
	 */
	public void sendSysMsg(int sysMsgId, Object... params) {
		MsgInform.SCSysMsg.Builder msg = MsgInform.SCSysMsg.newBuilder();
		msg.setSysMsgId(sysMsgId);
		List<Float> listA = new ArrayList<Float>();
		List<Integer> listB = new ArrayList<Integer>();
		List<Integer> listC = new ArrayList<Integer>();
		List<Integer> listD = new ArrayList<Integer>();
		List<Integer> listE = new ArrayList<Integer>();
		List<Integer> listF = new ArrayList<Integer>();
		List<Integer> listG = new ArrayList<Integer>();
		// 解析参数
		Param param = new Param(params);
		for (String keyParam : param.keySet()) {
			String key = keyParam.toUpperCase();
			if (key.contains("A")) {
				Object val = param.get(keyParam);
				if (val instanceof Float) {
					listA.add((Float) val);
				}
			} else if (key.contains("B")) {
				Object val = param.get(keyParam);
				if (val instanceof Integer) {
					listB.add((Integer) val);
				}
			} else if (key.contains("C")) {
				Object val = param.get(keyParam);
				if (val instanceof Integer) {
					listC.add((Integer) val);
				}
			} else if (key.contains("D")) {
				Object val = param.get(keyParam);
				if (val instanceof Integer) {
					listD.add((Integer) val);
				}
			} else if (key.contains("E")) {
				Object val = param.get(keyParam);
				if (val instanceof Integer) {
					listE.add((Integer) val);
				}
			} else if (key.contains("F")) {
				Object val = param.get(keyParam);
				if (val instanceof Integer) {
					listF.add((Integer) val);
				}
			} else if (key.contains("G")) {
				Object val = param.get(keyParam);
				if (val instanceof Integer) {
					listG.add((Integer) val);
				}
			}
		}
		if (!listA.isEmpty()) {
			msg.addAllParamA(listA);
		}
		if (!listB.isEmpty()) {
			msg.addAllParamB(listB);
		}
		if (!listC.isEmpty()) {
			msg.addAllParamC(listC);
		}
		if (!listD.isEmpty()) {
			msg.addAllParamD(listD);
		}
		if (!listE.isEmpty()) {
			msg.addAllParamE(listE);
		}
		if (!listF.isEmpty()) {
			msg.addAllParamF(listF);
		}
		if (!listG.isEmpty()) {
			msg.addAllParamG(listG);
		}
		sendMsg(msg.build());
	}

	/**
	 * 发送消息至玩家
	 * @param builder
	 */
	public void sendMsg(Builder builder) {
		if (builder == null)
			return;

		sendMsg(builder.build());
	}

	/**
	 * 发送消息至玩家
	 * @param msg
	 */
	public void sendMsg(Message msg) {
		if (msg == null)
			return;
//		msg.toBuilder().
		// 玩家连接信息
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(MsgIds.getIdByClass(msg.getClass()), new Chunk(msg));
		ttMsgFill.stop();//关闭计时器
	}

	public void sendMsg(List<Builder> builders) {
		if (builders == null || builders.isEmpty())
			return;

		List<Integer> idList = new ArrayList<Integer>();
		List<Chunk> chunkList = new ArrayList<Chunk>();
		for (Builder builder : builders) {
			Message msg = builder.build();
			idList.add(MsgIds.getIdByClass(msg.getClass()));
			chunkList.add(new Chunk(msg));
		}
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(idList, chunkList);
		ttMsgFill.stop();//关闭计时器
	}

	public void sendMsg(List<Integer> idList, List<Chunk> chunkList) {
		if (idList == null || chunkList == null || idList.isEmpty())
			return;
		int sizeChunk = chunkList.size();
		if (sizeChunk > 50) {
			long sizeBuff = 0;
			for (Chunk ck : chunkList) {
				sizeBuff += ck.length;
			}
			Log.human.info("===广播消息太多了HumanObject.sendMsg sizeChunk={}, sizeBuff={}", sizeChunk, sizeBuff);
		}
		
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(idList, chunkList);
		ttMsgFill.stop();//关闭计时器
	}
	
	/**
	 * 设置记录的队伍ID（用于掉线重连重新进入队伍或副本用的）
	 */
	public void setTeamIdRecord(int teamId) {
		this.getHuman().setTeamId(teamId);
	}

	/**
	 * 获取记录的队伍ID（用于掉线重连重新进入队伍或副本用的）
	 */
	public int getTeamIdRecord() {
		return this.getHuman().getTeamId();
	}

	/**
	 * 获取当前的队伍ID
	 */
	public int getTeamId() {
		int teamId = 0;
		if (this.teamData != null)
			teamId = this.teamData.teamId;
		return teamId;
	}

	public TeamData getTeam() {
		return this.teamData;
	}

	public void setTeam(TeamData team) {
		int teamId = 0;
		if (team != null) {
			teamId = team.teamId;
		}
		setTeamIdRecord(teamId);
		this.teamData = team;
	}

	public void setTeamStartOpen() {
		if (this.teamData != null) {
			this.teamData.startOpen();
		}
	}

	public void setTeamStartClose() {
		if (this.teamData != null) {
			this.teamData.startClose();
		}
	}

	public void setTeamMatchOpen() {
		if (this.teamData != null) {
			this.teamData.matchOpen();
		}
	}

	public void setTeamMatchClose() {
		if (this.teamData != null) {
			this.teamData.matchClose();
		}
	}

	public void connDelayCloseClear() {
		// add by shenjh,为了做断线重连，所以这里不马上触发logout,而是隐藏，过5分钟后才logout
		isInCloseDelay = true;// add by shenjh,断线延迟状态
		HumanManager.inst().saveStageHistory(this);// 保存玩家场景位置记录
		this.stageHide();// 隐藏玩家
		if (teamData != null && !teamData.isStart() && !teamData.isMatch()) {// 有队伍且还没匹配且还没开始则离开队伍
			Log.human.debug("===sjh断线重连：玩家掉线了 name={},teamId={},isStart={},isMatch={}", getHuman().getName(),
					teamData.teamId, teamData.isStart(), teamData.isMatch());
			TeamManager.inst()._msg_CSTeamLeave(this);
		}
		
		//退出抢夺本内容
		//没有做断线重连处理 如果需要重连要求 放在connCloseClear中
		outLootMap();
		
//		if (this.getHuman().getGuildBelong() > 0) {// 如果有公会情况下,下线
//			GuildManager.inst().humanLogOut(this.getHuman());// 更新公会会员信息
//		}
		// if(this.friendList != null){// 如果是双线好友的情况下， 通知我下线了
		// FriendListManager.inst().sendFriendLogout(this);
		// }
		
		
		// 处理延迟关于 启动关闭后XX 秒才会彻底清除玩家数据
		ttDeferClose.start(ParamManager.closeDelaySec * Time.SEC);
	}

	/**
	 * 玩家下线时进行清理
	 */
	public void connCloseClear() {
		// 发布退出事件
		Event.fireEx(EventKey.HumanLogout, stageObj.mapSn, "humanObj", this);
		Log.temp.info("===sjhtest HumanLogout pos={},{}", this.posNow.x, this.posNow.y);
		isInCloseDelay = false;// add by shenjh,断线延迟状态
		Human human = this.getHuman();
		
		// 清理
		HumanGlobalServiceProxy hgsprx = HumanGlobalServiceProxy.newInstance();
        Log.game.info("------------------ connCloseClear {}", id);
		hgsprx.cancel(id);
		
		// 刷新human表
		DB prx = DB.newInstance(human.getTableName());
		prx.flush();
		
		DB prxExt = DB.newInstance(this.extInfo.getTableName());
		prxExt.flush();
		
		// 清理各种引用关系
		if(null != running) {
			running.release();
		}
				
		// 添加登出日志:用户ID,登陆时间戳,账号ID,角色名,等级,日期串,在线时长（秒）,停留任务,场景地图,停留坐标,状态1,状态2,状态3
		LogOpUtils.LogLogout(this);
	}

	public void sendScFillMsg() {
		SCMsgFill.Builder msg = SCMsgFill.newBuilder();
		sendMsg(msg);
		// Log.temp.info("sendScFillMsg");
	}

	/**
	 * 返回玩家之前经历的地图路径的id集合，历史靠近的地图在list的index较小的位置
	 * @return
	 */
	public List<Long> getStageLastIds() {
		List<Long> res = new ArrayList<>();
		JSONArray ja = Utils.toJSONArray(getHuman().getStageHistory());
		if(ja.isEmpty()){
			Log.game.error("humanObject.getStageLastIds human.getStageHistory()={}", getHuman().getStageHistory());
			return res;
		}
		for (Object obj : ja) {
			JSONArray jaTemp = Utils.toJSONArray(obj.toString());
			if(jaTemp.isEmpty()){
				Log.game.error("humanObject.getStageLastIds obj={}", obj.toString());
				continue;
			}
			res.add(jaTemp.getLongValue(0));
		}
		return res;
	}
	
	/**
	 * 获取玩家当前场景的场景Id
	 */
	public long getStageStageId() {
		long ret = 0;
		if (stageObj != null) {
			ret = stageObj.stageId;
		} else {
			ret = getStageLastIds().get(0);
		}
		return ret;
	}
	
	/**
	 * 获取玩家当前场景的关卡Sn（非副本地图，则为0）
	 */
	public int getStageInstSn() {
		int ret = 0;
		if (stageObj != null) {
			ret = stageObj.stageSn;
		}
		return ret;
	}
	
	/**
	 * 获取玩家当前场景的地图Sn
	 */
	public int getStageMapSn() {
		int ret = 0;
		if (stageObj != null) {
			ret = stageObj.mapSn;
		}
		return ret;
	}
	
	/**
	 * 获取玩家在地图历史中某张地图的坐标
	 * @param stageId
	 * @return
	 */
	public Vector2D getStagePos(long stageId) {
		Vector2D vector = new Vector2D();
		Human human = getHuman();

		JSONArray ja = Utils.toJSONArray(human.getStageHistory());
		if(ja.isEmpty()){
			Log.game.error("humanObject.getStagePos human.getStageHistory()={}", human.getStageHistory());
			return vector;
		}
		for (Object obj : ja) {
			JSONArray jaTemp = Utils.toJSONArray(obj.toString());
			if(jaTemp.isEmpty()){
				Log.game.error("humanObject.getStagePos obj={}", obj.toString());
				continue;
			}
			if (stageId == jaTemp.getLongValue(0)) {
				vector.x = jaTemp.getDoubleValue(2);
				vector.y = jaTemp.getDoubleValue(3);
			}
		}

		return vector;
	}

	public CharacterObject getCharacterFromSlave(Long id) {
		if (id == this.id) {
			return this;
		}
		UnitObject unitObj = partnerMap.get(id);
		if (unitObj instanceof CharacterObject) {
			return (CharacterObject) unitObj;
		}

		return null;
	}

	/**
	 * 通过ID活着的自己可以控制的对象， 如果是玩家自己就返回自己，如果是武将那么就返回武将
	 * @param id
	 * @return
	 */
	public UnitObject getUnitControl(long id) {
		if (id==-1 || this.id == id) {
			return this;
		} else {
			return partnerMap.get(id);
		}
	}
	
	/**
	 * 恢复体力
	 */
	private void recoveryAct() {
		Human human = this.getHuman();
		int lvCur = human.getLevel();
		ConfLevelExp confLevelExp = ConfLevelExp.get(lvCur);
		if (confLevelExp == null) {
			Log.table.error("ConfLevelExp配表错误，no sn ={} ", lvCur);
			return;
		}
		
		//计时器不停 体力满足 不加
		if(human.getAct() >= confLevelExp.staminaMax){
			return;
		}
		
		// 扣去间隔时间
		if (human.getAct() < confLevelExp.staminaMax) {		
			// === 应急措施 20170420 体力回复 Start ===
			// human数据异常 会有脏数据
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.getInfo(getHumanId());
			prx.listenResult(this::_result_recoveryAct,"humanObject",this);
			// === 应急措施 20170420 体力回复 End ===
		}
		
	}
	
	// === 应急措施 20170420 体力回复 Start ===
	private void _result_recoveryAct(Param results, Param context){
		HumanGlobalInfo info = results.get();
		if(info == null) return;
		HumanObject humanObj = Utils.getParamValue(context, "humanObject",null);
		if(humanObj == null) return;
		Human human =  humanObj.getHuman();
		if(human == null) return;
		//Log.human.info("######################bugshow");
		if(info.level != human.getLevel()){
			Log.human.info("  ========  _result_recoveryAct   ======== info.level = {} , human.getLevel() = {}",info.level,human.getLevel());
			return;
		}
		RewardHelper.reward(humanObj, EMoneyType.act_VALUE, 1, LogSysModType.ActValueRecovery);
	}
	// === 应急措施 20170420 体力回复 End ===
	
	/**
	 * 设置vip等级
	 * @param viplv
	 */
	public void setVipLv(int viplv) {
		getHuman().setVipLevel(viplv);
		//VipStoreManager.inst().sendSCVipChangeMsg(this);//返回vip等级变化
	}
	
	/**
	 * 禁言
	 * @param keepTime
	 */
	public void silence(long keepTime){
		long timeEnd = Port.getTime() + keepTime;
		long preTime = getHuman().getSilenceEndTime();
		if(preTime > timeEnd){
			//直接等于preTime,不要使用preTime + keepTime的方式，当preTime很大的时候，会导致无限累加(如势力禁言)
			timeEnd = preTime;
		}
		getHuman().setSilenceEndTime(timeEnd);
	}
	
	public void setStageSwitching(boolean switching) {
		this.isStageSwitching = switching;
		if(switching){
			ttSwitching = new TickTimer(10 * Time.SEC);
		}else{
			ttSwitching = null;
		}
	}
	
	public boolean isStageSwitching() {
		return isStageSwitching;
	}
	
	public int getSwitchFrom() {
		return switchFrom;
	}

	public void setSwitchFrom(int switchFrom) {
		this.switchFrom = switchFrom;
	}

	/**
	 * 检测玩家切地图状态，注意：异步调用后条件可能也有新的变化,调用的方法必须为public
	 * @param results
	 * @param context
	 * @return
	 */
	public boolean checkHumanSwitchState(Param results, Param context, boolean needTrans) {
		if(switchState != SwitchState.InStage) {
			if(needTrans) {
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				StackTraceElement e = stackTrace[2];
				String className = e.getClassName();
				String methodName = e.getMethodName();
				context.put(HumanObject.paramKey, null);//置空，不传递
				HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance(); 				
				prx.doHumanInCurrentThread(this.id, className, methodName, results, context);
			}
			return false;
		}
		return true;
	}
	/**
	 * 切到普通地图 (此方法不能直接被调用，离开副本调用StageManager.inst().quitToCommon)
	 * @param sn
	 * @param params
	 */
	public void quitToCommon(int sn, Object... params) {
		this.switchState = SwitchState.WaitGlobal;
		StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
		prx.quitToCommon(this, sn, params);
	}
	
	public HumanExtInfo getHumanExtInfo() {
		return extInfo;
	}
	
	
	private void outLootMap(){
		
		if(isInLootMapSignUp()){ // 是否在抢夺本报名队列中
			loginOutClearLootMapSignUp();
		}
		
		if(stageObj instanceof StageObjectLootMapSingle){// 是否在单人抢夺本
			// stageObj
			((StageObjectLootMapSingle)stageObj).forciblyCloseLootMapState();
			InstLootMapManager.inst().humanOut(id);
		}
		if(stageObj instanceof StageObjectLootMapMultiple){// 是否在多人抢夺本
			((StageObjectLootMapMultiple)stageObj).humanOut(id);
			InstLootMapManager.inst().humanOut(id);
		}
	}

	/**
	 * 是否在抢夺本报名队列
	 * @return
	 */
	public boolean isInLootMapSignUp(){
		return lootMapSingUpRoomId != -1L;
	}
	
	/**
	 * 清除抢夺本报名
	 */
	public void clearLootMapSignUp(){
		if(isInLootMapSignUp()){
			lootMapSingUpRoomId = -1L;
		}
	}
	
	private void loginOutClearLootMapSignUp(){
		clearLootMapSignUp();
		InstLootMapServiceProxy prx = InstLootMapServiceProxy.newInstance();
		prx.humanSignOut(id);
	}
	
	/**
	 * 设置抢夺本报名房间id
	 * @param lootMapSingUpRoomId
	 */
	public void setLootMapSignUpRoomId(long lootMapSingUpRoomId){
		this.lootMapSingUpRoomId = lootMapSingUpRoomId;
	}
	
	/**
	 * 清空humanObj
	 */
	public void cleanHumanObj(HumanObject humanObj){
		humanObj.humanGlobalInfo = null;
		
	}
	
	/**
	 * 记录并发送战斗信息
	 * @param teamType 战队类型
	 * @param fightType 战斗类型
	 * @param mapSn 地图sn
	 * @param stageId 切换的场景id
	 */
	public void sendFightInfo(ETeamType teamType, ECrossFightType fightType, int mapSn, long stageId) {
		String switchKey = mapSn + ":" + stageId;
		this.crossFightSwitchKey = switchKey;
		this.crossFightMapSn = mapSn;
		this.crossFightFinishMsg = null;
		this.crossFightTeamId = 0;
		this.fightTeam = teamType;
		
		SCCrossFightInfo.Builder msg = SCCrossFightInfo.newBuilder();
		msg.setAreaSwitchKey(switchKey);
		msg.setMapSn(mapSn);
		msg.setFightType(fightType);
		msg.setTeam(teamType);
		this.sendMsg(msg.build());
		
		if (fightType == ECrossFightType.FIGHT_WORLD_BOSS || 
				fightType == ECrossFightType.FIGHT_LOOTMAP_MULTIPLE) {
			// 这里不是真的切换进副本的流程，所以要重置部分数据
			this.isClientStageReady = false;
			this.isStageSwitching = true;
		}
	}
}
