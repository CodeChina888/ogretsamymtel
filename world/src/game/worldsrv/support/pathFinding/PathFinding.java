package game.worldsrv.support.pathFinding;

import game.worldsrv.support.Vector2D;
import game.worldsrv.support.Vector3D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.support.SysException;
import game.worldsrv.config.ConfMap;
import game.worldsrv.support.pathFinding.PathFinding;
import game.worldsrv.support.pathFinding.PathFindingFlagKey;

/**
 * 寻路核心类
 */
public class PathFinding {
	// nav文件所在目录及其后缀
	private static String navDir = "META-INF/stageConfig/mesh/";
	private static String navSuffix = ".bytes";
	private static Map<Integer, Boolean> mapNAVFileExist = new HashMap<Integer, Boolean>();// 记录地图SN对应的导航网格文件是否存在
	
	
	static {
		
	}
	
	/**
	 * 初始化加载所有地图导航数据，需遍历nav导航网格数据所在文件夹
	 */
	public static void init() {
		// 加载寻路用到的C++动态库recast.dll
		System.loadLibrary("recast");
		
		// 检查地图配置的导航网格文件是否存在
		checkFilesExist();

		String basePath = Utils.getClassPath(navDir);
		//System.out.println("===PathFinding.init(),basePath=" + basePath);		
		File dir = new File(basePath);
		if (!dir.isDirectory())
			return;

		// 遍历nav文件所在目录
		File[] files = dir.listFiles();
		if (files == null)
			return;

		for (File f : files) {
			// 过滤不是nav文件
			String fileName = f.getName();
			if (!fileName.endsWith(navSuffix))
				continue;

			int stageSnCode = fileName.replace(navSuffix, "").hashCode();
			//Log.stageCommon.info("====stageSnCode={},fileName={}",stageSnCode,fileName);
			loadNavData(stageSnCode, f.getAbsolutePath());
		}
		Log.stageCommon.info("====加载导航网格完毕，navDir={}", navDir);
		
		/*
		 * //new // 给每个stagePort都初始化寻路相关数据 for(int i = 0; i <
		 * D.PORT_STAGE_STARTUP_NUM; ++i) { //拼PortId String portId =
		 * D.PORT_STAGE_PREFIX + i; // 遍历nav文件所在目录 File[] files =
		 * dir.listFiles(); for(File f : files) { // 过滤不是nav文件 String fileName =
		 * f.getName(); if(!fileName.endsWith(navSuffix)) continue; int
		 * stageSnCode = fileName.replace(navSuffix, "").hashCode(); int
		 * stageSnPortCode = (portId + fileName.replace(navSuffix,
		 * "")).hashCode(); boolean result = loadNavData(stageSnCode,
		 * stageSnPortCode, f.getAbsolutePath()); if(!result) { SysException e =
		 * new SysException(Utils.createStr("加载nav数据出错，stageInfo={}", portId +
		 * fileName.replace(navSuffix, ""))); throw e; } } }
		 */
	}

	/**
	 * 检查地图配置的导航网格文件是否存在
	 */
	public static void checkFilesExist() {
		Collection<ConfMap> confList = ConfMap.findAll();
		boolean isAllExist = true;// 是否都存在
		for (ConfMap confMap : confList) {
			// 判断导航网格文件是否都存在，如果不存在则直接抛出异常
			String basePath = Utils.getClassPath(navDir);
			//System.out.println("===PathFinding.checkFilesExist(),basePath=" + basePath);
			File navFile = new File(basePath + confMap.sceneAsset[0] + navSuffix);
			if (!navFile.exists()) {
				isAllExist = false;
				mapNAVFileExist.put(confMap.sn, false);
				Log.stageCommon.error("===地图缺失导航网格：mapSn={},asset={},filePath={}", confMap.sn, confMap.sceneAsset[0],
						navFile.getPath());
			} else {
				mapNAVFileExist.put(confMap.sn, true);
			}
		}
		if (!isAllExist) {
			throw new SysException(Utils.createStr("===地图缺失部分导航网格，无法启动！！！"));
		}
	}

	/**
	 * 初始化并加载nav数据
	 * @param navPath
	 */
	private static native void loadNavData(int mapSn, String navPath);

	// private static native boolean loadNavData(int mapSn, int stageSnPort,
	// String navPath);//new

	// new
	// private static int getStageSnPort(String name) {
	// return (Port.getCurrent().getId() + name).hashCode();
	// }
	
