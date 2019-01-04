package core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;

import core.support.Config;
import core.support.SysException;
import core.support.log.LogCore;

/**
 * 数据库连接对象 由于架构单线程无并发的特点 不使用连接池 保持一个连接即可
 */
public class DBConnection {
	private Logger log = LogCore.db;

	private String dbUrl; // 数据库连接URL
	private String dbUser; // 用户名
	private String dbPwd; // 密码

	/**
	 * 构造函数
	 */
	public DBConnection(String dbUrl, String dbUser, String dbPwd) {
		try {
			this.dbUrl = dbUrl;
			this.dbUser = dbUser;
			this.dbPwd = dbPwd;

			Class.forName("com.mysql.jdbc.Driver");// 加载mysql驱动必须用Class.forName来加载，否则报错
		} catch (ClassNotFoundException e) {
			throw new SysException(e);
		}
	}

	/**
	 * 获取PrepareStatement对象
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return getConnection().prepareStatement(sql);
	}

	/**
	 * 获取Statement对象
	 * @return
	 * @throws SQLException
	 */
	public Statement createStatement() throws SQLException {
		return getConnection().createStatement();
	}

	/**
	 * 关闭
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (CONN == null)
			return;

		CONN.close();
	}

	/**
	 * 获取数据库连接
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		if (CONN == null || !CONN.isValid(0)) {
			CONN = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
			CONN.prepareStatement("set names utf8mb4").executeQuery();
			// 记录日志
			log.info("创建新的数据库连接。");
		}

		return CONN;
	}
	private Connection CONN = null; // 保持连接 不要直接使用这个属性来获取连接
}
