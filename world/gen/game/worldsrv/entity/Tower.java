package game.worldsrv.entity;

import org.apache.commons.lang3.exception.ExceptionUtils;

import core.db.DBConsts;
import core.dbsrv.DB;
import core.Chunk;
import core.Port;
import core.Record;
import core.support.BufferPool;
import core.support.SysException;
import core.support.log.LogCore;
import core.entity.EntityBase;
import core.gen.GofGenFile;

@GofGenFile
public final class Tower extends EntityBase {
	public static final String tableName = "tower";

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String id = "id";	//id
		public static final String Score = "Score";	//赛季积分
		public static final String Rank = "Rank";	//赛季积分排名
		public static final String SeasonEndTime = "SeasonEndTime";	//赛季结束时间
		public static final String HistoryMaxLayer = "HistoryMaxLayer";	//历史挑战过的最高层数（difficulty * 1000 + layer）
		public static final String YestodayMaxLayer = "YestodayMaxLayer";	//昨日挑战过的最高层数（difficulty * 1000 + layer）
		public static final String MatchLv = "MatchLv";	//更新匹配当日爬塔数据时的等级
		public static final String MatchCombat = "MatchCombat";	//更新匹配当日爬塔数据时的战斗力
		public static final String FirstDailyTime = "FirstDailyTime";	//每日首次打开爬塔的时间戳
		public static final String FirstFightTime = "FirstFightTime";	//每日挑战首层的时间戳
		public static final String LastPassTime = "LastPassTime";	//最后一次过关的时间戳(通过首次和最后一次时间戳，作为排行榜的时间参数)
		public static final String StayLayer = "StayLayer";	//当前停留层数
		public static final String WillFightLayer = "WillFightLayer";	//将要挑战的层数
		public static final String LastSelDifficulty = "LastSelDifficulty";	//最后挑战层数选择的难度
		public static final String HaveLifeNum = "HaveLifeNum";	//当前拥有的生命数
		public static final String BuyLifeNum = "BuyLifeNum";	//已经购买的生命数
		public static final String Multiple = "Multiple";	//爬塔挑战胜利奖励倍数
		public static final String RewardBox = "RewardBox";	//(l=layer,sn=dropId,c=cardStates,sn1=dropId1,c1=cardStates1)奖励宝箱{l:1,d:10,c:0,1,0,c1:0,1,0},..
		public static final String AlreadyFight = "AlreadyFight";	//已经打过的难度层数列表 1001,1002,1003,...,2001,2002..3001
		public static final String DiffcultyLv1 = "DiffcultyLv1";	//(l=layer,id:=humanId,c=combat,cd=condition)难度1：[{l:1,id:1000,c:100,cd:1,1},...}]
		public static final String DiffcultyLv2 = "DiffcultyLv2";	//(l=layer,id:=humanId,c=combat,cd=condition)难度2：[{l:1,id:1000,c:100,cd:1,1},...}]
		public static final String DiffcultyLv3 = "DiffcultyLv3";	//(l=layer,id:=humanId,c=combat,cd=condition)难度3：[{l:1,id:1000,c:100,cd:1,1},...}]
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public Tower() {
		super();
		setMultiple(1);
		setRewardBox("{}");
		setDiffcultyLv1("{}");
		setDiffcultyLv2("{}");
		setDiffcultyLv3("{}");
	}

