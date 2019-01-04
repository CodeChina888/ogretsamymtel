package game.worldsrv.item;

import game.msg.Define.DEquip;
import game.msg.Define.EContainerType;
import game.msg.Define.EEquipRefineSLot;
import game.msg.Define.EInformType;
import game.msg.Define.EModeType;
import game.msg.Define.EMoneyType;
import game.msg.MsgCommon.SCStageObjectInfoChange;
import game.msg.MsgItem.CSReinforceAllEquip2Msg;
import game.msg.MsgItem.SCBodyItemInfo;
import game.msg.MsgItem.SCEquipRefineAbandonSlotUp;
import game.msg.MsgItem.SCEquipRefineSaveSlotUp;
import game.msg.MsgItem.SCEquipRefineSlotUp;
import game.msg.MsgItem.SCEquipRefineUp;
import game.msg.MsgItem.SCReinforceAllEquip2Msg;
import game.msg.MsgItem.SCReinforceEquipMsg;
import game.msg.MsgItem.SCUpEquipMsg;
import game.worldsrv.character.CharacterObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfEquipAdvanced;
import game.worldsrv.config.ConfEquipRefine;
import game.worldsrv.config.ConfEquipStrengthen;
import game.worldsrv.config.ConfItem;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.ItemBody;
import game.worldsrv.entity.Unit;
import game.worldsrv.enumType.ItemBindType;
import game.worldsrv.enumType.ItemPackType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.inform.InformManager;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageManager;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Param;
import core.support.SysException;
import core.support.Time;
import core.support.observer.Listener;

/**
 * 身上装备管理器
 */
public class ItemBodyManager extends ItemManager {

	public static ItemBodyManager inst() {
		return inst(ItemBodyManager.class);
	}

	@Override
	public ItemPack getPack(HumanObject humanObj) {
		return humanObj.itemBody;
	}

	@Override
	protected ItemPackType getPackType() {
		return ItemPackType.Body;
	}

	@Override
	protected EContainerType getContainerType() {
		return EContainerType.Body;
	}
	
	/**
	 *创角的时候生成配置表里默认装备 
	 */
	@Listener(EventKey.HumanCreate)
	public void initDeafultHumanEquips(Param param){
		Human human = param.get("human");
		int[] initHumanEquipSnArr = ParamManager.initHumanEquip;
		ConfEquipRefine conf = null;
		if (initHumanEquipSnArr.length > 0) {
			for (int i = 0; i < initHumanEquipSnArr.length; i++) {
				int itemSn = initHumanEquipSnArr[i];// 初始化装备
				ConfItem confItem = ConfItem.get(itemSn);
				if (confItem == null) {
					Log.table.error("ConfItem配表错误，no find sn={}", itemSn);
					continue;
				}
				
				ItemBody itemBody = createBodyEquip(human, confItem.sn, confItem.itemType);
				itemBody.setReinforceLv(ParamManager.initHumanEquipLv);
				itemBody.setRefineLv(0);
				conf = ConfEquipRefine.get(ConfigKeyFormula.getEquipRefineSn(itemBody.getType(), itemBody.getRefineLv()));
				int[] slotLv = new int[conf.slotUnlock.length];
				itemBody.setRefineSlotLv(Utils.arrayIntToStr(slotLv));
				//往角色身上穿戴一件装备
//				if(null != itemBody) {
//					humanObj.itemBody.add(itemBody);
//				}
			}
		}
	}
	/**
	 * 创建一个身上装备
	 */
	private ItemBody createBodyEquip(Human human, int itemSn, int itemType) {
		ItemBody itemBody = new ItemBody();
		itemBody.setId(Port.applyId());
		itemBody.setSn(itemSn);
		itemBody.setType(itemType);
		itemBody.setOwnerId(human.getId());
		itemBody.setOwnerName(human.getName());
		itemBody.setContainer(EContainerType.Body_VALUE);
		itemBody.setPosition(itemType);
		itemBody.persist();// 持久化
		return itemBody;
	}
	
	/**
	 * 玩家其它数据加载开始：加载玩家的身上物品信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(ItemBody.tableName);
		dbPrx.findBy(false, ItemBody.K.OwnerId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanItemBody, "humanObj", humanObj);
		
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanItemBody(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanItemBody humanObj is null");
			return;
		}
		
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanItemBody records=null");
		} else {
			// 加载数据
			for (Record record : records) {
				ItemBody item = new ItemBody(record);
				humanObj.itemBody.add(item);
			}
			
			this._send_SCBodyItemInfo(humanObj);// 下发所有身上物品信息
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	/**
	 * 下发所有身上物品信息
	 * @param humanObj
	 */
	private void _send_SCBodyItemInfo(HumanObject humanObj) {
		SCBodyItemInfo.Builder msg = SCBodyItemInfo.newBuilder();
		ItemPack itemBodyPack = humanObj.itemBody;// 背包信息
		List<DEquip> dEquipList = new ArrayList<>();
		
		// 同步全局的equipList
		List<ItemBody> equipList = new ArrayList<>();
		for (Item item : itemBodyPack.findAll()) {
			dEquipList.add(item.getDEquip());
			equipList.add((ItemBody) item);
		}
		msg.addAllDEquipList(dEquipList);// 身上物品列表
		humanObj.sendMsg(msg);
		
		// 更新全局数据
		HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
		pxy.updateEquip(humanObj.getHumanId(), equipList);
	}
	
