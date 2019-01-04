package game.worldsrv.drawCard;

import java.util.ArrayList;
import java.util.List;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define.DCardExchangeInfo;
import game.msg.Define.DDropItem;
import game.msg.Define.DPartnerInfo;
import game.msg.Define.DProduce;
import game.msg.Define.DrawCardBaseMsh;
import game.msg.Define.ECardExchange;
import game.msg.Define.ECostGoldType;
import game.msg.Define.EDrawOperation;
import game.msg.Define.EDrawType;
import game.msg.Define.EMapType;
import game.msg.Define.EMoneyType;
import game.msg.Define.EPartnerGetType;
import game.msg.MsgCard.SCDrawCardMsg;
import game.msg.MsgCard.SCLoadCardInfoMsg;
import game.msg.MsgCard.SCSummonScoreExchange;
import game.worldsrv.activity.ActivityInfo;
import game.worldsrv.activity.types.ActivityImmortalDiscount;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.config.ConfCardExchange;
import game.worldsrv.config.ConfCostGold;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfParam;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.drop.DropBag;
import game.worldsrv.drop.DropManager;
import game.worldsrv.entity.Card;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.ItemTypeKey;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class DrawCardManager extends ManagerBase {
	/**
	 * 普通抽卡一次消耗招募令消耗数量
	 */
	public static final String cardDrawUseSummonTokenByOne = "cardDrawUseSummonTokenByOne";

	/**
	 * 普通重发十次消耗招募令消耗数量
	 */
	public static final String cardDrawUseSummonTokenByTen = "cardDrawUseSummonTokenByTen";

	/**
	 * 首次执行招募令抽卡
	 */
	public static final String cardFirstCoinsCostId = "cardFirstCoinsCostId";

	/**
	 * 首次执行元宝抽卡
	 */
	public static final String cardFirstGoldCostId = "cardFirstGoldCostId";
	/**
	 * 普通招募令抽卡
	 */
	public static final String cardCoinsCostId = "cardCoinsCostId";
	/**
	 * 普通元宝抽卡
	 */
	public static final String cardGoldCostId = "cardGoldCostId";
	/**
	 * 特殊招募令抽卡
	 */
	public static final String cardSpecialCoinsCostId = "cardSpecialCoinsCostId";
	/**
	 * 特殊元宝令抽卡
	 */
	public static final String cardSpecialGoldCostId = "cardSpecialGoldCostId";
	/**
	 * 抽卡 - 首次付费进行元宝抽卡，获取DropInfo表的掉落编号
	 */
	public static final String cardFirstGoldPayId = "cardFirstGoldPayId";
	/**
	 * 抽卡 - 首次付费进行铜钱抽卡，获取DropInfo表的掉落编号
	 */
	public static final String cardFirstCoinsPayId = "cardFirstCoinsPayId";
	/**
	 * 招募令抽卡间隔免费倒计时（秒）
	 */
	public static final String cardCoinsCostFreeTime = "cardCoinsCostFreeTime";
	/**
	 * 元宝抽卡间隔免费倒计时（秒）
	 */
	public static final String cardGoldCostFreeTime = "cardGoldCostFreeTime";
	/**
	 * 招募令抽卡每日免费次数上限
	 */
	public static final String cardCoinsCostDayFreeTimes = "cardCoinsCostDayFreeTimes";

	public static int SPACE_ZML_NUM = 10;
	public static int SPACE_GOLD_NUM = 10;
	
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static DrawCardManager inst() {
		return inst(DrawCardManager.class);
	}

	/**
	 * 创角初始化
	 */
	@Listener(EventKey.HumanCreate)
	public void initDrawCardData(Param param){
		Human human = param.get("human");
		Card card = new Card();
		card.setId(Port.applyId());
		card.setHumanId(human.getId());
		card.setExchangeRound(1);
		card.persist();
		
//		humanObj.cardInfo = card;

		// 下发信息到客户端 TODO
//		send_CarInfo_loading(humanObj, card);
	
	}
	/**
	 * 玩家其它数据加载开始：加载玩家的抽卡信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_StageHumanEnter(Param params) {
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_StageHumanEnter humanObj is null");
			return;
		}
		Human human = humanObj.getHuman();

		// 设置状态 代表客户端已经准备好了
		humanObj.isClientStageReady = true;
		// 如果是创角后的第一个新手剧情
		if (human.getFirstStory() == 0 && humanObj.stageObj != null
				&& humanObj.stageObj.confMap.type.equals(EMapType.common.name())) {
			human.setFirstStory(1);
		}

		
		// 如果不是第一次创建角色
		// 不是第一次登陆，加载信息到humanObj
		DB dbPrx = DB.newInstance(Card.tableName);
		dbPrx.getBy(false, Card.K.HumanId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanDropInfo, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}

	// 加载数据库信息到humanObj
	public void _result_loadHumanDropInfo(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanPartner humanObj is null");
			return;
		}

		Record record = results.get();
		if (record == null) {
			Log.game.error("===_result_loadHumanPartner record is null");
		} else {
			// 加载数据到humanObj
			Card card = new Card(record);
			humanObj.cardInfo = card;
			// 下发信息到客户端
			send_CarInfo_loading(humanObj, card);
			//判断是否是第一次登陆
			boolean isFirstLogin = humanObj.isDailyFirstLogin;
			if(isFirstLogin){
				//重置免费抽卡次数
				Card carInfo = humanObj.cardInfo;
				if(carInfo!=null){
					carInfo.setDailyFreeSummonToken(0);
					DrawCardManager.inst().send_CarInfo_loading(humanObj,carInfo);
				}
			}
		}

		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			//重置免费抽卡次数
			Card carInfo = humanObj.cardInfo;
			if(carInfo!=null){
				carInfo.setDailyFreeSummonToken(0);
				DrawCardManager.inst().send_CarInfo_loading(humanObj,carInfo);
			}
		}
	}
	
	/**
	 * 登录的时候
	 * 发送消息到客户端
	 * 
	 * @param humanObj
	 * @param card
	 *            15710650805
	 */
	public void send_CarInfo_loading(HumanObject humanObj, Card card) {
		SCLoadCardInfoMsg.Builder msgs = SCLoadCardInfoMsg.newBuilder();
		DrawCardBaseMsh.Builder msgSummonToken = DrawCardBaseMsh.newBuilder();
		// 招募令
		msgSummonToken.setTypes(EDrawType.BySummonToken);
		long nextSummonTokenCd = card.getDrawCardFreeTimeBySummonToken();
		//
		msgSummonToken.setCD(nextSummonTokenCd);// 下次cd时间
		msgSummonToken.setFreeNum(card.getDailyFreeSummonToken());
		msgSummonToken.setFirstPay(card.isUserGoldFirst());
		msgs.addCardInfo(msgSummonToken);
		// 元宝
		DrawCardBaseMsh.Builder msgGold = DrawCardBaseMsh.newBuilder();
		msgGold.setTypes(EDrawType.ByGold);    
		long nextGoldTokenCd = card.getDrawCardFreeTimeByGold();
		msgGold.setCD(nextGoldTokenCd);
		msgGold.setNum(card.getTotleNumByGold());
		msgs.addCardInfo(msgGold);
		
		// 招募积分兑换信息
		DCardExchangeInfo.Builder info = msgs.getExchangeInfoBuilder();
		info.setRoundNum(card.getExchangeRound());
		for (int state : Utils.strToIntList(card.getExchangeState())) {
			info.addStates(ECardExchange.valueOf(state));
		}
		humanObj.sendMsg(msgs);
	}

	/**
	 * 下次免费招募令CD时间
	 * 
	 * @param nowCd
	 * @return
	 */
	public static long getNextCd_ZML(long nowCd) {
		ConfParam conf = ConfParam.get(cardCoinsCostFreeTime);
		long summonCd = Long.valueOf(conf.value) * 1000;
		return nowCd + summonCd;
	}

	/**
	 * 下次免费元宝CD时间
	 * 
	 * @param nowCd
	 * @return
	 */
	public static long getNextCd_Gold(long nowCd) {
		ConfParam conf = ConfParam.get(cardGoldCostFreeTime);
		long summonCd = Long.valueOf(conf.value) * 1000;
		return nowCd + summonCd;
	}

	/**
	 * 招募令抽卡
	 * @param humanObj
	 * @param type
	 */
	public void drawCard_ZML(HumanObject humanObj, EDrawOperation type) {
		if(type == EDrawOperation.One){
			boolean can = PartnerManager.inst().canRecruit(humanObj, 1);
			if(can){
				drawCarFor_SummonToken_Single(humanObj);
			}
		}else if(type == EDrawOperation.Ten){
			boolean can = PartnerManager.inst().canRecruit(humanObj, 10);
			if(can){
				drawCarFor_SummonToken_Ten(humanObj);
			}
		}
	}

	
	/**
	 * 元宝抽卡
	 * @param humanObj
	 * @param type
	 */
	public void drawCard_Gold(HumanObject humanObj, EDrawOperation type) {
		if(type == EDrawOperation.One){
			boolean can = PartnerManager.inst().canRecruit(humanObj, 1);
			if(can){
				drawCarFor_Goldn_Single(humanObj);
			}
		}else if(type == EDrawOperation.Ten){
			boolean can = PartnerManager.inst().canRecruit(humanObj, 10);
			if(can){
				drawCarFor_Gold_Ten(humanObj);
			}
		}
		
		int num = humanObj.cultureTimes.getGoldDraw() + type.getNumber();
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", num , "type",ActivitySevenTypeKey.Type_48);
		
	}
	
	
	/**
	 * 每日免费招募令抽卡
	 */
	public void drawCardForFree_ZML(HumanObject humanObj) {
		Card card = humanObj.cardInfo;
		//免费次数是否用完
		//今日免费抽卡次数
		int freeDailyNum = humanObj.cardInfo.getDailyFreeSummonToken();
		freeDailyNum++;
		ConfParam conf = ConfParam.get(cardCoinsCostDayFreeTimes);
		if(conf == null){
			Log.table.info("== initDrawCardData ConfParam table error sn ={}",cardCoinsCostDayFreeTimes);
			return;
		}
		int limit = Utils.intValue(conf.value);
		if(freeDailyNum > limit){
			//抽卡次数已经用完
			humanObj.sendSysMsg(18);
			return;
		}
		
		card.setDailyFreeSummonToken(freeDailyNum);

		// 冷却时间是否达到
		long cdTime = card.getDrawCardFreeTimeBySummonToken();
		if (cdTime != 0 && Port.getTime()  < cdTime) {
			// 冷却中
			
			return;
		}
		DropBag dropBag = null;
		if (card.isFreeForCardFirst_BySummonToken()) {
		
			dropBag = getDropBagByType(humanObj, cardFirstCoinsCostId);	// 不扣费 cardFirstCoinsCostId

			card.setFreeForCardFirst_BySummonToken(false);
		}else{
			 dropBag = getDropBagByType(humanObj, cardCoinsCostId);
		}
		//今日已抽卡次数+1
		int todayDraw = humanObj.cardInfo.getTodayDrawBySummonToken();
		todayDraw++;
		
		
		card.setTodayDrawBySummonToken(todayDraw);
		card.setDailyFreeSummonToken(freeDailyNum);
		// 设置时间
		long nextFreeTime  = getNextCd_ZML(Port.getTime());
		//测试代码 Port.getTime() 下面要改回nextFreeTime
		card.setDrawCardFreeTimeBySummonToken(nextFreeTime);
		
		
		
 		DrawCardBaseMsh.Builder dinfo = DrawCardBaseMsh.newBuilder();
		dinfo.setCD(humanObj.cardInfo.getDrawCardFreeTimeBySummonToken());
		dinfo.setNum(card.getTodayDrawByGold());
		dinfo.setFirstPay(humanObj.cardInfo.isUserGoldFirst());
		
		//要下发免费抽卡的次数 
		dinfo.setFreeNum(freeDailyNum);
		
		distributed(humanObj,dropBag,EDrawType.BySummonToken,EDrawOperation.One,dinfo);
		
		
		
	}

	/**
	 * 招募令抽卡 单次
	 * 
	 * @param humanObj
	 *
	 */
	public void drawCarFor_SummonToken_Single(HumanObject humanObj) {
		// 这辈子第一次免费机会用掉了没
		DropBag dropBag =null;
		Card card = humanObj.cardInfo;
	
		// 是否是首抽|正常付费抽卡
		String confIndex = card.isUserSummonTokenFirst() == true ? cardFirstCoinsPayId : cardCoinsCostId;
		// 扣费
		ConfParam conf = ConfParam.get(cardDrawUseSummonTokenByOne);
		int[] items = Utils.arrayStrToInt(conf.value);
		// 扣元宝
		if (!RewardHelper.checkAndConsume(humanObj, items[0], items[1], LogSysModType.DrawCard)) {
			return;
		}
		
		// 分配物品
		dropBag = getDropBagByType(humanObj, confIndex);
		
		
		DrawCardBaseMsh.Builder dinfo = DrawCardBaseMsh.newBuilder();
		dinfo.setCD(humanObj.cardInfo.getDrawCardFreeTimeBySummonToken());
		dinfo.setNum(card.getTodayDrawByGold());
		dinfo.setFirstPay(humanObj.cardInfo.isUserGoldFirst());
//		int freeNum = Integer.parseInt(ConfParam.get(cardCoinsCostDayFreeTimes).value);
		dinfo.setFreeNum(card.getDailyFreeSummonToken());
		dinfo.setNum(card.getTotleNumBySummonToken());
		
		card.setUserSummonTokenFirst(false);
		distributed(humanObj,dropBag,EDrawType.BySummonToken,EDrawOperation.One,dinfo);
		
	}
	
	
	/**
	 * 元宝抽卡 单次
	 * 
	 * @param humanObj
	 *
	 */
	public void drawCarFor_Goldn_Single(HumanObject humanObj) {
		// 是否是首抽|正常付费抽卡
		Card card = humanObj.cardInfo;
		boolean isFirstUse =  card.isUserGoldFirst();
		String confIndex = isFirstUse == true ? cardFirstGoldPayId : cardGoldCostId;
		// 扣费
		ConfCostGold conf = ConfCostGold.get(ECostGoldType.cardDrawOneCost_VALUE);
		if (null == conf) {
			Log.table.error("===ConfCostGold no find sn={}", ECostGoldType.cardDrawOneCost_VALUE);
			return;
		}
		//是否扣除代币
		ConfCostGold confGold = ConfCostGold.get(ECostGoldType.cardDrawOneCost_VALUE);
		if(confGold == null){
			Log.table.error("ConfCostGold error sn = {}",ECostGoldType.cardDrawOneCost_VALUE);
			return;
		}
		//代币不够才够钱，否则扣除代币
		long summonHigher = humanObj.getHuman().getSummonHigher();
		boolean isCostOther = summonHigher >= confGold.replaceItemNum?true:false;
		if(isCostOther){
			if(!RewardHelper.checkAndConsume(humanObj, EMoneyType.summonHigher_VALUE, confGold.replaceItemNum, LogSysModType.DrawCard)) {
				return;
			}
			
		}else{
			// 扣钱
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, conf.costGoldNum[0], LogSysModType.DrawCard)) {
				return;
			}
		}
		
		// 分配物品
		DropBag dropBag = getDropBagByType(humanObj, confIndex);
		
		//设置FLASE
		card.setUserGoldFirst(false);
		card.update();
		humanObj.cardInfo.setUserGoldFirst(false);
		
		
		
		DrawCardBaseMsh.Builder dinfo = DrawCardBaseMsh.newBuilder();
		dinfo.setCD(humanObj.cardInfo.getDrawCardFreeTimeByGold());
		dinfo.setNum(card.getTotleNumByGold());
		dinfo.setFirstPay(humanObj.cardInfo.isUserGoldFirst());
		distributed(humanObj,dropBag,EDrawType.ByGold,EDrawOperation.One,dinfo);
	}
	
	

	/**
	 * 招募令抽卡 连续抽10次
	 * 
	 * @param humanObj
	 *
	 */
	public void drawCarFor_SummonToken_Ten(HumanObject humanObj) {
			// 扣费
			ConfParam conf = ConfParam.get(cardDrawUseSummonTokenByTen);
			int[] items = Utils.arrayStrToInt(conf.value);
			
			//是否扣除代币
			ConfCostGold confGold = ConfCostGold.get(ECostGoldType.cardDrawTenCost_VALUE);
			if(confGold == null){
				Log.table.error("ConfCostGold error sn = {}",ECostGoldType.cardDrawTenCost_VALUE);
			}
				// 扣元宝
				if (!RewardHelper.checkAndConsume(humanObj, items[0], items[1], LogSysModType.DrawCard)) {
					return;
				}
			
			
			// 分配物品
			//看看首抽扣掉了没有
			// 是否是首抽|正常付费抽卡
			boolean confIndex = humanObj.cardInfo.isUserSummonTokenFirst();
			//抽卡掉落物品List
			List<DropBag> dropList = new ArrayList<>();
			if(confIndex){
				
				//一次首抽的概率，九次正常抽的概率
				DropBag dropSpecial = getDropBagByType(humanObj, cardFirstCoinsPayId);
				dropList.add(dropSpecial);
				for (int i = 0; i < 9; i++) {
					dropList.add(getDropBagByType(humanObj, cardCoinsCostId));
				}
				
				
			}else{
				//十次正常抽
				for (int i = 0; i < 10; i++) {
					dropList.add(getDropBagByType(humanObj, cardCoinsCostId));
				}
			}
			
			/*给予奖励并分发到客户端*/
			Card card = humanObj.cardInfo;
			
			DrawCardBaseMsh.Builder dpInfo = DrawCardBaseMsh.newBuilder();
			dpInfo.setCD(card.getDrawCardFreeTimeBySummonToken());
			dpInfo.setNum(card.getTotleNumBySummonToken());
			dpInfo.setFreeNum(card.getDailyFreeSummonToken());
			dpInfo.setFirstPay(card.isUserSummonTokenFirst());
			//给予奖励并分发到客户端
			DropBag dropBag = DropBag.getDropBagFromList(dropList);
			distributed(humanObj,dropBag,EDrawType.BySummonToken,EDrawOperation.Ten,dpInfo);
	}

	/**
	 * 元宝抽卡 连续抽10次
	 * 
	 * @param humanObj
	 *
	 */
	public void drawCarFor_Gold_Ten(HumanObject humanObj) {
			// 扣费
			ConfCostGold conf = ConfCostGold.get(ECostGoldType.cardDrawTenCost_VALUE);
			if (null == conf) {
				Log.table.error("===ConfCostGold no find sn={}", ECostGoldType.cardDrawTenCost_VALUE);
				return;
			}
			//是否扣除代币
			ConfCostGold confGold = ConfCostGold.get(ECostGoldType.cardDrawTenCost_VALUE);
			if(confGold == null){
				Log.table.error("ConfCostGold error sn = {}",ECostGoldType.cardDrawTenCost_VALUE);
				return;
			}
			//代币不够才够钱，否则扣除代币
			boolean isCostOther = humanObj.getHuman().getSummonHigher() >= confGold.replaceItemNum ? true:false;
			if(isCostOther){
				//扣代币
				if (!RewardHelper.checkAndConsume(humanObj, confGold.replaceItemSn, confGold.replaceItemNum, LogSysModType.DrawCard)) {
					return;
				}
			}else{
				// 扣元宝
				int costGoldNum = conf.costGoldNum[0];
				if(ActivityImmortalDiscount.isUpLimit(humanObj)){
					ConfCostGold confOne = ConfCostGold.get(ECostGoldType.cardDrawOneCost_VALUE);
					if (null == confOne) {
						Log.table.error("===ConfCostGold no find sn={}", ECostGoldType.cardDrawOneCost_VALUE);
						return;
					}
					
					costGoldNum = confOne.costGoldNum[0] * 10 * ActivityInfo.getAct31Discount() / 100;
				}
				if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGoldNum, LogSysModType.DrawCard)) {
					return;
				}
			}
			// 分配物品
			// 是否是首抽|正常付费抽卡
			boolean confIndex = humanObj.cardInfo.isUserSummonTokenFirst();
			//抽卡掉落物品List
			List<DropBag> dropList = new ArrayList<>();
			if(confIndex){
				
				//一次首抽的概率，九次正常抽的概率
				DropBag dropSpecial = getDropBagByType(humanObj, cardFirstGoldPayId);
				dropList.add(dropSpecial);
				for (int i = 0; i < 9; i++) {
					dropList.add(getDropBagByType(humanObj, cardGoldCostId));
				}
			}else{
				//十次正常抽
				for (int i = 0; i < 10; i++) {
					dropList.add(getDropBagByType(humanObj, cardGoldCostId));
				}
			}
			/*给予奖励并分发到客户端*/
			Card card = humanObj.cardInfo;
			
			
			DrawCardBaseMsh.Builder dpInfo = DrawCardBaseMsh.newBuilder();
			dpInfo.setCD(card.getDrawCardFreeTimeByGold());
			dpInfo.setNum(card.getTotleNumByGold());
			dpInfo.setFirstPay(card.isUserGoldFirst());
			
			DropBag dropBag = DropBag.getDropBagFromList(dropList);
			distributed(humanObj,dropBag,EDrawType.ByGold,EDrawOperation.Ten,dpInfo);
			//元宝10连 是否使用的代币
			Event.fire(EventKey.DrawCard_GoldTen, "humanObj", humanObj, "isCostOther",isCostOther);
	}
	
	
	/**
	 *每日免费元宝抽卡
	 */
	public void drawCardForFree_Gold(HumanObject humanObj) {
		// 冷却时间是否达到
		long cdTime = humanObj.cardInfo.getDrawCardFreeTimeByGold();
		if (Port.getTime() < cdTime) {
			// 冷却中
			return;
		}
			
		Card card = humanObj.cardInfo;
		DropBag dropBag = null;
		if (card.isFreeForCardFirst_ByGold()) {
			
			dropBag = getDropBagByType(humanObj, cardFirstGoldCostId);// 不扣费 cardFirstCoinsCostId
			
		}else{
			
			dropBag = getDropBagByType(humanObj, cardGoldCostId);// 分发奖励
		}
		
		long nextFreeTime  = getNextCd_Gold(Port.getTime());
		// 设置时间
		card.setDrawCardFreeTimeByGold(nextFreeTime);
		//设置抽卡次数
		card.setFreeForCardFirst_ByGold(false);
		
		
		DrawCardBaseMsh.Builder dinfo = DrawCardBaseMsh.newBuilder();
		dinfo.setCD(nextFreeTime);
		dinfo.setNum(card.getTotleNumByGold());
		//分发奖励
		distributed(humanObj,dropBag,EDrawType.ByGold,EDrawOperation.One,dinfo);
	}
	
	
	
	
	
	/**
	 * 
	 * 依据type返回物品列表
	 * @param humanObj
	 * @param type
	 */
	public DropBag getDropBagByType(HumanObject humanObj, String type) {
		Card card = humanObj.cardInfo;
		if(type.contains("Gold")){//元宝抽卡
			//记数
			int totle = card.getTotleNumByGold()+1;
			card.setTotleNumByGold(totle);
			card.update();
			
			boolean isSpecial = card.getTotleNumByGold() % DrawCardManager.SPACE_GOLD_NUM ==0?true:false;
			if(isSpecial){
				type = cardSpecialGoldCostId;
			}
			
		}else{//招募令
			//记数
			int totle = card.getTotleNumBySummonToken()+1;
			card.setTotleNumBySummonToken(totle);
			card.update();
			boolean isSpecial = card.getTotleNumBySummonToken() % DrawCardManager.SPACE_ZML_NUM ==0?true:false;
			
			if(isSpecial){
				type = cardSpecialCoinsCostId;
			}
		}
		
		ConfParam conf = ConfParam.get(type);
		int dropId = Integer.valueOf(conf.value);
		DropBag dropBag = DropManager.inst().getItem(humanObj, dropId);
		//设置循环最大上限
		int i = 0;
		int max = ParamManager.dropCountRepeatNum;
		while (i<max) {
			if(dropBag == null){
				continue;
			}
			if(dropBag.getItemSn().length>0){
				break;
			}
			dropBag = DropManager.inst().getItem(humanObj, dropId);
			i++;
		}
		return dropBag;
	}
	/**
	 * 设置掉落背包必出
	 * @return
	 */
	public void setBagWill(HumanObject humanObj,DropBag dropBag,int dropId){
		int i = 0;
		int max = ParamManager.dropCountRepeatNum;
		while (i<max) {
			if(dropBag == null){
				continue;
			}
			if(dropBag.getItemSn().length>0){
				break;
			}
			dropBag = DropManager.inst().getItem(humanObj, dropId);
			i++;
		}
	}
	
	public static final int PartnerItemType =120; 

	/**
	 * 给予奖励 并
	 * 分发到客户端
	 */
	public void distributed(HumanObject humanObj,DropBag dropBag,EDrawType type,EDrawOperation operation,DrawCardBaseMsh.Builder cardInfo){
		SCDrawCardMsg.Builder msg = SCDrawCardMsg.newBuilder();
		msg.setType(type);//1
		msg.setOperation(operation);//2
		
		if(cardInfo!= null){
			msg.setCardInfo(cardInfo);//6.cardInfo
		}
		int [] itemSn = dropBag.getItemSn();
		int [] itemNum = dropBag.getItemNum(); 
		int snLength = itemSn.length;
		
		// 招募次数
		int num = operation == EDrawOperation.One ? 1 : 10;
		// 招募获得的总积分
		int score = this.getDrawCardScore(type) * num;
		int[] moneys = {EMoneyType.summonScore_VALUE, EMoneyType.summonPresent_VALUE};
		int[] nums = {score, 0};
		if(type == EDrawType.ByGold){
			// 高级招募获得的仙缘 = 次数 * 一次获得的仙缘
			nums[1] = num * ParamManager.cardGetGodFate;
		} 
		RewardHelper.reward(humanObj, moneys, nums, LogSysModType.DrawCard);
		msg.setRewardScore(score); // 7.奖励积分
		
		List<PartnerObject> newPartnerList = new ArrayList<>();
		for (int i = 0; i < snLength ; i++) {
			int _itemSn = itemSn[i];
			
			ConfItem item = ConfItem.get(_itemSn);
			if(item == null){
				Log.table.error("== distributed 错误");
				continue;
			}
			//如果是掉落一个伙伴
 			if(item.itemType == PartnerItemType){
				int partnerSn =Integer.valueOf(item.param[0]);
				
					boolean hasPartner = PartnerManager.inst().isExistPartnerSn(humanObj, partnerSn);
					if(!hasPartner){
						//如果是新伙伴
						PartnerObject newPartner = PartnerManager.inst().recruit(humanObj,partnerSn,false,EPartnerGetType.DrawCard);
						newPartnerList.add(newPartner);
						continue;
					}else{
						//如果已有该伙伴
						List<DDropItem> rewards = PartnerManager.inst().decomposePartner(humanObj,partnerSn);
						for (DDropItem reward : rewards) {
							msg.addReward(reward);
						}
						msg.addChipItemList(1);//5.由英雄转换成物品的列表
					}
			}else{
				//如果是物品
				DDropItem.Builder reward = DDropItem.newBuilder();
				reward.setItemSn(itemSn[i]);
				reward.setItemNum(itemNum[i]);
				msg.addChipItemList(0);
				msg.addReward(reward);
			}
		}
		///新增伙伴信息
		for (PartnerObject pt : newPartnerList) {
			DPartnerInfo dpInfo = PartnerManager.inst().getDPartnerMsg(pt);

			msg.addPartnerInfoList(dpInfo);
		}
		humanObj.sendMsg(msg);
		Event.fire(EventKey.DrawCard, "humanObj", humanObj, "type",type.getNumber(),"num",operation.getNumber());
	}
	
	/**
	 * 招募一次获得的积分
	 * @param type 招募类型，高级/普通
	 * @return 一次的积分
	 */
	private int getDrawCardScore(EDrawType type) {
		int score = 0;
		int[] randAry = null;
		if (type == EDrawType.ByGold) {
			randAry = ParamManager.cardOnceHigherScore;
		} else {
			randAry = ParamManager.cardOnceNormalScore;
		}
		score = Utils.randomBetween(randAry[0], randAry[1]);
		return score;
		
	}

	/**
	 * 抽卡积分兑换
	 * @param index 兑换本轮的第几阶
	 * @param selectIndex 选择第几个兑换
	 */
	public void _msg_CSSummonScoreExchange(HumanObject humanObj, int index, int selectIndex) {
		// 获取轮数
		int round = humanObj.cardInfo.getExchangeRound();
		if (round > ParamManager.cardExchangeMaxRound) {
			Log.game.error("===招募积分兑换失败，超过兑换轮次===");
			humanObj.sendSysMsg(550401);
			return;
		}
		
		// 当前状态
		List<Integer> states = Utils.strToIntList(humanObj.cardInfo.getExchangeState());
		if (states.get(index) == null || states.get(index) == ECardExchange.ExchangeDo_VALUE) {
			Log.game.error("===招募积分兑换失败，已被兑换 ===");
			humanObj.sendSysMsg(550402);
			return;
		}
		// 配置表最大轮次
		int roundParam = ConfCardExchange.findAll().size() / 3;
		// 当前指向配置表的轮次 = 当前轮次 % 配置最大轮次
		int roundTag = round % roundParam;
		// 如果指向轮次等于0，则是刚好等于当前最大轮次 eg: 5 % 5 = 0,实际为第5轮
		if (roundTag == 0) {
			roundTag = roundParam;
		}
		
		ConfCardExchange conf = ConfCardExchange.get(ConfigKeyFormula.getCardExchangeSn(roundTag, index+1));
		if (conf == null) {
			Log.game.error("===招募积分兑换失败，配置异常 ===");
			humanObj.sendSysMsg(550403);
			return;
		}
		// 获取兑换的道具
		ConfItem confItem = ConfItem.get(conf.itemSn);
		if (confItem == null || confItem.itemType != ItemTypeKey.summonScoreExchange) {
			Log.game.error("===招募积分兑换失败，配置异常 itemSn:{} ===", conf.itemSn);
			humanObj.sendSysMsg(550403);
			return;
		}
		// 奖励配置
		ConfRewards confReward = ConfRewards.get(Utils.intValue(confItem.param[0]));
		if (confReward == null) {
			Log.game.error("===招募积分兑换失败，配置异常rewardSn:{} ===", confItem.param[0]);
			humanObj.sendSysMsg(550403);
			return;
		}
		int[] itemSns = confReward.itemSn;
		if (selectIndex > itemSns.length-1) {
			Log.game.error("===招募积分兑换失败，没有该选项 ===");
			humanObj.sendSysMsg(550404);
			return;
		}
		int rewardItemSn = itemSns[selectIndex];
		int rewardItemNum = confReward.itemNum[selectIndex];
		// 选择的道具
		confItem = ConfItem.get(rewardItemSn);
		if (confItem == null) {
			Log.game.error("===招募积分兑换失败，配置异常itemSn:{} ===", rewardItemSn);
			humanObj.sendSysMsg(550403);
			return;
		}
		
		// 检测积分是否满足解锁
		if (humanObj.getHuman().getSummonScore() < conf.unlockSocre) {
			Log.game.error("=== 积分不满足兑换条件 ===");
			humanObj.sendSysMsg(550405);
			return;
		}
		// 检测货币是否足够
		if (!RewardHelper.checkAndConsume(humanObj, conf.costSn, conf.costNum, LogSysModType.ScoreCardExchange)) {
			return;
		}
		
		// 奖励该道具
		ItemChange itemChange = RewardHelper.reward(humanObj, rewardItemSn, rewardItemNum,  LogSysModType.ScoreCardExchange);
		// 状态改变
		states.set(index, ECardExchange.ExchangeDo_VALUE);
		// 该轮次兑换数
		int count = 0;
		for (int state : states) {
			if (state == ECardExchange.ExchangeDo_VALUE) {
				count ++;
			}
		}
		// 如果全部都领取了
		if (count == states.size()) {
			// 状态重置
			states = Utils.strToIntList("0,0,0");
			// 轮次+1
			humanObj.cardInfo.setExchangeRound(round + 1);
		}
		humanObj.cardInfo.setExchangeState(Utils.ListIntegerToStr(states));
		
		SCSummonScoreExchange.Builder msg = SCSummonScoreExchange.newBuilder();
		DCardExchangeInfo.Builder dInfo = msg.getInfoBuilder();
		dInfo.setRoundNum(humanObj.cardInfo.getExchangeRound());
		for (int state : states) {
			dInfo.addStates(ECardExchange.valueOf(state));
		}
		DProduce dp = itemChange.getProduce().get(0);
		msg.setProduce(dp);
		humanObj.sendMsg(msg);
	}
	
	
	
}
