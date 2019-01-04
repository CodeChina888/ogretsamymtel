package game.worldsrv.achieveTitle;

import game.msg.Define.DAchieveTitle;
import game.msg.Define.EAchieveTitleStatus;
import game.msg.Define.EInformType;
import game.msg.MsgTitle.SCGainAchieveTitle;
import game.msg.MsgTitle.SCLoginAchieveTitle;
import game.msg.MsgTitle.SCSelectAchieveTitle;
import game.msg.MsgTitle.SCUpdateAchieveTitle;
import game.worldsrv.achieveTitle.achieveTitleVO.AchieveTitleVO;
import game.worldsrv.achieveTitle.achieveTitleVO.TitleVO;
import game.worldsrv.achieveTitle.typedata.AchieveTitleTypeDataFactory;
import game.worldsrv.achieveTitle.typedata.IAchieveTitleTypeData;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfAchieveTitle;
import game.worldsrv.config.ConfMainCityShow;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.entity.AchieveTitle;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Human;
import game.worldsrv.human.HumanManager;
import game.worldsrv.inform.InformManager;
import game.worldsrv.item.ItemBodyManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.PropCalcCommon;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

/**
 * @author Neak
 * 成就称号系统
 */
public class AchieveTitleManager extends ManagerBase {
	
	// 只需要更新的类型定义
	public static final int UPDATE_TITLE_DEF = 1;
	// 刚解锁获得的类型定义
	public static final int GAIN_TITLE_DEF = 2;

	/**
	 * 获取实例
	 * @return
	 */
	public static AchieveTitleManager inst() {
		return inst(AchieveTitleManager.class);
	}

	/**
	 * 初始化全部成就任务
	 */
	@Listener(EventKey.HumanCreate)
	public void _listener_initAchieveTitle(Param param) {
		Human human = param.get("human");
		// 临时map，存储称号成就<tyep, AchieveTitle>
		HashMap<Integer, AchieveTitleVO> tmpMap = new HashMap<>();
		
		// 挨个遍历配置，并且初始化
		for (ConfAchieveTitle conf : ConfAchieveTitle.findAll()) {
			// 等级不满足的任务不开放 创角时等级为
			if (conf.needLv > 1) {
				continue;
			}
			// 初始化新开放的任务
			IAchieveTitleTypeData typeData = AchieveTitleTypeDataFactory.getTypeData(conf.type);
			// 持久化
			AchieveTitleVO atVO = typeData.init(human.getId(), conf, tmpMap); 
			tmpMap.put(conf.type, atVO);
		}
	}
	
	/**
	 * 玩家升级的时候，检查新增成就称号
	 * @param param
	 */
	@Listener(EventKey.HumanLvUp)
	public void _listener_initByHumanLevelUp(Param param) {
		HumanObject humanObj = param.get("humanObj");
		int level = humanObj.getHuman().getLevel();
		
		// 临时map，存储称号成就<tyep, AchieveTitle>
		Map<Integer, AchieveTitleVO> achieveTitleMap = humanObj.achieveTitleMap;
		
		boolean change = false;
		// 挨个遍历配置，并且初始化
		for (ConfAchieveTitle conf : ConfAchieveTitle.findAll()) {
			// 等级不满足的任务不开放
			if (conf.needLv > level)
				continue;
			// 已经存在的成就不处理
			TitleVO to = this.getTitleVOBySn(humanObj, conf.sn);
			if (to != null) {
				continue;
			}
			// 初始化新开放的任务
			IAchieveTitleTypeData typeData = AchieveTitleTypeDataFactory.getTypeData(conf.type);
			// 持久化
			AchieveTitleVO atVO = typeData.init(humanObj.getHuman().getId(), conf, achieveTitleMap); 
			// 加入内存管理中
			achieveTitleMap.put(conf.type, atVO);
			change = true;
		}
		// 如果有新增的称号，则给客户度下发，全部称号
		if (change) {
			this._send_UpdateAllMsg(humanObj);
		}
	}

