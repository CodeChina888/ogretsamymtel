package game.worldsrv.immortalCave;

import game.msg.Define.DCaveBase;
import game.msg.Define.DCaveDefense;
import game.msg.Define.DCaveGuildMemberInfo;
import game.msg.Define.DProduce;
import game.msg.Define.ECaveType;
import game.msg.Define.ECostGoldType;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EMailType;
import game.msg.Define.EManType;
import game.msg.Define.EModeType;
import game.msg.Define.EMoneyType;
import game.msg.Define.ETeamType;
import game.msg.Define.ETokenType;
import game.msg.Define.EVipBuyType;
import game.msg.MsgCave.SCCaveCDTimeAdd;
import game.msg.MsgCave.SCCaveDefense;
import game.msg.MsgCave.SCCaveEnemy;
import game.msg.MsgCave.SCCaveEnemyInfo;
import game.msg.MsgCave.SCCaveFightEnd;
import game.msg.MsgCave.SCCaveGiveUp;
import game.msg.MsgCave.SCCaveGuildMemberInfo;
import game.msg.MsgCave.SCCaveInfo;
import game.msg.MsgCave.SCCaveMoneyInfo;
import game.msg.MsgCave.SCCaveOccupyInfo;
import game.msg.MsgCave.SCGetFreeCave;
import game.msg.MsgCave.SCMyCaveInfo;
import game.msg.MsgCave.SCMyCaveLost;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfCave;
import game.worldsrv.config.ConfCaveField;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfMap;
import game.worldsrv.entity.Cave;
import game.worldsrv.entity.CaveHuman;
import game.worldsrv.entity.CaveLog;
import game.worldsrv.entity.CavePartner;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.MergeCave;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.fightParam.CaveParam;
import game.worldsrv.guild.GuildData;
import game.worldsrv.guild.GuildServiceProxy;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.mail.MailManager;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.stage.StageManager;
import game.worldsrv.stage.StageServiceProxy;
import game.worldsrv.stage.types.StageObjectCave;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.vip.VipManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Distr;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

/**
 * 仙域
 * 
 * @author songy
 *
 */
public class CaveManager extends ManagerBase {

	public static final int CAN_NOT_MORE_THAN_TWO = 0;// 占领仙府数量不能超过2

	public static final int PLEASE_CHOOSE_OTHER = 1;// 请选择其他类型

	public static CaveManager inst() {
		return inst(CaveManager.class);
	}

	@Listener(EventKey.MergeOption)
	public void _listener_MergeOption(Param param) {
		DB db = DB.newInstance(MergeCave.tableName);
		db.findBy(false);
		db.listenResult(this::_result_sendRewardsAfterMerged);
//		Log.game.error("合服后处理仙域奖励发放：-----------------");
//		CaveServiceProxy prx = CaveServiceProxy.newInstance();
//		if (prx == null) {
//			Log.game.error("合服后处理仙域奖励发放：-----------------");
//		}
//		prx.sendRewardsAfterMerged();
//		Log.game.error("合服后处理仙域奖励发放：-----------------");
	}
	
	/**
	 * 合服后启服结算仙域奖励
	 * @param returns
	 * @param context
	 */
	private void _result_sendRewardsAfterMerged(Param returns, Param context) {
		List<Record> records = returns.get();
		if (records!=null && records.isEmpty()==false) {
			Map<Integer, List<CaveObject>> type1Caves = new HashMap<>();
			Map<Integer, List<CaveObject>> type2Caves = new HashMap<>();
			List<Object[]> list = new ArrayList<>(records.size());
			records.forEach(record-> {
				MergeCave mergeCave = new MergeCave(record);
				JSONObject jsonObject = Utils.toJSONObject(mergeCave.getCaveJsonStr());
				Long guildId = jsonObject.getLong("guildId");
				Cave cave = new Cave();
				cave.setBeRobNum(jsonObject.getIntValue(Cave.K.BeRobNum));
				cave.setCombat(jsonObject.getIntValue(Cave.K.Combat));
				cave.setDelayCount(jsonObject.getIntValue(Cave.K.DelayCount));
				cave.setExpOwnTime(jsonObject.getLongValue(Cave.K.ExpOwnTime));
				cave.setHumanID(jsonObject.getLongValue(Cave.K.HumanID));
				cave.setId(jsonObject.getIntValue(Cave.K.id));
				cave.setIndex(jsonObject.getIntValue(Cave.K.Index));
				cave.setIsOwn(jsonObject.getBooleanValue(Cave.K.isOwn));
				cave.setName(jsonObject.getString(Cave.K.name));
				cave.setOwnTime(jsonObject.getLongValue(Cave.K.ownTime));
				cave.setPage(jsonObject.getIntValue(Cave.K.Page));
				cave.setRobCount(jsonObject.getIntValue(Cave.K.RobCount));
				cave.setRobHumanList(jsonObject.getString(Cave.K.RobHumanList));
				cave.setSn(jsonObject.getIntValue(Cave.K.Sn));
				cave.setType(jsonObject.getIntValue(Cave.K.Type));
				if (cave.getType() == 1) {
					List<CaveObject>  type1CavesList = type1Caves.get(cave.getPage());
					if (type1CavesList == null) {
						type1CavesList = new ArrayList<>();
						type1Caves.put(cave.getPage(), type1CavesList);
					}
					type1CavesList.add(new CaveObject(cave, null, cave.getHumanID()));
				} else if (cave.getType() == 2) {
					List<CaveObject>  type2CavesList = type2Caves.get(cave.getPage());
					if (type2CavesList == null) {
						type2CavesList = new ArrayList<>();
						type2Caves.put(cave.getPage(), type2CavesList);
					}
					type2CavesList.add(new CaveObject(cave, null, cave.getHumanID()));
				}
				list.add(new Object[]{mergeCave, guildId, cave});
			});
			list.forEach(arr-> {
				MergeCave mergeCave = (MergeCave) arr[0];
				Long guildId = (Long) arr[1];
				Cave cave = (Cave) arr[2];
				if (mergeCave.getFlag() == 0) {
					doSendRewardsAfterMergedOneByOne(cave, guildId==null?0:guildId, cave.getType()==1?type1Caves:type2Caves);
					mergeCave.setFlag(1);
					mergeCave.update();
				} 
			});
		}
	}
	
