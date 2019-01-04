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
public final class Castellan extends EntityBase {
	public static final String tableName = "castellan";

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String id = "id";	//id
		public static final String HumanId = "HumanId";	//所属的人物ID
		public static final String Name = "Name";	//人物姓名
		public static final String Declaration = "Declaration";	//城主宣言
		public static final String HasBuyNum = "HasBuyNum";	//已购买次数
		public static final String type = "type";	//城主类型,与ECastellanType对应
		public static final String ModelSn = "ModelSn";	//城主ModelSn
		public static final String isNotice = "isNotice";	//是否通知客户端
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public Castellan() {
		super();
		setHasBuyNum(0);
		setType(0);
		setModelSn(0);
		setIsNotice(false);
	}

	public Castellan(Record record) {
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
	 * 人物姓名
	 */
	public String getName() {
		return record.get("Name");
	}

	public void setName(final String Name) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Name", Name);

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
	 * 城主宣言
	 */
	public String getDeclaration() {
		return record.get("Declaration");
	}

	public void setDeclaration(final String Declaration) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("Declaration", Declaration);

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
	 * 已购买次数
	 */
	public int getHasBuyNum() {
		return record.get("HasBuyNum");
	}

	public void setHasBuyNum(final int HasBuyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("HasBuyNum", HasBuyNum);

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
	 * 城主类型,与ECastellanType对应
	 */
	public int getType() {
		return record.get("type");
	}

	public void setType(final int type) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("type", type);

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
	 * 城主ModelSn
	 */
	public int getModelSn() {
		return record.get("ModelSn");
	}

	public void setModelSn(final int ModelSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("ModelSn", ModelSn);

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
	 * 是否通知客户端
	 */
	public boolean isIsNotice() {
		return record.<Integer>get("isNotice") == 1;
	}

	public void setIsNotice(boolean isNotice) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("isNotice", isNotice ? 1 : 0);

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