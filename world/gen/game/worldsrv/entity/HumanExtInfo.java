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
public final class HumanExtInfo extends EntityBase {
	public static final String tableName = "human_ext_info";

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String id = "id";	//id
		public static final String SignIndex = "SignIndex";	//签到次数
		public static final String SignGroup = "SignGroup";	//签到组
		public static final String SignCount = "SignCount";	//签到总数
		public static final String VipBuyTimes = "VipBuyTimes";	//vip各项购买次数  {type：buytimes}
		public static final String VipAwardId = "VipAwardId";	//领奖记录
		public static final String VipBuyLvs = "VipBuyLvs";	//已购买过vip等级礼包 id,id,id
		public static final String FirstChargeRewardState = "FirstChargeRewardState";	//是否领取首充2 已领取 1 可领取；0否
		public static final String MallJson = "MallJson";	//商城商品购买信息
		public static final String Popularity = "Popularity";	//人气
		public static final String Likelist = "Likelist";	//点赞玩家id
		public static final String PartnerLineup = "PartnerLineup";	//伙伴阵容(id列表) 
		public static final String PartnerStance = "PartnerStance";	//伙伴站位(0-W型；1-M型)
		public static final String ServantLock = "ServantLock";	//当前护法解锁个数 
		public static final String ActivityReword = "ActivityReword";	//已经<领取>的图鉴id,领取物品的时候用的
		public static final String AlreadyGetReword = "AlreadyGetReword";	//已经<激活>,可领取未领取的图鉴id,领取物品的时候用的
		public static final String ActivityHand = "ActivityHand";	//激活过的伙伴
		public static final String FirstInCompete = "FirstInCompete";	//是否第一次初始化竞技场
		public static final String lastCompeteTime = "lastCompeteTime";	//上次挑战时间
		public static final String competeBuyNumAdd = "competeBuyNumAdd";	//每天已购买挑战次数
		public static final String lastcompeteBuyTime = "lastcompeteBuyTime";	//上次购买时间
		public static final String challengeNum = "challengeNum";	//今日已挑战次数
		public static final String surplusNum = "surplusNum";	//剩余挑战次数
		public static final String todayWinNums = "todayWinNums";	//今日胜利次数
		public static final String LevelPackage = "LevelPackage";	//已领取的等级礼包
		public static final String WDBossJoinTime = "WDBossJoinTime";	//上次参与活动时间戳
		public static final String WDBossNextFightTime = "WDBossNextFightTime";	//下次挑战时间戳
		public static final String WDBossNextInspireTime = "WDBossNextInspireTime";	//下次鼓舞时间戳
		public static final String WDBossInspireNum = "WDBossInspireNum";	//本次活动已鼓舞次数
		public static final String WDBossRebornNum = "WDBossRebornNum";	//本次活动已涅槃重生次数
		public static final String TowerIsFight = "TowerIsFight";	//爬塔今日是否已经挑战过
		public static final String TowerPassTime = "TowerPassTime";	//爬塔耗时（秒）
		public static final String TowerMaxFloor = "TowerMaxFloor";	//爬塔最大层数
		public static final String TowerSelDiff = "TowerSelDiff";	//爬塔最后过关难度
		public static final String TowerScore = "TowerScore";	//爬塔积分
		public static final String RuneSummonSn = "RuneSummonSn";	//符文召唤当前的sn（RuneSummonSn）
		public static final String RuneSummonCount = "RuneSummonCount";	//符文占卜计数列表
		public static final String FriendGiveTimes = "FriendGiveTimes";	//赠送好友体力次数
		public static final String FriendReceiveTimes = "FriendReceiveTimes";	//接受好友赠送体力次数
		public static final String ActivityGetTime = "ActivityGetTime";	//整点体力领取时间
		public static final String LootMapMultipleJoinTime = "LootMapMultipleJoinTime";	//多人抢夺本最近参与时间
		public static final String LootMapMultipleJoinCount = "LootMapMultipleJoinCount";	//多人抢夺本最近参与时间内累计参加次数
		public static final String LootMapMultipleTodayScore = "LootMapMultipleTodayScore";	//多人抢夺本今日最高
		public static final String LootMapMultipleScore = "LootMapMultipleScore";	//多人抢夺本累计积分
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public HumanExtInfo() {
		super();
		setSignIndex(0);
		setSignGroup(1);
		setSignCount(0);
		setVipBuyTimes("[]");
		setVipBuyLvs("[]");
		setFirstChargeRewardState(0);
		setMallJson("[]");
		setPopularity(0);
		setLikelist("[]");
		setServantLock(0);
		setFirstInCompete(true);
		setCompeteBuyNumAdd(0);
		setLastcompeteBuyTime(0);
		setChallengeNum(0);
		setSurplusNum(0);
		setTodayWinNums(0);
		setWDBossJoinTime(0);
		setWDBossNextFightTime(0);
		setWDBossNextInspireTime(0);
		setWDBossInspireNum(0);
		setWDBossRebornNum(0);
		setTowerIsFight(false);
		setTowerPassTime(0);
		setTowerMaxFloor(0);
		setTowerSelDiff(0);
		setTowerScore(0);
		setRuneSummonSn(0);
		setRuneSummonCount("0,0,0,0,0");
		setFriendGiveTimes(0);
		setFriendReceiveTimes(0);
	}

