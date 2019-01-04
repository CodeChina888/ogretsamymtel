package game.worldsrv.vip;

/**
 * VIP特权购买类型
 */
public enum VipBuyTypeKey {
	TYPE_ACT(VipBuyType.ACT,1),//体力值购买次数
	TYPE_SINGLE_REP(VipBuyType.SINGLE_REP,2),//单人副本购买次数
//	TYPE_TEAM(VipBuyType.TEAM,InstanceConstants.INSTANCE_TYPE_TEAM_REP),//组队副本挑战次数
//	TYPE_TEAM_TICKET(VipBuyType.TEAM_TICKET,InstanceConstants.INSTANCE_TYPE_TEAM_TICKET),//组队副本-门票副本挑战次数
//	TYPE_TEAM_DRAGON_1(VipBuyType.TEAM_DRAGON_1,InstanceConstants.INSTANCE_TYPE_TEAM_DRAGON_1),//组队副-一条龙玩法1挑战次数
//	TYPE_TEAM_DRAGON_2(VipBuyType.TEAM_DRAGON_2,InstanceConstants.INSTANCE_TYPE_TEAM_DRAGON_2),//组队副-一条龙玩法2本挑战次数
//	TYPE_TEAM_DRAGON_3(VipBuyType.TEAM_DRAGON_3,InstanceConstants.INSTANCE_TYPE_TEAM_DRAGON_3),//组队副-一条龙玩法3本挑战次数
//	TYPE_TEAM_FACTION_PROTECTION(VipBuyType.TEAM_FACTION_PROTECTION,InstanceConstants.INSTANCE_TYPE_TEAM_FACTION_PROTECT),//组队副-帮派卫士
	TYPE_MONEY(VipBuyType.MONEY,9), //金币
	TYPE_ELITE_CLEAR(VipBuyType.ELITECOPY_CLEAR,10), //精英副本重置
	TYPE_STORE_VIP(VipBuyType.STOREREF_VIP,11), //特权商店刷新
	TYPE_STORE_ITEM(VipBuyType.STOREREF_ITEM,12) //道具商店刷新

	;
	
	private int type;
	private int subType;
	
	private VipBuyTypeKey(int type, int subType){
		this.type = type;
		this.subType = subType;
	}
	
	public int getType(){
		return this.type;
	}
	
	public int getSubType(){
		return this.subType;
	}
	
	public static VipBuyTypeKey getEnumByType(int type) {
		for(VipBuyTypeKey k : values()) {
			if(k.type == type)
				return k;	
		}
		return null;
	}
	
	public static VipBuyTypeKey getEnumBySubType(int subType) {
		for(VipBuyTypeKey k : values()) {
			if(k.subType == subType)
				return k;	
		}
		return null;
	}
	
}
