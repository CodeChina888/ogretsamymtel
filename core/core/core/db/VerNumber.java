package core.db;

public class VerNumber {
	// 所属表
	public final String tableName;
	// 所属port
	public final String linePortId;

	// 刷新中的版本号
	public long flush;
	// 完成版本号
	public long finish;

	/**
	 * 构造函数
	 * @param tableName
	 * @param linePortId
	 */
	public VerNumber(String tableName, String linePortId) {
		this.tableName = tableName;
		this.linePortId = linePortId;
	}

	/**
	 * 任务都已完毕
	 * @return
	 */
	public boolean isDone() {
		return finish >= flush;
	}
}
