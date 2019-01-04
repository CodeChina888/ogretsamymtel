package game.worldsrv.friend;

public class FriendType {
	public final static int Error = -1;//异常类型
	// 好友名单序列
	public final static int Apply = 1;// 申请
	public final static int Friend = 2;// 好友
	// 非好友名单序列
	public final static int Black = 3;// 黑名单
	public final static int Remove = 4;// 删除
	public final static int RemoveBlack = 5;// 删除黑名单
	// 以下类型禁止加入newFriend-type中
	public final static int Give = 10;// 赠送
	public final static int Gived = 15;// 被赠送体力
	public final static int Get = 11;// 领取
	public final static int Refuse = 12;// 拒绝
	public final static int Accept = 13;// 同意
	public final static int Blacked = 16;//被黑名单
}
