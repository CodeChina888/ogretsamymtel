package game.worldsrv.rank;

import game.msg.Define.ECastellanType;
import game.msg.Define.EMailType;
import game.msg.Define.ERankType;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.compete.CompeteServiceProxy;
import game.worldsrv.config.ConfActivityServerCompetition;
import game.worldsrv.config.ConfMainCityShow;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActServerCompetition;
import game.worldsrv.entity.Castellan;
import game.worldsrv.entity.RankFairyland;
import game.worldsrv.entity.RankGuild;
import game.worldsrv.entity.RankInstance;
import game.worldsrv.entity.RankLevel;
import game.worldsrv.entity.RankSumCombat;
import game.worldsrv.entity.RankTower;
import game.worldsrv.entity.RankVip;
import game.worldsrv.enumType.RankIntType;
import game.worldsrv.enumType.RankType;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.entity.EntityBase;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;

@DistrClass(servId = D.SERV_RANK, importClass = { RankType.class, EntityBase.class, List.class, RankData.class,
		Set.class, ERankType.class })
public class RankGlobalService extends GameServiceBase {
	// 全部排行（例如保存前一千名排行，ParamManager.rankTopRecordMaxNum）
	private Map<Long, RankLevel> mapAllLevel = new HashMap<>();// key=humanId
	private Map<Long, RankVip> mapAllVip = new HashMap<>();// key=humanId
	private Map<Long, RankTower> mapAllTower = new HashMap<>();// key=humanId 爬塔
	private Map<Long, RankSumCombat> mapAllSumCombat = new HashMap<>();// key=humanId
	private Map<Long, RankInstance> mapAllInstance = new HashMap<>();// key=humanId 副本
	private Map<Long, RankFairyland> mapAllFairyland = new HashMap<>();// key=humanId 洞天福地
	private Map<Long, RankGuild> mapAllGuild = new HashMap<>();// key=humanId 洞天福地

	// 实时排行榜
	private LinkedList<RankSumCombat> combatList = new LinkedList<RankSumCombat>();// 总战斗力
	private LinkedList<RankLevel> levelList = new LinkedList<RankLevel>();// 等级排行榜
	private LinkedList<RankInstance> instanceList = new LinkedList<RankInstance>();// 等级排行榜
	private LinkedList<RankFairyland> fairylandList = new LinkedList<RankFairyland>();// 洞天福地排行榜
	private LinkedList<RankTower> towerList = new LinkedList<RankTower>();// 爬塔排行榜
	private LinkedList<RankGuild> guildList = new LinkedList<RankGuild>();// 公会排行榜
	private static int countPerFind = 1000; // 每次查询1000

	// 活动处理
	private TickTimer activityPulseTimer = new TickTimer(); 
	private LinkedList<RankSumCombat> actCombatList = new LinkedList<RankSumCombat>();//总战斗力
	private int harmAwardIndex = 0;
	private static final int CountPer10Sec = 100;//每10秒处理100条数据
	public RankGlobalService(GamePort port) {
		super(port);

	}