	/**
	 * 玩家其它数据加载开始：加载玩家的称号信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(AchieveTitle.tableName);
		dbPrx.findBy(false, AchieveTitle.K.HumanId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanAchieveTitle, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanAchieveTitle(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanAchieveTitle humanObj is null");
			return;
		}
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanAchieveTitle records=null");
		} else {
			// 加载数据
			for (Record record : records) {
				AchieveTitle title = new AchieveTitle(record);
				AchieveTitleVO attObj = new AchieveTitleVO(title);
				humanObj.achieveTitleMap.put(title.getType(), attObj);
			}
			_send_LoginMsg(humanObj);
		}

		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}
	
	/**
	 * 玩家数据加载完毕后检查是否有称号过期
	 */
	@Listener(EventKey.HumanDataLoadAllFinish) 
	public void _listener_HumanDataLoadAllFinish(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		// 检查过期称号
		this.check_titleIsLimit(humanObj);
	}
	
	/**
	 * 每个整点执行一次
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_checkLimitTitle(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		// 检查过期称号
		this.check_titleIsLimit(humanObj);
	}
	/**
	 * 检查称号是否过期
	 */
	private void check_titleIsLimit(HumanObject humanObj) {
		long curTime = Port.getTime();
		// 当前使用的称号
		TitleVO curTitleVO = this.getCurUseTitleVO(humanObj);
		// 过期的称号列表
		List<TitleVO> limitTitles = new ArrayList<>(); 
		// 检查是否有成就到期
		for (AchieveTitleVO atVO : humanObj.achieveTitleMap.values()) {
			// 获取每个类型过期的称号
			limitTitles.addAll(atVO.getLimitTitleSnList(curTime));
		}
		
		// 如果有到期的称号，状态发生改变后，则推送给客户端
		if(limitTitles.size() != 0) {
			// 属性加成变化调整
			this._process_titleTotalProp(humanObj);
			// 如果之前佩戴的称号过期，则卸下该称号
			if (limitTitles.contains(curTitleVO)) {
				humanObj.getHuman().setTitleSn(0);
			}
			this._send_UpdateListMsg(humanObj, limitTitles);
		}
	}

