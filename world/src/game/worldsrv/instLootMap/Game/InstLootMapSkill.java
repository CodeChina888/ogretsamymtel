package game.worldsrv.instLootMap.Game;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.msg.Define.DLootMapSkill;
import game.worldsrv.config.ConfLootMapEvent;
import game.worldsrv.config.ConfLootMapLevelObject;

public class InstLootMapSkill implements ISerilizable {

	//该类之所以存在 是因为需要记录的信息较多
	//eventSn -> 攻击范围 客户端需求图标显示
	//levelObjSn -> 具体伤害 客户端需求模型
	
	public int eventSn;
	public int levelObjectSn;
	
	/**
	 * 获取攻击范围x
	 * @return
	 */
	public int getWidth(){
		ConfLootMapEvent conf = ConfLootMapEvent.get(eventSn);
		if(conf == null) return 0;
		return conf.param1[0];
	}
	
	/**
	 * 获取攻击范围y
	 * @return
	 */
	public int getHeight(){
		ConfLootMapEvent conf = ConfLootMapEvent.get(eventSn);
		if(conf == null) return 0;
		return conf.param1[1];
	}
	
	/**
	 * 获取攻击力
	 * @return
	 */
	public int getAttack(){
		ConfLootMapLevelObject conf = ConfLootMapLevelObject.get(levelObjectSn);
		if(conf == null) return 0;
		return conf.attack;
	}
	
	public DLootMapSkill.Builder getDLootMapSkill(){
		DLootMapSkill.Builder dSkill = DLootMapSkill.newBuilder();
		dSkill.setEventSn(eventSn);
		dSkill.setLevelObjectSn(levelObjectSn);
		return dSkill;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(eventSn);
		out.write(levelObjectSn);
	}
	@Override
	public void readFrom(InputStream in) throws IOException {
		eventSn = in.read();
		levelObjectSn = in.read();
	}

}
