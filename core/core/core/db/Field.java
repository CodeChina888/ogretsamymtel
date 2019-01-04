package core.db;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class Field implements ISerilizable {
	public String name; // 字段名称 实体与数据库相同
	public int columnType; // 数据库字段类型
	public int columnLen; // 数据库字段长度
	public int entityType; // 实体字段类型

	public void writeTo(OutputStream stream) throws IOException {
		stream.write(name);
		stream.write(columnType);
		stream.write(columnLen);
		stream.write(entityType);
	}

	public void readFrom(InputStream stream) throws IOException {
		name = stream.read();
		columnType = stream.read();
		columnLen = stream.read();
		entityType = stream.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", name).append("columnType", columnType)
				.append("columnLen", columnLen).append("entityType", entityType).toString();
	}
}
