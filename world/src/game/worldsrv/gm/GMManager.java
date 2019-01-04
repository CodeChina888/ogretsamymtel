package game.worldsrv.gm;

import game.msg.Define.DActivityParam;
import game.msg.Define.EAwardType;
import game.msg.Define.EInformType;
import game.msg.Define.ELoginType;
import game.msg.Define.EModeType;
import game.msg.Define.EMoneyType;
import game.msg.Define.EQuestDailyStatus;
import game.seam.msg.MsgParam;
import game.support.DataReloadManager;
import game.worldsrv.activity.ActivityManager;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.compete.CompeteManager;
import game.worldsrv.config.ConfInstChapter;
import game.worldsrv.config.ConfInstStage;
import game.worldsrv.config.ConfItem;
import game.worldsrv.config.ConfLevelExp;
import game.worldsrv.config.ConfModUnlock;
import game.worldsrv.config.ConfSkill;
import game.worldsrv.config.ConfVipUpgrade;
import game.worldsrv.drop.DropBag;
import game.worldsrv.drop.DropManager;
import game.worldsrv.entity.Castellan;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Instance;
import game.worldsrv.entity.Partner;
import game.worldsrv.enumType.GMPrivilegeType;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.guild.GuildInstManager;
import game.worldsrv.guild.GuildManager;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.immortalCave.CaveServiceProxy;
import game.worldsrv.inform.InformManager;
import game.worldsrv.instWorldBoss.InstWorldBossManager;
import game.worldsrv.instance.InstanceManager;
import game.worldsrv.item.ItemBagManager;
import game.worldsrv.item.ItemBodyManager;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.mail.MailManager;
import game.worldsrv.maincity.MaincityServiceProxy;
import game.worldsrv.modunlock.ModunlockManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.payment.PaymentManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.quest.QuestDailyManager;
import game.worldsrv.quest.QuestJSON;
import game.worldsrv.raffle.RaffleManager;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.rank.RankManager;
import game.worldsrv.stage.StageManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.SensitiveWordFilter;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;
import game.worldsrv.team.TeamManager;
import game.worldsrv.tower.TowerManager;
import game.worldsrv.tower.TowerServiceProxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.ManagerBase;
import core.support.OrderBy;
import core.support.Param;
import core.support.Time;

public class GMManager extends ManagerBase {
	/**
	 * 获取实例
	 * @return
	 */
	public static GMManager inst() {
		return inst(GMManager.class);
	}

