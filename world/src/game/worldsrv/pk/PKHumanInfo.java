package game.worldsrv.pk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

/**
 * pkHuman信息
 * @author Neak
 *
 */
public class PKHumanInfo implements ISerilizable{
	public long humanId = 0l;
	public String[] propName = null;
	public double[] multiply = null;
	// 上阵携带buffList
	public List<Integer> buffs = new ArrayList<>();
	
	public PKHumanInfo() {
		
	}
	
	public PKHumanInfo(long humanId) {
		this.humanId = humanId;
	}
	
	public PKHumanInfo(long humanId, String[] propName, double[] multiply, List<Integer> buffs) {
		this.humanId = humanId;
		this.propName = propName;
		this.multiply = multiply;
		this.buffs = buffs;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(humanId);
		out.write(propName);		
		out.write(multiply);
		out.write(buffs);
	}
	@Override
	public void readFrom(InputStream in) throws IOException {
		humanId = in.read();
		propName = in.read();
		multiply = in.read();
		buffs = in.read();
	}
}