	/**
	 * 双武器穿装备：RPG类游戏穿装备
	 * @param humanObj
	 * @param itemId
	 * @param posion 位置从1开始
	 */
	public void _msg_CSItemEquipPutOn(HumanObject humanObj, long itemId, int posion) {
		Human human = humanObj.getHuman();
		Item item = humanObj.itemBag.get(itemId);
		if (item == null) {
			return;
		}
		if (posion < 1) {
			return;
		}
		
		if (isOutmoded(item.getLife())) {// 判断装备是否是过期装备
			return;
		}
		boolean isEquip = ItemTypeKey.isSameType(ItemTypeKey.equip, item.getType());// 不是装备
		boolean isFashion = ItemTypeKey.isSameType(ItemTypeKey.fashion, item.getType());// 不是时装
		if (!isEquip && !isFashion) {
			humanObj.sendSysMsg(168105);
			Log.item.error("不是装备,HumanId:{},ItemId:{},error:{}", humanObj.getHumanId(), itemId, "该物品不是装备,不能穿戴");// wgz
			return;
		}
		// 获得装备基础信息
		ConfItem confItem = ConfItem.get(item.getSn());
		if (confItem == null) {
			Log.table.error("ConfItem配表错误，no find sn={}", item.getSn());
			return;
		}
		if (human.getLevel() < confItem.needLv) {// 等级不够
			humanObj.sendSysMsg(34);
			Log.item.error("等级不够,HumanId:{},ItemId:{},error:{}", humanObj.getHumanId(), itemId, "人物等级不满足条件");
			return;
		}
		boolean isEquipWeapon = ItemTypeKey.isSameType(ItemTypeKey.equipWeapon, item.getType());// 判断是否是武器
		if (!isEquipWeapon && posion == 2) {
			posion = 1;// 暂时只有双武器
		}
		if (confItem.profession != human.getProfession() && confItem.profession != 0) {// 职业和装备不符，0代表所有职业
			humanObj.sendSysMsg(168104);
			Log.item.error("职业和装备不符,HumanId:{},ItemId:{},error:{}", humanObj.getHumanId(), human.getId(), "职业不符，无法装备");
			return;
		}
		item.setBind(ItemBindType.Bind.value());// 穿上前设置绑定
		Item itemBodyed = null;
		if (isFashion) {// 时装的就走类型
			itemBodyed = getByType(humanObj, confItem.itemType);// 指定位置是否已装备物品
		} else {// 装备就走类型和位置
			itemBodyed = getByPos(humanObj, confItem.itemType, posion);// 指定位置是否已装备物品
		}
		item.setPosition(posion);// 多武器切换功能的时候需要 这里设置位置
		exchange(humanObj, ItemBagManager.inst().getPack(humanObj), getPack(humanObj), item, itemBodyed);
		//此功能已经过时了wgz
//		if (itemBodyed != null && isequipWeapon) {// 如果是武器
//			takeOffSkill(unitObj, itemBodyed.getSn(), posion);// 根据装备脱不属于的技能
//		}
		if (item.getLife() > 0) {
			HumanManager.inst().openHumanLifeEquip(humanObj);// 登录时开启玩家限时装备规则
		}

		//int elementSn = item.getElementSn();
		//int elementType = item.getElementType();
		
		/*
		human.setElementSn(elementSn);// 关联元素信息表sn
		human.setElementType(elementType);// 武器所属元素类型
		*/

		humanObj.sendSysMsg(41, "F", item.getSn());
		//此功能已经过时了wgz
//		if (isequipWeapon) {// 是武器
//			HumanManager.inst().setWeaponSkillSet(humanObj, posion, confItem.type);// 根据配表更换上阵技能
//		}
		if (human != null) {
			/*
			if (isequipWeapon) {// 是武器
				human.setEquipWeaponSn(item.getSn());// 主武器sn
				human.setEquipWeaponStrthLv(item.getStrengthenLevel());// 主武器强化等级
			} else if (ItemTypeKey.isSameType(ItemTypeKey.equipArmors, confItem.type)) {// 装备-护甲
				human.setEquipClothesSn(item.getSn());
				human.setEquipClothesStrthLv(item.getStrengthenLevel());
			} else 
			*/
			if (ItemTypeKey.isSameType(ItemTypeKey.fashionWeapon, confItem.itemType)) {// 时装-武器
				human.setFashionWeaponSn(item.getSn());
			} else if (ItemTypeKey.isSameType(ItemTypeKey.fashionClothes, confItem.itemType)) {// 时装-服装
				human.setFashionSn(item.getSn());
			}
			// 给附近玩家发送变化信息
			SCStageObjectInfoChange.Builder msgPutOn = SCStageObjectInfoChange.newBuilder();
			msgPutOn.setObj(humanObj.createMsg());
			StageManager.inst().sendMsgToArea(msgPutOn, humanObj.stageObj, humanObj.posNow);
		}
		// 发布使用物品事件
		Event.fire(EventKey.EquipPutOn, "humanObj", humanObj, "confItem", confItem, 
				"snItem", confItem.sn, "num", 1, "posion", posion);
		// 重新计算属性
		calc_equipProp(humanObj);
	}
	
	/**
	 * 主公穿装备
	 */
	public void _msg_CSMasterItemEquipPutOn(HumanObject humanObj, long itemId) {
		Human human = humanObj.getHuman();
		Item item = humanObj.itemBag.get(itemId);
		if (item == null) {
			return;
		}
		
		// 获得装备基础信息
		ConfItem confItem = ConfItem.get(item.getSn());
		if (confItem == null) {
			Log.table.error("ConfItem配表错误，no find sn={}", item.getSn());
			return;
		}
		//是不是 主公装备
		boolean isMasterEquip = ItemTypeKey.isSameType(ItemTypeKey.equip,confItem.itemType);
		if(!isMasterEquip){
			humanObj.sendSysMsg(168105);
			Log.item.error("不是装备,HumanId:{},ItemId:{},error:{}", humanObj.getHumanId(), itemId, "该物品不是装备,不能穿戴");
			return;
		}
//		if (unit.getLevel() < confItem.levelShow) {// 等级不够
//			humanObj.sendSysMsg(34);
//			Log.item.error("等级不够,HumanId:{},ItemId:{},error:{}", unitObj.getId(), itemId, "人物等级不满足条件");
//			return;
//		}

		item.setBind(ItemBindType.Bind.value());// 穿上前设置绑定
		Item itemBodyed = null;		
		itemBodyed = getByType(humanObj, confItem.itemType);// 指定位置是否已装备物品		
//		item.setPosition(posion);// 多武器切换功能的时候需要 这里设置位置
		exchange(humanObj, ItemBagManager.inst().getPack(humanObj), getPack(humanObj), item, itemBodyed);

		if (human != null) {
			// 给附近玩家发送变化信息
			SCStageObjectInfoChange.Builder msgPutOn = SCStageObjectInfoChange.newBuilder();
			msgPutOn.setObj(humanObj.createMsg());
			StageManager.inst().sendMsgToArea(msgPutOn, humanObj.stageObj, humanObj.posNow);
		}
		// 发布使用物品事件
		Event.fire(EventKey.EquipPutOn, "humanObj", humanObj, "confItem", confItem, 
				"snItem", confItem.sn, "num", 1);
		// 重新计算属性
		calc_equipProp(humanObj);
	}

