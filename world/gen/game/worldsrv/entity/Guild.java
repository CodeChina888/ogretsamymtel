package game.worldsrv.entity;

import org.apache.commons.lang3.exception.ExceptionUtils;

import core.db.DBConsts;
import core.dbsrv.DB;
import core.Chunk;
import core.Port;
import core.Record;
import core.support.BufferPool;
import core.support.SysException;
import core.support.log.LogCore;
import core.entity.EntityBase;
import core.gen.GofGenFile;

@GofGenFile
public final class Guild extends EntityBase {
	public static final String tableName = "guild";

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String id = "id";	//id
		public static final String GuildLevel = "GuildLevel";	//公会等级
		public static final String GuildIcon = "GuildIcon";	//公会图标
		public static final String GuildName = "GuildName";	//公会名字
		public static final String QQ = "QQ";	//QQ群
		public static final String InitiationMinLevel = "InitiationMinLevel";	//入会最低等级
		public static final String GuildLiveness = "GuildLiveness";	//公会活跃度
		public static final String GuildExp = "GuildExp";	//公会经验
		public static final String GuildPlan = "GuildPlan";	//公会进度
		public static final String GuildImmoNum = "GuildImmoNum";	//公会祭祀人数
		public static final String GuildTotalContribute = "GuildTotalContribute";	//公会总贡献值
		public static final String GuildLeaderId = "GuildLeaderId";	//会长id
		public static final String GuildLeaderName = "GuildLeaderName";	//会长名字
		public static final String GuildPostMember = "GuildPostMember";	//有官职的会员{id, post}
		public static final String GuildOwnNum = "GuildOwnNum";	//公会拥有会员总人数
		public static final String GuildStatus = "GuildStatus";	//公会状态 1 可加入，2 需申请
		public static final String GuildDeclare = "GuildDeclare";	//公会宣告
		public static final String GuildNotice = "GuildNotice";	//公会内部宣告
		public static final String GuildHuman = "GuildHuman";	//公会成员信息{id:?,name:?,lv:?,...}
		public static final String GuildApplyHuman = "GuildApplyHuman";	//申请入会成员信息{id:1,name:2,lv:3,pr:4}
		public static final String GuildApplyNum = "GuildApplyNum";	//申请表条数
		public static final String GuildUpdateTime = "GuildUpdateTime";	//记录凌晨4点更新时间
		public static final String GuildChapterMax = "GuildChapterMax";	//副本已打到的最大章节
		public static final String GuildEncourageTimes = "GuildEncourageTimes";	//团长使用全军出击次数
		public static final String GuildChapterRestTimes = "GuildChapterRestTimes";	//团长已经重置副本次数
		public static final String GuildChapterAutoReset = "GuildChapterAutoReset";	//副本是否自动重置
		public static final String GuildCombat = "GuildCombat";	//军团战斗力
		public static final String GuildInst = "GuildInst";	//公会副本
		public static final String GuildReward = "GuildReward";	//公会副本领奖情况
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public Guild() {
		super();
		setGuildLevel(1);
		setGuildIcon(1);
		setQQ(0);
		setInitiationMinLevel(0);
		setGuildLiveness(0);
		setGuildExp(0);
		setGuildPlan(0);
		setGuildImmoNum(0);
		setGuildTotalContribute(0);
		setGuildLeaderId(0);
		setGuildOwnNum(1);
		setGuildStatus(1);
		setGuildApplyNum(0);
		setGuildUpdateTime(0);
		setGuildChapterMax(0);
		setGuildEncourageTimes(0);
		setGuildChapterRestTimes(0);
		setGuildChapterAutoReset(false);
		setGuildCombat(0);
	}

