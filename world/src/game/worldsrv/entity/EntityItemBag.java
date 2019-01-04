package game.worldsrv.entity;

import game.worldsrv.item.Item;

import core.gen.entity.Entity;
import game.worldsrv.entity.EntityItem;

@Entity(entityName = "ItemBag", tableName = "item_bag", superEntity = EntityItem.class, superClass = Item.class)
public enum EntityItemBag {

}