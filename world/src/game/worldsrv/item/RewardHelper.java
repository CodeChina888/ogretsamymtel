package game.worldsrv.item;

import game.msg.Define.DMoney;
import game.msg.Define.ECostGoldType;
import game.msg.Define.EModUnlockType;
import game.msg.Define.EMoneyType;
import game.msg.MsgCommon.SCLogCost;
import game.msg.MsgCommon.SCMoneyInfoChange;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfCostGold;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.entity.CostLog;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanManager;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.payment.PaymentManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.LogOpUtils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.List;

import com.pwrd.op.LogOp;
import com.pwrd.op.LogOpChannel;

import core.Port;
import core.support.SysException;
import core.support.Time;
import core.support.Utils;

/**
 * 消费或者奖励的工具类
 */
public class RewardHelper {
	
	// 货币的有效ID范围(0, 100) 即(EMoneyType.minMoney_VALUE, EMoneyType.maxMoney_VALUE)
	// 物品的有效ID范围(100, 10000000)
	public static final int MIN_ITEM = EMoneyType.maxMoney_VALUE;
	public static final int MAX_ITEM = 10000000;
	// 物品包的有效ID范围(10000000, ~)
	
	/**
	 * 获取物品剩余数量
	 */
	public static int countBySn(HumanObject humanObj, int itemSn) {
		ItemPack itemPack = ItemBagManager.inst().getPack(humanObj);
		// 处理物品
		int hasNum = itemPack.countBySn(itemSn);
		return hasNum;
	}
	
	/**
	 * 发放奖励：单个
	 */
	public static ItemChange reward(HumanObject humanObj, int itemSn, int num, LogSysModType log) {
		ItemChange itemChange = new ItemChange();
		if(itemSn > EMoneyType.minMoney_VALUE && itemSn < EMoneyType.maxMoney_VALUE) {
			// 处理货币
			addMoney(humanObj, itemSn, num, log);
			itemChange.addProduce(itemSn, num);
		} else if (itemSn > MIN_ITEM && itemSn < MAX_ITEM) {
			// 处理物品
			ItemChange bagChange = new ItemChange();
			bagChange =  ItemBagManager.inst().add(humanObj, itemSn, num, log);
			itemChange.merge(bagChange);
		} else if (itemSn > MAX_ITEM) {
			// 处理物品包
			ItemChange proChange = ProduceManager.inst().getAndGiveProduce(humanObj, itemSn, num, log);
			itemChange.merge(proChange);
		}
		return itemChange;
	}
		
	/**
	 * 发放奖励：多个
	 */
	public static ItemChange reward(HumanObject humanObj, int[] items, int[] nums, LogSysModType key) {
		if (items == null || items.length == 0 || nums == null || nums.length == 0 || items.length != nums.length) {
			Log.item.error("奖励格式错误！items={},nums={}", Utils.arrayIntToStr(items), Utils.arrayIntToStr(nums));
			return null;
		}
				
		ItemChange itemChange = new ItemChange();
		//挨个遍历每个ITEM
		for (int i = 0; i < items.length; i++) {
			//SN和数量
			int sn = items[i];
			int num = nums[i];
			//发放奖励
			itemChange.merge(RewardHelper.reward(humanObj, sn, num, key));
		}
		return itemChange;
	}
	/**
	 * 发放奖励：多个
	 * @param add 即加成奖励数量的百分比
	 */
	public static ItemChange reward(HumanObject humanObj, int[] items, int[] nums, LogSysModType key, int add) {
		
		if (items == null || items.length == 0 || nums == null || nums.length == 0 || items.length != nums.length) {
			Log.item.error("奖励格式错误！items={},nums={}", Utils.arrayIntToStr(items), Utils.arrayIntToStr(nums));
			return null;
		}
		int[] nums2 = nums.clone();//深拷贝
		for (int i = 0 ; i < nums.length;i++) {
			nums2[i] = nums[i] * add / 100;
		}
		return reward(humanObj, items, nums2, key);
	}
	
	/**
	 * 发放奖励：批量
	 */
	public static ItemChange reward(HumanObject humanObj, List<ProduceVo> produceList, LogSysModType key) {
		ItemChange itemChange = new ItemChange();
		//挨个遍历
		for (ProduceVo vo : produceList) {
			//发放奖励
			itemChange.merge(RewardHelper.reward(humanObj, vo.sn, vo.num, key));
		}
		return itemChange;
	}

	/**
	 * 扣除花费&道具
	 */
	public static boolean checkAndConsume(HumanObject humanObj, int itemSn, int num, LogSysModType type) {
		boolean ret = canConsume(humanObj, itemSn, num);
		if (ret) {
			consume(humanObj, itemSn, num, type);
		}
		return ret;
	}
	/**
	 * 扣除花费&道具
	 */
	public static boolean checkAndConsume(HumanObject humanObj, int[] items, int[] nums, LogSysModType type) {
		if (items == null || items.length == 0 || nums == null || nums.length == 0 || items.length != nums.length) {
			Log.item.error("奖励格式错误！items={},nums={},type={}", Utils.arrayIntToStr(items), Utils.arrayIntToStr(nums), type);
			return false;
		}

		if (canConsume(humanObj, items, nums)) {
			consume(humanObj, items, nums, type);
		} else {
			return false;
		}
		return true;
	}
	
