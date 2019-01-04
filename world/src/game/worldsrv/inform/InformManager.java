package game.worldsrv.inform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Port;
import core.support.ManagerBase;
import game.msg.Define.EInformType;
import game.msg.Define.ERumorType;
import game.msg.MsgInform.SCInformMsg;
import game.worldsrv.character.HumanObject;
import game.worldsrv.config.ConfItem;
import game.worldsrv.entity.Human;
import game.worldsrv.human.HumanGlobalInfo;
import game.worldsrv.human.HumanGlobalServiceProxy;
import game.worldsrv.param.ParamManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.Utils;

public class InformManager extends ManagerBase {

	/**
	 * 获取实例
	 * @return
	 */
	public static InformManager inst() {
		return inst(InformManager.class);
	}
	
	/**
	 * 发送消息给单个玩家
	 * @param receiveHumanId 接收人Id
	 * @param content 内容
	 * @param sendHumanId 发送人Id
	 */
	public void user(Long receiveHumanId, String content, Long sendHumanId) {
		sendMsg(EInformType.PrivateInform, receiveHumanId, content, sendHumanId);
	}
	public void user(Long receiveHumanId, String content) {
		user(receiveHumanId, content, null);
	}
	public void WarnInform(Long receiveHumanId, String content) {
		sendMsg(EInformType.WarnInform, receiveHumanId, content, null);
	}
	public void ErrorInform(Long receiveHumanId, String content) {
		sendMsg(EInformType.ErrorInform, receiveHumanId, content, null);
	}
	public void DialogInform(Long receiveHumanId, String content) {
		sendMsg(EInformType.DialogInform, receiveHumanId, content, null);
	}
	
	/**
	 * 发送消息给所有人
	 * @param content 内容
	 * @param sendHumanId 发送者Id
	 */
	public void all(String content, Long sendHumanId) {
		sendMsg(EInformType.WorldInform, null, content, sendHumanId);
	}
	public void all(String content) {
		all(content, null);
	}
	public void GMInform(String content) {
		sendMsg(EInformType.GMInform, null, content, null);
	}
	public void SystemInform(String content) {
		sendMsg(EInformType.SystemInform, null, content, null);
	}
	public void RumorInform(String content) {
		sendMsg(EInformType.RumorInform, null, content, null);
	}
	public void HornInform(String content) {
		sendMsg(EInformType.HornInform, null, content, null);
	}

	/**
	 * 发送消息给多个玩家
	 * @param receiveHumanIds 接收人的Id集合
	 * @param content 发送内容
	 * @param sendHumanId 发送人Id
	 */
	public void users(List<Long> receiveHumanIds, String content, Long sendHumanId) {
		sendMsg(EInformType.PrivateInform, receiveHumanIds, content, sendHumanId);
	}
	public void users(List<Long> receiveHumanIds, String content) {
		users(receiveHumanIds, content, null);
	}

	/**
	 * 发送消息给指定地图的所有玩家
	 * @param stageId 地图Id
	 * @param content 内容
	 * @param sendHumanId 发送人Id
	 */
	public void stage(Long stageId, String content, Long sendHumanId) {
		sendMsg(EInformType.MapInform, stageId, content, sendHumanId);
	}
	public void stage(Long stageId, String content) {
		stage(stageId, content, null);
	}

	/**
	 * 发送消息给指定公会的所有玩家
	 * @param guildId 公会Id
	 * @param content 内容
	 * @param sendHumanId 发送者Id
	 */
	public void guild(Long guildId, String content, Long sendHumanId) {
		sendMsg(EInformType.GuildInform, guildId, content, sendHumanId);
	}
	public void guild(Long guildId, String content) {
		guild(guildId, content, null);
	}

	/**
	 * 发送消息给指定队伍的所有玩家
	 * @param teamId 队伍Id
	 * @param content 内容
	 * @param sendHumanId 发送者Id
	 */
	public void team(Long teamId, String content, Long sendHumanId) {
		sendMsg(EInformType.TeamInform, teamId, content, sendHumanId);
	}
	public void team(Long teamId, String content) {
		team(teamId, content, null);
	}
	
