package game.worldsrv.recharge;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define;
import game.msg.Define.EAwardType;
import game.msg.MsgVip.SCTimeLimitRecharge;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitDataPersistance;
import game.worldsrv.config.ConfActivity;
import game.worldsrv.config.ConfActivityTimeLimitRecharge;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ActivityHumanData;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.*;

public class ActivityTimeRechargeManager  extends ManagerBase {

    public static int special_Id = 24;
    /**
     * 获取实例
     * @return
     */
    public static ActivityTimeRechargeManager inst() {
        return inst(ActivityTimeRechargeManager.class);
    }
    /**
     * 创角初始化
     */
    @Listener(EventKey.HumanCreate)
    public void initData(Param param){
        Human human = param.get("human");
       
        Collection<ConfActivityTimeLimitRecharge> conf_collection = ConfActivityTimeLimitRecharge.findAll();
        for(ConfActivityTimeLimitRecharge conf : conf_collection){
        	ActivityHumanData data = new ActivityHumanData();
            int status =  Define.EAwardType.AwardNot_VALUE;
            data.setAid(conf.sn);
            data.setId(Port.applyId());
            data.setHumanId(human.getId());
            data.setNumValue(0);
            data.setStrValue(String.valueOf(status));
            data.setActivityId(special_Id);
            data.persist();
        }
    }

    /**
     * 登陆加载信息
     */
    @Listener(EventKey.HumanDataLoadOther)
    public void _listener_HumanDataLoadOther(Param param) {
        HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
        if (humanObj == null) {
            Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
            return;
        }
        DB dbPrx = DB.newInstance(ActivityHumanData.tableName);
        dbPrx.findBy(false, ActivityHumanData.K.HumanId, humanObj.id,ActivityHumanData.K.ActivityId,special_Id);
        dbPrx.listenResult(this::_result_loadHumanPartner, "humanObj", humanObj);
        // 玩家数据加载开始一个
        Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
    }
    private void _result_loadHumanPartner(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
        if (humanObj == null) {
            Log.game.error("===_result_loadHumanPartner humanObj is null");
            return;
        }

        List<Record> records = results.get();
        if (records == null) {
            Log.game.error("===_result_loadHumanPartner record is null");
            // 数据错误，加载为空，不能中断流程，也要发送数据加载完毕事件
            Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
            return;
        }
        Map<Integer,ActivityHumanData> LimitrechargeMap = humanObj.limitrechargeMap;
        for (Record record : records) {
            if (record == null) {
                continue;
            }
            ActivityHumanData activity_data = new ActivityHumanData(record);
            LimitrechargeMap.put(activity_data.getAid(),activity_data);
        }
        sendMsg(humanObj,false);
        // 玩家数据加载完成一个
        Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
    }

    /**
     * 当前等级能显示的最大的AID
     * @param lv
     * @return
     */
    private int findMaxAidByLevel(int lv){
        int aid = 0;
        Collection <ConfActivityTimeLimitRecharge> conf_collection = ConfActivityTimeLimitRecharge.findAll();
        for(ConfActivityTimeLimitRecharge conf:conf_collection){
            if(lv >= conf.minTLv){
                aid = conf.sn;
            }
        }

        return aid;
    }

    /**
     * 升级事件发生
     * 开始倒计时
     */
    @Listener(EventKey.HumanLvUp)
    public void startCountDown(Param param){
        HumanObject humanObj = param.get("humanObj");
        int aid = findMaxAidByLevel(humanObj.getHuman().getLevel());
        if (aid == 0) {
            return;
        }
        ActivityHumanData data = humanObj.limitrechargeMap.get(aid);
        if(data != null) {
        	data.setNumValue(Port.getTime());
            sendMsg(humanObj,false);
        }
        
    }

