package game.worldsrv.maincity;

import game.msg.Define.DCastellanInfo;
import game.msg.Define.ECastellanType;
import game.msg.MsgCastellan.SC_BecomeCastellan;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfCastellanShop;
import game.worldsrv.entity.Castellan;
import game.worldsrv.entity.RedPacket;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.rank.RankGlobalServiceProxy;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;


/**
 * 城主服务
 */
@DistrClass(servId = D.SERV_MAINCITY, importClass = {List.class, Map.class,Castellan.class,ECastellanType.class})
public class MaincityService extends GameServiceBase {
	private static final int CountPerFind = 1000; // 每次查询1000(分页加载每次加载的记录条数)
	//<ECastellan的类型,城主>
	private Map<Integer,Castellan> CastellanMap= new HashMap<Integer,Castellan>();
	//红包<红包ID,红包>
	private Map<Long,RedPacket> redPakeMap= new HashMap<Long,RedPacket>();
	
	
	private static int countPerFind = 1000; // 每次查询1000
	
	public MaincityService(GamePort port) {
		super(port);
	}

	@Override
	protected void init() {
		
		//加载CompeteHuman表数据进入Map
		long time = Port.getTime();
		DB dbPrx = DB.newInstance(RedPacket.tableName);
		dbPrx.countBy(false);// 获得数量
		Param result = dbPrx.waitForResult();
		int numExist = result.get();
		Log.game.info("MaincityService.init() : start load RedPacket, numExist={}", numExist);
		
		int loopCount =(int) Math.ceil((float)numExist / countPerFind);
		List<Record> records = null;
		// 分页查询
		for (int i = 0; i <= loopCount; i++) {
			dbPrx.findBy(false, i * CountPerFind, CountPerFind);
			result = dbPrx.waitForResult();
			records = result.get();
			if (records == null)
				continue;
			// 加载红包数据
			for (Record r : records) {
				
				RedPacket redpacket = new RedPacket(r);
				this.redPakeMap.put(redpacket.getId(), redpacket);
			}
			
		}
		
		/*加载城主 TODO */
		DB castellan_dbPrx = DB.newInstance(Castellan.tableName);
		castellan_dbPrx.findBy(false);
		Param castellan_result = dbPrx.waitForResult();
		List<Record> castellan_records = castellan_result.get();
		for(Record r : castellan_records){
			Castellan c = new Castellan(r);
			this.CastellanMap.put(c.getType(), c);
		}
		Log.game.info("MaincityService.init() : finish load RedPacket, numExist={}, costTime={}", numExist, Port.getTime() - time);
		
	}
	
	/**
	 * 根据类型添加城主
	 * @param type
	 * @param castellan
	 */
	@DistrMethod
	public void CastellanMap(ECastellanType type,Castellan castellan){
		int typeIndex = type.getNumber();
		CastellanMap.put(typeIndex, castellan);
	}
	
	/**
	 * 发一个红包
	 */
	@DistrMethod
	public void addRedPacket(long humanId,int packSn){
		RedPacket redpacket = RedPacketHelper.productRedPacket(humanId, packSn, Port.getTime());
		if(!redpacket.isOldRecord()) {
			redpacket.persist();
		}
		this.redPakeMap.put(redpacket.getId(), redpacket);
		port.returns("redpacket",redpacket);
	}
	/**
	 * 抢红包
	 */
	@DistrMethod
	public void robRedPacket(long humanId,String humanName,long packId){
		RedPacket redpacket = this.redPakeMap.get(packId);
		int getNum = 0;
		if(redpacket != null){
			//校验时间 TODO
//			long begintTime = redpacket.getBeginTime();
//			long endTime = redpacket.getEndTime();
//			long now = Port.getTime();
//			if(begintTime > now || endTime<now){
//				Log.game.info("红包已过时");
//			}
			if (MainCityManager.IsAlreadyGet(humanId, redpacket)) {
				getNum = 0;
			} else {
				getNum =RedPacketHelper.robRedPacket(humanId, humanName,redpacket);
			}
		}else{
			Log.game.info("该红包不存在");
		}
		port.returns("getNum",getNum,"redpacket",redpacket);
	}
	
	
	
