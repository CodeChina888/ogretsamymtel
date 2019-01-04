package game.worldsrv.payment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import core.InputStream;
import core.OutputStream;
import core.interfaces.ISerilizable;
import game.worldsrv.config.ConfPayCharge;
import game.msg.Define.DChargeInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 充值信息
 * @author g
 *
 */
public class ChargeInfoVO  implements ISerilizable {
	public int sn;					//Payment.xlsx表中PayCharge的sn
	public int num;					//累计购买数量
	public int tn;					//已经领取的次数(充值后立即通过邮件领取一次)，是否可领取的标志
	public long tt;					//最后一次领取时间
	
	public ChargeInfoVO(JSONObject jo) {
		this.sn = jo.getIntValue("sn");
		this.num = jo.getIntValue("num");
		this.tn = jo.getIntValue("tn");
		this.tt = jo.getLongValue("tt");
	}
	
	public ChargeInfoVO(int sn, int num, int takeNum, long takeTime) {
		this.sn = sn;
		this.num = num;
		this.tn = takeNum;
		this.tt = takeTime;
	}
	
	public ChargeInfoVO() {
	}
	
	/**
	 * 把Json转换为List
	 * @param json
	 * @return
	 */
	public static List<ChargeInfoVO> jsonToList(String json) {
		
		List<ChargeInfoVO> result = new ArrayList<ChargeInfoVO>();
		if(StringUtils.isBlank(json)){
			return result;
		}
		JSONArray ja = JSON.parseArray(json);
		for (int i = 0; i < ja.size(); i++) {
			ChargeInfoVO vo = new ChargeInfoVO(ja.getJSONObject(i));
			result.add(vo);
		}
		
		return result;
	}
	
	/**
	 * 将List转换为Json
	 * @param list
	 * @return
	 */
	public static String listToJson(List<ChargeInfoVO> list){
		JSONArray ja = new JSONArray();
		if (list != null) {
			for (ChargeInfoVO vo : list) {
				JSONObject jo = new JSONObject();
				jo.put("sn", vo.sn);
				jo.put("num", vo.num);
				jo.put("tn", vo.tn);
				jo.put("tt", vo.tt);
				ja.add(jo);
			}
		}
		return ja.toJSONString();
	}
	
	/**
	 * 获取充值信息
	 * @param list
	 * @param sn
	 * @return
	 */
	public static ChargeInfoVO getChargeInfo(List<ChargeInfoVO> list, int sn){
		for(ChargeInfoVO chargeInfo : list){
			if(chargeInfo.sn == sn){
				return chargeInfo;
			}
		}
		return null;
	}
	
	/**
	 * 返回剩余的天数
	 * @return
	 */
	public int getLastDay(int sn){
		int result = 0;
		ConfPayCharge payCharge  = ConfPayCharge.get(sn);
//		if(tn > 0 && payCharge != null){
		if(payCharge != null){
			result = payCharge.retDay*num - tn;	
		}
		return result;
	}
	
	
	/**
	 * 由VO拼装出简版DChargeInfo消息
	 * @return
	 */
//	public DChargeInfo createMsg() {
//		
//		int lastDay = getLastDay(sn);
//		
//		DChargeInfo.Builder msg =DChargeInfo.newBuilder();
//		msg.setSn(sn);
//		msg.setNum(num);
//		msg.setLastDay(lastDay);
//		ConfPayCharge conf = ConfPayCharge.get(sn);
//		if(conf != null){
//			msg.setGiftOnce(conf.giftOnce);
//			msg.setGift(conf.gift);
//			msg.setIosProductId(conf.iosProductId);
//		}
//		
//		return msg.build();
//	}
	/**
	 * 由VO拼装出简版DChargeInfo消息
	 * @return
	 */
	public DChargeInfo createMsg() {
		return createMsg(0);
	}
	
	/**
	 * 由VO拼装出简版DChargeInfo消息 
	 * @param packType  指定iOS包标识  （根据不同版本的iOS包，提供给客户端不同过的iOS充值货品名）
	 * @return
	 */
	public DChargeInfo createMsg(int packType) {
		
		int lastDay = getLastDay(sn);
		
		DChargeInfo.Builder msg =DChargeInfo.newBuilder();
		msg.setSn(sn);
		msg.setNum(num);
		msg.setLastDay(lastDay);

		ConfPayCharge conf = ConfPayCharge.get(sn);
		if(conf != null){
			msg.setGiftOnce(conf.giftOnce);
			msg.setGift(conf.gift);
			msg.setIosProductId(conf.iosProductId);
		}
		return msg.build();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("sn", sn)
					.append("num", num)
					.append("tn", tn)
					.append("tt",tt)
					.toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(num);
		out.write(tn);
		out.write(tt);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		num = in.read();
		tn = in.read();
		tt = in.read();
	}
}
