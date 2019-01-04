package game.worldsrv.fightParam;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class PassParam implements ISerilizable {
	public boolean isPass = false;// 是否通关过：是则可扫荡
	
	
	public PassParam() {
		
	}
	
	public PassParam(boolean isPass) {
		this.isPass = isPass;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(isPass);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		isPass = in.read();
	}
	
}
