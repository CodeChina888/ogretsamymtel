package game.worldsrv.guild;

import game.msg.Define;
import game.msg.Define.EGuildInstResetType;
import game.msg.Define.ELogHandleType;
import game.msg.Define.ELogType;
import game.msg.MsgGuild.SCApplyInfoResult;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfGuildInstStage;
import game.worldsrv.config.ConfGuildLevel;
import game.worldsrv.config.ConfGuildOpenLv;
import game.worldsrv.config.ConfInstMonster;
import game.worldsrv.config.ConfParam;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.entity.Guild;
import game.worldsrv.entity.GuildApply;
import game.worldsrv.entity.GuildImmoLog;
import game.worldsrv.entity.Human;
import game.worldsrv.enumType.FightPropName;
import game.worldsrv.fightParam.GuildInstParam;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalc;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;

@DistrClass(servId = D.SERV_GUILD, importClass = { Human.class, Guild.class, GuildData.class, List.class, EGuildInstResetType.class })
public class GuildService extends GameServiceBase {

	private static Set<String> guildNameSet = new HashSet<String>();// 所有的公会名字
	/** Map<工会ID,工会> */
	private static Map<Long, Guild> guildMap = new HashMap<>();// 所有公会
	/** map<主角id,公会id> **/
	private static Map<Long, Long> humanGuildMap = new HashMap<>();
	private static final int countPerFind = 1000; // 每次查询1000
	// 所有的帮贡信息 工会id,
	private static Map<Long, List<GuildImmoLog>> mapGuildImmo = new HashMap<>();
	// Map<humanId,List<所申请的工会id>>
	Map<Long, List<GuildApply>> humanApplyMap = new HashMap<>();
	// 所有公会副本
    Map<Long, GuildInstData> guildInst = new HashMap<>();
    Map<Long, GuildInstStageRewardData> guildReward = new HashMap<>();
    Map<String, Long> guildRewardLock = new HashMap<>();
    private TickTimer ttClose = new TickTimer(); // 结束定时器

	public GuildService(GamePort port) {
		super(port);
	}

	/**
	 * 初始公会信息
	 */
	@Override
	protected void init() {

		List<Record> records;
		DB dbPrx = DB.newInstance(Guild.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numAll = result.get();// 总数量

		Log.game.info("GuildService.init() : start load tableName={}, numAll={}", Guild.tableName, numAll);

		long time = Port.getTime();
		int numLoop = numAll / countPerFind;

		// 分页查询
		for (int i = 0; i <= numLoop; i++) {
			dbPrx.findBy(false, i * countPerFind, countPerFind);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null)
				continue;
			// 加载全部公会
			for (Record r : records) {
				Guild guild = new Guild(r);
				if (guild != null) {
					guildNameSet.add(guild.getGuildName());// 加载所有公会名字
					String guildHuman = GuildData.initGuildHumanOnlineStatus(guild.getGuildHuman());// 重置所有会员在线状态
					guild.setGuildHuman(guildHuman);
					updateGuildInfo(guild);// 加载所有的公会
					humanGuildMap.put(guild.getGuildLeaderId(), guild.getId());
					// mapGuildImmo.put(guild.getId(),
					// GuildImmoLog.getAll(guild.getGuildImmo()));//保存献祭信息

					long fourTime = Utils.getTimeHourOfToday(ParamManager.dailyHourReset);// 获取当天的重置时间
					long updateTime = guild.getGuildUpdateTime();// 获取上次更新时间
					if (port.getTimeCurrent() >= fourTime) {// 判断是否过来今天的重置时间
						if (!Utils.isSameDay(port.getTimeCurrent(), updateTime)) {// 判断两个时间是否是同一天
							detectionGuildInfo(guild);// 执行凌晨重置时间检测更新
						}
					}
				}
			}
		}
		// 加载人物申请信息表
		inithumanApplyMap();
		// 加载工会日志 mapGuildImmo
		initImmoLog();
		initGuildInst();

		Log.game.info("GuildService.init() : finish load tableName={}, costTime={}", Guild.tableName,
				Port.getTime() - time);
	}

	private void initImmoLog() {
		List<Record> records;
		DB dbPrx = DB.newInstance(GuildImmoLog.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numAll = result.get();// 总数量

		int numLoop = numAll / countPerFind;

		// 分页查询
		for (int i = 0; i <= numLoop; i++) {
			dbPrx.findBy(false, i * countPerFind, countPerFind);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null)
				continue;
			// 加载全部公会
			for (Record r : records) {
				GuildImmoLog gap = new GuildImmoLog(r);
				long humanId = gap.getHumanId();
				long guiId = gap.getGuildId();
				List<GuildImmoLog> list = mapGuildImmo.get(humanId);
				if (list == null) {
					list = new ArrayList<>();
					mapGuildImmo.put(humanId, list);
				}
				list.add(gap);
			}
		}
	}

	private void inithumanApplyMap() {
		List<Record> records;
		DB dbPrx = DB.newInstance(GuildApply.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numAll = result.get();// 总数量

		int numLoop = numAll / countPerFind;

		// 分页查询
		for (int i = 0; i <= numLoop; i++) {
			dbPrx.findBy(false, i * countPerFind, countPerFind);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null)
				continue;
			// 加载全部公会
			for (Record r : records) {
				GuildApply gap = new GuildApply(r);
				long humanId = gap.getHumanId();
				long guiId = gap.getGuildId();
				List<GuildApply> list = humanApplyMap.get(humanId);
				if (list == null) {
					list = new ArrayList<>();
					humanApplyMap.put(humanId, list);
				}
				list.add(gap);
			}
		}
	}

	@DistrMethod
	public void getMyApplyList(long humanId) {
		List<Long> applyList = getApplyLists(humanId);
		port.returns("applyList", applyList);
	}

	private List<Long> getApplyLists(long humanId) {
		List<GuildApply> guildList = humanApplyMap.get(humanId);
		if (guildList == null) {
			guildList = new ArrayList<>();
		}
		List<Long> applyList = new ArrayList<>();
		for (GuildApply g : guildList) {
			applyList.add(g.getGuildId());
		}
		return applyList;
	}

	/**
	 * 每个service预留空方法
	 */
	@DistrMethod
	public void update(Object... objs) {

	}

