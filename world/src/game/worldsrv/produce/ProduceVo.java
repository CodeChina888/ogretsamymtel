package game.worldsrv.produce;

import java.util.List;

import game.worldsrv.produce.ProduceVo;
import game.msg.Define.DProduce;
import game.msg.Define.EMoneyType;
import game.worldsrv.item.ItemVO;

public class ProduceVo {
	public int sn; // 物品SN或货币类型MoneyType
	public int num; // 数量
	public boolean isItem = true; // 是否物品
	public boolean isMoney = false; // 是否货币

	public ProduceVo(int sn, int num) {
		this.sn = sn;
		this.num = num;

		if (isMoney(sn)) {
			isItem = false;
			isMoney = true;
		} else {
			isItem = true;
			isMoney = false;
		}
	}
	
	public ProduceVo(DProduce dProduce) {
		this.sn = dProduce.getSn();
		this.num = dProduce.getNum();

		if (isMoney(sn)) {
			isItem = false;
			isMoney = true;
		} else {
			isItem = true;
			isMoney = false;
		}
	}

	private boolean isMoney(int itemId){
		return itemId > EMoneyType.minMoney_VALUE && itemId < EMoneyType.maxMoney_VALUE;
	}
	
	public boolean isCoin() {
		return EMoneyType.coin_VALUE == this.sn;
	}

	public boolean isGold() {
		return EMoneyType.gold_VALUE == this.sn;
	}

	public boolean isExp() {
		return EMoneyType.exp_VALUE == this.sn;
	}

	public ItemVO toItemVo() {
		if (isItem) {
			return new ItemVO(sn, num);
		} else {
			return null;
		}
	}

	public DProduce.Builder toDProduce() {
		DProduce.Builder dPro = DProduce.newBuilder();
		dPro.setSn(sn);
		dPro.setNum(num);
		dPro.setIsItem(isItem);

		return dPro;
	}

	public static List<ItemVO> addItemVoList(List<ItemVO> voList, ProduceVo item) {
		if (item.isItem) {
			voList.add(item.toItemVo());
		}
		return voList;
	}
	
}
