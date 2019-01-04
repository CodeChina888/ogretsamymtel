package game.worldsrv.vip;

import org.apache.commons.lang3.StringUtils;

import core.Port;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Utils;
import core.support.observer.Listener;
//import game.worldsrv.support.Utils;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfVipUpgrade;
import game.worldsrv.config.ConfParam;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.Human;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.msg.Define.EMoneyType;
import game.msg.Define.EVipBuyType;
import game.msg.Define.DVipBuy;
import game.msg.MsgVip.SCVIPBuy;
import game.msg.MsgVip.SCVIPBuyGift;
import game.msg.MsgVip.SCVIPBuyInfo;
import game.msg.MsgVip.SCVIPFirstChargeReward;
import game.worldsrv.support.Log;
import game.worldsrv.support.ReasonResult;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


/**
 * VIP管理类
 */
public class VipManager extends ManagerBase {
	
	/**
	 * 获取实例
	 */
	public static VipManager inst() {
		return inst(VipManager.class);
	}
	
	/**
	 * 每个整点执行一次
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置
			humanObj.getHumanExtInfo().setVipBuyTimes("[]");
			initVipbuyTimes(humanObj);
			//PaymentManager.inst().updateVipBuyTimes(humanObj, humanObj.getHuman().getVipLevel(), 0);
		}
	}
	
	/**
	 * 登录成功
	 * @param param
	 */
	@Listener(EventKey.HumanLoginFinish)
	public void onHumanLoginFinish(Param param) {
		HumanObject humanObj = param.get("humanObj");
		//vip购买次数信息
        if (humanObj.isDailyFirstLogin) {
            humanObj.getHumanExtInfo().setVipBuyTimes("[]");
        }
		VipManager.inst().initVipbuyTimes(humanObj);
	}
	/**
	 * 初始化vip购买次数信息
	 * @param humanObj
	 */
	public void initVipbuyTimes(HumanObject humanObj){
		//获取玩家购买信息数据
		String str = humanObj.getHumanExtInfo().getVipBuyTimes();
		int firstChargeRewardState = humanObj.getHumanExtInfo().getFirstChargeRewardState();
		JSONArray  vipBuyTimes = Utils.toJSONArray(str);
		SCVIPBuyInfo.Builder msg = SCVIPBuyInfo.newBuilder();
		//vip购买次数信息
		if(vipBuyTimes.size()>0){
			for (int j = 0; j < vipBuyTimes.size(); j++) {
				DVipBuy.Builder val = DVipBuy.newBuilder();
				val.setNumber(vipBuyTimes.getJSONObject(j).getIntValue("buytimes"));
				val.setType(vipBuyTimes.getJSONObject(j).getIntValue("type"));
				msg.addInfo(val);
			}
		}	
		//vip领奖id

//		msg.setAwardid(humanObj.getHumanExtInfo().getVipAwardId());
		int[] vipBuyLvs = Utils.strToIntArray(humanObj.getHumanExtInfo().getVipBuyLvs());
		if (vipBuyLvs != null && vipBuyLvs.length > 0) {
			for (int i = 0; i < vipBuyLvs.length; i++) {
				msg.addVIPBuyLvs(vipBuyLvs[i]);
			}
		}
		msg.setFirstChargeRewardState(firstChargeRewardState);
//		msg.setAwardid(humanObj.getHumanExtInfo().getVipAwardId());


		//msg.setAwardid(humanObj.getHumanExtInfo().getVipAwardId());

		humanObj.sendMsg(msg);


	}
	
	/**
	 * 获取配置表中的vip各项购买次数(限制)
	 * @param key
	 * @param vipLv
	 * @return
	 */
	public int getConfVipBuyTimes(EVipBuyType key, int vipLv){
		int num = 0;
		ConfVipUpgrade conf = ConfVipUpgrade.get(vipLv);
		if (conf != null) {
			if (key == EVipBuyType.actBuyNum) {// 每日购买体力次数
				num = conf.actBuyNum;
			} else if (key == EVipBuyType.coinBuyNum) {// 每日购买金币次数
				num = conf.coinBuyNum;
			} else if(key == EVipBuyType.instResetNum){// 每日重置副本次数
				num = conf.instResetNum;
			} else if(key == EVipBuyType.competeFightNum){// 购买竞技场挑战次数
				num = conf.competeFightNum;	
			} else if(key == EVipBuyType.caveDevelopment) {
				num = conf.grabBuyNum;
			}else if(key == EVipBuyType.caveSnatch) {
				num = conf.occupyBuyNum ;
			}
		}
		return num;
	}
	/**
	 * 检测购买次数是否有效
	 * @return
	 */
	public boolean checkVipBuyNum(EVipBuyType key, int curNum, int vipLv) {
		if(vipLv >= 0) {
			int limit = getConfVipBuyTimes(key,vipLv);
			if(limit > 0) {
				return curNum<limit;
			}
		}
		return false;
	}
	
	

