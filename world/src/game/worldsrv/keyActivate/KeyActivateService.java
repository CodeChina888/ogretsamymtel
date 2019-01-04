package game.worldsrv.keyActivate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import core.Port;
import core.Record;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.support.Config;
import core.support.Param;
import core.support.Utils;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.entity.ActivateKey;
import game.worldsrv.entity.GiftActivate;
import game.worldsrv.param.ParamManager;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.platform.DistrPF;

/**
 * 激活码服务
 */
@DistrClass(
		servId = D.SERV_KEYACTIVATE,
		importClass = {}
	)
public class KeyActivateService extends GameServiceBase{
	//使用激活码错误返回列表
	public static final String KEY_ACTIVATE_SUCCESS = "0";//激活成功
	public static final String KEY_ACTIVATE_YET = "1";		//已激活，无需激活
	public static final String KEY_ACTIVATE_FAIL = "2";		//验证失败
	public static final String KEY_ACTIVATE_USED = "3";		//激活码使用过
	
	public static final String GIFT_ACTIVATE = "/rest/giftCode";	//接口方法
	public static final String GIFT_PAYBACK = "/rest/payBack/item";	//公测返还
	
    //使用过的激活码
	private static Set<String> Key_all = new HashSet<String>();
	//激活过的玩家
	private static Set<String> account_activate= new HashSet<String>();
	//使用过的礼包激活码
	private static Set<String> receives= new HashSet<String>();
	private int queryMaxNum = 1000;//每次查询1000
	
	public KeyActivateService(GamePort port) {
		super(port); 
	}
	
	@Override
	protected void init() {
		long time = System.currentTimeMillis();
		initKeyActivate();
		initGiftActivate();
		Log.game.info("finish to init KeyActivateService: cost {}", System.currentTimeMillis() - time);
	}
	public void initKeyActivate(){
		DB db = DB.newInstance(ActivateKey.tableName);
		List<Record> records;
		db.countBy(false);
		Param param = db.waitForResult();
		int count = param.get();
		int page = count / queryMaxNum;
		
		for(int i = 0 ; i <= page ; i++) {
			db.findBy(false, i * queryMaxNum, queryMaxNum);
			param = db.waitForResult();
			records = param.get();
			for(Record r : records) {
				ActivateKey act = new ActivateKey(r);
				account_activate.add(act.getAccount());
				Key_all.add(act.getActivateKey());
			}
		}
	}
	public void initGiftActivate(){
		DB db = DB.newInstance(GiftActivate.tableName);
		List<Record> records;
		db.countBy(false);
		Param param = db.waitForResult();
		int count = param.get();
		int page = count / queryMaxNum;
		
		for(int i = 0 ; i <= page ; i++) {
			db.findBy(false, i * queryMaxNum, queryMaxNum);
			param = db.waitForResult();
			records = param.get();
			for(Record r : records) {
				GiftActivate act = new GiftActivate(r);
				receives.add(act.getGiftKey());
			}
		}
	}
	/**
	 * 是否已激活
	 * @param account
	 */
	@DistrMethod
	public void isActivate(String account){
		port.returns("result", account_activate.contains(account));
	}
	/**
	 * 使用激活码激活
	 * @param key
	 */
	@DistrMethod
	public void useKey(String account, String key ,String severId) {
		String reason = "";
		boolean result = false;
		
		if(Key_all.contains(key)){//错误激活码
			reason = KEY_ACTIVATE_USED;	//"验证失败，该验证码已被使用过";
			result = false;
			port.returns("result", result,"reason", reason);
			return;
		}
		if(account_activate.contains(account)){
			reason = KEY_ACTIVATE_YET;	//"您已经使用过激活码，无需再激活";
			result = false;
			port.returns("result", result,"reason", reason);
			return;
		}
		if(canActivate(key, "all", "") || canActivate(key, severId, "")){	
//			Log.temp.info("正确的激活码");
			account_activate.add(account);
			reason = KEY_ACTIVATE_SUCCESS;// "验证成功，恭喜您获得本次测试的资格。";
			result = true;
			ActivateKey keyAct = new ActivateKey();
			keyAct.setId(Port.applyId());
			keyAct.setAccount(account);
			keyAct.setActivateKey(key);
			keyAct.persist();
			Key_all.add(key);
			port.returns("result", result,"reason", reason);
			return;
		}
		reason = KEY_ACTIVATE_FAIL;	//"验证失败，验证码错误";
		result = false;
//		Log.temp.info("错误的激活码");
		port.returns("result", result,"reason", reason);
	}
	
	
	/**
	 * 激活礼包码
	 * @param key
	 * @param channel
	 */
	@DistrMethod
	public void activateGift(String key, String channel){
		String reason = "";
		boolean result = false;
		String gift = key.substring(4, key.length() - 4);
		if(receives.contains(key)){ //已经被使用
//			reason = "礼包码已被使用";
//			reason = Inform.getStringData(20202);
			result = false;
			port.returns("result", result,"reason", reason);
			return;
		}
		if(canActivate(key, "", gift) || canActivate(key, channel, gift)){
			reason = "礼包码激活成功";
//			reason = Inform.getStringData(20201);
			result = true;
			receives.add(key);
			GiftActivate act = new GiftActivate();
			act.setGiftKey(key);
			act.setId(Port.applyId());
			act.persist();
			port.returns("result", result,"reason", reason);
			return;
		}else{
//			reason = "礼包码错误";
//			reason = Inform.getStringData(20200);
			result = false;
			port.returns("result", result,"reason", reason);
			return;
		}
	}
	
