package game.worldsrv.item;

import java.util.List;

import core.Record;
import core.support.Utils;
import game.msg.Define.DEquip;
import game.msg.Define.DItem;
import game.msg.Define.EContainerType;
import game.worldsrv.entity.ItemBase;

public abstract class Item extends ItemBase {

	boolean newly = false;// 是否是新获得的物品
	EContainerType from = null;// 记录来自哪个容器的改变（where 物品所属容器：1身上，2背包，3仓库）

	public Item() {
		super();
	}

	public Item(Record record) {
		super(record);
	}

	/**
	 * 记录来自哪个容器的改变
	 */
	public void setFrom(EContainerType from) {
		this.from = from;
	}

	public EContainerType getFrom() {
		return this.from;
	}

	/**
	 * 设置是否是新获得的物品
	 * @param newly
	 */
	public void setNewly(boolean newly) {
		this.newly = newly;
	}

	public boolean getNewly() {
		return this.newly;
	}

	/**
	 * 获取DItem
	 */
	public DItem getDItem() {
		DItem.Builder dItem = DItem.newBuilder();
		dItem.setItemId(getId());
		dItem.setItemSn(getSn());
		dItem.setNum(getNum());
		return dItem.build();
	}
	/**
	 * 获取DEquip
	 */
	public DEquip getDEquip() {
		DEquip.Builder dEquip = DEquip.newBuilder();
		dEquip.setItemId(getId());
		dEquip.setItemSn(getSn());
		dEquip.setReinforceLv(getReinforceLv());
		dEquip.setAdvancedLv(getAdvancedLv());
		dEquip.setRefineLv(getRefineLv());
		dEquip.addAllRefineSlotLvs(Utils.strToIntList(getRefineSlotLv()));
		// 精炼未操作的结果记录
		List<Integer> refineRecordList = Utils.strToIntList(getRefineRecordLv());
		if (!refineRecordList.isEmpty()) {
			dEquip.addAllRefineSlotRecord(refineRecordList);
		}
		return dEquip.build();
	}

}