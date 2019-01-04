package game.worldsrv.rune;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import game.msg.Define.DRune;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfRune;
import game.worldsrv.entity.Rune;

/**
 * @author Neak
 * 玩家符文信息记录
 */
public class RuneRecord implements ISerilizable {
	// ----------------------
	// 符文唯一id = 数据库记录id
	// ----------------------
	// 已经穿戴的符文map <符文唯一id，符文信息>
	private Map<Long, Rune> wearMap = new HashMap<>();
	// 未穿戴的符文map <符文唯一id，符文信息>
	private Map<Long, Rune> noWearMap = new HashMap<>();
	
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(wearMap);
		out.write(noWearMap);
	}
	@Override
	public void readFrom(InputStream in) throws IOException {
		wearMap.clear();
		noWearMap.clear();
		
		wearMap.putAll(in.<Map<Long, Rune>> read());
		noWearMap.putAll(in.<Map<Long, Rune>> read());
	}
	
	/**
	 * 新建符文
	 */
	public Rune createNewRune(int runeSn, long humanId) {
		Rune rune = new Rune();
		rune.setId(Port.applyId());
		rune.setHumanId(humanId);
		rune.setSn(runeSn);
		rune.setLevel(1);
		rune.setExp(0);
		rune.setBelongUnitId(0);
		rune.persist();
		// 添加进管理池中
		this.addRune(rune);
		
		return rune;
	}
	
	/**
	 * 添加符文
	 */
	public void addRune(Rune rune) {
		// 穿戴的加入穿戴中
		if (rune.getBelongUnitId() != 0) {
			wearMap.put(rune.getId(), rune);
		} else {
			noWearMap.put(rune.getId(), rune);
		}
	}
	
	/**
	 * 根据符文id获得符文
	 * @param runeId 符文id
	 * @return 
	 */
	public Rune getRune(long runeId) {
		Rune rune = null;
		rune = wearMap.get(runeId);
		if (rune == null) {
			rune = noWearMap.get(runeId);
		}
		return rune;
	}
	
	/**
	 * 未穿戴符文的数量
	 */
	public int getRuneBagNum() {
		return noWearMap.size();
	}
	
	/**
	 * 脱下符文
	 */
	public void takeOffRune(long runeId) {
		Rune rune = wearMap.get(runeId);
		if (rune != null) {
			// 没有携带者
			rune.setBelongUnitId(0l);
			// 加入未穿戴的背包
			noWearMap.put(runeId, rune);
			// 从穿戴背包中移除
			wearMap.remove(runeId);
		}
	}
	
	/**
	 * 穿上符文
	 * @param runeId 穿戴的符文id
	 * @param unitId 穿戴符文的对象id
	 */
	public void wearRune(long runeId, long unitId) {
		Rune rune = noWearMap.get(runeId);
		if (rune != null) {
			// 设置携带人
			rune.setBelongUnitId(unitId);
			// 加入穿戴的背包
			wearMap.put(runeId, rune);
			// 从未穿戴背包中移除
			noWearMap.remove(runeId);
		}
	}
	
	/**
	 * 批量移除符文
	 */
	public void removeRune(List<Long> runeIds) {
		for (Long runeId : runeIds) {
			this.removeRune(runeId);
		}
	}
	/**
	 * 移除符文
	 * @param runeId
	 */
	public void removeRune(long runeId) {
		Rune rune = null;
		rune = wearMap.get(runeId);
		if (rune != null) {
			wearMap.remove(runeId);
			rune.remove();
			return;
		} 
		
		rune = noWearMap.get(runeId);
		if (rune != null) {
			noWearMap.remove(runeId);
			rune.remove();
			return;
		} 
	}
	
	/**
	 * 获取纹石List被养成时总经验
	 * @param 
	 */
	public int getExpByRuneIdList(List<Long> runeIds) {
		Rune rune = null;
		ConfRune conf = null;
		int exp = 0;
		for (Long runeId : runeIds) {
			rune = this.getRune(runeId);
			if (rune == null) {
				continue;
			}
			conf = ConfRune.get(rune.getSn());
			exp += rune.getExp() + conf.exp;
		}
		return exp;
	}

	/**
	 * 穿戴了的符文列表结构
	 */
	public List<DRune> getWearDRuneList() {
		List<DRune> msgs = new ArrayList<>();
		for (Rune rune : wearMap.values()) {
			msgs.add(createDRune(rune));
		}
		return msgs;
	}
	/**
	 * 没穿带的符文结构列表
	 */
	public List<DRune> getNoWearDRuneList() {
		List<DRune> msgs = new ArrayList<>();
		for (Rune rune : noWearMap.values()) {
			msgs.add(createDRune(rune));
		}
		return msgs;
	}
		
	/**
	 * 符文协议结构
	 * @param rune 符文
	 */
	public static DRune createDRune(Rune rune) {
		DRune.Builder msg = DRune.newBuilder();
		msg.setRuneId(rune.getId());
		msg.setRuneSn(rune.getSn());
		msg.setRuneLv(rune.getLevel());
		msg.setRuneExp(rune.getExp());
		msg.setState(rune.getBelongUnitId() != 0);
		return msg.build();
	}
	
	/**
	 * 符文结构列表
	 */
	public static List<DRune> createDRuneList(List<Rune> runeList) {
		List<DRune> msgs = new ArrayList<>();
		for (Rune rune : runeList) {
			msgs.add(createDRune(rune));
		}
		return msgs;
	}
	
	
	
	
	/////////////////////////////////////
	// 任务相关
	////////////////////////////////////
	/**
	 * 满足品质的符文数量
	 * @param humanObj
	 * @param quality
	 * @return
	 */
	public int getAmountByQuality(HumanObject humanObj, int quality) {
		int amount = 0;
		HashSet<Rune> runeSet = new HashSet<>();
		runeSet.addAll(wearMap.values());
		runeSet.addAll(noWearMap.values());
		ConfRune conf = null;
		for (Rune rune : runeSet) {
			conf = ConfRune.get(rune.getSn());
			if (conf != null && conf.qualityId >= quality) {
				amount++;
			}
		}
		return amount;
	}
	
	/**
	 * 满足等级的符文数量 
	 * @param lv
	 * @return
	 */
	public int getAmountByLv(HumanObject humanObj, int lv) {
		int amount = 0;
		HashSet<Rune> runeSet = new HashSet<>();
		runeSet.addAll(wearMap.values());
		runeSet.addAll(noWearMap.values());
		for (Rune rune : runeSet) {
			if (rune != null && rune.getLevel() >= lv) {
				amount++;
			}
		}
		return amount;
	}

	
}
