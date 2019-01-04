package game.worldsrv.guild;

import core.support.observer.MsgReceiver;
import game.msg.Define.EGuildImmoType;
import game.msg.MsgGuild.CSApplyClear;
import game.msg.MsgGuild.CSApplyInfo;
import game.msg.MsgGuild.CSApplyReply;
import game.msg.MsgGuild.CSDeclare;
import game.msg.MsgGuild.CSGuildCancleJoin;
import game.msg.MsgGuild.CSGuildCreate;
import game.msg.MsgGuild.CSGuildIcon;
import game.msg.MsgGuild.CSGuildImmo;
import game.msg.MsgGuild.CSGuildImmoGiftBag;
import game.msg.MsgGuild.CSGuildImmoInfo;
import game.msg.MsgGuild.CSGuildImmoLog;
import game.msg.MsgGuild.CSGuildInfo;
import game.msg.MsgGuild.CSGuildJoin;
import game.msg.MsgGuild.CSGuildKickOut;
import game.msg.MsgGuild.CSGuildLeave;
import game.msg.MsgGuild.CSGuildMemberInfo;
import game.msg.MsgGuild.CSGuildPostSet;
import game.msg.MsgGuild.CSGuildRename;
import game.msg.MsgGuild.CSGuildSeek;
import game.msg.MsgGuild.CSGuildSet;
import game.msg.MsgGuild.CSNotice;
import game.msg.MsgGuild.CSGuildSkillList;
import game.msg.MsgGuild.CSGuildSkillUpgrade;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

public class GuildMsgHandler {

	/**
	 * 请求所有公会信息
	 * @param param
	 */
	@MsgReceiver(CSGuildInfo.class)
	public void _msg_CSGuildInfo(MsgParam param) {
		CSGuildInfo msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		
		GuildManager.inst()._msg_CSGuildInfo(humanObj, msg.getPage());
	}

	/**
	 * 创建公会请求
	 * @param param
	 */
	@MsgReceiver(CSGuildCreate.class)
	public void _msg_CSGuildCreate(MsgParam param) {
		CSGuildCreate msg = param.getMsg();
		String guildName = msg.getGuildName();
		String content = msg.getContent();
		
		HumanObject humanObj = param.getHumanObject();
		int icon = msg.getIcon();
		GuildManager.inst()._msg_CSGuildCreate(humanObj, guildName,content,icon);
	}

	/**
	 * 设置公会QQ群和入会要求
	 * @param param
	 */
	@MsgReceiver(CSGuildSet.class)
	public void _msg_CSGuildSet(MsgParam param) {
		CSGuildSet msg = param.getMsg();
		long guildId = msg.getGuildId(); // 公会id
		int isApply = msg.getIsApply(); // 是否需要申请 0 默认可以直接加入， 1 需要申请
		int QQ = msg.getQQ();//QQ群
		int initiationMinLevel = msg.getInitiationMinLevel();//限制入会等级
		HumanObject humanObj = param.getHumanObject();
		if(0 == guildId) {
			return;
		}
		GuildManager.inst()._msg_CSGuildSet(humanObj, guildId, QQ, isApply, initiationMinLevel);
	}
	
	/**
	 * 公会改名
	 * @param param
	 */
	@MsgReceiver(CSGuildRename.class)
	public void _msg_SCGuildRename(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSGuildRename msg = param.getMsg();
		String guildName = msg.getGuildName();
		long guildId = msg.getGuildId();
		GuildManager.inst()._msg_SCGuildRename(humanObj, guildId, guildName);
	}
	
	/**
	 * 设置宣告
	 * @param param
	 */
	@MsgReceiver(CSDeclare.class)
	public void _msg_SCDeclare(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSDeclare msg = param.getMsg();
		long guildId = msg.getGuildId();
		String declare = msg.getGuildDeclare();//公会宣告
		GuildManager.inst()._msg_SCDeclare(humanObj, guildId, declare);
	}
	
	/**
	 * 设置内部宣告
	 * @param param
	 */
	@MsgReceiver(CSNotice.class)
	public void _msg_SCNotice(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSNotice msg = param.getMsg();
		long guildId = msg.getGuildId();
		String notice = msg.getGuildNotice();//公会内部宣告
		GuildManager.inst()._msg_SCNotice(humanObj, guildId, notice);
	}
	
	/**
	 * 设置公会图标
	 * @param param
	 */
	@MsgReceiver(CSGuildIcon.class)
	public void _msg_SCGuildIcon(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSGuildIcon msg = param.getMsg();
		int icon = msg.getIcon();
		long guildId = msg.getGuildId();
		GuildManager.inst()._msg_SCGuildIcon(humanObj, guildId, icon);
	}

