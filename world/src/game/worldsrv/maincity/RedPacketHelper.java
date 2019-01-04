package game.worldsrv.maincity;

import game.worldsrv.config.ConfRedPackage;
import game.worldsrv.entity.RedPacket;
import game.worldsrv.support.Log;
import game.worldsrv.support.Utils;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import core.Port;

/**
 * 红包管理器，提供生成红包，领取红包等静态方法
 * @author songy
 *
 */
public class RedPacketHelper {
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private RedPacketHelper(){};
	
	
	/**
	 * 依据配置sn 产生红包
	 */
	public static RedPacket productRedPacket(long humanId,int sn,long time){
		RedPacket redPacket = new RedPacket();
		ConfRedPackage conf = ConfRedPackage.get(sn);
		if(conf == null){
			Log.table.error("RedPacketHelper.productRedPacket can't find conf by sn={}",sn);
			return redPacket;
		}
		redPacket.setId(Port.applyId());
		redPacket.setSn(conf.sn);
		redPacket.setHumanId(humanId);//发红包的人
		redPacket.setNums(conf.totleNum);//红包个数
		redPacket.setBeginTime(time);//开始时间戳
		redPacket.setEndTime(time+conf.sec*1000);//结束时间戳
		
		//红包内物品
		int  itemSn = conf.itemSn;
		int  itemNum = conf.itemNum;
		int  minItemNum = conf.minItemNum;
		int  maxItemNum = conf.maxItemNum;
		int  totleNum = conf.totleNum;
			
		RedPacketObject redObject = new RedPacketObject(itemSn,itemNum,totleNum,minItemNum,maxItemNum);
		String itemJson = Utils.ListIntegerToStr(redObject.splitRedPackets());
		redPacket.setItemSn(itemSn);
		redPacket.setSurplusDItems(itemJson);
		redPacket.setGainsituation(new JSONArray().toJSONString());
		//System.out.println("产生红包"+redPacket.getSurplusDItems());
		return redPacket;
	}
	
	/**
	 * 抢红包
	 */
	public static int robRedPacket(long humanid,String name,RedPacket redpacket){
		//获取当前剩余红包个数
		List<Integer>  l = Utils.strToIntList(redpacket.getSurplusDItems());
		if(l.size() <= 0){
			return 0;
		}
		int getNum = l.get(0);
		l.remove(0);
		//记录红包领取
	    JSONArray gainJa = JSONArray.parseArray(redpacket.getGainsituation());
	    JSONObject record = new JSONObject();
	    record.put("humanid", humanid);
	    record.put("name", name);
	    record.put("getItem", String.valueOf(getNum));
	    gainJa.add(record);
	    redpacket.setGainsituation(gainJa.toString());
	    redpacket.setSurplusDItems(Utils.intListToStr(l));
		System.out.println("人物获得:"+getNum);
		
		return getNum;
	}
	
	public static void main(String[] args) {
		RedPacket r = RedPacketHelper.productRedPacket(100, 3, 123L);
	}
}
