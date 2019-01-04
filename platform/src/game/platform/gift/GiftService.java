package game.platform.gift;

import core.Port;
import core.Service;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Utils;
import game.platform.DistrPF;
import game.platform.LogPF;
import game.platform.http.HttpClient;

@DistrClass
public class GiftService extends Service {
	
	public static final String SERVLET = "/test1/gm/";	//领取礼包码
	public static final String METHOD = "useCode";	//领取礼包码
	public static final String GIFT_SERVLET = "/rest/giftCode2";//礼包码
	
	public static final int CODE_CHEK_ID_SUCCESS = 1;
	public static final int CODE_CHEK_ID_NOTEXIST = -1;
	public static final int CODE_CHEK_ID_TIMEOUT = -2;
	public static final int CODE_CHEK_ID_INVALID = -3;
	
	public GiftService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return DistrPF.SERV_GIFT;
	}
	
	@Override
	public void pulseOverride() {
		
	}
	
	/** 礼包码验证
	 * @param account
	 * @param humanId
	 * @param code
	 * @param serviceId
	 */
	@DistrMethod
	public void check(String account, long humanId, String code, String serviceId) {
		String url = Utils.createStr("http://{}:{}" + SERVLET + METHOD, DistrPF.HTTP_GM_IP, DistrPF.HTTP_GM_PORT);
		String result = Utils.httpGet(url, Utils.<String, String>ofMap("account", account,"humanId", String.valueOf(humanId), "codeId", code, "serviceId", serviceId));
		LogPF.platform.info("GM平台验证礼包码返回,  {}",result);
		port.returns(result);
	}
	
	/** 礼包码验证
	 * @param humanId
	 * @param code
	 * @param serviceId
	 */
	@DistrMethod
	public void checkGiftCode(long humanId, long humanLv, long humanCreateTime, String channel, String code, int serviceId) {
		String url = Utils.createStr("http://{}:{}" + GIFT_SERVLET, DistrPF.HTTP_GM_IP, DistrPF.HTTP_GM_PORT);
		String result = Utils.httpGet(url, Utils.<String, String>ofMap("userId", String.valueOf(humanId),"userLv", String.valueOf(humanLv), "userCreateDate", String.valueOf(humanCreateTime), "channel", channel, "giftCode", code, "server", String.valueOf(serviceId)));
		LogPF.platform.info("GM平台验证礼包码返回,  {}",result);
		port.returns(result);
	}
	
	@DistrMethod
	public void uploadChargeRecord(String jsonData) {
		byte[] dataByte = Utils.gzip(jsonData);
		HttpClient clinet = new HttpClient(DistrPF.HTTP_TALKING_GAME_SERVER);
		LogPF.platform.info(clinet.doPost(dataByte));
//		Upload.uploadChargeRecord(ConstPf.HTTP_TALKING_GAME_SERVER, jsonData);
	}
	
}