	/**
	 * 根据起点终点坐标找到路径
	 * @param startPos
	 * @param endPos
	 * @return
	 */
	public static List<Vector3D> findPaths(int mapSn, Vector3D startPos, Vector3D endPos) {
		return findPaths(mapSn, startPos, endPos, PathFindingFlagKey.init);
	}

	/**
	 * 根据起点，终点，掩码寻路，返回Vector2路径列表
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	public static List<Vector3D> findPaths(int mapSn, Vector3D startPos, Vector3D endPos, int flag) {
		List<Vector3D> result = new ArrayList<>();
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn ={}", mapSn);
			return result;
		}
		if (startPos.isZero() || startPos.isEqual(endPos)) {
			// Log.stageCommon.error("===PathFinding.findPaths startPos={},endPos={}",
			// startPos, endPos);
			return result;
		}
		if (startPos.isWrongPos() || endPos.isWrongPos()) {
			Log.stageCommon.error("===PathFinding.findPaths has wrong pos:mapSn={},startPos={},endPos={}", mapSn,
					startPos, endPos);
			return result;
		}

		// 转换坐标为float[]
		float[] start = startPos.toDetourFloat3();
		float[] end = endPos.toDetourFloat3();

		// 寻路结果
		String asset = confMap.sceneAsset[0];
		int stageSnCode = asset.hashCode();// getStageSnPort(asset);//
		float[] paths = null;
		paths = findPath(stageSnCode, start, end, flag);
		if (paths != null) {
			// Log.stageCommon.debug("===PathFinding.findPaths mapSn={},start={},end={},paths={}",
			// confMap.sn, start, end,
			// paths);//sjh
			int len = paths.length;
			// 转成需要的Vector2
			for (int i = 0; i <= len - 3; i += 3) {// 防止len不足3的倍数，导致数组越界
				Vector3D point = new Vector3D(paths[i], paths[i + 1], paths[i + 2]).toServFromDetour();
				result.add(point);
			}
		}

		return result;
	}

	/**
	 * 根据起点，终点，掩码寻路
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	private static native float[] findPath(int mapSn, float[] startPos, float[] endPos, int flag);

	/**
	 * 判断两个点是否能到达，判断标准为recase找到的终点与给定终点距离小于0.1
	 * @param mapSn
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	public static boolean canReach(int mapSn, Vector3D startPos, Vector3D endPos, int flag) {
		List<Vector3D> paths = findPaths(mapSn, startPos, endPos, flag);
		if (paths.isEmpty())
			return false;

		Vector3D pathEnd = paths.get(paths.size() - 1);
		if (pathEnd.distance(endPos) <= 0.1)
			return true;

		return false;
	}

	/**
	 * 判断坐标是否在阻挡区域内
	 * @param pos
	 * @return
	 */
	public static boolean isPosInBlock(int mapSn, Vector3D pos) {
		boolean ret = false;
		try {
			ConfMap confMap = ConfMap.get(mapSn);
			if (confMap == null) {
				Log.table.error("ConfMap配表错误，no find sn ={}", mapSn);
				return ret;
			}
			if (pos.isWrongPos()) {
				Log.stageCommon.error("===PathFinding.isPosInBlock has wrong pos:mapSn={},pos={}", mapSn, pos);
				return ret;
			}

			if (mapNAVFileExist.containsKey(confMap.sn)) {
				// 判断下"classes\META-INF\stageConfig\mesh"目录里是否有该文件
				// （防止文件不存在，调用C++的寻路库出错，导致服务器挂掉）
				if (!mapNAVFileExist.get(confMap.sn)) {
					Log.stageCommon.error("===地图缺失导航网格：mapSn={}, asset={}", confMap.sn, confMap.sceneAsset[0]);
				} else {// 存在导航网格文件才检查
					int stageSnCode = confMap.sceneAsset[0].hashCode();// getStageSnPort(confMap.asset);//
					ret = isPosInBlock(stageSnCode, new float[]{(float) pos.y, (float) pos.z, (float) pos.x});
				}
			}
		} catch (Exception e) {
			Log.stageCommon.error("PathFinding.isPosInBlock : " + e.getMessage());
			// e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 判断坐标是否在阻挡区域内
	 * @param pos
	 * @return
	 */
	private static native boolean isPosInBlock(int mapSn, float[] pos);

	/**
	 * 检测起点到终点是否有阻挡，有则返回阻挡坐标，无则返回终点坐标
	 * @param startPos
	 * @param endPos
	 * @return
	 */
	public static Vector3D raycast(int mapSn, Vector3D startPos, Vector3D endPos) {
		return raycast(mapSn, startPos, endPos, PathFindingFlagKey.init);
	}

	/**
	 * 检测起点到终点是否有阻挡，有则返回阻挡坐标，无则返回终点坐标 FIXME 待删除 由于导航网格bug，所以需要把坐标延长1000米，然后修正
	 * 等代码修整后注释这段代码
	 * @param mapSn
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	public static Vector3D raycast(int mapSn, Vector3D startPos, Vector3D endPos, int flag) {
		Vector3D result = new Vector3D();
		ConfMap confMap = ConfMap.get(mapSn);
		if (confMap == null) {
			Log.table.error("ConfMap配表错误，no find sn ={}", mapSn);
			return result;
		}
		if (startPos.isZero() || startPos.isEqual(endPos)) {
			// Log.stageCommon.error("===PathFinding.raycast startPos={}, endPos={}",
			// startPos, endPos);
			return result;
		}
		if (startPos.isWrongPos() || endPos.isWrongPos()) {
			Log.stageCommon.error("===PathFinding.raycast has wrong pos:mapSn={},startPos={},endPos={}", mapSn,
					startPos, endPos);
			return result;
		}

		Vector2D new2D = new Vector2D();
		Vector3D new3D = new Vector3D(endPos.x, endPos.y, endPos.z);
		new2D = new3D.sub(startPos).toVector2D().normalize().mul(9999);

		// 转换坐标为float[]
		float[] start = startPos.toDetourFloat3();
		float[] end = endPos.toDetourFloat3();

		end[0] = (float) new2D.y;
		end[2] = (float) new2D.x;

		float[] endFix = new float[3];
		endFix[0] = end[0] - start[0];

		// 检测结果
		String asset = ConfMap.get(mapSn).sceneAsset[0];
		int stageSnCode = asset.hashCode();// getStageSnPort(asset);//
		float[] paths = raycast(stageSnCode, start, end, flag);
		if (paths != null && paths.length >= 3) {
			// 转成需要的Vector2
			result = new Vector3D(paths[0], paths[1], paths[2]).toServFromDetour();
			// Log.stageCommon.info("raycast {} {} {} {}",end[0], end[1], end[2],
			// result );
		}

		if (result.distanceFar(startPos) > endPos.distanceFar(startPos)) {
			return endPos;
		}

		return result;

		/*
		 * 旧有代码 //转换坐标为float[] float[] start = startPos.toDetourFloat3();
		 * float[] end = endPos.toDetourFloat3(); // 检测结果 String asset =
		 * ConfMap.get(mapSn).asset; int stageSnCode = asset.hashCode(); float[]
		 * paths = raycast(stageSnCode, start, end, flag); // 转成需要的Vector2
		 * Vector3D result = new Vector3D(paths[0], paths[1],
		 * paths[2]).toServFromDetour(); return result;
		 */
	}

	/**
	 * 检测起点到终点是否有阻挡，有则返回阻挡坐标，无则返回终点坐标
	 * @param startPos
	 * @param endPos
	 * @param flag 包含的阻挡标志(setIncludeFlags)
	 * @return
	 */
	private static native float[] raycast(int mapSn, float[] startPos, float[] endPos, int flag);

	/**
	 * 获得坐标对应的高度
	 * @param pos
	 * @return
	 */
	private static native float[] posHeight(int mapSn, float[] pos);

	public static Vector3D posHeight(int mapSn, Vector2D pos) {
		// lock by sjh,在linux下有时候出错，就导致服务器挂了，很危险所以屏蔽掉
		if (pos.isWrongPos()) {
			Log.stageCommon.error("===PathFinding.posHeight has wrong pos:mapSn={},pos={}", mapSn, pos);
			return new Vector3D();
		}
		return new Vector3D(pos.x, pos.y, 0);

		// ConfMap confMap = ConfMap.get(mapSn);
		// if(confMap == null) {
		// Log.table.error("ConfMap配表错误，no find sn ={}",mapSn);
		// return result;
		// }
		// if(pos.isZero()) {
		// //Log.stageCommon.error("===PathFinding.posHeight pos={}", pos);
		// return result;
		// }
		//
		// String asset = confMap.asset;
		// int stageSnCode = asset.hashCode();//getStageSnPort(asset);//
		// float[] temp = null;
		// try {
		// temp = posHeight(stageSnCode, new float[]{(float)pos.y, 0,
		// (float)pos.x});
		// } catch(Exception e) {
		// throw new SysException(e);
		// }
		// //Vector3D result = new Vector3D();
		// //result.x = pos.x;
		// //result.y = pos.y;
		// if(temp != null) {
		// result.z = temp[1];
		// }
		// return result;
	}

}
