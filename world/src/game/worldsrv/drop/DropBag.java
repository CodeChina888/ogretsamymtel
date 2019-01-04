package game.worldsrv.drop;

import java.util.Arrays;
import java.util.List;

import game.msg.Define.DDropItem;
import game.msg.Define.EMoneyType;
import game.msg.MsgItem.SCDropItem;
import game.worldsrv.support.Utils;

public class DropBag {

	private boolean isEmpty = true;// 是否空的
	private int [] itemSn = {};
	private int [] itemNum = {};
	
	public DropBag() {
		
	}

	public DropBag(int[] itemSn, int[] itemNum) {
		super();
		this.itemSn = itemSn;
		this.itemNum = itemNum;
	}

	public int[] getItemSn() {
		return itemSn;
	}
	public void setItemSn(int[] itemSn) {
		this.itemSn = itemSn;
	}

	public int[] getItemNum() {
		return itemNum;
	}
	public void setItemNum(int[] itemNum) {
		this.itemNum = itemNum;
	}
	
	/**
	 * 是否空的
	 * @return
	 */
	public boolean isEmpty() {
		return isEmpty;
	}
	
	/**
	 * 添加物品进背包
	 * @param div
	 */
	public void add(DropItemVO div) {
		itemSn = Utils.appendInt(itemSn, div.getSn());
		itemNum = Utils.appendInt(itemNum, div.getNum());
		if (isEmpty) {
			isEmpty = false;
		}
	}
	/**
	 * 获取掉落物品列表
	 * @return
	 */
	public SCDropItem.Builder getDropItem(){
		SCDropItem.Builder msg = SCDropItem.newBuilder();
		for (int i = 0; i < itemSn.length; i++) {
			DDropItem.Builder dDropItem = DDropItem.newBuilder();
			dDropItem.setItemSn(itemSn[i]);
			dDropItem.setItemNum(itemNum[i]);
			msg.setItem(i, dDropItem);
		}
		return msg;
	}
	
	/**
	 * 将List<DropBag>组成DropBag
	 */
	public static  DropBag getDropBagFromList(List<DropBag> dlist){
		DropBag dropBag = new DropBag();
		for (DropBag dp : dlist) {
			int [] ditemSn = dp.getItemSn();
			int [] dropBagItemSn = dropBag.getItemSn();
			int []  _itemSn = Utils.concatAll_Int(ditemSn, dropBagItemSn);
			dropBag.setItemSn(_itemSn); 
			
			int [] ditemNum = dp.getItemNum();
			int [] dropBagItemNum = dp.getItemNum();
			int [] _itemNum = Utils.concatAll_Int(ditemNum, dropBagItemNum);
			dropBag.setItemNum(_itemNum);
		}
		return dropBag;
	}

	@Override
	public String toString() {
		return "DropBag [itemSn=" + Arrays.toString(itemSn) + ", itemNum=" + Arrays.toString(itemNum) + "]";
	}
	
	public int getPartnerExpInBag(){
		int hasexp = 0;
		if(itemSn.length>=itemNum.length){
			for (int i = 0; i < itemNum.length; i++) {
				if(itemSn[i] == EMoneyType.parnterExp_VALUE){
					hasexp += itemNum[i];
				}
			}
		}else{
			for (int i = 0; i < itemSn.length; i++) {
				if(itemSn[i] == EMoneyType.parnterExp_VALUE){
					hasexp += itemNum[i];
				}
			}
			
		}
		
		return hasexp;
	}
}
