package game.worldsrv.instWorldBoss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.entity.WorldBoss;
import game.worldsrv.support.Utils;

public class WBData implements ISerilizable {
	public int actInstSn = 0;		//活动副本SN
	public int bossInstSn = 0;		//boss副本SN
	public int bossMapSn = 0;		//boss地图SN
	public int bossPos = 0;			//boss所在位置
	public List<Integer> monsterSn = new ArrayList<>();	//怪物sn
	public List<Integer> monsterLv = new ArrayList<>();	//怪物等级
	public List<Integer> monsterHpMax = new ArrayList<>();	//怪物最大血量
	public List<Integer> monsterHpCur = new ArrayList<>();	//怪物当前血量，0即boss死亡
	public long totalHpCur = 0;		//怪物当前血量之和
	public long killerId = 0;		//击杀boss的玩家id
	public String killerName = "";	//击杀boss的玩家名
	public String RankTopName = ""; // 上次挑战上海榜前三的玩家
	
	public WBData() {
		
	}

	public WBData(WorldBoss worldBoss) {
		this.actInstSn = worldBoss.getActInstSn();
		this.bossInstSn = worldBoss.getBossInstSn();
		this.bossMapSn = worldBoss.getBossMapSn();
		this.bossPos = worldBoss.getBossPos();
		this.monsterSn = Utils.strToIntList(worldBoss.getMonsterSN());
		this.monsterLv = Utils.strToIntList(worldBoss.getMonsterLv());
		this.monsterHpMax = Utils.strToIntList(worldBoss.getMonsterHpMax());
		this.monsterHpCur = Utils.strToIntList(worldBoss.getMonsterHpCur());
		this.totalHpCur = worldBoss.getHpCur();
		this.killerId = worldBoss.getKillerId();
		this.killerName = worldBoss.getKillerName();
		this.RankTopName = worldBoss.getRankTopName();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(actInstSn);
		out.write(bossInstSn);
		out.write(bossMapSn);
		out.write(bossPos);
		out.write(monsterSn);
		out.write(monsterLv);
		out.write(monsterHpMax);
		out.write(monsterHpCur);
		out.write(totalHpCur);
		out.write(killerId);
		out.write(killerName);
		out.write(RankTopName);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		actInstSn = in.read();
		bossInstSn = in.read();
		bossMapSn = in.read();
		bossPos = in.read();
		monsterSn = in.read();
		monsterLv = in.read();
		monsterHpMax = in.read();
		monsterHpCur = in.read();
		totalHpCur = in.read();
		killerId = in.read();
		killerName = in.read();
		RankTopName = in.read();
	}

	/**
	 * 转为文本显示
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("actInstSn", actInstSn)
				.append("bossInstSn", bossInstSn)
				.append("bossMapSn", bossMapSn)
				.append("bossPos", bossPos)
				.append("monsterSn", monsterSn)
				.append("monsterLv", monsterLv)
				.append("monsterHpMax", monsterHpMax)
				.append("monsterHpCur", monsterHpCur)
				.append("killerId", killerId)
				.append("killerName", killerName)
				.append("RankTopName", RankTopName)
				.toString();
	}
}
