package core.dbsrv;

import static core.db.QueueStatusKey.S_EXEC;
import static core.db.QueueStatusKey.S_NONE;
import static core.db.QueueStatusKey.S_QUERY;
import static core.db.QueueTypeKey.T_COUNT;
import static core.db.QueueTypeKey.T_EXEC;
import static core.db.QueueTypeKey.T_QUERY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import core.CallReturn;
import core.Chunk;
import core.Port;
import core.Record;
import core.Service;
import core.db.CachedRecords;
import core.db.CachedTables;
import core.db.DBConsts;
import core.db.DBKey;
import core.db.Field;
import core.db.FieldTable;
import core.db.FlushingRecords;
import core.db.FlushingTables;
import core.db.QueueTables;
import core.db.QueueUnit;
import core.dbsrv.DBLineService;
import core.dbsrv.DBLineServiceProxy;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.interfaces.IRecord;
import core.statistics.StatisticsDB;
import core.support.Config;
import core.support.Distr;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;
import core.support.Utils;
import core.support.function.GofFunction2;
import core.support.function.GofFunction3;
import core.support.log.LogCore;

/**
 * 数据库写缓存&分配服务
 */
@DistrClass(importClass = {Chunk.class, List.class, Record.class})
public class DBPartService extends Service {
	// 字段长度（用来预估缓冲大小）
	private static final int COL_DEF_LEN = 12;

	// 全部参数KEY
	private static final String SQL_KEY_ALL = "*";

	// 执行基础语句
	private static final String SQL_BASE_UPDATE = "UPDATE `{}` SET {} WHERE `id`={}";
	private static final String SQL_BASE_INSERT = "INSERT INTO `{}`({}) VALUES({})";
	private static final String SQL_BASE_SELECT_BY = "SELECT {} FROM `{}` {}";
	private static final String SQL_BASE_SELECT_COL = "SELECT {} FROM `{}`";
	private static final String SQL_BASE_SELECT_ALL = "SELECT * FROM `{}`";
	private static final String SQL_BASE_COUNT = "SELECT COUNT(1) FROM `{}` {}";
	private static final String SQL_BASE_DELETE = "DELETE FROM `{}` WHERE `id`=?";
	private static final String SQL_BASE_DELETE_ALL = "DELETE FROM `{}`";

	// 常用SQL语句缓存
	private final Map<String, String> SQL_INSERT = new HashMap<>(); // 新增SQL语句缓存<表名,SQL>
	private final Map<String, String> SQL_SELECT_ALL = new HashMap<>(); // 查询全部数据SQL语句缓存<表名,SQL>
	private final Map<String, String> SQL_DELETE = new HashMap<>(); // 删除SQL语句缓存<表名,SQL>
	private final Map<String, String> SQL_DELETE_ALL = new HashMap<>(); // 清空表SQL语句缓存<表名,SQL>

	// 数据读取缓存
	// private final CachedTables cachedRead = new CachedTables();
	// 数据写入缓存
	private final CachedTables cachedWrite = new CachedTables();
	// 写入缓存刷新中的临时缓存
	private final FlushingTables flushing = new FlushingTables();
	// 执行队列
	private final QueueTables queue = new QueueTables();

	// 缓冲同步间隔
	private final TickTimer flushTimer = new TickTimer(Time.SEC);

	// 统计执行命令输出间隔
	private final TickTimer numTimer = new TickTimer(59 * Time.MIN);
	// 统计执行命令数量
	private long numPatch; // 数据升级包
	private long numQuery; // 查询
	private long numExec; // 执行
	private long numCount; // 数量统计
	
	//检索最大数值（当没有限定检索数量的时候用此数值）
	private static int MAX_GET_COUNT = 2000;

