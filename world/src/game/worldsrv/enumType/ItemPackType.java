package game.worldsrv.enumType;

import game.worldsrv.item.Item;
import game.msg.Define.EContainerType;
import game.worldsrv.entity.ItemBag;
import game.worldsrv.entity.ItemBody;

/**
 * 道具背包类型枚举
 * @author shenjh
 */
public enum ItemPackType {
	Body(ItemBody.class, EContainerType.Body), // 身上装备
	Bag(ItemBag.class, EContainerType.Bag), // 背包物品
	;

	private Class<? extends Item> pack;// 所属容器背包
	private EContainerType container;// 所属容器类型
	
	private ItemPackType(Class<? extends Item> pack, EContainerType container) {
		this.pack = pack;
		this.container = container;
	}
	
	public Class<? extends Item> getPack() {
		return pack;
	}
	
	public EContainerType getContainer() {
		return container;
	}
}