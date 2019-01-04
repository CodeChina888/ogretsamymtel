package game.worldsrv.fightParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class GuildParam implements ISerilizable {
	public int stageSn;
	public int chapterSn;
	public List<Integer> hpMax = new ArrayList<>();// 最大血量
	public List<Integer> hpCur = new ArrayList<>();// 当前血量
	
	
	public GuildParam() {
		
	}
	
	public GuildParam(int lv, int times, List<Integer> hpMax, List<Integer> hpCur) {
		this.stageSn = lv;
		this.chapterSn = times;
		this.hpMax = hpMax;
		this.hpCur = hpCur;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(stageSn);
		out.write(chapterSn);
		out.write(hpMax);
		out.write(hpCur);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		stageSn = in.read();
		chapterSn = in.read();
		hpMax = in.read();
		hpCur = in.read();
	}
	
}
