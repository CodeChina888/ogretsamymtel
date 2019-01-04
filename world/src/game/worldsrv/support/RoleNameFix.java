package game.worldsrv.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import core.support.RandomUtils;
import game.worldsrv.config.ConfRoleName;
import game.worldsrv.support.Log;

/**
 * 玩家取名处理类
 */
public class RoleNameFix {
	// AllXing库即"姓(男)"
	private static Map<Integer, String> mapAllXing = new ConcurrentHashMap<>();
	// AllMing库即"名(男)"
	private static Map<Integer, String> mapAllMing = new ConcurrentHashMap<>();
	// AllXing库即"姓(女)"
	private static Map<Integer, String> mapAllXingFemale = new ConcurrentHashMap<>();
	// AllMing库即"名(女)"
	private static Map<Integer, String> mapAllMingFemale = new ConcurrentHashMap<>();
	
	/**
	 * 重新加载玩家名字表
	 */
	public static void reloadRoleName() {
		if (!mapAllXing.isEmpty())
			mapAllXing.clear();
		if (!mapAllMing.isEmpty())
			mapAllMing.clear();
		if (!mapAllXingFemale.isEmpty())
			mapAllXingFemale.clear();
		if (!mapAllMingFemale.isEmpty())
			mapAllMingFemale.clear();

		int numXing = 0;
		int numMing = 0;
		int numXingFemale = 0;
		int numMingFemale = 0;
		for (ConfRoleName conf : ConfRoleName.findAll()) {
			if (conf == null)
				continue;
			// 把男姓和男名分别存放入map
			if (checkXing(conf.sn, conf.xing, false))
				numXing++;
			if (checkMing(conf.sn, conf.ming, false))
				numMing++;
			// 把女姓和女名分别存放入map
			if (checkXing(conf.sn, conf.xingFemale, true))
				numXingFemale++;
			if (checkMing(conf.sn, conf.mingFemale, true))
				numMingFemale++;
		}
		Log.human.info("===reloadRoleName: numXing={}, numMing={}, numXingFemale={}, numMingFemale={}", numXing,
				numMing, numXingFemale, numMingFemale);
	}

	private static boolean checkXing(int sn, String xing, boolean isFemale) {
		boolean ret = false;
		if (xing != null) {
			if (xing.isEmpty()) {
				// 允许该情况
//				if (isFemale)
//					Log.human.error("===reloadRoleName: empty xingFemale={}, sn={}", xing, sn);
//				else
//					Log.human.error("===reloadRoleName: empty xing={}, sn={}", xing, sn);
			} else {
				if (isFemale) {
					if (mapAllXingFemale.containsKey(sn)) {
						Log.human.error("===reloadRoleName: repeated xingFemale={}, sn={}", xing, sn);
					} else {
						mapAllXingFemale.put(sn, xing);
						ret = true;
					}
				} else {
					if (mapAllXing.containsKey(sn)) {
						Log.human.error("===reloadRoleName: repeated xing={}, sn={}", xing, sn);
					} else {
						mapAllXing.put(sn, xing);
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	private static boolean checkMing(int sn, String ming, boolean isFemale) {
		boolean ret = false;
		if (ming != null) {
			if (ming.isEmpty()) {
				// 允许该情况
//				if (isFemale)
//					Log.human.error("===reloadRoleName: empty mingFemale={}, sn={}", ming, sn);
//				else
//					Log.human.error("===reloadRoleName: empty ming={}, sn={}", ming, sn);
			} else {
				if (isFemale) {
					if (mapAllMingFemale.containsKey(sn)) {
						Log.human.error("===reloadRoleName: repeated mingFemale={}, sn={}", ming, sn);
					} else {
						mapAllMingFemale.put(sn, ming);
						ret = true;
					}
				} else {
					if (mapAllMing.containsKey(sn)) {
						Log.human.error("===reloadRoleName: repeated ming={}, sn={}", ming, sn);
					} else {
						mapAllMing.put(sn, ming);
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * 随机获取一个名字
	 */
	public static String randomName(boolean isFemale) {
		// 随机起一个姓
		String familyName = "";
		String fistName = "";
		// 随机起一个名字
		if (isFemale) {// 女名
			if (!mapAllXingFemale.isEmpty()) {
				int familyRandom = RandomUtils.nextInt(mapAllXingFemale.size());
				familyName = mapAllXingFemale.get(familyRandom);
			}
			if (!mapAllMingFemale.isEmpty()) {
				int firstRandom = RandomUtils.nextInt(mapAllMingFemale.size());
				fistName = mapAllMingFemale.get(firstRandom);
			}
		} else {// 男名
			if (!mapAllXing.isEmpty()) {
				int familyRandom = RandomUtils.nextInt(mapAllXing.size());
				familyName = mapAllXing.get(familyRandom);
			}
			if (!mapAllMing.isEmpty()) {
				int firstRandom = RandomUtils.nextInt(mapAllMing.size());
				fistName = mapAllMing.get(firstRandom);
			}
		}
		return familyName + fistName;
	}

}
