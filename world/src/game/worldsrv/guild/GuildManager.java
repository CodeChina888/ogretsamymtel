package game.worldsrv.guild;

import game.msg.Define.DGuildImmo;
import game.msg.Define.DGuildInfo;
import game.msg.Define.DGuildSkill;
import game.msg.Define.ECostGoldType;
import game.msg.Define.EGuildImmoType;
import game.msg.Define.ELogHandleType;
import game.msg.Define.ELogType;
import game.msg.Define.EMailType;
import game.msg.Define.EMoneyType;
import game.msg.MsgGuild.SCApplyInfoResult;
import game.msg.MsgGuild.SCApplyReplyResult;
import game.msg.MsgGuild.SCGuildCancleJoinResult;
import game.msg.MsgGuild.SCGuildCreateResult;
import game.msg.MsgGuild.SCGuildImmoGiftBag;
import game.msg.MsgGuild.SCGuildImmoInfoResult;
import game.msg.MsgGuild.SCGuildImmoLog;
import game.msg.MsgGuild.SCGuildImmoResult;
import game.msg.MsgGuild.SCGuildInfoResult;
import game.msg.MsgGuild.SCGuildJoinResult;
import game.msg.MsgGuild.SCGuildKickOut;
import game.msg.MsgGuild.SCGuildLeaveResult;
import game.msg.MsgGuild.SCGuildLvExp;
import game.msg.MsgGuild.SCGuildMemberResult;
import game.msg.MsgGuild.SCGuildPostSetResult;
import game.msg.MsgGuild.SCGuildSeekResult;
import game.msg.MsgGuild.SCGuildSet;
import game.msg.MsgGuild.SCGuildSkillList;
import game.msg.MsgGuild.SCGuildSkillUpgrade;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfGuildIcon;
import game.worldsrv.config.ConfGuildLevel;
import game.worldsrv.config.ConfGuildPray;
import game.worldsrv.config.ConfGuildSkill;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Guild;
import game.worldsrv.entity.GuildImmoLog;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.enumType.BacklogType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.MailType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalManager;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.AssetsTxtFix;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.SensitiveWordFilter;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

public class GuildManager extends ManagerBase {
	
	public final String GuildIdKey = "guildId";//公会id
	public final String GuildNameKey = "guildName";//公会id
	
	/**
	 * 获取实例
	 * @return
	 */
	public static GuildManager inst() {
		return inst(GuildManager.class);
	}
	
	/**
	 * 登录的时候判断是否有加入公会
	 */
	public void checkHumanLoginGuild(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		long guildBelong = human.getGuildId();
		if (guildBelong > 0) {// 大于0说明有公会
			GuildServiceProxy pxy = GuildServiceProxy.newInstance();
			pxy.getGuildInfo(guildBelong, human);
			pxy.listenResult(this::_result_checkHumanLoginGuild, HumanObject.paramKey, humanObj, "guildBelong", guildBelong);
		} else {//避免退回后公会相关的数据没清空
//			GuildServiceProxy pxy = GuildServiceProxy.newInstance();
//			pxy.mapGuildCDR(human);
//			pxy.listenResult(this::_result_mapGuildCDR, HumanObject.paramKey, humanObj);
		}		
	}
	
	
	
	/**
	 * 判断工会副本是否有红包未领取
	 * @param humanObj
	 */
	public void sendHasRedDot(HumanObject humanObj){
		long guildBelong = humanObj.getHuman().getGuildId();
		if (guildBelong > 0) {// 大于0说明有公会			
//			SCDot.Builder msg = SCDot.newBuilder();
//			msg.setSn(35);//根据NoticeFlag表格发sn
//			humanObj.sendMsg(msg);
		}
	}
	
	public void _result_mapGuildCDR(Param results, Param context){
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Human human = humanObj.getHuman();
		boolean result = Utils.getParamValue(results, "result", false);
		if(result){
			long guildId = Utils.getParamValue(results, "guildId", 0L);
			String guildName = Utils.getParamValue(results, "guildName", "");
			human.setGuildId(guildId);//所属公会
			human.setGuildName(guildName);
		}else{
			human.setGuildId(0);//所属公会
			human.setGuildLevel(0);//公会等级
//			human.setContribute(0);//对此公会贡献
//			human.setGuildFightTimes(0);//攻打次数清零
//			human.setGuildBuyTimes(0);//购买次数清零
		}
		
	}
	public void _result_checkHumanLoginGuild(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(results, "guild", null);
		long guildBelong = Utils.getParamValue(context, "guildBelong", 0L);
		Human human = humanObj.getHuman();
		if(guild == null){
			Log.guild.error("guild=null guildId={}", guildBelong);
			human.setGuildId(0);//所属公会
			human.setGuildName("");
			human.setGuildLevel(0);//公会等级
			return;
		}
		boolean isYetJoin = isYetJoin(guild,human.getId());//判断是否已经加入该公会
		if(!isYetJoin){
			human.setGuildId(0);//所属公会
			human.setGuildLevel(0);//公会等级
			return;
		}
		human.setGuildLevel(guild.getGuildLevel());//重置等级
		updateGuild(human, guildBelong);//登录更新状态
	}
	