	/**
	 * 发送消息至指定聊天频道
	 * @param type 通告类型
	 * @param keyVals 接受对象的Id列表(可以是 玩家Id、地图 Id、公会Id、队伍Id)
	 * @param content 发送内容
	 * @param sendHumanId 发送人的Id
	 */
	private void sendMsg(EInformType type, List<Long> keyVals, String content, Long sendHumanId) {
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendInform(type, keyVals, content, sendHumanId, 1, 0);
	}
	private void sendMsg(EInformType type, long keyVal, String content, Long sendHumanId) {
		List<Long> list = new ArrayList<>();
		list.add(keyVal);
		sendMsg(type, list, content, sendHumanId);
	}
	private void sendMsg(EInformType type, String content) {
		sendMsg(type, null, content, null);
	}

	/**
	 * 获得道具播报(现在只有抽奖)
	 * @param humanObj
	 * @param itemProduce 道具 每一道具播一次
	 */
	public void addItemReportNotice(HumanObject humanObj, List<ProduceVo> itemProduce){
		if (humanObj == null || itemProduce == null || itemProduce.isEmpty()) {
			return;
		}
		for (ProduceVo produceVo : itemProduce) {
			ConfItem confItem = ConfItem.get(produceVo.sn);
			// 0是不播报
			if (confItem == null || confItem.sysNotice == 0) {
				continue;
			}
			// 1|神级春哥|2032050 
			// 前面的0是给客户端判断的（后面或许其他类型的播报）confItem.sysNotice ！= 0，就发。策划修改：公告和聊天都要发
			String noticeStr = confItem.sysNotice+"|"+humanObj.name+"|"+confItem.sn;
			sendSCInformMsg(humanObj, EInformType.RumorInform, noticeStr);
		}
	}
	/**
	 * 装备进化
	 * @param humanObj
	 * @param oldSn
	 * @param newSn
	 */
	public void evoluItemNotice(HumanObject humanObj, int oldSn, int newSn){
		ConfItem confItem = ConfItem.get(newSn);
		// 0是不播报
		if (confItem == null || confItem.sysNotice == 0) {
			return;
		}
		// 3|神级春哥|oldSn|newSn
		String noticeStr = ERumorType.InformEvoluItem_VALUE+"|"+humanObj.name+"|"+oldSn+"|"+newSn;
		sendSCInformMsg(humanObj, EInformType.RumorInform, noticeStr);
	}
		
	/**
	 * 聊天/发言
	 * @param humanObj
	 * @param type 通告类型
	 * @param content 发送内容
	 * @param toHumanId 接受对象的Id
	 * @param num 播放次数，只适用于GMInform和SystemInform，格式："播放次数|内容"
	 */
	public void sendSCInformMsg(HumanObject humanObj, EInformType type, String content, long toHumanId, int num,String toHumanName) {
		Human human = humanObj.getHuman();
		long humanId = human.getId(); // 发言的玩家的ID

		// 检查世界频道的发言CD
		if (type == EInformType.WorldInform) {
			if (!isCooldown(humanObj)) {
				// 发言过快，请休息片刻
				humanObj.sendSysMsg(150101);
				return;
			}
		}

		switch (type) {
			case GMInform:
			case SystemInform: {
				sendNotify(type, content, num);// 发送通告
			}	break;
			case HornInform: {
				//TODO 扣除钻石
				//RewardHelper.reduceMoney(humanObj, EMoneyType.gold_VALUE, 0, LogSysModType.HornInform);
				sendMsg(type, content);
			}	break;
			case RumorInform: 
			case WorldInform: {
				sendMsg(type,null,content,humanId);
			}	break;
			case PrivateInform: {
				List<Long> toIds = new ArrayList<>();
				toIds.add(toHumanId);
				toIds.add(humanId);
				sendMsg(type, toIds, content, humanId);
			}	break;
			case WarnInform: 
			case ErrorInform: 
			case DialogInform: {
				sendMsg(type, humanId, content, humanId);
			}	break;
			case MapInform: {
				sendMsg(type, humanObj.stageObj.stageId, content, humanId);
			}	break;
			case GuildInform: {
				//sendMsg(type, guildId, content, humanId);
			}	break;
			case TeamInform: {
				sendMsg(type, (long)humanObj.getTeamId(), content, humanId);
			}	break;
			default:
				break;
		}
		
		// 更新世界频道的发言CD
		if (type == EInformType.WorldInform) {
			humanObj.worldInformCD = Port.getTime();
			//同步聊天信息到聊天服
			Map<String,String> params = new HashMap<>();
			params.put("userId", humanObj.getHuman().getAccountId());
			params.put("roleId", String.valueOf(humanObj.getHuman().getId()));
			params.put("roleName", humanObj.name);
			params.put("roleVipLv", humanObj.getHuman().getVipLevel()+"");
			params.put("chatChannel", String.valueOf(EInformType.WorldInform_VALUE));
			params.put("chatTime", Utils.formatTimeToDate(Port.getTime()));
			params.put("chatInfo", content);
			params.put("MAC", "");
			params.put("IDFA", "");
			params.put("deviceUniqueId", "");
			params.put("extendsFields", "");
			params.put("sn", String.valueOf(humanObj.sn));
			params.put("titleSn", humanObj.getHuman().getTitleSn() + "");
			ChatServiceProxy.newInstance().addChat(params, humanObj.getHuman().getLevel(), humanObj.getHuman().getVipLevel());
		}
	}
	public void sendSCInformMsg(HumanObject humanObj, EInformType type, String content, long toHumanId,String toHumanName) {
		sendSCInformMsg(humanObj, type, content, toHumanId, 1, toHumanName);
	}
	public void sendSCInformMsg(HumanObject humanObj, EInformType type, String content) {
		sendSCInformMsg(humanObj, type, content, 0, 1, null);
	}
	
