package game.worldsrv.fightParam;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class InstanceParam implements ISerilizable {
	public int star = 0;// 副本星数
	
	
	public InstanceParam() {
		
	}
	
	public InstanceParam(int star) {
		this.star = star;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(star);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		star = in.read();
	}
	
}