	/**
	 * GM命令
	 * @param param
	 * @param command
	 */
	public void gmCommand(MsgParam param, String command) {
		try {
			if (command == null || command.isEmpty()) {
				return;
			}
			// 检查是否开启GM
			if (!ParamManager.openGM) {
				Log.game.info("gm命令执行失败：配表没有开启GM！ParamManager.openGM={}", ParamManager.openGM);
				return;
			}
			// 检查是否有GM权限
			HumanObject humanObj = param.getHumanObject();
			// 上线后要开启
			if (GMPrivilegeType.Gm.value() != humanObj.getHuman().getGMPrivilege()) {
				Log.game.info("gm命令执行失败：没有权限！id={}", humanObj.getHumanId());
				return;
			}
			
			Log.game.info("gm命令执行开始：gmCommand={}", command);

			String[] order = command.split(" ");
			int orderLength = order.length;
			if (orderLength <= 0) {
				return;
			}
			
			String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
			
			if (gmOrder.length() >= 5 && gmOrder.substring(0, 5).equals("debug")) {//测试客户端调试相关功能
				gm_debug(humanObj, order);
			} else if (gmOrder.length() >= 4 && gmOrder.substring(0, 4).equals("test")) {//测试test相关功能
				gm_test(humanObj, order);
			} else if (gmOrder.length() >= 5 && gmOrder.substring(0, 5).equals("human")) {//人物相关功能
				gm_human(humanObj, order);
			} else if (gmOrder.length() >= 4 && gmOrder.substring(0, 4).equals("item")) {//物品相关功能
				gm_item(humanObj, order);
			} else if (gmOrder.length() >= 5 && gmOrder.substring(0, 5).equals("quest")) {//任务相关功能
				gm_quest(humanObj, order);
			} else if (gmOrder.length() >= 3 && gmOrder.substring(0, 3).equals("act")) {//活动相关功能
				gm_act(humanObj, order);
			} else if (gmOrder.length() >= 4 && gmOrder.substring(0, 4).equals("inst")) {//副本相关功能
				gm_inst(humanObj, order);
			} else if (gmOrder.length() >= 4 && gmOrder.substring(0, 5).equals("tower")) {//爬塔相关功能
				gm_tower(humanObj, order);
			} else if (gmOrder.length() >= 3 && gmOrder.substring(0, 3).equals("gen")) {//伙伴相关功能
				gm_gen(humanObj, order);
			}else if (gmOrder.length() >= 3 && gmOrder.substring(0, 3).equals("vip")) {//vip相关功能
				gm_vip(humanObj, order);
			} else if (gmOrder.length() >= 4 && gmOrder.substring(0, 4).equals("mail")) {//邮件相关功能
				gm_mail(humanObj, order);
			} else if (gmOrder.length() >= 4 && gmOrder.substring(0, 4).equals("team")) {//组队副本相关功能
				gm_team(humanObj, order);
			} else if (gmOrder.length() >= 4 && gmOrder.substring(0, 4).equals("rank")) {//排行榜相关功能
				gm_rank(humanObj, order);
			} else if (gmOrder.length() >= 5 && gmOrder.substring(0, 5).equals("guild")) {//公会相关功能
				gm_guild(humanObj, order);
			} else if (gmOrder.length() >= 5 && gmOrder.substring(0, 5).equals("daddy")) {//战斗无敌
				gm_daddy(humanObj, order);
			}
			
			Log.game.info("gm命令执行完毕：gmCommand={}", command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String toParamStr(Map<String, String> params, String splitStr) throws UnsupportedEncodingException {
		// 1.2 拼接参数
		StringBuilder strb = new StringBuilder();
		if (params.isEmpty() == false) {
			for (Entry<String, String> entry : params.entrySet()) {
				Object value = entry.getValue();
				String v = (value == null) ? "" : entry.getValue();
				
				strb.append(entry.getKey()).append("=").append(v).append(splitStr);
			}
			return strb.deleteCharAt(strb.length()-1).toString();
		}
		return strb.toString();
	}
	
	/**
	 * 测试计数掉落
	 * 
	 */
	public void DropTest(HumanObject humanObj,int groupId){

//		int size = GlobalConfVal.getChanceMap().get(1001).size();
//		System.out.println(size);
		
		List<DropBag> resultList = new ArrayList<>();
		int TestCount = 1000;
		for (int i = 0; i < TestCount; i++) {
			DropBag dr = DropManager.inst().getItem(humanObj, groupId);
			resultList.add(dr);
		}
		Map<Integer,Integer>  map = new HashMap<>();
		//统计
		System.out.println("===========结果列表============");
		System.out.println(resultList);
		for (DropBag db : resultList) {
			
			int ItemSn [] =  db.getItemSn();
			int ItemNum [] = db.getItemNum();
			if(ItemSn== null ||ItemSn.length<=0 ){
				continue;
			}
			for (int i = 0; i < ItemSn.length; i++) {
				if(!map.containsKey(ItemSn[i])){
					//map中没有该物品
					map.put(ItemSn[i], ItemNum[i]);
				}else{
					//如果有
					map.put(ItemSn[i], map.get(ItemSn[i])+ItemNum[i]);
				}
			}
			
		}
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
				ConfItem confItem =ConfItem.get(entry.getKey());
			   System.out.println("道具sn=" + confItem.sn + " and 数量="+ entry.getValue());
		}
		
	
	}
	/**
	 * 人物相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_human(HumanObject humanObj, String[] order){
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "humanmodunlock" :// 开启所有功能
				gm_humanModunlock(humanObj);
				break;
			case "humanmoney" :// 添加货币
				gm_humanMoney(humanObj, order);
				break;
			case "humansetlv" :// 设置等级
				gm_humanSetLv(humanObj, order);
				break;
			case "humanbuyactclear" :// 重置购买体力次数
				gm_humanBuyActClear(humanObj);
				break;
			case "humanbuyact" :// 购买体力
				gm_humanBuyAct(humanObj);
				break;
			case "humangomap" : // 传送地图 /humangomap 1即mapSn
				gm_humanGoMap(humanObj, order);
				break;
			case "humanonekey" :// 一键 成高级号
				gm_humanOnekey(humanObj);
				break;
			case "humannumclear" :// 重置人物身上大部分次数
				gm_humanNumClear(humanObj);
				break;
			case "humanreloadres" : // 重新加载表格数据
				gm_humanReloadConf(humanObj, order);
				break;
			case "humannotice": // GM临时公告
				gm_humanNotice(humanObj,order);
				break;
			case "humanofficialclear":// 重置俸禄领取状态
				gm_humanofficialClear(humanObj);
				break;
			case "humanallskill"://激活人物所有未激活的技能（普通技能除外）
				gm_humanAllSkill(humanObj);
				break;
			case "humandaddy"://各个物品都给99999
				gm_humanDaddy(humanObj,order);
				break;
			case "humancpbuy"://竞技场购买挑战次数
				gm_humancpbuy(humanObj,order);
				break;
			case "humanluck": //幸运转盘
				gm_humanluck(humanObj,order);
				break;
			default:
				break;
		}
	}
	/**
	 * 竞技场购买挑战次数
	 * @param humanObj
	 * @param order
	 */
	private void gm_humancpbuy(HumanObject humanObj, String[] order) {
		int num = Utils.intValue(order[1]);
		for (int i = 0; i < num; i++) {
			CompeteManager.inst().CSCompeteBuyNum(humanObj);
		}
	}
	
	/**
	 * 打开幸运转盘
	 * @param humanObj
	 * @param order
	 */
	private void gm_humanluck(HumanObject humanObj, String[] order) {
		int type = Utils.intValue(order[1]);
		int lv = Utils.intValue(order[2]);
		EModeType modeType = EModeType.valueOf(type);
		RaffleManager.inst().send_openTurntable(humanObj, modeType, lv);
	}

	/**
	 * 各个物品都给到99999
	 */
	private void gm_humanDaddy(HumanObject humanObj,String [] order){
		int num = Utils.intValue(order[1]);
		Collection<ConfItem> confmap = ConfItem.findAll();
		for (int i = 0; i <=10; i++) {
			RewardHelper.reward(humanObj, i, num, LogSysModType.GmTest);
		}
		
		for(ConfItem conf:confmap){
			try {
				RewardHelper.reward(humanObj, conf.sn, num, LogSysModType.GmTest);
			} catch (Exception e) {
				continue;
			}
		}
		gm_humanSetLv(humanObj, new String []{"60"});
		
	}
	/**
	 * 公会相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_guild(HumanObject humanObj, String[] order){
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "guildcave":
				gm_guildcave(humanObj, order);
				break;
            default:
                break;
		}
	}
	
	/**
	 * 工会相关
	 */
	public void gm_guildcave(HumanObject humanObj,String[] order) {
		CaveServiceProxy prx = CaveServiceProxy.newInstance();
		int type = Integer.valueOf(order[1]);
		int page = Integer.valueOf(order[2]);
		int index = Integer.valueOf(order[3]);
		prx.gmtest_getCaveInfo(type,page,index);
	}

	/**
	 * 任务相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_quest(HumanObject humanObj, String[] order){
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "questaccept":// 接受指定任务：/questaccept sn
				gm_questaccept(humanObj, order);
				break;
			case "questfinish" :// 完成指定任务：/questfinish sn
				gm_questfinish(humanObj, order);
				break;
			case "queststatusmodify":// 修改指定任务状态：/queststatusmodify sn,2
				gm_queststatusmodify(humanObj, order);
				break;
			case "questliveness" :// 增加活跃度
				gm_questLiveness(humanObj, order);
				break;
		default:
			break;
		}
	}
	
	/**
	 * 物品相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_item(HumanObject humanObj, String[] order){
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "itemadd" :// 添加物品
				gm_itemAdd(humanObj, order);
				break;
			case "itemuse" :// 使用道具
				gm_itemUse(humanObj, order);
				break;
			case "itemequip" :// 装备强化和精炼
				gm_itemEquip(humanObj, order);
				break;
			case "itembagadd":// 添加物品包
				gm_itemBagAdd(humanObj, order);
				break;
			case "itembagclear" :// 清空背包 /itemBagClear
				gm_itemBagClear(humanObj);
				break;
			case "itemhumanequip": // 得到主公装备等物品
				gm_itemHumanEquip(humanObj);
				break;
			case "itemtest": // 道具测试
				ItemBagManager.inst()._msg_CSSelectPackageItem(humanObj, 3, 113001, 1);
				break;
		default:
			break;
		}
	}
	
	/**
	 * 组队副本相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_team(HumanObject humanObj, String[] order) {
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "teamcreate" : // 创建组队副本
				gm_teamCreate(humanObj, order);
				break;
			case "teamjoin" : // 加入组队副本
				gm_teamJoin(humanObj, order);
				break;
			case "teamenterrep" : // 进入组队副本
				gm_teamEnterRep(humanObj, order);
				break;
				
		default:
			break;
		}
	}
	
	/**
	 * 邮件相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_mail(HumanObject humanObj, String[] order) {
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "mailonline" :// 给所有在线玩家发送邮件
				gm_mailOnline(order);
				break;
			case "mailall" :// 给所有玩家发送邮件
				gm_mailAll(order);
				break;
			case "mailmark" :// 发送特殊邮件
				gm_mailMarkSend(humanObj, order);
				break;
			case "mailcompetereward"://竞技场排行榜奖励
				gm_competereward();
				break;
		default:
			break;
		}
	}
	/**
	 * 竞技场排行榜奖励
	 */
	public void gm_competereward(){
	}
	
	
	/**
	 * 排行榜相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_rank(HumanObject humanObj, String[] order) {
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
			switch (gmOrder) {
			case "rankcombat" :// 查看战力排行
				gm_rankCombat(humanObj);
				break;
			case "ranklevel" :// 查看等级排行
				gm_rankLevel(humanObj);
				break;
			case "rankselfdelete" ://删除自身的排行榜信息
				gm_rankselfdelete(humanObj);
				break;
			case "rankupdate" :// 排行榜更新排行
				gm_rankUpdate();
				break;
			case "rankworshipclear" : // 排行重置玩家膜拜状态
				gm_rankWorshipClear(humanObj);
				break;
			case "rankupdateduck"://更换城主
				gmupdateDuke(humanObj);
				break;
			case "rankwboss"://成为世界boss城主
				gm_wboss(humanObj);
				break;
		default:
			break;
		}
	}
	
	/**
	 * 成为世界boss城主
	 */
	private void gm_wboss(HumanObject humanObj){
		// 发送玩家击杀boss事件
		Event.fire(EventKey.ActInstWorldBossKiller, "humanObj", humanObj);
	}
	
	/**
	 * 更换城主
	 */
	private void gmupdateDuke(HumanObject humanObj){
		MaincityServiceProxy prx  = MaincityServiceProxy.newInstance();
		prx.updateDuke();
		prx.listenResult(this::_result_changeCastellan, "humanObj", humanObj);
	}
	/**
	 * 玩家检查自己是否成为城主
	 */
	private void _result_changeCastellan(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		Map<Integer,Castellan> CastellanMap = results.get("castellanMap");
		for (Castellan castellan : CastellanMap.values()) {
			// 不是城主
			if (humanObj.getHumanId() != castellan.getHumanId()) {
				continue;
			}
			// 根据isNotice来提示
			if (castellan.isIsNotice()) {
				continue;
			}
			castellan.setIsNotice(true);
			// 发送通知，成为城主
			Event.fire(EventKey.MainCityCastellan, "humanObj", humanObj, "castellanType", castellan.getType());
		}
	}
	/**
	 * 测试客户端调试相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_debug(HumanObject humanObj, String[] order) {
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "debug" :// 是否开启了调试：1开启，0关闭
				if (order.length >= 1) {
					humanObj.getHuman().setClientOpenSRDebug((order[1].equals("1")) ? true : false);
					// 发送客户端调试状态
					HumanManager.inst().sendSCDebugClient(humanObj);
				}	break;
			case "debuglog" :// 调试日志级别：0-ERROR，1-Assert，2-Warning，3-Log，4-Exception
				if (order.length >= 1) { 
					humanObj.getHuman().setClientLogType(Utils.intValue(order[1]));
					// 发送客户端调试状态
					HumanManager.inst().sendSCDebugClient(humanObj);
				}	break;
			default:
				break;
		}
	}
	
	/**
	 * 开启战斗无敌
	 * @param humanObj
	 * @param order
	 */
	private void gm_daddy(HumanObject humanObj, String[] order) {
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
		case "daddy" :// 是否开启无敌：1开启，0关闭
			if (order.length >= 2) {
				humanObj.daddyFight[0] = Utils.intValue(order[1]);
				if (order.length >= 4) {
					humanObj.daddyFight[1] = Utils.intValue(order[2]);
					humanObj.daddyFight[2] = Utils.intValue(order[3]);
				}
			}	break;
		default:
			break;
		}	
	}
	
	/**
	 * 测试
	 * @param humanObj
	 * @param order
	 */
	private void gm_test(HumanObject humanObj, String[] order) {
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "testshield" :// 屏蔽字测试
				gm_testShield(humanObj, order);
				break;
			case "testcounttime" :// 获取体力恢复剩余时间
				gm_testCountTime(humanObj);
				break;
			case "testaccountbind":
				gm_testAccountBind(humanObj, order);
				break;
			case "testinform": {
				gm_testinform(humanObj, order);
			}	break;
			case "testrest":{
				gm_rest(humanObj);
			} break;
			case "testtime":
				gm_testtime(humanObj,order);//gmtesttime 2017-7-29 00:00:00
				break;
			case "testguild":
				gm_testguild(humanObj,order);
				break;
            case "testguildskill":
                gm_testguildskill(humanObj, order);
				break;
            case "testguildinst":
                gm_testguildinst(humanObj, order);
                break;
            case "testguildinstharm":
                gm_testguildinstharm(humanObj, order);
                break;
            case "testguildinstchapter":
                gm_testguildinstchapter(humanObj, order);
                break;
            case "testguildinststage":
                gm_testguildinststage(humanObj, order);
                break;
            case "testguildinstbuy":
                gm_testguildinstbuy(humanObj, order);
                break;
			case "testprop":
				gm_testprop(humanObj);
				break;
			case "testwb":
				gm_testwb(humanObj);
		default:
			break;
		}
	}
	
