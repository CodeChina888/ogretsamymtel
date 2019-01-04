package game.worldsrv.fightParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class FriendBossParam implements ISerilizable {
	public long enemyId;
	public int lv;
	public int times;
	public List<Integer> hpMax = new ArrayList<>();// 最大血量
	public List<Integer> hpCur = new ArrayList<>();// 当前血量
	
	
	public FriendBossParam() {
		
	}
	
	public FriendBossParam(long enemyId, int lv, int times, List<Integer> hpMax, List<Integer> hpCur) {
		this.enemyId = enemyId;
		this.lv = lv;
		this.times = times;
		this.hpMax = hpMax;
		this.hpCur = hpCur;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(enemyId);
		out.write(lv);
		out.write(times);
		out.write(hpMax);
		out.write(hpCur);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		enemyId = in.read();
		lv = in.read();
		times = in.read();
		hpMax = in.read();
		hpCur = in.read();
	}
	
}