    /**
     * 
     * @param humanObj
     * @param isGet 是否是领取的情况
     *  如果是领取的情况都要下发
     */
    public void sendMsg(HumanObject humanObj,boolean isGet){
    	int aid = findMaxAidByLevel(humanObj.getHuman().getLevel());
        if (aid == 0) {
            return;
        }
        ActivityHumanData data = humanObj.limitrechargeMap.get(aid);
        if(data == null) {
        	Log.activity.error("该玩家没有活动数据 name={},aid={}",humanObj.getHuman().getName(),aid);
        	return;
        }
        SCTimeLimitRecharge.Builder msg = SCTimeLimitRecharge.newBuilder();
        ConfActivityTimeLimitRecharge conf = ConfActivityTimeLimitRecharge.get(aid);
        if(conf == null) {
        	Log.activity.error("ConfActivityTimeLimitRecharge error sn={}",aid);
        	return;
        }
        msg.setAid(aid);
        msg.setLvl(conf.minTLv);
        msg.setMoney(conf.needRecharge);
        long deadLine = conf.countdown*1000+data.getNumValue();
        if(deadLine -Port.getTime()<0) {
        	//过期
        	Log.activity.debug("过期");
        	return;
        }
        int status = Integer.parseInt(data.getStrValue());
        msg.setRewardSn(conf.rewardSn); 
        if(status == EAwardType.Awarded_VALUE && !isGet){
        	//已经领取不下发
        	Log.activity.debug("已经领取");
        	return;
        }
        msg.setStatus(EAwardType.valueOf(status));
        msg.setLeaveTime(deadLine);
        humanObj.sendMsg(msg);
    }
    /**
     * 充值事件发生
     * 更改领取状态
     */

    @Listener(EventKey.PayNotify)
    public void payEevent(Param param){
        HumanObject humanObj = param.get("humanObj");
        //充值金额
        long gold = param.get("gold");//玩家充值的金额
        //是否在最规定时间内
        int aid = findMaxAidByLevel(humanObj.getHuman().getLevel());
        if (aid == 0) {
            return;
        }

        ConfActivityTimeLimitRecharge conf = ConfActivityTimeLimitRecharge.get(aid);
        if(conf == null){
            Log.table.error("ConfActivityTimeLimitRecharge error can't find sn = {}",aid);
            return;
        }
        if(gold < conf.needRecharge){
            Log.activity.debug("充值金额不足");
            return;
        }
        ActivityHumanData data = humanObj.limitrechargeMap.get(aid);
        long leaveTime = conf.countdown*1000 - (Port.getTime()-data.getNumValue());
        if(leaveTime <= 0){
            //在规定时间内完成充值
            Log.activity.debug("未在规定时间内完成充值");
            return;
        }
        //更改领取状态
        humanObj.limitrechargeMap.get(aid).setStrValue(String.valueOf(Define.EAwardType.Awarding_VALUE));
        sendMsg(humanObj,false);
    }


    /**
     * 领取奖励
     * @param humanObj
     * @param aid
     */
    public void getDraw(HumanObject humanObj,int aid){
        int humanLv= humanObj.getHuman().getLevel();

        //校验当前等级能显示的最大的AID
        int maxaid = findMaxAidByLevel(humanLv);
        if(maxaid != aid){
            Log.activity.error("允许领取的最大AID错误");
            return;
        }
        //倒计时是否结束
        ActivityHumanData data = humanObj.limitrechargeMap.get(aid);

        ConfActivityTimeLimitRecharge conf = ConfActivityTimeLimitRecharge.get(aid);
        if(conf == null){
            Log.table.error("ConfActivityTimeLimitRecharge error can't find sn = {}",aid);
        }

        long leaveTime = conf.countdown*1000 - (Port.getTime()-data.getNumValue());
        if(leaveTime <= 0){
            Log.activity.debug("倒计时已结束");
            return;
        }
        //状态是否正确
        int status = Integer.parseInt(humanObj.limitrechargeMap.get(aid).getStrValue());
        if(status != Define.EAwardType.Awarding_VALUE){
            Log.activity.debug("领取状态错误");
            return;
        }
        //发放奖品
        ConfRewards confRewards = ConfRewards.get(conf.rewardSn);
        if(confRewards == null){
            Log.table.error("ConfRewards error");
            return;
        }
        //变更状态
        humanObj.limitrechargeMap.get(aid).setStrValue(String.valueOf(Define.EAwardType.Awarded_VALUE));

        //发放奖励
        RewardHelper.reward(humanObj,confRewards.itemSn,confRewards.itemNum, LogSysModType.Activity);
        sendMsg(humanObj,true);
    }
}
