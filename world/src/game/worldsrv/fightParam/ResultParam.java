package game.worldsrv.fightParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class ResultParam implements ISerilizable {
	public long recordId = 0;// 战斗记录id
	public boolean isWin = false;// 是否胜利
	public List<Integer> hpLeft = new ArrayList<>();// 敌方剩余的血量
	
	public ResultParam() {
		
	}
	
	public ResultParam(long recordId, boolean isWin) {
		this.recordId = recordId;
		this.isWin = isWin;
	}
	
	public ResultParam(long recordId, boolean isWin, List<Integer> hpLeft) {
		this.recordId = recordId;
		this.isWin = isWin;
		this.hpLeft.addAll(hpLeft);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(recordId);
		out.write(isWin);
		out.write(hpLeft);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		recordId = in.read();
		isWin = in.read();
		hpLeft = in.read();
	}
	
}
