package game.worldsrv.instWorldBoss;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.entity.Human;

public class WBHarmData implements ISerilizable {
	public long rankTime = 0; // 最近一次更新排行值的时间
	// 玩家信息
	public long humanId = 0; // 角色Id
	public String name = ""; // 角色名字
	public int modelSn = 0; // 角色模型sn
	// 其他信息
	public int harm = 0; // 伤害值
	public boolean isKiller = false;// 是否击杀BOSS的玩家
	
	public WBHarmData() {

	}

	public WBHarmData(Human human) {
		// 玩家信息
		this.humanId = human.getId();
		this.name = human.getName();
		this.modelSn = human.getModelSn();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(rankTime);
		// 角色特殊字段
		out.write(humanId);
		out.write(name);
		out.write(modelSn);
		out.write(harm);
		out.write(isKiller);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		rankTime = in.read();
		// 角色特殊字段
		humanId = in.read();
		name = in.read();
		modelSn = in.read();
		harm = in.read();
		isKiller = in.read();
	}

}
