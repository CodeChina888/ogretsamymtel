package game.worldsrv.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.Port;
import core.support.ManagerBase;
import core.support.Param;
import core.support.SysException;
import game.msg.Define.DDropItem;
import game.msg.Define.EContainerType;
import game.msg.Define.EPartnerGetType;
import game.msg.MsgItem.SCBagUpdate;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfItem;
import game.worldsrv.drawCard.DrawCardManager;
import game.worldsrv.drop.DropBag;
import game.worldsrv.drop.DropManager;
import game.worldsrv.entity.EntityItem;
import game.worldsrv.enumType.ItemPackType;
import game.worldsrv.enumType.ItemReduceType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.partner.PartnerManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.rune.RuneManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.LogOpUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.tower.TowerManager;

public abstract class ItemManager extends ManagerBase {

	/**
	 * 获取背包容器
	 * @param humanObj
	 * @return
	 */
	public abstract ItemPack getPack(HumanObject humanObj);
	
	/**
	 * 获得物品所属容器(ItemBody or ItemBag or ItemStorage)
	 * @return
	 */
	protected abstract ItemPackType getPackType();

	/**
	 * 获得物品所属容器类型(Body or Bag or Storage)
	 * @return
	 */
	protected abstract EContainerType getContainerType();
	
	/**
	 * 根据背包类型 创建一个物品
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T extends Item> T newItem() {
		try {
			return (T) getPackType().getPack().newInstance();
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 增加物品：批量
	 * @param humanObj
	 * @param itemBagVOs 入包物品列表
	 * @return 背包变化
	 */
	public ItemChange add(HumanObject humanObj, List<ItemVO> itemBagVOs, LogSysModType log) {
		// 物品变化信息
		ItemChange itemChangeVO = new ItemChange();
		List<ProduceVo> itemProduceItem = new ArrayList<ProduceVo>();
		// 遍历添加
		for (ItemVO itemBagVO : itemBagVOs) {
			// 验证背包是否能够放下
			boolean ret = ItemBagManager.inst().canAdd(humanObj, itemBagVO.sn, itemBagVO.num);
			if (ret) {
				ItemChange change = add(humanObj, itemBagVO.sn, itemBagVO.num, log);
				itemChangeVO.merge(change);
			} else {
				ProduceVo pv = new ProduceVo(itemBagVO.sn, itemBagVO.num);
				itemProduceItem.add(pv);
			}
		}
		if (!itemProduceItem.isEmpty()) {
			// 特殊邮件内容：{MailTemplate.sn}
//			String detail = "{" + EMailType.BagFull.value() + "}";
//			// 发送邮件到玩家
//			MailManager.inst().sendSysMail(humanObj.getHumanId(), ParamManager.mailMark, detail, itemProduceItem);
		}
		// 发送变化消息
		sendChangeMsg(humanObj.getHumanObj(), itemChangeVO);
		return itemChangeVO;
	}
	
	/**
	 * 增加物品：批量
	 * @param sn 物品SN
	 * @param num 数量
	 */
	public ItemChange add(HumanObject humanObj, int sn[], int num[], LogSysModType log) {
		ItemChange itemChangeVO = new ItemChange();
		for (int i = 0; i < num.length; i++) {
			itemChangeVO.merge(add(humanObj, sn[i], num[i], log));
		}
		return itemChangeVO;
	}