	@Override
	protected void init() {
		// 遍历初始化各个排行榜
		List<Record> records;
		// 获得战力排行榜数量

		// 获得总战力排行榜数量
		DB dbPrxRankSumCombat = DB.newInstance(RankSumCombat.tableName);
		// 获取数量
		dbPrxRankSumCombat.countBy(false);
		Param rankSumCombat = dbPrxRankSumCombat.waitForResult();
		int num = rankSumCombat.get();
		int loopCount = (int) Math.ceil((float) num / countPerFind);
		if (num > 0) {
			for (int i = 0; i <= loopCount; i++) {
				String whereSql = Utils.createStr(" WHERE `Rank` <= {} limit {},{};",
						ParamManager.rankTopRecordMaxNum[RankIntType.SumCombat.value()], i * countPerFind,
						countPerFind);
				dbPrxRankSumCombat.findByQuery(false, whereSql);
				rankSumCombat = dbPrxRankSumCombat.waitForResult();
				records = rankSumCombat.get();
				if (records == null)
					continue;
				for (Record r : records) {
					RankSumCombat rank = new RankSumCombat(r);
					addNewToRank(rank, this.combatList, this.mapAllSumCombat);
				}
			}
		}

		// 获得等级排行数量
		DB dbPrxRankLevel = DB.newInstance(RankLevel.tableName);
		dbPrxRankLevel.countBy(false);
		Param rankLevel = dbPrxRankLevel.waitForResult();
		num = rankLevel.get();
		loopCount = (int) Math.ceil((float) num / countPerFind);
		if (num > 0) {
			for (int i = 0; i <= loopCount; i++) {
				String whereSql = Utils.createStr(" WHERE `Rank` <= {} limit {},{};",
						ParamManager.rankTopRecordMaxNum[RankIntType.Level.value()], i * countPerFind, countPerFind);
				dbPrxRankLevel.findByQuery(false, whereSql);
				rankLevel = dbPrxRankLevel.waitForResult();
				records = rankLevel.get();
				if (records == null)
					continue;
				for (Record r : records) {
					RankLevel rank = new RankLevel(r);
					addNewToRank(rank, this.levelList, this.mapAllLevel);
				}
			}
		}

		// 获得爬塔排行数量
		DB dbPrxRankTower = DB.newInstance(RankTower.tableName);
		dbPrxRankTower.countBy(false);
		Param rankTower = dbPrxRankTower.waitForResult();
		num = rankTower.get();
		loopCount = (int) Math.ceil((float) num / countPerFind);
		if (num > 0) {
			for (int i = 0; i <= loopCount; i++) {
				String whereSql = Utils.createStr(" WHERE `Rank` <= {} limit {},{};",
						ParamManager.rankTopRecordMaxNum[RankIntType.Tower.value()], i * countPerFind, countPerFind);
				dbPrxRankTower.findByQuery(false, whereSql);
				rankTower = dbPrxRankTower.waitForResult();
				records = rankTower.get();
				if (records == null)
					continue;
				for (Record r : records) {
					RankTower rank = new RankTower(r);
					addNewToRank(rank, this.towerList, this.mapAllTower);
				}
			}
		}

		// 获得副本排行数量
		DB dbPrxRankInstance = DB.newInstance(RankInstance.tableName);
		dbPrxRankInstance.countBy(false);
		Param rankInstance = dbPrxRankInstance.waitForResult();
		num = rankInstance.get();
		loopCount = (int) Math.ceil((float) num / countPerFind);
		if (num > 0) {
			for (int i = 0; i <= loopCount; i++) {
				String whereSql = Utils.createStr(" WHERE `Rank` <= {} limit {},{};",
						ParamManager.rankTopRecordMaxNum[RankIntType.Instance.value()], i * countPerFind, countPerFind);
				dbPrxRankInstance.findByQuery(false, whereSql);
				rankInstance = dbPrxRankInstance.waitForResult();
				records = rankInstance.get();
				if (records == null)
					continue;
				for (Record r : records) {
					RankInstance rank = new RankInstance(r);
					addNewToRank(rank, this.instanceList, this.mapAllInstance);
				}
			}
		}

		// 获得洞天福地排行数量
		DB dbPrxRankFairyland = DB.newInstance(RankFairyland.tableName);
		dbPrxRankFairyland.countBy(false);
		Param rankFairyland = dbPrxRankFairyland.waitForResult();
		num = rankFairyland.get();
		loopCount = (int) Math.ceil((float) num / countPerFind);
		if (num > 0) {
			for (int i = 0; i <= loopCount; i++) {
				String whereSql = Utils.createStr(" WHERE `Rank` <= {} limit {},{};",
						ParamManager.rankTopRecordMaxNum[RankIntType.Fairyland.value()], i * countPerFind,
						countPerFind);
				dbPrxRankFairyland.findByQuery(false, whereSql);
				rankFairyland = dbPrxRankFairyland.waitForResult();
				records = rankFairyland.get();
				if (records == null)
					continue;
				for (Record r : records) {
					RankFairyland rank = new RankFairyland(r);
					addNewToRank(rank, this.fairylandList, this.mapAllFairyland);
				}
			}
		}

		// 初始化仙府排行榜
		DB guildDBprx = DB.newInstance(RankGuild.tableName);
		guildDBprx.countBy(false);
		Param rankGuild = guildDBprx.waitForResult();
		num = rankGuild.get();
		loopCount = (int) Math.ceil((float) num / countPerFind);
		if (num > 0) {
			for (int i = 0; i <= loopCount; i++) {
				String whereSql = Utils.createStr(" WHERE `Rank` <= {} limit {},{};",
						ParamManager.rankTopRecordMaxNum[RankIntType.Guild.value()], i * countPerFind, countPerFind);
				guildDBprx.findByQuery(false, whereSql);
				rankGuild = guildDBprx.waitForResult();
				records = rankGuild.get();
				if (records == null)
					continue;
				for (Record r : records) {
					RankGuild rank = new RankGuild(r);
					addNewToRank(rank, this.guildList, this.mapAllGuild);
				}
			}
		}
		DB db = DB.newInstance(ActServerCompetition.tableName);
		db.countAll(false);
		Param paramCount = db.waitForResult();
		int count = paramCount.get();
		if (count > 0) {			
			int page = count / countPerFind;
			for (int i = 0; i <= page; i++) {
				db.findBy(false, i * countPerFind, countPerFind);
				Param params = db.waitForResult();
				List<Record> rList = params.get();
				for (Record record : rList) {					
					ActServerCompetition competition = new ActServerCompetition(record);
					if(competition != null){
						addActCombat(competition);
					}
				}
			}
		}
		Log.game.info("RankLoad finish");
	}
	public void addActCombat(ActServerCompetition competition){
		actCombatList.add(analysisActServerCompetition(competition));
	}
	private RankSumCombat analysisActServerCompetition(ActServerCompetition competition){
		RankSumCombat combat = new RankSumCombat();
		JSONObject jo = Utils.toJSONObject(competition.getParamJSON());
		combat.setId(competition.getId());
		combat.setRank(jo.getIntValue("Rank"));
		combat.setHumanId(jo.getIntValue("HumanId"));
		combat.setName(jo.getString("Name"));
		combat.setSumCombat(jo.getIntValue("SumCombat"));
		combat.setHeadSn(jo.getIntValue("HeadSn"));
		combat.setLv(jo.getIntValue("Lv"));
		combat.setModelSn(jo.getIntValue("ModelSn"));
		combat.setCombat(jo.getIntValue("Combat"));
		return combat;
	}
	/**
	 * 获取排行榜
	 */
	@DistrMethod
	public void getRank(ERankType rankType) {
		List<? extends EntityBase> list = new ArrayList<>();
		switch (rankType) {
		case RankTypeLevel:
			list = this.levelList;
			break;
		case RankTypeSumCombat:
			list = this.combatList;
			break;
		case RankTypeInstance:
			list = this.instanceList;
			break;
		case RankTypeFairyland:
			list = this.fairylandList;
			break;
		case RankTypeTower:
			list = this.towerList;
			break;
		case RankTypeGuild:
			list = this.guildList;
		default:
			break;
		}
		port.returns(list);
	}

