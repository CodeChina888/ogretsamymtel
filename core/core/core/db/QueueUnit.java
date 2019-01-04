package core.db;

import core.db.QueueStatusKey;
import core.db.QueueTypeKey;
import core.db.QueueUnit;
import core.support.Param;
import core.support.function.CommonFunction;
import core.support.function.GofFunction2;
import core.support.function.GofFunction3;

public class QueueUnit {
	// 执行SQL
	private String sql;
	// 执行参数
	private Object[] params;

	// 执行任务调用接口
	private QueueTypeKey type;
	// 执行任务所属状态
	private QueueStatusKey status;
	// 执行版本号
	private long version;
	// 目标ID
	private long id;

	// 回调设置
	private CommonFunction listener;
	private Param context;

	/**
	 * 构造函数
	 * @param type
	 * @param status
	 * @param sql
	 * @param params
	 */
	public QueueUnit(QueueTypeKey type, QueueStatusKey status, String sql, Object[] params) {
		this.type = type;
		this.status = status;
		this.sql = sql;
		this.params = params;
	}

	/**
	 * 返回值监听
	 * @param listener
	 * @param ctx
	 */
	public QueueUnit listenResult(GofFunction2<Param, Param> listener, Object... ctx) {
		this.listener = new CommonFunction(listener, 2);
		this.context = new Param(ctx);

		return this;
	}

	/**
	 * 返回值监听
	 * @param listener
	 * @param ctx
	 */
	public QueueUnit listenResult(GofFunction3<Boolean, Param, Param> listener, Object... ctx) {
		this.listener = new CommonFunction(listener, 3);
		this.context = new Param(ctx);

		return this;
	}

	/**
	 * 设置执行版本号
	 * @param version
	 * @return
	 */
	public QueueUnit version(long version) {
		this.version = version;

		return this;
	}

	/**
	 * 设置语句影响ID
	 * @param id
	 * @return
	 */
	public QueueUnit id(long id) {
		this.id = id;

		return this;
	}

	public String getSql() {
		return sql;
	}

	public Object[] getParams() {
		return params;
	}

	public QueueStatusKey getStatus() {
		return status;
	}

	public QueueTypeKey getType() {
		return type;
	}

	public CommonFunction getListener() {
		return listener;
	}

	public Param getContext() {
		return context;
	}

	public long getVersion() {
		return version;
	}

	public long getId() {
		return id;
	}
}