	/**
	 * 增加物品：增加单个
	 * @param sn 物品SN
	 * @param num 数量
	 */
	public ItemChange add(HumanObject humanObj, int sn, int num, LogSysModType log) {
		// 背包变化
		ItemChange itemChangeVO = new ItemChange();
		itemChangeVO.newly = true;
		
		//出现异常，直接返回
		if(sn < RewardHelper.MIN_ITEM || sn > RewardHelper.MAX_ITEM) {
			Log.item.error("不可以在增加货币的地方，增加钱{}", sn);
			return itemChangeVO;
		}

		// 增加物品数量
		int count = num;
		// 增加物品数量 0 的异常
		if (count <= 0) 
			return itemChangeVO;
		
		// 物品配置
		ConfItem confItem = ConfItem.get(sn);
		// 物品配置错误
		if (confItem == null) {
			Log.table.error("ConfItem配表错误，no find sn={}", sn);
			return itemChangeVO;
		}
		
		// 特殊类型处理
		int itemType = confItem.itemType;
		if (itemType == ItemTypeKey.partner) {
			this.process_PartnerType(humanObj, confItem, num, itemChangeVO);
			return itemChangeVO;
		} else if (itemType == ItemTypeKey.rune) {
			this.process_RuneType(humanObj, confItem, num);
			itemChangeVO.addProduce(confItem.sn, num);
			return itemChangeVO;
		} else if (itemType == ItemTypeKey.towerScore) {
			this.process_TowerScoreType(humanObj, num);
			itemChangeVO.addProduce(confItem.sn, num);
			return itemChangeVO;
		}
		
		// 自动礼包处理
		if (confItem.autoUse == 1) {
			itemChangeVO = getItemChangeByItemUse(humanObj, confItem, num);
			return itemChangeVO;
		}
		
		// 获取玩家背包
		ItemPack pack = getPack(humanObj);
		if(confItem.maxNum > 0){//如果是可以堆叠物品
			// 查找已经存在的
			List<Item> is = pack.findBySn(sn);
			if(is.size() > 0){
				Item item = is.get(0);
				item.setNum(item.getNum() + num);
				
				// 记录修改列表
				itemChangeVO.modItem(item, num);
				
			} else {	//如果没有，就建立一个
				Item item = create(humanObj.id, humanObj.name, sn, count);
				item.setNum(num);
				//加入背包
				item.setNewly(true);
				item.setFrom(EContainerType.Bag);
				item.setContainer(pack.getContainer().getNumber());
				item.setPosition(pack.getPositionFree());
				pack.add(item);
				
				// 记录添加列表
				itemChangeVO.addItem(item);
			}
		} else {//如果是不可以堆叠，每个都建立一个
			for (int i = 0; i < num; i++) {
				Item item = create(humanObj.id, humanObj.name, sn, 1);
				//加入背包
				item.setNewly(true);
				item.setFrom(EContainerType.Bag);
				item.setContainer(pack.getContainer().getNumber());
				item.setPosition(pack.getPositionFree());
				pack.add(item);
				
				// 记录添加列表
				itemChangeVO.addItem(item);
			}
		}
		
		// 发送变化消息
		sendChangeMsg(humanObj, itemChangeVO);
		
		// 发送物品变动事件
		itemChangeVO.fireChangeEvent(humanObj);
		
		//添加获得日志
		LogOpUtils.LogGain(humanObj.getHuman(), log, sn, num);
		
		return itemChangeVO;
	}
	
	/**
	 * 创建物品
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Item> T create(long ownerId, String ownerName, int sn, int num) {
		// 配置信息
		ConfItem confItem = ConfItem.get(sn);
		if (confItem == null) {
			Log.table.error("ConfItem配表错误，no find sn={}", sn);
			return null;
		}
		
		// 创建物品
		Item item = _createBase(ownerId, ownerName, confItem);
		item.setNum(num);
		item.setLife(ItemBodyManager.inst().getLife(confItem.life));// -1代表没有限制时间
		
		return (T)item;
	}

	/**
	 * 创建物品基本数据 不包括数量 仅仅是基本模型的创建 - 本函数不应该被随意调用 创建物品请使用create函数 -
	 * 进行背包级别(bag/mail/store等)扩展时，请覆盖本函数。
	 * 进行物品类型级别(equip/ruby/partner等)扩展时，请监听ITEM_INIT事件。
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Item> T _createBase(long ownerId, String ownerName, ConfItem confItem) {
		// 物品基本信息
		Item item = newItem();
		item.setId(Port.applyId());
		item.setSn(confItem.sn);
		item.setType(confItem.itemType);
		item.setOwnerId(ownerId);
		item.setOwnerName(ownerName);
		item.persist();// 保存
		// 抛出物品创建事件
		Event.fire(EventKey.ItemCreate, "item", item);

		return (T) item;
	}

	/**
	 * 发送物品变化消息
	 * @param humanObj
	 * @param itemChange
	 */
	public void sendChangeMsg(HumanObject humanObj, ItemChange itemChange) {
		SCBagUpdate.Builder updateMsg = itemChange.getUpdateMsg();
		if (updateMsg == null) {
			return;
		}
		humanObj.sendMsg(updateMsg);
	}

