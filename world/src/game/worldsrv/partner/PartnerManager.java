package game.worldsrv.partner;

import game.msg.Define;
import game.msg.Define.DDropItem;
import game.msg.Define.DItem;
import game.msg.Define.DPartnerBriefInfo;
import game.msg.Define.DPartnerInfo;
import game.msg.Define.DPartnerLineup;
import game.msg.Define.DProp;
import game.msg.Define.DServantInfo;
import game.msg.Define.DUnit;
import game.msg.Define.EInformType;
import game.msg.Define.EModeType;
import game.msg.Define.EMoneyType;
import game.msg.Define.EPartnerGetType;
import game.msg.Define.EPartnerLineup;
import game.msg.Define.EPartnerUp;
import game.msg.Define.EPropChangeType;
import game.msg.Define.EQualityType;
import game.msg.Define.EStanceType;
import game.msg.MsgCommon.SCPartnerPropInfoChange;
import game.msg.MsgPartner.SCAddPokedexInfo;
import game.msg.MsgPartner.SCAddServant;
import game.msg.MsgPartner.SCCimeliaAddCont;
import game.msg.MsgPartner.SCGetPokedexGroupReward;
import game.msg.MsgPartner.SCLoadPokedexInfo;
import game.msg.MsgPartner.SCNewDecomposeAll;
import game.msg.MsgPartner.SCPartnerAddCont;
import game.msg.MsgPartner.SCPartnerChangeLineup;
import game.msg.MsgPartner.SCPartnerDrop;
import game.msg.MsgPartner.SCPartnerInfo;
import game.msg.MsgPartner.SCPartnerLineup;
import game.msg.MsgPartner.SCRemoveServant;
import game.msg.MsgPartner.SCServantClear;
import game.worldsrv.activitySeven.ActivitySevenTypeKey;
import game.worldsrv.character.CharacterObject;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.character.UnitDataPersistance;
import game.worldsrv.character.UnitManager;
import game.worldsrv.config.ConfCimelia;
import game.worldsrv.config.ConfCimeliaConstitutions;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfPartnerConstitutions;
import game.worldsrv.config.ConfPartnerFate;
import game.worldsrv.config.ConfPartnerPokedexFate;
import game.worldsrv.config.ConfPartnerProperty;
import game.worldsrv.config.ConfPartnerRecruit;
import game.worldsrv.config.ConfPartnerStarUp;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.config.ConfServantLock;
import game.worldsrv.drop.DropBag;
import game.worldsrv.entity.Cimelia;
import game.worldsrv.entity.EntityUnitPropPlus;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.HumanExtInfo;
import game.worldsrv.entity.Partner;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.inform.InformManager;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.rune.RuneManager;
import game.worldsrv.support.ConfigKeyFormula;
import game.worldsrv.support.GlobalConfVal;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

public class PartnerManager extends ManagerBase {

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static PartnerManager inst() {

		return inst(PartnerManager.class);
	}

	/**
	 * 未拥有该随从的标识
	 */
	public static final long HasNoServant = -1L;
	/**
	 * 随从数量
	 */
	public static final int ServantNum = 3;

	private static final int[] PARTNER_EXP_RANGE = { 100001, 100014 };


	/**
	 * 创角初始化阵容信息
	 * 
	 */
	public void initLineup(HumanExtInfo extInfo) {
		int[] initGeneralSn = Utils.arrayStrToInt(ParamManager.initPartnerSn);
		List<Long> plist = new ArrayList<>();
		for(int partnerSn : initGeneralSn) {
			if(partnerSn == 0|| partnerSn == -1) {
				plist.add(Long.valueOf(partnerSn));
				continue;
			}
			UnitDataPersistance dataPers = createPartner(extInfo.getId(), partnerSn);
			if (dataPers == null) {
				continue;
			}
			plist.add(dataPers.unit.getId());
			// 创建法宝
			Cimelia cimelia = createCimelia(dataPers);
			if (cimelia == null) {
				continue;
			}
		}
		extInfo.setPartnerLineup(Utils.ListLongToStr(plist));
		extInfo.setPartnerStance(ParamManager.initPartnerStance);
	}

	/**
	 * 发送伙伴阵型信息
	 */
	public void sendMsg_SCPartnerLineup(HumanObject humanObj) {
		// 阵型
		EStanceType stance = EStanceType.valueOf(ParamManager.initPartnerStance);
		// 伙伴id列表 partnerList
		List<Long> partnerList = getPartnerLineUp(humanObj);
		// 发送给客户端
		SCPartnerLineup.Builder msg = SCPartnerLineup.newBuilder();
		DPartnerLineup.Builder dplineUp = DPartnerLineup.newBuilder();
		// 现在所有都一样
		dplineUp.setTypeLineup(EPartnerLineup.LUInst);

		dplineUp.setTypeStance(stance);
		for (Long pid : partnerList) {
			dplineUp.addIdPartner(pid);
		}
		msg.setLineup(dplineUp);
		humanObj.sendMsg(msg);
	}

	/**
	 * 登陆时候:玩家其它数据加载开始：加载玩家的武将信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(Partner.tableName);
		dbPrx.findBy(false, Partner.K.HumanId, humanObj.id);
		dbPrx.listenResult(this::_result_loadHumanPartner, "humanObj", humanObj);
		// 玩家数据加载开始一个
		Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);
	}

	private void _result_loadHumanPartner(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanPartner humanObj is null");
			return;
		}

		List<Record> records = results.get();
		if (records == null) {
			Log.game.error("===_result_loadHumanPartner record is null");
			return;
		}

		List<Partner> partnerList = new ArrayList<>();

		int partnerNum = records.size();
		// 加载数据
		for (Record record : records) {
			if (record == null) {
				continue;
			}
			Partner gen = new Partner(record);
			partnerList.add(gen);
			// 加载武将的属性信息
			Event.fire(EventKey.PartnerLoadLineUp, "humanObj", humanObj);
		}

		// 初始化阵容信息
		initLineUpByHumanObj(humanObj);

		// 初始化human身上的伙伴的属性
		for (Partner partner : partnerList) {
			PartnerObject partnerObj = addToUnit(humanObj, partner);
//			DB dbPrx = DB.newInstance(UnitPropPlus.tableName);
//			dbPrx.get(partnerObj.id);
//			dbPrx.listenResult(this::_result_loadPartnerUnitPropPlus, "humanObj", humanObj, "partnerObj", partnerObj,
//					"partnerNum", partnerNum);

			// 加载法宝信息
			Event.fire(EventKey.HumanDataLoadOtherBeginOne, "humanObj", humanObj);

			DB cimelia_dbPrx = DB.newInstance(Cimelia.tableName);
			cimelia_dbPrx.get(partnerObj.id);
			cimelia_dbPrx.listenResult(this::_result_loadCimelia, "humanObj", humanObj, "partnerObj", partnerObj);
		}
		// 初始化护法
		for (PartnerObject po : humanObj.partnerMap.values()) {
			List<Long> idlist = Utils.strToLongList(po.getPartner().getServantList());
			po.setServantList(humanObj, idlist);
		}
		// 玩家数据加载完成一个
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}

	/**
	 * 加载法宝
	 * 
	 * @param results
	 * @param context
	 */

	private void _result_loadCimelia(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadPartnerUnitPropPlus humanObj is null");
			Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
			return;
		}
		Record record = results.get();
		if (record == null) {
			//Log.game.error("===_result_loadPartnerUnitPropPlus record=null");
			Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
			return;
		}
		PartnerObject partnerObj = Utils.getParamValue(context, "partnerObj", null);
		if (partnerObj == null) {
			Log.game.error("===_result_loadPartnerUnitPropPlus partnerObj is null");
			Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
			return;
		}
		// int partnerNum = context.getInt("partnerNum");
		// 加载数据
		Cimelia cimelia = new Cimelia(record);
		partnerObj.setCimeLia(cimelia);
		Event.fire(EventKey.HumanDataLoadOtherFinishOne, "humanObj", humanObj);
	}

	/**
	 * 从数据库中获得伙伴的属性并且赋值给伙伴对象
	 */
