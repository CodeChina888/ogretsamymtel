package game.worldsrv.instLootMap.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DItem;
import game.msg.Define.DLootMapHuman;
import game.msg.Define.DMemberInfo;
import game.msg.Define.DVector2;
import game.msg.Define.ELootMapType;
import game.msg.Define.EMoneyType;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.enumType.FightPropName;
import game.worldsrv.instLootMap.Stage.StageObjectLootMap;
import game.worldsrv.instLootMap.Stage.StageObjectLootMapMultiple;
import game.worldsrv.param.ParamManager;
import game.worldsrv.pk.PKHumanInfo;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.rank.RankData;
import game.worldsrv.support.Log;
import game.worldsrv.support.Vector2D;

public class InstLootMapHumanData implements ISerilizable {

	private ELootMapType lootMapType;

	public long humanId = -1; // 角色Id
	public String name = ""; // 角色名字
	public int modelSn = -1; // 角色模型sn
	public int level = -1;

	public boolean isPking = false; // 是否pk
	public boolean isProtecting = false; // 是否保护
	public boolean isRedName = false; // 是否红名

	public int activeRobNum = -1; // 主动抢夺次数剩余
	public int passiveRobNum = -1;// 被抢夺次数剩余

	private InstLootMapSkill skill = null; // 彩蛋技能
	public int killNumber = 0; // 杀人数

	public int oneRank = 0; // 一场内排名
	private int oneScore = 0; // 一场内获得分数

	private int todayScore = 0; // 今日最高
	private long score = 0;// 所有场次累计总共分数
	// public int addScore = 0; // 本场增加的积分 针对于上场来说

	public int rank = -1;// 现在排名

	private int attack = -1;
	private int hp = -1;
	private int maxHp = -1; // 初始血量

	public Vector2D pos = null;// 坐标
	private Set<InstLootMapBuff> buffs = null;
	public List<InstLootMapBagItem> itemBag = null; // 道具

	InstLootMapScoreReward scoreReward = new InstLootMapScoreReward(); // 积分奖励
	public StageObjectLootMap stage;

	private int buffAttack = -1;
	public int deadTime = 0; // 死亡时间
	public int protectedTime = 0; // 保护时间

	public boolean isPKTrigger = false; // 是否是PK触发者
	public DVector2 lastPos;

	/**
	 * 判断操作点与现在点是否合适 -> 只会差1
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isCorrectRangePoint(float x, float y) {
		return isCorrectRangePoint((int) x, (int) y);
	}


	/**
	 * 判断操作点与现在点是否合适 -> 只会差1
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isCorrectRangePoint(int x, int y) {
		// return (getPosX() == x && getPosY() >= y-1 && getPosY() <= y+1) || //
		// x相等 y浮动为1
		// (getPosY() == y && getPosX() >= x-1 && getPosX() <= x+1); // y相等
		// x浮动为1
		// return getPosY() >= y-1 && getPosY() <= y+1 && getPosX() >= x-1 &&
		// getPosX() <= x+1;
		return true;
	}

	// 构造函数
	public InstLootMapHumanData() {
		pos = new Vector2D();
		buffs = new HashSet<InstLootMapBuff>();
		itemBag = new ArrayList<InstLootMapBagItem>();
	}

	// Human 构造
	public InstLootMapHumanData(Human human, HumanExtInfo exInfo, ELootMapType lootMapType) {
		this();
		this.lootMapType = lootMapType;
		init(human, exInfo);
	}

	// Human init
	public void init(Human human, HumanExtInfo exInfo) {
		if (human == null)
			return;
		// 角色特殊字段
		init(human.getId(), human.getName(), human.getDefaultModelSn(), human.getLevel());
		// 设置初始数值
		setInitValue();

		score = exInfo.getLootMapMultipleScore();
		todayScore = exInfo.getLootMapMultipleTodayScore();

		activeRobNum = ParamManager.lootMapCombatFrequencyLimit[0];
		passiveRobNum = ParamManager.lootMapCombatFrequencyLimit[1];
		oneScore = ParamManager.initLootMapStartScore;
	}

	// 玩家基本信息初始化
	private void init(long humanId, String name, int modelSn, int level) {
		this.humanId = humanId;
		this.name = name;
		this.modelSn = modelSn;
		this.level = level;

	}

	public void setSkill(InstLootMapSkill skill) {
		this.skill = skill;
	}

	public InstLootMapSkill getSkill() {
		return skill;
	}

	public int getOneScore() {
		return oneScore;
	}

	public int setOneScore(int s) {
		oneScore = s;
		if (oneScore < 0) {
			oneScore = 0;
		}
		if (stage instanceof StageObjectLootMapMultiple) {
			int rewaradSn = scoreReward.getReward(oneScore);// 判断奖励是否领取
			return rewaradSn;
		}
		return 0;
	}

	/**
	 * 设置被领取
	 */
	public void setOneScoreReceive() {
		scoreReward.setReceive(oneScore);
	}

