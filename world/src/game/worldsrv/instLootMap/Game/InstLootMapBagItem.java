package game.worldsrv.instLootMap.Game;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DItem;

public class InstLootMapBagItem implements ISerilizable{
	

	public int sn;
	public int number;
	
	public InstLootMapBagItem(){}

	@Override
	public void writeTo(OutputStream out) throws IOException {

		out.write(sn);
		out.write(number);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		number = in.read();
	}
	
	public DItem.Builder getDitem(){
		DItem.Builder builder = DItem.newBuilder();
		builder.setItemId(0);
		builder.setItemSn(sn);
		builder.setNum(number);
		return builder;
	}
}
