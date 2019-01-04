package game.worldsrv.instLootMap.Game.Event;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import game.msg.Define.ELootMapType;

public class InstLootMapEventHaloMonster extends InstLootMapEventMonster {
	
	int buffSn; //光环怪的buff
	
	public InstLootMapEventHaloMonster(int eventSn, int level, int humanLevel, ELootMapType mapType) {
		super(eventSn, level, humanLevel, mapType);
		//设置出生buff
		buffSn = super.getBuffSn();
	}
	
	@Override
	public int getBuffSn(){
		return buffSn;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		out.write(buffSn);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		buffSn = in.read();
	}
}
