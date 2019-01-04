package game.worldsrv.integration;

import game.platform.LogPF;
import game.worldsrv.character.HumanObject;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.config.ConfPayCharge;
import game.worldsrv.entity.Backlog;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.PayLog;
import game.worldsrv.enumType.BacklogType;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.human.HumanManager;
import game.worldsrv.payment.PaymentManager;
import game.worldsrv.support.AssetsTxtFix;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.RecordTransient;
import core.db.DBKey;
import core.dbsrv.DB;
import core.support.Config;
import core.support.ManagerBase;
import core.support.Param;
import core.support.observer.Listener;

/**
 * 充值逻辑类
 * @author 
 *
 */
public class PF_PAY_Manager extends ManagerBase{
	
	public static final String pidKey = "pid";// 标识
	public static final String productIdKey = "productId";// 标识
	public static final String userIdKey = "userId"; //帐号
	public static final String orderIdKey = "orderId";
	
//	public static final String successKey = "success";//返回结果 true,false
	public static final String successKey = "result";//返回结果 true,false
	public static final String reasonKey = "reason";//返回结果内容
	
	
	public static PF_PAY_Manager inst() {
		return inst(PF_PAY_Manager.class);
	}
	
	/**
	 * 充值事件
	 * @param param
	 */
	@Listener(value = EventKey.Pay)
	public void onPay(Param param) {
		JSONObject jo = param.get();
		LogPF.sdk.info(jo.toJSONString());
		String account = jo.getString(userIdKey);
		String orderId = jo.getString(orderIdKey);
		String productId = jo.getString(productIdKey);
		
		if(AssetsTxtFix.isInAccountWhiteList(account)){
			Port.getCurrent().returns(successKey, false);
			return;
		}
		
		//查询订单是否重复
		DB db = DB.newInstance(PayLog.tableName);
		String whereSql = Utils.createStr(" where orderId='{}'", orderId);
//		LogPF.platform.info("orderId====================================================== {}",orderId);
		List<String> columns = new ArrayList<>();
		columns.add(PayLog.K.id);
		db.findByQuery(false, whereSql, DBKey.COLUMN, columns);
		db.listenResult(this::_result_onPay1, pidKey, Port.getCurrent().createReturnAsync(),productIdKey,productId, "jo", jo);
		

		
//		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
//		prx.getInfo(roleId);
//		prx.listenResult(this::_result_onPay, pidKey, Port.getCurrent().createReturnAsync(), "jo", jo, "id", roleId);
	}
	
	private void _result_onPay1(Param results, Param context){
		long pid =  Utils.getParamValue(context, pidKey, 0L);
		
		List<RecordTransient> list = results.get();
		LogPF.platform.info("list====================================================== {}",list);
		if(list.size() > 0){
			//订单重复，直接返回
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "100"); //订单已存在
			return;
		}
		
