package game.worldsrv.support.pathFinding;

import game.worldsrv.support.Vector2D;
import game.worldsrv.support.Vector3D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import core.support.SysException;
import game.worldsrv.config.ConfMap;
import game.worldsrv.config.ConfMapPosition;
import game.worldsrv.support.pathFinding.HeightFinding;

/**
 * 根据配置文件，查找坐标信息
 */
public class HeightFinding {

	// add by shenjh,每个场景的出生点高度信息
	private static Map<String, Float> mapHeightInfo = new HashMap<String, Float>();

	// 每个场景所有的坐标高度点的信息
	private static Map<String, Map<Integer, Float>> HEIGHT_INFO = new HashMap<String, Map<Integer, Float>>();

	// 每个场景所有的宽度信息，用于计算高度信息的索引值
	private static Map<String, Integer> HEIGHT_WIDTH = new HashMap<String, Integer>();

	// 高度描述文件的路径
	private static final String HEIGHT_INFO_DIR = Utils.getClassPath() + "META-INF/stageConfig/heightInfo/";

	// 高度描述文件的后缀
	private static final String INFO_SUFFIX = ".txt";

	private static final String SPLIT_SUFFIX = ",";

	// 高度描述文件需要忽略的行数
	private static final int INGORE_LINE = 1;

	public static final float NO_HEIGHT_INFO = 0;// Float.MIN_VALUE;

	static {
		//init();//不处理寻路了，所以不加载地图阻挡数据
	}

	/**
	 * 启动时候加载数据文件到内存中
	 */
	private static void init() {
		// 遍历height文件所在目录
		File dir = new File(HEIGHT_INFO_DIR);
		if (!dir.isDirectory()) {
			// add by shenjh,没有高度文件信息，则取出生点的高度
			for (ConfMapPosition confMapPos : ConfMapPosition.findAll()) {
				int mapSn = confMapPos.sn;
				if (confMapPos == null || confMapPos.humanPos == null) {
					Log.table.error("ConfMapPosition配表错误，no find sn={} or humanPos==null", mapSn);
				} else {
					String[] strPos = Utils.strToStrArraySplit(confMapPos.humanPos[0]);
					if (strPos != null && strPos.length >= 3) {
						float height = Utils.floatValue(strPos[1]);
						ConfMap confMap = ConfMap.get(mapSn);
						if (confMap != null) {
							String asset = confMap.sceneAsset[0];
							mapHeightInfo.put(asset, height);
						}
					}
				}
			}
			return;
		}

		// 挨个文件遍历，放入到Map中
		BufferedReader br = null;
		File[] files = dir.listFiles();
		if (files == null)
			return;

		for (File f : files) {
			// 过滤不是匹配类型的文件
			String fileName = f.getName();
			if (!fileName.endsWith(INFO_SUFFIX))
				continue;

			// 获取Key，用asset作为Key
			String asset = fileName.replace(INFO_SUFFIX, "");

			// 读取文件流
			try {
				br = new BufferedReader(new FileReader(f));
				String brLine = br.readLine();
				if (brLine == null)
					continue;

				String[] minBoundInfo = brLine.split(SPLIT_SUFFIX);
				// 场景可能产生的坐标
				int minx = Utils.intValue(minBoundInfo[0]);
				int miny = Utils.intValue(minBoundInfo[2]);
				// 暂时不用的空行，忽略掉
				for (int i = 0; i < INGORE_LINE; i++) {
					br.readLine();
				}

				brLine = br.readLine();
				if (brLine == null)
					continue;

				// 这一行，是地图的大小
				String[] info = brLine.split(SPLIT_SUFFIX);
				if (info == null || info.length != 2)
					throw new SysException(Utils.createStr("场景高度描述文件错误：宽高信息缺失，文件:{}，行数:{}。", fileName, INGORE_LINE + 1));

				// 实际有的坐标
				int cosX = Utils.intValue(info[0]);
				int cosY = Utils.intValue(info[1]);
				// 进行了坐标修正处理
				int cosXOffset = (int) (Utils.floatValue(info[0]) + minx);
				// int cosYOffset = (int)(Utils.floatValue(info[1]) + miny);
				// 存放一下每个场景的宽度
				HEIGHT_WIDTH.put(asset, cosXOffset);

				// 读取所有的坐标的高度
				String content = "";
				String t;
				while ((t = br.readLine()) != null) {
					content += t;
				}
				String[] height = content.split(SPLIT_SUFFIX);
				if (cosX * cosY != height.length)
					throw new SysException(Utils.createStr("场景高度描述文件错误：坐标数量不匹配。文件:{}，坐标总数:{}，高度总数:{}。", fileName, cosX
							* cosY, height.length));

				// 把高度坐标放入每个点里
				Map<Integer, Float> point = new HashMap<>();
				for (int i = 0; i < cosX; i++) {
					for (int j = 0; j < cosY; j++) {
						point.put((j + miny) * cosXOffset + (i + minx), Utils.floatValue(height[j * cosX + i]));
					}
				}

				HEIGHT_INFO.put(asset, point);

			} catch (IOException e) {
				throw new SysException(Utils.createStr("场景高度描述文件错误：读取文件错误。文件:{}。", fileName));
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					throw new SysException(e);
				}
			}
		}
	}

	/**
	 * 获取指定位置的高度
	 * @param mapSn
	 * @param pos
	 * @return
	 */
	private static Float getPosHeight(int mapSn, Vector2D pos) {
		Float height = null;
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap != null) {
			String asset = confMap.sceneAsset[0];
			if (mapHeightInfo.containsKey(asset)) {// 取出生点高度信息
				height = mapHeightInfo.get(asset);
			} else {// 取坐标点高度信息
				if (HEIGHT_INFO.containsKey(asset) && HEIGHT_WIDTH.containsKey(asset)) {
					Map<Integer, Float> mapHeightInfo = HEIGHT_INFO.get(asset);
					Integer key = (int) pos.y * HEIGHT_WIDTH.get(asset) + (int) pos.x;
					if (mapHeightInfo != null && mapHeightInfo.containsKey(key)) { // 查到高度信息
						height = mapHeightInfo.get(key);
					}
				}
			}
		} else {
			Log.table.error("ConfMap配表错误，no find sn ={}", mapSn);
		}
		return height;
	}

	/**
	 * 查找2D坐标所在的位置的3D坐标，保护高度信息
	 * @param mapSn
	 * @param pos
	 * @return
	 */
	public static Vector3D posHeight(int mapSn, Vector2D pos) {
		Vector3D result = new Vector3D();
		result.x = pos.x;
		result.y = pos.y;
		result.z = HeightFinding.NO_HEIGHT_INFO;
		Float height = getPosHeight(mapSn, pos);
		if (height != null) {
			result.z = height;
		} else {// 查无高度信息
			result.z = HeightFinding.NO_HEIGHT_INFO;
		}
		return result;
	}

	/**
	 * 查找每个3D坐标是否有高度值，并且记录
	 * @param mapSn
	 * @param pos
	 * @return
	 */
	public static boolean posHeight(int mapSn, Vector3D pos) {
		boolean ret = false;
		Vector2D posV2 = new Vector2D(pos.x, pos.y);
		Float height = getPosHeight(mapSn, posV2);
		if (height != null) {
			pos.z = height;
			ret = true;
		}
		return ret;
	}

}
