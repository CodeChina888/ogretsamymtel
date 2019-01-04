package game.worldsrv.support;

import java.io.IOException;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.enumType.FightPropName;
import game.worldsrv.enumType.PropExtType;

/**
 * 通用属性累加，适用于PropType,PropExtType
 */
public class PropCalcCommon extends PropCalcBase<String> implements ISerilizable {
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(datas);		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		datas.clear();
		datas.putAll(in.<Map<String, Double>> read());
	}
	
	
	public PropCalcCommon() {
	}

	public PropCalcCommon(String json) {
		super(json);
	}
	
	public PropCalcCommon multiply(String[] propKey, int[] propValue) {
		PropCalcCommon prop = new PropCalcCommon();
		if (null != propKey && null != propValue && propKey.length == propValue.length) {
			for (int i = 0; i < propKey.length; i++) {
				Double val = datas.get(propKey[i]);
				if (val != null) {
					prop.add(propKey[i], val*propValue[i]/Utils.D10000);
				}
			}
		}
		return prop;
	}

	@Override
	protected String toKey(String key) {
		return key;
	}

	public float getFloat(FightPropName type) {
		return this.getFloat(type.name());
	}
	
	public int getInt(FightPropName type) {
		return this.getInt(type.name());
	}

	public int getInt(PropExtType type) {
		return this.getInt(type.name());
	}

}
