package core;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.Chunk;
import core.InputStream;
import core.OutputStream;
import core.Record;
import core.db.DBConsts;
import core.db.Field;
import core.db.FieldTable;
import core.interfaces.IRecord;
import core.interfaces.ISerilizable;
import core.support.SysException;
import core.support.Utils;
import core.support.log.LogCore;

public class Record implements ISerilizable, IRecord {
	public static final String PRIMARY_KEY_NAME = "id"; // 主键名称

	private String tableName; // 表名
	private final Map<String, Object> values = new HashMap<>(); // 字段当前实际数据
	private final Set<String> fieldModified = new HashSet<>(); // 修改过的字段

	// 数据当前状态 1新增(默认值) 2未修改 3有修改 4已删除
	private int status = DBConsts.RECORD_STATUS_NEW;
	// !此属性不参与串行化 是否为一个刚刚生成的数据（新建或串行化）用来做自动提交逻辑的一种特殊情况判断
	private boolean newness = true;

	// 写缓存创建时间
	private long writeCacheCreateTime;

	public Record() {

	}

	public Record(String tableName, ResultSet rs) {
		try {
			// 设置状态
			status = DBConsts.RECORD_STATUS_NONE;

			// 表名称
			this.tableName = tableName;

			// 数据集信息
			ResultSetMetaData meta = rs.getMetaData();
			// 字段定义信息
			FieldTable fs = getFieldTable();

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
	 * 创建一个新数据项
	 * @param tableName
	 */
	public static Record newInstance(String tableName) {
		Record r = new Record();

		// 设置为新增状态
		r.status = DBConsts.RECORD_STATUS_NEW;

		// 表名称
		r.tableName = tableName;

		// 字段定义信息
		FieldTable fs = r.getFieldTable();
		if (fs != null) {
			// 初始化字段及默认值
			for (Entry<String, Field> e : fs.entrySet()) {
				String name = e.getKey();
				Field field = e.getValue();
	
				if (field.entityType == DBConsts.ENTITY_TYPE_INT) {
					r.values.put(name, 0);
				} else if (field.entityType == DBConsts.ENTITY_TYPE_LONG) {
					r.values.put(name, 0L);
				} else if (field.entityType == DBConsts.ENTITY_TYPE_DOUBLE) {
					r.values.put(name, 0.0D);
				} else if (field.entityType == DBConsts.ENTITY_TYPE_FLOAT) {
					r.values.put(name, 0.0F);
				} else if (field.entityType == DBConsts.ENTITY_TYPE_STR) {
					r.values.put(name, null);
				} else if (field.entityType == DBConsts.ENTITY_TYPE_BYTES) {
					r.values.put(name, null);
				}
			}
		}

		return r;
	}

	/**
	 * 获取数据
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		T val = (T) values.get(name);

		// 为了配合protobuf不能设置null值的特性 这里进行特殊处理
		FieldTable fs = getFieldTable();
		if (fs != null) {
			// 字段数据
			Field field = fs.getField(name);
			// 验证是否存在
			// if (field == null &&
			// !name.equals(DBService.CACHE_WRITE_CREATE_TIME_KEY)) {
			// throw new SysException("发现实体与数据库不匹配，数据库缺少字段：table={}, column={}",
			// tableName, name);
			// }
			// 设置默认值
			if (field != null && field.entityType == DBConsts.ENTITY_TYPE_STR && val == null) {
				val = (T) "";
			}
		}

		return val;
	}

	/**
	 * 设置新数据
	 * @param name
	 * @param value
	 */
	public void set(String name, Object value) {
		// 记录新旧值
		Object valOld = values.get(name);
		Object valNew = value;

		// 未修改 无需改动
		if (Utils.isEquals(valOld, valNew))
			return;

		// 记录新值
		values.put(name, valNew);

		// 将数据设置为已修改
		setStatusToModified(name);
	}

	/**
	 * 数据是否为脏数据 与数据库不同步
	 * @return
	 */
	public boolean isDirty() {
		return status != DBConsts.RECORD_STATUS_NONE;
	}

	/**
	 * 数据在加载后是否被修改过
	 * @return
	 */
	public boolean isModified() {
		return status == DBConsts.RECORD_STATUS_MODIFIED;
	}

	/**
	 * 是否为新增数据
	 * @return
	 */
	public boolean isNew() {
		return status == DBConsts.RECORD_STATUS_NEW;
	}

	/**
	 * 是否为已删除数据
	 * @return
	 */
	public boolean isDeleted() {
		return status == DBConsts.RECORD_STATUS_DELETED;
	}

	/**
	 * 修改数据状态 1.将数据状态设置为已修改 2.记录修改的字段
	 * @param name
	 */
	private void setStatusToModified(String name) {
		// 修改数据状态为已修改
		if (status == DBConsts.RECORD_STATUS_NONE) {
			status = DBConsts.RECORD_STATUS_MODIFIED;
		}

		// 只有当修改状态下 设置修改字段才有意义
		if (status == DBConsts.RECORD_STATUS_MODIFIED) {
			fieldModified.add(name);
		}
	}

	/**
	 * 获取数据信息
	 * @return
	 */
	public FieldTable getFieldTable() {
		return FieldTable.get(tableName);
	}

	/**
	 * 生成update升级包 只包括被修改过的数据
	 * 返回的Chunk需要使用BufferPool.deallocate(chunk.buffer)来回收缓冲池
	 * @return
	 */
	public Chunk pathUpdateGen() {
		// 未修改过
		if (!isDirty()) {
			return null;
		}

		// 新数据也不能生成升级包
		if (this.status == DBConsts.RECORD_STATUS_NEW) {
			return null;
		}

		OutputStream out = new OutputStream();
		out.write(fieldModified.size());
		for (String name : fieldModified) {
			out.write(name);
			out.write(values.get(name));
		}

		return out.getChunk();
	}

	/**
	 * 应用Patch升级包
	 */
	public void patchUpdate(Chunk chunk, boolean debug) {
		// Debug详细信息
		Map<String, Object> patch = null;
		if (debug) {
			patch = new HashMap<>();
		}

		// 转换为输入流
		InputStream in = new InputStream(chunk);

		// 改动条数
		int changedFieldNum = in.read();
		for (int i = 0; i < changedFieldNum; i++) {
			String name = in.read();
			Object val = in.read();

			// 修改
			set(name, val);

			// 将数据设置为已修改
			// 在无法知道原值的情况下，无法利用上面的set方法自动记录被修改的字段，必须手动再调用一次setStatusToModified()。
			// 例如：当执行update的数据时非缓存数据，那么会自动生成一个空的Record（所有属性都是默认值0、false、null等）
			// 由于无法知道原值，上面的set函数无法判断是否与旧值相同，会造成更新丢失。（如果新值也恰好是0、false、null的话）
			setStatusToModified(name);

			// 记录Debug详细信息
			if (debug) {
				patch.put(name, val);
			}
		}

		// 日志
		if (debug) {
			LogCore.db.debug("接收到更新包:{}(id={})({})", tableName, get(PRIMARY_KEY_NAME), patch);
		}
	}

	/**
	 * 清理数据为初始状态 一般是当数据新增或修改请求完成后 用来重置数据当前状态为最新值
	 */
	public void resetStatus() {
		status = DBConsts.RECORD_STATUS_NONE;
		fieldModified.clear();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(tableName);
		out.write(values);
		out.write(fieldModified);
		out.write(status);
		out.write(writeCacheCreateTime);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		tableName = in.read();

		values.clear();
		values.putAll(in.<Map<String, Object>> read());

		fieldModified.clear();
		fieldModified.addAll(in.<Set<String>> read());

		status = in.read();
		writeCacheCreateTime = in.read();
	}

	public String getTableName() {
		return tableName;
	}

	public Set<String> getFieldModified() {
		return fieldModified;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isNewness() {
		return newness;
	}

	public void setNewness(boolean newness) {
		this.newness = newness;
	}

	public long getWriteCacheCreateTime() {
		return writeCacheCreateTime;
	}

	public void setWriteCacheCreateTime(long writeCacheCreateTime) {
		this.writeCacheCreateTime = writeCacheCreateTime;
	}

	/**
	 * 生成当前数据表
	 */
	public Chunk pathAllGen() {
		
		OutputStream out = new OutputStream();
		out.write(values.size());
		for(Entry<String, Object> entry : values.entrySet()) {
			out.write(entry.getKey());
			out.write(entry.getValue());
		}
		
		return out.getChunk();
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
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("tableName", getTableName())
				.append("id", this.<Long> get("id")).toString();
	}
}