    //Override
    //public void pulseOverride() {
    //    if (ttClose.isPeriod(Port.getTime())) {
    //        ttClose.stop();// 关闭定时器
    //        // TODO
    //    }
    //}
    /**
     * 每隔一分钟执行一次
     */
    @ScheduleMethod(Utils.cron_Day_Min)
    public void _cron_Day_Min() {
        // lock的超时移除, 5分钟
        long now = Port.getTime();
        List<String> rmLock = new ArrayList<>();
        for (Map.Entry<String, Long> e : guildRewardLock.entrySet()) {
            if (now - e.getValue() > Time.MIN * 5) {
                rmLock.add(e.getKey());
            }
        }
        for (String key : rmLock) {
            guildRewardLock.remove(key);
        }
    }
	/**
	 * 每个整点执行一次
	 */
	@ScheduleMethod(Utils.cron_Day_Hour)
	public void _cron_Day_Hour() {
	    long now = Port.getTime();
		int hour = Utils.getHourOfDay(now);
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置
			detectionGuildInfo();// 更新公会信息
            checkResetGuildInst(true);
		}
	}

	/**
	 * 4点时检测公会信息
	 */
	private void detectionGuildInfo() {
		for (Long id : guildMap.keySet()) {
			Guild guild = guildMap.get(id);
			if (guild == null) {
				continue;
			}
			detectionGuildInfo(guild);
		}
	}

	/**
	 * 加载时检查重置公会信息
	 * 
	 * @param guild
	 */
	private void detectionGuildInfo(Guild guild) {
		autoReplaceGuildCDR(guild);// 判断是否自动转换会长
		resetGuildInfo(guild);// 重置公会信息（清理过时的申请信息，重置建设等等）
		// 记录更新后时间
		guild.setGuildUpdateTime(Port.getTime());
	}

	/**
	 * 添加一个新的名字
	 * 
	 * @param name
	 */
	@DistrMethod
	public void add(String name) {
		guildNameSet.add(name);
	}

	@DistrMethod
	public void mapGuildCDR(Human human) {
		long humanId = human.getId();
		if (humanGuildMap.containsKey(humanId)) {
			long guildId = humanGuildMap.get(humanId);
			Guild guild = guildMap.get(guildId);
			GuildData guildData = GuildData.get(guild.getGuildHuman(), humanId);
			if (guildData == null) {
				JSONArray ja = Utils.toJSONArray(guild.getGuildHuman());// 获取并添加原先拥有的所有会员
				if (ja == null || ja.isEmpty()) {
					ja = new JSONArray();
				}
				JSONObject jo = GuildData.getJSONObject(new GuildData(human, 1, humanId==guild.getGuildLeaderId()?1:0, 0));
				ja.add(jo); // 添加新加入会员信息
				guild.setGuildHuman(ja.toJSONString()); // 公会成员信息
			}
			String guildName = guild.getGuildName();
			port.returns("result", true, "guildId", guildId,"guildName",guildName);
			return;
		}
		port.returns("result", false);
	}

	/**
	 * 更改公会名字
	 * 
	 * @param oldName
	 * @param newName
	 */
	@DistrMethod
	public void changeGuildName(String oldName, String newName) {
		guildNameSet.remove(oldName);
		guildNameSet.add(newName);
	}

	@DistrMethod
	public void updatHumanGlobalServiceGuildName(long humanId, long guildId) {
		String guildName = "";
		Guild guild = guildMap.get(guildId);
		if (guild != null) {
			guildName = guild.getGuildName();
		}
		HumanGlobalServiceProxy pxy = HumanGlobalServiceProxy.newInstance();
		pxy.updatGuildName(humanId, guildName);
	}

	/**
	 * 修改指定公会会长的名字
	 * 
	 * @param guildId
	 * @param newName
	 */
	@DistrMethod
	public void changeChairmanName(Human human, long guildId, String newName) {
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			return;
		}
		guild.setGuildLeaderName(newName);
		guildMap.put(guildId, guild);
		updateGuildHuman(guildId, human, 1, Port.getTime());
	}

	/**
	 * 名字是否重复
	 * 
	 * @param newName
	 */
	@DistrMethod
	public void isRepeatGuildName(String newName) {
		port.returns("repeat", guildNameSet.contains(newName));
	}

	/***
	 * 根据公会id 获取公会信息
	 * 
	 * @param guildId
	 */
	@DistrMethod
	public void getGuildInfo(long guildId, Human human) {
		Guild guild = guildMap.get(guildId);
		if (guild != null) {
			long humanId = human.getId();
			GuildData guildData = GuildData.get(guild.getGuildHuman(), humanId);
			if (guildData == null) {
				JSONArray ja = Utils.toJSONArray(guild.getGuildHuman());// 获取并添加原先拥有的所有会员
				if (ja == null || ja.isEmpty()) {
					ja = new JSONArray();
				}
				JSONObject jo = GuildData.getJSONObject(new GuildData(human, 1, humanId==guild.getGuildLeaderId()?1:0, 0));
				ja.add(jo); // 添加新加入会员信息
				guild.setGuildHuman(ja.toJSONString()); // 公会成员信息
			}
		}
		port.returns("guild", guildMap.get(guildId));
	}

	/**
	 * 添加或修改公会信息
	 * 
	 * @param guild
	 */
	@DistrMethod
	public void updateGuildInfo(Guild guild) {
		guildMap.put(guild.getId(), guild);
	}

	/**
	 * 创建公会
	 * 
	 * @param human
	 * @param guildName
	 */
	@DistrMethod
	public void createGuild(Human human, String guildName, String content,int icon) { // TODO
		Guild guild = new Guild();
		// 最后确认一次,避免有时前端同一时间发多次同一请求时出错
		// 名字是否重复
		boolean isRepeatGuildName = guildNameSet.contains(guildName);
		if (isRepeatGuildName) {
			// 返回失败
			port.returns("guild", null, "result", false);
			return;
		}
		// 是否已经是会长
		if (isGuildCDR(human.getId())) {
			// 返回失败
			port.returns("guild", null, "result", false);
			return;
		}
		guild.setId(Port.applyId()); // 公会id
		guild.setGuildLevel(1); // 公会等级
		guild.setGuildName(guildName); // 公会名字
		guild.setQQ(0); // 入会最低等级
		guild.setGuildLiveness(0); // 公会活跃度
		guild.setGuildLeaderId(human.getId()); // 公会会长id
		guild.setGuildLeaderName(human.getName()); // 公会会长名字
		guild.setGuildTotalContribute(0); // 公会总贡献值
		guild.setGuildOwnNum(1); // 公会拥有会员总人数
		guild.setGuildStatus(1); // 公会状态，1 默认可以直接加入，2 要申请

		guild.setGuildDeclare(ParamManager.guildDeclare); // 公会宣告
		guild.setGuildNotice(ParamManager.guildNotice); // 公会内部宣告
		if (!StringUtils.isEmpty(content)) {
			guild.setGuildDeclare(content);
			guild.setGuildNotice(content);
		}
		guild.setGuildIcon(icon); // 公会Icon

		JSONArray ja = new JSONArray();
		GuildData bean = new GuildData(human, 1, 1, 0);
		JSONObject jo = GuildData.getJSONObject(bean); // 生成会长信息
		ja.add(jo);
		guild.setGuildHuman(ja.toJSONString()); // 公会成员信息
		guild.setGuildCombat(bean.combat); // 军团总战斗力
		JSONObject postMember = Utils.toJSONObject(guild.getGuildPostMember());
		postMember.put(String.valueOf(bean.id), bean.post);
		guild.setGuildPostMember(postMember.toJSONString());
		guild.persist();
		human.setGuildId(guild.getId()); // 记录属于公会id
		human.setGuildName(guild.getGuildName());
		human.setGuildLevel(guild.getGuildLevel());// 公会等级
		guildNameSet.add(guildName); // 同步公会名字
		guildMap.put(guild.getId(), guild); // 同步公会信息
		// 加入公会排行榜
		changeGuildRank(guild);
		// 清空我的申请信息
		resetMyApplyList(human.getId());

		port.returns("guild", guild, "result", true); // 返回公会信息和是否成功
	}

	/**
	 * 判断是否已经是会长了
	 * 
	 * @param id
	 * @return
	 */
	private boolean isGuildCDR(long id) {
		for (Guild guild : guildMap.values()) {
			if (guild == null)
				continue;
			if (guild.getGuildLeaderId() == id) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否能创建
	 * 
	 */
	@DistrMethod
	public void isCanGuild(long humanId, String guildName) {
		boolean isRepeatGuildName = guildNameSet.contains(guildName);
		boolean isCDR = isGuildCDR(humanId);
		boolean ret = false;
		if (!isCDR && !isRepeatGuildName) {

			ret = true;
		}
		Log.guild.info("是否是会长={},是否工会名字重复={}", isCDR, isRepeatGuildName);
		port.returns("isCan", ret);
	}

	/**
	 * 删除公会
	 * 
	 * @param id
	 */
	@DistrMethod
	public void removeGuild(long id) {
		Guild guild = guildMap.get(id);
		if (guild != null) {
			removeGuild(guild);
		}
	}

	/**
	 * 删除公会
	 * 
	 * @param guild
	 */
	private void removeGuild(Guild guild) {
		if (guild != null) {
			long id = guild.getId();// 公会id
			String name = guild.getGuildName();// 公会名字
			removeGuildSendMail(guild);// 删除公会前要给所有成员发送解散公会邮件
			guildNameSet.remove(name);// 删除对应的名字
			// 删除公会排行榜
			RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
			// proxy.deleteRank(id, RankType.RankGuild);
			guild.remove();
			guildMap.remove(id);
		}
	}

	/**
	 * 删除公会时要先给所有会员发送邮件
	 * 
	 * @param guild
	 */
	private void removeGuildSendMail(Guild guild) {
		List<GuildData> listGuild = GuildData.jsonToList(guild.getGuildHuman());
		if (listGuild.isEmpty()) {
			return;
		}
		for (GuildData guildBean : listGuild) {
			long humanId = guildBean.id;
			// sn +|+ 公会名字 +|+ 会长名字
			GuildManager.inst().sendRemoveGuildSysMail(humanId, guild.getGuildName(), guild.getGuildLeaderName());
			GuildManager.inst().informLeaveGuild(humanId, guild.getId(), guild.getGuildName(), false);// 通知该玩家离开公会
		}
	}

	/**
	 * 快速寻找可直接加入的公会
	 * 
	 * @param humanId
	 */
	@DistrMethod
	public void autoAddGuild(long humanId, int level) {
		for (Guild guild : guildMap.values()) {
			// 不是可加入状态或等级未满足条件
			if (guild == null || guild.getGuildStatus() != 1 || level < guild.getInitiationMinLevel())
				continue;
			boolean isYetJoin = GuildManager.inst().isYetJoin(guild, humanId);// 判断是否已经加入该公会
			boolean result = isCanJoin(guild.getGuildOwnNum(), humanId, guild.getGuildLevel());
			if (isYetJoin || result) {// 可加入
				port.returns("guildId", guild.getId(), "result", true);
				return;
			}
		}
		port.returns("guildId", 0L, "result", false);
	}

	/**
	 * 是否可以快速加入公会
	 * 
	 * @param guildOwnNum
	 * @param humanId
	 * @param guildLevel
	 * @return
	 */
	public boolean isCanJoin(int guildOwnNum, long humanId, int guildLevel) {
		boolean isCan = false;
		ConfGuildLevel confguildLv = ConfGuildLevel.get(guildLevel);
		if (confguildLv == null) {
			Log.table.error("ConfGuildLevel 配表错误，no find sn={}", guildLevel);
			return isCan;
		}
		if (guildOwnNum >= confguildLv.maxNum) {// 判断是否已经满员
			// 仙盟可拥有人数已满！
			return isCan;
		}
		return true;
	}

	/**
	 * 判断是否可以改名
	 * 
	 * @param guildId
	 * @param humanId
	 * @param guildName
	 */
	@DistrMethod
	public void isCanRename(long guildId, long humanId, String guildName) {
		boolean ret = false;
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		int post = GuildManager.inst().getGuildPost(guild.getGuildPostMember(), humanId);
		if (guild.getGuildLeaderId() == humanId || post > 0) { // 会长
			ret = true;
		}
		boolean isRepeatGuildName = guildNameSet.contains(guildName);// 名字是否重复
		if (ret && !isRepeatGuildName) {
			port.returns("result", true, "guild", guild);
			return;
		}
		port.returns("result", false);
	}

	@DistrMethod
	public void updateNotice(long guildId, long humanId, String notice) {
		boolean ret = false;
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		int post = GuildManager.inst().getGuildPost(guild.getGuildPostMember(), humanId);
		if (guild.getGuildLeaderId() == humanId || post > 0) { // 会长
			ret = true;
		}
		if (ret) {
			guild.setGuildNotice(notice);
			port.returns("result", true);
			return;
		}
		port.returns("result", false);
	}

	/**
	 * 修改工会公告
	 * 
	 * @param guildId
	 * @param humanId
	 * @param declare
	 */
	@DistrMethod
	public void updateDeclare(long guildId, long humanId, String declare) {
		boolean ret = false;
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		// 判断是否有权限修改
		int post = GuildManager.inst().getGuildPost(guild.getGuildPostMember(), humanId);
		if (guild.getGuildLeaderId() == humanId || post > 0) {
			ret = true;
		}
		if (ret) {
			guild.setGuildDeclare(declare);
			port.returns("result", true);
			return;
		}
		port.returns("result", false);
	}

	@DistrMethod
	public void updateIcon(long guildId, long humanId, int icon) {
		boolean ret = false;
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		// 判断是否有权限修改
		int post = GuildManager.inst().getGuildPost(guild.getGuildPostMember(), humanId);
		if (guild.getGuildLeaderId() == humanId || post > 0) {
			ret = true;
		}
		// 判断公会等级是否已经解锁此图标
		boolean isIcon = GuildManager.inst().isIconMeetGuildLv(icon, guild.getGuildLevel());
		if (ret && isIcon) {
			guild.setGuildIcon(icon);
			port.returns("result", true);
			return;
		}
		port.returns("result", false);
	}

	/**
	 * 改名
	 * 
	 * @param guildId
	 * @param guildName
	 */
	@DistrMethod
	public void rename(long guildId, String guildName) {
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		// 最后验证一次
		boolean isRepeatGuildName = guildNameSet.contains(guildName);// 名字是否重复
		if (!isRepeatGuildName) {
			//把之前的名字清除
			guildNameSet.remove(guild.getGuildName());
			guild.setGuildName(guildName);
			guildNameSet.add(guildName);
			guildMap.put(guild.getId(), guild);
			port.returns("result", true);
			return;
		} else {
			port.returns("result", false);
			return;
		}
	}

	/**
	 * 更改公会设置
	 * 
	 * @param humanId
	 * @param guildId
	 *            公会id
	 * @param initiationMinLevel
	 *            入会最低等级
	 * @param isApply
	 *            是否需要申请 0 默认可以直接加入， 1 需要申请
	 */
	@DistrMethod
	public void changeGuildSet(long humanId, long guildId, int QQ, int isApply, int initiationMinLevel) {
		boolean ret = false;
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		// 有官职的都可以设置
		boolean isOwn = GuildManager.inst().isOwnGuildPost(guild.getGuildPostMember(), humanId);
		if (isOwn) {
			ret = true;
		}
		if (guild.getGuildLeaderId() == humanId) { // 会长
			ret = true;
		}
		if (ret) {// 有权限可以设置
			if (isApply != 0) {
				guild.setGuildStatus(isApply);// 是否需要申请,0不改变,1默认可以直接加入,2需要申请
			}
			guild.setQQ(QQ);// QQ群
			guild.setInitiationMinLevel(initiationMinLevel);// 入会最低等级 mapGuild.put(guild.getId(), guild);
			port.returns("result", true);
			return;
		}
		port.returns("result", false);
	}

	/*
	 * 根据公会名字 模糊查询相关的公会 分页
	 * 
	 * @DistrMethod public void selectNameGuild(String guildName, int page){ int
	 * pageSize = GuildManager.inst().getPageSize();//每页几条数据 if(page <= 0){ page =
	 * 1; } int index = (page-1) * pageSize; int endIndex = page * pageSize; String
	 * sql =
	 * Utils.createStr("where `{}` like `{}` ORDER BY `{}` DESC limit `{}`,`{}`" ,
	 * Guild.K.guildName, "%"+guildName+"%", Guild.K.guildLevel, index, endIndex);
	 * DBServiceProxy prx = DBServiceProxy.newInstance(); prx.findByQuery(false,
	 * Guild.tableName, sql); Param param = prx.waitForResult(); List<Record>
	 * records; records = param.get(); List<Guild> listGuild = new
	 * ArrayList<Guild>(); //List<Long> listGuildId = new ArrayList<Long>();
	 * for(Record r : records) { Guild guild = new Guild(r); if(guild != null){
	 * listGuild.add(guild); //listGuildId.add(guild.getGuildId()); } }
	 * port.returns("listGuild", listGuild); //port.returns("listGuildId",
	 * listGuildId); }
	 */

	/**
	 * 分页查询公会， 根据公会等级和活跃度排序 （降序）
	 * 
	 * @param page
	 */
	@DistrMethod
	public void selectAllGuildInfo(long humanId, int page) {
		List<Guild> listGuild = new ArrayList<>();
		listGuild.addAll(guildMap.values());
		Collections.sort(listGuild, new Comparator<Guild>() {// 降序
			@Override
			public int compare(Guild u1, Guild u2) {
				if (u1 == null || u2 == null)
					return 0;
				if (u1.getGuildLevel() > u2.getGuildLevel())
					return -1;// u1排前面
				else if (u1.getGuildLevel() < u2.getGuildLevel())
					return 1;// u1排后面
				else
					return (int) (u2.getGuildExp() - u1.getGuildExp());// >0，u1排前面；<0，u1排后面
					
			}
		});
		// 获取第几页的所有公会信息
		getGuildPageList(humanId, listGuild, page);
	}

	/**
	 * 根据查询页数，获取该页的所有公会信息
	 * 
	 * @param listGuild
	 *            所有公会信息（排序后）
	 * @param page
	 *            第几页
	 */
	private void getGuildPageList(long humanId, List<Guild> listGuild, int page) {

		if (page <= 0) {// 默认从1开始
			page = 1;
		}
		int listGuildSize = listGuild.size();
		int pageSize = 10;
		// ParamManager.selectGuildPageSize;// 每页几条数据
		int index = (page - 1) * pageSize;// 第page页 第一条数据下标位置 index
		if (index >= listGuildSize && index != 0) {
			port.returns("");
			return;
		}
		int temp = index + pageSize;// 获取 第page页 最后一条数据下标位置 temp
		if (temp > listGuildSize) {
			temp = listGuildSize;
		}
		List<Long> applyList = getApplyLists(humanId);
		port.returns("listGuild", listGuild, "page", page, "listGuildSize", listGuildSize, "applyList", applyList);
	}

	/**
	 * 查看会员信息
	 * 
	 * @param guildId
	 */
	@DistrMethod
	public void seeGuildMemberInfo(long guildId) {
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			return;
		}
		List<GuildData> listGuild = GuildData.jsonToList(guild.getGuildHuman());

		port.returns("listGuild", listGuild, "guild", guild);
	}

	/**
	 * 玩家退会
	 * 
	 * @param human
	 * @param guild
	 */
	@DistrMethod
	public void guildLeave(Human human, Guild guild) {
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		long humanId = human.getId();
		if (humanId == guild.getGuildLeaderId()) {// 会长退会
			int guildOwnNum = guild.getGuildOwnNum();
			boolean isCanMakeOver = isCanMakeOver(guild, humanId);// 判断会长退会后能否转让
			if (guildOwnNum <= 1 || !isCanMakeOver) {// 如果只有会长一个人就解散公会
				removeGuild(guild);
				port.returns("result", true);
				return;
			}
			boolean ret = getCDRLeave(humanId, guild);
			if (ret) {
				human.setGuildId(0);// 设置所属公会id
				human.setGuildName("");
				human.setGuildLevel(0);// 设置所属公会等级
			}
			port.returns("result", ret);
			return;
		}
		boolean isOwn = GuildManager.inst().isOwnGuildPost(guild.getGuildPostMember(), humanId);// 带有职位的会员
		if (isOwn) {// 有职位会员退会
			String guildPost = GuildManager.inst().removeGuildPost(guild.getGuildPostMember(), humanId);
			guild.setGuildPostMember(guildPost);
		}
		String guildHuman = GuildData.delete(guild.getGuildHuman(), humanId);
		guild.setGuildHuman(guildHuman);
		human.setGuildId(0);
		human.setGuildLevel(0);// 公会等级
		int ownNum = GuildData.getGuildHumanNum(guild.getGuildHuman());
		guild.setGuildOwnNum(ownNum);// 公会总人数
		// 公会战力改变
		long guildCombatNew = GuildData.countGuildCombat(guild.getGuildHuman());
		guild.setGuildCombat(guildCombatNew);
		// 记录日志
		guild = addGuildImmoLog(guild, human.getName(), ELogType.LogTypeDaily_VALUE, ELogHandleType.HandleLeave_VALUE,
				humanId, human.getAptitude());// 2踢出公会
		// 同步公会信息
		updateGuildInfo(guild);
		// 公会排行榜改变
		changeGuildRank(guild);
		port.returns("result", true);
	}

	/**
	 * 判断是否有满足当会长的会员
	 * 
	 * @param guild
	 * @param humanId
	 * @return
	 */
	private boolean isCanMakeOver(Guild guild, long humanId) {
		if (guild == null) {
			return false;
		}
		List<GuildData> listGuild = GuildData.jsonToList(guild.getGuildHuman());
		if (listGuild.isEmpty()) {
			return false;
		}

		for (GuildData guildBean : listGuild) {
			if (guildBean.id != humanId) {// 不是此人
				boolean isGoOut = GuildManager.inst().isGoOut(guildBean.timeLogout, ParamManager.offlineTimeLimit);// 是否过期
				if (!isGoOut) {// 如果有会员会员未离线太久 说明会长可以转让
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 会长退会
	 * 
	 * @param humanId
	 * @param guild
	 */
	private boolean getCDRLeave(long humanId, Guild guild) {
		if (guild == null) {
			return false;
		}

		GuildData guildListBean = GuildManager.inst().getGuildListBean(guild);
		if (guildListBean == null) {
			return false;
		}
		guild = updatePost(guild, guildListBean, true);
		if (guild != null) {
			String guildHuman = GuildData.delete(guild.getGuildHuman(), humanId);
			guild.setGuildHuman(guildHuman);
			// 获取并添加原先拥有的所有会员
			int ownNum = GuildData.getGuildHumanNum(guild.getGuildHuman());
			guild.setGuildOwnNum(ownNum);// 公会总人数
			// 公会战力改变
			long guildCombatNew = GuildData.countGuildCombat(guild.getGuildHuman());
			guild.setGuildCombat(guildCombatNew);
			updateGuildInfo(guild);
			// 公会排行榜改变
			changeGuildRank(guild);
			return true;
		}
		return false;
	}

	/**
	 * 判断玩家是否能加入公会
	 * 
	 */
	@DistrMethod
	public void isCanGuildJoin(GuildData bean, long guildId) {
		Guild guild = guildMap.get(guildId);
		if (guild != null && bean != null) {
			boolean isYetJoin = GuildManager.inst().isYetJoin(guild, bean.id);// 判断是否已经加入该公会
			if (isYetJoin) {
				// 已经加入
				port.returns("result", true, "guild", guild, "isJoin", true);
				return;
			}
			if (guild.getGuildStatus() != 1) {
				port.returns("result", false, "guild", guild, "isApply", true);
				return;
			}
			if (bean.level < guild.getInitiationMinLevel()) {
				// 等级不满足最低入会要求
				port.returns("result", false, "guild", guild, "sysMsgSn", 34);
				return;
			}

			boolean result = isCanJoin(guild.getGuildOwnNum(), bean.id, guild.getGuildLevel());
			if (!result) {
				// 仙盟可拥有人数已满！
				port.returns("result", false, "guild", guild, "sysMsgSn", 11112);
				return;
			}

			JSONArray ja = Utils.toJSONArray(guild.getGuildHuman());// 获取并添加原先拥有的所有会员
			if (ja == null || ja.isEmpty()) {
				ja = new JSONArray();
			}
			JSONObject jo = GuildData.getJSONObject(bean);
			ja.add(jo); // 添加新加入会员信息

			guild.setGuildHuman(ja.toJSONString());// 保存新的所有公会成员信息
			guild.setGuildOwnNum(ja.size());
			guild = addGuildImmoLog(guild, bean.name, ELogType.LogTypeDaily_VALUE, ELogHandleType.HandleJion_VALUE,
					bean.id, bean.aptitude);// 1进入公会日志
			// 公会战力改变
			long guildCombatNew = GuildData.countGuildCombat(guild.getGuildHuman());
			guild.setGuildCombat(guildCombatNew);
			// 同步修改公会
			updateGuildInfo(guild);
			// 公会排行改变
			changeGuildRank(guild);
			port.returns("result", true, "guild", guild, "repeat", true);
			return;
		}
		port.returns("result", false, "guild", null);
	}

	/**
	 * 玩家入会
	 * 
	 */
	@DistrMethod
	public void guildJoin(GuildData bean, long guildId) {
		Guild guild = guildMap.get(guildId);

		if (guild != null && bean != null) {
			boolean isYetJoin = GuildManager.inst().isYetJoin(guild, bean.id);
			if (isYetJoin) {
				port.returns();
				return;
			}
			JSONArray ja = Utils.toJSONArray(guild.getGuildHuman());// 获取并添加原先拥有的所有会员
			if (ja == null || ja.isEmpty()) {
				ja = new JSONArray();
			}
			JSONObject jo = GuildData.getJSONObject(bean);
			ja.add(jo); // 添加新加入会员信息

			guild.setGuildHuman(ja.toJSONString());// 保存新的所有公会成员信息
			guild.setGuildOwnNum(ja.size());
			guild = addGuildImmoLog(guild, bean.name, ELogType.LogTypeDaily_VALUE, ELogHandleType.HandleJion_VALUE,
					bean.id, bean.aptitude);// 1进入公会日志

			removeMyApplylist(bean.id, guildId);

			// 公会排行榜改变
			long guildCombatNew = GuildData.countGuildCombat(guild.getGuildHuman());
			guild.setGuildCombat(guildCombatNew);
			changeGuildRank(guild);
			updateGuildInfo(guild);// 同步修改公会

			// 重置个人申请列表状态
			resetMyApplyList(bean.id);

			port.returns("guild", guild, "repeat", true);
		}

	}

	/**
	 * 重置个人申请列表状态
	 * 
	 * @param humanId
	 */
	private void resetMyApplyList(long humanId) {
		List<GuildApply> idlist = humanApplyMap.get(humanId);
		if (idlist != null) {
			Iterator<GuildApply> it = idlist.iterator();
			while (it.hasNext()) {
				GuildApply ga = it.next();
				it.remove();
				ga.remove();
			}

		}
	}

	/**
	 * 增加活跃值
	 * 
	 * @param guild
	 * @param active
	 */
	@DistrMethod
	public void addGuildLiveness(Guild guild, int active) {
		if (guild != null) {
			int guildLiveness = guild.getGuildLiveness() + active;
			guild.setGuildLiveness(guildLiveness);
			updateGuildInfo(guild);// 同步修改公会
			port.returns("result", true, "guildLiveness", guildLiveness);
		}

	}

	/**
	 * 公会献祭，增加贡献，进度和建设（公会经验）
	 * 
	 * @param guild
	 *            公会
	 * @param guildContribute
	 *            对公会的贡献
	 * @param humanId
	 *            人物id
	 * @param type
	 *            0 未建设，1低级建设，2中级建设，3高级建设
	 * @param contributePlan
	 *            进度
	 * @param contributeErect
	 *            增加建设值（公会经验）
	 */
	@DistrMethod
	public void guildImmo(Guild guild, long guildContribute, long humanId, int type, int contributePlan,
			int contributeErect) {
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		if (guild != null) {
			// 获取公会当前等级，用来确认是否升级
			int oldLevel = guild.getGuildLevel();

			GuildData bean = GuildData.get(guild.getGuildHuman(), humanId);
			if (bean == null) {
				port.returns("result", false);
				return;
			}
			if (bean.type == type) {
				port.returns("result", false);
				return;
			}
			// 对公会的贡献
			bean.contribute += guildContribute;
			// 设置建设类型
			bean.type = type;
			//更新一下离线时间
			
			if (0 != type) {// 有消耗类型就保存
				guild = addGuildImmoLog(guild, bean.name, ELogType.LogTypeImmo_VALUE, type, humanId, bean.aptitude);
			}
			String guildHuman = GuildData.modify(guild.getGuildHuman(), bean);// 修改信息
			guild.setGuildHuman(guildHuman);// 保存新的所有公会成员信息
			// 增加进度
			int guildPlan = guild.getGuildPlan() + contributePlan;
			guild.setGuildPlan(guildPlan);
			guild.setGuildImmoNum(guild.getGuildImmoNum() + 1);
			// 加经验
			guild = addExp(guild, contributeErect);
			// 获取当前等级
			int newLevel = guild.getGuildLevel();
			// 同步修改公会
			updateGuildInfo(guild);
			boolean isUp = false;
			if (newLevel > oldLevel) {
				isUp = true;
			}
			port.returns("result", true, "isUp", isUp, "guild", guild);
		}
	}

	/**
	 * 添加记录
	 * 
	 * @param guild
	 * @param name
	 *            名字
	 * @param logType
	 *            1献祭，2挑战，3日常
	 * @param value
	 *            对应类型的参数
	 * @return
	 */
	private Guild addGuildImmoLog(Guild guild, String name, int logType, int value, long humanId, int aptitude) {
		long time = Port.getTime();
		long guildId = guild.getId();
		GuildImmoLog log = new GuildImmoLog();
		log.setId(Port.applyId());
		log.setGuildId(guildId);
		log.setHumanName(name);
		log.setTime(time);
		log.setLogTypeKey(logType);
		log.setAptitudeKey(value);
		log.setHumanId(humanId);

		switch (logType) {
		case 1:// 公会建设日志（献祭）
			log.setImmotype(value);
			break;
		case 2:// 挑战日志
			log.setContent(value);
			break;
		case 3:// 日常操作日志
			log.setHandle(value);
			break;
		}
		log.persist();

		List<GuildImmoLog> list = mapGuildImmo.get(guildId);// 保存献祭信息
		if (null == list || list.isEmpty()) {
			list = new ArrayList<GuildImmoLog>();
		}
		list.add(log);
		list = listMax(list);
		mapGuildImmo.put(guildId, list);

		return guild;
	}

	/**
	 * List<GuildImmoLog>排序且最大限制个数判断
	 * 
	 * @param list
	 * @return
	 */
	private static List<GuildImmoLog> listMax(List<GuildImmoLog> list) {
		int removeNum = 0;// 要删除的个数
		int size = list.size();// 记录个数
		if (size > ParamManager.guildImmoLogMax) {// 如果记录的数据条数大于最大条数
			removeNum = size - ParamManager.guildImmoLogMax;
			// 降序（时间越早的在越后面）
			Collections.sort(list, (m1, m2) -> (int) (m2.getTime() - m1.getTime()));
		}
		// 删除时间早的
		for (int i = 0; i < removeNum; i++) {
			list.remove(list.size() - 1);
		}
		/*
		 * Iterator<GuildImmoLog> it = list.iterator(); int i = 0;//记录删除了几条 int index =
		 * 1; try { while (it.hasNext()) { GuildImmoLog log = it.next(); if(removeNum >
		 * 0 && i < removeNum){//拥有要删除个数且未删除干净 it.remove(); i++; continue; } log.sn =
		 * index;//重置记录sn list.set(index-1, log); index++; } } catch (Exception e) {
		 * e.printStackTrace(); }
		 */

		return list;
	}

	/**
	 * 添加经验 判断是否升级
	 * 
	 * @param guild
	 * @param addExp
	 * @return
	 */
	private Guild addExp(Guild guild, int addExp) {
		if (guild == null) {
			return guild;
		}
		if (addExp <= 0) {
			return guild;
		}
		int exp = addExp + guild.getGuildExp();
		int guildLevel = guild.getGuildLevel();
		ConfGuildLevel conf = ConfGuildLevel.get(guildLevel);
		if (conf == null) {
			return guild;
		}
		boolean ret = false;
		int guildExp = conf.guildExp;
		while (exp >= guildExp) {// 判断是否满足升级需要
			exp -= guildExp;
			guildLevel++;
			ret = true;
			ConfGuildLevel newConf = ConfGuildLevel.get(guildLevel);
			if (newConf == null) {
				guildLevel--;// 等级降回下来
				guild.setGuildLevel(guildLevel);
				guild.setGuildExp(guildExp);// 策划要求满经验
				return guild;
			}
			guildExp = newConf.guildExp;
		}
		if (ret) {
			guild.setGuildLevel(guildLevel);
		}
		guild.setGuildExp(exp);
		
		
		//发布工会升级事件
		Event.fire(EventKey.GuildAddExp, "guild", guild);
		
		return guild;
	}

	/**
	 * 增加公会贡献
	 * 
	 * @param guild
	 * @param contribute
	 */
	@DistrMethod
	public void addGuildTotalContribute(Guild guild, int contribute) {
		if (guild != null) {
			long totalContribute = guild.getGuildTotalContribute() + contribute;
			guild.setGuildTotalContribute(totalContribute);
			updateGuildInfo(guild);// 同步修改公会
			port.returns("result", true, "totalContribute", totalContribute);
		}
	}

	/***
	 * 判断入会申请是否申请成功
	 * 
	 * @param human
	 * @param guildId
	 */
	@DistrMethod
	public void isCanApply(Human human, long guildId) {
		Guild guild = guildMap.get(guildId);
		if (guild != null && human != null) {
			int applyMaxNum = ParamManager.guildMemberLimit;
			if (guild.getGuildApplyNum() >= applyMaxNum) {
				// 申请列表已经满了
				port.returns("result", false, "sysMsgSn", 530104);
				return;
			}
			if (guild.getInitiationMinLevel() > human.getLevel()) {
				port.returns("result", false, "sysMsgSn", 34);// 等级不足
				return;
			}

			ConfGuildLevel conf = ConfGuildLevel.get(guild.getGuildLevel());
			if (conf == null) {
				Log.table.error("===ConfGuildLevel配表错误，no sn={}", guild.getGuildLevel());
				port.returns("result", false);
				return;
			}
			// 策划要求公会人数满了，就不能申请
			if (guild.getGuildOwnNum() >= conf.maxNum) {
				port.returns("result", false, "sysMsgSn", 11112);
				return;
			}
			GuildData bean = GuildData.get(guild.getGuildApplyHuman(), human.getId());
			if (bean != null) {
				// 申请过了
				port.returns("result", false, "sysMsgSn", 530103);
				return;
			}
			// 添加入会申请
			addApply(guild, human);
		}
		port.returns("result", true);
	}

	/**
	 * 添加入会申请
	 * 
	 * @param guild
	 * @param human
	 */
	private void addApply(Guild guild, Human human) {
		// 个人申请列表添加
		addMyApplylist(human.getId(), guild.getId());

		if (guild != null && human != null) {
			JSONArray ja = Utils.toJSONArray(guild.getGuildApplyHuman());// 获取原有所有申请信息
			if (ja == null || ja.isEmpty()) {
				ja = new JSONArray();
			}
			JSONObject jo = GuildApplyData.humanToJSONObject(human, Port.getTime());
			ja.add(jo);
			guild.setGuildApplyHuman(ja.toJSONString());
			guild.setGuildApplyNum(ja.size());
			updateGuildInfo(guild);// 同步修改公会
			port.returns("result", true, "guild", guild);
		}

	}

	/**
	 * 添加工会id到个人申请列表中
	 * 
	 * @param humanId
	 * @param guildid
	 */
	private void addMyApplylist(long humanId, long guildid) {
		GuildApply apply = new GuildApply();
		apply.setId(Port.applyId());
		apply.setApplyTime(Port.getTime());
		apply.setGuildId(guildid);
		apply.setHumanId(humanId);
		apply.persist();
		List<GuildApply> idlist = humanApplyMap.get(humanId);
		if (idlist == null) {
			idlist = new ArrayList<>();
			humanApplyMap.put(humanId, idlist);
		}
		idlist.add(apply);
	}

	/**
	 * 删除我的申请列表
	 */
	private void removeMyApplylist(long humanId, long guildid) {
		List<GuildApply> idlist = humanApplyMap.get(humanId);
		if (idlist != null) {
			Iterator<GuildApply> it = idlist.iterator();
			while (it.hasNext()) {
				GuildApply ga = it.next();
				if (ga.getGuildId() == guildid) {
					it.remove();
					ga.remove();
					break;
				}
			}

		}
	}

	/**
	 * 查看公会的所有申请入会信息
	 * 
	 * @param guildId
	 */
	@DistrMethod
	public void seeApplyInfo(long guildId) {
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			port.returns();
			return;
		}
		SCApplyInfoResult msg = getSCApplyInfoResultMsg(guild);

		port.returns("msg", msg, "guild", guild);
	}

	private SCApplyInfoResult getSCApplyInfoResultMsg(Guild guild) {
		SCApplyInfoResult.Builder msg = SCApplyInfoResult.newBuilder();
		List<GuildApplyData> applyList = GuildApplyData.jsonToList(guild.getGuildApplyHuman());
		if (!applyList.isEmpty()) {
			for (int i = 0; i < applyList.size(); i++) {
				GuildApplyData bean = applyList.get(i);
				if (bean == null)
					continue;
				msg.addApplyInfo(bean.createMsg());
			}
		}
		return msg.build();
	}

	/**
	 * 回复申请
	 * 
	 * @param humanId
	 * @param guildId
	 * @param replyId
	 */
	@DistrMethod
	public void replyApply(long humanId, long guildId, long replyId) {
		Guild guild = guildMap.get(guildId);
		if (guild == null || humanId <= 0 || replyId <= 0) {
			Log.game.info("guildId={},humanId={},replyId={}", guildId, humanId, replyId);
			port.returns("result", false, "guild", guild);
			return;
		}
		boolean ret = isOwnGuildPost(guild.getGuildPostMember(), humanId);
		// 不是会长,也没有职位,就没有权限
		if (humanId != guild.getGuildLeaderId() && !ret) {
			port.returns("result", false, "guild", guild);
			return;
		}
		// 查找申请列表中此人
		GuildApplyData bean = GuildApplyData.get(guild.getGuildApplyHuman(), replyId);
		if (bean == null) {
			port.returns("result", false, "guild", guild);
			return;
		}
		// 判断是否已经加入该公会
		boolean isYetJoin = GuildManager.inst().isYetJoin(guild, bean.id);
		if (isYetJoin) {
			// 已经加入
			port.returns("result", true, "guild", guild);
			return;
		}
		if (bean.level < guild.getInitiationMinLevel()) {
			// 等级不满足最低入会要求
			port.returns("result", false, "guild", guild, "sysMsgSn", 34);
			return;
		}
		boolean result = isCanJoin(guild.getGuildOwnNum(), bean.id, guild.getGuildLevel());
		if (!result) {
			// 仙盟可拥有人数已满！
			port.returns("result", false, "guild", guild, "sysMsgSn", 11112);
			return;
		}
		// 循环所有公会查看是否已经加入其它公会
		if (isCanOrderGuild(replyId)) {
			// 删除请求信息
			String apply = GuildApplyData.delete(guild.getGuildApplyHuman(), replyId);
			guild.setGuildApplyHuman(apply);
			// 该玩家已经加入别的公会
			port.returns("result", false, "guild", guild, "sysMsgSn", 530105);
			return;
		}
		GuildData guildData = new GuildData(bean);
		// 获取并添加原先拥有的所有会员
		JSONArray ja = Utils.toJSONArray(guild.getGuildHuman());
		if (ja == null || ja.isEmpty()) {
			ja = new JSONArray();
		}
		JSONObject jo = GuildData.getJSONObject(guildData);
		// 添加新加入会员信息
		ja.add(jo);
		// 保存新的所有公会成员信息
		guild.setGuildHuman(ja.toJSONString());
		guild.setGuildOwnNum(ja.size());
		// 1进入公会日志
		guild = addGuildImmoLog(guild, bean.name, ELogType.LogTypeDaily_VALUE, ELogHandleType.HandleJion_VALUE, bean.id,
				bean.aptitude);
		// 公会排行榜改变
		long guildCombatNew = GuildData.countGuildCombat(guild.getGuildHuman());
		guild.setGuildCombat(guildCombatNew);
		// 删除请求信息
		String apply = GuildApplyData.delete(guild.getGuildApplyHuman(), replyId);
		guild.setGuildApplyHuman(apply);
		// 更新排行
		changeGuildRank(guild);
		// 同步修改公会
		updateGuildInfo(guild);
		port.returns("result", true, "guild", guild);
	}

	private boolean isCanOrderGuild(long replyId) {
		for (Guild guild : guildMap.values()) {
			GuildData humanGuildData = GuildData.get(guild.getGuildHuman(), replyId);
			if (humanGuildData != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否拥有官职、权限
	 * 
	 * @param json
	 * @param humanId
	 * @return
	 */
	private boolean isOwnGuildPost(String json, long humanId) {
		JSONObject member = Utils.toJSONObject(json);
		if (!member.isEmpty()) {
			String key = String.valueOf(humanId);
			if (member.containsKey(key)) { // 有官职的都可以设置
				return true;
			}
		}
		return false;
	}

	/**
	 * 删除请求信息
	 * 
	 */
	@DistrMethod
	public void removeApplyInfo(long guildId, long replyId) {
		Guild guild = guildMap.get(guildId);
		if (guild == null || replyId <= 0) {
			return;
		}
		// if (guild.getGuildApplyHuman().isEmpty()) {
		// return;
		// }
		String apply = GuildApplyData.delete(guild.getGuildApplyHuman(), replyId);
		guild.setGuildApplyHuman(apply);
		updateGuildInfo(guild);

		removeMyApplylist(replyId, guildId);
		SCApplyInfoResult msg = getSCApplyInfoResultMsg(guild);
		port.returns("repeat", true, "replyId", replyId, "msg", msg, "success", true);
	}

	/**
	 * 踢出公会
	 * 
	 * @param guild
	 * @param humanId
	 */
	@DistrMethod
	public void kickOutGuild(Guild guild, long humanId) {
		if (guild == null) {
			port.returns("result", false);
			return;
		}
		GuildData humanGuildData = GuildData.get(guild.getGuildHuman(), humanId);
		String bean = GuildData.delete(guild.getGuildHuman(), humanId);
		if (bean == null || bean.isEmpty()) {
			port.returns("result", false);
			return;
		}
		// boolean ret = GuildManager.inst().isOwnGuildPost(guild.getGuildPostMember(),
		// humanId);
		// if(!ret) {
		// Log.guild.info("没有职权！");
		// return;
		// }
		String post = GuildManager.inst().removeGuildPost(guild.getGuildPostMember(), humanId);
		guild.setGuildPostMember(post);
		guild.setGuildHuman(bean);
		int ownNum = GuildData.getGuildHumanNum(guild.getGuildHuman());
		guild.setGuildOwnNum(ownNum);// 公会总人数
		if (humanGuildData != null) {
			// 记录日志
			guild = addGuildImmoLog(guild, humanGuildData.name, ELogType.LogTypeDaily_VALUE,
					ELogHandleType.HandleKick_VALUE, humanId, humanGuildData.aptitude);// 5踢出公会
		}
		// 公会排行榜改变
		long guildCombatNew = GuildData.countGuildCombat(guild.getGuildHuman());
		guild.setGuildCombat(guildCombatNew);
		changeGuildRank(guild);
		updateGuildInfo(guild);
		port.returns("result", true);
	}

	/**
	 * 清空申请列表信息
	 * 
	 * @param guild
	 */
	@DistrMethod
	public void removeApplyInfo(Guild guild) {
		if (guild == null) {
			return;
		}
		String applyStr = guild.getGuildApplyHuman();
		List<GuildApplyData> l = GuildApplyData.jsonToList(applyStr);
		long guildId = guild.getId();
		for (GuildApplyData ga : l) {
			long humanId = ga.id;
			removeMyApplylist(humanId, guildId);
		}

		guild.setGuildApplyHuman(null);
		guild.setGuildApplyNum(0);
		updateGuildInfo(guild);

		SCApplyInfoResult msg = getSCApplyInfoResultMsg(guild);
		port.returns("msg", msg);
	}

	/**
	 * 修改会员职位设置
	 * 
	 */
	@DistrMethod
	public void updateGuildPostMember(Guild guild, GuildData bean, boolean isCDR) {
		if (guild == null || bean == null) {
			port.returns("result", false);
			return;
		}
		guild = updatePost(guild, bean, isCDR);
		if (guild != null) {
			updateGuildInfo(guild);
			port.returns("result", true, "post", bean.post);
		} else {
			port.returns("result", false);
		}
	}

	/**
	 * 修改会员在公会内的职位
	 * 
	 * @param guild
	 * @param bean
	 * @param isCDR
	 * @return
	 */
	private Guild updatePost(Guild guild, GuildData bean, boolean isCDR) {

		String guildHuman = guild.getGuildHuman();
		JSONObject postMember = Utils.toJSONObject(guild.getGuildPostMember());
		String beanId = String.valueOf(bean.id);
		int post = 0;
		if (postMember.containsKey(beanId)) {
			post = postMember.getIntValue(beanId);
		}
		if (isCDR) {
			// 原会长
			long cdrId = guild.getGuildLeaderId();// 原会长id
			GuildData cdrBean = GuildData.get(guild.getGuildHuman(), cdrId);
			if (cdrBean == null) {
				return null;
			}
			String cdrBeanId = String.valueOf(cdrBean.id);
			if (postMember.containsKey(cdrBeanId)) {// 判断会长是否在里面
				postMember.remove(cdrBeanId);
			}
			cdrBean.post = 0;
			guildHuman = GuildData.modify(guildHuman, cdrBean);// 修改信息
			// 记录日志
			guild = addLog(1, cdrBean.post, guild, cdrBean.name, cdrBean.id, cdrBean.aptitude);
			// 新会长
			if (postMember.containsKey(beanId)) {
				postMember.remove(beanId);
			}
			bean.post = 1;
			guildHuman = GuildData.modify(guildHuman, cdrBean);// 修改信息
			guild.setGuildLeaderId(bean.id);// 新会长id
			guild.setGuildLeaderName(bean.name);
		}
		postMember.put(beanId, bean.post);
		guild = addLog(post, bean.post, guild, bean.name, bean.id, bean.aptitude);
		guild.setGuildPostMember(postMember.toJSONString());

		guildHuman = GuildData.modify(guildHuman, bean);// 修改信息
		guild.setGuildHuman(guildHuman);// 保存所有公会成员信息

		return guild;
	}

	/**
	 * 添加日志
	 * 
	 * @param postOld
	 * @param postNow
	 * @param guild
	 * @param name
	 */
	private Guild addLog(int postOld, int postNow, Guild guild, String name, long humanId, int aptitude) {
		if (postOld != postNow) {
			switch (postNow) {
			case 0:
				if (postOld == 1) {
					guild = addGuildImmoLog(guild, name, ELogType.LogTypeDaily_VALUE,
							ELogHandleType.HandleOutgoingCDR_VALUE, humanId, aptitude);// 6卸任会长
				}
				if (postOld == 2) {// 副会长
					guild = addGuildImmoLog(guild, name, ELogType.LogTypeDaily_VALUE,
							ELogHandleType.HandleOutgoingViceCDR_VALUE, humanId, aptitude);// 7卸任副会长
				}
				break;
			case 1:
				guild = addGuildImmoLog(guild, name, ELogType.LogTypeDaily_VALUE, ELogHandleType.HandleAppointCDR_VALUE,
						humanId, aptitude);// 3任命会长
				break;
			case 2:
				// 记录日志
				guild = addGuildImmoLog(guild, name, ELogType.LogTypeDaily_VALUE,
						ELogHandleType.HandleAppointViceCDR_VALUE, humanId, aptitude);// 3任命副会长
				break;

			default:
				break;
			}
		}
		return guild;
	}

	/**
	 * 修改会员信息
	 * 
	 * @param guildId
	 * @param human
	 */
	@DistrMethod
	public void updateGuildHuman(long guildId, Human human, int onlineStatus, long time) {
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			Log.game.error("guild=null guidlId={}", guildId);
			return;
		}
		long guildCombatOld = guild.getGuildCombat();

		String guildHuman = GuildData.modify(guild.getGuildHuman(), human, onlineStatus, time);
		if (guildHuman == null || guildHuman.isEmpty()) {
			Log.game.error("guildHuman=null guidlId={}, humanId={}", guildId, human.getId());
			return;
		}
		guild.setGuildHuman(guildHuman);
		// 重新计算公会总战力
		boolean isRankChange = false;
		long guildCombatNew = GuildData.countGuildCombat(guild.getGuildHuman());
		if (guildCombatNew > guildCombatOld) {
			guild.setGuildCombat(guildCombatNew);
			// 公会排行榜改变
			changeGuildRank(guild);
			isRankChange = true;
		}
		updateGuildInfo(guild);
	}

	/**
	 * 公会总战力改变，排行更新
	 * 
	 * @param guild
	 */
	private void changeGuildRank(Guild guild) {
		// 不处理
		//RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		//proxy.addNew(new RankData(guild),RankType.RankGuild);
	}

	/**
	 * 自动发送公会邮件
	 * 
	 * @param guildId
	 * @param isReplaceCDR
	 */
	public void sendGuildMail(long guildId, boolean isReplaceCDR, String name) {
		Guild guild = guildMap.get(guildId);
		if (guild == null) {
			Log.game.error("guild=null guildId={}", guildId);
			return;
		}
		String json = guild.getGuildHuman();
		if (json.isEmpty() || json.equals("")) {
			return;
		}
		JSONArray ja = Utils.toJSONArray(json);
		if (ja == null || ja.isEmpty()) {
			Log.game.error("guildHuman=null guildId={}", guildId);
			return;
		}
		// 会长离线时间太长 通知会员
		for (int i = 0; i < ja.size(); i++) {
			GuildData vo = new GuildData(ja.getJSONObject(i));
			if (vo != null) {
				if (isReplaceCDR) {// 是否是更换会长
					GuildManager.inst().sendGuildNewCDRMail(vo.id, name);
				} else {
					GuildManager.inst().sendGuildCDROfflineMail(vo.id);
				}
			}
		}

	}

	/**
	 * 根据名字查询相似名字的公会
	 */
	@DistrMethod
	public void getNameSimilarGuild(String name) {
		List<Guild> listGuild = new ArrayList<>();
		for (Guild guild : guildMap.values()) {
			if (ifContainIndexOf(guild.getGuildName(), name)) {
				listGuild.add(guild);
			}
		}
		Collections.sort(listGuild, new Comparator<Guild>() {// 降序
			@Override
			public int compare(Guild g1, Guild g2) {
				if (g1 == null || g2 == null)
					return 0;
				if (g1.getGuildLevel() > g2.getGuildLevel())
					return -1;// u1排前面
				else if (g1.getGuildLevel() < g2.getGuildLevel())
					return 1;// u1排后面
				else
					return (int) (g2.getId() - g1.getId());// >0，u1排前面；<0，u1排后面id小的更前
			}
		});
		port.returns("listGuild", listGuild);
	}

	@DistrMethod
	public void getGuildName(long guildId) {
		String guildName = "";
		if (guildMap.get(guildId) != null) {
			guildName = guildMap.get(guildId).getGuildName();
		}
		port.returns("guidlName", guildName);
	}

	/**
	 * 根据名字查询相似名字的公会
	 */
	@DistrMethod
	public void getNameGuild(String name) {
		if (!guildNameSet.contains(name)) {
			port.returns("guild", null);
			return;
		}
		for (Guild guild : guildMap.values()) {
			if (guild.getGuildName().equals(name)) {
				port.returns("guild", guild);
				return;
			}
		}
		port.returns("guild", null);
	}

	// indexof
	public static boolean ifContainIndexOf(String a, String b) {
		if (0 <= a.indexOf(b)) {
			return true;
		}
		return false;
	}

	// 正则
	public static boolean ifContainRegex(String a, String b) {
		if (a.matches(".*" + b + ".*")) {
			return true;
		}
		return false;
	}

	/**
	 * 重置公会信息（清理过时的申请信息，重置建设等等）
	 */
	public void resetGuildInfo(Guild guild) {
		if (guild == null) {
			return;
		}
		String applyHuman = GuildApplyData.deleteBy(guild.getGuildApplyHuman(), Port.getTime());
		guild.setGuildApplyHuman(applyHuman);
		String guildHuman = GuildData.modifyType(guild.getGuildHuman());
		if (guildHuman != null && !guildHuman.isEmpty()) {
			guild.setGuildHuman(guildHuman);
		}
		// 公会成员总信息
		int ownNum = GuildData.getGuildHumanNum(guild.getGuildHuman());
		if (ownNum <= 0) {
			Log.game.error("ownNum={} guildId={}, guildLv={}, leaderId={},LeaderName={}", ownNum, guild.getId(),
					guild.getGuildLevel(), guild.getGuildLeaderId(), guild.getGuildLeaderName());
			removeGuild(guild);
			return;
		}
		if (GuildData.getGuildHumanNum(guild.getGuildHuman()) != guild.getGuildOwnNum()) {
			guild.setGuildOwnNum(ownNum);
		}
		guild.setGuildPlan(0);// 公会祭祀进度重置
		guild.setGuildImmoNum(0);// 公会祭祀人数重置
		updateGuildInfo(guild);
	}

	public void autoReplaceGuildCDR(Guild guild) {
		if (guild == null) {
			return;
		}
		DB dbPrx = DB.newInstance(Human.tableName);
		dbPrx.get(guild.getGuildLeaderId());
		dbPrx.listenResult(this::_result_findHuman, "guild", guild);
	}

	/**
	 * 根据公会id获取公会等级
	 * 
	 * @param id
	 */
	@DistrMethod
	public void getGuildLevel(long id) {
		if (guildMap.containsKey(id)) {
			port.returns("guildLevel", guildMap.get(id).getGuildLevel());
		} else {
			port.returns("guildLevel", 0);
		}
	}

	/**
	 * 查找公会会长 判断会长是否太久未能上线
	 * 
	 * @param results
	 * @param context
	 */
	private void _result_findHuman(Param results, Param context) {
		Record re = results.get();
		if (re == null) {
			Log.game.error("re is null");
			return;
		}
		Human human = new Human(re);
		if (human != null) {
			Guild guild = Utils.getParamValue(context, "guild", null);
			if (guild == null) {
				Log.game.error("re is null");
				return;
			}
			long humanId = human.getId();
			GuildData humanGuildData = GuildData.get(guild.getGuildHuman(), humanId);
			if (humanGuildData == null) {
				Log.game.error("humanGuildData = null, guildId={}", guild.getId());
				return;
			}
			long timeLogout = humanGuildData.timeLogout; // 离线时的时间
			long timeLongin = human.getTimeLogin(); // 最近一次上线时间
			long belongGuildId = human.getGuildId(); // 所属公会id

			if (timeLongin < timeLogout) {// 离线
				/*
				 * 现在不用通知了 boolean ret1 = GuildManager.inst().isGoOut(time,
				 * ParamManager.offlineTime); if (ret1) {// 该向所有会员发送会长离线消息
				 * sendGuildMail(belongGuildId, false, ""); }
				 */
				boolean ret2 = GuildManager.inst().isGoOut(timeLogout, ParamManager.offlineTimeLimit);
				if (ret2) {// 该让出位置了
					boolean ret = isCanMakeOver(guild, humanId);// 是否可以转让
					if (!ret) {// 不可转让时， 解散公会
						removeGuild(guild);
						return;
					}
					GuildData guildListBean = GuildManager.inst().getGuildListBean(guild);
					if (guildListBean == null) {
						return;
					}
					guild = updatePost(guild, guildListBean, true);// 更换会长
					if (guild != null) {
						updateGuildInfo(guild);
						sendGuildMail(belongGuildId, true, guildListBean.name);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param guildId
	 */
	@DistrMethod
	public void getGuildImmo(long guildId) {
		if (!mapGuildImmo.containsKey(guildId) && !guildMap.containsKey(guildId)) {
			port.returns("guildImmo", null, "guildHuman", null);
			return;
		}
		port.returns("guildImmo", mapGuildImmo.get(guildId), "guildHuman", guildMap.get(guildId).getGuildHuman());
	}

	@DistrMethod
	public void cancleJoin(long humanId, long guildId) {

	}

	/**
	 * 某人是否在该工会中
	 * 
	 * @param humanid2
	 */
	@DistrMethod
	public void isUnion(long guildId, long humanid2) {
		Guild guild = guildMap.get(guildId);
		boolean isUnion = false;
		if (guild == null) {
			port.returns("isUnion", isUnion);
			return;
		}
		List<GuildData> listGuild = GuildData.jsonToList(guild.getGuildHuman());
		for (GuildData data : listGuild) {
			if (data.id == humanid2) {
				isUnion = true;
				break;
			}
		}
		port.returns("isUnion", isUnion);
	}

	public void broadcastGuildInstData(Guild guild, GuildInstData inst) {
        List<GuildData> listGuild = GuildData.jsonToList(guild.getGuildHuman());
		if (listGuild.isEmpty()) {
            return;
        }
		for (GuildData guildBean : listGuild) {
            long humanId = guildBean.id;
            HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
            prx.syncGuildInstData(humanId, inst);
        }
    }

    private void initGuildInst() {
        for (Guild guild : guildMap.values()) {
            if (guild == null)
                continue;
            String jsonStr = guild.getGuildInst();
            if (!jsonStr.isEmpty()) {
                GuildInstData inst = new GuildInstData(jsonStr);
                guildInst.put(guild.getId(), inst);
            }
            jsonStr = guild.getGuildReward();
            if (!jsonStr.isEmpty()) {
                GuildInstStageRewardData data = GuildInstStageRewardData.create(jsonStr);
                guildReward.put(guild.getId(), data);
            }
        }
        checkResetGuildInst(false);
    }

    private void checkResetGuildInst(boolean notify) {
	    long resetTime = Utils.getTimeHourOfToday(ParamManager.dailyHourReset);
	    for (Map.Entry<Long, GuildInstData> e : guildInst.entrySet()) {
	        GuildInstData inst = e.getValue();
	        if (inst.lastResetTime >= resetTime) {
	            continue;
            }
            long guildId = e.getKey();

	        int chapterOrder1 = inst.chapterOrder();
	        int chapterOrder2 = 0;

	        boolean resetYet = false;
            Define.EGuildInstResetType resetType = inst.resetType;
            if (resetType == Define.EGuildInstResetType.BACK) {
                int chapterOrder = inst.chapterOrder();
                if (chapterOrder > 1) {
                    backChapter(inst);
                    chapterOrder2 = inst.chapterOrder();
                    resetYet = true;
                }
            }
            if (!resetYet) {
                for (int i = 0; i < inst.hpMax.size(); ++i) {
                    List<Integer> l = inst.hpCur.get(i);
                    l.clear();
                    for (Integer hp : inst.hpMax.get(i)) {
                        l.add(hp);
                    }
                }
            }
            inst.lastResetTime = Port.getTime();

            Guild guild = guildMap.get(guildId);
            if (guild != null) {
                guild.setGuildInst(inst.toJsonStr());
                if (notify) {
                    broadcastGuildInstData(guild, inst);
                }
                GuildInstStageRewardData rewardData = guildReward.get(guildId);
                if (rewardData != null) {
                    if (chapterOrder1 > 0)
                        clearRewardRecord(rewardData, chapterOrder1);
                    if (chapterOrder2 > 0)
                        clearRewardRecord(rewardData, chapterOrder2);
                    guild.setGuildReward(rewardData.toJsonStr());
                }
            }
        }
    }

    private boolean isGuildInstOpen(Guild guild) {
        ConfGuildOpenLv confOpen = ConfGuildOpenLv.get(2);
        if (confOpen == null) {
            return false;
        }
        if (guild.getGuildLevel() < confOpen.level) {
            return false;
        }
        return true;
    }

    private long __getTime(String timeStr) {
	    int time = Integer.parseInt(timeStr);
	    int sec = time%100;
	    int min = time/100 % 100;
	    int hour = time/10000;
	    return hour * Time.HOUR + min * Time.MIN + sec * Time.SEC;
    }

    private boolean isGuildInstTime(long now) {
	    ConfParam confParam = ConfParam.get("guildInstTimeInterval");
	    if (confParam == null) {
	        return true;
        }
        String[] range = confParam.value.split(",");
	    if (range.length != 2) {
	        return false;
        }
        long tmZero = Utils.getTimeBeginOfToday(now);// 当天0点时间
        long start = tmZero + __getTime(range[0]);
        long end = tmZero + __getTime(range[1]);
        return now >= start && now < end;
    }

    private GuildInstData startGuildInstData() {
        GuildInstData inst = new GuildInstData();
        inst.chapter = GuildInstData.chapterSn(1);
        buildChapter(inst);
        inst.lastResetTime = 0;
        inst.resetType = Define.EGuildInstResetType.CURRENT;
        return inst;
    }

    private void clearRewardRecord(GuildInstStageRewardData data, int chapterOrder) {
        int startStage = GuildInstData.stageSn(chapterOrder, 1);
        for (int i = 0; i < 3; ++i) {
            int stage = i + startStage;
            data.stageMap.remove(stage);
        }
    }
    private boolean buildChapter(GuildInstData inst) {
	    inst.hpMax.clear();
	    inst.hpCur.clear();
	    int chapterOrder = inst.chapterOrder();
        int startStage = GuildInstData.stageSn(chapterOrder, 1);
        for (int i = 0; i < 3; ++i) {
            int stage = i + startStage;
            ConfGuildInstStage conf = ConfGuildInstStage.get(stage);
            if (conf == null) {
                break;
            }
            ConfInstMonster confMon = ConfInstMonster.get(conf.instSn);
            if (confMon == null) {
                break;
            }
            List<Integer> hpMax = new ArrayList<>();
            List<Integer> hpCur = new ArrayList<>();
            inst.hpMax.add(hpMax);
            inst.hpCur.add(hpCur);
            for (int monsterSn : confMon.monsterIds) {
                int hp = 0;
                if (monsterSn > 0) {
                    ConfPartnerProperty confProperty = ConfPartnerProperty.get(monsterSn);
                    if (confProperty != null) {
                        PropCalc levelPropCalc = new PropCalc(Utils.toJSONString(confProperty.propName, confProperty.propValue));
                        hp = levelPropCalc.getInt(FightPropName.HpMax);
                    } else {
                        Log.table.error("===ConfPartnerProperty 配表错误，no find sn={}", monsterSn);
                    }
                }
                hpMax.add(hp);
                hpCur.add(hp);
            }
        }
        return true;
    }

    private void nextChapter(GuildInstData inst) {
	    int chapter = inst.chapter+1;
        //ConfGuildInstChapter confChapter = ConfGuildInstChapter.get(chapter);
        //if (confChapter == null) {
        //    return;
        //}
	    inst.chapter = chapter;
	    buildChapter(inst);
    }

    private void backChapter(GuildInstData inst) {
	    inst.chapter--;
	    buildChapter(inst);
    }

    private GuildInstData newGuildInstData(int chapterOrder) {
	    GuildInstData inst = new GuildInstData();
	    inst.chapter = GuildInstData.chapterSn(chapterOrder);
	    buildChapter(inst);
        if (inst.hpMax.isEmpty()) {
            return null; // 没有关卡数据
        }
        return inst;
    }

    private GuildInstData __getGuildInstData(long guildId) {
        Guild guild = guildMap.get(guildId);
        if (guild == null) {
            return null;
        }
        if(!isGuildInstOpen(guild)) {
            return null;
        }
        GuildInstData inst = guildInst.get(guildId);
        if (inst == null) {
            inst = startGuildInstData();
            if (inst == null) {
                return null;
            }
            guildInst.put(guildId, inst);
        }
        return inst;
    }

    @DistrMethod
    public void getGuildInstData(long guildId) {
	    GuildInstData inst = __getGuildInstData(guildId);
	    port.returns("instData", inst);
    }

    @DistrMethod
    public void getChallengeGuildInstData(long guildId, int stage) {
	    Guild guild = guildMap.get(guildId);
	    if (guild == null) {
            port.returns("instData", null);
	        return;
        }
        GuildInstData inst = __getGuildInstData(guildId);
        if (inst == null) {
            port.returns("instData", null);
            return;
        }
        if (!isGuildInstOpen(guild)) {
            port.returns("instData", null);
            return;
        }
        long now = Port.getTime();
        if (!isGuildInstTime(now)) {
            port.returns("instData", null);
            return;
        }
        int chapter = GuildInstData.chapterSnFromStageSn(stage);
        if (chapter != inst.chapter) {
            port.returns("instData", null);
            return;
        }
        int idx = GuildInstData.stageOrder(stage) - 1;
        List<Integer> hpL = inst.hpCur.get(idx);
        if (hpL == null) {
            port.returns("instData", null);
            return;
        }
        GuildInstParam param = new GuildInstParam();
        param.stage = stage;
        param.hpCur = hpL;
        param.hpMax = inst.hpMax.get(idx);
        port.returns("instData", param);
    }

    @DistrMethod
	public void ReduceHp(long guildId, int stage, List<Integer> harmList, String playerName) {
	    GuildInstData inst = guildInst.get(guildId);
	    if (inst == null) {
            port.returns("ok", false, "instData", null);
	        return;
        }
        int chapter = GuildInstData.chapterSnFromStageSn(stage);
        if (chapter > inst.chapter) {
            port.returns("ok", false, "instData", inst);
	        return;
        } else if (chapter < inst.chapter) {
            String killer = inst.getKiller(stage);
            port.returns("ok", true, "instData", inst, "death", true, "killer", killer);
            return; // stage over
        }
        int idx = GuildInstData.stageOrder(stage) - 1;
        List<Integer> hpL = inst.hpCur.get(idx);
        if (hpL.isEmpty()) {
            String killer = inst.getKiller(stage);
            port.returns("ok", true, "instData", inst, "death", true, "killer", killer);
            return; // stage over
        }
        boolean death = true;
        for (int i=0; i<hpL.size(); ++i) {
            int hp = hpL.get(i);
            if (hp > 0) {
                int take = i < harmList.size() ? harmList.get(i) : 0;
                hp -= take;
                if (hp < 0) {
                    hp = 0;
                }
                hpL.set(i, hp);
                if (hp > 0) {
                    death = false;
                }
            }
        }
        String killer = "";
        if (death) {
            killer = playerName;

            hpL.clear();

            inst.setKiller(stage, killer);
            if (inst.isChapterOver()) {
                nextChapter(inst);
            }

            // 击杀增加仙盟建设度
            Guild guild = guildMap.get(guildId);
            if (guild != null) {
                ConfGuildInstStage conf = ConfGuildInstStage.get(stage);
                if (conf != null) {
                    addExp(guild, conf.guildReward);
                }
                guild.setGuildInst(inst.toJsonStr());
            }
        } else {
            Guild guild = guildMap.get(guildId);
            if (guild != null) {
                guild.setGuildInst(inst.toJsonStr());
            }
        }
        port.returns("ok", true, "instData", inst, "death", death, "killer", killer);
    }

    @DistrMethod
    public void setGuildInstResetType(long playerId, long guildId, Define.EGuildInstResetType resetType) {
	    Guild guild = guildMap.get(guildId);
        if (guild == null) {
            port.returns("result", 530107);
            return;
        }
        if (playerId != guild.getGuildLeaderId()) {
            port.returns("result", 530127);
            return;
        }
        if (!isGuildInstOpen(guild)) {
            port.returns("result", 34);
            return;
        }
        GuildInstData inst = guildInst.get(guildId);
        if (inst == null) {
            inst = startGuildInstData();
            if (inst == null) {
                port.returns("result", 530119);
                return;
            }
            guildInst.put(guildId, inst);
        }
        if (resetType != inst.resetType) {
            inst.resetType = resetType;
            guild.setGuildInst(inst.toJsonStr());
        }
        port.returns("result", 0, "resetType", resetType);
    }

    @DistrMethod
    public void checkChapterReward(long guildId, int chapter) {
        Guild guild = guildMap.get(guildId);
        if (guild == null) {
            port.returns("result", 530107);
            return;
        }
        if (!isGuildInstOpen(guild)) {
            port.returns("result", 34);
            return;
        }
        GuildInstData inst = guildInst.get(guildId);
        if (inst == null) {
            port.returns("result", 530119);
            return;
        }
        if (inst.chapter <= chapter) {
            port.returns("result", 530119);
            return;
        }
        port.returns("result", 0);
    }

    private String _rewardKey(long guildId, int stage, int slot) {
	    return String.format("%s-%s-%s", guildId, stage, slot);
    }

    @DistrMethod
    public void checkStageReward(long guildId, int stage, int slot) {
	    // check lock
	    String key = _rewardKey(guildId, stage, slot);
	    if (guildRewardLock.get(key) != null) {
            port.returns("result", 530128);
	        return;
        }
        // 该栏位是否被领取
        GuildInstStageRewardData data = guildReward.get(guildId);
        if (data != null) {
            List<GuildInstStageRewardInfo> list = data.stageMap.get(stage);
            if (list != null) {
                for (GuildInstStageRewardInfo info : list) {
                    if (info.slot == slot) {
                        port.returns("result", 530129);
                        return;
                    }
                }
            }
        }
        Guild guild = guildMap.get(guildId);
        if (guild == null) {
            port.returns("result", 530107);
            return;
        }
        if (!isGuildInstOpen(guild)) {
            port.returns("result", 34);
            return;
        }
        GuildInstData inst = guildInst.get(guildId);
        if (inst == null) {
            port.returns("result", 530120);
            return;
        }
        int chapter = GuildInstData.chapterSnFromStageSn(stage);
        if (chapter > inst.chapter) {
            port.returns("result", 530120);
            return;
        }
        if (chapter == inst.chapter) {
            int idx = GuildInstData.stageOrder(stage) - 1;
            if (!inst.isStageOver(idx)) {
                port.returns("result", 530120);
            }
        }
        // lock
        guildRewardLock.put(key, Port.getTime());
        port.returns("result", 0);
    }

    @DistrMethod
    public void syncStageReward(long guildId, String playerName, int stage, int slot, int itemSn, int itemNum, boolean ok) {
	    // remove Lock
        String key = _rewardKey(guildId, stage, slot);
        guildRewardLock.remove(key);

	    if (ok) {
            GuildInstStageRewardData data = guildReward.get(guildId);
            if (data == null) {
                data = new GuildInstStageRewardData();
                guildReward.put(guildId, data);
            }
            ArrayList<GuildInstStageRewardInfo> list = data.stageMap.get(stage);
            if (list == null) {
                list = new ArrayList<>();
                data.stageMap.put(stage, list);
            }
            GuildInstStageRewardInfo info = new GuildInstStageRewardInfo();
            info.slot = slot;
            info.playerName = playerName;
            info.itemSn = itemSn;
            info.itemNum = itemNum;
            list.add(info);
            Guild guild = guildMap.get(guildId);
            if (guild != null) {
                guild.setGuildReward(data.toJsonStr());
            }
        }

    }

    @DistrMethod
    public void stageRewardInfo(long guildId, int stage) {
        GuildInstStageRewardData data = guildReward.get(guildId);
        if (data == null) {
            port.returns("data", null);
            return;
        }
        List<GuildInstStageRewardInfo> list = data.stageMap.get(stage);
        port.returns("data", list);
    }
}
