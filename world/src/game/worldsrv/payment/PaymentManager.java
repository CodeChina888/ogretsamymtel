package game.worldsrv.payment;

import game.msg.Define.DPayLog;
import game.msg.Define.EMoneyType;
import game.msg.MsgPaymoney.SCCardChargeSuccess;
import game.msg.MsgPaymoney.SCOpenPayUI;
import game.msg.MsgPaymoney.SCOpenVipUI;
import game.msg.MsgPaymoney.SCPayCharge;
import game.msg.MsgPaymoney.SCPayChargeIOS;
import game.msg.MsgPaymoney.SCPayCheckCode;
import game.msg.MsgPaymoney.SCPayLogs;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfPayCharge;
import game.worldsrv.config.ConfPayMonthCard;
import game.worldsrv.config.ConfVipUpgrade;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.IOSPayOrder;
import game.worldsrv.entity.PayCheckCode;
import game.worldsrv.entity.PayLog;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.enumType.PayChargeType;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.inform.InformManager;
import game.worldsrv.integration.PF_PAY_Manager;
import game.worldsrv.item.Item;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.keyActivate.KeyActivateServiceProxy;
import game.worldsrv.mail.MailManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.AssetsTxtFix;
import game.worldsrv.support.Log;
import game.worldsrv.support.LogOpUtils;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.support.Config;
import core.support.ManagerBase;
import core.support.Param;
import core.support.Utils;
import core.support.observer.Listener;

/**
 * 充值逻辑
 */
public class PaymentManager extends ManagerBase {
	
	public static final int CHARGE_TYPE_NORMRL = 1; // 普通充值界面的类型
	public static final int CHARGE_TYPE_GROW = 2; // 成长的基金的类型
	
	//IOS充值校验结果
	public static final int IOS_PAY_SUCCESS = 0;		//充值成功
	public static final int IOS_PAY_DELAY = 1;			//网络异常，验证超时，延迟检查
	public static final int IOS_PAY_ERROR = 200;		//充值失败
	public static final int IOS_PAY_REPEAT = 201;		//重复的订单
	public static final int IOS_PAY_FAIL = 202;				//无效的订单
	public static final int IOS_PAY_UNKNOWN = 210;				//未知错误
	
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static PaymentManager inst() {
		return inst(PaymentManager.class);
	}