		JSONObject jo =  Utils.getParamValue(context,"jo",null);
		if(null == jo){
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "300"); // JSONObject为空
			Log.game.error("PF_PAY_Manager._result_onPay1 jo=null");
			return;
		}
		long roleId = jo.getLongValue("roleId");
		String productId = jo.getString("productId");
		
		//查询玩家的信息
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(roleId);
		prx.listenResult(this::_result_onPay2, pidKey, pid, "jo", jo, "id", roleId,"productId",productId);//"productId",productId
	}
	private void _result_onPay2(Param results, Param context){
		long pid = Utils.getParamValue(context,pidKey,0L);
		JSONObject jo = Utils.getParamValue(context, "jo", null);
		String productId = jo.getString("productId");
		Long userId = jo.getLong("userId");	
		long roleId = jo.getLong("roleId");
		HumanGlobalInfo info = results.get();
		if(null == jo || productId == ""){
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "300"); //JSONObject为空或id<=0
			Log.game.error("PF_PAY_Manager._result_onPay2 jo=null or id={}",productId);
			return;
		}
		
		//台湾充值
		if(Config.GAME_PLATFORM_NAME.equals("tw")){
//			onPayTw(pid, jo, id, info);
			return;
		}

		//玩家在线，直接发货
		if(null != info){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
			prx.pay(Utils.toJSONString(jo));			
//			HumanObject humanObj = new(HumanObject);
//			PaymentManager.inst().onPay(null, sn);
			prx.listenResult(this::_result_onPay3, pid);
		}else{
			//玩家不在线，查询玩家的角色信息
			DB db = DB.newInstance(Human.tableName);
			String whereSql = Utils.createStr(" where id='{}'", roleId);
			List<String> columns = new ArrayList<>();
			columns.add(Human.K.id);
			db.findByQuery(false, whereSql, DBKey.COLUMN, columns);
			db.listenResult(this::_result_onPay4, pidKey, pid, "jo", jo, "id", userId);
		}

	}
	
	private void _result_onPay3(Param results, Param context){
		long pid = context.get();
//		boolean result = results.get();
		boolean result = results.get();
		//角色存在，添加代办
//		JSONObject jo = Utils.getParamValue(context, "jo", null);
//		Long id = jo.getLong("userId");
//		recordPayLog(id, jo, "已发送");//充值记录数据库
	
		
		Port.getCurrent().returnsAsync(pid, successKey, result);
	}
	
	private void _result_onPay4(Param results, Param context){
		long pid = Utils.getParamValue(context, pidKey, 0L);
		JSONObject jo = Utils.getParamValue(context, "jo", null);
//		long id = Utils.getParamValue(context, "id", 0L);
//		Integer id = jo.getInteger("sn");
		Long id = jo.getLong("userId");
		long roleId = jo.getLong("roleId");
		if(null == jo || id <= 0){
			Log.game.error("PF_PAY_Manager._result_onPay4 jo=null or uid={}, roleid={}", id, roleId);
			return;
		}
		List<RecordTransient> list = results.get();
		if(list.isEmpty()){
			//角色不存在，返回
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "角色不存在");
			Log.game.error("PF_PAY_Manager._result_onPay4 角色不存在 id={}, roleid={}", id, roleId);
			return;
		}

		//角色存在，添加代办
		recordPayLog(id, jo, "代办中");//充值记录数据库
		
		//String productId = jo.getString("productId");
        String _productId = jo.getString("productId");
		ConfPayCharge cpc = ConfPayCharge.getBy("androidProductId", _productId);
		if(cpc == null){
			//sn不存在，返回
			Port.getCurrent().returnsAsync(pid, successKey, false, reasonKey, "sn不存在");
			Log.game.error("PF_PAY_Manager._result_onPay4 sn不存在 id={}, roleid={}", id, roleId);
			return;
		}
		int sn = cpc.getFieldValue("sn");	
		
		// 添加一个待办事项：邮件提醒
		JSONObject joPay = new JSONObject();
//		joPay.put("propId", jo.get("propId"));
//		joPay.put("chargePrice", jo.get("chargePrice"));
//		joPay.put("actualPrice", jo.get("actualPrice"));
//		joPay.put("orderId", jo.get("orderId"));
//		joPay.put("id", jo.get("id"));
		joPay.put("sn", sn);
