package game.worldsrv.instLootMap.Game.Event;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import game.msg.Define.ELootMapType;
import game.worldsrv.config.ConfLootMapEvent;
import game.worldsrv.config.ConfLootMapLevelObject;
import game.worldsrv.instLootMap.Game.InstLootMapSkill;

public class InstLootMapEventSkill extends InstLootMapEventLevel{
	
	private InstLootMapSkill skill;
	
	public InstLootMapSkill getSkill(){
		return skill;
	}

	public InstLootMapEventSkill(int eventSn,int mapLevel,int humanLevel,ELootMapType mapType){
		super(eventSn, mapLevel, humanLevel,mapType);
	}
	
	@Override
	protected void onInit(ConfLootMapEvent conf){
		if(conf.param1.length < 2){
			return;
		}
		skill = new InstLootMapSkill();
		skill.eventSn = conf.sn;
	}
	
	@Override
	protected void onInit(ConfLootMapLevelObject conf){
		super.onInit(conf);
		skill.levelObjectSn = conf.sn;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		out.write(skill);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		skill = in.read();
	}
}
