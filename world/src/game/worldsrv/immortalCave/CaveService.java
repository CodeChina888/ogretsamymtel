package game.worldsrv.immortalCave;

import game.msg.Define.ECaveType;
import game.msg.Define.EMailType;
import game.msg.Define.EMoneyType;
import game.msg.Define.ETokenType;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfCave;
import game.worldsrv.config.ConfCaveField;
import game.worldsrv.entity.Cave;
import game.worldsrv.entity.CaveHuman;
import game.worldsrv.entity.CaveLog;
import game.worldsrv.entity.CavePartner;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.MergeCave;
import game.worldsrv.entity.Partner;
import game.worldsrv.human.HumanPlusManager;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;

/**
 * 仙域
 */
@DistrClass(servId = D.SERV_CAVE, importClass = { List.class, Map.class, Human.class, Partner.class, CaveObject.class,
		HumanMirrorObject.class })
public class CaveService extends GameServiceBase {

	private static final int CountPerFind = 1000; // 每次查询1000

	/**
	 * Map<类型,Map<页数,列表<CaveObject>>>
	 */
	Map<Integer, Map<Integer, List<CaveObject>>> caveObjmaps = new HashMap<>();

	/**
	 * Map<玩家ID,洞府信息>
	 * 
	 * @param port
	 */
	Map<Long, List<CaveIndexes>> indexMap = new HashMap<>();

	/**
	 * 空闲的仙府 Map<sn,Map<type,CaveIndexs>> freeCave
	 * 
	 * @param port
	 */
	Map<Integer, Map<Integer, List<CaveIndexes>>> freeCave = new HashMap<>();

	/**
	 * 防守日志
	 * 
	 * @param port
	 */
	Map<Long, List<CaveLog>> caveLogsMap = new HashMap<>();

	public CaveService(GamePort port) {
		super(port);
	}

	/**
	 * 怪物Sn,机器人数据
	 */
	Map<Integer,CaveHumanObj> robotMap = new HashMap<>();
	
	/**
	 * 每分钟计算产量
	 */
	@Override
	public void pulseOverride() {
	}
	
	@Override
	protected void init() {
		// 读取配置表
		Collection<ConfCaveField> collection = ConfCaveField.findAll();

		for (ConfCaveField conf : collection) {
			if (conf == null) {
				Log.guild.debug("conf == null");
				continue;
			}
			int[] pageRegion = conf.pageRegion;
			int type = conf.type;
			for (int pages = pageRegion[0]; pages <= pageRegion[1]; pages++) {
				// 初始化仙府信息
				int index = 0;// 索引
				for (int sn : conf.caveSn) {
					
					
					initCaveObject(sn, type, pages, index);
					// Log.game.info("init page={},index={}",pages,index);
					index++;
				}
			}

		}

		// 读取数据库设置
		loadCave();
		// 加载日志信息
		loadCaveLog();
		Log.game.info("CaveService load finished...");

	}

	/**
	 * 初始化仙府
	 */
	private void initCaveObject(int sn, int type, int pages, int index) {
		ConfCave caveConf = ConfCave.get(sn);
		if (caveConf == null) {
			Log.guild.error("can't find sn ={}", sn);
		}
		int monsterSn = caveConf.monster;
		// 初始化仙府
		long caveId = getKey(type, pages, index);
		CaveObject caveObj = new CaveObject();
		Cave cave = new Cave();
		cave.setId(caveId);
		cave.setSn(sn);
		cave.setPage(pages);
		cave.setIndex(index);
		cave.setType(type);
		cave.setIsOwn(false);
		caveObj.setCaveId(caveId);
		caveObj.setHumanId(cave.getHumanID());
		caveObj.setCave(cave);

		setCaveIndexsStatus(cave.getSn(), type, pages, index, cave.isIsOwn());
		// 设置战斗镜像
		CaveHumanObj robot = robotMap.get(monsterSn);
		if(robot == null) {
			CaveHumanObj mirrorHuman = HumanPlusManager.inst().getCaveHumanObject(monsterSn);
			robotMap.put(monsterSn, mirrorHuman);
		}
//		caveObj.setMirrorhuman(mirrorHuman);
		
		// 获取该类型 Map<Integer, List<CaveObject>>
		Map<Integer, List<CaveObject>> caveObjmap = caveObjmaps.get(type);
		if (caveObjmap == null) {
			caveObjmap = new HashMap<Integer, List<CaveObject>>();
			caveObjmaps.put(type, caveObjmap);
		}
		List<CaveObject> caveObj_list = caveObjmap.get(pages);
		if (caveObj_list == null) {
			caveObj_list = new ArrayList<>();
			caveObjmap.put(pages, caveObj_list);
		}
		caveObj_list.add(caveObj);
	}

