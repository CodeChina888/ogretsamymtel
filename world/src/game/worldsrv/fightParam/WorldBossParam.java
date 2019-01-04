package game.worldsrv.fightParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.instWorldBoss.WBData;

public class WorldBossParam implements ISerilizable {
	public int actInstSn;			// 活动副本SN
	public int bossInstSn; 			// 世界boss配表sn
	public List<Integer> monsterLv = new ArrayList<>();	// 怪物等级
	public List<Integer> monsterHpMax = new ArrayList<>();	// 怪物最大血量
	public List<Integer> monsterHpCur = new ArrayList<>();	// 怪物当前血量
	public int addAtkPct;// 加成攻击万分比
	
	public WorldBossParam() {
		
	}
	
	public WorldBossParam(WBData wbData, int addAtkPct) {
		actInstSn = wbData.actInstSn;
		bossInstSn = wbData.bossInstSn;
		monsterLv = wbData.monsterLv;
		monsterHpMax = wbData.monsterHpMax;
		monsterHpCur = wbData.monsterHpCur;
		this.addAtkPct = addAtkPct;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(actInstSn);
		out.write(bossInstSn);
		out.write(monsterLv);
		out.write(monsterHpMax);
		out.write(monsterHpCur);
		out.write(addAtkPct);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		actInstSn = in.read();
		bossInstSn = in.read();
		monsterLv = in.read();
		monsterHpMax = in.read();
		monsterHpCur = in.read();
		addAtkPct = in.read();
	}
	
}