	/**
	 * 发送通告
	 */
	public boolean sendNotify(EInformType type, String content, int num) {
		// 无间隔
		return sendNotify(type, content, num, 0);
	}
	/**
	 * 发送通告
	 * @param type
	 * @param content
	 * @param num
	 * @param interval 通告显示间隔
	 * @return
	 */
	public boolean sendNotify(EInformType type, String content, int num, int interval) {
		boolean ret = true;
		if (type == null) {
			type = EInformType.SystemInform;
		}
		if (type != EInformType.GMInform && type != EInformType.SystemInform && type != EInformType.WorldInform) {
			type = EInformType.SystemInform;
		}
		if (type == EInformType.WorldInform) {
			type = EInformType.GMInform;
		}
		// TODO 后续补充时间间隔相关逻辑
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendInform(type, null, content, null, num, interval);
		return ret;
	}
	
	/**
	 * 验证发言是否冷却
	 * @return
	 */
	private boolean isCooldown(HumanObject humanObj) {
		if (humanObj.worldInformCD == 0)
			return true;
		// 发言间隔
		int interval = ParamManager.chatCDTime * Utils.I1000;
		return (humanObj.worldInformCD + interval) < Port.getTime();
	}
	
	/**
	 * 创建返回客户端的推送消息
	 * @param type
	 * @param content
	 * @return
	 */
	public SCInformMsg createMsg(EInformType type, String content, HumanGlobalInfo sender,
			List<HumanGlobalInfo> receiveHumanInfos) {
		SCInformMsg.Builder msg = SCInformMsg.newBuilder();
		msg.setType(type);
		msg.setContent(content);
		
		if (sender != null) {
			msg.setSendHumanId(sender.id);
			msg.setSendHumanName(sender.name);
			msg.setSnedVip(sender.vipLv);
			msg.setHeadId(sender.headSn);
			msg.setSnedLv(sender.level);
		}

		// 私聊特殊处理 "[张三]对 你 说，你 对[李四]说 " 所以要设置接受者信息
		if (type == EInformType.PrivateInform) {
			HumanGlobalInfo hsi = receiveHumanInfos.get(0);
			msg.setReceiveHumanId(hsi.id);
			msg.setReceiveHumanName(hsi.name);
		}

		return msg.build();
	}

	/**
	 * 描述：判断一个字符串是否为null或空值.
	 * @param str 指定的字符串
	 * @return true or false
	 */
	public boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	/**
	 * 描述：获取字符串的长度.
	 * @param str 指定的字符串
	 * @return 字符串的长度（中文字符计2个）
	 */
	public int strLength(String str) {
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
		if (!isEmpty(str)) {
			// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
			for (int i = 0; i < str.length(); i++) {
				// 获取一个字符
				String temp = str.substring(i, i + 1);
				// 判断是否为中文字符
				if (temp.matches(chinese)) {
					// 中文字符长度为2
					valueLength += 2;
				} else {
					// 其他字符长度为1
					valueLength += 1;
				}
			}
		}
		return valueLength;
	}
	
}