	private void doSendRewardsAfterMergedOneByOne(Cave cave, long guildId, Map<Integer, List<CaveObject>> pages) {
		// id校验正确才能结算
		//当前仙府玩家单位时长收益x占领时长
		int baseNum = CaveManager.inst().baseAccpunt(cave, cave.getExpOwnTime());
		
		//帮会加成
		int allyNum = getAllyNum(cave.getPage(), guildId, pages);
		Integer allyAddPercentage = ParamManager.domainGuildAddRatio.get(allyNum);
		if(allyAddPercentage == null) {
			allyAddPercentage = 100;
		}
		
		baseNum = (baseNum*(allyAddPercentage)/100)-cave.getBeRobNum();//扣除抢夺
		
		
		//额外收益(宝箱)
		List<ProduceVo> extra = CaveManager.inst().extraAccounts(cave);
		extra.add(new ProduceVo(EMoneyType.coin_VALUE, baseNum));
		
		String caveName = "";
		ConfCave caveConf = ConfCave.get(cave.getSn());
		if (caveConf != null) {
			caveName = caveConf.name;
		}
		long humanId = cave.getHumanID();
		if(baseNum >0) {
			// 下发邮件
			String detail = "{" + EMailType.MailCaveDevelopment_VALUE + "|" + caveName + "}";
			MailManager.inst().sendSysMailNoNotify(humanId, ParamManager.mailMark, detail, extra, MailManager.DEL_TIMESTAMP);
		}
	}
	
	public int getAllyNum(int page,long guildId, Map<Integer, List<CaveObject>> maps) {
		int num = 0;
		if (maps != null) {
			List<CaveObject> list = maps.get(page);
			if (list != null) {
				for(CaveObject caobj:list) {
					long id = caobj.getGuildId();
					if(id == guildId) {
						num++;
					}
				}
			}
		}
		return num;
	}
	
	@Listener(EventKey.HumanDataLoadAllFinish)
	public void _listener_HumanDataLoadAllFinish(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		getMyCaveInfo(humanObj);
	}

