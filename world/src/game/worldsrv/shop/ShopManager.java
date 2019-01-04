package game.worldsrv.shop;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Time;
import core.support.observer.Listener;
import game.msg.Define.DShopInfo;
import game.msg.Define.DShopItem;
import game.msg.Define.EShopType;
import game.msg.MsgShopExchange.SCOpenShop;
import game.msg.MsgShopExchange.SCShopBuy;
import game.msg.MsgShopExchange.SCShopRef;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfShopControl;
import game.worldsrv.config.ConfShopProps;
import game.worldsrv.config.ConfVipUpgrade;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Shop;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.GlobalConfVal;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

/**
 * @author hl
 * @author Neak对接修改
 */
public class ShopManager extends ManagerBase {

	// 格子数量
	public static final int I1000 = 1000;

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static ShopManager inst() {
		return inst(ShopManager.class);
	}
	
	/**
	 * 登录，加载商店信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Shop.tableName);
		dbPrx.findBy(false, Shop.K.HumanId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanShop, "humanObj", humanObj);
		
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}

	private void _result_loadHumanShop(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanShop humanObj is null");
			return;
		}
		
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanShop records=null");
		} else {
			// 加载数据
			for (Record record : records) {
				Shop shop = new Shop(record);
				humanObj.shopMap.put(shop.getType(), shop);
			}
			
			// 是否需要刷新商店
			_judge_resetShop(humanObj);
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	/**
	 * 判断是否需要刷新商店
	 */
	private void _judge_resetShop(HumanObject humanObj) {
		long nowTm = Port.getTime();
		Map<Integer, Shop> shopMap = humanObj.shopMap;
		for (Shop shop : shopMap.values()) {
			ConfShopControl conf = ConfShopControl.get(shop.getType());
			// 无法自动刷新的都跳过
//			if (conf.funcId == ShopRefreshType.MTReset_Just.value() || 
//				conf.funcId == ShopRefreshType.Un_Reset.value() ) {
//				continue;
//			}
            if (humanObj.isDailyFirstLogin) {
                // 每日手动刷新次数
                shop.setDailyMTRefCount(0);
            }
			// 如果当前时间大于商店下次刷新时间，则商店刷新
			if (nowTm > shop.getNextAutoRefTime()) {
				// 重置商店
				_reset_Shop(humanObj, conf, Utils.getHourOfDay(nowTm));
			}
		}
	}
	
	/**
	 * 整点重置
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		int hour = Utils.getHourOfDay(Port.getTime());
		Collection<ConfShopControl> findAll = ConfShopControl.findAll();
		if(findAll == null){
			Log.table.error("===配置表ShopControl错误");
			return;
		}
		for (ConfShopControl conf : findAll) {
			// 完全不刷新的跳过
//			if (conf.funcId == ShopRefreshType.Un_Reset.value() ) {
//				continue;
//			}
			int[] refreshHour = conf.refreshHour;
			// 当前小时在刷新时间数组中的index
			int index = indexOfArray(refreshHour, hour);
			// 当前小时在数组中
			if(index >= 0){
				// 重置商店
				_reset_Shop(humanObj, conf, hour);
			}
		}
	}
	/**
	 * 重置商店
	 * @param humanObj 
	 * @param conf 配置表ConfShopControl
	 * @param hour 当前小时
	 */
	private void _reset_Shop(HumanObject humanObj, ConfShopControl conf, int hour) {
		Map<Integer, Shop> shopMap = humanObj.shopMap;
		Shop shop = shopMap.get(conf.sn);
		if (shop == null) {
			return;
		}
		shop.setItemsJSON(createShopItems(shop.getType(), humanObj.getHuman().getLevel()));
		
		// 不是纯手动刷新类型的商店，都需要设置下次自动刷新时间
//		if (conf.funcId != ShopRefreshType.MTReset_Just.value()) {
			// 刷新时间
			long nextAutoRefreshTime = this.get_NextAutoRefTime(conf.refreshHour, hour);
			// 设置下次自动刷新的时间
			shop.setNextAutoRefTime(nextAutoRefreshTime);
//		}
		
		// 跨天重置刷新购买次数
		if(hour == ParamManager.dailyHourReset){
			// 每日手动刷新次数
			shop.setDailyMTRefCount(0);
		}
		_send_SCShopRef(humanObj, shop);
	}

