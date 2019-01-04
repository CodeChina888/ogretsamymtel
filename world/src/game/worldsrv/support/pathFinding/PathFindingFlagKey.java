package game.worldsrv.support.pathFinding;

public class PathFindingFlagKey {
	public static int walk = 0x01; // 走路
	public static int swim = 0x02; // 游泳
	public static int door = 0x04; // 穿门
	public static int jump = 0x08; // 跳跃
	public static int type0 = 0x10; // 自定义阻挡类型
	public static int type1 = 0x20; //
	public static int type2 = 0x40; //
	public static int type3 = 0x80; //
	public static int type4 = 0x100; //
	public static int type5 = 0x200; //
	public static int disabled = 0x400; // 地图阻挡区
	public static int all = 0xffff; // 全部都能过

	public static int init = initFlag(); // 地图中初始化值，除了自定义类型和地图阻挡区都能过
	public static int ignoreType = all ^ disabled; // 副本中特殊值，除了地图阻挡区都能过
	public static final int[] types = new int[]{type0, type1, type2, type3, type4, type5};

	/**
	 * 获取配置中1-5对应的真实的flag
	 * @param flag
	 * @return
	 */
	public static int getRealFlag(int flag) {
		return types[flag - 1];
	}

	/**
	 * 初始flag，disabled表示阻挡区域，默认不能过，type0-type5是按条件打开的，所以默认也不能过，
	 * 本方法表示从全部都能过中排除不能过的区域
	 * @return
	 */
	private static int initFlag() {
		return all ^ disabled ^ type0 ^ type1 ^ type2 ^ type3 ^ type4 ^ type5;
	}
}
