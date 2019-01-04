package game.worldsrv.compete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;
import core.support.RandomUtils;
import core.support.Time;
import game.msg.Define.ECastellanType;
import game.msg.Define.EInformType;
import game.msg.Define.EMailType;
import game.worldsrv.character.UnitManager;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfCompeteRobot;
import game.worldsrv.config.ConfMainCityShow;
import game.worldsrv.config.ConfRewards;
import game.worldsrv.entity.Castellan;
import game.worldsrv.entity.CompeteHistory;
import game.worldsrv.entity.CompeteHuman;
import game.worldsrv.entity.CompetePartner;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.Partner;
import game.worldsrv.entity.RankGuild;
import game.worldsrv.enumType.RankType;
import game.worldsrv.humanSkill.HumanSkillManager;
import game.worldsrv.inform.InformManager;
import game.worldsrv.mail.MailManager;
import game.worldsrv.name.NameServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.D;
import game.worldsrv.support.GlobalConfVal;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.tower.TowerServiceProxy;

/**
 * 竞技场全局服务
 */
@DistrClass(servId = D.SERV_COMPETE, importClass = {List.class, Map.class, Human.class, Partner.class, 
		CompeteHumanObj.class})
public class CompeteService extends GameServiceBase {
	private static final int CountPer10Sec = 100; // 每隔几秒分波发放奖励的人数
	private static final int CountPerFind = 1000; // 每次查询1000
	private static int CurrentCompetRank = 0;// 当前最后排名 
		
	// 竞技场玩家表 <HumanID, 信息>
	private Map<Long, CompeteHumanObj> mapCompeteHumans = new HashMap<>();
	// 竞技场排名表<排名, HumanID>
	private Map<Integer, Long> mapCompeteRanks = new HashMap<>();
	
	// 首次开服，爬塔数据加载使用参数：用于统计异步取的随即名字的次数
	private int randNameAmount_for_tower = 0;

	public CompeteService(GamePort port) {
		super(port);
	}

