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
public final class ShopExchange extends EntityBase {
	public static final String tableName = "shopexchange";

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String id = "id";	//id
		public static final String ShopLimitJSON = "ShopLimitJSON";	//限购的玩家已购情况
		public static final String ShopExResetTimes = "ShopExResetTimes";	//兑换商店刷新次数格式  试炼，竞技，公会,众神{1,2,1，1}
		public static final String RefreshRecord = "RefreshRecord";	//每日定点刷新商店记录{0,0,0}
		public static final String TrialShopBuyNum = "TrialShopBuyNum";	//试炼商店限购物品已购买次数
		public static final String CompeteShopBuyNum = "CompeteShopBuyNum";	//竞技场商店限购物品已购买次数
		public static final String GuildShopBuyNum = "GuildShopBuyNum";	//公会商店限购物品已购买次数
		public static final String GWarShopBuyNum = "GWarShopBuyNum";	//众神之战商店限购物品已购买次数
		public static final String ShopMysRefreshTimes = "ShopMysRefreshTimes";	//付费刷新次数
		public static final String ShopMysRefreshJSON = "ShopMysRefreshJSON";	//玩家刷新后得到的商品列表
		public static final String ShopSERefreshTimes = "ShopSERefreshTimes";	//付费刷新次数
		public static final String ShopSERefreshJSON = "ShopSERefreshJSON";	//玩家刷新后得到的商品列表
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public ShopExchange() {
		super();
		setShopExResetTimes("0,0,0,0");
	}

	public ShopExchange(Record record) {
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
	 * 限购的玩家已购情况
	 */
	public String getShopLimitJSON() {
		return record.get("ShopLimitJSON");
	}

	public void setShopLimitJSON(final String ShopLimitJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ShopLimitJSON", ShopLimitJSON);

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
	 * 兑换商店刷新次数格式  试炼，竞技，公会,众神{1,2,1，1}
	 */
	public String getShopExResetTimes() {
		return record.get("ShopExResetTimes");
	}

	public void setShopExResetTimes(final String ShopExResetTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ShopExResetTimes", ShopExResetTimes);

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
	 * 每日定点刷新商店记录{0,0,0}
	 */
	public String getRefreshRecord() {
		return record.get("RefreshRecord");
	}

	public void setRefreshRecord(final String RefreshRecord) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("RefreshRecord", RefreshRecord);

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
	 * 试炼商店限购物品已购买次数
	 */
	public String getTrialShopBuyNum() {
		return record.get("TrialShopBuyNum");
	}

	public void setTrialShopBuyNum(final String TrialShopBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("TrialShopBuyNum", TrialShopBuyNum);

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
	 * 竞技场商店限购物品已购买次数
	 */
	public String getCompeteShopBuyNum() {
		return record.get("CompeteShopBuyNum");
	}

	public void setCompeteShopBuyNum(final String CompeteShopBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("CompeteShopBuyNum", CompeteShopBuyNum);

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
	 * 公会商店限购物品已购买次数
	 */
	public String getGuildShopBuyNum() {
		return record.get("GuildShopBuyNum");
	}

	public void setGuildShopBuyNum(final String GuildShopBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildShopBuyNum", GuildShopBuyNum);

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
	 * 众神之战商店限购物品已购买次数
	 */
	public String getGWarShopBuyNum() {
		return record.get("GWarShopBuyNum");
	}

	public void setGWarShopBuyNum(final String GWarShopBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GWarShopBuyNum", GWarShopBuyNum);

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
	 * 付费刷新次数
	 */
	public int getShopMysRefreshTimes() {
		return record.get("ShopMysRefreshTimes");
	}

	public void setShopMysRefreshTimes(final int ShopMysRefreshTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ShopMysRefreshTimes", ShopMysRefreshTimes);

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
	 * 玩家刷新后得到的商品列表
	 */
	public String getShopMysRefreshJSON() {
		return record.get("ShopMysRefreshJSON");
	}

	public void setShopMysRefreshJSON(final String ShopMysRefreshJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ShopMysRefreshJSON", ShopMysRefreshJSON);

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
	 * 付费刷新次数
	 */
	public int getShopSERefreshTimes() {
		return record.get("ShopSERefreshTimes");
	}

	public void setShopSERefreshTimes(final int ShopSERefreshTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ShopSERefreshTimes", ShopSERefreshTimes);

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
	 * 玩家刷新后得到的商品列表
	 */
	public String getShopSERefreshJSON() {
		return record.get("ShopSERefreshJSON");
	}

	public void setShopSERefreshJSON(final String ShopSERefreshJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ShopSERefreshJSON", ShopSERefreshJSON);

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