	/**
	 * 获取仙府信息
	 * 
	 * @param humanObj
	 */
	public void getCSCaveInfo(HumanObject humanObj, int type, List<Integer> pageList) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		long humanId = humanObj.getHumanId();
		prx.getInfo(humanId, type, pageList);
		prx.listenResult(this::_result_getCSCaveInfo, "humanObj", humanObj);
	}

	/**
	 * 获取仙府信息回调
	 * 
	 */
	private void _result_getCSCaveInfo(Param results, Param context) {
		List<CaveObject> caveObj = results.get("caveObjList");
		Log.game.info("=================calist size" + caveObj.size());
		HumanObject humanObj = context.get("humanObj");
		List<Long> foeList = results.get("foeList");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		sendCaveMsg(humanObj, caveObj, foeList);
	}

	/**
	 * 发送洞府信息
	 * 
	 * @param humanObj
	 * @return
	 */

	public void sendCaveMsg(HumanObject humanObj, List<CaveObject> caveObjList, List<Long> foeList) {
		SCCaveInfo.Builder msg = SCCaveInfo.newBuilder();
		msg.addAllBaseMsg(getCaveMsg(humanObj, caveObjList, foeList));
		humanObj.sendMsg(msg);
	}

	/**
	 * 获取洞府信息
	 * 
	 * @param humanObj
	 * foeList 敌人列表
	 * @return
	 */
	private List<DCaveBase> getCaveMsg(HumanObject humanObj, List<CaveObject> caveObjList, List<Long> foeList) {
		List<DCaveBase> dinfoList = new ArrayList<>();
		if (caveObjList == null) {
			return dinfoList;
		}

		for (CaveObject caveObj : caveObjList) {

			DCaveBase.Builder dinfo = DCaveBase.newBuilder();
			Cave cave = caveObj.getCave();
			int type = cave.getType();
			dinfo.setCaveSn(cave.getSn());
			dinfo.setCdTimeCount(cave.getDelayCount());
			int useTime = (int) ((Port.getTime() - cave.getOwnTime()) / Utils.I1000);
			dinfo.setUseTime(useTime);
			dinfo.setIsOwn(cave.isIsOwn());
			dinfo.setIndex(cave.getIndex());
			
			
			dinfo.setGuildID(caveObj.getGuildId());
			if(caveObj.getMirrorhuman()!= null && caveObj.getMirrorhuman().getCaveHuman() != null) {
				dinfo.setGuildName(caveObj.getMirrorhuman().getCaveHuman().getGuiLdName());
			}
			dinfo.setCombat(cave.getCombat());
			dinfo.setPage(cave.getPage());
			dinfo.setCaveType(ECaveType.valueOf(type));
			dinfo.setHumanId(cave.getHumanID());
			dinfo.setHumanName(cave.getName());
			
			dinfo.setManType(EManType.Stateless);
			if (humanObj.getHuman().getGuildId() != 0 && humanObj.getHuman().getGuildId() == caveObj.getGuildId()) {
				dinfo.setManType(EManType.Alliance);
			}
			if (foeList.contains(cave.getHumanID())) {
				dinfo.setManType(EManType.Foe);
			}
			if (humanObj.getHuman().getGuildId() != 0 && humanObj.getHuman().getGuildId() == caveObj.getGuildId()
					&& foeList.contains(cave.getHumanID())) {
				dinfo.setManType(EManType.Allianceandfoe);
			}

			// System.out.println("======" + cave.getName());
			dinfoList.add(dinfo.build());
		}

		return dinfoList;
	}

	// 回执被占领情况
	public void CSCaveOccupyInfo(HumanObject humanObj, int type, int page, int index, long humanID) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		prx.getBaseMsg(type, page, index, humanObj.getHumanId());
		prx.listenResult(this::_result_CSCaveOccupyInfo, "humanObj", humanObj);
	}

	private void _result_CSCaveOccupyInfo(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		long humanId = humanObj.getHumanId();
		boolean isCorrect = results.get("isCorrect");
		CaveObject caveObj = results.get("caveObj");
		CaveHumanObj cObj = caveObj.getMirrorhuman();
		// HumanMirrorObject mirrorObj = new HumanMirrorObject(cObj);
		// HumanMirror mhuman = mirrorObj.getHumanMirror();
		CaveHuman mhuman = cObj.getCaveHuman();
		Cave cave = caveObj.getCave();
		SCCaveOccupyInfo.Builder msg = SCCaveOccupyInfo.newBuilder();
		msg.setCaveType(ECaveType.valueOf(cave.getType()));
		msg.setPage(cave.getPage());
		msg.setIndex(cave.getIndex());
		msg.setIsCorrect(isCorrect);
		msg.setHumanId(cave.getHumanID());
		msg.setHumanName(cave.getName());
		msg.setLevel(mhuman.getLevel());
		
		
		int combat = cObj.caveHuman.getCombat();
		Map<Long, CavePartner> partnerMap = cObj.partnerMap;
		if(partnerMap != null) {
			for(CavePartner p:partnerMap.values()) {
				combat += p.getCombat();
			}
		}
		
		
		msg.setCombat(combat);
		msg.setRoleSn(mhuman.getModelSn());
		msg.setIsOwn(caveObj.isOwn());
		msg.setIsMaxLoot(false);
		msg.setGuildName(mhuman.getGuiLdName());

		if (caveObj.isOwn()) {
			int useTime = (int) ((Port.getTime() - cave.getOwnTime()) / Utils.L1000);
			msg.setUseTime(useTime);
			int lootEarnings = robAccounts(caveObj, Port.getTime());
			// 发放奖励
			boolean isLoot = Utils.strToLongList(cave.getRobHumanList()).contains(humanId);
			msg.setIsLoot(isLoot);
			boolean isMax = cave.getRobCount() >= ParamManager.domaincaveNumLimit ? true : false;
			msg.setIsMaxLoot(isMax);
			msg.setLootEarnings(lootEarnings);
			msg.setCdTimeCount(cave.getDelayCount());
		}
		humanObj.sendMsg(msg);

	}

	/**
	 * 发起占领挑战
	 */
	public void occupyBattleFight(HumanObject humanObj, int type, int page, int index, ETokenType battletype) {

		long humanId = humanObj.getHumanId();
		// 创建竞技场并切换地图进入竞技场
		int stageSn = ParamManager.caveMapSn;
		int mapSn = getCaveMapSn();
		ConfMap confMap = ConfMap.get(mapSn);
		if (null == confMap) {
			Log.table.error("ConfMap配表错误，no find sn ={} ", mapSn);
			return;
		}

		// 检测开采令
		if (battletype == ETokenType.DevelopmentToken) {
			// 占领
			CaveServiceProxy prx = CaveServiceProxy.newInstance();
			prx.canOccupy(humanId, type);
			prx.listenResult(this::_result_canOccupy, "type", type, "page", page, "index", index, "humanObj", humanObj,
					"stageSn", stageSn, "mapSn", mapSn);

		} else if (battletype == ETokenType.SnatchToken) {
			// 抢夺
			CaveServiceProxy prx = CaveServiceProxy.newInstance();
			prx.canRob(humanId, type, page, index);
			prx.listenResult(this::_result_canRob, "type", type, "page", page, "index", index, "humanObj", humanObj,
					"stageSn", stageSn, "mapSn", mapSn);

		} else {
			Log.guild.info("错误的战斗类型");
			return;
		}

	}

	private void _result_canRob(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===_result_challenge humanObj is null");
			return;
		}
		boolean can = results.get("can");
		int type = context.get("type");
		int page = context.get("page");
		int index = context.get("index");
		int stageSn = context.get("stageSn");
		int mapSn = context.get("mapSn");
		if (!can) {
			humanObj.sendSysMsg(568012);// 本仙府已经超出可抢夺次数
			return;
		}
		List<ProduceVo> product = ParamManager.domainGrabBase;
		// 扣除抢夺令
		if (type == 2) {// 高级仙府
			product = ParamManager.domainGrabHigh;
		}

		RewardHelper.checkAndConsume(humanObj, product, LogSysModType.CaveBattle);

		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		prx.challenge(type, page, index);
		prx.listenResult(this::_result_challenge, "humanObj", humanObj, "stageSn", stageSn, "mapSn", mapSn);

	}

	private void _result_canOccupy(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===_result_challenge humanObj is null");
			return;
		}
		boolean can = results.get("can");
		int type = context.get("type");
		int page = context.get("page");
		int index = context.get("index");
		int stageSn = context.get("stageSn");
		int mapSn = context.get("mapSn");
		if (!can) {
			int reason = results.get("reason");
			if (reason == PLEASE_CHOOSE_OTHER && type == ECaveType.High_VALUE) {
				humanObj.sendSysMsg(568009);
			} else if (reason == PLEASE_CHOOSE_OTHER && type == ECaveType.Low_VALUE) {
				humanObj.sendSysMsg(568010);
			} else if (reason == CAN_NOT_MORE_THAN_TWO) {
				humanObj.sendSysMsg(568011);
			}
			return;
		}
		List<ProduceVo> product = ParamManager.domainOccupyBase;
		// 扣除开采令
		if (type == 2) {// 普通仙府
			product = ParamManager.domainOccupyHigh;
		}

		RewardHelper.checkAndConsume(humanObj, product, LogSysModType.CaveBattle);

		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		prx.challenge(type, page, index);
		prx.listenResult(this::_result_challenge, "humanObj", humanObj, "stageSn", stageSn, "mapSn", mapSn);
	}

	private void _result_challenge(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (null == humanObj) {
			Log.game.error("===_result_challenge humanObj is null");
			return;
		}
		int stageSn = Utils.getParamValue(context, "stageSn", 0);
		int mapSn = Utils.getParamValue(context, "mapSn", 0);
		CaveHumanObj cavehumanObj = Utils.getParamValue(results, "mirrorObj", null);
		HumanMirrorObject mirrorObj = new HumanMirrorObject(cavehumanObj);
		// 创建竞技场副本
		create(humanObj, mirrorObj, stageSn, mapSn);
	}

	/**
	 * 创建洞天福地战斗
	 */
	private void create(HumanObject humanObj, HumanMirrorObject mirrorObj, int stageSn, int mapSn) {
		humanObj.setCreateRepTime();// 进入异步前要先设置，避免重复操作

		CaveParam caveParam = new CaveParam(mirrorObj);
		Param param = new Param(HumanMirrorObject.CaveParam, caveParam);

		// 创建副本
		String portId = StageManager.inst().getStagePortId();
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(portId), portId, D.SERV_STAGE_DEFAULT);
		prx.createStageCave(stageSn, mapSn, ECrossFightType.FIGHT_INST_CAVE_VALUE, param);
		prx.listenResult(this::_result_create, "humanObj", humanObj, "mapSn", mapSn);
	}

	private void _result_create(Param results, Param context) {
		long stageId = Utils.getParamValue(results, "stageId", -1L);
		int mapSn = Utils.getParamValue(context, "mapSn", -1);
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (stageId < 0 || mapSn < 0 || null == humanObj) {
			Log.game.error("===创建失败：_result_create stageId={}, mapSn={}, humanObj={}", stageId, mapSn, humanObj);
			return;
		}
		if (!humanObj.checkHumanSwitchState(results, context, true)) {
			return;
		}

		// 记录及返回数据
		humanObj.crossFightSwitchKey = mapSn + ":" + stageId;
		humanObj.crossFightMapSn = mapSn;
		humanObj.crossFightFinishMsg = null;
		humanObj.crossFightTeamId = 0;
		humanObj.fightTeam = ETeamType.Team1;
		
		// 记录并发送战斗信息
		humanObj.sendFightInfo(ETeamType.Team1, ECrossFightType.FIGHT_INST_CAVE, mapSn, stageId);
	}

	/**
	 * 获取仙府地图
	 * 
	 * @return
	 */
	public int getCaveMapSn() {
		int mapSn = 0;
		int stageSn = ParamManager.caveMapSn;
		ConfInstStage conf = ConfInstStage.get(stageSn);
		if (null == conf) {
			Log.table.error("===ConfInstStage配表错误, no find sn={}", stageSn);
		} else {
			mapSn = conf.mapSN;
		}
		return mapSn;
	}

	/**
	 * 离开仙府
	 * 
	 * @param humanObj
	 */
	public void _msg_CSCaveFightLeave(HumanObject humanObj) {
		StageObjectCave stageObj = null;
		if (humanObj.stageObj instanceof StageObjectCave) {
			stageObj = (StageObjectCave) humanObj.stageObj;
			if (stageObj == null)
				return;
		} else {
			return;
		}

		// 离开副本
//		humanObj.quitToCommon(humanObj.stageObj.confMap.sn);
		StageManager.inst().quitToCommon(humanObj, humanObj.stageObj.confMap.sn);

	}

	/**
	 * 战斗结算
	 * 
	 * @param humanObj
	 * @param isWin
	 * @param cavetype
	 * @param page
	 * @param index
	 */
	public void _msg_CSCaveEnd(HumanObject humanObj, ETokenType battleType, boolean isWin, int cavetype, int page,
			int index) {

		CaveHumanObj caveHumanObj = new CaveHumanObj(humanObj);
		SCCaveFightEnd.Builder msg = SCCaveFightEnd.newBuilder();
		msg.setPage(page);
		msg.setIndex(index);
		msg.setHumanName(humanObj.getHuman().getName());
		msg.setIsWin(isWin);
		msg.setCaveType(ECaveType.valueOf(cavetype));
		msg.setBattleType(battleType);
		if (battleType == ETokenType.SnatchToken) {
			CaveServiceProxy prx = CaveServiceProxy.newInstance();
			prx.rob_battle_end(caveHumanObj, cavetype, page, index, isWin);
			prx.listenResult(this::_result_rob_battle_end, "humanObj", humanObj, "msg", msg);
		} else {
			CaveServiceProxy prx = CaveServiceProxy.newInstance();
			prx.occupy_battle_end(caveHumanObj, cavetype, page, index, isWin);// 战斗结算
			prx.listenResult(this::_result_occupy_battle_end, "humanObj", humanObj, "msg", msg);
		}

	}

	/**
	 * 占领战斗回调
	 */
	private void _result_occupy_battle_end(Param results, Param context) {
		SCCaveFightEnd.Builder msg = context.get("msg");
		HumanObject humanObj = context.get("humanObj");
		long oldHumanId = results.get("oldHumanId");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}

		humanObj.sendMsg(msg);
		// 通知被占领的玩家
		SCMyCaveLost.Builder b_msg = SCMyCaveLost.newBuilder();
		b_msg.setIndex(msg.getIndex());
		b_msg.setCaveType(msg.getCaveType());
		b_msg.setPage(msg.getPage());
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendMsg(oldHumanId, b_msg.build());
	}

	/**
	 * 抢夺战斗回调
	 * 
	 * @param results
	 * @param context
	 */
	private void _result_rob_battle_end(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		int robAccount = results.get("robAccount");
		
		RewardHelper.reward(humanObj, EMoneyType.coin_VALUE,robAccount, LogSysModType.CaveBattle);
		
		SCCaveFightEnd.Builder msg = context.get("msg");
		DProduce.Builder dp = DProduce.newBuilder();
		dp.setSn(EMoneyType.coin_VALUE);
		dp.setNum(robAccount);
		msg.addItemList(dp);
		humanObj.sendMsg(msg);
	}

	//
	// /**
	// * 占领战斗回调
	// * @param results
	// * @param context
	// */
	// private void _result_CSCaveEnd(Param results, Param context) {
	// SCCaveFightEnd.Builder msg = context.get("msg");
	// CaveIndexes caveIndexs = results.get("caveIndexs");
	// HumanObject humanObj = context.get("humanObj");
	//
	// DProduce.Builder dp = DProduce.newBuilder();
	// // 只有抢夺才有抢夺收益
	// if (msg.getBattleType() == ETokenType.SnatchToken) {
	// int robAccount = results.get("robAccount");
	// dp.setNum(robAccount);
	// }
	// dp.setSn(EMoneyType.coin_VALUE);
	// msg.addItemList(dp);
	//
	// if (humanObj == null) {
	// Log.game.error("humanObj is null");
	// return;
	// }
	//
	// humanObj.sendMsg(msg);
	// }

	/**
	 * 发送 开采购买次数 抢夺购买次数
	 * 
	 * @param humanObj
	 */
	public void sendCaveMoneyInfo(HumanObject humanObj) {
		SCCaveMoneyInfo.Builder msg = SCCaveMoneyInfo.newBuilder();
		Human human = humanObj.getHuman();
		int dev = (int) human.getBuyDevelopmentTokenCount();
		msg.setOccupyBuyCount(dev);// 开采令次数
		int snatch = (int) human.getBuySnatchTokeCountn();
		msg.setLootBuyCount(snatch);
		humanObj.sendMsg(msg);
	}

	/**
	 * 购买 开采令/强夺令
	 */
	public void buyToken(HumanObject humanObj, ETokenType type) {
		Human human = humanObj.getHuman();
		boolean can = canBuyNum(humanObj, type);
		if (!can) {
			return;
		}
		int moneyType = 0;
		long numBuy = 0;
		switch (type) {
		case DevelopmentToken:
			moneyType = EMoneyType.developmentToken_VALUE;
			numBuy = human.getBuyDevelopmentTokenCount();
			human.setBuyDevelopmentTokenCount(human.getBuyDevelopmentTokenCount() + 1);
			break;
		case SnatchToken:
			moneyType = EMoneyType.snatchToken_VALUE;
			numBuy = human.getBuySnatchTokeCountn();
			human.setBuySnatchTokeCountn(human.getBuySnatchTokeCountn() + 1);
			break;
		default:
			break;
		}
		// 剩余购买次数够不够
		int numVip = HumanManager.inst().getBuyNumVip(humanObj);// 可购买次数
		if (numBuy >= numVip) {
			humanObj.sendSysMsg(430801);// 可购买挑战次数不足！请提升VIP等级
			return;
		}
		// 本次是第几次购买
		int buys = (int) (numBuy + 1);
		// 钱够不够
		ECostGoldType costType = type == ETokenType.DevelopmentToken ? ECostGoldType.DevelopmentCost
				: ECostGoldType.SnatchCost;

		int needGold = RewardHelper.getCostGold(costType, buys);// 获取购买次数需花费的元宝
		if (needGold == -1) {
			Log.table.error("配置表错误");
		}
		// 扣元宝
		if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, needGold, LogSysModType.CompeteBuyNum)) {
			return;
		}

		// 增加次数
		RewardHelper.reward(humanObj, moneyType, 1, LogSysModType.CaveBuy);
		// 返回前端
		sendCaveMoneyInfo(humanObj);

	}

	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			return;
		}
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			resetDaily(humanObj);
		}
	}

	public void resetDaily(HumanObject humanObj) {
		boolean isunlock = ModunlockManager.inst().isUnlock(EModeType.ModeCave, humanObj);
		if (!isunlock) {
			return;
		}
		Human human = humanObj.getHuman();
		if (human == null) {
			return;
		}
		// 重置两个购买次数
		human.setBuyDevelopmentTokenCount(0);
		human.setBuySnatchTokeCountn(0);
		
		int developmentTokenNum = (int)human.getDevelopmentToken(); 
		int snatchTokenNum = (int)human.getSnatchToken();
		if(developmentTokenNum < ParamManager.domainDailyOccupyReset) {
			human.setDevelopmentToken(ParamManager.domainDailyOccupyReset);
		}
		
		
		if(snatchTokenNum < ParamManager.domainDailyGrabReset) {
			human.setSnatchToken(ParamManager.domainDailyGrabReset);
		}
	}

	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_StageHumanEnter(Param params) {
		HumanObject humanObj = Utils.getParamValue(params, "humanObj", null);
		if (humanObj == null) {
			return;
		}
		boolean isFirstLogin = humanObj.isDailyFirstLogin;
		if (isFirstLogin) {
			humanObj.getHuman().setBuyDevelopmentTokenCount(0);
			humanObj.getHuman().setBuySnatchTokeCountn(0);
		}
		// 测试用
		// CaveServiceProxy prx = CaveServiceProxy.newInstance();
		// prx.checkAccount(humanObj.getHumanId());
	}

	/**
	 * 是否可以购买竞技场挑战次数
	 * 
	 * @param humanObj
	 * @return
	 */
	private boolean canBuyNum(HumanObject humanObj, ETokenType type) {
		// 次数不够
		Human human = humanObj.getHuman();
		int numBuy = 0;// 已购买次数
		int numVip = 0;
		switch (type) {
		case DevelopmentToken:
			numBuy = (int) human.getBuyDevelopmentTokenCount();
			numVip = VipManager.inst().getConfVipBuyTimes(EVipBuyType.caveDevelopment,
					humanObj.getHuman().getVipLevel());
			break;
		case SnatchToken:
			numBuy = (int) human.getBuySnatchTokeCountn();
			numVip = VipManager.inst().getConfVipBuyTimes(EVipBuyType.caveSnatch, humanObj.getHuman().getVipLevel());
			break;
		default:
			break;
		}
		if (numBuy >= numVip) {
			humanObj.sendSysMsg(430801);// 可购买挑战次数不足！请提升VIP等级
			return false;
		}
		return true;
	}

	/**
	 * 
	 * 获取 抢夺仙府收益
	 * 
	 * 抢夺者收益=当前仙府玩家单位时长收益x占领时长x【抢夺比例x（1000+秘法加成-/万分比）】，若有小数则向下取整
	 */
	public int robAccounts(CaveObject caveObj, long now) {
		int base = baseAccpunt(caveObj.getCave(), now);// 当前仙府玩家单位时长收益x占领时长
		base = base * ParamManager.domainGrabRatio / 10000;
		// TODO
		return base;
	}

	/**
	 * 正常结算收益
	 */
	public List<ProduceVo> normalAccounts(CaveObject caveObj) {

		return null;
	}

	/**
	 * 当前仙府玩家单位时长收益x占领时长
	 * 
	 * @param caveObj
	 * @param now
	 * @return
	 */
	public int baseAccpunt(Cave cave, long now) {
		ConfCave conf = ConfCave.get(cave.getSn());
		if (conf == null) {
			Log.guild.info("can't find sn={}", cave.getSn());
			return 0;
		}
		int type = cave.getType();
		/* 基础产量 */
		int itemNum = 0;
		switch (type) {
		case ECaveType.High_VALUE:
			itemNum = conf.seniorAddNum;
			break;
		case ECaveType.Low_VALUE:
			itemNum = conf.normalAddNum;
			break;
		default:
			break;
		}

		// 计算占有几个单位时(分钟)
		long ownTime = 0;
		// 正常到期结算
		if (now >= cave.getExpOwnTime()) {
			ownTime = cave.getExpOwnTime() - cave.getOwnTime();
		} else {
			ownTime = now - cave.getOwnTime();
		}
		int ownTime_min = (int) ownTime / Utils.SEC_EVEVRY_MIN;

		/*
		 * 当前仙府收益 当前仙府单位时长收益x占领时长
		 */
		itemNum = itemNum * ownTime_min / ParamManager.domainUnitIncome;
		// itemNum = itemNum/100 ;
		if (itemNum <= 0) {
			Log.guild.error("错误的产量");
			itemNum = 0;
		}
		Log.guild.info("当前产量 ={}", itemNum);
		return itemNum;

	}

	/**
	 * 仙府额外收益
	 * 
	 * @param caveObj
	 * @return
	 */
	public List<ProduceVo> extraAccounts(Cave cave) {
		List<ProduceVo> reward_list = new ArrayList<>();
		// 计算占有几个单位时(分钟)
		long ownTime = 0;
		// 正常到期结算
		if (Port.getTime() >= cave.getExpOwnTime()) {
			ownTime = cave.getExpOwnTime() - cave.getOwnTime();
		} else {
			ownTime = Port.getTime() - cave.getOwnTime();
		}
		int ownTime_hour = (int) ownTime / Utils.SEC_EVEVRY_HOUR;// 计算出占领了多少个小时

		ConfCave conf = ConfCave.get(cave.getSn());
		if (conf == null) {
			Log.guild.info("can't find sn={}", cave.getSn());
			return reward_list;
		}

		int index = 0;
		for (int h : conf.cdTime) {
			if (ownTime_hour >= h) {
				ProduceVo p = new ProduceVo(conf.additionalReward[index], 1);
				reward_list.add(p);
			}
			index++;
		}
		return reward_list;
	}

	/**
	 * 获取我的洞府信息
	 * 
	 * @param humanObj
	 */
	public void getMyCaveInfo(HumanObject humanObj) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		long humanId = humanObj.getHumanId();
		prx.getMyCaveInfo(humanId);
		prx.listenResult(this::_result_getMyCaveInfo, "humanObj", humanObj);
	}

	private void _result_getMyCaveInfo(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		List<CaveObject> caveObjList = results.get("caveObjList");
		if (caveObjList == null) {
			Log.game.info("caveObjList is null");
			return;
		}
		List<Long> foeList = new ArrayList<>();
		for (CaveObject caveObj : caveObjList) {
			if (caveObj == null) {
				continue;
			}
			Cave cave = caveObj.getCave();
			if (cave == null) {
				continue;
			}
			List<Long> foe_list = Utils.strToLongList(cave.getRobHumanList());
			if (foe_list == null) {
				continue;
			}
			foeList.addAll(foe_list);
		}

		SCMyCaveInfo.Builder msg = SCMyCaveInfo.newBuilder();
		msg.addAllCaveList(getCaveMsg(humanObj, caveObjList, foeList));
		humanObj.sendMsg(msg);
	}

	/**
	 * 立刻结算仙府
	 */
	public void giveUp(HumanObject humanObj, int type, int page, int index) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		long humanId = humanObj.getHumanId();
		prx.giveUp(humanId, type, page, index);
		prx.listenResult(this::_result_giveUp, "humanObj", humanObj);
	}

	/**
	 * 立刻结算仙府回调
	 * 
	 */
	private void _result_giveUp(Param results, Param context) {
		CaveObject caveObj = results.get("caveObj");
		HumanObject humanObj = context.get("humanObj");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		SCCaveGiveUp.Builder msg = SCCaveGiveUp.newBuilder();
		if (caveObj == null) {
			Log.game.debug("caveObj is null");
			return;
		}
		Cave cave = caveObj.getCave();
		msg.setCaveType(ECaveType.valueOf(cave.getType()));
		msg.setPage(cave.getPage());
		msg.setIndex(cave.getIndex());
		humanObj.sendMsg(msg);
	}

	/**
	 * 根据 int type, int page, int index 获取sn
	 * 
	 * @param type
	 * @param page
	 * @param index
	 * @return
	 */
	public int getSn(int type, int page, int index) {
		int sn = 0;
		Collection<ConfCaveField> confAll = ConfCaveField.findAll();
		for (ConfCaveField conf : confAll) {
			int conf_type = conf.type;
			int[] pageRegion = conf.pageRegion;
			if (conf_type == type && pageRegion[0] <= page && pageRegion[1] >= page) {
				sn = conf.caveSn[index];
			}
		}
		return sn;
	}

	/**
	 * 延长仙府时间
	 */
	public void addTime(HumanObject humanObj, int type, int page, int index) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		long humanId = humanObj.getHumanId();
		prx.getCaveInfo(humanId, type, page, index);
		prx.listenResult(this::_result_addTime, "humanObj", humanObj);
	}

	private void _result_addTime(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		long humanId = humanObj.getHumanId();

		CaveObject caveObj = results.get("caveObj");
		Cave cave = caveObj.getCave();
		/* 扣费 */
		ConfCave caveConf = ConfCave.get(cave.getSn());
		if (caveConf == null) {
			Log.table.info("配置表错误 延时仙府cave模块");
			return;
		}
		int len = caveConf.cdTime.length;
		if (cave.getDelayCount() + 1 >= len) {
			Log.guild.debug("超出可延时的次数");
			return;
		}
		int delayCount = cave.getDelayCount();
		int costSn = caveConf.costItemSn[delayCount];
		int costNum = caveConf.costItemNum[delayCount];
		if(!RewardHelper.checkAndConsume(humanObj, costSn, costNum, LogSysModType.CaveDelay)) {
			return;
		}

		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		prx.addTime(humanId, cave.getType(), cave.getPage(), cave.getIndex());
		prx.listenResult(this::_result_addTime2, "humanObj", humanObj);
	}

	private void _result_addTime2(Param results, Param context) {
		CaveObject caveObj = results.get("caveObj");
		if (caveObj == null) {
			return;
		}
		Cave cave = caveObj.getCave();
		HumanObject humanObj = context.get("humanObj");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		int delayCount = results.get("delayCount");

		SCCaveCDTimeAdd.Builder msg = SCCaveCDTimeAdd.newBuilder();
		msg.setCaveType(ECaveType.valueOf(cave.getType()));
		msg.setPage(cave.getPage());
		msg.setIndex(cave.getIndex());
		msg.setHumanID(humanObj.getHumanId());
		msg.setCdTimeCount(delayCount);
		humanObj.sendMsg(msg);
	}

	/**
	 * 获取免费仙府
	 * 
	 * @param humanObj
	 * @param type
	 * @param sn
	 */
	public void getFreeCave(HumanObject humanObj, int type, int sn) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		prx.getFreeCave(sn, type);
		prx.listenResult(this::_result_getFreeCave, "humanObj", humanObj, "sn", sn);
	}

	private void _result_getFreeCave(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		int sn = context.get("sn");
		CaveIndexes caveIndex = results.get("caveIndex");
		SCGetFreeCave.Builder msg = SCGetFreeCave.newBuilder();
		if (caveIndex == null) {
			msg.setPage(0);
		} else {
			msg.setCaveType(ECaveType.valueOf(caveIndex.getType()));
			msg.setPage(caveIndex.getPage());
			msg.setIndex(caveIndex.getIndex());
			msg.setSn(sn);
		}
		humanObj.sendMsg(msg);
	}

	public void getCaveLogMsg(HumanObject humanObj) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		long humanId = humanObj.getHumanId();
		prx.getCaveLog(humanId);
		prx.listenResult(this::_result_getCaveLogMsg, "humanObj", humanObj);
	}

	private void _result_getCaveLogMsg(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");

		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		SCCaveDefense.Builder msg = SCCaveDefense.newBuilder();
		List<CaveLog> caveLog = results.get("caveLog");
		if (caveLog == null) {
			return;
		}
		msg.addAllRecordList(getDCaveDefenseMsg(caveLog, false));
		humanObj.sendMsg(msg);
	}

	/**
	 * 是否只显示仇人
	 * 
	 * @param caveLog
	 * @param isfilterenemy
	 * @return
	 */
	private List<DCaveDefense> getDCaveDefenseMsg(List<CaveLog> caveLog, boolean isfilterenemy) {
		List<DCaveDefense> list = new ArrayList<>();
		if (caveLog == null) {
			return list;
		}
		for (CaveLog log : caveLog) {
			DCaveDefense.Builder dinfo = DCaveDefense.newBuilder();
			if (isfilterenemy && log.isIsWin()) {
				continue;
			}
			dinfo.setIsWin(log.isIsWin());
			dinfo.setHumanName(log.getHumanName());
			dinfo.setCombat(log.getCombat());
			dinfo.setHumanID(log.getHumanId());
			dinfo.setBattleType(ETokenType.valueOf(log.getBattleType()));
			dinfo.setTime(log.getTime());//
			dinfo.setUseTime((int) ((Port.getTime() - log.getOwnTime()) / Utils.I1000));// 占领时间
			dinfo.setPage(log.getPage());
			dinfo.setIndex(log.getIndex());
			ECaveType type = ECaveType.valueOf(log.getType());
			if (type != null) {
				dinfo.setCaveType(ECaveType.valueOf(log.getType()));
			}
			list.add(dinfo.build());
		}
		return list;
	}

	public void getEnemyInfo(HumanObject humanObj, long enemyHumanID) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		prx.getMyCaveInfo(enemyHumanID);
		prx.listenResult(this::_result_getEnemyInfo, "humanObj", humanObj, "enemyHumanID", enemyHumanID);
	}

	private void _result_getEnemyInfo(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		long enemyHumanID = context.get("enemyHumanID");
		if (humanObj == null) {
			Log.game.error("humanObj is null");
			return;
		}
		List<CaveObject> caveObjList = results.get("caveObjList");
		List<Long> foeList = new ArrayList<>();
		for (CaveObject caveObj : caveObjList) {
			if (caveObj == null) {
				continue;
			}
			Cave cave = caveObj.getCave();
			if (cave == null) {
				continue;
			}
			List<Long> foe_list = Utils.strToLongList(cave.getRobHumanList());
			if (foe_list == null) {
				continue;
			}
			foeList.addAll(foe_list);
		}
		SCCaveEnemyInfo.Builder msg = SCCaveEnemyInfo.newBuilder();
		msg.addAllCaveList(getCaveMsg(humanObj, caveObjList, foeList));
		msg.setEnemyHumanID(enemyHumanID);
		humanObj.sendMsg(msg);
	}

	public void getEnemyMsg(HumanObject humanObj) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		long humanId = humanObj.getHumanId();
		prx.getCaveLog(humanId);
		prx.listenResult(this::_result_getEnemyMsg, "humanObj", humanObj);
	}

	private void _result_getEnemyMsg(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");

		List<CaveLog> caveLog = results.get("caveLog");
		if (caveLog == null) {
			return;
		}
		SCCaveEnemy.Builder msg = SCCaveEnemy.newBuilder();
		msg.addAllRecordList(getDCaveDefenseMsg(caveLog, false));
		System.out.println(msg.build());
		humanObj.sendMsg(msg);
	}

	/**
	 * 获取某人仙盟同盟的人所占领的洞府列表
	 * 
	 */
	public void getGuildMemberInfo(HumanObject humanObj) {
		long guildId = humanObj.getHuman().getGuildId();
		if(guildId <= 0) {
			Log.guild.info("没有同盟信息");
			return;
		}
		// 远程取出这个人盟友id列表
		GuildServiceProxy prx = GuildServiceProxy.newInstance();
		prx.getGuildImmo(guildId);
		prx.listenResult(this::_result_getGuildMemberInfo1, "humanObj", humanObj);
	}

	private void _result_getGuildMemberInfo1(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");

		String guildHuman = results.get("guildHuman");
		List<GuildData> data = GuildData.jsonToList(guildHuman);
		if(data == null) {
			Log.guild.info("没有同盟信息");
			return;
		}
		List<Long> humanIdList = new ArrayList<>();
		for(GuildData gd : data) {
			humanIdList.add(gd.id);
		}
		if(humanIdList.size()<=0) {
			Log.guild.info("没有同盟信息");
			return;
		}
		//去除自己
		if(humanIdList.contains(humanObj.getHumanId())) {
			humanIdList.remove(humanObj.getHumanId());
		}
		//远程去除id列表的所有洞府信息
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		prx.getCaveMemberInfo(humanIdList);
		prx.listenResult(this::_result_getGuildMemberInfo2, "humanObj", humanObj);
	}
	private void _result_getGuildMemberInfo2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<CaveIndexes> caveIndexList = results.get("caveIndexList");
		
		SCCaveGuildMemberInfo.Builder msg = SCCaveGuildMemberInfo.newBuilder();
		Map<Integer,DCaveGuildMemberInfo.Builder> maps = new HashMap<>();
		
		for(CaveIndexes cid:caveIndexList ) {
			int page = cid.getPage();
			DCaveGuildMemberInfo.Builder dinfo =  maps.get(page);
			if(dinfo == null) {
				dinfo = DCaveGuildMemberInfo.newBuilder();
				dinfo.setCavePage(page);
				dinfo.setHumanNum(1);
				dinfo.setCaveType(ECaveType.valueOf(cid.type));
			}else {
				int num = dinfo.getHumanNum();
				dinfo.setHumanNum(num+1);
			}
			msg.addCaveList(dinfo);
			
		}
		List l = new ArrayList<>(maps.values());
		msg.addAllCaveList(l);
		humanObj.sendMsg(msg);
	}
	

}