	/**
	 * 获取剩余购买次数
	 * @param humanObj 
	 * @param buyType 购买类型  EVipBuyType protocol 枚举值
	 * @return
	 */
	public int getVipBuyNum(HumanObject humanObj,int buyType) {		
		int vipLv = humanObj.getHuman().getVipLevel();
		EVipBuyType keyType = EVipBuyType.valueOf(buyType);
		int limit = 0;
		if(vipLv >= 0) {
			limit = getConfVipBuyTimes(keyType,vipLv);
		}		
		
		int buyTime = 0;//今日购买次数
		//获取购买次数
		JSONArray objArr = Utils.toJSONArray(humanObj.getHumanExtInfo().getVipBuyTimes());
		for(int i = 0; i < objArr.size(); ++ i){
			if (objArr.getJSONObject(i).getInteger("type") == buyType) {
				buyTime = objArr.getJSONObject(i).getIntValue("buytimes");
				break;
			}
		}
		
		int last = limit - buyTime;
		if (last < 0) {
			last = 0;
		}
		return last;
	}
	
	/**
	 * 购买VIP礼包
	 * @param humanObj
	 */
	public void buyVipGift(HumanObject humanObj, int vipLevel){
		// 获得商品ID
		Human human = humanObj.getHuman();
		ConfVipUpgrade confVip = ConfVipUpgrade.get(vipLevel);
		
		if(human.getVipLevel() < vipLevel){
			//vip等级不足
			humanObj.sendSysMsg(12, vipLevel);// VIP{b}级后才能购买！
			return;
		}
		
		//VIP道具
		String buyLog =  humanObj.getHumanExtInfo().getVipBuyLvs();
		boolean flag = Utils.check(buyLog, String.valueOf(vipLevel), ",");
		if(flag){
			humanObj.sendSysMsg(13);// 不能重复购买！
			return;
		}
		
		//扣除元宝
		LogSysModType logType = LogSysModType.VipLvGift;
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, confVip.nowGold, logType)) {
			return;
		}
		//vip等级礼包
		ConfRewards confReward = ConfRewards.get(confVip.vipLvGift);
		if(confReward!=null){
			int [] itemSn = confReward.itemSn;
			int [] itemNum =confReward.itemNum;
			RewardHelper.reward(humanObj, itemSn,itemNum, LogSysModType.VipLvGift);
		}
//		ConfItem confItem = ConfItem.get(confVip.vipLvGift);
//		if (confItem != null) {
//			// 进背包
////			ItemBagManager.inst().add(humanObj, confItem.id, 1,
////					LogSysModType.VIP等级礼包);
//			ItemChange itemChangeVO =RewardHelper.reward(humanObj, confItem.sn, 1, LogSysModType.VipLvGift);
//			// 发送变化消息
//			ItemBagManager.inst().sendChangeMsg(humanObj.getHumanObj(), itemChangeVO);
//		}
		
		//添加购买记录
		if(buyLog == null || !flag){
			if(StringUtils.isEmpty(buyLog)){
				buyLog = String.valueOf(vipLevel);
			} else {
				buyLog += "," + vipLevel;
			}
		}
		humanObj.getHumanExtInfo().setVipBuyLvs(buyLog);
		
		SCVIPBuyGift.Builder msg = SCVIPBuyGift.newBuilder();
		msg.setVipLevel(vipLevel);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 
	 * 
	 * 领取特殊物品礼包
	 * @param humanObj
	 * @param vipLv
	 */
	public void getGift(HumanObject humanObj,int vipLv){
		//判断是否可以领取
		int id = humanObj.getHumanExtInfo().getVipAwardId();
		if(id > humanObj.getHuman().getVipLevel()){
			humanObj.sendSysMsg(380102);// 您的vip等级不足
			return;
		}
		
		if(vipLv == id){
			vipLv = vipLv + 1;
		}
		
		humanObj.getHumanExtInfo().setVipAwardId(vipLv);
		
		
		//获取物品
		ConfVipUpgrade conf = ConfVipUpgrade.get(vipLv);
//		int itemid = conf.vipAward;
		int itemid = conf.vipLvGift;
		if(itemid > 0){
			ItemChange itemChange  = RewardHelper.reward(humanObj, itemid, 1, LogSysModType.VipAward);
			ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
			
			SCVIPBuyGift.Builder msg = SCVIPBuyGift.newBuilder();
			msg.setVipLevel(vipLv);
			humanObj.sendMsg(msg);
		}
		
	}
	
	/**
	 * 
	 * 
	 * 领取特殊物品礼包
	 * @param humanObj
	 */
	public void firstChargeReward(HumanObject humanObj){
		//判断是否可以领取
		int state = humanObj.getHumanExtInfo().getFirstChargeRewardState();
		if(state !=  1){ //已领取
			humanObj.sendSysMsg(380103);
			return;
		}
		
		//获取物品
		ConfParam firstChargeRewardId = ConfParam.get("firstChargeRewardId");
		int rewardSn = Utils.intValue(firstChargeRewardId.value);
		ConfRewards confRewards = ConfRewards.get(rewardSn);
		if (confRewards == null) {
			Log.table.error("===配置表错误ConfRewards no find sn={}", rewardSn);
		} else {
			// 奖励
			ItemChange rewards = RewardHelper.reward(humanObj, confRewards.itemSn, confRewards.itemNum, LogSysModType.VipFirstChargeReward);
		}
		
		state = 2;//成功领取
		humanObj.getHumanExtInfo().setFirstChargeRewardState(state);
		SCVIPFirstChargeReward.Builder msg = SCVIPFirstChargeReward.newBuilder();
		msg.setFirstChargeRewardState(state);
		humanObj.sendMsg(msg);
	}
	
}