	public void dead() {
		if (hp != 0) {
			hp = 0;
		}
		clearBuff();
	}

	/**
	 * 玩家复活
	 */
	public void revival() {
		setInitValue();
		clearBuff();
		deadTime = 0;
	}

	/**
	 * 设置初始数值
	 */
	private void setInitValue() {
		switch (lootMapType) {
		case LootMapSingle:
		case LootMapEgg: {
			ConfLevelExp conf = ConfLevelExp.get(level);
			if (conf == null) {
				attack = 10;
				maxHp = 100;
				Log.lootMap.error("   ========   setInitValue ConfLevelExp is null level = {}", level);
			} else {
				attack = conf.lootMapInfo[0];
				maxHp = conf.lootMapInfo[1];
			}
		}
			break;
		case LootMapMultip: {
			int[] startCombatDate = ParamManager.lootMapPvpStartCombatDate;
			attack = startCombatDate[0];
			maxHp = startCombatDate[1];
		}
			break;
		default:
			break;
		}
		buffAttack = attack;
		hp = maxHp;
	}

	// 移除Buff
	private void clearBuff() {
		if (buffs != null && buffs.size() != 0) {
			buffs.clear();
		}
	}

	// 进入战斗
	public void battle() {
		subAttackBuffCount();
	}

	// 在地图内做砍怪动作
	public void mapBattle() {
		subAttackBuffCount();
	}

	// buff次数扣除
	private void subAttackBuffCount() {
		Iterator<InstLootMapBuff> it = buffs.iterator();
		boolean isRmv = false;
		while (it.hasNext()) {
			InstLootMapBuff buffObj = it.next();
			if (InstLootMapBuff.isAttackBuff(buffObj.sn) && --buffObj.count <= 0) {
				it.remove();
				isRmv = true;
			}
		}
		if (isRmv) {
			initAttack();
		}
	}

	/**
	 * 添加buff
	 * 
	 * @param buffSn
	 * @param num
	 */
	public void addBuff(int buffSn, int num) {
		if (InstLootMapBuff.isHp(buffSn)) {
			hp += InstLootMapBuff.getAddHp(maxHp, buffSn);
			if (hp > maxHp) {
				hp = maxHp;
			}
		} else if (InstLootMapBuff.isAttack(buffSn)) {
			InstLootMapBuff buf = new InstLootMapBuff(buffSn, num);
			buffs.add(buf);
			initAttack();
		}
	}

	private void initAttack() {
		buffAttack = 0;
		Iterator<InstLootMapBuff> it = buffs.iterator();
		int addAttack = 0;
		while (it.hasNext()) {
			InstLootMapBuff buffObj = it.next();
			addAttack += (int) InstLootMapBuff.getAddAttack(attack, buffObj.sn);
		}
		buffAttack = attack + addAttack;

		if (buffAttack > ParamManager.lootMapHumanMaxAttack) {
			buffAttack = ParamManager.lootMapHumanMaxAttack;
		} else if (buffAttack < ParamManager.lootMapHumanMinAttack) {
			buffAttack = ParamManager.lootMapHumanMinAttack;
		}
	}

