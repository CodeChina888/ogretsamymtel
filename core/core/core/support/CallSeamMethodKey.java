package core.support;

/**
 * 世界服务器与系统结合需要实现的几个函数
 */
public enum CallSeamMethodKey {
	ACCOUNT_MSG, // 登陆阶段消息
	WORLD_MSG, // 游戏阶段消息

	ACCOUNT_LOST, // 登陆阶段连接丢失
	WORLD_LOST, // 游戏阶段连接丢失

	ACCOUNT_CHECK, // 登陆阶段状态验证
	WORLD_CHECK; // 游戏阶段状态验证

}