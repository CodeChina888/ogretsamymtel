package core;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

import core.Chunk;
import core.InputStream;
import core.OutputStream;
import core.db.DBConsts;
import core.db.Field;
import core.db.FieldTable;
import core.interfaces.IRecord;
import core.interfaces.ISerilizable;
import core.support.SysException;

public class RecordTransient implements ISerilizable, IRecord {
	private String tableName; // 表名
	private final Map<String, Object> values = new HashMap<>(); // 字段数据

	public RecordTransient() {
	}

	public RecordTransient(String tableName, ResultSet rs) {
		try {
			// 表名称
			this.tableName = tableName;

			// 数据集信息
			ResultSetMetaData meta = rs.getMetaData();
			// 字段定义信息
			FieldTable fs = FieldTable.get(tableName);

			// 初始化字段
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				String name = meta.getColumnName(i);
				Field field = fs.getField(name);

				if (field.entityType == DBConsts.ENTITY_TYPE_INT) {
					values.put(name, rs.getInt(name));
				} else if (field.entityType == DBConsts.ENTITY_TYPE_LONG) {
					values.put(name, rs.getLong(name));
				} else if (field.entityType == DBConsts.ENTITY_TYPE_DOUBLE) {
					values.put(name, rs.getDouble(name));
				} else if (field.entityType == DBConsts.ENTITY_TYPE_FLOAT) {
					values.put(name, rs.getFloat(name));
				} else if (field.entityType == DBConsts.ENTITY_TYPE_STR) {
					values.put(name, rs.getString(name));
				} else if (field.entityType == DBConsts.ENTITY_TYPE_BYTES) {
					values.put(name, rs.getBytes(name));
				}
			}
		} catch (SQLException e) {
			throw new SysException(e);
		}
	}

	/**
	 * 获取表名
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * 获取数据
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T) values.get(name);
	}

	/**
	 * 设置新数据
	 * @param name
	 * @param value
	 */
	public void set(String name, Object value) {
		// 记录新值
		values.put(name, value);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(tableName);
		out.write(values);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		tableName = in.read();

		values.clear();
		values.putAll(in.read());
	}

	/**
	 * 应用Patch升级包
	 * @param chunk
	 */
	public void patchUpdate(Chunk chunk, boolean debug) {
		// 转换为输入流
		InputStream in = new InputStream(chunk);

		// 改动条数
		int changedFieldNum = in.read();
		for (int i = 0; i < changedFieldNum; i++) {
			String name = in.read();
			Object val = in.read();

			// 修改
			set(name, val);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof IRecord)) {
			return false;
		}

		IRecord castOther = (IRecord) other;
		return Objects.equals(this.getTableName(), castOther.getTableName())
				&& Objects.equals(this.get("id"), castOther.get("id"));
	}

	@Override
	public int hashCode() {
		return Objects.hash(getTableName(), get("id"));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("tableName", getTableName()).append("id", this.<Long> get("id"))
				.toString();
	}
}