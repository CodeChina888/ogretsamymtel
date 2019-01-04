package game.worldsrv.enumType;

/**
 * 创建物件出现类型枚举
 * @author shenjh
 */
public enum SwitchState {
	InStage(0), 		// 0 在地图中，此时可在stage中处理humanObj事件
	WaitGlobal(1),		// 1 在全局中，此时可以global中处理humanObj事件
	WaitStage(2),		// 2 等待地图中，此时发往新地图
	;

	private int value;
	
	private SwitchState(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
	
}
