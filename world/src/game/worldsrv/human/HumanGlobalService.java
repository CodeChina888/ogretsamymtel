package game.worldsrv.human;

import game.msg.Define.DInformCrossChat;
import game.msg.Define.ECrossFightType;
import game.msg.Define.EInformType;
import game.msg.Define.ETeamType;
import game.msg.MsgCommon.SCHumanKick;
import game.msg.MsgInform.SCInformCrossMsgAll;
import game.msg.MsgInform.SCInformMsg;
import game.msg.MsgInform.SCInformMsgAll;
import game.seam.account.AccountService;
import game.seam.account.AccountServiceProxy;
import game.worldsrv.character.HumanMirrorObject;
import game.worldsrv.character.HumanObjectServiceProxy;
import game.worldsrv.character.PartnerObject;
import game.worldsrv.common.GamePort;
import game.worldsrv.common.GameServiceBase;
import game.worldsrv.config.ConfItem;
import game.worldsrv.entity.FillMail;
import game.worldsrv.entity.FriendObject;
import game.worldsrv.entity.Human;
import game.worldsrv.entity.ItemBody;
import game.worldsrv.entity.Unit;
import game.worldsrv.fightrecord.RecordInfo;
import game.worldsrv.friend.FriendServiceProxy;
import game.worldsrv.guild.GuildInstData;
import game.worldsrv.humanSkill.SkillGodsJSON;
import game.worldsrv.humanSkill.SkillJSON;
import game.worldsrv.humanSkill.SkillTrainJSON;
import game.worldsrv.inform.InformManager;
import game.worldsrv.item.Item;
import game.worldsrv.mail.MailManager;
import game.worldsrv.produce.ProduceVo;
import game.worldsrv.support.D;
import game.worldsrv.support.Log;
import game.worldsrv.support.LogOpUtils;
import game.worldsrv.support.Utils;
import game.worldsrv.support.observer.EventKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.Message;

import core.CallPoint;
import core.Port;
import core.connsrv.ConnectionProxy;
import core.dbsrv.DB;
import core.gen.proxy.DistrClass;
import core.gen.proxy.DistrMethod;
import core.scheduler.ScheduleMethod;
import core.support.Param;
import core.support.TickTimer;
import core.support.Time;

@DistrClass(servId = D.SERV_HUMAN_GLOBAL, importClass = {HumanGlobalInfo.class, EInformType.class, List.class,
		Message.class, FillMail.class , FriendObject.class, ItemBody.class, Unit.class, 
		HumanMirrorObject.class, RecordInfo.class, ETeamType.class, ECrossFightType.class,GuildInstData.class })
public class HumanGlobalService extends GameServiceBase {
	private final int COUNT100Per10SEC = 500;// 100
	
	private TickTimer logTimer = new TickTimer(300000); //写日志时间

	private TickTimer msgPulseTimer = new TickTimer(500); // 控制广播发送频率
	private Map<HumanGlobalInfo, SCInformMsgAll.Builder> msgMap = new LinkedHashMap<>();
	private TickTimer msgCrossPulseTimer = new TickTimer(500); // 跨服控制广播发送频率
	private Map<HumanGlobalInfo, SCInformCrossMsgAll.Builder> crossMsgMap = new LinkedHashMap<>();

	private TickTimer scheduleTimer = new TickTimer(10000); // 调度的处理
	private Map<Integer, Long> shceduleMap = new LinkedHashMap<>();

	// 玩家状态信息
	private Map<Long, HumanGlobalInfo> datas = new HashMap<>();
	
	// 向account同步服务器人数满员状态
	public static int ACCOUNTTIME = 10;			//检查超时时间
	private TickTimer accountOnlineFullTimer = new TickTimer(ACCOUNTTIME * Time.SEC);
	
	AccountServiceProxy accountServ;
		
	// 检查是否存在异常数据时用于记录待删除的数据
	private List<HumanGlobalInfo> removeList = new LinkedList<>();
	private TickTimer checkTimer = new TickTimer(Time.MIN); // 检查，每分钟检查一次