	public Guild(Record record) {
		super(record);
	}

	
	/**
	 * 新增数据
	 */
	@Override
	public void persist() {
		//状态错误
		if(record.getStatus() != DBConsts.RECORD_STATUS_NEW) {
			LogCore.db.error("只有新增包能调用persist函数，请确认状态：data={}, stackTrace={}", this, ExceptionUtils.getStackTrace(new Throwable()));
			return;
		}
		
		DB prx = DB.newInstance(getTableName());
		prx.insert(record);
		
		//重置状态
		record.resetStatus();
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * 默认不立即持久化到数据库
	 */
	@Override
	public void update() {
		update(false);
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * @param sync 是否立即同持久化到数据库
	 */
	@Override
	public void update(boolean sync) {
		//新增包不能直接调用update函数 请先调用persist
		if(record.getStatus() == DBConsts.RECORD_STATUS_NEW) {
			throw new SysException("新增包不能直接调用update函数，请先调用persist：data={}", this);
		}
		
		//升级包
		Chunk path = record.pathUpdateGen();
		if(path == null || path.length == 0) return;

		//将升级包同步至DB服务器
		DB prx = DB.newInstance(getTableName());
		prx.update(getId(), path, sync);
		
		//回收缓冲包
		BufferPool.deallocate(path.buffer);
		
		//重置状态
		record.resetStatus();
	}

	/**
	 * 删除数据
	 */
	@Override
	public void remove() {
		DB prx = DB.newInstance(getTableName());
		prx.delete(getId());
	}

	/**
	 * id
	 */
	public long getId() {
		return record.get("id");
	}

	public void setId(final long id) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("id", id);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会等级
	 */
	public int getGuildLevel() {
		return record.get("GuildLevel");
	}

	public void setGuildLevel(final int GuildLevel) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildLevel", GuildLevel);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会图标
	 */
	public int getGuildIcon() {
		return record.get("GuildIcon");
	}

	public void setGuildIcon(final int GuildIcon) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildIcon", GuildIcon);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会名字
	 */
	public String getGuildName() {
		return record.get("GuildName");
	}

	public void setGuildName(final String GuildName) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildName", GuildName);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * QQ群
	 */
	public int getQQ() {
		return record.get("QQ");
	}

	public void setQQ(final int QQ) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("QQ", QQ);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 入会最低等级
	 */
	public int getInitiationMinLevel() {
		return record.get("InitiationMinLevel");
	}

	public void setInitiationMinLevel(final int InitiationMinLevel) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("InitiationMinLevel", InitiationMinLevel);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会活跃度
	 */
	public int getGuildLiveness() {
		return record.get("GuildLiveness");
	}

	public void setGuildLiveness(final int GuildLiveness) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildLiveness", GuildLiveness);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会经验
	 */
	public int getGuildExp() {
		return record.get("GuildExp");
	}

	public void setGuildExp(final int GuildExp) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildExp", GuildExp);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会进度
	 */
	public int getGuildPlan() {
		return record.get("GuildPlan");
	}

	public void setGuildPlan(final int GuildPlan) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildPlan", GuildPlan);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会祭祀人数
	 */
	public int getGuildImmoNum() {
		return record.get("GuildImmoNum");
	}

	public void setGuildImmoNum(final int GuildImmoNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildImmoNum", GuildImmoNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会总贡献值
	 */
	public long getGuildTotalContribute() {
		return record.get("GuildTotalContribute");
	}

	public void setGuildTotalContribute(final long GuildTotalContribute) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildTotalContribute", GuildTotalContribute);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 会长id
	 */
	public long getGuildLeaderId() {
		return record.get("GuildLeaderId");
	}

	public void setGuildLeaderId(final long GuildLeaderId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildLeaderId", GuildLeaderId);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 会长名字
	 */
	public String getGuildLeaderName() {
		return record.get("GuildLeaderName");
	}

	public void setGuildLeaderName(final String GuildLeaderName) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildLeaderName", GuildLeaderName);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 有官职的会员{id, post}
	 */
	public String getGuildPostMember() {
		return record.get("GuildPostMember");
	}

	public void setGuildPostMember(final String GuildPostMember) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildPostMember", GuildPostMember);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会拥有会员总人数
	 */
	public int getGuildOwnNum() {
		return record.get("GuildOwnNum");
	}

	public void setGuildOwnNum(final int GuildOwnNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildOwnNum", GuildOwnNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会状态 1 可加入，2 需申请
	 */
	public int getGuildStatus() {
		return record.get("GuildStatus");
	}

	public void setGuildStatus(final int GuildStatus) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildStatus", GuildStatus);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会宣告
	 */
	public String getGuildDeclare() {
		return record.get("GuildDeclare");
	}

	public void setGuildDeclare(final String GuildDeclare) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildDeclare", GuildDeclare);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会内部宣告
	 */
	public String getGuildNotice() {
		return record.get("GuildNotice");
	}

	public void setGuildNotice(final String GuildNotice) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildNotice", GuildNotice);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会成员信息{id:?,name:?,lv:?,...}
	 */
	public String getGuildHuman() {
		return record.get("GuildHuman");
	}

	public void setGuildHuman(final String GuildHuman) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildHuman", GuildHuman);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 申请入会成员信息{id:1,name:2,lv:3,pr:4}
	 */
	public String getGuildApplyHuman() {
		return record.get("GuildApplyHuman");
	}

	public void setGuildApplyHuman(final String GuildApplyHuman) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildApplyHuman", GuildApplyHuman);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 申请表条数
	 */
	public int getGuildApplyNum() {
		return record.get("GuildApplyNum");
	}

	public void setGuildApplyNum(final int GuildApplyNum) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildApplyNum", GuildApplyNum);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 记录凌晨4点更新时间
	 */
	public long getGuildUpdateTime() {
		return record.get("GuildUpdateTime");
	}

	public void setGuildUpdateTime(final long GuildUpdateTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildUpdateTime", GuildUpdateTime);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 副本已打到的最大章节
	 */
	public int getGuildChapterMax() {
		return record.get("GuildChapterMax");
	}

	public void setGuildChapterMax(final int GuildChapterMax) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildChapterMax", GuildChapterMax);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 团长使用全军出击次数
	 */
	public int getGuildEncourageTimes() {
		return record.get("GuildEncourageTimes");
	}

	public void setGuildEncourageTimes(final int GuildEncourageTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildEncourageTimes", GuildEncourageTimes);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 团长已经重置副本次数
	 */
	public int getGuildChapterRestTimes() {
		return record.get("GuildChapterRestTimes");
	}

	public void setGuildChapterRestTimes(final int GuildChapterRestTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildChapterRestTimes", GuildChapterRestTimes);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 副本是否自动重置
	 */
	public boolean isGuildChapterAutoReset() {
		return record.<Integer>get("GuildChapterAutoReset") == 1;
	}

	public void setGuildChapterAutoReset(boolean GuildChapterAutoReset) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildChapterAutoReset", GuildChapterAutoReset ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 军团战斗力
	 */
	public long getGuildCombat() {
		return record.get("GuildCombat");
	}

	public void setGuildCombat(final long GuildCombat) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildCombat", GuildCombat);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会副本
	 */
	public String getGuildInst() {
		return record.get("GuildInst");
	}

	public void setGuildInst(final String GuildInst) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildInst", GuildInst);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 公会副本领奖情况
	 */
	public String getGuildReward() {
		return record.get("GuildReward");
	}

	public void setGuildReward(final String GuildReward) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("GuildReward", GuildReward);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}

}