	/**
	 * 下发登录数据
	 */
	@Listener(EventKey.HumanLogin)
	public void loginOperate(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("_listener_HumanLogin humanObj is null");
			return;
		}
		if(humanObj.isDailyFirstLogin){
			resetDaily(humanObj);//每日重置副本记录信息
		}
		checkBacklog(humanObj);
		checkHumanLoginGuild(humanObj);
	}
	
	/**
	 * 检查是否需要处理待办事项
	 */
	private void checkBacklog(HumanObject humanObj) {
//		HumanExt2 humanExt2 = humanObj.getHumanExt2();
//		long dailyResetTime = Utils.getTimeBeginOfToday(Port.getTime()) + ParamManager.dailyHourReset * Time.HOUR;
//		if(humanObj.backlogMap.containsKey(BacklogType.GuildAddFightNum.name())){// 
//			List<Backlog> backlogList = humanObj.backlogMap.get(BacklogType.GuildAddFightNum.name());
//			for (Backlog backlog : backlogList) {
//				JSONObject jo = Utils.toJSONObject(backlog.getParamJSON());
//				int addNum = jo.getIntValue(HumanExt2.K.GuildFightNum);
//				long time = Utils.longValue(jo.getString(ParamManager.timeKey));
//				if(dailyResetTime < time){
//					humanExt2.setGuildFightNum(humanExt2.getGuildFightNum()+addNum);
//				}
//				backlog.remove();//删除数据库中待办事项
//			}
//			humanObj.backlogMap.remove(BacklogType.GuildAddFightNum.name());//移除内存中待办事项
//		}
	}
	
	/**
	 * 每日重置
	 * @param param
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDaily(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, HumanObject.paramKey,null);
		if (humanObj == null) {
			Log.logGameObjectNull();
			return;
		}
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			resetDaily(humanObj);
		}
	}
	
	public void resetDaily(HumanObject humanObj){
		Human human = humanObj.getHuman();
	}
	
	/**
	 * 用户升级 更新有加入公会的会员信息
	 * @param param
	 */
	@Listener(EventKey.HumanLvUp)
	public void _listener_HumanLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, HumanObject.paramKey, null);
		if (humanObj == null) {
			Log.logGameObjectNull();
			return;
		}
		Human human = humanObj.getHuman();
		long guildId = human.getGuildId();
		if (guildId > 0) {
			updateGuild(human, guildId);
		}
	}
	
	/**
	 * 更新最大战力改变
	 * @param param
	 */
	@Listener(EventKey.HumanTotalCombatChange)
	public void _listener_HumanCombatChange(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, HumanObject.paramKey, null);
		if (humanObj == null) {
			Log.logGameObjectNull();
			return;
		}

		Human human = humanObj.getHuman();
		long guildId = human.getGuildId();
		if (guildId > 0) {
			updateGuild(human, guildId);
		}
	}
	
	/**
	 * 判断名字是否存在非法的特殊字符
	 * 判断名字是否有屏蔽字
	 * @param humanObj 可能为空
	 * @param guildName 公会名字
	 * @return res 是否发送消息
	 */
	public boolean canUseName(HumanObject humanObj, String guildName, boolean sendMsg) {
		if (StringUtils.isEmpty(guildName)) {
			if (sendMsg) {
				humanObj.sendSysMsg(50);// 名字不能为空
			}
			return false;
		}
		int guildNameLength = ParamManager.guildNameLength;// 名字限制长度
		// 检查是否存在非法的特殊字符
		if (!AssetsTxtFix.checkContent(guildName, guildNameLength)) {
			humanObj.sendSysMsg(22);// 输入的文本中存在非法字符！请重新输入！
			return false;
		}
		
		// 检查名字是否有屏蔽字，如果有屏蔽字返回
		String fix = SensitiveWordFilter.getInstance().getSensitiveWord(guildName);
		if (fix != null) {
			if (sendMsg) {
				humanObj.sendSysMsg(143);// 名字不合法，请检查敏感字
			}
			return false;
		}
		return true;
	}
	
	/**
	 * 判断是否能创建公会
	 * @param humanObj
	 */
	private boolean isCanCreateGuild(HumanObject humanObj) {
		boolean ret = false;
		Human human = humanObj.getHuman();
		// 创建公会需要消耗的钻石
		
		// 获取最低创建低级需求 等级不足就不能创建
		if (human.getLevel() < ParamManager.createGuildLevel) {
			// 等级不足！
			humanObj.sendSysMsg(34);
			return ret;
		}
		// 所属公会id
		long guildId = human.getGuildId();
		if (guildId != 0) {// 当前有公会不能创建新公会
			return ret;
		}
		
		
		return true;
	}
	
	/**
	 * 请求创建公会
	 * @param humanObj
	 * @param guildName
	 */
	public void _msg_CSGuildCreate(HumanObject humanObj, String guildName,String content,int icon) {
//		if (!ModunlockManager.inst().isUnlock(ModType.Guild.value(), humanObj.getHuman())) {
//			humanObj.sendSysMsg(10009);// 功能未开启
//			return;
//		}
		// 检查名字是否有屏蔽字，如果有屏蔽字返回
		String fix = SensitiveWordFilter.getInstance().getSensitiveWord(content);
		if (fix != null) {
			humanObj.sendSysMsg(143);// 名字不合法，请检查敏感字
			return ;
		}
		// 判断是否能满足创建公会的条件
		boolean isCanCreate = isCanCreateGuild(humanObj);
		// 判断名字是否合法
		boolean isCanUse = canUseName(humanObj, guildName, true);
		
		if (!isCanUse || !isCanCreate) {
			// 返回创建公会失败结果
			sendSCGuildCreateResultMsg(humanObj);
			return;
		}
		// 判断公会名字是否重复
		GuildServiceProxy prx = GuildServiceProxy.newInstance();
		prx.isRepeatGuildName(guildName);
		prx.listenResult(this::_result_msg_CSGuildCreate, HumanObject.paramKey, humanObj, "guildName", guildName,"content",content,"icon",icon);
	}
	
	/**
	 * 返回公会名字是否重复的结果
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildCreate(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		String content = context.get("content");
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		int icon = context.get("icon");
		String guildName = Utils.getParamValue(context, "guildName", "");
		if (humanObj == null || guildName.isEmpty()) {
			Log.game.info("humanObj={}, guildName={}", humanObj, guildName);
			sendSCGuildCreateResultMsg(humanObj);// 返回创建公会失败结果
			return;
		}
		boolean repeat = Utils.getParamValue(results, "repeat", true);
		if (repeat) {// 查询结果
			humanObj.sendSysMsg(12001);// 名字重复
			sendSCGuildCreateResultMsg(humanObj);//返回创建公会失败结果
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.isCanGuild(humanObj.getHuman().getId(), guildName);
		pxy.listenResult(this::_result_isCanGuild, HumanObject.paramKey, humanObj, "guildName", guildName,"content",content,"icon",icon);
	}
	
	/**
	 * 是否能创建公会
	 * @param results
	 * @param context
	 */
	public void _result_isCanGuild(Param results, Param context){
		boolean isCan = Utils.getParamValue(results, "isCan", false);
		if(!isCan){
			return;
		}
		String content = context.get("content");
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if (humanObj == null) {
			Log.logGameObjectNull();
			sendSCGuildCreateResultMsg(humanObj);//返回创建公会失败结果
			return;
		}
//		if(!humanObj.checkHumanSwitchState(results, context, true)){
//			return;
//		}
		int icon = context.get("icon");
		String guildName = Utils.getParamValue(context, "guildName", "");
		if(Utils.isEmptyJSONString(guildName)){
			return;
		}
		int costGold = RewardHelper.getCostGold(ECostGoldType.CreateGuild,icon);
		//扣钱
		if(!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGold,LogSysModType.GuildCreate)) {
			return;
		}
		
		
		// 同步创建公会 真正的创建公会操作
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.createGuild(humanObj.getHuman(), guildName,content,icon);
		pxy.listenResult(this::_result_createGuild, HumanObject.paramKey, humanObj);
	}
	
	/**
	 * 返回创建公会是否成功
	 * @param results
	 * @param context
	 */
	public void _result_createGuild(Param results, Param context) {
		Guild guild = Utils.getParamValue(results, "guild", null);
		boolean result = Utils.getParamValue(results, "result", false);
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if (humanObj == null) {
			Log.logGameObjectNull();
			sendSCGuildCreateResultMsg(humanObj);//返回创建公会失败结果
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Human human = humanObj.getHuman();
		// 返回创建公会消息
		SCGuildCreateResult.Builder msg = SCGuildCreateResult.newBuilder();
		msg.setResult(result); // true 创建成功
		if (guild != null && result) {
			humanObj.sendSysMsg(530112);// 创建公会成功
			msg.setGuildId(guild.getId());
			if (human.getGuildId() != guild.getId()) {
				human.setGuildId(guild.getId());//设置所属公会
				human.setGuildName(guild.getGuildName());//设置公会名
				//human.setGuildLevel(guild.getGuildLevel());//设置所属公会等级
                addGuildSkillProp(humanObj);
				// 同步公会名字
				HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
				pxy.updatGuildName(human.getId(), guild.getGuildName());
			}
			// 更新公会排行榜排名
//			RankManager.inst().addNewRank(humanObj, RankType.RankGuild, true);
			// 发送事件，创建军团副本
			//Event.fire(EventKey.GuildChapter, HumanObject.paramKey, humanObj,"guild",guild);		
		}
		humanObj.sendMsg(msg);

	}
	

	/**
	 * 返回创建公会失败结果
	 * @param humanObj
	 */
	public void sendSCGuildCreateResultMsg(HumanObject humanObj){
		SCGuildCreateResult.Builder msg = SCGuildCreateResult.newBuilder();
		msg.setResult(false);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 创建公会信息
	 * @param guild
	 * @return
	 */
	public DGuildInfo.Builder getDGuildMsg(Guild guild,int lv) {

		DGuildInfo.Builder dGuildInfo = DGuildInfo.newBuilder();
		dGuildInfo.setGuildId(guild.getId()); // 公会id
		dGuildInfo.setGuildName(guild.getGuildName()); // 公会名字
		dGuildInfo.setGuildLevel(guild.getGuildLevel()); // 公会等级
		dGuildInfo.setInitiationMinLevel(guild.getInitiationMinLevel()); // 入会最低等级
		dGuildInfo.setGuildLiveness(guild.getGuildPlan()); // 公会活跃度
		dGuildInfo.setLeadrtId(guild.getGuildLeaderId());//会长id
		dGuildInfo.setGuildCDRName(guild.getGuildLeaderName()); // 会长名字
		dGuildInfo.setGuildOwnNum(guild.getGuildOwnNum()); // 拥有会员人数
		int can = 2;
		int isApply = 0;
		if(lv >= guild.getInitiationMinLevel()) {
			isApply = 1;
		}
		if(isApply == 1 && guild.getGuildStatus() == 1) {
			can = 1;
		}
		dGuildInfo.setGuildStatus(can); // 公会状态 1可加入, 2 需申请
		dGuildInfo.setGuildDeclare(guild.getGuildDeclare()); // 公会宣告
		dGuildInfo.setGuildNotice(guild.getGuildNotice()); // 公会内部宣告
		dGuildInfo.setGuildIcon(guild.getGuildIcon()); // 公会图标
		dGuildInfo.setGuildExp(guild.getGuildExp()); // 公会经验
		dGuildInfo.setGuildQQ(guild.getQQ()); // QQ群
		dGuildInfo.setGuildPlan(guild.getGuildPlan()); // 公会每日进度
		dGuildInfo.setGuildCombat(guild.getGuildCombat());// 公会战力
		dGuildInfo.setCurrentPersion(guild.getGuildImmoNum());
		return dGuildInfo;
	}

	/**
	 * 分页查看所有公会信息
	 * @param humanObj
	 */
	public void _msg_CSGuildInfo(HumanObject humanObj, int page) {
//		if (!ModunlockManager.inst().isUnlock(ModType.Guild.value(), humanObj.getHuman())) {
//			humanObj.sendSysMsg(10009);// 功能未开启
//			return;
//		}
		long humanId = humanObj.getHumanId();
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.selectAllGuildInfo(humanId,page);
		pxy.listenResult(this::_result_msg_CSGuildInfo, HumanObject.paramKey, humanObj, "page", page);
	}

	/**
	 * 分页查看公会消息返回
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildInfo(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		List<Guild> listGuild = results.get("listGuild");
		if(listGuild == null) {
			Log.guild.info("===  listGuild is null");
			return;
		}
		int page = results.get("page");
		int listGuildSize = results.get("listGuildSize");
		List<Long> applyList = results.get("applyList");
		SCGuildInfoResult.Builder msg = SCGuildInfoResult.newBuilder();
		msg.setCurrentPage(page); // 当前查看的是第page 页
		msg.setSize(listGuildSize); // 总公会个数
		msg.addAllMyApplyId(applyList);
		int lv = humanObj.getHuman().getLevel();
		for(Guild guild:listGuild) {
			DGuildInfo.Builder dGuildInfo = GuildManager.inst().getDGuildMsg(guild,lv);
			msg.addGuildInfo(dGuildInfo);
		}
		// 返回查询的公会消息
		humanObj.sendMsg(msg);
	}

	/**
	 * 设置公会基础信息
	 * @param humanObj
	 * @param guildId 公会id
	 * @param initiationMinLevel 入会最低等级
	 * @param isApply 是否需要申请 0 默认可以直接加入， 1 需要申请
	 */
	public void _msg_CSGuildSet(HumanObject humanObj, long guildId, int QQ, int isApply, int initiationMinLevel) {
		long humanId = humanObj.getHumanId();
		long belongGuild = humanObj.getHuman().getGuildId();
		if (guildId != belongGuild) {
			return;
		}

		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.changeGuildSet(humanId, belongGuild, QQ, isApply, initiationMinLevel);
		pxy.listenResult(this::_result_msg_CSGuildSet, HumanObject.paramKey, humanObj, "pos",1);
	}
	
	/**
	 * 公会改名
	 * @param humanObj
	 * @param guildName
	 */
	public void _msg_SCGuildRename(HumanObject humanObj, long guildId, String guildName){
		long belongGuild = humanObj.getHuman().getGuildId();
		if (guildId != belongGuild) {
			return;
		}
		// 改名消耗钻石
		int costGold = RewardHelper.getCostGold(ECostGoldType.ChangeGuildName, 1);
		if(!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGold,LogSysModType.GuildRename)) {
			return;
		}
		
		// 判断名字是否合法
		boolean isCanUse = canUseName(humanObj, guildName, true);
		if (!isCanUse) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.isCanRename(belongGuild, humanObj.getHumanId(), guildName);
		pxy.listenResult(this::_result_msg_SCGuildRename, HumanObject.paramKey, humanObj, "guildName", guildName);
	}

	/**
	 * 获取判断是否改名，可以就扣钱并改名，否则返回失败
	 * @param results
	 * @param context
	 */
	public void _result_msg_SCGuildRename(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		boolean result = Utils.getParamValue(results, "result", false);
		if (!result) {// 不能改名
			humanObj.sendSysMsg(530102);// 设置失败
			return;
		}
		String guildName = Utils.getParamValue(context, "guildName", "");
		if (guildName.isEmpty() || guildName.equals("")) {
			return;
		}
		int costGold = ParamManager.guildRename;
		if(costGold > 0){
			RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGold,LogSysModType.GuildRename);
		}
		long belongGuild = humanObj.getHuman().getGuildId();
		// 通知公会名字改变
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.rename(belongGuild, guildName);// 改名
		pxy.listenResult(this::_result_msg_CSGuildSet, HumanObject.paramKey, humanObj, "pos", 2);		
	}
	
	
	
	/**
	 *  修改公会宣告
	 * @param humanObj
	 * @param guildId
	 * @param declare
	 */
	public void _msg_SCDeclare(HumanObject humanObj, long guildId, String declare){
		long belongGuild = humanObj.getHuman().getGuildId();
		if (guildId != belongGuild) {
			return;
		}
		// 检查名字是否有屏蔽字，如果有屏蔽字返回
		String fix = SensitiveWordFilter.getInstance().getSensitiveWord(declare);
		if (fix != null) {
			humanObj.sendSysMsg(143);// 名字不合法，请检查敏感字
			return ;
		}
		GuildServiceProxy prxy = GuildServiceProxy.newInstance();
		prxy.updateDeclare(belongGuild, humanObj.getHumanId(), declare);
		prxy.listenResult(this::_result_msg_CSGuildSet,HumanObject.paramKey, humanObj, "pos", 3);	
	}
	
	/** 修改公会内部宣告
	 * @param humanObj
	 * @param guildId
	 * @param notice
	 */
	public void _msg_SCNotice(HumanObject humanObj, long guildId, String notice){
		long belongGuild = humanObj.getHuman().getGuildId();
		if (guildId != belongGuild) {
			return;
		}
		// 检查名字是否有屏蔽字，如果有屏蔽字返回
		String fix = SensitiveWordFilter.getInstance().getSensitiveWord(notice);
		if (fix != null) {
			humanObj.sendSysMsg(143);// 名字不合法，请检查敏感字
			return ;
		}
		
		GuildServiceProxy prxy = GuildServiceProxy.newInstance();
		prxy.updateNotice(belongGuild, humanObj.getHumanId(), notice);
		prxy.listenResult(this::_result_msg_CSGuildSet,HumanObject.paramKey, humanObj, "pos", 4);	
	}
	
	/**
	 * 修改公会图标
	 * @param humanObj
	 * @param guildId
	 * @param icon
	 */
	public void _msg_SCGuildIcon(HumanObject humanObj, long guildId, int icon){
		long belongGuild = humanObj.getHuman().getGuildId();
		if (guildId != belongGuild) {
			return;
		}
		GuildServiceProxy prxy = GuildServiceProxy.newInstance();
		prxy.updateIcon(belongGuild, humanObj.getHumanId(), icon);
		prxy.listenResult(this::_result_msg_CSGuildSet,HumanObject.paramKey, humanObj, "pos", 5);	
	}
	
	/**
	 * 返回设置结果
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildSet(Param results, Param context){
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		boolean result = Utils.getParamValue(results, "result", false);
		int pos = Utils.getParamValue(context, "pos", 0);
		
		if(!result){
			// TODO 这里应该是提示失败消息
		}
		//如果改名失败就退钱
		if(pos == 2 && !result) {
			int costGold = ParamManager.guildRename;
			if(costGold > 0){
				RewardHelper.reward(humanObj, EMoneyType.gold_VALUE, costGold,LogSysModType.GuildRename);
			}
		}
		
		SCGuildSet.Builder msg = SCGuildSet.newBuilder();
		msg.setResult(result);
		msg.setPos(pos);
		humanObj.sendMsg(msg);
		if(pos == 2){//名字修改
			Guild guild = Utils.getParamValue(results, "guild", null);
			if(guild == null){
				Log.guild.error("_result_msg_CSGuildSet guild=null");
				return;
			}
			//TODO 更新数据到所有玩家
			_result_msg_guildRenameNotice(guild);
		}
		
		
	}
	

	/**
	 *  更新数据到所有玩家
	 * @param guild
	 */
	private void _result_msg_guildRenameNotice(Guild guild){
		if(guild == null){
			return;
		}
		//公会成员总信息
		List<GuildData> guildListbean = GuildData.jsonToList(guild.getGuildHuman());
		if (guildListbean.isEmpty()) {
			return;
		}
		String guildName = guild.getGuildName();
		for (GuildData bean : guildListbean) {
			if (bean == null)
				continue;			
			HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
			pxy.updatGuildName(bean.id, guildName);
		}		
	}
	
	/**
	 * 根据公会名字搜索公会
	 * @param humanObj
	 * @param guildName
	 */
	public void _msg_CSGuildSeek(HumanObject humanObj, String guildName) {
		int length = ParamManager.guildNameLength;
		if (guildName.isEmpty()) {
			humanObj.sendSysMsg(50);// 名字不能为空
			return;
		}
		if (guildName.length() < 1 || guildName.length() > length) {
			humanObj.sendSysMsg(51, "B", length, "B1", 1);// 名字长度不能超过{}8
			return;
		}
		boolean can = canUseName(humanObj,guildName,true);
		if(!can) {
			Log.guild.debug("can't get Name");
			return;
		}
		GuildServiceProxy prx = GuildServiceProxy.newInstance();
		prx.getNameSimilarGuild(guildName);
		prx.listenResult(this::_result_msg_CSGuildSeek, HumanObject.paramKey, humanObj);
	}
	/**
	 * 返回根据名字搜索公会结果
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildSeek(Param results, Param context) {
		// 上下文环境
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		List<Guild> listGuild = Utils.getParamValue(results, "listGuild", null);
		SCGuildSeekResult.Builder msg = SCGuildSeekResult.newBuilder();
		if (listGuild == null || listGuild.isEmpty()) {
			Log.guild.error("humanObj={}, listGuild={}", humanObj, listGuild);
			humanObj.sendSysMsg(530107);// 未搜索到相关公会！
			humanObj.sendMsg(msg);
			return;
		}
		for (int i = 0; i < listGuild.size(); i++) {
			Guild guild = listGuild.get(i);
			if (guild == null) {
				continue;
			}
			msg.addGuildInfo(getDGuildMsg(guild,humanObj.getHuman().getLevel()));
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 加入公会
	 * @param humanObj
	 * @param type
	 * @param guildId
	 */
	public void _msg_CSGuildJoin(HumanObject humanObj, int type, long guildId) {
//		if (!ModunlockManager.inst().isUnlock(ModType.Guild.value(), humanObj.getHuman())) {
//			humanObj.sendSysMsg(10009);// 功能未开启
//			return;
//		}
		Human human = humanObj.getHuman();
		if (human.getGuildId() != 0) {
			Log.guild.info("玩家已在公会中 guildid={}",human.getGuildId());
			return;
		}
		boolean isCd = isInLimitTime(human.getGuildLeaveTime());
		if (isCd) {// 判断是否在冷却时间内
			humanObj.sendSysMsg(11109);// 退出仙盟不满4小时,无法加入新仙盟
			return;
		}
		switch (type) {
			case 0 :// 快速加入
				GuildServiceProxy proxy = GuildServiceProxy.newInstance();
				proxy.autoAddGuild(human.getId(), human.getLevel());
				proxy.listenResult(this::_result_msg_CSGuildJoin, HumanObject.paramKey, humanObj);
				break;
			case 1 :// 加入
				GuildData bean = new GuildData(human, 1, 0, 0);
				GuildServiceProxy pxy = GuildServiceProxy.newInstance();
				pxy.isCanGuildJoin(bean, guildId);
				pxy.listenResult(this::_result_isCanGuildJoin, HumanObject.paramKey, humanObj);
				break;
			case 2 :// 申请加入
				
				HumanExtInfo exInfo = humanObj.getHumanExtInfo();
				GuildServiceProxy gspxy = GuildServiceProxy.newInstance();
				gspxy.isCanApply(human,guildId);
				gspxy.listenResult(this::_result_apply, HumanObject.paramKey, humanObj);
				break;
			default :
				break;
		}

	}

	/**
	 * 申请加入
	 * @param results
	 * @param context
	 */
	public void _result_apply(Param results, Param context) {		
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		boolean isCanApply = Utils.getParamValue(results, "result", false);
		if(!isCanApply){//申请失败
			int sysMsgSn = Utils.getParamValue(results, "sysMsgSn", 0);
			if(sysMsgSn > 0){
				humanObj.sendSysMsg(sysMsgSn);//发送提示
			}
			SCGuildJoinResult.Builder msg = SCGuildJoinResult.newBuilder();
			msg.setResult(false);
			msg.setIsApply(1);
			humanObj.sendMsg(msg);
		} else {
			SCGuildJoinResult.Builder msg = SCGuildJoinResult.newBuilder();
			msg.setResult(true);
			msg.setIsApply(1);
			humanObj.sendMsg(msg);
		}
	}

	/**
	 * 查看所有申请人信息
	 * @param humanObj
	 */
	public void _msg_CSApplyInfo(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		long guildId = human.getGuildId();
		if (guildId <= 0) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.seeApplyInfo(guildId);
		pxy.listenResult(this::_result_msg_CSApplyInfo, HumanObject.paramKey, humanObj);
	}

	/**
	 * 返回申请信息
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSApplyInfo(Param results, Param context) {	
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		SCApplyInfoResult msg = Utils.getParamValue(results, "msg", null);
		if (msg == null ) {
			Log.game.error("humanObj={}, msg={}", humanObj, msg);
			return;
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 判断是否拥有官职、权限
	 * @param json
	 * @param humanId
	 * @return
	 */
	public boolean isOwnGuildPost(String json, long humanId) {
		JSONObject member = Utils.toJSONObject(json);
		if (!member.isEmpty()) {
			String key = String.valueOf(humanId);
			if (member.containsKey(key)) { // 有官职的都可以设置
				return true;
			}
		}
		return false;
	}

	public String removeGuildPost(String json, long humanId) {
		JSONObject guildPost = Utils.toJSONObject(json);
		if (!guildPost.isEmpty()) {
			guildPost.remove(String.valueOf(humanId));
		}
		return guildPost.toJSONString();
	}


	/**
	 * 根据humanId 获取位置
	 * @param json
	 * @param humanId
	 * @return
	 */
	public int getGuildPost(String json, long humanId) {
		JSONObject guildPost = Utils.toJSONObject(json);
		int post = 0;
		if (!guildPost.isEmpty()) {
			post = guildPost.getIntValue(String.valueOf(humanId));
		}
		return post;
	}
	
	/**
	 * 公会等级是否满足图标解锁等级
	 * @param sn 图标sn
	 * @param guildLv
	 * @return
	 */
	public boolean isIconMeetGuildLv(int sn, int guildLv){
		ConfGuildIcon confIcon = ConfGuildIcon.get(sn);
		if(confIcon == null){
			return false;
		}
		if(confIcon.level > guildLv){
			return false;
		}
		return true;
		
	}

	/**
	 * 快速加入公会
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildJoin(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		long guildId = Utils.getParamValue(results, "guildId", 0L);
		if (guildId <= 0) {
			humanObj.sendSysMsg(432102);//未找到满足条件且可加入的公会！
			Log.game.info("guildId={}",guildId);
			return;
		}

		Human human = humanObj.getHuman();
		// 执行加入公会操作
		boolean result = Utils.getParamValue(results, "result", false);
		if (result) {
			GuildData bean = new GuildData(human, 1, 0, 0);
			// 数据库存在该玩家
			GuildServiceProxy pxy = GuildServiceProxy.newInstance();
			pxy.guildJoin(bean, guildId);
			pxy.listenResult(this::_result_msg_SCGuildJoinResult, HumanObject.paramKey, humanObj, "isApply", false);
		}else{
			SCGuildJoinResult.Builder msg = SCGuildJoinResult.newBuilder();
			msg.setResult(false);
			humanObj.sendMsg(msg);
		}
	}

	/**
	 * 指定加入某公会
	 * @param results
	 * @param context
	 */
	public void _result_isCanGuildJoin(Param results, Param context) {
		// 上下文环境
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		
		Guild guild = Utils.getParamValue(results, "guild", null);
		if (null == guild) {
			humanObj.sendSysMsg(432102);//未找到满足条件且可加入的公会！
			Log.logGameObjectNull("guild");
			return;
		}
		boolean result = Utils.getParamValue(results, "result", false);
		if(result){//加入成功
			HumanGlobalInfo hgInfo = humanObj.humanGlobalInfo;
			if(null != hgInfo){
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hgInfo.nodeId,hgInfo.portId, hgInfo.id);
				prxHumanObj.guildJoin(true, guild.getId(), guild.getGuildLevel(), guild.getGuildName(), 1);// 通知加入了公会
				prxHumanObj.listenResult(this:: _result_result_findOnlineHuman, "guildId", guild.getId(), "replyId"
						, hgInfo.id,"guildLv",guild.getGuildLevel(),"guildName",guild.getGuildName());
			}
			SCGuildJoinResult.Builder msg = SCGuildJoinResult.newBuilder();
			msg.setResult(true);
			humanObj.sendMsg(msg);
		} else {
			// 是否需要申请
			boolean isApply = Utils.getParamValue(results, "isApply", false);
			if(!isApply){
				int sysMsgSn = Utils.getParamValue(results, "sysMsgSn", 0);
				humanObj.sendSysMsg(sysMsgSn);//未找到满足条件且可加入的公会！
			}
			SCGuildJoinResult.Builder msg = SCGuildJoinResult.newBuilder();
			msg.setResult(false);
			humanObj.sendMsg(msg);
		}
	}

	/**
	 * 返回加入公会返回
	 * @param results
	 * @param context
	 */
	public void _result_msg_SCGuildJoinResult(Param results, Param context) {

		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(results, "guild", null);
		boolean repeat = Utils.getParamValue(results, "repeat", false);
		boolean isApply = Utils.getParamValue(context, "isApply", false);
		long guildId = 0;
		if (guild != null) {
			guildId = guild.getId();
		}
		if (isApply) {// 是同意申请加入
			long humanId = Utils.getParamValue(context, "humanId", -1L);// 获取申请入会玩家id
			if (humanId <= 0) {
				Log.game.error("humanId={}", humanId);
				return;
			}
			int onlineStatus = Utils.getParamValue(context, "onlineStatus", -1);
			if (repeat && guild != null) {
				// XXX 这个改为待办事件，那么加入公会和同意申请中要加判断待办事件中是否有事件
				GuildServiceProxy pxy = GuildServiceProxy.newInstance();
				pxy.removeApplyInfo(guildId, humanId);
				if (onlineStatus == 1) {
					HumanGlobalInfo humanInfo = Utils.getParamValue(context, "humanInfo", null);
					if (humanInfo != null) {
						HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(humanInfo.nodeId,
								humanInfo.portId, humanInfo.id);
						prxHumanObj.guildJoin(true, guild.getId(), guild.getGuildLevel(), guild.getGuildName(), 1);// 通知加入了公会
						prxHumanObj.listenResult(this:: _result_result_findOnlineHuman, "guildId", guild.getId()
								, "replyId", humanInfo.id,"guildLv",guild.getGuildLevel(),"guildName",guild.getGuildName());
					}
				} else {
					// XXX 这里是否不需要放到else中，因为是否在线存在不确定性
					// 添加一个待办事项：加入公会
					JSONObject join = new JSONObject();
					join.put(GuildIdKey, guild.getId());
					join.put(Human.K.GuildLevel, guild.getGuildLevel());
					join.put(GuildNameKey, guild.getGuildName());
					HumanManager.inst().saveOneBacklog(humanId, BacklogType.GuildJoin, join);
				}
			}
			sendGuildSysMail(humanId, guild.getGuildName());// 发送邮件通知玩家加入公会
			SCApplyReplyResult.Builder msg = SCApplyReplyResult.newBuilder();
			msg.setResult(repeat);
			msg.setHumanId(humanId);
			humanObj.sendMsg(msg);
		} else {//直接加入
			SCGuildJoinResult.Builder msg = SCGuildJoinResult.newBuilder();
			msg.setResult(repeat);
			if (repeat && guild != null) {
				msg.setGuildId(guildId);
				Human human = humanObj.getHuman();		
				sendGuildSysMail(human.getId(), guild.getGuildName());// 发送邮件通知玩家加入公会
				humanObj.sendSysMsg(530106);// 入会成功
				HumanGlobalInfo hgInfo = humanObj.humanGlobalInfo;
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hgInfo.nodeId, hgInfo.portId, hgInfo.id);
				prxHumanObj.guildJoin(true, guild.getId(), guild.getGuildLevel(), guild.getGuildName(), 0);// 通知加入了公会
				prxHumanObj.listenResult(this:: _result_result_findOnlineHuman, "guildId", guild.getId()
						, "replyId", hgInfo.id,"guildLv",guild.getGuildLevel(),"guildName",guild.getGuildName());
				
			}
			humanObj.sendMsg(msg);
		}
	}

	/**
	 * 发送入会邮件
	 * @param humanId
	 * @param guildName
	 */
	private void sendGuildSysMail(long humanId, String guildName) {
		String title = ParamManager.mailMark;// 获取特殊邮件标记
		// {sn|公会名字}
		String detail = "{" + MailType.GuildJoin.value() + "|" + guildName + "}";
		MailManager.inst().sendSysMail(humanId, title, detail, null);
	}

	/**
	 * 发送被踢出公会邮件
	 * @param humanId
	 * @param guildName
	 */
	private void sendKickOutGuildSysMail(long humanId, String guildName) {
		String title = ParamManager.mailMark;// 获取特殊邮件标记
		// {sn|公会名字}
		String detail = "{" + EMailType.MailGuildKickout_VALUE + "|" + guildName + "}";
		MailManager.inst().sendSysMail(humanId, title, detail, null);
	}

	/**
	 * 会长离线太久 向会员发送邮件消息
	 * @param humanId
	 */
	public void sendGuildCDROfflineMail(long humanId) {
//		String title = ParamManager.mailMark;// 获取特殊邮件标记 FIXME
//		// {sn}
//		String detail = "{" + EMailType.GuildLeaderOffline.value() + "}";
//		MailManager.inst().sendSysMail(humanId, title, detail, null);
	}

	/**
	 * 会长离线太久 自动更换会长
	 * @param humanId
	 * @param name 新会长名字
	 */
	public void sendGuildNewCDRMail(long humanId, String name) {
		String title = ParamManager.mailMark;// 获取特殊邮件标记
		// {sn|新会长名字}
		String detail = "{" + EMailType.MailGuildNewLeader_VALUE + "|" + name + "}";
		MailManager.inst().sendSysMail(humanId, title, detail, null);
	}

	/**
	 * 解散公会
	 * @param humanId
	 * @param humanName
	 * @param guildName
	 */
	public void sendRemoveGuildSysMail(long humanId, String guildName, String humanName) {
		String title = ParamManager.mailMark;// 获取特殊邮件标记
		// {sn|公会名字|会长名字}
		String detail = "{" + MailType.GuildRemove.value() + "|" + guildName + "|" + humanName + "}";
		MailManager.inst().sendSysMail(humanId, title, detail, null);
	}

	/**
	 * 是否在冷却时间内
	 * @param leaveTime
	 * @return
	 */
	public boolean isInLimitTime(long leaveTime) {
		long time = Port.getTime();// 获取当前时间
		int hour = (int) ((time - leaveTime) / (Utils.I1000 * 60 * 60));// 获取退会了多久
		int addGuildLimitTime = ParamManager.addGuildLimitTime;// 获取退会后冷却时间
		if (hour < addGuildLimitTime) {// 判断是否过了冷却时间
			return true;
		}
		return false;
	}

	/**
	 * 主动退出公会
	 * @param humanObj
	 */
	public void _msg_CSGuildLeave(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		long guildId = human.getGuildId();
		if (guildId <= 0) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildInfo(guildId, human);
		pxy.listenResult(this::_result_msg_CSGuildLeave, HumanObject.paramKey, humanObj);
		// pxy.listenResult(this::_result_msg_CSGuildLeave, "human", human);
	}

	/**
	 * 主动退会
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildLeave(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(results, "guild", null);
		if (guild == null) {
			Log.logGameObjectNull("guild");
			return;
		}
		if(guild.getGuildLeaderId() == humanObj.getHumanId()) {
			Log.guild.info("盟主不可退会");
			humanObj.sendSysMsg(568013);//盟主不可退出，请先转让工会
			return;
		}
		Human human = humanObj.getHuman();
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.guildLeave(human, guild);
		pxy.listenResult(this::_result_msg_SCGuildLeaveResult, HumanObject.paramKey, humanObj, "guildId", guild.getId());
	}

	/**
	 * 返回退会结果
	 * @param results
	 * @param context
	 */
	private void _result_msg_SCGuildLeaveResult(boolean timeout, Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		boolean result = Utils.getParamValue(results, "result", false);
		if (humanObj == null) {
			Log.logGameObjectNull();
			return;
		}
		if(timeout){
			// 添加一个待办事项：退出公会
			JSONObject joleave = new JSONObject();
			joleave.put(GuildIdKey, humanObj.getHuman().getGuildId());
			HumanManager.inst().saveOneBacklog(humanObj.id, BacklogType.GuildLeave, joleave);
			return;
		}
		SCGuildLeaveResult.Builder msg = SCGuildLeaveResult.newBuilder();
		msg.setResult(result);
		if (result) {
			msg.setPertainGuild(0);
			Human human = humanObj.getHuman();
			human.setGuildId(0);//设置所属公会
			human.setGuildName("");
			human.setGuildLevel(0);//设置所属公会等级
			human.setGuildLeaveTime(Port.getTime());
			humanObj.sendSysMsg(530108);// 退出公会成功！
			long guildId = Utils.getParamValue(context, "guildId", 0L);
			HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(humanObj.humanGlobalInfo.nodeId,
					humanObj.humanGlobalInfo.portId, humanObj.humanGlobalInfo.id);
			prxHumanObj.guildLeave(true, 0);// 通知在线的退公会 0：主动退会 1：被踢出公会
			prxHumanObj.listenResult(this::_result_guildLeave,"guildId", guildId, "humanId",human.getId());
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 产生新的会长
	 * @param guild
	 * @return
	 */
	public GuildData getGuildListBean(Guild guild) {
		GuildData guildListBean = null;
		List<GuildData> randomListGuild = new ArrayList<GuildData>();// 当职位等同
																		// 贡献等同的几个玩家
																		// 随机
		String json = guild.getGuildHuman();
		List<GuildData> listGuild = GuildData.jsonToList(json);
		for (GuildData guildBean : listGuild) {
			if (guildBean == null)
				continue;
			long id = guildBean.id;
			if (id == guild.getGuildLeaderId())
				continue;
			boolean isGoOut = GuildManager.inst().isGoOut(guildBean.timeLogout, ParamManager.offlineTimeLimit);// 判断是否有符合要求的会员
			if (!isGoOut) {
				if (guildListBean == null) {
					guildListBean = guildBean;
					continue;
				} else if (guildListBean.post > guildBean.post) {// post越小,职位越大,0除外
					if (guildBean.post != 0) {
						guildListBean = guildBean;
						randomListGuild = new ArrayList<GuildData>();// 清空集合
						continue;
					}
				} else if (guildListBean.post == guildBean.post && guildListBean.contribute < guildBean.contribute) {
					// 相同职位,有更高的贡献时,替换
					guildListBean = guildBean;
					randomListGuild = new ArrayList<GuildData>();// 清空集合
					continue;
				} else if (guildListBean.post == guildBean.post && guildListBean.contribute == guildBean.contribute) {
					// 多个相同贡献,添加进集合
					if (!randomListGuild.contains(guildListBean)) {
						randomListGuild.add(guildListBean);
					}
					if (!randomListGuild.contains(guildBean)) {
						randomListGuild.add(guildBean);
					}
					continue;
				}
				continue;
			}
		}
		if (!randomListGuild.isEmpty()) {// 有多个玩家可以当任会长 就随机产生一个玩家当会长
			int size = randomListGuild.size();
			int index = (int) (Math.random() * size - 1);
			guildListBean = randomListGuild.get(index);
		}
		if (guildListBean != null) {
			guildListBean.post = 1;// 设为会长
		}
		return guildListBean;
	}

	/**
	 * 判断一个时间是否已经过时了
	 * @param time
	 * @param limitTime
	 * @return
	 */
	public boolean isGoOut(long time, int limitTime) {
		int day = Utils.getDaysBetween(time, Port.getTime());
		// 刚加入的玩家time=0
		if (day >= limitTime && time != 0) {
			return true;
		}
		return false;
	}

	/**
	 * 分页查看会员信息
	 * @param humanObj
	 */
	public void _msg_CSGuildMemberInfo(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		sendSCGuildImmoInfoResult(humanObj);//献祭信息
		long belongGuild = human.getGuildId();
		if (belongGuild <= 0) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildInfo(belongGuild, human);
		pxy.listenResult(this::_result_SCGuildMemberInfo, HumanObject.paramKey, humanObj);
	}

	/**
	 * 返回公会成员信息
	 */
	public void _result_SCGuildMemberInfo(Param results, Param context) {

		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(results, "guild", null);
		if (guild == null ) {
			Log.game.error("humanObj={},guild={}", humanObj, guild);
			return;
		}
		List<GuildData> guildListbean = GuildData.jsonToList(guild.getGuildHuman());
		if (guildListbean.isEmpty()) {
			return;
		}
		SCGuildMemberResult.Builder msg = SCGuildMemberResult.newBuilder();
		msg.setGuildInfo(getDGuildMsg(guild,humanObj.getHuman().getLevel()));
		for (GuildData bean : guildListbean) {
			if (bean == null) {
				continue;
			}
			msg.addGuildMemberInfo(bean.createMsg());
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 踢出公会(1)
	 * @param humanObj
	 * @param humanId 被踢的人的id
	 */
	public void _msg_CSGuildKickOut(HumanObject humanObj, long humanId) {
		if (humanObj.getHumanId() == humanId) {// 不能踢自己
			return;
		}
		long guildId = humanObj.getHuman().getGuildId();
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildInfo(guildId, humanObj.getHuman());
		pxy.listenResult(this::_result_msg_CSGuildKickOut, HumanObject.paramKey, humanObj, "humanId", humanId);
	}

	/**
	 * 踢出公会（2） 判断是否
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildKickOut(Param results, Param context) {

		Guild guild = Utils.getParamValue(results, "guild", null);
		if (guild == null) {
			Log.logGameObjectNull("guild");
			return;
		}
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		long killerid = humanObj.getHumanId();
		long bekillhumanId = Utils.getParamValue(context, "humanId", 0L);
		if (bekillhumanId == 0) {
			Log.game.error("humanId=0");
			return;
		}
		// 不能踢会长,避免公会会长被踢
		if(bekillhumanId == guild.getGuildLeaderId()){
			return;
		}
		boolean ret = isOwnGuildPost(guild.getGuildPostMember(), killerid);
		if (killerid != guild.getGuildLeaderId() && !ret) {// 不是会长 也没有职位 就没有权限
			Log.guild.info("没有职权！");
			return;
		}
		int post1 = getGuildPost(guild.getGuildPostMember(), killerid);
		int post2 = getGuildPost(guild.getGuildPostMember(), bekillhumanId);
		if (post2 != 0 && post2 <= post1) {// 被踢的会员职位更大 没有权限踢他
			return;
		}

		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.kickOutGuild(guild, bekillhumanId);
		pxy.listenResult(this::_result_msg_SCGuildKickOut, HumanObject.paramKey, humanObj, "humanId", bekillhumanId, "guildName",
				guild.getGuildName(),"guildId",guild.getId());
	}

	/**
	 * 返回踢人结果
	 * @param results
	 * @param context
	 */
	public void _result_msg_SCGuildKickOut(Param results, Param context) {

		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		boolean result = Utils.getParamValue(results, "result", false);
		long humanId = Utils.getParamValue(context, "humanId", 0L);// 被踢玩家id
		if (humanId == 0) {
			Log.game.error("humanObj={},humanId={}", humanObj, humanId);
			return;
		}
		SCGuildKickOut.Builder msg = SCGuildKickOut.newBuilder();
		msg.setResult(result);
		humanObj.sendMsg(msg);
		if (result) {//踢出成功
			long guildId = Utils.getParamValue(context, "guildId", 0L);// 公会id
			if(0 == guildId){
				Log.game.error("guildId={}", guildId);
				return;
			}
			String guildName = Utils.getParamValue(context, "guildName", "");
			if (guildName.isEmpty()) {
				Log.game.error("guildName=null");
				return;
			}
			informLeaveGuild(humanId, guildId, guildName, true);// 通知该玩家离开公会
		}
	}

	/**
	 * 通知该玩家离开公会
	 * @param humanId
	 */
	public void informLeaveGuild(long humanId, long guildId, String guildName, boolean isKickOut) {
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_findOnlineHumanKickOut, "humanId", humanId, "guildId", guildId, "guildName", guildName, "isKickOut", isKickOut);
	}

	/**
	 * 现在玩家 被提出公会
	 * @param results
	 * @param context
	 */
	private void _result_findOnlineHumanKickOut(Param results, Param context) {
		long humanId = Utils.getParamValue(context, "humanId", 0L);
		long guildId = Utils.getParamValue(context, "guildId", 0L);// 公会id
		HumanGlobalInfo hgInfo = results.get();
		if (humanId == 0 || guildId == 0) {
			Log.game.error("humanId={}, guildId={}", humanId, guildId);
			return;
		}
		String guildName = Utils.getParamValue(context, "guildName", "");
		if (guildName.isEmpty()) {
			Log.game.error("guildName=null");
			return;
		}
		boolean isKickOut = Utils.getParamValue(context, "isKickOut", false);// 公会id
		if (null != hgInfo) {// 在线
//			if(hgInfo.guildId != guildId){
//				return;
//			}
			if(isKickOut){//是否被踢出
				sendKickOutGuildSysMail(humanId, guildName);// 发送被踢邮件消息
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hgInfo.nodeId, hgInfo.portId, hgInfo.id);
				prxHumanObj.guildLeave(true, 1);// 通知在线的退公会
				prxHumanObj.listenResult(this::_result_guildLeave,"guildId", guildId, "humanId",humanId);
			}else{
				HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(hgInfo.nodeId, hgInfo.portId, hgInfo.id);
				prxHumanObj.guildLeave(true, 0);// 通知在线的退公会
				prxHumanObj.listenResult(this::_result_guildLeave,"guildId", guildId, "humanId",humanId);
			}
		} else {
			// XXX 在此步骤之前是否需要查询玩家是否真的在此公会？暂时不做查询判断
			if(isKickOut){
				sendKickOutGuildSysMail(humanId, guildName);// 发送被踢邮件消息
			}
			// 添加一个待办事项：退出公会
			JSONObject joleave = new JSONObject();
			joleave.put(GuildIdKey, guildId);
			HumanManager.inst().saveOneBacklog(humanId, BacklogType.GuildLeave, joleave);
		}
	}
	private void _result_guildLeave(boolean timeout, Param result, Param context){
		boolean results = Utils.getParamValue(result, "results", false);
		long guildId = Utils.getParamValue(context, "guildId", 0L);
		long humanId = Utils.getParamValue(context, "humanId", 0L);
		if(timeout || !results){
			// 添加一个待办事项：退出公会
			JSONObject joleave = new JSONObject();
			joleave.put(GuildIdKey, guildId);
			HumanManager.inst().saveOneBacklog(humanId, BacklogType.GuildLeave, joleave);
		}
	}
	
	/**
	 * 对申请入会玩家处理(1)
	 * @param humanObj
	 * @param reply 1同意，2拒绝
	 */
	public void _msg_CSApplyReply(HumanObject humanObj, long replyId, int reply) {
		if (reply != 1 && reply != 2) {// 不是 1同意，2拒绝
			return;
		}
		long guildId = humanObj.getHuman().getGuildId();
		if (reply == 1) {// 同意
			long humanId = humanObj.getHumanId();
			GuildServiceProxy pxy = GuildServiceProxy.newInstance();
			pxy.replyApply(humanId,guildId, replyId);
			pxy.listenResult(this::_result_msg_CSApplyReply, HumanObject.paramKey, humanObj, "replyId", replyId);
		} else {
			GuildServiceProxy pxy = GuildServiceProxy.newInstance();
			pxy.removeApplyInfo(guildId, replyId);
			pxy.listenResult(this::_result_msg_SCApplyReply, HumanObject.paramKey, humanObj, "replyId", replyId);
		}
	}
	

	/**
	 * 对申请入会玩家处理(2)
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSApplyReply(Param results, Param context) {	
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(results, "guild", null);
		if (guild == null) {
			Log.game.error("guild={}", guild);
			return;
		}
		long replyId = Utils.getParamValue(context, "replyId", 0L);
		if (replyId == 0) {
			Log.game.error("humanId=0");
			return;
		}
		boolean result = Utils.getParamValue(results, "result", false);
		if(result){//可加入
			humanObj.sendSysMsg(503117);// 操作成功
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.getInfo(replyId);
			prx.listenResult(this::_result_findOnlineHuman, "guild", guild, HumanObject.paramKey, humanObj, "replyId", replyId);
		}
		sendMsgSCApplyReply(humanObj, result, replyId);
		
	}

	/**
	 * 判断是否已经加入该公会
	 * @param guild
	 * @param humanId
	 * @return
	 */
	public boolean isYetJoin(Guild guild, long humanId){
		if(null != guild){
			List<GuildData> listGuild = GuildData.jsonToList(guild.getGuildHuman());
			for (GuildData guildBean : listGuild) {
				if (guildBean.id == humanId) {// 玩家已经加入该公会
					return true;//更新该玩家数据
				}
			}
		}
		return false;
	} 
	
	/**
	 * 对申请入会玩家处理 返回
	 * @param results
	 * @param context
	 */
	public void _result_msg_SCApplyReply(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		boolean repeat = Utils.getParamValue(results, "repeat", false);
		long replyId = Utils.getParamValue(context, "replyId", -1L);
		sendMsgSCApplyReply(humanObj, repeat, replyId);
		humanObj.sendSysMsg(503117);// 操作成功
	}

	private void sendMsgSCApplyReply(HumanObject humanObj, boolean repeat, long humanId) {
		SCApplyReplyResult.Builder msg = SCApplyReplyResult.newBuilder();
		msg.setResult(repeat);
		msg.setHumanId(humanId);
		humanObj.sendMsg(msg);
	}

	/**
	 * 查询玩家是否在线
	 * @param results
	 * @param context
	 */
	public void _result_findOnlineHuman(Param results, Param context) {	
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj == null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(context, "guild", null);
		long replyId = Utils.getParamValue(context, "replyId", 0L);
		HumanGlobalInfo humanInfo = results.get();
		if (null == guild || null == humanObj || 0 == replyId) {
			Log.game.error("humanObj=={}, guild={}, humanId={}", humanObj, guild, replyId);
			return;
		}
		if(null != humanInfo){// 在线
			HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(humanInfo.nodeId,humanInfo.portId, humanInfo.id);
			prxHumanObj.guildJoin(true, guild.getId(), guild.getGuildLevel(), guild.getGuildName(), 1);// 通知加入了公会
			prxHumanObj.listenResult(this:: _result_result_findOnlineHuman, "guildId", guild.getId(), "replyId", replyId
					,"guildLv",guild.getGuildLevel(), "guildName",guild.getGuildName());
		} else{
			// 添加一个待办事项：加入公会
			JSONObject join = new JSONObject();
			join.put(GuildIdKey, guild.getId());
			join.put(Human.K.GuildLevel, guild.getGuildLevel());
			join.put(GuildNameKey, guild.getGuildName());
			HumanManager.inst().saveOneBacklog(replyId, BacklogType.GuildJoin, join);
		}
	}
	
	public void _result_result_findOnlineHuman(boolean timeout, Param result, Param context){
		boolean results = Utils.getParamValue(result, "results", false);
		long guildId = Utils.getParamValue(context, "guildId", 0L);
		long replyId = Utils.getParamValue(context, "replyId", 0L);
		int guildLv = Utils.getParamValue(context, "guildLv", 0);
		String guildName = Utils.getParamValue(context, "guildName", "");
		if(timeout || !results){
			// 添加一个待办事项：加入公会
			JSONObject join = new JSONObject();
			join.put(GuildIdKey, guildId);
			join.put(Human.K.GuildLevel, guildLv);
			join.put(GuildManager.inst().GuildNameKey, guildName);
			HumanManager.inst().saveOneBacklog(replyId, BacklogType.GuildJoin, join);
		}
	}
	
	/**
	 * 清空申请列表
	 * @param humanObj
	 */
	public void _msg_CSApplyClear(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		long belongGuildId = human.getGuildId();
		if (belongGuildId <= 0) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildInfo(belongGuildId, human);
		pxy.listenResult(this::_result_msg_CSApplyClear, "humanId", human.getId(),HumanObject.paramKey, humanObj);
	}

	/**
	 * 会长或副会长清空公会申请信息
	 * @param results
	 * @param context
	 */
	private void _result_msg_CSApplyClear(Param results, Param context) {
		Guild guild = Utils.getParamValue(results, "guild", null);
		long humanId = Utils.getParamValue(context, "humanId", 0L);
		HumanObject humanObj = context.get(HumanObject.paramKey);
		if(humanObj == null){
			Log.logGameObjectNull();
			return;
		}
		if (guild == null || humanId == 0) {
			Log.game.error("guild={}, humanId={}", guild, humanId);
			return;
		}
		int post = getGuildPost(guild.getGuildPostMember(), humanId);
		if (guild.getGuildLeaderId() != humanId && post != 2) {// 不是会长或者副会长就不能进行操作
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.removeApplyInfo(guild);
		pxy.listenResult(this::_result_removeApplyInfo, HumanObject.paramKey, humanObj);
	}
	private void _result_removeApplyInfo(Param results, Param context) {
		SCApplyInfoResult msg = results.get("msg");
		HumanObject humanObj = context.get(HumanObject.paramKey);
		if(humanObj == null){
			Log.logGameObjectNull();
			return;
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 设置会员职位
	 * @param humanObj
	 * @param humanId
	 * @param post
	 */
	public void _msg_CSGuildPostSet(HumanObject humanObj, long humanId, int post) {
		if (humanObj.getHumanId() == humanId) {
			return;
		}
		Human human = humanObj.getHuman();
		long guildId = human.getGuildId();
		if (guildId <= 0) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildInfo(guildId, human);
		pxy.listenResult(this::_result_msg_CSGuildPostSet, HumanObject.paramKey, humanObj, "humanId", humanId, "post", post);
	}

	/**
	 * 只有会长可以设置职位
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildPostSet(Param results, Param context) {
		Guild guild = Utils.getParamValue(results, "guild", null);
		if (guild == null) {
			Log.game.error("guild is null");
			return;
		}

		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		if (guild.getGuildLeaderId() != humanObj.getHumanId()) {
			return;
		}
		long humanId = Utils.getParamValue(context, "humanId", 0L);
		if (humanId == 0) {
			Log.game.error("humanId=0");
			return;
		}

		GuildData bean = GuildData.get(guild.getGuildHuman(), humanId);
		if (bean == null) {
			return;
		}
		int post = Utils.getParamValue(context, "post", -1);
		if (post < 0) {
			Log.game.error("post<0");
			return;
		}
		boolean isCDR = false;
		switch (post) {
			case 1 :// 会长
				if (bean.timeLogout != 0) {
					int day = Utils.getDaysBetween(bean.timeLogout, Port.getTime());
					int offlineTime = ParamManager.offlineTimeLimit;
					if (day >= offlineTime) {
						humanObj.sendSysMsg(530111);// 该玩家离线时间太长，不能担任会长
						return;
					}
				}
				isCDR = true;// 如果设置职位为1 说明是转让会长
				break;
			case 2 :// 副会长
				boolean ret = isPostEnough(humanObj, guild, post);
				if (!ret) {
					return;
				}
				break;
			default :
				break;
		}
		bean.post = post;
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.updateGuildPostMember(guild, bean, isCDR);
		pxy.listenResult(this::_result_msg_SCGuildPostSetResult, HumanObject.paramKey, humanObj, "humanId", humanId, "isCDR",
				isCDR);

	}

	/**
	 * 判断是否还有空位
	 * @param humanObj
	 * @param guild
	 * @param post
	 * @return
	 */
	private boolean isPostEnough(HumanObject humanObj, Guild guild, int post) {
		if (guild == null) {
			return false;
		}
		int num = 0;
		JSONObject jo = Utils.toJSONObject(guild.getGuildPostMember());
		if (!jo.isEmpty()) {
			for (String key : jo.keySet()) {
				int value = jo.getIntValue(key);
				if (value == post) {
					num++;
				}
			}
		}
		ConfGuildLevel conf = ConfGuildLevel.get(guild.getGuildLevel());
		if (conf == null) {
			Log.table.error("ConfGuildLevel 配表错误，no find sn={}", guild.getGuildLevel());
			return false;
		}
		if (num >= conf.viceChairmanNum) {
//			humanObj.sendSysMsg(530113, "B", conf.viceChairmanNum);// 530113该职位只能允许存在{b}个
			humanObj.sendSysMsg(530113);
			return false;
		}
		return true;
	}

	/**
	 * 返回职位设置
	 * @param results
	 * @param context
	 */
	private void _result_msg_SCGuildPostSetResult(Param results, Param context) {
		boolean result = Utils.getParamValue(results, "result", false);
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		long humanId = Utils.getParamValue(context, "humanId", 0L);
		boolean isCDR = Utils.getParamValue(context, "isCDR", false);
		if (humanObj == null || humanId == 0) {
			Log.game.error("humanObj={}, humanId={}", humanObj, humanId);
			return;
		}
		int post = Utils.getParamValue(results, "post", 0);
		SCGuildPostSetResult.Builder msg = SCGuildPostSetResult.newBuilder();
		msg.setResult(result);
		msg.setPost(post);
		if (result) {
			msg.setHumanId(humanId);
			if (isCDR) {
				humanObj.getHuman().setGuildCDR(false);// 原会长辞职
				humanObj.sendSysMsg(530109);// 公会转让成功！
			}else {
				humanObj.sendSysMsg(0);// 操作成功！
			}
			HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
			pxy.getInfo(humanId);
			pxy.listenResult(this::_result_humanGuildPostUp,"msg",msg,"humanId", humanId);
		}
		humanObj.sendMsg(msg);
	}
	/**
	 * 通知在线玩家公会的职位变更
	 * @param results
	 * @param context
	 */
	private void _result_humanGuildPostUp(Param results, Param context){
		HumanGlobalInfo humanInfo = results.get();
		SCGuildPostSetResult.Builder msg = Utils.getParamValue(context, "msg", null);
		if(msg != null){
			// 通知给玩家简易排行发生变化
			HumanGlobalManager.inst().sendMsg(humanInfo.connPoint, msg.build());	
		}
		long humanId = Utils.getParamValue(context, "humanId", 0L);
		if(humanId <= 0){
			Log.game.error("humanId={}", humanId);
			return;
		}
		if (humanInfo == null) {// 若玩家不在线
			JSONObject joCDR = new JSONObject();
			joCDR.put(Human.K.GuildCDR,true);// 设置新会长
			HumanManager.inst().saveOneBacklog(humanId, BacklogType.GuildCDR, joCDR);
			return;
		}else{
			HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(humanInfo.nodeId, humanInfo.portId, humanInfo.id);
			prxHumanObj.guildCDRUp(true);// 通知玩家成为会长
			prxHumanObj.listenResult(this::_result_result_humanGuildPostUp, "humanId", humanId);
		}
	}
	
	public void _result_result_humanGuildPostUp(boolean timeout, Param result, Param context){
		long humanId = Utils.getParamValue(context, "humanId", 0L);
		if(humanId <= 0){
			Log.game.error("humanId={]", humanId);
			return;
		}
		if(timeout){
			JSONObject joCDR = new JSONObject();
			joCDR.put(Human.K.GuildCDR,true);// 设置新会长
			HumanManager.inst().saveOneBacklog(humanId, BacklogType.GuildCDR, joCDR);
		}
		
	}

	/**
	 * 更新公会在线玩家信息
	 * @param human
	 */
	private void updateGuild(Human human, long guildId) {
		if (guildId <= 0) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.updateGuildHuman(guildId, human, 1, Port.getTime());
	}

	/**
	 * 玩家退出登录 更新会员离线时间
	 */
	@Listener(EventKey.HumanLogout)
	public void humanLogOut(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("humanLogOut humanObj is null");
			return;
		}
		Human human = humanObj.getHuman();
		long guildId = human.getGuildId();
		if (guildId <= 0) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.updateGuildHuman(guildId, human, 0, Port.getTime());
	}

	/**
	 * 获取公会献祭记录
	 * @param humanObj
	 */
	public void _msg_CSGuildImmoLog(HumanObject humanObj){
		long id = humanObj.getHuman().getGuildId();
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildImmo(id);
		pxy.listenResult(this::_result_msg_CSGuildImmoLog, HumanObject.paramKey, humanObj);
	}
	
	/**
	 * 查询公会获取公会献祭记录
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildImmoLog(Param results, Param context) {	
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		List<GuildImmoLog> guildImmo = Utils.getParamValue(results, "guildImmo", null);
		String guildHuman = Utils.getParamValue(results, "guildHuman", null);
		if(guildImmo == null || Utils.isEmptyJSONString(guildHuman)){
			SCGuildImmoLog.Builder msg = SCGuildImmoLog.newBuilder();
			humanObj.sendMsg(msg);
			Log.game.info("guildImmo={}, guildHuman={}", guildImmo, guildHuman);
			return;
		}
		sendMsgSCGuildImmoLog(humanObj,guildImmo, guildHuman);
	}
	

	/**
	 * 发送公会献祭记录
	 * @param humanObj
	 */
	public void sendMsgSCGuildImmoLog(HumanObject humanObj,List<GuildImmoLog> guildImmo, String guildHuman){
		SCGuildImmoLog.Builder msg = SCGuildImmoLog.newBuilder();
		if(null != guildImmo){
			Iterator<GuildImmoLog> it = guildImmo.iterator();
			while (it.hasNext()) {
				GuildImmoLog bean = it.next();
				if(null != bean){
					DGuildImmo.Builder log = DGuildImmo.newBuilder();
					log.setTime(bean.getTime());
					GuildData humanGuildData = GuildData.get(guildHuman, bean.getHumanId());
					if(humanGuildData != null){
						log.setName(humanGuildData.name);
					} else {
						log.setName(bean.getHumanName());
					}
					log.setType(EGuildImmoType.valueOf(bean.getImmotype()));
					log.setDareSn(bean.getContent());
					log.setHandle(ELogHandleType.valueOf(bean.getHandle()));
					log.setLogType(ELogType.valueOf(bean.getLogTypeKey()));
					log.setAptitude(bean.getAptitudeKey());
					msg.addLog(log);
				}
			}
		}
		humanObj.sendMsg(msg);
	}


	/**
	 * 帮贡
	 * @param humanObj
	 * @param type 0 普通献祭 1 高级献祭
	 */
	public void _msg_CSGuildImmo(HumanObject humanObj, int type) {

		Human human = humanObj.getHuman();
		long guildId = human.getGuildId();
		if (guildId <= 0) {
			return;
		}
		
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildInfo(guildId, human);
		pxy.listenResult(this::_result_msg_CSGuildImmo, HumanObject.paramKey, humanObj, "itemSn", 1, "itemNum", 1, "type", type);
	}
	
	/**
	 * 发送公会献祭变化
	 * @param humanObj
	 */
	private void sendSCGuildImmoInfoResult(HumanObject humanObj){
//		SCGuildImmoInfoResult.Builder msg = SCGuildImmoInfoResult.newBuilder();
//		msg.setIsGuildImmo(humanObj.getHumanExt().isGuildImmo());
//		int currentPersion = GuildManager.inst().getGuildImmoNum(guild);
//		dGuildInfo.setCurrentPersion(currentPersion);
//		humanObj.sendMsg(msg);
	}

	/**
	 * 扣除材料，增加贡献和进度及建设
	 * @param results
	 * @param context
	 */
	public void _result_msg_CSGuildImmo(Param results, Param context) {	
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(results, "guild", null);
		int itemSn = Utils.getParamValue(context, "itemSn", 0);
		int itemNum = Utils.getParamValue(context, "itemNum", 0);
		int type = Utils.getParamValue(context, "type", 0);
		if (humanObj == null || guild == null) {
			Log.game.error("humanObj={}, guild={}", humanObj, guild);
			return;
		}
		if(type == 0){
			Log.game.error("type=0");
			return;
		}
		if (guild != null) {
			long humanId = humanObj.getHumanId();
			GuildData humanGuildData = GuildData.get(guild.getGuildHuman(), humanId);
			if(humanGuildData == null || humanGuildData.type > 0){
				// 今日已经献祭过
				Log.guild.info("humanGuildData is null or 今日已经献祭过");
				return;
			}
			
			ConfGuildPray  conf = ConfGuildPray.get(type);
			if(conf == null) {
				Log.table.error("can't find sn={}",type);
				return;
			}
			
			if(!RewardHelper.checkAndConsume(humanObj, conf.costType, conf.costNum,LogSysModType.GuildImmo)) {
				return;
			}
			
			
			Human human = humanObj.getHuman();

			GuildServiceProxy pxy = GuildServiceProxy.newInstance();
			
			int contributePlan = conf.fete;
			int contributeErect = conf.build;
			long guildContribute = conf.persion;
			human.setContribute(human.getContribute()+guildContribute);
			RewardHelper.reward(humanObj, EMoneyType.guildCoin_VALUE,conf.persion, LogSysModType.GuildImmo);
			//记录一下离线时间
			
													//增加进度   //增加经验			
			pxy.guildImmo(guild, guildContribute, human.getId(), type,contributePlan, contributeErect);
			pxy.listenResult(this::_result_msg_SCGuildImmoResult, HumanObject.paramKey, humanObj);
		}
	}
	
	/**
	 * 返回献祭
	 * @param results
	 * @param context
	 */
	public void _result_msg_SCGuildImmoResult(Param results, Param context) {

		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull(HumanObject.paramKey);
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Human human = humanObj.getHuman();
		boolean ret = Utils.getParamValue(results, "result", false);
		boolean isUp = Utils.getParamValue(results, "isUp", false);
		Guild guild = Utils.getParamValue(results, "guild", null);
		if (ret) {
			humanObj.sendMsg(createGuildLvExpMsg(humanObj, guild));
			_msg_CSGuildImmoLog(humanObj);//发送公会献祭信息
			if (isUp) {
				human.setGuildLevel(guild.getGuildLevel());//更新所属公会等级
				humanGuildLvUp(guild, human.getId());//更新在线玩家所属公会等级
				// 判断是否需要全服公告
				ConfGuildLevel conf = ConfGuildLevel.get(guild.getGuildLevel());
				
			}
			
		}
		
		
		
		SCGuildImmoResult.Builder msg = SCGuildImmoResult.newBuilder();
		msg.setResult(ret);
		msg.setGuildLiveness(guild.getGuildPlan());//工会总的献祭次数
		msg.setCurrentPersion(guild.getGuildImmoNum());
		msg.setGuildLevel(guild.getGuildLevel());
		msg.setGuildExp(guild.getGuildExp());
		humanObj.sendMsg(msg);
		sendSCGuildImmoInfoResult(humanObj);
	}
	
	
	
	/**
	 * 更新人物公会等级
	 * @param guild
	 */
	private void humanGuildLvUp(Guild guild,long id){
		List<GuildData> guildListbean = GuildData.jsonToList(guild.getGuildHuman());
		if (guildListbean.isEmpty()) {
			return;
		}
		HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
		for (GuildData bean : guildListbean) {
			if (bean == null)
				continue;
			if(bean.id == id)
				continue;
			pxy.getInfo(bean.id);
			pxy.listenResult(this::_result_humanGuildLvUp,"guildLevel",guild.getGuildLevel());
		}
		
	}
	
	/**
	 * 查询在线的公会成员
	 * @param results
	 * @param context
	 */
	private void _result_humanGuildLvUp(Param results, Param context) {
		HumanGlobalInfo humanInfo = results.get();
		if (humanInfo == null) {// 若玩家不在线
			return;
		}
		int guildLevel = Utils.getParamValue(context, "guildLevel", 0);
		HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(humanInfo.nodeId, humanInfo.portId, humanInfo.id);
//		prxHumanObj.setHumanGuildLvUp(guildLevel);
	}

	/**
	 * 公会贡献经验及个人贡献等等
	 * @param humanObj
	 * @param guild
	 * @return
	 */
	public SCGuildLvExp.Builder createGuildLvExpMsg(HumanObject humanObj, Guild guild) {
		Human human = humanObj.getHuman();
		SCGuildLvExp.Builder msg = SCGuildLvExp.newBuilder();
		if (guild != null) {
			msg.setGuildId(guild.getId()); // 公会id
			msg.setGuildLevel(guild.getGuildLevel()); // 公会等级
			msg.setGuildExp(guild.getGuildExp()); // 公会经验
			msg.setGuildActive(guild.getGuildLiveness()); // 公会活跃
		}
		msg.setGuildContribute(human.getContribute()); // 玩家对公会的贡献
		return msg;
	}

	/**
	 * 修改公会会长名字
	 * @param newName
	 */
	public void changeChairmanName(Human human, String newName) {
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.changeChairmanName(human, human.getGuildId(), newName);
	}
	
	
	
	
	
	
	/**
	 * 监听玩家名字改变事件
	 * @param param
	 */
	@Listener(EventKey.HumanNameChange)
	public void _listener_HumanNameChange(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, HumanObject.paramKey, null);
		if (humanObj == null) {
			return;
		}
		Human human = humanObj.getHuman();
		long guildId = human.getGuildId();
		if (guildId > 0) {
			updateGuild(human, guildId);
		}
	}
	/**
	 * 是否建设过
	 * @param humanObj
	 */
	public void isBuild(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		long belongGuild = human.getGuildId();
		if (belongGuild <= 0) {
			return;
		}
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildInfo(belongGuild, human);
		pxy.listenResult(this::_result_isBuild,"humanObj", humanObj);
		
	}
	public void _result_isBuild(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		Human human = humanObj.getHuman();
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		long humanId = humanObj.getHumanId();
		Guild guild = Utils.getParamValue(results, "guild", null);
		if(guild == null) {
			Log.guild.info("guild is null");
			return;
		}
		GuildData bean = GuildData.get(guild.getGuildHuman(), humanId);
		if(bean == null) {
			Log.guild.info("bean is null");
			return;
		}
		boolean isBuild = false;
		int build = bean.type;
		isBuild = build>0 ?true:false;
		int num = build>0 ?1:0;
		SCGuildImmoInfoResult.Builder msg = SCGuildImmoInfoResult.newBuilder();
		msg.setIsGuildImmo(isBuild);
		msg.addAllImmoSnList(Utils.strToIntList(human.getGuildImmoSnList()));
		humanObj.sendMsg(msg);
	}
	/**
	 * 取消工会申请
	 * @param humanObj
	 * @param guildId
	 */
	public void cancleJoin(HumanObject humanObj, long guildId) {
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		long humanID = humanObj.getHumanId();
		pxy.removeApplyInfo(guildId,humanID);
		pxy.listenResult(this::_result_cancleJoin,"humanObj", humanObj);
	}
	private void _result_cancleJoin(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		boolean success =  results.get("success");
		SCGuildCancleJoinResult.Builder msg = SCGuildCancleJoinResult.newBuilder();
		msg.setResult(success);
		humanObj.sendMsg(msg);
		
	}
	
	/**
	 * 领取进度宝箱
	 * @param humanObj
	 */
	public void _msg_CSGuildImmoGiftBag(HumanObject humanObj, int index){
		
		long guildId = humanObj.getHuman().getGuildId();
		GuildServiceProxy pxy = GuildServiceProxy.newInstance();
		pxy.getGuildInfo(guildId, humanObj.getHuman());
		pxy.listenResult(this::_result_msg_CSGuildImmoGiftBag,HumanObject.paramKey, humanObj, "index", index);	
	}
	
	public void _result_msg_CSGuildImmoGiftBag(Param results, Param context){	
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(results, "guild", null);
		int index = Utils.getParamValue(context, "index", 0);
		if(guild == null ){
			return;
		}
		//获取工会等级
		ConfGuildLevel conf = ConfGuildLevel.get(guild.getGuildLevel());
		if(conf == null){
			Log.table.error("===ConfGuildLevel 配表错误，sn={}", guild.getGuildLevel());
			return;
		}
		// 进度不够，没达到要求
		if(guild.getGuildPlan() < conf.progessVal[index]){
			return;
		}
		Human human = humanObj.getHuman();
		List<Integer> list = Utils.strToIntList(human.getGuildImmoSnList());
		if(list.contains(index)){
			Log.guild.info("已经领取 index={}",index);
			return;
		}

		// 给礼包奖励
		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();				
		itemProduce.addAll(ProduceManager.inst().produceItem(conf.progessReward[index]));
		// 给物品
		RewardHelper.reward(humanObj, itemProduce, LogSysModType.GuildImmo);
		list.add(index);
		human.setGuildImmoSnList(Utils.ListIntegerToStr(list));
		// 返回消息
		SCGuildImmoGiftBag.Builder msg = SCGuildImmoGiftBag.newBuilder();
		msg.addAllSn(list);
		humanObj.sendMsg(msg);
		
	}
	
	/**
	 * 获取工会可容纳最大人数
	 */
	public int getGuildMaxNum(int lv) {
		int alowNum = 30;
		ConfGuildLevel conf = ConfGuildLevel.get(lv);
		if(conf == null) {
			Log.table.error("can't find sn ={}",lv);
			return alowNum;
		}
		return conf.maxNum;
	}
	
	public int getGuildImmoNum(Guild guild) {
		int count = 0;
		//公会成员总信息
		List<GuildData> guildListbean = GuildData.jsonToList(guild.getGuildHuman());
		for(GuildData data:guildListbean) {
			if(data.contribute>0) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 在线跨天推送献祭进度
	 * @param param
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		long guildId = humanObj.getHuman().getGuildId();
		if( guildId <= 0) {
			return;
		}
		GuildServiceProxy prx = GuildServiceProxy.newInstance();
		prx.getGuildInfo(guildId, humanObj.getHuman());
		prx.listenResult(this::_result__listener_ResetDailyHour, HumanObject.paramKey, humanObj);
	}
	
	private void _result__listener_ResetDailyHour(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if(humanObj==null){
			Log.logGameObjectNull();
			return;
		}
		if(!humanObj.checkHumanSwitchState(results, context, true)){
			return;
		}
		Guild guild = Utils.getParamValue(results, "guild", null);
		if(guild == null) {
			return;
		}
		SCGuildImmoResult.Builder msg = SCGuildImmoResult.newBuilder();
		msg.setResult(true);
		msg.setGuildLiveness(guild.getGuildPlan());//工会总的献祭次数
		msg.setCurrentPersion(guild.getGuildImmoNum());
		humanObj.sendMsg(msg);
	}

	public void _msg_CSGuildSkillList(HumanObject humanObj) {
		String json = humanObj.getHuman().getGuildSkills();
		List<GuildSkillData> list = GuildSkillData.jsonToList(json);

		SCGuildSkillList.Builder msg = SCGuildSkillList.newBuilder();
		for (GuildSkillData sd : list) {
			DGuildSkill.Builder one = DGuildSkill.newBuilder();
			one.setSn(sd.getSn());
			msg.addSkills(one);
		}
		humanObj.sendMsg(msg);
	}

	private GuildSkillData findSkill(List<GuildSkillData> list, int id) {
		for (GuildSkillData sd : list) {
			if(sd.id == id)	 {
				return sd;
			}
		}
		return null;
	}

	public void addGuildSkillProp(HumanObject humanObj) {
        UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.GuildSkill);
    }

    /**
     * @param humanObj
     * @return 帮会技能属性
     */
    public PropCalcCommon calcGuildSkillProps(HumanObject humanObj) {
    	if (humanObj.getHuman().getGuildId() == 0)
    		return new PropCalcCommon();
    	String json = humanObj.getHuman().getGuildSkills();
        List<GuildSkillData> list = GuildSkillData.jsonToList(json);
    	// 属性计算
    	PropCalcCommon propCalc = new PropCalcCommon();
    	for (GuildSkillData sd : list) {
    		int sn = sd.getSn();
    		ConfGuildSkill conf = ConfGuildSkill.get(sn);
    		if (conf == null) {
    			continue;
    		}
    		propCalc.add(conf.attribute, conf.attributeValue);
    	}
    	return propCalc;
    }

	public void _msg_CSGuildSkillUpgrade(HumanObject humanObj, int sn) {
	    Human human = humanObj.getHuman();

	    // check guild
        long guildId = human.getGuildId();
        if (guildId <= 0) {
            return;
        }

        // check guild level
        GuildServiceProxy pxy = GuildServiceProxy.newInstance();
        pxy.getGuildLevel(guildId);
        pxy.listenResult(this::_result_GuildSkillUpgradeGetGuildLevel, HumanObject.paramKey, humanObj, "sn", sn);
	}

    public void _result_GuildSkillUpgradeGetGuildLevel(Param results, Param context) {
        HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
        if(humanObj==null){
            Log.logGameObjectNull();
            return;
        }
        if(!humanObj.checkHumanSwitchState(results, context, true)){
            return;
        }
        int guildLevel = Utils.getParamValue(results, "guildLevel", 0);
        int sn = Utils.getParamValue(context, "sn", 0);
        int id = GuildSkillData.getIdBySn(sn);

        Human human = humanObj.getHuman();
        // check guild
        long guildId = human.getGuildId();
        if (guildId <= 0) {
            return;
        }

        ConfGuildSkill conf = null;

        // check exist
        String json = human.getGuildSkills();
        List<GuildSkillData> list = GuildSkillData.jsonToList(json);
        GuildSkillData sd = findSkill(list, id);
        if (sd == null) {
            // level 0
            sn = GuildSkillData.calcSn(id, 0);
            conf = ConfGuildSkill.get(sn);
            if (conf == null) {
                return;
            }
            if (conf.level != 0) {
                return;
            }
            sd = new GuildSkillData(id, 0);
            list.add(sd);
        } else {
            // get current level
            conf = ConfGuildSkill.get(sn);
            if (conf == null) {
                return;
            }
        }

        // check guild level
        if (guildLevel < conf.skillQm) {
            return;
        }

        // check consume
        if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.coin_VALUE , conf.useCost, LogSysModType.GuildSkillUpgrade)) {
            return;
        }
        if (!RewardHelper.checkAndConsume(humanObj, conf.costItem, conf.itemNumber, LogSysModType.GuildSkillUpgrade)) {
            return;
        }

        //
        sd.level++;
        json = GuildSkillData.listToJson(list);
        human.setGuildSkills(json);

        // effect
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.GuildSkill);


        int nextSn = GuildSkillData.calcSn(id, sd.level);
        SCGuildSkillUpgrade.Builder msg = SCGuildSkillUpgrade.newBuilder();
        msg.setResult(true);
        msg.setOldSn(sn);
        msg.setNewSn(nextSn);
        humanObj.sendMsg(msg);
    }
}
