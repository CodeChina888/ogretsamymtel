package game.worldsrv.instLootMap.Game.Event;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import game.msg.Define.ELootMapType;

public class InstLootMapEventDoor extends InstLootMapEvent {

	public InstLootMapEventDoor(int eventSn, ELootMapType mapType) {
		super(eventSn, mapType);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
	}
}