	/**
	 * 交换物品所在容器，不允许同个容器内交换
	 * @param humanObj 
	 * @param packSrc 物品当前容器
	 * @param packDes 物品目标容器
	 * @param itemSrc 当前容器存放的道具，不可为null
	 * @param itemDes 目标容器存放的道具，null代表目标是空格子
	 */
	public void exchange(HumanObject humanObj, ItemPack packSrc, ItemPack packDes, Item itemSrc, Item itemDes) {
		if (itemSrc == null) {// 当前容器不能为null
			return;
		}
		if (packSrc.getContainer() == packDes.getContainer()) {// 当前容器不能为目标容器
			return;
		}

		Item itemSrcCopy = null;// 变更容器后的
		Item itemDesCopy = null;// 变更容器
		// 发送改动消息
		ItemChange change = new ItemChange();
		if (itemSrc != null) {
			itemSrc.setFrom(packSrc.getContainer());// 记录来自哪个容器的改变
			itemSrc.setContainer(packDes.getContainer().getNumber());// 变更容器
			// 一定要立即执行，否则快速脱穿，脱穿操作会有异常
			itemSrc.update(true);
			itemSrcCopy = clone(itemSrc, packDes.getContainer());// 克隆至目标
			if (itemSrcCopy != null)
				itemSrcCopy.setId(itemSrc.getId());
			change.modItem(itemSrc, 1);
		}
		if (itemDes != null) {
			itemDes.setFrom(packDes.getContainer());// 记录来自哪个容器的改变
			itemDes.setContainer(packSrc.getContainer().getNumber());// 变更容器
			// 一定要立即执行，否则快速脱穿，脱穿操作会有异常
			itemDes.update(true);
			itemDesCopy = clone(itemDes, packSrc.getContainer());// 克隆至目标
			if (itemDesCopy != null)
				itemDesCopy.setId(itemDes.getId());
			change.modItem(itemDes, 1);
		}
		sendChangeMsg(humanObj.getHumanObj(), change);

		if (itemSrc != null) {
			itemSrc.remove();
			packSrc.remove(itemSrc.getId());
		}
		if (itemDes != null) {
			itemDes.remove();
			packDes.remove(itemDes.getId());
		}
		
		// 当前容器->目标容器
		if (itemSrcCopy != null) {// src-》des
			if (packDes.getContainer() == EContainerType.Bag) {// 放入背包的话，要设置格子位置
				if(itemSrcCopy.getType() == ItemTypeKey.rune){
					itemSrcCopy.setPosition(0);
				}else{
					itemSrcCopy.setPosition(packDes.getPositionFree());
				}
			} else {
				// 时装穿戴：时装从背包——>身上
				if(itemSrcCopy.getType() == ItemTypeKey.fashion){
					itemSrcCopy.setPosition(1);
				}
			}
			itemSrcCopy.setOwnerId(humanObj.id);
			itemSrcCopy.setOwnerName(humanObj.name);
			itemSrcCopy.persist();
			packDes.add(itemSrcCopy);
		}
		// 目标容器->当前容器
		if (itemDesCopy != null) {// des-》src
			if (packSrc.getContainer() == EContainerType.Bag) {// 放入背包的话，要设置格子位置
				if(itemDesCopy.getType() == ItemTypeKey.rune){
					itemDesCopy.setPosition(0);
				} else if(itemDesCopy.getType() == ItemTypeKey.fashion){
					itemDesCopy.setPosition(0);
				} else{
					itemDesCopy.setPosition(packSrc.getPositionFree());
				} 
			}
			itemDesCopy.setOwnerId(humanObj.id);
			itemDesCopy.setOwnerName(humanObj.name);
			itemDesCopy.persist();
			packSrc.add(itemDesCopy);
		}
	}

