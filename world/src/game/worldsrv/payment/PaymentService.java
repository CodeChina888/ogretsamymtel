package game.worldsrv.payment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;
import core.support.Time;
import core.support.Utils;
import game.msg.MsgPaymoney.SCGrantPresent;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfParam;
import game.worldsrv.config.ConfPayCharge;
import game.worldsrv.entity.IOSPayOrder;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.inform.InformManager;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.observer.Event;
import game.worldsrv.support.observer.EventKey;


@DistrClass(servId = D.SERV_PAY,importClass = {List.class, IOSPayOrder.class})
public class PaymentService  extends GameServiceBase{
	
	//红包ID(自增)
	private int id;
	//生成红包的集合
	private List<PaymentPresentVo> presentList = new LinkedList<>();
	Random random = new Random();
//	private Map<Integer, PaymentPresentVo> presentMap = new HashMap<>();
	//浮动概率(会根据抢夺的激烈程度，动态变化)
	private float rate = 1;

	//处理中的IOS订单
	private List<IOSPayOrder>  iosOrderList = new ArrayList<>();
	private Map<String, IOSPayOrder> iosOrderMap = new HashMap<>();
	
	//IOS充值校验地址
	public static final String URL_IOS_PAYCHECK_SANDBOX  = "https://sandbox.itunes.apple.com/verifyReceipt";
	public static final String URL_IOS_PAYCHECK_REAL = "https://buy.itunes.apple.com/verifyReceipt";
	//IOS订单状态
	public static final int IOS_ORDER_WAIT = 0;			//未校验
	public static final int IOS_ORDER_CHECKING = 1;	//校验中
	public static final int IOS_ORDER_FAIL = 2;			//IOS校验失败
	public static final int IOS_ORDER_PHONY = 3;		//IOS校验成功，货品却不对应
	public static final int IOS_ORDER_SUCCESS = 100;	 //校验成功
	public static final int QUERY_MAX_NUM = 1000;	
	public boolean initOK = false;//初始化完毕才开始处理订单
	public static final String IOS_SINA_BID = "com.sina.sng.eva";	//IOS程序bundle标识
	
	public PaymentService(GamePort port) {
		super(port);
	}

