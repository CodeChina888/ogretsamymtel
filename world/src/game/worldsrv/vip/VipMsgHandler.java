package game.worldsrv.vip;

import core.support.observer.MsgReceiver;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.msg.MsgVip.CSVIPBuy;
import game.msg.MsgVip.CSVIPBuyInfo;
import game.msg.MsgVip.CSVIPBuyGift;
import game.msg.MsgVip.CSVIPGetGift;
import game.msg.MsgVip.CSVIPFirstChargeReward;
import game.msg.MsgVip.CSTimeLimitRecharge;
/**
 * VIP相关
 */
public class VipMsgHandler {
	
	/**
	 * VIP特权购买-购买次数
	 * @param param
	 */
	@MsgReceiver(CSVIPBuyInfo.class)
	public void onVIPBuyInfo(MsgParam param){
		
		HumanObject humanObj = param.getHumanObject();
		
		VipManager.inst().initVipbuyTimes(humanObj);
	}
	
	/**
	 * 购买VIP特权道具(已废弃)
	 * @param param
	 */
	@MsgReceiver(CSVIPBuyGift.class)
	public void onVIPBuyGift(MsgParam param){
//		return;
		HumanObject humanObj = param.getHumanObject();
		CSVIPBuyGift msg = param.getMsg();
		
		VipManager.inst().buyVipGift(humanObj, msg.getVipLevel());
	}
	
	/**
	 * 客户端领取vip特殊礼品
	 * @param param
	 */
	@MsgReceiver(CSVIPGetGift.class)
	public void onVIPGetGift(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSVIPBuyGift msg = param.getMsg();
		
		VipManager.inst().getGift(humanObj, msg.getVipLevel());
	}
	
	/**
	 * 领取首充奖励
	 * @param param
	 */
	@MsgReceiver(CSVIPFirstChargeReward.class)
	public void onVIPFirstChargeReward(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
	
		
		VipManager.inst().firstChargeReward(humanObj);
	}


}
