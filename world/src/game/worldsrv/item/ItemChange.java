package game.worldsrv.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import game.worldsrv.character.UnitObject;
import game.worldsrv.item.Item;
import game.msg.Define.DBagUpdate;
import game.msg.Define.DProduce;
import game.msg.MsgItem.SCBagUpdate;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

public class ItemChange {

	/** 物品数量发生变化 */
	public static final byte CHANGE = 1;
	/** 增加物品 */
	public static final byte ADD = 2;
	/** 删除物品 */
	public static final byte DELETE = 3;
	
	// 添加列表
	private Set<Item> adds = new HashSet<>();
	// 修改列表
	private Set<Item> mods = new HashSet<>();
	// 删除列表
	private Set<Item> dels = new HashSet<>();
	// 是否是新获得的物品
	public boolean newly = false;

	//整个ItemChange生命周期产生的物品（来源于不同渠道的增加）
	private List<DProduce> produces = new ArrayList<>();
	// 未推给客户端的变动操作
	private Queue<DBagUpdate> modifyQueue = new LinkedList<>();

	/**
	 * 合并修改
	 * 
	 * @param change
	 */
	public void merge(ItemChange change) {
		
		//设置更新标识
		newly = change.newly;
		
		// 合并添加列表
		adds.addAll(change.adds);
		// 合并修改列表
		mods.addAll(change.mods);
		// 合并删除列表
		dels.addAll(change.dels);
		// 合并背包队列
		modifyQueue.addAll(change.modifyQueue);
		// 合并物品包队列
		produces.addAll(change.getProduce());
	}

	/**
	 * 发送事件
	 * 
	 * @param humanObj
	 */
	public void fireChangeEvent(UnitObject humanObj) {
		// 发送 物品增加 事件
		for (Item i : adds) {
			fireAddEvent(humanObj, i);
		}

		// 发送 物品修改 事件
		for (Item i : mods) {
			// 可堆叠的物品新增物品时发送增加事件
			if (newly) {
				fireAddEvent(humanObj, i);
				continue;
			}

			// 修改事件
			fireModEvent(humanObj, i);
		}

		// 发送 物品删除 事件
		for (Item i : dels) {
			fireDelEvent(humanObj, i);
		}
	}

	/**
	 * 增加事件
	 * 
	 * @param humanObj
	 * @param item
	 */
	private void fireAddEvent(UnitObject humanObj, Item item) {
		fireChangeEvent(humanObj, item);
		Event.fire(EventKey.ItemChangeAdd, "humanObj", humanObj, "item", item);
	}

	/**
	 * 删除事件
	 * 
	 * @param humanObj
	 * @param item
	 */
	private void fireDelEvent(UnitObject humanObj, Item item) {
		fireChangeEvent(humanObj, item);
		Event.fire(EventKey.ItemChangeDel, "humanObj", humanObj, "item", item);
	}

	/**
	 * 修改事件
	 * 
	 * @param humanObj
	 * @param item
	 */
	private void fireModEvent(UnitObject humanObj, Item item) {
		fireChangeEvent(humanObj, item);
		Event.fire(EventKey.ItemChangeMod, "humanObj", humanObj, "item", item);
	}

	/**
	 * 物品变动事件
	 * 
	 * @param humanObj
	 * @param item
	 */
	private void fireChangeEvent(UnitObject humanObj, Item item) {
		Event.fire(EventKey.ItemChange, "humanObj", humanObj, "item", item);
	}
	
	/**
	 * 加入物品
	 * @param item
	 */
	public void addItem(Item item) {
		this.adds.add(item);
		// 更新背包变化队列
		DBagUpdate.Builder b = DBagUpdate.newBuilder();
		b.setId(item.getId());
		b.setType(ADD);
		b.setBagType(item.getContainer());
		b.setItem(item.getDItem());
		// 记录背包变化，掉落包
		DProduce.Builder p = DProduce.newBuilder();
		p.setSn(item.getSn());
		p.setNum(item.getNum());
		this.produces.add(p.build());
		// 记录变更
		this.modifyQueue.add(b.build());
	}
	/**
	 * 标记一个物品将要被删除
	 */
	public void delItem(Item item) {
		this.dels.add(item);
		DBagUpdate.Builder b = DBagUpdate.newBuilder();
		b.setId(item.getId());
		b.setType(DELETE);
		b.setBagType(item.getContainer());
		b.setItem(item.getDItem());
		// 记录变更
		this.modifyQueue.add(b.build());
	}
	/**
	 * 修改物品数量
	 * @param item
	 */
	public void modItem(Item item, int changeNum) {
		this.mods.add(item);
		// 更新背包变化队列
		DBagUpdate.Builder b = DBagUpdate.newBuilder();
		b.setId(item.getId());
		b.setType(CHANGE);
		b.setBagType(item.getContainer());
		b.setItem(item.getDItem());
		// 记录背包变化，掉落包
		if(changeNum > 0){
			DProduce.Builder p = DProduce.newBuilder();
			p.setSn(item.getSn());
			p.setNum(changeNum);
			this.produces.add(p.build());
		}
		// 记录变更
		this.modifyQueue.add(b.build());
	}
	
	/**
	 * 添加一条奖励变化数据，用于记录非物品，比如各种货币
	 * 不触发变化数据
	 */
	public void addProduce(int sn, int num){
		DProduce.Builder p = DProduce.newBuilder();
		p.setSn(sn);
		p.setNum(num);
		this.produces.add(p.build());
	}

	/**
	 * 得到更新数据。
	 * 
	 * @return 更新数据。
	 */
	public SCBagUpdate.Builder getUpdateMsg() {
		//队列为空就返回
		if (modifyQueue.isEmpty())
			return null;
		
		//构建消息
		SCBagUpdate.Builder builder = SCBagUpdate.newBuilder();
		DBagUpdate update = modifyQueue.poll();
		
		//挨个出队，放入到背包消息里
		while (update != null) {
			builder.addUpdates(update);
			update = modifyQueue.poll();
		}
		
		return builder;
	}
	
	/**
	 * 获取整个ItemChange周期内的Dproduce列表
	 * @return
	 */
	public List<DProduce> getProduce() {
		
		return this.produces;
	}
	
	/**
	 * 获取添加的物品
	 * @return
	 */
	public List<Item> getAddList() {
		List<Item> result = new ArrayList<>();
		result.addAll(adds);
		result.addAll(mods);
		return result;
	}
	
}
