package game.worldsrv.drop;

import java.util.Random;

import com.mysql.jdbc.Util;

import core.support.Utils;
import game.worldsrv.config.ConfDropInfos;

public class DropItemVO {
	/**
	 * 万分比
	 */
	public static final  int Random_Count = 10000;
	
	private int sn; // 物品SN或货币类型MoneyType
	private int num; // 数量
	private int groupId ;//掉落分组
	String itemId;//掉落道具
	int chance;//掉落概率，万分比
	int weigh;//权重
	int minNum;//数量下限
	int maxNum;//数量上线
	int presionCountType;//计数类型
	int resetCount;//重置次数
	
	Random r = new Random();
	public DropItemVO() {

	}
	public DropItemVO(ConfDropInfos confs) {
		this.sn = confs.itemId;
		this.groupId = confs.groupId;
		this.chance = confs.chance;
		this.weigh = confs.weight;
		this.minNum = confs.minNum;
		this.maxNum = confs.maxNum;
		this.presionCountType = confs.presonCountType;
	}
	public int getSn() {
		return sn;
	}
	public void setSn(int sn) {
		this.sn = sn;
	}
	/**
	 * 获取得到数量
	 * @return
	 */
	public int getNum() {
		return Utils.randomBetween(minNum, maxNum);
	}
	/**
	 * 是否得到
	 * @return
	 */
	public boolean isGet(){
		Random r1 = new Random();
	    return chance > r1.nextInt(Random_Count);
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public int getChance() {
		return chance;
	}
	public void setChance(int chance) {
		this.chance = chance;
	}
	public int getWeigh() {
		return weigh;
	}
	public void setWeigh(int weigh) {
		this.weigh = weigh;
	}
	public int getMinNum() {
		return minNum;
	}
	public void setMinNum(int minNum) {
		this.minNum = minNum;
	}
	public int getMaxNum() {
		return maxNum;
	}
	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}
	public int getPresionCountType() {
		return presionCountType;
	}
	public void setPresionCountType(int presionCountType) {
		this.presionCountType = presionCountType;
	}
	public int getResetCount() {
		return resetCount;
	}
	public void setResetCount(int resetCount) {
		this.resetCount = resetCount;
	}

	
}