	/**
	 * 穿装备：RPG类游戏穿装备
	 */
	public void putOnRpg(HumanObject humanObj, long itemId) {
		Human human = humanObj.getHuman();
		Item item = humanObj.itemBag.get(itemId);
		if (item == null) {
			return;
		}
		
		// 不是装备
		boolean isEquip = ItemTypeKey.isSameType(ItemTypeKey.equip, item.getType());
		boolean isFashion = ItemTypeKey.isSameType(ItemTypeKey.fashion, item.getType());
		if (!isEquip && !isFashion) {
			humanObj.sendSysMsg(168105);
			Log.item.error("不是装备,HumanId:{},ItemId:{},error:{}", humanObj.getHumanId(), itemId, "该物品不是装备,不能穿戴");
			return;
		}

		// 获得装备基础信息
		ConfItem confItem = ConfItem.get(item.getSn());

		if (confItem == null) {
			Log.table.error("ConfItem配表错误，no find sn={}", item.getSn());
			return;
		}
		// 等级不够
		if (human.getLevel() < confItem.needLv) {
			humanObj.sendSysMsg(34);
			Log.item.error("等级不够,HumanId:{},ItemId:{},error:{}", humanObj.getHumanId(), itemId, "人物等级不满足条件");
			return;
		}

		// 职业和装备不符， 0代表所有职业
		if (confItem.profession != human.getProfession() && confItem.profession != 0) {
			humanObj.sendSysMsg(168104);
			Log.item.error("职业和装备不符,HumanId:{},ItemId:{},error:{}", humanObj.getHumanId(), human.getId(), "职业不符，无法装备");
			return;
		}

		// 穿上前设置绑定
		item.setBind(ItemBindType.Bind.value());

		// 指定位置是否已装备物品
		Item itemBodyed = getByType(humanObj, confItem.itemType);
		/*
		 * boolean isTakeOff = false;//需要卸下么？ if(itemBodyed != null) {
		 * if(isEquip) {//背包是否能装下 脱下来的装备 ReasonResult result =
		 * ItemBagManager.inst().canAdd(humanObj, itemBodyed.getSn(), 1,
		 * itemBodyed.getBind()); if(!result.success){ Inform.user(humanObj.id,
		 * Inform.promptError, result.reason);
		 * Log.item.error("背包不能装下,HumanId:{},error:{}",
		 * humanObj.id,result.reason); return; } } isTakeOff = true; }
		 */
		if (human != null) {
			if (itemBodyed != null) {
				// 根据装备脱不属于的技能
				// ItemBodyManager.inst().takeOffSkill(unitObj,
				// itemBodyed.getSn());
			}
			/*
			 * boolean isTakeOff = false;//需要卸下么？ if(itemBodyed != null) {
			 * if(isEquip) {//背包是否能装下 脱下来的装备 ReasonResult result =
			 * ItemBagManager.inst().canAdd(humanObj, itemBodyed.getSn(), 1,
			 * itemBodyed.getBind()); if(!result.success){
			 * Inform.user(humanObj.id, Inform.promptError, result.reason);
			 * Log.item.error("背包不能装下,HumanId:{},error:{}",
			 * humanObj.id,result.reason); return; } } isTakeOff = true; }
			 */
			exchange(humanObj, ItemBagManager.inst().getPack(humanObj), getPack(humanObj), item, itemBodyed);
			/*
			 * if(isTakeOff) { //卸下已装备的物品 ItemBagManager.inst().moveTo(unitObj,
			 * getPack(unitObj), ItemBagManager.inst().getPack(humanObj),
			 * itemBodyed.getId(), 0, "sendInform", false); } //从背包穿上装备
			 * moveTo(unitObj, ItemBagManager.inst().getPack(humanObj),
			 * getPack(unitObj), item.getId(), 0, "sendInform", false);
			 */
			humanObj.sendSysMsg(41, "F", item.getSn());

			if (human != null) {
				// 监听用户属性变化
				// HumanInfoChange.listen((HumanObject)unitObj);
				/*
				if (ItemTypeKey.isSameType(ItemTypeKey.equipWeapon, confItem.type)) {
					human.setEquipWeaponSn(item.getSn());
					human.setEquipWeaponStrthLv(item.getStrengthenLevel());
				} else if (ItemTypeKey.isSameType(ItemTypeKey.equipArmors, confItem.type)) {// 装备-护甲
					human.setEquipClothesSn(item.getSn());
					human.setEquipClothesStrthLv(item.getStrengthenLevel());
				} else 
				*/
				if (ItemTypeKey.isSameType(ItemTypeKey.fashionWeapon, confItem.itemType)) {// 时装-武器
					human.setFashionWeaponSn(item.getSn());
				} else if (ItemTypeKey.isSameType(ItemTypeKey.fashionClothes, confItem.itemType)) {// 时装-服装
					human.setFashionSn(item.getSn());
				}
				// 给附近玩家发送变化信息
				SCStageObjectInfoChange.Builder msgPutOn = SCStageObjectInfoChange.newBuilder();
				msgPutOn.setObj(humanObj.createMsg());
				StageManager.inst().sendMsgToArea(msgPutOn, humanObj.stageObj, humanObj.posNow);
			}
			// 重新计算属性
			calc_equipProp(humanObj);

		}
	}

	/**
	 * 脱装备 //@Deprecated
	 * @param itemId
	 */
	public void _msg_CSItemEquipTakeOff(HumanObject humanObj, long itemId) {
		Human human = humanObj.getHuman();
		Item itemBody = humanObj.itemBody.get(itemId);
		if (itemBody == null) {
			return;
		}

		// 验证背包是否能够放下
		boolean ret = ItemBagManager.inst().canAdd(humanObj, itemBody.getSn(), itemBody.getNum());
		if (!ret) {
			humanObj.sendSysMsg(10);
			Log.item.error("背包不能装下,HumanId:{}", humanObj.id);
			return;
		}
		ConfItem confItem = ConfItem.get(itemBody.getSn());
		if (confItem == null) {
			Log.table.error("ConfItem配表错误，no find sn={}", itemBody.getSn());
			return;
		}
		ItemBodyManager.inst().exchange(humanObj, ItemBodyManager.inst().getPack(humanObj),
				ItemBagManager.inst().getPack(humanObj), itemBody, null);
		// ItemBagManager.inst().moveTo(unitObj, getPack(unitObj),
		// ItemBagManager.inst().getPack(humanObj), itemBody.getId(), 0,
		// "sendInform", false);

		// 设置对应的武器或者衣服sn，用于客户端显示
		Unit unit = humanObj.getUnit();
		if (humanObj.isHumanObj()) {
			human = (Human) unit;
		}

		// 如果是主角
		if (human != null) {
			/*
			if (ItemTypeKey.isSameType(ItemTypeKey.equipWeapon, confItem.type)) {
				human.setEquipWeaponSn(0);
				human.setEquipWeaponStrthLv(0);
			} else if (ItemTypeKey.isSameType(ItemTypeKey.equipArmors, confItem.type)) {// 装备-护甲
				human.setEquipClothesSn(0);
				human.setEquipClothesStrthLv(0);
			} else 
			*/
			if (ItemTypeKey.isSameType(ItemTypeKey.fashionWeapon, confItem.itemType)) {// 时装-武器
				human.setFashionWeaponSn(0);
			} else if (ItemTypeKey.isSameType(ItemTypeKey.fashionClothes, confItem.itemType)) {// 时装-服装
				human.setFashionSn(0);
			}
			// //给附近玩家发送变化信息
			SCStageObjectInfoChange.Builder msgPutOn = SCStageObjectInfoChange.newBuilder();
			msgPutOn.setObj(humanObj.createMsg());
			StageManager.inst().sendMsgToArea(msgPutOn, humanObj.stageObj, humanObj.posNow);
		}
		// 你脱下了装备{a}
		humanObj.sendSysMsg(45, "F", confItem.sn);

		// 重新计算属性
		calc_equipProp(humanObj);
		takeOffSkill(humanObj, confItem.sn, itemBody.getPosition());
		// 根据装备脱不属于的技能
		// ItemBodyManager.inst().takeOffSkill(unitObj, confItem.sn);
	}

