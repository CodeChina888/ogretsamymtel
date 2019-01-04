package core.dbsrv;

import java.util.List;

import core.Chunk;
import core.Record;
import core.dbsrv.DBPartServiceProxy;
import core.support.BufferPool;
import core.support.Config;
import core.support.Distr;
import core.support.Param;
import core.support.Utils;
import core.support.function.GofFunction2;
import core.support.function.GofFunction3;

/**
 * 数据操作接口
 */
public class DB {
	// 操作代理类
	private DBPartServiceProxy prx;
	// 操作目标表名
	private String tableName;

	/**
	 * 内部构造函数
	 * @param tableName
	 */
	private DB(String tableName) {
		// 赋值
		this.tableName = tableName;

		// 初始化代理类 根据表名进行代理类压力分摊
		int hash = Math.abs(Utils.hash(tableName.hashCode()));
		String portId = Distr.PORT_DB_PART_PREFIX + hash % Distr.PORT_STARTUP_NUM_DB_PART;
		String nodeId = Distr.NODE_DB;
		if(Config.isCrossSrv) {
			nodeId = Config.getCrossPartDefaultNodeId(Distr.CROSS_NODE_DB);
		}
		this.prx = DBPartServiceProxy.newInstance(nodeId, portId, Distr.SERV_DEFAULT);
		
	}

	/**
	 * 创建一个新的操作接口
	 * @return
	 */
	public static DB newInstance(String tableName) {
		return new DB(tableName);
	}

	/**
	 * 异步监听返回值 当超时后<B>不会</B>进行回调通知
	 * @param func
	 * @param ctx
	 */
	public void listenResult(GofFunction2<Param, Param> func, Object... ctx) {
		prx.listenResult(func, new Param(ctx));
	}

	/**
	 * 异步监听返回值 当超时后<B>会</B>进行回调通知
	 * @param func
	 * @param ctx
	 */
	public void listenResult(GofFunction3<Boolean, Param, Param> func, Object... ctx) {
		prx.listenResult(func, new Param(ctx));
	}

	/**
	 * 异步监听返回值 当超时后<B>不会</B>进行回调通知
	 * @param func
	 * @param ctx
	 */
	public void listenResult(GofFunction2<Param, Param> func, Param ctx) {
		prx.listenResult(func, ctx);
	}

	/**
	 * 异步监听返回值 当超时后<B>会</B>进行回调通知
	 * @param func
	 * @param ctx
	 */
	public void listenResult(GofFunction3<Boolean, Param, Param> func, Param ctx) {
		prx.listenResult(func, ctx);
	}

	/**
	 * 同步等待返回值
	 */
	public Param waitForResult() {
		return prx.waitForResult();
	}

	/**
	 * 新增一条数据
	 */
	public void insert(Record record) {
		prx.insert(record);
	}

	/**
	 * 通过升级包更新数据
	 * @param id
	 * @param patch
	 * @param sync
	 */
	public void update(long id, Chunk patch, boolean sync) {
		prx.update(tableName, id, patch, sync);
	}

	/**
	 * 删除
	 */
	public void delete(long id) {
		prx.delete(tableName, id);
	}

//	/**
//	 * 获取全部数据集合
//	 */
//	public void findAll() {
//		prx.findAll(tableName);
//	}

	/**
	 * 根据主键获取数据
	 * @param ids 主键集合
	 */
	public void find(List<Long> ids) {
		prx.find(tableName, ids);
	}

	/**
	 * 获取符合条件的数据集合 支持排序
	 */
	public void findBy(boolean flush, Object... params) {
		prx.findBy(flush, tableName, params);
	}

	/**
	 * 获取符合条件的数据集合 支持分页，支持排序
	 * firstResult/maxResults设置定包装类型，是以为否则函数调用判断会与findBy(boolean flush,
	 * Object... params)冲突 定义时不冲突 调用时会报错
	 */
	public void findBy(boolean flush, Integer firstResult, Integer maxResults, Object... params) {
		prx.findBy(flush, firstResult, maxResults, tableName, params);
	}

	/**
	 * 获取查询条件的数据集合
	 * @param flush
	 * @param whereAndOther
	 * @param params
	 */
	public void findByQuery(boolean flush, String whereAndOther, Object... params) {
		prx.findByQuery(flush, tableName, whereAndOther, params);
	}

	/**
	 * 刷新单张表的缓存至数据库
	 */
	public void flush() {
		prx.flush(tableName);
	}

	/**
	 * 刷新全部缓存至数据库
	 */
	public void flushAll() {
		prx.flushAll();
	}

	/**
	 * 根据主键获取数据
	 * @param id 主键
	 */
	public void get(long id) {
		prx.get(tableName, id);
	}

	/**
	 * 获取符合条件的单体数据 如果有多条符合则返回第一条
	 * @param flush
	 * @param params
	 */
	public void getBy(boolean flush, Object... params) {
		prx.getBy(flush, tableName, params);
	}

	/**
	 * 获取查询的单体数据 如果有多条符合则返回第一条
	 * @param flush
	 * @param whereAndOther
	 * @param params
	 */
	public void getByQuery(boolean flush, String whereAndOther, Object... params) {
		prx.getByQuery(flush, tableName, whereAndOther, params);
	}

	/**
	 * 获取符合条件的数据数量
	 */
	public void countBy(boolean flush, Object... params) {
		prx.countBy(flush, tableName, params);
	}

	/**
	 * 获取查询的单体数据 如果有多条符合则返回第一条
	 */
	public void countByQuery(boolean flush, String whereAndOther, Object... params) {
		prx.countByQuery(flush, tableName, whereAndOther, params);
	}

	/**
	 * 获取单表数据总数量
	 */
	public void countAll(boolean flush) {
		prx.countAll(flush, tableName);
	}

	/**
	 * 清空表
	 */
	public void deleteAll() {
		prx.deleteAll(tableName);
	}

	/**
	 * 执行SQL语句，支持?占位符 除非特殊情况下，否则不建议调用本函数 因为牵扯到写缓存及多线程执行等问题，所以仅支持单表操作
	 * @param needResult 调用方是否需要收到执行完毕的消息（没有返回值，仅仅是完成返回通知）
	 * @param flush 执行前刷新写缓存
	 * @param sql 执行语句
	 * @param params 执行参数
	 */
	public void sql(boolean needResult, boolean flush, String sql, Object... params) {
		prx.sql(needResult, flush, tableName, sql, params);
	}

	/**
	 * 获取数据库表信息
	 */
	public void findFieldTable() {
		prx.findFieldTable();
	}
	
	/**
	 * 强制存库一条数据
	 */
	public void flush(long id, Record r) {
		Chunk patch = r.pathAllGen();
		update(id, patch, true);
		//回收缓冲buff
		BufferPool.deallocate(patch.buffer);
	}

}