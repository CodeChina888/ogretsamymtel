package game.worldsrv.fightrecord;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class RecordInfo implements ISerilizable {
	public long id;// id
	public String leftName;// 左边战斗者名
	public int leftSn;// 左边战斗者sn
	public int leftCombat;// 左边战斗者战力
	public int leftAptitude;// 左边战斗者品质
	public String rightName;// 右边战斗者名
	public int rightSn;// 右边战斗者sn
	public int rightCombat;// 右边战斗者战力
	public int rightAptitude;// 右边战斗者品质
	
	public RecordInfo() {		
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(leftName);
		out.write(leftSn);
		out.write(leftCombat);
		out.write(leftAptitude);
		out.write(rightName);
		out.write(rightSn);
		out.write(rightCombat);
		out.write(rightAptitude);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		leftName = in.read();
		leftSn = in.read();
		leftCombat = in.read();
		leftAptitude = in.read();
		rightName = in.read();
		rightSn = in.read();
		rightCombat = in.read();
		rightAptitude = in.read();		
	}
}