	private void loadCave() {
		DB dbPrx = DB.newInstance(Cave.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numExist = result.get();
		// 当前竞技场人数
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
				if (r == null) {
					continue;
				}
				Cave cave = new Cave(r);
				int type = cave.getType();
				int page = cave.getPage();
				int index = cave.getIndex();
				// Map<Integer,Map<Integer,List<CaveObject>>>
				Map<Integer, List<CaveObject>> maps = caveObjmaps.get(type);
				if (maps == null) {
					continue;
				}
				List<CaveObject> caveObj_list = maps.get(page);
				if (caveObj_list == null) {
					continue;
				}
				CaveObject caveObj = caveObj_list.get(index);
				// 数据库对象加入缓存
				caveObj.setCave(cave);

				// 加载人物镜像
				loadHumanMirror(caveObj);
				/* 设置仙府是否被占领 */
				setCaveIndexsStatus(cave.getSn(), type, page, index, cave.isIsOwn());
			}
		}
	}

	private void loadCaveLog() {

		DB dbPrx = DB.newInstance(CaveLog.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numExist = result.get();
		// 当前竞技场人数
		Log.game.info("CaveLog.init() : start load CompeteRobot, numExist={}", numExist);

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
				if (r == null) {
					continue;
				}
				CaveLog log = new CaveLog(r);
				long humanId = log.getBeChallegeHumanId();
				List<CaveLog> caveLog_list = this.caveLogsMap.get(humanId);
				if (caveLog_list == null) {
					caveLog_list = new ArrayList<>();
					this.caveLogsMap.put(humanId, caveLog_list);
				}
				caveLog_list.add(log);
			}

		}

	}

	private void loadHumanMirror(CaveObject caveObj) {
		DB dbPrx = DB.newInstance(CaveHuman.tableName);
		Cave cave = caveObj.getCave();
		if (cave == null) {
			return;
		}
		long humanId = cave.getHumanID();
		if (humanId == 0) {
			return;
		}
		dbPrx.get(cave.getId());
		Param result = dbPrx.waitForResult();
		Record record = result.get();
		if (record == null) {
			return;
		}
		CaveHuman humanMirror = new CaveHuman(record);
		caveObj.getMirrorhuman().caveHuman = humanMirror;

		List<CaveIndexes> i_list = indexMap.get(humanId);
		if (i_list == null) {
			i_list = new ArrayList<>();
			indexMap.put(humanId, i_list);
		}
		CaveIndexes caveIndexs = new CaveIndexes();
		caveIndexs.index = cave.getIndex();
		caveIndexs.page = cave.getPage();
		caveIndexs.type = cave.getType();
		i_list.add(caveIndexs);

		loadPartnerMirror(caveObj);
	}

	private void loadPartnerMirror(CaveObject caveObj) {
		Cave cave = caveObj.getCave();
		if (cave == null) {
			return;
		}
		long humanId = cave.getHumanID();

		Map<Long, CavePartner> partnerMirrorMap = new HashMap<>();

		DB dbPrx = DB.newInstance(CavePartner.tableName);

		List<Record> records = null;
		// 分页查询
		dbPrx.findBy(false, CavePartner.K.HumanId, humanId, CavePartner.K.CaveId, cave.getId());
		Param result = dbPrx.waitForResult();
		records = result.get();
		if (records == null) {
			return;
		}
		// 加载竞技数据
		for (Record r : records) {
			CavePartner pm = new CavePartner(r);
			partnerMirrorMap.put(pm.getId(), pm);
		}

		caveObj.getMirrorhuman().partnerMap = partnerMirrorMap;
		//
	}

	/**
	 * 占领成功仙府
	 */
	@DistrMethod
	public void OwnCave(long caveId, HumanMirrorObject humanMirrorObject) {
		// CaveObject caveObject = caveObjmap.get(caveId);
		// caveObject.setMirrorhuman(humanMirrorObject);

	}

	/**
	 * 获取洞穴信息
	 * 
	 * @param pageList
	 */
	@DistrMethod
	public void getInfo(long humanId,int type, List<Integer> pageList) {
		List<CaveObject> caveObjList = new ArrayList<>();
		for (Integer i : pageList) {
			Map<Integer, List<CaveObject>> maps = caveObjmaps.get(type);
			if (maps == null) {
				continue;
			}
			List<CaveObject> l = maps.get(i);
			if (l == null) {
				continue;
			}
			caveObjList.addAll(l);
		}
		port.returns("caveObjList", caveObjList,"foeList",getFoeList(humanId));
	}

	/**
	 * 根据类型,页数,生成
	 * 
	 * @param type
	 * @param pageSn
	 * @param index
	 * @return
	 */
	private long getKey(int type, int pageSn, int index) {
		long key = type * Utils.I10000 + pageSn * Utils.I100 + index;
		return key;
	}

	/**
	 * 获取洞穴基础信息信息
	 * 
	 */
	@DistrMethod
	public void getBaseMsg(int type, int page, int index, long humanId) {
		CaveObject caveObj = getCave(type, page, index);
		if (caveObj == null) {
			port.returns();
			return;
		}
		Cave cave = caveObj.getCave();
		int sn = cave.getSn();
		ConfCave caveConf = ConfCave.get(cave.getSn());
		if (caveConf == null) {
			Log.guild.error("can't find sn ={}", sn);
		}
		//如果是无主洞府就给机器人数据
		if(!cave.isIsOwn()) {
			int monsterSn = caveConf.monster;
			caveObj.setMirrorhuman(robotMap.get(monsterSn));	
		}
		
		boolean isCorrect = true;
		if (humanId == caveObj.getHumanId()) {
			isCorrect = true;
		}
		port.returns("caveObj", caveObj, "isCorrect", isCorrect);
	}

	private CaveObject getCave(int type, int page, int index) {
		Map<Integer, List<CaveObject>> maps = caveObjmaps.get(type);
		if (maps != null) {
			List<CaveObject> list = maps.get(page);
			if (list != null) {
				CaveObject caveObj = list.get(index);
				if (caveObj != null) {
					return caveObj;
				}
			}
		}
		return null;
	}

	/**
	 * 返回仙府信息
	 * 
	 * @param type
	 * @param page
	 * @param index
	 */
	@DistrMethod
	public void challenge(int type, int page, int index) {
		CaveObject caveObj = getCave(type, page, index);
		boolean result = false;
		if (caveObj != null) {
			result = true;
		}
		Cave cave = caveObj.getCave();
		int sn = cave.getSn();
		ConfCave caveConf = ConfCave.get(cave.getSn());
		if (caveConf == null) {
			Log.guild.error("can't find sn ={}", sn);
		}
		CaveHumanObj mirrorObj  = caveObj.getMirrorhuman();
		if(!cave.isIsOwn() || mirrorObj == null) {
			int monsterSn = caveConf.monster;
			//获取配置仙府机器人id 
			mirrorObj = robotMap.get(monsterSn);
		}
	
		port.returns("mirrorObj", mirrorObj, "result", result);
	}

	/**
	 * 抢夺占领结算
	 */
	@DistrMethod
	public void rob_battle_end(CaveHumanObj caveHumanObj, int type, int page, int index, boolean isWin) {
		CaveObject caveObj = getCave(type, page, index);
		Cave cave = caveObj.getCave();
		long now = Port.getTime();
		// 记录防守日志

		long bechanllegeId = cave.getHumanID();
		CaveLog caveLog = new CaveLog();
		CaveHuman caveHuman = caveHumanObj.caveHuman;
		caveLog.setBeChallegeHumanId(bechanllegeId);
		caveLog.setHumanName(caveHuman.getName());
		caveLog.setCombat(caveHuman.getCombat());
		caveLog.setHumanId(caveHuman.getId());
		caveLog.setIsWin(!isWin);
		caveLog.setBattleType(ETokenType.SnatchToken_VALUE);
		caveLog.setTime(now);
		caveLog.setId(Port.applyId());
		caveLog.setCaveId(cave.getId());
		caveLog.setType(type);
		caveLog.setPage(page);
		caveLog.setIndex(index);
		caveLog.setCaveId(cave.getId());
		caveLog.setOwnTime(now - cave.getOwnTime());

		// 设置抢夺人列表
		List<Long> robidList = Utils.strToLongList(cave.getRobHumanList());
		robidList.add(caveHuman.getId());
		cave.setRobHumanList(Utils.ListLongToStr(robidList));

		int robAccount = 0;// 被抢同比数量
		boolean success = false;// 是否抢夺成功
		if (isWin) {
			// 抢夺
			// 计算抢夺者收益
			cave.setRobCount(cave.getRobCount() + 1);

			robAccount = CaveManager.inst().robAccounts(caveObj, now);
			// 记录日志信息[被抢夺走的铜币]
			caveLog.setRobCoin(robAccount);

			// 记录被抢夺资源数-结算时扣除
			cave.setBeRobNum(cave.getBeRobNum() + robAccount);
			success = true;
		}
		saveCaveLog(bechanllegeId,caveLog);
	

		port.returns("success", success, "robAccount", robAccount);
	}

	private void saveCaveLog(long humanId,CaveLog caveLog) {
		// 保存防守日志到缓存和数据库
		List<CaveLog> caveLogs = this.caveLogsMap.get(humanId);
		if (caveLogs == null) {
			caveLogs = new ArrayList<>();
			this.caveLogsMap.put(humanId, caveLogs);
		}
		caveLogs.add(caveLog);
		if(caveLog.isOldRecord()) {
			caveLog.persist();
		}
	}
	
	/**
	 * 战斗占领结算
	 */
	@DistrMethod
	public void occupy_battle_end(CaveHumanObj caveHumanObj, int type, int page, int index, boolean isWin) {
		CaveObject caveObj = getCave(type, page, index);
		Cave cave = caveObj.getCave();
		long now = Port.getTime();
		
		long oldHumanId = cave.getHumanID();// 原占领者Id
		// 记录防守日志

		long bechanllegeId = cave.getHumanID();
		CaveLog caveLog = new CaveLog();
		CaveHuman caveHuman = caveHumanObj.caveHuman;
		caveLog.setBeChallegeHumanId(bechanllegeId);
		caveLog.setHumanName(caveHuman.getName());
		caveLog.setCombat(caveHuman.getCombat());
		caveLog.setHumanId(caveHuman.getId());
		caveLog.setIsWin(!isWin);
		caveLog.setBattleType(ETokenType.SnatchToken_VALUE);
		caveLog.setTime(now);
		caveLog.setId(Port.applyId());
		caveLog.setCaveId(cave.getId());
		caveLog.setType(type);
		caveLog.setPage(page);
		caveLog.setIndex(index);
		caveLog.setCaveId(cave.getId());
		caveLog.setOwnTime(now - cave.getOwnTime());
		if (isWin) {
			//替换占领者
			long newHumanId = caveHuman.getId();// 新占领者Id
			
			ConfCave caveConf = ConfCave.get(cave.getSn());
			
			CaveIndexes caveIndexs = new CaveIndexes();
			caveIndexs.index = cave.getIndex();
			caveIndexs.page = cave.getPage();
			caveIndexs.type = cave.getType();
			
			// 如果原领主是玩家
			if (cave.isIsOwn()) {
				// 移除 indexMap 中数据
				List<CaveIndexes> l = indexMap.get(oldHumanId);
				if(l != null) {
					l.remove(caveIndexs);
				}
				// 给原占领者发送收益邮件
				int account = CaveManager.inst().baseAccpunt(caveObj.getCave(), now);// 计算时间收益
				List<ProduceVo> itemProduce = new ArrayList<>();
				ProduceVo pVo = new ProduceVo(EMoneyType.coin_VALUE, account);
				List<ProduceVo> extra = CaveManager.inst().extraAccounts(caveObj.getCave());
				itemProduce.add(pVo);//基础收益
				itemProduce.addAll(extra);//额外收益
				
				String caveName = caveConf.name == null?"":caveConf.name;//洞府名字
				String ownName = caveHumanObj.caveHuman.getName();//新占领者名字
				String detail = "{" + EMailType.MailCaveSnatch_VALUE + "|" + caveName + "|" + ownName + "}";
				MailManager.inst().sendSysMail(oldHumanId, ParamManager.mailMark, detail, itemProduce);
			}			
			cave.setOwnTime(now);
			
			//这里的战斗力是主角战力+伙伴总战力
			int combat = caveHumanObj.caveHuman.getCombat();
			Map<Long, CavePartner> partnerMap = caveHumanObj.partnerMap;
			if(partnerMap != null) {
				for(CavePartner p:partnerMap.values()) {
					combat += p.getCombat();
				}
			}
			cave.setCombat(combat);
			cave.setDelayCount(0);
			int cdHour = caveConf == null?8:caveConf.cdTime[0];// 默认cd时间
			cave.setExpOwnTime(cdHour * Utils.SEC_EVEVRY_HOUR + now);
			
			// 更新 indexMap 中数据
			List<CaveIndexes> i_list = indexMap.get(newHumanId);
			if (i_list == null) {
				i_list = new ArrayList<>();
				indexMap.put(newHumanId, i_list);
			}
			i_list.add(caveIndexs);

			// 占领
			updateCaveHumanObj(caveObj, caveHumanObj);

			// 设置被占领
			cave.setIsOwn(true);
		}
		
		if(!cave.isOldRecord()) {
			cave.persist();
		}
		//保存防守日志
		saveCaveLog(bechanllegeId,caveLog);
		
		port.returns("success", true,"oldHumanId",oldHumanId);
	}

