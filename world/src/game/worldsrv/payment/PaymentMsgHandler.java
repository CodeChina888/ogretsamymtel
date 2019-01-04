package game.worldsrv.payment;

import game.msg.MsgIds;
import game.msg.MsgPaymoney.CSGsPayCharge;
import game.msg.MsgPaymoney.CSLotteryPresent;
import game.msg.MsgPaymoney.CSOpenPayUI;
import game.msg.MsgPaymoney.CSOpenVipUI;
import game.msg.MsgPaymoney.CSPayCharge;
import game.msg.MsgPaymoney.CSPayChargeIOS;
import game.msg.MsgPaymoney.CSPayCheckCode;
import game.msg.MsgPaymoney.CSPayLogs;
import game.msg.MsgPaymoney.CSRechargeSwitch;
import game.msg.MsgPaymoney.CSReqChargeUrl;
import game.msg.MsgPaymoney.CSYYBRecharge;
import game.msg.MsgPaymoney.SCRechargeSwitch;
import game.msg.MsgPaymoney.SCYYBRecharge;
import game.platform.DistrPF;
import game.platform.LogPF;
import game.platform.http.HttpAsyncSendServiceProxy;
import game.seam.msg.MsgParam;
import game.worldsrv.character.HumanObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

import core.Chunk;
import core.connsrv.ConnectionProxy;
import core.support.Param;
import core.support.Utils;
import core.support.observer.MsgReceiver;


/**
 * 付费相关点消息接受
 * @author GZC-WORK
 *
 */
public class PaymentMsgHandler {
	
	//Ios内购充值入口
	@MsgReceiver(CSPayChargeIOS.class)
	public void onPayChargeIOS(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSPayChargeIOS msg = param.getMsg();
		int sn = msg.getSn();
		String order = msg.getOrder();	//订单
		String receiptData = msg.getReceiptData();
		
		PaymentManager.inst().onPayIOS(humanObj, sn, order, receiptData);
	}
	
	/**
	 * vip充值
	 * @param param
	 */
	@MsgReceiver(CSPayCharge.class)
	public void onPayCharge(MsgParam param) {
		// 已经不走客户端发送了，直接后台推送：PFService.PF1_payNotice
		
//		HumanObject humanObj = param.getHumanObject();
//		CSPayCharge msg = param.getMsg();
//		int sn = msg.getSn();
//		
//		//gm调试命令打开，需要记录日志
//		if(C.IS_OPENGM){
//			PaymentManager.inst().onPay(humanObj, sn);
//			PaymentManager.inst().creatPayLog(humanObj, sn, "GM测试充值");
//			return ;
//		}
//		//非调试版本
//
//		//充值
//		PaymentManager.inst().onPay(humanObj, sn);
//		
//		JSONObject json = Utils.toJSONObject(humanObj.getHuman().getPayCharge());
//		int num = 0;
//		if(json.containsKey(String.valueOf(sn))){
//			num = json.getIntValue(String.valueOf(sn));
//		}
//		if(num > 0){   //充值补发
//			PaymentManager.inst().pay(param.getHumanObject(), sn);
//			PaymentManager.inst().creatPayLog(humanObj, sn, "充值补发");
//			if(num == 1){
//				json.remove(String.valueOf(sn));
//			}else{
//				json.put(String.valueOf(sn), num-1);
//			}
//			humanObj.getHuman().setPayCharge(json.toJSONString());
//			return ;
//		}
//		if(humanObj.getHuman().isFuli()){
//			PaymentManager.inst().fuli(humanObj);
//			return;
//		}
	}
		
	/**
	 * 请求充值校验码
	 * @param param
	 */
	@MsgReceiver(CSPayCheckCode.class)
	public void onCSPayCheckCode(MsgParam param){
		CSPayCheckCode msg = param.getMsg();
		PaymentManager.inst().makeHumanPayCode(param.getHumanObject(),msg.getSn());
	}
	/**
	 * 打开充值界面
	 * @param param
	 */
	@MsgReceiver(CSOpenPayUI.class)
	public void openPayUI(MsgParam param) {
		PaymentManager.inst().openPayUI(param.getHumanObject());
	}
	
	/**
	 * 打开VIP界面
	 * @param param
	 */
	@MsgReceiver(CSOpenVipUI.class)
	public void onCSOpenVipUI(MsgParam param) {
		PaymentManager.inst().openVipUI(param.getHumanObject());
	}
	
	/**
	 * 请求充值游戏服信息 
	 * @param param
	 */
	@MsgReceiver(CSReqChargeUrl.class)
	public void reqChargeUrl(MsgParam param) {
		PaymentManager.inst().reqChargeUrl(param.getHumanObject());
	}
	
	
	/**
	 * 请求充值记录
	 * @param param
	 */
	@MsgReceiver(CSPayLogs.class)
	public void payLogs(MsgParam param) {
		PaymentManager.inst().payLogs(param.getHumanObject());
	}
	