	/**
	 * 玩家其它数据加载开始：加载玩家的充值记录
	 */
	@Listener(EventKey.HumanDataLoadOther)
	public void _listener_HumanDataLoadOther(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_HumanDataLoadOther humanObj is null");
			return;
		}
		DB dbPrx = DB.newInstance(PayLog.tableName);
		dbPrx.findBy(false, PayLog.K.roleId, humanObj.getHumanId());
		dbPrx.listenResult(this::_result_loadHumanPayLog, "humanObj", humanObj);
	}
	private void _result_loadHumanPayLog(Param results, Param context) {
		List<Record> records = results.get();
		if (records == null) {// 可以没有充值记录
			return;
		}
		HumanObject humanObj = Utils.getParamValue(context, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_result_loadHumanPayLog humanObj is null");
			return;
		}
		humanObj.payLogs.clear();
		for (Record record : records) {
			PayLog payLog = new PayLog(record);
			humanObj.payLogs.add(payLog);
		}
	}
	
	/**
	 * 每个整点执行一次
	 */
	@Listener(EventKey.ResetDailyHour)
	public void _listener_ResetDailyHour(Param param) {
		HumanObject humanObj = Utils.getParamValue(param, "humanObj", null);
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		int hour = Utils.getHourOfDay(Port.getTime());
		if (hour == ParamManager.dailyHourReset) {
			// 每日重置：检查充值返还
			checkReturnPayment(humanObj);
		}
	}
	/**
	 * 检查是否有可领取的返还类型充值,如果有将资源(现在是绑定元宝)发送到邮箱(如:月卡)
	 * 创角后首次登陆
	 * @param param
	 */
	@Listener(EventKey.HumanFirstLogin)
	public void _listener_HumanFirstLogin(Param param) {
		HumanObject humanObj = param.get("humanObj");
		if (humanObj == null) {
			Log.game.error("===_listener_ResetDailyHour humanObj is null");
			return;
		}
		// 首次登陆检查充值返还
		checkReturnPayment(humanObj);
		if (Config.PAYBACK) {
            getAccoutPayBackItem(humanObj);
        }
	}
	/**
	 * 检查充值返还
	 */
	private void checkReturnPayment(HumanObject humanObj) {
		String json = humanObj.getHuman().getChargeInfo();
		List<ChargeInfoVO> list = ChargeInfoVO.jsonToList(json);
		ConfPayCharge conf = null;
		for (ChargeInfoVO charge : list) {
			conf = ConfPayCharge.get(charge.sn);
			if (conf != null && conf.type == 2 &&  conf.retDay > 0) {
				//每日返还的充值
				if (charge.getLastDay(conf.sn) > 0) {
//					if(Utils.getDaysBetween(Port.getTime(), charge.takeTime) ==  0){
					if(Utils.getDaysBetween(Port.getTime(), charge.tt) ==  0){
						//同一天已经领取过了，跳过
						continue;
					}
					// 通过邮件发送钻石返还
//					charge.takeNum++;
//					charge.takeTime = Port.getTime();
//					if(charge.takeNum == conf.retDay){
//						charge.takeNum = 0;//返还完毕后，将takeNum设置为0
//						charge.takeTime = 0;
						
					charge.tn++;
					charge.tt = Port.getTime();
					if(charge.tn == conf.retDay){
						charge.tn = 0;//返还完毕后，将takeNum设置为0
						charge.tt = 0;	
					}
					// 发送邮件
//					MailManager.inst().sendMail(humanObj.id,
//							MailManager.SYS_SENDER,
//							Inform.getServerData(9020),
//							Inform.getServerData(9021),
//							new int[] {MoneyItemType.Diamond},
//							new int[] { conf.retGold }, true,MailManager.SYS_SENDER_NAME);
				}
			}
		}

		humanObj.getHuman().setChargeInfo(ChargeInfoVO.listToJson(list));
		// 重置完成，记录一下
		//humanObj.setModule0Reset(ModuleResetTypeKey.returnPay.getS());
	}
	
	/**
	 * IOS充值校验
	 * @param humanObj
	 * @param sn
	 * @param order
	 * @param receiptData
	 */
	public void onPayIOS(HumanObject humanObj, int sn,String order, String receiptData) {
		//检查订单是否有效
		String receiptMd5 = Utils.md5(receiptData);
		DB db = DB.newInstance(IOSPayOrder.tableName);
		//先查询数据库校验数据库订单
		String sql = Utils.createStr("where (`{}`=? or `{}`=?)", IOSPayOrder.K.Order, IOSPayOrder.K.ReceiptMd5);
		db.getByQuery(false, sql, order, receiptMd5);
		db.listenResult(this::_result_iosPayDBCheck, "humanObj", humanObj, "sn",sn, "order",order,"receiptMd5",receiptMd5,"receiptData", receiptData);
	}
	//校验数据库订单结果
	public void _result_iosPayDBCheck(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		int sn = context.get("sn");
		String order =  context.get("order");
		
		List<Record> list = results.get();
		if(list != null && list.size() != 0){
			SCPayChargeIOS.Builder msg = SCPayChargeIOS.newBuilder();
			msg.setOrder(order);
			msg.setSn(sn);
			msg.setResultCode(IOS_PAY_REPEAT);
			humanObj.sendMsg(msg);
			return;
		}
		String receiptData =  context.get("receiptData");
		//远程调用加入队列进行处理
        PaymentServiceProxy.newInstance().addIOSPayOrder(humanObj.getHumanId(),sn,order,receiptData );
	}
	
	/**
	 * gm补发
	 */
	public void _gm_PayCharge(HumanObject humanObj, int sn) {
		PaymentManager.inst().onPay(humanObj, sn);
		PaymentManager.inst().creatPayLog(humanObj, sn, "GM充值");
	}
	
	/**
	 * 玩家充值补发
	 */
	public void _msg_CSGsPayChager(HumanObject humanObj, int sn) {
		if(Config.GAME_PLATFORM_NAME.equals("tw")){
			sn = sn - 10000;
		}
		
		//充值
		PaymentManager.inst().pay(humanObj, sn);
		
		//gm调试命令打开，需要记录日志
		if(ParamManager.openGM){
			PaymentManager.inst().creatPayLog(humanObj, sn, "GM测试充值");
			return ;
		}
		
		JSONObject json = Utils.toJSONObject(humanObj.getHuman().getPayCharge());
		int num = 0;
		if(json.containsKey(String.valueOf(sn))){
			num = json.getIntValue(String.valueOf(sn));
		}
		if(num > 0){   //充值补发
			PaymentManager.inst().pay(humanObj, sn);
			PaymentManager.inst().creatPayLog(humanObj, sn, "充值补发");
			if(num == 1){
				json.remove(String.valueOf(sn));
			}else{
				json.put(String.valueOf(sn), num-1);
			}
			humanObj.getHuman().setPayCharge(json.toJSONString());
			return ;
		}
		if(humanObj.getHuman().isGs()){
			PaymentManager.inst().gs(humanObj, sn);
			return;
		}
		if(humanObj.getHuman().isFuli()){
			PaymentManager.inst().fuli(humanObj);
			return;
		}
	}
	
	
	/** 
	 * 向指定 URL 发送POST方法的请求 
	 *  
	 * @param url 
	 *            发送请求的 URL 
	 * @param param 
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。 
	 * @return 所代表远程资源的响应结果 
	 */  
	public static String sendPost(String url, String param) {  
	    StringBuilder sb = new StringBuilder();  
	    PrintWriter out = null;  
	    BufferedReader in = null;  
	    try {  
	        URL realUrl = new URL(url);  
	        // 打开和URL之间的连接  
	        URLConnection conn = realUrl.openConnection();  
	        // 设置通用的请求属性  
	        conn.setRequestProperty("accept", "*/*");  
	        conn.setRequestProperty("connection", "Keep-Alive");  
	        conn.setRequestProperty("user-agent",  
	                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");  
	        // 发送POST请求必须设置如下两行  
	        conn.setDoOutput(true);  
	        conn.setDoInput(true);  
	        // 获取URLConnection对象对应的输出流  
	        out = new PrintWriter(conn.getOutputStream());  
	        // 发送请求参数  
	        out.print(param);  
	        // flush输出流的缓冲  
	        out.flush();  
	        // 定义BufferedReader输入流来读取URL的响应  
	        in = new BufferedReader(  
	                new InputStreamReader(conn.getInputStream()));  
	        String line;  
	        sb = new StringBuilder();  
	        while ((line = in.readLine()) != null) {  
	            sb.append(line);  
	        }  
	    } catch (Exception e) {  
	        System.out.println("发送 POST 请求出现异常！"+e);  
	        e.printStackTrace();  
	    }  
	    //使用finally块来关闭输出流、输入流  
	    finally{  
	        try{  
	            if(out!=null){  
	                out.close();  
	            }  
	            if(in!=null){  
	                in.close();  
	            }  
	        }  
	        catch(IOException ex){  
	            ex.printStackTrace();  
	        }  
	    }  
	    return sb.toString();  
	}
	
	public static String sendPost2(String url, String cookie) {
		StringBuilder sb = new StringBuilder();
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.addRequestProperty("Cookie", cookie);
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(3000);
			conn.connect();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			sb = new StringBuilder();
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return sb.toString();
	} 
	
	/**
	 * 玩家充值接口
	 * @param humanObj
	 * @param sn
	 * @return null->失败:1.未找到充值商品  2.月卡未过期，重复购买月卡  3.重复购买成长基金
	 */
	public Long pay(HumanObject humanObj, int sn) {
		Human human = humanObj.getHuman();
		// 大于等于100，送给别人的充值
		if (sn >= 100) {
			return otherPay(humanObj, sn);
		}
		if (Config.GAME_PLATFORM_NAME.equals("tw")) {
			sn += 10000;
		}
		ConfPayCharge confPay = ConfPayCharge.get(sn);
		if (confPay == null) {
			Log.table.error("===ConfPayCharge no find sn={}", sn);
			return null;
		}
		String json = human.getChargeInfo();

		List<ChargeInfoVO> list = ChargeInfoVO.jsonToList(json);
		ChargeInfoVO chargeVO = ChargeInfoVO.getChargeInfo(list, sn);
		if (chargeVO == null) {
			chargeVO = new ChargeInfoVO();
			list.add(chargeVO);
		}
		if (confPay.retDay > 0) {
			// 对于按天返还的充值不能重复
			if (confPay.type != PayChargeType.NormalPay.value() && chargeVO.num > 0 && chargeVO.getLastDay(sn) > 0) {
//				Inform.sendInform(humanObj.id, 9019, confPay.itemName);
				return null;
			}
		}
		
		//成长基金只能购买一次
		if(confPay.type != PayChargeType.NormalPay.value()){
			if(chargeVO.num > 0){
//				Inform.sendInform(humanObj.id, 9024, confPay.name);
				return null;
			}
		}
		// 充值获得的元宝
		long gold = confPay.gold;
		// 赠送的绑定元宝，单独记录
		int sendBindGold = 0;

		if (confPay.gift > 0) {
			// 每次充值都赠送的绑定元宝
			sendBindGold = confPay.gift;
		}
		

		if (confPay.giftOnce > 0) {
			// 只有在第一次充值该金额才赠送绑定元宝的情况
			if (chargeVO.num == 0) {
				sendBindGold = confPay.giftOnce;
			}
		}

		chargeVO.sn = sn;
		chargeVO.num += 1;
		if (confPay.type != 1 && confPay.retDay > 0) {
			//对于每日返还的立即返还一天
//			chargeVO.takeNum++;
//			chargeVO.takeTime = Port.getTime();
			chargeVO.tn++;
			chargeVO.tt = Port.getTime();
			// 特殊邮件内容：{MailTemplate.sn|}
//			String detail = "{" + EMailType.Pay.value() + "}";
//			MailManager.inst().sendSysMail(humanObj.id, ParamManager.mailMark, detail, 
//					ProduceManager.inst().produceItem(new int[]{EMoneyType.gold_VALUE}, new int[]{confPay.retGold}));
		}
		
		human.setChargeInfo(ChargeInfoVO.listToJson(list));
		if (AssetsTxtFix.isInAccountChargeList(human.getAccountId())) {
			return gold + sendBindGold;
		}
		if (gold > 0) {
			//System.out.println("gold============197======="+gold);
			RewardHelper.reward(humanObj, EMoneyType.gold_VALUE, (int)gold, LogSysModType.VipPay);
		}
		// 如果赠送的大于0,注意:赠送的是绑定元宝
		if (sendBindGold > 0) {
			RewardHelper.reward(humanObj, EMoneyType.gold_VALUE, sendBindGold, LogSysModType.VipPayBack);
		}

		// 设置VIP等级
		setVipLevel(humanObj, chargeVO, confPay.vipExp);
		
		//充值事件 
		Event.fire(EventKey.PayNotify, "humanObj", humanObj, "sn", sn, "gold", gold, "bindGold", sendBindGold);
		
		return gold + sendBindGold;
	}
	
	/**
	 * 玩家充值接口
	 * @param humanObj
	 * @param sn ConfPayCharge.sn
	 * @return null->失败:1.未找到充值商品  2.月卡未过期，重复购买月卡  3.重复购买成长基金
	 */
	public Long onPay(HumanObject humanObj, int sn) {
		Human human = humanObj.getHuman();
		ConfPayCharge confPay = ConfPayCharge.get(sn);
		if (confPay == null) {
			Log.table.error("===ConfPayCharge no find sn={}", sn);
			return null;
		}
		String json = human.getChargeInfo();

		List<ChargeInfoVO> list = ChargeInfoVO.jsonToList(json);
		ChargeInfoVO chargeVO = ChargeInfoVO.getChargeInfo(list, sn);
		if (chargeVO == null) {
			chargeVO = new ChargeInfoVO();
			list.add(chargeVO);
		}
		if (confPay.retDay > 0) {
			// 对于按天返还的充值不能重复
			if (confPay.type != PayChargeType.NormalPay.value() && chargeVO.num > 0 && chargeVO.getLastDay(sn) > 0) {
				Log.game.info("对于按天返还的充值不能重复");
				return null;
			}
		}
		
		//成长基金只能购买一次
//		if(confPay.type != PayChargeType.NormalPay.value()){
//			if(chargeVO.num > 0){
//				humanObj.sendSysMsg(9024);//	{}只能购买一次！
//				return null;
//			}
//		}

		// 充值获得的元宝
		long gold = confPay.gold;
		// 赠送的绑定元宝，单独记录
		int sendBindGold = 0;

		if (confPay.gift > 0) {
			// 每次充值都赠送的绑定元宝
			sendBindGold = confPay.gift;
		}
		

		if (confPay.giftOnce > 0) {
			// 只有在第一次充值该金额才赠送绑定元宝的情况
			if (chargeVO.num == 0) {
				sendBindGold = confPay.giftOnce;
			}
		}

		chargeVO.sn = sn;
		chargeVO.num += 1;

		if (confPay.type != PayChargeType.NormalPay.value() && confPay.retDay > 0) {
			//对于每日返还的立即返还一天 (旧版本立即返还一天)
//			chargeVO.tn++;
//			chargeVO.tt = Port.getTime();
			// 特殊邮件内容：{MailTemplate.sn|}
//			String detail = "{" + EMailType.ActOpenSeven.value() + "}";
//			MailManager.inst().sendSysMail(humanObj.id, ParamManager.mailMark, detail, 
//					ProduceManager.inst().produceItem(new int[]{EMoneyType.gold_VALUE}, new int[]{confPay.retGold}));
		}
		
		//设置首充状态 TODO 由于加载顺序改变 humanObj.getHumanExtInfo()有可能还未加载
		int state = humanObj.getHumanExtInfo().getFirstChargeRewardState();
		if(state <= 0){ //已领取
			humanObj.getHumanExtInfo().setFirstChargeRewardState(1);
		}
		
		
		human.setChargeInfo(ChargeInfoVO.listToJson(list));
		if(AssetsTxtFix.isInAccountChargeList(human.getAccountId())){
			return gold + sendBindGold;
		}
		if (gold > 0) {
			RewardHelper.reward(humanObj, EMoneyType.gold_VALUE, (int)gold, LogSysModType.VipPay);
		}
		// 如果赠送的大于0,注意:赠送的是绑定元宝
		if (sendBindGold > 0) {
			RewardHelper.reward(humanObj, EMoneyType.gold_VALUE, sendBindGold, LogSysModType.VipPayBack);
		}
		

		// 设置VIP等级
		setVipLevel(humanObj, chargeVO, confPay.vipExp);
		
				
		//充值事件 
		Event.fire(EventKey.PayNotify, "humanObj", humanObj, "sn", sn, "gold", gold, "bindGold", sendBindGold);

//		PaymentManager.inst().creatPayLog(humanObj, sn, "充值");
		
		return gold + sendBindGold;
	}
	
	/**
	 * Gs,福利号充值接口
	 */
	public Long payGs_Fuli(HumanObject humanObj, int sn) {
		Human human = humanObj.getHuman();
		// 大于等于100，送给别人的充值
		if (sn >= 100) {
			return otherPay(humanObj, sn);
		}
		if (Config.GAME_PLATFORM_NAME.equals("tw")) {
			sn += 10000;
		}
		ConfPayCharge confPay = ConfPayCharge.get(sn);
		if (confPay == null) {
			Log.table.error("===ConfPayCharge no find sn={}", sn);
			return null;
		}
		String json = human.getChargeInfo();

		List<ChargeInfoVO> list = ChargeInfoVO.jsonToList(json);
		ChargeInfoVO chargeVO = ChargeInfoVO.getChargeInfo(list, sn);
		if (chargeVO == null) {
			chargeVO = new ChargeInfoVO();
			list.add(chargeVO);
		}
		if (confPay.retDay > 0) {
			// 对于按天返还的充值不能重复
			if (confPay.type != PayChargeType.NormalPay.value() && chargeVO.num > 0 && chargeVO.getLastDay(sn) > 0) {
//				Inform.sendInform(humanObj.id, 9019, confPay.name);
				return null;
			}
		}
		
		//成长基金只能购买一次
		if(confPay.type != PayChargeType.NormalPay.value()){
			if(chargeVO.num > 0){
//				Inform.sendInform(humanObj.id, 9024, confPay.name);
				return null;
			}
		}

		// 充值获得的元宝
		long gold = confPay.gold;
		if (gold > 0) {   //gs，福利号只给绑定元宝
			RewardHelper.reward(humanObj, EMoneyType.gold_VALUE, (int)gold, LogSysModType.VipPay);
		}

		// 赠送的绑定元宝，单独记录
		int sendBindGold = 0;

		if (confPay.gift > 0) {
			// 每次充值都赠送的绑定元宝
			sendBindGold = confPay.gift;
		}

		if (confPay.giftOnce > 0) {
			// 只有在第一次充值该金额才赠送绑定元宝的情况
			if (chargeVO.num == 0) {
				sendBindGold = confPay.giftOnce;
			}
		}

		chargeVO.sn = sn;
		chargeVO.num += 1;
		if (confPay.type != PayChargeType.NormalPay.value() && confPay.retDay > 0) {
			//对于每日返还的立即返还一天
//			chargeVO.takeNum++;
//			chargeVO.takeTime = Port.getTime();
			chargeVO.tn++;
			chargeVO.tt = Port.getTime();
			// 特殊邮件内容：{MailTemplate.sn|}
//			String detail = "{" + EMailType.ActOpenSeven.value() + "}";
//			MailManager.inst().sendSysMail(humanObj.id, ParamManager.mailMark, detail, 
//					ProduceManager.inst().produceItem(new int[]{EMoneyType.gold_VALUE}, new int[]{confPay.retGold}));
		}
		human.setChargeInfo(ChargeInfoVO.listToJson(list));

		// 如果赠送的大于0,注意:赠送的是绑定元宝
		if (sendBindGold > 0) {
			RewardHelper.reward(humanObj, EMoneyType.gold_VALUE, sendBindGold, LogSysModType.VipPayBack);
		}

		// 设置VIP等级
		setVipLevel(humanObj, chargeVO, confPay.vipExp);
		
		//充值事件 
		Event.fire(EventKey.PayNotify, "humanObj", humanObj, "sn", sn, "gold", gold, "bindGold", sendBindGold);


		return gold + sendBindGold;
	}

	/**
	 * 充值日志+修改玩家每日充值信息
	 */
	@Listener(EventKey.PayNotify)
	public void _listener_PAY(Param param) {
		HumanObject humanObj = param.get("humanObj");
		int sn = param.get("sn");
		ConfPayCharge confPay = ConfPayCharge.get(sn);
		if (confPay == null) {
			return;
		}
		long gold = param.get("gold");
		int bindGold = param.get("bindGold");
		Human human = humanObj.getHuman();
		//添加充值日志
		LogOpUtils.LogRecharge(human, confPay.rmb, gold + bindGold);
		
		
	}
	
	/**
	 * 给别人充值
	 * @param humanObj
	 * @param sn
	 * @return
	 */
	public Long otherPay(HumanObject humanObj, int sn) {
		switch (sn) {
		case 100:
			return friendCardPay(humanObj, sn);
		default:
			return unionPay(humanObj, sn);
		}
	}

	/**
	 * 购买帮会礼包
	 * 
	 * @param humanObj
	 * @param sn
	 * @return
	 */
	private Long unionPay(HumanObject humanObj, int sn) {
		if (Config.GAME_PLATFORM_NAME.equals("tw")) {
			sn += 10000;
		}
		ConfPayMonthCard conf = ConfPayMonthCard.get(sn);
		if (conf == null) {
			Log.table.error("===ConfPayMonthCard no find sn={}", sn);
			return 0L;
		}
		ProduceVo itemProduce = new ProduceVo(conf.itemSn, 1);
		List<ProduceVo> list = new ArrayList<ProduceVo>();
		list.add(itemProduce);
		ProduceManager.inst() .giveProduceItem(humanObj, list, LogSysModType.VipPay);

		// 加vip
		int gold = conf.gold;
		PaymentManager.inst().setVipLevel(humanObj, null, gold);

		Item item = humanObj.itemBag.getBySn(conf.itemSn);
		if (item != null) {

			SCCardChargeSuccess.Builder msg = SCCardChargeSuccess.newBuilder();
			msg.setCardSn(sn);
			msg.setCardNum(item.getNum());
			humanObj.sendMsg(msg);
		}
		humanObj.sendSysMsg(9001);// 帮会红包购买成功，快去送给帮会的基友们吧！

		return 0L;
	}

	/**
	 * 购买好友月卡
	 * 
	 * @param humanObj
	 * @param sn
	 * @return
	 */
	private Long friendCardPay(HumanObject humanObj, int sn) {
		if (Config.GAME_PLATFORM_NAME.equals("tw")) {
			sn += 10000;
		}
		ConfPayMonthCard conf = ConfPayMonthCard.get(sn);
		if (conf == null) {
			Log.table.error("===ConfPayMonthCard no find sn={}", sn);
			return 0L;
		}
		
		ProduceVo itemProduce = new ProduceVo(conf.itemSn, 1);
		List<ProduceVo> list = new ArrayList<ProduceVo>();
		list.add(itemProduce);
		ProduceManager.inst().giveProduceItem(humanObj, list, LogSysModType.VipPay);

		// 加vip
		int gold = conf.gold;
		PaymentManager.inst().setVipLevel(humanObj, null, gold);

		Item item = humanObj.itemBag.getBySn(conf.itemSn);
		if (item != null) {

			SCCardChargeSuccess.Builder msg = SCCardChargeSuccess.newBuilder();
			msg.setCardSn(sn);
			msg.setCardNum(item.getNum());
			humanObj.sendMsg(msg);
		}
		humanObj.sendSysMsg(9002);// 商品购买成功，开始赠送给你的小伙伴吧！

		return 0L;
	}
	
	/**
	 * GM修改vip等级
	 * @param humanObj
	 * @param allCharge
	 */
	public void vipLevelGM(HumanObject humanObj,int allCharge){
		setVipLevel(humanObj, allCharge);
	}
	/**
	 * GM修改vip等级
	 */
	public void setVipLevel(HumanObject humanObj, int allCharge) {
		Human human = humanObj.getHuman();
		human.setChargeGold(allCharge);
		// 修改充值情况 
		this.onPayChange(humanObj, allCharge);
		
		// GM需要该临时表现数据，作为弹窗显示
		ChargeInfoVO info = new ChargeInfoVO();
		info.sn = 3;
		info.num = 0;
		
		// 推送玩家充值信息
		SCPayCharge.Builder msg = SCPayCharge.newBuilder();
		msg.setChargeGold(allCharge);
		msg.setVipLevel(human.getVipLevel());
		msg.setChargeInfo(info.createMsg());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 修改vip等级
	 * @param humanObj
	 * @param chargeInfo
	 * @param gold
	 */
	public void setVipLevel(HumanObject humanObj, ChargeInfoVO chargeInfo, long gold) {
		setVipLevel(humanObj, chargeInfo, gold, null, "", 0);
	}

	/**
	 * 设置vip等级且将结果发送给客户端
	 * 
	 * @param humanObj
	 * @param gold
	 * @param orderId
	 * @param sn
	 * @param actualPrice
	 */
	public void setVipLevel(HumanObject humanObj, ChargeInfoVO chargeInfo,
			long gold, String orderId, String sn, int actualPrice) {
		if (gold <= 0) {
			return;
		}
		
		Human human = humanObj.getHuman();
		long allCharge = human.getChargeGold() + gold;
		human.setChargeGold(allCharge);
		
		onPayChange(humanObj,allCharge);

		// 推送玩家充值信息
		SCPayCharge.Builder msg = SCPayCharge.newBuilder();
		msg.setChargeGold(allCharge);
		msg.setVipLevel(human.getVipLevel());
		if (chargeInfo != null) {
			msg.setChargeInfo(chargeInfo.createMsg());
		}
		humanObj.sendMsg(msg);
	}
	
	public void onPayChange(HumanObject humanObj, long allCharge) {
		Human human = humanObj.getHuman();
		int maxVipLevel = ParamManager.maxVipLv;
		// 先判断是不是最大级别
		ConfVipUpgrade confMax = ConfVipUpgrade.get(maxVipLevel);
		if (confMax == null) {
			Log.table.error("===ConfVipUpgrade no find sn={}", maxVipLevel);
			return;
		}
		int newLevel = human.getVipLevel();
		
		// 重新计算级别
		for (int i = newLevel; i < maxVipLevel; i++) {
			ConfVipUpgrade confCurr = ConfVipUpgrade.get(i + 1);
			if (confCurr == null) {
				break;
			}
			if (allCharge >= confCurr.amount) {
				newLevel = confCurr.sn;
			} else{
				break;
			}
		}
		// 如果变化，就升级
		if (newLevel > human.getVipLevel()) {
			//设置玩家薪vip等级
			human.setVipLevel(newLevel);	
			// 派发vip等级改变事件
			Event.fire(EventKey.VipLvChange, "humanObj", humanObj);
		}
	}
	
	@Listener(value = EventKey.VipLvChange)
	public void humanVipLevelChange(Param param){
		HumanObject humanObj = param.get("humanObj");
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.updateVipLv(humanObj.id, humanObj.getHuman().getVipLevel());
	}
	
	/**
	 * 使用vip卡的事件
	 * @param param
	 */
	@Listener(value = EventKey.ItemUse)
	public void onItemHpUse(Param param) {
		// FIXME 暂时没有vip道具卡
//		HumanObject humanObj = param.get("humanObj");
//		Item itemBag = param.get("itemBag");
//		ConfItem confItem = param.get("confItem");
//		int num = param.getInt("num");
//		Human human = humanObj.getHuman();
//		int level = confItem.needLv;
//		
//		if (human.getVipLevel() < level) {
//			human.setVipLevel(level);
//			// 派发vip等级改变事件
//			Event.fire(EventKey. VipLvChange, "humanObj", humanObj);
//		}
//
//		// 推送玩家充值信息
//		SCPayCharge.Builder msg = SCPayCharge.newBuilder();
//		msg.setChargeGold(human.getChargeGold());
//		msg.setVipLevel(human.getVipLevel());
//		humanObj.sendMsg(msg);
//
//		// 删掉使用的物品
//		ItemBagManager.inst().remove(humanObj, itemBag.getSn(), num, LogSysModType.BagItemUse);
	}
	
	/**
	 * 领取充值发送的红包
	 * @param humanObj
	 * @param id
	 */
	public void takeChargePresent(HumanObject humanObj, int id) {
		PaymentServiceProxy prx = PaymentServiceProxy.newInstance();
		prx.takeChargePresent(id, humanObj.id);
		prx.listenResult(this::_result_take_charge_present, "humanObj", humanObj);
	}

	private void _result_take_charge_present(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		if (!results.getBoolean("success")) {
			InformManager.inst().WarnInform(humanObj.id, results.get("reason"));
			return;
		}
		int moneyType = results.getInt("moneyType");
		int moneyCount = results.getInt("moneyCount");
		//String presentName = results.getString("p_name");
		RewardHelper.reward(humanObj, moneyType, moneyCount, LogSysModType.ChargePresent);
	}

	/**
	 * 福利号充值
	 * @param humanObj
	 */
	public void fuli(HumanObject humanObj) {
		long timeFuli = humanObj.getHuman().getTimeFuli();

		// 如果本星期已经领取过
		if (Utils.getTimeBeginOfWeek(timeFuli) == Utils.getTimeBeginOfWeek(Port
				.getTime())) {
			return;
		}

		// 充值
		payGs_Fuli(humanObj, 8);//福利号一周只给一次648档的充值
		creatPayLog(humanObj, 8, "福利号充值");
		// 置时间
		humanObj.getHuman().setTimeFuli(Port.getTime());

	}

	/**
	 * gs充值
	 * @param humanObj
	 * @param sn
	 */
	public void gs(HumanObject humanObj, int sn) {
		ConfPayCharge conf = ConfPayCharge.get(sn);
		ConfPayMonthCard confm = ConfPayMonthCard.get(sn);

		int rmb = 99999;
		if (conf != null) {
			rmb = conf.rmb;
		}
		if (confm != null) {
			rmb = confm.rmb;
		}
		if (humanObj.getHuman().getGsRmb() < rmb) {
			return;
		}

		// 充值
		payGs_Fuli(humanObj, sn);
		creatPayLog(humanObj, sn, "gs充值");
		// 扣rmb
		humanObj.getHuman().setGsRmb(humanObj.getHuman().getGsRmb() - conf.rmb);
	}
	
	/**
	 * 打开vip界面
	 * @param humanObj
	 */
	public void openVipUI(HumanObject humanObj){
		SCOpenVipUI.Builder msg  = SCOpenVipUI.newBuilder();
		msg.setChargeGold(humanObj.getHuman().getChargeGold());
		msg.setVipLevel(humanObj.getHuman().getVipLevel());
		
		humanObj.sendMsg(msg);
	}

	/**
	 * 打开充值界面,获取各档充值的信息
	 * 
	 * @param humanObj
	 */
	public void openPayUI(HumanObject humanObj) {
		//System.out.println("line===713----------openPayUI-------------");
		String json = humanObj.getHuman().getChargeInfo();
		List<ChargeInfoVO> list = ChargeInfoVO.jsonToList(json);

		SCOpenPayUI.Builder msg = SCOpenPayUI.newBuilder();
		msg.setChargeGold(humanObj.getHuman().getChargeGold());
//		msg.setVipLevel(humanObj.getHuman().getVipLevel());
		
		boolean change = false;//这辈子第一次打开界面或新加了商品，要添加商品购买记录对象
		List<ConfPayCharge> confs = new ArrayList<>();
		confs.addAll(ConfPayCharge.findAll());
		ChargeInfoVO chargeInfo = null;
		for (ConfPayCharge conf : confs) {
			chargeInfo = ChargeInfoVO.getChargeInfo(list, conf.sn);
			if (chargeInfo == null) {
				change = true;
				chargeInfo = new ChargeInfoVO(conf.sn, 0, 0, 0);
				list.add(chargeInfo);
			}
			msg.addChargeInfos(chargeInfo.createMsg());
		}
		
		if (change) {
			humanObj.getHuman().setChargeInfo(ChargeInfoVO.listToJson(list));
		}
		
		//System.out.println("line===738----------openPayUI-------sendMsg------");
		humanObj.sendMsg(msg);
	}

	/**
	 * 请求充值游戏服信息
	 * 
	 * @param humanObj
	 * return
	 * 	serverIp 游戏服地址
	 * 	serverId 游戏服serverId
	 */
	public void reqChargeUrl(HumanObject humanObj) {
//		SCReqChargeUrl.Builder msg = SCReqChargeUrl.newBuilder();
//		msg.setServerIp(Config.GAME_CHARGE_DOMAIN);
//		msg.setServerId(humanObj.getHuman().getServerId());
//		humanObj.sendMsg(msg);
	}

	/**
	 * 请求充值记录 (注意：目前客户端不需要查询充值记录)
	 * 
	 * @param humanObj
	 */
	public void payLogs(HumanObject humanObj) {
		SCPayLogs.Builder msg = SCPayLogs.newBuilder();

		for (PayLog log : humanObj.payLogs) {
			DPayLog.Builder dPayLog = DPayLog.newBuilder();
			dPayLog.setPropId(log.getPropId());
			dPayLog.setOrderId(log.getOrderId());
			dPayLog.setStatus(log.getStatus());
			dPayLog.setTime(log.getTime());
			msg.addLogs(dPayLog);
		}

		humanObj.sendMsg(msg);
	}
	
	/**
	 * 创建充值日志
	 * @param humanObj
	 * @param sn
	 */
	public void creatPayLog(HumanObject humanObj, int sn, String type){
		ConfPayCharge conf = ConfPayCharge.get(sn);
		ConfPayMonthCard confm = ConfPayMonthCard.get(sn);
		int rmb = 0;
		if(conf != null){
			rmb = conf.rmb;
		}
		if(confm != null){
			rmb = confm.rmb;
		}
		JSONObject jo = new JSONObject();
		jo.put("roleId", humanObj.id + "");
		jo.put("actualPrice", rmb*100 + "");
		jo.put("chargePrice", rmb*100 + "");
		jo.put("chargeUnitId", "");
		jo.put("currencyType", "");
		jo.put("deviceGroupId","");
		jo.put("localeId", "");
		jo.put("channelId", "");
		jo.put("orderId", "");
		jo.put("userId", "");
		jo.put("serviceId", "");
		jo.put("serverId", "");
		jo.put("payChannelId", "");
		jo.put("sign", "");
		jo.put("propId", "");
		
		PF_PAY_Manager.inst().recordPayLog(humanObj.id, jo, type);
	}
	
	/**
	 * 登录成功
	 * @param param
	 */
	@Listener(EventKey.HumanLoginFinish)
	public void onHumanLoginFinish(Param param) {
		HumanObject humanObj = param.get("humanObj");
		//vip数据
		PaymentManager.inst().openPayUI(humanObj);
	}
	
	//生成玩家充值单号
	public void makeHumanPayCode(HumanObject humanObj, int sn) {
		long time = Port.getTime();
		String code = humanObj.lastPayCheckCode;
		//五分钟过后才生成一个新的
		if(time - humanObj.createPayCheckCodeTime > 300000 || code.isEmpty()){
			humanObj.createPayCheckCodeTime = time;
			String codeStr = "Pay"+humanObj.getHumanId()+time+"Code";
			code = Utils.md5(codeStr);
			code = code.toLowerCase();
			PayCheckCode reCheckCode = new PayCheckCode();
			reCheckCode.setId(Port.applyId());
			reCheckCode.setHumanId(humanObj.getHumanId());
			reCheckCode.setTime(time);
			reCheckCode.setCheckCode(code);
			reCheckCode.persist();
		}
		
		//发给客户端
		SCPayCheckCode.Builder msg = SCPayCheckCode.newBuilder();
		msg.setCode(code.substring(5, 13));
		msg.setSn(sn);
		humanObj.sendMsg(msg);
	}
	/**
	 * 玩家创角后首次登陆  获取测试的活动返还奖励
	 * @param humanObj
	 */
	public void getAccoutPayBackItem(HumanObject humanObj){
        Log.game.info("getAccountPayBackItem humanId={} account={}", humanObj.getHumanId(), humanObj.getHuman().getAccountId());
		KeyActivateServiceProxy proxy = KeyActivateServiceProxy.newInstance();
		proxy.payBackGet(""+humanObj.getHumanId(), ""+humanObj.getHuman().getServerId(), humanObj.getHuman().getAccountId());
		proxy.listenResult(this::_result_getAccoutPayBackItem, "humanObj", humanObj);
	}
	private void _result_getAccoutPayBackItem(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		String result = results.get();
		Log.charge.info("玩家 {} ({}), account = {} 获得活动返回信息 {} ",humanObj.name,humanObj.id,humanObj.getHuman().getAccountId() , result);
		JSONObject jsonObject = Utils.toJSONObject(result);
		int resultCode = jsonObject.getInteger("result");
		switch(resultCode){
			 case 0:{
				 //参数错误
				 Log.charge.info("humanId = {}, account = {} 获得活动返还失败");
				 return;
			} case 1:{
				//成功  获得活动返还
				
				JSONArray mailArray = jsonObject.getJSONArray("mail");
				Log.charge.info("玩家 {} ({}), account = {} 获得活动返还  {} ",humanObj.name,humanObj.id,humanObj.getHuman().getAccountId() , mailArray.toString());
				if(mailArray!=null && mailArray.size() > 0){
					for(int i = 0; i < mailArray.size();i++){
						JSONObject mailJson = Utils.toJSONObject(mailArray.get(i).toString());
						String itemSnStr = mailJson.getString("sn");
						String itemCountStr = mailJson.getString("num");
						String title = mailJson.getString("title");
						String detail = mailJson.getString("detail");
						MailManager.inst().sendSysMail(humanObj.id, title, detail, 
								ProduceManager.inst().produceItem(itemSnStr, itemCountStr));
					}
				}
				break;
			} case 2:{
				//参数错误
				 Log.charge.info("humanId = {}, account = {} 没有可以领取的活动返还");
				return;
			} default:{
				return;
			}
		}
		
	}
	
	@Listener(value = EventKey.PayNotifyHttps)
	public void onPayNotifyHttps(Param param){
		JSONObject jo = param.get();
		long humanId = jo.getLongValue("userId");
		String sn = jo.getString("productId");
		Boolean result = jo.getBoolean("result");
		if (!result) {
			return;
		}

		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_pay, "jo", jo, "sn", sn, "humanId", humanId);
		
	}
	private void _result_pay(Param results, Param context){
		JSONObject jo = context.get("jo");
//		HumanGlobalInfo info = results.get();
		HumanObject info = results.get();
		
		long humanId = context.get("humanId");
		int sn = context.get("sn");
		
//		if(info != null){
			//奖励元宝
			onPay(info, sn);
//		}else{
			
//			if(Config.GAME_PLATFORM_NAME.equals("tw")){
//				return;
//			}
//			//代办
//			Pocket.add(humanId, PocketLineKey.GM_CHARGE, String.valueOf(sn));
//		}
	}
	
	
	////
	
}

