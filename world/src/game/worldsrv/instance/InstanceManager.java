package game.worldsrv.instance;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;
import game.msg.Define.DChapInfo;
import game.msg.Define.DInstEnd;
import game.msg.Define.DInstInfo;
import game.msg.Define.EAwardType;
import game.msg.Define.ECostGoldType;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EMapType;
import game.msg.Define.EMoneyType;
import game.msg.Define.ETeamType;
import game.msg.Define.EVipBuyType;
import game.msg.MsgInstance.SCInstAuto;
import game.msg.MsgInstance.SCInstEnd;
import game.msg.MsgInstance.SCInstFightNumReset;
import game.msg.MsgInstance.SCInstInfoAll;
import game.msg.MsgInstance.SCInstOpenBox;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfInstChapter;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.drop.DropBag;
import game.worldsrv.drop.DropManager;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Instance;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.param.ParamManager;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.stage.types.StageObjectInstance;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamManager;
import game.worldsrv.vip.VipManager;

/**
 * 单人副本
 * @author Administrator
 *
 */
public class InstanceManager extends ManagerBase {

	/**
	 * 获取实例
	 * @return
	 */
	public static InstanceManager inst() {
		return inst(InstanceManager.class);
	}
	
	/**
	 * 玩家其它数据加载开始：加载玩家的副本信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Instance.tableName);
		dbPrx.findBy(false, Instance.K.HumanId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanInst, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}
	private void _result_loadHumanInst(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanInst humanObj is null");
			return;
		}
		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanInst records=null");
		} else {
			// 加载数据
			for (Record rec : records) {
				Instance inst = new Instance(rec);
				humanObj.instancesMap.put(inst.getChapterSn(), inst);
			}
			
			if(humanObj.isDailyFirstLogin){
				resetDaily(humanObj);//每日重置副本记录信息
			} else {
				_send_SCInstInfoAll(humanObj);// 下发所有副本章节信息
			}
		}
		
		// 同步全局信息
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateInstStar(humanObj.getHumanId(), getAllStarCount(humanObj));
		
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}

	/**
	 * 每个整点执行一次
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置
			resetDaily(humanObj);//每日重置副本记录信息
		}
	}
	/**
	 * 每日重置副本记录信息
	 * @param humanObj
	 */
	private void resetDaily(HumanObject humanObj){
		for (Instance inst : humanObj.instancesMap.values()) {
			inst.setFightJSON("{}");// 重置已挑战次数
			inst.setResetJSON("{}");// 重置已重置次数
		}
		// 下发所有副本章节信息
		_send_SCInstInfoAll(humanObj);
	}
	
//	@Listener(EventKey.StageHumanEnter)
//	public void _listener_StageHumanEnter(Param params) {
//		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
//		if (humanObj == null) {
//			Log.game.error("===_listener_StageHumanEnter humanObj is null");
//			return;
//		}
//		if (!(humanObj.stageObj instanceof StageObjectInstance)) {
//			return;
//		}
//		
//		//StageObjectInstance stageObj = (StageObjectInstance) humanObj.stageObj;
//		// 发送副本掉落
//		//List<DTriggerDrop> triggerDrop = stageObj.listDTriggerDrop;
//		//SCInstTriggerDrop.Builder msg = SCInstTriggerDrop.newBuilder();
//		//msg.addAllTriggerDrop(triggerDrop);
//		//humanObj.sendMsg(msg);
//	}
	