	/**
	 * 装备强化--金币强化
	 */
	public void _msg_CSReinforceEquipMsg(HumanObject humanObj, long itemId) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeEquipIntensify, humanObj)) {
			// 等级未达到功能开放等级
			humanObj.sendSysMsg(66);
			return;
		}
		
		ItemBody itemBody = humanObj.itemBody.get(itemId);
		if (null == itemBody) {
			return;
		}
		
		// 判断是否是限时装备 是就不许强化
		boolean ret = ItemBodyManager.inst().isLimitEquip(humanObj, itemId);
		if (ret) {
			return;
		}
		
		_process_ReinforceOnce(humanObj, itemBody, itemBody.getReinforceLv()+1);
	}
	
	/**
	 * 一键强化--合部装备部位强化
	 * @param humanObj
	 */
	public void _msg_CSReinforceAllEquipMsg(HumanObject humanObj, CSReinforceAllEquip2Msg msg) {// 身体上的装备
		// 判断强化功能是否开启 true 未开启 false 开启
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeEquipIntensify, humanObj)) {
			// 等级未达到功能开放等级
			humanObj.sendSysMsg(66);
			return;
		}
		
		List<Long> strengthenedItems = new ArrayList<>();
		List<Integer> strengthenedTargetLevels = new ArrayList<>();
		List<Long> itemList = msg.getItemIDList();
		List<Integer> targetLevelList = msg.getTargetLevelList();
		for (int i = 0; i < itemList.size(); i++) {
			long itemId = itemList.get(i);
			// 判断是否是限时装备 是就不许强化
			boolean ret = ItemBodyManager.inst().isLimitEquip(humanObj, itemId);
			if (ret) {
				continue;
			}
			ItemBody itemBody = getPack(humanObj).get(itemId);
			int lv = _process_ReinforceOnce(humanObj, itemBody, targetLevelList.get(i));
			if(lv == targetLevelList.get(i)){
				strengthenedItems.add(itemId);
				strengthenedTargetLevels.add(targetLevelList.get(i));
			}
		}
		
		SCReinforceAllEquip2Msg.Builder sc_msg = SCReinforceAllEquip2Msg.newBuilder();
		sc_msg.addAllItemID(strengthenedItems);
		sc_msg.addAllTargetLevel(strengthenedTargetLevels);
		humanObj.sendMsg(sc_msg);
	}
	
	/**
	 * 装备进阶
	 * @param humanObj
	 * @param itemId
	 */
	public void _msg_CSAdvancedEquip(HumanObject humanObj, long itemId) {
		// 查找背包物品
		ItemPack itemBodyPack = getPack(humanObj);
		// 获取玩家身上的装备
		ItemBody equip = itemBodyPack.get(itemId);
		// 获取配置sn
		int equipSn = ConfigKeyFormula.getEquipAdvancedSn(equip.getType(), equip.getAdvancedLv());
		ConfEquipAdvanced conf = ConfEquipAdvanced.get(equipSn);
		// 获取空位的JSON {pos:sn}
		//Map<String, Object> posMap = Utils.jsonToMap(Utils.toJSONObject(temBody.getClkJson()));

		// 判断进阶满级不满级
		if (conf.qualityUpObj == 0) {
			humanObj.sendSysMsg(160808);//您的装备已经进阶到最高星级
			return;
		}

		// 判断配置在不在
		ConfEquipAdvanced targetConf = ConfEquipAdvanced.get(conf.qualityUpObj);
		if (targetConf == null) {
			throw new SysException("进阶配置不存在，原{}，目的{}", conf.sn,conf.qualityUpObj);
		}

		// 判断级别够不够开放
		/*ConfEquipInit initConf = ConfEquipInit.get(pos);
		if (initConf.unlockLv > humanObj.getHuman().getLevel()) {
			Inform.sendInform(humanObj.id, 3301);
			return;
		}*/
		
		// 判断消耗
		int[] itemSn = Utils.appendInt(conf.costItem, EMoneyType.coin_VALUE);
		int[] itemNum = Utils.appendInt(conf.costNum, conf.costGold);

		// 道具消耗判断
		if (!RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.EquipEvolution)) {
			return;
		}
		
		// 更新装备的进阶等级
		equip.setAdvancedLv(targetConf.level);

		// 重新计算玩家装备属性
		calc_equipProp(humanObj);
		
		SCUpEquipMsg.Builder msg = SCUpEquipMsg.newBuilder();
		msg.setResult(true);
		msg.setLevel(targetConf.level);
		humanObj.sendMsg(msg);		
		
		// 更新全局数据
		HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
		pxy.updateEquip(humanObj.getHumanId(), equip);

		// 发布进阶装备事件
		Event.fire(EventKey.EquipAdvanced, "humanObj", humanObj);
		
		// 铸造成功跑马灯
		// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
		String content = Utils.createStr("{}|{}|{}|{}|{}", ParamManager.sysMsgMark, 999004, 
				humanObj.getHuman().getName(), equipSn, targetConf.sn);
		InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
	}

	/**
	 * 装备精炼
	 * @param humanObj
	 * @param itemId
	 * @param isPerfect 是否完美精炼
	 */
	public void _msg_CSEquipRefineSlotUp(HumanObject humanObj, long itemId, boolean isPerfect) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeEquipRefine, humanObj)) {
			Log.item.error("=== 装备精炼未开放 ===");
			humanObj.sendSysMsg(162901);
			return;
		}
		
		// 查找背包物品
		ItemPack itemBodyPack = getPack(humanObj);
		// 获取玩家身上的装备
		ItemBody equip = itemBodyPack.get(itemId);
		if (equip == null) {
			Log.item.error("=== 操作异常，装备不存在 ===");
			humanObj.sendSysMsg(162902);
			return;
		}
		// 当前精炼品质的配置表
		ConfEquipRefine conf = ConfEquipRefine.get(ConfigKeyFormula.getEquipRefineSn(equip.getType(), equip.getRefineLv()));
		int[] curSlotLv = Utils.strToIntArray(equip.getRefineSlotLv());
		// 槽位满级的数量
		int maxLvCount = 0;
		// 槽位解锁的数量
		int unlockSlotNum = 0;
		for (int i = 0; i < conf.slotUnlock.length; i++) {
			if (conf.slotUnlock[i] == EEquipRefineSLot.RefineSlotUnlock_VALUE) {
				unlockSlotNum ++; 
				if (curSlotLv[i] == conf.maxSlotLv) {
					maxLvCount ++;
				}
			} 
		}
		if (maxLvCount == unlockSlotNum) {
			Log.item.error("=== 该品质所有槽位满级，无法精炼 ===");
			humanObj.sendSysMsg(162903);
			return;
		}
		
		// 修炼结果
		List<Integer> resultLvList = null;
		if (isPerfect) {
			// 完美修炼
			resultLvList = this._process_RefinePerfect(humanObj, equip, conf);
		} else {
			// 普通修炼
			resultLvList = this._process_RefineNormal(humanObj, equip, conf);
		}
		// 修炼失败，没有结果
		if (resultLvList == null || resultLvList.isEmpty()) {
			return;
		}
		// 保存修炼结果
		equip.setRefineRecordLv(Utils.ListIntegerToStr(resultLvList));
		
		SCEquipRefineSlotUp.Builder msg = SCEquipRefineSlotUp.newBuilder();
		msg.setItemID(itemId);
		msg.addAllResultList(resultLvList);
		humanObj.sendMsg(msg);
		
		humanObj.cultureTimes.setRefine(humanObj.cultureTimes.getRefine()+1);
		
		// 装备精炼事件
		Event.fire(EventKey.EquipRefine, "humanObj", humanObj);
	}
	
	/**
	 * 装备保存精炼结果
	 * @param humanObj
	 * @param itemId
	 */
	public void _msg_CSEquipRefineSaveSlotUp(HumanObject humanObj, long itemId) {
		// 查找背包物品
		ItemPack itemBodyPack = getPack(humanObj);
		// 获取玩家身上的装备
		ItemBody equip = itemBodyPack.get(itemId);
		if (equip == null) {
			Log.item.error("=== 操作异常，装备不存在 ===");
			humanObj.sendSysMsg(162902);
			return;
		}
		// 结果记录
		String refineRecordLv = equip.getRefineRecordLv();
		List<Integer> resultList = Utils.strToIntList(refineRecordLv);
		if (resultList.isEmpty()) {
			Log.item.error("=== 该装备无精炼结果，操作失败 ===");
			humanObj.sendSysMsg(163101);
			return;
		}
		int[] slotLvAry = Utils.strToIntArray(equip.getRefineSlotLv());
		for (int i = 0; i < resultList.size(); i++) {
			slotLvAry[i] = slotLvAry[i] + resultList.get(i);
		}
		
		// 保存结果记录为当前的数据
		equip.setRefineSlotLv(Utils.arrayIntToStr(slotLvAry));
		
		// 清空保存记录
		equip.setRefineRecordLv("");
		
		// 重新计算属性及战力
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.EquipRefine);
		
		SCEquipRefineSaveSlotUp.Builder msg = SCEquipRefineSaveSlotUp.newBuilder();
		msg.setItemID(itemId);
		msg.addAllSlotLvList(Utils.intToIntegerList(slotLvAry));
		humanObj.sendMsg(msg);
		
		// 更新全局数据
		HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
		pxy.updateEquip(humanObj.getHumanId(), equip);
	}
	
	/**
	 * 放弃精炼结果(该需求被废弃)
	 * @param humanObj
	 * @param itemId
	 */
	public void _msg_CSEquipRefineAbandonSlotUp(HumanObject humanObj, long itemId) {
		// 查找背包物品
		ItemPack itemBodyPack = getPack(humanObj);
		// 获取玩家身上的装备
		ItemBody equip = itemBodyPack.get(itemId);
		if (equip == null) {
			Log.item.error("=== 操作异常，装备不存在 ===");
			humanObj.sendSysMsg(162902);
			return;
		}
		// 清空保存记录
		equip.setRefineRecordLv("");
		
		SCEquipRefineAbandonSlotUp.Builder msg = SCEquipRefineAbandonSlotUp.newBuilder();
		msg.setItemID(itemId);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 装备精炼品质提升
	 * @param humanObj
	 * @param itemId
	 */
	public void _msg_CSEquipRefineUp(HumanObject humanObj, long itemId) {
		// 查找背包物品
		ItemPack itemBodyPack = getPack(humanObj);
		// 获取玩家身上的装备
		ItemBody equip = itemBodyPack.get(itemId);
		if (equip == null) {
			Log.item.error("=== 操作异常，装备不存在 ===");
			humanObj.sendSysMsg(162902);
			return;
		}
		
		// 提升前品质的配置表
		ConfEquipRefine oldConf = ConfEquipRefine.get(ConfigKeyFormula.getEquipRefineSn(equip.getType(), equip.getRefineLv()));
		if (oldConf.nextRefineLv == 0) {
			Log.item.error("=== 该装备精炼已达上限 ===");
			humanObj.sendSysMsg(163501);
			return;
		}
		
		// 该装备当前槽位等级
		int[] slotLv = Utils.strToIntArray(equip.getRefineSlotLv());
		// 当前品质槽位解锁数组
		int[] unlockSlot = oldConf.slotUnlock;
		int unlockCount = 0;
		int maxNum = 0;
		// 处理修炼结果
		for (int i = 0; i < unlockSlot.length; i++ ) {
			// 判断解锁状态
			if (unlockSlot[i] == EEquipRefineSLot.RefineSlotUnlock_VALUE) {
				unlockCount++;
			}
			// 满级
			if (slotLv[i] >= oldConf.maxSlotLv) {
				maxNum ++;
			}
		}
		if (maxNum < unlockCount) {
			Log.item.error("=== 提升失败，槽位等级不满足  ===");
			humanObj.sendSysMsg(163502);
			return;
		}
		
		
		int[] resetLvAry = new int[oldConf.slotUnlock.length];
		// 将当前槽位的等级重置为0
		equip.setRefineSlotLv(Utils.arrayIntToStr(resetLvAry));
		// 升级至下一品质
		equip.setRefineLv(oldConf.nextRefineLv);
		
		// 重新计算属性及战力
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.EquipRefine);
		
		SCEquipRefineUp.Builder msg = SCEquipRefineUp.newBuilder();
		msg.setItemID(itemId);
		msg.setRefineLv(equip.getRefineLv());
		msg.addAllSlotLvList(Utils.intToIntegerList(resetLvAry));
		humanObj.sendMsg(msg);
		
		ConfItem confItem = ConfItem.get(equip.getSn());
		if (confItem != null) {
			// 精炼成功跑马灯
			// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
			String content = Utils.createStr("{}|{}|{}|{}|{}", ParamManager.sysMsgMark, 999015, 
					humanObj.getHuman().getName(), confItem.sn, oldConf.nextRefineLv);
			InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
		}
	
		// 更新全局数据
		HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
		pxy.updateEquip(humanObj.getHumanId(), equip);
		
		// 装备精炼事件
		Event.fire(EventKey.EquipRefine, "humanObj", humanObj);
		// 装备精炼升级事件
		Event.fire(EventKey.EquipRefineUp, "humanObj", humanObj);
	}
	
	/////////////////////////////////////
	// 处理方法
	/////////////////////////////////////
	/**
	 * 处理强化一次
	 */
	private int _process_ReinforceOnce(HumanObject humanObj, ItemBody itemBody, int targetLevel) {
		int lvRein = itemBody.getReinforceLv();// 新的强化等级
		int snItem = itemBody.getSn();		
				
		// 获取装备位信息
		int snEquipStrengthen = itemBody.getType() * Utils.I1000 + targetLevel; // 升级表索引id：部位类型*1000+装备等级
		ConfEquipStrengthen confEquipStrengthen = ConfEquipStrengthen.get(snEquipStrengthen);
		if (confEquipStrengthen == null) {
			Log.table.error("===ConfEquipStrengthen no find sn={}", snEquipStrengthen);
			return -1;
		}

		// 验证玩家等级不能低于装备等级
		if (lvRein >= humanObj.getHuman().getLevel()) {
			humanObj.sendSysMsg(168107);//装备的等级，不能超过角色当前的等级
			return -1;
		}

		// 判断级别够不够开放
		/*ConfEquipInit initConf = ConfEquipInit.get(pos);
		if (initConf.unlockLv > humanObj.getHuman().getLevel()) {
			Inform.sendInform(humanObj.id, 3304);
			return;
		}*/

		//需要消耗当前强化等级的货币
		int currentEquipStrengthenSn; //= itemBody.getType() * Utils.I1000 + lvRein; // 升级表索引id：部位类型*1000+装备等级
		int realTargetLevel = targetLevel;
		for (int i = lvRein; i < targetLevel; i++) {
			currentEquipStrengthenSn = itemBody.getType() * Utils.I1000 + i; // 升级表索引id：部位类型*1000+装备等级
			ConfEquipStrengthen currentConfEquipStrengthen = ConfEquipStrengthen.get(currentEquipStrengthenSn);
			// 金币够不够
			boolean ret = RewardHelper.checkAndConsume(humanObj, EMoneyType.coin_VALUE, currentConfEquipStrengthen.costGold, LogSysModType.EquipIntensify);
			if (!ret) {
				return -1;
			}
			realTargetLevel = i+1;
		}

		// 升级
		itemBody.setReinforceLv(realTargetLevel);
		
		// 重新计算玩家装备属性
		this.calc_equipProp(humanObj);
		
		//如果是全部强化，就把强化成功的等级列表和id列表保存下来，以便发送给客户端
		SCReinforceEquipMsg.Builder msg = SCReinforceEquipMsg.newBuilder();
		msg.setReinforceLevel(realTargetLevel);
		msg.setSn(snItem);
		humanObj.sendMsg(msg);
		
		// 更新全局数据
		HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
		pxy.updateEquip(humanObj.getHumanId(), itemBody);
		
		// 发布强化装备事件
		Event.fire(EventKey.EquipIntensify, "humanObj", humanObj);
		return realTargetLevel;
	}
	
	/**
	 * 装备完美精炼
	 * @param humanObj
	 * @param equip 精炼的装备
	 * @param conf 该装备当前精炼品质的配置表
	 * @return
	 */
	private List<Integer> _process_RefinePerfect(HumanObject humanObj, Item equip, ConfEquipRefine conf) {
		// 判断消耗
		if (!RewardHelper.checkAndConsume(humanObj, conf.perfectCost, conf.perfectCostNum , LogSysModType.EquipRefine)) {
			Log.item.error("=== 完美修炼消耗不足 ===");
			return null;
		}
		
//		// 完美精炼的精炼值
//		List<Integer> slotRefineList = new ArrayList<>();
//		// 完美精炼的权重表
//		List<Integer> weights = new ArrayList<>(); 
//		// 遍历剔除洗练负面结果
//		for (int i = 0; i < conf.slotLv.length; i++) {
//			// 权重随机获得的影响等级
//			int resultLv = conf.slotLv[i];
//			// 影响等级如果为负，则不加入权重列表中
//			if (resultLv <= 0) {
//				continue;
//			}
//			slotRefineList.add(resultLv);
//			weights.add(conf.weight[i]);
//		}
		// 完美精炼值
		List<Integer> slotRefineList = Utils.intToIntegerList(conf.highSlotLv);
		// 完美精炼的权重表
		List<Integer> weights = Utils.intToIntegerList(conf.highWeight);
		
		// 修炼结果列表
		List<Integer> resultList = this._process_RefineResult(equip, conf, slotRefineList, weights);
		return resultList;
	}
	/**
	 * 装备普通精炼
	 * @param humanObj
	 * @param equip 精炼的装备
	 * @param conf 该装备当前精炼品质的配置表
	 * @return
	 */
	private List<Integer> _process_RefineNormal(HumanObject humanObj, Item equip, ConfEquipRefine conf) {
		// 判断消耗
		if (!RewardHelper.checkAndConsume(humanObj, conf.normalCost, conf.normalCostNum , LogSysModType.EquipRefine)) {
			Log.item.error("=== 普通修炼消耗不足 ===");
			return null;
		}
		// 普通精炼值
		List<Integer> slotRefineList = Utils.intToIntegerList(conf.slotLv);
		// 普通精炼的权重表
		List<Integer> weights = Utils.intToIntegerList(conf.weight); 
		
		// 修炼结果列表
		List<Integer> resultList = this._process_RefineResult(equip, conf, slotRefineList, weights);
		return resultList;
	}
	/**
	 * 获得精炼结果列表
	 * @param equip 精炼的装备
	 * @param conf 该装备当前精炼品质的配置表
	 * @param slotRefineList 精炼影响等级的权重结果列表
	 * @param weights 权重列表
	 * @return
	 */
	private List<Integer> _process_RefineResult(Item equip, ConfEquipRefine conf, List<Integer> slotRefineList, List<Integer> weights) {
		// 修炼结果列表
		List<Integer> resultList = new ArrayList<>();
		// 该装备当前槽位等级
		int[] slotLv = Utils.strToIntArray(equip.getRefineSlotLv());
		// 当前品质槽位解锁数组
		int[] unlockSlot = conf.slotUnlock;
		
		// 处理修炼结果
		for (int i = 0; i < unlockSlot.length; i++ ) {
			// 判断解锁状态
			if (unlockSlot[i] == EEquipRefineSLot.RefineSlotLock_VALUE) {
				// 变化值是0级
				resultList.add(0);
				continue;
			}
			// 满级
			if (slotLv[i] == conf.maxSlotLv) {
				// 变化值是0
				resultList.add(0);
				continue;
			}
			// 权重掉落的index为
			int dropIndex = Utils.getRandRange(weights);
			// 获得精炼影响等级
			int effectLv = slotRefineList.get(dropIndex);
			int resultLv = slotLv[i] + effectLv;
			// 保证结果不能超过当前段位上限
			if (resultLv > conf.maxSlotLv) {
				effectLv = conf.maxSlotLv - slotLv[i];
			} 
			// 当前段位不能为负值
			if (resultLv < 0) {
				effectLv = slotLv[i];
			}
			resultList.add(effectLv);
		}
		return resultList;
	}
	
	/**
	 * 删除装备
	 * @param oldItem
	 * @param humanObj
	 */
	public void deleteItem(Item oldItem, HumanObject humanObj) {
		if (oldItem.getContainer() == EContainerType.Body_VALUE) {
//			// 判断是否是武器 是就要脱技能
//			if (ItemTypeKey.isSameType(ItemTypeKey.equipWeapon, oldItem.getType())) {
//				ItemBodyManager.inst().takeOffSkill(humanObj, oldItem.getSn(), oldItem.getPosition());
//			}
			ItemBodyManager.inst().remove(humanObj, oldItem, 1);
//			clearHumanEquip(humanObj.getHuman(), oldItem);
//			calcEquipProp(humanObj);// 重新计算战力
		} else {
			ItemBagManager.inst().remove(humanObj, oldItem, 1);
		}
	}

	/**
	 * 脱技能
	 * @param unitObj
	 * @param sn
	 */
	public void takeOffSkill(CharacterObject unitObj, int sn, int position) {
//		if (!unitObj.isHumanObj()) {
//			return;
//		}
//		HumanObject humanObj = unitObj.getHumanObj();
//		Human human = humanObj.getHuman();
//		ConfItem confItem = ConfItem.get(sn);
//		if (confItem == null) {
//			return;
//		}
//
//		// 判断是否是武器
//		if (ItemTypeKey.isSameType(ItemTypeKey.equipWeapon, confItem.type)) {
//			if (position == EquipType.headrig_VALUE) {// 主武器
//				// 获取上阵技能
//				List<Integer> skillList = Utils.strToIntList(human.getSkillSet1());
//				String skillSet1 = "";
//				if (!skillList.isEmpty()) {
//					// 设置技能1为空
//					for (int i = 0; i < skillList.size(); i++) {
//						if (skillSet1.isEmpty()) {
//							skillSet1 = String.valueOf(0);
//							continue;
//						}
//						skillSet1 = skillSet1 + "," + 0;
//					}
//				}
//				// 保存上阵技能
//				human.setSkillSet1(skillSet1);
//			} else if (position == EquipType.deputyEquipment_VALUE) {// 副武器
//				// 获取上阵技能
//				List<Integer> skillList = Utils.strToIntList(human.getSkillSet2());
//				String skillSet2 = "";
//				if (!skillList.isEmpty()) {
//					// 设置技能1为空
//					for (int i = 0; i < skillList.size(); i++) {
//						if (skillSet2.isEmpty()) {
//							skillSet2 = String.valueOf(0);
//							continue;
//						}
//						skillSet2 = skillSet2 + "," + 0;
//					}
//				}
//				// 保存上阵技能
//				human.setSkillSet2(skillSet2);
//			}
//
//			// 下发消息：多武器技能设置
//			humanObj.sendMsg(HumanManager.inst().createMsgSCWeaponSkillSet(human.getSkillSet1(), human.getSkillSet2()));
//		}
	}

	/**
	 * 装备附魔
	 * @param humanObj
	 * @param id
	 */
	public void _msg_CSEnchantment(HumanObject humanObj, long id, int types, List<Integer> list) {
		
	}
	
	// 通过id获得装备item
	public Item getItemById(HumanObject humanObj, long itemId) {
		Item item = humanObj.itemBody.get(itemId);
		if (item == null) {
			item = humanObj.itemBag.get(itemId);
			if (item == null) {
				Log.item.error("error:ItemBodyManager.java,HumanId:{},ItemId:{},error:{}",
						humanObj.getHumanId(), itemId, "装备找不到");
				return null;
			}

		}
		return item;
	}

	// 同过item获得配表ConItem
	public ConfItem getConfItemByItem(Item item) {
		return ConfItem.get(item.getSn());
	}

	/**
	 * 判断身上是否有过时的装备 有则重新计算属性和战力 过时的装备不计算属性和战力
	 */
	public void takeOffLife(HumanObject humanObj) {
		long time = Port.getTime();// 当前时间
		ItemPack itemBody = getPack(humanObj);
		for (Item itb : itemBody.findAll()) {
			long life = itb.getLife();
			if (life > 0 && life < time) {
				calc_equipProp(humanObj);
			}
		}
	}

	/**
	 * 获得装备时 初始寿命 判断是否会过时 0代表不会过时
	 * @return
	 */
	public long getLife(int itemLife) {
		long life = 0;// <=0代表没有限时
		if (itemLife > 0) {
			long time = Port.getTime();
			life = time + itemLife * Time.SEC;
		}
		return life;
	}

	/**
	 * 判断是否过时了 true 装备过时了 false 还没过时
	 * @param stop
	 * @return
	 */
	private boolean isOutmoded(long stop) {
		long time = Port.getTime();
		if (stop > 0 && time > stop) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是限时装备
	 * @return true 是 false 否
	 */
	public boolean isLimitEquip(HumanObject humanObj, long id) {
		Item item = ItemBodyManager.inst().getItemById(humanObj, id);
		if (item != null && item.getLife() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 删除身上的装备 需要清除身上装备记录
	 * @param human
	 */
	public void clearHumanEquip(Human human, Item item) {
		/*
		if (ItemTypeKey.isSameType(ItemTypeKey.equipWeapon, item.getType())) {// 身上的武器
			human.setEquipWeaponSn(0);
			human.setEquipClothesStrthLv(0);
		} else if (ItemTypeKey.isSameType(ItemTypeKey.equipArmors, item.getType())) {// 身上的装备-护甲
			human.setEquipClothesSn(0);
			human.setEquipUnusedWeaponStrthLv(0);
		} else
		*/
		if (ItemTypeKey.isSameType(ItemTypeKey.fashionWeapon, item.getType())) {// 身上的时装-武器
			human.setFashionWeaponSn(0);
		}
	}
	
	

	


	/**
	 * 根据权重获取随机值或随机下标
	 * @param arry 数组
	 * @param baseRate 总权重
	 * @return isIndex 1 获取下标，0 获取值
	 */
	public int getRandArray(int[] arry, int baseRate, int isIndex) {
		int r = (int) (Math.random() * baseRate) + 1;
		int c = 0;
		if (arry == null) {
			return -1;
		}
		for (int i = 0; i < arry.length; i++) {
			int value = arry[i];
			c += value;
			if (r <= c) {
				if (isIndex == 1) {// 是否是下标
					return i;
				} else {// 否则是值
					return value;
				}
			}
		}
		return -1;
	}
	
	/**
	 * 获得相应部位的 星级 和 等级
	 * @param human
	 * @param position
	 * @return
	 */
	public int getEquipLevels(Human human,int position){
		JSONArray ja = Utils.toJSONArray(human.getEquipPosJSON());
		JSONObject jb = ja.getJSONObject(0);	
		return jb.getIntValue(String.valueOf(position));
	}
	
	public int getEquipStars(Human human,int position){
		JSONArray ja = Utils.toJSONArray(human.getEquipPosJSON());
		JSONObject jb = ja.getJSONObject(1);	
		return jb.getIntValue(String.valueOf(position));
	}
	/**
	 * 返回达到相应进阶等级的装备件数
	 * @return
	 */
	public int getEquipNumByAdvancedLv(HumanObject humanObj, int lv){
		int num = 0;
		ItemPack itemBodyPack = humanObj.itemBody;
		for (Item item : itemBodyPack.findAll()) {
			if(item.getAdvancedLv() >= lv){
				num++;
			}
		}
		return num;
	}
	/**
	 * 返回达到相应强化等级的装备件数
	 * @return
	 */
	public int getEquipNumByReinforceLv(HumanObject humanObj, int lv){
		int num = 0;
		ItemPack itemBodyPack = humanObj.itemBody;
		for (Item item : itemBodyPack.findAll()) {
			if(item.getReinforceLv() >= lv){
				num++;
			}
		}
		return num;
	}
	/**
	 * 返回达到相应精炼等级的装备件数
	 * @return
	 */
	public int getEquipNumByRefineLv(HumanObject humanObj, int lv){
		int num = 0;
		ItemPack itemBodyPack = humanObj.itemBody;
		for (Item item : itemBodyPack.findAll()) {
			if(item.getRefineLv() >= lv){
				num++;
			}
		}
		return num;
	}
	
	/**
	 * 升级后的等级保存数据库
	 * @param human
	 * @param position
	 * @param starlev
	 * @param lev
	 * @return
	 */
	public String setEquipState(Human human,int position,int starlev,int lev){
		JSONArray ja = Utils.toJSONArray(human.getEquipPosJSON());
		JSONObject jb = ja.getJSONObject(starlev);
		jb.replace(String.valueOf(position), lev);
		return ja.toJSONString();
	}
	
	/**
	 * 根据 成功率算出是否成功
	 * @param rate 成功率
	 * @return 是否成功
	 */
	/*public boolean getRateSucc(int rate){
		boolean through = false;
		int value = (int)(Math.random()*100);
//		System.out.println("成功概率:"+rate);
//		System.out.println("生成概率："+value);
		if(rate >= value){
			through = true;
		}
		return through;
	}*/
		
	/*public List<DGroove> getListDGroove(String json) {
		List<DGroove> listPetUse = new ArrayList<DGroove>();
		if (json == null || json.equals("")) {
			return listPetUse;
		}
		JSONArray ja = Utils.toJSONArray(json);
		if (ja.isEmpty()) {
			return listPetUse;
		}
		JSONObject jblevel = ja.getJSONObject(0);	
		JSONObject jbstar = ja.getJSONObject(1);
		
		for (int i = 1; i < 9; i++) {
			int level = jblevel.getIntValue(String.valueOf(i));
			int star = jbstar.getIntValue(String.valueOf(i));		
			DGroove.Builder msg = DGroove.newBuilder();
			msg.setGroove(i);
			msg.setLevels(level);
			msg.setStars(star);			
			listPetUse.add(msg.build());			
		}
		return listPetUse;
	}*/
	
	/**
	 * 获取身上道具数量
	 * @param humanObj
	 * @param itemSn 
	 * @return
	 */
	public int getNumBySn(HumanObject humanObj, int itemSn) {
		int num = 0;
		ItemPack itemBody = humanObj.itemBody;
		if (itemBody != null) {
			// 查找背包物品
			num = itemBody.getNumBySn(itemSn);
		}
		return num;
	}

	/**
	 * 获得身上的装备 通过装备类型
	 * @param type
	 * @return
	 */
	public Item getByType(HumanObject humanObj, int type) {
		ItemPack itemBody = getPack(humanObj);
		Item item = null;

		for (Item it : itemBody.findAll()) {
			ConfItem confItem = ConfItem.get(it.getSn());
			if (confItem == null) {
				Log.table.error("ConfItem配表错误，no find sn={}", it.getSn());
				continue;
			}
			//if (ItemTypeKey.getSubType3(type) == ItemTypeKey.getSubType3(confItem.itemType)) {// 是否同一类物品
			if (type == confItem.itemType) {// 是否同一类物品
				item = it;
				break;
			}
		}
		return item;
	}

	/**
	 * 获得身上的装备 通过装备类型
	 * @param type 物品类型
	 * @param position 物品所在位置
	 * @return
	 */
	public Item getByPos(HumanObject humanObj, int type, int position) {
		ItemPack itemBodyPack = getPack(humanObj);
		Item item = null;

		for (Item it : itemBodyPack.findAll()) {
			ConfItem confItem = ConfItem.get(it.getSn());
			if (confItem == null) {
				Log.table.error("ConfItem配表错误，no find sn={}", it.getSn());
				continue;
			}
			//if (ItemTypeKey.getSubType3(type) == ItemTypeKey.getSubType3(confItem.itemType) && i.getPosition() == position) {// 是否同一类物品// 平且在同一位置
			if (type == confItem.itemType && it.getPosition() == position) {// 是否同一类物品// 平且在同一位置
				item = it;
				break;
			}
		}
		return item;
	}

	/**
	 * 武将装备位置是否有装备
	 * @param position
	 * @return
	 */
	public Item getByGeneralPostion(HumanObject humanObj, int position) {
		ItemPack itemBody = getPack(humanObj);
		Item item = null;

		for (Item i : itemBody.findAll()) {
			ConfItem confItem = ConfItem.get(i.getSn());
			if (confItem == null) {
				Log.table.error("ConfItem配表错误，no find sn={}", i.getSn());
				continue;
			}
			if (i.getPosition() == position) {//在同一位置
				item = i;
				break;
			}
		}
		return item;
	}
	
	
	/**
	 * 计算装备属性
	 */
	public void calc_equipProp(HumanObject humanObj) {
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.ItemEquip);
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.EquipRefine);
	}
	
	public PropCalcCommon calc_equipPropWithoutRefine(HumanObject humanObj) {
		ItemPack itemBody = getPack(humanObj);
		List<Item> itemBodys = itemBody.findAll();
		
		PropCalcCommon propCalcCommon = new PropCalcCommon();
		// 各个装备基础养成加成的属性
		PropCalcCommon tmpEquipProp = null;
		for (Item equip : itemBodys) {
			long life = equip.getLife();
			if (life > 0 && life < Port.getTime()) {// 限时装备过期了 就不计算属性
				continue;
			}
			// 计算强化和进阶加成的属性
			tmpEquipProp = this.calc_strengthenAndAdvanced(equip);
			// 合并到精炼总加成prop
			propCalcCommon.merge(tmpEquipProp);
		}
		return propCalcCommon;
	}
	
	/**
	 * 计算装备精炼加成的总属性
	 * @param humanObj
	 * @return
	 */
	public PropCalcCommon calc_equipPropWithRefine(HumanObject humanObj) {
		ItemPack itemBody = getPack(humanObj);
		List<Item> itemBodys = itemBody.findAll();
		
		PropCalcCommon refineProp = new PropCalcCommon();
		
		// 各个装备基础养成加成的属性
		PropCalcCommon tmpEquipProp = null;
		// 各个装备精炼加成的属性
		PropCalcCommon tmpRefineProp = null;
		for (Item equip : itemBodys) {
			long life = equip.getLife();
			if (life > 0 && life < Port.getTime()) {// 限时装备过期了 就不计算属性
				continue;
			}
			// 计算强化和进阶加成的属性
			tmpEquipProp = this.calc_strengthenAndAdvanced(equip);
			
			// 计算精炼加成的属性
			tmpRefineProp = this.calc_equipRefineProp(tmpEquipProp, equip);
			// 合并到精炼总加成prop
			refineProp.merge(tmpRefineProp);
		}
		return refineProp;
	}
	
	/**
	 * 获得某个装备精炼加成的属性
	 * @param equipBaseProp 该装备非精炼加成的所有属性
	 * @param equip 装备
	 * @return
	 */
	private PropCalcCommon calc_equipRefineProp(PropCalcCommon equipBaseProp, Item equip) {
		PropCalcCommon equipRefineProp = new PropCalcCommon();
		
		// 当前精炼的配置表
		ConfEquipRefine confRefine = ConfEquipRefine.get(ConfigKeyFormula.getEquipRefineSn(equip.getType(), equip.getRefineLv()));
		if (confRefine == null) {
			Log.table.error("ConfEquipRefine配表错误，no find sn={}", equip.getSn());
			return equipRefineProp;
		}
		// 装备非精炼模块养成加成的属性map
		Map<String, Double> curPropMap = equipBaseProp.getDatas(); 
		// 根据当前curProp计算加成的百分比值
		for (Entry<String, Double> entry : curPropMap.entrySet()) {
			// 获取当前加成的值
			int addProp = (int) ((double)entry.getValue() * confRefine.refineRatio / Utils.D10000);
			// 添加当前养成按比例加成的属性值
			equipRefineProp.add(entry.getKey(), addProp);
		}
		
		// 添加计算四个槽位的加成属性值
		int[] slotLvAry = Utils.strToIntArray(equip.getRefineSlotLv());
		int addValue = 0;
		for (int i = 0; i < slotLvAry.length; i++) {
			addValue = slotLvAry[i] * confRefine.slotLvAttrValue[i];
			// 添加四个槽位加成的属性值
			equipRefineProp.add(confRefine.slotAttr[i], addValue);
		}
		
		// 添加该装备精炼品质加成属性(之前段位累积属性 + 当前段位额外加成属性)
		String[] refineAttr = confRefine.refineAttr;
		int[] refineAttrValue = confRefine.refineAttrValue;
		for (int i = 0; i < refineAttrValue.length; i++) {
			// 添加当前精炼品质加成的属性
			equipRefineProp.add(refineAttr[i], refineAttrValue[i]);
		}
		
		return equipRefineProp;
	}
	
	/**
	 * 计算获得装备强化和进阶加成的属性
	 * @param equip 计算的装备对象
	 */
	private PropCalcCommon calc_strengthenAndAdvanced(Item equip) {
		PropCalcCommon equipAddProp = new PropCalcCommon();
		long life = equip.getLife();
		if (life > 0 && life < Port.getTime()) {// 限时装备过期了 就不计算属性
			return equipAddProp;
		}
		// 装备强化sn
		int curStrengthSn = ConfigKeyFormula.getEquipReinforceSn(equip.getType(), equip.getReinforceLv()); 
		ConfEquipStrengthen confStrengthen = ConfEquipStrengthen.get(curStrengthSn);
		if (confStrengthen != null && confStrengthen.properties.length == confStrengthen.value.length) {
			// 强化加成属性
			equipAddProp.add(confStrengthen.properties, confStrengthen.value);
		} else {
			Log.table.error("ConfEquipStrengthen配表错误，no find sn={}", curStrengthSn);
		}
		
		// 进阶配置sn
		int equipSn = ConfigKeyFormula.getEquipAdvancedSn(equip.getType(), equip.getAdvancedLv());
		ConfEquipAdvanced confAdvanced = ConfEquipAdvanced.get(equipSn);
		if (confAdvanced != null && confAdvanced.properties.length == confAdvanced.value.length) {
			// 进阶加成属性
			equipAddProp.add(confAdvanced.properties, confAdvanced.value);
		} else {
			Log.table.error("ConfEquipStrengthen配表错误，no find sn={}", equipSn);
		}
		return equipAddProp;
	}
	
}
