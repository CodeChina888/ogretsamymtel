package core.dbsrv;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Port;
import core.Record;
import core.RecordTransient;
import core.Service;
import core.db.DBConnection;
import core.db.VerNumber;
import core.db.VerTables;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.interfaces.IRecord;
import core.support.Distr;
import core.support.SysException;
import core.support.TickTimer;
import core.support.Time;
import core.support.Utils;
import core.support.log.LogCore;

/**
 * 数据库操作执行服务
 */
@DistrClass(importClass = {})
public class DBLineService extends Service {
	// 数据库连接
	private final DBConnection dbConn;

	// 队列阻塞检查
	private final TickTimer fixTimer = new TickTimer(10 * Time.SEC);
	// 队列阻塞检查版本号记录
	private final Map<VerNumber, Long> fixNo = new HashMap<>();

	// 统计执行命令输出间隔
	private final TickTimer numTimer = new TickTimer(29 * Time.MIN);
	// 统计执行命令数量
	private long numQuery; // 查询
	private long numExec; // 执行
	private long numCount; // 数量统计

	/**
	 * 构造函数
	 * @param port
	 */
	public DBLineService(Port port, String dbUrl, String dbUser, String dbPwd) {
		super(port);
		// 建立数据库连接
		dbConn = new DBConnection(dbUrl, dbUser, dbPwd);
	}

	@Override
	public Object getId() {
		return Distr.SERV_DEFAULT;
	}

	/**
	 * 确定分配执行线程的PortId
	 * @param tableName
	 * @param id
	 * @return
	 */
	public static String linePortId(String tableName, long id) {
		long hash = tableName.hashCode() + Math.max(id, 0);
		hash = Utils.hash((int) hash);

		if (hash < 0)
			hash = Math.abs(hash);

		return Distr.PORT_DB_LINE_PREFIX + (hash % Distr.PORT_STARTUP_NUM_DB_LINE);
	}

	/**
	 * 每帧处理
	 */
	@Override
	public void pulseOverride() {
		// 检查修复无法继续执行的队列
		pulseFix();
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

		LogCore.db.info("[{}]执行队列启动后统计：query={}, exec={}, count={}", port.getId(), numQuery, numExec, numCount);
	}

	/**
	 * 检查修复无法继续执行的队列 防止未知错误，造成队列阻塞，这里进行一下保底修复
	 */
	private void pulseFix() {
		long now = port.getTimeCurrent();
		if (!fixTimer.isPeriod(now))
			return;

		// 1 验证上次未完成的队列是否有进展
		// 未有进展的队列视为堵塞队列，强制疏通
		for (Entry<VerNumber, Long> e : fixNo.entrySet()) {
			VerNumber ver = e.getKey();
			Long finish = e.getValue();
			// 不相等 证明有进展 则不进行处理
			if (ver.finish != finish)
				continue;

			LogCore.db.error("[{}]发现了阻塞执行队列，强制进行疏通：{}", port.getId(), ver);

			// 对于10秒钟都没有进展的 进行强行疏通
			VerTables.finish(ver.tableName, ver.linePortId, ver.flush);
		}

		// 2 记录本次未完成的队列
		fixNo.clear();
		for (VerNumber v : VerTables.getUndone(port.getId())) {
			fixNo.put(v, v.finish);
		}
	}

