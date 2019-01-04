package game.worldsrv.raffle.raffleMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.EModeType;

/**
 * @author Neak
 * 幸运转盘玩法
 */
public class LuckTurntableMod implements ISerilizable{
	// 幸运转盘次数
	public int count = 0;
	// 幸运转盘当前的权重数组
	public List<Integer> weights = new ArrayList<>();
	// 幸运转盘玩法
	public EModeType modeType = EModeType.ModeNone;
	// 转盘级别
	public int lv = 0;
	// 幸运转盘sn
	public int sn = 0;
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(count);
		out.write(weights);
		out.write(modeType);
		out.write(lv);
		out.write(sn);
	}
	@Override
	public void readFrom(InputStream in) throws IOException {
		weights.clear();
		
		count = in.read();
		weights = in.read();
		modeType = in.read();
		lv = in.read();
		sn = in.read();
	}
	
	public void reset() {
		count = 0;
		weights.clear();
		modeType = EModeType.ModeNone;
		lv = 0;
		sn = 0;
	}
}