	/**
	 * 打开商店
	 * @param humanObj
	 * @param shopType
	 */
	public void _msg_CSOpenShop(HumanObject humanObj, EShopType shopType) {
        if (shopType == EShopType.ShopNone) {
            return;
        }
		// 商店管理map<ShopControl.sn, Shop>
		Map<Integer, Shop> shopMap = humanObj.shopMap;
		// 类型
		int sn = shopType.getNumber();
		Shop shop = shopMap.get(sn);
		if (shop == null) {
			ConfShopControl conf = ConfShopControl.get(sn);
			if (conf == null) {
				Log.game.error(" === 打开的商店异常 sn={} ===", sn);
				humanObj.sendSysMsg(215201);
				return;
			}
			if (!ModunlockManager.inst().isUnlock(conf.ModSn, humanObj)){
				Log.game.error(" === 该商店未开放 sn={} ===" , sn);
				humanObj.sendSysMsg(215202);
				return;
			}
			// 满足条件首次创建该类型商店数据
			this.createShopInfo(humanObj, conf);
			shop = shopMap.get(sn);
		} 
		// 发送商店数据
		_send_SCOpenShop(humanObj, shop);
	}
	/**
	 * 发送商店数据
	 * @param humanObj
	 * @param shop 商店数据
	 */
	private void _send_SCOpenShop(HumanObject humanObj, Shop shop) {
		SCOpenShop.Builder msg = SCOpenShop.newBuilder();
		DShopInfo.Builder shopInfo = DShopInfo.newBuilder();
		shopInfo.setShopType(EShopType.valueOf(shop.getType()));
		shopInfo.addAllShopItem(ShopItemJSON.jsonToDShopItems(shop.getItemsJSON()));
		shopInfo.setFreeRefCount(shop.getFreeRefCount());
		shopInfo.setLastReplyTime(shop.getLastReplyTime());
		shopInfo.setCountRefDaily(shop.getDailyMTRefCount());
		shopInfo.setNextAutoRefTime(shop.getNextAutoRefTime());
		msg.setShopInfo(shopInfo.build());
		humanObj.sendMsg(msg);
	}