	public HumanGlobalService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		accountServ = AccountService.createProxy();
	}
	
	@Override
	public void pulseOverride() {
		long now = Port.getTime();

		// 延迟处理统一发送
		if (msgPulseTimer.isPeriod(now)) {
			sendInfomAll();
		}
		if (msgCrossPulseTimer.isPeriod(now)) {
			sendInfomCrossAll();
		}

		if (scheduleTimer.isPeriod(now)) {
			onSchdule();
		}
		
		// 每分钟检查一次所有玩家的同步时间是否超过5分钟，超过则踢下线
		if(checkTimer.isPeriod(now)){
			checkTime(now);
			//Log.game.info("========= 每分钟打印下在线人数：{} =========", datas.size());//sjhtest
		}
				
		// 写日志
		if(logTimer.isPeriod(now)) {
			writeOnlineLog();
		}
		
		// 向account同步服务器人数满员状态
		pulseAccountOnlineFull(now);
	}
	
	/**
	 * 向account同步服务器人数满员状态
	 * @param now
	 */
	private void pulseAccountOnlineFull(long now) {
		if(!accountOnlineFullTimer.isPeriod(now)) {
			return;
		}
		
		// 当前人数
		int num = datas.size();
		//Log.temp.info("pulseAccountOnlineFull:num============{}",num);
		// 同步到account服务器
		accountServ.setHumanOnlineFull(num);
	}
	
	/**
	 * 每分钟检查一次所有玩家的同步时间是否超过5分钟，超过则踢下线
	 */
	private void checkTime(long now) {
		removeList.clear();
		for (HumanGlobalInfo info:datas.values()) {
			// 超过5分钟没同步时间的玩家踢下线（同步时间固定30秒更新一次）
			if(info.timeSync > 0 && (info.timeSync + 5 * Time.MIN) < now) {
				removeList.add(info);
			}
		}
		if (removeList.size() > 0) {
			for (HumanGlobalInfo info:removeList) {
				Log.game.info("------------------ 超过5分钟没同步时间的玩家踢下线：id={},name={}", info.id, info.name);
				this.cancel(info.id);
			}
		}
	}
	
	private void sendInfomAll() {
		for (Map.Entry<HumanGlobalInfo, SCInformMsgAll.Builder> entry : msgMap.entrySet()) {
			HumanGlobalManager.inst().sendMsg(entry.getKey().connPoint, entry.getValue().build());
		}
		msgMap.clear();
	}
	/**
	 * 发送跨服消息
	 */
	private void sendInfomCrossAll(){
		for (Map.Entry<HumanGlobalInfo, SCInformCrossMsgAll.Builder> entry : crossMsgMap.entrySet()) {
			HumanGlobalManager.inst().sendMsg(entry.getKey().connPoint, entry.getValue().build());
		}
		crossMsgMap.clear();
	}
	
	private void onSchdule() {
		if (shceduleMap.isEmpty() || shceduleMap.size() < 0) {
			return;
		}

		int i = 0;
		for (HumanGlobalInfo obj : datas.values()) {
			boolean sendSchd = false;
			for (Entry<Integer, Long> entry : shceduleMap.entrySet()) {
				// 最后一次执行调度的时间 != 请求执行调度的时间，才执行调度
				if (obj.timeSchedule < entry.getValue()) {
//					obj.timeSchedule = entry.getValue();
					HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(obj.nodeId, obj.portId, obj.id);
                    Log.game.info("-------------- human onSchedule {} {}", entry.getKey(), entry.getValue());
					prxHumanObj.onSchedule(entry.getKey(), entry.getValue());
					sendSchd = true;
				}
			}
			
			obj.timeSchedule = Port.getTime();
			
			if (sendSchd) {
				i++;
				// 超过数量返回
				if (i > COUNT100Per10SEC) {
					return;
				}
			}
		}
		if (i == 0 && !shceduleMap.isEmpty()) {
			shceduleMap.clear();
		}
	}
	
	/**
	 * 每个整点执行一次
	 */
	@ScheduleMethod(Utils.cron_Day_Hour)
	public void _cron_Day_Min() {
		long timeNow = Port.getTime();
		Log.game.info("==_cron_Day_Hour ");
		shceduleMap.put(EventKey.ResetDailyHour, timeNow);
	}
	
	@ScheduleMethod(Utils.cron_Day_Min)
	public void _cron_Day_Hour() {
		long timeNow = Port.getTime();
		shceduleMap.put(EventKey.EVERY_MIN, timeNow);
	}
	/**
	 * 30秒同步一次，用于把异常玩家踢下线
	 * @param humanId
	 */
	@DistrMethod
	public void syncInfoTime(long humanId) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.timeSync = Port.getTime();
		}
	}
	
	/**
	 * 获取一个玩家的全局信息
	 * @param humanId
	 */
	@DistrMethod
	public void getInfo(long humanId) {
		port.returns(datas.get(humanId));
	}
	
	/**
	 * 获取一批玩家的全局信息
	 */
	@DistrMethod
	public void getInfoList(List<Long> humanIdList) {
		List<HumanGlobalInfo> hgList = getHumanInfos(humanIdList);
		port.returns(hgList);
	}
	private List<HumanGlobalInfo> getHumanInfos(List<Long> humanIdList){
		List<HumanGlobalInfo> hgList = new ArrayList<HumanGlobalInfo>();
		for (Long humanId : humanIdList) {
			if (null == humanId) {
				continue;
			}
			if (datas.containsKey(humanId)) {
				hgList.add(datas.get(humanId));
			}
		}
		return hgList;
	}
	
	/**
	 * 获取一批玩家的全局信息
	 */
	@DistrMethod
	public void getInfoMap(List<Long> humanIdList) {
		Map<Long, HumanGlobalInfo> friendDBMap = new HashMap<Long, HumanGlobalInfo>();
		for (Long humanId : humanIdList) {
			HumanGlobalInfo hgInfo = datas.get(humanId);
			if(null != hgInfo){
				friendDBMap.put(hgInfo.id, hgInfo);
			}
		}
		port.returns(friendDBMap);
	}

	/**
	 * 注册玩家全局信息
	 * @param status
	 */
	@DistrMethod
	public void register(HumanGlobalInfo status) {
        Log.game.info("--------------- register {}", status);
		datas.put(status.id, status);
	}
	
	/**
	 * 更新vip等级
	 * @param humanId
	 * @param vipLv
	 */
	@DistrMethod
	public void updateVipLv(long humanId,int vipLv){
		HumanGlobalInfo info = datas.get(humanId);
		if (info != null) {
			info.vipLv = vipLv;
		}
	}
	
	/**
	 * 更新竞技场排行榜
	 * @param humanId
	 * @param competeRank
	 */
	@DistrMethod
	public void updateCompeteRank(long humanId, int competeRank) {
		HumanGlobalInfo info = datas.get(humanId);
		if (info != null) {
			info.competeRank = competeRank;
		}
	}
	/**
	 * 更新副本星星总数
	 * @param humanId
	 * @param instStar
	 */
	@DistrMethod
	public void updateInstStar(long humanId, int instStar) {
		HumanGlobalInfo info = datas.get(humanId);
		if (info != null) {
			info.instStar = instStar;
		}
	}

	/**
	 * 更新玩家全局信息之：名字，等级，VIP等级，战斗力, 公会id, 模型, 称号
	 */
	@DistrMethod
	public void updateHumanInfo(long humanId, String name, int level, int vipLv, int combat, long unionId,int modelSn, int defaultModelSn,
			int titleSn, int partnerStance) {
		HumanGlobalInfo info = datas.get(humanId);
		if (info != null) {
			info.name = name;
			info.level = level;
			info.vipLv = vipLv;
			info.combat = combat;
			info.guildId = unionId;
			info.modelSn = modelSn;
			info.defaultModelSn = defaultModelSn;
			info.titleSn = titleSn;
			info.partnerStance = partnerStance;
		}
		port.returns("humanGlobalInfo", info);
	}
	
	/**
	 * 更新玩家全局信息：属性
	 */
	@DistrMethod
	public void updateProp(long humanId, Unit unit) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.unit = unit;
		}
	}

	/**
	 * 更新伙伴全局伙伴信息
	 * @param partnerList
	 */
	@DistrMethod
	public void updatePartnerInfo(long humanId, String lineup, int stance, List<PartnerObject> partnerList) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.lineup = lineup;
			info.stance = stance;
			info.partnerObjList = partnerList;
		}
	}
	
	/**
	 * 更新主角技能信息
	 */
	@DistrMethod
	public void updateSkillInfo(long humanId, List<SkillJSON> skillList, List<SkillGodsJSON> skillGodsList, 
			int installGods, List<SkillTrainJSON> skillTrainList) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.skillList = skillList;
			info.skillGodsList = skillGodsList;
			info.installGods = installGods;
			info.skillTrainList = skillTrainList;
		}
	}
	
	/**
	 * 更新主角技能信息
	 */
	@DistrMethod
	public void updateSkillList(long humanId, List<SkillJSON> skillList) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.skillList = skillList;
		}
	}
	
	/**
	 * 更新主角爆点信息
	 */
	@DistrMethod
	public void updateSkillGodsList(long humanId, List<SkillGodsJSON> skillGodsList) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.skillGodsList = skillGodsList;
		}
	}
	
	/**
	 * 更新主角上阵爆点sn
	 */
	@DistrMethod
	public void updateSkillGodsSn(long humanId, int installGods) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.installGods = installGods;
		}
	}
	
	/**
	 * 更新主角技能神通信息
	 */
	@DistrMethod
	public void updateSkillTrainList(long humanId, List<SkillTrainJSON> skillTrainList) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.skillTrainList = skillTrainList;
		}
	}
	
	/**
	 * 更新主角装备
	 * @param humanId
	 * @param equip 需要更新的装备
	 */
	@DistrMethod
	public void updateEquip(long humanId, ItemBody equip) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			for (Item equipItem : info.equipList) {
				if (equipItem.getType() == equip.getType()) {
					equipItem = equip;
					break;
				}
			}
		}
	}
	
	/**
	 * 更新主角全部装备
	 * @param humanId
	 */
	@DistrMethod
	public void updateEquip(long humanId, List<ItemBody> equipList) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			info.equipList = equipList;
		}
	}
	
	
	
	@DistrMethod
	public void changeConnPoint(Long id, CallPoint point) {
		HumanGlobalInfo info = datas.get(id);
		if (info != null) {
			info.connPoint = point;
		}
	}

	/**
	 * 玩家是否已登录
	 * @param account
	 */
	@DistrMethod
	public void isLogined(String account) {
		boolean logined = false;
		long humanId = 0;
		HumanGlobalInfo humanInfo = null;
		for (HumanGlobalInfo h : datas.values()) {
			if (StringUtils.equals(account, h.account)) {
				logined = true;
				humanId = h.id;
				humanInfo = h;
				break;
			}
		}

		port.returns("logined", logined, "humanId", humanId, "humanInfo", humanInfo);
	}

	/**
	 * 踢出玩家
	 * @param id
	 * @param reason
	 */
	@DistrMethod
	public void kick(long id, int reason) {
		kickHuman(id, reason);
	}

	/**
	 * 踢人下线
	 */
	private void kickHuman(long humanId, int reason) {
		// 发送消息 通知玩家被踢
		SCHumanKick.Builder msg = SCHumanKick.newBuilder();
		msg.setReason(reason);
		sendMsg(humanId, msg.build());
		
		HumanGlobalInfo info = datas.get(humanId);// 玩家连接信息
		if(null == info)
			return;
		
		// 断开连接
		ConnectionProxy prx = ConnectionProxy.newInstance(info.connPoint);
		Log.human.debug("kickHuman close");
		prx.close();

		// 直接清除玩家的一些信息 因为如果玩家连接
		HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, humanId);
		prxHumanObj.kickClosed((long) info.connPoint.servId);// 会调用cancel,但有例外

		if (datas.containsKey(humanId)) {
			if (Log.human.isDebugEnabled()) {
				Log.human.debug("HumanGlobalService.kickHuman：cancel humanId={}, reason={}", humanId, reason);
			}
            Log.human.info("------------------- HumanGlobalService.kickHuman：cancel humanId={}, reason={}", humanId, reason);
			this.cancel(humanId);// 防止例外情况，没被调用，导致登录不了
		}
	}

	/**
	 * 发送消息至玩家
	 */
	@DistrMethod(argsImmutable=true)
	public void sendMsg(long humanId, Message msg) {
		// 玩家连接信息
		HumanGlobalInfo info = datas.get(humanId);
		if (info != null) {
			HumanGlobalManager.inst().sendMsg(info.connPoint, msg);
		}
	}

	/**
	 * 发送消息至全服玩家，有要排除的则设置excludeIds
	 * @param excludeIds
	 * @param msg
	 */
	@DistrMethod(argsImmutable=true)
	public void sendMsgToAll(List<Long> excludeIds, Message msg) {
		if (null == excludeIds) {
			excludeIds = new ArrayList<>();
		}
		// 给所有玩家发送消息
		for (HumanGlobalInfo info : datas.values()) {
			// 排除不需要发送的玩家id
			if (excludeIds.contains(info.id)) {
				continue;
			}
			HumanGlobalManager.inst().sendMsg(info.connPoint, msg);
		}
	}
	
	/**
	 * 发送给指定的玩家
	 * @param humanIds
	 * @param msg
	 */
	@DistrMethod(argsImmutable=true)
	public void sendMsgTo(List<Long> humanIds, Message msg) {
		if (null == humanIds || humanIds.isEmpty()) {
			return;
		}
		// 给所有玩家发送消息
		for (HumanGlobalInfo info : datas.values()) {
			// 排除不需要发送的玩家id
			if(humanIds.contains(info.id)) {
				HumanGlobalManager.inst().sendMsg(info.connPoint, msg);
			}
		}
	}

	/**
	 * 清除玩家全局信息
	 */
	@DistrMethod
	public void cancel(long humanId) {
	    Log.game.info("------------------------- cancel {}", humanId);
		datas.remove(humanId);
	}

	@DistrMethod
	public void stageIdModify(long humanId, long stageIdNew, String stageName, String nodeId, String portId) {
		HumanGlobalInfo info = datas.get(humanId);

		// 特殊处理
		if (info == null) {
			Log.human.warn("修改玩家全局地图信息时出错，玩家数据不存在：" + "humanId={}, stageIdNew={}, stageName={}, nodeId={}, portId={}",
					humanId, stageIdNew, stageName, nodeId, portId);
			return;
		}

		// 更新地图ID
		info.stageId = stageIdNew;
		info.stageName = stageName;
		info.nodeId = nodeId;
		info.portId = portId;
	}
	
	@DistrMethod
	public void sendMailOnline(String... produces) {
		// 所有在线玩家
		List<HumanGlobalInfo> receiver = new ArrayList<>();
		receiver.addAll(datas.values());
		// 要发送的消息以及物品
		List<ProduceVo> list = new ArrayList<ProduceVo>();
		if (produces.length > 2) {
			for (int i = 3; i < produces.length; i += 2) {
				int itemSn = Utils.intValue(produces[i]);
				int itemNum = Utils.intValue(produces[i + 1]);
				ConfItem confItem = ConfItem.get(itemSn);
				if(null != confItem){// 物品存在才能添加
					ProduceVo pr = new ProduceVo(itemSn, itemNum);
					list.add(pr);
				}
			}
		}
		// 循环发送消息
		for (HumanGlobalInfo hg : receiver) {
			MailManager.inst().sendSysMail(hg.id, produces[1], produces[2], list);
		}
	}

	@DistrMethod
	public void sendInform(EInformType type, List<Long> keys, String content, Long sendHumanId, int num, int interval) {
		// 获取符合条件的玩家
		List<HumanGlobalInfo> receiver = getInfoByScope(type, keys);
		// 发送人
		HumanGlobalInfo sender = null;
		if (sendHumanId != null) {
			sender = datas.get(sendHumanId);
		}

		/* 拼聊天消息 */
		SCInformMsg.Builder msgBuild = SCInformMsg.newBuilder();
		msgBuild.setType(type);
		msgBuild.setNum(num);
		msgBuild.setInterval(interval);
		if (content != null) {
			msgBuild.setContent(content);
		}
		// 发送人
		if (sender != null) {
			msgBuild.setSendHumanId(sender.id);
			msgBuild.setSendHumanName(sender.name);
			msgBuild.setSnedVip(sender.vipLv);
			msgBuild.setHeadId(sender.headSn);
			msgBuild.setSnedLv(sender.level);
			// msgBuild.setSendHumanVIPLevel(sender.vipSn);
		}

		// 私聊特殊处理 "[张三]对 你 说，你 对[李四]说 " 所以要设置接受者信息
		// 私聊肯定是1v1的
		if (type == EInformType.PrivateInform && !receiver.isEmpty()) {
			HumanGlobalInfo r = receiver.get(0);
			msgBuild.setReceiveHumanId(r.id);
			msgBuild.setReceiveHumanName(r.name);
			msgBuild.setReceiveVip(r.vipLv);
			msgBuild.setReceiveLv(r.level);
			msgBuild.setHeadId(r.headSn);
		}

		// 循环发送消息
		for (HumanGlobalInfo r : receiver) {
			SCInformMsgAll.Builder msg = msgMap.get(r);
			if (msg == null) {
				msg = SCInformMsgAll.newBuilder();
			}
			msg.addInformMsg(msgBuild);
			msgMap.put(r, msg);
			//HumanGlobalManager.inst().sendMsg(r.connPoint, msg);
		}

		// 目标玩家不在线 提示发送者
		if (type == EInformType.PrivateInform && sender != null) {
			// 目标玩家不在线
			if (receiver.isEmpty()) {
				InformManager.inst().ErrorInform(sender.id, "消息发送失败，对方不在线");
				return;
			}
		}
	}
	
	/**
	 * 弹幕
	 * @param serverId
	 * @param content
	 * @param name
	 */
	@DistrMethod
	public void sendCrossInform(int serverId, String name, String content, int icon, int aptitude) {
		/* 拼聊天消息 */
		DInformCrossChat.Builder msgBuild = DInformCrossChat.newBuilder();
		if (null == content || content.isEmpty()) {//如果是空消息就不发送
			return;
		}
		msgBuild.setServerId(serverId);//服务器ID
		msgBuild.setContent(content);
		msgBuild.setName(name);// 发送人
		msgBuild.setIcon(icon);// 头像
		msgBuild.setAptitude(aptitude);// 资质
		
		// 循环发送消息
		for (HumanGlobalInfo r : datas.values()) {
			SCInformCrossMsgAll.Builder msg = crossMsgMap.get(r);
			if (msg == null) {
				msg = SCInformCrossMsgAll.newBuilder();
			}
			msg.addInformMsg(msgBuild);
			crossMsgMap.put(r, msg);
//			HumanGlobalManager.inst().sendMsg(r.connPoint, msg.build());
		}
	}

	private List<HumanGlobalInfo> getInfoByScope(EInformType type, List<Long> keys) {
		List<HumanGlobalInfo> list = new ArrayList<>();
		switch (type) {
			case WorldInform: 
			case GMInform: 
			case SystemInform: 
			case RumorInform: 
			case HornInform: {
				list.addAll(datas.values());
			}	break;
			case PrivateInform: 
			case WarnInform: 
			case ErrorInform: 
			case DialogInform: {
				for (long key : keys) {
					HumanGlobalInfo info = datas.get(key);
					if (info != null) {
						list.add(info);
					}
				}
			}	break;
			case MapInform: {
				for (HumanGlobalInfo i : datas.values()) {
					if (keys.contains(i.stageId))
						list.add(i);
				}
			}	break;
			case GuildInform: {
				for (HumanGlobalInfo i : datas.values()) {
					if (keys.contains(i.guildId))
						list.add(i);
				}
			}	break;
			case TeamInform: {
				for (HumanGlobalInfo i : datas.values()) {
					if (keys.contains(i.teamId))
						list.add(i);
				}
			}	break;
			default:
				break;
		}
		return list;
	}
	
	/**
	 * 设置gm权限
	 * @param humanId
	 * @param isOpen 0无任何权限，1gm权限
	 */
	@DistrMethod
	public void setGm(long humanId, boolean isOpen){
		HumanGlobalInfo info = datas.get(humanId);
		int value = 0;
		if(isOpen){
			value = 1;
		}
		//更新数据
		if(info != null) {
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
			prx.setGm(value);
		} else {
			//更数据库
			DB db = DB.newInstance(Human.tableName);
			db.sql(false, true, Utils.createStr("update {} set {}={} where id={}", Human.tableName, Human.K.GMPrivilege, value, humanId));
		}
	}
	
	/**
	 * 禁言/封号
	 * @param humanId
	 * @param type (1:禁言   2:封号)
	 * @param endTime
	 */
	@DistrMethod
	public void sealAccount(long humanId, int type, long endTime) {
		HumanGlobalInfo info = datas.get(humanId);
		
		//更新数据
		if(info != null) {
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
			prx.sealAccount(type, endTime);
		} else {
			//更数据库
			DB db = DB.newInstance(Human.tableName);
			String field;
			if(1 == type){
				field = Human.K.SilenceEndTime;
			}else{
				field = Human.K.SealEndTime;
			}
			db.sql(false, true, Utils.createStr("update {} set {}={} where id={}", Human.tableName, field, endTime, humanId));
		}
	}
	
	/**
	 * 禁言
	 * @param humanId 玩家D
	 * @param campType 操作者的阵营类型
	 * @param keepTime 持续时间(单位:毫秒)
	 */
	@DistrMethod
	public void silence(long humanId, int campType, long keepTime){
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {// 在线
//			if(operCampType > 0 && operCampType != info.campType){
//				Inform.sendInform(operHumanId, 7317);
//				return;
//			}
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
			prx.silence(keepTime);
			String content = "你被禁言至 "+ Utils.formatTime(keepTime, "yyyy-MM-dd HH:mm:ss")+" 解封";
			// 禁言成功
			InformManager.inst().WarnInform(humanId, content);
		} else {// 不在线
			String sql = Utils.createStr(" update `{}` set `{}` = ? where `{}`=?"
					, Human.tableName, Human.K.SilenceEndTime, Human.K.id);
			DB db = DB.newInstance(Human.tableName);
			db.sql(false, false, sql, keepTime, humanId);
			
			//不在线，添加代办
//			Map<String, Object> paramMap = new HashMap<String, Object>();
//			paramMap.put("operCampType",operCampType);
//			paramMap.put("keepTime",keepTime);
//			Pocket.add(humanId, PocketLineKey.SILENCE, Utils.toJSONString(paramMap));
		}
	}
	
	/**
	 * 给玩家禁言
	 */
	@DistrMethod
	public void setSilenceEndTime(long humanId) {
		HumanGlobalInfo info = datas.get(humanId);
		if (info != null) {
			HumanObjectServiceProxy prx = HumanObjectServiceProxy
					.newInstance(info.nodeId, info.portId, info.id);
			prx.setSilenceEndTime();
		}
	}
	
	/**
	 * 查询在线人数
	 */
	@DistrMethod
	public void queryOnlineNum(){
		port.returns(datas.size());
	}
	
	/**
	 * 发送补偿邮件(每发送100个人，等待1s)
	 */
	@DistrMethod
	public void sendFillMail(FillMail fillMail){
		
		if(fillMail.getStartTime() > Port.getTime()){
			//未到邮件发送时间,返回
			return;
		}
		
		int index = 0;
		for(HumanGlobalInfo info : datas.values()){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId,info.id);
			prx.acceptFillMail(fillMail);
			index++;
			if(index % 100 == 0){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 统计注册人数和在线人数,写日志
	 */
	public void writeOnlineLog() {
		//获得玩家数量
		DB db = DB.newInstance(Human.tableName);
		db.countAll(false);
		db.listenResult(this::_result_writeOnlineLog);
	}
	
	public void _result_writeOnlineLog(Param results, Param context) {
		int registerCount = results.get();
		int onlineCount = datas.size();
		
		//添加在线日志:注册人数,在线人数,统计时的时间戳,日期串
		LogOpUtils.LogOnline(registerCount, onlineCount);
	}
	
	/**
	 * 修改好友修改
	 */
	@DistrMethod
	public void FriendChange(long humanId, FriendObject object){
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy
					.newInstance(info.nodeId, info.portId, info.id);
			prx.updateFriendHumanObject(humanId, object);
		}
	}
	@DistrMethod
	public void disposeHumanFriend(long humanA,int type,long time,long humanB){
		FriendServiceProxy prx = FriendServiceProxy.newInstance();
		prx.disposeHumanFriend(datas.get(humanB), humanA, humanB, type, time);
	}
	/**
	 * 根据条件筛选在线玩家
	 * @param humanId
	 */
	@DistrMethod
	public void getRecommendNewUsers(long humanId,List<Long> humanIds){
		List<HumanGlobalInfo> recommendList = new ArrayList<HumanGlobalInfo>();
		int getSize = 200;// 默认从服务端提前200个
		for (HumanGlobalInfo info : datas.values()) {
			if (getSize <= 0) {
				break;
			}
			//如果是自己，则不推荐
			if (humanId == info.id) continue;
			if (humanIds.contains(info.id)) continue;
			recommendList.add(info);
			getSize --;
		}
		Collections.shuffle(recommendList);//将在线玩家随机打乱
		int size = 20;// 默认为20个 防止配表中没有数据
//	TODO 读配置表	
//		ConfGlobalParam conf = ConfGlobalParam.get(24006);
//		if (conf != null) {
//			size = conf.value;
//		}
		List<HumanGlobalInfo> userList = new ArrayList<>();
		if (recommendList.size() > size) {
			userList.addAll(recommendList.subList(0, size));
		}else{
			userList.addAll(recommendList);
		}
		port.returns("userList", userList);
	}
	
	/**
	 * 调用玩家实际线程处理
	 */
	@DistrMethod
	public void doHumanInCurrentThread(long humanId, String className, String methodName, 
			Param results, Param context) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
			prx.doHumanInCurrentThread(className, methodName, results, context);
		}
	}
	
	/**
	 * 每个service预留空方法
	 * @param objs
	 */
	@DistrMethod
	public void update(Object... objs) {
		
	}
	/**************************************************************************************************************
	 * 跨服消息
	 **************************************************************************************************************/
	/**
	 * 竞技PVP匹配超时
	 */
	@DistrMethod
	public void pvpMatchTimeOut(long humanId){
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy
					.newInstance(info.nodeId, info.portId, info.id);
//			prx.pvpMatchTimeOut();
		}
	}
	
	/**
	 * 竞技PVP取消成功
	 */
	@DistrMethod
	public void pvpMatchCancelOK(long humanId){
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
//			prx.pvpMatchCancelOK();
		}
	}

	/**
	 * pvp 匹配结果通知
	 * @param humanId
	 * @param type
	 * @param token
	 * @param crossIp
	 * @param crossPort
	 * @param teamCamp
	 * @param enemy
	 * @param nodeId
	 * @param stageId
	 */
	@DistrMethod
	public void pvpMatchResult(long humanId, int type, String token, String crossIp, int crossPort, int teamCamp, 
			HumanMirrorObject enemy, String nodeId, long stageId,int stageSn,int mapSn) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy
					.newInstance(info.nodeId, info.portId, info.id);
			prx.pvpMatchResult(humanId, type, token, crossIp,crossPort,teamCamp ,enemy, nodeId, stageId,stageSn,mapSn);
		}
		
	}
	
	@DistrMethod
	public void pvpFinishFight(long humanId, int type, int win, Message msg){
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy
					.newInstance(info.nodeId, info.portId, info.id);
			prx.pvpFinishFight(humanId, type, win, msg);
		}
	}

	@DistrMethod
	public void pvpLeaveFight(long humanId, int type){
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy
					.newInstance(info.nodeId, info.portId, info.id);
			prx.pvpLeaveFight(humanId, type);
		}
	}

	/**
	 * 观看录像结果通知
	 * @param humanId
	 * @param token
	 * @param crossIp
	 * @param crossPort
	 * @param stageId
	 * @param stageSn
	 * @param mapSn
	 */
	@DistrMethod
	public void replayRecordResult(long humanId, int result, int fightType, String token, 
			String crossIp, int crossPort, long stageId, int stageSn, int mapSn, RecordInfo record) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			HumanObjectServiceProxy prx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
			prx.replayRecordResult(humanId, result, fightType, token, 
					crossIp, crossPort, stageId, stageSn, mapSn, record);
		}
	}
	/**
	 * 快速战斗结果通知
	 */
	@DistrMethod
	public void quickFightResult(int result,int type, HumanMirrorObject humanMirrorObj, Param param){
		HumanGlobalInfo info = datas.get(humanMirrorObj.getHumanMirror().getId());
		if(info != null){
			HumanObjectServiceProxy prx = HumanObjectServiceProxy
					.newInstance(info.nodeId, info.portId, info.id);
			prx.quickFightResult(result,type, humanMirrorObj, param);
		}
	}
	
	/**
	 * 更新工会名
	 * @param id
	 * @param name
	 */
	@DistrMethod
	public void updatGuildName(Long id, String name) {
		HumanGlobalInfo info = datas.get(id);
		if (info != null) {
			info.guildName = name;
		}
	}
	
	/**
	 * 记录并发送战斗信息
	 * @param humanId
	 * @param teamType
	 * @param fightType
	 * @param mapSn
	 * @param stageId
	 */
	@DistrMethod
	public void sendFightInfo(long humanId, ETeamType teamType, ECrossFightType fightType, int mapSn, long stageId) {
		HumanGlobalInfo info = datas.get(humanId);
		if(info != null) {
			HumanObjectServiceProxy prx = HumanObjectServiceProxy
					.newInstance(info.nodeId, info.portId, info.id);
			prx.sendFightInfo(teamType, fightType, mapSn, stageId);
		}
	}

	@DistrMethod
    public void syncGuildInstData(long humanId, GuildInstData inst) {
        HumanGlobalInfo info = datas.get(humanId);
        if(info != null) {
            HumanObjectServiceProxy prx = HumanObjectServiceProxy
                    .newInstance(info.nodeId, info.portId, info.id);
            prx.syncGuildInstData(inst);
        }
    }
}