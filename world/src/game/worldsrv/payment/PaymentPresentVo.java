package game.worldsrv.payment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import core.InputStream;
import core.OutputStream;
import core.Port;
import core.interfaces.ISerilizable;
import core.support.Utils;
import game.worldsrv.config.ConfPayCharge;
import game.worldsrv.config.ConfPayGiftMore;
import game.worldsrv.support.Log;

public class PaymentPresentVo implements ISerilizable{
	
	public int id;	//红包ID
	public long humanId;	//发放红包人的humanId
	public String humanName;	//发放红包人的humanName
	
	public int count;		//红包份数
	public int maxCount;	//红包总份数
	public int moneyType;	//钱类型  1：铜币  14：绑定元宝
	public int moneyCount;	//钱数量
	public int rateHit;		//领取获得的概率
	public int rateTotal;	//领取获得概率的基数
	public String presentName;	//红包名称
	
	public Map<Long, Byte[]> lottryMap = new HashMap<>();	//领取过礼包的人
	public long startTime;	//发放时间
	public long endTime; 	//红包过期时间(默认是0,通知前端后设置时间)
	public long invalidTime;	//红包被领完的时间
	
	public int lotteryCount;	//有多少人领取过该红包
	
	
	public PaymentPresentVo(int id, long humanId, String humanName, int sn){
		ConfPayCharge conf = ConfPayCharge.get(sn);
		this.id = id;
		this.humanId = humanId;
		this.humanName = humanName;
		this.count = conf.p_count;
		this.maxCount = conf.p_count;
		this.moneyType = conf.p_moneyType;
		this.moneyCount = conf.p_moneyCount;
		this.rateHit = conf.p_rateHit;
		this.rateTotal = conf.p_rateTotal;
		this.presentName = conf.p_name;
//		this.InvalidTime = Port.getTime() + Long.valueOf(ConfParam.get("presentInvalidTime").value);
	}
	
	public PaymentPresentVo(int id, long humanId, String humanName, long gold) {
		this.id = id;
		this.humanId = humanId;
		this.humanName = humanName;
		ConfPayGiftMore conf = null;
		for(ConfPayGiftMore c : ConfPayGiftMore.findAll()){
			if(c.min <= gold && c.max >= gold){
				conf = c;
			}
		}
		if(conf == null){
			Log.table.error("===ConfPayGiftMore no find gold={} in range", gold);
			//humanObj.sendSysMsg(9008);
			return;
		}
		this.count = conf.p_count;
		this.maxCount = conf.p_count;
		this.moneyType = conf.p_moneyType;
		this.moneyCount = (int)(gold/conf.divisor) + conf.param;
		this.rateHit = conf.p_rateHit;	
		this.rateTotal = conf.p_rateTotal;
		this.presentName = Utils.createStr(conf.p_name, gold);
	}

	public boolean lottery(PaymentPresentVo vo){
		lotteryCount++;
		Random random = new Random();
		int rate = random.nextInt(rateTotal);
		if(rate < rateHit){
			this.count--;
			//红包被领取完，记录一下领取完的时间
			if(count == 0){
				invalidTime = Port.getTime();
			}
			return true;
		}
		return false;
	}
	public boolean hasLottery(long id){
		Byte[] bt = lottryMap.put(id, new Byte[0]);
		if(bt != null){
			return true;
		}
		return false;
		
	}
	

	@Override
	public void writeTo(OutputStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	
}