	/**
	 * 获取排行名次
	 */
	private int getRanking(RankType rankType, long humanId) {
		int ranking = Integer.MAX_VALUE;
		switch (rankType) {
		case RankLevel:
			RankLevel rLv = mapAllLevel.get(humanId);
			if (rLv != null) {
				ranking = rLv.getRank();
			}
			break;
		case RankSumCombat:
			RankSumCombat rSC = mapAllSumCombat.get(humanId);
			if (rSC != null) {
				ranking = rSC.getRank();
			}
			break;
		case RankInstance:
			RankInstance rInst = mapAllInstance.get(humanId);
			if (rInst != null) {
				ranking = rInst.getRank();
			}
			break;
		case RankFairyland:
			RankFairyland rFL = mapAllFairyland.get(humanId);
			if (rFL != null) {
				ranking = rFL.getRank();
			}
			;
			break;
		case RankTower:
			RankTower rTower = mapAllTower.get(humanId);
			if (rTower != null) {
				ranking = rTower.getRank();
			}
			break;
		default:
			break;
		}
		return ranking;
	}

	/**
	 * 获取等级排行榜
	 */
	@DistrMethod
	public void getLvRank(int topCount) {
		if (topCount <= 100 && topCount > 0) {
			JSONArray ja = new JSONArray();
			int i = 0;
			for (RankLevel r : levelList) {
				if (i > topCount) {
					break;
				}
				JSONObject rankjo = new JSONObject();
				rankjo.put("userId", r.getAccountId());
				rankjo.put("level", r.getLevel());
				rankjo.put("rank", r.getRank());
				ja.add(rankjo);
				i++;
			}
			port.returns(ja);
		}
	}

	/**
	 * 多条插入洞天福地
	 */
	@DistrMethod
	public void addNews(Set<RankData> data, RankType type, boolean isNeedUpdate) {
		for (RankData rankData : data) {
			addNew(rankData, RankType.RankFairyland);
		}
		port.returns("can", true);
	}

	/**
	 * 加入数据到全部排行(二分插入)
	 */
	@DistrMethod
	public void addNew(RankData data, RankType type) {
		long humanId = data.humanId;// 角色ID
		switch (type) {

		case RankSumCombat: {
			RankSumCombat rankMostCombat = RankManager.inst().newRankSumCombat(data);
			mapAllSumCombat.put(humanId, rankMostCombat);
			updateAllRankData(data);// 更新数据到全部排行
		}
			break;
		case RankLevel: {
			RankLevel rankLevel = RankManager.inst().newRankLevel(data);
			mapAllLevel.put(humanId, rankLevel);
			updateAllRankData(data);// 更新数据到全部排行
		}
			break;
		case RankVip: {
			RankVip rankVip = RankManager.inst().newRankVip(data);
			mapAllVip.put(humanId, rankVip);
			updateAllRankData(data);// 更新数据到全部排行
		}
			break;
		case RankTower: {
			RankTower rankTower = RankManager.inst().newRankTower(data);
			mapAllTower.put(humanId, rankTower);
			updateAllRankData(data);// 更新数据到全部排行
		}
			break;
		case RankInstance: {
			RankInstance rankInstance = RankManager.inst().newRankInstance(data);
			mapAllInstance.put(humanId, rankInstance);
			updateAllRankData(data);// 更新数据到全部排行
		}
			break;
		// 洞天福地排行
		case RankFairyland: {
			RankFairyland rankFairyland = RankManager.inst().newRankFairyland(data);
			mapAllFairyland.put(humanId, rankFairyland);
			updateAllRankData(data);
		}
			break;
		case RankGuild: {
			updateAllRankData(data);
		}
			break;
		default:
			break;
		}
		// 当前排名
		int ranking = getRanking(type, humanId);
		port.returns("ranking", ranking);
	}