	/**
	 * 竞技场达到最高名次 1
	 */
	@Listener(EventKey.CompeteRankHighest)
	public void _listener_CompeteRankHighest(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_CompeteRankHighest humanObj is null");
			return;
		}
		// 当前排名
		int rank = Utils.getParamValue(param, "rank", 0);
		achieveTitle_Update(humanObj, rank, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_1);
	}

	/**
	 * 主角升级 11
	 */
	@Listener(EventKey.HumanLvUp)
	public void _listener_HumanLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanLvUp humanObj is null");
			return;
		}
		// 当前等级
		int level = humanObj.getHuman().getLevel();
		achieveTitle_Update(humanObj, level, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_11);
	}
	
	/**
	 * 角色总战力变化 12
	 */
	@Listener(EventKey.HumanTotalCombatChange)
	public void _listener_HumanLineUpCombatChange(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanLineUpCombatChange humanObj is null");
			return;
		}
		// 当前上阵总战力
		int curCombat = HumanManager.inst().getLineUpCombat(humanObj);
		achieveTitle_Update(humanObj, curCombat, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_12);
	}
	
	/**
	 * 装备进阶 21
	 */
	@Listener(EventKey.EquipAdvanced)
	public void _listener_EquipAdvanced(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		AchieveTitleVO atVO = this.getAchieveTitleVOByType(humanObj, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_21);
		ConfAchieveTitle conf = null;
		int targetParam = 0;
		int progress = 0;
		for (TitleVO to : atVO.titleList) {
			conf = ConfAchieveTitle.get(to.sn);
			if (conf == null) {
				continue;
			}
			// 校验参考值：装备品阶
			targetParam = conf.param[1];
			// 达到条件的个数
			progress = ItemBodyManager.inst().getEquipNumByAdvancedLv(humanObj, targetParam);
			achieveTitle_Update(humanObj, progress, targetParam, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_21);
		}
	}
	
	/**
	 * 伙伴招募 31
	 */
	@Listener(EventKey.PartnerUnlocked)
	public void _listener_PartnerUnlocked(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		int partnerSn = Utils.getParamValue(param, "sn", null);
		ConfPartnerProperty confPartnerProperty = ConfPartnerProperty.get(partnerSn);
		if (confPartnerProperty == null) {
			Log.game.error("===_配置表PartnerProperty错误,sn={}", partnerSn);
			return;
		}
		// 校验参考值：伙伴品质
		int quality = confPartnerProperty.quality;
		achieveTitle_Update(humanObj, 1, quality, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_31);
	}
	
	/**
	 * 抽卡 32
	 */
	@Listener(EventKey.DrawCard)
	public void _listener_DrawCard(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		// 校验参考值：招募类型
		int paramTarget = Utils.getParamValue(param, "type", 0);
		int num = Utils.getParamValue(param, "num", 1);
		achieveTitle_Update(humanObj, num, paramTarget, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_32);
	}
	
	/**
	 * 参与世界boss 41
	 */
	@Listener(EventKey.ActInstWorldBossEnter)
	public void _listener_JoinWorldBoss(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		achieveTitle_Update(humanObj, 1, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_41);
	}
	
	/**
	 * 封印之地伤害达到前几 42
	 */
	@Listener(EventKey.ActInstWorldBossHarmRank)
	public void _listener_WorldBossDamage(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		// 本次伤害名次
		int rank = Utils.getParamValue(param, "rank", 0);
		achieveTitle_Update(humanObj, rank, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_42);
	}
	
	/**
	 * 九层妖塔通过x难度y层 51
	 */
	@Listener(EventKey.TowerPass)
	public void _listener_TowerPass(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		// 校验参考值：难度
		int diff = Utils.getParamValue(param, "selDiff", 1);
		int layer = Utils.getParamValue(param, "layer", 1);
		achieveTitle_Update(humanObj, layer, diff, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_51);
	}
	
	/**
	 * 成为某某玩法城主 61
	 */
	@Listener(EventKey.MainCityCastellan)
	public void _listener_MainCityMaster(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		// 进度：玩法sn
		int masterSn = Utils.getParamValue(param, "castellanType", 1);
		achieveTitle_Update(humanObj, masterSn, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_61);
		
		//发送系统公告
		ConfMainCityShow conf = ConfMainCityShow.get(masterSn);
		if (conf != null) {
			// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
			String content = Utils.createStr("{}|{}|{}|{}", ParamManager.sysMsgMark, 999003, 
					humanObj.getHuman().getName(), conf.sn);
			InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
		} else {
			Log.game.error("AchieveTitleManager._listener_MainCityMaster ConfMainCityShow error sn ={}",masterSn);
		}
	}
	
	/**
	 * 累计切磋达到几次 71
	 */
	@Listener(EventKey.PKMirrorFightNum)
	public void _listener_MainCityFightNum(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		achieveTitle_Update(humanObj, 1, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_71);
	}
	
	/**
	 * 累计参与洞天福地达到几次 81
	 */
	@Listener(EventKey.LootMapEnter)
	public void _listener_JoinLootMap(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ActConsumeGold humanObj is null");
			return;
		}
		// 校验参考值：模式（ELootMapType.LootMapSingle/ELootMapType.LootMapMultip），暂时没用