	public HumanExtInfo(Record record) {
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
	 * 签到次数
	 */
	public int getSignIndex() {
		return record.get("SignIndex");
	}

	public void setSignIndex(final int SignIndex) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SignIndex", SignIndex);

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
	 * 签到组
	 */
	public int getSignGroup() {
		return record.get("SignGroup");
	}

	public void setSignGroup(final int SignGroup) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SignGroup", SignGroup);

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
	 * 签到总数
	 */
	public int getSignCount() {
		return record.get("SignCount");
	}

	public void setSignCount(final int SignCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("SignCount", SignCount);

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
	 * vip各项购买次数  {type：buytimes}
	 */
	public String getVipBuyTimes() {
		return record.get("VipBuyTimes");
	}

	public void setVipBuyTimes(final String VipBuyTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("VipBuyTimes", VipBuyTimes);

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
	 * 领奖记录
	 */
	public int getVipAwardId() {
		return record.get("VipAwardId");
	}

	public void setVipAwardId(final int VipAwardId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("VipAwardId", VipAwardId);

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
	 * 已购买过vip等级礼包 id,id,id
	 */
	public String getVipBuyLvs() {
		return record.get("VipBuyLvs");
	}

	public void setVipBuyLvs(final String VipBuyLvs) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("VipBuyLvs", VipBuyLvs);

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
	 * 是否领取首充2 已领取 1 可领取；0否
	 */
	public int getFirstChargeRewardState() {
		return record.get("FirstChargeRewardState");
	}

	public void setFirstChargeRewardState(final int FirstChargeRewardState) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FirstChargeRewardState", FirstChargeRewardState);

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
	 * 商城商品购买信息
	 */
	public String getMallJson() {
		return record.get("MallJson");
	}

	public void setMallJson(final String MallJson) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("MallJson", MallJson);

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
	 * 人气
	 */
	public int getPopularity() {
		return record.get("Popularity");
	}

	public void setPopularity(final int Popularity) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Popularity", Popularity);

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
	 * 点赞玩家id
	 */
	public String getLikelist() {
		return record.get("Likelist");
	}

	public void setLikelist(final String Likelist) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Likelist", Likelist);

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
	 * 伙伴阵容(id列表) 
	 */
	public String getPartnerLineup() {
		return record.get("PartnerLineup");
	}

	public void setPartnerLineup(final String PartnerLineup) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("PartnerLineup", PartnerLineup);

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
	 * 伙伴站位(0-W型；1-M型)
	 */
	public int getPartnerStance() {
		return record.get("PartnerStance");
	}

	public void setPartnerStance(final int PartnerStance) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("PartnerStance", PartnerStance);

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
	 * 当前护法解锁个数 
	 */
	public int getServantLock() {
		return record.get("ServantLock");
	}

	public void setServantLock(final int ServantLock) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ServantLock", ServantLock);

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
	 * 已经<领取>的图鉴id,领取物品的时候用的
	 */
	public String getActivityReword() {
		return record.get("ActivityReword");
	}

	public void setActivityReword(final String ActivityReword) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ActivityReword", ActivityReword);

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
	 * 已经<激活>,可领取未领取的图鉴id,领取物品的时候用的
	 */
	public String getAlreadyGetReword() {
		return record.get("AlreadyGetReword");
	}

	public void setAlreadyGetReword(final String AlreadyGetReword) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("AlreadyGetReword", AlreadyGetReword);

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
	 * 激活过的伙伴
	 */
	public String getActivityHand() {
		return record.get("ActivityHand");
	}

	public void setActivityHand(final String ActivityHand) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ActivityHand", ActivityHand);

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
	 * 是否第一次初始化竞技场
	 */
	public boolean isFirstInCompete() {
		return record.<Integer>get("FirstInCompete") == 1;
	}

	public void setFirstInCompete(boolean FirstInCompete) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FirstInCompete", FirstInCompete ? 1 : 0);

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
	 * 上次挑战时间
	 */
	public long getLastCompeteTime() {
		return record.get("lastCompeteTime");
	}

	public void setLastCompeteTime(final long lastCompeteTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("lastCompeteTime", lastCompeteTime);

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
	 * 每天已购买挑战次数
	 */
	public int getCompeteBuyNumAdd() {
		return record.get("competeBuyNumAdd");
	}

	public void setCompeteBuyNumAdd(final int competeBuyNumAdd) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("competeBuyNumAdd", competeBuyNumAdd);

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
	 * 上次购买时间
	 */
	public long getLastcompeteBuyTime() {
		return record.get("lastcompeteBuyTime");
	}

	public void setLastcompeteBuyTime(final long lastcompeteBuyTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("lastcompeteBuyTime", lastcompeteBuyTime);

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
	 * 今日已挑战次数
	 */
	public int getChallengeNum() {
		return record.get("challengeNum");
	}

	public void setChallengeNum(final int challengeNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("challengeNum", challengeNum);

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
	 * 剩余挑战次数
	 */
	public int getSurplusNum() {
		return record.get("surplusNum");
	}

	public void setSurplusNum(final int surplusNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("surplusNum", surplusNum);

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
	 * 今日胜利次数
	 */
	public int getTodayWinNums() {
		return record.get("todayWinNums");
	}

	public void setTodayWinNums(final int todayWinNums) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("todayWinNums", todayWinNums);

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
	 * 已领取的等级礼包
	 */
	public String getLevelPackage() {
		return record.get("LevelPackage");
	}

	public void setLevelPackage(final String LevelPackage) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LevelPackage", LevelPackage);

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
	 * 上次参与活动时间戳
	 */
	public long getWDBossJoinTime() {
		return record.get("WDBossJoinTime");
	}

	public void setWDBossJoinTime(final long WDBossJoinTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("WDBossJoinTime", WDBossJoinTime);

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
	 * 下次挑战时间戳
	 */
	public long getWDBossNextFightTime() {
		return record.get("WDBossNextFightTime");
	}

	public void setWDBossNextFightTime(final long WDBossNextFightTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("WDBossNextFightTime", WDBossNextFightTime);

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
	 * 下次鼓舞时间戳
	 */
	public long getWDBossNextInspireTime() {
		return record.get("WDBossNextInspireTime");
	}

	public void setWDBossNextInspireTime(final long WDBossNextInspireTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("WDBossNextInspireTime", WDBossNextInspireTime);

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
	 * 本次活动已鼓舞次数
	 */
	public int getWDBossInspireNum() {
		return record.get("WDBossInspireNum");
	}

	public void setWDBossInspireNum(final int WDBossInspireNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("WDBossInspireNum", WDBossInspireNum);

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
	 * 本次活动已涅槃重生次数
	 */
	public int getWDBossRebornNum() {
		return record.get("WDBossRebornNum");
	}

	public void setWDBossRebornNum(final int WDBossRebornNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("WDBossRebornNum", WDBossRebornNum);

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
	 * 爬塔今日是否已经挑战过
	 */
	public boolean isTowerIsFight() {
		return record.<Integer>get("TowerIsFight") == 1;
	}

	public void setTowerIsFight(boolean TowerIsFight) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TowerIsFight", TowerIsFight ? 1 : 0);

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
	 * 爬塔耗时（秒）
	 */
	public int getTowerPassTime() {
		return record.get("TowerPassTime");
	}

	public void setTowerPassTime(final int TowerPassTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TowerPassTime", TowerPassTime);

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
	 * 爬塔最大层数
	 */
	public int getTowerMaxFloor() {
		return record.get("TowerMaxFloor");
	}

	public void setTowerMaxFloor(final int TowerMaxFloor) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TowerMaxFloor", TowerMaxFloor);

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
	 * 爬塔最后过关难度
	 */
	public int getTowerSelDiff() {
		return record.get("TowerSelDiff");
	}

	public void setTowerSelDiff(final int TowerSelDiff) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TowerSelDiff", TowerSelDiff);

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
	 * 爬塔积分
	 */
	public int getTowerScore() {
		return record.get("TowerScore");
	}

	public void setTowerScore(final int TowerScore) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TowerScore", TowerScore);

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
	 * 符文召唤当前的sn（RuneSummonSn）
	 */
	public int getRuneSummonSn() {
		return record.get("RuneSummonSn");
	}

	public void setRuneSummonSn(final int RuneSummonSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RuneSummonSn", RuneSummonSn);

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
	 * 符文占卜计数列表
	 */
	public String getRuneSummonCount() {
		return record.get("RuneSummonCount");
	}

	public void setRuneSummonCount(final String RuneSummonCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RuneSummonCount", RuneSummonCount);

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
	 * 赠送好友体力次数
	 */
	public int getFriendGiveTimes() {
		return record.get("FriendGiveTimes");
	}

	public void setFriendGiveTimes(final int FriendGiveTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FriendGiveTimes", FriendGiveTimes);

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
	 * 接受好友赠送体力次数
	 */
	public int getFriendReceiveTimes() {
		return record.get("FriendReceiveTimes");
	}

	public void setFriendReceiveTimes(final int FriendReceiveTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FriendReceiveTimes", FriendReceiveTimes);

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
	 * 整点体力领取时间
	 */
	public long getActivityGetTime() {
		return record.get("ActivityGetTime");
	}

	public void setActivityGetTime(final long ActivityGetTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ActivityGetTime", ActivityGetTime);

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
	 * 多人抢夺本最近参与时间
	 */
	public long getLootMapMultipleJoinTime() {
		return record.get("LootMapMultipleJoinTime");
	}

	public void setLootMapMultipleJoinTime(final long LootMapMultipleJoinTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LootMapMultipleJoinTime", LootMapMultipleJoinTime);

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
	 * 多人抢夺本最近参与时间内累计参加次数
	 */
	public int getLootMapMultipleJoinCount() {
		return record.get("LootMapMultipleJoinCount");
	}

	public void setLootMapMultipleJoinCount(final int LootMapMultipleJoinCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LootMapMultipleJoinCount", LootMapMultipleJoinCount);

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
	 * 多人抢夺本今日最高
	 */
	public int getLootMapMultipleTodayScore() {
		return record.get("LootMapMultipleTodayScore");
	}

	public void setLootMapMultipleTodayScore(final int LootMapMultipleTodayScore) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LootMapMultipleTodayScore", LootMapMultipleTodayScore);

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
	 * 多人抢夺本累计积分
	 */
	public long getLootMapMultipleScore() {
		return record.get("LootMapMultipleScore");
	}

	public void setLootMapMultipleScore(final long LootMapMultipleScore) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("LootMapMultipleScore", LootMapMultipleScore);

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