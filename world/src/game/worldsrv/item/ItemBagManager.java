 package game.worldsrv.item;

import java.util.ArrayList;
import java.util.List;

import core.Record;
import core.dbsrv.DB;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define.DItem;
import game.msg.Define.EContainerType;
import game.msg.Define.EMoneyType;
import game.msg.MsgItem;
import game.msg.MsgItem.SCCompoundItemMsg;
import game.msg.MsgItem.SCItemUse;
import game.msg.MsgItem.SCItemsBagSell;
import game.msg.MsgItem.SCSelectPackageItem;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfItemCompose;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.ItemBag;
import game.worldsrv.enumType.ItemBindType;
import game.worldsrv.enumType.ItemPackType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.ReasonResult;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

/**
 * 背包物品管理器
 */
public class ItemBagManager extends ItemManager {

	public static ItemBagManager inst() {
		return inst(ItemBagManager.class);
	}

	@Override
	public ItemPack getPack(HumanObject humanObj) {
		return humanObj.itemBag;
	}

	@Override
	protected ItemPackType getPackType() {
		return ItemPackType.Bag;
	}

	@Override
	protected EContainerType getContainerType() {
		return EContainerType.Bag;
	}

	/**
	 * 获取容器总容量
	 * @param humanObj
	 * @return 容器容量
	 */
	public int getNumMax(HumanObject humanObj) {
		return humanObj.getHuman().getItemBagNumMax();
	}

	/**
	 * 获取容器剩余大小
	 * @param humanObj
	 * @return 剩余大小
	 */
	public int getNumRest(HumanObject humanObj) {
		// 剩余格子 = 背包格子总数 - 使用的格子数量
		return humanObj.getHuman().getItemBagNumMax() - humanObj.itemBag.getNum();
	}

	/**
	 * 玩家其它数据加载开始：加载玩家的背包物品信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(ItemBag.tableName);
		dbPrx.findBy(false, ItemBag.K.OwnerId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanItemBag, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanItemBag(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanItemBag humanObj is null");
			return;
		}
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanItemBag records=null");
		} else {
			// 加载数据
			for (Record record : records) {
				ItemBag item = new ItemBag(record);
				
				// 临时处理，如果有相同的背包道具，则合成为一个道具
				List<Item> is = humanObj.itemBag.findBySn(item.getSn());
				if(is.size() > 0){
					Item itemTmp = is.get(0);
					// 如果该道具不是纹石也不是时装，则要合在一起
					if (!ItemTypeKey.isSameType(ItemTypeKey.fashion, itemTmp.getType())) {
						itemTmp.setNum(itemTmp.getNum() + item.getNum());
					}
					item.remove();
					item = (ItemBag)itemTmp;
				}
				humanObj.itemBag.add(item);
			}
			_send_SCBagItemInfo(humanObj);// 下发所有背包物品信息
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	/**
	 * 下发所有背包物品信息
	 * @param humanObj
	 */
	public void _send_SCBagItemInfo(HumanObject humanObj) {
		MsgItem.SCBagItemInfo.Builder msg = MsgItem.SCBagItemInfo.newBuilder();
		ItemPack itemBagPack = humanObj.itemBag;// 背包信息
		List<DItem> dItemList = new ArrayList<>();
		for (Item item : itemBagPack.findAll()) {
			dItemList.add(item.getDItem());
		}
		msg.addAllDItemList(dItemList);// 背包物品列表
		humanObj.sendMsg(msg);
	}

	/**
	 * 获取道具数量
	 * @param itemSn
	 * @return
	 */
	public int getNumBySn(HumanObject humanObj, int itemSn) {
		int num = 0;
		ItemPack itemBag = humanObj.itemBag;
		if (itemBag != null) {
			// 查找背包物品
			num = itemBag.getNumBySn(itemSn);
		}
		return num;
	}
	