//	/**
//	 * 战斗结算[抢夺和占领都进这里]
//	 */
//	@DistrMethod
//	public void battleSettleAccounts(CaveHumanObj caveHumanObj, int type, int page, int index, int battleType,boolean isWin) {
//		CaveObject caveObj = getCave(type, page, index);
//		Cave cave = caveObj.getCave();
//		long now = Port.getTime();
//		// 记录防守日志
//		// 防守方id
//		long bechanllegeId = cave.getHumanID();
//		CaveLog caveLog = new CaveLog();
//		CaveHuman caveHuman = caveHumanObj.caveHuman;
//		caveLog.setBeChallegeHumanId(bechanllegeId);
//		caveLog.setHumanName(caveHuman.getName());
//		caveLog.setCombat(caveHuman.getCombat());
//		caveLog.setHumanId(caveHuman.getId());
//		caveLog.setIsWin(!isWin);
//		caveLog.setBattleType(battleType);
//		caveLog.setTime(Port.getTime());
//		caveLog.setId(Port.applyId());
//		caveLog.setCaveId(cave.getId());
//		caveLog.setType(type);
//		caveLog.setPage(page);
//		caveLog.setIndex(index);
//		caveLog.setCaveId(cave.getId());
//		caveLog.setOwnTime(now - cave.getOwnTime());
//
//		if (battleType == ETokenType.SnatchToken_VALUE) {
//			// 设置抢夺人列表
//			List<Long> robidList = Utils.strToLongList(cave.getRobHumanList());
//			robidList.add(caveHuman.getId());
//			cave.setRobHumanList(Utils.ListLongToStr(robidList));
//		}
//
//		if (isWin) {
//			occupyCave(caveHumanObj, type, page, index, caveLog, battleType);
//		} else {
//			// 战败
//			CaveIndexes caveIndexs = new CaveIndexes();
//			caveIndexs.index = cave.getIndex();
//			caveIndexs.page = cave.getPage();
//			caveIndexs.type = cave.getType();
//
//			// 记录防守日志
//			caveLog.setRobCoin(0);
//
//			port.returns("success", true, "caveIndexs", caveIndexs, "robAccount", 0);
//		}
//
//		List<CaveLog> caveLogs = this.caveLogsMap.get(bechanllegeId);
//		if (caveLogs == null) {
//			caveLogs = new ArrayList<>();
//			this.caveLogsMap.put(bechanllegeId, caveLogs);
//		}
//		caveLogs.add(caveLog);
//		caveLog.persist();
//	}

