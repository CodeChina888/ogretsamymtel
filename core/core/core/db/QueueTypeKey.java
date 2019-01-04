package core.db;

/**
 * 执行队列调用接口关键字 决定了语句调用的执行接口API
 */
public enum QueueTypeKey {
	T_EXEC, // 执行接口
	T_QUERY, // 查询接口
	T_COUNT // 数量接口
}
