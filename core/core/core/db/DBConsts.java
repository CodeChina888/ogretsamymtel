package core.db;

public class DBConsts {
	// 实体字段类型
	public static final int ENTITY_TYPE_INT = 1;
	public static final int ENTITY_TYPE_LONG = 2;
	public static final int ENTITY_TYPE_DOUBLE = 3;
	public static final int ENTITY_TYPE_FLOAT = 4;
	public static final int ENTITY_TYPE_STR = 5;
	public static final int ENTITY_TYPE_BYTES = 6;

	// 数据的状态
	public static final int RECORD_STATUS_NEW = 1; // 新增
	public static final int RECORD_STATUS_NONE = 2; // 未被修改过
	public static final int RECORD_STATUS_MODIFIED = 3; // 被修改过
	public static final int RECORD_STATUS_DELETED = 4; // 已删除
}