	/**
	 *礼包码是否正确
	 * @param key(礼包码前四位)
	 * 礼包码解析：取礼包码前4位进行一次md5，得到的串再加上渠道channel，
	 * 再进行一次md5得到一个串，再进行一次md5得到一个串str，，取str的前两位+str后两位得到验证码
	 * 验证码跟礼包码的后四位比较。
	 * @param channel(渠道)
	 * @return
	 */
	public static boolean canActivate(String key, String channel, String gift){
//		key = key.toUpperCase();
		String keyF4 = key.substring(0, 4);   //礼包码前四位
		String keyB4 = key.substring(key.length() -4, key.length());//礼包码后四位
		String md51 = Utils.md5(keyF4);   //前四位md5一次
//		StringBuffer str1 = new StringBuffer(str);
//		str1.append(channel);
		String md52 = Utils.md5(md51 + channel + gift);   //加上渠道后md5一次
		String md53 = Utils.md5(md52 + gift);   //再md5一次
		//取最终串的前两位+后两位
		StringBuffer strKey = new StringBuffer("");
		strKey.append(md53.charAt(0));
		strKey.append(md53.charAt(1));
		strKey.append(md53.charAt(md53.length() - 2));
		strKey.append(md53.charAt(md53.length() - 1));
		String toKey = strKey.toString();
		toKey = toKey.toUpperCase();
//		Log.temp.info("{}--->{}",keyF4, toKey);
		//与礼包码的后四位比较
		if(keyB4.equals(toKey)){
			return true;
		}
		return false;
	}
	/**
	 * 检查礼包码是否可以使用
	 */
	@DistrMethod
	public void giftCodeActivate(String humanId, String server, String giftCode) {
		
		//如果没开启礼包码兑换，就直接返回掉
		if(!ParamManager.openGiftKey) {
			port.returns("0");
			return;
		}
		//查询远端服务器的充值返还
		String url = Utils.createStr("http://{}:{}" + GIFT_ACTIVATE, DistrPF.HTTP_GM_IP, DistrPF.HTTP_GM_PORT);
		String result = Utils.httpGet(url, Utils.<String, String>ofMap("humanId", humanId, "giftCode",giftCode, "server", server));
		port.returns(result);
	}
	//生成激活码
	public static void main(String[] args){
		long index = 0;//本次的起始索引
		long count = 100;//数量
		//随便确定四个固定的列表
		String keyStr1 = "edfghipqrstuv0123zklmno456789abcwxyz";
		String keyStr2 = "vk4uwl01xyz56qrabcdef789mno23pghizst";
		String keyStr3 = "a1r5hizkl2o6789bcdefgpqm0nxystuv4w3z";
		String keyStr4 = "0zvu78lkjihnmedcba123gf9ytsrqpo456xw";
		for(long i = index; i< index +count; i ++){
			
			String num36 = Long.toString(i, 36); 
			while (num36.length() < 4) {
				num36 = "0" + num36;
		     }
			int index1 = Integer.parseInt(num36.substring(0, 1),36);
			int index2 = Integer.parseInt(num36.substring(1, 2),36);
			int index3 = Integer.parseInt(num36.substring(2, 3),36);
			int index4 = Integer.parseInt(num36.substring(3, 4),36);
			String result = keyStr1.substring(index1, index1+1) + keyStr2.substring(index2, index2+1) + keyStr3.substring(index3, index3+1) + keyStr4.substring(index4, index4+1);
			System.out.println(result);  
		}
		
	}
	
	
	/**
	 * 检查充值返还
	 */
	@DistrMethod
	public void payBackGet(String humanId, String server, String account) {
		//查询远端服务器的充值返还
        Log.game.info("humanId={} server={} account={}", humanId, server, account);
		String url = Utils.createStr("http://{}:{}" + GIFT_PAYBACK, DistrPF.HTTP_GM_IP, DistrPF.HTTP_GM_PORT);
		String result = Utils.httpPost(url, Utils.<String, String>ofMap("userId", humanId, "accountId", account, "server", server));
        Log.game.info("humanId={} server={} account={} result={}", humanId, server, account, result);
		port.returns(result);
	}
		
	
}
