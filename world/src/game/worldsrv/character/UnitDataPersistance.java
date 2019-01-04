package game.worldsrv.character;

import game.worldsrv.entity.Buff;
import game.worldsrv.entity.Unit;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;

public class UnitDataPersistance implements ISerilizable {

	public Unit unit; // 基本信息
	@Deprecated
	public UnitPropPlusMap unitPropPlus = new UnitPropPlusMap(); // 属性加成
	
	// 玩家身上的buff集合，key=Buff.Type
	public final Map<Integer, Buff> buffs = new ConcurrentHashMap<>();

	/**
	 * 构造函数
	 */
	public UnitDataPersistance() {
		
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(unit);
		out.write(unitPropPlus);
		out.write(buffs);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		unit = in.read();
		unitPropPlus = in.read();
		buffs.clear();
		buffs.putAll(in.<Map<Integer, Buff>> read());
	}
}
