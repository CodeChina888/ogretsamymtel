package game.worldsrv.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import game.worldsrv.config.ConfCompeteRobot;
import game.worldsrv.config.ConfDropInfos;
import game.worldsrv.config.ConfLevelAward;
import game.worldsrv.config.ConfPartnerPokedexFate;
import game.worldsrv.config.ConfShopProps;
import game.worldsrv.config.ConfSkillGods;
import game.worldsrv.drop.DropItemVO;

/**
 * 全局配置表,为热加载准备
 */
public class GlobalConfVal {

	// 掉落表 <DropInfos.dropId 掉落编号,掉落物品实体 >
	private static Map<Integer, List<DropItemVO>> chanceMap = new ConcurrentHashMap<>();

	// 等级奖励表<LevelAward.style 类型 ， <LevelAward.condition 奖励条件， 对应的配置列表>>
	private static Map<Integer, Map<Integer, List<ConfLevelAward>>> levelAwardMap = new ConcurrentHashMap<>();
	
	// 爆点表 <SkillGods.godsSn 爆点sn, 爆点sn对应的配置列表 >
	private static Map<Integer, List<ConfSkillGods>> skillGodsMap = new ConcurrentHashMap<>();

	// 图鉴奖励表<PartnerPokedexFate.rewardId,heroFate>
	private static Map<Integer, List<Integer>> pokedexFateMap =  new ConcurrentHashMap<>();
	
	// 突破最高排行榜奖励<奖励表sn,ConfCompeteRecobot>
	private static Map<Integer, ConfCompeteRobot> breakRankRewardMap = new ConcurrentHashMap<>();
	
	// 竞技场排名奖励<范围[0-min][1-max], snReward>
	private static Map<int[], Integer> competeRewardMap = new ConcurrentHashMap<>();
	
	// 商店信息表<ConfShopProps.storeType, List<对应的ConfShopProps.sn>>
	private static Map<Integer, List<Integer>> shopPropsMap = new ConcurrentHashMap<>();
			
	/**
	 * 重载配置表
	 */
	public static void reloadConfVal() {
		// 重载掉落表
		reload_chanceMap();
		// 重载等级奖励表
		reload_levelAwardMap();
		// 重载爆点技能表
		reload_skillGodsMap();
		// 重载图鉴奖励表
		reload_pokedexFateMap();
		// 重载突破最高排行榜奖励
		reload_breakRankRewardMap();
		// 重载竞技场排名奖励
		reload_competeRewardMap();
		// 重载商店信息表
		reload_shopPropsMap();
	}
	
	/**
	 * 重载掉落表
	 */
	private static void reload_chanceMap() {
		chanceMap.clear();
		Collection<ConfDropInfos> confAll = ConfDropInfos.findAll();
		for (ConfDropInfos conf : confAll) {
			List<DropItemVO> voList = chanceMap.get(conf.dropId);
			if (voList == null) {
				voList = new ArrayList<>();
				chanceMap.put(conf.dropId, voList);
			}
			DropItemVO vo = new DropItemVO(conf);
			voList.add(vo);
		}
	}
	/**
	 * 获取掉落表
	 * @return
	 */
	public static Map<Integer, List<DropItemVO>> getChanceMap() {
		return Collections.unmodifiableMap(chanceMap);
	}
	
	/**
	 * 重载等级奖励表
	 */
	private static void reload_levelAwardMap() {
		levelAwardMap.clear();
		Collection<ConfLevelAward> confAll = ConfLevelAward.findAll();
		for (ConfLevelAward conf : confAll) {
			Map<Integer, List<ConfLevelAward>> styleMap = levelAwardMap.get(conf.style);
			if (styleMap == null) {
				styleMap = new HashMap<>();
				levelAwardMap.put(conf.style, styleMap);
			}
			List<ConfLevelAward> list = styleMap.get(conf.condition);
			if (list == null) {
				list = new ArrayList<>();
				styleMap.put(conf.condition, list);
			}
			list.add(conf);
		}
	}
	/**
	 * 获取等级奖励表
	 * @return
	 */
	public static Map<Integer, Map<Integer, List<ConfLevelAward>>>getLevelAwardMap() {
		return Collections.unmodifiableMap(levelAwardMap);
	}
	