	@Override
	protected void init() {
		DB dbPrx = DB.newInstance(IOSPayOrder.tableName);
		//订单校验中就遇上关服的订单优先
		dbPrx.findBy(false,  IOSPayOrder.K.Status, IOS_ORDER_CHECKING );

		Param param = dbPrx.waitForResult();
		List<Record> records = param.get();
		for(Record r: records){
			IOSPayOrder payOrder = new IOSPayOrder(r);
			payOrder.setStatus(IOS_ORDER_WAIT);
			iosOrderList.add(payOrder);
			iosOrderMap.put(payOrder.getOrder(), payOrder);
		}
		//未处理过的订单
		dbPrx.findBy(false, IOSPayOrder.K.Status, IOS_ORDER_WAIT);
		param = dbPrx.waitForResult();
		records = param.get();
		for(Record r: records){
			IOSPayOrder payOrder = new IOSPayOrder(r);
			iosOrderList.add(payOrder);
			iosOrderMap.put(payOrder.getOrder(), payOrder);
		}
		initOK = true;
		Log.charge.info("IOS未处理充值订单加载完毕"); 
	}
	//测试苹果验证
	public static void main(String[] args) {
		String receiptData = "{"
			+"\n"
			+"		\"signature\" = \"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\";"
			+"\n"
			+"		\"purchase-info\" = \"ewoJIm9yaWdpbmFsLXB1cmNoYXNlLWRhdGUtcHN0IiA9ICIyMDE2LTExLTE3IDE5OjI2OjA1IEFtZXJpY2EvTG9zX0FuZ2VsZXMiOwoJInVuaXF1ZS1pZGVudGlmaWVyIiA9ICJiZTViYzQ5NjIzNzYzZTAxYzhjN2M4NDAwYjhjZWY1ODA3MDU5MTE3IjsKCSJvcmlnaW5hbC10cmFuc2FjdGlvbi1pZCIgPSAiMTAwMDAwMDI1MTcwMzc0NSI7CgkiYnZycyIgPSAiMTg2NTAiOwoJInRyYW5zYWN0aW9uLWlkIiA9ICIxMDAwMDAwMjUxNzAzNzQ1IjsKCSJxdWFudGl0eSIgPSAiMSI7Cgkib3JpZ2luYWwtcHVyY2hhc2UtZGF0ZS1tcyIgPSAiMTQ3OTQzOTU2NTU4OCI7CgkidW5pcXVlLXZlbmRvci1pZGVudGlmaWVyIiA9ICJCMEYyNkE4Ri0zN0ExLTQxODctOEZDMS1BRTUyOTkzNUZFMjYiOwoJInByb2R1Y3QtaWQiID0gInNuZ2V2YTAwMSI7CgkiaXRlbS1pZCIgPSAiMTE3NzMzNTY5MSI7CgkiYmlkIiA9ICJjb20uc2luYS5zbmcuZXZhIjsKCSJwdXJjaGFzZS1kYXRlLW1zIiA9ICIxNDc5NDM5NTY1NTg4IjsKCSJwdXJjaGFzZS1kYXRlIiA9ICIyMDE2LTExLTE4IDAzOjI2OjA1IEV0Yy9HTVQiOwoJInB1cmNoYXNlLWRhdGUtcHN0IiA9ICIyMDE2LTExLTE3IDE5OjI2OjA1IEFtZXJpY2EvTG9zX0FuZ2VsZXMiOwoJIm9yaWdpbmFsLXB1cmNoYXNlLWRhdGUiID0gIjIwMTYtMTEtMTggMDM6MjY6MDUgRXRjL0dNVCI7Cn0=\";"
			+"\n"
			+"		\"environment\" = \"Sandbox\";"
			+"\n"
			+"		\"pod\" = \"100\";"
			+"\n"
			+"		\"signing-status\" = \"0\";"
			+"\n"
			+"}";
		//根据环境取校验地址
		String url = URL_IOS_PAYCHECK_REAL;
		if(receiptData.contains("environment") && receiptData.contains("Sandbox")){
			url = URL_IOS_PAYCHECK_SANDBOX;
		}
		
		//base64加密收据
//		byte[] b = receiptData.getBytes();  
//        String app_receipt = "";  
//        if (b != null) {  
//        	app_receipt = new sun.misc.BASE64Encoder().encode(b);  
//        }  
		String app_receipt = Base64.getEncoder().encodeToString(receiptData.getBytes());
		
        String itunesRequest = sendPost(url, "{\"receipt-data\":\"" + app_receipt +"\"}");
        System.out.println(itunesRequest);
        JSONObject resultJsonObj = Utils.toJSONObject(itunesRequest);
        if(resultJsonObj.getIntValue("status") == 0){
        	//验证成功
        	 System.out.println("SUCCESS");
        }
	}
	@DistrMethod
	public void addIOSPayOrder(long humanId, int sn, String order, String receiptData){
		//排除重复的订单
		if(iosOrderMap.containsKey(order)){
			port.returns(0);
			return;
		}
		Log.charge.info("收到新的IOS充值订单 humanId = {}, sn = {}, order = {}",humanId, sn, order); 
		String receiptMd5 = Utils.md5(receiptData);
		//加入待处理订单队列
		IOSPayOrder orderRec = new IOSPayOrder();
		orderRec.setId(Port.applyId());
		orderRec.setHumanId(humanId);
		orderRec.setOrder(order);
		orderRec.setProductSn(sn);
		orderRec.setReceiptMd5(receiptMd5);	
		orderRec.setOriginReceipt(receiptData);		//保存原始收据
		orderRec.setStatus(IOS_ORDER_WAIT);		//加入订单校验队列
		orderRec.setTime(Port.getTime());
		orderRec.persist();
		iosOrderList.add(orderRec);
		iosOrderMap.put(order, orderRec);
		//新订单优先处理一次
		sendIOSPayCheck(orderRec,true);
		port.returns(0);
	}
	