	/**
	 * 获取攻击buff
	 * 
	 * @return
	 */
	public int getAttack() {
		return buffAttack;
	}

	public int getHp() {
		return hp;
	}

	public void setHp(int hp) {
		if (hp < 0) {
			hp = 0;
		}
		if (hp > ParamManager.lootMapHumanMaxHp) {
			hp = ParamManager.lootMapHumanMaxHp;
		}
		this.hp = hp;
	}

	public void attackMonster(int monsterAttack) {
		hp -= monsterAttack;
		hp = hp < 0 ? 0 : hp;
	}

	public int getPosX() {
		return (int) pos.x;
	}

	public int getPosY() {
		return (int) pos.y;
	}

	public void setPosX(float x) {
		setPosX((int) x);
	}

	public void setPosY(float y) {
		setPosY((int) y);
	}

	public void setPosX(int x) {
		pos.x = x;
	}

	public void setPosY(int y) {
		pos.y = y;
	}

	/**
	 * 获取DMemberInfo.Builder
	 * 
	 * @return
	 */
	public DMemberInfo.Builder getDMemberInfo() {
		DMemberInfo.Builder builder = DMemberInfo.newBuilder();
		builder.setId(humanId);
		builder.setName(name);
		builder.setModelSn(modelSn);
		builder.setLevel(level);
		return builder;
	}

	/**
	 * 获取一个DLootMapHuman
	 * 
	 * @return
	 */
	public DLootMapHuman.Builder getDLootMapHuman() {

		DMemberInfo.Builder memberMsg = getDMemberInfo();

		DVector2.Builder pos = DVector2.newBuilder();
		pos.setX(getPosX());
		pos.setY(getPosY());

		DLootMapHuman.Builder msg = DLootMapHuman.newBuilder();
		msg.setHumanInfo(memberMsg);
		msg.setIsPking(isPking);
		msg.setIsProtecting(isProtecting);
		msg.setIsRedName(isRedName);

		msg.setActiveRobNum(activeRobNum);
		msg.setPassiveRobNum(passiveRobNum);
		msg.setKillNumber(killNumber);
		msg.setOneRank(oneRank);
		msg.setOneScore(oneScore);
		msg.setRank(rank);
		msg.setScore(score);
		msg.setAttack(attack);
		msg.setHp(hp);
		msg.setMaxHp(maxHp);
		if (skill != null) {
			msg.setEggSkill(skill.getDLootMapSkill());
		}

		for (InstLootMapBuff buf : buffs) {
			msg.addBuffs(buf.sn);
		}

		msg.setPos(pos);

		List<DItem.Builder> itemList = getAllItem();
		for (int i = 0; i < itemList.size(); i++) {
			msg.addItemList(itemList.get(i));
		}
		return msg;
	}

	public List<DItem.Builder> getAllItem() {
		List<DItem.Builder> list = new ArrayList<DItem.Builder>();
		for (int i = 0; i < itemBag.size(); i++) {
			InstLootMapBagItem item = itemBag.get(i);
			list.add(item.getDitem());
		}
		return list;
	}

	public List<ProduceVo> getAllItemProduceVo() {
		List<ProduceVo> list = new ArrayList<ProduceVo>();
		for (int i = 0; i < itemBag.size(); i++) {
			InstLootMapBagItem item = itemBag.get(i);
			ProduceVo vo = new ProduceVo(item.sn, item.number);
			list.add(vo);
		}
		return list;
	}

	private InstLootMapBagItem tryGetBagItem(int itemSn) {
		for (int i = 0; i < itemBag.size(); i++) {
			InstLootMapBagItem item = itemBag.get(i);
			if (item.sn == itemSn) {
				return item;
			}
		}
		return null;
	}

	private InstLootMapBagItem getBagItem(int itemSn) {
		InstLootMapBagItem item = tryGetBagItem(itemSn);
		if (item == null) {
			item = new InstLootMapBagItem();
			item.sn = itemSn;
			item.number = 0;
			itemBag.add(item);
		}
		return item;
	}