	/**
	 * 重载爆点技能表
	 */
	private static void reload_skillGodsMap() {
		skillGodsMap.clear();
		Collection<ConfSkillGods> confAll = ConfSkillGods.findAll();
		for (ConfSkillGods conf : confAll) {
			List<ConfSkillGods> list = skillGodsMap.get(conf.godsSn);
			if (list == null) {
				list = new ArrayList<>();
				skillGodsMap.put(conf.godsSn, list);
			}
			list.add(conf);
		}
	}
	/**
	 * 获取爆点表
	 * @return
	 */
	public static Map<Integer, List<ConfSkillGods>> getSkillGodsMap() {
		return Collections.unmodifiableMap(skillGodsMap);
	}
	
	/**
	 * 重载图鉴奖励表
	 */
	private static void reload_pokedexFateMap() {
		pokedexFateMap.clear();
		Collection<ConfPartnerPokedexFate> confAll  = ConfPartnerPokedexFate.findAll();
		for (ConfPartnerPokedexFate conf : confAll) {
			int [] heroFateArr  =conf.heroFate;
			pokedexFateMap.put(conf.rewardID, Utils.intToIntegerList(heroFateArr));
		}
	}
	/**
	 * 获取图鉴奖励表
	 */
	public static Map<Integer, List<Integer>> getPokedexFateMap() {
		return Collections.unmodifiableMap(pokedexFateMap);
	}
	
	/**
	 * 重载突破最高排行榜奖励
	 */
	private static void reload_breakRankRewardMap() {
		breakRankRewardMap.clear();
		Collection<ConfCompeteRobot> confAll = ConfCompeteRobot.findAll();
		for (ConfCompeteRobot conf : confAll) {
			breakRankRewardMap.put(conf.sn, conf);
		}
	}
	/**
	 * 获取突破最高排行榜奖励
	 */
	public static Map<Integer, ConfCompeteRobot> getBreakRankRewardMap() {
		return Collections.unmodifiableMap(breakRankRewardMap);
	}
	
	/**
	 * 重载竞技场排名奖励
	 */
	private static void reload_competeRewardMap() {
		competeRewardMap.clear();
		Collection<ConfCompeteRobot> confAll = ConfCompeteRobot.findAll();
		for (ConfCompeteRobot conf : confAll) {
			int[] range = new int[] {conf.rankMin, conf.rankMax};
			competeRewardMap.put(range, conf.dailyReward);
		}
	}
	/**
	 * 获取指定竞技场排名的奖励sn
	 */
	public static int getCompeteReward(int rank) {
		int snReward = 0;
		for (Entry<int[], Integer> enrty : competeRewardMap.entrySet()) {
			int min = enrty.getKey()[0];
			int max = enrty.getKey()[1];
			if(rank >= min && rank <= max) {
				snReward = enrty.getValue();
			}
		}
		return snReward;
	}
	
	/**
	 * 重载商店信息表
	 */
	private static void reload_shopPropsMap() {
		shopPropsMap.clear();
		ConfShopProps[] confAll = ConfShopProps.findArray();
		for (ConfShopProps conf : confAll) {
			List<Integer> list = shopPropsMap.get(conf.storeType);
			if (list == null) {
				list = new ArrayList<>();
				shopPropsMap.put(conf.storeType, list);
			}
			list.add(conf.sn);
		}
	}
	/**
	 * 获取商店货物list
	 * @param shopType 商店类型
	 */
	public static List<Integer> getShopPropsTypeList(int shopType) {
		return Collections.unmodifiableMap(shopPropsMap).get(shopType);
	}
}