	/**
	 * 容器间移动物品 - 本函数没有配备can函数，请利用canMoveIn canMoveOut来继续验证。
	 * @param packSrc 物品当前存放容器
	 * @param packDes 物品目标容器
	 * @param itemId 物品Id
	 * @optional 可选参数 boolean:sendMsgToSrc 是否向原容器发送消息协议 默认 true
	 *           boolean:sendMsgToDes 是否向目标容器发送消息协议 默认 true
	 */
	public void moveTo(HumanObject unitObj, ItemPack packSrc, ItemPack packDes, long itemId, int num, Object... params) {
		return;// 不使用该方法了，改用 exchange
		/*
		 * //解析参数 Param param = new Param(params); //向原容器发送消息协议，默认true boolean
		 * sendMsgToSrc = Utils.getParamValue(param, "sendMsgToSrc", true);
		 * //向目标容器发送消息协议，默认true boolean sendMsgToDes =
		 * Utils.getParamValue(param, "sendMsgToDes", true); //显示文字通知，默认true
		 * boolean sendInform = Utils.getParamValue(param, "sendInform", true);
		 * //发送改动消息 ItemChange change = new ItemChange(); Item itemSrc =
		 * packSrc.get(itemId);//原物品
		 * itemSrc.setContainer(packDes.getContainer().getNumber());//变更容器
		 * change.mods.add(itemSrc); sendChangeMsg(unitObj.getHumanObj(),
		 * change, packSrc.getContainer()); //发送文字提示消息 if(sendInform) { ConfItem
		 * confItem = ConfItem.get(itemSrc.getSn()); //你获得了物品 {}*{}
		 * unitObj.getHumanObj().sendSysMsg(47,
		 * "C",confItem.sn,"B",itemSrc.getNum()); } Item itemDes =
		 * clone(itemSrc);//克隆至目标 if(num <= 0 || num > itemSrc.getNum()) { num =
		 * itemSrc.getNum(); } //移出并删除原物品 itemSrc.setNum(itemSrc.getNum() -
		 * num); itemDes.setNum(num); //设置Id if(itemSrc.getNum() <= 0) {
		 * itemDes.setId(itemSrc.getId()); itemSrc.remove();
		 * packSrc.remove(itemSrc.getId()); } else {
		 * itemDes.setId(Port.applyId()); } if(packDes.getType() ==
		 * ItemPackTypeKey.body) { //如果是装备那么可以是玩家或者是武将
		 * itemDes.setHumanId(unitObj.id); } else if(packDes.getType() ==
		 * ItemPackTypeKey.bag) { //如果是背包那么只能是玩家
		 * itemDes.setHumanId(unitObj.getHumanId()); //设置新容器中的位置
		 * itemDes.setPosition(packDes.getPositionFree()); } //持久化并设置关系
		 * itemDes.persist(); packDes.add(itemDes);
		 */
	}

	/**
	 * 能否删除 对于业务上经常出现的仅使用非绑物品接口进行支持
	 * @param humanObj
	 * @param onlyNoBind 只删除不绑定物品
	 * @param sn 物品SN
	 * @param num 物品数量
	 * @return ReasonResult
	 */
	public boolean canRemove(HumanObject humanObj, boolean onlyNoBind, int sn, int num) {
		ItemReduceType type = onlyNoBind ? ItemReduceType.ONLY_BIND_NO : ItemReduceType.FIRST_BIND_YES;

		return canRemove(humanObj, sn, num, type);
	}

