package game.worldsrv.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.item.Item;
import game.worldsrv.item.ItemTypeKey;

import game.msg.Define.EContainerType;
import game.worldsrv.enumType.ItemPackType;

/**
 * 背包的集合：身上的装备，背包，仓库
 */
public class ItemPack implements ISerilizable {
	private ItemPackType packType;
	private Map<Long, Item> datas;// <Item.id即物品ID, Item>

	public ItemPack() {
	}

	public ItemPack(ItemPackType packType, List<Item> items) {
		this.packType = packType;
		this.datas = new HashMap<>();
		for (Item i : items) {
			this.datas.put(i.getId(), i);
		}
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(packType);
		out.write(datas);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		packType = in.read();
		datas = in.read();
	}

	/**
	 * 添加物品
	 * @param item
	 */
	public void add(Item item) {
		// 内存添加
		datas.put(item.getId(), item);
	}

	/**
	 * 添加物品
	 * @param items
	 */
	public void addAll(List<Item> items) {
		for (Item i : items) {
			add(i);
		}
	}

	/**
	 * 删除物品
	 * @param itemId
	 */
	public void remove(long itemId) {
		// 内存移除
		datas.remove(itemId);
	}

	public EContainerType getContainer() {
		return packType.getContainer();
	}

	public ItemPackType getPackType() {
		return packType;
	}

	/**
	 * 通过ID获取物品
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Item> T get(long id) {
		return (T) datas.get(id);
	}

	/**
	 * 获取全部物品
	 * @return
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T extends Item> List<T> findAll() {
		return (List<T>)new ArrayList(datas.values());
	}
	
	/**
	 * 统计物品sn的所有数量（绑定和非绑定都算）
	 * @param sn
	 * @return
	 */
	public int countBySn(int sn) {
		int result = 0;
		for (Item i : datas.values()) {
			if (i.getSn() != sn)
				continue;

			result += i.getNum();
		}
		
		return result;
	}
	
	/**
	 * 通过SN获取物品
	 */
	public Item getBySn(int sn) {
		for (Item i : datas.values()) {
			if (i.getSn() != sn)
				continue;

			return i;
		}
		return null;
	}
	
	/**
	 * 通过SN获取物品集合
	 */
	@SuppressWarnings({"unchecked"})
	public <T extends Item> List<T> findBySn(int sn) {
		List<Item> result = new ArrayList<>();
		for (Item i : datas.values()) {
			if (i.getSn() != sn)
				continue;

			result.add(i);
		}

		return (List<T>) result;
	}

	/**
	 * 通过SN及绑定获取物品集合
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Item> List<T> findBySnBind(int sn, int bind) {
		List<Item> result = findBySn(sn);
		for (Iterator<Item> iter = result.iterator(); iter.hasNext();) {
			Item i = iter.next();
			if (i.getBind() == bind)
				continue;

			iter.remove();
		}

		return (List<T>) result;
	}

	/**
	 * 通过物品Type获取物品集合
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Item> List<T> findByType(int type) {
		List<Item> results = new ArrayList<>();

		for (Map.Entry<Long, Item> m : datas.entrySet()) {
			Item item = m.getValue();
			if (item.getType() == type)
				results.add(item);
		}

		return (List<T>) results;
	}

	@SuppressWarnings("unchecked")
	public <T extends Item> List<T> findByPos(int pos) {
		List<Item> results = new ArrayList<>();

		for (Map.Entry<Long, Item> m : datas.entrySet()) {
			Item item = m.getValue();
			if (item.getPosition() == pos)
				results.add(item);
		}

		return (List<T>) results;
	}

	/**
	 * 是否存在指定SN的物品
	 * @param sn
	 * @return
	 */
	public boolean isExistBySn(int sn) {
		boolean isExist = false;
		for (Item item : datas.values()) {
			if (item != null && item.getSn() == sn) {
				isExist = true;
				break;
			}
		}
		return isExist;
	}

	/**
	 * 获取指定SN的物品数量
	 * @param sn
	 * @return
	 */
	public int getNumBySn(int sn) {
		int num = 0;
		for (Item i : findBySn(sn)) {
			num += i.getNum();
		}
		return num;
	}

	/**
	 * 获取物品数量
	 * @param sn
	 * @return
	 */
	public int getNumBySnBind(int sn, int bind) {
		int num = 0;
		for (Item i : findBySnBind(sn, bind)) {
			num += i.getNum();
		}
		return num;
	}

	/**
	 * 获得物品数量
	 * @return
	 */
	public int getNum() {
		int num = 0;
		for (Item item : datas.values()) {
			if (!ItemTypeKey.isSameType(ItemTypeKey.fashion, item.getType())) {// 时装道具，不计算在内
				num++;
			}
		}
		return num;
	}

	/**
	 * 容器是否空的
	 * @return
	 */
	public boolean isEmpty() {
		return datas.size() > 0;
	}

	/**
	 * 获取空闲的背包最小位置
	 * @return
	 */
	public int getPositionFree() {
		// 已占用的位置
		List<Integer> hasPos = new ArrayList<>();
		for (Item i : datas.values()) {
			hasPos.add(i.getPosition());
		}
		Collections.sort(hasPos);

		// 背包大小
		int size = hasPos.size();

		// 空闲位置
		int posFree = size + 1;
		for (int i = 1; i <= size; i++) {
			if (hasPos.contains(i))
				continue;
			posFree = i;
			break;
		}

		return posFree;
	}
}
