package game.worldsrv.maincity;

import game.msg.Define.DCastellanInfo;
import game.msg.Define.DGainsituation;
import game.msg.Define.DItem;
import game.msg.Define.DMoney;
import game.msg.Define.DNetRedPacket;
import game.msg.Define.ECastellanType;
import game.msg.Define.ECostGoldType;
import game.msg.Define.EMailType;
import game.msg.Define.EMoneyType;
import game.msg.Define.EWinksSendType;
import game.msg.MsgCastellan.SCLoginCastellanInfo;
import game.msg.MsgCastellan.SCLoginRedPacket;
import game.msg.MsgCastellan.SCNTFSendWinks;
import game.msg.MsgCastellan.SCSendWinks;
import game.msg.MsgCastellan.SC_BecomeCastellan;
import game.msg.MsgCastellan.SC_BuyMasterPackageMsg;
import game.msg.MsgCastellan.SC_GetRedPacket;
import game.msg.MsgCastellan.SC_RedPacketMsg;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.config.ConfCastellanShop;
import game.worldsrv.config.ConfMagicExpression;
import game.worldsrv.config.ConfMainCityShow;
import game.worldsrv.config.ConfVipUpgrade;
import game.worldsrv.entity.Castellan;
import game.worldsrv.entity.RedPacket;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Utils;
import core.support.observer.Listener;

public class MainCityManager extends ManagerBase {
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static MainCityManager inst() {
		return inst(MainCityManager.class);
	}
	
	/**
	 * 登陆时候:下发红包信息
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void laodRedPack(Param param) {
		//
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		MaincityServiceProxy prx = MaincityServiceProxy.newInstance();
		prx.getRedPacketMap();
		prx.listenResult(this::_result_laodRedPack, "humanObj",humanObj);
	}
	private void _result_laodRedPack(Param results, Param context){
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanPartner humanObj is null");
			return;
		}
		Map<Long,RedPacket> redPacketMap = results.get("map");
		if(redPacketMap== null){
			return;
		}
		SCLoginRedPacket.Builder msg = SCLoginRedPacket.newBuilder();
		
		for(Entry<Long,RedPacket> enrty:redPacketMap.entrySet()){
			RedPacket r = enrty.getValue();
			//判断红包是否被抢过，被抢过则不下发
			if(IsAlreadyGet(humanObj.getHumanId(), r)){
				continue;
			}
			DNetRedPacket info = getRedPackMsg(r);
			msg.addRedpacket(info);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 登陆时候:下发城主信息
	 */
	@Listener(EventKey.HumanDataLoadOther2)
	public void _listener_HumanDataLoadOther2(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		MaincityServiceProxy prx = MaincityServiceProxy.newInstance();
		prx.getCastellanMap();
		prx.listenResult(this::_result_loadCastenllanInfo, "humanObj",humanObj);
		// 玩家2级模块数据开始加载一个
		Event.fire(EventKey.HumanDataLoadOther2BeginOne, "humanObj", humanObj);
	}
	private void _result_loadCastenllanInfo(Param results, Param context){
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanPartner humanObj is null");
			return;
		}
		// 玩家2级模块数据完成加载一个
		Event.fire(EventKey.HumanDataLoadOther2FinishOne, "humanObj", humanObj);
		