	/**
	 * 执行数据查询语句 并返回结果
	 */
	@DistrMethod
	public void query(String verTable, long verNumber, String sql, Object... params) {
		// 读取数据
		try (PreparedStatement ps = dbConn.prepareStatement(sql)) {
			// 是否为全表查询
			boolean fullQuery = sql.toLowerCase().startsWith("select * ");

			// 设置?占位符参数
			int num = 1;
			for (Object val : params) {
				// 属性值 如果是Collection及子类则判定为in语句
				if (val instanceof Collection) {
					Collection<?> vals = (Collection<?>) val;
					// 设置in参数
					for (Object v : vals) {
						ps.setObject(num, v);
						num++;
					}
					// 默认为相等
				} else {
					ps.setObject(num, val);
					num++;
				}
			}

			// 查询结果 进行拼装
			List<Object> results = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				ResultSetMetaData meta = rs.getMetaData();
				// 表名 因为是单表，所以第一个字段所属表就是表名
				String tableName = meta.getTableName(1);

				// 封装结果
				while (rs.next()) {
					// 如果是全表查询 封装为有状态 反之为无状态
					IRecord data = fullQuery ? new Record(tableName, rs) : new RecordTransient(tableName, rs);
					// 加入返回值
					results.add(data);
				}
			}

			// 返回结果
			port.returnsImmutable(results);
		} catch (Exception e) {
			throw new SysException(e, "[{}]执行SQL={}, 参数={}", port.getId(), sql, Arrays.toString(params));
		} finally {
			// 设置已查询版本号
			VerTables.finish(verTable, port.getId(), verNumber);
			// 执行数量记录增加
			numQuery++;

			// 日志
			if (LogCore.db.isDebugEnabled()) {
				LogCore.db.debug("[{}][{}]执行SQL={}, 参数={}", port.getId(), verNumber, sql, Arrays.toString(params));
			}
		}
	}

	/**
	 * 执行数量查询语句 并返回结果
	 */
	@DistrMethod
	public void count(String verTable, long verNumber, String sql, Object... params) {
		// 读取数据
		try (PreparedStatement ps = dbConn.prepareStatement(sql)) {
			// 设置?占位符参数
			int num = 1;
			for (Object val : params) {
				// 属性值 如果是Collection及子类则判定为in语句
				if (val instanceof Collection) {
					Collection<?> vals = (Collection<?>) val;
					// 设置in参数
					for (Object v : vals) {
						ps.setObject(num, v);
						num++;
					}
				} else { // 默认为相等
					ps.setObject(num, val);
					num++;
				}
			}

			// 查询结果
			int result = 0;
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				result = (int) rs.getLong(1);
			}

			// 返回结果
			port.returnsImmutable(result);
		} catch (Exception e) {
			throw new SysException(e, "[{}]执行SQL={}, 参数={}", port.getId(), sql, Arrays.toString(params));
		} finally {
			// 设置已查询版本号
			VerTables.finish(verTable, port.getId(), verNumber);
			// 执行数量记录增加
			numCount++;

			// 日志
			if (LogCore.db.isDebugEnabled()) {
				LogCore.db.debug("[{}][{}]执行SQL={}, 参数={}", port.getId(), verNumber, sql, Arrays.toString(params));
			}

		}
	}

	/**
	 * 执行SQL语句，支持?占位符 一般用来执行update/insert/delete语句 及一些针对多行数据的批量操作
	 * @param sql
	 * @param needResult 是否需要返回值通知调用者
	 * @param params
	 */
	@DistrMethod
	public void execute(String verTable, long verNumber, boolean needResult, String sql, Object... params) {
		// 主体逻辑
		try (PreparedStatement ps = dbConn.prepareStatement(sql)) {
			// 设置参数
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}

			// 执行
			int result = ps.executeUpdate();

			// 返回执行完毕通知
			if (needResult) {
				port.returnsImmutable("success", true, "count", result);
			}
		} catch (Exception e) {
			// 如果需要返回值 则在错误情况下也进行通知
			if (needResult) {
				port.returnsImmutable("success", false, "count", 0);
			}

			throw new SysException(e, "[{}]执行SQL={}, 参数={}", port.getId(), sql, Arrays.toString(params));
		} finally {
			// 设置已执行版本号
			VerTables.finish(verTable, port.getId(), verNumber);
			// 执行数量记录增加
			numExec++;

			// 日志
			if (LogCore.db.isDebugEnabled()) {
				LogCore.db.debug("[{}][{}]执行SQL={}，参数={}", port.getId(), verNumber, sql, Arrays.toString(params));
			}
		}
	}
}