	/**
	 * 查看公会会员信息
	 * @param param
	 */
	@MsgReceiver(CSGuildMemberInfo.class)
	public void _msg_CSGuildMemberInfoInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSGuildMemberInfo(humanObj);
	}

	/**
	 * 入会请求
	 * @param param
	 */
	@MsgReceiver(CSGuildJoin.class)
	public void _msg_CSGuildJoin(MsgParam param) {
		CSGuildJoin msg = param.getMsg();
		int type = msg.getAddGuild();// 0 快速加入，1 申请加入， 2 加入
		long guildId = msg.getGuildId();// 公会id
		HumanObject humanObj = param.getHumanObject();
		
		GuildManager.inst()._msg_CSGuildJoin(humanObj, type, guildId);
	}

	/**
	 * 查询搜索相关名字的公会请求
	 * @param param
	 */
	@MsgReceiver(CSGuildSeek.class)
	public void _msg_CSGuildSeek(MsgParam param) {
		CSGuildSeek msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		String guildName = msg.getGuildName();
		GuildManager.inst()._msg_CSGuildSeek(humanObj, guildName);
	}

	/**
	 * 主动离开公会请求
	 * @param param
	 */
	@MsgReceiver(CSGuildLeave.class)
	public void _msg_CSGuildLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSGuildLeave(humanObj);
	}

	/**
	 * 会长踢出会员
	 * @param param
	 */
	@MsgReceiver(CSGuildKickOut.class)
	public void _msg_CSGuildKickOut(MsgParam param) {
		CSGuildKickOut msg = param.getMsg();
		long humanId = msg.getId();
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSGuildKickOut(humanObj, humanId);
	}

	/**
	 * 查看所有申请人信息
	 * @param param
	 */
	@MsgReceiver(CSApplyInfo.class)
	public void _msg_CSApplyInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSApplyInfo(humanObj);
	}

	/**
	 * 对申请入会玩家处理
	 * @param param
	 */
	@MsgReceiver(CSApplyReply.class)
	public void _msg_CSApplyReply(MsgParam param) {
		CSApplyReply msg = param.getMsg();
		int reply = msg.getReply();
		long humanId = msg.getHumanId();
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSApplyReply(humanObj, humanId, reply);
	}

	/**
	 * 清空申请列表
	 * @param param
	 */
	@MsgReceiver(CSApplyClear.class)
	public void _msg_CSApplyClear(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSApplyClear(humanObj);
	}

	/**
	 * 公会会员职位设置
	 * @param param
	 */
	@MsgReceiver(CSGuildPostSet.class)
	public void _msg_CSGuildPostSet(MsgParam param) {
		CSGuildPostSet msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		long humanId = msg.getHumanId();
		int post = msg.getPost();
		GuildManager.inst()._msg_CSGuildPostSet(humanObj, humanId, post);
	}

	/**
	 * 公会献祭
	 * @param param
	 */
	@MsgReceiver(CSGuildImmo.class)
	public void _msg_CSGuildImmo(MsgParam param) {
		CSGuildImmo msg = param.getMsg();
		EGuildImmoType type = msg.getType();
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSGuildImmo(humanObj, type.getNumber());
	}
	
	/**
	 * 
	 */
	@MsgReceiver(CSGuildCancleJoin.class)
	public void _msg_CSGuildCancleJoin(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGuildCancleJoin msg = param.getMsg();
		long guildId = 	msg.getGuildId();
		GuildManager.inst().cancleJoin(humanObj, guildId);
	}
	/**
	 * 查看日志
	 * @param param
	 */
	@MsgReceiver(CSGuildImmoLog.class)
	public void _msg_CSGuildImmoLog(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSGuildImmoLog(humanObj);
	}
	
	
	@MsgReceiver(CSGuildImmoInfo.class)
	public void onCSGuildImmoInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst().isBuild(humanObj);
	}
	
	/**
	 * 公会宝箱领取
	 * @param param
	 */
	@MsgReceiver(CSGuildImmoGiftBag.class)
	public void _msg_CSGuildImmoGiftBag(MsgParam param){
		CSGuildImmoGiftBag msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int sn = msg.getSn();
		GuildManager.inst()._msg_CSGuildImmoGiftBag(humanObj, sn);
	}


	@MsgReceiver(CSGuildSkillList.class)
	public void _msg_CSGuildSkillList(MsgParam param){
		CSGuildSkillList msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		GuildManager.inst()._msg_CSGuildSkillList(humanObj);
	}

	@MsgReceiver(CSGuildSkillUpgrade.class)
	public void _msg_CSGuildSkillUpgrade(MsgParam param){
		CSGuildSkillUpgrade msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int sn = msg.getSn();
		GuildManager.inst()._msg_CSGuildSkillUpgrade(humanObj, sn);
	}
}