	/**
	 * 购买道具
	 * 
	 * @param humanObject
	 * @param sn 商店格子Sn
	 * @param itemSn 道具Sn
	 * @param count 购买次数
	 */
	public void _msg_CSShopBuy(HumanObject humanObject, int sn, int itemSn, int count) {
		// 获取配表
		ConfShopProps confShopProp = ConfShopProps.get(sn);
		if (confShopProp == null) {
			Log.table.error("===ShopProps配置表错误 ,no find sn={}", sn);
			return;
		}
		// vip等级判断
		Human human = humanObject.getHuman();
		if (confShopProp.vipLv > human.getVipLevel()) {
			Log.game.error(" === vip等级不足,无法购买 ===", sn);
			humanObject.sendSysMsg(215401);
			return;
		}
		
		// 判断道具是否存在
		int index = 0;
		int[] goodsIDList = confShopProp.goodsIDList;
		for (int i = 0; i < goodsIDList.length; i++) {
			if (itemSn == goodsIDList[i]) {
				index = i;
				break;
			}
			index = -1;
		}
		if (index == -1) {
			Log.game.error(" === 您购买的商品不存在 sn={} ===", itemSn);
			humanObject.sendSysMsg(215402); 
			return;
		}
		// 判断等级
		int[] goodsShowMinLv = confShopProp.goodsShowMinLv;
		int[] goodsShowMaxLv = confShopProp.goodsShowMaxLv;
		if (human.getLevel() < goodsShowMinLv[index] || human.getLevel() > goodsShowMaxLv[index]) {
			Log.game.error(" === 购买物品不在您的等级范围内 sn={} ===", itemSn);
			humanObject.sendSysMsg(215403); 
			return;
		}
		// 扣除货币
		int shopTpye = sn / I1000;
		int shopIndex = sn % I1000;
		Shop shop = humanObject.shopMap.get(shopTpye);
		String shopItems = shop.getItemsJSON();
		int buyCount = shop.getBuyCount();
		List<ShopItemJSON> shopItemList = ShopItemJSON.jsonToList(shopItems);
		ShopItemJSON shopItemJSON = shopItemList.get(shopIndex - 1);
		// 购买次数并不是无限制购买
		if (shopItemJSON.num != -1 && count > shopItemJSON.num) {
			Log.game.error(" === 购买次数超过剩余次数 sn={} ===", itemSn);
			humanObject.sendSysMsg(215404);
			return;
		}
		//仙盟商店
		if(shopTpye == EShopType.ShopGuild_VALUE) {
			if(human.getGuildLevel() < confShopProp.guildLv) {
				//公会等级不足
				humanObject.sendSysMsg(431604);
				return;
			}
		}
		// 购买类型
		LogSysModType modType = LogSysModType.getByValue(1300 + shopTpye);
		// 需要的费用
		int costPrice = confShopProp.goodsPriceList[index] * count;
		if (!RewardHelper.checkAndConsume(humanObject, confShopProp.buyType, costPrice, modType)) {
			return;
		}
		// 次数大于0的才要减
		if (shopItemJSON.num > 0) { 
			shopItemJSON.num = shopItemJSON.num - count;
		}
		shop.setItemsJSON(ShopItemJSON.listToJson(shopItemList));
		// 该商店购买总次数（暂时无用）
		shop.setBuyCount(buyCount + count);
		// 发放奖励
		int buyGodsNum = confShopProp.goodsCountList[index] * count;
		RewardHelper.reward(humanObject, shopItemJSON.itemSn, buyGodsNum, modType);

		// 返回客户端数据
		SCShopBuy.Builder msg = SCShopBuy.newBuilder();
		DShopItem.Builder shopItem = DShopItem.newBuilder();
		shopItem.setSn(sn);
		shopItem.setItemSn(itemSn);
		shopItem.setNum(shopItemJSON.num);
		msg.setShopItem(shopItem.build());
		humanObject.sendMsg(msg);
		
		/*培养计数并且发布七日事件*/
		int num = 0;
		switch (EShopType.valueOf(shopTpye)) {
		case ShopGold:
			num = humanObject.cultureTimes.getGoldShop()+1;
			humanObject.cultureTimes.setGoldShop(num);
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObject, "progress",num , "type",
					ActivitySevenTypeKey.Type_43);
			break;
		case ShopGeneral:
			num = humanObject.cultureTimes.getSoulShop()+1;
			humanObject.cultureTimes.setSoulShop(num);
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObject, "progress",num , "type",
					ActivitySevenTypeKey.Type_44);
		case ShopArena:
			num = humanObject.cultureTimes.getCompeteShop()+1;
			humanObject.cultureTimes.setCompeteShop(num);
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObject, "progress",num , "type",
					ActivitySevenTypeKey.Type_45);
		case ShopTower:
			num = humanObject.cultureTimes.getJadeShop()+1;
			humanObject.cultureTimes.setJadeShop(num);
			Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObject, "progress",num , "type",
					ActivitySevenTypeKey.Type_46);
		default:
			break;
		}
		
		Event.fire(EventKey.ActShopBuy, "humanObj", humanObject, "shopType", shopTpye, "num", count);
	}

	private int getRefreshAddVip(HumanObject humanObj, EShopType shopType) {
		int vipLv = humanObj.getHuman().getVipLevel();
		ConfVipUpgrade conf = ConfVipUpgrade.get(vipLv);
		if (conf == null) {
			Log.table.info("==getRefreshAddVip ConfVipUpgrade sn={}", vipLv);
			return 0;
		}
		switch (shopType) {
		case ShopGold:
			return conf.goldShopNum;
		case ShopGeneral:
			return conf.goldShopNum1;
		case ShopArena:
			return conf.goldShopNum2;
		case ShopTower:
			return conf.goldShopNum3;
		case ShopSeven:
			return conf.goldShopNum4;
		case ShopGuild:
			return conf.goldShopNum5;
		default:
			return 0;
		}
	}
	
	/**
	 * 商店刷新
	 * @param humanObject
	 * @param shopType
	 */
	public void _msg_CSShopRef(HumanObject humanObject, EShopType shopType) {
		//取出相应类型商店
		Map<Integer, Shop> shopMap = humanObject.shopMap; 
		// ConfShopControl.sn
		int type = shopType.getNumber();
		Shop shop = shopMap.get(type);
		if(shop == null){
			Log.game.error(" === 请求购买商店类型有误 sn={} ===", type);
			humanObject.sendSysMsg(215601);
			return;
		}
		ConfShopControl conf = ConfShopControl.get(type);
		if(conf == null){
			Log.table.error("===ShopControl配置表错误,no find sn={}",type);
			return;
		}
		int countMax = conf.countMax + getRefreshAddVip(humanObject, shopType);
		//判断商店可否刷新
		if(countMax == 0){
			Log.game.error(" === 该商店不支持手动刷新 sn={} ===", type);
			humanObject.sendSysMsg(215602);
			return;
		}
		// 今日手动刷新次数
		int countRefDaily = shop.getDailyMTRefCount();
		if (countRefDaily >= countMax) {
			Log.game.error(" === 手动刷新次数超过上限 sn={} ===", type);
			humanObject.sendSysMsg(215603);
			return;
		}

		// 该商店当前可用的免费刷新次数
		int freeRefCount = shop.getFreeRefCount();
		// 当前时间
		long nowTime = Port.getTime();
		
		// 可以手动刷新
		if(conf.freeRefreshMax > 0){
			// 获取最后恢复刷新次数的时间戳
			long lastReplyTime = shop.getLastReplyTime();
			// 如果当前时间超过了最后恢复时间
			if (nowTime > lastReplyTime) {
				// 恢复一点的cd
				long cd = conf.freeRefreshTime * Time.SEC;
				// 经过的时间(ms) = 当前时间 - 最后恢复时间
				long throughTime = nowTime - lastReplyTime;
				// 计算恢复的次数
				long replyCount = throughTime / cd;
				freeRefCount += replyCount;
				
				// 当前免费次数超过上限，则设置为上限
				if (freeRefCount > conf.freeRefreshMax) {
					freeRefCount = conf.freeRefreshMax;
					// 满点的情况，则最后恢复时间则是当前时间
					shop.setLastReplyTime(nowTime);
				}
				else {
					// 目前最后恢复的时间 = 之前最后恢复时间 - 恢复cd * 恢复点数
					long nowLastReplyTime = lastReplyTime + cd * replyCount;
					shop.setLastReplyTime(nowLastReplyTime);
				}
			}
		}
			
		// 当前可用刷新次数
		if (freeRefCount > 0) { 
			// 刷新次数-1
			freeRefCount --;
			shop.setFreeRefCount(freeRefCount);
		} else {

			// 购买类型
			LogSysModType modType = LogSysModType.getByValue(1310 + type);
			if(!RewardHelper.checkAndConsume(humanObject, conf.moneyCost,conf.moneyNum, modType)){
				return;
			}
		}
		// 手动刷新次数+1
		shop.setDailyMTRefCount(countRefDaily + 1);
		
		//刷新商店道具
		int level = humanObject.getHuman().getLevel();
		//生成商店道具
		String shopItems = createShopItems(shopType.getNumber(), level);
		
		shop.setItemsJSON(shopItems);
		//返回客户端数据
		_send_SCShopRef(humanObject, shop);
	}

	/**
	 * 发送商店刷新信息
	 * @param humanObject
	 * @param shop
	 */
	private void _send_SCShopRef(HumanObject humanObject, Shop shop) {
		SCShopRef.Builder msg = SCShopRef.newBuilder();
		DShopInfo.Builder shopInfo = DShopInfo.newBuilder();
		shopInfo.setShopType(EShopType.valueOf(shop.getType()));
		shopInfo.addAllShopItem(ShopItemJSON.jsonToDShopItems(shop.getItemsJSON()));
		shopInfo.setFreeRefCount(shop.getFreeRefCount());
		shopInfo.setLastReplyTime(shop.getLastReplyTime());
		shopInfo.setCountRefDaily(shop.getDailyMTRefCount());
		msg.setShopInfo(shopInfo.build());
		humanObject.sendMsg(msg);
	}

	/**
	 * 生成商店数据
	 * @param humanObj
	 */
	public void createShopInfo(HumanObject humanObj, ConfShopControl conf) {
		long id = humanObj.id;
		int level = humanObj.getHuman().getLevel();
		Shop shop = new Shop();
		shop.setId(Port.applyId());
		shop.setHumanId(id);
		shop.setType(conf.sn);
		shop.setFreeRefCount(conf.freeRefreshMax);
		shop.setItemsJSON(createShopItems(conf.sn, level));
//		if (conf.funcId == ShopRefreshType.All_Reset.value()) {
			shop.setNextAutoRefTime(get_NextAutoRefTime(conf.refreshHour, Utils.getHourOfDay(Port.getTime())));
//		}
		shop.persist();
		humanObj.shopMap.put(conf.sn, shop);
	}

	/**
	 * 根据类型生成对应商店道具[{'sn':1,'in':1,'nm':1},.....] 没有符合的道具sn=0;
	 * @param shopType 商店类型
	 * @return
	 */
	public String createShopItems(int shopType, int level) {
		List<Integer> shopPropsSnList = GlobalConfVal.getShopPropsTypeList(shopType);
		if (shopPropsSnList == null) {
			return "[]";
		}
		JSONArray items = new JSONArray();
		int index = 0;// 取出道具的角标
		for (Integer shopPropsSn : shopPropsSnList) {
			ConfShopProps conf = ConfShopProps.get(shopPropsSn);
			if (conf == null) {
				Log.table.info("===ShopProps配表错误,no find sn={}", shopPropsSn);
				continue;
			}
			
			List<Integer> goodsWeightList = Utils.intToIntegerList(conf.goodsWeightList);
			if (shopType == conf.storeType) {
				// 判断等级如果不在范围内权重设置成0
				for (int j = 0; j < conf.goodsCountList.length; j++) {
					if (level < conf.goodsShowMinLv[j] || level > conf.goodsShowMaxLv[j]) {
						goodsWeightList.set(j, 0);
					}
				}
				if (conf.isRandom) {
					index = Utils.getRandRange(goodsWeightList);
				} else {
					index = 0;
				}
				int defaultBuyTime = conf.buyTime;
				// 购买次数为0，则次数无限，无限逻辑为-1
				if (defaultBuyTime == 0) {
					defaultBuyTime = -1;
				}
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("sn", shopPropsSn);
				jsonObject.put("in", index == -1 ? conf.goodsIDList[0] : conf.goodsIDList[index]);
				jsonObject.put("nm", defaultBuyTime);
				items.add(jsonObject);
			}
		}
		return items.toJSONString();
	}
	
	/**
	 * 获得值在数组中的索引
	 * @param array
	 * @param value
	 * @return -1不在数组中，大于0则对应数组索引 
	 */
	public int indexOfArray(int[] array, int value) {
		int index = -1;
        for (int i = 0 ; i < array.length; i++) {
            if (array[i] == value) {
            	index = i;
            }
        }
        return index;
    }
	
	/**
	 * 获取下次刷新时间
	 * @param refreshHours 刷新时间数组
	 * @param hour 当前时间的小时
	 * @return
	 */
	private long get_NextAutoRefTime(int[] refreshHours, int hour) {
		long nextTime = 0;
		// 刷新时间所对应的数组索引
		int index = -1;
		for (int i = refreshHours.length - 1 ; i >= 0; i--) {
		    if (hour >= refreshHours[i]) {
		    	index = i;
		    	break;
		    }
		}
		// 如果index小于刷新时间数组的长度
		if (index < refreshHours.length - 1) {
			// 下次刷新时间为
			int nextHour = refreshHours[index + 1];
			nextTime = Utils.getTimeBeginOfToday(Port.getTime()) + nextHour * Time.HOUR;
		} else {
			// 否则下次刷新时间为数组第一个
			int nextHour = refreshHours[0];
			nextTime = Utils.getTimeBeginOfToday(Port.getTime()) + nextHour * Time.HOUR + Time.DAY;
		}
		return nextTime;
	}
	
}