//		int lootMapType = Utils.getParamValue(param, "lootMapType", 1);
		achieveTitle_Update(humanObj, 1, AchieveTitleTypeKey.ACHIEVE_TITLE_TYPE_81);
	}
	
	/**
	 * 重载方法1：单一条件
	 * 更新成就完成度，判断是否是达到任务条件自动完成的类型
	 * @param humanObj
	 * @param progress
	 * @param type
	 */
	public void achieveTitle_Update(HumanObject humanObj, int progress, int type) {
		// 获得的称号sn 
		List<TitleVO> gainSns = new ArrayList<>();
		// 更新的称号
		List<TitleVO> updateSns = new ArrayList<>();
		
		// 取出所有类型称号
		Map<Integer, AchieveTitleVO> atMap = humanObj.achieveTitleMap;
		for (AchieveTitleVO atVO : atMap.values()) {
			// 任务类型不对，跳过
			if (atVO.achieveTitle.getType() != type) {
				continue;
			}
			// 处理任务进度
			IAchieveTitleTypeData typeData = AchieveTitleTypeDataFactory.getTypeData(type);
			Map<Integer, List<TitleVO>> tmpMap = typeData.doProgressAndCheckGain(humanObj, atVO, progress);
			
			// 获得的称号不为空
			List<TitleVO> tmpGainList = tmpMap.get(AchieveTitleManager.GAIN_TITLE_DEF);
			if (tmpGainList != null && !tmpGainList.isEmpty()) {
				gainSns.addAll(tmpGainList);
			}
			
			// 只需要更新的称号不为空
			List<TitleVO> tmpUpdateList = tmpMap.get(AchieveTitleManager.UPDATE_TITLE_DEF);
			if (tmpUpdateList != null && !tmpUpdateList.isEmpty()) {
				updateSns.addAll(tmpUpdateList);
			}
		}

		// 有新获得的称号，给客户端领取的数据
		if (gainSns.size() > 0) {
			_process_gainNewTitle(humanObj, gainSns);
		} 
		// 有需要更新的称号，给客户端发送更新的数据
		if (updateSns.size() > 0) {
			this._send_UpdateListMsg(humanObj, updateSns);
		}
	}
	/**
	 * 重载方法2：有校验参考值
	 * 更新成就完成度，判断是否是达到任务条件自动完成的类型
	 * @param humanObj
	 * @param progress 
	 * @param target 校验参考值
	 * @param type
	 */
	public void achieveTitle_Update(HumanObject humanObj, int progress, int target, int type) {
		// 获得的称号sn 
		List<TitleVO> gainSns = new ArrayList<>();
		// 更新的称号
		List<TitleVO> updateSns = new ArrayList<>();

		// 取出所有类型称号
		Map<Integer, AchieveTitleVO> atMap = humanObj.achieveTitleMap;
		for (AchieveTitleVO atVO : atMap.values()) {
			// 任务类型不对，跳过
			if (atVO.achieveTitle.getType() != type) {
				continue;
			}
			// 处理任务进度
			IAchieveTitleTypeData typeData = AchieveTitleTypeDataFactory.getTypeData(type);
			Map<Integer, List<TitleVO>> tmpMap = typeData.doProgressAndCheckGain(humanObj, atVO, progress, target);
			
			// 获得的称号不为空
			List<TitleVO> tmpGainList = tmpMap.get(AchieveTitleManager.GAIN_TITLE_DEF);
			if (tmpGainList != null && !tmpGainList.isEmpty()) {
				gainSns.addAll(tmpGainList);
			}
			
			// 只需要更新的称号不为空
			List<TitleVO> tmpUpdateList = tmpMap.get(AchieveTitleManager.UPDATE_TITLE_DEF);
			if (tmpUpdateList != null && !tmpUpdateList.isEmpty()) {
				updateSns.addAll(tmpUpdateList);
			}
		}

		// 有新获得的称号
		if (gainSns.size() > 0) {
			_process_gainNewTitle(humanObj, gainSns);
		} 
		// 有需要更新的称号，给客户端发送更新的数据
		if (updateSns.size() > 0) {
			this._send_UpdateListMsg(humanObj, updateSns);
		}
	}
	/**
	 * 获得新称号，下发数据，且更新属性
	 */
	public void _process_gainNewTitle(HumanObject humanObj, List<TitleVO> gainSns) {
		// 更新新称号
		this._send_UpdateListMsg(humanObj, gainSns);
		// 下发新解锁的称号，用于视图表现
		this._send_gainTitleMsg(humanObj, gainSns);
		
		// 处理称号的总属性（包含已经佩戴的）
		this._process_titleTotalProp(humanObj);
	}
	
	/**
	 * 新获得的所有称号sn下发
	 */
	private void _send_gainTitleMsg(HumanObject humanObj, List<TitleVO> gainSnList) {
		SCGainAchieveTitle.Builder msg = SCGainAchieveTitle.newBuilder();
		for (TitleVO titleVO : gainSnList) {
			msg.addTitleSn(titleVO.sn);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 登录下发称号成就信息
	 */
	private void _send_LoginMsg(HumanObject humanObj) {
		SCLoginAchieveTitle.Builder msg = SCLoginAchieveTitle.newBuilder();
		msg.addAllAchieveTitles(this.getAllDAchieveTitle(humanObj));
		TitleVO to = this.getCurUseTitleVO(humanObj);
		if (to != null) {
			msg.setCurTitleSn(to.sn);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 获得，更新，使用，称号成就信息
	 * @param humanObj
	 */
	private void _send_UpdateAllMsg(HumanObject humanObj) {
		SCUpdateAchieveTitle.Builder msg = SCUpdateAchieveTitle.newBuilder();
		msg.addAllAchieveTitles(this.getAllDAchieveTitle(humanObj));
		TitleVO to = this.getCurUseTitleVO(humanObj);
		if (to != null) {
			msg.setCurTitleSn(to.sn);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 发送相关sn的信息
	 */
	private void _send_UpdateListMsg(HumanObject humanObj, List<TitleVO> toList) {
		SCUpdateAchieveTitle.Builder msg = SCUpdateAchieveTitle.newBuilder();
		for (TitleVO to : toList) {
			msg.addAchieveTitles(to.createDAchieveTitle());
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 构建所有的DAchieveTitle结构给客户端
	 * @param humanObj
	 * @return
	 */
	private List<DAchieveTitle> getAllDAchieveTitle(HumanObject humanObj) {
		List<DAchieveTitle> list = new ArrayList<>();
		// 取出所有类型称号
		Map<Integer, AchieveTitleVO> atMap = humanObj.achieveTitleMap;
		for (AchieveTitleVO atVO : atMap.values()) {
			list.addAll(atVO.getDAchieveTitleList());
		}
		return list;
	}
	
	
	///////////////////////////////////
	// 处理与客户端交互协议
	//////////////////////////////////
	/**
	 * 玩家选择称号
	 * @param humanObj
	 * @param titleSn
	 */
	public void _msg_CSSelectAchieveTitle(HumanObject humanObj, int titleSn) {
		TitleVO to = this.getTitleVOBySn(humanObj, titleSn);
		if (to == null) {
			Log.game.error("=== 该称号不存在，titleSn = {} ===", titleSn);
			humanObj.sendSysMsg(631101);
			return;
		}
		if (to.status == EAchieveTitleStatus.AchieveTitleDoing_VALUE) {
			Log.game.error("=== 该称号未解锁 ，titleSn = {} ===", titleSn);
			humanObj.sendSysMsg(631102);
			return;
		}
		if (to.status == EAchieveTitleStatus.AchieveTitleUse_VALUE) {
			Log.game.error("=== 该称号正在使用中 ，titleSn = {} ===", titleSn);
			humanObj.sendSysMsg(631102);
			return;
		}
		
		int oldUseSn = 0;
		// 之前的称号设置为已获得
		TitleVO oldTo = this.getCurUseTitleVO(humanObj);
		if (oldTo != null) {
			// 设置为已获得
			oldTo.setStatus(EAchieveTitleStatus.AchieveTitleFinished_VALUE);
			AchieveTitleVO oldAtVO = this.getAchieveTitleVOByType(humanObj, oldTo.type);
			oldAtVO.modifyStatus();
			oldUseSn = oldTo.sn;
		}
		
		// 设置当前称号为正在使用的称号
		to.setStatus(EAchieveTitleStatus.AchieveTitleUse_VALUE);
		AchieveTitleVO curAtVO = this.getAchieveTitleVOByType(humanObj, to.type);
		curAtVO.modifyStatus();
		
		// 设置玩家当前的称号
		humanObj.getHuman().setTitleSn(to.sn);
		
		// 处理称号的总属性（包含已经佩戴的）
		this._process_titleTotalProp(humanObj);
		
		SCSelectAchieveTitle.Builder msg = SCSelectAchieveTitle.newBuilder();
		msg.setTitleSn(titleSn);
		msg.setOldUseSn(oldUseSn); // 之前使用的称号sn
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 处理称号的总属性（包含已经佩戴的）
	 */
	private void _process_titleTotalProp(HumanObject humanObj) {
		UnitManager.inst().propsChange(humanObj, EntityUnitPropPlus.Title);
	}
	
	public PropCalcCommon _process_titleTotalProps(HumanObject humanObj) {
		List<TitleVO> unlockTitleVOs = new ArrayList<>();
		// 取出对应类型称号
		for (AchieveTitleVO atVO : humanObj.achieveTitleMap.values()) {
			if (atVO == null) {
				continue;
			}
			// 添加该类型解锁的称号
			unlockTitleVOs.addAll(atVO.getUnlockTitleVO());
		}
		// 加成的属性
		PropCalcCommon prop = new PropCalcCommon();
		ConfAchieveTitle conf = null;
		for (TitleVO to : unlockTitleVOs) {
			// 该称号的配置表
			conf = ConfAchieveTitle.get(to.sn);
			if (conf == null) {
				continue;
			}
			
			// 增加的属性
			String[] gainAttr = conf.gainAttr;
			// 增加的属性值
			int[] gainAttrValue = conf.gainAttrValue;
			prop.add(gainAttr, gainAttrValue);
			
			if (to.status == EAchieveTitleStatus.AchieveTitleUse_VALUE) {
				// 佩戴加成的属性
				String[] selectAttr = conf.selectAttr;
				// 佩戴加成的属性值
				int[] selectAttrValue = conf.selectAttrValue;
				prop.add(selectAttr, selectAttrValue);
			}
		}
		return prop;
	}
	
	/**
	 * 查找指定Sn的称号
	 * @param humanObj
	 * @param titleSn
	 */
	public TitleVO getTitleVOBySn(HumanObject humanObj, int titleSn) {
		ConfAchieveTitle conf = ConfAchieveTitle.get(titleSn);
		if (conf == null) {
			return null;
		}
		// 取出对应类型称号
		AchieveTitleVO atVO = humanObj.achieveTitleMap.get(conf.type);
		if (atVO == null) {
			return null;
		}
		return atVO.getTitleVO(titleSn);
	}

	/**
	 * 查找指定type的称号成就
	 * @param humanObj
	 * @param type
	 */
	public AchieveTitleVO getAchieveTitleVOByType(HumanObject humanObj, int type) {
		// 取出对应类型称号
		AchieveTitleVO atVO = humanObj.achieveTitleMap.get(type);
		if (atVO == null) {
			return null;
		}
		return atVO;
	}
	
	/**
	 * 查找当前使用的称号VO
	 * @return TitleVO 称号VO
	 */
	public TitleVO getCurUseTitleVO(HumanObject humanObj) {
		int curSn = this.getCurUseTitileSn(humanObj);
		TitleVO to = this.getTitleVOBySn(humanObj, curSn);
		return to;
	}
	
	/**
	 * 查找当前使用的称号sn
	 * @return 称号sn
	 */
	public int getCurUseTitileSn(HumanObject humanObj) {
		int sn = humanObj.getHuman().getTitleSn();
		return sn;
	}

	/**
	 * 获取当前任务的参数
	 * @param sn
	 * @param titleSn
	 * @return
	 */
	public int getParamBySn(int sn, int titleSn) {
		ConfAchieveTitle conf = ConfAchieveTitle.get(titleSn);
		if (conf == null) {
			Log.table.error("=== 配置表AchieveTitle错误,sn={}", sn);
			return -1;
		}
		int[] param = conf.param;
		return param[0];
	}
	
}
