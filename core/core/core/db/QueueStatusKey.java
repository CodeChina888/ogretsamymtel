package core.db;

/**
 * 执行队列状态关键字 决定了语句间的共存互斥关系
 */
public enum QueueStatusKey {
	S_NONE, // 无状态 可以与任何状态共存
	S_EXEC, // 执行类 与QUERY互斥
	S_QUERY // 同步查询类 与EXEC互斥
}