	@ScheduleMethod("*/10 * * * * ?")
	public void checkIOSPayOrder(){
		if(initOK == false || iosOrderList.size() == 0){
			return;
		}
		for(IOSPayOrder payOrder: iosOrderList){
			//找到第一个待处理的订单进行处理
			if(payOrder.getStatus() == IOS_ORDER_WAIT){
				sendIOSPayCheck(payOrder, false);
				return;
			}
		}
	}
	/**
	 * 向IOS请求充值校验
	 * @param payOrder 订单
	 * @param once  只处理一次请求就结束
	 */
	public void sendIOSPayCheck(IOSPayOrder payOrder, boolean once){
		payOrder.setStatus(IOS_ORDER_CHECKING);
		payOrder.setTime(Port.getTime());
		String receiptData = payOrder.getOriginReceipt();
		//根据环境取校验地址
		String url = URL_IOS_PAYCHECK_REAL;
		if(receiptData.contains("environment") && receiptData.contains("Sandbox")){
			url = URL_IOS_PAYCHECK_SANDBOX;
		}
		//base64加密收据
//		byte[] b = receiptData.getBytes();  
//        String app_receipt = "";  
//        if (b != null) {  
//        	app_receipt = new sun.misc.BASE64Encoder().encode(b);  
//        }  
        String app_receipt = Base64.getEncoder().encodeToString(receiptData.getBytes());
        
        //前往苹果验证   可能会有3-6秒的延迟
        String itunesRequest = sendPost(url, "{\"receipt-data\":\"" + app_receipt +"\"}");
        if(itunesRequest == null || itunesRequest.isEmpty()){
        	payOrder.setStatus(IOS_ORDER_WAIT);
        	//校验过程失败，加到队尾
        	iosOrderList.remove(payOrder);
        	iosOrderList.add(payOrder);
        	return;
        }else{
        	/*
    		 * 验证结果 status:
    		 *	0  		  验证成功
    		 *  21000 App Store无法读取你提供的JSON数据
    		 *  21002 收据数据不符合格式
    		 *  21003 收据无法被验证
    		 *  21004 你提供的共享密钥和账户的共享密钥不一致
    		 *  21005 收据服务器当前不可用
    		 *  21006 收据是有效的，但订阅服务已经过期。当收到这个信息时，解码后的收据信息也包含在返回内容中
    		 *  21007 收据信息是测试用（sandbox），但却被发送到产品环境中验证
    		 *  21008 收据信息是产品环境中使用，但却被发送到测试环境中验证
    		 */ 
        	//Log.charge.info("IOS充值订单  humanId = {}, sn = {}, order = {} \n校验结果: \n{}",payOrder.getHumanId(),payOrder.getProductSn(), payOrder.getOrder(),itunesRequest); 
	        JSONObject resultJsonObj = Utils.toJSONObject(itunesRequest);
	        int status = resultJsonObj.getIntValue("status");
	        if(status == 21005){
	        	//21005 收据服务器当前不可用  保留在队列里
	        	if(payOrder.getStatus() == IOS_ORDER_CHECKING){
	        		payOrder.setStatus(IOS_ORDER_WAIT);
		        	//处理失败，加到队尾
		        	iosOrderList.remove(payOrder);
		        	iosOrderList.add(payOrder);
		        	return;
	        	}
	        }else{
	        	if(payOrder.getStatus() == IOS_ORDER_CHECKING){
	        		payOrder.setStatus(IOS_ORDER_FAIL);
	        		
		        	while(status == 0){
			        	//苹果验证成功
		        		JSONObject receiptObj = resultJsonObj.getJSONObject("receipt");
		        		//开发商校验
		        		String bid = receiptObj.getString("bid");
		        		if(bid == null || IOS_SINA_BID.equals(bid) == false){
		        			//不是程序自己订单号
		        			payOrder.setStatus(IOS_ORDER_PHONY);
		        			break;
		        		}
		        		//商品编号校验
			        	String product_id = receiptObj.getString("product_id");
			        	ConfPayCharge payConf = ConfPayCharge.get(payOrder.getProductSn());
			        	if(payConf != null && product_id.equals(payConf.iosProductId)){
			        		payOrder.setStatus(IOS_ORDER_SUCCESS);
			        		//给玩家发钱
				        	JSONObject jo = new JSONObject();
				        	jo.put("roleId", payOrder.getHumanId());
				        	jo.put("propId",""+payOrder.getProductSn());//商品sn
				        	jo.put("actualPrice", payConf.rmb*100);			//真实充值金额(分)
				        	jo.put("orderId", payOrder.getOrder() );
				        	Event.fire(EventKey.PayIOS, jo);
				        	Log.charge.info("IOS充值订单  humanId = {}, sn = {}, order = {} 处理成功",payOrder.getHumanId(),payOrder.getProductSn(), payOrder.getOrder()); 
			        	}
			        	else{
			        		//伪造的商品ID
			        		payOrder.setStatus(IOS_ORDER_PHONY);
			        	}
			        	break;
			        }
		        	payOrder.setTime(Port.getTime());
		        	//记录校验回执
		        	payOrder.setCheckReceipt(itunesRequest);
		        	
		        	//处理完毕从队列里删除
		        	iosOrderMap.remove(payOrder.getOrder());
		        	iosOrderList.remove(payOrder);
	        	}
	        }
        }
        //处理下一个
        if (once == false){
        	checkIOSPayOrder();
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
	    	Log.charge.info("发送 POST 请求出现异常！"+e); 
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
	
	/**
	 * 每秒检查一下是否有可发送的红包
	 */
	@ScheduleMethod("*/1 * * * * ?")
	public void checkChargePresent(){
		//没有红包，不发
		if(presentList.size() == 0){
			return;
		}
		
		PaymentPresentVo vo = presentList.get(0);
		long now = Port.getTime();
		//未通知前端的，通知所有在线人员
		if(vo.endTime == 0){
			sendChargePresentToAll(vo);
			return;
		}
		//如果红包没到失效时间，并且还有可领的份数，则不发新的红包
		if(now <= vo.endTime){
			return;
		}
		//当前的红包失效了，准备发送下一个吧
		presentList.remove(0);
		
		//如果红包被领没，则计算下一次红包的概率
		if(vo.invalidTime > 0){
			long time = vo.invalidTime - vo.startTime;
			Log.charge.info("红包在{}毫秒时被领完，红包数量{}，领取人数{}，概率{}， 浮动概率{}",time, vo.maxCount, vo.lotteryCount, (vo.rateHit*1.0)/vo.rateTotal, rate);
			if(time > Float.valueOf(ConfParam.get("payPresentTime").value)*Time.SEC){
				//增加概率
				rate = rate/Float.valueOf(ConfParam.get("payPresentRate").value);
				//概率不超过100%
				rate = rate >= 1? 1:rate; 
			}else{
				//减小概率
				rate = rate*Float.valueOf(ConfParam.get("payPresentRate").value);
			}
		}else{
			//Log.common.info("红包未被领完，剩余{}，红包数量{}，领取人数{}，概率{}， 浮动概率{}",vo.count, vo.maxCount, vo.lotteryCount, (vo.rateHit*1.0)/vo.rateTotal, rate);
			//增加概率
			rate = rate/Float.valueOf(ConfParam.get("payPresentRate").value);
			//概率不超过100%
			rate = rate >= 1? 1:rate; 
		}
		
		if(presentList.size() == 0){
			return;
		}
		
		//向客户端推送下一个红包
		vo = presentList.get(0);
		sendChargePresentToAll(vo);
	}
	
	/**
	 * 发送充值红包到所有在线玩家(通知客户端弹出抢夺按钮)
	 * @param vo
	 */
	private void sendChargePresentToAll(PaymentPresentVo vo){
		vo.startTime = Port.getTime();
		vo.endTime = Port.getTime() + Long.parseLong(ConfParam.get("presentInvalidTime").value);
		
		vo.rateHit = (int) (vo.rateHit*rate);
		
		SCGrantPresent.Builder msg = SCGrantPresent.newBuilder();
		msg.setId(vo.id);
		msg.setName(vo.presentName);
		msg.setHumanId(vo.humanId);
		msg.setHumanName(vo.humanName);
		msg.setMoneyType(vo.moneyType);
		msg.setMoneyCount(vo.count*vo.moneyCount);
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendMsgToAll(new ArrayList<>(), msg.build());

	}
	
	/**
	 * 生成充值红包
	 * @param humanId
	 * @param humanName
	 * @param sn
	 */
	@DistrMethod
	public void addChargePresent(long humanId, String humanName, int sn){
		//生成红包，放入map中
		PaymentPresentVo vo = new PaymentPresentVo(++id, humanId, humanName, sn);
		presentList.add(vo);
		
		//通知全服
//		SCGrantPresent.Builder msg = SCGrantPresent.newBuilder();
//		msg.setId(id);
//		msg.setName(vo.presentName);
//		msg.setHumanId(humanId);
//		msg.setHumanName(humanName);
//		msg.setMoneyType(vo.moneyType);
//		msg.setMoneyCount(vo.count*vo.moneyCount);
//		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
//		prx.sendMsgToAll(new ArrayList<>(), msg.build());
	}
	@DistrMethod
	public void generatePresentTw(long humanId, String humanName, long gold){
		//生成红包，放入map中
		PaymentPresentVo vo = new PaymentPresentVo(++id, humanId, humanName, gold);
		presentList.add(vo);
		
		//通知全服
//		SCGrantPresent.Builder msg = SCGrantPresent.newBuilder();
//		msg.setId(id);
//		msg.setName(vo.presentName);
//		msg.setHumanId(humanId);
//		msg.setHumanName(humanName);
//		msg.setMoneyType(vo.moneyType);
//		msg.setMoneyCount(vo.count*vo.moneyCount);
//		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
//		prx.sendMsgToAll(new ArrayList<>(), msg.build());
	}
	
	/**
	 * 领取充值红包
	 * 1.自己可以领取自己充值发送的红包
	 * 2.领取的时候走概率，不一定能得到奖励
	 * 3.抢夺者和充值者不是同一个人时，给充值者发私聊
	 * @param id 红包id
	 * @param humanId 领取玩家ID
	 */
	@DistrMethod
	public void takeChargePresent(int id, long humanId){
		if(presentList.size() == 0){
			port.returns("success", false, "reason", 9022);
			return;
		}
		PaymentPresentVo vo = presentList.get(0);
		//如果红包已经过期
		if(vo.id != id){
			port.returns("success", false, "reason", 9022);
//			port.returns("success", true, "moneyType", TokenItemType.COIN, "moneyCount", Utils.randomBetween(coinMin, coinMax), "p_name", vo.presentName);
			return;
		}
		//如果已经领完了
		if(vo.count <= 0){
			port.returns("success", false, "reason", 9022);
//			port.returns("success", true, "moneyType", TokenItemType.COIN, "moneyCount", Utils.randomBetween(coinMin, coinMax), "p_name", vo.presentName);
			return;
		}
		//是否已经领取过该红包
		if(vo.hasLottery(humanId)){
			port.returns("success", false, "reason", 9023);
			return;
		}
		//如果没有抽中红包
		if(!vo.lottery(vo)){
			port.returns("success", false, "reason", 9022);
//			port.returns("success", true, "moneyType", TokenItemType.COIN, "moneyCount", Utils.randomBetween(coinMin, coinMax), "p_name", vo.presentName);
			return;
		}
		
		if(humanId != vo.humanId){
			//抢夺者和充值者不是同一个人时，给充值者发私聊
			// 系统提示特殊格式：ParamManager.sysMsgMark{sysMsg.sn|参数1|...}
			String content = Utils.createStr("{}|{}", ParamManager.sysMsgMark, 9009);
			InformManager.inst().user(vo.humanId, content, humanId);
		}
		port.returns("success", true, "moneyType", vo.moneyType, "moneyCount", vo.moneyCount, "p_name", vo.presentName);
	}
	
	/**
	 * 后台支付通知
	 * @param msg https通讯 json传输格式
	 */
	@DistrMethod
	public void payNotifyHttps(String msg) {
		JSONObject jo = Utils.toJSONObject(msg);	
		Event.fire(EventKey.PayNotifyHttps, jo);		
	}
}
