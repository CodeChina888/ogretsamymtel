package game.worldsrv.maincity;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
/**
 * 随机分配红包算法
 * 
 * @author sys
 *
 */
public class RedPacketObject {
	/**
	 * 1.总金额不能超过200*100 单位是分
	 * 2.每个红包都要有钱，最低不能低于1分，最大金额不能超过200*100
	 */
	
	private int itemSn;
	private int totlemoney;
	private int num;
	private int min;
	private int max;
	
	
	
	public int getItemSn() {
		return itemSn;
	}
	public void setItemSn(int itemSn) {
		this.itemSn = itemSn;
	}
	public int getTotlemoney() {
		return totlemoney;
	}
	public void setTotlemoney(int totlemoney) {
		this.totlemoney = totlemoney;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	
	
	public RedPacketObject(int itemSn, int totlemoney, int num, int min, int max) {
		super();
		this.itemSn = itemSn;
		this.totlemoney = totlemoney;
		this.num = num;
		this.min = min;
		this.max = max;
	}
	public RedPacketObject(int totlemoney, int num, int min, int max) {
		super();
		this.totlemoney = totlemoney;
		this.num = num;
		this.min = min;
		this.max = max;
	}

	//	private static final int MINMONEY =1;
//	private static final int MAXMONEY =200*100;
	/**
	 * 这里为了避免某一个红包占用大量资金，我们需要设定非最后一个红包的最大金额，我们把他设置为红包金额平均值的N倍；
	 */
	private static final double TIMES =2.1;
	
	
	/**
	 * 拆分红包
	 * @return
	 */
	public  List<Integer> splitRedPackets(){
		int money = this.totlemoney;
		int count = this.num;
		//红包 合法性校验
		if(!isRight(money,count)){
			return null;
		}
		//红包列表
		List<Integer> list =new ArrayList<Integer>();
		//每个红包最大的金额为平均金额的Times 倍
		int max =(int)(money*TIMES/count);
		
		max = max>this.max ? this.max : max;
		//分配红包
		for (int i = 0; i < count; i++) {
			int one = randomRedPacket(money,this.min,max,count-i);
			list.add(one);
			money -=one;
		}
		return list;
	}
	/**
	 * 随机分配一个红包
	 * @param money
	 * @param minS :最小金额
	 * @param maxS ：最大金额(每个红包的默认Times倍最大值)
	 * @param count
	 * @return
	 */
	private int randomRedPacket(int money, int minS, int maxS, int count) {
		//若是只有一个，直接返回红包
		if(count==1){
			return money;
		}
		//若是最小金额红包 == 最大金额红包， 直接返回最小金额红包
		if(minS ==maxS){
			return minS;
		}
		//校验 最大值 max 要是比money 金额高的话？ 去 money 金额
		int max = maxS>money ? money : maxS;
		//随机一个红包 = 随机一个数* (金额-最小)+最小
		int one =((int)Math.rint(Math.random()*(max-minS)+minS));
		//剩下的金额
		int moneyOther =money-one;
		//校验这种随机方案是否可行，不合法的话，就要重新分配方案
		if(isRight(moneyOther, count-1)){
			return one;
		}else{
			//重新分配
			double avg =moneyOther /(count-1);
			//本次红包过大，导致下次的红包过小；如果红包过大，下次就随机一个小值到本次红包金额的一个红包
			if(avg<this.min){
				 //递归调用，修改红包最大金额  
				return randomRedPacket(money, minS, one, count);
				
			}else if(avg>this.max){
				 //递归调用，修改红包最小金额  
				return randomRedPacket(money, one, maxS, count);
			}
		}
		return one;
	}
	/**
	 * 红包 合法性校验
	 * @param money
	 * @param count
	 * @return
	 */
	private boolean isRight(int money, int count) {
		double avg =money/count;
		//小于最小金额
		if(avg<this.min){
			return false;
		//大于最大金额	
		}else if(avg>this.max){
			return false;
		}
		return true;
	}
	
//	public JSONObject getJson(){
//		JSONObject json = new JSONObject();
//		List<Integer> list = splitRedPackets();
//		for (int i = 0; i < list.size(); i++) {
//			json.put(String.valueOf(i), list.get(i));
//		}
//		return json;
//	}
	
	public static void main(String[] args) {
		RedPacketObject dd = new RedPacketObject(100, 5,1,30);
		//单位是分
		System.out.println(dd.splitRedPackets());
	}
}