	/**
	 * 排行榜用
	 * 
	 * @return
	 */
	public RankData getRankData() {
		RankData data = new RankData();
		data.humanId = humanId;
		data.name = name;
		data.level = level;
		data.modelSn = modelSn;
		data.lootMapScore = getRankDataScore();
		return data;
	}

	/**
	 * 获取排行榜需要的score 由现在的score + 该场增加的分数
	 * 
	 * @return
	 */
	public long getRankDataScore() {
		return score + getAddScore();
	}

	/**
	 * 获取增加的分数 今日最高 > 该场 -> 0 该场 > 今日最高 = 该场 -今日最高
	 * 
	 * @return
	 */
	public int getAddScore() {
		return todayScore > oneScore ? 0 : (oneScore - todayScore);
	}

	public int getTodayTop1Score() {
		return todayScore > oneScore ? todayScore : oneScore;
	}
	
	/**
	 * 增加道具
	 * 
	 * @param itemSn
	 * @param num
	 */
	public void addItem(int itemSn, int num) {
		if (itemSn == EMoneyType.lootScore_VALUE) {
			setOneScore(getOneScore() + num);
			return;
		}

		InstLootMapBagItem item = getBagItem(itemSn);
		item.number += num;
	}

	// /**
	// * 移除道具
	// * @param itemSn
	// * @param num
	// */
	// public void rmvItem(int itemSn,int num){
	// if(itemSn == EMoneyType.lootScore_VALUE){
	// setOneScore(getOneScore() - num);
	// return;
	// }
	// //判断是否存在
	// if(itemMap.containsKey(itemSn)){
	// int value = itemMap.get(itemSn);
	// value -= num;
	// if(value <= 0){
	// itemMap.remove(value);
	// }else{
	// itemMap.put(itemSn, value);
	// }
	// }
	// }
	
	
	public PKHumanInfo getPKHumanInfo() {
		double hpRatio = hp / (double) maxHp;
		if (hpRatio >= 1) {
			hpRatio = 1;
		}
		double atkRatio =  getAttack() / (double) attack;
		if (atkRatio >= 1) {
			atkRatio = 1;
		}
		String[] propName = {FightPropName.HpCur.value(), FightPropName.Atk.value()};
		double[] multiply = {hpRatio, atkRatio};
	    List<Integer> buffList = new ArrayList<>();
	    for (InstLootMapBuff buf : buffs) {
	    	buffList.add(buf.sn);
	    }
	    PKHumanInfo vo = new PKHumanInfo(humanId, propName, multiply, buffList);
	    return vo;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(humanId);
		out.write(name);
		out.write(modelSn);
		out.write(isPking);
		out.write(isProtecting);
		out.write(isRedName);
		out.write(activeRobNum);
		out.write(passiveRobNum);
		out.write(skill);
		out.write(killNumber);
		out.write(oneRank);
		out.write(oneScore);
		out.write(score);
		out.write(rank);
		out.write(attack);
		out.write(hp);
		out.write(pos);
		out.write(buffs);
		out.write(itemBag);
		out.write(level);
		out.write(scoreReward);
		out.write(maxHp);

		out.write(buffAttack);
		out.write(deadTime);
		out.write(protectedTime);
		out.write(isPKTrigger);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		humanId = in.read();
		name = in.read();
		modelSn = in.read();
		isPking = in.read();
		isProtecting = in.read();
		isRedName = in.read();
		activeRobNum = in.read();
		passiveRobNum = in.read();
		skill = in.read();
		killNumber = in.read();
		oneRank = in.read();
		oneScore = in.read();
		score = in.read();
		rank = in.read();
		attack = in.read();
		hp = in.read();
		pos = in.read();

		buffs.clear();
		buffs = in.read();

		itemBag.clear();
		itemBag = in.read();

		level = in.read();
		scoreReward = in.read();
		maxHp = in.read();

		buffAttack = in.read();
		deadTime = in.read();
		protectedTime = in.read();
		isPKTrigger = in.read();
	}

}