	/**
	 * 能否删除
	 * @param humanObj
	 * @param sn
	 * @param num
	 * @return ReasonResult
	 */
	public boolean canRemove(HumanObject humanObj, int sn, int num) {
		return canRemove(humanObj, sn, num, ItemReduceType.FIRST_BIND_YES);
	}

	/**
	 * 能否删除
	 * @param sn 物品SN
	 * @param num 物品数量
	 * @param typeKey 删除规则 仅绑定 仅非绑 优先绑定
	 * @return ReasonResult
	 */
	public boolean canRemove(HumanObject humanObj, int sn, int num, ItemReduceType typeKey) {
		ItemPack pack = getPack(humanObj);

		// 1 检查数量是否足够
		// 1.1 获取符合删除规则的物品数量
		int count = 0;
		switch (typeKey) {
		// 只移除绑定
			case ONLY_BIND_YES : {
				count += pack.getNumBySnBind(sn, 1);
			}
				break;

			// 只移除非绑
			case ONLY_BIND_NO : {
				count += pack.getNumBySnBind(sn, 0);
			}
				break;

			// 优先移除绑定 取出全部
			default : {
				count += pack.getNumBySn(sn);
			}
		}
		// 1.2 检查数量
		if (count < num) {
			// 返回错误提示
			humanObj.sendSysMsg(11, sn);// {道具sn}不足，无法进行操作！
			return false;
		}

		return true;
	}

	/**
	 * 能否删除
	 * @param humanObj
	 * @param num
	 * @return ReasonResult
	 */
	public boolean canRemove(HumanObject humanObj, Item item, int num) {
		// 检查数量
		if (item.getNum() < num) {
			// 返回错误提示
			humanObj.sendSysMsg(11, item.getSn());// {道具sn}不足，无法进行操作！
			return false;
		}
		return true;
	}

	/**
	 * 能否删除 对于业务上经常出现的仅使用非绑物品接口进行支持
	 * @param humanObj
	 * @param onlyNoBind
	 * @param sn
	 * @param num
	 * @param params
	 * @return
	 */
	public ItemChange remove(HumanObject humanObj, boolean onlyNoBind, int sn, int num, Object... params) {
		ItemReduceType type = onlyNoBind ? ItemReduceType.ONLY_BIND_NO : ItemReduceType.FIRST_BIND_YES;

		return remove(humanObj, sn, num, type, params);
	}

	/**
	 * 删除物品
	 * @param humanObj
	 * @param sn
	 * @param num
	 * @param params
	 * @return
	 */
	public ItemChange remove(HumanObject humanObj, int sn, int num, Object... params) {
		return remove(humanObj, sn, num, ItemReduceType.FIRST_BIND_YES, params);
	}

