package game.worldsrv.shop;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import game.msg.Define.DShopItem;
import game.worldsrv.support.Utils;

public class ShopItemJSON {
	private static final String shopSnKey = "sn";// 该key为商店格子sn
	private static final String itemSnKey = "in";// 该key为商店道具sn
	private static final String numKey = "nm";   // 该key为商店道具num

	public int shopSn; // 商店格子Sn
	public int itemSn; // 商店道具Sn
	public int num; // 商店道具购买次数

	public ShopItemJSON() {
	}

	public ShopItemJSON(int sn,int in, int nm) {
		this.shopSn = sn;
		this.itemSn = in;
		this.num = nm;
	}

	public ShopItemJSON(JSONObject jsonObject) {
		this.shopSn = jsonObject.getIntValue(shopSnKey);
		this.itemSn = jsonObject.getIntValue(itemSnKey);
		this.num = jsonObject.getIntValue(numKey);
	}

	/**
	 * 把Json转换为List
	 * 
	 * @param json
	 * @return
	 */
	public static List<ShopItemJSON> jsonToList(String json) {
		List<ShopItemJSON> result = new ArrayList<ShopItemJSON>();
		JSONArray ja = Utils.toJSONArray(json);
		if (ja.isEmpty()) {
			return result;
		}
		for (int i = 0; i < ja.size(); i++) {
			ShopItemJSON si = new ShopItemJSON(ja.getJSONObject(i));
			result.add(si);
		}
		return result;
	}

	/**
	 * 生成List<DShopItem>
	 * @param json
	 * @return
	 */
	public static List<DShopItem> jsonToDShopItems(String json) {
		List<ShopItemJSON> shopItemList = ShopItemJSON.jsonToList(json);
		List<DShopItem> items = new ArrayList<DShopItem>();
		if(shopItemList.isEmpty()){
			return items;
		}
		for (ShopItemJSON shopItemJSON : shopItemList) {
			items.add(shopItemJSON.createDShopItem());
		}
		return items;
	}

	/**
	 * 生成DShopItem
	 * @return
	 */
	public DShopItem createDShopItem() {
		DShopItem.Builder builder = DShopItem.newBuilder();
		builder.setSn(shopSn);
		builder.setItemSn(itemSn);
		builder.setNum(num);
		return builder.build();
	}

	/**
	 * 将List转换为Json
	 * 
	 * @return
	 */
	public static String listToJson(List<ShopItemJSON> shopItemJSONList) {
		JSONArray ja = new JSONArray();
		for (ShopItemJSON si : shopItemJSONList) {
			JSONObject jo = new JSONObject();
			jo.put(shopSnKey, si.shopSn);
			jo.put(itemSnKey, si.itemSn);
			jo.put(numKey, si.num);
			ja.add(jo);
		}
		return ja.toJSONString();
	}

}