	/**
	 * 请求获取充值红包
	 * @param param
	 */
	@MsgReceiver(CSLotteryPresent.class)
	public void takeChargePresent(MsgParam param) {
		CSLotteryPresent msg = param.getMsg();
		PaymentManager.inst().takeChargePresent(param.getHumanObject(), msg.getId());
	}
	/**
	 * gs,福利号,充值补发
	 * @param param
	 */
	@MsgReceiver(CSGsPayCharge.class)
	public void gsPay(MsgParam param){
		CSGsPayCharge msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		int sn = msg.getSn();
		PaymentManager.inst()._msg_CSGsPayChager(humanObj, sn);
	}
	
	/**
	 * 应用宝充值验证
	 * @param param
	 */
	@MsgReceiver(CSYYBRecharge.class)
	public void yybRecharge(MsgParam param) {
		CSYYBRecharge msg = param.getMsg();
		HumanObject humanObject = param.getHumanObject();
		String jsonStr = msg.getJsonStr();
		LogPF.platform.info(humanObject+"请求应用宝充值校验!jsonStr:"+jsonStr);
		try {
			JSONObject jsonObject = Utils.toJSONObject(jsonStr);
			String url = Utils.createStr("http://{}:{}/rest/csjx", DistrPF.HTTP_GM_IP2, DistrPF.HTTP_GM_PORT2);
//			String url = Utils.createStr("http://{}:{}/rest/csjx", "10.163.106.104", 8080);
			// 请求地址
			HashMap<String, String> params = new HashMap<>();
			Long ts = null;
			String format = "";
			for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
				String key = entry.getKey();
				if ("ts".equals(key)) {
					ts = (Long) entry.getValue();
				} else if ("format".equals(key)) {
					format = (String) entry.getValue();
				}
				params.put(key, entry.getValue().toString());
			}
			String portHttpAsync = DistrPF.PORT_HTTP_ASYNC_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_HTTP_ASYNC);
			HttpAsyncSendServiceProxy prx = HttpAsyncSendServiceProxy.newInstance(DistrPF.NODE_ID, portHttpAsync, DistrPF.SERV_HTTP_SEND);
			prx.httpGetAsync(url, params, true);
			prx.listenResult(this::_result_yybRecharge,"humanObj",humanObject,"ts",ts,"format",format);
		} catch (Exception e) {
			LogPF.platform.error(humanObject+"请求充值验证失败！jsonStr:"+jsonStr,e);
		}
	}
	
	private void _result_yybRecharge(Param param, Param context) {
		HumanObject humanObject = context.get("humanObj");
		String jsonStr = param.get();
		JSONObject jsonObject = Utils.toJSONObject(jsonStr);
		String result = jsonObject.getString("session");
		if ("0".equals(result)) {
			Long ts = context.get("ts");
			String format = context.getString("format");
			SCYYBRecharge.Builder builder = SCYYBRecharge.newBuilder();
			if (ts != null) {
				builder.setTimestamp(ts);
			}
			if (format != null) {
				builder.setJsonStr(format);
			}
			ConnectionProxy prxConn = ConnectionProxy.newInstance(humanObject.connPoint);
			prxConn.sendMsg(MsgIds.SCYYBRecharge, new Chunk(builder));
		} else {
			String msg = jsonObject.getString("msg");
			LogPF.platform.error(humanObject+"请求充值验证失败！session:"+result+",msg:"+msg);
		}
	}
	
	@MsgReceiver(CSRechargeSwitch.class)
	public void rechargeSwitch(MsgParam msgParam) {
		CSRechargeSwitch msg = msgParam.getMsg();
		HumanObject humanObject = msgParam.getHumanObject();
		String appID = msg.getAppId();
		String channelID = msg.getChannelId();
		String version = msg.getVersion();
		String payTimes = "";
		String json = humanObject.getHuman().getChargeInfo();
		List<ChargeInfoVO> list = ChargeInfoVO.jsonToList(json);
		int totalRechagedTimes = 0;
		if (list!=null && list.isEmpty()==false) {
			for (ChargeInfoVO vo : list) {
				totalRechagedTimes += vo.num;
			}
		}
		payTimes = totalRechagedTimes+"";
		String url = "http://api.lcatgame.com/v1/app/query_switch";
		HashMap<String, String> params = new HashMap<>();
		params.put("appID", appID);
		params.put("channelID", channelID);
		params.put("version", version);
		params.put("payTimes", payTimes);
		String portHttpAsync = DistrPF.PORT_HTTP_ASYNC_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_HTTP_ASYNC);
		HttpAsyncSendServiceProxy prx = HttpAsyncSendServiceProxy.newInstance(DistrPF.NODE_ID, portHttpAsync, DistrPF.SERV_HTTP_SEND);
		prx.httpGetAsync(url, params, true);
		prx.listenResult(this::_result_rechargeSwitch,"humanObj",humanObject);
		
		LogPF.platform.info(humanObject+"请求充值接口查询！"+",msg:"+msg);
	}
	
	private void _result_rechargeSwitch(Param param, Param context) {
		HumanObject humanObject = context.get("humanObj");
		String jsonStr = param.get();
		SCRechargeSwitch.Builder builder = SCRechargeSwitch.newBuilder().setJsonStr(jsonStr);
		ConnectionProxy prxConn = ConnectionProxy.newInstance(humanObject.connPoint);
		prxConn.sendMsg(MsgIds.SCRechargeSwitch, new Chunk(builder));
		LogPF.platform.info(humanObject+"充值接口查询结果："+jsonStr);
	}
	
}