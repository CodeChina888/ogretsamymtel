package game.worldsrv.produce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import core.Port;
import core.support.ManagerBase;
import game.worldsrv.config.ConfInstActConfig;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.Human;

import game.msg.MsgInstance.SCInstEnd;
import game.worldsrv.character.HumanObject;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.ItemVO;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.support.Log;
import game.worldsrv.support.ReasonResult;
import game.worldsrv.support.Utils;
import game.worldsrv.team.TeamManager;

import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;

public class ProduceManager extends ManagerBase {

	public static ProduceManager inst() {
		return inst(ProduceManager.class);
	}

	/**
	 * 从 ConfRewards配置中获得物品，例如：开箱子
	 * @param sn
	 * @return
	 */
	public List<ProduceVo> produceItem(int sn) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		ConfRewards confRewards = ConfRewards.get(sn);
		if (confRewards == null) {
			Log.table.error("===配表错误：ConfRewards no find sn={}", sn);
			return result;
		}
		if (confRewards.itemSn.length != confRewards.itemNum.length) {
			Log.table.error("===配表错误：ConfRewards sn={},itemSn.length != itemNum.length", sn);
			return result;
		}
		for (int i = 0; i < confRewards.itemSn.length; i++) {
			ProduceVo vo = new ProduceVo(confRewards.itemSn[i], confRewards.itemNum[i]);
			result.add(vo);
		}
		return result;
	}

	/**
	 * 从 produce 配置中获得物品
	 * @param sns
	 */
	public List<ProduceVo> produceItem(int[] sns) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if (sns != null && sns.length > 0) {
			for (int sn : sns) {
				result.addAll(produceItem(sn));
			}
		}
		return result;
	}

	/**
	 * 从类型数组 以及 数量数组拼装item
	 * @param snStr 多个物品sn（格式：1,2,3或1|2|3）
	 * @param numStr 多个物品数量（格式：1,2,3或1|2|3）
	 * @return
	 */
	public List<ProduceVo> produceItem(String snStr, String numStr) {
		int[] sns = new int[]{};
		int[] nums = new int[]{};
		
		if(StringUtils.isNotEmpty(snStr)) {
			String [] ids = null;
			if(snStr.contains("|")) {
				ids = snStr.split("|");
			} else {
				ids = snStr.split(",");
			}
			String [] ns = null; 
			if(numStr.contains("|")) {
				ns = numStr.split("|");
			} else {
				ns = numStr.split(",");
			}
			int len = ids.length;
			
			sns = new int[len];
			nums = new int[len];
			
			for(int i=0; i<len; i++) {
				sns[i] = Integer.parseInt(ids[i]);
				nums[i] = Integer.parseInt(ns[i]);
			}
		}
		
		return produceItem(sns, nums);
	}
	
	/**
	 * 从类型数组 以及 数量数组拼装item
	 * @param sns 多个物品sn
	 * @param nums 多个物品数量
	 * @return
	 */
	public List<ProduceVo> produceItem(int[] sns, int[] nums) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if (sns == null || nums == null || sns.length != nums.length) {
			return result;
		}

		if (sns != null && sns.length > 0) {
			int length = sns.length;
			for (int j = 0; j < length; j++) {
				result.add(new ProduceVo(sns[j], nums[j]));
			}
		}
		return result;
	}

	/**
	 * 从int类型 以及 数量拼装item
	 * @param sn 单个物品sn
	 * @param num 单个物品数量
	 * @return
	 */
	public List<ProduceVo> produceItem(int sn, int num) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		result.add(new ProduceVo(sn, num));
		return result;
	}

	/**
	 * 实际给物品
	 * @param humanObj
	 * @param itemProduce
	 * @param log
	 */
	public ItemChange giveProduceItem(HumanObject humanObj, List<ProduceVo> itemProduce, LogSysModType log) {
		//走统一给物品逻辑
		return RewardHelper.reward(humanObj, itemProduce, log);
		
//		// 封装itemVo list
//		List<ItemVO> itemList = new ArrayList<ItemVO>();
//
//		for (ProduceVo produceVo : itemProduce) {
//			ConfItem confItem = ConfItem.get(produceVo.sn);
//			if (confItem == null) {
//				Log.table.error("ConfItem配表错误，no find sn={}", produceVo.sn);
//				continue;
//			}
//			if (produceVo.isMoney) {// 添加货币：金钱，经验等
//				HumanManager.inst().addMoney(humanObj, produceVo.sn, produceVo.num, log);
//			} /*
//			 * else if (produceVo.isHotohori) {// 添加星宿碎片
//			 * HumanManager.inst().addHotohori(humanObj, produceVo.sn -
//			 * HotohoriType.Hotohori_0.getType(), produceVo.num, null); }
//			 */else {// 添加物品
//				itemList = ProduceVo.addItemVoList(itemList, produceVo);
//			}
//		}
//
//		// 添加物品
//		ItemBagManager.inst().add(humanObj, itemList, log);
	}

	/**
	 * 判断能不能添加
	 * @param humanObj
	 * @param itemProduce
	 */
	public boolean canGiveProduceItem(HumanObject humanObj, List<ProduceVo> itemProduce) {
		List<ItemVO> itemList = new ArrayList<ItemVO>();
		for (ProduceVo produceVo : itemProduce) {
			itemList = ProduceVo.addItemVoList(itemList, produceVo);
		}
		return ItemBagManager.inst().canAdd(humanObj, itemList);
	}

	/**
	 * 直接通过物品包SN 给物品 并返回给的物品：多个物品包，多个数量
	 * @param humanObj
	 * @param sns  
	 * @param log
	 * @return
	 */
	public List<ProduceVo> getAndGiveProduce(HumanObject humanObj, int[] sns,  int nums[], LogSysModType log) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if(sns != null && sns.length > 0) {
			//根据SN数量和NUM数量，进行多次匹配，获得总得物品包
			for (int i = 0; i < sns.length; i++) {
				int sn = sns[i];
				for (int j = 0; j < nums.length; j++) {
					result.addAll(produceItem(sn));
				}
			}
		}
		//实际给物品
		giveProduceItem(humanObj, result, log);
		return result;
	}
	
	/**
	 * 直接通过物品包SN 给物品 并返回给的物品：1个物品包，多个数量
	 * @param humanObj
	 * @param log
	 * @return
	 */
	public ItemChange getAndGiveProduce(HumanObject humanObj, int sn,  int nums, LogSysModType log) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
			
		//根据SN和NUM数量，进行多次匹配，获得总得物品包
		for (int j = 0; j < nums; j++) {
			result.addAll(produceItem(sn));
		}
		
		//实际给物品
		return giveProduceItem(humanObj, result, log);
	}
	
	/**
	 * 直接通过物品包SN 给物品 并返回给的物品
	 * @param humanObj
	 * @param sn
	 * @param log
	 * @return
	 */
	public List<ProduceVo> getAndGiveProduce(HumanObject humanObj, int sn, LogSysModType log) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		result.addAll(produceItem(sn));//根据SN获得总得物品包
		giveProduceItem(humanObj, result, log);//实际给物品
		return result;
	}

	/**
	 * 直接通过物品包SN 给物品 并返回给的物品
	 * @param humanObj
	 * @param log
	 * @return
	 */
	public List<ProduceVo> getAndGiveProduce(HumanObject humanObj, int[] sns, LogSysModType log) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if(sns != null && sns.length > 0) {
			//根据SN数量获得总得物品包
			for (int i = 0; i < sns.length; i++) {
				result.addAll(produceItem(sns[i]));
			}
		}
		//实际给物品
		giveProduceItem(humanObj, result, log);
		return result;
	}

	/**
	 * 合并物品 主要用于扫荡这样的模块 物品一堆一堆的 。 需要合并
	 * @param itemProduce
	 * @return
	 */
	public List<ProduceVo> mergeProduce(List<ProduceVo> itemProduce) {
		List<ProduceVo> itemProduceItem = new ArrayList<ProduceVo>();
		// 物品合并
		Map<Integer, ProduceVo> itemMerge = new HashMap<Integer, ProduceVo>();
		for (ProduceVo produceVo : itemProduce) {
			if (itemMerge.containsKey(produceVo.sn)) {
				ProduceVo temp = itemMerge.get(produceVo.sn);
				temp.num += produceVo.num;
			} else {
				itemMerge.put(produceVo.sn, produceVo);
			}
		}
		for (ProduceVo produceVo : itemMerge.values()) {
			itemProduceItem.add(produceVo);
		}
		return itemProduceItem;
	}

	/**
	 * 将produceVo转化为Jon
	 * @param list
	 * @return
	 */
	public String produceToJson(List<ProduceVo> list) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (ProduceVo produceVo : list) {
			Integer num = map.get(String.valueOf(produceVo.sn));
			if(num == null) {
				num = produceVo.num;
			}else {
				num += produceVo.num;
			}
			map.put(String.valueOf(produceVo.sn), num);
			
		}

		return Utils.toJSONString(map);
	}

	/**
	 * 把Json转化为List, Json里面是个Map<物品ID， 物品数量>
	 * @param json
	 * @return
	 */
	public List<ProduceVo> jsonToProduceList(String json) {
		// 判断非空
		if (StringUtils.isEmpty(json))
			return new ArrayList<ProduceVo>();

		// 提取Map
		Map<Integer, Integer> map = Utils.jsonToMapIntInt(json);
		List<ProduceVo> itemList = new ArrayList<ProduceVo>();
		for (Entry<Integer, Integer> e : map.entrySet()) {
			ProduceVo vo = new ProduceVo(Utils.intValue(e.getKey() + ""), e.getValue());
			itemList.add(vo);
		}

		return itemList;
	}

	/**
	 * 下发活动副本配表奖励
	 * @param msg
	 * @param endType 结束类型：0失败，1胜利，2平局（活动副本才需下发）
	 */
	public void giveActInstAward(HumanObject humanObj, SCInstEnd.Builder msg, int endType,
			ConfInstActConfig confInstActConfig) {
		Human human = humanObj.getHuman();
		//msg.setEndType(endType);
		// 给予活动副本配表的三种奖励：固定奖励，额外奖励，通关奖励（失败，胜利，平局）
		// 固定奖励
		List<ProduceVo> listProducesAll = new ArrayList<ProduceVo>();
		if (confInstActConfig.baseAward > 0) {
			List<ProduceVo> listProduces = ProduceManager.inst().produceItem(confInstActConfig.baseAward);
			if (!listProduces.isEmpty()) {
				listProducesAll.addAll(listProduces);
				for (ProduceVo produce : listProduces) {
					//msg.addEndAwardBase(produce.toDProduce());
				}
			}
		}

		// 每天给予奖励次数已用完，只给固定奖励，不给额外奖励和通关奖励
		if (!TeamManager.inst().isNoneDayAwardNum(human.getDailyInstFinishNum(), confInstActConfig)) {
			// 额外奖励
			if (confInstActConfig.startHourExtra != null && confInstActConfig.endHourExtra != null
					&& confInstActConfig.startHourExtra.length == confInstActConfig.endHourExtra.length) {
				int hourOfDay = Utils.getHourOfDay(Port.getTime());// 小时数
				boolean isExtraTime = false;// 是否在额外奖励时间段内
				for (int i = 0; i < confInstActConfig.startHourExtra.length; i++) {
					if (hourOfDay >= confInstActConfig.startHourExtra[i] && hourOfDay < confInstActConfig.endHourExtra[i]) {
						isExtraTime = true;
						break;
					}
				}
				if (isExtraTime && confInstActConfig.extraAward > 0) {
					List<ProduceVo> listProduces = ProduceManager.inst().produceItem(confInstActConfig.extraAward);
					if (!listProduces.isEmpty()) {
						listProducesAll.addAll(listProduces);
						for (ProduceVo produce : listProduces) {
							//msg.addEndAwardExtra(produce.toDProduce());
						}
					}
				}
			}
			// 通关奖励（失败，胜利，平局）
			if (endType == 0 && confInstActConfig.loseAward > 0) {// 失败
				List<ProduceVo> listProduces = ProduceManager.inst().produceItem(confInstActConfig.loseAward);
				if (!listProduces.isEmpty()) {
					listProducesAll.addAll(listProduces);
					for (ProduceVo produce : listProduces) {
						//msg.addEndAwardPass(produce.toDProduce());
					}
				}
			} else if (endType == 1 && confInstActConfig.winAward > 0) {// 胜利
				List<ProduceVo> listProduces = ProduceManager.inst().produceItem(confInstActConfig.winAward);
				if (!listProduces.isEmpty()) {
					listProducesAll.addAll(listProduces);
					for (ProduceVo produce : listProduces) {
						//msg.addEndAwardPass(produce.toDProduce());
					}
				}
			} else if (endType == 2 && confInstActConfig.tieAward > 0) {// 平局
				List<ProduceVo> listProduces = ProduceManager.inst().produceItem(confInstActConfig.tieAward);
				if (!listProduces.isEmpty()) {
					listProducesAll.addAll(listProduces);
					for (ProduceVo produce : listProduces) {
						//msg.addEndAwardPass(produce.toDProduce());
					}
				}
			}
		}

		if (!listProducesAll.isEmpty()) {// 下发奖励
			giveProduceItem(humanObj, listProducesAll, LogSysModType.Inst);
		}
	}
	
	/**
	 * 清理List<ProduceVo> listPro，删除无效的物品
	 * @param humanObj
	 * @param listPro
	 * @return
	 */
    public List<ProduceVo> cleanProduce(HumanObject humanObj,List<ProduceVo> listPro){	
    	List<ProduceVo> listReturn = new ArrayList<ProduceVo>();
		for(int i=0;i<listPro.size();i++){
			ProduceVo produceVo = listPro.get(i);			
			if(null != produceVo){
				ItemVO vo = new ItemVO(produceVo.sn, produceVo.num);
				ReasonResult rr = ItemBagManager.inst().canAdd(humanObj, vo);
				if(rr.success){
					listReturn.add(produceVo);
				}
			}			
		}
		return listReturn;
    }

}