	/**
	 * 合成道具
	 * @param humanObj
	 * @param compoundItemSn 
	 * @param compoundCount
	 */
	public void _msg_CSCompoundItemMsg(HumanObject humanObj, int compoundItemSn, int compoundCount) {
		ConfItem confItem = ConfItem.get(compoundItemSn);
		ConfItemCompose confItemCompose = ConfItemCompose.get(confItem.compoundId);		
		
		// 遍历所有的物品，判断材料够不够
		for (int i = 0; i < confItemCompose.fragmentId.length; i++) {
			boolean result = ItemBagManager.inst().canRemove(humanObj,confItemCompose.fragmentId[i], confItemCompose.needCount[i]*compoundCount);
			if (!result) {
				humanObj.sendSysMsg(17);//材料不足
				return;
			}
		}
		
		// 判断消耗
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.coin_VALUE, confItemCompose.cost, LogSysModType.BagItemCompose)) {
			return;
		}
		
		// 扣物品
		for (int i = 0; i < confItemCompose.fragmentId.length; i++) {
			ItemBagManager.inst().remove(humanObj, confItemCompose.fragmentId[i],confItemCompose.needCount[i]*compoundCount, LogSysModType.BagItemCompose);
		}
		RewardHelper.reward(humanObj, confItemCompose.targetId, compoundCount, LogSysModType.BagItemCompose);
		
		SCCompoundItemMsg.Builder msg = SCCompoundItemMsg.newBuilder();
		msg.setResult(true);
		humanObj.sendMsg(msg);
		
	}

	/**
	 * 出售背包物品 (单个按数量卖)
	 * @param itemId
	 * @param num
	 */
	public void _msg_CSItemBagSell(HumanObject humanObj, long itemId, int num) {
		// 小于1 不让卖
		if (num < 1)
			return;

		EContainerType from = getContainerType();
		// 能否出售
		ReasonResult result = canSell(humanObj, itemId);
		if (!result.success) {
			// Inform.user(humanObj.id, Inform.promptOperation, result.reason);
			Log.item.error("出售背包物品时错误 : HumanId={}, Container={}, itemId={}, error={}", humanObj.id, from.toString(),
					itemId, result.reason);
			return;
		}

		ItemPack bag = getPack(humanObj);
		Item item = bag.get(itemId);
		// 物品不存在或数量不对
		if (item == null || item.getNum() < num)
			return;

		// 扣减要出售的数量
		item.setNum(item.getNum() - num);
		item.setFrom(from);// 记录来自哪个容器的改变

		ItemChange change = new ItemChange();
		if (item.getNum() > 0) {// 剩余数量大于0 记录修改
			change.modItem(item, num);
		} else {// 剩余的物品小于0 直接删除
			change.delItem(item);
			bag.remove(itemId);
			item.remove();
		}

		ConfItem confItem = ConfItem.get(item.getSn());
		if (confItem == null) {
			Log.table.error("ConfItem 配表错误，no find sn={}", item.getSn());
			return;
		}
		// 最后出售价格
		long price = confItem.salePrice * num;

		// 发送 物品变动 事件
		change.fireChangeEvent(humanObj);

		// 给钱
		RewardHelper.reward(humanObj, EMoneyType.coin_VALUE, (int)price, LogSysModType.BagSellItem);

		// 发送物品变动信息
		sendChangeMsg(humanObj, change);
	}
	
	/**
	 * 出售背包物品 -- 多个物品
	 */
	public void _msg_CSItemsBagSell(HumanObject humanObj, List<DItem> listPro) {
		EContainerType from = getContainerType();
		ItemPack bag = getPack(humanObj);
		int price = 0;

		for(DItem pro : listPro){
			int num = pro.getNum();
			long id = pro.getItemId();
			// 小于1 不让卖
			if (pro.getNum() < 1)
				continue;
			// 能否出售
			ReasonResult result = canSell(humanObj, pro.getItemId());
			if (!result.success) {
				// Inform.user(humanObj.id, Inform.promptOperation, result.reason);
				Log.item.error("出售背包物品时错误 : HumanId={}, Container={}, itemId={}, error={}", humanObj.id, from.toString(),
						id, result.reason);
				continue;
			}
			
			Item item = bag.get(id);
			// 物品不存在或数量不对
			if (item == null || item.getNum() < num)
				continue;
			ConfItem confItem = ConfItem.get(item.getSn());
			if (confItem == null) {
				Log.table.error("ConfItem 配表错误，no find sn={}", item.getSn());
				continue;
			}
			
			// 扣减要出售的数量
			item.setNum(item.getNum() - num);
			item.setFrom(from);// 记录来自哪个容器的改变
			
			ItemChange change = new ItemChange();
			if (item.getNum() > 0) {// 剩余数量大于0 记录修改
				change.modItem(item, num);
			} else {// 剩余的物品小于0 直接删除
				change.delItem(item);
				bag.remove(id);
				item.remove();
			}
			
			// 最后出售价格--固定用金币
			price += confItem.salePrice * num;
			// 发送 物品变动 事件
			change.fireChangeEvent(humanObj);
			// 发送物品变动信息
			sendChangeMsg(humanObj, change);
		}
		// 给钱
		RewardHelper.reward(humanObj, EMoneyType.coin_VALUE, price, LogSysModType.BagSellItem);
		SCItemsBagSell.Builder msg = SCItemsBagSell.newBuilder();
		if(price > 0){//说明至少出售一件物品
			msg.setResult(true);
			msg.setGetMoney(price);
		}else{
			msg.setResult(false);
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 选择礼包道具中的物品
	 * @param humanObj
	 * @param num 礼包数量（可以同打开多个相同的礼包，选择同一个物品）
	 * @param packageSn
	 * @param index
	 */
	public void _msg_CSSelectPackageItem(HumanObject humanObj, int num, int packageSn, int index) {
		// 判断是否有那么多可以打开的礼包
		boolean result = ItemBagManager.inst().canRemove(humanObj, packageSn, num);
		if (!result) {
			Log.game.error("=== 礼包不足：{} ===", num);
			humanObj.sendSysMsg(160901);
			return;
		}
		
		ConfItem confItem = ConfItem.get(packageSn);
		if (confItem == null) {
			Log.game.error("=== 礼包不存在：{} ===", packageSn);
			humanObj.sendSysMsg(160902);
			return;
		}
		int rewardSn = Integer.valueOf(confItem.param[0]);
		ConfRewards confReward = ConfRewards.get(rewardSn);
		if (confReward == null) {
			Log.game.error("=== 礼包不存在：{} ===", rewardSn);
			humanObj.sendSysMsg(160902);
			return;
		}
		
		int[] itemSns = confReward.itemSn;
		int[] itemNum = confReward.itemNum;
		if (itemSns.length != itemNum.length || itemSns.length < index+1) {
			Log.game.error("=== 选择道具错误：{} ===", index);
			humanObj.sendSysMsg(160903);
			return;
		}
		// 获得选择的奖励
		ItemChange itemChange = RewardHelper.reward(humanObj, itemSns[index], itemNum[index] * num, LogSysModType.BagSelectPackageItem);
		// 移除该礼包
		ItemBagManager.inst().remove(humanObj, packageSn, num, LogSysModType.BagSelectPackageItem);
		
		SCSelectPackageItem.Builder msg = SCSelectPackageItem.newBuilder();
		msg.addAllProduce(itemChange.getProduce());
		humanObj.sendMsg(msg);
	}

	public ReasonResult canSell(HumanObject humanObj, long itemId) {
		ItemPack bag = this.getPack(humanObj);

		Item item = bag.get(itemId);
		// 物品不存在
		if (item == null) {
			Log.item.error("物品不存在，not fined id = {} ", itemId);
			return new ReasonResult(false);// 物品不存在 wgz humanObj.sendSysMsg(32);
		}
		ConfItem confItem = ConfItem.get(item.getSn());
		// 配置错误
		if (confItem == null) {
			Log.table.error("ConfItem  配表错误，no find sn={}", item.getSn());
			return new ReasonResult(false);
		}
		// 配置中不能卖
		/*if (!confItem.sell) {
			Log.item.warn("物品出售失败，配置为不能出售，SN:{}", item.getSn());
			humanObj.sendSysMsg(168102);
			return new ReasonResult(false);// 168102物品不能被出售 wgz
		}*/

		return new ReasonResult(true);
	}

	public ReasonResult itemUse(HumanObject humanObj, long itemId, int num, LogSysModType log) {
		ItemPack bag = getPack(humanObj);

		// 查找背包物品
		ItemBag itemBag = bag.get(itemId);
		if (itemBag == null) {
			Log.item.error("物品不存在，not fined id = {} ", itemId);
			return new ReasonResult(false);// 物品不存在 wgz humanObj.sendSysMsg(32);
		}

		// 查找配置
		ConfItem confItem = ConfItem.get(itemBag.getSn());
		if (confItem == null) {
			Log.table.error("ConfItem 配表错误，no find sn={}", itemBag.getSn());
			return new ReasonResult(false);
		}

		// 判断是否可以使用
		if (confItem.use == 0) {
			humanObj.sendSysMsg(168101);// 此物品不能使用
			return new ReasonResult(false);// 此物品不能使用 wgz
		}

		// 数量不足
		if (num < 0 || itemBag.getNum() < num) {
			humanObj.sendSysMsg(17);// 材料不足
			return new ReasonResult(false);// 物品不足 wgz
		}

		//int type = ItemTypeKey.getSubType(itemBag.getType());
		int type = itemBag.getType();
		// 发布使用物品事件
		Event.fireEx(EventKey.ItemUse, type, "humanObj", humanObj, "itemBag", itemBag, "confItem", confItem, "snItem", confItem.sn, "num", num);
		if(type == ItemTypeKey.useForAttributes || type == ItemTypeKey.useForDropInfo /*|| type == ItemTypeKey.useForItem*/){
			itemUseProcess(humanObj, itemBag, confItem, num);
		}

		return new ReasonResult(true);
	}

	/**
	 * 道具使用相关处理
	 * @param humanObj
	 * @param itemBag
	 * @param confItem
	 * @param num
	 */
	private void itemUseProcess(HumanObject humanObj, ItemBag itemBag, ConfItem confItem, int num) {
		if (null == humanObj) {
			Log.game.error("===_listener_ItemUse humanObj is null");
			return;
		}

		if (null == confItem) {
			Log.table.error("===HumanManager._listener_ItemUse, ConfItem=null");
			return;
		}
		if (null == confItem.param) {
			Log.table.error("===ConfItem配表错误，confItem.param == null，sn={}", confItem.sn);
			return;
		}
		// 获取使用礼包得到的物品
		ItemChange itemChange = getItemChangeByItemUse(humanObj, confItem, num);
		// 删掉使用的物品
		ItemBagManager.inst().remove(humanObj, itemBag, num);
		// 发送消息
		SCItemUse.Builder msg = SCItemUse.newBuilder();
		msg.addAllProduces(itemChange.getProduce());
//		for (ProduceVo produceVo : result) {
//			msg.addProduces(produceVo.toDProduce());
//		}
		//msg.setIsTip(!confItem.autoUse);
		msg.setIsTip(confItem.autoUse == 0);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 获取param中的物品或金币
	 * @param param
	 * @param num
	 * @return
	 */
	public List<ProduceVo> getProduceVoList(String[] param, int num) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if (null == param || 0 == num) {
			Log.game.error("===HumanManager.getProduceVoList, param={}, num={}", param, num);
			return result;
		} else {
			for (int i = 0; i < num; i++) {
				//for (int j = 0; j < param.length; j++) {
					//int[] itemParam = Utils.strToIntArraySplit(param[j]);
					int itemSn = Utils.intValue(param[0]);// 类型 <100:货币类型 item表sn
					int itemNum = Utils.intValue(param[1]);// 数量
					if (itemSn > EMoneyType.minMoney_VALUE && itemSn < EMoneyType.maxMoney_VALUE) { // 货币类型
						result.add(new ProduceVo(itemSn, itemNum));
					//} else if (sn == ItemParamType.GiftBag.value()) {// 物品包类型
					} else {// 物品包类型
						//result.addAll(ProduceManager.inst().produceItem(value));
						result.addAll(ProduceManager.inst().produceItem(itemSn,itemNum));
					}
				//}
			}
		}
		return result;
	}
	
	/**
	 * 清空背包
	 * @param humanObj
	 */
	public void itemBagClear(HumanObject humanObj) {
		ItemPack bag = getPack(humanObj);
		for (Item item : bag.findAll()) {
			if (item != null) {
				// 扣除道具
				ItemBagManager.inst().remove(humanObj, item, item.getNum());
			}
		}
	}
	


	// 字符串转int[]数组
	public int[] getStringToInt(String str) {
		if (str == null || str.isEmpty()) {
			return null;
		}
		String[] array = str.split("\\|");
		int[] result = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = Integer.valueOf(array[i]);
		}
		return result;
	}
	// String 转String数组
	public String[] getStringToStrArray(String str) {
		String[] array = str.split(",");

		return array;
	}
	
	
	/**
	 * 判断是否可以添加物品 多个物品
	 * @param humanObj
	 * @param sn
	 * @param num
	 * @return
	 */
	public boolean canAdd(HumanObject humanObj, int[] sn, int[] num) {
		// 判断物品类型和数量是否为空或个数不等
		if (sn == null || num == null || sn.length != num.length) {
			Log.table.error("配表有误！sn.length != num.length ,sn = {}, num = {}", sn, num);
			return false;
		}
		List<ItemVO> listItemVO = new ArrayList<ItemVO>();
		for (int i = 0; i < sn.length; i++) {
			ItemVO vo = new ItemVO(sn[i], num[i], ItemBindType.Bind.value());
			listItemVO.add(vo);
		}
		return canAdd(humanObj, listItemVO);
	}
	
	/**
	 * 判断是否可以添加物品 多个物品 多个配置 给东西 
	 * 比如 1小时内完成这个任务可以获得额外的东西
	 * @param humanObj
	 * @param sn
	 * @param num
	 * @return
	 */
	public boolean canAdd(HumanObject humanObj, int[] sn, int[] num, int[] sn2, int[] num2){
		// 判断物品类型和数量是否为空或个数不等
		if (sn == null || num == null || sn.length != num.length || sn2.length != num2.length) {
			Log.table.error("配表有误！sn.length != num.length ,sn = {}, num = {},sn2 = {}, num2 = {}", sn, num, sn2, num2);
			return false;
		}
		List<ItemVO> listItemVO = new ArrayList<ItemVO>();
		for (int i = 0; i < sn.length; i++) {
			ItemVO vo = new ItemVO(sn[i], num[i], ItemBindType.Bind.value());
			listItemVO.add(vo);
		}
		for (int i = 0; i < num2.length; i++) {
			ItemVO vo = new ItemVO(sn2[i], num2[i], ItemBindType.Bind.value());
			listItemVO.add(vo);
		}
		return canAdd(humanObj, listItemVO);
	}
	
	/**
	 * 判断是否可以添加物品 单个物品
	 * @param humanObj
	 * @param sn
	 * @param num
	 * @return
	 */
	public boolean canAdd(HumanObject humanObj, int sn, int num) {
		ItemVO vo = new ItemVO(sn, num);

		return canAdd(humanObj, Utils.ofList(vo));
	}
	
	/**
	 * 判断是否可添加
	 * @param humanObj
	 * @return
	 */
	public boolean canAdd(HumanObject humanObj, List<ItemVO> itemVOs) {
		// 当前剩余格子的数量
		int numRest = getNumRest(humanObj);
		// 新增物品需要使用的格子数
		int numNeed = 0;

		// 玩家背包
		ItemPack pack = getPack(humanObj);

		// 遍历要添加的内容 获取添加此种物品需要的格子数
		for (ItemVO itemVO : itemVOs) {
			if (itemVO.num <= 0) {// 数量错误
				Log.item.info("数量错误：ItemManager.canAdd error in sn={},num={}", itemVO.sn, itemVO.num);
				return false;
			}
			ConfItem confItem = ConfItem.get(itemVO.sn);
			if (confItem == null) {// 物品配置错误
				Log.table.error("ConfItem配表错误，no find sn={}", itemVO.sn);
				return false;
			}
			if (confItem.itemType == 0)
				continue;// 属于货币类型不进行计算

			// 非叠加物品 有多少个就需要多少个格子
			int need = 0;
			if (confItem.maxNum <= 1) {
				need = itemVO.num;

				// 可叠加物品 部分新增需求可以与之前的物品叠加
			} else {
				// 查看已有同类(SN+绑定)物品，可以用于叠加的数量
				int rest = 0;
				for (Item item : pack.findBySn(itemVO.sn)) {
					rest += confItem.maxNum - item.getNum();
				}

				// 如果要添加的数量大于可叠加的数量 那么就还需要占用新格子
				if (itemVO.num > rest) {
					need = (itemVO.num - rest) / confItem.maxNum + 1;
				}
			}

			// 累加需要的格子数
			numNeed += need;

			// 不用都叠加完毕 只要过程中已经超过可用格子数了 就可以直接返回了
			if (numNeed > numRest) {
				humanObj.sendSysMsg(10);
				return false;// 背包空间不足
			}
		}

		return true;
	}
	
	/**
	 * 单个物品 判断是否可添加
	 * @param humanObj
	 * @param itemVO
	 * @return
	 */
	public ReasonResult canAdd(HumanObject humanObj, ItemVO itemVO) {
		int numRest = getNumRest(humanObj);
		int numNeed = 0;
		ItemPack pack = getPack(humanObj);

		if (itemVO.num <= 0) {// 数量错误
			Log.item.info("数量错误：ItemManager.canAdd error in sn={},num={}",
					itemVO.sn, itemVO.num);
			return new ReasonResult(false);
		}
		ConfItem confItem = ConfItem.get(itemVO.sn);
		if (confItem == null) {
			Log.table.error("ConfItem配表错误，no find sn={}", itemVO.sn);
			return new ReasonResult(false);
		}
		if (confItem.itemType == 0)
			return new ReasonResult(true);
		
		int need = 0;
		if (confItem.maxNum <= 1) {
			need = itemVO.num;
		} else {
			int rest = 0;
			for (Item item : pack.findBySn(itemVO.sn)) {
				rest += confItem.maxNum - item.getNum();
			}
			if (itemVO.num > rest) {
				need = (itemVO.num - rest) / confItem.maxNum + 1;
			}
		}
		numNeed += need;
		if (numNeed > numRest) {
			humanObj.sendSysMsg(10);
			return new ReasonResult(false);// 背包空间不足
		}
		return new ReasonResult(true);
	}
	
	/**
	 * 玩家的背包添加物品信息
	 */
	@Listener(EventKey.ItemChangeAdd)
	public void _listener_ItemChangeAdd(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		Item item = Utils.getParamValue(param, "item", null);
		// 物品配置
		ConfItem confItem = ConfItem.get(item.getSn());
		if(confItem == null) {
			Log.table.error("can't find ConfItem sn= {}",item.getSn());
			return;
		}
		if(confItem.autoUse == 1 && item != null){
			ItemBagManager.inst().itemUse(humanObj, item.getId(), item.getNum(), LogSysModType.BagItemUse);
		}
	}

}