//		joPay.put("chargePrice", jo.get("chargePrice"));
//		joPay.put("actualPrice", jo.get("actualPrice"));
//		joPay.put("orderId", jo.get("orderId"));
		joPay.put("id", jo.get("id"));
		HumanManager.inst().saveOneBacklog(roleId, BacklogType.Pay, joPay);
		Port.getCurrent().returnsAsync(pid, successKey, true);
	}
	
	/**
	 * 充值代办事件
	 */
	public void backlogToPay(HumanObject humanObj, Backlog backlog) {
		if(null == humanObj || null == backlog){
			Log.game.error("PF_GM_Manager.backlogToPay humanObj={},backlog={}", humanObj, backlog);
			return;
		}
		JSONObject jo = Utils.toJSONObject(backlog.getParamJSON());
		
		//此处为充值代办，做了一个兼容处理。
//		int sn = 0;
//		String[] propId = jo.getString("propId").split("_");
//		if(propId.length > 1){
//			sn = Integer.parseInt(propId[1]);
//		} else {
//			sn = Integer.parseInt(propId[0]);
//		}
		//Event.fire(EventKey.Pay, jo); //发送支付通知 ??test
		
		//VipStoreManager.inst()._msg_CSBuyGold(humanObj, sn);
		int sn = jo.getInteger("sn");
		if(sn > 0) {
//			onPay(humanObj, jo, sn);
			PaymentManager.inst().onPay(humanObj, sn);
		}
	}

	/**
	 * 充值逻辑
	 * @param humanObj
	 * @param jo
	 * @param sn
	 */
	private void charge(HumanObject humanObj, JSONObject jo, int sn) {
		//如果送好友月卡
//		if(sn >= 100){
//			friendMonthCardCharge(humanObj, jo, sn, false);
//			
//		}
		/*
		int confPrice = 0;
		if(sn >= 100){
			confPrice = ConfPayMonthCard.get(sn).rmb;
		}else{
			confPrice = ConfPayCharge.get(sn).rmb;
			
		}
		int actualPrice;
		Object price = jo.get("actualPrice");
		if(price == null){
			actualPrice = jo.getIntValue("chargePrice")/100;
		}else{
			actualPrice = jo.getIntValue("actualPrice")/100;
		}
		
		Long addCount = null;
		
		if(confPrice == actualPrice || C.IS_OPENGM){
			//充值校验正确(充值金额和配置表的金额相同)
			addCount = PaymentManager.inst().pay(humanObj, sn);
			if(addCount == null){
				String sql = Utils.createStr("update {} set status='{}' where orderId='{}'", PayLog.tableName, "配置表错误", jo.getString("orderId"));
				DB db = DB.newInstance(PayLog.tableName);
				db.sql(false, true, sql);
				Inform.user(humanObj.id, Inform.提示操作, "配置表错误");
				return;
			}
			
			String sql = Utils.createStr("update {} set status='{}' where orderId='{}'", PayLog.tableName, "充值成功", jo.getString("orderId"));
			DB db = DB.newInstance(PayLog.tableName);
			db.sql(false, true, sql);
			
			uploadChargeRecord(humanObj.id, jo, addCount);
			
		}else{
			//充值校验不正确(实际金额大于配置表的金额：充值金额=充一个符合条件的商品+多出的金额)
			int extraRmb = -1;
			if(actualPrice > confPrice){
				extraRmb = actualPrice - confPrice;
				//充值错误,但是找到对应金额
				addCount = PaymentManager.inst().pay(humanObj, sn);
				if(addCount == null){
					String sql = Utils.createStr("update {} set status='{}' where orderId='{}'", PayLog.tableName, "配置表错误", jo.getString("orderId"));
					DB db = DB.newInstance(PayLog.tableName);
					db.sql(false, true, sql);
					Inform.user(humanObj.id, Inform.提示错误, "配置表错误");
					return;
				}
				
				uploadChargeRecord(humanObj.id, jo, addCount);
				
			}else{
				//实际金额小于配置表的金额(充值金额=实际金额)
				extraRmb = actualPrice;
			}
			
			//如果实际付费比商品价格高，则多出来的钱*10转成元宝
			if(extraRmb > 0){
				//配置表里没找到对应金额
				addCount = errorCharge(humanObj, null, extraRmb);
				Inform.user(humanObj.id, Inform.提示错误, "充值异常");
				
				String sql = Utils.createStr("update {} set status='{}' where orderId='{}'", PayLog.tableName, "充值" + actualPrice + "元,有异常", jo.getString("orderId"));
				DB db = DB.newInstance(PayLog.tableName);
				db.sql(false, true, sql);
					
			}
		}
		
		//返回前段充值信息
		if(sn >= 100){
			SCCharge100.Builder msg = SCCharge100.newBuilder();
			msg.setOrderId(jo.getString("orderId"));
			msg.setSn(sn);
			
			humanObj.sendMsg(msg);
		}else{
			SCCharge.Builder msg = SCCharge.newBuilder();
			msg.setOrderId(jo.getString("orderId"));
			if(addCount == null){
				addCount = 0L;
			}
			msg.setGold(addCount);
			msg.setSn(sn);
			
			humanObj.sendMsg(msg);
		}
		*/
		//TODO 改变玩家充值活动状态
	}