	@Override
	protected void init() {
		
		//加载CompeteHuman表数据进入Map
		long time = Port.getTime();
		DB dbPrx = DB.newInstance(CompeteHuman.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numExist = result.get();
		// 添加机器人
		if (numExist <= 0 ) {
			addRobot();
		}else{
			//当前竞技场人数
			CurrentCompetRank = numExist;
			Log.game.info("CompeteService.init() : start load CompeteRobot, numExist={}", numExist);
			
			int loopCount = numExist / CountPerFind;
			List<Record> records = null;
			// 分页查询
			for (int i = 0; i <= loopCount; i++) {
				dbPrx.findBy(false, i * CountPerFind, CountPerFind);
				result = dbPrx.waitForResult();
				records = result.get();
				if (records == null)
					continue;
				// 加载竞技数据
				for (Record r : records) {
					CompeteHumanObj cpHumanObj = new CompeteHumanObj();
					cpHumanObj.cpHuman = new CompeteHuman(r);
					// 保存全服基础数据
					mapCompeteHumans.put(cpHumanObj.cpHuman.getId(), cpHumanObj);
					mapCompeteRanks.put(cpHumanObj.cpHuman.getRank(), cpHumanObj.cpHuman.getId());
				}
			}
			loadPartner();
			
		}
		Log.game.info("CompeteService.init() : finish load CompeteRobot, numExist={}, costTime={}", 
				numExist, Port.getTime() - time);
	}
	/**
	 * 加载伙伴数据
	 */
	private void loadPartner(){
		DB dbPrx = DB.newInstance(CompetePartner.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numExist = result.get();
		//当前竞技场人数
		Log.game.info("CompeteService.init() : start load CompeteRobot, numExist={}", numExist);
		
		int loopCount = numExist / CountPerFind;
		List<Record> records = null;
		// 分页查询
		for (int i = 0; i <= loopCount; i++) {
			dbPrx.findBy(false, i * CountPerFind, CountPerFind);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null)
				continue;
			// 加载竞技数据
			for (Record r : records) {
				CompetePartner cpPartner = new CompetePartner(r);
				CompeteHumanObj cpObj = mapCompeteHumans.get(cpPartner.getHumanId());
				if(cpObj == null) 
					continue;
				cpObj.partnerMap.put(cpPartner.getId(), cpPartner);
			}
		}
	}
	/**
	 * 添加机器人
	 */
	private void addRobot() {
		//设置机器人数量
		int length = ParamManager.competeRobotNum;
		for (int rank = 1; rank <= length; rank++) {
			// 排名被占用则跳过
			if (mapCompeteRanks.containsKey(rank)){
				continue;
			}
			// 生成对应排名的机器人
			Collection<ConfCompeteRobot> confs = ConfCompeteRobot.findAll();
			ConfCompeteRobot confCPRobot = null;
			for (ConfCompeteRobot confTemp : confs) {
				if (rank >= confTemp.rankMin && rank <= confTemp.rankMax) {
					confCPRobot = confTemp;
					break;
				}
			}
			if (confCPRobot == null) {
				Log.human.error("===ConfCompeteRobot no find rank={} in range [rankMin,rankMax] ", rank);
				continue;
			} else {
				boolean isFemale = (RandomUtils.nextInt(2) == 1 ? true : false);// 随机性别
				NameServiceProxy prx = NameServiceProxy.newInstance();
				prx.getRandomNameAndSave(isFemale);
				prx.listenResult(this::_result_getRandomNameAndSave);
			}
		}
	}
	private void _result_getRandomNameAndSave(Param results, Param context) {
		String name = Utils.getParamValue(results, "randomName", "");
		if (name.isEmpty()) {
			Log.game.error("===_result_getRandomName name=null");
			return;
		}

		CompeteHumanObj cpHumanObj = new CompeteHumanObj();
		CompeteHuman cpHuman = new CompeteHuman();
		CurrentCompetRank++;
		int rank = CurrentCompetRank;// 设置排名递增
				
		long humanId = Port.applyId();// 机器人的humanId
		Collection<ConfCompeteRobot> confs = ConfCompeteRobot.findAll();
		ConfCompeteRobot confCPRobot = null;
		for (ConfCompeteRobot confTemp : confs) {
			if (rank >= confTemp.rankMin && rank <= confTemp.rankMax) {
				confCPRobot = confTemp;
				break;
			}
		}
		if (confCPRobot == null) {
			Log.human.error("===ConfCompeteRobot no find rank={} in range [rankMin,rankMax] ", rank);
			return;
		}

		// 添加机器人数据
		cpHuman.setId(humanId);
		cpHuman.setIsRobot(true);// 是否是机器人
		cpHuman.setRank(rank);// 当前排名
		setRankTop(cpHuman, rank);// 最高排名
		setRankDaily(cpHuman, rank);// 日最高排名
		cpHuman.setTimeSync(Port.getTime());// 玩家同步时间
		cpHuman.setName(name);
		//设置机器人武将镜像
		cpHumanObj.partnerMap = setRobotCpHuman(cpHuman, confCPRobot);
		
		//重新计算机器人战斗力
		int combat = UnitManager.inst().calcCombatProp(cpHuman);
		cpHuman.setCombat(combat);
		// 保存竞技场机器人数据
		cpHuman.persist();
		cpHumanObj.cpHuman = cpHuman;
		
		mapCompeteHumans.put(cpHuman.getId(), cpHumanObj);
		mapCompeteRanks.put(cpHuman.getRank(), cpHuman.getId());
		
		// 随机名字统计次数+1
		randNameAmount_for_tower++;
		// 随机名字统计次数 >= 需要生成的机器人数量
		if (randNameAmount_for_tower >=  ConfCompeteRobot.get(confs.size()).rankMax) { //ParamManager.competeRobotNum) {
			// 远程调用生成爬塔镜像数据
			// 添加机器人后，复制到爬塔中
			TowerServiceProxy proxy = TowerServiceProxy.newInstance();
			proxy.firstLoadTowerData();
		}
	}
	
	/**
	 * 设置机器人武将镜像
	 * @param cpHuman
	 * @param conf
	 */
	private Map<Long, CompetePartner> setRobotCpHuman(CompeteHuman cpHuman, ConfCompeteRobot conf){
		if(null == conf){
			Log.table.error("setRobotCpHuman ConfCompeteRobot配表错误 空");
			return null;
		}
		//随机一个军团ID
		int  [] armyOrder = conf.aiArmy;
		int random = Utils.randomBetween(0, armyOrder.length-1);
		int armyId = armyOrder[random];
		Map<Long, CompetePartner> tmpMap = CompeteManager.inst().getBotArmyById(cpHuman, armyId);
		return tmpMap;
	}
	
	/**
	 * 获取人物信息
	 * 进入竞技场使用
	 * @param newCpObj  外部传入的CompeteHumanObj
	 */
	@DistrMethod(argsImmutable=true)
	public void competeOpen(CompeteHumanObj newCpObj ){
		long humanId = newCpObj.cpHuman.getId();
		CompeteHumanObj cpObj = mapCompeteHumans.get(humanId);
		if(cpObj == null){
			cpObj = newCpObj;
			addNewMember(humanId, newCpObj);
		} else {
			//更新humanObj
			CompeteHuman newCpHuman = newCpObj.cpHuman;
			// 属性
			CompeteHuman cpHuman = cpObj.cpHuman;
			cpHuman.setSn(newCpObj.cpHuman.getSn());
			cpHuman.setModelSn(newCpHuman.getModelSn());
			cpHuman.setDefaultModelSn(newCpHuman.getDefaultModelSn());
			cpHuman.setCombat(newCpHuman.getCombat());
			// 属性
			UnitManager.inst().copyUnit(newCpObj.cpHuman, cpHuman);
			// 阵容
			cpHuman.setPartnerLineup(newCpObj.cpHuman.getPartnerLineup());
			cpHuman.setPartnerStance(newCpHuman.getPartnerStance());
			// 上阵技能和信息
			HumanSkillManager.inst().setHumanMirrorSkill(newCpHuman, cpHuman);
			
			//cpHuman.update();
			
			//更新伙伴数据库
			for(CompetePartner nc : newCpObj.partnerMap.values()){
				//旧的集合中没有
				long partnerid = nc.getId();
				if(cpObj.partnerMap.get(partnerid)==null){
					nc.persist();//保存数据库
				} else {
					cpObj.partnerMap.put(partnerid, nc);
				}
				
			}
			//更新伙伴-内存
			cpObj.partnerMap = newCpObj.partnerMap;
		}

		List<CompeteHumanObj> cpObjlist = getEnemy(humanId);
		cpObjlist.remove(null);
		for (Iterator<CompeteHumanObj> iterator = cpObjlist.iterator(); iterator.hasNext();) {
			if(iterator.next() == null){ 
				iterator.remove();
			}
		}
		port.returns("cpHuman", cpObj.cpHuman, "cpObjlist", cpObjlist);
	}
	/**
	 * 获取对手信息列表
	 * @param humanId
	 * @return
	 */
	private List<CompeteHumanObj> getEnemy(long humanId){
		CompeteHumanObj cpHumanObj = mapCompeteHumans.get(humanId);
		//对手列表
		List<Long> enemyIdList = new ArrayList<>();
		int cpRank = cpHumanObj.cpHuman.getRank();
		if (cpRank >= ParamManager.competeRankMax) {
			cpRank = ParamManager.competeRankMax - Utils.randomBetween(0, ParamManager.competeGradeRange);
		}
		int paramA = ParamManager.competeParamA;
		int paramB = ParamManager.competeParamB;
		//间隔
		int space = (cpRank / paramA) + paramB; 
		
		List<Integer> orderRankList = new ArrayList<>();
		
		//排名在20名开外
		if(cpRank > 20 ){
			//取前三
			for (int i = 1; i <= 3; i++) {
				
				orderRankList.add(i);
			}
			//往前取12个
			for (int i = 1; i <= 11; i++) {
				int getIndex =  cpRank-space*i;
				if(getIndex < 1){
					//往前没人了 
					break;
				}
				orderRankList.add(getIndex);
			}
			//加上自己
			if(cpRank < ParamManager.competeRankMax) {
				enemyIdList.add(humanId);
				orderRankList.add(cpRank);
			}
			//取后面两个
			for (int i = 1; i <= 2; i++) {
				int getIndex =  cpRank+space*i;
				if(getIndex > ParamManager.competeRankMax){
					break;
				}
				orderRankList.add(getIndex);
			}
		}
		//如果在前20
		else if(cpRank <= 20) {
			//取前20
			enemyIdList = new ArrayList<>();
			for(int i = 1; i <= 22; i++) {
				orderRankList.add(i);
			}
			enemyIdList.add(humanId);
		}
		
		//组合返回数据
		List<CompeteHumanObj> cpHumanObjList = new ArrayList<>();
		
		//--重构代码
		cpHumanObjList = new ArrayList<>();
		for (Integer rank : orderRankList) {
			Long eid = mapCompeteRanks.get(rank);
			CompeteHumanObj obj = mapCompeteHumans.get(eid);
			if(obj!= null) {
				cpHumanObjList.add(obj);
			}
		}
		
		if(cpRank < ParamManager.competeRobotNum){
			cpHumanObjList.add(cpHumanObj);
		}
		cpHumanObjList = Utils.distinctBySetOrder(cpHumanObjList);
		
		return cpHumanObjList;
	}
	
	/**
	 * 新增竞技场成员
	 */
	private void addNewMember(long humanId ,CompeteHumanObj cpHumanObj){
		//加入数据库
		CompeteHuman cpHuman = cpHumanObj.cpHuman;
		cpHuman.setRank(ParamManager.competeRankMax);
		cpHuman.setRankTop(ParamManager.competeRankMax);
		cpHuman.setRankDaily(ParamManager.competeRankMax);
		cpHuman.persist();
		
		for (CompetePartner cpa : cpHumanObj.partnerMap.values()) {
			cpa.setId(Port.applyId());
			cpa.persist();
		}
		
		//加入缓存
		mapCompeteHumans.put(humanId, cpHumanObj);
	}
	
	/**
	 * 挑战目标对象
	 */
	@DistrMethod
	public void challenge(long fightId) {
		if (mapCompeteHumans.containsKey(fightId)) {
			CompeteHumanObj fightCompeteHumanObj = mapCompeteHumans.get(fightId);
			if (fightCompeteHumanObj != null) {// && !fightCompeteHumanObj.isFight) {// 策划要的就是这个效果
				// 如果目标是战斗状态 反正不能战斗。 否则就锁定目标的状态
				// fightCompeteHumanObj.isFight = true;
				
				port.returns("result", true, "fightCompeteHumanObj", fightCompeteHumanObj);
			} else {
				port.returns("result", false);
			}
		} else {
			port.returns("result", false);
		}
	}
	
	@DistrMethod(argsImmutable=true)
	public void rename(long humanId, String name) {
		CompeteHumanObj comHum = mapCompeteHumans.get(humanId);
		if (comHum == null) {
			return;
		}
		comHum.cpHuman.setName(name);
	}
	
	@DistrMethod(argsImmutable=true)
	public void getTopRank(long humanId) {
		CompeteHumanObj comTarHumObj = mapCompeteHumans.get(humanId);
		if (comTarHumObj == null) {
			port.returns(0);
		} else {
			port.returns(comTarHumObj.cpHuman.getRankTop());
		}
	}

	/**
	 * 设置历史最高排名
	 * @param cpHuman
	 * @param rank 最高排名
	 */
	private void setRankTop(CompeteHuman cpHuman, int rank) {
		if (cpHuman.getRankTop() <= 0 || cpHuman.getRankTop() > rank) {
			cpHuman.setRankTop(rank);
		}
	}
	
	/**
	 * 设置每日排名
	 * @param cpHuman
	 * @param rank
	 */
	private void setRankDaily(CompeteHuman cpHuman, int rank) {
		if (cpHuman.getRankDaily() <= 0) {
			cpHuman.setRankDaily(rank);
		}

		if (cpHuman.isIsRobot()) {
			return;
		}
		
		// 只要是在系统结算时间开始， 并且玩家还没有结算完成的情况下 不能同步rankDaily
		long timeNow = Port.getTime();
		long time22 = Utils.getTimeBeginOfToday(timeNow) + 22 * Time.HOUR;
		long time23 = Utils.getTimeBeginOfToday(timeNow) + 23 * Time.HOUR;

		if (timeNow > time22 && timeNow < time23 && canGiveRankAwardDaily(timeNow,cpHuman.getTimeRankAwardDaily())) {
			return;
		}

		cpHuman.setRankDaily(rank);
		// 从结算时间开始 只能从 rankDaily 中取值。
		// 有结算完毕以后才能从让rank 同步到 rankDaily 中
		// 奖励发送完成以后接着同步rank
		// 怎么判断奖励是否发送过 奖励发送时间大于23小时 可以发送
	}
	
	/**
	 * 判断每日奖励时间是否满足23小时以上
	 * @param timeLast
	 * @return
	 */
	private boolean canGiveRankAwardDaily(long timeNow ,long timeLast) {
		// 当前时间距离上次时间是否有23小时以上
		if (timeLast + 23 * Time.HOUR <= timeNow) {
			return true;
		}
		return false;
	}
	
	/**
	 * 每日22时每隔10秒执行一次
	 */
	@ScheduleMethod(Utils.cron_Day_22ST_10)
	public void _cron_Day_22ST_10() {
		int count = 0;
		for (CompeteHumanObj cpObj : mapCompeteHumans.values()) {
			if (null == cpObj || cpObj.cpHuman.isIsRobot()) {
				continue;
			}
			// 是否已经发放过奖励
			long now = Port.getTime();
			if (!canGiveRankAwardDaily(now,cpObj.cpHuman.getTimeRankAwardDaily())) {
				continue;
			}
			// 检查排名范围
			int rank = cpObj.cpHuman.getRankDaily();
			if (rank < 0 || rank > ParamManager.competeRankMax) {
				continue;
			}
			// 发放奖励并记录相关信息
			int snReward = GlobalConfVal.getCompeteReward(rank);
			ConfRewards conf = ConfRewards.get(snReward);
			if (null == conf) {
				Log.table.error("===发放奖励失败！ConfRewards error in no find sn={}", snReward);;
			} else {
				int[] itemSn = conf.itemSn;
				int[] itemNum = conf.itemNum;
				List<ProduceVo> itemProduce = new ArrayList<>();
				for (int i = 0; i < itemSn.length; i++) {
					itemProduce.add(new ProduceVo(itemSn[i], itemNum[i]));
				}
				cpObj.cpHuman.setTimeRankAwardDaily(now);
				// 发送特殊邮件
				String detail =  "{" + EMailType.MailCompeteDailyAward_VALUE + "|" + rank  + "}";
				MailManager.inst().sendSysMail(cpObj.cpHuman.getId(), ParamManager.mailMark,detail, itemProduce);
			}
			// 是否超过一波发放最大数量
			count++;
			if (count >= CountPer10Sec) {
				return;
			}
		}
	}
	
	/**
	 * 竞技场战斗结算
	 * @param idAtk 挑战者ID
	 * @param isWin 是否胜利
	 * @param idDef 对手ID 
	 */
	@DistrMethod(argsImmutable=true)
	public void swapRank(long idAtk, long idDef, boolean isWin, long recordId) {
		CompeteHumanObj cpHumanObjAtk = mapCompeteHumans.get(idAtk);//挑战者
		CompeteHumanObj cpHumanObjDef = mapCompeteHumans.get(idDef);//被挑战者
		if(cpHumanObjAtk == null || cpHumanObjDef == null){
			if(cpHumanObjAtk == null) {
				Log.logGameTest("===CompeteService.swapRank, cpHumanObjAtk=null, id={}", idAtk);
			}
			if(cpHumanObjDef == null) {
				Log.logGameTest("===CompeteService.swapRank, cpHumanObjDef=null, id={}", idDef);
			}
			port.returns(0);
			return;
		}
		
		int rankOldAtk = cpHumanObjAtk.cpHuman.getRank();
		int rankOldDef = cpHumanObjDef.cpHuman.getRank();
		int rankNewAtk = rankOldAtk;
		int rankNewDef = rankOldDef;
		boolean isBreakTopRank = false;// 是否突破最高排名
		
		// 处理连胜
		cpHumanObjAtk.cpHuman.setLastBattleIsWin(isWin);
		cpHumanObjDef.cpHuman.setLastBattleIsWin(!isWin);
		winStreak(cpHumanObjAtk.cpHuman, isWin);
		winStreak(cpHumanObjDef.cpHuman, !isWin);
		
		
		// 战斗胜利且排名变化，则处理交换排名
		if(isWin && rankOldAtk > rankOldDef ) {
			//被挑战者设置排名下降标识
			cpHumanObjDef.cpHuman.setDrop(true);
			
			rankNewAtk = rankOldDef;
			rankNewDef = rankOldAtk;
			// 交换排名
			cpHumanObjAtk.cpHuman.setRank(rankNewAtk);
			cpHumanObjDef.cpHuman.setRank(rankNewDef);
			
			// 更改且保存排名
			mapCompeteRanks.put(rankNewAtk, cpHumanObjAtk.cpHuman.getId());
			mapCompeteRanks.put(rankNewDef, cpHumanObjDef.cpHuman.getId());
			
			// 设置每日排名
			setRankDaily(cpHumanObjAtk.cpHuman, rankNewAtk);
			setRankDaily(cpHumanObjDef.cpHuman, rankNewDef);
			
			// 是否突破最高排名
			isBreakTopRank = (cpHumanObjAtk.cpHuman.getRankTop() > rankNewAtk) ? true : false;
			if (isBreakTopRank) {
				setRankTop(cpHumanObjAtk.cpHuman, rankNewAtk);
			}
			
			// 跑马灯公告
			if (rankNewAtk == 1) {
				// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
				String content = Utils.createStr("{}|{}|{}", ParamManager.sysMsgMark, 999011, 
						cpHumanObjAtk.cpHuman.getName());
				InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
			}
			
			String detail = "{" + EMailType.MailCompeteRankDown_VALUE + "|"+ cpHumanObjAtk.cpHuman.getName()+ "|"+rankNewDef+ "}";
			
			MailManager.inst().sendSysMail(idDef, ParamManager.mailMark, detail,null);
		}

		
		// 记录战报
		CompeteHistory history = new CompeteHistory();
		history.setId(Port.applyId());
		history.setCreatedAt(Port.getTime());
		history.setWin(isWin);
		// 挑战者信息
		history.setHumanIdFight(idAtk);
		history.setHumanNameFight(cpHumanObjAtk.cpHuman.getName());
		history.setFightHeadSn(cpHumanObjAtk.cpHuman.getDefaultModelSn());
		history.setRank(rankOldAtk);
		// 被挑战者信息
		history.setHumanIdBeFight(idDef);
		history.setHumanNameBeFight(cpHumanObjDef.cpHuman.getName());
		history.setBeFightHeadSn(cpHumanObjDef.cpHuman.getDefaultModelSn());
		history.setBeRank(rankOldDef);
		history.persist();
		
		
		
		port.returns("cpHumanAtk", cpHumanObjAtk.cpHuman, "cpHumanDef", cpHumanObjDef.cpHuman, 
				"isBreakTopRank", isBreakTopRank, "rankOldAtk", rankOldAtk, "rankNewAtk", rankNewAtk);
	}
		
	/**
	 * 处理连胜
	 * @param cpHuman
	 * @param isWin 这场战斗是否胜利
	 */
	private void winStreak(CompeteHuman cpHuman, boolean isWin){
		//上次挑战者是否胜利
		boolean hitLastIsWin = cpHuman.isLastBattleIsWin();
		int count = cpHuman.getSerialWinNums();
		if(isWin && hitLastIsWin){
			//如果本次胜利且上次胜利
			count++;
			if(count >=10 && count %10 == 0){
				//挑战者十连胜
				// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
				String content = Utils.createStr("{}|{}|{}", ParamManager.sysMsgMark, 999012, cpHuman.getName());
				InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
			}
			cpHuman.setSerialWinNums(count);
		}else if(!isWin){
			//如果失败
			if(count>=10){
				//十连胜终结
				// 系统提示特殊格式：ParamManager.sysMsgMark|sysMsg.sn|参数1|...
				String content = Utils.createStr("{}|{}|{}", ParamManager.sysMsgMark, 999013, cpHuman.getName());
				InformManager.inst().sendNotify(EInformType.SystemInform, content, 1);
			}
			cpHuman.setSerialWinNums(0);
		}else if(isWin && !hitLastIsWin){
			//本次胜利，上次失败
			cpHuman.setSerialWinNums(1);
		}
	}
	
	/**
	 * 复制爬塔镜像数据
	 */
	@DistrMethod(argsImmutable=true)
	public void getTowerMirror(){
		//<排名,obj>
		HashMap<Integer,CompeteHumanObj> rankMap = new HashMap<>();
		int MirrorTotleNum = ConfCompeteRobot.findAll().size();
		for (int i = 1; i <= MirrorTotleNum; i++) {
			ConfCompeteRobot confR = ConfCompeteRobot.get(i);
			int num = confR.towerNum;
			for (int j = 0; j < num; j++) {
				int index = Utils.randomBetween(confR.rankMin, confR.rankMax);
				Long humanId = mapCompeteRanks.get(index);
				if (humanId == null) {
					continue;
				}
				CompeteHumanObj cpObj = mapCompeteHumans.get(humanId);
				rankMap.put(cpObj.cpHuman.getRank(), cpObj);
			}
		}
		
		port.returns("result",rankMap);
	}
	
	/**
	 * 获取某玩家排名
	 */
	@DistrMethod()
	public void getHumanRank(long humanId){
		int rank = ParamManager.competeRankMax;
		CompeteHumanObj coj = mapCompeteHumans.get(humanId);
		if(coj != null){
			rank = coj.cpHuman.getRank();
		}
		port.returns("rank",rank);
	}
	
	/**
	 * 获取竞技场排行榜
	 */
	@DistrMethod()
	public void getCompeteRank(){
		int size = RankType.RankSumCombat.getSize();//竞技场排行榜总人数
		Map<Integer,CompeteHumanObj> map = new HashMap<>();
		for(int i = 1;i<=size;i++){
			Long humanId = mapCompeteRanks.get(i);
			CompeteHumanObj cpobj =  mapCompeteHumans.get(humanId);
			//战斗力是计算总的战斗力
			map.put(i, cpobj);
		}
		port.returnsImmutable("map",map);
		
	}
	
	/**
	 * 获取竞技场城主
	 */
	@DistrMethod()
	public void getCompeteCastellan(){
		Long humanId = mapCompeteRanks.get(1);
		CompeteHumanObj cphuman = mapCompeteHumans.get(humanId);
		ConfMainCityShow conf = ConfMainCityShow.get(ECastellanType.ArenaDuke_VALUE);
		Castellan castellan = null;
		if(conf != null && cphuman.cpHuman.getLevel() > conf.needLev && !cphuman.cpHuman.isIsRobot()) {
			castellan = new Castellan();
			castellan.setId(Port.applyId());
			castellan.setModelSn(cphuman.cpHuman.getDefaultModelSn());
			castellan.setHumanId(cphuman.cpHuman.getId());
			castellan.setName(cphuman.cpHuman.getName());
			castellan.setType(ECastellanType.ArenaDuke_VALUE);
		}
		//设置城主商店默认购买信息
		port.returns("castellan",castellan);
	}

}