	private void gm_testwb(HumanObject humanObj) {
		InstWorldBossManager.inst().giveRankAward(humanObj.getHumanId(), 310003, 1);
	}
	
	private void gm_testprop(HumanObject humanObj) {
		String content = "";
		Map<Long, PartnerObject> pmap = humanObj.partnerMap;
		for (PartnerObject po : pmap.values()) {
			Partner p = po.getPartner();
			content += "\n伙伴：" + p.getName();
			content += "\n生命 = " + p.getHpMax();
			content += "\n攻击 = " + p.getAtk();
			content += "\n物防 = " + p.getDefPhy();
			content += "\n法防 = " + p.getDefMag();
			content += "\n";
		}
		Human human = humanObj.getHuman();
		content += "\n主角：" + human.getName();
		content += "\n生命 = " + human.getHpMax();
		content += "\n攻击 = " + human.getAtk();
		content += "\n物防 = " + human.getDefPhy();
		content += "\n法防 = " + human.getDefMag();
		content += "\n";
		InformManager.inst().sendSCInformMsg(humanObj, EInformType.WorldInform, content, humanObj.getHumanId(), "");
	}

	private void gm_testguild(HumanObject humanObj ,String [] timeStr){
		GuildManager.inst()._msg_CSGuildInfo(humanObj, 1);
	}

    private void gm_testguildskill(HumanObject humanObj ,String [] order){
	    GuildManager.inst()._msg_CSGuildSkillList(humanObj);
        int sn = Utils.intValue(order[1]);
        GuildManager.inst()._msg_CSGuildSkillUpgrade(humanObj, sn);
    }

    private void gm_testguildinst(HumanObject humanObj ,String [] order){
        GuildInstManager.inst()._msg_CSGuildInstInfo(humanObj);
        int stage = Utils.intValue(order[1]);
        GuildInstManager.inst()._msg_CSGuildInstChallenge(humanObj, stage);
    }
    private void gm_testguildinstharm(HumanObject humanObj ,String [] order){
        int stage = Utils.intValue(order[1]);
        long harmTotal = 0;
        List<Integer> harmList = new ArrayList<>();
        for (int i=0; i<10; i++) {
            int harm = (i+1)*100000;
            harmList.add(harm);
            harmTotal += harm;
        }
        boolean leave = stage == 0;
        GuildInstManager.inst()._msg_CSGuildInstHarm(humanObj, leave, stage, harmList, harmTotal);
    }
    private void gm_testguildinstchapter(HumanObject humanObj ,String [] order){
        int chapter = Utils.intValue(order[1]);
        GuildInstManager.inst()._msg_CSGuildInstChapterReward(humanObj, chapter);
    }

    private void gm_testguildinststage(HumanObject humanObj ,String [] order){
        int stage = Utils.intValue(order[1]);
        int slot = Utils.intValue(order[2]);
        GuildInstManager.inst()._msg_CSGuildInstStageReward(humanObj, stage, slot);
        GuildInstManager.inst()._msg_CSGuildInstStageRewardInfo(humanObj, stage);
    }

