package game.worldsrv.enumType;

// 待办事件
public enum BacklogType {
	
	// 好友
	FriendAddApply,	//添加好友申请
	FriendAdd,		//添加好友
	FriendRemove,	//删除好友
	
	Mail, //邮件
	
	// 聊天之私聊
	InformPrivateChat, 
	
	// PAY充值
	Pay,		// 充值
		
	// GM
	GMSilence,//禁言
	GMGS,  //设置gs
	GMFuli, //设置福利号
	GMCharge, //充值补发
	GMGSCharge, //gs充值
	
	// 爬塔
	TowerReset, // 每日重置 
	GuildLeave, //离开工会
	GuildJoin, //加入工会
	GuildCDR,//职位变更
	;
}