//	/**
//	 * 抢夺/占领仙府
//	 * 
//	 * @param caveHumanObj
//	 * @param type
//	 * @param page
//	 * @param index
//	 */
//	public void occupyCave(CaveHumanObj caveHumanObj, int type, int page, int index, CaveLog caveLog, int battleType) {
//
//		CaveObject caveObj = getCave(type, page, index);
//
//		if (caveObj == null) {
//			Log.guild.info("cave is null");
//			port.returns("success", false);
//			return;
//		}
//		Cave cave = caveObj.getCave();
//
//		long oldHumanId = caveObj.getCave().getHumanID();
//
//		// 替换 HumanMirrorObject
//		long humanId = caveHumanObj.caveHuman.getId();
//
//		caveObj.setHumanId(humanId);
//		ConfCave caveConf = ConfCave.get(cave.getSn());
//
//		int cdHour = 8;// 默认cd时间
//		if (caveConf != null) {
//			cdHour = caveConf.cdTime[0];
//		}
//
//		cave.setHumanID(humanId);
//		long nowTime = Port.getTime();
//		cave.setOwnTime(nowTime);
//		cave.setCombat(caveHumanObj.caveHuman.getCombat());
//		cave.setDelayCount(0);
//		cave.setExpOwnTime(cdHour * Utils.SEC_EVEVRY_HOUR + nowTime);
//		List<CaveIndexes> i_list = indexMap.get(humanId);
//		if (i_list == null) {
//			i_list = new ArrayList<>();
//			indexMap.put(humanId, i_list);
//		}
//		CaveIndexes caveIndexs = new CaveIndexes();
//		caveIndexs.index = cave.getIndex();
//		caveIndexs.page = cave.getPage();
//		caveIndexs.type = cave.getType();
//		i_list.add(caveIndexs);
//
//		if (battleType == ETokenType.DevelopmentToken_VALUE) {
//			// 占领
//
//			updateCaveHumanObj(caveObj, caveHumanObj);
//
//			// 如果原领主是玩家
//			if (cave.isIsOwn()) {
//				/*
//				 * 给原占领者发送收益邮件
//				 */
//				// 计算时间收益
//				int account = CaveManager.inst().baseAccpunt(caveObj, Port.getTime());
//				List<ProduceVo> itemProduce = new ArrayList<>();
//				ProduceVo pVo = new ProduceVo(EMoneyType.coin_VALUE, account);
//				itemProduce.add(pVo);
//				// 被谁占领
//				String caveName = "";
//				if (caveConf != null) {
//					caveName = caveConf.name;
//				}
//				String ownName = caveHumanObj.caveHuman.getName();
//				String detail = "{" + EMailType.MailCaveSnatch + "|" + caveName + "|" + ownName + "}";
//				MailManager.inst().sendSysMail(oldHumanId, ParamManager.mailMark, detail, itemProduce);
//			}
//
//			cave.setIsOwn(true);// 设置被占领
//			if (!cave.isOldRecord()) {
//				cave.persist();
//			}
//			port.returns("success", true, "caveIndexs", caveIndexs);
//		} else {
//			// 抢夺
//			// 计算抢夺者收益
//			cave.setRobCount(cave.getRobCount() + 1);
//
//			long now = Port.getTime();
//			int robAccount = CaveManager.inst().robAccounts(caveObj, now);
//			// 记录日志信息
//			caveLog.setRobCoin(robAccount);
//			caveLog.setTime(now);
//
//			// 记录被抢夺资源数
//			cave.setBeRobNum(cave.getBeRobNum() + robAccount);
//			port.returns("success", true, "caveIndexs", caveIndexs, "robAccount", robAccount);
//		}
//
//	}

	/**
	 * 仙府更换主人
	 * 
	 * @param caveObj
	 * @param newObj
	 */
	private void updateCaveHumanObj(CaveObject caveObj, CaveHumanObj newObj) {
		long newHumanId = newObj.caveHuman.getId();// 新占领者Id
		caveObj.setHumanId(newHumanId);
		Cave cave = caveObj.getCave();
		cave.setHumanID(newHumanId);
		cave.setName(newObj.caveHuman.getName());
		cave.setRobCount(0);
		cave.setRobHumanList(Utils.ListLongToStr(new ArrayList<>()));
		
		// 刪除旧的
		CaveHumanObj oldObj = caveObj.getMirrorhuman();
		if(oldObj.caveHuman.getId() != 0) {
			oldObj.caveHuman.remove();
		}
		
		for (CavePartner cp : oldObj.partnerMap.values()) {
			cp.remove();
		}

		CaveHuman caveHuman = newObj.caveHuman;
		//重组伙伴id
		String partnerList = caveHuman.getPartnerLineup();
		Map<Long, CavePartner> newPartnerMap = new HashMap<>();
		for (CavePartner cp : newObj.partnerMap.values()) {
			long newPartnerId = Port.applyId();
			long oldPartnerId = cp.getId();
			partnerList = partnerList.replace(String.valueOf(oldPartnerId), String.valueOf(newPartnerId));
			cp.setId(newPartnerId);
			cp.setCaveId(cave.getId());
			cp.persist();

			newPartnerMap.put(newPartnerId, cp);
		}
		newObj.partnerMap = newPartnerMap;

		// 保存新的
		caveHuman.setPartnerLineup(partnerList);
		caveHuman.setId(cave.getId());
		if(!caveHuman.isOldRecord()) {
			caveHuman.persist();
		}
		
		// 提交新的

		caveObj.setMirrorhuman(newObj);

		if (!cave.isOldRecord()) {
			cave.persist();
		}
	}

	@DistrMethod
	public void getMyCaveInfo(long humanId) {
		List<CaveIndexes> indexlist = indexMap.get(humanId);
		if (indexlist == null) {
			port.returns();
			return;
		}
		List<CaveObject> caveObjList = new ArrayList<>();
		for (CaveIndexes cid : indexlist) {
			int type = cid.type;
			int page = cid.page;
			int index = cid.index;
			CaveObject caveObj = getCave(type, page, index);
			if (caveObj == null) {
				continue;
			}
			caveObjList.add(caveObj);
		}
		port.returns("caveObjList", caveObjList);
	}

	/**
	 * 时间到了计算收益
	 */
	@DistrMethod
	public void checkAccount(long humanId) {

		List<CaveIndexes> clist = indexMap.get(humanId);
		if (clist == null) {
			port.returns();
			return;
		}
		for (CaveIndexes cid : clist) {
			int type = cid.type;
			int page = cid.page;
			int index = cid.index;
			checkAccountNow(humanId, type, page, index,true);
		}

	}

	/**
	 * 立刻结算仙府
	 */
	@DistrMethod
	public void giveUp(long humanId, int type, int page, int index) {
		checkAccountNow(humanId, type, page, index,true);
		//从我的仙府中移除
		List<CaveIndexes> caveIndexs = indexMap.get(humanId);
		CaveIndexes cid = new CaveIndexes(type, page, index);
		if(caveIndexs.contains(cid)) {
			caveIndexs.remove(cid);
		}
		
	}

	/**
	 * 用当前时间结算仙府
	 */
	private void checkAccountNow(long humanid, int type, int page, int index,boolean isPortreturn) {
		CaveObject caveObj = getCave(type, page, index);
		Cave cave = caveObj.getCave();
		long ownhumanId = cave.getHumanID();
		if (humanid != ownhumanId) {
			Log.guild.debug("这个仙府不是你的");
			port.returns();
			return;
		}
		long now = Port.getTime();
		if (humanid == ownhumanId) {
			// id校验正确才能结算
			//当前仙府玩家单位时长收益x占领时长
			int baseNum = CaveManager.inst().baseAccpunt(caveObj.getCave(), now);
			
			//帮会加成
			int allyNum = CaveManager.inst().getAllyNum(page, caveObj.getGuildId(), caveObjmaps.get(type));
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
				
				MailManager.inst().sendSysMail(humanId, ParamManager.mailMark, detail, extra);
			}
		

			/**
			 * 初始化仙府
			 */
			// 删除数据库中human,partner数据
			CaveHumanObj caveHumanObj = caveObj.getMirrorhuman();
			CaveHuman chuman = caveHumanObj.caveHuman;
			if(chuman!= null) {
				chuman.remove();
			}
			
			for (CavePartner cp : caveHumanObj.partnerMap.values()) {
				cp.remove();
			}
			// 初始化仙府
			int monsterSn = caveConf.monster;
			caveObj = new CaveObject();
			caveObj.setHumanId(0);
			// 设置战斗镜像
			CaveHumanObj mirrorHuman = robotMap.get(monsterSn);
			cave.setCombat(mirrorHuman.caveHuman.getCombat());
			cave.setExpOwnTime(0);
			cave.setDelayCount(0);
			cave.setOwnTime(0);
			cave.setHumanID(mirrorHuman.caveHuman.getId());
			cave.setName("");
			cave.setIsOwn(false);
			caveObj.setHumanId(0);
			caveObj.setCave(cave);
			caveObj.setMirrorhuman(mirrorHuman);
			// 获取该类型 Map<Integer, List<CaveObject>>
			Map<Integer, List<CaveObject>> caveObjmap = caveObjmaps.get(type);
			if (caveObjmap == null) {
				Log.guild.debug("初始化仙府失败");
				port.returns("");
			}
			List<CaveObject> l = caveObjmap.get(page);
			l.set(index, caveObj);

			CaveIndexes c = new CaveIndexes(type, page, index);
			List<CaveIndexes> clist = indexMap.get(humanId);
			if(clist != null) {
				clist.remove(c);
			}
			//设置仙府空闲
			setCaveIndexsStatus(cave.getSn(), type, page, index, false);
			if(isPortreturn) {
				port.returns("caveObj", caveObj);
			}
		}

	}

	
	/**
	 * 全服结算
	 */
	@ScheduleMethod(Utils.cron_Day_Min)
	public void balanceAllCaveObj() {
		long now = Port.getTime();
		for (ECaveType etype : ECaveType.values()) {
			Integer type = etype.getNumber();
			Map<Integer, List<CaveObject>> typemaps = caveObjmaps.get(type);
			if (typemaps == null) {
				Log.guild.debug("typemaps == null");
				continue;
			}
			for (List<CaveObject> cavelist : typemaps.values()) {
				if (cavelist == null) {
					Log.guild.debug("cavelist == null");
					continue;
				}
				for (CaveObject caveObj : cavelist) {
					Cave cave = caveObj.getCave();
					
					if (cave.getExpOwnTime()== 0 || now <= cave.getExpOwnTime() ) {
						continue;
					}
					long humanId = cave.getHumanID();
					int page = cave.getPage();
					int index= cave.getIndex();
					
					checkAccountNow(humanId, type, page, index,false);
				}
			}
		}
	}
	
	
	/**
	 * 获取扣费花费
	 * @param humanId
	 * @param type
	 * @param page
	 * @param index
	 */
	@DistrMethod
	public void getCaveInfo(long humanId, int type, int page, int index) {
		CaveObject caveObj = getCave(type, page, index);
		port.returns("caveObj", caveObj);
	}
	
	
	/**
	 * 延长仙府时间
	 */
	@DistrMethod
	public void addTime(long humanId, int type, int page, int index) {
		CaveObject caveObj = getCave(type, page, index);
		//扣费
		
		if (caveObj == null) {
			port.returns();
		}
		Cave cave = caveObj.getCave();

		if (cave == null) {
			port.returns();
		}

		if (cave.getHumanID() != humanId) {
			Log.guild.debug("不是你的洞府");
			port.returns();
			return;
		}

		ConfCave caveConf = ConfCave.get(cave.getSn());
		if (caveConf == null) {
			Log.table.info("配置表错误 延时仙府cave模块");
			port.returns();
			return;
		}
		int cdTime[] = caveConf.cdTime;
		int len = cdTime.length;
		if (cave.getDelayCount() +1 >= len) {
			Log.guild.debug("超出可延时的次数");
			port.returns();
			return;
		}
		
		
		int delayCount = cave.getDelayCount() + 1;
		
		
		cave.setDelayCount(delayCount);//延长的次数
		int addTimes = caveConf.cdTime[delayCount];
		cave.setExpOwnTime(cave.getOwnTime() + addTimes * Utils.SEC_EVEVRY_HOUR);
		port.returns("delayCount", delayCount, "caveObj", caveObj);
	}

	@DistrMethod
	public void getFreeCave(int sn, int type) {
		Map<Integer, List<CaveIndexes>> cl = freeCave.get(sn);
		if (cl != null) {
			List<CaveIndexes> caveIndexSet = cl.get(type);
			port.returns("caveIndex", caveIndexSet.get(0));
		}
		port.returns("");
	}

	/**
	 * 设置仙府状态
	 * 
	 * @param sn
	 * @param type
	 * @param pages
	 * @param index
	 * @param isOwn
	 *            false为空闲，true为占领
	 */
	private void setCaveIndexsStatus(int sn, int type, int pages, int index, boolean isOwn) {
		CaveIndexes cid = new CaveIndexes(type, pages, index);
		Map<Integer, List<CaveIndexes>> cl = freeCave.get(sn);
		if (cl == null) {
			cl = new HashMap<>();
			freeCave.put(sn, cl);
		}
		List<CaveIndexes> caveSet = cl.get(type);
		if (caveSet == null) {
			caveSet = new ArrayList<>();
			cl.put(type, caveSet);
		}
		if (!isOwn) {
			caveSet.add(cid);
		} else {
			if (caveSet.contains(cid)) {
				caveSet.remove(cid);
			}
		}
		
		
	}

	@DistrMethod
	public void getCaveLog(long humanId) {
		List<CaveLog> caveLog = this.caveLogsMap.get(humanId);
		port.returns("caveLog", caveLog);
	}

	/**
	 * 是否可以占领
	 * 
	 * @param humanId
	 * @return
	 */
	@DistrMethod
	public void canOccupy(long humanId, int type) {
		boolean can = false;
		int reason = CaveManager.CAN_NOT_MORE_THAN_TWO;
		List<CaveIndexes> cl = this.indexMap.get(humanId);
		if (cl == null || cl.size() <= 0) {
			can = true;
			port.returns("can", can);
			return;
		}
		if (cl.size() >= ParamManager.domaincaveNumLimit) {
			Log.guild.debug("占领仙府数量不能超过2");
			port.returns("can", can, "reason", reason);
			return;
		}
		// 查看已经占有的仙府的类型
		int alrady_has_type = cl.get(0).type;
		if (alrady_has_type == type) {
			reason = CaveManager.PLEASE_CHOOSE_OTHER;
			can = false;
		} else {
			can = true;
		}
		Log.guild.debug("不能占有用类型仙府={}", type);
		port.returns("can", can, "reason", reason);

	}

	/**
	 * 是否可以抢夺
	 * 
	 * @param type
	 * @param pages
	 * @param index
	 */
	@DistrMethod
	public void canRob(long humanId, int type, int pages, int index) {
		boolean can = false;
		boolean isAlreadyRob = false;// 是否已经强夺过了
		CaveObject caveObj = getCave(type, pages, index);
		Cave cave = caveObj.getCave();
		// 人是否在抢夺列表中
		List<Long> robidList = Utils.strToLongList(cave.getRobHumanList());
		if (robidList.contains(humanId)) {
			//
			isAlreadyRob = true;
		}
		if (caveObj != null) {

			int count = cave.getRobCount();
			if (count < ParamManager.domainBeGrabLimit && !isAlreadyRob) {
				can = true;
			}
		}
		port.returns("can", can);
	}
	
	private List<Long> getFoeList(long humanId){
		List<Long>  l = new ArrayList<>();
		List<CaveIndexes> mylist = indexMap.get(humanId);
		if(mylist!=null) {
			for(CaveIndexes c:mylist) {
				int type = c.type;
				int page = c.page;
				int index = c.index;
				CaveObject caveObj = getCave(type, page, index);
				if(caveObj == null) {
					continue;
				}
				Cave cave = caveObj.getCave();
				if(cave == null) {
					continue;
				}
				List<Long>  foe_list = Utils.strToLongList(cave.getRobHumanList());
				if(foe_list == null) {
					continue;
				}
				l.addAll(foe_list);
			}
		}
		return l;
		
	}
	
	
	/**
	 * 获取humanId列表的位置
	 * @param humanId
	 */
	@DistrMethod
	public void getCaveMemberInfo(List<Long> humanId) {
		List<CaveIndexes>caveIndexList = new ArrayList<>(); 
		for(Long hid:humanId) {
			List<CaveIndexes> c = indexMap.get(hid);
			if(c != null) {
				caveIndexList.addAll(c);
			}
		}
		port.returns("caveIndexList",caveIndexList);
		
	}
	
	
	
	
	// gmguildcave 1 1 6
	@DistrMethod
	public void gmtest_getCaveInfo(int type,int page,int index) {
		Log.guild.info(" test ad={}",caveObjmaps);
		
		CaveObject coj = getCave(type, page, index);
		Log.guild.info("coj humanId = {}",coj.getMirrorhuman().getCaveHuman().getId());
	}
	
	
	
}