    private void gm_testguildinstbuy(HumanObject humanObj ,String [] order){
        GuildInstManager.inst()._msg_CSGuildInstBuyChallengeTimes(humanObj);
    }
	/**
	 * 改变系统时间,仅在win上可使用
	 */
	private void gm_testtime(HumanObject humanObj ,String [] timeStr){
		RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, Utils.intValue(timeStr[1]),LogSysModType.GmTest);
	}
	
	/**
	 * 每日重置测试
	 * @param humanObj
	 */
	public void gm_rest(HumanObject humanObj){
//		int num =PartnerManager.inst().hasNumQuality(humanObj, 3);
//		System.out.println("num:"+num);
//		HumanManager.inst().resetDaily(humanObj);
	}
	/**
	 * 副本相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_inst(HumanObject humanObj, String[] order){
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "instpass" :// 通关指定副本：副本sn 星级(1-3)
				gm_instPass(humanObj, order);
				break;
			case "instpassany" : // 通过基于当前关卡的副本关数 ：星级(1-3) 通过数量
				gm_instPassAny(humanObj, order); 
				break;
			case "instpassall" :// 通关所有副本：星级(1-3)
				gm_instPassAll(humanObj, order);
				break;
			case "instsetstar" :// 设置星数：1011 3 关卡 星数
				gm_instSetStar(humanObj, Utils.intValue(order[1]), Utils.intValue(order[2]));
				break;
			case "instsetsumstar" ://设置章节总星星数： 章节id 星数 0普通 1精英
				gm_instSetSumStar(humanObj, order);
				break;
			case "instsetbox" ://设置可领取宝箱位置： 章节id 宝箱位置 0普通 1 精英
				gm_instSetBox(humanObj, order);
				break;
			case "instbuynum" :// 章节副本对应花钱次数：副本sn 次数
				gm_instBuyNum(humanObj, order);
				break;
			default:
				break;
		}
	}
	
	/**
	 * 爬塔相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_tower(HumanObject humanObj, String[] order) {
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {
			case "towerloaddata" :// 爬塔数据重置
				gm_towerloaddata();
				break;
			case "towermatch" : //重新匹配
				gm_towermatch(humanObj);
				break;
			default:
				break;
		}
	}
	
	/**
	 * 武将相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_gen(HumanObject humanObj, String[] order){
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		switch (gmOrder) {

		default:
			break;
		}
	}
	
	/**
	 * 活动相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_act(HumanObject humanObj, String[] order){
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母

		switch (gmOrder) {
		case "actci" :// commit
			int actId = Utils.intValue(order[1]);
			int aid = Utils.intValue(order[2]);
			List<DActivityParam> activityParamsList = new ArrayList<>();
			DActivityParam.Builder dp = DActivityParam.newBuilder();
			dp.addNumParam(aid);
			activityParamsList.add(dp.build());
			
			ActivityManager.inst().commitActivity(humanObj, actId, activityParamsList);
			break;
		default:
			break;
	}
		
//		Log.activity.error("发放活动奖励 2017.12.12");
		
//		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
//		proxy.actServerCompetitionPlanAbort();
//		Log.activity.error("发放活动奖励 2017.12.12 end");
//		
//		switch (gmOrder) {
//			
//		default:
//			break;
//		}
	}
	
	/**
	 * vip相关功能
	 * @param humanObj
	 * @param order
	 */
	private void gm_vip(HumanObject humanObj, String[] order){
		
		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
		
		
//		String gmOrder = order[0].toLowerCase();// 将所有大写全转换成小写字母
//		switch (gmOrder) {
//			
//			
//		default:
//			break;
//		}
		
		
		switch (gmOrder) {
			case "vipsetlv" :// 设置等级
				int vipLv = Utils.intValue(order[1]);
				if (vipLv > ParamManager.maxVipLv) {
					vipLv = ParamManager.maxVipLv;
				}
				// 先判断是不是最大级别
				ConfVipUpgrade confMax = ConfVipUpgrade.get(vipLv);
				if (confMax == null) {
					Log.table.error("===ConfVipUpgrade no find sn={}", vipLv);
					return;
				}
				PaymentManager.inst().vipLevelGM(humanObj,  confMax.amount);

				break;
			case "viponpay":
				PaymentManager.inst()._gm_PayCharge(humanObj, Utils.intValue(order[1]));
				break;
			default:
				break;
		}
	}
			
	/**
	 * 发临时公告
	 * @param humanObj
	 * @param order
	 */
	private void gm_humanNotice(HumanObject humanObj, String[] order){
		if (order.length == 2) {
			String noticeStr = order[1];// 要公告的话
			noticeStr = 1+"|"+noticeStr;// 1|这个是临时公告
			InformManager.inst().sendSCInformMsg(humanObj, EInformType.GMInform, noticeStr);
		}
	}
	
	/**
	 * 获取人物各部位装备
	 * @param humanObj
	 */
	private void gm_itemHumanEquip(HumanObject humanObj){
		// 判断背包空间是否足够
		int remaind = ItemBagManager.inst().getNumRest(humanObj);
		if (remaind<8) {
			return;
		}
		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
		//各部位装备
		itemProduce.add(new ProduceVo(301001, 1));
		itemProduce.add(new ProduceVo(302001, 1));
		itemProduce.add(new ProduceVo(303001, 1));
		itemProduce.add(new ProduceVo(304001, 1));
		itemProduce.add(new ProduceVo(305001, 1));
		itemProduce.add(new ProduceVo(306001, 1));
		itemProduce.add(new ProduceVo(307001, 1));
		itemProduce.add(new ProduceVo(308001, 1));
		//进阶材料
	//	itemProduce.add(new ProduceVo(20001, 500));	
	//	itemProduce.add(new ProduceVo(20002, 500));	
	//	itemProduce.add(new ProduceVo(20003, 500));	
	//	itemProduce.add(new ProduceVo(20004, 500));	
	//	itemProduce.add(new ProduceVo(20005, 500));	
	//	itemProduce.add(new ProduceVo(20006, 500));	
	//	itemProduce.add(new ProduceVo(20007, 500));	
	//	itemProduce.add(new ProduceVo(20008, 500));	
	//	itemProduce.add(new ProduceVo(20009, 500));	
	//	itemProduce.add(new ProduceVo(20010, 500));	
	//	itemProduce.add(new ProduceVo(20011, 2000));	
	//	itemProduce.add(new ProduceVo(20012, 2000));	
		
		ProduceManager.inst().giveProduceItem(humanObj, itemProduce, LogSysModType.GmTest);
	}

	/**
	 * 测试屏蔽字
	 * @param humanObj
	 * @param order
	 */
	private void gm_testShield(HumanObject humanObj, String[] order) {
		if (order.length >= 2) {
			// 检查名字是否有屏蔽字，如果有屏蔽字返回
			String shieldStr = order[1];
			String fix = SensitiveWordFilter.getInstance().getSensitiveWord(shieldStr.toLowerCase());
			if (fix != null) {
				Log.game.info("gm命令执行完毕，有屏蔽字：gm_shield() fix={}, shieldStr={}", fix, shieldStr);
			} else {
				Log.game.info("gm命令执行完毕，无屏蔽字：gm_shield() fix is null, shieldStr={}", shieldStr);
			}
		}
	}

	/**
	 * 测试绑定帐号
	 * @param humanObj
	 * @param order
	 */
	private void gm_testAccountBind(HumanObject humanObj, String[] order) {
		if (order.length >= 4) {
			int type = Utils.intValue(order[1]);
			ELoginType loginType = null;// 登录类型
			if(ELoginType.PC_VALUE == type){
				loginType = ELoginType.PC;//pc登录
			}else if(ELoginType.MI_VALUE == type){
				loginType = ELoginType.MI;//小米平台登录
			}else if(ELoginType.FACEBOOK_VALUE == type){
				loginType = ELoginType.FACEBOOK;//FACEBOOK平台登录
			}
			if(null == loginType){
				Log.game.error("GMManager.gm_testAccountBind loginType=null");
				return;
			}
			// 平台账号登录需要的参数
			String account = order[2];// 账号，即用户ID
			String password = order[3];// 密码，即访问口令（提供给服务器验证用的）
			HumanManager.inst().bindAccount(humanObj, loginType, account, password);
		}
	}
	
	/**
	 * 测试通告
	 * @param humanObj
	 * @param order
	 */
	private void gm_testinform(HumanObject humanObj, String[] order) {
		if (order.length >= 2) {
			int num = 1;
			if (order.length >= 2) {
				num = Utils.intValue(order[1]);
			}
			// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
			String content = Utils.createStr("{}|{}|{}|{}", ParamManager.sysMsgMark, 999008, 
					humanObj.getHuman().getName(), 9);
			InformManager.inst().sendNotify(EInformType.SystemInform, content, num);
//			InformManager.inst().sendSCInformMsg(humanObj, EInformType.valueOf(order[1]), order[2], num,null);
		}
	}
	
	/**
	 * 创建指定副本的队伍
	 * @param humanObj
	 * @param order
	 */
	private void gm_teamCreate(HumanObject humanObj, String[] order) {
		if (order.length >= 2) {
			int actInstSn = Utils.intValue(order[1]);
			TeamManager.inst()._msg_CSTeamCreate(humanObj, actInstSn);
		}
	}
	
	/**
	 * 加入组队副本
	 * @param humanObj
	 * @param order
	 */
	private void gm_teamJoin(HumanObject humanObj, String[] order) {
		if (order.length >= 2) {
			int actInstSn = Utils.intValue(order[1]);
			TeamManager.inst()._msg_CSTeamJoin(humanObj, actInstSn);
		}
	}

	/**
	 * 进入组队副本
	 * @param humanObj
	 * @param order
	 */
	private void gm_teamEnterRep(HumanObject humanObj, String[] order) {
		TeamManager.inst()._msg_CSTeamEnterRep(humanObj);
	}

	/**
	 * 重新加载表格数据：gmHumanReloadConf
	 */
	private void gm_humanReloadConf(HumanObject humanObj, String[] order) {
		DataReloadManager.inst().reloadConf();// add by shenjh,重新加载所有JSON表格数据
	}

	/**
	 * 传送地图：/gomap 1即mapSn
	 */
	private void gm_humanGoMap(HumanObject humanObj, String[] order) {
		if (order.length >= 2) {
			int mapSn = Utils.intValue(order[1]);
			StageManager.inst()._msg_CSStageSwitch(humanObj, mapSn, 0);
		}
	}

	/**
	 * 增加活跃度
	 */
	private void gm_questLiveness(HumanObject humanObj, String[] order) {
		if (order.length >= 2) {
			Human human = humanObj.getHuman();
			human.setDailyQuestLiveness(human.getDailyQuestLiveness() + Utils.intValue(order[1]));// 增加活跃度
			human.setWeeklyQuestLiveness(human.getWeeklyQuestLiveness() + Utils.intValue(order[1]));
			QuestDailyManager.inst()._send_SCLivenessInfoChange(humanObj);//发送活跃度变化
		}
	}

	/**
	 * 购买体力
	 * @param humanObj
	 */
	private void gm_humanBuyAct(HumanObject humanObj) {
		HumanManager.inst()._msg_CSDailyActBuy(humanObj);
	}

	/**
	 * 查看等级排行
	 * @param humanObj
	 */
	private void gm_rankLevel(HumanObject humanObj) {
		RankManager.inst()._msg_CSLevelRank(humanObj);
	}

	/**
	 * 适用物品
	 * @param humanObj
	 * @param order
	 */
	private void gm_itemUse(HumanObject humanObj, String[] order) {
		if (order.length >= 3) {
			ItemBagManager.inst().itemUse(humanObj, Utils.longValue(order[1]), Utils.intValue(order[2]),
					LogSysModType.GmTest);
		}
	}

	/**
	 * 添加货币
	 * @param humanObj
	 */
	private void gm_humanMoney(HumanObject humanObj, String[] order) {
		if (order.length >= 3) {
			if (Utils.intValue(order[2]) > 0) {
				RewardHelper.reward(humanObj, Utils.intValue(order[1]), Utils.intValue(order[2]),
						LogSysModType.GmTest);
			} else {
				// 扣除
				RewardHelper.checkAndConsume(humanObj, Utils.intValue(order[1]), Math.abs(Utils.intValue(order[2])),
						LogSysModType.GmTest);
			}
		}
	}

	/**
	 * 新的强化
	 * @param humanObj
	 */
	private void gm_itemEquip(HumanObject humanObj, String[] order) {
		if (order.length > 3) {
			long id = Utils.longValue(order[1]);
			int type = Utils.intValue(order[2]);
			ItemBodyManager.inst()._msg_CSReinforceEquipMsg(humanObj, id);
		}
	}

	/**
	 * 添加物品
	 * @param humanObj
	 */
	private void gm_itemAdd(HumanObject humanObj, String[] order) {
		if (order.length >= 3) {
			int itemSn = Utils.intValue(order[1]);
			int itemNum = Utils.intValue(order[2]);
			if (itemNum < 1) {
				itemNum = 1;// 最小一个
			}
			ConfItem conf = ConfItem.get(itemSn);
			if (conf == null) {
				Log.table.error("ConfItem  配表错误，no find sn={}", itemSn);
				return;
			}
			// 货币类型判断
			if (itemSn > EMoneyType.minMoney_VALUE && itemSn < EMoneyType.maxMoney_VALUE) { // 货币类型
				RewardHelper.reward(humanObj, itemSn, itemNum, LogSysModType.GmTest);
			} else {
				// 判断背包空间是否足够
				boolean ret = ItemBagManager.inst().canAdd(humanObj, itemSn, itemNum);
				if (!ret) {
					return;
				}
				List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
				ProduceVo produceVo = new ProduceVo(itemSn, itemNum);
				itemProduce.add(produceVo);
				ProduceManager.inst().giveProduceItem(humanObj, itemProduce, LogSysModType.GmTest);
			}
		}
	}

	/**
	 * 添加掉落包
	 * @param humanObj
	 */
	private void gm_itemBagAdd(HumanObject humanObj, String[] order) {
		if (order.length >= 2) {
			ProduceManager.inst().getAndGiveProduce(humanObj, Utils.intValue(order[1]), LogSysModType.GmTest);
		}
	}
	
	/**
	 * 接受指定任务：/questaccept sn
	 */
	private void gm_questaccept(HumanObject humanObj,String[] order){
		if (order.length == 2) {
			int sn = Utils.intValue(order[1]);
			//TODO 接受任务
		}
	}
	/**
	 * 完成指定任务：/questfinish sn
	 */
	private void gm_questfinish(HumanObject humanObj, String[] order) {
		if (order.length < 1) {
			return;
		}
		int snQuest = Utils.intValue(order[1]);
		QuestJSON questJSON = humanObj.questRecord.getNormalBy(snQuest);
		if (questJSON != null) {
			questJSON.status = EQuestDailyStatus.Completed_VALUE;
			humanObj.questRecord.modifyNormal(questJSON);
		}
	}
	/**
	 * 修改指定任务状态：/queststatusmodify sn,2
	 */
	private void gm_queststatusmodify(HumanObject humanObj,String[] order){
		if (order.length >= 3) {
			int snQuest = Utils.intValue(order[1]);
			int status = Utils.intValue(order[2]);
			QuestJSON questJSON = humanObj.questRecord.getNormalBy(snQuest);
			if (questJSON != null) {
				questJSON.status = status;
				humanObj.questRecord.modifyNormal(questJSON);
			}
		}
	}
		
	/**
	 * 开启所有功能
	 * @param humanObj
	 */
	private void gm_humanModunlock(HumanObject humanObj) {
		JSONObject modUnlock = new JSONObject();
		List<Integer> snList = new ArrayList<Integer>();
		for (ConfModUnlock conf : ConfModUnlock.findBy(ConfModUnlock.K.sn,OrderBy.ASC)) {// 升序
			if (conf == null || conf.sn == 0 || conf.type[0] == 1) {// 空或功能是根据等级解锁的跳过
				continue;
			}
			String sn = String.valueOf(conf.sn);
			snList.add(conf.sn);// 开启功能模块后加入
			modUnlock.put(sn, 1);// 此功能开启
		}
		if (!snList.isEmpty()) {
			humanObj.getHuman().setModUnlock(modUnlock.toJSONString());// 保存数据
			humanObj.getHuman().update(true);// 立即保存数据库
			ModunlockManager.inst().sendSCModUnlockMsg(humanObj, snList);// 发送消息
		}
	}

	/**
	 * 设置等级
	 * @param humanObj
	 */
	private void gm_humanSetLv(HumanObject humanObj, String[] order) {
		if (order.length < 2) {
			return;
		}
		// GM设定等级
		int setLv = Utils.intValue(order[1]); 
		int curLv = humanObj.getHuman().getLevel();
		// 设定等级 <= 当前等级 ，则默认升一级
		if (setLv <= curLv) {
			setLv = curLv + 1;
		}
		if (setLv > ParamManager.maxHumanLevel) {
			setLv = ParamManager.maxHumanLevel;
		} 
		
		ConfLevelExp curConf = ConfLevelExp.get(curLv);
		// 当前拥有的总经验
		long curExp = curConf.roleExp + humanObj.getHuman().getExp();
		
		// 设置等级的配置表
		ConfLevelExp conf = ConfLevelExp.get(setLv);
		if (conf == null) {
			Log.table.error("ConfLevelExp配表错误，no find sn={}", setLv);
			return;
		}
		// 增加经验值
		int addExp = conf.roleExp - (int) curExp;
		RewardHelper.reward(humanObj, EMoneyType.exp_VALUE, addExp, LogSysModType.GmTest);
	}
	
	/*
	 * 等级改变后，改变 满体时间 和 满体体力
	 */
	private void setActValue(HumanObject humanObj, long actOld, int actOldMax, int lvOld, int lvAdd) {
		ConfLevelExp confLAV = ConfLevelExp.get(lvAdd);
		if (confLAV == null) {
			Log.table.error("ConfLevelExp配表错误，no find sn={} ", 1);
			return;
		}
		long chargingTimes = humanObj.getHuman().getActFullTime();
		int actNewMax = confLAV.staminaMax;
		humanObj.getHuman().setActMax(actNewMax);
		if (actOld < actOldMax) {// 定时器一定是开着的
			if (actOld >= actNewMax) {
				humanObj.getHuman().setActFullTime(Port.getTime());
				if (humanObj.ttActValue.isStarted()) {
					humanObj.ttActValue.stop();
				}
			} else {
				if (actOldMax < actNewMax) {
					humanObj.getHuman().setActFullTime(
							chargingTimes + (actNewMax - actOldMax) * ParamManager.cdHumanAct * Time.MIN);// 体力恢复慢点所需时间：系统时间+体力点数*6分钟
				} else {
					humanObj.getHuman().setActFullTime(
							chargingTimes - (actOldMax - actNewMax) * ParamManager.cdHumanAct * Time.MIN);// 体力恢复慢点所需时间：系统时间-体力点数*6分钟

				}
			}
		} else {// actOld>=actOldMax
			if (actOldMax >= actNewMax) {
				// 倒计时 不开启，体力 超过 满体体力
			} else {
				if (actOld >= actNewMax) {
					// 倒计时 不开启，体力 超过 满体体力 ---不增加因为提升等级而带来的体力增加
				} else {
					// 扣除体力时，开始计时器
					if (!humanObj.ttActValue.isStarted()) {
						humanObj.ttActValue.start(Port.getTime(), ParamManager.cdHumanAct * Time.MIN);
						humanObj.getHuman().setActFullTime(
								Port.getTime() + (actNewMax - actOldMax) * ParamManager.cdHumanAct * Time.MIN);// 体力恢复慢点所需时间：系统时间+体力点数*6分钟
					}
				}
			}

		}
		HumanManager.inst().sendSCActValueFullTimeChange(humanObj);//发送体力时间变化
	}
	
	private void instPass(HumanObject humanObj, int stageSn, int instStar) {
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
		
		// 非活动副本中的单人副本才需要修改数据库的副本信息记录
		Instance inst = humanObj.instancesMap.get(confInstStage.chapterSN);
		if (inst == null) {
			inst = InstanceManager.inst().newInstance(humanObj, confInstStage.chapterSN);// 添加单人副本内容到数据库
		}
		// 修改副本已挑战次数
		
		// 手动通关处理通关副本星级
		JSONObject joStar = Utils.toJSONObject(inst.getStarJSON());
		int starOld = joStar.getIntValue(stageSnStr);
		int starNew = instStar;
		int starAll = inst.getStarAll();
		// 修改并保存副本星数及总星数
		if (starOld < starNew) {
			joStar.put(stageSnStr, starNew);
			inst.setStarJSON(joStar.toJSONString());
			starAll += (starNew - starOld);
			inst.setStarAll(starAll);// 总星数
		} else {
			joStar.put(stageSnStr, starNew);
			inst.setStarJSON(joStar.toJSONString());
			starAll -= (starOld - starNew);
			inst.setStarAll(starAll);// 总星数
		}
		// 修改并保存章节宝箱领取状态：0不能领，1可领取，2已领取
		JSONObject joBox = Utils.toJSONObject(inst.getBoxJSON());
		for (int star : confInstChapter.chapterStar) {
			String starStr = String.valueOf(star);
			int status = joBox.getIntValue(starStr);
			if (status == EAwardType.AwardNot_VALUE && starAll >= star) {
				joBox.put(starStr, EAwardType.Awarding_VALUE);
			}
		}
		inst.setBoxJSON(joBox.toJSONString());
		
		//发布副本首次通关事件
		Event.fire(EventKey.InstFirstPass, "humanObj", humanObj, "num", 1, "stageSn", stageSn);
		//发布通关副本事件
		Event.fire(EventKey.InstAnyPass, "humanObj", humanObj, "num", 1, "stageSn", stageSn);
	}
	/**
	 * 通关指定副本：星级(1-3) 副本sn
	 */
	private void gm_instPass(HumanObject humanObj, String[] order) {
		if (order.length < 3)
			return;
	
		int instStar = Utils.intValue(order[1]);
		int stageSn = Utils.intValue(order[2]);
		// 通关副本
		instPass(humanObj, stageSn, instStar);
				
		// 下发所有副本章节信息
		InstanceManager.inst()._send_SCInstInfoAll(humanObj);
	}
	
	/**
	 * 通过基于当前关卡的副本关数 ：星级(1-3) 通过数量
	 */
	private void gm_instPassAny(HumanObject humanObj, String[] order) {
		if (order.length < 3) {
			return;
		}
		int instStar = Utils.intValue(order[1]);
		// 通过关卡数
		int passNum = Utils.intValue(order[2]);
		if (passNum < 1) {
			passNum = 1;// 最小一个
		}
		int count = 0;
		List<ConfInstStage> listInstStage = ConfInstStage.findBy(ConfInstStage.K.sn, OrderBy.ASC);
		for (ConfInstStage conf : listInstStage) {
			if (count >= passNum) {
				break;
			}
			if (conf.chapterSN <= 0) {
				continue;
			}
			int stageSn = conf.sn;
			// 已经过关的继续
			if (InstanceManager.inst().isPassRepStage(humanObj, stageSn)) {
				continue;
			}
			
			// 通关副本
			instPass(humanObj, stageSn, instStar);
			count ++;
		}
		// 下发所有副本章节信息
		InstanceManager.inst()._send_SCInstInfoAll(humanObj);
	}
	
	/**
	 * 通关所有副本：星级(1-3)
	 * @param humanObj
	 */
	private void gm_instPassAll(HumanObject humanObj, String[] order) {
		if (order.length < 2)
			return;
		
		int instStar = Utils.intValue(order[1]);
		List<ConfInstStage> listInstStage = ConfInstStage.findBy(ConfInstStage.K.sn, OrderBy.ASC);
		for (ConfInstStage conf : listInstStage) {
			int stageSn = conf.sn;
			int chapSn = conf.chapterSN;
			if (chapSn <= 0) {
				continue;
			}
			// 已经过关的继续
			if (InstanceManager.inst().isPassRepStage(humanObj, stageSn)) {
				continue;
			}
//			if (stageSn == 102508 || stageSn == 202404) {
//				continue;
//			}
			// 通关副本
			instPass(humanObj, stageSn, instStar);
		}
		// 下发所有副本章节信息
		InstanceManager.inst()._send_SCInstInfoAll(humanObj);
	}
	
	/**
	 * 设置星数
	 * @param humanObj
	 * @param stageSn
	 * @param star
	 */
	private void gm_instSetStar(HumanObject humanObj, int stageSn, int star) {
		ConfInstStage conf = ConfInstStage.get(stageSn);
		if (conf == null) {
			Log.table.error("ConfInstStage配表错误，no find sn={} ", stageSn);
			return;
		}
		Instance inst = humanObj.instancesMap.get(conf.chapterSN);
		if (inst == null) {
			inst = new Instance();
			inst.setId(Port.applyId());
			inst.setHumanId(humanObj.id);
			inst.setChapterSn(conf.chapterSN);
			inst.persist();
			humanObj.instancesMap.put(conf.chapterSN, inst);
		}

		if (star < 1) {
			star = 1;
		} else if (star > 3) {
			star = 3;
		}
		String sn = String.valueOf(stageSn);
		JSONObject joStar = Utils.toJSONObject(inst.getStarJSON());
		joStar.put(sn, star);
		int starAll = 0;
		for (Object val : joStar.values()) {
			starAll += (Integer)val;
		}
		inst.setStarAll(starAll);// 修改总星数
		inst.setStarJSON(joStar.toJSONString());// 修改副本星数

		// 下发当前章节的副本全部信息
		InstanceManager.inst()._send_SCInstInfoAll(humanObj);
	}

	/**
	 * 设置星星数
	 * @param humanObj
	 */
	private void gm_instSetSumStar(HumanObject humanObj, String[] order) {
		if (order.length >= 4) {
			int snChapter = Utils.intValue(order[1]);
			int num = Utils.intValue(order[2]);
			int res = Utils.intValue(order[3]);
			ConfInstStage conf = ConfInstStage.get(snChapter);
			if (conf == null) {
				Log.table.error("ConfInstStage配表错误，no find sn={} ", snChapter);
				return;
			}
			Instance ins = humanObj.instancesMap.get(conf.chapterSN);
			if (ins == null) {
				Log.human.info("humanObj.dataPers.instances配表错误，no find chapterSN={}", conf.chapterSN);
				return;
			}
			switch (res) {
				case 0 :// 普通副本
				{
					num += ins.getStarAll();
					if (num < 0) {
						num = 0;
					}
					ins.setStarAll(num);
					break;
				}
				default :
					break;
			}
			//InstanceManager.inst()._msg_CSInstanceAll(humanObj, snChapter);
		}

	}

	/**
	 * 设置可领取宝箱位置 章节id 宝箱位置 0普通 1 精英
	 * @param humanObj
	 */
	private void gm_instSetBox(HumanObject humanObj, String[] order) {
		if (order.length >= 4) {
			int snChapter = Utils.intValue(order[1]);
			int index = Utils.intValue(order[2]);
			int res = Utils.intValue(order[3]);
			Instance ins = humanObj.instancesMap.get(snChapter);
			if (ins == null) {
				return;
			}
			if (index < 0) {
				index = 0;
			}
			switch (res) {
				case 0 : {// 普通副本
					//ins.setBoxStatus(index);
				}	break;
				default :
					break;
			}
			//InstanceManager.inst()._msg_CSInstanceAll(humanObj, snChapter);
		}

	}

	/**
	 * 设置章节副本对应花钱次数
	 * @param humanObj
	 */
	private void gm_instBuyNum(HumanObject humanObj, String[] order) {
		if (order.length >= 3) {
			int snChapter = Utils.intValue(order[1]);
			int index = Utils.intValue(order[2]);
			ConfInstStage stage = ConfInstStage.get(snChapter);
			if (stage == null) {
				Log.table.error("ConfInstStage配表错误，no find sn={} ", snChapter);
				return;
			}
			Instance inst = humanObj.instancesMap.get(stage.chapterSN);
			if (inst == null) {
				Log.human.info("humanObj.dataPers.instances配表错误，no find chapterSN={}", stage.chapterSN);
				return;
			}
			if (index < 0) {
				index = 0;
			}
			// 今天已经购买的次数
			inst.setResetJSON(Utils.addJSONValue(inst.getResetJSON(), snChapter, index));
			// 下发副本全部信息
			InstanceManager.inst()._send_SCInstInfoAll(humanObj);
		}
	}
	
	/**
	 * 爬塔数据从竞技场加载
	 */
	private void gm_towerloaddata() {
		TowerServiceProxy proxy = TowerServiceProxy.newInstance();
		proxy.firstLoadTowerData();
	}
	/**
	 * 爬塔重新匹配
	 */
	private void gm_towermatch(HumanObject humanObj) {
		if (!ModunlockManager.inst().isUnlock(EModeType.ModeTower_VALUE, humanObj)) {
			return;
		}
		TowerManager.inst()._gm_towerMatch(humanObj);
	}
	
	/**
	 * 重置体力
	 * @param humanObj
	 */
	private void gm_humanBuyActClear(HumanObject humanObj) {
		humanObj.getHuman().setDailyActBuyNum(0);
	}

	/**
	 * 给所有在线玩家发送邮件
	 * @param order
	 */
	private void gm_mailOnline(String[] order) {
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		if (prx != null) {
			prx.sendMailOnline(order);
		}
	}

	/**
	 * 给所有玩家发送邮件
	 * @param order
	 */
	private void gm_mailAll(String[] order) {
		MailManager.inst().sendMailAll(order);
	}

	/**
	 * 查看战力排行
	 * @param humanObj
	 */
	private void gm_rankCombat(HumanObject humanObj) {
//		RankManager.inst()._msg_CSCombatRank(humanObj);
	}

	/**
	 * 清空背包
	 * @param humanObj
	 */
	private void gm_itemBagClear(HumanObject humanObj) {
		ItemBagManager.inst().itemBagClear(humanObj);
	}

	/**
	 * 发送特殊邮件
	 * @param humanObj
	 * @param order
	 */
	private void gm_mailMarkSend(HumanObject humanObj, String[] order) {
		String title = ParamManager.mailMark;
		if (title.isEmpty()) {// 如果找不到特殊邮件的标志，说明配表有问题
			return;
		}

		String temp = "";
		for (int i = 1; i < order.length; i++) {
			if (i == 1) {
				temp = order[i];
				continue;
			}
			temp = temp + "|" + order[i];
		}
		
		// 特殊邮件内容：{MailTemplate.sn|参数1}
		String detail = "{" + temp + "}";
		// 发送邮件到玩家
		MailManager.inst().sendSysMail(humanObj.getHumanId(), title, detail, null);
	}

	/**
	 * 一键获得金币、钻石、等级、vip、法宝
	 * @param humanObj
	 */
	private void gm_humanOnekey(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		RewardHelper.reward(humanObj, EMoneyType.coin_VALUE, 1000000000, LogSysModType.GmTest);// 添加金币
		RewardHelper.reward(humanObj, EMoneyType.gold_VALUE, 1000000000, LogSysModType.GmTest);// 添加钻石

		ConfLevelExp confLV = ConfLevelExp.findBy(ConfLevelExp.K.sn,OrderBy.DESC).get(0);
		if (confLV != null && confLV.sn != 0) {
			int level = confLV.sn;
			human.setLevel(level);
			HumanManager.inst().sendSCLevelChange(humanObj);//等级变化
			Event.fire(EventKey.HumanLvUp, "humanObj", humanObj);
		}
		
		gm_humanModunlock(humanObj);//解开所有功能模块
	}

	/**
	 * 激活所有未激活的技能（普通技能除外）
	 * @param humanObj
	 */
	private void gm_humanAllSkill(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		boolean ret = false;
		List<Integer> skills = Utils.strToIntList(human.getSkillAllSn());
		for (int i = 0; i < skills.size(); i++) {
			int skillSn = Utils.intValue(skills.get(i));
			int level = 0;//SkillManager.inst().getSkillLv(skillSn);
			if (level == 0) {
				ConfSkill confSkill = ConfSkill.get(skillSn);
				if (confSkill == null) {
					continue;
				}
				// // FIXME GM技能激活
//				if (confSkill.type != SkillType.Attack.value()) {
//					ret = true;
//					int newSkillSn = skillSn + 1;
//					SkillManager.inst().replaceSkill(humanObj, newSkillSn, skillSn);// 替换技能
//					SkillManager.inst().sendMsgSCSkillUpgrade(humanObj, newSkillSn);// 返回技能信息
//				}
			}
		}
		if (ret) {
			// 发送事件
			Event.fire(EventKey.GenSkillUp, "humanObj", humanObj);
		}
	}

	/**
	 * 重置排行榜膜拜次数
	 */
	private void gm_rankWorshipClear(HumanObject humanObj) {
		// 重置排行榜膜拜次数
		humanObj.getHuman().setDailyRankWorship("{}");
	}

	/**
	 * 删除自身的排行榜信息
	 */
	private void gm_rankselfdelete(HumanObject humanObj) {
//		RankManager.inst().deleteHumanRankInfo(humanObj.id);
	}

	/**
	 * 排行榜更新排行
	 */

	private void gm_rankUpdate() {
		RankGlobalServiceProxy pxy = RankGlobalServiceProxy.newInstance();
//		pxy.rankUpdate();
	}

	/**
	 * 测试体力/好友/限时武器倒计时时间
	 * @param humanObj
	 */
	private void gm_testCountTime(HumanObject humanObj) {
		String str;
		String str2;
		long ll = humanObj.getHuman().getActFullTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Port.getTime());
		str2 = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());
		//System.out.println("系统当前时间是  :           " + str2);

		calendar.setTimeInMillis(ll);

		str = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(calendar.getTime());

		//System.out.println("倒计时终点时间是:           " + str);
		long l = ll - Port.getTime();

		long day = l / (24 * 60 * 60 * 1000);

		long hour = (l / (60 * 60 * 1000) - day * 24);

		long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);

		long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

		//System.out.println("还剩多长时间满体：" + day + "天" + hour + "小时" + min + "分" + s + "秒");

		// InformManager.inst().sendMsg(HumanScopeType.ALL, 0, humanObj.id,
		// channel, str);

		InformManager.inst().sendSCInformMsg(humanObj, EInformType.WorldInform, "系统当前时间时间: " + str2);
		InformManager.inst().sendSCInformMsg(humanObj, EInformType.WorldInform, "倒计时终点时间是: " + str);
		InformManager.inst().sendSCInformMsg(humanObj, EInformType.WorldInform,
				"还剩多长时间满体：" + day + "天" + hour + "小时" + min + "分" + s + "秒");
	}

	/**
	 * 重置人物次数
	 * @param humanObj
	 */
	private void gm_humanNumClear(HumanObject humanObj) {
		//humanObj.getHuman().setRenameNum(0);// 已经改名次数
		
		HumanManager.inst().resetDaily(humanObj);
	}
	
	/**
	 * 重置状态
	 * @param humanObj
	 */
	private void gm_humanofficialClear(HumanObject humanObj){
		//重置状态
//		humanObj.getHuman().setOfficialstatus(false);
	}

	
	public void readFile(MsgParam param, String path) {
		//按行读取本地文件
		String dir ="d:\\gm\\"+path.trim();
		Log.game.info("正在读取文件 filename={}", dir);
		File file=new File(dir); 
        BufferedReader reader=null;  
        String temp=null;  
        int line=1;  
        try{  
                reader=new BufferedReader(new FileReader(file));  
                while((temp=reader.readLine())!=null){  
                	gmCommand(param,temp);
                    line++;  
                }  
        }  
        catch(Exception e){  
            e.printStackTrace();  
        }  
        finally{  
            if(reader!=null){  
                try{  
                    reader.close();  
                }  
                catch(Exception e){  
                    e.printStackTrace();  
                }  
            }  
        }
		
	}
		
}