	/**
	 * 更新数据到全部排行
	 */
	@DistrMethod
	public void updateAllRankData(RankData data) {
		if (data == null)
			return;

		long humanId = data.humanId;// 角色ID
		// 更新数据到总战力排行
		if (humanId > 0 && mapAllSumCombat.containsKey(humanId)) {
			// 把map里面的数据更新
			RankSumCombat rankSumCombat = mapAllSumCombat.get(humanId);
			RankManager.inst().updateRankSumCombat(data, rankSumCombat);
			// 二分查找插入List
			addNewToRank(rankSumCombat, this.combatList, this.mapAllSumCombat);
		}

		// 更新数据到等级排行
		if (humanId > 0 && mapAllLevel.containsKey(humanId)) {
			RankLevel rankLevel = mapAllLevel.get(humanId);
			RankManager.inst().updateRankLevel(data, rankLevel);
			// 二分查找插入List
			addNewToRank(rankLevel, this.levelList, this.mapAllLevel);
		}
		// 更新数据到爬塔排行
		if (humanId > 0 && mapAllTower.containsKey(humanId)) {
			// 新版
			RankTower rankTower = mapAllTower.get(humanId);
			// 二分查找插入List
			if (data.towerScore > 0) {
				RankManager.inst().updateRankTower(data, rankTower);
				addNewToRank(rankTower, this.towerList, this.mapAllTower);
			}
		}
		// 更新数据到副本排行
		if (humanId > 0 && mapAllInstance.containsKey(humanId)) {
			RankInstance rankInstance = mapAllInstance.get(humanId);
			if (data.stars > 0) {
				RankManager.inst().updateRankInstance(data, rankInstance);
				// 二分查找插入List
				addNewToRank(rankInstance, this.instanceList, this.mapAllInstance);
			}
		}
		// 更新数据到洞天福地排行
		if (humanId > 0 && mapAllFairyland.containsKey(humanId)) {
			RankFairyland rankFairyland = mapAllFairyland.get(humanId);
			if (data.lootMapScore > 0) {
				RankManager.inst().updateRankFairyland(data, rankFairyland);
				// 二分查找插入List
				addNewToRank(rankFairyland, this.fairylandList, this.mapAllFairyland);
			}
		}
		//更新数据到工会排行榜
		if (data.guildId > 0) {
			RankGuild rankguild = mapAllGuild.get(data.guildId);
			if (rankguild == null) {
				rankguild = new RankGuild();
				mapAllGuild.put(data.guildId, rankguild);
			}
			RankManager.inst().buildRankGuild(data, rankguild);
			// 二分查找插入List
			addNewToRank(rankguild, this.guildList, this.mapAllGuild);
		}
	}

	/**
	 * 插入一条爬塔排行榜
	 */
	private void addNewToRank(RankTower rank, LinkedList<RankTower> rankList, Map<Long, RankTower> rankMap) {

		if (rank == null) {
			return;
		}
		// 校验是否超过榜单数量限制
		int Fairyland_Rank_maxNum = ParamManager.rankTopShowNum[RankIntType.Fairyland.value()];
		if (rankList.size() >= Fairyland_Rank_maxNum) {
			if (rank.getGrade() <= rankList.get(rankList.size() - 1).getGrade()) {
				return;
			}
		}
		// 榜单为空
		if (rankList.size() == 0) {
			rank.setRank(1);
			if (!rank.isOldRecord()) {
				rank.persist();
			}
			rankList.add(rank);
			rankMap.put(rank.getHumanId(), rank);
			return;
		}

		// 查找自己上榜的旧数据
		RankTower oldRank = rankMap.get(rank.getHumanId());

		if (oldRank != null) {
			if (oldRank.getGrade() > rank.getGrade()) {
				return;
			}

			if (rank.isOldRecord()) {
				return;
			}

			// 删掉旧的
			rankList.remove(oldRank);
			rankMap.remove(oldRank.getHumanId());
			oldRank.remove();
		}
		// 二分查找插入位置
		int index = binarySearch_Tower(rank.getGrade(), rankList, 0, rankList.size() - 1);

		// 插入新对象
		rank.setRank(index + 1);
		if (!rank.isOldRecord()) {
			rank.persist();
		}
		rankList.add(index, rank);
		rankMap.put(rank.getHumanId(), rank);

		// 删除多出排行的对象
		if (rankList.size() > Fairyland_Rank_maxNum) {
			RankTower r = rankList.get(rankList.size() - 1);
			if (r != null) {
				rankMap.remove(r.getHumanId());
				rankList.remove(r);
				r.remove();
			}
		}

	}