		Map<Integer,Castellan> CastellanMap = results.get("map");
		if(CastellanMap== null){
			return;
		}
		SCLoginCastellanInfo.Builder msg = SCLoginCastellanInfo.newBuilder();
		for(Entry<Integer,Castellan> enrty:CastellanMap.entrySet()){
			Castellan ca = enrty.getValue();
			// 是城主，且没有通知过客户端
			if(ca.getHumanId() == humanObj.getHumanId() && !ca.isIsNotice()){
				ca.setIsNotice(true);
				Event.fire(EventKey.MainCityCastellan, "humanObj", humanObj, "castellanType", ca.getType());
			}
			DCastellanInfo info = getCastellanMsg(ca);
			msg.addInfo(info);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 请求，城主购买礼包
	 * @param type
	 * @param humanObj
	 * @param sn 礼包sn
	 * @param num
	 */
	public void CS_BuyMasterPackageMsg(ECastellanType type,HumanObject humanObj,int sn,int num){
		MaincityServiceProxy prx = MaincityServiceProxy.newInstance();
		for(int i =0;i<num;i++){
			prx.getCastellanType_Buy(type,sn);
			prx.listenResult(this::result_CS_BuyMasterPackageMsg, "humanObj",humanObj,"sn",sn);
		}
	}
	private void result_CS_BuyMasterPackageMsg(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		Castellan castellan = results.get("castellan");
		boolean can = results.get("can");
		if(!can){
			Log.game.info("购买次数已达到上限");
			return;
		}
		int sn = context.getInt("sn");
		buyOnce(castellan,humanObj,sn);
	}
	
	/**
	 * 商店购买一次
	 * @param castellan
	 * @param humanObj
	 */
	public void buyOnce(Castellan castellan,HumanObject humanObj,int sn){
		//校验是否为当前城主
		if(castellan.getHumanId() != humanObj.getHumanId()){
			Log.game.info("该玩家不是当前城主");
			return;
		}
	
		ConfCastellanShop confShop = ConfCastellanShop.get(sn);
		if(confShop == null){
			Log.table.error("MaincityService.castellanBuy error sn ={}",sn);
		}
		//获取当前购买次数
		int hasBuyNum = castellan.getHasBuyNum();
		if(hasBuyNum > confShop.buyLmt){
			Log.game.info("购买次数已达到上限");
			return;
		}
		int currentNum = hasBuyNum;//当前购买次数
		//超出免费次数则付费
		if(currentNum > confShop.freeNum ){
			int goldCost = confShop.goldCost;
			//计算折扣
			if(currentNum >= confShop.costIncrease.length){
				//超过最大次数
				currentNum = confShop.costIncrease.length-1;
			}
			goldCost *= (float)confShop.costIncrease[currentNum-1]/Utils.I10000;//计算折扣
			if(!RewardHelper.checkAndConsume(humanObj,DMoney.GOLD_FIELD_NUMBER,goldCost,LogSysModType.MainCityBuy)) {
				return;
			}
		}
		//发放奖励
		int [] rewardSn = confShop.rewardSn;
		int [] rewardNum = confShop.rewardNum;
		RewardHelper.reward(humanObj, rewardSn, rewardNum,LogSysModType.MainCityBuy);
		
		
		//客户端购买回执
		SC_BuyMasterPackageMsg.Builder msg = SC_BuyMasterPackageMsg.newBuilder();
		msg.setType(ECastellanType.valueOf(castellan.getType()));
		for (int i = 0; i < rewardSn.length; i++) {
			DItem.Builder item =  DItem.newBuilder();
			item.setItemSn(rewardSn[i]);
			item.setNum(rewardNum[i]);
			msg.addItem(item);
		}
		msg.setBuyNum(castellan.getHasBuyNum());
		humanObj.sendMsg(msg);
		
		//如果是世界boss城主在世界boss开启前一分钟的时候是不能发红包的
//		ConfInstActConfig conf = ConfInstActConfig.get(310001);
//		int [] hour = conf.openHour;
////		int [] min = conf.openMinute;
//		if(castellan.getType() == ECastellanType.WorldBossDuke_VALUE){
//			for (int i = 0; i < hour.length; i++) {
//				if(Utils.getHourOfDay() == hour[i]){
//					Log.game.info("如果是世界boss城主在世界boss开启前一分钟的时候是不能发红包的");
//					humanObj.sendSysMsg(9905);
//					return;
//				}
//			}
//		}
//		//发放红包-异步操作
		int redPaketSn = confShop.redSn;
		MaincityServiceProxy prx = MaincityServiceProxy.newInstance();
		prx.addRedPacket(humanObj.getHumanId(), redPaketSn); 
		prx.listenResult(this::result_addRedPacket, "humanObj",humanObj);
	}
	private void result_addRedPacket(Param results, Param context) {
		// 创建完毕，用户切换地图
//		HumanObject humanObj = context.get("humanObj");
		RedPacket redpacket = results.get("redpacket");
		//通知客户端
		// 给所有在线玩家推送msg
		
		SC_RedPacketMsg.Builder msg = SC_RedPacketMsg.newBuilder();
		DNetRedPacket dinfo =	getRedPackMsg(redpacket);
		msg.setRedpacket(dinfo);
		HumanGlobalServiceProxy pr = HumanGlobalServiceProxy.newInstance();
		pr.sendMsgToAll(new ArrayList<>(), msg.build());
	}

	/**
	 * 是否已经抢过了
	 * @param humanId
	 * @param rp
	 * @return
	 */
	public static boolean IsAlreadyGet(long humanId,RedPacket rp) {
		boolean isGet = false;
		
		JSONArray gainJa = JSONArray.parseArray(rp.getGainsituation());
		Iterator<Object> it = gainJa.iterator();
		while (it.hasNext()) {
			//组合领取信息数据
			JSONObject jo = (JSONObject) it.next();
			long gain_humanId = jo.getLong("humanid");
			if(gain_humanId == humanId){
				isGet = true;
				break;
			}
		}
		return isGet;
	}
	
	/**
	 * 获取返回给客户端的消息
	 * @param rp
	 * @return
	 */
	public DNetRedPacket getRedPackMsg(RedPacket rp) {
		DNetRedPacket.Builder dnr = DNetRedPacket.newBuilder();
		dnr.setRedpacketid(rp.getId());
		dnr.setHumanId(rp.getHumanId());
		dnr.setNums(rp.getNums());
		dnr.setBegintime(rp.getBeginTime());
		dnr.setEndTime(rp.getEndTime());
		dnr.setRedPacketSn(rp.getSn());
		
		JSONArray gainJa = JSONArray.parseArray(rp.getGainsituation());
		Iterator<Object> it = gainJa.iterator();
		while (it.hasNext()) {
			DGainsituation.Builder ginfo = DGainsituation.newBuilder();
			//组合领取信息数据
			JSONObject jo = (JSONObject) it.next();
			long humanId = jo.getLong("humanid");
			String name = jo.getString("name");
			
			ginfo.setHumanId(humanId);
			ginfo.setName(name);
			
			DItem.Builder ditemMsg = DItem.newBuilder();
			int sn = rp.getItemSn();
			ditemMsg.setItemSn(sn);
			int num = jo.getIntValue("getItem");
			ditemMsg.setNum(num);
			ginfo.addItem(ditemMsg);
			dnr.addInfo(ginfo);
		}
		return dnr.build();
	}
	
	/**
	 * 抢红包
	 * @param humanObj
	 * @param packetdId
	 */
	public void robRedPacketMsg(HumanObject humanObj ,long packetdId){
		MaincityServiceProxy prx = MaincityServiceProxy.newInstance();
		prx.robRedPacket(humanObj.getHumanId(),humanObj.getHuman().getName(),packetdId);
		prx.listenResult(this::result_robRedPacketMsg, "humanObj",humanObj);
	}
	private void result_robRedPacketMsg(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		int getNum = results.getInt("getNum");
		RedPacket redpacket = results.get("redpacket");
		SC_GetRedPacket.Builder msg = SC_GetRedPacket.newBuilder();
		msg.setHumanId(humanObj.getHumanId());
		DNetRedPacket rinfo = getRedPackMsg(redpacket);
		msg.setRedpacket(rinfo);
		if(getNum == 0){
			msg.setSuccess(false);
			humanObj.sendMsg(msg);
			return;
		}
		DItem.Builder dmsg = DItem.newBuilder();
		//红包固定是铜币
		int ItemSn = redpacket.getItemSn();
		dmsg.setItemSn(ItemSn);
		dmsg.setNum(getNum);
		RewardHelper.reward(humanObj, ItemSn, getNum ,LogSysModType.MainCityBuy);
		msg.addItem(dmsg);
		msg.setSuccess(true);
		
 		humanObj.sendMsg(msg);
	}
	
	public DCastellanInfo getCastellanMsg(Castellan castellan){
		DCastellanInfo.Builder msg = DCastellanInfo.newBuilder();
		msg.setType(ECastellanType.valueOf(castellan.getType()));
		msg.setName(castellan.getName());
		msg.setHumanId(castellan.getHumanId());
		msg.setSn(castellan.getModelSn());
		msg.setBuyNum(castellan.getHasBuyNum());
		return msg.build();
	}
	
	/**
	 * 每个整点删除无用红包
	 */
	@Listener(EventKey.ResetDailyHour)
	public void cleanRedPacket(Param param){
		MaincityServiceProxy prx = MaincityServiceProxy.newInstance();
		prx.cleanRedPacket();
	}
	
	/**
	 * 每个整点更新一次,更换城主,被ooffilineGlobalService里面调用
	 */
	public void changeCastellan(){
		MaincityServiceProxy prx  = MaincityServiceProxy.newInstance();
		prx.updateDuke();
		prx.listenResult(this::_result_changeCastellan);
	}
	private void _result_changeCastellan(Param results, Param context) {
		// 玩家检查自己是否成为城主
		Map<Integer,Castellan> CastellanMap = results.get("castellanMap");
		for (Castellan castellan : CastellanMap.values()) {
			//查看玩家是否在线,在线就通知
			HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
			prx.getInfo(castellan.getHumanId());
			prx.listenResult(this::_result_changeCastellan2,"castellan",castellan);
			
		}
	}
	
	private void _result_changeCastellan2(Param results, Param context) {
		HumanGlobalInfo humanInfo = results.get();
		if(null != humanInfo){// 在线
			//发送通知
			HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(humanInfo.nodeId,humanInfo.portId, humanInfo.id);
			Castellan castellan = context.get("castellan");
			prxHumanObj.noticeMainCity(castellan);
		}
		
	}
	
	
	/**
	 * 更换世界boss城主
	 */
	@Listener(EventKey.ActInstWorldBossKiller)
	public void _listener_worldbossCastellan(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		Castellan castellan = new Castellan();
		castellan.setId(Port.applyId());
		castellan.setModelSn(humanObj.getHuman().getDefaultModelSn());
		castellan.setHumanId(humanObj.getHumanId());
		castellan.setName(humanObj.getHuman().getName());
		castellan.setType(ECastellanType.WorldBossDuke_VALUE);
		
		MaincityServiceProxy pxy = MaincityServiceProxy.newInstance();
		pxy.addWorldBossCastellan(castellan);
		pxy.listenResult(this::_result_worldbossCastellan, "humanObj", humanObj, "castellan", castellan);
	}
	private void _result_worldbossCastellan(Param results, Param context) {
		//更换城主完毕
		HumanObject humanObj = context.get("humanObj");
		Castellan castellan = context.get("castellan");
		
		SC_BecomeCastellan.Builder msg = SC_BecomeCastellan.newBuilder();
		DCastellanInfo dinfo = MainCityManager.inst().getCastellanMsg(castellan);
		msg.addInfo(dinfo);
		humanObj.sendMsg(msg);

		// 给所有在线玩家推送msg
		HumanGlobalServiceProxy pr = HumanGlobalServiceProxy.newInstance();
		pr.sendMsgToAll(new ArrayList<>(), msg.build());
		//发送系统邮件
		ConfMainCityShow conf = ConfMainCityShow.get(ECastellanType.WorldBossDuke_VALUE);
		String detail =  "{" + EMailType.MailMaster_VALUE + "|" + conf.sn  + "}";
		MailManager.inst()._msg_CSSendMail(humanObj.getHumanId(), ParamManager.mailMark,detail);
		
		Event.fire(EventKey.MainCityCastellan, "humanObj", humanObj, "castellanType", castellan.getType());
	}
	
	/**
	 * 发送魔法表情
	 * @param humanObj
	 * @param receiverId 接收魔法表情的玩家id
	 * @param winksSn
	 * @param sendType
	 */
	public void _msg_CSSendWinks(HumanObject humanObj, long receiverId, String receiverName, int winksSn, EWinksSendType sendType) {
		// 获得玩家vipLv
		int vipLv = humanObj.getHuman().getVipLevel();
		ConfVipUpgrade confVip = ConfVipUpgrade.get(vipLv);
		// 获得免费发送魔法表情次数
		int freeNum = confVip.winksNum;
		// 获得当前发送次数
		int sendNum = humanObj.getHuman().getDailySendWinksNum();
		
		ConfMagicExpression confME = ConfMagicExpression.get(winksSn);
		if (confME == null) {
			Log.game.error("=== 该魔法表情不存在 ===");
			humanObj.sendSysMsg(644102);
			return;
		}
		
		// 剩余免费次数
		int canFreeNum = 0;
		// 购买的次数
		int buyNum = 0;
		
		int nextNum = sendNum + 1;
		// 如果免费，不走消耗
		if (sendNum >= freeNum ) {
			int costMoney = RewardHelper.getCostGold(ECostGoldType.cityMagicLookCost, nextNum);
			if (!RewardHelper.checkAndConsume(humanObj, EMoneyType.gold_VALUE, costMoney, LogSysModType.TowerCardOpen)) {
				Log.tower.error("=== 发送失败，货币不足 ===");
				return;
			}
			// 购买次数 = 当前次数 - 免费的总次数
			buyNum = nextNum - freeNum;
		} else {
			// 可用免费次数 = 免费次数 - 当前次数 
			canFreeNum = freeNum - nextNum;
		}
		humanObj.getHuman().setDailySendWinksNum(nextNum);
		
		// 应答自己的消息
		SCSendWinks.Builder msg = SCSendWinks.newBuilder();
		msg.setCanFreeNum(canFreeNum);
		msg.setBuyNum(buyNum);
		msg.setRecieverName(receiverName);
		msg.setWinksSn(winksSn);
		humanObj.sendMsg(msg);
		
		// 通知对方的消息
		SCNTFSendWinks.Builder ntfMsg = SCNTFSendWinks.newBuilder();
		// 发送表情的玩家humanId
		ntfMsg.setSenderId(humanObj.getHumanId());
		ntfMsg.setSenderName(humanObj.getHuman().getName());
		ntfMsg.setWinksSn(winksSn);
		ntfMsg.setSendType(sendType);
		// 给对方发表情
		HumanGlobalServiceProxy proxy = HumanGlobalServiceProxy.newInstance();
		proxy.sendMsg(receiverId, ntfMsg.build());
	}
	
}
