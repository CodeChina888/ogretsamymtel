package game.worldsrv.instWorldBoss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import game.msg.Define.EActInstType;
import game.msg.Define.EWorldObjectType;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfWorldBoss;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.WorldBoss;
import game.worldsrv.enumType.FightPropName;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalc;
import game.worldsrv.support.Utils;

/**
 * 世界boss全局服务
 */
@DistrClass(servId = D.SERV_WORLDBOSS, importClass = {Human.class, WorldBoss.class, List.class})
public class WorldBossService extends GameServiceBase {
	private static final int HarmRank_Top3 = 3; // 记录前三名的名称
	private static final int CountPer10Sec = 100; // 10秒发送奖励的人数
	
	private boolean isOpen = false;// 是否开放活动副本，开放中boss才扣血，boss死亡则关闭活动副本
	private int actInstSn = 0;// 今日开启的活动副本sn
	
	private TickTimer ttClose = new TickTimer(); // 活动结束定时器
	private TickTimer ttRankAward = new TickTimer(); // 发放排行奖励定时器(10秒)
	private TickTimer ttRankAwardClear = new TickTimer(); // 发放排行奖励延时清除定时器
	private int harmAwardIndex = 0;// 排行奖励索引，记录发放到那个了
	private ArrayList<WBHarmData> harmRankAllList = new ArrayList<>();// 存放所有伤害值排行
	
	// boss信息表<actInstSn, 信息>
	private Map<Integer, WorldBoss> bossInfoMap = new HashMap<>();
	// boss伤害表<玩家ID, DPVEHarm>
	private Map<Long, WBHarmData> harmInfoMap = new HashMap<>();
	// boss伤害前几名：伤害从高到低排序
	private LinkedList<WBHarmData> harmRankTopList = new LinkedList<>();
	
	// boss地图中的玩家信息表<actInstSn, 玩家信息<玩家ID, Human>>
	private Map<Integer, Map<Long, Human>> allHumanMap = new HashMap<>();
	
