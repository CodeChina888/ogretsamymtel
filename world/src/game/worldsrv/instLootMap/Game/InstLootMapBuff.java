package game.worldsrv.instLootMap.Game;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.ELootMapBuffLimitType;
import game.worldsrv.config.ConfBuffVk;

public class InstLootMapBuff implements ISerilizable {
	
	public int sn;
	public int count;
	
	public InstLootMapBuff(){
	}
	
	public InstLootMapBuff(int sn,int count){
		this.sn = sn;
		this.count = count;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(count);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		count = in.read();
	}
	
	/**
	 * 获取攻击
	 * @param baseAttack
	 * @param sn
	 * @return
	 */
	public static float getAddAttack(int baseAttack,int sn){
		if(isAttack(sn) == false && isMonsterAttack(sn) == false) return 0;
		
		ConfBuffVk conf = ConfBuffVk.get(sn);
		if(conf == null) return 0;
		int scale = isAttackBuff(sn) || isMonsterAttackBuff(sn) ? 1:-1;
		return getBuffValue(baseAttack,conf.power,conf.fixedValue,scale);
	}
	
	/**
	 * 获取回血
	 * @param baseHp
	 * @param sn
	 * @return
	 */
	public static float getAddHp(int baseHp,int sn){
		if(isHp(sn) == false) return 0;
		ConfBuffVk conf = ConfBuffVk.get(sn);
		if(conf == null) return 0;
		int scale = isDefenseBuff(sn) ? 1:-1;
		return getBuffValue(baseHp,conf.power,conf.fixedValue,scale);
	}
	
	/**
	 * 是否是攻击类型buff
	 * @param sn
	 * @return
	 */
	static public boolean isAttack(int sn){
		return isAttackBuff(sn) || isAttackDebuff(sn);
	}
	
	/**
	 * 是否是回血类型buff
	 * @param sn
	 * @return
	 */
	static public boolean isHp(int sn){
		return isDefenseBuff(sn) || isDefenseDebuff(sn);
	}
	
	/**
	 * 是否是攻击buff
	 * @param sn
	 * @return
	 */
	static public boolean isAttackBuff(int sn) {
        return
            (sn >= ELootMapBuffLimitType.LootMapBufAddAttackMin_VALUE && sn <= ELootMapBuffLimitType.LootMapBufAddAttackMax_VALUE);
    }

	/**
	 * 是否是攻击debuff
	 * @param sn
	 * @return
	 */
    static public boolean isAttackDebuff(int sn) {
        return
            (sn >= ELootMapBuffLimitType.LootMapBufSubAttackMin_VALUE && sn <= ELootMapBuffLimitType.LootMapBufSubAttackMax_VALUE);
    }

    /**
     * 是否是回血buff
     * @param sn
     * @return
     */
    static public boolean isDefenseBuff(int sn) {
        return
            (sn >= ELootMapBuffLimitType.LootMapBufAddHpMin_VALUE && sn <= (int)ELootMapBuffLimitType.LootMapBufAddHpMax_VALUE);
    }

    /**
     * 是否是扣血buff
     * @param sn
     * @return
     */
    static public boolean isDefenseDebuff(int sn) {
        return
            (sn >= (int)ELootMapBuffLimitType.LootMapBufSubHpMin_VALUE && sn <= (int)ELootMapBuffLimitType.LootMapBufSubHpMax_VALUE);
    }
    
    static public boolean isMonsterAttack(int sn){
    	return isMonsterAttackBuff(sn) || isMonsterAttackDebuff(sn);
    }
    
    
    static public boolean isMonsterAttackBuff(int sn){
    	 return
    			 (sn >= (int)ELootMapBuffLimitType.LootMapBufMonsterAddAttackMin_VALUE && sn <= (int)ELootMapBuffLimitType.LootMapBufMonsterAddAttackMax_VALUE);
    }
    
    static public boolean isMonsterAttackDebuff(int sn){
    	return
   			 (sn >= (int)ELootMapBuffLimitType.LootMapBufMonsterSubAttackMin_VALUE && sn <= (int)ELootMapBuffLimitType.LootMapBufMonsterSubAttackMax_VALUE);
    }

    /**
     * 获取增加buff攻击
     * @param baseValue
     * @param power
     * @param fixedValue
     * @param scale
     * @return
     */
    static public float getBuffValue(int baseValue, int power, int fixedValue, int scale) {
        return (baseValue * power / 1000f + fixedValue) * scale;
    }
}