	/**
	 * 删除物品（指定SN，不指定Item，例如：吃药就是删除指定SN的药品）
	 * @param humanObj
	 * @param sn
	 * @param num
	 * @param typeKey
	 * @param params
	 * @return ItemChange
	 */
	public ItemChange remove(HumanObject humanObj, int sn, int num, ItemReduceType typeKey, Object... params) {
		// 物品修改信息
		ItemChange itemChangeVO = new ItemChange();
		EContainerType eContainer = getContainerType();
		// 验证
		boolean ret = canRemove(humanObj, sn, num, typeKey);
		if (!ret || num == 0) {// 数量不足或删除0个
			Log.item.error("删除物品:HumanId={},Container={},ItemSn={}", humanObj.id, eContainer.name(), sn);
			return itemChangeVO;
		}

		// 解析参数
		Param param = new Param(params);
		// 默认值-发送物品变动消息
		boolean sendMsg = Utils.getParamValue(param, "sendMsg", true);

		// 获取容器：背包or身上的装备
		ItemPack pack = getPack(humanObj);

		// 取出可以删除的物品
		List<Item> items = new ArrayList<>();
		switch (typeKey) {
			case ONLY_BIND_YES : {// 只移除绑定
				items = pack.findBySnBind(sn, 1);
			}
				break;
			case ONLY_BIND_NO : {// 只移除非绑定
				items = pack.findBySnBind(sn, 0);
			}
				break;
			default : {// 优先移除绑定，取出全部
				items = pack.findBySn(sn);
			}
		}

		// 进行排序：1优先删除绑定的，2优先删除叠加数量少的
		Collections.sort(items, new Comparator<Item>() {
			@Override
			public int compare(Item a, Item b) {
				if (a.getBind() == b.getBind()) {
					return a.getNum() - b.getNum();
				} else {
					return b.getBind() - a.getBind();
				}
			}
		});

		// 进行删除
		int numNeed = num; // 需要删除的数量
		for (Item i : items) {
			if (i.getNum() == 0)
				continue;

			i.setFrom(eContainer);// 记录来自哪个容器的改变
			// 当前物品可删除数量
			int n = Math.min(i.getNum(), numNeed);
			// 剩余需要删除的数量
			numNeed = numNeed - n;
			// 减少数量
			i.setNum(i.getNum() - n);

			// 物品需要被删除
			if (i.getNum() <= 0) {
				i.remove();
				pack.remove(i.getId());
				itemChangeVO.delItem(i);// 记录被删除的物品
			} else {// 物品仍然有剩余 那么说明已经不用在继续删除了
				itemChangeVO.modItem(i, n);// 记录被修改的物品
			}
			if (numNeed == 0) {// 已无需再删除了
				break;
			}
		}

		// 发送物品变动信息
		if (sendMsg) {
			sendChangeMsg(humanObj, itemChangeVO);
		}

		// 发送物品变动事件
		itemChangeVO.fireChangeEvent(humanObj);

		return itemChangeVO;
	}

	/**
	 * 删除物品（指定Item，不指定SN，例如：卖物品就是删除指定的物品）
	 * @param humanObj
	 * @param item
	 * @param num
	 * @param params
	 * @return ItemChange
	 */
	public ItemChange remove(HumanObject humanObj, Item item, int num, Object... params) {
		// 物品修改信息
		ItemChange itemChangeVO = new ItemChange();
		EContainerType eContainer = getContainerType();
		item.setFrom(eContainer);// 记录来自哪个容器的改变
		// 验证
		boolean ret = canRemove(humanObj, item, num);
		if (!ret || num == 0) {// 数量不足或删除0个
			Log.item.error("删除物品:HumanId={},Container={},ItemSn={}", humanObj.id, eContainer.name(), item.getSn());
			return itemChangeVO;
		}

		// 解析参数
		Param param = new Param(params);
		// 默认值-发送物品变动消息
		boolean sendMsg = Utils.getParamValue(param, "sendMsg", true);

		// 获取容器：背包or身上的装备
		ItemPack pack = getPack(humanObj);
		item.setNum(item.getNum() - num);// 修改数量

		// 物品需要被删除
		if (item.getNum() <= 0) {
			item.remove();
			pack.remove(item.getId());
			itemChangeVO.delItem(item);// 记录被删除的物品
		} else {// 物品仍然有剩余 那么说明已经不用在继续删除了
			itemChangeVO.modItem(item, num);// 记录被修改的物品
		}

		// 发送物品变动信息
		if (sendMsg) {
			sendChangeMsg(humanObj, itemChangeVO);
		}

		// 发送物品变动事件
		itemChangeVO.fireChangeEvent(humanObj);

		return itemChangeVO;
	}

