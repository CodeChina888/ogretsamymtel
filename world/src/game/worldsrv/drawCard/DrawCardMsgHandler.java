package game.worldsrv.drawCard;

import core.support.observer.MsgReceiver;
import game.msg.Define.EDrawOperation;
import game.msg.Define.EDrawType;
import game.msg.MsgCard.CSDrawCardMsg;
import game.msg.MsgCard.CSSummonScoreExchange;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.enumType.QuestDailyType;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class DrawCardMsgHandler {
	
	/**
	 * 抽卡
	 */
	@MsgReceiver(CSDrawCardMsg.class)
	public void onCSPartnerLineup(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSDrawCardMsg msg = param.getMsg();
		EDrawType type = msg.getType();
		EDrawOperation operation = msg.getOperation();
		boolean isFree =  msg.getIsFree();
		
		if(type == EDrawType.BySummonToken){//招募令抽卡
			
			if(isFree){
				
				DrawCardManager.inst().drawCardForFree_ZML(humanObj); //免费
			}else{
				
				DrawCardManager.inst().drawCard_ZML(humanObj,operation);//付费
			}
			
			//FIXME
//			Event.fire(EventKey.QuestUpdate, "humanObj", humanObj,"questType", QuestDailyType.RecruitGeneral,"num",operation);//普通招募
		
		}else if(type == EDrawType.ByGold){//元宝抽卡
			
			if(isFree){
				
				DrawCardManager.inst().drawCardForFree_Gold(humanObj);//免费元宝抽卡
			}else{
				
				DrawCardManager.inst().drawCard_Gold(humanObj,operation);//元宝付费
			}
			
			//FIXME
//			Event.fire(EventKey.QuestUpdate, "humanObj", humanObj,"questType", QuestDailyType.RecruitAdvanced,"num",operation);//高级招募
		
		}
	}
	
	/**
	 * 抽卡积分兑换
	 */
	@MsgReceiver(CSSummonScoreExchange.class)
	public void _msg_CSSummonScoreExchange(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSummonScoreExchange msg = param.getMsg();
		int index = msg.getIndex();
		int selectIndex = msg.getSelectIndex();
		DrawCardManager.inst()._msg_CSSummonScoreExchange(humanObj, index, selectIndex);
	}
	
	
}