//	private void _result_loadPartnerUnitPropPlus(Param results, Param context) {
//		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
//		if (humanObj == null) {
//			Log.game.error("===_result_loadPartnerUnitPropPlus humanObj is null");
//			return;
//		}
//
//		Record record = results.get();
//		if (record == null) {
//			Log.game.error("===_result_loadPartnerUnitPropPlus record=null");
//			return;
//		}
//
//		PartnerObject partnerObj = Utils.getParamValue(context, "partnerObj", null);
//		if (partnerObj == null) {
//			Log.game.error("===_result_loadPartnerUnitPropPlus partnerObj is null");
//			return;
//		}
//		// int partnerNum = context.getInt("partnerNum");
//		// 加载数据
//		UnitPropPlus unitPropPlus = new UnitPropPlus(record);
//		partnerObj.dataPers.unitPropPlus.init(unitPropPlus);
//		// 开始加载
//		humanObj.loadingGenNum++;
//		// //伙伴数据加载完成
//		// if(partnerNum == humanObj.loadingGenNum){
//		// this.loadDataOver(humanObj);
//		// }
//
//		// 重新计算属性并赋值
//		UnitManager.inst().propCalc(partnerObj, EPropChangeType.PropChangeLogin);
//	}

	/**
	 * 伙伴数据加载完成-登陆下发信息
	 */
	@Listener(EventKey.HumanDataLoadAllFinish)
	public void _listener_HumanDataLoadAllFinish(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadAllFinish humanObj is null");
			return;
		}
		// 同步全局信息
		PartnerManager.inst().updatePartnerGlobalInfo(humanObj);
		// 下发武将信息
		PartnerManager.inst().sendSCPartnerInfo(humanObj);
		// 下发图鉴信息，获取奖励信息
		PartnerManager.inst().load_HandBookMsg(humanObj);
		// 设置护法数量
		PartnerManager.inst().sendServantNum(humanObj);

	}

	/**
	 * 下发伙伴数量
	 * 
	 * @param humanObj
	 */
	private void sendServantNum(HumanObject humanObj) {
		SCServantClear.Builder msg = SCServantClear.newBuilder();
		msg.setNum(humanObj.getHumanExtInfo().getServantLock());
		humanObj.sendMsg(msg);
	}

	/**
	 * 下发图鉴信息，获取奖励信息
	 * 
	 * @param humanObj
	 */
	private void load_HandBookMsg(HumanObject humanObj) {
		SCLoadPokedexInfo.Builder msg = SCLoadPokedexInfo.newBuilder();
		// 已激活的图鉴
		String strSnList = humanObj.extInfo.getActivityHand();
		List<Integer> snList = Utils.strToIntList(strSnList);
		for (Integer sn : snList) {
			msg.addPartnerSn(sn);
		}
		// 已经获取过的集合奖励
		String pokedGropStr = humanObj.extInfo.getActivityReword();
		List<Integer> rewordList = Utils.strToIntList(pokedGropStr);
		for (Integer ireword : rewordList) {
			ConfPartnerPokedexFate conf = ConfPartnerPokedexFate.getBy("rewardID", ireword);
			msg.addPokedexGroupIdList(conf.sn);
		}

		humanObj.sendMsg(msg);
	}

	/**
	 * 发送阵容信息
	 */
	public void initLineUpByHumanObj(HumanObject humanObj) {
		HumanExtInfo exinfo = humanObj.getHumanExtInfo();
		String lienUpIdList = exinfo.getPartnerLineup();
		SCPartnerLineup.Builder msg = SCPartnerLineup.newBuilder();
		DPartnerLineup.Builder lineUpInfo = DPartnerLineup.newBuilder();
		lineUpInfo.setTypeLineup(EPartnerLineup.LUInst);
		lineUpInfo.setTypeStance(EStanceType.valueOf(exinfo.getPartnerStance()));
		lineUpInfo.addAllIdPartner(Utils.strToLongList(lienUpIdList));
		
		
		msg.setLineup(lineUpInfo);
		humanObj.sendMsg(msg);
	}

	/**
	 * 下发武将信息
	 * 
	 * @param humanObj
	 */
	private void sendSCPartnerInfo(HumanObject humanObj) {
		SCPartnerInfo.Builder msg = SCPartnerInfo.newBuilder();
		Map<Long, PartnerObject> partnerMap = humanObj.partnerMap;
		for (PartnerObject pobj : partnerMap.values()) {
			Partner partner = pobj.getPartner();
			int patnerSn = partner.getSn();
			// 主角和位置为0的玩家，则跳过
			if (patnerSn < 1 || pobj == null) {
				continue;
			}

			DPartnerInfo partnerInfo = getDPartnerMsg(pobj);
			msg.addUnit(partnerInfo);
			// 下发改武将护法信息
			DServantInfo dsf = sendDServantInfo(humanObj, partner);
			msg.addServant(dsf);
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 下发该伙伴护法消息
	 * 
	 * @param partner
	 */
	private DServantInfo sendDServantInfo(HumanObject humanObj, Partner partner) {
		List<Long> servantList = Utils.strToLongList(partner.getServantList());
		DServantInfo.Builder dinfo = DServantInfo.newBuilder();
		dinfo.setPartnerId(partner.getId());
		dinfo.addAllServantId(servantList);
		List<Integer> combatList = new ArrayList<>();
		for (Long servantId : servantList) {
			if (servantId == 0L || servantId == -1L) {
				continue;
			}
			PartnerObject poj = humanObj.partnerMap.get(servantId);
			if (poj == null) {
				continue;
			}
			int combat = poj.getPartner().getCombat();
			combatList.add(combat);
		}
		dinfo.addAllPower(combatList);
		return dinfo.build();
	}

	/**
	 * 依据partner构建DPartnerInfo
	 */
	public DPartnerInfo getDPartnerMsg(PartnerObject partnerObj) {
		Partner partner = partnerObj.getPartner();
		DPartnerInfo.Builder partnerInfo = DPartnerInfo.newBuilder();
		if (partner == null) {
			Log.table.error(" == getDPartnerMsg  partner= null");
			partnerInfo.setPartnerId(0);
			partnerInfo.setPartnerSn(0);
			return partnerInfo.build();
		}
		int patnerSn = partner.getSn();
		partnerInfo.setPartnerId(partner.getId());
		partnerInfo.setPartnerSn(patnerSn);
		partnerInfo.setStar(partner.getStar());
		partnerInfo.setAdvanceLevel(partner.getAdvLevel());
		partnerInfo.setPartnerExp(partner.getExp());
		// 符文穿戴信息
		partnerInfo.addAllRuneIds(Utils.strToLongList(partner.getRuneInfo()));
		// 设置法宝
		Define.DCimelia.Builder cmsg = Define.DCimelia.newBuilder();
		Cimelia cimeLia = partnerObj.getCimeLia();
		if (cimeLia != null) {
			Log.partner.debug("error 没有法宝信息");
			cmsg.setPartnerId(cimeLia.getPartnerId());
			cmsg.setAdvLevel(cimeLia.getAdvLevel());
			cmsg.setLevel(cimeLia.getLevel());
			cmsg.setSn(cimeLia.getSn());
			cmsg.setStar(cimeLia.getStar());
			partnerInfo.setCimelia(cmsg);
		}
		DUnit.Builder dunit = DUnit.newBuilder();
		ConfPartnerProperty conf = ConfPartnerProperty.get(patnerSn);
		if (conf == null) {
			Log.table.error(" == sendSCPartnerInfo tabel error  sn={}", patnerSn);
			return partnerInfo.build();
		}
		dunit.setId(partner.getId());
		dunit.setName(conf.name);
		dunit.setProfession(conf.type);
		dunit.setLevel(partner.getLevel());
		dunit.setModelSn(patnerSn);
		DProp.Builder dprop = getDProp(partner);
		dunit.setProp(dprop);
		partnerInfo.setUnit(dunit);
		partnerInfo.setCombat(partner.getCombat());
		return partnerInfo.build();
	}

	/**
	 * 依据伙伴sn 初始化伙伴属性 并加入数据库
	 * @param human
	 * @param snProperty 伙伴sn
	 * @return
	 */
	public UnitDataPersistance initPartner(Human human, int snProperty) {
		UnitDataPersistance dataPers = createPartner(human.getId(), snProperty);
		if (dataPers == null) {
			return null;
		}
		/*ConfPartnerProperty conf = ConfPartnerProperty.get(snProperty);
		if (conf == null) {
			Log.table.error("== ConfPartnerProperty table error sn={}", snProperty);
			return null;
		}
		// 发布跑马灯事件
		if (conf.quality == 3 || conf.quality == 4) {
			// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
			String content = Utils.createStr("{}|{}|{}|{}", ParamManager.sysMsgMark, 999002, human.getName(), conf.sn);
			InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
		}*/
		//by killerzyb
		ConfPartnerRecruit conf = ConfPartnerRecruit.get(snProperty);
		if (conf == null) {
			Log.table.error("== ConfPartnerRecruit table error sn={}", snProperty);
			return null;
		}
		// 发布跑马灯事件
		if (conf.needNotice > 0) {
			// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
			String content = Utils.createStr("{}|{}|{}|{}", ParamManager.sysMsgMark, 999002, human.getName(), conf.sn);
			InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
		}
		//by killerzyb
		
		return dataPers;
	}

	/**
	 * 将伙伴添加到主角管理的伙伴Map中，并且录入图鉴信息
	 * @param humanObj
	 * @param partner
	 * @return
	 */
	public PartnerObject addToUnit(HumanObject humanObj, Partner partner) {
		PartnerObject obj = new PartnerObject(humanObj, partner);
		humanObj.partnerMap.put(partner.getId(), obj);
		// 录入图鉴信息,图鉴奖励信息
		HandBook(humanObj, partner.getSn());
		return obj;
	}

	/**
	 * 伙伴初始化进数据库
	 * @return
	 */
	private UnitDataPersistance createPartner(long humanId, int snProperty) {
		ConfPartnerProperty conf = ConfPartnerProperty.get(snProperty);
		if (conf == null) {
			Log.table.error("== ConfPartnerProperty table error sn={}", snProperty);
			return null;
		}
		
		long id = Port.applyId();
		UnitDataPersistance dataPers = new UnitDataPersistance();
		Partner partner = new Partner();
		partner.setHumanId(humanId);
		partner.setId(id);
		partner.setSn(snProperty);// 玩家属性配表sn
		// 根据属性配表设置属性信息
		UnitManager.inst().initProperty(partner, snProperty);

		partner.setAptitude(conf.aptitude);// 资质
		partner.setName(conf.name);
		partner.persist();
		dataPers.unit = partner;

		return dataPers;
	}

	/**
	 * 根据玩法类型，获得出战武将的List
	 * @param type
	 * @return
	 */
	public void getPartnerLineup(HumanObject humanObj, EPartnerLineup type) {
		SCPartnerLineup.Builder msg = SCPartnerLineup.newBuilder();
		DPartnerLineup.Builder dpl = DPartnerLineup.newBuilder();
		dpl.setTypeLineup(EPartnerLineup.LUInst);
		dpl.setTypeStance(EStanceType.valueOf(humanObj.extInfo.getPartnerStance()));
		dpl.addAllIdPartner(getPartnerLineUp(humanObj));
		humanObj.sendMsg(msg);
	}

	/**
	 * 获取上阵伙伴ID列表
	 */
	public List<Long> getPartnerLineUp(HumanObject humanObj) {
		HumanExtInfo exinfo = humanObj.extInfo;
		// 初始化缓存中数据
		List<Long> lineUpInfo = Utils.strToLongList(exinfo.getPartnerLineup());
		return lineUpInfo;
	}

	/**
	 * 判断该伙伴是否上阵
	 */
	public boolean isLineUp(HumanObject humanObj, long pid) {
		List<Long> pidList = getPartnerLineUp(humanObj);
		return pidList.contains(pid);
	}

	/**
	 * 删除武将
	 * 
	 * @param humanObj
	 * @return
	 */
	public void removePartner(HumanObject humanObj, long generalId) {
		// 先从内存里删除
		humanObj.partnerMap.remove(generalId);
		// 从数据库里删除
		DB dbPrx = DB.newInstance(Partner.tableName);
		dbPrx.delete(generalId);
//		// 重新计算属性及战力
//		UnitManager.inst().propCalc(humanObj);

	}

	/**
	 * 重新计算所有单位的数值
	 * 
	 */
	public void propCalcAll(HumanObject humanObj) {
	}

	/**
	 * 构建武将基础信息的数据体
	 * 
	 * @param unit
	 * @return
	 */
	public DPartnerInfo.Builder createDPartnerInfo(CharacterObject unit) {
		DPartnerInfo.Builder msg = DPartnerInfo.newBuilder();
		return msg;
	}

	/**
	 * 获取武将简要信息
	 * 
	 * @param unit
	 * @return
	 */
	public DPartnerBriefInfo.Builder createDPartnerBriefInfo(CharacterObject unit) {
		DPartnerBriefInfo.Builder msg = DPartnerBriefInfo.newBuilder();
		return msg;
	}

	/**
	 * 更换阵容
	 * 
	 */
	public void _msg_CSPartnerChangeLineup(HumanObject humanObj, DPartnerLineup dPartnerLineup) {
		HumanExtInfo exinfo = humanObj.getHumanExtInfo();

		EPartnerLineup typeLineup = dPartnerLineup.getTypeLineup();// 伙伴阵容类型
		EStanceType typeStance = dPartnerLineup.getTypeStance();// 伙伴站位类型
		List<Long> oldList = Utils.strToLongList(exinfo.getPartnerLineup());// 更换之前的Id列表
		List<Long> newList = dPartnerLineup.getIdPartnerList();// 变更后的伙伴ID列表
		int upCount = 0;
		for (long partnerId : newList) {
			if (partnerId>0 && ++upCount>=5) {
				Log.game.info(humanObj+"上阵伙伴数量超过限制！！dPartnerLineup:"+dPartnerLineup);
				return;
			}
		}
		boolean flag = false; //上阵伙伴变化标记
		int len = oldList.size();
		// 从0位置上开始遍历-处理伙伴身上的护法
		for (int i = 0; i < len; i++) {
			long oldPartnerId = oldList.get(i);
			long newPartnerId = newList.get(i);
			PartnerObject oldpoj = humanObj.partnerMap.get(oldPartnerId);
			PartnerObject newpoj = humanObj.partnerMap.get(newPartnerId);
			// 新的阵容里面有有旧的伙伴存在，则continue;
			if (newList.contains(oldPartnerId)) {
				continue;
			} else if (newPartnerId == 0L || newPartnerId == -1) {
				// 下阵该位置的所有护法
				if (oldpoj != null) {
					flag = true;
					Log.game.debug("下阵该位置的所有护法");
					// 清除该位置伙伴的所有护法
					removeAllServent(humanObj, oldpoj);
					// 清除该位置伙伴的所有符文
					RuneManager.inst().process_TakeOffAllRune(humanObj, oldpoj);
				}
			} else {
				// 不更换该位置的护法
				if (oldpoj != null && newpoj != null) {
					flag = true;
					// 将卸下伙伴身上的符文，移接到上阵伙伴上
					RuneManager.inst().process_SwapWearRune(humanObj, oldpoj, newpoj);
					// 将卸下伙伴身上的护法，移接到上阵的伙伴上
					swapServant(humanObj, oldpoj, newpoj);
					// 重新计算新上阵伙伴属性
					UnitManager.inst().initPartnerProps(newpoj);
				} else if (newpoj != null) {
					flag = true;
					// 重新计算新上阵伙伴属性
					UnitManager.inst().initPartnerProps(newpoj);
				}
			}
		}
		// 变更数据库信息
		exinfo.setPartnerLineup(Utils.ListLongToStr(newList));
		exinfo.setPartnerStance(typeStance.getNumber());
		// 通知客户端
		SCPartnerChangeLineup.Builder msg = SCPartnerChangeLineup.newBuilder();
		DPartnerLineup.Builder dpl = DPartnerLineup.newBuilder();
		dpl.setTypeLineup(typeLineup);
		dpl.setTypeStance(typeStance);
		dpl.addAllIdPartner(dPartnerLineup.getIdPartnerList());
		msg.setLineup(dpl);
		humanObj.sendMsg(msg);

		// 同步全局信息
		this.updatePartnerGlobalInfo(humanObj);

		if (flag) {
			//重算玩家总战力
			PartnerPlusManager.inst().sumCombat(humanObj);
		}
	}

	/**
	 * 同步上阵伙伴的信息
	 * 
	 * @param humanObj
	 */
	public void updatePartnerGlobalInfo(HumanObject humanObj) {
		String lineup = humanObj.getHumanExtInfo().getPartnerLineup();
		int stance = humanObj.getHumanExtInfo().getPartnerStance();
		
		List<PartnerObject> partnerGolbInfo = getPartnerList(humanObj);
		// 同步全局信息
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updatePartnerInfo(humanObj.getHumanId(), lineup, stance, partnerGolbInfo);
	}

	/**
	 * 招募伙伴[招募]
	 * 
	 * @param humanObj
	 * @param partnerSn
	 * @param consume
	 *            是否扣费
	 * @param type
	 *            招募类型
	 */
	public PartnerObject recruit(HumanObject humanObj, int partnerSn, boolean consume, EPartnerGetType type) {

		ConfPartnerRecruit conf = ConfPartnerRecruit.get(partnerSn);
		if (conf == null) {
			Log.table.error("== recruit  error sn={} ", partnerSn);
			return null;
		}
		// 扣道具
		if (consume) {
			// 扣道具
			if (!RewardHelper.checkAndConsume(humanObj, conf.itemCost, conf.itemNum, LogSysModType.PartnerRecuit)) {
				return null;
			}
		}
		Human human = humanObj.getHuman();
		// 初始化伙伴
		UnitDataPersistance dataPers = initPartner(human, partnerSn);
		if (dataPers == null) {
			return null;
		}
		// 加入缓存
		Partner partner = (Partner) dataPers.unit;
		PartnerObject po = PartnerManager.inst().addToUnit(humanObj, partner);

		// 初始化法宝
		PartnerManager.inst().initCimelia(po);
		// 发布事件
		Event.fire(EventKey.PartnerUnlocked, "humanObj", humanObj, "sn", partner.getSn());
		// 处理伙伴缘分
		process_fateRelation(humanObj, po);

		// 伙伴直接上阵
		judge_AutoLineUp(humanObj, partner.getId());

		SCPartnerDrop.Builder msg = SCPartnerDrop.newBuilder();
		DPartnerInfo dpInfo = getDPartnerMsg(po);
		msg.setPartnerInfo(dpInfo);
		msg.setType(type);
		humanObj.sendMsg(msg);
		return po;
	}

	/**
	 * 初始化伙伴缘分
	 * @param newPartnerObj 招募到的新伙伴 
	 */
	public void process_fateRelation(HumanObject humanObj, PartnerObject newPartnerObj) {
		int partnerSn = newPartnerObj.getPartner().getSn();
		ConfPartnerRecruit conf = ConfPartnerRecruit.get(partnerSn);
		if (conf == null) {
			Log.table.error("== recruit  initPartnerRelation sn={} ", partnerSn);
			return;
		}
		// 新招募伙伴是否更新缘分列表
		boolean isUpdateFate = false;
		// 新招募伙伴的缘分snList
		List<Integer> newPartnerFateList = new ArrayList<>();
		// 新招募伙伴的缘分sn数组
		int[] fates = conf.fateAll;
		
		// 玩家列表中的伙伴对象
		Partner partner = null;
		// 玩家伙伴列表中伙伴的缘分snList
		List<Integer> partnerFateList = new ArrayList<>();
		// 缘分伙伴sn. 
		int fatePartnerSn = 0;
		// 遍历拥有的所有伙伴
		for (PartnerObject partnerObj : humanObj.partnerMap.values()) {
			// 已有伙伴是否新增缘分
			boolean isAddFate = false;
			partner = partnerObj.getPartner();
			// 伙伴缘分列表
			partnerFateList = Utils.strToIntList(partner.getRelationActive());
			// 遍历新招募伙伴关联的缘分sn
			for (int i = 0; i < fates.length; i++) {
				ConfPartnerFate confFate = ConfPartnerFate.get(fates[i]);
				if (fates[i] == 0) {
					continue;
				}
				// 0是招募的新伙伴sn，1是缘分伙伴sn
				fatePartnerSn = confFate.heroFate[1];
				// 该伙伴 不是关联的缘分伙伴 
				if (partner.getSn() != fatePartnerSn) {
					continue;
				}
				// 激活的缘分列表中，已经有该伙伴的缘分了
				if (partnerFateList.contains(confFate.sn)) {
					continue;
				}
				// 新增已有伙伴的缘分sn
				partnerFateList.add(confFate.sn);
				// 新的缘分
				isAddFate = true;
				
				// 添加新招募伙伴的缘分sn
				if (!newPartnerFateList.contains(confFate.sn)) {
					newPartnerFateList.add(confFate.sn);
					isUpdateFate = true;
				}
			}
			if (isAddFate) {
				// 设置已有伙伴的缘分sns
				partner.setRelationActive(partnerFateList.toString());
				// 计算已有伙伴缘分属性
				UnitManager.inst().propsChange(partnerObj, EntityUnitPropPlus.Fate);
			}
		}
		if (isUpdateFate) {
			// 设置新伙伴的缘分sns
			newPartnerObj.getPartner().setRelationActive(newPartnerFateList.toString());
		}
	}
	
	/**
	 * 下发图鉴信息
	 */
	public void sendSCAddPokedexInfo(HumanObject humanObj, int partnerSn) {
		SCAddPokedexInfo.Builder msg = SCAddPokedexInfo.newBuilder();
		msg.setPartnerSn(partnerSn);
		humanObj.sendMsg(msg);
	}

	/**
	 * 判断是否要自动上阵伙伴
	 */
	private void judge_AutoLineUp(HumanObject humanObj, long partnerId) {
		int humanLv = humanObj.getHuman().getLevel();
		// 不再自动上阵的等级区间，则不做处理
		if (humanLv < ParamManager.recruitAutoLineUpLvRange[0] || humanLv >= ParamManager.recruitAutoLineUpLvRange[1]) {
			return;
		}
		// 小于某个品阶，不自动上阵
		PartnerObject poj = humanObj.partnerMap.get(partnerId);
		if (poj != null) {
			ConfPartnerProperty conf = ConfPartnerProperty.get(poj.getSn());
			if (conf == null) {
				Log.table.info("ConfPartnerProperty ERROR sn ={}", poj.getSn());
				return;
			}
			if (conf.quality < EQualityType.QualityBlue_VALUE) {
				return;
			}
		}

		// 当前阵容
		List<Long> lineupIdList = this.getPartnerLineUp(humanObj);
		// 当前阵形
		int stance = humanObj.extInfo.getPartnerStance();

		// 阵容是否改变
		boolean isChange = false;
		// 获得解锁情况
		List<Boolean> bUnlockList = this.getStanceLockState(stance, humanLv);
		// 遍历当前阵容，查看有没有空位
		for (int i = 0; i < lineupIdList.size(); i++) {
			// M阵形2固定为0, W阵形5固定为0
			if ((stance == EStanceType.StanceM_VALUE && i == 2) || (stance == EStanceType.StanceW_VALUE && i == 5)) {
				continue;
			}
			// 位置上没有英雄并且该阵位已经解锁
			if (lineupIdList.get(i) == 0 && bUnlockList.get(i)) {
				if (lineupIdList.size() - 1 > i) {
					lineupIdList.set(i, partnerId);
					isChange = true;
				}
				break;
			}
		}
		// FIXME 阵形发生改变
		if (isChange) {
			DPartnerLineup.Builder dmsg = DPartnerLineup.newBuilder();
			dmsg.setTypeLineup(EPartnerLineup.LUInst);
			dmsg.setTypeStance(EStanceType.valueOf(stance));
			dmsg.addAllIdPartner(lineupIdList);
			this._msg_CSPartnerChangeLineup(humanObj, dmsg.build());
		}
	}

	/**
	 * 根据玩家信息，获得解锁情况
	 * 
	 * @param stance
	 * @param humanLv
	 * @return 返回解锁列表 列表参数：true解锁，false未解锁
	 */
	public List<Boolean> getStanceLockState(int stance, int humanLv) {
		// 布阵-1~5各阵位解锁等级（W型"前3后2"；M型"前2后3"）
		int[] lockLvs = ParamManager.arrayUnlock;
		List<Integer> lockLvList = Utils.intToIntegerList(lockLvs);
		if (stance == EStanceType.StanceM_VALUE) {
			lockLvList.add(2, 0);
		} else {
			lockLvList.add(5, 0);
		}
		List<Boolean> bUnlockList = new ArrayList<>();
		for (int i = 0; i < lockLvList.size(); i++) {
			// 对应位置的解锁等级
			bUnlockList.add(humanLv >= lockLvList.get(i));
		}
		return bUnlockList;
	}

	/**
	 * 通知客户端，掉落一个新伙伴
	 */
	public void _send_SCParnterDrop(HumanObject humanObj, int partnerSn, EPartnerGetType type) {
		boolean can = PartnerManager.inst().canRecruit(humanObj, 1);
		if (!can) {
			Log.partner.debug("can not be recruit");
			return;
		}
		// 直接招募该伙伴
		recruit(humanObj, partnerSn, false, type);

	}

	/**
	 * 依据Sn判断玩家是否拥有了此武将
	 * 
	 */
	public boolean isExistPartnerSn(HumanObject humanObj, int partnerSn) {
		Map<Long, PartnerObject> partnerObjMap = humanObj.partnerMap;
		for (Map.Entry<Long, PartnerObject> entry : partnerObjMap.entrySet()) {
			if (entry.getValue().getSn() == partnerSn) {
				return true;
			}
		}
		// humanObj.sendSysMsg(190511);// 英雄不存在
		return false;
	}

	/**
	 * 判断玩家是否拥有了此武将
	 * 
	 */
	public boolean isExistPartnerId(HumanObject humanObj, long partnerId) {
		Map<Long, PartnerObject> genObjMap = humanObj.partnerMap;
		if (genObjMap.containsKey(partnerId)) {
			return true;
		}
		return false;
	}

	/**
	 * 依据Id池id取出伙伴
	 * 
	 * @param humanObj
	 * @param id
	 * @return
	 */
	public PartnerObject getPartnerByPortId(HumanObject humanObj, long id) {
		PartnerObject pto = humanObj.partnerMap.get(id);
		return pto;
	}

	/**
	 * 请求，伙伴升级
	 * 
	 */
	public void _msg_CSPartnerAddLevel(HumanObject humanObj, long partnerId, List<Long> costIdList, List<DItem> itemList) {
		// 判断功能是否开放
		// if(!ModunlockManager.inst().isUnlock(1, humanObj.getHuman())){

		// }

		int hasexp = 0;
		PartnerObject pto = humanObj.partnerMap.get(partnerId);
		if (pto == null) {
			humanObj.sendSysMsg(190511);
			Log.human.info("===partnerLevelup 不存在该伙伴");
			return;
		}
		List<Long> lineUp = Utils.strToLongList(humanObj.getHumanExtInfo().getPartnerLineup());
		// 分解costIdList得出资源
		for (Long pid : costIdList) {
			if (!isExistPartnerId(humanObj, pid)) {
				Log.human.error("玩家未拥有该伙伴 humanId={},humanName={},partnerId={}", humanObj.getHumanId(),
						humanObj.getHuman().getName(), pid);
				return;
			}
			// 上阵伙伴不能献祭
			if (lineUp.contains(pid)) {
				Log.human.error("上阵伙伴不可献祭 humanId={},humanName={},partnerId={}", humanObj.getHumanId(),
						humanObj.getHuman().getName(), pid);
				return;
			}
		}

		// 献祭
		hasexp += PartnerManager.inst().decomposePartner(humanObj, costIdList, EPartnerUp.Level);

		// 分解物品
		for (DItem dItem : itemList) {
			int itemSn = dItem.getItemSn();
			int itemNum = dItem.getNum();
			boolean has = RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.PartnerAddLevel);
			if (has && itemSn >= PARTNER_EXP_RANGE[0] && itemNum <= PARTNER_EXP_RANGE[1]) {
				ConfItem conf = ConfItem.get(itemSn);
				if (conf == null) {
					Log.table.error("分解经验相关物品错误，没有对应sn ={}", itemSn);
					continue;
				}
				hasexp += Utils.intValue(conf.param[1]);
			}
		}

		// 加经验
		PartnerPlusManager.inst().partnerExpAdd(humanObj, pto, hasexp, LogSysModType.PartnerAddLevel);
	}

	/**
	 * 依据Id池id，找到对应伙伴Sn
	 */
	public int getPartnerSn(HumanObject humanObj, long id) {
		PartnerObject pto = humanObj.partnerMap.get(id);
		if (pto == null) {
			Log.game.error("玩家id {} 没有该伙伴 ,伙伴id={}", humanObj.getHumanId(), id);
			return 0;
		}
		Partner partner = pto.getPartner();
		return partner.getSn();
	}

	/**
	 * 法宝进阶
	 */
	public void _msg_CSCimeliaAddCont(HumanObject humanObj, long partnerId) {
		PartnerObject partnerObj = humanObj.partnerMap.get(partnerId);
		Cimelia cimelia = partnerObj.getCimeLia();
		int advlevel = cimelia.getAdvLevel();
		int cimeliaSn = partnerObj.getCimeLia().getSn();
		ConfCimelia confcimemlia = ConfCimelia.get(cimeliaSn);
		if (confcimemlia == null) {
			Log.table.error("ConfCimelia cant' find sn ={} ", cimeliaSn);
		}
		int planId = confcimemlia.planId;
		int sn = planId * 100 + advlevel;
		ConfCimeliaConstitutions conf = ConfCimeliaConstitutions.get(sn);
		// 等级是否达到条件
		if (conf == null) {
			Log.table.info("配置表错误 ===PartnerConstitution  sn={}", sn);
		}
		Human human = humanObj.getHuman();
		if (human == null) {
			Log.table.info("human == null");
		}
		if (partnerObj.getPartner().getLevel() < conf.rolelevQm) {
			// 伙伴等级不足,不能突破
			humanObj.sendSysMsg(360105);// 英雄的等級未達到要求
			return;
		}

		int nowAdvLevel = cimelia.getAdvLevel();

		// 如果没有下一个则达到上限
		ConfPartnerConstitutions nextconf = ConfPartnerConstitutions.get(sn + 1);
		if (nextconf == null) {
			Log.human.info("伙伴品质已达到上限制");
		}
		// 判断消耗
		int[] itemSn = Utils.appendInt(conf.itemSn, EMoneyType.coin_VALUE);
		int[] itemNum = Utils.appendInt(conf.itemNum, conf.costCoin);
		// // 扣道具
		if (!RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.PartnerAddCont)) {
			humanObj.sendSysMsg(215910);
			Log.item.info("道具不足");
			return;
		}

		// 获取品阶
		// 校验玩家身上是否有足够多该品阶的伙伴

		// 增加品阶入库，更新缓存
		nowAdvLevel++;
		cimelia.setAdvLevel(nowAdvLevel);

		// 法宝突破属性重新计算
		UnitManager.inst().propsChange(partnerObj, EntityUnitPropPlus.CimeliaAdv);

		humanObj.partnerMap.put(partnerId, partnerObj);

		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", 0, "type", ActivitySevenTypeKey.Type_59);

		// 发送给客户端
		SCCimeliaAddCont.Builder msg = SCCimeliaAddCont.newBuilder();
		msg.setResult(true);
		msg.setPartnerId(partnerId);
		msg.setCurCont(nowAdvLevel);
		humanObj.sendMsg(msg);
	}

	/**
	 * 伙伴进阶
	 * 
	 */
	public void _msg_CSPartnerAddCont(HumanObject humanObj, long partnerId, List<Long> costIdList) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModePartnerAdvance, humanObj)) {
			Log.skill.error("===_msg_CSPartnerAddCont 伙伴突破未开放 ===");
			return;
		}

		// 检测是否有该武将
		if (!isExistPartnerId(humanObj, partnerId)) {
			Log.human.error("玩家未拥有该伙伴 humanId={},humanName={},partnerId={}", humanObj.getHumanId(),
					humanObj.getHuman().getName(), partnerId);
			return;
		}
		
		// 2升2读伙伴阶数=2的配置信息
		PartnerObject partnerObj = getPartnerByPortId(humanObj, partnerId);
		Partner partner = partnerObj.getPartner();
		int advlevel = partner.getAdvLevel();
		ConfPartnerConstitutions conf = getConfPartnerConstitution(partner.getSn(), advlevel);
		// 等级是否达到条件
		if (conf == null) {
			Log.table.info("配置表错误 ===PartnerConstitution  advlevel = {}", advlevel);
			return;
		}
		if (humanObj.getHuman().getLevel() < conf.rolelevQm) {
			// 伙伴等级不足,不能突破
			humanObj.sendSysMsg(360105);// 英雄的等級未達到要求
			return;
		}

		// 如果没有下一个则达到上限
		ConfPartnerConstitutions nextconf = getConfPartnerConstitution(partner.getSn(), advlevel + 1);
		if (nextconf == null) {
			Log.human.debug("伙伴品质已达到上限制");
			return;
		}

		// 判断消耗
		int[] itemSn = Utils.appendInt(conf.itemSn, EMoneyType.coin_VALUE);
		int[] itemNum = Utils.appendInt(conf.itemNum, conf.costCoin);
		//  扣道具
		if (!RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.PartnerAddCont)) {
			humanObj.sendSysMsg(215910);
			Log.item.debug("道具不足");
			return;
		}

		// 分解相同品阶的伙伴数量
		int costPartnerNum = conf.customPartner;
		// 获取品阶
		int quality = PartnerPlusManager.inst().getQuality(partner.getSn());
		// 校验玩家身上是否有足够多该品阶的伙伴

		HumanExtInfo exinfo = humanObj.getHumanExtInfo();
		List<Long> idLists = Utils.strToLongList(exinfo.getPartnerLineup());

		// 检测是否有带献祭的伙伴
		for (Long pid : costIdList) {
			if (!isExistPartnerId(humanObj, pid)) {
				Log.human.error("玩家未拥有该伙伴 humanId={},humanName={},partnerId={}", humanObj.getHumanId(),
						humanObj.getHuman().getName(), pid);
				return;
			}

			if (idLists.contains(pid)) {
				Log.human.error("上阵伙伴不能献祭 humanId={},humanName={},partnerId={}", humanObj.getHumanId(),
						humanObj.getHuman().getName(), pid);
			}
			PartnerObject p = humanObj.partnerMap.get(pid);
			if (p == null) {
				Log.human.error(" partnerAddCont 该伙伴为空 humanId={},humanName={},partnerId={}", humanObj.getHumanId(),
						humanObj.getHuman().getName(), pid);
				return;
			}
			int pSn = p.getPartner().getSn();
			int partnerQuality = PartnerPlusManager.inst().getQuality(pSn);
			if (partnerQuality != quality) {
				humanObj.sendSysMsg(360105);// 英雄的等級未達到要求(品阶条件不满足)
				return;
			}
		}
		if (costPartnerNum != costIdList.size()) {
			Log.human.info("错误的突破献祭数量");
			return;
		}
		
		// 献祭 并返回
		List<ProduceVo> list = new ArrayList<>();
		
		for (Long pid : costIdList) {
			DropBag ireward = resolvePartner(humanObj, pid, true, EPartnerUp.Advance);
			if (ireward == null) {
				continue;
			}
			int[] itemSn_ = ireward.getItemSn();
			int[] itemNum_ = ireward.getItemNum();
			for (int i = 0; i < itemSn_.length; i++) {
				ProduceVo dt = new ProduceVo(itemSn_[i],itemNum_[i]);
				list.add(dt);
			}
		}
		
		
		
		
		
		RewardHelper.reward(humanObj, list, LogSysModType.PartnerResolve);
		
		// 增加品阶入库，更新缓存
		int curAdv = advlevel + 1;
		partner.setAdvLevel(curAdv);
		int oldApt = partner.getAptitude();
		// 当前资质
		int curApt = oldApt + nextconf.addAptitude;
		partner.setAptitude(curApt);

		// 突破属性重新计算
		UnitManager.inst().propsChange(partnerObj, EntityUnitPropPlus.Advance);
		if (oldApt != curApt) {
			// 资质影响 等级属性 重新计算
			UnitManager.inst().propsChange(partnerObj, EntityUnitPropPlus.Level);
		}
		humanObj.partnerMap.put(partnerId, partnerObj);

		// 发布事件-伙伴进阶
		Event.fire(EventKey.PartnerAdvanced, "humanObj", humanObj);

		// 发送给客户端
		SCPartnerAddCont.Builder msg = SCPartnerAddCont.newBuilder();
		msg.setResult(true);
		msg.setPartnerId(partnerId);
		msg.setCurCont(curAdv);
		humanObj.sendMsg(msg);
	}

	/**
	 * 分解伙伴,返回获得的经验
	 * 
	 * @param humanObj
	 * @param partnerIDList
	 */
	public int decomposePartner(HumanObject humanObj, List<Long> partnerIDList, EPartnerUp type) {
		SCNewDecomposeAll.Builder msg = SCNewDecomposeAll.newBuilder();
		int hasExp = 0;
		for (Long partnerId : partnerIDList) {
			int[] itemSn = {};
			int[] itemNum = {};
			DropBag ireward = resolvePartner(humanObj, partnerId, true, type);
			itemSn = Utils.concatAll_Int(itemSn, ireward.getItemSn());
			itemNum = Utils.concatAll_Int(itemNum, ireward.getItemNum());
			hasExp += ireward.getPartnerExpInBag();
		}
		msg.addAllPartnerId(partnerIDList);
		msg.setIsDescompose(true);
		// 发送客户端
		humanObj.sendMsg(msg);
		// 从DropBag中取出经验池
		return hasExp;
	}

	/**
	 * 分解伙伴，依据sn(抽卡的时候重复卡片的分解，不同于伙伴分解)
	 * 
	 * @param humanObj
	 * @param partnerSn
	 * @return {itemSn,itemNum}
	 */
	public List<DDropItem> decomposePartner(HumanObject humanObj, int partnerSn) {
		return this.decomposePartner(humanObj, partnerSn, 1);
	}

	/**
	 * 分解伙伴，依据sn(抽卡的时候重复卡片的分解，不同于伙伴分解)
	 * 
	 * @param humanObj
	 * @param partnerSn
	 * @param num
	 *            一次性分解几个
	 * @return {itemSn,itemNum}
	 */
	public List<DDropItem> decomposePartner(HumanObject humanObj, int partnerSn, int num) {
		int index = 120000 + partnerSn;
		ConfItem conf = ConfItem.get(index);
		if (conf == null || conf.param.length < 3) {
			Log.table.error("==decomposePartner error sn={}", index);
		}
		List<DDropItem> dpList = new ArrayList<>();
		int itemSn = Integer.valueOf(conf.param[1]);
		int itemNum = Integer.valueOf(conf.param[2]);
		itemNum *= num;
		DDropItem.Builder dp = DDropItem.newBuilder();
		dp.setItemSn(itemSn);
		dp.setItemNum(itemNum);
		dpList.add(dp.build());
		// 发送奖励
		RewardHelper.reward(humanObj, itemSn, itemNum, LogSysModType.PartnerResolve);

		return dpList;
	}



	public List<ProduceVo> getDecomposePartnerList(HumanObject humanObj, List<Long> partnerIDList, boolean isDescompose) {
		List<ProduceVo> itemList = new ArrayList<>();
		for (Long partnerId : partnerIDList) {
			DropBag ireward = resolvePartner(humanObj, partnerId, isDescompose, EPartnerUp.Decompose);
			if (ireward == null) {
				continue;
			}
			int[] itemSn = ireward.getItemSn();
			int[] itemNum = ireward.getItemNum();
			for (int i = 0; i < itemSn.length; i++) {
				ProduceVo dt = new ProduceVo(itemSn[i],itemNum[i]);
				itemList.add(dt);
			}
		}
		return itemList;
	}

	/**
	 * 分解一个伙伴,返回得到的物品
	 * 
	 * @param humanObj
	 */
	private DropBag resolvePartner(HumanObject humanObj, long partnerId, boolean isDecompose, EPartnerUp type) {
		if (!isExistPartnerId(humanObj, partnerId)) {
			Log.table.info(" function is resolvePartner 没有这个小伙伴 partnerId={}", partnerId);
			return null;
		}
		PartnerObject poj = humanObj.partnerMap.get(partnerId);
		Partner partner = poj.getPartner();
		int partnerSn = partner.getSn();
		// 删除武将
		if (isDecompose) {
			removePartner(humanObj, partnerId);
		}
		// 发送分解资源 如果是升级才要分解这张表
		// PartnerRecruit表分解--原始表
		ConfPartnerRecruit conf = ConfPartnerRecruit.get(partnerSn);
		int[] itemSnRecruit = {};
		int[] itemNumRecruit = {};
		if (conf != null) {
			if (type == EPartnerUp.Level) {
				itemSnRecruit = conf.levelHash;
				itemNumRecruit = conf.levelHashNum;
			} else if (type == EPartnerUp.Advance) {
				itemSnRecruit = conf.advHash;
				itemNumRecruit = conf.advHashNum;
			} else if (type == EPartnerUp.Cultivation) {
				itemSnRecruit = conf.cultivationHash;
				itemNumRecruit = conf.cultivationHashNum;
			} else if (type == EPartnerUp.Decompose) {
				itemSnRecruit = conf.resolveHash;
				itemNumRecruit = conf.resolveHashNum;
			}
		}

		// PartnerConstitutions表分解
		int plantId = conf.constitutionPlanId;
		int plantAdvlevel = partner.getAdvLevel();// 品质
		int ConstitutionSn = plantId * 100 + plantAdvlevel;

		ConfPartnerConstitutions confCons = ConfPartnerConstitutions.get(ConstitutionSn);
		int[] itemConst = {};
		int[] itemConstNum = {};
		if (confCons != null) {
			if (type == EPartnerUp.Level) {
				itemConst = confCons.levelHash;
				itemConstNum = confCons.levelHashNum;
			} else if (type == EPartnerUp.Advance) {
				itemConst = confCons.advHash;
				itemConstNum = confCons.advHashNum;
			} else if (type == EPartnerUp.Cultivation) {
				itemConst = confCons.cultivationHash;
				itemConstNum = confCons.cultivationHashNum;
			} else if (type == EPartnerUp.Decompose) {
				itemConst = confCons.resolveHash;
				itemConstNum = confCons.resolveHashNum;
			}
		}

		// PartnerStartUp表分解

		int start = partner.getStar();
		int startUpSn = partner.getSn() * Utils.I100 + start;
		ConfPartnerStarUp confStart = ConfPartnerStarUp.get(startUpSn);
		int[] itemStartUp = {};
		int[] itemStartUpNum = {};
		if (confStart != null) {
			if (type == EPartnerUp.Level) {
				itemStartUp = confStart.levelHash;
				itemStartUpNum = confStart.levelHashNum;
			} else if (type == EPartnerUp.Advance) {
				itemStartUp = confStart.advHash;
				itemStartUpNum = confStart.advHashNum;
			} else if (type == EPartnerUp.Cultivation) {
				itemStartUp = confStart.cultivationHash;
				itemStartUpNum = confStart.cultivationHashNum;
			} else if (type == EPartnerUp.Decompose) {
				itemStartUp = confStart.resolveHash;
				itemStartUpNum = confStart.resolveHashNum;
			}
		}

		// LevelExp表分解

		int levelSn = partner.getLevel();
		ConfLevelExp expConf = ConfLevelExp.get(levelSn);
		int[] itemLvSn = {};
		int[] itemLvNum = {};
		if (expConf != null) {
			if (type == EPartnerUp.Level) {
				itemLvSn = expConf.levelHash;
				itemLvNum = expConf.levelHashNum;
			} else if (type == EPartnerUp.Advance) {
				itemLvSn = expConf.advHash;
				itemLvNum = expConf.advHashNum;
			} else if (type == EPartnerUp.Cultivation) {
				itemLvSn = expConf.cultivationHash;
				itemLvNum = expConf.cultivationHashNum;
			} else if (type == EPartnerUp.Cultivation) {
				itemLvSn = expConf.cultivationHash;
				itemLvNum = expConf.cultivationHashNum;
			} else if (type == EPartnerUp.Decompose) {
				itemLvSn = expConf.resolveHash;
				itemLvNum = expConf.resolveHashNum;
			}
		}

		/*
		 * 分解法宝
		 */
		// 发送分解资源 如果是升级才要分解这张表
		// Cimelia表分解--原始表

		Cimelia cimelia = poj.getCimeLia();
		int[] cimelia_Recruit = {};
		int[] cimelia_Recruit_num = {};
		int[] cimelia_itemConst = {};
		int[] cimelia_itemConstNum = {};
		int[] cimelia_itemStartUp = {};
		int[] cimelia_itemStartUpNum = {};
		int[] cimelia_itemLvSn = {};
		int[] cimelia_itemLvNum = {};
		if (cimelia != null) {
			ConfCimelia conf_Cimelia = ConfCimelia.get(cimelia.getSn());

			if (conf_Cimelia != null) {
				if (type == EPartnerUp.Level) {
					cimelia_Recruit = conf_Cimelia.resolveHash;
					cimelia_Recruit_num = conf_Cimelia.resolveHashNum;
				} else if (type == EPartnerUp.Advance) {
					cimelia_Recruit = conf_Cimelia.advHash;
					cimelia_Recruit_num = conf_Cimelia.advHashNum;
				} else if (type == EPartnerUp.Cultivation) {
					cimelia_Recruit = conf_Cimelia.cultivationHash;
					cimelia_Recruit_num = conf_Cimelia.cultivationHashNum;
				} else if (type == EPartnerUp.Decompose) {
					cimelia_Recruit = conf_Cimelia.resolveHash;
					cimelia_Recruit_num = conf_Cimelia.resolveHashNum;
				}
			}

			// CimeliaConstitutions表分解
			int cimelia_plantId = conf_Cimelia.planId;
			int cimelia_plantAdvlevel = cimelia.getAdvLevel();// 品质
			int cimelia_ConstitutionSn = cimelia_plantId * 100 + cimelia_plantAdvlevel;

			ConfCimeliaConstitutions confCons_cimelia = ConfCimeliaConstitutions.get(cimelia_ConstitutionSn);

			if (confCons_cimelia != null) {
				if (type == EPartnerUp.Level) {
					cimelia_itemConst = confCons_cimelia.levelHash;
					cimelia_itemConstNum = confCons_cimelia.levelHashNum;
				} else if (type == EPartnerUp.Advance) {
					cimelia_itemConst = confCons_cimelia.advHash;
					cimelia_itemConstNum = confCons_cimelia.advHashNum;
				} else if (type == EPartnerUp.Cultivation) {
					cimelia_itemConst = confCons_cimelia.cultivationHash;
					cimelia_itemConstNum = confCons_cimelia.cultivationHashNum;
				} else if (type == EPartnerUp.Decompose) {
					cimelia_itemConst = confCons_cimelia.resolveHash;
					cimelia_itemConstNum = confCons_cimelia.resolveHashNum;
				}
			}

			// CimeliaStartUp表分解

			int cimelia_start = cimelia.getStar();
			int cimelia_startUpSn = cimelia.getSn() * 100 + cimelia_start;
			game.worldsrv.config.ConfCimeliaStarUp cimelia_confStart = game.worldsrv.config.ConfCimeliaStarUp.get(cimelia_startUpSn);

			if (cimelia_confStart != null) {
				if (type == EPartnerUp.Level) {
					cimelia_itemStartUp = cimelia_confStart.levelHash;
					cimelia_itemStartUpNum = cimelia_confStart.levelHashNum;
				} else if (type == EPartnerUp.Advance) {
					cimelia_itemStartUp = cimelia_confStart.advHash;
					cimelia_itemStartUpNum = cimelia_confStart.advHashNum;
				} else if (type == EPartnerUp.Cultivation) {
					cimelia_itemStartUp = cimelia_confStart.cultivationHash;
					cimelia_itemStartUpNum = cimelia_confStart.cultivationHashNum;
				} else if (type == EPartnerUp.Decompose) {
					cimelia_itemStartUp = cimelia_confStart.resolveHash;
					cimelia_itemStartUpNum = cimelia_confStart.resolveHashNum;
				}
			}

			// LevelExp表分解

			int cimelia_levelSn = cimelia.getLevel();
			ConfLevelExp cimelia_expConf = ConfLevelExp.get(cimelia_levelSn);

			if (cimelia_expConf != null) {
				if (type == EPartnerUp.Level) {
					cimelia_itemLvSn = cimelia_expConf.cimeliaLevelHash;
					cimelia_itemLvNum = cimelia_expConf.cimeliaLevelHashNum;
				} else if (type == EPartnerUp.Advance) {
					cimelia_itemLvSn = cimelia_expConf.cimeliaAdvHash;
					cimelia_itemLvNum = cimelia_expConf.cimeliaAdvHashNum;
				} else if (type == EPartnerUp.Decompose) {
					cimelia_itemLvSn = cimelia_expConf.cimeliaResolveHash;
					cimelia_itemLvNum = cimelia_expConf.cimeliaResolveHashNum;
				}
			}

		}

		// 返还总计
		int[] itemSn = Utils.concatAll_Int(itemSnRecruit, itemConst, itemStartUp, itemLvSn, cimelia_itemLvSn,
				cimelia_itemStartUp, cimelia_itemConst, cimelia_Recruit);
		int[] itemNum = Utils.concatAll_Int(itemNumRecruit, itemConstNum, itemStartUpNum, itemLvNum, cimelia_itemLvNum,
				cimelia_itemStartUpNum, cimelia_itemConstNum, cimelia_Recruit_num);
		DropBag ireward = new DropBag(itemSn, itemNum);

		return ireward;
	}

	/**
	 * 招募伙伴并下发信息(碎片招募)
	 * 
	 * @param humanObj
	 * @param sn
	 * @param bool
	 *            是否扣费
	 */
	public void beckonsPartner(HumanObject humanObj, int sn, boolean bool) {
		boolean can = PartnerManager.inst().canRecruit(humanObj, 1);
		if (!can) {
			return;
		}
		recruit(humanObj, sn, bool, EPartnerGetType.Debris);
	}

	/**
	 * 记录玩家每个招募过的武将, 用于武将缘分 每次新增伙伴都来判断一下是否激活新的图鉴
	 * 
	 * 获取新增的rewardList
	 * 
	 * @param humanObj
	 */
	public void HandBook(HumanObject humanObj, int sn) {
		if (sn == 0) {
			return;
		}

		/* 图鉴部分 */
		HumanExtInfo exinfo = humanObj.extInfo;
		// 已有图鉴(已经激活过的伙伴)
		String activityHandStr = exinfo.getActivityHand();

		List<Integer> activityHandList = Utils.strToIntList(activityHandStr);
		if (activityHandList.contains(0) && activityHandList.size() == 1) {
			// activityHandList
			activityHandList = new ArrayList<>();
		}

		if (activityHandList.contains(sn)) {
			// 老伙计
			return;
		}
		// 如果是新伙伴
		activityHandList.add(sn);

		// TODO 发布事件 现在迁移到addUnit
		// Event.fire(EventKey.PartnerUnlocked,"humanObj",humanObj, "sn", sn);
		// int quality = ConfPartnerProperty.get(sn).quality;
		exinfo.setActivityHand(Utils.intListToStr(activityHandList));

		// 下发图鉴
		sendSCAddPokedexInfo(humanObj, sn);

		/* 图鉴奖励 */

		// 配置表Map
		Map<Integer, List<Integer>> rewardFateMap = GlobalConfVal.getPokedexFateMap();
		// 已获取的奖励列表pin
		List<Integer> alreadyrewardList = Utils.strToIntList(exinfo.getAlreadyGetReword());

		if (alreadyrewardList.contains(0) && alreadyrewardList.size() == 1) {
			// activityHandList
			alreadyrewardList = new ArrayList<>();
		}

		for (Map.Entry<Integer, List<Integer>> entry : rewardFateMap.entrySet()) {
			// 配表上 heroFate
			List<Integer> confFate = entry.getValue();
			// 如果已经有了就直接continue
			Integer rewardID = entry.getKey();
			if (alreadyrewardList.contains(rewardID)) {
				continue;
			}
			if (activityHandList.containsAll(confFate)) {
				alreadyrewardList.add(rewardID);

			}
		}
		exinfo.setAlreadyGetReword(Utils.intListToStr(alreadyrewardList));
	}

	/**
	 * 领取图鉴集合奖励
	 */
	public void getHandBookReword(HumanObject humanObj, int pokedexGroupId) {
		// 坚持是否达成
		String rewordListStr = humanObj.extInfo.getAlreadyGetReword();
		List<Integer> rewordList = Utils.strToIntList(rewordListStr);
		ConfPartnerPokedexFate cppf = ConfPartnerPokedexFate.get(pokedexGroupId);
		int rewardId = cppf.rewardID;
		if (!rewordList.contains(rewardId)) {
			// 未达成
			return;
		}
		// 是否领取过
		HumanExtInfo exInfo = humanObj.extInfo;
		String activityRewordStr = exInfo.getActivityReword();
		List<Integer> activityRewordLsit = Utils.strToIntList(activityRewordStr);
		if (activityRewordLsit.contains(rewardId)) {
			// 已领取
			System.out.println("已经领取过了");
			return;
		}
		// 领取
		activityRewordLsit.add(rewardId);
		exInfo.setActivityReword(Utils.intListToStr(activityRewordLsit));

		// 发送奖励
		ConfRewards conf = ConfRewards.get(rewardId);
		RewardHelper.reward(humanObj, conf.itemSn, conf.itemNum, LogSysModType.Partner);

		// 回给客户端
		SCGetPokedexGroupReward.Builder msg = SCGetPokedexGroupReward.newBuilder();
		for (int i = 0; i < conf.itemSn.length; i++) {
			DItem.Builder ditem = DItem.newBuilder();
			ditem.setItemSn(conf.itemSn[i]);
			ditem.setNum(conf.itemNum[i]);
			msg.addItemList(ditem);
		}
		msg.setPokedexGroupId(pokedexGroupId);
		humanObj.sendMsg(msg);
	}

	/**
	 * 获取DProp协议
	 * 
	 * @return
	 */
	public static DProp.Builder getDProp(Partner partner) {
		DProp.Builder dProp = DProp.newBuilder();
		dProp.setHpMax(partner.getHpMax());

		dProp.setAtkPhy(partner.getAtkPhy());
		dProp.setDefPhy(partner.getDefPhy());
		dProp.setAtkMag(partner.getAtkMag());
		dProp.setDefMag(partner.getDefMag());

		dProp.setHit(partner.getHit());
		dProp.setDodge(partner.getDodge());
		dProp.setBlock(partner.getBlock());
		dProp.setCrit(partner.getCrit());
		dProp.setCritAdd(partner.getCritAdd());
		dProp.setAntiCrit(partner.getAntiCrit());
		return dProp;
	}

	/**
	 * 制作一个镜像数据
	 * 
	 */
	public PartnerObject getMirrorPartnerObject(long humanId, long partnerId, int snProperty) {
		Partner partner = new Partner();
		partner.setHumanId(humanId);
		partner.setId(partnerId);
		partner.setSn(snProperty);
		// 根据属性配表设置属性信息
		UnitManager.inst().initProperty(partner, snProperty);

		PartnerObject pOj = new PartnerObject(partner);
		return pOj;
	}

	/**
	 * 获取可得列表分解碎片
	 * 
	 * @param humanObj
	 * @param itemID
	 * @param isDescompose
	 *            是否分解
	 */
	public List<DItem> DecomposeItemMsg(HumanObject humanObj, List<DItem> itemID, boolean isDescompose) {
		// SCDecomposeItemMsg.Builder msg = SCDecomposeItemMsg.newBuilder();
		List<DItem> ditems = new ArrayList<>();
		for (DItem dItem : itemID) {
			// 扣除
			int itemSn = dItem.getItemSn();
			int itemNum = dItem.getNum();
			// 扣道具
			if (isDescompose) {
				if(!RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.PartnerResolve)) {
					continue;
				}
				
			}

			ConfItem conf = ConfItem.get(itemSn);
			int[] dec_itemSn = conf.hashId;
			int[] dec_itemNum = conf.hashNum;

			// 组DItem
			for (int i = 0; i < dec_itemSn.length; i++) {
				DItem.Builder dt = DItem.newBuilder();
				int desSn = dec_itemSn[i];
				int desNum = dec_itemNum[i] * itemNum;
				ditems.add(dt.build());
				dt.setItemSn(desSn);
				dt.setNum(desNum);
				// msg.addItemList(dt);
				if (isDescompose) {
					RewardHelper.reward(humanObj, desSn, desNum, LogSysModType.PartnerResolve);
				}

			}

		}
		// msg.setIsDescompose(isDescompose);
		// humanObj.sendMsg(msg);
		return ditems;
	}

	public List<DItem> DecomposeItemMsg(HumanObject humanObj, List<DItem> itemID) {
		List<DItem> ditems = new ArrayList<>();
		for (DItem dItem : itemID) {
			int itemSn = dItem.getItemSn();
			int itemNum = dItem.getNum();

			ConfItem conf = ConfItem.get(itemSn);
			int[] dec_itemSn = conf.hashId;
			int[] dec_itemNum = conf.hashNum;

			for (int i = 0; i < dec_itemSn.length; i++) {
				DItem.Builder dt = DItem.newBuilder();
				int desSn = dec_itemSn[i];
				int desNum = dec_itemNum[i] * itemNum;
				dt.setItemSn(desSn);
				dt.setNum(desNum);
				ditems.add(dt.build());

			}

		}
		return ditems;
	}

	/**
	 * 传入伙伴等级，返回达到该等级的伙伴数量
	 * 
	 * @param humanObj
	 * @param level
	 *            等级
	 */
	public int hasNumInLevel(HumanObject humanObj, int level) {
		int num = 0;
		Map<Long, PartnerObject> pList = humanObj.partnerMap;
		for (Entry<Long, PartnerObject> entry : pList.entrySet()) {
			Partner partner = entry.getValue().getPartner();
			if (partner.getLevel() >= level) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 传入伙伴进阶等级，返回达到该等级的伙伴数量
	 * 
	 * @param humanObj
	 * @param Advanced
	 *            等级
	 */
	public int hasNumAdvanced(HumanObject humanObj, int Advanced) {
		int num = 0;
		Map<Long, PartnerObject> pList = humanObj.partnerMap;
		for (Entry<Long, PartnerObject> entry : pList.entrySet()) {
			Partner partner = entry.getValue().getPartner();
			if (partner.getAdvLevel() >= Advanced) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 传入伙伴星级，返回达到该等级的伙伴数量
	 * 
	 * @param humanObj
	 * @param start
	 *            等级
	 */
	public int hasNumStart(HumanObject humanObj, int start) {
		int num = 0;
		Map<Long, PartnerObject> pList = humanObj.partnerMap;
		for (Entry<Long, PartnerObject> entry : pList.entrySet()) {
			Partner partner = entry.getValue().getPartner();
			// 一星 = 5个节点
			int curStar = partner.getStar() / 5;
			if (curStar >= start) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 传入伙伴品阶（颜色），返回该品阶的伙伴数量
	 * 
	 * @param humanObj
	 * @param quality
	 *            品阶
	 */
	public int hasNumQuality(HumanObject humanObj, int quality) {
		int num = 0;
		Map<Long, PartnerObject> pList = humanObj.partnerMap;
		ConfPartnerProperty conf = null;
		for (Entry<Long, PartnerObject> entry : pList.entrySet()) {
			Partner partner = entry.getValue().getPartner();
			int sn = partner.getSn();
			conf = ConfPartnerProperty.get(sn);
			if (conf == null) {
				continue;
			}
			if (quality == conf.quality) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 发送伙伴属性
	 */
	public void sendSCPropInfoChange(HumanObject humanObj, PartnerObject partnerObj) {
		sendSCPropInfoChange(humanObj, partnerObj, EPropChangeType.PropChangeNone);
	}

	/**
	 * 发送伙伴属性
	 * 
	 * @param humanObj
	 * @param partnerObj
	 * @param changeType
	 *            变化类型
	 */
	public void sendSCPropInfoChange(HumanObject humanObj, PartnerObject partnerObj, EPropChangeType changeType) {
		SCPartnerPropInfoChange.Builder msg = SCPartnerPropInfoChange.newBuilder();
		DProp dProp = UnitManager.inst().getDProp(partnerObj.getPartner());
		msg.setId(partnerObj.getPartner().getId());
		msg.setProp(dProp);
		msg.setType(changeType);
		msg.setCombat(partnerObj.getPartner().getCombat());
		humanObj.sendMsg(msg);
	}

	/**
	 * 获取上阵伙伴信息
	 */
	public List<PartnerObject> getPartnerList(HumanObject humanObj) {
		HumanExtInfo exinfo = humanObj.getHumanExtInfo();
		Map<Long, PartnerObject> partnerMap = humanObj.partnerMap;
		String ldList = exinfo.getPartnerLineup();
		List<Long> idLists = Utils.strToLongList(ldList);
		List<PartnerObject> partnerlist = new ArrayList<>();
		for (Long id : idLists) {
			PartnerObject poj = partnerMap.get(id);
			if (poj == null)
				continue;
			partnerlist.add(poj);
		}
		return partnerlist;
	}

	/**
	 * 给爬塔用的获取最强战斗力
	 */
	public int getHumanHighestCombat(HumanObject humanObj) {
		Map<Long, PartnerObject> partnerMap = humanObj.partnerMap;
		List<Integer> combatList = new ArrayList<>();
		if (partnerMap == null) {
			Log.game.info("== getHumanHighestCombat humanId ={}的伙伴阵容为0", humanObj.getHumanId());
			return 0;
		}
		for (Entry<Long, PartnerObject> po : partnerMap.entrySet()) {
			if (po.getValue() != null) {
				int combat = po.getValue().getPartner().getCombat();
				combatList.add(combat);
			}
		}
		// 排序
		combatList.sort((id1, id2) -> id2 - id1);
		int maxCombat = humanObj.getHuman().getCombat();
		int i = 0;
		for (Integer cb : combatList) {
			if (i >= 4) {
				break;
			}
			maxCombat += cb;
			i++;
		}
		return maxCombat;

	}

	// /**
	// * 添加随从
	// * @param humanObj
	// * @param partnerId
	// * @param servantId
	// * @param index
	// */
	// public void addServant(HumanObject humanObj, long partnerId, long servantId,
	// int index) {
	// //检测随从系统是否开放
	// ModunlockManager.inst().isUnlock(Define.EModeType.ModeServant,humanObj);
	//
	// if(PartnerManager.inst().isExistPartnerId(humanObj,partnerId) ){
	// PartnerObject pto = humanObj.partnerMap.get(partnerId);
	// pto.addServant(humanObj,partnerId,servantId,index);
	// }
	//
	// }
	/**
	 * 删除随从
	 * 
	 * @param humanObj
	 * @param partnerId
	 * @param servantId
	 * @param index
	 */
	public void _msg_CSRemoveServan(HumanObject humanObj, long partnerId, long servantId, int index) {
		PartnerObject partnerObject = humanObj.partnerMap.get(partnerId);
		if (partnerObject == null)
			return;
		boolean success = removeServent(humanObj, partnerId, servantId, index);
		SCRemoveServant.Builder msg = SCRemoveServant.newBuilder();
		if (success) {
			// 重新计算属性
			UnitManager.inst().propsChange(partnerObject, EntityUnitPropPlus.Servant);
			msg.setSuccess(true);
		} else {
			msg.setSuccess(false);
		}
		DProp.Builder dp = DProp.newBuilder();
		msg.setPartnerId(partnerId);
		msg.setServantId(servantId);
		msg.setIndex(index);
		msg.setAddProp(dp);
		humanObj.sendMsg(msg);
	}

	/**
	 * 设置随从位置
	 */
	public void setServentByIndex(HumanObject humanObj, PartnerObject poj, int index, long id) {
		List<Long> idl = poj.getServantList();
		if (idl.size() - 1 < index) {
			idl.add(id);
		} else {
			idl.set(index, id);
		}
		poj.setServantList(humanObj, idl);

		// 同步全局信息
		updatePartnerGlobalInfo(humanObj);
	}

	/**
	 * 添加伙伴为该伙伴随从
	 * @param humanObj
	 * @param partnerId 伙伴ID
	 * @param servantId 要添加的随从ID
	 * @param index 要添加的位置 0是第一个
	 * @return boolean 是否添加成功
	 */
	public void _msg_CSAddServant(HumanObject humanObj, long partnerId, long servantId, int index) {
		// 检测随从系统是否开放
		ModunlockManager.inst().isUnlock(Define.EModeType.ModeServant, humanObj);

		PartnerObject pObj = humanObj.partnerMap.get(partnerId);

		// 检测是否拥有该伙伴
		if (pObj == null) {
			return;
		}
		// 检测是否拥有该随从
		boolean hasser = PartnerManager.inst().isExistPartnerId(humanObj, servantId);
		if (!hasser) {
			return;
		}
		// 检测该位置是否开放
		boolean can = servantLock(humanObj);
		// if(!can){
		// Log.partner.info("Location not open yet
		// humanId={},index={}",humanObj.getHumanId(),index);
		// return;
		// }

		// 上阵的伙伴不能作为护法
		boolean isLineUp = PartnerManager.inst().isLineUp(humanObj, servantId);
		if (isLineUp) {
			// 上阵的伙伴不能作为护法
			return;
		}

		// 添加之前的战斗力
		int lastCombat = pObj.getPartner().getCombat();
		// DProp lastDp = UnitManager.inst().getDProp(pObj.getPartner());
		// 添加内存数据
		PartnerManager.inst().setServentByIndex(humanObj, pObj, index, servantId);
		PartnerObject servent = humanObj.partnerMap.get(servantId); 
		servent.getPartner().setIsServant(true);
		// 重新计算属性
		UnitManager.inst().initPartnerProps(servent);
		UnitManager.inst().propsChange(pObj, EntityUnitPropPlus.Servant);

		int nowCombat = pObj.getPartner().getCombat();
//		DProp curDp = UnitManager.inst().getDProp(pObj.getPartner());

		// 发送给客户端消息
		SCAddServant.Builder msg = SCAddServant.newBuilder();
		msg.setSuccess(true);
		msg.setPartnerId(partnerId);
		msg.setServantId(servantId);
		msg.setIndex(index);
		msg.setPower(nowCombat - lastCombat);
		// if(addProps!=null){
		// msg.setAddProp(addProps);
		// }
		humanObj.sendMsg(msg);
	}

	/**
	 * 卸掉该随从
	 */
	public boolean removeServent(HumanObject humanObj, long partnerId, long servantId, int index) {
		if (!PartnerManager.inst().isExistPartnerId(humanObj, partnerId)) {
			return false;// 没有改伙伴
		}
		// 检测该位置上是否是该随从
		PartnerObject pto = humanObj.partnerMap.get(partnerId);

		if (pto.getServantList().size() < index) {
			Log.game.info("错误的index");
			return false;
		}
		// if (pto.getServantList().get(index) == null ||
		// pto.getServantList().get(index) != servantId) {
		// // 该位置上不是这个随从
		// return false;
		// }
		// 移除该随从
		setServentByIndex(humanObj, pto, index, PartnerManager.HasNoServant);
		// 下发协议
		SCRemoveServant.Builder msg = SCRemoveServant.newBuilder();
		msg.setSuccess(true);
		msg.setPartnerId(partnerId);
		msg.setServantId(servantId);
		msg.setIndex(index);
		humanObj.sendMsg(msg);
		return true;
	}

	/**
	 * 卸掉该伙伴上的所有随从
	 */
	public void removeAllServent(HumanObject humanObj, PartnerObject pto) {
		List<Long> sList = pto.getServantList();
		int len = sList.size();
		for (int i = 0; i < len; i++) {
			removeServent(humanObj, pto.getPartnerId(), sList.get(i), i);
		}
	}

	/**
	 * 交换护法位置 把前面的护法给后面，并将前面的护法移除
	 * 
	 * @param oldPto
	 *            之前上阵的伙伴
	 * @param curPto
	 *            被交换的当前上阵伙伴
	 */
	public void swapServant(HumanObject humanObj, PartnerObject oldPto, PartnerObject curPto) {
		List<Long> ptoSid = oldPto.getServantList();

		curPto.setServantList(humanObj, ptoSid);
		// 遍历发送协议,通知客户端
		int len = curPto.getServantList().size();

		for (int i = 0; i < len; i++) {
			SCAddServant.Builder msg = SCAddServant.newBuilder();
			msg.setPartnerId(curPto.getPartnerId());
			msg.setServantId(curPto.getServantList().get(i));
			msg.setIndex(i);
			msg.setSuccess(true);
			msg.setPower(1);
			humanObj.sendMsg(msg);
		}

		// 移除前面的所有护法
		removeAllServent(humanObj, oldPto);
	}

	/**
	 * 获得伙伴当前突破养成的配置表
	 * @param partnerSn 伙伴sn
	 * @param adv 伙伴进阶等级
	 * @return
	 */
	public ConfPartnerConstitutions getConfPartnerConstitution(int partnerSn, int adv) {
		ConfPartnerRecruit confPR = ConfPartnerRecruit.get(partnerSn);
		if (confPR == null) {
			return null;
		}
		int planId = confPR.constitutionPlanId;
		int confPCSn = ConfigKeyFormula.getPartnerConsititutionsSn(planId, adv);
		ConfPartnerConstitutions conf = ConfPartnerConstitutions.get(confPCSn);
		return conf;
	}
	
	/**
	 * 获得伙伴当前突破养成的配置表
	 * @param partnerSn 伙伴sn
	 * @param star 伙伴进阶等级
	 * @return
	 */
	public ConfPartnerStarUp getConfPartnerStarUp(int partnerSn, int star) {
		int confStarSn = ConfigKeyFormula.getPartnerStarSn(partnerSn, star);
		ConfPartnerStarUp conf = ConfPartnerStarUp.get(confStarSn);
		return conf;
	}
	
	/**
	 * 获得伙伴法宝当前突破养成的配置表
	 * @param cimeliaSn 法宝sn
	 * @param adv 伙伴进阶等级
	 * @return
	 */
	public ConfCimeliaConstitutions getConfCimeliaConstitution(int cimeliaSn, int adv) {
		ConfCimelia confcimemlia = ConfCimelia.get(cimeliaSn);
		if (confcimemlia == null) {
			return null;
		}
		int planId = confcimemlia.planId;
		int sn = ConfigKeyFormula.getCimeliaConsititutionsSn(planId, adv);
		ConfCimeliaConstitutions conf = ConfCimeliaConstitutions.get(sn);
		return conf;
	}
	
	/**
	 * 判断该伙伴是否是其他伙伴的护法 TODO 这里要优化成伙伴身上的标识
	 */
	public boolean isServantLineUp(HumanObject humanObj, long servantId) {
		HumanExtInfo exinfo = humanObj.getHumanExtInfo();
		Map<Long, PartnerObject> partnerMap = humanObj.partnerMap;
		List<Long> idLists = Utils.strToLongList(exinfo.getPartnerLineup());

		List<Long> servantIds = new ArrayList<>();
		for (Long id : idLists) {
			PartnerObject po = partnerMap.get(id);
			if (po == null) {
				continue;
			}
			servantIds.clear();
			servantIds = po.getServantList();
			if (servantIds.contains(servantId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检测伙伴数量上限,是否可以招募伙伴
	 * 
	 * @param humanObj
	 * @param addNum
	 *            要添加的数量
	 * @return
	 */
	public boolean canRecruit(HumanObject humanObj, int addNum) {
		boolean can = true;
		if (humanObj.partnerMap.size() + addNum > ParamManager.partnerMaxNum) {
			can = false;
		}
		//Log.partner.info("伙伴数量超出限制，不可召唤 humanId = {} addNum ={}", humanObj.getHumanId(), addNum);
		return can;
	}

	/**
	 * 图鉴中是否拥有该伙伴sn
	 */
	public boolean partnerActivityHand(HumanObject humanObj, int partnerSn) {
		String strSnList = humanObj.extInfo.getActivityHand();
		List<Integer> snList = Utils.strToIntList(strSnList);
		if (snList.contains(partnerSn)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否可以打开护法位置
	 */
	@Listener(value = { EventKey.HumanLvUp, EventKey.InstAnyPass })
	public void openServantLock(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_VipChange humanObj is null");
			return;
		}
		// 获取现在的解锁个数
		int servantNum = humanObj.getHumanExtInfo().getServantLock();

		int nextSn = servantNum + 1;
		ConfServantLock conf = ConfServantLock.get(nextSn);
		if (conf == null) {
			Log.table.error("servantLock error sn = ", nextSn);
			return;
		}
		if (servantNum == 12) {
			Log.table.error("servantLock is max ");
			return;
		}
		int humanLv = humanObj.getHuman().getLevel();
		boolean passInst = InstanceManager.inst().isInstPass(humanObj, conf.passInst);
		if (humanLv >= conf.lv || passInst) {
			// 可以正常解锁
			setServantLockNum(humanObj, nextSn);
			// 发送协议
			sendServantNum(humanObj);
		}

	}

	/**
	 * 设置护法个数
	 */
	public void setServantLockNum(HumanObject humanObj, int num) {
		// 解锁七日事件
		Event.fire(EventKey.UpdateActivitySeven, "humanObj", humanObj, "progress", num, "type",ActivitySevenTypeKey.Type_47);

		humanObj.getHumanExtInfo().setServantLock(num);
	}

	/**
	 * 是否可以添加新护法
	 */
	public boolean servantLock(HumanObject humanObj) {
		// 现有护法数量
		int totle = 0;
		for (PartnerObject poj : humanObj.partnerMap.values()) {
			totle += poj.getServantList().size();
		}
		int sn = totle + 1;
		ConfServantLock conf = ConfServantLock.get(sn);
		if (conf == null) {
			Log.table.error("servantLock error sn = ", sn);
			return false;
		}
		int humanLv = humanObj.getHuman().getLevel();
		int viplv = humanObj.getHuman().getVipLevel();
		boolean passInst = InstanceManager.inst().isInstPass(humanObj, conf.passInst);
		if (humanLv >= conf.lv && viplv > conf.viplv && passInst) {
			return true;
		}

		return false;

	}

	/**
	 * 
	 * @param humanObj
	 */
	public void _msg_VIPServantClear(HumanObject humanObj) {
		// 获取伙伴数量
		int num = humanObj.getHumanExtInfo().getServantLock();
		// 校验VIp等级
		int nextSn = num + 1;
		int maxSn = ConfServantLock.findAll().size() - 1;
		if (nextSn > maxSn) {
			// 当前已经是最大能解锁的护法数量

			return;
		}
		ConfServantLock conf = ConfServantLock.get(nextSn);
		if (conf == null) {
			Log.table.error("servantLock error sn = ", nextSn);
			return;
		}
		int viplv = humanObj.getHuman().getVipLevel();
		SCServantClear.Builder msg = SCServantClear.newBuilder();

		if (viplv >= conf.viplv) {
			boolean cost = RewardHelper.checkAndConsume(humanObj, conf.itemSn, conf.itemNum, LogSysModType.ServantClean);
			if (cost) {
				setServantLockNum(humanObj, nextSn);
				msg.setSuccess(true);
				msg.setNum(nextSn);
			} else {
				msg.setSuccess(false);
				msg.setNum(num);
			}
		} else {
			// VIP等级不足
		}
		humanObj.sendMsg(msg);
	}

	/**
	 * 初始法宝Cimelia
	 * @param partnerObj
	 */
	private void initCimelia(PartnerObject partnerObj) {
		Cimelia cimelia = createCimelia(partnerObj.dataPers);
		if (cimelia == null) {
			return;
		}
		// 加入缓存
		partnerObj.setCimeLia(cimelia);
	}
	
	/**
	 * 创建法宝
	 */
	private Cimelia createCimelia(UnitDataPersistance dataPers) {
		int partnerSn = dataPers.unit.getSn();
		long partnerId = dataPers.unit.getId();
		
		Cimelia cimelia = new Cimelia();
		ConfPartnerRecruit confPartnerReruit = ConfPartnerRecruit.get(partnerSn);
		if (confPartnerReruit == null) {
			Log.table.error("== initCimelia ConfPartnerRecruit error,can't find sn ={} ", partnerSn);
			return null;
		}
		if (confPartnerReruit.cimelia == 0) {
		    return null;
        }
		ConfCimelia confCimelia = ConfCimelia.get(confPartnerReruit.cimelia);
		if (confCimelia == null) {
			Log.table.error("== initCimelia ConfCimelia error,can't find sn ={} ", confPartnerReruit.cimelia);
			return null;
		}

		cimelia.setId(partnerId);
		cimelia.setPartnerId(partnerId);
		cimelia.setAdvLevel(0);
		cimelia.setStar(0);
		cimelia.setAdvLevel(0);
		cimelia.setLevel(1);// 伙伴获得时，自带法宝，默认为1级
		cimelia.setQuality(confCimelia.quality);
		cimelia.setSn(confCimelia.sn);
		// 提交数据库
		cimelia.persist();
		
		return cimelia;
	}
	

	/**
	 * 法宝升级
	 * 
	 * @param humanObj
	 * @param partnerId
	 * @param itemList
	 */
	public void _msg_CSCimeliaAddLevel(HumanObject humanObj, long partnerId, List<DItem> itemList) {
		int hasexp = 0;
		PartnerObject pto = humanObj.partnerMap.get(partnerId);
		if (pto == null) {
			humanObj.sendSysMsg(190511);
			Log.human.info("===partnerLevelup 不存在该伙伴");
			return;
		}
		// 分解物品
		for (DItem dItem : itemList) {
			int itemSn = dItem.getItemSn();
			int itemNum = dItem.getNum();
			
			if(!RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.PartnerAddLevel)) {
				continue;
			}
			
			ConfItem conf = ConfItem.get(itemSn);
			if (conf == null) {
				Log.table.error("分解经验相关物品错误，没有对应sn ={}", itemSn);
				continue;
			}
			int exp = Utils.intValue(conf.param[1]) * itemNum;
			hasexp += exp;
		}

		// 加经验
		PartnerPlusManager.inst().cimeliaExpAdd(humanObj, pto, hasexp, LogSysModType.PartnerAddLevel);
	}

	/**
	 * 新伙伴+道具分解
	 * 
	 * @param humanObj
	 * @param partnerIdList
	 * @param ditemList
	 */
	public void NewDecomposeAll(HumanObject humanObj, List<Long> partnerIdList, List<DItem> ditemList,
			boolean isDescompose) {
		SCNewDecomposeAll.Builder msg = SCNewDecomposeAll.newBuilder();
		List<DItem> itemdes = DecomposeItemMsg(humanObj, ditemList);
		List<ProduceVo> partnerItemdes = getDecomposePartnerList(humanObj, partnerIdList, isDescompose);

		// 合并物品
		Map<Integer, Integer> maps = new HashMap<>();
		for (ProduceVo d : partnerItemdes) {
			int sn = d.sn;
			int num = d.num;
			if (maps.get(sn) != null) {
				maps.put(sn, num + maps.get(sn));
			} else {
				maps.put(sn, num);
			}
		}

		for (DItem ditem : itemdes) {
			int sn = ditem.getItemSn();
			int num = ditem.getNum();
			if (maps.get(sn) != null) {
				maps.put(sn, num + maps.get(sn));
			} else {
				maps.put(sn, num);
			}
		}
		List<DItem> result = new ArrayList<>();
		for (Entry<Integer, Integer> entry : maps.entrySet()) {
			DItem.Builder d = DItem.newBuilder();
			d.setItemSn(entry.getKey());
			d.setNum(entry.getValue());
			result.add(d.build());
		}

		// 如果是真的分解，奖励道具
		if (isDescompose) {
			// -消除伙伴，
			for (Long partnerId : partnerIdList) {
				removePartner(humanObj, partnerId);
			}
			msg.addAllPartnerId(partnerIdList);
			int[] itemSn = new int[ditemList.size()];
			int[] itemNum = new int[ditemList.size()];
			// 消除道具
			for (int i = 0; i < itemSn.length; i++) {
				DItem ditem = ditemList.get(i);
				itemSn[i] = ditem.getItemSn();
				itemNum[i] = ditem.getNum();
			}
			RewardHelper.checkAndConsume(humanObj, itemSn, itemNum, LogSysModType.PartnerResolve);
			// 发放奖励
			
			List<ProduceVo> plist = new ArrayList<>();
			for (Entry<Integer,Integer> entry : maps.entrySet()) {
				plist.add(new ProduceVo(entry.getKey(),entry.getValue()));
			}
			RewardHelper.reward(humanObj,plist, LogSysModType.PartnerResolve);
		}

		msg.addAllItemList(result);
		msg.setIsDescompose(isDescompose);
		humanObj.sendMsg(msg);
	}

	/**
	 * {0}个法宝升至{1}级
	 */

	public int howNumByLv(HumanObject humanObj, int lv) {
		int num = 0;
		for (PartnerObject poj : humanObj.partnerMap.values()) {
			Cimelia c = poj.getCimeLia();
			if (c != null && c.getLevel() >= lv) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 0}个法宝突破至{1}阶
	 */

	public int howNumByAdaLv(HumanObject humanObj, int lv) {
		int num = 0;
		for (PartnerObject poj : humanObj.partnerMap.values()) {
			Cimelia c = poj.getCimeLia();
			if (c == null) {
				continue;
			}
			if (c.getAdvLevel() >= lv) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 0}个法宝升至{1}星 -
	 */
	public int howNumByStart(HumanObject humanObj, int lv) {
		int num = 0;
		for (PartnerObject poj : humanObj.partnerMap.values()) {
			Cimelia c = poj.getCimeLia();
			if (c.getStar() >= lv) {
				num++;
			}
		}
		return num;
	}
}
