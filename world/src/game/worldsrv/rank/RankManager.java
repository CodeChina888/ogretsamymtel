package game.worldsrv.rank;

import game.msg.Define.DCompeteRank;
import game.msg.Define.DFairylandRank;
import game.msg.Define.DPVERank;
import game.msg.Define.DRank;
import game.msg.Define.DRankGuild;
import game.msg.Define.DRankWorship;
import game.msg.Define.ECastellanType;
import game.msg.Define.ERankType;
import game.msg.Define.EWorshipType;
import game.msg.MsgRank.SCActCombatRank;
import game.msg.MsgRank.SCCompeteRankNew;
import game.msg.MsgRank.SCFairylandRank;
import game.msg.MsgRank.SCGetPVETowerRank;
import game.msg.MsgRank.SCGuildRank;
import game.msg.MsgRank.SCInstanceRank;
import game.msg.MsgRank.SCLevelRank;
import game.msg.MsgRank.SCNodata;
import game.msg.MsgRank.SCSumCombatRank;
import game.msg.MsgRank.SCWorship;
import game.worldsrv.character.HumanObject;
import game.worldsrv.compete.CompeteHumanObj;
import game.worldsrv.compete.CompeteServiceProxy;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfMainCityShow;
import game.worldsrv.entity.Castellan;
import game.worldsrv.entity.CompeteHuman;
import game.worldsrv.entity.Guild;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.RankFairyland;
import game.worldsrv.entity.RankGuild;
import game.worldsrv.entity.RankInstance;
import game.worldsrv.entity.RankLevel;
import game.worldsrv.entity.RankSumCombat;
import game.worldsrv.entity.RankTower;
import game.worldsrv.entity.RankVip;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.RankIntType;
import game.worldsrv.enumType.RankType;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.io.IOException;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

public class RankManager extends ManagerBase {

