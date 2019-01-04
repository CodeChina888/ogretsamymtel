package game.worldsrv.shop;

import core.support.observer.MsgReceiver;
import game.msg.MsgShopExchange.CSOpenShop;
import game.msg.MsgShopExchange.CSShopBuy;
import game.msg.MsgShopExchange.CSShopRef;
import game.seam.msg.MsgParam;

public class ShopMsgHandler {

	/**
	 * 购买
	 * @param param
	 */
	@MsgReceiver(CSShopBuy.class)
	public void  onCSShopBuy(MsgParam param){
		CSShopBuy msg = param.getMsg();
		ShopManager.inst()._msg_CSShopBuy(param.getHumanObject(),msg.getSn(), msg.getItemSn(),Math.max(1,msg.getCount()) );
	}
	/**
	 * 刷新商店
	 * @param param
	 */
	@MsgReceiver(CSShopRef.class)
	public void onCSShopRef(MsgParam param){
		CSShopRef msg = param.getMsg();
		ShopManager.inst()._msg_CSShopRef(param.getHumanObject(), msg.getShopType());
	}
	/**
	 * 打开商店
	 * @param param
	 */
	@MsgReceiver(CSOpenShop.class)
	public void  CSOpenShop(MsgParam param){
		CSOpenShop msg = param.getMsg();
		ShopManager.inst()._msg_CSOpenShop(param.getHumanObject(), msg.getShopType());
	}
	
	
}
