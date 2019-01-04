package game.worldsrv.support;

import game.worldsrv.enumType.FightPropName;
import game.worldsrv.support.PropCalcBase;

/**
 * 属性计算，只供PropKey中枚举的字段使用
 */
public class PropCalc extends PropCalcBase<FightPropName> {
	public PropCalc() {

	}

	public PropCalc(String json) {
		super(json);
	}
	
	public PropCalc multiply(PropCalc base, String[] propKey, int[] propValue) {
		PropCalc prop = new PropCalc();
		if (null != propKey && null != propValue && propKey.length == propValue.length) {
			for (int i = 0; i < propKey.length; i++) {
				FightPropName key = toKey(propKey[i]);
				Double val = datas.get(key);
				if (val != null) {
					prop.add(key, val*propValue[i]/Utils.D10000);
				}
			}
		}
		return prop;
	}
	
	@Override
	protected FightPropName toKey(String key) {
		return FightPropName.get(key);
	}
	
}
