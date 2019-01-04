package game.worldsrv.instLootMap.Game.Event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import core.InputStream;
import core.OutputStream;
import game.msg.Define.DLootMapEvent;
import game.msg.Define.ELootMapType;
import game.worldsrv.config.ConfLootMapLevelObject;
import game.worldsrv.instLootMap.Game.InstLootMapBuff;

/**
 * 怪物相关继承这个
 *
 */
public class InstLootMapEventMonster extends InstLootMapEventBuff {
	
	//拥有的buff -> buffid number
	private Map<Integer,Integer> buffMap = new HashMap<>();
	
	int attack; // 攻击力
	int hp; // 生命
	int baseHp; // 最初生命-
	int buffAttack;
	
	public InstLootMapEventMonster(int eventSn, int level, int humanLevel,ELootMapType mapType) {
		super(eventSn, level, humanLevel,mapType);
	}
	
	@Override
	protected void onInit(ConfLootMapLevelObject conf){
		super.onInit(conf);
		this.attack = conf.attack;
		this.hp = conf.hp;
		baseHp = hp;
		buffAttack = attack;
	}
	
	public int getAttack(){
		return buffAttack;
	}
	
	public int getHP(){
		return hp;
	}
	
	public void setHp(int hp){
		if(hp < 0){
			hp = 0;
		}
		this.hp = hp;
	}
	
	@Override
	public void refersh(){
		//TODO 当怪物刷新的时候 需要把buff加进来
		super.refersh();
		setHp(baseHp);
		buffMap.clear();
	}
	
	/**
	 * 添加buff
	 * @param buff
	 * @param num
	 */
	public void addBuff(int buff,int num){
		if(InstLootMapBuff.isMonsterAttack(buff)){
			if(buffMap.containsKey(buff)){
				num += buffMap.get(buff);
			}
			buffMap.put(buff, num);
			initAttack();
		}
	}
	
	private void initAttack(){
		buffAttack = 0;
		int addAttack = 0;
		for(int key : buffMap.keySet()){
			int num = buffMap.get(key);
			addAttack += ((int)InstLootMapBuff.getAddAttack(attack,key)*num);
		}
		buffAttack =  attack + addAttack;
	}
	
	
	/**
	 * 移除buff
	 * @param buff
	 * @param num
	 */
	public void rmvBuff(int buff,int num){
		if(buffMap.containsKey(buff)== false) return;
		int value = buffMap.get(buff);
		value -= num;
		if(value <= 0){
			buffMap.remove(buff);
		}else{
			buffMap.put(buff, value);
		}
		initAttack();
	}
	
	public void attackHuman(int humanAttack){
		hp -= humanAttack;
		hp = hp < 0 ? 0:hp;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		super.writeTo(out);
		out.write(attack);
		out.write(hp);
		out.write(baseHp);
		out.write(buffMap);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		super.readFrom(in);
		attack = in.read();
		hp = in.read();
		baseHp = in.read();
		buffMap = in.read();
	}
	
	@Override
	protected void initDLootMapEvent(DLootMapEvent.Builder eventMsg){
		super.initDLootMapEvent(eventMsg);
		eventMsg.setHp(hp);
		eventMsg.setAttack(getAttack());
	}
}
