package game.worldsrv.tower;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;
import core.support.Time;
import game.msg.Define.ETowerDifficulty;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.compete.CompeteHumanObj;
import game.worldsrv.compete.CompeteServiceProxy;
import game.worldsrv.config.ConfTower;
import game.worldsrv.config.ConfTowerMatch;
import game.worldsrv.entity.TowerGlobal;
import game.worldsrv.entity.TowerHuman;
import game.worldsrv.entity.TowerPartner;
import game.worldsrv.enumType.RankType;
import game.worldsrv.param.ParamManager;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

/**
 * 爬塔全局服务
 */
@DistrClass(servId = D.SERV_TOWER, importClass = {List.class, Map.class, TowerRecord.class})
public class TowerService extends GameServiceBase {
	///////////////////////////////////////////////////////////
	/**
	 * 匹配层数模式
	 */
	public enum TowerMatchType {
		MatchHuman(1),
		MatchArmy(2),
		MatchRand(3);
		
		private int value;
		private TowerMatchType(int value) {
			this.value = value;
		}
		public int value() {
			return value;
		}
	}
	
	private static final int CountPerFind = 1000; // 每次查询1000
	
	private static final long Key_TowerGlobal = 1; // 爬塔全局数据表的唯一key值

	///////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////
	
	
	// 管理用于玩家匹配的爬塔玩家数据<玩家id, 爬塔玩家对象>，避免有玩家在取数据时，数据被清空
	private Map<Long, TowerHumanObj> towerHumanObjMap = new ConcurrentHashMap<>();
	
	// 生成爬塔的非玩家怪物数据
	private Map<Integer, TowerHumanObj> towerBotMap = new HashMap<>();
	
	// 爬塔全局信息
	private TowerGlobal towerGlobal = new TowerGlobal();
	// 记录爬塔每层人数的List
	private List<Integer> countList = new ArrayList<>();
	
	// 开服后数据是否加载完毕
	private boolean isLoad = false;

	public TowerService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		// 加载爬塔全局数据
		this.loadTowerGlobal();
		
