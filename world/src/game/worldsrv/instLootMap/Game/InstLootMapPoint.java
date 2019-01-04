package game.worldsrv.instLootMap.Game;

public class InstLootMapPoint {
	/**
	 * 判断移动点是否是相邻
	 * @param curX 当前x
	 * @param curY 当前y
	 * @param nowX 现在x
	 * @param nowY 现在y
	 */
	public static boolean isAroundPoint(int curX,int curY,int nowX,int nowY){
//		return (curX==nowX && curY!=nowY) || (curX!=nowX && curY==nowY);
		return curX == nowX || curY == nowY;
	}
	
	/**
	 * 获取范围内容
	 * @param curX 当前x
	 * @param curY 当前y
	 * @param rangeX x范围
	 * @param rangeY y范围
	 * @param mapMaxX 地图极值
	 * @param mapMaxY 地图极值
	 * @return 返回int[] {minx,miny,maxx,maxy}
	 */
	public static int[] getRangePoint(int curX,int curY,int rangeX,int rangeY,int mapMaxX,int mapMaxY){
		int[] p = new int[4];
		int minX = curX - rangeX;
		int maxX = curX + rangeX;
		int minY = curY - rangeY;
		int maxY = curY + rangeY;
		//设置值的合法范围
		p[0] = getLimitValue(minX,mapMaxX); // minx
		p[1] = getLimitValue(minY,mapMaxY); // miny
		p[2] = getLimitValue(maxX,mapMaxX); // maxx
		p[3] = getLimitValue(maxY,mapMaxY); // maxy
		return p;
	}
	
	/**
	 * 把值定在区间内
	 * @param curValue 需要限定的值 
	 * @param maxValue 最大值(最小值 = 0)
	 * @return
	 */
	private static int getLimitValue(int curValue,int maxValue){
		if(curValue < 0){// 小于最小值
			curValue = 0;
		}else if(curValue >= maxValue){// 大于最大值
			curValue = maxValue-1;
		}
		return curValue;
	}
	
	public static boolean isOnePoint(int x1,int y1,int x2,int y2){
		return (x1 == x2) && (y1 == y2);
	}
	
	public static boolean isInRange(int x,int y,int[] range){
		int minX = range[0];
		int minY = range[1];
		int maxX = range[2];
		int maxY = range[3];
		return x >= minX && x <= maxX && y <= maxY &&  y >= minY;
	}
}
