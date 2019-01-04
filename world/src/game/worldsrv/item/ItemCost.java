package game.worldsrv.item;

import java.util.Map;

public class ItemCost {
	private int result = 0;
	private int[] material = null;
	private int [] nums = null;
	private int [] costPass = null;
	private int [] numPass = null;
	
	
	public int[] getCostPass() {
		return costPass;
	}
	public void setCostPass(int[] costPass) {
		this.costPass = costPass;
	}
	public int[] getNumPass() {
		return numPass;
	}
	public void setNumPass(int[] numPass) {
		this.numPass = numPass;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	public int[] getMaterial() {
		return material;
	}
	public void setMaterial(int[] material) {
		this.material = material;
	}
	public int[] getNums() {
		return nums;
	}
	public void setNums(int[] nums) {
		this.nums = nums;
	}
	
	/**
	 * 物品加成
	 * @return
	 */
	public static Map<Integer,Integer> plus(Map<Integer,Integer> map,Integer key ,Integer value) {
		Integer valueNew = getInteger(map,key) + value;
		map.put(key,valueNew);
		return map;
	}
	
	/**
	 * 获取map 中 对应KEY 的值
	 * @param key
	 * @return
	 */
	public static Integer getInteger(Map<Integer,Integer> map,Integer key) {
		Integer v = map.get(key);
		if (v == null)
			return 0;
		return v;
	}
	

}
