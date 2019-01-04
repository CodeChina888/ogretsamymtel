package game.worldsrv.rank;

import game.worldsrv.character.HumanObject;
import game.worldsrv.entity.Guild;
import game.worldsrv.entity.Human;
import game.worldsrv.support.Log;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class RankData implements ISerilizable {
	public long rankTime = -1; // 最近一次更新排行值的时间
	// 角色特殊字段
	public long humanId = -1; // 角色Id
	public String name = ""; // 角色名字
	public int level = -1; // 角色等级
	public int vipLevel = -1; // 角色VIP等级
	public int combat = -1; // 最强整容战力
	public int modelSn = -1; // 角色模型sn
	public int equipWeaponSn = -1; // 角色带的武器sn
	public int equipClothesSn = -1; // 角色带的衣服sn
	public int fashionClothesSn = -1; // 角色带的时装sn
	public int isFashionShow = -1; // 角色是否显示时装(0false,1true)
	public int sumCombat = -1;// 总战斗力
	public String genLineupBriefInfo = "{}";//武将阵容简要信息
	public int icon = -1;// 头像 
	public String accountId = "-1";//用户ID
	// 公会特殊字段
	public long guildId = -1; // 公会Id
	public String guildName = ""; // 公会名字
	public int guildLevel = -1; // 公会等级
	public int guildExp = 0; // 公会建设度
	public long guildLeaderId = -1; // 公会会长Id
	public String guildLeaderName = ""; // 公会会长名
	public int num = 1;//工会现有人数
	public int guildIcon = 0;//工会图标
	// 爬塔特殊字段
	public int maxFloor = -1; // 爬塔的最大层数
	public int costTime = -1; // 爬塔耗时
	public int difficultly = -1;// 难度
	public int towerScore = -1; // 爬塔积分
	// 副本特殊字段
	public int stars = -1; // 玩家星星数
	
	// 洞天福地字段
	public long lootMapScore = -1; // 洞天福地积分
	
	
	public RankData() {

	}
	public RankData(HumanObject humanObj){
		if (humanObj == null) {
			Log.game.info("humanObj is null");
			return;
		}
		Human human = humanObj.getHuman();
		if (human != null) {
			setHumanInfo(humanObj, "{}");
		}
	}
	public RankData(Guild guild){
		if (guild == null) {
			Log.game.info("guild is null");
			return;
		}
		setGuildInfo(guild);
	}

	/**
	 * 记录角色信息
	 */
	private void setHumanInfo(HumanObject humanObj, String genBriefInfo) {
		if (humanObj == null)
			return;
		Human human = humanObj.getHuman();
		if (human == null)
			return;
		// 角色特殊字段
		this.humanId = human.getId();
		this.name = human.getName();
		this.level = human.getLevel();
		this.vipLevel = human.getVipLevel();
		this.accountId = human.getAccountId();
		//战斗力必须是总战斗力
		this.combat = human.getSumCombat();
		this.sumCombat = human.getSumCombat();
		this.modelSn = human.getDefaultModelSn();
		this.icon = human.getHeadSn();
		/*
		this.equipWeaponSn = human.getEquipWeaponSn();
		this.equipClothesSn = human.getEquipClothesSn();
		*/
		this.fashionClothesSn = human.getFashionSn();
		this.isFashionShow = human.isFashionShow() ? 1 : 0;
		
		this.genLineupBriefInfo = genBriefInfo;
		
		this.costTime = (int)humanObj.getHumanExtInfo().getTowerPassTime(); // 爬塔耗时
		this.maxFloor = humanObj.getHumanExtInfo().getTowerMaxFloor(); // 爬塔层数
		this.difficultly = humanObj.getHumanExtInfo().getTowerSelDiff(); // 最后过关的难度
		this.towerScore = humanObj.getHumanExtInfo().getTowerScore(); // 爬塔积分
		this.lootMapScore = humanObj.getHumanExtInfo().getLootMapMultipleScore(); // 洞天福地积分
		this.stars = human.getRankInstStars(); // 副本星星数
	}

	/**
	 * 记录公会信息
	 * @param guild
	 */
	private void setGuildInfo(Guild guild) {
		if (guild == null)
			return;
		// 公会特殊字段
		this.guildId = guild.getId();
		this.guildName = guild.getGuildName();
		this.guildLevel = guild.getGuildLevel();
		this.guildExp = guild.getGuildExp();
		this.guildLeaderId = guild.getGuildLeaderId();
		this.guildLeaderName = guild.getGuildLeaderName();
		this.num = guild.getGuildOwnNum();
		this.guildIcon = guild.getGuildIcon();
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(rankTime);
		// 角色特殊字段
		out.write(humanId);
		out.write(name);
		out.write(level);
		out.write(vipLevel);
		out.write(combat);
		out.write(modelSn);
		out.write(equipWeaponSn);
		out.write(equipClothesSn);
		out.write(fashionClothesSn);
		out.write(isFashionShow);
		out.write(sumCombat);
		out.write(genLineupBriefInfo);
		out.write(icon);
		// 公会特殊字段
		out.write(guildId);
		out.write(guildName);
		out.write(guildLevel);
		out.write(guildExp);
		out.write(guildLeaderId);
		out.write(guildLeaderName);
		out.write(num);
		out.write(guildIcon);
		// 爬塔特殊字段
		out.write(maxFloor);
		out.write(difficultly);
		out.write(costTime);
		out.write(towerScore);
		// 副本
		out.write(stars);
		
		out.write(lootMapScore);
		out.write(accountId);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		rankTime = in.read();
		// 角色特殊字段
		humanId = in.read();
		name = in.read();
		level = in.read();
		vipLevel = in.read();
		combat = in.read();
		modelSn = in.read();
		equipWeaponSn = in.read();
		equipClothesSn = in.read();
		fashionClothesSn = in.read();
		isFashionShow = in.read();
		sumCombat = in.read();
		genLineupBriefInfo = in.read();
		icon = in.read();
		// 公会特殊字段
		guildId = in.read();
		guildName = in.read();
		guildLevel = in.read();
		guildExp = in.read();
		guildLeaderId = in.read();
		guildLeaderName = in.read();
		num = in.read();
		guildIcon= in.read();
		// 爬塔特殊字段
		maxFloor = in.read();
		difficultly = in.read();
		costTime = in.read();
		towerScore = in.read();
		// 副本
		stars = in.read();
		
		lootMapScore = in.read();
		accountId = in.read();
	}
	public long getLootMapScore() {
		return lootMapScore;
	}
	public void setLootMapScore(long lootMapScore) {
		this.lootMapScore = lootMapScore;
	}

	
}