	/**
	 * 删除多个物品（指定Item，不指定SN，例如：卖物品就是删除指定的物品）
	 * @param humanObj
	 * @param num
	 * @param params
	 * @return
	 */
	public ItemChange remove(HumanObject humanObj, List<Item> items, int num, Object... params) {
		// 物品修改信息
		ItemChange itemChangeVO = new ItemChange();
		boolean changed = false;

		for (Item item : items) {
			EContainerType eContainer = getContainerType();
			item.setFrom(eContainer);// 记录来自哪个容器的改变
			// 验证
			boolean ret = canRemove(humanObj, item, num);
			if (!ret || num == 0) {// 数量不足或删除0个
				Log.item.error("删除物品:HumanId={},Container={},ItemSn={}", humanObj.id, eContainer.name(), item.getSn());
				continue;
			}

			// 解析参数
			Param param = new Param(params);
			// 默认值-发送物品变动消息
			boolean sendMsg = Utils.getParamValue(param, "sendMsg", true);
			if (sendMsg) {
				changed = true;
			}

			// 获取容器：背包or身上的装备
			ItemPack pack = getPack(humanObj);
			item.setNum(item.getNum() - num);// 修改数量

			// 物品需要被删除
			if (item.getNum() <= 0) {
				item.remove();
				pack.remove(item.getId());
				itemChangeVO.delItem(item);// 记录被删除的物品
			} else {// 物品仍然有剩余 那么说明已经不用在继续删除了
				itemChangeVO.modItem(item, num);// 记录被修改的物品
			}
		}

		// 发送物品变动信息
		if (changed) {
			sendChangeMsg(humanObj, itemChangeVO);
		}
		// 发送物品变动事件
		itemChangeVO.fireChangeEvent(humanObj);
		return itemChangeVO;
	}

	/**
	 * 创建并复制一个目标对象的副本
	 */
	@SuppressWarnings("unchecked")
	public <T> T clone(Item itemSrc) {
		Item itemDes = newItem();
		// 复制指定item的所有属性到目标item
		cloneItem(itemSrc, itemDes);
		return (T) itemDes;
	}

