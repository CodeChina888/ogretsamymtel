package game.worldsrv.stage;

import game.worldsrv.character.WorldObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.MonsterObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Chunk;
import game.worldsrv.stage.StageCell;

public class StageCell {
	public int i; // 行
	public int j; // 列
	private long stageId; // 所属地图
	private Map<Long, WorldObject> worldObjects = new HashMap<Long, WorldObject>(); // 该cell内所有地图单位
	private Map<Long, HumanObject> humans = new HashMap<>(); // 该cell内所有玩家
	private Map<Long, MonsterObject> monsters = new HashMap<>();
	private Map<Long, PartnerObject> generals = new HashMap<>();

	// 消息发送机制
	// public List<Builder> msgList = new ArrayList<Builder>();

	List<Integer> idList = new ArrayList<Integer>();
	List<Chunk> chunkList = new ArrayList<Chunk>();

	public StageCell(long stageId, int i, int j) {
		this.stageId = stageId;
		this.i = i;
		this.j = j;
	}

	public void cleanup() {

	}

	/**
	 * 心跳stageCell中的消息，把场景九宫格中需要广播的消息列表发送给玩家，例如：同步移动，出生，死亡等消息
	 */
	public void sendMsg() {
		if (idList.isEmpty()) {
			return;
		}

		for (HumanObject humanObj : getHumans().values()) {
			if (humanObj.isInCloseDelay)
				continue;// 处于断线延迟状态的玩家
			humanObj.sendMsg(idList, chunkList);
		}
		idList.clear();
		chunkList.clear();
	}

	/**
	 * 是否属于同一张地图的Cell
	 * @return
	 */
	public boolean isInSameStage(StageCell cell) {
		return this.stageId == cell.stageId;
	}

	/**
	 * 添加地图单元
	 * @param obj
	 */
	public void addWorldObject(WorldObject obj) {
		worldObjects.put(obj.id, obj);

		// 记录玩家
		if (obj instanceof HumanObject) {
			humans.put(obj.id, (HumanObject) obj);
		}

		// 记录怪物
		if (obj instanceof MonsterObject) {
			monsters.put(obj.id, (MonsterObject) obj);
		}

		// 武将
		if (obj instanceof PartnerObject) {
			generals.put(obj.id, (PartnerObject) obj);
		}
	}

	/**
	 * 删除地图单元
	 * @param obj
	 */
	public void delWorldObject(WorldObject obj) {
		worldObjects.remove(obj.id);

		// 删除玩家
		if (obj instanceof HumanObject) {
			this.humans.remove(obj.id);
		}

		// 删除玩家
		if (obj instanceof MonsterObject) {
			this.monsters.remove(obj.id);
		}

		// 删除武将
		if (obj instanceof PartnerObject) {
			this.generals.remove(obj.id);
		}
	}

	/**
	 * 判断两Cell是否为同一个
	 * @param cell
	 * @return
	 */
	public boolean isEquals(StageCell cell) {
		boolean ret = false;
		if (cell != null) {
			if (this.i == cell.i && this.j == cell.j)
				ret = true;
		}
		return ret;
	}

	// 根据id获取对象
	public WorldObject getWorldObject(long id) {
		return worldObjects.get(id);
	}

	public HumanObject getHuman(long id) {
		return humans.get(id);
	}

	public MonsterObject getMonster(long id) {
		return monsters.get(id);
	}

	public Map<Long, WorldObject> getWorldObjects() {
		return worldObjects;
	}

	public Map<Long, HumanObject> getHumans() {
		return humans;
	}

	public Map<Long, HumanObject> getHumanObjects() {
		return humans;
	}
}
