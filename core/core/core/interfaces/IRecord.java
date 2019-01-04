package core.interfaces;

import core.Chunk;

public interface IRecord {
	/**
	 * 应用Patch升级包
	 * @param chunk
	 */
	public void patchUpdate(Chunk chunk, boolean debug);

	/**
	 * 获取表名
	 * @return
	 */
	public String getTableName();

	/**
	 * 获取数据
	 * @param name
	 * @return
	 */
	public <T> T get(String name);

	/**
	 * 设置新数据
	 * @param name
	 * @param value
	 */
	public void set(String name, Object value);
}