package game.worldsrv.fightParam;

import java.io.IOException;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.character.HumanMirrorObject;

public class TowerParam implements ISerilizable {
	public HumanMirrorObject fightHumanMirrorObj;// 对手镜像数据
	public List<Integer> conditions = null; // 过关条件
	
	
	public TowerParam() {
		
	}
	
	public TowerParam(HumanMirrorObject fightHumanMirrorObj, List<Integer> conditions) {
		this.fightHumanMirrorObj = fightHumanMirrorObj;
		this.conditions = conditions;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(fightHumanMirrorObj);
		out.write(conditions);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		fightHumanMirrorObj = in.read();
		conditions = in.read();
	}
	
}