	public WorldBossService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		Log.game.info("BossService.init() : start load Boss");
		// 加载世界boss数据
		DB dbPrx = DB.newInstance(WorldBoss.tableName);
		String whereSql = " WHERE `ActInstSn` > 0;";
		dbPrx.findByQuery(false, whereSql);
		dbPrx.listenResult(this::_result_loadBossData);
	}
	private void _result_loadBossData(Param results, Param context) {
		List<Record> records = results.get();
		if (records != null) {
			// 加载竞技数据
			for (Record r : records) {
				WorldBoss boss = new WorldBoss(r);
				// 保存数据
				bossInfoMap.put(boss.getActInstSn(), boss);
			}
		}
		// 添加未记录的boss数据
		List<ConfInstActConfig> confList = ConfInstActConfig.findBy(ConfInstActConfig.K.type, EActInstType.WorldBoss_VALUE);
		for (ConfInstActConfig conf : confList) {
			if (bossInfoMap.containsKey(conf.sn))
				continue;
			// 生成boss数据
			WorldBoss boss = createBoss(conf.instSn);
			if (boss != null) {
				boss.setActInstSn(conf.sn);// 设置所属活动副本sn
				boss.persist();// 持久化到数据库
				// 保存数据
				bossInfoMap.put(conf.sn, boss);
			}
		}
		
		this.actInstSn = getDailyOpenSn();// 获取今日开启的活动副本sn
		isOpenActInst(this.actInstSn);// 是否要开启活动副本
	}
	
	@Override
	public void pulseOverride() {
		if (ttRankAward.isPeriod(Port.getTime())) {
			harmAwardIndex += CountPer10Sec;//要记录起来
			giveRankAward(harmRankAllList, harmAwardIndex);// 发放排名奖励
		}
		if (ttRankAwardClear.isPeriod(Port.getTime())) {
			clearHarmRank();// 清除伤害及排名记录
		}
		if (ttClose.isPeriod(Port.getTime())) {
			ttClose.stop();// 关闭定时器
			closeActInst();// 关闭活动副本
		}
	}
	
	/**
	 * 每隔一分钟执行一次
	 */
	@ScheduleMethod(Utils.cron_Day_Min)
	public void _cron_Day_Hour() {
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置
			this.actInstSn = getDailyOpenSn();// 获取今日开启的活动副本sn
		}
		
		ConfInstActConfig conf = ConfInstActConfig.get(this.actInstSn);
		if (null != conf) {
			for (int h : conf.openHour) {
				if (hour == h) {
					isOpenActInst(this.actInstSn);
				}
			}
		}
	}
		
	/**
	 * 获取今日开启的活动副本sn
	 */
	private int getDailyOpenSn() {
		int actInstSn = 0;
		List<ConfInstActConfig> confList = ConfInstActConfig.findBy(ConfInstActConfig.K.type, EActInstType.WorldBoss_VALUE);
		for (ConfInstActConfig conf : confList) {
			// **判断开启时间
			int[] openWeekDay = conf.openWeekDay;
			int dayOfWeek = Utils.getDayOfWeek(Port.getTime());// 星期几
			boolean isOpen = false;// 是否开启
			if (0 == openWeekDay[0]) {// 判断星期几：0即每天，1-7即星期一到星期日
				isOpen = true;
			} else {
				for (int i = 0; i < openWeekDay.length; i++) {
					if (openWeekDay[i] == dayOfWeek) {
						isOpen = true;
						break;
					}
				}
			}
			if (isOpen) {// 开启
				actInstSn = conf.sn;
				break;
			}
		}
		return actInstSn;
	}
	
	/**
	 * 是否要开启活动副本
	 */
	private void isOpenActInst(int actInstSn) {
		ConfInstActConfig conf = ConfInstActConfig.get(actInstSn);
		if (null == conf) {
			Log.game.error("===openBossWar error in no find actInstSn={}", actInstSn);
			return;
		}
		
		
		long tmNow = Port.getTime();// 当前时间
		long tmZero = Utils.getTimeBeginOfToday(tmNow);// 当天0点时间
		for (int i = 0; i < conf.openHour.length; i++) {
			long tmLast = conf.totalMinute[i] * Time.MIN;// 持续时间
			long tmOpen = tmZero + conf.openHour[i] * Time.HOUR + conf.openMinute[i] * Time.MIN;// 开始时间
			long tmClose = tmOpen + tmLast;// 结束时间
			if (tmNow >= tmOpen && tmNow < tmClose) {
				// 检查boss是否已击杀过了
				WorldBoss boss = bossInfoMap.get(actInstSn);
				if (null == boss) {
					Log.game.error("===世界boss出问题了，没生成boss数据? actInstSn={}", actInstSn);
				} else {
					// 最后击杀的时间
					long tmLastKill = boss.getTimeLastKill();
					// 最后击杀的时间!=0，且在当前世界boss时段内，则意味着当前世界boss已经被击杀
					if (tmLastKill != 0 && tmLastKill >= tmOpen && tmLast <= tmClose) {
						this.isOpen = false;// 该时间段的boss已击杀过了，则关闭活动副本
					} else {
						this.isOpen = true;// 开启活动副本
						boss.setTimeLastStart(Port.getTime());
						// 设置活动结束定时器
						ttClose.start(tmOpen, tmLast);
					}
				}
				break;
			}
		}
	}
	/**
	 * 关闭活动副本
	 */
	private void closeActInst() {
		this.isOpen = false;
		// 计算所有参与者的伤害值排行，并发放排名奖励
		rankAllAndGiveAward();
		// 重置活动副本，根据击杀时间，计算是否升级boss
		resetActInst();
	}
	
	/**
	 * 重置活动副本，根据击杀时间，计算是否升级boss
	 */
	private void resetActInst() {
		WorldBoss boss = bossInfoMap.get(actInstSn);
		if (null == boss) {
			Log.game.error("===resetActInst no find actInstSn={}", actInstSn);
			return;
		}
		if (boss.getHpCur() > 0) {
			// boss没打死，通知所有人活动结束
			sendBossInfoToAll(actInstSn, boss);
		}
		
		// 重置怪物当前血量为最大血量
		boss.setMonsterHpCur(boss.getMonsterHpMax());
		boss.setHpCur(boss.getHpMax());
		
		// 根据击杀时间，计算是否升级boss
		long tmKillUse = boss.getTimeLastKill() - boss.getTimeLastStart();
		if (tmKillUse > 0 && tmKillUse < ParamManager.worldBossKillSecond * Time.SEC) {
			int nextSN = 0;// 升级后的BOSS副本SN
			int bossInstSn = boss.getBossInstSn();
			ConfWorldBoss conf = ConfWorldBoss.get(bossInstSn);
			if (conf == null) {
				Log.table.error("===ConfWorldBoss 配表错误，no find sn={}", bossInstSn);
				return;
			}
			nextSN = conf.nextSN;
			if (nextSN > 0) {
				ConfWorldBoss confNext = ConfWorldBoss.get(nextSN);
				if (confNext == null) {
					Log.table.error("===ConfWorldBoss 配表错误，no find sn={}", nextSN);
					return;
				}
				// 根据配表设置BOSS数据
				setBossInfo(boss, confNext.sn);
			}
		}
	}
		
	/**
	 * 生成boss数据
	 * @param bossInstSn 即boss副本SN
	 */
	private WorldBoss createBoss(int bossInstSn) {
		WorldBoss boss = new WorldBoss();
		// 根据配表设置BOSS数据
		boolean ret = setBossInfo(boss, bossInstSn);
		if (!ret) {// 创建失败
			Log.table.error("===WorldBossService.createBoss error in bossInstSn={}", bossInstSn);
			return null;
		}
		// 保存到数据库
		boss.setId(Port.applyId());
		return boss;
	}
	/**
	 * 根据配表设置BOSS数据
	 * @param bossInstSn 即boss副本SN
	 */
	private boolean setBossInfo(WorldBoss boss, int bossInstSn) {
		ConfWorldBoss confWorldBoss = ConfWorldBoss.get(bossInstSn);
		if (null == confWorldBoss) {
			Log.table.error("ConfWorldBoss 配表错误，no find sn={}", bossInstSn);
			return false;
		}
		ConfInstMonster confInstMonster = ConfInstMonster.get(confWorldBoss.armyId);
		if (null == confInstMonster) {
			Log.table.error("ConfInstMonster 配表错误，no find sn={}", confWorldBoss.armyId);
			return false;
		}
		
		int[] monsterSn = confInstMonster.monsterIds;
		int len = monsterSn.length;
		int[] monsterLv = new int[len];
		int[] monsterHpMax = new int[len];
		int[] monsterHpCur = new int[len];
		long hpMax = 0;
		int bossPos = 0;// boss所在位置
		for (int pos = 0; pos < len; pos++) {
			int sn = monsterSn[pos];
			if (sn <= 0) {
				// 同步设置其他数据
				monsterSn[pos] = 0;
				monsterLv[pos] = 0;
				monsterHpMax[pos] = 0;
				monsterHpCur[pos] = 0;
				continue;
			}
			ConfPartnerProperty confProperty = ConfPartnerProperty.get(sn);
			if (null == confProperty) {
				Log.table.error("===ConfPartnerProperty 配表错误，no find sn={}", sn);
				// 同步设置其他数据
				monsterSn[pos] = 0;
				monsterLv[pos] = 0;
				monsterHpMax[pos] = 0;
				monsterHpCur[pos] = 0;
				continue;
			}
			if (confProperty.roleType == EWorldObjectType.MonsterBoss_VALUE) {
				bossPos = pos;
			}
			// 同步设置其他数据
			monsterSn[pos] = sn;
			monsterLv[pos] = confProperty.lvl;
			PropCalc levelPropCalc = new PropCalc(Utils.toJSONString(confProperty.propName, confProperty.propValue));
			monsterHpMax[pos] = levelPropCalc.getInt(FightPropName.HpMax);
			monsterHpCur[pos] = monsterHpMax[pos];
			hpMax += monsterHpMax[pos];
		}
		boss.setBossPos(bossPos);// boss所在位置
		boss.setHpMax(hpMax);// 怪物最大血量之和
		boss.setHpCur(hpMax);// 怪物当前血量之和
		boss.setBossInstSn(bossInstSn);// 设置世界Boss的sn
		boss.setBossMapSn(confWorldBoss.mapSN);// 设置世界Boss所在地图sn
		boss.setMonsterSN(Utils.arrayIntToStr(monsterSn));
		boss.setMonsterLv(Utils.arrayIntToStr(monsterLv));
		boss.setMonsterHpMax(Utils.arrayIntToStr(monsterHpMax));
		boss.setMonsterHpCur(Utils.arrayIntToStr(monsterHpCur));
		return true;
	}
	
	/**
	 * 扣血
	 */
	@DistrMethod
	public void reduceHp(Human human, int actInstSn, long harmTotal, List<Integer> harmList) {
		WorldBoss boss = bossInfoMap.get(actInstSn);
		if (null == boss) {
			Log.game.error("===世界boss出问题了，没生成boss数据? actInstSn={}", actInstSn);
			return;
		}
		
		if (!this.isOpen) {
			// 监听返回
			port.returns("harmTotal", harmTotal, "worldBoss", boss);
			return;// boss未开放
		}
		
		long hpCur = boss.getHpCur();
		// boss死亡后，几秒内的伤害输出有效
		if (0 == hpCur) {
			if (Port.getTime() > ParamManager.worldBossDieSecond * Time.SEC + boss.getTimeLastKill()) {
				this.isOpen = false;
				// 监听返回
				port.returns("harmTotal", harmTotal, "worldBoss", boss);
				return;
			}
		}
		
		boolean isKiller = false;// 是否击杀BOSS的玩家
		if (hpCur > harmTotal) {// 扣血
			// 扣除当前总血量
			hpCur -= harmTotal;
			boss.setHpCur(hpCur);
			// 扣除每只的当前血量
			int[] monsterHpCur = Utils.strToIntArray(boss.getMonsterHpCur());
			for (int i = 0; i < harmList.size(); i++) {
				int harm = harmList.get(i);
				if (harm <= 0) {
					continue;
				}
				monsterHpCur[i] -= harm;
				if (monsterHpCur[i] < 0) {
					monsterHpCur[i] = 0;
				}
			}
			boss.setMonsterHpCur(Utils.arrayIntToStr(monsterHpCur));
		} else if (hpCur > 0) {// boss还没死，则此次为击杀
			// boss已被击杀，则开放设置为false
			this.isOpen = false;
			isKiller = true;
			// 设置boss死亡
			boss.setHpCur(0);// 死亡
			boss.setMonsterHpCur(Utils.arrayIntToStr(new int[boss.getMonsterHpMax().length()]));
			boss.setTimeLastKill(Port.getTime());
			boss.setKillerId(human.getId());
			boss.setKillerName(human.getName());
			boss.update(true);// 需立即保存到数据库
			
			// boss被杀了，通知所有人活动结束
			sendBossInfoToAll(actInstSn, boss);
			
			// 提前结束，奖励在几秒后开始发放
			ttClose.stop();
			ttClose.start(3 * ParamManager.worldBossDieSecond * Time.SEC);
		}
		
		// 记录伤害
		WBHarmData wbHarmData = harmInfoMap.get(human.getId());
		if (wbHarmData != null) {// 更新数据
			wbHarmData.harm += harmTotal;
			wbHarmData.isKiller = isKiller;
			wbHarmData.rankTime = Port.getTime();// 记录最近一次更新排行值的时间
		} else {// 新建数据
			wbHarmData = new WBHarmData(human);
			wbHarmData.harm += harmTotal;
			wbHarmData.isKiller = isKiller;
			wbHarmData.rankTime = Port.getTime();// 记录最近一次更新排行值的时间
			harmInfoMap.put(human.getId(), wbHarmData);
		}
		// 判断是否进入前几名
		if (harmRankTopList.isEmpty()) {
			harmRankTopList.add(wbHarmData);
		} else {
			// 先检查是否已经存在,存在就先删除,在添加或插入
			Iterator<WBHarmData> it = harmRankTopList.iterator();
			while (it.hasNext()) {
				WBHarmData data = it.next();
				if(null != data && data.humanId == wbHarmData.humanId){
					it.remove();
					break;
				}
			}
			if (harmRankTopList.isEmpty()) {
				harmRankTopList.add(wbHarmData);
			} else {
				int size = harmRankTopList.size();
				int index = binarySearch(harmRankTopList, wbHarmData.harm, 0, size-1);
				if (index < ParamManager.worldBossTopRankNum) {
					harmRankTopList.add(index, wbHarmData);
					size += 1;
				}
				if (size > ParamManager.worldBossTopRankNum) {
					harmRankTopList.pollLast();
				}
			}
		}
		
		// 监听返回
		port.returns("harmTotal", harmTotal, "worldBoss", boss);
	}
	
	/**
	 * 二分查找插入
	 * @param rankList 排行列表
	 * @param value 新的排行数据
	 * @param from 搜索的起始位置，从0开始
	 * @param to 搜索的结束位置，
	 */
	private int binarySearch(List<WBHarmData> rankList, int value, int from, int to) {
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle).harm >= value) {
				return binarySearch(rankList, value, middle+1, to);
			} else {
				return binarySearch(rankList, value, from, middle-1);
			}
		} else {
			if (rankList.get(from).harm >= value) {
				return from+1;
			} else {
				return from;
			}
		}
	}
	
	/**
	 * 是否开启活动副本
	 */
	@DistrMethod
	public void isOpen() {
		WorldBoss boss = bossInfoMap.get(this.actInstSn);
		WBData wbData = new WBData(boss);
		// 监听返回
		port.returns("isOpen", this.isOpen, "wbData", wbData);
	}
	
	/**
	 * 获取当前血量和等级等信息
	 */
	@DistrMethod
	public void getBossInfo(int actInstSn) {
		WorldBoss boss = bossInfoMap.get(actInstSn);
		WBData wbData = new WBData(boss);
		// 监听返回
		port.returns("wbData", wbData);
	}
	
	/**
	 * 获取前几名的伤害值排行
	 */
	@DistrMethod
	public void getHarmRankList(int actInstSn, long humanId) {
		int harmSelf = 0;// 自己的输出伤害值
		int rankSelf = 0;// 自己的输出排名（如果排名>param.worldBossTopRankNum，则为0）
		for (int i = 0; i < harmRankTopList.size(); i++) {
			WBHarmData h = harmRankTopList.get(i);
			if (h.humanId == humanId) {
				rankSelf = i+1;
				harmSelf = h.harm;
				break;
			}
		}
		if (rankSelf == 0) {// 没在排名榜里
			WBHarmData wbHarmData = harmInfoMap.get(humanId);
			if (wbHarmData != null) {
				harmSelf = wbHarmData.harm;
			}
		}
		// 监听返回
		port.returns("harmRankTopList", harmRankTopList, "harmSelf", harmSelf, "rankSelf", rankSelf);
	}
	
	/**
	 * 计算所有参与者的伤害值排行，并发放排名奖励
	 */
	private void rankAllAndGiveAward() {
		// 重置记录，导入本次伤害排行
		harmAwardIndex = 0;// 重置索引
		if (!harmRankAllList.isEmpty()) {
			harmRankAllList.clear();
		}
		harmRankAllList.addAll(harmInfoMap.values());
		sortRankHarm(harmRankAllList);
		
		// 设置发放定时器
		ttRankAward.start(Port.getTime(), 10 * Time.SEC);// 设置10秒的定时器
		giveRankAward(harmRankAllList, 0);// 发放排名奖励：从下标0开始发放
	}
	
	/**
	 * 降序排序：伤害值排行
	 * @param rankList
	 */
	private void sortRankHarm(ArrayList<WBHarmData> rankList) {
		Collections.sort(rankList, new Comparator<WBHarmData>() {
			@Override
			public int compare(WBHarmData u1, WBHarmData u2) {
				if (u1 == null || u2 == null)
					return 0;
				if (u1.harm > u2.harm)
					return -1;
				else if (u1.harm < u2.harm)
					return 1;
				else
					return (int) ((u1.rankTime - u2.rankTime) / Utils.I1000);
			}
		});
	}
	/**
	 * 分波发放排名奖励
	 * @param rankList 已排序的排行列表
	 * @param index 列表起始下标
	 */
	private void giveRankAward(ArrayList<WBHarmData> rankList, int index) {
		if (index >= rankList.size()) {
			ttRankAward.stop();// 关闭定时器
			// 延时5分钟(即一场战斗最长时间)后再清除，是为确保最后时刻进入世界boss的玩家无法获得结算信息的问题
			ttRankAwardClear.start(Port.getTime(), 5 * Time.MIN);
			return;
		}
		WorldBoss boss = bossInfoMap.get(actInstSn);
		if (null == boss) {
			Log.game.error("===giveRankAward no find actInstSn={}", actInstSn);
			return;
		}
		int count = 0;
		List<String> top3Names = null;
		for (int i = index; i < rankList.size(); i++) {
			WBHarmData wbHarmData = rankList.get(i);
			// 记录前三名的昵称
			if (i < HarmRank_Top3) {
				if (i == 0) {
					top3Names = new LinkedList<>();
				}
				top3Names.add(wbHarmData.name);
				if (i == HarmRank_Top3 - 1) {
					boss.setRankTopName(Utils.ListStringToStr(top3Names));
				}
			}
			
			// 下发排名奖励，排名即下标+1
			InstWorldBossManager.inst().giveRankAward(wbHarmData.humanId, boss.getBossInstSn(), i + 1);
			// 下发击杀奖励
			if (wbHarmData.isKiller) {
				InstWorldBossManager.inst().giveKillerAward(wbHarmData.humanId, boss.getBossInstSn());
			}
			
			count++;
			if (count >= CountPer10Sec) {// 超过一波发放最大数量返回
				return;
			}
		}
	}
	/**
	 * 清除伤害及排名记录
	 */
	private void clearHarmRank() {
		ttRankAwardClear.stop();
		harmRankTopList.clear();// 清除boss伤害前几名
		harmInfoMap.clear();// 清除boss伤害表
	}
	
	/**
	 * 玩家进入世界Boss地图
	 */
	@DistrMethod
	public void humanEnter(Human human, int actInstSn) {
		if (allHumanMap.containsKey(actInstSn)) {
			Map<Long, Human> others = allHumanMap.get(actInstSn);
			others.put(human.getId(), human);
		} else {
			Map<Long, Human> others = new HashMap<>();
			others.put(human.getId(), human);
			allHumanMap.put(actInstSn, others);
		}
	}
	/**
	 * 玩家离开世界Boss地图
	 */
	@DistrMethod
	public void humanLeave(long humanId, int actInstSn) {
		Map<Long, Human> others = allHumanMap.get(actInstSn);
		others.remove(humanId);
	}
	/**
	 * 获取其他玩家信息
	 */
	@DistrMethod
	public void getOtherHuman(long humanId, int actInstSn) {
		Map<Long, Human> otherHumanMap = new HashMap<>();
		Map<Long, Human> humanMap = allHumanMap.get(actInstSn);
		if (!humanMap.isEmpty()) {
			int num = ParamManager.worldBossShowMaxNum;// 世界BOSS - 同屏显示最大人数 
			if (num > humanMap.size())
				num = humanMap.size();
			
			// 优先获取boss伤害前几名的玩家信息
			for (WBHarmData wbHarmData : harmRankTopList) {
				Human human = humanMap.get(wbHarmData.humanId);
				if (human != null && human.getId() != humanId) {// 存在
					num--;
					otherHumanMap.put(human.getId(), human);
				}
			}
			// 还有剩余名额
			for (; num > 0; num--) {
				for (Human human : humanMap.values()) {
					if (human != null && human.getId() != humanId && !otherHumanMap.containsKey(human.getId())) {
						otherHumanMap.put(human.getId(), human);
					}
				}
			}
		}
		// 监听返回
		port.returns("otherHumanMap", otherHumanMap);
	}
	
	/**
	 * 获取上次挑战被记录的伤害玩家昵称和击杀玩家昵称
	 */
	@DistrMethod
	public void getUponRecordNames(int actInstSn) {
		WorldBoss boss = bossInfoMap.get(actInstSn);
		if (null == boss) {
			port.returns("uponTop3Names", "", "killerName", "");
			return;
		}
		port.returns("uponTop3Names", boss.getRankTopName(), "killerName", boss.getKillerName());
	}
	
	/**
	 * 发送世界BOSS信息给所有人
	 */
	private void sendBossInfoToAll(int actInstSn, WorldBoss worldBoss) {
		WBData wbData = new WBData(worldBoss);
		List<Long> humanIdList = new ArrayList<>();
		Map<Long, Human> humanMap = allHumanMap.get(actInstSn);
		if (null == humanMap) {
			return;
		}
		for (Human human : humanMap.values()) {
			humanIdList.add(human.getId());
		}
		
		// 返回
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfoList(humanIdList);
		prx.listenResult(this::_result_sendBossInfoToAll, "wbData", wbData);
	}
	private void _result_sendBossInfoToAll(Param results, Param context) {
		WBData wbData = Utils.getParamValue(context, "wbData", null);
		if (null == wbData) {
			Log.game.error("===_result_sendBossInfoToAll wbData=null");
			return;
		}
		List<HumanGlobalInfo> hgList = results.get();
		if (null == hgList) {
			Log.game.error("===_result_sendBossInfoToAll hgList=null");
			return;
		}
		for (HumanGlobalInfo hg : hgList) {
			if (hg != null) {
				// 发送广播世界BOSS活动结束
				HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(hg.nodeId, hg.portId, hg.id);
				prx.sendSCWorldBossEnd(wbData);
			}
		}
	}
}