	/**
	 * 构造函数
	 * @param port
	 */
	public DBPartService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return Distr.SERV_DEFAULT;
	}

	@DistrMethod
	public void findFieldTable() {
		port.returns(FieldTable.getCache());
	}

	/**
	 * 通过升级包更新数据
	 * @param tableName
	 * @param id
	 * @param patch
	 * @param sync
	 */
	@DistrMethod
	public void update(String tableName, long id, Chunk patch, boolean sync) {
		/* 写缓存 */
		CachedRecords write = cachedWrite.getOrCreate(tableName);
		Record w = write.get(id);
		// 初始化
		if (w == null) {
			w = Record.newInstance(tableName);
			w.setStatus(DBConsts.RECORD_STATUS_MODIFIED);
			w.set("id", id);
			// 记录写缓存创建时间
			w.setWriteCacheCreateTime(port.getTimeCurrent());

			write.put(id, w);
		}

		// 使修改生效
		w.patchUpdate(patch, LogCore.db.isDebugEnabled());

		// 立即持久化至数据库
		if (sync || Config.DB_CACHED_SYNC_SEC == 0) {
			syncRecord(w);
		}

		// 执行数量记录增加
		numPatch++;

		// 统计运行信息
		StatisticsDB.patch(tableName, patch.length);
	}

	/**
	 * 新增一条数据
	 */
	@DistrMethod
	public void insert(Record record) {
		// 立即持久化
		syncRecord(record);
	}

	/**
	 * 删除
	 * @param tableName
	 * @param id
	 */
	@DistrMethod
	public void delete(String tableName, long id) {
		// 模拟一个record 进行删除
		Record record = Record.newInstance(tableName);
		record.setStatus(DBConsts.RECORD_STATUS_DELETED);
		record.set("id", id);

		// 删除数据
		syncRecord(record);

		// 删除写缓存
		cachedWrite.removeRecord(tableName, id);
	}

	/**
	 * 获取符合条件的数据数量
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void countBy(boolean flush, String tableName, Object... params) {
		utilCount(flush, tableName, params);
	}

	/**
	 * 获取查询的单体数据 如果有多条符合则返回第一条
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void countByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		utilSqlCount(flush, tableName, whereAndOther, params);
	}

	/**
	 * 获取单表数据总数量
	 * @param flush
	 * @param tableName
	 */
	@DistrMethod
	public void countAll(boolean flush, String tableName) {
		utilCount(flush, tableName);
	}

	/**
	 * 根据主键获取数据
	 * @param tableName 表名
	 * @param id 主键
	 */
	@DistrMethod
	public void get(String tableName, long id) {
		utilGetBy(false, tableName, "id", id);
	}

	/**
	 * 获取符合条件的单体数据 如果有多条符合则返回第一条
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void getBy(boolean flush, String tableName, Object... params) {
		utilGetBy(flush, tableName, params);
	}

	/**
	 * 获取查询的单体数据 如果有多条符合则返回第一条
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void getByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		utilSqlBase(flush, true, tableName, whereAndOther, params);
	}

//	/**
//	 * 获取全部数据集合
//	 * @param tableName
//	 * @param params
//	 */
//	@DistrMethod
//	public void findAll(String tableName) {
//		findBy(false, tableName);
//	}

	/**
	 * 根据主键获取数据
	 * @param tableName 表名
	 * @param ids 主键集合
	 */
	@DistrMethod
	public void find(String tableName, List<Long> ids) {
		utilFindBy(false, 0, ids.size(), tableName, "id", ids);
	}

	/**
	 * 获取符合条件的数据集合 支持排序
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void findBy(boolean flush, String tableName, Object... params) {
		//findBy(flush, 0, Integer.MAX_VALUE, tableName, params);
		findBy(flush, 0, MAX_GET_COUNT, tableName, params);
	}

	/**
	 * 获取符合条件的数据集合 支持分页，支持排序
	 * @param tableName
	 * @param firstResult
	 * @param maxResults
	 * @param params
	 */
	@DistrMethod
	public void findBy(boolean flush, int firstResult, int maxResults, String tableName, Object... params) {
		utilFindBy(flush, firstResult, maxResults, tableName, params);
	}

	/**
	 * 获取查询条件的数据集合
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void findByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		utilSqlBase(flush, false, tableName, whereAndOther, params);
	}

	/**
	 * 获取查询条件的数据集合
	 * @param tableName
	 */
	@DistrMethod
	public void deleteAll(String tableName) {
		// 执行SQL语句
		String sql = SQL_DELETE_ALL.get(tableName);

		// 没有找到缓存SQL 开始创建
		if (sql == null) {
			sql = Utils.createStr(SQL_BASE_DELETE_ALL, tableName);

			// 缓存SQL
			SQL_DELETE_ALL.put(tableName, sql);

			// 日志
			if (LogCore.db.isDebugEnabled()) {
				LogCore.db.debug("[{}]生成清空表SQL语句：table={}, sql={}", port.getId(), tableName, sql);
			}
		}

		sql(false, true, tableName, sql);
	}

	/**
	 * 执行SQL语句，支持?占位符 除非特殊情况下，否则不建议调用本函数 因为牵扯到缓存及多线程执行等问题，所以仅支持单表操作
	 * @param needResult 调用方是否需要收到执行完毕的消息（没有返回值，仅仅是完成返回通知）
	 * @param flush 执行前刷新写缓存
	 * @param tableName 执行语句的目标表名称
	 * @param sql 执行语句
	 * @param params 执行参数
	 */
	@DistrMethod
	public void sql(boolean needResult, boolean flush, String tableName, String sql, Object... params) {
		// 刷新缓存
		if (flush) {
			LogCore.db.info("[{}]调用了execute函数，触发写缓存刷新操作：tableName={}", port.getId(), tableName);
			flushTable(tableName, Long.MAX_VALUE);
		}

		// 加入执行队列
		QueueUnit qu = new QueueUnit(T_EXEC, flush ? S_EXEC : S_NONE, sql, params);
		queue.put(tableName, qu);

		// 监控返回值
		if (needResult) {
			qu.listenResult(this::_result_sql, "rid", port.createReturnAsync());
		}

		// 统计运行数据
		StatisticsDB.sql(tableName);
	}

	/**
	 * 执行SQL语句回调监听
	 * @param results
	 * @param context
	 */
	public void _result_sql(Param results, Param context) {
		long rid = context.get("rid");
		Object success = results.get("success");
		Object count = results.get("count");

		port.returnsImmutableAsync(rid, "success", success, "count", count);
	}

	/**
	 * 刷新全部缓存至数据库
	 */
	@DistrMethod
	public void flushAll() {
		flushDatabase(Long.MAX_VALUE);

		// 日志
		if (LogCore.db.isInfoEnabled()) {
			LogCore.db.info("[{}]远程调用刷新数据库全部写缓存。", port.getId());
		}
	}

	/**
	 * 刷新单张表的缓存至数据库
	 * @param tableName
	 */
	@DistrMethod
	public void flush(String tableName) {
		int count = flushTable(tableName, Long.MAX_VALUE);

		// 日志
		if (count > 0 && LogCore.db.isInfoEnabled()) {
			LogCore.db.info("[{}]远程调用刷新数据库写缓存，表名={}，数量={}", port.getId(), tableName, count);
		}
	}

	/**
	 * 刷新全部缓存至数据库
	 */
	private void flushDatabase(long flushTime) {
		// 缓存间隔时间为0时 就不用定时同步了
		// 因为更新时已经做了判断 会立即持久化至数据库
		if (Config.DB_CACHED_SYNC_SEC == 0) {
			return;
		}

		// 遍历数据 找到需要刷新的缓存
		int count = 0;
		for (String tableName : cachedWrite.getTableNames()) {
			int c = flushTable(tableName, flushTime);
			// 增加刷新数据量
			count += c;
		}

		// 记录日志
		// 关闭时自动调用flushNowOnce时会报错 所以忽略这种情况
		// 开启日志 && 不是关闭前刷新 && 有需要持久化的数据 && (是全局刷新 || 按时间刷新时符合抽样概率)
		if (count > 0 && LogCore.db.isInfoEnabled() && (flushTime == Long.MAX_VALUE || new Random().nextInt(10) == 0)) {
			LogCore.db.info("[{}]进行缓存同步，同步数据量={}。", port.getId(), count);
		}
	}

	/**
	 * 刷新单张表的缓存至数据库
	 * @param tableName 需要刷新的表名
	 * @param flushTime 刷新时间戳 小于这个时间之前的数据都需要进行持久化。
	 * @return
	 */
	private int flushTable(String tableName, long flushTime) {
		CachedRecords cached = cachedWrite.getOrCreate(tableName);

		// 确认需要刷新的数据
		// 因为syncRecord()逻辑中会修改CachedRecords集合引起抛错，
		// 所以此处先确认需要同步数据后，再在新循环中进行操作。
		List<Record> rs = new ArrayList<>();
		for (Record r : cached.values()) {
			// 不是脏数据 忽略
			if (!r.isDirty())
				continue;
			// 未达到刷新时间 忽略
			if (r.getWriteCacheCreateTime() > flushTime) {
				continue;
			}

			rs.add(r);
		}

		// 持久化数据
		for (Record r : rs) {
			syncRecord(r);
		}

		return rs.size();
	}

	/**
	 * 查询返回符合结果数量的基础函数
	 * @param tableName
	 * @param params
	 * @return
	 */
	private void utilCount(boolean flush, String tableName, Object... params) {
		// 查询参数
		Map<String, Object> paramsFilter = new LinkedHashMap<>(); // 过滤条件

		// 处理成对参数 & 当查询条件中有指定了ID时，可以进行执行队列分配优化
		long singleId = -1L;
		int len = params.length;
		for (int i = 0; i < len; i += 2) {
			Object key = params[i];
			Object val = params[i + 1];

			// 参数 排序规则忽略
			if (key instanceof DBKey)
				continue;
			// 忽略ID是集合的
			if (val instanceof Collection)
				continue;

			// 参数 过滤条件
			paramsFilter.put(key.toString(), val);

			// 取得指定ID
			if (key.equals(Record.PRIMARY_KEY_NAME)) {
				singleId = (val instanceof Long) ? (long) val : (int) val;
			}
		}

		// 最终查询SQL
		String sql = Utils.createStr(SQL_BASE_COUNT, tableName, utilBaseGenSqlWhere(paramsFilter));

		// 刷新本表的缓存
		if (flush) {
			flushTable(tableName, Long.MAX_VALUE);

			// 刷新日志
			LogCore.db.info("[{}]执行查询引起数据库刷新缓存操作，tableName={}, sql={}", port.getId(), tableName, sql);
		}

		// 创建异步返回
		CallReturn callReturn = port.getCall().createCallReturn();

		// 加入执行队列
		QueueUnit qu = new QueueUnit(T_COUNT, flush ? S_QUERY : S_NONE, sql, paramsFilter.values().toArray());
		if (singleId >= 0)
			qu.id(singleId);
		qu.listenResult(this::_result_utilCount, "callReturn", callReturn);

		queue.put(tableName, qu);
	}

	/**
	 * SQL结果数量查询基础函数
	 * @param whereAndOther
	 * @param params
	 * @return
	 */
	private void utilSqlCount(boolean flush, String tableName, String whereAndOther, Object... params) {
		List<Object> settings = Utils.ofList(params);

		// 拼接最终SQL
		String sql = Utils.createStr(SQL_BASE_COUNT, tableName, whereAndOther);

		// 刷新本表的缓存
		if (flush) {
			flushTable(tableName, Long.MAX_VALUE);

			// 刷新日志
			if (LogCore.db.isInfoEnabled()) {
				LogCore.db.info("[{}]执行查询引起数据库刷新缓存操作，tableName={}, sql={}", port.getId(), tableName, sql);
			}
		}

		// 创建异步返回
		CallReturn callReturn = port.getCall().createCallReturn();

		// 加入执行队列
		QueueUnit qu = new QueueUnit(T_COUNT, flush ? S_QUERY : S_NONE, sql, settings.toArray());
		qu.listenResult(this::_result_utilCount, "callReturn", callReturn);

		queue.put(tableName, qu);
	}

	public void _result_utilCount(Param results, Param context) {
		CallReturn callReturn = context.get("callReturn");
		int count = results.get();

		// 返回
		port.returnsImmutable(callReturn, count);
	}

	/**
	 * 查询返回数据集合的基础函数
	 * @param tableName
	 * @param firstResult
	 * @param maxResults
	 * @param params
	 * @return
	 */
	private void utilFindBy(boolean flush, int firstResult, int maxResults, String tableName, Object... params) {
		utilBase(flush, false, firstResult, maxResults, tableName, params);
	}

	/**
	 * 查询返回单体数据的基础函数
	 */
	private void utilGetBy(boolean flush, String tableName, Object... params) {
		utilBase(flush, true, 0, 1, tableName, params);
	}

	/**
	 * 查询信息基础函数 当查询出的结果在缓存中已存在时，返回缓存中的数据。 当需要缓存结果时，如果缓存已存在，则不更新缓存。
	 * @param flush 是否需要先刷新缓存
	 * @param single 是否返回单一结果
	 * @param tableName
	 * @param firstResult
	 * @param maxResults
	 * @param params
	 * @return
	 */
	private void utilBase(boolean flush, boolean single, int firstResult, int maxResults, String tableName,
			Object... params) {
		// 需要执行的SQL及参数
		Param sqlParam = utilBaseCreateSql(firstResult, maxResults, tableName, params);
		String sql = sqlParam.get("sql");
		Object[] pa = sqlParam.get("params");

		// 刷新本表的缓存
		if (flush) {
			flushTable(tableName, Long.MAX_VALUE);

			// 刷新日志
			if (LogCore.db.isInfoEnabled()) {
				LogCore.db.info("[{}]执行查询引起数据库刷新缓存操作，tableName={}, sql={}", port.getId(), tableName, sql);
			}
		}

		// 当查询条件中有指定了ID时，可以进行执行队列分配优化
		long singleId = -1L;
		int len = params.length;
		for (int i = 0; i < len; i += 2) {
			Object key = params[i];
			Object val = params[i + 1];
			// 忽略规则参数
			if (key instanceof DBKey)
				continue;
			// 忽略不是ID的参数
			if (!key.equals(Record.PRIMARY_KEY_NAME))
				continue;
			// 忽略ID是集合的
			if (val instanceof Collection)
				continue;
			// 获取ID
			singleId = (val instanceof Long) ? (long) val : (int) val;
		}

		// 创建异步返回
		CallReturn callReturn = port.getCall().createCallReturn();

		// 加入执行队列
		// 如果查询前刷新了 那么line那边直接返回即可
		// 如果没刷新就需要返回到这里再未入库数据同步
		QueueUnit qu = new QueueUnit(T_QUERY, flush ? S_QUERY : S_NONE, sql, pa);
		if (singleId >= 0)
			qu.id(singleId);
		qu.listenResult(this::_result_utilBase, "tableName", tableName, "callReturn", callReturn, "single", single);

		queue.put(tableName, qu);
	}

	/**
	 * 查询语句基础函数
	 * @return
	 */
	private void utilSqlBase(boolean flush, boolean single, String tableName, String whereAndOther, Object... params) {
		// 需要执行的SQL及参数
		Param sqlParam = utilSqlBaseCreateSql(tableName, whereAndOther, params);
		String sql = sqlParam.get("sql");
		Object[] pa = sqlParam.get("params");

		// 刷新本表的缓存
		if (flush) {
			flushTable(tableName, Long.MAX_VALUE);

			// 刷新日志
			if (LogCore.db.isInfoEnabled()) {
				LogCore.db.info("[{}]执行查询引起数据库刷新缓存操作，tableName={}, sql={}", port.getId(), tableName, sql);
			}
		}

		// 创建异步返回
		CallReturn callReturn = port.getCall().createCallReturn();

		// 加入执行队列
		// 如果查询前刷新了 那么line那边直接返回即可
		// 如果没刷新就需要返回到这里再未入库数据同步
		QueueUnit qu = new QueueUnit(T_QUERY, flush ? S_QUERY : S_NONE, sql, pa);
		qu.listenResult(this::_result_utilBase, "tableName", tableName, "callReturn", callReturn, "single", single);
		queue.put(tableName, qu);
	}

	public void _result_utilBase(Param results, Param context) {
		// 能来这个逻辑的 证明上面没做flush刷新缓存操作 需要做未入库数据修正
		List<? extends IRecord> result = results.get();
		String tableName = context.get("tableName");
		CallReturn callReturn = context.get("callReturn");
		boolean single = context.get("single");

		// 写缓存
		CachedRecords write = cachedWrite.getOrCreate(tableName);
		// 刷新中的临时缓存
		FlushingRecords flush = flushing.getOrCreate(tableName);

		// 未入库数据修正
		for (IRecord r : result) {
			flush.pathUpdate(r);
			write.pathUpdate(r);
		}

		// 处理返回值
		Object rst;
		if (single) {
			rst = result.isEmpty() ? null : result.get(0);
		} else {
			rst = result;
		}

		// 返回
		port.returnsImmutable(callReturn, rst);
	}

	/**
	 * 创建基本查询SQL语句
	 * @param firstResult
	 * @param maxResults
	 * @param tableName
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Param utilBaseCreateSql(int firstResult, int maxResults, String tableName, Object... params) {
		// 查询参数
		Map<String, Object> paramsFilter = new LinkedHashMap<>(); // 过滤条件
		Map<String, DBKey> paramsOrder = new LinkedHashMap<>(); // 排序规则
		List<String> paramsColumn = new ArrayList<>(); // 限定返回字段

		// 处理成对参数
		int len = params.length;
		for (int i = 0; i < len; i += 2) {
			Object key = params[i];
			Object val = params[i + 1];

			// 过滤条件
			if (!(key instanceof DBKey)) {
				paramsFilter.put(key.toString(), val);

				// 过滤规则
			} else {
				DBKey k = (DBKey) key;
				switch (k) {
					case ORDER_ASC :
					case ORDER_DESC : {
						paramsOrder.put(val.toString(), k);
					}
						break;
					case COLUMN : {
						paramsColumn.addAll((List<String>) val);
					}
						break;
					default : {
						LogCore.db.error("自动忽略语句中无法解析的参数，请检查：无法识别参数={}", k);
					}
						break;
				}
			}
		}

		// 根据参数拼装出参数
		String sql = utilBaseGenSqlMain(tableName, paramsColumn) + utilBaseGenSqlWhere(paramsFilter)
				+ utilBaseGenSqlOrderBy(paramsOrder) + utilBaseGenSqlLimit(firstResult, maxResults);

		return new Param("sql", sql, "params", paramsFilter.values().toArray());
	}

	/**
	 * 多表查询或返回部分值时 无法缓存结果
	 * @param params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Param utilSqlBaseCreateSql(String tableName, String whereAndOther, Object... params) {
		// 查询参数
		List<Object> paramsFilter = new ArrayList<>(); // 过滤条件
		List<String> paramsColumn = new ArrayList<>(); // 限定返回字段

		// 处理成对参数
		int len = params.length;
		for (int i = 0; i < len; i++) {
			Object key = params[i];

			// 过滤条件
			if (!(key instanceof DBKey)) {
				paramsFilter.add(key.toString());

				// 过滤规则
			} else {
				// 自动取下一个参数
				Object v = params[++i];
				DBKey k = (DBKey) key;
				switch (k) {
					case COLUMN : {
						paramsColumn.addAll((List<String>) v);
					}
						break;
					default : {
						LogCore.db.error("自动忽略语句中无法解析的参数，请检查：无法识别参数={}", k);
					}
						break;
				}
			}
		}

		/* 字段修饰 */
		// 拼接属性SQL
		String columnStr;

		// 特殊字段 单独处理
		if (paramsColumn.isEmpty()) {
			columnStr = SQL_KEY_ALL;

			// 常规字段
		} else {
			// 拼属性字符串
			int size = paramsColumn.size();
			StringBuilder sb = new StringBuilder(size * COL_DEF_LEN);
			for (int i = 0; i < size; i++) {
				String c = paramsColumn.get(i);

				// 多个参数分割符
				if (i > 0) {
					sb.append(",");
				}

				// 具体参数
				sb.append("`").append(c).append("`");
			}

			// 如果玩家查询属性中 没有ID 就给他加上，否则之后无法根据ID使未入库数据生效了。
			if (!paramsColumn.contains("id")) {
				sb.append(",`id`");
			}

			// 查询字段最终SQL
			columnStr = sb.toString();
		}

		// 拼接最终SQL
		String sql = Utils.createStr(SQL_BASE_SELECT_BY, columnStr, tableName, whereAndOther);

		return new Param("sql", sql, "params", paramsFilter.toArray());
	}

	/**
	 * 查询信息基础函数工具函数 用来生成SQL主体语句
	 * @param tableName
	 * @return
	 */
	private String utilBaseGenSqlMain(String tableName, List<String> columns) {
		// 返回全部字段
		if (columns.isEmpty()) {
			return utilBaseGenSqlMainAll(tableName);

			// 返回部分字段
		} else {
			return utilBaseGenSqlMainColumn(tableName, columns);
		}
	}

	/**
	 * 查询信息基础函数工具函数 用来生成SQL主体语句 当要返回全部字段时
	 * @param tableName
	 * @return
	 */
	private String utilBaseGenSqlMainAll(String tableName) {
		// 获取SQL
		String sql = SQL_SELECT_ALL.get(tableName);
		if (sql != null)
			return sql;

		// 没有找到缓存SQL 开始创建
		sql = Utils.createStr(SQL_BASE_SELECT_ALL, tableName);

		// 缓存SQL
		SQL_SELECT_ALL.put(tableName, sql);

		// 日志
		if (LogCore.db.isDebugEnabled()) {
			LogCore.db.debug("[{}]生成基础查询SQL语句：table={}, sql={}", port.getId(), tableName, sql);
		}

		return sql;
	}

	/**
	 * 查询信息基础函数工具函数 用来生成SQL主体语句 当要返回部分字段时
	 * @param tableName
	 * @return
	 */
	private String utilBaseGenSqlMainColumn(String tableName, List<String> columns) {
		// 拼属性字符串
		int size = columns.size();
		StringBuilder sb = new StringBuilder(size * COL_DEF_LEN);
		for (int i = 0; i < size; i++) {
			String c = columns.get(i);

			// 多个参数分割符
			if (i > 0) {
				sb.append(",");
			}

			// 具体参数
			sb.append("`").append(c).append("`");
		}

		// 如果玩家查询属性中 没有ID 就给他加上，否则之后无法根据ID使未入库数据生效了。
		if (!columns.contains("id")) {
			sb.append(",`id`");
		}

		// 查询字段最终SQL
		String columnStr = sb.toString();

		// 拼装所需SQL
		String sql = Utils.createStr(SQL_BASE_SELECT_COL, columnStr, tableName);

		return sql;
	}

	/**
	 * 查询信息基础函数工具函数 用来生成where语句
	 * @return
	 */
	private String utilBaseGenSqlWhere(Map<String, Object> paramsFilter) {
		// SQL语句
		StringBuilder sqlWhere = new StringBuilder(34);
		// 无参数 立即返回
		if (paramsFilter.isEmpty())
			return sqlWhere.toString();

		// 将参数拼装为?占位符的形式
		for (Entry<String, Object> e : paramsFilter.entrySet()) {
			String key = e.getKey();
			Object val = e.getValue();

			// 需要增加and分割
			if (sqlWhere.length() > 0)
				sqlWhere.append(" AND ");

			// 属性名
			sqlWhere.append("`").append(key).append("`");

			// 属性值 如果是List则判定为in语句
			if (val instanceof Collection) {
				Collection<?> vals = (Collection<?>) val;
				// 拼装in
				sqlWhere.append(" in ").append("(");
				for (int i = 0; i < vals.size(); i++) {
					if (i > 0)
						sqlWhere.append(",");
					sqlWhere.append("?");
				}
				sqlWhere.append(")");
			} else { // 默认为相等
				sqlWhere.append("=").append("?");
			}
		}

		// 在头部插入where语句
		sqlWhere.insert(0, " WHERE ");

		return sqlWhere.toString();
	}

	/**
	 * 查询信息基础函数工具函数 用来生成orderBy语句
	 * @return
	 */
	private String utilBaseGenSqlOrderBy(Map<String, DBKey> params) {
		// SQL语句
		StringBuilder orderWhere = new StringBuilder(34);
		// 无参数 立即返回
		if (params.isEmpty())
			return orderWhere.toString();

		// 将参数拼装
		for (Entry<String, DBKey> e : params.entrySet()) {
			// 需要增加and分割
			if (orderWhere.length() > 0)
				orderWhere.append(",");

			orderWhere.append("`").append(e.getKey()).append("`").append(" ")
					.append(e.getValue() == DBKey.ORDER_ASC ? "asc" : "desc");
		}

		orderWhere.insert(0, " ORDER BY ");

		return orderWhere.toString();
	}

	/**
	 * 查询信息基础函数工具函数 用来生成limit语句
	 * @return
	 */
	private String utilBaseGenSqlLimit(int firstResult, int maxResults) {
		// SQL语句
		StringBuilder limitWhere = new StringBuilder(34);

		// 默认值 无需limit语句
		if (firstResult == 0 && maxResults == Integer.MAX_VALUE) {
			return limitWhere.toString();
		}

		// 拼装SQL
		limitWhere.append(" LIMIT ").append(firstResult).append(",").append(maxResults);

		return limitWhere.toString();
	}

	/**
	 * 同步
	 * @param record
	 */
	private void syncRecord(Record record) {
		// 没有改动过
		if (!record.isDirty()) {
			return;
		}

		// 根据状态进行不同处理
		switch (record.getStatus()) {
		// 新增
			case DBConsts.RECORD_STATUS_NEW : {
				_syncRecordInsert(record);
				break;
			}
			// 修改
			case DBConsts.RECORD_STATUS_MODIFIED : {
				_syncRecordUpdate(record);
				break;
			}
			// 删除
			case DBConsts.RECORD_STATUS_DELETED : {
				_syncRecordDelete(record);
				break;
			}
		}
	}

	/**
	 * 将record新增数据持久化到数据库
	 * @param record
	 */
	private void _syncRecordInsert(Record record) {
		// 状态错误
		if (!record.isNew())
			return;

		// 执行SQL语句
		String sql = syncRecordInsertSQLGen(record);

		// 实际参数值
		Object[] params = syncRecordInsertParamsGen(record);

		// 执行
		QueueUnit qu = new QueueUnit(T_EXEC, S_EXEC, sql, params);
		qu.id(record.get("id"));
		queue.put(record.getTableName(), qu);

		// 重置状态
		record.resetStatus();

		// 统计运行数据
		StatisticsDB.insert(record.getTableName());
	}

	/**
	 * 将record被删除数据持久化到数据库
	 * @param record
	 */
	private void _syncRecordDelete(Record record) {
		// 状态错误
		if (!record.isDeleted())
			return;

		String tableName = record.getTableName();
		long id = record.get("id");

		// 执行SQL语句
		String sql = SQL_DELETE.get(tableName);

		// 没有找到缓存SQL 开始创建
		if (sql == null) {
			sql = Utils.createStr(SQL_BASE_DELETE, tableName);

			// 缓存SQL
			SQL_DELETE.put(tableName, sql);

			// 日志
			if (LogCore.db.isDebugEnabled()) {
				LogCore.db.debug("[{}]生成删除SQL语句：table={}, sql={}", port.getId(), tableName, sql);
			}
		}

		// 执行
		QueueUnit qu = new QueueUnit(T_EXEC, S_EXEC, sql, new Object[]{id});
		qu.id(id);
		queue.put(tableName, qu);

		// 统计运行数据
		StatisticsDB.delete(record.getTableName());
	}

	/**
	 * 将record更新数据持久化到数据库
	 * @param record
	 */
	private void _syncRecordUpdate(Record record) {
		// 状态错误
		if (!record.isModified())
			return;

		// 主键
		long id = record.get("id");
		String tableName = record.getTableName();

		// SET语句部分
		Map<String, Object> sqlSetMap = syncRecordUpdateSetGen(record);
		String sqlSet = (String) sqlSetMap.get("sql");
		Object[] params = (Object[]) sqlSetMap.get("params");

		// 最终SQL
		String sql = Utils.createStr(SQL_BASE_UPDATE, tableName, sqlSet, id);

		// 加入执行队列
		QueueUnit qu = new QueueUnit(T_EXEC, S_EXEC, sql, params).id(id);
		long ver = queue.put(tableName, qu);
		// 设置回调监听
		qu.listenResult(this::_result_syncRecordUpdate, "version", ver, "tableName", tableName, "id", id);

		/* 将缓存数据移入刷新中的临时缓存 */
		// 清除写缓存
		CachedRecords write = cachedWrite.getOrCreate(tableName);
		write.remove(id);

		// 移入刷新临时缓存
		FlushingRecords flush = flushing.getOrCreate(tableName);
		flush.put(id, ver, record);

		// 统计运行数据
		StatisticsDB.update(record.getTableName());
	}

	/**
	 * 监控回调 将record更新数据持久化到数据库
	 */
	public void _result_syncRecordUpdate(boolean timeout, Param results, Param ctx) {
		String tableName = ctx.get("tableName");
		long id = ctx.get("id");
		long version = ctx.get("version");

		// 删除刷新临时缓存
		if (version > 0) {
			FlushingRecords flush = flushing.getOrCreate(tableName);
			flush.remove(id, version);
		}

		// 记录日志
		if (timeout) {// 超时
			LogCore.db.error("执行更新操作超时：cxt={}", ctx);
		} else if (!results.<Boolean> get("success")) {
			// 正常返回 如果失败也记录
			LogCore.db.error("执行更新操作失败：cxt={}", ctx);
		}
	}

	/**
	 * 将record新增数据持久化到数据库 占位语句生成
	 * @param record
	 * @return <sqlInto: [属性列], sqlValues, [占位符], params: [参数]>
	 */
	private String syncRecordInsertSQLGen(Record record) {
		// 已有缓存 直接返回
		String sql = SQL_INSERT.get(record.getTableName());
		if (sql != null)
			return sql;

		// 拼写缓冲
		StringBuilder sqlInto = new StringBuilder(25 * COL_DEF_LEN);
		StringBuilder sqlValues = new StringBuilder(50);

		// 信息定义
		FieldTable fieldSet = record.getFieldTable();

		// 拼写SQL
		for (String name : fieldSet.getFieldNames()) {
			// 连接多个时 需要加入分隔符
			if (sqlInto.length() > 0) {
				sqlInto.append(",");
				sqlValues.append(",");
			}

			// 占位符
			sqlInto.append("`").append(name).append("`");
			sqlValues.append("?");
		}

		// 最终SQL
		sql = Utils.createStr(SQL_BASE_INSERT, record.getTableName(), sqlInto, sqlValues);

		// 缓存SQL
		SQL_INSERT.put(record.getTableName(), sql);

		// 日志
		if (LogCore.db.isDebugEnabled()) {
			LogCore.db.debug("[{}]生成新增SQL语句：table={}, sql={}", port.getId(), record.getTableName(), sql);
		}

		return sql;
	}

	/**
	 * 将record新增数据持久化到数据库 占位语句生成
	 * @param record
	 * @return <sqlInto: [属性列], sqlValues, [占位符], params: [参数]>
	 */
	private Object[] syncRecordInsertParamsGen(Record record) {
		List<Object> paramList = new ArrayList<>();

		// 信息定义
		FieldTable fieldSet = record.getFieldTable();

		// 实际值
		for (String name : fieldSet.getFieldNames()) {
			paramList.add(record.get(name));
		}

		// 参数拼装备数组 便于之后的操作
		Object[] params = new Object[paramList.size()];
		paramList.toArray(params);

		return params;
	}

	/**
	 * 将record更新数据持久化到数据库 set语句生成
	 * @param record
	 * @return {sql: sql语句, params: [参数1, 参数2]}
	 */
	private Map<String, Object> syncRecordUpdateSetGen(Record record) {
		StringBuilder sql = new StringBuilder(5 * COL_DEF_LEN);
		List<Object> paramList = new ArrayList<>();

		// 字段设置
		FieldTable fieldSet = FieldTable.get(record.getTableName());

		// 拼写SQL
		for (String name : record.getFieldModified()) {
			// 主键的更新忽略
			if (Record.PRIMARY_KEY_NAME.equals(name))
				continue;

			// 更新值
			Object val = record.get(name);

			// 验证字符串类型的长度是否符合
			Field f = fieldSet.getField(name);
			if (f.entityType == DBConsts.ENTITY_TYPE_STR) {
				String v = (String) val;
				if (f.columnLen < v.length()) {
					LogCore.db.error("[{}]刷新缓存时发现了超出长度的字段，忽略此字段的持久化：strLen={}, strContext={}, field={}, record={}",
							port.getId(), v.length(), v, f, record);
					continue;
				}
			}

			// 分隔符
			if (sql.length() > 0)
				sql.append(",");
			// 占位符
			sql.append("`").append(name).append("`=?");
			// 实际值
			paramList.add(val);
		}

		// 参数拼装备数组 便于之后的操作
		Object[] params = new Object[paramList.size()];
		paramList.toArray(params);

		return Utils.ofMap("sql", sql.toString(), "params", params);
	}

	@Override
	public void pulseOverride() {
		long now = port.getTimeCurrent();

		// 定时刷新缓存数据
		if (flushTimer.isPeriod(now)) {
			flushDatabase(now - Config.DB_CACHED_SYNC_SEC * Time.SEC);
		}

		// 发送执行队列
		pulseQueue();

		// 间隔输出执行数量
		pulseNum();
	}

	/**
	 * 间隔输出执行数量
	 */
	private void pulseNum() {
		long now = port.getTimeCurrent();
		if (!numTimer.isPeriod(now))
			return;

		LogCore.db.info("[{}]分配队列启动后统计：total={}, block={}, patch={}, query={}, exec={}, count={}", port.getId(),
				queue.getNumSQL(), queue.getNumBlock(), numPatch, numQuery, numExec, numCount);
	}

	/**
	 * 每帧检查可执行队列
	 */
	private void pulseQueue() {
		// 本分配队列负责的表名称
		Set<String> tables = queue.getTableNames();
		for (String t : tables) {
			QueueUnit u = queue.pop(t);
			while (u != null) {
				// 确认执行服务
				String linePortId = DBLineService.linePortId(t, u.getId());
				DBLineServiceProxy prx = DBLineServiceProxy.newInstance(port.getNodeId(), linePortId,
						Distr.SERV_DEFAULT);

				// 执行任务
				if (u.getType() == T_EXEC) {
					prx.execute(t, u.getVersion(), u.getListener() != null, u.getSql(), u.getParams());
					numExec++;

					// 统计任务
				} else if (u.getType() == T_COUNT) {
					prx.count(t, u.getVersion(), u.getSql(), u.getParams());
					numCount++;

					// 查询任务
				} else if (u.getType() == T_QUERY) {
					prx.query(t, u.getVersion(), u.getSql(), u.getParams());
					numQuery++;
				}

				// 日志
				if (LogCore.db.isDebugEnabled()) {
					LogCore.db.debug("[{}->{}][{},{},{},{}]执行SQL={}, 参数={}", port.getId(), linePortId, t,
							u.getVersion(), u.getType(), u.getStatus(), u.getSql(), Arrays.toString(u.getParams()));
				}

				// 需要监听返回值
				if (u.getListener() != null) {
					if (u.getListener().getParamCount() == 2) {
						prx.listenResult(u.getListener().<GofFunction2<Param, Param>> getFunc(), u.getContext());
					} else {
						prx.listenResult(u.getListener().<GofFunction3<Boolean, Param, Param>> getFunc(),
								u.getContext());
					}
				}

				// 进行后续处理
				u = queue.pop(t);
			}
		}
	}
}