	/**
	 * 创建并复制一个目标对象的副本
	 */
	@SuppressWarnings("unchecked")
	public <T> T clone(Item itemSrc, EContainerType type) {
		// 创建对象
		try {
			Item itemDes = null;
			if (type == EContainerType.Bag) {
				itemDes = ItemBagManager.inst().getPackType().getPack().newInstance();
			} else if (type == EContainerType.Body) {
				itemDes = ItemBodyManager.inst().getPackType().getPack().newInstance();
			}
			// 复制指定item的所有属性到目标item
			cloneItem(itemSrc, itemDes);
			return (T) itemDes;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 复制指定item的所有属性到目标item
	 * @param itemSrc 指定item
	 * @param itemDes 目标item
	 */
	@SuppressWarnings("deprecation")
	private void cloneItem(Item itemSrc, Item itemDes) {
//		for (EntityItem item : EntityItem.values()) {// 复制属性
//			Object value = Utils.fieldRead(itemSrc, item.name());
//			Utils.fieldWrite(itemDes, item.name(), value);
//		}
		for (EntityItem type : EntityItem.values()) {
			Object value = Utils.invokeMethodGet(itemSrc, type.name());
			if (value != null) {
				//System.out.println("itemSrc name="+type.name()+" value="+value);
				Utils.invokeMethodSet(itemDes, type.name(), value);
			}
		}
	}
	
	//////////////////////////////////
	// 使用物品，获得使用后得到的物品
	//////////////////////////////////
	public ItemChange getItemChangeByItemUse(HumanObject humanObj, ConfItem confItem, int num) {
		ItemChange itemChange = new ItemChange();
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if (ItemTypeKey.isSameType(ItemTypeKey.useForDropInfo, confItem.itemType)) {
			int dropId = Utils.intValue(confItem.param[0]);
			for (int i = 0; i < num; i++) {
				DropBag dropBag = DropManager.inst().getItem(humanObj, dropId);
				//设置必出
				DrawCardManager.inst().setBagWill(humanObj,dropBag,dropId);
				int[] items = dropBag.getItemSn();
				int[] nums = dropBag.getItemNum();
				for (int j = 0; j < items.length; j++) {
					//sn和数量
					int dropItemSn = items[j];
					int dropItemNum = nums[j];
					ProduceVo vo = new ProduceVo(dropItemSn, dropItemNum);
					result.add(vo);
					itemChange.merge(RewardHelper.reward(humanObj, dropItemSn, dropItemNum, LogSysModType.BagItemUse));
				}
			}
		}else{
			// 获得所有物品
			result = ItemBagManager.inst().getProduceVoList(confItem.param, num);
			// 判断是否可以给
			boolean ret = ProduceManager.inst().canGiveProduceItem(humanObj, result);
			if (ret) {
				// 叠加物品
				result = ProduceManager.inst().mergeProduce(result);
				// 实际给物品
				itemChange = ProduceManager.inst().giveProduceItem(humanObj, result, LogSysModType.BagItemUse);
			}
		}
		return itemChange;
	}
	
	//////////////////////////////////
	// 新增物品，特殊类型特殊处理
	//////////////////////////////////
	/**
	 * 伙伴类型
	 * @param humanObj
	 * @param confItem 配置
	 * @param num 获得数量
	 * @param itemChangeVO 存储获得道具的VO
	 */
	private void process_PartnerType(HumanObject humanObj, ConfItem confItem, int num, ItemChange itemChangeVO) {
		// 配置参数正常是有三个[0]伙伴sn，[1]伙伴碎片sn，[2]伙伴碎片合成数量
		if (confItem.param.length > 1) {
			int partnerSn = Integer.valueOf(confItem.param[0]);
			int recruitCount = 0;
			boolean hasPartner = PartnerManager.inst().isExistPartnerSn(humanObj, partnerSn);
			if (!hasPartner) {
				PartnerManager.inst()._send_SCParnterDrop(humanObj, partnerSn,EPartnerGetType.UseItem);
				recruitCount++;
				itemChangeVO.addProduce(confItem.sn, recruitCount);
				// 获得数量-1
				num--;
				// 刚招募，则变成已经拥有该伙伴
				hasPartner = true;
			}
			// 拥有该伙伴，分解
			List<DDropItem> dropItems = PartnerManager.inst().decomposePartner(humanObj, partnerSn, num);
			// 伙伴碎片
			for (DDropItem dDropItem : dropItems) {
				if (dDropItem.getItemNum() > 0) {
					itemChangeVO.addProduce(dDropItem.getItemSn(), dDropItem.getItemNum());
				}
			}	
		}
	}
	/**
	 * 符文（命格）类型
	 * @param humanObj
	 * @param conf 配置
	 * @param num 获得数量
	 */
	private void process_RuneType(HumanObject humanObj, ConfItem conf, int num) {
		List<ProduceVo> itemProduce = new ArrayList<>();
		itemProduce.add(new ProduceVo(conf.sn, num));
//		// 旧版本，发送道具奖励符文邮件
//		String detail =  "{ " + EMailType.MailItemToRune_VALUE + " }";
//		MailManager.inst().sendSysMail(humanObj.getHumanId(), ParamManager.mailMark, detail, itemProduce, Time.DAY * 1000);
		
		// 当前版本直接领取，超过上限不作处理，直接当作溢出爆掉
		RuneManager.inst().process_ItemToNewRune(humanObj, itemProduce);
		
	}
	/**
	 * 爬塔积分类型
	 * @param humanObj
	 * @param num 获得数量
	 */
	private void process_TowerScoreType(HumanObject humanObj, int num) {
		TowerManager.inst().process_ItemToTowerScore(humanObj, num);
	}

}