	/**
	 * 扣除：批量
	 */
	public static boolean checkAndConsume(HumanObject humanObj, List<ProduceVo> voList, LogSysModType type) {
		for (ProduceVo vo : voList) {
			if (canConsume(humanObj, vo.sn, vo.num)) {
				consume(humanObj, vo.sn, vo.num, type);
			} else {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 是否能扣除：批量
	 */
	public static boolean canConsume(HumanObject humanObj, int[] items, int[] nums) {
		if (items == null || items.length == 0 || nums == null || nums.length == 0 || items.length != nums.length) {
			Log.item.error("奖励格式错误！items={},nums={}", Utils.arrayIntToStr(items), Utils.arrayIntToStr(nums));
			return false;
		}
		
		ItemPack itemPack = ItemBagManager.inst().getPack(humanObj);
		//挨个遍历数量够不够
		for (int i = 0; i < items.length; i++) {
			if (nums[i] < 0) {// 数量不可为负数
				return false;
			}
			if(items[i] > EMoneyType.minMoney_VALUE && items[i] < EMoneyType.maxMoney_VALUE) {
				// 处理货币
				if (!canReduceMoney(humanObj, items[i], nums[i])) {
					return false;
				}
			} else if (items[i] > MIN_ITEM && items[i] < MAX_ITEM) {
				// 处理物品
				int hasNum = itemPack.countBySn(items[i]);
				if(hasNum < nums[i]){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 是否能扣除：单条
	 */
	public static boolean canConsume(HumanObject humanObj, int itemSn, int num) {
		if (num < 0) {// 数量不可为负数
			return false;
		}
		if(itemSn > EMoneyType.minMoney_VALUE && itemSn < EMoneyType.maxMoney_VALUE) {
			// 处理货币
			boolean res = canReduceMoney(humanObj, itemSn, num);
			if(!res) {
				return false;
			}
		} else if (itemSn > MIN_ITEM && itemSn < MAX_ITEM) {
			// 处理物品
			ItemPack itemPack = ItemBagManager.inst().getPack(humanObj);
			int hasNum = itemPack.countBySn(itemSn);
			if(hasNum < num){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 扣除：批量
	 */
	private static void consume(HumanObject humanObj, int[] items, int[] nums, LogSysModType key) {
		if (items == null || items.length == 0 || nums == null || nums.length == 0 || items.length != nums.length) {
			Log.item.error("奖励格式错误！items={},nums={},type={}", Utils.arrayIntToStr(items), Utils.arrayIntToStr(nums), key);
			return;
		}
		
		for (int i = 0; i < items.length; i++) {
			if(items[i] > EMoneyType.minMoney_VALUE && items[i] < EMoneyType.maxMoney_VALUE) {
				// 处理货币
				reduceMoney(humanObj, items[i], nums[i], key);
			} else if (items[i] > MIN_ITEM && items[i] < MAX_ITEM) {
				// 处理物品
				ItemBagManager.inst().remove(humanObj,items[i], nums[i], key);
			}
			// 推送日志变化
			LogOpUtils.sendCostLog(humanObj, key, items[i], nums[i]);
		}
		
	}
	
	/**
	 * 扣除：批量
	 */
	private static void consume(HumanObject humanObj, List<ProduceVo> voList, LogSysModType key) {
		for (ProduceVo vo : voList) {
			int itemSn = vo.sn;
			int num = vo.num;
			if(itemSn > EMoneyType.minMoney_VALUE && itemSn < EMoneyType.maxMoney_VALUE) {
				// 处理货币
				reduceMoney(humanObj, itemSn, num, key);
			} else if (itemSn > MIN_ITEM && itemSn < MAX_ITEM) {
				// 处理物品
				ItemBagManager.inst().remove(humanObj,itemSn, num, key);
			}
			// 推送日志变化
			LogOpUtils.sendCostLog(humanObj, key, itemSn, num);
		}
	}
	
	/**
	 * 扣除：批量
	 * @return 
	 */
	private static void consume(HumanObject humanObj, int itemSn, int num, LogSysModType key) {
		if(itemSn > EMoneyType.minMoney_VALUE && itemSn < EMoneyType.maxMoney_VALUE) {
			// 处理货币
			reduceMoney(humanObj, itemSn, num, key);
		} else if (itemSn > MIN_ITEM && itemSn < MAX_ITEM) {
			// 处理物品
			ItemBagManager.inst().remove(humanObj, itemSn, num, key);
		}
		// 推送日志变化
		LogOpUtils.sendCostLog(humanObj, key, itemSn, num);
	}

	/**
	 * 是否能扣除：批量
	 */
	private static boolean canConsume(HumanObject humanObj, List<ProduceVo> voList) {
		ItemPack itemPack = ItemBagManager.inst().getPack(humanObj);
		for (ProduceVo vo : voList) {
			int itemSn = vo.sn;
			int num = vo.num;
			if (num < 0) {// 数量不可为负数
				return false;
			}
			//如果是货币
			if(itemSn > EMoneyType.minMoney_VALUE && itemSn < EMoneyType.maxMoney_VALUE) {
				// 处理货币
				return canReduceMoney(humanObj, itemSn, num);
			} else if (itemSn > MIN_ITEM && itemSn < MAX_ITEM) {
				// 处理物品
				int hasNum = itemPack.countBySn(itemSn);
				if(hasNum < num){
					return false;
				}
			}
		}
		return true;
	}
	
	public static List<ProduceVo> addNewMerge(int[] items, int[] nums) {
		if (items == null || items.length == 0 || nums == null || nums.length == 0 || items.length != nums.length) {
			Log.item.error("奖励格式错误！items={},nums={}", Utils.arrayIntToStr(items), Utils.arrayIntToStr(nums));
			return null;
		}
		List<ProduceVo> list = new ArrayList<>();
		//挨个遍历每个ITEM
		for (int i = 0; i < items.length; i++) {
			//SN和数量
			int sn = items[i];
			int num = nums[i];
			if(sn > MAX_ITEM) {
				// 处理物品包
				List<ProduceVo> result = new ArrayList<ProduceVo>();
				//根据SN和NUM数量，进行多次匹配，获得总得物品包
				for (int j = 0; j < num; j++) {
					result.addAll(ProduceManager.inst().produceItem(sn));
				}
				list.addAll(result);
			} else {
				ProduceVo vo = new ProduceVo(sn, num);
				list.add(vo);
			}
		}
		return list;
	}
	
	/**
	 * 获取CostGold表的消费元宝数
	 * @param type 即消耗元宝类型
	 * @param count 即消费第几次：1即第一次
	 */
	public static int getCostGold(ECostGoldType type, int count) {
		int cost = -1;// -1代表错误配置
		ConfCostGold conf = ConfCostGold.get(type.getNumber());
		if (conf == null) {
			Log.table.error("===ConfCostGold no find sn={}", type.getNumber());
			return cost;
		}
		if (conf.count.length != conf.costGoldNum.length) {
			Log.table.error("===ConfCostGold count.length != costGoldNum.length");
			return cost;
		}
		if (count < 1)
			return cost;
		
		// 默认取最大数组下标
		int index = conf.count.length-1;
		for (int i : conf.count) {
			if (i == count) {
				index = i - 1;
				break;
			}
		}
		cost = conf.costGoldNum[index];
		return cost;
	}
	
	/**
	 * 增加货币接口 addMoney
	 * @param humanObj
	 * @param type 
	 * @param num
	 * @param log
	 */
	private static void addMoney(HumanObject humanObj, int type, long num, LogSysModType log) {
		// 判断是否是货币类型
		if (type <= EMoneyType.minMoney_VALUE || type >= EMoneyType.maxMoney_VALUE) {
			throw new SysException("货币类型无法解析！");
		}

		if (num == 0)
			return;
		if (num < 0) {// 判断数量
			Log.human.error("===给钱的数量<0：addMoney() num<0, please use reduceMoney()");
			return;
		}

		Human human = humanObj.getHuman();
		long oldNum = 0;//记录旧值
		long newNum = 0;//记录新值
		
		switch (type) {
		case EMoneyType.act_VALUE: {// 体力
			oldNum = human.getAct();
			newNum = addAct(humanObj, num, log);
		}
			break;
		case EMoneyType.exp_VALUE: {// 经验
			oldNum = human.getExp();
			newNum = addExp(humanObj, num, log);
		}
			break;
		case EMoneyType.coin_VALUE: {// 铜币
			oldNum = human.getCoin();
			newNum = oldNum + num;
			human.setCoin(newNum);
		}
			break;
		case EMoneyType.gold_VALUE: {// 元宝
			oldNum = human.getGold();
			newNum = oldNum + num;
			human.setGold(newNum);
			human.update(true);
		}
			break;
		case EMoneyType.competeToken_VALUE: {// 威望
			oldNum = human.getCompeteToken();
			newNum = oldNum + num;
			human.setCompeteToken(newNum);
		}
			break;
		case EMoneyType.soulToken_VALUE: {// 元魂
			oldNum = human.getSoulToken();
			newNum = oldNum + num;
			human.setSoulToken(newNum);
		}
			break;
		case EMoneyType.towerToken_VALUE: {// 远征积分
			oldNum = human.getTowerToken();
			newNum = oldNum + num;
			human.setTowerToken(newNum);
		}
			break;
		case EMoneyType.generalToken_VALUE: {// 将魂
			oldNum = human.getGeneralToken();
			newNum = oldNum + num;
			human.setGeneralToken(newNum);
		}
			break;
		case EMoneyType.summonToken_VALUE: {// 招募代币
			oldNum = human.getSummonToken();
			newNum = oldNum + num;
			human.setSummonToken(newNum);
		}
			break;
		case EMoneyType.parnterExp_VALUE: {// 伙伴经验池
//			numOld = human.getParnterExp();
//			numNew = numOld + num;
//			human.setParnterExp(numNew);
		}
			break;
		case EMoneyType.runeToken_VALUE: {// 纹石碎片
			oldNum = human.getRuneToken();
			newNum = oldNum + num;
			human.setRuneToken(newNum);
		}
			break;
		case EMoneyType.lootSingle_VALUE: {// 抢夺本单人
			oldNum = human.getLootSingle();
			newNum = oldNum + num;
			human.setLootSingle(newNum);
		}
			break;
		case EMoneyType.lootMultiple_VALUE: {// 抢夺本多人
			oldNum = human.getLootMultiple();
			newNum = oldNum + num;
			human.setLootMultiple(newNum);
		}
			break;
		case EMoneyType.vipExp_VALUE: {//特权经验
			oldNum = human.getChargeGold();  // 充值金额 = vip经验
			newNum = oldNum + num;
			PaymentManager.inst().vipLevelGM(humanObj, (int)newNum);
		}
			break;
		case EMoneyType.summonPresent_VALUE: {// 招募赠送代币（仙缘）
			oldNum = human.getSummonPresent();
			newNum = oldNum + num;
			human.setSummonPresent(newNum);
		}
			break;
		case EMoneyType.summonHigher_VALUE: {// 高级招募令
			oldNum = human.getSummonHigher();
			newNum = oldNum + num;
			human.setSummonHigher(newNum);
		}
			break;
		case EMoneyType.refreshToken_VALUE: {// 刷新令
			oldNum = human.getRefreshToken();
			newNum = oldNum + num;
			human.setRefreshToken(newNum);
		}
			break;
		case EMoneyType.skillExperience_VALUE: {// 阅历（技能修炼）
			oldNum = human.getSkillExperience();
			newNum = oldNum + num;
			human.setSkillExperience(newNum);
		}
			break;
		case EMoneyType.refineToken_VALUE: {// 精炼石
			oldNum = human.getRefineToken();
			newNum = oldNum + num;
			human.setRefineToken(newNum);
		}
			break;	
		case EMoneyType.summonScore_VALUE: {// 招募积分
			oldNum = human.getSummonScore();
			newNum = oldNum + num;
			human.setSummonScore(newNum);
		}
			break;	
		case EMoneyType.cimeliaToken_VALUE: {// 法宝灵气
			oldNum = human.getCimeliaToken();
			newNum = oldNum + num;
			human.setCimeliaToken(newNum);
		}
			break;	
		case EMoneyType.developmentToken_VALUE: {// 矿山开采令
			oldNum = human.getDevelopmentToken();
			newNum = oldNum + num;
			human.setDevelopmentToken(newNum);
		}
			break;	
		case EMoneyType.snatchToken_VALUE: {// 矿山抢夺令
			oldNum = human.getSnatchToken();
			newNum = oldNum + num;
			human.setSnatchToken(newNum);
		}
			break;	
		case EMoneyType.resetStone_VALUE: {// 修炼重置石
			oldNum = human.getResetStone();
			newNum = oldNum + num;
			human.setResetStone(newNum);
		}
			break;	
		case EMoneyType.guildCoin_VALUE: {// 仙盟币
			oldNum = human.getGuildCoin();
			newNum = oldNum + num;
			human.setGuildCoin(newNum);
		}
			break;	
		default:
			throw new SysException("错误的货币类型：addMoney type={}", type);
		}

		// 记录日志
		Event.fire(EventKey.HumanMoneyChange, "type", type, "humanObj", humanObj, "log", log, "propOld", oldNum, "propNew", newNum);
		Event.fire(EventKey.HumanMoneyAdd, "type", type, "humanObj", humanObj, "log", log, "propOld", oldNum, "propNew", newNum);

		// 添加获得日志
		LogOpUtils.LogGain(human, log, type, num);
		sendSCMoneyInfoChange(humanObj);// 发送货币信息变化
	}
	
	/**
	 * 增加最大体力
	 *  当这个接口与addAct一起调用时候,这个接口要在addAct之前调用
	 * @param humanObj
	 * @param num
	 */
	private static void addMaxAct(HumanObject humanObj,int num){
		Human human = humanObj.getHuman();
		int oldActMax = human.getActMax(); // 旧的最大体力
		int newActMax = oldActMax+num;
		int curAct = (int)human.getAct();
		if(num!=0){
			if(curAct < newActMax){//当体力不满的时候需要重新设置时间
				long leftTime = humanObj.ttActValue.getTimeLeft(Port.getTime());
				long fullTime = 0;
				if(leftTime!=0){
					fullTime = Port.getTime() + leftTime + (newActMax-curAct-1) * ParamManager.cdHumanAct * Time.MIN;
				}else{
					fullTime = Port.getTime() + (newActMax-curAct) * ParamManager.cdHumanAct * Time.MIN;
				}
				setActFullTime(humanObj,fullTime);
			}
			human.setActMax(newActMax);// 设置最大体力值
		}
	}
	
	/**
	 * 增加体力
	 * @param log
	 * @param humanObj
	 * @param num
	 */
	private static long addAct(HumanObject humanObj, long num, LogSysModType log) {
		Human human = humanObj.getHuman();
		//LogDebugAct(humanObj,"addAct start");
		long tmCur = Port.getTime();
		ConfLevelExp confLevelExp = ConfLevelExp.get(human.getLevel());
		if (confLevelExp == null) {
			Log.table.error("ConfLevelExp配表错误，no find sn={} ", human.getLevel());
			return human.getAct();
		}
		int actMax = confLevelExp.staminaMax;// 当前等级最高体力
		long actOld = human.getAct();// 当前体力
		long actNew = actOld + (int) num;
		// 判断是否体力回满了
		if (actNew < actMax) {
			long actFullTime = 0;
			switch(log){
				case ActValueRecovery:{
					//自动回复体力 强制重新计算体力
					long fullTime = (actMax-actNew)* ParamManager.cdHumanAct * Time.MIN;
					actFullTime = tmCur+fullTime;
				}break;
				default:{
					// 升级送 主角购买 GM指令 章节奖励 直接忽略计时器的增加体力 需要重新计算
					//定时器肯定在运行
					long timeLeft = humanObj.ttActValue.getTimeLeft(tmCur); // 帧剩余时间
					if(timeLeft == 0){ //帧时间大于0
						actFullTime = tmCur + (actMax-actNew)* ParamManager.cdHumanAct * Time.MIN;
					}else{
						actFullTime = tmCur + (actMax-actNew - 1)* ParamManager.cdHumanAct * Time.MIN;
						actFullTime += timeLeft;
					}
				};
				setActFullTime(humanObj,actFullTime);
			}
		}
		human.setAct(actNew);
		return actNew;
	}
	
	public static void LogDebugAct(HumanObject humanObj,String type){
		Human human = humanObj.getHuman();
		long fullTime = human.getActFullTime();
		
		Log.game.error("@LogDebugAct ========={}========= name = {}, humanId = {}, lv = {}, act = {}, actMax ={}, actFullTime = {}, nowTime = {}",
				type,
				human.getName(),
				human.getId(),
				human.getLevel(),
				human.getAct(),
				human.getActMax(),
				Utils.formatTimeToDate(fullTime),
				Utils.formatTimeToDate(Port.getTime()),
				new Throwable());
	}
	
	/**
	 * 存储发送体力时间
	 * @param humanObj
	 * @param fullTime
	 */
	private static void setActFullTime(HumanObject humanObj,long fullTime){
		if(Port.getTime() - fullTime > Time.SEC){
			Log.human.error("   ========= setActFullTime ========= time err");
			return;
		}
		Human human = humanObj.getHuman();
		human.setActFullTime(fullTime);
		human.update(true); // 刷新缓存
		HumanManager.inst().sendSCActValueFullTimeChange(humanObj);//发送体力时间变化
	}
	

	private static long addExp(HumanObject humanObj, long expAdd, LogSysModType log) {
		Human human = humanObj.getHuman();
		long expOld = human.getExp(); // 基于当前等级的经验
		int lvOld = human.getLevel();// 记录原来的等级
		
		// 取的升级前的原等级配置表
		ConfLevelExp oldConf = ConfLevelExp.get(lvOld);
		if (lvOld >= ParamManager.maxHumanLevel) {
			return expOld;// 超过配置的人物最大等级
		}
		// 下一等级的配置表
		ConfLevelExp confOldNext = ConfLevelExp.get(lvOld+1);
		if (confOldNext == null) {
			Log.table.error("ConfLevelExp配表错误，no find sn={}", lvOld);
			return expOld;
		}
		// 基于当前等级，升到下一等级需要的经验
		int expUpgrade = confOldNext.roleExp - oldConf.roleExp;
		
		boolean levelUp = false;// 是否升级
		
		// 验证能否升级的经验
		long expNew = expOld + expAdd;
		int lvNew = lvOld;// 记录原来的等级
		long actAdd = 0; // 记录升级送的体力
		int actMax = human.getActMax();// 最大体力值.
		
		// 升级后的配置表
		ConfLevelExp conf = null;
		// 升级后+1的配置表
		ConfLevelExp confNext = null;
		while (expNew >= expUpgrade) {// 如果达到升级条件
			// 已有的经验值 - 下一等级的总经验
			expNew -= expUpgrade; 
			// 升级
			lvNew++;
			if (lvNew > ParamManager.maxHumanLevel) {
				lvNew = ParamManager.maxHumanLevel;
				break;// 超过配置的人物最大等级
			}
			conf = ConfLevelExp.get(lvNew);
			if (null == conf) {
				Log.table.error("ConfLevelExp配表错误，no find sn={} ", lvNew);
				lvNew--;
				break;
			}
			
			levelUp = true;
			actAdd += conf.staminaAdd;// 升级送的体力
			actMax = conf.staminaMax;// 最大体力值
			
			// 升级后的下一级配置表
			confNext = ConfLevelExp.get(lvNew + 1);
			// 当前已经是大等级，无法找到下一等级的配置表信息
			if (confNext == null) {
				lvNew = ParamManager.maxHumanLevel;
				break;
			}
			// 基于升级后的等级，升到下一等级需要的经验
			expUpgrade = confNext.roleExp - conf.roleExp; 
		}		
		human.setExp(expNew);// 保存经验
		if (levelUp && lvOld != lvNew) {
			human.setLevel(lvNew);// 保存等级
			
			//升级增加体力
			addMaxAct(humanObj,actMax-human.getActMax());
			// 升级送的体力
			addAct(humanObj, actAdd, LogSysModType.ActValueLevelUp);
			
			// 发布升级事件
			Event.fire(EventKey.HumanLvUp, "humanObj", humanObj, "lvOld", lvOld, "lvNew", lvNew);
			HumanManager.inst().sendSCLevelChange(humanObj);// 发送等级变化
			
			// 设置等级属性
			UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.Level);
			
			// 升级触发功能解锁
			ModunlockManager.inst().triggerModUnlock(humanObj, EModUnlockType.ModUnlockHumanLevel_VALUE, human.getLevel());

			// 添加升级日志:用户ID,角色名,等级,时间戳,日期串,账号id
//			LogOp.log(LogOpChannel.UPGRADE, human.getId(), human.getName(), human.getLevel(), Port.getTime(), Utils.formatTime(Port.getTime(), "yyyy-MM-dd"),
//					human.getAccount());
			
			// 发送主角升级日志
			LogOpUtils.sendUpgradeLog(humanObj);
		}
		return expNew;
	}
	
	/**
	 * 扣除货币接口 reduceMoney
	 * @param humanObj
	 * @param type
	 * @param num
	 * @param log
	 */
	private static void reduceMoney(HumanObject humanObj, int type, long num, LogSysModType log) {
		if (num == 0)
			return;
		if (num < 0) {// 判断数量
			Log.human.error("===扣钱的数量<0：reduceMoney() num<0, please use addMoney()");
			return;
		}
		// 判断是否足够消耗
		boolean ret = canReduceItem(humanObj, type, num);
		if (!ret) {
			return;
		}
		
		Human human = humanObj.getHuman();
		long oldNum = 0;
		long newNum = 0;

		switch (type) {
		case EMoneyType.act_VALUE: {// 活力
			oldNum = human.getAct();
			reduceAct(humanObj, num, log);
			newNum = human.getAct();
		}
			break;
		case EMoneyType.exp_VALUE: {// 经验
			oldNum = human.getExp();
			reduceExp(humanObj, num, log);
			newNum = human.getExp();
		}
			break;
		case EMoneyType.coin_VALUE: {// 铜币
			oldNum = human.getCoin();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setCoin(newNum);

		}
			break;
		case EMoneyType.gold_VALUE: {// 元宝
			oldNum = human.getGold();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setGold(newNum);
			human.update(true);// 持久化gold数据
			Event.fire(EventKey.ActConsumeGold, "humanObj", humanObj, "num", num);
//TODO			QuestDailyManager.inst().addGoldConsume(humanObj, (int) num);
		}
			break;
		case EMoneyType.competeToken_VALUE: {// 威望
			oldNum = human.getCompeteToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setCompeteToken(newNum);
		}
			break;
		case EMoneyType.soulToken_VALUE: {// 元魂
			oldNum = human.getSoulToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setSoulToken(newNum);
		}
			break;
		case EMoneyType.towerToken_VALUE: {// 远征积分
			oldNum = human.getTowerToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setTowerToken(newNum);
		}
			break;
		case EMoneyType.generalToken_VALUE: {// 将魂
			oldNum = human.getGeneralToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setGeneralToken(newNum);
		}
			break;
		case EMoneyType.summonToken_VALUE: {// 招募代币
			oldNum = human.getSummonToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setSummonToken(newNum);
		}
			break;
		case EMoneyType.parnterExp_VALUE: {// 伙伴经验池
			oldNum = human.getParnterExp();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setParnterExp(newNum);
		}
			break;
		case EMoneyType.runeToken_VALUE: {// 纹石碎片
			oldNum = human.getRuneToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setRuneToken(newNum);
		}
			break;
		case EMoneyType.lootSingle_VALUE: {// 抢夺本单人
			oldNum = human.getLootSingle();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setLootSingle(newNum);
		}
			break;
		case EMoneyType.lootMultiple_VALUE: {// 抢夺本多人
			oldNum = human.getLootMultiple();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setLootMultiple(newNum);
		}
			break;
		case EMoneyType.summonPresent_VALUE: {// 招募赠送代币（仙缘）
			oldNum = human.getSummonPresent();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setSummonPresent(newNum);
		}	
			break;
		case EMoneyType.summonHigher_VALUE: {// 高级招募令
			oldNum = human.getSummonHigher();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setSummonHigher(newNum);
		}	
			break;
		case EMoneyType.refreshToken_VALUE: {// 刷新令
			oldNum = human.getRefreshToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setRefreshToken(newNum);
		}	
			break;
		case EMoneyType.skillExperience_VALUE: {// 阅历（技能修炼）
			oldNum = human.getSkillExperience();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setSkillExperience(newNum);
		}	
			break;
		case EMoneyType.refineToken_VALUE: {// 阅历（技能修炼）
			oldNum = human.getRefineToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setRefineToken(newNum);
		}	
			break;
			
		case EMoneyType.summonScore_VALUE: {// 招募积分
			oldNum = human.getSummonScore();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setSummonScore(newNum);
		}	
			break;
			
		case EMoneyType.cimeliaToken_VALUE: {// 法宝灵气
			oldNum = human.getCimeliaToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setCimeliaToken(newNum);
		}	
			break;
		case EMoneyType.developmentToken_VALUE: {// 矿山开采令
			oldNum = human.getDevelopmentToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setDevelopmentToken(newNum);
		}	
			break;	
		case EMoneyType.snatchToken_VALUE: {// 矿山抢夺令
			oldNum = human.getSnatchToken();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setSnatchToken(newNum);
		}	
			break;
		case EMoneyType.resetStone_VALUE: {// 修炼重置石
			oldNum = human.getResetStone();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setResetStone(newNum);
		}	
		case EMoneyType.guildCoin_VALUE: {// 仙盟币
			oldNum = human.getGuildCoin();
			newNum = oldNum - num;
			if (newNum < 0) {
				newNum = 0;
				Log.human.error("reduceMoney() error in oldNum={} < reduceNum={},moneyType={} ", oldNum, num, type);
			}
			human.setGuildCoin(newNum);
		}	
			break;
		default:
			throw new SysException("错误的货币类型：addMoney type={}", type);
		}

//		LogOpUtils.LogCost(human, log , type, num);

		// 记录日志
		Event.fire(EventKey.HumanMoneyChange, "type", type, "humanObj", humanObj, "log", log, "propOld", oldNum, "propNew", newNum);
		Event.fire(EventKey.HumanMoneyReduce, "type", type, "humanObj", humanObj, "log", log, "propOld", oldNum, "propNew", newNum,"num",num);

		sendSCMoneyInfoChange(humanObj);// 发送货币信息变化
	}
	/**
	 * 扣除体力
	 */
	private static void reduceAct(HumanObject humanObj, long num, LogSysModType log) {
		Human human = humanObj.getHuman();
		long oldNum = human.getAct();
		long newNum = oldNum - num;
		if (newNum < 0) {
			newNum = 0;
		}
		// 设置新值
		human.setAct((int) newNum);
		int maxNum = 0;
		ConfLevelExp confLevelExp = ConfLevelExp.get(human.getLevel());
		if (confLevelExp != null) {
			maxNum = confLevelExp.staminaMax;// 角色当前等级最高体力
		}
		//满扣除了还是满
		if (newNum < maxNum) {
			// 恢复的点数，根据所扣的点数区分计算
			long tmActFull = Port.getTime();
			if (oldNum >= maxNum) {
				// 情况：从体力满 -- 到体力不满
				tmActFull = Port.getTime() + (maxNum - newNum) * ParamManager.cdHumanAct * Time.MIN;
				humanObj.ttActValue.reStart();
			} else {
				// 情况：体力不满--再继续扣体力
				long leftTime = humanObj.ttActValue.getTimeLeft(Port.getTime());
				if(leftTime == 0){
					tmActFull = Port.getTime() + (maxNum - newNum) * ParamManager.cdHumanAct * Time.MIN;
				}else{
					tmActFull = Port.getTime() + (maxNum - newNum-1) * ParamManager.cdHumanAct * Time.MIN;
					tmActFull += leftTime;
				}
			}
			// 体力恢复满点所需时间
			setActFullTime(humanObj,tmActFull);
		}
		//LogDebugAct(humanObj,"reduceAct");
	}
	
	/**
	 * 扣除经验
	 */
	private static void reduceExp(HumanObject humanObj, long num, LogSysModType log) {
		Human human = humanObj.getHuman();
		human.setExp(human.getExp() - num);
		sendSCMoneyInfoChange(humanObj);// 用户money属性变化
	}
	
	/**
	 * 道具是否足够消耗
	 */
	private static boolean canReduceItem(HumanObject humanObj, int itemSn, long num) {
		//  类型判断
		if (itemSn > EMoneyType.minMoney_VALUE && itemSn < EMoneyType.maxMoney_VALUE) { // 货币类型
			return canReduceMoney(humanObj, itemSn, num);
		} else {// 检查所需道具是否足够
			int numHave = ItemBagManager.inst().getNumBySn(humanObj, itemSn);
			if (numHave < num) {
				return false;// 所需道具不足！
			} else {
				return true;
			}
		}
	}
	/**
	 * 货币是否足够消耗
	 */
	private static boolean canReduceMoney(HumanObject humanObj, int type, long num) {
		// 判断数量
		if (num == 0) {// 没消耗
			return true;
		}

		boolean ret = false;
		if (num < 0) {
			Log.item.error("error:HumanManager.canProduceReduce,数量不能小于0,MoneyType:{},num:{}", type, num);
			return ret;
		}
		
		if (type <= EMoneyType.minMoney_VALUE || type >= EMoneyType.maxMoney_VALUE) {
			Log.item.error("error:HumanManager.canProduceReduce,检查是否能消耗玩家货币时发现无法解析的类型,MoneyType:{}", type);
			return ret;
		}

		Human human = humanObj.getHuman();
		long oldNum = 0;
		switch (type) {
		case EMoneyType.act_VALUE: {// 活力
			oldNum = human.getAct();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.exp_VALUE: {// 经验
			oldNum = human.getExp();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.coin_VALUE: {// 铜币
			oldNum = human.getCoin();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.gold_VALUE: {// 元宝
			oldNum = human.getGold();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.competeToken_VALUE: {// 威望
			oldNum = human.getCompeteToken();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.soulToken_VALUE: {// 元魂
			oldNum = human.getSoulToken();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.towerToken_VALUE: {// 远征积分
			oldNum = human.getTowerToken();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.generalToken_VALUE: {// 将魂
			oldNum = human.getGeneralToken();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.summonToken_VALUE: {// 招募代币
			oldNum = human.getSummonToken();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.parnterExp_VALUE: {// 伙伴经验池
			oldNum = human.getParnterExp();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.runeToken_VALUE: {// 纹石碎片
			oldNum = human.getRuneToken();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.lootSingle_VALUE: {// 抢夺本单人
			oldNum = human.getLootSingle();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.lootMultiple_VALUE: {// 抢夺本多人
			oldNum = human.getLootMultiple();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.summonPresent_VALUE: {// 招募赠送代币（仙缘）
			oldNum = human.getSummonPresent();
			if (oldNum >= num) {
				ret = true;
			}
		}
			break;
		case EMoneyType.summonHigher_VALUE: {// 高级招募令
			oldNum = human.getSummonHigher();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.refreshToken_VALUE: {// 刷新令
			oldNum = human.getRefreshToken();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.skillExperience_VALUE: {// 阅历（技能修炼）
			oldNum = human.getSkillExperience();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.refineToken_VALUE: {// 精炼石
			oldNum = human.getRefineToken();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.summonScore_VALUE: {// 招募积分
			oldNum = human.getSummonScore();
			if (oldNum >= num) {
				ret = true;
			}
		}	
			break;
		case EMoneyType.cimeliaToken_VALUE: {// 法宝灵气
			oldNum = human.getCimeliaToken();
			if (oldNum >= num) {
				ret = true;
			}
		}
			break;	
		case EMoneyType.developmentToken_VALUE: {// 矿山开采令
			oldNum = human.getDevelopmentToken();
			if (oldNum >= num) {
				ret = true;
			}
		}
			break;	
		case EMoneyType.snatchToken_VALUE: {// 矿山抢夺令
			oldNum = human.getSnatchToken();
			if (oldNum >= num) {
				ret = true;
			}
		}
			break;	
		case EMoneyType.resetStone_VALUE: {// 修炼重置石
			oldNum = human.getResetStone();
			if (oldNum >= num) {
				ret = true;
			}
		}
			break;	
		case EMoneyType.guildCoin_VALUE: {// 仙盟币
			oldNum = human.getGuildCoin();
			if (oldNum >= num) {
				ret = true;
			}
		}
			break;	
		default: // 非货币类型
			throw new SysException("错误的货币类型：canProduceReduce type={}", type);
		}
		// 返回错误提示
		if (!ret) {
			Log.game.debug("===道具{}不足===", type);
			humanObj.sendSysMsg(11, "C0", type);// {道具sn}不足，无法进行操作！
		}
		return ret;
	}
	
	/**
	 * 获取玩家货币信息
	 */
	public static DMoney getDMoney(Human human) {
		DMoney.Builder dMoney = DMoney.newBuilder();
	    dMoney.setAct(human.getAct());                          // 1 体力
	    dMoney.setExp(human.getExp());                          // 2 经验
	    dMoney.setCoin(human.getCoin());                        // 3 铜币
	    dMoney.setGold(human.getGold());                        // 4 元宝
	    dMoney.setCompeteToken(human.getCompeteToken());        // 5 威望
	    dMoney.setSoulToken(human.getSoulToken());              // 6 元魂
	    dMoney.setTowerToken(human.getTowerToken());            // 7 远征积分
	    dMoney.setGeneralToken(human.getGeneralToken());        // 8 将魂
	    dMoney.setSummonToken(human.getSummonToken());          // 9 招募代币
	    dMoney.setParnterExp(human.getParnterExp());            // 10 伙伴经验池
	    dMoney.setRuneToken(human.getRuneToken());              // 11 纹石碎片
	    dMoney.setLootSingle(human.getLootSingle());            // 12 抢夺本单人
	    dMoney.setLootMultiple(human.getLootMultiple());        // 13 抢夺本多人
	    dMoney.setSummonPresent(human.getSummonPresent());      // 16 招募奖励代币（仙缘）
	    dMoney.setSummonHigher(human.getSummonHigher());        // 17 高级招募令
	    dMoney.setRefreshToken(human.getRefreshToken());        // 18 刷新令
	    dMoney.setSkillExperience(human.getSkillExperience());  // 19 阅历（技能修炼）
	    dMoney.setRefineToken(human.getRefineToken());          // 20 精炼石
	    dMoney.setSummonScore(human.getSummonScore());          // 21 招募积分
	    dMoney.setCimeliaToken(human.getCimeliaToken());        // 22 法宝灵气
	    dMoney.setDevelopmentToken(human.getDevelopmentToken());// 23 矿山开采令
	    dMoney.setSnatchToken(human.getSnatchToken());          // 24 矿山抢夺令
	    dMoney.setResetStone(human.getResetStone());            // 25 修炼重置石
	    dMoney.setGuildCoin(human.getGuildCoin());//26仙盟币
		dMoney.setDevelopmentToken(human.getDevelopmentToken());//开采令
		dMoney.setSnatchToken(human.getSnatchToken());//强夺令
		return dMoney.build();
	}

	/**
	 * 发送货币信息变化
	 * @param humanObj
	 */
	public static void sendSCMoneyInfoChange(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		SCMoneyInfoChange.Builder msg = SCMoneyInfoChange.newBuilder();
		DMoney dMoney = getDMoney(human); // 获取货币信息
		msg.setMoney(dMoney);
		humanObj.sendMsg(msg);
	}
		
	/**
	 * 记录日志，包含给前端推动消费日志
	 * @param humanObj
	 */
	private static void logCost(long cost, int count, String reason, HumanObject humanObj) {
		Human human = humanObj.getHuman();
		
		//添加消费日志
		ConfItem item = ConfItem.get(EMoneyType.gold_VALUE);
		String name = item==null?"":item.name;
		if(cost>=0){
			LogOp.log(LogOpChannel.COST, human.getId(),
					Port.getTime(),Utils.formatTime(Port.getTime(), "yyyy-MM-dd"),
					reason,
					name,
					cost, 
					human.getAccountId(),
					human.getName(),
					human.getLevel());
		}
		else{
			LogOp.log(LogOpChannel.GAIN, human.getId(),
					Port.getTime(),Utils.formatTime(Port.getTime(), "yyyy-MM-dd"),
					EMoneyType.gold_VALUE, 
					name,
					-cost, 
					reason,
					human.getAccountId(),
					human.getName(),
					human.getLevel());			
		}
		
		//给前端传送信息
		SCLogCost.Builder msg = SCLogCost.newBuilder();
		
		//元宝和绑定元宝分两份走
		if(cost != 0){
			msg.setSn(EMoneyType.gold_VALUE);
			msg.setCost(cost);
			msg.setReason(reason);
			humanObj.sendMsg(msg);
		}
		
	}
	
	/**
	 * 消费日志入库
	 * @param humanObj			
	 * @param type		    消费类型
	 * @param oldMoney	    消费前
	 * @param newMoney    消费后
	 * @param count       消费数量
	 */
	private static void costPersist(HumanObject humanObj, int type, long oldMoney, long newMoney, long count, LogSysModType logType) {
		// 银币小于8万不记录
		//if (type == MoneyItemType.Gold && Math.abs(count) < 80000)
		//	return;
		CostLog log = new CostLog();
		log.setId(Port.applyId());
		log.setHumanId(humanObj.id);
		log.setName(humanObj.name);
		log.setType(type);
		log.setOldMoney(oldMoney);
		log.setNum(count);
		log.setNewMoney(newMoney);
		log.setOperate(logType.name());
		log.setTime(Utils.formatTime(Port.getTime(), "yyyy-MM-dd HH:mm:ss"));
		log.persist();
	}
}
