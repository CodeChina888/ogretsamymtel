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
public final class Card extends EntityBase {
	public static final String tableName = "card";

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String id = "id";	//id
		public static final String HumanId = "HumanId";	//所属的人物ID
		public static final String FreeForCardFirst_BySummonToken = "FreeForCardFirst_BySummonToken";	//是否首次免费招募令抽卡
		public static final String FreeForCardFirst_ByGold = "FreeForCardFirst_ByGold";	//是否首次免费元宝抽卡
		public static final String UserSummonTokenFirst = "UserSummonTokenFirst";	//是否首次花费招募令抽卡
		public static final String UserGoldFirst = "UserGoldFirst";	//是否首次花费元宝抽卡
		public static final String DrawCardFreeTimeBySummonToken = "DrawCardFreeTimeBySummonToken";	//下次免费招募令抽卡时间
		public static final String DrawCardFreeTimeByGold = "DrawCardFreeTimeByGold";	//下次免费元宝抽卡时间
		public static final String TodayDrawByGold = "TodayDrawByGold";	//今日元宝抽卡次数
		public static final String TodayDrawBySummonToken = "TodayDrawBySummonToken";	//今日招募令抽卡次数
		public static final String totleNumByGold = "totleNumByGold";	//元宝总抽卡数量
		public static final String totleNumBySummonToken = "totleNumBySummonToken";	//招募令总抽卡数量
		public static final String dailyFreeSummonToken = "dailyFreeSummonToken";	//今日免费招募令抽卡次数
		public static final String ExchangeRound = "ExchangeRound";	//兑换第几轮次
		public static final String ExchangeState = "ExchangeState";	//本轮次每一阶领取状态,0未兑换，1已经兑换
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public Card() {
		super();
		setFreeForCardFirst_BySummonToken(true);
		setFreeForCardFirst_ByGold(true);
		setUserSummonTokenFirst(true);
		setUserGoldFirst(true);
		setDrawCardFreeTimeBySummonToken(0);
		setDrawCardFreeTimeByGold(0);
		setTodayDrawByGold(0);
		setTodayDrawBySummonToken(0);
		setTotleNumByGold(0);
		setTotleNumBySummonToken(0);
		setDailyFreeSummonToken(0);
		setExchangeRound(1);
		setExchangeState("0,0,0");
	}

	public Card(Record record) {
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
	 * 所属的人物ID
	 */
	public long getHumanId() {
		return record.get("HumanId");
	}

	public void setHumanId(final long HumanId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HumanId", HumanId);

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
	 * 是否首次免费招募令抽卡
	 */
	public boolean isFreeForCardFirst_BySummonToken() {
		return record.<Integer>get("FreeForCardFirst_BySummonToken") == 1;
	}

	public void setFreeForCardFirst_BySummonToken(boolean FreeForCardFirst_BySummonToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FreeForCardFirst_BySummonToken", FreeForCardFirst_BySummonToken ? 1 : 0);

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
	 * 是否首次免费元宝抽卡
	 */
	public boolean isFreeForCardFirst_ByGold() {
		return record.<Integer>get("FreeForCardFirst_ByGold") == 1;
	}

	public void setFreeForCardFirst_ByGold(boolean FreeForCardFirst_ByGold) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("FreeForCardFirst_ByGold", FreeForCardFirst_ByGold ? 1 : 0);

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
	 * 是否首次花费招募令抽卡
	 */
	public boolean isUserSummonTokenFirst() {
		return record.<Integer>get("UserSummonTokenFirst") == 1;
	}

	public void setUserSummonTokenFirst(boolean UserSummonTokenFirst) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("UserSummonTokenFirst", UserSummonTokenFirst ? 1 : 0);

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
	 * 是否首次花费元宝抽卡
	 */
	public boolean isUserGoldFirst() {
		return record.<Integer>get("UserGoldFirst") == 1;
	}

	public void setUserGoldFirst(boolean UserGoldFirst) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("UserGoldFirst", UserGoldFirst ? 1 : 0);

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
	 * 下次免费招募令抽卡时间
	 */
	public long getDrawCardFreeTimeBySummonToken() {
		return record.get("DrawCardFreeTimeBySummonToken");
	}

	public void setDrawCardFreeTimeBySummonToken(final long DrawCardFreeTimeBySummonToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DrawCardFreeTimeBySummonToken", DrawCardFreeTimeBySummonToken);

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
	 * 下次免费元宝抽卡时间
	 */
	public long getDrawCardFreeTimeByGold() {
		return record.get("DrawCardFreeTimeByGold");
	}

	public void setDrawCardFreeTimeByGold(final long DrawCardFreeTimeByGold) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("DrawCardFreeTimeByGold", DrawCardFreeTimeByGold);

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
	 * 今日元宝抽卡次数
	 */
	public int getTodayDrawByGold() {
		return record.get("TodayDrawByGold");
	}

	public void setTodayDrawByGold(final int TodayDrawByGold) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TodayDrawByGold", TodayDrawByGold);

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
	 * 今日招募令抽卡次数
	 */
	public int getTodayDrawBySummonToken() {
		return record.get("TodayDrawBySummonToken");
	}

	public void setTodayDrawBySummonToken(final int TodayDrawBySummonToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TodayDrawBySummonToken", TodayDrawBySummonToken);

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
	 * 元宝总抽卡数量
	 */
	public int getTotleNumByGold() {
		return record.get("totleNumByGold");
	}

	public void setTotleNumByGold(final int totleNumByGold) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("totleNumByGold", totleNumByGold);

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
	 * 招募令总抽卡数量
	 */
	public int getTotleNumBySummonToken() {
		return record.get("totleNumBySummonToken");
	}

	public void setTotleNumBySummonToken(final int totleNumBySummonToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("totleNumBySummonToken", totleNumBySummonToken);

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
	 * 今日免费招募令抽卡次数
	 */
	public int getDailyFreeSummonToken() {
		return record.get("dailyFreeSummonToken");
	}

	public void setDailyFreeSummonToken(final int dailyFreeSummonToken) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("dailyFreeSummonToken", dailyFreeSummonToken);

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
	 * 兑换第几轮次
	 */
	public int getExchangeRound() {
		return record.get("ExchangeRound");
	}

	public void setExchangeRound(final int ExchangeRound) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ExchangeRound", ExchangeRound);

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
	 * 本轮次每一阶领取状态,0未兑换，1已经兑换
	 */
	public String getExchangeState() {
		return record.get("ExchangeState");
	}

	public void setExchangeState(final String ExchangeState) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ExchangeState", ExchangeState);

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