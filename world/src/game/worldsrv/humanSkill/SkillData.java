package game.worldsrv.humanSkill;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

/**
 * 技能数据：包括技能sn和lv
 */
public class SkillData implements ISerilizable{
	public int sn;		// 技能sn
	public int lv;		// 技能lv
	
	public SkillData() {
		
	}
	
	public SkillData(int sn, int lv) {
		this.sn = sn;
		this.lv = lv;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(lv);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		lv = in.read();
	}
	
}