	public Tower(Record record) {
		super(record);
	}

	
	/**
	 * 新增数据
	 */
	@Override
	public void persist() {
		//状态错误
		if(record.getStatus() != DBConsts.RECORD_STATUS_NEW) {
			LogCore.db.error("只有新增包能调用persist函数，请确认状态：data={}, stackTrace={}", this, ExceptionUtils.getStackTrace(new Throwable()));
			return;
		}
		
		DB prx = DB.newInstance(getTableName());
		prx.insert(record);
		
		//重置状态
		record.resetStatus();
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * 默认不立即持久化到数据库
	 */
	@Override
	public void update() {
		update(false);
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * @param sync 是否立即同持久化到数据库
	 */
	@Override
	public void update(boolean sync) {
		//新增包不能直接调用update函数 请先调用persist
		if(record.getStatus() == DBConsts.RECORD_STATUS_NEW) {
			throw new SysException("新增包不能直接调用update函数，请先调用persist：data={}", this);
		}
		
		//升级包
		Chunk path = record.pathUpdateGen();
		if(path == null || path.length == 0) return;

		//将升级包同步至DB服务器
		DB prx = DB.newInstance(getTableName());
		prx.update(getId(), path, sync);
		
		//回收缓冲包
		BufferPool.deallocate(path.buffer);
		
		//重置状态
		record.resetStatus();
	}

	/**
	 * 删除数据
	 */
	@Override
	public void remove() {
		DB prx = DB.newInstance(getTableName());
		prx.delete(getId());
	}

	/**
	 * id
	 */
	public long getId() {
		return record.get("id");
	}

	public void setId(final long id) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("id", id);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 赛季积分
	 */
	public int getScore() {
		return record.get("Score");
	}

	public void setScore(final int Score) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Score", Score);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 赛季积分排名
	 */
	public int getRank() {
		return record.get("Rank");
	}

	public void setRank(final int Rank) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Rank", Rank);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 赛季结束时间
	 */
	public long getSeasonEndTime() {
		return record.get("SeasonEndTime");
	}

	public void setSeasonEndTime(final long SeasonEndTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SeasonEndTime", SeasonEndTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 历史挑战过的最高层数（difficulty * 1000 + layer）
	 */
	public int getHistoryMaxLayer() {
		return record.get("HistoryMaxLayer");
	}

	public void setHistoryMaxLayer(final int HistoryMaxLayer) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HistoryMaxLayer", HistoryMaxLayer);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 昨日挑战过的最高层数（difficulty * 1000 + layer）
	 */
	public int getYestodayMaxLayer() {
		return record.get("YestodayMaxLayer");
	}

	public void setYestodayMaxLayer(final int YestodayMaxLayer) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("YestodayMaxLayer", YestodayMaxLayer);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 更新匹配当日爬塔数据时的等级
	 */
	public int getMatchLv() {
		return record.get("MatchLv");
	}

	public void setMatchLv(final int MatchLv) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("MatchLv", MatchLv);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 更新匹配当日爬塔数据时的战斗力
	 */
	public int getMatchCombat() {
		return record.get("MatchCombat");
	}

	public void setMatchCombat(final int MatchCombat) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("MatchCombat", MatchCombat);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日首次打开爬塔的时间戳
	 */
	public long getFirstDailyTime() {
		return record.get("FirstDailyTime");
	}

	public void setFirstDailyTime(final long FirstDailyTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FirstDailyTime", FirstDailyTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 每日挑战首层的时间戳
	 */
	public long getFirstFightTime() {
		return record.get("FirstFightTime");
	}

	public void setFirstFightTime(final long FirstFightTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FirstFightTime", FirstFightTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 最后一次过关的时间戳(通过首次和最后一次时间戳，作为排行榜的时间参数)
	 */
	public long getLastPassTime() {
		return record.get("LastPassTime");
	}

	public void setLastPassTime(final long LastPassTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LastPassTime", LastPassTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 当前停留层数
	 */
	public int getStayLayer() {
		return record.get("StayLayer");
	}

	public void setStayLayer(final int StayLayer) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("StayLayer", StayLayer);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 将要挑战的层数
	 */
	public int getWillFightLayer() {
		return record.get("WillFightLayer");
	}

	public void setWillFightLayer(final int WillFightLayer) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("WillFightLayer", WillFightLayer);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 最后挑战层数选择的难度
	 */
	public int getLastSelDifficulty() {
		return record.get("LastSelDifficulty");
	}

	public void setLastSelDifficulty(final int LastSelDifficulty) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LastSelDifficulty", LastSelDifficulty);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 当前拥有的生命数
	 */
	public int getHaveLifeNum() {
		return record.get("HaveLifeNum");
	}

	public void setHaveLifeNum(final int HaveLifeNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HaveLifeNum", HaveLifeNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 已经购买的生命数
	 */
	public int getBuyLifeNum() {
		return record.get("BuyLifeNum");
	}

	public void setBuyLifeNum(final int BuyLifeNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("BuyLifeNum", BuyLifeNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 爬塔挑战胜利奖励倍数
	 */
	public int getMultiple() {
		return record.get("Multiple");
	}

	public void setMultiple(final int Multiple) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Multiple", Multiple);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * (l=layer,sn=dropId,c=cardStates,sn1=dropId1,c1=cardStates1)奖励宝箱{l:1,d:10,c:0,1,0,c1:0,1,0},..
	 */
	public String getRewardBox() {
		return record.get("RewardBox");
	}

	public void setRewardBox(final String RewardBox) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RewardBox", RewardBox);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 已经打过的难度层数列表 1001,1002,1003,...,2001,2002..3001
	 */
	public String getAlreadyFight() {
		return record.get("AlreadyFight");
	}

	public void setAlreadyFight(final String AlreadyFight) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("AlreadyFight", AlreadyFight);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * (l=layer,id:=humanId,c=combat,cd=condition)难度1：[{l:1,id:1000,c:100,cd:1,1},...}]
	 */
	public String getDiffcultyLv1() {
		return record.get("DiffcultyLv1");
	}

	public void setDiffcultyLv1(final String DiffcultyLv1) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DiffcultyLv1", DiffcultyLv1);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * (l=layer,id:=humanId,c=combat,cd=condition)难度2：[{l:1,id:1000,c:100,cd:1,1},...}]
	 */
	public String getDiffcultyLv2() {
		return record.get("DiffcultyLv2");
	}

	public void setDiffcultyLv2(final String DiffcultyLv2) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DiffcultyLv2", DiffcultyLv2);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * (l=layer,id:=humanId,c=combat,cd=condition)难度3：[{l:1,id:1000,c:100,cd:1,1},...}]
	 */
	public String getDiffcultyLv3() {
		return record.get("DiffcultyLv3");
	}

	public void setDiffcultyLv3(final String DiffcultyLv3) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DiffcultyLv3", DiffcultyLv3);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}

}