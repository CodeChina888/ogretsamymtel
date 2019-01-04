package game.worldsrv.recharge;

import core.support.ManagerBase;
import core.support.observer.MsgReceiver;
import game.msg.MsgVip.CSTimeLimitRecharge;
import game.msg.MsgVip;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;
import game.worldsrv.rank.RankManager;

public class ActivityTimeRechargeMsgHandler {

    /**
     *领取限时个人充值奖励
     */
    @MsgReceiver(MsgVip.CSTimeLimitRecharge.class)
    public void onCSTimeLimitRecharge(MsgParam param){
        HumanObject humanObj = param.getHumanObject();
        CSTimeLimitRecharge msg = param.getMsg();
        int aid = msg.getAid();
        ActivityTimeRechargeManager.inst().getDraw(humanObj,aid);
    }
}
