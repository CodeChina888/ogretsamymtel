package game.worldsrv.csplatform;

import game.msg.MsgPlatform.SCCheckGiftCodeReturn;
import game.platform.DistrPF;
import game.platform.gift.GiftServiceProxy;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfGiftCode;
import game.worldsrv.enumType.LogSysModType;
import game.worldsrv.item.ItemChange;
import game.worldsrv.item.RewardHelper;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

import core.support.ManagerBase;
import core.support.Param;
import core.support.Utils;

public class CSPlatformManager extends ManagerBase{

	public static CSPlatformManager inst() {
		return inst(CSPlatformManager.class);
	}
	
	/**
	 * 提交礼包激活码
	 */
	public void checkGiftCode(HumanObject humanObj, String giftCode) {
		String giftPort = DistrPF.PORT_GIFT_PREFIX + new Random().nextInt(DistrPF.PORT_STARTUP_NUM_GIFT);
		GiftServiceProxy ptx = GiftServiceProxy.newInstance(DistrPF.NODE_ID, giftPort, DistrPF.SERV_GIFT);
		ptx.checkGiftCode(humanObj.getHumanId(), 
				humanObj.getHuman().getLevel(), 
				humanObj.getHuman().getTimeCreate(), 
				humanObj.getHuman().getChannel(),//渠道
				giftCode, 
				humanObj.getHuman().getServerId());
		ptx.listenResult(this::_result_checkGiftCode, "humanObj",humanObj, "giftCode", giftCode);
		
	}
	
	/*返回值
		0：参数不对
		1：成功
		2：找不到礼包码
		3：该激活码使用次数达到上限
		4：用户的该批次激活码使用次数达到上限
		5：激活码不在生效时间
		6：玩家的等级不足
		7：玩家创建时间不足
		8：渠道不正确
		9：玩家等级或者创建时间不是整形的
      10：异常
	 */
	public void _result_checkGiftCode(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		SCCheckGiftCodeReturn.Builder msg = SCCheckGiftCodeReturn.newBuilder();
		try {
			String result = results.get();
			JSONObject json = Utils.toJSONObject(result);
			if (json == null) {
				Log.game.info("=== 礼包不存在errorCode：{} ===", 490102);
				humanObj.sendSysMsg(490102);
				return;
			}
			Integer returnvalue = json.getInteger("result");
			if (returnvalue == null || returnvalue.intValue() != 1) {
				int errorCode = 4901 * 100 + returnvalue.intValue();
				Log.game.info("=== 兑换礼包错误errorCode：{} ===", errorCode);
				humanObj.sendSysMsg(errorCode);
				return;
			}
			String numStr = json.getString("num");
			String snStr = json.getString("sn");
			if (numStr!=null && !"".equals(numStr) && snStr!=null && !"".equals(snStr)) {
				String[] numsStrs = numStr.split(",|，");
				String[] snsStrs = snStr.split(",|，");
				int length = Math.min(numsStrs.length,snsStrs.length);
				int[] nums,sns;
				nums = new int[length];
				sns = new int[length];
				for (int i = 0; i < length; i ++) {
					nums[i] = Integer.valueOf(numsStrs[i]);
					sns[i] = Integer.valueOf(snsStrs[i]);
				}
				ItemChange itemChange = RewardHelper.reward(humanObj, sns, nums, LogSysModType.ActGiftCode);
				msg.addAllProduceList(itemChange.getProduce());
			} else {
				Integer giftId = json.getInteger("giftId");
				if (giftId == null) {
					Log.game.info("=== 礼包不存在errorCode：{} ===", 490102);
					humanObj.sendSysMsg(490102);
					return;
				}
				// 奖励
				ConfGiftCode giftCode = ConfGiftCode.get(giftId.toString());
				if (giftCode != null && giftCode.itemId.length > 0 && giftCode.itemNum.length > 0) {
					List<ProduceVo> proList = new ArrayList<>();
					int itemcount = Math.min(giftCode.itemId.length, giftCode.itemNum.length);
					for (int i = 0; i < itemcount; ++i) {
						ProduceVo pro = new ProduceVo(giftCode.itemId[i], giftCode.itemNum[i]);
						proList.add(pro);
					}
					ItemChange itemChange = RewardHelper.reward(humanObj, proList, LogSysModType.ActGiftCode);
					msg.addAllProduceList(itemChange.getProduce());
				}
			}
			// 返回成功
			msg.setResult(returnvalue.intValue());
			humanObj.sendMsg(msg.build());
		} catch (Exception e) {
			// 返回异常
			msg.setResult(10);
			humanObj.sendMsg(msg.build());
		}
	}
}
