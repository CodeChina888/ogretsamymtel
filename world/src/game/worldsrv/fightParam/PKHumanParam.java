package game.worldsrv.fightParam;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.pk.PKHumanInfo;

public class PKHumanParam implements ISerilizable{
	public PKHumanInfo team1Human = new PKHumanInfo();
	public PKHumanInfo team2Human = new PKHumanInfo();
	
	public PKHumanParam() {
		
	}
	
	public PKHumanParam(PKHumanInfo team1Human, PKHumanInfo team2Human) {
		this.team1Human = team1Human;
		this.team2Human = team2Human;
	}
	

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(team1Human);
		out.write(team2Human);		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		team1Human = in.read();
		team2Human = in.read();
	}
}