	/**
	 * 下发所有副本章节信息
	 * @param humanObj
	 */
	public void _send_SCInstInfoAll(HumanObject humanObj) {
		SCInstInfoAll.Builder msg = SCInstInfoAll.newBuilder();
		for (Instance inst : humanObj.instancesMap.values()) {
			ConfInstChapter conf = ConfInstChapter.get(inst.getChapterSn());
			if (null == conf) {
				Log.table.error("ConfInstChapter no find sn={}", inst.getChapterSn());
				continue;
			}
			msg.addChapInfo(getDChapInfo(humanObj, inst));// 副本章节信息
		}
		humanObj.sendMsg(msg);
	}
	private DChapInfo getDChapInfo(HumanObject humanObj, Instance inst) {
		DChapInfo.Builder dChapInfo = DChapInfo.newBuilder();
		// 获取章节SN
		int chapSn = inst.getChapterSn();
		ConfInstChapter confInstChapter = ConfInstChapter.get(chapSn);
		if (confInstChapter == null) {
			Log.table.error("===ConfInstChapter is no find sn={}", chapSn);
			return dChapInfo.build();
		}
		dChapInfo.setChapSn(chapSn);
		// 获取章节宝箱领取状态
		JSONObject joBox = Utils.toJSONObject(inst.getBoxJSON());
		for (int star : confInstChapter.chapterStar) {
			int status = joBox.getIntValue(String.valueOf(star));
			dChapInfo.addBoxStatus(EAwardType.valueOf(status));
		}
		// 获取下属副本通关星数
		Map<Integer, Integer> mapStar = Utils.jsonToMapIntInt(inst.getStarJSON());
		int starAll = 0;// 总星数
		for (Integer star : mapStar.values()) {
			starAll += star;
		}
		dChapInfo.setStarAll(starAll);
		// 设置副本信息
		for (Entry<Integer, Integer> entry : mapStar.entrySet()) {
			int stageSn = entry.getKey();
			int star = entry.getValue();
			ConfInstStage conf = ConfInstStage.get(stageSn);
			if (conf == null) {
				Log.table.error("===ConfInstStage is no find sn={}", stageSn);
				continue;
			}
			DInstInfo.Builder dInstInfo = DInstInfo.newBuilder();
			dInstInfo.setInstSn(stageSn);
			dInstInfo.setStar(star);
			// 剩余挑战次数
			int numRemain = conf.fightNum;
			Integer numFight = Utils.getJSONValueInt(inst.getFightJSON(), stageSn);
			if (numFight != null) {
				numRemain -= numFight;
			}
			numRemain = (numRemain>0)?numRemain:0;
			dInstInfo.setFightNumRemain(numRemain);
			// 已重置次数
			Integer numReset = Utils.getJSONValueInt(inst.getResetJSON(), stageSn);
			if (numReset != null) {
				dInstInfo.setResetNum(numReset);
			} else {
				dInstInfo.setResetNum(0);
			}
			dChapInfo.addInstInfo(dInstInfo);
		}
		return dChapInfo.build();
	}
		
	/**
	 * 进入单人副本
	 * @param humanObj
	 * @param stageSn 关卡sn
	 */
	public void _msg_CSInstEnter(HumanObject humanObj, int stageSn) {
		int mapSn = 0;// 副本所在的地图sn
		// 判断副本配置是否合法
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (null == confInstStage) {
			Log.stageCommon.info("===进入单人副本错误：humanId={}, ConfInstStage.sn={}", humanObj.id, stageSn);
			// 发送文字提示消息 切换地图错误！
			humanObj.sendSysMsg(36);
			return;
		}
		mapSn = confInstStage.mapSN;
		// 判断是否满足单人副本进入条件
		if (confInstStage.chapterSN > 0) {// 章节副本
			boolean canEnter = canEnterRep(humanObj, confInstStage);
			if (!canEnter) {
				return;
			}
		}
		// 创建副本
		create(humanObj, stageSn, mapSn);
	}
	