//	
//	/**
//	 * 不匹配配置表的金额部分，充值给元宝
//	 * @param humanObj
//	 * @param chargeInfo
//	 * @param chargePrice
//	 * @return
//	 */
//	public long errorCharge(HumanObject humanObj, ChargeInfoVO chargeInfo, int chargePrice) {
//		if(chargePrice < 1){
//			return 0;
//		}
//		int gold = chargePrice*10;
//		RewardHelper.produceMoneyAdd(humanObj, ProduceMoneyKey.gold, gold, MoneyAddLogKey.错误充值);
//		
//		//给vip经验
//		PaymentManager.inst().setVipLevel(humanObj, chargeInfo, gold);
//		
//		return gold;
//		
//	}
	/**
	 * 充值记录
	 * @param id
	 * @param jo
	 * @param status
	 */
	public PayLog recordPayLog(long id, JSONObject jo, String status) {
		PayLog log = new PayLog();
		log.setId(Port.applyId());
		log.setActualPrice(jo.getString(PayLog.K.actualPrice));
		log.setChannelId(jo.getString(PayLog.K.channelId));
		log.setChargePrice(jo.getString(PayLog.K.chargePrice));
		log.setChargeUnitId(jo.getString(PayLog.K.chargeUnitId));
		log.setCurrencyType(jo.getString(PayLog.K.currencyType));
		log.setDeviceGroupId(jo.getString(PayLog.K.deviceGroupId));
		log.setLocaleId(jo.getString(PayLog.K.localeId));
		log.setOrderId(jo.getString(PayLog.K.orderId));
		log.setUserId(jo.getString(PayLog.K.userId));
		log.setServiceId(jo.getString(PayLog.K.serviceId));
		log.setServerId(jo.getString(PayLog.K.serverId));
		log.setRoleId(jo.getString(PayLog.K.roleId));
		log.setPayChannelId(jo.getString(PayLog.K.payChannelId));
		log.setSign(jo.getString(PayLog.K.sign));
		log.setPropId(jo.getString(PayLog.K.propId));
		log.setDeviceGroupId(jo.getString(PayLog.K.deviceGroupId));
		log.setTime(Utils.formatTime(Port.getTime(), "yyyy-MM-dd HH:mm:ss"));
		log.setStatus(status);
		
		log.persist();
		return log;
	}
//	
//	/**
//	 * 上传充值记录
//	 * @param humanId
//	 * @param jo
//	 * @param addCount
//	 */
//	public void uploadChargeRecord(long humanId, JSONObject jo, long addCount) {
//		if(!"appstore".equals(C.GAME_PLATFORM_NAME.toLowerCase())){
//			return;
//		}
//		
//		JSONArray ja = new JSONArray();
//		JSONObject json = new JSONObject();
//		json.put("msgID",  String.valueOf(Port.applyId()));
//		json.put("status",  successKey);
//		if("appstore".equals(C.GAME_PLATFORM_NAME.toLowerCase())){
//			json.put("OS",  "appstore");
//		}else{
//			json.put("OS",  "android");
//		}
//		json.put("accountID",  String.valueOf(humanId));
//		json.put("orderID",  jo.get("orderId"));
//		json.put("currencyAmount",  jo.getIntValue("chargePrice")/100);
//		json.put("currencyType",  "CNY");
//		json.put("virtualCurrencyAmount",  addCount);
////		json.put("chargeTime", Port.getTime());
//		
//		ja.add(json);
//		
//		String giftPort = ConstPf.PORT_GIFT_PREFIX + new Random().nextInt(ConstPf.PORT_STARTUP_NUM_GIFT);
//		GiftServiceProxy prx = GiftServiceProxy.newInstance(ConstPf.NODE_ID, giftPort, ConstPf.SERV_GIFT);
//		prx.uploadChargeRecord(ja.toJSONString());
//		
//	}

}