		// 加载镜像玩家数据
		this.loadHumanData();
	}
	
	/**
	 * 加载爬塔全局数据
	 */
	private void loadTowerGlobal() {
		DB dbPrx = DB.newInstance(TowerGlobal.tableName);
		// 获得数量
		dbPrx.get(Key_TowerGlobal);
		// 同步等待结果
		Param result = dbPrx.waitForResult();
		Record record = result.get();
		// 没有查询到数据 ，则插入一条新的数据
		if (record == null) {
			towerGlobal.setId(Key_TowerGlobal);
			// 设置默认的过关人数
			countList = Utils.intToIntegerList(new int[ParamManager.towerMaxLayer + 1]);
			towerGlobal.setLayerPass(Utils.ListIntegerToStr(countList));
			
			// 设置默认的通过状态
			String str = "";
			for (int i = 0; i < ParamManager.towerMaxLayer; i++) {
				str += "0|0|0" + ",";
			}
			towerGlobal.setFirstPassState(str.substring(0, str.length()-1));
			
			// 设置赛季结束时间
			towerGlobal.setSeasonEndTime(TowerManager.inst().getSeasonEndTime());
			
			towerGlobal.persist();
			return;
		}
		
		// 设置数据
		towerGlobal.setRecord(record);
		if (towerGlobal.getLayerPass() == null || towerGlobal.getLayerPass().isEmpty()) {
			// 设置默认的过关人数
			countList = Utils.intToIntegerList(new int[ParamManager.towerMaxLayer + 1]);
			towerGlobal.setLayerPass(Utils.ListIntegerToStr(countList));
		} else {
			// 解析到service
			countList = Utils.strToIntList(towerGlobal.getLayerPass());
		}
		
		if (towerGlobal.getFirstPassState() == null || towerGlobal.getFirstPassState().isEmpty()) {
			// 设置默认的通过状态
			String str = "";
			for (int i = 0; i < ParamManager.towerMaxLayer; i++) {
				str += "0|0|0" + ",";
			}
			towerGlobal.setFirstPassState(str.substring(0, str.length()-1));
		}
		// 判断新的赛季时间
		this.getEndTime();
	}
	/**
	 * 加载镜像玩家数据
	 */
	private void loadHumanData() {
		long time = Port.getTime();
		DB dbPrx = DB.newInstance(TowerHuman.tableName);
		// 获得数量
		dbPrx.countBy(false);
		// 同步等待结果
		Param result = dbPrx.waitForResult();
		int numExist = result.get();
		// 没有查询到数据 / 首次加载
		if (numExist == 0 || isLoad) {
			return;
		}
		
		Log.game.info("TowerService.init() : start load CompeteRobot, numExist={}", numExist);
		int loopCount = numExist / CountPerFind;
		List<Record> records = null;
		// 分页查询
		for (int i = 0; i <= loopCount; i++) {
			dbPrx.findBy(false, i * CountPerFind, CountPerFind);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null)
				continue;
			// 加载爬塔玩家数据
			for (Record r : records) {
				TowerHuman tHuman = new TowerHuman(r);
				TowerHumanObj tHumanObj = new TowerHumanObj();
				tHumanObj.setHuman(tHuman);
				// 保存数据
				towerHumanObjMap.put(tHuman.getId(), tHumanObj);
			}
		}
		// 加载镜像伙伴数据
		this.loadPartnerData();
			
		Log.game.info("TowerService.init() : finish load CompeteRobot, numExist={}, costTime={}", numExist, Port.getTime() - time);
	}
	/**
	 * 加载镜像伙伴数据
	 */
	private void loadPartnerData() {
		DB dbPrx = DB.newInstance(TowerPartner.tableName);
		// 获得数量
		dbPrx.countBy(false);
		// 同步等待结果
		Param result = dbPrx.waitForResult();
		int numExist = result.get();
		int loopCount = numExist / CountPerFind;
		List<Record> records = null;
		// 分页查询
		for (int i = 0; i <= loopCount; i++) {
			dbPrx.findBy(false, i * CountPerFind, CountPerFind);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null)
				continue;
			// 加载爬塔伙伴数据
			for (Record r : records) {
				TowerPartner tPartner = new TowerPartner(r);
				TowerHumanObj tHumanObj = towerHumanObjMap.get(tPartner.getHumanId());
				if (tHumanObj == null) {
					continue;
				}
				tHumanObj.getPartnerMap().put(tPartner.getId(), tPartner);
			}
		}
	}
	
	/**
	 * 每个整点执行一次
	 */
	@ScheduleMethod(Utils.cron_Day_Hour)
	public void _cron_Day_Hour() {
		int hour = Utils.getHourOfDay(Port.getTime());
		// 每日重置时间
		if (hour == ParamManager.dailyHourReset) {
			// 是否首次加载：false
			isLoad = false;
			// 从竞技场镜像数据中，复制一份数据到爬塔里
			copyFromCompete();
			
			// 重设每日每层首次通关状态
			String str = "";
			for (int i = 0; i < ParamManager.towerMaxLayer; i++) {
				str += "0|0|0" + ",";
			}
			towerGlobal.setFirstPassState(str.substring(0, str.length()-1));
		}
		// 当前时间大于等于赛季结束时间，则重新设置新赛季结束时间
		if (Port.getTime() >= towerGlobal.getSeasonEndTime()) {
			towerGlobal.setSeasonEndTime(TowerManager.inst().getSeasonEndTime());
			// 清空爬塔排行榜数据
			RankGlobalServiceProxy rankGlobalPxy = RankGlobalServiceProxy.newInstance();
			rankGlobalPxy.clearRank(RankType.RankTower);
		}
	}
	
	/**
	 * 获取赛季结束时间，并且要判断是否重新设置赛季时间
	 */
	@DistrMethod
	public void getSeasonEndTime() {
		long endTime = this.getEndTime();
		port.returns("seasonEndTime", endTime);	
	}
	
	/**
	 * 首次加载爬塔数据
	 */
	@DistrMethod
	public void firstLoadTowerData() {
		// 是否首次加载：true
		isLoad = true;
		copyFromCompete();
	}
	
	/**
	 * GM加载爬塔数据
	 */
	@DistrMethod
	public void gmLoadTowerData() {
		copyFromCompete();
	}
	
	/**
	 * 从竞技场镜像数据中，复制一份数据到爬塔里
	 */
	private void copyFromCompete() {
		CompeteServiceProxy prx = CompeteServiceProxy.newInstance();
		prx.getTowerMirror();
		prx.listenResult(this::_result_copyToTower);
	}
	/**
	 * 跨天时从竞技场镜像中拷贝数据，设置爬塔镜像数据
	 */
	private void _result_copyToTower(Param results, Param context) {
		// <排名， 竞技场玩家对象>
		HashMap<Integer, CompeteHumanObj> cpHumanObjMap = Utils.getParamValue(results, "result", null);
		if (cpHumanObjMap.size() == 0) {
			return;
		}
		
		for(CompeteHumanObj cpHumanObj : cpHumanObjMap.values()) {
			// 战力为0，不作为参考对象
			if (cpHumanObj.cpHuman.getCombat() == 0) {
				Log.tower.error(" === 竞技场镜像数据异常 ，战力为0 ，id:{}===", cpHumanObj.cpHuman.getId());
				continue;
			}
			// 根据复制数据，查找当前是否存在该玩家镜像爬塔数据
			TowerHumanObj tHumanObj = towerHumanObjMap.get(cpHumanObj.cpHuman.getId());
			// 该玩家数据存在，更新数据
			if (tHumanObj != null) {
				// 镜像数据存在，更新数据
				tHumanObj.updateFromCompeteHumanObj(cpHumanObj);
			} else {
				// 镜像数据不存在
				tHumanObj = new TowerHumanObj();
				// 从竞技场镜像中复制数据到爬塔中
				tHumanObj.persistFromCompeteHumanObj(cpHumanObj);
				// 保存进内存
				towerHumanObjMap.put(tHumanObj.getHuman().getId(), tHumanObj);
			}
		}
		
		// 清除一周以前的数据
		long tmDailyReset = Utils.getTimeBeginOfToday(Port.getTime()) + ParamManager.dailyHourReset * Time.HOUR - Time.DAY * 7;
		
		// 复制完镜像数据后，清空今日跨天以前的数据
        Iterator<Entry<Long, TowerHumanObj>> iter = towerHumanObjMap.entrySet().iterator();  //hashMap的迭代器
        for (; iter.hasNext(); ){                                                  
            Map.Entry<Long, TowerHumanObj> entry = (Map.Entry<Long, TowerHumanObj>) iter.next();
            TowerHumanObj thObj = entry.getValue();
            // 记录时间小于一周以前跨天的时间，则从内存中清除
 			if (thObj.getHuman().getRecordTime() < tmDailyReset) {
 				iter.remove();
 			}
        }
        // 清除一周前的爬塔主角镜像数据
        String sql = Utils.createStr("delete from {} where `{}` <= ?", TowerHuman.tableName ,TowerHuman.K.RecordTime); 
        DB dbPxy = DB.newInstance(TowerHuman.tableName);
        dbPxy.sql(false, false, sql, tmDailyReset);
        // 清除一周前爬塔伙伴镜像数据
        sql = Utils.createStr("delete from {} where `{}` <= ?", TowerPartner.tableName ,TowerPartner.K.RecordTime); 
        dbPxy = DB.newInstance(TowerPartner.tableName);
        dbPxy.sql(false, false, sql, tmDailyReset);
        
        // 爬塔每一层人数清空
        List<Long> passList = new ArrayList<>(ParamManager.towerMaxLayer + 1);
        towerGlobal.setLayerPass(Utils.ListLongToStr(passList));
	}
	
	/**
	 * 匹配玩家爬塔镜像数据
	 * （在这之前需要对玩家的匹配信息进行处理）
	 */
	@DistrMethod
	public void matchTowerHuman(TowerRecord towerRecord) {
		// 爬塔数据记录
		if (towerRecord == null) {
			port.returns("result" , false);
			return;
		}
		// 爬塔配置表
		Collection<ConfTower> confTowerList = ConfTower.findAll();
		// 爬塔，战力偏差表
		ConfTowerMatch confTM = TowerManager.inst().getConfTowerMatch(towerRecord.getTower().getMatchCombat());
		
		for (ConfTower confTower : confTowerList) {
			// 匹配类型
			int matchType = confTower.matching;
			if (matchType == TowerMatchType.MatchRand.value()) {
				// 权重得到是匹配军队还是玩家
				matchType = Utils.getRandRange(confTower.common2); 
			}
			
			int armyId = 0;
			if (matchType == TowerMatchType.MatchArmy.value()) {
				// 范围里随机一个军队id
				int[] armyAry = confTower.common1;
				int index =  Utils.randomBetween(0, armyAry.length-1);
				armyId = armyAry[index];
			}
			// 权重得出条件索引
			int cdIndex = Utils.getRandRange(confTower.conditionWeight);
			
			// 匹配难度1
			matchLayerHumanInfo(towerRecord, confTower, confTM, ETowerDifficulty.TowerDiffLv1_VALUE, armyId, cdIndex);
			// 匹配难度2
			matchLayerHumanInfo(towerRecord, confTower, confTM, ETowerDifficulty.TowerDiffLv2_VALUE, armyId, cdIndex);
			// 匹配难度3
			matchLayerHumanInfo(towerRecord, confTower, confTM, ETowerDifficulty.TowerDiffLv3_VALUE, armyId, cdIndex);
		}
		
		// 修改所有层数的匹配数据
		towerRecord.modifyAllLayer();
		port.returns("result" , true, "towerRecord", towerRecord);
	}
	/**
	 * 匹配一层的数据
	 * @param towerRecord 玩家爬塔数据
	 * @param confTower 当前层配置表
	 * @param diff 难度
	 * @param armyId 军队id(如果是玩家则为0)
	 * @param cdIndex 条件索引
	 */
	private void matchLayerHumanInfo(TowerRecord towerRecord, ConfTower confTower, ConfTowerMatch confTM, int diff, int armyId, int cdIndex) {
		// 匹配战力
		int matchCb = towerRecord.getTower().getMatchCombat();
		
		// 战力偏移值
		int[] offset = null;
		// 战力系数区间值
		int[] range = null;
		switch (diff) {
		case ETowerDifficulty.TowerDiffLv1_VALUE:
			offset = confTM.correction1;
			range = confTower.Range1;
			break;
		case ETowerDifficulty.TowerDiffLv2_VALUE:
			offset = confTM.correction2;
			range = confTower.Range2;
			break;
		case ETowerDifficulty.TowerDiffLv3_VALUE:
			offset = confTM.correction3;
			range = confTower.Range3;
			break;
		}
		// 战力区间 
		// 最小战斗力 = （偏移最小值:万份比 + 区间最小值:万份比 ）/ 10000 * 匹配战斗力
		int combatMin = (int) ( (offset[0] + range[0]) * matchCb / Utils.I10000);
		int combatMax = (int) ( (offset[1] + range[1]) * matchCb / Utils.I10000);
		
		// 默认战力（如果最终战力为最大战力，则战斗前根据公式将属性捏成战斗力对应的属性）
		int defaultCb = combatMax;
		// 默认的匹配结果id为军队id
		long defaultId = armyId;
		
		// 如果，军队id为0，则意味着这一层需要匹配的是玩家
		if (armyId == 0) {
			TowerHumanObj thObj = matchTowerHumanObj(combatMin, combatMax);
			// 如果没有匹配到数据
			if (thObj == null) {
				// 随机取一个对象作为匹配对象，战斗力为默认战力（区间的最大值）
				int randIndex = Utils.randomBetween(0, towerHumanObjMap.size()-1);
				List<TowerHumanObj> tmpList = new ArrayList<>(towerHumanObjMap.values());
				thObj = tmpList.get(randIndex);
			} else {
				// 有匹配到玩家，则使用玩家战力
				defaultCb = thObj.getCombat();
			}
			
			// 匹配到的玩家id
			defaultId = thObj.getHuman().getId();
		}
		
		// 匹配到的数据
		TowerLayerJSON tlJSON = new TowerLayerJSON();
		// 该层匹配结果的层数
		tlJSON.layer = confTower.sn;
		// 该层匹配结果的id
		tlJSON.humanId = defaultId;
		// 该层匹配结果的战斗力
		tlJSON.combat = defaultCb;
		// 该层匹配的胜利条件
		tlJSON.setCondition(confTower, cdIndex);
		
//		Log.tower.info(" \n =============== \n layer:" + confTower.sn + "\n Diff:" + diff +"\n combat:" + defaultCb + "\n===============");
		
		
		// 修改/新增一层爬塔数据
		towerRecord.putData(tlJSON, diff);
	}
	/**
	 * 根据玩家战力，取的匹配的玩家对象
	 */
	private TowerHumanObj matchTowerHumanObj(int combatMin, int combatMax) {
		List<TowerHumanObj> thObjList = new ArrayList<>();
		// 爬塔匹配池中的数据(由于调用次数过多，循环次数可能太多)
		for (TowerHumanObj thObj : towerHumanObjMap.values()) {
			// 作为匹配对象的战斗力
			int combat = thObj.getCombat();
			// 满足条件的插入list中
			if (combat >= combatMin && combat <= combatMax) {
				thObjList.add(thObj);
			}
		}
		// 如果没有满足条件的，则返回空
		if (thObjList.size() == 0 ) {
			return null;
		}
		int randIndex = Utils.randomBetween(0, thObjList.size()-1);
		return thObjList.get(randIndex);
	}
	
	/**
	 * 获取挑战对象的爬塔信息，从信息中取出伙伴
	 */
	@DistrMethod
	public void getTowerPartner(long humanId) {
		TowerHumanObj towerHumanObj = towerHumanObjMap.get(humanId);
		if (towerHumanObj == null) {
			towerHumanObj = towerBotMap.get(humanId);
		}
		port.returns("towerHumanObj", towerHumanObj);
	}
	
	/**
	 * 修改每一层的挑战人数
	 */
	@DistrMethod
	public void changeLayerCount(int willFightLayer) {
		Log.tower.info("willFightLayer" + willFightLayer);
		// 获取将要挑战层数对应的listIndex
		int willLayerIndex = willFightLayer - 1;
		if (willFightLayer > countList.size()) {
			for (int i = countList.size(); i < willFightLayer; i++) {
				countList.add(0);
			}
		}
		
		// 将要挑战层的挑战人数
		int willLayerCount = countList.get(willLayerIndex);
		
		// 不是第一层，则要处理刚通过层数的挑战人数-1
		if (willLayerIndex != 0) {
			// 刚通过的层数index = 将要挑战层数的index - 1
			int passLayerIndex = willLayerIndex - 1;  // eg:将要挑战的第二层index是1：通过的是第一层index是0 = 1 - 1   
			// 刚通过层的挑战人数
			int passLayerCount = countList.get(passLayerIndex);
			countList.set(passLayerIndex, --passLayerCount);
		}
		// 将要挑战层数的人数+1
		countList.set(willLayerIndex, ++willLayerCount);
		
		towerGlobal.setLayerPass(Utils.ListIntegerToStr(countList));
		
		port.returns("countList", getLayerCountList());
	}
	
	/**
	 * 获取每一层的人数
	 */
	@DistrMethod
	public void getLayerInfo() {
		long endTime = this.getEndTime();
		port.returns("countList", getLayerCountList(), "seasonEndTime", endTime);
	}
	/**
	 * 每层人数的list
	 */
	private List<Integer> getLayerCountList() {
		return countList;
	}
	/**
	 * 赛季结束时间 
	 */
	private long getEndTime() {
		long seasonEndTime = towerGlobal.getSeasonEndTime();
		// 当前时间大于等于赛季结束时间，则重新设置新赛季结束时间
//		if (Port.getTime() >= seasonEndTime) {
//			// 设置新赛季
//			towerGlobal.setSeasonEndTime(TowerManager.inst().getSeasonEndTime());
//			seasonEndTime = towerGlobal.getSeasonEndTime();
//		}
		return seasonEndTime;
	}
	
	/**
	 * 设置是否有人通过该层数
	 */
	@DistrMethod
	public void processFirstPass(int layer, int diff) {
		String firstPassState = towerGlobal.getFirstPassState();
		// 每一层的状态String列表
		List<String> layerPassList = Utils.strToStringList(firstPassState);
		if (layerPassList.size() < layer) {
			for (int i = layerPassList.size(); i < layer; i++) {
				layerPassList.add("0|0|0");
			}
		}
		int layerIndex = layer - 1;
		// 获取这一层的状态
		int[] states = Utils.strToIntArraySplit(layerPassList.get(layerIndex));
		int diffState = 0;
		if (diff == ETowerDifficulty.TowerDiffLv1_VALUE) {
			diffState = states[0];
			states[0] = 1;
		} else if (diff == ETowerDifficulty.TowerDiffLv2_VALUE) {
			diffState = states[1];
			states[1] = 1;
		} else if (diff == ETowerDifficulty.TowerDiffLv3_VALUE) {
			diffState = states[2];
			states[2] = 1;
		}
		// 是首次通过
		if (diffState == 0) {
			layerPassList.set(layerIndex, Utils.createStr("{}|{}|{}", states[0],states[1],states[2]));
			// 数据库设置
			towerGlobal.setFirstPassState(Utils.ListStringToStr(layerPassList));
			port.returns("isFirst", true);
		} else {
			port.returns("isFirst", false);
		}
	}
	
	/**
	 * 预留热更修补方法
	 */
	@DistrMethod()
	public void prevent_method() {
		prevent_method1();
	}
	/**
	 * 预留热更修补方法
	 */
	private void prevent_method1() {
		
	}
}