	//
	private int binarySearch_Tower(long grade, LinkedList<RankTower> rankList, int from, int to) {
		// 空表，直接插入
		if (rankList.size() == 0) {
			return 0;
		}
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle).getGrade() > grade) {
				return binarySearch_Tower(grade, rankList, middle + 1, to);
			}
			return binarySearch_Tower(grade, rankList, from, middle - 1);
		} else {
			if (rankList.size() > 0 && rankList.get(from).getGrade() >= grade) {
				return from + 1;
			}
			return from;
		}
	}

	// ==================插入一条工会排行
	/**
	 * 
	 */
	private long getGuildRankScore(RankGuild rank) {
		return ((long)rank.getGuildLevel())<<32 + rank.getGuildExp();
	}
	private void addNewToRank(RankGuild rank, LinkedList<RankGuild> rankList, Map<Long, RankGuild> rankMap) {

		if (rank == null) {
			return;
		}
		// 校验是否超过榜单数量限制
		int guild_Rank_maxNum = ParamManager.rankTopShowNum[RankIntType.Guild.value()];
		if (rankList.size() >= guild_Rank_maxNum) {
			if (getGuildRankScore(rank) <= getGuildRankScore(rankList.get(rankList.size() - 1))) {
				return;
			}
		}
		// 榜单为空
		if (rankList.size() == 0) {
			rank.setRank(1);
			if (!rank.isOldRecord()) {
				rank.persist();
			}
			rankList.add(rank);
			rankMap.put(rank.getHumanId(), rank);
			return;
		}

		// 查找自己上榜的旧数据
		RankGuild oldRank = rankMap.get(rank.getGuildId());

		if (oldRank != null) {
			if (getGuildRankScore(oldRank) > getGuildRankScore(rank)) {
				return;
			}

			if (rank.isOldRecord()) {
				return;
			}
			// 删掉旧的
			rankList.remove(oldRank);
			rankMap.remove(oldRank.getHumanId());
			oldRank.remove();
		}
		// 二分查找插入位置
		int index = binarySearch_Guild(getGuildRankScore(rank), rankList, 0, rankList.size() - 1);

		// 插入新对象
		rank.setRank(index + 1);
		if (!rank.isOldRecord()) {
			rank.persist();
		}
		rankList.add(index, rank);
		rankMap.put(rank.getHumanId(), rank);

		// 删除多出排行的对象
		if (rankList.size() > guild_Rank_maxNum) {
			RankGuild r = rankList.get(rankList.size() - 1);
			if (r != null) {
				rankMap.remove(r.getHumanId());
				rankList.remove(r);
				r.remove();
			}
		}

	}

	private int binarySearch_Guild(long exp, LinkedList<RankGuild> rankList, int from, int to) {
		// 空表，直接插入
		if (rankList.size() == 0) {
			return 0;
		}
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (getGuildRankScore(rankList.get(middle)) > exp) {
				return binarySearch_Guild(exp, rankList, middle + 1, to);
			}
			return binarySearch_Guild(exp, rankList, from, middle - 1);
		} else {
			if (rankList.size() > 0 && getGuildRankScore(rankList.get(from)) >= exp) {
				return from + 1;
			}
			return from;
		}
	}
	// ==================插入一条工会排行 end

	/**
	 * 插入一条洞天福地排行
	 */
	private void addNewToRank(RankFairyland rank, LinkedList<RankFairyland> rankList,
			Map<Long, RankFairyland> rankMap) {

		if (rank == null) {
			return;
		}
		// 校验是否超过榜单数量限制
		int Fairyland_Rank_maxNum = ParamManager.rankTopShowNum[RankIntType.Fairyland.value()];
		if (rankList.size() >= Fairyland_Rank_maxNum) {
			if (rank.getLootMapScore() <= rankList.get(rankList.size() - 1).getLootMapScore()) {
				return;
			}
		}
		// 榜单为空
		if (rankList.size() == 0) {
			rank.setRank(1);
			if (!rank.isOldRecord()) {
				rank.persist();
			}
			rankList.add(rank);
			rankMap.put(rank.getHumanId(), rank);
			return;
		}

		// 查找自己上榜的旧数据
		RankFairyland oldRank = rankMap.get(rank.getHumanId());

		if (oldRank != null) {
			if (oldRank.getLootMapScore() > rank.getLootMapScore()) {
				return;
			}

			if (rank.isOldRecord()) {
				return;
			}
			// 删掉旧的
			rankList.remove(oldRank);
			rankMap.remove(oldRank.getHumanId());
			oldRank.remove();
		}
		// 二分查找插入位置
		int index = binarySearch_Rairyland(rank.getLootMapScore(), rankList, 0, rankList.size() - 1);

		// 插入新对象
		rank.setRank(index + 1);
		if (!rank.isOldRecord()) {
			rank.persist();
		}
		rankList.add(index, rank);
		rankMap.put(rank.getHumanId(), rank);

		// 删除多出排行的对象
		if (rankList.size() > Fairyland_Rank_maxNum) {
			RankFairyland r = rankList.get(rankList.size() - 1);
			if (r != null) {
				rankMap.remove(r.getHumanId());
				rankList.remove(r);
				r.remove();
			}
		}

	}

	private int binarySearch_Rairyland(long score, LinkedList<RankFairyland> rankList, int from, int to) {
		// 空表，直接插入
		if (rankList.size() == 0) {
			return 0;
		}
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle).getLootMapScore() > score) {
				return binarySearch_Rairyland(score, rankList, middle + 1, to);
			}
			return binarySearch_Rairyland(score, rankList, from, middle - 1);
		} else {
			if (rankList.size() > 0 && rankList.get(from).getLootMapScore() >= score) {
				return from + 1;
			}
			return from;
		}
	}

	/**
	 * 插入一副本排行
	 * 
	 */
	private void addNewToRank(RankInstance rank, LinkedList<RankInstance> rankList, Map<Long, RankInstance> rankMap) {
		if (rank == null) {
			return;
		}
		// 校验是否超过榜单数量限制
		int combatRank_maxNum = ParamManager.rankTopShowNum[RankIntType.Instance.value()];
		if (rankList.size() >= combatRank_maxNum) {
			if (rank.getLevel() <= rankList.get(rankList.size() - 1).getLevel()) {// 如果等级小于最后一名的等级就return
				return;
			}
		}
		// 榜单为空
		if (rankList.size() == 0) {
			rank.setRank(1);
			if (!rank.isOldRecord()) {
				rank.persist();
			}
			rankList.add(rank);
			rankMap.put(rank.getHumanId(), rank);
			return;
		}

		// 查找自己上榜的旧数据
		RankInstance oldRank = rankMap.get(rank.getHumanId());

		if (oldRank != null) {
			if (oldRank.getStars() > rank.getStars()) {
				return;
			}
			if (rank.isOldRecord()) {
				return;
			}

			// 删掉旧的
			rankList.remove(oldRank);
			rankMap.remove(oldRank.getHumanId());
			oldRank.remove();
		}
		// 二分查找插入位置
		int index = binarySearch_Instance(rank.getStars(), rankList, 0, rankList.size() - 1);
		if (index == 1) {
			// 如果要插入第二名，要看一下战斗力，如果战斗力高于之前的第一名，且星星数一样，则不更改
			RankInstance first = rankList.get(index - 1);
			if (rank.getCombat() > first.getCombat()) {
				index = 0;
			}

		}
		// 插入新对象
		rank.setRank(index + 1);
		if (!rank.isOldRecord()) {
			rank.persist();
		}
		rankList.add(index, rank);
		rankMap.put(rank.getHumanId(), rank);

		// 删除多出排行的对象
		if (rankList.size() > combatRank_maxNum) {
			RankInstance r = rankList.get(rankList.size() - 1);
			if (r != null) {
				rankMap.remove(r.getHumanId());
				rankList.remove(r);
				r.remove();
			}
		}
	}

	private int binarySearch_Instance(int start, LinkedList<RankInstance> rankList, int from, int to) {
		// 空表，直接插入
		if (rankList.size() == 0) {
			return 0;
		}
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle).getStars() > start) {
				return binarySearch_Instance(start, rankList, middle + 1, to);
			}
			// if(rankList.get(middle).getStars() == start){
			// System.out.println("上半时比赛本身不是随便随便");
			// }
			return binarySearch_Instance(start, rankList, from, middle - 1);
		} else {
			if (rankList.size() > 0 && rankList.get(from).getStars() >= start) {
				return from + 1;
			}
			return from;
		}
	}

	/**
	 * 插入一条等级排行
	 */
	public void addNewToRank(RankLevel rank, LinkedList<RankLevel> rankList, Map<Long, RankLevel> rankMap) {

		if (rank == null) {
			return;
		}
		// 校验是否超过榜单数量限制
		int combatRank_maxNum = ParamManager.rankTopShowNum[RankIntType.Level.value()];
		if (rankList.size() >= combatRank_maxNum) {
			if (rank.getLevel() <= rankList.get(rankList.size() - 1).getLevel()) {
				return;
			}
		}
		// 榜单为空
		if (rankList.size() == 0) {
			rank.setRank(1);
			if (!rank.isOldRecord()) {
				rank.persist();
			}
			rankList.add(rank);
			rankMap.put(rank.getHumanId(), rank);
			return;
		}

		// 查找自己上榜的旧数据
		RankLevel oldRank = rankMap.get(rank.getHumanId());

		if (oldRank != null) {
			if (oldRank.getLevel() > rank.getLevel()) {
				return;
			}
			if (rank.isOldRecord()) {
				return;
			}
			// 删掉旧的
			rankList.remove(oldRank);
			rankMap.remove(oldRank.getHumanId());
			oldRank.remove();
		}
		// 二分查找插入位置
		int index = binarySearch_Level(rank.getLevel(), rankList, 0, rankList.size() - 1);

		// 插入新对象
		rank.setRank(index + 1);
		if (!rank.isOldRecord()) {
			rank.persist();
		}
		rankList.add(index, rank);
		rankMap.put(rank.getHumanId(), rank);

		// 删除多出排行的对象
		if (rankList.size() > combatRank_maxNum) {
			RankLevel r = rankList.get(rankList.size() - 1);
			if (r != null) {
				rankMap.remove(r.getHumanId());
				rankList.remove(r);
				r.remove();
			}
		}
	}

	/**
	 * 二分查找插入等级排行榜
	 * 
	 * @param level
	 * @param rankList
	 * @param from
	 * @param to
	 * @return
	 */
	private int binarySearch_Level(int level, LinkedList<RankLevel> rankList, int from, int to) {
		// 空表，直接插入
		if (rankList.size() == 0) {
			return 0;
		}
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle).getLevel() > level) {
				return binarySearch_Level(level, rankList, middle + 1, to);
			}
			return binarySearch_Level(level, rankList, from, middle - 1);
		} else {
			if (rankList.size() > 0 && rankList.get(from).getLevel() >= level) {
				return from + 1;
			}
			return from;
		}
	}

	/**
	 * 插入一条战斗力排行
	 * 
	 * @param rank
	 * @param rankList
	 */
	public void addNewToRank(RankSumCombat rank, LinkedList<RankSumCombat> rankList, Map<Long, RankSumCombat> rankMap) {

		if (rank == null) {
			return;
		}
		// 校验是否超过榜单数量限制
		int combatRank_maxNum = ParamManager.rankTopShowNum[RankIntType.Compete.value()];
		if (rankList.size() >= combatRank_maxNum) {
			if (rank.getSumCombat() <= rankList.get(rankList.size() - 1).getSumCombat()) {
				return;
			}
		}
		// 榜单为空
		if (rankList.size() == 0) {
			rank.setRank(1);
			if (!rank.isOldRecord()) {
				rank.persist();
			}
			rankList.add(rank);
			rankMap.put(rank.getHumanId(), rank);
			return;
		}

		// 查找自己上榜的旧数据
		RankSumCombat oldRank = rankMap.get(rank.getHumanId());

		if (oldRank != null) {
			if (oldRank.getSumCombat() > rank.getSumCombat()) {
				if (!oldRank.isOldRecord()) {
					oldRank.persist();
				}
				return;
			}
			// 删掉旧的
			rankList.remove(oldRank);
			rankMap.remove(oldRank.getHumanId());
			oldRank.remove();
		}
		// 二分查找插入位置
		int index = binarySearch_CombatRank(rank.getSumCombat(), rankList, 0, rankList.size() - 1);

		// 插入新对象
		rank.setRank(index + 1);
		if (!rank.isOldRecord()) {
			rank.persist();
		}
		rankList.add(index, rank);
		rankMap.put(rank.getHumanId(), rank);

		// 删除多出排行的对象
		if (rankList.size() > combatRank_maxNum) {
			RankSumCombat r = rankList.get(rankList.size() - 1);
			if (r != null) {
				rankMap.remove(r.getHumanId());
				rankList.remove(r);
				r.remove();
			}
		}
	}

	/**
	 * 二分查找插入(战斗力排行榜)
	 * 
	 * @param from
	 * @param to
	 */
	public int binarySearch_CombatRank(int combat, LinkedList<RankSumCombat> rankList, int from, int to) {
		// 空表，直接插入
		if (rankList.size() == 0) {
			return 0;
		}
		if (to - from > 0) {
			int middle = (from + to) / 2;
			// 降序
			if (rankList.get(middle).getSumCombat() > combat) {
				return binarySearch_CombatRank(combat, rankList, middle + 1, to);
			}
			return binarySearch_CombatRank(combat, rankList, from, middle - 1);
		} else {
			if (rankList.size() > 0 && rankList.get(from).getSumCombat() >= combat) {
				return from + 1;
			}
			return from;
		}
	}

	/**
	 * 获取各个排行榜第一的城主信息
	 */
	@DistrMethod
	public void getCastellan() {
		Map<Integer, Castellan> castellanMap = new HashMap<>();
		// 战斗力
		// if(combatList.size() >0){
		// RankSumCombat combat = this.combatList.get(0);
		// ConfMainCityShow conf =
		// ConfMainCityShow.get(ECastellanType.InstanceDuke_VALUE);
		// if(conf !=null && combat.getLv() >= conf.needLev){
		// Castellan combatDuke = RankManager.inst().rankToCastenllan(combat);
		// castellanMap.put(ECastellanType.SumCombatDuke_VALUE, combatDuke);
		// }
		//
		// }
		//

		// //等级排行榜
		// if(levelList.size()>0){
		// RankLevel level = this.levelList.get(0);
		// Castellan levelDuke = RankManager.inst().rankToCastenllan(level);
		// castellanMap.put(ECastellanType.LevelDuke_VALUE, levelDuke);
		//
		// }

		// 副本排行榜
		boolean isUpdate = RankManager.inst().isUpdateTime(ECastellanType.InstanceDuke);// 是更新时间
		if (isUpdate) {
			castellanMap.put(ECastellanType.InstanceDuke_VALUE, null);
			if (instanceList.size() > 0) {
				RankInstance instance = this.instanceList.get(0);
				ConfMainCityShow conf = ConfMainCityShow.get(ECastellanType.InstanceDuke_VALUE);
				if (conf != null && instance.getLevel() >= conf.needLev && isUpdate) {
					Castellan instanceDuck = RankManager.inst().rankToCastenllan(instance);
					castellanMap.put(ECastellanType.InstanceDuke_VALUE, instanceDuck);
				}
			}
		}

		// 洞天福地排行榜
		isUpdate = RankManager.inst().isUpdateTime(ECastellanType.FairylandDuke);// 是更新时间
		if (isUpdate) {
			castellanMap.put(ECastellanType.FairylandDuke_VALUE, null);
			if (fairylandList.size() > 0) {
				RankFairyland fairy = this.fairylandList.get(0);
				ConfMainCityShow conf = ConfMainCityShow.get(ECastellanType.FairylandDuke_VALUE);
				if (conf != null && fairy.getLevel() >= conf.needLev && isUpdate) {
					Castellan fairyDuck = RankManager.inst().rankToCastenllan(fairy);
					castellanMap.put(ECastellanType.FairylandDuke_VALUE, fairyDuck);
				}
			}
		}

		// 爬塔排行榜
		isUpdate = RankManager.inst().isUpdateTime(ECastellanType.TowerDuke);// 是更新时间
		if (isUpdate) {
			castellanMap.put(ECastellanType.TowerDuke_VALUE, null);
			if (towerList.size() > 0) {
				RankTower tower = this.towerList.get(0);
				ConfMainCityShow conf = ConfMainCityShow.get(ECastellanType.TowerDuke_VALUE);
				if (conf != null && tower.getLevel() > conf.needLev && isUpdate) {
					Castellan towerDuck = RankManager.inst().rankToCastenllan(tower);
					castellanMap.put(ECastellanType.TowerDuke_VALUE, towerDuck);
				}
			}
		}

		long pid = port.createReturnAsync();// 创建一个异步返回

		// 竞技场排行榜
		CompeteServiceProxy prx = CompeteServiceProxy.newInstance();
		prx.getCompeteCastellan();
		prx.listenResult(this::_result_getCastellan, "castellanMap", castellanMap, "pid", pid);
	}

	private void _result_getCastellan(Param result, Param context) {
		long pid = Utils.getParamValue(context, "pid", -1L);
		Map<Integer, Castellan> castellanMap = context.get("castellanMap");
		Castellan competeCastellan = result.get("castellan");

		boolean isUpdate = RankManager.inst().isUpdateTime(ECastellanType.ArenaDuke);// 是更新时间
		if (isUpdate) {
			castellanMap.put(ECastellanType.ArenaDuke_VALUE, competeCastellan);
		}
		port.returnsAsync(pid, "castellanMap", castellanMap);
	}

	/**
	 * 通过传入玩家id获取排名
	 * 
	 * @param humanId
	 */
	@DistrMethod
	public void getRankByLootMap(List<Long> humanId) {
		// <humanId,Rank排名>
		Map<Long, Integer> idAndRank = new HashMap<>();
		for (Long hid : humanId) {
			RankFairyland fairRank = mapAllFairyland.get(hid);
			int rank = 0;
			if (fairRank != null) {
				rank = fairylandList.indexOf(fairRank);
				if (rank < 0) {
					rank = ParamManager.competeRankMax;
				}
			}
			idAndRank.put(hid, rank);
		}
		port.returns("rank", idAndRank);
	}

	/**
	 * 删除工会
	 * 
	 * @param id
	 * @param rankguild
	 */
	public void deleteRank(long id, RankType rankguild) {
		// TODO Auto-generated method stub

	}

	@DistrMethod
	public void clearRank(RankType type) {
		switch (type) {
		case RankLevel:
			break;
		case RankSumCombat:
			break;
		case RankInstance:
			break;
		case RankFairyland:
			break;
		case RankTower:
			mapAllTower.clear();
			towerList.clear();
			String sql = Utils.createStr("truncate {}", RankTower.tableName); 
	        DB dbPxy = DB.newInstance(RankTower.tableName);
	        dbPxy.sql(false, false, sql);
			break;
		default:
			break;
		}
	}
	
	@DistrMethod
	public void actServerCompetitionPlanAbort(){
		if(actCombatList.size() > 0) {
			return;
		}
		actCombatList.clear();
		actCombatList =  (LinkedList<RankSumCombat>) combatList.clone();
		harmAwardIndex = 0;
		activityPulseTimer.start(Port.getTime(), 10 * Time.SEC, true);
	}
	@Override
	public void pulseOverride() {
		if(activityPulseTimer.isPeriod(Port.getTime())){
			disposeActServerCompetition();
		}
	}
	/**
	 * 处理活动-新服竞赛
	 */
	private void disposeActServerCompetition(){
		// 下标 size
		if(harmAwardIndex >= actCombatList.size() - 1){
			activityPulseTimer.stop();
			return;
		}
		for(int index = harmAwardIndex; index < CountPer10Sec; index ++){
			if(actCombatList.size() <= index){
				break;
			}
			RankSumCombat combat = actCombatList.get(index);
			if(combat == null){
				continue;
			}
			//持久化
			persistActServerCompetition(combat);
			try {
				List<ConfActivityServerCompetition> confList = 
						ConfActivityServerCompetition.findBy(ConfActivityServerCompetition.K.type,1);
				int rank = index + 1;
				for(ConfActivityServerCompetition conf : confList){
					if(conf.min <= rank && rank <= conf.max){
						ConfRewards confRewards = ConfRewards.get(conf.rewardSn);
						if (null == confRewards) {
							Log.table.error("===配表错误ConfRewards no find sn={}", conf.rewardSn);
							return;
						} 
						List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
						itemProduce.addAll(ProduceManager.inst().produceItem(confRewards.itemSn, confRewards.itemNum));
	
						String detail = "{" + EMailType.MailActCompetion_VALUE + "|" + (index + 1) + "}";
						MailManager.inst().sendSysMail(combat.getHumanId(), ParamManager.mailMark, detail, itemProduce);
						break;
					}
				}
			} catch (Exception e) {
				Log.activity.error("新服竞赛奖励下发失败，玩家ID："+combat.getHumanId());
				e.printStackTrace();
			}
		}
		harmAwardIndex += CountPer10Sec;
		activityPulseTimer.start(Port.getTime(),10 * Time.SEC);
	}
	private void persistActServerCompetition(RankSumCombat combat){
		ActServerCompetition competition = new ActServerCompetition();
		competition.setId(Port.applyId());
		competition.setType(1);
		JSONObject jo = new JSONObject();
		jo.put("HumanId", combat.getHumanId());
		jo.put("Rank", combat.getRank());//排行
		jo.put("Name", combat.getName());//玩家名字
		jo.put("SumCombat", combat.getSumCombat());//总战斗力
		jo.put("HeadSn", combat.getHeadSn());//头像SN
		jo.put("Lv", combat.getLv());
		jo.put("Combat", combat.getCombat());
		jo.put("ModelSn", combat.getModelSn());
		competition.setParamJSON(jo.toJSONString());
		competition.persist();
	}
	
	@DistrMethod
	public void getActCombatList(){
		port.returns(actCombatList);
	}

}