	/**
	 * 创建副本
	 * @param humanObj
	 * @param stageSn
	 * @param mapSn
	 */
	private void create(HumanObject humanObj, int stageSn, int mapSn) {
		humanObj.setCreateRepTime();//进入异步前要先设置，避免重复操作
		
		// 创建副本
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageInstance(stageSn, mapSn, ECrossFightType.FIGHT_INSTANCE_VALUE);
		prx.listenResult(this::_result_create, "humanObj", humanObj, "mapSn", mapSn);
		// 派发进入副本事件
		Event.fire(EventKey.InstEnter, "humanObj", humanObj, "stageSn", stageSn);
	}
	private void _result_create(Param results, Param context) {
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		int mapSn = Utils.getParamValue(context, "mapSn", -1);
		HumanObject humanObj = Utils.getParamValue(context, HumanObject.paramKey, null);
		if (stageId < 0 || mapSn < 0 || null == humanObj) {
			Log.game.error("===创建失败：_result_create stageId={}, mapSn={}, humanObj={}", 
					stageId, mapSn, humanObj);
			return;
		}
		if (!humanObj.checkHumanSwitchState(results, context, true)) {
			return;
		}
		
		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_INSTANCE, mapSn, stageId);
	}
	
	/**
	 * 离开单人副本 自动回到副本进入前的主地图
	 * @param humanObj
	 */
	public void _msg_CSInstLeave(HumanObject humanObj) {
		if (humanObj.stageObj == null || !(humanObj.stageObj instanceof StageObjectInstance)) {
			Log.stageCommon.error(" _msg_CSInstLeave stage error ,humanId={},humanName={}",humanObj.getHumanId(),humanObj.getHuman().getName());
			return;
		}
		
		if (humanObj.getTeamId() > 0) {// 离开单人副本则清除组队ID
			TeamManager.inst()._msg_CSTeamLeave(humanObj);
		}
		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);
	}

	public boolean canEnterRep(HumanObject humanObj, ConfInstStage confInstStage) {
		return canEnterRep(humanObj, confInstStage, 1);
	}

	/**
	 * 判断是否可以进入副本（单人副本才需进来判断剩余次数以及其他条件）
	 */
	public boolean canEnterRep(HumanObject humanObj, ConfInstStage confInstStage, int num) {
		boolean result = true;
		if (confInstStage == null) {
			Log.table.error("confInstStage is null");
			return false;
		}

		Human human = humanObj.getHuman();
		// 判断是否在common地图中
		if (!humanObj.stageObj.confMap.type.equals(EMapType.common.name())) {
			// 发送文字提示消息 不在主地图中！
			humanObj.sendSysMsg(33);
			return false;
		}

		// 获得当前的章节配置
		Instance inst = humanObj.instancesMap.get(confInstStage.chapterSN);
		if (inst == null) {
			inst = newInstance(humanObj, confInstStage.chapterSN);// 添加单人副本内容到数据库
		}

		// 判断是否通关前置关卡
		if (!isPassPreStage(humanObj, confInstStage)) {
			Log.human.debug("===进入副本关卡stageSn={}失败，前置关卡preStageSN={}没通过！", confInstStage.sn, confInstStage.preStageSN);
			return false;
		}
		// 判断体力
		if (human.getAct() < confInstStage.needManual * num) {
			// 发送文字提示消息 体力不足！
			humanObj.sendSysMsg(7);
			return false;
		}
		// 判断等级
		if(human.getLevel() < confInstStage.needLevel) {
			//发送文字提示消息 等级不足！
			humanObj.sendSysMsg(34);
			return false;
		}
		// 判断副本剩余次数
		Integer fightNum = Utils.getJSONValueInt(inst.getFightJSON(), confInstStage.sn);
		if (fightNum != null && fightNum + num > confInstStage.fightNum) {
			// 发送文字提示消息 战斗次数不够
			humanObj.sendSysMsg(56);
			return false;
		}
		return result;
	}

	/**
	 * 扫荡通关副本
	 * @param humanObj
	 * @param stageSn
	 * @param num
	 */
	public void _msg_CSInstAuto(HumanObject humanObj, int stageSn, int num) {
		if (num <= 0) {
			return;
		}
		// 获得副本配置
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={} ", stageSn);
			return;
		}
		Instance inst = humanObj.instancesMap.get(confInstStage.chapterSN);
		if (inst == null) {
			Log.human.error("扫荡报错：instancesMap no find chapterSN={}", confInstStage.chapterSN);
			return;
		}
		// 判断副本剩余次数
		Integer fightNum = Utils.getJSONValueInt(inst.getFightJSON(), confInstStage.sn);
		if (fightNum != null && fightNum + num > confInstStage.fightNum) {
			// 发送文字提示消息 战斗次数不够
			humanObj.sendSysMsg(56);
			return;
		}
		
		//TODO 判断VIP扫荡副本权利
		
		// 判断是否3星通关过副本
		if (getInstStar(humanObj, confInstStage) < 3) {
			humanObj.sendSysMsg(220901);// 未满星通关副本，不能扫荡！
			return;
		}

		// 判断能不能打
		if (!canEnterRep(humanObj, confInstStage, num)) {
			return;
		}
		
		// 扫荡副本通关处理
		if (confInstStage.chapterSN > 0) {
			instAuto(LogSysModType.InstAuto, humanObj, confInstStage, num);
		}
	}
	/**
	 * 扫荡通关副本：修改副本进度及给副本奖励相关
	 * @param num 扫荡次数
	 */
	private void instAuto(LogSysModType logType, HumanObject humanObj, ConfInstStage confInstStage, int num) {
		int stageSn = confInstStage.sn;
		String stageSnStr = String.valueOf(stageSn);
		// 扣除体力
		int costActValue = confInstStage.needManual;
		if (costActValue > 0) {
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.act_VALUE, costActValue * num, logType)) {
				return;
			}
		}
		
		SCInstAuto.Builder msg = SCInstAuto.newBuilder();
		msg.setInstSn(stageSn);
		// 获取副本信息记录
		Instance inst = humanObj.instancesMap.get(confInstStage.chapterSN);
		if (inst == null) {
			inst = newInstance(humanObj, confInstStage.chapterSN);// 添加单人副本内容到数据库
		}
		// 修改副本已挑战次数
		JSONObject joFight = Utils.toJSONObject(inst.getFightJSON());
		int fight = joFight.getIntValue(stageSnStr);
		fight += num;
		joFight.put(stageSnStr, fight);
		inst.setFightJSON(joFight.toJSONString());
		// 设置副本剩余挑战次数
		msg.setFightNumRemain(confInstStage.fightNum - fight);
		
		// 副本掉落
		for (int index = 1; index <= num; index++) {
			DInstEnd.Builder dInstEnd = null;
			dInstEnd = giveInstReward(humanObj, confInstStage, index);
			msg.addInstEnd(dInstEnd.build());// 副本结束奖励
		}
		// 返回消息
		humanObj.sendMsg(msg);
		
		// 发布扫荡副本消息
		Event.fire(EventKey.InstAnyPass, "humanObj", humanObj, "num", num, "stageSn", stageSn);//任意副本通关			
		Event.fire(EventKey.InstAutoPass, "humanObj", humanObj, "num", num, "stageSn", stageSn);
	}
	
	/**
	 * 手动通关副本
	 * @param humanObj
	 */
	public void _msg_CSInstEnd(HumanObject humanObj, boolean isFail, List<Integer> stars) {
		StageObjectInstance stageObj = null;
		if (humanObj.stageObj instanceof StageObjectInstance) {
			stageObj = (StageObjectInstance) (humanObj.stageObj);
			if (stageObj == null)
				return;
		} else {
			return;// 不在副本里
		}
		
		int stageSn = stageObj.stageSn;
		String stageSnStr = String.valueOf(stageSn);
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={}", stageSn);
			return;
		}
		ConfInstChapter confInstChapter = ConfInstChapter.get(confInstStage.chapterSN);
		if (confInstChapter == null) {
			Log.table.error("===ConfInstChapter配表错误，no find sn={}", confInstStage.chapterSN);
			return;
		}
		
		stageObj.isPass = true;// 设置副本通关
		stageObj.milliSecEnd = Port.getTime();// 记录副本结束时间戳
		// 计算通关副本所用秒数
		int second = (int) (stageObj.milliSecEnd - stageObj.createTime) / Utils.I1000;
		int num = 1;// 次数
		LogSysModType logType = LogSysModType.Inst;
		SCInstEnd.Builder msg = SCInstEnd.newBuilder();
		if (isFail) {
			DInstEnd.Builder dInstEnd = DInstEnd.newBuilder();
			dInstEnd.setInstSn(stageSn);
			msg.setInstEnd(dInstEnd);
			msg.setStar(0);// 0失败
			humanObj.sendMsg(msg);
			return;
		}
		
		// 成功通关副本，则扣除体力，给予奖励
		int costActValue = confInstStage.needManual;
		if (costActValue > 0) {
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.act_VALUE, costActValue * num, logType)) {
				return;
			}
		}
			
		boolean isFirst = false;// 是否第一次击打
		if (!isPassInstStage(humanObj, confInstStage)) {
			isFirst = true;
		}
		// 非活动副本中的单人副本才需要修改数据库的副本信息记录
		Instance inst = humanObj.instancesMap.get(confInstStage.chapterSN);
		if (inst == null) {
			inst = newInstance(humanObj, confInstStage.chapterSN);// 添加单人副本内容到数据库
		}
		// 修改副本已挑战次数
		if (confInstStage.fightNum > 0) {
			JSONObject joFight = Utils.toJSONObject(inst.getFightJSON());
			int fight = joFight.getIntValue(stageSnStr);
			fight++;
			joFight.put(stageSnStr, fight);
			inst.setFightJSON(joFight.toJSONString());
			// 设置副本剩余挑战次数
			msg.setFightNumRemain(confInstStage.fightNum - fight);
		}
		// 手动通关处理通关副本星级
		JSONObject joStar = Utils.toJSONObject(inst.getStarJSON());
		int starOld = joStar.getIntValue(stageSnStr);
		int starNew = countStars(stars);
		msg.setStarStatus(Utils.intListToStr(stars));// 副本通关星数状态：1,1,1
		msg.setStar(starNew);// 副本通关星数
		int starAll = inst.getStarAll();
		if (starOld < starNew) {
			// 修改并保存副本星数及总星数
			joStar.put(stageSnStr, starNew);
			inst.setStarJSON(joStar.toJSONString());
			starAll += (starNew - starOld);
			inst.setStarAll(starAll);// 总星数
			msg.setStarAll(starAll);// 章节总通关星数
		} else {
			msg.setStarAll(starAll);// 章节总通关星数
		}
		
		// 修改并保存章节宝箱领取状态：0不能领，1可领取，2已领取
		JSONObject joBox = Utils.toJSONObject(inst.getBoxJSON());
		for (int star : confInstChapter.chapterStar) {
			String starStr = String.valueOf(star);
			int status = joBox.getIntValue(starStr);
			if (status == EAwardType.AwardNot_VALUE && starAll >= star) {
				joBox.put(starStr, EAwardType.Awarding_VALUE);
				msg.addBoxStatus(EAwardType.Awarding);// 章节箱子领取状态标记
			} else {
				msg.addBoxStatus(EAwardType.valueOf(status));// 章节箱子领取状态标记
			}
		}
		inst.setBoxJSON(joBox.toJSONString());
		
		// 副本掉落
		DInstEnd.Builder dInstEnd = null;
		if (isFirst) {// 首次通关
			dInstEnd = giveInstRewardFirst(humanObj, confInstStage);
			// 副本首次过关
			Event.fire(EventKey.InstFirstPass, "humanObj", humanObj, "num", num, "stageSn", stageSn);
		} else {
			dInstEnd = giveInstReward(humanObj, confInstStage, num);
		}
		msg.setInstEnd(dInstEnd.build());// 副本结束奖励
		
		// 发布通关副本事件
		Event.fire(EventKey.InstAnyPass, "humanObj", humanObj, "num", num, "stageSn", stageSn);
		
		// 返回消息
		humanObj.sendMsg(msg);
	}
	/**
	 * 计算副本星数
	 */
	public int countStars(List<Integer> stars) {
		int ret = 0;
		for(int i : stars) {
			if(i == 1) {
				ret += 1;
			}
		}
		return ret;
	}
		
	/**
	 * 非首次通关奖励
	 */
	public DInstEnd.Builder giveInstReward(HumanObject humanObj, ConfInstStage confInstStage, int index) {
		DInstEnd.Builder dInstEnd = DInstEnd.newBuilder();
		Human human = humanObj.getHuman();
		int stageSn = confInstStage.sn;
		LogSysModType logType = LogSysModType.InstDropRand;
		// 副本结束
		dInstEnd.setInstSn(stageSn);// 副本sn
		dInstEnd.setIndex(index);// 第几次副本产出
		dInstEnd.setLevel(human.getLevel());// 玩家当前等级
		dInstEnd.setExp(confInstStage.exp);// 获得经验
		dInstEnd.setCoin(confInstStage.coin);// 获得金币
		// 给经验和金币
		int[] items = {EMoneyType.exp_VALUE, EMoneyType.coin_VALUE};
		// 获得经验 = 当前等级 * 经验系数 
		int exp = confInstStage.exp * human.getLevel();;
		int[] nums = {exp, confInstStage.coin};
		RewardHelper.reward(humanObj, items, nums, logType);
		// 随机掉落物品
		DropBag dropBag = DropManager.inst().getItem(humanObj, confInstStage.dropInfosSN);
		if (dropBag != null && !dropBag.isEmpty()) {
			ItemChange dropRand = RewardHelper.reward(humanObj, dropBag.getItemSn(), dropBag.getItemNum(), logType);
			if (dropRand != null)
				dInstEnd.addAllProducesRand(dropRand.getProduce());// 随机掉落奖励
		}
		return dInstEnd;
	}
	
	/**
	 * 首次通关奖励
	 */
	public DInstEnd.Builder giveInstRewardFirst(HumanObject humanObj, ConfInstStage confInstStage) {
		DInstEnd.Builder dInstEnd = DInstEnd.newBuilder();
		Human human = humanObj.getHuman();
		int stageSn = confInstStage.sn;
		LogSysModType logType = LogSysModType.InstDropFirst;
		// 副本结束
		dInstEnd.setInstSn(stageSn);// 副本sn
		dInstEnd.setIndex(1);// 第几次副本产出
		dInstEnd.setLevel(human.getLevel());// 玩家当前等级
		dInstEnd.setExp(confInstStage.exp);// 获得经验
		dInstEnd.setCoin(confInstStage.coin);// 获得金币
		// 给经验和金币
		int[] items = {EMoneyType.exp_VALUE, EMoneyType.coin_VALUE};
		// 获得经验 = 当前等级 * 经验系数 
		int exp = confInstStage.exp * human.getLevel();
		int[] nums = {exp, confInstStage.coin};
		RewardHelper.reward(humanObj, items, nums, logType);
		// 随机掉落物品
		DropBag dropBag = DropManager.inst().getItem(humanObj, confInstStage.dropInfosSN);
		if (dropBag != null && !dropBag.isEmpty()) {
			ItemChange dropRand = RewardHelper.reward(humanObj, dropBag.getItemSn(), dropBag.getItemNum(), logType);
			if (dropRand != null) {
				dInstEnd.addAllProducesRand(dropRand.getProduce());// 随机掉落奖励
			}
		}
		// 首次通关掉落
		ConfRewards confRewards = ConfRewards.get(confInstStage.rewardsSN);
		if (confRewards == null) {
			Log.table.error("===配置表错误ConfRewards no find sn={}", confInstStage.rewardsSN);
		} else {
			ItemChange dropFirst = RewardHelper.reward(humanObj, confRewards.itemSn, confRewards.itemNum, logType);
			dInstEnd.addAllProducesFirst(dropFirst.getProduce());// 首次通关奖励
		}
		return dInstEnd;
	}
	
	/**
	 * 是否通关前置关卡
	 * @param humanObj
	 * @param confInstStage 本关卡配置
	 * @return
	 */
	private boolean isPassPreStage(HumanObject humanObj, ConfInstStage confInstStage) {
		if (confInstStage == null)
			return false;// 不可打

		boolean ret = false;
		int preStageSN = confInstStage.preStageSN;
		if (preStageSN == 0) {
			ret = true;// 前置副本为0即可打的
		} else {
			ConfInstStage conf = ConfInstStage.get(preStageSN);
			if (conf != null && conf.chapterSN > 0) {
				Instance inst = humanObj.instancesMap.get(conf.chapterSN);
				if (inst != null) {
					// 前置关卡的章节的星星记录
					JSONObject joStar = Utils.toJSONObject(inst.getStarJSON());
					int preStar = joStar.getIntValue(String.valueOf(preStageSN));
					if (preStar > 0)
						ret = true;
				}
			}
		}
		return ret;
	}
	
	/**
	 * 是否已通关副本关卡
	 * @param humanObj
	 * @param snStage 关卡sn
	 * @return
	 */
	public boolean isPassRepStage(HumanObject humanObj, int snStage) {
		ConfInstStage confInstStage = ConfInstStage.get(snStage);
		return isPassInstStage(humanObj, confInstStage);
	}
	
	/**
	 * 是否已通关副本关卡
	 * @param humanObj
	 * @param confInstStage 关卡配置
	 * @return
	 */
	public boolean isPassInstStage(HumanObject humanObj, ConfInstStage confInstStage) {
		boolean isPass = false;// 默认未通关副本
		if (confInstStage != null) {
			Instance inst = humanObj.instancesMap.get(confInstStage.chapterSN);
			if (inst != null) {
				if (inst.getStarJSON().contains(String.valueOf(confInstStage.sn))) {
					isPass = true;
				}
			}
		}
		return isPass;
	}

	/**
	 * 重置副本挑战次数
	 */
	public void _msg_CSInstFightNumReset(HumanObject humanObj, int stageSn) {
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("===ConfInstStage配表错误，no find sn={}", stageSn);
			return;
		}
		
		// 获得当前的章节配置
		Instance inst = humanObj.instancesMap.get(confInstStage.chapterSN);
		if (inst == null) {
			Log.game.error("===重置副本错误：humanObj.instancesMap no find chapterSN={}", confInstStage.chapterSN);
			return;
		}
		
		// 判断副本是否打通关过
		if (getInstStar(humanObj, confInstStage) == 0) {
			return;
		}
				
		String stageSnStr = String.valueOf(confInstStage.sn);
		// 判断今日已挑战次数
		JSONObject joFight = Utils.toJSONObject(inst.getFightJSON());
		int numFight = joFight.getIntValue(stageSnStr);
		if (numFight < confInstStage.fightNum) {
			return;// 还有次数没用完
		}
		// 判断VIP每日重置副本次数
		JSONObject joReset = Utils.toJSONObject(inst.getResetJSON());
		int numReset = joReset.getIntValue(stageSnStr);
		if(!VipManager.inst().checkVipBuyNum(EVipBuyType.instResetNum, numReset, humanObj.getHuman().getVipLevel())){
			humanObj.sendSysMsg(14);// 今日可使用次数已用完！
			return;
		}
		
		// 扣元宝
		int costGold = RewardHelper.getCostGold(ECostGoldType.instResetCost, numReset+1);
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costGold, LogSysModType.InstResetFightNum)) {
			return;
		}
		
		// 修改并保存已重置次数
		numReset++;
		joReset.put(stageSnStr, numReset);
		inst.setResetJSON(joReset.toJSONString());
		// 修改并保存已挑战次数
		joFight.put(stageSnStr, 0);
		inst.setFightJSON(joFight.toJSONString());

		// 返回副本章节消息
		SCInstFightNumReset.Builder msg = SCInstFightNumReset.newBuilder();
		msg.setInstSn(stageSn);
		msg.setFightNumRemain(confInstStage.fightNum);// 剩余挑战次数
		msg.setResetNum(numReset);// 已重置次数
		humanObj.sendMsg(msg);
	}

	/**
	 * 副本开宝箱
	 * @param humanObj
	 * @param chapSn 章节sn
	 * @param index 打开宝箱索引（发0,1,2代表第1,2,3个箱子）
	 */
	public void _msg_CSInstOpenBox(HumanObject humanObj, int chapSn, int index) {
		ConfInstChapter confInstChapter = ConfInstChapter.get(chapSn);
		if (confInstChapter == null) {
			Log.table.error("===ConfInstChapter配表错误，no find sn={}", chapSn);
			return;
		}
		if (confInstChapter.chapterStar == null) {
			Log.table.error("===confInstChapter.chapterStar == null");
			return;
		}
		if (index < 0 || index >= confInstChapter.chapterStar.length) {
			Log.table.error("===chapterStar no find index={}", index);
			return;
		}
		if (confInstChapter.chapterStar.length != confInstChapter.rewardsSN.length) {
			Log.table.error("===chapterStar.length != rewardsSN.length");
			return;
		}
		
		// 获得当前的章节配置
		Instance inst = humanObj.instancesMap.get(chapSn);
		if (inst == null) {
			Log.human.info("===humanObj.instancesMap no find chapSn={}", chapSn);
			return;
		}
		
		int starNeed = confInstChapter.chapterStar[index];
		String starStr = String.valueOf(starNeed);
		// 判断是否已经领取过了
		JSONObject joBox = Utils.toJSONObject(inst.getBoxJSON());
		int boxStatus = joBox.getIntValue(starStr);
		if (boxStatus == EAwardType.Awarded_VALUE) {
			humanObj.sendSysMsg(67);// 已领取过，不可重复领取！
			return;
		}
		
		// 判断星数够不够
		int starAll = inst.getStarAll();
		if (starAll < starNeed) {
			humanObj.sendSysMsg(221301);// 副本星数不足，不可领取！
			if (boxStatus != EAwardType.AwardNot_VALUE) {
				joBox.put(starStr, EAwardType.AwardNot_VALUE);
				inst.setBoxJSON(joBox.toJSONString());
			}
			return;
		}
		
		// 星数够，可领取
		if (boxStatus != EAwardType.Awarding_VALUE) {
			joBox.put(starStr, EAwardType.Awarding_VALUE);
			inst.setBoxJSON(joBox.toJSONString());
		}
		// 给物品
		ConfRewards confRewards = ConfRewards.get(confInstChapter.rewardsSN[index]);
		if (confRewards == null) {
			Log.table.error("===配置表错误ConfRewards no find sn={}", confInstChapter.rewardsSN[index]);
			return;
		}
		
		ItemChange drop = RewardHelper.reward(humanObj, confRewards.itemSn, confRewards.itemNum, LogSysModType.InstStarBox);
		// 修改并保存章节宝箱领取状态
		joBox.put(starStr, EAwardType.Awarded_VALUE);
		inst.setBoxJSON(joBox.toJSONString());
		
		// 返回消息
		SCInstOpenBox.Builder msg = SCInstOpenBox.newBuilder();
		msg.setChapSn(chapSn);
		msg.addAllProduces(drop.getProduce());// 随机掉落奖励
		for (int star : confInstChapter.chapterStar) {
			int status = joBox.getIntValue(String.valueOf(star));
			msg.addBoxStatus(EAwardType.valueOf(status));
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 获得玩家所有副本的星星数量
	 * @param humanObj
	 */
	public int getAllStarCount(HumanObject humanObj) {
		int star = 0;
		for (Instance inst : humanObj.instancesMap.values()) {
			star += inst.getStarAll();
//			JSONObject ja = Utils.toJSONObject(inst.getStarJSON());
//			for (String key : ja.keySet()) {
//				star += ja.getIntValue(key);
//			}
		}
		return star;
	}

	/**
	 * 获取副本通关星数
	 */
	public int getInstStar(HumanObject humanObj, int stageSn) {
		int star = 0;
		ConfInstStage confInstStage = ConfInstStage.get(stageSn);
		if (confInstStage == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={} ", stageSn);
		} else {
			star = getInstStar(humanObj, confInstStage);
		}
		return star;
	}
	public int getInstStar(HumanObject humanObj, ConfInstStage confInstStage) {
		int star = 0;
		Instance inst = humanObj.instancesMap.get(confInstStage.chapterSN);
		if (inst != null) {
			Integer st = Utils.getJSONValueInt(inst.getStarJSON(), confInstStage.sn);
			if (st != null)
				star = st;
		}
		return star;
	}
	
	/**
	 * 获取副本章节总通关星数
	 */
	public int getInstChapStarAll(HumanObject humanObj, int chapterSn) {
		int starAll = 0;// 总星数
		Instance inst = humanObj.instancesMap.get(chapterSn);
		if (inst != null) {
			starAll += inst.getStarAll();
//			Map<Integer, Integer> mapStar = Utils.jsonToMapIntInt(inst.getStarJSON());
//			for (Integer star : mapStar.values()) {
//				starAll += star;
//			}
		}
		return starAll;
	}
	
	/**
	 * 是否全三星通关了指定副本章节
	 * @return
	 */
	public boolean isInstChapPassPerfect(HumanObject humanObj, int chapterSn) {
		ConfInstChapter conf = ConfInstChapter.get(chapterSn);
		if (null == conf) {
			Log.table.error("ConfInstChapter no find sn={}", chapterSn);
			return false;
		}
		boolean pass = false;
		int starAll = getInstChapStarAll(humanObj, chapterSn);
		if (starAll >= conf.levelNum * 3) {// 关卡总数*3
			pass = true;
		}
		return pass;
	}
	
	/**
	 * 是否三星通关了指定副本
	 * @return
	 */
	public boolean isInstPassPerfect(HumanObject humanObj, int stageSn) {
		ConfInstStage conf = ConfInstStage.get(stageSn);
		if (null == conf) {
			Log.table.error("ConfInstStage no find sn={}", stageSn);
			return false;
		}
		boolean pass = false;
		int star = getInstStar(humanObj, conf);
		if (star >= 3) {
			pass = true;
		}
		return pass;
	}
	/**
	 * 是否通关了指定副本
	 * @return
	 */
	public boolean isInstPass(HumanObject humanObj, int stageSn) {
        ConfInstStage conf = ConfInstStage.get(stageSn);
        if (null == conf) {
            Log.table.debug("ConfInstStage no find sn={}", stageSn);
            return false;
        }
        boolean pass = false;
        int star = getInstStar(humanObj, conf);
        if (star > 0) {
            pass = true;
        }
        return pass;
    }

	/**
	 * 添加单人副本内容到数据库及HumanObject
	 * @param humanObj
	 * @param chapSN 章节SN
	 * @return
	 */
	public Instance newInstance(HumanObject humanObj, int chapSN) {
		Instance inst = new Instance();
		inst.setId(Port.applyId());
		inst.setHumanId(humanObj.id);
		inst.setChapterSn(chapSN);
		inst.persist();
		// 保存副本信息
		if (chapSN > 0) {// 章节副本
			humanObj.instancesMap.put(chapSN, inst);
		}
		return inst;
	}
		
	/**
	 * 删除与角色相关的副本信息
	 * @param humanId
	 */
	public void deleteAllInstInfo(long humanId) {
		// String sql = Utils.createStr("delete from {} where `{}` = ?",
		// Inst.tableName, Inst.K.humanId);
		// dbSP.execute(false, false, sql, humanId);

		String sql = Utils.createStr("update `{}` set `{}` = 1 where `{}` = ?", Instance.tableName,
				Instance.K.DeleteRole, Instance.K.HumanId);
		DB dbPrx = DB.newInstance(Instance.tableName);
		dbPrx.sql(false, false, sql, humanId);
	}
		
}
