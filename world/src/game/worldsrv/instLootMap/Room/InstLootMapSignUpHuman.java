package game.worldsrv.instLootMap.Room;

import java.io.IOException;

import core.InputStream;
import core.OutputStream;
import core.gen.proxy.DistrClass;
import core.interfaces.ISerilizable;
import game.msg.Define.DMemberInfo;
import game.msg.MsgInstLootMap.SCLootMapIntoSignUpRoom;
import game.worldsrv.entity.Human;

public class InstLootMapSignUpHuman  implements ISerilizable{
	
	public long humanId;
	private String name;
	private int combat;
	private int profession;
	private int sex;
	private int modelSn;
	
	
	public InstLootMapSignUpHuman() {
		//super();
	}

	public InstLootMapSignUpHuman(Human human){
		humanId = human.getId();
		name = human.getName();
		combat = human.getCombat();
		profession = human.getProfession();
		sex = human.getSex();
		modelSn = human.getDefaultModelSn();
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(humanId);
		out.write(name);
		out.write(combat);
		out.write(profession);
		out.write(sex);
		out.write(modelSn);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		humanId = in.read();
		name = in.read();
		combat = in.read();
		profession = in.read();
		sex = in.read();
		modelSn = in.read();
	}
	
	public DMemberInfo.Builder getDMembetInfo(){
		DMemberInfo.Builder dMemberInfo = DMemberInfo.newBuilder();
		dMemberInfo.setId(humanId);
		dMemberInfo.setName(name);
		dMemberInfo.setCombat(combat);
		dMemberInfo.setProfession(profession);
		dMemberInfo.setSex(sex);
		dMemberInfo.setModelSn(modelSn);
		dMemberInfo.setIsOnline(true);
		return dMemberInfo;
	}
	
	public SCLootMapIntoSignUpRoom.Builder getSignUpRoomBuilder(){
		SCLootMapIntoSignUpRoom.Builder msg = SCLootMapIntoSignUpRoom.newBuilder();
		msg.setHuman(getDMembetInfo());
		return msg;
	}
	
}
