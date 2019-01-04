package core.db;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import core.InputStream;
import core.OutputStream;
import core.db.DBConsts;
import core.db.Field;
import core.db.FieldSet;
import core.interfaces.ISerilizable;
import core.support.SysException;

public class FieldSet implements ISerilizable {
	// 全局缓存 每个NODE节点都用这个信息数据<实体名, 数据>
	private static final Map<String, FieldSet> CACHE = new HashMap<>();

	// 各字段信息
	private final Map<String, Field> fields = new LinkedHashMap<>();

	/**
	 * 获取一个FieldSet对象 全局函数
	 * @param name
	 * @return
	 */
	public static FieldSet get(String name) {
		return CACHE.get(name);
	}

	/**
	 * 新增一个FieldS对象 全局函数
	 * @param name
	 * @param fieldSet
	 * @return
	 */
	public static void put(String name, FieldSet fieldSet) {
		CACHE.put(name, fieldSet);
	}

	/**
	 * 返回Cache，只有DB的return调用
	 */
	public static Map<String, FieldSet> getAllCache() {
		return CACHE;
	}

	/**
	 * 默认构造函数
	 */
	public FieldSet() {
	}

	/**
	 * 构造函数
	 * @param metaData
	 */
	public FieldSet(ResultSetMetaData metaData) {
		try {
			init(metaData);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 初始化
	 * @param meta
	 * @throws SQLException
	 */
	private void init(ResultSetMetaData meta) throws SQLException {

		for (int i = 1; i <= meta.getColumnCount(); i++) {
			Field f = new Field();
			f.name = meta.getColumnName(i); // 设置名称
			f.columnType = meta.getColumnType(i); // 设置数据库字段类型
			f.columnLen = meta.getColumnDisplaySize(i); // 设置数据库字段长度

			// 设置实体字段类型
			switch (f.columnType) {
				case Types.INTEGER :
				case Types.BIT :
				case Types.BOOLEAN :
				case Types.TINYINT :
				case Types.SMALLINT :
				case Types.CHAR : {
					f.entityType = DBConsts.ENTITY_TYPE_INT;
					break;
				}

				case Types.BIGINT : {
					f.entityType = DBConsts.ENTITY_TYPE_LONG;
					break;
				}

				case Types.DECIMAL :
				case Types.FLOAT :
				case Types.DOUBLE : {
					f.entityType = DBConsts.ENTITY_TYPE_DOUBLE;
					break;
				}
				case Types.REAL : {
					f.entityType = DBConsts.ENTITY_TYPE_FLOAT;
					break;
				}

				case Types.VARBINARY :
				case Types.LONGVARBINARY :
				case Types.BLOB : {
					f.entityType = DBConsts.ENTITY_TYPE_BYTES;
					break;
				}
				
				case Types.LONGNVARCHAR :
				case Types.LONGVARCHAR :
				case Types.VARCHAR : {
					f.entityType = DBConsts.ENTITY_TYPE_STR;
					break;
				}

				default : {
					throw new SysException("通过数据库字段类型推断实体字段类型时，发现未知数据库字段类型：{}", f.columnType);
				}
			}

			// 记录
			fields.put(f.name, f);
		}
	}

	/**
	 * 字段数量
	 * @return
	 */
	public int size() {
		return fields.size();
	}

	/**
	 * 获取具体字段信息
	 * @param name
	 * @return
	 */
	public Field getField(String name) {
		return fields.get(name);
	}

	/**
	 * 获取所有字段信息
	 * @return
	 */
	public List<Field> getFields() {
		return new ArrayList<>(fields.values());
	}

	/**
	 * 返回Entry
	 * @return
	 */
	public Set<String> getFieldNames() {
		return fields.keySet();
	}

	/**
	 * 返回Entry
	 * @return
	 */
	public Set<Entry<String, Field>> entrySet() {
		return fields.entrySet();
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(fields);
	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		fields.clear();
		fields.putAll(stream.<Map<String, Field>> read());
	}
}
