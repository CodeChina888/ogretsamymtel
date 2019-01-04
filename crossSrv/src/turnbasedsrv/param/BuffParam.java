package turnbasedsrv.param;

import java.util.ArrayList;
import java.util.List;

public class BuffParam {
	public long recordId = 0;// 战斗记录id
	public boolean isWin = false;// 是否胜利
	public List<Integer> hpLeft = new ArrayList<>();// 敌方剩余的血量

	public BuffParam() {

	}

	public BuffParam(long recordId, boolean isWin) {
		this.recordId = recordId;
		this.isWin = isWin;
	}

	public BuffParam(long recordId, boolean isWin, List<Integer> hpLeft) {
		this.recordId = recordId;
		this.isWin = isWin;
		this.hpLeft.addAll(hpLeft);
	}

}
