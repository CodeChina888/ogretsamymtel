package game.worldsrv.fightParam;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.character.HumanMirrorObject;

public class CompeteParam implements ISerilizable {
	public HumanMirrorObject fightHumanMirrorObj;// 对手镜像数据
	
	
	public CompeteParam() {
		
	}
	
	public CompeteParam(HumanMirrorObject fightHumanMirrorObj) {
		this.fightHumanMirrorObj = fightHumanMirrorObj;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(fightHumanMirrorObj);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		fightHumanMirrorObj = in.read();
	}
	
}
