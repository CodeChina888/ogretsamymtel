package game.worldsrv.human;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.CallPoint;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.entity.ItemBody;
import game.worldsrv.entity.Unit;
import game.worldsrv.humanSkill.SkillGodsJSON;
import game.worldsrv.humanSkill.SkillJSON;
import game.worldsrv.humanSkill.SkillTrainJSON;

public class HumanGlobalInfo implements ISerilizable {
	public CallPoint connPoint = new CallPoint();// 玩家连接ID
	public String nodeId; 	// Node名称
	public String portId; 	// Port名称
	public String channel;	// 渠道
	public int serverId;	// serverId
	
	public long timeSync;// 同步时间，用于把异常玩家踢下线
	public long timeLogin; // 玩家最后一次登陆时间
	public long timeSchedule; // 玩家最后一次执行调度的时间
	
	public long id; // 玩家ID
	public int digit; // 玩家标识id
	public String account; // 登录账号
	public String accountId; // 登录账号Id
	public String name; // 玩家名称
	public long stageId; // 所在地图ID
	public String stageName; // 所在地图名
	//public int countryId; // 国家
	//public long unionId; // 联盟
	public long teamId; // 队伍
	public int profession; // 职业
	public int sex; // 性别
	public int level; // 等级
	public int vipLv; // VIP等级
	public int combat; // 战斗力
	public int headSn; // 头像sn
	public int modelSn; // 玩家模型
	public int defaultModelSn; // 默认模型
	public int titleSn; // 玩家称号
	public long guildId; // 公会ID
	public int sn; // 角色Sn
	public int mountSn; //坐骑
	public int partnerStance = 1; // 阵容
	public String guildName;// 公会名字
	public int competeRank; // 竞技场排名
	public int instStar; // 副本总星数
	
	public Unit unit;// human的基本信息
	public String lineup; // 上阵阵容
	public int stance; // 队伍站位（W或M）
	public List<PartnerObject> partnerObjList = new ArrayList<PartnerObject>();// 上阵伙伴信息
	
	public List<SkillJSON> skillList = new ArrayList<>();	// 上阵技能
	public List<SkillGodsJSON> skillGodsList = new ArrayList<>();	// 上阵爆点技能
	public int installGods = 0; // 上阵爆点sn
	public List<SkillTrainJSON> skillTrainList = new ArrayList<>(); // 玩家技能神通
	
	public List<ItemBody> equipList = new ArrayList<>();// 身上的装备
	
//	public int magicWeaponSn;// 法宝--查看界面
//	public int magicWeaponLevel;// 法宝等级

//	public List<DItem> fashionbody;// 身上的时装
//	public boolean isFashionShow;// 时装是否显示
//	public int equipWeaponSn;// 在用武器
	

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(connPoint);
		out.write(nodeId);
		out.write(portId);
		out.write(channel);
		out.write(serverId);
		
		out.write(timeSync);
		out.write(timeLogin);
		out.write(timeSchedule);
		
		out.write(id);
		out.write(digit);
		out.write(account);
		out.write(accountId);
		out.write(name);
		out.write(stageId);
		out.write(stageName);
		//out.write(countryId);
		//out.write(unionId);
		out.write(teamId);
		out.write(profession);
		out.write(sex);
		out.write(level);
		out.write(vipLv);
		out.write(combat);
		out.write(headSn);
		out.write(modelSn);
		out.write(defaultModelSn);
		out.write(titleSn);
		out.write(guildId);
		out.write(sn);
		out.write(mountSn);
		out.write(partnerStance);
		out.write(guildName);
		out.write(competeRank);
		out.write(instStar);
		
		out.write(unit);
		out.write(lineup);
		out.write(stance);
		out.write(partnerObjList);
		out.write(skillList);
		out.write(skillGodsList);
		out.write(installGods);
		out.write(skillTrainList);
		out.write(equipList);
		
//		out.write(magicWeaponSn);
//		out.write(magicWeaponLevel);

//		out.write(fashionbody);
//		out.write(isFashionShow);
//		out.write(equipWeaponSn);
		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		connPoint = in.read();
		nodeId = in.read();
		portId = in.read();
		channel = in.read();
		serverId = in.read();
		
		timeSync = in.read();
		timeLogin = in.read();
		timeSchedule = in.read();
		
		id = in.read();
		digit = in.read();
		account = in.read();
		accountId = in.read();
		name = in.read();
		stageId = in.read();
		stageName = in.read();
		//countryId = in.read();
		//unionId = in.read();
		teamId = in.read();
		profession = in.read();
		sex = in.read();
		level = in.read();
		vipLv = in.read();
		combat = in.read();
		headSn = in.read();
		modelSn = in.read();
		defaultModelSn = in.read();
		titleSn = in.read();
		guildId = in.read();
		sn = in.read();
		mountSn = in.read();
		partnerStance = in.read();
		guildName = in.read();
		competeRank = in.read();
		instStar = in.read();
		
		unit = in.read();
		lineup = in.read();
		stance = in.read();
		partnerObjList = in.read();
		skillList = in.read();
		skillGodsList = in.read();
		installGods = in.read();
		skillTrainList = in.read();
		equipList = in.read();
//		magicWeaponSn = in.read();
//		magicWeaponLevel = in.read();
//		itembody = in.read();
//		fashionbody = in.read();
//		isFashionShow = in.read();
//		equipWeaponSn = in.read();
		
	}
	
	/**
	 * 获取上阵爆点
	 * @return
	 */
	public SkillGodsJSON getInstallGodsJSON() {
		for (SkillGodsJSON sgJSON : skillGodsList) {
			if(sgJSON != null && sgJSON.tag == installGods) {
				return sgJSON;
			}
		}
		return null;
	}

}