	public static RankManager inst() {
		return inst(RankManager.class);
	}
	

		
	/**
	 * 角色登陆，检查玩家的排行数据是否被误删
	 * @param param
	 */
	@Listener(EventKey.HumanLoginFinish)
	public void _listener_HumanLoginFinish(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanLoginFinish humanObj is null");
			return;
		}
		addNewRank(humanObj, RankType.RankSumCombat, false);
		addNewRank(humanObj, RankType.RankLevel, false);
		addNewRank(humanObj, RankType.RankVip, false);
		addNewRank(humanObj, RankType.RankTower, false);
		addNewRank(humanObj, RankType.RankInstance, false);
		addNewRank(humanObj, RankType.RankFairyland, false);
	} 

	/**
	 * 角色改名
	 * @param param
	 */
	@Listener(EventKey.HumanNameChange)
	public void _listener_HumanNameChange(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanNameChange humanObj is null");
			return;
		}
		updateRank(humanObj);// 更新数据到排行榜
	}

	/**
	 * 角色战力改变 （因目前战力为人物和武将的总战力所以此处暂
	 * @param param
	 */
	@Listener(EventKey.HumanCombatChange)
	public void _listener_HumanCombatChange(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanCombatChange unitObj==null");
			return;
		}
		// 人物需要进行战斗力排行
		addNewRank(humanObj, RankType.RankSumCombat, true);
	}

	/**
	 * 角色等级改变
	 * @param param
	 */
	@Listener(EventKey.HumanLvUp)
	public void _listener_HumanLvUp(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanLvUp humanObj is null");
			return;
		}
		int lvOld = Utils.getParamValue(param, "lvOld", 0);
		int lvNew = Utils.getParamValue(param, "lvNew", 0);
		if (lvOld > 0 && lvNew > 0 && lvOld > lvNew) {// 用GM命令降级了
			updateRank(humanObj);// 更新数据到排行榜
		} else {
			addNewRank(humanObj, RankType.RankLevel, true);// 加入数据到排行榜
		}
	}

	/**
	 * VIP改变
	 * @param param
	 */
	@Listener(EventKey.VipLvChange)
	public void _listener_VipChange(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_VipChange humanObj is null");
			return;
		}
		addNewRank(humanObj, RankType.RankVip, true);// 加入数据到排行榜
	}
	
	/**
	 * 任意副本(普通本或者精英本)通关
	 */
	@Listener(EventKey.InstAnyPass)
	public void _listener_InstAnyPass(Param param){
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_VipChange humanObj is null");
			return;
		}
		addNewRank(humanObj, RankType.RankInstance, true);// 加入数据到排行榜
	}
	
	
	
	/**
	 * 加入数据到排行榜，若已存在默认是需要更新数据
	 */
	public void addNewRank(HumanObject humanObj, RankType rankType, boolean isNeedUpdate) {
		Human human = humanObj.getHuman();
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		switch (rankType) {
			case RankSumCombat:{//总战力排行
				if(human.getSumCombat() >= ParamManager.rankTopRecordFilter[RankIntType.SumCombat.value()])
					proxy.addNew(new RankData(humanObj),RankType.RankSumCombat);
			}
				break;
			case RankLevel : {
				if (human.getLevel() >= ParamManager.rankTopRecordFilter[RankIntType.Level.value()])
					proxy.addNew(new RankData(humanObj), RankType.RankLevel);
			}
				break;
			case RankTower : {
				// FIXME 爬塔数据判断条件
				if (human.getTowerToken() >= ParamManager.rankTopRecordFilter[RankIntType.Tower.value()])
					proxy.addNew(new RankData(humanObj), RankType.RankTower);
			}
				break;
			case RankVip : {
				if (human.getVipLevel() >= 1)
					proxy.addNew(new RankData(humanObj), RankType.RankVip);
			}
				break;
			case RankInstance:{ //副本
				int start = InstanceManager.inst().getAllStarCount(humanObj);
				if (start >= ParamManager.rankTopRecordFilter[RankIntType.Instance.value()]){
					RankData instanceData =  new RankData(humanObj);
					if(start>0){
						instanceData.stars = start;
						proxy.addNew(instanceData, RankType.RankInstance);
					}
				}
			}
				break;
			case RankFairyland:{ //洞天福地
				// FIXME 洞天福地数据判断条件
				if (humanObj.getHumanExtInfo().getLootMapMultipleScore() >= ParamManager.rankTopRecordFilter[RankIntType.Fairyland.value()])
					proxy.addNew(new RankData(humanObj), RankType.RankFairyland);
			}
				break;
				
			default :
				break;
		}
	}
	
	
	/**
	 * 更新数据到排行榜
	 * @param humanObj
	 */
	public void updateRank(HumanObject humanObj) {
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.updateAllRankData(new RankData(humanObj));
	}

	/**
	 * 创建新的总战力排行数据
	 * @param data
	 * @return
	 */
	public RankSumCombat newRankSumCombat(RankData data){
		RankSumCombat rank = new RankSumCombat();
		rank.setId(data.humanId);
		rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		// 角色特殊字段
		rank.setHumanId(data.humanId);
		rank.setName(data.name);
		rank.setSumCombat(data.sumCombat);
		rank.setHeadSn(data.modelSn);
		rank.setLv(data.level);
		return rank;
	}
	
	/**
	 * 更新新的总战力排行数据
	 */
	public void updateRankSumCombat(RankData data, RankSumCombat rank) {
		if (data == null || rank == null)
			return;
		// 角色特殊字段
		if (data.humanId > 0)
			rank.setHumanId(data.humanId);
		if (!data.name.equals(""))
			rank.setName(data.name);
		if (data.sumCombat >= 0) {
			rank.setSumCombat(data.sumCombat);
			rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
			rank.setLv(data.level);//设置等级
		}
		rank.setModelSn(data.modelSn);
	}
	/**
	 * 创建新的等级排行数据
	 */
	public RankLevel newRankLevel(RankData data) {
		RankLevel rank = new RankLevel();
		rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		// 角色特殊字段
		rank.setId(data.humanId);
		rank.setHumanId(data.humanId);
		rank.setName(data.name);
		rank.setLevel(data.level);
		rank.setVipLevel(data.vipLevel);
		rank.setCombat(data.combat);
		rank.setModelSn(data.modelSn);
		rank.setEquipWeaponSn(data.equipWeaponSn);
		rank.setEquipClothesSn(data.equipClothesSn);
		rank.setFashionClothesSn(data.fashionClothesSn);
		rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		rank.setAccountId(data.accountId);
		return rank;
	}
	/**
	 * 更新新的等级排行数据
	 */
	public void updateRankLevel(RankData data, RankLevel rank) {
		if (data == null || rank == null)
			return;
		// 角色特殊字段
		if (data.humanId > 0)
			rank.setHumanId(data.humanId);
		if (!data.accountId.equals(""))
			rank.setAccountId(data.accountId);
		if (!data.name.equals(""))
			rank.setName(data.name);
		if (data.level >= 0) {
			rank.setLevel(data.level);
			rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		}
		if (data.vipLevel >= 0)
			rank.setVipLevel(data.vipLevel);
		if (data.combat >= 0)
			rank.setCombat(data.combat);
		if (data.modelSn >= 0)
			rank.setModelSn(data.modelSn);
		if (data.equipWeaponSn >= 0)
			rank.setEquipWeaponSn(data.equipWeaponSn);
		if (data.equipClothesSn >= 0)
			rank.setEquipClothesSn(data.equipClothesSn);
		if (data.fashionClothesSn >= 0)
			rank.setFashionClothesSn(data.fashionClothesSn);
		if (data.isFashionShow >= 0)
			rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		
		rank.setModelSn(data.modelSn);
	}

	/**
	 * 创建新的VIP排行数据
	 */
	public RankVip newRankVip(RankData data) {
		RankVip rank = new RankVip();
		rank.setId(data.humanId);
		rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		// 角色特殊字段
		rank.setHumanId(data.humanId);
		rank.setName(data.name);
		rank.setLevel(data.level);
		rank.setVipLevel(data.vipLevel);
		rank.setCombat(data.combat);
		rank.setModelSn(data.modelSn);
		rank.setEquipWeaponSn(data.equipWeaponSn);
		rank.setEquipClothesSn(data.equipClothesSn);
		rank.setFashionClothesSn(data.fashionClothesSn);
		rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		rank.setModelSn(data.modelSn);
		return rank;
	}
	/**
	 * 更新新的VIP排行数据
	 */
	public void updateRankVip(RankData data, RankVip rank) {
		if (data == null || rank == null)
			return;
		// 角色特殊字段
		if (data.humanId > 0)
			rank.setHumanId(data.humanId);
		if (!data.name.equals(""))
			rank.setName(data.name);
		if (data.level >= 0)
			rank.setLevel(data.level);
		if (data.vipLevel >= 0) {
			rank.setVipLevel(data.vipLevel);
			rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		}
		if (data.combat >= 0)
			rank.setCombat(data.combat);
		if (data.modelSn >= 0)
			rank.setModelSn(data.modelSn);
		if (data.equipWeaponSn >= 0)
			rank.setEquipWeaponSn(data.equipWeaponSn);
		if (data.equipClothesSn >= 0)
			rank.setEquipClothesSn(data.equipClothesSn);
		if (data.fashionClothesSn >= 0)
			rank.setFashionClothesSn(data.fashionClothesSn);
		if (data.isFashionShow >= 0)
			rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		rank.setModelSn(data.modelSn);
	}

	/**
	 * 创建新的爬塔排行数据
	 */
	public RankTower newRankTower(RankData data) {
		RankTower rank = new RankTower();
		rank.setId(data.humanId);
		rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		// 角色特殊字段
		rank.setHumanId(data.humanId);
		rank.setName(data.name);
		rank.setLevel(data.level);
		rank.setVipLevel(data.vipLevel);
		rank.setCombat(data.combat);
		rank.setModelSn(data.modelSn);
		rank.setEquipWeaponSn(data.equipWeaponSn);
		rank.setEquipClothesSn(data.equipClothesSn);
		rank.setFashionClothesSn(data.fashionClothesSn);
		rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		rank.setIcon(data.icon); // 头像
		// 爬塔特殊字段
		rank.setMaxFloor(data.maxFloor);
		// 爬塔耗时
		rank.setCostTime(data.costTime); 
		// 难度
		rank.setDIFFICULTY(data.difficultly);
		// 旧版：综合评分(排序依据,层数*10,000,000+难度*1000,000+耗时秒数)
//		rank.setGrade(data.maxFloor * 10000000+data.difficultly*1000000-data.costTime);
		// 新版：综合评分（积分）
		rank.setGrade(data.towerScore);
		rank.setModelSn(data.modelSn);
		return rank;
	}
	/**
	 * 更新新的爬塔排行数据
	 */
	public void updateRankTower(RankData data, RankTower rank) {
		if (data == null || rank == null)
			return;
		// 角色特殊字段
		if (data.humanId > 0)
			rank.setHumanId(data.humanId);
		if (!data.name.equals(""))
			rank.setName(data.name);
		if (data.level >= 0)
			rank.setLevel(data.level);
		if (data.vipLevel >= 0)
			rank.setVipLevel(data.vipLevel);
		if (data.combat >= 0)
			rank.setCombat(data.combat);
		if (data.modelSn >= 0)
			rank.setModelSn(data.modelSn);
		if (data.equipWeaponSn >= 0)
			rank.setEquipWeaponSn(data.equipWeaponSn);
		if (data.equipClothesSn >= 0)
			rank.setEquipClothesSn(data.equipClothesSn);
		if (data.fashionClothesSn >= 0)
			rank.setFashionClothesSn(data.fashionClothesSn);
		if (data.isFashionShow >= 0)
			rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		if (data.icon >= 0) {
			rank.setIcon(data.icon);
		}
		// 爬塔特殊字段
		if (data.maxFloor >= 0) {
			rank.setMaxFloor(data.maxFloor);
			rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		}
		if (data.costTime >= 0) {
			rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
			rank.setCostTime(data.costTime);
		}
		rank.setModelSn(data.modelSn);
	}

	
	/**
	 * 创建新的副本排行数据
	 */
	public RankInstance newRankInstance(RankData data) {
		RankInstance rank = new RankInstance();
		rank.setId(data.humanId);
		rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		rank.setHumanId(data.humanId);
		rank.setName(data.name);
		rank.setLevel(data.level);
		rank.setVipLevel(data.vipLevel);
		rank.setCombat(data.combat);
		rank.setModelSn(data.modelSn);
		rank.setEquipWeaponSn(data.equipWeaponSn);
		rank.setEquipClothesSn(data.equipClothesSn);
		rank.setFashionClothesSn(data.fashionClothesSn);
		rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		rank.setStars(data.stars);
		return rank;
	}
	/**
	 * 更新新的副本排行数据
	 */
	public void updateRankInstance(RankData data, RankInstance rank) {
		if (data == null || rank == null)
			return;
		// 角色特殊字段
		if (data.humanId > 0)
			rank.setHumanId(data.humanId);
		if (!data.name.equals(""))
			rank.setName(data.name);
		if (data.level >= 0)
			rank.setLevel(data.level);
		if (data.vipLevel >= 0)
			rank.setVipLevel(data.vipLevel);
		if (data.combat >= 0)
			rank.setCombat(data.combat);
		if (data.modelSn >= 0)
			rank.setModelSn(data.modelSn);
		if (data.equipWeaponSn >= 0)
			rank.setEquipWeaponSn(data.equipWeaponSn);
		if (data.equipClothesSn >= 0)
			rank.setEquipClothesSn(data.equipClothesSn);
		if (data.fashionClothesSn >= 0)
			rank.setFashionClothesSn(data.fashionClothesSn);
		if (data.isFashionShow >= 0)
			rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		if (data.stars >= 0) {
			rank.setStars(data.stars);
			rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		}
	}
	/**
	 * 创建新的洞天福地排行数据
	 */
	public RankFairyland newRankFairyland(RankData data) {
		RankFairyland rank = new RankFairyland();
		rank.setId(data.humanId);
		rank.setRankTime(Port.getTime());
		rank.setHumanId(data.humanId);
		rank.setName(data.name);
		rank.setLevel(data.level);
		rank.setVipLevel(data.vipLevel);
		rank.setCombat(data.combat);
		rank.setModelSn(data.modelSn);
		rank.setEquipWeaponSn(data.equipWeaponSn);
		rank.setEquipClothesSn(data.equipClothesSn);
		rank.setFashionClothesSn(data.fashionClothesSn);
		rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		rank.setIcon(data.icon);
		rank.setLootMapScore(data.lootMapScore);
		return rank;
	}
	/**
	 * 更新新的洞天福地排行数据
	 */
	public void updateRankFairyland(RankData data, RankFairyland rank) {
		if (data == null || rank == null)
			return;
		// 角色特殊字段
		if (data.humanId > 0)
			rank.setHumanId(data.humanId);
		if (!data.name.equals(""))
			rank.setName(data.name);
		if (data.level >= 0)
			rank.setLevel(data.level);
		if (data.vipLevel >= 0)
			rank.setVipLevel(data.vipLevel);
		if (data.combat >= 0)
			rank.setCombat(data.combat);
		if (data.modelSn >= 0)
			rank.setModelSn(data.modelSn);
		if (data.equipWeaponSn >= 0)
			rank.setEquipWeaponSn(data.equipWeaponSn);
		if (data.equipClothesSn >= 0)
			rank.setEquipClothesSn(data.equipClothesSn);
		if (data.fashionClothesSn >= 0)
			rank.setFashionClothesSn(data.fashionClothesSn);
		if (data.isFashionShow >= 0)
			rank.setIsFashionShow(data.isFashionShow == 1 ? true : false);
		if (data.icon >= 0)
			rank.setIcon(data.icon);
		if (data.lootMapScore >= 0) {
			rank.setLootMapScore(data.lootMapScore);
			rank.setRankTime(Port.getTime());// 记录最近一次更新排行值的时间
		}
	}

	/**
	 * 创建数据-等级排行
	 */
	public DRank createMsgDRankLevel(RankLevel rank) {
		DRank.Builder msg = DRank.newBuilder();
		msg.setIndex(rank.getRank());// 排名
		// 共用字段
		msg.setHumanId(rank.getHumanId());
		msg.setName(rank.getName());
		msg.setLevel(rank.getLevel());
		// 角色特殊字段
		// msg.setVipLevel(rank.getVipLevel());
		msg.setCombat(rank.getCombat());
		msg.setModelSn(rank.getModelSn());
		msg.setEquipClothesSn(rank.getEquipClothesSn());
		msg.setEquipWeaponSn(rank.getEquipWeaponSn());
		msg.setFashionClothesSn(rank.getFashionClothesSn());
		msg.setIsFashionShow(rank.isIsFashionShow());
		return msg.build();
	}

	/**
	 * 创建数据-VIP排行
	 */
	public DRank createMsgDRankVip(RankVip rank) {
		DRank.Builder msg = DRank.newBuilder();
		msg.setIndex(rank.getRank());// 排名
		// 共用字段
		msg.setHumanId(rank.getHumanId());
		msg.setName(rank.getName());
		msg.setLevel(rank.getLevel());
		// 角色特殊字段
		// msg.setVipLevel(rank.getVipLevel());
		msg.setCombat(rank.getCombat());
		msg.setModelSn(rank.getModelSn());
		msg.setEquipClothesSn(rank.getEquipClothesSn());
		msg.setEquipWeaponSn(rank.getEquipWeaponSn());
		msg.setFashionClothesSn(rank.getFashionClothesSn());
		msg.setIsFashionShow(rank.isIsFashionShow());
		return msg.build();
	}
	
	/**
	 * 创建数据-爬塔排行 给协议赋值
	 */
	public DPVERank createMsgDRankTower(RankTower rank) {
		DPVERank.Builder msg = DPVERank.newBuilder();
		msg.setRank(rank.getRank()); //排名
		msg.setKey(rank.getGrade()); //当前积分
		msg.setHumanName(rank.getName()); //名字
		msg.setModelSn(rank.getModelSn()); //模型sn
		msg.setHumanId(rank.getHumanId()); //id
		msg.setLvl(rank.getLevel()); //等级
		msg.setCombat(rank.getCombat()); //战力
		msg.setIcon(rank.getIcon()); //头像
		return msg.build();
	}
	
	/**
	 * 创建数据-公会排行
	 */
	public DRankGuild createMsgDRankGuild(RankGuild rank) {
		DRankGuild.Builder msg = DRankGuild.newBuilder();
		msg.setIndex(rank.getRank());// 排名
		// 共用字段
		msg.setGuildId(rank.getGuildId());
		msg.setName(rank.getGuildName());
		msg.setLevel(rank.getGuildLevel());
		// 公会特殊字段
		msg.setChairmanName(rank.getGuildLeaderName());
		msg.setChairmanId(rank.getGuildLeaderId());
		// 角色特殊字段
		// msg.setVipLevel(rank.getVipLevel());
		// msg.setCombat(rank.getCombat());
		msg.setModelSn(rank.getGuildIcon());
		msg.setEquipClothesSn(rank.getEquipClothesSn());
		msg.setEquipWeaponSn(rank.getEquipWeaponSn());
		msg.setFashionClothesSn(rank.getFashionClothesSn());
		msg.setIsFashionShow(rank.isIsFashionShow());
		return msg.build();
	}
	
	/**
	 * 创建数据-副本排行
	 */
	public DRank createMsgDRankInstance(RankInstance rank) {
		DRank.Builder msg = DRank.newBuilder();
		msg.setIndex(rank.getRank());// 排名
		// 共用字段
		msg.setHumanId(rank.getHumanId());
		msg.setName(rank.getName());
		msg.setLevel(rank.getLevel());
		msg.setCombat(rank.getCombat());
		msg.setModelSn(rank.getModelSn());
		msg.setEquipClothesSn(rank.getEquipClothesSn());
		msg.setEquipWeaponSn(rank.getEquipWeaponSn());
		msg.setFashionClothesSn(rank.getFashionClothesSn());
		msg.setIsFashionShow(rank.isIsFashionShow());
		msg.setStars(rank.getStars());
		return msg.build();
	}
	/**
	 * 创建数据-洞天福地排行 给协议赋值
	 */
	public DFairylandRank createMsgDRankFairyland(RankFairyland rank) {
		DFairylandRank.Builder msg = DFairylandRank.newBuilder();
		msg.setRank(rank.getRank()); //排名
		msg.setKey(Utils.intValue((rank.getLootMapScore()))); //积分
		msg.setHumanName(rank.getName()); //名字
		msg.setModelSn(rank.getModelSn()); //模型sn
		msg.setHumanId(rank.getHumanId()); //id
		msg.setLvl(rank.getLevel()); //等级
		msg.setCombat(rank.getCombat()); //战力
		msg.setIcon(rank.getIcon()); //头像
		return msg.build();
	}
	/**
	 * 创建数据-总战力排行榜信息
	 * @param rank
	 * @return
	 */
	public DRank createMsgDSumCombatRank(RankSumCombat rank){
		DRank.Builder msg = DRank.newBuilder();
		msg.setIndex(rank.getRank());
		msg.setHumanId(rank.getHumanId());
		msg.setName(rank.getName());
		msg.setCombat(rank.getSumCombat());
		msg.setModelSn(rank.getHeadSn());
		msg.setLevel(rank.getLv());
		return msg.build();
	}

	
	/**
	 * 请求定时等级排行
	 */
	public void _msg_CSLevelRank(HumanObject humanObj) {
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.getRank(ERankType.RankTypeLevel);
		proxy.listenResult(this::_result_levelRank, "humanObj", humanObj);
	}
	private void _result_levelRank(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		List<RankLevel> rankLevel = results.get();
		int selfRank = -1;// 自己的排名：-1即未上榜
		int selfLevel = 0;// 自己的等级

		if (humanObj == null || rankLevel == null || rankLevel.isEmpty()) {
			Log.game.error("===_result_rankLevel humanObj={}, rankList={}", humanObj, rankLevel);
			return;
		}
		for (RankLevel rank : rankLevel) {
			if (rank.getHumanId() == humanObj.getHuman().getId()) {
				selfRank = rank.getRank();
				selfLevel = rank.getLevel();
				break;
			}
		}
		if (selfLevel == 0) {
			selfLevel = humanObj.getHuman().getLevel();
		}

		SCLevelRank.Builder msg = SCLevelRank.newBuilder();
		for (RankLevel rank : rankLevel) {
			msg.addRanks(createMsgDRankLevel(rank));
		}
		msg.setSelfRank(selfRank);// 自己的排名
		msg.setSelfLevel(selfLevel);
		// 返回是否膜拜过
		int status = getWorshipStatus(humanObj, RankIntType.Level.value());
		msg.setStatus(status);
		humanObj.sendMsg(msg);
	}
	/**
	 * 请求工会排行榜
	 */
	public void _msg_CSGetGuild(HumanObject humanObj) {
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.getRank(ERankType.RankTypeGuild);
		proxy.listenResult(this::_result_GuildRank, "humanObj", humanObj);
	}
	private void _result_GuildRank(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		List<RankGuild> rankLevel = results.get();
		int selfRank = -1;// 自己的仙盟排名：-1即未上榜
		int selfLevel = 0;// 自己的仙盟等级

		if (humanObj == null || rankLevel == null || rankLevel.isEmpty()) {
			Log.game.error("===_result_rankLevel humanObj={}, rankList={}", humanObj, rankLevel);
			return;
		}
		for (RankGuild rank : rankLevel) {
			if (rank.getGuildId() == humanObj.getHuman().getGuildId()) {
				selfRank = rank.getRank();
				selfLevel = rank.getGuildLevel();
				break;
			}
		}
		if (selfLevel == 0) {
			selfLevel = humanObj.getHuman().getGuildLevel();
		}

		SCGuildRank.Builder msg = SCGuildRank.newBuilder();
		for (RankGuild rank : rankLevel) {
			msg.addRanks(createMsgDRankGuild(rank));
		}
		msg.setSelfRank(selfRank);// 自己的排名
		// 返回是否膜拜过
		int status = getWorshipStatus(humanObj, RankIntType.Level.value());
		msg.setStatus(status);
		msg.setSelfLevel(selfLevel);
		humanObj.sendMsg(msg);
	}
	
	
	/**
	 * 请求竞技场排行榜
	 */
	public void _msg_CSGetArenaDuke(HumanObject humanObj){
		CompeteServiceProxy prx = CompeteServiceProxy.newInstance();
		prx.getCompeteRank();
		prx.listenResult(this::_msg_CSGetArenaDuke, "humanObj", humanObj);
	}
	
	private void _msg_CSGetArenaDuke(Param results, Param context){
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		Map<Integer, CompeteHumanObj> map = results.get("map");
		SCCompeteRankNew.Builder msg = SCCompeteRankNew.newBuilder();
		int selfRank = -1;
		if(map.size()<=0){
			SCNodata.Builder msgs = SCNodata.newBuilder();
			humanObj.sendMsg(msgs);
		}
		
		for (Entry<Integer, CompeteHumanObj> entry:map.entrySet()) {
			CompeteHumanObj c = entry.getValue();
			CompeteHuman cp = c.cpHuman;
			int rank =entry.getKey();
			DCompeteRank.Builder dc = DCompeteRank.newBuilder();
			dc.setCombat(c.getSumCombat());
			dc.setHumanId(cp.getId());
			dc.setName(cp.getName());
			dc.setModelSn(cp.getModelSn());
			dc.setIndex(rank);
			dc.setLevel(cp.getLevel());
			msg.addRanks(dc);
			if(cp.getId() == humanObj.getHumanId()){
				selfRank = rank;
			}
		}
		msg.setSelfRank(selfRank);
		humanObj.sendMsg(msg);
	}
	/**
	 * 请求定时总战力排行
	 */
	public void _msg_CSSumCombatRank(HumanObject humanObj){
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.getRank(ERankType.RankTypeSumCombat);
		proxy.listenResult(this::_result_msg_CSSumCombatRank, "humanObj", humanObj);
	}
	private void _result_msg_CSSumCombatRank(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		List<RankSumCombat> rankSumCombat = results.get();
		int selfRank = -1;// 自己的排名：-1即未上榜
		if (humanObj == null || rankSumCombat == null || rankSumCombat.isEmpty()) {
			Log.game.error("===_result_GuildRank humanObj={}, rankList={}", humanObj, rankSumCombat);
			return;
		}
		if(rankSumCombat.size()<=0){
			SCNodata.Builder msgs = SCNodata.newBuilder();
			humanObj.sendMsg(msgs);
		}
		for (RankSumCombat rank : rankSumCombat) {
			if (rank.getHumanId() == humanObj.getHuman().getId()) {
				selfRank = rank.getRank();
				break;
			}
		}
		rankSumCombat = getRankSumCombatList(rankSumCombat, 0, RankType.RankSumCombat.getSize());
		
		SCSumCombatRank.Builder msg = SCSumCombatRank.newBuilder();
		for (RankSumCombat rank : rankSumCombat) {
			msg.addRanks(createMsgDSumCombatRank(rank));
		}
		msg.setSelfRank(selfRank);// 自己的排名
		msg.setSelfCombat(humanObj.getHuman().getSumCombat());//自身战斗力
		humanObj.sendMsg(msg);
	}

	public void _msg_SCActCombatRank(HumanObject humanObj){
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.getActCombatList();
		proxy.listenResult(this::_result_msg_SCActCombatRank, "humanObj",humanObj);
	}
	private void _result_msg_SCActCombatRank(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		List<RankSumCombat> rankSumCombat = results.get();
		int selfRank = -1;// 自己的排名：-1即未上榜
		int selfCombat = 0;
		if (humanObj == null || rankSumCombat == null || rankSumCombat.isEmpty()) {
			Log.game.error("===_result_GuildRank humanObj={}, rankList={}", humanObj, rankSumCombat);
			return;
		}
		if(rankSumCombat.size()<=0){
			SCNodata.Builder msgs = SCNodata.newBuilder();
			humanObj.sendMsg(msgs);
		}
		for (RankSumCombat rank : rankSumCombat) {
			if (rank.getHumanId() == humanObj.getHuman().getId()) {
				selfRank = rank.getRank();
				selfCombat = rank.getSumCombat();
				break;
			}
		}
		if(selfCombat == 0){
			selfCombat = humanObj.getHuman().getCombat();
		}
		rankSumCombat = getRankSumCombatList(rankSumCombat, 0, RankType.RankSumCombat.getSize());
		
		SCActCombatRank.Builder msg = SCActCombatRank.newBuilder();
		for (RankSumCombat rank : rankSumCombat) {
			msg.addRanks(createMsgDSumCombatRank(rank));
		}
		msg.setSelfRank(selfRank);// 自己的排名
		msg.setSelfCombat(selfCombat);//自身战斗力
		humanObj.sendMsg(msg);
	}
	/**
	 * 截取指定的长度
	 * @param list
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private List<RankSumCombat> getRankSumCombatList(List<RankSumCombat> list, int fromIndex, int toIndex) {
		if (fromIndex > toIndex || fromIndex < 0 || toIndex < 0) {
			return null;
		}
		if (list != null) {
			int size = list.size();
			if (toIndex > size)
				toIndex = size;
			if (list != null && fromIndex < size) {
				return list.subList(fromIndex, toIndex);
			}
		}
		return null;
	}

	/**
	 * 请求定时副本排行
	 * @param humanObj
	 */
	public void _msg_CSInstancsRank(HumanObject humanObj) {
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.getRank(ERankType.RankTypeInstance);
		proxy.listenResult(this::_result_instancsRank, "humanObj", humanObj);
	}
	private void _result_instancsRank(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		List<RankInstance> rankInstance = results.get(); // 获取RankInstance top集合

		int selfRank = -1;// 自己的排名：-1即未上榜
		int selfStars = 0;// 自己的星星数 0表示为挑战副本
		if (humanObj == null || rankInstance == null || rankInstance.isEmpty()) {
			Log.game.error("===_result_instancsRank humanObj={}, rankList={}", humanObj, rankInstance);
			SCInstanceRank.Builder msg = SCInstanceRank.newBuilder();
			humanObj.sendMsg(msg);
			return;
		}
		for (RankInstance rank : rankInstance) {
			if (rank.getHumanId() == humanObj.getHuman().getId()) {
				selfRank = rank.getRank();
				selfStars = rank.getStars();
				break;
			}
		}
		if (selfStars == 0) {
			selfStars = humanObj.getHuman().getRankInstStars(); //设置副本排行星星数
		}
		rankInstance = getRankInstacnceList(rankInstance, 0, RankType.RankInstance.getSize());
		// System.out.println("副本排行榜"+rankInstance);
		
		SCInstanceRank.Builder msg = SCInstanceRank.newBuilder();
		for (RankInstance rank : rankInstance) {
			msg.addRanks(createMsgDRankInstance(rank)); // 设置排行榜rank信息
		}
		msg.setSelfRank(selfRank);// 自己的排名
		msg.setSelfStars(selfStars);// 自己的星星数
		humanObj.sendMsg(msg);
	}
	/**
	 * 截取指定的长度
	 * @param list
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private List<RankInstance> getRankInstacnceList(List<RankInstance> list, int fromIndex, int toIndex) {
		if (fromIndex > toIndex || fromIndex < 0 || toIndex < 0) {
			return null;
		}
		if (list != null) {
			int size = list.size();
			if (toIndex > size)
				toIndex = size;
			if (list != null && fromIndex < size) {
				return list.subList(fromIndex, toIndex);
			}
		}
		return null;
	}
	
	/**
	 * 请求定时爬塔排行榜
	 * @param humanObj
	 */
	public void _msg_CSGetPVETowerRank(HumanObject humanObj) {
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.getRank(ERankType.RankTypeTower); //获取爬塔top排行数据
		proxy.listenResult(this::_result_PVETowerRank, "humanObj", humanObj);
	}
	private void _result_PVETowerRank(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		List<RankTower> rankTower = results.get();
		int selfRank = -1;// 自己的排名：-1即未上榜
		int selfGrade = -1;// 自己的爬塔数： -1即未爬塔过
		
		if (humanObj == null || rankTower == null || rankTower.isEmpty()) {
			
			Log.game.error("===_result_PVETowerRank humanObj={}, rankList={}", humanObj, rankTower);
			
			SCNodata.Builder msgs = SCNodata.newBuilder();
			humanObj.sendMsg(msgs);
			
			return;
		}
		for (RankTower rank : rankTower) {
			if (rank.getHumanId() == humanObj.getHuman().getId()) {
				selfRank = rank.getRank();
//				selfGrade = rank.getMaxFloor();
				selfGrade = rank.getGrade();
				break;
			}
		}
		if (selfGrade == -1) {
			//玩家实时积分 
			selfGrade = humanObj.getHumanExtInfo().getTowerScore(); //设置玩家实时积分
			//玩家实时爬塔数 
//			selfGrade = humanObj.getHumanExtInfo().getTowerMaxFloor(); //设置玩家实时爬塔数
		}
		
		rankTower = getRankTowerList(rankTower, 0, RankType.RankTower.getSize());
		
		SCGetPVETowerRank.Builder msg = SCGetPVETowerRank.newBuilder();
		
		for (RankTower rank : rankTower) {
			msg.addRank(createMsgDRankTower(rank)); //创建数据-爬塔排行
		}
		msg.setSelfRank(selfRank);// 自己的排名
		msg.setSelfGrade(selfGrade);// 自己的爬塔数
		humanObj.sendMsg(msg);
	}
	/**
	 * 截取指定的长度
	 * @param list
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private List<RankTower> getRankTowerList(List<RankTower> list, int fromIndex, int toIndex) {
		if (fromIndex > toIndex || fromIndex < 0 || toIndex < 0) {
			return null;
		}
		if (list != null) {
			int size = list.size();
			if (toIndex > size)
				toIndex = size;
			if (list != null && fromIndex < size) {
				return list.subList(fromIndex, toIndex);
			}
		}
		return null;
	}
	
	/**
	 * 请求定时洞天福地排行榜
	 */
	public void _msg_CSFairylandRank(HumanObject humanObj) {
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		proxy.getRank(ERankType.RankTypeFairyland); //请求top排行榜信息
		proxy.listenResult(this::_result_FairylandRank, "humanObj", humanObj);
	}
	private void _result_FairylandRank(Param results, Param context) {
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		List<RankFairyland> rankFairyland = results.get();
		int selfRank = -1;// 自己的排名：-1即未上榜
		long selfGrade = 0;// 自己的积分： -1即无积分
		
		if (humanObj == null || rankFairyland == null || rankFairyland.isEmpty()) {
			Log.game.error("===_result_FairylandRank humanObj={}, rankList={}", humanObj, rankFairyland);
			SCNodata.Builder msg = SCNodata.newBuilder();
			humanObj.sendMsg(msg);
			return;
		}
		
		for (RankFairyland rank : rankFairyland) {
			if (rank.getHumanId() == humanObj.getHuman().getId()) {
				selfRank = rank.getRank();
				selfGrade = rank.getLootMapScore();
				break;
			}
		}
		if (selfGrade == 0) {
			// 如果在排行版中没有找到玩家数据  返回玩家实时洞天福地积分
			selfGrade = humanObj.getHumanExtInfo().getLootMapMultipleScore(); // 获取玩家实时洞天福地积分
		}
		
		rankFairyland = getRankFairylandList(rankFairyland, 0, RankType.RankFairyland.getSize());
		
		SCFairylandRank.Builder msg = SCFairylandRank.newBuilder();
		for (RankFairyland rank : rankFairyland) {
			msg.addRanks(createMsgDRankFairyland(rank)); //创建数据-爬塔排行
		}
		msg.setSelfRank(selfRank);// 自己的排名
		msg.setSelfGrade(Utils.intValue(selfGrade));// 自己的爬塔数
		humanObj.sendMsg(msg);

	}
	/**
	 * 截取指定的长度
	 * @param list
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	private List<RankFairyland> getRankFairylandList(List<RankFairyland> list, int fromIndex, int toIndex) {
		if (fromIndex > toIndex || fromIndex < 0 || toIndex < 0) {
			return null;
		}
		if (list != null) {
			int size = list.size();
			if (toIndex > size)
				toIndex = size;
			if (list != null && fromIndex < size) {
				return list.subList(fromIndex, toIndex);
			}
		}
		return null;
	}
	
	
	/**
	 * 排行榜膜拜
	 * @param humanObj
	 */
	public void _msg_CSWorship(HumanObject humanObj, EWorshipType worshipType) {
		int type = worshipType.getNumber();
		boolean result = false;
		//目前改为只能膜拜战力榜的
		if (EWorshipType.WorshipCombat != worshipType || getWorshipStatus(humanObj, type) == 0) {
			SCWorship.Builder msg = SCWorship.newBuilder();
			msg.setResult(result);
			humanObj.sendMsg(msg);
			return;
		}

		SCWorship.Builder msg = SCWorship.newBuilder();

		if (ParamManager.rankTopRewardNum.isEmpty() || ParamManager.rankTopRewardSn.isEmpty()) {
			Log.table.error("Param配表出错,rankTopRewardNum，rankTopRewardSn不匹配");
			msg.setResult(result);
			humanObj.sendMsg(msg);
			return;
		}
		int humamLevel = humanObj.getHuman().getLevel();
		int[] Sns = getIntArr(ParamManager.rankTopRewardSn, type);
		int[] Nums = getIntArr(ParamManager.rankTopRewardNum, type);
		if (Sns == null || Nums == null || Sns.length != Nums.length) {
			Log.table.error("Param配表出错,rankTopRewardNum，rankTopRewardSn不匹配");
			msg.setResult(result);
			humanObj.sendMsg(msg);
			return;
		}

		// 放材料入背包
		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();

		for (int i = 0; i < Sns.length; i++) {
			if (ConfItem.get(Sns[i]) == null) {
				Log.table.error("ConfEquipResolve配表错误,没有指定sn的物品");
				continue;
			}
			ProduceVo produce = new ProduceVo(Sns[i], Nums[i] * humamLevel);
			itemProduce.add(produce);
		}
		result = true;
		// 添加物品如背包
		ProduceManager.inst().giveProduceItem(humanObj, itemProduce, LogSysModType.RankWorship);
		msg.setResult(result);
		// 改变膜拜状态
		setWorshipStatus(humanObj, type);
		humanObj.sendMsg(msg);
	}

	/**
	 * 从配表的String|string,获得int[]数组
	 * @param str
	 * @param index
	 * @return
	 */
	public int[] getIntArr(String str, int index) {
		String[] strs = ItemBagManager.inst().getStringToStrArray(str);
		int[] nums = ItemBagManager.inst().getStringToInt(strs[index]);

		return nums;
	}

	/**
	 * 得到具体排行榜的膜拜状态
	 * @param humanObj
	 * @param type
	 * @return
	 */
	public int getWorshipStatus(HumanObject humanObj, int type) {
		int ret = -1;
		int COUNT = ParamManager.rankWorshipTimes;// 配表可以膜拜的次数
		Integer result = Utils.getJSONValueInt(humanObj.getHuman().getDailyRankWorship(), type);
		if (result != null)
			ret = result.intValue();
		return COUNT - ret;
	}

	/**
	 * 设置具体排行榜的状态
	 * @param humanObj
	 * @param type
	 */
	public void setWorshipStatus(HumanObject humanObj, int type) {
		JSONObject jo = Utils.toJSONObject(humanObj.getHuman().getDailyRankWorship());
		if (jo != null && !jo.isEmpty() && jo.containsKey(String.valueOf(type))) {
			Integer status = (Integer) jo.get(String.valueOf(type));
			if (status != null) {
				jo.put(String.valueOf(type), status + 1);
			}
		}
		humanObj.getHuman().setDailyRankWorship(jo.toJSONString());
	}
	/**
	 * 获取排行榜膜拜次数信息
	 * @param human
	 * @return
	 */
	public List<DRankWorship> getRankWorship(Human human) {
		List<DRankWorship> list = new ArrayList<>();
		Map<Integer, Integer> mapRankWorship = Utils.jsonToMapIntInt(human.getDailyRankWorship());
		for (Entry<Integer, Integer> entry : mapRankWorship.entrySet()) {
			EWorshipType type = EWorshipType.valueOf(entry.getKey());
			Integer num = entry.getValue();
			if (type != null && num != null) {
				DRankWorship.Builder dRankWorship = DRankWorship.newBuilder();
				dRankWorship.setType(type);
				dRankWorship.setNum(num);
				list.add(dRankWorship.build());
			}
		}
		return list;
	}

	/**
	 * 将排行榜信息转换成城主信息
	 */
	public Castellan rankToCastenllan(RankSumCombat rank){
		if(rank == null){
			Log.game.error("RankManager.rankToCastenllan error RankSumCombat ");
			return null;
		}
		Castellan castellan = new Castellan();
		castellan.setId(rank.getId());
		castellan.setModelSn(rank.getHeadSn());
		castellan.setHumanId(rank.getHumanId());
		castellan.setName(rank.getName());
		castellan.setType(ECastellanType.SumCombatDuke_VALUE);
		//设置城主商店默认购买信息
		castellan.setHasBuyNum(0);
		return castellan;
	}

	public Castellan rankToCastenllan(RankLevel rank){
		if(rank == null){
			Log.game.error("RankManager.rankToCastenllan error RankLevel ");
			return null;
		}
		Castellan castellan = new Castellan();
		castellan.setId(rank.getId());
		castellan.setModelSn(rank.getModelSn());
		castellan.setHumanId(rank.getHumanId());
		castellan.setName(rank.getName());
		castellan.setType(ECastellanType.LevelDuke_VALUE);
		//设置城主商店默认购买信息
		castellan.setHasBuyNum(0);
		return castellan;
	}

	public Castellan rankToCastenllan(RankTower rank){
		if(rank == null){
			Log.game.error("RankManager.rankToCastenllan error RankTower ");
			return null;
		}
		Castellan castellan = new Castellan();
		castellan.setId(rank.getId());
		castellan.setModelSn(rank.getModelSn());
		castellan.setHumanId(rank.getHumanId());
		castellan.setName(rank.getName());
		castellan.setType(ECastellanType.TowerDuke_VALUE);
		//设置城主商店默认购买信息
		castellan.setHasBuyNum(0);
		return castellan;
	}

	public Castellan rankToCastenllan(RankInstance rank){
		if(rank == null){
			Log.game.error("RankManager.rankToCastenllan error RankInstance ");
			return null;
		}
		Castellan castellan = new Castellan();
		castellan.setId(rank.getId());
		castellan.setModelSn(rank.getModelSn());
		castellan.setHumanId(rank.getHumanId());
		castellan.setName(rank.getName());
		castellan.setType(ECastellanType.InstanceDuke_VALUE);
		//设置城主商店默认购买信息
		castellan.setHasBuyNum(0);
		return castellan;
	}

	public Castellan rankToCastenllan(RankFairyland rank){
		if(rank == null){
			Log.game.error("RankManager.rankToCastenllan error RankInstance ");
			return null;
		}
		Castellan castellan = new Castellan();
		castellan.setId(rank.getId());
		castellan.setModelSn(rank.getModelSn());
		castellan.setHumanId(rank.getHumanId());
		castellan.setName(rank.getName());
		castellan.setType(ECastellanType.FairylandDuke_VALUE);
		//设置城主商店默认购买信息
		castellan.setHasBuyNum(0);
		return castellan;
	}
	
	/**
	 * 深拷贝洞天福地排行
	 */
	public RankFairyland deepCloneRankFairyland(RankFairyland temp) throws IOException, OptionalDataException,
	ClassNotFoundException {
		OutputStream out = new OutputStream();
		temp.writeTo(out);
		RankFairyland ret = new RankFairyland();
		InputStream in = new InputStream(out.getChunk());
		ret.readFrom(in);
		out.close();// 切记要关闭
		return ret;
	}
	
	
	
	/**
	 * 深拷贝等级排行
	 */
	public RankLevel deepCloneRankLevel(RankLevel temp) throws IOException, OptionalDataException,
			ClassNotFoundException {
		OutputStream out = new OutputStream();
		temp.writeTo(out);
		RankLevel ret = new RankLevel();
		InputStream in = new InputStream(out.getChunk());
		ret.readFrom(in);
		out.close();// 切记要关闭
		return ret;
	}
	/**
	 * 深拷贝VIP排行
	 */
	public RankVip deepCloneRankVip(RankVip temp) throws IOException, OptionalDataException, ClassNotFoundException {
		OutputStream out = new OutputStream();
		temp.writeTo(out);
		RankVip ret = new RankVip();
		InputStream in = new InputStream(out.getChunk());
		ret.readFrom(in);
		out.close();// 切记要关闭
		return ret;
	}
	/**
	 * 深拷贝爬塔排行
	 */
	public RankTower deepCloneRankTower(RankTower temp) throws IOException, OptionalDataException,
			ClassNotFoundException {
		OutputStream out = new OutputStream();
		temp.writeTo(out);
		RankTower ret = new RankTower();
		InputStream in = new InputStream(out.getChunk());
		ret.readFrom(in);
		out.close();// 切记要关闭
		return ret;
	}
	/**
	 * 深拷贝公会排行
	 */
	public RankGuild deepCloneRankGuild(RankGuild temp) throws IOException, OptionalDataException,
			ClassNotFoundException {
		OutputStream out = new OutputStream();
		temp.writeTo(out);
		RankGuild ret = new RankGuild();
		InputStream in = new InputStream(out.getChunk());
		ret.readFrom(in);
		out.close();// 切记要关闭
		return ret;
	}
	/**
	 * 深拷贝副本排行
	 */
	public RankInstance deepCloneRankInstance(RankInstance temp) throws IOException, OptionalDataException,
	ClassNotFoundException {
		OutputStream out = new OutputStream();
		temp.writeTo(out);
		RankInstance ret = new RankInstance();
		InputStream in = new InputStream(out.getChunk());
		ret.readFrom(in);
		out.close();// 切记要关闭
		return ret;
	}
	/**
	 * 是否是城主换任时间
	 */
	public boolean isUpdateTime(ECastellanType type) {
		int sn = type.getNumber();
		ConfMainCityShow conf = ConfMainCityShow.get(sn);
		int nowHour =  Utils.getHourOfDay();
		if(conf == null) {
			Log.game.error("RankManager isUpdateTime  table error sn ={}",sn);
			return false;
		}
		int hour = conf.changeTime/10000;
//		return true;
		if(hour == nowHour) {
			return true;
		}else {
			return false;
		}
		
	}
	
	
	@Listener(EventKey.GuildAddExp)
	public void updateGuildRank(Param param) {
		Guild guild = Utils.getParamValue(param, "guild", null);
		if (guild == null) {
			return;
		}
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		//判断入榜最低条件
		if(guild.getGuildExp() >= ParamManager.rankTopRecordFilter[RankIntType.Guild.value()]) {
			proxy.addNew(new RankData(guild),RankType.RankGuild);
		}else {
			Log.game.info("未达到入榜条件");
		}
			
	}
	
	public void buildRankGuild(RankData data, RankGuild rankGuild) {
		rankGuild.setId(data.guildId);
		rankGuild.setGuildId(data.guildId);
		rankGuild.setGuildName(data.guildName);
		rankGuild.setGuildLevel(data.guildLevel);
		rankGuild.setGuildExp(data.guildExp);
		rankGuild.setGuildLeaderName(data.guildLeaderName);
		rankGuild.setGuildMember(data.num);
		rankGuild.setGuildIcon(data.guildIcon);
	}
}
