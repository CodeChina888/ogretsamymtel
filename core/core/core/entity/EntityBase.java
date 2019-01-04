package core.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.InputStream;
import core.OutputStream;
import core.Record;
import core.db.DBConsts;
import core.entity.EntityBase;
import core.interfaces.ISerilizable;

public abstract class EntityBase implements ISerilizable, Comparable<EntityBase> {
	// 持久化数据
	protected Record record;

	// 暂存数据
	private Map<String, Object> valueTrans = new HashMap<>();

	public abstract String getTableName();

	public abstract long getId();

	public abstract void persist();

	public abstract void update();

	public abstract void update(boolean sync);

	public abstract void remove();

	public EntityBase() {
		this.record = Record.newInstance(getTableName());
	}

	public EntityBase(Record record) {
		this.record = record;
	}

	/**
	 * 设置持久化数据
	 * @param record
	 */
	public void setRecord(Record record) {
		this.record = record;
	}

	public boolean isOldRecord() {
		boolean isOld = false;
		if (record != null && record.getStatus() != DBConsts.RECORD_STATUS_NEW)
			isOld = true;
		return isOld;
	}

	/**
	 * 获取临时数据
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValueTrans(String name) {
		return (T) valueTrans.get(name);
	}

	/**
	 * 设置临时数据
	 * @param name
	 * @param value
	 */
	protected void setValueTrans(String name, Object value) {
		valueTrans.put(name, value);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(record);
		out.write(valueTrans);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		record = in.read();
		valueTrans = in.read();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof EntityBase)) {
			return false;
		}

		EntityBase castOther = (EntityBase) other;
		return new EqualsBuilder().append(this.getClass(), castOther.getClass())
				.append(this.getId(), castOther.getId()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getClass()).append(getId()).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId()).toString();
	}

	@Override
	public int compareTo(EntityBase other) {
		return new CompareToBuilder().append(this.getId(), other.getId()).toComparison();
	}
}
