package game.worldsrv.raffle;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.raffle.raffleMode.LuckTurntableMod;

/**
 * @author Neak
 * 玩家所有抽奖信息
 */
public class RaffleInfo implements ISerilizable{
	// 抽奖类型：幸运转盘
	public LuckTurntableMod luckTurntable = new LuckTurntableMod();

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(luckTurntable);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		luckTurntable = in.read();
	}
	
	
	
}