	/*请求，城主购买礼包,单次*/
	@DistrMethod
	public void getCastellanType_Buy(ECastellanType type,int sn){
		Castellan castellan = CastellanMap.get(type.getNumber());
		int hasBuyNum = castellan.getHasBuyNum();
		ConfCastellanShop confShop = ConfCastellanShop.get(sn);
		if(confShop == null){
			Log.table.error("MaincityService.castellanBuy error sn ={}",sn);
		}
		boolean can = false;
		castellan.setHasBuyNum(hasBuyNum+1);
		//获取当前购买次数
		if(hasBuyNum <= confShop.buyLmt){
			can = true;
		}
	
		port.returns("castellan",castellan,"can",can);
	}
	/**
	 * 获取所有城主Map
	 */
	@DistrMethod
	public void getCastellanMap(){
		port.returns("map",this.CastellanMap);
	}
	/**
	 * 获取所有红包信息
	 */
	@DistrMethod
	public void getRedPacketMap(){
		port.returns("map",this.redPakeMap);
	}
	/**
	 * 更换城主
	 */
	@DistrMethod
	public void updateDuke(){
		RankGlobalServiceProxy prx = RankGlobalServiceProxy.newInstance();
		prx.getCastellan();
		long pid = port.createReturnAsync();// 创建一个异步返回
		prx.listenResult(this::result_updateDuke,"pid",pid);
	}
	
	public void result_updateDuke(Param results, Param context){
		long pid = Utils.getParamValue(context, "pid", -1L);
		//迭代器删除旧城主数据
//		Map<Integer,Castellan> oldCastenllanMap  = this.CastellanMap;
//		Iterator <Entry<Integer,Castellan>> iter = oldCastenllanMap.entrySet().iterator();
//		while(iter.hasNext()){
//			 Map.Entry<Integer, Castellan> entry = (Map.Entry<Integer, Castellan>) iter.next();
//			 Castellan casOld = entry.getValue();
//			 if(casOld.getType() ==ECastellanType.WorldBossDuke_VALUE ){//封印之地城主特殊处理,封印之地不删除
//				 continue;
//			 }
//			 casOld.remove();
//		}
		
		
		SC_BecomeCastellan.Builder msg = SC_BecomeCastellan.newBuilder();
		//新城主数据
		Map<Integer,Castellan> castellanMap = results.get("castellanMap");
		for(Iterator<Entry<Integer, Castellan>> iter = castellanMap.entrySet().iterator(); iter.hasNext(); ){
			Entry<Integer, Castellan> entry = iter.next();
			Integer type = entry.getKey();
			//删除旧城主中的数据
			Castellan oldcast = this.CastellanMap.remove(type);
			if(oldcast != null) {
				oldcast.remove();
			}
			Castellan cas = entry.getValue();
			if(cas == null) {
				iter.remove();
//				ECastellanType eType;
//				if (oldcast!=null && (eType=ECastellanType.valueOf(oldcast.getType()))!=null) {
//					msg.addInfo(DCastellanInfo.newBuilder().setType(eType).setHumanId(0));
//				}
			} else {
				cas.setId(Port.applyId());
				if(!cas.isOldRecord()){
					cas.persist();
				}
				DCastellanInfo dinfo = MainCityManager.inst().getCastellanMsg(cas);
				msg.addInfo(dinfo);
				this.CastellanMap.put(type, cas);
			}
		}
		if (msg.getInfoCount() > 0) {
			// 给所有在线玩家推送msg
			HumanGlobalServiceProxy pr = HumanGlobalServiceProxy.newInstance();
			pr.sendMsgToAll(new ArrayList<>(), msg.build());
		}
		port.returnsAsync(pid,"castellanMap",castellanMap);
	}
	
	/**
	 * 添加世界Boss城主
	 */
	@DistrMethod
	public void addWorldBossCastellan(Castellan castellan){
		//迭代器删除旧城主数据
		Map<Integer,Castellan> oldCastenllanMap  = this.CastellanMap;
		Iterator <Entry<Integer,Castellan>> iter = oldCastenllanMap.entrySet().iterator();
		while(iter.hasNext()){
			 Map.Entry<Integer, Castellan> entry = (Map.Entry<Integer, Castellan>) iter.next();
			 Castellan casOld = entry.getValue();
			 if(casOld.getType() == ECastellanType.WorldBossDuke_VALUE ){
				 casOld.remove();
			 }
		}
		CastellanMap.put(ECastellanType.WorldBossDuke_VALUE, castellan);
		
		port.returns("result", true);
	}
	
	@DistrMethod
	public void cleanRedPacket(){
		long nowTimeStamp = Port.getTime();
		// 复制完镜像数据后，清空今日跨天以前的数据
        Iterator<Entry<Long, RedPacket>> iter = redPakeMap.entrySet().iterator();  //hashMap的迭代器
        for (; iter.hasNext(); ){                                                  
            Map.Entry<Long, RedPacket> entry = (Map.Entry<Long, RedPacket>) iter.next();
            RedPacket r = entry.getValue();
            // 记录时间小于一周以前跨天的时间，则从内存中清除
 			if (nowTimeStamp > r.getEndTime()) {
 				iter.remove();
 				r.remove();
 			}
        }
	}
	
	/*每小时全服出发触发一次*/
	@ScheduleMethod(Utils.cron_Day_Hour)
	public void _cron_Day_Hour() {
		MainCityManager.inst().changeCastellan();
	}
}
