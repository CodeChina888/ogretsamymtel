package game.worldsrv.team;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.entity.Human;

/**
 * 队员
 */
public class TeamMember implements ISerilizable {
	public long id = 0; // 玩家Id
	public String name = ""; // 玩家名
	public int level = 0; // 等级
	public int combat = 0; // 战斗力
	public int profession = 0; // 职业
	public int sex = 0; // 性别
	public int modelSn = 0; // 模型SN
	public boolean isOnline = true; // 是否在线：true在线，false离线

	public TeamMember() {
	}

	public TeamMember(Human human) {
		this.id = human.getId();
		this.name = human.getName();
		this.level = human.getLevel();
		this.combat = human.getCombat();
		this.profession = human.getProfession();
		this.sex = human.getSex();
		this.modelSn = human.getDefaultModelSn();
		this.isOnline = true;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", id).append("name", name)
				.append("level", level).append("combat", combat).append("profession", profession).append("sex", sex)
				.append("modelSn", modelSn).append("isOnline", isOnline).toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(name);
		out.write(level);
		out.write(combat);
		out.write(profession);
		out.write(sex);
		out.write(modelSn);
		out.write(isOnline);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		name = in.read();
		level = in.read();
		combat = in.read();
		profession = in.read();
		sex = in.read();
		modelSn = in.read();
		isOnline = in.read();
	}

}
